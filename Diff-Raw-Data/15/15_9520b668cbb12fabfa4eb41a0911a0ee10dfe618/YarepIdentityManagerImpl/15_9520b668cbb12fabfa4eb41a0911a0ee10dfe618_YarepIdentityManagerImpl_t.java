 package org.wyona.security.impl.yarep;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 import org.apache.log4j.Logger;
 import org.wyona.security.core.AuthenticationException;
 import org.wyona.security.core.api.AccessManagementException;
 import org.wyona.security.core.api.GroupManager;
 import org.wyona.security.core.api.IdentityManager;
 import org.wyona.security.core.api.UserManager;
 import org.wyona.yarep.core.Repository;
 
 /**
  *
  */
 public class YarepIdentityManagerImpl implements IdentityManager {
     protected static Logger log = Logger.getLogger(YarepIdentityManagerImpl.class);
 
     protected Repository identitiesRepository;
     protected UserManager userManager;
     protected GroupManager groupManager;
 
     //TODO: the field should be used somewhere?
     private static String CONFIG= "ac-identities-yarep.properties";
 
     /**
      * No initialization, subclasses should use configure()
     */
    protected YarepIdentityManagerImpl() {
     }
     
     /**
      *  Basic initialization
      */
    public YarepIdentityManagerImpl(Repository identitiesRepository) throws AccessManagementException {
         this.identitiesRepository = identitiesRepository;
         userManager = new YarepUserManager(this, identitiesRepository);
         groupManager = new YarepGroupManager(this, identitiesRepository);
     }
     
    /**
     *
     */
     protected void configure(Configuration config) throws ConfigurationException, AccessManagementException{
        log.warn("Configurable identity managers should override this method!");
     }
     
     /**
      * @deprecated
      */
     public boolean authenticate(String username, String password) throws AuthenticationException {
         try {
             return this.userManager.getUser(username).authenticate(password);
         } catch (Exception e) {
             log.error(e.getMessage(), e);
             throw new AuthenticationException(e);
         }
     }
 
 
     public GroupManager getGroupManager() {
         return this.groupManager;
     }
 
 
     public UserManager getUserManager() {
         return this.userManager;
     }
 }
