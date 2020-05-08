 package demo;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.htmlunit.HtmlUnitDriver;
 
 import static junit.framework.Assert.assertNotNull;
 import static org.junit.Assert.assertThat;
 import static org.junit.internal.matchers.StringContains.containsString;
 
 public class HomePageTest {
 
     private HtmlUnitDriver driver;
 
     @Before
     public void setUp() throws Exception {
         driver = new HtmlUnitDriver(false);
        driver.get("http://localhost:8080/kone");
     }
 
     @Test
     public void shouldShowPlayVideoButton() {
         WebElement element = driver.findElement(By.id("play_video"));
         assertNotNull(element);
     }
 
     @Test
     public void shouldRedirectFromPlayVideoButton() {
         WebElement element = driver.findElement(By.id("play_video"));
         element.click();
         assertThat(driver.getPageSource(), containsString("Request accepted"));
     }
 
     @Test
     public void shouldShowPlaySlidesButton() {
         WebElement element = driver.findElement(By.id("play_slides"));
         assertNotNull(element);
     }
 
     @Test
     public void shouldRedirectFromPlaySlidesButton() {
         WebElement element = driver.findElement(By.id("play_slides"));
         element.click();
         assertThat(driver.getPageSource(), containsString("Request accepted"));
     }
 }
