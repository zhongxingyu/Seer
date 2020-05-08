 package org.openmrs.module.mirebalais.smoke;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.fail;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.pageobjects.AppDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.ArchivesRoomApp;
 import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientRegistrationDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.Registration;
 
 public class ArchivesRoomFlowTest extends BasicMirebalaisSmokeTest {
 
 	private static LoginPage loginPage;
 	private Registration registration;
 	private PatientDashboard patientDashboard;
 	private PatientRegistrationDashboard patientRegistrationDashboard;
 	private AppDashboard appDashboard;
 	private String patientIdentifier;
 	private String patientName;
 	private ArchivesRoomApp archivesRoomApp;
 	
 	
 	@Before
     public void setUp() {
 		loginPage = new LoginPage(driver);
 		registration = new Registration(driver);
 		patientDashboard = new PatientDashboard(driver);
 		patientRegistrationDashboard = new PatientRegistrationDashboard(driver);
 		appDashboard = new AppDashboard(driver);
 		archivesRoomApp = new ArchivesRoomApp(driver);
 	}
 
 	@Test
 	public void requestRecord() throws Exception{
 		String dossieNumber = null;
 		
 		loginPage.logInAsAdmin();
 		appDashboard.openPatientRegistrationApp();
 		registration.goThruRegistrationProcessWithoutPrintingCard(); 
 		patientIdentifier = patientRegistrationDashboard.getIdentifier();
 		patientName = patientRegistrationDashboard.getName();
 
 		appDashboard.findPatientById(patientIdentifier);
 		patientDashboard.requestRecord();
 		 
 		appDashboard.openArchivesRoomApp();
 		
 		try {
 			archivesRoomApp.createRecord(patientIdentifier);
 			
 			appDashboard.openArchivesRoomApp();
 			
 			dossieNumber = archivesRoomApp.getDossieNumber(patientName);
 			
 			archivesRoomApp.sendDossie(dossieNumber);
 			archivesRoomApp.returnRecord(dossieNumber);
 				
 			appDashboard.findPatientById(patientIdentifier);
 		} catch (Exception e) {
 			fail(e.getMessage());
 		}
 		
 		assertThat(patientDashboard.getDossieNumber(), is(dossieNumber));
 		assertThat(patientDashboard.canRequestRecord(), is(true));
 	}
 	
 	@After
 	public void cleanAllRequests() {
 		appDashboard.openArchivesRoomApp();
 		archivesRoomApp.clearRequestList();
 	}
 		
 }
