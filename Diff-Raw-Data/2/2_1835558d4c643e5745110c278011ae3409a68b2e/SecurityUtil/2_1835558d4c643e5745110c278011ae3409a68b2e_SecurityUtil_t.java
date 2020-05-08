 package org.otherobjects.cms.security;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.lang.StringUtils;
 import org.otherobjects.cms.bootstrap.OtherObjectsAdminUserCreator;
 import org.otherobjects.cms.model.Role;
 import org.otherobjects.cms.model.User;
 import org.otherobjects.cms.model.UserDao;
 import org.otherobjects.framework.OtherObjectsException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
 import org.springframework.security.core.Authentication;
 import org.springframework.security.core.GrantedAuthority;
 import org.springframework.security.core.context.SecurityContextHolder;
 import org.springframework.security.core.userdetails.UserDetails;
 import org.springframework.security.core.userdetails.UsernameNotFoundException;
 
 /**
  * A tool to provide static convenience methods to work with Spring security's {@link SecurityContextHolder}
  * 
  * @author joerg
  */
 public class SecurityUtil
 {
 
     protected final Logger logger = LoggerFactory.getLogger(getClass());
     
     public static final List<GrantedAuthority> NO_AUTHORITIES = Collections.emptyList();
 
     public static final String [] EDITOR_ROLE_NAMES = {OtherObjectsAdminUserCreator.DEFAULT_ADMIN_ROLE_NAME,OtherObjectsAdminUserCreator.DEFAULT_EDITOR_ROLE_NAME};
 
     static{
         Arrays.sort(EDITOR_ROLE_NAMES);
     }
     /**
      * @return The id of the current user or null if no user is associated with the current thread.
      */
     public static Long getUserId()
     {
         User currentUser = getCurrentUser();
         if (currentUser == null)
             return null;
         else
             return currentUser.getId();
     }
 
     /**
      * @param id
      * @return True if the passed in id is equal to the id of the current User.  Else false.
      */
     public static boolean isCurrentUser(String username)
     {
         String currentUsername = getCurrentUser().getUsername();
         if (username == null || currentUsername == null)
             return false;
 
         return currentUsername.equals(username);
     }
 
     /**
      * Returns true if the current user is an editor.
      * 
      * @return
      */
     public static boolean isEditor()
     {
         //FIXME if we don't have any authentication return true so that the default (existing) workspace is returned. Otherwise JackrabbitSessionFactory.registerNamespaces() will fail
         // when trying to obtain a session
         if (SecurityContextHolder.getContext().getAuthentication() == null)
             return true;
 
         for (GrantedAuthority ga : SecurityContextHolder.getContext().getAuthentication().getAuthorities())
         {
             if (Arrays.binarySearch(EDITOR_ROLE_NAMES,ga.getAuthority())>-1)
                 return true;
         }
         return false;
     }
 
     /**
      * @return Current user or null if no user associated with current thread.
      */
     public static User getCurrentUser()
     {
         if (SecurityContextHolder.getContext().getAuthentication() != null)
         {
 
             Object user = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
             if (user instanceof User)
                 return (User) user;
            else if (user instanceof String && ((String) user).equals("anonymousUser"))
                 // Anonymous user detected
                 return null;
             else
                 throw new OtherObjectsException("Current principal is not of type org.otherobjects.cms.model.User: " + user.toString());
         }
         else
             return null;
     }
 
     /**
      * 
      * @return comma separated list of authorities possessed by current user or 'anonymous' if no logged in user 
      */
     public static String getCurrentAuthoritiesAsString()
     {
         final User currentUser = getCurrentUser();
         if (currentUser == null)
             return "anonymous";
         else
             return StringUtils.join(new Iterator<String>()
             {
                 Iterator<GrantedAuthority> ri = currentUser.getRoles().iterator();
 
                 public boolean hasNext()
                 {
                     return ri.hasNext();
                 }
 
                 public String next()
                 {
                     // Note, we are expecting OO Roles, and this should get the name
                     return ri.next().getAuthority();
                 }
 
                 public void remove()
                 {
                     //noop
                 }
             }, ",");
     }
 
     /**
      * This method is for situations where code is not running in the context of an HTTP request and therefore standard acegi procedures don't apply.
      * It set's up a throwaway user with sufficient rights to use UserDao, looks up the user and populates ThreadLocal's SecurityContext.
      * 
      * Should always be called in a block with a consecutive finally block calling {@link SecurityContextHolder#clearContext()}
      * 
      * @param userDao
      * @param userName
      * @throws Exception
      */
     public static void setupAuthenticationForNamedUser(UserDao userDao, String userName) throws Exception
     {
         UserDetails realUser = null;
         try
         {
             // setup throwaway user
             User tempAdmin = new User();
             tempAdmin.setUsername("throwawayAdmin");
             tempAdmin.addRole(new Role("ROLE_ADMIN", "Administrator Role"));
             tempAdmin.addRole(new Role("ROLE_EDITOR", "Editor Role"));
 
             Authentication authentication = new UsernamePasswordAuthenticationToken(tempAdmin, null, tempAdmin.getAuthorities());
             SecurityContextHolder.getContext().setAuthentication(authentication);
 
             realUser = userDao.loadUserByUsername(userName);
         }
         catch (UsernameNotFoundException e)
         {
             throw new OtherObjectsException("There is no user with user name: '" + userName + "'");
         }
         finally
         {
             SecurityContextHolder.clearContext();
         }
 
         Authentication authentication = new UsernamePasswordAuthenticationToken(realUser, null, realUser.getAuthorities());
         SecurityContextHolder.getContext().setAuthentication(authentication);
 
     }
     
     /**
      * Returns the authorities of the current user.
      *
      * @return an array containing the current user's authorities (or an empty array if not authenticated), never null.
      */
     private static Collection<GrantedAuthority> getUserAuthorities() {
         Authentication auth = SecurityContextHolder.getContext().getAuthentication();
 
         if (auth == null || auth.getAuthorities() == null) {
             return NO_AUTHORITIES;
         }
 
         return auth.getAuthorities();
     }
     
     /**
      * Returns true if the current user has the specified authority.
      *
      * @param authority the authority to test for (e.g. "ROLE_A").
      * @return true if a GrantedAuthority object with the same string representation as the supplied authority
      * name exists in the current user's list of authorities. False otherwise, or if the user in not authenticated.
      */
     public static boolean userHasAuthority(String authority) {
         List<GrantedAuthority> authorities = (List<GrantedAuthority>) getUserAuthorities();
 
         for (GrantedAuthority grantedAuthority : authorities) {
             if (authority.equals(grantedAuthority.getAuthority())) {
                 return true;
             }
         }
 
         return false;
     }
 }
