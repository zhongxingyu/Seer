 package org.motechproject.ghana.national.service;
 
import org.joda.time.DateTime;
 import org.motechproject.ghana.national.domain.mobilemidwife.MobileMidwifeEnrollment;
 import org.motechproject.ghana.national.repository.AllMobileMidwifeEnrollments;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 @Service
 public class MobileMidwifeService {
 
     @Autowired
     private AllMobileMidwifeEnrollments allEnrollments;
     @Autowired
     private MobileMidwifeCampaign mobileMidwifeCampaign;
 
     public void register(MobileMidwifeEnrollment enrollment) {
        MobileMidwifeEnrollment newEnrollment;
         unregister(enrollment.getPatientId());
         enrollment.setActive(true);
         allEnrollments.add(enrollment);
 
         if (enrollment.campaignApplicable()) {
            //TODO: Hack for 24 hour interval
            //Cycle Date One Day Back For 24 Hour Interval With 2 hour Buffer ForFuture Schedule, if it falls on the same day
            DateTime scheduleStartDateFor24HourWindowWith2HourBuffer = mobileMidwifeCampaign.nearestCycleDate(enrollment).plusDays(-1).plusHours(2);
            mobileMidwifeCampaign.start(enrollment.setScheduleStartDate(scheduleStartDateFor24HourWindowWith2HourBuffer));
         }
     }
 
     public void unregister(String patientId) {
         MobileMidwifeEnrollment enrollment = findActiveBy(patientId);
         if (enrollment != null) {
             enrollment.setActive(false);
             allEnrollments.update(enrollment);
             if (enrollment.campaignApplicable()) mobileMidwifeCampaign.stop(enrollment);
         }
     }
 
     public MobileMidwifeEnrollment findActiveBy(String patientId) {
         return allEnrollments.findActiveBy(patientId);
     }
 
     public MobileMidwifeEnrollment findLatestEnrollment(String patientId) {
         return allEnrollments.findLatestEnrollment(patientId);
     }
 }
