 
 package org.motechproject.ghana.national.functional.patient;
 
 import org.junit.runner.RunWith;
 import org.motechproject.functional.data.TestFacility;
 import org.motechproject.functional.data.TestMobileMidwifeEnrollment;
 import org.motechproject.functional.data.TestPatient;
 import org.motechproject.functional.data.TestStaff;
 import org.motechproject.functional.pages.BasePage;
 import org.motechproject.functional.pages.facility.FacilityPage;
 import org.motechproject.functional.pages.patient.MobileMidwifeEnrollmentPage;
 import org.motechproject.functional.pages.patient.PatientEditPage;
 import org.motechproject.functional.pages.patient.PatientPage;
 import org.motechproject.functional.pages.patient.SearchPatientPage;
 import org.motechproject.functional.pages.staff.StaffPage;
 import org.motechproject.functional.util.DataGenerator;
 import org.motechproject.ghana.national.domain.mobilemidwife.Medium;
 import org.motechproject.ghana.national.domain.mobilemidwife.MessageStartWeek;
 import org.motechproject.ghana.national.domain.mobilemidwife.PhoneOwnership;
 import org.motechproject.ghana.national.domain.mobilemidwife.ServiceType;
 import org.motechproject.ghana.national.functional.LoggedInUserFunctionalTest;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertEquals;
 import static org.testng.Assert.assertFalse;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class MobileMidwifeTest extends LoggedInUserFunctionalTest {
 
     private DataGenerator dataGenerator;
     private TestFacility facility;
     private TestStaff staff;
 
     @org.testng.annotations.BeforeClass
     protected void setupStaff() {
         DataGenerator dataGenerator = new DataGenerator();
         login();
 
         StaffPage staffPage = browser.toStaffCreatePage(homePage);
         this.staff = TestStaff.with("Staff Jence" + dataGenerator.randomString(5));
         TestStaff staff = this.staff;
         staffPage.create(staff);
         staffPage.waitForSuccessMessage();
         staff.motechId(staffPage.staffId());
 
         FacilityPage facilityPage = browser.toFacilityPage(staffPage);
         this.facility = TestFacility.with("Test facility" + dataGenerator.randomString(5));
         TestFacility facility = this.facility;
         facilityPage.save(facility);
         facilityPage.waitForSuccessMessage();
         facility.facilityId(facilityPage.facilityId());
 
         logout();
     }
 
     @BeforeMethod
     public void setUp() {
          dataGenerator = new DataGenerator();
     }
 
     @Test
     public void shouldEnrollAPatientToMobileMidwifeProgramAndEditEnrollmentDetails() {
 
         // create
         String patientFirstName = "Samy Johnson" + new DataGenerator().randomString(5);
         TestPatient patient = TestPatient.with(patientFirstName).registrationMode(TestPatient.PATIENT_REGN_MODE.AUTO_GENERATE_ID);
 
         PatientPage patientPage = browser.toCreatePatient(homePage);
         patientPage.create(patient);
 
 
         TestMobileMidwifeEnrollment enrollmentDetails = TestMobileMidwifeEnrollment.with(staff.motechId(), facility.facilityId());
 
         MobileMidwifeEnrollmentPage enrollmentPage = toMobileMidwifeEnrollmentPage(patient, patientPage);
         enrollmentPage.enroll(enrollmentDetails);
 
         enrollmentPage = toMobileMidwifeEnrollmentPage(patient, enrollmentPage);
         assertEquals(enrollmentDetails, enrollmentPage.details());
 
         // edit
         enrollmentDetails.withServiceType(ServiceType.CHILD_CARE).withMessageStartWeek(new MessageStartWeek("45", "Baby-week 45", ServiceType.CHILD_CARE));
         enrollmentPage.enroll(enrollmentDetails);
 
         enrollmentPage = toMobileMidwifeEnrollmentPage(patient, enrollmentPage);
         assertEquals(enrollmentDetails, enrollmentPage.details());
     }
 
     private MobileMidwifeEnrollmentPage toMobileMidwifeEnrollmentPage(TestPatient patient, BasePage basePage) {
         SearchPatientPage searchPatientPage = browser.toSearchPatient(basePage);
         searchPatientPage.searchWithName(patient.firstName());
         PatientEditPage patientEditPage = browser.toPatientEditPage(searchPatientPage, patient);
         return browser.toMobileMidwifeEnrollmentForm(patientEditPage);
     }
 
     @Test
     public void shouldNotSubmitDayOfWeekAndTimeOfDayForSMSMedium_ForMobileMidwifeEnrollment() {
 
         // create
         String patientFirstName = "Samy Johnson" + new DataGenerator().randomString(5);
         TestPatient patient = TestPatient.with(patientFirstName).registrationMode(TestPatient.PATIENT_REGN_MODE.AUTO_GENERATE_ID);
 
         PatientPage patientPage = browser.toCreatePatient(homePage);
         patientPage.create(patient);
 
        MobileMidwifeEnrollmentPage enrollmentPage = toMobileMidwifeEnrollmentPage(patient, patientPage);;
         enrollmentPage.withConsent(true)
                 .withPhoneOwnership(PhoneOwnership.PERSONAL.toString())
                 .withMedium(Medium.SMS.toString());
 
         assertFalse(enrollmentPage.getDayOfWeek().isDisplayed());
         assertFalse(enrollmentPage.getHourOfDay().isDisplayed());
     }
 }
