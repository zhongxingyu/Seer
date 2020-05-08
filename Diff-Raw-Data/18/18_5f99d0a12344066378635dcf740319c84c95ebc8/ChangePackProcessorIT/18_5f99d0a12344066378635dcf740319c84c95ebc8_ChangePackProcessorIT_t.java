 package org.motechproject.ananya.kilkari.subscription.service;
 
 import org.joda.time.DateTime;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 import org.motechproject.ananya.kilkari.subscription.builder.SubscriptionBuilder;
 import org.motechproject.ananya.kilkari.subscription.domain.Channel;
 import org.motechproject.ananya.kilkari.subscription.domain.Subscription;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionPack;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionStatus;
 import org.motechproject.ananya.kilkari.subscription.repository.AllSubscriptions;
 import org.motechproject.ananya.kilkari.subscription.repository.SpringIntegrationTest;
 import org.motechproject.ananya.kilkari.subscription.service.request.ChangePackRequest;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 
 public class ChangePackProcessorIT extends SpringIntegrationTest {
 
     @Autowired
     private ChangePackService changePackService;
 
     @Autowired
     private AllSubscriptions allSubscriptions;
 
     @Autowired
     private SubscriptionService subscriptionService;
     private String msisdn = "1111111111";
 
     @Before
     @After
     public void tearDown() {
         allSubscriptions.removeAll();
     }
 
     @Test
     public void shouldChangePackForAnExistingSubscription() {
         Subscription existingSubscription = new SubscriptionBuilder().withDefaults().withMsisdn(msisdn).withPack(SubscriptionPack.BARI_KILKARI).build();
         allSubscriptions.add(existingSubscription);
         ChangePackRequest changePackRequest = new ChangePackRequest(msisdn, existingSubscription.getSubscriptionId(), SubscriptionPack.CHOTI_KILKARI, Channel.CALL_CENTER, DateTime.now(), DateTime.now().plusMonths(1), null);
 
         changePackService.process(changePackRequest);
 
         List<Subscription> subscriptions = allSubscriptions.findByMsisdn(msisdn);
         Subscription deactivatedSubscription = subscriptions.get(0);
        assertEquals(SubscriptionStatus.DEACTIVATION_REQUEST_RECEIVED, deactivatedSubscription.getStatus());
         assertEquals(deactivatedSubscription.getPack(), deactivatedSubscription.getPack());
         Subscription newSubscription = subscriptions.get(1);
         assertEquals(SubscriptionStatus.NEW_EARLY, newSubscription.getStatus());
         assertEquals(changePackRequest.getPack(), newSubscription.getPack());
     }
 }
