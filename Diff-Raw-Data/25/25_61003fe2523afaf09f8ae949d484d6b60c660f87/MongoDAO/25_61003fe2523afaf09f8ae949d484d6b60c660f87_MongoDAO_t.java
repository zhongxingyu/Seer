 package org.irishhombre.ds.mongo;
 
 import org.irishhombre.ds.mongo.MongoDbConnectionManager;
 import com.mongodb.DB;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCursor;
import com.mongodb.DBCollection;
 
 public class MongoDAO {
 
    public MongoDAO() {
 	    
    }
 
    private DBCollection getCollection(String collectionName) {
 	DB db = MongoDbConnectionManager.getInstance().getDB("ffrankings");
 	return db.getCollection(collectionName);
    }
 
    public DBCursor queryCollection(String collectionName, BasicDBObject query) {
 	return getCollection(collectionName).find(query);
    }
 
    public void updateCollection(String collectionName, BasicDBObject document) {
	getCollection(collectionName).save(document);
    }
 
    public void insertInCollection(String collectionName, BasicDBObject document) {
 	getCollection(collectionName).insert(document);
    }
 
 }
