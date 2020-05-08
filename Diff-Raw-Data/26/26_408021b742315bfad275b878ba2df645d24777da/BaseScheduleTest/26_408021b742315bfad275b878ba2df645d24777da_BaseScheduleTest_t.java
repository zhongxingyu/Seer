 package org.wv.stepsovc.core;
 
 import ch.lambdaj.Lambda;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.motechproject.appointments.api.model.jobs.AppointmentReminderJob;
 import org.motechproject.scheduler.MotechSchedulerServiceImpl;
import org.quartz.JobKey;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
import java.util.Set;
 
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
        scheduler.clear();
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
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.jobGroupEquals(jobGroupName));
            for (JobKey jobKey : jobKeys) {
                 for (int i = 0; i < count; i++) {
                     String jobId = AppointmentReminderJob.getJobIdUsing(enrollmentId, visitName, i);
                    if (jobKey.getName().contains(format("%s-%s", AppointmentReminderJob.SUBJECT, jobId))) {
                        List<Trigger> triggersOfJob = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                        assertEquals(1, triggersOfJob.size());
                        alertTriggers.add(new org.wv.stepsovc.core.TestJobDetail(triggersOfJob.get(0), scheduler.getJobDetail(jobKey)));
                     }
                 }
             }
         } catch (SchedulerException se) {
             throw new RuntimeException(se);
         }
         return alertTriggers;
     }
 
 }
