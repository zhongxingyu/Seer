 package org.oobium.persist.mongo;
 
 import static org.oobium.utils.coercion.TypeCoercer.coerce;
 import static org.oobium.persist.SessionCache.expireCache;
 import static org.oobium.utils.StringUtils.parseUrl;
 import static org.oobium.utils.StringUtils.tableName;
 import static org.oobium.utils.json.JsonUtils.toMap;
 import static org.oobium.utils.literal.Dictionary;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.concurrent.locks.ReadWriteLock;
 import java.util.concurrent.locks.ReentrantReadWriteLock;
 
 import org.bson.types.ObjectId;
 import org.oobium.logging.LogProvider;
 import org.oobium.logging.Logger;
 import org.oobium.persist.Model;
 import org.oobium.persist.ModelAdapter;
 import org.oobium.persist.PersistClient;
 import org.oobium.persist.PersistService;
 import org.oobium.persist.ServiceInfo;
 import org.oobium.persist.mongo.internal.ObjectIdCoercer;
 import org.oobium.utils.StringUtils;
 import org.oobium.utils.coercion.TypeCoercer;
 import org.osgi.framework.Bundle;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.util.tracker.ServiceTracker;
 import org.osgi.util.tracker.ServiceTrackerCustomizer;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 /**
  * A {@link PersistService} for MongoDB.
  * <p>
  * A note on style: this persist service uses {@link StringUtils#tableName(String)} for naming collections,
  * which causes them to be plural and underscored (DataPoint -> data_points), while the naming of fields uses
  * {@link StringUtils#varName(String)}, thus giving them a lower starting CamelCase form
  * (DataPoint -> dataPoint).
  * </p>
  */
 public class MongoPersistService implements BundleActivator, PersistService {
 
 	private static final String createdAt = "createdAt";
 	private static final String updatedAt = "updatedAt";
 	private static final String createdOn = "createdOn";
 	private static final String updatedOn = "updatedOn";
 	
 	private static final ThreadLocal<String> threadClient = new ThreadLocal<String>();
 	private static final ThreadLocal<DB> threadDB = new ThreadLocal<DB>();
 
 	static {
 		TypeCoercer.addCoercer(new ObjectIdCoercer());
 	}
 	
 	protected final Logger logger;
 	private BundleContext context;
 	private Map<String, Database> databases;
 
 	private ServiceTracker appTracker;
 
 	private final ReadWriteLock lock;
 
 	public MongoPersistService() {
 		logger = LogProvider.getLogger(MongoPersistService.class);
 		databases = new HashMap<String, Database>();
 		lock = new ReentrantReadWriteLock();
 	}
 	
 
 	/**
 	 * <p>Instantiates a new MongoPersistService, opens a session and adds the given database.
 	 * The service is ready to use as-is, but closeSession() must be called when it is
 	 * no longer used to free up database resources.</p>
 	 * <p>This form of MongoPersistService is not intended to be used in a multi-threaded 
 	 * environment because it uses a single connection</p>
 	 * <p>Specifying an in-memory database is a good performance increase for tests</p>
 	 * @param client
 	 * @param timeout
 	 */
 	public MongoPersistService(String client, Map<String, Object> properties) {
 		this();
 		addDatabase(client, properties);
 		openSession(client);
 	}
 	
 	public MongoPersistService(String client, String url) {
 		this(client, parseUrl(url));
 	}
 	
 	private void addDatabase(String client, Map<String, Object> properties) {
 		lock.readLock().lock();
 		try {
 			if(databases.containsKey(client)) {
 				return;
 			}
 		} finally {
 			lock.readLock().unlock();
 		}
 		
 		lock.writeLock().lock();
 		try {
 			Database db = new Database(properties);
 			databases.put(client, db);
 			logger.info("added Database for {} ({})", client, db.getDatabaseName());
 		} catch(Exception e) {
 			logger.error("failed to add Database for {}", e, client);
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 	
 	@Override
 	public void closeSession() {
 		threadClient.set(null);
 		expireCache();
 	}
 	
 	@Override
 	public long count(Class<? extends Model> clazz) throws Exception {
 		return count(clazz, (Map<String, Object>) null);
 	}
 	
 	@Override
 	public long count(Class<? extends Model> clazz, Map<String, Object> query, Object... values) throws Exception {
 		if(query == null) query = new HashMap<String, Object>(0);
 
 		DB db = getDB();
 		DBCollection c = db.getCollection(tableName(clazz));
 		
 		if(values.length > 0) {
 			int i = 0;
 			for(String key : query.keySet()) {
 				if("?".equals(query.get(key))) {
 					query.put(key, values[i++]);
 				}
 			}
 		}
 		
 		// remove $limit and $order so the same query can be used for #find and #findAll
 		query.remove("$limit");
 		query.remove("$order");
 
 		return c.count(new BasicDBObject(query));
 	}
 	
 	@Override
 	public long count(Class<? extends Model> clazz, String query, Object... values) throws Exception {
 		return count(clazz, toMap(query, values), new Object[0]);
 	}
 	
 	@Override
 	public void create(Model... models) throws Exception {
 		DB db = getDB();
 		for(Model model : models) {
 			logger.debug("start doCreate {}", model.asSimpleString());
 			ModelAdapter adapter = ModelAdapter.getAdapter(model);
 			DBCollection c = db.getCollection(tableName(adapter.getModelClass()));
 			Map<String, Object> data = getData(model, false, true);
 			logger.trace(String.valueOf(data));
 			DBObject dbo = new BasicDBObject(data);
 			c.insert(dbo);
 			model.setId(dbo.get("_id"));
 			logger.debug("end doCreate");
 		}
 	}
 
 	private Map<String, Object> getData(Model model, boolean includeId, boolean isCreate) {
 		ModelAdapter adapter = ModelAdapter.getAdapter(model);
 		Map<String, Object> data = model.getAll();
 		for(Iterator<String> iter = data.keySet().iterator(); iter.hasNext(); ) {
 			String field = iter.next();
 			if(adapter.hasAttribute(field)) {
 				continue;
 			}
 			if(adapter.hasOne(field) && adapter.hasKey(field)) {
 				Model m = (Model) model.get(field);
 				if(m != null) {
 					if(adapter.isEmbedded(field)) {
 						data.put(field, getData(m, true, isCreate));
 					} else {
 						if(m.isNew()) m.create();
 						data.put(field, m.getId());
 					}
 				}
 				continue;
 			}
 			if(adapter.hasMany(field) && adapter.isEmbedded(field)) {
 				Collection<?> collection = (Collection<?>) model.get(field);
 				if(!collection.isEmpty()) {
 					List<Object> list = new ArrayList<Object>();
 					for(Object o : collection) {
 						Model m = (Model) o;
 						Map<String, Object> map;
 						String[] fields = adapter.getEmbedded(field);
 						if(fields == null) {
 							map = getData(m, true, isCreate);
 						} else {
 							map = new HashMap<String, Object>();
 							for(String f : fields) {
 								map.put(f, m.get(f));
 							}
 							if(!m.isNew()) {
 								map.put("id", m.getId());
 							}
 						}
 						list.add(map);
 					}
 					data.put(field, list);
 				}
 				continue;
 			}
 			// if didn't hit a continue, fall through and remove this field
 			iter.remove();
 		}
 		if(includeId && !model.isNew()) {
 			data.put("id", model.getId());
 		}
 		if(adapter.isTimeStamped() || adapter.isDateStamped()) {
 			long date = System.currentTimeMillis();
 			if(adapter.isDateStamped()) {
 				if(isCreate) {
 					if(data.get(createdOn) == null) {
 						data.put(createdOn, date);
 					}
 					if(data.get(updatedOn) == null) {
 						data.put(updatedOn, date);
 					}
 				} else {
 					if(data.get(updatedOn) == null) {
 						data.put(updatedOn, date);
 					}
 				}
 			}
 			if(adapter.isTimeStamped()) {
 				if(isCreate) {
 					if(data.get(createdAt) == null) {
 						data.put(createdAt, date);
 					}
 					if(data.get(updatedAt) == null) {
 						data.put(updatedAt, date);
 					}
 				} else {
 					if(data.get(updatedAt) == null) {
 						data.put(updatedAt, date);
 					}
 				}
 			}
 		}
 		return data;
 	}
 	
 	@Override
 	public void destroy(Model... models) throws Exception {
 		DB db = getDB();
 		for(Model model : models) {
 			logger.debug("start doDestroy {}", model.asSimpleString());
 			DBCollection c = db.getCollection(tableName(model));
 			BasicDBObject dbo = new BasicDBObject("_id", model.getId(ObjectId.class));
 			c.remove(dbo);
 			logger.debug("end doDestroy");
 		}
 	}
 	
 	public void dropDatabase(String client) throws Exception {
 		Database db = getDatabase(client);
 		db.dropDatabase();
 	}
 
 	@Override
 	public <T extends Model> T find(Class<T> clazz, Map<String, Object> query, Object... values) throws Exception {
 		if(query == null) query = new HashMap<String, Object>(0);
 
 		DB db = getDB();
 		DBCollection c = db.getCollection(tableName(clazz));
 		
 		if(values.length > 0) {
 			int i = 0;
 			for(String key : query.keySet()) {
 				if("?".equals(query.get(key))) {
 					query.put(key, values[i++]);
 				}
 			}
 		}
 		
 		Object order = query.remove("$order");
 		Object limit = query.remove("$limit");
 		
 		DBCursor cursor = query.isEmpty() ? c.find() : c.find(new BasicDBObject(query));
 
 		if(order != null) {
 			cursor.sort(new BasicDBObject(coerce(order, Map.class)));
 		}
 		
 		// follow MySQL LIMIT convention: http://dev.mysql.com/doc/refman/5.1/en/select.html
 		//   LIMIT {[offset,] row_count}
 		if(limit instanceof Map) {
 			Entry<?,?> e = (Entry<?,?>) ((Map<?,?>) limit).entrySet().iterator().next();
 			cursor.skip(coerce(e.getKey(), int.class));
 		}
 		else if(limit instanceof String && ((String) limit).contains(",")) {
 			String[] sa = ((String) limit).split(",");
 			cursor.skip(coerce(sa[0], int.class));
 		}
 		
 		cursor.limit(1);
 
 		if(cursor.hasNext()) {
 			return getModel(clazz, cursor.next().toMap());
 		}
 		return null;
 	}
 	
 	@Override
 	public <T extends Model> T find(Class<T> clazz, String query, Object... values) throws Exception {
 		return find(clazz, toMap(query, values), new Object[0]);
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Map<String, Object> find(String collection, Object id) throws Exception {
 		DB db = getDB();
 		DBCollection c = db.getCollection(collection);
 		DBObject dbo = c.findOne(coerce(id, ObjectId.class));
 		return (dbo != null) ? dbo.toMap() : null;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public Map<String, Object> find(String collection, String jsonQuery, Object...values) throws Exception {
 		DB db = getDB();
 		DBCollection c = db.getCollection(collection);
 		DBObject dbo = new BasicDBObject(toMap(jsonQuery, values));
 		dbo = c.findOne(dbo);
 		return (dbo != null) ? dbo.toMap() : null;
 	}
 	
 	@Override
 	public <T extends Model> List<T> findAll(Class<T> clazz) throws Exception {
		return findAll(clazz, (Map<String, Object>) null, new Object[0]);
 	}
 	
 	@Override
 	public <T extends Model> List<T> findAll(Class<T> clazz, Map<String, Object> query, Object... values) throws Exception {
 		if(query == null) query = new HashMap<String, Object>(0);
 
 		DB db = getDB();
 		DBCollection c = db.getCollection(tableName(clazz));
 		
 		if(values.length > 0) {
 			int i = 0;
 			for(String key : query.keySet()) {
 				if("?".equals(query.get(key))) {
 					query.put(key, values[i++]);
 				}
 			}
 		}
 		
 		Object order = query.remove("$order");
 		Object limit = query.remove("$limit");
 		
 		DBCursor cursor = query.isEmpty() ? c.find() : c.find(new BasicDBObject(query));
 
 		if(order != null) {
 			cursor.sort(new BasicDBObject(coerce(order, Map.class)));
 		}
 		
 		if(limit != null) {
 			// follow MySQL LIMIT convention: http://dev.mysql.com/doc/refman/5.1/en/select.html
 			//   LIMIT {[offset,] row_count}
 			if(limit instanceof Map) {
 				Entry<?,?> e = (Entry<?,?>) ((Map<?,?>) limit).entrySet().iterator().next();
 				cursor.skip(coerce(e.getKey(), int.class)).limit(coerce(e.getValue(), int.class));
 			}
 			else if(limit instanceof String && ((String) limit).contains(",")) {
 				String[] sa = ((String) limit).split(",");
 				cursor.skip(coerce(sa[0], int.class)).limit(coerce(sa[1], int.class));
 			}
 			else {
 				cursor.limit(coerce(limit, int.class));
 			}
 		}
 
 		List<T> models = new ArrayList<T>();
 		while(cursor.hasNext()) {
 			models.add(getModel(clazz, cursor.next().toMap()));
 		}
 		return models;
 	}
 
 	@Override
 	public <T extends Model> List<T> findAll(Class<T> clazz, String query, Object... values) throws Exception {
 		return findAll(clazz, toMap(query, values), new Object[0]);
 	}
 
 	@Override
 	public <T extends Model> T findById(Class<T> clazz, Object id) throws Exception {
 		DB db = getDB();
 		DBCollection c = db.getCollection(tableName(clazz));
 		
 		DBObject o = c.findOne(coerce(id, ObjectId.class));
 		return (o != null) ? getModel(clazz, o.toMap()) : null;
 	}
 
 	@Override
 	public <T extends Model> T findById(Class<T> clazz, Object id, String include) throws Exception {
 		// TODO findById(Class<T> clazz, Object id, String include)
 		throw new UnsupportedOperationException("not yet implemented: MongoPersistService#findById(Class<T> clazz, Object id, String include)");
 	}
 
 	Bundle getBundle() {
 		return context.getBundle();
 	}
 
 	private Database getDatabase(String client) throws Exception {
 		if(databases != null) {
 			Database db = databases.get(client);
 			if(db == null) {
 				throw new Exception("database for " + client + " has not been setup");
 			}
 			return db;
 		} else {
 			throw new Exception("no connection pool has been setup");
 		}
 	}
 
 	public DB getDB() throws Exception {
 		return getDB(true);
 	}
 
 	private DB getDB(boolean create) throws Exception {
 		lock.readLock().lock();
 		try {
 			DB db = threadDB.get();
 			if(db == null && create) {
 				String client = threadClient.get();
 				if(client == null) {
 					throw new Exception(client + " is not a registered PersistClient");
 				}
 				Database database = getDatabase(client);
 				db = database.getDB();
 				threadDB.set(db);
 			}
 			return db;
 		} finally {
 			lock.readLock().unlock();
 		}
 	}
 
 	@Override
 	public ServiceInfo getInfo() {
 		return new MongoServiceInfo(this);
 	}
 
 	public String getMigrationServiceName() {
 		Object o = context.getBundle().getHeaders().get("Oobium-MigrationService");
 		if(o instanceof String) {
 			return (String) o;
 		}
 		return null;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	private <T> T getModel(Class<T> clazz, Map data) {
 		data.put("id", data.remove("_id"));
 		return coerce(data, clazz);
 	}
 
 	public Object insert(String collection, String json, Object...values) throws Exception {
 		DB db = getDB();
 		DBCollection c = db.getCollection(collection);
 		BasicDBObject dbo = new BasicDBObject(toMap(json, values));
 		c.insert(dbo);
 		return dbo.get("_id");
 	}
 	
 	@Override
 	public boolean isSessionOpen() {
 		return threadClient.get() != null;
 	}
 
 	@Override
 	public void openSession(String name) {
 		threadClient.set(name);
 		expireCache();
 	}
 
 	private void removeDatabase(String client) {
 		lock.writeLock().lock();
 		try {
 			Database db = databases.remove(client);
 			if(db != null) {
 				db.close();
 				logger.info("removed Database for {}", client);
 			}
 		} finally {
 			lock.writeLock().unlock();
 		}
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public void retrieve(Model... models) throws Exception {
 		DB db = getDB();
 		for(Model model : models) {
 			DBCollection c = db.getCollection(tableName(model));
 			DBObject o = c.findOne(model.getId(ObjectId.class));
 			if(o != null) {
 				model.putAll(o.toMap());
 			}
 		}
 	}
 	
 	@Override
 	public void retrieve(Model model, String hasMany) throws Exception {
 		// TODO retrieve(Model model, String hasMany)
 		throw new UnsupportedOperationException("not yet implemented: MongoPersistService#retrieve(Model model, String hasMany)");
 	}
 	
 	public void start(BundleContext context) throws Exception {
 		this.context = context;
 		
 		final String name = context.getBundle().getSymbolicName();
 
 		logger.setTag(name);
 		logger.info("PersistService starting");
 		
 		appTracker = new ServiceTracker(context, PersistClient.class.getName(), new ServiceTrackerCustomizer() {
 			@Override
 			public Object addingService(ServiceReference reference) {
 				String service = (String) reference.getProperty(PersistService.SERVICE);
 				if(name.equals(service)) {
 					String clientName = (String) reference.getProperty(PersistService.CLIENT);
 					if(clientName != null) {
 						Map<String, Object> properties = new HashMap<String, Object>();
 						for(String key : reference.getPropertyKeys()) {
 							properties.put(key, reference.getProperty(key));
 						}
 						addDatabase(clientName, properties);
 						return clientName;
 					}
 				}
 				return null;
 			}
 			@Override
 			public void modifiedService(ServiceReference reference, Object service) {
 				// nothing to do... ?
 			}
 			@Override
 			public void removedService(ServiceReference reference, Object service) {
 				if(service != null) {
 					removeDatabase((String) service);
 					MongoPersistService.this.context.ungetService(reference);
 				}
 			}
 		});
 		appTracker.open();
 
 		context.registerService(PersistService.class.getName(), this, Dictionary(PersistService.SERVICE, name));
 
 		logger.info("PersistService started");
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		appTracker.close();
 		appTracker = null;
 		this.context = null;
 		logger.info("PersistService stopped");
 		logger.setTag(null);
 	}
 
 	@Override
 	public void update(Model... models) throws Exception {
 		DB db = getDB();
 		for(Model model : models) {
 			logger.debug("start doUpdate: {}", model.asSimpleString());
 			DBCollection c = db.getCollection(tableName(model));
 			DBObject q = new BasicDBObject("_id", model.getId(ObjectId.class));
 			Map<String, Object> data = getData(model, false, false);
 			logger.trace(String.valueOf(data));
 			DBObject dbo = new BasicDBObject("$set", data);
 			c.update(q, dbo);
 			logger.debug("end doUpdate");
 		}
 	}
 
 }
