 package dataCollections;
 
 import interfaces.*;
 import exceptions.*;
 import app.KeyedItem;
 
//removeItem needs to be modified later11
 public class MyBST<T extends KeyedItem<KT>,KT extends Comparable<? super KT>>
 implements MyBSTInterface<T,KT>{
 	
 	private BSTNode<T> root; //referencing the root of the BST
 	private int num;	  // current number of elements stored in the BST
 	
 	//default constructor
 	public MyBST(){
 		root = null;
 		num = 0;
 	}
 	
 	public MyBST(T rootItem){
 		root = new BSTNode<T>(rootItem,null,null);
 		num =1;
 	}//end of constructors
 	
 	
 	// Description Inserts an element into a BST. Each element in the BST has a
     //             distinctunique search key.
     // Precondition: element is not already in the BST. No duplicated elements allowed.
     // Postcondition: If element does not already exist in the BST, then it
     //                has been added to the BST at its proper location, based on
     //                the element's search key. Otherwise, an exception is thrown.
 	// Exception: Throws the exception ElementAlreadyExistsException if element is 
     //            already exists in the BST.
 	public void insert(T element) throws ElementAlreadyExistsException{
 		
 		//call the utility insertItem function:
 		root = insertItem(root,element);
 		return;
 	}
 	
 	//Utility method insertItem:
 	protected BSTNode<T> insertItem(BSTNode<T> targetNode,T newItem)
 			throws ElementAlreadyExistsException{
 	
 		//no such element exist, so insert
 		if(targetNode ==null){
 			targetNode = new BSTNode<T>(newItem,null,null);
 			num++;
 			return targetNode;
 		}
 		
 		//if the searchKey for newItem < targetNode.getElement, go to its left node
 		if(newItem.getKey().compareTo(targetNode.getElement().getKey())<0){
 			targetNode.setLeft(insertItem(targetNode.getLeft(),newItem));
 			return targetNode;
 		}else if(newItem.getKey().compareTo(targetNode.getElement().getKey())==0){
 			throw new ElementAlreadyExistsException("This element already exists.");
 		}else{
 			//(newItem.getKey().compareTo(targetNode.getElement().getKey())>0){
 			targetNode.setRight(insertItem(targetNode.getRight(),newItem));
 			return targetNode;
 		}
 	
 	}//end of insert
 	
 
 	//Two ways to remove the Tree Item
 	public void remove(T element) 
 			throws ElementNotFoundInTreeException,EmptyTreeException{
 
 		if(num == 0){
 			throw new EmptyTreeException("There is no element in the list.");
 		}else{
 			root = removeItem(root,element.getKey());
 		}
 		
 		return;
 	}
 	
 
 	public void remove(KT searchKey) 
 			throws ElementNotFoundInTreeException,EmptyTreeException{
 		if(num == 0){
 			throw new EmptyTreeException("There is no element in the list.");
 		}else{
 			root = removeItem(root,searchKey);
 		}
 		
 		return;
 	}
 	
 	//Utility method: removeItem 
 	protected BSTNode<T> removeItem(BSTNode<T> targetNode,KT searchKey)
 			throws ElementNotFoundInTreeException,EmptyTreeException{
 		BSTNode<T> updatedTree=null;
 		
 		if(targetNode == null){
 			//element is not found
 			throw new ElementNotFoundInTreeException("This element doesn't exist in the list.");
 		}else{
 			//if the searchKey for newItem < targetNode.getElement, go to its left node
 			if(searchKey.compareTo(targetNode.getElement().getKey())<0){
 				//search the left subtree
 				updatedTree = removeItem(targetNode.getLeft(),searchKey);
				return updatedTree;
 			}else if(searchKey.compareTo(targetNode.getElement().getKey())==0){
 				//element found!
				updatedTree = removeNode(targetNode);
 				num--;
				return updatedTree;
 			}else{
 				//(searchKey.compareTo(targetNode.getElement().getKey())>0){
 				//search the right subtree
 				updatedTree = removeItem(targetNode.getRight(),searchKey);
				return updatedTree;
 			}

 		}
 		
 	}//end of remove
 	
 	protected BSTNode<T> removeNode(BSTNode<T> targetNode){
 		//When deleting a specific node, there are 3 cases:
 		//1. The node has no child
 		//2.a. The node has a left child
 		//  b. The node has a right child
 		//3. The node has left and right child
 		
 		//case 1:
 		if(targetNode.getLeft()==null&&targetNode.getRight()==null){
 			targetNode = null;
 			return targetNode;
 		}
 		
 		//case 2a:
 		else if(targetNode.getLeft() !=null && targetNode.getRight()==null){
 			targetNode = targetNode.getLeft();
 			return targetNode;
 		}
 		
 		//case 2b:
 		else if(targetNode.getLeft()==null&&targetNode.getRight()!=null){
 			targetNode = targetNode.getRight();
 			return targetNode;
 		}
 		
 		//case3
 		else{
 			//Important note: when switching the targetNode with its inoreder successor,
 			// switch the element of the Node instead of the Node itself!
 			targetNode.setElement( retrieveMin(targetNode.getRight()).getElement());
 			targetNode.setRight(removeMin(targetNode.getRight()));
 			
 			return targetNode;
 		}
 		
 	}
 	
 	public T retrieve(T element) 
 			throws ElementNotFoundInTreeException,EmptyTreeException{		
 		T theElement;
 		if(num == 0){
 			throw new EmptyTreeException("There is no element in the list.");
 		}else{
 			theElement =retrieveItem(root,element.getKey());
 		}
 		
 		return theElement;
 		
 	}
 
 	public T retrieve(KT searchKey) 
 			throws ElementNotFoundInTreeException,EmptyTreeException{		
 		T theElement;
 		if(num==0){
 			throw new EmptyTreeException("There is no element in the list.");
 		}else{
 			theElement =retrieveItem(root,searchKey);
 		}
 		
 		return theElement;
 		
 	}
 
 	
 	//Utility methodL retrieveItem:
 	protected T retrieveItem(BSTNode<T> targetNode,KT searchKey)
 			throws ElementNotFoundInTreeException,EmptyTreeException{
 		T returnItem = null;
 		//no such element exist, so insert
 		if(targetNode ==null){
 			throw new ElementNotFoundInTreeException("This element doesn't exist in the list.");
 		}
 		
 		//if the searchKey for newItem < targetNode.getElement, go to its left node
 		if(searchKey.compareTo(targetNode.getElement().getKey())<0){
 			//search the left subtree
 			returnItem = retrieveItem(targetNode.getLeft(),searchKey);
 			
 			return returnItem;
 		}else if(searchKey.compareTo(targetNode.getElement().getKey())==0){
 			//element found!
 			return targetNode.getElement();
 		}else{
 			//(newItem.getKey().compareTo(targetNode.getElement().getKey())>0){
 			//search the right subtree
 			returnItem = retrieveItem(targetNode.getRight(),searchKey);
 			return returnItem;
 		}
 
 	}
 	
 	//returns the node contains the max searchKey in the specified tree
 	protected BSTNode<T> retrieveMax(BSTNode<T> targetNode){
 		BSTNode<T> max=null;
 
 			if(targetNode.getRight()==null){
 				//there is no right subtree
 				//this node contains the max searchKey
 				max = targetNode;
 			}else{
 				max = retrieveMax(targetNode.getRight());
 			}
 		
 		return max;
 	}
 	
 	//returns the node contains the min searchKey in the specified tree
 	protected BSTNode<T> retrieveMin(BSTNode<T> targetNode){
 		BSTNode<T> min=null;
 		
 			if(targetNode.getLeft()==null){
 				//there is no left subtree
 				//this node contains the min searchKey
 				min = targetNode;
 			}else{
 				min = retrieveMin(targetNode.getLeft());
 			}
 		
 	
 		return min;
 	}
 	
 	protected BSTNode<T> removeMax(BSTNode<T> targetNode){
 	
 			if(targetNode.getRight()==null){
 				//there is no right subtree
 				//this node contains the max searchKey
 				targetNode = targetNode.getLeft();
 			}else{
 				targetNode.setRight( removeMax(targetNode.getRight()));
 			}
 			return targetNode;
 		
 	}
 	
 	protected BSTNode<T> removeMin(BSTNode<T> targetNode){
 		
 			if(targetNode.getLeft()==null){
 				//there is no left subtree
 				//this node contains the min searchKey
 				targetNode = targetNode.getRight();
 			}else{
 				targetNode.setLeft(removeMin(targetNode.getLeft()));
 			}
 			return targetNode;
 		
 	}
 
 	
 	public int size(){
 		return num;
 	}
 	
 	public void removeAll(){
 		root = null;
 		num = 0;
 		return;
 	}
 	
 
 }
