 package cz.fi.muni.pa165.calorycounter.frontend;
 
 import cz.fi.muni.pa165.calorycounter.serviceapi.UserService;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.AuthUserDto;
 import cz.fi.muni.pa165.calorycounter.serviceapi.dto.WeightCategory;
 import net.sourceforge.stripes.action.Before;
 import net.sourceforge.stripes.action.DefaultHandler;
 import net.sourceforge.stripes.action.ForwardResolution;
 import net.sourceforge.stripes.action.RedirectResolution;
 import net.sourceforge.stripes.action.Resolution;
 import net.sourceforge.stripes.action.UrlBinding;
 import net.sourceforge.stripes.controller.LifecycleStage;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.LocalizableError;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
import org.springframework.dao.RecoverableDataAccessException;
 
 /**
  * Stripes ActionBean for handling user profile operations.
  *
  * @author Martin Pasko (smartly23)
  */
 @RequireLogin
 @UrlBinding("/myprofile/{$event}")
 public class ProfileActionBean extends BaseActionBean {
 
     @ValidateNestedProperties(value = {
         @Validate(on = "save", field = "name", required = true),
         @Validate(on = "save", field = "age", required = true),
         @Validate(on = "save", field = "sex", required = true),
         @Validate(on = "save", field = "weightCategory", required = true)
     })
     private AuthUserDto user;
     @Validate(on = "confirmChangePassword", required = true)
     private String oldPassword;
     @Validate(on = "confirmChangePassword", required = true, minlength = 8)
     private String newPassword;
     @Validate(on = "confirmChangePassword", required = true)
     private String confirmNewPassword;
     private final Gender[] genders = cz.fi.muni.pa165.calorycounter.frontend.Gender.values();
     @SpringBean //Spring can inject even to private and protected fields
     private UserService userService;
     final static Logger log = LoggerFactory.getLogger(ProfileActionBean.class);
 
     public WeightCategory[] getWeightCategories() {
         return WeightCategory.values();
     }
 
     public void setOldPassword(String oldPassword) {
         this.oldPassword = oldPassword;
     }
 
     public void setNewPassword(String newPassword) {
         this.newPassword = newPassword;
     }
 
     public void setConfirmNewPassword(String confirmNewPassword) {
         this.confirmNewPassword = confirmNewPassword;
     }
 
     public String getOldPassword() {
         return oldPassword;
     }
 
     public String getNewPassword() {
         return newPassword;
     }
 
     public String getConfirmNewPassword() {
         return confirmNewPassword;
     }
 
     public void setUser(AuthUserDto user) {
         this.user = user;
     }
 
     public AuthUserDto getUser() {
         return user;
     }
 
     @Before(stages = LifecycleStage.BindingAndValidation)
     protected void loadUser() {
         user = getSessionUser();
     }
 
     @DefaultHandler
     public Resolution show() {
         log.debug("show()");
         return new ForwardResolution("/profile/show.jsp");
     }
 
     public Resolution edit() {
         log.debug("edit() user {}", user);
         return new ForwardResolution("/profile/edit.jsp");
     }
 
     public Resolution save() {
         log.debug("save() user {}", user);
         userService.update(user);
         return new RedirectResolution(this.getClass(), "show");
     }
 
     public Resolution changePassword() {
         log.debug("changePasword()");
         return new ForwardResolution("/profile/changePassword.jsp");
     }
 
     public Resolution confirmChangePassword() {
         log.debug("changePassword()");
        try {
            AuthUserDto actualUser = userService.login(user.getUsername(), oldPassword);
        } catch (RecoverableDataAccessException e) {
             this.getContext().getValidationErrors().addGlobalError(new LocalizableError("profile.wrongPassword"));
             return new ForwardResolution(this.getClass(), "changePassword");
         }
//        if (userService.login(user.getUsername(), oldPassword) == null) {
//            this.getContext().getValidationErrors().addGlobalError(new LocalizableError("profile.wrongPassword"));
//            return new ForwardResolution(this.getClass(), "changePassword");
//        }
         if (!newPassword.equals(confirmNewPassword)) {
             this.getContext().getValidationErrors().addGlobalError(new LocalizableError("profile.passwordsNotMatch"));
             return new ForwardResolution(this.getClass(), "changePassword");
         }
         userService.setPassword(user.getUsername(), newPassword);
         return new RedirectResolution(this.getClass(), "show");
     }
 
     public Resolution cancel() {
         return new RedirectResolution(this.getClass(), "show");
     }
 
     public Gender[] getGenders() {
         return genders;
     }
 
 }
