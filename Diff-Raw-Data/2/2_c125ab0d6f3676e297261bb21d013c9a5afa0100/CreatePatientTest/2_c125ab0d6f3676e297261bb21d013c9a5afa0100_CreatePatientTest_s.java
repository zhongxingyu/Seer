 package org.motechproject.ghana.national.functional;
 
 import org.junit.runner.RunWith;
 import org.motechproject.functional.base.WebDriverProvider;
 import org.motechproject.functional.pages.CreatePatientPage;
 import org.motechproject.functional.pages.HomePage;
 import org.motechproject.functional.pages.LoginPage;
 import org.motechproject.ghana.national.functional.helper.CreatePatientHelper;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.PageFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
 import org.testng.annotations.AfterSuite;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.Calendar;
 
 import static org.testng.Assert.assertTrue;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class CreatePatientTest extends AbstractTestNGSpringContextTests {
 
     @Autowired
     private CreatePatientHelper createPatientHelper;
 
     @Autowired
     private WebDriverProvider driverProvider;
 
     protected WebDriver driver;
 
     @BeforeMethod
     public void setUp() {
         driver = driverProvider.getWebDriver();
     }
 
     @Test
     public void createPatientWithValidValues() {
         Calendar dateOfBirth = Calendar.getInstance();
         dateOfBirth.set(1980, 01, 01);
        assertTrue(createPatientHelper.createPatient("AutomationPatient", "Auto Middle Name", "Auto Last Name", true, dateOfBirth, CreatePatientPage.PATIENT_TYPE.PATIENT_MOTHER, null));
     }
 
     @Test
     public void createPatientChildUnder5() {
         Calendar dateOfBirth = Calendar.getInstance();
         dateOfBirth.set(2009, 01, 01);
         assertTrue(createPatientHelper.createPatient("AutomationChild", "ChildMiddleName", "ChildLastName", false, dateOfBirth, CreatePatientPage.PATIENT_TYPE.CHILD_UNDER_FIVE, null));
     }
 
     @Test
     public void createPatientTypeOther() {
         Calendar dateOfBirth = Calendar.getInstance();
         dateOfBirth.set(2009, 01, 01);
         assertTrue(createPatientHelper.createPatient("AutomationOther", "OtherMiddleName", "OtherLastName", true, dateOfBirth, CreatePatientPage.PATIENT_TYPE.OTHER, null));
     }
 
     @AfterSuite
     public void closeall() {
         driver.quit();
     }
 }
