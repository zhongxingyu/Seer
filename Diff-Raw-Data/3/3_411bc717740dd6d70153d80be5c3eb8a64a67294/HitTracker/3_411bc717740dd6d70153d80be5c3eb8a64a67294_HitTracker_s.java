 package org.cloudydemo;
 
 import java.net.UnknownHostException;
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.annotation.PostConstruct;
 import javax.ejb.Schedule;
 import javax.ejb.Singleton;
 import javax.ejb.Startup;
 import javax.ejb.Timeout;
 
 import org.bson.types.ObjectId;
 import org.cloudydemo.model.Application;
 import org.cloudydemo.model.Gear;
 import org.cloudydemo.model.Hit;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 
 @Startup
 @Singleton
 public class HitTracker {
 	private Mongo mongo;
 	private DB mongoDB;
 
 	private static final Logger LOGGER = Logger.getLogger(HitTracker.class
 			.getName());
 
 	// Cached number of hits
 	private int hits;
 
 	// The gear id of the instance this singleton is running on
 	private String gearId;
 
 	// The application name
 	private String appName;
 
 	private final String COLLECTION = "hitTracker";
 
 	@PostConstruct
 	void initialize() {
 		String host = System.getenv("OPENSHIFT_MONGODB_DB_HOST");
 		String user = System.getenv("OPENSHIFT_MONGODB_DB_USERNAME");
 		String password = System.getenv("OPENSHIFT_MONGODB_DB_PASSWORD");
 		int port = Integer.decode(System.getenv("OPENSHIFT_MONGODB_DB_PORT"));
 		gearId = System.getenv("OPENSHIFT_GEAR_UUID");
 		appName = System.getenv("OPENSHIFT_APP_NAME");
 
 		LOGGER.fine("Connecting with host = " + host + " / port = " + port);
 
 		try {
 			mongo = new Mongo(host, port);
 		} catch (UnknownHostException e) {
 			e.printStackTrace();
 		}
 		mongoDB = mongo.getDB(System.getenv("OPENSHIFT_APP_NAME"));
 		if (user != null && password != null) {
 			if (mongoDB.authenticate(user, password.toCharArray()) == false) {
 				throw new RuntimeException("Mongo authentication failed");
 			}
 		} else {
 			LOGGER.warning("No username / password given so not authenticating with Mongo");
 		}
 	}
 
 	public Application displayHitsSince(long time) {
 		LOGGER.fine("Displaying hits");
 
 		Application app = new Application(appName);
 
 		try {
 			mongoDB.requestStart();
 			DBCollection coll = mongoDB.getCollection(COLLECTION);
 
 			BasicDBObject query = new BasicDBObject("time", new BasicDBObject(
 					"$gt", time));
 			DBCursor cur = coll.find(query);
 
 			try {
 				while (cur.hasNext()) {
 					DBObject result = cur.next();
 
 					String gearId = (String) result.get("gear");
 
 					// Get or create the gear for the application
 					Gear gear = new Gear(gearId);
 					if (!app.getChildren().contains(gear)) {
 						app.getChildren().add(gear);
 					} else {
 						int index = app.getChildren().indexOf(gear);
 						gear = app.getChildren().get(index);
 					}
 
 					String id = ((ObjectId) result.get("_id")).toString();
 					Date timestamp = new Date(
 							((Long) result.get("time")).longValue());
 					Integer hits = (Integer) result.get("hits");
 
 					// Add the hits and timestamp to the gear
 					gear.getChildren().add(
 							new Hit(id, timestamp, hits.intValue()));
 				}
 			} finally {
 				cur.close();
 			}
 		} finally {
 			mongoDB.requestDone();
 		}
 
 		LOGGER.fine("Application = " + app);
 
 		return app;
 	}
 
 	/*
 	 * Persist using the Timer service every second
 	 */
 	@Schedule(hour = "*", minute = "*", second = "*", persistent = false)
 	public void persist() {
 		if (hits > 0) {
 			LOGGER.fine("Persisting " + hits + " to Mongo for gear " + gearId);
 
 			try {
 				mongoDB.requestStart();
 
 				DBCollection coll = mongoDB.getCollection(COLLECTION);
 
 				BasicDBObject doc = new BasicDBObject();
 				doc.put("gear", gearId);
 				doc.put("hits", hits);
 				doc.put("time", System.currentTimeMillis());
 
 				coll.insert(doc);
 				
 				// Reset the hit counter
 				hits = 0;
 			} finally {
 				mongoDB.requestDone();
 			}
 		}
 	}
 	
 	@Timeout
 	public void timed() {
 		// Just created to handle timeouts on the schedule calls
 		// which can be ignored.
 	}
 
 	public void addHit() {
 		hits++;
 	}
 }
