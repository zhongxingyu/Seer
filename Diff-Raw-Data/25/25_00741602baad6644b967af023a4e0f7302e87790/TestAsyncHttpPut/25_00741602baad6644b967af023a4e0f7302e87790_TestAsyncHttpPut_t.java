 package com.hoccer.http;
 
 import com.hoccer.data.GenericStreamableContent;
 import com.hoccer.data.StreamableContent;
 import com.hoccer.data.StreamableString;
 import com.hoccer.tools.HttpHelper;
 import com.hoccer.tools.TestHelper;
 
 public class TestAsyncHttpPut extends HttpTestCase {
 
     @SuppressWarnings("unused")
     private static final String LOG_TAG = "TestAsyncHttpPut";
     private AsyncHttpPut        mRequest;
 
     public void testExecution() throws Exception {
 
         getServer().setResponseDelay(200);
         mRequest = new AsyncHttpPut(getServer().getUri());
         mRequest.start();
 
         TestHelper.blockUntilTrue("request should have started by now", 1000,
                 new TestHelper.Condition() {
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return mRequest.getProgress() > 0;
                     }
                 });
         assertTrue("request should have started, but progress is " + mRequest.getProgress() + "%",
                 mRequest.isRunning());
 
         TestHelper.blockUntilTrue("request should have got the response by now", 4000,
                 new TestHelper.Condition() {
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return mRequest.getProgress() > 1;
                     }
                 });
 
         blockUntilRequestIsDone(mRequest);
     }
 
     public void testPuttingStringData() throws Exception {
         String uri = getServer().getUri() + "/data";
         mRequest = new AsyncHttpPut(uri);
         mRequest.setBody("my data string");
         mRequest.start();
         blockUntilRequestIsDone(mRequest);
         assertEquals("the putted data should be returned as answer from the server",
                 "my data string", mRequest.getBodyAsString());
         assertEquals("the putted mime type should be passed to the server", "text/plain",
                 getServer().getLastRequest().header.getProperty("content-type"));
         assertEquals("the putted data should be getrievable via http GET", "my data string",
                 HttpHelper.getAsString(uri));
     }
 
     public void testPuttingStreamableContent() throws Exception {
         String uri = getServer().getUri() + "/data";
         mRequest = new AsyncHttpPut(uri);
 
         GenericStreamableContent data = new GenericStreamableContent();
         data.setContentType("text/xml");
         byte[] content = "testmango".getBytes();
 
         data.openOutputStream().write(content, 0, content.length);
 
         mRequest.setBody(data);
         mRequest.start();
         blockUntilRequestIsDone(mRequest);
         assertEquals("the putted data should be returned as answer from the server", "testmango",
                 mRequest.getBodyAsStreamableContent().toString());
 
         assertEquals("the putted mime type should be passed to the server", "text/xml", getServer()
                 .getLastRequest().header.getProperty("content-type"));
         assertEquals(
                 "the putted data should be returned with basic mime type by the mocked server",
                 "text/plain", mRequest.getBodyAsStreamableContent().getContentType());
 
         assertEquals("the putted data should be retrievable via http GET", "testmango", HttpHelper
                 .getAsString(uri));
     }
 
     public void testPuttingStringAsMultipart() throws Exception {
         String uri = getServer().getUri() + "/myMultipart";
         MultipartHttpEntity multipart = new MultipartHttpEntity();
         StreamableContent data = new StreamableString("test data string as stream");
         multipart.addPart("unit test data", data);
 
         mRequest = new AsyncHttpPut(uri);
         mRequest.setBody(multipart);
         mRequest.start();
 
         blockUntilRequestIsDone(mRequest);
         TestHelper.assertIncludes(
                 "the putted data should be somewhere in the returned as answer from the server",
                 "test data string as stream", mRequest.getBodyAsString());
 
         TestHelper.assertIncludes("the putted mime type should be passed to the server",
                 NanoHTTPD.MIME_MULTIPART, getServer().getLastRequest().header
                         .getProperty("content-type"));
 
         String mulitpartString = HttpHelper.getAsString(uri);
         assertTrue("putted data should contain mulitpart border string", mulitpartString
                 .contains(MultipartHttpEntity.BORDER));
         assertTrue("putted data should contain content-type informations", mulitpartString
                 .contains("Content-Type: text/plain"));
 
         assertMultipartDataEquals("test data string as stream", mulitpartString);
     }
 
     public void testPuttingXmlAsMultipart() throws Exception {
         String uri = getServer().getUri() + "/myMultipart";
         MultipartHttpEntity multipart = new MultipartHttpEntity();
 
         GenericStreamableContent data = new GenericStreamableContent();
         data.setContentType("text/xml");
         byte[] content = "test data string as stream".getBytes();
         data.openOutputStream().write(content, 0, content.length);
 
         multipart.addPart("unit test data", data);
 
         mRequest = new AsyncHttpPut(uri);
         mRequest.setBody(multipart);
         mRequest.start();
 
         blockUntilRequestIsDone(mRequest);
         TestHelper.assertIncludes(
                 "the putted data should be somewhere in the returned as answer from the server",
                 "test data string as stream", mRequest.getBodyAsString());
 
         String mulitpartString = HttpHelper.getAsString(uri);
         assertTrue("putted data should contain mulitpart border string", mulitpartString
                 .contains(MultipartHttpEntity.BORDER));
         assertTrue("putted data should contain content-type informations", mulitpartString
                 .contains("Content-Type: text/xml"));
 
         assertMultipartDataEquals("test data string as stream", mulitpartString);
     }
 
     private void assertMultipartDataEquals(String dataString, String pMultipartString) {
         int posOfEmptyLine = pMultipartString.indexOf("\r\n\r\n");
         assertTrue("should find the empty line in " + posOfEmptyLine, posOfEmptyLine != -1);
         int posOfMultipartEnd = pMultipartString.indexOf("\r\n--" + MultipartHttpEntity.BORDER
                 + "--");
         assertTrue("should find the multipart end in " + pMultipartString, posOfMultipartEnd != -1);
         assertEquals("the putted data should be placed inside the multipart data!", dataString,
                 pMultipartString.substring(posOfEmptyLine + 4, posOfMultipartEnd));
     }
 }
