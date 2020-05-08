 /*
   File: CyNode.java
 
   Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
 
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
 
 import cytoscape.giny.CytoscapeFingRootGraph;
 
 import cytoscape.groups.CyGroup;
 import cytoscape.groups.CyGroupManager;
 
 import giny.model.*;
 
 import java.util.ArrayList;
 import java.util.List;
 
 
 // Package visible class.
 /**
  *
  */
 public class CyNode implements giny.model.Node {
 	// Variables specific to public get/set methods.
 	CytoscapeFingRootGraph m_rootGraph = null;
 	int m_rootGraphIndex = 0;
 	String m_identifier = null;
 	ArrayList<CyGroup> groupList = null;
 
 	/**
 	 * Creates a new CyNode object.
 	 *
 	 * @param root  DOCUMENT ME!
 	 * @param rootGraphIndex  DOCUMENT ME!
 	 */
 	public CyNode(RootGraph root, int rootGraphIndex) {
 		this.m_rootGraph = (CytoscapeFingRootGraph) root;
 		this.m_rootGraphIndex = rootGraphIndex;
 		this.m_identifier = new Integer(m_rootGraphIndex).toString();
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public GraphPerspective getGraphPerspective() {
 		return m_rootGraph.createGraphPerspective(m_rootGraph.getNodeMetaChildIndicesArray(m_rootGraphIndex),
 		                                          m_rootGraph.getEdgeMetaChildIndicesArray(m_rootGraphIndex));
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param gp DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean setGraphPerspective(GraphPerspective gp) {
 		if (gp.getRootGraph() != m_rootGraph)
 			return false;
 
 		final int[] nodeInx = gp.getNodeIndicesArray();
 		final int[] edgeInx = gp.getEdgeIndicesArray();
 
 		for (int i = 0; i < nodeInx.length; i++)
 			m_rootGraph.addNodeMetaChild(m_rootGraphIndex, nodeInx[i]);
 
 		for (int i = 0; i < edgeInx.length; i++)
 			m_rootGraph.addEdgeMetaChild(m_rootGraphIndex, edgeInx[i]);
 
 		return true;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public RootGraph getRootGraph() {
 		return m_rootGraph;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public int getRootGraphIndex() {
 		return m_rootGraphIndex;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public String getIdentifier() {
 		return m_identifier;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param new_id DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public boolean setIdentifier(String new_id) {
 		if (new_id == null) {
 			m_rootGraph.setNodeIdentifier(m_identifier, 0);
 		} else {
 			m_rootGraph.setNodeIdentifier(new_id, m_rootGraphIndex);
 		}
 
 		m_identifier = new_id;
 
 		return true;
 	}
 
 	/**
 	 * Add this node to the specified group.
 	 *
 	 * @param group CyGroup to add this group to
 	 */
 	public void addToGroup(CyGroup group) {
 		// We want to create this lazily to avoid any unnecessary performance/memory
 		// hits on CyNodes!
 		if (groupList == null)
			groupList = new ArrayList<CyGroup>();
 
 		groupList.add(group);
 
 		if (!group.contains(this))
 			group.addNode(this);
 	}
 
 	/**
 	 * Remove this node from the specified group.
 	 *
 	 * @param group CyGroup to remove this group from
 	 */
 	public void removeFromGroup(CyGroup group) {
 		groupList.remove(group);
 		groupList.trimToSize();
 
 		if (group.contains(this))
 			group.removeNode(this);
 	}
 
 	/**
 	 * Return the list of groups this node is a member of
 	 *
 	 * @return list of CyGroups this group is a member of
 	 */
 	public List<CyGroup> getGroups() {
 		return groupList;
 	}
 
 	/**
 	 * Check to see if this node is a member of the requested group
 	 *
 	 * @param group the group to check
 	 * @return 'true' if this node is in group
 	 */
 	public boolean inGroup(CyGroup group) {
 		if (groupList == null)
 			return false;
 
 		return groupList.contains(group);
 	}
 
 	/**
 	 * Check to see if this node is a group
 	 *
 	 * @return 'true' if this node is a group
 	 */
 	public boolean isaGroup() {
 		return CyGroupManager.isaGroup(this);
 	}
 
 	/**
 	 * Return the "name" of a node
 	 *
 	 * @return string representation of the node
 	 */
 	public String toString() {
 		return getIdentifier();
 	}
 }
