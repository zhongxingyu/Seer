 /*
  *  Copyright (C) 2004  The Concord Consortium, Inc.,
  *  10 Concord Crossing, Concord, MA 01742
  *
  *  Web Site: http://www.concord.org
  *  Email: info@concord.org
  *
  *  This library is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public
  *  License as published by the Free Software Foundation; either
  *  version 2.1 of the License, or (at your option) any later version.
  *
  *  This library is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public
  *  License along with this library; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * END LICENSE */
 
 /*
  * Last modification information:
  * $Revision: 1.6 $
  * $Date: 2007-06-07 14:53:21 $
  * $Author: sfentress $
  *
  * Licence Information
  * Copyright 2004 The Concord Consortium 
 */
 package org.concord.view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.util.Hashtable;
 import java.util.Vector;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.UIManager;
 import javax.swing.event.TreeSelectionEvent;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.tree.DefaultMutableTreeNode;
 import javax.swing.tree.TreeNode;
 import javax.swing.tree.TreePath;
 
 import org.concord.framework.util.CheckedColorTreeModel;
 import org.concord.swing.CCJCheckBoxRenderer;
 import org.concord.swing.CCJCheckBoxTree;
 
 /**
  * 
  * CheckedColorTreeControler
  * Class name and description
  *
  * This class can be used to create a tree view of a model that
  * implements CheckedColorTreeModel.  This could still use a bit 
  * more refactoring.  It maintains a separate object graph to represent
  * the tree nodes, this could be eliminated.  
  *
  * Date created: Apr 27, 2006
  *
  * @author scott<p>
  *
  */
 public class CheckedColorTreeControler
 {
     CheckedColorTreeModel treeModel;
     
     CCJCheckBoxTree cTree = new CCJCheckBoxTree("Tree Root");
     TreePath lastSelectedPath;    
     
     Hashtable nodeGraphableMap = new Hashtable();
     Vector checkedTreeNodes = new Vector();
     
     AbstractAction newDataSetAction;
     AbstractAction deleteDataSetAction;
     AbstractAction renameAction;
     AbstractAction clearAction;
     
     JPanel treePanel;
     JPanel controlPanel;
 
 	private boolean editable;
     
     public JComponent setup(CheckedColorTreeModel customTreeModel,
     		boolean showNew, boolean editable)
     {
     	this.editable = editable;
         treeModel = customTreeModel;
         
         createActions();
 
         cTree.setCellRenderer(new CCJCheckBoxRenderer());
         cTree.setRootVisible(false);
 
         Vector initialItems = treeModel.getItems(null);
         
         for(int i=0; i<initialItems.size(); i++){
             Object item = initialItems.get(i);            
             String name = treeModel.getItemLabel(item); 
             Color color = treeModel.getItemColor(item);
             CCJCheckBoxTree.NodeHolder nodeHolder = 
                 new CCJCheckBoxTree.NodeHolder(name, true, color);
             cTree.setSelectionPath(null);
             cTree.addObject(nodeHolder);
             nodeGraphableMap.put(nodeHolder, item);
         }
         
         TreeNode rootNode = cTree.getRootNode();
         if(rootNode.getChildCount() > 1) {
             lastSelectedPath = cTree.getPathForRow(0);
             cTree.setSelectionPath(lastSelectedPath);
             
             DefaultMutableTreeNode node = (DefaultMutableTreeNode)cTree.getLastSelectedPathComponent();
             CCJCheckBoxTree.NodeHolder holder = (CCJCheckBoxTree.NodeHolder)node.getUserObject();
             Object selectedItem = nodeGraphableMap.get(holder);
             treeModel.setSelectedItem(selectedItem, true);
         }           
 
         treePanel = new JPanel();
         treePanel.setLayout(new BorderLayout());
 
         controlPanel = new JPanel();
         controlPanel.setLayout(new GridLayout(3,1));
         JButton bNew = new JButton(newDataSetAction);
         JButton bDelete = new JButton(deleteDataSetAction);
         JButton bRename = new JButton(renameAction);
         if(showNew){
         	controlPanel.add(bNew);
         }
         
         controlPanel.add(bDelete);
         controlPanel.add(bRename);
         bNew.setOpaque(false);
         bDelete.setOpaque(false);
         bRename.setOpaque(false);
         
         controlPanel.setBackground(Color.WHITE);
         
         //Added scroll bar so multiple data sets won't roll off screen.
         //Would be nice if there were a way to have scroll appear only as
         //needed, but NOT cut off parts of data names.
         
         JScrollPane cTreeScroll = new JScrollPane(cTree);
         cTreeScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		
        // background white on a Mac
         treePanel.setBackground(UIManager.getColor("Tree.textBackground"));
         treePanel.add(cTreeScroll, BorderLayout.CENTER);
         if(editable){
         	treePanel.add(controlPanel, BorderLayout.NORTH);
         }
                
         cTree.addTreeSelectionListener(tsl);
         cTree.addMouseListener(ml);
 
         return treePanel;
     }
     
     private void createActions() {
         newDataSetAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 addNewItem();
             }
         };
         newDataSetAction.putValue(Action.NAME, "New");
 
         deleteDataSetAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 removeNode();
             }
         };
         deleteDataSetAction.putValue(Action.NAME, "Delete");
 
         renameAction = new AbstractAction() {
             public void actionPerformed(ActionEvent e) {
                 renameNode();
             }
         };
         renameAction.putValue(Action.NAME, "Rename");        
     }
     
     private void renameNode() {
     	if(!editable){
     		return;
     	}
     	
         String newLabel = cTree.renameCurrentNode();
         DefaultMutableTreeNode node = (DefaultMutableTreeNode) cTree.getLastSelectedPathComponent();
         CCJCheckBoxTree.NodeHolder holder = 
             (CCJCheckBoxTree.NodeHolder)node.getUserObject();
         Object item = nodeGraphableMap.get(holder);
         if(item != null && newLabel != null && 
                 newLabel.trim().length() > 0) {
             treeModel.setItemLabel(item, newLabel);
         }
     }
     
     MouseAdapter ml = new MouseAdapter() {
         public void mouseReleased(MouseEvent e) {
             TreePath newSelectedPath = cTree.getSelectionPath();
             if(newSelectedPath != null) {
                 DefaultMutableTreeNode node = 
                     (DefaultMutableTreeNode)newSelectedPath.getLastPathComponent();
                 CCJCheckBoxTree.NodeHolder nodeHolder = 
                     (CCJCheckBoxTree.NodeHolder)node.getUserObject();
                 Object item = nodeGraphableMap.get(nodeHolder);                
                 treeModel.setSelectedItem(item, nodeHolder.checked);
             }
             
             if(e.getClickCount() == 2) {
                 renameNode();
             }
 
             updateItemCheckState();
             treeModel.updateItems();
         }
     };
     
     TreeSelectionListener tsl = new TreeSelectionListener() {
         public void valueChanged(TreeSelectionEvent e) {
             if(e.getSource() == cTree) {
                 TreePath newSelectedPath = cTree.getSelectionPath();
                 if(newSelectedPath != null && newSelectedPath != lastSelectedPath) {
                     lastSelectedPath = newSelectedPath;
                     DefaultMutableTreeNode node = 
                         (DefaultMutableTreeNode)lastSelectedPath.getLastPathComponent();
                     CCJCheckBoxTree.NodeHolder nodeHolder = 
                         (CCJCheckBoxTree.NodeHolder)node.getUserObject();
 
                     Object item = nodeGraphableMap.get(nodeHolder);
                     treeModel.setSelectedItem(item, nodeHolder.checked);
                 } else {
                     treeModel.setSelectedItem(null, false);
                 }
             }
         }
     };
     
     private void addNewItem() {
         // name of new node
         String prompt = "Enter name of the new " +
             treeModel.getItemTypeName();
         String name = 
             JOptionPane.showInputDialog(null, prompt);
         
         if(name == null || name.trim().length() == 0) return;
 
         // get color of new node
         Color color = treeModel.getNewColor();
         
         // add new node to tree if not null
         cTree.removeTreeSelectionListener(tsl);
         cTree.setSelectionPath(null);
         CCJCheckBoxTree.NodeHolder newNodeHolder = 
             new CCJCheckBoxTree.NodeHolder(name, true, color);
         cTree.addObject(newNodeHolder);
         cTree.setSelectionPath(lastSelectedPath);
         cTree.addTreeSelectionListener(tsl);
         
         Object item =   treeModel. addItem(null, name, color);
         
         nodeGraphableMap.put(newNodeHolder, item);
         
         //select the new created datastore
         cTree.setSelectionRow(cTree.getRowCount()-1);
     }
     
     /**
      * remove node from tree, also removes datagraphable from datagraph,
      * key-value pair from hashmap, color related to that node also removed.
      *
      */
     private void removeNode() {
         cTree.removeTreeSelectionListener(tsl);
         Object obj = cTree.removeCurrentNode();
         cTree.addTreeSelectionListener(tsl);
         //System.out.println(obj + " removed from tree");
         if(obj != null) {
             Object item = nodeGraphableMap.get(obj);
             nodeGraphableMap.remove(obj);
             treeModel.removeItem(null, item);
             if(checkedTreeNodes.contains(obj))checkedTreeNodes.removeElement(obj);
             //System.out.println(dataGraphable + " removed too");
         }
         treeModel.setSelectedItem(null, false);
     }
 
     private void updateItemCheckState() {
         checkedTreeNodes = cTree.getCheckedNodes();
         Vector allNodes = cTree.getAllNodes();
         int size = allNodes.size();
         for(int i = 0; i < size; i ++) {
             Object obj = allNodes.elementAt(i);
             Object item = nodeGraphableMap.get(obj);
             if(item != null) {
                 if(checkedTreeNodes.contains(obj)) {
                     treeModel.setItemChecked(item, true);
                 }
                 else {
                     treeModel.setItemChecked(item, false);
                 }
             }
         }        
     } 
     
     /**
      * Completely refresh the view
      *
      */
     public void refresh()
     {
         if(cTree != null && treePanel != null) {
             treePanel.remove(cTree);
         }
         nodeGraphableMap.clear();
         
         cTree = new CCJCheckBoxTree("Tree Root");
         
         cTree.setCellRenderer(new CCJCheckBoxRenderer());
         cTree.setRootVisible(false);
 
         Vector initialItems = treeModel.getItems(null);
         
         for(int i=0; i<initialItems.size(); i++){
             Object item = initialItems.get(i);            
             String name = treeModel.getItemLabel(item); 
             Color color = treeModel.getItemColor(item);
             CCJCheckBoxTree.NodeHolder nodeHolder = 
                 new CCJCheckBoxTree.NodeHolder(name, true, color);
             cTree.setSelectionPath(null);
             cTree.addObject(nodeHolder);
             nodeGraphableMap.put(nodeHolder, item);
         }
         
         TreeNode rootNode = cTree.getRootNode();
         if(rootNode.getChildCount() >= 1) {
             lastSelectedPath = cTree.getPathForRow(0);
             cTree.setSelectionPath(lastSelectedPath);
             
             DefaultMutableTreeNode node = (DefaultMutableTreeNode)cTree.getLastSelectedPathComponent();
             CCJCheckBoxTree.NodeHolder holder = (CCJCheckBoxTree.NodeHolder)node.getUserObject();
             Object selectedItem = nodeGraphableMap.get(holder);
             treeModel.setSelectedItem(selectedItem, true);
         }           
 
                
         cTree.addTreeSelectionListener(tsl);
         cTree.addMouseListener(ml);
 
         treePanel.add(cTree, BorderLayout.CENTER);
         
         // I don't understand this.  By calling the add method I would have
         // expected treePanel to automatically call validate or invalidate
         // invalidate doesn't actually work here
         // The only thing that I found works here is to call validate, that forces
         // the treePanel to relayout the components, otherwise the cTree ends up
         // behind the controlPanel. 
         treePanel.validate();       
     }
     
     public void setSelectedRow(int row)
     {
         lastSelectedPath = cTree.getPathForRow(row);
         cTree.setSelectionPath(lastSelectedPath);
         
         DefaultMutableTreeNode node = (DefaultMutableTreeNode)cTree.getLastSelectedPathComponent();
         CCJCheckBoxTree.NodeHolder holder = (CCJCheckBoxTree.NodeHolder)node.getUserObject();
         Object selectedItem = nodeGraphableMap.get(holder);
         treeModel.setSelectedItem(selectedItem, true);
     }
 }
