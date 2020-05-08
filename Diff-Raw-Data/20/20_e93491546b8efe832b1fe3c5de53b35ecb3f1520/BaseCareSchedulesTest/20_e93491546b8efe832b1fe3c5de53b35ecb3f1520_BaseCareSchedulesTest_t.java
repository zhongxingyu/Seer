 package org.motechproject.ghana.national.configuration;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
 import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
 import org.motechproject.model.RepeatingSchedulableJob;
 import org.motechproject.scheduler.MotechSchedulerService;
import org.motechproject.scheduletracking.api.domain.Enrollment;
 import org.motechproject.scheduletracking.api.repository.AllEnrollments;
 import org.motechproject.scheduletracking.api.repository.AllTrackedSchedules;
 import org.motechproject.scheduletracking.api.service.impl.EnrollmentAlertService;
 import org.motechproject.scheduletracking.api.service.impl.EnrollmentDefaultmentService;
 import org.motechproject.scheduletracking.api.service.impl.EnrollmentService;
 import org.motechproject.scheduletracking.api.service.impl.ScheduleTrackingServiceImpl;
 import org.motechproject.testing.utils.BaseUnitTest;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.List;
 
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class BaseCareSchedulesTest extends BaseUnitTest {
     @Autowired
     private AllTrackedSchedules allTrackedSchedules;
 
     @Mock
     protected MotechSchedulerService motechSchedulerService;
 
     protected ScheduleTrackingServiceImpl scheduleTrackingService;
 
    private String enrollmentId = "enrollmentId";

     @Before
     public void setUp() {
         initMocks(this);
         AllEnrollments allEnrollments = mock(AllEnrollments.class);
        when(allEnrollments.addOrReplace(Matchers.<Enrollment>any())).thenAnswer(new Answer<Enrollment>() {
            @Override
            public Enrollment answer(InvocationOnMock invocationOnMock) throws Throwable {
                Enrollment enrollment = (Enrollment) invocationOnMock.getArguments()[0];
                enrollment.setId(enrollmentId);
                return enrollment;
            }
        });
         EnrollmentAlertService enrollmentAlertService = new EnrollmentAlertService(allTrackedSchedules, motechSchedulerService);
         EnrollmentDefaultmentService enrollmentDefaultmentService = new EnrollmentDefaultmentService(allTrackedSchedules, motechSchedulerService);
         EnrollmentService enrollmentService = new EnrollmentService(allTrackedSchedules, allEnrollments, enrollmentAlertService, enrollmentDefaultmentService);
         scheduleTrackingService = new ScheduleTrackingServiceImpl(allTrackedSchedules, allEnrollments, enrollmentService);
     }
 
     protected List<RepeatingSchedulableJob> captureSchedules(int count) {
         ArgumentCaptor<RepeatingSchedulableJob> repeatingSchedulableJobArgumentCaptor = ArgumentCaptor.forClass(RepeatingSchedulableJob.class);
         verify(motechSchedulerService, times(count)).safeScheduleRepeatingJob(repeatingSchedulableJobArgumentCaptor.capture());
         return repeatingSchedulableJobArgumentCaptor.getAllValues();
     }
 
     protected void assertIfAScheduleWasCreatedOn(List<RepeatingSchedulableJob> schedules, LocalDate date){
         boolean scheduleAvailable = false;
         for(RepeatingSchedulableJob schedule : schedules){
             if(DateUtil.newDate(schedule.getStartTime()).equals(date)){
                 scheduleAvailable = true;
                 break;
             }
         }
         assertTrue(scheduleAvailable);
     }
 
 
 }
