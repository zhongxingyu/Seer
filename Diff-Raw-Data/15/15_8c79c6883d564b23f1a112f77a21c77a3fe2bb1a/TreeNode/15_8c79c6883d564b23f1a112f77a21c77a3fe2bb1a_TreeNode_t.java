 /*
 * Copyright (c) 2010 The Jackson Laboratory
 *
 * This is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this software. If not, see <http://www.gnu.org/licenses/>.
 */
 
 package org.jax.drakegenetics.shareddata.client;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * a node of the Tree<T> class
  * @author gbeane
  */
 public class TreeNode<T> implements Serializable {
 
	private static final long serialVersionUID = 1L;  
     protected T data;
     protected List<TreeNode<T>> children;
 
 
     public TreeNode() {
         children = new ArrayList<TreeNode<T>>();
     }
 
 
     public TreeNode(T data) {
         this();
         this.data = data;
     }
 
 
     public T getData() {
         return data;
     }
 
 
     public TreeNode<T> getChild(int i) {
         return children.get(i);
     }
 
 
     public List<TreeNode<T>> getChildren() {
         return children;
     }
 
 
     public void setChildren(List<TreeNode<T>> children) {
         this.children = children;
     }
 
 
     public void addChild(TreeNode<T> child) {
         children.add(child);
     }
 
     public int getChildCount() {
         return children.size();
     }
 
     boolean isLeaf() {
         if (children.size() > 0) {
             return false;
         }
 
         return true;
     }
 
     public TreeNode<T> findChild(T data) {
         for (TreeNode<T> n : children ) {
             if (data.equals(n.data)) {
                 return n;
             }
         }
 
         return null;
     }
 
     public boolean validatePath(List<T> nodes) {
 
         TreeNode<T> node = this;
 
         for (T t : nodes) {
             // look to see if this String is one of the child nodes of our current node
             node = node.findChild(t);
             if (node == null) {
                 return false;
             }
         }
 
         return true;
     }
 
 
     @Override
     public String toString() {
         StringBuilder sb = new StringBuilder();
         sb.append("{").append(data.toString()).append(",[");
 
         for (int i = 0; i < children.size(); i++) {
             if (i > 0) {
                 sb.append(",");
             }
             sb.append(children.get(i).getData().toString());
         }
 
         sb.append("]}");
 
         return sb.toString();
     }
 
     public String treeString() {
         return toList().toString();
     }
 
    /**
      * turn the tree rooted at this node into a List by doing a preorder traversal
      * @return a list of all nodes in the tree rooted at this node
      */
     public List<TreeNode<T>> toList() {
         List<TreeNode<T>> list = new ArrayList<TreeNode<T>>();
         traversePreOrder(this, list);
         return list;
 
     }
 
     /**
      * perform a preorder traversal from @param node and add nodes to @param list
      * @param node
      * @param list
      */
     private void traversePreOrder(TreeNode<T> node, List<TreeNode<T>> list) {
         list.add(node);
 
         for (int i = 0; i < node.getChildCount(); i++) {
             traversePreOrder(node.getChild(i), list);
         }
 
     }
 
 }
