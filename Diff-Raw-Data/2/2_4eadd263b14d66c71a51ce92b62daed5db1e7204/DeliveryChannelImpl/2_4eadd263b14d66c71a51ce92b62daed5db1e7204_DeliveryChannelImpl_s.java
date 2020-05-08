 /*
  * Copyright 2005-2006 The Apache Software Foundation.
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
 package org.apache.servicemix.jbi.messaging;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.jbi.component.Component;
 import javax.jbi.messaging.DeliveryChannel;
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessageExchangeFactory;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.MessageExchange.Role;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.transaction.Transaction;
 import javax.transaction.TransactionManager;
 import javax.xml.namespace.QName;
 import javax.xml.transform.dom.DOMSource;
 
 import org.apache.activemq.util.IdGenerator;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.servicemix.JbiConstants;
 import org.apache.servicemix.MessageExchangeListener;
 import org.apache.servicemix.jbi.ExchangeTimeoutException;
 import org.apache.servicemix.jbi.container.ActivationSpec;
 import org.apache.servicemix.jbi.container.JBIContainer;
 import org.apache.servicemix.jbi.framework.ComponentConnector;
 import org.apache.servicemix.jbi.framework.ComponentContextImpl;
 import org.apache.servicemix.jbi.framework.LocalComponentConnector;
 import org.apache.servicemix.jbi.jaxp.SourceTransformer;
 import org.apache.servicemix.jbi.util.BoundedLinkedQueue;
 import org.apache.servicemix.jbi.util.DOMUtil;
 import org.w3c.dom.Node;
 
 import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
 import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * DeliveryChannel implementation
  * 
  * @version $Revision$
  */
 public class DeliveryChannelImpl implements DeliveryChannel {
 
     private static final Log log = LogFactory.getLog(DeliveryChannel.class);
 
     private JBIContainer container;
     private ComponentContextImpl context;
     private LocalComponentConnector componentConnector;
     private BoundedLinkedQueue queue = new BoundedLinkedQueue();
     private IdGenerator idGenerator = new IdGenerator();
     private MessageExchangeFactory inboundFactory;
     private MessagingStats messagingStats;
     private boolean exchangeThrottling;
     private long throttlingTimeout = 100;
     private int throttlingInterval = 1;
     private int intervalCount = 0;
     private long lastSendTime = System.currentTimeMillis();
     private long lastReceiveTime = System.currentTimeMillis();
     private AtomicBoolean closed = new AtomicBoolean(false);
     private Map waiters = new ConcurrentHashMap();
     private Map exchangesById = new ConcurrentHashMap();
 
     /**
      * Constructor
      * 
      * @param container
      * @param componentName
      */
     public DeliveryChannelImpl(JBIContainer container, String componentName) {
         this.container = container;
         this.messagingStats = new MessagingStats(componentName);
     }
 
     /**
      * @return size of the inbound Queue
      */
     public int getQueueSize() {
         return queue.size();
     }
 
     /**
      * @return the capacity of the inbound queue
      */
     public int getQueueCapacity() {
         return queue.capacity();
     }
 
     /**
      * Set the inbound queue capacity
      * 
      * @param value
      */
     public void setQueueCapacity(int value) {
         queue.setCapacity(value);
     }
 
     /**
      * close the delivery channel
      * 
      * @throws MessagingException
      */
     public void close() throws MessagingException {
         if (this.closed.compareAndSet(false, true)) {
             List pending = queue.closeAndFlush();
             for (Iterator iter = pending.iterator(); iter.hasNext();) {
                 MessageExchangeImpl messageExchange = (MessageExchangeImpl) iter.next();
                 if (messageExchange.getTransactionContext() != null && messageExchange.getMirror().getSyncState() == MessageExchangeImpl.SYNC_STATE_SYNC_SENT) {
                     synchronized (messageExchange.getMirror()) {
                         if (log.isDebugEnabled()) {
                             log.debug("Notifying: " + messageExchange.getExchangeId());
                         }
                         messageExchange.getMirror().notify();
                     }
                 }
             }
             // Interrupt all blocked thread
             Object[] threads = waiters.keySet().toArray();
             for (int i = 0; i < threads.length; i++) {
                 ((Thread) threads[i]).interrupt();
             }
             // TODO: deactivate all endpoints from this component
             // TODO: Cause all accepts to return null
             // TODO: Abort all pending exchanges
         }
     }
 
     protected void checkNotClosed() throws MessagingException {
         if (closed.get()) {
             throw new MessagingException("DeliveryChannel has been closed.");
         }
     }
 
     /**
      * Create a message exchange factory. This factory will create exchange instances with all appropriate properties
      * set to null.
      * 
      * @return a message exchange factory
      */
     public MessageExchangeFactory createExchangeFactory() {
         MessageExchangeFactoryImpl result = createMessageExchangeFactory();
         result.setContext(context);
         ActivationSpec activationSpec = context.getActivationSpec();
         if (activationSpec != null) {
             String componentName = context.getComponentNameSpace().getName();
             // lets auto-default the container-routing information
             QName serviceName = activationSpec.getDestinationService();
             if (serviceName != null) {
                 result.setServiceName(serviceName);
                 log.info("default destination serviceName for " + componentName + " = " + serviceName);
             }
             QName interfaceName = activationSpec.getDestinationInterface();
             if (interfaceName != null) {
                 result.setInterfaceName(interfaceName);
                 log.info("default destination interfaceName for " + componentName + " = " + interfaceName);
             }
             QName operationName = activationSpec.getDestinationOperation();
             if (operationName != null) {
                 result.setOperationName(operationName);
                 log.info("default destination operationName for " + componentName + " = " + operationName);
             }
             String endpointName = activationSpec.getDestinationEndpoint();
             if (endpointName != null) {
                 boolean endpointSet = false;
                 log.info("default destination endpointName for " + componentName + " = " + endpointName);
                 if (serviceName != null && endpointName != null) {
                     endpointName = endpointName.trim();
                     ServiceEndpoint[] endpoints = container.getRegistry().getEndpointsForService(serviceName);
                     if (endpoints != null) {
                         for (int i = 0;i < endpoints.length;i++) {
                             if (endpoints[i].getEndpointName().equals(endpointName)) {
                                 result.setEndpoint(endpoints[i]);
                                 log.info("Set default destination endpoint for " + componentName + " to "
                                         + endpoints[i]);
                                 endpointSet = true;
                                 break;
                             }
                         }
                     }
                 }
                 if (!endpointSet) {
                     log.warn("Could not find destination endpoint for " + componentName + " service(" + serviceName
                             + ") with endpointName " + endpointName);
                 }
             }
         }
         return result;
     }
 
     /**
      * Create a message exchange factory for the given interface name.
      * 
      * @param interfaceName name of the interface for which all exchanges created by the returned factory will be set
      * @return an exchange factory that will create exchanges for the given interface; must be non-null
      */
     public MessageExchangeFactory createExchangeFactory(QName interfaceName) {
         MessageExchangeFactoryImpl result = createMessageExchangeFactory();
         result.setInterfaceName(interfaceName);
         return result;
     }
 
     /**
      * Create a message exchange factory for the given service name.
      * 
      * @param serviceName name of the service for which all exchanges created by the returned factory will be set
      * @return an exchange factory that will create exchanges for the given service; must be non-null
      */
     public MessageExchangeFactory createExchangeFactoryForService(QName serviceName) {
         MessageExchangeFactoryImpl result = createMessageExchangeFactory();
         result.setServiceName(serviceName);
         return result;
     }
 
     /**
      * Create a message exchange factory for the given endpoint.
      * 
      * @param endpoint endpoint for which all exchanges created by the returned factory will be set for
      * @return an exchange factory that will create exchanges for the given endpoint
      */
     public MessageExchangeFactory createExchangeFactory(ServiceEndpoint endpoint) {
         MessageExchangeFactoryImpl result = createMessageExchangeFactory();
         result.setEndpoint(endpoint);
         return result;
     }
 
     protected MessageExchangeFactoryImpl createMessageExchangeFactory() {
         MessageExchangeFactoryImpl messageExchangeFactory = new MessageExchangeFactoryImpl(idGenerator, closed);
         messageExchangeFactory.setContext(context);
         return messageExchangeFactory;
     }
 
     /**
      * @return a MessageExchange - blocking call
      * @throws MessagingException
      */
     public MessageExchange accept() throws MessagingException {
         try {
             checkNotClosed();
             MessageExchangeImpl me = (MessageExchangeImpl) queue.take();
             if (log.isDebugEnabled()) {
                 log.debug("Accepting " + me.getExchangeId() + " in " + this);
             }
             resumeTx(me);
             me.handleAccept();
             if (log.isTraceEnabled()) {
                 traceMessageExchange("Accepted", me);
             }
             return me;
         }
         catch (IllegalStateException e) {
             throw new MessagingException("DeliveryChannel has been closed.");
         }
         catch (InterruptedException e) {
             throw new MessagingException("accept failed", e);
         }
     }
 
     private void traceMessageExchange(String header, MessageExchange me) {
         try {
            StringBuffer sb = new StringBuffer();
             sb.append(header);
             sb.append(": ");
             sb.append("MessageExchange[\n");
             sb.append("  id: ").append(me.getExchangeId()).append('\n');
             sb.append("  status: ").append(me.getStatus()).append('\n');
             if (me.getMessage("in") != null) {
                 Node node = new SourceTransformer().toDOMNode(me.getMessage("in"));
                 me.getMessage("in").setContent(new DOMSource(node));
                 String str = DOMUtil.asXML(node);
                 sb.append("  in: ");
                 if (str.length() > 150) {
                     sb.append(str, 0, 150).append("...");
                 } else {
                     sb.append(str);
                 }
                 sb.append('\n');
             }
             if (me.getMessage("out") != null) {
                 Node node = new SourceTransformer().toDOMNode(me.getMessage("out"));
                 me.getMessage("out").setContent(new DOMSource(node));
                 String str = DOMUtil.asXML(node);
                 sb.append("  out: ");
                 if (str.length() > 150) {
                     sb.append(str, 0, 150).append("...");
                 } else {
                     sb.append(str);
                 }
                 sb.append('\n');
             }
             sb.append("]");
             log.trace(sb.toString());
         } catch (Exception e) {
             log.trace("Unable to display message", e);
         }
     }
 
     /**
      * return a MessageExchange
      * 
      * @param timeoutMS
      * @return Message Exchange
      * @throws MessagingException
      */
     public MessageExchange accept(long timeoutMS) throws MessagingException {
         try {
             checkNotClosed();
             MessageExchangeImpl me = (MessageExchangeImpl) queue.poll(timeoutMS);
             if (me != null) {
                 // If the exchange has already timed out,
                 // do not give it to the component
                 if (me.getPacket().isAborted()) {
                     if (log.isDebugEnabled()) {
                         log.debug("Aborted " + me.getExchangeId() + " in " + this);
                     }
                     me = null;
                 } else {
                     if (log.isDebugEnabled()) {
                         log.debug("Accepting " + me.getExchangeId() + " in " + this);
                     }
                     resumeTx(me);
                     me.handleAccept();
                     if (log.isTraceEnabled()) {
                         traceMessageExchange("Accepted", me);
                     }
                 }
             }
             return me;
         }
         catch (InterruptedException e) {
             throw new MessagingException("accept failed", e);
         }
     }
 
     protected void doSend(MessageExchangeImpl messageExchange, boolean sync) throws MessagingException {
         try {
             if (log.isTraceEnabled()) {
                 traceMessageExchange("Sent", messageExchange);
             }
             // If the delivery channel has been closed
             checkNotClosed();
             // If the message has timed out
             if (messageExchange.getPacket().isAborted()) {
                 throw new ExchangeTimeoutException(messageExchange);
             }
             // Auto enlist exchange in transaction
             autoEnlistInTx(messageExchange);
             // Update persistence info
             Boolean persistent = messageExchange.getPersistent();
             if (persistent == null) {
                 if (context.getActivationSpec().getPersistent() != null) {
                     persistent = context.getActivationSpec().getPersistent();
                 } else {
                     persistent = Boolean.valueOf(context.getContainer().isPersistent());
                 }
                 messageExchange.setPersistent(persistent);
             }
 
             if (exchangeThrottling) {
                 if (throttlingInterval > intervalCount) {
                     intervalCount = 0;
                     try {
                         Thread.sleep(throttlingTimeout);
                     }
                     catch (InterruptedException e) {
                         log.warn("throttling failed", e);
                     }
                 }
                 intervalCount++;
             }
 
             // Update stats
             long currentTime = System.currentTimeMillis();
             if (container.isNotifyStatistics()) {
                 long oldCount = messagingStats.getOutboundExchanges().getCount();
                 messagingStats.getOutboundExchanges().increment();
                 componentConnector.getComponentMBean().firePropertyChanged(
                         "outboundExchangeCount",
                         new Long(oldCount),
                         new Long(messagingStats.getOutboundExchanges().getCount()));
                 double oldRate = messagingStats.getOutboundExchangeRate().getAverageTime();
                 messagingStats.getOutboundExchangeRate().addTime(currentTime - lastSendTime);
                 componentConnector.getComponentMBean().firePropertyChanged("outboundExchangeRate",
                         new Double(oldRate),
                         new Double(messagingStats.getOutboundExchangeRate().getAverageTime()));
             } else {
                 messagingStats.getOutboundExchanges().increment();
                 messagingStats.getOutboundExchangeRate().addTime(currentTime - lastSendTime);
             }
             lastSendTime = currentTime;
 
             if (messageExchange.getRole() == Role.CONSUMER) {
                 messageExchange.setSourceId(componentConnector.getComponentNameSpace());
             }
 
             // Call the listeners before the ownership changes
             container.callListeners(messageExchange);
             messageExchange.handleSend(sync);
             container.sendExchange(messageExchange.getMirror());
         } catch (MessagingException e) {
             if (log.isDebugEnabled()) {
                 log.debug("Exception processing: " + messageExchange.getExchangeId() + " in " + this);
             }
             throw e;
         } finally {
             if (messageExchange.getTransactionContext() != null) {
                 if (messageExchange.getMirror().getSyncState() == MessageExchangeImpl.SYNC_STATE_SYNC_SENT) {
                     suspendTx(messageExchange);
                     if (log.isDebugEnabled()) {
                         log.debug("Notifying: " + messageExchange.getExchangeId() + " in " + this);
                     }
                     synchronized (messageExchange.getMirror()) {
                         messageExchange.getMirror().notify();
                     }
                 }
             }
         }
 
         /*
         if (messageExchange.getMirror().getSyncState() == MessageExchangeImpl.SYNC_STATE_SYNC_SENT) {
             synchronized (messageExchange.getMirror()) {
                 suspendTx(messageExchange);
                 messageExchange.getMirror().setSyncState(MessageExchangeImpl.SYNC_STATE_SYNC_RECEIVED);
                 messageExchange.getMirror().notify();
             }
         }
         */
     }
 
     /**
      * routes a MessageExchange
      * 
      * @param messageExchange
      * @throws MessagingException
      */
     public void send(MessageExchange messageExchange) throws MessagingException {
         messageExchange.setProperty(JbiConstants.SEND_SYNC, null);
         MessageExchangeImpl messageExchangeImpl = (MessageExchangeImpl) messageExchange;
         doSend(messageExchangeImpl, false);
     }
 
     /**
      * routes a MessageExchange
      * 
      * @param messageExchange
      * @return true if processed
      * @throws MessagingException
      */
     public boolean sendSync(MessageExchange messageExchange) throws MessagingException {
         return sendSync(messageExchange, Long.MAX_VALUE);
     }
 
     /**
      * routes a MessageExchange
      * 
      * @param messageExchange
      * @param timeoutMS
      * @return true if processed
      * @throws MessagingException
      */
     public boolean sendSync(MessageExchange messageExchange, long timeoutMS) throws MessagingException {
         boolean result = false;
         if (log.isDebugEnabled()) {
             log.debug("Sending " + messageExchange.getExchangeId() + " in " + this);
         }
         // JBI 5.5.2.1.3: set the sendSync property
         messageExchange.setProperty(JbiConstants.SEND_SYNC, Boolean.TRUE);
         MessageExchangeImpl messageExchangeImpl = (MessageExchangeImpl) messageExchange;
         exchangesById.put(messageExchange.getExchangeId(), messageExchange);
         try {
             // Synchronously send a message and wait for the response
             synchronized (messageExchangeImpl) {
                 doSend(messageExchangeImpl, true);
                 if (messageExchangeImpl.getSyncState() != MessageExchangeImpl.SYNC_STATE_SYNC_RECEIVED) {
                     messageExchangeImpl.wait(timeoutMS);
                 }
             }
             if (messageExchangeImpl.getSyncState() == MessageExchangeImpl.SYNC_STATE_SYNC_RECEIVED) {
                 messageExchangeImpl.handleAccept();
                 resumeTx(messageExchangeImpl);
                 result= true;
             } else {
                 // JBI 5.5.2.1.3: the exchange should be set to ERROR status
                 messageExchangeImpl.getPacket().setAborted(true);
                 result =  false;
             }
         } catch (InterruptedException e) {
             exchangesById.remove(messageExchange.getExchangeId());
             throw new MessagingException(e);
         }
         finally{
             exchangesById.remove(messageExchange.getExchangeId());
         }
         return result;
     }
 
     /**
      * @return Returns the container.
      */
     public JBIContainer getContainer() {
         return container;
     }
 
     /**
      * @param container The container to set.
      */
     public void setContainer(JBIContainer container) {
         this.container = container;
     }
 
     /**
      * @return Returns the componentConnector.
      */
     public ComponentConnector getConnector() {
         return componentConnector;
     }
 
     /**
      * Set the ComponentConnector
      * 
      * @param connector context to set.
      */
     public void setConnector(LocalComponentConnector connector) {
         this.componentConnector = connector;
     }
 
     /**
      * Get the context
      * 
      * @return the context
      */
     public ComponentContextImpl getContext() {
         return context;
     }
 
     /**
      * set the context
      * 
      * @param context
      */
     public void setContext(ComponentContextImpl context) {
         this.context = context;
     }
 
     /**
      * Get the MessagingStats
      * 
      * @return messaging stats
      */
     public MessagingStats getMessagingStats() {
         return messagingStats;
     }
 
     /**
      * Is MessageExchange sender throttling enabled ?
      * 
      * @return true if throttling enabled
      */
     public boolean isExchangeThrottling() {
         return exchangeThrottling;
     }
 
     /**
      * Set message throttling
      * 
      * @param value
      */
     public void setExchangeThrottling(boolean value) {
         this.exchangeThrottling = value;
     }
 
     /**
      * Get the throttling timeout
      * 
      * @return throttling tomeout (ms)
      */
     public long getThrottlingTimeout() {
         return throttlingTimeout;
     }
 
     /**
      * Set the throttling timout
      * 
      * @param value (ms)
      */
     public void setThrottlingTimeout(long value) {
         throttlingTimeout = value;
     }
 
     /**
      * Get the interval for throttling - number of Exchanges set before the throttling timeout is applied
      * 
      * @return interval for throttling
      */
     public int getThrottlingInterval() {
         return throttlingInterval;
     }
 
     /**
      * Set the throttling interval number of Exchanges set before the throttling timeout is applied
      * 
      * @param value
      */
     public void setThrottlingInterval(int value) {
         throttlingInterval = value;
     }
 
     /**
      * Used internally for passing in a MessageExchange
      * 
      * @param me
      * @throws MessagingException
      */
     public void processInBound(MessageExchangeImpl me) throws MessagingException {
         checkNotClosed();
 
         // Update stats
         long currentTime = System.currentTimeMillis();
         if (container.isNotifyStatistics()) {
             long oldCount = messagingStats.getInboundExchanges().getCount();
             messagingStats.getInboundExchanges().increment();
             componentConnector.getComponentMBean().firePropertyChanged(
                     "inboundExchangeCount",
                     new Long(oldCount),
                     new Long(messagingStats.getInboundExchanges().getCount()));
             double oldRate = messagingStats.getInboundExchangeRate().getAverageTime();
             messagingStats.getInboundExchangeRate().addTime(currentTime - lastReceiveTime);
             componentConnector.getComponentMBean().firePropertyChanged("inboundExchangeRate",
                     new Double(oldRate),
                     new Double(messagingStats.getInboundExchangeRate().getAverageTime()));
         } else {
             messagingStats.getInboundExchanges().increment();
             messagingStats.getInboundExchangeRate().addTime(currentTime - lastReceiveTime);
         }
         lastReceiveTime = currentTime;
 
         // If the message has been sent synchronously
         // this is the answer, so update the syncState and notify the waiter
         // Here, we don't need to put the message in the queue
         MessageExchangeImpl theOriginal = (MessageExchangeImpl) exchangesById.get(me.getExchangeId());
         if (theOriginal != null && theOriginal.getSyncState() == MessageExchangeImpl.SYNC_STATE_SYNC_SENT &&
             theOriginal.getRole() == me.getRole()) {
             suspendTx(theOriginal);
             synchronized (theOriginal) {
                 theOriginal.copyFrom(me);
                 theOriginal.setSyncState(MessageExchangeImpl.SYNC_STATE_SYNC_RECEIVED);
                 theOriginal.notify();
             }
         } else {
             Component component = ((LocalComponentConnector) componentConnector).getComponent();
             // If the component implements the MessageExchangeListener,
             // the delivery can be made synchronously, so we don't need
             // to bother about transactions
             if (component != null && component instanceof MessageExchangeListener) {
                 me.handleAccept();
                 ((MessageExchangeListener) component).onMessageExchange(me);
             }
             else {
                 // Component uses async delivery
                 try {
                     if (me.isTransacted() && me.getStatus() == ExchangeStatus.DONE) {
                         // Do nothing in this case
                     } else if (me.isTransacted() && me.getMirror().getSyncState() == MessageExchangeImpl.SYNC_STATE_ASYNC) {
                         suspendTx(me);
                         synchronized (me.getMirror()) {
                             me.getMirror().setSyncState(MessageExchangeImpl.SYNC_STATE_SYNC_SENT);
                             if (log.isDebugEnabled()) {
                                 log.debug("Queuing: " + me.getExchangeId() + " in " + this);
                             }
                             queue.put(me);
                             if (log.isDebugEnabled()) {
                                 log.debug("Waiting: " + me.getExchangeId() + " in " + this);
                             }
                             // If the channel is closed while here,
                             // we must abort
                             waiters.put(Thread.currentThread(), Boolean.TRUE);
                             try {
                                 me.getMirror().wait();
                             } finally {
                                 waiters.remove(Thread.currentThread());
                             }
                             if (log.isDebugEnabled()) {
                                 log.debug("Notified: " + me.getExchangeId() + " in " + this);
                             }
                         }
                         resumeTx(me);
                     } else {
                         suspendTx(me);
                         queue.put(me);
                     }
                 } catch (InterruptedException e) {
                     throw new MessagingException(e);
                 }
             }
         }
     }
 
     /**
      * Get Inbound Factory
      * 
      * @return the inbound message factory
      */
     public MessageExchangeFactory getInboundFactory() {
         if (inboundFactory == null) {
             inboundFactory = createExchangeFactory();
         }
         return inboundFactory;
     }
 
     protected void suspendTx(MessageExchangeImpl me) throws MessagingException {
         try {
             Transaction oldTx = me.getTransactionContext();
             if (oldTx != null) {
                 TransactionManager tm = (TransactionManager) container.getTransactionManager();
                 if (tm != null) {
                     if (log.isDebugEnabled()) {
                         log.debug("Suspending transaction for " + me.getExchangeId() + " in " + this);
                     }
                     Transaction tx = tm.suspend();
                     if (tx != oldTx) {
                         throw new IllegalStateException("the transaction context set in the messageExchange is not bound to the current thread");
                     }
                 }
             }
         } catch (Exception e) {
             throw new MessagingException(e);
         }
     }
 
     protected void resumeTx(MessageExchangeImpl me) throws MessagingException {
         try {
             Transaction oldTx = me.getTransactionContext();
             if (oldTx != null) {
                 TransactionManager tm = (TransactionManager) container.getTransactionManager();
                 if (tm != null) {
                     if (log.isDebugEnabled()) {
                         log.debug("Resuming transaction for " + me.getExchangeId() + " in " + this);
                     }
                     tm.resume(oldTx);
                 }
             }
         } catch (Exception e) {
             throw new MessagingException(e);
         }
     }
 
     /**
      * If the jbi container configured to do so, the message exchange will
      * automatically be enlisted in the current transaction, if exists. 
      * 
      * @param messageExchange
      * @throws MessagingException
      */
     protected void autoEnlistInTx(MessageExchangeImpl me) throws MessagingException {
         try {
             if (container.isAutoEnlistInTransaction()) {
                 TransactionManager tm = (TransactionManager) container.getTransactionManager();
                 if (tm != null) {
                     Transaction tx = tm.getTransaction();
                     if (tx != null) {
                         Object oldTx = me.getTransactionContext();
                         if (oldTx == null) {
                             me.setTransactionContext(tx);
                         } else if (oldTx != tx) {
                             throw new IllegalStateException(
                                     "the transaction context set in the messageExchange is not bound to the current thread");
                         }
                     }
                 }
             }
         } catch (Exception e) {
             throw new MessagingException(e);
         }
     }
 
     /**
      * @return pretty print
      */
     public String toString() {
         return "DeliveryChannel{" + componentConnector.getComponentNameSpace() + "}";
     }
 }
