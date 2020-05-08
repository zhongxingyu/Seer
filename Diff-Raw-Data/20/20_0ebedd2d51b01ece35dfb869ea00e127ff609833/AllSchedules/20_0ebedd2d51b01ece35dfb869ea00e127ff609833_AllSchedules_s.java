 package org.motechproject.ghana.national.repository;
 
 import org.joda.time.LocalDate;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.scheduletracking.api.service.EnrollmentResponse;
 import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Repository;
 
 import java.util.List;
 
 @Repository
 public class AllSchedules {
     private ScheduleTrackingService scheduleTrackingService;
 
     @Autowired
     public AllSchedules(ScheduleTrackingService scheduleTrackingService) {
         this.scheduleTrackingService = scheduleTrackingService;
     }
 
     public void enrollOrFulfill(EnrollmentRequest enrollmentRequest, LocalDate fulfillmentDate) {
         if (enrollment(enrollmentRequest) == null) {
             enroll(enrollmentRequest);
         }
         fulfilCurrentMilestone(enrollmentRequest.getExternalId(), enrollmentRequest.getScheduleName(), fulfillmentDate);
     }
 
     public void enroll(EnrollmentRequest enrollmentRequest) {
         scheduleTrackingService.enroll(enrollmentRequest);
     }
 
     public void fulfilCurrentMilestone(String externalId, String scheduleName, LocalDate fulfillmentDate) {
         scheduleTrackingService.fulfillCurrentMilestone(externalId, scheduleName, fulfillmentDate);
     }
 
     public void unEnroll(String externalId, String scheduleName) {
        scheduleTrackingService.unenroll(externalId, scheduleName);
     }
 
     public void unEnroll(String externalId, List<String> scheduleNames) {
        scheduleTrackingService.safeUnEnroll(externalId, scheduleNames);
     }
 
     public EnrollmentResponse enrollment(EnrollmentRequest enrollmentRequest) {
         return scheduleTrackingService.getEnrollment(enrollmentRequest.getExternalId(), enrollmentRequest.getScheduleName());
     }
 }
