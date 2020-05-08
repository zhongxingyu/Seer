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
 package org.jtalks.poulpe.web.controller.users;
 
 import org.jtalks.common.model.entity.Group;
 import org.jtalks.common.model.entity.User;
 import org.jtalks.common.service.exceptions.NotFoundException;
 import org.jtalks.poulpe.model.entity.PoulpeUser;
 import org.jtalks.poulpe.service.GroupService;
 import org.jtalks.poulpe.service.UserService;
 import org.jtalks.poulpe.web.controller.section.dialogs.AbstractDialogVm;
 import org.zkoss.bind.annotation.BindingParam;
 import org.zkoss.bind.annotation.GlobalCommand;
 import org.zkoss.bind.annotation.NotifyChange;
 
 import javax.annotation.Nonnull;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * View-model for 'Edit user group list' dialog Is used to order to work with page that allows admin to manage groups
  * list of selected user.
  *
  * @author Leonid Kazancev
  */
 public class EditGroupsVm extends AbstractDialogVm {
     private boolean chosen = true;
     private boolean notChosen = true;
     protected static final String GROUPS = "groupsToShow";
     private static final String EDIT_GROUPS = "editGroups";
     private static final String FILTER_GROUPS="filterGroups";
     private static final String SAVE_CHANGES = "saveUserGroupsChanges";
     private static final String USER_TO_EDIT = "userToEdit";
 
     private List<GroupBooleanPair> groupsToShow;
     private List<GroupBooleanPair> groups;
 
     /**
      * User to be edited
      */
     private PoulpeUser userToEdit;
     private final UserService userService;
     private final GroupService groupService;
     private final UsersVm usersVm;
 
     /**
      * @param usersVm view model of parent window
      * @param userService service instance to save changes
      */
     public EditGroupsVm(@Nonnull UsersVm usersVm, @Nonnull UserService userService, @Nonnull GroupService groupService) {
         this.userService = userService;
         this.groupService = groupService;
         this.usersVm = usersVm;
     }
 
     /**
      * Dialog opening command.
      */
     @GlobalCommand(EDIT_GROUPS)
     @NotifyChange({SHOW_DIALOG, GROUPS, USER_TO_EDIT})
     public void editGroups() throws NotFoundException {
         userToEdit = usersVm.getSelectedUser();
         init();
         showDialog();
     }
 
     /**
      * Activates group filtering.
      */
     @GlobalCommand(FILTER_GROUPS)
     @NotifyChange(GROUPS)
     public void filterGroups() {
         doFilter();
     }
 
     /**
      * Marker method, used to force group list refresh on group selection.
      */
     @GlobalCommand
     @NotifyChange(GROUPS)
     public void colorChange() {
     }
 
     /**
      * Save changes command.
      */
     @GlobalCommand(SAVE_CHANGES)
    @NotifyChange(SHOW_DIALOG)
     public void saveChanges() {
         List<Group> userGroups = userToEdit.getGroups();
         boolean changed = false;
         for (GroupBooleanPair group : groups) {
             Group currentGroup = group.getGroup();
             if (group.isChanged()) {
                 if (group.isEnable()) {
                     userGroups.add(group.getGroup());
                     currentGroup.getUsers().add(userToEdit);
                 } else {
                     userGroups.remove(group.getGroup());
                     currentGroup.getUsers().remove(userToEdit);
                 }
                 groupService.saveGroup(currentGroup);
                 changed = true;
             }
         }
         if (changed) {
             userService.updateUser(userToEdit);
         }
         closeDialog();
     }
 
     private void init() {
         List<Group> allGroups = groupService.getAll();
         List<Group> userGroupList = userToEdit.getGroups();
         groups = new ArrayList<GroupBooleanPair>(allGroups.size());
 
         for (Group group : allGroups) {
             boolean isMember = userGroupList.contains(group);
             GroupBooleanPair pair = new GroupBooleanPair(group, isMember);
             groups.add(pair);
         }
         groupsToShow = new ArrayList<GroupBooleanPair>(groups);
         Collections.sort(groupsToShow);
     }
 
     private void doFilter() {
         groupsToShow = new ArrayList<GroupBooleanPair>(groups.size());
         for (GroupBooleanPair group : groups) {
             boolean isMember = group.isEnable();
            if ((chosen && isMember)|| (notChosen && !isMember)) {
                 groupsToShow.add(group);
             }
         }
         Collections.sort(groupsToShow);
     }
 
     private void closeDialog() {
         isShowDialog();
     }
 
     /**
      * @return chosen filter status
      */
     public boolean isChosen() {
         return chosen;
     }
 
     /**
      * @param  chosen filter status to set
      */
     public void setChosen(boolean chosen) {
         this.chosen = chosen;
     }
 
     /**
      * @return notChosen filter status
      */
     public boolean isNotChosen() {
         return notChosen;
     }
 
     /**
      * @param  notChosen filter status to set
      */
     public void setNotChosen(boolean notChosen) {
         this.notChosen = notChosen;
     }
 
     /**
      * @return all {@link GroupBooleanPair} can be shown at dialog
      */
     public List<GroupBooleanPair> getGroups() {
         return groups;
     }
 
     /**
      * @return {@link PoulpeUser} selected for edit at dialog
      */
     public User getUserToEdit() {
         return userToEdit;
     }
 
     /**
      * @return {@link GroupBooleanPair} currently shown at dialog
      */
     public List<GroupBooleanPair> getGroupsToShow() {
         return groupsToShow;
     }
 }
