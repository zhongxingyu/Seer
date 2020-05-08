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
 package cytoscape.groups;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import giny.model.RootGraph;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.view.CyNetworkView;
 import cytoscape.data.CyAttributes;
 
 /**
  * The CyGroup class provides the implementation for a group model that
  * maintains the list of nodes belonging to a group, the parent of a particular
  * group, and the node that represents the group.  Group information is stored
  * in the CyGroup itself, as well as in special group attributes that are associated
  * with the network, nodes, and edges involved.  These attributes provide a natural
  * mechanism for the saving and restoration of groups.  There are also opaque flags
  */
 public class CyGroupManager {
 	// Static data
 
 	/**
 	 * The list of groups, indexed by the CyNode that represents the group.  The values
 	 * are the CyGroup itself.
 	 */
 	private static Map<CyNode, CyGroup> groupMap = new HashMap<CyNode, CyGroup>();
 
 	/**
 	 * The list of group viewers currently registered.
 	 */
 	private static Map<String, CyGroupViewer> viewerMap = new HashMap<String, CyGroupViewer>();
 
 	/**
 	 * The list of groups, indexed by the managing viewer
 	 */
 	private static Map<CyGroupViewer, List<CyGroup>> groupViewerMap = new HashMap<CyGroupViewer, List<CyGroup>>();
 
 	/**
 	 * The list of groups, indexed by the network
 	 */
 	private static Map<CyNetwork, List<CyGroup>> networkGroupMap = new HashMap<CyNetwork, List<CyGroup>>();
 
 	/**
 	 * The list of group change listeners
 	 */
 	private static List<CyGroupChangeListener> changeListeners = new ArrayList<CyGroupChangeListener>();
 
 	/**
 	 * The internal group map for global groups
 	 */
 	private static CyNetwork GLOBAL_GROUPS = Cytoscape.getNullNetwork();
 
 	/**
 	 * Initialize our property change listener
 	 */
 	private static GroupPropertyChangeListener gpcl = new GroupPropertyChangeListener();
 
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
 
 		return groupMap.get(groupNode);
 	}
 
 	/**
 	 * getGroup is a static method that returns a CyGroup structure when
 	 * given a CyNode that is a member of a group.
 	 *
 	 * @param memberNode a CyNode whose group membership we're looking for
 	 * @return a list of CyGroups this node is a member of
 	 */
 	public static List<CyGroup> getGroup(CyNode memberNode) {
 		List<CyGroup> groupList = new ArrayList<CyGroup>();
 
 		for (CyGroup group: groupMap.values()) {
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
 	public static List<CyGroup> getGroupList() {
 		Collection<CyGroup> c = groupMap.values();
 
 		return new ArrayList<CyGroup>(c);
 	}
 
 	/**
 	 * Return the list of all groups for this network
 	 *
 	 * @param network the network we're interested in.  If network is null, return
 	 * the list of global groups
 	 * @return the list of groups
 	 */
 	public static List<CyGroup> getGroupList(CyNetwork network) {
 		if (network == null) {
 			network = GLOBAL_GROUPS;
 		}
 
 		if (networkGroupMap.containsKey(network))
 			return networkGroupMap.get(network);
 
 		return new ArrayList();
 	}
 
 	/**
 	 * Return the list of all groups managed by a particular viewer
 	 *
 	 * @param viewer the CyGroupViewer
 	 * @return the list of groups
 	 */
 	public static List<CyGroup> getGroupList(CyGroupViewer viewer) {
 		if (!groupViewerMap.containsKey(viewer))
 			return null;
 		List<CyGroup> groupList = groupViewerMap.get(viewer);
 
 		return groupList;
 	}
 
 	/**
 	 * Search all groups for the group named 'groupName'
 	 *
 	 * @param groupName the name of the group to find
 	 * @return the group, or null if no such group exists
 	 */
 	public static CyGroup findGroup(String groupName) {
 		for (CyGroup group: getGroupList()) {
 			if (group.getGroupName().equals(groupName)) {
 				return group;
 			} else if (group.getGroupNode().getIdentifier().equals(groupName)) {
 				// This means that someone changed the name of our groupNode, but
 				// not the name of the group.  Update our name and return
 				((CyGroupImpl)group).setGroupName(group.getGroupNode().getIdentifier());
 				return group;
 			}
 		}
 		return null;
 	}
 
 	/**
  	 * Create a copy of a group, potentially in a new network, and name the
  	 * copy automatically.  The copy name is simply formed by placing an integer
  	 * in brackets after the name of the current group.
  	 *
  	 * @param group the group to make a copy of
  	 * @param network the network the copy is to be part of
  	 * @return the new group, or null if we can't find a suitable name
  	 */
 	public static CyGroup copyGroup(CyGroup group, CyNetwork network) {
 		// First, create a new (unique) name for the group
 		String currentName = group.getGroupNode().getIdentifier();
 		for (int i = 0; i < 1000; i++) {
 			String newName = currentName+"["+i+"]";
 			if (Cytoscape.getCyNode(newName, false) == null) {
 				return copyGroup(newName, group, network);
 			}
 		}
 		return null;
 	}
 
 	/**
  	 * Create a copy of a group, potentially in a new network, and name the
  	 * copy with the provided new name.
  	 *
  	 * @param newName the name of the copied group
  	 * @param group the group to make a copy of
  	 * @param network the network the copy is to be part of
  	 * @return the new group, or null if a node of 'newName' already exists
  	 */
 	public static CyGroup copyGroup(String newName, CyGroup group, CyNetwork network) {
 		// If the new name already exists, return null
 		if (Cytoscape.getCyNode(newName, false) != null) { return null; }
 
 		// Great, now create the new group
 		CyGroup newGroup = createGroup(newName, group.getNodes(), group.getInnerEdges(),
 		                               group.getOuterEdges(), group.getViewer(), network);
 		return newGroup;
 	}
 
 	/**
 	 * Create a new group by specifying all components.  Use this to get a new group.  
 	 * This constructor allows the caller to provide all of the components required
 	 * to create a group.  This is the most efficient of all of the constructors in
 	 * that no additional processing is done to find internal and external edges,
 	 * etc.  If innerEdgeList and outerEdgeList are both null, the underlying implementation
 	 * will find all internal and external edges.
 	 *
 	 * @param groupName the identifier to use for this group -- should be unique!
 	 * @param nodeList the initial set of nodes for this group
 	 * @param innerEdgeList the initial set of internal edges for this group
 	 * @param outerEdgeList the initial set of external edges for this group
 	 * @param viewer the name of the viewer to manage this group
 	 * @param network the network that this group is in
 	 * @return the newly created group
 	 */
 	public static CyGroup createGroup(String groupName, List<CyNode> nodeList,
 	                                  List<CyEdge> innerEdgeList, List<CyEdge> outerEdgeList,
 	                                  String viewer, CyNetwork network) {
 		// Do we already have a group by this name?
 		if (findGroup(groupName) != null) return null;
 		// Create a node for the group
 		CyNode groupNode = Cytoscape.getCyNode(groupName, true);
 		// Create the group itself
 		CyGroup group = new CyGroupImpl(groupNode, nodeList, innerEdgeList, outerEdgeList, network);
 		groupMap.put(group.getGroupNode(), group);
 		notifyListeners(group, CyGroupChangeEvent.GROUP_CREATED);
 		if (viewer != null)
 			setGroupViewer(group, viewer, null, true);
 		return group;
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
 	 * @param network the network that this group is in
 	 * @return the newly created group
 	 */
 	public static CyGroup createGroup(String groupName, String viewer, CyNetwork network) {
 		// Do we already have a group by this name?
 		if (findGroup(groupName) != null) return null;
 		// Create the group
 		CyGroup group = new CyGroupImpl(groupName);
 		groupMap.put(group.getGroupNode(), group);
 		setGroupNetwork(group, network);
 		notifyListeners(group, CyGroupChangeEvent.GROUP_CREATED);
 		setGroupViewer(group, viewer, null, true);
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
 	 * @param network the network that this group is in
 	 */
 	public static CyGroup createGroup(String groupName, List<CyNode> nodeList, String viewer, 
 	                                  CyNetwork network) {
 		// Do we already have a group by this name?
 		if (findGroup(groupName) != null) return null;
 		// Create the group
 		CyGroup group = new CyGroupImpl(groupName, nodeList);
 		groupMap.put(group.getGroupNode(), group);
 		setGroupNetwork(group, network);
 		notifyListeners(group, CyGroupChangeEvent.GROUP_CREATED);
 		setGroupViewer(group, viewer, null, false);
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
 	 * @param network the network that this group is in
 	 */
 	public static CyGroup createGroup(CyNode groupNode, List<CyNode> nodeList, String viewer, 
 	                                  CyNetwork network) {
 		// Do we already have a group by this name?
 		if (findGroup(groupNode.getIdentifier()) != null) return null;
 		// Create the group
 		CyGroup group = null;
 		if (nodeList != null)
 			group = new CyGroupImpl(groupNode, nodeList);
 		else
 			group = new CyGroupImpl(groupNode);
 
 		groupMap.put(group.getGroupNode(), group);
 
 		// See if this groupNode has a state attribute
 		CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
 		try {
 			int state = nodeAttributes.getIntegerAttribute(groupNode.getIdentifier(), CyGroup.GROUP_STATE_ATTR);
 			group.setState(state);
 		} catch (Exception e) {}
 
 		setGroupNetwork(group, network);
 
 		notifyListeners(group, CyGroupChangeEvent.GROUP_CREATED);
 
 		if (viewer != null)
 			setGroupViewer(group, viewer, null, true);
 		return group;
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
 		return createGroup(groupName, viewer, null);
 	}
 
 	/**
 	 * Create a new, empty group.  Use this to get a new group.  In particular,
 	 * this form should be used by internal routines (as opposed to view
 	 * implementations) as this form will cause the viewer to be notified of
 	 * the group creation.  Viewers should use createGroup(String, List, String)
 	 * as defined below.
 	 *
 	 * @param groupName the identifier to use for this group -- should be unique!
 	 * @param nodeList the initial set of nodes for this group
 	 * @param viewer the name of the viewer to manage this group
 	 * @return the newly created group
 	 */
 	public static CyGroup createGroup(String groupName, List<CyNode>nodeList, String viewer) {
 		return createGroup(groupName, nodeList, viewer, null);
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
 	public static CyGroup createGroup(CyNode groupNode, List<CyNode> nodeList, String viewer) {
 		return createGroup(groupNode, nodeList, viewer, null);
 	}
 
 	/**
 	 * Remove (delete) a group
 	 *
 	 * @param group the group to remove
 	 */
 	public static void removeGroup(CyGroup group) {
 		removeGroup(group.getGroupNode());
 	}
 
 	/**
 	 * Remove (delete) a group
 	 *
 	 * @param groupNode the group node of the group to remove
 	 */
 	public static void removeGroup(CyNode groupNode) {
 		if (groupMap.containsKey(groupNode)) {
 			notifyRemoveGroup(groupMap.get(groupNode));
 
 			// Now, remove this group's node from any groups
 			// it might be a member of
 			List<CyGroup> groupList = groupNode.getGroups();
 			if (groupList != null && groupList.size() > 0) {
				for (CyGroup group: new ArrayList<CyGroup>(groupList)) {
 					group.removeNode(groupNode);
 				}
 			}
 
 			// Remove this from the viewer's list
 			CyGroup group = groupMap.get(groupNode);
 			String viewer = group.getViewer();
 			CyNetwork network = group.getNetwork();
 
 			if ((viewer != null) && viewerMap.containsKey(viewer)) {
 				CyGroupViewer groupViewer = viewerMap.get(viewer);
 				List<CyGroup> gList = groupViewerMap.get(groupViewer);
 				gList.remove(group);
 			}
 
 			if ((network != null) && networkGroupMap.containsKey(network)) {
 				List<CyGroup> gList = networkGroupMap.get(network);
 				gList.remove(group);
 
 				if (gList.size() == 0) 
 					networkGroupMap.remove(network);
 			}
 
 			// Remove it from the groupMap
 			groupMap.remove(groupNode);
 
 			// Remove this group from all the nodes
 			List<CyNode> nodeList = group.getNodes();
 			for (CyNode node: nodeList) {
 				node.removeFromGroup(group);
 			}
 
 			// Remove the group node from the network
 			if (network == null) 
 				network = Cytoscape.getCurrentNetwork();
 			network.removeNode(groupNode.getRootGraphIndex(), false);
 
 			// Remove it from the root graph
 			RootGraph rg = groupNode.getRootGraph();
 			rg.removeNode(groupNode);
 
 			notifyListeners(group, CyGroupChangeEvent.GROUP_DELETED);
 		}
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
 	 * Return a list of all registered viewers
 	 *
 	 * @return list of registered group viewers
 	 */
 	public static Collection<CyGroupViewer> getGroupViewers() {
 		return viewerMap.values();
 	}
 
 	/**
 	 * Set the viewer for a group
 	 *
 	 * @param group the group we're associating with a viewer
 	 * @param viewer the viewer
 	 * @param myView the network view that this is being operated on
 	 * @param notify if 'true' the viewer will be notified of the creation
 	 */
 	public static void setGroupViewer(CyGroup group, String viewer, CyNetworkView myView, boolean notify) {
 		if (group == null) return;
 
 		// See if we need to remove the current viewer first
 		if (group.getViewer() != null) {
 			// get the viewer
 			CyGroupViewer v = (CyGroupViewer) viewerMap.get(group.getViewer());
 			if (groupViewerMap.containsKey(v)) {
 				groupViewerMap.get(v).remove(group);
 				if (notify)
 					v.groupWillBeRemoved(group);
 			}
 			((CyGroupImpl)group).setViewer(null);
 		}
 
 		if ((viewer != null) && viewerMap.containsKey(viewer)) {
 			// get the viewer
 			CyGroupViewer v = viewerMap.get(viewer);
 
 			// create the list if necessary
 			if (!groupViewerMap.containsKey(v))
 				groupViewerMap.put(v, new ArrayList<CyGroup>());
 
 			// Add this group to the list
 			groupViewerMap.get(v).add(group);
 
 			if (notify) {
 				// Make sure we have a view before we notify
 				CyNetworkView currentView = Cytoscape.getCurrentNetworkView();
 				if (myView != null) {
 					v.groupCreated(group, myView);
 				} else if (currentView != null) {
 					v.groupCreated(group, currentView);
 				}
 				notifyListeners(group, CyGroupChangeEvent.GROUP_MODIFIED);
 			}
 		}
 
 		((CyGroupImpl)group).setViewer(viewer);
 	}
 
 	/**
 	 * Return the viewer object for a named viewer
 	 *
 	 * @param viewerName the name of the viewer
 	 * @return the viewer object
 	 */
 	public static CyGroupViewer getGroupViewer(String viewerName) {
 		if ((viewerName != null) && viewerMap.containsKey(viewerName))
 			return viewerMap.get(viewerName);
 		return null;
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
 
 	/**
 	 * Add a new change listener to our list of listeners
 	 *
 	 * @param listener the listener to add
 	 */
 	public static void addGroupChangeListener(CyGroupChangeListener listener) {
 		changeListeners.add(listener);
 	}
 
 	/**
 	 * Remove a change listener from our list of listeners
 	 *
 	 * @param listener the listener to remove
 	 */
 	public static void removeGroupChangeListener(CyGroupChangeListener listener) {
 		changeListeners.remove(listener);
 	}
 
 	/**
 	 * Notify a listener that something has happened
 	 *
 	 * @param group the group that has changed
 	 * @param whatChanged the thing that has changed about the group
 	 */
 	private static void notifyListeners(CyGroup group, CyGroupChangeEvent whatChanged) {
 		for (CyGroupChangeListener listener: changeListeners) {
 			listener.groupChanged(group, whatChanged);
 		}
 	}
 
 
 	/**
 	 * Maintain the network group map
 	 *
 	 * @param group the group we're adding to the map
 	 * @param network the network -- if it's null, we're adding it to the global map
 	 */
 	private static void setGroupNetwork(CyGroup group, CyNetwork network) {
 		group.setNetwork(network, false);
 		if (network == null)
 			network = GLOBAL_GROUPS;
 
 		List<CyGroup> groupList = null;
 
 		if (!networkGroupMap.containsKey(network)) {
 			groupList = new ArrayList<CyGroup>();
 		} else {
 			groupList = networkGroupMap.get(network);
 		}
 		groupList.add(group);
 		networkGroupMap.put(network, groupList);
 	}
 
 }
