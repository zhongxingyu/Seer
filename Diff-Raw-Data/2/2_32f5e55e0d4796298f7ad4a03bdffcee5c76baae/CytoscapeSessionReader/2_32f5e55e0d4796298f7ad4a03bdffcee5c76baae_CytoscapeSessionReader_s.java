 /*
  File: CytoscapeSessionReader.java
 
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
 package cytoscape.data.readers;
 
 import java.awt.Component;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.net.JarURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import javax.swing.JInternalFrame;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import cytoscape.CyEdge;
 import cytoscape.CyNetwork;
 import cytoscape.CyNode;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.bookmarks.Bookmarks;
 import cytoscape.data.Semantics;
 import cytoscape.generated.Child;
 import cytoscape.generated.Cysession;
 import cytoscape.generated.Edge;
 import cytoscape.generated.HiddenEdges;
 import cytoscape.generated.HiddenNodes;
 import cytoscape.generated.Network;
 import cytoscape.generated.NetworkFrame;
 import cytoscape.generated.Node;
 import cytoscape.generated.Ontology;
 import cytoscape.generated.SelectedEdges;
 import cytoscape.generated.SelectedNodes;
 import cytoscape.task.TaskMonitor;
 import cytoscape.util.PercentUtil;
 import cytoscape.util.URLUtil;
 import cytoscape.view.CyNetworkView;
 import cytoscape.logger.CyLogger;
 
 /**
  * Reader to load CYtoscape Session file (.cys).<br>
  * This class unzip cys file and read all files in the archive.
  * <p>
  * This class accept input as URL only! If it is a file, use File.toURL() to get
  * platform dependent file URL.
  * </p>
  *
  * @version 1.0
  * @since Cytoscape 2.3
  * @see cytoscape.data.readers.XGMMLReader
  * @author kono
  *
  */
 public class CytoscapeSessionReader {
 	/**
 	 *
 	 */
 	public static final String PACKAGE_NAME = "cytoscape.generated";
 
 	/**
 	 *
 	 */
 	public static final String BOOKMARK_PACKAGE_NAME = "cytoscape.bookmarks";
 
 	/**
 	 *
 	 */
 	public static final String CYSESSION = "cysession.xml";
 
 	/**
 	 *
 	 */
 	public static final String VIZMAP_PROPS = "vizmap.props";
 
 	/**
 	 *
 	 */
 	public static final String CY_PROPS = "cytoscape.props";
 
 	/**
 	 *
 	 */
 	public static final String XGMML_EXT = ".xgmml";
 	private static final String BOOKMARKS_FILE = "session_bookmarks.xml";
 	private static final String NETWORK_ROOT = "Network Root";
 	private URL sourceURL;
 	private HashMap networkURLs = null;
 	private URL cysessionFileURL = null;
 	private URL vizmapFileURL = null;
 	private URL cytoscapePropsURL = null;
 	private URL bookmarksFileURL = null;
 	private HashMap<String, Network> netMap;
 	private String sessionID;
 	private Cysession session;
 	private List networkList;
 	private Bookmarks bookmarks = null;
 	private HashMap<String, List<File>> pluginFileListMap;
 	private HashMap<String, List<String>> theURLstrMap = new HashMap<String, List<String>>();
 
 	// Task monitor
 	private TaskMonitor taskMonitor = null;
 	private PercentUtil percentUtil;
 	private float networkCounter = 0;
 	private float netIndex = 0;
 	private long start;
 	private String lastVSName = null;
 
 	// Logging
 	private CyLogger logger = null;
 
 	/**
 	 * Constructor for remote file (specified by an URL)<br>
 	 *
 	 * @param sourceName
 	 * @throws IOException
 	 *
 	 * This is for remote session file (URL).
 	 */
 	public CytoscapeSessionReader(final URL sourceName, final TaskMonitor monitor)
 	    throws IOException {
 		this.sourceURL = sourceName;
 
 		if (sourceName.getProtocol().equals("file"))
 			this.sourceURL = sourceName;
 		else
 			this.sourceURL = temporaryLocalFileURL(sourceName);
 
 		networkList = new ArrayList();
 		bookmarks = new Bookmarks();
 		pluginFileListMap = new HashMap<String, List<File>>();
 
 		this.taskMonitor = monitor;
 		if (monitor != null) {
 			percentUtil = new PercentUtil(1);
 		}
 
 		this.logger = CyLogger.getLogger(CytoscapeSessionReader.class);
 	}
 
 	/**
 	 * Creates a new CytoscapeSessionReader object.
 	 *
 	 * @param fileName  DOCUMENT ME!
 	 * @param monitor  DOCUMENT ME!
 	 *
 	 * @throws IOException  DOCUMENT ME!
 	 */
 	public CytoscapeSessionReader(final String fileName, final TaskMonitor monitor)
 	    throws IOException {
 		this(new File(fileName).toURL(), monitor);
 	}
 
 	/**
 	 * Constructor.<br>
 	 * Create reader from file name.
 	 *
 	 * @param fileName
 	 * @throws IOException
 	 */
 	public CytoscapeSessionReader(final String fileName) throws IOException {
 		this(new File(fileName).toURL(), (TaskMonitor)null);
 	}
 
 	/**
 	 * Creates a new CytoscapeSessionReader object.
 	 *
 	 * @param sourceName  DOCUMENT ME!
 	 *
 	 * @throws IOException  DOCUMENT ME!
 	 */
 	public CytoscapeSessionReader(final URL sourceName) throws IOException {
 		this(sourceName, (TaskMonitor)null);
 	}
 
 	/**
 	 * Extract Zip entries in the remote file
 	 *
 	 * @param sourceName
 	 * @throws IOException
 	 */
 	private void extractEntry() throws IOException {
 		/*
 		 * This is an important part!
 		 *
 		 * We can create InputStream directly from URL, but it does not work
 		 * always due to the cashing mechanism in URLConnection.
 		 *
 		 * By default, URLConnection creates a cash for session file name. This
 		 * will be used even after we saved the session. Due to the conflict
 		 * between cashed name and new saved session name (generated from system
 		 * time), session reader cannot find the entry in the zip file. To avoid
 		 * this problem, we shoud turn off the cashing mechanism by using:
 		 *
 		 * URLConnection.setDefaultUseCaches(false)
 		 *
 		 * This is a "sticky" parameter for all URLConnections and we have to
 		 * set this only once.
 		 *
 		 */
 		final URLConnection juc = sourceURL.openConnection();
 		juc.setDefaultUseCaches(false);
 
 		ZipInputStream zis = new ZipInputStream(juc.getInputStream());
 
 		networkURLs = new HashMap();
 
 		// Extract list of entries
 		ZipEntry zen = null;
 		String entryName = null;
 
 		while ((zen = zis.getNextEntry()) != null) {
 			entryName = zen.getName();
 
 			if (entryName.contains("/plugins/")) {
 				extractPluginEntry(entryName);
 			} else if (entryName.endsWith(CYSESSION)) {
 				cysessionFileURL = new URL("jar:" + sourceURL.toString() + "!/" + entryName);
 			} else if (entryName.endsWith(VIZMAP_PROPS)) {
 				vizmapFileURL = new URL("jar:" + sourceURL.toString() + "!/" + entryName);
 			} else if (entryName.endsWith(CY_PROPS)) {
 				cytoscapePropsURL = new URL("jar:" + sourceURL.toString() + "!/" + entryName);
 			} else if (entryName.endsWith(XGMML_EXT)) {
 				URL networkURL = new URL("jar:" + sourceURL.toString() + "!/" + entryName);
 				networkURLs.put(entryName, networkURL);
 				networkCounter++;
 			} else if (entryName.endsWith(BOOKMARKS_FILE)) {
 				bookmarksFileURL = new URL("jar:" + sourceURL.toString() + "!/" + entryName);
 			} else {
 				logger.warn("Unknown entry found in session zip file: " + entryName);
 			}
 		} // while loop
 
 		if (zis != null) {
 			try {
 				zis.close();
 			} finally {
 				zis = null;
 			}
 		}
 	}
 
 	private void extractPluginEntry(String entryName) {
 		String[] items = entryName.split("/");
 		
 		if (items.length < 3) {
 			// It's a directory name, not a file name
 			return;
 		}
 		
 		String pluginName = items[2];
 		String URLstr = "jar:" + sourceURL.toString() + "!/" + entryName;
 
 		if (theURLstrMap.containsKey(pluginName)) {
 			List<String> theURLstrList = theURLstrMap.get(pluginName);
 			theURLstrList.add(URLstr);
 		} else {
 			List<String> theURLstrList = new ArrayList<String>();
 			theURLstrList.add(URLstr);
 			theURLstrMap.put(pluginName, theURLstrList);
 		}
 	}
 
 	/**
 	 * Read a session file.
 	 *
 	 * @throws IOException
 	 * @throws JAXBException
 	 */
 	public void read() throws IOException, JAXBException, Exception {
 		start = System.currentTimeMillis();
 
 		// All listeners should listen to this event to ignore unnecessary events!
 		Cytoscape.firePropertyChange(Integer.toString(Cytoscape.SESSION_OPENED), null, true);
 
 		if (Cytoscape.getDesktop() != null) {
 			Cytoscape.getDesktop().getVizMapperUI().initializeTableState();
 		}
 
 		try {
 			unzipSessionFromURL();
 		} catch (cytoscape.visual.DuplicateCalculatorNameException dcne) {
 			logger.warn("Duplicate VS name found.  It will be ignored...", dcne);
 		}
 
 		//logger.debug("unzipSessionFromURL: " + (System.currentTimeMillis() - start) + " msec.");
 		if (session.getSessionState().getDesktop() != null) {
 			restoreDesktopState();
 
 			//logger.debug("restoreDesktopState: " + (System.currentTimeMillis() - start) + " msec.");
 		}
 
 		if (session.getSessionState().getServer() != null) {
 			restoreOntologyServerStatus();
 			//logger.debug("restoreOntologyServerStatus: " + (System.currentTimeMillis() - start) + " msec.");
 		}
 
 		// Send signal to others
 		Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);
 		Cytoscape.firePropertyChange(Cytoscape.NETWORK_LOADED, null, null);
 		//logger.debug("fire attrs and network loaded: " + (System.currentTimeMillis() - start) + " msec.");
 
 		// Send signal to plugins
 		Cytoscape.firePropertyChange(Cytoscape.RESTORE_PLUGIN_STATE, pluginFileListMap, null);
 		//logger.debug("fire restore_plugin_state: " + (System.currentTimeMillis() - start) + " msec.");
 		deleteTmpPluginFiles();
 		//logger.debug("deleteTmpPluginFiles: " + (System.currentTimeMillis() - start) + " msec.");
 
 		// Send message with list of loaded networks.
 		Cytoscape.firePropertyChange(Cytoscape.SESSION_LOADED, null, networkList);
 
 		// Restore listener for VizMapper.
 		if (Cytoscape.getDesktop() != null) {
 
 			// Cleanup view
 			final CyNetworkView curView = Cytoscape.getCurrentNetworkView();
 
 			if ((curView != null) && (curView.equals(Cytoscape.getNullNetworkView()) == false)) {
 				curView.setVisualStyle(lastVSName);
 				Cytoscape.getVisualMappingManager().setNetworkView(curView);
 				Cytoscape.getVisualMappingManager().setVisualStyle(curView.getVisualStyle());
 				curView.redrawGraph(false, true);
 			}
 
 			Cytoscape.getDesktop().getVizMapperUI().enableListeners(true);
 		}
 
 		logger.info("Session loaded in " + (System.currentTimeMillis() - start) + " msec.");
 	}
 
 	// Delete tmp files (the plugin state files) to cleanup
 	private void deleteTmpPluginFiles() {
 		if ((pluginFileListMap == null) || (pluginFileListMap.size() == 0))
 			return;
 
 		Set<String> pluginSet = pluginFileListMap.keySet();
 
 		for (String plugin : pluginSet) {
 			List<File> theFileList = pluginFileListMap.get(plugin);
 
 			for (File theFile : theFileList) {
 				if (theFile != null) {
 					theFile.delete();
 				}
 			}
 		}
 	}
 
 	/**
 	 * Decompress session file
 	 *
 	 * @throws IOException
 	 * @throws JAXBException
 	 */
 	private void unzipSessionFromURL() throws IOException, JAXBException, Exception {
 		extractEntry();
 		logger.info("extractEntry: " + (System.currentTimeMillis() - start) + " msec.");
 
 		/*
 		 * Check the contents.  If broken/invalid, throw exception.
 		 */
 		if ((cysessionFileURL == null) || (vizmapFileURL == null) || (cytoscapePropsURL == null)) {
 			IOException e = new IOException("Session file is broken or this is not a session file.");
 			throw e;
 		}
 
 		// restore vizmap.props
 		Cytoscape.firePropertyChange(Cytoscape.VIZMAP_RESTORED, null, vizmapFileURL);
 
 		// Restore bookmarks
 		if (bookmarksFileURL != null) {
 			bookmarks = getBookmarksFromZip(bookmarksFileURL);
 			Cytoscape.setBookmarks(bookmarks);
 //			logger.debug("getBookmarksFromZip: " + (System.currentTimeMillis() - start)
 //			                   + " msec.");
 		}
 
 		// restore cytoscape properties
 		// CytoscapeInit.getProperties().load(cytoscapePropsURL.openStream());
 		// Even though cytoscapePropsURL is probably a local URL, error on the
 		// side of caution and use URLUtil to get the input stream (which
 		// handles proxy servers and cached pages):
 		CytoscapeInit.getProperties().load(URLUtil.getBasicInputStream(cytoscapePropsURL));
 		loadCySession();
 		logger.info("loadCySession: " + (System.currentTimeMillis() - start) + " msec.");
 
 		// restore plugin state files
 		restorePlugnStateFilesFromZip();
 //		logger.debug("restorePlugnStateFilesFromZip: " + (System.currentTimeMillis() - start)
 //		                   + " msec.");
 	}
 
 	private void restorePlugnStateFilesFromZip() {
 		if ((theURLstrMap == null) || (theURLstrMap.size() == 0)) {
 			return;
 		}
 
 		Set<String> pluginSet = theURLstrMap.keySet();
 
 		for (String pluginName : pluginSet) {
 			List<String> URLstrList = theURLstrMap.get(pluginName);
 
 			if ((URLstrList == null) || (URLstrList.size() == 0)) {
 				continue;
 			}
 
 			File theFile = null;
 
 			for (String URLstr : URLstrList) {
 				int index = URLstr.lastIndexOf("/");
				String fileName = pluginName + "_" + URLstr.substring(index + 1);
 
 				theFile = new File(fileName);
 
 				try {
 					// get inputstream from ZIP
 					URL theURL = new URL(URLstr);
 
 					// InputStream is = theURL.openStream();
 					// Even though theURL derives from a File, error on the
 					// side of caution and use URLUtil to get the input stream (which
 					// handles proxy servers and cached pages):
 					InputStream is = URLUtil.getBasicInputStream(theURL);
 
 					// Write input stream into tmp file
 					BufferedWriter out = null;
 					BufferedReader in = null;
 
 					in = new BufferedReader(new InputStreamReader(is));
 					out = new BufferedWriter(new FileWriter(theFile));
 
 					// Write to tmp file
 					String inputLine;
 
 					while ((inputLine = in.readLine()) != null) {
 						out.write(inputLine);
 						out.newLine();
 					}
 
 					in.close();
 					out.close();
 				} catch (IOException e) {
 					theFile = null;
 					logger.error("Error: reading from zip: " + URLstr, e);
 				}
 
 				if (theFile == null)
 					continue;
 
 				// Put the file into pluginFileListMap
 				if (!pluginFileListMap.containsKey(pluginName)) {
 					List<File> fileList = new ArrayList<File>();
 					fileList.add(theFile);
 					pluginFileListMap.put(pluginName, fileList);
 				} else {
 					List<File> fileList = pluginFileListMap.get(pluginName);
 					fileList.add(theFile);
 				}
 			}
 		}
 	}
 
 	private Bookmarks getBookmarksFromZip(URL pBookmarksFileURL) {
 		Bookmarks theBookmark = null;
 
 		try {
 			// InputStream is = pBookmarksFileURL.openStream();
 			// Use URLUtil to get the InputStream since we might be using a proxy server 
 			// and because pages may be cached:
 			InputStream is = URLUtil.getBasicInputStream(pBookmarksFileURL);
 
 			final JAXBContext jaxbContext = JAXBContext.newInstance(BOOKMARK_PACKAGE_NAME,
 			                                                        this.getClass().getClassLoader());
 			final Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
 
 			theBookmark = (Bookmarks) unmarshaller.unmarshal(is);
 
 			if (is != null) {
 				is.close();
 			}
 		} catch (FileNotFoundException e1) {
 			logger.warn("Can not find bookmark file in "+pBookmarksFileURL.toString(), e1);
 		} catch (IOException e2) {
 			logger.warn("Can not read bookmark file from "+pBookmarksFileURL.toString(), e2);
 		} catch (JAXBException e3) {
 			logger.warn("XML parse err in bookmark file in "+pBookmarksFileURL.toString(), e3);
 		}
 
 		return theBookmark;
 	}
 
 	private void loadCySession() throws JAXBException, IOException, Exception {
 		// InputStream is = cysessionFileURL.openStream();
 		// Even though cytoscapeFileURL is probably a local URL, error on the
 		// side of caution and use URLUtil to get the input stream (which
 		// handles proxy servers and cached pages):
 		InputStream is = URLUtil.getBasicInputStream(cysessionFileURL);
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
 		netMap = new HashMap<String, Network>();
 
 		for (Network curNet : session.getNetworkTree().getNetwork()) {
 			netMap.put(curNet.getId(), curNet);
 		}
 
 		walkTree(netMap.get(NETWORK_ROOT), null, cysessionFileURL);
 	}
 
 	private void restoreDesktopState() {
 		
 		// Restore Desktop size
 		Cytoscape.getDesktop()
 		         .setSize(session.getSessionState().getDesktop().getDesktopSize().getWidth()
 		                         .intValue(),
 		                  session.getSessionState().getDesktop().getDesktopSize().getHeight()
 		                         .intValue());
 
 		final List<NetworkFrame> frames = session.getSessionState().getDesktop().getNetworkFrames()
 		                          .getNetworkFrame();
 		final Map<String, NetworkFrame> frameMap = new HashMap<String, NetworkFrame>();
 
 		for(NetworkFrame netFrame: frames)
 			frameMap.put(netFrame.getFrameID(), netFrame);
 
 		Component[] desktopFrames = Cytoscape.getDesktop().getNetworkViewManager().getDesktopPane()
 		                              .getComponents();
 
 		for (int i = 0; i < desktopFrames.length; i++) {
 			JInternalFrame frame = (JInternalFrame) desktopFrames[i];
 			NetworkFrame nFrame = (NetworkFrame) frameMap.get(frame.getTitle());
 
 			if (nFrame != null) {
 				frame.setSize(nFrame.getWidth().intValue(), nFrame.getHeight().intValue());
 				frame.setLocation(nFrame.getX().intValue(), nFrame.getY().intValue());
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
 		final List<Child> children = currentNetwork.getChild();
 
 		// Traverse using recursive call
 		final int numChildren = children.size();
 		Child child;
 		Network childNet;
 		URL targetNetworkURL;
 		JarURLConnection jarConnection;
 		InputStream networkStream;
 		CyNetwork new_network = null;
 		CyNetworkView curNetView;
 
 		for (int i = 0; i < numChildren; i++) {
 			child = children.get(i);
 			childNet = netMap.get(child.getId());
 
 			String targetNwUrlName = sessionID + "/" + childNet.getFilename();
 			targetNetworkURL = (URL) networkURLs.get(targetNwUrlName);
 
 			// handle the unlikely event that the stored network is corrupted with a bad filename (bug fix)
 			if (targetNetworkURL == null) {
 				logger.error("Session file corrupt: Filename " + childNet.getFilename()
 				                      + " does not correspond to a network of that name in session file");
 				continue;
 			}
 
 			jarConnection = (JarURLConnection) targetNetworkURL.openConnection();
 			networkStream = (InputStream) jarConnection.getContent();
 
 			// Get the current state of the vsbSwitch
 			Properties prop = CytoscapeInit.getProperties();
 			String vsbSwitch = prop.getProperty("visualStyleBuilder");
 			// Since we're reading a session (which already has visual
 			// styles defined) force the vsbSwitch off
 			prop.setProperty("visualStyleBuilder", "off");
 
 			final XGMMLReader reader = new XGMMLReader(networkStream);
 			try {
 				new_network = Cytoscape.createNetwork(reader, false, parent);
 			} catch (Exception e) {
 				String message = "Unable to read XGMML file: "+childNet.getFilename()+".  "+e.getMessage();
 				logger.error(message, e);
 				Cytoscape.destroyNetwork(new_network);
 				if (taskMonitor != null)
 					taskMonitor.setException(e, message);
 				// Load child networks, even if this network is bad
 				if (childNet.getChild().size() != 0)
 					walkTree(childNet, new_network, sessionSource);
 				continue;
 			}
 			logger.info("XGMMLReader " + new_network.getIdentifier() + ": "
 			                   + (System.currentTimeMillis() - start) + " msec.");
 
 			// Restore the original state of the vsbSwitch
 			if (vsbSwitch != null)
 				prop.setProperty("visualStyleBuilder", vsbSwitch);
 			else
 				prop.remove("visualStyleBuilder");
 
 			if ((taskMonitor != null) && (networkCounter >= 20)) {
 				netIndex++;
 				taskMonitor.setPercentCompleted(((Number) ((netIndex / networkCounter) * 100))
 															 .intValue());
 			}
 
 			if (networkStream != null) {
 				try {
 					networkStream.close();
 				} finally {
 					networkStream = null;
 				}
 			}
 
 			networkList.add(new_network.getIdentifier());
 
 			// Execute if view is available.
 			if (childNet.isViewAvailable()) {
 				// Set visual style
 				String vsName = childNet.getVisualStyle();
 
 				if (vsName == null)
 					vsName = "default";
 
 				lastVSName = vsName;
 
 				curNetView = Cytoscape.createNetworkView(new_network, new_network.getTitle(),
 				                                         reader.getLayoutAlgorithm());
 
 				//logger.debug("createNetworkView "+new_network.getIdentifier()+": " + (System.currentTimeMillis() - start) + " msec.");
 				curNetView.setVisualStyle(vsName);
 
 				Cytoscape.getVisualMappingManager().setNetworkView(curNetView);
 				Cytoscape.getVisualMappingManager().setVisualStyle(vsName);
 
 				//logger.debug("setVisualStyle stuff "+new_network.getIdentifier()+": " + (System.currentTimeMillis() - start) + " msec.");
 				reader.doPostProcessing(new_network);
 				//logger.debug("doPostProcessing "+new_network.getIdentifier()+": " + (System.currentTimeMillis() - start) + " msec.");
 
 				// Set hidden nodes + edges
 				setHiddenNodes(curNetView, (HiddenNodes) childNet.getHiddenNodes());
 				setHiddenEdges(curNetView, (HiddenEdges) childNet.getHiddenEdges());
 			}
 
 			setSelectedNodes(new_network, (SelectedNodes) childNet.getSelectedNodes());
 			setSelectedEdges(new_network, (SelectedEdges) childNet.getSelectedEdges());
 
 			// Load child networks
 			if (childNet.getChild().size() != 0)
 				walkTree(childNet, new_network, sessionSource);
 		}
 	}
 
 	private void setSelectedNodes(final CyNetwork network, final SelectedNodes selected) {
 		if (selected == null) {
 			return;
 		}
 
 		final List selectedNodeList = new ArrayList();
 		final Iterator it = selected.getNode().iterator();
 
 		while (it.hasNext()) {
 			final Node selectedNode = (Node) it.next();
 			selectedNodeList.add(Cytoscape.getCyNode(selectedNode.getId(), false));
 		}
 
 		network.setSelectedNodeState(selectedNodeList, true);
 	}
 
 	private void setHiddenNodes(final CyNetworkView view, final HiddenNodes hidden) {
 		if (hidden == null) {
 			return;
 		}
 
 		final Iterator it = hidden.getNode().iterator();
 
 		while (it.hasNext()) {
 			final Node hiddenNodeObject = (Node) it.next();
 			final CyNode hiddenNode = Cytoscape.getCyNode(hiddenNodeObject.getId(), false);
 			view.hideGraphObject(view.getNodeView(hiddenNode));
 		}
 	}
 
 	private void setHiddenEdges(final CyNetworkView view, final HiddenEdges hidden) {
 		if (hidden == null) {
 			return;
 		}
 
 		final Iterator it = hidden.getEdge().iterator();
 
 		while (it.hasNext()) {
 			final Edge hiddenEdgeObject = (Edge) it.next();
 			final CyEdge hiddenEdge = getCyEdge(hiddenEdgeObject);
 
 			if (hiddenEdge != null) {
 				view.hideGraphObject(view.getEdgeView(hiddenEdge));
 			}
 		}
 	}
 
 	private void setSelectedEdges(final CyNetwork network, final SelectedEdges selected) {
 		if (selected == null) {
 			return;
 		}
 
 		CyEdge targetEdge = null;
 		final List selectedEdgeList = new ArrayList();
 		final Iterator it = selected.getEdge().iterator();
 
 		while (it.hasNext()) {
 			final cytoscape.generated.Edge selectedEdge = (cytoscape.generated.Edge) it.next();
 			targetEdge = getCyEdge(selectedEdge);
 
 			if (targetEdge != null) {
 				selectedEdgeList.add(targetEdge);
 			}
 		}
 
 		network.setSelectedEdgeState(selectedEdgeList, true);
 	}
 
 	private CyEdge getCyEdge(final Edge edge) {
 		CyEdge targetEdge = null;
 
 		final String sourceString = edge.getSource();
 		final String targetString = edge.getTarget();
 		CyNode source = null;
 		CyNode target = null;
 		String interaction = edge.getInteraction();
 
 		// Try to get CyEdge by the source & target IDs
 		if ((sourceString != null) && (targetString != null)) {
 			source = Cytoscape.getCyNode(sourceString);
 			target = Cytoscape.getCyNode(targetString);
 		}
 
 		// If all 3 parameters are available, try to get CyEdge
 		if (((source != null) & (target != null)) && (interaction != null)) {
 			targetEdge = Cytoscape.getCyEdge(source, target, Semantics.INTERACTION, interaction,
 			                                 false, true);
 		}
 
 		// If CyEdge is still null, try to get one from ID
 		if (targetEdge == null) {
 			final String[] parts = edge.getId().split(" ");
 
 			if (parts.length == 3) {
 				source = Cytoscape.getCyNode(parts[0], false);
 				target = Cytoscape.getCyNode(parts[2], false);
 				interaction = parts[1].substring(1, parts[1].length() - 1);
 
 				if ((source != null) && (target != null) && (interaction != null)) {
 					targetEdge = Cytoscape.getCyEdge(source, target, Semantics.INTERACTION,
 					                                 interaction, false, true);
 				}
 			}
 		}
 
 		return targetEdge;
 	}
 
 	/**
 	 * Extract Session Note test field in cysession.xml
 	 *
 	 * @return Session Note
 	 */
 	public String getCysessionNote() {
 		return session.getSessionNote();
 	}
 
 	/**
 	 * Restore list of ontology servers
 	 *
 	 * @throws MalformedURLException
 	 *
 	 */
 	private void restoreOntologyServerStatus() throws MalformedURLException {
 		Map<String, URL> newMap = new HashMap<String, URL>();
 
 		List<Ontology> servers = session.getSessionState().getServer().getOntologyServer()
 		                                .getOntology();
 		String targetCyNetworkID = null;
 		String curator = null;
 		String description = null;
 
 		for (Ontology server : servers) {
 			newMap.put(server.getName(), new URL(server.getHref()));
 			targetCyNetworkID = getNetworkIdFromTitle(server.getName());
 
 			cytoscape.data.ontology.Ontology onto = new cytoscape.data.ontology.Ontology(server
 			                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              .getName(),
 			                                                                             curator,
 			                                                                             description,
 			                                                                             Cytoscape
 			                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                  .getNetwork(targetCyNetworkID));
 			Cytoscape.getOntologyServer().addOntology(onto);
 		}
 
 		Cytoscape.getOntologyServer().setOntologySources(newMap);
 	}
 
 	private String getNetworkIdFromTitle(String title) {
 		Set<CyNetwork> networks = Cytoscape.getNetworkSet();
 
 		for (CyNetwork net : networks) {
 			if (net.getTitle().equals(title)) {
 				return net.getIdentifier();
 			}
 		}
 
 		// Not found
 		return null;
 	}
 
 	/**
 	 * The CytoscapeSessionReader class reopens the session file each time it accesses or reads one of
 	 * the files contained within the session file (which is just a zip container). This is fine for
 	 * local files, but if you refer to a remote URL, it means that the session file is downloaded once
 	 * for each file accessed -- that's 4-5 times on average, and performance suffers immensely.
 	 *
 	 * This method will attempt to create a local temporary file and copy the contents of the remote URL
 	 * into the temporary file. It returns the URL for the local temporary file. Note that the file is
 	 * marked "deleteOnExit", which means that if the JVM exits normally, it will be cleaned up automatically.
 	 *
 	 * The method returns either the local file URL, or the original remote URL if there was a problem in
 	 * creating the local file.
 	 *
 	 * @param remoteURL
 	 * @return
 	 */
 	private URL temporaryLocalFileURL(final URL remoteURL) {
 		try {
 			// We need a unique local filename for the temporary file - which seems like a
 			// logical job for Java's UUID class...
 			File tempFile = File.createTempFile(java.util.UUID.randomUUID().toString(), ".cys");
 			tempFile.deleteOnExit();
 
 			byte[] buffer = new byte[100000];
 			java.io.OutputStream localContent = new java.io.FileOutputStream(tempFile);
 
 			// java.io.InputStream   remoteContent  = remoteURL.openStream();
 			// Use URLUtil to get the InputStream since we might be using a proxy server 
 			// and because pages may be cached:
 			java.io.InputStream remoteContent = URLUtil.getBasicInputStream(remoteURL);
 
 			for (int nBytes = remoteContent.read(buffer); nBytes > 0;
 			     nBytes = remoteContent.read(buffer)) {
 				localContent.write(buffer, 0, nBytes);
 			}
 
 			remoteContent.close();
 			localContent.close();
 			remoteContent = null;
 			localContent = null;
 			buffer = null;
 
 			return tempFile.toURL();
 		} catch (FileNotFoundException e) {
 			// This could happen if the OS' temp directory doesn't exist
 			logger.error("Can't create a temporary file.", e);
 		} catch (MalformedURLException e) {
 			// If the provided URL is bad, this will happen
 			logger.error("Bad URL provided: " + remoteURL.toString(), e);
 		} catch (IOException e) {
 			// Any problem read or writing from either the remoteURL or the tempFile
 			logger.error("I/O error while cacheing URL data",e);
 		}
 
 		return remoteURL; // if we can't make a local copy for some reason, work with the remote.
 	}
 }
