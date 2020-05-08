 package org.motechproject.ananya.kilkari.service;
 
 import org.apache.commons.collections.CollectionUtils;
 import org.apache.commons.collections.Transformer;
 import org.joda.time.DateTime;
 import org.motechproject.ananya.kilkari.message.service.CampaignMessageAlertService;
 import org.motechproject.ananya.kilkari.message.service.InboxEventKeys;
 import org.motechproject.ananya.kilkari.message.service.InboxService;
 import org.motechproject.ananya.kilkari.messagecampaign.service.MessageCampaignService;
 import org.motechproject.ananya.kilkari.reporting.service.ReportingService;
 import org.motechproject.ananya.kilkari.subscription.domain.Subscription;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionEventKeys;
 import org.motechproject.ananya.kilkari.utils.CampaignMessageIdStrategy;
 import org.motechproject.ananya.reports.kilkari.contract.request.CampaignScheduleAlertRequest;
 import org.motechproject.scheduler.MotechSchedulerService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 @Service
 public class KilkariCampaignService {
 
     private MessageCampaignService messageCampaignService;
     private KilkariSubscriptionService kilkariSubscriptionService;
     private CampaignMessageAlertService campaignMessageAlertService;
     private ReportingService reportingService;
     private InboxService inboxService;
 
     private final Logger logger = LoggerFactory.getLogger(KilkariCampaignService.class);
     private MotechSchedulerService motechSchedulerService;
 
     KilkariCampaignService() {
     }
 
     @Autowired
     public KilkariCampaignService(MessageCampaignService messageCampaignService,
                                   KilkariSubscriptionService kilkariSubscriptionService,
                                   CampaignMessageAlertService campaignMessageAlertService,
                                   ReportingService reportingService, InboxService inboxService,
                                   MotechSchedulerService motechSchedulerService) {
         this.messageCampaignService = messageCampaignService;
         this.kilkariSubscriptionService = kilkariSubscriptionService;
         this.campaignMessageAlertService = campaignMessageAlertService;
         this.reportingService = reportingService;
         this.inboxService = inboxService;
         this.motechSchedulerService = motechSchedulerService;
     }
 
     public Map<String, List<DateTime>> getTimings(String msisdn) {
         List<Subscription> subscriptionList = kilkariSubscriptionService.findByMsisdn(msisdn);
         Map<String, List<DateTime>> subscriptionEventsMap = new HashMap<>();
 
         for (Subscription subscription : subscriptionList) {
             String subscriptionId = subscription.getSubscriptionId();
             try {
                if (subscription.isEarlySubscription()) {
                     subscriptionEventsMap.put("Early Subscription for " + subscriptionId, getSchedules(subscriptionId, SubscriptionEventKeys.EARLY_SUBSCRIPTION,
                             subscription.getCreationDate().toDate(), subscription.getStartDate().toDate()));
                     continue;
                 }
                 subscriptionEventsMap.put("Message Schedule for " + subscriptionId, getMessageTimings(subscription));
                 subscriptionEventsMap.put("Inbox Deletion for " + subscriptionId, getScheduleTimings(subscription, InboxEventKeys.DELETE_INBOX));
                 subscriptionEventsMap.put("Subscription Deactivation for " + subscriptionId, getScheduleTimings(subscription, SubscriptionEventKeys.DEACTIVATE_SUBSCRIPTION));
                 subscriptionEventsMap.put("Subscription Completion for " + subscriptionId, getScheduleTimings(subscription, SubscriptionEventKeys.SUBSCRIPTION_COMPLETE));
 
             } catch (NullPointerException ne) {
                 //ignore
             }
         }
         return subscriptionEventsMap;
     }
 
     private List<DateTime> getScheduleTimings(Subscription subscription, String subject) {
         Date startDate = subscription.getCreationDate().toDate();
         Date endDate = subscription.endDate().plusWeeks(2).toDate();
         return getSchedules(subscription.getSubscriptionId(), subject, startDate, endDate);
     }
 
     private List<DateTime> getSchedules(String subscriptionId, String subject, Date startDate, Date endDate) {
         List<Date> timings = motechSchedulerService.getScheduledJobTimingsWithPrefix(subject, subscriptionId, startDate, endDate);
 
         return (List<DateTime>) CollectionUtils.collect(timings, new Transformer() {
             @Override
             public Object transform(Object input) {
                 return new DateTime(input);
             }
         });
     }
 
     private List<DateTime> getMessageTimings(Subscription subscription) {
         return messageCampaignService.getMessageTimings(subscription.getSubscriptionId(), subscription.getCreationDate(), subscription.endDate());
     }
 
     public void scheduleWeeklyMessage(String subscriptionId, String campaignName) {
         Subscription subscription = kilkariSubscriptionService.findBySubscriptionId(subscriptionId);
 
         final String messageId = new CampaignMessageIdStrategy().createMessageId(campaignName, messageCampaignService.getCampaignStartDate(subscriptionId, campaignName), subscription.getPack());
         final DateTime messageExpiryDate = subscription.getCurrentWeeksMessageExpiryDate();
 
         reportingService.reportCampaignScheduleAlertReceived(new CampaignScheduleAlertRequest(subscriptionId, messageId, DateTime.now()));
         campaignMessageAlertService.scheduleCampaignMessageAlert(subscriptionId, messageId, messageExpiryDate, subscription.getMsisdn(), subscription.getOperator().name());
 
         if (subscription.hasBeenActivated())
             inboxService.newMessage(subscriptionId, messageId);
     }
 
     public void processCampaignCompletion(String subscriptionId) {
         Subscription subscription = kilkariSubscriptionService.findBySubscriptionId(subscriptionId);
         if (!subscription.isInDeactivatedState())
             kilkariSubscriptionService.processSubscriptionCompletion(subscription);
     }
 }
