 package com.atlassian.refapp.auth.internal;
 
 import java.security.Principal;
 import java.util.Collection;
import java.util.Collections;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.atlassian.seraph.auth.Authenticator;
 import com.atlassian.seraph.auth.RoleMapper;
 import com.atlassian.seraph.config.SecurityConfig;
 import com.atlassian.user.EntityException;
 import com.atlassian.user.Group;
 import com.atlassian.user.GroupManager;
 import com.atlassian.user.User;
 import com.atlassian.user.search.page.Pager;
 
 
 public class GroupRoleMapper implements RoleMapper
 {
     private final Authenticator authenticator;
     private final GroupManager groupManager;
 
     public GroupRoleMapper(Authenticator authenticator, GroupManager groupManager)
     {
         this.authenticator = authenticator;
         this.groupManager = groupManager;
     }
     
     public void init(Map params, SecurityConfig config)
     {
     }
 
     /**
      * Assume that roles == groups.
      */
     public boolean hasRole(Principal user, HttpServletRequest request, String role)
     {
         Collection<String> groups = getGroups(request);
 
         if (groups == null && role == null)
         {
             return true;
         }
         else if (groups == null)
         {
             return false;
         }
         else
         {
             return groups.contains(role);
         }
     }
 
     public boolean canLogin(Principal user, HttpServletRequest request)
     {
         return user != null;
     }
     
     private Collection<String> getGroups(HttpServletRequest request)
     {
         User user = (User) authenticator.getUser(request);
         if (user == null)
         {
             return Collections.emptyList();
         }
         try
         {
             Pager groupPager = groupManager.getGroups(user);
             List<String> groups = new LinkedList<String>();
             for (Iterator<Group> i = groupPager.iterator(); i.hasNext(); )
             {
                 groups.add(i.next().getName());
             }
             return groups;
         }
         catch (EntityException e)
         {
             return Collections.emptyList();
         }
     }
 }
