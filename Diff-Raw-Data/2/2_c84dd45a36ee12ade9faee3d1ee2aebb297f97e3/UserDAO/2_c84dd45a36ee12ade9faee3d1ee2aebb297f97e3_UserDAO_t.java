 package de.locked.signalcoverage;
 
 import com.google.gson.JsonSyntaxException;
 import com.mongodb.BasicDBObject;
 import com.mongodb.DB;
 import com.mongodb.DBCursor;
 import com.mongodb.DBObject;
 import de.locked.signalcoverage.share.v2.ApiUser;
 import java.net.UnknownHostException;
 import java.util.List;
 import java.util.UUID;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.mongodb.morphia.AdvancedDatastore;
 import org.mongodb.morphia.query.UpdateException;
 
 public class UserDAO extends MongoDAO {
 
     private static final Logger log = Logger.getLogger(UserDAO.class.getName());
 
     public UserDAO() throws UnknownHostException {
         super();
     }
 
     public DbUser getUser(int userId) throws JsonSyntaxException {
         AdvancedDatastore ds = getDatastore(DB_BASE);
         List<DbUser> users = ds.find(DbUser.class, "userId", userId).limit(1).asList();
         for (DbUser dbUser : users) {
             return dbUser;
         }
         return null;
     }
 
     private synchronized int getNewUserId() {
         int id = 1;
 
         DB db = getDB(DB_BASE);
         // get Max userID
         try (DBCursor cursor = db.getCollection(COLLECTION_USER).
                 find(null, new BasicDBObject("userId", 1).append("_id", 0)).
                sort(new BasicDBObject("userId", -1)).
                 limit(1)) {
             while (cursor.hasNext()) {
                 DBObject dbo = cursor.next();
                 Object _id = dbo.get("userId");
                 id = Integer.parseInt(_id.toString()) + 1;
             }
         }
         return id;
     }
 
     public DbUser newDbUser(ApiUser apiUser) {
         byte[] hashed = ApiUser.makePassBase64(apiUser.userId, apiUser.secret);
         String digest = new String(hashed);
         return new DbUser(apiUser.userId, digest);
     }
 
     public ApiUser createNewUser() {
         int i = getNewUserId();
         String pass = UUID.randomUUID().toString();
 
         ApiUser apiUser = new ApiUser(i, pass);
         DbUser save = newDbUser(apiUser);
 
         try {
             getDatastore(DB_BASE).save(save);
             return apiUser;
         } catch (UpdateException ue) {
             log.log(Level.WARNING, ue.getMessage(), ue);
             return null;
         }
     }
 }
