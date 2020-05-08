 package bbmri.action;
 
 import bbmri.entities.User;
 import bbmri.service.LoginService;
 import bbmri.service.UserService;
 import net.sourceforge.stripes.action.*;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.LongTypeConverter;
 import net.sourceforge.stripes.validation.Validate;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ori
  * Date: 22.10.12
  * Time: 23:14
  * To change this template use File | Settings | File Templates.
  */
 
 @UrlBinding("/login/{$event}/{user.id}")
 public class LoginActionBean extends BasicActionBean{
 
     @SpringBean
     private UserService userService;
 
     @SpringBean
     private LoginService loginService;
 
     @Validate(converter = LongTypeConverter.class, on = {"login"},
             required = true, minvalue = 1)
     private Long id;
     @Validate(on = {"login"}, required = true)
     private String password;
 
     public String getPassword() {
         return password;
     }
     public void setPassword(String password) {
         this.password = password;
     }
 
     public Long getId() {
         return id;
     }
 
     public void setId(Long id) {
         this.id = id;
     }
 
     @DefaultHandler
     public Resolution login() {
         getContext().setLoggedUser(null);
         User user = loginService.login(id, password);
         if (user != null) {
             getContext().setLoggedUser(user);
             return new RedirectResolution("/project_all.jsp");
         }
         return new ForwardResolution("/index.jsp");
     }
 
     @HandlesEvent("logout")
     public Resolution logoutUser() {
         loginService.logout(getContext().getLoggedUser());
         getContext().setLoggedUser(null);
        return new RedirectResolution("/index.jsp");
     }
 
        public void refreshLoggedUser(){
         getContext().setLoggedUser(userService.getById(getLoggedUser().getId()));
      }
 
 }
