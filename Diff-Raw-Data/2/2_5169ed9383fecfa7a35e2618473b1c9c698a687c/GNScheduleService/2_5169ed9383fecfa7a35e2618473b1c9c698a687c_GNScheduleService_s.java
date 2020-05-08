 package org.motechproject.ghana.national.web.service;
 
 import org.motechproject.MotechException;
 import org.motechproject.ghana.national.configuration.ScheduleNames;
 import org.motechproject.ghana.national.domain.Patient;
 import org.motechproject.ghana.national.service.PatientService;
 import org.motechproject.ghana.national.web.domain.Alert;
 import org.motechproject.ghana.national.web.domain.JobDetail;
 import org.motechproject.openmrs.advice.ApiSession;
 import org.motechproject.openmrs.advice.LoginAsAdmin;
 import org.motechproject.scheduler.MotechSchedulerServiceImpl;
 import org.motechproject.scheduletracking.api.domain.Enrollment;
 import org.motechproject.scheduletracking.api.domain.EnrollmentStatus;
 import org.motechproject.scheduletracking.api.domain.WindowName;
 import org.motechproject.scheduletracking.api.events.constants.EventDataKeys;
 import org.motechproject.scheduletracking.api.events.constants.EventSubjects;
 import org.motechproject.scheduletracking.api.repository.AllEnrollments;
 import org.quartz.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 import org.springframework.stereotype.Service;
 
 import java.lang.reflect.Field;
 import java.util.*;
 import java.util.Calendar;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static java.lang.String.format;
 
 @Service
 public class GNScheduleService {
 
     @Autowired
     private AllEnrollments allEnrollments;
 
     @Autowired
     private SchedulerFactoryBean schedulerFactoryBean;
 
     @Autowired
     private PatientService patientService;
 
     private Pattern ALERT_ORDER_INDEX_REGEX = Pattern.compile("^.*\\.(.*?)-repeat$");
 
     public void filterActiveSchedulesWithin(Map<String, Map<String, List<Alert>>> patientSchedules, Date startDate, Date endDate) {
         for (Map<String, List<Alert>> schedules : patientSchedules.values()) {
             for (Map.Entry<String, List<Alert>> scheduleEntry : schedules.entrySet()) {
                 for (Alert alert : scheduleEntry.getValue()) {
                     if (alert.getAlertDate().after(endDate) || alert.getAlertDate().before(startDate)) {
                         scheduleEntry.getValue().remove(alert);
                     }
                 }
                 if (scheduleEntry.getValue().size() == 0) {
                     schedules.remove(scheduleEntry.getKey());
                 }
             }
         }
     }
 
     @LoginAsAdmin
     @ApiSession
     public Map<String, Map<String, List<Alert>>> getAllActiveSchedules() {
         final List<Enrollment> enrollments = allEnrollments.getAll();
         Map<String, Map<String, List<Alert>>> schedules = new HashMap<String, Map<String, List<Alert>>>();
         for (Enrollment enrollment : enrollments) {
             if (EnrollmentStatus.ACTIVE.equals(enrollment.getStatus())) {
                 final Map<String, List<Alert>> alerts = getAllSchedulesByMrsPatientId(enrollment.getExternalId());
                 schedules.put(patientService.patientByOpenmrsId(enrollment.getExternalId()).getMotechId(), alerts);
             }
         }
         return schedules;
     }
 
     @LoginAsAdmin
     @ApiSession
     public Map<String, List<Alert>> getAllSchedulesByMotechId(final String patientId) {
         Patient patientByMotechId = patientService.getPatientByMotechId(patientId);
         return getAllSchedulesByMrsId(patientByMotechId.getMRSPatientId());
     }
 
     public Map<String, List<Alert>> getAllSchedulesByMrsId(String mrsPatientId) {
         return getAllSchedulesByMrsPatientId(mrsPatientId);
     }
 
     private Map<String, List<Alert>> getAllSchedulesByMrsPatientId(String patientId) {
         Map<String, List<Alert>> schedules = new ConcurrentHashMap<String, List<Alert>>();
         try {
             for (Field field : ScheduleNames.class.getFields()) {
                final String scheduleName = (String) field.get(field);
                 Enrollment activeEnrollment = allEnrollments.getActiveEnrollment(patientId, scheduleName);
                 if (activeEnrollment != null) {
                     schedules.put(scheduleName + "(" + activeEnrollment.getCurrentMilestoneName() + ")", captureAlertsForNextMilestone(activeEnrollment.getId()));
                 }
             }
         } catch (Exception e) {
             throw new MotechException("Encountered exception, ", e);
         }
         return schedules;
     }
 
 
     private List<Alert> captureAlertsForNextMilestone(String enrollmentId) throws SchedulerException {
         final Scheduler scheduler = schedulerFactoryBean.getScheduler();
         final String jobGroupName = MotechSchedulerServiceImpl.JOB_GROUP_NAME;
         String[] jobNames = scheduler.getJobNames(jobGroupName);
         List<org.motechproject.ghana.national.web.domain.JobDetail> alertTriggers = new ArrayList<JobDetail>();
 
         for (String jobName : jobNames) {
             if (jobName.contains(format("%s-%s", EventSubjects.MILESTONE_ALERT, enrollmentId))) {
                 Trigger[] triggersOfJob = scheduler.getTriggersOfJob(jobName, jobGroupName);
                 alertTriggers.add(new org.motechproject.ghana.national.web.domain.JobDetail((SimpleTrigger) triggersOfJob[0], scheduler.getJobDetail(jobName, jobGroupName)));
             }
         }
         return createActualTestAlertTimes(alertTriggers);
     }
 
     private List<Alert> createActualTestAlertTimes(List<org.motechproject.ghana.national.web.domain.JobDetail> alertsJobDetails) {
         sortBasedOnIndexInAlertName(alertsJobDetails);
 
         List<Alert> actualAlertTimes = new CopyOnWriteArrayList<Alert>();
         for (org.motechproject.ghana.national.web.domain.JobDetail jobDetail : alertsJobDetails) {
             SimpleTrigger alert = jobDetail.trigger();
             Date nextFireTime = alert.getNextFireTime();
             JobDataMap dataMap = jobDetail.getJobDetail().getJobDataMap();
             actualAlertTimes.add(new Alert(window(dataMap), nextFireTime));
             for (int i = 1; i <= alert.getRepeatCount() - alert.getTimesTriggered(); i++) {
                 Calendar calendar = Calendar.getInstance();
                 calendar.setTime((Date) nextFireTime.clone());
                 calendar.add(Calendar.DAY_OF_MONTH, toDays(i * alert.getRepeatInterval()));
                 actualAlertTimes.add(new Alert(window(dataMap), calendar.getTime()));
             }
         }
         return actualAlertTimes;
     }
 
     private void sortBasedOnIndexInAlertName(List<org.motechproject.ghana.national.web.domain.JobDetail> alertJobDetails) {
         Collections.sort(alertJobDetails, new Comparator<org.motechproject.ghana.national.web.domain.JobDetail>() {
             @Override
             public int compare(org.motechproject.ghana.national.web.domain.JobDetail jobDetail1, org.motechproject.ghana.national.web.domain.JobDetail jobDetail2) {
                 return extractIndexFromAlertName(jobDetail1.trigger().getName()).compareTo(extractIndexFromAlertName(jobDetail2.trigger().getName()));
             }
         });
     }
 
     private Integer extractIndexFromAlertName(String name) {
         Matcher matcher = ALERT_ORDER_INDEX_REGEX.matcher(name);
         return matcher.find() ? Integer.parseInt(matcher.group(1)) : null;
     }
 
 
     private int toDays(long milliseconds) {
         return (int) (milliseconds / 1000 / 60 / 60 / 24);
     }
 
     private WindowName window(JobDataMap dataMap) {
         return WindowName.valueOf((String) dataMap.get(EventDataKeys.WINDOW_NAME));
     }
 }
