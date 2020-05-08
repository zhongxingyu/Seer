 /*
  File: CytoscapeDesktop.java
 
  Copyright (c) 2010, The Cytoscape Consortium (www.cytoscape.org)
 
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
 package cytoscape.view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.List;
 import java.util.Properties;
 
 import javax.help.HelpBroker;
 import javax.help.HelpSet;
 import javax.swing.ImageIcon;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.SwingConstants;
 import javax.swing.WindowConstants;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.SwingPropertyChangeSupport;
 
 import cytoscape.CyNetwork;
 import cytoscape.Cytoscape;
 import cytoscape.CytoscapeInit;
 import cytoscape.CytoscapeVersion;
 import cytoscape.data.webservice.ui.WebServiceContextMenuListener;
 import cytoscape.util.undo.CyUndo;
 import cytoscape.view.cytopanels.BiModalJSplitPane;
 import cytoscape.view.cytopanels.CytoPanel;
 import cytoscape.view.cytopanels.CytoPanelImp;
 import cytoscape.view.cytopanels.CytoPanelState;
 import cytoscape.visual.VisualMappingManager;
 import cytoscape.visual.VisualStyle;
 import cytoscape.visual.ui.HideEdgeListener;
 import cytoscape.visual.ui.HideNodeListener;
 import cytoscape.visual.ui.NestedNetworkListener;
 import cytoscape.visual.ui.VizMapBypassNetworkListener;
 import cytoscape.visual.ui.VizMapperMainPanel;
 
 /**
  * The CytoscapeDesktop is the central Window for working with Cytoscape
  */
 public class CytoscapeDesktop extends JFrame implements PropertyChangeListener {
 	
 	private static final long serialVersionUID = 2986320083629749014L;
 	
 	protected long lastPluginRegistryUpdate;
 	protected int returnVal;
 
 	// Default sizes of components
 	private static final Dimension DEF_DESKTOP_SIZE = new Dimension(980, 720);
 	private static final int DEF_DEVIDER_LOCATION = 325;
 	private static final int DEF_DATAPANEL_DEVIDER_LOCATION = 410;
 
 	/**
 	 *
 	 */
 	public static final String NETWORK_VIEWS_SELECTED = "NETWORK_VIEWS_SELECTED";
 
 	/**
 	 *
 	 */
 	public static final String NETWORK_VIEW_FOCUSED = "NETWORK_VIEW_FOCUSED";
 
 	/**
 	 *
 	 */
 	public static final String NETWORK_VIEW_FOCUS = "NETWORK_VIEW_FOCUS";
 
 	/**
 	 *
 	 */
 	public static final String NETWORK_VIEW_CREATED = "NETWORK_VIEW_CREATED";
 
 	/**
 	 *
 	 */
 	public static final String NETWORK_VIEW_DESTROYED = "NETWORK_VIEW_DESTROYED";
 
 	// state variables
 	/**
 	 *
 	 */
 	public static final String VISUAL_STYLE = "VISUAL_STYLE";
 
 	/**
 	 *
 	 */
 	public static final String VIZMAP_ENABLED = "VIZMAP_ENABLED";
 
 	private static final String SMALL_ICON = "images/c16.png";
 
 	protected VisualStyle defaultVisualStyle;
 
 	/**
 	 * The network panel that sends out events when a network is selected from
 	 * the Tree that it contains.
 	 */
 	protected NetworkPanel networkPanel;
 
 	/**
 	 * The CyMenus object provides access to the all of the menus and toolbars
 	 * that will be needed.
 	 */
 	protected CyMenus cyMenus;
 
 	/**
 	 * The NetworkViewManager can support three types of interfaces.
 	 * Tabbed/InternalFrame/ExternalFrame
 	 */
 	protected NetworkViewManager networkViewManager;
 
 
 	/**
 	 * provides support for property change events
 	 */
 	protected final SwingPropertyChangeSupport pcs = new SwingPropertyChangeSupport(this);
 
 	/**
 	 * The GraphViewController for all NetworkViews that we know about
 	 */
 	protected GraphViewController graphViewController;
 
 	/**
 	 * Provides Operations for Mapping Data Attributes of CyNetworks to
 	 * CyNetworkViews
 	 */
 	protected VisualMappingManager vmm;
 
 	/**
 	 * New VizMapper UI
 	 */
 	protected VizMapperMainPanel vizmapperUI;
 
 	/**
 	 * Current network and view.
 	 */
 	protected String currentNetworkID;
 	protected String currentNetworkViewID;
 	
 	// CytoPanels
 	protected CytoPanelImp cytoPanelWest;
 	protected CytoPanelImp cytoPanelEast;
 	protected CytoPanelImp cytoPanelSouth;
 	protected CytoPanelImp cytoPanelSouthWest;
 
 	// Status Bar
 	protected JLabel statusBar;
 
 	protected final JPanel main_panel;
 
 	// This is used to keep track of the visual style combo box. 
 	// This is the index of the box in the toolbar. We use this so that we can
 	// add and remove the stylebox from the same place.
 	protected int styleBoxIndex = -1;
 
 	// Overview Window;
 	private BirdsEyeViewHandler bevh;
 
 
 	/**
 	 * Creates a new CytoscapeDesktop object.
 	 */
 	public CytoscapeDesktop() {
 		super("Cytoscape Desktop (New Session)");
 
 		setIconImage(Toolkit.getDefaultToolkit().getImage(Cytoscape.class.getResource(SMALL_ICON)));
 
 		main_panel = new JPanel();
 		main_panel.setLayout(new BorderLayout());
 
 		// ------------------------------//
 		// Set up the Panels, Menus, and Event Firing
 		networkViewManager = new NetworkViewManager(this);
 		
 		getBirdsEyeViewHandler();
 		
 		getSwingPropertyChangeSupport().addPropertyChangeListener(bevh);
 		Cytoscape.getPropertyChangeSupport().addPropertyChangeListener(bevh);
 
 		networkPanel = new NetworkPanel(this);
 		networkPanel.setNavigator(bevh.getBirdsEyeView());
 
 		cyMenus = new CyMenus();
 
 		// Listener Setup
 		// ----------------------------------------
 		// |----------|
 		// | CyMenus  |
 		// |----------|
 		// |
 		// |
 		// |-----|      |---------|    |------|  |-------|
 		// | N P |------| Desktop |----| NVM  |--| Views |
 		// |-----|      |---------|    |------|  |-------|
 		// |
 		// |
 		// |-----------|
 		// | Cytoscape |
 		// |-----------|
 
 		// The CytoscapeDesktop listens to NETWORK_VIEW_CREATED events,
 		// and passes them on, The NetworkPanel listens for them
 		// The Desktop also keeps Cytoscape up2date, but NOT via events
 		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(this);
 
 		// The Networkviewmanager listens to the CytoscapeDesktop to know when
 		// to
 		// put new NetworkViews in the userspace and to get passed focus events
 		// from
 		// the NetworkPanel. The CytoscapeDesktop also listens to the NVM
 		this.getSwingPropertyChangeSupport().addPropertyChangeListener(networkViewManager);
 		networkViewManager.getSwingPropertyChangeSupport().addPropertyChangeListener(this);
 
 		// The NetworkPanel listens to the CytoscapeDesktop for
 		// NETWORK_CREATED_EVENTS a
 		// as well as for passing focused events from the Networkviewmanager.
 		// The
 		// CytoscapeDesktop also listens to the NetworkPanel
 		this.getSwingPropertyChangeSupport().addPropertyChangeListener(networkPanel);
 		networkPanel.getSwingPropertyChangeSupport().addPropertyChangeListener(this);
 
 		// add a listener for node bypass
 		Cytoscape.getSwingPropertyChangeSupport()
 		         .addPropertyChangeListener(new VizMapBypassNetworkListener());
 
 		// add a listener for nestedNetwork
 		Cytoscape.getSwingPropertyChangeSupport()
 		         .addPropertyChangeListener(new NestedNetworkListener());
 		
 		// Web Service Client context menu.
 		Cytoscape.getSwingPropertyChangeSupport()
 			.addPropertyChangeListener(new WebServiceContextMenuListener());
 
 		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(new HideNodeListener());
 		Cytoscape.getSwingPropertyChangeSupport().addPropertyChangeListener(new HideEdgeListener());
 
 		// initialize Menus
 		cyMenus.initializeMenus();
 
 		// create the CytoscapeDesktop
 		final BiModalJSplitPane masterPane = setupCytoPanels(networkPanel, networkViewManager);
 
 		// note - proper networkViewManager has been properly selected in
 		// setupCytoPanels()
 		main_panel.add(masterPane, BorderLayout.CENTER);
 		main_panel.add(cyMenus.getToolBar(), BorderLayout.NORTH);
 		
 		// Set the width of Cytopanel West
 		masterPane.setDividerLocation(DEF_DEVIDER_LOCATION);
 		
 		// Remove status bar.
 		initStatusBar(main_panel);
 		setJMenuBar(cyMenus.getMenuBar());
 
 		// Set up the VizMapper
 		//setupVizMapper();
 		getVizMapperUI();
 
 		// don't automatically close window. Let Cytoscape.exit(returnVal)
 		// handle this,
 		// based upon user confirmation.
 		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
 
 		addWindowListener(new WindowAdapter() {
 				public void windowClosing(WindowEvent we) {
 					Cytoscape.exit(returnVal);
 				}
 			});
 
 		setContentPane(main_panel);
 		pack();
 		setSize(DEF_DESKTOP_SIZE);
 		
 		// Set desktop location
 		setDesktopLocation();
 		
 		// show the Desktop
 		setVisible(true);
 		toFront();
 	}
 
 	private void setDesktopLocation(){
 		//restore desktop to previous location
 		try {	
 			Properties props = new Properties();
 			File desktop_prop_file = new File(CytoscapeInit.getConfigVersionDirectory(), "desktop.props");
 			props.load(new FileInputStream(desktop_prop_file));
 			this.setLocation(new Integer(props.get("x").toString()).intValue(), 
 					new Integer(props.get("y").toString()).intValue());
 			this.setSize(new Integer(props.get("w").toString()).intValue(), 
 					new Integer(props.get("h").toString()).intValue());
 		}
 		catch (Exception e){
 			setLocationRelativeTo(null);
 		}		
 	}
 	
 	private void initStatusBar(JPanel panel) {
 		statusBar = new JLabel();
 		statusBar.setBorder(new EmptyBorder(0, 7, 5, 7));
 		statusBar.setForeground(new Color(75, 75, 75));
 		panel.add(statusBar, BorderLayout.SOUTH);
 		setStatusBarMsg("Welcome to Cytoscape " + CytoscapeVersion.version
 		                + "              Right-click + drag  to  ZOOM             Middle-click + drag  to  PAN");
 	}
 
 	/**
 	 * Sets the Status Bar Message.
 	 *
 	 * @param msg
 	 *            Status Bar Message.
 	 */
 	public void setStatusBarMsg(String msg) {
 		statusBar.setText(msg);
 	}
 
 	/**
 	 * Clears the Status Bar Message.
 	 */
 	public void clearStatusBar() {
 		// By using mutiple white spaces, layout for the statusBar is preserved.
 		statusBar.setText("   ");
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public NetworkPanel getNetworkPanel() {
 		return networkPanel;
 	}
 
 	/**
 	 * @deprecated Will be removed April 2008. Use CyHelpBroker.getHelpBroker() instead.
 	 */
 	public HelpBroker getHelpBroker() {
 		return CyHelpBroker.getHelpBroker();
 	}
 
 	/**
 	 * @deprecated Will be removed April 2008. Use CyHelpBroker.getHelpSet() instead.
 	 */
 	public HelpSet getHelpSet() {
 		return CyHelpBroker.getHelpSet();
 	}
 
 	/**
 	 * Don't use this!
 	 *
 	 * @param edit An undoable edit.
 	 * @deprecated Use CyUndo.getUndoableEditSupport().postEdit(edit) instead. Will be removed March 2008.
 	 */
 	public void addEdit(javax.swing.undo.UndoableEdit edit) {
 		CyUndo.getUndoableEditSupport().postEdit(edit);
 	}
 
 	/**
 	 * Return the view type for this CytoscapeDesktop
 	 * @deprecated View type is no longer used, so just don't use this method. Will be gone August 2008.
 	 */
 	public int getViewType() {
 		return 1; // what was internal
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public CyMenus getCyMenus() {
 		return cyMenus;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param newNetwork DOCUMENT ME!
 	 */
 	public void setNewNetwork(CyNetwork newNetwork) {
 	}
 
 	/**
 	 *  Returns new vizmapper GUI.
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public VizMapperMainPanel getVizMapperUI() {
 		if (vizmapperUI == null) {
 			this.vmm = Cytoscape.getVisualMappingManager();
 			vizmapperUI = VizMapperMainPanel.getVizMapperUI();
 			getCytoPanel(SwingConstants.WEST).add("VizMapper\u2122", vizmapperUI);
 			this.getSwingPropertyChangeSupport().addPropertyChangeListener(vizmapperUI);
 			
 		}
 
 		return vizmapperUI;
 	}
 
 	// ----------------------------------------//
 	// Focus Management
 
 	/**
 	 * @param style
 	 *            the NEW VisualStyle
 	 * @return the OLD VisualStyle
 	 */
 	public VisualStyle setVisualStyle(VisualStyle style) {
 		//
 		//				VisualStyle old_style = (VisualStyle) vizMapUI.getStyleSelector().getToolbarComboBox()
 		//				                                              .getSelectedItem();
 		//		
 		vmm.setVisualStyle(style);
 
 		//vizMapUI.getStyleSelector().getToolbarComboBox().setSelectedItem(style);
 		return null;
 	}
 
 	protected void updateFocus(String network_id) {
 		final VisualStyle old_style = vmm.getVisualStyle();
 		final CyNetworkView old_view = Cytoscape.getCurrentNetworkView();
 
 		if (old_view != null) {
 			old_view.putClientData(VISUAL_STYLE, old_style);
 			old_view.putClientData(VIZMAP_ENABLED, new Boolean(old_view.getVisualMapperEnabled()));
 		}
 
 		// set the current Network/View
 		Cytoscape.setCurrentNetwork(network_id);
 
 		if (Cytoscape.setCurrentNetworkView(network_id)) {
 			// deal with the new Network
 			final CyNetworkView new_view = Cytoscape.getCurrentNetworkView();
 
 			//			VisualStyle new_style = (VisualStyle) new_view.getClientData(VISUAL_STYLE);
 			VisualStyle new_style = new_view.getVisualStyle();
 
 			Boolean vizmap_enabled = ((Boolean) new_view.getClientData(VIZMAP_ENABLED));
 
 			if (new_style == null)
 				new_style = vmm.getCalculatorCatalog().getVisualStyle("default");
 
 			if (vizmap_enabled == null)
 				vizmap_enabled = true;
 
 			vmm.setNetworkView(new_view);
 
 			if (new_style.getName().equals(old_style.getName()) == false) {
 				vmm.setVisualStyle(new_style);
 				
 				// Redraw Graph only when current network view's style is not equal to the selected style.
 				final CyNetworkView curView = Cytoscape.getCurrentNetworkView();
 				final VisualStyle curViewStyle = curView.getVisualStyle();
 				if (curView != Cytoscape.getNullNetworkView() && curViewStyle != null) {
 					if (new_style.getName().equals(curViewStyle.getName()) == false)
 						Cytoscape.getCurrentNetworkView().redrawGraph(false, true);
 				}
 			}
 		}
 	}
 
 	/**
 	 *  TODO: We should remove one of this event!
 	 *
 	 * @param network_id DOCUMENT ME!
 	 */
 	public void setFocus(String network_id) {
 		pcs.firePropertyChange(new PropertyChangeEvent(this, NETWORK_VIEW_FOCUSED, null, network_id));
 		pcs.firePropertyChange(new PropertyChangeEvent(this, NETWORK_VIEW_FOCUS, null, network_id));
 	}
 
 	/**
 	 * TO keep things clearer there is one GraphView Controller per
 	 * CytoscapeDesktop
 	 */
 	public GraphViewController getGraphViewController() {
 		if (graphViewController == null)
 			graphViewController = new GraphViewController();
 
 		return graphViewController;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public SwingPropertyChangeSupport getSwingPropertyChangeSupport() {
 		return pcs;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param e DOCUMENT ME!
 	 */
 	public void propertyChange(PropertyChangeEvent e) {
 		if (NETWORK_VIEW_CREATED.equals(e.getPropertyName())) {
 			//CyLogger.getLogger().info("CytoscapeDesktop got: NETWORK_VIEW_CREATED  " +
 			 //                   e.getSource().getClass().getName());
 			// add the new view to the GraphViewController
 			getGraphViewController().addGraphView((CyNetworkView) e.getNewValue());
 			// pass on the event
 			pcs.firePropertyChange(e);
 		} else if (NETWORK_VIEW_FOCUSED.equals(e.getPropertyName())) {
 			//CyLogger.getLogger().info("CytoscapeDesktop got: NETWORK_VIEW_FOCUSED " +
 			 //                   e.getSource().getClass().getName());
 			// get focus event from NetworkViewManager
 			updateFocus(e.getNewValue().toString());
 			pcs.firePropertyChange(e);
 		} else if (NETWORK_VIEW_FOCUS.equals(e.getPropertyName())) {
 			//CyLogger.getLogger().info("CytoscapeDesktop got: NETWORK_VIEW_FOCUS " +
 			 //                   e.getSource().getClass().getName());
 			// get Focus from NetworkPanel
 			updateFocus(e.getNewValue().toString());
 			pcs.firePropertyChange(e);
 		} else if (NETWORK_VIEWS_SELECTED.equals(e.getPropertyName())) {
 			//CyLogger.getLogger().info("CytoscapeDesktop got: NETWORK_VIEWS_SELECTED " +
 			 //                   e.getSource().getClass().getName());
 			Cytoscape.setSelectedNetworkViews( (List<String>)(e.getNewValue()) );
 			Cytoscape.setSelectedNetworks( (List<String>)(e.getNewValue()) );
 			pcs.firePropertyChange(e);
 		} else if (Cytoscape.NETWORK_CREATED.equals(e.getPropertyName())) {
 			//CyLogger.getLogger().info("CytoscapeDesktop got: NETWORK_CREATED " +
 			 //                   e.getSource().getClass().getName());
 			// fire the event so that the NetworkPanel can catch it
 			pcs.firePropertyChange(e);
 		} else if (Cytoscape.NETWORK_DESTROYED.equals(e.getPropertyName())) {
 			//CyLogger.getLogger().info("CytoscapeDesktop got: NETWORK_DESTROYED " +
 			 //                   e.getSource().getClass().getName());
 			// fire the event so that the NetworkPanel can catch it
 			pcs.firePropertyChange(e);
 
 			// Check new session or not
 			if ((Cytoscape.getNetworkSet().size() == 0)
 			    && (Cytoscape.getSessionstate() != Cytoscape.SESSION_OPENED)) {
 				String message = "Do you want to create a new session?.\n(All attributes will be lost!)";
 
 				int result = JOptionPane.showConfirmDialog(this, message, "Create New Session?",
 				                                           JOptionPane.YES_NO_OPTION,
 				                                           JOptionPane.WARNING_MESSAGE, null);
 
 				if (result == JOptionPane.YES_OPTION)
 					Cytoscape.createNewSession();
 			}
 		} else if (NETWORK_VIEW_DESTROYED.equals(e.getPropertyName())) {
 			//CyLogger.getLogger().info("CytoscapeDesktop got: NETWORK_VIEW_DESTROYED " +
 			 //                  e.getSource().getClass().getName());
 			// remove the view from the GraphViewController
 			getGraphViewController().removeGraphView((CyNetworkView) e.getNewValue());
 			// pass on the event
 			pcs.firePropertyChange(e);
 		}
 		else if (e.getPropertyName().equalsIgnoreCase(Cytoscape.CYTOSCAPE_EXIT)){
 			// save Desktop location into property file "desktop.prop"
 			File desktop_prop_file = new File(CytoscapeInit.getConfigVersionDirectory(), "desktop.props");
 			Properties props = new Properties();
 			props.setProperty("x", new Integer(this.getX()).toString());
 			props.setProperty("y", new Integer(this.getY()).toString());
 			props.setProperty("w", new Integer(this.getWidth()).toString());
 			props.setProperty("h", new Integer(this.getHeight()).toString());
 			try {
 				props.store(new FileOutputStream(desktop_prop_file), "Remember desktop frame location");
 			}
 			catch (IOException ioe){
 			}

			try {
				Cytoscape.getRecentlyOpenedSessionTracker().writeOut();
			} catch (final IOException ioe) {
				System.err.println("failed to save recent session URLs!");
			}
 		}
 	}
 
 	// ---------------------------------------------------------------------------//
 	// Cytopanels - Public and Protected methods
 
 	/**
 	 * Gets a cytoPanel given a Compass direction.
 	 *
 	 * @param compassDirection
 	 *            Compass Direction (SwingConstants.{SOUTH,EAST,WEST}).
 	 * @return CytoPanel The CytoPanel that lives in the region specified by
 	 *         compass direction.
 	 */
 	public CytoPanel getCytoPanel(int compassDirection) {
 		// return appropriate cytoPanel based on compass direction
 		switch (compassDirection) {
 			case SwingConstants.SOUTH:
 				return (CytoPanel) cytoPanelSouth;
 
 			case SwingConstants.EAST:
 				return (CytoPanel) cytoPanelEast;
 
 			case SwingConstants.WEST:
 				return (CytoPanel) cytoPanelWest;
 
 			case SwingConstants.SOUTH_WEST:
 				return (CytoPanel) cytoPanelSouthWest;
 		}
 
 		// houston we have a problem
 		throw new IllegalArgumentException("Illegal Argument:  " + compassDirection
 		                                   + ".  Must be one of:  SwingConstants.{SOUTH,EAST,WEST,SOUTH_WEST}.");
 	}
 
 	/**
 	 * Create the CytoPanels UI.
 	 *
 	 * @param networkPanel
 	 *            to load on left side of right bimodal.
 	 * @param networkViewManager
 	 *            to load on left side (CytoPanel West).
 	 * @return BiModalJSplitPane Object.
 	 */
 	protected BiModalJSplitPane setupCytoPanels(NetworkPanel networkPanel,
 	                                            NetworkViewManager networkViewManager) {
 		// bimodals that our Cytopanels Live within
 		final BiModalJSplitPane topRightPane = createTopRightPane(networkViewManager);
 		final BiModalJSplitPane rightPane = createRightPane(topRightPane);
 		final BiModalJSplitPane masterPane = createMasterPane(networkPanel, rightPane);
 		createBottomLeft();
 
 		return masterPane;
 	}
 
 	protected void createBottomLeft() {
 		cytoPanelSouthWest = new CytoPanelImp(SwingConstants.SOUTH_WEST, JTabbedPane.TOP,
 	                                                             CytoPanelState.HIDE);
 
 		BiModalJSplitPane split = new BiModalJSplitPane(this, JSplitPane.VERTICAL_SPLIT,
 		                              BiModalJSplitPane.MODE_HIDE_SPLIT, new JPanel(),
 		                              cytoPanelSouthWest);
 		split.setResizeWeight(0);
 		cytoPanelSouthWest.setCytoPanelContainer(split);
 		cytoPanelSouthWest.setMinimumSize(new Dimension(180, 230));
 		cytoPanelSouthWest.setMaximumSize(new Dimension(180, 230));
 		cytoPanelSouthWest.setPreferredSize(new Dimension(180, 230));
 
 		new ToolCytoPanelListener( split, (CytoPanelImp)cytoPanelWest, cytoPanelSouthWest );
 	}
 
 	/**
 	 * Creates the TopRight Pane.
 	 *
 	 * @param networkViewManager
 	 *            to load on left side of top right bimodal.
 	 * @return BiModalJSplitPane Object.
 	 */
 	protected BiModalJSplitPane createTopRightPane(NetworkViewManager networkViewManager) {
 		// create cytopanel with tabs along the top
 		cytoPanelEast = new CytoPanelImp(SwingConstants.EAST, JTabbedPane.TOP, CytoPanelState.HIDE);
 
 		// determine proper network view manager component
 		Component networkViewComp = null;
 
 		networkViewComp = (Component) networkViewManager.getDesktopPane();
 
 		// create the split pane - we show this on startup
 		BiModalJSplitPane splitPane = new BiModalJSplitPane(this, JSplitPane.HORIZONTAL_SPLIT,
 		                                                    BiModalJSplitPane.MODE_HIDE_SPLIT,
 		                                                    networkViewComp, cytoPanelEast);
 
 		// set the cytopanelcontainer
 		cytoPanelEast.setCytoPanelContainer(splitPane);
 
 		// set the resize weight - left component gets extra space
 		splitPane.setResizeWeight(1.0);
 
 		// outta here
 		return splitPane;
 	}
 
 	/**
 	 * Creates the Right Panel.
 	 *
 	 * @param topRightPane
 	 *            TopRightPane Object.
 	 * @return BiModalJSplitPane Object
 	 */
 	protected BiModalJSplitPane createRightPane(BiModalJSplitPane topRightPane) {
 		// create cytopanel with tabs along the bottom
 		cytoPanelSouth = new CytoPanelImp(SwingConstants.SOUTH, JTabbedPane.BOTTOM,
 		                                  CytoPanelState.HIDE);
 
 		// create the split pane - hidden by default
 		final BiModalJSplitPane splitPane = new BiModalJSplitPane(this, JSplitPane.VERTICAL_SPLIT,
 		                                                    BiModalJSplitPane.MODE_HIDE_SPLIT,
 		                                                    topRightPane, cytoPanelSouth);
 		splitPane.setDividerLocation(DEF_DATAPANEL_DEVIDER_LOCATION);
 
 		// set the cytopanel container
 		cytoPanelSouth.setCytoPanelContainer(splitPane);
 
 		// set resize weight - top component gets all the extra space.
 		splitPane.setResizeWeight(1.0);
 
 		// outta here
 		return splitPane;
 	}
 
 	/**
 	 * Creates the Master Split Pane.
 	 *
 	 * @param networkPanel
 	 *            to load on left side of CytoPanel (cytoPanelWest).
 	 * @param rightPane
 	 *            BiModalJSplitPane Object.
 	 * @return BiModalJSplitPane Object.
 	 */
 	protected BiModalJSplitPane createMasterPane(NetworkPanel networkPanel,
 	                                             BiModalJSplitPane rightPane) {
 		// create cytopanel with tabs along the top
 		cytoPanelWest = new CytoPanelImp(SwingConstants.WEST, JTabbedPane.TOP, CytoPanelState.DOCK);
 
 		// add the network panel to our tab
 		String tab1Name = new String("Network");
 		cytoPanelWest.add(tab1Name, new ImageIcon(Cytoscape.class.getResource("images/class_hi.gif")),
 		                  networkPanel, "Cytoscape Network List");
 
 		// create the split pane - hidden by default
 		BiModalJSplitPane splitPane = new BiModalJSplitPane(this, JSplitPane.HORIZONTAL_SPLIT,
 		                                                    BiModalJSplitPane.MODE_SHOW_SPLIT,
 		                                                    cytoPanelWest, rightPane);
 
 		// set the cytopanel container
 		cytoPanelWest.setCytoPanelContainer(splitPane);
 
 		// outta here
 		return splitPane;
 	}
 
 	// End Cytopanels - Public and Protected methods
 	// ---------------------------------------------------------------------------//
 
 	/**
 	 * Gets the NetworkView Manager.
 	 *
 	 * @return NetworkViewManager Object.
 	 */
 	public NetworkViewManager getNetworkViewManager() {
 		return this.networkViewManager;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 */
 	public BirdsEyeViewHandler getBirdsEyeViewHandler() {
 		if(bevh == null) {
 			bevh = new BirdsEyeViewHandler(networkViewManager.getDesktopPane());
 		}
 		
 		return bevh;
 	}
 
 	/**
 	 *  DOCUMENT ME!
 	 *
 	 * @param vt DOCUMENT ME!
 	 *
 	 * @return  DOCUMENT ME!
 	 * @deprecated View types are no longer support so stop using this.  Will be removed August 2008.
 	 */
 	public static int parseViewType(String vt) {
 		return 1; // What was internal
 	}
 }
