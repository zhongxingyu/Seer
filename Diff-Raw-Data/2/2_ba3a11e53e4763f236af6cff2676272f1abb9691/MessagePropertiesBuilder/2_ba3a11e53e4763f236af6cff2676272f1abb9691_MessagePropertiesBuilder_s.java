 /*
  * Copyright (C) 2010-2011, Zenoss Inc.  All Rights Reserved.
  */
 package org.zenoss.amqp;
 
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 public class MessagePropertiesBuilder {
 
     private MessagePropertiesImpl properties = new MessagePropertiesImpl();
 
     private MessagePropertiesBuilder() {
     }
 
     public MessagePropertiesBuilder setContentType(String contentType) {
         properties.contentType = contentType;
         return this;
     }
 
     public MessagePropertiesBuilder setContentEncoding(String contentEncoding) {
         properties.contentEncoding = contentEncoding;
         return this;
     }
 
     public MessagePropertiesBuilder addHeader(String name, Object value) {
         properties.headers.put(name, value);
         return this;
     }
 
     public MessagePropertiesBuilder removeHeader(String name) {
         properties.headers.remove(name);
         return this;
     }
 
     public MessagePropertiesBuilder setDeliveryMode(
             MessageDeliveryMode deliveryMode) {
         properties.deliveryMode = deliveryMode;
         return this;
     }
 
     public MessagePropertiesBuilder setPriority(int priority) {
         properties.priority = priority;
         return this;
     }
 
     public MessagePropertiesBuilder setCorrelationId(String correlationId) {
         properties.correlationId = correlationId;
         return this;
     }
 
     public MessagePropertiesBuilder setReplyTo(String replyTo) {
         properties.replyTo = replyTo;
         return this;
     }
 
     public MessagePropertiesBuilder setExpiration(String expiration) {
         properties.expiration = expiration;
         return this;
     }
 
     public MessagePropertiesBuilder setMessageId(String messageId) {
         properties.messageId = messageId;
         return this;
     }
 
     public MessagePropertiesBuilder setTimestamp(Date timestamp) {
         properties.timestamp = (timestamp != null) ? (Date) timestamp.clone()
                 : null;
         return this;
     }
 
     public MessagePropertiesBuilder setType(String type) {
         properties.type = type;
         return this;
     }
 
     public MessagePropertiesBuilder setUserId(String userId) {
         properties.userId = userId;
         return this;
     }
 
     public MessagePropertiesBuilder setAppId(String appId) {
         properties.appId = appId;
         return this;
     }
 
     public MessageProperties build() {
         MessageProperties built = this.properties;
         this.properties = new MessagePropertiesImpl();
         return built;
     }
 
     public static MessagePropertiesBuilder newBuilder() {
         return new MessagePropertiesBuilder();
     }
 
     private static class MessagePropertiesImpl implements MessageProperties {
 
         private String contentType;
         private String contentEncoding;
         private Map<String, Object> headers = new HashMap<String, Object>();
        private MessageDeliveryMode deliveryMode = MessageDeliveryMode.NON_PERSISTENT;
         private int priority = 0;
         private String correlationId;
         private String replyTo;
         private String expiration;
         private String messageId;
         private Date timestamp;
         private String type;
         private String userId;
         private String appId;
 
         @Override
         public String getContentType() {
             return contentType;
         }
 
         @Override
         public String getContentEncoding() {
             return contentEncoding;
         }
 
         @Override
         public Map<String, Object> getHeaders() {
             return Collections.unmodifiableMap(headers);
         }
 
         @Override
         public MessageDeliveryMode getDeliveryMode() {
             return deliveryMode;
         }
 
         @Override
         public int getPriority() {
             return priority;
         }
 
         @Override
         public String getCorrelationId() {
             return correlationId;
         }
 
         @Override
         public String getReplyTo() {
             return replyTo;
         }
 
         @Override
         public String getExpiration() {
             return expiration;
         }
 
         @Override
         public String getMessageId() {
             return messageId;
         }
 
         @Override
         public Date getTimestamp() {
             return (timestamp != null) ? (Date) timestamp.clone() : null;
         }
 
         @Override
         public String getType() {
             return type;
         }
 
         @Override
         public String getUserId() {
             return userId;
         }
 
         @Override
         public String getAppId() {
             return appId;
         }
 
         @Override
         public String toString() {
             return String
                     .format("MessageProperties [contentType=%s, contentEncoding=%s, headers=%s, deliveryMode=%s, priority=%s, correlationId=%s, replyTo=%s, expiration=%s, messageId=%s, timestamp=%s, type=%s, userId=%s, appId=%s]",
                             contentType, contentEncoding, headers,
                             deliveryMode, priority, correlationId, replyTo,
                             expiration, messageId, timestamp, type, userId,
                             appId);
         }
     }
 }
