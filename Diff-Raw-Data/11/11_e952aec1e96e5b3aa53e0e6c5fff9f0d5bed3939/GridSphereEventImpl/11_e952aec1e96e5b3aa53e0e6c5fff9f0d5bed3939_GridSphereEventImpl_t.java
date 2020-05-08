 /*
  * Created by IntelliJ IDEA.
  * User: novotny
  * Date: Dec 11, 2002
  * Time: 9:19:06 PM
  * To change template for new class use 
  * Code Style | Class Templates options (Tools | IDE Options).
  */
 package org.gridlab.gridsphere.portletcontainer.impl;
 
 import org.gridlab.gridsphere.portlet.PortletRequest;
 import org.gridlab.gridsphere.portlet.PortletContext;
 import org.gridlab.gridsphere.portlet.AbstractPortlet;
 import org.gridlab.gridsphere.portlet.impl.*;
 import org.gridlab.gridsphere.portletcontainer.GridSphereEvent;
 import org.gridlab.gridsphere.portletcontainer.GridSphereProperties;
 import org.gridlab.gridsphere.portletcontainer.PortletWrapper;
 import org.gridlab.gridsphere.portletcontainer.ApplicationPortlet;
 import org.gridlab.gridsphere.services.container.registry.impl.PortletRegistryManager;
 import org.gridlab.gridsphere.services.container.registry.UserPortletManager;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletConfig;
 
 public class GridSphereEventImpl implements GridSphereEvent {
 
     protected SportletRequest req;
     protected SportletResponse res;
     protected PortletContext ctx;
 
     protected int PortletComponentID = -1;
     protected String ActivePortletID = null;
     protected UserPortletManager userPortletManager = UserPortletManager.getInstance();
 
     public GridSphereEventImpl(ServletConfig config, HttpServletRequest req, HttpServletResponse res) {
         this.req = new SportletRequestImpl(req);
         this.res = new SportletResponse(res, req);
         this.ctx = new SportletContext(config);
 
         String cidStr = req.getParameter(GridSphereProperties.COMPONENT_ID);
         try {
             PortletComponentID = new Integer(cidStr).intValue();
         } catch (NumberFormatException e) {
         }
 
         ActivePortletID = req.getParameter(GridSphereProperties.PORTLETID);
 
     }
 
     public UserPortletManager getUserPortletManager() {
         return userPortletManager;
     }
 
     public SportletRequest getSportletRequest() {
         return req;
     }
 
     public SportletResponse getSportletResponse() {
         return res;
     }
 
     public PortletContext getPortletContext() {
         return ctx;
     }
 
     public GridSphereEvent.Action getAction() {
         String action = req.getParameter(GridSphereProperties.ACTION);
         return GridSphereEvent.Action.toAction(action);
     }
 
     public boolean hasAction() {
         if (getPortletComponentID() != -1) return true;
         return false;
     }
 
    public boolean hasMessagingAction() {
        return false;
    }

     public SportletURI createNewAction(GridSphereEvent.Action action, int PortletComponentID, String ActivePortletID) {
         SportletURI sportletURI = new SportletURI(res);
         sportletURI.addParameter(GridSphereProperties.ACTION, action.toString());
         String sid = new Integer(PortletComponentID).toString();
         sportletURI.addParameter(GridSphereProperties.COMPONENT_ID, sid);
        if (ActivePortletID != null) {
            sportletURI.addParameter(GridSphereProperties.PORTLETID, ActivePortletID);
        }
         return sportletURI;
     }
 
     public int getPortletComponentID() {
         return PortletComponentID;
     }
 
     public String getActivePortletID() {
         return ActivePortletID;
     }
 
 }
