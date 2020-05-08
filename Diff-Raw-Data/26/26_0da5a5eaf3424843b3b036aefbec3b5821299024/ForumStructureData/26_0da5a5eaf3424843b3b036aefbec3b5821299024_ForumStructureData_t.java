 package org.jtalks.poulpe.web.controller.section;
 
 import org.jtalks.common.model.entity.Entity;
 import org.jtalks.poulpe.model.entity.Jcommune;
 import org.jtalks.poulpe.model.entity.PoulpeBranch;
 import org.jtalks.poulpe.model.entity.PoulpeSection;
 import org.jtalks.poulpe.web.controller.zkutils.ZkTreeModel;
 import org.jtalks.poulpe.web.controller.zkutils.ZkTreeNode;
 import org.zkoss.zul.DefaultTreeNode;
 import org.zkoss.zul.ListModelList;
 import org.zkoss.zul.TreeNode;
 
 import javax.annotation.Nonnull;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Is used to contain data for the view and do operations on view. It doesn't do any business logic, only logic that is
  * connected to presenting the data. In MVP it would be a View.
  *
  * @author stanislav bashkirtsev
  * @author Guram Savinov
  */
 public class ForumStructureData {
     private ListModelList<ForumStructureItem> sectionList = new ListModelList<ForumStructureItem>();
     private ForumStructureItem selectedItem = new ForumStructureItem();
     private ZkTreeModel<ForumStructureItem> sectionTree;
     private boolean showSectionDialog;
     private boolean showBranchDialog;
 
     /**
      * Since the {@link Jcommune} object is the root of the Forum Structure tree, this method can fetch this object from
      * the tree and return it.
      *
      * @return the {@link Jcommune} object that is the root of the Forum Structure tree
      */
     public Jcommune getRootAsJcommune() {
         return (Jcommune) (Object) sectionTree.getRoot().getData();
     }
 
     public ForumStructureData closeDialog() {
         showSectionDialog = false;
         showBranchDialog = false;
         return this;
     }
 
     public ForumStructureData showBranchDialog(boolean createNew) {
         if (createNew) {
             selectedItem = new ForumStructureItem(new PoulpeBranch());
         }
         TreeNode<ForumStructureItem> section = sectionTree.getRoot().getChildAt(sectionTree.getSelectionPath()[0]);
         sectionTree.clearSelection();
         sectionList.addToSelection(section.getData());
         showBranchDialog = true;
         return this;
     }
 
     public ForumStructureData showSectionDialog(boolean createNew) {
         if (createNew) {
             selectedItem = new ForumStructureItem(new PoulpeSection());
         }
         showSectionDialog = true;
         return this;
     }
 
     /**
      * Sets an empty item into the {@link #selectedItem} field (should be treated as {@code null}, but Null Object
      * pattern is used to get rid of cumbersome conditions).
      *
      * @return the item that was previously set as selected (the one that is removed by this method)
      */
     public ForumStructureItem removeSelectedItem() {
         int[] selectedPath = getSectionTree().getSelectionPath();
         if (getSelectedItem().isBranch()) {
             getSectionTree().getRoot().getChildAt(selectedPath[0]).remove(selectedPath[1]);
         } else {
             getSectionTree().getRoot().remove(selectedPath[0]);
         }
         return setSelectedItem(new ForumStructureItem());
     }
 
     /**
      * Gets the section that was chosen in the list of available sections while moving the branch to another section (or
      * creating new).
      *
      * @return the section that was chosen in the list of available sections
      */
     public ForumStructureItem getSectionSelectedInDropdown() {
         return getSectionList().getSelection().iterator().next();
     }
 
     /**
      * The list of available sections (in the branch dialog where Admin can choose to what section this branch should
      * go).
      *
      * @return the list of available sections
      */
     public ListModelList<ForumStructureItem> getSectionList() {
         return sectionList;
     }
 
     /**
      * The item being currently selected. It can be empty (without underlying entity) if nothing was actually selected.
      *
      * @return item being currently selected or an empty item if nothing is selected
      */
     public ForumStructureItem getSelectedItem() {
         return selectedItem;
     }
 
     /**
      * Replaces the old selected item with the new one specified.
      *
      * @param selectedItem a new selected item to be set
      * @return the previous selected item or empty item if nothing was previously set (since during the class creation,
      *         an empty item is created and set to the selected one)
      */
     public ForumStructureItem setSelectedItem(@Nonnull ForumStructureItem selectedItem) {
         ForumStructureItem previous = this.selectedItem;
         this.selectedItem = selectedItem;
         return previous;
     }
 
     /**
      * Figures out whether the Branch Editing Dialog should be shown right now. It also changes the flag to {@code
      * false} each time because we don't send anything to the server when closing window, so during next change
      * notification ZK will think that we need to show that dialog again which is wrong.
      *
      * @return {@code true} if the Branch Editing dialog should be shown
      * @see #showBranchDialog
      */
     public boolean isShowBranchDialog() {
         boolean show = showBranchDialog && selectedItem.isBranch();
         this.showBranchDialog = false;
         return show;
     }
 
     /**
      * Figures out whether the Section Editing Dialog should be shown right now. It also changes the flag to {@code
      * false} each time because we don't send anything to the server when closing window, so during next change
      * notification ZK will think that we need to show that dialog again which is wrong.
      *
      * @return {@code true} if the Section Editing dialog should be shown
      * @see #showSectionDialog
      */
     public boolean isShowSectionDialog() {
         boolean show = showSectionDialog && !selectedItem.isBranch();
         this.showSectionDialog = false;
         return show;
     }
 
     /**
      * The whole tree of sections and branches as a ZK tree model.
      *
      * @return the tree of sections and branches
      */
     public ZkTreeModel<ForumStructureItem> getSectionTree() {
         return sectionTree;
     }
 
     /**
      * A shorthand method to get currently selected item.
      *
      * @param clazz the class (branch or section) the selected item should be casted to
      * @param <T>   {@link  PoulpeBranch} or {@link PoulpeSection}
      * @return currently selected entity or {@code null} if nothing is selected
      * @see ForumStructureItem#getItem(Class)
      */
     public <T extends Entity> T getSelectedEntity(Class<T> clazz) {
         return getSelectedItem().getItem(clazz);
     }
 
     /**
      * If a new section was created (and thus was put into {@link #setSelectedItem(ForumStructureItem)}, then we need to
      * add that section to the Forum Structure tree to the last position. This method does exactly this and it results
      * in no-op if the selected section is already persistent.
      *
      * @return this
      */
     public ForumStructureData addSelectedSectionToTreeIfNew() {
         if (!getSelectedItem().isPersisted()) {
             TreeNode<ForumStructureItem> sectionNode = new DefaultTreeNode<ForumStructureItem>(
                     getSelectedItem(), new ArrayList<TreeNode<ForumStructureItem>>());
             getSectionTree().getRoot().add(sectionNode);
             getSectionTree().addToSelection(sectionNode);
             getSectionList().add(sectionNode.getData());
         }
         return this;
     }
 
     /**
      * Finds the branch that is currently set as selected and if this is a new branch (was just added via branch
      * creation dialog), adds it to the list of branches of the section. The section that is chosen - the one that was
      * selected in the dropdown list of sections (and it can be accessed via {@link #getSectionSelectedInDropdown()}. It
      * results in the branch been moved to another section if the currently selected section is not persisted yet. When
      * you trigger this method, conditions should be met:
      * <pre>
      *   <li>{@link #getSelectedItem()} should return a non null branch (no matter whether it's already persistent or
      *         not)
      *   </li>
      *   <li>{@link #getSectionSelectedInDropdown()} should return a section chosen in the Branch Editing dialog</li>
      * </pre>
      *
      * @return this
      */
     public ForumStructureData putSelectedBranchToSectionInDropdown() {
         ForumStructureItem branchToPut = getSelectedItem();
         TreeNode<ForumStructureItem> destinationSectionNode = getSectionTree().find(getSectionSelectedInDropdown());
         ZkTreeNode<ForumStructureItem> branchNodeToPut = (ZkTreeNode<ForumStructureItem>) getSectionTree().find(branchToPut);
         if (branchNodeToPut == null) {
             branchNodeToPut = new ZkTreeNode<ForumStructureItem>(branchToPut);
         }
         branchNodeToPut.moveTo(destinationSectionNode);
         getSectionTree().addToSelection(branchNodeToPut);
         getSectionTree().addOpenObject(destinationSectionNode);
         return this;
     }
 
     /**
      * Sets and rebuilds the Forum Structure tree and also updates the branch dialog with the list of available sections
      * so that in combo box we have only sections that are present in the section tree.
      *
      * @param sectionTree the new tree of sections and branches to be shown on the Forum Structure page
      */
 
     public void setSectionTree(@Nonnull ZkTreeModel<ForumStructureItem> sectionTree) {
         this.sectionTree = sectionTree;
         this.sectionList.clear();
         List<ForumStructureItem> sections = unwrap(sectionTree.getRoot().getChildren());
         this.sectionList.addAll(sections);
     }
 
     private List<ForumStructureItem> unwrap(List<TreeNode<ForumStructureItem>> sectionNodes) {
         List<ForumStructureItem> sections = new ArrayList<ForumStructureItem>();
         for (TreeNode<ForumStructureItem> sectionNode : sectionNodes) {
             sections.add(sectionNode.getData());
         }
         return sections;
     }
 }
