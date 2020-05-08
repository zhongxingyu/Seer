 package com.euroit.militaryshop.trolley;
 
 import static org.junit.Assert.*;
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
 import com.euroit.militaryshop.ProductCardPage;
 
 public class PutInTrolleyFromProductCardTest {
 	
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
 		ProductCardPage productCardPage = mainPage.openFirstProductCard();
		
		String urlBeforeAdd = driver.getCurrentUrl();
		
 		productCardPage.selectFirstItemOptions();		
 		
 		Wait<WebDriver> wait = new FluentWait<WebDriver>(driver)
                 .withTimeout(30, TimeUnit.SECONDS)
                 .pollingEvery(2, TimeUnit.SECONDS);
         wait.until(ExpectedConditions.textToBePresentInElement(By.id("trolley-items-count"), "1 Artikel"));
 		
		assertEquals(urlBeforeAdd, driver.getCurrentUrl());
 	}
 	
 	@AfterClass
     public static void afterAll() {
         driver.quit();
     }
 }
