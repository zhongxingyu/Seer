 package net.onlite.morplay;
 
 import com.google.code.morphia.Datastore;
 
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Mongo collection wrappers cache
  */
 public class MongoCollectionCache {
     /**
      * Cache
      */
    private final Map<String, MongoCollection> cache = new HashMap<>();
     
     public <T> boolean contains(Class<T> entityClass, Datastore ds) {
         return cache.containsKey(key(entityClass, ds.getDB().getName()));
     }
     
     public <T> MongoCollection<T> add(Class<T> entityClass, Datastore ds, MongoCollection<T> collection) {
         cache.put(key(entityClass, ds.getDB().getName()), collection);
         return collection;
     }
     
     public <T> MongoCollection<T> get(Class<T> entityClass,
                                       Datastore ds,
                                       Class<? extends MongoCollection<T>> resultClass) {
 
         if (!contains(entityClass, ds)) {
             return null;
         }
 
         return resultClass.cast(cache.get(key(entityClass, ds.getDB().getName())));
     }
 
     public void clear() {
         cache.clear();
     }
     
     private static <T> String key(Class<T> entityClass, String dbName) {
         return entityClass.getSimpleName() + dbName;
     }
 }
