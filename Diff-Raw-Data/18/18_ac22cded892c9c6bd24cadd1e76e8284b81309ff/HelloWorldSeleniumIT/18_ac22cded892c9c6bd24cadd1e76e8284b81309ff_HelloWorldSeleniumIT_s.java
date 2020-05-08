 package com.shaunabram.springmvc;
 
 import java.util.concurrent.TimeUnit;
 
 import org.junit.*;
 import static org.junit.Assert.*;
 
 import org.openqa.selenium.*;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 
 public class HelloWorldSeleniumIT {
 	private WebDriver driver;
 	private String baseUrl;
 	private StringBuffer verificationErrors = new StringBuffer();
 //    private final Log log = LogFactory.getLog(HelloWorldSeleniumIT.class);
 
 	@Before
 	public void setUp() throws Exception {
 		driver = new FirefoxDriver();//or ChromeDriver();
 		baseUrl = "http://localhost:8080/";
 		driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
 	}
 
     @Test
     public void containsInitialGreeting() throws Exception {
         driver.get(baseUrl + "SpringMVC/HelloWorld");
         assertTrue(driver.getPageSource().contains("Hello World!"));
     }
 
 	@Test
 	public void displaysCustomizedGreetingFromJavaScript() throws Exception {
 		driver.get(baseUrl + "SpringMVC/HelloWorld");
 		driver.findElement(By.id("jsTextbox")).clear();
         String recipient = "John";
 		driver.findElement(By.id("jsTextbox")).sendKeys(recipient);
        driver.findElement(By.name("jsSubmit"));
        assertTrue(driver.getPageSource().contains("Hello " + recipient + " from JavaScript!"));
 	}
 
    @Ignore
     @Test
     public void displaysCustomizedGreetingFromSpringMVC() throws Exception {
         driver.get(baseUrl + "SpringMVC/HelloWorld");
        driver.findElement(By.id("springmvcTextbox")).clear();
         String recipient = "Mary";
         driver.findElement(By.name("springmvcTextbox")).sendKeys(recipient);
        driver.findElement(By.name("springmvcSubmit"));
        String greeting = driver.findElement(By.id("jsTextbox")).getText();
        assertEquals("Hello " + recipient + " from SpringMVC! failme", greeting);
        //pageSource.contains("Hello " + recipient + " from JavaScript!");
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
