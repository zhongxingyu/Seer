 package pt.com.broker.messaging;
 
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicBoolean;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelHandlerContext;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.types.ChannelAttributes;
 import pt.com.broker.types.NetFault;
 import pt.com.broker.types.NetMessage;
 import pt.com.broker.types.NetAction.DestinationType;
 import pt.com.gcs.messaging.ForwardResult;
 import pt.com.gcs.messaging.GcsExecutor;
 import pt.com.gcs.messaging.InternalMessage;
 import pt.com.gcs.messaging.LocalQueueConsumers;
 import pt.com.gcs.messaging.MessageListener;
 import pt.com.gcs.messaging.QueueProcessor;
 import pt.com.gcs.messaging.QueueProcessorList;
 import pt.com.gcs.messaging.ForwardResult.Result;
 import pt.com.gcs.messaging.QueueProcessorList.MaximumQueuesAllowedReachedException;
 
 /*
  * SynchronousMessageListener represents a poll request by a client. 
  */
 public class SynchronousMessageListener implements MessageListener
 {
 	private static final Logger log = LoggerFactory.getLogger(SynchronousMessageListener.class);
 
 	private static final String SESSION_ATT_PREFIX = "SYNC_MESSAGE_LISTENER#";
 
 	private static final long RESERVE_TIME = 15 * 60 * 1000; // reserve for 15mn
 	private static final long ACTIVE_INTERVAL = 5 * 60 * 1000; // 5mn
 
 	private AtomicBoolean ready;
 	private final String queueName;
 	private final Channel channel;
 
 	private volatile long expires;
 	private volatile boolean inNoWaitMode;
 	private volatile String actionId;
 
 	private AtomicLong lastDeliveredMessage = new AtomicLong(0);
 
 	public SynchronousMessageListener(String queueName, Channel channel)
 	{
 		this.ready = new AtomicBoolean(false);
 		this.queueName = queueName;
 		this.channel = channel;
 		this.setInNoWaitMode(false);
 	}
 
 	@Override
 	public String getDestinationName()
 	{
 		return queueName;
 	}
 
 	@Override
 	public DestinationType getSourceDestinationType()
 	{
 		return DestinationType.QUEUE;
 	}
 
 	@Override
 	public DestinationType getTargetDestinationType()
 	{
 		return DestinationType.QUEUE;
 	}
 
 	private static final ForwardResult failed = new ForwardResult(Result.FAILED);
 	private static final ForwardResult success = new ForwardResult(Result.SUCCESS, RESERVE_TIME);
 
 	@Override
 	public ForwardResult onMessage(InternalMessage message)
 	{
 		if (!ready.get())
 		{
 			log.error("We shouldn't be here. A SynchronousMessageListener should not be called when in a 'not ready' state.");
 			return failed;
 		}
 
 		ready.set(false);
 
 		if ((channel != null) && channel.isConnected() && channel.isWritable())
 		{
 			final NetMessage response = BrokerListener.buildNotification(message, getDestinationName(), getSourceDestinationType());
 			channel.write(response);
 
 			lastDeliveredMessage.set(System.currentTimeMillis());
 		}
 		else
 		{
 			if ((channel == null) || !channel.isConnected())
 			{
 				LocalQueueConsumers.remove(this);
 			}
 			return failed;
 		}
 
 		return success;
 	}
 
 	@Override
 	public boolean ready()
 	{
 		return ready.get();
 	}
 
 	public void activate(long timeout, String actionId)
 	{
 		this.actionId = actionId;
 		activate(timeout);
 	}
 
 	public void activate(long timeout)
 	{
 		if (timeout == 0)
 		{
 			// wait for ever
 
 			this.setExpires(Long.MAX_VALUE);
 
 			ready.set(true);
 			return;
 		}
 
 		if (timeout < 0)
 		{
 			boolean noMessages = false;
 
 			setInNoWaitMode(true);
 
 			QueueProcessor queueProcessor;
 			try
 			{
 				queueProcessor = QueueProcessorList.get(getDestinationName());
 				if (queueProcessor.getQueuedMessagesCount() == 0)
 				{
 					noMessages = true;
 				}
 			}
 			catch (MaximumQueuesAllowedReachedException e)
 			{
 				noMessages = true;
 			}
 			if (noMessages)
 			{
 				NetMessage faultMsg = NetFault.getMessageFaultWithDetail(NetFault.NoMessageInQueueErrorMessage, getDestinationName());
 				if (actionId != null)
 				{
 					faultMsg.getAction().getFaultMessage().setActionId(actionId);
 				}
 				if ((channel != null) && channel.isConnected() && channel.isWritable())
 				{
 					channel.write(faultMsg);
 				}
 
 				ready.set(false);
 				setInNoWaitMode(false);
 				return;
 			}
 			// There is, at least one message. That is no guarantee that the sync client will receive it, so set a timeout of one second and set mode to no wait (inNoWaitMode)
 
 			timeout = 1000;
 		}
 
 		this.setExpires(System.currentTimeMillis() + timeout);
 		ready.set(true);
 
 		GcsExecutor.schedule(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				notifyTimeout();
 			}
 
 		}, timeout, TimeUnit.MILLISECONDS);
 	}
 
 	public void notifyTimeout()
 	{
 		if ((System.currentTimeMillis() >= getExpires()) && ready.get())
 		{
 
 			ready.set(false);
 
 			NetMessage faultMsg = null;
 
 			if (isInNoWaitMode())
 			{
 				faultMsg = NetFault.getMessageFaultWithDetail(NetFault.NoMessageInQueueErrorMessage, getDestinationName());
 			}
 			else
 			{
 				faultMsg = NetFault.getMessageFaultWithDetail(NetFault.PollTimeoutErrorMessage, getDestinationName());
 			}
 
 			if (actionId != null)
 			{
 				faultMsg.getAction().getFaultMessage().setActionId(actionId);
 			}
 			if ((channel != null) && channel.isConnected() && channel.isWritable())
 			{
 				channel.write(faultMsg);
 			}
 		}
 	}
 
 	public static String getComposedQueueName(String queueName)
 	{
 		return SESSION_ATT_PREFIX + queueName;
 	}
 
 	public static void removeSession(ChannelHandlerContext ctx)
 	{
 		// Set<String> attributeKeys = channel.getAttributeKeys();
 		// Channel channel = ctx.getChannel();
 		Set<String> attributeKeys = ChannelAttributes.getAttributeKeys(ctx);
 		for (String attributeKey : attributeKeys)
 		{
 			if (attributeKey.toString().startsWith(SESSION_ATT_PREFIX))
 			{
 				Object attributeValue = ChannelAttributes.get(ctx, attributeKey);
 				if (attributeValue instanceof SynchronousMessageListener)
 				{
 					SynchronousMessageListener listener = (SynchronousMessageListener) attributeValue;
 					BrokerSyncConsumer.pollStoped(listener.getDestinationName());
 					LocalQueueConsumers.remove(listener);
 				}
 			}
 		}
 	}
 
 	private void setInNoWaitMode(boolean inNoWaitMode)
 	{
 		synchronized (this)
 		{
 			this.inNoWaitMode = inNoWaitMode;
 		}
 	}
 
 	private boolean isInNoWaitMode()
 	{
 		synchronized (this)
 		{
 			return inNoWaitMode;
 		}
 	}
 
 	private void setExpires(long expires)
 	{
 		synchronized (this)
 		{
 			this.expires = expires;
 		}
 	}
 
 	private long getExpires()
 	{
 		synchronized (this)
 		{
 			return expires;
 		}
 	}
 
 	@Override
 	public boolean isActive()
 	{
		if(ready())
			return true;
		
 		return (lastDeliveredMessage.get() + ACTIVE_INTERVAL) >= System.currentTimeMillis();
 	}
 }
