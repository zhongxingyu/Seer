 package ch.ethz.mlmq.scenario.impl;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 import java.util.logging.Logger;
 
 import ch.ethz.mlmq.client.ClientConfiguration;
 import ch.ethz.mlmq.dto.MessageDto;
 import ch.ethz.mlmq.dto.MessageQueryInfoDto;
 import ch.ethz.mlmq.dto.QueueDto;
 import ch.ethz.mlmq.exception.MlmqException;
 import ch.ethz.mlmq.logging.LoggerUtil;
 import ch.ethz.mlmq.scenario.ClientScenario;
 
 public class SendReceiveClient extends ClientScenario {
 	private static final Logger logger = Logger.getLogger(SendReceiveClient.class.getSimpleName());
 
 	private static final String NUMMESSAGE_KEY = "scenario.SimpleSendClient.numMessages";
 	private static final String WAITBETWEENMESSAGES_KEY = "scenario.SimpleSendClient.waitTimeBetweenMessages";
 
 	private final int numMessages;
 
 	private long waitTimeBetweenMessages;
 
 	/**
 	 * Queue of this client
 	 */
 	private QueueDto queue;
 
 	private Random rnd = new Random(System.currentTimeMillis());
 
 	private final List<QueueDto> queues = new ArrayList<>();
 
 	private List<QueueDto> publicQueues;
 
 	private final int totalNrOfClients;
 
 	private int receivedMessages;
 
 	private int sentMessages;
 
 	public SendReceiveClient(ClientConfiguration config) {
 		super(config);
 
 		numMessages = config.getIntConfig(NUMMESSAGE_KEY);
 		waitTimeBetweenMessages = config.getLongConfig(WAITBETWEENMESSAGES_KEY);
 		totalNrOfClients = config.getAllClientScenarioMapping().size();
 	}
 
 	@Override
 	public void run() throws IOException, MlmqException {
 		client.register();
 
 		String queueName = "QueueOf" + config.getName();
 		queue = getOrCreateQueue(queueName);
 		publicQueues = createPublicQueues();
 
 		// time when we started to send messages
 		long startTime = System.currentTimeMillis();
 		for (int i = 0; i < numMessages; i++) {
 			try {
 				performAction();
 
 				long dt = System.currentTimeMillis() - startTime;
 
 				if (dt / waitTimeBetweenMessages <= i) {
 					long timeToSleep = waitTimeBetweenMessages - (dt % waitTimeBetweenMessages);
 					Thread.sleep(timeToSleep);
 					// else { We are behind in sending messages - don't sleep }
 				}
 			} catch (MlmqException e) {
 				logger.severe("MlmQEception while performing action - " + e + " " + LoggerUtil.getStackTraceString(e));
 
 			} catch (IOException e) {
 				logger.severe("IOEception while sending message - shutdown " + e + " " + LoggerUtil.getStackTraceString(e));
 
 				// stop sending messages in case of ioexception
 				i = numMessages;
 			} catch (Exception e) {
 				logger.warning("Error while sending message " + e + " " + LoggerUtil.getStackTraceString(e));
 			}
 		}
 	}
 
 	private List<QueueDto> createPublicQueues() throws IOException, MlmqException {
 		List<QueueDto> result = new ArrayList<>();
 		for (int i = 0; i < 10; i++) {
 			result.add(getOrCreateQueue("Public-" + i));
 		}
 		return result;
 	}
 
 	private void performAction() throws IOException, MlmqException {
 
 		if (rnd.nextBoolean()) {
 			// send a message
 
 			// yes, we are guessing clientId's.... omg
 			int clientId = rnd.nextInt(totalNrOfClients);
 
 			QueueDto receivingClientQueue = client.lookupClientQueue(clientId);
 
 			if (receivingClientQueue != null && rnd.nextBoolean()) {
 				// send personal message
				logger.info("Sending personal Message to Client " + clientId);
 				client.sendMessage(receivingClientQueue.getId(), createRandomMessage(), rnd.nextInt(10));
 				sentMessages++;
 
 			} else {
 				// send to a random public queue
 				int queueIndex = rnd.nextInt(publicQueues.size());
				logger.info("Sending public Message to Queue " + queueIndex);
 				client.sendMessage(publicQueues.get(queueIndex).getId(), createRandomMessage(), rnd.nextInt(10));
 				sentMessages++;
 			}
 
 		} else {
 			// receive a message
 			queues.clear();
 			int messagesForMe = client.queuesWithPendingMessages(queues, 10);
 
 			MessageQueryInfoDto msgQueryInfo = null;
 			if (messagesForMe > 0 && rnd.nextBoolean()) {
 				// receive personal message
 				msgQueryInfo = new MessageQueryInfoDto(queue);
 
 			} else if (queues.size() > 0) {
 				msgQueryInfo = new MessageQueryInfoDto(queues.get(0));
 			}
 
 			if (msgQueryInfo != null) {
 				MessageDto msg = client.dequeueMessage(msgQueryInfo);
 				if (msg != null) {
 					logger.info("Receive Message " + new String(msg.getContent()));
 					receivedMessages++;
 				}
 			}
 		}
 	}
 
 	private byte[] createRandomMessage() {
 		return ("Hi there I am a client with SentMessages[" + sentMessages + "] ReceivedMessages[" + receivedMessages + "] sent at " + new Date()).getBytes();
 	}
 
 	private QueueDto getOrCreateQueue(String queueName) throws IOException, MlmqException {
 		try {
 			return client.createQueue(queueName);
 		} catch (Exception e) {
			logger.info("Queue " + queueName + " already exists try to lookup queue");
 			return client.lookupClientQueue(queueName);
 		}
 	}
 }
