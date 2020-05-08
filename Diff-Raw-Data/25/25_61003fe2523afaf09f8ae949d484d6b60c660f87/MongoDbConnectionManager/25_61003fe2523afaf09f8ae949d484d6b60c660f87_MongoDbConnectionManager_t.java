 package org.irishhombre.ds.mongo;
 
 import com.mongodb.Mongo;
import com.mongodb.DB;
 
 public class MongoDbConnectionManager {
   public static final MongoDbConnectionManager instance = new MongoDbConnectionManager();
  private Mongo m;
 
   private MongoDbConnectionManager() {
 	try {
 	  //TODO: Change this to a JVM property
	  m = new Mongo("localhost", 27017);
 	} catch (Exception e) {
 	  throw new RuntimeException("Failed to initialize mongo....");
 	}
   }
 
   public static MongoDbConnectionManager getInstance() {
 	return instance;
   }

  private Mongo getMongo() {
	return m;
  }

  public DB getDB(String db) {
	return getInstance().getMongo().getDB(db);
  }
  
 }
 
