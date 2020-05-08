 package pt.com.gcs.messaging;
 
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.apache.mina.common.IoSession;
 import org.apache.mina.common.WriteFuture;
import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.gcs.conf.AgentInfo;
 
 class LocalTopicConsumers
 {
 	private static Logger log = LoggerFactory.getLogger(LocalTopicConsumers.class);
 
 	private static final LocalTopicConsumers instance = new LocalTopicConsumers();
 
 	public static void add(String topicName, MessageListener listener, boolean broadcast)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localTopicConsumers.get(topicName);
 		if (listeners == null)
 		{
 			listeners = new CopyOnWriteArrayList<MessageListener>();
 		}
 		listeners.add(listener);
 		instance.localTopicConsumers.put(topicName, listeners);
 		if (broadcast)
 		{
 			instance.broadCastNewTopicConsumer(topicName);
 			instance.broadCastableTopics.add(topicName);
 		}
 	}
 
 	public static Set<String> getBroadcastableTopics()
 	{
 		return Collections.unmodifiableSet(instance.broadCastableTopics);
 	}
 
 	public static void notify(Message message)
 	{
 		if (instance.localTopicConsumers.size() > 0)
 		{
 			String topicName = message.getDestination();
 			Set<String> subscriptionNames = instance.localTopicConsumers.keySet();
 
 			Set<String> matches = new HashSet<String>();
 			for (String sname : subscriptionNames)
 			{
 				if (sname.equals(topicName))
 				{
 					matches.add(topicName);
 				}
 				else
 				{
 					if (TopicMatcher.match(sname, topicName))
 						matches.add(sname);
 				}
 			}
 
 			for (String destination : matches)
 			{
 				message.setDestination(destination);
 				instance.doNotify(message);
 			}
 		}
 	}
 
 	public void doNotify(Message message)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = localTopicConsumers.get(message.getDestination());
 		if (listeners != null)
 		{
 			for (MessageListener messageListener : listeners)
 			{
 				if (messageListener != null)
 				{
 					messageListener.onMessage(message);
 				}
 			}
 		}
 		else
 		{
 			log.debug("There are no local listeners for topic: '{}'", message.getDestination());
 		}
 	}
 
 	public static void remove(MessageListener listener)
 	{
 		if (listener != null)
 		{
 			Set<String> keys = instance.localTopicConsumers.keySet();
 			for (String topicName : keys)
 			{
 				CopyOnWriteArrayList<MessageListener> listeners = instance.localTopicConsumers.get(topicName);
 				if (listeners != null)
 				{
 					listeners.remove(listener);
 				}
 				instance.localTopicConsumers.remove(listeners);
 				instance.broadCastRemovedTopicConsumer(topicName);
 			}
 		}
 	}
 
 	private Map<String, CopyOnWriteArrayList<MessageListener>> localTopicConsumers = new ConcurrentHashMap<String, CopyOnWriteArrayList<MessageListener>>();
 
 	private Set<String> broadCastableTopics = new CopyOnWriteArraySet<String>();
 
 	private LocalTopicConsumers()
 	{
 	}
 
 	private void broadCastActionTopicConsumer(String destinationName, String action)
 	{
 		Set<IoSession> sessions = Gcs.getManagedConnectorSessions();
 
 		for (IoSession ioSession : sessions)
 		{
 			broadCastTopicInfo(destinationName, action, ioSession);
 		}
 	}
 
 	public static void broadCastTopicInfo(String destinationName, String action, IoSession ioSession)
 	{
		if (StringUtils.isBlank(destinationName))
		{
			return;
		}
		
 		if (action.equals("CREATE"))
 		{
 			log.info("Tell '{}' about new topic consumer for: '{}'", ioSession.getRemoteAddress().toString(), destinationName);
 		}
 		else if (action.equals("DELETE"))
 		{
 			log.info("Tell '{}' about deleted topic consumer of: '{}'", ioSession.getRemoteAddress().toString(), destinationName);
 		}
 
 		Message m = new Message();
 		m.setType((MessageType.SYSTEM_TOPIC));
 		String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><destination>%s</destination></sysmessage>";
 		String payload = String.format(ptemplate, action, AgentInfo.getAgentName(), ioSession.getLocalAddress().toString(), destinationName);
 		m.setDestination(destinationName);
 		m.setContent(payload);
 		WriteFuture wf = ioSession.write(m);
 		wf.awaitUninterruptibly();
 	}
 
 	private void broadCastNewTopicConsumer(String topicName)
 	{
 		broadCastActionTopicConsumer(topicName, "CREATE");
 	}
 
 	private void broadCastRemovedTopicConsumer(String topicName)
 	{
 		broadCastActionTopicConsumer(topicName, "DELETE");
 	}
 
 	public static int size()
 	{
 		return instance.localTopicConsumers.size();
 	}
 
 	public static int size(String destinationName)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localTopicConsumers.get(destinationName);
 		if (listeners != null)
 		{
 			return listeners.size();
 		}
 		return 0;
 	}
 }
