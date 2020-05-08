 package pt.com.gcs.messaging;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.mina.core.session.IoSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * SystemMessagesPublisher is responsible for holding and delivering system messages such as SYSTEM_TOPIC and SYSTEM_QUEUE. If these messages are not acknowledged them are resent.
  * 
  */
 
 public class SystemMessagesPublisher
 {
 	private static class TimeoutMessage
 	{
 		final InternalMessage message;
 		long timeout;
 		final IoSession session;
 
 		TimeoutMessage(InternalMessage message, IoSession session, long timeout)
 		{
 			this.message = message;
 			this.session = session;
 			this.timeout = timeout;
 		}
 	}
 
 	private static Logger log = LoggerFactory.getLogger(SystemMessagesPublisher.class);
 
 	private static final long ACKNOWLEDGE_INTERVAL = 1 * 60 * 1000;
 
 	static
 	{
 		Runnable command = new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				try
 				{
 					long currentTime = System.currentTimeMillis();
 					List<TimeoutMessage> retryMessages = new ArrayList<TimeoutMessage>();
 
 					synchronized (pending_messages)
 					{
 						for (Map.Entry<String, TimeoutMessage> entry : pending_messages.entrySet())
 						{
 							if (entry.getValue().timeout <= currentTime)
 							{
 								retryMessages.add(entry.getValue());
								log.warn("System message with message id" + entry.getValue().message.getMessageId() + " timed out.");
 								break;
 							}
 						}
 
 						for (TimeoutMessage tm : retryMessages)
 						{
 							tm.session.write(tm.message);
 							tm.timeout = System.currentTimeMillis() + ACKNOWLEDGE_INTERVAL;
 						}
 					}
 
 				}
 				catch (Throwable t)
 				{
 					log.error("Timeout verification runnable", t);
 				}
 			}
 
 		};
 
 		GcsExecutor.scheduleAtFixedRate(command, ACKNOWLEDGE_INTERVAL, 500, TimeUnit.MILLISECONDS);
 	}
 
 	private static Map<String, TimeoutMessage> pending_messages = new HashMap<String, TimeoutMessage>();
 
 	public static void sendMessage(InternalMessage message, IoSession session)
 	{
 		TimeoutMessage tm = new TimeoutMessage(message, session, System.currentTimeMillis() + ACKNOWLEDGE_INTERVAL);
 
 		synchronized (pending_messages)
 		{
 			pending_messages.put(message.getMessageId(), tm);
 		}
 		session.write(message);
 	}
 
 	public static void sessionClosed(IoSession session)
 	{
 		List<String> message_identifiers = new ArrayList<String>();
 
 		synchronized (pending_messages)
 		{
 			for (Map.Entry<String, TimeoutMessage> entry : pending_messages.entrySet())
 			{
 				if (entry.getValue().session.equals(session))
 					message_identifiers.add(entry.getValue().message.getMessageId());
 			}
 			for (String msgId : message_identifiers)
 				pending_messages.remove(msgId);
 		}
 	}
 
 	public static void messageAcknowledged(String messageId)
 	{
 		synchronized (pending_messages)
 		{
 			pending_messages.remove(messageId);
 		}
 	}
 
 }
