 package com.euroit.militaryshop.trolley;
 
 import static org.junit.Assert.assertEquals;
 
 import java.util.concurrent.TimeUnit;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.Test;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.FluentWait;
 import org.openqa.selenium.support.ui.Wait;
 
 import com.euroit.militaryshop.Constants;
 import com.euroit.militaryshop.MainPage;
 
 public class PutInTrolleyOnMainTest {
 	
 	private final static WebDriver driver = new FirefoxDriver();
 	
 	@Before
     public void setUp() {
 		//main page is root
 		driver.get(Constants.HOST_PORT_CONTEXT);
 	}
 	
 	@Test
 	public void putInTrolleyTest() {
 		MainPage mainPage = new MainPage(driver);
 		assertEquals("leer", mainPage.getNumberOfItemsInTrolley());
 		mainPage.putFirstProductInTrolleyAndCheck();
		assertEquals(Constants.HOST_PORT_CONTEXT + "#", driver.getCurrentUrl());
 	}
 	
 	@AfterClass
     public static void afterAll() {
         driver.quit();
     }
 }
