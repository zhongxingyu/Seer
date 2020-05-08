 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.hibernate.coreuser;
 
import org.acegisecurity.ConfigAttribute;
import org.acegisecurity.ConfigAttributeDefinition;
import org.acegisecurity.SecurityConfig;
 import org.acegisecurity.context.SecurityContextHolder;
 import org.acegisecurity.Authentication;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
import org.apache.commons.lang.Validate;

 import com.globant.katari.hibernate.coreuser.domain.CoreUser;
 import com.globant.katari.hibernate.coreuser.domain.CoreUserDetails;
 
 /** Convenient operations useful for all modules, getCurrentUser for now.
  */
 public final class SecurityUtils {
 
   /** The class logger.
    */
   private static Logger log = LoggerFactory.getLogger(SecurityUtils.class);
 
   /** Constructor.
    *
    * The default private constructor for an Utility Class.
    */
   private SecurityUtils() {
   }
 
   /** Obtains the currently logged in user.
    *
    * @return the currently logged in user. It is a detached User instance. It
    * returns null, if no user has logged in yet.
    */
   public static CoreUser getCurrentUser() {
     log.trace("Entering getCurrentUser");
 
     // Obtains the current user.
     Authentication authentication;
     authentication = SecurityContextHolder.getContext().getAuthentication();
     if (authentication == null) {
       log.trace("Leaving getCurrentUser with null");
       return null;
     }
     Object principal = authentication.getPrincipal();
     if (principal == null) {
       log.trace("Leaving getCurrentUser with null");
       return null;
     }
 
     CoreUser user = ((CoreUserDetails) principal).getUser();
 
     log.trace("Leaving getCurrentUser");
     return user;
   }
 }
 
