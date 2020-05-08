 package com.abudko.scheduled.jobs.huuto;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.abudko.scheduled.jobs.Scheduler;
 
 public class PublishScheduler implements Scheduler {
 
     private Logger log = LoggerFactory.getLogger(getClass());
     
 
     public void schedule() {
         log.info("********* Start scheduled scanning *******");
         try {
 
             // TODO
             
             log.info("********* End scheduled scanning *******");
 
         } catch (Exception e) {
             log.error("Exception happened during scheduled scan: ", e);
         }
     }
 }
