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
 	private static List<String[]> osBrowserPairs = new ArrayList<String[]>();
 
 	public @Rule
 	ResultReportingTestWatcher reportPassFail = new ResultReportingTestWatcher(
 			this, key.user(), key.key());
 	public @Rule
 	TestName testName = new TestName();
 
 	private String sessionIdString;
 
 	static {
 		// Test latest stable Firefox and Chrome on xp, vista, and linux.
 		// Note that xp = windows server 2003
 		// vista = windows server 2008
		// final String xp = "xp";
 		final String vista = "vista";
 		final String linux = "linux";
 		final String firefox = "firefox";
 		final String chrome = "chrome";
 
 		osBrowserPairs.add(new String[] { xp, firefox });
 		osBrowserPairs.add(new String[] { xp, chrome });
 
 		osBrowserPairs.add(new String[] { vista, firefox });
 		osBrowserPairs.add(new String[] { vista, chrome });
 		// Typed array support required. IE 10 + (not yet on sauce).
 		// osBrowserPairs.add(new String[] { vista, "ie" });
 
 		osBrowserPairs.add(new String[] { linux, firefox });
 		osBrowserPairs.add(new String[] { linux, chrome });
 	}
 
 	@DescriptivelyParameterized.Parameters
 	public static List<String[]> getParameters() {
 		return osBrowserPairs;
 	}
 
 	public RunTestsOnSauce(final String os, final String browser) {
 		this.os = os;
 		this.browser = browser;
 	}
 
 	@Before
 	public void setUp() {
 		setUpDriver(os, browser);
 
 		sessionIdString = ((RemoteWebDriver) driver).getSessionId().toString();
 	}
 
 	@Override
 	public String getSessionId() {
 		return sessionIdString;
 	}
 }
