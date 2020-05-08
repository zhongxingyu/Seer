 package org.motechproject.ghana.national.functional.mobile;
 
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.domain.*;
 import org.motechproject.ghana.national.functional.OpenMRSAwareFunctionalTest;
 import org.motechproject.ghana.national.functional.data.TestCWCEnrollment;
 import org.motechproject.ghana.national.functional.data.TestPatient;
 import org.motechproject.ghana.national.functional.framework.OpenMRSDB;
 import org.motechproject.ghana.national.functional.framework.ScheduleTracker;
 import org.motechproject.ghana.national.functional.framework.XformHttpClient;
 import org.motechproject.ghana.national.functional.helper.ScheduleHelper;
 import org.motechproject.ghana.national.functional.mobileforms.MobileForm;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSEncounterPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.OpenMRSPatientPage;
 import org.motechproject.ghana.national.functional.pages.openmrs.vo.OpenMRSObservationVO;
 import org.motechproject.ghana.national.functional.pages.patient.CWCEnrollmentPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientEditPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientPage;
 import org.motechproject.ghana.national.functional.pages.patient.SearchPatientPage;
 import org.motechproject.ghana.national.functional.util.DataGenerator;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.BeforeMethod;
 import org.testng.annotations.Test;
 
 import java.util.HashMap;
 
 import static java.util.Arrays.asList;
 import static junit.framework.Assert.assertEquals;
 import static org.joda.time.format.DateTimeFormat.forPattern;
 import static org.motechproject.ghana.national.configuration.ScheduleNames.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class CWCVisitFormUploadTest extends OpenMRSAwareFunctionalTest {
 
     private DataGenerator dataGenerator;
     @Autowired
     ScheduleTracker scheduleTracker;
 
     @Autowired
     private OpenMRSDB openMRSDB;
 
     @BeforeMethod
     public void setUp() {
         dataGenerator = new DataGenerator();
     }
 
     @Test
     public void shouldCreateCWCVisit() {
         final String staffId = staffGenerator.createStaff(browser, homePage);
         final String facilityId = facilityGenerator.createFacility(browser, homePage);
         String patientFirstName = "First Name" + dataGenerator.randomString(5);
         final PatientPage patientPage = browser.toCreatePatient(homePage);
         final DateTime visitDate = DateUtil.now();
         final TestPatient patient = TestPatient.with(patientFirstName, staffId)
                 .patientType(TestPatient.PATIENT_TYPE.CHILD_UNDER_FIVE)
                 .estimatedDateOfBirth(false)
                 .dateOfBirth(visitDate.minusYears(2).toLocalDate());
         patientPage.create(patient);
         SearchPatientPage searchPatientPage = browser.toSearchPatient(homePage);
         searchPatientPage.searchWithName(patient.firstName());
         PatientEditPage patientEditPage = browser.toPatientEditPage(searchPatientPage, patient);
         LocalDate lastOPVDate = DateUtil.today().minusWeeks(2);
         TestCWCEnrollment testCWCEnrollment = TestCWCEnrollment.create().withStaffId(staffId)
                 .withMotechPatientId(patientEditPage.motechId())
                 .withAddCareHistory(asList(CwcCareHistory.YF, CwcCareHistory.BCG, CwcCareHistory.OPV))
                 .withLastYellowFeverDate(visitDate.toLocalDate())
                 .withLastBcgDate(visitDate.toLocalDate())
                 .withLastOPV("1")
                 .withLastOPVDate(lastOPVDate);
         final CWCEnrollmentPage cwcEnrollmentPage = browser.toEnrollCWCPage(patientEditPage);
         cwcEnrollmentPage.save(testCWCEnrollment);
 
         final String motechId = cwcEnrollmentPage.getPatientMotechId();
         XformHttpClient.XformResponse response = mobile.upload(MobileForm.cwcVisitForm(), new HashMap<String, String>() {{
             put("staffId", staffId);
             put("facilityId", facilityId);
             put("date", visitDate.toString(forPattern("yyyy-MM-dd")));
             put("motechId", motechId);
             put("serialNumber", "1234567");
             put("immunizations", "BCG,OPV,YF,DEWORMER,ROTAVIRUS,PNEUMOCOCCAL");
             put("opvdose", "2");
             put("pentadose", "1");
             put("iptidose", "1");
             put("rotavirusdose", "1");
             put("pneumococcaldose", "1");
             put("weight", "23");
             put("muac", "12");
             put("height", "13");
             put("maleInvolved", "Y");
             put("cwcLocation", "2");
             put("house", "32");
             put("community", "Home");
             put("comments", "Unknwon");
         }});
 
         assertEquals(0, response.getErrors().size());
 
         String openMRSId = openMRSDB.getOpenMRSId(motechId);
         OpenMRSPatientPage openMRSPatientPage = openMRSBrowser.toOpenMRSPatientPage(openMRSId);
         String encounterId = openMRSPatientPage.chooseEncounter("CWCVISIT");
 
         OpenMRSEncounterPage openMRSEncounterPage = openMRSBrowser.toOpenMRSEncounterPage(encounterId);
         openMRSEncounterPage.displaying(asList(
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "BACILLE CAMILE-GUERIN VACCINATION"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "YELLOW FEVER VACCINATION"),
                 new OpenMRSObservationVO("IMMUNIZATIONS ORDERED", "DEWORMER"),
                 new OpenMRSObservationVO("HOUSE", "32"),
                 new OpenMRSObservationVO("CWC LOCATION", "2.0"),
                 new OpenMRSObservationVO("WEIGHT (KG)", "23.0"),
                 new OpenMRSObservationVO("COMMENTS", "Unknwon"),
                 new OpenMRSObservationVO("COMMUNITY", "Home"),
                 new OpenMRSObservationVO("HEIGHT (CM)", "13.0"),
                 new OpenMRSObservationVO("INTERMITTENT PREVENTATIVE TREATMENT INFANTS DOSE", "1.0"),
                 new OpenMRSObservationVO("PENTA VACCINATION DOSE", "1.0"),
                 new OpenMRSObservationVO("ROTAVIRUS", "1.0"),
                 new OpenMRSObservationVO("PNEUMOCOCCAL", "1.0"),
                 new OpenMRSObservationVO("MID-UPPER ARM CIRCUMFERENCE", "12.0"),
                 new OpenMRSObservationVO("ORAL POLIO VACCINATION DOSE", "2.0"),
                 new OpenMRSObservationVO("SERIAL NUMBER", "1234567"),
                 new OpenMRSObservationVO("MALE INVOLVEMENT", "true")
         ));
 
        ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_OPV_OTHERS.getName()).getAlertAsLocalDate(), scheduleTracker.firstAlert(CWC_OPV_OTHERS.getName(), lastOPVDate, OPVDose.OPV_2.milestoneName()));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PENTA.getName()).getAlertAsLocalDate(), scheduleTracker.firstAlert(CWC_PENTA.getName(), visitDate.toLocalDate(), PentaDose.PENTA2.milestoneName()));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_ROTAVIRUS.getName()).getAlertAsLocalDate(), scheduleTracker.firstAlert(CWC_ROTAVIRUS.getName(), visitDate.toLocalDate(), RotavirusDose.ROTAVIRUS2.milestoneName()));
         ScheduleHelper.assertAlertDate(scheduleTracker.firstAlertScheduledFor(openMRSId, CWC_PNEUMOCOCCAL.getName()).getAlertAsLocalDate(), scheduleTracker.firstAlert(CWC_PNEUMOCOCCAL.getName(), visitDate.toLocalDate(), PneumococcalDose.PNEUMO2.milestoneName()));
     }
 }
