 package org.gridsphere.layout;
 
 import org.gridsphere.portlet.*;
 import org.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridsphere.portlet.impl.SportletLog;
 import org.gridsphere.portlet.impl.SportletProperties;
 import org.gridsphere.services.core.portal.PortalConfigService;
 import org.gridsphere.services.core.security.role.PortletRole;
 import org.gridsphere.services.core.security.role.RoleManagerService;
 import org.gridsphere.services.core.security.group.GroupManagerService;
 import org.gridsphere.services.core.user.UserManagerService;
 import org.gridsphere.portletcontainer.impl.PortletSessionManager;
 
 import javax.servlet.ServletContext;
 import java.io.*;
 import java.net.URLEncoder;
 import java.net.URL;
 import java.util.*;
 
 /**
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id: PortletPageFactory.java 5032 2006-08-17 18:15:06Z novotny $
  */
 public class PortletPageFactory implements PortletSessionListener {
 
     public static final String TCK_PAGE = "TCK";
     public static final String SETUP_PAGE = "SetupLayout";
     public static final String ERROR_PAGE = "ErrorLayout";
     public static final String GUEST_PAGE = "GuestUserLayout";
 
     // TODO these need refactoring later on
     public static final String TEMPLATE_PAGE = "TemplateLayout";
     public static final String USER_PAGE = "LoggedInUserLayout";
 
     private static String USER_LAYOUT_DIR = null;
 
     private static final String DEFAULT_THEME = "default";
 
     private static PortletPageFactory instance = null;
     private PortletSessionManager sessionManager = PortletSessionManager.getInstance();
     private PortalConfigService portalConfigService = null;
     private RoleManagerService roleService = null;
 
     private UserManagerService userManagerService = null;
     private PortletLog log = SportletLog.getInstance(PortletPageFactory.class);
 
     protected URL LAYOUT_MAPPING_PATH = getClass().getResource("/org/gridsphere/layout/layout-mapping.xml");
 
     // Store user layouts in a hash
     private static Map userLayouts = new HashMap();
 
     // a hash of hashes to contain all the users layouts
     private static Map layouts = new HashMap();
 
     // a hash of loaded master layouts used to make copies
     private static Map masterLayouts = new HashMap();
     private static Set editableLayoutIds = new HashSet();
 
     private ServletContext context;
 
     private boolean setupNeeded = true;
 
     private PortletPageFactory() {
 
     }
 
     public void init(ServletContext ctx) {
 
         this.context = ctx;
 
         USER_LAYOUT_DIR = ctx.getRealPath("/WEB-INF/CustomPortal/layouts/users");
 
         String layoutsDirPath = ctx.getRealPath("/WEB-INF/CustomPortal/layouts");
         File layoutsDir = new File(layoutsDirPath);
         File[] layoutFiles = layoutsDir.listFiles();
         PortletPage page = null;
         for (int i = 0; i < layoutFiles.length; i++) {
             File layoutFile = layoutFiles[i];
             String layoutFileName = layoutFile.getName();
             if (layoutFileName.endsWith(".xml")) {
                 String layoutId = layoutFileName.substring(0, layoutFileName.indexOf(".xml"));
                 try {
                     page = PortletLayoutDescriptor.loadPortletPage(layoutFile.getAbsolutePath(), LAYOUT_MAPPING_PATH);
                     page.setLayoutDescriptor(layoutFile.getAbsolutePath());
                     if (page.getEditable()) editableLayoutIds.add(layoutId);
                     masterLayouts.put(layoutId, page);
                 } catch (Exception e) {
                     log.error("Unable to load portlet page: " + layoutFileName);
                 }
             }
         }
 
         String newuserLayoutPath = ctx.getRealPath("/WEB-INF/CustomPortal/layouts/users/");
         File userdir = new File(newuserLayoutPath);
         if (!userdir.exists()) {
             userdir.mkdir();
         }
 
         portalConfigService = (PortalConfigService)PortletServiceFactory.createPortletService(PortalConfigService.class, true);
         roleService = (RoleManagerService) PortletServiceFactory.createPortletService(RoleManagerService.class, true);
         userManagerService = (UserManagerService) PortletServiceFactory.createPortletService(UserManagerService.class, true);
     }
 
     public static synchronized PortletPageFactory getInstance() {
         if (instance == null) {
             instance = new PortletPageFactory();
         }
         return instance;
     }
 
     public void login(PortletRequest request) {
 
     }
 
     public void logout(PortletSession session) {
         log.debug("in logout PortletPageFactory");
         String sessionId = session.getId();
 
         Map usersLayouts = (Map)layouts.get(sessionId);
         if (usersLayouts != null) {
             Iterator it = usersLayouts.keySet().iterator();
             while (it.hasNext()) {
                 String layoutId = (String)it.next();
                 log.debug("Removing " + layoutId + " container for:" + sessionId);
                 it.remove();
             }
             layouts.remove(sessionId);
         }
         if (userLayouts.containsKey(sessionId)) {
             log.debug("Removing user  container for:" + sessionId);
             userLayouts.remove(sessionId);
         }
 
     }
 
     public synchronized void destroy() {
         layouts.clear();
         userLayouts.clear();
         masterLayouts.clear();
         editableLayoutIds.clear();
     }
 
     public Set getEditableLayoutIds() {
         return editableLayoutIds;
     }
 
     public Set getLayoutIds() {
         return masterLayouts.keySet();
     }
 
     public PortletTabbedPane getUserTabbedPane(PortletRequest req) {
 
 
         User user = req.getUser();
 
         String sessionId = req.getPortletSession(true).getId();
 
         String userLayout = USER_LAYOUT_DIR + File.separator + user.getUserName();
 
         if (userLayouts.containsKey(sessionId)) {
             PortletPage page = (PortletPage)userLayouts.get(USER_PAGE);
             PortletTabbedPane pane = new PortletTabbedPane();
             pane.setLayoutDescriptor(userLayout);
             PortletTabbedPane existPane = page.getPortletTabbedPane();
             List tabs = existPane.getPortletTabs();
             Iterator it = tabs.iterator();
             while (it.hasNext()) {
                 PortletTab tab = (PortletTab) it.next();
                 if (tab.getCanModify()) {
                     pane.addTab(tab);
                 }
             }
             return (!pane.getPortletTabs().isEmpty() ? pane : null);
         }
 
         File f = new File(userLayout);
         PortletTabbedPane pane = null;
 
 
         if (f.exists()) {
             try {
                 pane = PortletLayoutDescriptor.loadPortletTabs(userLayout, LAYOUT_MAPPING_PATH);
                 pane.setLayoutDescriptor(userLayout);
                 log.debug("Adding user tab to layout");
             } catch (Exception e) {
                 log.error("Unable to make a clone of the templatePage", e);
                 return null;
             }
         } else {
             return null;
         }
 
         // create tmp page
         PortletPage tmpPage = new PortletPage();
         try {
             //tmpPage.setLayoutDescriptor(userLayout + ".tmp");
             PortletTabbedPane tmpPane = (PortletTabbedPane) deepCopy(pane);
             tmpPage.setPortletTabbedPane(tmpPane);
             this.setPageTheme(tmpPage, req);
             tmpPage.init(req, new ArrayList());
 
             // when deleting must reinit everytime
             int i = 0;
             boolean found;
             List allowedPortlets = new ArrayList();
             while (i < tmpPage.getComponentIdentifierList().size()) {
                 found = false;
                 Iterator it = tmpPage.getComponentIdentifierList().iterator();
                 while (it.hasNext() && (!found)) {
                     found = false;
                     ComponentIdentifier cid = (ComponentIdentifier) it.next();
                     if (cid.getPortletComponent() instanceof PortletFrame) {
                         if (!allowedPortlets.contains(cid.getPortletClass())) {
                             PortletComponent pc = cid.getPortletComponent();
                             PortletComponent parent = pc.getParentComponent();
                             parent.remove(pc, req);
                             tmpPage.init(req, new ArrayList());
                             found = true;
                         }
                     }
                 }
                 i++;
             }
 
             tmpPane.save(context);
             return tmpPane;
         } catch (Exception e) {
             log.error("Unable to save user pane!", e);
         }
 
         return null;
 
     }
 
     public void setPageTheme(PortletPage page, PortletRequest req) {
         String defaultTheme = portalConfigService.getProperty("DEFAULT_THEME");
         String theme = null;
         if (defaultTheme != null) theme = defaultTheme;
         User user = req.getUser();
         if (user != null) {
             theme = (String) user.getAttribute(User.THEME);
         }
         if (theme == null) theme = DEFAULT_THEME;
         req.getPortletSession().setAttribute(SportletProperties.LAYOUT_THEME, theme);
     }
 
     public PortletTabbedPane createNewUserPane(PortletRequest req, int cols, String tabName) {
 
         PortletTabbedPane pane = null;
         try {
             pane = getUserTabbedPane(req);
             int tabNum = PortletTab.DEFAULT_USERTAB_ORDER;
             if (pane == null) {
                 pane = new PortletTabbedPane();
                 User user = req.getUser();
                 String userLayout = USER_LAYOUT_DIR + File.separator + user.getUserName();
                 pane.setLayoutDescriptor(userLayout);
             } else {
                 tabNum = pane.getLastPortletTab().getTabOrder() + 1;
             }
 
             PortletTab topTab = new PortletTab();
             topTab.setTabOrder(tabNum);
 
             topTab.setCanModify(true);
             topTab.setTitle(req.getLocale().getLanguage(), tabName);
             PortletTabbedPane childPane = new PortletTabbedPane();
             PortletTab childTab = new PortletTab();
 
             childPane.setStyle("sub-menu");
             topTab.setPortletComponent(childPane);
             pane.addTab(topTab);
             topTab.setName(tabName);
             topTab.setLabel(URLEncoder.encode(tabName, "UTF-8") + "Tab");
             //pane.save(userLayout);
 
             PortletTableLayout table = new PortletTableLayout();
 
             table.setCanModify(true);
             table.setLabel(URLEncoder.encode(tabName, "UTF-8") + "TL");
 
 
             PortletRowLayout row = new PortletRowLayout();
             int width = 100 / cols;
             for (int i = 0; i < cols; i++) {
                 PortletColumnLayout col = new PortletColumnLayout();
                 col.setWidth(String.valueOf(width) + "%");
                 row.addPortletComponent(col);
             }
 
             table.addPortletComponent(row);
 
             childTab.setPortletComponent(table);
             childTab.setTitle(req.getLocale().getLanguage(), "");
             childPane.addTab(childTab);
 
         } catch (Exception e) {
             log.error("Unable to make a clone of the templatePage", e);
 
         }
         return pane;
 
     }
 
     public PortletPage createPortletPageCopy(String layoutId) {
         // get the master copy of the page
         PortletPage masterPage = (PortletPage)masterLayouts.get(layoutId);
         PortletPage copy = null;
         // there are two cases where a master may not be there, TCK case and logged in  user
         try {
             copy = (PortletPage) deepCopy(masterPage);
         } catch (Exception e) {
             log.error("Failed to make a copy of the master page: " + layoutId);
             return createErrorPage();
         }
         return copy;
     }
 
     public void savePortletPageMaster(PortletPage page) {
         String layoutDesc = page.getLayoutDescriptor();
         String layoutId = layoutDesc.substring(layoutDesc.lastIndexOf(File.separator)+1, layoutDesc.lastIndexOf(".xml"));
         log.debug("saving layout: " + layoutId);
 
 
         try {
             PortletLayoutDescriptor.savePortletPage(page, layoutDesc, LAYOUT_MAPPING_PATH);
             masterLayouts.put(layoutId, page);
         } catch (Exception e) {
             log.error("Unable to save layout descriptor: " + layoutDesc, e);
         }
 
         // remove any active layouts with this layoutId
         Iterator it = layouts.values().iterator();
         while (it.hasNext()) {
             Map userLayouts = (Map)it.next();
             userLayouts.remove(layoutId);
             log.debug("removing a layout: " + layoutId);
         }
 
     }
 
 
     /**
      * This is the primary entrance to retrieving a PortletPage
      *
      * @param req
      * @return a portlet page
      */
     public PortletPage getPortletPage(PortletRequest req) {
         // first check for layout id in request parameter
         // TODO potential security risk!!! MUST DO SOME ROLE CHECKING HERE!!
         if (setupNeeded) {
            if (!(userManagerService.getNumUsers() == 0) || (roleService.getUsersInRole(PortletRole.ADMIN)).size() > 0) {
                 setupNeeded = false;
             } else {
                 req.setAttribute(SportletProperties.LAYOUT_PAGE, SETUP_PAGE);
             }
         }
         String layoutId = (String)req.getAttribute(SportletProperties.LAYOUT_PAGE);
         if (layoutId == null) {
             if (req.getUser() == null) {
                 // if no reference to a layout exists, return a guest layout
                 layoutId = GUEST_PAGE;
             } else {
                 layoutId = USER_PAGE;
             }
             req.setAttribute(SportletProperties.LAYOUT_PAGE, layoutId);
         }
         return getPortletPageFromHash(req, layoutId);
     }
 
     /**
      * This returns the page from the hashtable or creates a new one if necessary
      * @param req
      * @param layoutId
      * @return the page
      */
     protected PortletPage getPortletPageFromHash(PortletRequest req, String layoutId) {
         PortletSession session = req.getPortletSession();
         PortletPage page = null;
 
         Map usersLayouts = (Map)layouts.get(session.getId());
         if (usersLayouts == null) {
             usersLayouts = new HashMap();
             layouts.put(session.getId(), usersLayouts);
         }
         // now check for existing layout in hash
         page = (PortletPage)usersLayouts.get(layoutId);
         // only if no page exists, create a new one and place in hash
         if (page == null) {
             page = createPortletPage(req, layoutId);
             String role = page.getRequiredRole();
             if (!role.equals("") && !req.isUserInRole(role) && !setupNeeded) {
                 // use existing page
                 log.debug("User does not have proper permissions for layout=" + layoutId + "!!");
                 if (req.getUser() == null) {
                     // if no reference to a layout exists, return a guest layout
                     layoutId = GUEST_PAGE;
                 } else {
                     layoutId = USER_PAGE;
                 }
                 page = (PortletPage)usersLayouts.get(layoutId);
                 if (page == null) page = createPortletPage(req, layoutId);
                 req.setAttribute(SportletProperties.LAYOUT_PAGE, layoutId);
             }
             usersLayouts.put(layoutId, page);
             log.debug("Creating new page " + layoutId + " placing in session " + session.getId());
             sessionManager.addSessionListener(session.getId(), this);
         }
         return page;
     }
 
     public PortletPage createPortletPage(PortletRequest req, String layoutId) {
         // get the master copy of the page
         PortletPage masterPage = (PortletPage)masterLayouts.get(layoutId);
         PortletPage copy = null;
         // there are two cases where a master may not be there, TCK case and logged in  user
         if (masterPage == null) {
             if (layoutId.equals(TCK_PAGE)) {
                 copy = createTCKPage(req);
             } else {
               if (req.getUser() == null) {
                     // if no reference to a layout exists, return a guest layout
                     return getPortletPageFromHash(req, GUEST_PAGE);
                 } else {
                     return getPortletPageFromHash(req, USER_PAGE);
                 }  
             }
         } else {
             try {
                 copy = (PortletPage) deepCopy(masterPage);
                 log.debug("Creating deep copy of page " + layoutId);
             } catch (Exception e) {
                 log.error("Failed to make a copy of the master page: " + layoutId);
                 return createErrorPage();
             }
         }
         setPageTheme(copy, req);
         req.setAttribute(SportletProperties.COMPONENT_ID_VAR, SportletProperties.COMPONENT_ID);
         copy.init(req, new ArrayList());
         return copy;
     }
 
     public void removePortletPage(PortletRequest req) {
         PortletSession session = req.getPortletSession();
 
         String id = session.getId();
         if (layouts.containsKey(id)) {
             layouts.remove(id);
         }
         //log.debug("removed user layout: " + userLayout);
     }
 
 
 
     public static Object deepCopy(Object oldObj) throws Exception {
         ObjectOutputStream oos = null;
         ObjectInputStream ois = null;
         try {
             ByteArrayOutputStream bos =
                     new ByteArrayOutputStream(); // A
             oos = new ObjectOutputStream(bos); // B
             // serialize and pass the object
             oos.writeObject(oldObj);   // C
             oos.flush();               // D
             ByteArrayInputStream bin =
                     new ByteArrayInputStream(bos.toByteArray()); // E
             ois = new ObjectInputStream(bin);                  // F
             // return the new object
             return ois.readObject(); // G
         } catch (Exception e) {
             //log.error("Exception in ObjectCloner = ", e);
             e.printStackTrace();
             throw(e);
         } finally {
             if (oos != null) oos.close();
             if (ois != null) ois.close();
         }
     }
 
     public void logStatistics() {
         /*
         log.debug("\n\nnumber of guest layouts: " + guests.size());
         Iterator it = guests.keySet().iterator();
         while (it.hasNext()) {
             String id = (String) it.next();
             log.debug("guest has session: " + id);
         }
         log.debug("number of user layouts: " + userLayouts.size());
         it = userLayouts.keySet().iterator();
         while (it.hasNext()) {
             String id = (String) it.next();
             log.debug("user has session: " + id);
         }
         */
     }
 
     // TODO
     public PortletPage createErrorPage() {
         PortletPage errorPage = createPortletPageCopy(ERROR_PAGE);
         return errorPage;
     }
 
     public PortletPage createTCKPage(PortletRequest req) {
         String[] portletNames = req.getParameterValues("portletName");
         PortletPage page = null;
         // Sun TCK test uses Jakarta Commons-HttpClient/2.0beta1
 
         if (req.getClient().getUserAgent().indexOf("HttpClient") > 0) {
             if (portletNames != null) {
                 log.info("Creating TCK LAYOUT!");
                 String pageName = req.getParameter("pageName");
                 page = new PortletPage();
                 PortletTableLayout tableLayout = new PortletTableLayout();
                 StringTokenizer tokenizer;
                 for (int i = 0; i < portletNames.length; i++) {
                     tokenizer = new StringTokenizer(portletNames[i], "/");
                     String appName = tokenizer.nextToken();
                     String portletName = tokenizer.nextToken();
                     //String portletClass = registry.getPortletClassName(appName, portletName);
                     //if (portletClass == null) {
                     //    log.error("Unable to find portlet class for " + portletName);
                     //}
                     if (pageName == null) {
                         pageName = "TCK_testpage_" + portletName;
                     }
                     PortletFrame frame = new PortletFrame();
                     PortletTitleBar tb = new PortletTitleBar();
                     //tb.setPortletClass(portletClass);
                     tb.setPortletClass(appName + "#" + portletName);
                     frame.setPortletTitleBar(tb);
                     //frame.setPortletClass(portletClass);
                     frame.setPortletClass(appName + "#" + portletName);
                     tableLayout.addPortletComponent(frame);
                 }
 
                 PortletTab tab = new PortletTab();
                 tab.setTitle("en", pageName);
                 tab.setPortletComponent(tableLayout);
                 PortletTabbedPane pane = new PortletTabbedPane();
                 pane.addTab(tab);
                 page.setPortletTabbedPane(pane);
                 page.setLayoutDescriptor("/tmp/test.xml");
                 try {
                     page.save(context);
                     this.setPageTheme(page, req);
                     page.init(req, new ArrayList());
                 } catch (IOException e) {
                     log.error("Unable to save TCK page to /tmp/test.xml", e);
                 }
             }
         }
         return page;
     }
 
 
 }
