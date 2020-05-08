 /**
  * Mule Pubnub Connector
  *
  * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
  *
  * The software in this package is published under the terms of the CPAL v1.0
  * license, a copy of which has been included with this distribution in the
  * LICENSE.txt file.
  */
 
 package org.mule.module.pubnub;
 
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.node.ObjectNode;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.mule.util.concurrent.Latch;
 
 import java.util.concurrent.TimeUnit;
 
 public class PubnubFunctionalJavaTestCase {
     private final String CHANNEL = "cc_channel";
     // TODO doesn't preserve chars first two chars
 //    private final String TEST_VALUE = "Hello World! --> ɂ顶@#$%^&*()!";
     private final String TEST_VALUE = "Hello World!";
 
     private PubNubModule pubnub;
 
     @Before
     public void init() {
         //Using the PubNub demo account for testing
         pubnub = new PubNubModule("demo", "demo", "");
     }
 
     @Test
     public void time() {
         //Not sure how to use this value
         double time = pubnub.serverTime();
         Assert.assertFalse("Did not receive a valid server time value", time == 0);
     }
 
     @Test
     public void publish() {
         ObjectNode msg = pubnub.createMessage();
         msg.put("some_val", TEST_VALUE);
         JsonNode info = pubnub.publish(CHANNEL, msg);
         Assert.assertEquals("TODO what is this value", 1, info.get(0).getIntValue());
        Assert.assertEquals("TODO what is this code", "D", info.get(1).getTextValue()); //TODO what are the return codes?
     }
 
     @Test
     public void history() {
         int limit = 1;
         // Get History
         JsonNode response = pubnub.history(CHANNEL, limit);
         // Print Response from PubNub JSONP REST Service
         Assert.assertEquals("There is only one history entry on this channel, the message should have been the same as the last publish",
                 TEST_VALUE, response.get(0).get("some_val").getTextValue());
     }
 
     @Test
     public void subscribe() throws InterruptedException {
         final Latch latch = new Latch();
         // Callback Interface when a Message is Received
         final MessageListener callback = new MessageListener() {
             @Override
             public boolean onMessage(JsonNode message) {
                 // Print Received Message
                 System.out.println("Subscribed Message received: " + message);
                 latch.release();
                 return false;
             }
         };
 
         //We need to to subscribe and publish in different threads since PubNub is not a queuing
         //system, so messages are only received to subscribers who are actively listening
         final Latch pubLatch = new Latch();
         Thread t = new Thread(new Runnable() {
             @Override
             public void run() {
                 // Listen for Messages (Subscribe)
                 pubLatch.release();
                 pubnub.subscribe(CHANNEL, callback);
             }
         });
 
         t.start();
         //We wait for the thread to start before publishing a message. This ensures that our
         //subscribe is listening before the message is published
         Assert.assertTrue("Subscriber was not registered in a separate thread", pubLatch.await(5, TimeUnit.SECONDS));
 
         ObjectNode msg = pubnub.createMessage();
         msg.put("hello", "you");
 
 
         pubnub.publish(CHANNEL, msg);
         Assert.assertTrue("Message was not received on channel: " + CHANNEL, latch.await(30, TimeUnit.SECONDS));
     }
 
 
     /*
     @Test
     public void request() throws InterruptedException
     {
         final Latch latch = new Latch();
         final Latch pubLatch = new Latch();
         final StringBuilder errorMessage = new StringBuilder();
         final AtomicReference<JsonNode> result = new AtomicReference<JsonNode>();
 
         Thread t = new Thread(new Runnable()
         {
             @Override
             public void run()
             {
                 long timeout = 5000;
                 // Listen for Messages (Subscribe)
                 pubLatch.release();
                 long start = System.currentTimeMillis();
 
                 JsonNode response = pubnub.request(CHANNEL, timeout);
                 if (response==null)
                 {
                     errorMessage.append("We should have received one message on the channel");
                 }
                 result.set(response);
                 latch.release();
             }
         });
 
         t.start();
         Assert.assertTrue("Request was not made in a separate thread", pubLatch.await(15, TimeUnit.SECONDS));
         System.out.println("ready to publish");
         if(errorMessage.length() > 0) {
             Assert.fail(errorMessage.toString());
         }
 
         ObjectNode msg = pubnub.createMessage();
         msg.put("hello", "me");
 
 
         pubnub.publish(CHANNEL, msg);
 
         System.out.println("published");
 
         Assert.assertTrue("Message was not received on channel: " + CHANNEL, latch.await(15, TimeUnit.SECONDS));
 
         Assert.assertNotNull(result.get());
         Assert.assertEquals(1, result.get().size());
         System.out.println(result.get().getTextValue());
     }
     */
 }
 
 
