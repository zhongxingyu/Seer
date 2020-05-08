 package itspring.sessionmanagement;
 
 import itspring.domain.User;
 import itspring.services.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationListener;
 import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
 import org.springframework.security.core.Authentication;
 import org.springframework.stereotype.Component;
 
 import java.util.Date;
 
 /**
  * @author Andrey Volkov
  */
 @Component
 public class SuccessfulAuthenticationListener implements ApplicationListener<AuthenticationSuccessEvent> {
     @Autowired
     UserService userService;
 
     @Override
     public void onApplicationEvent(AuthenticationSuccessEvent event) {
         User user = (User) ((Authentication) event.getSource()).getPrincipal();
        User fromDb = userService.findById(user.getId());
        fromDb.setOnline(true);
        fromDb.setLastLoginData(new Date());
        userService.save(fromDb);
     }
 }
