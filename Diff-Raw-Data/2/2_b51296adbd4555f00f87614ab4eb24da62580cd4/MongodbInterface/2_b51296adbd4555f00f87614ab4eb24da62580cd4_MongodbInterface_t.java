 /**
  **/
 package com.admob.rocksteady.util;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.Mongo;
 import com.mongodb.MongoException;
 
 import java.util.*;
 import java.io.*;
 import java.net.*;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class MongodbInterface {
 
     /**
      * The log instance.
      */
 
 
     private static final Logger LOG = LoggerFactory.getLogger(MongodbInterface.class);
 
     private Boolean enableSend = true;
 
     /**
      * The server name hosting Mongodb.
      */
     private final String mongodbServer;
 
     /**
      * The port Mongodb is listening on.
      */
     private final short mongodbPort;
 
     private final String user;
     private final char[] passwd;
     private final String dbname;
 
     private DBCollection coll;
 
     /**
      * The number of milliseconds between each stats update.
      */
     private int updateInterval = 5 * 60 * 1000; // 5 minute default
 
     /**
      * The timer that runs jobs at the <code>updateInterval</code>.
      */
     private Timer jobTimer;
 
 
     public MongodbInterface(String mongodbServer, short mongodbPort, String user, char[] passwd, String dbname) {
 	this.mongodbServer = mongodbServer;
 	this.mongodbPort = mongodbPort;
 	this.user = user;
 	this.passwd = passwd;
 	this.dbname = dbname;
     }
 
     public MongodbInterface(String mongodbServer, String dbname) {
 	this(mongodbServer, (short)27017, null, null, dbname);
     }
 
     public void setInterval(int seconds) {
 	this.updateInterval = seconds * 1000; // Convert to ms
     }
 
 
     /**
      * Forms the input string to Mongodb to record all the current data.
      *
      * @return A BasicDBObject that can be sent to the Mongodb collection.
      */
     public BasicDBObject mongodbObject(String retention, String app, String name, String colo, String cluster, String hostname, String value, String timestampstring) {
 
 	// Current UNIX timestamp.
 	long timestamp;
 	if (timestampstring == null) {
 	    timestamp = new Date().getTime() / 1000; // seconds since midnight Jan
 	} else {
 	    timestamp = Long.parseLong(timestampstring);
 	}
 	// 1, 1970
 	BasicDBObject obj = new BasicDBObject();
 	obj.put("retention", retention);
 	obj.put("app", app);
 	obj.put("name", name);
 	obj.put("colo", colo);
 	obj.put("cluster", cluster);
 	obj.put("value", value);
 	obj.put("timestamp", timestamp);
 	return obj;
     }
 
     private void connect() {
 	if (this.coll == null) {
 	    try {
 		Mongo mongo = new Mongo(this.mongodbServer, this.mongodbPort);
 		DB db = mongo.getDB( this.dbname );
 		if(!db.authenticate(user, passwd)) {
 		    LOG.error("wrong passwd or user");
 		}
		this.coll = db.getCollection("metrics");
 	    } catch (UnknownHostException e) {
 		LOG.error("cant connect Host " + e.toString());
 		this.coll = null;
 	    } catch (MongoException e) {
 		LOG.error("mongodb connect error " + e.toString());
 		this.coll = null;
 	    }
 	    LOG.trace("Connect Mongodb instance.");
 	}
     }
 
     /**
      * Sends a mongodb object to Mongodb.
      *
      * @param input is all the statistics to record in Mongodb's format. See
      *        <code>mongodbString</code>.
      */
     public void send(BasicDBObject input) {
 	LOG.trace("Going to record statistics with Mongodb.");
 	BasicDBObject data;
 
 	data = input;
 
 	if (enableSend) {
 	    // Write the data to the socket.
 	    try {
 		// Make sure we're connected to Mongodb.
 		connect();
 
 		// Write the data as sequence of 1-byte characters. Can't just do
 		// our.writeUTF because
 		// it prefixes UTF encoding characters that we don't want.
 		if (this.coll != null) {
 		    this.coll.insert(data);
 		}
 		LOG.trace("Recorded statistics with Mongodb.");
 	    } catch (MongoException e) {
 		LOG.error("Problem sending statistics to Mongodb." + e.toString());
 		if (this.coll != null) {
 		    this.coll = null;
 		}
 	    } catch (Exception e) {
 		LOG.error("error" + e.toString());
 		if (this.coll != null) {
 		    this.coll = null;
 		}
 	    }
 	}
     }
 
     public void setEnableSend(Boolean enableSend) {
 	this.enableSend = enableSend;
     }
 
     public Boolean getEnableSend() {
 	return enableSend;
     }
 
 }
