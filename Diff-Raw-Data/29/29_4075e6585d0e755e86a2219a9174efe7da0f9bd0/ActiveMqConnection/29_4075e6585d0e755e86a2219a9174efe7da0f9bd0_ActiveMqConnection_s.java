 package com.irdeto.activemq;
 
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 
 public class ActiveMqConnection { 
 	
 	private static ActiveMqConnection instance = new ActiveMqConnection();
 	
 	private ActiveMqConnection(){
 	}
 	
 	public static ActiveMqConnection getInstance() {
         return instance;
 	}
 
 	public void initialiseAMQ() {
 		LoadConfiguration.loadProperties("config.properties");
		ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(LoadConfiguration.url);
		Connection connection;
 		
 		try {
 			connection = connectionFactory.createConnection();
 			connection.start();
 
 			Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
 			Destination destination = session.createQueue(LoadConfiguration.queue);
 
 			MessageConsumer consumer = session.createConsumer(destination);
 			consumer.setMessageListener(new MessageListener() {
 
 				@Override
 				public void onMessage(Message message) {
 					if (message instanceof TextMessage) {
 						TextMessage textMessage = (TextMessage) message;
 						try {
 							System.out.println("Received message '"	+ textMessage.getText() + "'");
 						} catch (JMSException e) {
 							e.printStackTrace();
 						}
 					}
 				}
 			});
 		
 		} catch (JMSException e) {
 			System.err.println("Can't connect to ActiveMq! Check URL.");
 			e.printStackTrace();
 		} catch(Exception ex){
 			System.err.println("Problem connectiong to ActiveMq. Check the properties file and the ActiveMq instance");
 			ex.printStackTrace();
 		}
 	}
 }
