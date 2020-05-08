 package gnutch.quartz;
 
 import java.util.List;
 import java.util.Iterator;
 
 import org.quartz.Job;
 import org.quartz.JobDataMap;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import gnutch.quartz.SchedulerService;
 import gnutch.quartz.TimeoutListener;
 
 /**
  * Generic Scheduler Job that invokes all the associated TimeoutListener instances.
  */
class TimeoutListenerInvokingJob implements Job {
     private static Logger LOG = LoggerFactory.getLogger(TimeoutListenerInvokingJob.class);
 
     @Override
     public void execute(JobExecutionContext context) throws JobExecutionException {
         JobDataMap map = context.getJobDetail().getJobDataMap();
 
         String key = context.getJobDetail().getKey().getName();
         List<TimeoutListener> timeoutListeners = (List<TimeoutListener>)map.get("timeoutListeners");
         System.out.println("Listeners:" + timeoutListeners);
         LOG.trace("Triggering job with key: " + key);
         synchronized(timeoutListeners) {
             TimeoutListener listener = null;
             Iterator<TimeoutListener> it = timeoutListeners.iterator();
             while(it.hasNext()){
                 listener = it.next();
                 listener.onTimeout(key);
             }
         }
     }
 }
