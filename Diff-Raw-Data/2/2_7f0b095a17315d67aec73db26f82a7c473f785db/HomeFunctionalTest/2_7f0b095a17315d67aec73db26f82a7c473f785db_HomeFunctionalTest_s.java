 package functional.com.thoughtworks.twu;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class HomeFunctionalTest {
 
     private WebDriver webDriver;
 
     @Before
     public void setUp() {
        webDriver = new InternetExplorerDriver();
     }
 
     @Test
     public void shouldShowTryMeLink() {
         webDriver.get("http://localhost:9876/twu");
         WebElement link = webDriver.findElement(By.tagName("a"));
 
         assertThat(link.getText(), is("Try me"));
         assertThat(link.getAttribute("href"), is("http://localhost:9876/twu/?username=bill"));
 
         webDriver.get(link.getAttribute("href"));
         WebElement h1 = webDriver.findElement(By.tagName("h1"));
 
         assertThat(h1.getText(), is("Hallo bill"));
     }
 
     @After
     public void tearDown() {
         webDriver.close();
     }
 
 
 }
