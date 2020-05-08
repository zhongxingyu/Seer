 package com.gmail.at.zhuikov.aleksandr.it;
 
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 
 import org.bouncycastle.crypto.RuntimeCryptoException;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.rules.TestName;
 import org.junit.rules.TestWatcher;
 import org.junit.runner.Description;
 import org.openqa.selenium.Platform;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 
 import com.saucelabs.sauce_ondemand.driver.SauceOnDemandSelenium;
 import com.saucelabs.selenium.client.factory.SeleniumFactory;
 import com.saucelabs.selenium.client.factory.spi.SeleniumFactorySPI;
 
 public abstract class AbstractWebDriverTest {
 
 	protected WebDriver driver;
 	private final String relativeUrl;
 	@Rule public TestName name = new TestName();
 	
 	@Rule public TestWatcher watcher = new TestWatcher() {
 
 		@Override
 		protected void succeeded(Description description) {
 			if (driver instanceof SauceOnDemandSelenium) {
 				try {
 					((SauceOnDemandSelenium) driver).jobPassed();
 				} catch (IOException e1) {
 					throw new RuntimeException(e1);
 				}
 			}
 			driver.quit();
 		}
 
 		@Override
 		protected void failed(Throwable e, Description description) {
 			if (driver instanceof SauceOnDemandSelenium) {
 				try {
 					((SauceOnDemandSelenium) driver).jobFailed();
 				} catch (IOException e1) {
 					throw new RuntimeException(e1);
 				}
 			}
 			driver.quit();
 		}
 		
 	};
 	
 	public AbstractWebDriverTest(String relativeUrl) {
 		this.relativeUrl = relativeUrl;
 	}
 	
 	protected WebDriver createDriver() {
 		
 		String seleniumDriverUri = System.getenv("SELENIUM_DRIVER");
		seleniumDriverUri += "&" + getClass().getName() + "." + name.getMethodName();
 		
 		WebDriver driver = SeleniumFactory.createWebDriver(
 				System.getenv("SELENIUM_STARTING_URL"),
 				seleniumDriverUri);
 		
 		if (driver instanceof SauceOnDemandSelenium) {
 			try {
 				((SauceOnDemandSelenium) driver).setBuildNumber(System.getenv("BUILD_NUMBER"));
 			} catch (IOException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		return driver;
 //		DesiredCapabilities capabillities = DesiredCapabilities.firefox();
 //		capabillities.setCapability("version", "5");
 //		capabillities.setCapability("platform", Platform.XP);
 //		capabillities.setCapability("name", getClass().getSimpleName() + "."
 //				+ name.getMethodName());
 //		capabillities.setCapability("capture-html", true);
 //		capabillities.setCapability("build", System.getenv("BUILD_NUMBER"));
 //
 //        try {
 //			return new RemoteWebDriver(
 //			   new URL("http://cloudbees_reference:91d5e3b0-cbd6-4e23-acc9-ce14dc000565@ondemand.saucelabs.com:80/wd/hub"),
 //			   capabillities);
 //		} catch (MalformedURLException e) {
 //			throw new RuntimeException(e);
 //		}
 	}
 	
 	@Before
 	public void openPage() {
 		driver = createDriver();
 		driver.get("http://snapshot.reference.cloudbees.net/" + relativeUrl);
 	}
 	
 	@After
 	public void closePage() {
 //		driver.quit();
 	}
 	
 	protected String getUniqueCharString() {
 		String millis = String.valueOf(System.currentTimeMillis());
 	    
 	    char[] random = new char[millis.length()];
 	    for (int i = 0; i < millis.length(); i++) {
 	      random[i] = (char) (millis.charAt(i) - '0' + 'a');
 	    }
 
 		return new String(random);
 	}
 }
