 package functional.com.thoughtworks.twu;
 
import com.thoughtworks.twu.constants.URLPaths;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
 
 import java.net.UnknownHostException;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;
 
 public class DashboardTest extends BaseTest {
     private String validPasswordString = "Th0ughtW0rks@12";
 
     public DashboardTest() throws UnknownHostException {
         super();
     }
 
     @Before
     public void setup() throws Exception {
         super.setUpAndroid();
         webDriver.get(dashboardUrl);
         super.submitCredentials(validPasswordString);
     }
 
     @Test
     public void shouldJumpToDatepickerPage() throws Exception {
         webDriver.get(dashboardUrl);
         WebElement datePickerLink = webDriver.findElement(By.id("new_timesheet"));
         datePickerLink.click();
        assertThat(webDriver.getCurrentUrl(), containsString(URLPaths.DATEPICKER_PATH));
     }
 
     @Test
     public void shouldDisplayNameInHeader() throws Exception {
         webDriver.get(dashboardUrl);
         WebElement header = webDriver.findElement(By.id("header"));
         assertEquals("Welcome Tester", header.getText());
     }
 
     @After
     public void tearDown() {
         webDriver.close();
     }
 }
