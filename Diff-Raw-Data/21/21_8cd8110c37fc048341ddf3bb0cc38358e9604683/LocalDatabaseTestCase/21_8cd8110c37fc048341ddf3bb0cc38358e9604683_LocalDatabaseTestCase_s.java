 package org.jcouchdb.db;
 
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.any;
 import static org.hamcrest.Matchers.greaterThan;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.notNullValue;
 import static org.hamcrest.Matchers.nullValue;
 
 import java.io.ByteArrayInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.io.IOUtils;
 import org.jcouchdb.document.Attachment;
 import org.jcouchdb.document.BaseDocument;
 import org.jcouchdb.document.DesignDocument;
 import org.jcouchdb.document.Document;
 import org.jcouchdb.document.DocumentInfo;
 import org.jcouchdb.document.ValueAndDocumentRow;
 import org.jcouchdb.document.ValueRow;
 import org.jcouchdb.document.View;
 import org.jcouchdb.document.ViewAndDocumentsResult;
 import org.jcouchdb.document.ViewResult;
 import org.jcouchdb.exception.DataAccessException;
 import org.jcouchdb.exception.DocumentValidationException;
 import org.jcouchdb.exception.NotFoundException;
 import org.jcouchdb.exception.UpdateConflictException;
 import org.junit.Assert;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.JUnitCore;
 import org.junit.runners.JUnit4;
 import org.slf4j.LoggerFactory;
 import org.svenson.JSON;
 import org.svenson.JSONParser;
 
 /**
  * Runs tests against a real couchdb database running on localhost
  *
  * @author fforw at gmx dot de
  */
 public class LocalDatabaseTestCase
 {
     private JSON jsonGenerator = new JSON();
 
     public final static String COUCHDB_HOST = "localhost";
 
     public final static int COUCHDB_PORT = Server.DEFAULT_PORT;
 
     private static final String TESTDB_NAME = "jcouchdb_test";
 
     private static final String MY_FOO_DOC_ID = "myFoo/DocId";
 
     private static final String BY_VALUE_FUNCTION = "function(doc) { if (doc.type == 'foo') { emit(doc.value,doc); }  }";
 
     private static final String BY_VALUE_TO_NULL_FUNCTION = "function(doc) { if (doc.type == 'foo') { emit(doc.value,null); }  }";
 
     private static final String COMPLEX_KEY_FUNCTION = "function(doc) { if (doc.type == 'foo') { emit([1,{\"value\":doc.value}],doc); }  }";
     
     private static org.slf4j.Logger log = LoggerFactory.getLogger(LocalDatabaseTestCase.class);
 
     public static Database createDatabaseForTest()
     {
        Server server = new ServerImpl(COUCHDB_HOST, COUCHDB_PORT);
         List<String> databases = server.listDatabases();
 
         log.debug("databases = " + databases);
 
         if (!databases.contains(TESTDB_NAME))
         {
             server.createDatabase(TESTDB_NAME);
         }
 
         return new Database(server,TESTDB_NAME);
     }
 
     @Test
     public void testInRightOrder() throws IOException
     {
         recreateTestDatabase();
         createTestDocuments();
         thatMapDocumentsWork();
         thatCreateNamedDocWorks();
         thatUpdateDocWorks();
         thatUpdateConflictWorks();
         testGetAll();
         testCreateDesignDocument();
         queryDocuments();
         queryViewAndDocuments();
         queryDocumentsWithComplexKey();
         thatGetDocumentWorks();
         thatAdHocViewsWork();
         thatNonDocumentFetchingWorks();
         thatBulkCreationWorks();
         thatBulkCreationWithIdsWorks();
         thatUpdateConflictsWork();
         thatDeleteWorks();
         thatDeleteFailsIfWrong();
         thatAttachmentHandlingWorks(); 
         thatViewKeyQueryingFromAllDocsWorks();
         thatViewKeyQueryingFromAllDocsWorks2();
         thatViewKeyQueryingWorks();
         thatViewAndDocumentQueryingWorks();
         testPureBaseDocumentAccess();
         testAttachmentStreaming(); 
         testValidation();
         thatBulkDeletionWorks();
         testThatStatsWorks();
         thatShowsWorks();
         thatViewsWorks(); 
         thatDesignDocumentDeletionWorks();
         thatFindDocumentWorks();
 
     }
     
     
     public void recreateTestDatabase()
     {
         try
         {
            Server server = new ServerImpl(COUCHDB_HOST, COUCHDB_PORT);
             List<String> databases = server.listDatabases();
 
             log.debug("databases = " + databases);
 
             if (databases.contains(TESTDB_NAME))
             {
                 server.deleteDatabase(TESTDB_NAME);
             }
 
             server.createDatabase(TESTDB_NAME);
 
         }
         catch (RuntimeException e)
         {
             log.error("", e);
         }
     }
 
     
     public void createTestDocuments()
     {
         Database db = createDatabaseForTest();
 
         FooDocument foo = new FooDocument("bar!");
 
         assertThat(foo.getId(), is(nullValue()));
         assertThat(foo.getRevision(), is(nullValue()));
 
         db.createDocument(foo);
 
         assertThat(foo.getId(), is(notNullValue()));
         assertThat(foo.getRevision(), is(notNullValue()));
 
         foo = new FooDocument("baz!");
         foo.setProperty("baz2", "Some test value");
 
         db.createDocument(foo);
 
         log.debug("-- resetted database ----------------------------------");
     }
 
     
     public void thatMapDocumentsWork()
     {
         Database db = createDatabaseForTest();
 
         Map<String,String> doc = new HashMap<String, String>();
 
         doc.put("foo", "value for the foo attribute");
         doc.put("bar", "value for the bar attribute");
 
         db.createDocument(doc);
 
         final String id = doc.get("_id");
         assertThat(id, is(notNullValue()));
         assertThat(doc.get("_rev"), is(notNullValue()));
 
         doc = db.getDocument(Map.class, id);
 
         assertThat(doc.get("foo"), is("value for the foo attribute"));
         assertThat(doc.get("bar"), is("value for the bar attribute"));
 
     }
 
     
     public void thatCreateNamedDocWorks()
     {
         FooDocument doc = new FooDocument("qux");
         doc.setId(MY_FOO_DOC_ID);
 
         Database db = createDatabaseForTest();
         db.createDocument(doc);
         assertThat(doc.getId(), is(MY_FOO_DOC_ID));
         assertThat(doc.getRevision(), is(notNullValue()));
 
     }
 
     
     public void thatUpdateDocWorks()
     {
         Database db = createDatabaseForTest();
 
         FooDocument doc = db.getDocument(FooDocument.class, MY_FOO_DOC_ID);
         assertThat(doc.getValue(), is("qux"));
 
         doc.setValue("qux!");
         db.updateDocument(doc);
 
         doc = db.getDocument(FooDocument.class, MY_FOO_DOC_ID);
         assertThat(doc.getValue(), is("qux!"));
     }
 
     
     public void thatUpdateConflictWorks()
     {
         boolean conflict;
         try
         {
         FooDocument doc = new FooDocument("qux");
         doc.setId(MY_FOO_DOC_ID);
 
         createDatabaseForTest().createDocument(doc);
         conflict = false;
         }
         catch(UpdateConflictException e)
         {
             conflict = true;
         }
         
         assertThat(conflict, is(true));
     }
 
     
     public void testGetAll()
     {
         Database db = createDatabaseForTest();
 
         ViewResult<Map> result = db.listDocuments(null,null);
 
         List<ValueRow<Map>> rows = result.getRows();
 
         String json = jsonGenerator.forValue(rows);
         System.out.println("rows = " + json);
 
         assertThat(rows.size(), is(4));
 
     }
 
 //  removed in 0.11 
 //    
 //    public void testGetAllBySeq()
 //    {
 //        Database db = createDatabaseForTest();
 //
 //        ViewResult<Map> result = db.listDocumentsByUpdateSequence(null,null);
 //
 //        List<ValueRow<Map>> rows = result.getRows();
 //
 //        assertThat(rows.size(), is(4));
 //        assertThat(rows.get(0).getKey().toString(), is("1"));
 //        assertThat(rows.get(1).getKey().toString(), is("2"));
 //        assertThat(rows.get(2).getKey().toString(), is("3"));
 //        assertThat(rows.get(3).getKey().toString(), is("5"));   // this one was updated once
 //
 //        String json = jsonGenerator.forValue(rows);
 //        log.debug("rows = " + json);
 //    }
 
     
     public void testCreateDesignDocument()
     {
         Database db = createDatabaseForTest();
 
         DesignDocument designDocument = new DesignDocument("foo");
         designDocument.addView("byValue", new View(BY_VALUE_FUNCTION));
         designDocument.addView("complex", new View(COMPLEX_KEY_FUNCTION));
         log.debug("DESIGN DOC = " + jsonGenerator.dumpObjectFormatted(designDocument));
 
         db.createDocument(designDocument);
 
         DesignDocument doc = db.getDesignDocument("foo");
         log.debug(jsonGenerator.dumpObjectFormatted(doc));
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getId(), is(DesignDocument.PREFIX + "foo"));
         assertThat(doc.getViews().get("byValue").getMap(), is(BY_VALUE_FUNCTION));
         
         assertThat(doc.getProperty("id"), is(nullValue()));
     }
 
     
     public void queryDocuments()
     {
         Database db = createDatabaseForTest();
         ViewResult<FooDocument> result = db.queryView("foo/byValue", FooDocument.class, null, null);
 
         assertThat(result.getRows().size(), is(3));
 
         FooDocument doc = result.getRows().get(0).getValue();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getValue(), is("bar!"));
 
         doc = result.getRows().get(1).getValue();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getValue(), is("baz!"));
 
         doc = result.getRows().get(2).getValue();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getId(), is(MY_FOO_DOC_ID));
         assertThat(doc.getValue(), is("qux!"));
 
     }
 
     
     public void queryViewAndDocuments()
     {
         Database db = createDatabaseForTest();
         ViewAndDocumentsResult<Object,FooDocument> result = db.queryViewAndDocuments("foo/byValue", Object.class, FooDocument.class, null, null);
 
         assertThat(result.getRows().size(), is(3));
 
         FooDocument doc = result.getRows().get(0).getDocument();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getValue(), is("bar!"));
 
         doc = result.getRows().get(1).getDocument();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getValue(), is("baz!"));
 
         doc = result.getRows().get(2).getDocument();
         assertThat(doc, is(notNullValue()));
         assertThat(doc.getId(), is(MY_FOO_DOC_ID));
         assertThat(doc.getValue(), is("qux!"));
 
     }
 
     
     public void queryDocumentsWithComplexKey()
     {
         Database db = createDatabaseForTest();
         ViewResult<FooDocument> result = db.queryView("foo/complex", FooDocument.class, null, null);
 
         assertThat(result.getRows().size(), is(3));
 
         ValueRow<FooDocument> row = result.getRows().get(0);
         assertThat(jsonGenerator.forValue(row.getKey()), is("[1,{\"value\":\"bar!\"}]"));
 
     }
 
     
     public void thatGetDocumentWorks()
     {
         Database db = createDatabaseForTest();
         FooDocument doc = db.getDocument(FooDocument.class, MY_FOO_DOC_ID);
         assertThat(doc.getId(), is(MY_FOO_DOC_ID));
         assertThat(doc.getRevision(), is(notNullValue()));
         assertThat(doc.getValue(), is("qux!"));
 
         log.debug(jsonGenerator.dumpObjectFormatted(doc));
 
     }
 
     
     public void thatAdHocViewsWork()
     {
         Database db = createDatabaseForTest();
         ViewResult<FooDocument>  result = db.queryAdHocView(FooDocument.class, "{ \"map\" : \"function(doc) { if (doc.baz2 == 'Some test value') emit(null,doc);  } \" }", null, null);
 
         assertThat(result.getRows().size(), is(1));
 
         FooDocument doc = result.getRows().get(0).getValue();
         assertThat((String)doc.getProperty("baz2"), is("Some test value"));
     }
 
     
     public void thatNonDocumentFetchingWorks()
     {
         Database db = createDatabaseForTest();
         NotADocument doc = db.getDocument(NotADocument.class, MY_FOO_DOC_ID);
         assertThat(doc.getId(), is(MY_FOO_DOC_ID));
         assertThat(doc.getRevision(), is(notNullValue()));
         assertThat((String)doc.getProperty("value"), is("qux!"));
 
         log.debug(jsonGenerator.dumpObjectFormatted(doc));
 
         doc.setProperty("value", "changed");
 
         db.updateDocument(doc);
 
         NotADocument doc2 = db.getDocument(NotADocument.class, MY_FOO_DOC_ID);
         assertThat((String)doc2.getProperty("value"), is("changed"));
 
     }
 
 
     
     public void thatBulkCreationWorks()
     {
         Database db = createDatabaseForTest();
 
         List<Document> docs = new ArrayList<Document>();
 
         docs.add(new FooDocument("doc-1"));
         docs.add(new FooDocument("doc-2"));
         docs.add(new FooDocument("doc-3"));
         List<DocumentInfo> infos = db.bulkCreateDocuments(docs);
 
         assertThat(infos.size(), is(3));
 
     }
 
     
     public void thatBulkCreationWithIdsWorks()
     {
         Database db = createDatabaseForTest();
 
         List<Document> docs = new ArrayList<Document>();
 
         FooDocument fooDocument = new FooDocument("doc-2");
         fooDocument.setId("second-foo-with-id");
         docs.add(new FooDocument("doc-1"));
 
         docs.add(fooDocument);
         FooDocument fd2 = new FooDocument("doc-3");
         fd2.setId(MY_FOO_DOC_ID);
         docs.add(fd2);
 
         List<DocumentInfo> infos = db.bulkCreateDocuments(docs);
 
         assertThat(infos.size(), is(3));
 
         assertThat(infos.get(0).getId().length(), is(greaterThan(0)));
         assertThat(infos.get(1).getId(), is("second-foo-with-id"));
         
         // conflict results in error and reason being set
         assertThat(infos.get(2).getError().length(), is(greaterThan(0)));
         assertThat(infos.get(2).getReason().length(), is(greaterThan(0)));
 
     }
 
     public void thatUpdateConflictsWork()
     {
         boolean conflict;
         try
         {
             FooDocument foo = new FooDocument("value foo");
             FooDocument foo2 = new FooDocument("value foo2");
             foo.setId("update_conflict");
             foo2.setId("update_conflict");
     
             Database db = createDatabaseForTest();
             db.createDocument(foo);
             db.createDocument(foo2);
             conflict = false;
         }
         catch(UpdateConflictException e)
         {
             conflict = true;
         }
         
         assertThat(conflict, is(true));
 
     }
 
     
     public void thatDeleteWorks()
     {
         FooDocument foo = new FooDocument("a document");
         Database db = createDatabaseForTest();
         db.createDocument(foo);
 
         assertThat(foo.getId(), is ( notNullValue()));
 
         FooDocument foo2 = db.getDocument(FooDocument.class, foo.getId());
 
         assertThat(foo.getValue(), is(foo2.getValue()));
 
         db.delete(foo);
 
         try
         {
             db.getDocument(FooDocument.class, foo.getId());
             throw new IllegalStateException("document shouldn't be there anymore");
         }
         catch(NotFoundException nfe)
         {
             // yay!
         }
     }
 
     public void thatDeleteFailsIfWrong()
     {
         boolean error;
         try
         {
             Database db = createDatabaseForTest();
             db.delete("fakeid", "fakrev");
             error = false;
         }
         catch(DataAccessException e)
         {
             error = true;
         }
         
         assertThat(error,is(true));
     }
 
     private int valueCount(ViewResult<FooDocument> viewResult, String value)
     {
         int cnt = 0;
         for (ValueRow<FooDocument> row : viewResult.getRows())
         {
             if (row.getValue().getValue().equals(value))
             {
                 cnt++;
             }
         }
         return cnt;
     }
 
 
 
     
     public void thatAttachmentHandlingWorks() throws UnsupportedEncodingException
     {
         final String attachmentContent = "The quick brown fox jumps over the lazy dog.";
         
         FooDocument fooDocument = new FooDocument("foo with attachment");
         fooDocument.addAttachment("test", new Attachment("text/plain", attachmentContent.getBytes()));
 
         Database db = createDatabaseForTest();
         db.createDocument(fooDocument);
 
         String id = fooDocument.getId();
         // re-read document
         fooDocument = db.getDocument(FooDocument.class, id);
 
         Attachment attachment = fooDocument.getAttachments().get("test");
         assertThat(attachment, is(notNullValue()));
         assertThat(attachment.isStub(), is(true));
         assertThat(attachment.getContentType(), is("text/plain"));
         assertThat(attachment.getLength(), is(44l));
 
         // re-save the document to test that we can save with 'stubs'
         fooDocument.setProperty("ping", "pong");
         db.createOrUpdateDocument(fooDocument);
         
         String content = new String(db.getAttachment(id, "test"));
         assertThat(content, is(attachmentContent));
 
         String newRev = db.updateAttachment(fooDocument.getId(), fooDocument.getRevision(), "test", "text/plain", (attachmentContent+"!!").getBytes());
         assertThat(newRev, is(notNullValue()));
         assertThat(newRev.length(), is(greaterThan(0)));
 
         content = new String(db.getAttachment(id, "test"));
         assertThat(content, is(attachmentContent+"!!"));
 
         newRev = db.deleteAttachment(fooDocument.getId(), newRev, "test");
 
         assertThat(newRev, is(notNullValue()));
         assertThat(newRev.length(), is(greaterThan(0)));
 
         try
         {
             content = new String(db.getAttachment(id, "test"));
             throw new IllegalStateException("attachment should be gone by now");
         }
         catch(NotFoundException e)
         {
             // yay!
         }
 
 
         newRev = db.createAttachment(fooDocument.getId(), newRev, "test", "text/plain", "TEST".getBytes());
 
         assertThat(newRev, is(notNullValue()));
         assertThat(newRev.length(), is(greaterThan(0)));
 
         content = new String(db.getAttachment(id, "test"));
         assertThat(content, is("TEST"));
     }
 
 
     
     public void thatViewKeyQueryingFromAllDocsWorks()
     {
         Database db = createDatabaseForTest();
         ViewResult<Map> result = db.queryByKeys(Map.class, Arrays.asList(MY_FOO_DOC_ID,"second-foo-with-id"), null, null);
         assertThat(result.getRows().size(), is(2));
         assertThat(result.getRows().get(0).getId(), is(MY_FOO_DOC_ID));
         assertThat(result.getRows().get(1).getId(), is("second-foo-with-id"));
     }
 
 
     
     public void thatViewKeyQueryingFromAllDocsWorks2()
     {
         Database db = createDatabaseForTest();
         ViewResult<Map> result = db.queryByKeys(Map.class, Arrays.asList(MY_FOO_DOC_ID,"second-foo-with-id"), null, null);
         assertThat(result.getRows().size(), is(2));
         assertThat(result.getRows().get(0).getId(), is(MY_FOO_DOC_ID));
         assertThat(result.getRows().get(1).getId(), is("second-foo-with-id"));
     }
 
     
     public void thatViewKeyQueryingWorks()
     {
         Database db = createDatabaseForTest();
         ViewResult<FooDocument> result = db.queryViewByKeys("foo/byValue", FooDocument.class, Arrays.asList("doc-1","doc-2"), null, null);
 
         assertThat(result.getRows().size(), is(4));
         assertThat( valueCount(result,"doc-1"), is(2));
         assertThat( valueCount(result,"doc-2"), is(2));
 
     }
 
     
     public void thatViewAndDocumentQueryingWorks()
     {
         Database db = createDatabaseForTest();
         ViewAndDocumentsResult<Object,FooDocument> result = db.queryViewAndDocumentsByKeys("foo/byValue", Object.class, FooDocument.class, Arrays.asList("doc-1"), null, null);
         List<ValueAndDocumentRow<Object, FooDocument>> rows = result.getRows();
         assertThat(rows.size(), is(2));
 
         ValueAndDocumentRow<Object, FooDocument> row = rows.get(0);
         assertThat(row.getDocument(), is(notNullValue()));
         assertThat(row.getDocument().getValue(), is("doc-1"));
 
         row = rows.get(1);
         assertThat(row.getDocument(), is(notNullValue()));
         assertThat(row.getDocument().getValue(), is("doc-1"));
 
 
     }
 
     
     public void testPureBaseDocumentAccess()
     {
         Database db = createDatabaseForTest();
 
         BaseDocument newdoc = new BaseDocument();
         final String value = "baz403872349";
         newdoc.setProperty("foo",value); // same as JSON: { foo: "baz..." }
 
         assertThat(newdoc.getId(), is(nullValue()));
         assertThat(newdoc.getRevision(), is(nullValue()));
 
         db.createDocument(newdoc); // auto-generated id given by the database
 
         assertThat(newdoc.getId().length(), is(greaterThan(0)));
         assertThat(newdoc.getRevision().length(), is(greaterThan(0)));
 
         BaseDocument doc = db.getDocument(BaseDocument.class, newdoc.getId());
 
         assertThat((String)doc.getProperty("foo"), is(value));
 
     }
     
     
     public void testAttachmentStreaming() throws IOException
     {
         Database db = createDatabaseForTest();
         
         final String docId = "attachmentStreamingDoc";
         final String content = "Streaming test.";
         final String content2 = "Streaming test 2.";
         byte[] data = content.getBytes();
         String revision = db.createAttachment(docId, null, "test.txt", "text/plain", new ByteArrayInputStream(data), data.length);
         assertThat(revision.length(), is(greaterThan(0)));
         
         Response resp = db.getAttachmentResponse(docId, "test.txt");
         InputStream is = resp.getInputStream();
         assertThat(new String(IOUtils.toByteArray(is)), is(content));
         resp.destroy();
         
         byte[] data2 = content2.getBytes();
         revision = db.updateAttachment(docId, revision, "test.txt", "text/plain", new ByteArrayInputStream(data2), data2.length);
             
         resp = db.getAttachmentResponse(docId, "test.txt");
         is = resp.getInputStream();
         assertThat(new String(IOUtils.toByteArray(is)), is(content2));
         resp.destroy();
     }
     
     
     public void testValidation()
     {
         String fn = 
                 "function(newDoc, oldDoc, userCtx){\n" + 
         		"  if (newDoc.validationTestField && newDoc.validationTestField !== '123') {\n" + 
         		"    throw({'forbidden':'not 123'});\n" + 
         		"  }\n" + 
         		"}";
         DesignDocument designDoc = new DesignDocument("validate_test");
         designDoc.setValidateOnDocUpdate(fn);
         
         Database db = createDatabaseForTest();
         
         assertThat(designDoc.getRevision(), is(nullValue()));
         db.createDocument(designDoc);
         assertThat(designDoc.getRevision(), is(notNullValue()));
         
         BaseDocument doc = new BaseDocument();
         doc.setProperty("validationTestField", "123");
         
         assertThat(doc.getRevision(), is(nullValue()));
         db.createDocument(doc);
         assertThat(doc.getRevision(), is(notNullValue()));
         
         doc.setProperty("validationTestField", "invalid");
         
         DocumentValidationException e = null;
         try
         {
             db.updateDocument(doc);
         }
         catch(DocumentValidationException e2)
         {
             e = e2;
         }
         
         assertThat(e, is(notNullValue()));
         assertThat(e.getReason(), is("not 123"));
         assertThat(e.getError(), is("forbidden"));
     }
     
     
     @Ignore
     public void thatHandlingHugeAttachmentsWorks()
     {
         Database db = createDatabaseForTest();
         
         BaseDocument doc = new BaseDocument();
         db.createDocument(doc);
         
         long length = (long)(Runtime.getRuntime().maxMemory() * 1.1);
         InputStream is = new SizedInputStreamMock((byte)'A', length);
         db.createAttachment(doc.getId(), doc.getRevision(), "hugeAttachment.txt", "text/plain", is, length);
         
         doc = db.getDocument(BaseDocument.class, doc.getId());
         
         Map<String, Attachment> attachments = doc.getAttachments();
         assertThat(attachments.size(), is(1));
         Attachment attachment = attachments.get("hugeAttachment.txt");
         assertThat(attachment, is(notNullValue()));
         assertThat(attachment.getLength(), is(length));
         assertThat(attachment.getContentType(), is("text/plain"));
         assertThat(attachment.isStub(), is(true));
     }
 
 
     
     public void thatBulkDeletionWorks()
     {
         Database db = createDatabaseForTest();
         String[] ids = new String[] { "doc-1", "doc-2", "doc-3" };
 
         List<Document> docs = new ArrayList<Document>();
 
         for (String id : ids)
         {
             Document d = new FooDocument("value-" + id);
             d.setId(id);
             docs.add(d);
         }
 
         List<DocumentInfo> infos = db.bulkCreateDocuments(docs);
         assertThat(infos.size(), is(3));
         docs.clear();
 
         for (String docid : ids)
         {
             docs.add(db.getDocument(FooDocument.class, docid));
         }
 
         infos = db.bulkDeleteDocuments(docs);
         assertThat(infos.size(), is(3));
 
         for (String docid : ids)
         {
             try
             {
                 db.getDocument(FooDocument.class, docid);
                 assertThat("NotFoundException expected", true, is(false));
             }
             catch (NotFoundException nfe)
             {
                 // expected
             }
         }
     }
     
     public void testThatStatsWorks()
     {
         Database db = createDatabaseForTest();
         {
             Map<String,Map<String,Object>> stats = db.getServer().getStats(null);
 
             assertThat(stats.size(), is(greaterThan(1)));
             assertThat(stats.get("couchdb"), is(any(Map.class)));
         }
 
         {
             Map<String,Map<String,Object>> stats = db.getServer().getStats("/couchdb/request_time");
 
             assertThat(stats.size(), is(1));
             assertThat(stats.get("couchdb").size(), is(1));
             assertThat((Map)stats.get("couchdb").get("request_time"), is(any(Map.class)));
         }
     }
 
     
     public void thatShowsWorks()
     {
         DesignDocument doc = new DesignDocument("showDoc");
         doc.addShowFunction("foo", "function(doc,req) { return {body: '[' + doc.value + ']'}; }");
         
         Database db = createDatabaseForTest();
         db.createDocument(doc);
 
         String content = db.queryShow("showDoc/foo", MY_FOO_DOC_ID, null).getContentAsString();
 
         assertThat(content, is("[changed]"));
     }
 
     
     public void thatViewsWorks() throws FileNotFoundException, IOException
     {
         Database db = createDatabaseForTest();
         
         
         DesignDocument doc = null;
         try
         {
             doc = db.getDesignDocument("listDoc");
         }
         catch(NotFoundException e)
         {
             
         }
         
         if (doc == null)
         {
             doc = new DesignDocument("listDoc");
         }
         
         doc.addView("foos-by-value", new View(BY_VALUE_TO_NULL_FUNCTION));
                 
         doc.addListFunction("foo", "function(head, req){\n" + 
         "  var row;\n" +
         "  send('{\"head\": ' + toJSON(head) + ',\"rows\":[' );\n" +
         "  var first = true;" +
         "  while(row = getRow()) {\n" +
         "    send((first?'':',') + toJSON(row));\n" +
         "    first = false;" + 
         "  }\n" +
         "  send(']}');" + 
         "}");
         
         
         db.createOrUpdateDocument(doc);
         
         Response response = null;
         try
         {
             response = db.queryList("listDoc/foo", "foos-by-value", new Options().key("changed"));
             
             JSONParser parser = new JSONParser();
             parser.addTypeHint(".rows[]", ValueRow.class);
             
             response.setParser(parser);
             String s = response.getContentAsString();
             System.out.println(s);
             Map m = parser.parse(Map.class, s);
      
             Map head = (Map)m.get("head");
             assertThat(head, is(notNullValue()));
             assertThat((Long)head.get("total_rows"), is(10L));
             assertThat((Long)head.get("offset"), is(2L));
     
             List<ValueRow<String>> rows = (List<ValueRow<String>>) m.get("rows");
             assertThat(rows.size(), is(1));
             ValueRow<String> row = rows.get(0);
             assertThat(row.getId(),is(MY_FOO_DOC_ID));
             assertThat((String)row.getKey(),is("changed"));
         }
         finally
         {
             if (response != null)
             {
                 response.destroy();
             }
         }
 
         response = null;
         try
         {
             response = db.queryList("listDoc/foo", "foos-by-value", new Options().key("changed"));
             
             JSONParser parser = new JSONParser();
             parser.addTypeHint(".rows[]", ValueRow.class);
             
             response.setParser(parser);
             String s = response.getContentAsString();
             System.out.println(s);
             Map m = parser.parse(Map.class, s);
      
             Map head = (Map)m.get("head");
             assertThat(head, is(notNullValue()));
             assertThat((Long)head.get("total_rows"), is(10L));
             assertThat((Long)head.get("offset"), is(2L));
     
             List<ValueRow<String>> rows = (List<ValueRow<String>>) m.get("rows");
             assertThat(rows.size(), is(1));
             ValueRow<String> row = rows.get(0);
             assertThat(row.getId(),is(MY_FOO_DOC_ID));
             assertThat((String)row.getKey(),is("changed"));
         }
         finally
         {
             if (response != null)
             {
                 response.destroy();
             }
         }
         
     }
     
     public static void deleteDocIfExists(Database db, String docId)
     {
         try
         {
             BaseDocument doc = db.getDocument(BaseDocument.class, docId);
             db.delete(doc);
         }
         catch(NotFoundException e)
         {
             // ignore
         }
     }
 
     public static void assertNotExist(Database db, String docId)
     {
         try
         {
             BaseDocument doc = db.getDocument(BaseDocument.class, docId);
             Assert.fail(docId + " should not exists, but does exist");
         }
         catch(NotFoundException e)
         {
             // ignore
         }
     }
     
     
     public void thatDesignDocumentDeletionWorks()
     {
         Database db = createDatabaseForTest();
         
         DesignDocument doc = db.getDesignDocument("foo");
         assertThat(doc,is(notNullValue()));
         db.delete(doc);
         
         try
         {
             doc = db.getDesignDocument("foo");
             Assert.fail("design doc should be deleted.");
         }
         catch(NotFoundException e)
         {
             // ok
         }
     }
     
     
     public void thatFindDocumentWorks()
     {
         BaseDocument doc = new BaseDocument();
         String value = "X0Z:@S-3Poj.+q&STXgO";
         doc.setProperty("prop", value);
         
         Database db = createDatabaseForTest();
         db.createDocument(doc);
         
         
         BaseDocument doc2 = db.findDocument(BaseDocument.class, doc.getId(), null);
         assertThat((String)doc2.getProperty("prop"), is(value));
         
         BaseDocument doc3 = db.findDocument(BaseDocument.class, "noExistingId", null);
         assertThat(doc3,is(nullValue()));
     }
 
     public static Database createRandomNamedDB(String prefix)
     {
         Server server = createServer();
         
         String name = prefix + server.getUUIDs(1).get(0);
         
         server.createDatabase(name);
         
         return new Database(server, name);
     }
 
     private static ServerImpl createServer()
     {
        return new ServerImpl(LocalDatabaseTestCase.COUCHDB_HOST, LocalDatabaseTestCase.COUCHDB_PORT);
     }
 
     public static Database recreateDB(String name)
     {
         Server server = createServer();
         
         if (server.listDatabases().contains(name))
         {
             server.deleteDatabase(name);
         }
         server.createDatabase(name);
         
         return new Database(server, name);
     }
 }
