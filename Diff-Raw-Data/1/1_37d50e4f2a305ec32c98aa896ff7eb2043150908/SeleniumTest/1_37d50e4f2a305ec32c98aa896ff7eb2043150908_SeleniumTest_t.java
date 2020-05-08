 package com.test.selenium;
 
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.concurrent.TimeUnit;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.remote.DesiredCapabilities;
 import org.openqa.selenium.remote.RemoteWebDriver;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Test;
 
 public class SeleniumTest {
 
 	private WebDriver browser;
 
 	@BeforeClass
 	public void setUp() throws MalformedURLException {
 		browser = new RemoteWebDriver(
 				new URL("http://192.168.56.102:80/wd/hub"),
 				DesiredCapabilities.firefox());
 	}
 
 	@Test
 	public void testOpenGoogle() {
 		browser.get("http://www.google.com");
 		new WebDriverWait(browser, 20).pollingEvery(2, TimeUnit.SECONDS).until(
 				ExpectedConditions.presenceOfElementLocated(By
 						.xpath("//*[@name='q']")));
 		WebElement inputField = browser.findElement(By.xpath("//*[@name='q']"));
 		inputField.sendKeys("test search");
 		WebElement searchButton = browser.findElement(By
 				.xpath("//*[@name='btnK']"));
 		searchButton.click();
 		new WebDriverWait(browser, 20).pollingEvery(2, TimeUnit.SECONDS).until(
 				ExpectedConditions.titleContains("test search"));
 	}
 }
