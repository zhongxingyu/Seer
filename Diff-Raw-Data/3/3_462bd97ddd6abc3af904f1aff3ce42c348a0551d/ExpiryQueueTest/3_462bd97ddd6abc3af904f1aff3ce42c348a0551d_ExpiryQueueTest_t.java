 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jboss.test.messaging.jms;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.DeliveryMode;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 import javax.jms.Topic;
 import javax.management.ObjectName;
 import javax.naming.InitialContext;
 import javax.naming.NameNotFoundException;
 
 import org.jboss.jms.destination.JBossQueue;
 import org.jboss.jms.server.endpoint.ServerSessionEndpoint;
 import org.jboss.test.messaging.MessagingTestCase;
 import org.jboss.test.messaging.tools.ServerManagement;
 
 /**
  * 
  * A ExpiryQueueTest
  *
  * @author <a href="mailto:tim.fox@jboss.com">Tim Fox</a>
  * @version <tt>$Revision$</tt>
  *
  * $Id$
  *
  */
 public class ExpiryQueueTest extends MessagingTestCase
 {
    // Constants -----------------------------------------------------
 
    // Static --------------------------------------------------------
 
    // Attributes ----------------------------------------------------
 
    protected InitialContext ic;
    protected ConnectionFactory cf;
    protected Queue queue;
    protected Topic topic;
 
    // Constructors --------------------------------------------------
 
    public ExpiryQueueTest(String name)
    {
       super(name);
    }
 
    // Public --------------------------------------------------------
 
    public void testExpiryQueueAlreadyDeployed() throws Exception
    {
       if (ServerManagement.isRemote())
       {
          return;
       }
       
       try
       {
       
          ServerManagement.deployQueue("ExpiryQueue");
          
          ObjectName serverPeerObjectName = ServerManagement.getServerPeerObjectName();
          
          ObjectName expiryQueueObjectName = (ObjectName)ServerManagement.getAttribute(serverPeerObjectName, "DefaultExpiryQueue");
          
          assertNotNull(expiryQueueObjectName);
                
          String name = (String)ServerManagement.getAttribute(expiryQueueObjectName, "Name");
          
          assertNotNull(name);
          
          assertEquals("ExpiryQueue", name);
    
          String jndiName = (String)ServerManagement.getAttribute(expiryQueueObjectName, "JNDIName");
          
          assertNotNull(jndiName);
          
          assertEquals("/queue/ExpiryQueue", jndiName);
          
          org.jboss.messaging.core.Queue expiryQueue = ServerManagement.getServer().getServerPeer().getDefaultExpiryQueueInstance();
    
          assertNotNull(expiryQueue);
    
          InitialContext ic = null;
  
          ic = new InitialContext(ServerManagement.getJNDIEnvironment());
 
          JBossQueue q = (JBossQueue)ic.lookup("/queue/ExpiryQueue");
 
          assertNotNull(q);
 
          assertEquals("ExpiryQueue", q.getName());
       }
       finally
       {
          if (ic != null) ic.close();
 
          ServerManagement.undeployQueue("ExpiryQueue");
 
       }
    }
 
    public void testExpiryQueueNotAlreadyDeployed() throws Exception
    {
       if (ServerManagement.isRemote())
       {
          return;
       }
       
       org.jboss.messaging.core.Queue expiryQueue = ServerManagement.getServer().getServerPeer().getDefaultExpiryQueueInstance();
 
       assertNull(expiryQueue);
 
       InitialContext ic = null;
 
       try
       {
          ic = new InitialContext(ServerManagement.getJNDIEnvironment());
 
          try
          {
             ic.lookup("/queue/ExpiryQueue");
 
             fail();
          }
          catch (NameNotFoundException e)
          {
             //Ok
          }
       }
       finally
       {
          if (ic != null) ic.close();
       }
    }
    
    public void testDefaultAndOverrideExpiryQueue() throws Exception
    {
       final int NUM_MESSAGES = 5;
       
       Connection conn = null;
       
       ObjectName serverPeerObjectName = ServerManagement.getServerPeerObjectName();
       
       try
       { 
       
          ServerManagement.deployQueue("DefaultExpiry");
          
          ServerManagement.deployQueue("OverrideExpiry");
          
          ServerManagement.deployQueue("TestQueue");
          
          String defaultExpiryObjectName = "jboss.messaging.destination:service=Queue,name=DefaultExpiry";
          
          String overrideExpiryObjectName = "jboss.messaging.destination:service=Queue,name=OverrideExpiry";
          
          String testQueueObjectName = "jboss.messaging.destination:service=Queue,name=TestQueue";         
          
          ServerManagement.setAttribute(serverPeerObjectName, "DefaultExpiryQueue", defaultExpiryObjectName);
          
          ServerManagement.setAttribute(new ObjectName(testQueueObjectName), "ExpiryQueue", "");
          
          Queue testQueue = (Queue)ic.lookup("/queue/TestQueue");
          
          Queue defaultExpiry = (Queue)ic.lookup("/queue/DefaultExpiry");
          
          Queue overrideExpiry = (Queue)ic.lookup("/queue/OverrideExpiry");
          
          drainDestination(cf, testQueue);
                
          drainDestination(cf, defaultExpiry);
                
          drainDestination(cf, overrideExpiry);
                      
          conn = cf.createConnection();
          
          {         
             Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
             MessageProducer prod = sess.createProducer(testQueue);
             
             conn.start();
    
             for (int i = 0; i < NUM_MESSAGES; i++)
             {
                TextMessage tm = sess.createTextMessage("Message:" + i);
    
                //Send messages with time to live of 2000 enough time to get to client consumer - so 
                //they won't be expired on the server side
                prod.send(tm, DeliveryMode.PERSISTENT, 4, 2000);
             }
             
             Session sess2 = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                         
             MessageConsumer cons = sess2.createConsumer(testQueue);
             
             //The messages should now be sitting in the consumer buffer
             
             //Now give them enough time to expire
             
             Thread.sleep(2500);
             
             //Now try and receive
             
             Message m = cons.receive(1000);
 
             assertNull(m);
 
             //Message should all be in the default expiry queue - let's check
                         
             MessageConsumer cons3 = sess.createConsumer(defaultExpiry);
             
             for (int i = 0; i < NUM_MESSAGES; i++)
             {
                TextMessage tm = (TextMessage)cons3.receive(1000);
    
                assertNotNull(tm);
    
                assertEquals("Message:" + i, tm.getText());
             }
             
             conn.close();
          }
          
          
          //now try with overriding the default expiry queue
          {         
             ServerManagement.setAttribute(new ObjectName(testQueueObjectName), "ExpiryQueue", overrideExpiryObjectName);
             
             conn = cf.createConnection();
             
             Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
             MessageProducer prod = sess.createProducer(testQueue);
             
             conn.start();
    
             for (int i = 0; i < NUM_MESSAGES; i++)
             {
                TextMessage tm = sess.createTextMessage("Message:" + i);
    
                //Send messages with time to live of 2000 enough time to get to client consumer - so 
                //they won't be expired on the server side
                prod.send(tm, DeliveryMode.PERSISTENT, 4, 2000);
             }
             
             Session sess2 = conn.createSession(false, Session.CLIENT_ACKNOWLEDGE);
                         
             MessageConsumer cons = sess2.createConsumer(testQueue);
             
             //The messages should now be sitting in the consumer buffer
             
             //Now give them enough time to expire
             
             Thread.sleep(2500);
             
             //Now try and receive
             
             Message m = cons.receive(1000);
 
             assertNull(m);
 
             //Message should all be in the override expiry queue - let's check
                         
             MessageConsumer cons3 = sess.createConsumer(overrideExpiry);
             
             for (int i = 0; i < NUM_MESSAGES; i++)
             {
                TextMessage tm = (TextMessage)cons3.receive(1000);
    
                assertNotNull(tm);
                
                assertEquals("Message:" + i, tm.getText());
             }
          }
       }
       finally
       {
          ServerManagement.setAttribute(serverPeerObjectName, "DefaultExpiryQueue", "jboss.messaging.destination:service=Queue,name=ExpiryQueue");
                   
          ServerManagement.undeployQueue("DefaultDLQ");
          
          ServerManagement.undeployQueue("OverrideDLQ");
          
          ServerManagement.undeployQueue("TestQueue");
          
          if (conn != null)
          {
             conn.close();
          }
       }
    }
    
    public void testExpireSameMessagesMultiple() throws Exception
    {
       final int NUM_MESSAGES = 5;
       
       Connection conn = null;
       
       try
       {    
       
          ServerManagement.deployQueue("ExpiryQueue");
          
          String defaultExpiryObjectName = "jboss.messaging.destination:service=Queue,name=ExpiryQueue";
             
          ObjectName serverPeerObjectName = ServerManagement.getServerPeerObjectName();
              
          ServerManagement.setAttribute(serverPeerObjectName, "DefaultExpiryQueue", defaultExpiryObjectName);
           
          Queue defaultExpiry = (Queue)ic.lookup("/queue/ExpiryQueue");
          
          drainDestination(cf, defaultExpiry);                   
         
          conn = cf.createConnection();
          
          conn.setClientID("wib1");
                         
          Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
    
          MessageProducer prod = sess.createProducer(topic);
             
          conn.start();
          
          //Create 3 durable subscriptions
          
          MessageConsumer sub1 = sess.createDurableSubscriber(topic, "sub1");
          
          MessageConsumer sub2 = sess.createDurableSubscriber(topic, "sub2");
          
          MessageConsumer sub3 = sess.createDurableSubscriber(topic, "sub3");
          
          Map origIds = new HashMap();
                            
          long now = System.currentTimeMillis();
          
          for (int i = 0; i < NUM_MESSAGES; i++)
          {
             TextMessage tm = sess.createTextMessage("Message:" + i);
 
             //Send messages with time to live of 3000 enough time to get to client consumer - so 
             //they won't be expired on the server side
             prod.send(tm, DeliveryMode.PERSISTENT, 4, 3000);
             
             origIds.put(tm.getText(), tm.getJMSMessageID());
          }
          
          long approxExpiry = now + 3000;
          
                   
          //Now sleep. This wil give them enough time to expire
          
          Thread.sleep(3500);
          
          //Now try and consume from each - this should force the message to the expiry queue
          
          Message m = sub1.receive(500);
          assertNull(m);
          
          m = sub2.receive(500);
          assertNull(m);
          
          m = sub3.receive(500);
          assertNull(m);
          
          //Now the messages should all be in the expiry queue
          
          MessageConsumer cons2 = sess.createConsumer(defaultExpiry);
          
          while (true)
          {
             TextMessage tm = (TextMessage)cons2.receive(500);
             
             if (tm == null)
             {
                break;
             }
             
             // Check the headers
             String origDest =
                tm.getStringProperty(ServerSessionEndpoint.JBOSS_MESSAGING_ORIG_DESTINATION);
             
             String origMessageId =
                tm.getStringProperty(ServerSessionEndpoint.JBOSS_MESSAGING_ORIG_MESSAGE_ID);
             
             long actualExpiryTime =
                tm.getLongProperty(ServerSessionEndpoint.JBOSS_MESSAGING_ACTUAL_EXPIRY_TIME);
             
             assertEquals(topic.toString(), origDest);
             
             String origId = (String)origIds.get(tm.getText());
             
             assertEquals(origId, origMessageId);
             
             assertTrue(actualExpiryTime >= approxExpiry);
          }
          
          cons2.close();
          
          sub1.close();
          
          sub2.close();
          
          sub3.close();
          
          sess.unsubscribe("sub1");
          
          sess.unsubscribe("sub2");
          
          sess.unsubscribe("sub3");
             
       }
       finally
       {        
          ServerManagement.undeployQueue("ExpiryQueue");
          
          if (conn != null)
          {
             conn.close();
          }
       }
    }
 
    public void testWithMessageListenerPersistent() throws Exception
    {
       testWithMessageListener(true);
    }
 
    public void testWithMessageListenerNonPersistent() throws Exception
    {
       testWithMessageListener(false);
    }
 
    public void testWithReceivePersistent() throws Exception
    {
       this.testWithReceive(true);
    }
 
    public void testWithReceiveNonPersistent() throws Exception
    {
       testWithReceive(false);
    }   
 
    public void testWithMessageListener(boolean persistent) throws Exception
    {            
       Connection conn = null;
       
       try
       {
          ServerManagement.deployQueue("ExpiryQueue");
    
          Queue expiryQueue = (Queue)ic.lookup("/queue/ExpiryQueue");
          
          drainDestination(cf, expiryQueue);
           
          final int NUM_MESSAGES = 5;
 
          conn = cf.createConnection();
          
          conn.start();
 
          Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
          MessageProducer prod = sess.createProducer(queue);
 
          int deliveryMode = persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
          
          for (int i = 0; i < NUM_MESSAGES; i++)
          {
             TextMessage tm = sess.createTextMessage("Message:" + i);
                         
             //Send messages with time to live of 2000 enough time to get to client consumer - so 
             //they won't be expired on the server side
             prod.send(tm, deliveryMode, 4, 2000);
          }
 
          MessageConsumer cons = sess.createConsumer(queue);
          
          //The messages should now be sitting in the consumer buffer
          
          //Now give them enough time to expire
          
          Thread.sleep(2500);
          
          //Now set a listener
          
          FailingMessageListener listener  = new FailingMessageListener();
 
          cons.setMessageListener(listener);
          
          Thread.sleep(1000);
 
          cons.setMessageListener(null);
          
          //No messages should have been received
          assertEquals(0, listener.deliveryCount);
                   
          //Shouldn't be able to receive any more
          
          Message m = cons.receive(1000);
 
          assertNull(m);
 
          //Message should all be in the expiry queue - let's check
          
          MessageConsumer cons2 = sess.createConsumer(expiryQueue);
          
          for (int i = 0; i < NUM_MESSAGES; i++)
          {
             TextMessage tm = (TextMessage)cons2.receive(1000);
             
             assertNotNull(tm);
 
             assertEquals("Message:" + i, tm.getText());
          }
 
       }
       finally
       {
          ServerManagement.undeployQueue("ExpiryQueue");
 
          if (conn != null) conn.close();
       }
    }
    
    public void testWithReceive(boolean persistent) throws Exception
    {
       Connection conn = null;
       
       try
       {
 
          ServerManagement.deployQueue("ExpiryQueue");
    
          Queue expiryQueue = (Queue)ic.lookup("/queue/ExpiryQueue");
          
          drainDestination(cf, expiryQueue);
           
          final int NUM_MESSAGES = 5;
 
          conn = cf.createConnection();
          
          conn.start();
 
          Session sess = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
          MessageProducer prod = sess.createProducer(queue);
 
          int deliveryMode = persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT;
          
          for (int i = 0; i < NUM_MESSAGES; i++)
          {
             TextMessage tm = sess.createTextMessage("Message:" + i);
                         
             //Send messages with time to live of 2000 enough time to get to client consumer - so 
             //they won't be expired on the server side
             prod.send(tm, deliveryMode, 4, 2000);
          }
 
          MessageConsumer cons = sess.createConsumer(queue);
          
          //The messages should now be sitting in the consumer buffer
          
          //Now give them enough time to expire
          
          Thread.sleep(2500);
          
          //Now try and receive
          
          Message m = cons.receive(1000);
 
          assertNull(m);
 
          //Message should all be in the expiry queue - let's check
          
          MessageConsumer cons2 = sess.createConsumer(expiryQueue);
          
          for (int i = 0; i < NUM_MESSAGES; i++)
          {
             TextMessage tm = (TextMessage)cons2.receive(1000);
             
             assertNotNull(tm);
 
             assertEquals("Message:" + i, tm.getText());
          }
 
       }
       finally
       {
          ServerManagement.undeployQueue("ExpiryQueue");
 
          if (conn != null) conn.close();
       }
    }
    
    public void testExpirationTransfer() throws Exception
    {
 
       ServerManagement.deployQueue("expiryQueue");
 
       Object originalValue = ServerManagement.getAttribute(ServerManagement.getServerPeerObjectName(), "DefaultExpiryQueue");
 
       ServerManagement.setAttribute(ServerManagement.getServerPeerObjectName(), "DefaultExpiryQueue", "jboss.messaging.destination:service=Queue,name=expiryQueue");
 
       Connection conn = null;
 
       try
       {
 
          ConnectionFactory cf = (ConnectionFactory)ic.lookup("/ConnectionFactory");
          
          conn = cf.createConnection();
 
          Session session = conn.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
          conn.start();
 
          MessageProducer prod = session.createProducer(queue);
          prod.setTimeToLive(100);
 
          Message m = session.createTextMessage("This message will die");
 
          prod.send(m);
 
          // wait for the message to die
          Thread.sleep(2000);
 
          MessageConsumer cons = session.createConsumer(queue);
 
          assertNull(cons.receive(3000));
          
          Queue queueExpiryQueue = (Queue)ic.lookup("/queue/expiryQueue");
 
          MessageConsumer consumerExpiredQueue = session.createConsumer(queueExpiryQueue);
 
          TextMessage txt = (TextMessage) consumerExpiredQueue.receive(1000);
 
          assertEquals("This message will die", txt.getText());
 
          assertNull(consumerExpiredQueue.receive(1000));
       }
       finally
       {
          if (conn != null)
          {
             conn.close();
          }
         
         ServerManagement.destroyQueue("expiryQueue");
          
          ServerManagement.setAttribute(ServerManagement.getServerPeerObjectName(), "DefaultExpiryQueue", originalValue.toString());
       }
    }
 
       
    // Package protected ---------------------------------------------
 
    // Protected -----------------------------------------------------
 
    protected void setUp() throws Exception
    {
       super.setUp();
 
       ServerManagement.start("all");
 
       ic = new InitialContext(ServerManagement.getJNDIEnvironment());
 
       cf = (ConnectionFactory)ic.lookup("/ConnectionFactory");
 
       ServerManagement.undeployQueue("Queue");
       
       ServerManagement.undeployTopic("Topic");
       
       ServerManagement.deployQueue("Queue");
       
       ServerManagement.deployTopic("Topic");
 
       queue = (Queue)ic.lookup("/queue/Queue");
       
       topic = (Topic)ic.lookup("/topic/Topic");
 
    }
 
    protected void tearDown() throws Exception
    {
       super.tearDown();
 
       ServerManagement.undeployQueue("Queue");
       
       ServerManagement.undeployTopic("Topic");
 
       if (ic != null) ic.close();
    }
 
    // Private -------------------------------------------------------
 
    // Inner classes -------------------------------------------------
    
    class FailingMessageListener implements MessageListener
    {
       volatile int deliveryCount;
 
       public void onMessage(Message msg)
       {
          deliveryCount++;
          
          throw new RuntimeException("Your mum!");
       }
       
    }
 
 }
