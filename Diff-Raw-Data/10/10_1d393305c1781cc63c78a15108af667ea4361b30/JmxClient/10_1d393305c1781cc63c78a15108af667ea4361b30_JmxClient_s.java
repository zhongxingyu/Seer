 package multiplexer.jmx;
 
 import static multiplexer.jmx.internal.Queues.pollUninterruptibly;
 
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentMap;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.TimeUnit;
 
 import multiplexer.Multiplexer.BackendForPacketSearch;
 import multiplexer.Multiplexer.MultiplexerMessage;
 import multiplexer.Multiplexer.MultiplexerMessage.Builder;
 import multiplexer.constants.Peers;
 import multiplexer.constants.Types;
 import multiplexer.jmx.exceptions.BackendUnreachableException;
 import multiplexer.jmx.exceptions.OperationFailedException;
 import multiplexer.jmx.exceptions.OperationTimeoutException;
 
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.util.Timeout;
 import org.jboss.netty.util.TimerTask;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.protobuf.ByteString;
 
 /**
  * A Multiplexer server's client class. It provides methods for connecting with
  * a specified address, creating, sending and receiving Multiplexer messages.
  * {@code event} method sends a specified message to all connected Multiplexer
  * servers while {@code query} method is useful for getting a remote backend
  * solve a task.
  * 
  * @author Kasia Findeisen
  * @author Piotr Findeisen
  * 
  */
 public class JmxClient {
 
 	private static final Logger logger = LoggerFactory
 		.getLogger(JmxClient.class);
 
 	protected final ConnectionsManager connectionsManager;
 
 	private ConcurrentMap<Long, BlockingQueue<IncomingMessageData>> queryResponses = new ConcurrentHashMap<Long, BlockingQueue<IncomingMessageData>>();
 	private BlockingQueue<IncomingMessageData> messageQueue = new LinkedBlockingQueue<IncomingMessageData>();
 
 	/**
 	 * Creates a new instance of a specified type ({@code instanceType}). Sets
 	 * the instance's {@link ConnectionsManager} and defines a callback method
 	 * {@code onMessageReceived}. The method, invoked by the {@code
 	 * ConnectionsManager}, puts the {@link IncomingMessageData}, accordingly to
 	 * it's {@code referenceId} in one of the instance's {@link BlockingQueue}s,
 	 * from where it might be read by method {@code receive()}.
 	 * 
 	 * @param instanceType
 	 *            client types are defined in {@link Peers}
 	 */
 	public JmxClient(int instanceType) {
 		connectionsManager = new ConnectionsManager(instanceType);
 		connectionsManager
 			.setMessageReceivedListener(new MessageReceivedListener() {
 
 				@Override
 				public void onMessageReceived(MultiplexerMessage message,
 					Connection connection) {
 					long id = message.getReferences();
 					IncomingMessageData msg = new IncomingMessageData(message,
 						connection);
 					BlockingQueue<IncomingMessageData> queryQueue = queryResponses
 						.get(id);
 					if (queryQueue == null) {
 						messageQueue.add(msg);
 					} else {
 						queryQueue.add(msg);
 					}
 
 				}
 			});
 	}
 
 	/**
 	 * Begins asynchronously an attempt of connection with the specified {@code
 	 * address}.
 	 * 
 	 * @param address
 	 * @return a future object which notifies when this connection attempt
 	 *         succeeds or fails
 	 */
 	public ChannelFuture asyncConnect(SocketAddress address) {
 		return connectionsManager.asyncConnect(address);
 	}
 
 	/**
 	 * Connects synchronously with the specified {@code address}.
 	 * 
 	 * @param address
 	 */
 	public void connect(SocketAddress address) {
 		asyncConnect(address).awaitUninterruptibly();
 	}
 
 	/**
 	 * Creates a new {@link MultiplexerMessage} with the specified
 	 * {@link ByteString} as a {@code message} and a specified {@code type}.
 	 * Also, fields {@code id}, {@code from} and {@code timestamp} are set.
 	 * 
 	 * @param message
 	 * @param type
 	 *            message types are defined in {@link Types}
 	 */
 	public MultiplexerMessage createMessage(ByteString message, int type) {
 		return connectionsManager.createMessage(message, type);
 	}
 
 	/**
 	 * Creates a new {@link MultiplexerMessage} using the specified
 	 * {@link Builder}. Allows to set all the message's fields. However, fields
 	 * {@code id}, {@code from} and {@code timestamp} are reset.
 	 * 
 	 * @param message
 	 *            use {@code MultiplexerMessage.newBuilder()} to obtain a
 	 *            builder. Use {@code builder.setAttributeName(name)} to set
 	 *            {@code attributeName}. {@link MultiplexerMessage}'s attributes
 	 *            are defined in {@code Multiplexer.proto} ->
 	 *            {@link MultiplexerMessage}.
 	 */
 	public MultiplexerMessage createMessage(MultiplexerMessage.Builder message) {
 		return connectionsManager.createMessage(message);
 	}
 
 	/**
 	 * Creates new {@link Builder} with {@code from}, {@code id} and {@code
 	 * timestamp} already filled in.
 	 * 
 	 * @return new builder with prefilled fields
 	 */
 	public MultiplexerMessage.Builder createMessageBuilder() {
 		return connectionsManager.createMessageBuilder();
 	}
 
 	/**
 	 * Sends one or more copies of {@code message}, accordingly to specified
 	 * {@link SendingMethod}. {@code sendingMethod} might specify the receiver
 	 * or just specify the type and quantity of receivers. In case the receiver
 	 * should be any of a given type, it is chosen on a basis of round-robin
 	 * algorithm.
 	 * 
 	 * @param message
 	 * @param sendingMethod
 	 * @return a future object which notifies when this message sending attempt
 	 *         succeeds or fails
 	 */
 	public ChannelFutureGroup send(MultiplexerMessage message,
 		SendingMethod sendingMethod) {
 		return connectionsManager.sendMessage(message, sendingMethod);
 	}
 
 	/**
 	 * Attempts to send synchronously all pending messages.
 	 * 
 	 * @throws InterruptedException
 	 */
 	public void flush() throws InterruptedException {
 		connectionsManager.flushAll();
 	}
 
 	/**
 	 * Attempts to send synchronously all pending messages. Returns if the task
 	 * is completed or {@code timeout} expires.
 	 * 
 	 * @return true if and only if all asynchronous operations started prior to
 	 *         the call completed
 	 * @throws InterruptedException
 	 */
 	public boolean flush(long timeout, TimeUnit unit)
 		throws InterruptedException {
 		return connectionsManager.flushAll(timeout, unit);
 	}
 
 	/**
 	 * Attempts to send synchronously all pending messages. Returns if the task
 	 * is completed or {@code timeoutMillis} expires.
 	 * 
 	 * @return true if and only if all asynchronous operations started prior to
 	 *         the call completed
 	 * @throws InterruptedException
 	 */
 	public boolean flush(long timeoutMillis) throws InterruptedException {
 		return connectionsManager.flushAll(timeoutMillis);
 	}
 
 	/**
 	 * Tries to take one message from {@link BlockingQueue} of received
 	 * messages. Blocks until any message is available.
 	 * 
 	 * @throws InterruptedException
 	 * @return includes the received message and a connection
 	 */
 	public IncomingMessageData receive() throws InterruptedException {
 		return messageQueue.take();
 	}
 
 	/**
 	 * Tries to take one message from {@link BlockingQueue} of received
 	 * messages. Blocks until any message is available or timeout occurs.
 	 * 
 	 * @return includes the received message and a connection
 	 * @throws InterruptedException
 	 */
 	public IncomingMessageData receive(long timeout, TimeUnit unit)
 		throws InterruptedException {
 
 		return messageQueue.poll(timeout, unit);
 	}
 
 	/**
 	 * Sends a specified {@code message} through all connected Multiplexer
 	 * servers.
 	 * 
 	 * @param message
 	 * @return a future object which notifies when this connection attempt
 	 *         succeeds or fails. Call {@code ChannelFutureGroup.size()} to get
 	 *         the number of copies of {@code message} sent.
 	 */
 	public ChannelFutureGroup event(MultiplexerMessage message) {
 		return send(message, SendingMethod.THROUGH_ALL);
 	}
 
 	/**
 	 * Attempts to send out a message with specified content ({@code message})
 	 * and of specified type ({@code MessageType}) and get an answer from remote
 	 * backend.
 	 * 
 	 * This is a 3-phase algorithm, however it may end at any stage on proper
 	 * conditions. In phase 1, the message is sent through one Multiplexer. If
 	 * the answer doesn't appear within specified amount of time ({@code
 	 * timeout}), the algorithm enters phase 2. A special message, aimed to find
 	 * a proper backend is sent through all connected Mulitplexer servers
 	 * (method {@code event}). If an answer from the backend comes within a
 	 * specified amount of time ({@code timeout}), the algorithm enters phase 3.
 	 * The message is sent directly to the backend, and another {@code timeout}
 	 * is given to receive the answer.
 	 * 
 	 * The algorithm only reads it's own messages. Other messages,
 	 * simultaneously received by the {@code Client}, are not affected.
 	 * 
 	 * @param message
 	 *            the request to be sent over Multiplexer connection
 	 * @param messageType
 	 *            type of the request, from which a Multiplexer can deduce the
 	 *            right backend type
 	 * @param timeout
 	 *            each of the 3 phases of the algorithm has this time limit,
 	 *            measured in milliseconds
 	 * @return an answer message
 	 * @throws OperationFailedException
 	 *             when operation times out or there are no reachable backends
 	 *             that can handle the query
 	 */
 	public IncomingMessageData query(final ByteString message,
 		final int messageType, long timeout) throws OperationFailedException {
 
 		final List<Long> queryMessageIds = new ArrayList<Long>(3);
 		try {
 			return query(message, messageType, timeout, queryMessageIds);
 		} finally {
 			// remove IDs of messages sent during this query from
 			// `queryResponses'
 			removeFromQueryResponses(queryMessageIds, 5, TimeUnit.SECONDS);
 		}
 	}
 
 	/**
 	 * A {@link JmxClient#query(ByteString, int, long)}, which registers its
 	 * {@code queryQueue} with {@code queryMessageIds} and does not de-register
 	 * it.
 	 */
 	protected IncomingMessageData query(final ByteString message,
 		final int messageType, long timeout, List<Long> queryMessageIds)
 		throws OperationFailedException {
 
 		// TODO: support Types.BACKEND_ERROR
 
 		final BlockingQueue<IncomingMessageData> queryQueue = new LinkedBlockingQueue<IncomingMessageData>();
 		MultiplexerMessage queryMessage = createMessage(message, messageType);
 		boolean phase1DeliveryError = false;
 		boolean phase3DeliveryError = false;
 
 		final long queryId = queryMessage.getId();
 
 		// Send queryMessage and wait for the answer to arrive.
 		addMessageIdToQueryResponses(queryId, queryMessageIds, queryQueue);
 		send(queryMessage, SendingMethod.THROUGH_ONE);
 		IncomingMessageData answer = pollUninterruptibly(queryQueue, timeout);
 		if (answer != null) {
 			if (answer.getMessage().getType() != Types.DELIVERY_ERROR) {
 				return answer;
 			} else
 				phase1DeliveryError = true;
 		}
 
 		MultiplexerMessage backendSearchMessage = makeBackendForPacketSearch(messageType);
 		final long backendSearchMessageId = backendSearchMessage.getId();
 
 		addMessageIdToQueryResponses(backendSearchMessageId, queryMessageIds,
 			queryQueue);
 		int activeBackendSearches = event(backendSearchMessage).size();
 
 		answer = null;
 		long answerFromId;
 		TimeoutCounter timer = new TimeoutCounter(timeout);
 		for (;;) { // The loop breaks by getting the answer or timeout.
 
 			answer = pollUninterruptibly(queryQueue, timer);
 
 			if (answer == null) {
 				throw new OperationTimeoutException("query phase 2 timed out");
 			}
 
 			long references = answer.getMessage().getReferences();
 			int type = answer.getMessage().getType();
 
 			if (type == Types.DELIVERY_ERROR && references == queryId) {
 				phase1DeliveryError = true;
 				continue;
 			}
 
 			if (type == Types.DELIVERY_ERROR) {
 				assert type == backendSearchMessageId;
 				activeBackendSearches--;
 				if (activeBackendSearches == 0) {
 					throw new BackendUnreachableException(
 						"query phase 2 rejected by all peers");
 				}
 				continue;
 			}
 
 			if (references == queryId) {
 				return answer;
 			}
 
 			if (references == backendSearchMessageId) {
 				answerFromId = answer.getMessage().getFrom();
 				MultiplexerMessage backendQueryMessage = createMessage(MultiplexerMessage
 					.newBuilder().setMessage(message).setType(messageType)
 					.setTo(answerFromId));
 
 				final long backendQueryId = backendQueryMessage.getId();
 				addMessageIdToQueryResponses(backendQueryId, queryMessageIds,
 					queryQueue);
 				send(backendQueryMessage, SendingMethod.via(answer
 					.getConnection()));
 
 				answer = null;
 				timer = new TimeoutCounter(timeout);
 				for (;;) { // The loop breaks by getting the answer or timeout.
 
 					answer = pollUninterruptibly(queryQueue, timer);
 
 					if (answer == null) {
 						throw new OperationTimeoutException(
 							"query phase 3 timed out");
 					}
 
 					references = answer.getMessage().getReferences();
 					type = answer.getMessage().getType();
 
 					if (references == backendSearchMessageId) {
 						continue;
 					}
 
 					if (type != Types.DELIVERY_ERROR
 						&& ((references == queryId) || (references == backendQueryId))) {
 						return answer;
 					}
 
 					if (references == queryId) {
 						assert type == Types.DELIVERY_ERROR;
 						if (phase3DeliveryError) {
 							throw new BackendUnreachableException(
 								"query phases 1 and 3 rejected");
 						} else {
 							phase1DeliveryError = true;
 							continue;
 						}
 					}
 
 					if (references == backendQueryId) {
 						assert type == Types.DELIVERY_ERROR;
 						if (phase1DeliveryError) {
 							throw new BackendUnreachableException(
 								"query phases 1 and 3 rejected");
 						} else {
 							phase3DeliveryError = true;
 							continue;
 						}
 					}
 					throw new AssertionError("Unreachable code.");
 				}
 			}
 			throw new AssertionError("Unreachanle code.");
 		}
 	}
 
 	/**
 	 * Registers {@code queryQueue} within {@link #queryResponses} at index
 	 * {@code messageId}. If registration is successful, {@code messageId} is
 	 * also added to {@code messageIds} collection, which should be later used
 	 * with {@link #removeFromQueryResponses} to de-register the {@code
 	 * queryQueue}.
 	 * 
 	 * @param messageId
 	 * @param messageIds
 	 */
 	private void addMessageIdToQueryResponses(long messageId,
 		Collection<Long> messageIds,
 		BlockingQueue<IncomingMessageData> queryQueue) {
 
 		messageIds.add(messageId);
 		// In case `put' throws, we don't need to remove messageId from
 		// messageIds -- it's OK to have redundant IDs there.
 		queryResponses.put(messageId, queryQueue);
 	}
 
 	/**
 	 * Removes message IDs from {@link JmxClient#queryResponses}. Never throws
 	 * any {@link Exception}.
 	 * 
 	 * @param messageIds
 	 *            IDs to be removed
 	 */
 	private void removeFromQueryResponses(final Collection<Long> messageIds,
 		long delay, TimeUnit unit) {
 		try {
 			TimerTask removalTask = new TimerTask() {
 
 				@Override
 				public void run(Timeout timeout) throws Exception {
 					if (timeout != null && timeout.isCancelled())
 						return;
 					for (long referentId : messageIds) {
 						queryResponses.remove(referentId);
 					}
 				}
 
 			};
 
 			if (delay == 0) {
 				removalTask.run(null);
 			} else {
 				connectionsManager.getTimer().newTimeout(removalTask, delay,
 					unit);
 			}
 		} catch (Exception e) {
 			logger.warn("Scheduling or executing removalTask failed.", e);
 		}
 	}
 
 	/**
 	 * Constructs new {@link MultiplexerMessage} searching for backends able to
 	 * handle query of type {@code messageType}.
 	 * 
 	 * @param messageType
 	 *            query type
 	 * @return constructed message
 	 */
 	private MultiplexerMessage makeBackendForPacketSearch(final int messageType) {
 		BackendForPacketSearch backendSearch = BackendForPacketSearch
 			.newBuilder().setPacketType(messageType).build();
 		MultiplexerMessage backendSearchMessage = createMessage(backendSearch
 			.toByteString(), Types.BACKEND_FOR_PACKET_SEARCH);
 		return backendSearchMessage;
 	}
 	
 	public long getInstanceId() {
 		return connectionsManager.getInstanceId();
 	}
 }
