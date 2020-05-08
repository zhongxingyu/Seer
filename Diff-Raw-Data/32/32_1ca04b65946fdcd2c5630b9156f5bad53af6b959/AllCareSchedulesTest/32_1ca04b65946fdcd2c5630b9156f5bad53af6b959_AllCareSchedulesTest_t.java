 package org.motechproject.ghana.national.repository;
 
 import org.joda.time.LocalDate;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.domain.exception.InvalidEnrollmentException;
 import org.motechproject.scheduletracking.api.service.*;
 
 import java.util.Arrays;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.equalTo;
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class AllCareSchedulesTest {
 
     @Mock
     ScheduleTrackingService mockScheduleTrackingService;
 
     AllCareSchedules allCareSchedules;
 
     @Before
     public void setUp() {
         initMocks(this);
         allCareSchedules = new AllCareSchedules(mockScheduleTrackingService);
     }
 
     @Test
     public void shouldEnroll() {
         EnrollmentRequest mockEnrollmentRequest = Matchers.any();
         allCareSchedules.enroll(mockEnrollmentRequest);
         verify(mockScheduleTrackingService).enroll(mockEnrollmentRequest);
     }
 
     @Test
     public void shouldUnEnrollListOfSchedules() {
         String patientId = "12";
         List<String> scheduleNames = Arrays.asList("scheduleName1", "scheduleName2", "scheduleName3");
 
         allCareSchedules.unEnroll(patientId, scheduleNames);
 
         verify(mockScheduleTrackingService).unenroll(patientId, scheduleNames);
     }
 
     @Test
     public void shouldEnrollAndFulfilIfNoEnrollmentsWereFoundForTheSchedule() {
         String scheduleName = "scheduleName";
         String externalId = "12";
         EnrollmentRequest anyEnrollmentRequest = new EnrollmentRequest(externalId, scheduleName, new Time(12, 0), new LocalDate(), null, null, null, null, null);
        EnrollmentRecord newEnrollment = mock(EnrollmentRecord.class);
        when(mockScheduleTrackingService.getEnrollment(externalId, scheduleName)).thenReturn(null,newEnrollment);
 
         allCareSchedules.enrollOrFulfill(anyEnrollmentRequest, null);
 
         verify(mockScheduleTrackingService).enroll(anyEnrollmentRequest);
         verify(mockScheduleTrackingService).fulfillCurrentMilestone(externalId, scheduleName, null);
     }
 
     @Test
     public void shouldNotEnrollWhileFulfillingIfEnrollmentsWereFoundForTheSchedule() {
         String scheduleName = "scheduleName";
         String externalId = "12";
         EnrollmentRequest anyEnrollmentRequest = new EnrollmentRequest(externalId, scheduleName, new Time(12, 0), new LocalDate(), null, null, null, null, null);
         EnrollmentRecord notNullEnrollment = mock(EnrollmentRecord.class);
         when(mockScheduleTrackingService.getEnrollment(externalId, scheduleName)).thenReturn(notNullEnrollment);
 
         allCareSchedules.enrollOrFulfill(anyEnrollmentRequest, null);
 
         verify(mockScheduleTrackingService, never()).enroll(anyEnrollmentRequest);
         verify(mockScheduleTrackingService).fulfillCurrentMilestone(externalId, scheduleName, null);
     }
 
     @Test
    public void shouldNotFulfillAScheduleIfThereIsNoActiveEnrollment() {

        String scheduleName = "scheduleName";
        String externalId = "12";
        EnrollmentRequest anyEnrollmentRequest = new EnrollmentRequest(externalId, scheduleName, new Time(12, 0), new LocalDate(), null, null, null, null, null);
        when(mockScheduleTrackingService.getEnrollment(externalId, scheduleName)).thenReturn(null);

        allCareSchedules.enrollOrFulfill(anyEnrollmentRequest, null);

        verify(mockScheduleTrackingService, times(1)).enroll(anyEnrollmentRequest);
        verify(mockScheduleTrackingService,never()).fulfillCurrentMilestone(externalId, scheduleName, null);
    }

    @Test
     public void shouldEnrollForSchedule() {
         EnrollmentRequest request = new EnrollmentRequest("123", "sche", new Time(12, 0), new LocalDate(), null, null, null, null, null);
         allCareSchedules.enroll(request);
         verify(mockScheduleTrackingService).enroll(request);
     }
 
     @Test
     public void shouldFulfilMilestoneForSchedule() {
         String externalId = "123";
         String scheduleName = "scheduleName";
         LocalDate fulfillmentDate = new LocalDate();
 
         allCareSchedules.fulfilCurrentMilestone(externalId, scheduleName, fulfillmentDate);
         verify(mockScheduleTrackingService).fulfillCurrentMilestone(externalId, scheduleName, fulfillmentDate);
 
         reset(mockScheduleTrackingService);
 
         Time fulfillmentTime = new Time(2, 3);
         allCareSchedules.fulfilCurrentMilestone(externalId, scheduleName, fulfillmentDate, fulfillmentTime);
         verify(mockScheduleTrackingService).fulfillCurrentMilestone(externalId, scheduleName, fulfillmentDate, fulfillmentTime);
     }
 
     @Test
     public void shouldFetchEnrollmentForSchedule() {
         EnrollmentRequest request = new EnrollmentRequest("123", "scheduleName", new Time(12, 0), new LocalDate(), null, null, null, null, null);
         allCareSchedules.enrollment(request);
         verify(mockScheduleTrackingService).getEnrollment(request.getExternalId(), request.getScheduleName());
     }
 
     @Test
     public void shouldFetchEnrollmentAlongWithAllStartWindowDateInfo() {
         EnrollmentsQuery enrollmentsQuery = mock(EnrollmentsQuery.class);
         allCareSchedules.search(enrollmentsQuery);
         verify(mockScheduleTrackingService).searchWithWindowDates(enrollmentsQuery);
     }
 
     @Test
     public void shouldFulfilCurrentMilestoneSafely() {
         doNothing().when(mockScheduleTrackingService).fulfillCurrentMilestone(Matchers.<String>any(), Matchers.<String>any(), Matchers.<LocalDate>any());
         assertTrue(allCareSchedules.safeFulfilCurrentMilestone("id", "some name", null));
 
         reset(mockScheduleTrackingService);
 
         doThrow(new InvalidEnrollmentException("not exists")).when(mockScheduleTrackingService).fulfillCurrentMilestone(Matchers.<String>any(), Matchers.<String>any(), Matchers.<LocalDate>any());
         assertFalse(allCareSchedules.safeFulfilCurrentMilestone("id", "some name", null));
     }
 
     @Test
     public void shouldReturnActiveEnrollmentToAScheduleGivenAnEnrollmentIdAndScheduleName(){
         String externalId = "external id";
         String scheduleName = "schedule name";
         EnrollmentRecord enrollmentRecord = mock(EnrollmentRecord.class);
 
         when(mockScheduleTrackingService.getEnrollment(externalId, scheduleName)).thenReturn(enrollmentRecord);
         assertThat(enrollmentRecord, is(equalTo(allCareSchedules.getActiveEnrollment(externalId, scheduleName))));
     }
 
     @Test
     public void shouldReturnDueAlertsGivenAnEnrollmentRequest() {
         EnrollmentRequest request = new EnrollmentRequest("123", "scheduleName", new Time(12, 0), new LocalDate(), null, null, null, null, null);
         MilestoneAlerts mockMilestoneAlerts = mock(MilestoneAlerts.class);
         when(mockScheduleTrackingService.getAlertTimings(request)).thenReturn(mockMilestoneAlerts);
         allCareSchedules.getDueWindowAlertTimings(request);
         verify(mockMilestoneAlerts).getDueWindowAlertTimings();
     }
 }
