 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.layout.impl.PortletTitleBarEventImpl;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.GuestUser;
 import org.gridlab.gridsphere.portlet.impl.SportletRequest;
 import org.gridlab.gridsphere.portlet.impl.SportletResponse;
 import org.gridlab.gridsphere.portlet.impl.SportletURI;
 import org.gridlab.gridsphere.portletcontainer.*;
 import org.gridlab.gridsphere.portletcontainer.descriptor.AllowsWindowStates;
 import org.gridlab.gridsphere.portletcontainer.descriptor.Markup;
 import org.gridlab.gridsphere.portletcontainer.descriptor.SupportsModes;
 import org.gridlab.gridsphere.event.impl.WindowEventImpl;
 import org.gridlab.gridsphere.event.WindowEvent;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 
 public class PortletTitleBar extends BasePortletComponent {
 
     private String title = "";
     private String portletClass = null;
 
     private PortletWindow.State portletWindowState = PortletWindow.State.NORMAL;
 
     private String[] portletModes = { Portlet.Mode.VIEW.toString() };
     private Portlet.Mode portletMode = Portlet.Mode.VIEW;
     private Portlet.Mode previousMode = null;
 
     protected String name = PortletTitleBar.class.getName();
     private List listeners = new ArrayList();
 
     private PortletSettings settings;
     private List modeList;
     private List allowsWindowStates = new ArrayList();
     private String ErrorMessage = null;
 
     public PortletTitleBar() {}
 
     public void setPortletClass(String portletClass) {
         this.portletClass = portletClass;
     }
 
     public String getPortletClass() {
         return portletClass;
     }
 
     public String getTitle() {
         return title;
     }
 
     public void setTitle(String title) {
         this.title = title;
     }
 
     public void setPortletWindowState(PortletWindow.State portletWindow) {
         this.portletWindowState = portletWindow;
     }
 
     public PortletWindow.State getPortletWindowState() {
         return portletWindowState;
     }
 
     public void setPortletMode(Portlet.Mode portletMode) {
         this.portletMode = portletMode;
     }
 
     public Portlet.Mode getPortletMode() {
         return portletMode;
     }
 
     public void addTitleBarListener(PortletTitleBarListener listener) {
         listeners.add(listener);
     }
 
     public List init(List list) {
         COMPONENT_ID = list.size();
         ComponentIdentifier compId = new ComponentIdentifier();
         compId.setPortletLifecycle(this);
         compId.setPortletClass(portletClass);
         compId.setComponentID(list.size());
         compId.setClassName(this.getClass().getName());
         list.add(compId);
 
         PortletRegistryManager registryManager = PortletRegistryManager.getInstance();
         String appID = registryManager.getApplicationPortletID(portletClass);
         ApplicationPortlet appPortlet = registryManager.getApplicationPortlet(appID);

         SupportsModes supportedModes = appPortlet.getPortletApplicationDescriptor().getSupportsModes();
         modeList = supportedModes.getMarkupList();
         ConcretePortlet concPortlet = appPortlet.getConcretePortlet(portletClass);
         settings = concPortlet.getSportletSettings();
 
         // Get window state settings
         AllowsWindowStates allowedWindowStates = appPortlet.getPortletApplicationDescriptor().getAllowsWindowStates();
         allowsWindowStates = allowedWindowStates.getWindowStatesAsStrings();
         return list;
     }
 
     public void destroy() {
 
     }
 
     public List makeWindowLinks(GridSphereEvent event) {
 
         SportletURI sportletURI;
 
         // subtract current window state
         List windowStates = new ArrayList(allowsWindowStates);
 
         if (portletWindowState == PortletWindow.State.NORMAL) {
             windowStates.remove(PortletWindow.State.RESIZING.toString().toLowerCase());
         }
         // remove current state from list
         windowStates.remove(portletWindowState.toString().toLowerCase());
 
         // create a URI for each of the window states
         PortletStateLink stateLink;
         List stateLinks = new Vector();
         Iterator it = windowStates.iterator();
         while (it.hasNext()) {
             String winState = (String)it.next();
             sportletURI = event.createNewAction(GridSphereEvent.Action.LAYOUT_ACTION, COMPONENT_ID, portletClass);
             try {
                 stateLink = new PortletStateLink(winState);
                 // Create portlet link Href
                 //modeAction = new DefaultPortletAction(LayoutProperties.CHANGESTATE);
                 //sportletURI.addAction(modeAction);
                 sportletURI.addParameter(GridSphereProperties.PORTLETWINDOW, winState);
                 stateLink.setStateHref(sportletURI.toString());
                 stateLinks.add(stateLink);
             } catch (Exception e) {
                 ErrorMessage += "Unable to create window state link: " + winState + "\n";
             }
         }
         return stateLinks;
     }
 
     public List makeModeLinks(GridSphereEvent event) {
 
         int i;
 
         SportletRequest req = event.getSportletRequest();
 
         // get client preferred markup
         Client client = req.getClient();
 
         // Make sure Client supports supported modes
         Markup m = null;
         Iterator it = modeList.iterator();
         while (it.hasNext()) {
             m = (Markup)it.next();
             if (m.getName().equals(client.getMarkupName())) {
                 portletModes = m.getPortletModesAsStrings();
             }
         }
 
         // subtract current portlet mode
         for (i = 0; i < portletModes.length; i++) {
             if (portletModes[i].equalsIgnoreCase(portletMode.toString())) {
                 portletModes[i] = "";
             }
         }
 
         // create a URI for each of the portlet modes
         PortletURI sportletURI;
         PortletModeLink modeLink;
         List portletLinks = new Vector();
         for (i = 0; i < portletModes.length; i++) {
             sportletURI = event.createNewAction(GridSphereEvent.Action.LAYOUT_ACTION, COMPONENT_ID, portletClass);
             //sportletURI.addParameter(GridSphereProperties.PORTLETID, (String)req.getAttribute(GridSphereProperties.PORTLETID));
             try {
                 modeLink = new PortletModeLink(portletModes[i]);
                 // Create portlet link Href
 
                 //modeAction = new DefaultPortletAction(PortletAction.CHANGEMODE);
                 //sportletURI.addAction(modeAction);
                 sportletURI.addParameter(GridSphereProperties.PORTLETMODE, portletModes[i]);
                 modeLink.setModeHref(sportletURI.toString());
                 portletLinks.add(modeLink);
             } catch (Exception e) {
                 ErrorMessage += "Unable to create portlet mode link: " + portletModes[i] + "\n";
             }
         }
         return portletLinks;
     }
 
     public void actionPerformed(GridSphereEvent event) throws PortletLayoutException, IOException {
         PortletTitleBarEvent evt = new PortletTitleBarEventImpl(event, COMPONENT_ID);
         if (evt.getAction() == PortletTitleBarEvent.Action.WINDOW_MODIFY) {
             PortletRequest req = event.getSportletRequest();
             PortletResponse res = event.getSportletResponse();
             UserPortletManager userManager = event.getUserPortletManager();
             portletWindowState = evt.getState();
             WindowEvent winEvent = null;
 
             if (portletWindowState == PortletWindow.State.MAXIMIZED) {
                 winEvent = new WindowEventImpl(req, WindowEvent.WINDOW_MAXIMIZED);
             } else if (portletWindowState == PortletWindow.State.MINIMIZED) {
                 winEvent = new WindowEventImpl(req, WindowEvent.WINDOW_MINIMIZED);
             } else if (portletWindowState == PortletWindow.State.RESIZING) {
                 winEvent = new WindowEventImpl(req, WindowEvent.WINDOW_RESTORED);
             }
             if (winEvent != null) {
                 try {
                     userManager.windowEvent(portletClass, winEvent, req, res);
                 } catch (PortletException e) {
                     throw new PortletLayoutException("Failed to invoke window event method of portlet: " + portletClass);
                 }
             }
         } else if (evt.getAction() == PortletTitleBarEvent.Action.MODE_MODIFY) {
             previousMode = portletMode;
             portletMode = evt.getMode();
         }
         if (evt != null) fireTitleBarAction(evt);
     }
 
     protected void fireTitleBarAction(PortletTitleBarEvent event) throws PortletLayoutException {
         Iterator it = listeners.iterator();
         PortletTitleBarListener l;
         while (it.hasNext()) {
             l = (PortletTitleBarListener)it.next();
             l.handleTitleBarEvent(event);
         }
     }
 
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
 
         // title bar: configure, edit, help, title, min, max
         SportletRequest req = event.getSportletRequest();
         SportletResponse res = event.getSportletResponse();
 
         // get the appropriate title for this client
         Client client = req.getClient();
         title = settings.getTitle(req.getLocale(), client);
 
         List modeLinks = null, windowLinks = null;
         User user = req.getUser();
         if (user instanceof GuestUser) {
         } else {
             if (portletClass != null) {
                 modeLinks = makeModeLinks(event);
                 windowLinks = makeWindowLinks(event);
             }
         }
 
         req.setMode(portletMode);
         req.setPreviousMode(previousMode);
 
         PrintWriter out = res.getWriter();
 
         String uiTheme = GridSphereConfig.getProperty(GridSphereConfigProperties.UI_THEME);
 
         out.println("<tr><td class=\"window-title\">");
         out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\"><tr>");
 
         // Output portlet mode icons
         if (modeLinks != null) {
             Iterator modesIt = modeLinks.iterator();
             out.println("<td class=\"window-icon-left\">");
             PortletModeLink mode;
             while (modesIt.hasNext()) {
                 mode = (PortletModeLink)modesIt.next();
                 out.println("<a href=\"" +  mode.getModeHref() + "\"><img border=\"0\" src=\"themes/"+uiTheme+"/" +  mode.getImageSrc() + "\" title=\"" + mode.getAltTag() + "\"/></a>");
             }
             out.println("</td>");
         }
 
         // Invoke doTitle of portlet whose action was perfomed
         String actionStr = req.getParameter(GridSphereProperties.ACTION);
         out.println("<td class=\"window-title-name\">");
         if (actionStr != null) {
             UserPortletManager userManager = event.getUserPortletManager();
             //req.setPortletSettings(settings);
             try {
                 userManager.doTitle(portletClass, req, res);
                 out.println(" ("+portletMode.toString()+") ");
                 title = "";
             } catch (PortletException e) {
                 ErrorMessage += "Unable to invoke doTitle on active portlet\n";
                 throw new PortletLayoutException("Unable to invoke doTitle on active portlet " + portletClass + "  " + COMPONENT_ID, e);
             }
 
         } else {
             out.println(title);
         }
 
         out.println("</td>");
 
         // Output window state icons
         if (windowLinks != null) {
             Iterator windowsIt = windowLinks.iterator();
             PortletStateLink state;
             out.println("<td class=\"window-icon-right\">");
             while (windowsIt.hasNext()) {
                 state = (PortletStateLink)windowsIt.next();
                 out.println("<a href=\"" +  state.getStateHref() + "\"><img border=\"0\" src=\"themes/"+uiTheme+"/" +  state.getImageSrc() + "\" title=\"" + state.getAltTag() + "\"/></a>");
             }
             out.println("</td>");
         }
         out.println("</tr></table>");
         out.println("</td></tr>");
     }
 
 
     public boolean hasError() {
         if (ErrorMessage == null) return false;
         return true;
     }
 
     public String getErrorReport() {
         return ErrorMessage;
     }
 
 }
