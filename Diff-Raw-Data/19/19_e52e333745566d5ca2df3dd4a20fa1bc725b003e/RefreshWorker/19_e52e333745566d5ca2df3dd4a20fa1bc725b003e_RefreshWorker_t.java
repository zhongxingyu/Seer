 package edu.rit.asksg.dataio;
 
 import edu.rit.asksg.common.Log;
 import edu.rit.asksg.domain.Service;
 import edu.rit.asksg.service.ConversationService;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowire;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Async;
 import org.springframework.stereotype.Component;
 
 /**
  */
 public class RefreshWorker implements AsyncWorker {
 
 	@Log
 	Logger log;
 
 	@Autowired
 	ConversationService conversationService;
 
 	/**
 	 * Pull new content for each service in an async context
 	 *
 	 * @param service
 	 */
 	@Async
 	public void work(final Service service) {
 		final String threadName = Thread.currentThread().getName();
 		log.debug("Pull worker executing on " + threadName + " for service " + service.toString());
 
 		try {
 			conversationService.saveConversations(service.getNewContent());
 		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
 		}
 
 		log.debug("Pull worker on " + threadName + " for service " + service.toString() + " completed.");
 	}
 
 
 }
