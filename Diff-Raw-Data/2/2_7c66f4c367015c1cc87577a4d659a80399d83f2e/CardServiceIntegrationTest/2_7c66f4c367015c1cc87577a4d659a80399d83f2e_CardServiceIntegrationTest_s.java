 package kanbannow;
 
 import com.example.helloworld.HelloWorldService;
 import org.apache.http.HttpResponse;
 import org.apache.http.StatusLine;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 
 import org.junit.Test;
 
 
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.junit.Assert.assertThat;
 
 public class CardServiceIntegrationTest {
 
 
 
     @Test
     public void test() throws Exception {
 
         HelloWorldService service = new HelloWorldService();
 
         service.startEmbeddedServer("hello-world.yml");
         if (!service.isEmbeddedServerRunning()) {
             throw new Exception("Service ended immediately after starting.");
         }
 
 
         HttpClient httpclient = new DefaultHttpClient();
 
         try {
             HttpGet httpget = new HttpGet("http://localhost:9595/hello-world");
 
             System.out.println("executing request " + httpget.getURI());
 
             // Create a response handler
 //            ResponseHandler<String> responseHandler = new BasicResponseHandler();
 //            String responseBody = httpclient.execute(httpget, responseHandler);
             HttpResponse httpResponse = httpclient.execute(httpget);
 //            System.out.println("----------------------------------------");
 //            System.out.println(responseBody);
 //            System.out.println("----------------------------------------");
 
             StatusLine statusLine = httpResponse.getStatusLine();
             int statusCode = statusLine.getStatusCode();
 
 
            assertThat(statusCode, equalTo(200));
 
 
             Thread.sleep(5000);
         }
         catch (Exception e) {
             throw e;
         }
         finally {
             service.stopEmbeddedServer();
         }
     }
 
 
 
 }
