 package com.thoughtworks.twistexamples.ofbiztest;
 
 // JUnit Assert framework can be used for verification
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class Themes {
 
 	private WebDriver browser;
 
 	public Themes(WebDriver browser) {
 		this.browser = browser;

 	}
 
	public void setAdminThemeTo(String partialThemeName) throws Exception {
		if (themesAreNotDirectlyOnThePage()) {

			browser.findElements(By.linkText("PREFERENCES")).get(0).click();
			browser.findElement(By.id("theme")).click();
			browser.findElements(By.partialLinkText(partialThemeName)).get(0).click();
		} else {
			browser.findElements(By.linkText("Visual Themes")).get(0).click();
			browser.findElements(By.partialLinkText(partialThemeName)).get(0).click();
		}

	}
 
	private boolean themesAreNotDirectlyOnThePage() {
		return browser.findElements(By.linkText("Visual Themes")).isEmpty();
 	}
 
 }
