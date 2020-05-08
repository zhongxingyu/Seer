 // InteractionsReader:  from semi-structured text file, into an array of Interactions
 
 /** Copyright (c) 2002 Institute for Systems Biology and the Whitehead Institute
  **
  ** This library is free software; you can redistribute it and/or modify it
  ** under the terms of the GNU Lesser General Public License as published
  ** by the Free Software Foundation; either version 2.1 of the License, or
  ** any later version.
  **
  ** This library is distributed in the hope that it will be useful, but
  ** WITHOUT ANY WARRANTY, WITHOUT EVEN THE IMPLIED WARRANTY OF
  ** MERCHANTABILITY OR FITNESS FOR A PARTICULAR PURPOSE.  The software and
  ** documentation provided hereunder is on an "as is" basis, and the
  ** Institute for Systems Biology and the Whitehead Institute
  ** have no obligations to provide maintenance, support,
  ** updates, enhancements or modifications.  In no event shall the
  ** Institute for Systems Biology and the Whitehead Institute
  ** be liable to any party for direct, indirect, special,
  ** incidental or consequential damages, including lost profits, arising
  ** out of the use of this software and its documentation, even if the
  ** Institute for Systems Biology and the Whitehead Institute
  ** have been advised of the possibility of such damage.  See
  ** the GNU Lesser General Public License for more details.
  **
  ** You should have received a copy of the GNU Lesser General Public License
  ** along with this library; if not, write to the Free Software Foundation,
  ** Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
  **/
 
 //------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //------------------------------
 package cytoscape.data.readers;
 
 //------------------------------
 import giny.model.Edge;
 import giny.model.Node;
 import giny.model.RootGraph;
 import giny.view.GraphView;
 
 import java.io.IOException;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Vector;
 
 import cern.colt.list.IntArrayList;
 import cern.colt.map.OpenIntIntHashMap;
 import cytoscape.Cytoscape;
 import cytoscape.data.CyAttributes;
 import cytoscape.data.Interaction;
 import cytoscape.data.servers.BioDataServer;
 import cytoscape.task.TaskMonitor;
 import cytoscape.util.PercentUtil;
 
 /**
  * Reader for graphs in the interactions file format. Given the filename,
  * provides the graph and attributes objects constructed from the file.
  */
 public class InteractionsReader implements GraphReader {
 	private TaskMonitor taskMonitor;
 	private PercentUtil percentUtil;
 
 	/**
 	 * The File to be loaded
 	 */
 	protected String filename;
 
 	/**
 	 * A Vector that holds all of the Interactions
 	 */
 	protected Vector allInteractions = new Vector();
 	BioDataServer dataServer;
 	String species;
 	String zip_entry;
 	boolean is_zip = false;
 
 	IntArrayList node_indices;
 	OpenIntIntHashMap edges;
 
 	// ------------------------------
 	/**
 	 * Interactions Reader Constructor Creates a new Interactions Reader This
 	 * constructor assumes a Y-Files graph is wanted. If not then use the other
 	 * constructor to say so.
	 * 
 	 * @param dataServer
 	 *            a BioDataServer
 	 * @param species
 	 *            the species of the network being loaded
 	 * @param filename
 	 *            the file to load the network from
 	 */
 	public InteractionsReader(BioDataServer dataServer, String species,
 			String filename) {
 		this.filename = filename;
 		this.dataServer = dataServer;
 		this.species = species;
 	}
 
 	/**
 	 * Interactions Reader Constructor Creates a new Interactions Reader This
 	 * constructor assumes a Y-Files graph is wanted. If not then use the other
 	 * constructor to say so.
	 * 
 	 * @param dataServer
 	 *            a BioDataServer
 	 * @param species
 	 *            the species of the network being loaded
 	 * @param filename
 	 *            the file to load the network from
 	 */
 	public InteractionsReader(BioDataServer dataServer, String species,
 			String filename, TaskMonitor taskMonitor) {
 		this.filename = filename;
 		this.dataServer = dataServer;
 		this.species = species;
 		this.taskMonitor = taskMonitor;
 	}
 
 	public InteractionsReader(BioDataServer dataServer, String species,
 			String zip_entry, boolean is_zip) {
 		this.zip_entry = zip_entry;
 		this.dataServer = dataServer;
 		this.species = species;
 		this.is_zip = is_zip;
 	}
 
 	public void layout(GraphView view) {
 	}
 
 	// ----------------------------------------------------------------------------------------
 	public void read(boolean canonicalize) throws IOException {
 		String rawText;
 		if (!is_zip) {
 			if (filename.trim().startsWith("jar://")) {
 				TextJarReader reader = new TextJarReader(filename);
 				reader.read();
 				rawText = reader.getText();
 			} else {
 				TextFileReader reader = new TextFileReader(filename);
 				reader.read();
 				rawText = reader.getText();
 			}
 		} else {
 			rawText = zip_entry;
 		}
 
 		String delimiter = " ";
 		if (rawText.indexOf("\t") >= 0)
 			delimiter = "\t";
 
 		String[] lines = rawText.split("\n");
 
 		// There are a total of 6 steps to read in a complete SIF File
 		if (taskMonitor != null) {
 			percentUtil = new PercentUtil(6);
 		}
 
 		for (int i = 0; i < lines.length; i++) {
 
 			if (taskMonitor != null) {
 				taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(1,
 						i, lines.length));
 			}
 			String newLine = lines[i];
 			Interaction newInteraction = new Interaction(newLine, delimiter);
 			allInteractions.addElement(newInteraction);
 		}
 		createRootGraphFromInteractionData(canonicalize);
 
 	}
 
 	// -----------------------------------------------------------------------------------------
 	/**
	 * Calls read(true)
 	 */
 	public void read() throws IOException {
		read(true);
 	} // readFromFile
 
 	// -------------------------------------------------------------------------------------------
 	public int getCount() {
 		return allInteractions.size();
 	}
 
 	// -------------------------------------------------------------------------------------------
 	public Interaction[] getAllInteractions() {
 		Interaction[] result = new Interaction[allInteractions.size()];
 
 		for (int i = 0; i < allInteractions.size(); i++) {
 			Interaction inter = (Interaction) allInteractions.elementAt(i);
 			result[i] = inter;
 		}
 
 		return result;
 
 	}
 
 	// -------------------------------------------------------------------------------------------
 	protected String canonicalizeName(String name) {
 
 		String canonicalName = name;
 		if (dataServer != null) {
 			canonicalName = dataServer.getCanonicalName(species, name);
 			// added by iliana 11.14.2002
 			// for some strange reason the server returned a null canonical name
 			if (canonicalName == null) {
 				canonicalName = name;
 			}
 			// System.out.println (" -- canonicalizeName from server: " +
 			// canonicalName);
 		}
 		// System.out.println("the canonicalName for " + name + " is " +
 		// canonicalName);
 		// System.out.flush();
 		return canonicalName;
 
 	} // canonicalizeName
 
 	// -------------------------------------------------------------------------------------------
 	protected void createRootGraphFromInteractionData(boolean canonicalize) {
 		Interaction[] interactions = getAllInteractions();
 		// figure out how many nodes and edges we need before we create the
 		// graph;
 		// this improves performance for large graphs
 		Set nodeNameSet = new HashSet();
 		int edgeCount = 0;
 
 		for (int i = 0; i < interactions.length; i++) {
 			Interaction interaction = interactions[i];
 			String sourceName = interaction.getSource();
 			if (canonicalize)
 				sourceName = canonicalizeName(sourceName);
 			nodeNameSet.add(sourceName); // does nothing if already there
 
 			String[] targets = interaction.getTargets();
 			for (int t = 0; t < targets.length; t++) {
 				String targetNodeName = targets[t];
 				if (canonicalize)
 					targetNodeName = canonicalizeName(targetNodeName);
 				nodeNameSet.add(targetNodeName); // does nothing if already
 													// there
 				edgeCount++;
 			}
 			if (taskMonitor != null) {
 				taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(2,
 						i, interactions.length));
 			}
 		}
 
 		Cytoscape.ensureCapacity(nodeNameSet.size(), edgeCount);
 		node_indices = new IntArrayList(nodeNameSet.size());
 		edges = new OpenIntIntHashMap(edgeCount);
 
 		// now create all of the nodes, storing a hash from name to node
 		// Map nodes = new HashMap();
 		int counter = 0;
 		for (Iterator si = nodeNameSet.iterator(); si.hasNext();) {
 			String nodeName = (String) si.next();
 
 			if (taskMonitor != null) {
 				taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(3,
 						counter, nodeNameSet.size()));
 				counter++;
 			}
 
 			// use the static method
 			Node node = (Node) Cytoscape.getCyNode(nodeName, true);
 			node_indices.add(node.getRootGraphIndex());
 			// nodes.put(nodeName, node);
 
 			// int node_i = Cytoscape.getRootGraph().createNode();
 			// Node node = Cytoscape.getRootGraph().getNode( node_i);
 			// node.setIdentifier(nodeName);
 			// Cytoscape.getNodeNetworkData().addNameMapping(nodeName, node);
 
 		}
 
 		// ---------------------------------------------------------------------------
 		// now loop over the interactions again, this time creating edges
 		// between
 		// all sources and each of their respective targets.
 		// for each edge, save the source-target pair, and their interaction
 		// type,
 		// in Cytoscape.getEdgeNetworkData() -- a hash of a hash of name-value
 		// pairs, like this:
 		// Cytoscape.getEdgeNetworkData() ["interaction"] = interactionHash
 		// interactionHash [sourceNode::targetNode] = "pd"
 		// ---------------------------------------------------------------------------
 		String targetNodeName;
 		for (int i = 0; i < interactions.length; i++) {
 
 			if (taskMonitor != null) {
 				taskMonitor.setPercentCompleted(percentUtil.getGlobalPercent(4,
 						i, interactions.length));
 			}
 
 			Interaction interaction = interactions[i];
 			String nodeName = interaction.getSource();
 			if (canonicalize)
 				nodeName = canonicalizeName(interaction.getSource());
 
 			String interactionType = interaction.getType();
 
 			// giny.model.Node sourceNode = (giny.model.Node) nodes.get
 			// (nodeName);
 
 			String[] targets = interaction.getTargets();
 			for (int t = 0; t < targets.length; t++) {
 
 				if (canonicalize)
 					targetNodeName = canonicalizeName(targets[t]);
 				else
 					targetNodeName = targets[t];
 
 				// Node targetNode = (Node) nodes.get (targetNodeName);
 				String edgeName = nodeName + " (" + interactionType + ") "
 						+ targetNodeName;
 				Edge edge = (Edge) Cytoscape.getCyEdge(nodeName, edgeName,
 						targetNodeName, interactionType);
 				edges.put(edge.getRootGraphIndex(), 0);
 
 				// System.out.println( "edge: "+edge.getRootGraphIndex() );
 
 				// int previousMatchingEntries =
 				// Cytoscape.getEdgeNetworkData().countIdentical(edgeName);
 				// Edge edge =
 				// Cytoscape.getRootGraph().getEdge(Cytoscape.getRootGraph().createEdge
 				// (sourceNode, targetNode));
 				// int previousMatchingEntries =
 				// Cytoscape.getEdgeNetworkData().countIdentical(edgeName);
 				// if ( previousMatchingEntries > 0 ) {
 				// edgeName = edgeName + "_" + previousMatchingEntries;
 				// } else {
 				// Cytoscape.getEdgeNetworkData().add ("interaction", edgeName,
 				// interactionType);
 				// Cytoscape.getEdgeNetworkData().addNameMapping (edgeName,
 				// edge);
 				// }
 			} // for t
 		} // for i
 
 	} // createRootGraphFromInteractionData
 
 	public RootGraph getRootGraph() {
 		return Cytoscape.getRootGraph();
 
 	} // createGraph
 
 	public CyAttributes getNodeAttributes() {
 		return Cytoscape.getNodeAttributes();
 
 	} // getNodeAttributes
 
 	public CyAttributes getEdgeAttributes() {
 		return Cytoscape.getEdgeAttributes();
 
 	} // getEdgeAttributes
 
 	public int[] getNodeIndicesArray() {
 		node_indices.trimToSize();
 		return node_indices.elements();
 	}
 
 	public int[] getEdgeIndicesArray() {
 		// edges.trimToSize();
 		// return edges.elements();
 		IntArrayList edge_indices = new IntArrayList(edges.size());
 		edges.keys(edge_indices);
 		edge_indices.trimToSize();
 		return edge_indices.elements();
 
 	}
 
 } // InteractionsReader
 
