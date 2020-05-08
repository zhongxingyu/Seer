 package org.motechproject.ghana.national.functional.mobile;
 
 import org.joda.time.LocalDate;
 import org.junit.runner.RunWith;
 import org.motechproject.appointments.api.EventKeys;
 import org.motechproject.ghana.national.domain.RegistrationToday;
 import org.motechproject.ghana.national.functional.LoggedInUserFunctionalTest;
 import org.motechproject.ghana.national.functional.data.TestANCEnrollment;
 import org.motechproject.ghana.national.functional.data.TestPatient;
 import org.motechproject.ghana.national.functional.framework.XformHttpClient;
 import org.motechproject.ghana.national.functional.mobileforms.MobileForm;
 import org.motechproject.ghana.national.functional.pages.patient.ANCEnrollmentPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientEditPage;
 import org.motechproject.ghana.national.functional.pages.patient.PatientPage;
 import org.motechproject.ghana.national.functional.pages.patient.SearchPatientPage;
 import org.motechproject.ghana.national.functional.util.DataGenerator;
 import org.motechproject.scheduler.MotechSchedulerServiceImpl;
 import org.motechproject.util.DateUtil;
 import org.quartz.CronTrigger;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.Trigger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.testng.annotations.Test;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 import static java.lang.String.format;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.testng.AssertJUnit.assertEquals;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/applicationContext-functional-tests.xml"})
 public class ANCVisitFormUploadTest extends LoggedInUserFunctionalTest {
 
     @Autowired
     private SchedulerFactoryBean schedulerFactoryBean;
 
     @Test
     public void shouldUploadANCVisitFormSuccessfully() throws SchedulerException {
         // create
         final String staffId = staffGenerator.createStaff(browser, homePage);
 
        DataGenerator dataGenerator = new DataGenerator();
         String patientFirstName = "patient first name" + dataGenerator.randomString(5);
         final TestPatient testPatient = TestPatient.with(patientFirstName, staffId)
                 .patientType(TestPatient.PATIENT_TYPE.PREGNANT_MOTHER)
                 .estimatedDateOfBirth(false);
 
         PatientPage patientPage = browser.toCreatePatient(homePage);
         patientPage.create(testPatient);
 
         TestANCEnrollment ancEnrollment = TestANCEnrollment.create().withStaffId(staffId)
                 .withRegistrationToday(RegistrationToday.IN_PAST);
         SearchPatientPage searchPatientPage = browser.toSearchPatient(patientPage);
 
         searchPatientPage.searchWithName(patientFirstName);
         searchPatientPage.displaying(testPatient);
 
         PatientEditPage patientEditPage = browser.toPatientEditPage(searchPatientPage, testPatient);
         final ANCEnrollmentPage ancEnrollmentPage = browser.toEnrollANCPage(patientEditPage);
         ancEnrollmentPage.save(ancEnrollment);
 
         final LocalDate nextANCVisitDate = DateUtil.today().plusDays(5);
         XformHttpClient.XformResponse xformResponse = createAncVisit(staffId, testPatient, ancEnrollmentPage, nextANCVisitDate);
         verifyAncVisitSchedules(ancEnrollmentPage, xformResponse, nextANCVisitDate.minusWeeks(2).toDate(),
                 nextANCVisitDate.minusWeeks(1).toDate(), nextANCVisitDate.toDate(), nextANCVisitDate.plusWeeks(1).toDate());
 
         LocalDate newANCVisitDate = DateUtil.today().plusDays(35);
         xformResponse = createAncVisit(staffId, testPatient, ancEnrollmentPage, newANCVisitDate);
         verifyAncVisitSchedules(ancEnrollmentPage, xformResponse, newANCVisitDate.minusWeeks(2).toDate(),
                 newANCVisitDate.minusWeeks(1).toDate(), newANCVisitDate.toDate(), newANCVisitDate.plusWeeks(1).toDate());
     }
 
     private void verifyAncVisitSchedules(ANCEnrollmentPage ancEnrollmentPage, XformHttpClient.XformResponse xformResponse, Date minDate, Date dueDate, Date lateDate1, Date lateDate2) throws SchedulerException {
         assertEquals(1, xformResponse.getSuccessCount());
         List<CronTrigger> cronTriggers = captureAlertsForNextMilestone(ancEnrollmentPage.getMotechPatientId());
         assertEquals(4, cronTriggers.size());
 
         CronTrigger dueTrigger = cronTriggers.get(0);
         CronTrigger lateTrigger1 = cronTriggers.get(1);
         CronTrigger lateTrigger2 = cronTriggers.get(2);
         CronTrigger lateTrigger3 = cronTriggers.get(3);
 
         assertThat(dueTrigger.getNextFireTime(), is(minDate));
         assertThat(lateTrigger1.getNextFireTime(), is(dueDate));
         assertThat(lateTrigger2.getNextFireTime(), is(lateDate1));
         assertThat(lateTrigger3.getNextFireTime(), is(lateDate2));
 
         assertThat(dueTrigger.getCronExpression(), is("0 0 0 ? * *"));
         assertThat(lateTrigger1.getCronExpression(), is("0 0 0 ? * *"));
         assertThat(lateTrigger2.getCronExpression(), is("0 0 0 ? * *"));
         assertThat(lateTrigger3.getCronExpression(), is("0 0 0 ? * *"));
     }
 
     private XformHttpClient.XformResponse createAncVisit(final String staffId, final TestPatient testPatient, final ANCEnrollmentPage ancEnrollmentPage, final LocalDate nextANCVisitDate) {
         return mobile.upload(MobileForm.ancVisitForm(), new HashMap<String, String>() {{
             put("staffId", staffId);
             put("facilityId", testPatient.facilityId());
             put("motechId", ancEnrollmentPage.getMotechPatientId());
             put("date", "2012-01-03");
             put("serialNumber", "4ds65");
             put("visitNumber", "4");
             put("bpDiastolic", "67");
             put("bpSystolic", "10");
             put("weight", "65.67");
             put("comments", "comments");
             put("ttdose", "4");
             put("iptdose", "2");
             put("iptReactive", "Y");
             put("itnUse", "N");
             put("fht", "4.3");
             put("fhr", "4");
             put("urineTestGlucosePositive", "1");
             put("urineTestProteinPositive", "1");
             put("hemoglobin", "13.8");
             put("vdrlReactive", "N");
             put("vdrlTreatment", "Y");
             put("dewormer", "Y");
             put("pmtct", "Y");
             put("preTestCounseled", "Y");
             put("hivTestResult", "POSITIVE");
             put("postTestCounseled", "Y");
             put("pmtctTreament", "Y");
             put("location", "2");
             put("house", "house");
             put("community", "community");
             put("referred", "Y");
             put("maleInvolved", "N");
             put("nextANCDate", new SimpleDateFormat("yyyy-MM-dd").format(nextANCVisitDate.toDate()));
         }});
     }
 
     protected List<CronTrigger> captureAlertsForNextMilestone(String enrollmentId) throws SchedulerException {
         final Scheduler scheduler = schedulerFactoryBean.getScheduler();
         final String jobGroupName = MotechSchedulerServiceImpl.JOB_GROUP_NAME;
         String[] jobNames = scheduler.getJobNames(jobGroupName);
         List<CronTrigger> alertTriggers = new ArrayList<CronTrigger>();
 
         for (String jobName : jobNames) {
             if (jobName.contains(format("%s-%s", EventKeys.APPOINTMENT_REMINDER_EVENT_SUBJECT, enrollmentId))) {
                 Trigger[] triggersOfJob = scheduler.getTriggersOfJob(jobName, jobGroupName);
                 assertEquals(1, triggersOfJob.length);
                 alertTriggers.add((CronTrigger) triggersOfJob[0]);
             }
         }
         return alertTriggers;
     }
 }
