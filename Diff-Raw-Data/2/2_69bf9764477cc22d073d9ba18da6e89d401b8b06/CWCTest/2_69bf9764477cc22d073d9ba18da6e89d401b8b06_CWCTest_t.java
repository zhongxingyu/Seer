 package org.motechproject.ghana.national.functional.patient;
 
 import org.junit.runner.RunWith;
 import org.motechproject.functional.data.TestPatient;
 import org.motechproject.functional.data.TestStaff;
 import org.motechproject.functional.pages.patient.CWCEnrollmentPage;
 import org.motechproject.functional.pages.patient.PatientPage;
 import org.motechproject.functional.pages.staff.StaffPage;
 import org.motechproject.functional.util.DataGenerator;
 import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.functional.LoggedInUserFunctionalTest;
 import org.motechproject.util.DateUtil;
 import org.openqa.selenium.By;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import static org.testng.Assert.assertTrue;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class CWCTest extends LoggedInUserFunctionalTest {
     private PatientPage patientPage;
     private DataGenerator dataGenerator;
 
     @BeforeMethod
     public void setUp() {
         dataGenerator = new DataGenerator();
     }
 
    @Test
     public void shouldEnrollForCWCForAPatient() {
         String firstName = "First Name" + dataGenerator.randomString(5);
         TestStaff staff = TestStaff.with(firstName);
         StaffPage staffPage = browser.toStaffCreatePage(homePage);
         staffPage.create(staff);
 
         String staffId = staffPage.staffId();
 
         patientPage = browser.toCreatePatient(staffPage);
         createPatient();
         CWCEnrollmentPage cwcEnrollmentPage = browser.toCWCEnrollmentForm(patientPage);
 
         cwcEnrollmentPage.withStaffId(staffId).withRegistrationToday(RegistrationToday.IN_PAST.toString()).withSerialNumber("trew654gf")
                 .withCountry("Ghana").withRegion("Central Region").withDistrict("Awutu Senya").withSubDistrict("Awutu")
                 .withRegistrationDate(DateUtil.newDate(2011, 11, 30)).withAddHistory(false).withFacility("Awutu HC").submit();
         
         assertTrue(cwcEnrollmentPage.getDriver().findElement(By.className("success")).getText().equals("Client registered for CWC successfully."));
     }
 
     private void createPatient() {
         TestPatient patient = TestPatient.with("First Name" + dataGenerator.randomString(5)).
                 registrationMode(TestPatient.PATIENT_REGN_MODE.AUTO_GENERATE_ID).
                 patientType(TestPatient.PATIENT_TYPE.PATIENT_MOTHER).estimatedDateOfBirth(false);
         patientPage.create(patient);
     }
 }
