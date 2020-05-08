 package org.patientview.radar.test.roles.unitadmin;
 
 import org.apache.commons.lang.StringUtils;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.patientview.model.Centre;
 import org.patientview.model.Patient;
 import org.patientview.model.enums.NhsNumberType;
 import org.patientview.model.generic.DiseaseGroup;
 import org.patientview.radar.dao.UtilityDao;
 import org.patientview.radar.model.user.PatientUser;
 import org.patientview.radar.service.DemographicsManager;
 import org.patientview.radar.service.UserManager;
 import org.patientview.radar.test.TestPvDbSchema;
 import org.springframework.test.context.ContextConfiguration;
 
 import javax.inject.Inject;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 /**
  * Notes: Seen on frontend via ExistingPatientsListingPage,
  * the DemographicsDataProvider uses DemographicsManager.getDemographicsByRenalUnit.
  * <p/>
  * <p/>
  * Todo move the logic to get Centre from User out of ExistingPatientsListingPage so it can be covered by these tests
  * <p/>
  * todo don't apply the dataset.xml, and build the test context from scratch for each role
  */
 @RunWith(org.springframework.test.context.junit4.SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:test-context.xml"})
 public class UnitAdminTests extends TestPvDbSchema {
 
     @Inject
     RoleHelper roleHelper;
 
     @Inject
     UtilityDao utilityDao;
 
     @Inject
     UserManager userManager;
 
     @Inject
     DemographicsManager demographicsManager;
 
     /**
      * A unit admin is created by the superadmin in PV.
      * <p/>
      * Create a basic PV user structure - User - Role as unit admin etc
      */
     @Before
     public void setup() {
 
     }
 
 
     /**
      * Test: the creation of a patient from Radar and make sure the User table and the UserMappings table get
      * correctly updated. For this user there will be no record existing in the patient table.
      *
      * The correct mapping should be
      * 1) To the disease the patient has currently diagnosed with
      * 2) The unit that the user selected in the Renal Unit
      * 3) The user should be mapped to the Patient Unit Group
      *
      * @throws Exception
      */
     @Test
     public void testUserRegistrationWithMappings() throws Exception {
 
         final String testDiseaseUnit = "RENALT";
         final String testRenalUnit = "DISEASET";
         Patient patient = new Patient();
         PatientUser patientUser = null;
         try {
 
             // Setup
             utilityDao.createUnit(testRenalUnit);
             utilityDao.createUnit(testDiseaseUnit);
 
             patient.setSurname("Test");
             patient.setForename("Registration");
             patient.setDateofbirth("01-01-1940");
             patient.setDob(new Date());
             patient.setEmailAddress("test@testtheregistrationbit.com");
             patient.setUnitcode("");
             patient.setNhsno("8768768765");
             patient.setNhsNumberType(NhsNumberType.NHS_NUMBER);
             patient.setUnitcode(testDiseaseUnit);
 
             Centre centre = new Centre();
             centre.setUnitCode(testRenalUnit);
             patient.setRenalUnit(centre);
 
             DiseaseGroup diseaseGroup = new DiseaseGroup();
             diseaseGroup.setId(testDiseaseUnit);
             patient.setDiseaseGroup(diseaseGroup);
 
             // Test
             patientUser = userManager.registerPatient(patient);
 
             // Assert
             List<String> unitCodes = userManager.getUnitCodes(patientUser);
             Assert.assertTrue("There should be two units mapped to the user", unitCodes.size() == 3);
             Assert.assertTrue("The should be a " + testDiseaseUnit + " unit code mapped", containsUnitCode(unitCodes, testDiseaseUnit));
             Assert.assertTrue("The should be a " + testRenalUnit + " unit code mapped", containsUnitCode(unitCodes, testRenalUnit));
             Assert.assertTrue("The should be a PATIENT unit code mapped", containsUnitCode(unitCodes, "PATIENT"));
 
         } finally {
             // Clean up
             utilityDao.deletePatientViewMapping(patient.getNhsno());
             utilityDao.deletePatientViewUser(patient.getNhsno());
             utilityDao.deletePatient(patient.getNhsno());
             utilityDao.deleteUnit(testRenalUnit);
             utilityDao.deleteUnit(testDiseaseUnit);
         }
 
     }
 
     private boolean containsUnitCode(List<String> unitCodes, String unitCode) {
 
         for (String s : unitCodes) {
             if (s.equalsIgnoreCase(unitCode)) {
                 return true;
             }
         }
         return false;
     }
 
     /**
      * Add a user mapping for Renal Unit A
      * Create patients into Renal Unit A
      * Show that the service call for get Patients as shown on the Radar patient list ExistingPatientsListingPage
      * will only return users with mappings into Unit A
      */
     @Test
     public void testUnitAdminCanSeePatientsInTheirRenalUnit() throws Exception {
         final String testUnit = "testUnit";
         List<Patient> patients = new ArrayList<Patient>();
         try {
             /* -- Create Data for test */
             utilityDao.createUnit(testUnit);
 
             PatientUser userAdmin = roleHelper.createUnitAdmin(StringUtils.right(Long.toString(new Date().getTime()), 10), testUnit);
             // Add 10 patients to the unit
             for (int i = 0; i < 9; i++) {
 
                 Patient patient = new Patient();
                 patient.setUnitcode(testUnit);
                 patient.setForename("Unit");
                 patient.setSurname("Tester");
                 patient.setDateofbirth("21-01-2013");
                 patients.add(patient);
 
                 userManager.registerPatient(patient);
 
             }
 
             // Test Bit
             demographicsManager.getDemographicsByUnitAdmin(userAdmin);
 
         } catch (Exception e) {
             Assert.fail(e.getMessage());
         } finally {
             roleHelper.cleanTestData(patients, testUnit);
         }
     }
 
     /**
      * Add a user mapping for Disease group Alports
      * Create patients into Disease group Alports
      * Show that the service call for get Patients as shown on the Radar patient list ExistingPatientsListingPage
      * will only return users with mappings into Disease group Alports
      */
     @Test
     public void testUnitAdminCanSeePatientsInTheirDiseaseGroup() {
 
     }
 
     /**
      * Add a user mapping for Renal Unit A AND Disease group Alports
      * Create patients into Renal Unit A AND Disease group Alports
      * Show that the service call for get Patients as shown on the Radar patient list ExistingPatientsListingPage
      * will only return users with mappings into Renal Unit A AND Disease group Alports
      */
     @Test
     public void testUnitAdminCanSeePatientsAggregatingRenalUnitAndDiseaseGroup() {
 
     }
 
     @After
     public void  tearDown() throws Exception {
 
        this.emptyDatabase();
 
     }
 
 }
