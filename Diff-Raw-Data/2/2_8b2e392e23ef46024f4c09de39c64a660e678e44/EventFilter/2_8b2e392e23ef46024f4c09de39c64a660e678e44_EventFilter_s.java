 package ch.x42.terye.observation;
 
 import javax.jcr.RepositoryException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import ch.x42.terye.SessionImpl;
 import ch.x42.terye.path.Path;
 import ch.x42.terye.path.PathFactory;
 
 public class EventFilter {
 
     private final Logger logger = LoggerFactory.getLogger(getClass());
 
     private int eventTypes;
     private Path path;
     private boolean isDeep;
     private String[] ids;
     private String[] nodeTypeNames;
     private boolean noLocal;
     private SessionImpl session;
 
     public EventFilter(int eventTypes, String absPath, boolean isDeep,
             String[] ids, String[] nodeTypeNames, boolean noLocal,
             SessionImpl session) throws RepositoryException {
         this.eventTypes = eventTypes;
         this.path = PathFactory.create(absPath);
         if (this.path.isRelative()) {
             throw new RepositoryException("Path must be absolute");
         }
         this.isDeep = isDeep;
         this.ids = ids;
         this.nodeTypeNames = nodeTypeNames;
         this.noLocal = noLocal;
         this.session = session;
     }
 
     public boolean filters(EventImpl event) {
         // event type
         if ((eventTypes & event.getType()) == 0) {
             return true;
         }
         // locality
        if (noLocal && !session.equals(event.getSession())) {
             return true;
         }
         // check path
         if (event.getParentId() != null) {
             Path eventPath = PathFactory.create(event.getParentId().toString());
             boolean match = eventPath.equals(path);
             if (!match && isDeep) {
                 try {
                     match = eventPath.isDescendantOf(path);
                 } catch (RepositoryException e) {
                     logger.warn("Caught exception while applying event filter",
                             e);
                 }
             }
             if (!match) {
                 return true;
             }
         } else {
             // filter root node
             return true;
         }
 
         // XXX: check ids
         // XXX: check node types
         return false;
     }
 
 }
