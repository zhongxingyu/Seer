 package test;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.rules.TestName;
 import org.junit.runner.RunWith;
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 import test_util.DescriptivelyParameterized;
 import test_util.ParallelRunner;
 import test_util.Sauce;
 
 import com.saucelabs.saucerest.junit.ResultReportingTestWatcher;
 import com.saucelabs.saucerest.junit.SessionIdProvider;
 
 /*
  1.Parameterization
 
  https://github.com/dynacron-group/parallel-webtest/blob/master/src/main/java/com/dynacrongroup/webtest/DescriptivelyParameterized.java
  instead of default Parameterized.class which numbers tests [0], [1] and so on.
 
  2. Parallelization
 
  http://hwellmann.blogspot.com/2009/12/running-parameterized-junit-tests-in.html
  http://saucelabs.com/blog/index.php/2010/10/parallel-junit-4-and-selenium-part-three-parallelism-and-ondemand/
  https://github.com/saucelabs/parallel-test-examples/blob/master/java/junit/src/main/java/com/saucelabs/junit/Parallelized.java
 
  http://saucelabs.com/blog/index.php/2012/02/getting-started-with-web-testing-using-selenium-sauce-labs/
  https://github.com/dynacron-group/parallel-webtest
  */
 
 @RunWith(ParallelRunner.class)
 public class RunTestsOnSauce extends Sauce implements SessionIdProvider {
     private String os;
     private String browser;
     private String version;
     private static List<String[]> cap = new ArrayList<String[]>();
 
     public @Rule
     ResultReportingTestWatcher reportPassFail = new ResultReportingTestWatcher(
             this, key.user(), key.key());
     public @Rule
     TestName testName = new TestName();
 
     private String sessionIdString;
 
     private static void a(final String operatingSystem, final String browser,
             final String browserVersion) {
         cap.add(new String[] { operatingSystem, browser, browserVersion });
     }
 
     static {
         // Operating Systems
         final String win2003 = "Windows 2003"; // XP
         final String win2008 = "Windows 2008"; // Windows 7
         final String win2012 = "Windows 2012"; // Windows 8
         final String osx108 = "Mac 10.8";
         final String osx106 = "Mac 10.6";
 
         // Browsers
         final String safari = "safari";
         final String linux = "Linux";
         final String firefox = "firefox";
         final String chrome = "chrome";
         final String ie = "ie";
         final String ipad = "ipad";
         final String iphone = "iphone";
         final String android = "android";
 
         // Browser Versions
         // Sauce only supports one version of Android.
         final String androidVersion = "4";
 
         // Never specify version for Chrome.
         final String chromeVersion = "";
 
         /**
          * <pre>
          * Sauce Labs
          * 
          * caps = webdriver.DesiredCapabilities.INTERNETEXPLORER
          * caps['platform'] = 'Windows 2003'
          * caps['version'] = '8'
          * 
          * caps['platform'] = 'Windows 2008'
          * caps['version'] = '9'
          * 
          * caps['platform'] = 'Windows 2012'
          * caps['version'] = '10'
          * </pre>
          */
         a(win2003, ie, "8");
         a(win2008, ie, "9");
         a(win2012, ie, "10");
 
         /**
          * <pre>
          * caps = webdriver.DesiredCapabilities.FIREFOX
          * 
          * caps['platform'] = 'Windows 2003'
          * caps['version'] = '15'
          * 
          * caps['platform'] = 'Windows 2003'
          * caps['version'] = '16'
          * 
          * caps['platform'] = 'Windows 2003'
          * caps['version'] = '17'
          * </pre>
          */
         a(win2003, firefox, "15");
         a(win2003, firefox, "16");
         a(win2003, firefox, "17");
 
         /**
          * <pre>
          * caps = webdriver.DesiredCapabilities.FIREFOX
          * 
          * caps['platform'] = 'Windows 2008'
          * caps['version'] = '15'
          * 
          * caps['platform'] = 'Windows 2008'
          * caps['version'] = '16'
          * 
          * caps['platform'] = 'Windows 2008'
          * caps['version'] = '17'
          * </pre>
          */
         a(win2008, firefox, "15");
         a(win2008, firefox, "16");
        a(win2008, firefox, "17");
 
         /**
          * <pre>
          * caps = webdriver.DesiredCapabilities.FIREFOX
          * 
          * caps['platform'] = 'Windows 2012'
          * caps['version'] = '15'
          * 
          * caps['platform'] = 'Windows 2012'
          * caps['version'] = '16'
          * 
          * caps['platform'] = 'Windows 2012'
          * caps['version'] = '17'
          * </pre>
          */
         a(win2012, firefox, "15");
         a(win2012, firefox, "16");
         a(win2012, firefox, "17");
 
         /**
          * <pre>
          * caps = webdriver.DesiredCapabilities.CHROME
          * caps['platform'] = 'Windows 2003'
          * 
          * caps['platform'] = 'Windows 2008'
          * </pre>
          */
         a(win2003, chrome, chromeVersion);
         a(win2008, chrome, chromeVersion);
         // Sauce doesn't support Chrome on Win 2012.
 
         /**
          * <pre>
          * caps = webdriver.DesiredCapabilities.IPAD
          * caps['platform'] = 'Mac 10.8'
          * caps['version'] = '5.1'
          * 
          * caps['platform'] = 'Mac 10.8'
          * caps['version'] = '6'
          * 
          * caps['platform'] = 'Mac 10.6'
          * caps['version'] = '4.3'
          * 
          * caps['platform'] = 'Mac 10.6'
          * caps['version'] = '5'
          * </pre>
          */
         a(osx108, ipad, "5.1");
         a(osx108, ipad, "6");
         a(osx106, ipad, "4.3");
         a(osx106, ipad, "5");
 
         /**
          * <pre>
          * caps = webdriver.DesiredCapabilities.CHROME
          * caps['platform'] = 'Mac 10.8'
          * 
          * caps['platform'] = 'Mac 10.6'
          * </pre>
          */
         a(osx108, chrome, chromeVersion);
         a(osx106, chrome, chromeVersion);
 
         /**
          * <pre>
          * caps = webdriver.DesiredCapabilities.FIREFOX
          * caps['platform'] = 'Mac 10.6'
          * caps['version'] = '14
          * </pre>
          */
         a(osx106, firefox, "14");
 
         /**
          * <pre>
          * caps = webdriver.DesiredCapabilities.IPHONE
          * 
          * caps['platform'] = 'Mac 10.8'
          * caps['version'] = '5.1'
          * 
          * caps['platform'] = 'Mac 10.8'
          * caps['version'] = '6'
          * 
          * caps['platform'] = 'Mac 10.6'
          * caps['version'] = '4.3'
          * 
          * caps['platform'] = 'Mac 10.6'
          * caps['version'] = '5'
          * </pre>
          */
         a(osx108, iphone, "5.1");
         a(osx108, iphone, "6");
         a(osx106, iphone, "4.3");
         a(osx106, iphone, "5");
 
         /**
          * <pre>
          * caps = {'browserName': 'safari'}
          * caps['platform'] = 'Mac 10.8'
          * caps['version'] = '6'
          * 
          * caps['platform'] = 'Mac 10.6'
          * caps['version'] = '5'
          * </pre>
          */
         a(osx108, safari, "6");
         a(osx106, safari, "5");
 
         /**
          * <pre>
          * caps = webdriver.DesiredCapabilities.CHROME
          * caps['platform'] = 'Linux'
          * </pre>
          */
         a(linux, chrome, chromeVersion);
 
         /**
          * <pre>
          * caps = webdriver.DesiredCapabilities.ANDROID
          * caps['platform'] = 'Linux'
          * caps['version'] = '4'
          * </pre>
          */
         // Only one android version is supported currently on sauce.
         a(linux, android, androidVersion);
 
         /**
          * <pre>
          * caps = webdriver.DesiredCapabilities.FIREFOX
          * caps['platform'] = 'Linux'
          * caps['version'] = '15'
          * 
          * caps['platform'] = 'Linux'
          * caps['version'] = '16'
          * 
          * caps['platform'] = 'Linux'
          * caps['version'] = '17'
          * 
          * </pre>
          */
         a(linux, firefox, "15");
         a(linux, firefox, "16");
         a(linux, firefox, "17");
     }
 
     @DescriptivelyParameterized.Parameters
     public static List<String[]> getParameters() {
         return cap;
     }
 
     public RunTestsOnSauce(final String os, final String browser,
             final String version) {
         this.os = os;
         this.browser = browser;
         this.version = version;
     }
 
     @Before
     public void setUp() {
         setUpDriver(os, browser, version);
 
         sessionIdString = ((RemoteWebDriver) driver).getSessionId().toString();
     }
 
     @Override
     public String getSessionId() {
         return sessionIdString;
     }
 }
