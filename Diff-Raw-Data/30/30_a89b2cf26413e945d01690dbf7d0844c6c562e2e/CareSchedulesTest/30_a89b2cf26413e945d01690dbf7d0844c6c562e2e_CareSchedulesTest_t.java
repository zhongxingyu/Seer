 package org.motechproject.ghana.national.configuration;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.vo.Pregnancy;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.quartz.SchedulerException;
 import org.quartz.SimpleTrigger;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static org.motechproject.util.DateUtil.newDate;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/testApplicationContext-core.xml"})
 public class CareSchedulesTest extends BaseScheduleTrackingTest {
     @Before
     public void setUp() {
         super.setUp();
         preferredAlertTime = new Time(10, 10);
     }
 
     @Test
     public void verifyPregnancyScheduleWhenEDDIsVeryFarInFuture() throws SchedulerException {
         mockToday(newDate(2012, 2, 1));
 
         LocalDate expectedDeliveryDate = newDate(2012, 11, 10);
 
         enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);
 
         List<SimpleTrigger> alerts = captureAlertsForNextMilestone(enrollmentId);
         assertAlerts(alerts, new ArrayList<Date>() {{
             add(onDate(newDate(2012, 11, 3)));
             add(onDate(newDate(2012, 11, 10)));
             add(onDate(newDate(2012, 11, 17)));
             add(onDate(newDate(2012, 11, 24)));
         }});
     }
 
     @Test
     @Ignore("Waiting for bug fix from platform")
     public void verifyPregnancyScheduleWhenEDDIsUnderOneWeekFromToday() throws SchedulerException {
         mockToday(newDate(2012, 2, 1));
         LocalDate expectedDeliveryDate = newDate(2012, 2, 3);
         enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);
 
         List<SimpleTrigger> alerts = captureAlertsForNextMilestone(enrollmentId);
         assertAlerts(alerts, new ArrayList<Date>() {{
             add(onDate(newDate(2012, 2, 3)));
             add(onDate(newDate(2012, 2, 10)));
             add(onDate(newDate(2012, 2, 17)));
         }});
     }
 
     @Test
     @Ignore("Waiting for bug fix from platform")
     public void verifyPregnancyScheduleWhenEDDIsInThePast() throws SchedulerException {
         mockToday(newDate(2012, 2, 1));
         LocalDate expectedDeliveryDate = newDate(2012, 1, 23);
         enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);
 
         List<SimpleTrigger> alerts = captureAlertsForNextMilestone(enrollmentId);
         assertAlerts(alerts, new ArrayList<Date>() {{
             add(onDate(newDate(2012, 2, 6)));
         }});
     }
 
     @Test
     public void verifyPregnancyScheduleWhenEDDIsInOverOneWeekFromToday() throws SchedulerException {
         mockToday(newDate(2012, 2, 1));
         LocalDate expectedDeliveryDate = newDate(2012, 2, 11);
         enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);
 
         List<SimpleTrigger> alerts = captureAlertsForNextMilestone(enrollmentId);
         assertAlerts(alerts, new ArrayList<Date>() {{
             add(onDate(newDate(2012, 2, 4)));
             add(onDate(newDate(2012, 2, 11)));
             add(onDate(newDate(2012, 2, 18)));
             add(onDate(newDate(2012, 2, 25)));
         }});
     }
 
     @Test
     public void verifyPregnancyScheduleWhenEDDIsOnToday() throws SchedulerException {
         mockToday(newDate(2012, 2, 1));
         LocalDate expectedDeliveryDate = newDate(2012, 2, 1);
         enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);
 
         List<SimpleTrigger> alerts = captureAlertsForNextMilestone(enrollmentId);
         assertAlerts(alerts, new ArrayList<Date>() {{
             add(onDate(newDate(2012, 2, 1)));
             add(onDate(newDate(2012, 2, 8)));
             add(onDate(newDate(2012, 2, 15)));
         }});
     }
 
     @Test
     @Ignore("Waiting for bug fix from platform")
     public void verifyPregnancyScheduleWhenEDDIsInTheFarPast() throws SchedulerException {
         mockToday(newDate(2012, 2, 1));
         LocalDate expectedDeliveryDate = newDate(2012, 2, 4);
         enrollmentId = scheduleAlertForDeliveryNotfication(expectedDeliveryDate);
 
         List<SimpleTrigger> alerts = captureAlertsForNextMilestone(enrollmentId);
         assertAlerts(alerts, new ArrayList<Date>());
     }
 
     private String scheduleAlertForDeliveryNotfication(LocalDate expectedDeliveryDate) {
         final LocalDate conceptionDate = Pregnancy.basedOnDeliveryDate(expectedDeliveryDate).dateOfConception();
         EnrollmentRequest enrollmentRequest = new EnrollmentRequest("123", CareScheduleNames.DELIVERY, preferredAlertTime, conceptionDate);
         return scheduleTrackingService.enroll(enrollmentRequest);
     }
 
 }
