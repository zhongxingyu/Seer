 package org.bahmni.feed.openerp.job;
 
 
 import com.sun.syndication.io.FeedException;
import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 
 @Controller
 public class OpenERPCustomerFeedJob {
     private OpenMRSFeedJob openMRSFeedJob;
     private static final String jobName = "openerp.customer.service";
     private String feedName;
 
    @Autowired
     public OpenERPCustomerFeedJob(OpenMRSFeedJob openMRSFeedJob, String feedName) throws FeedException {
         this.openMRSFeedJob = openMRSFeedJob;
         this.feedName = feedName;
     }
 
     public void processFeed()  {
         openMRSFeedJob.processFeed(feedName, jobName);
     }
 
     public void processFailedEvents()  {
         openMRSFeedJob.processFailedEvents(feedName, jobName);
     }
 
 }
