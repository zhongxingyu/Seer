 package pl.psnc.dl.wf4ever.monitoring;
 
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.doAnswer;
 import static org.mockito.Mockito.spy;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.invocation.InvocationOnMock;
 import org.mockito.stubbing.Answer;
 import org.quartz.CronTrigger;
 import org.quartz.JobDetail;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.Trigger;
 import org.quartz.impl.StdSchedulerFactory;
 
 import pl.psnc.dl.wf4ever.model.BaseTest;
 
 /**
  * Test the cron scheduler.
  * 
  * @author piotrekhol
  * 
  */
 public class MonitoringSchedulerTest extends BaseTest {
 
     /** The cron value provided in quartz.properties. */
    private static final Object EXPECTED_CRON_SCHEDULE = "0 0/2 8-17 * * ?";
 
     /** Jobs scheduled in the last call. */
     private ScheduledJobsAnswer answer;
 
 
     @Override
     @Before
     public void setUp()
             throws Exception {
         super.setUp();
         answer = new ScheduledJobsAnswer();
 
         StdSchedulerFactory schedulerFactory = spy(new StdSchedulerFactory());
         doAnswer(new Answer<Scheduler>() {
 
             @Override
             public Scheduler answer(InvocationOnMock invocation)
                     throws Throwable {
                 Scheduler scheduler = (Scheduler) spy(invocation.callRealMethod());
                 doAnswer(answer).when(scheduler).scheduleJob(any(JobDetail.class), any(Trigger.class));
                 return scheduler;
             }
         }).when(schedulerFactory).getScheduler();
         MonitoringScheduler.setSchedulerFactory(schedulerFactory);
     }
 
 
     /**
      * Test that the RO monitoring dispatcher is scheduled with a cron trigger.
      * 
      * @throws SchedulerException
      *             any scheduler exception
      */
     @Test
     public final void testStart()
             throws SchedulerException {
         MonitoringScheduler monitoringScheduler = MonitoringScheduler.getInstance();
 
         monitoringScheduler.start();
         Assert.assertEquals(1, answer.getJobs().size());
         JobDetail job = answer.getJobs().keySet().iterator().next();
         Assert.assertEquals(ResearchObjectMonitoringDispatcherJob.class, job.getJobClass());
         Trigger trigger = answer.getJobs().get(job);
         Assert.assertEquals(EXPECTED_CRON_SCHEDULE, ((CronTrigger) trigger).getCronExpression());
     }
 }
