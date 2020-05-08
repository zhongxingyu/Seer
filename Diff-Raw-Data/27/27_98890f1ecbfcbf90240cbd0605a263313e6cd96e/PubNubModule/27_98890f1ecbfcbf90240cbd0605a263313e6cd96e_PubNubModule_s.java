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
 
 import com.sun.jersey.api.client.Client;
 import com.sun.jersey.api.client.WebResource;
 
 import org.apache.commons.lang.StringUtils;
 import org.codehaus.jackson.JsonNode;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ObjectNode;
 import org.mule.api.annotations.Configurable;
 import org.mule.api.annotations.Module;
 import org.mule.api.annotations.Processor;
 import org.mule.api.annotations.Source;
 import org.mule.api.annotations.lifecycle.Start;
 import org.mule.api.annotations.param.Default;
 import org.mule.api.annotations.param.Optional;
 import org.mule.api.callback.SourceCallback;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.math.BigInteger;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * PubNub is an Internet-Wide Messaging Service for Real-time Web and Mobile apps and games. PubNub
  * is a massively scalable Real-time Bidirectional Messaging Service in the Cloud.
  * <p/>
  * This connector supports version 3 of the PubNub REST API.
  * <p/>
  * PubNub is a freemium service where users get 5,000 messages per day for free but then have reasonable pricing
  * plans for higher volume usage.  For more information go to PubNub: http://pubnub.com
  *
  * @author MuleSoft, Inc.
  */
 @Module(name = "pubnub")
 public class PubNubModule {
 
     public static final int MESSAGE_LIMIT = 1800;
     private static final String ORIGIN = "pubsub.pubnub.com";
     private static Logger logger = LoggerFactory.getLogger(PubNubModule.class);
 
     private String originUrl;
 
     /**
      * Your publish key that allows you to send data to the PubNub cloud
      */
     @Configurable
     private String publishKey;
 
     /**
      * Your subscribe key that allows you to send data to the PubNub cloud
      */
     @Configurable
     private String subscribeKey;
 
     /**
      * The secret key given to you when you created the account
      */
     @Configurable
     @Optional
     private String secretKey;
 
     /**
      * Whether to use SSL or not when communicating with the PubNub cloud
      */
     @Configurable
     @Optional
     @Default("false")
     private boolean ssl;
 
     private Client httpClient = Client.create();
     private ObjectMapper mapper = new ObjectMapper();
 
     /**
      * Creates a new PubNub connector with no state.  If this constructor is used, then you must
      * also call {@link #config(String, String, String, boolean)}
      */
     public PubNubModule() {
     }
 
     /**
      * Creates a new PubHub connector with required state. The keys required to create the connector are available from
      * your account on the PubNub website: http://www.pubnub.com/account.
      * SSL can be used by setting the SSL flag.
      *
      * @param publishKey   Your publish key that allows you to send data to the PubNub cloud
      * @param subscribeKey Your subscribe key that allows you to send data to the PubNub cloud
      * @param secretKey    The secret key given to you when you created the account
      * @param ssl          Whether to use SSL or not when communicating with the PubNub cloud
      */
     public PubNubModule(String publishKey, String subscribeKey, String secretKey, boolean ssl) {
         this.publishKey = publishKey;
         this.subscribeKey = subscribeKey;
         this.secretKey = secretKey;
         this.ssl = ssl;
         init();
     }
 
 
     /**
      * Creates a new PubHub connector with required state. The keys required to create the connector are available from
      * your account on the PubNub website: http://www.pubnub.com/account.
      *
      * @param publishKey   Your publish key that allows you to send data to the PubNub cloud
      * @param subscribeKey Your subscribe key that allows you to send data to the PubNub cloud
      * @param secretKey    The secret key given to you when you created the account
      */
     public PubNubModule(String publishKey, String subscribeKey, String secretKey) {
         this.publishKey = publishKey;
         this.subscribeKey = subscribeKey;
         this.secretKey = secretKey;
         init();
     }
 
 
     public String getPublishKey() {
         return publishKey;
     }
 
     public void setPublishKey(String publishKey) {
         this.publishKey = publishKey;
     }
 
     public String getSubscribeKey() {
         return subscribeKey;
     }
 
     public void setSubscribeKey(String subscribeKey) {
         this.subscribeKey = subscribeKey;
     }
 
     public String getSecretKey() {
         return secretKey;
     }
 
     public void setSecretKey(String secretKey) {
         this.secretKey = secretKey;
     }
 
     public boolean isSsl() {
         return ssl;
     }
 
     public void setSsl(boolean ssl) {
         this.ssl = ssl;
     }
 
     @Start
     public void init() {
         this.originUrl = "http" + (ssl ? "s://" : "://") + ORIGIN;
         httpClient = Client.create();
         mapper = new ObjectMapper();
 
         httpClient.setReadTimeout(1000 * 60);
         httpClient.setConnectTimeout(1000 * 10);
     }
 
     protected String getBaseUrl() {
         if (originUrl == null) {
             originUrl = "http" + (ssl ? "s://" : "://") + ORIGIN;
             //Work around because there is no lifecycle support
             httpClient.setReadTimeout(1000 * 60);
             httpClient.setConnectTimeout(1000 * 10);
         }
         return originUrl;
     }
 
     /**
      * Send a json message to a channel.
      *
      * @param channel     to publish to
      * @param jsonMessage the message to publish
      * @return the response from the server
      */
     public JsonNode publish(String channel, JsonNode jsonMessage) {
         return publish(channel, jsonMessage.toString());
     }
 
     /**
      * Send a json message to a channel.
      * <p/>
      * {@sample.xml ../../../doc/mule-module-pubnub.xml.sample pubnub:publish}
      *
      * @param channel     name to publish the message to
      * @param jsonMessage the message to publish
      * @return boolean false on fail.
      */
     @Processor
     public JsonNode publish(String channel, String jsonMessage) {
         // Generate String to Sign
         String signature = "0";
         if (StringUtils.isNotBlank(this.secretKey)) {
             StringBuilder string_to_sign = new StringBuilder();
             string_to_sign
                     .append(this.publishKey)
                     .append('/')
                     .append(this.subscribeKey)
                     .append('/')
                     .append(this.secretKey)
                     .append('/')
                     .append(channel)
                     .append('/')
                     .append(jsonMessage);
 
             // Sign Message
             signature = md5(string_to_sign.toString());
         }
 
         // Build URL
         List<String> url = new ArrayList<String>();
         url.add("publish");
         url.add(this.publishKey);
         url.add(this.subscribeKey);
         url.add(signature);
         url.add(channel);
         url.add("0");
         url.add(jsonMessage);
 
         // Return JSONArray
         return doRequest(url);
     }
 
     /**
      * Listen for a message on a channel.
      *
      * {@sample.xml ../../../doc/mule-module-pubnub.xml.sample pubnub:subscribe}
      *
      * @param channel  name to listen on
      * @param callback Callback to execute when a message comes in
      */
     @Source
     public void subscribe(String channel, final SourceCallback callback) {
         this.doSubscribe(channel, new MessageListener() {
             public boolean onMessage(JsonNode message) {
                 try {
                     callback.process(message);
                 } catch (Exception e) {
                     logger.error(e.getMessage(), e);
                 }
                 return true;
             }
         }, "0");
     }
 
 
     /**
      * Register a message listener for a channel.  This is a blocking subscribe which also means that only one
      * subscription can be made per connector.
      * <p/>
      * Listen for a message on a channel.
      *
      * @param channel  name to listen on
      * @param listener the message callback to invoke when a message is received. there will be one invocation
      *                 per message and the invocation on the message listener is guaranteed never to be null.
      */
     public void subscribe(String channel, MessageListener listener) {
         this.doSubscribe(channel, listener, "0");
     }
 
     /**
      * Makes a blocking request to receive a message on a channel.
      * <p/>
      * {@sample.xml ../../../doc/mule-module-pubnub.xml.sample pubnub:request}
      *
      * @param channel name to read from
      * @param timeout how long to wait for a message. The value is expressed in milliseconds. specify for
      * @return zero or more messages depending on what was in the queue
      */
     @Processor
     public JsonNode request(String channel, @Optional @Default("5000") final long timeout) {
         // Build URL
         String timetoken = "0";
         List<String> url = java.util.Arrays.asList("subscribe", this.subscribeKey, channel, "0", timetoken);
 
         long start = System.currentTimeMillis();
         do {
             // Wait for Message
             JsonNode response = doRequest(url);
 
             JsonNode messages = response.get(0);
             if (messages.size() > 0) {
                 return messages;
             }
             // Update TimeToken
             if (response.get(1).getTextValue().length() > 0) {
                 timetoken = response.get(1).getTextValue();
                 url = java.util.Arrays.asList("subscribe", this.subscribeKey, channel, "0", timetoken);
             }
         }
         while ((start + timeout) > System.currentTimeMillis());
         return null;
     }
 
     /**
      * @param channel   name to subscribe to
      * @param listener  callback for messages received
      * @param timetoken the time since the last read from the server. This value is usually sent with a result
      *                  from the subscribe RESt method on the server.
      */
     private void doSubscribe(String channel, MessageListener listener, String timetoken) {
         while (true) {
             try {
                 // Build URL
                 List<String> url = java.util.Arrays.asList(
                         "subscribe", this.subscribeKey, channel, "0", timetoken
                 );
 
                 // Wait for Message
                 JsonNode response = doRequest(url);
 
                 JsonNode messages = response.get(0);
 
                 // Update TimeToken
                 if (response.get(1).getTextValue().length() > 0) {
                     timetoken = response.get(1).getTextValue();
                 }
 
 
                 // Run user Callback and Reconnect if user permits. If
                 // there's a timeout then messages.length() == 0.
                 for (int i = 0; messages.size() > i; i++) {
                     JsonNode message = messages.get(i);
                     if (!listener.onMessage(message)) {
                         return;
                     }
                 }
             } catch (Exception e) {
                 try {
                     Thread.sleep(1000);
                 } catch (InterruptedException ignored) {
                 }
             }
         }
     }
 
 
     /**
      * Load history from a channel.
      * <p/>
      * {@sample.xml ../../../doc/mule-module-pubnub.xml.sample pubnub:history}
      *
      * @param channel name.
      * @param limit   history count response.
      * @return JsonNode of history.
      */
     @Processor
     public JsonNode history(String channel, int limit) {
         List<String> url = new ArrayList<String>();
 
         url.add("history");
         url.add(this.subscribeKey);
         url.add(channel);
         url.add("0");
         url.add(Integer.toString(limit));
 
         return doRequest(url);
     }
 
     /**
      * Get the Timestamp from PubNub Cloud.
      * <p/>
      * {@sample.xml ../../../doc/mule-module-pubnub.xml.sample pubnub:server-time}
      *
      * @return double timestamp.
      */
     @Processor
     public double serverTime() {
         List<String> url = new ArrayList<String>();
 
         url.add("time");
         url.add("0");
 
         JsonNode response = doRequest(url);
         return response.get(0).getDoubleValue();
     }
 
     /**
      * Creates a new Json Object message. The {@link org.codehaus.jackson.node.ObjectNode} can be used like a Map
      * to write key/value pairs.  Objects can be text, numeric, binary or complex objects
      *
      * @return a newly created {@link org.codehaus.jackson.node.ObjectNode} instance.
      */
     public ObjectNode createMessage() {
         return mapper.createObjectNode();
     }
 
     /**
      * Request URL
      *
      * @param urlParts List<String> request of url directories.
      * @return JSONArray from JSON response.
      */
     private JsonNode doRequest(List<String> urlParts) {
         StringBuilder url = new StringBuilder(getBaseUrl());
 
         // Generate URL with UTF-8 Encoding
         for (final String param: urlParts) {
             try {
                 url.append("/").append(_encodeURIcomponent(param));
             } catch (Exception e) {
                return createResponse("Failed UTF-8 Encoding URL.");
             }
         }
 
         // Fail if string too long
         if (url.length() > MESSAGE_LIMIT) {
            return createResponse("Message too long");
         }
 
         try {
             WebResource webResource = httpClient.resource(url.toString());
             String response = webResource.get(String.class);
             return mapper.readTree(response);
         } catch (Exception e) {
            return createResponse("Failed JSONP HTTP Request.");
         }
     }
 
    private JsonNode createResponse(String message) {
        ObjectNode nd = mapper.createObjectNode();
        nd.arrayNode().add(message);
        return nd;
     }
 
     private String _encodeURIcomponent(String s) {
         StringBuilder o = new StringBuilder();
         for (char ch : s.toCharArray()) {
             if (isUnsafe(ch)) {
                 o.append('%');
                 o.append(toHex(ch / 16));
                 o.append(toHex(ch % 16));
             } else {
                 o.append(ch);
             }
         }
         return o.toString();
     }
 
     private char toHex(int ch) {
         return (char) (ch < 10 ? '0' + ch : 'A' + ch - 10);
     }
 
     private boolean isUnsafe(char ch) {
         return " ~`!@#$%^&*()+=[]\\{}|;':\",./<>?".indexOf(ch) >= 0;
     }
 
     private String md5(String input) {
         try {
             MessageDigest md = MessageDigest.getInstance("MD5");
             byte[] messageDigest = md.digest(input.getBytes());
             BigInteger number = new BigInteger(1, messageDigest);
             String hashtext = number.toString(16);
 
             while (hashtext.length() < 32) {
                 hashtext = "0" + hashtext;
             }
 
             return hashtext;
         } catch (NoSuchAlgorithmException e) {
             throw new RuntimeException(e);
         }
     }
 }
