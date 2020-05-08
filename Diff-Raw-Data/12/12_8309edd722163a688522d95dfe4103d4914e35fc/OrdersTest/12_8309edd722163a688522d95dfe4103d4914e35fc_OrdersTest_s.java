 package org.openmrs.module.mirebalais.smoke;
 
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.pageobjects.AppDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientRegistrationDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.Registration;
 import org.openqa.selenium.By;
 import org.openqa.selenium.WebDriver;
 import org.openqa.selenium.support.ui.ExpectedCondition;
 import org.openqa.selenium.support.ui.Wait;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 public class OrdersTest extends BasicMirebalaisSmokeTest {
 
     private static LoginPage loginPage;
     private Registration registration;
     private PatientRegistrationDashboard patientRegistrationDashboard;
     private PatientDashboard patientDashboard;
     private AppDashboard appDashboard;
     private String patientIdentifier;
     
     private static final String STUDY_1 = "Hanche - Gauche, 2 vues (Radiographie)";
     private static final String STUDY_2 = "Humérus - Gauche, 2 vues (Radiographie)";
     
     @Before
 	public void setUp() {
         loginPage = new LoginPage(driver);
         registration = new Registration(driver);
         patientDashboard = new PatientDashboard(driver);
         patientRegistrationDashboard = new PatientRegistrationDashboard(driver);
         appDashboard = new AppDashboard(driver);
 	}
     
 	@Test
 	public void orderSingleXRay() throws Exception{
         loginPage.logInAsAdmin();
         appDashboard.openPatientRegistrationApp();
         registration.goThruRegistrationProcessWithoutPrintingCard();
         patientIdentifier = patientRegistrationDashboard.getIdentifier();
 
         appDashboard.findPatientById(patientIdentifier);
         patientDashboard.startVisit();
 
         Wait<WebDriver> wait = new WebDriverWait(driver, 5);
         wait.until(new ExpectedCondition<Boolean>() {
             @Override
             public Boolean apply(WebDriver webDriver) {
                return webDriver.findElement(By.cssSelector("div.visit-status")).isDisplayed();
             }
         });
         assertTrue(patientDashboard.hasActiveVisit());
 		patientDashboard.orderXRay(STUDY_1, STUDY_2);
 		
 		assertThat(patientDashboard.countEncouters(PatientDashboard.RADIOLOGY), not(0));
 	}
 }
 
