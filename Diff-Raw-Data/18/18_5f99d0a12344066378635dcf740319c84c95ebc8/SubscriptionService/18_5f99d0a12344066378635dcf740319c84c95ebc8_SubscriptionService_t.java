 package org.motechproject.ananya.kilkari.subscription.service;
 
 import org.joda.time.DateTime;
 import org.motechproject.ananya.kilkari.message.service.CampaignMessageAlertService;
 import org.motechproject.ananya.kilkari.message.service.InboxService;
 import org.motechproject.ananya.kilkari.messagecampaign.domain.MessageCampaignPack;
 import org.motechproject.ananya.kilkari.messagecampaign.request.MessageCampaignRequest;
 import org.motechproject.ananya.kilkari.messagecampaign.service.MessageCampaignService;
 import org.motechproject.ananya.kilkari.obd.service.CampaignMessageService;
 import org.motechproject.ananya.kilkari.reporting.service.ReportingService;
 import org.motechproject.ananya.kilkari.subscription.domain.*;
 import org.motechproject.ananya.kilkari.subscription.repository.AllSubscriptions;
 import org.motechproject.ananya.kilkari.subscription.repository.KilkariPropertiesData;
 import org.motechproject.ananya.kilkari.subscription.repository.OnMobileSubscriptionGateway;
 import org.motechproject.ananya.kilkari.subscription.request.OMSubscriptionRequest;
 import org.motechproject.ananya.kilkari.subscription.service.mapper.SubscriptionMapper;
 import org.motechproject.ananya.kilkari.subscription.service.request.*;
 import org.motechproject.ananya.kilkari.subscription.validators.ChangeMsisdnValidator;
 import org.motechproject.ananya.kilkari.subscription.validators.SubscriptionValidator;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriberLocation;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriberReportRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriptionStateChangeRequest;
 import org.motechproject.ananya.reports.kilkari.contract.response.SubscriberResponse;
 import org.motechproject.scheduler.MotechSchedulerService;
 import org.motechproject.scheduler.domain.MotechEvent;
 import org.motechproject.scheduler.domain.RunOnceSchedulableJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 
 @Service
 public class SubscriptionService {
     private AllSubscriptions allSubscriptions;
     private OnMobileSubscriptionManagerPublisher onMobileSubscriptionManagerPublisher;
     private SubscriptionValidator subscriptionValidator;
     private ReportingService reportingService;
     private InboxService inboxService;
     private MessageCampaignService messageCampaignService;
     private OnMobileSubscriptionGateway onMobileSubscriptionGateway;
     private CampaignMessageService campaignMessageService;
     private CampaignMessageAlertService campaignMessageAlertService;
     private KilkariPropertiesData kilkariPropertiesData;
     private MotechSchedulerService motechSchedulerService;
     private ChangeMsisdnValidator changeMsisdnValidator;
 
     private final static Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
 
     @Autowired
     public SubscriptionService(AllSubscriptions allSubscriptions, OnMobileSubscriptionManagerPublisher onMobileSubscriptionManagerPublisher,
                                SubscriptionValidator subscriptionValidator, ReportingService reportingService,
                                InboxService inboxService, MessageCampaignService messageCampaignService, OnMobileSubscriptionGateway onMobileSubscriptionGateway,
                                CampaignMessageService campaignMessageService, CampaignMessageAlertService campaignMessageAlertService, KilkariPropertiesData kilkariPropertiesData,
                                MotechSchedulerService motechSchedulerService, ChangeMsisdnValidator changeMsisdnValidator) {
         this.allSubscriptions = allSubscriptions;
         this.onMobileSubscriptionManagerPublisher = onMobileSubscriptionManagerPublisher;
         this.subscriptionValidator = subscriptionValidator;
         this.reportingService = reportingService;
         this.inboxService = inboxService;
         this.messageCampaignService = messageCampaignService;
         this.onMobileSubscriptionGateway = onMobileSubscriptionGateway;
         this.campaignMessageService = campaignMessageService;
         this.campaignMessageAlertService = campaignMessageAlertService;
         this.kilkariPropertiesData = kilkariPropertiesData;
         this.motechSchedulerService = motechSchedulerService;
         this.changeMsisdnValidator = changeMsisdnValidator;
     }
 
     public Subscription createSubscriptionWithReporting(SubscriptionRequest subscriptionRequest, Channel channel) {
         Subscription subscription = createSubscription(subscriptionRequest, channel);
 
         reportingService.reportSubscriptionCreation(SubscriptionMapper.createSubscriptionCreationReportRequest(
                 subscription, channel, subscriptionRequest));
 
         return subscription;
     }
 
     public Subscription createSubscription(SubscriptionRequest subscriptionRequest, Channel channel) {
         subscriptionValidator.validate(subscriptionRequest);
 
         DateTime startDate = subscriptionRequest.getSubscriptionStartDate();
         boolean isEarlySubscription = subscriptionRequest.isEarlySubscription(startDate);
 
         Subscription subscription = new Subscription(subscriptionRequest.getMsisdn(), subscriptionRequest.getPack(),
                 subscriptionRequest.getCreationDate(), (isEarlySubscription ? SubscriptionStatus.NEW_EARLY : SubscriptionStatus.NEW));
         subscription.setStartDate(startDate);
         allSubscriptions.add(subscription);
 
         OMSubscriptionRequest omSubscriptionRequest = SubscriptionMapper.createOMSubscriptionRequest(subscription, channel);
         if (isEarlySubscription) {
             scheduleEarlySubscription(startDate, omSubscriptionRequest);
         } else
             initiateActivationRequest(omSubscriptionRequest);
         return subscription;
     }
 
 
     private void scheduleEarlySubscription(DateTime startDate, OMSubscriptionRequest omSubscriptionRequest) {
 
         String subjectKey = SubscriptionEventKeys.EARLY_SUBSCRIPTION;
 
         HashMap<String, Object> parameters = new HashMap<>();
         parameters.put(MotechSchedulerService.JOB_ID_KEY, omSubscriptionRequest.getSubscriptionId());
         parameters.put("0", omSubscriptionRequest);
         MotechEvent motechEvent = new MotechEvent(subjectKey, parameters);
 
         RunOnceSchedulableJob runOnceSchedulableJob = new RunOnceSchedulableJob(motechEvent, startDate.toDate());
 
         motechSchedulerService.safeScheduleRunOnceJob(runOnceSchedulableJob);
     }
 
     public void initiateActivationRequest(OMSubscriptionRequest omSubscriptionRequest) {
         onMobileSubscriptionManagerPublisher.sendActivationRequest(omSubscriptionRequest);
     }
 
     public List<Subscription> findByMsisdn(String msisdn) {
         return (List<Subscription>) (List<? extends Subscription>) allSubscriptions.findByMsisdn(msisdn);
     }
 
     private DateTime getBufferedDateTime(DateTime dateTime) {
         return dateTime.plusDays(kilkariPropertiesData.getCampaignScheduleDeltaDays())
                 .plusMinutes(kilkariPropertiesData.getCampaignScheduleDeltaMinutes());
     }
 
     public void activate(String subscriptionId, final DateTime activatedOn, final String operator) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         final DateTime scheduleStartDateTime = subscription.getStartDateForSubscription(activatedOn);
         scheduleCampaign(subscription, scheduleStartDateTime);
         updateStatusAndReport(subscription, activatedOn, null, operator, null, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.activate(operator, getBufferedDateTime(scheduleStartDateTime));
             }
         });
     }
 
     public void activationFailed(String subscriptionId, DateTime updatedOn, String reason, final String operator) {
         updateStatusAndReport(subscriptionId, updatedOn, reason, operator, null, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.activationFailed(operator);
             }
         });
     }
 
     public void activationRequested(OMSubscriptionRequest omSubscriptionRequest) {
         onMobileSubscriptionGateway.activateSubscription(omSubscriptionRequest);
         updateStatusAndReport(omSubscriptionRequest.getSubscriptionId(), DateTime.now(), null, null, null, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.activationRequestSent();
             }
         });
     }
 
     public void requestDeactivation(DeactivationRequest deactivationRequest) {
         String subscriptionId = deactivationRequest.getSubscriptionId();
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         if (!subscription.isInProgress()) {
             logger.debug(String.format("Cannot unsubscribe. Subscription in %s status", subscription.getStatus()));
             return;
         }
         updateStatusAndReport(subscription, deactivationRequest.getCreatedAt(), null, null, null, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.deactivationRequestReceived();
             }
         });
         onMobileSubscriptionManagerPublisher.processDeactivation(SubscriptionMapper.createOMSubscriptionRequest(subscription, deactivationRequest.getChannel()));
     }
 
     public void deactivationRequested(OMSubscriptionRequest omSubscriptionRequest) {
         onMobileSubscriptionGateway.deactivateSubscription(omSubscriptionRequest);
         updateStatusAndReport(omSubscriptionRequest.getSubscriptionId(), DateTime.now(), null, null, null, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.deactivationRequestSent();
             }
         });
     }
 
     public void renewSubscription(String subscriptionId, final DateTime renewedDate, Integer graceCount) {
         updateStatusAndReport(subscriptionId, renewedDate, null, null, graceCount, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.activateOnRenewal();
             }
         });
     }
 
     public void suspendSubscription(String subscriptionId, final DateTime renewalDate, String reason, Integer graceCount) {
         updateStatusAndReport(subscriptionId, renewalDate, reason, null, graceCount, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.suspendOnRenewal();
             }
         });
     }
 
     public void processDeactivation(String subscriptionId, final DateTime deactivationDate, String reason, Integer graceCount) {
         Subscription subscription = findBySubscriptionId(subscriptionId);
         if (subscription.isSubscriptionCompletionRequestSent())
             deactivateSubscription(subscriptionId, deactivationDate, reason, graceCount);
         else
             scheduleDeactivation(subscriptionId, deactivationDate, reason, graceCount);
     }
 
     private void scheduleDeactivation(String subscriptionId, final DateTime deactivationDate, String reason, Integer graceCount) {
         String subjectKey = SubscriptionEventKeys.DEACTIVATE_SUBSCRIPTION;
         Date startDate = DateTime.now().plusDays(kilkariPropertiesData.getBufferDaysToAllowRenewalForPackCompletion()).toDate();
         HashMap<String, Object> parameters = new HashMap<>();
         parameters.put(MotechSchedulerService.JOB_ID_KEY, subscriptionId);
         parameters.put("0", new ScheduleDeactivationRequest(subscriptionId, deactivationDate, reason, graceCount));
 
         MotechEvent motechEvent = new MotechEvent(subjectKey, parameters);
         RunOnceSchedulableJob runOnceSchedulableJob = new RunOnceSchedulableJob(motechEvent, startDate);
 
         motechSchedulerService.safeScheduleRunOnceJob(runOnceSchedulableJob);
     }
 
     public void deactivateSubscription(String subscriptionId, final DateTime deactivationDate, String reason, Integer graceCount) {
         updateStatusAndReport(subscriptionId, deactivationDate, reason, null, graceCount, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.deactivate();
                 inboxService.scheduleInboxDeletion(subscription.getSubscriptionId(), subscription.getCurrentWeeksMessageExpiryDate());
                 unScheduleCampaign(subscription);
                 campaignMessageAlertService.deleteFor(subscription.getSubscriptionId());
             }
         });
     }
 
     public void subscriptionComplete(OMSubscriptionRequest omSubscriptionRequest) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(omSubscriptionRequest.getSubscriptionId());
         if (subscription.isInDeactivatedState()) {
             logger.info(String.format("Cannot unsubscribe for subscriptionid: %s  msisdn: %s as it is already in the %s state", omSubscriptionRequest.getSubscriptionId(), omSubscriptionRequest.getMsisdn(), subscription.getStatus()));
             return;
         }
         onMobileSubscriptionGateway.deactivateSubscription(omSubscriptionRequest);
 
         updateStatusAndReport(subscription, DateTime.now(), "Subscription completed", null, null, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.complete();
                 inboxService.scheduleInboxDeletion(subscription.getSubscriptionId(), subscription.getCurrentWeeksMessageExpiryDate());
                 campaignMessageAlertService.deleteFor(subscription.getSubscriptionId());
             }
         });
     }
 
     public Subscription findBySubscriptionId(String subscriptionId) {
         return allSubscriptions.findBySubscriptionId(subscriptionId);
     }
 
     public void rescheduleCampaign(CampaignRescheduleRequest campaignRescheduleRequest) {
         String subscriptionId = campaignRescheduleRequest.getSubscriptionId();
         subscriptionValidator.validateActiveSubscriptionExists(subscriptionId);
 
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         DateTime nextAlertDateTime = messageCampaignService.getMessageTimings(subscriptionId, campaignRescheduleRequest.getCreatedAt(), campaignRescheduleRequest.getCreatedAt().plusMonths(1)).get(0);
         unScheduleCampaign(subscription);
         removeScheduledMessagesFromOBD(subscriptionId);
         scheduleCampaign(campaignRescheduleRequest, nextAlertDateTime);
     }
 
     public void updateSubscriberDetails(SubscriberRequest request) {
         subscriptionValidator.validateSubscriberDetails(request);
 
         SubscriberLocation subscriberLocation = new SubscriberLocation(request.getDistrict(), request.getBlock(), request.getPanchayat());
         reportingService.reportSubscriberDetailsChange(request.getSubscriptionId(), new SubscriberReportRequest(request.getCreatedAt(),
                 request.getBeneficiaryName(), request.getBeneficiaryAge(), request.getExpectedDateOfDelivery(), request.getDateOfBirth(), subscriberLocation));
     }
 
     public void unScheduleCampaign(Subscription subscription) {
         String activeCampaignName = messageCampaignService.getActiveCampaignName(subscription.getSubscriptionId());
         MessageCampaignRequest unEnrollRequest = new MessageCampaignRequest(subscription.getSubscriptionId(), activeCampaignName, subscription.getStartDate());
         messageCampaignService.stop(unEnrollRequest);
     }
 
     private void scheduleCampaign(CampaignRescheduleRequest campaignRescheduleRequest, DateTime nextAlertDateTime) {
         String campaignName = MessageCampaignPack.from(campaignRescheduleRequest.getReason().name()).getCampaignName();
         MessageCampaignRequest enrollRequest = new MessageCampaignRequest(campaignRescheduleRequest.getSubscriptionId(),
                 campaignName, nextAlertDateTime);
         messageCampaignService.start(enrollRequest, 0, kilkariPropertiesData.getCampaignScheduleDeltaMinutes());
     }
 
     private void scheduleCampaign(Subscription subscription, DateTime activatedOn) {
         String campaignName = MessageCampaignPack.from(subscription.getPack().name()).getCampaignName();
         MessageCampaignRequest campaignRequest = new MessageCampaignRequest(
                 subscription.getSubscriptionId(), campaignName, activatedOn);
         messageCampaignService.start(campaignRequest, kilkariPropertiesData.getCampaignScheduleDeltaDays(), kilkariPropertiesData.getCampaignScheduleDeltaMinutes());
     }
 
     private void removeScheduledMessagesFromOBD(String subscriptionId) {
         campaignMessageService.deleteCampaignMessagesFor(subscriptionId);
         campaignMessageAlertService.clearMessageId(subscriptionId);
     }
 
     private void updateStatusAndReport(String subscriptionId, DateTime updatedOn, String reason, String operator,
                                        Integer graceCount, Action<Subscription> action) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         updateStatusAndReport(subscription, updatedOn, reason, operator, graceCount, action);
     }
 
     private void updateStatusAndReport(Subscription subscription, DateTime updatedOn, String reason, String operator,
                                        Integer graceCount, Action<Subscription> action) {
         action.perform(subscription);
        logger.info("Updating Subscription and reporting change " + subscription);
         allSubscriptions.update(subscription);
         reportingService.reportSubscriptionStateChange(new SubscriptionStateChangeRequest(subscription.getSubscriptionId(),
                 subscription.getStatus().name(), reason, updatedOn, operator, graceCount));
     }
 
     private boolean shouldChangeMsisdn(Subscription subscription, ChangeMsisdnRequest changeMsisdnRequest) {
         if (changeMsisdnRequest.getShouldChangeAllPacks()) return true;
 
         return changeMsisdnRequest.getPacks().contains(subscription.getPack());
 
     }
 
     public void changeMsisdn(ChangeMsisdnRequest changeMsisdnRequest) {
         changeMsisdnValidator.validate(changeMsisdnRequest);
 
         String oldMsisdn = changeMsisdnRequest.getOldMsisdn();
         List<Subscription> subscriptionsInProgress = allSubscriptions.findSubscriptionsInProgress(oldMsisdn);
         for (Subscription subscription : subscriptionsInProgress) {
             if (!shouldChangeMsisdn(subscription, changeMsisdnRequest)) continue;
 
             if (subscription.getStatus().equals(SubscriptionStatus.NEW_EARLY))
                 changeMsisdnForEarlySubscription(subscription, changeMsisdnRequest);
             else
                 migrateMsisdnToNewSubscription(subscription, changeMsisdnRequest);
         }
     }
 
     private void migrateMsisdnToNewSubscription(Subscription subscription, ChangeMsisdnRequest changeMsisdnRequest) {
         requestDeactivation(new DeactivationRequest(subscription.getSubscriptionId(), changeMsisdnRequest.getChannel(), DateTime.now()));
 
         SubscriberResponse subscriberResponse = reportingService.getSubscriber(subscription.getSubscriptionId());
 
         Location location = null;
         if (subscriberResponse.getLocationResponse() != null) {
             location = new Location(subscriberResponse.getLocationResponse().getDistrict(),
                     subscriberResponse.getLocationResponse().getBlock(), subscriberResponse.getLocationResponse().getPanchayat());
         }
         Subscriber subscriber = new Subscriber(subscriberResponse.getBeneficiaryName(), subscriberResponse.getBeneficiaryAge(),
                 subscriberResponse.getDateOfBirth(), subscriberResponse.getExpectedDateOfDelivery(), subscription.getNextWeekNumber());
 
         SubscriptionRequest subscriptionRequest = new SubscriptionRequest(changeMsisdnRequest.getNewMsisdn(),
                 DateTime.now(), subscription.getPack(), location, subscriber);
         subscriptionRequest.setOldSubscriptionId(subscription.getSubscriptionId());
 
         createSubscriptionWithReporting(subscriptionRequest, changeMsisdnRequest.getChannel());
     }
 
     private void changeMsisdnForEarlySubscription(Subscription subscription, ChangeMsisdnRequest changeMsisdnRequest) {
         subscription.setMsisdn(changeMsisdnRequest.getNewMsisdn());
         allSubscriptions.update(subscription);
         reportingService.reportChangeMsisdnForSubscriber(subscription.getSubscriptionId(), changeMsisdnRequest.getNewMsisdn());
     }
 }
