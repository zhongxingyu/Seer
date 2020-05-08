 package org.wyona.security.impl;
 
 import org.wyona.security.core.api.Identity;
 import org.wyona.security.core.api.IdentityManager;
 import org.wyona.yarep.core.Path;
 import org.wyona.yarep.core.Repository;
 import org.wyona.yarep.core.RepositoryFactory;
 import org.wyona.yarep.util.RepoPath;
 import org.wyona.yarep.util.YarepUtil;
 
 import org.apache.log4j.Category;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 
 /**
  *
  */
 public class IdentityManagerImpl implements IdentityManager {
 
     private static Category log = Category.getInstance(IdentityManagerImpl.class);
 
     private RepositoryFactory repoFactory;
     private DefaultConfigurationBuilder configBuilder;
 
     /**
      *
      */
     public IdentityManagerImpl() {
         try {
             repoFactory = new RepositoryFactory("ac-identities-yarep.properties");
             configBuilder = new DefaultConfigurationBuilder();
         } catch(Exception e) {
             log.error(e.getMessage(), e);
         }
     }
 
     /**
      *
      */
     public boolean authenticate(String username, String password, String realmID) {
         if(username == null || password == null) {
             log.warn("Username or password is null!");
             return false;
         }
 
         Repository repo = null;
         if (realmID != null) {
             repo = repoFactory.newRepository(realmID);
         } else {
             repo = repoFactory.firstRepository();
         }
 
         if (repo != null) {
             try {
                 Configuration config = configBuilder.build(repo.getInputStream(new Path("/" + username + ".iml")));
                 Configuration passwdConfig = config.getChild("password");
                 if(passwdConfig.getValue().equals(Password.getMD5(password))) {
                     return true;
                 }
             } catch(Exception e) {
                 log.error(e);
             }
         }
 
         return false;
     }
 }
