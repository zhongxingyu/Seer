 package org.wyona.security.impl.yarep;
 
 import java.util.HashMap;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 import org.apache.log4j.Logger;
 import org.wyona.security.core.api.AccessManagementException;
 import org.wyona.security.core.api.Group;
 import org.wyona.security.core.api.IdentityManager;
 import org.wyona.security.core.api.User;
 import org.wyona.security.core.api.UserManager;
 import org.wyona.yarep.core.NoSuchNodeException;
 import org.wyona.yarep.core.Node;
 import org.wyona.yarep.core.NodeType;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryException;
 
 /**
  * The YarepUserManager expects to find all existing users under the node /users.
  * If the node /users does not exist, it will look under the root node.
  * All files which have &lt;user&gt; as root element will be recognized as a user
  * configuration. &lt;identity&gt; is also recognized as a user for backwards 
  * compatibility.
  */
 public class YarepUserManager implements UserManager {
     protected static Logger log = Logger.getLogger(YarepUserManager.class);
     
     private Repository identitiesRepository;
 
     protected IdentityManager identityManager;
 
     protected HashMap users;
 
     private String SUFFIX = "xml";
     private String DEPRECATED_SUFFIX = "iml";
 
     /**
      * Constructor.
      * @param identityManager
      * @param identitiesRepository
      * @throws AccessManagementException
      */
     public YarepUserManager(IdentityManager identityManager, Repository identitiesRepository) throws AccessManagementException {
         this.identityManager = identityManager;
         this.identitiesRepository = identitiesRepository;
     }
 
     /**
      * Finds all user nodes in the repository and instantiates the users.
      *
      * Note re caching: If the UserManager is being instantiated only once at the startup of a server for instance, then the users are basically being cached (see getUser) and changes within the repository by a third pary application will not be noticed.
      *
      * @throws AccessManagementException
      */
     public synchronized void loadUsers() throws AccessManagementException {
         log.warn("Load users: " + identitiesRepository.getName());
         this.users = new HashMap();
         try {
             Node usersParentNode = getUsersParentNode();
             // TODO: There seems to be a bug such that users like ac-identities/http\:/michaelwechner.livejournal.com/.xml are not being detected either by getNodes() or isResource()!
             Node[] userNodes = usersParentNode.getNodes();
             DefaultConfigurationBuilder configBuilder = new DefaultConfigurationBuilder(true);
             for (int i = 0; i < userNodes.length; i++) {
                 if (userNodes[i].isResource()) {
                     try {
                         Configuration config = configBuilder.build(userNodes[i].getInputStream());
                         // also support identity for backwards compatibility
                         if (config.getName().equals(YarepUser.USER) || config.getName().equals("identity")) {
                             User user = constructUser(this.identityManager, userNodes[i]);
                             log.debug("User (re)loaded: " + userNodes[i].getName() + ", " + user.getID());
                             this.users.put(user.getID(), user);
                         }
                     } catch (Exception e) {
                         String errorMsg = "Could not create user from repository node: " 
                             + userNodes[i].getPath() + ": " + e.getMessage();
                         log.error(errorMsg, e);
                         // NOTE[et]: Do not fail here because other users may still be ok
                         //throw new AccessManagementException(errorMsg, e);
                     }
                 }
             }
         } catch (RepositoryException e) {
             String errorMsg = "Could not read users from repository: " + e.getMessage();
             log.error(errorMsg, e);
             throw new AccessManagementException(errorMsg, e);
         }
     }
 
     /**
      * @see org.wyona.security.core.api.UserManager#createUser(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
      */
     public User createUser(String id, String name, String email, String password) throws AccessManagementException {
         if (existsUser(id)) {
             throw new AccessManagementException("User " + id + " already exists!");
         }
         try {
             Node usersParentNode = getUsersParentNode();
             //YarepUser user = new YarepUser(this, identityManager.getGroupManager(), usersParentNode, id, name, email, password);
             YarepUser user = new YarepUser(this, identityManager.getGroupManager(), id, name);
             user.setEmail(email);
             user.setPassword(password);
             user.setNode(usersParentNode.addNode(id + "." + SUFFIX, NodeType.RESOURCE));
             user.save();
             // Add to cache
             this.users.put(id, user);
             return user;
         } catch (Exception e) {
             log.error(e, e);
             throw new AccessManagementException(e.getMessage(), e);
         }
     }
 
     /**
      * Check if user exists within cache
      */
     private boolean existsWithinCache(String userId) {
         if (this.users.containsKey(userId)) return true;
         return false;
     }
 
     /**
      * Check if user exists within persistent access control repository
      */
     private boolean existsWithinRepository(String userId) {
         try {
             Node usersParentNode = getUsersParentNode();
 
             // Check .iml suffix in order to stay backwards compatible
             if (usersParentNode.hasNode(userId + "." + DEPRECATED_SUFFIX)) {
                 log.warn("Deprecated user node path '" + userId + "." + DEPRECATED_SUFFIX + "' within repository '" + identitiesRepository.getName() + "'. Please upgrade by replacing the suffix '." + DEPRECATED_SUFFIX + "' by '." + SUFFIX + "'");
                 return true;
             }
 
             if (usersParentNode.hasNode(userId + "." + SUFFIX)) return true;
         } catch (Exception e) {
             log.warn(e.getMessage(), e);
         }
         log.warn("No such user within persistent repository: " + userId);
         return false;
     }
 
     /**
      * @see org.wyona.security.core.api.UserManager#existsUser(java.lang.String)
      */
     public boolean existsUser(String id) throws AccessManagementException {
         // Check the cache first
         if (!existsWithinCache(id)) {
             // Also check the repository
             return existsWithinRepository(id);
         }
         return true;
     }
 
     /**
      * @see org.wyona.security.core.api.UserManager#getUser(java.lang.String)
      */
     public User getUser(String id) throws AccessManagementException {
         if (!existsWithinCache(id)) {
             if (existsWithinRepository(id)) {
                 // TODO: Do not re-init the whole cache, but only incrementally for this particular user. Also please note that getUser(String, boolean) already refreshes the cache!
                 try {
                     String nodeName = id + "." + SUFFIX;
                     Node usersParentNode = getUsersParentNode();
 
                     // Check for .iml suffix in order to stay backwards compatible
                     if (!usersParentNode.hasNode(nodeName)) {
                         nodeName = id + "." + DEPRECATED_SUFFIX;
                     }
 
                     return constructUser(this.identityManager, usersParentNode.getNode(nodeName));
                 } catch (Exception e) {
                     log.error(e, e);
                     throw new AccessManagementException(e.getMessage());
                 }
 
                 // Refresh memory
                 //loadUsers();
                 // Get user from memory (what if null?)
                 //return (User) this.users.get(id);
             }
             log.warn("No such user (neither within memory nor within persistent repository): " + id);
             return null;
         }
 
         return (User) this.users.get(id);
     }
 
     /**
      * @see org.wyona.security.core.api.UserManager#getUser(java.lang.String, boolean)
      */
     public User getUser(String id, boolean refresh) throws AccessManagementException {
         if(refresh){
             refreshCache(id);
         }
         return getUser(id);
     }
 
     /**
      * @see org.wyona.security.core.api.UserManager#getUsers()
      */
     public User[] getUsers() throws AccessManagementException {
         return (User[]) this.users.values().toArray(new User[this.users.size()]);
     }
 
     /**
      * @see org.wyona.security.core.api.UserManager#getUsers(boolean)
      */
     public User[] getUsers(boolean refresh) throws AccessManagementException {
         if(refresh){
             refreshCache(null);
         }
         return getUsers();
     }
 
     /**
      * @see org.wyona.security.core.api.UserManager#removeUser(java.lang.String)
      */
     public void removeUser(String id) throws AccessManagementException {
         if (!existsUser(id)) {
             throw new AccessManagementException("User " + id + " does not exist.");
         }
         User user = getUser(id);
         Group[] groups = user.getGroups();
         for (int i=0; i<groups.length; i++) {
             groups[i].removeMember(user);
             groups[i].save();
         }
         this.users.remove(id);
         user.delete();
     }
 
     /**
      * Gets the repository node which is the parent node of all user nodes.
      *
      * @return node which is the parent of all user nodes.
      * @throws NoSuchNodeException
      * @throws RepositoryException
      */
     protected Node getUsersParentNode() throws NoSuchNodeException, RepositoryException {
         if (this.identitiesRepository.existsNode("/users")) {
             return this.identitiesRepository.getNode("/users");
         }
 
         log.warn("Fallback to root node (Repository: " + identitiesRepository.getName() + ") for backwards compatibility. Please upgrade by introducing a /users node!");
         return this.identitiesRepository.getNode("/");
     }
     
     /**
      * Refresh a particular user within cache
      */
     private void refreshCache(String userId) throws AccessManagementException {
         // TODO: Only user with ID 'userId' actually would need to be refreshed!
         log.warn("Reloading all users in order to refresh cache!");
         loadUsers();
        log.warn("TODO: Refresh of group manager!");
         ((YarepGroupManager)identityManager.getGroupManager()).loadGroups();
     }
     
     /**
      * Override in subclasses
      */
     protected User constructUser(IdentityManager identityManager, Node node) throws AccessManagementException{
         return new YarepUser(this, identityManager.getGroupManager(), node);
     }
 }
