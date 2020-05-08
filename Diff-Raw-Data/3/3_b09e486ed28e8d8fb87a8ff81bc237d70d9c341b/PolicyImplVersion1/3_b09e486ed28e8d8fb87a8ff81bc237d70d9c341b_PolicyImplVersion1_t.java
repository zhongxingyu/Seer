 package org.wyona.security.impl;
 
 import org.wyona.security.core.GroupPolicy;
 import org.wyona.security.core.IdentityPolicy;
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
  * @deprecated
  */
 public class PolicyImplVersion1 implements Policy {
 
     private static Logger log = Logger.getLogger(PolicyImplVersion1.class);
     protected DefaultConfigurationBuilder builder = null;
     protected Vector usecasePolicies = null;
     protected boolean useInheritedPolicies = true;
 
     private static String USECASE_ELEMENT_NAME = "role";
 
     /**
      *
      */
     public PolicyImplVersion1() throws Exception {
         this.usecasePolicies = new Vector();
     }
 
     /**
      *
      */
     public PolicyImplVersion1(java.io.InputStream in) throws Exception {
         boolean enableNamespaces = true;
         builder = new DefaultConfigurationBuilder(enableNamespaces);
         Configuration config = builder.build(in);
 
         String useInheritedPoliciesString = config.getAttribute("use-inherited-policies", "true");
         if (useInheritedPoliciesString.equals("false")) useInheritedPolicies = false;
 
         Configuration[] upConfigs = config.getChildren(USECASE_ELEMENT_NAME);
         usecasePolicies = new Vector();
         for (int i = 0; i < upConfigs.length; i++) {
             usecasePolicies.add(readUsecasePolicy(upConfigs[i]));
         }
     }
 
     public UsecasePolicy[] getUsecasePolicies() {
         UsecasePolicy[] ups = new UsecasePolicy[usecasePolicies.size()];
         for (int i = 0; i < ups.length; i++) {
             ups[i] = (UsecasePolicy) usecasePolicies.elementAt(i);
         }
         return ups;
     }
 
     public void addUsecasePolicy(UsecasePolicy up) throws AccessManagementException {
         usecasePolicies.add(up);
     }
 
     /**
      * @see
      */
     public String getPath() throws AccessManagementException {
         log.warn("Not implemented yet!");
         return null;
     }
 
     public Policy getParentPolicy() throws AccessManagementException {
         log.warn("Not implemented yet!");
         return null;
     }
 
     public String toString() {
         StringBuffer sb = new StringBuffer("Policy:\n");
         UsecasePolicy[] ups = getUsecasePolicies();
         for (int i = 0; i < ups.length; i++) {
             sb.append("  Usecase: " + ups[i].getName() + "\n");
             IdentityPolicy[] idps = ups[i].getIdentityPolicies();
             for (int j = 0; j < idps.length; j++) {
                 if (idps[j].getIdentity().isWorld()) {
                     sb.append("    WORLD (" + idps[j].getPermission() + ")\n");
                 } else {
                     sb.append("    User: " + idps[j].getIdentity().getUsername() + " (" + idps[j].getPermission() + ")\n");
                 }
             }
             GroupPolicy[] gps = ups[i].getGroupPolicies();
             for (int j = 0; j < gps.length; j++) {
                 sb.append("    Group: " + gps[j].getId() + " (" + gps[j].getPermission() + ")\n");
             }
         }
         return sb.toString();
     }
 
     /**
      *
      */
     protected UsecasePolicy readUsecasePolicy(Configuration upConfig) throws Exception {
             UsecasePolicy up = new UsecasePolicy(upConfig.getAttribute("id"));
 
             up.setUseInheritedPolicies(upConfig.getAttributeAsBoolean("use-inherited-policies", true));
             
             Configuration[] worldConfigs = upConfig.getChildren("world");
             if (worldConfigs.length > 1) log.warn("Usecase policy contains more than one WORLD entry!");
             for (int j = 0; j < worldConfigs.length; j++) {
                 String permission = worldConfigs[j].getAttribute("permission", "true");
                 up.addIdentity(new Identity(), new Boolean(permission).booleanValue());
             }
 
             Configuration[] userConfigs = upConfig.getChildren("user");
             for (int j = 0; j < userConfigs.length; j++) {
                 String permission = userConfigs[j].getAttribute("permission", "true");
                String id = userConfigs[j].getAttribute("id");
                up.addIdentity(new Identity(id, id), new Boolean(permission).booleanValue());
             }
 
             Configuration[] groupConfigs = upConfig.getChildren("group");
             for (int j = 0; j < groupConfigs.length; j++) {
                 String permission = groupConfigs[j].getAttribute("permission", "true");
                 String id = groupConfigs[j].getAttribute("id");
                 if (permission != null) {
                     up.addGroupPolicy(new GroupPolicy(id, new Boolean(permission).booleanValue()));
                 } else {
                     up.addGroupPolicy(new GroupPolicy(id, true));
                 }
             }
         return up;
     }
 
     /**
      *
      */
     public boolean useInheritedPolicies() {
         return useInheritedPolicies;
     }
 
     public void setUseInheritedPolicies(boolean useInheritedPolicies) {
         this.useInheritedPolicies = useInheritedPolicies;
     }
     
     public void removeUsecasePolicy(String name) throws AccessManagementException {
         for (int i = 0; i < usecasePolicies.size(); i++) {
             UsecasePolicy up = (UsecasePolicy)usecasePolicies.elementAt(i);
             if (up.getName().equals(name)) {
                 usecasePolicies.remove(i);
                 return;
             }
         }
     }
 
     public UsecasePolicy getUsecasePolicy(String name) throws AccessManagementException {
         for (int i = 0; i < usecasePolicies.size(); i++) {
             UsecasePolicy up = (UsecasePolicy)usecasePolicies.elementAt(i);
             if (up.getName().equals(name)) {
                 return (UsecasePolicy)usecasePolicies.elementAt(i);
             }
         }
         return null;
     }
 
 }
 
