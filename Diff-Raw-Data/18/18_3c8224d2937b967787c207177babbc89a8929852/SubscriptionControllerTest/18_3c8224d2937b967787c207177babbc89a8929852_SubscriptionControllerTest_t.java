 package org.motechproject.ananya.kilkari.web.controller;
 
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.hamcrest.Matcher;
 import org.joda.time.DateTime;
 import org.junit.Before;
 import org.junit.Rule;
 import org.junit.Test;
 import org.junit.rules.ExpectedException;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.builder.SubscriptionWebRequestBuilder;
 import org.motechproject.ananya.kilkari.domain.CallbackAction;
 import org.motechproject.ananya.kilkari.domain.CallbackStatus;
 import org.motechproject.ananya.kilkari.obd.domain.Channel;
 import org.motechproject.ananya.kilkari.obd.service.validator.Errors;
 import org.motechproject.ananya.kilkari.request.*;
 import org.motechproject.ananya.kilkari.service.KilkariSubscriptionService;
 import org.motechproject.ananya.kilkari.subscription.domain.*;
 import org.motechproject.ananya.kilkari.subscription.exceptions.ValidationException;
 import org.motechproject.ananya.kilkari.subscription.service.SubscriptionService;
 import org.motechproject.ananya.kilkari.web.HttpConstants;
 import org.motechproject.ananya.kilkari.web.HttpHeaders;
 import org.motechproject.ananya.kilkari.web.TestUtils;
 import org.motechproject.ananya.kilkari.web.mapper.SubscriptionDetailsMapper;
 import org.motechproject.ananya.kilkari.web.response.BaseResponse;
 import org.motechproject.ananya.kilkari.web.response.SubscriptionDetails;
 import org.motechproject.ananya.kilkari.web.response.SubscriptionWebResponse;
 import org.motechproject.ananya.kilkari.web.validators.CallbackRequestValidator;
 import org.motechproject.web.context.HttpThreadContext;
 import org.springframework.http.MediaType;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import static org.junit.Assert.*;
 import static org.mockito.Mockito.*;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.motechproject.ananya.kilkari.web.MVCTestUtils.mockMvc;
 import static org.motechproject.ananya.kilkari.web.controller.ResponseMatchers.baseResponseMatcher;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.*;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
 
 
 public class SubscriptionControllerTest {
     private SubscriptionController subscriptionController;
     @Rule
     public ExpectedException expectedException = ExpectedException.none();
 
     @Mock
     private Subscription mockedSubscription;
     @Mock
     private KilkariSubscriptionService kilkariSubscriptionService;
     @Mock
     private SubscriptionService subscriptionService;
     @Mock
     private CallbackRequestValidator callbackRequestValidator;
     @Mock
     private SubscriptionDetailsMapper mockedSubscriptionDetailsMapper;
 
     private static final String IVR_RESPONSE_PREFIX = "var response = ";
 
     @Before
     public void setUp() {
         initMocks(this);
         subscriptionController = new SubscriptionController(kilkariSubscriptionService, callbackRequestValidator, mockedSubscriptionDetailsMapper);
     }
 
     @Test
     public void shouldGetSubscriptionsForGivenMsisdnForChannelIvr() throws Exception {
         String msisdn = "1234567890";
         String channel = "ivr";
 
         mockSubscription(msisdn);
         ArrayList<Subscription> subscriptions = new ArrayList<>();
         subscriptions.add(mockedSubscription);
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenReturn(subscriptions);
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JAVASCRIPT))
                 .andExpect(content().string(subscriberResponseMatcherWithSubscriptions(channel)));
     }
 
     @Test
     public void shouldGetSubscriptionsForGivenMsisdnForChannelOtherThanIvr() throws Exception {
         String msisdn = "1234567890";
         String channel = "CONTACT_CENTER";
 
         mockSubscription(msisdn);
         ArrayList<Subscription> subscriptions = new ArrayList<>();
         subscriptions.add(mockedSubscription);
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenReturn(subscriptions);
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(subscriberResponseMatcherWithSubscriptions(channel)));
     }
 
     @Test
     public void shouldGetEmptySubscriptionResponseIfThereAreNoSubscriptionsForAGivenMsisdnForIvr() throws Exception {
         String msisdn = "1234567890";
         String channel = "ivr";
 
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenReturn(null);
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpConstants.IVR.getResponseContentType()))
                 .andExpect(content().string(subscriberResponseMatcherWithNoSubscriptions(channel)));
     }
 
     @Test
     public void shouldGetEmptySubscriptionResponseIfThereAreNoSubscriptionsForAGivenMsisdnOtherThanIvr() throws Exception {
         String msisdn = "1234567890";
         String channel = "CONTACT_CENTER";
 
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenReturn(null);
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(subscriberResponseMatcherWithNoSubscriptions(channel)));
     }
 
     @Test
     public void shouldReturnErrorResponseForInvalidMsisdnNumberForIvr() throws Exception {
         String msisdn = "12345";
         String channel = "ivr";
 
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenThrow(new ValidationException("Invalid Msisdn"));
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JAVASCRIPT))
                 .andExpect(content().string(errorResponseMatcherForInvalidMsisdn(channel)));
 
     }
 
     @Test
     public void shouldReturnErrorResponseForInvalidMsisdnNumberOtherThanIvr() throws Exception {
         String msisdn = "12345";
         String channel = "CONTACT_CENTER";
 
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenThrow(new ValidationException("Invalid Msisdn"));
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().is(400))
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(errorResponseMatcherForInvalidMsisdn(channel)));
     }
 
     @Test
     public void shouldReturnCorrectErrorResponseForRuntimeExceptionForIvr() throws Exception {
         String msisdn = "1234567890";
         String channel = "ivr";
 
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenThrow(new RuntimeException("runtime exception"));
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JAVASCRIPT))
                 .andExpect(content().string(errorResponseMatcherForRuntimeException(channel)));
     }
 
     @Test
     public void shouldReturnCorrectErrorResponseForRuntimeExceptionForOtherThanIvr() throws Exception {
         String msisdn = "1234567890";
         String channel = "CONTACT_CENTER";
 
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenThrow(new RuntimeException("runtime exception"));
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().is(500))
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(errorResponseMatcherForRuntimeException(channel)));
     }
 
     @Test
     public void shouldValidateChannelForGetSubscriptions() throws Exception {
         String msisdn = "1234567890";
         String channel = "invalidChannel";
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().is(400))
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(errorResponseMatcherForInvalidChannel(channel)));
     }
 
     @Test
     public void shouldCreateNewSubscriptionEvent() throws Exception {
         String msisdn = "1234567890";
         String channel = "ivr";
         String pack = "twelve-months";
         DateTime beforeCreate = DateTime.now();
 
         mockMvc(subscriptionController)
                 .perform(get("/subscription").param("msisdn", msisdn).param("channel", channel).param("pack", pack))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JAVASCRIPT))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Subscription request submitted successfully")));
 
         ArgumentCaptor<SubscriptionWebRequest> subscriptionRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionWebRequest.class);
         verify(kilkariSubscriptionService).createSubscriptionAsync(subscriptionRequestArgumentCaptor.capture());
         SubscriptionWebRequest subscriptionWebRequest = subscriptionRequestArgumentCaptor.getValue();
 
         assertEquals(msisdn, subscriptionWebRequest.getMsisdn());
         assertEquals(pack, subscriptionWebRequest.getPack());
         assertEquals(channel, subscriptionWebRequest.getChannel());
 
         assertCreatedAt(beforeCreate, subscriptionWebRequest);
     }
 
     @Test
     public void shouldCreateNewSubscriptionEventForCC() throws Exception {
         DateTime createdAt = DateTime.now();
 
         SubscriptionWebRequest expectedWebRequest = new SubscriptionWebRequestBuilder().withDefaults().withCreatedAt(createdAt).build();
 
         mockMvc(subscriptionController)
                 .perform(post("/subscription")
                         .param("channel", Channel.CONTACT_CENTER.toString())
                         .body(TestUtils.toJson(expectedWebRequest).getBytes()).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Subscription request submitted successfully")));
 
         ArgumentCaptor<SubscriptionWebRequest> subscriptionRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionWebRequest.class);
         verify(kilkariSubscriptionService).createSubscription(subscriptionRequestArgumentCaptor.capture());
         SubscriptionWebRequest subscriptionWebRequest = subscriptionRequestArgumentCaptor.getValue();
 
         assertTrue(expectedWebRequest.equals(subscriptionWebRequest));
         assertCreatedAt(createdAt, subscriptionWebRequest);
     }
 
     @Test
     public void shouldGiveAnErrorMessageWhenCallBackRequestIsInvalid() throws Exception {
         Errors errorsMessages = new Errors() {
             {
                 add("Invalid msisdn invalidMsisdn");
                 add("Invalid operator invalidOperator");
             }
         };
 
         byte[] requestBody = TestUtils.toJson(new CallbackRequest()).getBytes();
 
         Errors errors = new Errors();
         errors.addAll(errorsMessages);
         when(callbackRequestValidator.validate(any(CallbackRequestWrapper.class))).thenReturn(errors);
 
         mockMvc(subscriptionController)
                 .perform(put("/subscription/abcd1234")
                         .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isBadRequest())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                .andExpect(content().string(baseResponseMatcher("FAILED", "Invalid msisdn invalidMsisdn,Invalid operator invalidOperator")));
 
 
         verifyZeroInteractions(kilkariSubscriptionService);
     }
 
     @Test
     public void shouldPublishTheCallbackRequestIfValidationSucceeds() throws Exception {
         String subscriptionId = "abcd1234";
         CallbackRequest callbackRequest = new CallbackRequest();
         callbackRequest.setMsisdn("1234567890");
         callbackRequest.setAction(CallbackAction.ACT.name());
         callbackRequest.setStatus(CallbackStatus.SUCCESS.name());
         callbackRequest.setReason("reason");
         callbackRequest.setOperator(Operator.AIRTEL.name());
         callbackRequest.setGraceCount("2");
         byte[] requestBody = TestUtils.toJson(callbackRequest).getBytes();
         when(callbackRequestValidator.validate(any(CallbackRequestWrapper.class))).thenReturn(new Errors());
 
         mockMvc(subscriptionController)
                 .perform(put("/subscription/" + subscriptionId)
                         .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Callback request processed successfully")));
 
         ArgumentCaptor<CallbackRequestWrapper> callbackRequestWrapperArgumentCaptor = ArgumentCaptor.forClass(CallbackRequestWrapper.class);
         verify(kilkariSubscriptionService).processCallbackRequest(callbackRequestWrapperArgumentCaptor.capture());
         CallbackRequestWrapper callbackRequestWrapper = callbackRequestWrapperArgumentCaptor.getValue();
 
         assertEquals(subscriptionId, callbackRequestWrapper.getSubscriptionId());
         assertEquals(CallbackAction.ACT.name(), callbackRequestWrapper.getAction());
         assertEquals(CallbackStatus.SUCCESS.name(), callbackRequestWrapper.getStatus());
         assertNotNull(callbackRequestWrapper.getCreatedAt());
     }
 
     @Test
     public void shouldValidateChannelOnSubscriptionCreationRequest_FromCC() {
         SubscriptionWebRequest subscriptionWebRequest = mock(SubscriptionWebRequest.class);
         when(subscriptionWebRequest.getChannel()).thenReturn("CONTACT_CENTER");
 
         subscriptionController.createSubscription(subscriptionWebRequest, "CONTACT_CENTER");
 
         verify(subscriptionWebRequest).validateChannel();
     }
 
     @Test
     public void shouldValidateChannelOnSubscriptionCreationRequest_FromIVR() {
         SubscriptionWebRequest subscriptionWebRequest = mock(SubscriptionWebRequest.class);
         when(subscriptionWebRequest.getChannel()).thenReturn("ivr");
 
         subscriptionController.createSubscriptionForIVR(subscriptionWebRequest);
 
         verify(subscriptionWebRequest).validateChannel();
     }
 
     @Test
     public void shouldValidateForChannel() {
         expectedException.expect(ValidationException.class);
         expectedException.expectMessage("Invalid channel xyz");
 
         SubscriptionWebRequest subscriptionWebRequest = new SubscriptionWebRequestBuilder().withDefaults().withChannel("xyz").build();
 
         subscriptionController.createSubscription(subscriptionWebRequest, "xyz");
     }
 
     @Test
     public void shouldUnsubscribeAUserGivenValidDetails() throws Exception {
         String subscriptionId = "abcd1234";
         UnSubscriptionWebRequest unSubscriptionWebRequest = new UnSubscriptionWebRequest();
         unSubscriptionWebRequest.setReason("reason");
         byte[] requestBody = TestUtils.toJson(unSubscriptionWebRequest).getBytes();
 
         mockMvc(subscriptionController)
                 .perform(delete("/subscription/" + subscriptionId)
                         .param("channel", Channel.CONTACT_CENTER.toString())
                         .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Subscription unsubscribed successfully")));
 
         ArgumentCaptor<UnSubscriptionWebRequest> unsubscriptionRequestArgumentCaptor = ArgumentCaptor.forClass(UnSubscriptionWebRequest.class);
         ArgumentCaptor<String> stringArgumentCaptor = ArgumentCaptor.forClass(String.class);
         verify(kilkariSubscriptionService).requestUnsubscription(stringArgumentCaptor.capture(), unsubscriptionRequestArgumentCaptor.capture());
 
         UnSubscriptionWebRequest actualUnSubscriptionWebRequest = unsubscriptionRequestArgumentCaptor.getValue();
         String actualSubscriptionId = stringArgumentCaptor.getValue();
 
         assertEquals(subscriptionId, actualSubscriptionId);
         assertEquals(Channel.CONTACT_CENTER.name(), actualUnSubscriptionWebRequest.getChannel());
     }
 
     @Test
     public void shouldValidateUnsubscriptionRequestDetails() throws Exception {
         String subscriptionId = "abcd1234";
         UnSubscriptionWebRequest unSubscriptionWebRequest = new UnSubscriptionWebRequest();
         unSubscriptionWebRequest.setReason("reason");
         byte[] requestBody = TestUtils.toJson(unSubscriptionWebRequest).getBytes();
 
         doThrow(new ValidationException("some error description")).when(kilkariSubscriptionService).requestUnsubscription(anyString(), any(UnSubscriptionWebRequest.class));
 
         mockMvc(subscriptionController)
                 .perform(delete("/subscription/" + subscriptionId)
                         .param("channel", Channel.CONTACT_CENTER.toString())
                         .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isBadRequest())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                .andExpect(content().string(baseResponseMatcher("FAILED", "some error description")));
     }
 
     @Test
     public void shouldProcessValidCampaignChangeRequest() throws Exception {
         CampaignChangeRequest campaignChangeRequest = new CampaignChangeRequest();
         String subscriptionId = "subscriptionId";
         String reason = "INFANT_DEATH";
         campaignChangeRequest.setReason(reason);
         byte[] requestBody = TestUtils.toJson(campaignChangeRequest).getBytes();
 
         mockMvc(subscriptionController)
                 .perform(post("/subscription/" + subscriptionId + "/changecampaign")
                         .param("channel", Channel.CONTACT_CENTER.toString())
                         .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Campaign Change successfully completed")));
 
         ArgumentCaptor<CampaignChangeRequest> campaignChangeRequestArgumentCaptor = ArgumentCaptor.forClass(CampaignChangeRequest.class);
         ArgumentCaptor<String> subscriptionIdCaptor = ArgumentCaptor.forClass(String.class);
         verify(kilkariSubscriptionService).processCampaignChange(campaignChangeRequestArgumentCaptor.capture(), subscriptionIdCaptor.capture());
         CampaignChangeRequest changeRequest = campaignChangeRequestArgumentCaptor.getValue();
 
         assertEquals(subscriptionId, subscriptionIdCaptor.getValue());
         assertEquals(reason, changeRequest.getReason());
     }
 
     @Test
     public void shouldUpdateSubscriberDetails() throws Exception {
         SubscriberWebRequest subscriberWebRequest = new SubscriberWebRequest();
         byte[] requestBody = TestUtils.toJson(subscriberWebRequest).getBytes();
         String subscriptionId = "subscription-id";
 
         mockMvc(subscriptionController)
                 .perform(put("/subscriber/" + subscriptionId)
                         .param("channel", Channel.CONTACT_CENTER.toString())
                         .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Subscriber Update request submitted successfully")));
 
         subscriberWebRequest.setChannel(Channel.CONTACT_CENTER.name());
         verify(kilkariSubscriptionService).updateSubscriberDetails(subscriberWebRequest, subscriptionId);
     }
 
     @Test
     public void shouldChangePackForTheGivenSubscriber() throws Exception {
         String subscriptionId = "abcd1234";
         String channel = Channel.CONTACT_CENTER.name();
         String pack = SubscriptionPack.BARI_KILKARI.name();
         ChangeSubscriptionWebRequest changeSubscriptionWebRequest = new ChangeSubscriptionWebRequest();
         changeSubscriptionWebRequest.setPack(pack);
 
         byte[] requestBody = TestUtils.toJson(changeSubscriptionWebRequest).getBytes();
 
         mockMvc(subscriptionController)
                 .perform(put("/subscription/" + subscriptionId + "/changesubscription")
                         .param("channel", Channel.CONTACT_CENTER.toString())
                         .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Change Subscription successfully completed")));
 
         ArgumentCaptor<ChangeSubscriptionWebRequest> changePackWebRequestArgumentCaptor = ArgumentCaptor.forClass(ChangeSubscriptionWebRequest.class);
         ArgumentCaptor<String> subscriptionIdCaptor = ArgumentCaptor.forClass(String.class);
         verify(kilkariSubscriptionService).changeSubscription(changePackWebRequestArgumentCaptor.capture(), subscriptionIdCaptor.capture());
         ChangeSubscriptionWebRequest request = changePackWebRequestArgumentCaptor.getValue();
 
         assertEquals(subscriptionId, subscriptionIdCaptor.getValue());
         assertEquals(channel, request.getChannel());
         assertEquals(pack, request.getPack());
         assertNotNull(request.getCreatedAt());
     }
 
     private void mockSubscription(String msisdn) {
         String subscriptionId = "subscription-id";
         SubscriptionPack subscriptionPack = SubscriptionPack.BARI_KILKARI;
         SubscriptionStatus subscriptionStatus = SubscriptionStatus.NEW;
 
         when(mockedSubscription.getMsisdn()).thenReturn(msisdn);
         when(mockedSubscription.getPack()).thenReturn(subscriptionPack);
         when(mockedSubscription.getStatus()).thenReturn(subscriptionStatus);
         when(mockedSubscription.getSubscriptionId()).thenReturn(subscriptionId);
 
         when(mockedSubscriptionDetailsMapper.mapFrom(mockedSubscription)).thenReturn(new SubscriptionDetails(subscriptionId, subscriptionPack.name(), subscriptionStatus.name(), null));
     }
 
     private void assertCreatedAt(DateTime beforeCreate, SubscriptionWebRequest subscriptionWebRequest) {
         DateTime createdAt = subscriptionWebRequest.getCreatedAt();
         DateTime afterCreate = DateTime.now();
         assertTrue(createdAt.isEqual(beforeCreate) || createdAt.isAfter(beforeCreate));
         assertTrue(createdAt.isEqual(afterCreate) || createdAt.isBefore(afterCreate));
     }
 
 
     private Matcher<String> errorResponseMatcherForInvalidMsisdn(final String channel) {
         return new BaseMatcher<String>() {
             @Override
             public boolean matches(Object o) {
                 return assertErrorResponseForInvalidMsisdn((String) o, channel);
             }
 
             @Override
             public void describeTo(Description matcherDescription) {
             }
         };
     }
 
     private Matcher<String> errorResponseMatcherForInvalidChannel(final String channel) {
         return new BaseMatcher<String>() {
             @Override
             public boolean matches(Object o) {
                 return assertErrorResponseForInvalidChannel((String) o, channel);
             }
 
             @Override
             public void describeTo(Description matcherDescription) {
             }
         };
     }
 
     private Matcher<String> errorResponseMatcherForRuntimeException(final String channel) {
         return new BaseMatcher<String>() {
             @Override
             public boolean matches(Object o) {
                 return assertErrorResponseForRuntimeException((String) o, channel);
             }
 
             @Override
             public void describeTo(Description matcherDescription) {
             }
         };
     }
 
     private BaseMatcher<String> subscriberResponseMatcherWithNoSubscriptions(final String channel) {
         return new BaseMatcher<String>() {
             @Override
             public boolean matches(Object o) {
                 return assertSubscriberResponseWithNoSubscriptions((String) o, channel);
             }
 
             @Override
             public void describeTo(Description matcherDescription) {
             }
         };
     }
 
     private BaseMatcher<String> subscriberResponseMatcherWithSubscriptions(final String channel) {
         return new BaseMatcher<String>() {
             @Override
             public boolean matches(Object o) {
                 return assertSubscriberResponse((String) o, channel);
             }
 
             @Override
             public void describeTo(Description description) {
             }
         };
     }
 
 
     private String performIVRChannelValidationAndCleanup(String jsonContent, String channel) {
         if (Channel.isIVR(channel)) {
             assertTrue(jsonContent.startsWith(IVR_RESPONSE_PREFIX));
             jsonContent = jsonContent.replace(IVR_RESPONSE_PREFIX, "");
         }
         return jsonContent;
     }
 
     private boolean assertErrorResponseForInvalidMsisdn(String jsonContent, String channel) {
         jsonContent = performIVRChannelValidationAndCleanup(jsonContent, channel);
 
         BaseResponse baseResponse = TestUtils.fromJson(jsonContent, BaseResponse.class);
 
         return baseResponse.isError() &&
                 baseResponse.getDescription().equals("Invalid Msisdn");
     }
 
     private boolean assertErrorResponseForInvalidChannel(String jsonContent, String channel) {
         jsonContent = performIVRChannelValidationAndCleanup(jsonContent, channel);
 
         BaseResponse baseResponse = TestUtils.fromJson(jsonContent, BaseResponse.class);
 
         return baseResponse.isError() &&
                 baseResponse.getDescription().contains("Invalid channel");
     }
 
     private boolean assertErrorResponseForRuntimeException(String jsonContent, String channel) {
         jsonContent = performIVRChannelValidationAndCleanup(jsonContent, channel);
 
         BaseResponse baseResponse = TestUtils.fromJson(jsonContent, BaseResponse.class);
 
         return baseResponse.isError() &&
                 baseResponse.getDescription().equals("runtime exception");
     }
 
     private boolean assertSubscriberResponse(String jsonContent, String channel) {
         jsonContent = performIVRChannelValidationAndCleanup(jsonContent, channel);
 
         SubscriptionWebResponse subscriptionWebResponse = TestUtils.fromJson(jsonContent, SubscriptionWebResponse.class);
         SubscriptionDetails subscriptionDetails = subscriptionWebResponse.getSubscriptionDetails().get(0);
 
         return subscriptionDetails.getPack().equals(mockedSubscription.getPack().name())
                 && subscriptionDetails.getStatus().equals(mockedSubscription.getStatus().name())
                 && subscriptionDetails.getSubscriptionId().equals(mockedSubscription.getSubscriptionId());
     }
 
     private boolean assertSubscriberResponseWithNoSubscriptions(String jsonContent, String channel) {
         jsonContent = performIVRChannelValidationAndCleanup(jsonContent, channel);
 
         SubscriptionWebResponse subscriptionWebResponse = TestUtils.fromJson(jsonContent, SubscriptionWebResponse.class);
 
         return subscriptionWebResponse.getSubscriptionDetails().size() == 0;
     }
 
     @Test
     public void shouldSetIVRAsChannelInHttpThreadContext() throws Exception {
         mockMvc(subscriptionController)
                 .perform(get("/subscription").param("channel", Channel.IVR.toString())).andExpect(status().isOk()).andReturn();
 
         assertEquals(Channel.IVR.toString(), HttpThreadContext.get());
     }
 
     @Test
     public void shouldSetCallCenterAsChannelInHttpThreadContext() throws Exception {
         ChangeMsisdnWebRequest changeMsisdnWebRequest = new ChangeMsisdnWebRequest(
                 "1234567890", "1234567891", Arrays.asList(SubscriptionPack.NANHI_KILKARI.toString()), Channel.CONTACT_CENTER.toString());
 
         mockMvc(subscriptionController).perform(
                 post("/subscription/changemsisdn")
                         .param("channel", Channel.CONTACT_CENTER.toString())
                         .body(TestUtils.toJson(changeMsisdnWebRequest).getBytes())
                         .contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk()).andReturn();
 
         assertEquals(Channel.CONTACT_CENTER.toString(), HttpThreadContext.get());
     }
 
     @Test
     public void shouldProcessChangeMsisdnRequestSuccessfully() throws Exception {
         String oldMsisdn = "1234567890";
         String newMsisdn = "9876543210";
         String channel = Channel.CONTACT_CENTER.name();
 
         ChangeMsisdnWebRequest changeMsisdnWebRequest = new ChangeMsisdnWebRequest();
         changeMsisdnWebRequest.setOldMsisdn(oldMsisdn);
         changeMsisdnWebRequest.setNewMsisdn(newMsisdn);
         ArrayList<String> packs = new ArrayList<>();
         packs.add(SubscriptionPack.BARI_KILKARI.name());
         changeMsisdnWebRequest.setPacks(packs);
         byte[] requestBody = TestUtils.toJson(changeMsisdnWebRequest).getBytes();
 
         mockMvc(subscriptionController)
                 .perform(post("/subscription/changemsisdn")
                         .param("channel", channel)
                         .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type(HttpHeaders.APPLICATION_JSON))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Change Msisdn request submitted successfully")));
 
         ArgumentCaptor<ChangeMsisdnWebRequest> changeMsisdnWebRequestArgumentCaptor = ArgumentCaptor.forClass(ChangeMsisdnWebRequest.class);
         verify(kilkariSubscriptionService).changeMsisdn(changeMsisdnWebRequestArgumentCaptor.capture());
         ChangeMsisdnWebRequest request = changeMsisdnWebRequestArgumentCaptor.getValue();
 
         assertEquals(oldMsisdn, request.getOldMsisdn());
         assertEquals(newMsisdn, request.getNewMsisdn());
         assertEquals(channel, request.getChannel());
     }
 }
