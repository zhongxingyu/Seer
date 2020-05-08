 package org.wyona.security.util;
 
 import org.wyona.security.core.GroupPolicy;
 import org.wyona.security.core.UsecasePolicy;
 import org.wyona.security.core.api.AccessManagementException;
 import org.wyona.security.core.api.Group;
 import org.wyona.security.core.api.Identity;
 import org.wyona.security.core.api.Policy;
 
 import org.apache.log4j.Logger;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 
 import java.util.Vector;
 
 /**
  * Allows to parse an XML such as for example
  * <policy xmlns="http://www.wyona.org/security/1.0" use-inherited-policies="false">
  *   <world>
  *     <right id="view">Read</right>
  *     <right id="write">Write</right>
  *   </world>
  *   <user id="http://michaelwechner.livejournal.com/">
  *     <right id="open"/>
  *     <right id="write"/>
  *   </user>
  *   <group id="admin">
  *     <right id="open"/>
  *     <right id="write"/>
  *   </group>
  * </policy>
  */
 public class PolicyParser implements Policy {
 
     private static Logger log = Logger.getLogger(PolicyParser.class);
     protected DefaultConfigurationBuilder builder = null;
     protected Vector usecasePolicies = null;
     protected boolean useInheritedPolicies = true;
 
     /**
      *
      */
     public PolicyParser() throws Exception {
         usecasePolicies = new Vector();
     }
 
     /**
      *
      */
     public Policy parseXML(java.io.InputStream in) throws Exception {
         boolean enableNamespaces = true;
         builder = new DefaultConfigurationBuilder(enableNamespaces);
         Configuration config = builder.build(in);
 
         String useInheritedPoliciesString = config.getAttribute("use-inherited-policies", "true");
         if (useInheritedPoliciesString.equals("false")) useInheritedPolicies = false;
 
         // TODO: Check for world
 
         Configuration[] userConfigs = config.getChildren("user");
         for (int i = 0; i < userConfigs.length; i++) {
             Configuration[] rightConfigs = userConfigs[i].getChildren("right");
             for (int k = 0; k < rightConfigs.length; k++) {
                 UsecasePolicy up = new UsecasePolicy(rightConfigs[k].getAttribute("id"));
                up.addIdentity(new Identity(userConfigs[i].getAttribute("id"), null));
                 addUsecasePolicy(up);
             }
         }
 
         Configuration[] groupConfigs = config.getChildren("group");
         for (int i = 0; i < groupConfigs.length; i++) {
             Configuration[] rightConfigs = groupConfigs[i].getChildren("right");
             for (int k = 0; k < rightConfigs.length; k++) {
                 UsecasePolicy up = new UsecasePolicy(rightConfigs[k].getAttribute("id"));
                up.addGroupPolicy(new GroupPolicy(groupConfigs[i].getAttribute("id"), true));
                 addUsecasePolicy(up);
             }
         }
 
         // TODO: Check for hosts
 
         return this;
     }
 
     /**
      * @see
      */
     public UsecasePolicy[] getUsecasePolicies() {
         UsecasePolicy[] ups = new UsecasePolicy[usecasePolicies.size()];
         for (int i = 0; i < ups.length; i++) {
             ups[i] = (UsecasePolicy) usecasePolicies.elementAt(i);
         }
         return ups;
     }
 
     /**
      * @see
      */
     public void addUsecasePolicy(UsecasePolicy up) throws AccessManagementException {
         UsecasePolicy existingUsecasePolicy = null;
 
         for (int i = 0; i < usecasePolicies.size(); i++) {
             UsecasePolicy existingUP = (UsecasePolicy) usecasePolicies.elementAt(i);
             if (existingUP.getName().equals(up.getName())) {
                 existingUsecasePolicy = existingUP;
                 break;
             }
         }
 
         if (existingUsecasePolicy != null) {
             // Merge identities and groups
            Identity[] identities = up.getIdentities();
            for (int k = 0; k < identities.length; k++) {
                 boolean identityExists = false;
                Identity[] existingIdentities = existingUsecasePolicy.getIdentities();
                for (int i = 0; i < existingIdentities.length; i++) {
                    if (identities[k].getUsername().equals(existingIdentities[i].getUsername())) {
                         identityExists = true;
                         break;
                     }
                 }
                 if (!identityExists) {
                    existingUsecasePolicy.addIdentity(new Identity(identities[k].getUsername(), null));
                 }
             }
 
             GroupPolicy[] groups = up.getGroupPolicies();
             for (int k = 0; k < groups.length; k++) {
                 boolean groupExists = false;
                 GroupPolicy[] existingGroups = existingUsecasePolicy.getGroupPolicies();
                 for (int i = 0; i < existingGroups.length; i++) {
                     if (groups[k].getId().equals(existingGroups[i].getId())) {
                         groupExists = true;
                         break;
                     }
                 }
                 if (!groupExists) {
                     existingUsecasePolicy.addGroupPolicy(new GroupPolicy(groups[k].getId()));
                 }
             }
         } else {
             usecasePolicies.add(up);
             log.info("New usecase policy has been added: " + up.getName());
         }
     }
 
     /**
      * @see
      */
     public Policy getParentPolicy() throws AccessManagementException {
         log.warn("Not implemented yet!");
         return null;
     }
 
     /**
      * @see
      */
     public boolean useInheritedPolicies() {
         return useInheritedPolicies;
     }
 
     /**
      *
      */
 /*
     protected UsecasePolicy readUsecasePolicy(Configuration upConfig) throws Exception {
             UsecasePolicy up = new UsecasePolicy(upConfig.getAttribute("id"));
 
             Configuration[] worldConfigs = upConfig.getChildren("world");
             if (worldConfigs.length > 1) log.warn("Usecase policy contains more than one WORLD entry!");
             for (int j = 0; j < worldConfigs.length; j++) {
                 up.addIdentity(new Identity());
             }
 
             Configuration[] userConfigs = upConfig.getChildren("user");
             for (int j = 0; j < userConfigs.length; j++) {
                 up.addIdentity(new Identity(userConfigs[j].getAttribute("id"), null));
             }
 
             Configuration[] groupConfigs = upConfig.getChildren("group");
             for (int j = 0; j < groupConfigs.length; j++) {
                 String permission = groupConfigs[j].getAttribute("permission");
                 String id = groupConfigs[j].getAttribute("id");
                 if (permission != null) {
                     up.addGroupPolicy(new GroupPolicy(id, getBoolean(permission)));
                 } else {
                     up.addGroupPolicy(new GroupPolicy(id));
                 }
             }
         return up;
     }
 */
 
     /**
      *
      */
 /*
     private boolean getBoolean(String value) {
         if (value.equals("false")) {
             return false;
         } else if (value.equals("true")) {
             return true;
         } else {
             log.error("No such boolean value: " + value);
             return false;
         }
     }
 */
 
     public String toString() {
         StringBuffer sb = new StringBuffer("Policy:\n");
         UsecasePolicy[] ups = getUsecasePolicies();
         for (int i = 0; i < ups.length; i++) {
             sb.append("  Usecase: " + ups[i].getName() + "\n");
             Identity[] ids = ups[i].getIdentities();
             for (int j = 0; j < ids.length; j++) {
                 if (ids[j].isWorld()) {
                     sb.append("    WORLD\n");
                 } else {
                     sb.append("    User: " + ids[j].getUsername() + "\n");
                 }
             }
             GroupPolicy[] gps = ups[i].getGroupPolicies();
             for (int j = 0; j < gps.length; j++) {
                 sb.append("    Group: " + gps[j].getId() + " (" + gps[j].getPermission() + ")\n");
             }
         }
         return sb.toString();
     }
 }
 
