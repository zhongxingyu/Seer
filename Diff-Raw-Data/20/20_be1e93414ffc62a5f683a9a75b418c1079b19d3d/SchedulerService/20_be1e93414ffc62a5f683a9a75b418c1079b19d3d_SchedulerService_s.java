 package gnutch.quartz;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Properties;
 
 import org.quartz.Job;
 import org.quartz.JobBuilder;
 import org.quartz.JobDetail;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 import org.quartz.JobKey;
 import org.quartz.Scheduler;
 import org.quartz.SchedulerException;
 import org.quartz.SchedulerFactory;
 import org.quartz.SimpleScheduleBuilder;
 import org.quartz.Trigger;
 import org.quartz.TriggerBuilder;
 import org.quartz.impl.StdSchedulerFactory;
 import org.quartz.spi.JobFactory;
 import org.quartz.spi.TriggerFiredBundle;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * This is a Java Bean which has these fields: <br/>
  * 	timeoutListeners (List<TimeoutListener>>), <br/>
  * 	properties (java.util.Properties) and <br/>
  * 	quartzScheduler of type of Scheduler. <br/>
  * TimeoutListener is a simple interface with single method onTimeout, which is triggered upon scheduled event. <br/>
  * Properties defines all scheduled jobs and timeouts in form: <br/>
  * - 66.txt = 5 minutes <br/>
  * - 67.txt = 1 hour 5 minutes 10 seconds <br/>
  * - 68.txt = 2 months <br/>
  * quartzScheduler field is set in bean constructor using this code: <br/>
  * <br/>
  * SchedulerFactory sf = new StdSchedulerFactory(); <br/>
  * Scheduler sched = sf.getScheduler(); <br/>
  * <br/>
  * For `66.txt = 5 minutes` quartz should trigger every 5 minutes and TimeoutListener#onTimeout should be called for all listeners <br/>
  * with `66.txt` as `String key` parameter. <br/>
  * <br/>
  * @author rumen.dimov.mail@gmail.com
  * @version %I%, %G%
  */
 public class SchedulerService {
 
     Logger LOG = LoggerFactory.getLogger(SchedulerService.class);
     /**
      * Generic Scheduler Job that invokes all the associated TimeoutListener instances.
      */
     private class TimeoutListenerInvokingJob implements Job {
 
         @Override
         public void execute(JobExecutionContext context) throws JobExecutionException {
             String key = context.getJobDetail().getKey().getName();
             LOG.trace("Triggering job with key:" + key);
             synchronized(SchedulerService.this) {
                 TimeoutListener listener = null;
                 Iterator<TimeoutListener> it = timeoutListeners.iterator();
                 while(it.hasNext()){
                     listener = it.next();
                     listener.onTimeout(key);
                 }
             }
         }
     }
 
    protected List<TimeoutListener> timeoutListeners; // we need it protected for testin'
     private Properties properties;
     private Scheduler quartzScheduler;
 
     /**
      * Default constructor.
      *
      * @throws SchedulerException
      */
     public SchedulerService() throws SchedulerException {
         SchedulerFactory sf = new StdSchedulerFactory();
         quartzScheduler = sf.getScheduler();
         // The following is needed because Class#newInstance() does not work for inner classes
         quartzScheduler.setJobFactory(new JobFactory(){
         	   public Job newJob(TriggerFiredBundle paramTriggerFiredBundle,
                               Scheduler paramScheduler) throws SchedulerException {
                 return new TimeoutListenerInvokingJob();
         	   }
         });
         timeoutListeners = Collections.synchronizedList(new ArrayList<TimeoutListener>());
         quartzScheduler.start();
     }
 
     /**
      * Stores the TimeoutListener under the given key name for later use.
      *
      * @param key
      * @param listener
      */
     public void addTimeoutListener(TimeoutListener listener) {
         if((listener != null) && (timeoutListeners.contains(listener) == false)){
             LOG.trace("Adding TimeoutListener");
             timeoutListeners.add(listener);
         } else {
             LOG.trace("Ignoring to add TimeoutListener");
         }
     }
 
     /**
      * Stores the TimeoutListener under the given key name for later use.
      *
      * @param key
      * @param listener
      */
     public void removeTimeoutListener(TimeoutListener listener) {
         if((listener != null) && (timeoutListeners.contains(listener) == true)){
             LOG.trace("Removing TimeoutListener");
             timeoutListeners.remove(listener);
         } else {
             LOG.trace("Ignoring to remove TimeoutListener");
         }
     }
 
     /**
      * @return the properties
      */
     public Properties getProperties() {
         return properties;
     }
 
     /**
      * Properties defines all scheduled jobs and timeouts in form: <br/>
      * - 66.txt = 5 minutes <br/>
      * - 67.txt = 1 hour 5 minutes 10 seconds <br/>
      * - 68.txt = 2 months <br/>
      * <br/>
      * For example: `66.txt = 5 minutes` means a job is triggered every 5 minutes and TimeoutListener#onTimeout is called <br/>
      * for all listeners having `66.txt` as `String key` parameter. <br/>
      * <br/>
      * This method "merges" the jobs defined in the Properties with the jobs already running.<br/>
      * So, if you "reschedule" a job, it first unschedules the running job and then schedules it again using the new trigger parameter.<br/>
      * The jobs are distinguished by the Property keys used as their names in the Scheduler.<br/>
      * <br/>
      * <b>Given the above, rescheduling a job within 100 ms around its fire point may result in extra fires.<b/><br/>
      * <br/>
      * @param properties the properties to set
      * @throws SchedulerException
      *
      * @see {@link #addTimeoutListener(String, TimeoutListener)}
      */
     public synchronized void setProperties(Properties properties) throws SchedulerException {
         if(properties == null) {
             this.properties = null;
         } else {
             this.properties = (Properties)properties.clone(); // shallow copy, prevent from modifying behind the scenes
             for(Entry entry : this.properties.entrySet()) {
                 String key = entry.getKey().toString().trim();
                 scheduleJob(key, (String)entry.getValue());
             }
         }
     }
 
     /**
      * Schedules the job named `key` with timeout `timeout`
      *
      * @return Date object on which scheduled job will invoke
      */
     public Date scheduleJob(String key, String timeout) throws SchedulerException {
         // Remove the indicated Trigger from the scheduler
         // If the related job does not have any other triggers, and the job is not durable, then the job will also be deleted
         quartzScheduler.deleteJob(new JobKey(key,key)); // see how we define a new TriggerKey below
         // Define the new job
         // Note the Scheduler needs a custom JobFactory to instantiate the inner class below
         JobDetail job = JobBuilder.newJob(TimeoutListenerInvokingJob.class)
             .withIdentity(key, key) // name "key", group "key"
             .build();
         Long interval = StringDateUtils.fromStringToIntervalMs(timeout);
         LOG.trace("Job is scheduling to be fired in " + timeout + " interval, which is:" + interval + " msec");
         // Trigger the job to run now and forever through regular intervals
         Trigger trigger = TriggerBuilder.newTrigger()
             .withIdentity(key, key) // name "key", group "key"
             .startNow()
             .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                           .withIntervalInMilliseconds(interval)
                           .repeatForever())
             .build();
         // Schedule the job
         return quartzScheduler.scheduleJob(job, trigger);
     }
 
     /**
      * @return the quartzScheduler
      */
     public Scheduler getQuartzScheduler() {
         return quartzScheduler;
     }
 }
