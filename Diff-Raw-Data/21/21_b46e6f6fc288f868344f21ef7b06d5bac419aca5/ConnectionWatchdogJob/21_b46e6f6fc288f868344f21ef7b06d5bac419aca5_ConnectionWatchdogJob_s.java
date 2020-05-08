 package de.paluch.midi.relay.job;
 
 import de.paluch.midi.relay.relay.ETHRLY16;
 import org.apache.log4j.Logger;
 import org.quartz.Job;
 import org.quartz.JobDataMap;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 /**
  * @author <a href="mailto:mark.paluch@1und1.de">Mark Paluch</a>
  * @since 09.11.12 19:20
  */
 public class ConnectionWatchdogJob implements Job {
 
     private Logger log = Logger.getLogger(getClass());
 
 
     @Override
     public void execute(JobExecutionContext context) throws JobExecutionException {
 
        log.info("keepaliveOrClose");
         JobDataMap data = context.getJobDetail().getJobDataMap();
         ETHRLY16 ethrly16 = (ETHRLY16) data.get("ethrly16");
         ethrly16.keepaliveOrClose();
     }
 }
