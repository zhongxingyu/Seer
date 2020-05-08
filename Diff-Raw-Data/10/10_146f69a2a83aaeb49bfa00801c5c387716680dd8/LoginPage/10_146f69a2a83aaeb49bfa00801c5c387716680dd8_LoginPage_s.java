 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class LoginPage {
 	
 	private WebDriver driver;
     
 	public LoginPage(WebDriver driver) {
 		this.driver = driver;
 	}
 	
 	public void logIn(String user, String password) {
		driver.findElement(By.id("uname")).sendKeys(user);
    	driver.findElement(By.name("pw")).sendKeys(password);
    	driver.findElement(By.cssSelector("input[type=\"submit\"]")).click();
 	}
 
 	public void logOut() {
		driver.findElement(By.linkText("Log Out")).click();
 	}
 	
 }
