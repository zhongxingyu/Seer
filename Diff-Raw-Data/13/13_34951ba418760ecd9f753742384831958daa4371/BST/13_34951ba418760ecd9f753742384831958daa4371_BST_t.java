 package com.shubham.ds;
 
 class TreeNode {
 	private TreeNode left;
 	private int info;
 	private TreeNode right;
 	
 	public TreeNode(int info) {
 		this.info = info;
 		this.left = null;
 		this.right = null;
 	}
 	
 	public TreeNode getRight() {
 		return right;
 	}
 
 	public TreeNode getLeft() {
 		return left;
 	}
 
 	public int getInfo() {
 		return info;
 	}
 
 	public void setLeft(TreeNode left) {
 		this.left = left;
 	}
 	
 	public void setRight(TreeNode right) {
 		this.right = right;
 	}
 	
 	public void setInfo(int info) {
 		this.info = info;
 	}
 }
 
 public class BST {
 	TreeNode root;
 	
 	public BST() {
 		root = null;
 	}
 	
 	public void insert(TreeNode root, int key) {
 		if(this.root == null) {
 			this.root = new TreeNode(key);
 		} else {
 			TreeNode currentNode = root;
 			if(key < currentNode.getInfo()) {
 				if(currentNode.getLeft() == null) {
 					currentNode.setLeft(new TreeNode(key));
 				} else {
 					insert(currentNode.getLeft(), key);
 				}
 			} else {
 				if(currentNode.getRight() == null) {
 					currentNode.setRight(new TreeNode(key));
 				} else {
 					insert(currentNode.getRight(), key);
 				}
 			}
 		}
 	}
 	
 	
 	void inorderTraversal(TreeNode root) {
 		if(root == null) {
 			return;
 		}
 		inorderTraversal(root.getLeft());
 		System.out.print(root.getInfo() + " ---> ");
 		inorderTraversal(root.getRight());
 	}
 	
 	int getLCA(TreeNode currentNode, int n1, int n2) throws Exception {
 		if(currentNode == null) {
 			throw new Exception("Cannot find LCA for an empty tree");
 		}
 		// if one of n1 or n2 is the root node itself, then LCA does not exist
 		if(currentNode.getInfo() == n1 || currentNode.getInfo() == n2) {
 			throw new Exception("Cannot find LCA for a tree since one of n1 or n2 is the root itself");
 		}
 		// if one of n1 or n2 is a child of the currentNode, then the current node is the LCA
 		if(currentNode.getLeft() != null && (currentNode.getLeft().getInfo() == n1 || currentNode.getLeft().getInfo() == n2 ) ||
 				currentNode.getRight() != null && (currentNode.getRight().getInfo() == n1 || currentNode.getRight().getInfo() == n2 )) {
 			return currentNode.getInfo();
 		}
 		
 		if((n1 < currentNode.getInfo() && currentNode.getInfo() < n2)
 				|| (n2 < currentNode.getInfo() && currentNode.getInfo() < n1) ) return currentNode.getInfo();
 		
 		if(currentNode != null && n1 < currentNode.getInfo() && n2 < currentNode.getInfo()) {
 			return getLCA(currentNode.getLeft(), n1, n2);
 		} else if(currentNode != null && n1 > currentNode.getInfo() && n2 > currentNode.getInfo()) {
 			return getLCA(currentNode.getRight(), n1, n2);
 		} else {
 			return currentNode.getInfo();
 		}
 		
 		
 	}
 	
 	boolean isBst(TreeNode root, int min, int max) {
 		if(root == null) {
 			return true;
 		}
 		if(root.getInfo() < min || root.getInfo() > max) {
 			return false;
 		}
 		
 		return isBst(root.getLeft(), min, root.getInfo()) && isBst(root.getRight(), root.getInfo(), max);
 	}
 	
 	// mirror of a BST without creating a new tree
 	void mirror1(TreeNode root) {
 		if(root == null) {
 			return;
 		}
 		
 		TreeNode left = root.getLeft();
 		TreeNode right = root.getRight();
 		root.setLeft(right);
 		root.setRight(left);
 		
 		mirror1(root.getLeft());
 		mirror1(root.getRight());
 	}
 	
 	// mirror of a BST while creating a new tree
 	TreeNode mirror2(TreeNode root) {
 		if(root == null) {
 			return root;
 		}
 		
 		TreeNode newNode = new TreeNode(root.getInfo());
 		newNode.setLeft(mirror2(root.getRight()));
 		newNode.setRight(mirror2(root.getLeft()));
 		return newNode;
 	}
 	
 	void deleteTree(TreeNode root) {
 		if(root == null) {
 			return;
 		}
 		
 		TreeNode left = root.getLeft();
 		TreeNode right = root.getRight();
 		root = null;
 		
 		deleteTree(left);
 		deleteTree(right);
 		
 	}
 	
 	TreeNode parentNode;
 	boolean isLeftChild;
 	// Delete a node from a BST
 	void deleteNode(TreeNode node, int keyToDelete) throws Exception {
 		if(node == null) {
 			throw new Exception("Could not find the node to be deleted");
 		}
 		if(node.getInfo() == keyToDelete) {
 			// CORE LOGIC FOR NODE DELETION
 			// 1. If the node to be deleted is a leaf node, just delete it
 			if(node.getLeft() == null && node.getRight() == null) {
				if(isLeftChild) {
					parentNode.setLeft(null);
				} else {
					parentNode.setRight(null);
				}
 			} else if (node.getLeft() == null){
				if(isLeftChild) {
					parentNode.setLeft(node.getRight());
				} else {
					parentNode.setRight(node.getRight());
				}
 			} else {
 				TreeNode maxElementOnLeftSubtree = getMaxNode(node.getLeft());
 				node.setInfo(maxElementOnLeftSubtree.getInfo());
 				deleteNode(node.getLeft(), maxElementOnLeftSubtree.getInfo());
 			}
 			
 		} else if(node.getInfo() < keyToDelete) {
 			parentNode = node;
 			isLeftChild = false;
 			deleteNode(node.getRight(), keyToDelete);
 		} else {
 			parentNode = node;
 			isLeftChild = true;
 			deleteNode(node.getLeft(), keyToDelete);
 		}
 	}
 	
 	int maxHeight(TreeNode root) {
 		if(root == null) {
 			return 0;
 		}
 		
 		int leftHeight = maxHeight(root.getLeft());
 		int rightHeight = maxHeight(root.getRight());
 		return leftHeight > rightHeight ? maxHeight(root.getLeft()) + 1 : maxHeight(root.getRight()) + 1;
 	}
 	
 	private TreeNode getMaxNode(TreeNode root) {
 		while(root != null) {
 			root = root.getRight();
 		}
 		return root;
 	}
 	
 	// balancing BST - B-Tree
 	
 	public static void main(String[] args) throws Exception {
 		BST bst = new BST();
 		bst.insert(bst.root, 8);
 		bst.insert(bst.root, 4);
 		bst.insert(bst.root, 12);
 		bst.insert(bst.root, 1);
 		bst.insert(bst.root, 6);
 		bst.inorderTraversal(bst.root);
 		
 		int lca = bst.getLCA(bst.root, 1, 6);
 		System.out.println("\nLCA: " + lca);
 		
 	}
 	
 }
