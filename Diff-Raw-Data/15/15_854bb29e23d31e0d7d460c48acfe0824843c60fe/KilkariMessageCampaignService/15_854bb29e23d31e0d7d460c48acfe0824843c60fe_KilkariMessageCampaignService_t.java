 package org.motechproject.ananya.kilkari.messagecampaign.service;
 
 import org.joda.time.DateTime;
 import org.motechproject.ananya.kilkari.messagecampaign.mapper.KilkariMessageCampaignRequestMapper;
 import org.motechproject.ananya.kilkari.messagecampaign.request.KilkariMessageCampaignRequest;
 import org.motechproject.server.messagecampaign.service.MessageCampaignService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 @Service
 public class KilkariMessageCampaignService {
 
     private MessageCampaignService campaignService;
 
     @Autowired
     public KilkariMessageCampaignService(MessageCampaignService campaignService) {
         this.campaignService = campaignService;
     }
 
     public Boolean start(KilkariMessageCampaignRequest campaignRequest) {
         campaignService.startFor(KilkariMessageCampaignRequestMapper.map(campaignRequest));
         return true;
     }
 
     public Boolean stop(KilkariMessageCampaignRequest enrollRequest) {
         campaignService.stopAll(KilkariMessageCampaignRequestMapper.map(enrollRequest));
         return true;
     }
 
     public List<DateTime> getMessageTimings(String subscriptionId, String campaignName) {
        // TODO: to remove comment and remove the dummy lines below after platform fix
//        List<Date> dateList = campaignService.getMessageTimings(subscriptionId, campaignName);
        List<Date> dateList = new ArrayList<Date>();
        dateList.add(new Date(DateTime.now().getMillis()));
        dateList.add(new Date(DateTime.now().getMillis()));
        dateList.add(new Date(DateTime.now().getMillis()));
         List<DateTime> messageTimings = new ArrayList<DateTime>();
         for (Date date : dateList) {
             messageTimings.add(new DateTime(date.getTime()));
         }
         return messageTimings;
     }
 }
