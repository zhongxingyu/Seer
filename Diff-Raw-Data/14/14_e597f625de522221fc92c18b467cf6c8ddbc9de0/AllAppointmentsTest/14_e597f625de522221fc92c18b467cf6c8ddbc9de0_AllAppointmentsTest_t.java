 package org.motechproject.ghana.national.repository;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.appointments.api.contract.CreateVisitRequest;
 import org.motechproject.appointments.api.contract.ReminderConfiguration;
import org.motechproject.appointments.api.service.AppointmentService;
 import org.motechproject.ghana.national.domain.EncounterType;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.mrs.model.MRSPatient;
 import org.motechproject.util.DateUtil;
 import org.springframework.test.util.ReflectionTestUtils;
 import org.unitils.reflectionassert.ReflectionComparatorMode;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
 
 public class AllAppointmentsTest {
     @Mock
     AppointmentService mockAppointmentService;
     AllAppointments allAppointments;
 
     @Before
     public void setup() {
         initMocks(this);
         allAppointments = new AllAppointments();
         ReflectionTestUtils.setField(allAppointments, "appointmentService", mockAppointmentService);
     }
 
     @Test
     public void shouldFulfilCurrentVisitSchedule() {
         String motechId = "motech id";
         allAppointments.remove(new Patient(new MRSPatient(motechId, null, null)));
         verify(mockAppointmentService).removeCalendar(motechId);
     }
 
     @Test
     public void shouldFulfillCurrentANCVisitSchedule() {
         String motechId = "1212121";
         Date visitedDate = new Date();
         allAppointments.fulfillCurrentANCVisit(new Patient(new MRSPatient(motechId, null, null)), visitedDate);
         verify(mockAppointmentService).visited(motechId, EncounterType.ANC_VISIT.value(), DateUtil.newDateTime(visitedDate));
     }
 
     @Test
     public void shouldCreateANCVisitSchedule() {
         String motechId = "1234556";
         DateTime nextANCVisit = DateTime.now();
 
         allAppointments.updateANCVisitSchedule(new Patient(new MRSPatient(motechId, null, null)), nextANCVisit);
 
         ArgumentCaptor<CreateVisitRequest> argumentCaptor = ArgumentCaptor.forClass(CreateVisitRequest.class);
         verify(mockAppointmentService).addVisit(eq(motechId), argumentCaptor.capture());
 
         CreateVisitRequest actualVisitRequest = argumentCaptor.getValue();
         List<ReminderConfiguration> actualReminderConfigurations = actualVisitRequest.getAppointmentReminderConfigurations();
 
         List<ReminderConfiguration> expectedReminderConfigurations = new ArrayList<ReminderConfiguration>();
         expectedReminderConfigurations.add(new ReminderConfiguration().setRemindFrom(AllAppointments.ONE_WEEK_BEFORE));
         expectedReminderConfigurations.add(new ReminderConfiguration());
         expectedReminderConfigurations.add(new ReminderConfiguration().setRemindFrom(AllAppointments.ONE_WEEK_LATER));
         expectedReminderConfigurations.add(new ReminderConfiguration().setRemindFrom(AllAppointments.TWO_WEEKS_LATER));
 
         assertThat(actualReminderConfigurations.size(), is(4));
         assertThat(actualVisitRequest.getVisitName(), is(EncounterType.ANC_VISIT.value()));
         assertReflectionEquals(expectedReminderConfigurations, actualReminderConfigurations, ReflectionComparatorMode.LENIENT_ORDER);
     }
 }
