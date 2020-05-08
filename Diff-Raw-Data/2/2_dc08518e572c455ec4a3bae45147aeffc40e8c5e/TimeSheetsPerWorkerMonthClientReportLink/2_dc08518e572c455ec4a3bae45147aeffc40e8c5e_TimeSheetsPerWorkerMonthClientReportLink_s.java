 package com.roosterpark.rptime.selenium.control.complex.navbar.link;
 
 import com.roosterpark.rptime.selenium.control.Link;
 import com.roosterpark.rptime.selenium.page.admin.TimeSheetsPerWorkerMonthClientReportPage;
 import com.roosterpark.rptime.selenium.timer.WaitForVisible;
 import org.openqa.selenium.WebDriver;
 
 /**
  * User: John
  * Date: 11/20/13
  * Time: 2:00 PM
  */
 public class TimeSheetsPerWorkerMonthClientReportLink extends Link<TimeSheetsPerWorkerMonthClientReportPage> {
 
    private static final String ID = "Hours per Worker/Month, all Clients";
 
     public TimeSheetsPerWorkerMonthClientReportLink(WebDriver driver) {
         super(driver, ID);
     }
 
     @Override
     public TimeSheetsPerWorkerMonthClientReportPage click() {
         WaitForVisible waitForVisible = new WaitForVisible(getElement());
         waitForVisible.defaultWaitForVisible();
         getElement().click();
         return new TimeSheetsPerWorkerMonthClientReportPage(getDriver());
     }
 }
