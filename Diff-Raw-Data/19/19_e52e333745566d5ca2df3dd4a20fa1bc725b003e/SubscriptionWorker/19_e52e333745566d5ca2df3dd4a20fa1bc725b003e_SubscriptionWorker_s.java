 package edu.rit.asksg.dataio;
 
 import edu.rit.asksg.common.Log;
 import edu.rit.asksg.domain.Conversation;
 import edu.rit.asksg.domain.Service;
 import edu.rit.asksg.domain.SocialSubscription;
 import edu.rit.asksg.repository.SocialSubscriptionRepository;
 import edu.rit.asksg.service.ConversationService;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Async;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Collection;
 import java.util.List;
 
 /**
  */
 public class SubscriptionWorker implements AsyncWorker {
 
 	@Log
 	Logger log;
 
 	@Autowired
 	ConversationService conversationService;
 
 	@Async
 	public void work(final Service service) {
 		final String threadName = Thread.currentThread().getName();
 		log.debug("Subscription worker executing on " + threadName + " for service " + service.toString());
 
 		if (service instanceof SubscriptionProvider) {
 			for (SocialSubscription socialSubscription : service.getConfig().getSubscriptions()) {
 				final Collection<Conversation> conversationList =
 						((SubscriptionProvider) service).getContentFor(socialSubscription);
 
 				try {
 					conversationService.saveConversations(conversationList);
 				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
 				}
 			}
 		}
 		log.debug("Subscription worker on " + threadName + " for service " + service.toString() + " completed.");
 
 
 	}
 
 
 }
