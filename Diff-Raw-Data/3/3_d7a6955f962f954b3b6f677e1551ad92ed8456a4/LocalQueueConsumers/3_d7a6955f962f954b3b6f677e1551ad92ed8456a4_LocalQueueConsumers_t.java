 package pt.com.gcs.messaging;
 
 import java.io.UnsupportedEncodingException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.apache.mina.core.session.IoSession;
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.types.NetBrokerMessage;
 import pt.com.gcs.conf.GcsInfo;
 import pt.com.gcs.net.IoSessionHelper;
 
 /**
  * LocalQueueConsumers maintains current local queue consumers.
  *
  */
 
 class LocalQueueConsumers
 {
 	private static Logger log = LoggerFactory.getLogger(LocalQueueConsumers.class);
 
 	private static final LocalQueueConsumers instance = new LocalQueueConsumers();
 
 	public static final AtomicLong ackedMessages = new AtomicLong(0L);
 
 	private static final HashMap<String, List<IoSession>> syncConsumers = new HashMap<String, List<IoSession>>();
 	private static final Set<String> _syncConsumers = new HashSet<String>();
 	
 	protected static void acknowledgeMessage(InternalMessage msg, IoSession ioSession)
 	{
 		log.debug("Acknowledge message with Id: '{}'.", msg.getMessageId());
 
 		try
 		{
 			NetBrokerMessage brkMsg = new NetBrokerMessage("ACK".getBytes("UTF-8"));
 
 			InternalMessage m = new InternalMessage(msg.getMessageId(), msg.getDestination(), brkMsg);
 			m.setType(MessageType.ACK);
 			ioSession.write(m);
 		}
 		catch (Throwable ct)
 		{
 			log.error(ct.getMessage(), ct);
 
 			try
 			{
 				ioSession.close();
 			}
 			catch (Throwable ict)
 			{
 				log.error(ict.getMessage(), ict);
 			}
 		}
 	}
 
 	protected synchronized static void add(String queueName, MessageListener listener)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(queueName);
 		if (listeners == null)
 		{
 			listeners = new CopyOnWriteArrayList<MessageListener>();
 		}
 		listeners.add(listener);
 		instance.localQueueConsumers.put(queueName, listeners);
 		if(!instance.registreadSyncConsumer(queueName)) // Broadcast if there is no registered sync consumers
 			instance.broadCastNewQueueConsumer(queueName);
 	}
 	
 	protected synchronized static void remove(MessageListener listener)
 	{
 		if (listener != null)
 		{
 			String queueName = listener.getDestinationName();
 			CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(queueName);
 			if (listeners != null)
 			{
 				listeners.remove(listener);
 
 				if (listeners.size() == 0)
 				{
 					instance.localQueueConsumers.remove(listeners);
 					System.out.println("localQueue is empty");
 					if(!instance.registreadSyncConsumer(queueName))// Broadcast if there is no registered sync consumers
 					{ 
 						instance.broadCastRemovedQueueConsumer(queueName);
 						System.out.println("    Sending broadcast");
 					}
 				}
 			}
 		}
 	}
 
 	protected static void addSyncConsumer(String queueName, IoSession session)
 	{
 		boolean broadcast = false;
 		synchronized (syncConsumers)
 		{
 			List<IoSession> sessionList = syncConsumers.get(queueName);
 			if(sessionList == null)
 			{
 				sessionList = new LinkedList<IoSession>();
 				syncConsumers.put(queueName, sessionList);
 				broadcast = !instance.registreadAsyncConsumer(queueName); // Broadcast if there is no registered async consumers
 			}
 			if(!sessionList.contains(session))
 				syncConsumers.get(queueName).add(session);
 		}
 		if(broadcast)
 			instance.broadCastNewQueueConsumer(queueName);
 	}
 	
 	protected static void removeSyncConsumer(String queueName, IoSession session)
 	{
 		boolean broadcast = false;
 		synchronized (syncConsumers)
 		{
 			List<IoSession> syncSessions = syncConsumers.get(queueName);
 			if (syncSessions == null)
 			{
 				log.info("Tried to remove a syn consumer queue, when there was none registread. Queue name '{}'", queueName);
 				return;
 			}
 			if(!syncSessions.contains(session))
 			{
 				log.info("Tried to remove a syn consumer session, when there was none registread. Session: '{}'", session);
 				return;
 			}
 			if(syncSessions.size() == 1)
 			{
 				syncConsumers.remove(queueName);
 				broadcast = !instance.registreadAsyncConsumer(queueName); // Broadcast if there is no registered async consumers
 			}
 			else
 			{
 				syncSessions.remove(session);
 			}
 			
 		}
 		if(broadcast)
 			instance.broadCastRemovedQueueConsumer(queueName);
 	}
 	
 	protected static void broadCastQueueInfo(String destinationName, String action, IoSession ioSession)
 	{
 		if (StringUtils.isBlank(destinationName))
 		{
 			return;
 		}
 
 		if (action.equals("CREATE"))
 		{
 			log.info("Tell {} about new queue consumer for: {}.", IoSessionHelper.getRemoteAddress(ioSession), destinationName);
 		}
 		else if (action.equals("DELETE"))
 		{
 			log.info("Tell {} about deleted queue consumer of: {}.", IoSessionHelper.getRemoteAddress(ioSession), destinationName);
 		}
 
 		String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><destination>%s</destination></sysmessage>";
 		String payload = String.format(ptemplate, action, GcsInfo.getAgentName(), ioSession.getLocalAddress().toString(), destinationName);
 
 		InternalMessage m = new InternalMessage();
 		NetBrokerMessage brkMsg;
 		try
 		{
 			brkMsg = new NetBrokerMessage(payload.getBytes("UTF-8"));
 			m.setType(MessageType.SYSTEM_QUEUE);
 
 			m.setDestination(destinationName);
 			m.setContent(brkMsg);
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			// This exception is never thrown because UTF-8 encoding is built-in
 			// in every JVM
 		}
 		SystemMessagesPublisher.sendMessage(m, ioSession);
 	}
 
 	protected synchronized static void delete(String queueName)
 	{
 		instance.localQueueConsumers.remove(queueName);
 	}
 
 	protected static Set<String> getBroadcastableQueues()
 	{
 		return Collections.unmodifiableSet(instance.localQueueConsumers.keySet());
 	}
 
 	protected static boolean notify(InternalMessage message)
 	{
 		return instance.doNotify(message);
 	}
 
 	
 	protected synchronized static void removeAllListeners()
 	{
 		Set<String> queueNameSet = instance.localQueueConsumers.keySet();
 
 		for (String queueName : queueNameSet)
 		{
 			CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(queueName);
 			listeners.clear();
 			instance.localQueueConsumers.remove(queueName);
 			instance.broadCastRemovedQueueConsumer(queueName);
 		}
 	}
 
 	protected static int size(String destinationName)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(destinationName);
 		if (listeners != null)
 		{
 			return listeners.size();
 		}
 		return 0;
 	}
 
 	private Map<String, CopyOnWriteArrayList<MessageListener>> localQueueConsumers = new ConcurrentHashMap<String, CopyOnWriteArrayList<MessageListener>>();
 
 	private int currentQEP = 0;
 
 	private Object rr_mutex = new Object();
 
 	private LocalQueueConsumers()
 	{
 	}
 
 	private void broadCastActionQueueConsumer(String destinationName, String action)
 	{
 		Set<IoSession> sessions = Gcs.getManagedConnectorSessions();
 
 		for (IoSession ioSession : sessions)
 		{
 			try
 			{
 				broadCastQueueInfo(destinationName, action, ioSession);
 			}
 			catch (Throwable t)
 			{
 				log.error(t.getMessage(), t);
 
 				try
 				{
 					ioSession.close();
 				}
 				catch (Throwable ct)
 				{
 					log.error(ct.getMessage(), ct);
 				}
 			}
 		}
 	}
 
 	private boolean registreadSyncConsumer(String queueName)
 	{
 		synchronized (syncConsumers)
 		{
 			return syncConsumers.containsKey(queueName);
 		}
 	}
 	
 	private boolean registreadAsyncConsumer(String queueName)
 	{
 		synchronized (LocalQueueConsumers.class)
 		{
 			CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(queueName);
 			boolean result = (listeners != null) && (listeners.size() != 0);
 			return result;
 		}
 	}
 	
 	
 	private void broadCastNewQueueConsumer(String destinationName)
 	{
 		broadCastActionQueueConsumer(destinationName, "CREATE");
 	}
 
 	private void broadCastRemovedQueueConsumer(String destinationName)
 	{
 		broadCastActionQueueConsumer(destinationName, "DELETE");
 	}
 
 	protected boolean doNotify(InternalMessage message)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = localQueueConsumers.get(message.getDestination());
 		if (listeners != null)
 		{
 			int n = listeners.size();
 			if (n > 0)
 			{
 				MessageListener listener = pick(listeners);
 				if (listener != null)
 				{
 					return listener.onMessage(message);
 				}
 			}
 		}
 
 		if (log.isDebugEnabled())
 		{
 			log.debug("There are no local listeners for queue: {}", message.getDestination());
 		}
 
 		return false;
 	}
 
 	private MessageListener pick(CopyOnWriteArrayList<MessageListener> listeners)
 	{
 		synchronized (rr_mutex)
 		{
 			int n = listeners.size();
 			if (n == 0)
 				return null;
 
 			if (currentQEP == (n - 1))
 			{
 				currentQEP = 0;
 			}
 			else
 			{
 				++currentQEP;
 			}
 
 			try
 			{
 				return listeners.get(currentQEP);
 			}
 			catch (Exception e)
 			{
 				try
 				{
 					currentQEP = 0;
 					return listeners.get(currentQEP);
 				}
 				catch (Throwable t)
 				{
 					return null;
 				}
 			}
 		}
 	}
 
 }
