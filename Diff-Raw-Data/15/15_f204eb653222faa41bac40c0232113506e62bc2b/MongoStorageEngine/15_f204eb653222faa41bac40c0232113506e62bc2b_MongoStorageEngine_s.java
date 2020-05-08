 package eu.europeana.uim.store.mongo;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.atomic.AtomicLong;
 
 import com.google.code.morphia.Datastore;
 import com.google.code.morphia.Morphia;
 import com.google.code.morphia.mapping.DefaultCreator;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import eu.europeana.uim.MetaDataRecord;
 import eu.europeana.uim.api.StorageEngine;
 import eu.europeana.uim.api.StorageEngineException;
 import eu.europeana.uim.store.Collection;
 import eu.europeana.uim.store.DataSet;
 import eu.europeana.uim.store.Execution;
 import eu.europeana.uim.store.Provider;
 import eu.europeana.uim.store.Request;
 import org.apache.commons.lang.ArrayUtils;
 
 /**
  * Basic implementation of a StorageEngine based on MongoDB with Morphia.
  * Not optimized whatsoever.
  * <p/>
  * TODO optimize
  * TODO implement the recursive flag for providers
  *
  * @author Manuel Bernhardt <bernhardt.manuel@gmail.com>
  */
 public class MongoStorageEngine implements StorageEngine<Long> {
 
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
 
     // guys, if you change your minds again, here is a switch.
     private static final boolean ALLOW_DUPLICATE_MDR_IDENTIFIERS = true;
 
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
     
 	@Override
 	public void checkpoint() {
 	}
 	
     @Override
     public void command(String command) {
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
 
     @Override
     public Provider createProvider() {
         Provider p = new MongoProvider(providerIdCounter.getAndIncrement());
         ds.save(p);
         return p;
     }
 
     @Override
     public void updateProvider(Provider<Long> provider) throws StorageEngineException {
         for (Provider p : getAllProviders()) {
             if (p.getName() != null && (p.getName().equals(provider.getName()) || p.getMnemonic().equals(provider.getMnemonic())) && p.getId() != provider.getId()) {
                 throw new StorageEngineException("Provider with name '" + provider.getMnemonic() + "' already exists");
             }
             if (p.getMnemonic() != null && p.getMnemonic().equals(provider.getMnemonic()) && p.getId() != provider.getId()) {
                 throw new StorageEngineException("Provider with mnemonic '" + provider.getMnemonic() + "' already exists");
             }
         }
 
         for (Provider<Long> related : provider.getRelatedOut()) {
             if (!related.getRelatedIn().contains(provider)) {
                 related.getRelatedIn().add(provider);
                 ds.merge(related);
             }
         }
         for (Provider<Long> related : provider.getRelatedIn()) {
             if (!related.getRelatedOut().contains(provider)) {
                 related.getRelatedOut().add(provider);
                 ds.merge(related);
             }
         }
 
         ds.merge(provider);
     }
 
     @Override
     public Provider getProvider(Long id) {
         return ds.find(MongoProvider.class).filter(AbstractMongoEntity.LID, id).get();
     }
 
     @Override
     public Provider findProvider(String mnemonic) {
         return ds.find(MongoProvider.class).field("mnemonic").equal(mnemonic).get();
     }
 
     @Override
     public List<Provider<Long>> getAllProviders() {
         final List<Provider<Long>> res = new ArrayList<Provider<Long>>();
         for (Provider p : ds.find(MongoProvider.class).asList()) {
             res.add(p);
         }
         return res;
     }
 
     @Override
     public Collection createCollection(Provider provider) {
         Collection c = new MongodbCollection(collectionIdCounter.getAndIncrement(), provider);
         ds.save(c);
         return c;
     }
 
     @Override
     public void updateCollection(Collection collection) throws StorageEngineException {
         for (Collection c : getAllCollections()) {
             if (c.getName() != null && (c.getName().equals(collection.getName())) && c.getId() != collection.getId()) {
                 throw new StorageEngineException("Collection with name '" + collection.getMnemonic() + "' already exists");
             }
             if (c.getMnemonic() != null && c.getMnemonic().equals(collection.getMnemonic()) && c.getId() != collection.getId()) {
                 throw new StorageEngineException("Collection with mnemonic '" + collection.getMnemonic() + "' already exists");
             }
 
         }
         ds.merge(collection);
     }
 
     @Override
     public Collection<Long> getCollection(Long id) {
         return ds.find(MongodbCollection.class).filter(AbstractMongoEntity.LID, id).get();
     }
 
     @Override
     public Collection<Long> findCollection(String mnemonic) {
         return ds.find(MongodbCollection.class).filter("mnemonic", mnemonic).get();
     }
 
     @Override
    public List<Collection> getCollections(Provider provider) {
        List<Collection> res = new ArrayList<Collection>();
         for (Collection c : ds.find(MongodbCollection.class).filter("provider", provider).asList()) {
             res.add(c);
         }
         return res;
     }
 
     @Override
     public List<Collection<Long>> getAllCollections() {
         List<Collection<Long>> res = new ArrayList<Collection<Long>>();
         for (Collection c : ds.find(MongodbCollection.class).asList()) {
             res.add(c);
         }
         return res;
     }
 
     @Override
     public Request createRequest(Collection collection, Date date) {
         Request r = new MongoRequest(requestIdCounter.getAndIncrement(), (MongodbCollection) collection, date);
         ds.save(r);
         return r;
     }
 
     @Override
     public void updateRequest(Request request) throws StorageEngineException {
 
         for (Request r : ds.find(MongoRequest.class).filter("collection", request.getCollection()).asList()) {
             if (r.getDate().equals(request.getDate()) && r.getId() != request.getId()) {
                 String unique = "REQUEST/" + request.getCollection().getMnemonic() + "/" + request.getDate();
                 throw new IllegalStateException("Duplicate unique key for request: <" + unique + ">");
             }
         }
         ds.merge(request);
     }
 
     @Override
    public List<Request> getRequests(Collection collection) {
        List<Request> res = new ArrayList<Request>();
         for (Request r : ds.find(MongoRequest.class).filter("collection", collection).asList()) {
             res.add(r);
         }
         return res;
     }
 
     @Override
     public Request getRequest(Long id) throws StorageEngineException {
         return ds.find(MongoRequest.class).filter(AbstractMongoEntity.LID, id).get();
     }
 
     @Override
     public MetaDataRecord createMetaDataRecord(Request request, String identifier) throws StorageEngineException {
         BasicDBObject object = new BasicDBObject();
         MongoMetadataRecord mdr = new MongoMetadataRecord(object, request, identifier, mdrIdCounter.getAndIncrement());
         records.insert(mdr.getObject());
         return mdr;
     }
 
     @Override
     public void updateMetaDataRecord(MetaDataRecord record) throws StorageEngineException {
 /*
         if(!ALLOW_DUPLICATE_MDR_IDENTIFIERS) {
             String unique = "MDR/" + record.getRequest().getCollection().getProvider().getMnemonic() + "/" + record.getIdentifier();
 
             // no sql, just pain
             // this absolutely doesn't scale, someone else will have to optimize this piece of crap - I'm not proud of it but at least it works.
             BasicDBObject uniqueQuery = new BasicDBObject("identifier", record.getIdentifier());
             List<DBObject> one = records.find(uniqueQuery, BasicDBObjectBuilder.start("request", 1).add(AbstractMongoEntity.LID, 1).get()).toArray();
             List<MongoRequest> ftw = ds.find(MongoRequest.class).asList();
             for (DBObject o : one) {
                 for (MongoRequest f : ftw) {
                     if (o.get("request").equals(f.getId()) && !o.get(AbstractMongoEntity.LID).equals(record.getId())) {
                         if (f.getCollection().getProvider().getMnemonic().equals(record.getRequest().getCollection().getProvider().getMnemonic())) {
                             throw new IllegalStateException("Duplicate unique key for record: <" + unique + ">");
                         }
                     }
                 }
             }
         }
 */
 
         BasicDBObject query = new BasicDBObject(AbstractMongoEntity.LID, record.getId());
         records.update(query, ((MongoMetadataRecord) record).getObject());
     }
 
     @Override
     public Execution createExecution(DataSet entity, String workflow) throws StorageEngineException {
         MongoExecution me = new MongoExecution(executionIdCounter.getAndIncrement());
         me.setDataSet(entity);
         me.setWorkflowName(workflow);
         ds.save(me);
         return me;
     }
 
     @Override
     public void updateExecution(Execution execution) throws StorageEngineException {
         ds.merge(execution);
     }
 
     @Override
     public List<Execution<Long>> getAllExecutions() {
         List<Execution<Long>> res = new ArrayList<Execution<Long>>();
         for (Execution<Long> e : ds.find(MongoExecution.class).asList()) {
             res.add(e);
         }
         return res;
     }
 
     @Override
     public List<MetaDataRecord<Long>> getMetaDataRecords(List<Long> ids) {
         ArrayList<MetaDataRecord<Long>> res = new ArrayList<MetaDataRecord<Long>>();
         BasicDBObject query = new BasicDBObject();
         query.put(AbstractMongoEntity.LID, new BasicDBObject("$in", ids));
         for (DBObject object : records.find(query)) {
             Request request = ds.find(MongoRequest.class).filter(AbstractMongoEntity.LID, object.get("request")).get();
             res.add(new MongoMetadataRecord(object, request, (String) object.get("identifier"), ((Long) object.get(AbstractMongoEntity.LID)).longValue()));
         }
 
         return res;
     }
 
     @Override
     public MetaDataRecord<Long> getMetaDataRecord(Long id) throws StorageEngineException {
         BasicDBObject query = new BasicDBObject(AbstractMongoEntity.LID, id);
         DBObject theOne = records.findOne(query);
         Request request = ds.find(MongoRequest.class).filter(AbstractMongoEntity.LID, theOne.get("request")).get();
         return new MongoMetadataRecord<Long>(theOne, request, (String) theOne.get("identifier"), id);
     }
 
 
     @Override
     public Long[] getByRequest(Request request) {
         BasicDBObject query = new BasicDBObject("request", request.getId());
         BasicDBObject fields = new BasicDBObject(AbstractMongoEntity.LID, 1);
         List<DBObject> results = records.find(query, fields).toArray();
         Long[] res = new Long[results.size()];
         for (int i = 0; i < results.size(); i++) {
             res[i] = (Long) results.get(i).get(AbstractMongoEntity.LID);
         }
 
         return res;
     }
 
     @Override
     public Long[] getByCollection(Collection collection) {
         // mdr -> request -> collection
         MongodbCollection mongodbCollection = ds.find(MongodbCollection.class).filter("lid", collection.getId()).get();
         Long[] reqIds = getFromCollection(mongodbCollection);
         Long[] res = getRecordsFromRequestIds(reqIds);
         return res;
     }
 
     private Long[] getRecordsFromRequestIds(Long[] reqIds) {
         BasicDBObject query = new BasicDBObject("request", new BasicDBObject("$in", reqIds));
         BasicDBObject fields = new BasicDBObject(AbstractMongoEntity.LID, 1);
 
         List<DBObject> results = records.find(query, fields).toArray();
         Long[] res = new Long[results.size()];
         for (int i = 0; i < results.size(); i++) {
             res[i] = (Long) results.get(i).get(AbstractMongoEntity.LID);
         }
         return res;
     }
 
     private Long[] getFromCollection(MongodbCollection<Long> mongodbCollection) {
        List<MongoRequest<Long>> reqs = (List<MongoRequest<Long>>) ds.find(MongoRequest.class).filter("collection", mongodbCollection).asList();
         Long[] reqIds = new Long[reqs.size()];
         for (int i = 0; i < reqs.size(); i++) {
            reqIds[i] = reqs.get(i).getId();
         }
         return reqIds;
     }
 
     // TODO recursive
     public Long[] getByProvider(Provider<Long> provider, boolean recursive) {
         List<Long> providers = new ArrayList<Long>();
         if(recursive) {
             getRecursive(provider, providers);
         } else {
             providers.add(provider.getId());
         }
 
         Long[] ids = new Long[0];
 
         for(long id : providers) {
             MongoProvider mongoProvider = ds.find(MongoProvider.class).filter("lid", id).get();
             ids = (Long[]) ArrayUtils.addAll(ids, getRequestIdsFromProvider(mongoProvider));
         }
         return getRecordsFromRequestIds(ids);
     }
 
     public void getRecursive(Provider<Long> provider, List<Long> result) {
         if (!result.contains(provider.getId())){
             result.add(provider.getId());
             for (Provider related : provider.getRelatedOut()) {
                 getRecursive(related, result);
             }
         }
     }
 
 
     private Long[] getRequestIdsFromProvider(MongoProvider mongoProvider) {
         List<MongoRequest> reqs = new ArrayList<MongoRequest>();
         List<MongodbCollection> collections = ds.find(MongodbCollection.class).filter("provider", mongoProvider).asList();
         Long[] reqIds = new Long[0];
 
         for (MongodbCollection collection : collections) {
             reqIds = (Long[]) ArrayUtils.addAll(reqIds, getFromCollection(collection));
         }
         return reqIds;
     }
 
     @Override
     public Long[] getAllIds() {
         List<DBObject> tutti = records.find().toArray();
         Long[] tuttiArray = new Long[tutti.size()];
         for (int i = 0; i < tutti.size(); i++) {
             tuttiArray[i] = (Long) tutti.get(i).get("lid");
         }
         return tuttiArray;
     }
 
     @Override
     public int getTotalByRequest(Request request) {
         BasicDBObject query = new BasicDBObject("request", request.getId());
         BasicDBObject fields = new BasicDBObject(AbstractMongoEntity.LID, 1);
         return records.find(query, fields).count();
     }
 
     @Override
     public int getTotalByCollection(Collection<Long> collection) {
         MongodbCollection mongodbCollection = ds.find(MongodbCollection.class).filter("lid", collection.getId()).get();
         Long[] reqIds = getFromCollection(mongodbCollection);
 
         return getCountFromRequestIds(reqIds);
     }
 
     private int getCountFromRequestIds(Long[] reqIds) {
         BasicDBObject query = new BasicDBObject("request", new BasicDBObject("$in", reqIds));
         BasicDBObject fields = new BasicDBObject(AbstractMongoEntity.LID, 1);
 
         return records.find(query, fields).count();
     }
 
     // TODO recursive
     @Override
     public int getTotalByProvider(Provider provider, boolean recursive) {
         MongoProvider mongoProvider = ds.find(MongoProvider.class).filter("lid", provider.getId()).get();
         return getCountFromRequestIds(getRequestIdsFromProvider(mongoProvider));
     }
 
     @Override
     public int getTotalForAllIds() {
         return new Long(records.count()).intValue();
     }
 
 
     @Override
     public Execution getExecution(Long id) throws StorageEngineException {
         return ds.find(MongoExecution.class, AbstractMongoEntity.LID, id).get();
     }
 
 
 }
