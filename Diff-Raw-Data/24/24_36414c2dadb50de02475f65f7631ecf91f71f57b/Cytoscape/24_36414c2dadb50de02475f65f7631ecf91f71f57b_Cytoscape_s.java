 /*
  File: Cytoscape.java 
  
  Copyright (c) 2006, The Cytoscape Consortium (www.cytoscape.org)
  
  The Cytoscape Consortium is: 
  - Institute for Systems Biology
  - University of California San Diego
  - Memorial Sloan-Kettering Cancer Center
  - Pasteur Institute
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
 
 //---------------------------------------------------------------------------
 package cytoscape;
 
 import giny.model.Edge;
 import giny.model.Node;
 import giny.view.GraphView;
 import giny.view.NodeView;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeSupport;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.swing.JOptionPane;
 import javax.swing.event.SwingPropertyChangeSupport;
 
 import cytoscape.actions.SaveSessionAction;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.CyAttributesImpl;
 import cytoscape.data.ExpressionData;
 import cytoscape.data.GraphObjAttributes;
 import cytoscape.data.Semantics;
 import cytoscape.data.readers.CyAttributesReader;
 import cytoscape.data.readers.GMLReader2;
 import cytoscape.data.readers.XGMMLReader;
 import cytoscape.data.readers.GraphReader;
 import cytoscape.data.readers.InteractionsReader;
 import cytoscape.data.servers.BioDataServer;
 import cytoscape.ding.CyGraphLOD;
 import cytoscape.ding.DingNetworkView;
 import cytoscape.giny.CytoscapeFingRootGraph;
 import cytoscape.giny.CytoscapeRootGraph;
 import cytoscape.util.CyNetworkNaming;
 import cytoscape.util.FileUtil;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CytoscapeDesktop;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.init.CyInitParams;
 
 /**
  * This class, Cytoscape is <i>the</i> primary class in the API.
  * 
  * All Nodes and Edges must be created using the methods getCyNode and
  * getCyEdge, available only in this class. Once A node or edge is created using
  * these methods it can then be added to a CyNetwork, where it can be used
  * algorithmically.<BR>
  * <BR>
  * The methods get/setNode/EdgeAttributeValue allow you to assocate data with
  * nodes or edges. That data is then carried into all CyNetworks where that
  * Node/Edge is present.
  */
 public abstract class Cytoscape {
 	//
 	// Signals
 	//
 	public static String NETWORK_CREATED = "NETWORK_CREATED";
 	public static String ATTRIBUTES_CHANGED = "ATTRIBUTES_CHANGED";
 	public static String DATASERVER_CHANGED = "DATASERVER_CHANGED";
 	public static String EXPRESSION_DATA_LOADED = "EXPRESSION_DATA_LOADED";
 	public static String NETWORK_DESTROYED = "NETWORK_DESTROYED";
 	public static String CYTOSCAPE_EXIT = "CYTOSCAPE_EXIT";
 
 	// KONO: 03/10/2006 For vizmap saving and loading
 	public static String SESSION_SAVED = "SESSION_SAVED";
 	public static String SESSION_LOADED = "SESSION_LOADED";
 	public static String VIZMAP_RESTORED = "VIZMAP_RESTORED";
 
 	public static String SAVE_VIZMAP_PROPS = "SAVE_VIZMAP_PROPS";
 	public static String VIZMAP_LOADED = "VIZMAP_LOADED";
 
 	// events for network modification
 	public static final String NETWORK_MODIFIED = "NETWORK_MODIFIED";
 	public static final String NETWORK_SAVED = "NETWORK_SAVED";
 	public static final String NETWORK_LOADED = "NETWORK_LOADED";
 
 	// Events for Preference Dialog (properties).
 	// Signals that the preference has change interally to the
 	// prefs dialog.
 	public static final String PREFERENCE_MODIFIED = "PREFERENCE_MODIFIED";
 
 	// Signals that CytoscapeInit properties have been updated.
 	public static final String PREFERENCES_UPDATED = "PREFERENCES_UPDATED";
 
 	/**
 	 * When creating a network, use one of the standard suffixes to have it
 	 * parsed correctly<BR>
 	 * <ul>
 	 * <li> sif -- Simple Interaction File</li>
 	 * <li> gml -- Graph Markup Languange</li>
 	 * <li> sbml -- SBML</li>
 	 * <li> xgmml -- XGMML</li>
 	 * </ul>
 	 */
 	public static int FILE_BY_SUFFIX = 0;
 	public static int FILE_GML = 1;
 	public static int FILE_SIF = 2;
 	public static int FILE_SBML = 3;
 	public static int FILE_XGMML = 4;
 
 	// constants for tracking selection mode globally
 	public static final int SELECT_NODES_ONLY = 1;
 	public static final int SELECT_EDGES_ONLY = 2;
 	public static final int SELECT_NODES_AND_EDGES = 3;
 
 	// global to represent which selection mode is active
 	private static int currentSelectionMode = SELECT_NODES_ONLY;
 
 	// Value to manage session state
 	public static final int SESSION_NEW = 0;
 	public static final int SESSION_OPENED = 1;
 	public static final int SESSION_CHANGED = 2;
 	public static final int SESSION_CLOSED = 3;
 	private static int sessionState = SESSION_NEW;
 
 	private static BioDataServer bioDataServer;
 
 	private static String species;
 
 	// global flag to indicate if Squiggle is turned on
 	private static boolean squiggleEnabled = false;
 
 	/**
 	 * The shared RootGraph between all Networks
 	 */
 	protected static CytoscapeRootGraph cytoscapeRootGraph;
 
 	/**
 	 * Node CyAttributes.
 	 */
 	private static CyAttributes nodeAttributes = new CyAttributesImpl();
 	private static GraphObjAttributes nodeData = new GraphObjAttributes(
 			nodeAttributes);
 
 	/**
 	 * Edge CyAttributes.
 	 */
 	private static CyAttributes edgeAttributes = new CyAttributesImpl();
 	private static GraphObjAttributes edgeData = new GraphObjAttributes(
 			edgeAttributes);
 
 	/**
 	 * Network CyAttributes.
 	 */
 	private static CyAttributes networkAttributes = new CyAttributesImpl();
 
 	protected static ExpressionData expressionData;
 
 	protected static Object pcsO = new Object();
 
 	protected static SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(
 			pcsO);
 
 	// Test
 	protected static Object pcs2 = new Object();
 
 	protected static PropertyChangeSupport newPcs = new PropertyChangeSupport(
 			pcs2);
 
 	protected static Map networkViewMap;
 
 	protected static Map networkMap;
 
 	protected static CytoscapeDesktop defaultDesktop;
 
 	protected static String currentNetworkID;
 
 	protected static String currentNetworkViewID;
 
 	/**
 	 * Used by session writer. If this is null, session writer opens the file
 	 * chooser. Otherwise, overwrite the file.
 	 * 
 	 * KONO: 02/23/2006
 	 */
 	private static String currentSessionFileName;
 
 	/**
 	 * A null CyNetwork to give when there is no Current Network
 	 */
 	protected static CyNetwork nullNetwork = getRootGraph().createNetwork(
 			new int[] {}, new int[] {});
 
 	/**
 	 * A null CyNetworkView to give when there is no Current NetworkView
 	 */
 	protected static CyNetworkView nullNetworkView = new DingNetworkView(
 			nullNetwork, "null");
 
 	/*
 	 * VMM should be tied to Cytoscape, not to Desktop. Developers should call
 	 * this from here.
 	 */
 	protected static VisualMappingManager VMM = new VisualMappingManager(
 			nullNetworkView);
 
 	/**
 	 * Shuts down Cytoscape, after giving plugins time to react.
 	 * @deprecated Use exit(returnVal) instead.  This will be removed
 	 * in Sept 2006.
 	 */
 	public static void exit() {
 		exit(0);
 	}
 
 	/**
 	 * Shuts down Cytoscape, after giving plugins time to react.
 	 * @param returnVal The return value. Zero indicates success,
 	 * non-zero otherwise.
 	 */
 	public static void exit(int returnVal) {
 
 		int mode = CytoscapeInit.getCyInitParams().getMode();
 
 		if (mode == CyInitParams.EMBEDDED_WINDOW ||
 		    mode == CyInitParams.GUI ) {
 			// prompt the user about saving modified files before quitting
 			if (confirmQuit()) {
 				try {
 					firePropertyChange(CYTOSCAPE_EXIT, null, "now");
 				} catch (Exception e) {
 					System.out.println("Errors on close, closed anyways.");
 				}
 
 				System.out.println("Cytoscape Exiting....");
 				if (mode == CyInitParams.EMBEDDED_WINDOW) {                                                                                    
 					// don't system exit since we are running as part 
 					// of a bigger application. Instead, dispose of the
 					// desktop.
 					getDesktop().dispose();
 				} else {
 					System.exit(returnVal);
 				}
 			} else {
 				return;
 			}
 			// if we get here, we're not quitting!
 
 		} else {
 			System.out.println("Cytoscape Exiting....");
 			System.exit(returnVal);
 		}
 	}
 
 	/**
 	 * Prompt the user about saving modified files before quitting.
 	 */
 	private static boolean confirmQuit() {
 		String msg = "You have made modifications to the following networks:\n";
 		Set netSet = Cytoscape.getNetworkSet();
 		Iterator it = netSet.iterator();
 		int networkCount = netSet.size();
 
 		// // TODO: filter networks for only those modified
 		// while (it.hasNext()) {
 		// CyNetwork net = (CyNetwork) it.next();
 		// boolean modified = CytoscapeModifiedNetworkManager.isModified(net);
 		// if (modified) {
 		// String name = net.getTitle();
 		// msg += " " + name + "\n";
 		// networkCount++;
 		// }
 		// }
 
 		if (networkCount == 0) {
 			System.out.println("ConfirmQuit = " + true);
 			return true; // no networks have been modified
 		}
 		// msg += "Are you sure you want to exit without saving?";
 
 		//
 		// Confirm user to save current session or not.
 		//
 		msg = "Do you want to exit without saving session?";
 		Object[] options = { "Yes, quit anyway.", "No, save current session.", "Cancel"};
 		int n = JOptionPane.showOptionDialog(Cytoscape.getDesktop(), msg,
 				"Save Networks Before Quitting?", JOptionPane.YES_NO_OPTION,
 				JOptionPane.QUESTION_MESSAGE, null, options, options[1]);
 		if (n == JOptionPane.YES_OPTION) {
 			System.out.println("ConfirmQuit = " + true);
 			return true;
 		} else if (n == JOptionPane.NO_OPTION) {
 			System.out.println("ConfirmQuit = " + false);
 			System.out.println("Save current session...");
 
 			SaveSessionAction saveAction = new SaveSessionAction();
 			saveAction.actionPerformed(null);
 
 			return true;
 		} else {
 			return false; // default if dialog box is closed
 		}
 	}
 
 	// --------------------//
 	// Root Graph Methods
 	// --------------------//
 
 	/**
 	 * Bound events are:
 	 * <ol>
 	 * <li>NETWORK_CREATED
 	 * <li>NETWORK_DESTROYED
 	 * <li>ATTRIBUTES_ADDED
 	 * <li>CYTOSCAPE_EXIT
 	 * </ol>
 	 */
 	public static SwingPropertyChangeSupport getSwingPropertyChangeSupport() {
 		return pcs;
 	}
 
 	public static PropertyChangeSupport getPropertyChangeSupport() {
 		return newPcs;
 	}
 
 	public static VisualMappingManager getVisualMappingManager() {
 		return VMM;
 	}
 
 	/**
 	 * Return the CytoscapeRootGraph
 	 */
 	public static CytoscapeRootGraph getRootGraph() {
 		if (cytoscapeRootGraph == null)
 			cytoscapeRootGraph = new CytoscapeFingRootGraph();
 		return cytoscapeRootGraph;
 	}
 
 	/**
 	 * Ensure the capacity of Cytoscapce. This is to prevent the inefficiency of
 	 * adding nodes one at a time.
 	 */
 	public static void ensureCapacity(int nodes, int edges) {
 		// getRootGraph().ensureCapacity( nodes, edges );
 	}
 
 	/**
 	 * @deprecated WARNING: this should only be used under special
 	 *             circumstances.
 	 */
 	public static void clearCytoscape() {
 
 		// removed since it was only added for old unit test code to work.
 
 	}
 
 	/**
 	 * @return all CyNodes that are present in Cytoscape
 	 */
 	public static List getCyNodesList() {
 		return getRootGraph().nodesList();
 	}
 
 	/**
 	 * @return all CyEdges that are present in Cytoscape
 	 */
 	public static List getCyEdgesList() {
 		return getRootGraph().edgesList();
 	}
 
 	/**
 	 * @param alias
 	 *            an alias of a node
 	 * @return will return a node, if one exists for the given alias
 	 */
 	public static CyNode getCyNode(String alias) {
 		return getCyNode(alias, false);
 	}
 
 	/**
 	 * @param nodeID
 	 *            an alias of a node
 	 * @param create
 	 *            will create a node if one does not exist
 	 * @return will always return a node, if <code>create</code> is true
 	 * 
 	 * KONO: 5/4/2006 Since we removed the canonicalName, no "canonicalization"
 	 * is necessary. This method uses given nodeID as the identifier.
 	 * 
 	 */
 	public static CyNode getCyNode(String nodeID, boolean create) {
 
 		CyNode node = Cytoscape.getRootGraph().getNode(nodeID);
 
 		// If the node is already exists,return it.
 		if (node != null) {
 			return node;
 		}
 
 		// And if we do not have to create new one, just return null
 		if (!create) {
 			return null;
 		}
 
 		// Now, create a new node.
 		node = (CyNode) getRootGraph().getNode(Cytoscape.getRootGraph().createNode());
 		node.setIdentifier(nodeID);
 		
 		/*
 		 * We do not need canonicalName anymore.  If necessary, user should
 		 * create one from Attribute Browser.
 		 *
 		 * NOTE:
 		 *
 		 * The following statement referencing CANONICAL_NAME should not be
 		 * deleted until its removed from the core (Semantics.java)
 		 * on April, 2007 _DO NOT DELETE UNTIL THEN_
 		 */
 		getNodeAttributes().setAttribute(nodeID,Semantics.CANONICAL_NAME,nodeID);
 
 		return node;
 	}
 
 	/**
 	 * Gets the first CyEdge found between the two nodes (direction does not
 	 * matter) that has the given value for the given attribute.
 	 * 
 	 * @param node_1
 	 *            one end of the edge
 	 * @param node_2
 	 *            the other end of the edge
 	 * @param attribute
 	 *            the attribute of the edge to be searched, a common one is
 	 *            {@link Semantics#INTERACTION }
 	 * @param attribute_value
 	 *            a value for the attribute, like "pp"
 	 * @param create
 	 *            will create an edge if one does not exist and if attribute is
 	 *            {@link Semantics#INTERACTION}
 	 * @return returns an existing CyEdge if present, or creates one if
 	 *         <code>create</code> is true and attribute is
 	 *         Semantics.INTERACTION, otherwise returns null.
 	 */
 	public static CyEdge getCyEdge(Node node_1, Node node_2, String attribute,
 			Object attribute_value, boolean create) {
 
 		if (Cytoscape.getRootGraph().getEdgeCount() != 0) {
 			int[] n1Edges = Cytoscape.getRootGraph()
 					.getAdjacentEdgeIndicesArray(node_1.getRootGraphIndex(),
 							true, true, true);
 
 			for (int i = 0; i < n1Edges.length; i++) {
 				CyEdge edge = (CyEdge) Cytoscape.getRootGraph().getEdge(
 						n1Edges[i]);
 				Object attValue = private_getEdgeAttributeValue(edge, attribute);
 
 				if (attValue != null && attValue.equals(attribute_value)) {
 					CyNode otherNode = (CyNode) edge.getTarget();
 					if (otherNode.getRootGraphIndex() == node_1
 							.getRootGraphIndex()) {
 						otherNode = (CyNode) edge.getSource();
 					}
 
 					if (otherNode.getRootGraphIndex() == node_2
 							.getRootGraphIndex()) {
 						return edge;
 					}
 				}
 			}// for i
 		}
 
 		if (create && attribute instanceof String
 				&& attribute.equals(Semantics.INTERACTION)) {
 			// create the edge
 			CyEdge edge = (CyEdge) Cytoscape.getRootGraph().getEdge(
 					Cytoscape.getRootGraph().createEdge(node_1, node_2));
 
 			// create the edge id
 			String edge_name = node_1.getIdentifier() + " (" + attribute_value
 					+ ") " + node_2.getIdentifier();
 			edge.setIdentifier(edge_name);
 
 			// Store Edge Name Mapping within GOB.
 			Cytoscape.getEdgeNetworkData().addNameMapping(edge_name, edge);
 
 			// store edge id as INTERACTION / CANONICAL_NAME Attributes
 			edgeAttributes.setAttribute(edge_name, Semantics.INTERACTION,
 					(String) attribute_value);
 			return edge;
 		}
 		return null;
 	}
 
 	/**
 	 * @param source_alias
 	 *            an alias of a node
 	 * @param edge_name
 	 *            the name of the node
 	 * @param target_alias
 	 *            an alias of a node
 	 * @return will always return an edge
 	 */
 	public static CyEdge getCyEdge(String source_alias, String edge_name,
 			String target_alias, String interaction_type) {
 
 		edge_name = canonicalizeName(edge_name);
 		CyEdge edge = Cytoscape.getRootGraph().getEdge(edge_name);
 		if (edge != null) {
 			// System.out.print( "`" );
 			return edge;
 		}
 
 		// edge does not exist, create one
 		// System.out.print( "*" );
 		CyNode source = getCyNode(source_alias);
 		CyNode target = getCyNode(target_alias);
 
 		return getCyEdge(source, target, Semantics.INTERACTION,
 				interaction_type, true);
 
 		// edge = ( CyEdge )Cytoscape.getRootGraph().getEdge(
 		// Cytoscape.getRootGraph().createEdge (source, target));
 
 		// Cytoscape.getEdgeNetworkData().add ("interaction", edge_name,
 		// interaction_type);
 		// Cytoscape.getEdgeNetworkData().addNameMapping (edge_name, edge);
 		// return edge;
 	}
 
 	/**
 	 * Returns the requested Attribute for the given Node
 	 * 
 	 * @param node
 	 *            the given CyNode
 	 * @param attribute
 	 *            the name of the requested attribute
 	 * @return the value for the give node, for the given attribute.
 	 * @deprecated Use {@link CyAttributes} directly. This method will be
 	 *             removed in September, 2006.
 	 */
 	public static Object getNodeAttributeValue(Node node, String attribute) {
 		final CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
 		final String canonName = node.getIdentifier();
 		final byte cyType = nodeAttrs.getType(attribute);
 		if (cyType == CyAttributes.TYPE_BOOLEAN) {
 			return nodeAttrs.getBooleanAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_FLOATING) {
 			return nodeAttrs.getDoubleAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_INTEGER) {
 			return nodeAttrs.getIntegerAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_STRING) {
 			return nodeAttrs.getStringAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_SIMPLE_LIST) {
 			return nodeAttrs.getAttributeList(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_SIMPLE_MAP) {
 			return nodeAttrs.getAttributeMap(canonName, attribute);
 		} else {
 			// As a last resort, check the GOB for arbitary objects.
 			return nodeData.get(attribute, canonName);
 		}
 	}
 
 	/**
 	 * Returns the requested Attribute for the given Edge
 	 * 
 	 * @deprecated Use {@link CyAttributes} directly. This method will be
 	 *             removed in September, 2006.
 	 */
 	public static Object getEdgeAttributeValue(Edge edge, String attribute) {
 		final CyAttributes edgeAttrs = Cytoscape.getEdgeAttributes();
 		final String canonName = edge.getIdentifier();
 		final byte cyType = edgeAttrs.getType(attribute);
 		if (cyType == CyAttributes.TYPE_BOOLEAN) {
 			return edgeAttrs.getBooleanAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_FLOATING) {
 			return edgeAttrs.getDoubleAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_INTEGER) {
 			return edgeAttrs.getIntegerAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_STRING) {
 			return edgeAttrs.getStringAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_SIMPLE_LIST) {
 			return edgeAttrs.getAttributeList(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_SIMPLE_MAP) {
 			return edgeAttrs.getAttributeMap(canonName, attribute);
 		} else {
 			// As a last resort, check the GOB for arbitary objects.
 			return edgeData.get(attribute, canonName);
 		}
 	}
 
 	private static Object private_getEdgeAttributeValue(Edge edge,
 			String attribute) {
 		final CyAttributes edgeAttrs = Cytoscape.getEdgeAttributes();
 		final String canonName = edge.getIdentifier();
 		final byte cyType = edgeAttrs.getType(attribute);
 		if (cyType == CyAttributes.TYPE_BOOLEAN) {
 			return edgeAttrs.getBooleanAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_FLOATING) {
 			return edgeAttrs.getDoubleAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_INTEGER) {
 			return edgeAttrs.getIntegerAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_STRING) {
 			return edgeAttrs.getStringAttribute(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_SIMPLE_LIST) {
 			return edgeAttrs.getAttributeList(canonName, attribute);
 		} else if (cyType == CyAttributes.TYPE_SIMPLE_MAP) {
 			return edgeAttrs.getAttributeMap(canonName, attribute);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * Return all availble Attributes for the Nodes in this CyNetwork.
 	 * 
 	 * @deprecated Use {@link CyAttributes} directly. This method will be
 	 *             removed in September, 2006.
 	 */
 	public static String[] getNodeAttributesList() {
 		return Cytoscape.getNodeAttributes().getAttributeNames();
 	}
 
 	/**
 	 * Return all available Attributes for the given Nodes.
 	 * 
 	 * @deprecated Use {@link CyAttributes} directly. This method will be
 	 *             removed in September, 2006.
 	 */
 	public static String[] getNodeAttributesList(Node[] nodes) {
 		return Cytoscape.getNodeAttributes().getAttributeNames();
 	}
 
 	/**
 	 * Return all availble Attributes for the Edges in this CyNetwork.
 	 * 
 	 * @deprecated Use {@link CyAttributes} directly. This method will be
 	 *             removed in September, 2006.
 	 */
 	public static String[] getEdgeAttributesList() {
 		return Cytoscape.getEdgeAttributes().getAttributeNames();
 	}
 
 	/**
 	 * Return all available Attributes for the given Edges
 	 * 
 	 * @deprecated Use {@link CyAttributes} directly. This method will be
 	 *             removed in September, 2006.
 	 */
 	public static String[] getNodeAttributesList(Edge[] edges) {
 		return Cytoscape.getEdgeAttributes().getAttributeNames();
 	}
 
 	/**
 	 * Return the requested Attribute for the given Node
 	 * 
 	 * @param node
 	 *            the given CyNode
 	 * @param attribute
 	 *            the name of the requested attribute
 	 * @param value
 	 *            the value to be set
 	 * @return if it overwrites a previous value
 	 * @deprecated Use {@link CyAttributes} directly. This method will be
 	 *             removed in September, 2006.
 	 */
 	public static boolean setNodeAttributeValue(Node node, String attribute,
 			Object value) {
 		final CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
 		final String canonName = node.getIdentifier();
 		if (value instanceof Boolean) {
 			nodeAttrs.setAttribute(canonName, attribute, (Boolean) value);
 			return true;
 		} else if (value instanceof Integer) {
 			nodeAttrs.setAttribute(canonName, attribute, (Integer) value);
 			return true;
 		} else if (value instanceof Double) {
 			nodeAttrs.setAttribute(canonName, attribute, (Double) value);
 			return true;
 		} else if (value instanceof String) {
 			nodeAttrs.setAttribute(canonName, attribute, (String) value);
 			return true;
 		} else {
 			// If this is an arbitary object, use GOB for backward
 			// compatibility.
 			nodeData.set(attribute, canonName, value);
 		}
 		return false;
 	}
 
 	/**
 	 * Return the requested Attribute for the given Edge
 	 * 
 	 * @deprecated Use {@link CyAttributes} directly. This method will be
 	 *             removed in September, 2006.
 	 */
 	public static boolean setEdgeAttributeValue(Edge edge, String attribute,
 			Object value) {
 		final CyAttributes edgeAttrs = Cytoscape.getEdgeAttributes();
 		final String canonName = edge.getIdentifier();
 		if (value instanceof Boolean) {
 			edgeAttrs.setAttribute(canonName, attribute, (Boolean) value);
 			return true;
 		} else if (value instanceof Integer) {
 			edgeAttrs.setAttribute(canonName, attribute, (Integer) value);
 			return true;
 		} else if (value instanceof Double) {
 			edgeAttrs.setAttribute(canonName, attribute, (Double) value);
 			return true;
 		} else if (value instanceof String) {
 			edgeAttrs.setAttribute(canonName, attribute, (String) value);
 			return true;
 		} else {
 			// If this is an arbitary object, use GOB for backward
 			// compatibility.
 			edgeData.set(attribute, canonName, value);
 		}
 		return false;
 	}
 
 	/**
 	 * @deprecated argh!...
 	 */
 	private static String canonicalizeName(String name) {
 		String canonicalName = name;
 
 		if (bioDataServer != null) {
 			canonicalName = bioDataServer.getCanonicalName(species, name);
 			if (canonicalName == null) {
 				canonicalName = name;
 			}
 		}
 		return canonicalName;
 	}
 
 	/**
 	 * @deprecated argh!...
 	 */
 	public static void setSpecies() {
 		species = CytoscapeInit.getProperties().getProperty("defaultSpeciesName");
 	}
 
 	// --------------------//
 	// Network Methods
 	// --------------------//
 
 	/**
 	 * Return the Network that currently has the Focus. Can be different from
 	 * getCurrentNetworkView
 	 */
 	public static CyNetwork getCurrentNetwork() {
		if (currentNetworkID == null)
 			return nullNetwork;
 
 		CyNetwork network = (CyNetwork) getNetworkMap().get(currentNetworkID);
 		return network;
 	}
 
 	/**
 	 * Return a List of all available CyNetworks
 	 */
 	public static Set getNetworkSet() {
 		return new java.util.LinkedHashSet(((HashMap) getNetworkMap()).values());
 	}
 
 	/**
 	 * @return the CyNetwork that has the given identifier or null if there is
 	 *         no such network
 	 */
 	public static CyNetwork getNetwork(String id) {
		if (getNetworkMap().containsKey(id))
 			return (CyNetwork) getNetworkMap().get(id);
 		return nullNetwork;
 	}
 
 	/**
 	 * @return a CyNetworkView for the given ID, if one exists, otherwise
 	 *         returns null
 	 */
 	public static CyNetworkView getNetworkView(String network_id) {
		if (network_id == null)
 			return nullNetworkView;
 
 		CyNetworkView nview = (CyNetworkView) getNetworkViewMap().get(
 				network_id);
 		return nview;
 	}
 
 	/**
 	 * @return if a view exists for a given network id
 	 */
 	public static boolean viewExists(String network_id) {
 		return getNetworkViewMap().containsKey(network_id);
 	}
 
 	/**
 	 * Return the CyNetworkView that currently has the focus. Can be different
 	 * from getCurrentNetwork
 	 */
 	public static CyNetworkView getCurrentNetworkView() {
		if (currentNetworkViewID == null)
 			return nullNetworkView;
 
 		// System.out.println( "Cytoscape returning current network view:
 		// "+currentNetworkViewID );
 
 		CyNetworkView nview = (CyNetworkView) getNetworkViewMap().get(
 				currentNetworkViewID);
 		return nview;
 	}
 
 	/**
 	 * @return the reference to the One CytoscapeDesktop
 	 */
 	public static CytoscapeDesktop getDesktop() {
 		if (defaultDesktop == null) {
 			// System.out.println( " Defaultdesktop created: "+defaultDesktop );
 			defaultDesktop = new CytoscapeDesktop(CytoscapeDesktop
 					.parseViewType(CytoscapeInit.getProperties().getProperty("viewType")));
 		}
 		return defaultDesktop;
 	}
 
 	/**
 	 * @deprecated
 	 */
 	public static void setCurrentNetwork(String id) {
 		if (getNetworkMap().containsKey(id))
 			currentNetworkID = id;
 
 		// System.out.println( "Currentnetworkid is: "+currentNetworkID+ " set
 		// from : "+id );
 
 	}
 
 	/**
 	 * @deprecated
 	 * @return true if there is network view, false if not
 	 */
 	public static boolean setCurrentNetworkView(String id) {
 		if (getNetworkViewMap().containsKey(id)) {
 			currentNetworkViewID = id;
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * This Map has keys that are Strings ( network_ids ) and values that are
 	 * networks.
 	 */
 	protected static Map getNetworkMap() {
 		if (networkMap == null) {
 			networkMap = new HashMap();
 		}
 		return networkMap;
 	}
 
 	/**
 	 * This Map has keys that are Strings ( network_ids ) and values that are
 	 * networkviews.
 	 */
 	public static Map getNetworkViewMap() {
 		if (networkViewMap == null) {
 			networkViewMap = new HashMap();
 		}
 		return networkViewMap;
 	}
 
 	/**
 	 * destroys the given network
 	 */
 	public static void destroyNetwork(String network_id) {
 		destroyNetwork((CyNetwork) getNetworkMap().get(network_id));
 	}
 
 	/**
 	 * destroys the given network
 	 */
 	public static void destroyNetwork(CyNetwork network) {
 		destroyNetwork(network, false);
 	}
 
 	/**
 	 * destroys the given network
 	 * 
 	 * @param network
 	 *            the network tobe destroyed
 	 * @param destroy_unique
 	 *            if this is true, then all Nodes and Edges that are in this
 	 *            network, but no other are also destroyed.
 	 */
 	public static void destroyNetwork(CyNetwork network, boolean destroy_unique) {
 
 		getNetworkMap().remove(network.getIdentifier());
 		if (viewExists(network.getIdentifier()))
 			destroyNetworkView(network);
 		firePropertyChange(NETWORK_DESTROYED, null, network.getIdentifier());
 
 		if (destroy_unique) {
 
 			ArrayList nodes = new ArrayList();
 			ArrayList edges = new ArrayList();
 
 			Collection networks = networkMap.values();
 
 			Iterator nodes_i = network.nodesIterator();
 			Iterator edges_i = network.edgesIterator();
 
 			while (nodes_i.hasNext()) {
 				Node node = (Node) nodes_i.next();
 				boolean add = true;
 				for (Iterator n_i = networks.iterator(); n_i.hasNext();) {
 					CyNetwork net = (CyNetwork) n_i.next();
 					if (net.containsNode(node)) {
 						add = false;
 						continue;
 					}
 				}
 				if (add) {
 					nodes.add(node);
 				}
 			}
 
 			while (edges_i.hasNext()) {
 				Edge edge = (Edge) edges_i.next();
 				boolean add = true;
 				for (Iterator n_i = networks.iterator(); n_i.hasNext();) {
 					CyNetwork net = (CyNetwork) n_i.next();
 					if (net.containsEdge(edge)) {
 						add = false;
 						continue;
 					}
 				}
 				if (add) {
 					edges.add(edge);
 				}
 			}
 
 			getRootGraph().removeNodes(nodes);
 			getRootGraph().removeEdges(edges);
 
 		}
 
 		// theoretically this should not be set to null till after the events
 		// firing is done
 		network = null;
 	}
 
 	/**
 	 * destroys the networkview, including any layout information
 	 */
 	public static void destroyNetworkView(CyNetworkView view) {
 
 		// System.out.println( "destroying: "+view.getIdentifier()+" :
 		// "+getNetworkViewMap().get( view.getIdentifier() ) );
 
 		getNetworkViewMap().remove(view.getIdentifier());
 
 		// System.out.println( "gone from hash: "+view.getIdentifier()+" :
 		// "+getNetworkViewMap().get( view.getIdentifier() ) );
 
 		firePropertyChange(CytoscapeDesktop.NETWORK_VIEW_DESTROYED, null, view);
 		// theoretically this should not be set to null till after the events
 		// firing is done
 		view = null;
 		// TODO: do we want here?
 		System.gc();
 	}
 
 	/**
 	 * destroys the networkview, including any layout information
 	 */
 	public static void destroyNetworkView(String network_view_id) {
 		destroyNetworkView((CyNetworkView) getNetworkViewMap().get(
 				network_view_id));
 	}
 
 	/**
 	 * destroys the networkview, including any layout information
 	 */
 	public static void destroyNetworkView(CyNetwork network) {
 		destroyNetworkView((CyNetworkView) getNetworkViewMap().get(
 				network.getIdentifier()));
 	}
 
 	protected static void addNetwork(CyNetwork network, String title,
 			CyNetwork parent, boolean create_view) {
 
 		// System.out.println( "CyNetwork Added: "+network.getIdentifier() );
 
 		getNetworkMap().put(network.getIdentifier(), network);
 		network.setTitle(title);
 		String p_id = null;
 		if (parent != null) {
 			p_id = parent.getIdentifier();
 		}
 
 		firePropertyChange(NETWORK_CREATED, p_id, network.getIdentifier());
 		if (network.getNodeCount() < Integer.parseInt(CytoscapeInit
 				.getProperties().getProperty("viewThreshold"))
 				&& create_view) {
 			createNetworkView(network);
 		}
 	}
 
 	/**
 	 * Creates a new, empty Network.
 	 * 
 	 * @param title
 	 *            the title of the new network.
 	 */
 	public static CyNetwork createNetwork(String title) {
 		return createNetwork(title, true);
 	}
 
 	/**
 	 * Creates a new, empty Network.
 	 * 
 	 * @param title
 	 *            the title of the new network.
 	 * @param create_view
 	 *            if the size of the network is under the node limit, create a
 	 *            view
 	 */
 	public static CyNetwork createNetwork(String title, boolean create_view) {
 		CyNetwork network = getRootGraph().createNetwork(new int[] {},
 				new int[] {});
 		addNetwork(network, title, null, false);
 		return network;
 	}
 
 	/**
 	 * Creates a new Network
 	 * 
 	 * @param nodes
 	 *            the indeces of nodes
 	 * @param edges
 	 *            the indeces of edges
 	 * @param title
 	 *            the title of the new network.
 	 */
 	public static CyNetwork createNetwork(int[] nodes, int[] edges, String title) {
 		CyNetwork network = getRootGraph().createNetwork(nodes, edges);
 		addNetwork(network, title, null, true);
 		return network;
 	}
 
 	/**
 	 * Creates a new Network
 	 * 
 	 * @param nodes
 	 *            a collection of nodes
 	 * @param edges
 	 *            a collection of edges
 	 * @param title
 	 *            the title of the new network.
 	 */
 	public static CyNetwork createNetwork(Collection nodes, Collection edges,
 			String title) {
 		CyNetwork network = getRootGraph().createNetwork(nodes, edges);
 		addNetwork(network, title, null, true);
 		return network;
 	}
 
 	/**
 	 * Creates a new Network, that inherits from the given ParentNetwork
 	 * 
 	 * @param nodes
 	 *            the indeces of nodes
 	 * @param edges
 	 *            the indeces of edges
 	 * @param child_title
 	 *            the title of the new network.
 	 * @param param
 	 *            the parent of the this Network
 	 */
 	public static CyNetwork createNetwork(int[] nodes, int[] edges,
 			String child_title, CyNetwork parent) {
 		CyNetwork network = getRootGraph().createNetwork(nodes, edges);
 		addNetwork(network, child_title, parent, true);
 		return network;
 	}
 
 	/**
 	 * Creates a new Network, that inherits from the given ParentNetwork
 	 * 
 	 * @param nodes
 	 *            the indeces of nodes
 	 * @param edges
 	 *            the indeces of edges
 	 * @param param
 	 *            the parent of the this Network
 	 */
 	public static CyNetwork createNetwork(Collection nodes, Collection edges,
 			String child_title, CyNetwork parent) {
 		CyNetwork network = getRootGraph().createNetwork(nodes, edges);
 		addNetwork(network, child_title, parent, true);
 		return network;
 	}
 
 	/**
 	 * Creates a cytoscape.data.CyNetwork from a file. The file type is
 	 * determined by the suffice of the file
 	 * <ul>
 	 * <li> sif -- Simple Interaction File</li>
 	 * <li> gml -- Graph Markup Languange</li>
 	 * <li> sbml -- SBML</li>
 	 * </ul>
 	 * 
 	 * @param location
 	 *            the location of the file
 	 */
 	public static CyNetwork createNetworkFromFile(String location) {
 		return createNetwork(location, FILE_BY_SUFFIX, false, null, null);
 	}
 
 	/**
 	 * Creates a cytoscape.data.CyNetwork from a file. The passed variable
 	 * determines the type of file, i.e. GML, SIF, SBML, etc.
 	 * <p>
 	 * This operation may take a long time to complete. It is a good idea NOT to
 	 * call this method from the AWT event handling thread.
 	 * 
 	 * @param location
 	 *            the location of the file
 	 * @param file_type
 	 *            the type of file GML, SIF, SBML, etc.
 	 * @param canonicalize
 	 *            this will set the preferred display name to what is on the
 	 *            server.
 	 * @param biodataserver
 	 *            provides the name conversion service
 	 * @param species
 	 *            the species used by the BioDataServer
 	 */
 	public static CyNetwork createNetwork(String location, int file_type,
 			boolean canonicalize, BioDataServer biodataserver, String species) {
 		// return null for a null file
 		if (location == null)
 			return null;
 
 		GraphReader reader;
 
 		// set the reader according to what file type was passed.
 		if (file_type == FILE_SIF
 				|| (file_type == FILE_BY_SUFFIX && location.endsWith("sif"))) {
 			reader = new InteractionsReader(biodataserver, species, location);
 		} else if (file_type == FILE_GML
 				|| (file_type == FILE_BY_SUFFIX && location.endsWith("gml"))) {
 			reader = new GMLReader2(location);
 		} else if (file_type == FILE_XGMML
 				|| (file_type == FILE_BY_SUFFIX && location.endsWith("xgmml")) 
 				|| (file_type == FILE_BY_SUFFIX && location.endsWith("xml"))) {
 			reader = new XGMMLReader(location); 
 		} else {
 			// TODO: come up with a really good way of supporting arbitrary
 			// file types via plugin support.
 			System.err.println("File Type not Supported, sorry");
 			return Cytoscape.createNetwork(null);
 		}
 
 		// have the GraphReader read the given file
 		try {
 			reader.read();
 		} catch (Exception e) {
 
 			// JOptionPane.showMessageDialog(Cytoscape.getDesktop(),e.getMessage(),"Error
 			// reading graph file",JOptionPane.ERROR_MESSAGE);
 			System.err.println("Cytoscape: Error Reading Network File: "
 					+ location + "\n--------------------\n");
 			e.printStackTrace();
 			return null;
 		}
 
 		// get the RootGraph indices of the nodes and
 		// edges that were just created
 		int[] nodes = reader.getNodeIndicesArray();
 		int[] edges = reader.getEdgeIndicesArray();
 
 		if (nodes == null) {
 			System.err.println("reader returned null nodes");
 		}
 
 		if (edges == null) {
 			System.err.println("reader returned null edges");
 		}
 
 		String[] title = location.split("/");
 		if (System.getProperty("os.name").startsWith("Win")) {
 			title = location.split("//");
 		}
 
 		// Create a new cytoscape.data.CyNetwork from these nodes and edges
 		CyNetwork network = createNetwork(nodes, edges, CyNetworkNaming
 				.getSuggestedNetworkTitle(title[title.length - 1]));
 
 		if (file_type == FILE_GML
 				|| (file_type == FILE_BY_SUFFIX && location.endsWith("gml"))) {
 
 			System.out.println("GML file gettign reader: "
 					+ title[title.length - 1]);
 			network.putClientData("GML", reader);
 		}
 
 		System.out.println("NV: " + getNetworkView(network.getIdentifier()));
 
 		if (getNetworkView(network.getIdentifier()) != null) {
 			reader.layout(getNetworkView(network.getIdentifier()));
 		}
 
 		return network;
 
 	}
 
 	// --------------------//
 	// Network Data Methods
 	// --------------------//
 
 	/**
 	 * @deprecated
 	 */
 	public static CytoscapeObj getCytoscapeObj() {
 		return new CytoscapeObj();
 	}
 
 	/**
 	 * Gets Node Network Data: GraphObjAttributes.
 	 * 
 	 * @return GraphObjAttributes Object.
 	 * @deprecated Use {@link Cytoscape#getNodeAttributes()} instead. This
 	 *             method will be removed in September, 2006.
 	 */
 	public static GraphObjAttributes getNodeNetworkData() {
 		return nodeData;
 	}
 
 	/**
 	 * Gets Edge Network Data: GraphObjAttributes.
 	 * 
 	 * @return GraphObjAttributes Object.
 	 * @deprecated Use {@link Cytoscape#getEdgeAttributes()} instead. This
 	 *             method will be removed in September, 2006.
 	 */
 	public static GraphObjAttributes getEdgeNetworkData() {
 		return edgeData;
 	}
 
 	/**
 	 * Gets Global Node Attributes.
 	 * 
 	 * @return CyAttributes Object.
 	 */
 	public static CyAttributes getNodeAttributes() {
 		return nodeAttributes;
 	}
 
 	/**
 	 * Gets Global Edge Attributes
 	 * 
 	 * @return CyAttributes Object.
 	 */
 	public static CyAttributes getEdgeAttributes() {
 		return edgeAttributes;
 	}
 
 	/**
 	 * Gets Global Network Attributes.
 	 * 
 	 * @return CyAttributes Object.
 	 */
 	public static CyAttributes getNetworkAttributes() {
 		return networkAttributes;
 	}
 
 	public static ExpressionData getExpressionData() {
 		return expressionData;
 	}
 
 	public static void setExpressionData(ExpressionData expData) {
 		expressionData = expData;
 	}
 
 	/**
 	 * Load Expression Data
 	 */
 	// TODO: remove the JOption Pane stuff
 	public static boolean loadExpressionData(String filename, boolean copy_atts) {
 		try {
 			expressionData = new ExpressionData(filename);
 		} catch (Exception e) {
 			System.err.println("Unable to Load Expression Data");
 			String errString = "Unable to load expression data from "
 					+ filename;
 			String title = "Load Expression Data";
 
 		}
 
 		if (copy_atts) {
 			expressionData.copyToAttribs(getNodeAttributes(), null);
 			firePropertyChange(ATTRIBUTES_CHANGED, null, null);
 		}
 
 		// Fire off an EXPRESSION_DATA_LOADED event.
 		Cytoscape.firePropertyChange(Cytoscape.EXPRESSION_DATA_LOADED, null,
 				expressionData);
 
 		return true;
 	}
 
 	/**
 	 * Loads Node and Edge attribute data into Cytoscape from the given file
 	 * locations. Currently, the only supported attribute types are of the type
 	 * "name = value".
 	 * 
 	 * @param nodeAttrLocations
 	 *            an array of node attribute file locations. May be null.
 	 * @param edgeAttrLocations
 	 *            an array of edge attribute file locations. May be null.
 	 * @param canonicalize
 	 *            convert to the preffered name on the biodataserver
 	 * @param bioDataServer
 	 *            provides the name conversion service
 	 * @param species
 	 *            the species to use with the bioDataServer's
 	 */
 	public static void loadAttributes(String[] nodeAttrLocations,
 			String[] edgeAttrLocations, boolean canonicalize,
 			BioDataServer bioDataServer, String species) {
 
 		// check to see if there are Node Attributes passed
 		if (nodeAttrLocations != null) {
 			for (int i = 0; i < nodeAttrLocations.length; ++i) {
 				try {
 					InputStreamReader reader = new InputStreamReader(FileUtil
 							.getInputStream(nodeAttrLocations[i]));
 					CyAttributesReader.loadAttributes(nodeAttributes, reader);
 					firePropertyChange(ATTRIBUTES_CHANGED, null, null);
 				} catch (Exception e) {
 					throw new IllegalArgumentException(
 							"Failure loading node attribute data: "
 									+ nodeAttrLocations[i]);
 				}
 			}
 		}
 
 		// Check to see if there are Edge Attributes Passed
 		if (edgeAttrLocations != null) {
 			for (int j = 0; j < edgeAttrLocations.length; ++j) {
 				try {
 					InputStreamReader reader = new InputStreamReader(FileUtil
 							.getInputStream(edgeAttrLocations[j]));
 					CyAttributesReader.loadAttributes(edgeAttributes, reader);
 					firePropertyChange(ATTRIBUTES_CHANGED, null, null);
 				} catch (Exception e) {
 					throw new IllegalArgumentException(
 							"Failure loading edge attribute data: "
 									+ edgeAttrLocations[j]);
 				}
 			}
 		}
 
 	}
 
 	/**
 	 * Loads Node and Edge attribute data into Cytoscape from the given file
 	 * locations. Currently, the only supported attribute types are of the type
 	 * "name = value".
 	 * 
 	 * @param nodeAttrLocations
 	 *            an array of node attribute file locations. May be null.
 	 * @param edgeAttrLocations
 	 *            an array of edge attribute file locations. May be null.
 	 */
 	public static void loadAttributes(String[] nodeAttrLocations,
 			String[] edgeAttrLocations) {
 		loadAttributes(nodeAttrLocations, edgeAttrLocations, false, null, null);
 	}
 
 	/**
 	 * Constructs a network using information from a CyProject argument that
 	 * contains information on the location of the graph file, any node/edge
 	 * attribute files, and a possible expression data file. If the data server
 	 * argument is non-null and the project requests canonicalization, the data
 	 * server will be used for name resolution given the names in the
 	 * graph/attributes files.
 	 * 
 	 * @see CyProject
 	 * @deprecated Will be removed Oct 2006. This is not apparently used, so
 	 *             don't start. This functionality has been subsumed by
 	 *             Cytoscape Sessions.
 	 */
 	public static CyNetwork createNetworkFromProject(CyProject project,
 			BioDataServer bioDataServer) {
 		if (project == null) {
 			return null;
 		}
 
 		boolean canonicalize = project.getCanonicalize();
 		String species = project.getDefaultSpeciesName();
 		CyNetwork network = null;
 		if (project.getInteractionsFilename() != null) {
 			// read graph from interaction data
 			String filename = project.getInteractionsFilename();
 			network = createNetwork(filename, Cytoscape.FILE_SIF, canonicalize,
 					bioDataServer, species);
 		} else if (project.getGeometryFilename() != null) {
 			// read a GML file
 			String filename = project.getGeometryFilename();
 			network = createNetwork(filename, Cytoscape.FILE_GML, false, null,
 					null);
 
 		}
 
 		if (network == null) {// no graph specified, or unable to read
 			// create a default network
 			network = createNetwork(null);
 		}
 
 		// load attributes files
 		String[] nodeAttributeFilenames = project.getNodeAttributeFilenames();
 		String[] edgeAttributeFilenames = project.getEdgeAttributeFilenames();
 		loadAttributes(nodeAttributeFilenames, edgeAttributeFilenames,
 				canonicalize, bioDataServer, species);
 		// load expression data
 		// ExpressionData expData = null;
 		// if (project.getExpressionFilename() != null) {
 		// expData = new ExpressionData( project.getExpressionFilename() );
 		// network.setExpressionData(expData);
 		// }
 		loadExpressionData(project.getExpressionFilename(), true);
 
 		return network;
 	}
 
 	/**
 	 * A BioDataServer should be loadable from a file systems file or from a
 	 * URL.
 	 */
 	public static BioDataServer loadBioDataServer(String location) {
 		try {
 			bioDataServer = new BioDataServer(location);
 		} catch (Exception e) {
 			System.err
 					.println("Could not Load BioDataServer from: " + location);
 			return null;
 		}
 		return bioDataServer;
 	}
 
 	/**
 	 * @return the BioDataServer that was loaded, should not be null, but not
 	 *         contain any data.
 	 */
 	public static BioDataServer getBioDataServer() {
 		return bioDataServer;
 	}
 
 	// ------------------------------//
 	// CyNetworkView Creation Methods
 	// ------------------------------//
 
 	/**
 	 * Creates a CyNetworkView, but doesn't do anything with it. Ifnn's you want
 	 * to use it
 	 * 
 	 * @link {CytoscapeDesktop}
 	 * @param network
 	 *            the network to create a view of
 	 */
 	public static CyNetworkView createNetworkView(CyNetwork network) {
 		return createNetworkView(network, network.getTitle());
 	}
 
 	/**
 	 * Creates a CyNetworkView, but doesn't do anything with it. Ifnn's you want
 	 * to use it
 	 * 
 	 * @link {CytoscapeDesktop}
 	 * @param network
 	 *            the network to create a view of
 	 */
 	public static CyNetworkView createNetworkView(CyNetwork network,
 			String title) {
 
 		if (network == nullNetwork) {
 			return nullNetworkView;
 		}
 		if (viewExists(network.getIdentifier())) {
 			return getNetworkView(network.getIdentifier());
 		}
 		final DingNetworkView view = new DingNetworkView(network, title);
 		view.setIdentifier(network.getIdentifier());
 		view.setGraphLOD(new CyGraphLOD());
 		getNetworkViewMap().put(network.getIdentifier(), view);
 		view.setTitle(network.getTitle());
 
 		if (network.getClientData("GML") != null) {
 			((GraphReader) network.getClientData("GML")).layout(view);
 		}
 
 		else {
 			double distanceBetweenNodes = 80.0d;
 			int columns = (int) Math.sqrt(view.nodeCount());
 			Iterator nodeViews = view.getNodeViewsIterator();
 			double currX = 0.0d;
 			double currY = 0.0d;
 			int count = 0;
 			while (nodeViews.hasNext()) {
 				NodeView nView = (NodeView) nodeViews.next();
 				nView.setOffset(currX, currY);
 				count++;
 				if (count == columns) {
 					count = 0;
 					currX = 0.0d;
 					currY += distanceBetweenNodes;
 				} else {
 					currX += distanceBetweenNodes;
 				}
 			}
 		}
 
 		firePropertyChange(
 				cytoscape.view.CytoscapeDesktop.NETWORK_VIEW_CREATED, null,
 				view);
 
 		// Instead of calling fitContent(), access PGrap*View directly.
 		// This enables us to disable animation. Modified by Ethan Cerami.
 		// SwingUtilities.invokeLater(new Runnable() {
 		// public void run() {
 		// view.getCanvas().getCamera().animateViewToCenterBounds(
 		// view.getCanvas().getLayer().getFullBounds(), true, 0);
 		// // if Squiggle function enabled, enable it on the view
 		// if (squiggleEnabled) {
 		// view.getSquiggleHandler().beginSquiggling();
 		// }
 		// set the selection mode on the view
 		setSelectionMode(currentSelectionMode, view);
 		// }
 		// });
 		view.fitContent();
 		return view;
 	}
 
 	public static void firePropertyChange(String property_type,
 			Object old_value, Object new_value) {
 
 		PropertyChangeEvent e = new PropertyChangeEvent(pcsO, property_type,
 				old_value, new_value);
 		// System.out.println("Cytoscape FIRING : " + property_type);
 
 		getSwingPropertyChangeSupport().firePropertyChange(e);
 		getPropertyChangeSupport().firePropertyChange(e);
 	}
 
 	private static void setSquiggleState(boolean isEnabled) {
 
 		// enable Squiggle on all network views
 		// PGrap*View view;
 		// String network_id;
 		// Map networkViewMap = getNetworkViewMap();
 		// for (Iterator iter = networkViewMap.keySet().iterator();
 		// iter.hasNext();) {
 		// network_id = (String) iter.next();
 		// view = (PGrap*View) networkViewMap.get(network_id);
 		// if (isEnabled) {
 		// view.getSquiggleHandler().beginSquiggling();
 		// } else {
 		// view.getSquiggleHandler().stopSquiggling();
 		// }
 		// }
 
 	}
 
 	/**
 	 * Utility method to enable Squiggle function.
 	 * @deprecated Squiggle is gone and we don't expect the functionality to return. 
 	 * if this causes major problems, let us know.  This method will be removed Sept 2006.
 	 */
 	public static void enableSquiggle() {
 
 		// set the global flag to indicate that Squiggle is enabled
 		squiggleEnabled = true;
 		setSquiggleState(true);
 
 	}
 
 	/**
 	 * Utility method to disable Squiggle function.
 	 * @deprecated Squiggle is gone and we don't expect the functionality to return. 
 	 * if this causes major problems, let us know.  This method will be removed Sept 2006.
 	 */
 	public static void disableSquiggle() {
 
 		// set the global flag to indicate that Squiggle is disabled
 		squiggleEnabled = false;
 		setSquiggleState(false);
 
 	}
 
 	/**
 	 * Returns the value of the global flag to indicate whether the Squiggle
 	 * function is enabled.
 	 * @deprecated Squiggle is gone and we don't expect the functionality to return. 
 	 * if this causes major problems, let us know.  This method will be removed Sept 2006.
 	 */
 	public static boolean isSquiggleEnabled() {
 		return squiggleEnabled;
 	}
 
 	/**
 	 * Gets the selection mode value.
 	 */
 	public static int getSelectionMode() {
 		return currentSelectionMode;
 	}
 
 	/**
 	 * Sets the specified selection mode on all views.
 	 * 
 	 * @param selectionMode
 	 *            SELECT_NODES_ONLY, SELECT_EDGES_ONLY, or
 	 *            SELECT_NODES_AND_EDGES.
 	 */
 	public static void setSelectionMode(int selectionMode) {
 
 		// set the selection mode on all the views
 		GraphView view;
 		String network_id;
 		Map networkViewMap = getNetworkViewMap();
 		for (Iterator iter = networkViewMap.keySet().iterator(); iter.hasNext();) {
 			network_id = (String) iter.next();
 			view = (GraphView) networkViewMap.get(network_id);
 			setSelectionMode(selectionMode, view);
 		}
 
 		// update the global indicating the selection mode
 		currentSelectionMode = selectionMode;
 
 	}
 
 	/**
 	 * Utility method to set the selection mode on the specified GraphView.
 	 * 
 	 * @param selectionMode
 	 *            SELECT_NODES_ONLY, SELECT_EDGES_ONLY, or
 	 *            SELECT_NODES_AND_EDGES.
 	 * @param view
 	 *            the GraphView to set the selection mode on.
 	 */
 	public static void setSelectionMode(int selectionMode, GraphView view) {
 
 		// first, disable node and edge selection on the view
 		view.disableNodeSelection();
 		view.disableEdgeSelection();
 
 		// then, based on selection mode, enable node and/or edge selection
 		switch (selectionMode) {
 
 		case SELECT_NODES_ONLY:
 			view.enableNodeSelection();
 			break;
 
 		case SELECT_EDGES_ONLY:
 			view.enableEdgeSelection();
 			break;
 
 		case SELECT_NODES_AND_EDGES:
 			view.enableNodeSelection();
 			view.enableEdgeSelection();
 			break;
 
 		}
 
 	}
 
 	/**
 	 * Get name of the current session file.
 	 * 
 	 * @return current session file name
 	 */
 	public static String getCurrentSessionFileName() {
 		return currentSessionFileName;
 	}
 
 	/**
 	 * Once session writer is called, the file name is stored here.
 	 * 
 	 * @param newName
 	 */
 	public static void setCurrentSessionFileName(String newName) {
 		currentSessionFileName = newName;
 	}
 
 	public static void setSessionState(int state) {
 		sessionState = state;
 	}
 
 	public static int getSessionstate() {
 		return sessionState;
 	}
 
 	public static void createNewSession() {
 		Set netSet = getNetworkSet();
 		Iterator it = netSet.iterator();
 
 		while (it.hasNext()) {
 			CyNetwork net = (CyNetwork) it.next();
 			destroyNetwork(net);
 		}
 
 		// Clear node attributes
 		CyAttributes nodeAttributes = getNodeAttributes();
 		String[] nodeAttrNames = nodeAttributes.getAttributeNames();
 		for (int i = 0; i < nodeAttrNames.length; i++) {
 			nodeAttributes.deleteAttribute(nodeAttrNames[i]);
 		}
 
 		// Clear edge attributes
 		CyAttributes edgeAttributes = getEdgeAttributes();
 		String[] edgeAttrNames = edgeAttributes.getAttributeNames();
 		for (int i = 0; i < edgeAttrNames.length; i++) {
 			edgeAttributes.deleteAttribute(edgeAttrNames[i]);
 		}
 		
 		// Clear network attributes
 		CyAttributes networkAttributes = getNetworkAttributes();
 		String[] networkAttrNames = networkAttributes.getAttributeNames();
 		for (int i = 0; i < networkAttrNames.length; i++) {
 			networkAttributes.deleteAttribute(networkAttrNames[i]);
 		}
 
 		getDesktop().setTitle("Cytoscape Desktop (New Session)");
 		setCurrentSessionFileName(null);
 
 	}
 
 }
