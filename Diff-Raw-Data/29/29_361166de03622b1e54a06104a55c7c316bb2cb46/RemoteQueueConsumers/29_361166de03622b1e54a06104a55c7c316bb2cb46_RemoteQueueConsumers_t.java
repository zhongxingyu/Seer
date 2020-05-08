 package pt.com.gcs.messaging;
 
 import java.util.Map;
 import java.util.Set;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.CopyOnWriteArrayList;
 
 import org.apache.mina.core.session.IoSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.gcs.conf.GcsInfo;
 import pt.com.gcs.net.IoSessionHelper;
 
 /**
  * RemoteQueueConsumers maintains current remote queue consumers (other agents).
  * 
  */
 class RemoteQueueConsumers
 {
 	private static final int WRITE_BUFFER_SIZE = 128 * 1024;
 
 	private final static double MAX_SUSPENSION_TIME = 1000;
 
 	private final static double LOW_WATER_MARK = (double) WRITE_BUFFER_SIZE;
 	private final static double HIGH_WATER_MARK = LOW_WATER_MARK * 2;
 
 	private final static double DELTA = HIGH_WATER_MARK - LOW_WATER_MARK;
 
 	private static class SessionInfo
 	{
 		IoSession session;
 		long time;
 
 		SessionInfo(IoSession session)
 		{
 			this.session = session;
 			time = 0;
 		}
 
 		boolean isReady()
 		{
 			return time < System.currentTimeMillis();
 		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + ((session == null) ? 0 : session.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			SessionInfo other = (SessionInfo) obj;
			if (session == null)
			{
				if (other.session != null)
					return false;
			}
			else if (!session.equals(other.session))
				return false;
			return true;
		}
 	}
 
 	private static final RemoteQueueConsumers instance = new RemoteQueueConsumers();
 
 	private static Logger log = LoggerFactory.getLogger(RemoteQueueConsumers.class);
 
 	protected synchronized static void add(String queueName, IoSession iosession)
 	{
 		CopyOnWriteArrayList<SessionInfo> sessions = instance.remoteQueueConsumers.get(queueName);
 		if (sessions == null)
 		{
 			sessions = new CopyOnWriteArrayList<SessionInfo>();
 		}
 
 		if (!sessions.contains(iosession))
 		{
 			sessions.add(new SessionInfo(iosession));
 			log.info("Add remote queue consumer for '{}'", queueName);
 		}
 		else
 		{
 			log.info("Remote topic consumer '{}' and session '{}' already exists", queueName, IoSessionHelper.getRemoteAddress(iosession));
 		}
 
 		instance.remoteQueueConsumers.put(queueName, sessions);
 	}
 
 	protected synchronized static void delete(String queueName)
 	{
 		instance.remoteQueueConsumers.remove(queueName);
 	}
 
 	protected static long notify(InternalMessage message)
 	{
 		return instance.doNotify(message);
 	}
 
 	protected synchronized static void remove(IoSession iosession)
 	{
 		Set<String> keys = instance.remoteQueueConsumers.keySet();
 		for (String queueName : keys)
 		{
 			CopyOnWriteArrayList<SessionInfo> sessions = instance.remoteQueueConsumers.get(queueName);
 			if (sessions != null)
 			{
 				if( sessions.remove(new SessionInfo(iosession)) )
 				{
 					log.info("Remove remote queue consumer for '{}' and session '{}'", queueName, IoSessionHelper.getRemoteAddress(iosession));
 				}
 			}
 			instance.remoteQueueConsumers.put(queueName, sessions);
 		}
 	}
 
 	protected synchronized static void remove(String queueName, IoSession iosession)
 	{
 		CopyOnWriteArrayList<SessionInfo> sessions = instance.remoteQueueConsumers.get(queueName);
 		if (sessions != null)
 		{
 			if(sessions.remove(new SessionInfo(iosession)))
 			{
 				log.info("Remove remote queue consumer for '{}' and session '{}'", queueName, IoSessionHelper.getRemoteAddress(iosession));
 			}
 		}
 		instance.remoteQueueConsumers.put(queueName, sessions);
 	}
 
 	protected synchronized static int size(String destinationName)
 	{
 		CopyOnWriteArrayList<SessionInfo> sessions = instance.remoteQueueConsumers.get(destinationName);
 		if (sessions != null)
 		{
 			return sessions.size();
 		}
 		return 0;
 	}
 
 	private Map<String, CopyOnWriteArrayList<SessionInfo>> remoteQueueConsumers = new ConcurrentHashMap<String, CopyOnWriteArrayList<SessionInfo>>();
 
 	private int currentQEP = 0;
 
 	private Object rr_mutex = new Object();
 
 	private RemoteQueueConsumers()
 	{
 	}
 
 	protected long doNotify(InternalMessage message)
 	{
 		CopyOnWriteArrayList<SessionInfo> sessions = remoteQueueConsumers.get(message.getDestination());
 		if (sessions != null)
 		{
 			int n = sessions.size();
 
 			if (n > 0)
 			{
 				SessionInfo sessionInfo = pick(sessions);
 				if (sessionInfo != null)
 				{
 					try
 					{
 
 						if ((sessionInfo != null) && (sessionInfo.session != null))
 						{
 							IoSession ioSession = sessionInfo.session;
 							if (ioSession.isConnected() && !ioSession.isClosing())
 							{
 								long scheduledWriteBytes = ioSession.getScheduledWriteBytes();
 
 								if ((scheduledWriteBytes > LOW_WATER_MARK) && (scheduledWriteBytes < HIGH_WATER_MARK))
 								{
 									long time = (long) ((scheduledWriteBytes - LOW_WATER_MARK) * (MAX_SUSPENSION_TIME / DELTA));
 									sessionInfo.time = System.currentTimeMillis() + time;
 								}
 								else if (scheduledWriteBytes >= HIGH_WATER_MARK)
 								{
 									sessionInfo.time = (long) (System.currentTimeMillis() + MAX_SUSPENSION_TIME);
 									if (log.isDebugEnabled())
 									{
 										log.debug("MAX_SESSION_BUFFER_SIZE reached in session '{}'", ioSession.toString());
 									}
 
 									String log_msg = String.format("Write Queue is full, delay message. MessageId: '%s', Destination: '%s', Target Agent: '%s'", message.getMessageId(), message.getDestination(), sessionInfo.session.getRemoteAddress().toString());
 									log.warn(log_msg);
 
 									String dname = String.format("/system/warn/write-queue/#%s#", GcsInfo.getAgentName());
 									String info_msg = String.format("%s#%s#%s", message.getMessageId(), message.getDestination(), sessionInfo.session.getRemoteAddress().toString());
 									InternalPublisher.send(dname, info_msg);
 									
 									return -1;
 								}
 
 								ioSession.write(message);
 
 								return 2 * 60 * 1000; // reserve for 2mn
 							}
 						}
 					}
 					catch (Throwable ct)
 					{
 						log.error(ct.getMessage(), ct);
 						try
 						{
 							sessionInfo.session.close();
 						}
 						catch (Throwable ict)
 						{
 							log.error(ict.getMessage(), ict);
 						}
 					}
 				}
 			}
 		}
 
 		if (log.isDebugEnabled())
 		{
 			log.debug("There are no remote consumers for queue: {}", message.getDestination());
 		}
 
 		return -1;
 	}
 
 	private SessionInfo pick(CopyOnWriteArrayList<SessionInfo> sessions)
 	{
 		synchronized (rr_mutex)
 		{
 			int n = sessions.size();
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
 				for (int i = 0; i != n; ++i)
 				{
 					SessionInfo sessionInfo = sessions.get(currentQEP);
 					if (sessionInfo.isReady())
 					{
 						return sessionInfo;
 					}
 				}
 			}
 			catch (Throwable t)
 			{
 				try
 				{
 					currentQEP = 0;
 					do
 					{
 						SessionInfo sessionInfo = sessions.get(currentQEP);
 						if (sessionInfo.isReady())
 						{
 							return sessionInfo;
 						}
 					}
 					while ((++currentQEP) != (n - 1));
 				}
 				catch (Throwable t2)
 				{
 					return null;
 				}
 
 			}
 		}
 		return null;
 	}
 }
