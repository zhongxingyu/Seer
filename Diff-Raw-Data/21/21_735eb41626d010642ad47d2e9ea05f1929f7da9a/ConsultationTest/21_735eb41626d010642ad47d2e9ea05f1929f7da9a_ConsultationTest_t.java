 package org.openmrs.module.mirebalais.smoke;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.dataModel.Patient;
 import org.openmrs.module.mirebalais.smoke.helper.PatientDatabaseHandler;
 import org.openmrs.module.mirebalais.smoke.pageobjects.LoginPage;
 import org.openmrs.module.mirebalais.smoke.pageobjects.PatientDashboard;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class ConsultationTest extends DbTest {
 
     private static final String PRIMARY_DIAGNOSIS = "IGU";
     private static final String EDITED_PRIMARY_DIAGNOSIS = "Asthme";
 
 	@BeforeClass
 	public static void prepare() {
		new LoginPage(driver).logInAsDoctor();
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		Patient testPatient = PatientDatabaseHandler.insertNewTestPatient();
 		initBasicPageObjects();
 		
 		appDashboard.goToPatientPage(testPatient.getId());
 		patientDashboard.startVisit();
 	}
 	
 	@Test
 	public void addConsultationToAVisitWithoutCheckin() throws Exception {
 		patientDashboard.addConsultNoteWithDischarge(PRIMARY_DIAGNOSIS);
 		
 		assertThat(patientDashboard.countEncountersOfType(PatientDashboard.CONSULTATION), is(1));
 	}
 	
 	@Test
 	public void addConsultationNoteWithDeathAsDispositionClosesVisit() throws Exception {
 		patientDashboard.addConsultNoteWithDeath(PRIMARY_DIAGNOSIS);
 		
 		assertThat(patientDashboard.isDead(), is(true));
 		assertThat(patientDashboard.hasActiveVisit(), is(false));
 		assertThat(patientDashboard.startVisitButtonIsVisible(), is(false));
 	}
 
 
     @Test
     public void editConsultationNote() throws Exception {
 
         patientDashboard.addConsultNoteWithDischarge(PRIMARY_DIAGNOSIS);
         patientDashboard.editExistingConsultNote(EDITED_PRIMARY_DIAGNOSIS);
 
         assertThat(patientDashboard.countEncountersOfType(PatientDashboard.CONSULTATION), is(1));
 
         patientDashboard.viewConsultationDetails();
         assertThat(patientDashboard.containsText(EDITED_PRIMARY_DIAGNOSIS), is(true));
         assertThat(patientDashboard.containsText(PRIMARY_DIAGNOSIS), is(false));
     }
 
 	@Test
 	public void addEDNote() throws Exception {
 		patientDashboard.addEmergencyDepartmentNote(PRIMARY_DIAGNOSIS);
 		
 		assertThat(patientDashboard.countEncountersOfType(PatientDashboard.CONSULTATION), is(1));
 	}
 
     @Test
     public void editEDNote() throws Exception {
 
         patientDashboard.addEmergencyDepartmentNote(PRIMARY_DIAGNOSIS);
         patientDashboard.editExistingEDNote(EDITED_PRIMARY_DIAGNOSIS);
 
         assertThat(patientDashboard.countEncountersOfType(PatientDashboard.CONSULTATION), is(1));
 
         patientDashboard.viewConsultationDetails();
         assertThat(patientDashboard.containsText(EDITED_PRIMARY_DIAGNOSIS), is(true));
         assertThat(patientDashboard.containsText(PRIMARY_DIAGNOSIS), is(false));
     }
 
 }
