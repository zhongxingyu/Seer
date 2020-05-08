 package org.korotovsky.bst.tree;
 
 interface TreeInterface<T> {
     public void changeTreeSize(Integer how);
 
    public TreeNode<T> findNode(Comparable key, TreeNode<T> treeNode);
 
    public TreeNode<T> createNode(Comparable key, T data, TreeNode<T> treeNode);
 }
