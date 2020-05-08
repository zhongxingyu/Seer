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
 import org.motechproject.ananya.kilkari.builder.SubscriptionRequestBuilder;
 import org.motechproject.ananya.kilkari.domain.*;
 import org.motechproject.ananya.kilkari.exceptions.ValidationException;
 import org.motechproject.ananya.kilkari.request.CallbackRequest;
 import org.motechproject.ananya.kilkari.request.CallbackRequestWrapper;
 import org.motechproject.ananya.kilkari.service.KilkariSubscriptionService;
 import org.motechproject.ananya.kilkari.service.SubscriptionService;
 import org.motechproject.ananya.kilkari.validator.SubscriptionRequestValidator;
 import org.motechproject.ananya.kilkari.web.HttpConstants;
 import org.motechproject.ananya.kilkari.web.TestUtils;
 import org.motechproject.ananya.kilkari.web.contract.response.BaseResponse;
 import org.motechproject.ananya.kilkari.web.contract.response.SubscriberResponse;
 import org.motechproject.ananya.kilkari.web.contract.response.SubscriptionDetails;
 import org.motechproject.ananya.kilkari.web.domain.CallbackRequestValidator;
 import org.springframework.http.MediaType;
 
 import java.util.ArrayList;
 
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
     @Mock
     private Subscription mockedSubscription;
     @Mock
     private KilkariSubscriptionService kilkariSubscriptionService;
     @Mock
     private SubscriptionRequestValidator subscriptionRequestValidator;
     @Rule
     public ExpectedException expectedException = ExpectedException.none();
     @Mock
     private SubscriptionService subscriptionService;
     @Mock
     private CallbackRequestValidator callbackRequestValidator;
 
     private static final String CONTENT_TYPE_JAVASCRIPT = "application/javascript;charset=UTF-8";
     private static final String CONTENT_TYPE_JSON = "application/json;charset=UTF-8";
     private static final String IVR_RESPONSE_PREFIX = "var response = ";
 
     @Before
     public void setUp() {
         initMocks(this);
         subscriptionController = new SubscriptionController(kilkariSubscriptionService, subscriptionRequestValidator, callbackRequestValidator);
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
                 .andExpect(content().type(CONTENT_TYPE_JAVASCRIPT))
                 .andExpect(content().string(subscriberResponseMatcherWithSubscriptions(channel)));
     }
 
     @Test
     public void shouldGetSubscriptionsForGivenMsisdnForChannelOtherThanIvr() throws Exception {
         String msisdn = "1234567890";
         String channel = "call_center";
 
         mockSubscription(msisdn);
         ArrayList<Subscription> subscriptions = new ArrayList<>();
         subscriptions.add(mockedSubscription);
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenReturn(subscriptions);
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                 .andExpect(content().type(CONTENT_TYPE_JSON))
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
         String channel = "call_center";
 
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenReturn(null);
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                 .andExpect(content().type(CONTENT_TYPE_JSON))
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
                 .andExpect(content().type(CONTENT_TYPE_JAVASCRIPT))
                 .andExpect(content().string(errorResponseMatcherForInvalidMsisdn(channel)));
 
     }
 
     @Test
     public void shouldReturnErrorResponseForInvalidMsisdnNumberOtherThanIvr() throws Exception {
         String msisdn = "12345";
         String channel = "call_center";
 
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenThrow(new ValidationException("Invalid Msisdn"));
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().is(400))
                 .andExpect(content().type(CONTENT_TYPE_JSON))
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
                 .andExpect(content().type(CONTENT_TYPE_JAVASCRIPT))
                 .andExpect(content().string(errorResponseMatcherForRuntimeException(channel)));
     }
 
     @Test
     public void shouldReturnCorrectErrorResponseForRuntimeExceptionForOtherThanIvr() throws Exception {
         String msisdn = "1234567890";
         String channel = "call_center";
 
         when(kilkariSubscriptionService.findByMsisdn(msisdn)).thenThrow(new RuntimeException("runtime exception"));
 
         mockMvc(subscriptionController)
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().is(500))
                 .andExpect(content().type(CONTENT_TYPE_JSON))
                 .andExpect(content().string(errorResponseMatcherForRuntimeException(channel)));
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
                 .andExpect(content().type(CONTENT_TYPE_JAVASCRIPT))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Subscription request submitted successfully")));
 
         ArgumentCaptor<SubscriptionRequest> subscriptionRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionRequest.class);
         verify(kilkariSubscriptionService).createSubscription(subscriptionRequestArgumentCaptor.capture());
         SubscriptionRequest subscriptionRequest = subscriptionRequestArgumentCaptor.getValue();
 
         assertEquals(msisdn, subscriptionRequest.getMsisdn());
         assertEquals(pack, subscriptionRequest.getPack());
         assertEquals(channel, subscriptionRequest.getChannel());
 
         assertCreatedAt(beforeCreate, subscriptionRequest);
     }
 
     @Test
     public void shouldCreateNewSubscriptionEventForCC() throws Exception {
         DateTime createdAt = DateTime.now();
 
         SubscriptionRequest expectedRequest = new SubscriptionRequestBuilder().withDefaults().withCreatedAt(createdAt).build();
 
         mockMvc(subscriptionController)
                 .perform(post("/subscription").body(TestUtils.toJson(expectedRequest).getBytes()).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type(CONTENT_TYPE_JSON))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Subscription request submitted successfully")));
 
         ArgumentCaptor<SubscriptionRequest> subscriptionRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionRequest.class);
         verify(kilkariSubscriptionService).createSubscription(subscriptionRequestArgumentCaptor.capture());
         SubscriptionRequest subscriptionRequest = subscriptionRequestArgumentCaptor.getValue();
 
         assertTrue(expectedRequest.equals(subscriptionRequest));
         assertCreatedAt(createdAt, subscriptionRequest);
     }
 
     @Test
     public void shouldGiveAnErrorMessageWhenCallBackRequestIsInvalid() throws Exception {
         String subscriptionId = "abcd1234";
         CallbackRequest callbackRequest = new CallbackRequest();
         callbackRequest.setMsisdn("invalidMsisdn");
         callbackRequest.setAction("invalidAction");
         callbackRequest.setStatus("invalidStatus");
         callbackRequest.setOperator("invalidOperator");
 
         byte[] requestBody = TestUtils.toJson(callbackRequest).getBytes();
 
         mockMvc(subscriptionController)
                 .perform(put("/subscription/" + subscriptionId)
                         .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type(CONTENT_TYPE_JSON))
                 .andExpect(content().string(baseResponseMatcher("ERROR", "Callback Request Invalid: Invalid callbackAction invalidAction,Invalid callbackStatus invalidStatus,Invalid msisdn invalidMsisdn,Invalid operator invalidOperator")));
 
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
         when(callbackRequestValidator.validate(any(CallbackRequestWrapper.class))).thenReturn(new ArrayList<String>());
 
         mockMvc(subscriptionController)
                 .perform(put("/subscription/" + subscriptionId)
                         .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type(CONTENT_TYPE_JSON))
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
     public void shouldValidateSubscriptionRequestIfCreateRequestIsCalledFromCallCenter() {
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withChannel(Channel.CALL_CENTER.toString()).build();
 
         subscriptionController.createSubscription(subscriptionRequest);
 
         verify(subscriptionRequestValidator).validate(subscriptionRequest);
     }
 
     @Test
     public void shouldNotValidateSubscriptionRequest_ForIVRChannel() {
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withChannel(Channel.IVR.toString()).build();
 
         subscriptionController.createSubscription(subscriptionRequest);
 
         verify(subscriptionRequestValidator, never()).validate(subscriptionRequest);
     }
 
     @Test
     public void shouldValidateForChannel() {
         expectedException.expect(ValidationException.class);
         expectedException.expectMessage("Invalid channel xyz");
 
         SubscriptionRequest subscriptionRequest = new SubscriptionRequestBuilder().withDefaults().withChannel("xyz").build();
 
         subscriptionController.createSubscription(subscriptionRequest);
     }
 
     private void mockSubscription(String msisdn) {
         when(mockedSubscription.getMsisdn()).thenReturn(msisdn);
         when(mockedSubscription.getPack()).thenReturn(SubscriptionPack.FIFTEEN_MONTHS);
         when(mockedSubscription.getStatus()).thenReturn(SubscriptionStatus.NEW);
         when(mockedSubscription.getSubscriptionId()).thenReturn("subscription-id");
     }
 
     private void assertCreatedAt(DateTime beforeCreate, SubscriptionRequest subscriptionRequest) {
         DateTime createdAt = subscriptionRequest.getCreatedAt();
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
 
     private boolean assertErrorResponseForRuntimeException(String jsonContent, String channel) {
         jsonContent = performIVRChannelValidationAndCleanup(jsonContent, channel);
 
         BaseResponse baseResponse = TestUtils.fromJson(jsonContent, BaseResponse.class);
 
         return baseResponse.isError() &&
                 baseResponse.getDescription().equals("runtime exception");
     }
 
     private boolean assertSubscriberResponse(String jsonContent, String channel) {
         jsonContent = performIVRChannelValidationAndCleanup(jsonContent, channel);
 
         SubscriberResponse subscriberResponse = TestUtils.fromJson(jsonContent, SubscriberResponse.class);
         SubscriptionDetails subscriptionDetails = subscriberResponse.getSubscriptionDetails().get(0);
 
         return subscriptionDetails.getPack().equals(mockedSubscription.getPack().name())
                 && subscriptionDetails.getStatus().equals(mockedSubscription.getStatus().name())
                 && subscriptionDetails.getSubscriptionId().equals(mockedSubscription.getSubscriptionId());
     }
 
     private boolean assertSubscriberResponseWithNoSubscriptions(String jsonContent, String channel) {
         jsonContent = performIVRChannelValidationAndCleanup(jsonContent, channel);
 
         SubscriberResponse subscriberResponse = TestUtils.fromJson(jsonContent, SubscriberResponse.class);
 
         return subscriberResponse.getSubscriptionDetails().size() == 0;
     }
 }
