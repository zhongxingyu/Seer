 package uk.ac.ox.oucs.search2.event;
 
 import org.sakaiproject.event.api.Event;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import uk.ac.ox.oucs.search2.content.Content;
 
 import java.util.Collections;
 
 /**
  * Basic implementation of {@link IndexEventHandler}
  *
  * @author Colin Hebert
  */
 public abstract class AbstractIndexEventHandler implements IndexEventHandler {
     private static final Logger logger = LoggerFactory.getLogger(AbstractIndexEventHandler.class);
 
     @Override
     public Iterable<Content> getContent(Event event) {
         IndexAction indexAction = getIndexAction(event);
         String reference = event.getResource();
 
         switch (indexAction) {
             case INDEX_FILE:
             case UNINDEX_FILE:
                 return Collections.singleton(getContent(reference));
             case INDEX_SITE:
             case REINDEX_SITE:
             case UNINDEX_SITE:
                 return getSiteContent(reference);
             case INDEX_ALL:
             case REINDEX_ALL:
             case UNINDEX_ALL:
                 return getAllContent();
             default:
                logger.warn("Action '" + indexAction + "' isn't supported");
                 return Collections.emptyList();
         }
     }
 
     /**
      * {@inheritDoc}
      * Get the current class canonical name as the unique name
      */
     @Override
     public String getName() {
         return this.getClass().getCanonicalName();
     }
 
     /**
      * Get content from a reference
      *
      * @param reference Reference of the wanted content
      * @return a Content
      */
     protected abstract Content getContent(String reference);
 
     /**
      * Get every content possible associated with one site
      *
      * @param siteId Unique identifier of the site
      * @return an Iterable containing every element related to the site
      */
     protected abstract Iterable<Content> getSiteContent(String siteId);
 
     /**
      * Get every possible content to be indexed/unindexed
      *
      * @return an Iterable containing every element handled
      */
     protected abstract Iterable<Content> getAllContent();
 }
