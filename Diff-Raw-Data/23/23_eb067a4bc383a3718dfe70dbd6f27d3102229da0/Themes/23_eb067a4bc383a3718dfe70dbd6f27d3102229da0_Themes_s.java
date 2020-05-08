 package com.thoughtworks.twistexamples.ofbiztest;
 
 // JUnit Assert framework can be used for verification
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class Themes {
 
 	private WebDriver browser;
 
 	public Themes(WebDriver browser) {
 		this.browser = browser;
 	}
 
	public void setAdminTheme() throws Exception {
		browser.findElements(By.linkText("PREFERENCES")).get(0).click();
		browser.findElement(By.id("theme")).click();
		browser.findElements(
				By.linkText("It's bizzness, it's bizzness time. I couldn't have said it better myself. This theme gets down"))
				.get(0).click();
 
 	}
 
 }
