 package pt.com.gcs.messaging;
 
 class TopicToQueueDispatcher implements MessageListener
 {
 
 	private final String _queueName;
 
	public TopicToQueueDispatcher(String queueName)
 	{
 		_queueName = queueName;
 	}
 
 	@Override
 	public DestinationType getDestinationType()
 	{
 		return DestinationType.TOPIC;
 	}
 
 	public boolean onMessage(Message message)
 	{
 		message.setDestination(_queueName);
 		Gcs.enqueue(message);
 		return true;
 	}
 
 	public String getDestinationName()
 	{
		return _queueName;
 	}
 }
