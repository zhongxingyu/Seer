 package com.roosterpark.rptime.selenium.control.complex.reports.generator;
 
 import com.roosterpark.rptime.selenium.control.complex.list.worker.WorkerLink;
 import com.roosterpark.rptime.selenium.control.complex.reports.TimeSheetsSummaryReportRow;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.LinkedList;
 import java.util.List;
 
 /**
  * User: John
  * Date: 1/17/14
  * Time: 10:46 AM
  */
 public class TimeSheetsSummaryReportRowGenerator {
 
     private WebDriver driver;
     private WebElement parentElement;
 
     public TimeSheetsSummaryReportRowGenerator(WebDriver driver, WebElement parentElement) {
         this.driver = driver;
         this.parentElement = parentElement;
     }
 
     public List<TimeSheetsSummaryReportRow> generate() {
         List<TimeSheetsSummaryReportRow> rows = new LinkedList<>();
         List<WebElement> tableRows = getTableRowElements(getTableBodyFromParentElement());
         for (WebElement tableRow : tableRows) {
             WorkerLink workerLink = new WorkerLink(driver, getLinkId(tableRow));
             String worker = getLinkText(tableRow);
             Integer days = getDays(tableRow);
             Double hours = getHours(tableRow);
             rows.add(new TimeSheetsSummaryReportRow(workerLink, days, hours, worker));
         }
         return rows;
     }
 
     private WebElement getTableBodyFromParentElement() {
         return parentElement.findElement(By.xpath(".//tbody"));
     }
 
     private List<WebElement> getTableRowElements(WebElement element) {
         return element.findElements(By.xpath(".//tr"));
     }
 
     private String getLinkId(WebElement element) {
         WebElement linkElement = element.findElement(By.xpath(".//td/a"));
         return linkElement.getAttribute("id");
     }
 
     private String getLinkText(WebElement element) {
         WebElement linkElement = element.findElement(By.xpath(".//td/a"));
         return linkElement.getText().trim();
     }
 
     private Integer getDays(WebElement element) {
         List<WebElement> allTds = element.findElements(By.xpath(".//td"));
         String days = null;
         for (WebElement td : allTds) {
             if (td.getAttribute("id").contains("days-")) {
                 days = td.getText().trim();
             }
         }
         if (days != null) {
             String[] parts = days.split(" ");
             return Integer.valueOf(parts[0]);
         } else {
             return 0;
         }
     }
 
     private Double getHours(WebElement element) {
         List<WebElement> allTds = element.findElements(By.xpath(".//td"));
         String hours = null;
         for (WebElement td : allTds) {
            if (td.getAttribute("id").contains("hours-")) {
                 hours = td.getText().trim();
             }
         }
         if (hours != null) {
             String[] parts = hours.split(" ");
             return Double.valueOf(parts[0]);
         } else {
             return 0.0;
         }
     }
 
 }
