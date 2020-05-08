 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.login;
 
 import org.gridlab.gridsphere.portlet.*;
 
 import java.io.IOException;
 
 
 public class LogoutPortlet extends AbstractPortlet {
 
     public void doView(PortletRequest request, PortletResponse response) throws PortletException, IOException {
        String title = getPortletSettings().getTitle(request.getLocale(), null);
        if (title == null) {
            title = getPortletSettings().getTitle(getPortletSettings().getDefaultLocale(), null);
         }
         request.setAttribute("GRIDSPHERE_LOGOUT_LABEL", title);
         request.setAttribute("username", request.getUser().getFullName());
         getPortletConfig().getContext().include("/jsp/login/logout.jsp", request, response);
     }
 
 }
