 package fi.helsinki.cs.okkopa.model;
 
 import fi.helsinki.cs.okkopa.shared.Settings;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class BatchDetails {
 
     private int successfulTikliSaves;
     private int totalPages;
     private int failedScans;
     private String reportEmailAddress;
     private String emailContent;
     private Settings settings;
 
     @Autowired
     public BatchDetails(Settings settings) {
         this.settings = settings;
         reset();
     }
     
     private BatchDetails() {
         
     }
     //Courseinfo:
 
     public String getCourseCode() {
         return courseCode;
     }
 
     public void setCourseCode(String courseCode) {
         this.courseCode = courseCode;
     }
 
     public String getPeriod() {
         return period;
     }
 
     public void setPeriod(String period) {
         this.period = period;
     }
 
     public int getYear() {
         return year;
     }
 
     public void setYear(int year) {
         this.year = year;
     }
 
     public String getType() {
         return type;
     }
 
     public void setType(String type) {
         this.type = type;
     }
 
     public int getCourseNumber() {
         return courseNumber;
     }
 
     public void setCourseNumber(int courseNumber) {
         this.courseNumber = courseNumber;
     }
     private String courseCode;
     private String period;
     private int year;
     private String type;
     private int courseNumber;
 
     public int getTotalPages() {
         return totalPages;
     }
 
     public void setTotalPages(int totalPages) {
         this.totalPages = totalPages;
     }
 
     public int getFailedScans() {
         return failedScans;
     }
 
     public void setFailedScans(int failedScans) {
         this.failedScans = failedScans;
     }
 
     public void addFailedScan() {
         this.failedScans++;
     }
 
     public String getReportEmailAddress() {
         return reportEmailAddress;
     }
 
     public void setReportEmailAddress(String reportEmailAddress) {
         this.reportEmailAddress = reportEmailAddress;
     }
 
     public String getEmailContent() {
         return emailContent;
     }
 
     public void appendEmailContent(String emailContent) {
         this.emailContent= this.emailContent+"\n"+emailContent;
     }
 
     public int getSuccessfulTikliSaves() {
         return successfulTikliSaves;
     }
 
     public void setSuccessfulTikliSaves(int successfulTikliSaves) {
         this.successfulTikliSaves = successfulTikliSaves;
     }
     
     public void addSuccessfulTikliSave() {
         this.successfulTikliSaves++;
     }
 
     public void reset() {
         this.emailContent = settings.getProperty("mail.message.defaultbody");
         this.reportEmailAddress = null;
         this.totalPages = 0;
         this.failedScans = 0;
         this.courseCode = null;
         this.courseNumber = 0;
         this.period = null;
         this.type = null;
         this.year = 0;
         this.successfulTikliSaves = 0;
 
     }
 }
