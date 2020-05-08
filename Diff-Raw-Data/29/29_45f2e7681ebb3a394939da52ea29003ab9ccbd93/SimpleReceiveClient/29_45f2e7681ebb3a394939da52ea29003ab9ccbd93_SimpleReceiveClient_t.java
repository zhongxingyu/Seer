 package ch.ethz.mlmq.scenario.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 import ch.ethz.mlmq.client.ClientConfiguration;
 import ch.ethz.mlmq.dto.ClientDto;
 import ch.ethz.mlmq.dto.MessageDto;
 import ch.ethz.mlmq.dto.MessageQueryInfoDto;
 import ch.ethz.mlmq.dto.QueueDto;
 import ch.ethz.mlmq.exception.MlmqException;
 import ch.ethz.mlmq.logging.LoggerUtil;
 import ch.ethz.mlmq.scenario.ClientScenario;
 
 public class SimpleReceiveClient extends ClientScenario {
 	private static final Logger logger = Logger.getLogger(SimpleReceiveClient.class.getSimpleName());
 
 	private static final String WAITBETWEENACTION_KEY = "scenario.SimpleReceiveClient.waitTimeBetweenActions";
 
 	private long waitTimeBetweenMessages;
 
 	private volatile boolean running;
 
 	public SimpleReceiveClient(ClientConfiguration config) {
 		super(config);
 
 		waitTimeBetweenMessages = config.getLongConfig(WAITBETWEENACTION_KEY);
 	}
 
 	@Override
 	public void run() throws IOException, MlmqException {
 		ClientDto me = client.register();
 		QueueDto myQueue = client.lookupClientQueue(me.getId());
 
 		List<QueueDto> queues = new ArrayList<>();
 		running = true;
 		while (running) {
 			try {
 				queues.clear();
 				int messagesForMe = client.queuesWithPendingMessages(queues, 10);
				logger.fine("Queues with pending messages " + queues);
				logger.fine("My Queue contains " + messagesForMe + " messages");
 
 				MessageDto msg = null;
 				MessageQueryInfoDto messageQueryInfo = new MessageQueryInfoDto(myQueue);
 				if (messagesForMe > 0) {
 
 					do {
 						msg = client.dequeueMessage(messageQueryInfo);
 						messagesForMe--;
						logger.fine("Dequeued personal Message " + new String(msg.getContent()));
 					} while (messagesForMe >= 0 && msg != null && running);
 				}
 
 				for (QueueDto queue : queues) {
 					messageQueryInfo = new MessageQueryInfoDto(queue);
 					msg = client.dequeueMessage(messageQueryInfo);
 					if (msg != null) {
						logger.fine("Dequeued Public Message From " + msg.getSender() + " Content " + new String(msg.getContent()));
 					}
 				}
 
 				try {
 					Thread.sleep(waitTimeBetweenMessages);
 				} catch (InterruptedException e) {
 					logger.severe(LoggerUtil.getStackTraceString(e));
 				}
 
 			} catch (MlmqException e) {
 				logger.severe("MlmQEception while sending message - try again - " + e + " " + LoggerUtil.getStackTraceString(e));
 			} catch (IOException e) {
 				logger.severe("MlmQEception while receiving message - " + e + " " + LoggerUtil.getStackTraceString(e));
 				running = false;
 			}
 		}
 
 	}
 
 	@Override
 	public void shutdown() {
 		super.shutdown();
 		running = false;
 	}
 }
