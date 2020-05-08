 package com.dymczyk.orientdb;
 
 import com.dymczyk.TestData;
 import com.google.common.base.Stopwatch;
 import com.orientechnologies.orient.core.config.OGlobalConfiguration;
 import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;
 import com.orientechnologies.orient.core.exception.ODatabaseException;
 import com.orientechnologies.orient.core.record.impl.ODocument;
 import com.orientechnologies.orient.core.record.impl.ORecordBytes;
 import com.orientechnologies.orient.server.OServer;
 import com.orientechnologies.orient.server.OServerMain;
 
 public class EmbeddedOrientDB {
 
   public static void main(String[] args) {
     OServer server = null;
     OGlobalConfiguration.STORAGE_KEEP_OPEN.setValue(false);
     ODatabaseDocumentTx db = null;
     try {
       // Start the server
       server = OServerMain.create();
       server.startup(Thread.currentThread().getContextClassLoader().getResourceAsStream("orientdb/orientdb.config"));
 
       // Open th DB
       db = openDb();
 
       // Time blob saving
       Stopwatch watch = Stopwatch.createStarted();
       for(int i = 0; i < TestData.FEW_BLOBS; i++) {
         ODocument blobDoc = new ODocument("Blob");
         ORecordBytes record = new ORecordBytes(db, TestData.getBlob());
         blobDoc.field("key", i);
         blobDoc.field("blob", record);
         blobDoc.save();
       }
       System.out.println("\n"+watch.stop());
 
       // Remove all the blobs
       for (ODocument doc : db.browseClass("Blob")) {
         doc.delete();
       }
     } catch (Exception e) {
       throw new ODatabaseException("Something went wrong with the DB", e);
     } finally {
       db.close();
       server.shutdown();
     }
 
   }
 
   private static ODatabaseDocumentTx openDb() {
    System.out.println(TestData.currDir() + "/src/main/resources/orientdb/database");
     ODatabaseDocumentTx db = new ODatabaseDocumentTx("local:"+ TestData.currDir() + "/src/main/resources/orientdb/database");
     if(!db.exists()) {
       db.create();
     }
     return db.open("admin", "admin");
   }
 
 }
