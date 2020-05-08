 
 
 package org.gridlab.gridsphere.portlets.core.admin.portlets;
 
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portlet.service.PortletServiceException;
 import org.gridlab.gridsphere.provider.event.FormEvent;
 import org.gridlab.gridsphere.provider.portlet.ActionPortlet;
 import org.gridlab.gridsphere.services.core.user.UserManagerService;
 import org.gridlab.gridsphere.portletcontainer.PortletSessionManager;
 
 import javax.servlet.UnavailableException;
 import javax.servlet.http.HttpSession;
 import java.util.List;
 import java.util.Iterator;
 import java.util.ArrayList;
 import java.util.Set;
 
 /**
  * The
  */
 public class SessionManagerPortlet extends ActionPortlet {
 
     private PortletSessionManager portletSessionMgr = PortletSessionManager.getInstance();
     private UserManagerService userManager = null;
 
     public void init(PortletConfig config) throws UnavailableException {
         super.init(config);
         try {
             userManager = (UserManagerService)getConfig().getContext().getService(UserManagerService.class);
         } catch (PortletServiceException e) {
             log.error("Unable to get portlet manager instance", e);
         }
         DEFAULT_VIEW_PAGE = "showSessions";
     }
 
 
     public void showSessions(FormEvent event) throws PortletException {
 
         PortletRequest req = event.getPortletRequest();
         List names = new ArrayList();
 
         Set sessions = portletSessionMgr.getSessions();
         Iterator it = sessions.iterator();
         while (it.hasNext()) {
             HttpSession session = (HttpSession)it.next();
             String uid = (String) session.getAttribute(SportletProperties.PORTLET_USER);
             if (uid != null) {
                 User user = userManager.getUser(uid);
                 names.add(user);
             }
         }
         int numSessions = portletSessionMgr.getNumSessions();
 
         req.setAttribute("num_sessions", String.valueOf(numSessions));
 
         req.setAttribute("uids", names);
         setNextState(req, "admin/session/view.jsp");


     }
 
 
 }
