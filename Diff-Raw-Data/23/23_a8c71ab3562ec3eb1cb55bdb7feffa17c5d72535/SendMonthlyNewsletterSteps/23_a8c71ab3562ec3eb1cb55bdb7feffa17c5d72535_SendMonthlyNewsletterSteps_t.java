 package org.sukrupa.cucumber.steps;
 
 import cuke4duke.annotation.After;
 import cuke4duke.annotation.I18n.EN.*;
 import org.apache.james.mime4j.storage.TempFileStorageProvider;
 
 import java.io.File;
 import java.io.IOException;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.sukrupa.cucumber.SahiFacade.browser;
 
 public class SendMonthlyNewsletterSteps {
     @When("^I send the mail")
     public void sendMail() {
         browser().submit("send").click();
     }
 
     @When("^I attach a file$")
     public void attachFile() throws IOException {
         File tempFile = new File("testFile.txt");
         if (!tempFile.exists())
             tempFile.createNewFile();
        browser().setFile(browser().byId("attach"), tempFile.getCanonicalPath());
     }
 
     @After
     public void cleanUp() {
         File tempFile = new File("testFile.txt");
         if (tempFile.exists())
             tempFile.delete();
     }
 
 
     @Then("^the file entry is cleared$")
     public void theFileEntryIsCleared() {
         assertThat(browser().byId("fakebox").value(), is(""));
     }
 }
