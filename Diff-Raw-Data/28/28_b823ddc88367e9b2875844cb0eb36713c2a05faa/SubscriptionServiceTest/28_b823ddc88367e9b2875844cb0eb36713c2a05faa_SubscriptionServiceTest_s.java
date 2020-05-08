 package org.motechproject.ananya.kilkari.subscription.service;
 
 import junit.framework.Assert;
 import org.joda.time.DateTime;
 import org.joda.time.Weeks;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InOrder;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.message.repository.AllInboxMessages;
 import org.motechproject.ananya.kilkari.message.service.CampaignMessageAlertService;
 import org.motechproject.ananya.kilkari.message.service.InboxService;
 import org.motechproject.ananya.kilkari.messagecampaign.domain.MessageCampaignPack;
 import org.motechproject.ananya.kilkari.messagecampaign.request.MessageCampaignRequest;
 import org.motechproject.ananya.kilkari.messagecampaign.service.MessageCampaignService;
 import org.motechproject.ananya.kilkari.obd.domain.Channel;
 import org.motechproject.ananya.kilkari.obd.service.CampaignMessageService;
 import org.motechproject.ananya.kilkari.reporting.service.ReportingServiceImpl;
 import org.motechproject.ananya.kilkari.subscription.builder.SubscriptionBuilder;
 import org.motechproject.ananya.kilkari.subscription.builder.SubscriptionRequestBuilder;
 import org.motechproject.ananya.kilkari.subscription.domain.*;
 import org.motechproject.ananya.kilkari.subscription.exceptions.ValidationException;
 import org.motechproject.ananya.kilkari.subscription.repository.AllSubscriptions;
 import org.motechproject.ananya.kilkari.subscription.repository.KilkariPropertiesData;
 import org.motechproject.ananya.kilkari.subscription.repository.OnMobileSubscriptionGateway;
 import org.motechproject.ananya.kilkari.subscription.request.OMSubscriptionRequest;
 import org.motechproject.ananya.kilkari.subscription.service.mapper.SubscriptionDetailsResponseMapper;
 import org.motechproject.ananya.kilkari.subscription.service.request.ChangeMsisdnRequest;
 import org.motechproject.ananya.kilkari.subscription.service.request.Location;
 import org.motechproject.ananya.kilkari.subscription.service.request.SubscriberRequest;
 import org.motechproject.ananya.kilkari.subscription.service.request.SubscriptionRequest;
 import org.motechproject.ananya.kilkari.subscription.service.response.SubscriptionDetailsResponse;
 import org.motechproject.ananya.kilkari.subscription.validators.ChangeMsisdnValidator;
 import org.motechproject.ananya.kilkari.subscription.validators.SubscriptionValidator;
 import org.motechproject.ananya.kilkari.subscription.validators.UnsubscriptionValidator;
 import org.motechproject.ananya.kilkari.sync.service.RefdataSyncService;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriberChangeMsisdnReportRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriberReportRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriptionReportRequest;
 import org.motechproject.ananya.reports.kilkari.contract.request.SubscriptionStateChangeRequest;
 import org.motechproject.ananya.reports.kilkari.contract.response.LocationResponse;
 import org.motechproject.ananya.reports.kilkari.contract.response.SubscriberResponse;
 import org.motechproject.scheduler.MotechSchedulerService;
 import org.motechproject.scheduler.domain.RunOnceSchedulableJob;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertNull;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class SubscriptionServiceTest {
 
     private SubscriptionService subscriptionService;
 
     @Rule
     public ExpectedException expectedException = ExpectedException.none();
     @Mock
     private AllSubscriptions allSubscriptions;
     @Mock
     private OnMobileSubscriptionManagerPublisher onMobileSubscriptionManagerPublisher;
     @Mock
     private SubscriptionValidator subscriptionValidator;
     @Mock
     private ReportingServiceImpl reportingServiceImpl;
     @Mock
     private AllInboxMessages allInboxMessages;
     @Mock
     private InboxService inboxService;
     @Mock
     private MessageCampaignService messageCampaignService;
     @Mock
     private OnMobileSubscriptionGateway onMobileSubscriptionGateway;
     @Mock
     private CampaignMessageService campaignMessageService;
     @Mock
     private CampaignMessageAlertService campaignMessageAlertService;
     @Mock
     private KilkariPropertiesData kilkariPropertiesData;
     @Mock
     private MotechSchedulerService motechSchedulerService;
     @Mock
     private ChangeMsisdnValidator changeMsisdnValidator;
     @Mock
     private UnsubscriptionValidator unsubscriptionValidator;
     @Mock
     private RefdataSyncService refdataSyncService;
     @Mock
     private SubscriptionDetailsResponseMapper subscriptionDetailsResponseMapper;
 
     @Before
     public void setUp() {
         initMocks(this);
         subscriptionService = new SubscriptionService(allSubscriptions, onMobileSubscriptionManagerPublisher, subscriptionValidator, reportingServiceImpl,
                 inboxService, messageCampaignService, onMobileSubscriptionGateway, campaignMessageService, campaignMessageAlertService, kilkariPropertiesData, motechSchedulerService, changeMsisdnValidator, unsubscriptionValidator, refdataSyncService, subscriptionDetailsResponseMapper);
     }
 
     @Test
     public void shouldCreateNewSubscription() {
         String msisdn = "1234567890";
         Channel channel = Channel.IVR;
         SubscriptionPack subscriptionPack = SubscriptionPack.NAVJAAT_KILKARI;
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         Integer weekNumber = 7;
         SubscriptionRequest subscription = new SubscriptionRequestBuilder().withDefaults().withMsisdn(msisdn)
                 .withPack(subscriptionPack).withWeek(weekNumber).build();
 
         Subscription createdSubscription = subscriptionService.createSubscription(subscription, channel);
 
         assertNotNull(createdSubscription);
         verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription subscriptionSaved = subscriptionArgumentCaptor.getValue();
         assertEquals(msisdn, subscriptionSaved.getMsisdn());
         assertEquals(weekNumber, subscriptionSaved.getStartWeekNumber());
         assertEquals(subscriptionPack, subscriptionSaved.getPack());
         assertEquals(createdSubscription, subscriptionSaved);
     }
 
     @Test
     public void shouldValidateSubscriptionRequestOnCreation() {
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         verify(subscriptionValidator).validate(any(SubscriptionRequest.class));
     }
 
     @Test
     public void shouldNotCreateSubscriptionIfValidationFails() {
         SubscriptionRequest subscription = new SubscriptionRequestBuilder().withDefaults().build();
         doThrow(new ValidationException("")).when(subscriptionValidator).validate(any(SubscriptionRequest.class));
 
         try {
             subscriptionService.createSubscription(subscription, Channel.CONTACT_CENTER);
         } catch (ValidationException e) {
             //ignore
         }
 
         verify(allSubscriptions, never()).add(any(Subscription.class));
         verify(messageCampaignService, never()).start(any(MessageCampaignRequest.class), any(Integer.class), any(Integer.class));
         verify(reportingServiceImpl, never()).reportSubscriptionCreation(any(SubscriptionReportRequest.class));
         verify(onMobileSubscriptionManagerPublisher, never()).sendActivationRequest(any(OMSubscriptionRequest.class));
         verify(refdataSyncService, never()).syncNewLocation(anyString(), anyString(), anyString());
     }
 
     @Test
     public void shouldPublishNewAndPendingActivationReportingEventsOnCreatingSubscription() {
         String msisdn = "1234567890";
         Channel channel = Channel.IVR;
         SubscriptionPack subscriptionPack = SubscriptionPack.NAVJAAT_KILKARI;
         ArgumentCaptor<SubscriptionReportRequest> subscriptionReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionReportRequest.class);
         ArgumentCaptor<OMSubscriptionRequest> subscriptionActivationRequestArgumentCaptor = ArgumentCaptor.forClass(OMSubscriptionRequest.class);
         SubscriptionRequest subscription = new SubscriptionRequestBuilder().withDefaults().withMsisdn(msisdn).withPack(subscriptionPack).build();
 
         subscriptionService.createSubscription(subscription, channel);
 
         InOrder order = inOrder(reportingServiceImpl, onMobileSubscriptionManagerPublisher);
         order.verify(reportingServiceImpl).reportSubscriptionCreation(subscriptionReportRequestArgumentCaptor.capture());
         SubscriptionReportRequest actualSubscriptionCreationReportRequest = subscriptionReportRequestArgumentCaptor.getValue();
         assertEquals(msisdn, actualSubscriptionCreationReportRequest.getMsisdn().toString());
         assertEquals(subscriptionPack.name(), actualSubscriptionCreationReportRequest.getPack());
         assertEquals(channel.name(), actualSubscriptionCreationReportRequest.getChannel());
 
         order.verify(onMobileSubscriptionManagerPublisher).sendActivationRequest(subscriptionActivationRequestArgumentCaptor.capture());
         OMSubscriptionRequest actualOMSubscriptionRequest = subscriptionActivationRequestArgumentCaptor.getValue();
         assertEquals(msisdn, actualOMSubscriptionRequest.getMsisdn());
         assertEquals(subscriptionPack, actualOMSubscriptionRequest.getPack());
         assertEquals(channel, actualOMSubscriptionRequest.getChannel());
     }
 
     @Test
     public void shouldGetSubscriptionsResponseForAGivenMsisdn() {
         String msisdn = "1234567890";
         ArrayList<Subscription> subscriptionsToBeReturned = new ArrayList<>();
         Subscription subscription1 = new Subscription(msisdn, SubscriptionPack.NAVJAAT_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription1.setStatus(SubscriptionStatus.NEW);
         subscriptionsToBeReturned.add(subscription1);
 
         Subscription subscription2 = new Subscription(msisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription2.setStatus(SubscriptionStatus.NEW);
         subscriptionsToBeReturned.add(subscription2);
 
         when(allSubscriptions.findByMsisdn(msisdn)).thenReturn(subscriptionsToBeReturned);
 
         List<Subscription> subscriptions = subscriptionService.findByMsisdn(msisdn);
 
         assertEquals(2, subscriptions.size());
         assertEquals(msisdn, subscriptions.get(0).getMsisdn());
         assertEquals(SubscriptionPack.NAVJAAT_KILKARI, subscriptions.get(0).getPack());
         assertEquals(msisdn, subscriptions.get(1).getMsisdn());
         assertEquals(SubscriptionPack.BARI_KILKARI, subscriptions.get(1).getPack());
     }
 
     @Test
     public void shouldGetSubscriptionsForAGivenMsisdnAndPack() {
         String msisdn = "1234567890";
         ArrayList<Subscription> subscriptionsToBeReturned = new ArrayList<>();
         SubscriptionPack pack = SubscriptionPack.NAVJAAT_KILKARI;
         Subscription subscription1 = new Subscription(msisdn, pack, DateTime.now(), DateTime.now(), null);
         subscription1.setStatus(SubscriptionStatus.NEW);
         subscriptionsToBeReturned.add(subscription1);
 
         Subscription subscription2 = new Subscription(msisdn, pack, DateTime.now(), DateTime.now(), null);
         subscription2.setStatus(SubscriptionStatus.NEW);
         subscriptionsToBeReturned.add(subscription2);
 
         when(allSubscriptions.findByMsisdnAndPack(msisdn, pack)).thenReturn(subscriptionsToBeReturned);
 
         List<Subscription> subscriptions = subscriptionService.findByMsisdnAndPack(msisdn, pack);
 
         assertEquals(2, subscriptions.size());
         assertEquals(msisdn, subscriptions.get(0).getMsisdn());
         assertEquals(pack, subscriptions.get(0).getPack());
         assertEquals(msisdn, subscriptions.get(1).getMsisdn());
         assertEquals(pack, subscriptions.get(1).getPack());
     }
 
     @Test
     public void shouldUpdateTheSubscriptionStatusToPendingActivation_WhenActivationIsRequested() {
         String subscriptionId = "abcd1234";
         SubscriptionStatus status = SubscriptionStatus.PENDING_ACTIVATION;
         Subscription mockedSubscription = mock(Subscription.class);
         OMSubscriptionRequest omSubscriptionRequest = new OMSubscriptionRequest("1234567890", SubscriptionPack.BARI_KILKARI, Channel.IVR, subscriptionId);
         when(mockedSubscription.getStatus()).thenReturn(status);
         when(mockedSubscription.getSubscriptionId()).thenReturn(subscriptionId);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(mockedSubscription);
         when(mockedSubscription.canSendActivationRequest()).thenReturn(true);
 
         subscriptionService.activationRequested(omSubscriptionRequest);
 
         InOrder order = inOrder(allSubscriptions, mockedSubscription, reportingServiceImpl);
         order.verify(allSubscriptions).findBySubscriptionId(subscriptionId);
         order.verify(mockedSubscription).activationRequestSent();
         order.verify(allSubscriptions).update(mockedSubscription);
         ArgumentCaptor<SubscriptionStateChangeRequest> subscriptionStateChangeReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         verify(onMobileSubscriptionGateway).activateSubscription(omSubscriptionRequest);
         verify(reportingServiceImpl, never()).reportSubscriptionStateChange(any(SubscriptionStateChangeRequest.class));
     }
 
     @Test
     public void shouldUpdateTheSubscriptionStatusToDeactivationRequested_WhenDeactivationIsRequested() {
         String subscriptionId = "abcd1234";
         SubscriptionStatus status = SubscriptionStatus.ACTIVE;
         Subscription mockedSubscription = mock(Subscription.class);
         String reason = "some reason";
 
         when(mockedSubscription.getStatus()).thenReturn(status);
         when(mockedSubscription.getSubscriptionId()).thenReturn(subscriptionId);
         when(mockedSubscription.isInProgress()).thenReturn(true);
         when(mockedSubscription.canReceiveDeactivationRequest()).thenReturn(true);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(mockedSubscription);
 
         subscriptionService.requestDeactivation(new DeactivationRequest(subscriptionId, Channel.IVR, DateTime.now(), reason));
 
         InOrder order = inOrder(allSubscriptions, mockedSubscription, reportingServiceImpl);
         order.verify(allSubscriptions).findBySubscriptionId(subscriptionId);
         order.verify(mockedSubscription).deactivationRequestReceived();
         order.verify(allSubscriptions).update(mockedSubscription);
         ArgumentCaptor<SubscriptionStateChangeRequest> subscriptionStateChangeReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         order.verify(reportingServiceImpl).reportSubscriptionStateChange(subscriptionStateChangeReportRequestArgumentCaptor.capture());
         SubscriptionStateChangeRequest subscriptionStateChangeReportRequest = subscriptionStateChangeReportRequestArgumentCaptor.getValue();
 
         assertEquals(subscriptionId, subscriptionStateChangeReportRequest.getSubscriptionId());
         assertEquals(status.name(), subscriptionStateChangeReportRequest.getSubscriptionStatus());
         assertEquals(reason, subscriptionStateChangeReportRequest.getReason());
         assertNull(subscriptionStateChangeReportRequest.getOperator());
     }
 
     @Test
     public void shouldUpdateTheSubscriptionStatusToPendingDeactivation_WhenDeactivationRequestedIsComplete() {
         String subscriptionId = "abcd1234";
         SubscriptionStatus status = SubscriptionStatus.DEACTIVATION_REQUEST_RECEIVED;
         Subscription mockedSubscription = mock(Subscription.class);
         OMSubscriptionRequest omSubscriptionRequest = new OMSubscriptionRequest("1234567890", SubscriptionPack.BARI_KILKARI, Channel.IVR, subscriptionId);
         when(mockedSubscription.getStatus()).thenReturn(status);
         when(mockedSubscription.getSubscriptionId()).thenReturn(subscriptionId);
         when(mockedSubscription.canMoveToPendingDeactivation()).thenReturn(true);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(mockedSubscription);
 
         subscriptionService.deactivationRequested(omSubscriptionRequest);
 
         InOrder order = inOrder(allSubscriptions, mockedSubscription, reportingServiceImpl);
         order.verify(allSubscriptions).findBySubscriptionId(subscriptionId);
         order.verify(mockedSubscription).deactivationRequestSent();
         order.verify(allSubscriptions).update(mockedSubscription);
         ArgumentCaptor<SubscriptionStateChangeRequest> subscriptionStateChangeReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         order.verify(reportingServiceImpl).reportSubscriptionStateChange(subscriptionStateChangeReportRequestArgumentCaptor.capture());
         SubscriptionStateChangeRequest subscriptionStateChangeReportRequest = subscriptionStateChangeReportRequestArgumentCaptor.getValue();
         verify(onMobileSubscriptionGateway).deactivateSubscription(omSubscriptionRequest);
         assertEquals(subscriptionId, subscriptionStateChangeReportRequest.getSubscriptionId());
         assertEquals(status.name(), subscriptionStateChangeReportRequest.getSubscriptionStatus());
         assertNull(subscriptionStateChangeReportRequest.getOperator());
     }
 
     @Test
     public void shouldUpdateTheSubscriptionStatusToActivationFailed_GivenSubscriptionId() {
         SubscriptionStatus status = SubscriptionStatus.ACTIVATION_FAILED;
         Subscription mockedSubscription = mock(Subscription.class);
         String subscriptionId = "abcd1234";
         String operator = Operator.AIRTEL.name();
         String reason = "Activation Failed For some error";
 
         when(mockedSubscription.getStatus()).thenReturn(status);
         when(mockedSubscription.getSubscriptionId()).thenReturn(subscriptionId);
         when(mockedSubscription.canFailActivation()).thenReturn(true);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(mockedSubscription);
 
         subscriptionService.activationFailed(subscriptionId, DateTime.now(), reason, operator);
 
         InOrder order = inOrder(allSubscriptions, mockedSubscription, reportingServiceImpl);
         order.verify(allSubscriptions).findBySubscriptionId(subscriptionId);
         order.verify(mockedSubscription).activationFailed(operator);
         order.verify(allSubscriptions).update(mockedSubscription);
         ArgumentCaptor<SubscriptionStateChangeRequest> subscriptionStateChangeReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         order.verify(reportingServiceImpl).reportSubscriptionStateChange(subscriptionStateChangeReportRequestArgumentCaptor.capture());
         SubscriptionStateChangeRequest subscriptionStateChangeReportRequest = subscriptionStateChangeReportRequestArgumentCaptor.getValue();
 
         assertEquals(subscriptionId, subscriptionStateChangeReportRequest.getSubscriptionId());
         assertEquals(status.name(), subscriptionStateChangeReportRequest.getSubscriptionStatus());
         assertEquals(reason, subscriptionStateChangeReportRequest.getReason());
         assertEquals(operator, subscriptionStateChangeReportRequest.getOperator());
         assertNull(subscriptionStateChangeReportRequest.getWeekNumber());
     }
 
     @Test
     public void shouldUpdateInboxDuringActivationWhenMessageHasAlreadyBeenScheduled() {
         Subscription subscription = new SubscriptionBuilder().withDefaults().build();
         String operator = subscription.getOperator().name();
         DateTime activatedOn = DateTime.now();
         String subscriptionId = subscription.getSubscriptionId();
         int deltaDays = 2;
         int deltaMinutes = 30;
         String messageId = "mesasgeId";
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         when(kilkariPropertiesData.getCampaignScheduleDeltaDays()).thenReturn(deltaDays);
         when(kilkariPropertiesData.getCampaignScheduleDeltaMinutes()).thenReturn(deltaMinutes);
         when(campaignMessageAlertService.scheduleCampaignMessageAlertForActivation(subscriptionId, subscription.getMsisdn(), operator)).thenReturn(messageId);
 
         subscriptionService.activate(subscriptionId, activatedOn, operator);
 
         verify(inboxService).newMessage(subscriptionId, messageId);
     }
 
     @Test
     public void shouldNotUpdateInboxDuringActivationWhenMessageHasNotAlreadyBeenScheduled() {
         Subscription subscription = new SubscriptionBuilder().withDefaults().build();
         String operator = subscription.getOperator().name();
         DateTime activatedOn = DateTime.now();
         String subscriptionId = subscription.getSubscriptionId();
         int deltaDays = 2;
         int deltaMinutes = 30;
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         when(kilkariPropertiesData.getCampaignScheduleDeltaDays()).thenReturn(deltaDays);
         when(kilkariPropertiesData.getCampaignScheduleDeltaMinutes()).thenReturn(deltaMinutes);
 
         subscriptionService.activate(subscriptionId, activatedOn, operator);
 
         verify(inboxService, never()).newMessage(anyString(), anyString());
     }
 
     @Test
     public void shouldCallCampaignMessageAlertServiceOnActivation() {
         Subscription subscription = new SubscriptionBuilder().withDefaults().build();
         String operator = subscription.getOperator().name();
         DateTime activatedOn = DateTime.now();
         String subscriptionId = subscription.getSubscriptionId();
         int deltaDays = 2;
         int deltaMinutes = 30;
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         when(kilkariPropertiesData.getCampaignScheduleDeltaDays()).thenReturn(deltaDays);
         when(kilkariPropertiesData.getCampaignScheduleDeltaMinutes()).thenReturn(deltaMinutes);
 
         subscriptionService.activate(subscriptionId, activatedOn, operator);
 
         verify(campaignMessageAlertService).scheduleCampaignMessageAlertForActivation(subscriptionId, subscription.getMsisdn(), subscription.getOperator().name());
     }
 
     @Test
     public void shouldActivateSubscriptionAndScheduleCampaign() {
         Subscription subscription = new SubscriptionBuilder().withDefaults().build();
         String operator = "airtel";
         DateTime activatedOn = DateTime.now();
         String subscriptionId = subscription.getSubscriptionId();
         int deltaDays = 2;
         int deltaMinutes = 30;
         DateTime scheduleStartDate = activatedOn.plusDays(deltaDays).plusMinutes(deltaMinutes);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         when(kilkariPropertiesData.getCampaignScheduleDeltaDays()).thenReturn(deltaDays);
         when(kilkariPropertiesData.getCampaignScheduleDeltaMinutes()).thenReturn(deltaMinutes);
 
         subscriptionService.activate(subscriptionId, activatedOn, operator);
 
         ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).update(captor.capture());
         Subscription actualSubscription = captor.getValue();
         assertEquals(SubscriptionStatus.ACTIVE, actualSubscription.getStatus());
         assertEquals(subscriptionId, actualSubscription.getSubscriptionId());
         assertEquals(activatedOn.withSecondOfMinute(0).withMillisOfSecond(0), actualSubscription.getActivationDate());
         assertEquals(scheduleStartDate.withSecondOfMinute(0).withMillisOfSecond(0), actualSubscription.getScheduleStartDate());
 
         ArgumentCaptor<SubscriptionStateChangeRequest> subscriptionStateChangeReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         verify(reportingServiceImpl).reportSubscriptionStateChange(subscriptionStateChangeReportRequestArgumentCaptor.capture());
         SubscriptionStateChangeRequest stateChangeReportRequest = subscriptionStateChangeReportRequestArgumentCaptor.getValue();
         assertEquals(operator, stateChangeReportRequest.getOperator());
         assertEquals(subscriptionId, stateChangeReportRequest.getSubscriptionId());
         assertEquals(SubscriptionStatus.ACTIVE.name(), stateChangeReportRequest.getSubscriptionStatus());
         assertEquals(1, (int) stateChangeReportRequest.getWeekNumber());
 
         ArgumentCaptor<MessageCampaignRequest> messageCampaignRequestArgumentCaptor = ArgumentCaptor.forClass(MessageCampaignRequest.class);
         ArgumentCaptor<Integer> actualDeltaDays = ArgumentCaptor.forClass(Integer.class);
         ArgumentCaptor<Integer> actualDeltaMinutes = ArgumentCaptor.forClass(Integer.class);
         verify(messageCampaignService).start(messageCampaignRequestArgumentCaptor.capture(), actualDeltaDays.capture(), actualDeltaMinutes.capture());
         MessageCampaignRequest actualMessageCampaignRequest = messageCampaignRequestArgumentCaptor.getValue();
 
         assertEquals(subscription.getSubscriptionId(), actualMessageCampaignRequest.getExternalId());
         assertEquals(MessageCampaignPack.BARI_KILKARI.getCampaignName(), actualMessageCampaignRequest.getCampaignName());
         assertEquals(deltaDays, actualDeltaDays.getValue().intValue());
         assertEquals(deltaMinutes, actualDeltaMinutes.getValue().intValue());
     }
 
     @Test
     public void shouldActivateRenewedSubscriptionWhichHadBeenSuspended() {
         DateTime renewalDate = DateTime.now();
         int graceCount = 2;
 
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn("123").withPack(SubscriptionPack.NAVJAAT_KILKARI)
                 .withScheduleStartDate(DateTime.now().minusWeeks(3)).build();
         final String subscriptionId = subscription.getSubscriptionId();
 
         subscription.setStatus(SubscriptionStatus.SUSPENDED);
         subscription.setOperator(Operator.AIRTEL);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.renewSubscription(subscriptionId, renewalDate, graceCount);
 
         verify(allSubscriptions).update(subscription);
         ArgumentCaptor<SubscriptionStateChangeRequest> subscriptionStateChangeReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         verify(reportingServiceImpl).reportSubscriptionStateChange(subscriptionStateChangeReportRequestArgumentCaptor.capture());
         SubscriptionStateChangeRequest subscriptionStateChangeReportRequest = subscriptionStateChangeReportRequestArgumentCaptor.getValue();
 
         assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
         assertEquals(subscriptionId, subscriptionStateChangeReportRequest.getSubscriptionId());
         assertEquals(SubscriptionStatus.ACTIVE.name(), subscriptionStateChangeReportRequest.getSubscriptionStatus());
         assertEquals(renewalDate, subscriptionStateChangeReportRequest.getCreatedAt());
         assertEquals((Integer) graceCount, subscriptionStateChangeReportRequest.getGraceCount());
         assertEquals(20, (int) subscriptionStateChangeReportRequest.getWeekNumber());
     }
 
     @Test
     public void shouldSuspendRenewedSubscriptionWhichWasActive() {
 
         final DateTime renewalDate = DateTime.now();
         final String reason = "Balance Low";
         final int graceCount = 0;
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn("123").withPack(SubscriptionPack.NAVJAAT_KILKARI)
                 .withScheduleStartDate(DateTime.now().minusWeeks(3)).build();
 
         final String subscriptionId = subscription.getSubscriptionId();
         subscription.setStatus(SubscriptionStatus.ACTIVE);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.suspendSubscription(subscriptionId, renewalDate, reason, graceCount);
 
         verify(allSubscriptions).update(subscription);
         ArgumentCaptor<SubscriptionStateChangeRequest> subscriptionStateChangeReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         verify(reportingServiceImpl).reportSubscriptionStateChange(subscriptionStateChangeReportRequestArgumentCaptor.capture());
         SubscriptionStateChangeRequest subscriptionStateChangeReportRequest = subscriptionStateChangeReportRequestArgumentCaptor.getValue();
 
         assertEquals(SubscriptionStatus.SUSPENDED, subscription.getStatus());
         assertEquals(subscriptionId, subscriptionStateChangeReportRequest.getSubscriptionId());
         assertEquals(SubscriptionStatus.SUSPENDED.name(), subscriptionStateChangeReportRequest.getSubscriptionStatus());
         assertEquals(renewalDate, subscriptionStateChangeReportRequest.getCreatedAt());
         assertEquals((Integer) graceCount, subscriptionStateChangeReportRequest.getGraceCount());
         assertEquals(20, (int) subscriptionStateChangeReportRequest.getWeekNumber());
     }
 
     @Test
     public void shouldDeactivateSubscriptionWithAppropriateReason() {
         DateTime date = DateTime.now();
         String reason = "balance is low";
         Integer graceCount = 7;
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn("1234567890").withPack(SubscriptionPack.BARI_KILKARI)
                 .withScheduleStartDate(date.minusWeeks(4)).build();
         String subscriptionId = subscription.getSubscriptionId();
         subscription.setStatus(SubscriptionStatus.SUSPENDED);
 
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.deactivateSubscription(subscriptionId, date, reason, graceCount);
 
         verify(allSubscriptions).update(subscription);
         ArgumentCaptor<SubscriptionStateChangeRequest> subscriptionStateChangeReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         verify(reportingServiceImpl).reportSubscriptionStateChange(subscriptionStateChangeReportRequestArgumentCaptor.capture());
         SubscriptionStateChangeRequest subscriptionStateChangeReportRequest = subscriptionStateChangeReportRequestArgumentCaptor.getValue();
 
         assertEquals(SubscriptionStatus.DEACTIVATED, subscription.getStatus());
         assertEquals(subscriptionId, subscriptionStateChangeReportRequest.getSubscriptionId());
         assertEquals(SubscriptionStatus.DEACTIVATED.name(), subscriptionStateChangeReportRequest.getSubscriptionStatus());
         assertEquals(date, subscriptionStateChangeReportRequest.getCreatedAt());
         assertEquals(5, (int) subscriptionStateChangeReportRequest.getWeekNumber());
         assertEquals(graceCount, subscriptionStateChangeReportRequest.getGraceCount());
     }
 
     @Test
     public void shouldReturnASubscriptionGivenAnId() {
         String subscriptionID = "subscriptionID";
 
         subscriptionService.findBySubscriptionId(subscriptionID);
 
         verify(allSubscriptions).findBySubscriptionId(subscriptionID);
     }
 
     @Test
     public void shouldRequestDeactivation() {
         String subscriptionId = "subscriptionId";
         String msisdn = "1234567890";
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn(msisdn)
                 .withScheduleStartDate(DateTime.now()).build();
         subscription.setStatus(SubscriptionStatus.NEW);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         Channel channel = Channel.IVR;
 
         subscriptionService.requestDeactivation(new DeactivationRequest(subscriptionId, channel, DateTime.now(), null));
 
         ArgumentCaptor<Subscription> captor = ArgumentCaptor.forClass(Subscription.class);
         ArgumentCaptor<OMSubscriptionRequest> captor1 = ArgumentCaptor.forClass(OMSubscriptionRequest.class);
         verify(allSubscriptions).update(captor.capture());
         Subscription actualSubscription = captor.getValue();
         Assert.assertEquals(msisdn, actualSubscription.getMsisdn());
         Assert.assertEquals(SubscriptionStatus.DEACTIVATION_REQUEST_RECEIVED, actualSubscription.getStatus());
 
         verify(onMobileSubscriptionManagerPublisher).processDeactivation(captor1.capture());
         OMSubscriptionRequest actualOMSubscriptionRequest = captor1.getValue();
         assertEquals(msisdn, actualOMSubscriptionRequest.getMsisdn());
         assertEquals(channel, actualOMSubscriptionRequest.getChannel());
         assertEquals(subscription.getSubscriptionId(), actualOMSubscriptionRequest.getSubscriptionId());
     }
 
     @Test
     public void shouldProcessSubscriptionCompletion() {
         final String msisdn = "9988776655";
         final SubscriptionPack pack = SubscriptionPack.NAVJAAT_KILKARI;
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn(msisdn).withStatus(SubscriptionStatus.ACTIVE).withStartDate(DateTime.now().minusWeeks(4)).build();
         final String subscriptionId = subscription.getSubscriptionId();
         final OMSubscriptionRequest omSubscriptionRequest = new OMSubscriptionRequest(msisdn, pack, null, subscriptionId);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.subscriptionComplete(omSubscriptionRequest);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).update(subscriptionArgumentCaptor.capture());
         Subscription actualSubscription = subscriptionArgumentCaptor.getValue();
         Assert.assertEquals(msisdn, actualSubscription.getMsisdn());
         Assert.assertEquals(SubscriptionStatus.PENDING_COMPLETION, actualSubscription.getStatus());
 
         ArgumentCaptor<SubscriptionStateChangeRequest> subscriptionStateChangeReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         verify(reportingServiceImpl).reportSubscriptionStateChange(subscriptionStateChangeReportRequestArgumentCaptor.capture());
         SubscriptionStateChangeRequest subscriptionStateChangeReportRequest = subscriptionStateChangeReportRequestArgumentCaptor.getValue();
         assertEquals(subscriptionId, subscriptionStateChangeReportRequest.getSubscriptionId());
         assertEquals(SubscriptionStatus.PENDING_COMPLETION.name(), subscriptionStateChangeReportRequest.getSubscriptionStatus());
         assertEquals("Subscription completed", subscriptionStateChangeReportRequest.getReason());
         assertNotNull(subscriptionStateChangeReportRequest.getWeekNumber());
 
         ArgumentCaptor<OMSubscriptionRequest> processSubscriptionRequestArgumentCaptor = ArgumentCaptor.forClass(OMSubscriptionRequest.class);
         verify(onMobileSubscriptionGateway).deactivateSubscription(processSubscriptionRequestArgumentCaptor.capture());
         OMSubscriptionRequest OMSubscriptionRequest = processSubscriptionRequestArgumentCaptor.getValue();
 
         assertEquals(msisdn, OMSubscriptionRequest.getMsisdn());
         assertEquals(null, OMSubscriptionRequest.getChannel());
         assertEquals(pack, OMSubscriptionRequest.getPack());
         assertEquals(subscriptionId, OMSubscriptionRequest.getSubscriptionId());
 
         verify(campaignMessageAlertService).deleteFor(subscriptionId);
     }
 
     @Test
     public void shouldScheduleInboxDeletionUponSubscriptionCompletion() {
         String msisdn = "1234567890";
         SubscriptionPack pack = SubscriptionPack.NANHI_KILKARI;
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn(msisdn).withPack(pack)
                 .withScheduleStartDate(DateTime.now()).build();
         subscription.setStatus(SubscriptionStatus.ACTIVE);
         String subscriptionId = subscription.getSubscriptionId();
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.subscriptionComplete(new OMSubscriptionRequest(msisdn, pack, Channel.IVR, subscriptionId));
 
         verify(inboxService).scheduleInboxDeletion(subscription.getSubscriptionId(), subscription.getCurrentWeeksMessageExpiryDate());
     }
 
     @Test
     public void shouldNotSendDeactivationRequestAgainIfTheExistingSubscriptionIsAlreadyInDeactivatedState() {
         final String msisdn = "9988776655";
         final Subscription subscription = new SubscriptionBuilder().withDefaults().withStatus(SubscriptionStatus.DEACTIVATED).build();
         final String subscriptionId = subscription.getSubscriptionId();
         final SubscriptionPack pack = SubscriptionPack.NAVJAAT_KILKARI;
         OMSubscriptionRequest value = new OMSubscriptionRequest(msisdn, pack, null, subscriptionId);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.subscriptionComplete(value);
 
         verify(onMobileSubscriptionGateway, never()).deactivateSubscription(any(OMSubscriptionRequest.class));
         verify(onMobileSubscriptionGateway, never()).deactivateSubscription(any(OMSubscriptionRequest.class));
         verify(inboxService, never()).scheduleInboxDeletion(any(String.class), any(DateTime.class));
         verify(campaignMessageAlertService, never()).deleteFor(any(String.class));
     }
 
     @Test
     public void shouldScheduleInboxDeletionUponSubscriptionDeactivation() {
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn("1234567890")
                 .withScheduleStartDate(DateTime.now()).build();
         subscription.setStatus(SubscriptionStatus.ACTIVE);
         String subscriptionId = subscription.getSubscriptionId();
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.deactivateSubscription(subscriptionId, DateTime.now(), null, null);
 
         verify(inboxService).scheduleInboxDeletion(subscriptionId, subscription.getCurrentWeeksMessageExpiryDate());
     }
 
     @Test
     public void shouldUnScheduleCurrentCampaignAndScheduleNewCampaignForMCOrID() {
         String subscriptionId = "subscriptionId";
         String msisdn = "1234567890";
         SubscriptionPack subscriptionPack = SubscriptionPack.BARI_KILKARI;
         int deltaMinutes = 30;
         DateTime friday = new DateTime(2011, 11, 25, 12, 30, 30);
         DateTime existingCampaignStartDate = friday;
         CampaignChangeReason campaignChangeReason = CampaignChangeReason.MISCARRIAGE;
         DateTime saturday = existingCampaignStartDate.plusMonths(3);
         DateTime rescheduleRequestedDate = saturday;
         DateTime nextAlertDateTime = rescheduleRequestedDate.plusHours(1);
         String existingCampaignName = MessageCampaignPack.BARI_KILKARI.getCampaignName();
 
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn(msisdn).withPack(subscriptionPack).withScheduleStartDate(existingCampaignStartDate).build();
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         when(messageCampaignService.getActiveCampaignName(subscription.getSubscriptionId())).thenReturn(existingCampaignName);
         when(messageCampaignService.getMessageTimings(subscriptionId, rescheduleRequestedDate, rescheduleRequestedDate.plusMonths(1))).thenReturn(Arrays.asList(nextAlertDateTime, new DateTime()));
         when(kilkariPropertiesData.getCampaignScheduleDeltaMinutes()).thenReturn(deltaMinutes);
 
         subscriptionService.rescheduleCampaign(new CampaignRescheduleRequest(subscriptionId, campaignChangeReason, rescheduleRequestedDate));
 
         InOrder order = inOrder(messageCampaignService, campaignMessageService, campaignMessageAlertService);
         order.verify(messageCampaignService).getMessageTimings(subscriptionId, rescheduleRequestedDate, rescheduleRequestedDate.plusMonths(1));
         ArgumentCaptor<MessageCampaignRequest> campaignUnEnrollmentRequestArgumentCaptor = ArgumentCaptor.forClass(MessageCampaignRequest.class);
         order.verify(messageCampaignService).getActiveCampaignName(subscription.getSubscriptionId());
         order.verify(messageCampaignService).stop(campaignUnEnrollmentRequestArgumentCaptor.capture());
         MessageCampaignRequest campaignRequest = campaignUnEnrollmentRequestArgumentCaptor.getValue();
         assertEquals(subscription.getSubscriptionId(), campaignRequest.getExternalId());
         assertEquals(existingCampaignName, campaignRequest.getCampaignName());
         assertEquals(subscription.getScheduleStartDate(), campaignRequest.getScheduleStartDate());
 
         order.verify(campaignMessageService).deleteCampaignMessagesFor(subscriptionId);
         order.verify(campaignMessageAlertService).clearMessageId(subscriptionId);
 
         ArgumentCaptor<MessageCampaignRequest> campaignEnrollmentRequestArgumentCaptor = ArgumentCaptor.forClass(MessageCampaignRequest.class);
         ArgumentCaptor<Integer> deltaDaysCaptor = ArgumentCaptor.forClass(Integer.class);
         ArgumentCaptor<Integer> deltaMinutesCaptor = ArgumentCaptor.forClass(Integer.class);
         order.verify(messageCampaignService).start(campaignEnrollmentRequestArgumentCaptor.capture(), deltaDaysCaptor.capture(), deltaMinutesCaptor.capture());
         MessageCampaignRequest campaignEnrollmentRequest = campaignEnrollmentRequestArgumentCaptor.getValue();
         assertEquals(subscriptionId, campaignEnrollmentRequest.getExternalId());
         assertEquals(MessageCampaignPack.MISCARRIAGE.getCampaignName(), campaignEnrollmentRequest.getCampaignName());
         assertEquals(nextAlertDateTime, campaignEnrollmentRequest.getScheduleStartDate());
         assertEquals(0, deltaDaysCaptor.getValue().intValue());
         assertEquals(deltaMinutes, deltaMinutesCaptor.getValue().intValue());
     }
 
     @Test
     public void shouldThrowExceptionWhenUnScheduleCurrentCampaignIsInvokedForANonActivePack() {
         String subscriptionId = "subscriptionId";
         String message = "some error";
         CampaignChangeReason campaignChangeReason = CampaignChangeReason.MISCARRIAGE;
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn("1234567890").withPack(SubscriptionPack.BARI_KILKARI).withCreationDate(DateTime.now()).withStatus(SubscriptionStatus.PENDING_COMPLETION).build();
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         doThrow(new ValidationException(message)).when(subscriptionValidator).validateChangeCampaign(subscriptionId, CampaignChangeReason.MISCARRIAGE);
         expectedException.expect(ValidationException.class);
         expectedException.expectMessage(message);
 
         subscriptionService.rescheduleCampaign(new CampaignRescheduleRequest(subscriptionId, campaignChangeReason, DateTime.now()));
 
         verify(messageCampaignService, never()).stop(any(MessageCampaignRequest.class));
         verify(campaignMessageService, never()).deleteCampaignMessagesFor(any(String.class));
         verify(messageCampaignService, never()).start(any(MessageCampaignRequest.class), 0, 0);
     }
 
     @Test
     public void shouldThrowExceptionIfValidationFailsForUpdatingSubscriberDetails() {
         SubscriberRequest request = mock(SubscriberRequest.class);
         String message = "some error";
         doThrow(new ValidationException(message)).when(subscriptionValidator).validateSubscriberDetails(request);
         expectedException.expect(ValidationException.class);
         expectedException.expectMessage(message);
 
         subscriptionService.updateSubscriberDetails(request);
 
         verify(reportingServiceImpl, never()).getLocation(anyString(), anyString(), anyString());
         verify(reportingServiceImpl, never()).reportSubscriberDetailsChange(request.getSubscriptionId(), any(SubscriberReportRequest.class));
     }
 
     @Test
     public void shouldPublishASubscriberUpdateEventWithLocation() {
         String subscriptionId = "subscriptionId";
         String district = "district";
         String block = "block";
         String panchayat = "panchayat";
         when(reportingServiceImpl.getLocation(district, block, panchayat)).thenReturn(null);
 
         subscriptionService.updateSubscriberDetails(new SubscriberRequest(subscriptionId, Channel.CONTACT_CENTER.name(), DateTime.now(), "name", 23,
                 new Location(district, block, panchayat)));
 
         verify(reportingServiceImpl).getLocation(district, block, panchayat);
 
         ArgumentCaptor<SubscriberReportRequest> requestCaptor = ArgumentCaptor.forClass(SubscriberReportRequest.class);
         ArgumentCaptor<String> subscriptionIdCaptor = ArgumentCaptor.forClass(String.class);
         verify(reportingServiceImpl).reportSubscriberDetailsChange(subscriptionIdCaptor.capture(), requestCaptor.capture());
         SubscriberReportRequest reportRequest = requestCaptor.getValue();
         String actualSubscriptionId = subscriptionIdCaptor.getValue();
 
         assertEquals(subscriptionId, actualSubscriptionId);
         assertEquals(23, (int) reportRequest.getBeneficiaryAge());
         assertEquals("name", reportRequest.getBeneficiaryName());
         assertEquals(district, reportRequest.getLocation().getDistrict());
         assertEquals(block, reportRequest.getLocation().getBlock());
         assertEquals(panchayat, reportRequest.getLocation().getPanchayat());
     }
 
     @Test
     public void shouldPublishASubscriberUpdateEventWithoutALocation() {
         String subscriptionId = "subscriptionId";
         when(reportingServiceImpl.getLocation(anyString(), anyString(), anyString())).thenReturn(null);
 
         subscriptionService.updateSubscriberDetails(new SubscriberRequest(subscriptionId, Channel.CONTACT_CENTER.name(), DateTime.now(), "name", 23,
                 Location.NULL));
 
         ArgumentCaptor<SubscriberReportRequest> requestCaptor = ArgumentCaptor.forClass(SubscriberReportRequest.class);
         ArgumentCaptor<String> subscriptionIdCaptor = ArgumentCaptor.forClass(String.class);
         verify(reportingServiceImpl).reportSubscriberDetailsChange(subscriptionIdCaptor.capture(), requestCaptor.capture());
         SubscriberReportRequest reportRequest = requestCaptor.getValue();
         String actualSubscriptionId = subscriptionIdCaptor.getValue();
         assertEquals(subscriptionId, actualSubscriptionId);
         assertEquals(23, (int) reportRequest.getBeneficiaryAge());
         assertEquals("name", reportRequest.getBeneficiaryName());
         assertNull(reportRequest.getLocation());
     }
 
     @Test
     public void shouldNotProcessDeactivationRequestWhenSubscriptionIsNotInProgress() {
         String subscriptionId = "subsId";
         Subscription mockedSubscription = mock(Subscription.class);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(mockedSubscription);
 
         subscriptionService.requestDeactivation(new DeactivationRequest(subscriptionId, Channel.IVR, DateTime.now(), null));
 
         verify(mockedSubscription, never()).deactivationRequestReceived();
         verify(onMobileSubscriptionManagerPublisher, never()).processDeactivation(Matchers.<OMSubscriptionRequest>any());
         verify(reportingServiceImpl, never()).reportSubscriptionStateChange(Matchers.<SubscriptionStateChangeRequest>any());
         verify(allSubscriptions, never()).update(mockedSubscription);
     }
 
     @Test
     public void shouldBackDateStartDateForALateSubscriptionWhenEDDIsGiven() {
         DateTime edd = DateTime.now().plusWeeks(1);
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withPack(SubscriptionPack.BARI_KILKARI).withExpectedDateOfDelivery(edd).build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription subscriptionArgumentCaptorValue = subscriptionArgumentCaptor.getValue();
         assertEquals(16, Weeks.weeksBetween(subscriptionArgumentCaptorValue.getStartDate(), edd).getWeeks());
     }
 
     @Test
     public void shouldBackDateStartDateForALateSubscriptionWhenDOBIsGivenForSixteenMonthsPack() {
         DateTime dob = DateTime.now().minusMonths(1);
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withPack(SubscriptionPack.BARI_KILKARI).withDateOfBirth(dob).build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription subscriptionArgumentCaptorValue = subscriptionArgumentCaptor.getValue();
         assertEquals(16, Weeks.weeksBetween(subscriptionArgumentCaptorValue.getStartDate(), dob).getWeeks());
     }
 
     @Test
     public void shouldBackDateStartDateForALateSubscriptionWhenDOBIsGivenForTwelveMonthsPack() {
         DateTime dob = DateTime.now().minusMonths(1);
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withPack(SubscriptionPack.NAVJAAT_KILKARI).withDateOfBirth(dob).build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription subscriptionArgumentCaptorValue = subscriptionArgumentCaptor.getValue();
         Assert.assertEquals(dob.withSecondOfMinute(0).withMillisOfSecond(0), subscriptionArgumentCaptorValue.getStartDate());
     }
 
     @Test
     public void shouldBackDateStartDateForALateSubscriptionWhenDOBIsGivenForSevenMonthsPack() {
         DateTime dob = DateTime.now().minusMonths(1);
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withPack(SubscriptionPack.NANHI_KILKARI).withDateOfBirth(dob).build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription subscriptionArgumentCaptorValue = subscriptionArgumentCaptor.getValue();
         assertTrue(Weeks.weeksBetween(dob.withSecondOfMinute(0).withMillisOfSecond(0), subscriptionArgumentCaptorValue.getStartDate()).getWeeks() >= 20);
     }
 
     @Test
     public void shouldBackDateStartDateForALateSubscriptionWhenWeekNumberIsGivenForSevenMonthsPack() {
         Integer weekNumber = 28;
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withPack(SubscriptionPack.NANHI_KILKARI).withWeek(weekNumber).build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription subscriptionArgumentCaptorValue = subscriptionArgumentCaptor.getValue();
         assertEquals(subscriptionRequest.getCreationDate().minusWeeks(27).withSecondOfMinute(0).withMillisOfSecond(0), subscriptionArgumentCaptorValue.getStartDate());
     }
 
     @Test
     public void shouldSetDateForAnEarlySubscriptionWhenEDDIsGivenForSixteenMonths() {
         DateTime now = DateTime.now();
         DateTime edd = now.plusMonths(4);
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withPack(SubscriptionPack.BARI_KILKARI).withExpectedDateOfDelivery(edd).build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription subscription = subscriptionArgumentCaptor.getValue();
 
         assertEquals(16, Weeks.weeksBetween(subscription.getStartDate(), edd).getWeeks());
         assertEquals(SubscriptionStatus.NEW_EARLY, subscription.getStatus());
     }
 
     @Test
     public void shouldSetDateForAnEarlySubscriptionWhenEDDIsGivenForTwelveMonths() {
         DateTime now = DateTime.now();
         DateTime edd = now.plusMonths(4);
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withPack(SubscriptionPack.NAVJAAT_KILKARI).withExpectedDateOfDelivery(edd).build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription subscription = subscriptionArgumentCaptor.getValue();
         assertEquals(edd.withSecondOfMinute(0).withMillisOfSecond(0), subscription.getStartDate());
         assertEquals(SubscriptionStatus.NEW_EARLY, subscription.getStatus());
     }
 
     @Test
     public void shouldSetDateForAnEarlySubscriptionWhenEDDIsGivenForSevenMonths() {
         DateTime now = DateTime.now();
         DateTime edd = now.plusMonths(4);
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withPack(SubscriptionPack.NANHI_KILKARI).withExpectedDateOfDelivery(edd).build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription subscriptionArgumentCaptorValue = subscriptionArgumentCaptor.getValue();
         assertTrue(Weeks.weeksBetween(edd.withSecondOfMinute(0).withMillisOfSecond(0), subscriptionArgumentCaptorValue.getStartDate()).getWeeks() >= 20);
     }
 
     @Test
     public void shouldSetDateForAnEarlySubscriptionWhenDOBIsGivenForTwelveMonths() {
         DateTime dob = DateTime.now().plusMonths(3);
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withPack(SubscriptionPack.NAVJAAT_KILKARI).withDateOfBirth(dob).build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription subscriptionArgumentCaptorValue = subscriptionArgumentCaptor.getValue();
         assertEquals(dob.withSecondOfMinute(0).withMillisOfSecond(0), subscriptionArgumentCaptorValue.getStartDate());
     }
 
     @Test
     public void shouldSetDateForAnEarlySubscriptionWhenDOBIsGivenForSevenMonths() {
         DateTime dob = DateTime.now().plusMonths(3);
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withPack(SubscriptionPack.NANHI_KILKARI).withDateOfBirth(dob).build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription subscriptionArgumentCaptorValue = subscriptionArgumentCaptor.getValue();
         assertTrue(Weeks.weeksBetween(dob.withSecondOfMinute(0).withMillisOfSecond(0), subscriptionArgumentCaptorValue.getStartDate()).getWeeks() >= 20);
     }
 
     @Test
     public void shouldUnScheduleMessageCampaignAndDeleteCampaignMessageAlertOnSuccessfulDeactivationRequest() {
         DateTime createdAt = DateTime.now();
         DateTime scheduleStartDate = createdAt.plusDays(2);
         Subscription subscription = new SubscriptionBuilder()
                 .withDefaults()
                 .withMsisdn("1234567890")
                 .withPack(SubscriptionPack.NAVJAAT_KILKARI)
                 .withCreationDate(createdAt)
                 .withStartDate(createdAt)
                 .withScheduleStartDate(scheduleStartDate).build();
         subscription.setStatus(SubscriptionStatus.ACTIVE);
 
         String subscriptionId = subscription.getSubscriptionId();
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         when(messageCampaignService.getActiveCampaignName(subscriptionId)).thenReturn(MessageCampaignPack.NAVJAAT_KILKARI.getCampaignName());
 
         subscriptionService.deactivateSubscription(subscriptionId, createdAt.plusWeeks(1), "Balance Low", null);
 
         ArgumentCaptor<MessageCampaignRequest> campaignRequestArgumentCaptor = ArgumentCaptor.forClass(MessageCampaignRequest.class);
         verify(messageCampaignService).stop(campaignRequestArgumentCaptor.capture());
         MessageCampaignRequest messageCampaignRequest = campaignRequestArgumentCaptor.getValue();
         assertEquals(subscriptionId, messageCampaignRequest.getExternalId());
         assertEquals(MessageCampaignPack.NAVJAAT_KILKARI.getCampaignName(), messageCampaignRequest.getCampaignName());
         assertEquals(scheduleStartDate.withSecondOfMinute(0).withMillisOfSecond(0), messageCampaignRequest.getScheduleStartDate());
         verify(campaignMessageAlertService).deleteFor(subscriptionId);
     }
 
     @Test
     public void shouldScheduleEarlySubscription() {
         DateTime dob = DateTime.now().plusMonths(3);
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withPack(SubscriptionPack.NAVJAAT_KILKARI).withDateOfBirth(dob).build();
 
         subscriptionService.createSubscription(subscriptionRequest, Channel.CONTACT_CENTER);
 
         ArgumentCaptor<RunOnceSchedulableJob> runOnceSchedulableJobArgumentCaptor = ArgumentCaptor.forClass(RunOnceSchedulableJob.class);
         verify(motechSchedulerService).safeScheduleRunOnceJob(runOnceSchedulableJobArgumentCaptor.capture());
         ArgumentCaptor<SubscriptionReportRequest> subscriptionReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionReportRequest.class);
         verify(reportingServiceImpl).reportSubscriptionCreation(subscriptionReportRequestArgumentCaptor.capture());
 
         RunOnceSchedulableJob runOnceSchedulableJobArgumentCaptorValue = runOnceSchedulableJobArgumentCaptor.getValue();
         assertEquals(dob.toDate(), runOnceSchedulableJobArgumentCaptorValue.getStartDate());
         assertEquals(SubscriptionEventKeys.EARLY_SUBSCRIPTION, runOnceSchedulableJobArgumentCaptorValue.getMotechEvent().getSubject());
         assertEquals(OMSubscriptionRequest.class, runOnceSchedulableJobArgumentCaptorValue.getMotechEvent().getParameters().get("0").getClass());
 
         SubscriptionReportRequest subscriptionCreationReportRequest = subscriptionReportRequestArgumentCaptor.getValue();
         assertEquals(subscriptionRequest.getMsisdn(), subscriptionCreationReportRequest.getMsisdn().toString());
         assertEquals(subscriptionRequest.getPack().toString(), subscriptionCreationReportRequest.getPack());
         assertEquals(subscriptionRequest.getSubscriber().getDateOfBirth(), subscriptionCreationReportRequest.getDateOfBirth());
         assertEquals(SubscriptionStatus.NEW_EARLY.toString(), subscriptionCreationReportRequest.getSubscriptionStatus());
     }
 
     @Test
     public void shouldChangeMsisdn() {
         String oldMsisdn = "9876543210";
         String newMsisdn = "9876543211";
         DateTime now = DateTime.now();
         ChangeMsisdnRequest changeMsisdnRequest = new ChangeMsisdnRequest(oldMsisdn, newMsisdn, Channel.CONTACT_CENTER, null);
         changeMsisdnRequest.setPacks(Arrays.asList(SubscriptionPack.NANHI_KILKARI));
 
         Subscription subscription1 = new SubscriptionBuilder().withDefaults().withMsisdn(oldMsisdn).withPack(SubscriptionPack.NANHI_KILKARI)
                 .withCreationDate(now.minusWeeks(2).minusHours(1)).withStartDate(now).withScheduleStartDate(now).build();
         subscription1.setStatus(SubscriptionStatus.ACTIVE);
         String subscriptionId = subscription1.getSubscriptionId();
 
         Subscription subscription2 = new SubscriptionBuilder().withDefaults().withMsisdn(oldMsisdn).withPack(SubscriptionPack.NAVJAAT_KILKARI)
                 .withCreationDate(now).withStartDate(now).withScheduleStartDate(now).build();
         subscription2.setStatus(SubscriptionStatus.ACTIVE);
 
         String beneficiaryName = "name";
         Integer beneficiaryAge = 25;
         SubscriberResponse subscriberResponse = new SubscriberResponse("subscriptionId", beneficiaryName, beneficiaryAge, null, null, null, null);
 
         when(allSubscriptions.findUpdatableSubscriptions(oldMsisdn)).thenReturn(Arrays.asList(subscription1, subscription2));
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription1);
         when(reportingServiceImpl.getSubscriber(subscriptionId)).thenReturn(subscriberResponse);
 
         subscriptionService.changeMsisdn(changeMsisdnRequest);
 
         verify(changeMsisdnValidator).validate(changeMsisdnRequest);
 
         InOrder order = inOrder(reportingServiceImpl, onMobileSubscriptionManagerPublisher, allSubscriptions);
 
         order.verify(reportingServiceImpl).getSubscriber(subscriptionId);
 
         ArgumentCaptor<SubscriptionStateChangeRequest> subscriptionStateChangeRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         order.verify(reportingServiceImpl).reportSubscriptionStateChange(subscriptionStateChangeRequestArgumentCaptor.capture());
         SubscriptionStateChangeRequest subscriptionStateChangeRequest = subscriptionStateChangeRequestArgumentCaptor.getValue();
 
         assertEquals(subscriptionId, subscriptionStateChangeRequest.getSubscriptionId());
         assertEquals(SubscriptionStatus.DEACTIVATION_REQUEST_RECEIVED.toString(), subscriptionStateChangeRequest.getSubscriptionStatus());
 
         ArgumentCaptor<OMSubscriptionRequest> deactivationRequest = ArgumentCaptor.forClass(OMSubscriptionRequest.class);
         order.verify(onMobileSubscriptionManagerPublisher).processDeactivation(deactivationRequest.capture());
         OMSubscriptionRequest actualDeactivationRequest = deactivationRequest.getValue();
 
         assertEquals(oldMsisdn, actualDeactivationRequest.getMsisdn());
         assertEquals(SubscriptionPack.NANHI_KILKARI, actualDeactivationRequest.getPack());
         assertEquals(subscriptionId, actualDeactivationRequest.getSubscriptionId());
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         order.verify(allSubscriptions).add(subscriptionArgumentCaptor.capture());
         Subscription actualSubscription = subscriptionArgumentCaptor.getValue();
 
         assertEquals(newMsisdn, actualSubscription.getMsisdn());
         assertEquals(SubscriptionPack.NANHI_KILKARI, actualSubscription.getPack());
 
         ArgumentCaptor<SubscriptionReportRequest> subscriptionReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionReportRequest.class);
         order.verify(reportingServiceImpl).reportSubscriptionCreation(subscriptionReportRequestArgumentCaptor.capture());
         SubscriptionReportRequest subscriptionReportRequest = subscriptionReportRequestArgumentCaptor.getValue();
 
         assertEquals(beneficiaryName, subscriptionReportRequest.getName());
         assertEquals(beneficiaryAge, subscriptionReportRequest.getAgeOfBeneficiary());
         assertEquals(subscriptionId, subscriptionReportRequest.getOldSubscriptionId());
 
         ArgumentCaptor<OMSubscriptionRequest> activationRequest = ArgumentCaptor.forClass(OMSubscriptionRequest.class);
         order.verify(onMobileSubscriptionManagerPublisher).sendActivationRequest(activationRequest.capture());
         OMSubscriptionRequest actualActivationRequest = activationRequest.getValue();
 
         assertEquals(newMsisdn, actualActivationRequest.getMsisdn());
         assertEquals(SubscriptionPack.NANHI_KILKARI, actualActivationRequest.getPack());
     }
 
     @Test
     public void shouldChangeMsisdnForEarlySubscription() {
         String oldMsisdn = "9876543210";
         String newMsisdn = "9876543211";
         ChangeMsisdnRequest changeMsisdnRequest = new ChangeMsisdnRequest(oldMsisdn, newMsisdn, Channel.CONTACT_CENTER, null);
         changeMsisdnRequest.setPacks(Arrays.asList(SubscriptionPack.NANHI_KILKARI));
 
         Subscription subscription1 = new Subscription(oldMsisdn, SubscriptionPack.NANHI_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription1.setStatus(SubscriptionStatus.NEW_EARLY);
 
         Subscription subscription2 = new Subscription(oldMsisdn, SubscriptionPack.NAVJAAT_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription2.setStatus(SubscriptionStatus.ACTIVE);
 
 
         when(allSubscriptions.findUpdatableSubscriptions(oldMsisdn)).thenReturn(Arrays.asList(subscription1, subscription2));
         when(allSubscriptions.findBySubscriptionId(subscription1.getSubscriptionId())).thenReturn(subscription1);
 
         subscriptionService.changeMsisdn(changeMsisdnRequest);
 
         ArgumentCaptor<Subscription> subscriptionArgumentCaptor = ArgumentCaptor.forClass(Subscription.class);
         verify(allSubscriptions).update(subscriptionArgumentCaptor.capture());
         Subscription updatedSubscription = subscriptionArgumentCaptor.getValue();
 
         assertEquals(subscription1.getSubscriptionId(), updatedSubscription.getSubscriptionId());
         assertEquals(subscription1.getPack(), updatedSubscription.getPack());
         assertEquals(subscription1.getMsisdn(), updatedSubscription.getMsisdn());
 
         verifyZeroInteractions(onMobileSubscriptionManagerPublisher);
         ArgumentCaptor<SubscriberChangeMsisdnReportRequest> requestArgumentCaptor = ArgumentCaptor.forClass(SubscriberChangeMsisdnReportRequest.class);
         verify(reportingServiceImpl).reportChangeMsisdnForEarlySubscription(requestArgumentCaptor.capture());
         SubscriberChangeMsisdnReportRequest reportRequest = requestArgumentCaptor.getValue();
         assertEquals(newMsisdn, reportRequest.getMsisdn().toString());
     }
 
     @Test
     public void shouldScheduleDeactivation() {
         Integer graceCount = 2;
         final String subscriptionId = "subscriptionId";
         String reason = "Some reason";
         DateTime deactivationDate = DateTime.now();
         Subscription subscription = new Subscription("1234567890", SubscriptionPack.BARI_KILKARI, DateTime.now(), DateTime.now(), null) {
             public String getSubscriptionId() {
                 return subscriptionId;
             }
         };
         subscription.setStatus(SubscriptionStatus.PENDING_DEACTIVATION);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         int bufferForDeactivationInDays = 2;
         when(kilkariPropertiesData.getBufferDaysToAllowRenewalForDeactivation()).thenReturn(bufferForDeactivationInDays);
 
         subscriptionService.processDeactivation(subscriptionId, deactivationDate, reason, graceCount);
 
         ArgumentCaptor<RunOnceSchedulableJob> runOnceSchedulableJobArgumentCaptor = ArgumentCaptor.forClass(RunOnceSchedulableJob.class);
         verify(motechSchedulerService).safeScheduleRunOnceJob(runOnceSchedulableJobArgumentCaptor.capture());
         RunOnceSchedulableJob actualSchedulableJob = runOnceSchedulableJobArgumentCaptor.getValue();
         assertEquals(SubscriptionEventKeys.DEACTIVATE_SUBSCRIPTION, actualSchedulableJob.getMotechEvent().getSubject());
         ScheduleDeactivationRequest scheduleDeactivationRequest = (ScheduleDeactivationRequest) actualSchedulableJob.getMotechEvent().getParameters().get("0");
         verify(kilkariPropertiesData).getBufferDaysToAllowRenewalForDeactivation();
         assertEquals(new ScheduleDeactivationRequest(subscriptionId, deactivationDate, reason, graceCount), scheduleDeactivationRequest);
     }
 
     @Test
     public void processDeactivationForSubscriptionCompletionShouldNotScheduleDeactivation() {
         DateTime date = DateTime.now();
         String reason = "balance is low";
         Integer graceCount = 7;
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn("1234567890").withPack(SubscriptionPack.BARI_KILKARI)
                 .withScheduleStartDate(date.minusWeeks(12)).build();
         String subscriptionId = subscription.getSubscriptionId();
         subscription.setStatus(SubscriptionStatus.PENDING_COMPLETION);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.processDeactivation(subscriptionId, date, reason, graceCount);
 
         verify(allSubscriptions).update(subscription);
         ArgumentCaptor<SubscriptionStateChangeRequest> subscriptionStateChangeReportRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         verify(reportingServiceImpl).reportSubscriptionStateChange(subscriptionStateChangeReportRequestArgumentCaptor.capture());
         SubscriptionStateChangeRequest subscriptionStateChangeReportRequest = subscriptionStateChangeReportRequestArgumentCaptor.getValue();
         assertEquals(SubscriptionStatus.COMPLETED, subscription.getStatus());
         assertEquals(subscriptionId, subscriptionStateChangeReportRequest.getSubscriptionId());
         assertEquals(SubscriptionStatus.COMPLETED.name(), subscriptionStateChangeReportRequest.getSubscriptionStatus());
         assertEquals(date, subscriptionStateChangeReportRequest.getCreatedAt());
         assertEquals(graceCount, subscriptionStateChangeReportRequest.getGraceCount());
         assertEquals(13, (int) subscriptionStateChangeReportRequest.getWeekNumber());
     }
 
     @Test
     public void shouldNotUpdateInboxDuringRenewalWhenMessageHasNotAlreadyBeenScheduled() {
         String msisdn = "1234567890";
         Subscription subscription = new Subscription(msisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription.setStatus(SubscriptionStatus.NEW);
         String subscriptionId = subscription.getSubscriptionId();
         Operator operator = Operator.AIRTEL;
         subscription.setOperator(operator);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.renewSubscription(subscriptionId, DateTime.now(), null);
 
         verify(inboxService, never()).newMessage(anyString(), anyString());
     }
 
     @Test
     public void shouldNotUpdateInboxDuringRenewalnWhenMessageHasAlreadyBeenScheduled() {
         String msisdn = "1234567890";
         Subscription subscription = new Subscription(msisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), DateTime.now(), null);
         subscription.setStatus(SubscriptionStatus.NEW);
         String subscriptionId = subscription.getSubscriptionId();
         Operator operator = Operator.AIRTEL;
         subscription.setOperator(operator);
         String messageId = "mesasgeId";
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         when(campaignMessageAlertService.scheduleCampaignMessageAlertForActivation(subscriptionId, msisdn, operator.name())).thenReturn(messageId);
 
         subscriptionService.renewSubscription(subscriptionId, DateTime.now(), null);
 
         verify(inboxService, never()).newMessage(anyString(), anyString());
     }
 
     @Test
     public void shouldCallCampaignMessageAlertServiceOnRenewal() {
         String msisdn = "1234567890";
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn(msisdn)
                 .withScheduleStartDate(DateTime.now()).build();
         subscription.setStatus(SubscriptionStatus.ACTIVE);
         String subscriptionId = subscription.getSubscriptionId();
         Operator operator = Operator.AIRTEL;
         subscription.setOperator(operator);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.renewSubscription(subscriptionId, DateTime.now(), null);
 
         verify(campaignMessageAlertService).scheduleCampaignMessageAlertForRenewal(subscriptionId, msisdn, operator.name());
     }
 
     @Test
     public void shouldUnsubscribeASubscription() {
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn("9988776655").withScheduleStartDate(DateTime.now()).build();
         String subscriptionId = subscription.getSubscriptionId();
         subscription.setStatus(SubscriptionStatus.ACTIVE);
         DeactivationRequest deactivationRequest = new DeactivationRequest(subscriptionId, Channel.CONTACT_CENTER, DateTime.now(), "Reason");
 
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.requestUnsubscription(deactivationRequest);
 
         verify(unsubscriptionValidator).validate(subscriptionId);
 
         verify(allSubscriptions).update(Matchers.<Subscription>any());
         verify(onMobileSubscriptionManagerPublisher).processDeactivation(Matchers.<OMSubscriptionRequest>any());
         verify(reportingServiceImpl).reportSubscriptionStateChange(Matchers.<SubscriptionStateChangeRequest>any());
     }
 
     @Test
     public void shouldSyncLocationIfItDoesNotExistWhenCreatingSubscription() {
         String district = "d";
         String block = "b";
         String panchayat = "p";
         SubscriptionRequest request = new SubscriptionRequestBuilder().withDefaults().withLocation(district, block, panchayat).build();
         when(reportingServiceImpl.getLocation(district, block, panchayat)).thenReturn(null);
 
         subscriptionService.createSubscription(request, Channel.CONTACT_CENTER);
 
         InOrder order = inOrder(reportingServiceImpl, onMobileSubscriptionManagerPublisher, refdataSyncService);
         order.verify(reportingServiceImpl).getLocation(district, block, panchayat);
         order.verify(reportingServiceImpl).reportSubscriptionCreation(any(SubscriptionReportRequest.class));
         order.verify(onMobileSubscriptionManagerPublisher).sendActivationRequest(any(OMSubscriptionRequest.class));
         order.verify(refdataSyncService).syncNewLocation(district, block, panchayat);
     }
 
     @Test
     public void shouldNotSyncLocationIfNotProvidedWhileCreatingSubscription() {
         SubscriptionRequest request = new SubscriptionRequestBuilder().withDefaults().withLocation(Location.NULL).build();
 
         subscriptionService.createSubscription(request, Channel.CONTACT_CENTER);
 
         verify(refdataSyncService, never()).syncNewLocation(anyString(), anyString(), anyString());
     }
 
     @Test
     public void shouldNotSyncWhenLocationAlreadyExistsWhenCreatingSubscription() {
         String district = "d";
         String block = "b";
         String panchayat = "p";
         SubscriptionRequest request = new SubscriptionRequestBuilder().withDefaults().withLocation(district, block, panchayat).build();
         when(reportingServiceImpl.getLocation(district, block, panchayat)).thenReturn(new LocationResponse());
 
         subscriptionService.createSubscription(request, Channel.CONTACT_CENTER);
 
         verify(refdataSyncService, never()).syncNewLocation(anyString(), anyString(), anyString());
     }
 
     @Test
     public void shouldNotFetchLocationIfTheRequestDoesNotHaveLocation_WhenCreatingSubscription() {
         SubscriptionRequest request = new SubscriptionRequestBuilder().withDefaults()
                 .withLocation(Location.NULL).build();
 
         subscriptionService.createSubscription(request, Channel.CONTACT_CENTER);
 
         verify(reportingServiceImpl, never()).getLocation(anyString(), anyString(), anyString());
         verify(refdataSyncService, never()).syncNewLocation(anyString(), anyString(), anyString());
     }
 
     @Test
     public void shouldSyncLocationIfItDoesNotExistWhenUpdatingSubscriberDetails() {
         String district = "d";
         String block = "b";
         String panchayat = "p";
         when(reportingServiceImpl.getLocation(district, block, panchayat)).thenReturn(null);
 
         subscriptionService.updateSubscriberDetails(new SubscriberRequest(null, null, null, null, null, new Location(district, block, panchayat)));
 
         InOrder order = inOrder(reportingServiceImpl, refdataSyncService);
         order.verify(reportingServiceImpl).getLocation(district, block, panchayat);
         order.verify(reportingServiceImpl).reportSubscriberDetailsChange(anyString(), any(SubscriberReportRequest.class));
         order.verify(refdataSyncService).syncNewLocation(district, block, panchayat);
     }
 
     @Test
     public void shouldNotSyncLocationIfNotProvidedWhileUpdatingSubscriberDetails() {
         subscriptionService.updateSubscriberDetails(new SubscriberRequest(null, null, null, null, null, null));
 
         verify(refdataSyncService, never()).syncNewLocation(anyString(), anyString(), anyString());
     }
 
     @Test
     public void shouldNotSyncIfLocationAlreadyExistsWhenUpdatingSubscriberDetails() {
         String district = "d";
         String block = "b";
         String panchayat = "p";
         when(reportingServiceImpl.getLocation(district, block, panchayat)).thenReturn(new LocationResponse());
 
         subscriptionService.updateSubscriberDetails(new SubscriberRequest(null, null, null, null, null, new Location(district, block, panchayat)));
 
         verify(refdataSyncService, never()).syncNewLocation(anyString(), anyString(), anyString());
     }
 
     @Test
     public void shouldNotFetchLocationIfTheRequestDoesNotHaveLocation_WhenUpdatingSubscriberDetails() {
         subscriptionService.updateSubscriberDetails(new SubscriberRequest(null, null, null, null, null, Location.NULL));
 
         verify(reportingServiceImpl, never()).getLocation(anyString(), anyString(), anyString());
         verify(refdataSyncService, never()).syncNewLocation(anyString(), anyString(), anyString());
     }
 
     @Test
     public void shouldGetSubscriptionDetailsAndAlsoFromReportIfChannelIsContactCenter() {
         String msisdn = "1234567890";
         SubscriptionPack pack = SubscriptionPack.BARI_KILKARI;
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn(msisdn).withPack(pack).build();
         SubscriberResponse subscriberResponse = new SubscriberResponse("subscriptionId", "bName", 25, DateTime.now(), DateTime.now(), null, new LocationResponse("d", "b", "p"));
         ArrayList<Subscription> subscriptionList = new ArrayList<>();
         subscriptionList.add(subscription);
         ArrayList<SubscriberResponse> subscriberResponseList = new ArrayList<>();
         subscriberResponseList.add(subscriberResponse);
         ArrayList<SubscriptionDetailsResponse> expectedResponse = new ArrayList<>();
         expectedResponse.add(new SubscriptionDetailsResponse(null, pack, null, null));
         when(allSubscriptions.findByMsisdn(msisdn)).thenReturn(subscriptionList);
         when(reportingServiceImpl.getSubscribersByMsisdn(msisdn)).thenReturn(subscriberResponseList);
         when(subscriptionDetailsResponseMapper.map(subscriptionList, subscriberResponseList)).thenReturn(expectedResponse);
 
         List<SubscriptionDetailsResponse> actualResponse = subscriptionService.getSubscriptionDetails(msisdn, Channel.CONTACT_CENTER);
 
         verify(allSubscriptions).findByMsisdn(msisdn);
         verify(reportingServiceImpl).getSubscribersByMsisdn(msisdn);
         verify(subscriptionDetailsResponseMapper).map(subscriptionList, subscriberResponseList);
         assertEquals(expectedResponse, actualResponse);
     }
 
     @Test
     public void shouldGetSubscriptionDetailsOnlyFromTransactionalDBAndNotFromReportIfChannelIsIVR() {
         String msisdn = "1234567890";
         SubscriptionPack pack = SubscriptionPack.BARI_KILKARI;
         Subscription subscription = new SubscriptionBuilder().withDefaults().withMsisdn(msisdn).withPack(pack).build();
         ArrayList<Subscription> subscriptionList = new ArrayList<>();
         subscriptionList.add(subscription);
         ArrayList<SubscriptionDetailsResponse> expectedResponse = new ArrayList<>();
         expectedResponse.add(new SubscriptionDetailsResponse(null, pack, null, null));
         when(allSubscriptions.findByMsisdn(msisdn)).thenReturn(subscriptionList);
         when(subscriptionDetailsResponseMapper.map(subscriptionList, Collections.EMPTY_LIST)).thenReturn(expectedResponse);
 
         List<SubscriptionDetailsResponse> actualResponse = subscriptionService.getSubscriptionDetails(msisdn, Channel.IVR);
 
         verify(allSubscriptions).findByMsisdn(msisdn);
         verify(reportingServiceImpl, never()).getSubscribersByMsisdn(anyString());
         verify(subscriptionDetailsResponseMapper).map(subscriptionList, Collections.EMPTY_LIST);
         assertEquals(expectedResponse, actualResponse);
     }
 
     @Test
     public void shouldUpdateToLatestMsisdnForAnEarlySubscriptionActivationSentToSMWhenMsisdnChanged() {
         String subscriptionId = "subscriptionId";
         String oldMsisdn = "123467890";
         SubscriptionPack pack = SubscriptionPack.NANHI_KILKARI;
         Channel channel = Channel.IVR;
         String newMsisdn = "1234567891";
         OMSubscriptionRequest omSubscriptionRequest = new OMSubscriptionRequest(oldMsisdn, pack, channel, subscriptionId);
         Subscription subscription = new Subscription(newMsisdn, pack, DateTime.now(), DateTime.now().plusDays(20), 1);
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.initiateActivationRequestForEarlySubscription(omSubscriptionRequest);
 
         ArgumentCaptor<OMSubscriptionRequest> captor = ArgumentCaptor.forClass(OMSubscriptionRequest.class);
         verify(onMobileSubscriptionManagerPublisher).sendActivationRequest(captor.capture());
         OMSubscriptionRequest actualOMSubscriptionRequest = captor.getValue();
         assertEquals(subscriptionId, actualOMSubscriptionRequest.getSubscriptionId());
         assertEquals(newMsisdn, actualOMSubscriptionRequest.getMsisdn());
         assertEquals(channel, actualOMSubscriptionRequest.getChannel());
         assertEquals(pack, actualOMSubscriptionRequest.getPack());
     }
 
     @Test
     public void shouldCalculateWeekNumberBasedOnPackAndActivationDateIfTheSubscriptionIsActive() {
         DateTime now = DateTime.now();
         Subscription subscription = new SubscriptionBuilder().withDefaults().withScheduleStartDate(now.minusWeeks(4)).withPack(SubscriptionPack.NAVJAAT_KILKARI).withStatus(SubscriptionStatus.ACTIVE).build();
         String subscriptionId = subscription.getSubscriptionId();
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.renewSubscription(subscription.getSubscriptionId(), now, null);
 
         ArgumentCaptor<SubscriptionStateChangeRequest> reportRequestCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         verify(reportingServiceImpl).reportSubscriptionStateChange(reportRequestCaptor.capture());
         SubscriptionStateChangeRequest request = reportRequestCaptor.getValue();
         assertEquals(Integer.valueOf(21), request.getWeekNumber());
     }
 
     @Test
     public void shouldReturnNullWeekNumberIfScheduleStartDateIsNull_WhenSubscriptionHasNotBeenActivated() {
         DateTime now = DateTime.now();
         Subscription subscription = new Subscription("1234567890", SubscriptionPack.NANHI_KILKARI, now, now.plusWeeks(4), null);
         subscription.setStatus(SubscriptionStatus.NEW_EARLY);
         String subscriptionId = subscription.getSubscriptionId();
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.requestDeactivation(new DeactivationRequest(subscriptionId, Channel.CONTACT_CENTER, now.plusDays(2), "Reason"));
 
         ArgumentCaptor<SubscriptionStateChangeRequest> reportRequestCaptor = ArgumentCaptor.forClass(SubscriptionStateChangeRequest.class);
         verify(reportingServiceImpl).reportSubscriptionStateChange(reportRequestCaptor.capture());
         SubscriptionStateChangeRequest request = reportRequestCaptor.getValue();
         assertNull(request.getWeekNumber());
     }
 
     @Test
     public void shouldRescheduleACompletionEventForNowIfExists() {
         DateTime now = DateTime.now().withMillisOfSecond(0);
         SubscriptionPack pack = SubscriptionPack.NAVJAAT_KILKARI;
         Subscription subscription = new SubscriptionBuilder().withDefaults().withScheduleStartDate(now.minusWeeks(pack.getTotalWeeks() - 1)).withPack(pack).withStatus(SubscriptionStatus.ACTIVE).build();
         String subscriptionId = subscription.getSubscriptionId();
         subscription.campaignCompleted();
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.renewSubscription(subscription.getSubscriptionId(), now, 0);
 
         ArgumentCaptor<RunOnceSchedulableJob> captor = ArgumentCaptor.forClass(RunOnceSchedulableJob.class);
         verify(motechSchedulerService).safeScheduleRunOnceJob(captor.capture());
         RunOnceSchedulableJob runOnceSchedulableJob = captor.getValue();
         assertEquals(SubscriptionEventKeys.SUBSCRIPTION_COMPLETE, runOnceSchedulableJob.getMotechEvent().getSubject());
         assertEquals(subscriptionId, runOnceSchedulableJob.getMotechEvent().getParameters().get(MotechSchedulerService.JOB_ID_KEY));
         OMSubscriptionRequest OMSubscriptionRequest = (OMSubscriptionRequest) runOnceSchedulableJob.getMotechEvent().getParameters().get("0");
         assertEquals(OMSubscriptionRequest.class, OMSubscriptionRequest.getClass());
         assertEquals(now, new DateTime(runOnceSchedulableJob.getStartDate()).withMillisOfSecond(0));
         assertEquals(Channel.MOTECH, OMSubscriptionRequest.getChannel());
     }
 
     @Test
     public void shouldNotRescheduleACompletionEventIfRenewalIsNotForLastMessage() {
         DateTime now = DateTime.now().withMillisOfSecond(0);
         SubscriptionPack pack = SubscriptionPack.NAVJAAT_KILKARI;
         Subscription subscription = new SubscriptionBuilder().withDefaults().withScheduleStartDate(now.minusWeeks(pack.getTotalWeeks() - 1)).withPack(pack).withStatus(SubscriptionStatus.ACTIVE).build();
         String subscriptionId = subscription.getSubscriptionId();
         when(allSubscriptions.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         subscriptionService.renewSubscription(subscription.getSubscriptionId(), now, 0);
 
         verify(motechSchedulerService, never()).safeScheduleRunOnceJob(any(RunOnceSchedulableJob.class));
     }
 
     @Test
     public void shouldScheduleCompletionEventForASubscription() {
         DateTime now = DateTime.now().withMillisOfSecond(0);
         SubscriptionPack pack = SubscriptionPack.NAVJAAT_KILKARI;
         Subscription subscription = new SubscriptionBuilder().withDefaults().withScheduleStartDate(now.minusWeeks(pack.getTotalWeeks() - 1)).withPack(pack).withStatus(SubscriptionStatus.ACTIVE).build();
         String subscriptionId = subscription.getSubscriptionId();
 
         subscriptionService.scheduleCompletion(subscription, now);
 
         ArgumentCaptor<RunOnceSchedulableJob> captor = ArgumentCaptor.forClass(RunOnceSchedulableJob.class);
         verify(motechSchedulerService).safeScheduleRunOnceJob(captor.capture());
         RunOnceSchedulableJob runOnceSchedulableJob = captor.getValue();
         assertEquals(SubscriptionEventKeys.SUBSCRIPTION_COMPLETE, runOnceSchedulableJob.getMotechEvent().getSubject());
         assertEquals(subscriptionId, runOnceSchedulableJob.getMotechEvent().getParameters().get(MotechSchedulerService.JOB_ID_KEY));
         OMSubscriptionRequest OMSubscriptionRequest = (OMSubscriptionRequest) runOnceSchedulableJob.getMotechEvent().getParameters().get("0");
         assertEquals(OMSubscriptionRequest.class, OMSubscriptionRequest.getClass());
         assertEquals(now, new DateTime(runOnceSchedulableJob.getStartDate()).withMillisOfSecond(0));
         assertEquals(Channel.MOTECH, OMSubscriptionRequest.getChannel());
     }
 
     @Test
     public void shouldThrowAnExceptionIfSubscriptionDoesNotExistForUpdate() {
         Subscription subscription = new SubscriptionBuilder().withDefaults().withPack(SubscriptionPack.NAVJAAT_KILKARI).withStatus(SubscriptionStatus.ACTIVE).build();
 
         expectedException.expect(IllegalArgumentException.class);
         expectedException.expectMessage(String.format("Subscription %s does not exist in db", subscription.getSubscriptionId()));
 
         doThrow(new IllegalArgumentException("Subscription does not exist")).when(allSubscriptions).update(subscription);
         subscriptionService.updateSubscription(subscription);
     }
 
     @Test
     public void shouldUpdateASubscriptionIfExists() {
         Subscription subscription = new SubscriptionBuilder().withDefaults().withPack(SubscriptionPack.NAVJAAT_KILKARI).withStatus(SubscriptionStatus.ACTIVE).build();
         subscription.campaignCompleted();
 
         subscriptionService.updateSubscription(subscription);
 
         verify(allSubscriptions).update(subscription);
     }
 }
