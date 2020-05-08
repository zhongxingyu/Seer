 package cryptocast.server;
 
 import cryptocast.crypto.*;
 
 import com.google.common.base.Optional;
 
 import java.io.Serializable;
 import java.security.PrivateKey;
 import java.util.Map;
 
 /** Contains the data which is managed by the controller and presented by the view.
  * @param <ID> The type of the user identities
  */
 public class ServerData<ID> implements Serializable {
     private Map<String, User> userByName;
     private Map<ID, User> userById;
     private BroadcastSchemeUserManager<ID> users;
     private BroadcastSchemeKeyManager<ID> keys;
     private OutChannel server;
 
     /**
      * Creates and saves a new user by name.
      * @param name The user's name
      * @return The new user if he has been added successfully, else absent is returned.
      */
     public Optional<User<ID>> createNewUser(String name) { return null; }
 
     /**
      * Retrieves a user by name
      * @param name The user's name
      * @return A user instance, if it was found, or absent otherwise
      */
     public Optional<User<ID>> getUserByName(String name) { return null; }
 
     /**
      * Retrieves a user's personal key
      * @param user The user object
      * @return The private key
      */
     public Optional<PrivateKey> getPersonalKey(User<ID> user) { return null; }
 }
