 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.layout.impl.PortletFrameEventImpl;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.portlet.impl.SportletRequest;
 import org.gridlab.gridsphere.portlet.impl.SportletResponse;
 import org.gridlab.gridsphere.portlet.impl.SportletWindow;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 import org.gridlab.gridsphere.portletcontainer.GridSphereProperties;
 import org.gridlab.gridsphere.portletcontainer.PortletErrorMessage;
 import org.gridlab.gridsphere.portletcontainer.UserPortletManager;
 import org.gridlab.gridsphere.services.user.UserManagerService;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 public class PortletFrame extends BasePortletComponent implements PortletTitleBarListener {
 
     // renderPortlet is true in doView and false on minimized
     private boolean renderPortlet = true;
 
     private String componentIDStr = null;
 
     private PortletWindow portletWindow = null;
     private String portletClass = null;
     private PortletTitleBar titleBar = null;
     private List listeners = new ArrayList();
     private PortletErrorMessage error = null;
     private boolean transparent = false;
 
     public PortletFrame() {}
 
     public void setPortletClass(String portletClass) {
         this.portletClass = portletClass;
     }
 
     public String getPortletClass() {
         return portletClass;
     }
 
     public void setTransparent(boolean transparent) {
         this.transparent = transparent;
     }
 
     public boolean getTransparent() {
         return this.transparent;
     }
 
     public void setWindowState(String windowState) {
         try {
             portletWindow = new SportletWindow(windowState);
         } catch (Exception e) {
         }
     }
 
     public String getWindowState() {
         return portletWindow.toString();
     }
 
     public List init(List list) {
         COMPONENT_ID = list.size();
         componentIDStr = String.valueOf(COMPONENT_ID);
         ComponentIdentifier compId = new ComponentIdentifier();
         compId.setPortletComponent(this);
         compId.setPortletClass(portletClass);
         compId.setComponentID(list.size());
         compId.setClassName(this.getClass().getName());
         list.add(compId);
         if (transparent == false) titleBar = new PortletTitleBar();
         if (titleBar != null) {
             titleBar.setPortletClass(portletClass);
             list = titleBar.init(list);
             titleBar.addTitleBarListener(this);
         }
         return list;
     }
 
     public void addFrameListener(PortletFrameListener listener) {
         listeners.add(listener);
     }
 
     protected void fireFrameEvent(PortletFrameEvent event) throws PortletLayoutException {
         Iterator it = listeners.iterator();
         PortletFrameListener l;
         while (it.hasNext()) {
             l = (PortletFrameListener)it.next();
             l.handleFrameEvent(event);
         }
     }
 
     /**
      * Notifies this listener that a portlet window has been maximized.
      *
      * @param event the window event
      */
     public void handleTitleBarEvent(PortletTitleBarEvent event) throws PortletLayoutException {
 
         if (event.getAction() == PortletTitleBarEvent.Action.MODE_MODIFY) {
             //previousMode = portletMode;
             //portletMode = event.getMode();
         }
         if (event.getAction() == PortletTitleBarEvent.Action.WINDOW_MODIFY) {
 
             PortletWindow.State state = event.getState();
             PortletFrameEvent evt = null;
             if (state == PortletWindow.State.MINIMIZED) {
                 renderPortlet = false;
                 evt = new PortletFrameEventImpl(PortletFrameEvent.Action.FRAME_MINIMIZED, COMPONENT_ID);
             } else if (state == PortletWindow.State.RESIZING) {
                 renderPortlet = true;
                 evt = new PortletFrameEventImpl(PortletFrameEvent.Action.FRAME_RESIZED, COMPONENT_ID);
             } else if (state == PortletWindow.State.MAXIMIZED) {
                 renderPortlet = true;
                 evt = new PortletFrameEventImpl(PortletFrameEvent.Action.FRAME_MAXIMIZED, COMPONENT_ID);
             }
             fireFrameEvent(evt);
         }
     }
 
     public void actionPerformed(GridSphereEvent event) throws PortletLayoutException, IOException {
 
         // process events
         SportletRequest req = event.getSportletRequest();
         SportletResponse res = event.getSportletResponse();
         PortletContext ctx = event.getPortletContext();
 
         req.setAttribute(GridSphereProperties.PORTLETID, portletClass);
         req.setAttribute(GridSphereProperties.COMPONENT_ID, componentIDStr);
 
         String newmode = req.getParameter(GridSphereProperties.PORTLETMODE);
         if (newmode != null) {
             req.setMode(Portlet.Mode.getInstance(newmode));
         } else {
             if (titleBar != null) {
                 req.setMode(titleBar.getPortletMode());
             } else {
                 req.setMode(Portlet.Mode.VIEW);
             }
         }
        req.setModeModifier(Portlet.ModeModifier.CURRENT);
 
         // Set the portlet data
        /*
         PortletData data = null;
         try {
             UserManagerService userManager = (UserManagerService)ctx.getService(UserManagerService.class);
             data = userManager.getPortletData(req.getUser(), portletClass);
         } catch (PortletServiceException e) {}
         req.setData(data);
        */
 
         // now perform actionPerformed on Portlet if it has an action
         DefaultPortletAction action = event.getAction();
         if (action.getName() != "") {
             try {
                 UserPortletManager userPortletManager = event.getUserPortletManager();
                 userPortletManager.actionPerformed(portletClass, action, req, res);
             } catch (PortletException e) {
                 error = new PortletErrorMessage(portletClass, e);
             }
         }
 
     }
 
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
         super.doRender(event);
         SportletRequest req = event.getSportletRequest();
         SportletResponse res = event.getSportletResponse();
 
         req.setAttribute(GridSphereProperties.PORTLETID, portletClass);
         req.setAttribute(GridSphereProperties.COMPONENT_ID, componentIDStr);
 
         ///// begin portlet frame
         PrintWriter out = res.getWriter();
 
         out.println("<!-- PORTLET STARTS HERE -->");
         //out.println("<div class=\"window-main\">");
         out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");        // this is the main table around one portlet
        req.setMode(Portlet.Mode.VIEW);
        req.setPreviousMode(Portlet.Mode.VIEW);
         if (titleBar != null) titleBar.doRender(event);
        //req.setMode(titleBar.getPortletMode());
 
 
         if (error != null) {
             out.println(error.getMessage());
         } else {
 
             if (renderPortlet) {
                 if (!transparent) {
                     out.println("<tr><td class=\"window-content\">");      // now the portlet content begins
                 } else {
                     out.println("<tr><td>");
                 }
                 UserPortletManager userPortletManager = event.getUserPortletManager();
                 try {
                     userPortletManager.service(portletClass, req, res);
                 } catch (PortletException e) {
                     out.println("Portlet Unavailable");
                     out.println(e.toString());
                 }
                 out.println("</td></tr>");
             } else {
                 out.println("<tr><td class=\"window-content-minimize\">");      // now the portlet content begins
                 out.println("</td></tr>");
             }
         }
         //out.println("</div>");
 
         out.println("</table>");
         out.println("<!--- PORTLET ENDS HERE -->");
     }
 
 }
