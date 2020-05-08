 /*
  * Copyright (c) 2000-2003 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse products derived from The Software without without written consent of Netspective. "Netspective",
  *    "Axiom", "Commons", "Junxion", and "Sparx" may not appear in the names of products derived from The Software
  *    without written consent of Netspective.
  *
  * 5. Please attribute functionality where possible. We suggest using the "powered by Netspective" button or creating
  *    a "powered by Netspective(tm)" link to http://www.netspective.com for each application using The Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF HE HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  *
  * @author Shahid N. Shah
  */
 
 /**
 * $Id: BasicAuthenticatedUser.java,v 1.4 2003-03-20 20:42:04 shahbaz.javeed Exp $
  */
 
 package com.netspective.commons.security;
 
 import java.util.ArrayList;
 import java.util.BitSet;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.List;
 
 import com.netspective.commons.acl.Permission;
 import com.netspective.commons.acl.PermissionNotFoundException;
 import com.netspective.commons.acl.AccessControlListsManager;
 
 public class BasicAuthenticatedUser implements AuthenticatedUser
 {
     private String userName;
     private String userId;
     private String[] userRoles;
     private BitSet userPermissions;
     private String userOrgName;
     private String userOrgId;
     private Map attributes = new HashMap();
 
     public BasicAuthenticatedUser()
     {
     }
 
     public BasicAuthenticatedUser(String name, String id)
     {
         userName = name;
         userId = id;
     }
 
     public BasicAuthenticatedUser(String name, String id, String orgName, String orgId)
     {
         this(name, id);
         userOrgName = orgName;
         userOrgId = orgId;
     }
 
     public String getUserName()
     {
         return userName;
     }
 
     public void setUserName(String userName)
     {
         this.userName = userName;
     }
 
     public String getName() // implementation for java.security.Principal
     {
         return userId;
     }
 
     public String getUserId()
     {
         return userId;
     }
 
     public void setUserId(String userId)
     {
         this.userId = userId;
     }
 
     public String getUserOrgName()
     {
         return userOrgName;
     }
 
     public void setUserOrgName(String userOrgName)
     {
         this.userOrgName = userOrgName;
     }
 
     public String getUserOrgId()
     {
         return userOrgId;
     }
 
     public void setUserOrgId(String userOrgId)
     {
         this.userOrgId = userOrgId;
     }
 
     public BitSet getUserPermissions()
     {
         return userPermissions;
     }
 
     public String[] getUserRoles()
     {
         return userRoles;
     }
 
     public void setRoles(AccessControlListsManager aclsManager, String[] roles) throws PermissionNotFoundException
     {
         userRoles = roles;
         if(userRoles == null)
             return;
 
         if(userPermissions == null)
            userPermissions = new BitSet(aclsManager.getAccessControlLists().getHighestPermissionId());
 
         for(int i = 0; i < userRoles.length; i++)
         {
             String roleName = roles[i];
             Permission role = aclsManager.getPermission(roleName);
             if(role == null)
                 throw new RuntimeException("Role '" + roleName + "' does not exist in ACL.");
             userPermissions.or(role.getChildPermissions());
         }
     }
 
     //TODO: public void addRoles(AccessControlListsManager aclsManager, String[] roles) - to add roles
 
     public void removeRoles(AccessControlListsManager aclsManager, String[] roles) throws PermissionNotFoundException
     {
         if(userRoles == null || userPermissions == null)
             return;
 
         // Check to make sure all roles are valid ...
         for(int i = 0; i < roles.length; i++)
         {
             Permission role = aclsManager.getPermission(roles[i]);
             if(role == null)
                 throw new RuntimeException("Role '" + roles[i] + "' does not exist in ACL.");
         }
 
 	    // Clear all permissions until the shakeup is complete...
	    userPermissions.clear();
 
         if(roles == userRoles)
         {
             // if we're removing all the current user roles, it's a special case
             // because we're probably coming from the removeAllRoles method
             userRoles = null;
         }
         else
         {
             // loop through the current user roles and track the ones we're keeping
             // so that we can hang on to them in userRoles
             List keepRoles = new ArrayList();
             for(int i = 0; i < userRoles.length; i++)
             {
                 String checkRole = userRoles[i];
                 boolean removingRole = false;
                 for(int j = 0; j < roles.length; j++)
                 {
                     if(checkRole.equals(roles[j]))
                     {
                         removingRole = true;
                         break;
                     }
                 }
                 if(!removingRole)
                     keepRoles.add(checkRole);
             }
 
             if(keepRoles.size() > 0)
                 userRoles = (String[]) keepRoles.toArray(new String[keepRoles.size()]);
             else
                 userRoles = null;
 
 			// Recalculate all the permissions for the roles left after the shakeup
 			for (int i = 0; i < userRoles.length; i ++)
 			{
 				Permission role = aclsManager.getPermission(userRoles[i]);
 				userPermissions.or(role.getChildPermissions());
 			}
         }
     }
 
     public void removeAllRoles(AccessControlListsManager aclsManager) throws PermissionNotFoundException
     {
         if(userRoles != null)
             removeRoles(aclsManager, userRoles);
     }
 
     public boolean hasPermission(AccessControlListsManager aclsManager, String permissionName) throws PermissionNotFoundException
     {
         Permission perm = aclsManager.getPermission(permissionName);
         if(perm == null)
             throw new RuntimeException("Permission '" + permissionName + "' does not exist in ACL.");
         return userPermissions.get(perm.getId());
     }
 
     public boolean hasAnyPermission(AccessControlListsManager aclsManager, String[] permissionNames) throws PermissionNotFoundException
     {
         for(int i = 0; i < permissionNames.length; i++)
         {
             if(hasPermission(aclsManager, permissionNames[i]))
                 return true;
         }
         return false;
     }
 
     public Object getAttribute(String attrName)
     {
         return attributes.get(attrName);
     }
 
     public void setAttribute(String attrName, Object attrValue)
     {
         attributes.put(attrName, attrValue);
     }
 
     public void removeAttribute(String attrName)
     {
         attributes.remove(attrName);
     }
 }
