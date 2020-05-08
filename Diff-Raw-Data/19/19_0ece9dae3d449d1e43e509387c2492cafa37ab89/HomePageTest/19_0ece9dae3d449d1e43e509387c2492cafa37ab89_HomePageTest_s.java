 package org.motechproject.ghana.national.functional;
 
import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.functional.base.WebDriverProvider;
 import org.motechproject.functional.pages.LoginPage;
 import org.openqa.selenium.WebDriver;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.BeforeMethod;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class HomePageTest {
     @Autowired
     private LoginPage loginPage;
 
     @Autowired
     private WebDriverProvider driverProvider;
 
     private WebDriver driver;
 
     @BeforeMethod
     public void setUp() {
         driver = driverProvider.getWebDriver();
     }
 
    @Test
     public void verifyHomePageLinks() {
         loginPage.LoginAs("admin", "P@ssw0rd");
     }
 }
