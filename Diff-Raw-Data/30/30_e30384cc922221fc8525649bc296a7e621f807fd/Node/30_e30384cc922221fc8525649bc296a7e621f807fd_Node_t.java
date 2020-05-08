 package util.ast.node;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 
 
 import back_end.Visitor;
 
 /**
  * A node within an AST. This is an abstract class that specifies behavior for
  * all node classes in Hog.
  * 
  * @author sam
  * 
  */
 public abstract class Node implements Comparable<Node> {
 
 	// logger used for all nodes
 	protected final static Logger LOGGER = Logger.getLogger(Node.class
 			.getName());
 	
 	protected static int nodeCount = 0;
 	
 
 	protected Node parent;
 	// Note: children are ordered left-to-right (first child is leftmost child).
 	protected List<Node> children;
 	protected int id;
 	
 	// for use in building symbol table tree
 	protected boolean newScope;
 
 	/**
 	 * Construct a new node. Creates an empty child list.
 	 */
 	public Node() {
 		this(new ArrayList<Node>());
 		this.id = nodeCount;
 		nodeCount++;
 	}
 
 	/**
 	 * Construct a new node with parent as it's parent.
 	 * 
 	 * @param parent
 	 *            the node that will become the parent of this node.
 	 */
 	public Node(List<Node> children) {
 		this.parent = null;
 		this.children = children;
 		this.newScope = false;
 		this.id = nodeCount;
 		nodeCount++;
 	}
 
 	/**
 	 * Call the appropriate method in the Visitor v for visiting this node.
 	 * 
 	 * @param v - the Visitor to visit this node.
 	 */
 	public abstract void accept(Visitor v);
 
 	public abstract int visitorTest(Visitor v);
 
 	/**
 	 * Test if a child is on this node's children list.
 	 * 
 	 * @param child - the potential child node.
 	 * @return true if child is in children, false otherwise.
 	 */
 	public boolean hasChild(Node child) {
 		if (children == null) {
 			return false;
 		}
 		return children.contains(child);
 	}
 
 	/**
 	 * Get all the children of this node, sorted in left-> order.
 	 * 
 	 * @return a list of all children of this node.
 	 */
 	public List<Node> getChildren() {
 		return children;
 	}
 	
 	/**
 	 * Returns true of the node has children
 	 * @return
 	 */
 	public boolean hasChildren() {
 		return !children.isEmpty();
 	}
 
 	/**
 	 * Add a child to this node. The new child will always been the rightmost
 	 * child in this node's subtree. NOTE: addChild implicitly sets child's
 	 * parent to be this, by calling setParent().
 	 * 
 	 * @param child
 	 *            The node to be added.
 	 * @throws UnsupportedOperationException
 	 *             If child already has a parent.
 	 * 
 	 */
 	public void addChild(Node child) throws UnsupportedOperationException {
 		// Node.LOGGER.info("Before adding child to Node: " + this.toString());
 		if (children == null) {
 			children = new ArrayList<Node>();
 		}
 		// don't add a null child
 		if (child == null) {
 			Node.LOGGER.fine(this.toString() + " was asked to add "
 					+ "a null child in addChild. Nothing happened.");
 		} else if (child.hasParent()) {
 			throw new UnsupportedOperationException("Cannot add child " + child
 					+ " to " + this + ", as it already has a parent ("
 					+ child.getParent() + ")!");
 		} else if (!children.contains(child)) {
 			children.add(child);
 			child.setParent(this);
 		}
 	}
 
 	/**
 	 * Remove a child from this node's children list. Implicitly unset's child's
 	 * parent.
 	 * 
 	 * @param child
 	 *            - the node to be removed
 	 * @return true if child was on this.children and was removed, false
 	 *         otherwise.
 	 */
 	public boolean removeChild(Node child) {
 		if (children == null) {
 			return false;
 		} else if (!children.contains(child)) {
 			return false;
 		}
 		child.unsetParent();
 		return true;
 	}
 
 	/**
 	 * Check if this node has a parent.
 	 * 
 	 * @return true if this has a parent, false otherwise.
 	 */
 	public boolean hasParent() {
 		return (this.parent != null);
 	}
 
 	/**
 	 * Get the parent of this node.
 	 * 
 	 * @return the parent of this node.
 	 */
 	public Node getParent() {
 		return parent;
 	}
 
 	/**
 	 * Sets the parent of this node to be the passed node, if this node doesn't
 	 * yet have a parent.
 	 * 
 	 * @param p
 	 *            - the proposed parent node
 	 * @throws UnsupportedOperationException
 	 *             if this node already has a parent.
 	 */
 	public void setParent(Node p) {
 		if (parent == null) {
 			parent = p;
 			return;
 		}
 		throw new UnsupportedOperationException(this.toString()
 				+ " already has a parent!");
 	}
 
 	public void unsetParent() {
 		parent.getChildren().remove(this);
 		parent = null;
 	}
 
 	/**
 	 * Get the identifying name of this node.
 	 * 
 	 * @return a string representation of the identifying name of this node.
 	 */
 	public abstract String getName();
 
 	/**
 	 * A convenient and concise String representation of this node.
 	 * 
 	 * Override getName() in any implementation of Node to change default
 	 * behavior.
 	 * 
 	 * @return a string representation of this node.
 	 */
 	@Override
 	public String toString() {
		return this.getName();
 	}
 
 	/**
 	 * Return a pretty-printed version of the children list.
 	 * 
 	 * @return A string representing the children list.
 	 */
 	public String getChildrenString() {
 
 		StringBuilder stringBuilder = new StringBuilder();
 
 		stringBuilder.append(this.getName());
 
 		stringBuilder.append(" Children: [");
 
 		for (Node child : children) {
 			stringBuilder.append(child.getName());
 			stringBuilder.append("; ");
 		}
 
 		if (!children.isEmpty()) {
 			stringBuilder.replace(stringBuilder.lastIndexOf("; "),
 					stringBuilder.length(), "");
 		}
 
 		stringBuilder.append("]");
 
 		return stringBuilder.toString();
 
 	}
 
 	/**
 	 * Compares this node to another node.
 	 * 
 	 * Comparison is done based on the names of the two nodes.
 	 */
 	@Override
 	public int compareTo(Node that) {
 		return this.getName().compareTo(that.getName());
 	}
 
 	/**
 	 * A pretty-printer for the subtree that this is a root of. Code taken from:
 	 * http
 	 * ://stackoverflow.com/questions/4965335/how-to-print-binary-tree-diagram
 	 * 
 	 */
 	public void print() {
 		print("", true);
 	}
 
 	/**
 	 * A pretty printer for the subtree that this is a root of. Code taken from:
 	 * http
 	 * ://stackoverflow.com/questions/4965335/how-to-print-binary-tree-diagram
 	 */
 	private void print(String prefix, boolean isTail) {
 		System.out
 				.println(prefix + (isTail ? "L__ " : "|-- ") + this.getName());
 		if (children != null) {
 			for (int i = 0; i < children.size() - 1; i++) {
 				children.get(i).print(prefix + (isTail ? "    " : "|   "),
 						false);
 			}
 			if (children.size() >= 1) {
 				children.get(children.size() - 1).print(
 						prefix + (isTail ? "    " : "|   "), true);
 			}
 		}
 	}
 	
 	public void setNewScope() {
 		this.newScope = true;
 	}
 	
 	/**
 	 * 
 	 * @return true if the node represents the start of a new scope
 	 */
 	public boolean isNewScope() {
 		return this.newScope;
 	}
 
 		/** Method to Print Java Source
 	 * 
 	 * @return
 	 */
 	public String toSource(){
 		return this.getName();
 	}
 
 }
