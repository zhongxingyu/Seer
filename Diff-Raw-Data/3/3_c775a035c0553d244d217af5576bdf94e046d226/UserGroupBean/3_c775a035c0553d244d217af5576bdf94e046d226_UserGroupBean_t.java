 /***************************************************************
  *  This file is part of the [fleXive](R) backend application.
  *
  *  Copyright (c) 1999-2008
  *  UCS - unique computing solutions gmbh (http://www.ucs.at)
  *  All rights reserved
  *
  *  The [fleXive](R) backend application is free software; you can redistribute
  *  it and/or modify it under the terms of the GNU General Public
  *  License as published by the Free Software Foundation;
  *  either version 2 of the License, or (at your option) any
  *  later version.
  *
  *  The GNU General Public License can be found at
  *  http://www.gnu.org/licenses/gpl.html.
  *  A copy is found in the textfile GPL.txt and important notices to the
  *  license from the author are found in LICENSE.txt distributed with
  *  these libraries.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  For further information about UCS - unique computing solutions gmbh,
  *  please see the company website: http://www.ucs.at
  *
  *  For further information about [fleXive](R), please see the
  *  project website: http://www.flexive.org
  *
  *
  *  This copyright notice MUST APPEAR in all copies of the file!
  ***************************************************************/
 package com.flexive.war.beans.admin.main;
 
 import com.flexive.faces.FxJsfUtils;
 import com.flexive.faces.messages.FxFacesMsgErr;
 import com.flexive.faces.messages.FxFacesMsgInfo;
 import com.flexive.shared.EJBLookup;
 import com.flexive.shared.FxArrayUtils;
 import com.flexive.shared.FxContext;
 import com.flexive.shared.FxSharedUtils;
 import com.flexive.shared.exceptions.FxApplicationException;
 import com.flexive.shared.interfaces.AccountEngine;
 import com.flexive.shared.interfaces.UserGroupEngine;
 import com.flexive.shared.security.*;
 import static com.google.common.collect.Lists.newArrayList;
 
 import java.io.Serializable;
 import java.util.*;
 
 import org.apache.commons.lang.ArrayUtils;
 
 /**
  * Bean providing access the the userGroup functionality.
  *
  * @author Gregor Schober (gregor.schober@flexive.com), UCS - unique computing solutions gmbh (http://www.ucs.at)
  * @version $Rev$
  */
 public class UserGroupBean implements Serializable {
     private static final long serialVersionUID = -5545259367431927116L;
     private static final String ID_CACHE_KEY = UserGroupBean.class + "_id";
 
     private String name = null;
     private String color = null;
     private long mandator = -1;
     private long id = -1;
     private AccountEngine accountEngine;
     private UserGroupEngine groupEngine;
     private Long[] roles = new Long[0];
     private Hashtable<Long, List<UserGroup>> groupLists;
     private long createdGroupId = -1;
     private List<Account> members;
     private List<ACLAssignment> aclAssignments;
 
 
     public Long[] getRoles() {
         return this.roles;
     }
 
     public void setRoles(Long[] roles) {
         this.roles = roles;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String sName) {
         this.name = sName;
     }
 
     public String getColor() {
         return color;
     }
 
     public void setColor(String sColor) {
         this.color = sColor;
     }
 
     public long getMandator() {
         return mandator;
     }
 
     public void setMandator(long mandator) {
         this.mandator = mandator;
     }
 
     public long getId() {
         return id;
     }
 
     public List<Account> getMembers() throws FxApplicationException {
         if ((members == null || members.isEmpty()) && id != -1) {
             members = newArrayList(EJBLookup.getAccountEngine().getAssignedUsers(id, 0, -1));
             Collections.sort(members, new FxSharedUtils.SelectableObjectSorter());
         }
         if (members == null) {
             members = new ArrayList<Account>();
         }
         return members;
     }
 
     public List<ACLAssignment> getAclAssignments() throws FxApplicationException {
         if ((aclAssignments == null || aclAssignments.isEmpty()) && id != -1) {
             aclAssignments = newArrayList(EJBLookup.getAclEngine().loadGroupAssignments(id));
         }
         if (aclAssignments == null) {
             aclAssignments = newArrayList();
         }
         return aclAssignments;
     }
 
     public void setId(long id) {
         this.id = id;
         FxJsfUtils.setSessionAttribute(ID_CACHE_KEY, id);
     }
 
     public long getCreatedGroupId() {
         return createdGroupId;
     }
 
     public UserGroupBean() {
         try {
             this.groupEngine = EJBLookup.getUserGroupEngine();
             this.accountEngine = EJBLookup.getAccountEngine();
             this.groupLists = new Hashtable<Long, List<UserGroup>>(5);
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
     }
 
     /**
      * Returns the list of all usergroups.
      *
      * @return all usergroups
      */
     public List<UserGroup> getList() {
         try {
             final UserTicket ticket = FxContext.getUserTicket();
             long mandatorId;
             if (ticket.isGlobalSupervisor()) {
                 // Drop down list enabled -> handle it
                 mandatorId = getMandator();
             } else {
                 // Only show the groups of the mandator the user belongs to
                 mandatorId = ticket.getMandatorId();
             }
 
             // Load an cache within the result
             List<UserGroup> result = groupLists.get(mandatorId);
             if (result == null) {
                 result = groupEngine.loadAll(mandatorId);
                 groupLists.put(mandatorId, result);
             }
             return result;
 
 
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
             return new ArrayList<UserGroup>(0);
         }
     }
 
     /**
      * Get the number of users for a group, call using userGroupBean.userCount['groupId']
      *
      * @return user count
      */
     public Map<Long, Long> getUserCount() {
         return FxSharedUtils.getMappedFunction(new FxSharedUtils.ParameterMapper<Long, Long>() {
             public Long get(Object key) {
                 try {
                     return accountEngine.getAssignedUsersCount((Long) key, false);
                 } catch (FxApplicationException e) {
                     return 0L;
                 }
             }
         });
     }
 
     /**
      * Creates a new user group.
      *
      * @return the next page to render
      */
     public String create() {
         try {
             final UserTicket ticket = FxContext.getUserTicket();
             long mandatorId = getMandator();
             if (!ticket.isGlobalSupervisor()) {
                 mandatorId = ticket.getMandatorId();
             }
             this.setId(groupEngine.create(name, color, mandatorId));
             this.createdGroupId = this.id;
             new FxFacesMsgInfo("UserGroup.nfo.created", name).addToContext();
 
             // Assign the given roles to the group
             try {
                 groupEngine.setRoles(this.id, ArrayUtils.toPrimitive(roles));
             } catch (Exception exc) {
                 new FxFacesMsgErr(exc).addToContext();
                 color = groupEngine.load(id).getColor();
                 return "userGroupEdit";
             }
 
             // Deselect the group and return to the overview
             this.setId(-1);
             return "userGroupOverview";
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
             return "userGroupNew";
         }
     }
 
     /**
      * Deletes an existing user group.
      *
      * @return the next page to render.
      */
     public String delete() {
         try {
             ensureIdSet();
             groupEngine.remove(id);
             this.groupLists.clear();
            new FxFacesMsgInfo("UserGroup.nfo.deleted").addToContext();
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
         return "userGroupOverview";
     }
 
     /**
      * Edits a existing user group.
      *
      * @return the next page to render.
      */
     public String edit() {
         try {
             ensureIdSet();
             UserGroup grp = groupEngine.load(id);
             this.color = grp.getColor();
             this.name = grp.getName();
             this.mandator = grp.getMandatorId();
             this.roles = Role.toIdArray(groupEngine.getRoles(grp.getId()));
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
         return "userGroupEdit";
     }
 
 
     private void ensureIdSet() {
         if (this.id <= 0) {
             this.id = (Long) FxJsfUtils.getSessionAttribute(ID_CACHE_KEY);
         }
     }
 
     /**
      * Updates a existing group.
      *
      * @return the next page to render
      */
     public String update() {
         try {
             // Update group data
             groupEngine.update(id, name, color);
             // Update the role assignments
             try {
                 groupEngine.setRoles(this.id, ArrayUtils.toPrimitive(getRoles()));
                new FxFacesMsgInfo("UserGroup.nfo.updated", name).addToContext();
             } catch (Exception exc) {
                 new FxFacesMsgErr(exc).addToContext();
             }
             // Reload data
             edit();
         } catch (Exception exc) {
             new FxFacesMsgErr(exc).addToContext();
         }
         return "userGroupEdit";
     }
 
 }
