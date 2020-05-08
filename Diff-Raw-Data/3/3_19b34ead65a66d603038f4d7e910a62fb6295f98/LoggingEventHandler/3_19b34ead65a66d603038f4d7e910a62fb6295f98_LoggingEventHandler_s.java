 package edu.nyu.grouper.osgi;
 
 import org.apache.felix.scr.annotations.Component;
 import org.apache.felix.scr.annotations.Properties;
 import org.apache.felix.scr.annotations.Property;
 import org.apache.felix.scr.annotations.Service;
 import org.osgi.service.event.Event;
 import org.osgi.service.event.EventConstants;
 import org.osgi.service.event.EventHandler;
 import org.sakaiproject.nakamura.api.lite.util.Iterables;
 import org.sakaiproject.nakamura.lite.OSGiStoreListener;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /** 
  * Sanity-check class.
  * @author froese
  * @see OSGiStoreListener for the list of topics
  */
 @Service(value = EventHandler.class)
 @Component(metatype=true)
 @Properties(value = { 
 		@Property(name = EventConstants.EVENT_TOPIC, 
 				value = {
 				"org/sakaiproject/nakamura/lite/authorizables/ADDED",
 				"org/sakaiproject/nakamura/lite/authorizables/DELETE",
 				"org/sakaiproject/nakamura/lite/group/ADDED",
 				"org/sakaiproject/nakamura/lite/group/DELETE",
				"org/sakaiproject/nakamura/lite/group/UPDATED",
		})
 })
 public class LoggingEventHandler implements EventHandler {
 
 	private static final Logger log = LoggerFactory.getLogger(LoggingEventHandler.class);
 
 	public void handleEvent(Event event) {
 		logEvent(event);
 	}
 
 	/**
 	 * Print the event to the log.
 	 * @param event
 	 */
 	private void logEvent(Event event) {
 		if (log.isInfoEnabled()){
 			StringBuffer buffer = new StringBuffer();
 			for(String name: Iterables.of(event.getPropertyNames())){
 				buffer.append("\n" + name + "=" + event.getProperty(name));
 			}
 			log.info("\ntopic : " + event.getTopic() + " properties: " + buffer.toString());
 		}
 	}
 }
