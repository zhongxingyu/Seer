 /*
  * This file is part of Disconnected.
  * Copyright (c) 2013 QuarterCode <http://www.quartercode.com/>
  *
  * Disconnected is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Disconnected is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with Disconnected. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package com.quartercode.disconnected.graphics.component;
 
 import de.matthiasmann.twl.model.AbstractTreeTableModel;
 import de.matthiasmann.twl.model.AbstractTreeTableNode;
 import de.matthiasmann.twl.model.TreeTableNode;
 
 /**
  * This tree model is an implementation of twl's abstract tree table model using the {@link TreeNode}-interface.
  * The tree allows adding and removing child nodes.
  * 
  * @see TreeNode
  * @see TreeNodeImpl
  */
 public class TreeModel extends AbstractTreeTableModel implements TreeNode {
 
     private final String[] columns;
 
     /**
      * Creates a new tree model with the given columns.
      * 
      * @param columns The column names to use for the table.
      */
     public TreeModel(String... columns) {
 
         this.columns = columns;
     }
 
     /**
      * Returns the column names which are used for the table.
      * 
      * @return The column names which are used for the table.
      */
     public String[] getColumns() {
 
         return columns;
     }
 
     @Override
     public int getNumColumns() {
 
         return columns.length;
     }
 
     @Override
     public String getColumnHeaderText(int column) {
 
         return columns[column];
     }
 
     @Override
     public TreeNode[] getChildren() {
 
         TreeNodeImpl[] children = new TreeNodeImpl[getNumChildren()];
         for (int index = 0; index < getNumChildren(); index++) {
             children[index] = (TreeNodeImpl) getChild(index);
         }
         return children;
     }
 
     @Override
     public TreeNode addChild(Object... data) {
 
         TreeNodeImpl child = new TreeNodeImpl(this, data);
         insertChild(child, getNumChildren());
         return child;
     }
 
     @Override
     public void removeChild(TreeNode child) {
 
         removeChild(getChildIndex(child));
     }
 
     @Override
     public void removeAllChildren() {
 
         super.removeAllChildren();
     }
 
     /**
      * Tree nodes are used by the tree model to store the tree structure.
      * Every node can hold a data array to use for the columns of the table.
      * 
      * @see TreeNode
      * @see TreeModel
      */
     public static class TreeNodeImpl extends AbstractTreeTableNode implements TreeNode {
 
         private final Object[] data;
 
         /**
          * Creates a new node for a tree with the given parent node and data array to use for the columns of the table.
          * 
          * @param parent The parent node which holds the new node.
          * @param data The data to use for the columns of the table.
          */
         protected TreeNodeImpl(TreeTableNode parent, Object... data) {
 
             super(parent);
 
             this.data = data;
             setLeaf(true);
         }
 
         @Override
         public Object getData(int column) {
 
             return data[column];
         }
 
         @Override
         public TreeNode[] getChildren() {
 
             TreeNodeImpl[] children = new TreeNodeImpl[getNumChildren()];
             for (int index = 0; index < getNumChildren(); index++) {
                 children[index] = (TreeNodeImpl) getChild(index);
             }
             return children;
         }
 
         @Override
         public TreeNode addChild(Object... data) {
 
             TreeNodeImpl child = new TreeNodeImpl(this, data);
             insertChild(child, getNumChildren());
             setLeaf(false);
             return child;
         }
 
         @Override
         public void removeChild(TreeNode child) {
 
             removeChild(getChildIndex(child));
             if (getNumChildren() == 0) {
                 setLeaf(true);
             }
         }
 
         @Override
         public void removeAllChildren() {
 
             super.removeAllChildren();
         }

     }
 
 }
