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
 
 import java.util.Iterator;
 import java.util.List;
 
import cytoscape.CyEdge;
import cytoscape.CyNode;

 
 /**
  * The CyGroup interface provides the methods for a group model that
  * maintains the list of nodes belonging to a group, the parent of a particular
  * group, and the node that represents the group.  Group information is stored
  * in the CyGroup itself, as well as in special group attributes that are associated
  * with the network, nodes, and edges involved.  These attributes provide a natural
  * mechanism for the saving and restoration of groups.  There are also opaque flags
  */
 public interface CyGroup {
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
 	 * The attribute key to use for group state
 	 */
 	public static final String GROUP_STATE_ATTR = "__groupState";
 
 	// Public methods
 
 	/**
 	 * Return the name of this group
 	 */
 	public String getGroupName();
 
 	/**
 	 * Get all of the nodes in this group
 	 *
 	 * @return list of nodes in the group
 	 */
 	public List<CyNode> getNodes();
 
 	/**
 	 * Get the CyNode that represents this group
 	 *
 	 * @return CyNode representing the group
 	 */
 	public CyNode getGroupNode();
 
 	/**
 	 * Get an iterator over all of the nodes in this group
 	 *
 	 * @return node iterator
 	 */
 	public Iterator<CyNode> getNodeIterator();
 
 	/**
 	 * Get all of the edges completely contained within this group
 	 *
 	 * @return list of edges in the group
 	 */
 	public List<CyEdge> getInnerEdges();
 
 	/**
 	 * Get all of the edges partially contained within this group
 	 *
 	 * @return list of edges in the group
 	 */
 	public List<CyEdge> getOuterEdges();
 
 	/**
 	 * Add an outer edge to the map.  Some viewers may need to do this
 	 * if they add and remove edges, for example.
 	 *
 	 * @param edge the CyEdge to add to the outer edge map
 	 */
 	public void addOuterEdge(CyEdge edge);
 
 	/**
 	 * Add an inner edge to the map.  Some viewers may need to do this
 	 * if they add and remove edges, for example.
 	 *
 	 * @param edge the CyEdge to add to the innter edge map
 	 */
 	public void addInnerEdge(CyEdge edge);
 
 	/**
 	 * Determine if a node is a member of this group
 	 *
 	 * @param node the CyNode to test
 	 * @return true if node is a member of the group
 	 */
 	public boolean contains(CyNode node);
 
 	/**
 	 * Set the state of the group
 	 *
 	 * @param state the state to set
 	 */
 	public void setState(int state);
 
 	/**
 	 * Get the state of the group
 	 *
 	 * @return group state
 	 */
 	public int getState();
 
 	/**
 	 * Provide the default toString method
 	 *
 	 * @return group name
 	 */
 	public String toString();
 
 	/**
 	 * Get the name of the viewer for this group
 	 *
 	 * @return viewer for this group
 	 */
 	public String getViewer();
 
 	/**
 	 * Add a new node to this group
 	 *
 	 * @param node the node to add
 	 */
 	public void addNode ( CyNode node );
 
 
 	/**
 	 * Remove a node from a group
 	 *
 	 * @param node the node to remove
 	 */
 	public void removeNode ( CyNode node );
 }
