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
 
 				String topicName = StringUtils.substringAfter(queueName, "@");
				TopicToQueueDispatcher qp = new TopicToQueueDispatcher(topicName, queueName);
 
 				LocalTopicConsumers.add(topicName, qp, false);
 				VirtualQueueStorage.saveVirtualQueue(queueName);
 				return qp;
 			}
 			catch (Throwable e)
 			{
 				throw new RuntimeException(e);
 			}
 		}
 	};
 
 	protected synchronized static void create(String queueName)
 	{
 		log.info("Get Dispatcher for: {}", queueName);
 
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
 
 	protected synchronized static void removeDispatcher(String queueName)
 	{
 		try
 		{
 			if (_dCache.containsKey(queueName))
 			{
 				TopicToQueueDispatcher listener = _dCache.get(queueName, qp_cf);
 				Gcs.removeAsyncConsumer(listener);
 				_dCache.remove(queueName);
 				VirtualQueueStorage.deleteVirtualQueue(queueName);
 			}
 		}
 		catch (InterruptedException ie)
 		{
 			Thread.currentThread().interrupt();
 			throw new RuntimeException(ie);
 		}
 	}
 
 	protected static int size()
 	{
 		return _dCache.size();
 	}
 }
