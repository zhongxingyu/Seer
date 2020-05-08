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
 import org.jtalks.poulpe.web.controller.zkutils.ZkTreeModel;
 import org.jtalks.poulpe.web.controller.zkutils.ZkTreeNode;
 import org.zkoss.zul.TreeNode;
 
 import javax.annotation.Nonnull;
 import javax.annotation.Nullable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * A tree model specifically dedicated to work with forum structure.
  *
  * @author stanislav bashkirtsev
  * @author Guram Savinov
  */
 public class ForumStructureTreeModel extends ZkTreeModel<ForumStructureItem> {
     private static final long serialVersionUID = 20110138264143L;
 
     public ForumStructureTreeModel(@Nonnull ZkTreeNode<ForumStructureItem> root) {
         super(root);
     }
 
     /**
      * Puts the specified branch to the specified section. It moves the branch from its parent if it's already existing
      * one or it will simply create a new node inside the section.The branch doesn't change its position if it's already
      * in the specified section.
      *
      * @param branchToPut        a branch item to be moved/added to the specified section
      * @param destinationSection a section that is accepting a specified branch
      * @return this
      */
     public ForumStructureTreeModel putBranch(ForumStructureItem branchToPut, ForumStructureItem destinationSection) {
         TreeNode<ForumStructureItem> destinationSectionNode = find(destinationSection);
         ZkTreeNode<ForumStructureItem> branchNodeToPut = (ZkTreeNode<ForumStructureItem>) find(branchToPut);
         if (branchNodeToPut == null) {
             branchNodeToPut = new ZkTreeNode<ForumStructureItem>(branchToPut);
         }
         branchNodeToPut.moveTo(destinationSectionNode);
         addToSelection(branchNodeToPut);
         addOpenObject(destinationSectionNode);
         return this;
     }
 
     public ForumStructureTreeModel moveBranchIfSectionChanged(PoulpeBranch branch) {
         ZkTreeNode<ForumStructureItem> branchNode = (ZkTreeNode<ForumStructureItem>) find(
                 new ForumStructureItem(branch));
         ZkTreeNode<ForumStructureItem> sectionNode = (ZkTreeNode<ForumStructureItem>) find(
                 new ForumStructureItem(branch.getSection()));
         if (branchNode == null) {
             branchNode = new ZkTreeNode<ForumStructureItem>(new ForumStructureItem(branch));
         }
         branchNode.moveTo(sectionNode);
         setSelectedNode(branchNode);
         addOpenObject(sectionNode);
         return this;
     }
 
     public void addIfAbsent(PoulpeSection section) {
         TreeNode<ForumStructureItem> sectionNode = find(new ForumStructureItem(section));
         if (sectionNode == null) {
             sectionNode = createSectionNode(section);
             getRoot().add(sectionNode);
         }
     }
 
     public PoulpeSection getSelectedSection() {
         ForumStructureItem selectedData = getSelectedData(0);
         if (selectedData != null) {
             return selectedData.getSectionItem();
         }
         return null;
     }
 
     /**
      * Removes a branch from the tree or does nothing if the branch wasn't found.
      *
      * @param branch a branch to remove from the tree
      * @return a node that was containing that branch or null if no such node found and thus branch wasn't removed
      */
     public ZkTreeNode<ForumStructureItem> removeBranch(@Nullable PoulpeBranch branch) {
         ForumStructureItem itemToRemove = new ForumStructureItem(branch);
         return removeItem(itemToRemove);
     }
 
     /**
      * Deletes a section from the tree or does nothing if the section can't be found.
      *
      * @param section a section to be removed from the tree
      * @return the removed node or null if no section was found and thus nothing was removed
      */
     public ZkTreeNode<ForumStructureItem> removeSection(@Nullable PoulpeSection section) {
         ForumStructureItem nodeData = new ForumStructureItem(section);
         return removeItem(nodeData);
     }
 
     private ZkTreeNode<ForumStructureItem> removeItem(ForumStructureItem itemToRemove) {
         ZkTreeNode<ForumStructureItem> nodeToRemove = (ZkTreeNode<ForumStructureItem>) find(itemToRemove);
         if (nodeToRemove != null) {
             nodeToRemove.removeFromParent();
         }
         return nodeToRemove;
     }
 
     /**
      * @return a list of sections or empty list if there are no sections in forum structure
      */
     public List<PoulpeSection> getSections() {
         List<PoulpeSection> sections = new ArrayList<PoulpeSection>();
         List<TreeNode<ForumStructureItem>> sectionNodes = getRoot().getChildren();
         if (sectionNodes == null) {
             return sections;
         }
         for (TreeNode<ForumStructureItem> sectionNode : sectionNodes) {
             sections.add(sectionNode.getData().getSectionItem());
         }
         return sections;
     }
 
     private ZkTreeNode<ForumStructureItem> createSectionNode(PoulpeSection section) {
         ForumStructureItem sectionItem = new ForumStructureItem(section);
         return new ZkTreeNode<ForumStructureItem>(sectionItem, new ArrayList<TreeNode<ForumStructureItem>>());
     }
 
     /**
      * Handler of event when branch node dragged and dropped to another node.
      *
      * @param draggedNode the node represents dragged branch item
      * @param targetNode  the node represents target item (it can be branch or section item)
      */
     public void onDropBranch(TreeNode<ForumStructureItem> draggedNode,
                              TreeNode<ForumStructureItem> targetNode) {
         ForumStructureItem targetItem = targetNode.getData();
         if (targetItem.isBranch()) {
             dropNodeBefore(draggedNode, targetNode);
         } else {
             dropNodeIn(draggedNode, targetNode);
         }
         setSelectedNode(draggedNode);
     }
 
     /**
      * Handler of event when section node dragged and dropped to another section node.
      *
      * @param draggedNode the node represents dragged section item
      * @param targetNode  the node represents target section item
      */
     public void onDropSection(TreeNode<ForumStructureItem> draggedNode,
                               TreeNode<ForumStructureItem> targetNode) {
         ForumStructureItem draggedItem = draggedNode.getData();
         ForumStructureItem targetItem = targetNode.getData();
         PoulpeSection draggedSection = draggedItem.getSectionItem();
         PoulpeSection targetSection = targetItem.getSectionItem();
         Jcommune jcommune = getRootAsJcommune();
         jcommune.moveSection(draggedSection, targetSection);
         dropNodeBefore(draggedNode, targetNode);
         setSelectedNode(draggedNode);
     }
 
     /**
      * Checks that dropping item haven't effect.
      *
      * @param draggedItem the item to move
      * @param targetItem  the target item
      * @return {@code true} if dropping have no effect, otherwise return {@code false}
      */
     public boolean noEffectAfterDropItem(ForumStructureItem draggedItem, ForumStructureItem targetItem) {
         if (draggedItem.isBranch()) {
             PoulpeBranch draggedBranch = draggedItem.getBranchItem();
             return noEffectAfterDropBranch(draggedBranch, targetItem);
         } else if (draggedItem.isSection()) {
             return noEffectAfterDropSection(draggedItem, targetItem);
         }
         return false;
     }
 
     /**
      * Checks that dropping branch haven't effect.
      *
      * @param draggedBranch the branch to move
      * @param targetItem    the target item, may be branch as well as section
      * @return {@code true} if dropping have no effect, otherwise return {@code false}
      */
     public boolean noEffectAfterDropBranch(PoulpeBranch draggedBranch, ForumStructureItem targetItem) {
         PoulpeSection draggedSection = draggedBranch.getPoulpeSection();
         if (targetItem.isSection()) {
             return draggedSection.equals(targetItem.getSectionItem());
         }
 
         PoulpeBranch targetBranch = targetItem.getBranchItem();
         PoulpeSection targetSection = targetBranch.getPoulpeSection();
         if (draggedSection.equals(targetSection)) {
             List<PoulpeBranch> branches = draggedSection.getPoulpeBranches();
             int draggedIndex = branches.indexOf(draggedBranch);
             int targetIndex = branches.indexOf(targetBranch);
             if (targetIndex - 1 == draggedIndex) {
                 return true;
             }
         }
 
         return false;
     }
 
     /**
      * Checks that dropping section haven't effect.
      *
      * @param draggedItem the section item to move
      * @param targetItem  the target section item
      * @return {@code true} if dropping have no effect, otherwise return {@code false}
      */
     public boolean noEffectAfterDropSection(ForumStructureItem draggedItem, ForumStructureItem targetItem) {
         PoulpeSection draggedSection = draggedItem.getSectionItem();
         List<PoulpeSection> sections = getRootAsJcommune().getSections();
         int draggedIndex = sections.indexOf(draggedSection);
         int targetIndex = sections.indexOf(targetItem.getSectionItem());
         return targetIndex - 1 == draggedIndex;
     }
 
     /**
      * Since the {@link Jcommune} object is the root of the Forum Structure tree, this method can fetch this object from
      * the tree and return it.
      *
      * @return the {@link Jcommune} object that is the root of the Forum Structure tree
      */
     public Jcommune getRootAsJcommune() {
         return (Jcommune) (Object) getRoot().getData();
     }
 
 }
