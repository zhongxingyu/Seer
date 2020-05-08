 package org.motechproject.ghana.national.configuration;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.util.DateUtil;
 import org.quartz.SchedulerException;
 import org.quartz.SimpleTrigger;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static org.motechproject.ghana.national.configuration.CareScheduleNames.TTVaccine;
 import static org.motechproject.util.DateUtil.newDate;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/testApplicationContext-core.xml"})
 public class TTVaccinationSchedulesTest extends BaseScheduleTrackingTest {
     private static final String PATIENT_ID = "Patient id";
 
     @Before
     public void setUp() {
         super.setUp();
         preferredAlertTime = new Time(10, 10);
     }
 
     @Test
     public void verifyTTVaccinationScheduleFulfillmentHappensAfterTheLastAlert() throws SchedulerException {
         LocalDate firstDosageDate = mockToday(DateUtil.newDate(2000, 2, 1));
 
         enrollmentId = scheduleAlertForTTVaccination(firstDosageDate);
         fulfillCurrentMilestone(firstDosageDate);
 
         List<SimpleTrigger> tt2Alerts = captureAlertsForNextMilestone(enrollmentId);
         assertAlerts(tt2Alerts, new ArrayList<Date>() {{
             add(onDate(newDate(2000, 2, 22)));
             add(onDate(newDate(2000, 2, 29)));
             add(onDate(newDate(2000, 3, 7)));
             add(onDate(newDate(2000, 3, 14)));
         }});
 
         fulfillCurrentMilestone(newDate(2000, 3, 22));
 
         List<SimpleTrigger> tt3Alerts = captureAlertsForNextMilestone(enrollmentId);
         assertAlerts(tt3Alerts, new ArrayList<Date>() {{
             add(onDate(newDate(2000, 8, 30)));
            add(onDate(newDate(2000, 9, 9)));
             add(onDate(newDate(2000, 9, 16)));
             add(onDate(newDate(2000, 9, 23)));
         }});
 
         fulfillCurrentMilestone(newDate(2000, 10, 18));
 
         List<SimpleTrigger> tt4Alerts = captureAlertsForNextMilestone(enrollmentId);
         assertAlerts(tt4Alerts, new ArrayList<Date>() {{
             add(onDate(newDate(2001, 9, 12)));
            add(onDate(newDate(2001, 9, 22)));
             add(onDate(newDate(2001, 9, 29)));
             add(onDate(newDate(2001, 10, 6)));
         }});
 
         fulfillCurrentMilestone(newDate(2001, 10, 15));
 
         List<SimpleTrigger> tt5Alerts = captureAlertsForNextMilestone(enrollmentId);
         assertAlerts(tt5Alerts, new ArrayList<Date>() {{
             add(onDate(newDate(2002, 9, 9)));
            add(onDate(newDate(2002, 9, 19)));
             add(onDate(newDate(2002, 9, 26)));
             add(onDate(newDate(2002, 10, 3)));
         }});
     }
 
     private void fulfillCurrentMilestone(LocalDate date) {
         mockToday(date);
         scheduleTrackingService.fulfillCurrentMilestone(PATIENT_ID, TTVaccine);
     }
 
 
     private String scheduleAlertForTTVaccination(LocalDate firstDosageDate) {
         EnrollmentRequest enrollmentRequest = new EnrollmentRequest(PATIENT_ID, TTVaccine, preferredAlertTime, firstDosageDate);
         return scheduleTrackingService.enroll(enrollmentRequest);
     }
 }
