 package org.openmrs.module.mirebalais.smoke;
 
 import static org.junit.Assert.assertTrue;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.pageobjects.AppDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.MergeFlow;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.Registration;
 import org.openmrs.module.mirebalais.smoke.pageobjects.SysAdminPage;
 
 public class MergePatientTest extends BasicMirebalaisSmokeTest {
 
 	private static LoginPage loginPage;
 	private AppDashboard appDashboard;
 	private SysAdminPage sysAdminPage;
 	private MergeFlow mergeFlow;
 	private Registration registration;
 	private PatientDashboard patientDashboard; 
 	
	private String patientOne = "Bruce Wayne";
	private String patientTwo = "Clark Kent";
	
 	
 	@Before
     public void setUp() {
 		loginPage = new LoginPage(driver);
 		appDashboard = new AppDashboard(driver);
 		sysAdminPage = new SysAdminPage(driver);
 		mergeFlow = new MergeFlow(driver);
 		registration = new Registration(driver);
 		patientDashboard = new PatientDashboard(driver);
 
 		loginPage.logIn("admin", "Admin123");
 
 		appDashboard.openPatientRegistrationApp();
 		registration.registerSpecificGuyWithoutPrintingCard(patientOne);
 		
 		appDashboard.openPatientRegistrationApp();
 		registration.registerSpecificGuyWithoutPrintingCard(patientTwo);
 	}
 	
 	@Test
 	public void mergePatientsByName() {
 		appDashboard.openSysAdminApp();
 		sysAdminPage.openManagePatientRecords();
 		
 		mergeFlow.setPatientsToMerge(patientOne, patientTwo);
 		
 		assertTrue(patientDashboard.getIdentifiers().size() > 1);
 	}
 
 
 }
