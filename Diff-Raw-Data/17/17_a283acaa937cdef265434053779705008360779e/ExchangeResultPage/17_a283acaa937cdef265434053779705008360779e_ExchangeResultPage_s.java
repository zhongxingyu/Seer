 package com.telstra.webtest.acceptance.pages;
 
 import org.openqa.selenium.By;
 
 public class ExchangeResultPage extends BasePage {
     @Override
     protected String getPath() {
         return "/exchange";
     }
 
     public double getToAmount() {
        return Double.valueOf(webDriver.findElement(By.cssSelector("form.exchange input[name='toAmount']")).getValue());
     }
 }
