 /**
  *
  * Copyright 2005 LogicBlaze, Inc.
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  **/
 
 package org.logicblaze.lingo.jms;
 
 import org.aopalliance.intercept.MethodInterceptor;
 import org.aopalliance.intercept.MethodInvocation;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.logicblaze.lingo.LingoInvocation;
 import org.logicblaze.lingo.LingoRemoteInvocationFactory;
 import org.logicblaze.lingo.MetadataStrategy;
 import org.logicblaze.lingo.MetadataStrategyHelper;
 import org.logicblaze.lingo.MethodMetadata;
 import org.logicblaze.lingo.ResultJoinStrategy;
 import org.logicblaze.lingo.jms.impl.AsyncReplyHandler;
 import org.logicblaze.lingo.jms.impl.MultiplexingRequestor;
 import org.logicblaze.lingo.jms.impl.ResultJoinHandler;
 import org.logicblaze.lingo.jms.marshall.DefaultMarshaller;
 import org.logicblaze.lingo.jms.marshall.Marshaller;
 import org.springframework.aop.support.AopUtils;
 import org.springframework.beans.factory.DisposableBean;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.remoting.RemoteAccessException;
 import org.springframework.remoting.support.RemoteInvocationBasedAccessor;
 import org.springframework.remoting.support.RemoteInvocationFactory;
 import org.springframework.remoting.support.RemoteInvocationResult;
 
 import javax.jms.ConnectionFactory;
 import javax.jms.DeliveryMode;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.Topic;
 
 import java.util.Iterator;
 import java.util.Map;
 import java.util.WeakHashMap;
 
 /**
  * Interceptor for accessing a JMS based service which must be configured with a
  * {@link org.logicblaze.lingo.LingoRemoteInvocationFactory} instance.
  * 
  * @author James Strachan
  * @see #setServiceInterface
  * @see #setServiceUrl
  * @see JmsServiceExporter
  * @see JmsProxyFactoryBean
  */
 public class JmsClientInterceptor extends RemoteInvocationBasedAccessor implements MethodInterceptor, InitializingBean, DisposableBean {
     private static final Log log = LogFactory.getLog(JmsClientInterceptor.class);
 
     private Map remoteObjects = new WeakHashMap();
     private Requestor requestor;
     private Destination destination;
     private Destination responseDestination;
     private String correlationID;
     private Marshaller marshaller;
     private ConnectionFactory connectionFactory;
     private String jmsType;
     private Map messageProperties;
     private int jmsExpiration = -1;
     private JmsProducerConfig producerConfig = new JmsProducerConfig();
     private MetadataStrategy metadataStrategy;
     private boolean multipleResponsesExpected;
     private long responseTimeout = 30000L;
     private long multipleResponseTimeout = 5000L;
     private long remoteReferenceTimeout = 60000L;
 
     public JmsClientInterceptor() {
         setRemoteInvocationFactory(createRemoteInvocationFactory());
     }
 
     public JmsClientInterceptor(Requestor requestor) {
         this.requestor = requestor;
         setRemoteInvocationFactory(createRemoteInvocationFactory());
     }
 
     public JmsClientInterceptor(Requestor requestor, LingoRemoteInvocationFactory factory) {
         this.requestor = requestor;
         setRemoteInvocationFactory(factory);
     }
 
    public void afterPropertiesSet() {
         RemoteInvocationFactory factory = getRemoteInvocationFactory();
         if (!(factory instanceof LingoRemoteInvocationFactory)) {
             throw new IllegalArgumentException("remoteInvocationFactory must be an instance of LingoRemoteInvocationFactory but was: " + factory);
 
         }
         else {
             LingoRemoteInvocationFactory invocationFactory = (LingoRemoteInvocationFactory) factory;
             invocationFactory.setMetadataStrategy(getMetadataStrategy());
         }
         if (requestor == null) {
             if (connectionFactory == null) {
                 throw new IllegalArgumentException("requestor or connectionFactory is required");
             }
             else {
                try {
                    requestor = createRequestor();
                } catch (JMSException e) {
                    throw new IllegalArgumentException("unable to create requestor", e);
                }
             }
         }
         if (marshaller == null) {
             // default to standard JMS marshaling
             marshaller = new DefaultMarshaller();
         }
     }
 
     public Object invoke(MethodInvocation methodInvocation) throws Throwable {
         if (AopUtils.isToStringMethod(methodInvocation.getMethod())) {
             return "JMS invoker proxy for service URL [" + getServiceUrl() + "]";
         }
         LingoInvocation invocation = (LingoInvocation) createRemoteInvocation(methodInvocation);
         MethodMetadata metadata = invocation.getMetadata();
         replaceRemoteReferences(invocation, metadata);
         try {
             Message requestMessage = marshaller.createRequestMessage(requestor, invocation);
             populateHeaders(requestMessage);
             if (metadata.isOneWay()) {
                 requestor.send(destination, requestMessage);
                 return null;
             }
             else if (!isMultipleResponse(methodInvocation, metadata)) {
                 Message response = requestor.request(destination, requestMessage, getResponseTimeout());
                 RemoteInvocationResult result = marshaller.extractInvocationResult(response);
                 return recreateRemoteInvocationResult(result);
             }
             else {
                 ResultJoinHandler handler = createResultJoinHandler(methodInvocation, metadata);
                 requestor.request(destination, requestMessage, handler, getMultipleResponseTimeout());
                 RemoteInvocationResult result = handler.waitForResult();
                 return recreateRemoteInvocationResult(result);
             }
         }
         catch (JMSException e) {
             log.warn("Remote access error: " + methodInvocation, e);
             throw new RemoteAccessException("Cannot access JMS invoker remote service at [" + getServiceUrl() + "]", e);
         }
     }
 
     public void destroy() throws Exception {
         requestor.close();
     }
 
     // Properties
     // -------------------------------------------------------------------------
     public Requestor getRequestor() {
         return requestor;
     }
 
     public void setRequestor(Requestor requestor) {
         this.requestor = requestor;
     }
 
     public Destination getDestination() {
         return destination;
     }
 
     /**
      * Sets the destination used to make requests
      * 
      * @param destination
      */
     public void setDestination(Destination destination) {
         this.destination = destination;
     }
 
     public Destination getResponseDestination() {
         return responseDestination;
     }
 
     /**
      * Sets the destination used to consume responses on - or null and a
      * temporary queue will be created.
      * 
      * @param responseDestination
      */
     public void setResponseDestination(Destination responseDestination) {
         this.responseDestination = responseDestination;
     }
 
     public void setCorrelationID(String correlationID) {
         this.correlationID = correlationID;
     }
 
     public String getJmsType() {
         return jmsType;
     }
 
     /**
      * Sets the JMS message type string which is appended to messages if set
      */
     public void setJmsType(String jmsType) {
         this.jmsType = jmsType;
     }
 
     public Map getMessageProperties() {
         return messageProperties;
     }
 
     public int getJmsPriority() {
         return producerConfig.getPriority();
     }
 
     /**
      * Sets the JMS priority of the request message
      */
     public void setJmsPriority(int jmsPriority) {
         producerConfig.setPriority(jmsPriority);
     }
 
     public int getTimeToLive() {
         return producerConfig.getTimeToLive();
     }
 
     /**
      * Sets the JMS expiration timeout (in milliseconds) of the request message
      */
     public void setTimeToLive(int timeToLive) {
         producerConfig.setTimeToLive(timeToLive);
     }
 
     /**
      * Sets the message properties to be added to each message. Note that the
      * keys should be Strings and the values should be primitive types.
      */
     public void setMessageProperties(Map messageProperties) {
         this.messageProperties = messageProperties;
     }
 
     public Marshaller getMarshaller() {
         return marshaller;
     }
 
     public void setMarshaller(Marshaller marshaller) {
         this.marshaller = marshaller;
     }
 
     public ConnectionFactory getConnectionFactory() {
         return connectionFactory;
     }
 
     /**
      * Used to create a default {@link Requestor} if no requestor is explicitly
      * configured.
      */
     public void setConnectionFactory(ConnectionFactory connectionFactory) {
         this.connectionFactory = connectionFactory;
     }
 
     public JmsProducerConfig getProducerConfig() {
         return producerConfig;
     }
 
     /**
      * Sets the configuration of the producer used to send back responses
      */
     public void setProducerConfig(JmsProducerConfig producerConfig) {
         this.producerConfig = producerConfig;
     }
 
     public boolean isPersistentDelivery() {
         return producerConfig.getDeliveryMode() == DeliveryMode.PERSISTENT;
     }
 
     /**
      * Sets the delivery mode to be persistent or non-persistent.
      */
     public void setPersistentDelivery(boolean persistent) {
         producerConfig.setDeliveryMode(persistent ? DeliveryMode.PERSISTENT : DeliveryMode.NON_PERSISTENT);
     }
 
     public String getClientID() {
         return producerConfig.getClientID();
     }
 
     /**
      * Sets the JMS connections unique clientID. This is optional unless you
      * wish to use durable topic subscriptions. Only one connection can have a
      * given clientID at any time.
      */
     public void setClientID(String clientID) {
         producerConfig.setClientID(clientID);
     }
 
     public MetadataStrategy getMetadataStrategy() {
         if (metadataStrategy == null) {
             metadataStrategy = createMetadataStrategy();
         }
         return metadataStrategy;
     }
 
     public void setMetadataStrategy(MetadataStrategy metadataStrategy) {
         this.metadataStrategy = metadataStrategy;
     }
 
     public boolean isMultipleResponsesExpected() {
         return multipleResponsesExpected;
     }
 
     /**
      * Sets whether or not multiple response messages are expected. Typically
      * multiple responses are only expected when the {@link #getDestination()}
      * method returns a {@link Topic} but there could be circumstances when
      * sending a request to a queue results in messages being fanned out to many
      * servers which could all respond.
      */
     public void setMultipleResponsesExpected(boolean multipleResponsesExpected) {
         this.multipleResponsesExpected = multipleResponsesExpected;
     }
 
     public long getRemoteReferenceTimeout() {
         return remoteReferenceTimeout;
     }
 
     /**
      * Sets the maximum amount of time an inactive remote object reference will
      * keep around until it is garbage collected.
      */
     public void setRemoteReferenceTimeout(long remoteReferenceTimeout) {
         this.remoteReferenceTimeout = remoteReferenceTimeout;
     }
 
     public long getMultipleResponseTimeout() {
         return multipleResponseTimeout;
     }
 
     /**
      * Sets the maximum amount of time to wait for multiple results to come back
      * if communicating with multiple servers and aggregating together the
      * results.
      */
     public void setMultipleResponseTimeout(long multipleResponseTimeout) {
         this.multipleResponseTimeout = multipleResponseTimeout;
     }
 
     public long getResponseTimeout() {
         return responseTimeout;
     }
 
     /**
      * Sets the maximum amount of time (in milliseconds) to wait for responses 
      * to come back before timing out the request. You should typically set this 
      * value greater than the {@link #getTimeToLive()} property which is used to 
      * set the expiration time on the request message.
      */
     public void setResponseTimeout(long responseTimeout) {
         this.responseTimeout = responseTimeout;
     }
 
     // Implementation methods
     // -------------------------------------------------------------------------
 
     /**
      * Returns true if this method expects multiple response messages such as
      * when sending a message over a topic.
      */
     protected boolean isMultipleResponse(MethodInvocation methodInvocation, MethodMetadata metadata) {
         return (getDestination() instanceof Topic) || isMultipleResponsesExpected();
     }
 
     protected void populateHeaders(Message requestMessage) throws JMSException {
         if (correlationID != null) {
             requestMessage.setJMSCorrelationID(correlationID);
         }
         if (jmsType != null) {
             requestMessage.setJMSType(jmsType);
         }
         if (jmsExpiration >= 0) {
             requestMessage.setJMSExpiration(jmsExpiration);
         }
         int jmsPriority = getJmsPriority();
         if (jmsPriority >= 0) {
             requestMessage.setJMSPriority(jmsPriority);
         }
         if (messageProperties != null) {
             for (Iterator iter = messageProperties.entrySet().iterator(); iter.hasNext();) {
                 Map.Entry entry = (Map.Entry) iter.next();
                 String name = entry.getKey().toString();
                 Object value = entry.getValue();
                 requestMessage.setObjectProperty(name, value);
             }
         }
     }
 
     /**
      * Recreate the invocation result contained in the given
      * RemoteInvocationResult object. The default implementation calls the
      * default recreate method.
      * <p>
      * Can be overridden in subclass to provide custom recreation, potentially
      * processing the returned result object.
      * 
      * @param result
      *            the RemoteInvocationResult to recreate
      * @return a return value if the invocation result is a successful return
      * @throws Throwable
      *             if the invocation result is an exception
      * @see org.springframework.remoting.support.RemoteInvocationResult#recreate
      */
     protected Object recreateRemoteInvocationResult(RemoteInvocationResult result) throws Throwable {
         return result.recreate();
     }
 
     protected void replaceRemoteReferences(LingoInvocation invocation, MethodMetadata metadata) {
         Object[] arguments = invocation.getArguments();
         Class[] parameterTypes = invocation.getParameterTypes();
         for (int i = 0; i < parameterTypes.length; i++) {
             if (metadata.isRemoteParameter(i)) {
                 arguments[i] = remoteReference(parameterTypes[i], arguments[i]);
             }
         }
     }
 
     protected Object remoteReference(Class type, Object value) {
         if (value == null) {
             return null;
         }
         String correlationID = (String) remoteObjects.get(value);
         if (correlationID == null) {
             correlationID = requestor.createCorrelationID();
             remoteObjects.put(value, correlationID);
         }
         if (requestor instanceof MultiplexingRequestor) {
             MultiplexingRequestor multiplexingRequestor = (MultiplexingRequestor) requestor;
             multiplexingRequestor.registerHandler(correlationID, createAsyncHandler(value), getRemoteReferenceTimeout());
         }
         else {
             throw new IllegalArgumentException("You can only pass remote references with a MultiplexingRequestor");
         }
         return correlationID;
     }
 
     protected ResultJoinHandler createResultJoinHandler(MethodInvocation methodInvocation, MethodMetadata metadata) {
         ResultJoinStrategy joinStrategy = getMetadataStrategy().getResultJoinStrategy(methodInvocation, metadata);
         return new ResultJoinHandler(marshaller, joinStrategy);
     }
 
     protected AsyncReplyHandler createAsyncHandler(Object value) {
         AsyncReplyHandler replyHandler = new AsyncReplyHandler(value, marshaller, getMetadataStrategy());
         replyHandler.setConnectionFactory(connectionFactory);
         replyHandler.setMarshaller(marshaller);
         replyHandler.setProducerConfig(producerConfig);
         replyHandler.setResponseRequestor(getRequestor());
         replyHandler.setInvocationFactory(getRemoteInvocationFactory());
         try {
             replyHandler.afterPropertiesSet();
         }
         catch (Exception e) {
             throw new RuntimeException("Could not create the AsyncHandler: " + e, e);
         }
         return replyHandler;
     }
 
     protected Requestor createRequestor() throws JMSException {
         return MultiplexingRequestor.newInstance(connectionFactory, getProducerConfig(), destination, responseDestination);
     }
 
     /**
      * Factory method to create a default lingo based invocation factory if none
      * is configured
      */
     protected LingoRemoteInvocationFactory createRemoteInvocationFactory() {
         return new LingoRemoteInvocationFactory(getMetadataStrategy());
     }
 
     /**
      * Factory method to create a default metadata strategy if none is
      * configured
      * 
      * @return
      */
     protected MetadataStrategy createMetadataStrategy() {
         return MetadataStrategyHelper.newInstance();
     }
 
 }
