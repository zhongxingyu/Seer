 package archimate.util;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.eclipse.uml2.uml.NamedElement;
 import org.eclipse.uml2.uml.Stereotype;
 
 import archimate.codegen.ICodeElement;
 
 /**
  * This class implements the tree containing all <code>archiMateTags</code> in a
  * hierarchical structure
  * 
  * @author Samuel Esposito
  * 
  */
 public class TagTree {
 	/**
 	 * Tree root tag
 	 */
 	public static final String ROOT = "root";
 	// tree root
 	private TagNode root;
 	// currently selected node
 	private TagNode current;
 	// currently selected code
 	private ICodeElement currentCode;
 	// restricted interfaces
 	ArrayList<JavaClass> interfaces = new ArrayList<JavaClass>();
 	// restricted methods
 	ArrayList<JavaMethod> methods = new ArrayList<JavaMethod>();
 
 	/**
 	 * Creates a new tree, sets the tree root and marks it as selected
 	 */
 	public TagTree() {
 		root = new TagNode(ROOT);
 		current = root;
 	}
 
 	/**
 	 * Adds an interface name to the list of restricted interfaces
 	 * 
 	 * @param interfaceName
 	 *            name of the restricted interface
 	 * @param type
 	 *            the type of the interface
 	 * @param packageName
 	 *            the name of the package
 	 */
 	public void addRestrictedInterface(JavaClass restrictedInterface) {
 		interfaces.add(restrictedInterface);
 	}
 
 	/**
 	 * Adds an method to the list of restricted methods
 	 * 
 	 * @param method
 	 *            name of the restricted method
 	 * @param type
 	 *            the type of the class the method is defined in
 	 * @param packageName
 	 *            the package of the class the method is defined in
 	 */
 	public void addRestrictedMethod(JavaMethod restrictedMethod) {
 		methods.add(restrictedMethod);
 	}
 
 	/**
 	 * Returns the list of restricted interfaces
 	 * 
 	 * @return The list of restricted interfaces
 	 */
 	public ArrayList<JavaClass> restrictedInterfaces() {
 		return interfaces;
 	}
 
 	/**
 	 * Returns the list of restricted methods
 	 * 
 	 * @return The list of restricted methods
 	 */
 	public ArrayList<JavaMethod> restrictedMethods() {
 		return methods;
 	}
 
 	/**
 	 * @return the root of the tree
 	 */
 	public TagNode root() {
 		return root;
 	}
 
 	/**
 	 * Marks a node as selected
 	 * 
 	 * @param node
 	 *            the node you want to select
 	 */
 	public void setCurrent(TagNode node) {
 		if (node != null)
 			current = node;
 	}
 
 	/**
 	 * @return the node currently selected
 	 */
 	public TagNode current() {
 		return current;
 	}
 
 	/**
 	 * Sets the current code element to the given element
 	 * 
 	 * @param code
 	 *            the code element to be marked as current
 	 */
 	public void setCurrentCode(ICodeElement code) {
 		currentCode = code;
 	}
 
 	/**
 	 * Returns the currently selected code element
 	 * 
 	 * @return The currently selected code element
 	 */
 	public ICodeElement currentCode() {
 		return currentCode;
 	}
 
 	/**
 	 * Returns the number of nodes in the tree
 	 * 
 	 * @return The number of nodes in the tree
 	 */
 	public int nodeCount() {
 		return nodeCount(root);
 	}
 
 	// Recursively counts the number of nodes in the tree
 	private int nodeCount(TagNode node) {
 		int nodes = node.nrOfChildren();
 		for (Iterator<TagNode> iter = node.children().iterator(); iter
 				.hasNext();) {
 			nodes += nodeCount(iter.next());
 		}
 		return nodes;
 	}
 
 	/**
 	 * Searches for a node with a certain tag
 	 * 
 	 * @param tag
 	 *            The tag to match
 	 * @return A child having a matching tag
 	 */
 	public TagNode getNode(String tag) {
 		return getNode(root, tag);
 	}
 
 	/**
 	 * Searches for a node with a certain tag in the nodes children. If the node
 	 * is not found in the children, the children's children are searched.
 	 * 
 	 * @param node
 	 *            The node which children are searched
 	 * @param tag
 	 *            The tag to match
 	 * @return A child having a matching tag
 	 */
 	public TagNode getNode(TagNode node, String tag) {
 		for (TagNode child : node.children()) {
 			if (child.tag().equals(tag))
 				return child;
 		}
 		for (TagNode child : node.children()) {
 			TagNode found = getNode(child, tag);
 			if (found != null)
 				return found;
 		}
 		return null;
 	}
 
 	/**
 	 * Searches the children of the current node for all unvisited nodes
 	 * 
 	 * @return A list of nodes which are unvisited
 	 */
 	public ArrayList<TagNode> getUnvisited() {
 		ArrayList<TagNode> unvisited = new ArrayList<TagNode>();
 		for (Iterator<TagNode> iter = current.children().iterator(); iter
 				.hasNext();) {
 			TagNode child = iter.next();
 			if (!child.visited()) {
 				unvisited.add(child);
 			}
 		}
 		return unvisited;
 	}
 
 	/**
 	 * Searches the tree for all unvisited nodes
 	 * 
 	 * @return A list of nodes which are unvisited
 	 */
 	public ArrayList<TagNode> getAllUnvisited() {
 		ArrayList<TagNode> unvisited = new ArrayList<TagNode>();
 		for (Iterator<TagNode> iter = root.children().iterator(); iter
 				.hasNext();) {
 			TagNode child = iter.next();
 			if (!child.visited()) {
 				unvisited.add(child);
 			}
 			unvisited.addAll(getAllUnvisited(child));
 		}
 		return unvisited;
 	}
 
 	// Recursively searches for all unvisited nodes in a tree
 	private ArrayList<TagNode> getAllUnvisited(TagNode node) {
 		ArrayList<TagNode> unvisited = new ArrayList<TagNode>();
 		for (Iterator<TagNode> iter = node.children().iterator(); iter
 				.hasNext();) {
 			TagNode child = iter.next();
 			if (!child.visited()) {
 				unvisited.add(child);
 			}
 			unvisited.addAll(getAllUnvisited(child));
 		}
 		return unvisited;
 	}
 
 	/**
 	 * Marks all nodes in the tree as visited
 	 */
 	public void resetVisited() {
 		resetVisited(root);
 	}
 
 	// Recursive method setting the state of all nodes in a tree to unvisited
 	private void resetVisited(TagNode node) {
 		node.resetVisited();
 		for (Iterator<TagNode> iter = node.children().iterator(); iter
 				.hasNext();) {
 			resetVisited(iter.next());
 		}
 	}
 
 	/**
 	 * Searches the {@link TagTree} for a node containing an
 	 * {@link ICodeElement} with a UML element stereotyped by the given
 	 * stereotype
 	 * 
 	 * @param stereotype
 	 *            the stereotype to match
 	 * @return the list of found nodes
 	 */
 	public TagNode getNodeByStereotype(String stereotype) {
 		return getNodeByStereotype(root, stereotype);
 	}
 
 	// Searches the TagTree recursively for a node containing source with a UML
 	// element stereotyped by the given stereotype
 	private TagNode getNodeByStereotype(TagNode node, String stereotype) {
 		for (TagNode child : node.children()) {
 			if (checkNode(child, stereotype)) {
 				return child;
 			}
 			// recursively search the childs children
 			TagNode foundNode = getNodeByStereotype(child, stereotype);
 			if (foundNode != null)
 				return foundNode;
 		}
 		return null;
 	}
 
 	// Checks whether the node contains source with a UML element stereotyped by
 	// the given stereotype
 	private boolean checkNode(TagNode node, String stereotypeName) {
 		for (ICodeElement element : node.source()) {
 			for (NamedElement umlElement : element.umlElements()) {
 				for (Stereotype stereotype : umlElement.getAppliedStereotypes()) {
 					if (stereotype.getName().equals(stereotypeName)) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Deletes a node from the tree
 	 * 
 	 * @param node
 	 *            the node to delete
 	 */
 	public void dropNode(TagNode node) {
 		if (node != null) {
 			TagNode parent = node.parent();
 			if (parent != null) {
 				parent.children().remove(node);
 			}
 			node.setParent(null);
 			dropCode(node);
 		}
 	}
 
 	// Removes the source elements in the node from their parents
 	private void dropCode(TagNode node) {
 		for (ICodeElement code : node.source()) {
 			if (code.parent() != null) {
 				ICodeElement parent = code.parent();
 				parent.children().remove(code);
 				code.setParent(null);
 			}
 		}
 	}
 
 	/**
 	 * Deletes an {@link TagNode} from the {@link TagTree}
 	 * 
 	 * @param archiMateTag
 	 *            the archiMateTag of the {@link TagNode} to delete
 	 */
 	public void dropNode(String tag) {
 		dropNode(getNode(tag));
 	}
 
 	// Returns a list of all tree nodes and their state for debug purposes
 	public String toString() {
		return printRestrictions() + "\nTagTree:\n"
				+ printNodes(root.children(), " ");
 	}
 
 	// Returns a list of all restrictions for debug purposes
 	private String printRestrictions() {
 		String out = "\nRestricted Interfaces:\n";
 		for (JavaClass interfaceRestriction : interfaces) {
 			out += interfaceRestriction.toString();
 		}
 		out += "\nRestrictedMethods:\n";
 		for (JavaMethod methodRestriction : methods) {
 			out += methodRestriction.toString();
 		}
 		return out;
 	}
 
 	// Recursively returns the state of all nodes in the tree for debug purposes
 	private String printNodes(ArrayList<TagNode> children, String prefix) {
 		String out = "";
 		for (Iterator<TagNode> iter = children.iterator(); iter.hasNext();) {
 			TagNode node = iter.next();
 			out += prefix + node.toString();
 			out += printNodes(node.children(), prefix + "- ");
 		}
 		return out;
 	}
 
 }
