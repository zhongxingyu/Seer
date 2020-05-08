 import java.util.ArrayList;
 import java.util.ConcurrentModificationException;
 import java.util.Iterator;
 import java.util.NoSuchElementException;
 import java.util.Stack;
 
 /**
  * A AVLTree implementation class
  * @author risdenkj
  * 
  */
 
 public class AVLTree<T extends Comparable<? super T>> implements Iterable<T> {
 
 	private AVLNode root;
 	private int modCount = 0;
 	private int rotationCount = 0;
 	
 	/**
 	 * Constructs a AVLTree
 	 * Sets the root to null
 	 */
 	public AVLTree() {
 		root = null;
 	}
 
 	/**
 	 * Checks if the AVLTree has no nodes
 	 * 
 	 * @return 	true if the AVLTree has no nodes; false if has nodes
 	 */
 	public boolean isEmpty() {
 		return root == null ? true : false;
 	}
 	
 	/**
 	 * Default iterator method returns the nodes in preorder
 	 * 
 	 * @return 	an iterator to traverse the nodes in preorder 
 	 */
 	public Iterator<T> iterator() {
 		return new preOrderTreeIterator(root);
 	}
 	
 	/**
 	 * Iterator that returns the nodes in inorder
 	 * 
 	 * @return 	an iterator to traverse the nodes in inorder
 	 */
 	public Iterator<T> inOrderIterator() {
 		return new inOrderTreeIterator(root);
 	}
 	
 	/**
 	 * Iterator that returns the nodes in preorder
 	 * 
 	 * @return 	an iterator to traverse the nodes in preorder
 	 */
 	public Iterator<T> preOrderIterator() {
 		return new preOrderTreeIterator(root);
 	}
 	
 	/**
 	 * Method that returns an ArrayList representation of the AVLTree
 	 * 
 	 * @return 	ArrayList with the nodes in order
 	 */
 	public ArrayList<T> toArrayList() {
 		if(root == null) {
 			return new ArrayList<T>();
 		}
 		return root.toArrayList(new ArrayList<T>());
 	}
 	
 	/**
 	 * Method that returns an Array representation of the AVLTree
 	 * 
 	 * @return 	Array with the nodes in order
 	 */
 	public Object[] toArray() {
 		return toArrayList().toArray();
 	}
 
 	/**
 	 * Method to determine the height of the AVLTree
 	 * 
 	 * @return 	height of the AVLTree; -1 if AVLTree is empty
 	 */
 	public int height(){
 		return !isEmpty() ? root.height() : -1;
 	}
 	
 	/**
 	 * Method that returns a String representation of the AVLTree
 	 * 
 	 * @return 	string in [element, element] format with the AVLTree AVLNodes in order 
 	 */
 	public String toString() {
 		String temp = "";
 		if(root == null) {
 			return temp;
 		}
 		Iterator<T> i = iterator();
 		while(i.hasNext()) {
 			temp += "[" + i.next() + "]";
 			if(i.hasNext()) {
 				temp += ", ";
 			}
 		}
 		return temp;
 	}
 	
 	//TODO REMOVE
 	public String toStringPre() {
 		String temp = "";
 		if(root == null) {
 			return temp;
 		}
 		Iterator<T> i = preOrderIterator();
 		while(i.hasNext()) {
 			temp += "[" + i.next() + "]";
 			if(i.hasNext()) {
 				temp += ", ";
 			}
 		}
 		return temp;
 	}
 	
 	/**
 	 * Method to determine the size of the AVLTree
 	 * 
 	 * @return 	size of the AVLTree; 0 if AVLTree is empty
 	 */
 	public int size() {
 		return !isEmpty() ? root.size() : 0;
 	}
 	
 	/**
 	 * Returns a boolean value representing whether the tree was modified
 	 * or not. The item argument must be of the same type that was used 
 	 * when initiating the AVLTree class.
 	 *
 	 * @param item	the item to be inserted into the AVLTree
 	 * @return      true if the tree was modified, false if not
 	 * @exception	IllegalArgumentException if item is null
 	 */
 	public boolean insert(T item) {
 		if(item == null) {
 			throw new IllegalArgumentException();
 		}
 		if(root != null) {
 			return root.insert(item);
 		} else {
 			root = new AVLNode(item);
 			modCount++;
 			return true;
 		} 
 	}
 	
 	/**
 	 * Removes the provided item from the AVLTree
 	 * 
 	 * @param item	the item that will be removed from the AVLTree
 	 * @return		true if remove successful; false if not
 	 * @exception	IllegalArgumentException if item is null
 	 */
 	public boolean remove(T item) {
 		modWrapper mod = new modWrapper();
 		if(item == null) {
 			throw new IllegalArgumentException();
 		}
 		if(root != null) {
 			root = root.remove(item, mod);
 		}
 		if(mod.getValue()) {
 			modCount++;
 		}
 		return mod.getValue();
 	}
 	
 	/**
 	 * Method that gets the rotationCount of the AVLTree
 	 * 
 	 * @return rotationCount The number of times the AVLTree has been rotated
 	 */
 	public int getRotationCount() {
 		return rotationCount;
 	}
 	
 	/**
 	 * Get method that returns a pointer to the item provided
 	 * 
 	 * @param item item to be found in the AVLTree
 	 * @return pointer to item if found; null if not found
 	 * @exception IllegalArgumentException if item is null
 	 */
 	public T get(T item) {
 		if(item == null) {
 			throw new IllegalArgumentException();
 		}
 		return root.get(item);
 	}
 	
 	/**
 	 * A AVLNode Implementation Class
 	 * @author risdenkj
 	 * 
 	 */
 	private class AVLNode {
 		private T element;
 		private AVLNode left;
 		private AVLNode right;
 		
 		/**
 		 * Constructs a AVLNode
 		 * Sets the left and right children to null
 		 * 
 		 * @param initelement	The element that becomes the AVLNode
 		 */
 		public AVLNode(T initelement) {
 			element = initelement;
 			left = null;
 			right = null;
 		}
 		
 		/**
 		 * Returns the string representation of the current AVLNode
 		 * 
 		 * @return	string of the current AVLNode
 		 */
 		public String toString() {
 			return element.toString();
 		}
 		
 		/**
 		 * Recursive method that returns an ArrayList of the AVLNode and its children
 		 * 
 		 * @param list	the ArrayList that elements should be added onto
 		 * @return 	ArrayList of the AVLNode and its children
 		 */
 		public ArrayList<T> toArrayList(ArrayList<T> list) {
 			if(left != null) {
 				left.toArrayList(list);
 			}
 			list.add(element);
 			if(right != null) {
 				right.toArrayList(list);
 			}
 			return list;
 		}
 		
 		/**
 		 * Method that determines the height of the AVLNode
 		 * 
 		 * @return 	height of the AVLNode
 		 */
 		public int height() {
 			int leftheight = 0, rightheight = 0;
 			if(left != null) {
 				leftheight = 1 + left.height();
 			}
 			if(right != null) {
 				rightheight = 1 + right.height();
 			}
 			if(leftheight > rightheight) {
 				return leftheight;
 			} else { 
 				return rightheight;
 			}
 		}
 		
 		/**
 		 * Method that determines the size of the AVLNode 
 		 * 
 		 * @return 	size of the AVLNode
 		 */
 		public int size() {
 			int size = 1;
 			if(left != null) {
 				size += left.size();
 			}
 			if(right != null) {
 				size += right.size();
 			}
 			return size;
 		}
 		
 		/**
 		 * Inserts the provided element as a child to the AVLNode
 		 * The item becomes a left child if less than current AVLNode
 		 * The item becomes a right child if greater than current AVLNode
 		 * If the insert is successful adds 1 to the modCount
 		 * 
 		 * @param item	item to be inserted as a child to the AVLNode
 		 * @return 	true if insert successful; false if not
 		 */
 		public boolean insert(T item) {
 			if(item.compareTo(element) < 0) {
 				if(left != null) {
 					boolean temp = left.insert(item);
 					int rightheight = 0;
 					if(right != null) {
 						rightheight = right.height()+1; 
 					}
 					if(left.height()+1 - rightheight == 2) {
 						if(item.compareTo(left.element) < 0) {
 							rotateRight(this);
 						} else {
 							rotateDoubleLeft(this);
 						}
 					}
 					
 					return temp;
 				} else {
 					left = new AVLNode(item);
 					modCount++;
 					return true;
 				}
 			} else if(item.compareTo(element) > 0) {
 				if(right != null) {
 					boolean temp = right.insert(item);
 					int leftheight = 0;
 					if(left != null) {
 						leftheight = left.height()+1; 
 					}
 					if(right.height()+1 - leftheight == 2) {
 						if(item.compareTo(right.element) > 0) {
 							rotateLeft(this);
 						} else {
 							rotateDoubleRight(this);
 						}
 					}
 					return temp;
 				} else {
 					right = new AVLNode(item);
 					modCount++;
 					return true;
 				}
 			} else {
 				return false;
 			}
 		}
 		
 		public AVLNode rotateRight(AVLNode node) {
 			//System.out.println("rotateRight");
 			AVLNode temp1 = node.left;
 			AVLNode temp2 = new AVLNode(node.element); 
 			temp2.right = node.right;
			temp2.left = temp1.right;
 			node.left = temp1.left;
 			node.element = temp1.element;
 			node.right = temp2;
 			rotationCount++;
 			return node;
 		}
 		
 		public AVLNode rotateDoubleRight(AVLNode node) {
 			//System.out.println("rotateDoubleRight");
 			rotateRight(node.right);
 			rotateLeft(node);
 			return node;
 		}
 		 
 		public AVLNode rotateLeft(AVLNode node) {
 			//System.out.println("rotateRight");
 			AVLNode temp1 = node.right;
 			AVLNode temp2 = new AVLNode(node.element); 
 			temp2.left = node.left;
			temp2.right = temp1.left;
 			node.right = temp1.right;
 			node.element = temp1.element;
 			node.left = temp2;
 			rotationCount++;
 			return node;
 		}
 		 
 		public AVLNode rotateDoubleLeft(AVLNode node) {
 			//System.out.println("rotateDoubleLeft");
 			rotateLeft(node.left);
 			rotateRight(node);
 			return node;
 		}
 		
 		/**
 		 * Removes the provided item from the AVLNode
 		 * In the event of the AVLNode having two children, the
 		 * algorithm finds the largest left child.
 		 * 
 		 * @param item 	the item that will be removed from the AVLNode
 		 * @param mod 	ModWrapper boolean that will be set to true if remove successful
 		 * @return 	AVLNode that is removed
 		 */
 		public AVLNode remove(T item, modWrapper mod) {
 			if(left == null && right == null) {
 				if(item.compareTo(element) == 0) {
 					mod.setTrue();
 					return null;
 				}
 				return this;
 			} else if(right == null) {
 				if(item.compareTo(element) < 0) {
 					left = left.remove(item, mod);
 				}
 				mod.setTrue();
 				return left;
 			} else if(left == null) {
 				if(item.compareTo(element) > 0) {
 					right = right.remove(item, mod);
 				}
 				mod.setTrue();
 				return right;
 			} else {
 				if(item.compareTo(element) > 0) {
 					right = right.remove(item,mod);
 				} else if(item.compareTo(element) < 0) {
 					left = left.remove(item, mod);
 				} else {
 					T temp = element;
 					AVLNode largestChildNode = findLargestChild(left);
 					element = largestChildNode.element;
 					largestChildNode.element = temp;
 					left = left.remove(temp, mod);
 				}
 				return this;
 			}
 		}
 		
 		/**
 		 * Method that finds the largest left child
 		 * 
 		 * @param node	AVLNode to look for largest left child
 		 * @return 	the largest left child of the provided AVLNode
 		 */
 		public AVLNode findLargestChild(AVLNode node) {
 			while(node.right != null) {
 				node = node.right;
 			}
 			return node;
 		}
 		
 		/**
 		 * Get method that returns a pointer to the item provided
 		 * 
 		 * @param item item to be found in the AVLNode
 		 * @return pointer to item if found; null if not found
 		 */
 		public T get(T item) {
 			if(item.compareTo(element) > 0) {
 				return right.get(item);
 			} else if(item.compareTo(element) < 0) {
 				return left.get(item);
 			} else {
 				return element;
 			}
 		}
 	}
 	
 	/**
 	 * Creates a wrapper for the mod boolean
 	 * @author risdenkj
 	 * 
 	 */
 	private class modWrapper {
 		private boolean mod = false;
 		
 		public void setTrue() {
 			this.mod = true;
 		}
 		
 		public boolean getValue() {
 			return mod;
 		}
 	}
 	
 	/**
 	 * A preorder AVLTree iterator implementation class
 	 * @author risdenkj
 	 * 
 	 */
 	private class preOrderTreeIterator implements Iterator<T> {
 		private Stack<AVLNode> list = new Stack<AVLNode>();
 		private AVLNode node = null;
 		private int mod;
 		
 		/**
 		 * Constructs a preOrderTreeIterator
 		 * Sets the modification boolean flag to false
 		 * 
 		 * @param node	AVLNode to start the iterator from
 		 */
 		public preOrderTreeIterator(AVLNode node) {
 			if(node != null) {
 				list.push(node);
 				this.mod = modCount;
 			}
 		}
 		
 		/**
 		 * Checks if there is another element in the AVLTree that hasn't been accessed
 		 * 
 		 * @return	true if there is another element to return; false if not
 		 */
 		public boolean hasNext() {
 			return !list.empty();
 		}
 
 		/**
 		 * Method that returns the next AVLNode element from the AVLTree
 		 * 
 		 * @return AVLNode	element in the AVLTree
 		 * @exception 	ConcurrentModificationException if the AVLTree was modified after initializing the iterator
 		 * @exception 	NoSuchElementException if there are no more elements to return
 		 */
 		public T next() {
 			if(this.mod != modCount) {
 				throw new ConcurrentModificationException();
 			}
 			AVLNode item = null;
 			
 			if(!list.empty()) {
 				item = list.pop();
 			} else {
 				throw new NoSuchElementException();
 			}
 			
 			if(item.right != null) {
 				list.push(item.right);
 			}
 			if(item.left != null) {
 				list.push(item.left);
 			}
 			node = item;
 			return item.element;
 		}
 		
 		/**
 		 * Removes an element from the AVLTree
 		 * 
 		 * @exception	IllegalStateException if next() not called before
 		 */
 		public void remove() {
 			if(node == null) {
 				throw new IllegalStateException();
 			}
 			if(AVLTree.this.remove(node.element)) {
 				node = null;
 				mod++;
 			}
 		}
 	}
 	
 	/**
 	 * An in order AVLTree iterator implementation class
 	 * @author risdenkj
 	 * 
 	 */
 	private class inOrderTreeIterator implements Iterator<T> {
 		private Stack<AVLNode> list = new Stack<AVLNode>();
 		private AVLNode node = null;
 		private int mod;
 		
 		/**
 		 * Constructs an inOrderTreeIterator
 		 * Sets the modification boolean flag to false
 		 * 
 		 * @param node	AVLNode to start the iterator from
 		 */
 		public inOrderTreeIterator(AVLNode node) {
 			this.mod = modCount;
 			checkLeft(node);
 		}
 		
 		/**
 		 * Checks if there is another element in the AVLTree that hasn't been accessed
 		 * 
 		 * @return 	true if there is another element to return; false if not
 		 */
 		public boolean hasNext() {
 			return !list.empty();
 		}
 		
 		/**
 		 * Method that returns the next AVLNode element from the AVLTree
 		 * 
 		 * @return AVLNode	element in the AVLTree
 		 * @exception 	ConcurrentModificationException if the AVLTree was modified after initializing the iterator
 		 * @exception 	NoSuchElementException if there are no more elements to return
 		 */
 		public T next() {
 			if(this.mod != modCount) {
 				throw new ConcurrentModificationException();
 			}
 			AVLNode item = null;
 			if(list.empty()) {
 				throw new NoSuchElementException();
 			}
 			item = list.pop();
 			checkLeft(item.right);
 			node = item;
 			return item.element;
 		}
 
 		/**
 		 * Checks if the provided AVLNode has a left child
 		 * 
 		 * @param node	node to to check if it has a left child
 		 */
 		public void checkLeft(AVLNode node) {
 			while(node != null) {
 				list.push(node);
 				node = node.left;
 			}
 		}
 		
 		/**
 		 * Removes an element from the AVLTree
 		 * 
 		 * @exception	IllegalStateException if next() not called before
 		 */
 		public void remove() {
 			if(node == null) {
 				throw new IllegalStateException();
 			}
 			if(AVLTree.this.remove(node.element)) {
 				node = null;
 				mod++;
 			}
 		}
 	}
 }
