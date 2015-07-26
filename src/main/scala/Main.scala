package orientest

import com.orientechnologies.orient.core.sql.{OCommandSQL, OLiveCommandExecutorSQLFactory}
import com.orientechnologies.orient.core.query.live.OLiveQueryHook
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx
import com.orientechnologies.orient.core.db.record.ORecordOperation
import com.orientechnologies.orient.core.record.impl.ODocument
import com.orientechnologies.orient.core.sql.query.{OLiveQuery, OLiveResultListener}


object Main extends App {
  OLiveCommandExecutorSQLFactory.init()
  val db = new ODatabaseDocumentTx(s"memory:mylittletest");
  db.create()

  db.activateOnCurrentThread()
  db.registerHook(new OLiveQueryHook(db))

  val ops = scala.collection.mutable.ListBuffer[ORecordOperation]()

  try {
    db.getMetadata.getSchema.createClass("test");
    db.getMetadata.getSchema.createClass("test2");

    val listener = new OLiveResultListener {
      override def onLiveResult(iLiveToken: Int, iOp: ORecordOperation): Unit = {
        ops += iOp
      }
    }

    db.query(new OLiveQuery[ODocument]("live select from test", listener));

    db.command(new OCommandSQL("insert into test set name = 'foo', surname = 'bar'")).execute();
    db.command(new OCommandSQL("insert into test set name = 'foo', surname = 'baz'")).execute();
    db.command(new OCommandSQL("insert into test2 set name = 'foo'"));

    Thread.sleep(3);

    for (op <- ops) {
      println(op.toString)
    }

  } finally {
    db.drop();
  }
}

// case class SQLCmd(cmd: String) 
// 
// class DbWorker() extends Actor {
//   val db = new ODatabaseDocumentTx(s"memory:mylittletest")
// 	db.open("admin","admin")
// 
// 	
// 	def receive = {
// 		case SQLCmd(cmd) =>
// 			db.command(new OCommandSQL(cmd)).execute()
// 			
// 		case _ =>
// 
// 	}
// }