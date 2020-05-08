 package functional.com.thoughtworks.twu;
 
 import com.thoughtworks.twu.utils.WaitHelper;
 import functional.com.thoughtworks.twu.utils.Cas;
 import functional.com.thoughtworks.twu.utils.Talk;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 
 public class ViewRecentTalksTest {
 
     public static final int HTTP_PORT = 9191;
     public static final String HTTP_BASE_URL = "http://localhost:" + HTTP_PORT + "/twu/home.html";
     private WebDriver webDriver;
 
     @Before
     public void setUp() {
         webDriver = new FirefoxDriver();
         webDriver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
         webDriver.get(HTTP_BASE_URL);
         Cas.login(webDriver);
     }
 
     @Test
     public void shouldDisplayPastTalkWithinTwoDays() throws InterruptedException {
         DateTime dateTime= DateTime.now().minusDays(1);
        String title1  = "title_" + UUID.randomUUID().toString();
         Talk pastTalkWithinTwoDays=new Talk(webDriver);
         pastTalkWithinTwoDays.newTalk(title1,"1 day back","here",dateTime.toString("dd/MM/YYYY"),"11:00 AM");
 
         dateTime=DateTime.now().minusDays(3);
        String title2  = "title_" + UUID.randomUUID().toString();
         Talk pastTalkNotWithinTwoDays = new Talk(webDriver);
         pastTalkNotWithinTwoDays.newTalk(title2,"3 days back","somewhere",dateTime.toString("dd/MM/YYYY"),"01:00 PM");
 
         dateTime=DateTime.now().plusDays(1);
        String title3  = "title_" + UUID.randomUUID().toString();
         Talk upcomingTalk=new Talk(webDriver);
         upcomingTalk.newTalk(title3,"1 day later","here",dateTime.toString("dd/MM/YYYY"),"03:00 PM");
 
         WebElement recentTalksButton= webDriver.findElement(By.id("talks_button"));
         recentTalksButton.click();
         WaitHelper.waitForAjax(webDriver);
         WebElement talkContainer=webDriver.findElement(By.id("talks_container"));
         String talkContainerText=talkContainer.getText();
         assertTrue(talkContainerText.contains(title1));
         assertFalse(talkContainerText.contains(title2));
         assertFalse(talkContainerText.contains(title3));
     }
 
     @After
     public void tearDown() {
         Cas.logout(webDriver);
         webDriver.close();
     }
 
 }
 
