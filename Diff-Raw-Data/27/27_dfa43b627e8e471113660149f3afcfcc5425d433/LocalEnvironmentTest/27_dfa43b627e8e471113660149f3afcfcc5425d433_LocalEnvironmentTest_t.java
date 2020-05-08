 package br.com.dextra.expertus.environment;
 
 import junit.framework.Assert;
 
 import org.apache.commons.lang3.StringUtils;
 import org.junit.Test;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import br.com.dextra.expertus.EnvironmentFactory;
 
 public class LocalEnvironmentTest {
 
 	private boolean runTests = false;
 
 	public LocalEnvironmentTest() {
 		runTests = StringUtils.isNotEmpty(System.getProperty("expertus.environment.local.test"));
 		System.setProperty(EnvironmentFactory.ENVIRONMENT_TYPE_PROPERTY, EnvironmentType.local.toString());
 	}
 
 	@Test
 	public void testCreateFirefoxBrowser() {
 		if (runTests) {
 			System.setProperty(Environment.ENVIRONMENT_BROWSER_PROPERTY, LocalEnvironmentBrowser.FIREFOX.toString());
 			WebDriver firefoxDriver = EnvironmentFactory.createEnvironment().createDriver();
 			Assert.assertTrue(firefoxDriver instanceof FirefoxDriver);
 			firefoxDriver.quit();
 		}
 	}
 
 }
