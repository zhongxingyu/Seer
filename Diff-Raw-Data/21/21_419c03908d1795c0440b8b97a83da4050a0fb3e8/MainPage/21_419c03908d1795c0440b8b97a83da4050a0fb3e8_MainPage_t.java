 package com.usulful.jenkins.page;
 
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
 
 public class MainPage {
    private static final int INITIALIZATION_TIMEOUT_IN_SECONDS = 20;
    private static final String NEW_JOB = "New Job";
     private WebDriver driver;
 
     public MainPage(WebDriver driver) {
         this.driver = driver;
     }
 
     public NewJobPage clickNewJob() {
        driver.findElement(By.linkText(NEW_JOB)).click();
         return PageFactory.initElements(driver, NewJobPage.class);
     }
 
     public JobPage openJob(String jobName) {
         driver.findElement(By.linkText(jobName)).click();
         return PageFactory.initElements(driver, JobPage.class);
     }

    public MainPage waitForInitialization() {
        new WebDriverWait(driver, INITIALIZATION_TIMEOUT_IN_SECONDS)
                .until(ExpectedConditions.elementToBeClickable(By.linkText(NEW_JOB)));
        return this;
    }
 }
