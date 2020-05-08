 package itspring.sessionmanagement;
 
 import itspring.domain.User;
 import itspring.services.UserService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationListener;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.session.SessionDestroyedEvent;
 import org.springframework.security.web.session.HttpSessionDestroyedEvent;
 import org.springframework.stereotype.Component;
 
 /**
  * @author Andrey Volkov
  */
 @Component
 public class SessionDestroyedListener implements ApplicationListener<HttpSessionDestroyedEvent> {
     @Autowired
     UserService userService;
 
     @Override
     public void onApplicationEvent(HttpSessionDestroyedEvent event) {
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
         if (authentication != null && authentication.getPrincipal() instanceof User) {
             User user = (User) authentication.getPrincipal();
            user.setOnline(false);
            userService.save(user);
         }
     }
 }
