 package edu.sjsu.cmpe.bigdata.api.resources;
 
 import javax.management.Query;
 import javax.ws.rs.Consumes;
 import javax.ws.rs.GET;
 import javax.ws.rs.POST;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.QueryParam;
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 
 import com.yammer.metrics.annotation.Timed;
 
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.mongodb.MongoClient;
 import com.mongodb.MongoException;
 import com.mongodb.WriteConcern;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBObject;
 import com.mongodb.DBCursor;
 import com.mongodb.ServerAddress;
 
 import edu.sjsu.cmpe.bigdata.domain.User;
 import edu.sjsu.cmpe.bigdata.dto.LinkDto;
 import edu.sjsu.cmpe.bigdata.dto.LinksDto;
 import edu.sjsu.cmpe.bigdata.dao.MongoDBDAO;
 
 
 @Path("/v1/users")
 @Produces(MediaType.APPLICATION_JSON)
 @Consumes(MediaType.APPLICATION_JSON)
 public class UserResource {
 	private int authenticated=400;
 
     public UserResource() {
     }
 
     @POST
     @Timed(name = "create-user")
     public Response createUser(User user) throws FileNotFoundException, IOException {
     	/**
     	 * Creating an instance of MongoDB Data Access Layer to connect to database
     	 */
     	MongoDBDAO mongoClient = new MongoDBDAO();
     	mongoClient.getDBConnection(mongoClient.getDbHostName(), mongoClient.getDbPortNumber());
     	mongoClient.getDB(mongoClient.getDbName());
     	
 		/**
     	 * Creating a new Collection: bigdataUserCollection
     	 */
     	mongoClient.getCollection(mongoClient.getBigdataUserCollection());
 		
     	/**
     	 * Creating a new document and inserting data
     	 */
     	BasicDBObject doc = new BasicDBObject("username",user.getUsername()).append("email", user.getEmail()).append("password", user.getPassword());
 		mongoClient.insertData(doc);
		mongoClient.closeConnection();
     	
 		/**
     	 * Closing connection
     	 */
 		mongoClient.closeConnection(); 	
 	return Response.status(201).build();
     }
     
     @GET
     @Timed(name = "authenticate-user")
     public Response authenticateUser(@QueryParam("username") String  username, @QueryParam("password") String password) throws FileNotFoundException, IOException {   	
     	/**
     	 * Creating an instance of MongoDB Data Access Layer to connect to database
     	 */
     	MongoDBDAO mongoClient = new MongoDBDAO();
     	mongoClient.getDBConnection(mongoClient.getDbHostName(), mongoClient.getDbPortNumber());
     	mongoClient.getDB(mongoClient.getDbName());
     	
 		/**
     	 * Accessing Collection: bigdataUserCollection
     	 */
     	mongoClient.getCollection("bigdataUserCollection");
 		
 		/**
     	 * Creating query1 for user authentication
     	 */
 		BasicDBObject query1 = new BasicDBObject();
 		List<BasicDBObject> query1List = new ArrayList<BasicDBObject>();
 		query1List.add(new BasicDBObject("username", username));
 		query1List.add(new BasicDBObject("password", password));
 		query1.put("$and", query1List);
 	 
 		DBCursor cursor = mongoClient.findData(query1);
 		while (cursor.hasNext()) {
 			System.out.println(cursor.next());
 			authenticated=200;
 		}
     	
 		/**
     	 * Closing connection
     	 */
 		mongoClient.closeConnection(); 	
 	return Response.status(authenticated).build();
     }
     
     @GET
     @Path("/{id}/competition")
     @Timed(name = "getCompetition")
     public List<DBObject> getCompetition(@PathParam("id") String id) throws FileNotFoundException, IOException {
     	/**
     	 * Creating an instance of MongoDB Data Access Layer to connect to database
     	 */
     	MongoDBDAO mongoClient = new MongoDBDAO();
     	mongoClient.getDBConnection(mongoClient.getDbHostName(), mongoClient.getDbPortNumber());
     	mongoClient.getDB(mongoClient.getDbName());
     	
 		/**
     	 * Accessing Collection: yelp
     	 */
     	mongoClient.getCollection("yelp");
     	
 		/**
     	 * Creating query1 to find competition
     	 */
 		BasicDBObject query1 = new BasicDBObject();
 		query1.put("type", "business");
 		query1.put("categories", "Restaurants");
 		query1.put("schools", "Stanford University");
 		
 		DBCursor cursor = mongoClient.findData(query1);
 		List<DBObject> compList = new ArrayList<DBObject>();
 		while(cursor.hasNext()) {
 			compList.add(cursor.next());
 		}
 		
 		/**
     	 * Closing connection
     	 */
 		mongoClient.closeConnection(); 	
 	return compList;
     }
 }
 
