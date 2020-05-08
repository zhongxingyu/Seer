 package com.comoyo.jelastic;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import com.mongodb.Mongo;
 import java.net.UnknownHostException;
 import java.util.Date;
 
 public class PersistentStorage {
 
     public static void storeMatch(final String winner, final String loser) {
         final DB pingpong = getDB();
         Date date = new Date();
         DBCollection matchesTable = pingpong.getCollection("matches");
         BasicDBObject document = new BasicDBObject();
         document.put("winner", winner);
         document.put("loser", loser);
         document.put("date", date);
         matchesTable.insert(document);
     }
 
     private static DB getDB() {
         Mongo mongoClient;
         try {
             mongoClient = new Mongo("mongodb-rankitapp.jelastic.dogado.eu", 27017);
         } catch (UnknownHostException e) {
             throw new RuntimeException(e);
         }
         final DB pingpong = mongoClient.getDB("pingpong");
         final boolean auth = pingpong.authenticate("admin", "XIyqkxSMgQ".toCharArray());
         if (!auth) {
             return pingpong;
             //throw new RuntimeException("Mongo authentication failed!");
         }
         return pingpong;
     }
 
     public static String getMatches(final int limit) {
         final DB pingpong = getDB();
         DBCollection matchesTable = pingpong.getCollection("matches");
         DBCursor cursor = matchesTable.find().sort(new BasicDBObject("date", -1)).limit(limit);
         String result = "";
         for (DBObject dbObject : cursor) {
             result = result + dbObject.get("winner") + " beat " + dbObject.get("loser") + " at " +
                     dbObject.get("date") + "\n";
         }
         return result;
     }
 
     public static Person getPerson(final String name) {
         final DB pingpong = getDB();
         DBCollection personTable = pingpong.getCollection("persons");
        BasicDBObject query = new BasicDBObject("name", 1);
         DBObject dbObject = personTable.findOne(query);
         if (dbObject == null) {
             return new Person(name, 1200);
         }
         return new Person(dbObject.get("name").toString(),
                 Double.parseDouble(dbObject.get("ranking").toString()));
     }
 
     public static void setPerson(final Person person) {
         final DB pingpong = getDB();
         DBCollection personTable = pingpong.getCollection("persons");
         BasicDBObject query = new BasicDBObject("name", person.name);
         BasicDBObject updatedPerson = new BasicDBObject();
         updatedPerson.put("name", person.name);
         updatedPerson.put("ranking", person.ranking);
 
         personTable.update(query, new BasicDBObject("$set", updatedPerson), true, false);
     }
 
     public static String getRankingList() {
         final DB pingpong = getDB();
         DBCollection matchesTable = pingpong.getCollection("persons");
         DBCursor cursor = matchesTable.find().sort(new BasicDBObject("ranking", -1));
         String result = "";
         for (DBObject dbObject : cursor) {
             result = result + dbObject.get("name") + ": " + dbObject.get("ranking") + "<br>\n";
         }
         return result;
     }
 }
