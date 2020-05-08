 package org.motechproject.ghana.national.web;
 
 import org.motechproject.MotechException;
 import org.motechproject.ghana.national.configuration.ScheduleNames;
import org.motechproject.ghana.national.repository.AllSchedules;
 import org.motechproject.scheduler.MotechSchedulerServiceImpl;
 import org.motechproject.scheduletracking.api.domain.WindowName;
 import org.motechproject.scheduletracking.api.events.constants.EventDataKeys;
 import org.motechproject.scheduletracking.api.events.constants.EventSubjects;
 import org.quartz.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.ResponseBody;
 
 import javax.servlet.http.HttpServletRequest;
 import java.io.UnsupportedEncodingException;
 import java.lang.reflect.Field;
 import java.text.ParseException;
 import java.util.*;
 import java.util.Calendar;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import static java.lang.String.format;
 
 @Controller
 @RequestMapping(value = "/schedule/")
 public class ScheduleController {
 
     @Autowired
     private SchedulerFactoryBean schedulerFactoryBean;
 
     @Autowired
    private AllSchedules allSchedules;
 
     private Pattern ALERT_ORDER_INDEX_REGEX = Pattern.compile("^.*\\.(.*?)-repeat$");
 
     @RequestMapping(value = "patient", method = RequestMethod.GET)
     @ResponseBody
     public String newStaff(HttpServletRequest request) throws UnsupportedEncodingException, ParseException {
         final String motechId = request.getParameter("id");
         final Map<String, List<TestAlert>> schedules = getAllSchedulesFor(motechId);
         return schedules.toString();
     }
 
     private Map<String, List<TestAlert>> getAllSchedulesFor(final String motechId) {
 
         Map<String, List<TestAlert>> schedules = new HashMap<String, List<TestAlert>>();
         try {
             for (Field field : ScheduleNames.class.getFields()) {
                 try {
                     final String scheduleName = (String) field.get(field);
                    String externalId = allSchedules.getActiveEnrollment(motechId, scheduleName).getExternalId();
                    schedules.put(scheduleName, captureAlertsForNextMilestone(externalId));
                 } catch (IllegalAccessException e) {
                     throw new MotechException("Encountered exception, ", e);
                 }
             }
         } catch (SchedulerException e) {
             throw new MotechException("Encountered exception, ", e);
         }
         return schedules;
     }
 
 
     private List<TestAlert> captureAlertsForNextMilestone(String enrollmentId) throws SchedulerException {
         final Scheduler scheduler = schedulerFactoryBean.getScheduler();
         final String jobGroupName = MotechSchedulerServiceImpl.JOB_GROUP_NAME;
         String[] jobNames = scheduler.getJobNames(jobGroupName);
         List<TestJobDetail> alertTriggers = new ArrayList<TestJobDetail>();
 
         for (String jobName : jobNames) {
             if (jobName.contains(format("%s-%s", EventSubjects.MILESTONE_ALERT, enrollmentId))) {
                 Trigger[] triggersOfJob = scheduler.getTriggersOfJob(jobName, jobGroupName);
                 alertTriggers.add(new TestJobDetail((SimpleTrigger) triggersOfJob[0], scheduler.getJobDetail(jobName, jobGroupName)));
             }
         }
         return createActualTestAlertTimes(alertTriggers);
     }
 
     private List<TestAlert> createActualTestAlertTimes(List<TestJobDetail> alertsJobDetails) {
         sortBasedOnIndexInAlertName(alertsJobDetails);
 
         List<TestAlert> actualAlertTimes = new ArrayList<TestAlert>();
         for (TestJobDetail testJobDetail : alertsJobDetails) {
             SimpleTrigger alert = testJobDetail.trigger();
             Date nextFireTime = alert.getNextFireTime();
             JobDataMap dataMap = testJobDetail.getJobDetail().getJobDataMap();
             actualAlertTimes.add(new TestAlert(window(dataMap), nextFireTime));
             for (int i = 1; i <= alert.getRepeatCount(); i++) {
                 Calendar calendar = Calendar.getInstance();
                 calendar.setTime((Date) nextFireTime.clone());
                 calendar.add(Calendar.DAY_OF_MONTH, toDays(i * alert.getRepeatInterval()));
                 actualAlertTimes.add(new TestAlert(window(dataMap), calendar.getTime()));
             }
         }
         return actualAlertTimes;
     }
 
     private void sortBasedOnIndexInAlertName(List<TestJobDetail> alertJobDetails) {
         Collections.sort(alertJobDetails, new Comparator<TestJobDetail>() {
             @Override
             public int compare(TestJobDetail testJobDetail1, TestJobDetail testJobDetail2) {
                 return extractIndexFromAlertName(testJobDetail1.trigger().getName()).compareTo(extractIndexFromAlertName(testJobDetail2.trigger().getName()));
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
 
     public class TestAlert {
         private WindowName window;
         private Date alertDate;
 
         public TestAlert(WindowName window, Date alertDate) {
             this.window = window;
             this.alertDate = alertDate;
         }
 
         @Override
         public String toString() {
             return "TestAlert{" +
                     "window=" + window +
                     ", alertDate=" + alertDate +
                     '}';
         }
     }
 
     public class TestJobDetail {
 
         SimpleTrigger trigger;
         JobDetail jobDetail;
 
         public TestJobDetail(SimpleTrigger trigger, JobDetail jobDetail) {
             this.trigger = trigger;
             this.jobDetail = jobDetail;
         }
 
         public JobDetail getJobDetail() {
             return jobDetail;
         }
 
         public SimpleTrigger trigger() {
             return trigger;
         }
     }
 
 }
