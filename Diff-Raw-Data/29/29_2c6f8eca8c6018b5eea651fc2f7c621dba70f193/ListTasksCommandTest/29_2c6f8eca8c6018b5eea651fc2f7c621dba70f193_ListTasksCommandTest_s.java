 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.quartz.application;
 
 import static org.easymock.classextension.EasyMock.*;
 
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.CoreMatchers.*;
 
 import java.io.StringWriter;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.LinkedList;
 
 import org.json.JSONArray;
 import org.json.JSONObject;
 import org.json.JSONException;
 
 import org.junit.Test;
 
 import com.globant.katari.core.application.JsonRepresentation;
 import com.globant.katari.quartz.domain.ScheduledCommand;
 
 import org.quartz.JobDataMap;
 import org.quartz.JobDetail;
 import org.quartz.JobExecutionContext;
 import org.quartz.Scheduler;
 import org.quartz.Trigger;
 import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
 
 /** @author waabox (emiliano[dot]arango[at]globant[dot]com)
  */
@SuppressWarnings("deprecation")
 public class ListTasksCommandTest {
 
   // This was created as new Date(110, 9, 20, 13, 30, 0), but we need to
   // guarantee a specific timezone, because the test expects a specific date.
   //
   // The other form failed in CI during a mvn site run. It looks like something
   // in maven is fiddling with the timezones.
   private Date last = new Date(1287592200000L);
 
   // This was created as new Date(110, 9, 20, 15, 30, 0), but we need to
   // guarantee a specific timezone, because the test expects a specific date.
   private Date next = new Date(1287599400000L);
 
   /** Creates a scheduler that contains the provided command, and a null
    * previous fire time if nullPreviousTime is null.
    */
   private Scheduler createScheduler(final ScheduledCommand command,
       final boolean nullPreviousTime) throws Exception {
     Scheduler scheduler = createMock(Scheduler.class);
     JobDetail detail1 = createMock(JobDetail.class);
     JobDetail detail2 = createMock(JobDetail.class);
 
     JobExecutionContext context = createMock(JobExecutionContext.class);
     expect(context.getJobDetail()).andReturn(detail1).anyTimes();
     replay(context);
 
     JobDataMap dataMap = createMock(JobDataMap.class);
     MethodInvokingJobDetailFactoryBean methodInvoking;
     methodInvoking = createMock(MethodInvokingJobDetailFactoryBean.class);
     Trigger trigger = createMock(Trigger.class);
 
     String group1 = "group1";
     String trigger1 = "trigger1";
     String[] jobs = new String[] { "job1", "job2" };
     String[] groups = new String[] { group1 };
     String[] triggers = new String[] { trigger1 };
 
     LinkedList<JobExecutionContext> runningJobs;
     runningJobs = new LinkedList<JobExecutionContext>();
     runningJobs.add(context);
 
     expect(scheduler.getCurrentlyExecutingJobs()).andReturn(runningJobs);
     expect(scheduler.getJobGroupNames()).andReturn(groups);
     expect(scheduler.getTriggerNames(group1)).andReturn(triggers);
     expect(scheduler.getJobNames(group1)).andReturn(jobs);
     expect(scheduler.getJobDetail("job1", group1)).andReturn(detail1);
     expect(scheduler.getJobDetail("job2", group1)).andReturn(detail2);
     expect(detail1.getJobDataMap()).andReturn(dataMap);
     expect(detail2.getJobDataMap()).andReturn(dataMap);
     expect(dataMap.get("methodInvoker")).andReturn(methodInvoking).anyTimes();
     expect(methodInvoking.getTargetObject()).andReturn(command).anyTimes();
     expect(scheduler.getTrigger(trigger1, group1)).andReturn(trigger)
       .anyTimes();
     String jobName = "jobName";
     expect(trigger.getJobName()).andReturn(jobName);
     expect(trigger.getJobName()).andReturn("jobName2");
     expect(detail1.getName()).andReturn(jobName);
     expect(detail2.getName()).andReturn("jobName2");
     expect(trigger.getNextFireTime()).andReturn(next).anyTimes();
     if (nullPreviousTime) {
       expect(trigger.getPreviousFireTime()).andReturn(null).anyTimes();
     } else {
       expect(trigger.getPreviousFireTime()).andReturn(last).anyTimes();
     }
 
     replay(scheduler);
     replay(detail1);
     replay(detail2);
     replay(dataMap);
     replay(methodInvoking);
     replay(trigger);
 
     return scheduler;
   }
 
   @Test
   public void testExecute() throws Exception {
 
     ScheduledCommand job = createMock(ScheduledCommand.class);
     expect(job.getProgressPercent()).andReturn(new Integer(4)).anyTimes();
     expect(job.getDisplayName()).andReturn("The friendly name").anyTimes();
     Map<String, String> information = new HashMap<String,String>();
     information.put("Processing row","104");
     expect(job.getInformation()).andReturn(information).anyTimes();
     replay(job);
 
     ListTasksCommand command;
     command = new ListTasksCommand(createScheduler(job, false));
 
     JsonRepresentation result = command.execute();
 
     StringWriter writer = new StringWriter();
     result.write(writer);
 
     assertThat(writer.toString(), is(baselineJson(true, true)));
   }
 
   @Test
   public void testExecute_noProgress() throws Exception {
 
     ScheduledCommand job = createMock(ScheduledCommand.class);
     expect(job.getProgressPercent()).andReturn(null).anyTimes();
     expect(job.getDisplayName()).andReturn("The friendly name").anyTimes();
     Map<String, String> information = new HashMap<String,String>();
     information.put("Processing row","104");
     expect(job.getInformation()).andReturn(information).anyTimes();
     replay(job);
 
     ListTasksCommand command;
     command = new ListTasksCommand(createScheduler(job, false));
 
     JsonRepresentation result = command.execute();
 
     StringWriter writer = new StringWriter();
     result.write(writer);
 
     assertThat(writer.toString(), is(baselineJson(false, true)));
   }
 
   @Test
   public void testExecute_noFireTime() throws Exception {
 
     ScheduledCommand job = createMock(ScheduledCommand.class);
     expect(job.getProgressPercent()).andReturn(null).anyTimes();
     expect(job.getDisplayName()).andReturn("The friendly name").anyTimes();
     Map<String, String> information = new HashMap<String,String>();
     information.put("Processing row","104");
     expect(job.getInformation()).andReturn(information).anyTimes();
     replay(job);
 
     ListTasksCommand command;
     command = new ListTasksCommand(createScheduler(job, true));
 
     JsonRepresentation result = command.execute();
 
     StringWriter writer = new StringWriter();
     result.write(writer);
 
     assertThat(writer.toString(), is(baselineJson(false, false)));
   }
 
   /** Creates the baseline json string, a string with a sample json object.
    *
    * @return the json string.
    *
    * @throws JSONException
    */
   private String baselineJson(final boolean isRunning,
       final boolean hasLastTime) throws JSONException {
 
     JSONObject information = new JSONObject();
     information.put("Processing row", "104");
 
     JSONObject task1 = new JSONObject();
     JSONObject task2 = new JSONObject();
     if (isRunning) {
       task1.put("progressPercent", 4);
       task2.put("progressPercent", 4);
     }
     task1.put("groupName", "group1");
     task2.put("groupName", "group1");
     task1.put("jobName", "job1");
     task2.put("jobName", "job2");
     task1.put("friendlyName", "The friendly name");
     task2.put("friendlyName", "The friendly name");
     task1.put("isRunning", true);
     task2.put("isRunning", false);
     task1.put("information", information);
     task2.put("information", information);
     task1.put("nextExecutionTime", "2010-10-20T18:30:00Z");
     task2.put("nextExecutionTime", "2010-10-20T18:30:00Z");
     if (hasLastTime) {
       task1.put("lastExecutionTime", "2010-10-20T16:30:00Z");
       task2.put("lastExecutionTime", "2010-10-20T16:30:00Z");
     }
     JSONArray tasks = new JSONArray();
     tasks.put(task1);
     tasks.put(task2);
 
     return tasks.toString();
   }
 }
 
