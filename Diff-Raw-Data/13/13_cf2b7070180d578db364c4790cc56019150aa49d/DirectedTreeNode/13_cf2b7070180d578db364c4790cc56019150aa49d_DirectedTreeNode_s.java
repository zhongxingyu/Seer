 /*
  * Copyright (c) 2013. AgileApes (http://www.agileapes.scom/), and
  * associated organization.
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy of this
  * software and associated documentation files (the "Software"), to deal in the Software
  * without restriction, including without limitation the rights to use, copy, modify,
  * merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
  * permit persons to whom the Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be included in all copies
  * or substantial portions of the Software.
  */
 
 package com.agileapes.couteau.graph.tree.node.impl;
 
 import com.agileapes.couteau.basics.api.Stringifiable;
 import com.agileapes.couteau.graph.node.impl.DirectedNode;
 import com.agileapes.couteau.graph.tree.node.TreeNode;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 /**
  * @author Mohammad Milad Naseri (m.m.naseri@gmail.com)
  * @since 1.0 (2013/7/25, 10:30)
  */
 public class DirectedTreeNode<N extends DirectedTreeNode<N>> extends DirectedNode<N> implements TreeNode<N> {
 
     private final List<N> children = new ArrayList<N>();
     private N parent = null;
     private final Stringifiable<DirectedTreeNode> stringifiable;
 
     public DirectedTreeNode() {
         this(null);
     }
 
     public DirectedTreeNode(Stringifiable<DirectedTreeNode> stringifiable) {
         this.stringifiable = stringifiable;
     }
 
     /**
      * Adds a child to this node
      * @param child    the child to be added
      */
     @Override
     public void addChild(N child) {
         if (child == null) {
             children.add(null);
             return;
         }
         if (child.getParent() != null) {
             throw new IllegalStateException();
         }
         if (!children.contains(child)) {
             children.add(child);
             //noinspection unchecked
             child.setParent((N) this);
             addNeighbor(child);
         }
     }
 
     /**
      * Removes the given child from this node's list of children
      * @param child    the child to be removed
      */
     @Override
     public void removeChild(N child) {
         if (child == null) {
             return;
         }
         children.remove(child);
         removeNeighbor(child);
     }
 
     /**
      * Changes the parent node of this node
      * @param parent    the new parent
      */
     @Override
     public void setParent(N parent) {
         this.parent = parent;
     }
 
     /**
      * Removes all this nodes children
      */
     @Override
     public void removeAllChildren() {
         children.clear();
         removeAllNeighbors();
     }
 
     /**
      * Returns the depth of this node in this tree
      * @return this node's depth relative to the root, with the root's depth
      * being {@code 0}.
      */
     @Override
     public int getDepth() {
         TreeNode node = this;
         int depth = -1;
         while (node != null) {
             depth ++;
             node = node.getParent();
         }
         return depth;
     }
 
     @Override
     public N getFirstChild() {
         return isLeaf() ? null : getChildren().get(0);
     }
 
     @Override
     public N getLastChild() {
         return isLeaf() ? null : getChildren().get(getChildren().size() - 1);
     }
 
     @Override
     public N getPreviousSibling() {
         return isRoot() || getNodeIndex() == 0 ? null : getParent().getChildren().get(getNodeIndex() - 1);
     }
 
     @Override
     public N getNextSibling() {
         return isRoot() || getNodeIndex() == getParent().getChildren().size() - 1 ? null : getParent().getChildren().get(getNodeIndex() + 1);
     }
 
     @Override
     public int getNodeIndex() {
         //noinspection SuspiciousMethodCalls
         return isRoot() ? 0 : getParent().getChildren().indexOf(this);
     }
 
     @Override
     public N getRoot() {
         //noinspection unchecked
         return isRoot() ? (N) this : getParent().getRoot();
     }
 
     /**
      * @return the parent node for this node, or {@code null} if this node is the
      * root node
      */
     @Override
     public N getParent() {
         return parent;
     }
 
     /**
      * @return this node's children. If this list is empty, it means that this node
      * is a leaf node in the tree.
      */
     @Override
     public List<N> getChildren() {
         return Collections.unmodifiableList(children);
     }
 
     /**
      * @return Determines whether or not this node is the root of the tree. In a single
      * tree, only one node can be the root.
      */
     @Override
     public boolean isRoot() {
         return parent == null;
     }
 
     /**
      * Marks leaves in the tree.
      * @return {@code true} if this node is a leaf node in the tree.
      */
     @Override
     public boolean isLeaf() {
         return getChildren().isEmpty();
     }
 
     /**
      * Determines whether this is inside node. An inside node is a node that is the parent
      * to some nodes, and has a parent of its own.
      * @return {@code true} if this is an inside node.
      */
     @Override
     public boolean isInsideNode() {
         return !isRoot() && !isLeaf();
     }
 
     /**
      * This method adds a new neighbor to the node, with the weight being set to zero.
      * If the node is already a neighbor of this node, the weight of the connection between
      * the two will be set to zero. This method also ensures that a node is only added
      * to this node's list of neighbors if it does not create an immediate loop, and thus
      * breaking the tree property.
      * @param neighbor    the node be added as a neighbor
      */
     @Override
     public void addNeighbor(N neighbor) {
         //noinspection SuspiciousMethodCalls
         if (getChildren().contains(neighbor) || (getParent() != null && getParent().equals(neighbor))) {
             super.addNeighbor(neighbor);
         }
     }
 
     @Override
     public String toString() {
         if (stringifiable != null) {
             return stringifiable.toString(this);
         }
         return super.toString();
     }
 }
