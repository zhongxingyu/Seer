 // This program is for fetching most recent analysis details
 
 package edu.sjsu.cmpe.bigdata.dto;
 
 import java.lang.reflect.Array;
 import java.net.UnknownHostException;
 import java.util.Arrays;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.MongoClient;
 
 public class SentimentAnalysisDto {
 	
 	public String getSentiment() throws UnknownHostException {
		
 		// Creating connection with MongoDB
 		MongoClient client = new MongoClient();
 		DB courseDB = client.getDB("bigdata");
 		DBCollection collection = courseDB.getCollection("analysis");
 		
 		// Creating query for fetching details of latest analysis
 		DBObject query1 = new BasicDBObject()
 						.append("business_id", "VFslQjSgrw4Mu5_Q1xk1KQ");
 
 		DBObject query2 = new BasicDBObject()
 						.append("date", -1);
 		
 		// Executing query
 		DBCursor cursor = collection.find(query1).sort(query2).limit(1);
 			
 		// Fetching "keys" from cursor	
 		DBObject cur = cursor.next();
 		String business_id = (String) cur.get("business_id");
 		int positive = (Integer) cur.get("positive");
 		int negative = (Integer) cur.get("negative");
 		int neutral  = (Integer) cur.get("neutral");
 		int notEval  = (Integer) cur.get("notEval");
 			
 		// Printing analysis details
 		System.out.println("Number of neutral reviews       : "+ neutral);
 		System.out.println("Number of positive reviews      : "+ positive);
 		System.out.println("Number of negative reviews      : "+ negative);
 		System.out.println("Number of reviews not evaluated : "+ notEval);
 		
		//NOTE: Fix below to return data in json format
		return "";
 		}
 	}
