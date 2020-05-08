 package org.motechproject.ananya.kilkari.web.services;
 
 import org.motechproject.ananya.kilkari.web.domain.SubscriptionEventKeys;
 import org.motechproject.scheduler.context.EventContext;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 
 public class PublishService {
     private EventContext eventContext;
 
     @Autowired
     public PublishService(@Qualifier("eventContext") EventContext eventContext) {
         this.eventContext = eventContext;
     }
 
     public void createSubscription(String msisdn, String pack) {
         eventContext.send(SubscriptionEventKeys.CREATE_SUBSCRIPTION, msisdn, pack);
     }
 }
