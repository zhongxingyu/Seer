 package org.motechproject.ananya.kilkari.messagecampaign.service;
 
 import org.joda.time.DateTime;
 import org.motechproject.ananya.kilkari.messagecampaign.contract.MessageCampaignEnrollment;
 import org.motechproject.ananya.kilkari.messagecampaign.contract.MessageCampaignRequest;
 import org.motechproject.ananya.kilkari.messagecampaign.contract.mapper.MessageCampaignRequestMapper;
 import org.motechproject.ananya.kilkari.messagecampaign.domain.SubscriptionPack;
 import org.motechproject.server.messagecampaign.service.CampaignEnrollmentRecord;
 import org.motechproject.server.messagecampaign.service.CampaignEnrollmentsQuery;
 import org.motechproject.server.messagecampaign.service.MessageCampaignService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 
 @Service
 public class KilkariMessageCampaignService {
 
     public static final String FIFTEEN_MONTHS = "kilkari-mother-child-campaign-fifteen-months";
     public static final String TWELVE_MONTHS = "kilkari-mother-child-campaign-twelve-months";
     public static final String SEVEN_MONTHS = "kilkari-mother-child-campaign-seven-months";
     public static final String CAMPAIGN_MESSAGE_NAME = "Mother Child Health Care";
     public int campaignScheduleDeltaDays;
     public int campaignScheduleDeltaMinutes;
 
     @Value("#{kilkariProperties['kilkari.campaign.schedule.delta.days']}")
     public void setCampaignScheduleDeltaDays(int campaignScheduleDeltaDays) {
         this.campaignScheduleDeltaDays = campaignScheduleDeltaDays;
     }
 
     @Value("#{kilkariProperties['kilkari.campaign.schedule.delta.minutes']}")
     public void setCampaignScheduleDeltaMinutes(int campaignScheduleDeltaMinutes) {
         this.campaignScheduleDeltaMinutes = campaignScheduleDeltaMinutes;
     }
 
     private MessageCampaignService campaignService;
 
     @Autowired
     public KilkariMessageCampaignService(MessageCampaignService campaignService) {
         this.campaignService = campaignService;
     }
 
     public void start(MessageCampaignRequest campaignRequest) {
         campaignService.startFor(MessageCampaignRequestMapper.newRequestFrom(campaignRequest, campaignScheduleDeltaDays, campaignScheduleDeltaMinutes));
     }
 
     public boolean stop(MessageCampaignRequest enrollRequest) {
         campaignService.stopAll(MessageCampaignRequestMapper.newRequestFrom(enrollRequest, campaignScheduleDeltaDays, campaignScheduleDeltaMinutes));
         return true;
     }
 
     public MessageCampaignEnrollment searchEnrollment(String externalId, String campaignName) {
         List<CampaignEnrollmentRecord> enrollmentRecords = campaignService.search(
                 new CampaignEnrollmentsQuery().withExternalId(externalId).withCampaignName(campaignName));
 
         CampaignEnrollmentRecord campaignEnrollmentRecord = enrollmentRecords.get(0);
        return enrollmentRecords.size() > 0
                ? new MessageCampaignEnrollment(campaignEnrollmentRecord.getExternalId(),
                 campaignEnrollmentRecord.getCampaignName(), campaignEnrollmentRecord.getStartDate(),
                campaignEnrollmentRecord.getStatus())
                : null;
     }
 
     public List<DateTime> getMessageTimings(String subscriptionId, String packName, DateTime startDate, DateTime endDate) {
         String campaignName = SubscriptionPack.from(packName).getCampaignName();
         Map<String, List<Date>> campaignTimings = campaignService.getCampaignTimings(subscriptionId, campaignName,
                 startDate.toDate(), endDate.toDate());
         List<Date> campaignMessageTimings = campaignTimings.get(CAMPAIGN_MESSAGE_NAME);
 
         List<DateTime> alertTimings = new ArrayList<>();
         if (campaignMessageTimings == null || campaignMessageTimings.isEmpty())
             return alertTimings;
 
         for (Date date : campaignMessageTimings) {
             alertTimings.add(new DateTime(date));
         }
         return alertTimings;
     }
 }
