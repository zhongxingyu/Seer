 package org.otherobjects.cms.security;
 
 import org.otherobjects.cms.OtherObjectsException;
 import org.otherobjects.cms.dao.UserDao;
 import org.otherobjects.cms.model.Role;
 import org.otherobjects.cms.model.User;
 import org.springframework.security.Authentication;
 import org.springframework.security.context.SecurityContextHolder;
 import org.springframework.security.providers.UsernamePasswordAuthenticationToken;
 import org.springframework.security.userdetails.UserDetails;
 
 /**
  * A tool to provide static convenience methods to work with Spring security's {@link SecurityContextHolder}
  * 
  * @author joerg
  */
 public class SecurityTool
 {
 
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
     public static boolean isCurrentUser(String id)
     {
         Long currentUserId = getUserId();
         if (id == null || currentUserId == null)
             return false;
 
         return id.equals(currentUserId.toString());
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
            else if (user instanceof String && ((String) user).equals("roleAnonymous"))
                // Anonymous user detected
                return null;
            else
                throw new OtherObjectsException("Current principal is not of type org.otherobjects.cms.model.User: " + user.toString());
         }
         else
             return null;
     }
 
     /**
      * This method is for situations where code is not running in the context of an HTTP request and therefore standard acegi procedures don't apply.
      * It set's up a throwaway user with sufficient rights to use UserDao, looks up the user and populates ThreadLocal's SecurityContext.
      * 
      * Should always be called in a block with a consecutive finally block calling {@link SecurityContextHolder#clearContext()}
      * 
      * @param userDao
      * @param userId
      * @throws Exception
      */
     public static void setupAuthenticationForNamedUser(UserDao userDao, String userId) throws Exception
     {
         UserDetails realUser = null;
         try
         {
             // setup throwaway user
             User tempAdmin = new User();
             tempAdmin.setUsername("throwawayAdmin");
             tempAdmin.addRole(new Role("ROLE_ADMIN", "Administrator Role"));
             tempAdmin.addRole(new Role("ROLE_USER", "User Role"));
 
             Authentication authentication = new UsernamePasswordAuthenticationToken(tempAdmin, null, tempAdmin.getAuthorities());
             SecurityContextHolder.getContext().setAuthentication(authentication);
 
             realUser = userDao.get(Long.parseLong(userId));
         }
         finally
         {
             SecurityContextHolder.clearContext();
         }
 
         if (realUser == null)
             throw new OtherObjectsException("There is no user with the userId: '" + userId + "'");
 
         Authentication authentication = new UsernamePasswordAuthenticationToken(realUser, null, realUser.getAuthorities());
         SecurityContextHolder.getContext().setAuthentication(authentication);
 
     }
 }
