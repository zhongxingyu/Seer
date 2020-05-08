 package functional.com.thoughtworks.twu;
 
 import constants.TestData;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 import java.net.UnknownHostException;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.testng.Assert.assertTrue;
 
 public class DashboardTest extends BaseTest {
 
     public DashboardTest() throws UnknownHostException {
         super();
     }
 
     @Before
     public void setup() throws Exception {
         super.setUpAndroid();
         webDriver.get(dashboardUrl);
         super.submitCredentials(TestData.validPasswordString);
     }
 
     @Test
     public void shouldJumpToDatepickerPage() throws Exception {
         webDriver.get(dashboardUrl);
         WebElement datePickerLink = webDriver.findElement(By.id("new_timesheet"));
         datePickerLink.click();
         assertNotNull(waitForVisibilityOfElementById("weekEndingDate"));
     }
 
     @Test
     public void shouldDisplayNameInHeader() throws Exception {
         webDriver.get(dashboardUrl);
         WebElement header = webDriver.findElement(By.id("header"));
         assertEquals("Welcome Tester", header.getText());
     }
     @Test
    @Ignore
     public void shouldSortTimesheetsInReverseChronologicalOrderByWeekendingDate() {
         WebElement newTimesheetButton = webDriver.findElement(By.id("new_timesheet"));
         newTimesheetButton.click();
        chooseParticularSundayAsWeekEndingDate(1);
         String selectedWeekEndingDate1 = webDriver.findElement(By.id("weekEndingDate")).getAttribute("value");
         WebElement dateSubmitButton = webDriver.findElement(By.id("submit"));
         dateSubmitButton.click();
         WebElement timesheetSubmitButton = webDriver.findElement(By.id("submit"));
         timesheetSubmitButton.click();
         waitForVisibilityOfElementById("new_timesheet").click();
        chooseParticularSundayAsWeekEndingDate(2);
         String selectedWeekEndingDate2 = webDriver.findElement(By.id("weekEndingDate")).getAttribute("value");
         waitForVisibilityOfElementById("submit").click();
         waitForVisibilityOfElementById("submit").click();
         assertTrue(getPositionOfParticularTimesheetGivenWeekendingDateDate(selectedWeekEndingDate2) < (getPositionOfParticularTimesheetGivenWeekendingDateDate(selectedWeekEndingDate1)));
     }
     @After
     public void tearDown() {
         webDriver.close();
     }
     private int getPositionOfParticularTimesheetGivenWeekendingDateDate(String date) {
         List<WebElement> submittedTimesheets = webDriver.findElements(By.className("ui-block-a"));
         int i;
         for (i = 0; i < submittedTimesheets.size(); i++) {
             if(submittedTimesheets.get(i).getText().substring(0,9).equals(date)) {
                 return i;
             }
         }
         return -1;
     }
 
 }
