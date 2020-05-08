 package org.motechproject.ghana.national.handlers;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.motechproject.appointments.api.service.AppointmentService;
 import org.motechproject.appointments.api.service.contract.VisitResponse;
 import org.motechproject.appointments.api.service.contract.VisitsQuery;
 import org.motechproject.ghana.national.bean.GeneralQueryForm;
 import org.motechproject.ghana.national.domain.Facility;
 import org.motechproject.ghana.national.domain.GeneralQueryType;
 import org.motechproject.ghana.national.domain.Patient;
import org.motechproject.ghana.national.exception.XFormHandlerException;
 import org.motechproject.ghana.national.messagegateway.domain.MessageDispatcher;
 import org.motechproject.ghana.national.repository.SMSGateway;
 import org.motechproject.ghana.national.service.FacilityService;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.mobileforms.api.callbacks.FormPublishHandler;
 import org.motechproject.model.MotechEvent;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.motechproject.openmrs.advice.LoginAsAdmin;
 import org.motechproject.scheduletracking.api.domain.Enrollment;
 import org.motechproject.scheduletracking.api.domain.EnrollmentStatus;
 import org.motechproject.scheduletracking.api.domain.WindowName;
 import org.motechproject.scheduletracking.api.service.EnrollmentsQuery;
 import org.motechproject.scheduletracking.api.service.impl.EnrollmentsQueryService;
 import org.motechproject.server.event.annotations.MotechListener;
 import org.motechproject.util.DateUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 
 import static org.motechproject.ghana.national.domain.Constants.FORM_BEAN;
 import static org.motechproject.util.DateUtil.newDateTime;
 
 @Component
 public class GeneralQueryFormHandler implements FormPublishHandler {
     private final Logger log = LoggerFactory.getLogger(this.getClass());
 
     @Autowired
     EnrollmentsQueryService enrollmentsQueryService;
 
     @Autowired
     AppointmentService appointmentService;
 
     @Autowired
     FacilityService facilityService;
 
     @Autowired
     SMSGateway smsGateway;
 
     @Autowired
     PatientService patientService;
 
     @Override
     @MotechListener(subjects = "form.validation.successful.NurseQuery.GeneralQuery")
     @LoginAsAdmin
     @ApiSession
     public void handleFormEvent(MotechEvent event) {
        try {
         GeneralQueryForm generalQueryForm = (GeneralQueryForm) event.getParameters().get(FORM_BEAN);
         GeneralQueryType queryType = generalQueryForm.getQueryType();
         String facilityMotechId = generalQueryForm.getFacilityId();
         Facility facility = facilityService.getFacilityByMotechId(facilityMotechId);
 
         String messageContent = prepareMessageContentForQuery(queryType, facility, getEnrollmentQuery(generalQueryForm, facility));
         smsGateway.dispatchSMS(generalQueryForm.getResponsePhoneNumber(), messageContent);
        }
        catch (Exception e){
            log.error("Exception occured while quering for defaulters", e);
            throw new XFormHandlerException(event.getSubject(), e);
        }
     }
 
     private String prepareMessageContentForQuery(GeneralQueryType queryType, Facility facility, EnrollmentsQuery enrollmentsQuery) {
         StringBuilder messageContent = new StringBuilder();
         StringBuilder messageBody = new StringBuilder();
         messageContent.append("List of ").append(queryType.name());
         if (queryType.getSchedules().length > 1) {
             messageContent.append("-").append(StringUtils.join(queryType.getSchedules(), ",")).append(MessageDispatcher.SMS_SEPARATOR);
         } else {
             messageContent.append(MessageDispatcher.SMS_SEPARATOR);
         }
         for (Enrollment enrollment : enrollmentsQueryService.search(enrollmentsQuery)) {
             Patient patient = patientService.patientByOpenmrsId(enrollment.getExternalId());
             String name = enrollment.getCurrentMilestoneName() == null ? enrollment.getScheduleName() : enrollment.getCurrentMilestoneName();
             messageBody.append(messageFor(name, patient)).append(MessageDispatcher.SMS_SEPARATOR);
         }
         if (GeneralQueryType.ANC_DEFAULTERS.equals(queryType)) {
             messageBody.append(fetchUnvisitedANCAppointments(facility));
         }
 
         if (StringUtils.isEmpty(messageBody.toString())) {
             messageBody.append("No Patients found for ").append(queryType);
         }
         return messageContent.append(messageBody).toString();
     }
 
     private EnrollmentsQuery getEnrollmentQuery(GeneralQueryForm generalQueryForm, Facility facility) {
         GeneralQueryType queryType = generalQueryForm.getQueryType();
         String[] schedules = queryType.getSchedules();
         EnrollmentsQuery enrollmentsQuery = new EnrollmentsQuery().havingMetadata("facilityId", facility.getMrsFacilityId()).havingSchedule(schedules);
         DateTime today = newDateTime(DateUtil.today());
 
         if (GeneralQueryType.UPCOMING_DELIVERIES.equals(queryType)) {
             enrollmentsQuery = enrollmentsQuery.havingState(EnrollmentStatus.ACTIVE).havingWindowEndingDuring(WindowName.earliest, today.withTimeAtStartOfDay(), today.plusWeeks(1));
         } else if (GeneralQueryType.RECENT_DELIVERIES.equals(queryType)) {
             enrollmentsQuery = enrollmentsQuery.completedDuring(today.minusWeeks(1), today);
         } else {
             enrollmentsQuery = enrollmentsQuery.havingState(EnrollmentStatus.ACTIVE).currentlyInWindow(WindowName.late, WindowName.max);
         }
         return enrollmentsQuery;
     }
 
     private StringBuilder fetchUnvisitedANCAppointments(Facility facility) {
         StringBuilder messageContent = new StringBuilder();
         DateTime today = newDateTime(DateUtil.today());
         VisitsQuery visitsQuery = new VisitsQuery().havingMetadata("facilityId", facility.getMrsFacilityId()).withDueDateIn(today.minusWeeks(3), today).unvisited();
         List<VisitResponse> visitResults = appointmentService.search(visitsQuery);
         for (VisitResponse visitResult : visitResults) {
             Patient patient = patientService.getPatientByMotechId(visitResult.getExternalId());
             messageContent.append(messageFor(visitResult.getName(), patient)).append(MessageDispatcher.SMS_SEPARATOR);
         }
         return messageContent;
     }
 
     private String messageFor(String milestone, Patient patient) {
         return String.format("%s %s, %s %s", patient.getFirstName(), patient.getLastName(), patient.getMotechId(), milestone);
     }
 }
