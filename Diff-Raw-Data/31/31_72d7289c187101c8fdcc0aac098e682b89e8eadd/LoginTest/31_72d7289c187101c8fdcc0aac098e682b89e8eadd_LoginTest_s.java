 /*
  * LICENSE
  * "THE BEER-WARE LICENSE" (Revision 42):
  * "Sven Strittmatter" <ich(at)weltraumschaf(dot)de> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a beer in return.
  */
 package org.cacert.frontendtests;
 
 import java.util.List;
 import org.junit.Ignore;
 import org.junit.Test;
 import static org.junit.Assert.*;
 import org.junit.Before;
 import org.junit.After;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.WebElement;
 
 /**
  *
  * @author Sven Strittmatter <ich@weltraumschaf.de>
  */
 public class LoginTest {
 
     private static final String TEST_SYSTEM = "https://cacert1.it-sls.de/";
     private static final String MANAGEMENT_SYSTEM = "https://ca-mgr1.it-sls.de/";
     private WebDriver driver;
     private RandomString stringGenerator;
 
     public LoginTest() {
         stringGenerator = new RandomString(10);
     }
 
     @Before
     public void setUp() {
         try {
             driver = TestHelper.createDriverByType(TestHelper.DriverType.HTMLUNIT);
         } catch (Exception ex) {
             fail(ex.getMessage());
         }
     }
 
     @After
     public void tearDown() {
         driver.close();
         driver = null;
     }
 
     private String generateFirstName() {
         return "First" + stringGenerator.next();
     }
 
     @Test public void checkFirstNamePrefix() {
         assertEquals("First", generateFirstName().substring(0, 5));
         assertEquals("First", generateFirstName().substring(0, 5));
         assertEquals("First", generateFirstName().substring(0, 5));
     }
 
     private String generateLastName() {
         return "Last" + stringGenerator.next();
     }
 
     @Test public void checkLastNamePrefix() {
         assertEquals("Last", generateLastName().substring(0, 4));
         assertEquals("Last", generateLastName().substring(0, 4));
         assertEquals("Last", generateLastName().substring(0, 4));
     }
 
     private String generatePassword() {
         return "PW-" + stringGenerator.next();
     }
 
     @Test public void checkPasswordPrefix() {
         assertEquals("PW-", generatePassword().substring(0, 3));
         assertEquals("PW-", generatePassword().substring(0, 3));
         assertEquals("PW-", generatePassword().substring(0, 3));
     }
 
     private String generateEmailAddres(String firstName, String lastName) {
         StringBuilder sb = new StringBuilder(firstName.toLowerCase());
         sb.append("@").append(lastName.toLowerCase()).append(".de");
         return sb.toString();
     }
 
     @Test
     public void checkGeneratedEmailAddreses() {
         assertEquals("hans@dampf.de", generateEmailAddres("hans", "dampf"));
         assertEquals("hans@dampf.de", generateEmailAddres("Hans", "dampf"));
         assertEquals("hans@dampf.de", generateEmailAddres("Hans", "Dampf"));
     }
 
//    @Ignore("Not ready yet")
     @Test
     public void registerActivateAndLoginNewUser() {
         /*
          * 1. register at TEST_SYSTEM
          * 2. activate at MANAGEMENT_SYSTEM
          * 3. login at TEST_SYSTEM
          * 4. logout at TEST_SYSTEM
          */
         String firstName    = generateFirstName();
         String lastName     = generateLastName();
         String emailAddress = generateEmailAddres(firstName, lastName);
         String password     = generatePassword();
         int day             = 23;
         int month           = 5;
         String year         = "1979";
 
         driver.get(TEST_SYSTEM + "index.php?id=1");
         driver.findElement(By.name("fname"))
               .sendKeys(firstName);
         driver.findElement(By.name("lname"))
               .sendKeys(lastName);
         List<WebElement> days = driver.findElement(By.name("day"))
                                       .findElements(By.tagName("option"));
         days.get(day - 1).click();
         List<WebElement> months = driver.findElement(By.name("month"))
                                         .findElements(By.tagName("option"));
         months.get(month - 1).click();
         driver.findElement(By.name("year"))
               .sendKeys(year);
         driver.findElement(By.name("email"))
               .sendKeys(emailAddress);
         driver.findElement(By.name("pword1"))
               .sendKeys(password);
         driver.findElement(By.name("pword2"))
               .sendKeys(password);
 
        for (int i = 1; i <= 5 ; i++) {
            driver.findElement(By.name("Q" + i))
                  .sendKeys("qestion_" + i);
            driver.findElement(By.name("A" + 1))
                  .sendKeys("answer_" + i);
        }
 
         driver.findElement(By.name("cca_agree"))
               .click();
         driver.findElement(By.name("process"))
               .click();
         System.out.println(driver.getPageSource());
//        driver.findElement(By.cssSelector(".storry"))
//              .findElement(By.tagName("p"));
 //        System.out.println(emailAddress + " - " + password);
     }
 }
