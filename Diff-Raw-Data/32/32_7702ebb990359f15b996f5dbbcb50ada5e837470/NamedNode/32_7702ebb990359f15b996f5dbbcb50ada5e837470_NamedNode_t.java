 package com.evervoid.utils.namedtree;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.evervoid.utils.BadInitialization;
 
 /**
  * NamedNodes are the nodes of a tree with n nodes. Associations to children nodes are named and must be unique. Each node also
  * contains a reference to the parent node.
  * 
  * @param <T>
  *            The type of the values to be stored in each node within this tree.
  */
 public class NamedNode<T>
 {
 	/**
 	 * The parent node; null if this node is the head of a tree.
 	 */
 	public NamedNode<T> aParent;
 	/**
 	 * A map from names to children nodes.
 	 */
 	private final Map<String, NamedNode<T>> fChildren;
 	/**
 	 * The name of the node.
 	 */
 	private final String fName;
 	/**
 	 * The value stored by this node.
 	 */
 	private final T fValue;
 
 	/**
 	 * Creates a new node and notifies the parent.
 	 * 
 	 * @param pParent
 	 *            The node to set as parent after creating this node.
 	 * @param pName
 	 *            The name associated with this node in the parent map.
 	 * @param pValue
 	 *            The value to be stored in this node.
 	 * @throws BadInitialization
 	 *             If the parent name is already taken in the parent node.
 	 */
 	public NamedNode(final NamedNode<T> pParent, final String pName, final T pValue) throws BadInitialization
 	{
 		this(pName, pValue);
 		if (pParent.addChild(this)) {
 			aParent = pParent;
 		}
 		else {
 			throw new BadInitialization("Name taken in parent");
 		}
 	}
 
 	/**
 	 * Sets this Node as the head of a tree with the given value.
 	 * 
 	 * @param pName
 	 *            The name of the node.
 	 * @param pValue
 	 *            The value to be stored in this node.
 	 */
 	public NamedNode(final String pName, final T pValue)
 	{
 		fName = pName;
 		fValue = pValue;
 		fChildren = new HashMap<String, NamedNode<T>>();
 	}
 
 	/**
 	 * Adds the parameter node to this one as a child with the parameter name if it isn't already taken.
 	 * 
 	 * @param pNode
 	 *            The node to add as a child.
 	 * @return Whether the node was added.
 	 */
 	public boolean addChild(final NamedNode<T> pNode)
 	{
 		if (fChildren.containsKey(pNode.getName())) {
 			return false;
 		}
 		fChildren.put(pNode.getName(), pNode);
 		pNode.setParent(this);
 		return true;
 	}
 
 	/**
 	 * Creates a node containing the value and adds it to this one as a child with the given name if it isn't already taken.
 	 * 
 	 * @param pName
 	 *            The name of the node to add.
 	 * @param pValue
 	 *            The node to add as a child.
 	 * @return The node if it was added, null otherwise.
 	 */
 	public NamedNode<T> addChild(final String pName, final T pValue)
 	{
 		NamedNode<T> node = null;
 		try {
 			node = new NamedNode<T>(this, pName, pValue);
 		}
 		catch (final BadInitialization e) {
 			return null;
 		}
 		return node;
 	}
 
 	/**
 	 * @param pNode
 	 *            The node to check against.
 	 * @return Whether this node this node's children contains the parameter value.
 	 */
 	public boolean childrenContain(final NamedNode<T> pNode)
 	{
 		return childrenContain(pNode.getValue());
 	}
 
 	/**
 	 * @param pValue
 	 *            The value to check against.
 	 * @return Whether any of this node's children contains the parameter value.
 	 */
 	public boolean childrenContain(final T pValue)
 	{
 		for (final NamedNode<T> node : fChildren.values()) {
 			if (node.contains(pValue) || node.childrenContain(pValue)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * @param pValue
 	 *            The value to check against.
 	 * @return Whether this node contains the parameter value.
 	 */
 	public boolean contains(final T pValue)
 	{
 		return fValue.equals(pValue);
 	}
 
 	/**
 	 * Searches for the value and returns the node name hierarchy if found. Helper function for findNodeHierarchy in order to
 	 * omit the name of this node.
 	 * 
 	 * @param pValue
 	 *            The value to look for.
 	 * @return The hierarchy.
 	 */
 	private List<String> findChildNodeHierarchy(final T pValue)
 	{
 		if (fValue.equals(pValue)) {
 			final List<String> result = new ArrayList<String>();
 			result.add(0, getName());
 			return result;
 		}
 		for (final NamedNode<T> node : fChildren.values()) {
 			final List<String> result = node.findChildNodeHierarchy(pValue);
 			if (result != null) {
 				result.add(0, getName());
 				return result;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Finds a node using the given hierarchy.
 	 * 
 	 * @param hierarchy
 	 *            The hierarchy leading down to the wanted node.
 	 * @return The node.
 	 */
 	public NamedNode<T> findNode(final List<String> hierarchy)
 	{
		return findNodeRecursive(new ArrayList<String>(hierarchy));
 	}
 
 	/**
 	 * Searches iteratively through the tree for a node containing the value. Returns that node, or null if no such node exists.
 	 * 
 	 * @param pValue
 	 *            The value to search for.
 	 * @return The node containing pValue or null if no such node.
 	 */
 	public NamedNode<T> findNode(final T pValue)
 	{
 		if (fValue.equals(pValue)) {
 			return this;
 		}
 		NamedNode<T> result = null;
 		for (final NamedNode<T> node : fChildren.values()) {
 			result = node.findNode(pValue);
 			if (result != null) {
 				return result;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Note: This node's name is omitted from the hierarchy.
 	 * 
 	 * @param pValue
 	 *            The value we are looking for.
 	 * @return A list of names corresponding to the node hierarchy on the way to the node containing the given value.
 	 */
 	public List<String> findNodeHierarchy(final T pValue)
 	{
 		if (fValue.equals(pValue)) {
 			return new ArrayList<String>();
 		}
 		for (final NamedNode<T> node : fChildren.values()) {
 			final List<String> result = node.findChildNodeHierarchy(pValue);
 			if (result != null) {
 				return result;
 			}
 		}
 		return null;
 	}
 
 	/**
	 * Helper function for findNode() so that the list can be cloned.
	 * 
	 * @param hierarchy
	 *            The hierarchy of the node to find. Will end up empty.
	 * @return The node.
	 */
	public NamedNode<T> findNodeRecursive(final List<String> hierarchy)
	{
		if (hierarchy.size() == 0) {
			return this;
		}
		final String childName = hierarchy.get(0);
		hierarchy.remove(0);
		return getChild(childName).findNode(hierarchy);
	}

	/**
 	 * @param pName
 	 *            The name of the child to return.
 	 * @return The child node associated with the given name, null if it doesn't exist.
 	 */
 	public NamedNode<T> getChild(final String pName)
 	{
 		return fChildren.get(pName);
 	}
 
 	/**
 	 * @return A collection of this node's children.
 	 */
 	public Collection<NamedNode<T>> getChildren()
 	{
 		return fChildren.values();
 	}
 
 	/**
 	 * @return The names of this node's children
 	 */
 	public Set<String> getChildrenNames()
 	{
 		return fChildren.keySet();
 	}
 
 	/**
 	 * @return The name of the node.
 	 */
 	public String getName()
 	{
 		return fName;
 	}
 
 	/**
 	 * @return The parent node, null if is head.
 	 */
 	public NamedNode<T> getParent()
 	{
 		return aParent;
 	}
 
 	/**
 	 * @return the value stored by this node.
 	 */
 	public T getValue()
 	{
 		return fValue;
 	}
 
 	/**
 	 * @return Whether the node is a head.
 	 */
 	public boolean isHead()
 	{
 		return aParent == null;
 	}
 
 	/**
 	 * @return Whether the node is a leaf.
 	 */
 	public boolean isLeaf()
 	{
 		return fChildren.isEmpty();
 	}
 
 	/**
 	 * @param pName
 	 *            The name to compare to.
 	 * @return Whether the node has the given name.
 	 */
 	public boolean isNamed(final String pName)
 	{
 		return fName.equalsIgnoreCase(pName);
 	}
 
 	/**
 	 * Sets the parameter node as the parent of this node.
 	 * 
 	 * @param pNode
 	 *            The node to set as a parent.
 	 */
 	private void setParent(final NamedNode<T> pNode)
 	{
 		aParent = pNode;
 	}
 }
