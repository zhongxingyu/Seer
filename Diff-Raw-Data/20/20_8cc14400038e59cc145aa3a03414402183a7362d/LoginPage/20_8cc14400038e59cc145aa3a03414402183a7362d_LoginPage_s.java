 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.Wait;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 public class LoginPage {
 	
 	private WebDriver driver;
     
 	public LoginPage(WebDriver driver) {
 		this.driver = driver;
 	}
 	
 	public void logIn(String user, String password) {
		dealingWithConcurrentDeploys();
		
 		driver.findElement(By.id("username")).sendKeys(user);
    	driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.cssSelector("#sessionLocation li")).click();
    	driver.findElement(By.id("login-button")).click();
 	}
 	
	private void dealingWithConcurrentDeploys() {
		Wait<WebDriver> wait = new WebDriverWait(driver, 600);
		wait.until(new ExpectedCondition<Boolean>() {
			@Override
			public Boolean apply(WebDriver webDriver) {
				return webDriver.findElement(By.id("username")).isDisplayed();
			}
		});
	}

 	public void logInAsAdmin() {
 		this.logIn("admin", "Admin123");
 	}
 }
