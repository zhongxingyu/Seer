 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet.impl;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portletcontainer.ApplicationPortletConfig;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.Hashtable;
 import java.util.List;
import java.io.File;
 
 /**
  * The <code>SportletConfig</code> class provides the portlet with its
  * configuration. The configuration holds information about the portlet that
  * is valid for all users, and is maintained by the administrator. The portlet
  * can therefore only read the configuration. Only when the portlet is in
  * <code>CONFIGURE</code> mode, it has write access to the configuration data
  * (the rest of the configuration is managed by the portlet container directly).
  */
 public class SportletConfig implements PortletConfig {
 
     private static PortletLog log = SportletLog.getInstance(SportletConfig.class);
 
     private ServletConfig servletConfig = null;
     private PortletContext context = null;
     private String portletName = null;
     private String groupName = null;
     private List supportedModes = new ArrayList();
     private List allowedStates = new ArrayList();
     private Hashtable configs = new Hashtable();
 
     /**
      * Cannot instantiate uninitialized SportletConfig
      */
     private SportletConfig() {}
 
     /**
      * Constructs an instance of PortletConfig from a servlet configuration
      * object and an application portlet descriptor
      *
      * @param servletConfig a <code>ServletConfig</code>
      * @param appConfig a <code>ApplicationSportletConfig</code>
      */
     public SportletConfig(ServletConfig servletConfig, ApplicationPortletConfig appConfig) {
         this.servletConfig = servletConfig;
         this.context = new SportletContext(servletConfig);
 
         // set portlet modes
         supportedModes = appConfig.getSupportedModes();
 
         // set allowed states info
         allowedStates = appConfig.getAllowedWindowStates();
 
         // set portlet config information
         configs = appConfig.getConfigParams();
 
         portletName = appConfig.getPortletName();
 
         // the group name is the web application name which can be found from
         // the context path
         String ctxPath = context.getRealPath("");
        int i = ctxPath.lastIndexOf(File.separator);
         groupName = ctxPath.substring(i+1);
 
         //this.logConfig();
     }
 
     /**
      * Returns the portlet context.
      *
      * @return the portlet context
      */
     public PortletContext getContext() {
         return context;
     }
 
     /**
      * Returns the name of the portlet.
      *
      * @return the name of the portlet
      */
     public String getName() {
         return portletName;
     }
 
     /**
      * Returns the group name associated with the portlet
      *
      * @return the group name associated with the portlet
      */
     public String getGroupName() {
         return groupName;
     }
 
     /**
      * Returns whether the portlet supports the given mode for the given client.
      *
      * @param mode the portlet mode
      *
      * @return <code>true</code> if the window supports the given state,
      * <code>false</code> otherwise
      */
     public boolean supports(Portlet.Mode mode) {
         return (supportedModes.contains(mode) ? true : false);
     }
 
     /**
      * Returns whether the portlet window supports the given state
      *
      * @param state the portlet window state
      * @return <code>true</code> if the window supports the given state,
      * <code>false</code> otherwise
      */
     public boolean supports(PortletWindow.State state) {
         return (allowedStates.contains(state) ? true : false);
     }
 
     public final String getInitParameter(String name) {
         return (String) configs.get(name);
     }
 
     public final Enumeration getInitParameterNames() {
         return configs.keys();
     }
 
     public final ServletContext getServletContext() {
         return servletConfig.getServletContext();
     }
 
     public final String getServletName() {
         return servletConfig.getServletName();
     }
 
     public void logConfig() {
         String name, paramvalue, vals;
         Object attrvalue;
         Enumeration enum, eenum;
 
         log.debug("PortletConfig Information");
         log.debug("portlet name: " + this.getName());
         log.debug("portlet group name: " + this.getGroupName());
         log.debug("servlet name: " + this.getServletName());
         log.debug("config init parameters: ");
         enum = this.getInitParameterNames();
         while (enum.hasMoreElements()) {
             name = (String) enum.nextElement();
             vals = getInitParameter(name);
             log.debug("\t\tname=" + name + " value=" + vals);
         }
         log.debug("PortletContext Information:");
         log.debug("Container Info: " + context.getContainerInfo());
         log.debug("major version: " + context.getMajorVersion() + " minor version: " + context.getMinorVersion());
         /*
         log.info("Server Info: " + context.getServerInfo());
         enum = context.getAttributeNames();
         while (enum.hasMoreElements()) {
             name = (String) enum.nextElement();
             attrvalue = (Object) context.getAttribute(name);
             log.debug("\t\tname=" + name + " object type=" + attrvalue.getClass().getName());
         }
 
         log.info("context init parameters: ");
         enum = context.getInitParameterNames();
         while (enum.hasMoreElements()) {
             name = (String) enum.nextElement();
             vals = context.getInitParameter(name);
             log.info("\t\tname=" + name + " value=" + vals);
         }
        */
     }
 }
