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
import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.zip.ZipEntry;
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
 import cytoscape.generated.Edge;
 import cytoscape.generated.HiddenEdges;
 import cytoscape.generated.HiddenNodes;
 import cytoscape.generated.Network;
 import cytoscape.generated.NetworkTree;
 import cytoscape.generated.Node;
 import cytoscape.generated.SelectedEdges;
 import cytoscape.generated.SelectedNodes;
 import cytoscape.view.CyNetworkView;
 import ding.view.DGraphView;
 
 /**
  * Reaser to load CYtoscape Session file (.cys).<br>
  * This class unzip cys file and read all files in the archive.
  * <p>
  * This class accept input as URL only!
  * If it is a file, use File.toURL() to get platform dependent file URL.
  * </p>
  * 
  * @version 1.0
  * @since Cytoscape 2.3
  * @see cytoscape.data.readers.XGMMLReader
  * @author kono
  * 
  */
 public class CytoscapeSessionReader {
 
 	public static final String PACKAGE_NAME = "cytoscape.generated";
 	public static final String CYSESSION = "cysession.xml";
 	public static final String VIZMAP_PROPS = "vizmap.props";
 	public static final String CY_PROPS = "cytoscape.props";
 	public static final String XGMML_EXT = ".xgmml";
 
 	private static final String NETWORK_ROOT = "Network Root";
 
 	private URL sourceURL;
 
 	private HashMap networkURLs = null;
 
 	private URL cysessionFileURL = null;
 	private URL vizmapFileURL = null;
 	private URL cytoscapePropsURL = null;
 
 	private HashMap netMap;
 	private String sessionID;
 
 	private Cysession session;
 
 	private List networkList;
 
 	/*
 	 * Stores networkName as the key and value is visualStyleName associated
 	 * with it.
 	 */
 	HashMap vsMap;
 	HashMap vsMapByName;
 
 	/**
 	 * Constructor for remote file (specified by an URL)<br>
 	 * 
 	 * @param sourceName
 	 * @throws IOException
 	 * 
 	 * This is for remote session file (URL).
 	 */
 	public CytoscapeSessionReader(final URL sourceName) throws IOException {
 		this.sourceURL = sourceName;
 		
 		networkList = new ArrayList();
 		vsMap = new HashMap();
 		vsMapByName = new HashMap();
 	}
 
 	/**
 	 * Extract Zip entries in the remote file
 	 * 
 	 * @param sourceName
 	 * @throws IOException
 	 */
 	private void extractEntry() throws IOException {
		/*
		 * This is an inportant part!
		 * 
		 * We can create InputStream directly from URL, but it does
		 * not work always due to the cashing mechanism in URLConnection.
		 * 
		 * By default, URLConnection creates a cash for session file name.
		 * This will be used even after we saved the session.  Due to the 
		 * conflict between cashed name and new saved session name (generated
		 * from system time), session reader cannot find the entry in the zip
		 * file.  To avoid this problem, we shoud turn off the cashing mechanism
		 * by using:
		 * 
		 * URLConnection.setDefaultUseCaches(false)
		 * 
		 * This is a "sticky" parameter for all URLConnections and we have to 
		 * set this only once.
		 * 
		 */
		URLConnection juc = sourceURL.openConnection();
		juc.setDefaultUseCaches(false);
		ZipInputStream zis = new ZipInputStream(juc.getInputStream());
 
 		networkURLs = new HashMap();
 
 		// Extract list of entries
 		ZipEntry zen = null;
 		String entryName = null;
 		while ((zen = zis.getNextEntry()) != null) {
 			entryName = zen.getName();
 			if (entryName.endsWith(CYSESSION)) {
 				cysessionFileURL = new URL("jar:" + sourceURL.toString() + "!/"
 						+ entryName);
 			} else if (entryName.endsWith(VIZMAP_PROPS)) {
 				vizmapFileURL = new URL("jar:" + sourceURL.toString() + "!/"
 						+ entryName);
 			} else if (entryName.endsWith(CY_PROPS)) {
 				cytoscapePropsURL = new URL("jar:" + sourceURL.toString()
 						+ "!/" + entryName);
 			} else if (entryName.endsWith(XGMML_EXT)) {
 				URL networkURL = new URL("jar:" + sourceURL.toString() + "!/"
 						+ entryName);
 				networkURLs.put(entryName, networkURL);
 			}
 		}
 		if (zis != null) {
 			try {
 				zis.close();
 			} finally {
 				zis = null;
 			}
 		}
 	}
 
 	/**
 	 * Read a session file.
 	 * 
 	 * @throws IOException
 	 * @throws JAXBException
 	 */
 	public void read() throws IOException, JAXBException {
 
 		unzipSessionFromURL();
 
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
 	private void unzipSessionFromURL() throws IOException, JAXBException {
 
 		extractEntry();
 
 		// restore vizmap.props
 		Cytoscape.firePropertyChange(Cytoscape.VIZMAP_RESTORED, null,
 				vizmapFileURL);
 
 		// restore cytoscape properties
 		CytoscapeInit.getProperties().load(cytoscapePropsURL.openStream());
 
 		loadCySession(cysessionFileURL);
 	}
 
 	private void loadCySession(final URL cysessionSource)
 			throws JAXBException, IOException {
 
 		InputStream is = cysessionSource.openStream();
 		final JAXBContext jaxbContext = JAXBContext.newInstance(PACKAGE_NAME,
 				this.getClass().getClassLoader());
 		final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
 
 		session = (Cysession) unmarshaller.unmarshal(is);
 
 		if (is != null) {
 			try {
 				is.close();
 			} finally {
 				is = null;
 			}
 		}
 
 		/*
 		 * Session ID is the name of folder which contains everything for this
 		 * session.
 		 */
 		sessionID = session.getId();
 
 		// Convert it to map
 		final Iterator it = ((NetworkTree) session.getNetworkTree())
 				.getNetwork().iterator();
 		netMap = new HashMap();
 		while (it.hasNext()) {
 			final Network curNet = (Network) it.next();
 			netMap.put(curNet.getId(), curNet);
 		}
 
 		walkTree((Network) netMap.get(NETWORK_ROOT), null, cysessionSource);
 
 		/*
 		 * Set VS for each network view
 		 */
 		String currentNetworkID = null;
 		final Iterator viewIt = vsMap.keySet().iterator();
 		while (viewIt.hasNext()) {
 			currentNetworkID = (String) viewIt.next();
 			final CyNetworkView targetView = Cytoscape
 					.getNetworkView(currentNetworkID);
 			if (targetView != Cytoscape.getNullNetworkView()) {
 				targetView.setVisualStyle((String) vsMap.get(currentNetworkID));
 				targetView.applyVizmapper(targetView.getVisualStyle());
 				((DingNetworkView)targetView).getCanvas().setVisible(true);
 			}
 		}
 	}
 
 	/**
 	 * Load the root network and then create its children.<br>
 	 * 
 	 * @param currentNetwork
 	 * @param parent
 	 * @param sessionSource
 	 * @throws JAXBException
 	 * @throws IOException
 	 */
 	private void walkTree(final Network currentNetwork, final CyNetwork parent,
 			final Object sessionSource) throws JAXBException, IOException {
 
 		// Get the list of children under this root
 		final List children = currentNetwork.getChild();
 
 		// Traverse using recursive call
 		for (int i = 0; i < children.size(); i++) {
 
 			final Child child = (Child) children.get(i);
 			final Network childNet = (Network) netMap.get(child.getId());
 			String vsName = childNet.getVisualStyle();
 			if (vsName == null) {
 				vsName = "default";
 			}
 
 			vsMapByName.put(child.getId(), vsName);
 
 			final URL targetNetworkURL = (URL) networkURLs.get(sessionID + "/"
 					+ childNet.getFilename());
 			final JarURLConnection jarConnection = (JarURLConnection) (targetNetworkURL)
 					.openConnection();
 			InputStream networkStream = (InputStream) jarConnection
 					.getContent();
 
 			final CyNetwork new_network = this.createNetwork(parent,
 					networkStream, childNet.isViewAvailable());
 
 			/*
 			 * Extract network view
 			 */
 			final CyNetworkView curNetView = Cytoscape
 					.getNetworkView(new_network.getIdentifier());
 
 			if (networkStream != null) {
 				try {
 					networkStream.close();
 				} finally {
 					networkStream = null;
 				}
 			}
 			vsMap.put(new_network.getIdentifier(), vsName);
 			networkList.add(new_network.getIdentifier());
 
 			/*
 			 * Set selected & hidden nodes/edges
 			 */
 			if (curNetView != Cytoscape.getNullNetworkView()) {
 				setHiddenNodes(curNetView, (HiddenNodes) childNet
 						.getHiddenNodes());
 				setHiddenEdges(curNetView, (HiddenEdges) childNet
 						.getHiddenEdges());
 			}
 			setSelectedNodes(new_network, (SelectedNodes) childNet
 					.getSelectedNodes());
 			setSelectedEdges(new_network, (SelectedEdges) childNet
 					.getSelectedEdges());
 
 			/*
 			 * Load child networks
 			 */
 			if (childNet.getChild().size() != 0) {
 				walkTree(childNet, new_network, sessionSource);
 			}
 		}
 	}
 
 	private void setSelectedNodes(final CyNetwork network,
 			final SelectedNodes selected) {
 		if (selected == null) {
 			return;
 		}
 
 		final List selectedNodeList = new ArrayList();
 		final Iterator it = selected.getNode().iterator();
 
 		while (it.hasNext()) {
 			final Node selectedNode = (Node) it.next();
 			selectedNodeList.add(Cytoscape.getCyNode(selectedNode.getId(),
 					false));
 		}
 		network.setSelectedNodeState(selectedNodeList, true);
 	}
 
 	private void setHiddenNodes(final CyNetworkView view,
 			final HiddenNodes hidden) {
 		if (hidden == null) {
 			return;
 		}
 		final Iterator it = hidden.getNode().iterator();
 		while (it.hasNext()) {
 			final Node hiddenNodeObject = (Node) it.next();
 			final CyNode hiddenNode = Cytoscape.getCyNode(hiddenNodeObject
 					.getId(), false);
 			view.hideGraphObject(view.getNodeView(hiddenNode));
 		}
 	}
 
 	private void setHiddenEdges(final CyNetworkView view,
 			final HiddenEdges hidden) {
 		if (hidden == null) {
 			return;
 		}
 		final Iterator it = hidden.getEdge().iterator();
 		while (it.hasNext()) {
 			final Edge hiddenEdgeObject = (Edge) it.next();
 			final CyEdge hiddenEdge = getEdgeFromID(hiddenEdgeObject.getId());
 			if (hiddenEdge != null) {
 				view.hideGraphObject(view.getEdgeView(hiddenEdge));
 			}
 		}
 	}
 
 	private void setSelectedEdges(final CyNetwork network,
 			final SelectedEdges selected) {
 		if (selected == null) {
 			return;
 		}
 		CyEdge targetEdge = null;
 		final List selectedEdgeList = new ArrayList();
 		final Iterator it = selected.getEdge().iterator();
 		while (it.hasNext()) {
 
 			final cytoscape.generated.Edge selectedEdge = (cytoscape.generated.Edge) it
 					.next();
 			targetEdge = getEdgeFromID(selectedEdge.getId());
 			if (targetEdge != null) {
 				selectedEdgeList.add(targetEdge);
 			}
 		}
 		network.setSelectedEdgeState(selectedEdgeList, true);
 	}
 
 	private CyEdge getEdgeFromID(final String edgeID) {
 		CyEdge targetEdge = null;
 		final String[] parts = edgeID.split(" ");
 		if (parts.length == 3) {
 			final CyNode source = Cytoscape.getCyNode(parts[0], false);
 			final CyNode target = Cytoscape.getCyNode(parts[2], false);
 			final String interaction = parts[1].substring(1,
 					parts[1].length() - 1);
 			targetEdge = Cytoscape.getCyEdge(source, target,
 					Semantics.INTERACTION, interaction, false);
 		}
 		return targetEdge;
 	}
 
 	private CyNetwork createNetwork(final CyNetwork parent,
 			final InputStream is, final boolean viewAvailable)
 			throws IOException, JAXBException {
 
 		// Read an XGMML file
 		final XGMMLReader reader = new XGMMLReader(is);
 		reader.read();
 
 		/*
 		 * Create the CyNetwork. First, set the view threshold to 0. By doing
 		 * so, we can disable the auto-creating of the CyNetworkView.
 		 */
 		final int realThreshold = Integer.valueOf(
 				CytoscapeInit.getProperties().getProperty("viewThreshold"))
 				.intValue();
 		CytoscapeInit.getProperties().setProperty("viewThreshold",
 				Integer.toString(0));
 
 		CyNetwork network = null;
 		if (parent == null) {
 			network = Cytoscape.createNetwork(reader.getNodeIndicesArray(),
 					reader.getEdgeIndicesArray(), reader.getNetworkID());
 
 		} else {
 			network = Cytoscape
 					.createNetwork(reader.getNodeIndicesArray(), reader
 							.getEdgeIndicesArray(), reader.getNetworkID(),
 							parent);
 		}
 
 		// Set network Attributes here, not in the read() method in XGMMLReader!
 		// Otherwise, ID mismatch may happen.
 		reader.setNetworkAttributes(network);
 
 		// Reset back to the real View Threshold
 		CytoscapeInit.getProperties().setProperty("viewThreshold",
 				Integer.toString(realThreshold));
 
 		// Conditionally, Create the CyNetworkView
 		if (viewAvailable) {
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
 					final DingNetworkView view = (DingNetworkView) Cytoscape
 							.getCurrentNetworkView();
 					view.setGraphLOD(new CyGraphLOD());
 				}
 			});
 
 			final String curVS = (String) vsMapByName.get(network.getTitle());
 			final CyNetworkView curView = Cytoscape.getNetworkView(network
 					.getIdentifier());
 			if (curVS != null) {
 				curView.setVisualStyle(curVS);
 				Cytoscape.getDesktop().getVizMapUI().getStyleSelector()
 						.resetStyles(curVS);
 				Cytoscape.getDesktop().getVizMapUI().visualStyleChanged();
 				Cytoscape.getVisualMappingManager().setVisualStyle(curVS);
 
 			} else {
 				curView.setVisualStyle(Cytoscape.getVisualMappingManager()
 						.getVisualStyle().getName());
 			}
 
 			// set view zoom
 			final Double zoomLevel = reader.getGraphViewZoomLevel();
 			if (zoomLevel != null) {
 				curView.setZoom(zoomLevel.doubleValue());
 			}
 			// set view center
 			final Point2D center = reader.getGraphViewCenter();
 			if (center != null) {
 				((DGraphView) curView).setCenter(center.getX(), center.getY());
 			}
 		}
 		return network;
 	}
 
 	// same as other loaders
 	//
 	private void createCyNetworkView(final CyNetwork cyNetwork) {
 		final DingNetworkView view = new DingNetworkView(cyNetwork, cyNetwork
 				.getTitle());
 
 		// For performance, hide canvas.
 		view.getCanvas().setVisible(false);
 
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
