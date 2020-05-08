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
 
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 
 /**
  * Wires {@link Group} and {@link PoulpeBranch} by holding selected group for branch, allows to make interface with
  * dynamic comboboxes count.
  *
  * @author Leonid Kazancev
  */
 public class ModeratingGroupComboboxRow {
     private PoulpeBranch currentBranch;
     private Group selectedGroup;
 
     /**
      * {@inheritDoc}
      */
     @Override
     public boolean equals(Object object) {
         if (object instanceof PoulpeBranch) {
             PoulpeBranch otherBranch = (PoulpeBranch) object;
             return currentBranch.getId() == otherBranch.getId();
         } else {
             return super.equals(object);
         }
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public int hashCode() {
         return (int) currentBranch.getId();
     }
 
     /**
      * Construct ModeratingGroupComboboxRow by setting up {@link PoulpeBranch} 'currentBranch'. Field 'selectedGroup'
      * are not seted up, it will initialized by setter method, called from ZK.
      *
      * @param currentBranch {@link PoulpeBranch} at currently processing row
      */
     public ModeratingGroupComboboxRow(PoulpeBranch currentBranch) {
         this.currentBranch = currentBranch;
     }
 
     /**
      * @return {@link Group} witch are selected for 'currentBranch'
      */
     public Group getSelectedGroup() {
         return selectedGroup;
     }
 
     /**
      * @param selectedGroup {@link Group} to hold as pair for 'currentBranch'
      */
     public void setSelectedGroup(Group selectedGroup) {
         this.selectedGroup = selectedGroup;
     }
 
     /**
      * @return {@link PoulpeBranch}
      */
     public PoulpeBranch getCurrentBranch() {
         return currentBranch;
     }
 
     /**
     * Checking for changes of moderator group for branch, also verifies correctness of this comparation
      *
      * @param group {@link Group} to compare
      * @param branch {@link PoulpeBranch} to compare with 'currentBranch'
      * @return true if branch are target branch and moderator group is not same as arg group, false otherwise
      */
     public boolean currentBrunchHasChanges(Group group, PoulpeBranch branch) {
         return hasChanges(group) && currentBranch.equals(branch);
     }
 
     /**
     * Compares {@link Group} taken as arg with {@link Group} currently selected as moderator for current branch
      *
      * @param group {@link Group} to compare
      * @return true if moderators group are not same, and should be changed, false otherwise
      */
     public boolean hasChanges(Group group) {
         return !group.equals(selectedGroup);
     }
 
     /**
     * Sets moderators {@link Group} for 'currentBranch'
      */
     public void setModeratorsGroup() {
         currentBranch.setModeratorsGroup(selectedGroup);
     }
 }
