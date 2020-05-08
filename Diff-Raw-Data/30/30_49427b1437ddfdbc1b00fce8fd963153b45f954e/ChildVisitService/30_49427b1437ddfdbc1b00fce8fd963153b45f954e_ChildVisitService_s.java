 package org.motechproject.ghana.national.service;
 
 import org.apache.commons.lang.StringUtils;
 import org.joda.time.DateTime;
 import org.joda.time.LocalDate;
 import org.motechproject.ghana.national.domain.IPTVaccine;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.factory.ChildVisitEncounterFactory;
 import org.motechproject.ghana.national.mapper.ScheduleEnrollmentMapper;
 import org.motechproject.ghana.national.repository.AllEncounters;
 import org.motechproject.ghana.national.repository.AllSchedules;
 import org.motechproject.ghana.national.service.request.PNCBabyRequest;
 import org.motechproject.ghana.national.vo.CWCVisit;
 import org.motechproject.mrs.model.MRSEncounter;
 import org.motechproject.scheduletracking.api.service.EnrollmentRecord;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.util.DateUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.List;
 
 import static org.motechproject.ghana.national.configuration.ScheduleNames.*;
 import static org.motechproject.ghana.national.domain.Concept.MEASLES;
 import static org.motechproject.ghana.national.domain.Concept.YF;
 import static org.motechproject.util.DateUtil.newDate;
 import static org.motechproject.util.DateUtil.time;
 
 @Service
 public class ChildVisitService {
     private AllEncounters allEncounters;
     private AllSchedules allSchedules;
 
     @Autowired
     public ChildVisitService(AllEncounters allEncounters, AllSchedules allSchedules) {
         this.allEncounters = allEncounters;
         this.allSchedules = allSchedules;
     }
 
     public MRSEncounter save(CWCVisit cwcVisit) {
         updateIPTSchedule(cwcVisit);
         updatePentaSchedule(cwcVisit);
         updateYellowFeverSchedule(cwcVisit);
         updateMeaslesSchedule(cwcVisit);
         return allEncounters.persistEncounter(new ChildVisitEncounterFactory().createEncounter(cwcVisit));
     }
 
     public MRSEncounter save(PNCBabyRequest pncBabyRequest) {
         DateTime visitDate = pncBabyRequest.getDate();
         Patient patient = pncBabyRequest.getPatient();
         EnrollmentRequest enrollmentOrFulfillRequest = new ScheduleEnrollmentMapper().map(patient, patient.pncProgramToFulfilOnVisit(pncBabyRequest.getVisit(), visitDate)
                 , visitDate);
         allSchedules.enrollOrFulfill(enrollmentOrFulfillRequest, visitDate.toLocalDate(), time(visitDate));
         return allEncounters.persistEncounter(new ChildVisitEncounterFactory().createEncounter(pncBabyRequest));
     }
 
     void updateIPTSchedule(CWCVisit cwcVisit) {
         IPTVaccine iptVaccine = IPTVaccine.createFromCWCVisit(cwcVisit);
         if(iptVaccine != null) {
             Patient patient = iptVaccine.getGivenTo();
             LocalDate visitDate = newDate(cwcVisit.getDate());
             EnrollmentRequest enrollmentOrFulfillRequest = new ScheduleEnrollmentMapper().map(patient, patient.cwcIPTPatientCareEnrollOnVisitAfter14Weeks(visitDate), visitDate, iptVaccine.getIptMilestone());
             allSchedules.enrollOrFulfill(enrollmentOrFulfillRequest, visitDate);
         }
     }
 
     private void updateMeaslesSchedule(CWCVisit cwcVisit) {
         List<String> immunizations = cwcVisit.getImmunizations();
         Patient patient = cwcVisit.getPatient();
         if (immunizations.contains(MEASLES.name()) && enrollment(patient.getMRSPatientId(), CWC_MEASLES_VACCINE) != null) {
             allSchedules.fulfilCurrentMilestone(patient.getMRSPatientId(), CWC_MEASLES_VACCINE, newDate(cwcVisit.getDate()));
         }
     }
 
     void updateYellowFeverSchedule(CWCVisit cwcVisit) {
         if (cwcVisit.getImmunizations().contains(YF.name())) {
             Patient patient = cwcVisit.getPatient();
             LocalDate visitDate = DateUtil.newDate(cwcVisit.getDate());
             allSchedules.fulfilCurrentMilestone(patient.getMRSPatientId(), CWC_YELLOW_FEVER, visitDate);
         }
     }
 
     void updatePentaSchedule(CWCVisit cwcVisit) {
         if (!StringUtils.isEmpty(cwcVisit.getPentadose())) {
             Patient patient = cwcVisit.getPatient();
             LocalDate visitDate = DateUtil.newDate(cwcVisit.getDate());
 
             if (null == enrollment(patient.getMRSPatientId(), CWC_PENTA)) {
                 allSchedules.enroll(new ScheduleEnrollmentMapper().map(patient, patient.pentaPatientCare(), visitDate, milestoneName(cwcVisit)));
             }
             allSchedules.fulfilCurrentMilestone(patient.getMRSPatientId(), CWC_PENTA, visitDate);
         }
     }
 
     private String milestoneName(CWCVisit cwcVisit) {
         return PentaDose.byValue(Integer.parseInt(cwcVisit.getPentadose())).milestoneName();
     }
 
     private EnrollmentRecord enrollment(String mrsPatientId, String programName) {
         return allSchedules.enrollment(new ScheduleEnrollmentMapper().map(mrsPatientId, programName));
     }
 }
