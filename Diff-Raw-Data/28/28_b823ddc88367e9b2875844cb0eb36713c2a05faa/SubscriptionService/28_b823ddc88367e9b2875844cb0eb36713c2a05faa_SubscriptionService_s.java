 package org.motechproject.ananya.kilkari.subscription.service;
 
 import org.joda.time.DateTime;
 import org.joda.time.Weeks;
 import org.motechproject.ananya.kilkari.message.service.CampaignMessageAlertService;
 import org.motechproject.ananya.kilkari.message.service.InboxService;
 import org.motechproject.ananya.kilkari.messagecampaign.domain.MessageCampaignPack;
 import org.motechproject.ananya.kilkari.messagecampaign.request.MessageCampaignRequest;
 import org.motechproject.ananya.kilkari.messagecampaign.service.MessageCampaignService;
 import org.motechproject.ananya.kilkari.obd.domain.Channel;
 import org.motechproject.ananya.kilkari.obd.service.CampaignMessageService;
 import org.motechproject.ananya.kilkari.reporting.service.ReportingService;
 import org.motechproject.ananya.kilkari.subscription.domain.*;
 import org.motechproject.ananya.kilkari.subscription.repository.AllSubscriptions;
 import org.motechproject.ananya.kilkari.subscription.repository.KilkariPropertiesData;
 import org.motechproject.ananya.kilkari.subscription.repository.OnMobileSubscriptionGateway;
 import org.motechproject.ananya.kilkari.subscription.request.OMSubscriptionRequest;
 import org.motechproject.ananya.kilkari.subscription.service.mapper.SubscriptionDetailsResponseMapper;
 import org.motechproject.ananya.kilkari.subscription.service.mapper.SubscriptionMapper;
 import org.motechproject.ananya.kilkari.subscription.service.request.*;
 import org.motechproject.ananya.kilkari.subscription.service.response.SubscriptionDetailsResponse;
 import org.motechproject.ananya.kilkari.subscription.validators.ChangeMsisdnValidator;
 import org.motechproject.ananya.kilkari.subscription.validators.SubscriptionValidator;
 import org.motechproject.ananya.kilkari.subscription.validators.UnsubscriptionValidator;
 import org.motechproject.ananya.kilkari.sync.service.RefdataSyncService;
 import org.motechproject.ananya.reports.kilkari.contract.request.*;
 import org.motechproject.ananya.reports.kilkari.contract.response.LocationResponse;
 import org.motechproject.ananya.reports.kilkari.contract.response.SubscriberResponse;
 import org.motechproject.event.MotechEvent;
 import org.motechproject.scheduler.MotechSchedulerService;
 import org.motechproject.scheduler.domain.RunOnceSchedulableJob;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.Collections;
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
     private UnsubscriptionValidator unsubscriptionValidator;
     private RefdataSyncService refdataSyncService;
     private SubscriptionDetailsResponseMapper subscriptionDetailsResponseMapper;
 
     private final static Logger logger = LoggerFactory.getLogger(SubscriptionService.class);
 
     @Autowired
     public SubscriptionService(AllSubscriptions allSubscriptions, OnMobileSubscriptionManagerPublisher onMobileSubscriptionManagerPublisher,
                                SubscriptionValidator subscriptionValidator, ReportingService reportingService,
                                InboxService inboxService, MessageCampaignService messageCampaignService, OnMobileSubscriptionGateway onMobileSubscriptionGateway,
                                CampaignMessageService campaignMessageService, CampaignMessageAlertService campaignMessageAlertService, KilkariPropertiesData kilkariPropertiesData,
                                MotechSchedulerService motechSchedulerService, ChangeMsisdnValidator changeMsisdnValidator, UnsubscriptionValidator unsubscriptionValidator,
                                RefdataSyncService refdataSyncService, SubscriptionDetailsResponseMapper subscriptionDetailsResponseMapper) {
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
         this.unsubscriptionValidator = unsubscriptionValidator;
         this.refdataSyncService = refdataSyncService;
         this.subscriptionDetailsResponseMapper = subscriptionDetailsResponseMapper;
     }
 
     public Subscription createSubscription(SubscriptionRequest subscriptionRequest, Channel channel) {
         subscriptionValidator.validate(subscriptionRequest);
 
         Subscription subscription = new Subscription(subscriptionRequest.getMsisdn(), subscriptionRequest.getPack(),
                 subscriptionRequest.getCreationDate(), subscriptionRequest.getSubscriptionStartDate(), subscriptionRequest.getSubscriber().getWeek());
         allSubscriptions.add(subscription);
 
         Location location = subscriptionRequest.getLocation();
         LocationResponse existingLocation = getExistingLocation(location);
 
         SubscriptionReportRequest reportRequest = SubscriptionMapper.createSubscriptionCreationReportRequest(
                 subscription, channel, subscriptionRequest);
         reportingService.reportSubscriptionCreation(reportRequest);
 
         OMSubscriptionRequest omSubscriptionRequest = SubscriptionMapper.createOMSubscriptionRequest(subscription, channel);
         if (subscription.isEarlySubscription()) {
             scheduleEarlySubscription(subscriptionRequest.getSubscriptionStartDate(), omSubscriptionRequest);
         } else
             initiateActivationRequest(omSubscriptionRequest);
 
         if (existingLocation == null && subscriptionRequest.hasLocation()) {
             refdataSyncService.syncNewLocation(location.getDistrict(), location.getBlock(), location.getPanchayat());
         }
 
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
 
     public void initiateActivationRequestForEarlySubscription(OMSubscriptionRequest omSubscriptionRequest) {
         updateToLatestMsisdn(omSubscriptionRequest);
         onMobileSubscriptionManagerPublisher.sendActivationRequest(omSubscriptionRequest);
     }
 
     public List<Subscription> findByMsisdn(String msisdn) {
         return allSubscriptions.findByMsisdn(msisdn);
     }
 
     public List<Subscription> findByMsisdnAndPack(String msisdn, SubscriptionPack pack) {
         return allSubscriptions.findByMsisdnAndPack(msisdn, pack);
     }
 
     public void activate(String subscriptionId, final DateTime activatedOn, final String operator) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         if (!subscription.canActivate()) {
             logger.warn("Cannot ACTIVATE from state : " + subscription.getStatus());
             return;
         }
 
         final DateTime scheduleStartDateTime = subscription.getStartDateForSubscription(activatedOn);
         scheduleCampaign(subscription, scheduleStartDateTime);
         updateStatusAndReport(subscription, activatedOn, null, operator, null, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.activate(operator, getBufferedDateTime(scheduleStartDateTime), activatedOn);
             }
         });
         activateSchedule(subscription);
     }
 
     public void activationFailed(String subscriptionId, DateTime updatedOn, String reason, final String operator) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         if (!subscription.canFailActivation()) {
             logger.warn("Cannot move to ACTIVATION_FAILED state from state : " + subscription.getStatus());
             return;
         }
         updateStatusAndReport(subscription, updatedOn, reason, operator, null, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.activationFailed(operator);
             }
         });
     }
 
     public void activationRequested(OMSubscriptionRequest omSubscriptionRequest) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(omSubscriptionRequest.getSubscriptionId());
         if (!subscription.canSendActivationRequest()) {
             logger.warn("Cannot move to PENDING_ACTIVATION state from state : " + subscription.getStatus());
             return;
         }
 
         onMobileSubscriptionGateway.activateSubscription(omSubscriptionRequest);
         updateStatusWithoutReporting(subscription, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.activationRequestSent();
             }
         });
     }
 
     public void requestDeactivation(DeactivationRequest deactivationRequest) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(deactivationRequest.getSubscriptionId());
         if (subscription.isNewEarly()) {
             deactivateAndUnschedule(subscription, deactivationRequest);
             return;
         }
         if (!subscription.canReceiveDeactivationRequest()) {
             logger.warn("Cannot move to DEACTIVATION_REQUEST_RECEIVED state from state : " + subscription.getStatus());
             return;
         }
         updateStatusAndReport(subscription, deactivationRequest.getCreatedAt(), deactivationRequest.getReason(), null, null, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.deactivationRequestReceived();
             }
         });
         onMobileSubscriptionManagerPublisher.processDeactivation(SubscriptionMapper.createOMSubscriptionRequest(subscription, deactivationRequest.getChannel()));
     }
 
     public void requestUnsubscription(DeactivationRequest deactivationRequest) {
         unsubscriptionValidator.validate(deactivationRequest.getSubscriptionId());
         requestDeactivation(deactivationRequest);
     }
 
     public void deactivationRequested(OMSubscriptionRequest omSubscriptionRequest) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(omSubscriptionRequest.getSubscriptionId());
         if (!subscription.canMoveToPendingDeactivation()) {
             logger.warn("Cannot move to PENDING_DEACTIVATION state from state : " + subscription.getStatus());
             return;
         }
 
         onMobileSubscriptionGateway.deactivateSubscription(omSubscriptionRequest);
         updateStatusAndReport(subscription, DateTime.now(), null, null, null, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.deactivationRequestSent();
             }
         });
     }
 
     public void renewSubscription(String subscriptionId, final DateTime renewedDate, Integer graceCount) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         if (!subscription.canActivate()) {
             logger.warn("Cannot renew from state : " + subscription.getStatus());
             return;
         }
 
         updateStatusAndReport(subscription, renewedDate, null, null, graceCount, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.activateOnRenewal();
             }
         });
         renewSchedule(subscription);
     }
 
     public void suspendSubscription(String subscriptionId, final DateTime renewalDate, String reason, Integer graceCount) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         if (!subscription.canSuspend()) {
             logger.warn("Cannot suspend from state : " + subscription.getStatus());
             return;
         }
 
         updateStatusAndReport(subscription, renewalDate, reason, null, graceCount, new Action<Subscription>() {
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
 
     public void deactivateSubscription(String subscriptionId, final DateTime deactivationDate, String reason, Integer graceCount) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         if (!(subscription.canDeactivate() || subscription.canComplete())) {
             logger.warn("Cannot deactivate from state : " + subscription.getStatus());
             return;
         }
 
         updateStatusAndReport(subscription, deactivationDate, reason, null, graceCount, new Action<Subscription>() {
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
         if (!subscription.canMoveToPendingCompletion()) {
             logger.warn(String.format("Cannot unsubscribe for subscriptionid: %s  msisdn: %s as it is already in the %s state", omSubscriptionRequest.getSubscriptionId(), omSubscriptionRequest.getMsisdn(), subscription.getStatus()));
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
 
     public void changeMsisdn(ChangeMsisdnRequest changeMsisdnRequest) {
         changeMsisdnValidator.validate(changeMsisdnRequest);
 
         String oldMsisdn = changeMsisdnRequest.getOldMsisdn();
         List<Subscription> updatableSubscriptions = allSubscriptions.findUpdatableSubscriptions(oldMsisdn);
         for (Subscription subscription : updatableSubscriptions) {
             if (!shouldChangeMsisdn(subscription, changeMsisdnRequest)) continue;
 
             if (subscription.isNewEarly())
                 changeMsisdnForEarlySubscription(subscription, changeMsisdnRequest);
             else
                 migrateMsisdnToNewSubscription(subscription, changeMsisdnRequest);
         }
     }
 
     public void rescheduleCampaign(CampaignRescheduleRequest campaignRescheduleRequest) {
         String subscriptionId = campaignRescheduleRequest.getSubscriptionId();
         subscriptionValidator.validateChangeCampaign(subscriptionId, campaignRescheduleRequest.getReason());
 
         Subscription subscription = allSubscriptions.findBySubscriptionId(subscriptionId);
         DateTime nextAlertDateTime = messageCampaignService.getMessageTimings(subscriptionId, campaignRescheduleRequest.getCreatedAt(), campaignRescheduleRequest.getCreatedAt().plusMonths(1)).get(0);
         unScheduleCampaign(subscription);
         removeScheduledMessagesFromOBD(subscriptionId);
         scheduleCampaign(campaignRescheduleRequest, nextAlertDateTime);
     }
 
     public void updateSubscriberDetails(SubscriberRequest request) {
         subscriptionValidator.validateSubscriberDetails(request);
 
         Location location = request.getLocation();
         LocationResponse existingLocation = getExistingLocation(location);
 
         SubscriberLocation subscriberLocation = request.hasLocation() ? new SubscriberLocation(location.getDistrict(), location.getBlock(), location.getPanchayat()) : null;
         reportingService.reportSubscriberDetailsChange(request.getSubscriptionId(), new SubscriberReportRequest(request.getCreatedAt(),
                 request.getBeneficiaryName(), request.getBeneficiaryAge(), subscriberLocation));
 
         if (existingLocation == null && request.hasLocation()) {
             refdataSyncService.syncNewLocation(location.getDistrict(), location.getBlock(), location.getPanchayat());
         }
     }
 
     public void unScheduleCampaign(Subscription subscription) {
         String activeCampaignName = messageCampaignService.getActiveCampaignName(subscription.getSubscriptionId());
         MessageCampaignRequest unEnrollRequest = new MessageCampaignRequest(subscription.getSubscriptionId(), activeCampaignName, subscription.getScheduleStartDate());
         messageCampaignService.stop(unEnrollRequest);
     }
 
     public List<SubscriptionDetailsResponse> getSubscriptionDetails(String msisdn, Channel channel) {
         List<Subscription> subscriptionList = findByMsisdn(msisdn);
         if (Channel.IVR.equals(channel)) {
             return subscriptionDetailsResponseMapper.map(subscriptionList, Collections.EMPTY_LIST);
         }
 
         List<SubscriberResponse> subscriberDetailsFromReports = reportingService.getSubscribersByMsisdn(msisdn);
         return subscriptionDetailsResponseMapper.map(subscriptionList, subscriberDetailsFromReports);
     }
 
     public void scheduleCompletion(Subscription subscription, DateTime completionDate) {
         String subjectKey = SubscriptionEventKeys.SUBSCRIPTION_COMPLETE;
         HashMap<String, Object> parameters = new HashMap<>();
         parameters.put(MotechSchedulerService.JOB_ID_KEY, subscription.getSubscriptionId());
         parameters.put("0", SubscriptionMapper.createOMSubscriptionRequest(subscription, Channel.MOTECH));
 
         MotechEvent motechEvent = new MotechEvent(subjectKey, parameters);
         RunOnceSchedulableJob runOnceSchedulableJob = new RunOnceSchedulableJob(motechEvent, completionDate.toDate());
 
         motechSchedulerService.safeScheduleRunOnceJob(runOnceSchedulableJob);
     }
 
     private void updateToLatestMsisdn(OMSubscriptionRequest omSubscriptionRequest) {
         Subscription subscription = allSubscriptions.findBySubscriptionId(omSubscriptionRequest.getSubscriptionId());
         omSubscriptionRequest.setMsisdn(subscription.getMsisdn());
     }
 
     private void deactivateAndUnschedule(Subscription subscription, DeactivationRequest deactivationRequest) {
         motechSchedulerService.safeUnscheduleRunOnceJob(SubscriptionEventKeys.EARLY_SUBSCRIPTION, deactivationRequest.getSubscriptionId());
         updateStatusAndReport(subscription, deactivationRequest.getCreatedAt(), deactivationRequest.getReason(), null, null, new Action<Subscription>() {
             @Override
             public void perform(Subscription subscription) {
                 subscription.deactivate();
             }
         });
     }
 
     private LocationResponse getExistingLocation(Location location) {
         if (location == Location.NULL)
             return null;
 
         return reportingService.getLocation(location.getDistrict(), location.getBlock(), location.getPanchayat());
     }
 
     private void renewSchedule(Subscription subscription) {
         String subscriptionId = subscription.getSubscriptionId();
         logger.info(String.format("Processing renewal for subscriptionId: %s", subscriptionId));
         campaignMessageAlertService.scheduleCampaignMessageAlertForRenewal(subscriptionId, subscription.getMsisdn(), subscription.getOperator().name());
         rescheduleSubscriptionCompletionIfExists(subscription);
     }
 
     private void rescheduleSubscriptionCompletionIfExists(Subscription subscription) {
         if (!subscription.isCampaignCompleted()) return;
         scheduleCompletion(subscription, DateTime.now());
         logger.info(String.format("Rescheduled the completion of subscription %s to now", subscription.getSubscriptionId()));
     }
 
     private void scheduleCampaign(CampaignRescheduleRequest campaignRescheduleRequest, DateTime nextAlertDateTime) {
         String campaignName = MessageCampaignPack.from(campaignRescheduleRequest.getReason().name()).getCampaignName();
         MessageCampaignRequest enrollRequest = new MessageCampaignRequest(campaignRescheduleRequest.getSubscriptionId(),
                 campaignName, nextAlertDateTime);
         messageCampaignService.start(enrollRequest, 0, kilkariPropertiesData.getCampaignScheduleDeltaMinutes());
     }
 
     private void scheduleDeactivation(String subscriptionId, final DateTime deactivationDate, String reason, Integer graceCount) {
         String subjectKey = SubscriptionEventKeys.DEACTIVATE_SUBSCRIPTION;
         Date startDate = DateTime.now().plusDays(kilkariPropertiesData.getBufferDaysToAllowRenewalForDeactivation()).toDate();
         HashMap<String, Object> parameters = new HashMap<>();
         parameters.put(MotechSchedulerService.JOB_ID_KEY, subscriptionId);
         parameters.put("0", new ScheduleDeactivationRequest(subscriptionId, deactivationDate, reason, graceCount));
 
         MotechEvent motechEvent = new MotechEvent(subjectKey, parameters);
         RunOnceSchedulableJob runOnceSchedulableJob = new RunOnceSchedulableJob(motechEvent, startDate);
 
         motechSchedulerService.safeScheduleRunOnceJob(runOnceSchedulableJob);
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
 
     private DateTime getBufferedDateTime(DateTime dateTime) {
         return dateTime.plusDays(kilkariPropertiesData.getCampaignScheduleDeltaDays())
                 .plusMinutes(kilkariPropertiesData.getCampaignScheduleDeltaMinutes());
     }
 
     private void activateSchedule(Subscription subscription) {
         String subscriptionId = subscription.getSubscriptionId();
         logger.info(String.format("Processing activation for subscriptionId: %s", subscriptionId));
 
         String currentMessageId = campaignMessageAlertService.scheduleCampaignMessageAlertForActivation(subscriptionId, subscription.getMsisdn(), subscription.getOperator().name());
 
         if (currentMessageId != null)
             inboxService.newMessage(subscriptionId, currentMessageId);
     }
 
     private void updateStatusAndReport(Subscription subscription, DateTime updatedOn, String reason, String operator,
                                        Integer graceCount, Action<Subscription> action) {
         action.perform(subscription);
         logger.info("Updating Subscription and reporting change " + subscription);
         allSubscriptions.update(subscription);
         reportingService.reportSubscriptionStateChange(new SubscriptionStateChangeRequest(subscription.getSubscriptionId(),
                 subscription.getStatus().name(), reason, updatedOn, operator, graceCount, getSubscriptionWeekNumber(subscription, updatedOn)));
     }
 
     private Integer getSubscriptionWeekNumber(Subscription subscription, DateTime endDate) {
         if (subscription.getScheduleStartDate() == null)
             return null;
         Integer diffInWeeks = Weeks.weeksBetween(subscription.getScheduleStartDate(), endDate).getWeeks();
         return subscription.getPack().getStartWeek() + diffInWeeks;
     }
 
     private void updateStatusWithoutReporting(Subscription subscription, Action<Subscription> action) {
         action.perform(subscription);
         logger.info("Updating Subscription and reporting change " + subscription);
         allSubscriptions.update(subscription);
     }
 
     private boolean shouldChangeMsisdn(Subscription subscription, ChangeMsisdnRequest changeMsisdnRequest) {
         if (changeMsisdnRequest.getShouldChangeAllPacks()) return true;
 
         return changeMsisdnRequest.getPacks().contains(subscription.getPack());
     }
 
     private void migrateMsisdnToNewSubscription(Subscription subscription, ChangeMsisdnRequest changeMsisdnRequest) {
         SubscriberResponse subscriberResponse = reportingService.getSubscriber(subscription.getSubscriptionId());
 
         requestDeactivation(new DeactivationRequest(subscription.getSubscriptionId(), changeMsisdnRequest.getChannel(), DateTime.now(), changeMsisdnRequest.getReason()));
 
         Location location = null;
         if (subscriberResponse.getLocationResponse() != null) {
             location = new Location(subscriberResponse.getLocationResponse().getDistrict(),
                     subscriberResponse.getLocationResponse().getBlock(), subscriberResponse.getLocationResponse().getPanchayat());
         }
         Subscriber subscriber = new Subscriber(subscriberResponse.getBeneficiaryName(), subscriberResponse.getBeneficiaryAge(),
                 subscriberResponse.getDateOfBirth(), subscriberResponse.getExpectedDateOfDelivery(), subscription.getNextWeekNumber());
 
         SubscriptionRequest subscriptionRequest = new SubscriptionRequest(changeMsisdnRequest.getNewMsisdn(),
                 DateTime.now(), subscription.getPack(), location, subscriber, changeMsisdnRequest.getReason());
         subscriptionRequest.setOldSubscriptionId(subscription.getSubscriptionId());
 
         createSubscription(subscriptionRequest, changeMsisdnRequest.getChannel());
     }
 
     private void changeMsisdnForEarlySubscription(Subscription subscription, ChangeMsisdnRequest changeMsisdnRequest) {
         subscription.setMsisdn(changeMsisdnRequest.getNewMsisdn());
         allSubscriptions.update(subscription);
         reportingService.reportChangeMsisdnForEarlySubscription(new SubscriberChangeMsisdnReportRequest(subscription.getSubscriptionId(), Long.valueOf(changeMsisdnRequest.getNewMsisdn()), changeMsisdnRequest.getReason()));
     }
 
     public void updateSubscription(Subscription subscription) {
         try {
             allSubscriptions.update(subscription);
         } catch (IllegalArgumentException exception) {
             throw new IllegalArgumentException(String.format("Subscription %s does not exist in db", subscription.getSubscriptionId()));
         }
     }
 }
