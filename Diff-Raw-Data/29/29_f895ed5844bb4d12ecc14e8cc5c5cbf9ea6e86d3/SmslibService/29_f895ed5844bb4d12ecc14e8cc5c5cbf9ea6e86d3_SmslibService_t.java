 package net.frontlinesms.camel.smslib;
 
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.smslib.CIncomingMessage;
 import org.smslib.COutgoingMessage;
 import org.smslib.CService;
import org.smslib.service.MessageClass;
 
 public class SmslibService {
 	static { System.out.println("SmsLibService class loaded."); }
 	
 	private final CService cService;
 	
 	private SmslibProducer producer;
 	private SmslibConsumer consumer;
 	
 	private boolean consumerRunning;
 	private boolean producerRunning;
 	
 	private MessageClass receiveMessageClass = MessageClass.ALL;
 
 //> CONSTRUCTORS
 	public SmslibService(CServiceFactory cServiceFactory, String uri, String remaining, Map<String, Object> parameters) {
 		this.cService = cServiceFactory.create(uri, remaining, parameters);
 		
 		Object receiveMessageClass = parameters.get("receiveMessageClass");
 		if(receiveMessageClass != null) {
 			if(receiveMessageClass instanceof MessageClass)
 				this.receiveMessageClass = (MessageClass) receiveMessageClass;
 			else warn("Could not set receiveMessageClass to value " + receiveMessageClass +
 					" of class " + receiveMessageClass.getClass());
 		}
 	}
 	
 //> ACCESSORS
 	public boolean isConsumerRunning() {
 		return consumerRunning;
 	}
 	
 	public boolean isProducerRunning() {
 		return producerRunning;
 	}
 	
 	public void setReceiveMessageClass(MessageClass receiveMessageClass) {
 		this.receiveMessageClass = receiveMessageClass;
 	}
 
 	public synchronized SmslibProducer getProducer() {
 		return producer;
 	}
 	public synchronized void setProducer(SmslibProducer producer) throws SmslibServiceException {
 		if(this.producer != null) {
 			throw new SmslibServiceException("Producer already set.");
 		}
 		this.producer = producer;
 	}
 
 	public synchronized SmslibConsumer getConsumer() {
 		return consumer;
 	}
 	public synchronized void setConsumer(SmslibConsumer consumer) throws SmslibServiceException {
 		if(this.consumer != null) {
 			throw new SmslibServiceException("Consumer already set.");
 		}
 		this.consumer = consumer;
 	}
 	
 //> SERVICE METHODS
 	public synchronized void startForConsumer() throws Exception {
 		assert(consumer != null);
 		consumerRunning = true;
 		new ReceiveThread().start();
 		startService();
 	}
 
 	public synchronized void startForProducer() throws Exception {
 		assert(producer != null);
 		try {
 			producerRunning = true;
 			startService();
 		} catch(Exception ex) {
 			producerRunning = false;
 			producer = null;
 			throw ex;
 		}
 	}
 	
 	public synchronized void stopForProducer() throws Exception {
 		assert(producer != null);
 		producerRunning = false;
 		stopIfUnused();
 	}
 	
 	public synchronized void stopForConsumer() throws Exception {
 		assert(consumer != null);
 		consumerRunning = false;
 		stopIfUnused();
 	}
 
 	private synchronized void stopIfUnused() throws Exception {
 		if(cService.isConnected()
 				&& !producerRunning
 				&& !consumerRunning) {
 			cService.disconnect();
 		}
 	}
 
 //> SEND/RECEIVE METHODS
 	private synchronized void startService() throws Exception {
 		if(!cService.isConnected()) {
 			cService.connect();
 		}
 	}
 
 	public void send(OutgoingSmslibCamelMessage message) throws Exception {
 		COutgoingMessage cMess = message.getCMessage();
 		this.cService.sendMessage(cMess);
 		if(cMess.getRefNo() < 0) {
 			throw new MessageRejectedException(cMess);
 		}
 	}
 
 	public void doReceive() throws Exception {
 		LinkedList<CIncomingMessage> messageList = new LinkedList<CIncomingMessage>();
 		this.cService.readMessages(messageList, receiveMessageClass);
 		for(CIncomingMessage m : messageList) {
 			this.consumer.accept(new IncomingSmslibCamelMessage(m));
 			// TODO deletion should be done in markRead() method in suitable class - sometimes deletion is
 			// not desired and this behaviour should not be forced
 			System.out.println("Deleting message...");
 			this.cService.deleteMessage(m);
 			System.out.println("Message deleted.");
 		}
 	}
 	
 	class ReceiveThread extends Thread {
 		public void run() {
 			while(consumerRunning) {
 				try {
 					doReceive();
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				try {
 					Thread.sleep(30000);
 				} catch (InterruptedException e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 			}
 		}
 	}
 	
 //> LOGGING METHODS
 	private void warn(String message) {
 		System.out.println("WARN: " + message);
 	}
 }
