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
 package org.jtalks.poulpe.web.controller.group;
 
 import com.google.common.collect.ImmutableList;
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.service.BranchService;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 /**
  * Logic controller actions performed at delete moderator group dialog. Holds collection of
  * {@link ModeratingGroupComboboxRow} and list of groups available to set as moderator. This class was created to
  * implement dialog with dynamic comboboxes count and content.
  *
  * @author Leonid Kazancev
  */
 public class BranchGroupMap {
     private List<Group> allAvailableGroups;
     private List<ModeratingGroupComboboxRow> branchesCollection;
     private Iterator<ModeratingGroupComboboxRow> iterator;
 
     /**
      * Construct branchCollection by creating list record for every branch, so every branch will had his own combobox,
      * to select moderator group.
      *
     * @param branches      list of {@link PoulpeBranch} moderated by currently selected at {@link UserGroupVm} group
     * @param groups        list of {@link Group} available for selection
     * @param branchService the branch service instance
      */
     public BranchGroupMap(List<PoulpeBranch> branches, List<Group> groups, BranchService branchService) {
         allAvailableGroups = ImmutableList.copyOf(groups);
         branchesCollection = new ArrayList<ModeratingGroupComboboxRow>(branches.size());
         for (PoulpeBranch branch : branches) {
             branchesCollection.add(new ModeratingGroupComboboxRow(branch, branchService));
         }
     }
 
     /**
      * Sets group as selected for all branches at holded List 'branchesCollection', so all comboboxes will select
      * this group.
      *
      * @param group {@link Group} to set
      */
     public void setSelectedGroupForAllBranches(Group group) {
         for (ModeratingGroupComboboxRow controller : branchesCollection) {
             controller.setSelectedGroup(group);
         }
     }
 
     /**
      * @return currently holded {@link List} of {@link ModeratingGroupComboboxRow}
      */
     public List<ModeratingGroupComboboxRow> getBranchesCollection() {
         return branchesCollection;
     }
 
     /**
      * Saves moderator group for all branches at 'branchCollection' if oldModeratorsGroup were changed, nothing to
      * save otherwise.
      *
      * @param oldModeratorGroup instance of {@link Group} witch were moderator for all branches before dialog were
      *                          shown
      */
     public void setModeratingGroupForAllBranches(Group oldModeratorGroup) {
         iterator = branchesCollection.iterator();
         while (iterator.hasNext()) {
             ModeratingGroupComboboxRow controller = iterator.next();
             if (controller.hasChanges(oldModeratorGroup)) {
                 controller.saveModeratorsGroup();
                 iterator.remove();
             }
         }
     }
 
 
     /**
      * Saves moderator group for single branch if group were changed.
      *
      * @param oldModeratorGroup instance of {@link Group} witch were moderator for 'branch' before method called
      * @param branch            instance of {@link PoulpeBranch} to set moderated by {@link Group} seleted at
      *                          {@link ModeratingGroupComboboxRow}
      */
     public void setModeratingGroupForCurrentBranch(Group oldModeratorGroup, PoulpeBranch branch) {
         iterator = branchesCollection.iterator();
         while (iterator.hasNext()) {
             ModeratingGroupComboboxRow controller = iterator.next();
             if (controller.currentBrunchHasChanges(oldModeratorGroup, branch)) {
                 controller.saveModeratorsGroup();
                 iterator.remove();
             }
         }
     }
 
     /**
      * @return list of all available {@link Group}
      */
     public List<Group> getAllAvailableGroups() {
         return allAvailableGroups;
     }
 
     /**
      * @param allAvailableGroups list of all available {@link Group}
      */
     public void setAllAvailableGroups(List<Group> allAvailableGroups) {
         this.allAvailableGroups = allAvailableGroups;
     }
 }
