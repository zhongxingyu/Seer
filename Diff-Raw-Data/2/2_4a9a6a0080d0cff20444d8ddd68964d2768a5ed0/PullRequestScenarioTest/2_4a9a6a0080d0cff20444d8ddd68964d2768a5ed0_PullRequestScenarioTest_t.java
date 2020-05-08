 package org.openmrs.module.mirebalais.smoke;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.pageobjects.AppDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.CheckIn;
 import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientRegistrationDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.Registration;
 import org.openqa.selenium.By;
 import org.openqa.selenium.support.ui.ExpectedConditions;
 import org.openqa.selenium.support.ui.WebDriverWait;
 
 public class PullRequestScenarioTest extends BasicMirebalaisSmokeTest {
 
 	private CheckIn checkIn;
 	private static LoginPage loginPage;
 	private Registration registration;
 	private PatientRegistrationDashboard patientDashboard;
 	private AppDashboard appDashboard;
 	
 	private String patientName;
 	private String patientIdentifier;
 	
 	
 	@Before
     public void setUp() {
 		registration = new Registration(driver);
 		patientDashboard = new PatientRegistrationDashboard(driver);
 		checkIn = new CheckIn(driver);
 		appDashboard = new AppDashboard(driver);
 	}
 
 	@BeforeClass
     public static void setUpEnvironment() {
     	loginPage = new LoginPage(driver);
     	loginPage.logInAsAdmin();
     }
 
 	@Test
 	public void createsARecord() throws InterruptedException {
 		appDashboard.openPatientRegistrationApp();
 		registration.goThruRegistrationProcessWithoutPrintingCard();
 		patientIdentifier = patientDashboard.getIdentifier();
 		patientName = patientDashboard.getName();

        appDashboard.openStartClinicVisitApp();
 		checkIn.checkInPatient(patientIdentifier, patientName);
 		appDashboard.openArchivesRoomApp();
 
         WebDriverWait wait = new WebDriverWait(driver, 1000);
         wait.until(ExpectedConditions.textToBePresentInElement(By.id("create_requests_table"), patientName));
         wait.until(ExpectedConditions.textToBePresentInElement(By.id("create_requests_table"), patientIdentifier));
 	}
 
 }
