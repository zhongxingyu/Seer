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
 package org.jtalks.poulpe.web.controller.section;
 
 import org.jtalks.poulpe.model.entity.Jcommune;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.jtalks.poulpe.service.ComponentService;
 import org.jtalks.poulpe.service.ForumStructureService;
 import org.jtalks.poulpe.web.controller.SelectedEntity;
 import org.jtalks.poulpe.web.controller.WindowManager;
 import org.jtalks.poulpe.web.controller.branch.BranchPermissionManagementVm;
 import org.zkoss.bind.annotation.*;
 import org.zkoss.zk.ui.event.DropEvent;
 import org.zkoss.zul.TreeNode;
 import org.zkoss.zul.Treeitem;
 
 import javax.annotation.Nonnull;
 
 import static org.jtalks.poulpe.web.controller.section.TreeNodeFactory.buildForumStructure;
 
 /**
  * Is used in order to work with page that allows admin to manage sections and branches (moving them, reordering,
  * removing, editing, etc.). Note, that this class is responsible for back-end of the operations (presenter,
  * controller), so it stores all the changes to the database using {@link ComponentService}.
  *
  * @author stanislav bashkirtsev
  * @author Guram Savinov
  */
 public class ForumStructureVm {
     private static final String SELECTED_ITEM_PROP = "selectedItemInTree", TREE_MODEL = "treeModel";
     private final ForumStructureService forumStructureService;
     private final WindowManager windowManager;
     private ForumStructureItem selectedItemInTree = new ForumStructureItem(null);
     private SelectedEntity<PoulpeBranch> selectedBranchForPermissions;
     private ForumStructureTreeModel treeModel;
 
     public ForumStructureVm(@Nonnull ForumStructureService forumStructureService, @Nonnull WindowManager windowManager,
                             @Nonnull SelectedEntity<PoulpeBranch> selectedBranchForPermissions) {
         this.forumStructureService = forumStructureService;
         this.windowManager = windowManager;
         this.selectedBranchForPermissions = selectedBranchForPermissions;
     }
 
     /**
      * Creates the whole sections and branches structure. Always hits database. Is executed each time a page is
      * opening.
      */
     @Init
     public void init() {
         treeModel = new ForumStructureTreeModel(buildForumStructure(loadJcommune()));
     }
 
     /**
      * Global command to update tree on events from different view models. <a href="http://books.zkoss.org/wiki/ZK%20Developer's%20Reference/MVVM/Data%20Binding/Global%20Command%20Binding">More
      * information about global commands</a>.
      */
     @GlobalCommand
     @NotifyChange({TREE_MODEL, SELECTED_ITEM_PROP})
     public void refreshTree() {
     }
 
     /**
      * Opens a separate page - Branch Permissions where admin can edit what Groups have wha Permissions on the selected
      * branch.
      */
     @Command
     public void openBranchPermissions() {
         selectedBranchForPermissions.setEntity(getSelectedItemInTree().getBranchItem());
         BranchPermissionManagementVm.showPage(windowManager);
     }
 
     public void removeBranchFromTree(PoulpeBranch branch) {
         treeModel.removeBranch(branch);
     }
 
     public void removeSectionFromTree(PoulpeSection section) {
         treeModel.removeSection(section);
     }
 
     public void updateBranchInTree(PoulpeBranch branch) {
         treeModel.moveBranchIfSectionChanged(branch);
         selectedItemInTree = new ForumStructureItem(branch);
     }
 
     public void updateSectionInTree(PoulpeSection section) {
         treeModel.addIfAbsent(section);
         selectedItemInTree = new ForumStructureItem(section);
     }
 
     public ForumStructureItem getSelectedItemInTree() {
         return selectedItemInTree;
     }
 
     /**
      * Is used by ZK binder to inject the section that is currently selected.
      *
      * @param selectedNode the section that is currently selected
      */
     @NotifyChange(SELECTED_ITEM_PROP)
     public void setSelectedNode(TreeNode<ForumStructureItem> selectedNode) {
         this.selectedItemInTree = selectedNode.getData();
     }
 
     /**
      * Loads instance of JCommune from database.
      *
      * @return instance of JCommune from database
      */
     private Jcommune loadJcommune() {
         Jcommune jcommune = forumStructureService.getJcommune();
         if (jcommune == null) {
             throw new IllegalStateException("Please, create a Forum Component first.");
         }
         return jcommune;
     }
 
     /**
      * Handler of tree nodes drag and drop event.
      *
      * @param event contains all needed info about drag and drop event
      */
     @Command
     @NotifyChange({TREE_MODEL, SELECTED_ITEM_PROP})
     public void dropEventHandler(@BindingParam("event") DropEvent event) {
         TreeNode<ForumStructureItem> draggedNode = ((Treeitem) event.getDragged()).getValue();
         TreeNode<ForumStructureItem> targetNode = ((Treeitem) event.getTarget()).getValue();
         ForumStructureItem draggedItem = draggedNode.getData();
         ForumStructureItem targetItem = targetNode.getData();
         if (treeModel.noEffectAfterDropItem(draggedItem, targetItem)) {
             return;
         }
         if (draggedItem.isBranch()) {
             PoulpeBranch draggedBranch = draggedItem.getBranchItem();
             treeModel.onDropBranch(draggedNode, targetNode);
             if (targetItem.isBranch()) {
                 PoulpeBranch targetBranch = targetItem.getBranchItem();
                 forumStructureService.moveBranch(draggedBranch, targetBranch);
             } else {
                 PoulpeSection targetSection = targetItem.getSectionItem();
                 forumStructureService.moveBranch(draggedBranch, targetSection);
             }
         } else if (draggedItem.isSection()) {
             treeModel.onDropSection(draggedNode, targetNode);
             PoulpeSection draggedSection = draggedItem.getSectionItem();
             PoulpeSection targetSection = targetItem.getSectionItem();
             Jcommune jcommune = treeModel.getRootAsJcommune();
             jcommune.moveSection(draggedSection, targetSection);
             forumStructureService.saveJcommune(jcommune);
         }
     }
 
     public ForumStructureTreeModel getTreeModel() {
         return treeModel;
     }
 
     public void setTreeModel(ForumStructureTreeModel treeModel) {
         this.treeModel = treeModel;
     }
 }
