 package org.who.mcheck.core.service;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.joda.time.Period;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.domain.Milestone;
 import org.motechproject.scheduletracking.api.domain.Schedule;
 import org.motechproject.scheduletracking.api.repository.AllSchedules;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 import org.who.mcheck.core.util.DateUtil;
 
 import java.text.MessageFormat;
 
 import static org.joda.time.LocalDate.parse;
 import static org.who.mcheck.core.AllConstants.Schedule.MORNING;
 import static org.who.mcheck.core.AllConstants.Schedule.POST_DELIVERY_DANGER_SIGNS_SCHEDULE_NAME;
 
 @Service
 public class MotherScheduleService {
     private final Log log = LogFactory.getLog(MotherScheduleService.class);
     private AllSchedules allSchedules;
     private ScheduleTrackingService scheduleTrackingService;
     private String preferredCallTimeInMorning;
     private String preferredCallTimeInAfternoon;
 
     @Autowired
     public MotherScheduleService(ScheduleTrackingService scheduleTrackingService,
                                  AllSchedules allSchedules,
                                  @Value("#{mCheck['ivr.preferred.call.time.morning']}") String preferredCallTimeInMorning,
                                  @Value("#{mCheck['ivr.preferred.call.time.afternoon']}") String preferredCallTimeInAfternoon) {
         this.scheduleTrackingService = scheduleTrackingService;
         this.allSchedules = allSchedules;
         this.preferredCallTimeInMorning = preferredCallTimeInMorning;
         this.preferredCallTimeInAfternoon = preferredCallTimeInAfternoon;
     }
 
     public void enroll(String motherId, String registrationDate, String dailyCallPreference) {
         String startingMilestoneName = getStartingMilestoneName(POST_DELIVERY_DANGER_SIGNS_SCHEDULE_NAME, parse(registrationDate));
         String preferredAlertTime =
                 MORNING.equalsIgnoreCase(dailyCallPreference)
                         ? preferredCallTimeInMorning
                         : preferredCallTimeInAfternoon;
         EnrollmentRequest scheduleEnrollmentRequest = new EnrollmentRequest()
                 .setScheduleName(POST_DELIVERY_DANGER_SIGNS_SCHEDULE_NAME)
                 .setExternalId(motherId)
                 .setPreferredAlertTime(new Time(LocalTime.parse(preferredAlertTime)))
                 .setReferenceDate(parse(registrationDate))
                 .setStartingMilestoneName(startingMilestoneName);
 
         log.info(MessageFormat.format("Enrolling mother with ID: {0} to schedule: {1}, to milestone: {2} preferred call time: {3}",
                motherId, POST_DELIVERY_DANGER_SIGNS_SCHEDULE_NAME, startingMilestoneName, preferredAlertTime));
         scheduleTrackingService.enroll(scheduleEnrollmentRequest);
     }
 
     private String getStartingMilestoneName(String name, LocalDate referenceDate) {
         Schedule schedule = allSchedules.getByName(name);
         Period totalDuration = new Period();
         for (Milestone milestone : schedule.getMilestones()) {
             totalDuration = totalDuration.plus(milestone.getMaximumDuration());
             if (referenceDate.plus(totalDuration).isAfter(DateUtil.today()))
                 return milestone.getName();
         }
         return null;
     }
 }
