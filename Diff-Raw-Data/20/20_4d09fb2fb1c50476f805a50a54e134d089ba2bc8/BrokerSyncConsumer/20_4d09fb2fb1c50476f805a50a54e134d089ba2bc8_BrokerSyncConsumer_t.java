 package pt.com.broker.messaging;
 
 import org.apache.mina.core.session.IoSession;
 
 import pt.com.broker.types.NetPoll;
 import pt.com.gcs.messaging.LocalQueueConsumers;
import pt.com.gcs.messaging.QueueProcessorList;
 
 /**
  * BrokerSyncConsumer represents a queue synchronous consumer.
  * 
  */
 public class BrokerSyncConsumer
 {
 	//private static final Logger log = LoggerFactory.getLogger(BrokerSyncConsumer.class);
 
 	public static void poll(NetPoll poll, IoSession ios)
 	{
 
 		try
 		{
			QueueProcessorList.get(poll.getDestination());
			
 			String destination = poll.getDestination();
 			String composedQueueName = SynchronousMessageListener.getComposedQueueName(destination);
 			Object attribute = ios.getAttribute(composedQueueName);
 			SynchronousMessageListener msgListener = null;
 			if (attribute != null)
 			{
 				msgListener = (SynchronousMessageListener) attribute;
 			}
 			else
 			{
 				msgListener = new SynchronousMessageListener(destination, ios);
 				ios.setAttribute(composedQueueName, msgListener);
 				LocalQueueConsumers.add(destination, msgListener);
 			}
 			msgListener.activate(poll.getTimeout(), poll.getActionId());
 		}
 		catch (Throwable t)
 		{
 			try
 			{
 				(ios.getHandler()).exceptionCaught(ios, t);
 			}
 			catch (Throwable t2)
 			{
 				throw new RuntimeException(t2);
 			}
 		}
 	}
 
 }
