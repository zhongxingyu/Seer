 package org.motechproject.ananya.kilkari.functional.test;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.motechproject.ananya.kilkari.functional.test.builder.SubscriptionDataBuilder;
 import org.motechproject.ananya.kilkari.functional.test.domain.SubscriptionData;
 import org.motechproject.ananya.kilkari.functional.test.utils.BaseFunctionalTest;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionStatus;
 import org.motechproject.ananya.kilkari.subscription.repository.KilkariPropertiesData;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static org.motechproject.ananya.kilkari.functional.test.Actions.*;
 
 public class SubscriptionCompletionFlowBaseFunctionalTest extends BaseFunctionalTest {
 
     @Autowired
     private KilkariPropertiesData kilkariProperties;
 
     @Test
     public void shouldSubscribeAndProgressAndListenToInboxAndDeactivateSubscriptionSuccessfully() throws Exception {
         System.out.println("Now running shouldSubscribeAndProgressAndListenToInboxAndCompleteSubscriptionSuccessfully");
         int scheduleDeltaDays = kilkariProperties.getCampaignScheduleDeltaDays();
         int deltaMinutes = kilkariProperties.getCampaignScheduleDeltaMinutes();
         DateTime futureDateForFirstCampaignAlertToBeRaised = DateTime.now().plusDays(scheduleDeltaDays).plusMinutes(deltaMinutes + 1);
         DateTime futureDateOfSecondCampaignAlert = futureDateForFirstCampaignAlertToBeRaised.plusWeeks(1);
         DateTime futureDateForCampaignAlertToBeRaisedAfterDeactivation = futureDateOfSecondCampaignAlert.plusWeeks(1);
 
         SubscriptionData subscriptionData = new SubscriptionDataBuilder().withDefaults().build();
 
         when(callCenter).subscribes(subscriptionData);
         and(subscriptionManager).activates(subscriptionData);
         and(time).isMovedToFuture(futureDateForFirstCampaignAlertToBeRaised);
         then(user).messageIsReady(subscriptionData, "WEEK1");
         then(user).canListenToThisWeeksInboxMessage(subscriptionData, "WEEK1");
 
         when(subscriptionManager).renews(subscriptionData);
         and(time).isMovedToFuture(futureDateOfSecondCampaignAlert);
         then(user).messageIsReady(subscriptionData, "WEEK2");
         then(user).cannotListenToPreviousWeeksInboxMessage(subscriptionData, "WEEK1");
 
         when(callCenter).unSubscribes(subscriptionData);
         and(subscriptionManager).confirmsDeactivation(subscriptionData, SubscriptionStatus.DEACTIVATED);
         and(time).isMovedToFuture(futureDateForCampaignAlertToBeRaisedAfterDeactivation);
         then(user).messageIsNotCreated(subscriptionData, "WEEK3");
     }
 
     @Test
     public void shouldSubscribeAndProgressAndListenToLastMessageAndCompleteSubscriptionWithIntermediateSuspensionsSuccessfully() throws Exception {
         System.out.println("Now running shouldSubscribeAndProgressAndListenToLastMessageAndCompleteSubscriptionWithIntermediateSuspensionsSuccessfully");
 
         int scheduleDeltaDays = kilkariProperties.getCampaignScheduleDeltaDays();
         int deltaMinutes = kilkariProperties.getCampaignScheduleDeltaMinutes();
         DateTime futureDateForFirstCampaignAlertToBeRaised = DateTime.now().plusDays(scheduleDeltaDays).plusMinutes(deltaMinutes + 1);
         DateTime futureDateOfSecondCampaignAlert = futureDateForFirstCampaignAlertToBeRaised.plusWeeks(1);
         DateTime futureDateForThirdCampaignAlert = futureDateOfSecondCampaignAlert.plusWeeks(1);
         DateTime futureDateOfPackCompletion = futureDateForFirstCampaignAlertToBeRaised.plusWeeks(63);
         DateTime week2MessageExpiryDate = futureDateOfSecondCampaignAlert.plusWeeks(1);
 
         SubscriptionData subscriptionData = new SubscriptionDataBuilder().withDefaults().build();
 
         when(callCenter).subscribes(subscriptionData);
         and(subscriptionManager).activates(subscriptionData);
         and(user).resetCampaignMessageVerifier();
         and(time).isMovedToFuture(futureDateForFirstCampaignAlertToBeRaised);
         then(user).messageIsReady(subscriptionData, "WEEK1");
 
         when(subscriptionManager).failsRenew(subscriptionData);
         and(user).resetCampaignMessageVerifier();
         and(time).isMovedToFuture(futureDateOfSecondCampaignAlert);
         then(user).messageIsNotCreated(subscriptionData, "WEEK2");
 
         when(user).resetCampaignMessageVerifier();
         and(time).isMovedToFuture(week2MessageExpiryDate);
         then(subscriptionManager).renews(subscriptionData);
         then(user).messageIsNotCreated(subscriptionData, "WEEK2");
 
         when(user).resetCampaignMessageVerifier();
         and(time).isMovedToFuture(futureDateForThirdCampaignAlert);
         and(user).messageIsReady(subscriptionData, "WEEK3");
 
         when(user).resetCampaignMessageVerifier();
         and(time).isMovedToFuture(futureDateOfPackCompletion);
         then(subscriptionManager).renews(subscriptionData);
         and(user).messageIsReady(subscriptionData, "WEEK64");
 
         and(time).isMovedToFuture(futureDateOfPackCompletion.plusHours(1));
         then(subscriptionVerifier).verifySubscriptionState(subscriptionData, SubscriptionStatus.PENDING_COMPLETION);
 
         then(user).canListenToThisWeeksInboxMessage(subscriptionData, "WEEK64");
         and(time).isMovedToFuture(futureDateOfPackCompletion.plusWeeks(1));
         then(user).cannotListenToPreviousWeeksInboxMessage(subscriptionData, "WEEK64");
     }
 
 
     @Test
     public void shouldSubscribeAndProgressAndNotifyAndReceiveInfantDeathMessages() throws Exception {
 
         System.out.println("Now running shouldSubscribeAndProgressAndNotifyAndReceiveInfantDeathMessages");
 
         int scheduleDeltaDays = kilkariProperties.getCampaignScheduleDeltaDays();
         int deltaMinutes = kilkariProperties.getCampaignScheduleDeltaMinutes();
 
         DateTime now = DateTime.now();
         DateTime futureDateForFirstCampaignAlertToBeRaised = now.plusDays(scheduleDeltaDays).plusMinutes(deltaMinutes + 5);
         DateTime futureDateForSecondCampaignAlert = futureDateForFirstCampaignAlertToBeRaised.plusWeeks(1);
 
         DateTime futureDateForFirstInfantDeathCampaignAlert = futureDateForSecondCampaignAlert.plusWeeks(1).plusMinutes(30);
         DateTime futureDateForSecondInfantDeathCampaignAlert = futureDateForFirstInfantDeathCampaignAlert.plusWeeks(1);
         DateTime futureDateOfCompletion = futureDateForSecondInfantDeathCampaignAlert.plusWeeks(1);
         SubscriptionData subscriptionData = new SubscriptionDataBuilder().withDefaults().build();
 
         when(callCenter).subscribes(subscriptionData);
         and(subscriptionManager).activates(subscriptionData);
         and(time).isMovedToFuture(futureDateForFirstCampaignAlertToBeRaised);
         then(user).messageIsReady(subscriptionData, "WEEK1");
 
         when(time).isMovedToFuture(futureDateForSecondCampaignAlert);
         and(subscriptionManager).renews(subscriptionData);
         and(user).messageIsReady(subscriptionData, "WEEK2");
 
         when(callCenter).changesCampaign(subscriptionData);
         and(time).isMovedToFuture(futureDateForFirstInfantDeathCampaignAlert);
         and(subscriptionManager).renews(subscriptionData);
         then(user).messageIsNotCreated(subscriptionData, "WEEK3");
         then(user).messageIsReady(subscriptionData, "ID1");
 
         when(time).isMovedToFuture(futureDateForSecondInfantDeathCampaignAlert);
         and(subscriptionManager).renews(subscriptionData);
         then(user).messageIsReady(subscriptionData, "ID2");
 
         when(time).isMovedToFuture(futureDateOfCompletion);
         then(subscriptionVerifier).verifySubscriptionState(subscriptionData, SubscriptionStatus.PENDING_COMPLETION);
     }
 
     @Test
     public void shouldNotSendMessageWhenCustomerIsPerpetuallySuspended() throws Exception {
 
         System.out.println("Now running shouldNotSendMessageWhenCustomerIsPerpetuallySuspended");
 
         int scheduleDeltaDays = kilkariProperties.getCampaignScheduleDeltaDays();
         int deltaMinutes = kilkariProperties.getCampaignScheduleDeltaMinutes();
 
         DateTime now = DateTime.now();
         DateTime firstCampaignAlertDate = now.plusDays(scheduleDeltaDays).plusMinutes(deltaMinutes + 5);
         DateTime secondCampaignAlertDate = firstCampaignAlertDate.plusWeeks(1);
         DateTime thirdCampaignAlertDate = secondCampaignAlertDate.plusWeeks(1);
         DateTime lastCampaignAlertDate = firstCampaignAlertDate.plusWeeks(63);
         DateTime lastCampaignExpiryDate = lastCampaignAlertDate.plusWeeks(1);
 
         SubscriptionData subscriptionData = new SubscriptionDataBuilder().withDefaults().build();
 
         when(callCenter).subscribes(subscriptionData);
         and(subscriptionManager).activates(subscriptionData);
         and(time).isMovedToFuture(firstCampaignAlertDate);
         then(user).messageIsReady(subscriptionData, "WEEK1");
 
         when(time).isMovedToFuture(secondCampaignAlertDate);
         and(subscriptionManager).failsRenew(subscriptionData);
         then(user).messageIsNotCreated(subscriptionData, "WEEK2");
 
         when(time).isMovedToFuture(thirdCampaignAlertDate);
         and(subscriptionManager).failsRenew(subscriptionData);
         then(user).messageIsNotCreated(subscriptionData, "WEEK3");
 
         when(time).isMovedToFuture(lastCampaignAlertDate);
         then(user).messageIsNotCreated(subscriptionData, "WEEK64");
        Thread.sleep(15000);
 
         when(time).isMovedToFuture(lastCampaignExpiryDate);
         and(subscriptionVerifier).verifySubscriptionState(subscriptionData, SubscriptionStatus.PENDING_COMPLETION);
         and(subscriptionManager).confirmsDeactivation(subscriptionData, SubscriptionStatus.COMPLETED);
     }
 }
 
