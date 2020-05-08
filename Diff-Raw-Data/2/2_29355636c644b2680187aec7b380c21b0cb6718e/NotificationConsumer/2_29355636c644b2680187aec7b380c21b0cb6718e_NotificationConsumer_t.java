 package com.collective.jms;
 
 import java.io.IOException;
 import java.nio.CharBuffer;
 
 import javax.jms.Connection;
 import javax.jms.JMSException;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.Queue;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 
 import com.collective.manager.ConnectionManager;
 import com.collective.message.JSonHelper;
 import com.collective.message.NotificationMessage;
 import com.collective.server.WebsocketMessageInbound;
 
 public class NotificationConsumer {
 
 	private Connection connection = null;
 	private Session session = null;
 	private MessageConsumer consumer = null;
 	private static NotificationConsumer notificationConsumer;
 
 	private final String QUEUE_HOST = "tcp://localhost:61616";
 	private final String QUEUE_NAME = "ActiveMQ";
 
 	private NotificationConsumer() {
 		init();
 	}
 
 	public synchronized static NotificationConsumer getInstance() {
 
 		try {
 			if (notificationConsumer == null) {
 				notificationConsumer = new NotificationConsumer();
 			}
 		} catch (Exception e) {
 			notificationConsumer.shutdown();
 		}
 		return notificationConsumer;
 	}
 
 	public void init() {
 		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(QUEUE_HOST);
 		try {
 			connection = connectionFactory.createConnection();
 			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 			Queue queue = session.createQueue(QUEUE_NAME);
 			MessageConsumer consumer = session.createConsumer(queue);
 			consumer.setMessageListener(listener);
 			connection.start();
 		} catch (JMSException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public void shutdown() {
 		try {
 			if (consumer != null)
 				consumer.close();
 		} catch (Exception e) {
 			System.out.println("Could not close producer " + e.getMessage());
 		}
 		try {
 			if (session != null)
 				session.close();
 		} catch (Exception e) {
 			System.out.println("Could not close session " + e.getMessage());
 		}
 		try {
 			if (connection != null)
 				connection.close();
 		} catch (Exception e) {
 			System.out.println("Could not close connection " + e.getMessage());
 		}
 	}
 
 	public void addConnection(int userId, WebsocketMessageInbound connection) {
 		ConnectionManager.activeConnectionList.put(userId, connection);
 	}
 
 	public void removeConnectionByUid(int userId) {
 		ConnectionManager.activeConnectionList.remove(userId);
 	}
 
 	public void removeConnectionByConnection(WebsocketMessageInbound connection) {
 		ConnectionManager.activeConnectionList.values().remove(connection);
 	}
 
 	private final MessageListener listener = new MessageListener() {
 
 		@Override
 		public void onMessage(javax.jms.Message message) {
 			if (message != null) {
 				if (message instanceof TextMessage) {
 					try {
 						NotificationMessage msg = (NotificationMessage) JSonHelper
 								.JSONStringToObject(((TextMessage) message).getText(),
 										NotificationMessage.class);
 						CharBuffer buffer = CharBuffer.wrap(((TextMessage) message).getText());
 
 						if (msg.isGlobalMessage()) {
 							for (WebsocketMessageInbound connections : ConnectionManager.activeConnectionList
 									.values()) {
 								buffer.position(0);
 								connections.getWsOutbound().writeTextMessage(buffer);
 							}
 						} else {
 							if (ConnectionManager.activeConnectionList.containsKey(msg.getUserId())) {
 								ConnectionManager.activeConnectionList.get(msg.getUserId())
 										.getWsOutbound().writeTextMessage(buffer);
 
 							}
 						}
 					} catch (IOException e1) {
 						e1.printStackTrace();
 					} catch (JMSException e1) {
 						e1.printStackTrace();
 					}
 				}
 			}
 		}
	
 }
