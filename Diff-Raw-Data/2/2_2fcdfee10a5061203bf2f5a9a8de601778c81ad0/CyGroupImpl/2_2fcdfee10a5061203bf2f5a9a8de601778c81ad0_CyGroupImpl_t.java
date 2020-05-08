 /* vim :set ts=2: */
 /*
   File: CyGroupImpl.java
 
   Copyright (c) 2007, The Cytoscape Consortium (www.cytoscape.org)
 
   The Cytoscape Consortium is:
   - Institute for Systems Biology
   - University of California San Diego
   - Memorial Sloan-Kettering Cancer Center
   - Institut Pasteur
   - Agilent Technologies
 
   This library is free software; you can redistribute it and/or modify it
   under the terms of the GNU Lesser General Public License as published
   by the Free Software Foundation; either version 2.1 of the License, or
   any later version.
 
   This library is distributed in the hope that it will be useful, but
   WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
   MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
   documentation provided hereunder is on an "as is" basis, and the
   Institute for Systems Biology and the Whitehead Institute
   have no obligations to provide maintenance, support,
   updates, enhancements or modifications.  In no event shall the
   Institute for Systems Biology and the Whitehead Institute
   be liable to any party for direct, indirect, special,
   incidental or consequential damages, including lost profits, arising
   out of the use of this software and its documentation, even if the
   Institute for Systems Biology and the Whitehead Institute
   have been advised of the possibility of such damage.  See
   the GNU Lesser General Public License for more details.
 
   You should have received a copy of the GNU Lesser General Public License
   along with this library; if not, write to the Free Software Foundation,
   Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
 package cytoscape.groups;
 
 import cytoscape.Cytoscape;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.CyEdge;
 
 import cytoscape.data.CyAttributes;
 
 import giny.model.RootGraph;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 
 /**
  * The CyGroup class provides the implementation for a group model that
  * maintains the list of nodes belonging to a group, the parent of a particular
  * group, and the node that represents the group.  Group information is stored
  * in the CyGroup itself, as well as in special group attributes that are associated
  * with the network, nodes, and edges involved.  These attributes provide a natural
  * mechanism for the saving and restoration of groups.  There are also opaque flags
  */
 public class CyGroupImpl implements CyGroup {
 	// Instance data
 
 	/**
 	 * The members of this group, indexed by the Node.
 	 */
 	private HashMap<CyNode, CyNode> nodeMap;
 
 	/**
 	 * The edges in this group that only involve members of this group
 	 */
 	private HashMap<CyEdge, CyEdge> innerEdgeMap;
 
 	/**
 	 * The edges in this group that involve members outside of this group
 	 */
 	private HashMap<CyEdge, CyEdge> outerEdgeMap;
 
 	/**
 	 * A map storing the list of edges for a node at the time it was
 	 * added to the group
 	 */
 	private HashMap<CyNode,List<CyEdge>> nodeToEdgeMap;
 
 	/**
 	 * The node that represents this group
 	 */
 	private CyNode groupNode = null;
 
 	/**
 	 * The group name
 	 */
 	private String groupName = null;
 
 	/**
 	 * Group state.  This is used by the view components to set the current
 	 * state of the group (collapsed, expanded, etc.)
 	 */
 	private int groupState = 0;
 
 	/**
 	 * viewValue is an opaque type for use by view compoenents to "remember"
 	 * information about the group
 	 */
 	private Object viewValue = null;
 
 	/**
 	 * viewer is the viewer that this group is managed by
 	 */
 	private String viewer = null;
 
 	// Public methods
 
 	/**
 	 * Empty constructor
 	 */
 	protected CyGroupImpl() {
 		this.nodeMap = new HashMap();
 		this.nodeToEdgeMap = new HashMap();
 		this.innerEdgeMap = new HashMap();
 		this.outerEdgeMap = new HashMap();
 	}
 
 	/**
 	 * Constructor to create an empty group.
 	 *
 	 * @param groupName the identifier to use for this group -- should be unique!
 	 */
 	protected CyGroupImpl(String groupName) {
 		this();
 		this.groupNode = Cytoscape.getCyNode(groupName, true);
 		this.groupName = groupName;
 	}
 
 	/**
 	 * Constructor to create an empty group when the group node is specified.
 	 *
 	 * @param groupNode the CyNode to use for this group
 	 */
 	protected CyGroupImpl(CyNode groupNode) {
 		this();
 		this.groupNode = groupNode;
 		this.groupName = this.groupNode.getIdentifier();
 	}
 
 	/**
 	 * Constructor to create a group with the listed nodes as initial members, and a predetermined
 	 * CyNode to act as the group Node.
 	 *
 	 * @param groupNode the group node to use for this group
 	 * @param nodeList the initial set of nodes for this group
 	 */
 	protected CyGroupImpl(CyNode groupNode, List nodeList) {
 		this(groupNode); // Create all of the necessary structures
 
 		Iterator iter = nodeList.iterator();
 
 		while (iter.hasNext()) {
 			this.addNodeToGroup ( (CyNode)iter.next() );
 		}
 	}
 
 	/**
 	 * Constructor to create a group with the listed nodes as initial members.
 	 *
 	 * @param groupName the identifier to use for this group -- should be unique!
 	 * @param nodeList the initial set of nodes for this group
 	 */
 	protected CyGroupImpl(String groupName, List nodeList) {
 		this(groupName); // Create all of the necessary structures
 
 		Iterator iter = nodeList.iterator();
 
 		while (iter.hasNext()) {
 			this.addNodeToGroup ( (CyNode)iter.next() );
 		}
 	}
 
 	/**
 	 * Return the name of this group
 	 */
 	public String getGroupName() {
 		return groupName;
 	}
 
 	/**
 	 * Get all of the nodes in this group
 	 *
 	 * @return list of nodes in the group
 	 */
 	public List<CyNode> getNodes() {
 		Collection<CyNode> v = nodeMap.values();
 
 		return new ArrayList<CyNode>(v);
 	}
 
 	/**
 	 * Get the CyNode that represents this group
 	 *
 	 * @return CyNode representing the group
 	 */
 	public CyNode getGroupNode() {
 		return this.groupNode;
 	}
 
 	/**
 	 * Get an iterator over all of the nodes in this group
 	 *
 	 * @return node iterator
 	 */
 	public Iterator<CyNode> getNodeIterator() {
 		Collection<CyNode> v = nodeMap.values();
 
 		return v.iterator();
 	}
 
 	/**
 	 * Get all of the edges completely contained within this group
 	 *
 	 * @return list of edges in the group
 	 */
 	public List<CyEdge> getInnerEdges() {
 		Collection<CyEdge> v = innerEdgeMap.values();
 
 		return new ArrayList<CyEdge>(v);
 	}
 
 	/**
 	 * Get all of the edges partially contained within this group
 	 *
 	 * @return list of edges in the group
 	 */
 	public List<CyEdge> getOuterEdges() {
 		Collection<CyEdge> v = outerEdgeMap.values();
 
 		return new ArrayList<CyEdge>(v);
 	}
 
 	/**
 	 * Add an outer edge to the map.  Some viewers may need to do this
 	 * if they add and remove edges, for example.
 	 *
 	 * @param edge the CyEdge to add to the outer edge map
 	 */
 	public void addOuterEdge(CyEdge edge) {
 		outerEdgeMap.put(edge, edge);
 	}
 
 	/**
 	 * Add an inner edge to the map.  Some viewers may need to do this
 	 * if they add and remove edges, for example.
 	 *
 	 * @param edge the CyEdge to add to the innter edge map
 	 */
 	public void addInnerEdge(CyEdge edge) {
 		innerEdgeMap.put(edge, edge);
 	}
 
 	/**
 	 * Determine if a node is a member of this group
 	 *
 	 * @param node the CyNode to test
 	 * @return true if node is a member of the group
 	 */
 	public boolean contains(CyNode node) {
 		if (nodeMap.containsKey(node))
 			return true;
 
 		return false;
 	}
 
 	/**
 	 * Set the state of the group
 	 *
 	 * @param state the state to set
 	 */
 	public void setState(int state) {
 		CyAttributes attributes = Cytoscape.getNodeAttributes();
 		this.groupState = state;
 		attributes.setAttribute(this.groupName, GROUP_STATE_ATTR, this.groupState);
 		attributes.setUserVisible(GROUP_STATE_ATTR, false);
 	}
 
 	/**
 	 * Get the state of the group
 	 *
 	 * @return group state
 	 */
 	public int getState() {
 		return this.groupState;
 	}
 
 	/**
 	 * Set the viewValue for the group
 	 *
 	 * @param viewValue the view value to set
 	 */
 	public void setViewValue(Object viewValue) {
 		this.viewValue = viewValue;
 	}
 
 	/**
 	 * Get the viewValue for the group
 	 *
 	 * @return the view value
 	 */
 	public Object getViewValue() {
 		return this.viewValue;
 	}
 
 	/**
 	 * Provide the default toString method
 	 *
 	 * @return group name
 	 */
 	public String toString() {
 		return this.groupName;
 	}
 
 	/**
 	 * Set the viewer for this group.  In order to maintain the
 	 * static tables correctly, this method is protected and
 	 * CyGroup.setGroupViewer(group, viewer, notify) should be used
 	 * instead.
 	 *
 	 * @param viewerName name of the viewer for the group
 	 */
 	protected void setViewer(String viewerName) {
 		CyAttributes attributes = Cytoscape.getNodeAttributes();
 		this.viewer = viewerName;
 
 		if (this.viewer != null) {
 			attributes.setAttribute(this.groupName, GROUP_VIEWER_ATTR, this.viewer);
 			attributes.setUserVisible(GROUP_VIEWER_ATTR, false);
 		}
 	}
 
 	/**
 	 * Get the name of the viewer for this group
 	 *
 	 * @return viewer for this group
 	 */
 	public String getViewer() {
 		return this.viewer;
 	}
 
 	/**
 	 * Add a new node to this group
 	 *
 	 * @param node the node to add
 	 */
 	public void addNode ( CyNode node ) {
 		// We need to go throught our outerEdgeMap first to see if this
 		// node has outer edges and proactively move them to inner edges.
 		// this needs to be done here because some viewers might have
 		// hidden edges on us, so the the call to getAdjacentEdgeIndices in
 		// addNodeToGroup won't return all of the edges.
 		List <CyEdge> eMove = new ArrayList<CyEdge>();
 		Iterator <CyEdge>edgeIter = outerEdgeMap.keySet().iterator();
 		while (edgeIter.hasNext()) {
 			CyEdge edge = edgeIter.next();
 			if (edge.getTarget() == node || edge.getSource() == node) {
 				eMove.add(edge);
 			}
 		}
 		edgeIter = eMove.iterator();
 		while (edgeIter.hasNext()) {
 			CyEdge edge = edgeIter.next();
 			outerEdgeMap.remove(edge);
 			innerEdgeMap.put(edge,edge);
 		}
 
 		// Note the cute little trick we play -- making sure these
 		// are added to the edgeMap
 		nodeToEdgeMap.put(node, eMove);
 
 		addNodeToGroup(node);
 
 		// Get our viewer
 		CyGroupViewer v = CyGroupManager.getGroupViewer(this.viewer);
 		if (v != null) {
 			// Tell the viewer that something has changed
 			v.groupChanged(this, node, CyGroupViewer.ChangeType.NODE_ADDED);
 		}
 	}
 
 
 	/**
 	 * Remove a node from a group
 	 *
 	 * @param node the node to remove
 	 */
 	public void removeNode ( CyNode node ) {
 		removeNodeFromGroup(node);
 		// Get our viewer
 		CyGroupViewer v = CyGroupManager.getGroupViewer(this.viewer);
 		if (v != null) {
 			// Tell the viewer that something has changed
 			v.groupChanged(this, node, CyGroupViewer.ChangeType.NODE_REMOVED);
 		}
 	}
 
 	/**
 	 * Add a new node to this group
 	 *
 	 * @param node the node to add
 	 */
 	private void addNodeToGroup ( CyNode node ) {
 		// Put this node in our map
 		nodeMap.put(node, node);
 		CyNetwork network = Cytoscape.getCurrentNetwork();
 		List <CyEdge>edgeList = null;
 
 		if (nodeToEdgeMap.containsKey(node)) {
 			edgeList = nodeToEdgeMap.get(node);
 		} else {
 			edgeList = new ArrayList<CyEdge>();
 		}
 
 		// Add all of the edges
 		int [] edgeArray = network.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(),true,true,true);
		if (edgeArray == null)
			edgeArray = new int[]{};
 		for (int edgeIndex = 0; edgeIndex < edgeArray.length; edgeIndex++) {
 			CyEdge edge = (CyEdge)network.getEdge(edgeArray[edgeIndex]);
 			// Not sure if this is faster or slower than going through the entire loop
 			if (edgeList.contains(edge))
 				continue;
 
 			edgeList.add(edge);
 			CyNode target = (CyNode)edge.getTarget();
 			CyNode source = (CyNode)edge.getSource();
 
 			// Check to see if this edge is one of our own metaEdges
 			if (source == groupNode || target == groupNode) {
 				// It is -- skip it
 				continue;
 			}
 
 			if (outerEdgeMap.containsKey(edge)) {
 				outerEdgeMap.remove(edge);
 				innerEdgeMap.put(edge,edge);
 			} else if (nodeMap.containsKey(target) && nodeMap.containsKey(source)) {
 				innerEdgeMap.put(edge,edge);
 			} else if (nodeMap.containsKey(target) || nodeMap.containsKey(source)) {
 				outerEdgeMap.put(edge,edge);
 			}
 		}
 		nodeToEdgeMap.put(node, edgeList);
 
 		// Tell the node about it (if necessary)
 		if (!node.inGroup(this))
 			node.addToGroup(this);
 	}
 
 	/**
 	 * Remove a node from a group
 	 *
 	 * @param node the node to remove
 	 */
 	private void removeNodeFromGroup ( CyNode node ) {
 		// Remove the node from our map
 		nodeMap.remove(node);
 
 		RootGraph rg = node.getRootGraph();
 
 		// Get the list of edges
 		List <CyEdge>edgeArray = nodeToEdgeMap.get(node);
 		for (Iterator <CyEdge>iter = edgeArray.iterator(); iter.hasNext(); ) {
 			CyEdge edge = iter.next();
 			if (innerEdgeMap.containsKey(edge)) {
 				innerEdgeMap.remove(edge);
 				outerEdgeMap.put(edge,edge);
 			} else if (outerEdgeMap.containsKey(edge)) {
 				outerEdgeMap.remove(edge);
 			}
 		}
 		nodeToEdgeMap.remove(node);
 
 		// Tell the node about it (if necessary)
 		if (node.inGroup(this))
 			node.removeFromGroup(this);
 	}
 }
