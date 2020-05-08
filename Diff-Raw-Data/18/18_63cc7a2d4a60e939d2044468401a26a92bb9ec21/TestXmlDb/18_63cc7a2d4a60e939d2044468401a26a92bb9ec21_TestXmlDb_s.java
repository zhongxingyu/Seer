 package com.razie.dist.db.test;
 
 import junit.framework.TestCase;
 
 import com.razie.dist.db.XmlDb;
import com.razie.pub.base.AttrAccess;
 import com.razie.pub.base.log.Log;
 
 public class TestXmlDb extends TestCase {
     static String newdbname  = null;
     static String syncdbname = null;
 
     public void setUp() {
         if (newdbname == null) {
             newdbname = "testdb-1";
             syncdbname = newdbname + "_copy";
         }
     }
 
     public void test1Create() {
         new XmlDb();
     }
 
     public void test2Add() {
         XmlDb db = new XmlDb();
         db.initialize("");
        db.add("/db", "node1", new AttrAccess.Impl("attr1", "val1", "attr2", "val2"));
 
         XmlDb db2 = new XmlDb();
         db2.initialize("");
         db2.applyDiffs(0, 1, db.getDiffs(0, 1));
 
         String s = db2.getAttr("/db/node1/@attr1");
         assertTrue("val1".equals(s));
     }
 
     public void test4Update() {
         XmlDb db = new XmlDb();
         db.initialize("");
        db.add("/db", "node1", new AttrAccess.Impl("attr1", "val1", "attr2", "val2"));
        db.add("/db/node1", "newnode", new AttrAccess.Impl("attr3", "newval3"));
 
         XmlDb db2 = new XmlDb();
         db2.initialize("");
         db2.applyDiffs(0, 1, db.getDiffs(0, 1));
 
         db.setAttr("/db/node1", "attr1", "newval1");
         db2.applyDiffs(1, 2, db.getDiffs(1, 2));
 
         String s = db2.getAttr("/db/node1/@attr1");
         assertTrue("newval1".equals(s));
     }
 
     public void test4Remove() {
         XmlDb db = new XmlDb();
         db.initialize("");
        db.add("/db", "node1", new AttrAccess.Impl("attr1", "val1", "attr2", "val2"));
        db.add("/db/node1", "newnode", new AttrAccess.Impl("attr3", "newval3"));
 
         XmlDb db2 = new XmlDb();
         db2.initialize("");
         db2.applyDiffs(0, 1, db.getDiffs(0, 1));
 
        db.delete("/db/node1", "newnode", new AttrAccess.Impl("attr3", "newval3"));
         db2.applyDiffs(1, 2, db.getDiffs(1, 2));
 
         assertTrue(db2.listEntities("/db/node1/newnode").size() == 0);
     }
 
     static final Log logger = Log.Factory.create(TestXmlDb.class.getName());
 }
