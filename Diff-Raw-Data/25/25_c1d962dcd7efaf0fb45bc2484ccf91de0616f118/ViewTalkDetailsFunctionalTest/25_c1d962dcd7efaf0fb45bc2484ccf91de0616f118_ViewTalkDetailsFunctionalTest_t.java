 package functional.com.thoughtworks.twu;
 
 import functional.com.thoughtworks.twu.utils.Cas;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.testng.Assert;
 
 import java.util.UUID;
 
 import static com.thoughtworks.twu.utils.WaitHelper.waitForAjax;
 import static org.junit.Assert.assertTrue;
 
 public class ViewTalkDetailsFunctionalTest {
     public static final int HTTP_PORT = 9191;
     public static final String HTTP_BASE_URL = "http://localhost:" + HTTP_PORT + "/twu/home.html";
     private WebDriver webDriver;
 
     @Before
     public void setUp() {
         webDriver = new FirefoxDriver();
         webDriver.get(HTTP_BASE_URL);
         Cas.login(webDriver);
     }
 
     @Test
     public void shouldDisplayTalkDetailsOfCreatedTalk() throws Exception {
         assertTrue(webDriver.findElement(By.id("my_talks_button")).isDisplayed());
 
         webDriver.findElement(By.id("my_talks_button")).click();
         waitForAjax(webDriver);
         Assert.assertTrue(webDriver.findElement(By.id("new_talk")).isDisplayed());
         webDriver.findElement(By.id("new_talk")).click();
         waitForAjax(webDriver);
        String testTitle = "Title_" + UUID.randomUUID().toString();
 
         webDriver.findElement(By.id("title")).sendKeys(testTitle);
         webDriver.findElement(By.id("description")).sendKeys("test description");
         webDriver.findElement(By.id("venue")).sendKeys("test venue");
         JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
         javascriptExecutor.executeScript("$('#datepicker').val('06/09/2012')");
         javascriptExecutor.executeScript("$('#timepicker').val('10:00 AM')");
         javascriptExecutor.executeScript("$('#new_talk_submit').click()");
         waitForAjax(webDriver);
         webDriver.findElement(By.linkText(testTitle)).click();
         waitForAjax(webDriver);
         assertTrue(webDriver.getPageSource().contains(testTitle));
         assertTrue(webDriver.getPageSource().contains("test.twu"));
         assertTrue(webDriver.getPageSource().contains("test description"));
         assertTrue(webDriver.getPageSource().contains("test venue"));
         assertTrue(webDriver.getPageSource().contains("06/09/2012"));
         assertTrue(webDriver.getPageSource().contains("10:00 AM"));
         assertTrue(webDriver.getPageSource().contains("test.twu@thoughtworks.com"));
 
     }
 
     @After
     public void tearDown() {
         Cas.logout(webDriver);
         webDriver.close();
     }
 
 
 }
