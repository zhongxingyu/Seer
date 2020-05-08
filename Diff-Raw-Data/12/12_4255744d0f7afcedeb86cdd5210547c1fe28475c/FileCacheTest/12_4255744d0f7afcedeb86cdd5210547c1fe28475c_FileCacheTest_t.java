 /*******************************************************************************
  * Copyright (C) 2009, 2010, Hoccer GmbH Berlin, Germany <www.hoccer.com>
  * 
  * These coded instructions, statements, and computer programs contain
  * proprietary information of Hoccer GmbH Berlin, and are copy protected
  * by law. They may be used, modified and redistributed under the terms
  * of GNU General Public License referenced below. 
  *    
  * Alternative licensing without the obligations of the GPL is
  * available upon request.
  * 
  * GPL v3 Licensing:
  * 
  * This file is part of the "Linccer Java-API".
  * 
  * Linccer Java-API is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  * 
  * Linccer Java-API is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with Linccer Java-API. If not, see <http://www.gnu.org/licenses/>.
  ******************************************************************************/
 package com.hoccer.api;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 import java.util.HashMap;
 import java.util.UUID;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import com.hoccer.data.GenericStreamableContent;
 import com.hoccer.data.StreamableContent;
 import com.hoccer.data.StreamableString;
 import com.hoccer.http.AsyncHttpPut;
 import com.hoccer.http.AsyncHttpRequest;
 import com.hoccer.http.ResponseHandlerForTesting;
 import com.hoccer.tools.HttpClientException;
 import com.hoccer.tools.HttpHelper;
 import com.hoccer.tools.TestHelper;
 
 public class FileCacheTest {
 
     // Constants ---------------------------------------------------------
 
    private static final String FILE_CACHE_URL_PREFIX = "https://filecache-testing.hoccer.com/v3/";
 
     @Before
     public void setUp() {
 
         ClientConfig.useTestingServers();
     }
 
     @Test
     public void storeTextInFileCache() throws Exception {
 
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test"));
 
         String uri = filecache.store(new StreamableString("hello world"), 100);
         assertThat(uri, containsString(FILE_CACHE_URL_PREFIX));
 
         StreamableContent data = new GenericStreamableContent();
         filecache.fetch(uri, data);
 
         assertThat(data.toString(), is(equalTo("hello world")));
         assertThat(data.getContentType(), is(equalTo("text/plain")));
     }
 
     @Test
     public void storeBinaryDataInFileCache() throws Exception {
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test"));
 
         GenericStreamableContent content = new GenericStreamableContent();
         content.setFilename("data.png");
         content.setContentType("image/png");
         byte[] data = { 23, 42, 23 };
         content.openNewOutputStream().write(data);
 
         String locationUri = filecache.store(content, 2);
         assertThat(locationUri, containsString(FILE_CACHE_URL_PREFIX));
     }
 
     @Test
     public void plainAsyncStoringOfText() throws Exception {
 
         String uri = ClientConfig.getFileCacheBaseUri() + "/" + UUID.randomUUID()
                 + "?expires_in=10000";
 
         AsyncHttpPut storeRequest = new AsyncHttpPut(ApiSigningTools.sign(uri,
                 ApiKeySigningTest.demoKey, ApiKeySigningTest.demoSecret));
         storeRequest.setBody(new StreamableString("hello world"));
         storeRequest.start();
 
         blockUntilRequestIsDone(storeRequest);
 
         assertThat(storeRequest.getStatusCode(), is(equalTo(201)));
         assertThat(storeRequest.getBodyAsString(),
                 containsString(uri.substring(0, uri.indexOf('?'))));
     }
 
     protected void blockUntilRequestIsDone(final AsyncHttpRequest pRequest) throws Exception {
         TestHelper.blockUntilTrue("request should have been performed by now", 6000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return pRequest.isTaskCompleted();
                     }
                 });
     }
 
     @Test
     public void asyncStoringOfTextViaFileCache() throws Exception {
 
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test"));
         final ResponseHandlerForTesting handler = new ResponseHandlerForTesting();
         String uri = filecache.asyncStore(new StreamableString("hello world 12 11"), 10, handler);
         blockUntilRequestWasSuccsessful(handler);
 
         assertThat(handler.body.toString(), containsString(uri));
         assertThat(HttpHelper.getAsString(uri), is(equalTo("hello world 12 11")));
     }
 
     private void blockUntilRequestWasSuccsessful(final ResponseHandlerForTesting handler)
             throws Exception {
         TestHelper.blockUntilTrue("request should have been successful by now", 3000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return handler.wasSuccessful;
                     }
                 });
     }
 
     @Test
     public void asyncFetchingOfTextViaFileCache() throws Exception {
 
         ClientConfig config = new ClientConfig("File Cache Unit Test");
         FileCache filecache = new FileCache(config);
         final String uri = filecache.store(new StreamableString("hello file cache"), 10);
 
         final GenericStreamableContent sink = new GenericStreamableContent();
         final ResponseHandlerForTesting handler = new ResponseHandlerForTesting() {
             @Override
             public void onHeaderAvailable(HashMap<String, String> headers) {
                 AsyncHttpRequest.setTypeAndFilename(sink, headers, uri);
             }
         };
         filecache.asyncFetch(uri, sink, handler);
 
         TestHelper.blockUntilTrue("request should have been successful by now", 3000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return handler.wasSuccessful;
                     }
                 });
         assertThat(uri, containsString(FILE_CACHE_URL_PREFIX));
         assertThat(sink.getContentType(), is(equalTo("text/plain")));
         assertThat(handler.body.toString(), is(equalTo("hello file cache")));
     }
 
     @Test
     public void storingLargerAmountOfData() throws Exception {
 
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test"));
         final ResponseHandlerForTesting handler = new ResponseHandlerForTesting();
 
         GenericStreamableContent data = getLargeDataObject(1000000);
 
         String uri = filecache.asyncStore(data, 10, handler);
 
         TestHelper.blockUntilTrue("request should have been successful by now", 6000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return handler.wasSuccessful;
                     }
                 });
 
         assertThat(handler.body.toString(), containsString(uri));
         assertThat(HttpHelper.getAsString(uri), is(equalTo(data.toString())));
     }
 
     // @Test // TODO server currently doesn't do this
     public void abortStoringIfNotAuthentificated() throws Exception {
 
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test",
                 "invalid-api-key", "invalid-secret"));
         final ResponseHandlerForTesting handler = new ResponseHandlerForTesting();
 
         GenericStreamableContent data = getLargeDataObject(1000000);
 
         String uri = filecache.asyncStore(data, 10, handler);
 
         TestHelper.blockUntilTrue("request should have been aborted by now", 6000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return handler.hasError;
                     }
                 });
 
         assertThat(handler.statusCode, is(equalTo(401)));
 
         boolean has404 = false;
         try {
             HttpHelper.getAsString(uri);
         } catch (HttpClientException e) {
             if (e.getStatusCode() == 404)
                 has404 = true;
         }
         assertTrue("should have got 404", has404);
 
         assertThat(handler.body.toString(), is(equalTo("missing api key")));
     }
 
     // @Test // TODO server currently doesn't do this
     public void simultaneousStoreAndFetch() throws Exception {
 
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test"));
         final ResponseHandlerForTesting storeHandler = new ResponseHandlerForTesting();
         final ResponseHandlerForTesting fetchHandler = new ResponseHandlerForTesting();
 
         GenericStreamableContent source = getLargeDataObject(1000000);
         String uri = filecache.asyncStore(source, 10, storeHandler);
         Thread.sleep(400);
 
         GenericStreamableContent sink = new GenericStreamableContent();
         filecache.asyncFetch(uri, sink, fetchHandler);
 
         TestHelper.blockUntilTrue("request should have been successful by now", 20000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return fetchHandler.receiveProgress > 10;
                     }
                 });
 
         assertTrue("should not have fully received the data", fetchHandler.receiveProgress < 70);
         assertTrue("should have started uploading", storeHandler.sendProgress > 10);
         assertTrue("should not have finished uploading", storeHandler.sendProgress < 95);
 
         TestHelper.blockUntilTrue("request should have been successful by now", 10000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return fetchHandler.wasSuccessful;
                     }
                 });
 
         assertThat(sink.openNewOutputStream().toString(), is(equalTo(source.openNewOutputStream()
                 .toString())));
         assertThat(sink.getContentType(), is(equalTo("text/plain")));
     }
 
     @Test
     public void cachelStoringWhileUploading() throws Exception {
 
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test",
                 "invalid-api-key", "invalid-secret"));
         final ResponseHandlerForTesting handler = new ResponseHandlerForTesting();
 
         GenericStreamableContent data = getLargeDataObject(100000);
         String uri = filecache.asyncStore(data, 10, handler);
         Thread.sleep(200);
         filecache.cancel(uri);
 
         TestHelper.blockUntilTrue("request should have been aborted by now", 3000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return handler.hasError;
                     }
                 });
 
         filecache.cancel(uri);
     }
 
     private GenericStreamableContent getLargeDataObject(int size) throws Exception {
         GenericStreamableContent data = new GenericStreamableContent();
         data.setContentType("text/plain");
         StringBuffer content = new StringBuffer();
         for (int i = 0; i < size; i++) {
             content.append('a');
         }
 
         data.openNewOutputStream().write(content.toString().getBytes(), 0,
                 content.toString().getBytes().length);
         return data;
     }
 
     @Test
     public void ongoingRequestHandling() throws Exception {
 
         FileCache filecache = new FileCache(new ClientConfig("File Cache Unit Test"));
         assertThat(filecache.getOngoingRequests().size(), is(equalTo(0)));
 
         final ResponseHandlerForTesting handler = new ResponseHandlerForTesting();
         String uri = filecache.asyncStore(getLargeDataObject(10000), 10, handler);
         assertThat(filecache.getOngoingRequests().size(), is(equalTo(1)));
 
         blockUntilRequestWasSuccsessful(handler);
 
         assertThat(filecache.getOngoingRequests().size(), is(equalTo(0)));
     }
 }
