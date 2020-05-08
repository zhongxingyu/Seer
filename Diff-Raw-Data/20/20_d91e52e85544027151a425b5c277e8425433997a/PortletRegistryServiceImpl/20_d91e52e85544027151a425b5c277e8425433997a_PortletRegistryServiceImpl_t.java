 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.services.registry.impl;
 
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridlab.gridsphere.portletcontainer.ApplicationPortlet;
 import org.gridlab.gridsphere.portletcontainer.PortletWebApplication;
 import org.gridlab.gridsphere.portletcontainer.PortletRegistryManager;
import org.gridlab.gridsphere.services.registry.PortletRegistryService;
import org.gridlab.gridsphere.layout.PortletLayoutEngine;
 
 import javax.servlet.ServletContext;
 import java.util.*;
 
 /**
  * The PortletRegistryService acts as a repository for portlets and makes them available to the portlet
  * container, layout manager and any other services that require an active portlet.
  * The PortletInfo base class is responsible for reading in the associated portlet.xml file and
  * creating a ConcretePortlet object which represents the portlet. The PortletRegistryService maintains
  * a Set of RegisteredPortlets and provides operations for the registration, unregistration and querying
  * of ConcretePortlet objects.
  */
public class PortletRegistryServiceImpl implements PortletRegistryService, PortletServiceProvider {
 
     public final static String CORE_CONTEXT = "coreContext";
 
     private static PortletLog log = org.gridlab.gridsphere.portlet.impl.SportletLog.getInstance(PortletRegistryServiceImpl.class);
     private static org.gridlab.gridsphere.services.registry.PortletRegistryService registryService = null;
     private ServletContext context = null;
 
    private PortletLayoutEngine layoutEngine = PortletLayoutEngine.getInstance();
     private PortletRegistryManager manager = PortletRegistryManager.getInstance();
 
     // A multi-valued hashtable with a webapp key and a List value containing portletAppID's
     private Map webapps = new Hashtable();
 
     /**
      * The init method is responsible for parsing portlet.xml and creating ConcretePortlet objects based on the
      * entries. The RegisteredPortlets are managed by the PortletRegistryService.
      */
     public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
         log.info("in init()");
         this.context = config.getServletConfig().getServletContext();
         String webapp = config.getInitParameter(CORE_CONTEXT);
         addWebApp(webapp);
     }
 
     public void destroy() {
         log.info("in destroy()");
     }
 
     /**
      * Adds a portlet web application to the registry
      *
      * @param the web application name
      */
     public void addPortletWebApplication(User user, String webApplicationName) {
         addWebApp(webApplicationName);
     }
 
    /**
     *
     */
     protected void addWebApp(String webApplicationName) {
         PortletWebApplication portletWebApp = new PortletWebApplication(webApplicationName, context);
         Collection appPortlets = portletWebApp.getAllApplicationPortlets();
         Iterator it = appPortlets.iterator();
         while (it.hasNext()) {
             ApplicationPortlet appPortlet = (ApplicationPortlet)it.next();
             webapps.put(webApplicationName, appPortlet);
             manager.addApplicationPortlet(appPortlet);
         }
        layoutEngine.addApplicationTab(webApplicationName, portletWebApp.getApplicationTab());
     }
 
     /**
      * Removes a portlet web application from the registry
      *
      * @param the web application name
      */
     public void removePortletWebApplication(User user, String webApplicationName) {
        ApplicationPortlet appPortlet = (ApplicationPortlet)webapps.get(webApplicationName);
        manager.removeApplicationPortlet((String)appPortlet.getPortletAppID());
         webapps.remove(webApplicationName);
     }
 
     /**
      * Reloads a portlet web application from the registry
      *
      * @param the web application name
      */
     public void reloadPortletWebApplication(User user, String webApplicationName) {
         removePortletWebApplication(user, webApplicationName);
         addPortletWebApplication(user, webApplicationName);
     }
 
     /**
      * Lists all the portlet web applications in the registry
      *
      * @return the list of web application names
      */
     public List listPortletWebApplications() {
         List l = new ArrayList();
         Set set = webapps.keySet();
         Iterator it = set.iterator();
         while (it.hasNext()) {
             l.add((String)it.next());
         }
         return l;
     }
 
 }
