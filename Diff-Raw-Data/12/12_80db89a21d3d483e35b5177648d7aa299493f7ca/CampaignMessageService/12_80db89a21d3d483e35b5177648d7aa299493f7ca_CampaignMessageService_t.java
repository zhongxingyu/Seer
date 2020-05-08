 package org.motechproject.ananya.kilkari.obd.service;
 
 import org.motechproject.ananya.kilkari.obd.builder.CampaignMessageCSVBuilder;
 import org.motechproject.ananya.kilkari.obd.domain.CampaignMessage;
 import org.motechproject.ananya.kilkari.obd.domain.InvalidCallRecord;
 import org.motechproject.ananya.kilkari.obd.gateway.OnMobileOBDGateway;
 import org.motechproject.ananya.kilkari.obd.repository.AllCampaignMessages;
 import org.motechproject.ananya.kilkari.obd.repository.AllInvalidCallRecords;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Service
 public class CampaignMessageService {
 
     private AllCampaignMessages allCampaignMessages;
     private OnMobileOBDGateway onMobileOBDGateway;
     private CampaignMessageCSVBuilder campaignMessageCSVBuilder;
     private AllInvalidCallRecords allInvalidCallRecords;
 
     private static final Logger logger = LoggerFactory.getLogger(CampaignMessageService.class);
 
     @Autowired
     public CampaignMessageService(AllCampaignMessages allCampaignMessages, OnMobileOBDGateway onMobileOBDGateway, CampaignMessageCSVBuilder campaignMessageCSVBuilder, AllInvalidCallRecords allInvalidCallRecords) {
         this.allCampaignMessages = allCampaignMessages;
         this.onMobileOBDGateway = onMobileOBDGateway;
         this.campaignMessageCSVBuilder = campaignMessageCSVBuilder;
         this.allInvalidCallRecords = allInvalidCallRecords;
     }
 
     public void scheduleCampaignMessage(String subscriptionId, String messageId, String msisdn, String operator) {
         allCampaignMessages.add(new CampaignMessage(subscriptionId, messageId, msisdn, operator));
     }
 
     public void sendNewMessages() {
         List<CampaignMessage> allNewMessages = allCampaignMessages.getAllUnsentNewMessages();
         GatewayAction gatewayAction = new GatewayAction() {
             @Override
             public void send(String content) {
                 onMobileOBDGateway.sendNewMessages(content);
             }
         };
         sendMessagesToOBD(allNewMessages, gatewayAction);
     }
 
     public void sendRetryMessages() {
         List<CampaignMessage> allRetryMessages = allCampaignMessages.getAllUnsentRetryMessages();
        GatewayAction gatewayAction =   new GatewayAction() {
             @Override
             public void send(String content) {
                 onMobileOBDGateway.sendRetryMessages(content);
             }
         };
         sendMessagesToOBD(allRetryMessages, gatewayAction);
     }
 
     private void sendMessagesToOBD(List<CampaignMessage> messages, GatewayAction gatewayAction) {
         logger.info(String.format("Sending %s campaign messages to obd", messages.size()));
         if(messages.isEmpty())
             return;
         String campaignMessageCSVContent = campaignMessageCSVBuilder.getCSV(messages);
         gatewayAction.send(campaignMessageCSVContent);
         for (CampaignMessage message : messages) {
             message.markSent();
             allCampaignMessages.update(message);
         }
     }
 
     public void deleteCampaignMessage(String subscriptionId, String campaignId) {
         CampaignMessage campaignMessage = allCampaignMessages.find(subscriptionId, campaignId);
         if(campaignMessage != null)
             allCampaignMessages.delete(campaignMessage);
     }
 
     public void processInvalidCallRecords(ArrayList<InvalidCallRecord> invalidCallRecords) {
         for(InvalidCallRecord record : invalidCallRecords){
             allInvalidCallRecords.add(record);
         }
     }
 
     private interface GatewayAction {
         public void send(String content);
     }
 }
