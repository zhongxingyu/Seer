 import com.seleniumscreenflow.Grid;
 import com.seleniumscreenflow.ScreenshotFlow;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.testng.annotations.AfterTest;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Test;
 
 import java.io.File;
 import java.io.IOException;
 
 import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOfElementLocated;
 
 /**
  * Author: Sun4Android
  * Date: 09.12.12
  * Time: 15:39
  */
 public class ScreeFlowTest {
     private static final String CHROME_DRIVER_PATH = ".\\bin\\chromedriver.exe";
     private static final String OUTPUT_PATH = ".\\target\\out.png";
     private WebDriver driver;
 
     @BeforeTest
     public void beforeTest() {
         System.setProperty("webdriver.chrome.driver", CHROME_DRIVER_PATH);
         driver = new ChromeDriver();
     }
 
     @AfterTest
     public void afterTest() {
         if(driver != null) {
             driver.quit();
         }
     }
 
     @Test
     public void questionTest() {
         String question = "2 + 2";
 
         ScreenshotFlow flow = new ScreenshotFlow(driver);
         WebDriverWait wait = new WebDriverWait(driver, 10);
         driver.get("http://www.wolframalpha.com/");
 
         // Using By
         flow.takeScreenshot(By.id("calculate"), "Looking at form");
 
         // Using WebElement
         WebElement input = wait.until(visibilityOfElementLocated(By.id("i")));
         input.sendKeys(question);
         flow.takeScreenshot(input, "Looking at question");
 
 
         flow.takeScreenshot(wait.until(visibilityOfElementLocated(By.id("imath"))), "Looking at suggestions");
         WebElement equals = driver.findElement(By.id("equal"));
         equals.click();
 
         flow.takeScreenshot(wait.until(visibilityOfElementLocated(By.id("pod_0100"))), "Looking at 1 section of answers")
            .takeScreenshot(wait.until(visibilityOfElementLocated(By.id("pod_0200"))), "Looking at 2 section of answers")
            .takeScreenshot(wait.until(visibilityOfElementLocated(By.id("pod_0300"))), "Looking at 3 section of answers")
            .takeScreenshot(wait.until(visibilityOfElementLocated(By.id("pod_0400"))), "Looking at 4 section of answers")
            .takeScreenshot(wait.until(visibilityOfElementLocated(By.id("pod_0500"))), "Looking at 5 section of answers")
            .takeScreenshot(wait.until(visibilityOfElementLocated(By.id("pod_0600"))), "Looking at 6 section of answers");
 
         try {
             flow.toFile(new File(OUTPUT_PATH), new Grid(), 1920, 1080);
         } catch (IOException e) {
             //ignore
         } finally {
             flow.clear();
         }
     }
 }
