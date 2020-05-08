 /*
  *
  * @version: $Id$
  */
 package org.gridlab.gridsphere.services.core.security.acl.impl;
 
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerException;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerFactory;
 import org.gridlab.gridsphere.core.persistence.PersistenceManagerRdbms;
 import org.gridlab.gridsphere.portlet.*;
 import org.gridlab.gridsphere.portlet.impl.SportletGroup;
 import org.gridlab.gridsphere.portlet.impl.SportletLog;
 import org.gridlab.gridsphere.portlet.impl.SportletRoleInfo;
 import org.gridlab.gridsphere.portlet.impl.SportletProperties;
 import org.gridlab.gridsphere.portlet.service.PortletServiceUnavailableException;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceConfig;
 import org.gridlab.gridsphere.portlet.service.spi.PortletServiceProvider;
 import org.gridlab.gridsphere.services.core.security.acl.AccessControlManagerService;
 import org.gridlab.gridsphere.services.core.security.acl.GroupEntry;
 import org.gridlab.gridsphere.services.core.security.acl.GroupRequest;
 import org.gridlab.gridsphere.services.core.user.impl.UserManagerServiceImpl;
 
 import java.util.*;
 
 public class AccessControlManagerServiceImpl implements PortletServiceProvider, AccessControlManagerService {
 
     private static PortletLog log = SportletLog.getInstance(AccessControlManagerServiceImpl.class);
     private static AccessControlManagerServiceImpl instance = new AccessControlManagerServiceImpl();
     private static UserManagerServiceImpl userManager = UserManagerServiceImpl.getInstance();
 
     private static PersistenceManagerRdbms pm = null;
 
     private String jdoGroupRequest = GroupRequestImpl.class.getName();
     private String jdoPortletGroup = SportletGroup.class.getName();
 
     public AccessControlManagerServiceImpl() {
 
     }
 
     public static synchronized AccessControlManagerServiceImpl getInstance() {
         pm = PersistenceManagerFactory.createGridSphereRdbms();
         return instance;
     }
 
     public void init(PortletServiceConfig config) throws PortletServiceUnavailableException {
 
     }
 
     public void destroy() {
         log.info("Calling destroy()");
     }
 
     public GroupRequest createGroupEntry() {
         GroupRequest request = new GroupRequestImpl();
         this.saveGroupEntry(request);
         return request;
     }
 
     public GroupRequest editGroupEntry(GroupEntry groupEntry) {
         GroupRequest request = (GroupRequest) this.getGroupEntry(groupEntry.getID());
         return request;
     }
 
     public List getGroupEntries() {
         String criteria = "";
         return selectGroupEntries(criteria);
     }
 
     public List getGroupEntries(User user) {
         String criteria = "where groupRequest.sportletUser.oid='" + user.getID() + "'";
         return selectGroupEntries(criteria);
     }
 
     public List getGroupEntries(PortletGroup group) {
         String criteria = "where groupRequest.sportletGroup.oid='" + group.getID() + "'";
         return selectGroupEntries(criteria);
     }
 
     private List selectGroupEntries(String criteria) {
         String oql = "select groupRequest from "
                 + jdoGroupRequest
                 + " groupRequest "
                 + criteria;
         try {
             return pm.restoreList(oql);
         } catch (PersistenceManagerException e) {
             String msg = "Error retrieving access right";
             log.error(msg, e);
             return new Vector();
         }
     }
 
     public GroupEntry getGroupEntry(String id) {
         String criteria = "where groupRequest.oid='" + id + "'";
         return selectGroupRequestImpl(criteria);
     }
 
     public GroupEntry getGroupEntry(User user, PortletGroup group) {
         return getGroupRequestImpl(user, group);
     }
 
     private GroupRequestImpl getGroupRequestImpl(User user, PortletGroup group) {
         String criteria = " where groupRequest.sportletUser.oid='" + user.getID() + "'"
                 + " and groupRequest.sportletGroup.oid='" + group.getID() + "'";
         return selectGroupRequestImpl(criteria);
     }
 
     private GroupRequestImpl selectGroupRequestImpl(String criteria) {
         String oql = "select groupRequest from "
                 + jdoGroupRequest
                 + " groupRequest "
                 + criteria;
         try {
             return (GroupRequestImpl) pm.restore(oql);
         } catch (PersistenceManagerException e) {
             String msg = "Error retrieving access right";
             log.error(msg, e);
             return null;
         }
     }
 
     private boolean existsGroupEntry(GroupEntry entry) {
         GroupRequestImpl rightImpl = (GroupRequestImpl) entry;
         String oql = "select groupRequest.oid from "
                 + jdoGroupRequest
                 + " groupRequest where groupRequest.oid='"
                 + rightImpl.getOid() + "'";
         try {
             return (pm.restore(oql) != null);
         } catch (PersistenceManagerException e) {
             String msg = "Error retrieving access right";
             log.error(msg, e);
         }
         return false;
     }
 
     public void saveGroupEntry(GroupEntry entry) {
         // Create or update access right
         if (existsGroupEntry(entry)) {
             try {
                 pm.update(entry);
             } catch (PersistenceManagerException e) {
                 String msg = "Error creating access right";
                 log.error(msg, e);
             }
         } else {
             try {
                 pm.create(entry);
             } catch (PersistenceManagerException e) {
                 String msg = "Error creating access right";
                 log.error(msg, e);
             }
         }
     }
 
     public void deleteGroupEntry(GroupEntry entry) {
         try {
             pm.delete(entry);
         } catch (PersistenceManagerException e) {
             String msg = "Error deleting access right";
             log.error(msg, e);
         }
     }
 
     public void deleteGroupEntries(User user) {
         Iterator groupEntries = getGroupEntries(user).iterator();
         while (groupEntries.hasNext()) {
             GroupEntry groupEntry = (GroupEntry) groupEntries.next();
             deleteGroupEntry(groupEntry);
         }
     }
 
     public List getGroups() {
         return selectGroups("");
     }
 
 
     public List selectGroups(String criteria) {
         // Build object query
         StringBuffer oqlBuffer = new StringBuffer();
         oqlBuffer.append("select grp from ");
         oqlBuffer.append(jdoPortletGroup);
         oqlBuffer.append(" grp ");
         oqlBuffer.append(criteria);
         oqlBuffer.append("'");
         // Generate object query
         String oql = oqlBuffer.toString();
         // Execute query
         try {
             return pm.restoreList(oql);
         } catch (PersistenceManagerException e) {
             String msg = "Error retrieving portlet groups";
             log.error(msg, e);
             return new Vector();
         }
     }
 
     public PortletGroup getGroupByName(String name) {
         return selectSportletGroup("where grp.Name='" + name + "'");
     }
 
     public PortletGroup getGroup(String id) {
         return selectSportletGroup("where grp.oid='" + id + "'");
     }
 
     public PortletGroup getCoreGroup() {
        return selectSportletGroup("where grp.Core='" + true + "'");
     }
 
     private SportletGroup selectSportletGroup(String criteria) {
         // Build object query
         StringBuffer oqlBuffer = new StringBuffer();
         oqlBuffer.append("select grp from ");
         oqlBuffer.append(jdoPortletGroup);
         oqlBuffer.append(" grp ");
         oqlBuffer.append(criteria);
         // Generate object query
         String oql = oqlBuffer.toString();
         log.debug(oql);
         try {
             return (SportletGroup) pm.restore(oql);
         } catch (PersistenceManagerException e) {
             String msg = "Error retrieving portlet group";
             log.error(msg, e);
             return null;
         }
     }
 
     public boolean existsGroupWithName(String groupName) {
         return (getGroupByName(groupName) != null);
     }
 
     public PortletGroup createGroup(SportletGroup portletGroup) {
         Iterator it = portletGroup.getPortletRoleList().iterator();
         while (it.hasNext()) {
             SportletRoleInfo info = (SportletRoleInfo) it.next();
             try {
                 if (info.getOid() == null) {
                     pm.create(info);
                 } else {
                     pm.update(info);
                 }
             } catch (PersistenceManagerException e) {
                 log.error("Error creating SportletRoleInfo " + info.getRole(), e);
             }
         }
         portletGroup.setPortletRoleList(portletGroup.getPortletRoleList());
         try {
             if (portletGroup.getOid() == null) {
                 pm.create(portletGroup);
             } else {
                 pm.update(portletGroup);
             }
         } catch (PersistenceManagerException e) {
             String msg = "Error creating portlet group " + portletGroup.getName();
             log.error(msg, e);
         }
         return portletGroup;
     }
 
 
     public void deleteGroup(PortletGroup group) {
         try {
             pm.delete(group);
         } catch (PersistenceManagerException e) {
             String msg = "Error deleting portlet group";
             log.error(msg, e);
         }
     }
 
     public List getUsers(PortletGroup group) {
         String oql = "select groupRequest.sportletUser from "
                 + jdoGroupRequest
                 + " groupRequest where groupRequest.sportletGroup.oid='"
                 + group.getID()
                 + "'";
         try {
             return pm.restoreList(oql);
         } catch (PersistenceManagerException e) {
             String msg = "Error retrieving access right";
             log.error(msg, e);
             return new Vector();
         }
     }
 
     public List getUsers(PortletGroup group, PortletRole role) {
         List users = this.getUsers(group);
         Iterator it = users.iterator();
         List l = new Vector();
         while (it.hasNext()) {
             User u = (User) it.next();
             //System.err.println("Checking if " + u.getFullName() + " has " + role);
             if (this.hasRoleInGroup(u, group, role)) {
                 //System.err.println("user has role in group" + u.getFullName() + " " + u.getEmailAddress());
                 l.add(u);
             }
         }
         return l;
     }
 
 
     public List getUsersNotInGroup(PortletGroup group) {
         List usersNotInGroup = new Vector();
         Iterator allUsers = userManager.getUsers().iterator();
         while (allUsers.hasNext()) {
             User user = (User) allUsers.next();
             // If user has super role, don't include
             //   if (hasSuperRole(user)) {
             //     continue;
             //  }
             // Else, if user not in group, then include
             if (!isUserInGroup(user, group)) {
                 usersNotInGroup.add(user);
             }
         }
         return usersNotInGroup;
     }
 
     public boolean isUserInGroup(User user, PortletGroup group) {
         return (getGroupEntry(user, group) != null);
     }
 
     public List getGroups(User user) {
         List groups = null;
         // If user has super role
         //if (hasSuperRole(user)) {
         //    groups = getGroups();
         //} else {
         // Otherwise, return groups for given user
         String oql = "select groupRequest.sportletGroup from "
                 + jdoGroupRequest
                 + " groupRequest where groupRequest.sportletUser.oid='"
                 + user.getID()
                 + "'";
         try {
             groups = pm.restoreList(oql);
         } catch (PersistenceManagerException e) {
             String msg = "Error retrieving access right";
             log.error(msg, e);
             return new Vector();
         }
         // }
         return groups;
     }
 
     public List getGroupsNotMemberOf(User user) {
         List groupsNotMemberOf = new Vector();
         if (!hasSuperRole(user)) {
             Iterator allGroups = getGroups(user).iterator();
             while (allGroups.hasNext()) {
                 PortletGroup group = (PortletGroup) allGroups.next();
                 if (!isUserInGroup(user, group)) {
                     groupsNotMemberOf.add(user);
                 }
             }
         }
         return groupsNotMemberOf;
     }
 
     public PortletRole getRoleInGroup(User user, PortletGroup group) {
         //if (hasSuperRole(user)) {
         //    return PortletRole.SUPER;
         //} else {
         GroupEntry entry = getGroupEntry(user, group);
         if (entry == null) {
             return PortletRole.GUEST;
         }
         return entry.getRole();
         //}
     }
 
     public boolean hasRequiredRole(PortletRequest req, String portletClass, boolean checkAdmin) {
         Map userGroups = (Map)req.getAttribute(SportletProperties.PORTLETGROUPS);
 
         boolean found = false;
 
         //Iterator it = aclService.getGroups().iterator();
 
         Iterator it = userGroups.keySet().iterator();
         while (it.hasNext()) {
             PortletGroup group = (PortletGroup)it.next();
             Set roleList = group.getPortletRoleList();
             Iterator roleIt = roleList.iterator();
             //System.err.println("group= " + group.getName());
             while (roleIt.hasNext()) {
                 SportletRoleInfo roleInfo = (SportletRoleInfo)roleIt.next();
                 //System.err.println("class= " + roleInfo.getPortletClass());
                 if (roleInfo.getPortletClass().equals(portletClass)) {
                     // check if user has this group
                     found = true;
                     //if (userGroups.containsKey(group)) {
                         //System.err.println("group= " + group.getName());
                         PortletRole usersRole = (PortletRole)userGroups.get(group);
                         //System.err.println("usersRole= " + usersRole);
                         PortletRole reqRole = PortletRole.toPortletRole(roleInfo.getRole());
                         //System.err.println("reqRole= " + reqRole);
                         if (usersRole.compare(usersRole, reqRole) >= 0) {
                             if (checkAdmin) {
                                 if (usersRole.compare(usersRole, PortletRole.ADMIN) >= 0) {
                                     return true;
                                 }
                             } else {
                                 return true;
                             }
                         }
                     //}
                 }
             }
         }
         return (found == true) ? false : true;
     }
 
     public boolean hasRequiredRole(User user, String portletID, boolean checkAdmin) {
         // first check to see if portletID is in groups
 
         List userGroups = this.getGroups(user);
         boolean found = false;
         Iterator it = this.getGroups().iterator();
         while (it.hasNext()) {
             PortletGroup group = (PortletGroup)it.next();
             Set roleList = group.getPortletRoleList();
             Iterator roleIt = roleList.iterator();
             //System.err.println("group= " + group.getName());
             while (roleIt.hasNext()) {
                 SportletRoleInfo roleInfo = (SportletRoleInfo)roleIt.next();
                 //System.err.println("class= " + roleInfo.getPortletClass());
                 if (roleInfo.getPortletClass().equals(portletID)) {
                     // check if user has this group
                     found = true;
                     if (userGroups.contains(group)) {
                         //System.err.println("group= " + group.getName());
                         PortletRole usersRole = this.getRoleInGroup(user, group);
                         //System.err.println("usersRole= " + usersRole);
                         PortletRole reqRole = PortletRole.toPortletRole(roleInfo.getRole());
                         //System.err.println("reqRole= " + reqRole);
                         if (usersRole.compare(usersRole, reqRole) >= 0) {
                             if (checkAdmin) {
                                 if (usersRole.compare(usersRole, PortletRole.ADMIN) >= 0) {
                                     return true;
                                 } 
                             } else {
                                 return true;
                             }
                         }
                     }
                 }
             }
         }
         return (found == true) ? false : true;
     }
 
     public void addGroupEntry(User user, PortletGroup group, PortletRole role) {
         GroupRequestImpl right = getGroupRequestImpl(user, group);
         if (right != null) {
             deleteGroupEntry(right);
         }
         right = new GroupRequestImpl();
         right.setUser(user);
         right.setGroup(group);
         right.setRole(role);
         saveGroupEntry(right);
     }
 
     public boolean hasRoleInGroup(User user, PortletGroup group, PortletRole role) {
         PortletRole test = getRoleInGroup(user, group);
         return test.equals(role);
     }
 
     public boolean hasAdminRoleInGroup(User user, PortletGroup group) {
         return hasRoleInGroup(user, group, PortletRole.ADMIN);
     }
 
     public boolean hasUserRoleInGroup(User user, PortletGroup group) {
         return hasRoleInGroup(user, group, PortletRole.USER);
     }
 
     public boolean hasGuestRoleInGroup(User user, PortletGroup group) {
         return hasRoleInGroup(user, group, PortletRole.GUEST);
     }
 
     public List getUsersWithSuperRole() {
         List users = getUsers(getCoreGroup());
         List supers = new Vector();
         Iterator it = users.iterator();
         while (it.hasNext()) {
             User u = (User) it.next();
             if (this.hasRoleInGroup(u, getCoreGroup(), PortletRole.SUPER)) {
                 supers.add(u);
             }
         }
         return supers;
     }
 
     public void grantSuperRole(User user) {
         //addGroupEntry(user, SportletGroup.SUPER, PortletRole.SUPER);
         addGroupEntry(user, getCoreGroup(), PortletRole.SUPER);
     }
 
     public boolean hasSuperRole(User user) {
         return hasRoleInGroup(user, getCoreGroup(), PortletRole.SUPER);
         //isUserInGroup(user, SportletGroup.SUPER);
     }
 }
