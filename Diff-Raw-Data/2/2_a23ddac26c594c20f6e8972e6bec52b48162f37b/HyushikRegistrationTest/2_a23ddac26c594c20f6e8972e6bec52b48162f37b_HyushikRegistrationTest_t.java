 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.hyushik.registration.test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 import org.junit.Ignore;
 import org.junit.Before;
 import org.junit.After;
 import org.junit.runner.RunWith;
 import org.junit.runners.JUnit4;
 import junit.framework.TestCase;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 import org.openqa.selenium.firefox.FirefoxDriver;
 import java.util.Properties;
 import java.io.FileInputStream;
 import java.io.IOException;
 
 /**
  *
  * @author McAfee
  */
 @RunWith(JUnit4.class)
 public class HyushikRegistrationTest{
     private Properties props = new Properties();
     private String baseUrl;
     private WebDriver driver;
 
     @Before
     public void openBrowser() {
         FileInputStream fizban;
         try{
            fizban = new FileInputStream("webserver.properties");
         }catch(Exception e){
             return;
         }
         try{
             props.load(fizban);
         }catch(Exception io){
             return;
         }
         
         baseUrl = props.getProperty("weburl");
         driver = new FirefoxDriver();
         driver.get(baseUrl);
     }
   
     @Test
     public void assTrue(){
         String actualTitle = driver.getTitle();
         assertEquals("Hyushik Registration", actualTitle);
         
     } 
     
     @After
     public void closeBrowser(){
         driver.close();
     }
     
     
             
 }
