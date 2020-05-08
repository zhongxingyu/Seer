 package de.flower.rmt.service.security;
 
 import de.flower.rmt.model.db.entity.User;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.security.core.context.SecurityContextHolderStrategy;
 import org.springframework.stereotype.Service;
 
 /**
  * @author flowerrrr
  */
 @Service
 public class SecurityService implements ISecurityService {
 
     @Autowired
     private SecurityContextHolderStrategy schs;
 
     @Override
     public UserDetailsBean getCurrentUser() {
        Object o = schs.getContext().getAuthentication().getPrincipal();
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
