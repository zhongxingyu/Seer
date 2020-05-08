 package ch.x42.terye;
 
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.NavigableMap;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.jcr.PathNotFoundException;
 import javax.jcr.RepositoryException;
 
 import ch.x42.terye.persistence.ChangeLog;
 import ch.x42.terye.persistence.ItemState.ItemType;
 import ch.x42.terye.persistence.NodeState;
 import ch.x42.terye.persistence.PersistenceManager;
 import ch.x42.terye.persistence.PropertyState;
 
 public class ItemManager {
 
     private PersistenceManager pm;
     private ChangeLog log;
     private NavigableMap<String, ItemImpl> cache = new TreeMap<String, ItemImpl>();
     private Set<String> removed = new HashSet<String>();
 
     protected ItemManager() throws RepositoryException {
         pm = PersistenceManager.getInstance();
         log = new ChangeLog();
     }
 
     private boolean isCached(String path) {
         return cache.containsKey(path);
     }
 
     /**
      * @param path canonical path
      */
     public NodeImpl getNode(String path) throws PathNotFoundException {
         // check if the node existed and has been removed in this session
         if (removed.contains(path)) {
             throw new PathNotFoundException();
         }
         // check if the node is cached
         if (isCached(path)) {
             return (NodeImpl) cache.get(path);
         }
         // load node from db
         NodeState state = pm.load(path);
         if (state == null) {
             throw new PathNotFoundException();
         }
         NodeImpl node = new NodeImpl(this, state);
         cache.put(path, node);
         return node;
     }
 
     /**
      * @param path canonical path
      * @param parent canonical path to parent
      */
     public NodeImpl createNode(String path, String parentPath)
             throws PathNotFoundException {
         NodeState state = new NodeState(path, parentPath);
         NodeImpl node = new NodeImpl(this, state);
         cache.put(path, node);
         removed.remove(path);
         log.nodeAdded(node);
         if (parentPath == null) {
             // only the case for the root node
             return node;
         }
         NodeImpl parent = getNode(parentPath);
         parent.getState().getChildren().add(path);
         log.nodeModified(parent);
         return node;
     }
 
     /**
      * 
      * @param path canonical path
      * @param parentPath canonical path to parent
      */
    public PropertyImpl createProperty(String path, String parentPath, Object value) {
         PropertyState state = new PropertyState(path, parentPath, value);
         PropertyImpl property = new PropertyImpl(this, state);
         cache.put(path, property);
         removed.remove(path);
         log.propertyAdded(property);
         return property;
     }
 
     /**
      * @param path canonical path
      */
     public void removeNode(String path) throws RepositoryException {
         NodeImpl node = getNode(path);
         removed.add(path);
         // takes care of removing descendents from db
         log.nodeRemoved(node);
         // remove entry from parent's children
         NodeImpl parent = (NodeImpl) node.getParent();
         parent.getState().getChildren().remove(path);
         log.nodeModified(parent);
 
         // remove node and descendents from cache
         Iterator<String> iterator = cache.tailMap(path, true).navigableKeySet()
                 .iterator();
         boolean done = false;
         while (iterator.hasNext() && !done) {
             String key = iterator.next();
             if (!key.startsWith(path)) {
                 done = true;
             } else {
                 iterator.remove();
             }
         }
     }
 
     /**
      * @param path canonical path
      */
    public boolean nodeExists(String path) throws PathNotFoundException {
        if (isCached(path)) {
            return true;
         }
        return getNode(path) != null;
     }
 
     /**
      * @param path canonical slash-terminated prefix path
      */
     public boolean nodesExist(String pathPrefix) {
         return pm.count(pathPrefix, ItemType.NODE) > 0;
     }
 
     public void save() throws RepositoryException {
         pm.persist(log);
     }
 
 }
