 package org.wyona.security.impl;
 
 import java.util.Hashtable;
 
 import org.wyona.commons.io.Path;
 import org.wyona.commons.io.PathUtil;
 import org.wyona.security.core.AuthorizationException;
 import org.wyona.security.core.api.Identity;
 import org.wyona.security.core.api.Policy;
 import org.wyona.security.core.api.PolicyManager;
 import org.wyona.security.core.api.Role;
 import org.wyona.security.core.api.Usecase;
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
 
     private Repository policiesRepository;
     private DefaultConfigurationBuilder configBuilder;
 
     /**
      *
      */
     public PolicyManagerImpl(Repository policiesRepository) {
         this.policiesRepository = policiesRepository;
         configBuilder = new DefaultConfigurationBuilder();
     }
     
     /**
      * Get policies repository
      */
      public Repository getPoliciesRepository() {
            return policiesRepository;
      }
      
     /**
      * @deprecated
      */
     public boolean authorize(Path path, Identity identity, Role role) throws AuthorizationException {
         return authorize(path.toString(), identity, role);
     }
      
     /**
      *
      */
     public boolean authorize(Policy policy, Identity identity, Usecase usecase) throws AuthorizationException {
         log.error("Not implemented yet!");
         return false;
     }
 
     /**
      *
      */
     public boolean authorize(String path, Identity identity, Usecase usecase) throws AuthorizationException {
         Role role = new Role(usecase.getName());
         return authorize(path, identity, role);
     }
    
     /**
      * @deprecated
      */
     public boolean authorize(String path, Identity identity, Role role) throws AuthorizationException {
         if(path == null || identity == null || role == null) {
             log.error("Path or identity or role is null! [" + path + ", " + identity + ", " + role + "]");
             throw new AuthorizationException("Path or identity or role is null! [" + path + ", " + identity + ", " + role + "]");
         }
 
         try {
             return authorize(policiesRepository, path, identity, role);
         } catch(Exception e) {
             log.error(e.getMessage(), e);
             throw new AuthorizationException("Error authorizing " + policiesRepository.getID() + 
                     ", " + path + ", " + identity + ", " + role, e);
         }
     }
 
     /**
      *
      */
     private boolean authorize(Repository repo, String path, Identity identity, Role role) throws Exception {
         if(repo == null) {
             log.error("Repo is null!");
             throw new Exception("Repo is null!");
         } else if(path == null) {
             log.error("Path is null!");
             throw new Exception("Path is null!");
         } else if(identity == null) {
             log.error("Identity is null!");
             throw new Exception("Identity is null!");
         } else if(role == null) {
             log.error("Role is null!");
             throw new Exception("Role is null!");
         }
 
         String yarepPath = getPolicyPath(path); 
         log.debug("Policy Yarep Path: " + yarepPath + ", Original Path: " + path + ", Repo: " + repo);
         if (repo.existsNode(yarepPath)) {
             try {
                 Configuration config = configBuilder.build(repo.getNode(yarepPath).getInputStream());
                 boolean useInheritedPolicies = config.getAttributeAsBoolean("use-inherited-policies", true);
             Configuration[] roles = config.getChildren("role");
             for (int i = 0; i < roles.length; i++) {
                 String roleName = roles[i].getAttribute("id", null);
                 if (roleName != null && roleName.equals(role.getName())) {
                     boolean useInheritedRolePolicies = roles[i].getAttributeAsBoolean("use-inherited-policies", true);
                     Configuration[] accreditableObjects = roles[i].getChildren();
 
                     boolean worldCredentialExists = false;
                     boolean worldIsNotAuthorized = true;
                     for (int k = 0; k < accreditableObjects.length; k++) {
                         String aObjectName = accreditableObjects[k].getName();
                         log.debug("Accreditable Object Name: " + aObjectName);
 
                         if (aObjectName.equals("world")) {
                             worldCredentialExists = true;
                             String permission = accreditableObjects[k].getAttribute("permission", null);
                             if (permission.equals("true")) {
                                 log.debug("Access granted: " + path);
                                 worldIsNotAuthorized = false;
                                 return true;
                             } else {
                                 worldIsNotAuthorized = true;
                             }
                         } else if (aObjectName.equals("group")) {
                             if (identity.getGroupnames() != null) {
                                 String groupName = accreditableObjects[k].getAttribute("id", null);
                                 String[] groupnames = identity.getGroupnames();
                                 if (groupnames != null) {
                                     for (int j = 0; j < groupnames.length; j++) {
                                         if (groupName.equals(groupnames[j])) {
                                             String permission = accreditableObjects[k].getAttribute("permission", null);
                                             if (permission.equals("true")) {
                                                 log.debug("Access granted: Path = " + path + ", Group = " + groupName);
                                                 return true;
                                             } else {
                                                 log.debug("Access denied: Path = " + path + ", Group = " + groupName);
                                                 return false;
                                             }
                                         }
                                     }
                                 }
                             }
                         } else if (aObjectName.equals("user")) {
                             if (identity.getUsername() != null) {
                                 String userName = accreditableObjects[k].getAttribute("id", null);
                                 if (userName.equals(identity.getUsername())) {
                                     String permission = accreditableObjects[k].getAttribute("permission", null);
                                     if (permission.equals("true")) {
                                         log.debug("Access granted: Path = " + path + ", User = " + userName);
                                         return true;
                                     } else {
                                         log.debug("Access denied: Path = " + path + ", User = " + userName);
                                         return false;
                                     }
                                 }
                             }
                         } else if (aObjectName.equals("iprange")) {
                             log.warn("Credential IP Range not implemented yet!");
                             //return false;
                         } else {
                             log.warn("No such accreditable object implemented: " + aObjectName);
                             //return false;
                         }
                     }
                     if (worldCredentialExists && worldIsNotAuthorized) {
                        log.debug("Access for world denied: " + path);
                        return false;
                     }
                     if (!useInheritedRolePolicies){
                         log.debug("Policy inheritance disabled for role:" + roleName + ". Access denied: "+ path);
                         return false;
                     }
                 }
             }
                 if (!useInheritedPolicies) {
                     log.debug("Policy inheritance disabled. Access denied: "+ path);
                     return false;
                 }
             } catch(NoSuchNodeException e) {
                 log.error(e.getMessage(), e);
             }
         } else {
             if (yarepPath.equals("/.policy")) {
                 log.warn("No such node: " + yarepPath + " (" + repo + ")");
             } else {
                 if (log.isDebugEnabled()) log.debug("No such node: " + yarepPath + " (Fallback to parent policy ...)");
             }
         }
 
         String parent = PathUtil.getParent(path);
         if (parent != null) {
             // Check policy of parent in order to inherit credentials ...
             log.debug("Check parent policy: " + parent + " ... (Current path: " + path + ")");
             return authorize(repo, parent, identity, role);
         } else {
             log.warn("Trying to get parent of " + path + " (" + repo + ") failed. Access denied.");
             return false;
         }
     }
 
     /**
      * Append '.policy' to path as suffix
      */
     private String getPolicyPath(String path) {
         // Remove trailing slash except for ROOT ...
         if (path.length() > 1 && path.charAt(path.length() - 1) == '/') {
             return path.substring(0, path.length() - 1) + ".policy";
         }
         return path + ".policy";
     }
 
     /**
      *
      */
     public Policy getPolicy(String path, boolean aggregated) throws AuthorizationException {
         try {
             if (getPoliciesRepository().existsNode(getPolicyPath(path))) {
                 return new PolicyImplVersion1(getPoliciesRepository().getNode(getPolicyPath(path)).getInputStream());
             } else {
                if (aggregated) {
                    if (!path.equals("/")) {
                        log.warn("No policy found for '" + path + "'. Check for parent '" + PathUtil.getParent(path) + "'.");
                        return getPolicy(PathUtil.getParent(path), aggregated);
                    } else {
                        log.warn("No policies found at all, not even a root policy!");
                        return null;
                    }
                } else {
                    log.warn("Aggregated has been set to false, hence do not check for parent policies!");
                    return null;
                }
             }
         } catch(Exception e) {
             log.error(e, e);
             throw new AuthorizationException(e.getMessage());
         }
     }
 }
