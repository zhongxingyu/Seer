 package pt.com.gcs.messaging;
 
 import java.nio.charset.Charset;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArraySet;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.caudexorigo.text.StringUtils;
 import org.jboss.netty.channel.Channel;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.types.ForwardResult;
 import pt.com.broker.types.ForwardResult.Result;
 import pt.com.broker.types.MessageListener;
 import pt.com.broker.types.MessageListener.MessageListenerState;
 import pt.com.broker.types.MessageListenerEventChangeHandler;
 import pt.com.broker.types.NetAction;
 import pt.com.broker.types.NetAction.DestinationType;
 import pt.com.broker.types.NetBrokerMessage;
 import pt.com.broker.types.NetMessage;
 import pt.com.broker.types.NetNotification;
 import pt.com.gcs.conf.GcsInfo;
 import pt.com.gcs.conf.GlobalConfig;
 
 /**
  * QueueProcessor provides several queue related features, representing each instance a distinct queue.
  */
 
 public class QueueProcessor
 {
 	private static Logger log = LoggerFactory.getLogger(QueueProcessor.class);
 	private static final ForwardResult failed = new ForwardResult(Result.FAILED);
 	private static final Charset UTF8 = Charset.forName("UTF-8");
 	protected final AtomicBoolean emptyQueueInfoDisplay = new AtomicBoolean(false);
 
 	protected final AtomicLong counter = new AtomicLong(0);
 
 	private final AtomicBoolean isWorking = new AtomicBoolean(false);
 
 	private final CopyOnWriteArraySet<MessageListener> localQueueListeners = new CopyOnWriteArraySet<MessageListener>();
 
 	private final String queueName;
 
 	private final Set<MessageListener> remoteQueueListeners = new CopyOnWriteArraySet<MessageListener>();
 
 	private final AtomicLong sequence;
 
 	private final BDBStorage storage;
 
 	private TopicToQueueDispatcher topicFwd;
 
 	private final QueueStatistics queueStatistics = new QueueStatistics();
 
 	private final AtomicBoolean deliveringMessages = new AtomicBoolean(false);
 
 	private final AtomicLong nextRun = new AtomicLong(Long.MAX_VALUE);
 
 	private final AtomicLong lastCycle = new AtomicLong(Long.MAX_VALUE);
 
 	private final AtomicLong currentIdx = new AtomicLong(0);
 
 	protected QueueProcessor(String queueName)
 	{
 		if (StringUtils.isBlank(queueName))
 		{
 			throw new IllegalArgumentException("Queue names can not be blank");
 		}
 
 		this.queueName = queueName;
 
 		storage = new BDBStorage(this);
 
 		long cnt = storage.count();
 
 		if (cnt == 0)
 		{
 			sequence = new AtomicLong(0L);
 			counter.set(0);
 		}
 		else
 		{
 			sequence = new AtomicLong(storage.getLastSequenceValue());
 			counter.set(cnt);
 		}
 
 		if (GlobalConfig.supportVirtualQueues())
 		{
 			createDispatcher();
 		}
 
 		log.info("Create Queue Processor for '{}'.", queueName);
 		log.info("Queue '{}' has {} message(s).", queueName, getQueuedMessagesCount());
 	}
 
 	protected void ack(final String msgId)
 	{
 		if (log.isDebugEnabled())
 		{
 			log.debug("Ack message . MsgId: '{}'.", msgId);
 		}
 
 		if (storage.deleteMessage(msgId))
 		{
 			counter.decrementAndGet();
 		}
 	}
 
 	public void add(MessageListener listener)
 	{
 		if (listener != null)
 		{
 			boolean success = false;
 			if (listener.getType() == MessageListener.Type.LOCAL)
 			{
 				success = addLocal(listener);
 			}
 			else if (listener.getType() == MessageListener.Type.REMOTE)
 			{
 				success = addRemote(listener);
 			}
 			if (success)
 			{
 				listener.addStateChangeListener(getMessageListenerEventChangeHandler());
 
 				deliverMessages();
 			}
 		}
 		else
 		{
 			throw new IllegalArgumentException(String.format("Cannot add null listener to queue '%s'", queueName));
 		}
 	}
 
 	private boolean addLocal(MessageListener listener)
 	{
 		synchronized (localQueueListeners)
 		{
 			if (localQueueListeners.add(listener))
 			{
 				if (localQueueListeners.size() == 1)
 				{
 					broadCastNewQueueConsumer(listener);
 				}
 				log.info("Add listener -> '{}'", listener.toString());
 				return true;
 			}
 			return false;
 		}
 	}
 
 	private boolean addRemote(MessageListener listener)
 	{
 		synchronized (remoteQueueListeners)
 		{
 			if (remoteQueueListeners.add(listener))
 			{
 				log.info("Add listener -> '{}'", listener.toString());
 				return true;
 			}
 			return false;
 		}
 	}
 
 	private void broadCastActionQueueConsumer(String action)
 	{
 		Set<Channel> sessions = Gcs.getManagedConnectorSessions();
 
 		for (Channel channel : sessions)
 		{
 			try
 			{
 				broadCastQueueInfo(action, channel);
 			}
 			catch (Throwable t)
 			{
 				log.error(t.getMessage(), t);
 
 				try
 				{
 					channel.close();
 				}
 				catch (Throwable ct)
 				{
 					log.error(ct.getMessage(), ct);
 				}
 			}
 		}
 	}
 
 	private void broadCastNewQueueConsumer(MessageListener listener)
 	{
 		log.info("Tell all peers about new queue consumer for: '{}' in channel '{}'", queueName, listener.getChannel().getRemoteAddressAsString());
 		broadCastActionQueueConsumer("CREATE");
 	}
 
 	protected void broadCastQueueInfo(String action, Channel channel)
 	{
 		String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><destination>%s</destination></sysmessage>";
 		String payload = String.format(ptemplate, action, GcsInfo.getAgentName(), channel.getLocalAddress().toString(), queueName);
 
 		NetBrokerMessage brkMsg = new NetBrokerMessage(payload.getBytes(UTF8));
 		brkMsg.setMessageId(MessageId.getMessageId());
 
 		NetNotification notification = new NetNotification("/system/peer", DestinationType.TOPIC, brkMsg, "/system/peer");
 
 		NetAction naction = new NetAction(NetAction.ActionType.NOTIFICATION);
 		naction.setNotificationMessage(notification);
 
 		NetMessage nmsg = new NetMessage(naction);
 
 		nmsg.getHeaders().put("TYPE", "SYSTEM_QUEUE");
 
 		SystemMessagesPublisher.sendMessage(nmsg, channel);
 	}
 
 	private void broadCastRemovedQueueConsumer(MessageListener listener)
 	{
 		log.info("Tell all peers about deleted queue consumer for: '{}' in channel '{}'", queueName, listener.getChannel().getRemoteAddressAsString());
 		broadCastActionQueueConsumer("DELETE");
 	}
 
 	public synchronized void clearStorage()
 	{
 		removeDispatcher();
 		storage.deleteQueue();
 	}
 
 	private void createDispatcher()
 	{
 		try
 		{
 			if (StringUtils.contains(queueName, "@"))
 			{
 				log.info("Get Dispatcher for: {}", queueName);
 
 				String topicName = StringUtils.substringAfter(queueName, "@");
 				if (StringUtils.isBlank(topicName))
 				{
 					String errorMessage = String.format("Can't create a topic dispatcher whose name is empty. VirtualQueue name: '%s'", queueName);
 					log.error(errorMessage);
 					throw new RuntimeException(errorMessage);
 				}
 				topicFwd = new TopicToQueueDispatcher(null, topicName, queueName);
 
 				VirtualQueueStorage.saveVirtualQueue(queueName);
 				TopicProcessor topicProcessor = TopicProcessorList.get(topicName);
 				if (topicProcessor != null)
 				{
 					topicProcessor.add(topicFwd, false);
 				}
 			}
 		}
 		catch (Throwable e)
 		{
 			topicFwd = null;
 			throw new RuntimeException(e);
 		}
 	}
 
 	public long decrementQueuedMessagesCount()
 	{
 		return counter.decrementAndGet();
 	}
 
 	public void deleteExpiredMessages()
 	{
 		if (!hasRecipient())
 		{
 			storage.deleteExpiredMessages();
 		}
 	}
 
 	/**
 	 * @param nmsg
 	 *            The message to deliver.
 	 * @param preferLocalConsumer
 	 *            If 'true' messages will be delivered to local consumers or remote consumers if there aren't any active local consumers. If 'false' messages will be delivered to local consumers, then to remote consumers.
 	 * @return ForwardResult.SUCCESS or ForwardResult.FAILED
 	 */
 	protected ForwardResult forward(NetMessage nmsg, boolean preferLocalConsumer)
 	{
 		ForwardResult result = notify(localQueueListeners, nmsg);
 
 		if (result.result == Result.FAILED)
 		{
			if ((!preferLocalConsumer) || (!hasActiveListeners(localQueueListeners)))
 			{
 				result = notify(remoteQueueListeners, nmsg);
 			}
 		}
 
 		if (result.result == Result.SUCCESS)
 		{
 			queueStatistics.newQueueMessageDelivered();
 		}
 
 		if (log.isDebugEnabled())
 		{
 			log.debug("forward-> isDelivered: " + result.result.toString());
 		}
 
 		return result;
 	}
 
 	public long getQueuedMessagesCount()
 	{
 		return counter.get();
 	}
 
 	public String getQueueName()
 	{
 		return queueName;
 	}
 
 	private boolean hasActiveListeners(Set<MessageListener> listeners)
 	{
 		for (MessageListener ml : listeners)
 		{
 			if (ml.isActive())
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	private boolean hasReadyListeners(Set<MessageListener> listeners)
 	{
 		for (MessageListener ml : listeners)
 		{
 			if (ml.isReady())
 			{
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	protected boolean hasRecipient()
 	{
 		if (hasReadyListeners(localQueueListeners))
 		{
 			return true;
 		}
 		if (hasActiveListeners(localQueueListeners))
 		{
 			// it has sync consumers that are active but not ready. So, ignore remote consumers
 			return false;
 		}
 
 		return hasReadyListeners(remoteQueueListeners);
 	}
 
 	public Set<MessageListener> localListeners()
 	{
 		return localQueueListeners;
 	}
 
 	private ForwardResult notify(Set<MessageListener> listeners, NetMessage nmsg)
 	{
 		int s = listeners.size();
 		if (s == 0)
 		{
 			return failed;
 		}
 
 		int n = (int) currentIdx.get();
 
 		try
 		{
 			int idx = 0;
 
 			// first we cycle the collection and only notify the first ready listener with an "index" greater than "current index"
 			for (MessageListener ml : listeners)
 			{
 				if (idx++ >= n)
 				{
 					if (ml.isReady())
 					{
 						currentIdx.set(idx);
 						return ml.onMessage(nmsg);
 					}
 				}
 			}
 
 			// if no ready listener was found we cycle through the collection again and try the listeners with an "index" lower than the "current index"
 			idx = 0;
 			for (MessageListener ml : listeners)
 			{
 				if (idx++ < n)
 				{
 					if (ml.isReady())
 					{
 						currentIdx.set(idx);
 						return ml.onMessage(nmsg);
 					}
 				}
 				else
 				{
 					break;
 				}
 			}
 
 			// oh well ... we tried. To need to forward current index
 			return failed;
 		}
 		catch (Throwable t)
 		{
 			return failed;
 		}
 	}
 
 	public Set<MessageListener> remoteListeners()
 	{
 		return remoteQueueListeners;
 	}
 
 	public void remove(MessageListener listener)
 	{
 		if (listener != null)
 		{
 			boolean removed = false;
 			if (listener.getType() == MessageListener.Type.LOCAL)
 			{
 				synchronized (localQueueListeners)
 				{
 					if (localQueueListeners.remove(listener))
 					{
 						log.info("Removed listener -> '{}'", listener.toString());
 
 						if (localQueueListeners.size() == 0)
 						{
 							broadCastRemovedQueueConsumer(listener);
 						}
 						removed = true;
 					}
 				}
 			}
 			if (listener.getType() == MessageListener.Type.REMOTE)
 			{
 				synchronized (remoteQueueListeners)
 				{
 					if (remoteQueueListeners.remove(listener))
 					{
 						log.info("Removed listener -> '{}'", listener.toString());
 						removed = true;
 					}
 				}
 			}
 			if (removed)
 			{
 
 				listener.removeStateChangeListener(getMessageListenerEventChangeHandler());
 			}
 		}
 		else
 		{
 			log.error(String.format("Cannot remove null listener to queue '%s'", queueName));
 		}
 	}
 
 	private void removeDispatcher()
 	{
 		try
 		{
 			if (topicFwd != null)
 			{
 				Gcs.removeAsyncConsumer(topicFwd);
 				VirtualQueueStorage.deleteVirtualQueue(getQueueName());
 			}
 		}
 		catch (Throwable e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void setSequenceNumber(long seqNumber)
 	{
 		sequence.set(seqNumber);
 	}
 
 	public int size()
 	{
 		return localQueueListeners.size() + remoteQueueListeners.size();
 	}
 
 	public long getLastCycle()
 	{
 		return lastCycle.get();
 	}
 
 	public void store(final NetMessage nmsg, boolean preferLocalConsumer)
 	{
 		try
 		{
 			long seq_nr = sequence.incrementAndGet();
 			String mid = storage.insert(nmsg, seq_nr, preferLocalConsumer);
 
 			if (log.isDebugEnabled())
 			{
 				log.debug(String.format("Stored message with id '%s'.", mid));
 			}
 
 			counter.incrementAndGet();
 
 			deliverMessages();
 		}
 		catch (Throwable t)
 		{
 			throw new RuntimeException(t);
 		}
 	}
 
 	protected final void wakeup()
 	{
 		if (isWorking.getAndSet(true))
 		{
 			if (log.isDebugEnabled())
 			{
 				log.debug("Queue '{}' is running, skip wakeup", queueName);
 			}
 			return;
 		}
 
 		long cnt = getQueuedMessagesCount();
 
 		if (cnt > 0)
 		{
 			emptyQueueInfoDisplay.set(false);
 
 			if (hasReadyListeners(localQueueListeners) || hasReadyListeners(remoteQueueListeners))
 			{
 				try
 				{
 					if (log.isDebugEnabled())
 					{
 						log.debug("Wakeup queue '{}'", queueName);
 					}
 
 					long nextCycleDelay = storage.recoverMessages();
 
 					long now = System.currentTimeMillis();
 					long nextCycleTime = now + nextCycleDelay;
 
 					if (((nextCycleDelay != 0) && (nextCycleTime < nextRun.get())) || ((nextCycleDelay != 0) && (nextRun.get() < now)))
 					{
 						/*
 						 * Schedule a new delivery if: - the nextCycleTime is earlier than a previous scheduled delivery, or - the nextRun is previous to 'now'
 						 */
 
 						nextRun.set(nextCycleTime);
 						GcsExecutor.schedule(new Runnable()
 						{
 							@Override
 							public void run()
 							{
 								deliverMessages();
 							}
 						}, nextCycleDelay, TimeUnit.MILLISECONDS);
 					}
 				}
 				catch (Throwable t)
 				{
 					log.error(t.getMessage(), t);
 					throw new RuntimeException(t);
 				}
 				finally
 				{
 					isWorking.set(false);
 				}
 			}
 		}
 		isWorking.set(false);
 	}
 
 	public QueueStatistics getQueueStatistics()
 	{
 		return queueStatistics;
 	}
 
 	/***
 	 * Events
 	 */
 
 	private AtomicBoolean deliveryRequest = new AtomicBoolean(false);
 
 	// Calling methods have to ensure that deliveringMessages was set to true;
 	protected boolean deliverMessages()
 	{
 		boolean set = deliveringMessages.compareAndSet(false, true);
 		deliveryRequest.set(true);
 		if (set)
 		{
 			// Messages were not being delivered (deliveringMessages was 'false'). deliveringMessages was set to 'true' and let the delivery begin.
 			GcsExecutor.execute(new Runnable()
 			{
 				@Override
 				public void run()
 				{
 					while (deliveryRequest.compareAndSet(true, false))
 					{
 						wakeup();
 					}
 					deliveringMessages.set(false);
 					lastCycle.set(System.currentTimeMillis());
 				}
 			});
 		}
 		return set;
 	}
 
 	private MessageListenerEventChangeHandler msgListenterEventHandler = new MessageListenerEventChangeHandler()
 	{
 		@Override
 		public void stateChanged(MessageListener messageListener, MessageListenerState state)
 		{
 			if (messageListener.isReady() && messageListener.isActive())
 			{
 				deliverMessages();
 			}
 		}
 	};
 
 	private MessageListenerEventChangeHandler getMessageListenerEventChangeHandler()
 	{
 		return msgListenterEventHandler;
 	}
 }
