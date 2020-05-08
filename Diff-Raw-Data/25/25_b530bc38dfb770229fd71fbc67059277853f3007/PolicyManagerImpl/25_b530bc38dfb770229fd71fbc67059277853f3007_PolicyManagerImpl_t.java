 package org.wyona.security.impl;
 
 import org.wyona.commons.io.Path;
 import org.wyona.security.core.api.Identity;
 import org.wyona.security.core.api.PolicyManager;
 import org.wyona.security.core.api.Role;
 import org.wyona.yarep.core.NoSuchNodeException;
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
 public class PolicyManagerImpl implements PolicyManager {
 
     private static Category log = Category.getInstance(PolicyManagerImpl.class);
 
     private RepositoryFactory repoFactory;
     private DefaultConfigurationBuilder configBuilder;
 
     /**
      *
      */
     public PolicyManagerImpl() {
         try {
             repoFactory = new RepositoryFactory("ac-policies-yarep.properties");
             configBuilder = new DefaultConfigurationBuilder();
         } catch(Exception e) {
             log.error(e.getMessage(), e);
         }
     }
 
     /**
     * TODO: Implement inherited policies (recursive authorize(path.getParent(), identity, role))
      */
     public boolean authorize(Path path, Identity identity, Role role) {
        if(path == null || identity == null || role == null) {
            log.warn("Path or identity or role was null!");
            return false;
        }
 
         try {
            RepoPath rp = new YarepUtil().getRepositoryPath(new org.wyona.yarep.core.Path(path.toString()), repoFactory);
             Repository repo = rp.getRepo();
 
            Configuration config = configBuilder.build(repo.getInputStream(new org.wyona.yarep.core.Path(getPolicyPath(new Path(rp.getPath().toString())).toString())));
             Configuration[] roles = config.getChildren("role");
             for (int i = 0; i < roles.length; i++) {
                 String roleName = roles[i].getAttribute("id", null);
                 if (roleName != null && roleName.equals(role.getName())) {
                     Configuration[] accreditableObjects = roles[i].getChildren();
                     for (int k = 0; k < accreditableObjects.length; k++) {
                         String aObjectName = accreditableObjects[k].getName();
                         log.error("DEBUG: Accreditable Object Name: " + aObjectName);
                         if (aObjectName.equals("world")) {
                             String permission = accreditableObjects[k].getAttribute("permission", null);
                             if (permission.equals("true")) {
                                 log.error("DEBUG: Access granted: " + path);
                                 return true;
                             }
                         } else if (aObjectName.equals("group")) {
                             String permission = accreditableObjects[k].getAttribute("permission", null);
                             if (permission.equals("true")) {
                                 String groupName = accreditableObjects[k].getAttribute("id", null);
                                 String[] groupnames = identity.getGroupnames();
                                if (groupnames != null) {
                                    for (int j = 0; j < groupnames.length; j++) {
                                        if (groupName.equals(groupnames[j])) {
                                            log.error("DEBUG: Access granted: " + path);
                                            return true;
                                        }
                                     }
                                 }
                             }
                         } else if (aObjectName.equals("user")) {
                             String permission = accreditableObjects[k].getAttribute("permission", null);
                             if (permission.equals("true")) {
                                 String userName = accreditableObjects[k].getAttribute("id", null);
                                 if (userName.equals(identity.getUsername())) {
                                     log.error("DEBUG: Access granted: " + path);
                                     return true;
                                 }
                             }
                         } else if (aObjectName.equals("group")) {
                             log.warn("IP Range not implemented yet!");
                         } else {
                             log.warn("No such accreditable object implemented: " + aObjectName);
                         }
                     }
                 }
             }
         } catch(NoSuchNodeException e) {
             log.warn(e.getMessage());
         } catch(Exception e) {
             log.error(e.getMessage(), e);
         }
         log.error("DEBUG: Access denied: " + path);
         return false;
     }
 
     /**
      *
      */
     public Path getPolicyPath(Path path) {
         // Remove trailing slash except for ROOT ...
         String p = path.toString();
         if (p.length() > 1 && p.charAt(p.length() - 1) == '/') {
             return new Path(p.substring(0, p.length() - 1) + ".policy");
         }
         return new Path(p + ".policy");
     }
 }
