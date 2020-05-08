 package com.exadel.borsch.web.users;
 
 import com.exadel.borsch.dao.AccessRight;
 import com.exadel.borsch.dao.User;
 import java.security.Principal;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.springframework.security.access.AccessDeniedException;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 
 /**
  * @author zubr
  */
 public final class UserUtils {
 
     private static final Logger LOGGER = Logger.getLogger(UserUtils.class.getName());
 
     private UserUtils() {
     }
 
     public static User getUserByPrincipal(Principal principal) {
         if (principal instanceof UsernamePasswordAuthenticationToken) {
             UsernamePasswordAuthenticationToken token = (UsernamePasswordAuthenticationToken) principal;
             if (token.getPrincipal() instanceof BorschUserDetails) {
                 return ((BorschUserDetails) token.getPrincipal()).getUser();
             }
         }
 
         LOGGER.log(Level.SEVERE, "The Principal is not BorschUser");
         return null;
     }
 
     public static void hasRole(Principal principal, AccessRight accessRight) throws AccessDeniedException {
         User user = getUserByPrincipal(principal);
         if (user != null) {
             if (!user.hasAccessRight(accessRight)) {
                 throw new AccessDeniedException("Access for user with login \"" + user.getLogin() + "\" is denied.");
             }
        } else {
             throw new AccessDeniedException("Access is denied.");
         }
     }
 }
