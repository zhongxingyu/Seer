 package org.javamv.sysvar.acceptance.stepdef;
 
 import cucumber.annotation.en.Given;
 import cucumber.annotation.en.Then;
 import cucumber.annotation.en.When;
 import cucumber.runtime.PendingException;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 /**
  * Created with IntelliJ IDEA.
  * User: VMoskalenko
  * Date: 17.07.12
  * Time: 22:37
  */
 public class WebSteps {
 
     private WebDriver driver;
     private WebElement element;
 
     @Given("^I access \"([^\"]*)\" page$")
     public void I_access_page(String service) {
         driver = new HtmlUnitDriver();
         driver.get("http://localhost:8888/sysvar/" + service);
     }
 
     @When("^I enter \"([^\"]*)\" in \"([^\"]*)\" filed$")
     public void I_enter_in_filed(String value, String fieldName) {
         element = driver.findElement(By.name(fieldName));
         element.sendKeys(value);
     }
 
     @When("^click \"([^\"]*)\" button$")
     public void click_button(String name) {
         element = driver.findElement(By.name(name));
         element.submit();
     }
 
    @Then("^I can see \"([^\"]*)\" in result text$")
    public void I_can_see_in_result_text(String expectedValue) {
         element = driver.findElement(By.id("result_text"));
        assertThat(element.getText(), is(expectedValue));
     }
 
 }
