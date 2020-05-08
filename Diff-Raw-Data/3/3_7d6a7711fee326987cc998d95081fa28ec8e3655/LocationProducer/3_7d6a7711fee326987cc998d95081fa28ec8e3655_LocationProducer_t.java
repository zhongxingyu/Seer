 package messaging;
 
 import javax.jms.Topic;
 
 import models.Location;
 import models.User;
 
 import org.apache.activemq.command.ActiveMQQueue;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.jms.core.JmsTemplate;
 import org.springframework.stereotype.Component;
 
 import controllers.forms.CenterForm;
 
 @Component
 public class LocationProducer {
 
 	@Autowired
 	private JmsTemplate template;
 
 	@Autowired
 	private Topic locationTopic;
 
 	public void publishLocation(final Location aLocation) {
 		// it uses messaging.JsonMessageConverter to serialize the object to
 		// Json
 		template.convertAndSend(locationTopic, aLocation);
 	}
 	
 	public void centerLocation(final CenterForm centerForm) {
 		Location loc = new Location(new User(centerForm.userId), centerForm.lat, centerForm.lon, false);
//		template.convertAndSend(new ActiveMQQueue("/queue/"+centerForm.userId),loc);
		template.convertAndSend(new ActiveMQQueue(centerForm.userId),loc);
 	}
 }
