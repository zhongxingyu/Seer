 package com.telstra.webtest.acceptance.pages;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 
 public class Navigator {
     private static WebDriver webDriver = new ChromeDriver();
 
     public ExchangePage toExchangePage() {
         return (ExchangePage) visitAndInit(ExchangePage.class);
     }
 
     private <T extends BasePage> BasePage visitAndInit(Class<T> pageClass) {
         try {
             T t = pageClass.newInstance();
             webDriver.get(t.getUrl());
             t.initWithWebDriver(webDriver);
             return t;
         } catch (Exception e) {
             throw new RuntimeException("Failed to page " + pageClass.getName(), e);
         }
     }
 }
