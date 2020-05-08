 package org.motechproject.ghana.mtn.service;
 
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Matchers;
 import org.mockito.Mock;
 import org.motechproject.ghana.mtn.billing.dto.BillingServiceRequest;
 import org.motechproject.ghana.mtn.billing.dto.BillingServiceResponse;
 import org.motechproject.ghana.mtn.billing.dto.BillingCycleRequest;
 import org.motechproject.ghana.mtn.billing.service.BillingService;
 import org.motechproject.ghana.mtn.domain.*;
 import org.motechproject.ghana.mtn.domain.dto.SMSServiceRequest;
 import org.motechproject.ghana.mtn.domain.dto.SubscriptionRequest;
 import org.motechproject.ghana.mtn.domain.vo.Day;
 import org.motechproject.ghana.mtn.domain.vo.Week;
 import org.motechproject.ghana.mtn.domain.vo.WeekAndDay;
 import org.motechproject.ghana.mtn.exception.UserRegistrationFailureException;
 import org.motechproject.ghana.mtn.repository.AllSubscribers;
 import org.motechproject.ghana.mtn.repository.AllSubscriptions;
 import org.motechproject.ghana.mtn.testbuilders.TestSubscription;
 import org.motechproject.ghana.mtn.testbuilders.TestSubscriptionRequest;
 import org.motechproject.ghana.mtn.testbuilders.TestProgramType;
 import org.motechproject.ghana.mtn.validation.InputMessageParser;
 import org.motechproject.ghana.mtn.validation.ValidationError;
 import org.motechproject.server.messagecampaign.contract.CampaignRequest;
 import org.motechproject.server.messagecampaign.service.MessageCampaignService;
 
 import java.util.Arrays;
 import java.util.Collections;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ghana.mtn.domain.MessageBundle.BILLING_SUCCESS;
 import static org.motechproject.ghana.mtn.domain.MessageBundle.ENROLLMENT_SUCCESS;
 
 public class SubscriptionServiceImplTest {
     private SubscriptionServiceImpl service;
     @Mock
     private AllSubscribers allSubscribers;
     @Mock
     private AllSubscriptions allSubscriptions;
     @Mock
     private MessageCampaignService campaignService;
     @Mock
     private InputMessageParser inputMessageParser;
     @Mock
     private BillingService billingService;
     @Mock
     private MessageBundle messageBundle;
     @Mock
     private SMSService smsService;
 
     @Before
     public void setUp() {
         initMocks(this);
         service = new SubscriptionServiceImpl(allSubscribers, allSubscriptions, campaignService, inputMessageParser, billingService, messageBundle, smsService);
     }
 
     @Test
     public void shouldNotEnrollIfSubscriptionIsNotValid() {
         SubscriptionRequest subscriptionRequest = TestSubscriptionRequest.with("1234567890", "P 25");
         ProgramType programType = TestProgramType.with("Pregnancy", 3, 12, Arrays.asList("P"));
         Subscription subscription = TestSubscription.with(null, programType, DateTime.now(), new WeekAndDay(new Week(92), Day.MONDAY));
 
         when(inputMessageParser.parse("P 25")).thenReturn(subscription);
         when(messageBundle.get(MessageBundle.ENROLLMENT_FAILURE)).thenReturn("error");
 
         String actualResponse = service.enroll(subscriptionRequest);
         assertEquals("error", actualResponse);
 
         verify(allSubscriptions, never()).add(any(Subscription.class));
         verify(allSubscribers, never()).add(any(Subscriber.class));
         verify(campaignService, never()).startFor(any(CampaignRequest.class));
         verify(billingService, never()).startBillingCycle(Matchers.<BillingCycleRequest>any());
         assertEquals(null, subscription.getStatus());
     }
 
 
     @Test
     public void shouldNotEnrollIfSubscriberAlreadyHasAnActiveSubscriptionOfSameType() {
         SubscriptionRequest subscriptionRequest = TestSubscriptionRequest.with("1234567890", "P 25");
         ProgramType programType = TestProgramType.with("Pregnancy", 3, 12, Arrays.asList("P"));
         Subscription subscription = TestSubscription.with(null, programType, DateTime.now(), new WeekAndDay(new Week(12), Day.MONDAY));
         Subscription existingActiveSubscription = TestSubscription.with(null, programType, DateTime.now(), new WeekAndDay(new Week(31), Day.MONDAY));
 
         when(inputMessageParser.parse("P 25")).thenReturn(subscription);
         when(allSubscriptions.getAllActiveSubscriptionsForSubscriber("1234567890")).thenReturn(Arrays.asList(existingActiveSubscription));
         when(messageBundle.get(MessageBundle.ACTIVE_SUBSCRIPTION_PRESENT)).thenReturn("error");
 
         String response = service.enroll(subscriptionRequest);
         assertEquals("error", response);
 
         verify(allSubscriptions, never()).add(any(Subscription.class));
         verify(allSubscribers, never()).add(any(Subscriber.class));
         verify(campaignService, never()).startFor(any(CampaignRequest.class));
         verify(billingService, never()).startBillingCycle(Matchers.<BillingCycleRequest>any());
         assertEquals(null, subscription.getStatus());
     }
 
     @Test
     public void shouldPersistSubscriptionAndCampaignRequestForAValidSubscription() {
         SubscriptionRequest subscriptionRequest = TestSubscriptionRequest.with("1234567890", "P 25");
         ProgramType programType = TestProgramType.with("Pregnancy", 3, 12, Arrays.asList("P"));
         Subscription subscription = TestSubscription.with(null, programType, new DateTime(2011, 10, 8, 10, 10), new WeekAndDay(new Week(12), Day.MONDAY));
 
         when(inputMessageParser.parse("P 25")).thenReturn(subscription);
         when(allSubscriptions.getAllActiveSubscriptionsForSubscriber("1234567890")).thenReturn(Collections.EMPTY_LIST);
         when(billingService.checkIfUserHasFunds(Matchers.<BillingServiceRequest>any())).thenReturn(new BillingServiceResponse());
        when(billingService.processRegistration(Matchers.<RegistrationBillingRequest>any())).thenReturn(new BillingServiceResponse());
         when(messageBundle.get(ENROLLMENT_SUCCESS)).thenReturn("success");
 
         String response = service.enroll(subscriptionRequest);
         assertEquals("success",response);
 
         verify(allSubscriptions).add(subscription);
         verify(allSubscribers).add(any(Subscriber.class));
         verify(campaignService).startFor(any(CampaignRequest.class));
 
         ArgumentCaptor<SMSServiceRequest> captorForSmsService = ArgumentCaptor.forClass(SMSServiceRequest.class);
         verify(smsService, times(2)).send(captorForSmsService.capture());
 
         SMSServiceRequest smsRequestForSuccessfulBilling = captorForSmsService.getAllValues().get(0);
         assertEquals(messageBundle.get(BILLING_SUCCESS), smsRequestForSuccessfulBilling.getMessage());
         assertEquals(subscription.subscriberNumber() ,smsRequestForSuccessfulBilling.getMobileNumber());
         assertEquals(subscription.getProgramType(),smsRequestForSuccessfulBilling.getProgramType());
 
         SMSServiceRequest smsForRegistrationConfirmation = captorForSmsService.getAllValues().get(1);
         assertEquals(messageBundle.get(ENROLLMENT_SUCCESS),smsForRegistrationConfirmation.getMessage());
         assertEquals(subscription.subscriberNumber() ,smsForRegistrationConfirmation.getMobileNumber());
         assertEquals(subscription.getProgramType(),smsForRegistrationConfirmation.getProgramType());
 
         assertEquals(SubscriptionStatus.ACTIVE, subscription.getStatus());
         assertEquals(Day.MONDAY, subscription.getStartWeekAndDay().getDay());
     }
 
     @Test
     public void shouldNotPersistSubscriptionAndCreateCampaignRequest_IfBillingServiceRegistrationFails() {
         SubscriptionRequest subscriptionRequest = TestSubscriptionRequest.with("1234567890", "P 25");
         ProgramType programType = TestProgramType.with("Pregnancy", 3, 13, Arrays.asList("P"));
         Subscription subscription = TestSubscription.with(null, programType, new DateTime(2011, 10, 8, 10, 10), new WeekAndDay(new Week(12), Day.MONDAY));
 
         when(inputMessageParser.parse("P 25")).thenReturn(subscription);
         when(allSubscriptions.getAllActiveSubscriptionsForSubscriber("1234567899")).thenReturn(Collections.EMPTY_LIST);
         when(billingService.checkIfUserHasFunds(Matchers.<BillingServiceRequest>any())).thenReturn(new BillingServiceResponse());
         when(messageBundle.get(ValidationError.INSUFFICIENT_FUNDS)).thenReturn("no money");
 
         BillingServiceResponse billingServiceResponse = new BillingServiceResponse();
         billingServiceResponse.addError(ValidationError.INSUFFICIENT_FUNDS);
         when(billingService.startBillingCycle(Matchers.<BillingCycleRequest>any())).thenReturn(billingServiceResponse);
 
         String response = service.enroll(subscriptionRequest);
         assertEquals("no money", response);
 
         verify(allSubscriptions, never()).add(subscription);
         verify(allSubscribers, never()).add(any(Subscriber.class));
         verify(campaignService, never()).startFor(any(CampaignRequest.class));
         assertEquals(null, subscription.getStatus());
 
     }
 
     @Test
     public void shouldReturnDefaultErrorMessage_IfUserRegistrationFailureMessageIsEmpty() {
         Subscription subscription = TestSubscription.with(null, TestProgramType.with("Pregnancy", 3, 13, Arrays.asList("P")),
                 new DateTime(2011, 10, 8, 10, 10), new WeekAndDay(new Week(12), Day.MONDAY));
 
         when(inputMessageParser.parse("P 25")).thenReturn(subscription);
         when(allSubscriptions.getAllActiveSubscriptionsForSubscriber("1234567899")).thenReturn(Collections.EMPTY_LIST);
         when(billingService.checkIfUserHasFunds(Matchers.<BillingServiceRequest>any())).thenThrow(new UserRegistrationFailureException(""));
         when(messageBundle.get(MessageBundle.ENROLLMENT_FAILURE)).thenReturn("error");
 
         String response = service.enroll(TestSubscriptionRequest.with("1234567890", "P 25"));
         assertEquals("error", response);
     }
 }
