 package org.motechproject.ghana.national.configuration;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.motechproject.ghana.national.vo.Pregnancy;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.quartz.SchedulerException;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import static org.apache.commons.lang.RandomStringUtils.randomAlphabetic;
 import static org.motechproject.ghana.national.configuration.CareScheduleNames.ANC_IPT_VACCINE;
 import static org.motechproject.ghana.national.vo.Pregnancy.basedOnDeliveryDate;
 import static org.motechproject.util.DateUtil.newDate;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/testApplicationContext-core.xml"})
 public class IPTVaccinationSchedulesTest extends BaseScheduleTrackingTest {
 
     String externalId;
 
     @Before
     public void setUp() {
         super.setUp();
         preferredAlertTime = new Time(10, 10);
         externalId = "patient_id" + randomAlphabetic(6);
     }
 
     @Test
     public void verifyPregnancyIPT1ScheduleForPatientInStartDayOf12thWeekOfPregnancy() throws SchedulerException {
 
         Pregnancy pregnancy = basedOnDeliveryDate(newDate(2012, 9, 22));
         LocalDate dateOfConception = pregnancy.dateOfConception();
 
         mockToday(dateOfConception);
 
         enrollmentId = enrollForIPTVaccine(dateOfConception);
         assertAlerts(captureAlertsForNextMilestone(enrollmentId), new ArrayList<Date>() {{
             add(onDate(new LocalDate(2012, 3, 3)));
             add(onDate(new LocalDate(2012, 3, 10)));
             add(onDate(new LocalDate(2012, 3, 17)));
             add(onDate(new LocalDate(2012, 3, 24)));
         }});
     }
 
     @Test
     public void verifyPregnancyIPTScheduleForPatientRegistrationOnMidOf12thOfPregnancy() throws SchedulerException {
 
         Pregnancy pregnancy = basedOnDeliveryDate(newDate(2012, 9, 22));
         // 12 week start day is 3 Mar - 2012
         LocalDate midOfWeek12_6Mar12 = pregnancy.dateOfConception().plusWeeks(11).plusDays(3);
         mockCurrentDate(midOfWeek12_6Mar12);
 
         enrollmentId = enrollForIPTVaccine(pregnancy.dateOfConception());
         assertAlerts(captureAlertsForNextMilestone(enrollmentId), new ArrayList<Date>() {{
            add(onDate(new LocalDate(2012, 3, 3))); 
             add(onDate(new LocalDate(2012, 3, 10)));
             add(onDate(new LocalDate(2012, 3, 17)));
             add(onDate(new LocalDate(2012, 3, 24)));
         }});
 
         fulfilMilestoneOnVisitDate(new LocalDate(2012, 3, 15));
         assertAlerts(captureAlertsForNextMilestone(enrollmentId), new ArrayList<Date>() {{
             add(onDate(new LocalDate(2012, 4, 5)));
             add(onDate(new LocalDate(2012, 4, 12)));
            add(onDate(new LocalDate(2012, 4, 19)));
            add(onDate(new LocalDate(2012, 4, 26)));
         }});
 
        fulfilMilestoneOnVisitDate(new LocalDate(2012, 5, 7));
         assertAlerts(captureAlertsForNextMilestone(enrollmentId), new ArrayList<Date>() {{
            add(onDate(new LocalDate(2012, 5, 28)));
            add(onDate(new LocalDate(2012, 6, 4)));
            add(onDate(new LocalDate(2012, 6, 11)));
            add(onDate(new LocalDate(2012, 6, 18)));
         }});
     }
 
     private void fulfilMilestoneOnVisitDate(LocalDate visitDate) {
         mockCurrentDate(visitDate);
         scheduleTrackingService.fulfillCurrentMilestone(externalId, ANC_IPT_VACCINE);
     }
 
     private String enrollForIPTVaccine(LocalDate referenceDate) {
         EnrollmentRequest enrollmentRequest = new EnrollmentRequest(externalId, ANC_IPT_VACCINE, preferredAlertTime, referenceDate);
         return scheduleTrackingService.enroll(enrollmentRequest);
     }
 }
