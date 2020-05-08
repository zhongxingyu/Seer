 package example;
 
 import java.util.concurrent.TimeUnit;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.openqa.selenium.*;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 public class Juega2Wdriver4Test {
   private WebDriver driver;
   private String baseUrl;
   private StringBuffer verificationErrors = new StringBuffer();
 
   @Before
   public void setUp() throws Exception {
     driver = new FirefoxDriver();
     baseUrl = "https://www.juega.es/";
     driver.manage().timeouts().implicitlyWait(600, TimeUnit.SECONDS);
   }
 
   @Test
   public void testJuegaWebdriver4Test() throws Exception {
 	System.out.println("Starting test execution!!");
 	driver.get(baseUrl + "/");
     driver.findElement(By.linkText("Bingo")).click();
     System.out.println("Page title is: " + driver.getTitle());
     driver.findElement(By.xpath("//html/body/header/hgroup/div/div[2]/div/a")).click();
     System.out.println("Page title is: " + driver.getTitle());
     driver.findElement(By.id("first_name")).clear();
     driver.findElement(By.id("first_name")).sendKeys("petras");
     driver.findElement(By.id("last_name")).clear();
     driver.findElement(By.id("last_name")).sendKeys("petriukas");
     driver.findElement(By.id("submit-first-page")).click();
     // Warning: verifyTextPresent may require manual changes
     try {
     	assertTrue(driver.findElement(By.cssSelector("BODY")).getText().matches("^[\\s\\S]*Este campo es obligatorio[\\s\\S]*$"));
     } catch (Error e) {
       verificationErrors.append(e.toString());
     }
     System.out.println("Works perfectly!!");
   }
 
   @After
   public void tearDown() throws Exception {
     driver.quit();
     String verificationErrorString = verificationErrors.toString();
     if (!"".equals(verificationErrorString)) {
       fail(verificationErrorString);
     }
   }
 }
