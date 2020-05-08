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
 package org.apache.servicemix.common;
 
 import java.lang.reflect.Method;
 import java.util.Map;
 
 import javax.jbi.JBIException;
 import javax.jbi.component.ComponentContext;
 import javax.jbi.component.ComponentLifeCycle;
 import javax.jbi.messaging.DeliveryChannel;
 import javax.jbi.messaging.ExchangeStatus;
 import javax.jbi.messaging.MessageExchange;
 import javax.jbi.messaging.MessagingException;
 import javax.jbi.messaging.MessageExchange.Role;
 import javax.jbi.servicedesc.ServiceEndpoint;
 import javax.management.MBeanServer;
 import javax.management.ObjectName;
 import javax.transaction.Status;
 import javax.transaction.Transaction;
 import javax.transaction.TransactionManager;
 import javax.xml.namespace.QName;
 
 import org.apache.commons.logging.Log;
 import org.apache.servicemix.JbiConstants;
 import org.apache.servicemix.executors.Executor;
 import org.apache.servicemix.executors.ExecutorFactory;
 import org.apache.servicemix.executors.impl.ExecutorFactoryImpl;
 
 import edu.emory.mathcs.backport.java.util.concurrent.ConcurrentHashMap;
 import edu.emory.mathcs.backport.java.util.concurrent.atomic.AtomicBoolean;
 
 /**
  * Base class for life cycle management of components. This class may be used as
  * is.
  * 
  * @author Guillaume Nodet
  * @version $Revision: 399873 $
  * @since 3.0
  */
 public class AsyncBaseLifeCycle implements ComponentLifeCycle {
 
     protected transient Log logger;
 
     protected ServiceMixComponent component;
 
     protected ComponentContext context;
 
     protected ObjectName mbeanName;
 
     protected ExecutorFactory executorFactory;
     
     protected Executor executor;
 
     protected AtomicBoolean running;
 
     protected DeliveryChannel channel;
 
     protected Thread poller;
 
     protected AtomicBoolean polling;
 
     protected TransactionManager transactionManager;
 
     protected boolean workManagerCreated;
 
     protected Map processors;
     
     protected ThreadLocal correlationId;
 
     public AsyncBaseLifeCycle() {
         this.running = new AtomicBoolean(false);
         this.polling = new AtomicBoolean(false);
         this.processors = new ConcurrentHashMap();
         this.correlationId = new ThreadLocal();
     }
 
     public AsyncBaseLifeCycle(ServiceMixComponent component) {
         this();
         setComponent(component);
     }
 
     protected void setComponent(ServiceMixComponent component) {
         this.component = component;
         this.logger = component.getLogger();
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.jbi.component.ComponentLifeCycle#getExtensionMBeanName()
      */
     public ObjectName getExtensionMBeanName() {
         return mbeanName;
     }
 
     protected Object getExtensionMBean() throws Exception {
         return null;
     }
 
     protected ObjectName createExtensionMBeanName() throws Exception {
         return this.context.getMBeanNames().createCustomComponentMBeanName("Configuration");
     }
 
     public QName getEPRServiceName() {
         return null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.jbi.component.ComponentLifeCycle#init(javax.jbi.component.ComponentContext)
      */
     public void init(ComponentContext context) throws JBIException {
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Initializing component");
             }
             this.context = context;
             this.channel = context.getDeliveryChannel();
             try {
                 this.transactionManager = (TransactionManager) context.getTransactionManager();
             } catch (Throwable e) {
                 // Ignore, this is just a safeguard against non compliant
                 // JBI implementation which throws an exception instead of
                 // return null
             }
             doInit();
             if (logger.isDebugEnabled()) {
                 logger.debug("Component initialized");
             }
         } catch (JBIException e) {
             throw e;
         } catch (Exception e) {
             throw new JBIException("Error calling init", e);
         }
     }
 
     protected void doInit() throws Exception {
         // Register extension mbean
         Object mbean = getExtensionMBean();
         if (mbean != null) {
             MBeanServer server = this.context.getMBeanServer();
             if (server == null) {
                 // TODO: log a warning ?
                 // throw new JBIException("null mBeanServer");
             } else {
                 this.mbeanName = createExtensionMBeanName();
                 if (server.isRegistered(this.mbeanName)) {
                     server.unregisterMBean(this.mbeanName);
                 }
                 server.registerMBean(mbean, this.mbeanName);
             }
         }
         // Obtain or create the work manager
         // When using the WorkManager from ServiceMix,
         // some class loader problems can appear when
         // trying to uninstall the components.
         // Some threads owned by the work manager have a
         // security context referencing the component class loader
         // so that every loaded classes are locked
         // this.workManager = findWorkManager();
         if (this.executorFactory == null) {
             this.executorFactory = findExecutorFactory();
         }
         if (this.executorFactory == null) {
             this.executorFactory = createExecutorFactory();
         }
         this.executor = this.executorFactory.createExecutor("component." + getContext().getComponentName());
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.jbi.component.ComponentLifeCycle#shutDown()
      */
     public void shutDown() throws JBIException {
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Shutting down component");
             }
             doShutDown();
             this.context = null;
             if (logger.isDebugEnabled()) {
                 logger.debug("Component shut down");
             }
         } catch (JBIException e) {
             throw e;
         } catch (Exception e) {
             throw new JBIException("Error calling shutdown", e);
         }
     }
 
     protected void doShutDown() throws Exception {
         // Unregister mbean
         if (this.mbeanName != null) {
             MBeanServer server = this.context.getMBeanServer();
             if (server == null) {
                 throw new JBIException("null mBeanServer");
             }
             if (server.isRegistered(this.mbeanName)) {
                 server.unregisterMBean(this.mbeanName);
             }
         }
         // Destroy excutor
         executor.shutdown();
         executor = null;
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.jbi.component.ComponentLifeCycle#start()
      */
     public void start() throws JBIException {
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Starting component");
             }
             if (this.running.compareAndSet(false, true)) {
                 doStart();
             }
             if (logger.isDebugEnabled()) {
                 logger.debug("Component started");
             }
         } catch (JBIException e) {
             throw e;
         } catch (Exception e) {
             throw new JBIException("Error calling start", e);
         }
     }
 
     protected void doStart() throws Exception {
         synchronized (this.polling) {
             executor.execute(new Runnable() {
                 public void run() {
                     poller = Thread.currentThread();
                     pollDeliveryChannel();
                 }
             });
             polling.wait();
         }
     }
 
     protected void pollDeliveryChannel() {
         synchronized (polling) {
             polling.set(true);
             polling.notify();
         }
         while (running.get()) {
             try {
                 final MessageExchange exchange = channel.accept(1000L);
                 if (exchange != null) {
                     final Transaction tx = (Transaction) exchange
                                     .getProperty(MessageExchange.JTA_TRANSACTION_PROPERTY_NAME);
                     if (tx != null) {
                         if (transactionManager == null) {
                             throw new IllegalStateException(
                                             "Exchange is enlisted in a transaction, but no transaction manager is available");
                         }
                         transactionManager.suspend();
                     }
                     executor.execute(new Runnable() {
                         public void run() {
                             processExchangeInTx(exchange, tx);
                         }
                     });
                 }
             } catch (Throwable t) {
                 if (running.get() == false) {
                     // Should have been interrupted, discard the throwable
                     if (logger.isDebugEnabled()) {
                         logger.debug("Polling thread will stop");
                     }
                 } else {
                     logger.error("Error polling delivery channel", t);
                 }
             }
         }
         synchronized (polling) {
             polling.set(false);
             polling.notify();
         }
     }
 
     /*
      * (non-Javadoc)
      * 
      * @see javax.jbi.component.ComponentLifeCycle#stop()
      */
     public void stop() throws JBIException {
         try {
             if (logger.isDebugEnabled()) {
                 logger.debug("Stopping component");
             }
             if (this.running.compareAndSet(true, false)) {
                 doStop();
             }
             if (logger.isDebugEnabled()) {
                 logger.debug("Component stopped");
             }
         } catch (JBIException e) {
             throw e;
         } catch (Exception e) {
             throw new JBIException("Error calling stop", e);
         }
     }
 
     protected void doStop() throws Exception {
         // Interrupt the polling thread and await termination
         try {
             synchronized (polling) {
                 if (polling.get()) {
                     poller.interrupt();
                     polling.wait();
                 }
             }
         } finally {
             poller = null;
         }
     }
 
     /**
      * @return Returns the context.
      */
     public ComponentContext getContext() {
         return context;
     }
 
     public Executor getExecutor() {
         return executor;
     }
 
     protected ExecutorFactory createExecutorFactory() {
         // Create a very simple one
         return new ExecutorFactoryImpl();
     }
 
     protected ExecutorFactory findExecutorFactory() {
         // If inside ServiceMix, retrieve its executor factory
         try {
             Method getContainerMth = context.getClass().getMethod("getContainer", new Class[0]);
             Object container = getContainerMth.invoke(context, new Object[0]);
             Method getWorkManagerMth = container.getClass().getMethod("getExecutorFactory", new Class[0]);
             return (ExecutorFactory) getWorkManagerMth.invoke(container, new Object[0]);
         } catch (Throwable t) {
             if (logger.isDebugEnabled()) {
                 logger.debug("JBI container is not ServiceMix. Will create our own ExecutorFactory", t);
             }
         }
         // TODO: should look in jndi for an existing ExecutorFactory
         return null;
     }
 
     protected void processExchangeInTx(MessageExchange exchange, Transaction tx) {
         try {
             if (tx != null) {
                 transactionManager.resume(tx);
             }
             processExchange(exchange);
         } catch (Exception e) {
             logger.error("Error processing exchange " + exchange, e);
             try {
                 // If we are transacted, check if this exception should
                 // rollback the transaction
                 if (transactionManager != null && transactionManager.getStatus() == Status.STATUS_ACTIVE
                                 && exceptionShouldRollbackTx(e)) {
                     transactionManager.setRollbackOnly();
                 }
                 exchange.setError(e);
                 channel.send(exchange);
             } catch (Exception inner) {
                 logger.error("Error setting exchange status to ERROR", inner);
             }
         } finally {
             try {
                 // Check transaction status
                 if (tx != null) {
                     int status = transactionManager.getStatus();
                     // We use pull delivery, so the transaction should already
                     // have been transfered to another thread because the
                     // component
                     // must have answered.
                     if (status != Status.STATUS_NO_TRANSACTION) {
                        logger.error("Transaction is still active after exchange processing. Trying to rollback transaction.");
                         try {
                             transactionManager.rollback();
                         } catch (Throwable t) {
                             logger.error("Error trying to rollback transaction.", t);
                         }
                     }
                 }
             } catch (Throwable t) {
                 logger.error("Error checking transaction status.", t);
             }
         }
     }
 
     protected boolean exceptionShouldRollbackTx(Exception e) {
         return false;
     }
 
     protected void processExchange(MessageExchange exchange) throws Exception {
         if (logger.isDebugEnabled()) {
             logger.debug("Received exchange: status: " + exchange.getStatus() + ", role: "
                             + (exchange.getRole() == Role.CONSUMER ? "consumer" : "provider"));
         }
         if (exchange.getRole() == Role.PROVIDER) {
             boolean dynamic = false;
             ServiceEndpoint endpoint = exchange.getEndpoint();
             String key = EndpointSupport.getKey(exchange.getEndpoint());
             Endpoint ep = (Endpoint) this.component.getRegistry().getEndpoint(key);
             if (ep == null) {
                 if (endpoint.getServiceName().equals(getEPRServiceName())) {
                     ep = getResolvedEPR(exchange.getEndpoint());
                     dynamic = true;
                 }
                 if (ep == null) {
                     throw new IllegalStateException("Endpoint not found: " + key);
                 }
             }
             ExchangeProcessor processor = ep.getProcessor();
             if (processor == null) {
                 throw new IllegalStateException("No processor found for endpoint: " + key);
             }
             try {
                 doProcess(ep, processor, exchange);
             } finally {
                 // If the endpoint is dynamic, deactivate it
                 if (dynamic) {
                     ep.deactivate();
                 }
             }
         } else {
             ExchangeProcessor processor = null;
             Endpoint ep = null;
             if (exchange.getProperty(JbiConstants.SENDER_ENDPOINT) != null) {
                 String key = exchange.getProperty(JbiConstants.SENDER_ENDPOINT).toString();
                 ep = (Endpoint) this.component.getRegistry().getEndpoint(key);
                 if (ep != null) {
                     processor = ep.getProcessor();
                 }
             } else {
                 processor = (ExchangeProcessor) processors.remove(exchange.getExchangeId());
             }
             if (processor == null) {
                 throw new IllegalStateException("No processor found for: " + exchange.getExchangeId());
             }
             doProcess(ep, processor, exchange);
         }
 
     }
 
     /**
      * Thin wrapper around the call to the processor to ensure that the Endpoints
      * classloader is used where available
      * 
      */
     private void doProcess(Endpoint ep, ExchangeProcessor processor, MessageExchange exchange) throws Exception {
         ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
         try {
             if (ep != null && ep.getServiceUnit().getConfigurationClassLoader() != null) {
                 ClassLoader classLoader = ep.getServiceUnit().getConfigurationClassLoader();
                 Thread.currentThread().setContextClassLoader(classLoader);
             }
             // Read the correlation id from the exchange and set it in the correlation id property
             String correlationID = (String)exchange.getProperty(JbiConstants.CORRELATION_ID);
             if (correlationID != null) {
                 // Set the id in threadlocal variable
                 correlationId.set(correlationID);
             }
             if (logger.isDebugEnabled()) {
                 logger.debug("Retrieved correlation id: " + correlationID);
             }
             processor.process(exchange);
         } finally {
             Thread.currentThread().setContextClassLoader(oldCl);
             // Clean the threadlocal variable
             correlationId.set(null);
         }
     }
 
     /**
      * 
      * @param exchange
      * @param processor
      * @throws MessagingException
      * @deprecated use sendConsumerExchange(MessageExchange, Endpoint) instead
      */
     public void sendConsumerExchange(MessageExchange exchange, ExchangeProcessor processor) throws MessagingException {
         // If the exchange is not ACTIVE, no answer is expected
         if (exchange.getStatus() == ExchangeStatus.ACTIVE) {
             processors.put(exchange.getExchangeId(), processor);
         }
         channel.send(exchange);
     }
 
     /**
      * This method allows the component to keep no state in memory so that
      * components can be clustered and provide fail-over and load-balancing.
      * 
      * @param exchange
      * @param endpoint
      * @throws MessagingException
      */
     public void sendConsumerExchange(MessageExchange exchange, Endpoint endpoint) throws MessagingException {
         // Check if a correlation id is already set on the exchange, otherwise create it
         String correlationIDValue = (String) exchange.getProperty(JbiConstants.CORRELATION_ID);
         if (correlationIDValue == null) {
             // Retrieve correlation id from thread local variable, if exist
             correlationIDValue = (String) correlationId.get();
             if (correlationIDValue == null) {
                 // Set a correlation id property that have to be propagated in all components
                 // to trace the process instance
                 correlationIDValue = exchange.getExchangeId();
                 exchange.setProperty(JbiConstants.CORRELATION_ID, exchange.getExchangeId());
                 if (logger.isDebugEnabled()) {
                     logger.debug("Created correlation id: " + correlationIDValue);
                 }
             } else {
                 // Use correlation id retrieved from previous message exchange
                 exchange.setProperty(JbiConstants.CORRELATION_ID, correlationIDValue);
                 if (logger.isDebugEnabled()) {
                     logger.debug("Correlation id retrieved from ThreadLocal: " + correlationIDValue);
                 }
             }
         }
         // Set the sender endpoint property
         String key = EndpointSupport.getKey(endpoint);
         exchange.setProperty(JbiConstants.SENDER_ENDPOINT, key);
         // Send the exchange
         channel.send(exchange);
     }
 
     /**
      * Handle an exchange sent to an EPR resolved by this component
      * 
      * @param ep the service endpoint
      * @return an endpoint to use for handling the exchange
      * @throws Exception
      */
     protected Endpoint getResolvedEPR(ServiceEndpoint ep) throws Exception {
         throw new UnsupportedOperationException("Component does not handle EPR exchanges");
     }
 
 }
