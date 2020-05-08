 package org.motechproject.ananya.reports.kilkari.service;
 
 import org.joda.time.DateTime;
 import org.motechproject.ananya.reports.kilkari.contract.request.CampaignChangeReportRequest;
 import org.motechproject.ananya.reports.kilkari.domain.MessageCampaignPack;
 import org.motechproject.ananya.reports.kilkari.domain.dimension.Subscription;
 import org.motechproject.ananya.reports.kilkari.repository.AllSubscriptions;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.sql.Timestamp;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 @Service
 public class SubscriptionService {
 
     private AllSubscriptions allSubscriptions;
 
     private SubscriberService subscriberService;
 
     public SubscriptionService() {
     }
 
     @Autowired
     public SubscriptionService(AllSubscriptions allSubscriptions, SubscriberService subscriberService) {
         this.allSubscriptions = allSubscriptions;
         this.subscriberService = subscriberService;
     }
 
     public Subscription makeFor(Subscription subscription) {
         return allSubscriptions.save(subscription);
     }
 
     @Transactional
     public Subscription fetchFor(String subscriptionId) {
         return allSubscriptions.findBySubscriptionId(subscriptionId);
     }
 
     @Transactional
     public List<Subscription> findByMsisdn(String msisdn) {
         Long msisdnAsLong = tryParse(msisdn);
         return msisdnAsLong != null ? allSubscriptions.findByMsisdn(msisdnAsLong) : Collections.EMPTY_LIST;
     }
 
     public Set<Subscription> getAllRelatedSubscriptions(String msisdn) {
         List<Subscription> subscriptions = findByMsisdn(msisdn);
         return allSubscriptions.getAllRelatedSubscriptions(subscriptions);
     }
 
     public void updateLastScheduledMessageDate(String subscriptionId, DateTime lastScheduledMessageDate) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         subscription.setLastScheduledMessageDate(new Timestamp(lastScheduledMessageDate.getMillis()));
         allSubscriptions.update(subscription);
     }
 
     private Long tryParse(String msisdn) {
         try {
             return Long.parseLong(msisdn);
         } catch (Exception e) {
             return null;
         }
     }
 
     @Transactional
     public void updateMessageCampaign(CampaignChangeReportRequest campaignChangeReportRequest, String subscriptionId) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         if (subscription == null)
             return;
         subscription.updateMessageCampaignPack(MessageCampaignPack.from(campaignChangeReportRequest.getMessageCampaignPack()), campaignChangeReportRequest.getCreatedAt());
         allSubscriptions.update(subscription);
     }
 
     @Transactional
     public void deleteAll(Set<Subscription> subscriptions) {
         allSubscriptions.deleteAll(subscriptions);
     }
 }
