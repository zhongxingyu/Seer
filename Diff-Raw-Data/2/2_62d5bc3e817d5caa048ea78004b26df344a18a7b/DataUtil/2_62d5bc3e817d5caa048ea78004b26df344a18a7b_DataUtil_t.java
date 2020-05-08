 package utils;
 
 import com.mongodb.DB;
 import com.mongodb.ReadPreference;
 import com.mongodb.MongoClient;
 import models.User;
 import net.vz.mongodb.jackson.DBCursor;
 import net.vz.mongodb.jackson.JacksonDBCollection;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * User: Charles
  * Date: 4/28/13
  */
 public class DataUtil {
 
     private static MongoClient mongoClient;
 
     public static DB getDB() {
 
         try {
 
             mongoClient = new MongoClient( "ds061787.mongolab.com" , 61787 );
 
            mongoClient.setReadPreference(ReadPreference.primary());

             DB dataBase = mongoClient.getDB("heroku_app15452455");
 
             dataBase.authenticate("heroku_app15452455", "73mi73eoolvr4s7v47ugutfru9".toCharArray());
 
             return dataBase;
 
         } catch (UnknownHostException e) {
             return null;
         }
     }
 
     public static JacksonDBCollection getCollection(String collection, Class clazz) {
 
         try {
 
             return JacksonDBCollection.wrap(getDB().getCollection(collection), clazz, String.class);
 
         } catch (Exception e) {
             return null;
         }
 
     }
 
     public static Object getEntityById(String collection, Class clazz, String id) {
 
         try {
 
             JacksonDBCollection jacksonDBCollection =  JacksonDBCollection.wrap(getDB().getCollection(collection), clazz, String.class);
             return jacksonDBCollection.findOneById(id);
 
         } catch (Exception e) {
             return null;
         }
 
     }
 
     public static List<String> dump(String collection, Class clazz) {
 
         try {
 
             List<String> dumpList = new ArrayList<String>();
 
             JacksonDBCollection jacksonCollection = getCollection(collection, clazz);
 
             DBCursor cursorDoc = jacksonCollection.find();
 
             while (cursorDoc.hasNext()) {
                 User user = (User)cursorDoc.next();
                 dumpList.add(user.username);
             }
 
             return dumpList;
 
         } catch (Exception e) {
             return null;
         }
     }
 }
