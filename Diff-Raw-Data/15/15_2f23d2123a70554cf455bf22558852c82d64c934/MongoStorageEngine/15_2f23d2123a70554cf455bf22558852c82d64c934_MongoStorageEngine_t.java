 package eu.europeana.uim.store.mongo;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.Morphia;
 import com.google.code.morphia.mapping.DefaultCreator;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import eu.europeana.uim.MDRFieldRegistry;
 import eu.europeana.uim.MetaDataRecord;
 import eu.europeana.uim.api.StorageEngine;
 import eu.europeana.uim.api.StorageEngineException;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.Execution;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.store.Request;
 import org.apache.commons.lang.ArrayUtils;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicLong;
 
 /**
  * Basic implementation of a StorageEngine based on MongoDB with Morphia.
  * Not optimized whatsoever.
  * <p/>
  * TODO optimize
  * TODO implement the recursive flag for providers
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class MongoStorageEngine implements StorageEngine {
 
     private static final String DEFAULT_UIM_DB_NAME = "UIM";
     Mongo mongo = null;
     private DB db = null;
     private DBCollection records = null;
     private Datastore ds = null;
 
     private EngineStatus status = EngineStatus.STOPPED;
 
     private AtomicLong providerIdCounter = null;
     private AtomicLong collectionIdCounter = null;
     private AtomicLong requestIdCounter = null;
     private AtomicLong executionIdCounter = null;
     private AtomicLong mdrIdCounter = null;
 
     private String dbName;
 
     public MongoStorageEngine(String dbName) {
         this.dbName = dbName;
     }
 
     public MongoStorageEngine() {
 
     }
 
     public String getIdentifier() {
         return MongoStorageEngine.class.getSimpleName();
     }
 
     public void setConfiguration(Map<String, String> config) {
     }
 
     public Map<String, String> getConfiguration() {
         return new HashMap<String, String>();
     }
 
     public void initialize() {
         try {
             if (dbName == null) {
                 dbName = DEFAULT_UIM_DB_NAME;
             }
             status = EngineStatus.BOOTING;
             mongo = new Mongo();
             db = mongo.getDB(dbName);
             records = db.getCollection("records");
             Morphia morphia = new Morphia();
 
             // see http://code.google.com/p/morphia/issues/detail?id=208
             morphia.getMapper().getOptions().setObjectFactory(new DefaultCreator() {
                 @Override
                 protected ClassLoader getClassLoaderForClass(String clazz, DBObject object) {
                     // we're the only ones for now using Morphia so we can be sure that in any case
                     // the classloader of this bundle has to be used
                     return MongoBundleActivator.getBundleClassLoader();
                 }
             });
 
             morphia.
                     map(MongodbCollection.class).
                     map(MongoExecution.class).
                     map(MongoProvider.class).
                     map(MongoRequest.class);
             ds = morphia.createDatastore(mongo, dbName);
             status = EngineStatus.RUNNING;
 
             // initialize counters
             providerIdCounter = new AtomicLong(ds.find(MongoProvider.class).countAll());
             requestIdCounter = new AtomicLong(ds.find(MongoRequest.class).countAll());
             collectionIdCounter = new AtomicLong(ds.find(MongodbCollection.class).countAll());
             executionIdCounter = new AtomicLong(ds.find(MongoExecution.class).countAll());
             mdrIdCounter = new AtomicLong(records.count());
         } catch (UnknownHostException e) {
             e.printStackTrace();
         }
     }
 
     public void shutdown() {
         status = EngineStatus.STOPPED;
     }
 
     public String getDbName() {
         return dbName;
     }
 
     public EngineStatus getStatus() {
         return status;
     }
 
     public long size() {
         return 0;
     }
 
     public Provider createProvider() {
         Provider p = new MongoProvider(providerIdCounter.getAndIncrement());
         ds.save(p);
         return p;
     }
 
     public void updateProvider(Provider provider) throws StorageEngineException {
         for (Provider p : getProvider()) {
             if (p.getName() != null && p.getName().equals(provider.getName()) && p.getId() != provider.getId()) {
                 throw new StorageEngineException("Provider with name '" + provider.getMnemonic() + "' already exists");
             }
             if (p.getMnemonic() != null && p.getMnemonic().equals(provider.getMnemonic())  && p.getId() != provider.getId()) {
                 throw new StorageEngineException("Provider with mnemonic '" + provider.getMnemonic() + "' already exists");
             }
         }
        ds.merge(provider);
     }
 
     public Provider getProvider(long id) {
         return ds.find(MongoProvider.class).filter(AbstractMongoEntity.LID, id).get();
     }
 
     public Provider findProvider(String mnemonic) {
         return ds.find(MongoProvider.class).field("mnemonic").equal(mnemonic).get();
     }
 
     public List<Provider> getProvider() {
         final List<Provider> res = new ArrayList<Provider>();
         for (Provider p : ds.find(MongoProvider.class).asList()) {
             res.add(p);
         }
         return res;
     }
 
     public Collection createCollection(Provider provider) {
         Collection c = new MongodbCollection(collectionIdCounter.getAndIncrement(), provider);
         ds.save(c);
         return c;
     }
 
     public void updateCollection(Collection collection) throws StorageEngineException {
         for (Collection c : getAllCollections()) {
             if (c.getName() != null && c.getName().equals(collection.getName()) && c.getId() != collection.getId()) {
                 throw new StorageEngineException("Collection with name '" + collection.getMnemonic() + "' already exists");
             }
             if (c.getMnemonic() != null && c.getMnemonic().equals(collection.getMnemonic()) && c.getId() != collection.getId()) {
                 throw new StorageEngineException("Collection with mnemonic '" + collection.getMnemonic() + "' already exists");
             }
 
         }
        ds.merge(collection);
     }
 
     public Collection getCollection(long id) {
         return ds.find(MongodbCollection.class).filter(AbstractMongoEntity.LID, id).get();
     }
 
     public Collection findCollection(String mnemonic) {
         return ds.find(MongodbCollection.class).filter("mnemonic", mnemonic).get();
     }
 
     public List<Collection> getCollections(Provider provider) {
         List<Collection> res = new ArrayList<Collection>();
         for (Collection c : ds.find(MongodbCollection.class).filter("provider", provider).asList()) {
             res.add(c);
         }
         return res;
     }
 
     public List<Collection> getAllCollections() {
         List<Collection> res = new ArrayList<Collection>();
         for (Collection c : ds.find(MongodbCollection.class).asList()) {
             res.add(c);
         }
         return res;
     }
 
     public Request createRequest(Collection collection) {
         Request r = new MongoRequest(requestIdCounter.getAndIncrement(), (MongodbCollection) collection);
         ds.save(r);
         return r;
     }
 
     public void updateRequest(Request request) throws StorageEngineException {
        ds.merge(request);
     }
 
     public List<Request> getRequests(Collection collection) {
         List<Request> res = new ArrayList<Request>();
         for (Request r : ds.find(MongoRequest.class).filter("collection", collection).asList()) {
             res.add(r);
         }
         return res;
     }
 
     public MetaDataRecord<MDRFieldRegistry> createMetaDataRecord(Request request) {
         BasicDBObject object = new BasicDBObject();
         MongoMetadataRecord mdr = new MongoMetadataRecord(object, request, mdrIdCounter.getAndIncrement());
         records.insert(mdr.getObject());
         return mdr;
     }
 
     public void updateMetaDataRecord(MetaDataRecord<MDRFieldRegistry> record) throws StorageEngineException {
        BasicDBObject query = new BasicDBObject(AbstractMongoEntity.LID, record.getId());
        records.update(query, ((MongoMetadataRecord<MDRFieldRegistry>) record).getObject());
     }
 
     public Execution createExecution() {
         MongoExecution me = new MongoExecution(executionIdCounter.getAndIncrement());
         ds.save(me);
         return me;
     }
 
     public void updateExecution(Execution execution) throws StorageEngineException {
        ds.merge(execution);
     }
 
     public List<Execution> getExecutions() {
         List<Execution> res = new ArrayList<Execution>();
         for (Execution e : ds.find(MongoExecution.class).asList()) {
             res.add(e);
         }
         return res;
     }
 
     public MetaDataRecord<MDRFieldRegistry>[] getMetaDataRecords(long... ids) {
         ArrayList<MetaDataRecord<MDRFieldRegistry>> res = new ArrayList<MetaDataRecord<MDRFieldRegistry>>();
         BasicDBObject query = new BasicDBObject();
         query.put(AbstractMongoEntity.LID, new BasicDBObject("$in", ids));
         for (DBObject object : records.find(query)) {
             Request request = ds.find(MongoRequest.class).filter(AbstractMongoEntity.LID, object.get("request")).get();
             res.add(new MongoMetadataRecord<MDRFieldRegistry>(object, request, ((Long) object.get(AbstractMongoEntity.LID)).longValue()));
         }
 
         return res.toArray(new MetaDataRecord[res.size()]);
     }
 
     public long[] getByRequest(Request request) {
         BasicDBObject query = new BasicDBObject("request", request.getId());
         BasicDBObject fields = new BasicDBObject(AbstractMongoEntity.LID, 1);
         List<DBObject> results = records.find(query, fields).toArray();
         long[] res = new long[results.size()];
         for (int i = 0; i < results.size(); i++) {
             res[i] = (Long) results.get(i).get(AbstractMongoEntity.LID);
         }
 
         return res;
     }
 
     public long[] getByCollection(Collection collection) {
         // mdr -> request -> collection
         MongodbCollection mongodbCollection = ds.find(MongodbCollection.class).filter("lid", collection.getId()).get();
         long[] reqIds = getFromCollection(mongodbCollection);
         long[] res = getRecordsFromRequestIds(reqIds);
         return res;
     }
 
     private long[] getRecordsFromRequestIds(long[] reqIds) {
         BasicDBObject query = new BasicDBObject("request", new BasicDBObject("$in", reqIds));
         BasicDBObject fields = new BasicDBObject(AbstractMongoEntity.LID, 1);
 
         List<DBObject> results = records.find(query, fields).toArray();
         long[] res = new long[results.size()];
         for (int i = 0; i < results.size(); i++) {
             res[i] = (Long) results.get(i).get(AbstractMongoEntity.LID);
         }
         return res;
     }
 
     private long[] getFromCollection(MongodbCollection mongodbCollection) {
         List<MongoRequest> reqs = ds.find(MongoRequest.class).filter("collection", mongodbCollection).asList();
         long[] reqIds = new long[reqs.size()];
         for (int i = 0; i < reqs.size(); i++) {
             reqIds[i] = reqs.get(i).getId();
         }
         return reqIds;
     }
 
     // TODO recursive
     public long[] getByProvider(Provider provider, boolean recursive) {
         MongoProvider mongoProvider = ds.find(MongoProvider.class).filter("lid", provider.getId()).get();
         long[] reqIds = getRequestIdsFromProvider(mongoProvider);
         return getRecordsFromRequestIds(reqIds);
     }
 
     private long[] getRequestIdsFromProvider(MongoProvider mongoProvider) {
         List<MongoRequest> reqs = new ArrayList<MongoRequest>();
         List<MongodbCollection> collections = ds.find(MongodbCollection.class).filter("provider", mongoProvider).asList();
         long[] reqIds = new long[0];
 
         for (MongodbCollection collection : collections) {
             reqIds = ArrayUtils.addAll(reqIds, getFromCollection(collection));
         }
         return reqIds;
     }
 
     public long[] getAllIds() {
         BasicDBObject query = new BasicDBObject("1", 1);
         BasicDBObject fields = new BasicDBObject(AbstractMongoEntity.LID, 1);
         List<DBObject> tutti = records.find(query, fields).toArray();
         long[] tuttiArray = new long[tutti.size()];
         for (int i = 0; i < tutti.size(); i++) {
             tuttiArray[i] = (Long) tutti.get(i).get("lid");
         }
         return tuttiArray;
     }
 
     public int getTotalByRequest(Request request) {
         BasicDBObject query = new BasicDBObject("request", request.getId());
         BasicDBObject fields = new BasicDBObject(AbstractMongoEntity.LID, 1);
         return records.find(query, fields).count();
     }
 
     public int getTotalByCollection(Collection collection) {
         MongodbCollection mongodbCollection = ds.find(MongodbCollection.class).filter("lid", collection.getId()).get();
         long[] reqIds = getFromCollection(mongodbCollection);
 
         return getCountFromRequestIds(reqIds);
     }
 
     private int getCountFromRequestIds(long[] reqIds) {
         BasicDBObject query = new BasicDBObject("request", new BasicDBObject("$in", reqIds));
         BasicDBObject fields = new BasicDBObject(AbstractMongoEntity.LID, 1);
 
         return records.find(query, fields).count();
     }
 
     // TODO recursive
     public int getTotalByProvider(Provider provider, boolean recursive) {
         MongoProvider mongoProvider = ds.find(MongoProvider.class).filter("lid", provider.getId()).get();
         return getCountFromRequestIds(getRequestIdsFromProvider(mongoProvider));
     }
 
     public int getTotalForAllIds() {
         return new Long(records.count()).intValue();
     }
 }
