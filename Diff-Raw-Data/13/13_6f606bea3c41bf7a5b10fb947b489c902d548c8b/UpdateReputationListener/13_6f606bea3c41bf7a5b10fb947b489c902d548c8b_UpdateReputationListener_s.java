 package org.xwiki.contrib.ratings.internal;
 
 import java.util.Arrays;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Provider;
 import javax.inject.Singleton;
 
 import org.xwiki.bridge.event.WikiReadyEvent;
 import org.xwiki.component.annotation.Component;
 import org.xwiki.contrib.ratings.ReputationAlgorithm;
 import org.xwiki.contrib.ratings.UpdateRatingEvent;
 import org.xwiki.observation.EventListener;
 import org.xwiki.observation.event.Event;
 
 
@Component
 @Singleton
 public class UpdateReputationListener implements EventListener
 {
     @Inject
     Provider<ReputationAlgorithm> reputationAlgorithm;
 
     @Override
     public List<Event> getEvents()
     {
         return Arrays.<Event>asList(new UpdateRatingEvent());
     }
 
     @Override
     public String getName()
     {
         return "updatereputation";
     }
 
     @Override
     public void onEvent(Event event, Object arg1, Object arg2)
     {
        UpdateRatingEvent ratingEvent = (UpdateRatingEvent) event;
        reputationAlgorithm.get().updateReputation(ratingEvent.getDocumentName(), ratingEvent.getNewRating(), ratingEvent.getOldRating());
     }
   
 }
