 package org.sukrupa.event;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 
 public class ErrorPage {
     private WebDriver driver;
 
     public ErrorPage(WebDriver driver) {
         this.driver = driver;
     }
 
     public boolean isValid() {
        return driver.findElement(By.id("body")).getText().equals("Entity you are looking for does not exist");
     }
 }
