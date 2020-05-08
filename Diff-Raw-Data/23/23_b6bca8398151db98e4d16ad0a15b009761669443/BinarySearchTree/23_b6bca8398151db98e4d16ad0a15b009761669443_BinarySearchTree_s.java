 package edu.puc.concurrentavl;
 import java.io.PrintStream;
 
 public class BinarySearchTree implements ISearchTree {
 
 	public final int LEFT = 0;
 	public final int RIGHT = 1;
 
 	protected BinarySearchTree parent = null;
 	protected int value;
 	protected boolean hasValue = false;
 	protected BinarySearchTree[] children = new BinarySearchTree[2];
 	protected int height = 0;
 	protected boolean unbalanced = false;
 	
 	public BinarySearchTree() {
 		
 	}
 	
 	public BinarySearchTree(BinarySearchTree parent, int value) {
 		
 		this.value = value;
 		this.hasValue = true;
 		this.parent = parent;
 		
 	}
 
 	@Override
 	public void delete(int delVal) {
 
 		int numChildren = numChildren();
 
 		//Root node
 		if(this.parent == null && this.value == delVal && numChildren == 0)
 		{
 			this.hasValue = false;
 			return;
 		}
 
 		if(value == delVal)
 		{
 
 			
 			if(numChildren == 2)
 			{
 				System.out.println("Deleting " + value + " with numChildren = " + numChildren);
 				BinarySearchTree successor = children[RIGHT].findMin();
 				System.out.println("Successor: " + successor.value);
 				this.value = successor.value;
 				successor.delete(successor.value);
 			}
 			else if(numChildren == 1) 
 			{
 
 				BinarySearchTree child = children[LEFT] != null ? children[LEFT] : children[RIGHT];
 				//Check if root
 				if(parent != null)
 				{
 					int directionFromParent = this.equals(parent.children[LEFT]) ? LEFT : RIGHT;
 					parent.children[directionFromParent] = child;
 				}
 
 				child.parent = this.parent;
 			}
 			else 
 			{	
 				int directionFromParent = this.equals(parent.children[LEFT]) ? LEFT : RIGHT;
 				parent.children[directionFromParent] = null;
 				this.parent = null;
 			}
 		}
 
 		int direction = delVal < value ? LEFT : RIGHT;
 		BinarySearchTree child = children[direction];
 		if(child != null)
 			child.delete(delVal);
 
 
 	}	
 
 
 	@Override
 	public boolean find(int value) {	
 
 		int nodeVal = this.value;		
 		if(value == nodeVal) 
 			return true;	
 
 		else if(children[LEFT] != null && value < nodeVal )
 			return children[LEFT].find(value);
 
 		else if(children[RIGHT] != null)
 			return children[RIGHT].find(value);
 
 		return false;
 	}
 
 	protected BinarySearchTree findMin()
 	{
 		if(children[LEFT] != null)
 			return children[LEFT].findMin();
 		else
 			return this;
 	}
 
 	@Override
 	public void insert(int newValue) {
 
 		//Empty tree
 		if(this.parent == null && !hasValue)
 		{
 			this.value = newValue;
 			this.hasValue = true;
 			return;
 		}
 
 		if(this.value == newValue)
 			return;
 
 		int direction = newValue < value ? LEFT : RIGHT;
 		BinarySearchTree child = children[direction];
 		if(child == null)
 		{			
 			if (children[direction ^ 1] == null) 
 				++ height;
 			
 			createChildNode(direction, newValue);			
 		}
 		else
 		{
 			child.insert(newValue);		
 		}
		if(parent == null)
 		{
 			this.print();
 		}
 	}
 
 	protected void createChildNode(int direction, int newValue)
 	{
 		children[direction] = new BinarySearchTree(this, newValue);
 		
 	}
 	
 	
 	public boolean isValid()
 	{
 		boolean isValid = true;
 
 		if(children[RIGHT] != null)
 			isValid = children[RIGHT].value > value && isValid;
 
 			if(children[LEFT] != null)
 				isValid = children[LEFT].value < value && isValid;
 
 			return isValid;
 	}
 
 	protected int numChildren()
 	{
 		int numChildren = 0;
 		if(children[LEFT] != null)
 			++numChildren;
 		if(children[RIGHT] != null)
 			++numChildren;
 
 		return numChildren;
 	}
 
 	public void print()
 	{
 		print(System.out, "", "",  '-', RIGHT);
 	}
 
 	public void print(PrintStream ps)
 	{		
 		if(children[RIGHT] != null)
 			print(ps, "", "", '-', RIGHT);		
 
 	}
 
 	protected String replaceLastOf(String str, char ch, char newChar)
 	{
 		int idx = str.lastIndexOf(ch);
 		char[] arr = str.toCharArray();
 		arr[idx] = newChar;
 		return new String(arr);
 	}
 
 	protected void print(PrintStream ps, String whitespace, String prefix,  char padding, int lastDirection)
 	{		
 
 		int numChildren = numChildren();
 		int extraWidth = 0;
 		String newSpace = whitespace;
 
 		if(children[RIGHT] != null) 
 		{				
 			if(parent != null && lastDirection == RIGHT)
 			{
 				newSpace = replaceLastOf(whitespace, '|', ' ');
 			}
 
 			children[RIGHT].print(ps, newSpace + "          |", "----",  padding, RIGHT);	
 
 			for(int i = 0; i < extraWidth; ++ i)
 				ps.println(newSpace + "          |");
 		}
 
 		//ps.print(whitespace);
 		//ps.print(prefix);	
 
 
 		String numString = "" + value;
 		if(this.unbalanced) numString =  "[" + numString + "]";
 		this.unbalanced = false;
 		
 		while(numChildren > 0 && numString.length() + prefix.length() < 10)
 			numString += padding;
 		
 		
 		String line = whitespace + prefix + numString;
 		ps.println(line);
 
 		newSpace = whitespace;
 
 		if(children[LEFT] != null) 
 		{
 			if(parent != null && lastDirection == LEFT)
 			{
 				newSpace = replaceLastOf(whitespace, '|', ' ');
 			}
 			for(int i = 0; i < extraWidth; ++ i)
 				ps.println(newSpace + "          |");
 			children[LEFT].print(ps, newSpace + "          |", "----", padding, LEFT);
 
 
 		}
 	}
 
 	public void printOrder()
 	{
 		printOrder(System.out);
 	}
 
 	public void printOrder(PrintStream ps)
 	{
 		if(children[LEFT] != null)
 			children[LEFT].printOrder(ps);
 
 		ps.println(value);
 
 		if(children[RIGHT] != null)
 			children[RIGHT].printOrder(ps);
 	}
 
 }
