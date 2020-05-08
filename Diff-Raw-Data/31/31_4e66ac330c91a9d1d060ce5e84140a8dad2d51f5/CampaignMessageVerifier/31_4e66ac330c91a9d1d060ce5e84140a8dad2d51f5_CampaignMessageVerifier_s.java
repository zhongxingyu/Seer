 package org.motechproject.ananya.kilkari.functional.test.verifiers;
 
 import org.motechproject.ananya.kilkari.functional.test.domain.SubscriptionData;
 import org.motechproject.ananya.kilkari.functional.test.utils.TimedRunner;
 import org.motechproject.ananya.kilkari.functional.test.utils.TimedRunnerResponse;
 import org.motechproject.ananya.kilkari.obd.domain.CampaignMessage;
 import org.motechproject.ananya.kilkari.obd.domain.CampaignMessageStatus;
 import org.motechproject.ananya.kilkari.obd.repository.AllCampaignMessages;
 import org.motechproject.scheduler.domain.MotechEvent;
 import org.motechproject.server.event.annotations.MotechListener;
 import org.motechproject.server.messagecampaign.EventKeys;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import javax.security.auth.Subject;
 import java.util.List;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.*;
 
 @Component
 public class CampaignMessageVerifier {
 
 
     @Autowired
     private AllCampaignMessages allCampaignMessages;
 
     private boolean campaignAlertRaised = false;
 
     public void verifyCampaignMessageExists(final SubscriptionData subscriptionData, final String weekMessageId) {
         CampaignMessage campaignMessage = findCampaignMessageWithRetry(subscriptionData, weekMessageId);
 
         if (campaignMessage == null)
             throw new RuntimeException(String.format("Campaign Message for subscription id - %s not in OBD db for week %s ", subscriptionData.getSubscriptionId(), weekMessageId));
     }
 
    public void verifyCampaignMessageIsNotCreatedAfterCampaignAlert(SubscriptionData subscriptionData, String weekMessageId) {
        CampaignMessage campaignMessage = findCampaignMessageWithRetry(subscriptionData, weekMessageId);
         assertNull(campaignMessage);
     }
 
     @MotechListener(subjects = {EventKeys.SEND_MESSAGE})
     public void handleCampaignAlert(MotechEvent motechEvent) {
         campaignAlertRaised = true;
     }
 
 
     public void verifyCampaignMessageFailedDueToDNP(SubscriptionData subscriptionData, String campaignId) {
         CampaignMessage campaignMessage = findCampaignMessageWithStatus(subscriptionData, campaignId, CampaignMessageStatus.DNP);
         assertTrue(campaignMessage.hasFailed());
         assertFalse(campaignMessage.isSent());
     }
 
     private CampaignMessage findCampaignMessageWithStatus(final SubscriptionData subscriptionData,
                                                           final String weekMessageId,
                                                           final CampaignMessageStatus status) {
         return new TimedRunner<CampaignMessage>(120, 1000) {
             public TimedRunnerResponse<CampaignMessage> run() {
                 CampaignMessage campaignMessage = allCampaignMessages.find(subscriptionData.getSubscriptionId(), weekMessageId);
                 if(campaignMessage == null) {
                     return null;
                 }
                 return campaignMessage.getMessageId().equals(weekMessageId)
                         && campaignMessage.getStatus().equals(status) ? new TimedRunnerResponse<>(campaignMessage) : null;
             }
         }.executeWithTimeout();
     }
 
     public void verifyCampaignMessageFailedDueToDNC(SubscriptionData subscriptionData, String campaignId) {
         CampaignMessage campaignMessage = findCampaignMessageWithStatus(subscriptionData, campaignId, CampaignMessageStatus.DNC);
         assertTrue(campaignMessage.hasFailed());
         assertFalse(campaignMessage.isSent());
     }
 
     private CampaignMessage findCampaignMessageWithRetry(final SubscriptionData subscriptionData, final String weekMessageId) {
         return new TimedRunner<CampaignMessage>(120, 1000) {
             public TimedRunnerResponse<CampaignMessage> run() {
                 CampaignMessage campaignMessage = findCampaignMessage(subscriptionData, weekMessageId);
                 return campaignMessage == null ? null : new TimedRunnerResponse<>(campaignMessage);
             }
         }.executeWithTimeout();
     }
 
     private CampaignMessage findCampaignMessage(SubscriptionData subscriptionData, String weekMessageId) {
        CampaignMessage campaignMessage = allCampaignMessages.find(subscriptionData.getSubscriptionId(), weekMessageId);
        return campaignMessage != null && campaignMessage.getMessageId().equals(weekMessageId) ? campaignMessage : null;
     }
 
     public void reset() {
         campaignAlertRaised = false;
     }
 }
