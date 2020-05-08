 package main.java.Client;
 
 import java.math.BigInteger;
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import com.mongodb.Mongo;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.DBCursor;
 
 public class DataController {
 
     /*
      *  Class variables
      */
 
     static Mongo m;
     static DB db;
 
     /*
      *  Initializer
      *
      *  Opens a connection to the database for the given user
     ------------------------------------------------------------------------ 
      */
 
     public static void initialize(String username, String password) throws Exception {
         // Initialize database connection
         m = new Mongo("localhost", 27017);
 
         // Get database for specified user
         db = m.getDB("abbc_" + username);
         
         // Authenticate user
         boolean auth = db.authenticate(username, password.toCharArray());
 
         if (!auth) {
             throw new Exception("Unable to authenticate database connection");
         }
     }
 
     /*
      *  Storing
      ************************************************************************
      */
 
     /*
      *  Client
      -----------------------------------------------------------------------
      */
 
     public static boolean registerClient( String username,
                             String password,
                             String certificate,
                             BigInteger privateKey) {
 
         // Get the client collection
     	DBCollection coll = db.getCollection("client");
 
 		// Reset registered client if already exists
 		coll.drop();
 
         // Build client data
     	BasicDBObject clientData = new BasicDBObject();
         clientData.put("username", username);
         clientData.put("password", password);
         clientData.put("certificate", certificate);
         clientData.put("privateKey", privateKey.toString());
 
         // Insert the client data document into the collection
         coll.insert(clientData);
 
         return true;
     }
 
     /*
      *  Friends
      -----------------------------------------------------------------------
      */
 
    public static boolean addFriend(  String realname,
                         String username,
                         JSONObject cert) throws JSONException {
     	DBCollection coll = db.getCollection("friends");			// Get the friends collection
     	BasicDBObject friend = new BasicDBObject();
 
         friend.put("realname", realname);							// Input the info for the friend
         friend.put("username", username);
         BasicDBObject certificate = new BasicDBObject();			// Copy over the certificate info
         
         certificate.put("version", cert.get("version"));
         certificate.put("realname", cert.get("realname"));
         certificate.put("username", cert.get("username"));
         certificate.put("email", cert.get("email"));
         certificate.put("not_before", cert.get("not_before"));
         certificate.put("not_after", cert.get("not_after")); 
         certificate.put("notes", cert.get("notes"));
         certificate.put("modulus", cert.get("modulus"));			
         certificate.put("public_exponent", cert.get("public_exponent"));
         
         friend.put("certificate", certificate);
         
         coll.insert(friend);										 // Insert the friend document into the collection
 
         return true;
     }
 
     /*
      *  Wall Posts
      -----------------------------------------------------------------------
      */
 
     public static boolean addWallPost( String username,
                         String message) {
 
         // Get the wallposts collection
     	DBCollection coll = db.getCollection("wallposts");
 
         // Build wallpost
     	BasicDBObject wallpost = new BasicDBObject();
         wallpost.put("username", username);
         wallpost.put("message", message);
 
         // Insert the wallpost document into the collection
         coll.insert(wallpost);
 
         return true;
     }
 
     /*
      *  Retrieving
      ************************************************************************
      */
 
     /*
      *  Client
      -----------------------------------------------------------------------
      */
 
     public static BigInteger getPrivateKey() {
 		
     	DBCollection coll = db.getCollection("client");
     	
     	BasicDBObject query = new BasicDBObject();
         DBObject client = coll.findOne();
         
         if(client != null) {
     		return new BigInteger(client.get("privateKey").toString());
 		}
 		else {
 			return null;
 		}			
 
     }
 
     public static String getCertificate() {
 
     	DBCollection coll = db.getCollection("client");
     	
     	BasicDBObject query = new BasicDBObject();
         DBObject client = coll.findOne();
         
         if(client != null) {
     		return client.get("certificate").toString();
 		}
 		else {
 			return null;
 		}			
 
     }
 
     public static String getUsername() {
         
     	DBCollection coll = db.getCollection("client");
     	
     	BasicDBObject query = new BasicDBObject();
         DBObject client = coll.findOne();
         
         if(client != null) {
     		return client.get("username").toString();
 		}
 		else {
 			return null;
 		}			
 
     }
 
     /*
      *  Friends
      -----------------------------------------------------------------------
      */
 
     public static JSONObject[] getFriends() throws JSONException {
     	DBCollection coll = db.getCollection("friends");
     	JSONObject[] arr = new JSONObject[(int)coll.getCount()];		// Create a JSONObject array with length of coll
     	DBCursor cur = coll.find();
     	int index = 0;
     	
     	while(cur.hasNext()){
     		JSONObject j = new JSONObject();
     		DBObject friend = cur.next();
         	BasicDBObject cert = (BasicDBObject)friend.get("certificate");		// Retrieve the certificate of the friend
         	
         	j.put("realname", cert.get("realname"));
         	j.put("username", cert.get("username"));
         	j.put("public_exponent", cert.get("public_exponent"));
     		arr[index] = j;
     	}
     	
         return arr;
     }
 
     public static JSONObject getCertificate(String username) throws JSONException {
     	DBCollection coll = db.getCollection("friends");
     	
     	BasicDBObject query = new BasicDBObject();
         query.put("username", username);
         DBCursor cur = coll.find(query);						// Form a query checking the username field
         
         if(cur.hasNext()){
         	JSONObject c = new JSONObject();
         	DBObject friend = cur.next();
         	BasicDBObject cert = (BasicDBObject)friend.get("certificate");		// Retrieve the certificate of the friend
         	
         	c.put("version", cert.get("version"));				// Copy all the data to the JSONObject
             c.put("realname", cert.get("realname"));
             c.put("username", cert.get("username"));
             c.put("email", cert.get("email"));
             c.put("not_before", cert.get("not_before"));
             c.put("not_after", cert.get("not_after")); 
             c.put("notes", cert.get("notes"));
             c.put("modulus", cert.get("modulus"));			
             c.put("public_exponent", cert.get("public_exponent"));
             
             return c;
         }
         
         return null;
     }
 
     public static boolean isFriend(String username) {
     	DBCollection coll = db.getCollection("friends");
     	
     	BasicDBObject query = new BasicDBObject();
         query.put("username", username);
         DBCursor cur = coll.find(query);					// Form a query checking the username field of the friends document
         
         if(cur.hasNext())
         	return true;									// If the query returns a document, return true
         return false;
     }
 
     /*
      *  Wall Posts
      -----------------------------------------------------------------------
      */
 
     public static JSONObject[] getWallPosts() throws JSONException {
 
         // Get wallposts collection
     	DBCollection coll = db.getCollection("wallposts");
 
         // Create a JSONObject array with length of coll
     	JSONObject[] arr = new JSONObject[(int)coll.getCount()];
 
         // Get cursor to interate through wallposts
     	DBCursor cur = coll.find();
 
     	int index = 0;
     	while(cur.hasNext()){
     		JSONObject j = new JSONObject();
     		DBObject wallpost = cur.next();
         	
         	j.put("username", wallpost.get("username"));
         	j.put("message", wallpost.get("message"));
 
             // Add wallpost JSON object to array
     		arr[index] = j;
     	}
 
         return arr;
     }
 
     /*
      *  Printing
      ************************************************************************
      */
 
     /*
      *  Friends
      -----------------------------------------------------------------------
      */
 
     public static void printFriends() {
     	DBCollection coll = db.getCollection("friends");
     	
     	DBCursor cur = coll.find();											// Get the list of friends
     	
     	while(cur.hasNext()){
     		DBObject friend = cur.next();								// Iterate through the friends
         	BasicDBObject cert = (BasicDBObject)friend.get("certificate");		// Retrieve the certificate of the friend
     		
     		System.out.println("Real name: " + cert.get("realname") + 		// Print out the friend's info
     						" Username: " + cert.get("username") + 
     						" Public Key: " + cert.get("public_exponent"));
     	}
 
     }
 
     /*
      *  Wall Posts
      -----------------------------------------------------------------------
      */
 
     public static void printWallPosts() {
 		
     	DBCollection coll = db.getCollection("wallposts");
     	
     	DBCursor cur = coll.find();	// Get the list of wallposts
     	
     	while(cur.hasNext()){
     		DBObject wallpost = cur.next();	// Iterate through the wallposts
     		
     		System.out.println(	"Sender's username: " + wallpost.get("username") + "\n" +
     							"Message: " + wallpost.get("message"));
     	}
 
     }
 
 }
