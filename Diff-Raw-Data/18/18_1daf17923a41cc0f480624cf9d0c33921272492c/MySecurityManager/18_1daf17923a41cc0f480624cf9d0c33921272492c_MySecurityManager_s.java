 package org.alx.stripestemplate.stripes.noext;
 
import net.sourceforge.stripes.action.ActionBean;
import net.sourceforge.stripes.action.ErrorResolution;
import net.sourceforge.stripes.action.RedirectResolution;
import net.sourceforge.stripes.action.Resolution;
 import org.alx.stripestemplate.action.BaseActionBean;
 import org.alx.stripestemplate.action.LoginActionBean;
 import org.alx.stripestemplate.model.Role;
 import org.alx.stripestemplate.model.User;
 import org.alx.stripestemplate.util.MLogger;
 import org.stripesstuff.plugin.security.InstanceBasedSecurityManager;
 import org.stripesstuff.plugin.security.SecurityHandler;
 
 import javax.servlet.http.HttpServletResponse;
 import java.lang.reflect.Method;
 import java.util.Collection;
 
 /**
  * @author Alexis Boissonnat - alexis.boissonnat 'at' gmail.com
  */
 public class MySecurityManager extends InstanceBasedSecurityManager implements SecurityHandler {
 
     private static final MLogger logger = MLogger.getLogger(MySecurityManager.class);
 
     @Override
     protected Boolean isUserAuthenticated(ActionBean bean, Method handler) {
         return getUser(bean) != null;
     }
 
     @Override
     protected Boolean hasRoleName(ActionBean bean, Method handler, String role) {
         User user = getUser(bean);
         logger.debug("User : " + user);
         if (user != null) {
             if (user.hasRole(role)) return true;
         }
         return false;
     }
 
     public Resolution handleAccessDenied(ActionBean bean, Method handler) {
         if (!isUserAuthenticated(bean, handler)) {
             RedirectResolution resolution =
                 new RedirectResolution(LoginActionBean.class);
             if (bean.getContext().getRequest().getMethod()
                 .equalsIgnoreCase("GET"))
             {
                 String loginUrl = ((BaseActionBean) bean).getLastUrl();
                 resolution.addParameter("loginUrl", loginUrl);
             }
             return resolution;
         }
        return new ErrorResolution(HttpServletResponse.SC_UNAUTHORIZED);
     }
 
     private User getUser(ActionBean bean) {
         logger.debug("Get user from bean : " + ((BaseActionBean) bean).getContext().getUser());
         return ((BaseActionBean) bean).getContext().getUser();
     }
 
 }
