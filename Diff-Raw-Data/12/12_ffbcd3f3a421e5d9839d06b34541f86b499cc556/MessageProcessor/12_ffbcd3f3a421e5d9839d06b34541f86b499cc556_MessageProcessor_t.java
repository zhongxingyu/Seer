 /*
  * Copyright 2010 the original author or authors.
  * Copyright 2009 Paxxis Technology LLC
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package com.paxxis.cornerstone.service;
 
 import com.paxxis.cornerstone.base.MessagingConstants;

 import com.paxxis.cornerstone.common.MessagePayload;
 
 import javax.jms.Message;
 import javax.jms.MessageProducer;
 import javax.jms.TemporaryQueue;
 
import org.apache.log4j.Logger;



 /**
  *
  * @author Robert Englander
  */
 public abstract class MessageProcessor extends SimpleMessageProcessor {
    private static final Logger logger = Logger.getLogger(MessageProcessor.class);
    
     public MessageProcessor(MessagePayload type) {
         super(type);
     }
 
     public void run() {
         try {
             Message message = getMessage();
             com.paxxis.cornerstone.base.Message resp = process(false);
             if (resp != null) {
                 Object response = resp.getAsPayload(getPayloadType().getType());
                 Message responseMsg = getPayloadType().createMessage(getSession(), response);
 
                 responseMsg.setIntProperty(MessagingConstants.HeaderConstant.MessageType.name(), resp.getMessageType());
                 responseMsg.setIntProperty(MessagingConstants.HeaderConstant.MessageVersion.name(), resp.getMessageVersion());
 
                 // copy the correlation id from the incoming message
                 responseMsg.setJMSCorrelationID(message.getJMSCorrelationID());
 
                 // create a sender for the replyTo queue
                 TemporaryQueue tq = (TemporaryQueue)message.getJMSReplyTo();
                 if (tq != null) {
                     MessageProducer qsender = getSession().createProducer(tq);
 
                     // stamp the message
                     responseMsg.setLongProperty("TIMESTAMP", System.currentTimeMillis());
                     qsender.send(responseMsg);
                     qsender.close();
                 }
             }
 
             if (isClientAck()) {
                 message.acknowledge();
             }
         } catch (Exception e) {
            logger.error(e);
         }
     }
 }
