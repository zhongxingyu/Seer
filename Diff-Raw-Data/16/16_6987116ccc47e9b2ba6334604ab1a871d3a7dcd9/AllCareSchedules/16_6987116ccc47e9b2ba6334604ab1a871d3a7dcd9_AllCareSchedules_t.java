 package org.motechproject.ghana.national.repository;
 
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.joda.time.Period;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.domain.WindowName;
 import org.motechproject.scheduletracking.api.domain.exception.InvalidEnrollmentException;
 import org.motechproject.scheduletracking.api.service.EnrollmentRecord;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.scheduletracking.api.service.EnrollmentsQuery;
 import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
 import org.motechproject.util.DateUtil;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import java.util.List;
 
 import static java.lang.String.format;
 import static org.motechproject.util.DateUtil.endOfDay;
 
 @Repository
 public class AllCareSchedules {
     private ScheduleTrackingService scheduleTrackingService;
     private Logger log = LoggerFactory.getLogger(AllCareSchedules.class);
 
     @Autowired
     public AllCareSchedules(ScheduleTrackingService scheduleTrackingService) {
         this.scheduleTrackingService = scheduleTrackingService;
     }
 
     public void enrollOrFulfill(EnrollmentRequest enrollmentRequest, LocalDate fulfillmentDate, Time fulfillmentTime) {
         if (enrollment(enrollmentRequest) == null) {
             enroll(enrollmentRequest);
         }
        try {
             fulfilCurrentMilestone(enrollmentRequest.getExternalId(), enrollmentRequest.getScheduleName(), fulfillmentDate, fulfillmentTime);
        } catch (InvalidEnrollmentException e) {
            // For PNCBaby and Mother where defaultment schedule triggers immediately after it gets created.
         }
     }
 
     public void enrollOrFulfill(EnrollmentRequest enrollmentRequest, LocalDate fulfillmentDate) {
         if (enrollment(enrollmentRequest) == null) {
             enroll(enrollmentRequest);
         }
        try {
             fulfilCurrentMilestone(enrollmentRequest.getExternalId(), enrollmentRequest.getScheduleName(), fulfillmentDate);
        } catch (InvalidEnrollmentException e) {
            // For PNCBaby and Mother where defaultment schedule triggers immediately after it gets created.
         }
     }
 
     public void enroll(EnrollmentRequest enrollmentRequest) {
         scheduleTrackingService.enroll(enrollmentRequest);
     }
 
     public boolean enrollIfNotActive(EnrollmentRequest enrollmentRequest) {
         if (enrollment(enrollmentRequest) == null) {
             enroll(enrollmentRequest);
             return true;
         }
         return false;
     }
 
     public boolean safeFulfilCurrentMilestone(String externalId, String scheduleName, LocalDate fulfillmentDate) {
         try {
             scheduleTrackingService.fulfillCurrentMilestone(externalId, scheduleName, fulfillmentDate);
             return true;
         } catch (InvalidEnrollmentException invalidEnrollmentException) {
             log.warn(format("Problem in fulfil for {%s, %s, %s}", externalId, scheduleName, fulfillmentDate), invalidEnrollmentException.toString());
             return false;
         }
     }
 
     public void fulfilCurrentMilestone(String externalId, String scheduleName, LocalDate fulfillmentDate) {
         scheduleTrackingService.fulfillCurrentMilestone(externalId, scheduleName, fulfillmentDate);
     }
 
     void fulfilCurrentMilestone(String externalId, String scheduleName, LocalDate fulfillmentDate, Time fulfillmentTime) {
         scheduleTrackingService.fulfillCurrentMilestone(externalId, scheduleName, fulfillmentDate, fulfillmentTime);
     }
 
     public void unEnroll(String externalId, List<String> scheduleNames) {
         scheduleTrackingService.unenroll(externalId, scheduleNames);
     }
 
     public EnrollmentRecord enrollment(EnrollmentRequest enrollmentRequest) {
         return scheduleTrackingService.getEnrollment(enrollmentRequest.getExternalId(), enrollmentRequest.getScheduleName());
     }
 
     public List<EnrollmentRecord> search(EnrollmentsQuery enrollmentQuery) {
         return scheduleTrackingService.searchWithWindowDates(enrollmentQuery);
     }
 
     public List<EnrollmentRecord> upcomingCareForCurrentWeek(String externalId) {
         final DateTime startToday = DateUtil.now().withTimeAtStartOfDay();
         Period period = Period.days(6);
         return search(new EnrollmentsQuery().havingExternalId(externalId)
                 .havingWindowStartingDuring(WindowName.due, startToday, endOfDay(startToday.plus(period).toDate())));
     }
 
     public List<EnrollmentRecord> defaultersByMetaSearch(String metaField, String value) {
         return search(new EnrollmentsQuery().currentlyInWindow(WindowName.late)
                 .havingMetadata(metaField, value));
     }
 
     public EnrollmentRecord getActiveEnrollment(String externalId, String scheduleName) {
         return scheduleTrackingService.getEnrollment(externalId, scheduleName);
     }

    public List<DateTime> getDueWindowAlertTimings(EnrollmentRequest enrollmentRequest) {
         return scheduleTrackingService.getAlertTimings(enrollmentRequest).getDueWindowAlertTimings();
     }
 
 }
