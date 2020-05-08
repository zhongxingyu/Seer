 package pt.com.gcs.messaging;
 
 import java.net.InetSocketAddress;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.CopyOnWriteArraySet;
 
 import org.apache.mina.core.future.WriteFuture;
 import org.apache.mina.core.session.IoSession;
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.gcs.conf.GcsInfo;
 import pt.com.gcs.net.IoSessionHelper;
 
 class LocalTopicConsumers
 {
 	private static Logger log = LoggerFactory.getLogger(LocalTopicConsumers.class);
 
 	private static final LocalTopicConsumers instance = new LocalTopicConsumers();
 
 	protected synchronized static void add(String subscriptionName, MessageListener listener, boolean broadcast)
 	{
 
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localTopicConsumers.get(subscriptionName);
 		if (listeners == null)
 		{
 			listeners = new CopyOnWriteArrayList<MessageListener>();
 		}
 		listeners.add(listener);
 		instance.localTopicConsumers.put(subscriptionName, listeners);
 		if (broadcast)
 		{
 			instance.broadCastNewTopicConsumer(subscriptionName);
 			instance.broadCastableTopics.add(subscriptionName);
 		}
 	}
 
 	protected static Set<String> getBroadcastableTopics()
 	{
 		return Collections.unmodifiableSet(instance.broadCastableTopics);
 	}
 
 	protected static void notify(Message message)
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
 
 			for (String subscriptionName : matches)
 			{
 				// message.setDestination(destination);
 				instance.doNotify(subscriptionName, message);
 			}
 		}
 	}
 
 	private void doNotify(String subscriptionName, Message message)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = localTopicConsumers.get(subscriptionName);
		String topicName = message.getDestination();
 		if (listeners != null)
 		{
 			for (MessageListener messageListener : listeners)
 			{
 				if (messageListener != null)
 				{
 					messageListener.onMessage(message);
					message.setDestination(topicName); // -> Set the destination name, queue dispatchers change it.
 				}
 			}
 		}
 		else
 		{
 			log.info("There are no local listeners for topic: '{}'", message.getDestination());
 		}
 	}
 
 	protected synchronized static void remove(MessageListener listener)
 	{
 		if (listener != null)
 		{
 			CopyOnWriteArrayList<MessageListener> listeners = instance.localTopicConsumers.get(listener.getDestinationName());
 			if (listeners != null)
 			{
 				listeners.remove(listener);
 				instance.localTopicConsumers.remove(listeners);
 				if (listeners.size() == 0)
 				{
 					instance.broadCastableTopics.remove(listener.getDestinationName());
 				}
 			}
 			
 			instance.broadCastRemovedTopicConsumer(listener.getDestinationName());
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
 			try
 			{
 				broadCastTopicInfo(destinationName, action, ioSession);
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
 
 	protected static void broadCastTopicInfo(String destinationName, String action, IoSession ioSession)
 	{
 		if (StringUtils.isBlank(destinationName))
 		{
 			return;
 		}
 
 		if (action.equals("CREATE"))
 		{
 			log.info("Tell '{}' about new topic consumer for: '{}'", IoSessionHelper.getRemoteAddress(ioSession), destinationName);
 		}
 		else if (action.equals("DELETE"))
 		{
 			log.info("Tell '{}' about deleted topic consumer of: '{}'", IoSessionHelper.getRemoteAddress(ioSession), destinationName);
 		}
 
 		Message m = new Message();
 		m.setType((MessageType.SYSTEM_TOPIC));
 		String ptemplate = "<sysmessage><action>%s</action><source-name>%s</source-name><source-ip>%s</source-ip><destination>%s</destination></sysmessage>";
 		String payload = String.format(ptemplate, action, GcsInfo.getAgentName(), ((InetSocketAddress) IoSessionHelper.getRemoteInetAddress(ioSession)).getHostName(), destinationName);
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
 
 	protected static int size()
 	{
 		return instance.localTopicConsumers.size();
 	}
 
 	protected static int size(String destinationName)
 	{
 		CopyOnWriteArrayList<MessageListener> listeners = instance.localTopicConsumers.get(destinationName);
 		if (listeners != null)
 		{
 			return listeners.size();
 		}
 		return 0;
 	}
 }
