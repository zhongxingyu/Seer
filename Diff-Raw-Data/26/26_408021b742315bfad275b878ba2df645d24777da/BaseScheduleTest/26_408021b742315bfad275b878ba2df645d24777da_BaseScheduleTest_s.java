 package org.wv.stepsovc.core;
 
 import ch.lambdaj.Lambda;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.motechproject.appointments.api.model.jobs.AppointmentReminderJob;
 import org.motechproject.scheduler.MotechSchedulerServiceImpl;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.Trigger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static ch.lambdaj.Lambda.on;
 import static java.lang.String.format;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.isIn;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 public abstract class BaseScheduleTest {
 
     @Autowired
     private SchedulerFactoryBean schedulerFactoryBean;
 
     @After
     public final void deleteAllJobs() throws SchedulerException {
 
         Scheduler scheduler = schedulerFactoryBean.getScheduler();
        for (String jobGroup : scheduler.getJobGroupNames()) {
            for (String jobName : scheduler.getJobNames(jobGroup)) {
                scheduler.deleteJob(jobName, jobGroup);
            }
        }
     }
 
     protected void assertAlerts(List<org.wv.stepsovc.core.TestJobDetail> actualJobDetails, List<org.wv.stepsovc.core.TestAlert> expectedAlerts) {
 
         assertThat(actualJobDetails.size(), is(expectedAlerts.size()));
         List<Date> expectedDates = Lambda.extract(expectedAlerts, on(org.wv.stepsovc.core.TestAlert.class).getAlertDate());
         for (org.wv.stepsovc.core.TestJobDetail testJobDetail : actualJobDetails) {
             assertThat(testJobDetail.trigger().getStartTime(), isIn(expectedDates));
         }
     }
 
     protected org.wv.stepsovc.core.TestAlert alert(DateTime dateTime) {
         return new org.wv.stepsovc.core.TestAlert(dateTime.toDate());
     }
 
     protected List<org.wv.stepsovc.core.TestJobDetail> captureAlertsForReferral(String enrollmentId, int count, String visitName) {
         final Scheduler scheduler = schedulerFactoryBean.getScheduler();
         final String jobGroupName = MotechSchedulerServiceImpl.JOB_GROUP_NAME;
         List<org.wv.stepsovc.core.TestJobDetail> alertTriggers = new ArrayList<org.wv.stepsovc.core.TestJobDetail>();
 
         try {
            String[] jobNames = scheduler.getJobNames(jobGroupName);
            for (String jobName : jobNames) {
                 for (int i = 0; i < count; i++) {
                     String jobId = AppointmentReminderJob.getJobIdUsing(enrollmentId, visitName, i);
                    if (jobName.contains(format("%s-%s", AppointmentReminderJob.SUBJECT, jobId))) {
                        Trigger[] triggersOfJob = scheduler.getTriggersOfJob(jobName, jobGroupName);
                        assertEquals(1, triggersOfJob.length);
                        alertTriggers.add(new org.wv.stepsovc.core.TestJobDetail(triggersOfJob[0], scheduler.getJobDetail(jobName, jobGroupName)));
                     }
                 }
             }
         } catch (SchedulerException se) {
             throw new RuntimeException(se);
         }
         return alertTriggers;
     }
 
 }
