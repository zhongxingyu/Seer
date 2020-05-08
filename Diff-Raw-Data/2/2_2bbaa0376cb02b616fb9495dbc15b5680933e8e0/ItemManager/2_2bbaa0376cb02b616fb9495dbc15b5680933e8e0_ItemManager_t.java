 package ch.x42.terye;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.NavigableMap;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.jcr.ItemExistsException;
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
 import javax.jcr.Value;
 
 import ch.x42.terye.persistence.ChangeLog;
 import ch.x42.terye.persistence.ItemState;
 import ch.x42.terye.persistence.ItemType;
 import ch.x42.terye.persistence.NodeState;
 import ch.x42.terye.persistence.PersistenceManager;
 import ch.x42.terye.persistence.PropertyState;
 
 public class ItemManager {
 
     private SessionImpl session;
     private PersistenceManager pm;
     private ChangeLog log;
     private NavigableMap<String, ItemImpl> cache = new TreeMap<String, ItemImpl>();
     private Set<String> removed = new HashSet<String>();
 
     protected ItemManager(SessionImpl session) throws RepositoryException {
         this.session = session;
         pm = PersistenceManager.getInstance();
         log = new ChangeLog();
     }
 
     /**
      * Returns an item by looking up the cache and if not present fetching the
      * item from the database.
      * 
      * @param path canonical path
      * @param type item type wanted or null if it doesn't matter
      * @return the item
      * @throws PathNotFoundException when no item at that path exists or the
      *             types don't match
      */
     public ItemImpl getItem(Path path, ItemType type)
             throws PathNotFoundException {
         // check if the item has been loaded and removed in this session
         if (removed.contains(path.toString())) {
             throw new PathNotFoundException(path.toString());
         }
         // check if the item is cached
         ItemImpl item = cache.get(path.toString());
         if (item != null) {
             // if type matters, then both types must match
             if (type == null || item.getState().getType().equals(type)) {
                 return item;
             }
             throw new PathNotFoundException(path.toString());
         }
         // load item state from db
         ItemState state = pm.load(path.toString(), type);
         if (state == null) {
             throw new PathNotFoundException(path.toString());
         }
         // instantiate, cache and return item
         if (state.getType().equals(ItemType.NODE)) {
             item = new NodeImpl(session, (NodeState) state);
         } else {
             item = new PropertyImpl(session, (PropertyState) state);
         }
         cache.put(path.toString(), item);
         return item;
     }
 
     public ItemImpl getItem(Path path) throws PathNotFoundException {
         return getItem(path, null);
     }
 
     public NodeImpl getNode(Path path) throws PathNotFoundException {
         return (NodeImpl) getItem(path, ItemType.NODE);
     }
 
     public PropertyImpl getProperty(Path path) throws PathNotFoundException {
         return (PropertyImpl) getItem(path, ItemType.PROPERTY);
     }
 
     /**
      * @param path canonical path
      */
     public NodeImpl createNode(Path path) throws PathNotFoundException,
             ItemExistsException {
         if (nodeExists(path)) {
             throw new ItemExistsException(path.toString());
         }
         NodeState state = new NodeState(path.toString());
         NodeImpl node = new NodeImpl(session, state);
         cache.put(path.toString(), node);
         removed.remove(path.toString());
         log.itemAdded(node);
 
         Path parentPath = path.getParent();
         if (parentPath == null) {
             // only the case for the root node
             return node;
         }
         NodeImpl parent = getNode(parentPath);
         parent.getState().getChildren().add(path.toString());
         log.itemModified(parent);
         return node;
     }
 
     /**
      * @param path canonical path
      */
     public PropertyImpl createProperty(Path path, Value value)
             throws PathNotFoundException, ItemExistsException {
         // disallow nodes and properties having the same path
         if (nodeExists(path)) {
             throw new ItemExistsException();
         }
         PropertyState state = new PropertyState(path.toString(), value);
         PropertyImpl property = new PropertyImpl(session, state);
         cache.put(path.toString(), property);
         removed.remove(path.toString());
         log.itemAdded(property);
         NodeImpl parent = getNode(path.getParent());
         if (!parent.getState().getProperties().contains(path.toString())) {
             parent.getState().getProperties().add(path.toString());
         }
         log.itemModified(parent);
         return property;
     }
 
     /**
      * @param path canonical path
      * @param value the new value
      */
     public void updateProperty(Path path, Value value)
             throws PathNotFoundException {
         PropertyImpl property = getProperty(path);
         property.getState().setValue(value);
         log.itemModified(property);
     }
 
     /**
      * Removes an item from cache and the database (on persist). All descendants
      * are automatically being removed from the database.
      * 
      * @param path canonical path
      */
     public void removeItem(Path path) throws RepositoryException {
         // item must be in cache, since we're being called from it
        ItemImpl item = getItem(path);
         cache.remove(path.toString());
         removed.add(path.toString());
         // takes care of removing descendants from db
         log.itemRemoved(item);
 
         // remove reference in parent
         NodeImpl parent = (NodeImpl) item.getParent();
         if (item.isNode()) {
             parent.getState().getChildren().remove(path.toString());
         } else {
             parent.getState().getProperties().remove(path.toString());
         }
         log.itemModified(parent);
 
         // only for nodes: remove descendants from cache
         if (!item.isNode()) {
             return;
         }
         Iterator<String> iterator = cache.tailMap(path.toString(), true)
                 .navigableKeySet().iterator();
         boolean done = false;
         while (iterator.hasNext() && !done) {
             String key = iterator.next();
             if (!key.startsWith(path.toString())) {
                 done = true;
             } else {
                 iterator.remove();
             }
         }
     }
 
     /**
      * @param path canonical path
      */
     public boolean nodeExists(Path path) {
         try {
             getNode(path);
         } catch (PathNotFoundException e) {
             return false;
         }
         return true;
     }
 
     /**
      * @param path canonical path
      */
     public boolean propertyExists(Path path) {
         try {
             getProperty(path);
         } catch (PathNotFoundException e) {
             return false;
         }
         return true;
     }
 
     /**
      * @param path canonical path
      */
     public boolean itemExists(Path path) {
         // XXX: not optimal
         return nodeExists(path) || propertyExists(path);
     }
 
     public void save() throws RepositoryException {
         pm.persist(log);
     }
 
     public boolean hasPendingChanges() {
         return !log.isEmpty();
     }
 
 }
