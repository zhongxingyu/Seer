 package pt.com.gcs.messaging;
 
 import org.caudexorigo.ds.Cache;
 import org.caudexorigo.ds.CacheFiller;
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 
 public class DispatcherList
 {
 
 	private static final Logger log = LoggerFactory.getLogger(DispatcherList.class);
 
 	// key: destinationName
 	private static final Cache<String, TopicToQueueDispatcher> _dCache = new Cache<String, TopicToQueueDispatcher>();
 
 	private DispatcherList()
 	{
 	}
 
 	private static final CacheFiller<String, TopicToQueueDispatcher> qp_cf = new CacheFiller<String, TopicToQueueDispatcher>()
 	{
 		public TopicToQueueDispatcher populate(String queueName)
 		{
 			try
 			{
 				log.debug("Populate DispatcherList");
 
 				TopicToQueueDispatcher qp = new TopicToQueueDispatcher(queueName);
 				String topicName = StringUtils.substringAfter(queueName, "@");
 				LocalTopicConsumers.add(topicName, qp, false);
 				DbStorage.saveVirtualQueue(queueName);
 				return qp;
 			}
 			catch (Throwable e)
 			{
 				throw new RuntimeException(e);
 			}
 		}
 	};
 
 	public static void create(String queueName)
 	{
 		log.debug("Get Dispatcher for: {}", queueName);
 
 		try
 		{
 			_dCache.get(queueName, qp_cf);
 		}
 		catch (InterruptedException ie)
 		{
 			Thread.currentThread().interrupt();
 			throw new RuntimeException(ie);
 		}
 	}
 
 
 	public static void removeDispatcher(String queueName)
 	{
 		try
 		{
 			TopicToQueueDispatcher listener = _dCache.get(queueName, qp_cf);
 			_dCache.remove(queueName);
 			Gcs.removeTopicConsumer(listener);
 			DbStorage.deleteVirtualQueue(queueName);
			QueueProcessorList.remove(queueName);
 		}
 		catch (InterruptedException ie)
 		{
 			Thread.currentThread().interrupt();
 			throw new RuntimeException(ie);
 		}
 	}
 
 	public static int size()
 	{
 		return _dCache.size();
 	}
 }
