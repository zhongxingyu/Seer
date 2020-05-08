 package org.korotovsky.bst.tree;
 
import org.korotovsky.bst.tree.exceptions.DuplicateItemTreeException;
import org.korotovsky.bst.tree.exceptions.NotFoundTreeException;

 interface TreeInterface<T> {
     public void changeTreeSize(Integer how);
 
    public Boolean isEmpty();
    
    public TreeNode<T> findNode(Comparable key, TreeNode<T> treeNode) throws NotFoundTreeException;

    public TreeNode<T> createNode(Comparable key, T data, TreeNode<T> treeNode) throws DuplicateItemTreeException;
 
    public TreeNode<T> removeNode(Comparable key, TreeNode<T> treeNode) throws NotFoundTreeException;
 }
