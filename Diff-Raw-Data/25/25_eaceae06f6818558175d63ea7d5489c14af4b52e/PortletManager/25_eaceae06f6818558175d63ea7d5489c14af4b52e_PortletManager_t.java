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
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.portletcontainer.*;
 import org.gridlab.gridsphere.portletcontainer.impl.PortletWebApplicationImpl;
 import org.gridlab.gridsphere.services.core.registry.PortletManagerService;
 
 import javax.servlet.ServletContext;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 
 /**
  * The <code>PortletManager</code> is a singleton responsible for maintaining the registry of portlet
  * web applications known to the portlet container.
  */
 public class PortletManager implements PortletManagerService {
 
     private static PortletLog log = SportletLog.getInstance(PortletManager.class);
     private static PortletManager instance = new PortletManager();
     private ServletContext context = null;
     private boolean isInitialized = false;
     private PortletRegistry registry = PortletRegistry.getInstance();
 
     // A multi-valued hashtable with a webapp key and a List value containing portletAppID's
     private List webapps = new Vector();
 
     // An ordered list of known portlet webapps
     private String[] webappFiles = null;
 
     private static String PORTLETS_PATH = "/WEB-INF/CustomPortal/portlets";
     /**
      * Default instantiation disallowed
      */
     private PortletManager() {
     }
 
     private static class WebappComparator implements Comparator {
 
         public int compare(Object webapp1, Object webapp2) {
 
             if (!(webapp1 instanceof String) || !(webapp2 instanceof String)) {
                 throw new ClassCastException("Can only compare string webapp names!!");
             }
             String _webapp1 = (String) webapp1;
             String _webapp2 = (String) webapp2;
 
             int a = _webapp1.lastIndexOf(".");
 
             int b = _webapp2.lastIndexOf(".");
 
             // check if a and b do not have a priority then use alphabetical
             if ((a > 0) && (b > 0)) {
                 try {
                     int a1 = Integer.valueOf(_webapp1.substring(a + 1)).intValue();
                     int a2 = Integer.valueOf(_webapp2.substring(b + 1)).intValue();
                    if (a1 > a2) return 1;
                    if (a1 < a2) return -1;
                     if (a1 == a2) return 0;
                 } catch (NumberFormatException e) {
                     // oh well
                 }
             }
 
            // check if a has a priority and b does not
            if ((a > 0) && (b < 0)) {
                return -1;
            }
            // check if b has a priority and a does not
            if ((a < 0) && (b > 0)) {
                return 1;
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
 
     public synchronized void init() throws PortletServiceUnavailableException {
         if (!isInitialized) {
             log.debug("in init of PM");
             context = GridSphereConfig.getServletContext();
 
             String portletsPath = context.getRealPath(PORTLETS_PATH);
             File f = new File(portletsPath);
             if (f.exists() && f.isDirectory()) {
                 webappFiles = f.list();
 
                 // sort webapps by priority
                 Arrays.sort(webappFiles, new WebappComparator());
 
                 // get rid of any priority numbers
                 String webapp = "";
                 int idx;
                 for (int i = 0; i < webappFiles.length; i++) {
                     webapp = webappFiles[i];
                     if ((idx = webapp.lastIndexOf(".")) > 0) {
                         webappFiles[i] = webapp.substring(0, idx);
                     }
                 }
                 /*
                 for (int i = 0; i < webappFiles.length; i++) {
 
                     try {
                         webapp = webappFiles[i];
 
                         // forget about readme file !
                         if (webapp.startsWith("README")) continue;
                         System.err.println("Creating webapp for " + webapp);
                         PortletWebApplication portletWebApp = new PortletWebApplicationImpl(webapp, context);
                         addWebApp(portletWebApp);
                         System.err.println("END Creating webapp for " + webapp);
                     } catch (PortletException e) {
                         log.error("Unable to create portlet web application: " + webapp);
                         throw new PortletServiceUnavailableException("Unable to create portlet web application: " + webapp, e);
                     }
                 }
                  */
             } else {
                 log.error("Portlet application " + portletsPath + " does not exist!");
                 throw new PortletServiceUnavailableException("Portlet application " + portletsPath + " does not exist!");
             }
             isInitialized = true;
         }
     }
 
     /**
      * Initializes portlet web applications that are defined by the <code>PortletManagerService</code>
      *
      * @param config the <code>PortletServiceConfig</code>
      */
     public synchronized void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
         init();
     }
 
     /**
      * Adds a portlet web application to the registry
      *
      * @param portletWebApp the portlet web application name
      */
     public synchronized void addWebApp(PortletWebApplication portletWebApp) throws PortletServiceException {
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
 
     private void addPortletFile(String webappName) throws PortletServiceException {
         String portletsPath = GridSphereConfig.getServletContext().getRealPath(PORTLETS_PATH);
 
         File f = new File(portletsPath);
         if (f.exists() && f.isDirectory()) {
             String[] webappFiles = f.list();
             for (int i = 0; i < webappFiles.length; i++) {
                 if (webappFiles[i].startsWith(webappName) || webappName.startsWith(webappFiles[i])) return;
             }
         }
         File newfile = new File(portletsPath + File.separator + webappName);
         try {
             newfile.createNewFile();
         } catch (IOException e) {
             log.error("Unable to create portlet app file: " + newfile.getAbsolutePath());
             throw new PortletServiceException("Unable to create portlet app file: " + newfile.getAbsolutePath(), e);
         }
     }
 
     private void removePortletFile(String webappName) {
         String portletsPath = GridSphereConfig.getServletContext().getRealPath(PORTLETS_PATH);
         File f = new File(portletsPath);
         if (f.exists() && f.isDirectory()) {
             String[] webappFiles = f.list();
             for (int i = 0; i < webappFiles.length; i++) {
                 if (webappFiles[i].startsWith(webappName) || webappName.startsWith(webappFiles[i])) {
                     File newfile = new File(portletsPath + File.separator + webappFiles[i]);
                     newfile.delete();
                 }
             }
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
 
         while (it.hasNext()) {
             PortletWebApplication webApp = (PortletWebApplication) it.next();
 
             if (webApp.getWebApplicationName().equalsIgnoreCase(webApplicationName)) {
                 webApp.destroy();
                 Collection appPortlets = webApp.getAllApplicationPortlets();
                 Iterator appsit = appPortlets.iterator();
                 while (appsit.hasNext()) {
                     ApplicationPortlet appPortlet = (ApplicationPortlet) appsit.next();
                     registry.removeApplicationPortlet(appPortlet);
                 }
                 log.debug("removing " + webApp.getWebApplicationName());
                 it.remove();
             }
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
 
         while (it.hasNext()) {
             PortletWebApplication webApp = (PortletWebApplication) it.next();
 
             if (webApp.getWebApplicationName().equalsIgnoreCase(webApplication.getWebApplicationName())) {
 
                 Collection appPortlets = webApp.getAllApplicationPortlets();
                 Iterator appsit = appPortlets.iterator();
                 while (appsit.hasNext()) {
                     ApplicationPortlet appPortlet = (ApplicationPortlet) appsit.next();
                     registry.removeApplicationPortlet(appPortlet);
                 }
                 log.debug("removing " + webApp.getWebApplicationName());
 
                 webApp.destroy();
                 it.remove();
             }
         }
         removePortletFile(webApplication.getWebApplicationName());
     }
 
     /**
      * Initializes all known portlet web applications in order
      *
      * @param req                the portlet request
      * @param res                the portlet response
      * @throws IOException      if an I/O error occurs
      * @throws PortletException if a portlet exception occurs
      */
     public synchronized void initAllPortletWebApplications(PortletRequest req, PortletResponse res) throws IOException, PortletException {
         for (int i = 0; i < webappFiles.length; i++) {
             if (webappFiles[i].startsWith("README")) continue;
             initPortletWebApplication(webappFiles[i], req, res);
         }
     }
 
     /**
      * Initializes the portlet application
      *
      * @param webApplicationName the name of the portlet application
      * @param req                the portlet request
      * @param res                the portlet response
      * @throws IOException      if an I/O error occurs
      * @throws PortletException if a portlet exception occurs
      */
     public synchronized void initPortletWebApplication(String webApplicationName, PortletRequest req, PortletResponse res) throws IOException, PortletException {
         log.debug("initing web app " + webApplicationName);
         PortletWebApplication portletWebApp = new PortletWebApplicationImpl(webApplicationName, context);
         addWebApp(portletWebApp);
         PortletInvoker.initPortletWebApp(webApplicationName, req, res);
     }
 
     /**
      * Destroys the portlet web application and removes from registry
      *
      * @param webApplicationName the portlet application name
      * @param req                the portlet request
      * @param res                the portlet response
      * @throws IOException
      * @throws PortletException
      */
     public synchronized void destroyPortletWebApplication(String webApplicationName, PortletRequest req, PortletResponse res) throws IOException, PortletException {
         log.debug("in destroyPortletWebApplication: " + webApplicationName);
         PortletInvoker.destroyPortletWebApp(webApplicationName, req, res);
         removePortletWebApplication(webApplicationName);
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
             PortletWebApplication webapp = (PortletWebApplication) webapps.get(i);
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
             PortletWebApplication webapp = (PortletWebApplication) webapps.get(i);
             String webappName = webapp.getWebApplicationName();
             if (!l.contains(webappName)) l.add(webappName);
         }
         Iterator it = l.iterator();
         while (it.hasNext()) {
             String s = (String) it.next();
             int idx = s.indexOf(".");
             if (idx < 0) {
                 if (l.contains(s + ".1")) it.remove();
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
             PortletWebApplication webApp = (PortletWebApplication) webapps.get(i);
             if (webApp.getWebApplicationName().equalsIgnoreCase(webApplicationName)) {
                 return webApp.getWebApplicationDescription();
             }
         }
         return webappDesc;
     }
 
 }
