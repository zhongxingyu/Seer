 package org.sukrupa.app.students;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 import java.util.List;
 
 public class ViewStudentPage {
     private WebDriver driver;
 
     public ViewStudentPage(WebDriver driver, String id) {
         this.driver = driver;
         driver.get("http://localhost:8080/students/" + id);
     }
 
     public String getStudentName() {
        List<WebElement> elements = driver.findElements(By.xpath("//p[@class='name']"));
         WebElement nameElement = elements.get(0);
         return nameElement.getText();
     }
 }
