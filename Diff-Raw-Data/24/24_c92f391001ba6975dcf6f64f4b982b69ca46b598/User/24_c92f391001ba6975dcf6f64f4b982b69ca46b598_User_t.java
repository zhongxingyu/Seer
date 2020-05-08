 package models;
 
 import net.vz.mongodb.jackson.*;
 import org.codehaus.jackson.annotate.JsonProperty;
 import utils.DataUtil;
 import utils.EncryptionUtil;
 
 @MongoCollection(name = "users")
 public class User {
 
     private String id;
 
     @ObjectId
     @JsonProperty("_id")
     public String getId() {
         return id;
     }
 
     @ObjectId
     @JsonProperty("_id")
     public void setId(String id) {
         this.id = id;
     }
 
     public String username;
 
     public String password;
 
     public String securityQuestionOne;
 
     public String securityAnswerOne;
 
     public String securityQuestionTwo;
 
     public String securityAnswerTwo;
 
     public String securityQuestionThree;
 
     public String securityAnswerThree;
 
     public User() {   }
 
     public User(String username, String password) {
         this.username = username;
         this.password = password;
     }
 
     public static Boolean isUsernameTaken(String username) {
         JacksonDBCollection<User, String> collection = DataUtil.getCollection("users", User.class);
 
         DBCursor cursorDoc = collection.find(DBQuery.is("username", username));
 
         return (cursorDoc.hasNext());
     }
 
     public static User findUserByName(String username) {
         JacksonDBCollection<User, String> collection = DataUtil.getCollection("users", User.class);
 
         DBCursor cursorDoc = collection.find(DBQuery.is("username", username));
 
         if(cursorDoc.hasNext())
             return ((User)cursorDoc.next());
         else
             return null;
 
     }
 
     public static String getDecryptedPasswordForUser(String username) {
         JacksonDBCollection<User, String> collection = DataUtil.getCollection("users", User.class);
 
         DBCursor cursorDoc = collection.find(DBQuery.is("username", username));
 
         User user = (User) cursorDoc.next();
         EncryptionUtil decryptor;
 
         try {
             decryptor = new EncryptionUtil();
         } catch (Exception e) {
             return null;
         }
 
         if (user != null)
             return decryptor.decrypt(user.password);
         else
             return null;
     }
 
 }
