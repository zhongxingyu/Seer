 package bbmri.action;
 
 import bbmri.entities.User;
 import bbmri.service.UserService;
 import net.sourceforge.stripes.action.*;
 import net.sourceforge.stripes.controller.LifecycleStage;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.Validate;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.annotation.security.PermitAll;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Ori
  * Date: 24.10.12
  * Time: 14:12
  * To change this template use File | Settings | File Templates.
  */
 @PermitAll
 @UrlBinding("/account/{$event}/{user.id}")
 public class AccountActionBean extends BasicActionBean {
 
     Logger logger = LoggerFactory.getLogger(this.getClass().getName());
 
     private static final String MY_ACCOUNT = "/my_account.jsp";
 
     @Validate(on = {"changePassword"}, required = true)
     private String password;
     @Validate(on = {"changePassword"}, required = true)
     private String password2;
     private User user;
 
     public String getPassword() {
         return password;
     }
 
     public void setPassword(String password) {
         this.password = password;
     }
 
     public String getPassword2() {
         return password2;
     }
 
     public void setPassword2(String password2) {
         this.password2 = password2;
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     @DontValidate
     @DefaultHandler
     public Resolution display() {
         user = getLoggedUser();
         return new ForwardResolution(MY_ACCOUNT);
     }
 
 
     @Before(stages = LifecycleStage.BindingAndValidation, on = {"update", "changePassword"})
     public void fillInputs() {
         user = getLoggedUser();
     }
 
     @DontValidate
     public Resolution update() {
        logger.debug("Update: User: " + user);
        User userDB = getLoggedUser();

         if (user.getName() != null) {
            userDB.setName(user.getName());
         }
         if (user.getSurname() != null)
            userDB.setSurname(user.getSurname());
 
        userService.update(userDB);
         return new RedirectResolution(this.getClass(), "display");
     }
     @DontValidate
     public Resolution changePassword() {
        User userDB = getLoggedUser();
         if (password != null && password2 != null) {
             if (password.equals(password2))
                userDB.setPassword(password);
            userService.update(userDB);
             getContext().getMessages().add(
                     new SimpleMessage("Password was changed")
             );
         }
         return new RedirectResolution(this.getClass(), "display");
     }
 }
