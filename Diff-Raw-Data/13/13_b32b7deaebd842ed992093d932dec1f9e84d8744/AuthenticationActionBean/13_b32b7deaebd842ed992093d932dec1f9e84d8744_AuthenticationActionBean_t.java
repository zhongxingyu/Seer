 package cz.fi.muni.pa165.calorycounter.frontend;
 
import static cz.fi.muni.pa165.calorycounter.frontend.BaseActionBean.escapeHTML;
 import cz.fi.muni.pa165.calorycounter.serviceapi.UserService;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.AuthUserDto;
 import javax.servlet.http.HttpSession;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
import net.sourceforge.stripes.action.LocalizableMessage;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.SimpleError;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.springframework.dao.RecoverableDataAccessException;
 
 /**
  *
  * @author Martin Bryndza
  */
 @DoesNotRequireLogin
 @UrlBinding("/auth/{$event}")
 public class AuthenticationActionBean extends BaseActionBean {
 
     final static Logger log = LoggerFactory.getLogger(AuthenticationActionBean.class);
     @SpringBean
     protected UserService userService;
 
     @Validate(on = {"login"}, required = true)
     private String password;
 
     @Validate(on = {"login"}, required = true)
     private String username;
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     @DefaultHandler
     public Resolution showLoginForm() {
         log.debug("showLoginForm()");
         return new ForwardResolution("/authentication/login.jsp");
     }
 
     public Resolution login() {
         log.debug("login()");
         HttpSession session = getContext().getRequest().getSession();
         String path = (String) session.getAttribute("authPath");
        try {
            user = userService.login(username, password);
        } catch (RecoverableDataAccessException e) {
            this.getContext().getValidationErrors().addGlobalError(new SimpleError("login.failed"));
            return new ForwardResolution(this.getClass(), "showLoginForm");
        }
         log.debug("Login user: " + user);
 
         if (user != null) {
             session.setAttribute("user", user);
         } else {
             this.getContext().getValidationErrors().addGlobalError(new SimpleError("login.failed"));
             return new ForwardResolution("/authentication/login.jsp");
         }
 
         if (path != null) {
             return new RedirectResolution(path.substring(0));
         } else {
             return new RedirectResolution("/index.jsp");
         }
     }
 
     public Resolution showRegisterForm() {
         log.debug("showRegisterForm()");
         return new ForwardResolution("/authentication/register.jsp");
     }
 
     public Resolution register() {
         return new RedirectResolution("/index.jsp");
     }
 
     public Resolution logout() {
         getContext().getRequest().getSession().setAttribute("user", null);
         return new RedirectResolution("/authentication/login.jsp");
     }
 }
