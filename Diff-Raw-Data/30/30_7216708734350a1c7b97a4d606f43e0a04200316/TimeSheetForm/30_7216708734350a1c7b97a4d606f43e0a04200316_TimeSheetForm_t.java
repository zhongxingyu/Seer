 package com.roosterpark.rptime.selenium.control.complex.timesheet;
 
 import com.roosterpark.rptime.selenium.page.UserTimeSheetPage;
 import com.roosterpark.rptime.selenium.timer.WaitForVisible;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 /**
  * User: John
  * Date: 12/5/13
  * Time: 10:06 AM
  */
 public class TimeSheetForm {
 
     private WebDriver driver;
     private Map<String, TimeSheetRow> rowsByDay;
     private Map<Integer, TimeSheetRow> rowsByDayOfWeek;
 
     private static final Map<String, Integer> DAYS_OF_WEEK = new HashMap<>();
 
     static {
         DAYS_OF_WEEK.put("Sunday", 0);
         DAYS_OF_WEEK.put("Monday", 1);
         DAYS_OF_WEEK.put("Tuesday", 2);
         DAYS_OF_WEEK.put("Wednesday", 3);
         DAYS_OF_WEEK.put("Thursday", 4);
         DAYS_OF_WEEK.put("Friday", 5);
         DAYS_OF_WEEK.put("Saturday", 6);
     }
 
     public TimeSheetForm(WebDriver driver) {
         this.driver = driver;
         rowsByDay = new HashMap<>();
         rowsByDayOfWeek = new HashMap<>();
     }
 
     public void initialize() {
         WebElement createTimeSheetDiv = driver.findElement(By.id("createTimeSheet"));
         WaitForVisible waitForVisible = new WaitForVisible(createTimeSheetDiv);
         waitForVisible.defaultWaitForVisible();
        List<WebElement> elements = createTimeSheetDiv.findElements(By.xpath(".//div[@class='panel-body']/div/ul/li"));
         for (WebElement element: elements) {
             String day = getDay(element);
             if ((day.equals("Sunday") || (day.equals("Saturday")))) {
                 TimeSheetRow row = new TimeSheetRow(driver, element, true);
                 row.initialize();
                 rowsByDay.put(day, row);
                 rowsByDayOfWeek.put(DAYS_OF_WEEK.get(day), row);
             } else {
                 TimeSheetRow row = new TimeSheetRow(driver, element, false);
                 row.initialize();
                 rowsByDay.put(day, row);
                 rowsByDayOfWeek.put(DAYS_OF_WEEK.get(day), row);
             }
         }
     }
 
     public WebDriver getDriver() {
         return driver;
     }
 
     public TimeSheetRow getRowByDay(String day) {
         return rowsByDay.get(day);
     }
 
     public TimeSheetRow getRowByDayOfWeek(Integer dayOfWeek) {
         return rowsByDayOfWeek.get(dayOfWeek);
     }
 
     private String getDay(WebElement element) {
         WebElement dayElement = element.findElement(By.xpath(".//div[@class='col-sm-4 ng-binding']"));
         String baseText = dayElement.getText().trim();
         String[] parts = baseText.split(" ");
         return parts[1];
     }
 
     public void clickSaveButton() {
         SaveButton saveButton = new SaveButton(getDriver());
         saveButton.click();
     }
 
     public UserTimeSheetPage clickSubmitButton() {
         SubmitButton submitButton = new SubmitButton(getDriver());
         return submitButton.click();
     }
 
 }
