 package functional.com.thoughtworks.twu;
 
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class LoginFunctionalTest {
 
     private WebDriver webDriver;
 
     @Before
     public void setUp() {
         webDriver = new FirefoxDriver();
     }
 
     @Test
     public void shouldShowErrorWhenEmptyFields() {
         webDriver.get("https://cas.thoughtworks.com/cas/login");
         webDriver.findElement(By.name("submit")).click();
 
         WebElement errorMsgElement = webDriver.findElement(By.id("msg"));
 
         String expectedErrorMsg = "Password is a required field.\n" +
                 "Username is a required field.";
         assertThat(errorMsgElement.getText(), is(expectedErrorMsg));
     }
 
     @Test
     public void shouldShowErrorMsgWhenInvalidCredentials() {
         webDriver.get("https://cas.thoughtworks.com/cas/login");
         webDriver.findElement(By.id("username")).sendKeys("blah");
         webDriver.findElement(By.id("password")).sendKeys("blah");
         webDriver.findElement(By.name("submit")).click();
 
         WebElement errorMsgElement = webDriver.findElement(By.id("msg"));
 
         String expectedErrorMsg = "The credentials you provided cannot be determined to be authentic.";
         assertThat(errorMsgElement.getText(), is(expectedErrorMsg));
     }
 
     @Test
     public void shouldLoginSuccessfully() {
         webDriver.get("http://127.0.0.1:8080/twu/");
         String username = "test.twu";
         String password = "Th0ughtW0rks@12";
 
         webDriver.findElement(By.id("username")).sendKeys(username);
         webDriver.findElement(By.id("password")).sendKeys(password);
         webDriver.findElement(By.name("submit")).click();
 
         String expectedUrl = webDriver.getCurrentUrl();
 
         assertThat(expectedUrl.contains("http://127.0.0.1:8080/twu"), is(true));
 
     }
 
     public void shouldBeAbleToPerformSSO() {
         webDriver.get("http://castest.thoughtworks.com");
         String username = "test.twu";
         String password = "Th0ughtW0rks@12";
 
         webDriver.findElement(By.id("username")).sendKeys(username);
         webDriver.findElement(By.id("password")).sendKeys(password);
         webDriver.findElement(By.name("submit")).click();
         webDriver.get("http://127.0.0.1:8080/twu/");
 
         String expectedUrl = webDriver.getCurrentUrl();
 
         assertThat(expectedUrl.contains("http://127.0.0.1:8080/twu"), is(true));
 
         webDriver.getPageSource().contains("test.twu");
 
     }
 
     @After
     public void tearDown() {
         webDriver.close();
     }
 
 }
