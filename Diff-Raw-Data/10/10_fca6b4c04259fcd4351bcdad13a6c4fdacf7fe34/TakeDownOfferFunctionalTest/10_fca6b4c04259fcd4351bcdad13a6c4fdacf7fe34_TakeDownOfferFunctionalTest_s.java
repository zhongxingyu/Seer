 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.ui.Select;
 
 import java.util.GregorianCalendar;
 
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.hamcrest.core.Is.is;
 import static org.junit.Assert.assertThat;
 
 public class TakeDownOfferFunctionalTest {
 
 
     private WebDriver webDriver;
     private String username;
     private String password;
 
     @Before
     public void setUp() {
         webDriver = new FirefoxDriver();
         username = "test.twu";
         password = "Th0ughtW0rks@12";
     }
 
     @Test
     public void shouldGoToHomeAfterHideAnOffer() throws Exception {
 
         logIn();
 
         Thread.sleep(1000);
         webDriver.findElement(By.id("createOffer")).click();
 
         String offerTitle = "TITLE_"+ GregorianCalendar.getInstance().getTime().getTime();
         webDriver.findElement(By.name("title")).sendKeys(offerTitle);
 
         Select select = new Select(webDriver.findElement(By.tagName("select")));
         select.selectByValue("Cars");
 
         webDriver.findElement(By.name("description")).sendKeys("To pass the test or not, this is a question");
         webDriver.findElement(By.name("submit")).click();
         Thread.sleep(1000);
 
 
 
         webDriver.findElement(By.id("takeDownButton")).click();
 
         String pageTitle = webDriver.getTitle();
 
        String expectedPageTitle = "Carousel Template · Bootstrap";
 
         assertThat(pageTitle, is(expectedPageTitle));
 
     }
 
     @After
     public void tearDown() throws Exception {
         webDriver.close();
     }
 
     private void logIn() {
         webDriver.get("http://127.0.0.1:8080/twu/");
 
         webDriver.findElement(By.id("username")).sendKeys(username);
         webDriver.findElement(By.id("password")).sendKeys(password);
         webDriver.findElement(By.name("submit")).click();
     }
 
 }
