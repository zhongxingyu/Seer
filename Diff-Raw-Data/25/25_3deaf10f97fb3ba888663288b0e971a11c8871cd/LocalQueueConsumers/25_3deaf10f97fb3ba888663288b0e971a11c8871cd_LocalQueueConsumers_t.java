 package pt.com.gcs.messaging;
 
 import java.util.Collections;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.apache.mina.common.IoSession;
 import org.apache.mina.common.WriteFuture;
import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.gcs.conf.AgentInfo;
 
 class LocalQueueConsumers
 {
 	private static Logger log = LoggerFactory.getLogger(LocalQueueConsumers.class);
 
 	private static final LocalQueueConsumers instance = new LocalQueueConsumers();
 
 	public static final AtomicLong ackedMessages = new AtomicLong(0L);
 
 	public static void acknowledgeMessage(Message msg, IoSession ioSession)
 	{
 		log.debug("Acknowledge message with Id: '{}'.", msg.getMessageId());
 
 		Message m = new Message(msg.getMessageId(), msg.getDestination(), "ACK");
 		m.setType((MessageType.ACK));
 		// WriteFuture wf = ioSession.write(m);
 		// wf.awaitUninterruptibly();
 		ioSession.write(m);
 	}
 
 	public static void add(String queueName, MessageListener listener)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(queueName);
 		if (listeners == null)
 		{
 			listeners = new CopyOnWriteArrayList<MessageListener>();
 		}
 		listeners.add(listener);
 		instance.localQueueConsumers.put(queueName, listeners);
 		instance.broadCastNewQueueConsumer(queueName);
 	}
 
 
 	public static void broadCastQueueInfo(String destinationName, String action, IoSession ioSession)
 	{
		if (StringUtils.isBlank(destinationName))
		{
			return;
		}
		
 		if (action.equals("CREATE"))
 		{
 			log.info("Tell {} about new queue consumer for: {}.", ioSession.getRemoteAddress().toString(), destinationName);
 		}
 		else if (action.equals("DELETE"))
 		{
 			log.info("Tell {} about deleted queue consumer of: {}.", ioSession.getRemoteAddress().toString(), destinationName);
 		}
 
 		Message m = new Message();
 		m.setType(MessageType.SYSTEM_QUEUE);
 		String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><destination>%s</destination></sysmessage>";
 		String payload = String.format(ptemplate, action, AgentInfo.getAgentName(), ioSession.getLocalAddress().toString(), destinationName);
 		m.setDestination(destinationName);
 		m.setContent(payload);
 		WriteFuture wf = ioSession.write(m);
 		wf.awaitUninterruptibly();
 	}
 	
 	public synchronized static void delete(String queueName)
 	{
 		instance.localQueueConsumers.remove(queueName);
 	}
 
 	public static Set<String> getBroadcastableQueues()
 	{
 		return Collections.unmodifiableSet(instance.localQueueConsumers.keySet());
 	}
 
 	public static boolean notify(Message message)
 	{
 		return instance.doNotify(message);
 	}
 
 	public static void remove(MessageListener listener)
 	{
 		if (listener != null)
 		{
 			Set<String> keys = instance.localQueueConsumers.keySet();
 			for (String queueName : keys)
 			{
 				CopyOnWriteArrayList<MessageListener> listeners = instance.localQueueConsumers.get(queueName);
 				if (listeners != null)
 				{
 					listeners.remove(listener);
 				}
 				instance.localQueueConsumers.remove(listeners);
 				instance.broadCastRemovedQueueConsumer(queueName);
 			}
 		}
 	}
 
 	public static int size()
 	{
 		return instance.localQueueConsumers.size();
 	}
 
 	public static int size(String destinationName)
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
 			broadCastQueueInfo(destinationName, action, ioSession);
 		}
 	}
 
 	private void broadCastNewQueueConsumer(String topicName)
 	{
 		broadCastActionQueueConsumer(topicName, "CREATE");
 	}
 
 	private void broadCastRemovedQueueConsumer(String topicName)
 	{
 		broadCastActionQueueConsumer(topicName, "DELETE");
 	}
 
 	public boolean doNotify(Message message)
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
 				currentQEP = 0;
 				return listeners.get(currentQEP);
 			}
 		}
 	}
 
 }
