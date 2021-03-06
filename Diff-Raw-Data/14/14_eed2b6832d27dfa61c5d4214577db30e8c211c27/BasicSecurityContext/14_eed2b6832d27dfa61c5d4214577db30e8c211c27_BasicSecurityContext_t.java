 /**
  * Copyright (c) 2010, Sebastian Sdorra
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  * 1. Redistributions of source code must retain the above copyright notice,
  *    this list of conditions and the following disclaimer.
  * 2. Redistributions in binary form must reproduce the above copyright notice,
  *    this list of conditions and the following disclaimer in the documentation
  *    and/or other materials provided with the distribution.
  * 3. Neither the name of SCM-Manager; nor the names of its
  *    contributors may be used to endorse or promote products derived from this
  *    software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  *
  * http://bitbucket.org/sdorra/scm-manager
  *
  */
 
 
 
 package sonia.scm.web.security;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import com.google.common.collect.Sets;
 import com.google.inject.Inject;
 import com.google.inject.servlet.SessionScoped;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import sonia.scm.config.ScmConfiguration;
 import sonia.scm.group.Group;
 import sonia.scm.group.GroupManager;
 import sonia.scm.security.CipherUtil;
 import sonia.scm.user.User;
 import sonia.scm.user.UserException;
 import sonia.scm.user.UserManager;
 import sonia.scm.util.Util;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.io.IOException;
 
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.servlet.http.HttpSession;
 
 /**
  *
  * @author Sebastian Sdorra
  */
 @SessionScoped
 public class BasicSecurityContext implements WebSecurityContext
 {
 
   /** Field description */
   public static final String SCM_CREDENTIALS = "SCM_CREDENTIALS";
 
   /** Field description */
   public static final String USER_ANONYMOUS = "anonymous";
 
   /** the logger for BasicSecurityContext */
   private static final Logger logger =
     LoggerFactory.getLogger(BasicSecurityContext.class);
 
   //~--- constructors ---------------------------------------------------------
 
   /**
    * Constructs ...
    *
    *
    *
    * @param configuration
    * @param authenticator
    * @param groupManager
    * @param userManager
    */
   @Inject
   public BasicSecurityContext(ScmConfiguration configuration,
                               AuthenticationManager authenticator,
                               GroupManager groupManager,
                               UserManager userManager)
   {
     this.configuration = configuration;
     this.authenticator = authenticator;
     this.groupManager = groupManager;
     this.userManager = userManager;
   }
 
   //~--- methods --------------------------------------------------------------
 
   /**
    * Method description
    *
    *
    * @param request
    * @param response
    * @param username
    * @param password
    *
    * @return
    */
   @Override
   public User authenticate(HttpServletRequest request,
                            HttpServletResponse response, String username,
                            String password)
   {
     AuthenticationResult ar = authenticator.authenticate(request, response,
                                 username, password);
 
     if ((ar != null) && (ar.getState() == AuthenticationState.SUCCESS))
     {
       authenticate(request, password, ar);
     }
 
     return user;
   }
 
   /**
    * Method description
    *
    *
    * @param request
    * @param response
    */
   @Override
   public void logout(HttpServletRequest request, HttpServletResponse response)
   {
     user = null;
     groups = new HashSet<String>();
 
     HttpSession session = request.getSession(false);
 
     if (session != null)
     {
       session.invalidate();
     }
   }
 
   //~--- get methods ----------------------------------------------------------
 
   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public Collection<String> getGroups()
   {
     if (groups == null)
     {
       groups = new HashSet<String>();
     }
 
     return groups;
   }
 
   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public User getUser()
   {
     if ((user == null) && configuration.isAnonymousAccessEnabled())
     {
       user = userManager.get(USER_ANONYMOUS);
     }
 
     return user;
   }
 
   /**
    * Method description
    *
    *
    * @return
    */
   @Override
   public boolean isAuthenticated()
   {
     return getUser() != null;
   }
 
   //~--- methods --------------------------------------------------------------
 
   /**
    * Method description
    *
    *
    * @param request
    * @param password
    * @param ar
    */
   private void authenticate(HttpServletRequest request, String password,
                             AuthenticationResult ar)
   {
     user = ar.getUser();
 
     try
     {
       Set<String> groupSet = createGroupSet(ar);
 
       // check for admin user
       checkForAuthenticatedAdmin(user, groupSet);
 
       // store user
       User dbUser = userManager.get(user.getName());
 
       if (dbUser != null)
       {
         checkDBForAdmin(user, dbUser);
         checkDBForActive(user, dbUser);
       }
 
       // create new user
       else
       {
         userManager.create(user);
       }
 
       if (user.isActive())
       {
         groups = groupSet;
 
         if (logger.isDebugEnabled())
         {
           logGroups();
         }
 
         // store encrypted credentials in session
         String credentials = user.getName();
 
         if (Util.isNotEmpty(password))
         {
           credentials = credentials.concat(":").concat(password);
         }
 
         credentials = CipherUtil.getInstance().encode(credentials);
         request.getSession(true).setAttribute(SCM_CREDENTIALS, credentials);
       }
       else
       {
         if (logger.isWarnEnabled())
         {
           logger.warn("user {} is deactivated", user.getName());
         }
 
         user = null;
         groups = null;
       }
     }
     catch (Exception ex)
     {
       user = null;
 
       if (groups != null)
       {
         groups.clear();
       }
 
       logger.error("authentication failed", ex);
     }
   }
 
   /**
    * Method description
    *
    *
    * @param user
    * @param dbUser
    */
   private void checkDBForActive(User user, User dbUser)
   {
 
     // user is deactivated by database
     if (!dbUser.isActive())
     {
       if (logger.isDebugEnabled())
       {
        logger.debug("user {} is marked as deactivated by local database",
                     user.getName());
       }
 
       user.setActive(false);
     }
   }
 
   /**
    * Method description
    *
    *
    * @param user
    * @param dbUser
    *
    * @throws IOException
    * @throws UserException
    */
   private void checkDBForAdmin(User user, User dbUser)
           throws UserException, IOException
   {
 
     // if database user is an admin, set admin for the current user
     if (dbUser.isAdmin())
     {
       if (logger.isDebugEnabled())
       {
        logger.debug("user {} of type {} is marked as admin by local database",
                     user.getName(), user.getType());
       }
 
       user.setAdmin(true);
     }
 
     // modify existing user, copy properties except password and admin
     if (user.copyProperties(dbUser, false))
     {
       userManager.modify(dbUser);
     }
   }
 
   /**
    * Method description
    *
    *
    * @param user
    * @param groupSet
    */
   private void checkForAuthenticatedAdmin(User user, Set<String> groupSet)
   {
     if (!user.isAdmin())
     {
       user.setAdmin(isAdmin(groupSet));
 
       if (logger.isDebugEnabled() && user.isAdmin())
       {
         logger.debug("user {} is marked as admin by configuration",
                      user.getName());
       }
     }
     else if (logger.isDebugEnabled())
     {
      logger.debug("authenticator {} marked user {} as admin", user.getType(),
                   user.getName());
     }
   }
 
   /**
    * Method description
    *
    *
    * @param ar
    *
    * @return
    */
   private Set<String> createGroupSet(AuthenticationResult ar)
   {
     Set<String> groupSet = Sets.newHashSet();
 
     // load external groups
     Collection<String> extGroups = ar.getGroups();
 
     if (extGroups != null)
     {
       groupSet.addAll(extGroups);
     }
 
     // load internal groups
     loadGroups(groupSet);
 
     return groupSet;
   }
 
   /**
    * Method description
    *
    *
    * @param groupSet
    */
   private void loadGroups(Set<String> groupSet)
   {
     Collection<Group> groupCollection =
       groupManager.getGroupsForMember(user.getName());
 
     if (groupCollection != null)
     {
       for (Group group : groupCollection)
       {
         groupSet.add(group.getName());
       }
     }
   }
 
   /**
    * Method description
    *
    */
   private void logGroups()
   {
     StringBuilder msg = new StringBuilder("user ");
 
     msg.append(user.getName());
 
     if (Util.isNotEmpty(groups))
     {
       msg.append(" is member of ");
 
       Iterator<String> groupIt = groups.iterator();
 
       while (groupIt.hasNext())
       {
         msg.append(groupIt.next());
 
         if (groupIt.hasNext())
         {
           msg.append(", ");
         }
       }
     }
     else
     {
       msg.append(" is not a member of a group");
     }
 
     logger.debug(msg.toString());
   }
 
   //~--- get methods ----------------------------------------------------------
 
   /**
    * Method description
    *
    *
    *
    * @param groups
    * @return
    */
   private boolean isAdmin(Collection<String> groups)
   {
     boolean result = false;
     Set<String> adminUsers = configuration.getAdminUsers();
 
     if (adminUsers != null)
     {
       result = adminUsers.contains(user.getName());
     }
 
     if (!result)
     {
       Set<String> adminGroups = configuration.getAdminGroups();
 
       if (adminGroups != null)
       {
         result = Util.containsOne(adminGroups, groups);
       }
     }
 
     return result;
   }
 
   //~--- fields ---------------------------------------------------------------
 
   /** Field description */
   private AuthenticationManager authenticator;
 
   /** Field description */
   private ScmConfiguration configuration;
 
   /** Field description */
   private GroupManager groupManager;
 
   /** Field description */
   private Set<String> groups = new HashSet<String>();
 
   /** Field description */
   private User user;
 
   /** Field description */
   private UserManager userManager;
 }
