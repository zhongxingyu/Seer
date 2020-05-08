 /**
  * 
  */
 package datamodel.tree.sequential;
 
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import datamodel.tree.Tree;
 import datamodel.tree.TreeNode;
 
 /**
  * An implementation of the tree interface.<br>
  * uses a sequential construction
  * 
  * @author Jakob Karolus, Kevin Munk
  * @version 1.0
  * 
  */
 public class SequentialTree extends Tree {
 	private static final Logger logger =
 		Logger.getLogger(SequentialTree.class);
 
 
 	private Vector<TreeNode> nodes;
 	private int depth;
 	private TreeNode rootNode;
 
 	private TreeNode currentNode;
 	private int currentPosition;
 
 	/**
 	 * Constructor: Creates an empty sequential tree
 	 */
 	public SequentialTree() {
 		this.nodes = new Vector<TreeNode>(1);
 		this.rootNode = null;
 		this.currentNode = null;
 		this.currentPosition = 0;
 		this.depth = 0;
 	}
 
 	/**
 	 * Moves to the left child of the current node
 	 * 
 	 * @return true if left child exists and the move was successful; otherwise
 	 *         false
 	 */
 	public boolean moveToLeftNode() {
 		if(hasLeftNode()) {
 			currentPosition = 2 * currentPosition + 1;
 			currentNode = nodes.get(currentPosition);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Moves to the right child of the current node
 	 * 
 	 * @return true if right child exists and the move was successful; otherwise
 	 *         false
 	 */
 	public boolean moveToRightNode() {
 		if(hasRightNode()) {
 			currentPosition = 2 * currentPosition + 2;
 			currentNode = nodes.get(currentPosition);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Moves to the parent of the current node
 	 * 
 	 * @return true if parent exists and the move was successful; otherwise
 	 *         false
 	 */
 	public boolean moveToParentNode() {
 		if(hasParentNode()) {
 			currentPosition = getParentPosition();
 			currentNode = nodes.get(currentPosition);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * @return true if a nodes exists at the specified position
 	 */
 	private boolean nodeExists(int position) {
 		if(position >= 0 && position < nodes.size() && nodes.get(position) != null)
 			return true;
 		else
 			return false;
 	}
 
 	/**
 	 * @return true if left child exists; otherwise false
 	 */
 	public boolean hasLeftNode() {
 		return nodeExists(2 * currentPosition + 1);
 	}
 
 	/**
 	 * @return true if right child exists; otherwise false
 	 */
 	public boolean hasRightNode() {
 		return nodeExists(2 * currentPosition + 2);
 	}
 
 	/**
 	 * @return the parent position of the current node
 	 */
 	public int getParentPosition() {
 		if(currentPosition == 0)
 			return -1;
 		else
 			return currentPosition % 2 == 0 ? currentPosition / 2 - 1 : currentPosition / 2;
 	}
 
 	/**
 	 * @return true if parent exists; otherwise false
 	 */
 	public boolean hasParentNode() {
 		return nodeExists(getParentPosition());
 	}
 
 	/**
 	 * Sets a node at the specified position, if the node opens a new
 	 * depth of the tree, the vector is expanded to hold the new node.
 	 *
 	 * @return true if successful; otherwise false (no root set)
 	 *
 	 */
 	private boolean setNodeAt(TreeNode node, int newPosition) {
 		if(currentNode != null) {
 			int size    = nodes.size();
 
 			// if the node opens a new depth
 			if(newPosition >= size) {
 				depth++;
 				nodes.setSize(size + new Long((long)Math.pow(2, depth)).intValue());
 			}
 
 			nodes.insertElementAt(node, newPosition);
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * Sets the left child of the current node
 	 * 
 	 * @return true if successful; otherwise false (no root set)
 	 * 
 	 */
 	public boolean setLeftNode(TreeNode node) {
 		return setNodeAt(node, 2 * currentPosition + 1);
 	}
 
 	/**
 	 * Sets the right child of the current node
 	 * 
 	 * @return true if successful; otherwise false (no root set)
 	 * 
 	 */
 	public boolean setRightNode(TreeNode node) {
 		return setNodeAt(node, 2 * currentPosition + 2);
 	}
 
 	/**
 	 * Sets the current node. If the tree is empty, sets the root.
 	 * 
 	 */
 	public void setCurrentNode(TreeNode node) {
 		if(currentPosition == 0) {
 			rootNode = node;
 			currentNode = rootNode;
 		}
 		setNodeAt(node, currentPosition);
 	}
 
 	/**
 	 * @return the current node or null if the tree is empty
 	 */
 	public TreeNode getCurrentNode() {
 		return currentNode;
 	}
 
 	/**
 	 * moves to the root node of this tree
 	 * 
 	 * @return true if there's a root; otherwise false
 	 */
 	public boolean moveToRoot() {
 		if(rootNode != null) {
 			currentNode = rootNode;
 			currentPosition = 0;
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	/**
 	 * clears the whole tree, which includes deleting all nodes and the root
 	 * node
 	 */
 	public void clearTree() {
 		nodes.clear();
 		rootNode = null;
 		currentNode = null;
 		currentPosition = -1;
 	}
 	
 	/**
 	 * TODO: add Javadoc
 	 */
 	public boolean equals(Object o) {
 		//TODO: Implement me
 		throw new UnsupportedOperationException("Implement Me");
 	}
 
 	public String toString() {
 		return "Sequential Tree:\n"
 			+ "size: "+nodes.size()+"\n"
 			+ "capacity: "+nodes.capacity()+"\n"
 			+ "elements: "+nodes.toString();
 	}
 }
