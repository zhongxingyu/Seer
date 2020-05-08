 package org.openmrs.module.mirebalais.smoke;
 
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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
 
 public class ConsultationTest extends BasicMirebalaisSmokeTest {
 
 	private static LoginPage loginPage;
 	private Registration registration;
 	private PatientRegistrationDashboard patientRegistrationDashboard;
 	private PatientDashboard patientDashboard;
 	private AppDashboard appDashboard;
 	private String patientIdentifier;
 	
 	
 	@Before
     public void setUp() {
 		loginPage = new LoginPage(driver);
 		registration = new Registration(driver);
 		patientRegistrationDashboard = new PatientRegistrationDashboard(driver);
 		patientDashboard = new PatientDashboard(driver);
 		appDashboard = new AppDashboard(driver);
 	}
 	
 	@Test
 	public void addConsultationToAVisitWithoutCheckin() throws Exception {
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
				return 	webDriver.findElement(By.cssSelector("div.status-container")).isDisplayed();
 			}
 		});
 		assertTrue(patientDashboard.hasActiveVisit());
 		
 		patientDashboard.addConsulteNote();
 		assertThat(patientDashboard.countEncouters(PatientDashboard.CONSULTATION), is(1));
 	}
 }
