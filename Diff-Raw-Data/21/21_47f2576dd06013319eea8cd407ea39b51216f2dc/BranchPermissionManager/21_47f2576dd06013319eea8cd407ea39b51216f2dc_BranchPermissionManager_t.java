 /**
  * Copyright (C) 2011  JTalks.org Team
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
  */
 package org.jtalks.poulpe.logic;
 
 import org.jtalks.common.model.permissions.BranchPermission;
 import org.jtalks.common.security.acl.AclManager;
 import org.jtalks.common.security.acl.GroupAce;
 import org.jtalks.common.security.acl.builders.AclAction;
 import org.jtalks.common.security.acl.builders.AclBuilders;
 import org.jtalks.poulpe.model.dao.GroupDao;
 import org.jtalks.poulpe.model.dto.branches.BranchAccessChanges;
 import org.jtalks.poulpe.model.dto.branches.BranchAccessList;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeGroup;
 
 import javax.annotation.Nonnull;
 import java.util.List;
 
 /**
  * Responsible for allowing, restricting or deleting the permissions of the User
  * Groups to actions related to the Branches.
  * 
  * @author stanislav bashkirtsev
  */
 public class BranchPermissionManager {
     private final AclManager aclManager;
     private final GroupDao groupDao;
 
     /**
      * Constructs {@link BranchPermissionManager} with given {@link AclManager}
      * and {@link GroupDao}
      * 
      * @param aclManager manager instance
      * @param groupDao group dao instance
      */
     public BranchPermissionManager(@Nonnull AclManager aclManager, @Nonnull GroupDao groupDao) {
         this.aclManager = aclManager;
         this.groupDao = groupDao;
     }
 
     /**
      * Changes the granted permissions according to the specified changes.
      * 
      * @param branch the branch to change permissions to
      * @param changes contains a permission itself, a list of groups to be
      * granted to the permission and the list of groups to remove their granted
      * privileges
      * @see org.jtalks.poulpe.model.dto.branches.BranchAccessChanges#getNewlyAddedGroupsAsArray()
      * @see org.jtalks.poulpe.model.dto.branches.BranchAccessChanges#getRemovedGroups()
      */
     public void changeGrants(PoulpeBranch branch, BranchAccessChanges changes) {
        AclBuilders builders = new AclBuilders();
        builders.newBuilder(aclManager)
                .grant(changes.getPermission()).to(changes.getNewlyAddedGroupsAsArray()).on(branch).flush();
        builders.newBuilder(aclManager)
                .delete(changes.getPermission()).from(changes.getRemovedGroupsAsArray()).on(branch).flush();
     }
 
     /**
      * Changes the restricting permissions according to the specified changes.
      * 
      * @param branch the branch to change permissions to
      * @param changes contains a permission itself, a list of groups to be
      * restricted from the permission and the list of groups to remove their
      * restricting privileges
      * @see org.jtalks.poulpe.model.dto.branches.BranchAccessChanges#getNewlyAddedGroupsAsArray()
      * @see org.jtalks.poulpe.model.dto.branches.BranchAccessChanges#getRemovedGroups()
      */
     public void changeRestrictions(PoulpeBranch branch, BranchAccessChanges changes) {
        AclBuilders builders = new AclBuilders();
        builders.newBuilder(aclManager)
                .restrict(changes.getPermission()).to(changes.getNewlyAddedGroupsAsArray()).on(branch).flush();
        builders.newBuilder(aclManager)
                .delete(changes.getPermission()).from(changes.getRemovedGroupsAsArray()).on(branch).flush();
     }
 
     /**
      * @param branch object identity
      * @return {@link BranchAccessList} for given branch
      */
     public BranchAccessList getGroupAccessListFor(PoulpeBranch branch) {
         BranchAccessList branchAccessList = BranchAccessList.create(BranchPermission.getAllAsList());
         List<GroupAce> groupAces = aclManager.getBranchPermissions(branch);
         for (GroupAce groupAce : groupAces) {
             branchAccessList.put(groupAce.getBranchPermission(), getGroup(groupAce), groupAce.isGranting());
         }
         return branchAccessList;
     }
 
     /**
      * @param groupAce from which if of group should be extracted
      * @return {@link PoulpeGroup} extracted from {@link GroupAce}
      */
     private PoulpeGroup getGroup(GroupAce groupAce) {
         return groupDao.get(groupAce.getGroupId());
     }
 
 }
