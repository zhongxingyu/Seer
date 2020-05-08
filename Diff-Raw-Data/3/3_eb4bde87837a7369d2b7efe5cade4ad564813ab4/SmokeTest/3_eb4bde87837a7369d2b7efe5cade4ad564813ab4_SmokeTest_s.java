 import org.junit.AfterClass;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import static org.junit.Assert.assertEquals;
 
 public class SmokeTest {
 
     private static WebDriver driver;
 
     @BeforeClass
     public static void before() {
         driver = new FirefoxDriver();
     }
 
     @AfterClass
     public static void after() {
         driver.close();
     }
 
     @Test
//    @Ignore // TODO: create code to complete the CRUD operations for a 'Thingy'
     public void thingy_CRUD() {
         driver.get("http://localhost:8080/YOUR_APP_NAME/thingy");
         driver.findElement(By.linkText("New Thingy")).click();
         driver.findElement(By.name("name")).sendKeys("Bob's Amazing Widget");
         driver.findElement(By.xpath("//input[@type='submit']")).click();
         assertEquals("Bob's Amazing Widget", driver.findElement(By.cssSelector("#name")).getText());
     }
 
 }
