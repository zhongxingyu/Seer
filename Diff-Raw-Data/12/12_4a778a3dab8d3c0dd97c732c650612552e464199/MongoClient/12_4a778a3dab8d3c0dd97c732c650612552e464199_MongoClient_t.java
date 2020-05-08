 package org.sakaiproject.nakamura.lite.storage.mongo;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.apache.commons.lang.StringUtils;
 import org.sakaiproject.nakamura.api.lite.Repository;
 import org.sakaiproject.nakamura.api.lite.StorageClientException;
 import org.sakaiproject.nakamura.api.lite.StorageClientUtils;
 import org.sakaiproject.nakamura.api.lite.StorageConstants;
 import org.sakaiproject.nakamura.api.lite.accesscontrol.AccessDeniedException;
 import org.sakaiproject.nakamura.api.lite.content.Content;
 import org.sakaiproject.nakamura.lite.storage.spi.DirectCacheAccess;
 import org.sakaiproject.nakamura.lite.storage.spi.DisposableIterator;
 import org.sakaiproject.nakamura.lite.storage.spi.Disposer;
 import org.sakaiproject.nakamura.lite.storage.spi.RowHasher;
 import org.sakaiproject.nakamura.lite.storage.spi.SparseMapRow;
 import org.sakaiproject.nakamura.lite.storage.spi.SparseRow;
 import org.sakaiproject.nakamura.lite.storage.spi.StorageClient;
 import org.sakaiproject.nakamura.lite.storage.spi.StorageClientListener;
 import org.sakaiproject.nakamura.lite.storage.spi.content.StreamedContentHelper;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.ImmutableMap;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoException;
 
 /**
  *
  * A {@link StorageClient} for SpaeseMapContent that uses MongoDB as a backend.
  *
  * For the most part the concepts and objects in SMC and MongoDB are very similar.
  *
  * Sparse Map Content => MongoDB
  *
  * Column Family      => Collection
  * Content Object     => Document
  * Content Property   => Document Field
  *
  * Both have one method for saving data. SMC is insert, MongoDB is update.
  * We use the MongoDB update with the upsert flag set to true so these methods
  * are equivalent abd we'll just use the term upsert.
  *
  * There are some subtle differences do be aware of though.
  *
  * 1.Both SMC and MongoDB think they own a property on each object named _id.
  *
  * This is not the case. In reality MongoDB owns it. This driver takes pain to
  * rewrite the _id property on upsert to another key. This way Mongo manages
  * the _id property in the underlying storage and we don't step on its toes too much.
  *
  * 2. MongoDB treats .'s in field names as nested documents
  *
  * If you try to store a field on x with the name a.b and value 1 in MongoDB,
  * you will actually store:
  * x.a = { b : 1 }
  *
  * This driver changes .'s in field names to some obscure constant string before it
  * upserts. When reading data out of Mongo we change it back to . so noone knows our
  * little secret. shh. This is a bit of a hack.
  *
  */
 public class MongoClient implements StorageClient, RowHasher {
 
 	private static final Logger log = LoggerFactory.getLogger(MongoClient.class);
 
 	// This belongs to MongoDB. Never set this field.
 	public static final String MONGO_INTERNAL_ID_FIELD = "_id";
 
 	// This primary id as far as SMC is concerned 
 	// unless there iss an entry in alternatKeys for this columnFamily
 	public static final String MONGO_INTERNAL_SPARSE_UUID_FIELD = Repository.SYSTEM_PROP_PREFIX + "smcid";
 
 	// Connection to MongoDB
 	private DB mongodb;
 
 	// SMC may use something other than _id as its key
 	private Map<String,String> alternateKeys;
 
 	// Reads and Writes file content to a filesystem
 	private StreamedContentHelper streamedContentHelper;
 
 	// Throws events for the migration framework
 	private StorageClientListener storageClientListener;
 
 	private Map<String, Object> props;
 
 	@SuppressWarnings("unchecked")
 	public MongoClient(DB mongodb, Map<String,Object> props) {
 		this.mongodb = mongodb;
 		this.props = props;
 
		String user = StorageClientUtils.getSetting(props.get(MongoClientPool.PROP_MONGO_USER),  null); 
		String password = StorageClientUtils.getSetting(props.get(MongoClientPool.PROP_MONGO_USER), null);
 
		if (user != null && password != null && !this.mongodb.isAuthenticated()){
			if (!this.mongodb.authenticate(user, password.toCharArray())){
 				throw new MongoException("Unable to authenticate");
 			}
 		}
 
 		this.alternateKeys = (Map<String,String>)props.get(MongoClientPool.PROP_ALT_KEYS);
 		this.streamedContentHelper = new GridFSContentHelper(mongodb, this, props);
 		this.mongodb.requestStart();
 	}
 
 	public Map<String, Object> get(String keySpace, String columnFamily,
 			String key) throws StorageClientException {
 		columnFamily = columnFamily.toLowerCase();
 		log.debug("get {}:{}:{}", new Object[]{keySpace, columnFamily, key});
 		DBCollection collection = mongodb.getCollection(columnFamily);
 
 		DBObject query = null;
 		if (alternateKeys.containsKey(columnFamily)) {
 			String altKey = alternateKeys.get(columnFamily);
 			query = new BasicDBObject(altKey, key);
 		}
 		else {
 			query = new BasicDBObject(MONGO_INTERNAL_SPARSE_UUID_FIELD, key);
 		}
 		DBCursor cursor = collection.find(query);
 
 		// Check the result and return it.
 		Map<String,Object> result = null;
 		if (cursor.size() == 1){
 			result = MongoUtils.convertDBObjectToMap(cursor.next());
 		}
 		if (result == null){
 			result = new HashMap<String, Object>();
 		}
 		return result;
 	}
 
 	public void insert(String keySpace, String columnFamily, String key,
 			Map<String, Object> values, boolean probablyNew)
 	throws StorageClientException {
 		columnFamily = columnFamily.toLowerCase();
 		HashMap<String,Object> mutableValues = new HashMap<String,Object>(values);
 
 		// rewrite _id => MongoClient.MONGO_INTERNAL_SPARSE_UUID_FIELD
 		if (mutableValues.containsKey(MongoClient.MONGO_INTERNAL_ID_FIELD)){
 			mutableValues.put(MongoClient.MONGO_INTERNAL_SPARSE_UUID_FIELD,
 					mutableValues.get(MongoClient.MONGO_INTERNAL_ID_FIELD));
 			mutableValues.remove(MongoClient.MONGO_INTERNAL_ID_FIELD);
 		}
 
 		// Set the parent path hash if this is a piece of content that is not a root (roots are orphans)
 		if (mutableValues.keySet().contains(Content.PATH_FIELD) && !StorageClientUtils.isRoot(key)) {
 			mutableValues.put(Content.PARENT_HASH_FIELD,
 					rowHash(keySpace, columnFamily, StorageClientUtils.getParentObjectPath(key)));
 		}
 
 		DBCollection collection = mongodb.getCollection(columnFamily);
 
 		// The document to update identified its _smcid or _aclKey
 		DBObject query = null;
 
 		if (alternateKeys.containsKey(columnFamily)) {
 			String altKey = alternateKeys.get(columnFamily);
 			query = new BasicDBObject(altKey, key);
 			mutableValues.put(altKey, key);
 		}
 		else {
 			query = new BasicDBObject(MONGO_INTERNAL_SPARSE_UUID_FIELD, key);
 			mutableValues.put(MONGO_INTERNAL_SPARSE_UUID_FIELD, key);
 		}
 
 		// Converts the insert into a bunch of set, unset Mongo operations
 		DBObject insert = MongoUtils.cleanPropertiesForInsert(mutableValues);
 
 		Map<String,Object> mapBefore = this.get(keySpace, columnFamily, key);
 		if ( storageClientListener != null ) {
 			storageClientListener.before(keySpace, columnFamily, key, mapBefore);
 		}
 
 		// Update or insert a single document.
 		collection.update(query, insert, true, false);
 		log.debug("insert {}:{}:{} => {}", new Object[] {keySpace, columnFamily, key, insert.toString()});
 
 		if ( storageClientListener != null ) {
 			storageClientListener.after(keySpace, columnFamily, key, mutableValues);
 		}
 	}
 
 	public void remove(String keySpace, String columnFamily, String key)
 	throws StorageClientException {
 		columnFamily = columnFamily.toLowerCase();
 		DBCollection collection = mongodb.getCollection(columnFamily);
 		
 		// Soft delete content
 		if (columnFamily.equals((String)props.get(MongoClientPool.PROP_CONTENT_COLLECTION))){
 			insert(keySpace, columnFamily, key, ImmutableMap.of(DELETED_FIELD, (Object)TRUE), false);
 		}
 		else {
 			collection.remove(new BasicDBObject(MONGO_INTERNAL_SPARSE_UUID_FIELD, key));
 		}
 		log.debug("remove {}:{}:{}", new Object[]{keySpace, columnFamily, key});
 	}
 
 	public DisposableIterator<SparseRow> listAll(String keySpace,
 			String columnFamily) throws StorageClientException {
 		columnFamily = columnFamily.toLowerCase();
 		DBCollection collection = mongodb.getCollection(columnFamily);
 		log.debug("listAll {}:{}", new Object[]{keySpace, columnFamily});
 
 		final DBCursor cursor = collection.find();
 		final Iterator<DBObject> itr = cursor.iterator();
 
 		return new DisposableIterator<SparseRow>() {
 
 			public boolean hasNext() {
 				return itr.hasNext();
 			}
 
 			public SparseRow next() {
 				DBObject next = itr.next();
 				return new SparseMapRow((String)next.get(MONGO_INTERNAL_SPARSE_UUID_FIELD),
 						MongoUtils.convertDBObjectToMap(next));
 			}
 
 			public void close() {
 				cursor.close();
 				mongodb.requestDone();
 			}
 			public void remove() { }
 			public void setDisposer(Disposer disposer) { }
 		};
 	}
 
 	public long allCount(String keySpace, String columnFamily)
 			throws StorageClientException {
 		columnFamily = columnFamily.toLowerCase();
 		log.debug("allCount {}:{}", new Object[]{keySpace, columnFamily});
 		DBCollection collection = mongodb.getCollection(columnFamily);
 		return collection.count();
 	}
 
 	public InputStream streamBodyOut(String keySpace, String columnFamily,
 			String contentId, String contentBlockId, String streamId,
 			Map<String, Object> content) throws StorageClientException,
 			AccessDeniedException, IOException {
 		columnFamily = columnFamily.toLowerCase();
 		return streamedContentHelper.readBody(keySpace, columnFamily, contentBlockId, streamId, content);
 	}
 
 	public Map<String, Object> streamBodyIn(String keySpace,
 			String columnFamily, String contentId, String contentBlockId,
 			String streamId, Map<String, Object> content, InputStream in)
 			throws StorageClientException, AccessDeniedException, IOException {
 		columnFamily = columnFamily.toLowerCase();
 		Map<String,Object> meta = streamedContentHelper.writeBody(keySpace, columnFamily, contentId, contentBlockId, streamId, content, in);
 		return meta;
 	}
 
 
 	@SuppressWarnings("unchecked")
 	public DisposableIterator<Map<String, Object>> find(String keySpace,
 			String columnFamily, Map<String, Object> properties, DirectCacheAccess cachingManager)
 			throws StorageClientException {
 
 		columnFamily = columnFamily.toLowerCase();
 		DBCollection collection = mongodb.getCollection(columnFamily);
 		BasicDBObject query = new BasicDBObject();
 
 		for (Entry<String, Object> e : properties.entrySet()){
 			Object val = e.getValue();
 			String key = MongoUtils.escapeFieldName(e.getKey());
 
 			if (val instanceof Map){
 				// This is how it comes from sparse
 				// properties = { "orset0" : { "fieldName" : [ "searchVal0", "searchVal1" ] } }
 				Map<String,Object> multiValQueryMap = (Map<String, Object>) val;
 				String field = multiValQueryMap.keySet().iterator().next();
 				List<String> searchValues = (List<String>)multiValQueryMap.get(field);
 
 				// This is what mongo expects
 				// mongoQuery = { "$or" : [ BasicDBObject("field", "val0"),
 				//                             BasicDBObject("field", "val1") ] }
 				ArrayList<BasicDBObject> mongoQuery = new ArrayList<BasicDBObject>();
 				for(String searchVal: searchValues){
 					mongoQuery.add(new BasicDBObject(field, searchVal));
 				}
 
 				if (key.startsWith("orset")){
 					// Remove the original query and add a Mongo OR query.
 					query.remove(key);
 					query.put(Operators.OR, mongoQuery);
 				}
 			}
 			else if (val instanceof List){
 				// What mongo expects
 				// { "fieldName" : { "$all" : [ "valueX", "valueY" ] } }
 				List<String> valList = (List<String>)val;
 				BasicDBObject mongoSet = new BasicDBObject();
 				mongoSet.put(Operators.ALL, valList);
 				// overwrite the original value of key
 				query.put(key, mongoSet);
 			}
 			else {
 				query.put(key, val);
 			}
 		}
 
 		/*
 		 * Support the custom count queries.
 		 * TODO: A better way to define custom queries dynamically.
 		 * Maybe a list of JSON queries and use mongodo.eval(...)?
 		 */
 		String customStatementSet = query.getString(StorageConstants.CUSTOM_STATEMENT_SET);
 		if (customStatementSet != null && "countestimate".equals(customStatementSet)){
 			query.remove(StorageConstants.CUSTOM_STATEMENT_SET);
 			query.remove(StorageConstants.RAWRESULTS);
 			final int count = (int)collection.count(query);
 
 			return new DisposableIterator<Map<String,Object>>() {
 				private boolean hasNext = true;
 
 				// Return true only once.
 				public boolean hasNext() {
 					if (hasNext){
 						hasNext = false;
 						return true;
 					}
 					return hasNext;
 				}
 
 				public Map<String, Object> next() {
 					return ImmutableMap.of("1", (Object)Integer.valueOf(count));
 				}
 				public void remove() { }
 				public void close() { mongodb.requestDone(); }
 				public void setDisposer(Disposer disposer) { }
 			};
 		}
 		else {
 			// See if we need to sort
 			final DBCursor cursor = collection.find(query);
 			if (properties.containsKey(StorageConstants.SORT)){
 				query.remove(StorageConstants.SORT);
 				cursor.sort(new BasicDBObject((String)properties.get(StorageConstants.SORT), 1));
 			}
 			final Iterator<DBObject> itr = cursor.iterator();
 
 			// Iterator with the results.
 			return new DisposableIterator<Map<String,Object>>() {
 				public boolean hasNext() {
 					return itr.hasNext();
 				}
 				public Map<String, Object> next() {
 					return MongoUtils.convertDBObjectToMap(itr.next());
 				}
 				public void close() {
 					cursor.close();
 					mongodb.requestDone();
 				}
 				public void remove() { }
 				public void setDisposer(Disposer disposer) { }
 			};
 		}
 	}
 
 	public void close() {
 		log.debug("Closed");
 		this.mongodb.requestDone();
 	}
 
 	public DisposableIterator<Map<String, Object>> listChildren(
 			String keySpace, String columnFamily, String key, DirectCacheAccess cachingManager)
 			throws StorageClientException {
 		columnFamily = columnFamily.toLowerCase();
 		// Hash the object we're considering
 		String hash = rowHash(keySpace, columnFamily, key);
 		log.debug("Finding {}:{}:{} as {} ", new Object[]{keySpace,columnFamily, key, hash});
 		// Issue a query for anyone who lists that hash as their parent.
 		return find(keySpace, columnFamily, ImmutableMap.of(Content.PARENT_HASH_FIELD, (Object)hash), cachingManager);
 	}
 
 	public boolean hasBody(Map<String, Object> content, String streamId) {
 		// Is there a binary stream of data for this object with this streamId?
 		return streamedContentHelper.hasStream(content, streamId);
 	}
 
 	/**
 	 * Generate the row id hashes needed to maintain ids and relationships in sparse.
 	 */
 	public String rowHash(String keySpace, String columnFamily, String key)
 	throws StorageClientException {
 		columnFamily = columnFamily.toLowerCase();
 		MessageDigest hasher;
 		try {
 			hasher = MessageDigest.getInstance("SHA1");
 		} catch (NoSuchAlgorithmException e1) {
 			throw new StorageClientException("Unable to get hash algorithm " + e1.getMessage(), e1);
 		}
 		String keystring = keySpace + ":" + columnFamily + ":" + key;
 		byte[] ridkey;
 		try {
 			ridkey = keystring.getBytes("UTF8");
 		} catch (UnsupportedEncodingException e) {
 			ridkey = keystring.getBytes();
 		}
 		String hash = StorageClientUtils.encode(hasher.digest(ridkey));
 		log.debug("rowHash: {}:{}:{} => {}", new Object[]{keySpace, columnFamily, key, hash});
 		return hash;
 	}
 
 	public void setStorageClientListener(
 			StorageClientListener storageClientListener) {
 		this.storageClientListener = storageClientListener;
 	}
 }
