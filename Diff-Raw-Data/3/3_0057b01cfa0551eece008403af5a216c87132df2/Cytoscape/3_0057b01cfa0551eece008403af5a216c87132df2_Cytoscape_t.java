 //---------------------------------------------------------------------------
 package cytoscape;
 
 import giny.model.Edge;
 import giny.model.Node;
 import giny.view.GraphView;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeSupport;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.lang.reflect.InvocationTargetException;
 import java.io.FileReader;
 
 import javax.swing.*;
 import javax.swing.event.SwingPropertyChangeSupport;
 
 import cytoscape.data.*;
 import cytoscape.data.readers.GMLReader2;
 import cytoscape.data.readers.GraphReader;
 import cytoscape.data.readers.InteractionsReader;
 import cytoscape.data.readers.CyAttributesReader;
 import cytoscape.data.servers.BioDataServer;
 import cytoscape.giny.CytoscapeRootGraph;
 import cytoscape.giny.CytoscapeFingRootGraph;
 
 import cytoscape.giny.PhoebeNetworkView;
 import cytoscape.util.CyNetworkNaming;
 import cytoscape.view.CyNetworkView;
 import cytoscape.view.CytoscapeDesktop;
 import phoebe.PGraphView;
 
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
 
 	public static String NETWORK_CREATED = "NETWORK_CREATED";
 
 	public static String ATTRIBUTES_CHANGED = "ATTRIBUTES_CHANGED";
 
 	public static String EXPRESSION_DATA_LOADED = "EXPRESSION_DATA_LOADED";
 
 	public static String NETWORK_DESTROYED = "NETWORK_DESTROYED";
 
 	public static String CYTOSCAPE_EXIT = "CYTOSCAPE_EXIT";
 
 	// MLC 09/19/05 BEGIN:
 	// AJK: 09/12/05 BEGIN
 	// events for network modification
 	public static final String NETWORK_MODIFIED = "NETWORK_MODIFIED";
 
 	public static final String NETWORK_SAVED = "NETWORK_SAVED";
 
 	// AJK: 09/12/05 END
 	public static final String NETWORK_LOADED = "NETWORK_LOADED";
 
 	// MLC 09/19/05 END.
 
 	// KONO 10/10/05 BEGIN
 	// Events for Priference Dialog (properties).
 	public static final String PREFERENCE_MODIFIED = "PREFERENCE_MODIFIED";
 	// KONO 10/10/05 END
 
 	/**
 	 * When creating a network, use one of the standard suffixes to have it
 	 * parsed correctly<BR>
 	 * <ul>
 	 * <li> sif -- Simple Interaction File</li>
 	 * <li> gml -- Graph Markup Languange</li>
 	 * <li> sbml -- SBML</li>
 	 * </ul>
 	 */
 	public static int FILE_BY_SUFFIX = 0;
 
 	public static int FILE_GML = 1;
 
 	public static int FILE_SIF = 2;
 
 	public static int FILE_SBML = 3;
 
 	// constants for tracking selection mode globally
 	public static final int SELECT_NODES_ONLY = 1;
 
 	public static final int SELECT_EDGES_ONLY = 2;
 
 	public static final int SELECT_NODES_AND_EDGES = 3;
 
 	// global to represent which selection mode is active
 	private static int currentSelectionMode = SELECT_NODES_ONLY;
 
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
     private static GraphObjAttributes nodeData = new GraphObjAttributes
             (nodeAttributes);
 
     /**
      * Edge CyAttributes.
      */
     private static CyAttributes edgeAttributes = new CyAttributesImpl();
     private static GraphObjAttributes edgeData = new GraphObjAttributes
             (edgeAttributes);
 
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
 	 * A null CyNetwork to give when there is no Current Network
 	 */
 	protected static CyNetwork nullNetwork = getRootGraph().createNetwork(
 			new int[] {}, new int[] {});
 
 	/**
 	 * A null CyNetworkView to give when there is no Current NetworkView
 	 */
 	protected static CyNetworkView nullNetworkView = new PhoebeNetworkView(
 			nullNetwork, "null");
 
 	/**
 	 * Shuts down Cytoscape, after giving plugins time to react.
 	 */
 	public static void exit() {
 		// AJK: 09/12/05 BEGIN
 		// prompt the user about saving modified files before quitting
 		if (confirmQuit()) {
 			System.out.println("Cytoscape Exiting....");
 			try {
 				firePropertyChange(CYTOSCAPE_EXIT, null, "now");
 			} catch (Exception e) {
 				System.out.println("Errors on close, closed anyways.");
 			}
 
 			System.exit(0);
 		}
 		// System.exit(0);
 		// AJK: 09/12/05 END
 	}
 
 	// AJK: 09/12/05 BEGIN
 	// prompt the user about saving modified files before quitting
 	/**
 	 * prompt the user about saving modified files before quitting
 	 * 
 	 * @return
 	 */
 	private static boolean confirmQuit() {
 		String msg = "You have made modifications to the following networks:\n";
 		Set netSet = Cytoscape.getNetworkSet();
 		Iterator it = netSet.iterator();
 		int networkCount = 0;
 		// TODO: filter networks for only those modified
 		while (it.hasNext()) {
 			CyNetwork net = (CyNetwork) it.next();
 			boolean modified = CytoscapeModifiedNetworkManager.isModified(net);
 			if (modified) {
 				String name = net.getTitle();
 				msg += "     " + name + "\n";
 				networkCount++;
 			}
 		}
 		if (networkCount == 0) {
 			System.out.println("ConfirmQuit = " + true);
 			return true; // no networks have been modified
 		}
 		msg += "Are you sure you want to exit without saving?";
 		Object[] options = { "Yes, quit anyway.", "No, do not quit." };
 		int n = JOptionPane.showOptionDialog(Cytoscape.getDesktop(), msg,
 				"Save Networks Before Quitting?", JOptionPane.YES_NO_OPTION,
 				JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
 		if (n == JOptionPane.YES_OPTION) {
 			System.out.println("ConfirmQuit = " + true);
 			return true;
 		} else if (n == JOptionPane.NO_OPTION) {
 			System.out.println("ConfirmQuit = " + false);
 			return false;
 		} else {
 			System.out.println("ConfirmQuit = " + false);
 			return false; // default if dialog box is closed
 		}
 	}
 
 	// AJK: 09/12/05 END
 
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
 	 * @param alias
 	 *            an alias of a node
 	 * @param create
 	 *            will create a node if one does not exist
 	 * @return will always return a node, if <code>create</code> is true
 	 */
 	public static CyNode getCyNode(String alias, boolean create) {
 
 		String old_name = alias;
 		alias = canonicalizeName(alias);
 
                 CyNode node = Cytoscape.getRootGraph().getNode(alias);
 		if (node != null) {
 			// System.out.print(".");
 			return node;
 		}
 		// node does not exist, create one
 
 		if (!create) {
 			return null;
 		}
 
 		// System.out.print( "|" );
 		node = (CyNode) Cytoscape.getRootGraph().getNode(
 				Cytoscape.getRootGraph().createNode());
 		node.setIdentifier(alias);
                 getNodeAttributes().setAttribute(alias, Semantics.CANONICAL_NAME, alias);
 		// System.out.println( node.getRootGraphIndex()+" = Node: "+node+" alias
 		// :"+alias+" old_name: "+old_name );
 		// if ( old_name != alias )
 		// setNodeAttributeValue( node, "alias", old_name );
 // 		Cytoscape.getNodeNetworkData().addNameMapping(alias, node);
 		Semantics.assignNodeAliases(node, null, null);
 		return node;
 	}
 
 	/**
      * Gets the first CyEdge found between the two nodes (direction does not
      * matter) that has the given value for the given attribute.
      *
      * @param node_1          one end of the edge
      * @param node_2          the other end of the edge
      * @param attribute       the attribute of the edge to be searched, a
      *                        common one is {@link Semantics#INTERACTION }
      * @param attribute_value a value for the attribute, like "pp"
      * @param create          will create an edge if one does not exist and
      *                        if attribute is {@link Semantics#INTERACTION}
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
 				Object attValue = getEdgeAttributeValue(edge, attribute);
 
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
 
 		if (create && attribute instanceof String && attribute.equals
                 (Semantics.INTERACTION)) {
 		    // create the edge
             CyEdge edge = (CyEdge) Cytoscape.getRootGraph().getEdge(
 					Cytoscape.getRootGraph().createEdge(node_1, node_2));
 
             //  create the edge id
 			String edge_name = node_1.getIdentifier() + " (" + attribute_value
 					+ ") " + node_2.getIdentifier();
             edge.setIdentifier(edge_name);
 
            //  Store Edge Name Mapping within GOB.
            Cytoscape.getEdgeNetworkData().addNameMapping(edge_name, edge);

             //  store edge id as INTERACTION / CANONICAL_NAME Attributes
             edgeAttributes.setAttribute(edge_name, Semantics.INTERACTION,
                     (String) attribute_value);
             edgeAttributes.setAttribute(edge_name, Semantics.CANONICAL_NAME,
                     edge_name);
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
 	 * @param node          the given CyNode
 	 * @param attribute     the name of the requested attribute
 	 * @return the value for the give node, for the given attribute.
      * @deprecated Use {@link CyAttributes} directly.  This method will
      * be removed in September, 2006.
 	 */
 	public static Object getNodeAttributeValue(Node node, String attribute) {
           final CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
           final String canonName = node.getIdentifier();
           final byte cyType = nodeAttrs.getType(attribute);
           if (cyType == CyAttributes.TYPE_BOOLEAN) {
             return nodeAttrs.getBooleanAttribute(canonName, attribute); }
           else if (cyType == CyAttributes.TYPE_FLOATING) {
             return nodeAttrs.getDoubleAttribute(canonName, attribute); }
           else if (cyType == CyAttributes.TYPE_INTEGER) {
             return nodeAttrs.getIntegerAttribute(canonName, attribute); }
           else if (cyType == CyAttributes.TYPE_STRING) {
             return nodeAttrs.getStringAttribute(canonName, attribute); }
           else if (cyType == CyAttributes.TYPE_SIMPLE_LIST) {
             return nodeAttrs.getAttributeList(canonName, attribute); }
           else if (cyType == CyAttributes.TYPE_SIMPLE_MAP) {
             return nodeAttrs.getAttributeMap(canonName, attribute); }
           else {
             return null; }
 	}
 
 	/**
 	 * Returns the requested Attribute for the given Edge
      * @deprecated Use {@link CyAttributes} directly.  This method will
      * be removed in September, 2006.
 	 */
 	public static Object getEdgeAttributeValue(Edge edge, String attribute) {
           final CyAttributes edgeAttrs = Cytoscape.getEdgeAttributes();
           final String canonName = edge.getIdentifier();
           final byte cyType = edgeAttrs.getType(attribute);
           if (cyType == CyAttributes.TYPE_BOOLEAN) {
             return edgeAttrs.getBooleanAttribute(canonName, attribute); }
           else if (cyType == CyAttributes.TYPE_FLOATING) {
             return edgeAttrs.getDoubleAttribute(canonName, attribute); }
           else if (cyType == CyAttributes.TYPE_INTEGER) {
             return edgeAttrs.getIntegerAttribute(canonName, attribute); }
           else if (cyType == CyAttributes.TYPE_STRING) {
             return edgeAttrs.getStringAttribute(canonName, attribute); }
           else if (cyType == CyAttributes.TYPE_SIMPLE_LIST) {
             return edgeAttrs.getAttributeList(canonName, attribute); }
           else if (cyType == CyAttributes.TYPE_SIMPLE_MAP) {
             return edgeAttrs.getAttributeMap(canonName, attribute); }
           else {
             return null; }
 	}
 
 	/**
 	 * Return all availble Attributes for the Nodes in this CyNetwork.
      * @deprecated Use {@link CyAttributes} directly.  This method will
      * be removed in September, 2006.
 	 */
 	public static String[] getNodeAttributesList() {
           return Cytoscape.getNodeAttributes().getAttributeNames();
 	}
 
 	/**
 	 * Return all available Attributes for the given Nodes.
      * @deprecated Use {@link CyAttributes} directly.  This method will
      * be removed in September, 2006.
 	 */
 	public static String[] getNodeAttributesList(Node[] nodes) {
           return Cytoscape.getNodeAttributes().getAttributeNames();
 	}
 
 	/**
 	 * Return all availble Attributes for the Edges in this CyNetwork.
      * @deprecated Use {@link CyAttributes} directly.  This method will
      * be removed in September, 2006.
 	 */
 	public static String[] getEdgeAttributesList() {
           return Cytoscape.getEdgeAttributes().getAttributeNames();
 	}
 
 	/**
 	 * Return all available Attributes for the given Edges
      * @deprecated Use {@link CyAttributes} directly.  This method will
      * be removed in September, 2006.
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
      * @deprecated Use {@link CyAttributes} directly.  This method will
      * be removed in September, 2006.
 	 */
 	public static boolean setNodeAttributeValue(Node node, String attribute,
 			Object value) {
           final CyAttributes nodeAttrs = Cytoscape.getNodeAttributes();
           final String canonName = node.getIdentifier();
           if (value instanceof Boolean) {
             nodeAttrs.setAttribute(canonName, attribute, (Boolean) value);
             return true; }
           else if (value instanceof Integer) {
             nodeAttrs.setAttribute(canonName, attribute, (Integer) value);
             return true; }
           else if (value instanceof Double) {
             nodeAttrs.setAttribute(canonName, attribute, (Double) value);
             return true; }
           else if (value instanceof String) {
             nodeAttrs.setAttribute(canonName, attribute, (String) value);
             return true; }
           return false;
 	}
 
 	/**
 	 * Return the requested Attribute for the given Edge
      * @deprecated Use {@link CyAttributes} directly.  This method will
      * be removed in September, 2006.
 	 */
 	public static boolean setEdgeAttributeValue(Edge edge, String attribute,
 			Object value) {
           final CyAttributes edgeAttrs = Cytoscape.getEdgeAttributes();
           final String canonName = edge.getIdentifier();
           if (value instanceof Boolean) {
             edgeAttrs.setAttribute(canonName, attribute, (Boolean) value);
             return true; }
           else if (value instanceof Integer) {
             edgeAttrs.setAttribute(canonName, attribute, (Integer) value);
             return true; }
           else if (value instanceof Double) {
             edgeAttrs.setAttribute(canonName, attribute, (Double) value);
             return true; }
           else if (value instanceof String) {
             edgeAttrs.setAttribute(canonName, attribute, (String) value);
             return true; }
           return false;
 	}
 
 	/**
 	 * @deprecated argh!...
 	 */
 	private static String canonicalizeName(String name) {
 		String canonicalName = name;
 
 		// System.out.println( "Biodataserver is: "+bioDataServer+" species is:
 		// "+species );
 
 		if (bioDataServer != null) {
 			canonicalName = bioDataServer.getCanonicalName(species, name);
 			if (canonicalName == null) {
 				// System.out.println( "canonicalName was null for "+name );
 				canonicalName = name;
 			}
 			// System.out.println( name+" canonicalized to: "+canonicalName );
 		}
 		return canonicalName;
 	}
 
 	/**
 	 * @deprecated argh!...
 	 */
 	public static void setSpecies() {
 		species = CytoscapeInit.getDefaultSpeciesName();
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
 			defaultDesktop = new CytoscapeDesktop(CytoscapeInit.getViewType());
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
 					getRootGraph().removeNode(node);
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
 					getRootGraph().removeEdge(edge);
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
 
 	protected static void addNetwork(CyNetwork network) {
 		addNetwork(network, null, true);
 	}
 
 	protected static void addNetwork(CyNetwork network, String title,
 			boolean create_view) {
 		addNetwork(network, title, null, create_view);
 	}
 
 	protected static void addNetwork(CyNetwork network, String title) {
 		addNetwork(network, title, true);
 	}
 
 	protected static void addNetwork(CyNetwork network, String title,
 			CyNetwork parent) {
 		addNetwork(network, title, parent, true);
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
 		if (network.getNodeCount() < CytoscapeInit.getViewThreshold()
 				&& create_view) {
 			createNetworkView(network);
 		}
 
 		// createNetworkView( network );
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
 		addNetwork(network, title, false);
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
 		addNetwork(network, title);
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
 		addNetwork(network, title);
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
 		addNetwork(network, child_title, parent);
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
 		addNetwork(network, child_title, parent);
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
      * Gets Node Network Data:  GraphObjAttributes.
      * @return GraphObjAttributes Object.
 	 * @deprecated Use {@link Cytoscape#getNodeAttributes()} instead.  This
      * method will be removed in September, 2006.
 	 */
 	public static GraphObjAttributes getNodeNetworkData() {
 		return nodeData;
 	}
 
 	/**
      * Gets Edge Network Data:  GraphObjAttributes.
      * @return GraphObjAttributes Object.
      * @deprecated Use {@link Cytoscape#getEdgeAttributes()} instead.  This
      * method will be removed in September, 2006.
 	 */
 	public static GraphObjAttributes getEdgeNetworkData() {
 		return edgeData;
 	}
 
     /**
      * Gets Global Node Attributes.
      * @return CyAttributes Object.
      */
     public static CyAttributes getNodeAttributes() {
         return nodeAttributes;
     }
 
     /**
      * Gets Global Edge Attributes
      * @return CyAttributes Object.
      */
     public static CyAttributes getEdgeAttributes() {
         return edgeAttributes;
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
 
 			if (CytoscapeInit.suppressView()) {
 				JOptionPane.showMessageDialog(getDesktop(), errString, title,
 						JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 
 		}
 
 		if (copy_atts) {
 			expressionData.copyToAttribs(getNodeAttributes(), null);
 			firePropertyChange(ATTRIBUTES_CHANGED, null, null);
 		}
 
 		// Fire off an EXPRESSION_DATA_LOADED event.
 		Cytoscape.firePropertyChange(Cytoscape.EXPRESSION_DATA_LOADED, null,
 				expressionData);
 
 		if (CytoscapeInit.suppressView()) {
 			// display a description of the data in a dialog
 			String expDescript = expressionData.getDescription();
 			String title = "Load Expression Data";
 			JOptionPane.showMessageDialog(getDesktop(), expDescript, title,
 					JOptionPane.PLAIN_MESSAGE);
 		}
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
                     FileReader reader = new FileReader (nodeAttrLocations[i]);
                     CyAttributesReader.loadAttributes(nodeAttributes, reader);
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
                     FileReader reader = new FileReader (edgeAttrLocations[j]);
                     CyAttributesReader.loadAttributes(edgeAttributes, reader);
 				} catch (Exception e) {
 					throw new IllegalArgumentException(
 							"Failure loading edge attribute data: "
 									+ edgeAttrLocations[j]);
 				}
 			}
 		}
 
 		firePropertyChange(ATTRIBUTES_CHANGED, null, null);
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
 		final PhoebeNetworkView view = new PhoebeNetworkView(network, title);
 		view.setIdentifier(network.getIdentifier());
 		getNetworkViewMap().put(network.getIdentifier(), view);
 		view.setTitle(network.getTitle());
 
 		if (network.getClientData("GML") != null) {
 			((GraphReader) network.getClientData("GML")).layout(view);
 		}
 
 		firePropertyChange(
 				cytoscape.view.CytoscapeDesktop.NETWORK_VIEW_CREATED, null,
 				view);
 
 		// Instead of calling fitContent(), access PGraphView directly.
 		// This enables us to disable animation. Modified by Ethan Cerami.
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				view.getCanvas().getCamera().animateViewToCenterBounds(
 						view.getCanvas().getLayer().getFullBounds(), true, 0);
 				// if Squiggle function enabled, enable it on the view
 				if (squiggleEnabled) {
 					view.getSquiggleHandler().beginSquiggling();
 				}
 				// set the selection mode on the view
 				setSelectionMode(currentSelectionMode, view);
 			}
 		});
 		view.redrawGraph(false, false);
 		return view;
 	}
 
 	public static void firePropertyChange(String property_type,
 			Object old_value, Object new_value) {
 
 		PropertyChangeEvent e = new PropertyChangeEvent(pcsO, property_type,
 				old_value, new_value);
 		//System.out.println("Cytoscape FIRING : " + property_type);
 
 		getSwingPropertyChangeSupport().firePropertyChange(e);
 		getPropertyChangeSupport().firePropertyChange(e);
 	}
 
 	private static void setSquiggleState(boolean isEnabled) {
 
 		// enable Squiggle on all network views
 		PGraphView view;
 		String network_id;
 		Map networkViewMap = getNetworkViewMap();
 		for (Iterator iter = networkViewMap.keySet().iterator(); iter.hasNext();) {
 			network_id = (String) iter.next();
 			view = (PGraphView) networkViewMap.get(network_id);
 			if (isEnabled) {
 				view.getSquiggleHandler().beginSquiggling();
 			} else {
 				view.getSquiggleHandler().stopSquiggling();
 			}
 		}
 
 	}
 
 	/**
 	 * Utility method to enable Squiggle function.
 	 */
 	public static void enableSquiggle() {
 
 		// set the global flag to indicate that Squiggle is enabled
 		squiggleEnabled = true;
 		setSquiggleState(true);
 
 	}
 
 	/**
 	 * Utility method to disable Squiggle function.
 	 */
 	public static void disableSquiggle() {
 
 		// set the global flag to indicate that Squiggle is disabled
 		squiggleEnabled = false;
 		setSquiggleState(false);
 
 	}
 
 	/**
 	 * Returns the value of the global flag to indicate whether the Squiggle
 	 * function is enabled.
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
 
 }
