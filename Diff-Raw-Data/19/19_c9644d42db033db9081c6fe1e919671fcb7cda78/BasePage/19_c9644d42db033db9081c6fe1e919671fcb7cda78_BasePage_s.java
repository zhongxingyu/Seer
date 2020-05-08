 package com.telstra.webtest.acceptance.pages;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 public abstract class BasePage {
     protected WebDriverWait wait;
     protected WebDriver webDriver;
 
     protected String getUrl() {
         return getHost() + getPath();
     }
 
     protected String getHost() {
        return "localhost:8080/WebTestDemo/";
     }
 
     protected abstract String getPath();
 
     public void initWithWebDriver(WebDriver webDriver) {
         this.webDriver = webDriver;
         this.wait = new WebDriverWait(webDriver, 60);
         org.openqa.selenium.support.PageFactory.initElements(webDriver, this);
     }
 
     protected <T extends BasePage> T asPage(Class<T> pageClass) {
         try {
             T page = pageClass.newInstance();
             page.initWithWebDriver(this.webDriver);
             return page;
         } catch (Exception e) {
             throw new RuntimeException("Failed to page " + pageClass.getName(), e);
         }
     }
 }
