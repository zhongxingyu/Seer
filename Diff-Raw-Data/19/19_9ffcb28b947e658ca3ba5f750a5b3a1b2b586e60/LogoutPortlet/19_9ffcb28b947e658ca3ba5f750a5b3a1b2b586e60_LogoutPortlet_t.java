 /*
  * @author <a href="mailto:novotny@aei.mpg.de">Jason Novotny</a>
  * @version $Id$
  */
 package org.gridlab.gridsphere.portlets.core.login;
 
 import org.gridlab.gridsphere.portlet.*;
 
 import java.io.IOException;
import java.util.Locale;
 
 
 public class LogoutPortlet extends AbstractPortlet {
 
     public void doView(PortletRequest request, PortletResponse response) throws PortletException, IOException {
        Client client = request.getClient();

        Locale locale = Locale.getDefault();
        String title = "";
        User user = request.getUser();
        String userlocale = (String)user.getAttribute(User.LOCALE);
        if (userlocale != null) {
            locale = new Locale(userlocale, "", "");
            title = getPortletSettings().getTitle(locale, client);
        } else {
            locale = getPortletSettings().getDefaultLocale();
            title =  getPortletSettings().getTitle(locale, client);
         }

         request.setAttribute("GRIDSPHERE_LOGOUT_LABEL", title);
         request.setAttribute("username", request.getUser().getFullName());
         getPortletConfig().getContext().include("/jsp/login/logout.jsp", request, response);
     }
 
 }
