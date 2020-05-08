 package org.motechproject.ananya.kilkari.service;
 
 import org.joda.time.DateTime;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InOrder;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.motechproject.ananya.kilkari.contract.request.CallDetailsReportRequest;
 import org.motechproject.ananya.kilkari.factory.OBDServiceOptionFactory;
 import org.motechproject.ananya.kilkari.handlers.callback.obd.ServiceOptionHandler;
 import org.motechproject.ananya.kilkari.mapper.ValidCallDeliveryFailureRecordObjectMapper;
 import org.motechproject.ananya.kilkari.message.service.CampaignMessageAlertService;
 import org.motechproject.ananya.kilkari.message.service.InboxService;
 import org.motechproject.ananya.kilkari.messagecampaign.service.MessageCampaignService;
 import org.motechproject.ananya.kilkari.obd.domain.CampaignMessage;
 import org.motechproject.ananya.kilkari.obd.domain.CampaignMessageStatus;
 import org.motechproject.ananya.kilkari.obd.domain.ServiceOption;
 import org.motechproject.ananya.kilkari.obd.domain.ValidFailedCallReport;
 import org.motechproject.ananya.kilkari.obd.request.*;
 import org.motechproject.ananya.kilkari.obd.service.CampaignMessageService;
 import org.motechproject.ananya.kilkari.reporting.service.ReportingService;
 import org.motechproject.ananya.kilkari.request.CallDurationWebRequest;
 import org.motechproject.ananya.kilkari.request.InboxCallDetailsWebRequest;
 import org.motechproject.ananya.kilkari.request.OBDSuccessfulCallDetailsRequest;
 import org.motechproject.ananya.kilkari.request.OBDSuccessfulCallDetailsWebRequest;
 import org.motechproject.ananya.kilkari.subscription.builder.SubscriptionBuilder;
 import org.motechproject.ananya.kilkari.subscription.domain.Operator;
 import org.motechproject.ananya.kilkari.subscription.domain.Subscription;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionPack;
 import org.motechproject.ananya.kilkari.subscription.domain.SubscriptionStatus;
 import org.motechproject.ananya.kilkari.subscription.exceptions.ValidationException;
 import org.motechproject.ananya.kilkari.subscription.validators.DateUtils;
 import org.motechproject.ananya.kilkari.subscription.validators.Errors;
 import org.motechproject.ananya.kilkari.utils.CampaignMessageIdStrategy;
 import org.motechproject.ananya.kilkari.service.validator.CallDeliveryFailureRecordValidator;
 import org.motechproject.ananya.kilkari.service.validator.CallDetailsRequestValidator;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import static junit.framework.Assert.assertEquals;
 import static org.hamcrest.Matchers.hasEntry;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertThat;
 import static org.mockito.Matchers.eq;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class KilkariCampaignServiceTest {
 
     private KilkariCampaignService kilkariCampaignService;
 
     @Mock
     private MessageCampaignService messageCampaignService;
     @Mock
     private KilkariSubscriptionService kilkariSubscriptionService;
     @Mock
     private CampaignMessageIdStrategy campaignMessageIdStrategy;
     @Mock
     private CampaignMessageService campaignMessageService;
     @Mock
     private ReportingService reportingService;
     @Mock
     private CallDetailsRequestPublisher callDetailsRequestPublisher;
     @Mock
     private CallDeliveryFailureRecordValidator callDeliveryFailureRecordValidator;
     @Mock
     private InboxService inboxService;
     @Mock
     private OBDServiceOptionFactory obdServiceOptionFactory;
     @Mock
     private ServiceOptionHandler serviceOptionHandler;
     @Mock
     private CallDetailsRequestValidator successfulCallDetailsRequestValidator;
     @Mock
     private CampaignMessageAlertService campaignMessageAlertService;
     @Mock
     private ValidCallDeliveryFailureRecordObjectMapper validCallDeliveryFailureRecordMapper;
     @Rule
     public ExpectedException expectedException = ExpectedException.none();
 
     @Before
     public void setUp() {
         initMocks(this);
         kilkariCampaignService = new KilkariCampaignService(messageCampaignService, kilkariSubscriptionService, campaignMessageIdStrategy, campaignMessageAlertService, campaignMessageService, reportingService, callDetailsRequestPublisher, callDeliveryFailureRecordValidator, inboxService, obdServiceOptionFactory, successfulCallDetailsRequestValidator, validCallDeliveryFailureRecordMapper);
     }
 
     @Test
     public void shouldGetMessageTimings() {
         String msisdn = "1234567890";
         List<Subscription> subscriptions = new ArrayList<>();
 
         DateTime now = DateTime.now();
         DateTime subscriptionStartDate1 = now.plusWeeks(2);
         DateTime subscriptionStartDate2 = now.plusWeeks(3);
         Subscription subscription1 = new SubscriptionBuilder().withDefaults().withCreationDate(now).withStartDate(subscriptionStartDate1).withStatus(SubscriptionStatus.ACTIVE).build();
         Subscription subscription2 = new SubscriptionBuilder().withDefaults().withCreationDate(now).withStartDate(subscriptionStartDate2).withStatus(SubscriptionStatus.ACTIVE).build();
         subscriptions.add(subscription1);
         subscriptions.add(subscription2);
 
         List<DateTime> dateTimes = new ArrayList<>();
         dateTimes.add(now);
 
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenReturn(subscriptions);
 
         when(messageCampaignService.getMessageTimings(
                 subscription1.getSubscriptionId(),
                subscription1.getCreationDate(),
                 subscription1.endDate())).thenReturn(dateTimes);
         when(messageCampaignService.getMessageTimings(
                 subscription2.getSubscriptionId(),
                subscription2.getCreationDate(),
                 subscription2.endDate())).thenReturn(dateTimes);
 
         Map<String, List<DateTime>> messageTimings = kilkariCampaignService.getMessageTimings(msisdn);
 
         verify(messageCampaignService).getMessageTimings(
                 eq(subscription1.getSubscriptionId()),
                eq(subscription1.getCreationDate()),
                 eq(subscription1.endDate()));
 
         verify(messageCampaignService).getMessageTimings(
                 eq(subscription2.getSubscriptionId()),
                eq(subscription2.getCreationDate()),
                 eq(subscription2.endDate()));
 
         assertThat(messageTimings.size(), is(2));
         assertThat(messageTimings, hasEntry(subscription1.getSubscriptionId(), dateTimes));
         assertThat(messageTimings, hasEntry(subscription2.getSubscriptionId(), dateTimes));
     }
 
     @Test
     public void shouldPublishObdCallbackRequest() {
         OBDSuccessfulCallDetailsWebRequest obdSuccessfulCallDetailsRequest = new OBDSuccessfulCallDetailsWebRequest();
 
         kilkariCampaignService.publishSuccessfulCallRequest(obdSuccessfulCallDetailsRequest);
 
         verify(callDetailsRequestPublisher).publishSuccessfulCallRequest(obdSuccessfulCallDetailsRequest);
     }
 
     @Test
     public void shouldProcessSuccessfulCampaignMessageDelivery() {
         OBDSuccessfulCallDetailsWebRequest obdSuccessfulCallDetailsRequest = Mockito.mock(OBDSuccessfulCallDetailsWebRequest.class);
         String subscriptionId = "subscriptionId";
         String campaignId = "WEEK1";
         String msisdn = "1234567890";
         Integer retryCount = 3;
         String serviceOption = ServiceOption.HELP.name();
         String startTime = "25-12-2012 12-13-14";
         String endTime = "27-12-2012 12-15-19";
         CallDurationWebRequest callDurationWebRequest = new CallDurationWebRequest(startTime, endTime);
 
         when(obdSuccessfulCallDetailsRequest.getMsisdn()).thenReturn(msisdn);
         when(obdSuccessfulCallDetailsRequest.getCampaignId()).thenReturn(campaignId);
         when(obdSuccessfulCallDetailsRequest.getServiceOption()).thenReturn(serviceOption);
         when(obdSuccessfulCallDetailsRequest.getCallDurationWebRequest()).thenReturn(callDurationWebRequest);
         when(obdSuccessfulCallDetailsRequest.getSubscriptionId()).thenReturn(subscriptionId);
 
         CampaignMessage campaignMessage = mock(CampaignMessage.class);
         when(campaignMessage.getDnpRetryCount()).thenReturn(retryCount);
         when(campaignMessageService.find(subscriptionId, campaignId)).thenReturn(campaignMessage);
 
         when(successfulCallDetailsRequestValidator.validate(any(OBDSuccessfulCallDetailsRequest.class))).thenReturn(new Errors());
         when(obdServiceOptionFactory.getHandler(ServiceOption.HELP)).thenReturn(serviceOptionHandler);
         when(obdSuccessfulCallDetailsRequest.validate()).thenReturn(new Errors());
 
         kilkariCampaignService.processSuccessfulMessageDelivery(obdSuccessfulCallDetailsRequest);
 
         InOrder inOrder = Mockito.inOrder(obdSuccessfulCallDetailsRequest, successfulCallDetailsRequestValidator, campaignMessageService, reportingService);
         inOrder.verify(obdSuccessfulCallDetailsRequest).validate();
         inOrder.verify(successfulCallDetailsRequestValidator).validate(any(OBDSuccessfulCallDetailsRequest.class));
         inOrder.verify(campaignMessageService).find(subscriptionId, campaignId);
 
         ArgumentCaptor<CallDetailsReportRequest> campaignMessageDeliveryReportRequestArgumentCaptor = ArgumentCaptor.forClass(CallDetailsReportRequest.class);
         verify(reportingService).reportCampaignMessageDeliveryStatus(campaignMessageDeliveryReportRequestArgumentCaptor.capture());
         CallDetailsReportRequest campaignMessageDeliveryReportRequest = campaignMessageDeliveryReportRequestArgumentCaptor.getValue();
 
         inOrder.verify(campaignMessageService).deleteCampaignMessage(campaignMessage);
 
         assertEquals(subscriptionId, campaignMessageDeliveryReportRequest.getSubscriptionId());
         assertEquals(msisdn, campaignMessageDeliveryReportRequest.getMsisdn());
         assertEquals(campaignId, campaignMessageDeliveryReportRequest.getCampaignId());
         assertEquals(retryCount.toString(), campaignMessageDeliveryReportRequest.getRetryCount());
         assertEquals(serviceOption, campaignMessageDeliveryReportRequest.getServiceOption());
         assertEquals(new DateTime(2012, 12, 25, 12, 13, 14), campaignMessageDeliveryReportRequest.getStartTime());
         assertEquals(new DateTime(2012, 12, 27, 12, 15, 19), campaignMessageDeliveryReportRequest.getEndTime());
 
         verify(serviceOptionHandler).process(any(OBDSuccessfulCallDetailsRequest.class));
     }
 
     @Test
     public void shouldNotProcessSuccessfulCampaignMessageDeliveryIfThereIsNoSubscriptionAvailable() {
         String subscriptionId = "subscriptionId";
         String campaignId = "WEEK1";
         String msisdn = "1234567890";
         String serviceOption = ServiceOption.HELP.name();
         String startTime = "25-12-2012 12-13-14";
         String endTime = "27-12-2012 12-15-19";
         CallDurationWebRequest callDurationWebRequest = new CallDurationWebRequest(startTime, endTime);
         OBDSuccessfulCallDetailsWebRequest obdSuccessfulCallDetailsRequest = new OBDSuccessfulCallDetailsWebRequest(msisdn, campaignId, callDurationWebRequest, serviceOption);
 
         obdSuccessfulCallDetailsRequest.setSubscriptionId(subscriptionId);
         when(campaignMessageService.find(subscriptionId, campaignId)).thenReturn(null);
 
         when(successfulCallDetailsRequestValidator.validate(any(OBDSuccessfulCallDetailsRequest.class))).thenReturn(new Errors());
 
         kilkariCampaignService.processSuccessfulMessageDelivery(obdSuccessfulCallDetailsRequest);
 
         verify(campaignMessageService).find(subscriptionId, campaignId);
         verify(campaignMessageService, never()).deleteCampaignMessage(any(CampaignMessage.class));
         verifyZeroInteractions(reportingService);
     }
 
     @Test
     public void shouldProcessInvalidCallRecords() {
         InvalidOBDRequestEntries invalidOBDRequestEntries = new InvalidOBDRequestEntries();
 
         kilkariCampaignService.publishInvalidCallRecordsRequest(invalidOBDRequestEntries);
 
         verify(callDetailsRequestPublisher).publishInvalidCallRecordsRequest(invalidOBDRequestEntries);
     }
 
     @Test
     public void shouldScheduleUnsubscriptionWhenPackIsCompletedAndWhenStatusIsNotDeactivated() {
         String subscriptionId = "abcd1234";
 
         Subscription subscription = new Subscription("9988776655", SubscriptionPack.BARI_KILKARI, DateTime.now().minusWeeks(1), SubscriptionStatus.NEW);
 
         subscription.setStatus(SubscriptionStatus.ACTIVE);
 
         when(kilkariSubscriptionService.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         kilkariCampaignService.processCampaignCompletion(subscriptionId);
 
         verify(kilkariSubscriptionService).processSubscriptionCompletion(subscription);
     }
 
     @Test
     public void shouldNotScheduleUnsubscriptionWhenPackIsCompletedAndStatusIsDeactivated() {
         String subscriptionId = "abcd1234";
         Subscription subscription = new Subscription("9988776655", SubscriptionPack.BARI_KILKARI, DateTime.now().minusWeeks(1), SubscriptionStatus.NEW);
 
         subscription.setStatus(SubscriptionStatus.PENDING_DEACTIVATION);
 
         when(kilkariSubscriptionService.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         kilkariCampaignService.processCampaignCompletion(subscriptionId);
 
         verify(kilkariSubscriptionService, never()).processSubscriptionCompletion(subscription);
     }
 
     @Test
     public void shouldPublishCallDeliveryFailureRecords() {
         FailedCallReports failedCallReports = Mockito.mock(FailedCallReports.class);
         kilkariCampaignService.publishCallDeliveryFailureRequest(failedCallReports);
         verify(callDetailsRequestPublisher).publishCallDeliveryFailureRecord(failedCallReports);
     }
 
     @Test
     public void shouldHandleCallDeliveryFailureRecord() {
         FailedCallReports failedCallReports = new FailedCallReports();
 
         ArrayList<FailedCallReport> reportArrayList = new ArrayList<>();
         FailedCallReport failedCallReport = mock(FailedCallReport.class);
         reportArrayList.add(failedCallReport);
         failedCallReports.setFailedCallReports(reportArrayList);
 
         when(callDeliveryFailureRecordValidator.validate(failedCallReport)).thenReturn(new Errors());
 
         kilkariCampaignService.processCallDeliveryFailureRecord(failedCallReports);
 
         verify(callDeliveryFailureRecordValidator, times(1)).validate(any(FailedCallReport.class));
     }
 
     @Test
     public void shouldPublishErroredOutCallDeliveryFailureRecords() {
         String msisdn = "12345";
         String subscriptionId = "abcd";
         FailedCallReports failedCallReports = new FailedCallReports();
 
         ArrayList<FailedCallReport> callDeliveryFailureRecordObjects = new ArrayList<>();
         FailedCallReport erroredOutFailedCallReport = mock(FailedCallReport.class);
         when(erroredOutFailedCallReport.getMsisdn()).thenReturn(msisdn);
         when(erroredOutFailedCallReport.getSubscriptionId()).thenReturn(subscriptionId);
         FailedCallReport successfulFailedCallReport = mock(FailedCallReport.class);
         callDeliveryFailureRecordObjects.add(erroredOutFailedCallReport);
         callDeliveryFailureRecordObjects.add(successfulFailedCallReport);
         failedCallReports.setFailedCallReports(callDeliveryFailureRecordObjects);
 
         when(callDeliveryFailureRecordValidator.validate(successfulFailedCallReport)).thenReturn(new Errors());
 
         Errors errors = new Errors();
         errors.add("Some error description");
         when(callDeliveryFailureRecordValidator.validate(erroredOutFailedCallReport)).thenReturn(errors);
 
         kilkariCampaignService.processCallDeliveryFailureRecord(failedCallReports);
 
         verify(callDeliveryFailureRecordValidator, times(2)).validate(any(FailedCallReport.class));
 
         ArgumentCaptor<InvalidFailedCallReports> invalidCallDeliveryFailureRecordArgumentCaptor = ArgumentCaptor.forClass(InvalidFailedCallReports.class);
         verify(callDetailsRequestPublisher).publishInvalidCallDeliveryFailureRecord(invalidCallDeliveryFailureRecordArgumentCaptor.capture());
         InvalidFailedCallReports invalidFailedCallReports = invalidCallDeliveryFailureRecordArgumentCaptor.getValue();
         List<InvalidFailedCallReport> recordObjectFaileds = invalidFailedCallReports.getRecordObjectFaileds();
 
         assertEquals(1, recordObjectFaileds.size());
         assertEquals("Some error description", recordObjectFaileds.get(0).getDescription());
         assertEquals(msisdn, recordObjectFaileds.get(0).getMsisdn());
         assertEquals(subscriptionId, recordObjectFaileds.get(0).getSubscriptionId());
     }
 
     @Test
     public void shouldPublishSuccessfulCallDeliveryFailureRecords() {
         FailedCallReports failedCallReports = new FailedCallReports();
 
         ArrayList<FailedCallReport> callDeliveryFailureRecordObjects = new ArrayList<>();
         FailedCallReport erroredOutFailedCallReport = mock(FailedCallReport.class);
         FailedCallReport successfulFailedCallReport1 = new FailedCallReport("sub1", "1234567890", "WEEK13", "iu_dnp");
         FailedCallReport successfulFailedCallReport2 = new FailedCallReport("sub2", "1234567891", "WEEK13", "iu_dnc");
         callDeliveryFailureRecordObjects.add(erroredOutFailedCallReport);
         callDeliveryFailureRecordObjects.add(successfulFailedCallReport1);
         callDeliveryFailureRecordObjects.add(successfulFailedCallReport2);
         failedCallReports.setFailedCallReports(callDeliveryFailureRecordObjects);
 
         Errors errors = new Errors();
         errors.add("Some error description");
         when(callDeliveryFailureRecordValidator.validate(erroredOutFailedCallReport)).thenReturn(errors);
         Errors noError = new Errors();
         when(callDeliveryFailureRecordValidator.validate(successfulFailedCallReport1)).thenReturn(noError);
         when(callDeliveryFailureRecordValidator.validate(successfulFailedCallReport2)).thenReturn(noError);
         ValidFailedCallReport validFailedCallReport1 = new ValidFailedCallReport("sub1", "1234567890", "WEEK13", CampaignMessageStatus.DNP, DateTime.now());
         ValidFailedCallReport validFailedCallReport2 = new ValidFailedCallReport("sub2", "1234567891", "WEEK13", CampaignMessageStatus.DNC, DateTime.now());
         when(validCallDeliveryFailureRecordMapper.mapFrom(successfulFailedCallReport1)).thenReturn(validFailedCallReport1);
         when(validCallDeliveryFailureRecordMapper.mapFrom(successfulFailedCallReport2)).thenReturn(validFailedCallReport2);
 
         kilkariCampaignService.processCallDeliveryFailureRecord(failedCallReports);
 
         verify(callDeliveryFailureRecordValidator, times(3)).validate(any(FailedCallReport.class));
 
         ArgumentCaptor<ValidFailedCallReport> captor = ArgumentCaptor.forClass(ValidFailedCallReport.class);
         verify(callDetailsRequestPublisher, times(2)).publishValidCallDeliveryFailureRecord(captor.capture());
         List<ValidFailedCallReport> actualValidFailedCallReports = captor.getAllValues();
         assertEquals(validFailedCallReport1, actualValidFailedCallReports.get(0));
         assertEquals(validFailedCallReport2, actualValidFailedCallReports.get(1));
     }
 
     @Test
     public void shouldNotPublishToErrorQueueIfErroredOutCallDeliveryFailureRecordsAreEmpty() {
         FailedCallReports failedCallReports = new FailedCallReports();
 
         ArrayList<FailedCallReport> callDeliveryFailureRecordObjects = new ArrayList<>();
         FailedCallReport successfulFailedCallReport = mock(FailedCallReport.class);
         callDeliveryFailureRecordObjects.add(successfulFailedCallReport);
         failedCallReports.setFailedCallReports(callDeliveryFailureRecordObjects);
 
         when(callDeliveryFailureRecordValidator.validate(successfulFailedCallReport)).thenReturn(new Errors());
 
         kilkariCampaignService.processCallDeliveryFailureRecord(failedCallReports);
 
         verify(callDeliveryFailureRecordValidator, times(1)).validate(any(FailedCallReport.class));
         verify(callDetailsRequestPublisher, never()).publishInvalidCallDeliveryFailureRecord(any(InvalidFailedCallReports.class));
     }
 
     @Test
     public void shouldCallCampaignMessageAlertServiceAndUpdateInboxToHoldLastScheduledMessage() {
         DateTime creationDate = DateTime.now();
         String messageId = "week10";
         String campaignName = "campaignName";
         Operator operator = Operator.AIRTEL;
         String msisdn = "9988776655";
         Subscription subscription = new Subscription(msisdn, SubscriptionPack.BARI_KILKARI, creationDate, SubscriptionStatus.NEW);
         subscription.setOperator(operator);
         subscription.setStatus(SubscriptionStatus.ACTIVE);
         String subscriptionId = subscription.getSubscriptionId();
         DateTime expiryDate = creationDate.plusWeeks(1);
 
         when(kilkariSubscriptionService.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         when(messageCampaignService.getCampaignStartDate(subscriptionId, campaignName)).thenReturn(creationDate);
         when(campaignMessageIdStrategy.createMessageId(campaignName, creationDate, subscription.getPack())).thenReturn(messageId);
 
         kilkariCampaignService.scheduleWeeklyMessage(subscriptionId, campaignName);
 
         verify(campaignMessageAlertService).scheduleCampaignMessageAlert(subscriptionId, messageId, expiryDate, msisdn, operator.name());
         verify(inboxService).newMessage(subscriptionId, messageId);
     }
 
     @Test
     public void shouldCallCampaignMessageAlertServiceAndNotUpdateInboxWhenSubscriptionIsNotActive() {
         DateTime creationDate = DateTime.now();
         String msisdn = "9988776655";
         Subscription subscription = new Subscription(msisdn, SubscriptionPack.BARI_KILKARI, creationDate, SubscriptionStatus.NEW);
         Operator operator = Operator.AIRTEL;
         subscription.setOperator(operator);
         subscription.setStatus(SubscriptionStatus.PENDING_ACTIVATION);
         String subscriptionId = subscription.getSubscriptionId();
         String messageId = "week10";
         String campaignName = "campaignName";
         DateTime expiryDate = creationDate.plusWeeks(1);
 
         when(kilkariSubscriptionService.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         when(messageCampaignService.getCampaignStartDate(subscriptionId, campaignName)).thenReturn(creationDate);
         when(campaignMessageIdStrategy.createMessageId(campaignName, subscription.getStartDate(), subscription.getPack())).thenReturn(messageId);
 
         kilkariCampaignService.scheduleWeeklyMessage(subscriptionId, campaignName);
 
         verify(campaignMessageAlertService).scheduleCampaignMessageAlert(subscriptionId, messageId, expiryDate, msisdn, operator.name());
         verify(inboxService, never()).newMessage(subscriptionId, messageId);
     }
 
     @Test
     public void shouldNotUpdateInboxDuringActivationWhenMessageHasNotAlreadyBeenScheduled() {
         String msisdn = "1234567890";
         Subscription subscription = new Subscription(msisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), SubscriptionStatus.NEW);
         String subscriptionId = subscription.getSubscriptionId();
         Operator operator = Operator.AIRTEL;
         subscription.setOperator(operator);
 
         when(kilkariSubscriptionService.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         kilkariCampaignService.activateSchedule(subscriptionId);
 
         verify(inboxService, never()).newMessage(anyString(), anyString());
     }
 
     @Test
     public void shouldUpdateInboxDuringActivationWhenMessageHasAlreadyBeenScheduled() {
         String msisdn = "1234567890";
         Subscription subscription = new Subscription(msisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), SubscriptionStatus.NEW);
         String subscriptionId = subscription.getSubscriptionId();
         Operator operator = Operator.AIRTEL;
         subscription.setOperator(operator);
         String messageId = "mesasgeId";
 
         when(kilkariSubscriptionService.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         when(campaignMessageAlertService.scheduleCampaignMessageAlertForActivation(subscriptionId, msisdn, operator.name())).thenReturn(messageId);
 
         kilkariCampaignService.activateSchedule(subscriptionId);
 
         verify(inboxService).newMessage(subscriptionId, messageId);
     }
 
     @Test
     public void shouldNotUpdateInboxDuringRenewalWhenMessageHasNotAlreadyBeenScheduled() {
         String msisdn = "1234567890";
         Subscription subscription = new Subscription(msisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), SubscriptionStatus.NEW);
         String subscriptionId = subscription.getSubscriptionId();
         Operator operator = Operator.AIRTEL;
         subscription.setOperator(operator);
 
         when(kilkariSubscriptionService.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
 
         kilkariCampaignService.renewSchedule(subscriptionId);
 
         verify(inboxService, never()).newMessage(anyString(), anyString());
     }
 
     @Test
     public void shouldNotUpdateInboxDuringRenewalnWhenMessageHasAlreadyBeenScheduled() {
         String msisdn = "1234567890";
         Subscription subscription = new Subscription(msisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), SubscriptionStatus.NEW);
         String subscriptionId = subscription.getSubscriptionId();
         Operator operator = Operator.AIRTEL;
         subscription.setOperator(operator);
         String messageId = "mesasgeId";
 
         when(kilkariSubscriptionService.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         when(campaignMessageAlertService.scheduleCampaignMessageAlertForActivation(subscriptionId, msisdn, operator.name())).thenReturn(messageId);
 
         kilkariCampaignService.renewSchedule(subscriptionId);
 
         verify(inboxService, never()).newMessage(anyString(), anyString());
     }
 
     @Test
     public void shouldCallCampaignMessageAlertServiceOnActivation() {
         String msisdn = "1234567890";
         Subscription subscription = new Subscription(msisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), SubscriptionStatus.NEW);
         String subscriptionId = subscription.getSubscriptionId();
         Operator operator = Operator.AIRTEL;
         subscription.setOperator(operator);
 
         when(kilkariSubscriptionService.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         kilkariCampaignService.activateSchedule(subscriptionId);
 
         verify(campaignMessageAlertService).scheduleCampaignMessageAlertForActivation(subscriptionId, msisdn, operator.name());
     }
 
     @Test
     public void shouldCallCampaignMessageAlertServiceOnRenewal() {
         String msisdn = "1234567890";
         Subscription subscription = new Subscription(msisdn, SubscriptionPack.BARI_KILKARI, DateTime.now(), SubscriptionStatus.NEW);
         String subscriptionId = subscription.getSubscriptionId();
         Operator operator = Operator.AIRTEL;
         subscription.setOperator(operator);
 
         when(kilkariSubscriptionService.findBySubscriptionId(subscriptionId)).thenReturn(subscription);
         kilkariCampaignService.renewSchedule(subscriptionId);
 
         verify(campaignMessageAlertService).scheduleCampaignMessageAlertForRenewal(subscriptionId, msisdn, operator.name());
     }
 
     @Test
     public void shouldPublishInboxCallDetails() {
         InboxCallDetailsWebRequest inboxCallDetailsWebRequest = new InboxCallDetailsWebRequest();
 
         kilkariCampaignService.publishInboxCallDetailsRequest(inboxCallDetailsWebRequest);
 
         verify(callDetailsRequestPublisher).publishInboxCallDetailsRequest(inboxCallDetailsWebRequest);
     }
 
     @Test
     public void shouldValidateSubscriptionIdIfRequestValidationIsSuccessful() {
         String pack = "choti_kilkari";
         String msisdn = "1234567890";
         InboxCallDetailsWebRequest inboxCallDetailsWebRequest = new InboxCallDetailsWebRequest(msisdn, "WEEK12", new CallDurationWebRequest("22-11-2011 11-55-35", "23-12-2012 12-59-34"), pack);
         when(kilkariSubscriptionService.findSubscriptionInProgress(msisdn, SubscriptionPack.CHOTI_KILKARI)).thenReturn(null);
         expectedException.expect(ValidationException.class);
         expectedException.expectMessage("Invalid inbox call details request: Subscription not found");
 
         kilkariCampaignService.processInboxCallDetailsRequest(inboxCallDetailsWebRequest);
     }
 
     @Test
     public void shouldReportInboxCallDetails() {
         String pack = "choti_kilkari";
         String msisdn = "1234567890";
         String subscriptionId = "subscriptionId";
         String campaignId = "WEEK12";
         String startTime = "22-11-2011 11-55-35";
         String endTime = "23-12-2012 12-59-34";
         InboxCallDetailsWebRequest inboxCallDetailsWebRequest = new InboxCallDetailsWebRequest(msisdn, campaignId, new CallDurationWebRequest(startTime, endTime), pack);
         Subscription subscription = Mockito.mock(Subscription.class);
         when(kilkariSubscriptionService.findSubscriptionInProgress(msisdn, SubscriptionPack.CHOTI_KILKARI)).thenReturn(subscription);
         when(subscription.getSubscriptionId()).thenReturn(subscriptionId);
 
         kilkariCampaignService.processInboxCallDetailsRequest(inboxCallDetailsWebRequest);
 
         ArgumentCaptor<CallDetailsReportRequest> captor = ArgumentCaptor.forClass(CallDetailsReportRequest.class);
         verify(reportingService).reportCampaignMessageDeliveryStatus(captor.capture());
         CallDetailsReportRequest callDetailsReportRequest = captor.getValue();
 
         Assert.assertEquals(msisdn, callDetailsReportRequest.getMsisdn());
         Assert.assertEquals("INBOX", callDetailsReportRequest.getCallSource());
         Assert.assertEquals(campaignId, callDetailsReportRequest.getCampaignId());
         Assert.assertNull(callDetailsReportRequest.getRetryCount());
         Assert.assertNull(callDetailsReportRequest.getServiceOption());
         Assert.assertEquals(DateUtils.parseDateTime(startTime), callDetailsReportRequest.getStartTime());
         Assert.assertEquals(DateUtils.parseDateTime(endTime), callDetailsReportRequest.getEndTime());
         Assert.assertEquals("SUCCESS", callDetailsReportRequest.getStatus());
         Assert.assertEquals(subscriptionId, callDetailsReportRequest.getSubscriptionId());
 
         Assert.assertNull(callDetailsReportRequest.getServiceOption());
     }
 
     @Test
     public void shouldValidateInboxCallDetailsRequest() {
         InboxCallDetailsWebRequest inboxCallDetailsWebRequest = Mockito.mock(InboxCallDetailsWebRequest.class);
         Errors errors = new Errors();
         String errorString = "some error";
         errors.add(errorString);
         when(inboxCallDetailsWebRequest.validate()).thenReturn(errors);
         expectedException.expect(ValidationException.class);
         expectedException.expectMessage("Invalid inbox call details request: " + errorString);
 
         kilkariCampaignService.processInboxCallDetailsRequest(inboxCallDetailsWebRequest);
     }
 }
