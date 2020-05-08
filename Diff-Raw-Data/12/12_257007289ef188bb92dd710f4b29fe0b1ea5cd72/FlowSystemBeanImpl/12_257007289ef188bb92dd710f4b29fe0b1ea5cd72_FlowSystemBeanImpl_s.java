 package org.motechproject.ananya.kilkari.functional.test;
 
 import org.joda.time.DateTime;
 import org.motechproject.ananya.kilkari.subscription.domain.Subscription;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionStatus;
 import org.motechproject.ananya.kilkari.subscription.repository.AllSubscriptions;
 import org.motechproject.ananya.kilkari.web.controller.SubscriptionController;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.http.MediaType;
 import org.springframework.stereotype.Component;
 
 import java.util.Date;
 
 import static org.motechproject.ananya.kilkari.functional.test.utils.MVCTestUtils.mockMvc;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.put;
 
 @Component
 public class FlowSystemBeanImpl implements FlowSystem {
     private SubscriptionController subscriptionController;
     private AllSubscriptions allSubscriptions;
     private SubscriptionStatusVerifier subscriptionStatusVerifier;
     private SubscriptionData subscriptionData;
 
     @Autowired
     public FlowSystemBeanImpl(SubscriptionController subscriptionController, AllSubscriptions allSubscriptions,SubscriptionStatusVerifier subscriptionStatusVerifier) {
         this.subscriptionController = subscriptionController;
         this.allSubscriptions = allSubscriptions;
         this.subscriptionStatusVerifier = subscriptionStatusVerifier;
     }
 
     @Override
     public FlowSystem subscribe(final SubscriptionData subscriptionData) throws Exception {
         this.subscriptionData = subscriptionData;
         mockMvc(subscriptionController)
                 .perform(get("/subscription").param("msisdn", subscriptionData.getMsisdn()).param("pack", subscriptionData.getPack())
                         .param("channel", subscriptionData.getChannel()));
 
         Subscription subscription = subscriptionStatusVerifier.verify(subscriptionData, SubscriptionStatus.PENDING_ACTIVATION);
         subscriptionData.setSubscriptionId(subscription.getSubscriptionId());
         return this;
     }
 
     @Override
     public FlowSystem nextFirstSlot() {
         return null;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
    public FlowSystem assertHappens(FlowEvent flowEvent) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
     public FlowSystem activate() throws Exception {
         mockMvc(subscriptionController)
                 .perform(put(String.format("/subscription/%s", subscriptionData.getSubscriptionId()))
                         .contentType(MediaType.APPLICATION_JSON)
                         .body(
                                 new CallBackRequestBuilder().forMsisdn(subscriptionData.getMsisdn())
                                         .forAction("ACT")
                                         .forStatus("SUCCESS")
                                         .build()
                                         .getBytes()
                         ));
         subscriptionStatusVerifier.verify(subscriptionData, SubscriptionStatus.ACTIVE);
         return this;
     }
 
     @Override
     public FlowSystem moveToFutureTime(DateTime dateTime) {
         String offsetValue = System.getProperty("faketime.offset.seconds");
         long currentOffset = Long.parseLong(offsetValue == null ? "0" : offsetValue);
 
         Date newDateTime = dateTime.toDate();
         System.out.println("Current Time:" + new Date());
         System.out.println("Request for Updated Time:" + newDateTime);
 
         long newOffset = ((newDateTime.getTime() - System.currentTimeMillis()) / 1000) + currentOffset;
         System.setProperty("faketime.offset.seconds", String.valueOf(newOffset));
 
         System.out.println("Updated Time:" + new Date());
         return this;  //To change body of implemented methods use File | Settings | File Templates.
     }
 }
