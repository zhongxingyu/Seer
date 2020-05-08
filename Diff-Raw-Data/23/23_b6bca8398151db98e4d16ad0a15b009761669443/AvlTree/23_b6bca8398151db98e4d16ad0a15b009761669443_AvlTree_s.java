 package edu.puc.concurrentavl;
 
 public class AvlTree extends BinarySearchTree {
 
 	protected int balance = 0;
 	
 	public AvlTree() {}
 	
 	public AvlTree(AvlTree parent, int value) {
 		this.value = value;
 		this.hasValue = true;
 		this.parent = parent;
 	}
 	
 	public void reBalance() {
 		
 		int leftHeight = 0, rightHeight = 0;
 		
 		if(children[RIGHT] != null) {
 			rightHeight =  children[RIGHT].height + 1;
 			this.height = rightHeight;
 		}
 		if(children[LEFT] != null) {
 			leftHeight = children[LEFT].height + 1;
 			this.height = Math.max(this.height, leftHeight);
 		}
 		
		if(rightHeight - leftHeight > 1 || rightHeight-leftHeight < 1) {
			System.out.println("Node " + this.value + " has become unbalanced!");
 			this.unbalanced = true;
			return;
 		}
 		
 		if(parent != null)
 			((AvlTree)parent).reBalance();
 	
 	}
 	
 	public void insert(int newValue) {
 		
 		super.insert(newValue);
 		
 	}
 	
 	public void delete(int delVal) {
 		
 		super.delete(delVal);
 	}
 	
 	protected void createChildNode(int direction, int newValue) {		
 		children[direction] = new AvlTree(this, newValue);		
 		if(parent != null)
 		{
 			((AvlTree)parent).reBalance();
 		}
 	}
 	
 
 	
 }
