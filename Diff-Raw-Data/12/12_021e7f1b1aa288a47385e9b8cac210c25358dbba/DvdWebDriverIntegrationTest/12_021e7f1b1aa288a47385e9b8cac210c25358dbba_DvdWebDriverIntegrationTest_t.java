 package com.henrik.dvd.web.integration;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
import org.openqa.selenium.Capabilities;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
 import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
 
 public class DvdWebDriverIntegrationTest {
 
 	protected WebDriver driver;
 	
 	@Before
 	public void setUp() 
 	{
//		System.setProperty("webdriver.chrome.driver", "/Users/Shared/Jenkins/chromedriver");
//		DesiredCapabilities capabilities = DesiredCapabilities.chrome();
//		ChromeOptions options = new ChromeOptions();
//		options.setBinary("/Applications/Google\\ Chrome.app");
//		capabilities.setCapability(ChromeOptions.CAPABILITY, options);
 		driver = new ChromeDriver();
 	}
 	
 	@Test
 	public void test() {
 		driver.get("http://localhost:8090/dvd-web/default.xhtml");
 		WebElement element = driver.findElement(By.id("test"));
 		assertTrue(element.getText().equals("Heisann"));
 	}
 	
 	@After
 	public void tearDown() {
 		driver.quit();
 	}
 
 }
