 package assignment6;
 
 import java.io.FileWriter;
 import java.io.PrintWriter;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.NoSuchElementException;
 
 /**
  * 
  * @author Dylan Noaker & Erich Newey
  * This is a binary search tree.
  *
  * @param <Type> whatever type you want to store
  */
 
 public class BinarySearchTree<Type extends Comparable<? super Type>> implements SortedSet<Type> 
 {
 	private BinaryNode root;
 	private int size;
 	private boolean bigFlag;
 
 	public BinarySearchTree() 
 	{
 		size = 0;
 	}
 	/**
 	 * Ensures that this set contains the specified item.
 	 * 
 	 * @param item
 	 *          - the item whose presence is ensured in this set
 	 * @return true if this set changed as a result of this method call (that is,
 	 *         if the input item was actually inserted); otherwise, returns false
 	 * @throws NullPointerException
 	 *           if the item is null
 	 */
 	/* start at root
 	 * make a comparison, get to an empty branch, make an assignment
 	 * else, compare our way down the tree using a while loop. 
 	 */
 	public boolean add(Type item)
 	{
 		//If the item exists in the BST, we can't add it again.
 		if (this.contains(item))
 			return false;
 
 		if(size==0)
 		{
 			root = new BinaryNode(item);
			//writeDot("treePic.dot");
 			size++;
 			return true;
 		}
 		else{
 			BinaryNode temp = root;
 
 			//Temp should never be null, if this works right.
 			while (temp != null)
 			{
 				//Left hand side
 				if (temp.getData().compareTo(item) > 0)
 				{
 					if (temp.getLeft() == null)
 					{
 						//Add item
 						temp.setLeft(new BinaryNode(item));
						//writeDot("treePic.dot");
 						size++;
 						return true;
 					}
 					else //Keep searching
 						temp = temp.getLeft();
 				}
 
 				//Right hand side
 				else if (temp.getData().compareTo(item) < 0)
 				{
 					if (temp.getRight() == null)
 					{
 						//Add item
 						temp.setRight(new BinaryNode(item));
						//writeDot("treePic.dot");
 						size++;
 						return true;
 					}
 					else //Keep searching
 						temp = temp.getRight();
 				}
 
 				//Should never reach this
 				else
 					return false;
 			}
 
 			//Should never reach this either.
 			return false;
 		}
 	}
 
 	/**
 	 * Ensures that this set contains all items in the specified collection.
 	 * 
 	 * @param items
 	 *          - the collection of items whose presence is ensured in this set
 	 * @return true if this set changed as a result of this method call (that is,
 	 *         if any item in the input collection was actually inserted);
 	 *         otherwise, returns false
 	 * @throws NullPointerException
 	 *           if any of the items are null
 	 */
 	public boolean addAll(Collection<? extends Type> items) throws NullPointerException
 	{
 		boolean flag = false;
 		for (Type i : items)
 			flag = this.add(i); //Simply uses the add() method. Item will not be added if it already exists in BST.
 
 		return flag;
 	}
 
 	/**
 	 * Removes all items from this set. The set will be empty after this method
 	 * call.
 	 */
 	public void clear()
 	{
 		this.root = null;
 		size = 0;
 		writeDot("treePic.dot");
 	}
 
 	/**
 	 * Determines if there is an item in this set that is equal to the specified
 	 * item.
 	 * 
 	 * @param item
 	 *          - the item sought in this set
 	 * @return true if there is an item in this set that is equal to the input
 	 *         item{ otherwise, returns false
 	 * @throws NullPointerException
 	 *           if the item is null
 	 */
 	public boolean contains(Type item) throws NullPointerException
 	{
 		if (item == null)
 			throw new NullPointerException();
 
 		//starting at the root
 		BinaryNode temp = root;
 
 		//Loop stops after it hits a leaf node.
 		while (temp != null)
 		{
			int result = temp.getData().compareTo(item);
			
			if (result < 0)
 				temp = temp.getRight();
			else if (result == 0)
 				return true; //Contains the item
			else if (result > 0)
				temp = temp.getLeft(); 
 		}
 
 		//Will reach this point when temp is null.
 		return false;
 
 	}
 
 
 	/**
 	 * Determines if for each item in the specified collection, there is an item
 	 * in this set that is equal to it.
 	 * 
 	 * @param items
 	 *          - the collection of items sought in this set
 	 * @return true if for each item in the specified collection, there is an item
 	 *         in this set that is equal to it{ otherwise, returns false
 	 * @throws NullPointerException
 	 *           if any of the items is null
 	 */
 	public boolean containsAll(Collection<? extends Type> items) throws NullPointerException
 	{	    	
 		for(Type item : items)
 		{
 			//			if (item == null)
 			//				throw new NullPointerException();
 
 			if (!this.contains(item))
 				return false;
 		}
 		return true;
 
 	}
 
 	/**
 	 * Returns the first (i.e., smallest) item in this set.
 	 * 
 	 * @throws NoSuchElementException
 	 *           if the set is empty
 	 */
 	public Type first() throws NoSuchElementException
 	{
 		if(root == null)
 			throw new NoSuchElementException();
 
 		if(root.getLeft()!=null)
 			return root.getLeftmostNode().getData();
 		else
 			return root.getData();
 	}
 
 	/**
 	 * Returns true if this set contains no items.
 	 */
 	public boolean isEmpty()
 	{
 		if(root==null)
 			return true;
 		return false;
 
 	}
 
 	/**
 	 * Returns the last (i.e., largest) item in this set.
 	 * 
 	 * @throws NoSuchElementException
 	 *           if the set is empty
 	 */
 	public Type last() throws NoSuchElementException
 	{
 		if(root == null)
 			throw new NoSuchElementException();
 
 		if(root.getRight()!=null)
 			return root.getRightmostNode().getData();
 		else
 			return root.getData(); 
 	}
 
 	/**
 	 * Ensures that this set does not contain the specified item.
 	 * 
 	 * @param item
 	 *          - the item whose absence is ensured in this set
 	 * @return true if this set changed as a result of this method call (that is,
 	 *         if the input item was actually removed){ otherwise, returns false
 	 * @throws NullPointerException
 	 *           if the item is null
 	 */
 	public boolean remove(Type item) throws NullPointerException
 	{
 		if (item == null)
 			throw new NullPointerException();
 		//extra search
 		if (!this.contains(item))
 			return false;
 		else
 		{
 			//starting at the root
 			BinaryNode temp = root;
 			//deprecated use of parent node
 			BinaryNode parent;
 			int typeOfCase = 0; // 1 = leaf node, 2 = node with 1 child, 3 = node with 2 children
 			boolean movement = false; //false = last move was left; true = last move was right
 
 			int compare = temp.getData().compareTo(item);
 
 			if (compare == 0) //If the remove item is the root 
 			{
 				//If the root has no children, set it to null.
 				if (temp.getLeft() == null && temp.getRight() == null){
 					this.root = null;
 					writeDot("treePic.dot");
 					size--;
 					return true;
 				}
 
 				//Otherwise, set the root to its successor.
 				else
 					typeOfCase = 3;
 			}
 
 
 			parent = temp;
 			//Typical cases
 			while (typeOfCase == 0)
 			{
 
 				if (compare < 0)
 				{
 					parent = temp;
 					temp = temp.getRight();
 					movement = true;
 				}
 				else if (compare > 0)
 				{
 					parent = temp;
 					temp = temp.getLeft();
 					movement = false;
 				}
 				else //temp = the item
 					typeOfCase = temp.numChildren() + 1;
 
 				compare = temp.getData().compareTo(item);
 			}  
 
 			switch (typeOfCase) 
 			{
 
 			//if the node to be removed has no children
 			case 1: 
 				if (movement){
 					parent.setRight(null);
 					writeDot("treePic.dot");
 					size--;
 					return true;
 				}
 				else{
 					parent.setLeft(null);
 					writeDot("treePic.dot");
 					size--;
 					return true;
 				}
 
 				//if the node to be removed has one child
 			case 2: 
 				if (movement)
 				{
 					if (temp.getLeft() != null){
 						parent.setRight(temp.getLeft());
 						writeDot("treePic.dot");
 						size--;
 						return true;
 					}
 					else{
 						parent.setRight(temp.getRight());
 						writeDot("treePic.dot");
 						size--;
 						return true;
 					}
 
 				}  
 				else
 				{
 					if (temp.getRight() != null)
 						parent.setLeft(temp.getRight());
 					else
 						parent.setLeft(temp.getLeft());
 				}
 				break;
 
 				//if the node to be removed has two children
 			case 3: 
 				Type i = temp.getSuccessor().getData(); //find the successor: O(log N)
 				this.remove(i); //remove the successor
 				temp.setData(i); //set the data of the removal node to the data of the successor, effectively removing the desired node.
 				return true;
 
 				//Should not hit this case.
 			default:
 				return false;
 			}
 			size--;
 
 		}
 		//Should never hit this.
 		return false;
 	}
 	/**
 	 * Ensures that this set does not contain the specified item.
 	 * 
 	 * @param item
 	 *          - the item whose absence is ensured in this set
 	 * @return true if this set changed as a result of this method call (that is,
 	 *         if the input item was actually removed){ otherwise, returns false
 	 * @throws NullPointerException
 	 *           if the item is null
 	 */
 	public boolean doyleRemove(Type item) throws NullPointerException, NoSuchElementException{
 		bigFlag=false;
 		BinaryNode in = root;
 		recursiveRemove(in,item);
 
 		return bigFlag;
 
 	}
 	private BinaryNode recursiveRemove(BinaryNode curr, Type item){
 		//base case
 		if (curr == null)
 			return curr;//or maybe throw nse
 		//look for "it"
 		if (item.compareTo(curr.getData())<0)
 			curr.left = recursiveRemove(curr.left,item);
 		else if (item.compareTo(curr.getData())>0)
 			curr.right = recursiveRemove(curr.right,item);
 		//by now we've found it! base cases
 		//two children
 		else if(curr.left!=null&&curr.right!=null){
 			BinaryNode temp = curr.getSuccessor();
 			curr.data = temp.data;
 			if (temp.getLeft()!=null){
 				temp = temp.getLeft();
 			}
 			else{
 				temp = temp.right;
 			}
 			writeDot("treePic.dot");
 			size--;
 			bigFlag = true;
 		}
 		//maybe one child
 		else{
 			curr = (curr.left!=null)? curr.left:curr.right;
 			writeDot("treePic.dot");
 			size--;
 			bigFlag=true;
 		}
 		//return the appropriate node for the recursive calls
 		return curr;
 
 	}
 	/**
 	 * Ensures that this set does not contain any of the items in the specified
 	 * collection.
 	 * 
 	 * @param items
 	 *          - the collection of items whose absence is ensured in this set
 	 * @return true if this set changed as a result of this method call (that is,
 	 *         if any item in the input collection was actually removed){
 	 *         otherwise, returns false
 	 * @throws NullPointerException
 	 *           if any of the items is null
 	 */
 	public boolean removeAll(Collection<? extends Type> items) throws NullPointerException
 	{
 		boolean flag = false;
 		for (Type t : items)
 		{
 			if(!flag)
 				flag =this.doyleRemove(t);
 			else
 				doyleRemove(t);
 		}
 
 		return flag;
 
 	}
 
 	/**
 	 * Returns the number of items in this set.
 	 */
 	public int size(){
 		return size;
 	}
 
 	/**
 	 * Returns an ArrayList containing all of the items in this set, in sorted
 	 * order.
 	 * Driver Method.
 	 * @return sorted ArrayList of this collection's elements
 	 */
 	public ArrayList<Type> toArrayList()
 	{
 		ArrayList<Type> list = new ArrayList<Type>();
 		BinaryNode node = root;
 		toArrayList(node, list);
 		return list;
 	}
 
 	/**Recursive helper method for toArrayList
 	 * @param BinaryNode
 	 * @param ArrayList
 	 */
 	public void toArrayList(BinaryNode node, ArrayList<Type> list)
 	{
 		if (node == null)
 			return;
 		//in order DFT
 		toArrayList(node.getLeft(), list);
 		list.add(node.data);
 		toArrayList(node.getRight(), list);
 	}
 
 	/**Write method for .dot file
 	 * 
 	 * @param filename
 	 */
 	public void writeDot(String filename)
 	{
 		try {
 			PrintWriter output = new PrintWriter(new FileWriter(filename));
 			output.println("graph g {");
 			if(root != null)
 				output.println(root.hashCode() + "[label=\"" + root + "\"]");
 			writeDotRecursive(root, output);
 			output.println("}");
 			output.close();
 		}
 		catch(Exception e){e.printStackTrace();}
 	}
 
 	// Recursively traverse the tree, outputting each node to the .dot file
 	private void writeDotRecursive(BinaryNode n, PrintWriter output) throws Exception
 	{
 		if(n == null)
 			return;
 		if(n.left != null)
 		{
 			output.println(n.left.hashCode() + "[label=\"" + n.left + "\"]");
 			output.println(n.hashCode() + " -- " + n.left.hashCode());
 		}
 		if(n.right != null)
 		{
 			output.println(n.right.hashCode() + "[label=\"" + n.right + "\"]");
 			output.println(n.hashCode() + " -- " + n.right.hashCode());
 		}
 
 		writeDotRecursive(n.left, output);
 		writeDotRecursive(n.right, output);
 	}
 
 
 
 
 	private class BinaryNode 
 	{
 		// Since the outer BST class declares <Type>, we can use it here without redeclaring it for BinaryNode
 		Type data;
 		BinaryNode left;
 		BinaryNode right;
 		/**
 		 * Construct a new node with known children
 		 * 
 		 */
 		public BinaryNode(Type _data, BinaryNode _left, BinaryNode _right) 
 		{
 			data = _data;
 			left = _left;
 			right = _right;
 		}
 
 		/**
 		 * Construct a new node with no children
 		 * 
 		 */
 		public BinaryNode(Type _data) 
 		{
 			this(_data, null, null);
 		}
 
 		/**
 		 * Getter method.
 		 * 
 		 * @return the node data.
 		 */
 		public Type getData() 
 		{
 			return data;
 		}
 
 		/**
 		 * Setter method.
 		 * 
 		 * @param _data
 		 *          - the node data to be set.
 		 */
 		public void setData(Type _data) 
 		{
 			data = _data;
 		}
 
 		/**
 		 * Getter method.
 		 * 
 		 * @return the left child node.
 		 */
 		public BinaryNode getLeft() 
 		{
 			return left;
 		}
 
 		/**
 		 * Setter method.
 		 * 
 		 * @param _left
 		 *          - the left child node to be set.
 		 */
 		public void setLeft(BinaryNode _left) 
 		{
 			left = _left;
 		}
 
 		/**
 		 * Getter method.
 		 * 
 		 * @return the right child node.
 		 */
 		public BinaryNode getRight() 
 		{
 			return right;
 		}
 
 		/**
 		 * Setter method.
 		 * 
 		 * @param _right
 		 *          - the right child node to be set.
 		 */
 		public void setRight(BinaryNode _right) 
 		{
 			right = _right;
 		}
 
 		/**
 		 * Number of children
 		 * Use this to help figure out which BST deletion case to perform
 		 * 
 		 * @return The number of children of this node
 		 */
 		public int numChildren()
 		{
 			int numChildren = 0;
 
 			if(getLeft() != null)
 				numChildren++;
 			if(getRight() != null)
 				numChildren++;
 
 			return numChildren;
 		}
 		@Override
 		public String toString(){
 			return data.toString();
 		}
 
 
 		/**
 		 * @return The leftmost node in the binary tree rooted at this node.
 		 */
 		public BinaryNode getLeftmostNode() 
 		{
 			// Base case, done for you
 			if(getLeft() == null)
 				return this; // returns "this" node
 
 			// FILL IN - do not return null
 			return getLeft().getLeftmostNode();
 		}
 
 		/**
 		 * this method finds the right most node
 		 * @return The rightmost node in the binary tree rooted at this node.
 		 */
 		public BinaryNode getRightmostNode() 
 		{
 			// Base case, done for you
 			if(getRight() == null)
 				return this; // returns "this" node
 
 			// FILL IN - do not return null
 			return getRight().getRightmostNode();
 		}		
 
 		/** 
 		 * This method applies to binary search trees only (not general binary trees).
 		 * 
 		 * @return The successor of this node.
 		 *  
 		 * The successor is a node which can replace this node in a case-3 BST deletion.
 		 * It is either the smallest node in the right subtree,
 		 * or the largest node in the left subtree.
 		 */
 		public BinaryNode getSuccessor() 
 		{	
 			if(this.getRight() != null)
 				return this.getRight().getLeftmostNode();
 
 			else if(this.getLeft() != null)
 				return this.getLeft().getRightmostNode();
 			else 
 				return null;
 		}
 
 		/**
 		 * @return The height of the binary tree rooted at this node. The height of a
 		 * tree is the length of the longest path to a leaf node. Consider a tree with
 		 * a single node to have a height of zero.
 		 *  
 		 * The height of a tree with more than one node is the greater of its two subtrees'
 		 * heights, plus 1
 		 */
 		@SuppressWarnings("unused")
 		public int height() 
 		{			
 			int leftheight= 0 , rightheight = 0;
 			BinaryNode temp = this;
 			if(temp.getRight()==null&&temp.getLeft()==null)
 				return 0;
 			if(temp.getLeft() != null){
 				leftheight= temp.getLeft().height();
 			}
 			if(temp.getRight() != null){
 				rightheight = temp.getRight().height();
 			}
 			return Math.max(leftheight, rightheight)+1;
 		}
 	}	
 }
 
 
 
