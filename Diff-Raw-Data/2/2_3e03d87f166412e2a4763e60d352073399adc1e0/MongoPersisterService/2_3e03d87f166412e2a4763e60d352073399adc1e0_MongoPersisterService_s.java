 package org.pasut.persister;
 
 import java.util.List;
 import java.util.UUID;
 
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 
 public class MongoPersisterService implements PersisterService {
 	private final Mongo mongo;
 	private final DB db;
 	private final GsonMongoMapper mapper = new GsonMongoMapper();
 	private final ExampleFactory factory = new ExampleFactory();
 	
 	public MongoPersisterService(String dbName, String host, int port){
 		try {
 			mongo = new Mongo(host,port);
 			db = mongo.getDB(dbName);	
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 	}
 	public <T> T insert(T object) {
 		DBCollection collection = db.getCollection(getCollectionName(object.getClass()));
 		DBObject dbObject = mapper.toDbObject(object);
 		if(dbObject.get("_id")==null){
 			dbObject.put("_id", UUID.randomUUID().toString());
 			collection.insert(dbObject);
 		}
 		else{
 			collection.save(dbObject);
 		}
 		@SuppressWarnings("unchecked")
 		T newObject = (T)mapper.fromDbObject(dbObject, object.getClass());
 		return newObject;
 	}
 
 	public <T> List<T> find(Class<T> clazz) {
 		DBCollection collection = db.getCollection(getCollectionName(clazz));
 		List<DBObject> list = collection.find().toArray();
 		return mapper.fromDbObject(list, clazz);
 	}
 
 	public <T> List<T> find(T example, String[] properties) {
 		DBCollection collection = db.getCollection(getCollectionName(example.getClass()));
 		List<DBObject> list = collection.find(factory.createExample(example,properties)).toArray();
 
 		@SuppressWarnings("unchecked")
 		List<T> result = (List<T>)mapper.fromDbObject(list, example.getClass());
 		return result;
 	}
 
 	public <T> T update(T object) {
 		DBCollection collection = db.getCollection(getCollectionName(object.getClass()));
 		DBObject dbObject = mapper.toDbObject(object);
 		collection.save(dbObject);
 		@SuppressWarnings("unchecked")
 		T newObject = (T)mapper.fromDbObject(dbObject, object.getClass());
 		return newObject;
 	}
 	
 	private <T> String getCollectionName(Class<T> clazz){
 		Entity annotation = clazz.getAnnotation(Entity.class);
 		if(annotation==null) throw new PersistenceException(clazz);
 		return annotation.value();
 	}
 	public <T> long count(Class<T> clazz) {
 		DBCollection collection = db.getCollection(getCollectionName(clazz));
 		return collection.count();
 	}
 	public <T> long count(T example, String[] properties) {
 		DBCollection collection = db.getCollection(getCollectionName(example.getClass()));
 		return collection.count(factory.createExample(example,properties));
 	}
 	@SuppressWarnings("unchecked")
 	public <T> T findOne(T example, String[] properties) {
 		DBCollection collection = db.getCollection(getCollectionName(example.getClass()));
 		DBObject dbObject = collection.findOne(factory.createExample(example,properties));

 		return (T) mapper.fromDbObject(dbObject, example.getClass());
 	}
 
 }
