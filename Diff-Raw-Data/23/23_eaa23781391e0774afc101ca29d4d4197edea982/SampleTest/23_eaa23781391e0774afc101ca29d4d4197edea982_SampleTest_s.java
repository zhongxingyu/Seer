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
 		baseUrl = "http://localhost/amir/";
 		String jobName = System.getenv("JOB_NAME");
 		String buildNumber = System.getenv("BUILD_NUMBER");
		baseUrl += jobName + "-" + buildNumber;
		driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
 	}
 
 	@Test
 	public void testSample() throws Exception {
		driver.get(baseUrl);
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
