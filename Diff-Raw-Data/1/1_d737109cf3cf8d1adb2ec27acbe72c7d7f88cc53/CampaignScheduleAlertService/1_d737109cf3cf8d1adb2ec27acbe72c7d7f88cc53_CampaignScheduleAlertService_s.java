 package org.motechproject.ananya.reports.kilkari.service;
 
 import org.joda.time.DateTime;
 import org.motechproject.ananya.reports.kilkari.contract.request.CampaignScheduleAlertRequest;
 import org.motechproject.ananya.reports.kilkari.domain.dimension.CampaignDimension;
 import org.motechproject.ananya.reports.kilkari.domain.dimension.CampaignScheduleAlertDetails;
 import org.motechproject.ananya.reports.kilkari.domain.dimension.DateDimension;
 import org.motechproject.ananya.reports.kilkari.domain.dimension.TimeDimension;
 import org.motechproject.ananya.reports.kilkari.repository.*;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 @Service
 public class CampaignScheduleAlertService {
 
     private AllCampaignScheduleAlerts allCampaignScheduleAlerts;
     private AllSubscriptions allSubscriptions;
     private AllDateDimensions allDateDimensions;
     private AllTimeDimensions allTimeDimensions;
     private AllCampaignDimensions allCampaignDimensions;
     private SubscriptionService subscriptionService;
 
     public CampaignScheduleAlertService() {
     }
 
     @Autowired
     public CampaignScheduleAlertService(AllCampaignScheduleAlerts allCampaignScheduleAlerts, AllSubscriptions allSubscriptions, AllDateDimensions allDateDimensions, AllTimeDimensions allTimeDimensions, AllCampaignDimensions allCampaignDimensions, SubscriptionService subscriptionService) {
         this.allCampaignScheduleAlerts = allCampaignScheduleAlerts;
         this.allSubscriptions = allSubscriptions;
         this.allDateDimensions = allDateDimensions;
         this.allTimeDimensions = allTimeDimensions;
         this.allCampaignDimensions = allCampaignDimensions;
         this.subscriptionService = subscriptionService;
     }
 
     @Transactional
     public void createCampaignScheduleAlert(CampaignScheduleAlertRequest campaignScheduleAlertRequest) {
         DateTime scheduledTime = campaignScheduleAlertRequest.getScheduledTime();
         DateDimension dateDimension = allDateDimensions.fetchFor(scheduledTime);
         TimeDimension timeDimension = allTimeDimensions.fetchFor(scheduledTime);
         CampaignDimension campaignDimension = allCampaignDimensions.fetchFor(campaignScheduleAlertRequest.getCampaignId());
         CampaignScheduleAlertDetails campaignScheduleAlertDetails = new CampaignScheduleAlertDetails(
                 allSubscriptions.findBySubscriptionId(campaignScheduleAlertRequest.getSubscriptionId()),
                 campaignDimension,
                 dateDimension,
                 timeDimension);
         allCampaignScheduleAlerts.save(campaignScheduleAlertDetails);
         subscriptionService.updateLastScheduledMessageDate(campaignScheduleAlertRequest.getSubscriptionId() ,campaignScheduleAlertRequest.getScheduledTime());
     }
 
     public void deleteFor(Long msisdn) {
         allCampaignScheduleAlerts.deleteFor(msisdn);
     }
 }
