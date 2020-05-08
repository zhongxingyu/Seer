 package org.sukrupa.cucumber.steps;
 
 import cuke4duke.annotation.I18n.EN.*;
 import static org.sukrupa.cucumber.SahiFacade.browser;
 
 public class SendProfileViewSteps extends BasicWebSteps {
     @Given("^I am on the Profile View page for ([^\"]*)$")
     public void navigateToProfileViewPageFor(String studentName) {
         navigateToPage("View Students");
         browser().link(studentName).click();
         pageIsDisplayed("Student Record");
         browser().link("Generate Profile View").click();
         pageIsDisplayed("Profile View");
     }
 
    @When("^I enter the email \"([^\"]*)\" as To$")
     public void enterSendToEmailId(String emailId){
        enterIntoTheTextBox(emailId, "sendTo");
     }
 
     @When("^I Send Profile to sponsor$")
     public void sendProfileToSponsor() {
        browser().submit("sendProfile").click();
     }
 }
