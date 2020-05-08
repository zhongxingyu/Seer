 package com.velix.jmongo;
 
 import com.velix.bson.BSONDocument;
 import com.velix.jmongo.impl.MongoImpl;
 
 public class Test {
 
 	public static void main(String[] args) throws Exception {
 		Mongo mongo = new MongoImpl("127.0.0.1", 27017, new Configuration());
 		MongoDB db = mongo.getDB("goojia");
 		MongoCollection collection = db.getCollection("goojia");
		collection.setSafeMode(true);
		BSONDocument query = new BSONDocument();
		query.put("name", "velna");
 //		Cursor cursor = collection.find(query);
 //		for (BSONDocument doc : cursor) {
 //			System.out.println(doc);
 //		}
 //		cursor.close();
		collection.save(query);
 		System.out.println(collection.count(new BSONDocument(), null));
 //		System.out.println(db.getCollectionNames());
 		mongo.close();
 	}
 }
