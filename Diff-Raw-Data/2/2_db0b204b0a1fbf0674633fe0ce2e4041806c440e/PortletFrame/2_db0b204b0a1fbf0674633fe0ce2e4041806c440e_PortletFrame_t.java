 /*
  * @author <a href="mailto:novotny@gridsphere.org">Jason Novotny</a>
  * @version $Id: PortletFrame.java 5032 2006-08-17 18:15:06Z novotny $
  */
 package org.gridsphere.layout;
 
 import org.gridsphere.layout.event.PortletComponentEvent;
 import org.gridsphere.layout.event.PortletFrameEvent;
 import org.gridsphere.layout.event.PortletFrameListener;
 import org.gridsphere.layout.event.PortletTitleBarEvent;
 import org.gridsphere.layout.event.impl.PortletFrameEventImpl;
 import org.gridsphere.layout.view.FrameView;
 import org.gridsphere.portlet.impl.PortletURLImpl;
 import org.gridsphere.portlet.impl.SportletProperties;
 import org.gridsphere.portlet.impl.StoredPortletResponseImpl;
 import org.gridsphere.portlet.service.PortletServiceException;
 import org.gridsphere.portlet.service.spi.PortletServiceFactory;
 import org.gridsphere.portletcontainer.ApplicationPortlet;
 import org.gridsphere.portletcontainer.DefaultPortletAction;
 import org.gridsphere.portletcontainer.DefaultPortletRender;
 import org.gridsphere.portletcontainer.GridSphereEvent;
 import org.gridsphere.portletcontainer.impl.PortletInvoker;
 import org.gridsphere.services.core.cache.CacheService;
 import org.gridsphere.services.core.mail.MailMessage;
 import org.gridsphere.services.core.mail.MailService;
 import org.gridsphere.services.core.portal.PortalConfigService;
 import org.gridsphere.services.core.registry.PortletRegistryService;
 import org.gridsphere.services.core.user.User;
 
 import javax.portlet.*;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import java.io.PrintWriter;
 import java.io.Serializable;
 import java.io.StringWriter;
 import java.security.Principal;
 import java.text.DateFormat;
 import java.util.*;
 
 /**
  * <code>PortletFrame</code> provides the visual representation of a portlet. A portlet frame
  * contains a portlet title bar unless visible is set to false.
  */
 public class PortletFrame extends BasePortletComponent implements Serializable, Cloneable {
 
     public static final String FRAME_CLOSE_OK_ACTION = "close";
 
     public static final String FRAME_CLOSE_CANCEL_ACTION = "cancelClose";
 
     public static final String DELETE_PORTLET = "deletePortlet";
 
     private transient CacheService cacheService = null;
 
     private transient PortalConfigService portalConfigService = null;
     private transient PortletRegistryService portletRegistryService = null;
 
     private transient PortletInvoker portletInvoker = null;
 
     // renderPortlet is true in doView and false on minimized
     private boolean renderPortlet = true;
     private String portletClass = null;
 
     private PortletTitleBar titleBar = null;
 
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
 
     private String lastFrame = "";
 
     private String portletName = "Untitled";
 
     private String windowId = "unknown";
 
     //private Supports[] supports = null;
 
     /**
      * Constructs an instance of PortletFrame
      */
     public PortletFrame() {
     }
 
     public String getPortletName() {
         return portletName;
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
     public List<ComponentIdentifier> init(PortletRequest req, List<ComponentIdentifier> list) {
         try {
             cacheService = (CacheService) PortletServiceFactory.createPortletService(CacheService.class, true);
             portalConfigService = (PortalConfigService) PortletServiceFactory.createPortletService(PortalConfigService.class, true);
             portletRegistryService = (PortletRegistryService) PortletServiceFactory.createPortletService(PortletRegistryService.class, true);
         } catch (PortletServiceException e) {
             log.error("Unable to init services! ", e);
         }
         list = super.init(req, list);
 
         portletInvoker = new PortletInvoker();
         frameView = (FrameView) getRenderClass(req, "Frame");
 
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
 
         list = titleBar.init(req, list);
         titleBar.addComponentListener(this);
         titleBar.setParentComponent(this);
 
         // invalidate cache
         req.setAttribute(CacheService.NO_CACHE, "true");
 
         if (windowId == null) windowId = componentIDStr;
         String appID = portletRegistryService.getApplicationPortletID(portletClass);
         ApplicationPortlet appPortlet = portletRegistryService.getApplicationPortlet(appID);
         if (appPortlet != null) {
             //supports = appPortlet.getSupports();
             portletName = appPortlet.getPortletName();
             cacheExpiration = appPortlet.getCacheExpires();
         }
         return list;
     }
 
     /**
      * Fires a frame event notification
      *
      * @param event a portlet frame event
      */
     protected void fireFrameEvent(PortletFrameEvent event) {
         Iterator it = listeners.iterator();
         PortletFrameListener l;
         while (it.hasNext()) {
             l = (PortletFrameListener) it.next();
             l.handleFrameEvent(event);
         }
     }
 
     /**
      * Performs an action on this portlet frame component
      *
      * @param event a gridsphere event
      */
     public void actionPerformed(GridSphereEvent event) {
         super.actionPerformed(event);
 
         HttpServletRequest request = event.getHttpServletRequest();
         String id = request.getSession(true).getId();
 
         // remove cached output
         cacheService.removeCached(this.getComponentID() + portletClass + id);
 
         PortletComponentEvent titleBarEvent = event.getLastRenderEvent();
 
         if ((titleBarEvent != null) && (titleBarEvent instanceof PortletTitleBarEvent)) {
 
             PortletTitleBarEvent tbEvt = (PortletTitleBarEvent) titleBarEvent;
             if (tbEvt.hasWindowStateAction()) {
                 WindowState state = tbEvt.getState();
 
                 PortletFrameEventImpl frameEvent = null;
                 if (state.equals(WindowState.MINIMIZED)) {
                     renderPortlet = false;
                     frameEvent = new PortletFrameEventImpl(this, PortletFrameEvent.FrameAction.FRAME_MINIMIZED, COMPONENT_ID);
                 } else if (state.equals(WindowState.NORMAL)) {
                     renderPortlet = true;
                     frameEvent = new PortletFrameEventImpl(this, PortletFrameEvent.FrameAction.FRAME_RESTORED, COMPONENT_ID);
                     frameEvent.setOriginalWidth(originalWidth);
                 } else if (state.equals(WindowState.MAXIMIZED)) {
                     renderPortlet = true;
                     frameEvent = new PortletFrameEventImpl(this, PortletFrameEvent.FrameAction.FRAME_MAXIMIZED, COMPONENT_ID);
                 } else if (state.equals(new WindowState("CLOSE"))) {
                     renderPortlet = true;
                     isClosing = true;
 
                     // check for portlet closing action
                     if (event.hasAction()) {
                         if (event.getAction().getName().equals(FRAME_CLOSE_OK_ACTION)) {
                             isClosing = false;
                             frameEvent = new PortletFrameEventImpl(this, PortletFrameEvent.FrameAction.FRAME_CLOSED, COMPONENT_ID);
                             request.setAttribute(SportletProperties.INIT_PAGE, "true");
                         }
                         if (event.getAction().getName().equals(FRAME_CLOSE_CANCEL_ACTION)) {
                             isClosing = false;
                         }
                     }
                 }
 
 
                 Iterator it = listeners.iterator();
                 PortletComponent comp;
                 while (it.hasNext()) {
                     comp = (PortletComponent) it.next();
                     event.addNewRenderEvent(frameEvent);
                     comp.actionPerformed(event);
                 }
 
 
             }
 
         } else {
             // now perform actionPerformed on Portlet if it has an action
             titleBar.actionPerformed(event);
 
             request.setAttribute(SportletProperties.COMPONENT_ID, componentIDStr);
 
             request.setAttribute(SportletProperties.PORTLET_WINDOW_ID, windowId);
 
             ActionResponse res = event.getActionResponse();
 
             request.setAttribute(SportletProperties.PORTLETID, portletClass);
 
             // Override if user is a guest
             Principal principal = event.getActionRequest().getUserPrincipal();
             // String userName = "";
             try {
                 if (principal == null) {
                     res.setPortletMode(PortletMode.VIEW);
                     //userName = "guest";
                 } else {
                     PortletMode mode = titleBar.getPortletMode();
                     res.setPortletMode(mode);
                     // userName = principal.getName();
                 }
             } catch (PortletModeException e) {
                 System.err.println("unsupported mode ");
             }
             titleBar.setPortletMode(event.getActionRequest().getPortletMode());
 
             //System.err.println("in PortletFrame action invoked for " + portletClass);
             if (event.hasAction()
                     && (!event.getAction().getName().equals(FRAME_CLOSE_OK_ACTION))
                     && (!event.getAction().getName().equals(FRAME_CLOSE_CANCEL_ACTION))) {
                 DefaultPortletAction action = event.getAction();
 
                 renderParams.clear();
                 onlyRender = false;
                 String pid = (String) request.getAttribute(SportletProperties.PORTLETID);
 
                 try {
                     portletInvoker.actionPerformed(pid, action, event.getHttpServletRequest(), event.getHttpServletResponse());
                     Throwable e = (Throwable) request.getAttribute(SportletProperties.PORTLETERROR + pid);
                     if (e != null) {
                         setError(event.getActionRequest(), e);
                     }
                 } catch (Exception e) {
                     log.error("An error occured performing action on: " + pid, e.getCause());
                     this.setError(event.getActionRequest(), e.getCause());
                     // catch it and keep processing
                 }
 
                 // see if mode has been set
                 PortletMode mymode = (PortletMode) request.getAttribute(SportletProperties.PORTLET_MODE);
                 //PortletMode mymode = new PortletMode(mymodeStr);
                 if (mymode != null) {
                     //System.err.println("setting title mode to " + mymode);
                     titleBar.setPortletMode(mymode);
                 }
 
                 // see if state has been set
                 PortletFrameEventImpl frameEvent = null;
                 WindowState mystate = (WindowState) request.getAttribute(SportletProperties.PORTLET_WINDOW);
                 if (mystate != null) {
                     //System.err.println("setting title state to " + mystate);
                     titleBar.setWindowState(mystate);
 
                     if (mystate.equals(WindowState.MINIMIZED)) {
                         renderPortlet = false;
                     } else if (mystate.equals(WindowState.NORMAL)) {
                         renderPortlet = true;
                         frameEvent = new PortletFrameEventImpl(this, PortletFrameEvent.FrameAction.FRAME_RESTORED, COMPONENT_ID);
                         frameEvent.setOriginalWidth(originalWidth);
                     } else if (mystate.equals(WindowState.MAXIMIZED)) {
                         renderPortlet = true;
                         frameEvent = new PortletFrameEventImpl(this, PortletFrameEvent.FrameAction.FRAME_MAXIMIZED, COMPONENT_ID);
                     }
 
                     for (PortletComponent comp : listeners) {
                         event.addNewRenderEvent(frameEvent);
                         comp.actionPerformed(event);
                     }
 
                 }
             }
 
             // see if render params are set from actionResponse
             Map tmpParams = (Map) request.getAttribute(SportletProperties.RENDER_PARAM_PREFIX + portletClass + "_" + componentIDStr);
             if (tmpParams != null) renderParams = tmpParams;
 
             addRenderParams(event.getHttpServletRequest());
 
             for (PortletComponent comp : listeners) {
                 event.addNewRenderEvent(titleBarEvent);
                 comp.actionPerformed(event);
             }
         }
     }
 
     private void addRenderParams(HttpServletRequest req) {
         // first get rid of existing render params
         Iterator it;
         if (onlyRender) {
             it = renderParams.keySet().iterator();
             while (it.hasNext()) {
                 String key = (String) it.next();
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
                 String key = (String) it.next();
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
 
         RenderRequest req = event.getRenderRequest();
         RenderResponse res = event.getRenderResponse();
 
         req.setAttribute(SportletProperties.PORTLET_WINDOW_ID, windowId);
         if (req.getAttribute(SportletProperties.LAYOUT_EDIT_MODE) != null) {
             StringBuffer content = new StringBuffer();
             PortletURLImpl portletURI = (PortletURLImpl) res.createActionURL();
             String editLink = portletURI.toString();
             portletURI.setAction(DELETE_PORTLET);                                                                                                                                                                                     /*getLocalizedText(req, "DELETE")*/
             String deleteLink = portletURI.toString();                                                                                               /*getLocalizedText(req, "EDIT")*/
             content.append("<br/><fieldset>");
             content.append(portletName);
             content.append("&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a href=\"");
             content.append(editLink);
             content.append("\">");
             content.append("<img src=\"");
             content.append(req.getContextPath());
             content.append("/images/edit.gif\" alt=\"").append(getLocalizedText(req, "EDIT")).append("\"/>");
             content.append("</a>&nbsp;&nbsp;&nbsp;<a href=\"");
             content.append(deleteLink);
             content.append("\">");
             content.append("<img src=\"");
             content.append(req.getContextPath());
             content.append("/images/delete.gif\" alt=\"").append(getLocalizedText(req, "DELETE")).append("\"/>");
             content.append("</a></fieldset>");
             setBufferedOutput(req, content);
             return;
         }
 
         // check permissions
         if (!requiredRoleName.equals("") && (!req.isUserInRole(requiredRoleName))) return;
 
         // check for render params
         if (onlyRender) {
             if ((event.getComponentID().equals(componentIDStr))) {
                 addRenderParams(event.getHttpServletRequest());
             }
         }
         onlyRender = true;
 
         String id = req.getPortletSession(true).getId();
 
         StringBuffer frame = (StringBuffer) cacheService.getCached(this.getComponentID() + portletClass + id);
         String nocache = (String) req.getAttribute(CacheService.NO_CACHE);
         if ((frame != null) && (nocache == null)) {
             setBufferedOutput(req, frame);
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
             req.setAttribute(SportletProperties.PORTLET_MODE, titleBar.getPortletMode());
             req.setAttribute(SportletProperties.PREVIOUS_MODE, titleBar.getPreviousMode());
             req.setAttribute(SportletProperties.PORTLET_WINDOW, titleBar.getWindowState());
         }
         super.doRender(event);
         if (req.getAttribute(SportletProperties.RESPONSE_COMMITTED) != null) {
             renderPortlet = false;
         }
 
         String appID = portletRegistryService.getApplicationPortletID(portletClass);
         ApplicationPortlet appPortlet = portletRegistryService.getApplicationPortlet(appID);
         if (appPortlet != null) {
             Set<String> supportedModes = appPortlet.getSupportedModes(event.getClient().getMimeType());
             SortedSet<String> mimeTypes = appPortlet.getSupportedMimeTypes(req.getPortletMode());
             req.setAttribute(SportletProperties.ALLOWED_MODES, supportedModes);
             req.setAttribute(SportletProperties.MIME_TYPES, mimeTypes);
         }
 
         req.setAttribute(SportletProperties.PORTLET_WINDOW_ID, windowId);
 
         StringWriter storedWriter = new StringWriter();
         PrintWriter writer = new PrintWriter(storedWriter);
         if (renderPortlet) {
             if (!transparent) {
                 postframe.append(titleBar.getBufferedOutput(req));
             }
 
             postframe.append(frameView.doStartBorder(event, this));
 
             RenderResponse wrappedResponse = new StoredPortletResponseImpl(event.getHttpServletRequest(), event.getHttpServletResponse(), writer);
 
             if (isClosing) {
                 postframe.append(frameView.doRenderCloseFrame(event, this));
             } else {
                 //System.err.println("in portlet frame render: class= " + portletClass + " setting prev mode= " + req.getPreviousMode() + " cur mode= " + req.getMode());
                 Throwable ex = getError(req);
                 if (ex != null) {
                     doRenderError(req, wrappedResponse, ex);
                     postframe.append(storedWriter.toString());
                 } else if ((titleBar != null) && (titleBar.hasRenderError())) {
                     postframe.append(titleBar.getErrorMessage());
                 } else {
                     try {
                         if (!renderParams.isEmpty()) {
                             //System.err.println("PortletFrame: in " + portletClass + " sending render params");
                             //System.err.println("in render " + portletClass + " there are render params in the frame setting in request! key= " + SportletProperties.RENDER_PARAM_PREFIX + portletClass + "_" + componentIDStr);
                             req.setAttribute(SportletProperties.RENDER_PARAM_PREFIX + portletClass + "_" + componentIDStr, renderParams);
                         }
                         DefaultPortletRender render = event.getRender();
                         portletInvoker.service((String) req.getAttribute(SportletProperties.PORTLETID), render, (HttpServletRequest) req, (HttpServletResponse) wrappedResponse);
                         lastFrame = storedWriter.toString();
                         postframe.append(lastFrame);
                     } catch (Exception e) {
                         doRenderError(req, wrappedResponse, e);
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
 
         setBufferedOutput(req, frame);
 
         // check if expiration was set in render response
         Map props = (Map) req.getAttribute(SportletProperties.PORTAL_PROPERTIES);
         if (props != null) {
             List vals = (List) props.get(RenderResponse.EXPIRATION_CACHE);
             if (vals != null) {
                 String cacheExpiryStr = (String) vals.get(0);
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
 
     public void setError(PortletRequest req, Throwable ex) {
         req.getPortletSession(true).setAttribute(SportletProperties.PORTLETERROR + portletClass, ex, PortletSession.APPLICATION_SCOPE);
     }
 
     public Throwable getError(PortletRequest req) {
         Throwable ex = (Throwable) req.getPortletSession(true).getAttribute(SportletProperties.PORTLETERROR + portletClass, PortletSession.APPLICATION_SCOPE);
         removeError(req);
         return ex;
     }
 
     public void removeError(PortletRequest req) {
         req.getPortletSession(true).removeAttribute(SportletProperties.PORTLETERROR + portletClass, PortletSession.APPLICATION_SCOPE);
     }
 
     public void doRenderError(RenderRequest req, RenderResponse res, Throwable ex) {
         Throwable cause = ex.getCause();
         if (cause == null) {
             cause = ex;
         }
         try {
             MailService mailService = (MailService) PortletServiceFactory.createPortletService(MailService.class, true);
             Boolean sendMail = Boolean.valueOf(portalConfigService.getProperty(PortalConfigService.ENABLE_ERROR_HANDLING));
             if (sendMail.booleanValue()) {
                 MailMessage mailToUser = new MailMessage();
                String noreply = portalConfigService.getProperty(PortalConfigService.MAIL_FROM);
                mailToUser.setSender(noreply);
                 String portalAdmin = portalConfigService.getProperty(PortalConfigService.PORTAL_ADMIN_EMAIL);
                 mailToUser.setEmailAddress(portalAdmin);
                 mailToUser.setSubject(getLocalizedText(req, "PORTAL_ERROR_SUBJECT"));
                 StringBuffer body = new StringBuffer();
                 body.append(getLocalizedText(req, "PORTAL_ERROR_BODY"));
                 body.append("\n\n");
                 body.append("portlet title: ");
                 body.append(titleBar.getTitle());
                 body.append("\n\n");
                 User user = (User) req.getAttribute(SportletProperties.PORTLET_USER);
                 body.append(DateFormat.getDateTimeInstance().format(Calendar.getInstance().getTime()));
                 body.append("\n\n");
                 if (user != null) {
                     body.append(user);
                     body.append("\n\n");
                 }
                 StringWriter sw = new StringWriter();
                 PrintWriter pout = new PrintWriter(sw);
                 cause.printStackTrace(pout);
                 body.append(sw.getBuffer());
                 mailToUser.setBody(body.toString());
 
                 try {
                     mailService.sendMail(mailToUser);
                     req.setAttribute("lastFrame", lastFrame);
                     PortletRequestDispatcher dispatcher = req.getPortletSession().getPortletContext().getRequestDispatcher("/jsp/errors/custom_error.jsp");
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
             req.setAttribute("error", cause);
             PortletRequestDispatcher dispatcher = req.getPortletSession().getPortletContext().getRequestDispatcher("/jsp/errors/custom_error.jsp");
             dispatcher.include(req, res);
         } catch (Exception e) {
             System.err.println("Unable to include custom error page!!");
             e.printStackTrace();
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
 
     public String toString() {
         StringBuffer sb = new StringBuffer();
         sb.append(super.toString());
         sb.append("\nportlet class=").append(portletClass);
         return sb.toString();
     }
 }
