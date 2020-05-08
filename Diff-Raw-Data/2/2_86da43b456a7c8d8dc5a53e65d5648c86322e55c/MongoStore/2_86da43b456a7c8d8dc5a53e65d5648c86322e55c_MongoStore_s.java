 package net.onlite.morplay.mongo;
 
 import com.github.jmkgreen.morphia.Datastore;
 import com.github.jmkgreen.morphia.query.Query;
 
 /**
  * Responsible for operations on database.
  */
 public class MongoStore {
     /**
      * Morphia datastore implementation
      */
     private Datastore datastore;
 
     /**
      * Mongo collections cache
      */
     private static final MongoCollectionCache collectionCache = new MongoCollectionCache();
 
     /**
      * Constructor.
      * Initialize mongo connection.
      * @param datastore Morphia datastore implementation instance
      */
     public MongoStore(Datastore datastore) {
         this.datastore = datastore;
     }
 
     /**
      * Get collection
      * @param entityClass Entity class instance
      * @param <T> Entity type
      * @return Collection wrapper
      */
    protected <T> MongoCollection<T> collection(Class<T> entityClass) {
 
         // We need to create concrete classes for specific entity type
 
         /**
          * Concrete atomic operation class
          */
         class TAtomicOperation extends AtomicOperation<T> {
             public TAtomicOperation(Datastore ds, Query<T> query, boolean multiple) {
                 super(ds, query, multiple);
             }
         }
 
         /**
          * Concrete collection class
          */
         class TMongoCollection extends MongoCollection<T> {
             public TMongoCollection(Class<T> entityClass, Datastore ds) {
                 super(entityClass, ds);
             }
 
             @Override
             public AtomicOperation<T> atomic(Filter... filters) {
                 return new TAtomicOperation(ds, query(filters), false);
             }
 
             @Override
             public AtomicOperation<T> atomicAll(Filter... filters) {
                 return new TAtomicOperation(ds, query(filters), true);
             }
         }
 
         if (collectionCache.contains(entityClass, ds())) {
             return collectionCache.get(entityClass, ds(), TMongoCollection.class);
         }
 
         return collectionCache.add(entityClass, ds(), new TMongoCollection(entityClass, ds()));
     }
 
     /**
      * Return morphia data store implementation.
      * @return Datastore implementation instance.
      */
     public Datastore ds() {
         return datastore;
     }
 
     /**
      * Reset cache
      */
     public static void resetCache() {
         collectionCache.clear();
     }
 }
