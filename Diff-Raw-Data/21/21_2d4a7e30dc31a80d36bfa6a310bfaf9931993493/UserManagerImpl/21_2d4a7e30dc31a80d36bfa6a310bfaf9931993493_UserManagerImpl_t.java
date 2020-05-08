 package polly.core;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import org.apache.log4j.Logger;
 
 import polly.data.Attribute;
 import polly.events.EventProvider;
 
 import de.skuzzle.polly.parsing.Declarations;
 import de.skuzzle.polly.parsing.Namespaces;
 import de.skuzzle.polly.parsing.ParseException;
 import de.skuzzle.polly.sdk.Disposable;
 import de.skuzzle.polly.sdk.UserManager;
 import de.skuzzle.polly.sdk.eventlistener.IrcUser;
 import de.skuzzle.polly.sdk.exceptions.AlreadySignedOnException;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.UnknownAttributeException;
 import de.skuzzle.polly.sdk.exceptions.UnknownUserException;
 import de.skuzzle.polly.sdk.exceptions.UserExistsException;
 import de.skuzzle.polly.sdk.model.User;
 
 
 
 public class UserManagerImpl implements UserManager, Disposable {
 
     private static Logger logger = Logger.getLogger(UserManagerImpl.class.getName());
     private PersistenceManagerImpl persistence;
 
     /**
      * Stores the currently signed on users. Key: the nickname.
      */
     private Map<String, User> onlineCache;
     
     
     /**
      * Stores user specific declarations. Key: the user name.
      */
    
     private String declarationCachePath;
     
     private EventProvider eventProvider;
     private Namespaces namespaces;
     
     
     
     public UserManagerImpl(PersistenceManagerImpl persistence, 
             String declarationCache, EventProvider eventProvider) {
         this.eventProvider = eventProvider;
         this.persistence = persistence;
         this.onlineCache = Collections.synchronizedMap(new HashMap<String, User>());
         this.declarationCachePath = declarationCache.endsWith("/") ? declarationCache :
             declarationCache + "/";
         this.createNamespaces();
     }
     
     
     
     public Namespaces getNamespaces() {
         return this.namespaces;
     }
     
     
     
     private void createNamespaces() {
         logger.debug("Creating user namespaces");
         this.namespaces = new Namespaces();
         
         FileFilter cacheFiles = new FileFilter() {
             @Override
             public boolean accept(File pathname) {
                 return pathname.getName().endsWith(".cache");
             }
         };
         
         File cacheDir = new File(this.declarationCachePath);
         File files[] = cacheDir.listFiles(cacheFiles);
         for (File cacheFile : files) {
             // remove '.cache' from filename
             String name = cacheFile.getName();
             name = name.substring(0, name.length() - 6);
             try {
                 this.namespaces.create(name, Declarations.restore(cacheFile));
                 logger.debug("Restored declaration cache for user '" + name + "'");
             } catch (Exception e) {
                 logger.error("Error while restoring declarations for user '" + name +
                     '.', e);
             }
         }
     }
     
     
     
    public synchronized Declarations getDeclarations(User user) {
         try {
             return this.namespaces.get(user.getName());
         } catch (ParseException goOn) { }
         
         
         logger.warn("Declaration-Cache file for user '" + user.getName() + 
                 " not found -- creating empty declarations.");
         Declarations d = new Declarations();
         d.getDeclarations().putAll(Declarations.RESERVED.getDeclarations());
         d.enter();
         this.namespaces.create(user.getName(), d);
         return d;
     }
     
     
     
     private void storeDeclarations() {
         logger.debug("Storing declaration cache to disk.");
         for (Entry<String, Declarations> e : this.namespaces.getNamespaces().entrySet()) {
             try {
                 File location = new File(this.declarationCachePath + e.getKey() 
                         + ".cache");
                 e.getValue().store(location);
             } catch (Exception e1) {
                 logger.error("Could not write declaration file for user '" + 
                         e.getKey() + "'", e1);
             }
         }
     }
     
     
 
     @Override
     public void deleteDeclaration(User user, String id) {
         try {
             Declarations d = this.namespaces.get(user.getName());
             if (d == null) {
                 return;
             }
             d.getDeclarations().remove(id);
         } catch (Exception e) {
             logger.error("Accessing non-existant namepsace " + user.getName());
         }
     }
     
     
     
     @Override
     public synchronized Set<String> getDeclaredIdentifiers(User user) {
         try {
             return this.namespaces.get(user.getName()).getDeclarations().keySet();
         } catch (ParseException e) {
             return new HashSet<String>();
         }
     }
     
 
     
     @Override
     public User getUser(IrcUser user) {
         return this.onlineCache.get(user.getNickName());
     }
 
     
     
     @Override
     public User getUser(String registeredName) {
         try {
             this.persistence.readLock();
             return this.persistence.findSingle(User.class, 
                     "USER_BY_NAME", registeredName);
         } finally {
             this.persistence.readUnlock();
         }
     }
     
     
 
     @Override
     public User updateUser(User old, User updated) {
         try {
             this.persistence.writeLock();
             this.persistence.startTransaction();
             old.setHashedPassword(updated.getHashedPassword());
             old.setUserLevel(updated.getUserLevel());
             this.persistence.commitTransaction();
             logger.info("User '" + old + "' updated to '" + updated + "'");
         } catch (DatabaseException e) {
             e.printStackTrace();
         } finally {
             this.persistence.writeUnlock();
         }
         return old;
     }
     
     
     
     public void addUser(User user) throws UserExistsException, DatabaseException {
         try {
             this.persistence.writeLock();
             User check = this.persistence.findSingle(User.class, "USER_BY_NAME", 
                     user.getName());
             
             if (check != null) {
                 logger.trace("User already exists.");
                 throw new UserExistsException(check);
             }
             
             List<Attribute> attributes = this.persistence.findList(Attribute.class, 
                     "ALL_ATTRIBUTES");
             polly.data.User u = (polly.data.User) user;
             logger.trace("Adding all attributes to new user.");
             for (Attribute att : attributes) {
                 u.getAttributes().put(att.getName(), att.getDefaultValue());
             }
             this.persistence.startTransaction();
             this.persistence.persist(user);
             this.persistence.commitTransaction();
             logger.info("Added user " + user);
         } finally {
             this.persistence.writeUnlock();
         }
     }
     
     
 
     @Override
     public void addUser(String name, String password, int level) 
             throws UserExistsException, DatabaseException {
         this.addUser(new polly.data.User(name, password, level));
     }
 
     
     
     @Override
     public void deleteUser(User user) throws DatabaseException {
         try {
             this.persistence.writeLock();
             this.logoff(user);
             this.persistence.startTransaction();
             this.persistence.remove(user);
             this.persistence.commitTransaction();
             logger.info("Deleted user " + user);
         } finally {
             this.persistence.writeUnlock();
         }
     }
 
     
     
     @Override
     public synchronized User logon(String from, String registeredName, String password) 
             throws UnknownUserException, AlreadySignedOnException {
         logger.info("Trying to log on user '" + registeredName + "'.");
         User user = this.getUser(registeredName);
         if (user == null) {
             throw new UnknownUserException(registeredName);
         }
         
         if (this.onlineCache.containsKey(user.getCurrentNickName())) {
             // TODO: throw exception
             throw new AlreadySignedOnException(user.getName());
         }
         
         if (user.checkPassword(password)) {
             user.setCurrentNickName(from);
             this.onlineCache.put(user.getCurrentNickName(), user);
             logger.info("Irc User " + from + " successfully logged in as " + 
                     registeredName);
             return user;
         }
         
         logger.warn("Login from '" + from + "' with user name '" + registeredName + 
                 "' failed: Invalid password.");
         return null;
     }
 
     
     
     @Override
     public void logoff(User user) {
         logger.info("User " + user + " logged off.");
         this.onlineCache.remove(user.getCurrentNickName());
     }
     
     
     
     @Override
     public void logoff(IrcUser user) {
         logger.info("User " + user + " logged off.");
         this.onlineCache.remove(user.getNickName());
     }
     
 
 
     @Override
     public boolean isSignedOn(IrcUser user) {
         return this.onlineCache.containsKey(user.getNickName());
     }
     
     
     
     @Override
     public boolean isSignedOn(User user) {
         return this.onlineCache.containsKey(user.getCurrentNickName());
     }
     
     
     
    public synchronized void traceNickChange(IrcUser oldUser, IrcUser newUser) {
         logger.debug("Tracing nickchange from '" + oldUser + "' to '" + newUser + "'");
         User tmp = this.onlineCache.get(oldUser.getNickName());
         tmp.setCurrentNickName(newUser.getNickName());
         this.onlineCache.remove(oldUser.getNickName());
         this.onlineCache.put(newUser.getNickName(), tmp);
     }
 
 
 
     @Override
     public void addAttribute(String name, String defaultValue) throws DatabaseException {
         try {
             this.persistence.writeLock();
             
             Attribute att = new Attribute(name, defaultValue);
             Attribute check = this.persistence.findSingle(Attribute.class, 
                     "ATTRIBUTE_BY_NAME", name);
             
             if (check != null) {
                 logger.trace("Tried to add an attribute that already existed: " + att + 
                         ". Existing attribute: " + check);
                 return;
             }
             List<User> all = this.persistence.findList(User.class, "ALL_USERS");
             this.persistence.startTransaction();
             this.persistence.persist(att);
             
             logger.trace("Adding new attribute to each user.");
             for (User user : all) {
                 polly.data.User u = (polly.data.User) user;
                 u.getAttributes().put(name, defaultValue);
             }
             this.persistence.commitTransaction();
             logger.info("Attribute " + att + " added.");
         } finally {
             this.persistence.writeUnlock();
         }
     }
 
 
 
     @Override
     public void removeAttribute(String name) throws DatabaseException {
         try {
             this.persistence.writeLock();
             List<User> all = this.persistence.findList(User.class, "ALL_USERS");
             Attribute att = this.persistence.findSingle(
                     Attribute.class, "ATTRIBUTE_BY_NAME", name);
             
             if (att == null) {
                 throw new UnknownAttributeException(name);
             }
             
             this.persistence.startTransaction();
             logger.trace("Removing attribute from all users.");
             for (User user : all) {
                 polly.data.User u = (polly.data.User) user;
                 u.getAttributes().remove(name);
             }
             this.persistence.remove(att);
             this.persistence.commitTransaction();
             logger.info("Attribute " + att + " removed.");
         } finally {
             this.persistence.writeUnlock();
         }
     }
 
 
 
     @Override
     public void dispose() {
         this.storeDeclarations();
         this.persistence = null;
         this.onlineCache.clear();
         this.onlineCache = null;
         this.namespaces = null;
     }
 
 
 
     @Override
     public User createUser(String name, String password, int userLevel) {
         return new polly.data.User(name, password, userLevel);
     }
     
     
     
     protected void fireUserSignedOn() {
         // TODO: ISSUE: 0000006
     }
     
     
     
     protected void fireUserSignedOff() {
         // TODO: ISSUE: 0000006
     }
 }
