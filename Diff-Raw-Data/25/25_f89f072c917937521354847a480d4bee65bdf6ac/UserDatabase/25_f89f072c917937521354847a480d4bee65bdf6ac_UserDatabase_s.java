 package chatter.server;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.HashMap;
 
 /**
  * @author Bharath Ravi
  * @author Kapil Gole
  * @author Alban Dumouilla
  *
  * A database of {@code User} objects. This is simply a
  * HashMap of usernames to their corresponding User objects.
  *
  * This is a singleton, read-only class that is created once when the server
  * starts up and accessed by all threads.
  *
  * This could be better designed, but was not necessary for purpose of
  * this project.
  */
 public class UserDatabase {
   HashMap<String, User> database = new HashMap<String, User>();
 
   private static final UserDatabase instance = new UserDatabase();
 
   public static UserDatabase getInstance() {
     return instance;
   }
 
   private UserDatabase(){
     // Hardcode some users into the database.
     database.put("bharath",
         new User("bharath",
             "574a69a9525f736f80b97f61a1e7b11a9bc536428678e4326e0e5bfa099ad6fe",
             "n)(3A93nA42a2946"));
 
     database.put("alban",
         new User("alban",
             "936af28a46918f27329eae6062e81543339a9360a465c4b2c246871e68676110",
             "(FNXO#ns&fd91956"));
 
     database.put("kapil",
         new User("kapil",
             "6126bc96ec205fefb135ce5e268d6fffea68b7bbed0a2870b4cebace2b928aa1",
             "}{fm09x)ncTT8456"));
 
     database.put("george",
         new User("george",
             "168f252663f77313e897de4a669bee409251cab928fff066585d6e208a2d7374",
             "Jd92^.<'SHuJas67"));
   }
 }
