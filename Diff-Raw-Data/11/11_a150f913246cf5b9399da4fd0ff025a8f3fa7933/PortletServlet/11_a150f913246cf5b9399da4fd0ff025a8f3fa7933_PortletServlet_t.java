 /*
 * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
 * @version $Id$
 */
 package org.gridlab.gridsphere.provider.portlet.jsr;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.jsrimpl.*;
 
 import org.gridlab.gridsphere.portlet.PortletLog;
 import org.gridlab.gridsphere.portlet.User;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portlet.impl.ClientImpl;
 
 import org.gridlab.gridsphere.portletcontainer.PortletRegistry;
 import org.gridlab.gridsphere.portletcontainer.ApplicationPortletConfig;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.JSRApplicationPortletImpl;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.JSRPortletWebApplicationImpl;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.*;
 import org.gridlab.gridsphere.portletcontainer.jsrimpl.descriptor.types.TransportGuaranteeType;
 import org.gridlab.gridsphere.services.core.registry.impl.PortletManager;
 import org.gridlab.gridsphere.services.core.user.LoginService;
 
 import javax.portlet.*;
 import javax.portlet.PortletMode;
 import javax.portlet.Portlet;
 import javax.portlet.PortletConfig;
 import javax.portlet.PortletContext;
 import javax.portlet.PortletException;
 import javax.portlet.PortletRequest;
 import javax.servlet.*;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSessionActivationListener;
 import javax.servlet.http.HttpSessionAttributeListener;
 import javax.servlet.http.HttpSessionBindingEvent;
 import javax.servlet.http.HttpSessionEvent;
 import javax.servlet.http.HttpSessionListener;
 import javax.portlet.UnavailableException;
 import java.io.*;
 import java.util.*;
 import java.util.ResourceBundle;
 
 public class PortletServlet extends HttpServlet
         implements Servlet, ServletConfig, 
         HttpSessionAttributeListener, HttpSessionListener, HttpSessionActivationListener {
 
     protected transient static PortletLog log = SportletLog.getInstance(PortletServlet.class);
     protected transient static PortletRegistry registry = null;
 
     protected JSRPortletWebApplicationImpl portletWebApp = null;
     
     private PortletManager manager = PortletManager.getInstance();
 
     protected PortletContext portletContext = null;
 
     protected Map portlets = null;
     protected Map portletclasses = null;
 
     protected Map portletConfigHash = null;
 
     //private PortletPreferencesManager prefsManager = null;
 
     private Map userKeys = new HashMap();
     private List securePortlets = new ArrayList();
 
     public void init(ServletConfig config) throws ServletException {
         super.init(config);
         log.debug("in init of PortletServlet");
         portlets = new Hashtable();
         portletclasses = new Hashtable();
         portletConfigHash = new Hashtable();
     }
 
     public void initJSRPortletWebapp() {
         ServletContext ctx = this.getServletContext();
         try {
             portletWebApp = new JSRPortletWebApplicationImpl(ctx, Thread.currentThread().getContextClassLoader());
         } catch (Exception e) {
             log.error("Unable to create portlet web application ", e);
             return;
         }
         Collection appPortlets = portletWebApp.getAllApplicationPortlets();
         Iterator it = appPortlets.iterator();
         while (it.hasNext()) {
             JSRApplicationPortletImpl appPortlet = (JSRApplicationPortletImpl) it.next();
             String portletClass = appPortlet.getApplicationPortletClassName();
             String portletName = appPortlet.getApplicationPortletName();
             try {
                 // instantiate portlet classes
                 Portlet portletInstance = (Portlet) Class.forName(portletClass).newInstance();
                 //portlets.put(portletClass, portletInstance);
                 portlets.put(portletName, portletInstance);
 
                 // mappings between names and classes
                 portletclasses.put(portletClass, portletName);
                 log.debug("Creating new portlet instance: " + portletClass);
 
                 // put portlet web app in registry
 
             } catch (Exception e) {
                 log.error("Unable to create jsr portlet instance: " + portletClass, e);
                 return;
             }
         }
 
         UserAttribute[] userAttrs = portletWebApp.getUserAttributes();
         if (userAttrs != null) {
             String key = null;
             for (int i = 0; i < userAttrs.length; i++) {
                 key = userAttrs[i].getName().getContent();
                 userKeys.put(key, "");
             }
         }
 
         SecurityConstraint[] secConstraints = portletWebApp.getSecurityConstraints();
         if (secConstraints != null) {
             for (int i = 0; i < secConstraints.length; i++) {
                 PortletCollection portlets = secConstraints[i].getPortletCollection();
                 PortletName[] names = portlets.getPortletName();
                 UserDataConstraint userConstraint = secConstraints[i].getUserDataConstraint();
                 TransportGuaranteeType guaranteeType = userConstraint.getTransportGuarantee();
                 if (guaranteeType.equals(TransportGuaranteeType.NONE)) {
                     names = null;
                 }
                 if (names != null) {
                     for (int j = 0; j < names.length; j++) {
                         securePortlets.add(names[j].getContent());
                     }
                 }
             }
         }
 
 
         // create portlet context
         portletContext = new PortletContextImpl(ctx);
 
         // load in any authentication modules if found-- this is a GridSphere extension
         PortletServiceFactory factory = SportletServiceFactory.getInstance();
         try {
             LoginService loginService = (LoginService)factory.createPortletService(LoginService.class, true);
             InputStream is = this.getServletContext().getResourceAsStream("/WEB-INF/authmodules.xml");
             if (is != null) {
                 String authModulePath = this.getServletContext().getRealPath("/WEB-INF/authmodules.xml");
                 loginService.loadAuthModules(authModulePath, Thread.currentThread().getContextClassLoader());
                 log.info("loading authentication modules from: " + authModulePath);
             } else {
                 log.debug("no auth module descriptor found");
             }
         } catch (PortletServiceException e) {
             log.error("Unable to create login service instance!", e);
         }
     }
 
     public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
 
         // security check
         // make sure request comes only from gridsphere servlet same ip
         //System.err.println("remote Address: " + request.getRemoteAddr());
 
         registry = PortletRegistry.getInstance();
         // If no portlet ID exists, this may be a command to init or shutdown a portlet instance
 
         // currently either all portlets are initialized or shutdown, not one individually...
         String method = (String) request.getAttribute(SportletProperties.PORTLET_LIFECYCLE_METHOD);
 
         if (method.equals(SportletProperties.INIT)) {
             initJSRPortletWebapp();
             Set set = portlets.keySet();
             Iterator it = set.iterator();
             while (it.hasNext()) {
                 String portletName = (String) it.next();
                 Portlet portlet = (Portlet) portlets.get(portletName);
                 log.debug("in PortletServlet: service(): Initializing portlet " + portletName);
                 PortletDefinition portletDef = portletWebApp.getPortletDefinition(portletName);
                 PortletConfig portletConfig = new PortletConfigImpl(getServletConfig(), portletDef, Thread.currentThread().getContextClassLoader());
                 try {
                     portlet.init(portletConfig);
                     portletConfigHash.put(portletName, portletConfig);
                 } catch (Exception e) {
                     log.error("in PortletServlet: service(): Unable to INIT portlet " + portletName, e);
                     // PLT.5.5.2.1 Portlet that fails to initialize must not be placed in active service
                     it.remove();
                 }
             }
             manager.addWebApp(portletWebApp);
             return;
         } else if (method.equals(SportletProperties.INIT_CONCRETE)) {
             // do nothing for concrete portlets
             return;
         } else if (method.equals(SportletProperties.DESTROY)) {
             Iterator it = portlets.keySet().iterator();
             while (it.hasNext()) {
                 String portletName = (String) it.next();
                 Portlet portlet = (Portlet) portlets.get(portletName);
                 log.debug("in PortletServlet: service(): Destroying portlet " + portletName);
                 try {
                     portlet.destroy();
                 } catch (RuntimeException e) {
                     log.error("Caught exception during portlet destroy", e);
                 }
             }
             return;
         } else if (method.equals(SportletProperties.DESTROY_CONCRETE)) {
             // do nothing for concrete portlets
             return;
         } else if (method.equals(SportletProperties.LOGIN)) {
 
         } else if (method.equals(SportletProperties.LOGOUT)) {
             request.getSession(true).invalidate();
         }
 
         // There must be a portlet ID to know which portlet to service
         String pid = (String) request.getAttribute(SportletProperties.PORTLETID);
         String cid = (String) request.getAttribute(SportletProperties.COMPONENT_ID);
 
         if (pid == null) {
             // it may be in the request parameter
             pid = request.getParameter(SportletProperties.PORTLETID);
             if (pid == null) {
                 log.debug("in PortletServlet: service(): No PortletID found in request!");
                 return;
             }
             request.setAttribute(SportletProperties.PORTLETID, pid);
         }
 
         log.debug("have a portlet id " + pid + " component id= " + cid);
 
         String portletName = "";
         int idx = pid.indexOf("#");
         Portlet portlet = null;
         if (idx > 0) {
             portletName = pid.substring(idx+1);
             portlet = (Portlet) portlets.get(portletName);
             request.setAttribute(SportletProperties.PORTLET_CONFIG, portletConfigHash.get(portletName));
             // this hack uses the portletclasses hash that identifies classname to portlet mappings
         } else {
             portletName = (String)portletclasses.get(pid);
             portlet = (Portlet)portlets.get(portletName);
             request.setAttribute(SportletProperties.PORTLET_CONFIG, portletConfigHash.get(portletName));
         }
 
         JSRApplicationPortletImpl appPortlet =
                 (JSRApplicationPortletImpl) registry.getApplicationPortlet(pid);
 
         if (appPortlet == null) {
             log.error("Unable to get portlet from registry identified by: " + pid);
             return;
         }
 
         // set the supported mime types
         Supports[] supports = appPortlet.getSupports();
         List mimeTypes = new ArrayList();
         for (int i = 0; i < supports.length; i++) {
             Supports s = supports[i];
             String mimeType = s.getMimeType().getContent();
             mimeTypes.add(mimeType);
             request.setAttribute(SportletProperties.MIME_TYPES, mimeTypes);
         }
 
         setPortletModes(request, appPortlet);
 
         // perform user conversion from gridsphere to JSR model
         User user = (User) request.getAttribute(SportletProperties.PORTLET_USER);
         Map userInfo;
 
         if (user != null) {
             userInfo = new HashMap();
             userInfo.putAll(userKeys);
             if (userInfo.containsKey("user.name")) userInfo.put("user.name", user.getUserName());
             if (userInfo.containsKey("user.id")) userInfo.put("user.id", user.getID());
             if (userInfo.containsKey("user.email")) userInfo.put("user.email", user.getEmailAddress());
             if (userInfo.containsKey("user.organization")) userInfo.put("user.organization", user.getOrganization());
             if (userInfo.containsKey("user.lastlogintime")) userInfo.put("user.lastlogintime", new Long(user.getLastLoginTime()).toString());
             if (userInfo.containsKey("user.name.full")) userInfo.put("user.name.full", user.getFullName());
             if (userInfo.containsKey("user.timezone")) userInfo.put("user.timezone", user.getAttribute(User.TIMEZONE));
             if (userInfo.containsKey("user.locale")) userInfo.put("user.locale", user.getAttribute(User.LOCALE));
             if (userInfo.containsKey("user.theme")) userInfo.put("user.theme", user.getAttribute(User.THEME));
             if (userInfo.containsKey("user.role")) userInfo.put("user.role", ((PortletRole)request.getAttribute(SportletProperties.PORTLET_ROLE)).getName());
             if (userInfo.containsKey("user.login.id")) userInfo.put("user.login.id", user.getUserName());
 
             Enumeration e = user.getAttributeNames();
             while (e.hasMoreElements()) {
                 String key = (String)e.nextElement();
                 if (userInfo.containsKey(key)) userInfo.put(key, user.getAttribute(key));
             }
 
 
             //userInfo.put("user.name.given", user.getGivenName());
             //userInfo.put("user.name.family", user.getFamilyName());
             request.setAttribute(PortletRequest.USER_INFO, userInfo);
 
             // create user principal
             UserPrincipal userPrincipal = new UserPrincipal(user.getUserName());
             request.setAttribute(SportletProperties.PORTLET_USER_PRINCIPAL, userPrincipal);
         }
 
 
         /*
         UserAttribute[] userAttrs = portletWebApp.getUserAttributes();
         for (int i = 0; i < userAttrs.length; i++) {
             UserAttribute userAttr = userAttrs[i];
             String name = userAttr.getName().getContent();
             userInfo.put(name, "");
         }
         request.setAttribute(PortletRequest.USER_INFO, userInfo);
         */
 
         // portlet preferences
         PortalContext portalContext = appPortlet.getPortalContext();
         request.setAttribute(SportletProperties.PORTAL_CONTEXT, portalContext);
         if (portlet == null) {
             log.error("in PortletServlet: service(): No portlet matching " + pid + " found!");
             return;
         }
 
         request.removeAttribute(SportletProperties.SSL_REQUIRED);
         if (securePortlets.contains(pid)) {
             request.setAttribute(SportletProperties.SSL_REQUIRED, "true");
         }
 
         if (method.equals(SportletProperties.SERVICE)) {
             String action = (String) request.getAttribute(SportletProperties.PORTLET_ACTION_METHOD);
             if (action != null) {
                 log.debug("in PortletServlet: action is not NULL");
                 if (action.equals(SportletProperties.DO_TITLE)) {
                     RenderRequest renderRequest = new RenderRequestImpl(request, portalContext, portletContext, supports);
                     RenderResponse renderResponse = new RenderResponseImpl(request, response, portalContext);
                     renderRequest.setAttribute(SportletProperties.RENDER_REQUEST, renderRequest);
                     renderRequest.setAttribute(SportletProperties.RENDER_RESPONSE, renderResponse);
                     log.debug("in PortletServlet: do title " + pid);
                     try {
                         doTitle(portlet, renderRequest, renderResponse);
                     } catch (PortletException e) {
                         log.error("Error during doTitle:", e);
                        request.setAttribute(SportletProperties.PORTLETERROR + pid, new org.gridlab.gridsphere.portlet.PortletException(e));
                     }
                 } else if (action.equals(SportletProperties.WINDOW_EVENT)) {
                     // do nothing
                 } else if (action.equals(SportletProperties.MESSAGE_RECEIVED)) {
                     // do nothing
                 } else if (action.equals(SportletProperties.ACTION_PERFORMED)) {
                     PortletPreferencesManager prefsManager = new PortletPreferencesManager();
                     javax.portlet.PreferencesValidator validator = appPortlet.getPreferencesValidator();
                     prefsManager.setValidator(validator);
                     prefsManager.setIsRender(false);
                     prefsManager.setUserId(user.getID());
                     prefsManager.setPortletId(appPortlet.getApplicationPortletID());
                     prefsManager.setPreferencesDesc(appPortlet.getPreferencesDescriptor());
                     request.setAttribute(SportletProperties.PORTLET_PREFERENCES_MANAGER, prefsManager);
 
                     ActionRequestImpl actionRequest = new ActionRequestImpl(request, portalContext, portletContext, supports);
                     ActionResponse actionResponse = new ActionResponseImpl(request, response, portalContext);
 
                     log.debug("in PortletServlet: action handling portlet " + pid);
                     try {
                         // INVOKE PORTLET ACTION
                         portlet.processAction(actionRequest, actionResponse);
                         Map params = ((ActionResponseImpl) actionResponse).getRenderParameters();
                         actionRequest.setAttribute(SportletProperties.RENDER_PARAM_PREFIX + pid + "_" + cid, params);
                         log.debug("placing render params in attribute: key= " + SportletProperties.RENDER_PARAM_PREFIX + pid + "_" + cid);
                         redirect(request, response, actionRequest, actionResponse, portalContext);
                     } catch (Exception e) {
                         log.error("Error during processAction:", e);
                         request.setAttribute(SportletProperties.PORTLETERROR + pid, new org.gridlab.gridsphere.portlet.PortletException(e));
                     }
                 }
             } else {
                 if (user != null) {
                     PortletPreferencesManager prefsManager = new PortletPreferencesManager();
                     javax.portlet.PreferencesValidator validator = appPortlet.getPreferencesValidator();
                     prefsManager.setValidator(validator);
                     prefsManager.setIsRender(true);
                     prefsManager.setUserId(user.getID());
                     prefsManager.setPortletId(appPortlet.getApplicationPortletID());
                     prefsManager.setPreferencesDesc(appPortlet.getPreferencesDescriptor());
                     request.setAttribute(SportletProperties.PORTLET_PREFERENCES_MANAGER, prefsManager);
                 }
                 RenderRequest renderRequest = new RenderRequestImpl(request, portalContext, portletContext, supports);
                 RenderResponse renderResponse = new RenderResponseImpl(request, response, portalContext);
 
                 renderRequest.setAttribute(SportletProperties.RENDER_REQUEST, renderRequest);
                 renderRequest.setAttribute(SportletProperties.RENDER_RESPONSE, renderResponse);
 
                 log.debug("in PortletServlet: rendering  portlet " + pid);
                 if (renderRequest.getAttribute(SportletProperties.RESPONSE_COMMITTED) == null) {
                     try {
                         portlet.render(renderRequest, renderResponse);
                     } catch (UnavailableException e) {
                         log.error("in PortletServlet(): doRender() caught unavailable exception: ");
                         try {
                             portlet.destroy();
                         } catch (Exception d) {
                             log.error("in PortletServlet(): destroy caught unavailable exception: ", d);
                         }
                     } catch (Exception e) {
                         if (request.getAttribute(SportletProperties.PORTLETERROR + pid) == null) {
                             request.setAttribute(SportletProperties.PORTLETERROR + pid, e);
                         }
                         throw new ServletException(e);
                     }
                 }
             }
             request.removeAttribute(SportletProperties.PORTLET_ACTION_METHOD);
         } else {
             log.error("in PortletServlet: service(): No " + SportletProperties.PORTLET_LIFECYCLE_METHOD + " found in request!");
         }
         request.removeAttribute(SportletProperties.PORTLET_LIFECYCLE_METHOD);
     }
 
 /*
 protected void setGroupAndRole(PortletRequest request, PortletResponse response) {
 String ctxPath = this.getServletContext().getRealPath("");
 int i = ctxPath.lastIndexOf(File.separator);
 String groupName = ctxPath.substring(i+1);
 
 PortletGroup group = aclService.getGroup(groupName);
 if (group == null)
 group = PortletGroupFactory.createPortletGroup(groupName);
 
 PortletRole role = aclService.getRoleInGroup(request.getUser(), group);
 
 log.debug("Setting Group: " + group.toString() + " Role: " + role.toString());
 
 request.setAttribute(SportletProperties.PORTLET_GROUP, group);
 request.setAttribute(SportletProperties.PORTLET_ROLE, role);
 }
 */
     protected void setPortletModes(HttpServletRequest request, JSRApplicationPortletImpl appPortlet) {
         ApplicationPortletConfig appPortletConfig = appPortlet.getApplicationPortletConfig();
 
         Client client = (Client)request.getSession().getAttribute(SportletProperties.CLIENT);
         if (client == null) {
             client = new ClientImpl(request);
             request.getSession().setAttribute(SportletProperties.CLIENT, client);
         }
 
         List appModes = appPortletConfig.getSupportedModes(client.getMimeType());
         // convert modes from GridSphere type to JSR
         Iterator it = appModes.iterator();
         List myModes = new ArrayList();
         PortletMode m = PortletMode.VIEW;
         while (it.hasNext()) {
             org.gridlab.gridsphere.portlet.Portlet.Mode mode = (org.gridlab.gridsphere.portlet.Portlet.Mode)it.next();
             if (mode == org.gridlab.gridsphere.portlet.Portlet.Mode.VIEW) {
                 m = PortletMode.VIEW;
             } else if (mode == org.gridlab.gridsphere.portlet.Portlet.Mode.EDIT) {
                 m = PortletMode.EDIT;
             } else if (mode == org.gridlab.gridsphere.portlet.Portlet.Mode.HELP) {
                 m = PortletMode.HELP;
             } else if (mode == org.gridlab.gridsphere.portlet.Portlet.Mode.CONFIGURE) {
                 m = new PortletMode("config");
             } else {
                 m = new PortletMode(mode.toString());
             }
             myModes.add(m.toString());
         }
 
         request.setAttribute(SportletProperties.ALLOWED_MODES, myModes);
 
     }
 
     protected void doTitle(Portlet portlet, RenderRequest request, RenderResponse response) throws PortletException {
         try {
             Portlet por = (Portlet)portlet;
             if (por instanceof GenericPortlet) {
                 GenericPortlet genPortlet = ((GenericPortlet) portlet);
                 if (genPortlet.getPortletConfig() == null) throw new PortletException("Unable to get PortletConfig from Portlet");
                 ResourceBundle resBundle = genPortlet.getPortletConfig().getResourceBundle(request.getLocale());
                 String title = resBundle.getString("javax.portlet.title");
                 response.setContentType("text/html");
                 PrintWriter out = response.getWriter();
                 out.println(title);
             }
        } catch (Exception e) {
            throw new PortletException(e);
         }
     }
 
     protected void doGet(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         super.doGet(req, res);
     }
 
     protected void doPut(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         super.doPut(req, res);
     }
 
     protected void doPost(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         super.doPost(req, res);
     }
 
     protected void doTrace(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         super.doTrace(req, res);
     }
 
     protected void doDelete(HttpServletRequest req, HttpServletResponse res)
             throws ServletException, IOException {
         super.doDelete(req, res);
     }
 
     protected void redirect(HttpServletRequest servletRequest,
                             HttpServletResponse servletResponse,
                             ActionRequest actionRequest,
                             ActionResponse actionResponse, PortalContext portalContext)
             throws IOException {
         if (actionResponse instanceof ActionResponseImpl) {
             ActionResponseImpl aResponse = (ActionResponseImpl) actionResponse;
             String location = aResponse.getRedirectLocation();
 
             if (location != null) {
                 javax.servlet.http.HttpServletResponse redirectResponse = servletResponse;
                 while (redirectResponse instanceof javax.servlet.http.HttpServletResponseWrapper) {
                     redirectResponse = (javax.servlet.http.HttpServletResponse)
                             ((javax.servlet.http.HttpServletResponseWrapper) redirectResponse).getResponse();
                 }
 
                 log.debug("redirecting to location= " + location);
                 redirectResponse.sendRedirect(location);
             }
         }
     }
 
     /**
      * Record the fact that a servlet context attribute was added.
      *
      * @param event The session attribute event
      */
     public void attributeAdded(HttpSessionBindingEvent event) {
 
         log.debug("attributeAdded('" + event.getSession().getId() + "', '" +
                 event.getName() + "', '" + event.getValue() + "')");
 
     }
 
 
     /**
      * Record the fact that a servlet context attribute was removed.
      *
      * @param event The session attribute event
      */
     public void attributeRemoved(HttpSessionBindingEvent event) {
 
         log.debug("attributeRemoved('" + event.getSession().getId() + "', '" +
                 event.getName() + "', '" + event.getValue() + "')");
 
     }
 
 
     /**
      * Record the fact that a servlet context attribute was replaced.
      *
      * @param event The session attribute event
      */
     public void attributeReplaced(HttpSessionBindingEvent event) {
 
         log.debug("attributeReplaced('" + event.getSession().getId() + "', '" +
                 event.getName() + "', '" + event.getValue() + "')");
 
     }
 
     /**
      * Record the fact that a session has been created.
      *
      * @param event The session event
      */
     public void sessionCreated(HttpSessionEvent event) {
         log.debug("sessionCreated('" + event.getSession().getId() + "')");
         //sessionManager.sessionCreated(event);
     }
 
 
     /**
      * Record the fact that a session has been destroyed.
      *
      * @param event The session event
      */
     public void sessionDestroyed(HttpSessionEvent event) {
         //sessionManager.sessionDestroyed(event);
         //loginService.sessionDestroyed(event.getSession());
         log.debug("sessionDestroyed('" + event.getSession().getId() + "')");
 
         //HttpSession session = event.getSession();
         //User user = (User) session.getAttribute(SportletProperties.PORTLET_USER);
         //System.err.println("user : " + user.getUserID() + " expired!");
         //PortletLayoutEngine engine = PortletLayoutEngine.getDefault();
         //engine.removeUser(user);
         //engine.logoutPortlets(event);
     }
 
     /**
      * Record the fact that a session has been created.
      *
      * @param event The session event
      */
     public void sessionDidActivate(HttpSessionEvent event) {
         log.debug("sessionDidActivate('" + event.getSession().getId() + "')");
         //sessionManager.sessionCreated(event);
     }
 
 
     /**
      * Record the fact that a session has been destroyed.
      *
      * @param event The session event
      */
     public void sessionWillPassivate(HttpSessionEvent event) {
         //sessionManager.sessionDestroyed(event);
         //loginService.sessionDestroyed(event.getSession());
         log.debug("sessionWillPassivate('" + event.getSession().getId() + "')");
         //HttpSession session = event.getSession();
         //User user = (User) session.getAttribute(SportletProperties.USER);
         //System.err.println("user : " + user.getUserID() + " expired!");
         //PortletLayoutEngine engine = PortletLayoutEngine.getDefault();
         //engine.removeUser(user);
         //engine.logoutPortlets(event);
     }
 }
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
 
