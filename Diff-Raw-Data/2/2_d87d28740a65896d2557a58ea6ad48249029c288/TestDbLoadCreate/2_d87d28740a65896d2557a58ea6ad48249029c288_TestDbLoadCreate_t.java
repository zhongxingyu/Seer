 package com.razie.dist.db.test;
 
 import junit.framework.TestCase;
 
 import org.json.JSONObject;
 
 import com.razie.assets.InventoryAssetMgr;
 import com.razie.dist.db.AgentDb;
 import com.razie.pub.agent.Agent;
 import com.razie.pub.agent.AgentFileService;
 import com.razie.pub.base.AttrAccess;
 import com.razie.pub.base.log.Log;
 import com.razie.pub.comms.AgentCloud;
 import com.razie.pub.comms.AgentHandle;
 import com.razie.pub.comms.Agents;
 
 public class TestDbLoadCreate extends TestCase {
 	static String newdbname = null;
 	static String syncdbname = null;
 
 	// TODO simplify using Agents for testing...
 	static AgentHandle me = new AgentHandle("localhost", "localhost",
			"127.0.0.1", "4451", "http://localhost:4451", "ubuntu",
 			"/host/Video/razmutant");
 
 	Agent agent;
 
 	public void setUp() {
 		if (newdbname == null) {
 			AgentCloud group = new AgentCloud(me);
 			agent = new scala.razie.SimpleAgent(me, group).onInit();
 
 			agent.getThreadContext().enter();
 
 			InventoryAssetMgr.init(new InventoryAssetMgr());
 			// give them time to update cloud status
 			try {
 				Thread.sleep(150);
 			} catch (InterruptedException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 
 			newdbname = "testdb-" + Agents.me().name
 					+ System.currentTimeMillis();
 			syncdbname = newdbname + "_copy";
 		}
 	}
 
 	// create a db - no exceptions
 	public void test1Create() {
 		AgentDb db = AgentDb.db(newdbname);
 		db.save(false, true);
 	}
 
 	// add a new entry, save, reload and check
 	public void test2Add() {
 		AgentDb db = AgentDb.db(newdbname);
 		db.xml().add("/db", "node1",
 				new AttrAccess.Impl("attr1", "val1", "attr2", "val2"));
 		db.save(false, true);
 
 		db = AgentDb.reload(newdbname);
 		String s = db.xml().getAttr("/db/node1/@attr1");
 		assertTrue("val1".equals(s));
 	}
 
 	// copy a db
 	public void test3Copy() {
 		// copy is essentially a sync to an empty location
 		AgentDb.sync(syncdbname, Agents.me(), newdbname, true);
 
 		AgentDb db = AgentDb.db(syncdbname);
 		String s = db.xml().getAttr("/db/node1/@attr1");
 		assertTrue("val1".equals(s));
 	}
 
 	public void test4Sync() {
 		AgentDb db = AgentDb.db(newdbname);
 		db.xml().add("/db/node1", "newnode",
 				new AttrAccess.Impl("attr3", "newval3"));
 		db.xml().setAttr("/db/node1", "attr1", "newval1");
 		db.save(false, true);
 
 		AgentDb.sync(syncdbname, Agents.me(), newdbname, true);
 
 		AgentDb sdb = AgentDb.db(syncdbname);
 		String s = sdb.xml().getAttr("/db/node1/@attr1");
 		assertTrue("newval1".equals(s));
 	}
 
 	public void test5List() {
 		AttrAccess m = AgentDb.listLocalDb();
 		Log.logThis("found dbs: " + m.toJson(new JSONObject()).toString());
 	}
 
 	public void test6AnotherDb() {
 		String adb = "anotherdb";
 		AgentDb db = AgentDb.db(adb);
 
 		db.xml().add(
 				"/db",
 				"anode",
 				new AttrAccess.Impl("msec", String.valueOf(System
 						.currentTimeMillis())));
 		db.save(false, true);
 	}
 
 	static final Log logger = Log.Factory.create(TestDbLoadCreate.class
 			.getName());
 }
