 package org.patientview.radar.test.service;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.patientview.model.Centre;
 import org.patientview.model.Patient;
 import org.patientview.model.enums.NhsNumberType;
 import org.patientview.model.enums.SourceType;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.radar.dao.UserDao;
 import org.patientview.radar.model.user.PatientUser;
 import org.patientview.radar.service.PatientManager;
 import org.patientview.radar.service.UserManager;
 import org.patientview.radar.test.TestDataHelper;
 import org.patientview.radar.test.TestPvDbSchema;
 import org.springframework.test.context.ContextConfiguration;
 
 import javax.inject.Inject;
 import java.util.Date;
 import java.util.List;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 
 @RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:test-context.xml"})
 public class UserManagerTest extends TestPvDbSchema {
 
     @Inject
     private UserDao userDao;
 
     @Inject
     private UserManager userManager;
 
     @Inject
     private PatientManager patientManager;
 
 
 
     @Inject
     private TestDataHelper testDataHelper;
 
     private DiseaseGroup diseaseGroup;
 
     private Centre centre;
 
 
     @Before
     public void setUp() {
         diseaseGroup = new DiseaseGroup();
         diseaseGroup.setId("1");
         diseaseGroup.setName("testGroup");
         diseaseGroup.setShortName("shortName");
 
         centre = new Centre();
         centre.setUnitCode("testCodeA");
 
         testDataHelper.createSpecialty();
     }
 
     /**
      * Create the unit admin who will create the user:
      *      - user, tbl_users, specialuserrole (unitadmin) etc
      *      - usermapping (PV) of username, unitcode e.g. ALPORTS unit, nhsno, specialty (mock this)
      *      - usermapping (RDR) of radarUserId (tbl_users.uID), role (ROLE_PROFESSIONAL)
      *
      * Create a patient using this unit admin (the first patient in the system so not linking madness)
      *      - user, tbl_patient_users, specialuserrole (patient) etc
      *      - usermapping (PV) of username, unitcode e.g. ALPORTS unit, nhsno, specialty (mock this)
      *      - patient record
      */
     @Test
     public void testRadarUnitAdminCanCreatePatientFromScratch() {
 
     }
 
     /**
      * This is testing the creation of a record in the tbl patient users table
      *
      * @throws Exception
      */
     @Test
     public void testPatientUserRegistration() throws Exception {
         // create a user row as per patient view
         PatientUser patientUser = new PatientUser();
         patientUser.setName("my user");
         patientUser.setUsername("testusername");
         patientUser.setEmail("test@test.com");
         patientUser.setPassword("passwordhash");
         userDao.createUser(patientUser);
 
         // create a demographic
         Patient patient = createDemographics("Test", "User", centre, "NHS123", "test@test.com", SourceType.RADAR);
 
         userManager.addPatientUserOrUpdatePatient(patient);
 
         // Try and register - will throw an exception as no matching radar number
         patientUser = userManager.getPatientUser(patient.getEmailAddress());
 
         assertNotNull("registered user is null", patientUser);
         assertNotNull("no password generated", patientUser.getPassword());
         assertEquals("Email not set on user", patient.getEmailAddress(), patientUser.getEmail());
         assertNotNull("Dob not set on user", patientUser.getDateOfBirth());
     }
 
     /**
      * Get a patient record with a patient view source type and then link a radar patient to it
      *
      *
      */
     @Test
     public void testLinkPatientCreation() throws Exception {
 
         // Create the link radarPatient
         String linkNhsNo = "897897898";
         Patient pvPatient = createDemographics("Test", "Link", centre, linkNhsNo, "890789@ajkhjk.com", SourceType.PATIENT_VIEW);
         patientManager.save(pvPatient);
 
         // Set the user to a Patient View user
         testDataHelper.setPatientSource(pvPatient.getId(), SourceType.PATIENT_VIEW);
 

        Patient linkPatient = patientManager.createLinkPatient(pvPatient);
        linkPatient.setRenalUnit(centre);
        userManager.addPatientUserOrUpdatePatient(linkPatient);
 
         List<Patient> patients = patientManager.getPatientByNhsNumber(linkNhsNo);
 
         // Reload the radarPatient
         pvPatient = patientManager.getById(pvPatient.getId());
 
        assertTrue("There should be two patients with the same nhs number", patients.size() == 2);
 
 
     }
 
 
     private Patient createDemographics(String forename, String surname, Centre centre, String nhsno,
                                             String email, SourceType sourceType) {
         Patient patient = new Patient();
         patient.setForename(forename);
         patient.setSurname(surname);
         patient.setDob(new Date());
         patient.setNhsNumberType(NhsNumberType.NHS_NUMBER);
         patient.setNhsno(nhsno);
         patient.setRenalUnit(centre);
         patient.setEmailAddress(email);
         patient.setDiseaseGroup(diseaseGroup);
         patient.setSourceType(sourceType.getName());
         return patient;
     }
 
 }
