 /*  
  * Copyright 2008-2010 the original author or authors 
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 package org.kaleidofoundry.messaging.jms;
 
 import static org.kaleidofoundry.messaging.ClientContextBuilder.CONSUMER_READ_BUFFER_SIZE;
 import static org.kaleidofoundry.messaging.ClientContextBuilder.CONSUMER_RECEIVE_TIMEOUT_PROPERTY;
 import static org.kaleidofoundry.messaging.MessagingConstants.MESSAGE_TYPE_FIELD;
 import static org.kaleidofoundry.messaging.ClientContextBuilder.CONSUMER_DESTINATION;
 import static org.kaleidofoundry.messaging.ClientContextBuilder.CONSUMER_MESSAGE_SELECTOR_PROPERTY;
 import static org.kaleidofoundry.messaging.ClientContextBuilder.CONSUMER_NOLOCAL_PROPERTY;
 
 import java.io.ByteArrayOutputStream;
 import java.io.Serializable;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.jms.BytesMessage;
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.MapMessage;
 import javax.jms.MessageConsumer;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 import javax.jms.StreamMessage;
 import javax.jms.TextMessage;
 
 import org.kaleidofoundry.core.context.RuntimeContext;
 import org.kaleidofoundry.core.lang.annotation.NotNull;
 import org.kaleidofoundry.core.lang.annotation.Task;
 import org.kaleidofoundry.core.lang.annotation.Tasks;
 import org.kaleidofoundry.core.plugin.Declare;
 import org.kaleidofoundry.messaging.AbstractConsumer;
 import org.kaleidofoundry.messaging.AbstractMessage;
 import org.kaleidofoundry.messaging.BaseMessage;
 import org.kaleidofoundry.messaging.Consumer;
 import org.kaleidofoundry.messaging.JavaBeanMessage;
 import org.kaleidofoundry.messaging.Message;
 import org.kaleidofoundry.messaging.MessageException;
 import org.kaleidofoundry.messaging.MessageTimeoutException;
 import org.kaleidofoundry.messaging.MessageTypeEnum;
 import org.kaleidofoundry.messaging.MessagingConstants;
 import org.kaleidofoundry.messaging.MessagingException;
 import org.kaleidofoundry.messaging.TransportException;
 import org.kaleidofoundry.messaging.TransportRegistryException;
 
 /**
  * @author Jerome RADUGET
  */
 @Declare(MessagingConstants.JMS_CONSUMER_PLUGIN)
 @Tasks(tasks = { @Task(comment = "Handle manual jms session commit / rollback ? Keep a reference to the session in the message handle ?"),	
	@Task(comment = "Handle request / reply: http://activemq.apache.org/how-should-i-implement-request-response-with-jms.html") })
 public class JmsConsumer extends AbstractConsumer {
 
    private AbstractJmsTransport<ConnectionFactory, Connection, Destination> transport;
 
    /**
     * @param context
     * @throws TransportRegistryException
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public JmsConsumer(RuntimeContext<Consumer> context) {
 	super(context);
 
 	this.transport = (AbstractJmsTransport) super.transport;
    }
 
    /*
     * (non-Javadoc)
     * @see org.kaleidofoundry.messaging.AbstractConsumer#newWorker(java.lang.String)
     */
    @Override
    protected ConsumerWorker newWorker(String workerName, int workerIndex) throws TransportException {
 
 	return new ConsumerWorker(workerIndex, workerName) {
 
 	   private Connection connection;
 
 	   @Override
 	   public void init() throws TransportException {
 		connection = transport.createConnection();
 		try {
 		   connection.start();
 		} catch (JMSException jmse) {
 		   throw new TransportException("messaging.transport.jms.connection.create", jmse);
 		}
 	   }
 
 	   @Override
 	   public void receive(@NotNull MessageWrapper messageWrapper) {
 
 		boolean noLocal = context.getBoolean(CONSUMER_NOLOCAL_PROPERTY, false);
 		long messageTimeout = context.getLong(CONSUMER_RECEIVE_TIMEOUT_PROPERTY, -1l);
 		String messageSelector = context.getString(CONSUMER_MESSAGE_SELECTOR_PROPERTY);
 		String destinationJndiName = context.getString(CONSUMER_DESTINATION);
 
 		// create message consumer
 		final MessageConsumer jmsConsumer;
 		final Destination destination;
 		Session session = null;
 
 		try {
 		   session = transport.createSession(connection);
 		   destination =  transport.getDestination(session, destinationJndiName);
 		   jmsConsumer = session.createConsumer(destination, messageSelector, noLocal);
 		} catch (JMSException jmse) {
 		   messageWrapper.setError(new MessagingException("messaging.consumer.jms.create.error", jmse));
 		   return;
 		} catch (TransportException te) {
 		   messageWrapper.setError(te);
 		   return;
 		}
 
 		// handle received message
 		try {
 		   final javax.jms.Message jmsMessage;
 		   final Message message;
 
 		   if (messageTimeout > 0) {
 			jmsMessage = jmsConsumer.receive(messageTimeout);
 			if (jmsMessage == null) {
 			   messageWrapper.setError(MessageTimeoutException.buildConsumerTimeoutException(getName()));
 			   return;
 			}
 		   } else {
 			jmsMessage = jmsConsumer.receive();
 		   }
 
 		   // get message header properties
 		   Map<String, Object> parameters = new HashMap<String, Object>();
 		   if (jmsMessage.getPropertyNames() != null) {
 			@SuppressWarnings("unchecked")
 			Enumeration<String> messageProperties = jmsMessage.getPropertyNames();
 			while (messageProperties.hasMoreElements()) {
 			   String pname = messageProperties.nextElement();
 			   parameters.put(pname, jmsMessage.getObjectProperty(pname));
 			}
 		   }
 
 		   String type = jmsMessage.getStringProperty(MESSAGE_TYPE_FIELD);
 
 		   // switch on message type
 		   if (jmsMessage instanceof TextMessage && MessageTypeEnum.Xml.getCode().equalsIgnoreCase(type)) {
 
 			String content = ((TextMessage) jmsMessage).getText();
 			message = new org.kaleidofoundry.messaging.XmlMessage(content, parameters);
 
 		   } else if (jmsMessage instanceof TextMessage) {
 			String content = ((TextMessage) jmsMessage).getText();
 			message = new org.kaleidofoundry.messaging.TextMessage(content, parameters);
 
 		   } else if (jmsMessage instanceof BytesMessage) {
 			BytesMessage bytesMessage = (BytesMessage) jmsMessage;
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 
 			byte[] buffer = new byte[context.getInteger(CONSUMER_READ_BUFFER_SIZE, 256)];
 
 			int buffSize;
 			do {
 			   buffSize = bytesMessage.readBytes(buffer);
 			   if (buffSize > 0) {
 				out.write(buffer, 0, buffSize);
 			   }
 			} while (buffSize > 0);
 
 			message = new org.kaleidofoundry.messaging.BytesMessage(out.toByteArray(), parameters);
 
 		   } else if (jmsMessage instanceof MapMessage) {
 
 			MapMessage mapMessage = (MapMessage) jmsMessage;
 			Map<String, Object> mapParameters = new HashMap<String, Object>();
 
 			@SuppressWarnings("unchecked")
 			Enumeration<String> mapNames = mapMessage.getMapNames();
 			while (mapNames.hasMoreElements()) {
 			   String pname = mapNames.nextElement();
 			   mapParameters.put(pname, mapMessage.getObject(pname));
 			}
 
 			message = new BaseMessage(mapParameters);
 
 		   } else if (jmsMessage instanceof ObjectMessage) {
 			ObjectMessage objectMessage = (ObjectMessage) jmsMessage;
 			message = new JavaBeanMessage(objectMessage.getObject(), parameters);
 		   } else if (jmsMessage instanceof StreamMessage) {
 			StreamMessage streamMessage = (StreamMessage) jmsMessage;
 			message = new JavaBeanMessage((Serializable) streamMessage.readObject(), parameters);
 
 		   } else {
 			message = new BaseMessage(parameters);
 		   }
 
 		   ((AbstractMessage) message).setProviderId(jmsMessage.getJMSMessageID());
 		   ((AbstractMessage) message).setCorrelationId(jmsMessage.getJMSCorrelationID());
 
 		   messageWrapper.setProviderObject(jmsMessage);		   
 		   messageWrapper.setMessage(message);
 
 		} catch (JMSException jmse) {
 		   messageWrapper.setError(new MessagingException("messaging.consumer.jms.receive.error", jmse));
 		} finally {
 		   if (session != null) {
 			try {
 			   transport.closeSession(session);
 			} catch (TransportException te) {
 			   messageWrapper.setError(te);
 			}
 		   }
 		}
 	   }
 
 	   @Override
 	   public void acknowledge(MessageWrapper messageWrapper) throws MessagingException {
 
 		if (transport.getAcknowledgeMode() == javax.jms.Session.CLIENT_ACKNOWLEDGE) {
 		   if (messageWrapper.getProviderObject() != null && messageWrapper.getProviderObject() instanceof javax.jms.Message) {
 			try {
 			   ((javax.jms.Message) messageWrapper.getProviderObject()).acknowledge();
 			} catch (JMSException jmse) {
 			   throw new MessageException("messaging.consumer.acknowledge.jms.error", jmse);
 			}
 		   }
 		}
 	   }
 
 	   @Override
 	   public void destroy() {
 		super.destroy();
 		if (connection != null) {
 		   try {
 			transport.closeConnection(connection);
 		   } catch (TransportException te) {
 			LOGGER.error(MESSAGING_BUNDLE.getMessage("messaging.transport.jms.connection.close"), te);
 		   }
 		}
 	   }
 
 	   @Override
 	   public void interrupt() {
 		super.interrupt();
 		if (connection != null) {
 		   try {
 			connection.stop();
 		   } catch (JMSException jmse) {
 			LOGGER.error(MESSAGING_BUNDLE.getMessage("messaging.transport.jms.connection.stop"), jmse);
 		   }
 
 		}
 	   }
 
 	};
    }
 
 }
