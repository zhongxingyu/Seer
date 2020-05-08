 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 
 import java.net.UnknownHostException;
 
 import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
 
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
        assertNotNull(waitForVisibilityOfElementById("wecal"));
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
