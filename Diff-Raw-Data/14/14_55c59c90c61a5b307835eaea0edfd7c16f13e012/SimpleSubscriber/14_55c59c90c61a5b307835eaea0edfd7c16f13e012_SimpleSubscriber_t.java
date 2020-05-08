 package de.guderlei.pubsub.subscriber;
 
import org.osgi.service.log.LogService;

 import de.guderlei.pubsub.model.Message;
 import de.guderlei.pubsub.model.Subscriber;
 
 public class SimpleSubscriber implements Subscriber {
	
	private LogService log;
	
	public void setLog(LogService log){
		if(log != null){
			this.log = log;
		}
	}
 
 	@Override
 	public void receive(Message message) {
		log.log(LogService.LOG_INFO, "SCR Subscriber: " + message.toString());
 	}
 
 }
