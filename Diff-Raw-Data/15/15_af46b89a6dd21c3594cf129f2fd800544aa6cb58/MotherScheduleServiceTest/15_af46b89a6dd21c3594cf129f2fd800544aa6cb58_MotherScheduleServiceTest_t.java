 package org.who.mcheck.integration;
 
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentMatcher;
 import org.mockito.Mock;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
 import org.who.mcheck.core.service.MotherScheduleService;
 import org.who.mcheck.core.service.PreferredCallTimeService;
 import org.who.mcheck.core.util.DateUtil;
 import org.who.mcheck.core.util.LocalTimeUtil;
 
 import static org.joda.time.LocalDate.parse;
 import static org.mockito.Matchers.argThat;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class MotherScheduleServiceTest {
     @Mock
     private ScheduleTrackingService scheduleTrackingService;
     @Mock
     private PreferredCallTimeService preferredCallTimeService;
 
     private MotherScheduleService service;
 
     @Before
     public void setUp() throws Exception {
         initMocks(this);
         service = new MotherScheduleService(scheduleTrackingService, "5", preferredCallTimeService);
     }
 
     @Test
     public void shouldEnrollMotherWithFirstScheduleAsDay1ScheduleWhenRegistrationDateAndDeliveryDateAreSame() throws Exception {
         DateUtil.fakeIt(LocalDate.parse("2013-01-01"));
         LocalTimeUtil.fakeIt(new LocalTime(9, 0));
         when(preferredCallTimeService.getPreferredCallTime("morning")).thenReturn(LocalTime.parse("09:30:00"));
 
         service.enroll("id", parse("2013-01-01"), parse("2013-01-01"), "morning");
 
         verify(scheduleTrackingService)
                 .enroll(enrollmentFor("id", "Post Delivery Danger Signs - Day 1", parse("2013-01-01"), new Time(9, 5)));
     }
 
     @Test
     public void shouldEnrollMotherWithFirstScheduleAsDay1ScheduleWhenRegistrationDateIsOneDayAfterDeliveryDate() throws Exception {
         DateUtil.fakeIt(LocalDate.parse("2013-01-02"));
         LocalTimeUtil.fakeIt(new LocalTime(9, 0));
         when(preferredCallTimeService.getPreferredCallTime("morning")).thenReturn(LocalTime.parse("09:30:00"));
 
         service.enroll("id", parse("2013-01-02"), parse("2013-01-01"), "morning");
 
         verify(scheduleTrackingService)
                 .enroll(enrollmentFor("id", "Post Delivery Danger Signs - Day 1", parse("2013-01-02"), new Time(9, 5)));
     }
 
     @Test
     public void shouldEnrollMotherWithFirstScheduleAsDay3ScheduleWhenRegistrationDateIsTwoDaysAfterDeliveryDate() throws Exception {
         DateUtil.fakeIt(LocalDate.parse("2013-01-03"));
         LocalTimeUtil.fakeIt(new LocalTime(9, 0));
         when(preferredCallTimeService.getPreferredCallTime("morning")).thenReturn(LocalTime.parse("09:30:00"));
 
         service.enroll("id", parse("2013-01-03"), parse("2013-01-01"), "morning");
 
         verify(scheduleTrackingService)
                 .enroll(enrollmentFor("id", "Post Delivery Danger Signs - Day 3", parse("2013-01-03"), new Time(9, 5)));
     }
 
     @Test
     public void shouldEnrollMotherWithFirstScheduleAsDay4ScheduleWhenRegistrationDateIsThreeDaysAfterDeliveryDate() throws Exception {
         DateUtil.fakeIt(LocalDate.parse("2013-01-04"));
         LocalTimeUtil.fakeIt(new LocalTime(9, 0));
         when(preferredCallTimeService.getPreferredCallTime("morning")).thenReturn(LocalTime.parse("09:30:00"));
 
         service.enroll("id", parse("2013-01-04"), parse("2013-01-01"), "morning");
 
         verify(scheduleTrackingService)
                 .enroll(enrollmentFor("id", "Post Delivery Danger Signs - Day 4", parse("2013-01-04"), new Time(9, 5)));
     }
 
     @Test
     public void shouldEnrollMotherToDay2ScheduleOnTheNextDayOfRegistrationWhenRegistrationDateAndDeliveryDateAreSame() throws Exception {
        DateUtil.fakeIt(LocalDate.parse("2013-01-01"));
         LocalTimeUtil.fakeIt(new LocalTime(9, 0));
         when(preferredCallTimeService.getPreferredCallTime("morning")).thenReturn(LocalTime.parse("09:30:00"));
 
         service.enroll("id", parse("2013-01-01"), parse("2013-01-01"), "morning");
 
         verify(scheduleTrackingService)
                 .enroll(enrollmentFor("id", "Post Delivery Danger Signs - Day 2", parse("2013-01-02"), new Time(9, 30)));
     }
 
     @Test
     public void shouldEnrollMotherToDay2ScheduleOnTheNextDayOfRegistrationWhenRegistrationDateIsOneDayAfterDeliveryDate() throws Exception {
        DateUtil.fakeIt(LocalDate.parse("2013-01-02"));
         LocalTimeUtil.fakeIt(new LocalTime(9, 0));
         when(preferredCallTimeService.getPreferredCallTime("afternoon")).thenReturn(LocalTime.parse("14:30:00"));
 
         service.enroll("id", parse("2013-01-02"), parse("2013-01-01"), "afternoon");
 
         verify(scheduleTrackingService)
                 .enroll(enrollmentFor("id", "Post Delivery Danger Signs - Day 2", parse("2013-01-03"), new Time(14, 30)));
     }
 
     @Test
     public void shouldEnrollMotherToDay4ScheduleOnTheNextDayOfRegistrationWhenRegistrationDateIsTwoDaysAfterDeliveryDate() throws Exception {
        DateUtil.fakeIt(LocalDate.parse("2013-01-03"));
         LocalTimeUtil.fakeIt(new LocalTime(9, 0));
         when(preferredCallTimeService.getPreferredCallTime("morning")).thenReturn(LocalTime.parse("09:30:00"));
 
         service.enroll("id", parse("2013-01-03"), parse("2013-01-01"), "morning");
 
         verify(scheduleTrackingService)
                 .enroll(enrollmentFor("id", "Post Delivery Danger Signs - Day 4", parse("2013-01-04"), new Time(9, 30)));
     }
 
     @Test
     public void shouldEnrollMotherToDay5ScheduleOnTheNextDayOfRegistrationWhenRegistrationDateIsThreeDaysAfterDeliveryDate() throws Exception {
         LocalTimeUtil.fakeIt(new LocalTime(9, 0));
         when(preferredCallTimeService.getPreferredCallTime("morning")).thenReturn(LocalTime.parse("09:30:00"));
 
         service.enroll("id", parse("2013-01-04"), parse("2013-01-01"), "morning");
 
         verify(scheduleTrackingService)
                 .enroll(enrollmentFor("id", "Post Delivery Danger Signs - Day 5", parse("2013-01-05"), new Time(9, 30)));
     }
 
     @Test
     public void shouldNotEnrollMotherToAnyScheduleWhenRegistrationDateIsSevenDaysAfterDeliveryDate() throws Exception {
         LocalTimeUtil.fakeIt(new LocalTime(9, 0));
         when(preferredCallTimeService.getPreferredCallTime("morning")).thenReturn(LocalTime.parse("09:30:00"));
 
         service.enroll("id", parse("2013-01-08"), parse("2013-01-01"), "morning");
 
         verifyZeroInteractions(scheduleTrackingService);
     }
 
     @Test
     public void shouldNotEnrollMotherToSecondScheduleWhenRegistrationDateIsSixDaysAfterDeliveryDate() throws Exception {
         DateUtil.fakeIt(LocalDate.parse("2013-01-07"));
         LocalTimeUtil.fakeIt(new LocalTime(9, 0));
 
         service.enroll("id", parse("2013-01-07"), parse("2013-01-01"), "morning");
 
         verify(scheduleTrackingService)
                 .enroll(enrollmentFor("id", "Post Delivery Danger Signs - Day 7", parse("2013-01-07"), new Time(9, 5)));
         verifyNoMoreInteractions(scheduleTrackingService);
     }
 
     @Test
     public void shouldNotEnrollMotherIfRegistrationDateIsNotToday() throws Exception {
        DateUtil.fakeIt(LocalDate.parse("2013-01-10"));
 
         service.enroll("id", parse("2013-01-02"), parse("2013-01-01"), "morning");
 
         verifyZeroInteractions(scheduleTrackingService);
     }
 
     private EnrollmentRequest enrollmentFor(final String externalId, final String scheduleName,
                                             final LocalDate referenceDate, final Time preferredAlertTime) {
         return argThat(new ArgumentMatcher<EnrollmentRequest>() {
             @Override
             public boolean matches(Object o) {
                 EnrollmentRequest request = (EnrollmentRequest) o;
                 return externalId.equals(request.getExternalId()) && referenceDate.equals(request.getReferenceDate())
                         && scheduleName.equals(request.getScheduleName())
                         && preferredAlertTime.equals(request.getPreferredAlertTime()
                 );
             }
         });
     }
 }
