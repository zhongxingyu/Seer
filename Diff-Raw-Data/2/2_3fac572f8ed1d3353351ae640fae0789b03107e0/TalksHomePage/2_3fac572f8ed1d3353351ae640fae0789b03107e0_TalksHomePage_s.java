 package functional.com.thoughtworks.twu;
 
 import com.thoughtworks.twu.utils.CasLoginLogout;
 import com.thoughtworks.twu.utils.Talk;
 import com.thoughtworks.twu.utils.WaitForAjax;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import java.util.UUID;
 import java.util.concurrent.TimeUnit;
 
 import static com.thoughtworks.twu.utils.WaitForAjax.*;
 import static org.hamcrest.CoreMatchers.is;
 import static org.joda.time.DateTime.now;
 import static org.junit.Assert.assertThat;
 import static org.testng.Assert.assertTrue;
 
 public class TalksHomePage {
     public static final int HTTP_PORT = 9191;
     public static final String HTTP_BASE_URL = "http://localhost:" + HTTP_PORT + "/twu/";
     private WebDriver webDriver;
     private String failMessage;
     private String successMessage;
     private String errorCssValue;
 
 
     @Before
     public void setUp() {
         webDriver = new FirefoxDriver();
        webDriver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
         webDriver.get(HTTP_BASE_URL);
         failMessage = "Please Supply Valid Entries For All Fields";
         successMessage="New Talk Successfully Created";
         errorCssValue = "rgb(255, 0, 0) 0px 0px 12px 0px";
         CasLoginLogout.login(webDriver);
 
 
 
     }
 
     @Test
     public void shouldBeAbleToCreateNewTalk() throws InterruptedException {
         WebElement myTalksButton = webDriver.findElement(By.id("my_talks_button"));
         myTalksButton.click();
         WaitForAjax(webDriver);
         assertTrue(webDriver.findElement(By.id("new_talk")).isDisplayed());
         webDriver.findElement(By.id("new_talk")).click();
         assertTrue(webDriver.findElement(By.id("title")).isDisplayed());
         webDriver.findElement(By.id("title")).sendKeys(now().toString());
         //webDriver.findElement(By.id("description")).sendKeys("Seven wise men");
         webDriver.findElement(By.id("venue")).sendKeys("Ajanta Ellora");
         JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
         javascriptExecutor.executeScript("$('#datepicker').val('28/09/2012')");
         javascriptExecutor.executeScript("$('#timepicker').val('11:42 AM')");
         javascriptExecutor.executeScript("$('#new_talk_submit').click()");
         WaitForAjax(webDriver);
         WebElement text = webDriver.findElement(By.id("message_box_success"));
         assertThat(text.getText(), is(successMessage));
     }
 
     @Test
     public void shouldBeAbleToCreateNewTalkWithoutDescription() throws Exception {
         WebElement myTalksButton = webDriver.findElement(By.id("my_talks_button"));
         myTalksButton.click();
         assertTrue(webDriver.findElement(By.id("new_talk")).isDisplayed());
         webDriver.findElement(By.id("new_talk")).click();
         assertTrue(webDriver.findElement(By.id("title")).isDisplayed());
         webDriver.findElement(By.id("title")).sendKeys(now().toString());
         webDriver.findElement(By.id("venue")).sendKeys("Ajanta Ellora");
         JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
         javascriptExecutor.executeScript("$('#datepicker').val('28/09/2012')");
         javascriptExecutor.executeScript("$('#timepicker').val('11:42 AM')");
         javascriptExecutor.executeScript("$('#new_talk_submit').click()");
         WaitForAjax(webDriver);
         WebElement text = webDriver.findElement(By.id("message_box_success"));
         assertThat(text.getText(), is(successMessage));
     }
 
     @Test
     public void shouldNotBeAbleToCreateNewTalkWithoutTitle() throws Exception {
         WebElement myTalksButton = webDriver.findElement(By.id("my_talks_button"));
         myTalksButton.click();
         webDriver.findElement(By.id("new_talk")).click();
         webDriver.findElement(By.id("description")).sendKeys("Seven wise men");
         webDriver.findElement(By.id("venue")).sendKeys("Ajanta Ellora");
         JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
         javascriptExecutor.executeScript("$('#datepicker').val('28/09/2012')");
         javascriptExecutor.executeScript("$('#timepicker').val('11:42 AM')");
         javascriptExecutor.executeScript("$('#new_talk_submit').click()");
 
         assertThat(webDriver.findElement(By.id("title")).getCssValue("box-shadow"), is(errorCssValue));
     }
 
     @Test
     public void shouldNotBeAbleToCreateNewTalkWithoutVenue() throws Exception {
         WebElement myTalksButton = webDriver.findElement(By.id("my_talks_button"));
         myTalksButton.click();
         webDriver.findElement(By.id("new_talk")).click();
         webDriver.findElement(By.id("title")).sendKeys(now().toString());
         webDriver.findElement(By.id("description")).sendKeys("Seven wise men");
         JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
         javascriptExecutor.executeScript("$('#datepicker').val('28/09/2012')");
         javascriptExecutor.executeScript("$('#timepicker').val('11:42 AM')");
         javascriptExecutor.executeScript("$('#new_talk_submit').click()");
 
         assertThat(webDriver.findElement(By.id("venue")).getCssValue("box-shadow"), is(errorCssValue));
     }
 
     @Test
     public void shouldNotBeAbleToCreateNewTalkWithoutDate() throws Exception {
         WebElement myTalksButton = webDriver.findElement(By.id("my_talks_button"));
         myTalksButton.click();
         webDriver.findElement(By.id("new_talk")).click();
         webDriver.findElement(By.id("title")).sendKeys(now().toString());
         webDriver.findElement(By.id("description")).sendKeys("Seven wise men");
         webDriver.findElement(By.id("venue")).sendKeys("Ajanta Ellora");
         JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
         javascriptExecutor.executeScript("$('#timepicker').val('11:42 AM')");
         javascriptExecutor.executeScript("$('#new_talk_submit').click()");
 
         assertThat(webDriver.findElement(By.id("datepicker")).getCssValue("box-shadow"), is(errorCssValue));
     }
 
     @Test
     public void shouldNotBeAbleToCreateNewTalkWithoutTime() throws Exception {
         WebElement myTalksButton = webDriver.findElement(By.id("my_talks_button"));
         myTalksButton.click();
         webDriver.findElement(By.id("new_talk")).click();
         webDriver.findElement(By.id("title")).sendKeys(now().toString());
         webDriver.findElement(By.id("description")).sendKeys("Seven wise men");
         webDriver.findElement(By.id("venue")).sendKeys("Ajanta Ellora");
         JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
         javascriptExecutor.executeScript("$('#datepicker').val('28/09/2012')");
         javascriptExecutor.executeScript("$('#new_talk_submit').click()");
 
         assertThat(webDriver.findElement(By.id("timepicker")).getCssValue("box-shadow"), is(errorCssValue));
     }
 
 
     @After
     public void tearDown() {
         CasLoginLogout.logout(webDriver);
         webDriver.close();
     }
 
 
 }
