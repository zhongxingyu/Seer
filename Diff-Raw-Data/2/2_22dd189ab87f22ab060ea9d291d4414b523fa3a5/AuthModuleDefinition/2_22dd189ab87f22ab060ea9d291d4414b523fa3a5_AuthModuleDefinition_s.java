 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.services.core.security.auth.modules.impl.descriptor;
 
 import org.gridlab.gridsphere.core.persistence.castor.descriptor.ConfigParam;
 import org.gridlab.gridsphere.services.core.security.auth.modules.LoginAuthModule;
 import org.gridlab.gridsphere.services.core.security.auth.AuthorizationException;
 import org.gridlab.gridsphere.portlet.User;
 
 import java.util.*;
 
 /**
  * The <code>AuthModuleDefinition</code> defines
  */
 public class AuthModuleDefinition {
 
     protected AuthModulesDescriptor authDescriptor = null;
     private String oid = null;
     protected String moduleName = "";
     private List moduleDescriptions = new Vector();
     private List moduleErrors = new Vector();
 
     protected int priority = 100;
     protected String moduleImplementation = "";
     protected boolean moduleActive = false;
 
     protected List configParamList = new Vector();
     protected Map attributes = new HashMap();
     protected Properties configProps = null;
 
     public String getOid() {
         return oid;
     }
 
     public void setOid(String oid) {
         this.oid = oid;
     }
 
     /**
      * Sets the auth module descriptor
      *
      * @param descriptor the auth module descriptor
      */
     public void setDescriptor(AuthModulesDescriptor descriptor) {
         this.authDescriptor = descriptor;
     }
 
     /**
      * Returns the auth module descriptor
      *
      * @return descriptor the auth module descriptor
      */
     public AuthModulesDescriptor getDescriptor() {
         return authDescriptor;
     }
 
     /**
      * Sets the portlet service name
      *
      * @param moduleName the portlet service name
      */
     public void setModuleName(String moduleName) {
         this.moduleName = moduleName;
     }
 
     /**
      * Returns the portlet service name
      *
      * @return the portlet service name
      */
     public String getModuleName() {
         return this.moduleName;
     }
 
     /**
      * Sets the list of module descriptions
      *
      * @param moduleDescriptions the list of module descriptions
      */
     public void setModuleDescriptions(List moduleDescriptions) {
         this.moduleDescriptions = moduleDescriptions;
     }
 
     /**
      * Returns the module descriptions
      *
      * @return the module descriptions
      */
     public List getModuleDescriptions() {
         return this.moduleDescriptions;
     }
 
     /**
      * Sets the list of module errors
      *
      * @param moduleErrors the list of module errors
      */
     public void setModuleErrors(List moduleErrors) {
         this.moduleErrors = moduleErrors;
     }
 
     /**
      * Returns the module errors
      *
      * @return the module errors
      */
     public List getModuleErrors() {
         return this.moduleErrors;
     }
 
     /**
      * Returns the portlet service implementation
      *
      * @return the portlet service implementation
      */
     public String getModuleImplementation() {
         return this.moduleImplementation;
     }
 
     /**
      * Sets the portlet service implementation
      *
      * @param moduleImplementation the portlet service implementation
      */
     public void setModuleImplementation(String moduleImplementation) {
         this.moduleImplementation = moduleImplementation;
     }
 
     /**
      * Returns the module priority
      *
      * @return the module priority
      */
     public int getModulePriority() {
         return priority;
     }
 
     /**
      * Sets the module priority
      *
      * @param priority
      */
     public void setModulePriority(int priority) {
         this.priority = priority;
     }
 
     /**
      * Returns true of this module is turned on for all users
      *
      * @return true of this module is turned on for all users
      */
     public boolean getModuleActive() {
         return moduleActive;
     }
 
     /**
      * If true, this module will be active for all users
      *
      * @param moduleActive if true, this module will be active for all users
      */
     public void setModuleActive(boolean moduleActive) {
         this.moduleActive = moduleActive;
     }
 
     /**
      * Sets the service configuration parameter list
      *
      * @param configParamList the configuration parameter list
      */
     public void setConfigParamList(List configParamList) {
         this.configParamList = configParamList;
     }
 
     /**
      * Returns the service configuration parameter list
      *
      * @return the configuration parameter list
      */
     public List getConfigParamList() {
         return this.configParamList;
     }
 
     public String getAttribute(String name) {
         return (String) attributes.get(name);
     }
 
     public Map getAttributes() {
         return attributes;
     }
 
     public void setAttributes(Map attributes) {
         this.attributes = attributes;
     }
 
     /**
      * Creates a properties from the  ConfigParamList
      *
      * @see #getConfigParamList
      */
     private void createProperties() {
         configProps = new Properties();
         Iterator it = this.configParamList.iterator();
         ConfigParam param;
         while (it.hasNext()) {
             param = (ConfigParam) it.next();
             configProps.setProperty(param.getParamName(), param.getParamValue());
         }
     }
 
     /**
      * Return the configuration properties
      *
      * @return the configuration properties
      */
     public Properties getConfigProperties() {
         if (configProps == null)
             createProperties();
         return configProps;
     }
 
     /**
      * Sets the configuration properties
      *
      * @param props the configuration properties
      */
     public void setConfigProperties(Properties props) {
         Enumeration e = props.keys();
         if (!props.isEmpty()) {
             configParamList = new Vector();
         }
         while (e.hasMoreElements()) {
             String key = (String) e.nextElement();
             ConfigParam param = new ConfigParam(key, props.getProperty(key));
             configParamList.add(param);
         }
     }
 
     /**
      * Returns a <code>String</code> representation if this auth module
      * definition
      *
      * @return the auth module definition as a <code>String</code>
      */
     public String toString() {
         StringBuffer sb = new StringBuffer("\n");
         sb.append("auth module name: " + this.moduleName + "\n");
         sb.append("auth module description: " + this.moduleDescriptions.get(0) + "\n");
        sb.append("auth module error: " + this.moduleErrors.get(0) + "\n");
         sb.append("auth module implementation: " + this.moduleImplementation + "\n");
         sb.append("auth module priority: " + this.priority + "\n");
         sb.append("config properties: ");
         Iterator it = this.configParamList.iterator();
         ConfigParam c;
         while (it.hasNext()) {
             c = (ConfigParam) it.next();
             sb.append("\tname: " + c.getParamName() + "\tvalue: " + c.getParamValue());
         }
         return sb.toString();
     }
 
 }
