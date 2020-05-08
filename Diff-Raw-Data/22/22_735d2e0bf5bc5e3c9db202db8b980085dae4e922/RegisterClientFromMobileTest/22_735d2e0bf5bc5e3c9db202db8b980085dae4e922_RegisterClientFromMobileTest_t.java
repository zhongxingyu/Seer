 package org.motechproject.ghana.national.functional.mobile;
 
 import org.apache.commons.collections.MapUtils;
 import org.joda.time.LocalDate;
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.configuration.ScheduleNames;
 import org.motechproject.ghana.national.domain.IPTiDose;
 import org.motechproject.ghana.national.domain.OPVDose;
 import org.motechproject.ghana.national.functional.OpenMRSAwareFunctionalTest;
 import org.motechproject.ghana.national.functional.data.*;
 import org.motechproject.ghana.national.functional.framework.ScheduleTracker;
 import org.motechproject.ghana.national.functional.framework.XformHttpClient;
 import org.motechproject.ghana.national.functional.helper.ScheduleHelper;
 import org.motechproject.ghana.national.functional.mobileforms.MobileForm;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSEncounterPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSPatientPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.vo.OpenMRSObservationVO;
 import org.motechproject.ghana.national.functional.pages.patient.*;
 import org.motechproject.ghana.national.functional.util.DataGenerator;
 import org.motechproject.ghana.national.vo.Pregnancy;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.Test;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import static java.util.Arrays.asList;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.hamcrest.Matchers.hasItem;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.*;
 import static org.motechproject.ghana.national.domain.IPTDose.SP1;
 import static org.motechproject.ghana.national.domain.TTVaccineDosage.TT1;
 import static org.motechproject.util.DateUtil.today;
 import static org.testng.Assert.assertEquals;
 import static org.testng.AssertJUnit.assertNull;
 
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class RegisterClientFromMobileTest extends OpenMRSAwareFunctionalTest {
 
     @Autowired
     private ScheduleTracker scheduleTracker;
     
     DataGenerator dataGenerator;
 
     public RegisterClientFromMobileTest() {
         this.dataGenerator = new DataGenerator();
     }
 
     @Test
     public void shouldCheckForMandatoryFields() throws Exception {
 
         final XformHttpClient.XformResponse xformResponse = mobile.upload(MobileForm.registerClientForm(), MapUtils.EMPTY_MAP);
 
         final List<XformHttpClient.Error> errors = xformResponse.getErrors();
         assertEquals(errors.size(), 1);
         final Map<String, List<String>> errorsMap = errors.iterator().next().getErrors();
 
         assertThat(errorsMap.get("registrationMode"), hasItem("is mandatory"));
         assertThat(errorsMap.get("registrantType"), hasItem("is mandatory"));
         assertThat(errorsMap.get("firstName"), hasItem("is mandatory"));
         assertThat(errorsMap.get("lastName"), hasItem("is mandatory"));
         assertThat(errorsMap.get("dateOfBirth"), hasItem("is mandatory"));
         assertThat(errorsMap.get("date"), hasItem("is mandatory"));
         assertThat(errorsMap.get("estimatedBirthDate"), hasItem("is mandatory"));
         assertThat(errorsMap.get("insured"), hasItem("is mandatory"));
         assertThat(errorsMap.get("date"), hasItem("is mandatory"));
         assertThat(errorsMap.get("address"), hasItem("is mandatory"));
         assertThat(errorsMap.get("facilityId"), hasItem("not found"));
         assertThat(errorsMap.get("staffId"), hasItem("not found"));
     }
 
     @Test
     public void shouldGiveErrorForFirstNameIfNotGiven() throws Exception {
 
         final XformHttpClient.XformResponse xformResponse = mobile.upload(MobileForm.registerClientForm(), new HashMap<String, String>() {{
             put("firstName", "Joe");
         }});
 
         final List<XformHttpClient.Error> errors = xformResponse.getErrors();
         assertEquals(errors.size(), 1);
         final Map<String, List<String>> errorsMap = errors.iterator().next().getErrors();
         assertNull(errorsMap.get("firstName"));
     }
 
     @Test
     public void shouldCreateAPatientWithMobileDeviceAndSearchForHerByName() {
         String staffId = staffGenerator.createStaff(browser, homePage);
 
         TestPatient patient = TestPatient.with("First Name" + dataGenerator.randomString(5), staffId).
                 patientType(TestPatient.PATIENT_TYPE.OTHER).estimatedDateOfBirth(false);
 
         mobile.upload(MobileForm.registerClientForm(), patient.forMobile());
 
         SearchPatientPage searchPatientPage = browser.toSearchPatient();
         searchPatientPage.searchWithName(patient.firstName());
         searchPatientPage.displaying(patient);
         PatientEditPage patientEditPage = browser.toPatientEditPage(searchPatientPage,patient);
 
         String motechId = patientEditPage.motechId();
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(motechId));
         String encounterId = openMRSPatientPage.chooseEncounter("PATIENTREGVISIT");
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
 
         openMRSEncounterPage.displaying(Collections.<OpenMRSObservationVO>emptyList());
     }
 
     @Test
     public void shouldCreatePatientWithANCRegistrationInfoAndSearchForHer() {
         String staffId = staffGenerator.createStaff(browser, homePage);
 
         TestPatient patient = TestPatient.with("Second ANC Name" + dataGenerator.randomString(5), staffId).
                 patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER).estimatedDateOfBirth(false);
 
         TestMobileMidwifeEnrollment mmEnrollmentDetails = TestMobileMidwifeEnrollment.with(staffId, patient.facilityId());
 
         TestANCEnrollment ancEnrollmentDetails = TestANCEnrollment.create();
 
         TestClientRegistration<TestANCEnrollment> testClientRegistration = new TestClientRegistration<TestANCEnrollment>(patient, ancEnrollmentDetails, mmEnrollmentDetails);
 
         mobile.upload(MobileForm.registerClientForm(), testClientRegistration.withProgramEnrollmentThroughMobile());
 
         SearchPatientPage searchPatientPage = browser.toSearchPatient();
         searchPatientPage.searchWithName(patient.firstName());
         searchPatientPage.displaying(patient);
 
         PatientEditPage editPage = browser.toPatientEditPage(searchPatientPage, patient);
         String patientId = editPage.motechId();
 
         ANCEnrollmentPage ancEnrollmentPage = browser.toEnrollANCPage(editPage);
 
             TestANCEnrollment expectedANCEnrollment = testClientRegistration.getEnrollment()
                 .withStaffId(testClientRegistration.getPatient().staffId())
                 .withFacilityId(testClientRegistration.getPatient().facilityId())
                 .withMotechPatientId(patientId)
                 .withRegistrationDate(testClientRegistration.getPatient().getRegistrationDate());
 
         ancEnrollmentPage.displaying(expectedANCEnrollment);
 
         browser.toSearchPatient();
         searchPatientPage.searchWithName(patient.firstName());
         editPage = browser.toPatientEditPage(searchPatientPage, patient);
         MobileMidwifeEnrollmentPage enrollmentPage = browser.toMobileMidwifeEnrollmentForm(editPage);
 
         assertEquals(mmEnrollmentDetails.patientId(patientId), enrollmentPage.details());
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(patientId));
         String encounterId = openMRSPatientPage.chooseEncounter("PATIENTREGVISIT");
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
 
         openMRSEncounterPage.displaying(Collections.<OpenMRSObservationVO>emptyList());
 
         openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(patientId));
         encounterId = openMRSPatientPage.chooseEncounter("PREGREGVISIT");
         openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(asList(
                 new OpenMRSObservationVO("PREGNANCY STATUS", "true"),
                 new OpenMRSObservationVO("ESTIMATED DATE OF CONFINEMENT", "03 February 2012 00:00:00 IST"),
                 new OpenMRSObservationVO("DATE OF CONFINEMENT CONFIRMED","true")
         ));
 
         openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(patientId));
         encounterId = openMRSPatientPage.chooseEncounter("ANCREGVISIT");
 
         openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(asList(
                 new OpenMRSObservationVO("INTERMITTENT PREVENTATIVE TREATMENT DOSE", "1.0"),
                 new OpenMRSObservationVO("TETANUS TOXOID DOSE", "1.0"),
                 new OpenMRSObservationVO("GRAVIDA", "3.0"),
                 new OpenMRSObservationVO("PARITY", "4.0"),
                 new OpenMRSObservationVO("SERIAL NUMBER", "serialNumber"),
                 new OpenMRSObservationVO("HEIGHT (CM)", "124.0")
         ));
     }
     
     @Test
     public void shouldCreatePatientWithANCHistoryAndVerifySchedules() {
 
         LocalDate registrationDate = today();
         LocalDate estimatedDateOfDelivery = registrationDate.plusMonths(4);
         LocalDate lastIPTDate = registrationDate.minusWeeks(6);
         LocalDate lastTTDate = registrationDate.minusWeeks(8);
 
         String staffId = staffGenerator.createStaff(browser, homePage);
 
         TestPatient patient = TestPatient.with("ANC History Name" + dataGenerator.randomString(5), staffId).
                 patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER).estimatedDateOfBirth(false).registrationDate(registrationDate);
 
         TestANCEnrollment ancEnrollmentDetails = TestANCEnrollment.create().withEstimatedDateOfDelivery(estimatedDateOfDelivery)
                                                     .withLastIPT("1").withLastIPTDate(lastIPTDate)
                                                     .withLastTT("1").withLastTTDate(lastTTDate);
         TestMobileMidwifeEnrollment mmEnrollmentDetails = TestMobileMidwifeEnrollment.with(staffId).consent(false);
         TestClientRegistration<TestANCEnrollment> testClientRegistration = new TestClientRegistration<TestANCEnrollment>(patient, ancEnrollmentDetails, mmEnrollmentDetails);
 
         mobile.upload(MobileForm.registerClientForm(), testClientRegistration.withProgramEnrollmentThroughMobile());
 
         SearchPatientPage searchPatientPage = browser.toSearchPatient();
         searchPatientPage.searchWithName(patient.firstName());
         searchPatientPage.displaying(patient);
 
         PatientEditPage editPage = browser.toPatientEditPage(searchPatientPage, patient);
 
         String patientId = editPage.motechId();
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
 
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, ANC_DELIVERY.getName()).getAlertAsLocalDate(),scheduleTracker.firstAlert(ANC_DELIVERY.getName(), Pregnancy.basedOnDeliveryDate(estimatedDateOfDelivery).dateOfConception()));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, ANC_IPT_VACCINE.getName()).getAlertAsLocalDate(),today().plusWeeks(1));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, TT_VACCINATION.getName()).getAlertAsLocalDate(),today().plusWeeks(1));
     }
 
     @Test
     public void shouldCreatePatientWithCWCRegistrationInfoAndSearchForChild() {
         String staffId = staffGenerator.createStaff(browser, homePage);
 
         String motherMotechId = patientGenerator.createPatient(browser, homePage, staffId);
 
         TestPatient patient = TestPatient.with("Second CWC Name" + dataGenerator.randomString(5), staffId).
                 patientType(TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE).estimatedDateOfBirth(false).
                 motherMotechId(motherMotechId).registrationDate(DateUtil.today().plusDays(5)).dateOfBirth(DateUtil.newDate(2010,11,11));
 
         TestMobileMidwifeEnrollment mmEnrollmentDetails = TestMobileMidwifeEnrollment.with(staffId, patient.facilityId());
 
         TestCWCEnrollment cwcEnrollmentDetails = TestCWCEnrollment.create();
 
         TestClientRegistration<TestCWCEnrollment> testClientRegistration = new TestClientRegistration<TestCWCEnrollment>(patient, cwcEnrollmentDetails, mmEnrollmentDetails);
 
         Map<String, String> data = testClientRegistration.withProgramEnrollmentThroughMobile();
         mobile.upload(MobileForm.registerClientForm(), data);
 
         SearchPatientPage searchPatientPage = browser.toSearchPatient();
         searchPatientPage.searchWithName(patient.firstName());
         searchPatientPage.displaying(patient);
 
         TestCWCEnrollment expectedCWCEnrollment = testClientRegistration.getEnrollment()
                 .withStaffId(testClientRegistration.getPatient().staffId())
                 .withFacilityId(testClientRegistration.getPatient().facilityId())
                 .withMotechPatientId(testClientRegistration.getPatient().motechId())
                 .withRegistrationDate(patient.getRegistrationDate());
 
         PatientEditPage editPage = browser.toPatientEditPage(searchPatientPage, patient);
         String patientId = editPage.motechId();
 
         CWCEnrollmentPage cwcEnrollmentPage = browser.toEnrollCWCPage(editPage);
         cwcEnrollmentPage.displaying(expectedCWCEnrollment);
 
         browser.toSearchPatient();
         searchPatientPage.searchWithName(patient.firstName());
         editPage = browser.toPatientEditPage(searchPatientPage, patient);
         MobileMidwifeEnrollmentPage enrollmentPage = browser.toMobileMidwifeEnrollmentForm(editPage);
 
         assertEquals(mmEnrollmentDetails.patientId(patientId), enrollmentPage.details());
 
 
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSDB.getOpenMRSId(patientId));
         String encounterId = openMRSPatientPage.chooseEncounter("CWCREGVISIT");
 
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(asList(
                 new OpenMRSObservationVO("PENTA VACCINATION DOSE","3.0"),
                 new OpenMRSObservationVO("INTERMITTENT PREVENTATIVE TREATMENT INFANTS DOSE","2.0"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED","VITAMIN A"),
                 new OpenMRSObservationVO("SERIAL NUMBER","serialNumber"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED","MEASLES VACCINATION"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED","BACILLE CAMILE-GUERIN VACCINATION"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED","YELLOW FEVER VACCINATION"),
                new OpenMRSObservationVO("IMMUNIZATIONS ORDERED","ROTAVIRUS"),
                 new OpenMRSObservationVO("ORAL POLIO VACCINATION DOSE","1.0")
         ));
     }
     
     @Test
     public void shouldCreatePatientWithCWCPentaAndIPTiHistoryAndVerifySchedules() {
         String staffId = staffGenerator.createStaff(browser, homePage);
 
         String motherMotechId = patientGenerator.createPatient(browser, homePage, staffId);
 
         LocalDate registrationDate = today();
         LocalDate birthDate = registrationDate.minusMonths(2);
 
         TestPatient patient = TestPatient.with("CWC History Name" + dataGenerator.randomString(5), staffId).
                 patientType(TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE).estimatedDateOfBirth(false).
                 motherMotechId(motherMotechId).registrationDate(registrationDate).dateOfBirth(birthDate);
 
         TestMobileMidwifeEnrollment mmEnrollmentDetails = TestMobileMidwifeEnrollment.with(staffId, patient.facilityId()).consent(false);
 
         TestCWCEnrollment cwcEnrollmentDetails = TestCWCEnrollment.create()
                 .withLastPenta("1").withLastPentaDate(birthDate.plusWeeks(4))
                 .withLastIPTi("1").withLastIPTiDate(birthDate.plusWeeks(3));
 
         TestClientRegistration<TestCWCEnrollment> testClientRegistration = new TestClientRegistration<TestCWCEnrollment>(patient, cwcEnrollmentDetails, mmEnrollmentDetails);
 
         Map<String, String> data = testClientRegistration.withProgramEnrollmentThroughMobile();
         mobile.upload(MobileForm.registerClientForm(), data);
 
         SearchPatientPage searchPatientPage = browser.toSearchPatient();
         searchPatientPage.searchWithName(patient.firstName());
         searchPatientPage.displaying(patient);
 
         PatientEditPage editPage = browser.toPatientEditPage(searchPatientPage, patient);
         String openMRSId = openMRSDB.getOpenMRSId(editPage.motechId());
         
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId,CWC_IPT_VACCINE.getName()).getAlertAsLocalDate(),today().plusWeeks(1));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId,CWC_PENTA.getName()).getAlertAsLocalDate(),today().plusWeeks(1));
     }
 
     @Test
     public void shouldCreatePatientWithCWCOPVHistoryAndVerifySchedules() {
         String staffId = staffGenerator.createStaff(browser, homePage);
 
         String motherMotechId = patientGenerator.createPatient(browser, homePage, staffId);
 
         LocalDate registrationDate = today();
         LocalDate birthDate = registrationDate.minusWeeks(5);
 
         TestPatient patient = TestPatient.with("CWC History Name" + dataGenerator.randomString(5), staffId).
                 patientType(TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE).estimatedDateOfBirth(false).
                 motherMotechId(motherMotechId).registrationDate(registrationDate).dateOfBirth(birthDate);
 
         TestMobileMidwifeEnrollment mmEnrollmentDetails = TestMobileMidwifeEnrollment.with(staffId, patient.facilityId()).consent(false);
 
         TestCWCEnrollment cwcEnrollmentDetails = TestCWCEnrollment.create()
                 .withLastOPV("0").withLastOPVDate(birthDate.plusDays(1));
 
         TestClientRegistration<TestCWCEnrollment> testClientRegistration = new TestClientRegistration<TestCWCEnrollment>(patient, cwcEnrollmentDetails, mmEnrollmentDetails);
 
         Map<String, String> data = testClientRegistration.withProgramEnrollmentThroughMobile();
         mobile.upload(MobileForm.registerClientForm(), data);
 
         SearchPatientPage searchPatientPage = browser.toSearchPatient();
         searchPatientPage.searchWithName(patient.firstName());
         searchPatientPage.displaying(patient);
 
         PatientEditPage editPage = browser.toPatientEditPage(searchPatientPage, patient);
         String openMRSId = openMRSDB.getOpenMRSId(editPage.motechId());
 
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId,CWC_OPV_OTHERS.getName()).getAlertAsLocalDate(),scheduleTracker.firstAlert(CWC_OPV_OTHERS.getName(), birthDate));
     }
 
     @Test
     public void shouldCreateSchedulesForPatientTypeOtherWithANC(){
         LocalDate registrationDate = today();
         LocalDate estimatedDateOfDelivery = registrationDate.plusMonths(6);
         LocalDate dateOfConception = Pregnancy.basedOnDeliveryDate(estimatedDateOfDelivery).dateOfConception();
         String staffId = staffGenerator.createStaff(browser, homePage);
         TestPatient testPatient = TestPatient.with("OtherPatient "+dataGenerator.randomString(4),staffId)
                 .registrationDate(registrationDate).patientType(TestPatient.PATIENT_TYPE.OTHER);
         String patientId = patientGenerator.createPatient(testPatient, browser, homePage);
 
         TestANCEnrollment testANCEnrollment = TestANCEnrollment.createWithoutHistory().withStaffId(staffId).withMotechPatientId(patientId)
                 .withRegistrationDate(testPatient.getRegistrationDate())
                 .withEstimatedDateOfDelivery(estimatedDateOfDelivery).withAddHistory(false);
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerANCForm(), testANCEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
 
         assertEquals(1,response.getSuccessCount());
 
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, TT_VACCINATION.getName()).getAlertAsLocalDate(), scheduleTracker.getActiveMilestone(openMRSId, TT_VACCINATION.getName()),
                 scheduleTracker.firstAlert(TT_VACCINATION.getName(),registrationDate), TT1.getScheduleMilestoneName());
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId,ANC_IPT_VACCINE.getName()).getAlertAsLocalDate(), scheduleTracker.getActiveMilestone(openMRSId,ANC_IPT_VACCINE.getName()),
                 scheduleTracker.firstAlert(ANC_IPT_VACCINE.getName(),dateOfConception), SP1.milestone());
     }
 
     @Test
     public void shouldCreateSchedulesForPatientTypeOtherWithCWC(){
         LocalDate registrationDate = today();
         LocalDate dateOfBirth = registrationDate.minusDays(2);
 
         String staffId = staffGenerator.createStaff(browser, homePage);
         TestPatient testPatient = TestPatient.with("OtherPatient "+dataGenerator.randomString(4),staffId)
                 .registrationDate(registrationDate).patientType(TestPatient.PATIENT_TYPE.OTHER).dateOfBirth(dateOfBirth);
         String patientId = patientGenerator.createPatient(testPatient, browser, homePage);
 
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.createWithoutHistory().withStaffId(staffId).withMotechPatientId(patientId)
                 .withRegistrationDate(testPatient.getRegistrationDate());
 
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.registerCWCForm(), testCWCEnrollment.withoutMobileMidwifeEnrollmentThroughMobile());
 
         assertEquals(1,response.getSuccessCount());
 
         String openMRSId = openMRSDB.getOpenMRSId(patientId);
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_OPV_OTHERS.getName()).getAlertAsLocalDate(), scheduleTracker.getActiveMilestone(openMRSId, CWC_OPV_OTHERS.getName()),
                 scheduleTracker.firstAlert(CWC_OPV_OTHERS.getName(), dateOfBirth), OPVDose.OPV_1.milestoneName());
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, ScheduleNames.CWC_IPT_VACCINE.getName()).getAlertAsLocalDate(), scheduleTracker.getActiveMilestone(openMRSId,CWC_IPT_VACCINE.getName()),
                 scheduleTracker.firstAlert(CWC_IPT_VACCINE.getName(),dateOfBirth), IPTiDose.IPTi1.name());
     }
 
 }
