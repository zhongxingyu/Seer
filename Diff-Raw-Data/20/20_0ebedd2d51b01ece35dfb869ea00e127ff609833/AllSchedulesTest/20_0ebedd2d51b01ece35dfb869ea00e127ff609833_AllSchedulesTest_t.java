 package org.motechproject.ghana.national.repository;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.scheduletracking.api.service.EnrollmentResponse;
 import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
 
 import java.util.Arrays;
 import java.util.List;
 
import static java.util.Arrays.asList;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class AllSchedulesTest {
 
     @Mock
     ScheduleTrackingService mockScheduleTrackingService;
 
     AllSchedules allSchedules;
 
     @Before
     public void setUp() {
         initMocks(this);
         allSchedules = new AllSchedules(mockScheduleTrackingService);
     }
 
     @Test
     public void shouldEnroll() {
         EnrollmentRequest mockEnrollmentRequest = Matchers.any();
         allSchedules.enroll(mockEnrollmentRequest);
         verify(mockScheduleTrackingService).enroll(mockEnrollmentRequest);
     }
 
     @Test
     public void shouldUnEnroll() {
         String patientId = "12";
         String scheduleName = "scheduleName";
 
         allSchedules.unEnroll(patientId, scheduleName);
 
        verify(mockScheduleTrackingService).unenroll(patientId, asList(scheduleName));
     }
 
     @Test
     public void shouldUnEnrollListOfSchedules(){
        String patientId = "12";
         List<String> scheduleNames = Arrays.asList("scheduleName1","scheduleName2","scheduleName3");
 
         allSchedules.unEnroll(patientId, scheduleNames);
 
        verify(mockScheduleTrackingService).unenroll(patientId, scheduleNames);
     }
 
     @Test
     public void shouldEnrollAndFulfilIfNoEnrollmentsWereFoundForTheSchedule() {
         String scheduleName = "scheduleName";
         String externalId = "12";
         EnrollmentRequest anyEnrollmentRequest = new EnrollmentRequest(externalId, scheduleName, new Time(12, 0), new LocalDate(), null, null);
         when(mockScheduleTrackingService.getEnrollment(externalId, scheduleName)).thenReturn(null);
 
         allSchedules.enrollOrFulfill(anyEnrollmentRequest, null);
 
         verify(mockScheduleTrackingService).enroll(anyEnrollmentRequest);
         verify(mockScheduleTrackingService).fulfillCurrentMilestone(externalId, scheduleName, null);
     }
 
     @Test
     public void shouldNotEnrollWhileFulfillingIfEnrollmentsWereFoundForTheSchedule() {
         String scheduleName = "scheduleName";
         String externalId = "12";
         EnrollmentRequest anyEnrollmentRequest = new EnrollmentRequest(externalId, scheduleName, new Time(12, 0), new LocalDate(), null, null);
         EnrollmentResponse notNullEnrollment =mock(EnrollmentResponse.class);
         when(mockScheduleTrackingService.getEnrollment(externalId, scheduleName)).thenReturn(notNullEnrollment);
 
         allSchedules.enrollOrFulfill(anyEnrollmentRequest, null);
 
         verify(mockScheduleTrackingService, never()).enroll(anyEnrollmentRequest);
         verify(mockScheduleTrackingService).fulfillCurrentMilestone(externalId, scheduleName, null);
     }
 
     @Test
     public void shouldEnrollForSchedule() {
         EnrollmentRequest request = new EnrollmentRequest("123", "sche", new Time(12, 0), new LocalDate(), null, null);
         allSchedules.enroll(request);
         verify(mockScheduleTrackingService).enroll(request);
     }
 
     @Test
     public void shouldFulfilMilestoneForSchedule() {
         String externalId = "123";
         String scheduleName = "scheduleName";
         LocalDate fulfillmentDate = new LocalDate();
         allSchedules.fulfilCurrentMilestone(externalId, scheduleName, fulfillmentDate);
         verify(mockScheduleTrackingService).fulfillCurrentMilestone(externalId, scheduleName, fulfillmentDate);
     }
 
     @Test
     public void shouldFetchEnrollmentForSchedule() {
         EnrollmentRequest request = new EnrollmentRequest("123", "scheduleName", new Time(12, 0), new LocalDate(), null, null);
         allSchedules.enrollment(request);
         verify(mockScheduleTrackingService).getEnrollment(request.getExternalId(), request.getScheduleName());
     }
 }
