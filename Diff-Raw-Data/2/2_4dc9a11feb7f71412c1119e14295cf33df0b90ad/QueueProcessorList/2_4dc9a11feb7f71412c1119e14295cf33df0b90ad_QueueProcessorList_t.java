 package pt.com.gcs.messaging;
 
 import java.util.Collection;
 
 import javax.swing.text.html.HTMLDocument.HTMLReader.IsindexAction;
 
 import org.apache.thrift.server.TThreadPoolServer;
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.ds.Cache;
 import org.caudexorigo.ds.CacheFiller;
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.gcs.conf.GcsInfo;
 
 /**
  * QueueProcessorList contains references for all active QueueProcessor objects.
  * 
  */
 
 public class QueueProcessorList
 {
 
 	public static class MaximumQueuesAllowedReachedException extends Exception
 	{
 	}
 
 	private static final QueueProcessorList instance = new QueueProcessorList();
 
 	private static final Logger log = LoggerFactory.getLogger(QueueProcessorList.class);
 
 	private static final CacheFiller<String, QueueProcessor> qp_cf = new CacheFiller<String, QueueProcessor>()
 	{
 		public QueueProcessor populate(String destinationName)
 		{
 			try
 			{
 				if (size() > GcsInfo.getMaxQueues())
 				{
 					throw new MaximumQueuesAllowedReachedException();
 				}
 				log.debug("Populate QueueProcessorList");
 				QueueProcessor qp = new QueueProcessor(destinationName);
 				return qp;
 			}
 			catch (Throwable e)
 			{
 				throw new RuntimeException(e);
 			}
 		}
 	};
 
 	public static QueueProcessor get(String destinationName) throws MaximumQueuesAllowedReachedException
 	{
 		return instance.i_get(destinationName);
 	}
 
 	protected static void remove(String queueName)
 	{
 		instance.i_remove(queueName);
 	}
 
 	protected static void removeValue(QueueProcessor value)
 	{
 		instance.i_removeValue(value);
 	}
 
 	protected static int size()
 	{
 		return instance.i_size();
 	}
 
 	protected static Collection<QueueProcessor> values()
 	{
 		return instance.i_values();
 	}
 
 	// key: destinationName
 	private Cache<String, QueueProcessor> qpCache = new Cache<String, QueueProcessor>();
 
 	private QueueProcessorList()
 	{
 	}
 
 	private QueueProcessor i_get(String destinationName) throws MaximumQueuesAllowedReachedException
 	{
 		log.debug("Get Queue for: {}", destinationName);
 
 		try
 		{
 			return qpCache.get(destinationName, qp_cf);
 		}
 		catch (InterruptedException ie)
 		{
 			Thread.currentThread().interrupt();
 			throw new RuntimeException(ie);
 		}
 		catch (RuntimeException re)
 		{
 			Throwable rootCause = ErrorAnalyser.findRootCause(re);
 			ErrorAnalyser.exitIfOOM(rootCause);
 			if (rootCause instanceof MaximumQueuesAllowedReachedException)
 			{
 				throw (MaximumQueuesAllowedReachedException) rootCause;
 			}
 		}
 		return null;
 	}
 
 	private synchronized void i_remove(String queueName)
 	{
 		try
 		{
 
 			if (!qpCache.containsKey(queueName))
 			{
				throw new IllegalArgumentException(String.format("Queue named '%s' doesn't exist.", queueName));
 			}
 
 
 
 			QueueProcessor qp;
 			try
 			{
 				qp = get(queueName);
 			}
 			catch (MaximumQueuesAllowedReachedException e)
 			{
 				// This should never happens
 				log.error("Trying to remove an inexistent queue.");
 				return;
 			}
 
 			if (qp.hasRecipient())
 			{
 				String m = String.format("Queue '%s' has active consumers.", queueName);
 				throw new IllegalStateException(m);
 			}
 			
 			if (StringUtils.contains(queueName, "@"))
 			{
 				DispatcherList.removeDispatcher(queueName);
 			}
 
 			LocalQueueConsumers.delete(queueName);
 			RemoteQueueConsumers.delete(queueName);
 			qp.clearStorage();
 
 			qpCache.remove(queueName);
 
 			log.info("Destination '{}' was deleted", queueName);
 
 		}
 		catch (InterruptedException ie)
 		{
 			Thread.currentThread().interrupt();
 			throw new RuntimeException(ie);
 		}
 	}
 
 	private void i_removeValue(QueueProcessor value)
 	{
 		try
 		{
 			qpCache.removeValue(value);
 		}
 		catch (InterruptedException ie)
 		{
 			Thread.currentThread().interrupt();
 			throw new RuntimeException(ie);
 		}
 	}
 
 	private int i_size()
 	{
 		return qpCache.size();
 	}
 
 	private Collection<QueueProcessor> i_values()
 	{
 		try
 		{
 			return qpCache.values();
 		}
 		catch (InterruptedException ie)
 		{
 			Thread.currentThread().interrupt();
 			throw new RuntimeException(ie);
 		}
 	}
 }
