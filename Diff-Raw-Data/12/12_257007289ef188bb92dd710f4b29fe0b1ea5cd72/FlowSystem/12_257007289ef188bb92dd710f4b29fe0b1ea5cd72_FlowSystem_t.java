 package org.motechproject.ananya.kilkari.functional.test;
 
 import org.joda.time.DateTime;
 
 public interface FlowSystem {
     FlowSystem subscribe(SubscriptionData subscriptionData) throws Exception;
 
     FlowSystem nextFirstSlot();
 
     FlowSystem activate() throws Exception;
 
     FlowSystem moveToFutureTime(DateTime dateTime);
 }
