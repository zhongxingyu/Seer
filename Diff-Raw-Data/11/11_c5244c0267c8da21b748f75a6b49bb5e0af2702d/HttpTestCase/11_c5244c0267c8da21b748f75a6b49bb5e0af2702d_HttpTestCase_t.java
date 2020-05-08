 package me.fumiz.android.test.http;
 
 import android.test.AndroidTestCase;
 import me.fumiz.android.test.http.impl.nano.NanoTestServer;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.ResponseHandler;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.IOException;
 
 /**
  * TestCase with TestServer
  * User: fumiz
  * Date: 11/09/26
  * Time: 21:08
  */
 public class HttpTestCase extends AndroidTestCase {
     /**
      * Target TestServer instance
      */
     TestServer mServer = null;
 
     /**
      * TestServer's default port number
      */
     protected static final int sDefaultPortNumber = 9801;
 
     /**
      * create url to test server
      * @param path absolute path from documentroot
      * @return url
      */
     protected String createUrl(String path) {
         return String.format("http://127.0.0.1:%d%s", getPortNumber(), path);
     }
 
     /**
      * get TestServer's port number.
      * if you want to use another port number, you can override this method.
      * @return port number
      */
     protected int getPortNumber() {
         return sDefaultPortNumber;
     }
 
     /**
      * create TestServer's instance.
      * if you want to use another TestServer implementation, you can change here.
      * @return TestServer
      */
     protected TestServer createTestServer() {
         return new NanoTestServer(getPortNumber());
     }
 
     /**
      * set up and start TestServer.
      */
     @Override
     public void setUp() {
         mServer = createTestServer();
         mServer.start();
     }
 
     /**
      * shutdown TestServer.
      */
     @Override
     public void tearDown() {
         mServer.stop();
     }
 
     /**
      * set Request Handler from Specific TestCase.
     * @param handler request handler
      */
     protected void setRequestHandler(TestRequestHandler handler) {
         mServer.setRequestHandler(handler);
     }
 
 
     /**
      * Exception Container
      * @param <T> ExceptionType
      */
     public class ExceptionContainer<T extends Throwable> {
         private T mObj = null;
         public void set(T obj) {
             mObj = obj;
         }
         public T get() {
             return mObj;
         }
     }
 
     /**
      * execute HTTP Request and watch Server-side AssertionError.
      * @param handler Server-side request handler
      * @param httpUriRequest URIRequest
      * @param responseHandler Client-side response handler
      * @param <T> response type
      * @return response handler's returned value
      * @throws IOException occurred when http request
      * @throws AssertionError occurred when server-side assertion failed
      */
     protected <T> T execute(final TestRequestHandler handler, final org.apache.http.client.methods.HttpUriRequest httpUriRequest, final org.apache.http.client.ResponseHandler<? extends T> responseHandler) throws IOException, AssertionError {
         final ExceptionContainer<AssertionError> tContainer = new ExceptionContainer<AssertionError>();
         setRequestHandler(new TestRequestHandler() {
             @Override
             public TestResponse onRequest(TestRequest request) {
                 AssertionError thrown = null;
                 TestResponse result = null;
                 try {
                     result = handler.onRequest(request);
                 } catch (AssertionError e) {
                     thrown = e;
                 }
                 tContainer.set(thrown);
                 return result;
             }
         });
 
         HttpClient client = new DefaultHttpClient();
         T ret = client.execute(httpUriRequest, new ResponseHandler<T>() {
            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
             @Override
             public T handleResponse(HttpResponse httpResponse) throws IOException {
                 AssertionError error = tContainer.get();
                 if (error != null) {
                     return null;
                 }
                 return responseHandler.handleResponse(httpResponse);
             }
         });
         client.getConnectionManager().shutdown();
         setRequestHandler(null);
 
         AssertionError error = tContainer.get();
         if (error != null) {
             throw error;
         }
 
         return ret;
     }
 }
