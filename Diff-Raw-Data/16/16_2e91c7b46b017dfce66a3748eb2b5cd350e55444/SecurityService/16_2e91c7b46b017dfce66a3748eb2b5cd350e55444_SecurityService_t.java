 package de.flower.rmt.service.security;
 
 import de.flower.rmt.model.db.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
 import org.springframework.security.core.context.SecurityContextHolderStrategy;
 import org.springframework.stereotype.Service;
 
 /**
  * @author flowerrrr
  */
 @Service
 public class SecurityService implements ISecurityService {
 
    private final static Logger log = LoggerFactory.getLogger(SecurityService.class);

     @Autowired
     private SecurityContextHolderStrategy schs;
 
     @Override
     public UserDetailsBean getCurrentUser() {
        Authentication authentication = schs.getContext().getAuthentication();
        if (authentication == null) {
            // RMT-684. gracefully tolerate missconfiguration and treat situation like an anonymous request.
            log.warn("Security context not set. Was SecurityContextPersistenceFilter called in current request?");
            return null;
        }
        Object o = authentication.getPrincipal();
         if (o instanceof UserDetailsBean) {
             UserDetailsBean principal = (UserDetailsBean) o;
             return principal;
         } else {
             // anonymous user.
             return null;
         }
     }
 
     @Override
     public boolean isCurrentUserLoggedIn() {
         return getCurrentUser() != null;
     }
 
     @Override
     public boolean isCurrentUser(User user) {
         return getCurrentUser().getUser().equals(user);
     }
 
     @Override
     public User getUser() {
         return getCurrentUser().getUser();
     }
 }
