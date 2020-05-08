 package functional.com.thoughtworks.twu;
 
 import functional.com.thoughtworks.twu.utils.Cas;
 import com.thoughtworks.twu.utils.WaitHelper;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import java.util.concurrent.TimeUnit;
 
 import static com.thoughtworks.twu.utils.WaitHelper.waitForElement;
 import static org.hamcrest.CoreMatchers.is;
 import static org.joda.time.DateTime.now;
 import static org.junit.Assert.assertThat;
 import static org.testng.Assert.assertTrue;
 
 public class TalksHomePage {
     public static final int HTTP_PORT = 9191;
     public static final String HTTP_BASE_URL = "http://localhost:" + HTTP_PORT + "/twu/home.html";
     private WebDriver webDriver;
     private static final String SUCCESS_MESSAGE = "New Talk Successfully Created";
     private static final String ERROR_CSS_VALUE = "rgb(255, 0, 0) 0px 0px 12px 0px";
 
     @Before
     public void setUp() {
         webDriver = new FirefoxDriver();
         webDriver.manage().timeouts().implicitlyWait(2, TimeUnit.SECONDS);
         webDriver.get(HTTP_BASE_URL);
         Cas.login(webDriver);
     }
 
     @Test
     public void shouldBeAbleToCreateNewTalk() throws Exception {
         WebElement myTalksButton = webDriver.findElement(By.id("my_talks_button"));
         myTalksButton.click();
         WebElement element = waitForElement(webDriver, "new_talk");
         element.click();
 
         waitForElement(webDriver, "title").sendKeys(now().toString());
         waitForElement(webDriver, "description").sendKeys("Seven wise men");
         waitForElement(webDriver, "venue").sendKeys("Ajanta Ellora");
         JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
         javascriptExecutor.executeScript("$('#datepicker').val('28/09/2012')");
         javascriptExecutor.executeScript("$('#timepicker').val('11:42 AM')");
         javascriptExecutor.executeScript("$('#new_talk_submit').click()");
         WebElement text = waitForElement(webDriver, "message_box_success");
         assertThat(text.getText(), is(SUCCESS_MESSAGE));
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
        WaitHelper.waitForAjax(webDriver);
        WebElement text = webDriver.findElement(By.id("message_box_success"));
         assertThat(text.getText(), is(SUCCESS_MESSAGE));
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
 
         assertThat(webDriver.findElement(By.id("title")).getCssValue("box-shadow"), is(ERROR_CSS_VALUE));
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
 
         assertThat(webDriver.findElement(By.id("venue")).getCssValue("box-shadow"), is(ERROR_CSS_VALUE));
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
 
         assertThat(webDriver.findElement(By.id("datepicker")).getCssValue("box-shadow"), is(ERROR_CSS_VALUE));
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
 
         assertThat(webDriver.findElement(By.id("timepicker")).getCssValue("box-shadow"), is(ERROR_CSS_VALUE));
     }
 
 
     @After
     public void tearDown() {
         Cas.logout(webDriver);
         webDriver.close();
     }
 
 
 }
