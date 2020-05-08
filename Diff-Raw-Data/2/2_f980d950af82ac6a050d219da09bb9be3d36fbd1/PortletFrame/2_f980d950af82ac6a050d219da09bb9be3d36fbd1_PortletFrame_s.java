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
 import org.gridlab.gridsphere.layout.event.impl.PortletTitleBarEventImpl;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.jsrimpl.RenderResponseImpl;
 import org.gridlab.gridsphere.portlet.impl.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridlab.gridsphere.portlet.service.spi.impl.SportletServiceFactory;
 import org.gridlab.gridsphere.portletcontainer.*;
 import org.gridlab.gridsphere.services.core.cache.CacheService;
 import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
 
 import javax.portlet.PortletMode;
 import javax.servlet.RequestDispatcher;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.util.*;
 
 /**
  * <code>PortletFrame</code> provides the visual representation of a portlet. A portlet frame
  * contains a portlet title bar unless visible is set to false.
  */
 public class PortletFrame extends BasePortletComponent implements Serializable, Cloneable {
 
     public static final String FRAME_CLOSE_OK_ACTION = "close";
 
     public static final String FRAME_CLOSE_CANCEL_ACTION = "cancelClose";
 
     private transient CacheService cacheService = null;
 
     private transient AccessControlManagerService aclService = null;
 
     // renderPortlet is true in doView and false on minimized
     private boolean renderPortlet = true;
     private String portletClass = null;
 
     private PortletTitleBar titleBar = null;
     //private PortletErrorFrame errorFrame = new PortletErrorFrame();
     private boolean transparent = false;
     private String innerPadding = "";   // has to be empty and not 0!
     private String outerPadding = "";   // has to be empty and not 0!
 
     private long cacheExpiration = 0;
 
     //private PortletRole requiredRole = PortletRole.GUEST;
 
     // keep track of the original width
     private String originalWidth = "";
 
     //private transient PortletDataManager dataManager = null;
 
     private boolean hasTitleBarEvent = false;
 
     // switch to determine if the user wishes to close this portlet
 
     private boolean isClosing = false;
 
     // render params are the persistent per portlet parameters stored as key names and string[] values
     private Map renderParams = new HashMap();
     private boolean onlyRender = true;
 
     //private StringBuffer frame = new StringBuffer();
 
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
             cacheService = (CacheService) factory.createPortletService(CacheService.class, null, true);
             aclService = (AccessControlManagerService)factory.createPortletService(AccessControlManagerService.class, null, true);
         } catch (PortletServiceException e) {
             System.err.println("Unable to init Cache service! " + e.getMessage());
         }
         list = super.init(req, list);
         //dataManager = SportletDataManager.getInstance();
         ComponentIdentifier compId = new ComponentIdentifier();
         compId.setPortletComponent(this);
 
         compId.setPortletClass(portletClass);
 
         compId.setComponentID(list.size());
         compId.setComponentLabel(label);
         compId.setClassName(this.getClass().getName());
         list.add(compId);
         hasTitleBarEvent = false;
         this.originalWidth = width;
 
         titleBar = new PortletTitleBar();
 
         // if title bar is not assigned a label and we have one then use it
         if ((!label.equals("")) && (titleBar.getLabel().equals(""))) titleBar.setLabel(label + "TB");
         titleBar.setPortletClass(portletClass);
 
         titleBar.setCanModify(canModify);
         titleBar.setTheme(theme);
         list = titleBar.init(req, list);
         titleBar.addComponentListener(this);
         titleBar.setParentComponent(this);
         titleBar.setAccessControlService(aclService);
 
         // invalidate cache
         req.setAttribute(CacheService.NO_CACHE, "true");
         doConfig();
         return list;
     }
 
     protected void doConfig() {
         PortletRegistry registryManager = PortletRegistry.getInstance();
         String appID = "";
 
         appID = PortletRegistry.getApplicationPortletID(portletClass);
 
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
      * @throws PortletLayoutException if a layout error occurs
      */
     protected void fireFrameEvent(PortletFrameEvent event) throws PortletLayoutException {
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
      * @throws PortletLayoutException if a layout error occurs during rendering
      * @throws IOException            if an I/O error occurs during rendering
      */
     public void actionPerformed(GridSphereEvent event) throws PortletLayoutException, IOException {
         //System.err.println("in action performed portlet frame: " + portletClass);
 
         super.actionPerformed(event);
 
         User user = event.getPortletRequest().getUser();
         PortletRequest request = event.getPortletRequest();
         String id = request.getPortletSession(true).getId();
 
         // remove cached output
         cacheService.removeCached(this.getComponentID() + portletClass + id);
         //frame = null;
 
         hasTitleBarEvent = false;
 
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
 
             //PortletRole role = req.getRole();
             //if (role.compare(role, requiredRole) < 0) return;
 
             PortletResponse res = event.getPortletResponse();
 
             request.setAttribute(SportletProperties.PORTLETID, portletClass);
 
             // Override if user is a guest
             //Uuser = req.getUser();
             if (user instanceof GuestUser) {
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
 
                 System.err.println("clearing render params");
 
                 try {
                     PortletInvoker.actionPerformed((String)request.getAttribute(SportletProperties.PORTLETID), action, request, res);
                 } catch (PortletException e) {
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
                        renderParams.remove(key);
                     }
                 }
             }
         }
         Map tmpParams = (Map)req.getParameterMap();
             if (tmpParams != null) {
                 it = tmpParams.keySet().iterator();
                 while (it.hasNext()) {
                     String key = (String)it.next();
                     String[] paramValues = req.getParameterValues( key );
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
      * @throws PortletLayoutException if a layout error occurs during rendering
      * @throws IOException            if an I/O error occurs during rendering
      */
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
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
         if (!(user instanceof GuestUser)) {
             boolean hasrole = false;
             hasrole = aclService.hasRequiredRole(req, portletClass, false);
             //boolean hasrole = aclService.hasRequiredRole(user, portletClass, false);
             //System.err.println("hasRole = " + hasrole + " portletclass= " + portletClass);
             if (!hasrole) {
                 System.err.println("User " + user + " has no permissions to access portlet: " + portletClass + "!");
                 return;
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
 
         // TODO try to cache portlet's rendering---
         StringWriter storedWriter = new StringWriter();
         PrintWriter writer = new PrintWriter(storedWriter);
 
         ///// begin portlet frame
         writer.println("<!-- PORTLET STARTS HERE -->");
         //out.println("<div class=\"window-main\">");
         writer.print("<table  ");
 
         if (getOuterPadding().equals("")) {
             writer.print(" cellspacing=\"0\" class=\"window-main\"");
         } else {
             //out.print("border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"");        // this is the main table around one portlet
             //out.print(" cellspacing=\""+getOuterPadding()+"\" style=\"padding:"+getOuterPadding()+"px\"  class=\"window-main\" ");        // this is the main table around one portlet
             writer.print(" cellspacing=\"0\" style=\"margin:" + getOuterPadding() + "px\" class=\"window-main\" ");        // this is the main table around one portlet
             //out.print("cellpadding=\""+getOuterPadding()+"\" class=\"window-main\" ");        // this is the main table around one portlet
         }
 
         writer.println(">");
 
         String preframe = storedWriter.toString();
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
 
 
         if (renderPortlet) {
             if (!transparent) {
                 postframe.append(titleBar.getBufferedOutput(req));
                 postframe.append("<tr><td  ");      // now the portlet content begins
                 if (!getInnerPadding().equals("")) {
                     writer.print("style=\"padding:" + getInnerPadding() + "px\"");
                 }
                 postframe.append(" class=\"window-content\"> ");
             } else {
                 postframe.append("<tr><td >");
             }
 
             // TODO try to cache portlet's rendering---
             storedWriter = new StringWriter();
             writer = new PrintWriter(storedWriter);
             PortletResponse wrappedResponse = new StoredPortletResponseImpl(res, writer);
 
             if (isClosing) {
 
                 PortletURI portletURI = res.createURI();
                 portletURI.addParameter(SportletProperties.COMPONENT_ID, String.valueOf(titleBar.getComponentID()));
                 portletURI.addParameter(SportletProperties.PORTLET_WINDOW, PortletWindow.State.CLOSED.toString());
                 postframe.append("<form action=\"" + portletURI.toString() + "\" method=\"POST\"");
                 Locale locale = req.getLocale();
                 ResourceBundle bundle = ResourceBundle.getBundle("gridsphere.resources.Portlet", locale);
                 String value = bundle.getString("UNSUBSCRIBE_MESSAGE");
                 String ok = bundle.getString("OK");
                 String cancel = bundle.getString("CANCEL");
                 postframe.append("<p><b>" + value + "</b></p>");
 
                 portletURI = res.createURI();
 
                 portletURI.addParameter(PortletWindow.State.CLOSED.toString(), Boolean.TRUE.toString());
 
                 postframe.append("<p><input class=\"portlet-form-button\" type=\"submit\" name=\"" + SportletProperties.DEFAULT_PORTLET_ACTION + "=" + FRAME_CLOSE_OK_ACTION + "\" value=\"" + ok + "\"");
                 portletURI = res.createURI();
 
                 portletURI.addParameter(PortletWindow.State.CLOSED.toString(), Boolean.FALSE.toString());
                 postframe.append("<input class=\"portlet-form-button\" type=\"submit\" name=\"" + SportletProperties.DEFAULT_PORTLET_ACTION + "=" + FRAME_CLOSE_CANCEL_ACTION + "\" value=\"" + cancel + "\"");
                 postframe.append("</p></form>");
             } else {
 
                 //System.err.println("in portlet frame render: class= " + portletClass + " setting prev mode= " + req.getPreviousMode() + " cur mode= " + req.getMode());
                 if (hasError(req)) {
                     doRenderCustomError(postframe, req, wrappedResponse);
                     if (storedWriter.toString().equals("")) {
                         doRenderError(postframe, req, wrappedResponse);
                     }
                     postframe.append(storedWriter.toString());
                 } else {
                     try {
 
                         if (!renderParams.isEmpty()) {
                             //System.err.println("PortletFrame: in " + portletClass + " sending render params");
                             System.err.println("in render " + portletClass + " there are render params in the frame setting in request! key= " + SportletProperties.RENDER_PARAM_PREFIX + portletClass + "_" + componentIDStr);
                             req.setAttribute(SportletProperties.RENDER_PARAM_PREFIX + portletClass + "_" + componentIDStr, renderParams);
                         }
 
                         PortletInvoker.service((String)req.getAttribute(SportletProperties.PORTLETID), req, wrappedResponse);
                         postframe.append(storedWriter.toString());
                     } catch (PortletException e) {
                         doRenderCustomError(postframe, req, wrappedResponse);
                         if (storedWriter.toString().equals("")) {
                             doRenderError(postframe, req, wrappedResponse);
                         }
                         postframe.append(storedWriter.toString());
                     }
                 }
             }
 
             postframe.append("</td></tr>");
         } else {
             postframe.append("<tr><td class=\"window-content-minimize\">");      // now the portlet content begins
             postframe.append("</td></tr>");
         }
         postframe.append("</table>");
         postframe.append("<!--- PORTLET ENDS HERE -->");
 
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
             String cacheExpiryStr = (String)props.get(RenderResponseImpl.EXPIRATION_CACHE);
             if (cacheExpiryStr != null) {
                 try {
                     cacheExpiration = Integer.valueOf(cacheExpiryStr).intValue();
                 } catch (IllegalArgumentException e) {
                     // do nothing
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
         return (((Exception)req.getAttribute(SportletProperties.PORTLETERROR + portletClass) != null) ? true : false);
     }
 
     public void doRenderError(StringBuffer postframe, PortletRequest req, PortletResponse res) {
         Throwable ex = (Throwable)req.getAttribute(SportletProperties.PORTLETERROR + portletClass);
         if (ex != null) {
             try {
                 req.setAttribute("error", ex);
                 RequestDispatcher dispatcher = GridSphereConfig.getServletContext().getRequestDispatcher("/jsp/errors/custom_error.jsp");
                 dispatcher.include(req, res);
             } catch (Exception e) {
                 System.err.println("Unable to include custom error page!!");
                 ex.printStackTrace();
             }
         }
     }
 
     public void doRenderCustomError(StringBuffer postframe, PortletRequest req, PortletResponse res) {
         Throwable ex = (Throwable)req.getAttribute(SportletProperties.PORTLETERROR + portletClass);
         if (ex != null) {
             try {
                 req.setAttribute("error", ex);
                 RequestDispatcher dispatcher = req.getRequestDispatcher("/jsp/gs_error.jsp");
                 dispatcher.include(req, res);
             } catch (Exception e) {
                 System.err.println("Unable to include custom error page!!");
                 ex.printStackTrace();
             }
         }
     }
     public Object clone() throws CloneNotSupportedException {
         PortletFrame f = (PortletFrame) super.clone();
         f.titleBar = (this.titleBar == null) ? null : (PortletTitleBar) this.titleBar.clone();
         f.outerPadding = this.outerPadding;
         f.transparent = this.transparent;
         f.innerPadding = this.innerPadding;
         f.portletClass = this.portletClass;
         f.roleString = this.roleString;
         f.requiredRole = (PortletRole) this.requiredRole.clone();
         f.renderPortlet = this.renderPortlet;
         f.hasTitleBarEvent = false;
         return f;
     }
 
 
     /* (non-Javadoc)
     * @see org.gridlab.gridsphere.layout.PortletComponent#messageEvent(java.lang.String, org.gridlab.gridsphere.portlet.PortletMessage, org.gridlab.gridsphere.portletcontainer.GridSphereEvent)
     */
     public void messageEvent(String concPortletID, PortletMessage msg, GridSphereEvent event) {
 
         if (portletClass.equals(concPortletID)) {
             PortletRequest req = event.getPortletRequest();
 
             req.setAttribute(SportletProperties.COMPONENT_ID, componentIDStr);
 
             PortletRole role = req.getRole();
             if (role.compare(role, requiredRole) < 0) return;
 
             PortletResponse res = event.getPortletResponse();
 
 
                 req.setAttribute(SportletProperties.PORTLETID, portletClass);
 
 
             // Override if user is a guest
             User user = req.getUser();
             if (user instanceof GuestUser) {
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
 
 
             // Set the portlet data
             /*
             PortletData data = null;
             if (!(user instanceof GuestUser)) {
                 try {
                     data = dataManager.getPortletData(req.getUser(), portletClass);
                     req.setAttribute(SportletProperties.PORTLET_DATA, data);
                 } catch (PersistenceManagerException e) {
                     errorFrame.setError("Unable to retrieve user's portlet data!", e);
                 }
             }
             */
 
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
