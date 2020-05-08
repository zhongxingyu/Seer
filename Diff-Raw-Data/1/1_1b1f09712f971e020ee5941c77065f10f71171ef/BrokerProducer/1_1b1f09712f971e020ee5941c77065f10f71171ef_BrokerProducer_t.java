 package pt.com.broker.messaging;
 
 import org.caudexorigo.text.DateUtil;
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.gcs.messaging.Gcs;
 import pt.com.gcs.messaging.Message;
 import pt.com.gcs.messaging.MessageType;
 
 public class BrokerProducer
 {
 	private static final Logger log = LoggerFactory.getLogger(BrokerProducer.class);
 
 	private static final BrokerProducer instance = new BrokerProducer();
 
 	public static BrokerProducer getInstance()
 	{
 		return instance;
 	}
 
 	private BrokerProducer()
 	{
 	}
 
 	private Message prepareForSending(BrokerMessage brkMessage, String messageSource)
 	{
 		try
 		{
 			final Message message = new Message();
 
 			if (StringUtils.isNotBlank(brkMessage.messageId))
 				message.setMessageId(brkMessage.messageId);
 
 			if (StringUtils.isNotBlank(brkMessage.correlationId))
 				message.setCorrelationId(brkMessage.correlationId);
 
 			if (StringUtils.isNotBlank(brkMessage.destinationName))
 				message.setDestination(brkMessage.destinationName);
 
 			if (StringUtils.isNotBlank(brkMessage.timestamp))
 			{
 				message.setTimestamp(DateUtil.parseISODate(brkMessage.timestamp).getTime());
 			}
 
 			if (StringUtils.isNotBlank(brkMessage.expiration))
 			{
 				message.setExpiration(DateUtil.parseISODate(brkMessage.expiration).getTime());
 			}
 
 			message.setContent(brkMessage.textPayload);
 			message.setSourceApp(messageSource);
			message.setPriority(brkMessage.priority);
 
 			if (log.isDebugEnabled())
 			{
 				log.debug("Received message: {}", message.getMessageId());
 			}
 
 			return message;
 		}
 		catch (Throwable e)
 		{
 			throw new RuntimeException(e);
 		}
 	}
 
 	public void enqueueMessage(Enqueue enqreq, String messageSource)
 	{
 		BrokerMessage brkm = enqreq.brokerMessage;
 		Message msg = prepareForSending(brkm, messageSource);
 		msg.setType(MessageType.COM_QUEUE);
 
 		Gcs.enqueue(msg);
 	}
 
 	public void publishMessage(final Publish pubreq, final String messageSource)
 	{
 		final BrokerMessage brkm = pubreq.brokerMessage;
 
 		Message msg = prepareForSending(brkm, messageSource);
 		msg.setType(MessageType.COM_TOPIC);
 		Gcs.publish(msg);
 	}
 
 	public void acknowledge(Acknowledge ackReq)
 	{
 		Gcs.ackMessage(ackReq.destinationName, ackReq.messageId);
 	}
 
 }
