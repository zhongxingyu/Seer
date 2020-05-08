 package org.motechproject.ananya.kilkari.functional.test;
 
 import org.joda.time.DateTime;
 import org.junit.Test;
 import org.motechproject.ananya.kilkari.functional.test.builder.SubscriptionDataBuilder;
 import org.motechproject.ananya.kilkari.functional.test.domain.SubscriptionData;
 import org.motechproject.ananya.kilkari.functional.test.utils.FunctionalTestUtils;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionStatus;
 import org.motechproject.ananya.kilkari.subscription.repository.KilkariPropertiesData;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import static org.motechproject.ananya.kilkari.functional.test.Actions.*;
 
 public class SubscriptionCompletionFlowFunctionalTest extends FunctionalTestUtils {
 
     @Autowired
     private KilkariPropertiesData kilkariProperties;
 
     @Test
     public void shouldSubscribeAndProgressAndCompleteSubscriptionSuccessfully() throws Exception {
         int scheduleDeltaDays = kilkariProperties.getCampaignScheduleDeltaDays();
         int deltaMinutes = kilkariProperties.getCampaignScheduleDeltaMinutes();
         DateTime futureDateForFirstCampaignAlertToBeRaised = DateTime.now().plusDays(scheduleDeltaDays).plusMinutes(deltaMinutes + 1);
         DateTime futureDateOfSecondCampaignAlert = futureDateForFirstCampaignAlertToBeRaised.plusWeeks(1);
         DateTime futureDateOfPackCompletion = futureDateForFirstCampaignAlertToBeRaised.plusWeeks(59);
 
         SubscriptionData subscriptionData = new SubscriptionDataBuilder().withDefaults().build();
 

         when(callCenter).subscribes(subscriptionData);
         and(subscriptionManager).activates(subscriptionData);
         and(time).isMovedToFuture(futureDateForFirstCampaignAlertToBeRaised);
         then(user).messageIsReady(subscriptionData, "WEEK1");
         when(subscriptionManager).renews(subscriptionData);
         and(time).isMovedToFuture(futureDateOfSecondCampaignAlert);
         then(user).messageIsReady(subscriptionData, "WEEK2");
         and(time).isMovedToFuture(futureDateOfPackCompletion.plusHours(1));
         then(subscriptionVerifier).verifySubscriptionState(subscriptionData, SubscriptionStatus.PENDING_COMPLETION);
     }
 
     @Test
     public void shouldSubscribeAndProgressAndCompleteSubscriptionWithIntermediateSuspensionsSuccessfully() throws Exception {
         int scheduleDeltaDays = kilkariProperties.getCampaignScheduleDeltaDays();
         int deltaMinutes = kilkariProperties.getCampaignScheduleDeltaMinutes();
         DateTime futureDateForFirstCampaignAlertToBeRaised = DateTime.now().plusDays(scheduleDeltaDays).plusMinutes(deltaMinutes + 1);
         DateTime futureDateOfSecondCampaignAlert = futureDateForFirstCampaignAlertToBeRaised.plusWeeks(1);
         DateTime futureDateForThirdCampaignAlert = futureDateOfSecondCampaignAlert.plusWeeks(1);
         DateTime futureDateOfPackCompletion = futureDateForFirstCampaignAlertToBeRaised.plusWeeks(59);
         DateTime week2MessageExpiryDate = futureDateOfSecondCampaignAlert.plusWeeks(1);
 
         SubscriptionData subscriptionData = new SubscriptionDataBuilder().withDefaults().build();
 
         when(callCenter).subscribes(subscriptionData);
         and(subscriptionManager).activates(subscriptionData);
         and(time).isMovedToFuture(futureDateForFirstCampaignAlertToBeRaised);
         then(user).messageIsReady(subscriptionData, "WEEK1");
 
         when(subscriptionManager).failsRenew(subscriptionData);
         and(time).isMovedToFuture(futureDateOfSecondCampaignAlert);
         then(user).messageIsNotReady(subscriptionData, "WEEK2");
 
         when(time).isMovedToFuture(week2MessageExpiryDate);
         then(subscriptionManager).renews(subscriptionData);
         then(user).messageIsNotReady(subscriptionData, "WEEK2");
 
         when(time).isMovedToFuture(futureDateForThirdCampaignAlert);
         and(user).messageIsReady(subscriptionData, "WEEK3");
 
         and(time).isMovedToFuture(futureDateOfPackCompletion.plusHours(1));
         then(subscriptionVerifier).verifySubscriptionState(subscriptionData, SubscriptionStatus.PENDING_COMPLETION);
     }
 }
 
