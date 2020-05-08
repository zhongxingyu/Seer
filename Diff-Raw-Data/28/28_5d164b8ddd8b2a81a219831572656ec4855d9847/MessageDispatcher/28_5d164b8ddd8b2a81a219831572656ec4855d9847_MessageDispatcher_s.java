 package de.fettlaus.thekraken.model;
 
 import java.util.SortedSet;
 import de.fettlaus.thekraken.events.EventBus;
 
 public class MessageDispatcher implements Runnable {
 
 	private final SortedSet<Message> messages;
 	EventBus evt;
 	
 	public MessageDispatcher(SortedSet<Message> messages2) {
 		this.messages = messages2;
 		evt = EventBus.instance();
 	}
 
 	@Override
 	public void run() {
 		Message msg;
		Message msg1 = new KrakenMessage(MessageType.PING, 23, "");
		Message msg2 = new KrakenMessage(MessageType.PING, 23, "");
		System.out.println(msg1.compareTo(msg2));
 		while (true) {
 				if(messages.size()>50){
 					msg = messages.first();
 					messages.remove(msg);
 					evt.post(msg);
 				}else{
 					try {
 						Thread.sleep(1);
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 				}
 			
 		}
 
 	}
 
 }
