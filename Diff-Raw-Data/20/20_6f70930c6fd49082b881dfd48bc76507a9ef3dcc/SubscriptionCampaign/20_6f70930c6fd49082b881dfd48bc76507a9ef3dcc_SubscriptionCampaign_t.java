 package org.motechproject.ghana.mtn.process;
 
 import org.motechproject.ghana.mtn.domain.MessageBundle;
 import org.motechproject.ghana.mtn.domain.Subscription;
 import org.motechproject.ghana.mtn.service.SMSService;
 import org.motechproject.server.messagecampaign.service.MessageCampaignService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 @Component
 public class SubscriptionCampaign extends BaseSubscriptionProcess implements ISubscriptionFlowProcess {
     private MessageCampaignService campaignService;
 
     @Autowired
     public SubscriptionCampaign(SMSService smsService, MessageBundle messageBundle, MessageCampaignService campaignService) {
         super(smsService, messageBundle);
         this.campaignService = campaignService;
     }
 
     @Override
     public Boolean startFor(Subscription subscription) {
         campaignService.startFor(subscription.createCampaignRequest());
         sendMessage(subscription, messageFor(MessageBundle.ENROLLMENT_SUCCESS));
         return true;
     }
 
     @Override
     public Boolean stopExpired(Subscription subscription) {
         campaignService.stopFor(subscription.createCampaignRequest());
         sendMessage(subscription, messageFor(MessageBundle.ENROLLMENT_STOPPED));
         return true;
     }
 
     @Override
     public Boolean stopByUser(Subscription subscription) {
        campaignService.stopFor(subscription.createCampaignRequest());
        sendMessage(subscription, messageFor(MessageBundle.STOP_PROGRAM_SUCCESS));
        return true;
     }
 
     @Override
     public Boolean rollOver(Subscription fromSubscription, Subscription toSubscription) {
         campaignService.stopFor(fromSubscription.createCampaignRequest());
         campaignService.startFor(toSubscription.createCampaignRequest());
         sendMessage(toSubscription, messageFor(MessageBundle.ENROLLMENT_ROLlOVER));
         return true;
     }
 }
