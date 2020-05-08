 package org.bahmni.feed.openerp.job;
 
 
 import org.apache.commons.lang3.exception.ExceptionUtils;
 import org.apache.log4j.Logger;
 import org.bahmni.feed.openerp.FeedException;
 import org.bahmni.feed.openerp.TaskMonitor;
 import org.bahmni.feed.openerp.client.AtomFeedClientHelper;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 
 public class OpenMRSFeedJob {
     private static Logger logger = Logger.getLogger(OpenMRSFeedJob.class);
 
     private AtomFeedClientHelper atomFeedClientHelper;
     TaskMonitor taskMonitor;
 
     @Autowired
     public OpenMRSFeedJob(
             AtomFeedClientHelper atomFeedClientHelper,
             TaskMonitor taskMonitor) throws com.sun.syndication.io.FeedException {
         this.atomFeedClientHelper = atomFeedClientHelper;
         this.taskMonitor = taskMonitor;
     }
 
     public void processFeed(String feedName, String jobName) {
         try {
             taskMonitor.startTask();
            logger.info("Processing Customer Feed");
 
             atomFeedClientHelper.getAtomFeedClient(feedName, jobName).processEvents();
         } catch (Exception e) {
            logger.error("failed customer feed execution ", e);
             handleAuthorizationException(e, feedName, jobName);
         } finally {
             taskMonitor.endTask();
         }
     }
 
     public void processFailedEvents(String feedName, String jobName) {
         try {
             taskMonitor.startTask();
            logger.info("Processing failed events for Customer Feed");
 
             atomFeedClientHelper.getAtomFeedClient(feedName, jobName).processFailedEvents();
         } catch (Exception e) {
            logger.error("failed customer feed execution ", e);
             handleAuthorizationException(e, feedName, jobName);
         } finally {
             taskMonitor.endTask();
         }
     }
 
     protected void handleAuthorizationException(Throwable e, String feedName, String jobName) throws FeedException {
         if (e != null && ExceptionUtils.getStackTrace(e).contains("HTTP response code: 401")) {
             atomFeedClientHelper.reInitializeAtomFeedClient(feedName, jobName);
         }
     }
 }
