 package org.mule.galaxy.util;
 
 import java.util.Collections;
 import java.util.Set;
 
 import org.acegisecurity.Authentication;
 import org.acegisecurity.GrantedAuthority;
 import org.acegisecurity.context.SecurityContext;
 import org.acegisecurity.context.SecurityContextHolder;
 import org.acegisecurity.runas.RunAsUserToken;
import org.mule.galaxy.Item;
import org.mule.galaxy.impl.jcr.UserDetailsWrapper;
import org.mule.galaxy.security.Permission;
import org.mule.galaxy.security.User;
 
 public final class SecurityUtils {
     public static final User SYSTEM_USER = new User("system");
     
     public static User getCurrentUser() {
         SecurityContext ctx = SecurityContextHolder.getContext();
         if (ctx == null) {
             return null;
         }
         Authentication auth = ctx.getAuthentication();
         if (auth == null) {
             return null;
         }
         
         UserDetailsWrapper wrapper = (UserDetailsWrapper) auth.getPrincipal();
         if (wrapper == null) {
             return null;
         }
         return wrapper.getUser();
     }
     
     public static void doPriveleged(Runnable runnable) {
         doAs(SYSTEM_USER, runnable);
     }
 
     public static void doAs(User user, Runnable runnable) {
         SecurityContext context = SecurityContextHolder.getContext();
         Authentication prevAuth = context.getAuthentication();
         try {
             Set<Permission> perms = Collections.emptySet();
            UserDetailsWrapper wrapper = new UserDetailsWrapper(SYSTEM_USER, perms, "");
             Authentication auth = new RunAsUserToken("system", wrapper, "", new GrantedAuthority[0], User.class);
             context.setAuthentication(auth);
             
             runnable.run();
         } finally {
             context.setAuthentication(prevAuth);
         }
     }
 
     public static boolean appliesTo(Permission p, Class<? extends Item> itemClass){
         for (Class<? extends Item> c : p.getAppliesTo()) {
             if (c.isAssignableFrom(itemClass)) return true;
         }
         return false;
     }
 }
