 package com.riverglide.screenplay.examples.petclinic;
 
import com.gargoylesoftware.htmlunit.SilentCssErrorHandler;
import com.gargoylesoftware.htmlunit.WebClient;
 import cucumber.api.java.After;
 import cucumber.api.java.en.Then;
 import cucumber.api.java.en.When;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class ReceptionistSteps {
 
     private WebDriver driver;
 
     @When("^I get started$")
     public void I_get_started() throws Throwable {
        driver = new HtmlUnitDriver() {
            @Override
            protected WebClient modifyWebClient(WebClient client) {
                client.setCssErrorHandler(new SilentCssErrorHandler());
                return client;
            }
        };

         String petClinicHomePage = "http://localhost:8888/petclinic";
         driver.get(petClinicHomePage);
     }
 
     @Then("^I should feel welcome$")
     public void I_should_feel_welcome() throws Throwable {
         String message = driver.findElement(By.cssSelector("h2")).getText();
         assertThat(message, is("Welcome"));
     }
 
     @After
     public void closeBrowser() {
         driver.quit();
     }
 }
