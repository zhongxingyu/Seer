 package name.felixbecker.hornetq.services;
 
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.apache.log4j.Logger;
 import org.hornetq.api.core.HornetQException;
 import org.hornetq.api.core.TransportConfiguration;
 import org.hornetq.api.core.client.ClientConsumer;
 import org.hornetq.api.core.client.ClientMessage;
 import org.hornetq.api.core.client.ClientSession;
 import org.hornetq.api.core.client.ClientSessionFactory;
 import org.hornetq.api.core.client.HornetQClient;
 import org.hornetq.api.core.client.MessageHandler;
 import org.hornetq.api.core.client.ServerLocator;
 import org.hornetq.core.remoting.impl.invm.InVMConnectorFactory;
 
 public class SampleConsumer implements MessageHandler {
 
 	private final Logger LOGGER = Logger.getLogger(SampleConsumer.class);
 	
 	
 	private final String consumerName;
 	private final ClientConsumer consumer;
 
 	private final ClientSession clientSession;
 	
	private AtomicLong messageCounter;
 	
 	public SampleConsumer(String consumerName, String consumerQueue) {
 	
 		this.consumerName = consumerName;
 		try {
 		
 			ServerLocator serverLocator = HornetQClient.createServerLocatorWithoutHA(new TransportConfiguration(InVMConnectorFactory.class.getName()));
 			ClientSessionFactory hornetQSessionFactory = serverLocator.createSessionFactory();
 			ClientSession clientSession = hornetQSessionFactory.createSession(true, true);
 			consumer = clientSession.createConsumer(consumerQueue);
 			consumer.setMessageHandler(this);
 			this.clientSession = clientSession;
 			clientSession.start();
 
 		} catch(Exception e){
 			throw new RuntimeException("creating consumer failed! " + e.getMessage(), e);
 		}
 
 		
 	}
 	
 	public void destroy(){
 		try {
 			consumer.close();
 			clientSession.close();
 		} catch (HornetQException e) {
 			throw new RuntimeException("destroying consumer failed! " + e.getMessage(), e);
 		}
 		
 	}
 
 	@Override
 	public void onMessage(ClientMessage message) {
 		LOGGER.info("Consumer "+ consumerName + "here! message: " + message.getStringProperty("content"));
 		try {
 			message.acknowledge();
 			clientSession.commit();
 			messageCounter.incrementAndGet();
 		} catch (HornetQException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public String getConsumerName() {
 		return consumerName;
 	}
 	
 	public synchronized long getMessageCounter(){
 		return messageCounter.get();
 	}
 	
 
 }
