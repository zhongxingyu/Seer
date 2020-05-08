 package com.roosterpark.rptime.selenium.control.complex.navbar.link;
 
 import com.roosterpark.rptime.selenium.control.Link;
 import com.roosterpark.rptime.selenium.page.UserTimeSheetPage;
 import org.openqa.selenium.WebDriver;
 
 /**
  * User: John
  * Date: 10/24/13
  * Time: 10:34 AM
  */
 public class UserTimeSheetsLink extends Link<UserTimeSheetPage> {
 
    private static final String TIME_SHEETS_LINK = "My Time Sheets";
 
     public UserTimeSheetsLink(WebDriver driver) {
         super(driver, TIME_SHEETS_LINK);
     }
 
     @Override
     public UserTimeSheetPage click() {
         getElement().click();
         return new UserTimeSheetPage(getDriver());
     }
 }
