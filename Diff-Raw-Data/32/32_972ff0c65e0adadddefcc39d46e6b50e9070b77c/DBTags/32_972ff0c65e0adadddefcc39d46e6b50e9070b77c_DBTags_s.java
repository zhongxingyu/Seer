 package smartread.db;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import com.mongodb.BasicDBObject;
 import com.mongodb.DBCollection;
 import com.mongodb.DBObject;
 
 public class DBTags extends DBBase {
     private static final Logger logger = LogManager.getLogger(DBTags.class);

    public static void addTag(String tag){
    }
    
    public void removeTag(String tag){
        
    }
     
     public static void addTags(List<String> tags){
         Long starttime = System.currentTimeMillis();
 
         if(mongoClient == null){
             try {
                 initDB();
             } catch (UnknownHostException e) {
                 e.printStackTrace();
             }
         }
         DBCollection tagsColl = db.getCollection(DB_TAGS_TABLE);
                 
         List<DBObject> objs = new ArrayList<DBObject>();
         for(String tag: tags){
             DBObject obj = new BasicDBObject("name", tag);
             DBObject result = tagsColl.findOne(obj);
             if(result!=null) continue;
             objs.add(obj);
         }
         
         tagsColl.insert(objs);
         Long endtime = System.currentTimeMillis();
         logger.debug("Time(ms) taken to add tags in DB: "+ String.valueOf(endtime-starttime));
     }
     
     public static void cleanTags(){
         Long starttime = System.currentTimeMillis();
 
         if(mongoClient == null){
             try {
                 initDB();
             } catch (UnknownHostException e) {
                 e.printStackTrace();
             }
         }
         DBCollection tagsColl = db.getCollection(DB_TAGS_TABLE);
         tagsColl.drop();
         Long endtime = System.currentTimeMillis();
         logger.debug("Time(ms) taken to drop tags in DB: "+ String.valueOf(endtime-starttime));
     }
 }
