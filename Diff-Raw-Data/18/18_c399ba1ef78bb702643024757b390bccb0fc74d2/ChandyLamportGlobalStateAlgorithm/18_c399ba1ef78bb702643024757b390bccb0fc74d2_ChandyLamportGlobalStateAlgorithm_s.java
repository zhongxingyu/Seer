 package nl.tudelft.ewi.in4150.group18;
 
 import java.util.Queue;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicReference;
 
 import nl.tudelft.in4150.group18.DistributedAlgorithm;
 import nl.tudelft.in4150.group18.common.IRemoteObject.IMessage;
 import nl.tudelft.in4150.group18.network.Address;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class ChandyLamportGlobalStateAlgorithm extends DistributedAlgorithm {
 
 	private static final Logger log = LoggerFactory.getLogger(ChandyLamportGlobalStateAlgorithm.class);
 
 	private final AtomicBoolean recordedState = new AtomicBoolean();
 	private final AtomicInteger markerId = new AtomicInteger();
	private final AtomicReference<MarkerCounter> counter = new AtomicReference<>(new MarkerCounter());
 	private final AtomicReference<NumberSender> sender = new AtomicReference<>();
 
 	/** Queue of messages since last received marker */
 	private final MultiQueue<IMessage> messageQueues = MultiQueue.create();
 	private final MultiQueue<IMessage> channelStates = MultiQueue.create();
 
 	/** On arrival of a message or marker either save local state or process the message one at a time */
 	private final Object lock = new Object();
 
 	@Override
 	public void start() {
 		if (sender.get() == null) {
 			NumberSender numberSender = new NumberSender(getRemoteAddresses()) {
 				@Override
 				void sendTransaction(Message transaction, Address to) throws Throwable {
 					send(transaction, to);
 				}
 			};
 
 			sender.set(numberSender);
 			numberSender.start();
 		} 
 		else {
 			captureState();
 		}
 	}
 	
 	public void captureState() {
 		recordLocalState(new Marker(getLocalAddress(), markerId.incrementAndGet()));
 	}
 
 	@Override
 	public void onMessage(IMessage message, Address from) {
 		synchronized (lock) {
 			log.info("{} - Received: {}", getLocalAddress(), message);
 			Queue<IMessage> queue = messageQueues.getQueue(from);
 
 			if (message instanceof Marker) {
 				Marker marker = (Marker) message;
				if (counter.get().getCount(marker) >= 2) {
					return;
				}
				
				counter.get().add(marker);
				
 				log.info("{} - Received Marker: {}", getLocalAddress(), marker);
 				if (!isStateRecorded()) {
 					channelStates.clearQueue(from);
 					recordLocalState(marker);
 				} else {
 					channelStates.setContents(from, queue);
 					log.info("{} - Channel state is: {}", getLocalAddress(), channelStates.getQueue(from));
 					recordedState.set(false);
 				}
 			} else {
 				if (isStateRecorded()) {
 					queue.add(message);
 				} else {
 					while (!queue.isEmpty()) {
 						handleMessage(queue.poll());
 					}
 					handleMessage(message);
 				}
 			}
 		}
 	}
 
 	private void handleMessage(IMessage message) {
 		log.info("{} - Handled message: {}", getLocalAddress(), message);
 	}
 
 	private void recordLocalState(Marker marker) {
 		synchronized (lock) {
 			recordInternalState();
 			broadcast(marker);
 
 			for (Address address : getRemoteAddresses()) {
 				messageQueues.clearQueue(address);
 			}
 		}
 	}
 
 	private void recordInternalState() {
 		NumberSender numberSender = sender.get();
 		numberSender.pause();
 		log.info("{} - Recording internal state...", getLocalAddress());
 		try {
 			//TODO actually record the local state: for instance where are we with sending or calculating stuff
			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			log.error(e.getMessage(), e);
 		}
 		log.info("{} - Recorded internal state: {}", getLocalAddress(), numberSender.getId());
 		recordedState.set(true);
 		numberSender.resume();
 	}
 
 	private boolean isStateRecorded() {
 		return recordedState.get();
 	}
 }
