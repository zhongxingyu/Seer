 package cz.fi.muni.pa165.calorycounter.frontend;
 
 import cz.fi.muni.pa165.calorycounter.serviceapi.UserService;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.AuthUserDto;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.UserRole;
 import javax.servlet.http.HttpSession;
 import net.sourceforge.stripes.action.ActionBean;
 import net.sourceforge.stripes.action.Before;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.controller.LifecycleStage;
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
 @UrlBinding("/auth/{$event}")
 public class AuthenticationActionBean extends BaseActionBean {
 
     final static Logger log = LoggerFactory.getLogger(AuthenticationActionBean.class);
     @SpringBean
     protected UserService userService;
     @Validate(on = {"login", "register"}, required = true, minlength = 3, trim = true)
     private String password;
     @Validate(on = {"login"}, required = true)
     private String username;
     @ValidateNestedProperties(value = {
         @Validate(on = "register", field = "username", required = true, trim = true),
         @Validate(on = "register", field = "name", required = true),
         @Validate(on = "register", field = "age", required = true),
         @Validate(on = "register", field = "sex", required = true),
         @Validate(on = "register", field = "weightCategory", required = true)
     })
     private AuthUserDto user;
     private final Gender[] genders = cz.fi.muni.pa165.calorycounter.frontend.Gender.values();
     private final UserRole defaultRole = cz.fi.muni.pa165.calorycounter.serviceapi.dto.UserRole.USER;
 
     public Gender[] getGenders() {
         return genders;
     }
 
     public UserRole getDefaultRole() {
         return defaultRole;
     }
 
     public void setUser(AuthUserDto user) {
         this.user = user;
     }
 
     public AuthUserDto getUser() {
         return user;
     }
 
     public void setUsername(String username) {
         this.username = username;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     @Before(stages = LifecycleStage.HandlerResolution)
     public void logOutUser() {
         getContext().getRequest().getSession().setAttribute("user", null);
     }
 
     @DefaultHandler
     public Resolution showLoginForm() {
         log.debug("showLoginForm()");
         return new ForwardResolution("/authentication/login.jsp");
     }
 
     public Resolution login() {
         log.debug("login()");
         HttpSession session = getContext().getRequest().getSession();
         ActionBean originalBean = (ActionBean) session.getAttribute("authPath");
         AuthUserDto user;
         try {
             user = userService.login(username, password);
         } catch (RecoverableDataAccessException e) {
             this.getContext().getValidationErrors().addGlobalError(new SimpleError("login.failed"));
             return new RedirectResolution(this.getClass(), "showLoginForm");
         }
         log.debug("Login user: " + user);
 
         if (user != null) {
             setSessionUser(user);
         } else {
             this.getContext().getValidationErrors().addGlobalError(new SimpleError("login.failed"));
             return new ForwardResolution("/authentication/login.jsp");
         }
 
        if (originalBean != null) {
             return new RedirectResolution(originalBean.getClass());
         } else {
             return new RedirectResolution("/index.jsp");
         }
     }
 
     public Resolution showRegisterForm() {
         log.debug("showRegisterForm()");
         return new ForwardResolution("/authentication/register.jsp");
     }
 
     public Resolution register() {
         log.debug("registerUser():" + user + ", " + password);
         Long userId;
         try {
             userId = userService.register(user, password);
         } catch (IllegalArgumentException e) {
             this.getContext().getValidationErrors().addGlobalError(new SimpleError("register.username.error"));
             return new ForwardResolution("/authentication/register.jsp");
         } catch (RecoverableDataAccessException e) {
             this.getContext().getValidationErrors().addGlobalError(new SimpleError("register.error"));
             return new ForwardResolution("/authentication/register.jsp");
         }
         user.setUserId(userId);
         setSessionUser(user);
         return new RedirectResolution("/index.jsp");
     }
 
     public Resolution logout() {
         logOutUser();
         return new RedirectResolution("/authentication/login.jsp");
     }
 }
