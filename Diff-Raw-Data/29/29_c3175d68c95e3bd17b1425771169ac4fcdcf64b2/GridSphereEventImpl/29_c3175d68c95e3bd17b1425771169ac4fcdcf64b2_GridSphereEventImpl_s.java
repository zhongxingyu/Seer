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
 import org.gridlab.gridsphere.portletcontainer.*;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.ServletConfig;
 
 public class GridSphereEventImpl implements GridSphereEvent {
 
     protected SportletRequest portletRequest;
     protected SportletResponse portletResponse;
     protected PortletContext portletContext;
 
     protected int PortletComponentID = -1;
     protected String ActivePortletID = null;
     protected UserPortletManager userPortletManager = UserPortletManager.getInstance();
 
     public GridSphereEventImpl(PortletContext ctx, HttpServletRequest req, HttpServletResponse res) {
         portletRequest = new SportletRequestImpl(req);
         portletResponse = new SportletResponse(res, portletRequest);
         portletContext = ctx;
 
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
         return portletRequest;
     }
 
     public SportletResponse getSportletResponse() {
         return portletResponse;
     }
 
     public PortletContext getPortletContext() {
         return portletContext;
     }
 
     public GridSphereEvent.Action getAction() {
         String action = portletRequest.getParameter(GridSphereProperties.ACTION);
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
        SportletURI sportletURI = new SportletURI(portletResponse);
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
