 /**
  * Copyright (c) 2009 Aurora Software Technology Studio. All rights reserved.
  */
 package com.corona.test.servlet;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.corona.mock.AbstractWebsiteTest;
 
 /**
  * <p>TEST PAGE: /index.pdf </p>
  *
  * @author $Author$
  * @version $Id$
  */
 public class SessionVariableHtmlTest extends AbstractWebsiteTest {
 
 	/**
 	 * @throws Exception if throws exception
 	 */
 	@Test public void index() throws Exception {
 
 		// store data to session
 		WebDriver driver = this.getWebDriver("/session/store");
 		WebElement element = driver.findElement(By.id("load"));
 		Assert.assertTrue(element.getText().startsWith("SESSION"));
 		
 		// load value from session
		element.click();
		Assert.assertTrue(driver.getPageSource().startsWith("[SESSION]"));
 	}
 }
