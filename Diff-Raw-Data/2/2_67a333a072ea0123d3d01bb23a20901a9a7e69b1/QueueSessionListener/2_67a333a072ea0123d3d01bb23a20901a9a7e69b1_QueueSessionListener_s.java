 package pt.com.broker.messaging;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.mina.core.session.IoSession;
 import org.apache.mina.core.write.DefaultWriteRequest;
 import org.apache.mina.core.write.WriteRequest;
 import org.apache.mina.core.write.WriteTimeoutException;
 import org.caudexorigo.concurrent.Sleep;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.gcs.messaging.Gcs;
 import pt.com.gcs.messaging.InternalMessage;
 import pt.com.gcs.net.IoSessionHelper;
 import pt.com.types.NetMessage;
 import pt.com.types.NetAction.DestinationType;
 
 public class QueueSessionListener extends BrokerListener
 {
 	private final static int MAX_SESSION_BUFFER_SIZE = 2 * 1024 * 1024;
 
 	private int currentQEP = 0;
 
 	private static final Logger log = LoggerFactory.getLogger(QueueSessionListener.class);
 
 	private final List<IoSession> _sessions = new ArrayList<IoSession>();
 
 	private final String _dname;
 
 	private final Object mutex = new Object();
 
 	public QueueSessionListener(String destinationName)
 	{
 		_dname = destinationName;
 	}
 
 	@Override
 	public DestinationType getDestinationType()
 	{
 		return DestinationType.QUEUE;
 	}
 
 	public boolean onMessage(final InternalMessage msg)
 	{
 		if (msg == null)
 			return true;
 
 		final IoSession ioSession = pick();
 
 		try
 		{
 			if (ioSession != null)
 			{
 				if (ioSession.isConnected() && !ioSession.isClosing())
 				{
 					if (ioSession.getScheduledWriteBytes() > MAX_SESSION_BUFFER_SIZE)
 					{
 						return false;
 					}
					final NetMessage response = BrokerListener.buildNotification(msg, pt.com.types.NetAction.DestinationType.QUEUE);
 					ioSession.write(response);
 					return true;
 				}
 			}
 		}
 		catch (Throwable e)
 		{
 			if (e instanceof org.jibx.runtime.JiBXException)
 			{
 				Gcs.ackMessage(_dname, msg.getMessageId());
 				log.warn("Undeliverable message was deleted. Id: '{}'", msg.getMessageId());
 			}
 
 			try
 			{
 				(ioSession.getHandler()).exceptionCaught(ioSession, e);
 			}
 			catch (Throwable t)
 			{
 				log.error(t.getMessage(), t);
 			}
 		}
 
 		return false;
 	}
 
 	private IoSession pick()
 	{
 		synchronized (mutex)
 		{
 			int n = _sessions.size();
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
 				return _sessions.get(currentQEP);
 			}
 			catch (Exception e)
 			{
 				try
 				{
 					currentQEP = 0;
 					return _sessions.get(currentQEP);
 				}
 				catch (Exception e2)
 				{
 					return null;
 				}
 			}
 		}
 	}
 
 	public int addConsumer(IoSession iosession)
 	{
 		synchronized (mutex)
 		{
 			if (!_sessions.contains(iosession))
 			{
 				_sessions.add(iosession);
 				log.info("Create message consumer for queue: " + _dname + ", address: " + IoSessionHelper.getRemoteAddress(iosession));
 			}
 			return _sessions.size();
 		}
 	}
 
 	public int removeSessionConsumer(IoSession iosession)
 	{
 		synchronized (mutex)
 		{
 			if (_sessions.remove(iosession))
 				log.info("Remove message consumer for queue: " + _dname + ", address: " + IoSessionHelper.getRemoteAddress(iosession));
 
 			if (_sessions.isEmpty())
 			{
 				QueueSessionListenerList.remove(_dname);
 				Gcs.removeAsyncConsumer(this);
 			}
 
 			return _sessions.size();
 		}
 	}
 
 	public String getDestinationName()
 	{
 		return _dname;
 	}
 
 	public int count()
 	{
 		synchronized (mutex)
 		{
 			return _sessions.size();
 		}
 	}
 }
