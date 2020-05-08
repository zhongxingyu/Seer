 package com.roosterpark.rptime.selenium.control.complex.timesheet;
 
 import com.roosterpark.rptime.selenium.timer.WaitForVisible;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: John
  * Date: 11/27/13
  * Time: 9:59 AM
  */
 public class ClientDropDownPopup {
 
     private Map<String, ClientDropDownLink> linkMap;
     private WebDriver driver;
     private WebElement element;
 
     public ClientDropDownPopup(WebDriver driver, WebElement element, List<ClientDropDownLink> links) {
         this.driver = driver;
         this.element = element;
         linkMap = new HashMap<>();
         linksByName(links);
     }
 
     private void linksByName(List<ClientDropDownLink> links) {
         for (ClientDropDownLink link : links) {
             linkMap.put(link.getName(), link);
         }
     }
 
     public WebDriver getDriver() {
         return driver;
     }
 
     public void view() {
         WaitForVisible waitForVisible = new WaitForVisible(element);
         waitForVisible.defaultWaitForVisible();
 
     }
 
     public void clickClientByName(String name) {
         linkMap.get(name).click();
     }
 
 }
