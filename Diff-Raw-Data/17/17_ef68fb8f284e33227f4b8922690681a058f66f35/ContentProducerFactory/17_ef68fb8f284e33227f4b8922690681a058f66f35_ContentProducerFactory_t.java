 package uk.ac.ox.oucs.search.solr;
 
 import org.sakaiproject.event.api.Event;
 import org.sakaiproject.search.api.EntityContentProducer;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashSet;
 
 /**
  * @author Colin Hebert
  */
 public class ContentProducerFactory {
     private static final Logger logger = LoggerFactory.getLogger(SolrSearchIndexBuilder.class);
     private final Collection<EntityContentProducer> contentProducers = new HashSet<EntityContentProducer>();
 
     public void addContentProducer(EntityContentProducer contentProducer) {
         logger.info(contentProducer.getClass() + " registered to provide content to the search index from " + contentProducer.getTool());
         contentProducers.add(contentProducer);
     }
 
     public EntityContentProducer getContentProducerForElement(String reference) {
         for (EntityContentProducer contentProducer : contentProducers) {
            try {
                if (contentProducer.matches(reference)) {
                    return contentProducer;
                }
            } catch (Exception e) {
                logger.warn("The content producer '" + contentProducer.getClass() + "' has thrown an exception", e);
             }
         }
         logger.info("Couldn't find a content producer for reference '" + reference + "'");
         return null;
     }
 
     public EntityContentProducer getContentProducerForEvent(Event event) {
         for (EntityContentProducer contentProducer : contentProducers) {
            try {
                if (contentProducer.matches(event)) {
                    return contentProducer;
                }
            } catch (Exception e) {
                logger.warn("The content producer '" + contentProducer.getClass() + "' has thrown an exception", e);
             }
         }
         logger.info("Couldn't find a content producer for event '" + event + "'");
         return null;
     }
 
     public Collection<EntityContentProducer> getContentProducers() {
         return Collections.unmodifiableCollection(contentProducers);
     }
 }
