 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.firefox.FirefoxProfile;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import javax.annotation.Nullable;
 import java.util.Arrays;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 @RunWith(Parameterized.class)
 public class CreateIdeaTest {
 
     private WebDriver webDriver;
     private FirefoxPreference firefoxPreference;
 
     @Parameterized.Parameters
     public static List<Object[]> firefoxPreferences() {
         return Arrays.asList(
                 new Object[][] {
                         { new FirefoxPreference("general.useragent.override", "Mozilla/5.0 (Android; Linux armv7l; rv:2.0.1) Gecko/20100101 Firefox/4.0.1 Fennec/2.0.1") },
                         { new FirefoxPreference("general.useragent.override", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_2_1 like Mac OS X; da-dk) AppleWebKit/533.17.9 (KHTML, like Gecko) Version/5.0.2 Mobile/8C148 Safari/6533.18.5") }
                 }
         );
     }
 
     public CreateIdeaTest(FirefoxPreference firefoxPreference) {
         this.firefoxPreference = firefoxPreference;
     }
 
     @Before
     public void setUp(){
         FirefoxProfile firefoxProfile = new FirefoxProfile();
         firefoxProfile.setPreference(firefoxPreference.name, firefoxPreference.value);
 
         webDriver = new FirefoxDriver(firefoxProfile);
 
     }
 
     @Test
     public void shouldDisplayErrorOnEmptyIdea() {
         navigateToCreateIdeaView();
 
         submitIdea();
 
         assertDisplayedMessageIs("Please enter some text.");
     }
 
     @Test
     public void shouldShowErrorMessageAfterFailedSubmission() throws Exception {
         navigateToCreateIdeaView();
 
         addIdeaText("Functional test idea!");
 
         submitIdea();
 
         assertDisplayedMessageIs("Failed to submit. Please try again in some time.");
     }
 
     @Test
    @Ignore("Pending until cross-domain issue is solved")
     public void shouldShowCreatedMessageAfterSubmissionOfIdea() {
         navigateToCreateIdeaView();
 
         addIdeaText("Functional test idea!");
 
         submitIdea();
 
         assertDisplayedMessageIs("Your idea has been posted.");
     }
 
     @After
     public void tearDown(){
         webDriver.close();
     }
 
     private void navigateToCreateIdeaView() {
        webDriver.get("http://localhost:9876/mib/index.html#for/mibTest/9");
         webDriver.findElement(By.className("ideaIcon")).click();
     }
 
     private void submitIdea() {
         WebElement button = webDriver.findElement(By.id("submitBtn"));
         button.click();
     }
 
     private void addIdeaText(String ideaText) {
         WebElement message = webDriver.findElement(By.id("ideaText"));
         message.sendKeys(ideaText);
     }
 
     private void waitForElementWithText(final String toCheckFor) {
         (new WebDriverWait(webDriver, 1)).until(new ExpectedCondition<Boolean>() {
             @Override
             public Boolean apply(@Nullable WebDriver input) {
                 return input.getPageSource().contains(toCheckFor);
             }
         });
     }
 
     private void assertDisplayedMessageIs(String message) {
         waitForElementWithText(message);
 
         WebElement alertArea = webDriver.findElement(By.id("alert-area"));
         assertThat(alertArea.getText(), is(message));
     }
 
     private static class FirefoxPreference {
         private String name;
         private String value;
 
         private FirefoxPreference(String name, String value) {
             this.name = name;
             this.value = value;
         }
     }
 }
