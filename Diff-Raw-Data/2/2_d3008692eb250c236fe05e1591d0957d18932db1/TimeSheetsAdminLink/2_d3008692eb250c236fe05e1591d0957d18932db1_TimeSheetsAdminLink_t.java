 package com.roosterpark.rptime.selenium.control.complex.navbar.link;
 
 import com.roosterpark.rptime.selenium.control.Link;
 import com.roosterpark.rptime.selenium.page.admin.TimeSheetAdminPage;
 import com.roosterpark.rptime.selenium.timer.WaitForVisible;
 import org.openqa.selenium.WebDriver;
 
 /**
  * User: John
  * Date: 10/24/13
  * Time: 10:34 AM
  */
 public class TimeSheetsAdminLink extends Link<TimeSheetAdminPage> {
 
    private static final String TIME_SHEETS_LINK = "Time Sheets Admin";
 
     public TimeSheetsAdminLink(WebDriver driver) {
         super(driver, TIME_SHEETS_LINK);
     }
 
     @Override
     public TimeSheetAdminPage click() {
         WaitForVisible waitForVisible = new WaitForVisible(getElement());
         waitForVisible.defaultWaitForVisible();
         getElement().click();
         return new TimeSheetAdminPage(getDriver());
     }
 }
