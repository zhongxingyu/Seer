 package functional.com.thoughtworks.twu;
 
 import com.thoughtworks.twu.domain.Message;
 import com.thoughtworks.twu.service.MessageService;
 import constants.TestData;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.TimeoutException;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 import static org.hamcrest.CoreMatchers.is;
 
 import static org.junit.Assert.assertThat;
 import static org.testng.AssertJUnit.*;
 
 
 import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
 
 
 public class SearchActivityCodeTest extends BaseTest {
 
     public SearchActivityCodeTest() throws UnknownHostException {
         super();
     }
 
     @Before
     public void setup() throws UnknownHostException {
         super.setUpAndroid();
         webDriver.get(newTimesheetUrl);
         super.submitCredentials(TestData.validPasswordString);
         webDriver.get(searchActivityUrl);
         //The following two commands need to be executed once redirections are complete
 //        webDriver.findElement(By.id("timeRecord")).click();
 //        webDriver.findElement(By.id("searchActivityCode")).click();
     }
 
     @Test
     @Ignore("Redirection Present but not added as title")
     public void shouldReturnActivityCodeToActivityCodeField() {
         String selectedActivityCode = searchAndSelectActivity("YTYOO96");
         assertThat(webDriver.findElement(By.id("activity")).getText(),is(selectedActivityCode));
     }
 
     @Test
     public void shouldShowErrorForSearchStringLessThan2Characters() {
         enterSearchString("a");
         assertEquals(getExpectedErrorMessage("Alteast2CharsForSearch"),waitForVisibilityOfElementById("result").getText());
     }
 
     @Test
     public void shouldShowErrorForNoMatchFound() {
         enterSearchString("XYZZ");
         assertTrue(new WebDriverWait(webDriver, 60).until(ExpectedConditions.textToBePresentInElement(By.id("result"), expectedMessages.get("NoMatchingActivity"))));
     }
     @Test
     @Ignore("Not implemented")
     public void shouldSetBillableFlagAsNoForNonBillableActivity() {
         enterSearchString("YTYOO96");
         //Find element of list: There will be only one element
         //Element.click()
         //assertThat(webDriver.findElement(By.id("activity")).getText(),is("No"));
         //assertTrue(!webDriver.findElement(By.id("activity")).isEnabled())
     }
 
//    @After
//    public void teardown() {
//        webDriver.close();
//    }
     private void enterSearchString(String searchString){
         WebElement search = waitForVisibilityOfElementById("searchCriteria");
         search.sendKeys(searchString);
         search.submit();
     }
 
     private String searchAndSelectActivity(String searchString) {
         webDriver.get(timeRecordUrl);
         enterSearchString(searchString);
         WebElement searchResult = webDriver.findElement(By.className("ui-li-heading"));
         String selectedActivityCode = searchResult.getText();
         searchResult.click();
         return selectedActivityCode;
     }
 
 }
