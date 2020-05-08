 import java.util.regex.Pattern;
 import java.util.concurrent.TimeUnit;
 
 import junit.framework.TestCase;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 import static org.hamcrest.CoreMatchers.*;
 import org.openqa.selenium.*;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.ui.Select;
 
 public class SampleTest extends TestCase {
 	private WebDriver driver;
 	private String baseUrl;
 	private StringBuffer verificationErrors = new StringBuffer();
 	@Before
 	public void setUp() throws Exception {
 		driver = new FirefoxDriver();
 		buildUrl();
 		driver.manage().timeouts().implicitlyWait(3, TimeUnit.SECONDS);
 	}
 
 	private void buildUrl() {
 		baseUrl = "http://localhost/amir/";
 		String jobName = System.getenv("JOB_NAME");
 		String buildNumber = System.getenv("BUILD_NUMBER");
 		if (jobName != null) { //we are dealing with hudson so let's run this job
 			baseUrl += jobName + "-" + buildNumber;
 		} else {
 			baseUrl += "latest";
 		}
 	}
 
 	@Test
 	public void testSample() throws Exception {
 		driver.get(baseUrl + "/test.html");
         driver.findElement(By.id("testLink1")).click();
         driver.findElement(By.id("name")).clear();
         driver.findElement(By.id("name")).sendKeys("amir");
         driver.findElement(By.id("email")).clear();
         driver.findElement(By.id("email")).sendKeys("amirdt22@gmail.com");
         driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
        assertTrue(driver.findElement(By.id("successMessag")).getText().contains("congrats"));
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		driver.quit();
 		String verificationErrorString = verificationErrors.toString();
 		if (!"".equals(verificationErrorString)) {
 			fail(verificationErrorString);
 		}
 	}
 
 	private boolean isElementPresent(By by) {
 		try {
 			driver.findElement(by);
 			return true;
 		} catch (NoSuchElementException e) {
 			return false;
 		}
 	}
 }
