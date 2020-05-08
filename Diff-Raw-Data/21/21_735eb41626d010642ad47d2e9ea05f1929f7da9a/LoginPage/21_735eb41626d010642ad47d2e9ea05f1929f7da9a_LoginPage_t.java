 package org.openmrs.module.mirebalais.smoke.pageobjects;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class LoginPage {
 
    	private WebDriver driver;
 
 	public LoginPage(WebDriver driver) {
 		this.driver = driver;
 	}
 
     public void logIn(String user, String password, int location) {
         driver.findElement(By.id("username")).sendKeys(user);
         driver.findElement(By.id("password")).sendKeys(password);
         driver.findElements(By.cssSelector("#sessionLocation li")).get(location).click();
         driver.findElement(By.id("login-button")).click();
     }
 
 	public void logIn(String user, String password) {
 		logIn(user, password, 0);
 	}
 
 	public void logInAsAdmin() {
 		this.logIn("admin", "Admin123");
 	}

    public void logInAsDoctor() {
        this.logIn("doctor", "Doctor123");
    }
 }
