 /*
 * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
 * @version $Id: GridSphereServlet.java 4956 2006-07-26 16:15:56Z novotny $
 */
 package org.gridsphere.servlets;
 
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.gridsphere.filters.PortalFilter;
 import org.gridsphere.filters.impl.descriptor.PortalFilterDescriptor;
 import org.gridsphere.layout.PortletLayoutEngine;
 import org.gridsphere.layout.PortletPageFactory;
 import org.gridsphere.services.core.user.User;
 import org.gridsphere.services.core.user.UserPrincipal;
 import org.gridsphere.portlet.impl.PortletContextImpl;
 import org.gridsphere.portlet.impl.SportletProperties;
 import org.gridsphere.services.core.user.impl.UserImpl;
 import org.gridsphere.portlet.service.PortletServiceException;
 import org.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridsphere.portlet.service.spi.impl.descriptor.PortletServiceCollection;
 import org.gridsphere.portletcontainer.GridSphereEvent;
 import org.gridsphere.portletcontainer.PortletDispatcherException;
 import org.gridsphere.portletcontainer.impl.GridSphereEventImpl;
 import org.gridsphere.portletcontainer.impl.PortletServiceDescriptor;
 import org.gridsphere.portletcontainer.impl.PortletSessionManager;
 import org.gridsphere.services.core.persistence.PersistenceManagerException;
 import org.gridsphere.services.core.persistence.PersistenceManagerRdbms;
 import org.gridsphere.services.core.persistence.PersistenceManagerService;
 import org.gridsphere.services.core.portal.PortalConfigService;
 import org.gridsphere.services.core.registry.PortletManagerService;
 import org.gridsphere.services.core.security.auth.AuthenticationException;
 import org.gridsphere.services.core.security.auth.AuthorizationException;
 import org.gridsphere.services.core.security.auth.LoginService;
 import org.gridsphere.services.core.security.role.PortletRole;
 import org.gridsphere.services.core.security.role.RoleManagerService;
 import org.gridsphere.services.core.user.UserManagerService;
 import org.hibernate.StaleObjectStateException;
 
 import javax.activation.DataHandler;
 import javax.activation.FileDataSource;
 import javax.portlet.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.SocketException;
 import java.security.Principal;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Calendar;
 
 
 /**
  * The <code>GridSphereServlet</code> is the GridSphere portlet container.
  * All portlet requests get proccessed by the GridSphereServlet before they
  * are rendered.
  */
 public class GridSphereServlet extends HttpServlet implements ServletContextListener, HttpSessionListener {
 
     private Log log = LogFactory.getLog(GridSphereServlet.class);
 
     /* GridSphere Portlet Registry Service */
     private PortletManagerService portletManager = null;
 
     /* GridSphere Access Control Service */
     private RoleManagerService roleService = null;
 
     private UserManagerService userManagerService = null;
 
     private PortalConfigService portalConfigService = null;
 
     private LoginService loginService = null;
 
     /* GridSphere Portlet layout Engine handles rendering */
     private PortletLayoutEngine layoutEngine = PortletLayoutEngine.getInstance();
 
     private PortletSessionManager sessionManager = PortletSessionManager.getInstance();
 
     private List<PortalFilter> portalFilters = null;
 
     private boolean firstDoGet = true;
 
     private boolean isTCK = false;
 
     /**
      * Initializes the GridSphere portlet container
      *
      * @param config the <code>ServletConfig</code>
      * @throws ServletException if an error occurs during initialization
      */
     public final void init(ServletConfig config) throws ServletException {
         super.init(config);
         log.info("in init of GridSphereServlet");
         String descriptorPath = config.getServletContext().getRealPath("/WEB-INF/GridSphereServices.xml");
         // add core gridsphere services to ServiceFactory
         PortletServiceDescriptor descriptor = null;
         try {
             log.debug("loading from: " + descriptorPath);
             descriptor = new PortletServiceDescriptor(descriptorPath);
             PortletServiceCollection serviceCollection = descriptor.getServiceCollection();
             PortletServiceFactory.addServices("gridsphere", config.getServletContext(), serviceCollection, Thread.currentThread().getContextClassLoader());
         } catch (PersistenceManagerException e) {
             //log.error("error unmarshalling " + servicesPath + " using " + servicesMappingPath + " : " + e.getMessage());
             throw new PortletServiceException("error unmarshalling " + descriptorPath, e);
         }
         PortletLayoutEngine layoutEngine = PortletLayoutEngine.getInstance();
         layoutEngine.init(config.getServletContext());
 
         /*
         String realPath = config.getServletContext().getRealPath("");
         int l = realPath.lastIndexOf(File.separator);
         String ctxPath = realPath.substring(l + 1);
         System.err.println("ctx path=" + ctxPath);
         portalConfigService = (PortalConfigService)PortletServiceFactory.createPortletService(PortalConfigService.class, true);
         if (ctxPath.equals("ROOT")) ctxPath = "";
         portalConfigService.setProperty("gridsphere.deploy", ctxPath);
         try {
             portalConfigService.storeProperties();
         } catch (IOException e) {
             log.error("Unable to write to properties file!");
         }
         */
     }
 
     private void initializeServices() throws PortletServiceException {
         roleService = (RoleManagerService) PortletServiceFactory.createPortletService(RoleManagerService.class, true);
         userManagerService = (UserManagerService) PortletServiceFactory.createPortletService(UserManagerService.class, true);
         portalConfigService = (PortalConfigService) PortletServiceFactory.createPortletService(PortalConfigService.class, true);
 
         loginService = (LoginService) PortletServiceFactory.createPortletService(LoginService.class, true);
         portletManager = (PortletManagerService) PortletServiceFactory.createPortletService(PortletManagerService.class, true);
         String filterDescriptorPath = getServletContext().getRealPath("/WEB-INF/filters.xml");
         try {
             PortalFilterDescriptor filterDescriptor = new PortalFilterDescriptor(getServletConfig(), filterDescriptorPath);
             portalFilters = filterDescriptor.getPortalFilters();
         } catch (PersistenceManagerException e) {
             //log.error("error unmarshalling " + servicesPath + " using " + servicesMappingPath + " : " + e.getMessage());
             throw new PortletServiceException("error unmarshalling " + filterDescriptorPath, e);
         }
 
     }
 
 
     public void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 
         PersistenceManagerService pms = null;
 
         pms = (PersistenceManagerService)PortletServiceFactory.createPortletService(PersistenceManagerService.class, true);
 
         PersistenceManagerRdbms pm = null;
         try {
             log.info("Starting a database transaction");
 
             pm = pms.createGridSphereRdbms();
             pm.beginTransaction();
 
             processRequest(req, res);
             // Commit and cleanup
             log.info("Committing the database transaction");
 
             pm.endTransaction();
         } catch (StaleObjectStateException staleEx) {
             log.error("This interceptor does not implement optimistic concurrency control!");
             log.error("Your application will not work until you add compensation actions!");
             // Rollback, close everything, possibly compensate for any permanent changes
             // during the conversation, and finally restart business conversation. Maybe
             // give the user of the application a chance to merge some of his work with
             // fresh data... what you do here depends on your applications design.
             //throw staleEx;
         } catch (Throwable ex) {
             ex.printStackTrace();
             pm.endTransaction();
             try {
                 pm.rollbackTransaction();
             } catch (Throwable rbEx) {
                 log.error("Could not rollback transaction after exception!", rbEx);
             }
             // Let others handle it... maybe another interceptor for exceptions?
             //throw new ServletException(ex);
         }
     }
 
     /**
      * Processes GridSphere portal framework requests
      *
      * @param req the <code>HttpServletRequest</code>
      * @param res the <code>HttpServletResponse</code>
      * @throws IOException      if an I/O error occurs
      * @throws ServletException if a servlet error occurs
      */
     public void processRequest(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
 
         if (firstDoGet) {
             initializeServices();
             updateDatabase();
             firstDoGet = false;
         }
 
         long startTime = System.currentTimeMillis();
 
         PortletContext ctx = new PortletContextImpl(getServletContext());
         GridSphereEvent event = new GridSphereEventImpl(ctx, req, res);
 
 
         // check to see if user has been authorized by means of container managed authorization
         checkWebContainerAuthorization(event);
 
         for (PortalFilter filter : portalFilters) {
             filter.doBeforeEveryRequest(req, res);
         }
 
         // Used for TCK tests
 
         if (isTCK) {
             req.setAttribute(SportletProperties.LAYOUT_PAGE, PortletPageFactory.TCK_PAGE);
             setTCKUser(req);
         } else {
             setUserAndRoles(event);
         }
 
         // Handle user login and logout
         if (event.hasAction()) {
             String actionName = event.getAction().getName();
             if (actionName.equals(SportletProperties.LOGIN)) {
                 String LOGIN_ERROR_FLAG = "LOGIN_FAILED";
                 try {
                     login(event);
                     setUserAndRoles(event);
                     return;
                 } catch (AuthorizationException err) {
                     log.debug(err.getMessage());
                     req.getSession(true).setAttribute(LOGIN_ERROR_FLAG, err.getMessage());
                 } catch (AuthenticationException err) {
                     log.debug(err.getMessage());
                     req.getSession(true).setAttribute(LOGIN_ERROR_FLAG, err.getMessage());
                 }
             }
             if (actionName.equals(SportletProperties.LOGOUT)) {
                 logout(event);
                 long endTime = System.currentTimeMillis();
                 System.err.println("Page render time = " + (endTime - startTime) + " (ms) request= " + req.getQueryString());
                 return;
             }
         }
 

         layoutEngine.actionPerformed(event);
 
         // perform a redirect-after-POST!
         if (event.hasAction() && req.getMethod().equalsIgnoreCase("POST")) {
             String requestURL = (String)req.getAttribute(SportletProperties.PORTAL_REDIRECT_PATH);
             log.debug("redirect after POST to: " + requestURL);
             if (req.getParameter("ajax") == null) {
                 res.sendRedirect(requestURL.toString());
                 return;
             }           
         }
 
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
 
 
 
         // Used for TCK tests
         if (isTCK) {
             setTCKUser(req);
         } else {
             setUserAndRoles(event);
         }
 
         layoutEngine.service(event);
 
         for (PortalFilter portalFilter : portalFilters) {
             PortalFilter filter = (PortalFilter) portalFilter;
             filter.doAfterEveryRequest(req, res);
         }
 
         //log.debug("Portlet service factory stats");
         //factory.logStatistics();
         long endTime = System.currentTimeMillis();
         System.err.println("Page render time = " + (endTime - startTime) + " (ms) request= " + req.getQueryString());
         sessionManager.dumpSessions();
         System.err.println("after dump");
 
         //event.getRenderResponse().createRenderURL();
     }
 
     /**
      * Method to set the response headers to perform file downloads to a browser
      *
      * @param req the HttpServletRequest
      * @param res the HttpServletResponse
      * @throws PortletException if a portlet exception occurs
      * @throws IOException if an IO error occurs
      */
     public void downloadFile(HttpServletRequest req, HttpServletResponse res) throws PortletException, IOException {
 
         String fileName = (String) req.getAttribute(SportletProperties.FILE_DOWNLOAD_NAME);
         if (fileName == null) return;
         String path = (String) req.getAttribute(SportletProperties.FILE_DOWNLOAD_PATH);
         Boolean deleteFile = (Boolean)req.getAttribute(SportletProperties.FILE_DELETE);
         File file = (File) req.getAttribute(SportletProperties.FILE_DOWNLOAD_BINARY);
 
         req.removeAttribute(SportletProperties.FILE_DOWNLOAD_NAME);
         req.removeAttribute(SportletProperties.FILE_DOWNLOAD_PATH);
         req.removeAttribute(SportletProperties.FILE_DELETE);
         req.removeAttribute(SportletProperties.FILE_DOWNLOAD_BINARY);
 
         try {
             if (file == null) {
                 file = new File(path + fileName);
             }
             if (deleteFile == null) deleteFile = Boolean.FALSE;
             log.debug("in downloadFile");
             log.debug("filename: " + fileName + " filepath= " + path);
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
         }
     }
 
     public boolean isDownload(HttpServletRequest req) {
         return (req.getAttribute(SportletProperties.FILE_DOWNLOAD_NAME) != null);
     }
 
     public void setTCKUser(HttpServletRequest req) {
         log.info("Setting a TCK user");
         UserImpl u = new UserImpl();
         u.setUserName("tckuser");
         u.setUserID("tckuser");
         u.setID("500");
         req.setAttribute(SportletProperties.PORTLET_USER, u);
         req.setAttribute(SportletProperties.PORTLET_ROLE, new ArrayList());
         isTCK = true;
     }
 
     public void setUserAndRoles(GridSphereEvent event) {
         // Retrieve user if there is one
         HttpServletRequest req = event.getHttpServletRequest();
         HttpSession session = req.getSession(true);
         User user = null;
         String uid = (String) session.getAttribute(SportletProperties.PORTLET_USER);
         if (uid != null) {
             user = userManagerService.getUser(uid);
         }
         List<String> roles = new ArrayList<String>();
         if (user != null) {
             UserPrincipal userPrincipal = new UserPrincipal(user.getUserName());
             req.setAttribute(SportletProperties.PORTLET_USER_PRINCIPAL, userPrincipal);
             List<PortletRole> proles = roleService.getRolesForUser(user);
             for (PortletRole prole : proles) {
                 roles.add(prole.getName());
             }
         }
 
         
 
         // set user, role and groups in request
         req.setAttribute(SportletProperties.PORTLET_USER, user);
         req.setAttribute(SportletProperties.PORTLET_ROLE, roles);
     }
 
     // Dmitry Gavrilov (2005-03-17)
     // FIX for web container authorization
     private void checkWebContainerAuthorization(GridSphereEvent event) {
         PortletRequest request = event.getActionRequest();
         PortletSession session = request.getPortletSession();
         if (session.getAttribute(SportletProperties.PORTLET_USER) != null) return;
         if(!(event.hasAction() && event.getAction().getName().equals(SportletProperties.LOGOUT))) {
             Principal principal = request.getUserPrincipal();
             if(principal != null) {
                 // fix for OC4J. it must work in Tomcat also
                 int indeDelimeter = principal.getName().lastIndexOf('/');
                 indeDelimeter = (indeDelimeter > 0) ? (indeDelimeter + 1) : 0;
                 String login = principal.getName().substring(indeDelimeter);
                 User user = userManagerService.getLoggedInUser(login);
                 if (user != null) {
                     request.setAttribute(SportletProperties.PORTLET_USER, user);
                     session.setAttribute(SportletProperties.PORTLET_USER, user.getID(), PortletSession.APPLICATION_SCOPE);
                 }
             }
         }
     }
 
     /**
      * Handles login requests
      *
      * @param event a <code>GridSphereEvent</code>
      * @throws AuthenticationException if auth fails
      * @throws AuthorizationException if authz fails
      */
     protected void login(GridSphereEvent event) throws AuthenticationException, AuthorizationException {
 
         System.err.println("in login of GS servlet!!");
 
 
         HttpServletRequest req = event.getHttpServletRequest();
         RenderResponse res = event.getRenderResponse();
 
         User user = loginService.login(req);
         Long now = Calendar.getInstance().getTime().getTime();
         user.setLastLoginTime(now);
         Integer numLogins = user.getNumLogins();
         if (numLogins == null) numLogins = 0;
         numLogins++;
 
         user.setNumLogins(numLogins);
         userManagerService.saveUser(user);
 
         req.setAttribute(SportletProperties.PORTLET_USER, user);
         req.getSession(true).setAttribute(SportletProperties.PORTLET_USER, user.getID());
 
         String query = event.getAction().getParameter("queryString");
 
         PortletURL uri = res.createActionURL();
         if (query != null) {
             uri.setParameter("cid", query);
         }
         req.setAttribute(SportletProperties.LAYOUT_PAGE, PortletPageFactory.USER_PAGE);
 
         String realuri = uri.toString().substring("http".length());
         Boolean useSecureRedirect = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.USE_HTTPS_REDIRECT));
         if (useSecureRedirect.booleanValue()) {
             realuri = "https" + realuri;
         } else {
             realuri = "http" + realuri;
         }
 
         for (PortalFilter filter : portalFilters) {
             filter.doAfterLogin(event.getHttpServletRequest(), event.getHttpServletResponse());
         }
 
         
         log.debug("in login redirecting to portal: " + realuri.toString());
         try {
             if (req.getParameter("ajax") != null) {
                 res.setContentType("text/html");
                 res.getWriter().print(realuri.toString());    
             } else {
                 event.getHttpServletResponse().sendRedirect(realuri.toString());
             }
         } catch (IOException e) {
             log.error("Unable to perform a redirect!", e);
         }
     }
 
     /**
      * Handles logout requests
      *
      * @param event a <code>GridSphereEvent</code>
      */
     protected void logout(GridSphereEvent event) {
         log.debug("in logout of GridSphere Servlet");
         PortletRequest req = event.getActionRequest();
         RenderResponse res = event.getRenderResponse();
         //removeUserCookie(event);
 
         req.removeAttribute(SportletProperties.PORTLET_USER);
         req.removeAttribute(SportletProperties.PORTLET_USER_PRINCIPAL);
 
         try {
             portletManager.logoutAllPortletWebApplications(event.getHttpServletRequest(), event.getHttpServletResponse());
         } catch (PortletDispatcherException e) {
             log.error("Failed to logout portlets!", e);
         }
 
         //req.getPortletSession(true).invalidate();
 
         for (PortalFilter filter : portalFilters) {
             filter.doAfterLogout(event.getHttpServletRequest(), event.getHttpServletResponse());
         }
         //pageFactory.
 
         try {
             event.getActionResponse().sendRedirect(res.createRenderURL().toString());
         } catch (IOException e) {
             log.error("Unable to do a redirect!", e);
         }
 
     }
 
     /**
      * @see #doGet
      */
     public final void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
         System.err.println("do post!!");
         
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
         // Shutdown services
         PortletServiceFactory.shutdownServices();
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
         log.info("contextName: " + ctx.getServletContextName());
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
         // loop thru users make sure first and last name are created from full name
         List<User> users = userManagerService.getUsers();     
         for (User user : users) {
             if (user.getFirstName().equals("") && user.getLastName().equals("")) {
                 String full = user.getFullName();
                 int idx = full.lastIndexOf(" ");
                 if (idx > 0) {
                     user.setFirstName(full.substring(0, idx));
                     user.setLastName(full.substring(idx + 1));
                 } else {
                     user.setFirstName(full);
                 }
                 Integer numLogins = user.getNumLogins();
                 if (numLogins == null) numLogins = 0;
                 user.setNumLogins(numLogins++);
                 userManagerService.saveUser(user);
             }
         }
     }
 }
