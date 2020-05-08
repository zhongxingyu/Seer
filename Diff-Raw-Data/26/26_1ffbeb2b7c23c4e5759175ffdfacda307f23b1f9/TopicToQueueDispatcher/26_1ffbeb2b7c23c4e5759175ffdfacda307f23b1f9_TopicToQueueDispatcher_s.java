 package pt.com.gcs.messaging;
 
 class TopicToQueueDispatcher implements MessageListener
 {
 
 	private final String _queueName;
	private final String _topicName;
 
	public TopicToQueueDispatcher(String topicName, String queueName)
 	{
 		_queueName = queueName;
		_topicName = topicName;
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
		message.setDestination(_topicName);
 		return true;
 	}
 
 	public String getDestinationName()
 	{
		return _topicName;
 	}
 }
