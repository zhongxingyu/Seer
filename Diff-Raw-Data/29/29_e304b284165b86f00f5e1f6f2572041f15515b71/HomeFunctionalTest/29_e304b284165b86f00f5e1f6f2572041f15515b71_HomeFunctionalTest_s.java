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
 
 public class HomeFunctionalTest {
 
    public static final int HTTP_PORT = 9191;
     public static final String HTTP_BASE_URL = "http://localhost:" + HTTP_PORT + "/twu";
     private WebDriver webDriver;
 
     @Before
     public void setUp() {
         webDriver = new FirefoxDriver();
     }
 
     @Test
     public void shouldShowTryMeLink() {
         webDriver.get(HTTP_BASE_URL);
         WebElement link = webDriver.findElement(By.tagName("a"));
 
         assertThat(link.getText(), is("Try me"));
         assertThat(link.getAttribute("href"), is(HTTP_BASE_URL + "/?username=bill"));
 
         webDriver.get(link.getAttribute("href"));
         WebElement h1 = webDriver.findElement(By.tagName("h1"));
 
         assertThat(h1.getText(), is("Hallo bill"));
     }
 
     @After
     public void tearDown() {
         webDriver.close();
     }
 
 
 }
