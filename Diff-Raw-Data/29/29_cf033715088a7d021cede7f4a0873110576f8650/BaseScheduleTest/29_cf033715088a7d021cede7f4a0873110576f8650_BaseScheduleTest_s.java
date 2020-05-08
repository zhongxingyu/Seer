 package org.wv.stepsovc.core;
 
 import ch.lambdaj.Lambda;
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.motechproject.appointments.api.model.jobs.AppointmentReminderJob;
 import org.motechproject.http.client.constants.EventSubjects;
 import org.motechproject.scheduler.MotechSchedulerServiceImpl;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.SimpleTrigger;
 import org.quartz.Trigger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.quartz.SchedulerFactoryBean;
 import org.wv.stepsovc.core.domain.Referral;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import static ch.lambdaj.Lambda.on;
 import static java.lang.String.format;
 import static org.hamcrest.Matchers.is;
 import static org.hamcrest.Matchers.isIn;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
public class BaseScheduleTest {
 
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
 
     protected void assertAlerts(List<TestJobDetail> actualJobDetails, List<TestAlert> expectedAlerts) {
 
         assertThat(actualJobDetails.size(), is(expectedAlerts.size()));
         List<Date> expectedDates = Lambda.extract(expectedAlerts, on(TestAlert.class).getAlertDate());
         for(TestJobDetail testJobDetail : actualJobDetails) {
             assertThat(testJobDetail.trigger().getStartTime(), isIn(expectedDates));
         }
     }
 
     protected TestAlert alert(DateTime dateTime) {
         return new TestAlert(dateTime.toDate());
     }
 
     protected List<TestJobDetail> captureAlertsForReferral(String enrollmentId, int count) {
         final Scheduler scheduler = schedulerFactoryBean.getScheduler();
         final String jobGroupName = MotechSchedulerServiceImpl.JOB_GROUP_NAME;
         List<TestJobDetail> alertTriggers = new ArrayList<TestJobDetail>();
 
         try {
         String[] jobNames = scheduler.getJobNames(jobGroupName);
         for (String jobName : jobNames) {
             for(int i=0;i<count;i++) {
                 String jobId = AppointmentReminderJob.getJobIdUsing(enrollmentId, Referral.VISIT_NAME, i);
                 if (jobName.contains(format("%s-%s", AppointmentReminderJob.SUBJECT, jobId))) {
                     Trigger[] triggersOfJob = scheduler.getTriggersOfJob(jobName, jobGroupName);
                     assertEquals(1, triggersOfJob.length);
                 alertTriggers.add(new TestJobDetail(triggersOfJob[0], scheduler.getJobDetail(jobName, jobGroupName)));
             }
             }
         }
         } catch (SchedulerException se) {
             throw new RuntimeException(se);
         }
         return alertTriggers;
     }
 
 }
