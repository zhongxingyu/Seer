 package com.gigaspaces.persistency;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.gigaspaces.document.SpaceDocument;
 import com.gigaspaces.metadata.SpaceTypeDescriptor;
 import com.gigaspaces.persistency.metadata.DefaultPojoToMongoMapper;
 import com.gigaspaces.persistency.metadata.Mapper;
 import com.gigaspaces.sync.DataSyncOperation;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 import com.mongodb.ServerAddress;
 
 /**
  * @author Shadi Massalha
  * 
  */
 public class MongoClientPool {
 
 	private static final Log logger = LogFactory.getLog(MongoClientPool.class);
 
 	private MongoClient client;
 	private String dbName;
 	private final Object batchSynchLock = new Object();
 
 	private static final Map<SpaceTypeDescriptor, Mapper<SpaceDocument, DBObject>> _mappingCache = new HashMap<SpaceTypeDescriptor, Mapper<SpaceDocument, DBObject>>();
 	private static final Object synch = new Object();
 
 	public MongoClientPool(ServerAddress host, String db) {
 		this.client = new MongoClient(host);
 		this.dbName = db;
 	}
 
 	public synchronized DB checkOut() {
 		DB db = client.getDB(dbName);
 		return db;
 
 	}
 
 	public void performBatch(DataSyncOperation[] dataSyncOperations) {
 
		logger.trace("MongoClientPool.performBatch()");

 		final DB db = checkOut();
 
 		synchronized (batchSynchLock) {
 			int len = dataSyncOperations.length;
 
 			for (int i = 0; i < len; i++) {
 				DataSyncOperation dataSyncOperation = dataSyncOperations[i];
 
 				if (!dataSyncOperation.supportsDataAsDocument())
 					continue;
 
 				SpaceDocument spaceDoc = dataSyncOperation.getDataAsDocument();
 				SpaceTypeDescriptor spaceTypeDescriptor = dataSyncOperation
 						.getTypeDescriptor();
 
 				Mapper<SpaceDocument, DBObject> mapper = getMapper(spaceTypeDescriptor);
 
 				DBObject obj = mapper.maps(spaceDoc);
 
 				DBCollection col = db.getCollection(spaceTypeDescriptor
 						.getTypeSimpleName());
 
 				switch (dataSyncOperation.getDataSyncOperationType()) {
 				case WRITE:
 				case UPDATE:
 				case PARTIAL_UPDATE:
 					col.save(obj);
 					break;
 				case REMOVE:
 					col.remove(obj);
					break;				
				default:
 					throw new IllegalStateException(
 							"Unsupported data sync operation type: "
 									+ dataSyncOperation
 											.getDataSyncOperationType());

 				}
 
 			}
 		}
 	}
 
 	protected Mapper<SpaceDocument, DBObject> getMapper(
 			SpaceTypeDescriptor spaceTypeDescriptor) {
 
 		Mapper<SpaceDocument, DBObject> mapper = null;
 
 		synchronized (synch) {
 			mapper = _mappingCache.get(spaceTypeDescriptor);
 
 			if (mapper == null) {
 				mapper = new DefaultPojoToMongoMapper(spaceTypeDescriptor);
 
 				_mappingCache.put(spaceTypeDescriptor, mapper);
 			}
 
 		}
 
 		return mapper;
 	}
 
	public synchronized void close() {
		client.close();
 	}
 }
