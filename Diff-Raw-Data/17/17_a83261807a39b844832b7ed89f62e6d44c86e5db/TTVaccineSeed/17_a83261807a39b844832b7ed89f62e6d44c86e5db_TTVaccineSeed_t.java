 package org.motechproject.ghana.national.tools.seed.data;
 
 import org.joda.time.DateTime;
 import org.motechproject.ghana.national.configuration.ScheduleNames;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.domain.TTVaccineDosage;
 import org.motechproject.ghana.national.repository.AllSchedules;
 import org.motechproject.ghana.national.tools.seed.data.domain.UpcomingSchedule;
 import org.motechproject.ghana.national.tools.seed.data.source.OldGhanaScheduleSource;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.repository.AllTrackedSchedules;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 
 import static java.lang.Boolean.FALSE;
 
 @Component("ttVaccineSeed")
 public class TTVaccineSeed extends ScheduleMigrationSeed {
 
     @Autowired
     public TTVaccineSeed(OldGhanaScheduleSource oldGhanaScheduleSource, AllTrackedSchedules allTrackedSchedules, AllSchedules allSchedules) {
         super(allTrackedSchedules, oldGhanaScheduleSource, allSchedules, FALSE);
     }
 
     protected List<UpcomingSchedule> getAllUpcomingSchedules() {
         return oldGhanaScheduleSource.getUpcomingTTSchedules();
     }
 
 
     @Override
     protected String mapMilestoneName(String milestoneName) {
         return milestoneName;
     }
 
     @Override
     public String getScheduleName(String milestoneName) {
         return ScheduleNames.TT_VACCINATION;
     }
 
     @Override
     protected void enroll(DateTime milestoneReferenceDate, String milestoneName, Patient patient) {
         if (TTVaccineDosage.TT1.getScheduleMilestoneName().equals(milestoneName)) {
             milestoneReferenceDate = milestoneReferenceDate.minusWeeks(1);
         }
         EnrollmentRequest enrollmentRequest = new EnrollmentRequest(patient.getMRSPatientId(),
                getScheduleName(milestoneName), null,
                 milestoneReferenceDate.toLocalDate(), new Time(milestoneReferenceDate.toLocalTime()),
                 milestoneReferenceDate.toLocalDate(), new Time(milestoneReferenceDate.toLocalTime()), milestoneName, null);
         allSchedules.enroll(enrollmentRequest);
 
     }
 }
