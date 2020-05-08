 /*
  * [The "New BSD" license]
  * Copyright (c) 2012 The Board of Trustees of The University of Alabama
  * All rights reserved.
  *
  * See LICENSE for details.
  */
 package edu.ua.eng.software.novel;
 
 import java.util.Collections;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.LinkedList;
 import java.util.ArrayList;
 import java.util.Set;
 
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.JTree;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.DefaultTreeCellRenderer;
 import javax.swing.tree.DefaultTreeModel;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 import javax.swing.tree.TreeSelectionModel;
 
 import de.uni_bremen.st.rcf.model.CloneClass;
 import de.uni_bremen.st.rcf.model.File;
 import de.uni_bremen.st.rcf.model.Fragment;
 
 /**
  * Initializes the tree structure for clone classes
  * 
  * @author Colin C. Hemphill <colin@hemphill.us>
  */
 @SuppressWarnings("serial")
 public class NovelClassesTree extends JTree implements TreeSelectionListener
 {
     public NovelClassesTree() {
         setRootVisible(false);
         getSelectionModel().setSelectionMode(
                 TreeSelectionModel.SINGLE_TREE_SELECTION);
         addTreeSelectionListener(this);
         addMouseListener(new NovelClassesTreeMouseListener());
 
         DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
         renderer.setOpenIcon(null);
         renderer.setClosedIcon(null);
         renderer.setLeafIcon(null);
         setCellRenderer(renderer);
         loadFromDataModel();
     }
 
     public void loadFromDataModel() {
         root = new DefaultMutableTreeNode("Clone Classes");
         model = new DefaultTreeModel(root);
         setModel(model);
         CloneDataModel dataModel = CloneDataModel.getInstance();
         List<CloneClass> cloneClasses = dataModel.getCloneClasses();
         for(CloneClass cc : cloneClasses) {
             Set<Fragment> fragments = dataModel.getClones(cc);
             ClassCell classCell = new ClassCell(fragments);
             root.add(classCell);
             classCell.updateString();
         }
         model.reload();
     }
 
     /** Required by TreeSelectionListener interface. */
     public void valueChanged(TreeSelectionEvent e) {
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) getLastSelectedPathComponent();
 
         if (node == null)
             return;
     }
 
     private DefaultMutableTreeNode root;
     private DefaultTreeModel model;
 
     public class ClassCell extends DefaultMutableTreeNode
     {
         public ClassCell(Set<Fragment> cloneClass) {
             for(Fragment fragment : cloneClass) {
                 FragmentCell cell = new FragmentCell(fragment);
                 add(cell);
             }
         }
 
         public void updateString() {
             setUserObject(this.toString());
             for(FragmentCell cell : getChildren()) {
                 cell.updateString();
             }
         }
 
         public ArrayList<FragmentCell> getChildren() {
             ArrayList<FragmentCell> list = new ArrayList<FragmentCell>();
             Enumeration en = children();
             while(en.hasMoreElements()) {
                 Object obj = en.nextElement();
                 if(obj instanceof FragmentCell) {
                     list.add((FragmentCell) obj);
                 }
             }
             return list;
         }
 
         public int getPosition() {
             return getParent().getIndex(this);
         }
 
         public String toString() {
             return String.format("%d: CloneClass [%d]",
                 getPosition() + 1,
                 getChildCount());
         }
     }
 
     public class FragmentCell extends DefaultMutableTreeNode
     {
         public FragmentCell(Fragment f) {
             this.fragment = f;
         }
 
         public void updateString() {
             setUserObject(this.toString());
         }
 
         public Fragment getFragment() {
             return fragment;
         }
 
         public int getPosition() {
             return getParent().getIndex(this);
         }
 
         public String toString() {
             TreeNode parent = getParent();
             return String.format("%d.%d: %d:%d::%s",
                 parent.getParent().getIndex(parent) + 1,
                 getPosition() + 1,
                 getFragment().getStart().getLine(),
                 getFragment().getEnd().getLine(),
                 getFile().getRelativePath());
         }
 
         public File getFile() {
             return getFragment().getStart().getFile();
         }
 
         public ClassCell getParent() {
             return (ClassCell) super.getParent();
         }
 
         private Fragment fragment;
     }
 
     private class NovelClassesTreeMouseListener extends MouseAdapter
     {
         public void mouseClicked(MouseEvent evt) {
             NovelClassesTree tree = (NovelClassesTree) evt.getSource();
             if (evt.getClickCount() == 2) {
                 TreePath path = getPathForLocation(evt.getX(), evt.getY());
                if(path != null) {
                    TreeNode node = (TreeNode) path.getLastPathComponent();
                    if(!node.isLeaf()) {
                        ClassCell cell = (ClassCell) node;
                        NovelPanelController.getInstance().classCellSelected(cell);
                     } else {
                        FragmentCell cell = (FragmentCell) node;
                        List<FragmentCell> cells = new LinkedList<FragmentCell>();
                        cells.add(cell);
                        if(cell.getNextSibling() != null) {
                            cells.add((FragmentCell) cell.getNextSibling());
                        } else {
                            cells.add((FragmentCell) cell.getPreviousSibling());
                        }
                        NovelPanelController.getInstance().fragmentCellsSelected(cells);
                     }
                 }
             }
         }
     }
 }
