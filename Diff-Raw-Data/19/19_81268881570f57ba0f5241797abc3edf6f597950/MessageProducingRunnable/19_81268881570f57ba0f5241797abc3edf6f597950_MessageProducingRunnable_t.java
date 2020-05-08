 package name.felixbecker.hornetq;
 
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 
 import org.apache.log4j.Logger;
 import org.hornetq.api.core.HornetQException;
 import org.hornetq.api.core.client.ClientMessage;
 import org.hornetq.api.core.client.ClientProducer;
 import org.hornetq.api.core.client.ClientSession;
 import org.hornetq.api.core.client.ClientSessionFactory;
 
 public class MessageProducingRunnable implements Runnable {
 
 	private final ClientSessionFactory clientSessionFactory;
 	
 	private static final Logger LOGGER = Logger.getLogger(MessageProducingRunnable.class);
 	
 	private volatile boolean shutdown = false;
 	
 	public MessageProducingRunnable(ClientSessionFactory clientSessionFactory) {
 		this.clientSessionFactory = clientSessionFactory;
 	}
 	
 	public void run() {
 
 		while(!shutdown){
 			try {
 				
 				LOGGER.debug("Sending message to horny hornet queue");
 				
 				final ClientSession session = clientSessionFactory.createSession(false, false); // no auto commit, no auto ack
 				ClientProducer producer = session.createProducer(TestConstants.TEST_ADDRESS);
 				
				final ClientMessage message = session.createMessage(true);
 					
				String hostname = InetAddress.getLocalHost().getHostName();
 					
				message.putStringProperty(TestConstants.TEST_MESSAGE_PROPERTY, "Message from " + hostname); 
 					
				producer.send(message);
 					
				session.commit();

 				producer.close();
				
 				session.close();
 				
 				Thread.sleep(1000);
 			} catch(InterruptedException e){
 				LOGGER.error("did you forget to shutdown this runnable in your context destroyed listener?", e);
 			} catch (HornetQException e) {
 				LOGGER.error("Failed to send message to hornetq!", e);
 			} catch (UnknownHostException e) {
 				LOGGER.error("you don't have a hostname for your localhost!", e);
 			}
 			
 		}
 		
 		LOGGER.info("sent messages");
 			
 	}
 	
 	
 	public void shutdown(){
 		shutdown = true;
 	}
 	
 }
