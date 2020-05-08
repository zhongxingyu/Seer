 package org.wyona.security.impl;
 
 import org.wyona.security.core.UsecasePolicy;
 import org.wyona.security.core.api.AccessManagementException;
 import org.wyona.security.core.api.Policy;
 
 import org.apache.log4j.Logger;
 
 import org.apache.avalon.framework.configuration.Configuration;
 import org.apache.avalon.framework.configuration.ConfigurationException;
 import org.apache.avalon.framework.configuration.DefaultConfigurationBuilder;
 
 import java.util.Vector;
 
 /**
  *
  */
 class PolicyImpl implements Policy {
 
     private static Logger log = Logger.getLogger(PolicyImpl.class);
     private DefaultConfigurationBuilder builder = null;
     private Vector usecasePolicies = null;
 
     /**
      *
      */
     public PolicyImpl(java.io.InputStream in) throws Exception {
         log.warn("Implementation not finished yet!");
         boolean enableNamespaces = true;
         builder = new DefaultConfigurationBuilder(enableNamespaces);
         Configuration config = builder.build(in);
        Configuration[] upConfigs = config.getChildren("role");
        //Configuration[] upConfigs = config.getChildren("usecase");
         usecasePolicies = new Vector();
         for (int i = 0; i < upConfigs.length; i++) {
             usecasePolicies.add(new UsecasePolicy(upConfigs[i].getAttribute("id")));
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
         log.warn("Not implemented yet!");
     }
 
     public Policy getParentPolicy() throws AccessManagementException {
         log.warn("Not implemented yet!");
         return null;
     }
 
     public String toString() {
         StringBuffer sb = new StringBuffer("Policy:\n");
         UsecasePolicy[] ups = getUsecasePolicies();
         for (int i = 0; i < ups.length; i++) {
             sb.append("Usecase: " + ups[i].getName() + "\n");
         }
         return sb.toString();
     }
 }
 
