 /* vim :set ts=2: */
 /*
   File: CyGroup.java
 
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
 package cytoscape;
 
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
 public class CyGroup {
 	// Static data
 
 	/**
 	 * The attribute key to use for group membership
 	 */
 	public static final String MEMBER_LIST_ATTR = "__groupMembers";
 
 	/**
 	 * The attribute key to use for group viewer
 	 */
 	public static final String GROUP_VIEWER_ATTR = "__groupViewer";
 
 	/**
 	 * The change values
 	 */
 	public static final int NODE_ADDED = 1;
 	public static final int NODE_REMOVED = 2;
 
 	/**
 	 * The list of groups, indexed by the CyNode that represents the group.  The values
 	 * are the CyGroup itself.
 	 */
 	private static HashMap<CyNode, CyGroup> groupMap = new HashMap();
 
 	/**
 	 * The list of group viewers currently registered.
 	 */
 	private static HashMap<String, CyGroupViewer> viewerMap = new HashMap();
 
 	/**
 	 * The list of groups, indexed by the managing viewer
 	 */
 	private static HashMap<CyGroupViewer, List<CyGroup>> groupViewerMap = new HashMap();
 
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
 
 	// Static methods
 	/**
 	 * getCyGroup is a static method that returns a CyGroup structure when
 	 * given the CyNode that represents this group.
 	 *
 	 * @param groupNode the CyNode that represents this group
 	 * @return the associated CyGroup structure
 	 */
 	public static CyGroup getCyGroup(CyNode groupNode) {
 		if ((groupMap == null) || !groupMap.containsKey(groupNode))
 			return null;
 
 		return (CyGroup) groupMap.get(groupNode);
 	}
 
 	/**
 	 * getGroup is a static method that returns a CyGroup structure when
 	 * given a CyNode that is a member of a group.
 	 *
 	 * @param memberNode a CyNode whose group membership we're looking for
 	 * @return a list of CyGroups this node is a member of
 	 */
 	public static List getGroup(CyNode memberNode) {
 		ArrayList<CyGroup> groupList = new ArrayList();
 		Iterator groupIter = groupMap.values().iterator();
 
 		while (groupIter.hasNext()) {
 			CyGroup group = (CyGroup) groupIter.next();
 
 			if (group.contains(memberNode))
 				groupList.add(group);
 		}
 
 		if (groupList.size() == 0)
 			return null;
 
 		return groupList;
 	}
 
 	/**
 	 * Return the list of all groups
 	 *
 	 * @return the list of groups
 	 */
 	public static List getGroupList() {
 		Collection<CyGroup> c = groupMap.values();
 
 		return new ArrayList(c);
 	}
 
 	/**
 	 * Return the list of all groups managed by a particular viewer
 	 *
 	 * @param viewer the CyGroupViewer
 	 * @return the list of groups
 	 */
 	public static List getGroupList(CyGroupViewer viewer) {
 		if (!groupViewerMap.containsKey(viewer))
 			return null;
 		List<CyGroup> groupList = groupViewerMap.get(viewer);
 
 		return groupList;
 	}
 
 	/**
 	 * Return the list of all groups managed by a particular viewer
 	 *
 	 * @param viewer the name of the CyGroupViewer
 	 * @return the list of groups
 	 */
 	public static List getGroupList(String viewer) {
 		if (viewerMap.containsKey(viewer))
 			return getGroupList(viewerMap.get(viewer));
 
 		return null;
 	}
 
 	/**
 	 * Create a new, empty group.  Use this to get a new group.  In particular,
 	 * this form should be used by internal routines (as opposed to view
 	 * implementations) as this form will cause the viewer to be notified of
 	 * the group creation.  Viewers should use createGroup(String, List, String)
 	 * as defined below.
 	 *
 	 * @param groupName the identifier to use for this group -- should be unique!
 	 * @param viewer the name of the viewer to manage this group
 	 * @return the newly created group
 	 */
 	public static CyGroup createGroup(String groupName, String viewer) {
 		// Create the group
 		CyGroup group = new CyGroup(groupName);
 		groupMap.put(group.getGroupNode(), group);
 		setGroupViewer(group, viewer, true);
 
 		return group;
 	}
 
 	/**
 	 * Create a new group with a list of nodes as initial members.  Note that this
 	 * method is the prefered method to be used by viewers.  Using this method,
 	 * once the group is created the viewer is *not* notified (since it is assumed
 	 * they are doing the creation).
 	 *
 	 * @param groupName the identifier to use for this group -- should be unique!
 	 * @param nodeList the initial set of nodes for this group
 	 * @param viewer the name of the viewer to manage this group
 	 */
 	public static CyGroup createGroup(String groupName, List nodeList, String viewer) {
 		// Create the group
 		CyGroup group = new CyGroup(groupName, nodeList);
 		groupMap.put(group.getGroupNode(), group);
 		setGroupViewer(group, viewer, false);
 
 		return group;
 	}
 
 	/**
 	 * Create a new group with a list of nodes as initial members, and a precreated
 	 * group node.  This is usually used by the XGMML reader since the group node
 	 * may need to alread be created with its associated "extra" edges.  Note that
 	 * the node will be created, but *not* added to the network.  That is the
 	 * responsibility of the appropriate viewer.
 	 *
 	 * @param groupNode the groupNode to use for this group
 	 * @param nodeList the initial set of nodes for this group
 	 * @param viewer the name of the viewer to manage this group
 	 */
 	public static CyGroup createGroup(CyNode groupNode, List nodeList, String viewer) {
 		// Create the group
 		CyGroup group = new CyGroup(groupNode, nodeList);
 		groupMap.put(group.getGroupNode(), group);
 
 		if (viewer != null)
 			setGroupViewer(group, viewer, true);
 
 		return group;
 	}
 
 	/**
 	 * Remove (delete) a group
 	 *
 	 * @param group the group to remove
 	 */
 	public static void removeGroup(CyGroup group) {
 		CyNode groupNode = group.getGroupNode();
 		removeGroup(groupNode);
 	}
 
 	/**
 	 * Remove (delete) a group
 	 *
 	 * @param groupNode the group node of the group to remove
 	 */
 	public static void removeGroup(CyNode groupNode) {
 		if (groupMap.containsKey(groupNode)) {
 			notifyRemoveGroup((CyGroup) groupMap.get(groupNode));
 
 			// Remove this from the viewer's list
 			CyGroup group = groupMap.get(groupNode);
 			String viewer = group.getViewer();
 
 			if ((viewer != null) && viewerMap.containsKey(viewer)) {
 				CyGroupViewer groupViewer = viewerMap.get(viewer);
 				List<CyGroup> gList = groupViewerMap.get(groupViewer);
 				gList.remove(group);
 			}
 
 			// Remove it from the groupMap
 			groupMap.remove(groupNode);
 
			// Get the rootGraph for this node
			RootGraph rg = groupNode.getRootGraph();
 			// Remove it from the root graph
			rg.removeNode(groupNode);
 		}
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
 		List <CyEdge> eMove = new ArrayList();
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
 		CyGroupViewer v = (CyGroupViewer)viewerMap.get(this.viewer);
 		// Tell the viewer that something has changed
 		v.groupChanged(this, node, NODE_ADDED);
 	}
 
 	/**
 	 * Remove a node from a group
 	 *
 	 * @param node the node to remove
 	 */
 	public void removeNode ( CyNode node ) {
 		removeNodeFromGroup(node);
 		// Get our viewer
 		CyGroupViewer v = (CyGroupViewer)viewerMap.get(this.viewer);
 		// Tell the viewer that something has changed
 		v.groupChanged(this, node, NODE_REMOVED);
 	}
 
 	/**
 	 * See if this CyNode represents a group
 	 *
 	 * @param groupNode the node we want to test
 	 * @return 'true' if groupNode is a group
 	 */
 	public static boolean isaGroup(CyNode groupNode) {
 		return groupMap.containsKey(groupNode);
 	}
 
 	// Viewer methods
 	/**
 	 * Register a viewer.
 	 *
 	 * @param viewer the viewer we're registering
 	 */
 	public static void registerGroupViewer(CyGroupViewer viewer) {
 		viewerMap.put(viewer.getViewerName(), viewer);
 	}
 
 	/**
 	 * Set the viewer for a group
 	 *
 	 * @param group the group we're associating with a viewer
 	 * @param viewer the viewer
 	 * @param notify if 'true' the viewer will be notified of the creation
 	 */
 	public static void setGroupViewer(CyGroup group, String viewer, boolean notify) {
 		if ((viewer != null) && viewerMap.containsKey(viewer)) {
 			// get the viewer
 			CyGroupViewer v = (CyGroupViewer) viewerMap.get(viewer);
 
 			// create the list if necessary
 			if (!groupViewerMap.containsKey(v))
 				groupViewerMap.put(v, new ArrayList());
 
 			// Add this group to the list
 			groupViewerMap.get(v).add(group);
 
 			if (notify)
 				v.groupCreated(group);
 		}
 
 		group.setViewer(viewer);
 	}
 
 	/**
 	 * Notify a viewer that a group has been created for them to manage.
 	 *
 	 * @param group the group that was just created
 	 */
 	public static void notifyCreateGroup(CyGroup group) {
 		String viewer = group.getViewer();
 
 		if ((viewer != null) && viewerMap.containsKey(viewer)) {
 			CyGroupViewer v = viewerMap.get(viewer);
 			v.groupCreated(group);
 		}
 	}
 
 	/**
 	 * Notify a viewer the a group of interest is going to be removed.
 	 *
 	 * @param group the group to be removed
 	 */
 	public static void notifyRemoveGroup(CyGroup group) {
 		String viewer = group.getViewer();
 
 		if ((viewer != null) && viewerMap.containsKey(viewer)) {
 			CyGroupViewer v = viewerMap.get(viewer);
 			v.groupWillBeRemoved(group);
 		}
 	}
 
 	// Public methods
 
 	/**
 	 * Empty constructor
 	 */
 	protected CyGroup() {
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
 	protected CyGroup(String groupName) {
 		this();
 		this.groupNode = Cytoscape.getCyNode(groupName, true);
 		this.groupName = groupName;
 	}
 
 	/**
 	 * Constructor to create an empty group when the group node is specified.
 	 *
 	 * @param groupNode the CyNode to use for this group
 	 */
 	protected CyGroup(CyNode groupNode) {
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
 	protected CyGroup(CyNode groupNode, List nodeList) {
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
 	protected CyGroup(String groupName, List nodeList) {
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
 	public List getNodes() {
 		Collection<CyNode> v = nodeMap.values();
 
 		return new ArrayList(v);
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
 
 		return new ArrayList(v);
 	}
 
 	/**
 	 * Get all of the edges partially contained within this group
 	 *
 	 * @return list of edges in the group
 	 */
 	public List<CyEdge> getOuterEdges() {
 		Collection<CyEdge> v = outerEdgeMap.values();
 
 		return new ArrayList(v);
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
 		this.groupState = state;
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
 		}
 
 		attributes.setUserVisible(GROUP_VIEWER_ATTR, false);
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
 	private void addNodeToGroup ( CyNode node ) {
 		// Put this node in our map
 		nodeMap.put(node, node);
 		CyNetwork network = Cytoscape.getCurrentNetwork();
 		List <CyEdge>edgeList = null;
 
 		if (nodeToEdgeMap.containsKey(node)) {
 			edgeList = nodeToEdgeMap.get(node);
 		} else {
 			edgeList = new ArrayList();
 		}
 
 		// Add all of the edges
 		int [] edgeArray = network.getAdjacentEdgeIndicesArray(node.getRootGraphIndex(),true,true,true);
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
