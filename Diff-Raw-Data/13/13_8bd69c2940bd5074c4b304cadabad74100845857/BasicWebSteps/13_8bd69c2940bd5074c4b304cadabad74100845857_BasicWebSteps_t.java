 package org.sukrupa.cucumber.steps;
 
 import cuke4duke.annotation.After;
 import cuke4duke.annotation.I18n;
 import cuke4duke.annotation.I18n.EN.Then;
 import cuke4duke.annotation.I18n.EN.When;
 import net.sf.sahi.client.Browser;
 import net.sf.sahi.client.BrowserCondition;
 import net.sf.sahi.client.ExecutionException;
 import org.sukrupa.cucumber.SahiFacade;
 

 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.sukrupa.cucumber.SahiFacade.browser;
 
 public class BasicWebSteps {
 
     private static final String TOP_LEVEL_DIV = "page";
     public static final String HOME_PAGE = "http://localhost:8080/";
 
 
 
     @When("^I enter \"([^\"]*)\" as the \"([^\"]*)\" ")
    public void enterTextIntoGenericField(String objectInput, String objectName ){

 
     }
 
     @When("^I click \"([^\"]*)\" button$")
     public void clickButton(String buttonText) {
         browser().button(buttonText).click();
     }
 
     @When("^I click \"([^\"]*)\" submit button$")
     public void clickSubmitButton(String buttonText) {
         browser().submit(buttonText).click();
     }
 
     @When("^I submit the \"([^\"]*)\" form$")
     public void submitTheForm(String buttonText) {
         browser().submit(buttonText).click();
     }
 
     @Then("^\"([^\"]*)\" should be displayed$")
     public void shouldBeDisplayed(String text) {
         assertTrue(browser().containsText(browser().div(TOP_LEVEL_DIV), text));
     }
 
     @Then("^\"([^\"]*)\" should not be displayed$")
     public void shouldNotBeDisplayed(String text) {
         assertFalse(browser().containsText(browser().div(TOP_LEVEL_DIV), text));
     }
 
 
     @When("^I click \"([^\"]*)\" link$")
     public void clickLink(String text) {
         browser().link(text).click();
     }
 
 
     @When("^I fill in the \"([^\"]*)\" text box with \"([^\"]*)\"$")
     public void fillInTheTextBoxWith(String textBox, String value) {
         browser().textbox(textBox).setValue(value);
     }
 
     @After
     public void closeBrowser() {
         SahiFacade.closeBrowser();
     }
 }
