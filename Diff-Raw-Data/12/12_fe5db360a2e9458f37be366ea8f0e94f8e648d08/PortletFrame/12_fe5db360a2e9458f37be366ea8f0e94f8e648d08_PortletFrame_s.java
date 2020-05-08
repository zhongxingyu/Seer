 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.layout;
 
 import org.gridlab.gridsphere.layout.impl.PortletFrameEventImpl;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletWindow;
 import org.gridlab.gridsphere.portletcontainer.*;
 import org.gridlab.gridsphere.portletcontainer.impl.SportletDataManager;
 
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
 
     public PortletFrame() {
     }
 
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
         list = super.init(list);
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
             l = (PortletFrameListener) it.next();
             l.handleFrameEvent(event);
         }
     }
 
     /**
      * Notifies this listener that a portlet window has been maximized.
      *
      * @param event the window event
      */
     public void handleTitleBarEvent(PortletTitleBarEvent event) throws PortletLayoutException {
 
 
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
         super.actionPerformed(event);
         // process events
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
         PortletContext ctx = event.getPortletContext();
 
         req.setAttribute(GridSphereProperties.PORTLETID, portletClass);
 
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
 
         // Set the portlet data
         User user = req.getUser();
         PortletData data = null;
         if (!(user instanceof GuestUser)) {
            PortletDataManager dataManager = SportletDataManager.getInstance();
             data = dataManager.getPortletData(req.getUser(), portletClass);
             req.setAttribute(GridSphereProperties.PORTLETDATA, data);
         }
 
 
         // now perform actionPerformed on Portlet if it has an action
         DefaultPortletAction action = event.getAction();
         if (action.getName() != "") {
             try {
                 PortletInvoker.actionPerformed(portletClass, action, req, res);
             } catch (PortletException e) {
                 error = new PortletErrorMessage(portletClass, e);
             }
         }
         // in case portlet mode got reset
         if (titleBar != null) titleBar.setPortletMode(req.getMode());
     }
 
     public void doRender(GridSphereEvent event) throws PortletLayoutException, IOException {
         super.doRender(event);
         PortletRequest req = event.getPortletRequest();
         PortletResponse res = event.getPortletResponse();
 
         req.setAttribute(GridSphereProperties.PORTLETID, portletClass);
 
         ///// begin portlet frame
         PrintWriter out = res.getWriter();
 
         out.println("<!-- PORTLET STARTS HERE -->");
         //out.println("<div class=\"window-main\">");
         out.println("<table border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");        // this is the main table around one portlet
 
         // Render title bar
         if (titleBar != null) titleBar.doRender(event);
 
 
         if (error != null) {
             out.println(error.getMessage());
         } else {
 
             if (renderPortlet) {
                 if (!transparent) {
                     out.println("<tr><td class=\"window-content\">");      // now the portlet content begins
                 } else {
                     out.println("<tr><td>");
                 }
 
                 try {
                     PortletInvoker.service(portletClass, req, res);
                 } catch (PortletException e) {
                     out.println("Portlet Unavailable");
                     out.println(e.toString());
                     e.printStackTrace();
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
