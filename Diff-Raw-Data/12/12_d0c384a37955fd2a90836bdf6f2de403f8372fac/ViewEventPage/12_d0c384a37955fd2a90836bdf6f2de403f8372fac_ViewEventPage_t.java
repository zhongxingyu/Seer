 package org.sukrupa.app.event;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class ViewEventPage {
     private WebDriver driver;
 
     public ViewEventPage(WebDriver driver) {
         this.driver = driver;
     }
 
     public String getTitle() {
         return textFor("eventTitle");
     }
 
     public String getVenue() {
         return textFor("venue");
     }
 
     public String getDate() {
         return textFor("date");
     }
 
     public String getDay() {
         return textFor("day");
     }
 
     public String getTime() {
         return textFor("time");
     }
 
     public String getDescription() {
        return textFor("eventDescription");
     }
 
     public String getNotes() {
        return textFor("eventNotes");
     }
 
     public Set<String> getAttendees() {
         Set<String> attendees = new HashSet<String>();
         WebElement element = driver.findElement(By.xpath("//*[@class='attendees']"));
         for (String attendeeName : element.getText().split(", ")) {
             attendees.add(attendeeName);
         }
         return attendees;
     }
 
     private String textFor(String field) {
         return driver.findElement(By.xpath("//*[@class='" + field + "']")).getText();
     }
 
     public void navigateTo(int eventId) {
         driver.get("http://localhost:8080/events/" + eventId);
     }
 }
