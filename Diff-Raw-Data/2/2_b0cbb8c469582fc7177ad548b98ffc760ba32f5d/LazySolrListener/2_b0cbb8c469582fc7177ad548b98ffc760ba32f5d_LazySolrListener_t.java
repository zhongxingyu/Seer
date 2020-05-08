 package pl.psnc.dl.wf4ever.eventbus.lazy.listeners;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import pl.psnc.dl.wf4ever.eventbus.events.ROAfterCreateEvent;
 import pl.psnc.dl.wf4ever.eventbus.events.ROAfterUpdateEvent;
 import pl.psnc.dl.wf4ever.eventbus.events.ROBeforeDeleteEvent;
 import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterCreateEvent;
 import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterDeleteEvent;
 import pl.psnc.dl.wf4ever.eventbus.events.ROComponentAfterUpdateEvent;
 import pl.psnc.dl.wf4ever.preservation.model.ResearchObjectSerializable;
 
 import com.google.common.eventbus.EventBus;
 import com.google.common.eventbus.Subscribe;
 
 /**
  * Listener for ResearchObject and ResearchObjectComponent, performs operation on solr indexs.
  * 
  * @author pejot
  * 
  */
 public class LazySolrListener {
 
     /** A set of ROs to reindex in the commit() method. */
     private Set<ResearchObjectSerializable> researchObjectsToReindex = new HashSet<>();
 
     /** A set of ROs to delete from index in the commit() method. */
     private Set<ResearchObjectSerializable> researchObjectsToDeleteFromIndex = new HashSet<>();
 
 
     /**
      * Constructor.
      * 
      * @param eventBus
      *            EventBus instance
      */
     public LazySolrListener(EventBus eventBus) {
         eventBus.register(this);
     }
 
 
     /**
      * Subscription method.
      * 
      * @param event
      *            processed event
      */
     @Subscribe
     public void onAfterROComponentCreate(ROComponentAfterCreateEvent event) {
         researchObjectsToReindex.add(event.getResearchObjectComponent().getResearchObject());
     }
 
 
     /**
      * Subscription method.
      * 
      * @param event
      *            processed event
      */
     @Subscribe
     public void onAfterROComponentDelete(ROComponentAfterDeleteEvent event) {
         researchObjectsToReindex.add(event.getResearchObjectComponent().getResearchObject());
     }
 
 
     /**
      * Subscription method.
      * 
      * @param event
      *            processed event
      */
     @Subscribe
     public void onAfterROComponentUpdate(ROComponentAfterUpdateEvent event) {
         researchObjectsToReindex.add(event.getResearchObjectComponent().getResearchObject());
     }
 
 
     /**
      * Subscription method.
      * 
      * @param event
      *            processed event
      */
     @Subscribe
     public void onAfterROCreate(ROAfterCreateEvent event) {
         researchObjectsToReindex.add(event.getResearchObject());
     }
 
 
     /**
      * Subscription method.
      * 
      * @param event
      *            processed event
      */
     @Subscribe
     public void onBeforeRODelete(ROBeforeDeleteEvent event) {
         researchObjectsToDeleteFromIndex.add(event.getResearchObject());
     }
 
 
     /**
      * Subscription method.
      * 
      * @param event
      *            processed event
      */
     @Subscribe
     public void onAfterUpdate(ROAfterUpdateEvent event) {
         researchObjectsToReindex.add(event.getResearchObject());
     }
 
 
     /**
      * Reindex all necessary ROs.
      */
     public void commit() {
        researchObjectsToReindex.removeAll(researchObjectsToDeleteFromIndex);
         for (ResearchObjectSerializable ro : researchObjectsToReindex) {
             ro.updateIndexAttributes();
         }
         researchObjectsToReindex.clear();
         for (ResearchObjectSerializable ro : researchObjectsToDeleteFromIndex) {
             ro.deleteIndexAttributes();
         }
         researchObjectsToDeleteFromIndex.clear();
     }
 }
