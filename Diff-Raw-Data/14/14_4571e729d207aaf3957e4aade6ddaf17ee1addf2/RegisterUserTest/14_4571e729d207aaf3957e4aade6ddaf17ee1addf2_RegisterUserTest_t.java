 package org.timadorus.webapp.tests.selenium;
 
 // Tests the registration, login and logout of users
 public class RegisterUserTest extends WebTestCase {
   
   final int sleepTime = 30000;
   final int ajaxWait = 2000;
   
   @Override
   public void setUp() throws Exception {
     setUp("http://127.0.0.1:8888/", "*firefox");
   }
 
   public void testRegisterUser() throws Exception {
     selenium.open("/TimadorusWebApp.html");
     
     // registration
     click("link=Account registrieren");
     type("//input[@type='text']", "Test");
     type("//div[@id='content']/form/table/tbody/tr[2]/td[2]/input", "1");
     type("//div[@id='content']/form/table/tbody/tr[3]/td[2]/input", "31.10.1988");
     type("//div[@id='content']/form/table/tbody/tr[4]/td[2]/input", "test1@home.de");
     type("//div[@id='content']/form/table/tbody/tr[5]/td[2]/input", "test1@home.de");
     type("//div[@id='content']/form/table/tbody/tr[6]/td[2]/input", "test1");
     type("//input[@type='password']", "geheim");
     type("//div[@id='content']/form/table/tbody/tr[8]/td[2]/input", "geheim");    
     click("//button[@type='button']");
     
     // creation of database and table "users"    
     Thread.sleep(sleepTime);
     
     // activation link
    String activationLink = selenium.getText("//b");
    selenium.open(activationLink);
     selenium.type("//input[@type='text']", "test1");
     selenium.type("//input[@type='password']", "geheim");
     selenium.click("//button[@type='button']");
     Thread.sleep(ajaxWait);
     selenium.type("//input[@type='text']", "test1");
     selenium.type("//input[@type='password']", "geheim");
     selenium.click("//button[@type='button']");
     
     // Verification
     verifyTrue(selenium.isTextPresent("Du bist als test1 angemeldet"));
     verifyTrue(selenium.isTextPresent("Liste der Charaktere"));
     verifyTrue(selenium.isTextPresent("Profil bearbeiten"));
     verifyTrue(selenium.isTextPresent("Ausloggen"));
     click("link=Ausloggen");
     verifyTrue(selenium.isTextPresent("Sie haben sich ausgeloggt."));
   }
 }
