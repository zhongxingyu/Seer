 /*
  * Copyright (c) 2011 Imaginea Technologies Private Ltd.
  * Hyderabad, India
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following condition
  * is met:
  *
  *     + Neither the name of Imaginea, nor the
  *       names of its contributors may be used to endorse or promote
  *       products derived from this software.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
  * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
  * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
  * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
  * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
  * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
  * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package com.imaginea.mongodb.controllers;
 
 import com.imaginea.mongodb.exceptions.ApplicationException;
 import com.imaginea.mongodb.exceptions.DocumentException;
 import com.imaginea.mongodb.exceptions.ErrorCodes;
 import com.imaginea.mongodb.utils.JSON;
 import com.mongodb.BasicDBList;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoException;
 import org.apache.log4j.Logger;
 import org.json.JSONException;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.mock.web.MockHttpServletRequest;
 
 import javax.servlet.http.HttpServletRequest;
 import java.util.ArrayList;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 
 /**
  * Tests the document request dispatcher resource that handles the GET and POST
  * request for documents present in Mongo. Tests the get and post functions
  * mentioned in the resource with dummy request and test document and collection
  * names and check the functionality.
  *
  * @author Rachit Mittal
  * @since 16 Jul 2011
  */
 public class DocumentControllerTest extends TestingTemplate {
 
     /**
      * Object of class to be tested
      */
     private DocumentController testDocumentController;
 
     private static HttpServletRequest request = new MockHttpServletRequest();
     private static String connectionId;
 
     private static Logger logger = Logger.getLogger(DocumentControllerTest.class);
 
     @Before
     public void instantiateTestClass() {
         // Class to be tested
         testDocumentController = new DocumentController();
         connectionId = loginAndGetConnectionId(request);
     }
 
     /**
      * Tests the GET Request which gets names of all documents present in Mongo.
      * Here we construct the test document first and will test if this created
      * document is present in the response of the GET Request made. If it is,
      * then tested ok.
      */
 
     @Test
     public void getDocRequest() throws DocumentException, JSONException {
 
         // ArrayList of several test Objects - possible inputs
         List<String> testDbNames = new ArrayList<String>();
         // Add some test Cases.
         testDbNames.add("random");
         testDbNames.add("");
         testDbNames.add(null);
 
         List<String> testCollNames = new ArrayList<String>();
         testCollNames.add("foo");
         testCollNames.add("");
 
         List<DBObject> testDocumentNames = new ArrayList<DBObject>();
         testDocumentNames.add(new BasicDBObject("test", "test"));
 
         for (final String dbName : testDbNames) {
             for (final String collName : testCollNames) {
                 for (final DBObject documentName : testDocumentNames)
                     TestingTemplate.execute(logger, new ResponseCallback() {
                         public Object execute() throws Exception {
                             try {
                                 if (dbName != null && collName != null) {
                                     if (!dbName.equals("") && !collName.equals("")) {
                                         if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collName)) {
                                             DBObject options = new BasicDBObject();
                                             mongoInstance.getDB(dbName).createCollection(collName, options);
                                         }
 
                                         mongoInstance.getDB(dbName).getCollection(collName).insert(documentName);
                                     }
                                 }
 
                                 String fields = "test,_id";
 
                                 String docList = testDocumentController.executeQuery(dbName, collName, "db." + collName + ".find()", connectionId, fields, "100", "0", "", false, request);
 
                                 DBObject response = (BasicDBObject) JSON.parse(docList);
 
                                 if (dbName == null) {
                                     DBObject error = (BasicDBObject) response.get("response");
                                     String code = (String) ((BasicDBObject) error.get("error")).get("code");
                                     assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
 
                                 } else if (dbName.equals("")) {
                                     DBObject error = (BasicDBObject) response.get("response");
                                     String code = (String) ((BasicDBObject) error.get("error")).get("code");
                                     assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
                                 } else {
                                     if (collName == null) {
                                         DBObject error = (BasicDBObject) response.get("response");
                                         String code = (String) ((BasicDBObject) error.get("error")).get("code");
                                         assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);
 
                                     } else if (collName.equals("")) {
                                         DBObject error = (BasicDBObject) response.get("response");
                                         String code = (String) ((BasicDBObject) error.get("error")).get("code");
                                         assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);// DB
                                         // exists
                                     } else {
                                         DBObject result = (BasicDBObject) response.get("response");
                                         BasicDBList docs = ((BasicDBList) result.get("result"));
                                         for (int index = 0; index < docs.size(); index++) {
                                             DBObject doc = (BasicDBObject) docs.get(index);
                                             if (doc.get("test") != null) {
                                                 assertEquals(doc.get("test"), documentName.get("test"));
                                                 break;
                                             }
 
                                         }
                                         mongoInstance.dropDatabase(dbName);
                                     }
                                 }
 
                             } catch (MongoException m) {
                                ApplicationException e = new ApplicationException(ErrorCodes.GET_DOCUMENT_LIST_EXCEPTION, "GET_DOCUMENT_LIST_EXCEPTION", m.getCause());
                                 throw e;
                             }
                             return null;
                         }
                     });
             }
         }
     }
 
     /**
      * Tests the POST Request which create document in Mongo Db. Here we
      * construct the Test document using service first and then will check if
      * that document exists in the list.
      *
      * @throws DocumentException
      */
     @Test
     public void createDocRequest() throws DocumentException {
 
         // ArrayList of several test Objects - possible inputs
         List<String> testDbNames = new ArrayList<String>();
         // Add some test Cases.
         testDbNames.add("random");
         testDbNames.add("");
         testDbNames.add(null);
 
         List<String> testCollNames = new ArrayList<String>();
         testCollNames.add("foo");
         testCollNames.add("");
 
         List<DBObject> testDocumentNames = new ArrayList<DBObject>();
         testDocumentNames.add(new BasicDBObject("test", "test"));
         for (final String dbName : testDbNames) {
             for (final String collName : testCollNames) {
                 for (final DBObject documentName : testDocumentNames)
                     TestingTemplate.execute(logger, new ResponseCallback() {
                         public Object execute() throws Exception {
                             try {
                                 if (dbName != null && collName != null) {
                                     if (!dbName.equals("") && !collName.equals("")) {
                                         if (!mongoInstance.getDB(dbName).getCollectionNames().contains(collName)) {
                                             DBObject options = new BasicDBObject();
                                             mongoInstance.getDB(dbName).createCollection(collName, options);
                                         }
                                     }
                                 }
 
                                 String resp = testDocumentController.postDocsRequest(dbName, collName, "PUT", documentName.toString(), null, null, connectionId, request);
                                 DBObject response = (BasicDBObject) JSON.parse(resp);
 
                                 if (dbName == null) {
                                     DBObject error = (BasicDBObject) response.get("response");
                                     String code = (String) ((BasicDBObject) error.get("error")).get("code");
                                     assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
 
                                 } else if (dbName.equals("")) {
                                     DBObject error = (BasicDBObject) response.get("response");
                                     String code = (String) ((BasicDBObject) error.get("error")).get("code");
                                     assertEquals(ErrorCodes.DB_NAME_EMPTY, code);
                                 } else {
                                     if (collName == null) {
                                         DBObject error = (BasicDBObject) response.get("response");
                                         String code = (String) ((BasicDBObject) error.get("error")).get("code");
                                         assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);
 
                                     } else if (collName.equals("")) {
                                         DBObject error = (BasicDBObject) response.get("response");
                                         String code = (String) ((BasicDBObject) error.get("error")).get("code");
                                         assertEquals(ErrorCodes.COLLECTION_NAME_EMPTY, code);// DB
                                         // exists
                                     } else {
                                         List<DBObject> documentList = new ArrayList<DBObject>();
 
                                         DBCursor cursor = mongoInstance.getDB(dbName).getCollection(collName).find();
                                         while (cursor.hasNext()) {
                                             documentList.add(cursor.next());
                                         }
 
                                         boolean flag = false;
                                         for (DBObject document : documentList) {
                                             for (String key : documentName.keySet()) {
                                                 if (document.get(key) != null) {
                                                     assertEquals(document.get(key), documentName.get(key));
                                                     flag = true;
                                                 } else {
                                                     flag = false;
                                                     break; // break from inner
                                                 }
                                             }
                                         }
                                         if (!flag) {
                                             assert (false);
                                         }
                                         // Delete the document
                                         mongoInstance.getDB(dbName).getCollection(collName).remove(documentName);
                                     }
                                 }
 
                             } catch (MongoException m) {
                                 ApplicationException e = new ApplicationException(ErrorCodes.DB_CREATION_EXCEPTION, "DB_CREATION_EXCEPTION", m.getCause());
                                 throw e;
                             }
                             return null;
                         }
                     });
             }
         }
     }
 
     // TODO Test update and delete doc
     @AfterClass
     public static void destroyMongoProcess() {
         logout(connectionId, request);
     }
 }
