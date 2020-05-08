 /*
  File: CytoscapeSessionReader.java 
  
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
 
 package cytoscape.data.readers;
 
 import java.awt.geom.Point2D;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.JarURLConnection;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 import java.util.zip.ZipInputStream;
 
 import javax.swing.SwingUtilities;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.data.Semantics;
 import cytoscape.ding.CyGraphLOD;
 import cytoscape.ding.DingNetworkView;
 import cytoscape.generated.Child;
 import cytoscape.generated.Cysession;
 import cytoscape.generated.HiddenNodes;
 import cytoscape.generated.Network;
 import cytoscape.generated.NetworkTree;
 import cytoscape.generated.Node;
 import cytoscape.generated.SelectedEdges;
 import cytoscape.generated.SelectedNodes;
 import cytoscape.view.CyNetworkView;
 import ding.view.DGraphView;
 
 /**
  * Cytoscape Session Reader. This class expands cys file and read all files in
  * the archive.
  * 
  * @author kono
  * 
  */
 public class CytoscapeSessionReader {
 
 	private final String PACKAGE_NAME = "cytoscape.generated";
 	
 	public static final String CYSESSION = "cysession.xml";
 	
 	private static final String XML_EXT = ".xml";
 	private static final String XGMML_EXT = ".xgmml";
 	
 	private final String FS = System.getProperty("file.separator");
 
 	private String fileName;
 	private Object sourceObject;
 
 	private HashMap networkURLs = null;
 
 	private URL cysessionFileURL = null;
 	private URL vizmapFileURL = null;
 	private URL cytoscapePropsURL = null;
 	
 	HashMap netMap;
 	String sessionID;
 
 	Cysession session;
 
 	List networkList;
 
 	// Stores networkName as the key and value is visualStyleName associated
 	// with it.
 	HashMap vsMap;
 	HashMap vsMapByName;
 
 	/**
 	 * Constructor for local files.
 	 * 
 	 * @param filename
 	 *            Name of .cys file.
 	 * @throws JAXBException
 	 * @throws Exception
 	 * 
 	 * This constructor is for loading local session file.
 	 */
 	public CytoscapeSessionReader(String filename) {
 		this.fileName = filename;
 		this.sourceObject = fileName;
 		this.networkList = new ArrayList();
 
 		vsMap = new HashMap();
 		vsMapByName = new HashMap();
 	}
 
 	/**
 	 * Constructor for remote file (specified by an URL)
 	 * 
 	 * @param sourceName
 	 * @throws IOException
 	 * 
 	 *  This is for remote session file (URL).
 	 */
 	public CytoscapeSessionReader(URL sourceName) throws IOException {
 		this.sourceObject = sourceName;
 		this.networkList = new ArrayList();
 
 		vsMap = new HashMap();
 		vsMapByName = new HashMap();
 		
 		extractEntry((URL) sourceObject);
 	}
 
 	/**
 	 * Extract Zip entries in the remote file
 	 *  
 	 * @param sourceName
 	 * @throws IOException
 	 */
 	private void extractEntry(URL sourceName) throws IOException {
 		ZipInputStream zis = null;
 		zis = new ZipInputStream(sourceName.openStream());
 		networkURLs = new HashMap();
 
 		// Extract list of entries
 		ZipEntry zen = null;
 		String entryName = null;
 		while ((zen = zis.getNextEntry()) != null) {
 
 			entryName = zen.getName();
 			if (entryName.endsWith(XML_EXT)) {
 				cysessionFileURL = new URL("jar:" + sourceName.toString()
 						+ "!/" + entryName);
 			} else if (entryName.endsWith("vizmap.props")) {
 				vizmapFileURL = new URL("jar:" + sourceName.toString() + "!/"
 						+ entryName);
 			} else if (entryName.endsWith("cytoscape.props")) {
 				cytoscapePropsURL = new URL("jar:" + sourceName.toString()
 						+ "!/" + entryName);
 			} else if (entryName.endsWith(XGMML_EXT)) {
 				URL networkURL = new URL("jar:" + sourceName.toString() + "!/"
 						+ entryName);
 				networkURLs.put(entryName, networkURL);
 			}
 		}
 		zis.close();
 	}
 
 	/**
 	 * Read a session file.
 	 * 
 	 * @throws IOException
 	 * @throws JAXBException
 	 */
 	public void read() throws IOException, JAXBException {
 		
 
 		if (sourceObject.getClass() == String.class) {
 			unzipSessionFromFile();
 		} else if (sourceObject.getClass() == URL.class) {
 			unzipSessionFromURL();
 		}
 
 		// Send message with list of loaded networks.
 		Cytoscape.firePropertyChange(Cytoscape.SESSION_LOADED, null,
 				networkList);
 		// Send signal to others
 		Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 		Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null, null);
 	}
 
 	/**
 	 * Decompress session file
 	 * 
 	 * @throws IOException
 	 * @throws JAXBException
 	 */
 	private boolean unzipSessionFromURL() throws IOException, JAXBException {
 
 		// restore vizmap.props
 		Cytoscape.firePropertyChange(Cytoscape.VIZMAP_RESTORED, null,
 				vizmapFileURL);
 
 		// restore cytoscape properties
 		CytoscapeInit.getProperties().load(cytoscapePropsURL.openStream());
 
 		loadCySession(cysessionFileURL);
 
 		return true;
 	}
 
 	/**
 	 * Decompress session file
 	 * 
 	 * @throws IOException
 	 * @throws JAXBException
 	 */
 	private boolean unzipSessionFromFile() throws IOException, JAXBException {
 
 		// Create zipfile object to be extracted
 		ZipFile cysFile = new ZipFile(fileName);
 
 		String cysessionFileName = null;
 
 		for (Enumeration e = cysFile.entries(); e.hasMoreElements();) {
 			ZipEntry zent = (ZipEntry) e.nextElement();
 			String curEntry = zent.toString();
 			if (curEntry.endsWith("xml")) {
 				cysessionFileName = curEntry;
 
 			} else if (curEntry.endsWith("vizmap.props")) {
 				// We should restore VIZMAP first since it will be used by
 				// Graph Layout.
 				Cytoscape.firePropertyChange(Cytoscape.VIZMAP_RESTORED, null,
 						fileName);
 			} else if (curEntry.endsWith("cytoscape.props")) {
 				CytoscapeInit.getProperties()
 						.load(cysFile.getInputStream(zent));
 			}
 		}
 
 		cysFile.close();
 		loadCySession(cysessionFileName);
 
 		return true;
 	}
 
 	private boolean loadCySession(Object cysessionSource) throws JAXBException,
 			IOException {
 
 		InputStream is = null;
 		ZipFile sessionFile = null;
 
 		// Prepare input stream based on the
 		if (cysessionSource.getClass() == URL.class) {
 			JarURLConnection jarConnection = null;
 			jarConnection = (JarURLConnection) ((URL) cysessionSource)
 					.openConnection();
 			is = (InputStream) jarConnection.getContent();
 
 		} else if (cysessionSource.getClass() == String.class) {
 			sessionFile = new ZipFile(fileName);
 			ZipEntry csxml = sessionFile.getEntry((String) cysessionSource);
 			is = sessionFile.getInputStream(csxml);
 		}
 
 		JAXBContext jc = JAXBContext.newInstance(PACKAGE_NAME, this.getClass().getClassLoader());
 		Unmarshaller u = jc.createUnmarshaller();
 
 		session = (Cysession) u.unmarshal(is);
 		if (is != null) {
 			is.close();
 		}
 
 		// Session ID is the name of folder which contains everything
 		// for this session.
 		sessionID = session.getId();
 
 		// Extract tree structure
 		NetworkTree networks = (NetworkTree) session.getNetworkTree();
 
 		List netList = networks.getNetwork();
 		// Convert it to map
 		Iterator it = netList.iterator();
 		netMap = new HashMap();
 		while (it.hasNext()) {
 			Network curNet = (Network) it.next();
 			netMap.put(curNet.getId(), curNet);
 
 		}
 
 		// Extract root network
 		// ("root network" is a network which belongs directly to
 		// the Network Root.)
 
 		Network root = (Network) netMap.get("Network Root");
 		List rootTrees = root.getChild();
 		Iterator rootIt = rootTrees.iterator();
 
 		while (rootIt.hasNext()) {
 			Child curNet = (Child) rootIt.next();
 			String curNetName = curNet.getId();
 
 			Network targetRoot = (Network) netMap.get(curNetName);
 			vsMapByName.put(targetRoot.getId(), targetRoot.getVisualStyle());
 
 			// Create a root network here.
 			//String targetNetwork = sessionID + FS + targetRoot.getFilename();
 			String targetNetwork = sessionID + "/" + targetRoot.getFilename();
 
 			InputStream networkStream = null;
 
 			if (cysessionSource.getClass() == URL.class) {
 				// Problem: targetNetwork improperly uses the file separator
 				// FS for URLs and one Windows machine FS is '\'
 				// Fix: for URLs, replace '\' with '/'
 				targetNetwork = targetNetwork.replace('\\', '/');
 				URL targetNetworkURL = (URL) networkURLs.get(targetNetwork);
 				JarURLConnection jarConnection = (JarURLConnection) (targetNetworkURL)
 						.openConnection();
 				networkStream = (InputStream) jarConnection.getContent();
 			} else if (cysessionSource.getClass() == String.class) {
 				networkStream = sessionFile.getInputStream(sessionFile
 						.getEntry(targetNetwork));
 			}
 
 			CyNetwork rootNetwork = this.createNetwork(null, targetNetwork,
 					networkStream, Cytoscape.FILE_XGMML, CytoscapeInit
 							.getProperties().getProperty("defaultSpeciesName"),
 					targetRoot.isViewAvailable());
 
 			if(networkStream != null) {
 				networkStream.close();
 			}
 			
 
 			String vsName = targetRoot.getVisualStyle();
 			if (vsName == null) {
 				vsName = "default";
 			}
 			vsMap.put(rootNetwork.getIdentifier(), vsName);
 
 			networkList.add(rootNetwork.getIdentifier());
 
 			// Set selected nodes & edges
 			SelectedNodes sNodes = (SelectedNodes) targetRoot
 					.getSelectedNodes();
 
 			if (sNodes != null) {
 				setSelectedNodes(rootNetwork, sNodes);
 			}
 			
 			HiddenNodes hNodes = (HiddenNodes) targetRoot.getHiddenNodes();
 			CyNetworkView curNetView = Cytoscape.getNetworkView(rootNetwork.getIdentifier());
			if( curNetView != Cytoscape.getNullNetworkView() && hNodes != null) {
 				setHiddenNodes(curNetView, hNodes);
 			}
 			
 			SelectedEdges sEdges = (SelectedEdges) targetRoot
 					.getSelectedEdges();
 			if (sEdges != null) {
 				setSelectedEdges(rootNetwork, sEdges);
 			}
 			if (cysessionSource.getClass() == URL.class) {
 				walkTree(targetRoot, rootNetwork, cysessionSource);
 			} else if (cysessionSource.getClass() == String.class) {
 				walkTree(targetRoot, rootNetwork, sessionFile);
 			}
 		}
 
 		if (sessionFile != null) {
 			sessionFile.close();
 		}
 
 		// Set VS for each network view
 		Set networkIDSet = vsMap.keySet();
 		String currentNetworkID = null;
 		Iterator it2 = networkIDSet.iterator();
 		while (it2.hasNext()) {
 			currentNetworkID = (String) it2.next();
 			CyNetworkView targetView = Cytoscape
 					.getNetworkView(currentNetworkID);
 			if (targetView != null) {
 				targetView.setVisualStyle((String) vsMap.get(currentNetworkID));
 				targetView.applyVizmapper(targetView.getVisualStyle());
 			}
 		}
 
 		// Traverse network tree
 
 		return false;
 	}
 
 	private void setSelectedNodes(CyNetwork network, SelectedNodes selected) {
 
 		List selectedNodeList = new ArrayList();
 
 		Iterator it = selected.getNode().iterator();
 		while (it.hasNext()) {
 			Node selectedNode = (Node) it.next();
 			String nodeID = selectedNode.getId();
 			selectedNodeList.add(Cytoscape.getCyNode(nodeID, false));
 		}
 		network.setSelectedNodeState(selectedNodeList, true);
 	}
 	
 	private void setHiddenNodes(final CyNetworkView view, final HiddenNodes hidden) {
 		final Iterator it = hidden.getNode().iterator();
 		while (it.hasNext()) {
 			final Node hiddenNodeObject = (Node) it.next();
 			final CyNode hiddenNode = Cytoscape.getCyNode(hiddenNodeObject.getId(), false);
 			view.hideGraphObject( view.getNodeView(hiddenNode) );
 		}
 	}
 
 	private void setSelectedEdges(CyNetwork network, SelectedEdges selected) {
 
 		String edgeID = null;
 		String[] parts = null;
 		CyEdge targetEdge = null;
 		List selectedEdgeList = new ArrayList();
 		
 		Iterator it = selected.getEdge().iterator();
 		while (it.hasNext()) {
 			
 			cytoscape.generated.Edge selectedEdge = (cytoscape.generated.Edge) it
 					.next();
 			edgeID = selectedEdge.getId();
 			parts = edgeID.split(" ");
 			if(parts.length == 3) {
 				CyNode source = Cytoscape.getCyNode(parts[0], false);
 				CyNode target = Cytoscape.getCyNode(parts[2], false);
 				String interaction = parts[1].substring(1, parts[1].length()-1);
 				targetEdge = Cytoscape.getCyEdge(source, target, Semantics.INTERACTION, interaction, false);
 				if(targetEdge != null) {
 					selectedEdgeList.add(targetEdge);
 				}
 			}
 		}
 		network.setSelectedEdgeState(selectedEdgeList, true);
 	}
 
 	// Load the root network and then create its children.
 	private void walkTree(Network currentNetwork, CyNetwork parent,
 			Object sessionSource) throws JAXBException, IOException {
 		
 		// Get the list of children under this root
 		List children = currentNetwork.getChild();
 
 		// Traverse using recursive call
 		for (int i = 0; i < children.size(); i++) {
 
 			Child child = (Child) children.get(i);
 			Network childNet = (Network) netMap.get(child.getId());
 			String vsName = childNet.getVisualStyle();
 			if (vsName == null) {
 				vsName = "default";
 			}
 
 			vsMapByName.put(child.getId(), vsName);
 
 			String childFile = sessionID + FS + childNet.getFilename();
 
 			InputStream networkStream = null;
 			if (sessionSource.getClass() == ZipFile.class) {
 				ZipFile zipSourceFile = (ZipFile) sessionSource;
 				networkStream = zipSourceFile.getInputStream(zipSourceFile
 						.getEntry(childFile));
 			} else if (sessionSource.getClass() == URL.class) {
 				URL targetNetworkURL = (URL) networkURLs.get(childFile);
 				JarURLConnection jarConnection = (JarURLConnection) (targetNetworkURL)
 						.openConnection();
 				networkStream = (InputStream) jarConnection.getContent();
 			}
 
 			CyNetwork new_network = this.createNetwork(parent, childFile,
 					networkStream, Cytoscape.FILE_XGMML, CytoscapeInit
 							.getProperties().getProperty("defaultSpeciesName"),
 					childNet.isViewAvailable());
 
 			if(networkStream != null) {
 				networkStream.close();
 			}
 			vsMap.put(new_network.getIdentifier(), vsName);
 			networkList.add(new_network.getIdentifier());
 			//
 			// Set selected/hidden nodes & edges
 			//
 			SelectedNodes sNodes = (SelectedNodes) childNet.getSelectedNodes();
 			if (sNodes != null) {
 				setSelectedNodes(new_network, sNodes);
 			}
 
 			SelectedEdges sEdges = (SelectedEdges) childNet.getSelectedEdges();
 			if (sEdges != null) {
 				setSelectedEdges(new_network, sEdges);
 			}
 			// Re-create
 
 			// network = createChildNetwork(childNet, network);
 			if (childNet.getChild().size() == 0) {
 			} else {
 				walkTree(childNet, new_network, sessionSource);
 			}
 
 		}
 	}
 
 	private CyNetwork createNetwork(CyNetwork parent, String location,
 			InputStream is, int file_type, String species, boolean viewAvailable)
 			throws IOException, JAXBException {
 
 		// Create reader and read an XGMML file
 		XGMMLReader reader;
 		reader = new XGMMLReader(is);
 		reader.read();
 
 		// Get the RootGraph indices of the nodes and
 		// Edges that were just created
 		final int[] nodes = reader.getNodeIndicesArray();
 		final int[] edges = reader.getEdgeIndicesArray();
 
 		// Create the CyNetwork
 		// First, set the view threshold to 0. By doing so, we can disable
 		// the auto-creating of the CyNetworkView.
 
 		int realThreshold = Integer.valueOf(
 				CytoscapeInit.getProperties().getProperty("viewThreshold",
 						"5000")).intValue();
 
 		CytoscapeInit.setViewThreshold(0);
 
 		CyNetwork network = null;
 		if (parent == null) {
 			network = Cytoscape.createNetwork(nodes, edges, reader
 					.getNetworkID());
 
 		} else {
 			network = Cytoscape.createNetwork(nodes, edges, reader
 					.getNetworkID(), parent);
 		}
 		
 		// Set network Attributes here, not in the read() method in XGMMLReader!
 		// Otherwise, ID mismatch may happen.
 		reader.setNetworkAttributes(network);
 
 		// network.putClientData(
 		// "metaNodeViewer.model.GPMetaNodeFactory.metaNodeRindices",
 		// reader.getMetanodeList());
 
 		// Reset back to the real View Threshold
 		CytoscapeInit.setViewThreshold(realThreshold);
 
 		// Conditionally, Create the CyNetworkView
 
 		if (viewAvailable == true) {
 			createCyNetworkView(network);
 
 			if (Cytoscape.getNetworkView(network.getIdentifier()) != Cytoscape
 					.getNullNetworkView()) {
 				reader
 						.layout(Cytoscape.getNetworkView(network
 								.getIdentifier()));
 			}
 
 			// Lastly, make the GraphView Canvas Visible.
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					DingNetworkView view = (DingNetworkView) Cytoscape
 							.getCurrentNetworkView();
 					view.setGraphLOD(new CyGraphLOD());
 				}
 			});
 
 			String curVS = (String) vsMapByName.get(network.getTitle());
 			CyNetworkView curView = Cytoscape.getNetworkView(network
 					.getIdentifier());
 			if (curVS != null) {
 				curView.setVisualStyle(curVS);
 				Cytoscape.getDesktop().getVizMapUI().getStyleSelector()
 						.resetStyles(curVS);
 				Cytoscape.getDesktop().getVizMapUI().visualStyleChanged();
 				Cytoscape.getVisualMappingManager().setVisualStyle(curVS);
 
 			} else
 				curView.setVisualStyle(Cytoscape.getVisualMappingManager()
 						.getVisualStyle().getName());
 
 			curView.fitContent();
 
 			// set view zoom
 			Double zoomLevel = reader.getGraphViewZoomLevel();
 			if (zoomLevel != null)
 				curView.setZoom(zoomLevel.doubleValue());
 
 			// set view center
 			Point2D center = reader.getGraphViewCenter();
 			if (center != null)
 				((DGraphView) curView).setCenter(center.getX(), center.getY());
 		}
 
 		return network;
 	}
 
 	// same as other loaders
 	//
 	private void createCyNetworkView(CyNetwork cyNetwork) {
 		final DingNetworkView view = new DingNetworkView(cyNetwork, cyNetwork
 				.getTitle());
 
 		view.setIdentifier(cyNetwork.getIdentifier());
 		Cytoscape.getNetworkViewMap().put(cyNetwork.getIdentifier(), view);
 		view.setTitle(cyNetwork.getTitle());
 
 		// set the selection mode on the view
 		Cytoscape.setSelectionMode(Cytoscape.getSelectionMode(), view);
 
 		Cytoscape.firePropertyChange(
 				cytoscape.view.CytoscapeDesktop.NETWORK_VIEW_CREATED, null,
 				view);
 	}
 
 }
