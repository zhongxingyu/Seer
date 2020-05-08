 package org.robot.gtf.service.impl.mongodb;
 
 import java.net.ConnectException;
 import java.net.UnknownHostException;
 import com.mongodb.CommandResult;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.Mongo;
 
 /**
  * This class provides basic methods for establishing a connection to MongoDB 
  * (with or without authorization) and returning collections.
  * @author thomas.jaspers
  */
 public class MongoHandler {
 
 	private DB mongoDb;
 	private boolean isAuthenticated = false;
 
 	/**
 	 * Creates a connection to a MongoDB that does not require authentification.
 	 * @param hostName Host running MongoDB
 	 * @param port Port of the MongoDB instance
 	 * @param dbName Name of the database
 	 * @throws UnknownHostException Problem during connection
 	 * @throws ConnectException Problem during connection
 	 */
 	public void connect(String hostName, int port, String dbName) 
 			throws UnknownHostException, ConnectException {
 		Mongo mongo = new Mongo(hostName, port);
 		mongoDb = mongo.getDB(dbName);
 		
 		CommandResult lastError = mongoDb.getLastError();
 		if (!lastError.ok()) {
 			throw new ConnectException("Error connecting to MongoDB: " + lastError.getErrorMessage());
 		}
 		
 		isAuthenticated = true;
 	}
 
 	/**
 	 * Creates a connection to a MongoDB that does require authentification.
 	 * @param hostName Host running MongoDB
 	 * @param port Port of the MongoDB instance
 	 * @param dbName Name of the database
 	 * @param username Username for authentification
 	 * @param password Password for authetification
 	 * @return True if authentification succeeded, otherwise false
 	 * @throws UnknownHostException Problem during connection
 	 * @throws ConnectException Problem during connection
 	 */
 	public boolean connect(String hostName, int port, String dbName, String username, String password) 
 			throws UnknownHostException, ConnectException {
 		Mongo mongo = new Mongo(hostName, port);
 		mongoDb = mongo.getDB(dbName);
 		
 		CommandResult lastError = mongoDb.getLastError();
 		if (!lastError.ok()) {
 			throw new ConnectException("Error connecting to MongoDB: " + lastError.getErrorMessage());
 		}
 		
 		isAuthenticated = mongoDb.authenticate(username, password.toCharArray());
 		return isAuthenticated;
 	}
 
 	/**
 	 * Return the MongoDB collection with the specified name. 
 	 * @param name Name of a MongoDB collection
 	 * @return Collection object
 	 */
 	public DBCollection getCollection(String name) {
 		return mongoDb.getCollection(name);
 	}
 }
