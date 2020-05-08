 package org.bahmni.feed.openerp.job;
 
 
 import com.sun.syndication.io.FeedException;
 import org.springframework.stereotype.Controller;
 
 @Controller
 public class OpenERPSaleOrderFeedJob {
     private OpenMRSFeedJob openMRSFeedJob;
     private static final String jobName = "openerp.saleorder.service";
     private static String feedName;
 
    public OpenERPSaleOrderFeedJob() {
    }

    public OpenERPSaleOrderFeedJob(OpenMRSFeedJob openMRSFeedJob, String saleOrderFeedName) throws FeedException {
         this.openMRSFeedJob = openMRSFeedJob;
        this.feedName = saleOrderFeedName;
     }
 

     public void processFeed()  {
         openMRSFeedJob.processFeed(feedName, jobName);
     }
 
     public void processFailedEvents()  {
         openMRSFeedJob.processFailedEvents(feedName, jobName);
     }
 
 
 }
