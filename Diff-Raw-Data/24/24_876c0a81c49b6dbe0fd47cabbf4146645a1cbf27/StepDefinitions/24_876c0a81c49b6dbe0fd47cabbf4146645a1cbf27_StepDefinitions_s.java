 package cukepresentation;
 
 import cucumber.annotation.After;
 import cucumber.annotation.Before;
 import cucumber.annotation.en.Then;
 import cucumber.annotation.en.When;
 import junit.framework.Assert;
 import org.apache.http.HttpResponse;
 import org.apache.http.client.HttpClient;
 import org.apache.http.client.methods.HttpGet;
 import org.apache.http.impl.client.DefaultHttpClient;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 
 /**
  * @author: Stephen Abrams
  */
 public class StepDefinitions {
 
     HttpResponse response;
     String body;
 
     HelloWorldWebServer webServer;
 
     @Before()
     public void startServer() throws IOException {
         this.webServer = new HelloWorldWebServer(8080);
         this.webServer.start();
     }
 
     @After()
     public void stopServer() {
         this.webServer.stop();
     }
 
     @When("^a request is made to \"([^\"]*)\"$")
     public void a_request_is_made_to(String arg1) throws Throwable {
         HttpClient httpclient = new DefaultHttpClient();
         String uri;
         if (arg1.startsWith("/"))
             uri = "http://localhost:8080" + arg1;
         else
             uri = arg1;
 
         HttpGet httpget = new HttpGet(uri);
         response = httpclient.execute(httpget);
     }
 
     @Then("^the response body should be \"([^\"]*)\"$")
     public void the_response_body_should_be(String expectedBody) throws Throwable {
         Assert.assertEquals(expectedBody, body());
     }
 
     /**
      * Use this method to make sure we read the body only once
      *
      * @return the response body
      */
     private String body() {
         if (this.body != null)
             return this.body;
         this.body = "";
         try {
             String line;
             InputStream is = response.getEntity().getContent();
             BufferedReader bufferedReader = new BufferedReader(
                     new InputStreamReader(is));
            while ((line = bufferedReader.readLine()) != null)
                body = body + line + System.getProperty("line.separator");
         } catch (IOException e) {
             throw new RuntimeException("Failure while attempting to read HTTP response body", e);
         }
         return this.body;
     }
 
 }
