 package com.usulful.jenkins.page;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.PageFactory;
 
 public class MainPage {
     private WebDriver driver;
 
     public MainPage(WebDriver driver) {
         this.driver = driver;
     }
 
     public NewJobPage clickNewJob() {
        driver.findElement(By.linkText("New Job")).click();
         return PageFactory.initElements(driver, NewJobPage.class);
     }
 
     public JobPage openJob(String jobName) {
         driver.findElement(By.linkText(jobName)).click();
         return PageFactory.initElements(driver, JobPage.class);
     }
 }
