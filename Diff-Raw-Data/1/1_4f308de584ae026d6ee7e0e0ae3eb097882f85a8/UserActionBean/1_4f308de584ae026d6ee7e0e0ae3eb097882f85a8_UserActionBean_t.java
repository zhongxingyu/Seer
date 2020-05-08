 package bbmri.action;
 
 import bbmri.entities.User;
 import bbmri.service.UserService;
 import net.sourceforge.stripes.action.*;
 import net.sourceforge.stripes.integration.spring.SpringBean;
 import net.sourceforge.stripes.validation.Validate;
 import net.sourceforge.stripes.validation.ValidateNestedProperties;
 
 import java.util.List;
 
 
 @UrlBinding("/user/{$event}/{user.id}")
 public class UserActionBean extends BasicActionBean {
 
     private User user;
     private long id;
 
     @SpringBean
     private UserService userService;
     private List<User> users;
 
     public List<User> getUsers() {
         return userService.getAll();
     }
 
     public User getUser() {
         return user;
     }
 
     public void setUser(User user) {
         this.user = user;
     }
 
     public long getId() {
         return id;
     }
 
     public void setId(long id) {
         this.id = id;
     }
 
     @ValidateNestedProperties(value = {
             @Validate(on = {"pridej", "uloz"}, field = "name", required = true),
             @Validate(on = {"pridej", "uloz"}, field = "surname", required = true)
     }
     )
 
     @DefaultHandler
     public Resolution zobraz() {
 
         users = userService.getAll();
         return new ForwardResolution("/user_all.jsp");
     }
 
     public Resolution create() {
        getUser();
         userService.create(user);
         return new RedirectResolution(this.getClass(), "zobraz");
     }
 
     public Resolution delete() {
         userService.remove((Long) id);
         return new RedirectResolution(this.getClass(), "zobraz");
     }
 
     public void refreshLoggedUser(){
         getContext().setLoggedUser(userService.getById(getLoggedUser().getId()));
     }
 }
 
 
