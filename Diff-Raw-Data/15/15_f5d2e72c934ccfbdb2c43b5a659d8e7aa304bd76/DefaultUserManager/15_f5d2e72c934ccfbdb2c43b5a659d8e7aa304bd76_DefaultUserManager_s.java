 package com.atlassian.sal.confluence.user;
 
 import java.security.Principal;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.atlassian.confluence.security.Permission;
 import com.atlassian.confluence.security.PermissionManager;
 import com.atlassian.confluence.user.AuthenticatedUserThreadLocal;
 import com.atlassian.confluence.user.UserAccessor;
 import com.atlassian.sal.api.user.UserManager;
 import com.atlassian.sal.api.user.UserResolutionException;
 import com.atlassian.user.User;
 
 /** Authenticates a user against UserAccessor in Confluence. */
 public class DefaultUserManager implements UserManager
 {
     private final UserAccessor userAccessor;
     private final PermissionManager permissionManager;
 
     public DefaultUserManager(final UserAccessor userAccessor, final PermissionManager permissionManager)
     {
         this.userAccessor = userAccessor;
         this.permissionManager = permissionManager;
     }
 
     public String getRemoteUsername()
     {
         final User user = AuthenticatedUserThreadLocal.getUser();
         if (user != null)
         {
             return user.getName();
         }
         return null;
     }
 
     public boolean isSystemAdmin(final String username)
     {
         final User user = userAccessor.getUser(username);
         return user != null &&
             ( permissionManager.hasPermission(user, Permission.ADMINISTER, PermissionManager.TARGET_SYSTEM)
               || userAccessor.isSuperUser(user));
     }
 
     public boolean authenticate(final String username, final String password)
     {
         final User user = userAccessor.getUser(username);
         return user != null && userAccessor.authenticate(user.getName(), password);
     }
 
     /**
      * Returns whether the user is in the specify group
      *
      * @param username The username to check
      * @param group The group to check
      * @return True if the user is in the specified group
      */
     public boolean isUserInGroup(final String username, final String group)
     {
         return userAccessor.hasMembership(group, username);
     }
 
     public String getRemoteUsername(final HttpServletRequest request)
     {
         // TODO Implement SAL-16
         return getRemoteUsername();
     }
 
    public Principal resolve(final String username) throws UserResolutionException
     {
        if (userAccessor.getUser(username) == null)
        {
            return null;
        }
        return new Principal()
        {
            public String getName()
            {
                return username;
            }
        };
     }
 }
