 package org.openmrs.module.mirebalais.smoke;
 
 import org.junit.Test;
 import org.openmrs.module.mirebalais.smoke.dataModel.Patient;
 import org.openmrs.module.mirebalais.smoke.helper.PatientDatabaseHandler;
 import org.openmrs.module.mirebalais.smoke.pageobjects.ArchivesRoomApp;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 
 public class ArchivesRoomFlowTest extends DbTest {
 	
 	@Test
 	public void requestRecord() throws Exception {
         Patient testPatient = PatientDatabaseHandler.insertNewTestPatient();
         initBasicPageObjects();
         ArchivesRoomApp archivesRoomApp = new ArchivesRoomApp(driver);
 
 		login();
 		
 		appDashboard.goToPatientPage(testPatient.getId());
 		patientDashboard.requestRecord();
 		
 		appDashboard.openArchivesRoomApp();
 		
 		String dossierNumber = archivesRoomApp.createRecord(testPatient.getIdentifier());
 		archivesRoomApp.sendDossier(dossierNumber);
 		archivesRoomApp.returnRecord(dossierNumber);
 		
 		appDashboard.goToPatientPage(testPatient.getId());
 		
 		assertThat(patientDashboard.getDossierNumber(), is(dossierNumber));
 		assertThat(patientDashboard.canRequestRecord(), is(true));
 	}
 	
 }
