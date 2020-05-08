 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.services.core.registry.impl;
 
 import org.gridlab.gridsphere.portlet.PortletException;
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.PortletResponse;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.portletcontainer.*;
 import org.gridlab.gridsphere.portletcontainer.impl.PortletWebApplicationImpl;
 import org.gridlab.gridsphere.services.core.registry.PortletManagerService;
 
 import javax.servlet.ServletContext;
 import java.io.IOException;
 import java.io.File;
 import java.util.*;
 
 /**
  * The <code>PortletManager</code> is a singleton responsible for maintaining the registry of portlet
  * web applications known to the portlet container.
  */
 public class PortletManager implements PortletManagerService {
 
     private final static String CORE_CONTEXT = "startup-portlet-webapps";
     private static PortletLog log = SportletLog.getInstance(PortletManager.class);
     private static PortletManager instance = new PortletManager();
     private ServletContext context = null;
     private boolean isInitialized = false;
     private PortletRegistry registry = PortletRegistry.getInstance();
 
     // A multi-valued hashtable with a webapp key and a List value containing portletAppID's
     private List webapps = new Vector();
 
     /**
      * Default instantiation disallowed
      */
     private PortletManager() {}
 
 
     private static class WebappComparator implements Comparator {
 
         public int compare(Object webapp1, Object webapp2) {
 
             if (!(webapp1 instanceof String) || !(webapp2 instanceof String)) {
                 throw new ClassCastException("Can only compare string webapp names!!");
             }
             String _webapp1 = (String)webapp1;
             String _webapp2 = (String)webapp2;
 
             int a = _webapp1.lastIndexOf(".");
 
             int b = _webapp2.lastIndexOf(".");
 
             // check if a has a priority and b does not
             if ((a > 0) && (b < 0)) {
                 return 1;
             }
             // check if b has a priority and a does not
             if ((a < 0) && (b > 0)) {
                 return -1;
             }
             // check if a and b do not have a priority then use alphabetical
             if ((a > 0) && (b > 0)) {
 
                 try {
                     int a1 = Integer.valueOf(_webapp1.substring(a+1)).intValue();
                     int a2 = Integer.valueOf(_webapp2.substring(b+1)).intValue();
                     if (a1 > a2) return 1;
                     if (a1 < a2) return -1;
                     if (a1 == a2) return 0;
                 } catch (NumberFormatException e) {
                     // oh well
                 }
             }
 
             // use alphabetical comparison
             return _webapp1.compareTo(_webapp2);
 
         }
 
     }
 
     /**
      * Return an instance of PortletManager
      *
      * @return the PortletManager instance
      */
     public static PortletManager getInstance() {
         return instance;
     }
 
     /**
      * Initializes portlet web applications that are defined by the <code>PortletManagerService</code>
      *
      * @param config the <code>PortletServiceConfig</code>
      * @throws PortletServiceUnavailableException if initialization fails
      */
     public synchronized void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
         log.debug("in init()");
         if (!isInitialized) {
             context = config.getServletContext();
             //String webapps = config.getInitParameter(CORE_CONTEXT);
 
             String portletsPath = GridSphereConfig.getServletContext().getRealPath("/WEB-INF/Portlets");
             File f = new File(portletsPath);
             if (f.exists() && f.isDirectory()) {
                 String[] webappFiles = f.list();
 
                 // sort webapps by priority
                 Arrays.sort(webappFiles, new WebappComparator());
 
                 // get rid of any priority numbers
                 String webapp = "";
                 int idx = 0;
                 for (int i = 0; i < webappFiles.length; i++) {
                     webapp = webappFiles[i];
                     if ((idx = webapp.lastIndexOf(".")) > 0) {
                         webappFiles[i] = webapp.substring(0, idx);
                     }
                 }
 
                 if (webappFiles != null) {
                     for (int i = 0; i < webappFiles.length; i++) {
                         try {
                             webapp = webappFiles[i];
                             PortletWebApplication portletWebApp = new PortletWebApplicationImpl(webapp, context);
                             addWebApp(portletWebApp);
                         } catch (PortletException e) {
                             log.error("Unable to create portlet web application: " + webapp);
                         }
                     }          
                 }
             } else {
                 log.error(portletsPath + " does not exist!");
             }
             isInitialized = true;
         }
     }
 
     /**
      * Adds a portlet web application to the registry
      *
      * @param portletWebApp the portlet web application name
      */
     public synchronized void addWebApp(PortletWebApplication portletWebApp) {
         log.debug("adding webapp: " + portletWebApp.getWebApplicationName());
         addPortletFile(portletWebApp.getWebApplicationName());
         Collection appPortlets = portletWebApp.getAllApplicationPortlets();
         Iterator it = appPortlets.iterator();
         while (it.hasNext()) {
             ApplicationPortlet appPortlet = (ApplicationPortlet) it.next();
             log.debug("Adding application portlet: " + appPortlet.getApplicationPortletID());
             registry.addApplicationPortlet(appPortlet);
         }
         webapps.add(portletWebApp);
     }
 
     private void addPortletFile(String webappName) {
         String portletsPath = GridSphereConfig.getServletContext().getRealPath("/WEB-INF/Portlets");
         File newfile = new File(portletsPath + File.separator + webappName);
         try {
             newfile.createNewFile();
         } catch (IOException e) {
             log.error("Unable to create portlet app file: " + newfile.getAbsolutePath());
         }
     }
 
     private void removePortletFile(String webappName) {
         String portletsPath = GridSphereConfig.getServletContext().getRealPath("/WEB-INF/Portlets");
        File newfile = new File(portletsPath + File.separator + webappName);
        if (newfile.exists()) {
            newfile.delete();
         }
     }
 
     /**
      * Removes the portlet application
      *
      * @param webApplicationName the portlet application name
      */
     protected synchronized void removePortletWebApplication(String webApplicationName) {
         log.debug("in removePortletWebApplication: " + webApplicationName);
         Iterator it = webapps.iterator();
         List removeWebApps = new ArrayList();
         while (it.hasNext()) {
             PortletWebApplication webApp = (PortletWebApplication)it.next();
 
             if (webApp.getWebApplicationName().equalsIgnoreCase(webApplicationName)) {
                 webApp.destroy();
                 Collection appPortlets = webApp.getAllApplicationPortlets();
                 Iterator appsit = appPortlets.iterator();
                 while (appsit.hasNext()) {
                     ApplicationPortlet appPortlet = (ApplicationPortlet) appsit.next();
                     registry.removeApplicationPortlet(appPortlet);
                 }
                 log.debug("removing " + webApp.getWebApplicationName());
                 //webapps.remove(webApp);
                 removeWebApps.add(webApp);
             }
         }
 
         it = removeWebApps.iterator();
         while (it.hasNext()) {
             webapps.remove(it.next());
         }
         removePortletFile(webApplicationName);
     }
 
     /**
      * Removes the portlet application
      *
      * @param webApplication the portlet application name
      */
     public synchronized void removePortletWebApplication(PortletWebApplication webApplication) {
         log.debug("in removePortletWebApplication: " + webApplication);
         Iterator it = webapps.iterator();
         List removeWebApps = new ArrayList();
         while (it.hasNext()) {
             PortletWebApplication webApp = (PortletWebApplication)it.next();
 
             if (webApp.getWebApplicationName().equalsIgnoreCase(webApplication.getWebApplicationName())) {
                 webApp.destroy();
                 Collection appPortlets = webApp.getAllApplicationPortlets();
                 Iterator appsit = appPortlets.iterator();
                 while (appsit.hasNext()) {
                     ApplicationPortlet appPortlet = (ApplicationPortlet) appsit.next();
                     registry.removeApplicationPortlet(appPortlet);
                 }
                 log.debug("removing " + webApp.getWebApplicationName());
                 //webapps.remove(webApp);
                 removeWebApps.add(webApp);
             }
         }
 
         it = removeWebApps.iterator();
         while (it.hasNext()) {
             webapps.remove(it.next());
         }
         removePortletFile(webApplication.getWebApplicationName());
     }
 
     /**
      * Initializes the portlet application
      *
      * @param webApplicationName  the name of the portlet application
      * @param req the portlet request
      * @param res the portlet response
      * @throws IOException  if an I/O error occurs
      * @throws PortletException if a portlet exception occurs
      */
     public synchronized void initPortletWebApplication(String webApplicationName, PortletRequest req, PortletResponse res) throws IOException, PortletException {
         System.err.println("adding web app" + webApplicationName);
         PortletWebApplication portletWebApp = new PortletWebApplicationImpl(webApplicationName, context);
         addWebApp(portletWebApp);
         System.err.println("initing web app " + webApplicationName);
         PortletInvoker.initPortletWebApp(webApplicationName, req, res);
     }
 
     /**
      * Initializes the portlet application
      *
      * @param portletWebApplication the portlet web application
      * @param req the portlet request
      * @param res the portlet response
      * @throws IOException  if an I/O error occurs
      * @throws PortletException if a portlet exception occurs
      */
     public synchronized void initPortletWebApplication(PortletWebApplication portletWebApplication, PortletRequest req, PortletResponse res) throws IOException, PortletException {
         String webapp = portletWebApplication.getWebApplicationName();
         System.err.println("adding web app" + webapp);
         PortletWebApplication portletWebApp = new PortletWebApplicationImpl(webapp, context);
         addWebApp(portletWebApp);
         System.err.println("initing web app " + webapp);
         PortletInvoker.initPortletWebApp(webapp, req, res);
     }
 
     /**
      * Destroys the portlet web application and removes from registry
      *
      * @param webApplicationName the portlet application name
      * @param req the portlet request
      * @param res the portlet response
      * @throws IOException
      * @throws PortletException
      */
     public synchronized void destroyPortletWebApplication(String webApplicationName, PortletRequest req, PortletResponse res) throws IOException, PortletException {
         log.debug("in destroyPortletWebApplication: " + webApplicationName);
         PortletInvoker.destroyPortletWebApp(webApplicationName, req, res);
         removePortletWebApplication(webApplicationName);
     }
 
     /**
      * Shuts down the portlet application
      *
      * @param portletWebApplication the portlet web application
      * @param req the portlet request
      * @param res the portlet response
      * @throws IOException  if an I/O error occurs
      * @throws PortletException if a portlet exception occurs
      */
     public synchronized void destroyPortletWebApplication(PortletWebApplication portletWebApplication, PortletRequest req, PortletResponse res) throws IOException, PortletException {
         String webapp = portletWebApplication.getWebApplicationName();
         log.debug("in destroyPortletWebApplication: " + webapp);
         PortletInvoker.destroyPortletWebApp(webapp, req, res);
         removePortletWebApplication(webapp);
     }
 
     /**
      * Returns the deployed web application names
      *
      * @return the known web application names
      */
     public List getPortletWebApplicationNames() {
         List l = new Vector();
         // get rid of duplicates -- in the case of JSR portlets two webapps exist by the same name
         // since the first one represents the "classical" webapp which itself adds the jsr webapp to the
         // registry with the same name
         for (int i = 0; i < webapps.size(); i++) {
             PortletWebApplication webapp = (PortletWebApplication)webapps.get(i);
             String webappName = webapp.getWebApplicationName();
             if (!l.contains(webappName)) l.add(webappName);
         }
         return l;
     }
 
     /**
      * Returns the deployed web application names
      *
      * @return the known web application names
      */
     public List getWebApplicationNames() {
         List l = new Vector();
         // get rid of duplicates -- in the case of JSR portlets two webapps exist by the same name
         // since the first one represents the "classical" webapp which itself adds the jsr webapp to the
         // registry with the same name
         for (int i = 0; i < webapps.size(); i++) {
             PortletWebApplication webapp = (PortletWebApplication)webapps.get(i);
             String webappName = webapp.getWebApplicationName();
             if (!l.contains(webappName)) l.add(webappName);
         }
         for (int i = 0; i < l.size(); i++) {
             String s = (String)l.get(i);
             int idx = s.indexOf(".");
             if ((idx > 0) && s.endsWith(".1")) {
                 String n = s.substring(0,idx);
                 System.err.println("n = " + n);
                 l.remove(n);
             }
         }
         return l;
     }
 
     /**
      * Returns the description of the supplied web application
      *
      * @param webApplicationName
      * @return the description of this web application
      */
     public String getPortletWebApplicationDescription(String webApplicationName) {
         String webappDesc = "Undefined portlet web application";
         for (int i = 0; i < webapps.size(); i++) {
             PortletWebApplication webApp = (PortletWebApplication)webapps.get(i);
             if (webApp.getWebApplicationName().equalsIgnoreCase(webApplicationName)) {
                 return webApp.getWebApplicationDescription();
             }
         }
         return webappDesc;
     }
 
 }
