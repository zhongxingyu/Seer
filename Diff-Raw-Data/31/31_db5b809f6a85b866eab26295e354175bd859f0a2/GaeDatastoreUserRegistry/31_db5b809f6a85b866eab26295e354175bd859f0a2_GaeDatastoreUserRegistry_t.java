 /**
  * 
  */
 package org.mdkt.library.security.users;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.EnumSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 import org.springframework.security.core.GrantedAuthority;
 
 import com.google.appengine.api.datastore.DatastoreService;
 import com.google.appengine.api.datastore.DatastoreServiceFactory;
 import com.google.appengine.api.datastore.Entity;
 import com.google.appengine.api.datastore.EntityNotFoundException;
 import com.google.appengine.api.datastore.Key;
 import com.google.appengine.api.datastore.KeyFactory;
 import com.google.appengine.api.datastore.Query;
 
 /**
  * UserRegistry implementation which uses GAE's low-level Datastore APIs.
  *
  * @author Luke Taylor
  */
 public class GaeDatastoreUserRegistry implements UserRegistry {
     private final Logger logger = Logger.getLogger(getClass());
 
     private static final String TEMP_USER_TYPE = "TempGaeUser";
     private static final String TEMP_USER_GOOGLE_USER_ID = "googleUserId";
     private static final String TEMP_USER_EXPIRES = "_expires";
     
     private static final String USER_TYPE = "GaeUser";
     private static final String USER_FIRSTNAME = "firstname";
     private static final String USER_LASTNAME = "lastname";
     private static final String USER_EMAIL = "email";
     private static final String USER_ENABLED = "enabled";
     private static final String USER_LAST_SIGNED_IN = "lastSignedIn";
     private static final String USER_AUTHORITIES = "authorities";
 
     public GaeUser findUser(String userId) {
         Key key = KeyFactory.createKey(USER_TYPE, userId);
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
         try {
             Entity user = datastore.get(key);
 
             return fromEntity(user);
             
         } catch (EntityNotFoundException e) {
             logger.debug(userId + " not found in datastore");
             return null;
         }
     }
 
     private GaeUser fromEntity(Entity user) {
         long binaryAuthorities = (Long)user.getProperty(USER_AUTHORITIES);
         Set<AppRole> roles = EnumSet.noneOf(AppRole.class);
 
         for (AppRole r : AppRole.values()) {
             if ((binaryAuthorities & (1 << r.getBit())) != 0) {
                 roles.add(r);
             }
         }
 
         GaeUser gaeUser = new GaeUser(
                 user.getKey().getName(),
                 emptyIfNull((String)user.getProperty(USER_EMAIL)),
                 emptyIfNull((String)user.getProperty(USER_FIRSTNAME)),
                 emptyIfNull((String)user.getProperty(USER_LASTNAME)),
                 roles,
                 (Boolean)user.getProperty(USER_ENABLED));
        gaeUser.setLastSignedIn((Long)user.getProperty(USER_LAST_SIGNED_IN));
 
         return gaeUser;
 	}
 
 	private String emptyIfNull(String value) {
 		return value == null ? "" : value;
 	}
 
 	/**
 	 * Create/Update user
 	 * @see DatastoreService.put()
 	 */
 	public void registerUser(GaeUser newUser) {
         logger.debug("Attempting to create new user " + newUser);
 
         Key key = KeyFactory.createKey(USER_TYPE, newUser.getUserId());
         Entity user = new Entity(key);
         user.setProperty(USER_EMAIL, newUser.getEmail());
         user.setProperty(USER_FIRSTNAME, newUser.getFirstname());
         user.setProperty(USER_LASTNAME, newUser.getLastname());
         user.setProperty(USER_LAST_SIGNED_IN, 0);
         user.setUnindexedProperty(USER_ENABLED, newUser.isEnabled());
 
         Collection<? extends GrantedAuthority> roles = newUser.getAuthorities();
 
         long binaryAuthorities = 0;
 
         for (GrantedAuthority r : roles) {
             binaryAuthorities |= 1 << ((AppRole)r).getBit();
         }
 
         user.setUnindexedProperty(USER_AUTHORITIES, binaryAuthorities);
 
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
         datastore.put(user);
     }
 
     public void removeUser(String userId) {
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
         Key key = KeyFactory.createKey(USER_TYPE, userId);
 
         datastore.delete(key);
     }
     
     public void removeUsers(ArrayList<String> userIds) {
     	ArrayList<Key> keys = new ArrayList<Key>();
     	for (String userId : userIds) {
     		keys.add(KeyFactory.createKey(USER_TYPE, userId));
     	}
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     	datastore.delete(keys);
     }
     
     @Override
     public List<GaeUser> getAllGaeUsers() {
     	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     	Query query = new Query(USER_TYPE);
     	// TODO paging???
     	Iterator<Entity> entities = datastore.prepare(query).asIterator();
     	List<GaeUser> gaeUsers = new ArrayList<GaeUser>();
     	while (entities.hasNext()) {
     		gaeUsers.add(fromEntity(entities.next()));
     	}
     	return gaeUsers;
     }
     
     @Override
     public void acceptUser(String userId, String email) {
     	Key key = KeyFactory.createKey(TEMP_USER_TYPE, email);
     	Entity e = new Entity(key);
     	e.setProperty(TEMP_USER_GOOGLE_USER_ID, userId);
     	e.setProperty(TEMP_USER_EXPIRES, System.currentTimeMillis() + 7 * 24 * 60 + 60 * 1000); // 1 week
     	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     	datastore.put(e);
     }
     
     @Override
     public String findAcceptedUser(String email) {
     	Key key = KeyFactory.createKey(TEMP_USER_TYPE, email);
     	DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
     	 try {
              Entity user = datastore.get(key);
 
              return (String) user.getProperty(TEMP_USER_GOOGLE_USER_ID);
              
          } catch (EntityNotFoundException e) {
              logger.debug(email + " not found in datastore");
              return null;
          }
     }
     
     @Override
     public void updateLastSignedIn(String userId) {
     	Key key = KeyFactory.createKey(USER_TYPE, userId);
         DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
 
         try {
             Entity user = datastore.get(key);
             user.setProperty(USER_LAST_SIGNED_IN, System.currentTimeMillis());
             datastore.put(user);
         } catch (EntityNotFoundException e) {
             logger.debug(userId + " not found in datastore");
         } catch (Exception e1) {
         	logger.error("Update failed due to " + e1.getMessage());
         }
     	
     }
 }
