 package org.motechproject.ananya.kilkari.web.controller;
 
 import com.google.gson.Gson;
 import org.hamcrest.BaseMatcher;
 import org.hamcrest.Description;
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.ArgumentCaptor;
 import org.mockito.Mock;
 import org.motechproject.ananya.kilkari.domain.Subscription;
 import org.motechproject.ananya.kilkari.domain.SubscriptionPack;
 import org.motechproject.ananya.kilkari.domain.SubscriptionRequest;
 import org.motechproject.ananya.kilkari.domain.SubscriptionStatus;
 import org.motechproject.ananya.kilkari.exceptions.ValidationException;
 import org.motechproject.ananya.kilkari.service.SubscriptionService;
 import org.motechproject.ananya.kilkari.web.controller.requests.CallbackRequest;
 import org.motechproject.ananya.kilkari.web.domain.CallbackAction;
 import org.motechproject.ananya.kilkari.web.domain.CallbackStatus;
 import org.motechproject.ananya.kilkari.web.interceptors.KilkariChannelInterceptor;
 import org.motechproject.ananya.kilkari.web.response.BaseResponse;
 import org.motechproject.ananya.kilkari.web.response.SubscriberResponse;
 import org.motechproject.ananya.kilkari.web.response.SubscriptionDetails;
 import org.motechproject.ananya.kilkari.web.services.SubscriptionPublisher;
 import org.springframework.http.MediaType;
 import org.springframework.test.web.server.setup.MockMvcBuilders;
 
 import java.util.ArrayList;
 
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.get;
 import static org.springframework.test.web.server.request.MockMvcRequestBuilders.put;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.content;
 import static org.springframework.test.web.server.result.MockMvcResultMatchers.status;
 
 public class SubscriptionControllerTest {
     private SubscriptionController subscriptionController;
 
     @Mock
     private SubscriptionService subscriptionService;
 
     @Mock
     private Subscription mockedSubscription;
 
     @Mock
     private SubscriptionPublisher subscriptionPublisher;
 
     @Before
     public void setUp() {
         initMocks(this);
         subscriptionController = new SubscriptionController(subscriptionService, subscriptionPublisher);
     }
 
     @Test
     public void shouldGetSubscriptionsForGivenMsisdnForChannelIvr() throws Exception {
         String msisdn = "1234567890";
         String channel = "ivr";
 
         mockSubscription(msisdn);
         ArrayList<Subscription> subscriptions = new ArrayList<>();
         subscriptions.add(mockedSubscription);
         when(subscriptionService.findByMsisdn(msisdn)).thenReturn(subscriptions);
 
         MockMvcBuilders.standaloneSetup(subscriptionController).addInterceptors(new KilkariChannelInterceptor()).build()
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                .andExpect(content().type("application/json;charset=UTF-8"))
                 .andExpect(content().string(subscriberResponseMatcherWithSubscriptions()));
     }
 
     @Test
     public void shouldGetSubscriptionsForGivenMsisdnForChannelOtherThanIvr() throws Exception {
         String msisdn = "1234567890";
         String channel = "not-ivr";
 
         mockSubscription(msisdn);
         ArrayList<Subscription> subscriptions = new ArrayList<>();
         subscriptions.add(mockedSubscription);
         when(subscriptionService.findByMsisdn(msisdn)).thenReturn(subscriptions);
 
         MockMvcBuilders.standaloneSetup(subscriptionController).addInterceptors(new KilkariChannelInterceptor()).build()
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                 .andExpect(content().type("application/json;charset=UTF-8"))
                 .andExpect(content().string(subscriberResponseMatcherWithSubscriptions()));
     }
 
     @Test
     public void shouldGetEmptySubscriptionResponseIfThereAreNoSubscriptionsForAGivenMsisdn() throws Exception {
         String msisdn = "1234567890";
         String channel = "not-ivr";
 
         when(subscriptionService.findByMsisdn(msisdn)).thenReturn(null);
 
         MockMvcBuilders.standaloneSetup(subscriptionController).addInterceptors(new KilkariChannelInterceptor()).build()
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                 .andExpect(content().type("application/json;charset=UTF-8"))
                 .andExpect(content().string(subscriberResponseMatcherWithNoSubscriptions("SUCCESS", "Subscriber details successfully fetched")));
     }
 
     @Test
     public void shouldReturnErrorResponseForInvalidMsisdnNumber() throws Exception {
         String msisdn = "12345";
         String channel = "ivr";
 
         when(subscriptionService.findByMsisdn(msisdn)).thenThrow(new ValidationException("Invalid Msisdn"));
 
         MockMvcBuilders.standaloneSetup(subscriptionController).addInterceptors(new KilkariChannelInterceptor()).build()
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                .andExpect(content().type("application/json;charset=UTF-8"))
                 .andExpect(content().string(subscriberResponseMatcherWithNoSubscriptions("ERROR", "Invalid Msisdn")));
     }
 
     @Test
     public void shouldReturnErrorResponseForNonNumericMsisdnNumber() throws Exception {
         String msisdn = "123456789a";
         String channel = "ivr";
 
         when(subscriptionService.findByMsisdn(msisdn)).thenThrow(new ValidationException("Invalid Msisdn"));
 
         MockMvcBuilders.standaloneSetup(subscriptionController).addInterceptors(new KilkariChannelInterceptor()).build()
                 .perform(get("/subscriber").param("msisdn", msisdn).param("channel", channel))
                 .andExpect(status().isOk())
                .andExpect(content().type("application/json;charset=UTF-8"))
                 .andExpect(content().string(subscriberResponseMatcherWithNoSubscriptions("ERROR", "Invalid Msisdn")));
     }
 
     @Test
     public void shouldCreateNewSubscriptionEvent() throws Exception {
         String msisdn = "1234567890";
         String channel = "ivr";
         String pack = "twelve-months";
 
         MockMvcBuilders.standaloneSetup(subscriptionController).addInterceptors(new KilkariChannelInterceptor()).build()
                 .perform(get("/subscription").param("msisdn", msisdn).param("channel", channel).param("pack", pack))
                 .andExpect(status().isOk())
                .andExpect(content().type("application/json;charset=UTF-8"))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Subscription request submitted successfully")));
 
         ArgumentCaptor<SubscriptionRequest> subscriptionRequestArgumentCaptor = ArgumentCaptor.forClass(SubscriptionRequest.class);
         verify(subscriptionPublisher).createSubscription(subscriptionRequestArgumentCaptor.capture());
         SubscriptionRequest subscriptionRequest = subscriptionRequestArgumentCaptor.getValue();
 
         assertEquals(msisdn, subscriptionRequest.getMsisdn());
         assertEquals(pack, subscriptionRequest.getPack());
         assertEquals(channel, subscriptionRequest.getChannel());
     }
 
     @Test
     public void shouldActivateTheSubscriptionWhenCallBackUrlIsInvokedWithSuccessStatusForActivationRequest() throws Exception {
         String subscriptionId = "abcd1234";
         CallbackRequest callbackRequest = new CallbackRequest();
         callbackRequest.setMsisdn("msisdn");
         callbackRequest.setAction(CallbackAction.ACT);
         callbackRequest.setStatus(CallbackStatus.SUCCESS);
         callbackRequest.setReason("reason");
         callbackRequest.setOperator("operator");
         callbackRequest.setGraceCount("2");
         byte[] requestBody = toJson(callbackRequest).getBytes();
 
         MockMvcBuilders.standaloneSetup(subscriptionController).build()
                 .perform(put("/subscription/" + subscriptionId)
                 .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type("application/json;charset=UTF-8"))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Callback request processed successfully")));
 
         verify(subscriptionService).activate(subscriptionId);
     }
 
     @Test
     public void shouldMakeActivationFailWhenCallBackUrlIsInvokedWithNonSuccessStatusForActivationRequest() throws Exception {
         String subscriptionId = "abcd1234";
         CallbackRequest callbackRequest = new CallbackRequest();
         callbackRequest.setMsisdn("msisdn");
         callbackRequest.setAction(CallbackAction.ACT);
         callbackRequest.setStatus(CallbackStatus.FAILURE);
         callbackRequest.setReason("reason");
         callbackRequest.setOperator("operator");
         callbackRequest.setGraceCount("2");
         byte[] requestBody = toJson(callbackRequest).getBytes();
 
         MockMvcBuilders.standaloneSetup(subscriptionController).build()
                 .perform(put("/subscription/" + subscriptionId)
                         .body(requestBody).contentType(MediaType.APPLICATION_JSON))
                 .andExpect(status().isOk())
                 .andExpect(content().type("application/json;charset=UTF-8"))
                 .andExpect(content().string(baseResponseMatcher("SUCCESS", "Callback request processed successfully")));
 
         verify(subscriptionService).updateSubscriptionStatus(subscriptionId, SubscriptionStatus.ACTIVATION_FAILED);
     }
 
     private void mockSubscription(String msisdn) {
         when(mockedSubscription.getMsisdn()).thenReturn(msisdn);
         when(mockedSubscription.getPack()).thenReturn(SubscriptionPack.FIFTEEN_MONTHS);
         when(mockedSubscription.getStatus()).thenReturn(SubscriptionStatus.NEW);
         when(mockedSubscription.getSubscriptionId()).thenReturn("subscription-id");
     }
 
     private BaseMatcher<String> baseResponseMatcher(final String status, final String description) {
         return new BaseMatcher<String>() {
             @Override
             public boolean matches(Object o) {
                 return assertBaseResponse((String) o, status, description);
             }
 
             @Override
             public void describeTo(Description matcherDescription) {
             }
         };
     }
 
     private BaseMatcher<String> subscriberResponseMatcherWithNoSubscriptions(final String status, final String description) {
         return new BaseMatcher<String>() {
             @Override
             public boolean matches(Object o) {
                 return assertSubscriberResponseWithNoSubscriptions((String) o, status, description);
             }
 
             @Override
             public void describeTo(Description matcherDescription) {
                 //To change body of implemented methods use File | Settings | File Templates.
             }
         };
     }
 
     private BaseMatcher<String> subscriberResponseMatcherWithSubscriptions() {
         return new BaseMatcher<String>() {
             @Override
             public boolean matches(Object o) {
                 return assertSubscriberResponse((String) o);
             }
 
             @Override
             public void describeTo(Description description) {
             }
         };
     }
 
     private boolean assertBaseResponse(String jsonContent, String status, String description) {
         BaseResponse baseResponse = fromJson(jsonContent.replace("var response = ", ""), BaseResponse.class);
 
         return baseResponse.getStatus().equals(status)
                 && baseResponse.getDescription().equals(description);
     }
 
     private boolean assertSubscriberResponse(String jsonContent) {
         SubscriberResponse subscriberResponse = fromJson(jsonContent.replace("var response = ", ""), SubscriberResponse.class);
         SubscriptionDetails subscriptionDetails = subscriberResponse.getSubscriptionDetails().get(0);
 
         return subscriberResponse.getStatus().equals("SUCCESS")
                 && subscriberResponse.getDescription().equals("Subscriber details successfully fetched")
                 && subscriptionDetails.getPack().equals(mockedSubscription.getPack().name())
                 && subscriptionDetails.getStatus().equals(mockedSubscription.getStatus().name())
                 && subscriptionDetails.getSubscriptionId().equals(mockedSubscription.getSubscriptionId());
     }
 
     private boolean assertSubscriberResponseWithNoSubscriptions(String jsonContent, String status, String description) {
         SubscriberResponse subscriberResponse = fromJson(jsonContent.replace("var response = ", ""), SubscriberResponse.class);
 
         return subscriberResponse.getStatus().equals(status)
                 && subscriberResponse.getDescription().equals(description)
                 && subscriberResponse.getSubscriptionDetails().size() == 0;
     }
 
     private String toJson(Object objectToSerialize) {
         Gson gson = new Gson();
         return gson.toJson(objectToSerialize);
     }
 
     private <T> T fromJson(String jsonString, Class<T> subscriberResponseClass) {
         Gson gson = new Gson();
         return gson.fromJson(jsonString, subscriberResponseClass);
     }
 }
