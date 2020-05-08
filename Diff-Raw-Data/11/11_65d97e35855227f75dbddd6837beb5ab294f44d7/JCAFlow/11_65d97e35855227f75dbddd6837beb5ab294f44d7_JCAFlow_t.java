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
 package org.apache.servicemix.jbi.nmr.flow.jca;
 
 import java.io.Serializable;
 import java.util.Map;
 import java.util.Set;
 
 import javax.jbi.JBIException;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.MessageExchange.Role;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.DeliveryMode;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.MessageProducer;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 import javax.jms.Topic;
 import javax.resource.spi.BootstrapContext;
 import javax.resource.spi.ConnectionManager;
 import javax.resource.spi.ResourceAdapter;
 import javax.resource.spi.ResourceAdapterInternalException;
 import javax.transaction.Status;
 import javax.transaction.SystemException;
 import javax.transaction.TransactionManager;
 
 import org.apache.activemq.advisory.AdvisorySupport;
 import org.apache.activemq.command.ActiveMQDestination;
 import org.apache.activemq.command.ActiveMQMessage;
 import org.apache.activemq.command.ActiveMQQueue;
 import org.apache.activemq.command.ActiveMQTopic;
 import org.apache.activemq.command.ConsumerId;
 import org.apache.activemq.command.ConsumerInfo;
 import org.apache.activemq.command.RemoveInfo;
 import org.apache.activemq.ra.ActiveMQActivationSpec;
 import org.apache.activemq.ra.ActiveMQManagedConnectionFactory;
 import org.apache.activemq.ra.ActiveMQResourceAdapter;
 import org.apache.geronimo.connector.BootstrapContextImpl;
 import org.apache.geronimo.connector.outbound.connectionmanagerconfig.SinglePool;
 import org.apache.geronimo.connector.outbound.connectionmanagerconfig.XATransactions;
 import org.apache.geronimo.connector.work.GeronimoWorkManager;
 import org.apache.geronimo.transaction.context.TransactionContextManager;
 import org.apache.servicemix.JbiConstants;
 import org.apache.servicemix.jbi.container.SpringJBIContainer;
 import org.apache.servicemix.jbi.event.ComponentAdapter;
 import org.apache.servicemix.jbi.event.ComponentEvent;
 import org.apache.servicemix.jbi.event.ComponentListener;
 import org.apache.servicemix.jbi.event.EndpointAdapter;
 import org.apache.servicemix.jbi.event.EndpointEvent;
 import org.apache.servicemix.jbi.event.EndpointListener;
 import org.apache.servicemix.jbi.messaging.MessageExchangeImpl;
 import org.apache.servicemix.jbi.nmr.Broker;
 import org.apache.servicemix.jbi.nmr.flow.AbstractFlow;
 import org.apache.servicemix.jbi.servicedesc.EndpointSupport;
 import org.apache.servicemix.jbi.servicedesc.InternalEndpoint;
 import org.jencks.JCAConnector;
 import org.jencks.SingletonEndpointFactory;
 import org.jencks.factory.ConnectionManagerFactoryBean;
 import org.springframework.context.ApplicationContext;
 
 import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
 import edu.emory.mathcs.backport.java.util.concurrent.CopyOnWriteArraySet;
 import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * Use for message routing among a network of containers. All routing/registration happens automatically.
  * 
  * @version $Revision$
  * @org.apache.xbean.XBean element="jcaFlow"
  */
 public class JCAFlow extends AbstractFlow implements MessageListener {
     
     private static final String INBOUND_PREFIX = "org.apache.servicemix.jca.";
     private String jmsURL = "tcp://localhost:61616";
     private String userName;
     private String password;
     private ConnectionFactory connectionFactory;
     private Connection connection;
     private String broadcastDestinationName = "org.apache.servicemix.JCAFlow";
     private Topic broadcastTopic;
     private Map connectorMap = new ConcurrentHashMap();
     private AtomicBoolean started = new AtomicBoolean(false);
     private Set subscriberSet=new CopyOnWriteArraySet();
     private TransactionContextManager transactionContextManager;
     private ConnectionManager connectionManager;
     private BootstrapContext bootstrapContext;
     private ResourceAdapter resourceAdapter;
     private JCAConnector containerConnector;
     private JCAConnector broadcastConnector;
     private Session broadcastSession;
     private Topic advisoryTopic;
     private MessageConsumer advisoryConsumer;
 
     private EndpointListener endpointListener;
 
     private ComponentListener componentListener;
 
     /**
      * The type of Flow
      * 
      * @return the type
      */
     public String getDescription() {
         return "jca";
     }
 
     /**
      * Returns the JMS URL for this flow
      * 
      * @return Returns the jmsURL.
      */
     public String getJmsURL() {
         return jmsURL;
     }
 
     /**
      * Sets the JMS URL for this flow
      * 
      * @param jmsURL The jmsURL to set.
      */
     public void setJmsURL(String jmsURL) {
         this.jmsURL = jmsURL;
     }
 
     /**
      * Returns the password for this flow
      * 
      * @return Returns the password.
      */
     public String getPassword() {
         return password;
     }
 
     /**
      * Sets the password for this flow
      * 
      * @param password The password to set.
      */
     public void setPassword(String password) {
         this.password = password;
     }
 
     /**
      * Sets the User Name for this flow
      * 
      * @return Returns the userName.
      */
     public String getUserName() {
         return userName;
     }
 
     /**
      * Returns the User Name for this flow
      * 
      * @param userName The userName to set.
      */
     public void setUserName(String userName) {
         this.userName = userName;
     }
 
     /**
      * Returns the ConnectionFactory for this flow
      * 
      * @return Returns the connectionFactory.
      */
     public ConnectionFactory getConnectionFactory() {
         return connectionFactory;
     }
 
     /**
      * Sets the ConnectionFactory for this flow
      * 
      * @param connectionFactory The connectionFactory to set.
      */
     public void setConnectionFactory(ConnectionFactory connectoFactory) {
         this.connectionFactory = connectoFactory;
     }
 
     /**
      * Returns the Broadcast Destination Name for this flow
      * 
      * @return Returns the broadcastDestinationName.
      */
     public String getBroadcastDestinationName() {
         return broadcastDestinationName;
     }
 
     /**
      * Sets the Broadcast Destination Name for this flow
      * 
      * @param broadcastDestinationName The broadcastDestinationName to set.
      */
     public void setBroadcastDestinationName(String broadcastDestinationName) {
         this.broadcastDestinationName = broadcastDestinationName;
     }
 
     protected ResourceAdapter createResourceAdapter() throws ResourceAdapterInternalException {
     	ActiveMQResourceAdapter ra = new ActiveMQResourceAdapter();
     	ra.setServerUrl(jmsURL);
     	ra.start(getBootstrapContext());
     	return ra;
     }
     
     public TransactionManager getTransactionManager() {
     	return (TransactionManager) broker.getContainer().getTransactionManager();
     }
     
     /**
      * Initialize the Region
      * 
      * @param broker
      * @throws JBIException
      */
     public void init(Broker broker) throws JBIException {
         log.info(broker.getContainer().getName() + ": Initializing jca flow");
         super.init(broker);
         // Create and register endpoint listener
         endpointListener = new EndpointAdapter() {
             public void internalEndpointRegistered(EndpointEvent event) {
                 onInternalEndpointRegistered(event, true);
             }
 
             public void internalEndpointUnregistered(EndpointEvent event) {
                 onInternalEndpointUnregistered(event, true);
             }
         };
         broker.getContainer().addListener(endpointListener);
         // Create and register component listener
         componentListener = new ComponentAdapter() {
             public void componentStarted(ComponentEvent event) {
                 onComponentStarted(event);
             }
             public void componentStopped(ComponentEvent event) {
                 onComponentStopped(event);
             }
         };
         broker.getContainer().addListener(componentListener);
         try {
         	resourceAdapter = createResourceAdapter();
         	
         	// Inbound connector
         	ActiveMQActivationSpec ac = new ActiveMQActivationSpec();
         	ac.setDestinationType("javax.jms.Queue");
         	ac.setDestination(INBOUND_PREFIX + broker.getContainer().getName());
         	containerConnector = new JCAConnector();
         	containerConnector.setBootstrapContext(getBootstrapContext());
         	containerConnector.setActivationSpec(ac);
         	containerConnector.setResourceAdapter(resourceAdapter);
         	containerConnector.setEndpointFactory(new SingletonEndpointFactory(this, getTransactionManager()));
        	containerConnector.start();
         	
         	// Outbound connector
         	ActiveMQManagedConnectionFactory mcf = new ActiveMQManagedConnectionFactory();
         	mcf.setResourceAdapter(resourceAdapter);
         	connectionFactory = (ConnectionFactory) mcf.createConnectionFactory(getConnectionManager());
         	
         	// Outbound broadcast
         	connection = ((ActiveMQResourceAdapter) resourceAdapter).makeConnection();
         	connection.start();
         	broadcastTopic = new ActiveMQTopic(broadcastDestinationName);
             
             broadcastSession=connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
             broadcastTopic = new ActiveMQTopic(broadcastDestinationName);
             advisoryTopic=AdvisorySupport.getConsumerAdvisoryTopic((ActiveMQDestination) broadcastTopic);
         }
         catch (Exception e) {
             log.error("Failed to initialize JCAFlow", e);
             throw new JBIException(e);
         }
     }
 
     /**
      * start the flow
      * 
      * @throws JBIException
      */
     public void start() throws JBIException {
         if (started.compareAndSet(false, true)) {
             super.start();
             try {
                 // Inbound broadcast
                 ActiveMQActivationSpec ac = new ActiveMQActivationSpec();
                 ac.setDestinationType("javax.jms.Topic");
                 ac.setDestination(broadcastDestinationName);
                 broadcastConnector = new JCAConnector();
                 broadcastConnector.setBootstrapContext(getBootstrapContext());
                 broadcastConnector.setActivationSpec(ac);
                 broadcastConnector.setResourceAdapter(resourceAdapter);
                 broadcastConnector.setEndpointFactory(new SingletonEndpointFactory(new MessageListener() {
                     public void onMessage(Message message) {
                         try {
                             Object obj = ((ObjectMessage) message).getObject();
                             if (obj instanceof EndpointEvent) {
                                 EndpointEvent event = (EndpointEvent) obj;
                                 String container = ((InternalEndpoint) event.getEndpoint()).getComponentNameSpace().getContainerName();
                                 if (!getBroker().getContainer().getName().equals(container)) {
                                     if (event.getEventType() == EndpointEvent.INTERNAL_ENDPOINT_REGISTERED) {
                                         onRemoteEndpointRegistered(event);
                                     } else if (event.getEventType() == EndpointEvent.INTERNAL_ENDPOINT_UNREGISTERED) {
                                         onRemoteEndpointUnregistered(event);
                                     }
                                 }
                             }
                         } catch (Exception e) {
                             log.error("Error processing incoming broadcast message", e);
                         }
                     }
                 }));
                broadcastConnector.start();
                 
                 advisoryConsumer = broadcastSession.createConsumer(advisoryTopic);
                 advisoryConsumer.setMessageListener(new MessageListener() {
                     public void onMessage(Message message) {
                         if (started.get()) {
                             onAdvisoryMessage(((ActiveMQMessage) message).getDataStructure());
                         }
                     }
                 });
             }
             catch (Exception e) {
                 throw new JBIException("JMSException caught in start: " + e.getMessage(), e);
             }
         }
     }
 
     /**
      * stop the flow
      * 
      * @throws JBIException
      */
     public void stop() throws JBIException {
         if (started.compareAndSet(true, false)) {
             super.stop();
             try {
                 advisoryConsumer.close();
             }
             catch (JMSException e) {
                 JBIException jbiEx = new JBIException("JMSException caught in stop: " + e.getMessage());
                 throw jbiEx;
             }
         }
     }
 
     public void shutDown() throws JBIException {
         super.shutDown();
         stop();
         // Remove endpoint listener
         broker.getContainer().removeListener(endpointListener);
         // Remove component listener
         broker.getContainer().removeListener(componentListener);
         // Destroy connectors
         while (!connectorMap.isEmpty()) {
         	JCAConnector connector = (JCAConnector) connectorMap.remove(connectorMap.keySet().iterator().next());
         	try {
         		connector.destroy();
         	} catch (Exception e) {
         		log.warn("error closing jca connector", e);
         	}
         }
         try {
         	containerConnector.destroy();
     	} catch (Exception e) {
     		log.warn("error closing jca connector", e);
         }
         try {
         	broadcastConnector.destroy();
     	} catch (Exception e) {
     		log.warn("error closing jca connector", e);
         }
         // Destroy the resource adapter
     	resourceAdapter.stop();
         if (this.connection != null) {
             try {
                 this.connection.close();
             }
             catch (JMSException e) {
                 log.warn("error closing JMS Connection", e);
             }
         }
     }
 
     /**
      * useful for testing
      * 
      * @return number of containers in the network
      */
     public int numberInNetwork() {
         return subscriberSet.size();
     }
 
     /**
      * Check if the flow can support the requested QoS for this exchange
      * @param me the exchange to check
      * @return true if this flow can handle the given exchange
      */
     public boolean canHandle(MessageExchange me) {
         if (isTransacted(me) && isSynchronous(me)) {
             return false;
         }
         return true;
     }
     
     public void onInternalEndpointRegistered(EndpointEvent event, boolean broadcast) {
         if (!started.get()) {
             return;
         }
         try {
             String key = EndpointSupport.getKey(event.getEndpoint());
             if(!connectorMap.containsKey(key)){
                 ActiveMQActivationSpec ac = new ActiveMQActivationSpec();
                 ac.setDestinationType("javax.jms.Queue");
                 ac.setDestination(INBOUND_PREFIX + key);
                 JCAConnector connector = new JCAConnector();
                 connector.setBootstrapContext(getBootstrapContext());
                 connector.setActivationSpec(ac);
                 connector.setResourceAdapter(resourceAdapter);
                 connector.setEndpointFactory(new SingletonEndpointFactory(this, getTransactionManager()));
                connector.start();
                 connectorMap.put(key, connector);
             }
             // broadcast change to the network
             if (broadcast) {
                 log.info(broker.getContainer().getName() + ": broadcasting info for " + event);
                 sendJmsMessage(broadcastTopic, event, false, false);
             }
         } catch (Exception e) {
             log.error("Cannot create consumer for " + event.getEndpoint(), e);
         }
     }
     
     public void onInternalEndpointUnregistered(EndpointEvent event, boolean broadcast) {
         try{
             String key = EndpointSupport.getKey(event.getEndpoint());
             JCAConnector connector=(JCAConnector) connectorMap.remove(key);
             if(connector!=null){
                 connector.destroy();
             }
             // broadcast change to the network
             if (broadcast) {
                 log.info(broker.getContainer().getName() + ": broadcasting info for " + event);
                 sendJmsMessage(broadcastTopic, event, false, false);
             }
         } catch (Exception e) {
             log.error("Cannot destroy consumer for " + event, e);
         }
     }
     
     public void onComponentStarted(ComponentEvent event) {
         if (!started.get()) {
             return;
         }
         try {
             String key = event.getComponent().getName();
             if(!connectorMap.containsKey(key)){
                 ActiveMQActivationSpec ac = new ActiveMQActivationSpec();
                 ac.setDestinationType("javax.jms.Queue");
                 ac.setDestination(INBOUND_PREFIX + key);
                 JCAConnector connector = new JCAConnector();
                 connector.setBootstrapContext(getBootstrapContext());
                 connector.setActivationSpec(ac);
                 connector.setResourceAdapter(resourceAdapter);
                 connector.setEndpointFactory(new SingletonEndpointFactory(this, getTransactionManager()));
                connector.start();
                 connectorMap.put(key, connector);
             }
         } catch (Exception e) {
             log.error("Cannot create consumer for component " + event.getComponent().getName(), e);
         }
     }
     
     public void onComponentStopped(ComponentEvent event) {
         try {
             String key = event.getComponent().getName();
             JCAConnector connector = (JCAConnector) connectorMap.remove(key);
             if (connector != null){
                 connector.destroy();
             }
         } catch (Exception e) {
             log.error("Cannot destroy consumer for component " + event.getComponent().getName(), e);
         }
     }
 
     public void onRemoteEndpointRegistered(EndpointEvent event) {
         log.info(broker.getContainer().getName() + ": adding remote endpoint: " + event.getEndpoint());
         broker.getContainer().getRegistry().registerRemoteEndpoint(event.getEndpoint());
     }
 
     public void onRemoteEndpointUnregistered(EndpointEvent event) {
         log.info(broker.getContainer().getName() + ": removing remote endpoint: " + event.getEndpoint());
         broker.getContainer().getRegistry().unregisterRemoteEndpoint(event.getEndpoint());
     }
 
     /**
      * Distribute an ExchangePacket
      * 
      * @param me
      * @throws MessagingException
      */
     protected void doSend(MessageExchangeImpl me) throws MessagingException {
         doRouting(me);
     }
     
     /**
      * Distribute an ExchangePacket
      * 
      * @param me
      * @throws MessagingException
      */
     public void doRouting(final MessageExchangeImpl me) throws MessagingException {
         // let ActiveMQ do the routing ...
         try {
             String destination;
             if (me.getRole() == Role.PROVIDER) {
                 if (me.getDestinationId() == null) {
                     destination = INBOUND_PREFIX + EndpointSupport.getKey(me.getEndpoint());
                 } else if (Boolean.TRUE.equals(me.getProperty(JbiConstants.STATELESS_PROVIDER)) && !isSynchronous(me)) {
                     destination = INBOUND_PREFIX + me.getDestinationId().getName();
                 } else {
                     destination = INBOUND_PREFIX + me.getDestinationId().getContainerName();
                 }
             } else {
                 if (me.getSourceId() == null) {
                     throw new IllegalStateException("No sourceId set on the exchange");
                 } else if (Boolean.TRUE.equals(me.getProperty(JbiConstants.STATELESS_CONSUMER)) && !isSynchronous(me)) {
                     // If the consumer is stateless and has specified a sender endpoint,
                     // this exchange will be sent to the given endpoint queue, so that
                     // This property must have been created using EndpointSupport.getKey
                     // fail-over and load-balancing can be achieved
                     if (me.getProperty(JbiConstants.SENDER_ENDPOINT) != null) {
                         destination = INBOUND_PREFIX + me.getProperty(JbiConstants.SENDER_ENDPOINT);
                     } else {
                         destination = INBOUND_PREFIX + me.getSourceId().getName();
                     }
                 } else {
                     destination = INBOUND_PREFIX + me.getSourceId().getContainerName();
                 }
             }
             if (me.isTransacted()) {
                 me.setTxState(MessageExchangeImpl.TX_STATE_ENLISTED);
             }
             sendJmsMessage(new ActiveMQQueue(destination), me, isPersistent(me), me.isTransacted());
         } catch (JMSException e) {
             log.error("Failed to send exchange: " + me + " internal JMS Network", e);
             throw new MessagingException(e);
         } catch (SystemException e) {
             log.error("Failed to send exchange: " + me + " transaction problem", e);
             throw new MessagingException(e);
         }
     }
 
     /**
      * MessageListener implementation
      * 
      * @param message
      */
     public void onMessage(Message message) {
         try {
             if (message != null && started.get()) {
                 ObjectMessage objMsg = (ObjectMessage) message;
                 final MessageExchangeImpl me = (MessageExchangeImpl) objMsg.getObject();
                 // Hack for redelivery: AMQ is too optimized and the object is the same upon redelivery
                 // so that there are side effect (the exchange state may have been modified)
                 // See http://jira.activemq.org/jira/browse/AMQ-519
                 //me = (MessageExchangeImpl) ((ActiveMQObjectMessage) ((ActiveMQObjectMessage) message).copy()).getObject();
                 TransactionManager tm = (TransactionManager) getTransactionManager();
                 if (tm != null) {
                     me.setTransactionContext(tm.getTransaction());
                 }
                 if (me.getDestinationId() == null) {
                     ServiceEndpoint se = me.getEndpoint();
                     se = broker.getContainer().getRegistry()
                             .getInternalEndpoint(se.getServiceName(), se.getEndpointName());
                     me.setEndpoint(se);
                     me.setDestinationId(((InternalEndpoint) se).getComponentNameSpace());
                 }
                 super.doRouting(me);
             }
         }
         catch (JMSException jmsEx) {
             log.error("Caught an exception unpacking JMS Message: ", jmsEx);
         }
         catch (MessagingException e) {
             log.error("Caught an exception routing ExchangePacket: ", e);
         } 
         catch (SystemException e) {
             log.error("Caught an exception acessing transaction context: ", e);
 		}
     }
 
     protected void onAdvisoryMessage(Object obj) {
         if (obj instanceof ConsumerInfo) {
             ConsumerInfo info = (ConsumerInfo) obj;
             subscriberSet.add(info.getConsumerId().getConnectionId());
             ServiceEndpoint[] endpoints = broker.getContainer().getRegistry().getEndpointsForInterface(null);
             for (int i = 0; i < endpoints.length; i++) {
                 if (endpoints[i] instanceof InternalEndpoint && ((InternalEndpoint) endpoints[i]).isLocal()) {
                     onInternalEndpointRegistered(new EndpointEvent(endpoints[i],
                             EndpointEvent.INTERNAL_ENDPOINT_REGISTERED), true);
                 }
             }
         } else if (obj instanceof RemoveInfo) {
             ConsumerId id = (ConsumerId) ((RemoveInfo) obj).getObjectId();
             subscriberSet.remove(id.getConnectionId());
             removeAllPackets(id.getConnectionId());
         }
     }
 
     private void removeAllPackets(String containerName) {
         //TODO: broker.getRegistry().unregisterRemoteEndpoints(containerName);
     }
 
 	public ConnectionManager getConnectionManager() throws Exception {
 		if (connectionManager == null) {
         	ConnectionManagerFactoryBean cmfb = new ConnectionManagerFactoryBean();
         	cmfb.setTransactionContextManager(getTransactionContextManager());
         	cmfb.setPoolingSupport(new SinglePool(
         			16, // max size 
         			0, // min size
         			100, // blockingTimeoutMilliseconds
                     1, // idleTimeoutMinutes 
                     true, // matchOne
                     true,  // matchAll
                     true)); // selectOneAssumeMatch
         	cmfb.setTransactionSupport(new XATransactions(
         			true, // useTransactionCaching
         			false)); // useThreadCaching
         	cmfb.afterPropertiesSet();
 			connectionManager = (ConnectionManager) cmfb.getObject();
 		}
 		return connectionManager;
 	}
 
 	public void setConnectionManager(ConnectionManager connectionManager) {
 		this.connectionManager = connectionManager;
 	}
 
 	public TransactionContextManager getTransactionContextManager() {
         if (transactionContextManager == null) {
             if (broker != null && broker.getContainer() instanceof SpringJBIContainer) {
                 ApplicationContext applicationContext = ((SpringJBIContainer) broker.getContainer()).getApplicationContext();
                 if (applicationContext != null) {
                     Map map = applicationContext.getBeansOfType(TransactionContextManager.class);
                     if( map.size() == 1) {
                         transactionContextManager = (TransactionContextManager) map.values().iterator().next();
                     }
                 }
             }
         }
 		return transactionContextManager;
 	}
 
 	public void setTransactionContextManager(
 			TransactionContextManager transactionContextManager) {
 		this.transactionContextManager = transactionContextManager;
 	}
 
 	public BootstrapContext getBootstrapContext() {
 		if (bootstrapContext == null) {
 	    	GeronimoWorkManager wm = (GeronimoWorkManager) broker.getContainer().getWorkManager();
 	    	bootstrapContext = new BootstrapContextImpl(wm);
 		}
 		return bootstrapContext;
 	}
 
 	public void setBootstrapContext(BootstrapContext bootstrapContext) {
 		this.bootstrapContext = bootstrapContext;
 	}
     
     public String toString(){
         return broker.getContainer().getName() + " JCAFlow";
     }
     
     private void sendJmsMessage(Destination dest, Serializable object, boolean persistent, boolean transacted) throws JMSException, SystemException {
         if (transacted) {
             TransactionManager tm = (TransactionManager) getBroker().getContainer().getTransactionManager();
             if (tm.getStatus() == Status.STATUS_MARKED_ROLLBACK) {
                 return;
             }
         }
     	Connection connection = connectionFactory.createConnection();
     	try {
     		Session session = connection.createSession(transacted, transacted ? Session.SESSION_TRANSACTED : Session.AUTO_ACKNOWLEDGE);
     		ObjectMessage msg = session.createObjectMessage(object);
     		MessageProducer producer = session.createProducer(dest);
     		producer.setDeliveryMode(persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
     		producer.send(msg);
     	} finally {
     		connection.close();
     	}
     }
 
 }
