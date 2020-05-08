 package functional.com.thoughtworks.twu;
 
 
 import com.thoughtworks.twu.constants.URLPaths;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 import java.net.UnknownHostException;
 
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.containsString;
 
 public class DatepickerTest extends BaseTest {
     private String validPasswordString = "Th0ughtW0rks@12";
 
     public DatepickerTest() throws UnknownHostException {
         super();
     }
 
     @Before
 
     public void setup() throws UnknownHostException {
         super.setUpAndroid();
         webDriver.get(dashboardUrl);
         super.submitCredentials(validPasswordString);
     }
     @Test
     public void shouldShowErrorWhenBlank(){
         WebElement newTimesheetButton = webDriver.findElement(By.id("new_timesheet"));
         newTimesheetButton.click();
         WebElement submit = webDriver.findElement(By.id("submit"));
         submit.submit();
         WebElement message = webDriver.findElement(By.xpath("//label[@class='error']"));
         assertThat(message.getText(), is("Week ending date is required."));
     }
 
     @Test
    @Ignore("Not yet Done")
     public void shouldShowErrorWhenDuplicateDate(){
 
         WebElement newTimesheetButton = webDriver.findElement(By.id("new_timesheet"));
         newTimesheetButton.click();
         WebElement openDatepickerButton = webDriver.findElement(By.xpath("//a[@title='Open Date Picker']"));
         openDatepickerButton.click();
         WebElement sundayButton = webDriver.findElement(By.className("ui-btn-up-e"));
         sundayButton.click();
 
         String selectedWeekEndingDate = webDriver.findElement(By.id("weekEndingDate")).getAttribute("value");
 
         WebElement dateSubmitButton = webDriver.findElement(By.id("submit"));
         dateSubmitButton.click();
 
         WebElement timesheetSubmitButton = webDriver.findElement(By.id("submit"));
         timesheetSubmitButton.click();
 
         newTimesheetButton.click();
         openDatepickerButton.click();
         sundayButton.click();
         dateSubmitButton.click();
 
 
         WebElement message = webDriver.findElement(By.xpath("//label[@class='error']"));
         assertEquals(message.getText(), getExpectedErrorMessage("DuplicateTimesheetForWeek"));
     }
     @Test
     public void shouldBeReadOnly() {
         WebElement newTimesheetButton = webDriver.findElement(By.id("new_timesheet"));
         newTimesheetButton.click();
 
 
         WebElement calender = webDriver.findElement(By.id("weekEndingDate"));
         calender.sendKeys("16-Sep-12");
 
         assertThat(webDriver.findElement(By.id("weekEndingDate")).getAttribute("value"), is(""));
 
 
     }
     @Test
     @Ignore("Link to put back date on timesheet pending")
     public void shouldLinkToNewTimesheetWithValidInput() {
 
         WebElement newTimesheetButton = webDriver.findElement(By.id("new_timesheet"));
         newTimesheetButton.click();
         WebElement openDatepickerButton = webDriver.findElement(By.xpath("//a[@title='Open Date Picker']"));
         openDatepickerButton.click();
         WebElement sundayButton = webDriver.findElement(By.className("ui-btn-up-e"));
         sundayButton.click();
 
         String selectedWeekEndingDate = webDriver.findElement(By.id("weekEndingDate")).getAttribute("value");
 
         WebElement submitButton = webDriver.findElement(By.id("submit"));
         submitButton.click();
 
         WebElement weekEndingDisplay = webDriver.findElement(By.id("weekEndingDate"));
         assertEquals(selectedWeekEndingDate, weekEndingDisplay.getText());
     }
 
     @Test
     public void shouldReturnToDashboardOnCancel() {
         WebElement newTimesheetButton = webDriver.findElement(By.id("new_timesheet"));
         newTimesheetButton.click();
 
         WebElement cancelButton = webDriver.findElement(By.id("cancel"));
         cancelButton.click();
 
         assertThat(webDriver.getCurrentUrl(), containsString(URLPaths.DASHBOARD_PATH));
     }
 
 
 
 
 
 
     @After
     public void tearDown(){
         webDriver.close();
     }
 
 }
