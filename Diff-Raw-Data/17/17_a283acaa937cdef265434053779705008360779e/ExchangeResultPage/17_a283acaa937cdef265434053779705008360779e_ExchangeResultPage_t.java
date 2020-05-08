 package com.telstra.webtest.acceptance.pages;
 
 import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
 
 public class ExchangeResultPage extends BasePage {
     @Override
     protected String getPath() {
         return "/exchange";
     }
 
     public double getToAmount() {
        WebElement element = webDriver.findElement(By.cssSelector(".toAmount"));
        return Double.valueOf(element.getText());
     }
 }
