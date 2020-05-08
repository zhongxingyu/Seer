 /*
  * $Id$
  * --------------------------------------------------------------------------------------
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.pubsubhubbub;
 
 import java.io.StringReader;
 import java.net.URI;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.lang.RandomStringUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.NumberUtils;
 import org.mule.api.MuleException;
 import org.mule.api.MuleMessage;
 import org.mule.module.client.MuleClient;
 import org.mule.module.pubsubhubbub.data.DataStore;
 import org.mule.module.pubsubhubbub.data.TopicSubscription;
 import org.mule.tck.DynamicPortTestCase;
 import org.mule.tck.functional.CountdownCallback;
 import org.mule.tck.functional.FunctionalTestComponent;
 import org.mule.transport.http.HttpConstants;
 
 import com.sun.syndication.feed.synd.SyndFeed;
 import com.sun.syndication.io.SyndFeedInput;
 
 public class HubITCase extends DynamicPortTestCase
 {
     private enum Action
     {
         SUBSCRIBE, UNSUBSCRIBE;
 
         String asHubMode()
         {
             return toString().toLowerCase();
         }
     };
 
     private enum Verification
     {
         SYNC("204"), ASYNC("202");
 
         private final String expectedStatusCode;
 
         private Verification(final String expectedStatusCode)
         {
             this.expectedStatusCode = expectedStatusCode;
         }
 
         String asVerify()
         {
             return toString().toLowerCase();
         }
 
         String getExpectedStatusCode()
         {
             return expectedStatusCode;
         }
     }
 
     private static final String TEST_TOPIC = "http://mulesoft.org/fake-topic";
     private static final String DEFAULT_CALLBACK_QUERY = "";
     private static final Map<String, List<String>> DEFAULT_SUBSCRIPTION_PARAMS = Collections.emptyMap();
 
     private MuleClient muleClient;
     private DataStore dataStore;
 
     private FunctionalTestComponent successfullSubscriberFTC;
     private CountdownCallback successfullSubscriberCC;
 
     private FunctionalTestComponent publisherFTC;
     private CountdownCallback publisherCC;
 
     @Override
     protected void doSetUp() throws Exception
     {
         super.doSetUp();
        dataStore = muleContext.getRegistry().lookupObject(HubModule.class).getDataStore();
         muleClient = new MuleClient(muleContext);
         setupSuccessfullSubscriberFTC(1);
         setupPublisherFTC(1);
     }
 
     private void setupSuccessfullSubscriberFTC(final int messagesExpected) throws Exception
     {
         successfullSubscriberFTC = getFunctionalTestComponent("successfullSubscriberCallback");
         successfullSubscriberCC = new CountdownCallback(messagesExpected);
         successfullSubscriberFTC.setEventCallback(successfullSubscriberCC);
     }
 
     private void setupPublisherFTC(final int messagesExpected) throws Exception
     {
         publisherFTC = getFunctionalTestComponent("publisher");
         publisherCC = new CountdownCallback(messagesExpected);
         publisherFTC.setEventCallback(publisherCC);
     }
 
     @Override
     protected int getNumPortsToFind()
     {
         return 3;
     }
 
     @Override
     protected String getConfigResources()
     {
         return "push-tests-config.xml";
     }
 
     public void testBadContentType() throws Exception
     {
         final Map<String, List<String>> subscriptionRequest = new HashMap<String, List<String>>();
         subscriptionRequest.put("hub.mode", Collections.singletonList("subscribe"));
 
         final MuleMessage response = sendRequestToHub(subscriptionRequest, "application/octet-stream");
         assertEquals("400", response.getInboundProperty("http.status"));
         assertEquals("Content type must be: application/x-www-form-urlencoded", response.getPayloadAsString());
     }
 
     public void testUnknownHubMode() throws Exception
     {
         final Map<String, String> subscriptionRequest = new HashMap<String, String>();
         subscriptionRequest.put("hub.mode", "foo");
 
         final MuleMessage response = wrapAndSendRequestToHub(subscriptionRequest);
         assertEquals("400", response.getInboundProperty("http.status"));
         assertEquals("Unsupported hub mode: foo", response.getPayloadAsString());
     }
 
     public void testWrongMultivaluedRequest() throws Exception
     {
         final Map<String, List<String>> subscriptionRequest = new HashMap<String, List<String>>();
         subscriptionRequest.put("hub.mode", Arrays.asList("subscribe", "unsubscribe"));
 
         final MuleMessage response = sendRequestToHub(subscriptionRequest);
         assertEquals("400", response.getInboundProperty("http.status"));
         assertTrue(StringUtils.startsWith(response.getPayloadAsString(),
             "Multivalued parameters are only supported for:"));
     }
 
     public void testBadSubscriptionRequest() throws Exception
     {
         final Map<String, String> subscriptionRequest = new HashMap<String, String>();
         subscriptionRequest.put("hub.mode", "subscribe");
         // missing all other required parameters
 
         final MuleMessage response = wrapAndSendRequestToHub(subscriptionRequest);
         assertEquals("400", response.getInboundProperty("http.status"));
         assertEquals("Missing mandatory parameter: hub.callback", response.getPayloadAsString());
     }
 
     public void testSubscriptionRequestWithTooBigASecret() throws Exception
     {
         final Map<String, String> subscriptionRequest = new HashMap<String, String>();
         subscriptionRequest.put("hub.mode", "subscribe");
         subscriptionRequest.put("hub.callback", "http://localhost:" + getSubscriberCallbacksPort()
                                                 + "/cb-failure");
         subscriptionRequest.put("hub.topic", TEST_TOPIC);
         subscriptionRequest.put("hub.verify", "sync");
         subscriptionRequest.put("hub.secret", RandomStringUtils.randomAlphanumeric(200));
 
         final MuleMessage response = wrapAndSendRequestToHub(subscriptionRequest);
         assertEquals("400", response.getInboundProperty("http.status"));
         assertEquals("Maximum secret size is 200 bytes", response.getPayloadAsString());
     }
 
     public void testFailedSynchronousSubscriptionConfirmation() throws Exception
     {
         final Map<String, String> subscriptionRequest = new HashMap<String, String>();
         subscriptionRequest.put("hub.mode", "subscribe");
         subscriptionRequest.put("hub.callback", "http://localhost:" + getSubscriberCallbacksPort()
                                                 + "/cb-failure");
         subscriptionRequest.put("hub.topic", TEST_TOPIC);
         subscriptionRequest.put("hub.verify", "sync");
 
         final MuleMessage response = wrapAndSendRequestToHub(subscriptionRequest);
         assertEquals("500", response.getInboundProperty("http.status"));
     }
 
     public void testSuccessfullSynchronousSubscription() throws Exception
     {
         doTestSuccessfullSynchronousVerifiableAction(Action.SUBSCRIBE);
     }
 
     public void testSuccessfullSynchronousSubscriptionWithVerifyToken() throws Exception
     {
         final Map<String, List<String>> extraSubscriptionParam = Collections.singletonMap("hub.verify_token",
             Collections.singletonList(RandomStringUtils.randomAlphanumeric(20)));
         doTestSuccessfullSynchronousVerifiableAction(Action.SUBSCRIBE, extraSubscriptionParam);
     }
 
     public void testSuccessfullSynchronousSubscriptionWithQueryParamInCallback() throws Exception
     {
         doTestSuccessfullSynchronousVerifiableAction(Action.SUBSCRIBE, "?foo=bar");
     }
 
     public void testSuccessfullSynchronousSubscriptionWithSecret() throws Exception
     {
         final Map<String, List<String>> extraSubscriptionParam = Collections.singletonMap("hub.secret",
             Collections.singletonList(RandomStringUtils.randomAlphanumeric(20)));
         doTestSuccessfullSynchronousVerifiableAction(Action.SUBSCRIBE, extraSubscriptionParam);
     }
 
     public void testSuccessfullSynchronousMultiTopicsSubscription() throws Exception
     {
         final Map<String, List<String>> extraSubscriptionParam = Collections.singletonMap("hub.topic",
             Arrays.asList("http://mulesoft.org/faketopic1", "http://mulesoft.org/faketopic2"));
         doTestSuccessfullSynchronousVerifiableAction(Action.SUBSCRIBE, extraSubscriptionParam);
     }
 
     public void testSuccessfullSynchronousResubscription() throws Exception
     {
         doTestSuccessfullSynchronousVerifiableAction(Action.SUBSCRIBE);
         setupSuccessfullSubscriberFTC(1);
         doTestSuccessfullSynchronousVerifiableAction(Action.SUBSCRIBE);
     }
 
     public void testSuccessfullSynchronousUnsubscription() throws Exception
     {
         doTestSuccessfullSynchronousVerifiableAction(Action.SUBSCRIBE);
         setupSuccessfullSubscriberFTC(1);
         doTestSuccessfullSynchronousVerifiableAction(Action.UNSUBSCRIBE);
     }
 
     public void testSuccessfullAsynchronousUnsubscription() throws Exception
     {
         doTestSuccessfullVerifiableAction(Action.SUBSCRIBE, Verification.ASYNC, DEFAULT_SUBSCRIPTION_PARAMS,
             DEFAULT_CALLBACK_QUERY);
     }
 
     public void testSuccessfullNewContentNotificationAndContentFetch() throws Exception
     {
         final String topicUrl = "http://localhost:" + getPublisherPort() + "/feeds/mouth/rss";
         doTestSuccessfullNewContentNotificationAndContentFetch(topicUrl);
     }
 
     public void testSuccessfullNewMultiContentNotificationAndContentFetch() throws Exception
     {
         setupPublisherFTC(2);
 
         final Map<String, List<String>> subscriptionRequest = new HashMap<String, List<String>>();
         subscriptionRequest.put("hub.mode", Arrays.asList("publish"));
         subscriptionRequest.put(
             "hub.url",
             Arrays.asList("http://localhost:" + getPublisherPort() + "/feeds/mouth/rss", "http://localhost:"
                                                                                          + getPublisherPort()
                                                                                          + "/feeds/mouth/rss"));
 
         final MuleMessage response = sendRequestToHub(subscriptionRequest);
         assertEquals("204", response.getInboundProperty("http.status"));
 
         publisherCC.await(TimeUnit.SECONDS.toMillis(getTestTimeoutSecs()));
         final int receivedMessagesCount = publisherFTC.getReceivedMessagesCount();
         assertEquals(2, receivedMessagesCount);
         final Set<String> expectedMessages = new HashSet<String>(Arrays.asList("/feeds/mouth/rss",
             "/feeds/mouth/rss"));
         for (int i = 1; i <= receivedMessagesCount; i++)
         {
             assertTrue(expectedMessages.contains(publisherFTC.getReceivedMessage(i)));
         }
     }
 
     public void testSuccessfullContentDistribution() throws Exception
     {
         final String topicUrl = "http://localhost:" + getPublisherPort() + "/feeds/mouth/rss";
         final Map<String, List<String>> extraSubscriptionParam = Collections.singletonMap("hub.topic",
             Collections.singletonList(topicUrl));
         doTestSuccessfullSynchronousVerifiableAction(Action.SUBSCRIBE, extraSubscriptionParam);
 
         // reset the callback FTC latch
         setupSuccessfullSubscriberFTC(1);
 
         doTestSuccessfullNewContentNotificationAndContentFetch(topicUrl);
 
         // check RSS content has been pushed to callback FTC
         successfullSubscriberCC.await(TimeUnit.SECONDS.toMillis(getTestTimeoutSecs()));
         final SyndFeed syndFeed = new SyndFeedInput(true).build(new StringReader(
             (String) successfullSubscriberFTC.getLastReceivedMessage()));
         assertEquals("rss_2.0", syndFeed.getFeedType());
 
         assertEquals(123, dataStore.getTotalSubscriberCount(new URI(topicUrl)));
     }
 
     //
     // Supporting methods
     //
     private void doTestSuccessfullSynchronousVerifiableAction(final Action action) throws Exception
     {
         doTestSuccessfullSynchronousVerifiableAction(action, DEFAULT_CALLBACK_QUERY);
     }
 
     private void doTestSuccessfullSynchronousVerifiableAction(final Action action,
                                                               final Map<String, List<String>> extraSubscriptionParam)
         throws Exception
     {
         doTestSuccessfullVerifiableAction(action, Verification.SYNC, extraSubscriptionParam,
             DEFAULT_CALLBACK_QUERY);
     }
 
     private void doTestSuccessfullSynchronousVerifiableAction(final Action action, final String callbackQuery)
         throws Exception
     {
         doTestSuccessfullVerifiableAction(action, Verification.SYNC, DEFAULT_SUBSCRIPTION_PARAMS,
             callbackQuery);
     }
 
     private void doTestSuccessfullVerifiableAction(final Action action,
                                                    final Verification verification,
                                                    final Map<String, List<String>> extraSubscriptionParam,
                                                    final String callbackQuery) throws Exception
     {
         final String callback = "http://localhost:" + getSubscriberCallbacksPort() + "/cb-success"
                                 + callbackQuery;
 
         final Map<String, List<String>> subscriptionRequest = new HashMap<String, List<String>>();
         subscriptionRequest.put("hub.mode", Collections.singletonList(action.asHubMode()));
         subscriptionRequest.put("hub.callback", Collections.singletonList(callback));
         subscriptionRequest.put("hub.topic", Collections.singletonList(TEST_TOPIC));
         subscriptionRequest.put("hub.verify", Collections.singletonList(verification.asVerify()));
         subscriptionRequest.putAll(extraSubscriptionParam);
 
         final MuleMessage response = sendRequestToHub(subscriptionRequest);
 
         assertEquals(verification.getExpectedStatusCode(), response.getInboundProperty("http.status"));
 
         checkVerificationMessage(callbackQuery, subscriptionRequest);
 
         switch (action)
         {
             case SUBSCRIBE :
                 checkTopicSubscriptionStored(callback, subscriptionRequest);
                 break;
 
             case UNSUBSCRIBE :
                 checkTopicSubscriptionCleared(callback, subscriptionRequest);
                 break;
 
             default :
                 throw new UnsupportedOperationException("no store check for action: " + action);
         }
     }
 
     private void checkVerificationMessage(final String callbackQuery,
                                           final Map<String, List<String>> subscriptionRequest)
         throws Exception
     {
         successfullSubscriberCC.await(TimeUnit.SECONDS.toMillis(getTestTimeoutSecs()));
 
         final Map<String, List<String>> subscriberVerifyParams = TestUtils.getUrlParameters(successfullSubscriberFTC.getLastReceivedMessage()
             .toString());
 
         assertEquals(subscriptionRequest.get("hub.mode").get(0), subscriberVerifyParams.get("hub.mode")
             .get(0));
         assertTrue(StringUtils.isNotBlank(subscriberVerifyParams.get("hub.challenge").get(0)));
         assertTrue(NumberUtils.isDigits(subscriberVerifyParams.get("hub.lease_seconds").get(0)));
 
         for (final String hubTopic : subscriptionRequest.get("hub.topic"))
         {
             assertTrue(subscriberVerifyParams.get("hub.topic").contains(hubTopic));
         }
 
         final String verifyToken = subscriptionRequest.get("hub.verify_token") != null
                                                                                       ? subscriptionRequest.get(
                                                                                           "hub.verify_token")
                                                                                           .get(0)
                                                                                       : null;
         if (StringUtils.isNotBlank(verifyToken))
         {
             assertEquals(verifyToken, subscriberVerifyParams.get("hub.verify_token").get(0));
         }
         else
         {
             assertNull(subscriberVerifyParams.get("hub.verify_token"));
         }
 
         if (StringUtils.isNotBlank(callbackQuery))
         {
             final Map<String, List<String>> queryParams = TestUtils.getUrlParameters(callbackQuery);
             for (final Entry<String, List<String>> queryParam : queryParams.entrySet())
             {
                 assertEquals(queryParam.getValue(), subscriberVerifyParams.get(queryParam.getKey()));
             }
         }
         else
         {
             assertNull(subscriberVerifyParams.get("foo"));
         }
     }
 
     private void checkTopicSubscriptionStored(final String callback,
                                               final Map<String, List<String>> subscriptionRequest)
         throws Exception
     {
         for (final String hubTopic : subscriptionRequest.get("hub.topic"))
         {
             final URI hubTopicUri = new URI(hubTopic);
             final Set<TopicSubscription> topicSubscriptions = ponderUntilSubscriptionStored(hubTopicUri);
             assertEquals(1, topicSubscriptions.size());
             final TopicSubscription topicSubscription = topicSubscriptions.iterator().next();
             assertEquals(hubTopicUri, topicSubscription.getTopicUrl());
             assertEquals(new URI(callback), topicSubscription.getCallbackUrl());
             assertTrue(topicSubscription.getExpiryTime() > 0L);
             final String secretAsString = subscriptionRequest.get("hub.secret") != null
                                                                                        ? subscriptionRequest.get(
                                                                                            "hub.secret")
                                                                                            .get(0)
                                                                                        : null;
             if (StringUtils.isNotBlank(secretAsString))
             {
                 assertTrue(Arrays.equals(secretAsString.getBytes("utf-8"), topicSubscription.getSecret()));
             }
             else
             {
                 assertNull(topicSubscription.getSecret());
             }
         }
     }
 
     private void checkTopicSubscriptionCleared(final String callback,
                                                final Map<String, List<String>> subscriptionRequest)
         throws Exception
     {
         for (final String hubTopic : subscriptionRequest.get("hub.topic"))
         {
             final URI hubTopicUri = new URI(hubTopic);
             final Set<TopicSubscription> topicSubscriptions = dataStore.getTopicSubscriptions(hubTopicUri);
             assertEquals(0, topicSubscriptions.size());
         }
     }
 
     private Set<TopicSubscription> ponderUntilSubscriptionStored(final URI hubTopicUri)
         throws InterruptedException
     {
 
         for (int attempts = 0; attempts < 300; attempts++)
         {
             final Set<TopicSubscription> topicSubscriptions = dataStore.getTopicSubscriptions(hubTopicUri);
             if (!topicSubscriptions.isEmpty())
             {
                 return topicSubscriptions;
             }
             Thread.sleep(100L);
         }
         return Collections.emptySet();
     }
 
     private void doTestSuccessfullNewContentNotificationAndContentFetch(final String topicUrl)
         throws MuleException, InterruptedException
     {
         final Map<String, String> subscriptionRequest = new HashMap<String, String>();
         subscriptionRequest.put("hub.mode", "publish");
         subscriptionRequest.put("hub.url", topicUrl);
 
         final MuleMessage response = wrapAndSendRequestToHub(subscriptionRequest);
         assertEquals("204", response.getInboundProperty("http.status"));
 
         publisherCC.await(TimeUnit.SECONDS.toMillis(getTestTimeoutSecs()));
         assertEquals("/feeds/mouth/rss", publisherFTC.getLastReceivedMessage());
     }
 
     private MuleMessage wrapAndSendRequestToHub(final Map<String, String> subscriptionRequest)
         throws MuleException
     {
         final Map<String, List<String>> wrappedRequest = new HashMap<String, List<String>>();
         for (final Entry<String, String> param : subscriptionRequest.entrySet())
         {
             wrappedRequest.put(param.getKey(), Collections.singletonList(param.getValue()));
         }
         return sendRequestToHub(wrappedRequest);
     }
 
     private MuleMessage sendRequestToHub(final Map<String, List<String>> subscriptionRequest)
         throws MuleException
     {
         return sendRequestToHub(subscriptionRequest, "application/x-www-form-urlencoded");
     }
 
     private MuleMessage sendRequestToHub(final Map<String, List<String>> subscriptionRequest,
                                          final String contentType) throws MuleException
     {
         final String hubUrl = "http://localhost:" + getHubPort() + "/hub";
 
         final PostMethod postMethod = new PostMethod(hubUrl);
         postMethod.setRequestHeader(HttpConstants.HEADER_CONTENT_TYPE, contentType);
         for (final Entry<String, List<String>> param : subscriptionRequest.entrySet())
         {
             for (final String value : param.getValue())
             {
                 postMethod.addParameter(param.getKey(), value);
             }
         }
         final MuleMessage response = muleClient.send(hubUrl, postMethod, null);
         return response;
     }
 
     private Integer getHubPort()
     {
         return getPorts().get(0);
     }
 
     private Integer getSubscriberCallbacksPort()
     {
         return getPorts().get(1);
     }
 
     private Integer getPublisherPort()
     {
         return getPorts().get(2);
     }
 }
