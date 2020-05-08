 /*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
 package org.gridlab.gridsphere.servlets;
 
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
 import org.gridlab.gridsphere.core.persistence.hibernate.DBTask;
 import org.gridlab.gridsphere.layout.PortletLayoutEngine;
 import org.gridlab.gridsphere.layout.PortletPageFactory;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.UserPrincipal;
 import org.gridlab.gridsphere.portlet.impl.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.portletcontainer.impl.GridSphereEventImpl;
 import org.gridlab.gridsphere.portletcontainer.impl.SportletMessageManager;
 import org.gridlab.gridsphere.portletcontainer.*;
 import org.gridlab.gridsphere.services.core.registry.PortletManagerService;
 import org.gridlab.gridsphere.services.core.security.group.impl.UserGroup;
 import org.gridlab.gridsphere.services.core.security.group.GroupManagerService;
 import org.gridlab.gridsphere.services.core.security.auth.AuthorizationException;
 import org.gridlab.gridsphere.services.core.security.auth.AuthenticationException;
 import org.gridlab.gridsphere.services.core.security.role.RoleManagerService;
 import org.gridlab.gridsphere.services.core.user.LoginService;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 import org.gridlab.gridsphere.services.core.request.RequestService;
 import org.gridlab.gridsphere.services.core.request.GenericRequest;
 import org.gridlab.gridsphere.services.core.tracker.TrackerService;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigService;
 
 import javax.servlet.*;
 import javax.servlet.http.*;
 import javax.activation.FileDataSource;
 import javax.activation.DataHandler;
 import java.io.IOException;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.*;
 import java.security.Principal;
 import java.net.SocketException;
 
 
 /**
  * The <code>GridSphereServlet</code> is the GridSphere portlet container.
  * All portlet requests get proccessed by the GridSphereServlet before they
  * are rendered.
  */
 public class GridSphereServlet extends HttpServlet implements ServletContextListener,
         HttpSessionAttributeListener, HttpSessionListener {
 
     /* GridSphere logger */
     private static PortletLog log = SportletLog.getInstance(GridSphereServlet.class);
 
     /* GridSphere service factory */
     private static SportletServiceFactory factory = null;
 
     /* GridSphere Portlet Registry Service */
     private static PortletManagerService portletManager = null;
 
     /* GridSphere Access Control Service */
     private static RoleManagerService roleService = null;
     private static GroupManagerService groupService = null;
 
     private static UserManagerService userManagerService = null;
 
     private static LoginService loginService = null;
 
     private static TrackerService trackerService = null;
 
     private static PortalConfigService portalConfigService = null;
     private PortletMessageManager messageManager = SportletMessageManager.getInstance();
 
     /* GridSphere Portlet layout Engine handles rendering */
     private static PortletLayoutEngine layoutEngine = null;
 
     /* creates cookie requests */
     private RequestService requestService = null;
 
     private PortletContext context = null;
     private static Boolean firstDoGet = Boolean.TRUE;
 
     private static PortletSessionManager sessionManager = PortletSessionManager.getInstance();
 
     //private static PortletRegistry registry = PortletRegistry.getInstance();
     private static final String COOKIE_REQUEST = "cookie-request";
     private int COOKIE_EXPIRATION_TIME = 60 * 60 * 24 * 7;  // 1 week (in secs)
 
     private PortletGroup coreGroup = null;
 
     private boolean isTCK = false;
 
     /**
      * Initializes the GridSphere portlet container
      *
      * @param config the <code>ServletConfig</code>
      * @throws ServletException if an error occurs during initialization
      */
     public final void init(ServletConfig config) throws ServletException {
         super.init(config);
         GridSphereConfig.setServletConfig(config);
         this.context = new SportletContext(config);
         factory = SportletServiceFactory.getInstance();
         factory.init();
         layoutEngine = PortletLayoutEngine.getInstance();
         log.debug("in init of GridSphereServlet");
     }
 
     public synchronized void initializeServices() throws PortletServiceException {
         requestService = (RequestService) factory.createPortletService(RequestService.class, true);
         log.debug("Creating access control manager service");
         roleService = (RoleManagerService) factory.createPortletService(RoleManagerService.class, true);
         groupService = (GroupManagerService) factory.createPortletService(GroupManagerService.class, true);
 
         // create root user in default group if necessary
         log.debug("Creating user manager service");
         userManagerService = (UserManagerService) factory.createPortletService(UserManagerService.class, true);
 
         loginService = (LoginService) factory.createPortletService(LoginService.class, true);
         log.debug("Creating portlet manager service");
         portletManager = (PortletManagerService) factory.createPortletService(PortletManagerService.class, true);
         portalConfigService = (PortalConfigService)factory.createPortletService(PortalConfigService.class, true);
         trackerService = (TrackerService) factory.createPortletService(TrackerService.class, true);
     }
 
     /**
      * Processes GridSphere portal framework requests
      *
      * @param req the <code>HttpServletRequest</code>
      * @param res the <code>HttpServletResponse</code>
      * @throws IOException      if an I/O error occurs
      * @throws ServletException if a servlet error occurs
      */
     public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
         processRequest(req, res);
     }
 
     public void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
         // set content to UTF-8 for il8n and compression if supported
         req.setCharacterEncoding("utf-8");
 
         long startTime = System.currentTimeMillis();
         GridSphereEvent event = new GridSphereEventImpl(context, req, res);
         PortletRequest portletReq = event.getPortletRequest();
 
         // If first time being called, instantiate all portlets
         if (firstDoGet.equals(Boolean.TRUE)) {
             firstDoGet = Boolean.FALSE;
             log.debug("Testing Database");
             // checking if database setup is correct
             DBTask dt = new DBTask();
             dt.setAction(DBTask.ACTION_CHECKDB);
             dt.setConfigDir(GridSphereConfig.getServletContext().getRealPath(""));
 
             log.debug("Initializing services");
             try {
                 // initialize needed services
                 initializeServices();
                 updateDatabase();
 
                 // deep inside a service is used which is why this must follow the factory.init
                 layoutEngine.init(getServletConfig().getServletContext());
                 if (isTCK) PortletPageFactory.setUseTCK(true);
             } catch (Exception e) {
                 log.error("GridSphere initialization failed!", e);
                 RequestDispatcher rd = req.getRequestDispatcher("/jsp/errors/init_error.jsp");
                 req.setAttribute("error", e);
                 rd.forward(req, res);
                 return;
             }
             coreGroup = groupService.getCoreGroup();
         }
 
         // check to see if user has been authorized by means of container managed authorization
         checkWebContainerAuthorization(event);
 
         setUserAndGroups(event);
 
         String userName;
         User user = portletReq.getUser();
         if (user == null) {
             userName = "guest";
         } else {
             userName = user.getUserName();
         }
 
         String trackme = req.getParameter(TrackerService.TRACK_PARAM);
         if (trackme != null) {
             trackerService.trackURL(trackme, req.getHeader("user-agent"), userName);
             String url = req.getParameter(TrackerService.REDIRECT_URL);
             if (url != null) {
                 System.err.println("redirect: " + url);
                 res.sendRedirect(url);
             }
          }
 
         checkUserHasCookie(event);
 
         // Used for TCK tests
         if (isTCK) setTCKUser(portletReq);
 
         // Handle user login and logout
         if (event.hasAction()) {
             String actionName = event.getAction().getName();
             if (actionName.equals(SportletProperties.LOGIN)) {
                 login(event);
                 long endTime = System.currentTimeMillis();
                 System.err.println("Time taken = " + (endTime - startTime) + " (ms) request= " + req.getQueryString());
                 return;
             }
             if (actionName.equals(SportletProperties.LOGOUT)) {
                 logout(event);
                 long endTime = System.currentTimeMillis();
                 System.err.println("Time taken = " + (endTime - startTime) + " (ms) request= " + req.getQueryString());
                 return;
             }
             if (trackerService.hasTrackingAction(actionName)) {
                 trackerService.trackURL(actionName, req.getHeader("user-agent"), userName);
             }
         }
 
         layoutEngine.actionPerformed(event);
 
 
         // is this a file download operation?
         if (isDownload(req)) {
             try {
                 downloadFile(req, res);
                 return;
             } catch (PortletException e) {
                 log.error("Unable to download file!", e);
                 req.setAttribute(SportletProperties.FILE_DOWNLOAD_ERROR, e);
             }
         }
 
         // Handle any outstanding messages
         // This needs work certainly!!!
         Map portletMessageLists = messageManager.retrieveAllMessages();
         if (!portletMessageLists.isEmpty()) {
             Set keys = portletMessageLists.keySet();
             Iterator it = keys.iterator();
             String concPortletID;
             List messages;
             while (it.hasNext()) {
                 concPortletID = (String) it.next();
                 messages = (List) portletMessageLists.get(concPortletID);
                 Iterator newit = messages.iterator();
                 while (newit.hasNext()) {
                     PortletMessage msg = (PortletMessage) newit.next();
                     layoutEngine.messageEvent(concPortletID, msg, event);
                 }
 
             }
             messageManager.removeAllMessages();
         }
 
         setUserAndGroups(event);
 
         // Used for TCK tests
         if (isTCK) setTCKUser(portletReq);
 
         layoutEngine.service(event);
 
         //log.debug("Session stats");
         //userSessionManager.dumpSessions();
 
         //log.debug("Portlet service factory stats");
         //factory.logStatistics();
         long endTime = System.currentTimeMillis();
         System.err.println("Time taken = " + (endTime - startTime) + " (ms) request= " + req.getQueryString());
     }
 
     /**
      * Method to set the response headers to perform file downloads to a browser
      *
      * @param req the HttpServletRequest
      * @param res the HttpServletResponse
      * @throws org.gridlab.gridsphere.portlet.PortletException
      */
     public void downloadFile(HttpServletRequest req, HttpServletResponse res) throws PortletException, IOException {
         try {
             String fileName = (String) req.getAttribute(SportletProperties.FILE_DOWNLOAD_NAME);
             String path = (String) req.getAttribute(SportletProperties.FILE_DOWNLOAD_PATH);
             Boolean deleteFile = (Boolean)req.getAttribute(SportletProperties.FILE_DELETE);
             if (deleteFile == null) deleteFile = Boolean.FALSE;
             if (fileName == null) return;
             log.debug("in downloadFile");
             log.debug("filename: " + fileName + " filepath= " + path);
             File file = (File) req.getAttribute(SportletProperties.FILE_DOWNLOAD_BINARY);
             if (file == null) {
                 file = new File(path + fileName);
             }
             FileDataSource fds = new FileDataSource(file);
             log.debug("filename: " + fileName + " filepath= " + path + " content type=" + fds.getContentType());
             res.setContentType(fds.getContentType());
             res.setHeader("Content-Disposition", "attachment; filename=" + fileName);
             res.setHeader("Content-Length", String.valueOf(file.length()));
             DataHandler handler = new DataHandler(fds);
             handler.writeTo(res.getOutputStream());
             if (deleteFile.booleanValue()) {
                 file.delete();
             }
         } catch (FileNotFoundException e) {
             throw new PortletException("Unable to find file!", e);
         } catch (SecurityException e) {
             // this gets thrown if a security policy applies to the file. see java.io.File for details.
             throw new PortletException("A security error occurred!", e);
         } catch (SocketException e) {
             throw new PortletException("A socket error occurred!", e);
         } finally {
             req.removeAttribute(SportletProperties.FILE_DOWNLOAD_NAME);
             req.removeAttribute(SportletProperties.FILE_DOWNLOAD_PATH);
             req.removeAttribute(SportletProperties.FILE_DELETE);
             req.removeAttribute(SportletProperties.FILE_DOWNLOAD_BINARY);
         }
     }
 
     public boolean isDownload(HttpServletRequest req) {
         return (req.getAttribute(SportletProperties.FILE_DOWNLOAD_NAME) != null);
     }
 
     public void setTCKUser(PortletRequest req) {
         //String tck = (String)req.getPortletSession(true).getAttribute("tck");
         String[] portletNames = req.getParameterValues("portletName");
         if ((isTCK) || (portletNames != null)) {
             log.info("Setting a TCK user");
             SportletUserImpl u = new SportletUserImpl();
             u.setUserName("tckuser");
             u.setUserID("tckuser");
             u.setID("500");
             List groupList = new ArrayList();
             groupList.add(coreGroup.getName());
             req.setAttribute(SportletProperties.PORTLET_GROUP, coreGroup);
             req.setAttribute(SportletProperties.PORTLET_USER, u);
             req.setAttribute(SportletProperties.PORTLETGROUPS, groupList);
             req.setAttribute(SportletProperties.PORTLET_ROLE, new ArrayList());
             isTCK = true;
         }
     }
 
     public void setUserAndGroups(GridSphereEvent event) {
         // Retrieve user if there is one
         User user = null;
         PortletSession session = event.getPortletRequest().getPortletSession();
         String uid = (String) session.getAttribute(SportletProperties.PORTLET_USER);
         if (uid != null) {
             user = userManagerService.getUser(uid);
         }
 
         List groups = new ArrayList();
         if (user != null) {
             UserPrincipal userPrincipal = new UserPrincipal(user.getUserName());
             event.getPortletRequest().setAttribute(SportletProperties.PORTLET_USER_PRINCIPAL, userPrincipal);
             List mygroups = groupService.getGroups(user);
             Iterator it = mygroups.iterator();
             while (it.hasNext()) {
                 PortletGroup g = (PortletGroup) it.next();
                 groups.add(g.getName());
             }
             List proles = roleService.getRolesForUser(user);
             List roles = new ArrayList();
             it = proles.iterator();
             while (it.hasNext()) {
                 roles.add(((PortletRole)it.next()).getName());
             }
             PortletRequest req = event.getPortletRequest();
             // set user, role and groups in request
             req.setAttribute(SportletProperties.PORTLET_GROUP, coreGroup);
             req.setAttribute(SportletProperties.PORTLET_USER, user);
             req.setAttribute(SportletProperties.PORTLETGROUPS, groups);
             req.setAttribute(SportletProperties.PORTLET_ROLE, roles);
         }
     }
 
     // Dmitry Gavrilov (2005-03-17)
     // FIX for web container authorization
     private void checkWebContainerAuthorization(GridSphereEvent event) {
         PortletSession session = event.getPortletRequest().getPortletSession();
         if (session.getAttribute(SportletProperties.PORTLET_USER) != null) return;
         if(!(event.hasAction() && event.getAction().getName().equals(SportletProperties.LOGOUT))) {
             PortletRequest portletRequest = event.getPortletRequest();
             Principal principal = portletRequest.getUserPrincipal();
             if(principal != null) {
                 // fix for OC4J. it must work in Tomcat also
                 int indeDelimeter = principal.getName().lastIndexOf('/');
                 indeDelimeter = (indeDelimeter > 0) ? (indeDelimeter + 1) : 0;
                 String login = principal.getName().substring(indeDelimeter);
                 User user = userManagerService.getLoggedInUser(login);
                 if (user != null) {
                     setUserSettings(event, user);
                 }
             }
         }
     }
 
     protected void checkUserHasCookie(GridSphereEvent event) {
         PortletRequest req = event.getPortletRequest();
         User user = req.getUser();
         if (user == null) {
             Cookie[] cookies = req.getCookies();
             if (cookies != null) {
                 for (int i = 0; i < cookies.length; i++) {
                     Cookie c = cookies[i];
                     System.err.println("found a cookie:");
                     System.err.println("name=" + c.getName());
                     System.err.println("value=" + c.getValue());
                     if (c.getName().equals("gsuid")) {
 
                         String cookieVal = c.getValue();
                         int hashidx = cookieVal.indexOf("#");
                         if (hashidx > 0) {
                             String uid = cookieVal.substring(0, hashidx);
 
                             System.err.println("uid = " + uid);
 
                             String reqid = cookieVal.substring(hashidx+1);
                             System.err.println("reqid = " + reqid);
 
                             GenericRequest genreq = requestService.getRequest(reqid, COOKIE_REQUEST);
                             if (genreq != null) {
 
                                 if (genreq.getUserID().equals(uid)) {
                                     User newuser = userManagerService.getUser(uid);
                                     if (newuser != null) {
                                         System.err.println("in checkUserHasCookie-- seting user settings!!");
                                         setUserSettings(event, newuser);
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
         }
     }
 
     protected void setUserCookie(GridSphereEvent event) {
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
 
         User user = req.getUser();
         GenericRequest request = requestService.createRequest(COOKIE_REQUEST);
         Cookie cookie = new Cookie("gsuid", user.getID() + "#" + request.getOid());
         request.setUserID(user.getID());
         long time = Calendar.getInstance().getTime().getTime() + COOKIE_EXPIRATION_TIME * 1000;
         request.setLifetime(new Date(time));
         requestService.saveRequest(request);
 
         // COOKIE_EXPIRATION_TIME is specified in secs
         cookie.setMaxAge(COOKIE_EXPIRATION_TIME);
         res.addCookie(cookie);
         //System.err.println("adding a  cookie");
     }
 
     protected void removeUserCookie(GridSphereEvent event) {
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         Cookie[] cookies = req.getCookies();
         if (cookies != null) {
             for (int i = 0; i < cookies.length; i++) {
                 Cookie c = cookies[i];
                 if (c.getName().equals("gsuid")) {
                     int idx = c.getValue().indexOf("#");
                     if (idx > 0) {
                         String reqid = c.getValue().substring(idx+1);
                         //System.err.println("reqid= " + reqid);
                         GenericRequest request = requestService.getRequest(reqid, COOKIE_REQUEST);
                         if (request != null) requestService.deleteRequest(request);
                     }
                     c.setMaxAge(0);
                     res.addCookie(c);
                 }
             }
         }
 
     }
 
     /**
      * Handles login requests
      *
      * @param event a <code>GridSphereEvent</code>
      */
     protected void login(GridSphereEvent event) {
         String LOGIN_ERROR_FLAG = "LOGIN_FAILED";
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         try {
             User user = loginService.login(req);
 
             setUserSettings(event, user);
 
             String query = event.getAction().getParameter("queryString");
 
             String remme = req.getParameter("remlogin");
             if (remme != null) {
                 setUserCookie(event);
             } else {
                 removeUserCookie(event);
             }
             Boolean useSecureRedirect = Boolean.valueOf(GridSphereConfig.getProperty("use.https.redirect"));
 
            PortletURI uri = res.createURI(useSecureRedirect);
             if (query != null) {
                 uri.addParameter("cid", query);
             }
             res.sendRedirect(uri.toString());
         } catch (AuthorizationException err) {
             log.debug(err.getMessage());
             req.setAttribute(LOGIN_ERROR_FLAG, err.getMessage());
         } catch (AuthenticationException err) {
             log.debug(err.getMessage());
             req.setAttribute(LOGIN_ERROR_FLAG, err.getMessage());
         } catch (IOException e) {
             log.error("Unable to perform a redirect!", e);
         }
     }
 
     public void setUserSettings(GridSphereEvent event, User user) {
         PortletRequest req = event.getPortletRequest();
         PortletSession session = req.getPortletSession(true);
 
         req.setAttribute(SportletProperties.PORTLET_USER, user);
         session.setAttribute(SportletProperties.PORTLET_USER, user.getID());
         if (user.getAttribute(User.LOCALE) != null) {
             session.setAttribute(User.LOCALE, new Locale((String)user.getAttribute(User.LOCALE), "", ""));
         }
         setUserAndGroups(event);
         layoutEngine.loginPortlets(event);
     }
 
     /**
      * Handles logout requests
      *
      * @param event a <code>GridSphereEvent</code>
      */
     protected void logout(GridSphereEvent event) {
         getServletContext().log("in logout of GridSphere Servlet");
         PortletRequest req = event.getPortletRequest();
         removeUserCookie(event);
         PortletSession session = req.getPortletSession();
         layoutEngine.logoutPortlets(event);
         req.removeAttribute(SportletProperties.PORTLET_USER);
         req.removeAttribute(SportletProperties.PORTLET_USER_PRINCIPAL);
         //System.err.println("in logout of GS, calling invalidate on s=" + session.getId());
         session.invalidate();
         try {
             PortletResponse res = event.getPortletResponse();
             res.sendRedirect(res.createURI().toString());
         } catch (IOException e) {
             log.error("Unable to do a redirect!", e);
         }
     }
 
     /**
      * @see #doGet
      */
     public final void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
         doGet(req, res);
     }
 
     /**
      * Return the servlet info.
      *
      * @return a string with the servlet information.
      */
     public final String getServletInfo() {
         return "GridSphere Servlet";
     }
 
     /**
      * Shuts down the GridSphere portlet container
      */
     public final void destroy() {
         log.debug("in destroy: Shutting down services");
         //userSessionManager.destroy();
         layoutEngine.destroy();
         // Shutdown services
         factory.shutdownServices();
         // shutdown the persistencemanagers
         PersistenceManagerFactory.shutdown();
         System.gc();
     }
 
     /**
      * Record the fact that a servlet context attribute was added.
      *
      * @param event The session attribute event
      */
     public void attributeAdded(HttpSessionBindingEvent event) {
         try {
             log.debug("attributeAdded('" + event.getSession().getId() + "', '" +
                 event.getName() + "', '" + event.getValue() + "')");
         } catch (IllegalStateException e) {
             // do nothing
         }
     }
 
 
     /**
      * Record the fact that a servlet context attribute was removed.
      *
      * @param event The session attribute event
      */
     public void attributeRemoved(HttpSessionBindingEvent event) {
         try {
             log.debug("attributeRemoved('" + event.getSession().getId() + "', '" +
                 event.getName() + "', '" + event.getValue() + "')");
         } catch (IllegalStateException e) {
             // do nothing
         }
 
     }
 
 
     /**
      * Record the fact that a servlet context attribute was replaced.
      *
      * @param event The session attribute event
      */
     public void attributeReplaced(HttpSessionBindingEvent event) {
         try {
             log.debug("attributeReplaced('" + event.getSession().getId() + "', '" +
                 event.getName() + "', '" + event.getValue() + "')");
         } catch (IllegalStateException e) {
             // do nothing
         }
 
     }
 
 
     /**
      * Record the fact that this ui application has been destroyed.
      *
      * @param event The servlet context event
      */
     public void contextDestroyed(ServletContextEvent event) {
         ServletContext ctx = event.getServletContext();
         log.debug("contextDestroyed()");
         log.debug("contextName: " + ctx.getServletContextName());
         log.debug("context path: " + ctx.getRealPath(""));
     }
 
 
     /**
      * Record the fact that this ui application has been initialized.
      *
      * @param event The servlet context event
      */
     public void contextInitialized(ServletContextEvent event) {
         System.err.println("in contextInitialized of GridSphereServlet");
         ServletContext ctx = event.getServletContext();
         GridSphereConfig.setServletContext(ctx);
         log.debug("contextName: " + ctx.getServletContextName());
         log.debug("context path: " + ctx.getRealPath(""));
 
     }
 
     /**
      * Record the fact that a session has been created.
      *
      * @param event The session event
      */
     public void sessionCreated(HttpSessionEvent event) {
         System.err.println("sessionCreated('" + event.getSession().getId() + "')");
         sessionManager.sessionCreated(event);
     }
 
 
     /**
      * Record the fact that a session has been destroyed.
      *
      * @param event The session event
      */
     public void sessionDestroyed(HttpSessionEvent event) {
         sessionManager.sessionDestroyed(event);
         System.err.println("sessionDestroyed('" + event.getSession().getId() + "')");
     }
 
     public void updateDatabase() {
         // update group entries from 2.0.4 to 2.1
         List groupEntries = groupService.getUserGroups();
         Iterator it = groupEntries.iterator();
         while (it.hasNext()) {
             UserGroup ge = (UserGroup)it.next();
             String roleName = ge.getRoleName();
             //System.err.println(ge.getUser() + " " + ge.getGroup() + ge.getRole());
             if ((roleName != null) && !roleName.equals("")) {
                 if (ge.getUser() != null) {
                     //System.err.println("user= " + ge.getUser() + " role=" + roleName);
                     roleService.addUserToRole(ge.getUser(), roleService.getRole(roleName));
                     if (roleName.equalsIgnoreCase("SUPER")) {
                         roleService.addUserToRole(ge.getUser(), PortletRole.ADMIN);
                         roleService.addUserToRole(ge.getUser(), PortletRole.USER);
                     }
                     if (roleName.equalsIgnoreCase("ADMIN")) {
                         roleService.addUserToRole(ge.getUser(), PortletRole.USER);
                     }
                     ge.setRoleName("");
                     groupService.saveUserGroup(ge);
                 }
             }
             PortletRole role = ge.getRole();
             if (role != null) {
                 if (ge.getUser() != null) {
                     //System.err.println("user1= " + ge.getUser() + " role=" + roleName);
                     roleService.addUserToRole(ge.getUser(), role);
                     if (role.equals(PortletRole.SUPER)) {
                         roleService.addUserToRole(ge.getUser(), PortletRole.ADMIN);
                         roleService.addUserToRole(ge.getUser(), PortletRole.USER);
                     }
                     if (role.equals(PortletRole.ADMIN)) {
                         roleService.addUserToRole(ge.getUser(), PortletRole.USER);
                     }
                     ge.setRole(null);
                     groupService.saveUserGroup(ge);
                 }
             }
         }
         List groups = groupService.getGroups();
         it = groups.iterator();
         while (it.hasNext()) {
             PortletGroup group = (PortletGroup)it.next();
             Set portletSet = group.getPortletRoleList();
             Iterator portletSetIt = portletSet.iterator();
             while (portletSetIt.hasNext()) {
                 SportletRoleInfo roleInfo = (SportletRoleInfo)portletSetIt.next();
                 String roleName = roleInfo.getRole();
                 PortletRole portletRole = roleService.getRole(roleName);
                 if (portletRole != null) {
                     if (portletRole.getName().equalsIgnoreCase("GUEST")) portletRole = roleService.getRole("USER");
                     roleInfo.setSportletRole(portletRole);
                     roleInfo.setRole("");
                 }
             }
             groupService.saveGroup(group);
         }
         // eliminate GUEST role
         PortletRole guest = roleService.getRole("GUEST");
         if (guest != null) roleService.deleteRole(guest);
     }
 }
