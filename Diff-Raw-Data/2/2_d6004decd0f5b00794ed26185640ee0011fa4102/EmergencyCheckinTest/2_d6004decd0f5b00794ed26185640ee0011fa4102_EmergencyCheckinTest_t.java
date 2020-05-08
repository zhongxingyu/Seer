 package org.openmrs.module.mirebalais.smoke;
 
 import static org.hamcrest.CoreMatchers.*;
 import static org.junit.Assert.assertThat;
 
 import java.util.Arrays;
 
 import org.hamcrest.Matchers;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.dataModel.BasicReportData;
 import org.openmrs.module.mirebalais.smoke.pageobjects.AppDashboard;
 import org.openmrs.module.mirebalais.smoke.pageobjects.BasicReportPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.EmergencyCheckin;
 import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientRegistrationDashboard;
 
 public class EmergencyCheckinTest extends BasicMirebalaisSmokeTest {
 
     private EmergencyCheckin emergencyCheckinPO;
     private static LoginPage loginPage;
     private PatientRegistrationDashboard patientDashboard;
 	private BasicReportPage basicReport;
 	private AppDashboard appDashboard;
     
     @Before
     public void setUp() {
         emergencyCheckinPO = new EmergencyCheckin(driver);
         patientDashboard = new PatientRegistrationDashboard(driver);
         appDashboard = new AppDashboard(driver);
         basicReport = new BasicReportPage(driver);
     }
 
     @BeforeClass
     public static void setUpEnvironment() {
     	loginPage = new LoginPage(driver);
     	loginPage.logInAsAdmin();
     }
     
     @Test
     public void checkinOnEmergencyShouldCountOnReports() {
     	appDashboard.openReportApp();
     	BasicReportData brd1 = basicReport.getData();
     	
     	appDashboard.openStartHospitalVisitApp();
         emergencyCheckinPO.checkinMaleUnindentifiedPatient();
         
         assertThat(patientDashboard.getIdentifier(), is(notNullValue()));
         assertThat(patientDashboard.getGender(), is("M"));
         assertThat(patientDashboard.getName(), Matchers.stringContainsInOrder(Arrays.asList("UNKNOWN", "UNKNOWN")));
         
         appDashboard.openReportApp();
         BasicReportData brd2 = basicReport.getData();
         
         assertThat(brd2.getOpenVisits(), Matchers.greaterThan(brd1.getOpenVisits()));
        assertThat(brd2.getRegistrationToday(), Matchers.greaterThan(brd1.getRegistrationToday()));
     }
 }
