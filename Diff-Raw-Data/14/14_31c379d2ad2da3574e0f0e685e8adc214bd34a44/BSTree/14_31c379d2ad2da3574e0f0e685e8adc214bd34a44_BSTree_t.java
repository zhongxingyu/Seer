 package com.blogspot.vikkyrk;
 
 public class BSTree<T extends Comparable<T>> {
 	
 	BSTNode root = null;
 	private class BSTNode {
 		T value;
 		BSTNode left, right;
 		
 		private BSTNode(T t) {
 			this(t,null,null);
 		}
 		
 		private BSTNode(T t, BSTNode l, BSTNode r) {
 			value = t;
 			left = l;
 			right = r;
 		}
 	}
 	
 	public void insert(T t) {
 		if(root == null) {
 			root = new BSTNode(t);
 			return;
 		}
 		
 		BSTNode temp = root, prevNode = null;
 		int compare;
 		
 		while(temp!=null) {
 			compare = temp.value.compareTo(t);
 			if(compare < 0) {
 				prevNode = temp;
 				temp = temp.right;
 			} else if(compare > 0) {
 				prevNode = temp;
 				temp = temp.left;
 			} else {
 				throw new IllegalArgumentException("This element already exists");
 			}
 		}
 		
 		if(prevNode.value.compareTo(t) < 0) {
 			prevNode.right = new BSTNode(t);
 		} else {
 			prevNode.left = new BSTNode(t);
 		}
 	}
 	
 	public void recursiveInsert(T t) {
 		recursiveInsert(root, t);
 	}
 	
	//FIXME: Currently Not working
 	private void recursiveInsert(BSTNode node, T t) {
 		if(node == null) {
 			node = new BSTNode(t);
 			return;
 		}
 		
 		if(node.value.compareTo(t) < 0) {
 			recursiveInsert(node.right,t);
 		} else {
 		    recursiveInsert(node.left,t);
 		}
 	}
 	
 	public void preOrderTraversal() {
 		System.out.println("\nPreOrderTraversal");
 		preOrderTraversal(root);
 	}
 	
 	private void preOrderTraversal(BSTNode node) {
 		if(node == null) 
 			return;
 		System.out.print(node.value + ",");
 		preOrderTraversal(node.left);
 		preOrderTraversal(node.right);
 		
 	}
 	
 	public void inOrderTraversal() {
 		System.out.println("\nInOrderTraversal");
 		inOrderTraversal(root);
 	}
 	
 	/*
 	 * Inorder traversal on a BSTree prints out the list
 	 * in a sorted order. This is the principle of TreeSort.
 	 * But if the tree is unbalanced, the worst case is
 	 * O(n^2). Explore self balancing trees for that.
 	 */
 	private void inOrderTraversal(BSTNode node) {
 		if(node == null) 
 			return;
 		inOrderTraversal(node.left);
 		System.out.print(node.value + ",");
 		inOrderTraversal(node.right);
 	}
 	
 	public void postOrderTraversal() {
 		System.out.println("\nPostOrderTraversal");
 		postOrderTraversal(root);
 	}
 	
 	private void postOrderTraversal(BSTNode node) {
 		if(node == null) 
 			return;
 		postOrderTraversal(node.left);
 		postOrderTraversal(node.right);
 		System.out.print(node.value + ",");		
 	}
 }
