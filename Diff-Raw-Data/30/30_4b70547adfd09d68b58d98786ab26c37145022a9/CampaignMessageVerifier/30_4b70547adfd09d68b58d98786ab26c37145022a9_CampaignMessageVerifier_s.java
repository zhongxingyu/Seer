 package org.motechproject.ananya.kilkari.functional.test.verifiers;
 
 import org.junit.Assert;
 import org.motechproject.ananya.kilkari.functional.test.domain.SubscriptionData;
 import org.motechproject.ananya.kilkari.functional.test.utils.EventHandler;
 import org.motechproject.ananya.kilkari.functional.test.utils.TimedRunner;
 import org.motechproject.ananya.kilkari.functional.test.utils.TimedRunnerResponse;
 import org.motechproject.ananya.kilkari.obd.domain.CampaignMessage;
 import org.motechproject.ananya.kilkari.obd.domain.CampaignMessageStatus;
 import org.motechproject.ananya.kilkari.obd.repository.AllCampaignMessages;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 
 import static org.junit.Assert.*;
 
 @Component
 public class CampaignMessageVerifier {
 
 
     @Autowired
     private AllCampaignMessages allCampaignMessages;
 
     @Autowired
     private EventHandler eventHandler;
 
     public void verifyCampaignMessageExists(final SubscriptionData subscriptionData, final String weekMessageId) {
         CampaignMessage campaignMessage = findCampaignMessageWithRetry(subscriptionData, weekMessageId);
 
         if (campaignMessage == null)
             throw new RuntimeException(String.format("Campaign Message for subscription id - %s not in OBD db for week %s ", subscriptionData.getSubscriptionId(), weekMessageId));
     }
 
 
     public void verifyCampaignMessageIsNotCreatedAfterCampaignAlert(final SubscriptionData subscriptionData, final String weekMessageId) {
         Boolean campaignAlertRaised = waitForCampaignAlertToBeRaised();
         assertTrue("Campaign alert should have been raised", campaignAlertRaised);
 
         //waiting for 5 seconds before we assert for obd entry
         waitFor(5000);
         CampaignMessage campaignMessage = findOBDCampaignMessage(subscriptionData, weekMessageId);
         assertNull(campaignMessage);
     }
 
     private Boolean waitForCampaignAlertToBeRaised() {
         return new TimedRunner<Boolean>(50, 2000) {
             @Override
             protected TimedRunnerResponse<Boolean> run() {
                 if (!eventHandler.hasCampaignAlertBeenRaised()) {
                     return null;
                 }
                return TimedRunnerResponse.EMPTY;
             }
         }.executeWithTimeout();
     }
 
     private void waitFor(int millis) {
         try {
             Thread.sleep(millis);
         } catch (InterruptedException e) {
             Assert.fail("Failed while waiting for 10 seconds");
             e.printStackTrace();
         }
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
                 if (campaignMessage == null) {
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
                 CampaignMessage campaignMessage = findOBDCampaignMessage(subscriptionData, weekMessageId);
                 return campaignMessage == null ? null : new TimedRunnerResponse<>(campaignMessage);
             }
         }.executeWithTimeout();
     }
 
     private CampaignMessage findOBDCampaignMessage(SubscriptionData subscriptionData, String weekMessageId) {
         return allCampaignMessages.find(subscriptionData.getSubscriptionId(), weekMessageId);
     }
 
     public void reset() {
         eventHandler.reset();
     }
 }
