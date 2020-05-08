 /*
  * Licensed to the Apache Software Foundation (ASF) under one or more
  * contributor license agreements.  See the NOTICE file distributed with
  * this work for additional information regarding copyright ownership.
  * The ASF licenses this file to You under the Apache License, Version 2.0
  * (the "License"); you may not use this file except in compliance with
  * the License.  You may obtain a copy of the License at
  * 
  *      http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.log4j.net;
 
 import javax.jms.MessageProducer;
 import javax.jms.ObjectMessage;
 import javax.jms.Queue;
 import javax.jms.QueueConnection;
 import javax.jms.QueueConnectionFactory;
 import javax.jms.QueueSender;
 import javax.jms.QueueSession;
 import javax.jms.Session;
 import javax.naming.Context;
 import javax.naming.NameNotFoundException;
 import javax.naming.NamingException;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 import org.apache.log4j.AppenderSkeleton;
 import org.apache.log4j.Logger;
 import org.apache.log4j.spi.LoggingEvent;
 
 public class JMSAppender extends AppenderSkeleton
 {
     boolean delayedActivation;
 
     boolean activated;
 
     QueueConnection queueConnection;
 
     QueueSession queueSession;
 
     QueueSender queueSender;
     
     MessageProducer producer;
 
     Logger logger = Logger.getLogger(this.getClass());
 
     public JMSAppender()
     {
     }
 
     /**
      * The <b>DelayedActivation</b> option takes a boolean value. If
      * <code>delayedActivation</code> is <code>true</code> the appender
      * options are activated when <code>append</code> is called for the first
      * time.
      */
     public void setDelayedActivation(boolean delayedActivation)
     {
         this.delayedActivation = delayedActivation;
     }
 
     /**
      * Returns the value of the <b>DelayedActivation</b> option.
      */
     public boolean getDelayedActivation()
     {
         return delayedActivation;
     }
 
     /**
      * Options are activated and become effective after calling this method when
      * <code>DelayedActivation</code> is <code>false</code>.
      */
     public void activateOptions()
     {
         if (!delayedActivation)
         {
             internalActivateOptions();
         }
     }
 
     /**
      * Options are activated and become effective only after calling this
      * method.
      */
     protected void internalActivateOptions()
     {
         if (activated)
         {
             return;
         }
         else
         {
             activated = true;
         }
 
         QueueConnectionFactory queueConnectionFactory;
 
         try
         {
            queueConnectionFactory = new ActiveMQConnectionFactory("tcp://fucker:6969");
             logger.info("About to create QueueConnection.");
             queueConnection = queueConnectionFactory.createQueueConnection();
 
             logger.info("Creating QueueSession, transactional, " + "in AUTO_ACKNOWLEDGE mode.");
            queueSession = queueConnection.createQueueSession(true, Session.AUTO_ACKNOWLEDGE);
 
             logger.info("About to create Queue.");
            Queue queue = queueSession.createQueue("queue/exceptionQueue");
 
             logger.info("Creating QueueSender.");
             queueSender = queueSession.createSender(queue);
             
             logger.info("Starting QueueConnection.");
             queueConnection.start();
         }
         catch (Exception e)
         {
             logger.error("Error while activating options for appender named [" + name + "].", e);
         }
     }
 
     protected Object lookup(Context ctx, String name) throws NamingException
     {
         try
         {
             return ctx.lookup(name);
         }
         catch (NameNotFoundException e)
         {
             logger.error("Could not find name [" + name + "].", e);
             throw e;
         }
     }
 
     protected boolean checkEntryConditions(LoggingEvent event)
     {
         String fail = null;
 
         if (delayedActivation)
         {
             internalActivateOptions();
         }
 
         if (this.queueConnection == null)
         {
             fail = "No QueueConnection";
         }
         else if (this.queueSession == null)
         {
             fail = "No QueueSession";
         }
         else if (this.queueSender == null)
         {
             fail = "No QueueSender";
         }
 
         if (fail != null)
         {
             logger.error(fail + " for JMSAppender named [" + name + "].");
             return false;
         }
         else
         {
             return true;
         }
     }
 
     /**
      * Close this JMSAppender. Closing releases all resources used by the
      * appender. A closed appender cannot be re-opened.
      */
     public synchronized void close()
     {
         // The synchronized modifier avoids concurrent append and close
         // operations
 
         if (this.closed)
             return;
 
         logger.info("Closing appender [" + name + "].");
         this.closed = true;
     }
 
     /**
      * This method called by {@link AppenderSkeleton#doAppend} method to do most
      * of the real appending work.
      */
     public void append(LoggingEvent event)
     {
         if (!checkEntryConditions(event))
         {
             return;
         }
 
         try
         {
             logger.info("About to send message!");
             
            producer = queueSession.createProducer(queueSender.getDestination());
             ObjectMessage msg = queueSession.createObjectMessage();
             msg.setObject(event);
            producer.send(msg);
            
             queueSession.commit();
             
             logger.info("Message sent!");
         }
         catch (Exception e)
         {
             logger.error("Could not publish message in JMSAppender [" + name + "].", e);
         }
     }
 
     /**
      * The JMSAppender sends serialized events and consequently does not require
      * a layout.
      */
     public boolean requiresLayout()
     {
         return false;
     }
 }
