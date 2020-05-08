 package org.motechproject.ghana.national.repository;
 
 import org.joda.time.DateTime;
 import org.joda.time.Period;
import org.motechproject.appointments.api.contract.CreateVisitRequest;
import org.motechproject.appointments.api.contract.ReminderConfiguration;
import org.motechproject.appointments.api.contract.VisitResponse;
import org.motechproject.appointments.api.contract.VisitsQuery;
import org.motechproject.appointments.api.service.AppointmentService;
 import org.motechproject.ghana.national.domain.EncounterType;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import java.util.Date;
 import java.util.List;
 
 import static org.motechproject.util.DateUtil.endOfDay;
 
 @Repository
 public class AllAppointments {
 
     @Autowired
     AppointmentService appointmentService;
     static final int ONE_WEEK_BEFORE = 7;   //to be in sync with motech-appointments api
     static final int ONE_WEEK_LATER = -7;   //to be in sync with motech-appointments api
     static final int TWO_WEEKS_LATER = -14;
 
     public void remove(Patient patient) {
         appointmentService.removeCalendar(patient.getMotechId());
     }
 
     public void updateANCVisitSchedule(Patient patient, DateTime nextVisitDate) {
         CreateVisitRequest createVisitRequest = new CreateVisitRequest().setAppointmentDueDate(nextVisitDate).setVisitName(EncounterType.ANC_VISIT.value());
 
         createVisitRequest.addAppointmentReminderConfiguration(new ReminderConfiguration().setRemindFrom(ONE_WEEK_BEFORE));
         createVisitRequest.addAppointmentReminderConfiguration(new ReminderConfiguration());
         createVisitRequest.addAppointmentReminderConfiguration(new ReminderConfiguration().setRemindFrom(ONE_WEEK_LATER));
         createVisitRequest.addAppointmentReminderConfiguration(new ReminderConfiguration().setRemindFrom(TWO_WEEKS_LATER));
         appointmentService.addVisit(patient.getMotechId(), createVisitRequest);
     }
 
     public void fulfillCurrentANCVisit(Patient patient, Date visitedDate) {
         appointmentService.visited(patient.getMotechId(), EncounterType.ANC_VISIT.value(), DateUtil.newDateTime(visitedDate));
     }
 
     public List<VisitResponse> upcomingAppointmentsForCurrentWeek(String externalId) {
         final DateTime startToday = DateUtil.now().withTimeAtStartOfDay();
         Period period = Period.days(6);
         return appointmentService.search(new VisitsQuery().havingExternalId(externalId).withDueDateIn(startToday, endOfDay(startToday.plus(period).toDate())));
     }
 }
