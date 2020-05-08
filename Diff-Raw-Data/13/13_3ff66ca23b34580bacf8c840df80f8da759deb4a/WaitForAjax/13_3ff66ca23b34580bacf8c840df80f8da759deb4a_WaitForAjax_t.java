 package com.thoughtworks.twu.utils;
 
 import org.openqa.selenium.JavascriptExecutor;
 import org.openqa.selenium.WebDriver;
 
 public  class WaitForAjax {
     public static void  WaitForAjax(WebDriver webDriver) throws InterruptedException {
 
         JavascriptExecutor javascriptExecutor = (JavascriptExecutor) webDriver;
         while (true)
         {
             Boolean ajaxIsComplete =(Boolean) javascriptExecutor.executeScript("return jQuery.active == 0");
             if (ajaxIsComplete)
                 break;
 
            Thread.currentThread().sleep(500);
         }
     }
 }
