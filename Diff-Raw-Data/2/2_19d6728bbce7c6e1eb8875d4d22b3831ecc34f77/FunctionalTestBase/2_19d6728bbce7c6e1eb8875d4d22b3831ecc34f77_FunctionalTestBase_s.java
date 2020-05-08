 package com.forum.web.page.tests;
 
 
 import com.forum.web.page.Browser;
 import org.junit.After;
 import org.junit.Before;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 
 public class FunctionalTestBase {
     public Browser browser;
 
     @Before
     public void initializeWebDriver() {
        browser = new Browser("http://10.10.5.107:8080/forum", true);
         browser.open("/");
     }
 
     @After
     public void closeBrowser() {
         browser.stop();
     }
 
     protected void login() {
         WebElement login = browser.findElement(By.linkText("Login"));
         login.click();
         WebElement loginUsername = browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.name("j_username")));
         loginUsername.sendKeys("Jules");
         WebElement loginPassword = browser.findElement(By.name("j_password"));
         loginPassword.sendKeys("password");
         WebElement submit = browser.findElement(By.name("submit"));
         submit.click();
         browser.waitFor(ExpectedConditions.visibilityOfElementLocated(By.id("logout")));
     }
 }
