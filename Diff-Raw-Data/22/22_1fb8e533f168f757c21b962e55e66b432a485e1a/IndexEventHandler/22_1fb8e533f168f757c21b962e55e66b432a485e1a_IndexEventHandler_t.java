 package uk.ac.ox.oucs.search2.event;
 
 import org.sakaiproject.event.api.Event;
 import uk.ac.ox.oucs.search2.content.Content;
 
 import java.util.Collection;
 
 /**
  * Component able to intercept an {@link Event} and provide a {@link Content} and the appropriate course of action
  *
  * @author Colin Hebert
  */
 public interface IndexEventHandler {
     /**
      * Actions possible with the {@link uk.ac.ox.oucs.search2.IndexingService}
      */
     public enum IndexAction {
         /**
          * Index a new specific content, if the content was already indexed, it will be reindexed and the new values
          * will overwrite the previous values
          */
         INDEX_FILE,
         /**
          * Remove a content from the index
          */
         UNINDEX_FILE,
 
         /**
          * Index an entire site, if the content was indexed, the new version will overwrite the previous one.
          * If the content is not available anymore, it won't be modified (or deleted) from the index.
          */
         INDEX_SITE,
         /**
          * Index an entire site, if the content was indexed, the new version will overwrite the previous one.
          * If the content is not available anymore, it will be removed from the index.
          */
         REINDEX_SITE,
         /**
          * Remove every entry in the index related to one site
          */
         UNINDEX_SITE,
 
         /**
          * Index every content available and handled.
          * If the content is not available anymore, it won't be modified (or deleted) from the index.
          */
         INDEX_ALL,
         /**
          * Index every content available and handled.
          * If the content is not available anymore, it will be removed from the index.
          */
         REINDEX_ALL,
         /**
          * Remove every entry in the index
          */
         UNINDEX_ALL,
 
         /**
          * Do nothing
          */
         IGNORE
     }
 
     /**
      * Get a collection of events handled.
      *
      * @return Every event that can be handled
      */
     Collection<String> getSupportedEventTypes();
 
     /**
      * Get the action associated with the current event.
      * <p>
      * IndexActions returned by one {@link IndexEventHandler} will only affect content handled by the said {@link IndexEventHandler}.
      * Returning {@link IndexAction#UNINDEX_ALL} will only remove entries in the index which have been added by the {@link IndexEventHandler} which added them in the first place.
      * </p>
      *
      * @param event Event requiring an indexing action
      * @return The action executed for the given event, {@link IndexAction#IGNORE} if the event isn't handled
      */
     IndexAction getIndexAction(Event event);
 
     /**
      * Get the content affected by a event.
      * <p>
      * It's recommended to have a lazy {@link Iterable} which will generate {@link Content} instances on the fly.
      * </p>
      *
      * @param event event starting the indexation process
      * @return an {@link Iterable} going through each content affected
      */
     Iterable<Content> getContent(Event event);
 
     /**
      * Get the site affected by an event.
      *
      * @param event event handled
      * @return The site ID or null if there is no site affected
      */
     String getSite(Event event);
 
     /**
      * Get an unique name for the current event handler.
      * <p>
      * This name will allow to keep track of which content has been indexed by the current event handler and will be
      * reindexed or unindexed later.
      * </p>
      *
      * @return {@link IndexEventHandler} name
      */
     String getName();
 
     /**
      * Returns true if and only if the event is handled
     *
      * @param event Event to check
      * @return true if and only if the event is handled
      */
     boolean isHandled(Event event);
 }
