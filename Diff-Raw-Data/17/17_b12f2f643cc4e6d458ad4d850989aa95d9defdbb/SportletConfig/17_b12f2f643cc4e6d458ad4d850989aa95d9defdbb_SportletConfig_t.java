 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlet.impl;
 
 import org.gridlab.gridsphere.portlet.*;
 
 import org.gridlab.gridsphere.core.persistence.castor.descriptor.ConfigParam;
 import org.gridlab.gridsphere.portletcontainer.descriptor.PortletApp;
 import org.gridlab.gridsphere.portletcontainer.descriptor.SupportsModes;
 import org.gridlab.gridsphere.portletcontainer.descriptor.Markup;
 import org.gridlab.gridsphere.portletcontainer.descriptor.AllowsWindowStates;
 import org.exolab.castor.types.AnyNode;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletContext;
 import java.util.*;
 
 /**
  * The SortletConfig class provides the portlet with its configuration.
  * The configuration holds information about the portlet that is valid for all users,
  * and is maintained by the administrator. The portlet can therefore only read the configuration.
  * Only when the portlet is in CONFIGURE mode, it has write access to the configuration data
  * (the rest of the configuration is managed by the portlet container directly).
  */
 public class SportletConfig implements PortletConfig {
 
     private static PortletLog log = SportletLog.getInstance(SportletConfig.class);
 
     private ServletConfig servletConfig = null;
     private PortletContext context = null;
     private String portletName = null;
     private Map supportedModes = null;
     private List allowedStates = null;
     private Hashtable configs = new Hashtable();
 
     /**
      * Constructor creates a PortletConfig from a ServletConfig
      */
     public SportletConfig(ServletConfig servletConfig, PortletApp portletApp) {
         this.servletConfig = servletConfig;
         this.context = new SportletContext(servletConfig);
         Iterator it;
 
          // set supported modes info
         supportedModes = new Hashtable();
         SupportsModes smodes = portletApp.getSupportsModes();
         List modesList = smodes.getMarkupList();
         it = modesList.iterator();
         while (it.hasNext()) {
             Markup markup = (Markup)it.next();
             String name = markup.getName();
             Iterator modesIt = markup.getPortletModes().iterator();
             List modes = new Vector();
             while (modesIt.hasNext()) {
                 AnyNode anode = (AnyNode)modesIt.next();
                 modes.add(anode.getLocalName().toUpperCase());
             }
             supportedModes.put(name, modes);
         }
 
         // set allowed states info
         AllowsWindowStates states = portletApp.getAllowsWindowStates();
        allowedStates = new Vector();
         it = states.getWindowStates().iterator();
         while (it.hasNext()) {
             AnyNode anode = (AnyNode)it.next();
             String state = anode.getLocalName().toUpperCase();
             allowedStates.add(state);
         }
 
         // set portlet config information
         List configList = portletApp.getConfigParamList();
         it = configList.iterator();
         while (it.hasNext()) {
             ConfigParam configParam = (ConfigParam)it.next();
             configs.put(configParam.getParamName(), configParam.getParamValue());
         }
 
         portletName = portletApp.getPortletName();
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
      * Returns whether the portlet supports the given mode for the given client.
      *
      * @param mode the portlet mode
      * @param client the given client
      *
      * @return true if the window supports the given state, false otherwise
      */
     public boolean supports(Portlet.Mode mode, Client client) {
         String clientMarkup = client.getMarkupName();
         if (supportedModes.containsKey(clientMarkup)) {
             List modes = (List)supportedModes.get(clientMarkup);
             if (modes.contains(mode.toString().toUpperCase())) return true;
         }
         return false;
     }
 
     /**
      * Returns whether the portlet window supports the given state
      *
      * @param state the portlet window state
      * @return true if the window supports the given state, false otherwise
      */
     public boolean supports(PortletWindow.State state) {
         if (allowedStates.contains(state.toString().toUpperCase())) return true;
         return false;
     }
 
     // Overridden GenericServlet methods
     public final String getInitParameter(String name) {
         return (String)configs.get(name);
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
 
         log.info("PortletConfig Information");
         log.info("portlet name: " + this.getName());
         log.info("servlet name: " + this.getServletName());
         log.info("config init parameters: ");
         enum = this.getInitParameterNames();
         while (enum.hasMoreElements()) {
             name = (String) enum.nextElement();
             vals = getInitParameter(name);
             log.info("\t\tname=" + name + " value=" + vals);
         }
         log.info("PortletContext Information:");
         log.info("Container Info: " + context.getContainerInfo());
         log.info("major version: " + context.getMajorVersion() + " minor version: " + context.getMinorVersion());
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
