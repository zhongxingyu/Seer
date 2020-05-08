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
 import org.jtalks.poulpe.service.GroupService;
 import org.jtalks.poulpe.web.controller.SelectedEntity;
 import org.jtalks.poulpe.web.controller.WindowManager;
 import org.zkoss.bind.annotation.BindingParam;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.bind.annotation.NotifyChange;
 import org.zkoss.zul.ListModelList;
 
 import javax.annotation.Nonnull;
 
 /**
  * View-model for 'User Groups' Is used to order to work with page that allows admin to manage groups(add, edit,
  * delete). Also class provides access to Members edit window, presented by {@link EditGroupMembersVm}.
  *
  * @author Leonid Kazancev
  */
 public class UserGroupVm {
     private static final String SHOW_DELETE_DIALOG = "showDeleteDialog", SHOW_EDIT_DIALOG = "showEditDialog",
             SHOW_NEW_DIALOG = "showNewDialog", SELECTED_GROUP = "selectedGroup";
 
     //Injected
     private GroupService groupService;
     private final WindowManager windowManager;
 
     private ListModelList<Group> groups;
     private Group selectedGroup;
     private SelectedEntity<Group> selectedEntity;
     private String searchString = "";
 
     private boolean showDeleteDialog;
     private boolean showEditDialog;
     private boolean showNewDialog;
 
     /**
      * Construct View-Model for 'User groups' view.
      *
      * @param groupService   the group service instance
      * @param selectedEntity the selected entity instance
      * @param windowManager  the window manager instance
      */
    public UserGroupVm(@Nonnull GroupService groupService, @Nonnull SelectedEntity<Group> selectedEntity, @Nonnull WindowManager windowManager) {
         this.groupService = groupService;
         this.selectedEntity = selectedEntity;
         this.windowManager = windowManager;
 
         this.groups = new ListModelList<Group>(groupService.getAll(), true);
     }
 
     /**
      * Makes group list view actual.
      */
     public void updateView() {
         groups.clear();
         groups.addAll(groupService.getAll());
     }
 
     // -- ZK Command bindings --------------------
 
     /**
      * Look for the users matching specified pattern from the search textbox.
      */
     @Command
     public void searchGroup() {
         groups.clear();
         groups.addAll(groupService.getAllMatchedByName(searchString));
     }
 
     /**
      * Opens edit group members window.
      */
     @Command
     public void showGroupMemberEditWindow() {
         selectedEntity.setEntity(selectedGroup);
         EditGroupMembersVm.showDialog(windowManager);
     }
 
     /**
      * Deletes selected group.
      */
     @Command
     @NotifyChange(SELECTED_GROUP)
     public void deleteGroup() {
         groupService.deleteGroup(selectedGroup);
         closeDialog();
         updateView();
     }
 
     /**
      * Opens group adding dialog.
      */
     @Command
     @NotifyChange({SELECTED_GROUP, SHOW_NEW_DIALOG})
     public void showNewGroupDialog() {
         selectedGroup = new Group();
         showNewDialog = true;
     }
 
     /**
      * Saves group, closing group edit(add) dialog and updates view.
      *
      * @param group editing group
      */
 
     @Command
     @NotifyChange({SHOW_NEW_DIALOG, SHOW_DELETE_DIALOG, SHOW_EDIT_DIALOG})
     public void saveGroup(@BindingParam(value = "group") Group group) {
         groupService.saveGroup(group);
         closeDialog();
         updateView();
     }
 
     /**
      * Close all dialogs by set visibility to false.
      */
     @Command
     @NotifyChange({SHOW_NEW_DIALOG, SHOW_DELETE_DIALOG, SHOW_EDIT_DIALOG})
     public void closeDialog() {
         showNewDialog = false;
         showDeleteDialog = false;
         showEditDialog = false;
     }
 
     // -- Getters/Setters --------------------
 
     /**
      * Gets visibility status of Delete dialog window.
      *
      * @return true if dialog is visible false if dialog is invisible
      */
     public boolean isShowDeleteDialog() {
         return showDeleteDialog;
     }
 
     /**
      * Gets visibility status of Edit dialog window.
      *
      * @return true if dialog is visible false if dialog is invisible
      */
     public boolean isShowEditDialog() {
         return showEditDialog;
     }
 
     /**
      * Gets visibility status of New group dialog window, boolean show added as fix for onClose action, which don't send
      * anything to the server when closing window because of event.stopPropagation, so during next change notification
      * ZK will think that we need to show that dialog again which is wrong.
      *
      * @return true if dialog is visible false if dialog is invisible
      */
     public boolean isShowNewDialog() {
         boolean show = showNewDialog;
         showNewDialog = false;
         return show;
     }
 
     /**
      * Gets List of groups which shown at UI.
      *
      * @return Groups currently displayed at UI.
      */
     @SuppressWarnings("unused")
     public ListModelList<Group> getGroups() {
         return groups;
     }
 
     /**
      * Gets current selected group.
      *
      * @return Group selected at UI.
      */
     @SuppressWarnings("unused")
     public Group getSelectedGroup() {
         return selectedGroup;
     }
 
     /**
      * Sets current selected group.
      *
      * @param group selected at UI.
      */
     public void setSelectedGroup(Group group) {
         this.selectedGroup = group;
     }
 
     /**
      * Sets List of groups which shown at UI.
      *
      * @param groups selected at UI.
      */
     public void setGroups(ListModelList<Group> groups) {
         this.groups = groups;
     }
 
     /**
      * Sets Search string, used for group search.
      *
      * @param searchString string used for group search.
      */
     public void setSearchString(String searchString) {
         this.searchString = searchString;
     }
 }
