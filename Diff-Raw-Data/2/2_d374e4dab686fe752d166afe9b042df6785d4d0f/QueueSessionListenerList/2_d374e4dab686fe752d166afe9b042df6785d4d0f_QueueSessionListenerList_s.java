 package pt.com.broker.messaging;
 
 import java.util.Collection;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.mina.core.session.IoSession;
 import org.caudexorigo.ds.Cache;
 import org.caudexorigo.ds.CacheFiller;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.core.BrokerExecutor;
 import pt.com.gcs.conf.GcsInfo;
 import pt.com.gcs.messaging.Gcs;
 import pt.com.gcs.messaging.Message;
 
 public class QueueSessionListenerList
 {
 	// key: destinationName
 	private static final Cache<String, QueueSessionListener> queueSessionListener = new Cache<String, QueueSessionListener>();
 	private static Logger log = LoggerFactory.getLogger(QueueSessionListenerList.class);
 
 	private QueueSessionListenerList()
 	{
 		Runnable counter = new Runnable()
 		{
 			public void run()
 			{
 				try
 				{
 					Collection<QueueSessionListener> qsl = queueSessionListener.values();
 
 					for (QueueSessionListener qs : qsl)
 					{
 						int ssize = qs.count();
 
 						Message cnt_message = new Message();
						String ctName = String.format("/system/stats/topic-consumer-count/#%s#", qs.getDestinationName());
 						String content = GcsInfo.getAgentName() + "#" + qs.getDestinationName() + "#" + ssize;
 						cnt_message.setDestination(ctName);
 						cnt_message.setContent(content);
 
 						Gcs.publish(cnt_message);
 					}
 				}
 				catch (Throwable t)
 				{
 					log.error(t.getMessage(), t);
 				}
 
 			}
 		};
 
 		BrokerExecutor.scheduleWithFixedDelay(counter, 20, 20, TimeUnit.SECONDS);
 	}
 
 	private static final CacheFiller<String, QueueSessionListener> queue_listeners_cf = new CacheFiller<String, QueueSessionListener>()
 	{
 		public QueueSessionListener populate(String destinationName)
 		{
 			try
 			{
 				QueueSessionListener qsl = new QueueSessionListener(destinationName);
 				Gcs.addAsyncConsumer(destinationName, qsl);
 				return qsl;
 			}
 			catch (Throwable e)
 			{
 				throw new RuntimeException(e);
 			}
 		}
 	};
 
 	public static QueueSessionListener get(String destinationName)
 	{
 		try
 		{
 			return queueSessionListener.get(destinationName, queue_listeners_cf);
 		}
 		catch (InterruptedException ie)
 		{
 			Thread.currentThread().interrupt();
 			throw new RuntimeException(ie);
 		}
 	}
 
 	public static void removeValue(QueueSessionListener value)
 	{
 		try
 		{
 			queueSessionListener.removeValue(value);
 		}
 		catch (InterruptedException ie)
 		{
 			Thread.currentThread().interrupt();
 		}
 	}
 
 	public static void remove(String queueName)
 	{
 		try
 		{
 			queueSessionListener.remove(queueName);
 		}
 		catch (InterruptedException ie)
 		{
 			Thread.currentThread().interrupt();
 		}
 	}
 
 	public static void removeSession(IoSession iosession)
 	{
 		try
 		{
 			Collection<QueueSessionListener> list = queueSessionListener.values();
 			for (QueueSessionListener queueSessionListener : list)
 			{
 				queueSessionListener.removeConsumer(iosession);
 			}
 
 		}
 		catch (InterruptedException ie)
 		{
 			Thread.currentThread().interrupt();
 		}
 	}
 }
