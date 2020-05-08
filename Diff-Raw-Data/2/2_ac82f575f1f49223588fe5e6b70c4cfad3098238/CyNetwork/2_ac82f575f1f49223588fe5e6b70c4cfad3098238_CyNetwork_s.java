 /*
  File: CyNetwork.java
 
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
 
 import cytoscape.data.CyAttributes;
 import cytoscape.data.ExpressionData;
 import cytoscape.data.SelectEventListener;
 import cytoscape.data.SelectFilter;
 
 import giny.model.Edge;
 import giny.model.GraphPerspective;
 import giny.model.Node;
 
 import java.util.Collection;
 import java.util.Set;
 
 
 /**
  * CyNetwork is the primary class for algorithm writing.&nbsp; All algorithms
  * should take a CyNetwork as input, and do their best to only use the API of
  * CyNetwork.&nbsp; Plugins that want to affect the display of a graph can look
  * into using CyNetworkView as well.<br>
  * <br>
  * A CyNetwork can create Nodes or Edges.&nbsp; Any Nodes or Edges that wish to
 * be added to a CyNetwork firt need to be created in <span style="font-style:
  * italic;">Cytoscape.</span>&nbsp; <br>
  * <br>
  * The methods that are defined by CyNetwork mostly deal with data integration
  * and flagging of nodes/edges.&nbsp; All methods that deal with graph traversal
  * are part of the inherited API of the GraphPerspective class.&nbsp; Links to
  * which can be found at the bottom of the methods list.&nbsp; <br>
  * <br>
  * In general, all methods are supported for working with Nodes/Edges as
  * objects, and as indices.<br>
  */
 public interface CyNetwork extends GraphPerspective {
 	/**
 	 * Can Change
 	 */
 	public String getTitle();
 
 	/**
 	 * Can Change
 	 */
 	public void setTitle(String new_id);
 
 	/**
 	 * Can't Change
 	 */
 	public String getIdentifier();
 
 	/**
 	 * Can't Change
 	 */
 	public String setIdentifier(String new_id);
 
 	// ----------------------------------------//
 	// Network Methods
 	// ----------------------------------------//
 
 	/**
 	 * Appends all of the nodes and edges in the given Network to this Network
 	 */
 	public void appendNetwork(CyNetwork network);
 
 	// ------------------------------//
 	// Listener Methods
 	// ------------------------------//
 
 	/**
 	 * Registers the argument as a listener to this object. Does nothing if the
 	 * argument is already a listener.
 	 */
 	public void addCyNetworkListener(CyNetworkListener listener);
 
 	/**
 	 * Removes the argument from the set of listeners for this object. Returns
 	 * true if the argument was a listener before this call, false otherwise.
 	 */
 	public boolean removeCyNetworkListener(CyNetworkListener listener);
 
 	/**
 	 * Returns the set of listeners registered with this object.
 	 */
 	public Set getCyNetworkListeners();
 
 	/**
 	 * Sets the selected state of all nodes in this CyNetwork to true
 	 */
 	public void selectAllNodes();
 
 	/**
 	 * Sets the selected state of all edges in this CyNetwork to true
 	 */
 	public void selectAllEdges();
 
 	/**
 	 * Sets the selected state of all nodes in this CyNetwork to false
 	 */
 	public void unselectAllNodes();
 
 	/**
 	 * Sets the selected state of all edges in this CyNetwork to false
 	 */
 	public void unselectAllEdges();
 
 	/**
 	 * Sets the selected state of a collection of nodes.
 	 *
 	 * @param nodes a Collection of Nodes
 	 * @param selected_state the desired selection state for the nodes
 	 */
 	public void setSelectedNodeState(Collection nodes, boolean selected_state);
 
 	/**
 	 * Sets the selected state of a single node.
 	 *
 	 * @param node a single Node
 	 * @param selected_state the desired selection state for the nodes
 	 */
 	public void setSelectedNodeState(Node node, boolean selected_state);
 
 	/**
 	 * Sets the selected state of a collection of edges.
 	 *
 	 * @param edges a Collection of Edges
 	 * @param selected_state the desired selection state for the edges
 	 */
 	public void setSelectedEdgeState(Collection edges, boolean selected_state);
 
 	/**
 	 * Sets the selected state of a single edge.
 	 *
 	 * @param edge a single Edge
 	 * @param selected_state the desired selection state for the edges
 	 */
 	public void setSelectedEdgeState(Edge edge, boolean selected_state);
 
 	/**
 	 * Returns the selected state of the given node.
 	 *
 	 * @param node the node
 	 * @return true if selected, false otherwise
 	 */
 	public boolean isSelected(Node node);
 
 	/**
 	 * Returns the selected state of the given edge.
 	 *
 	 * @param edge the edge
 	 * @return true if selected, false otherwise
 	 */
 	public boolean isSelected(Edge edge);
 
 	/**
 	 * Returns the set of selected nodes in this CyNetwork
 	 *
 	 * @return a Set of selected nodes
 	 */
 	public Set getSelectedNodes();
 
 	/**
 	 * Returns the set of selected edges in this CyNetwork
 	 *
 	 * @return a Set of selected edges
 	 */
 	public Set getSelectedEdges();
 
 	/**
 	 * Adds a listener for SelectEvents to this CyNetwork
 	 *
 	 * @param listener
 	 */
 	public void addSelectEventListener(SelectEventListener listener);
 
 	/**
 	 * Removes a listener for SelectEvents from this CyNetwork
 	 * @param listener
 	 */
 	public void removeSelectEventListener(SelectEventListener listener);
 
 	/**
 	 *
 	 * @return SelectFilter
 	 */
 	public SelectFilter getSelectFilter();
 
 	// --------------------//
 	// Network Client Data
 
 	/**
 	 * Networks can support client data.
 	 *
 	 * @deprecated Use {@link CyAttributes} directly. This method will be
 	 *             removed in May, 2007.
 	 */
 	public void putClientData(String data_name, Object data);
 
 	/**
 	 * Get a list of all currently available ClientData objects
 	 *
 	 * @deprecated Use {@link CyAttributes} directly. This method will be
 	 *             removed in May, 2007.
 	 */
 	public Collection getClientDataNames();
 
 	/**
 	 * Get Some client data
 	 *
 	 * @deprecated Use {@link CyAttributes} directly. This method will be
 	 *             removed in May, 2007.
 	 */
 	public Object getClientData(String data_name);
 
 	// ----------------------------------------//
 	// Node and Edge creation/deletion
 	// ----------------------------------------//
 
 	// --------------------//
 	// Nodes
 
 	/**
 	 * Add a node to this Network that already exists in Cytoscape
 	 *
 	 * @return the Network Index of this node
 	 */
 	public int addNode(int cytoscape_node);
 
 	/**
 	 * Add a node to this Network that already exists in Cytoscape
 	 *
 	 * @return the Network Index of this node
 	 */
 	public CyNode addNode(Node cytoscape_node);
 
 	/**
 	 * This will remove this node from the Network. However, unless forced, it
 	 * will remain in Cytoscape to be possibly resused by another Network in the
 	 * future.
 	 *
 	 * @param set_remove
 	 *            true removes this node from all of Cytoscape, false lets it be
 	 *            used by other CyNetworks
 	 * @return true if the node is still present in Cytoscape ( i.e. in another
 	 *         Network )
 	 */
 	public boolean removeNode(int node_index, boolean set_remove);
 
 	// --------------------//
 	// Edges
 
 	/**
 	 * Add a edge to this Network that already exists in Cytoscape
 	 *
 	 * @return the Network Index of this edge
 	 */
 	public int addEdge(int cytoscape_edge);
 
 	/**
 	 * Add a edge to this Network that already exists in Cytoscape
 	 *
 	 * @return the Network Index of this edge
 	 */
 	public CyEdge addEdge(Edge cytoscape_edge);
 
 	/**
 	 * This will remove this edge from the Network. However, unless forced, it
 	 * will remain in Cytoscape to be possibly resused by another Network in the
 	 * future.
 	 *
 	 * @param set_remove
 	 *            true removes this edge from all of Cytoscape, false lets it be
 	 *            used by other CyNetworks
 	 * @return true if the edge is still present in Cytoscape ( i.e. in another
 	 *         Network )
 	 */
 	public boolean removeEdge(int edge_index, boolean set_remove);
 }
