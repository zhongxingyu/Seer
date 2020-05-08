 package steps;
 
 import static org.junit.Assert.*;
 import java.io.IOException;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import cuke4duke.annotation.I18n.EN.Given;
 import cuke4duke.annotation.I18n.EN.Then;
 import cuke4duke.annotation.I18n.EN.When;
 
 
 public class JavaSteps 
 {
     @Given("^I am learning Cuke$")
     public void learning() {
     }
     
     @When("^I want to see docs$")
     public void seeDocumentation() {
     }
     
     @Then("^the site is up$")
     public void siteAvailable() throws MalformedURLException, IOException
     {
         URLConnection connection = new URL("http://cukes.info/").openConnection();
         connection.connect();
           HttpURLConnection httpConnection = (HttpURLConnection) connection;
         assertEquals(200, httpConnection.getResponseCode());
     }
 }
