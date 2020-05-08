 package com.fortysix.seleniumTests;
 
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.chrome.ChromeDriver;
 import org.openqa.selenium.firefox.FirefoxDriver;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Workday
  * User: johnwelsh
  * Date: 7/2/12
  * Time: 8:44 PM
  */
 public class TestMain {
     static Logger logger = Logger.getLogger("mainLogger");
     public static void main(String[] args) {
         logger.log(Level.INFO, "Hello world!");
        logger.log(Level.INFO, "HI THERE JOHN");
         //WebDriver firefoxDriver = new FirefoxDriver();
         //SeleniumExample example = new SeleniumExample(firefoxDriver);
         //example.testGoogleSearch();
     }
 }
