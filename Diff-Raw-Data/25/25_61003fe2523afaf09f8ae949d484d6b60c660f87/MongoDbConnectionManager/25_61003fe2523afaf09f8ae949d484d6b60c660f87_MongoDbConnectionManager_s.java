 package org.irishhombre.ds.mongo;
 
 import com.mongodb.Mongo;
 
 public class MongoDbConnectionManager {
   public static final MongoDbConnectionManager instance = new MongoDbConnectionManager();
 
   private MongoDbConnectionManager() {
 	try {
 	  //TODO: Change this to a JVM property
	  Mongo m = new Mongo("localhost", 27017);
 	} catch (Exception e) {
 	  throw new RuntimeException("Failed to initialize mongo....");
 	}
   }
 
   public static MongoDbConnectionManager getInstance() {
 	return instance;
   }
 }
 
