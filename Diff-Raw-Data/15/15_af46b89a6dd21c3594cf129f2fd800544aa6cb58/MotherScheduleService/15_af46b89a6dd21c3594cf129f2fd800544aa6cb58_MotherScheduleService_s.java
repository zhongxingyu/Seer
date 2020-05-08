 package org.who.mcheck.core.service;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.joda.time.LocalDate;
 import org.joda.time.LocalTime;
 import org.joda.time.Period;
 import org.motechproject.model.Time;
 import org.motechproject.scheduletracking.api.service.EnrollmentRequest;
 import org.motechproject.scheduletracking.api.service.ScheduleTrackingService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 import org.who.mcheck.core.AllConstants;
 import org.who.mcheck.core.util.DateUtil;
 import org.who.mcheck.core.util.IntegerUtil;
 import org.who.mcheck.core.util.LocalTimeUtil;
 
 import java.text.MessageFormat;
 
 @Service
 public class MotherScheduleService {
     private final Log log = LogFactory.getLog(MotherScheduleService.class);
     private final int firstCallDelay;
     private ScheduleTrackingService scheduleTrackingService;
     private PreferredCallTimeService preferredCallTimeService;
 
     @Autowired
     public MotherScheduleService(ScheduleTrackingService scheduleTrackingService,
                                  @Value("#{mCheck['first.call.delay']}") String firstCallDelay,
                                  PreferredCallTimeService preferredCallTimeService) {
         this.scheduleTrackingService = scheduleTrackingService;
         this.preferredCallTimeService = preferredCallTimeService;
         this.firstCallDelay = IntegerUtil.tryParse(firstCallDelay, AllConstants.DEFAULT_FIRST_CALL_DELAY_IN_MINUTES);
     }
 
     public void enroll(String motherId, LocalDate registrationDate, LocalDate deliveryDate, String dailyCallPreference) {
        if (DateUtil.today().isBefore(registrationDate)) {
             log.info(MessageFormat.format("Not making any calls for mother: {0} as registration date: {1} is in the past, today: {2}.",
                     motherId, registrationDate, DateUtil.today()));
             return;
         }
         int firstSchedule = enrollMotherToFirstDaySchedule(motherId, registrationDate, deliveryDate);
         enrollMotherToSecondDaySchedule(motherId, registrationDate, dailyCallPreference, firstSchedule, deliveryDate);
     }
 
     private int enrollMotherToFirstDaySchedule(String motherId, LocalDate registrationDate, LocalDate deliveryDate) {
         Period periodBetweenDeliveryAndRegistration = new Period(deliveryDate, registrationDate);
         int firstSchedule = periodBetweenDeliveryAndRegistration.toStandardDays().getDays() < 2
                 ? 1
                 : periodBetweenDeliveryAndRegistration.toStandardDays().getDays() + 1;
         if (firstSchedule > AllConstants.Schedule.LAST_DAY_OF_POST_DELIVERY_DANGER_SIGNS_SCHEDULE) {
             log.info(MessageFormat.format("Not making first day call as the registration date: {0} is {1} days past delivery date: {2}.",
                     registrationDate, firstSchedule, deliveryDate));
             return firstSchedule;
         }
         String firstScheduleName = MessageFormat.format(AllConstants.Schedule.POST_DELIVERY_DANGER_SIGNS_SCHEDULE_TEMPLATE, firstSchedule);
         LocalTime firstCallTime = LocalTimeUtil.now().plusMinutes(firstCallDelay);
         enrollToSchedule(motherId, registrationDate, firstScheduleName, firstCallTime);
         return firstSchedule;
     }
 
     private void enrollMotherToSecondDaySchedule(String motherId, LocalDate registrationDate, String dailyCallPreference, int firstSchedule, LocalDate deliveryDate) {
         if (firstSchedule >= AllConstants.Schedule.LAST_DAY_OF_POST_DELIVERY_DANGER_SIGNS_SCHEDULE) {
             log.info(MessageFormat.format("Not enrolling mother to subsequent calls as the registration date: {0} is {1} days past delivery date: {2}.",
                     registrationDate, firstSchedule, deliveryDate));
             return;
         }
 
         String secondScheduleName = MessageFormat.format(AllConstants.Schedule.POST_DELIVERY_DANGER_SIGNS_SCHEDULE_TEMPLATE, firstSchedule + 1);
         LocalDate secondScheduleReferenceDate = registrationDate.plusDays(1);
         enrollToSchedule(motherId, secondScheduleReferenceDate, secondScheduleName, preferredCallTimeService.getPreferredCallTime(dailyCallPreference));
     }
 
     private void enrollToSchedule(String motherId, LocalDate referenceDate, String scheduleName, LocalTime callTime) {
         EnrollmentRequest enrollmentRequest = new EnrollmentRequest()
                 .setScheduleName(scheduleName)
                 .setExternalId(motherId)
                 .setPreferredAlertTime(new Time(callTime))
                 .setReferenceDate(referenceDate);
         log.info(MessageFormat.format("Enrolling mother with ID: {0} to schedule: {1}, reference date: {2}, preferred call time: {3}",
                 motherId, scheduleName, referenceDate, callTime));
         scheduleTrackingService.enroll(enrollmentRequest);
     }
 }
