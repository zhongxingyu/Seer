 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.layout.event.PortletComponentEvent;
 import org.gridlab.gridsphere.layout.event.PortletFrameEvent;
 import org.gridlab.gridsphere.layout.event.PortletFrameListener;
 import org.gridlab.gridsphere.layout.event.PortletTitleBarEvent;
 import org.gridlab.gridsphere.layout.event.impl.PortletFrameEventImpl;
 import org.gridlab.gridsphere.layout.view.FrameView;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portlet.impl.StoredPortletResponseImpl;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.portletcontainer.*;
 import org.gridlab.gridsphere.services.core.cache.CacheService;
 import org.gridlab.gridsphere.services.core.security.role.RoleManagerService;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigSettings;
 import org.gridlab.gridsphere.services.core.portal.PortalConfigService;
 import org.gridlab.gridsphere.services.core.messaging.TextMessagingService;
 import org.gridsphere.tmf.message.MailMessage;
 
 import javax.servlet.RequestDispatcher;
 import javax.portlet.RenderResponse;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.util.*;
 import java.security.Principal;
 import java.text.DateFormat;
 
 /**
  * <code>PortletFrame</code> provides the visual representation of a portlet. A portlet frame
  * contains a portlet title bar unless visible is set to false.
  */
 public class PortletFrame extends BasePortletComponent implements Serializable, Cloneable {
 
     public static final String FRAME_CLOSE_OK_ACTION = "close";
 
     public static final String FRAME_CLOSE_CANCEL_ACTION = "cancelClose";
 
     private transient CacheService cacheService = null;
 
     private transient RoleManagerService roleService = null;
 
     // renderPortlet is true in doView and false on minimized
     private boolean renderPortlet = true;
     private String portletClass = null;
 
     private PortletTitleBar titleBar = null;
     //private PortletErrorFrame errorFrame = new PortletErrorFrame();
     private boolean transparent = false;
     private String innerPadding = "";   // has to be empty and not 0!
     private String outerPadding = "";   // has to be empty and not 0!
 
     private long cacheExpiration = 0;
 
     // keep track of the original width
     private String originalWidth = "";
 
     // switch to determine if the user wishes to close this portlet
 
     private boolean isClosing = false;
 
     // render params are the persistent per portlet parameters stored as key names and string[] values
     private Map renderParams = new HashMap();
     private boolean onlyRender = true;
 
     private transient FrameView frameView = null;
 
     private String lastFrame = new String();
 
     /**
      * Constructs an instance of PortletFrame
      */
     public PortletFrame() {
     }
 
     /**
      * Sets the portlet title bar contained by this portlet frame
      *
      * @param titleBar the portlet title bar
      */
     public void setPortletTitleBar(PortletTitleBar titleBar) {
         this.titleBar = titleBar;
     }
 
     /**
      * Returns the portlet title bar contained by this portlet frame
      *
      * @return the portlet title bar
      */
     public PortletTitleBar getPortletTitleBar() {
         return titleBar;
     }
 
     /**
      * Sets the portlet class contained by this portlet frame
      *
      * @param portletClass the fully qualified portlet classname
      */
     public void setPortletClass(String portletClass) {
         this.portletClass = portletClass;
     }
 
     /**
      * Returns the portlet class contained by this portlet frame
      *
      * @return the fully qualified portlet classname
      */
     public String getPortletClass() {
         return portletClass;
     }
 
     /**
      * Sets the inner padding of the portlet frame
      *
      * @param innerPadding the inner padding
      */
     public void setInnerPadding(String innerPadding) {
         this.innerPadding = innerPadding;
     }
 
     /**
      * Returns the inner padding of the portlet frame
      *
      * @return the inner padding
      */
     public String getInnerPadding() {
         return innerPadding;
     }
 
     /**
      * Sets the outer padding of the portlet frame
      *
      * @param outerPadding the outer padding
      */
     public void setOuterPadding(String outerPadding) {
         this.outerPadding = outerPadding;
     }
 
     /**
      * Returns the outer padding of the portlet frame
      *
      * @return the outer padding
      */
     public String getOuterPadding() {
         return outerPadding;
     }
 
     public void setTheme(String theme) {
         this.theme = theme;
         if (titleBar != null) titleBar.setTheme(theme);
     }
 
     /**
      * If set to <code>true</code> the portlet is rendered transparently without a
      * defining border and title bar. This is used for example for the LogoutPortlet
      *
      * @param transparent if set to <code>true</code>, portlet frame is displayed transparently, <code>false</code> otherwise
      */
     public void setTransparent(boolean transparent) {
         this.transparent = transparent;
     }
 
     /**
      * If set to <code>true</code> the portlet is rendered transparently without a
      * defining border and title bar. This is used for example for the LogoutPortlet
      *
      * @return <code>true</code> if the portlet frame is displayed transparently, <code>false</code> otherwise
      */
     public boolean getTransparent() {
         return this.transparent;
     }
 
     /**
      * Initializes the portlet frame component. Since the components are isolated
      * after Castor unmarshalls from XML, the ordering is determined by a
      * passed in List containing the previous portlet components in the tree.
      *
      * @param list a <code>List</code> of component identifiers
      * @return a <code>List</code> of updated component identifiers
      * @see ComponentIdentifier
      */
     public List init(PortletRequest req, List list) {
         PortletServiceFactory factory = SportletServiceFactory.getInstance();
         try {
             cacheService = (CacheService) factory.createPortletService(CacheService.class, true);
             roleService = (RoleManagerService)factory.createPortletService(RoleManagerService.class, true);
         } catch (PortletServiceException e) {
             System.err.println("Unable to init Cache service! " + e.getMessage());
         }
         list = super.init(req, list);
 
         frameView = (FrameView)getRenderClass("Frame");
 
         ComponentIdentifier compId = new ComponentIdentifier();
         compId.setPortletComponent(this);
 
         compId.setPortletClass(portletClass);
 
         compId.setComponentID(list.size());
         compId.setComponentLabel(label);
         compId.setClassName(this.getClass().getName());
         list.add(compId);
         this.originalWidth = width;
 
         titleBar = new PortletTitleBar();
 
         // if title bar is not assigned a label and we have one then use it
         if ((!label.equals("")) && (titleBar.getLabel().equals(""))) titleBar.setLabel(label + "TB");
         titleBar.setPortletClass(portletClass);
 
         titleBar.setCanModify(canModify);
         titleBar.setTheme(theme);
         titleBar.setRenderKit(renderKit);
         list = titleBar.init(req, list);
         titleBar.addComponentListener(this);
         titleBar.setParentComponent(this);
         titleBar.setRoleService(roleService);
         //System.err.println("useDiv= " + useDiv);
 
         // invalidate cache
         req.setAttribute(CacheService.NO_CACHE, "true");
         doConfig();
         return list;
     }
 
     protected void doConfig() {
         PortletRegistry registryManager = PortletRegistry.getInstance();
         String appID = PortletRegistry.getApplicationPortletID(portletClass);
 
         ApplicationPortlet appPortlet = registryManager.getApplicationPortlet(appID);
         if (appPortlet != null) {
             ApplicationPortletConfig appConfig = appPortlet.getApplicationPortletConfig();
             if (appConfig != null) {
                 cacheExpiration = appConfig.getCacheExpires();
                 //System.err.println("Cache for " + portletClass + "expires: " + cacheExpiration);
             }
         }
     }
 
     public void remove(PortletComponent pc, PortletRequest req) {
         if (parent != null) parent.remove(this, req);
     }
 
     /**
      * Fires a frame event notification
      *
      * @param event a portlet frame event
      */
     protected void fireFrameEvent(PortletFrameEvent event) {
         List slisteners = Collections.synchronizedList(listeners);
         synchronized (slisteners) {
             Iterator it = slisteners.iterator();
             PortletFrameListener l;
             while (it.hasNext()) {
                 l = (PortletFrameListener) it.next();
                 l.handleFrameEvent(event);
             }
         }
     }
 
     /**
      * Performs an action on this portlet frame component
      *
      * @param event a gridsphere event
      */
     public void actionPerformed(GridSphereEvent event) {
         super.actionPerformed(event);
 
         PortletRequest request = event.getPortletRequest();
         String id = request.getPortletSession(true).getId();
 
         // remove cached output
         cacheService.removeCached(this.getComponentID() + portletClass + id);
         //frame = null;
 
         PortletComponentEvent titleBarEvent = event.getLastRenderEvent();
 
         if ((titleBarEvent != null) && (titleBarEvent instanceof PortletTitleBarEvent)) {
             PortletTitleBarEvent tbEvt = (PortletTitleBarEvent) titleBarEvent;
             if (tbEvt.hasWindowStateAction()) {
 
                 PortletWindow.State state = tbEvt.getState();
                 PortletFrameEventImpl frameEvent = null;
                 if (state == PortletWindow.State.MINIMIZED) {
                     renderPortlet = false;
                     frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_MINIMIZED, COMPONENT_ID);
                 } else if (state == PortletWindow.State.RESIZING) {
                     renderPortlet = true;
                     frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_RESTORED, COMPONENT_ID);
                     frameEvent.setOriginalWidth(originalWidth);
                 } else if (state == PortletWindow.State.MAXIMIZED) {
                     renderPortlet = true;
                     frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_MAXIMIZED, COMPONENT_ID);
                 } else if (state == PortletWindow.State.CLOSED) {
                     renderPortlet = true;
                     isClosing = true;
 
                     // check for portlet closing action
                     if (event.hasAction()) {
                         if (event.getAction().getName().equals(FRAME_CLOSE_OK_ACTION)) {
                             isClosing = false;
                             frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_CLOSED, COMPONENT_ID);
                             request.setAttribute(SportletProperties.INIT_PAGE, "true");
                         }
                         if (event.getAction().getName().equals(FRAME_CLOSE_CANCEL_ACTION)) {
                             isClosing = false;
                         }
                     }
                 }
 
                 List slisteners = Collections.synchronizedList(listeners);
                 synchronized (slisteners) {
                     Iterator it = slisteners.iterator();
                     PortletComponent comp;
                     while (it.hasNext()) {
                         comp = (PortletComponent) it.next();
                         event.addNewRenderEvent(frameEvent);
                         comp.actionPerformed(event);
                     }
                 }
 
             }
 
         } else {
             // now perform actionPerformed on Portlet if it has an action
             titleBar.actionPerformed(event);
 
             request.setAttribute(SportletProperties.COMPONENT_ID, componentIDStr);
 
             PortletResponse res = event.getPortletResponse();
 
             request.setAttribute(SportletProperties.PORTLETID, portletClass);
 
             // Override if user is a guest
             Principal principal = request.getUserPrincipal();
             if (principal == null) {
                 request.setMode(Portlet.Mode.VIEW);
             } else {
                 Portlet.Mode mode = titleBar.getPortletMode();
                 request.setMode(mode);
             }
 
             titleBar.setPortletMode(request.getMode());
 
             //System.err.println("in PortletFrame action invoked for " + portletClass);
             if (event.hasAction()
                     && (!event.getAction().getName().equals(FRAME_CLOSE_OK_ACTION))
                     && (!event.getAction().getName().equals(FRAME_CLOSE_CANCEL_ACTION))) {
                 DefaultPortletAction action = event.getAction();
 
                 renderParams.clear();
                 onlyRender = false;
                 String pid = (String)request.getAttribute(SportletProperties.PORTLETID);
                 try {
                     PortletInvoker.actionPerformed(pid, action, request, res);
                 } catch (Exception e) {
                     log.error("An error occured performing action on: " + pid, e);
                     // catch it and keep processing
                 }
 
                 // see if mode has been set
                 String mymodeStr = (String)request.getAttribute(SportletProperties.PORTLET_MODE);
                 Portlet.Mode mymode = Portlet.Mode.toMode(mymodeStr);
                 if (mymode != null) {
                     //System.err.println("setting title mode to " + mymode);
                     titleBar.setPortletMode(mymode);
                 }
 
                 // see if state has been set
                 PortletFrameEventImpl frameEvent = null;
                 PortletWindow.State mystate  = (PortletWindow.State)request.getAttribute(SportletProperties.PORTLET_WINDOW);
                 if (mystate != null) {
                     //System.err.println("setting title state to " + mystate);
                     titleBar.setWindowState(mystate);
 
                     if (mystate == PortletWindow.State.MINIMIZED) {
                         renderPortlet = false;
                     } else if ((mystate == PortletWindow.State.RESIZING) || (mystate == PortletWindow.State.NORMAL)) {
                         renderPortlet = true;
                         frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_RESTORED, COMPONENT_ID);
                         frameEvent.setOriginalWidth(originalWidth);
                     } else if (mystate == PortletWindow.State.MAXIMIZED) {
                         renderPortlet = true;
                         frameEvent = new PortletFrameEventImpl(this, request, PortletFrameEvent.FrameAction.FRAME_MAXIMIZED, COMPONENT_ID);
                     }
 
                     List slisteners = Collections.synchronizedList(listeners);
                     synchronized (slisteners) {
                         Iterator it = slisteners.iterator();
                         PortletComponent comp;
                         while (it.hasNext()) {
                             comp = (PortletComponent) it.next();
                             event.addNewRenderEvent(frameEvent);
                             comp.actionPerformed(event);
                         }
                     }
                 }
 
             }
 
             // see if render params are set from actionResponse
             Map tmpParams = (Map)request.getAttribute(SportletProperties.RENDER_PARAM_PREFIX + portletClass + "_" + componentIDStr);
             if (tmpParams != null) renderParams = tmpParams;
 
             addRenderParams(request);
 
             List slisteners = Collections.synchronizedList(listeners);
             synchronized (slisteners) {
                 Iterator it = slisteners.iterator();
                 PortletComponent comp;
                 while (it.hasNext()) {
                     comp = (PortletComponent) it.next();
                     event.addNewRenderEvent(titleBarEvent);
                     comp.actionPerformed(event);
                 }
             }
         }
 
     }
 
     private void addRenderParams(PortletRequest req) {
         // first get rid of existing render params
         Iterator it;
         if (onlyRender) {
             it = renderParams.keySet().iterator();
             while (it.hasNext()) {
                 String key = (String)it.next();
                 if (key.startsWith(SportletProperties.RENDER_PARAM_PREFIX)) {
                     if (req.getParameter(key) == null) {
                         //System.err.println("removing existing render param " + key);
                         it.remove();
                     }
                 }
             }
         }
         Map tmpParams = req.getParameterMap();
         if (tmpParams != null) {
             it = tmpParams.keySet().iterator();
             while (it.hasNext()) {
                 String key = (String)it.next();
                 ///String[] paramValues = req.getParameterValues( key );
                 if (key.startsWith(SportletProperties.RENDER_PARAM_PREFIX)) {
                     //System.err.println("replacing render param " + key);
                     renderParams.put(key, tmpParams.get(key));
                 }
             }
         }
     }
 
     /**
      * Renders the portlet frame component
      *
      * @param event a gridsphere event
      */
     public void doRender(GridSphereEvent event) {
         super.doRender(event);
 
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
 
         // check for render params
         if (onlyRender)  {
             if ((event.getPortletComponentID().equals(componentIDStr))) {
                 addRenderParams(req);
             }
         }
         onlyRender = true;
         User user = req.getUser();
         if (!roleString.equals("")) {
             Principal principal = req.getUserPrincipal();
             if (principal != null) {
                 List roles = req.getRoles();
                 if (!groupString.equals("")) {
                     List groups = req.getGroups();
                     if (groups.contains(groupString)) {
                         System.err.println("User " + user + " has no permissions to access portlet: " + portletClass + "!");
                         return;
                     }
                 }
                 if (roles.contains(roleString)) {
                     System.err.println("User " + user + " has no permissions to access portlet: " + portletClass + "!");
                     return;
                 }
             }
         }
 
         String id = event.getPortletRequest().getPortletSession(true).getId();
 
         StringBuffer frame = (StringBuffer) cacheService.getCached(this.getComponentID() + portletClass + id);
         String nocache = (String) req.getAttribute(CacheService.NO_CACHE);
         if ((frame != null) && (nocache == null)) {
             req.setAttribute(SportletProperties.RENDER_OUTPUT + componentIDStr, frame);
             return;
         }
         frame = new StringBuffer();
 
         req.setAttribute(SportletProperties.PORTLETID, portletClass);
 
 
         StringBuffer preframe = frameView.doStart(event, this);
         StringBuffer postframe = new StringBuffer();
 
         // Render title bar
         if (!transparent) {
             titleBar.doRender(event);
         } else {
             req.setMode(titleBar.getPortletMode());
             req.setAttribute(SportletProperties.PREVIOUS_MODE, titleBar.getPreviousMode());
             req.setAttribute(SportletProperties.PORTLET_WINDOW, titleBar.getWindowState());
         }
 
         if (req.getAttribute(SportletProperties.RESPONSE_COMMITTED) != null) {
             renderPortlet = false;
         }
 
 
         // TODO try to cache portlet's rendering---
         StringWriter storedWriter = new StringWriter();
         PrintWriter writer = new PrintWriter(storedWriter);
         if (renderPortlet) {
             if (!transparent) {
                 postframe.append(titleBar.getBufferedOutput(req));
             }
 
             postframe.append(frameView.doStartBorder(event, this));
 
             // TODO try to cache portlet's rendering---
             storedWriter = new StringWriter();
             writer = new PrintWriter(storedWriter);
             PortletResponse wrappedResponse = new StoredPortletResponseImpl(res, writer);
 
             if (isClosing) {
                 postframe.append(frameView.doRenderCloseFrame(event, this));
             } else {
                 //System.err.println("in portlet frame render: class= " + portletClass + " setting prev mode= " + req.getPreviousMode() + " cur mode= " + req.getMode());
                 if (hasError(req)) {
                     doRenderError(req, wrappedResponse);
                     postframe.append(storedWriter.toString());
                 } else if ((titleBar != null) && (titleBar.hasRenderError())) {
                     postframe.append(titleBar.getErrorMessage());
                 } else {
                     try {
                         if (!renderParams.isEmpty()) {
                             //System.err.println("PortletFrame: in " + portletClass + " sending render params");
                             System.err.println("in render " + portletClass + " there are render params in the frame setting in request! key= " + SportletProperties.RENDER_PARAM_PREFIX + portletClass + "_" + componentIDStr);
                             req.setAttribute(SportletProperties.RENDER_PARAM_PREFIX + portletClass + "_" + componentIDStr, renderParams);
                         }
                         PortletInvoker.service((String)req.getAttribute(SportletProperties.PORTLETID), req, wrappedResponse);
                         lastFrame = storedWriter.toString();
                         postframe.append(lastFrame);
                     } catch (Exception e) {
                         doRenderError(req, wrappedResponse);
                         postframe.append(storedWriter.toString());
                     }
                 }
             }
             postframe.append(frameView.doEndBorder(event, this));
         } else {
             postframe.append(frameView.doRenderMinimizeFrame(event, this));
         }
         postframe.append(frameView.doEnd(event, this));
 
         if (req.getAttribute(SportletProperties.RESPONSE_COMMITTED) != null) {
             renderPortlet = true;
         }
 
         // piece together portlet frame + title depending on whether title was set during doXXX method
         // or not
         frame.append(preframe);
         if (!transparent) {
             String titleStr = (String) req.getAttribute(SportletProperties.PORTLET_TITLE);
             if (titleStr == null) {
                 titleStr = titleBar.getTitle();
             }
             frame.append(titleBar.getPreBufferedTitle(req));
             frame.append(titleStr);
             frame.append(titleBar.getPostBufferedTitle(req));
         }
         req.removeAttribute(SportletProperties.PORTLET_TITLE);
 
         frame.append(postframe);
 
 
         req.setAttribute(SportletProperties.RENDER_OUTPUT + componentIDStr, frame);
 
         // check if expiration was set in render response
         Map props = (Map)req.getAttribute(SportletProperties.PORTAL_PROPERTIES);
         if (props != null) {
             List vals = (List)props.get(RenderResponse.EXPIRATION_CACHE);
             if (vals != null) {
                 String cacheExpiryStr = (String)vals.get(0);
                 if (cacheExpiryStr != null) {
                     try {
                         cacheExpiration = Integer.valueOf(cacheExpiryStr).intValue();
                     } catch (IllegalArgumentException e) {
                         // do nothing
                     }
                 }
             }
         }
 
         if (nocache == null) {
             if ((cacheExpiration > 0) || (cacheExpiration == -1)) {
                 cacheService.cache(this.getComponentID() + portletClass + id, frame, cacheExpiration);
             }
         }
     }
 
     public boolean isTargetedPortlet(PortletRequest req) {
         return (req.getParameter(SportletProperties.COMPONENT_ID).equals(req.getAttribute(SportletProperties.COMPONENT_ID)));
     }
 
     public boolean hasError(PortletRequest req) {
         return (req.getAttribute(SportletProperties.PORTLETERROR + portletClass) != null);
     }
 
     protected String getLocalizedText(PortletRequest req, String key) {
         Locale locale = req.getLocale();
         ResourceBundle bundle = ResourceBundle.getBundle("gridsphere.resources.Portlet", locale);
         return bundle.getString(key);
     }
 
     public void doRenderError(PortletRequest req, PortletResponse res) {
         PortletServiceFactory factory = SportletServiceFactory.getInstance();
         Throwable ex = (Throwable)req.getAttribute(SportletProperties.PORTLETERROR + portletClass);
         if (ex == null) return;
         try {
             PortalConfigService portalConfigService = (PortalConfigService)factory.createPortletService(PortalConfigService.class, true);
             TextMessagingService tms = (TextMessagingService)factory.createPortletService(TextMessagingService.class, true);
             PortalConfigSettings settings = portalConfigService.getPortalConfigSettings();
             RoleManagerService roleManagerService = (RoleManagerService)factory.createPortletService(RoleManagerService.class, true);
             Boolean sendMail = Boolean.valueOf(settings.getAttribute("LOGIN_ERROR_HANDLING"));
             if (sendMail.booleanValue()) {
                 MailMessage mailToUser = tms.getMailMessage();
                 List superUsers = roleManagerService.getUsersInRole(PortletRole.SUPER);
                 User superUser = (User)superUsers.get(0);
                 mailToUser.setTo(superUser.getEmailAddress());
                 mailToUser.setSubject(getLocalizedText(req, "PORTAL_ERROR_SUBJECT"));
                 StringBuffer body = new StringBuffer();
                 body.append(getLocalizedText(req, "PORTAL_ERROR_BODY") + "\n\n");
                 body.append("portlet title: " + titleBar.getTitle() + "\n\n");
                 User user = req.getUser();
                 body.append(DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()) + "\n\n");
                 if (user != null) body.append(user + "\n\n");
                 StringWriter sw = new StringWriter();
                 PrintWriter pout = new PrintWriter(sw);
                ex.printStackTrace(pout);
                 body.append(sw.getBuffer());

                 mailToUser.setBody(body.toString());
                 mailToUser.setServiceid("mail");
                 try {
                     tms.send(mailToUser);
                     req.setAttribute("lastFrame", lastFrame);
                     RequestDispatcher dispatcher = GridSphereConfig.getServletContext().getRequestDispatcher("/jsp/errors/custom_error.jsp");
                     dispatcher.include(req, res);
                     return;
                 } catch (Exception e) {
                     log.error("Unable to send mail message!", e);
                 }
             }
         } catch (PortletServiceException e) {
             log.error("Unable to get instance of needed portlet services", e);
         }
         try {
            req.setAttribute("error", ex);
             RequestDispatcher dispatcher = GridSphereConfig.getServletContext().getRequestDispatcher("/jsp/errors/custom_error.jsp");
             dispatcher.include(req, res);
         } catch (Exception e) {
             System.err.println("Unable to include custom error page!!");
            ex.printStackTrace();
         }
     }
 
     public Object clone() throws CloneNotSupportedException {
         PortletFrame f = (PortletFrame) super.clone();
         f.titleBar = (this.titleBar == null) ? null : (PortletTitleBar) this.titleBar.clone();
         f.outerPadding = this.outerPadding;
         f.transparent = this.transparent;
         f.innerPadding = this.innerPadding;
         f.portletClass = this.portletClass;
         f.renderPortlet = this.renderPortlet;
         return f;
     }
 
 
     /* (non-Javadoc)
     * @see org.gridlab.gridsphere.layout.PortletComponent#messageEvent(java.lang.String, org.gridlab.gridsphere.portlet.PortletMessage, org.gridlab.gridsphere.portletcontainer.GridSphereEvent)
     */
     public void messageEvent(String concPortletID, PortletMessage msg, GridSphereEvent event) {
 
         if (portletClass.equals(concPortletID)) {
             PortletRequest req = event.getPortletRequest();
 
             req.setAttribute(SportletProperties.COMPONENT_ID, componentIDStr);
 
             List roles = req.getRoles();
             if (!roleString.equals("") && (!roles.contains(roleString))) return;
 
             PortletResponse res = event.getPortletResponse();
 
 
             req.setAttribute(SportletProperties.PORTLETID, portletClass);
 
 
             // Override if user is a guest
             Principal principal = req.getUserPrincipal();
             if (principal == null) {
             //User user = req.getUser();
             //if (user instanceof GuestUser) {
                 req.setMode(Portlet.Mode.VIEW);
             } else {
                 if (titleBar != null) {
                     Portlet.Mode mode = titleBar.getPortletMode();
                     //System.err.println("setting mode in " + portletClass + " to " + mode.toString());
                     req.setMode(mode);
                 } else {
                     req.setMode(Portlet.Mode.VIEW);
                 }
             }
 
             try {
                 PortletInvoker.messageEvent(portletClass, msg, req, res);
             } catch (Exception ioex) {
                 // do nothing the render will take care of displaying the error    
             }
 
         } else {
             super.messageEvent(concPortletID, msg, event);
         }
     }
 
 }
