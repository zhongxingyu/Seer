 package smartread.db;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import smartread.User;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 
 public class DBUser extends DBBase{
     private static final Logger logger = LogManager.getLogger(DBUser.class);
 
 /*    public static User retrieveUser(String uid) {
         assert uid!=null && uid!="" : "User id should be empty or null";
         Long starttime = System.currentTimeMillis();
 
         if(mongoClient == null){
             try {
                 initDB();
             } catch (UnknownHostException e) {
                 e.printStackTrace();
                 return null;
             }
         }
         
         DBCollection coll = db.getCollection(DB_USER_INTEREST_TABLE);
 
         DBObject obj = new BasicDBObject(DB_UID_FIELD, uid);
         DBObject userInfo = coll.findOne(obj);
         if(userInfo==null){
             logger.error("Cannot find user: "+uid+" in DB");
             return null;
         }
 
         Map<String, Double> maps = new HashMap<String, Double>();
         DBObject interests;
 
         String[] int_info = { "interests_5m", "interests_1h", "interests_1d", "interests_7d" };
         for (String s : int_info) {
             double factor = 1.0;
             if(s.equalsIgnoreCase("interests_1h")){
                 factor = 0.8;
             }else if (s.equalsIgnoreCase("interests_1d")){
                 factor = 0.6;
             }else if (s.equalsIgnoreCase("interests_7d")){
                 factor = 0.4;
             }
             
             interests = (DBObject) userInfo.get(s);
             for (String key : interests.keySet()) {
                 if (maps.containsKey(key)) {
                     maps.put(
                             key,
                             maps.get(key)
                                     + factor*Double.valueOf(interests.get(key)
                                             .toString()));
                 } else {
                     maps.put(key, factor*Double.valueOf(interests.get(key).toString()));
                 }
             }
         }
 
         Long endtime = System.currentTimeMillis();
         logger.debug("Time(ms) taken to retrive user from DB: "+ String.valueOf(endtime-starttime));
 
         return new User((String) userInfo.get(DB_UID_FIELD), maps);
     }
 */
     public static void updateUserInterest(String uid, String freq, Map<List<String>, Double> tags) {
         Long starttime = System.currentTimeMillis();
 
         if(mongoClient == null){
             try {
                 initDB();
             } catch (UnknownHostException e) {
                 e.printStackTrace();
                 return;
             }
         }
 
         DBCollection coll = db.getCollection(DB_USER_INTEREST_TABLE);
 
         DBObject query = new BasicDBObject(DB_UID_FIELD, uid);
         
         
         DBObject userInfo = coll.findOne(query);
         if(userInfo==null){
             userInfo = initUserInterest(uid);
         }
         String interest_info = DB_INTEREST_FIELD+freq;
         BasicDBObject interestsDB = (BasicDBObject) userInfo.get(interest_info);
         
         for (List<String> key : tags.keySet()) {
             Double score = tags.get(key);
 
             for(String tag: key){
                 Double value = (Double) interestsDB.get(tag);
                 if(value == null){
                     interestsDB.append(tag, score);
                 }else{
                     interestsDB.put(tag, value+score);
                 }
             }
         }
         
         coll.update(query, new BasicDBObject().append("$set", 
                 new BasicDBObject().append(DB_INTEREST_FIELD+freq, interestsDB).append("last_update", System.currentTimeMillis())));
         Long endtime = System.currentTimeMillis();
         logger.debug("Time(ms) taken to update interests for user "+uid+": "+ String.valueOf(endtime-starttime));
     }
 
     private static BasicDBObject initUserInterest(String uid) {
         DBCollection coll = db.getCollection(DBBase.DB_USER_INTEREST_TABLE);
         BasicDBObject doc = new BasicDBObject(DBBase.DB_UID_FIELD, uid)
                 .append("interests_5m", new BasicDBObject())
                 .append("interests_1h", new BasicDBObject())
                 .append("interests_1d", new BasicDBObject())
                 .append("interests_7d", new BasicDBObject());
         coll.insert(doc);
         return doc;
     }
 
     public static void updateUserInterest(String freq_now, String freq_pre) {
         Long starttime = System.currentTimeMillis();
 
         if(mongoClient == null){
             try {
                 initDB();
             } catch (UnknownHostException e) {
                 e.printStackTrace();
                 return;
             }
         }
 
         DBCollection coll = db.getCollection(DB_USER_INTEREST_TABLE);
 
         DBObject query = new BasicDBObject(DB_INTEREST_FIELD+freq_pre, new BasicDBObject("$ne", new BasicDBObject()));
         
         DBCursor cursor = coll.find(query);
 
         try {
             while(cursor.hasNext()) {
                 DBObject user = cursor.next();
                 BasicDBObject interests_pre = (BasicDBObject) user.get(DB_INTEREST_FIELD+freq_pre);
                 BasicDBObject interests_now = (BasicDBObject) user.get(DB_INTEREST_FIELD+freq_now);
                 
                 for(String tag: interests_pre.keySet()){
                     Double score_pre = (Double) interests_pre.get(tag);
                     Double score_now = (Double) interests_now.get(tag);
                     if(score_now == null){
                         interests_now.append(tag, score_pre);
                     }else{
                         interests_now.put(tag, score_now+score_pre);
                     }
                 }
                 
                 coll.update(new BasicDBObject(DB_UID_FIELD, user.get(DB_UID_FIELD)) , new BasicDBObject().append("$set", 
                         new BasicDBObject().append(DB_INTEREST_FIELD+freq_now, interests_now).append(DB_INTEREST_FIELD+freq_pre, new BasicDBObject())));
             }
         } finally {
             cursor.close();
         }
 
         
         Long endtime = System.currentTimeMillis();
         logger.debug("Time(ms) taken to update interests for freq "+freq_now+": "+ String.valueOf(endtime-starttime));
     }
 
     
     public static List<User> retrieveAllUser() {
         Long starttime = System.currentTimeMillis();
         if(mongoClient == null){
             try {
                 initDB();
             } catch (UnknownHostException e) {
                 e.printStackTrace();
                 return null;
             }
         }
         
         DBCollection coll = db.getCollection(DB_USER_INTEREST_TABLE);
         DBCursor cursor = coll.find();
         List<User> users = new ArrayList<User>();
         try {
             while(cursor.hasNext()) {
                 DBObject userInfo = cursor.next();
                 Map<String, Double> maps = new HashMap<String, Double>();
                 DBObject interests;
 
                 String[] int_info = { "interests_5m", "interests_1h", "interests_1d", "interests_7d" };
                 for (String s : int_info) {
                     interests = (DBObject) userInfo.get(s);
                     double factor = 1.0;
                     if(s.equalsIgnoreCase("interests_1h")){
                         factor = 0.8;
                     }else if (s.equalsIgnoreCase("interests_1d")){
                         factor = 0.5;
                     }else if (s.equalsIgnoreCase("interests_7d")){
                         factor = 0.2;
                     }
                     
                     for (String key : interests.keySet()) {
                         if (maps.containsKey(key)) {
                             maps.put(
                                     key,
                                     maps.get(key)
                                             + factor*Double.valueOf(interests.get(key)
                                                     .toString()));
                         } else {
                             maps.put(key, factor*Double.valueOf(interests.get(key).toString()));
                         }
                     }
                 }
                 users.add(new User((String) userInfo.get(DB_UID_FIELD), maps));
             }
         } finally {
             cursor.close();
         }
 
         
         Long endtime = System.currentTimeMillis();
         logger.debug("Time(ms) taken to retrieve all users: "+ String.valueOf(endtime-starttime));
         return users;
     }
 }
