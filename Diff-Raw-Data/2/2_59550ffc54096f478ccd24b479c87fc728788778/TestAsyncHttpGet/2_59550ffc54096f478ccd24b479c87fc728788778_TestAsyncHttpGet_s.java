 package com.hoccer.http;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 
 import org.apache.http.impl.client.DefaultHttpClient;
 import org.apache.http.params.BasicHttpParams;
 
 import com.hoccer.tools.HttpHelper;
 import com.hoccer.tools.TestHelper;
 
 public class TestAsyncHttpGet extends AsyncHttpTestCase {
 
     private static final String LOG_TAG = "TestAsyncHttpGet";
     AsyncHttpGet                mRequest;
 
     private void blockUntilNormalHttpGetResponse() throws Exception {
         TestHelper.blockUntilEquals("request should respond with a text", 2000,
                 "I'm a mock server for test purposes", new TestHelper.Measurement() {
 
                     @Override
                     public Object getActualValue() throws Exception {
                         return mRequest.getBodyAsString();
                     }
                 });
         assertTrue("request should be sucessful", mRequest.wasSuccessful());
     }
 
     public void testCreating() throws Exception {
 
         mRequest = new AsyncHttpGet(getServer().getUri());
         mRequest.start();
 
         assertFalse("http get should run asyncrunous", mRequest.isTaskCompleted());
         blockUntilRequestIsDone(mRequest);
     }
 
     public void testGettingResultFromRequestObject() throws Exception {
 
         mRequest = new AsyncHttpGet(getServer().getUri());
         mRequest.start();
         blockUntilNormalHttpGetResponse();
     }
 
     public void testGettingRoundTripTime() throws Exception {
 
         mRequest = new AsyncHttpGet(getServer().getUri());
         assertEquals("RTT should be 0", 0, mRequest.getUploadTime());
 
         final ResponseHandlerForTesting requestStatus = new ResponseHandlerForTesting();
         mRequest.registerResponseHandler(requestStatus);
         mRequest.start();
         blockUntilHeadersAvailable(requestStatus);
 
         assertTrue("RTT should not be 0", mRequest.getUploadTime() > 0);
     }
 
     public void testComputingDownloadSpeed() throws Exception {
 
         mRequest = new AsyncHttpGet("http://hoccer.com/wp-content/themes/hoccer/images/logo.jpg");
         mRequest.start();
         blockUntilRequestIsDone(mRequest);
 
         long time = mRequest.getDownloadTime();
         long size = mRequest.getBodyAsStreamableContent().getStreamLength();
     }
 
     public void testGettingResultWithoutPerformingTheRequest() throws Exception {
 
         mRequest = new AsyncHttpGet(getServer().getUri());
         TestHelper.blockUntilEquals("request should give empty body before started", 1000, "",
                 new TestHelper.Measurement() {
 
                     @Override
                     public Object getActualValue() throws Exception {
                         return mRequest.getBodyAsString();
                     }
                 });
     }
 
     public void testGettingNoifiedAboutSuccessViaResponseHandler() throws Exception {
 
         getServer().setResponseDelay(100);
         mRequest = new AsyncHttpGet(getServer().getUri());
         final ResponseHandlerForTesting requestStatus = new ResponseHandlerForTesting();
         mRequest.registerResponseHandler(requestStatus);
         mRequest.start();
         Thread.sleep(50);
         assertTrue("request should have started, but is at " + mRequest.getProgress() + "%",
                 mRequest.isRunning());
 
         blockUntilHeadersAvailable(requestStatus);
 
         blockUntilRequestIsDone(mRequest);
         assertTrue("should be successful", requestStatus.wasSuccessful);
         assertNotNull("should have an response body", requestStatus.body);
         assertEquals("response should come from mocked server",
                 "I'm a mock server for test purposes", requestStatus.body.toString());
     }
 
     public void testGettingNoifiedAboutFailureViaResponseHandler() throws Exception {
 
         mRequest = new AsyncHttpGet(getServer().getUri() + "/not-a-valid-address");
         final ResponseHandlerForTesting requestStatus = new ResponseHandlerForTesting();
         mRequest.registerResponseHandler(requestStatus);
         mRequest.start();
         TestHelper.blockUntilTrue("request should have called onError", 2000,
                 new TestHelper.Condition() {
 
                     @Override
                     public boolean isSatisfied() throws Exception {
                         return requestStatus.hasError;
                     }
                 });
         assertFalse("should not be successful", requestStatus.wasSuccessful);
         assertNotNull("should have no response body", requestStatus.body);
         assertEquals("response from mocked server should describe 404", "Not Found",
                 requestStatus.body.toString());
     }
 
     public void testDefaultUserAgentStringInRequest() throws Exception {
         mRequest = new AsyncHttpGet(getServer().getUri());
         mRequest.start();
         blockUntilRequestIsDone(mRequest);
         assertEquals("User-Agent string in HTTP header", "Hoccer Java API", getServer()
                 .getLastRequest().header.getProperty("user-agent"));
     }
 
     public void testDefaultUserAgentStringInRequestWithCustomHttpClient() throws Exception {
         mRequest = new AsyncHttpGet(getServer().getUri(), new DefaultHttpClient());
         mRequest.start();
         blockUntilRequestIsDone(mRequest);
        assertEquals("User-Agent string in HTTP header", " shuld be y60", getServer()
                 .getLastRequest().header.getProperty("user-agent"));
     }
 
     public void testOwnUserAgentStringInRequestWithCustomHttpClient() throws Exception {
         DefaultHttpClient httpClient = new DefaultHttpClient();
         httpClient.getParams().setParameter("http.useragent", "Y60/0.1 HTTP Unit Test");
 
         mRequest = new AsyncHttpGet(getServer().getUri(), httpClient);
         mRequest.start();
         blockUntilRequestIsDone(mRequest);
         assertEquals("User-Agent string in HTTP header shuld be y60", "Y60/0.1 HTTP Unit Test",
                 getServer().getLastRequest().header.getProperty("user-agent"));
     }
 
     public void testCustomHttpClientWithDefaultParams() throws Exception {
         DefaultHttpClient httpClient = new DefaultHttpClient(new BasicHttpParams());
         mRequest = new AsyncHttpGet(getServer().getUri(), httpClient);
         mRequest.start();
         blockUntilNormalHttpGetResponse();
         assertEquals("User-Agent string in HTTP header", "Hoccer Java API", getServer()
                 .getLastRequest().header.getProperty("user-agent"));
     }
 
     public void testUriOfSolvableRequest() throws Exception {
         mRequest = new AsyncHttpGet(getServer().getUri());
         assertEquals("should get uri of creation", getServer().getUri(), mRequest.getUri());
         mRequest.start();
         assertEquals("should get uri of creation", getServer().getUri(), mRequest.getUri());
         blockUntilRequestIsDone(mRequest);
         assertEquals("should get uri of creation", getServer().getUri(), mRequest.getUri());
         assertTrue("request should be sucessful", mRequest.wasSuccessful());
     }
 
     public void testUriOf404Request() throws Exception {
         String uri = getServer().getUri() + "/not-existing";
         mRequest = new AsyncHttpGet(uri);
         assertEquals("should get uri of creation", uri, mRequest.getUri());
         mRequest.start();
         assertEquals("should get uri of creation", uri, mRequest.getUri());
         blockUntilRequestIsDone(mRequest);
         assertEquals("should get uri of creation", uri, mRequest.getUri());
         assertTrue("request should tell about client error", mRequest.hadClientError());
     }
 
     public void testGettingImageFromWeb() throws Exception {
         String uri = "http://artcom.de/templates/artcom/css/images/artcom_rgb_screen_193x22.png";
         byte[] expectedData = HttpHelper.getAsByteArray(uri);
         mRequest = new AsyncHttpGet(uri);
         mRequest.start();
         blockUntilRequestIsDone(mRequest);
         InputStream downloadedData = mRequest.getBodyAsStreamableContent().openInputStream();
         TestHelper.assertInputStreamEquals("Downloaded should be equal", new ByteArrayInputStream(
                 expectedData), downloadedData);
     }
 
 }
