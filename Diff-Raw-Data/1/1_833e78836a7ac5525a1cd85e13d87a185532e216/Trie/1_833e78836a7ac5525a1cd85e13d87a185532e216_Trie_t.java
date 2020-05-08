 /**
  * 
  */
 package datamodel.trie;
 
 import datamodel.RGB;
 
 /**
  * the whole trie<br>
  * provides communication functionality 
  * 
  * @author Jakob Karolus, Kevin Munk
  * @version 1.0
  *
  */
 public class Trie {
 	
 	private TrieNode root;
 	private TrieNode currentNode;
 	private int depth;
	//TODO check correct depth handling an passing
 	
 	
 	/**
 	 * The Trie-Constructor
 	 */
 	public Trie(){
 		root = new TrieNode(false, 0);
 		currentNode = root;
 		depth = 0;
 	}
 	
 	
 	/**
 	 * moves to the root-node
 	 * @return true if successful, false if not
 	 */
 	public boolean moveToRoot(){
 		
 		if		(root != null){
 					currentNode = root;
 					return true;}
 		
 		else		return false;
 	}
 	
 	
 	/**
 	 * moves to a child.node
 	 * @return true if successful, false if not
 	 */
 	public boolean moveToChild(int index){
 		
 		if		(currentNode.getNodeAtSlot(index) != null){
 					currentNode = currentNode.getNodeAtSlot(index);
 					return true;}
 		
 		else		return false;
 	}
 
 	
 	/**
 	 * creates a child at a given slot
 	 * @param index slot number
 	 * @param isLeaf is it a leaf or not?
 	 * @return successful?
 	 */
 	public boolean createChildAt(int index, boolean isLeaf){
 		
 		int childDepth = currentNode.getDepth() + 1;
 		TrieNode childNode = new TrieNode(isLeaf, childDepth);
 		depth = childDepth;
 		
 		return currentNode.setNodeAtSlot(index, childNode);
 	}
 	
 	
 	/**
 	 * sets a Leaf-value at a given slot
 	 * @param index slot number
 	 * @param value value
 	 * @return successful?
 	 */
 	public boolean setLeafSlot(int index, Integer value){
 
 		return currentNode.setLeafValueAtSlot(index, value);
 		
 	}
 	
 	
 	/**
 	 * calculates the missing depth of a color in Trie
 	 * @param color color to check
 	 * @return missing depth, 0 if color completely in Trie
 	 */
 	public int getMissingDepth(RGB color){
 		moveToRoot();
 		int missing = depth+1;
 		
 		for	(int i = 0; i < depth; i++){
 				int present_key = color.getTrieKeyForDepth(i);
 				
 				if		(currentNode.hasNodeAtSlot(present_key)){
 								missing--;
 								this.moveToChild(present_key);}
 				else	return missing;
 				}
 		return 0;
 	}
 	
 	
 	/**
 	 * get current node
 	 * @return current node
 	 */
 	public TrieNode getCurrentNode(){
 		return currentNode;
 	}
 	
 	public void setCurrentNode(TrieNode node){
 		currentNode = node;
 	}
 	
 	
 	/**
 	 * set the depth of this Trie manually
 	 * @param depth depth
 	 */
 	public void setDepth(int depth){
 		this.depth = depth;
 	}
 	
 	
 	/**
 	 * returns Trie's depth
 	 * @return depth
 	 */
 	public int getDepth(){
 		return depth;
 	}
 	
 	
 	/**
 	 * get root
 	 * @return root node
 	 */
 	public TrieNode getRoot(){
 		return root;
 	}
 	
 	
 	
 	
 }
