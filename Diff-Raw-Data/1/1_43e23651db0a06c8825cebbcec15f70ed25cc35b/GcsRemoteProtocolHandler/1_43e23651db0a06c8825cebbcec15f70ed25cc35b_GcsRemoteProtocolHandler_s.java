 package pt.com.gcs.messaging;
 
 import java.net.SocketAddress;
 import java.util.Set;
 
 import org.apache.mina.common.IdleStatus;
 import org.apache.mina.common.IoHandlerAdapter;
 import org.apache.mina.common.IoSession;
 import org.caudexorigo.ErrorAnalyser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.gcs.conf.AgentInfo;
 import pt.com.gcs.net.IoSessionHelper;
 
 class GcsRemoteProtocolHandler extends IoHandlerAdapter
 {
 	private static Logger log = LoggerFactory.getLogger(GcsRemoteProtocolHandler.class);
 
 	@Override
 	public void exceptionCaught(IoSession iosession, Throwable cause) throws Exception
 	{
 		Throwable rootCause = ErrorAnalyser.findRootCause(cause);
 		log.error("Exception Caught:{}, {}", IoSessionHelper.getRemoteAddress(iosession), rootCause.getMessage());
 		if (iosession.isConnected() && !iosession.isClosing())
 		{
 			log.error("STACKTRACE", rootCause);
 		}
 	}
 
 	@Override
 	public void messageReceived(final IoSession iosession, Object omessage) throws Exception
 	{
 		final Message msg = (Message) omessage;
 
 		if (log.isDebugEnabled())
 		{
 			log.debug("Message Received from: '{}', Type: '{}'", IoSessionHelper.getRemoteAddress(iosession), msg.getType());
 		}
 
 		if (msg.getType() == (MessageType.COM_TOPIC))
 		{
 			LocalTopicConsumers.notify(msg);		
 			
 		}
 		else if (msg.getType() == (MessageType.COM_QUEUE))
 		{
 			ReceivedMessagesBuffer receivedMessages = ReceivedMessagesBufferList.get(msg.getDestination());
 
 			if (!receivedMessages.isDuplicate(msg.getMessageId()))
 			{
 				QueueProcessorList.get(msg.getDestination()).store(msg, true);
 				LocalQueueConsumers.acknowledgeMessage(msg, iosession);
 			}
 
 		}
 		else
 		{
 			log.warn("Unkwown message type. Don't know how to handle message");
 		}
 	}
 
 	@Override
 	public void messageSent(IoSession iosession, Object message) throws Exception
 	{
 		if (log.isDebugEnabled())
 		{
 			log.debug("Message Sent: '{}', '{}'", IoSessionHelper.getRemoteAddress(iosession), message.toString());
 		}
 	}
 
 	@Override
 	public void sessionClosed(final IoSession iosession) throws Exception
 	{
 		log.info("Session Closed: '{}'", IoSessionHelper.getRemoteAddress(iosession));
 		Gcs.connect((SocketAddress) IoSessionHelper.getRemoteInetAddress(iosession));
 	}
 
 	@Override
 	public void sessionCreated(IoSession iosession) throws Exception
 	{
 		IoSessionHelper.tagWithRemoteAddress(iosession);
 		if (log.isDebugEnabled())
 		{
 			log.debug("Session Created: '{}'", IoSessionHelper.getRemoteAddress(iosession));
 		}		
 	}
 
 	@Override
 	public void sessionIdle(IoSession iosession, IdleStatus status) throws Exception
 	{
 		if (log.isDebugEnabled())
 		{
 			log.debug("Session Idle:'{}'", IoSessionHelper.getRemoteAddress(iosession));
 		}
 	}
 
 	@Override
 	public void sessionOpened(IoSession iosession) throws Exception
 	{
 		log.info("Session Opened: '{}'", IoSessionHelper.getRemoteAddress(iosession));
 		sayHello(iosession);
 	}
 
 	public void sayHello(IoSession iosession)
 	{
 		if (log.isDebugEnabled())
 		{
 			log.debug("Say Hello: '{}'", IoSessionHelper.getRemoteAddress(iosession));
 		}
 
 		Message m = new Message();
 		String agentId = AgentInfo.getAgentName() + "@" + AgentInfo.getAgentHost() + ":" + AgentInfo.getAgentPort();
 		m.setType((MessageType.HELLO));
 		m.setDestination("HELLO");
 		m.setContent(agentId);
 
 		log.info("Send agentId: '{}'", agentId);
 
 		// iosession.write(m).awaitUninterruptibly();
 		iosession.write(m);
 
 		Set<String> topicNameSet = LocalTopicConsumers.getBroadcastableTopics();
 		for (String topicName : topicNameSet)
 		{
 			LocalTopicConsumers.broadCastTopicInfo(topicName, "CREATE", iosession);
 		}
 
 		Set<String> queueNameSet = LocalQueueConsumers.getBroadcastableQueues();
 		for (String queueName : queueNameSet)
 		{
 			LocalQueueConsumers.broadCastQueueInfo(queueName, "CREATE", iosession);
 		}
 	}
 }
