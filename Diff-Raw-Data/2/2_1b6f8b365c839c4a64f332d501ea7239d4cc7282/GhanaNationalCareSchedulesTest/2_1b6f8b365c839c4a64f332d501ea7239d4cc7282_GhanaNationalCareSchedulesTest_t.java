 package org.motechproject.ghana.national.configuration;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.motechproject.model.RepeatingSchedulableJob;
 import org.motechproject.model.Time;
 import org.motechproject.scheduler.MotechSchedulerService;
 import org.motechproject.scheduletracking.api.repository.AllEnrollments;
 import org.motechproject.scheduletracking.api.repository.AllTrackedSchedules;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.scheduletracking.api.service.EnrollmentService;
 import org.motechproject.scheduletracking.api.service.ScheduleTrackingServiceImpl;
 import org.motechproject.testing.utils.BaseUnitTest;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static org.mockito.Mockito.*;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(locations = {"classpath:/testApplicationContext-core.xml"})
 public class GhanaNationalCareSchedulesTest extends BaseUnitTest {
     @Autowired
     AllTrackedSchedules allTrackedSchedules;
     private Time preferredAlertTime;
 
     @Before
     public void setUp() {
         preferredAlertTime = new Time(10, 10);
     }
 
     @Test
     public void verifyPregnancySchedule() {
         LocalDate today = DateUtil.newDate(2000, 1, 1);
         mockCurrentDate(today);
         LocalDate conceptionDate = today.minusWeeks(1);
 
         AllEnrollments allEnrollments = mock(AllEnrollments.class);
         MotechSchedulerService motechSchedulerService = mock(MotechSchedulerService.class);
        EnrollmentService enrollmentService = new EnrollmentService(allTrackedSchedules, allEnrollments, null, null);
         ScheduleTrackingServiceImpl scheduleTrackingService = new ScheduleTrackingServiceImpl(allTrackedSchedules, allEnrollments, enrollmentService);
         EnrollmentRequest enrollmentRequest = new EnrollmentRequest("123", CareScheduleNames.DELIVERY, preferredAlertTime, conceptionDate);
         scheduleTrackingService.enroll(enrollmentRequest);
 
         ArgumentCaptor<RepeatingSchedulableJob> repeatingSchedulableJobArgumentCaptor = ArgumentCaptor.forClass(RepeatingSchedulableJob.class);
         int numberOfScheduledJobs = 2;
         verify(motechSchedulerService, times(numberOfScheduledJobs)).safeScheduleRepeatingJob(repeatingSchedulableJobArgumentCaptor.capture());
         List<RepeatingSchedulableJob> schedulableJobs = repeatingSchedulableJobArgumentCaptor.getAllValues();
         assertEquals(numberOfScheduledJobs, schedulableJobs.size());
         assertEquals(DateUtil.newDate(schedulableJobs.get(0).getStartTime()), onDate(conceptionDate, 39));
         assertEquals(DateUtil.newDate(schedulableJobs.get(1).getStartTime()), onDate(conceptionDate, 40));
     }
 
     private LocalDate onDate(LocalDate conceptionDate, int numberOfWeeks) {
         return conceptionDate.plusWeeks(numberOfWeeks);
     }
 }
