 package org.motechproject.ghana.mtn.process;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ghana.mtn.billing.dto.BillingServiceRequest;
 import org.motechproject.ghana.mtn.billing.dto.BillingServiceResponse;
 import org.motechproject.ghana.mtn.billing.service.BillingService;
 import org.motechproject.ghana.mtn.domain.MessageBundle;
 import org.motechproject.ghana.mtn.domain.ProgramType;
 import org.motechproject.ghana.mtn.domain.Subscription;
 import org.motechproject.ghana.mtn.domain.dto.SMSServiceRequest;
 import org.motechproject.ghana.mtn.repository.AllSubscriptions;
 import org.motechproject.ghana.mtn.service.SMSService;
 import org.motechproject.ghana.mtn.validation.ValidationError;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import static junit.framework.Assert.assertEquals;
 import static junit.framework.Assert.assertFalse;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class SubscriptionValidationTest {
 
     private SubscriptionValidation validation;
     @Mock
     private SMSService smsService;
     @Mock
     private MessageBundle messageBundle;
     @Mock
     private AllSubscriptions allSubscriptions;
     @Mock
     private BillingService billingService;
 
     @Before
     public void setUp() {
         initMocks(this);
         validation = new SubscriptionValidation(smsService, messageBundle, allSubscriptions, billingService);
     }
 
     @Test
     public void shouldReturnMessageForInvalidSubscription() {
         String mobileNumber = "123";
         String errMsg = "error msg";
         String program = "program";
 
         Subscription subscription = mock(Subscription.class);
         ProgramType programType = mock(ProgramType.class);
 
         setupSubscriptionMock(mobileNumber, program, programType, subscription);
         when(subscription.isNotValid()).thenReturn(true);
         when(messageBundle.get(MessageBundle.ENROLLMENT_FAILURE)).thenReturn(errMsg);
 
         Boolean reply = validation.startFor(subscription);
 
         assertFalse(reply);
         assertSMSRequest(mobileNumber, errMsg, program);
     }
 
     @Test
     public void shouldReturnErrorMessageIfActiveSubscriptionIsAlreadyPresent() {
         String mobileNumber = "123";
        String errMsg = "error msg";
         String program = "program";
 
         Subscription subscription = mock(Subscription.class);
         ProgramType programType = mock(ProgramType.class);
         List<Subscription> dbSubscriptions = Arrays.asList(subscription);
 
         setupSubscriptionMock(mobileNumber, program, programType, subscription);
         when(subscription.isNotValid()).thenReturn(false);
         when(messageBundle.get(MessageBundle.ACTIVE_SUBSCRIPTION_PRESENT)).thenReturn(errMsg);
         when(allSubscriptions.getAllActiveSubscriptionsForSubscriber(mobileNumber)).thenReturn(dbSubscriptions);
 
         Boolean reply = validation.startFor(subscription);
 
         assertFalse(reply);
        assertSMSRequest(mobileNumber, errMsg, program);
     }
 
 
     @Test
     public void shouldReturnMessageIfUserHasNoMoney() {
         String mobileNumber = "123";
         String errMsg = "error msg";
         String program = "program";
 
         Subscription subscription = mock(Subscription.class);
         ProgramType programType = mock(ProgramType.class);
         List<ValidationError> errors = new ArrayList<ValidationError>();
         BillingServiceResponse response = mock(BillingServiceResponse.class);
 
 
         setupSubscriptionMock(mobileNumber, program, programType, subscription);
         when(subscription.isNotValid()).thenReturn(false);
         when(messageBundle.get(errors)).thenReturn(errMsg);
         when(allSubscriptions.getAllActiveSubscriptionsForSubscriber(mobileNumber)).thenReturn(Collections.EMPTY_LIST);
         when(response.hasErrors()).thenReturn(true);
         when(response.getValidationErrors()).thenReturn(errors);
         when(billingService.checkIfUserHasFunds(any(BillingServiceRequest.class))).thenReturn(response);
 
         Boolean reply = validation.startFor(subscription);
 
         assertFalse(reply);
         assertSMSRequest(mobileNumber, errMsg, program);
     }
 
 
     private void assertSMSRequest(String mobileNumber, String errorMsg, String program) {
         ArgumentCaptor<SMSServiceRequest> captor = ArgumentCaptor.forClass(SMSServiceRequest.class);
         verify(smsService).send(captor.capture());
         SMSServiceRequest captured = captor.getValue();
 
         assertEquals(errorMsg, captured.getMessage());
         assertEquals(mobileNumber, captured.getMobileNumber());
         assertEquals(program, captured.programName());
     }
 
     private void setupSubscriptionMock(String mobileNumber, String program, ProgramType programType, Subscription subscription) {
         when(programType.getProgramName()).thenReturn(program);
         when(subscription.subscriberNumber()).thenReturn(mobileNumber);
         when(subscription.getProgramType()).thenReturn(programType);
     }
 
 
 }
