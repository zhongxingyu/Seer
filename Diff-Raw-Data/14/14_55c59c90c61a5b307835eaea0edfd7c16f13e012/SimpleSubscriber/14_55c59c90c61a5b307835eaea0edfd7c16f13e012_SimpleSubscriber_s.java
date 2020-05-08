 package de.guderlei.pubsub.subscriber;
 
 import de.guderlei.pubsub.model.Message;
 import de.guderlei.pubsub.model.Subscriber;
 
 public class SimpleSubscriber implements Subscriber {
 
 	@Override
 	public void receive(Message message) {
		System.out.println(message);
 	}
 
 }
