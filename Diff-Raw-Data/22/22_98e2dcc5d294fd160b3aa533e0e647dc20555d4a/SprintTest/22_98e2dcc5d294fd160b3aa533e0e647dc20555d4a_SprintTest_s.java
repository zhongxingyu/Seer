 package io.blitz.curl;
 
 import io.blitz.curl.exception.ValidationException;
 import io.blitz.curl.sprint.ISprintListener;
 import io.blitz.curl.sprint.SprintResult;
 import io.blitz.mock.MockURLStreamHandler;
 import java.io.ByteArrayOutputStream;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLStreamHandlerFactory;
 import java.util.HashMap;
import java.util.concurrent.TimeUnit;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import static org.junit.Assert.*;
 
 /**
  *
  * @author ghermeto
  */
 public class SprintTest {
 
     private static MockURLStreamHandler handler;
     private static URLStreamHandlerFactory factory;
 
     @BeforeClass
     public static void mockSetup() {
         handler = CurlTestSuite.getHandler();
         factory = CurlTestSuite.getFactory();
         try{ URL.setURLStreamHandlerFactory(factory); } catch(Error e) {}
         handler.getConnection().setUseMapped(true);
     }
 
     @Before
     public void resetMock() {
         handler.getConnection().setHeaders(new HashMap<String, String>());
         handler.getConnection().setOutput(new ByteArrayOutputStream());
     }
     
     @Test
     public void successful() throws MalformedURLException, InterruptedException{
         //login response
         handler.getConnection().setMappedData("/login/api", 
                 "{\"ok\":true, \"api_key\":\"private-key\"}");
         
         //execute response
         handler.getConnection().setMappedData("/api/1/curl/execute", 
                 "{\"ok\":true, \"status\":\"queued\", "
                     + "\"region\":\"california\", \"job_id\":\"a123\"}");
         
         //job_status response
         handler.getConnection().setMappedData("/api/1/jobs/a123/status",
                 "{\"_id\":\"a123\",\"ok\":true, \"status\":\"completed\","
                 + "\"result\":{\"region\":\"california\",\"duration\":10,\"connect\":1,"
                 + "\"request\":{\"line\":\"GET / HTTP/1.1\",\"method\":\"GET\","
                 + "\"url\":\"http://localhost:9295\",\"headers\":{\"a\":\"b\"},\"content\":\"abc\"},"
                 + "\"response\":{\"line\":\"GET / HTTP/1.1\",\"message\":\"message\","
                 + "\"status\":200,\"headers\":{\"c\":\"d\"},\"content\":\"abd\"}}}");
         
         Sprint s = new Sprint("user", "public-key", "localhost", 9295);
         s.setUrl(new URL("http://example.com"));
         s.addListener(new ISprintListener() {
 
             public void onError(ErrorResult result) {
                 // fail if we get an error
                 assertFalse(true);
             }
 
             public void onSuccess(SprintResult result) {
                 assertNotNull(result);
                 assertNotNull(result.getRequest());
                 assertNotNull(result.getResponse());
                 assertEquals("california", result.getRegion());
                 assertEquals("GET", result.getRequest().getMethod());
                 assertEquals(new Integer(200), result.getResponse().getStatus());
                 assertNotNull(result.getRequest().getHeaders());
                 assertEquals(1, result.getRequest().getHeaders().size());
                 assertNotNull(result.getRequest().getHeaders().get("a"));
                 assertEquals("b", result.getRequest().getHeaders().get("a"));
             }
         });
         s.execute();
         assertEquals(handler.getConnection().getHeaders().get("X-API-Key"), "private-key");
         String output = handler.getConnection().getOutputStreamAsString("UTF-8");
         assertEquals(output, "{\"url\":\"http://example.com\"}");
     }
     
     @Test
     public void failedLogin() throws MalformedURLException {
         //login response
         handler.getConnection().setMappedData("/login/api", 
                 "{\"error\":\"login\", \"reason\":\"test\"}");
         
         Sprint s = new Sprint("user", "public-key", "localhost", 9295);
         s.setUrl(new URL("http://example.com"));
         s.addListener(new ISprintListener() {
 
             public void onError(ErrorResult result) {
                 assertNotNull(result);
                 assertEquals("login", result.getError());
                 assertEquals("test", result.getReason());
             }
 
             public void onSuccess(SprintResult result) {
                 // fail if we get a ok message
                 assertFalse(true);
             }
         });
         s.execute();
         assertEquals(handler.getConnection().getHeaders().get("X-API-Key"), "public-key");
     }
 
     @Test
     public void failedToQueue() throws MalformedURLException {
         //login response
         handler.getConnection().setMappedData("/login/api", 
                 "{\"ok\":true, \"api_key\":\"private-key\"}");
         
         //execute response
         handler.getConnection().setMappedData("/api/1/curl/execute", 
                 "{\"error\":\"throttle\", \"reason\":\"Slow down please!\"}");
         
         Sprint s = new Sprint("user", "public-key", "localhost", 9295);
         s.setUrl(new URL("http://example.com"));
         s.addListener(new ISprintListener() {
 
             public void onError(ErrorResult result) {
                 assertNotNull(result);
                 assertEquals("throttle", result.getError());
             }
 
             public void onSuccess(SprintResult result) {
                 // fail if we get a ok message
                 assertFalse(true);
             }
         });
         s.execute();
         assertEquals(handler.getConnection().getHeaders().get("X-API-Key"), "private-key");
         String output = handler.getConnection().getOutputStreamAsString("UTF-8");
         assertEquals(output, "{\"url\":\"http://example.com\"}");
     }
 
     @Test
     public void failedUrlValidation() {
         try {
             Sprint s = new Sprint("user", "public-key", "localhost", 9295);
             s.addListener(new ISprintListener() {
 
                 public void onError(ErrorResult result) {
                     // fail if we get an error
                     assertFalse(true);
                 }
 
                 public void onSuccess(SprintResult result) {
                     // fail if we get a ok message
                     assertFalse(true);
                 }
             });
             s.execute();
 
         } catch (ValidationException ex) {
             assertNotNull(ex);
             assertEquals("validation", ex.getError());
             assertEquals("Url is required", ex.getReason());
         }
     }
 
     @Test
     public void abort() throws MalformedURLException, InterruptedException{
         //abort response
         handler.getConnection().setMappedData("/api/1/jobs/a123/abort",
                 "{\"_id\":\"a123\",\"ok\":true}");
 
         //login response
         handler.getConnection().setMappedData("/login/api", 
                 "{\"ok\":true, \"api_key\":\"private-key\"}");
         
         //execute response
         handler.getConnection().setMappedData("/api/1/curl/execute", 
                 "{\"ok\":true, \"status\":\"queued\", "
                     + "\"region\":\"california\", \"job_id\":\"a123\"}");
         
         //job_status response
         handler.getConnection().setMappedData("/api/1/jobs/a123/status",
                 "{\"_id\":\"a123\",\"ok\":true, \"status\":\"completed\","
                 + "\"result\":{\"region\":\"california\",\"duration\":10,\"connect\":1,"
                 + "\"request\":{\"line\":\"GET / HTTP/1.1\",\"method\":\"GET\","
                 + "\"url\":\"http://localhost:9295\",\"headers\":{\"a\":\"b\"},\"content\":\"abc\"},"
                 + "\"response\":{\"line\":\"GET / HTTP/1.1\",\"message\":\"message\","
                 + "\"status\":200,\"headers\":{\"c\":\"d\"},\"content\":\"abd\"}}}");
         
         Sprint s = new Sprint("user", "public-key", "localhost", 9295);
         s.setUrl(new URL("http://example.com"));
         s.addListener(new ISprintListener() {
 
             public void onError(ErrorResult result) {
                 // fail if we get an error
                 assertFalse(true);
             }
 
             public void onSuccess(SprintResult result) {
                 assertNotNull(result);
                 assertNotNull(result.getRequest());
                 assertNotNull(result.getResponse());
                 assertEquals("california", result.getRegion());
                 assertEquals("GET", result.getRequest().getMethod());
                 assertEquals(new Integer(200), result.getResponse().getStatus());
                 assertNotNull(result.getRequest().getHeaders());
                 assertEquals(1, result.getRequest().getHeaders().size());
                 assertNotNull(result.getRequest().getHeaders().get("a"));
                 assertEquals("b", result.getRequest().getHeaders().get("a"));
             }
         });
         s.execute();
         assertEquals(handler.getConnection().getHeaders().get("X-API-Key"), "private-key");
         String output = handler.getConnection().getOutputStreamAsString("UTF-8");
         assertEquals(output, "{\"url\":\"http://example.com\"}");
 
         boolean aborted = s.abort();
         assertTrue(aborted);
     }
 }
 
