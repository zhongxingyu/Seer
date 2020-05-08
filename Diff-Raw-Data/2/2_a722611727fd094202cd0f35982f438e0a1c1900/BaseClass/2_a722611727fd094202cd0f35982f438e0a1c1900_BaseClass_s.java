 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package runThrghTestNG;
 
 import java.io.File;
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.chrome.ChromeOptions;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.ie.InternetExplorerDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.testng.Reporter;
 import org.testng.annotations.AfterTest;
 import org.testng.annotations.BeforeTest;
 import org.testng.annotations.Listeners;
 import org.testng.annotations.Parameters;
 import smoketest.XpathValues;
 import smoketest.IsPresent;
 import smoketest.ProgramValues;
 import smoketest.Utility;
 
 @Listeners({runThrghTestNG.TestNGCustomReport.class})
 public class BaseClass {
 
     public static XpathValues xpv;
     public static ProgramValues pv;
     public static WebDriver driver;
     public IsPresent ip = new IsPresent();
     public static String program;
     public static String brwsr;
     public static String test;
 
     //The annotated method will be run before any test method belonging to the classes inside the <test> tag is run
     @BeforeTest(groups = {"prerequisite"})
     @Parameters({"program", "brwsr", "os", "test"})
     public void setUp(String program, String brwsr, String os, String test) throws Exception {
 
         this.program = program;
         this.brwsr = brwsr;
         this.test = test;
 
         pv = new ProgramValues(this.program);
         xpv = new XpathValues("xPathAccountProperty");
         System.out.println("program: " + this.program);
         System.out.println("brwsr: " + this.brwsr);
         System.out.println("os: " + os);
         File directory = new File(".");
         switch (brwsr) {
             case "chrome":
                 String chromDrvrPath;
                 chromDrvrPath = directory.getCanonicalPath() + File.separator + "lib" + File.separator;
 
                 os:
                 switch (os) {
                     case "linux32":
                     case "linux64":
                     case "mac":
                         System.out.println("initialize OS: " + os);
                         System.setProperty("webdriver.chrome.driver", chromDrvrPath + "chromedriver_" + os + File.separator + "chromedriver");
                         break os;
                     case "win":
                         System.out.println("initialize  OS: " + os);
                         System.setProperty("webdriver.chrome.driver", chromDrvrPath + "chromedriver_" + os + File.separator + "chromedriver.exe");
                         break os;
                     default:
                         Utility.illegalStateException("Invalid OS paramter, expected values 'linux32||linux64||mac||win'");
                 }
 
                 //ChromeOptions feature does not work on 'MAC' OS
                 if (os.equalsIgnoreCase("mac")) {
                     driver = new ChromeDriver();
                    ((JavascriptExecutor) driver).executeScript("window.open('','chromeBrwsr','width=1280,height=800,top=0,left‌​=0')");
                     driver.close();
                     driver.switchTo().window("chromeBrwsr");
                 } else {
                     ChromeOptions options = new ChromeOptions();
                     options.addArguments("--start-maximized");
                     driver = new ChromeDriver(options);
                 }
                 Reporter.log("Browser: " + brwsr);
                 Reporter.log("OS: " + os);
                 break;
             case "ie":
                 DesiredCapabilities caps = DesiredCapabilities.internetExplorer();
                 caps.setCapability("ignoreZoomSetting", true);
                 driver = new InternetExplorerDriver(caps);
                 ip.isTitlePresent(driver, "WebDriver");
                 Reporter.log("Browser: IE");
                 break;
             default:
                 driver = new FirefoxDriver();
                 driver.manage().window().maximize();
                 Reporter.log("Browser: firefox");
         }
         driver.get(pv.getTokenValue("programURL"));
         ip.isTitlePresent(driver, pv.getTokenValue("loginPageTitle"));
    }
 
     //The annotated method will be run after all the test methods belonging to the classes inside the <test> tag have run 
     @AfterTest(alwaysRun = true, groups = {"prerequisite"})
     public void tearDown() throws Exception {
         driver.quit();
     }
 }
