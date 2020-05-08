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
 package org.jtalks.poulpe.web.controller.section.mvvm;
 
 import org.jtalks.common.model.entity.ComponentType;
 import org.jtalks.poulpe.model.entity.Jcommune;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.jtalks.poulpe.service.ComponentService;
 import org.jtalks.poulpe.web.controller.section.TreeNodeFactory;
 import org.zkoss.bind.annotation.BindingParam;
 import org.zkoss.bind.annotation.Command;
 import org.zkoss.bind.annotation.NotifyChange;
 import org.zkoss.zul.DefaultTreeModel;
 import org.zkoss.zul.DefaultTreeNode;
 import org.zkoss.zul.TreeModel;
 
 import javax.validation.constraints.NotNull;
 
 /**
  * Is used in order to work with page that allows admin to manage sections and branches (moving them, reordering,
  * removing, editing, etc.).
  *
  * @author stanislav bashkirtsev
  * @author Guram Savinov
  */
 public class ForumStructureVm {
     private static final String SHOW_CREATE_SECTION_DIALOG_PROP = "showCreateSectionDialogAndSetFalse",
             SELECTED_ITEM_PROP = "selectedItem", SECTIONS_PROP = "sections",
            SHOW_CREATE_BRANCH_DIALOG_PROP = "showCreateBranchDialogAndSetFalse";
 
     private final ComponentService componentService;
     private ForumStructureItem selectedItem = new ForumStructureItem();
     private DefaultTreeModel<ForumStructureItem> sections;
     private boolean showCreateSectionDialog;
     private boolean showCreateBranchDialog;
 
     public ForumStructureVm(@NotNull ComponentService componentService) {
         this.componentService = componentService;
     }
 
     /**
      * First of all decides whether to show a dialog for creation of the entity or for editing by looking at {@link
      * #getSelectedItem} and then changes the flag {@link #isShowCreateSectionDialogAndSetFalse} in order to open the
      * editing dialog.
      */
     @Command
     @NotifyChange({SHOW_CREATE_SECTION_DIALOG_PROP, SELECTED_ITEM_PROP})
     public void showNewSectionDialog(@BindingParam("createNew") boolean createSection) {
         showCreateSectionDialog = true;
         if (createSection) {
             selectedItem = new ForumStructureItem().setItem(new PoulpeSection());
         } else {
             selectedItem = sections.getSelection().iterator().next().getData();
         }
     }
 
     /**
      * First of all decides whether to show a dialog for creation of the entity or for editing by looking at {@link
      * #getSelectedBranch()} and then changes the flag {@link #isShowCreateBranchDialog()} in order to open the editing
      * dialog.
      */
     @Command
     @NotifyChange({SHOW_CREATE_BRANCH_DIALOG_PROP, SELECTED_ITEM_PROP})
     public void showNewBranchDialog() {
         showCreateBranchDialog = true;
         if (isCreatingNewItem()) {
             selectedItem.setItem(new PoulpeBranch());
         }
     }
 
     /**
      * While opening a dialog we need to know whether the dialog is for creating a new item or editing the existing
      * one.
      *
      * @return {@code true} if there are no existing items being selected for editing
      */
     private boolean isCreatingNewItem() {
         return !selectedItem.isPersisted();
     }
 
     @Command
     @NotifyChange({SECTIONS_PROP, SELECTED_ITEM_PROP})
     public void deleteSelectedSection() {
         Jcommune jcommune = getTreeRootAsJcommune();
         jcommune.getSections().remove(selectedItem.getItem(PoulpeSection.class));
         componentService.saveComponent(jcommune);
         selectedItem = new ForumStructureItem();
     }
 
     /**
      * Saves the {@link #getSelectedItem} to the database, adds it as the last one to the list of sections and cleans
      * the selected section. Also makes the create section dialog to be closed.
      */
     @Command
     @NotifyChange({SECTIONS_PROP, SHOW_CREATE_SECTION_DIALOG_PROP})
     public void saveSection() {
         Jcommune jcommune = getTreeRootAsJcommune();
         jcommune.addSection(selectedItem.getItem(PoulpeSection.class));
         componentService.saveComponent(jcommune);
         selectedItem.clearState();
         showCreateSectionDialog = false;
     }
 
     /**
      * Returns all the sections in our database in order they are actually sorted.
      *
      * @return all the sections in our database in order they are actually sorted or empty list if there are no
      *         sections. Can't return {@code null}.
      */
     @SuppressWarnings("unchecked")
     public TreeModel getSections() {
         return sections = new DefaultTreeModel<ForumStructureItem>(TreeNodeFactory.buildForumStructure(loadJcommune()));
     }
 
     /**
      * Decides whether the  Edit Section dialog should be shown. It's bound to the ZK Window on ZUL page by {@code
      * visible="@bind(...)"}. You should use this in order to control the window visibility. Use NotifyChanges in order
      * ZUL to understand that this field was changed.
      *
      * @return {@code true} if the dialog should be shown to the user
      */
     public boolean isShowCreateSectionDialogAndSetFalse() {
         boolean result = showCreateSectionDialog;
         showCreateSectionDialog = false;
         return result;
     }
    
     /**
      * Decides whether the  Edit Branch dialog should be shown. It's bound to the ZK Window on ZUL page by {@code
      * visible="@bind(...)"}. You should use this in order to control the window visibility. Use NotifyChanges in order
      * ZUL to understand that this field was changed.
      *
      * @return {@code true} if the dialog should be shown to the user
      */
    public boolean isShowCreateBranchDialogAndSetFalse() {
        boolean result = showCreateBranchDialog;
        showCreateBranchDialog = false;
        return result;
     }
 
     /**
      * Let's ZK binder know what section the edit section dialog should work with. It's changed by {@link
      * #setSelectedItem} or it's a newly created section if the one is being created (see {@link
      * #showNewSectionDialog()} for more details).
      *
      * @return currently selected (or newly created) section to be filled by edit section dialog
      */
     public ForumStructureItem getSelectedItem() {
         return selectedItem;
     }
 
     public void setSelectedItem(ForumStructureItem selectedItem) {
         this.selectedItem = selectedItem;
     }
 
     /**
      * Is used by ZK binder to inject the section that is currently selected.
      *
      * @param selectedNode the section that is currently selected
      */
     public void setSelectedNode(DefaultTreeNode selectedNode) {
         this.selectedItem = (ForumStructureItem) selectedNode.getData();
     }
 
     private Jcommune getTreeRootAsJcommune() {
         return (Jcommune) (Object) sections.getRoot().getData();
     }
 
     private Jcommune loadJcommune() {
         return (Jcommune) componentService.getByType(ComponentType.FORUM);
     }
 }
