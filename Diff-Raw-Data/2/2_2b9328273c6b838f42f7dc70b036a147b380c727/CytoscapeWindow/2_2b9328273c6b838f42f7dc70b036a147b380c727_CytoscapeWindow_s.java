 // CytoscapeWindow.java:  a yfiles, GUI tool for exploring genetic networks
 //------------------------------------------------------------------------------
 // $Revision$
 // $Date$
 // $Author$
 //------------------------------------------------------------------------------
 package cytoscape;
 //------------------------------------------------------------------------------
 import java.awt.*;
 import java.awt.geom.*;
 import java.awt.event.*;
 import javax.swing.*;
 import javax.swing.event.*;
 import javax.swing.JOptionPane;
 
 import java.io.*;
 import java.util.*;
 import java.util.logging.*;
 
 import y.base.*;
 import y.view.*;
 
 import y.layout.Layouter;
 import y.layout.GraphLayout;
 
 import y.layout.circular.CircularLayouter;
 import y.layout.hierarchic.*;
 import y.layout.organic.OrganicLayouter;
 import y.layout.random.RandomLayouter;
 
 import y.io.YGFIOHandler;
 import y.io.GMLIOHandler;
 
 import y.algo.GraphHider;
 
  // printing
 import java.awt.print.PageFormat;
 import java.awt.print.PrinterException;
 import java.awt.print.Printable;
 import java.awt.print.PrinterJob;
 import javax.print.attribute.*;
 
 import y.option.OptionHandler; 
 import y.view.Graph2DPrinter;
 
 
 import cytoscape.data.*;
 import cytoscape.data.annotation.*;
 import cytoscape.data.readers.*;
 import cytoscape.data.servers.*;
 import cytoscape.dialogs.*;
 import cytoscape.browsers.*;
 import cytoscape.layout.*;
 import cytoscape.vizmap.*;
 import cytoscape.view.*;
 import cytoscape.undo.*;
 import cytoscape.util.MutableString;
 import cytoscape.util.MutableBool;
 
 import cytoscape.filters.*;
 import cytoscape.filters.dialogs.*;
 
 import java.util.Iterator;
 import java.util.Set;
 import java.util.Map;
 import java.util.HashMap;
 //-----------------------------------------------------------------------------------
 public class CytoscapeWindow extends JPanel implements FilterDialogClient, Graph2DSelectionListener {
 
   protected static final int DEFAULT_WIDTH = 700;
   protected static final int DEFAULT_HEIGHT = 700;
 
   protected cytoscape parentApp;
   protected Graph2D graph;
   protected String geometryFilename;
   protected String expressionDataFilename;
   protected Logger logger;
 
   protected JFrame mainFrame;
   protected JMenuBar menuBar;
   protected JMenu opsMenu, vizMenu, selectSubmenu, layoutMenu;
   protected JToolBar toolbar;
   protected JLabel infoLabel;
 
   protected Cursor defaultCursor = Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR);
   protected Cursor busyCursor = Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR);
 
   protected Layouter layouter;
 
   protected Graph2DView graphView;
 
   protected ViewMode editGraphMode  = new EditGraphMode ();
   protected ViewMode readOnlyGraphMode = new ReadOnlyGraphMode ();
   protected ViewMode currentGraphMode = readOnlyGraphMode;
   protected ViewMode nodeAttributesPopupMode = new NodeBrowsingMode ();
   protected ViewMode currentPopupMode = nodeAttributesPopupMode;
   protected boolean viewModesInstalled = false;
 
   protected BioDataServer bioDataServer;
   protected String bioDataServerName;
 
   protected GraphObjAttributes nodeAttributes = new GraphObjAttributes ();
   protected GraphObjAttributes edgeAttributes = new GraphObjAttributes ();
 
   protected AttributeMapper vizMapper;
   protected VizMapperCategories vizMapperCategories;
 
   protected ExpressionData expressionData = null;
 
   protected final String goModeMenuLabel = "Show GeneOntology Info";
   protected final String expressionModeMenuLabel = "Show mRNA Expression";
 
     // added by dramage 2002-08-21
     protected CytoscapeUndoManager undoManager;
     protected JMenuItem undoMenuItem, redoMenuItem;
 
   // protected VizChooser theVizChooser = new VizChooser();
 
   protected UndoableGraphHider graphHider;
   protected Vector subwindows = new Vector ();
 
   protected String windowTitle;
   protected File currentDirectory;
    // selected nodes can be displayed in a new window.  this next variable
    // provides a title for that new window
   protected String titleForCurrentSelection = null;
   protected CytoscapeConfig config;
 
   protected JMenuItem deleteSelectionMenuItem;
 //------------------------------------------------------------------------------
 public CytoscapeWindow (cytoscape parentApp,
                         CytoscapeConfig config,
                         Logger logger,
                         Graph2D graph, 
                         ExpressionData expressionData,
                         BioDataServer bioDataServer,
                         GraphObjAttributes nodeAttributes,
                         GraphObjAttributes edgeAttributes,
                         String geometryFilename,
                         String expressionDataFilename,
                         String title,
                         boolean doFreshLayout)
    throws Exception
 {
   // System.out.println ("--- constructing CytoscapeWindow in cstest0");
   this.parentApp = parentApp;
   this.logger = logger;
   this.geometryFilename = geometryFilename;
   this.expressionDataFilename = expressionDataFilename;
   this.bioDataServer = bioDataServer;
   this.expressionData = expressionData;
   this.config = config;
 
   // setupLogging ();
 
   if (nodeAttributes != null) 
     this.nodeAttributes = nodeAttributes;
 
   if (edgeAttributes != null)
     this.edgeAttributes = edgeAttributes;
 
   this.currentDirectory = new File (System.getProperty ("user.dir"));
 
   vizMapperCategories = new VizMapperCategories();
   vizMapper = new AttributeMapper( vizMapperCategories.getInitialDefaults() );
   AttributeMapperPropertiesAdapter adapter =
       new AttributeMapperPropertiesAdapter(vizMapper, vizMapperCategories);
   adapter.applyAllRangeProperties( config.getProperties() );
 
   if (title == null)
     this.windowTitle = "";
   else
     this.windowTitle = title;
 
 
   initializeWidgets ();
   JButton annotationButton = toolbar.add (new AnnotationGui (CytoscapeWindow.this));
 
   setGraph (graph);
   annotationButton.setIcon (new ImageIcon (getClass().getResource("images/AnnotationGui.gif")));
   annotationButton.setToolTipText ("add annotation to nodes");
   annotationButton.setBorderPainted (false);
 
   assignSpeciesAttributeToAllNodes ();
   displayCommonNodeNames ();
   displayNewGraph (doFreshLayout);
 
   mainFrame.addWindowListener (parentApp);
   mainFrame.setVisible (true);
 
 
   // load plugins last, after the main window is setup, since they
   // will often need access to all of the parts of a fully
   // instantiated CytoscapeWindow
 
   loadPlugins ();
   setPopupMode (new NodeBrowsingMode ());
 
 } // ctor
 //------------------------------------------------------------------------------
 /**
  * configure logging:  cytoscape.props specifies what level of logging
  * messages are written to the console; by default, only SEVERE messages
  * are written.  in time, more control of logging (i.e., optional logging
  * to a file, disabling console logging, per-plugin logging) will be provided
  */
 protected void setupLogging ()
 {
   logger = Logger.getLogger ("global"); 
   Properties properties = getConfiguration().getProperties();
   String level = properties.getProperty ("logging", "SEVERE");
 
   if (level.equalsIgnoreCase ("severe"))
     logger.setLevel (Level.SEVERE);
   else if (level.equalsIgnoreCase ("warning"))
     logger.setLevel (Level.WARNING);
   else if (level.equalsIgnoreCase ("info"))
     logger.setLevel (Level.INFO);
   else if (level.equalsIgnoreCase ("config"))
     logger.setLevel (Level.CONFIG);
 
 } // setupLogging
 
 //------------------------------------------------------------------------------
 public BioDataServer getBioDataServer ()
 {
  return this.bioDataServer;
 
 } // getBioDataServer 
 //------------------------------------------------------------------------------
 public Logger getLogger ()
 {
   return logger;
 }
 
 
 /**
  * Uses the plugin loader to load the appropriate plugins.  Ensures
  * that the operations menu contains only relevant items.
  *
  * added by dramage 2002-08-21
  */
 public void loadPlugins () {
 
     // clear the operations menu
     opsMenu.removeAll();
 
     // load plugins
     PluginLoader pluginLoader
         = new PluginLoader (this, config, nodeAttributes, edgeAttributes);
 
     pluginLoader.load ();
     logger.info (pluginLoader.getMessages ());
 
     // add default unselectable "no plugins loaded" if none loaded
     if (opsMenu.getItemCount() == 0) {
         JMenuItem none = new JMenuItem("No plugins loaded");
         none.setEnabled(false);
         opsMenu.add(none);
     }
 }
 
 
 //------------------------------------------------------------------------------
 public void windowStateChanged (WindowEvent e)
 {
   logger.info ("--- windowStateChanged: " + e);
 }
 //------------------------------------------------------------------------------
 public CytoscapeConfig getConfiguration() {
     return config;
 }
 
 
 /**
  * Returns the window's current UndoManager.
  *
  * added by dramage 2002-08-22
  */
 public CytoscapeUndoManager getUndoManager() {
     return undoManager;
 }
 
 
 //------------------------------------------------------------------------------
 public Graph2D getGraph ()
 {  
   return graph;
 }
 //------------------------------------------------------------------------------
 public GraphProps getProps () 
 {
     return new GraphProps(graph, nodeAttributes, edgeAttributes);
 }
 //------------------------------------------------------------------------------
 public File getCurrentDirectory() {
     return currentDirectory;
 }
 //------------------------------------------------------------------------------
 public void setCurrentDirectory(File dir) {
     currentDirectory = dir;
 }
 //------------------------------------------------------------------------------
 public void setGraph (Graph2D graph) 
 {
   if (this.graph != null) {
    this.graph.removeGraph2DSelectionListener(this);
    if (undoManager != null)
      this.graph.removeGraphListener(undoManager);
    }
 
   this.graph = graph;
   graph.addGraph2DSelectionListener(this);
   undoManager = new CytoscapeUndoManager(this, graph);
   graph.addGraphListener(undoManager);
   updateUndoRedoMenuItemStatus();
   graphHider = new UndoableGraphHider (graph, undoManager);
     
   setLayouterAndGraphView();
   // displayCommonNodeNames ();
   
 } // setGraph
 //-----------------------------------------------------------------------------
 public Graph2DView getGraphView(){
     return graphView;
 }
 //------------------------------------------------------------------------------
 public UndoableGraphHider getGraphHider ()
 {  
   return graphHider;
 }
 //------------------------------------------------------------------------------
 public void redrawGraph() {
   redrawGraph(false);
 }
 //------------------------------------------------------------------------------
 public void redrawGraph (boolean doLayout)
 {
   applyVizmapSettings();
   if (doLayout) {
     applyLayout(false);
   }
   graphView.updateView(); //forces the view to update it's contents
   /* paintImmediately() is needed because sometimes updates can be buffered */
   graphView.paintImmediately(0,0,graphView.getWidth(),graphView.getHeight());
   updateStatusText();
 
 } // redrawGraph
 
 
 public void updateStatusText () {
     updateStatusText(0,0);
 }
 
 /**
  * Resets the info label status bar text with the current number of
  * nodes, edges, selected nodes, and selected edges.
  *
  * The Adjust fields is an ugly hack that is necessary because of a
  * yFiles API quirk.  See selectionStateChanged() for details.
  *
  * added by dramage 2002-08-16
  */
 public void updateStatusText (int nodeAdjust, int edgeAdjust) {
     int nodeCount = graph.nodeCount();
     int selectedNodes = graph.selectedNodes().size() + nodeAdjust;
     
     int edgeCount = graph.edgeCount();
     int selectedEdges = graph.selectedEdges().size() + edgeAdjust;
     infoLabel.setText ("  Nodes: " + nodeCount
                        + " ("+selectedNodes+" selected)"
                        + " Edges: " + edgeCount
                        + " ("+selectedEdges+" selected)");
 }
 
 /**
  * This function is called as part of the Graph2DSelectionListener
  * interface.  When the selection status of the graph changes, this
  * function calls updateStatusText to reflect the change.
  *
  * There is a quirk with yFiles that causes this function to be called
  * *just before* the selection/deselection actually occurs.  That
  * means we must adjust the status text appropriately to reflect the
  * coming change.
  */
 public void selectionStateChanged(Graph2DSelectionEvent e) {
     if (e.isEdgeSelection()) {
         updateStatusText(0,
                          (graph.isSelected((Edge)e.getSubject()) ? -1 : +1));
     } else if (e.isNodeSelection()) {
         updateStatusText((graph.isSelected((Node)e.getSubject()) ? -1 : +1),
                          0);
     }
 }
 
 
 //------------------------------------------------------------------------------
 public JFrame getMainFrame ()
 {
   return mainFrame;
 }
 //------------------------------------------------------------------------------
 public String getWindowTitle() {
   return windowTitle;
 }
 //------------------------------------------------------------------------------
 public void setWindowTitle(String newTitle) {
     windowTitle = newTitle;
     mainFrame.setTitle(windowTitle);
 }
 //------------------------------------------------------------------------------
 public JMenuBar getMenuBar ()
 {
   return menuBar;
 }
 //------------------------------------------------------------------------------
 public void setMenuBar (JMenuBar newMenuBar)
 {
   mainFrame.setJMenuBar (newMenuBar);
 }
 //------------------------------------------------------------------------------
 public JToolBar getToolBar ()
 {
   return toolbar;
 }
 //------------------------------------------------------------------------------
 public ExpressionData getExpressionData ()
 {
   return expressionData;
 }
 //------------------------------------------------------------------------------
 public void setExpressionData(ExpressionData expData, String expressionDataFilename){
     this.expressionData = expData;
     this.expressionDataFilename = expressionDataFilename;
 }
 //------------------------------------------------------------------------------
 public AttributeMapper getVizMapper ()
 {
   return vizMapper;
 }
 //------------------------------------------------------------------------------
 public VizMapperCategories getVizMapperCategories ()
 {
   return vizMapperCategories;
 }
 //------------------------------------------------------------------------------
 protected void initializeWidgets ()
 {
   setLayout (new BorderLayout ());  
   graphView = new Graph2DView ();
 
   // added owo 2002.04.18
   DefaultBackgroundRenderer renderer = new DefaultBackgroundRenderer (graphView);
   graphView.setBackgroundRenderer (renderer);
 
   add (graphView, BorderLayout.CENTER);
   graphView.setPreferredSize (new Dimension (DEFAULT_WIDTH, DEFAULT_HEIGHT));
 
   toolbar = createToolBar ();
   add (toolbar, BorderLayout.NORTH);
 
   infoLabel = new JLabel ();
   add (infoLabel, BorderLayout.SOUTH);
 
   mainFrame = new JFrame (windowTitle);
     
   mainFrame.setJMenuBar (createMenuBar ());
   mainFrame.setContentPane (this);
   mainFrame.pack ();
   setInteractivity (true);
 
 } // initializeWidgets
 //------------------------------------------------------------------------------
 public String getCanonicalNodeName (Node node)
 {
   return nodeAttributes.getCanonicalName (node);
 
 } // getCanonicalNodeName
 //------------------------------------------------------------------------------
 protected void assignSpeciesAttributeToAllNodes ()
 {
   Node [] nodes = graphView.getGraph2D().getNodeArray();
   for (int i=0; i < nodes.length; i++)
     nodeAttributes.set ("species", 
                         nodeAttributes.getCanonicalName (nodes [i]), 
                         getSpecies (nodes [i]));
 
 } // assignSpeciesAttributeToAllNodes
 //------------------------------------------------------------------------------
 public String getDefaultSpecies ()
 {
   String species = config.getDefaultSpeciesName ();
   if (species != null)
     return species;
 
   species = getConfiguration().getProperties().getProperty ("species", "unknown");
   return species;
 
 } // getSpecies
 //------------------------------------------------------------------------------
 public String getSpecies (Node node)
 {
   String species = nodeAttributes.getStringValue ("species", 
                                                   nodeAttributes.getCanonicalName (node));
   if (species != null)
     return species;
 
   return getDefaultSpecies ();
 
 } // getSpecies
 //------------------------------------------------------------------------------
 public String [] getAllSpecies ()
 {
   Vector list = new Vector ();
   Node [] nodes = graphView.getGraph2D().getNodeArray();
   for (int i=0; i < nodes.length; i++) {
     String species = getSpecies (nodes [i]);
     if (!list.contains (species) && species != null)
       list.add (species);
     } // for i
 
   return (String []) list.toArray (new String [0]);
 
 } // getAllSpecies
 //------------------------------------------------------------------------------
 protected void displayNewGraph (boolean doLayout)
 {
   if (graph == null)
       setGraph(new Graph2D ());
 
   String defaultLayoutStrategy = config.getDefaultLayoutStrategy ();
   if (defaultLayoutStrategy.equals ("hierarchical"))
     layouter = new HierarchicLayouter ();
   else if (defaultLayoutStrategy.equals ("circular"))
     layouter = new CircularLayouter ();
   else if (defaultLayoutStrategy.equals ("embedded"))
     layouter = new EmbeddedLayouter();
   else if (defaultLayoutStrategy.equals ("organic")) {
     OrganicLayouter ol = new OrganicLayouter ();
     ol.setActivateDeterministicMode (true);
     ol.setPreferredEdgeLength (80);
     layouter = ol;
     }
 
   graphView.setGraph2D (graph);
 
   this.redrawGraph(doLayout);
 
   graphView.fitContent ();
   graphView.setZoom (graphView.getZoom ()*0.9);
 
 } // displayGraph
 //------------------------------------------------------------------------------
 protected void setLayouterAndGraphView ()
 {
   if (graph == null)
    setGraph (new Graph2D ());
     
   String defaultLayoutStrategy = config.getDefaultLayoutStrategy ();
   if (defaultLayoutStrategy.equals ("hierarchical"))
     layouter = new HierarchicLayouter ();
   else if (defaultLayoutStrategy.equals ("circular"))
     layouter = new CircularLayouter ();
   else if (defaultLayoutStrategy.equals ("embedded"))
     layouter = new EmbeddedLayouter();
   else if (defaultLayoutStrategy.equals ("organic")) {
     OrganicLayouter ol = new OrganicLayouter ();
     ol.setActivateDeterministicMode (true);
     ol.setPreferredEdgeLength (80);
     layouter = ol;
     }
 
   graphView.setGraph2D (graph);
     
   graphView.fitContent ();
   graphView.setZoom (graphView.getZoom ()*0.9); 
 
 }
 //------------------------------------------------------------------------------
 protected void applyVizmapSettings ()
 {
   Node [] nodes = graphView.getGraph2D().getNodeArray();
 
   Color bgColor =
       vizMapperCategories.getBGColor(vizMapper);
   if(bgColor != null) {
       //graphView.setBackground(bgColor);
       DefaultBackgroundRenderer bgr = (DefaultBackgroundRenderer)graphView.getBackgroundRenderer();
       bgr.setColor(bgColor);
       //CytoscapeWindow.this.setBackground(bgColor);
   }
  
   boolean setTh = false;
   for (int i=0; i < nodes.length; i++) {
     Node node = nodes [i];
     String canonicalName = nodeAttributes.getCanonicalName (node);
     HashMap bundle = nodeAttributes.getAttributes (canonicalName);
     Color nodeColor =
         vizMapperCategories.getNodeFillColor(bundle, vizMapper);
     Color nodeBorderColor =
         vizMapperCategories.getNodeBorderColor(bundle, vizMapper);
     LineType nodeBorderLinetype = 
         vizMapperCategories.getNodeBorderLineType(bundle,vizMapper);
     double nodeHeight =
         vizMapperCategories.getNodeHeight(bundle, vizMapper);
     double nodeWidth =
         vizMapperCategories.getNodeWidth(bundle, vizMapper);
     byte nodeShape =
         vizMapperCategories.getNodeShape(bundle, vizMapper);
     
     NodeRealizer nr = graphView.getGraph2D().getRealizer(node);
     nr.setFillColor (nodeColor);
     nr.setLineColor(nodeBorderColor);
     nr.setLineType(nodeBorderLinetype);
     nr.setHeight(nodeHeight);
     nr.setWidth(nodeWidth);
     if (nr instanceof ShapeNodeRealizer) {
         ShapeNodeRealizer snr = (ShapeNodeRealizer)nr;
         snr.setShapeType(nodeShape);
     }
     // System.out.println(canonicalName + " " + nodeColor + " " + nodeBorderColor + " " + nodeBorderLinetype + " " + nodeHeight + " " 
     //	       + nodeShape);
     //System.out.flush();
   } // for i
 
   EdgeCursor cursor = graphView.getGraph2D().edges();
   cursor.toFirst ();
 
   for (int i=0; i < cursor.size (); i++) {
     Edge edge = cursor.edge ();
     String canonicalName = edgeAttributes.getCanonicalName (edge);
     HashMap bundle = edgeAttributes.getAttributes (canonicalName);
     Color color = vizMapperCategories.getEdgeColor(bundle, vizMapper);
     LineType line = vizMapperCategories.getEdgeLineType(bundle, vizMapper);
     Arrow sourceArrow =
       vizMapperCategories.getEdgeSourceDecoration(bundle, vizMapper);
     Arrow targetArrow =
       vizMapperCategories.getEdgeTargetDecoration(bundle, vizMapper);
     EdgeRealizer er = graphView.getGraph2D().getRealizer(edge);
     er.setLineColor (color);
     er.setLineType(line);
     er.setSourceArrow(sourceArrow);
     er.setTargetArrow(targetArrow);
     cursor.cyclicNext ();
   } // for i
 
 } // applyVizmapSettings
 //------------------------------------------------------------------------------
 public Node getNode (String canonicalNodeName)
 {
   Node [] nodes = graphView.getGraph2D().getNodeArray();
   for (int i=0; i < nodes.length; i++) {
     Node node = nodes [i];
     String canonicalName = nodeAttributes.getCanonicalName (node);
     logger.warning (" -- checking " + canonicalNodeName + " against " + canonicalName + " " + node);
     if (canonicalNodeName.equals (canonicalName)) 
       return node;
     }
 
   return null;
   
 } // getNode
 //------------------------------------------------------------------------------
 public GraphObjAttributes getNodeAttributes ()
 {
   return nodeAttributes;
 }
 //------------------------------------------------------------------------------
 public void setNodeAttributes (GraphObjAttributes newValue)
 {
   nodeAttributes = newValue;
   nodeAttributes.clearNameMap ();
   Node [] nodes = graphView.getGraph2D().getNodeArray();
 
   for (int i=0; i < nodes.length; i++) {
     String canonicalName = nodes [i].toString ();
     nodeAttributes.addNameMapping (canonicalName, nodes [i]);
     }
 
   displayCommonNodeNames ();
   
 }
 //------------------------------------------------------------------------------
 public void setNodeAttributes(GraphObjAttributes newValue, boolean skipAddNameMapping)
 {
   nodeAttributes = newValue;
   if (!skipAddNameMapping) {
     nodeAttributes.clearNameMap ();
     Node [] nodes = graphView.getGraph2D().getNodeArray();
     for (int i=0; i < nodes.length; i++) {
       String canonicalName = nodes [i].toString ();
       System.out.println ("-- adding name mapping for node " + nodes [i] + "   canonical: " + canonicalName);
       nodeAttributes.addNameMapping (canonicalName, nodes [i]);
       }
     }
 
   displayCommonNodeNames ();
   
 }
 //------------------------------------------------------------------------------
 public GraphObjAttributes getEdgeAttributes ()
 {
   return edgeAttributes;
 }
 //------------------------------------------------------------------------------
 public void setEdgeAttributes(GraphObjAttributes edgeAttributes){
     this.edgeAttributes = edgeAttributes;
 }
 //------------------------------------------------------------------------------
 /**
  * 
  */
 public void displayCommonNodeNames()
 {
     // common names may have been already loaded using
     // an attributes file
     // if bioDataServer is null, shouldn't we make sure common names do not
     // exist before returning?
     // -iliana 10/21/2002
     
     if (bioDataServer == null){ 
 	System.out.println("\nCytoscapeWindow: the bioDataServer is down");
 	System.out.flush();
 	return;
     }
 
   Node [] nodes = graph.getNodeArray ();
 
   for (int i=0; i < nodes.length; i++) {
     Node node = nodes [i];
     NodeRealizer r = graphView.getGraph2D().getRealizer(node);
     String newName = r.getLabelText ();  // we hope to replace this 
     String canonicalName = getCanonicalNodeName (node);
     try {
       String [] synonyms = bioDataServer.getAllCommonNames (getDefaultSpecies (), canonicalName);
       if (synonyms.length > 0) {
 	  newName = synonyms [0];
       }
       // added by iliana 11.5.2002
       // the label of the node may have no text
       if(newName == null || newName.length() == 0){
 	  newName = canonicalName;
       }
       
       nodeAttributes.set ("commonName", canonicalName, newName);
       // System.out.println ("setting node attribute commonName for " + canonicalName + " to " + newName);
       r.setLabelText (newName);
       }
     catch (Exception ignoreForNow) {;}
     } // for i
 
 } // displayCommonNodeNames
 //------------------------------------------------------------------------------
 /**
  *  displayNodeLabels()
  *  attempts to display the hashed graphObjAttribute value
  *  at key "key" as the label on every node.  Special case
  *  if the key is "canonicalName": canonicalName is stored
  *  in a different data structure, so we access it differently.
  */
 public void displayNodeLabels (String key)
 {
     Node [] nodes = graph.getNodeArray ();
 
     for (int i=0; i < nodes.length; i++) {
         Node node = nodes [i];
         String canonicalName = getCanonicalNodeName(node);
         String newName = "";
         if(!(key.equals("canonicalName"))) {
             if (nodeAttributes.getClass (key) == "string".getClass ())
                newName = nodeAttributes.getStringValue (key, canonicalName);
             else {
               HashMap attribmap = nodeAttributes.getAttributes(canonicalName);
               Object newObjectWithName  = (Object)attribmap.get(key);
               if(newObjectWithName != null)
                 newName = newObjectWithName.toString();
               }
         } // if key is not canonicalName
         else
             newName = canonicalName;
         NodeRealizer r = graphView.getGraph2D().getRealizer(node);
         r.setLabelText (newName);
     } // for i
     
 } // displayNodeLabels
 //------------------------------------------------------------------------------
 public JMenu getOperationsMenu ()
 {
   return opsMenu;
 }
 //------------------------------------------------------------------------------
 public JMenu getVizMenu ()
 {
   return vizMenu;
 }
 //------------------------------------------------------------------------------
 public JMenu getSelectMenu ()
 {
   return selectSubmenu;
 }
 //------------------------------------------------------------------------------
 public JMenu getLayoutMenu ()
 {
   return layoutMenu;
 }
 //------------------------------------------------------------------------------
 public Layouter getLayouter(){return this.layouter;}
 //------------------------------------------------------------------------------
 public void setPopupMode (PopupMode newMode)
 {
   if (currentPopupMode != null) {
     graphView.removeViewMode (currentPopupMode);
     currentPopupMode = newMode;
       // todo (pshannon, 23 oct 2002): a terrible hack! 
       // find a way to remove this special case, perhaps by adjusting 
       // CytoscapeWindow's ctor so that modes are installed only after
       // the CW is well initialized.  but since the y-supplied nodes no
       // nothing about their CytoscapeWindow parent, this is again a special
       // case.  this problem did not arise in the previous version because our
       // cytocape-aware mode was an inner class of CW, and thus could gain
       // access to CW data without being explicitly constructed with a CW reference.
     if (newMode instanceof NodeBrowsingMode) {
       NodeBrowsingMode m = (NodeBrowsingMode) newMode;
       m.set (CytoscapeWindow.this);
       }
     }
 
   graphView.addViewMode (newMode);
 
 } // setPopupMode
 //------------------------------------------------------------------------------
 public void setInteractivity (boolean newState)
 {
   if (newState == true) { // turn interactivity ON
     if (!viewModesInstalled) {
       graphView.addViewMode (currentGraphMode);
       if (currentPopupMode != null) graphView.addViewMode (currentPopupMode);
       viewModesInstalled = true;
       }
     graphView.setViewCursor (defaultCursor);
     setCursor (defaultCursor);
 
     // accept new undo entries - added by dramage 2002-08-23
     if (undoManager != null)
       undoManager.resume();
     }
   else {  // turn interactivity OFF
     if (viewModesInstalled) {
       graphView.removeViewMode (currentGraphMode);
       graphView.removeViewMode (currentPopupMode); 
       viewModesInstalled = false;
       }
     graphView.setViewCursor (busyCursor);
     setCursor (busyCursor);
 
     // deny new undo entries - added by dramage 2002-08-23
     if (undoManager != null)
         undoManager.pause();
     }
 
 } // setInteractivity
 //------------------------------------------------------------------------------
 protected JMenuBar createMenuBar ()
 {
   menuBar = new JMenuBar ();
   JMenu fileMenu = new JMenu ("File");
 
   JMenu loadSubMenu = new JMenu ("Load");
   fileMenu.add (loadSubMenu);
   JMenuItem mi = loadSubMenu.add (new LoadGMLFileAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_G, ActionEvent.CTRL_MASK));
   mi = loadSubMenu.add (new LoadInteractionFileAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_I, ActionEvent.CTRL_MASK));
   mi = loadSubMenu.add (new LoadExpressionMatrixAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_E, ActionEvent.CTRL_MASK));
   mi = loadSubMenu.add (new LoadBioDataServerAction ());
 
   JMenu saveSubMenu = new JMenu ("Save");
   fileMenu.add (saveSubMenu);
   saveSubMenu.add (new SaveAsGMLAction ());
   saveSubMenu.add (new SaveAsInteractionsAction ());
   saveSubMenu.add (new SaveVisibleNodesAction());
 
   fileMenu.add (new PrintAction ());
 
   mi = fileMenu.add (new CloseWindowAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_W, ActionEvent.CTRL_MASK));
   mi = fileMenu.add (new ExitAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_Q, ActionEvent.CTRL_MASK));
 
 
   menuBar.add (fileMenu);
 
   JMenu editMenu = new JMenu ("Edit");
   menuBar.add (editMenu);
   // added by dramage 2002-08-21
   undoMenuItem = editMenu.add (new UndoAction ());
   undoMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_Z, ActionEvent.CTRL_MASK));
   redoMenuItem = editMenu.add (new RedoAction ());
   redoMenuItem.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_Y, ActionEvent.CTRL_MASK));
   editMenu.addSeparator();
 
   ButtonGroup modeGroup = new ButtonGroup ();
   JRadioButtonMenuItem readOnlyModeButton = new JRadioButtonMenuItem ("Read-only mode");
   JRadioButtonMenuItem editModeButton = new JRadioButtonMenuItem ("Edit mode for nodes and edges");
   modeGroup.add (readOnlyModeButton);
   modeGroup.add (editModeButton);
   editMenu.add (readOnlyModeButton);
   editMenu.add (editModeButton);
   readOnlyModeButton.setSelected (true);
   readOnlyModeButton.addActionListener (new ReadOnlyModeAction ());
   editModeButton.addActionListener (new EditModeAction ());
   editMenu.addSeparator ();
 
   deleteSelectionMenuItem = editMenu.add (new DeleteSelectedAction ());
   deleteSelectionMenuItem.setEnabled (false);
 
   JMenu selectMenu = new JMenu ("Select");
   menuBar.add (selectMenu);
   mi = selectMenu.add (new EdgeManipulationAction ());
   JMenu viewMenu = new JMenu ("View");
   menuBar.add (viewMenu);
 
   JMenu displayNWSubMenu = new JMenu("New Window");
   viewMenu.add(displayNWSubMenu);
   mi = displayNWSubMenu.add(new NewWindowSelectedNodesOnlyAction());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_N, ActionEvent.CTRL_MASK));
   mi = displayNWSubMenu.add(new NewWindowSelectedNodesEdgesAction());
   mi = displayNWSubMenu.add (new CloneGraphInNewWindowAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_K, ActionEvent.CTRL_MASK));
 
   JMenu viewNodeSubMenu = new JMenu("Node Selection");
   viewMenu.add(viewNodeSubMenu);
   mi = viewNodeSubMenu.add(new InvertSelectedNodesAction());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_V, ActionEvent.CTRL_MASK));
   mi = viewNodeSubMenu.add(new HideSelectedNodesAction());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_H, ActionEvent.CTRL_MASK));
 
   mi = viewNodeSubMenu.add (new DisplayAttributesOfSelectedNodesAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_D, ActionEvent.CTRL_MASK));
   JMenu viewEdgeSubMenu = new JMenu("Edge Selection");
   viewMenu.add(viewEdgeSubMenu);
   viewEdgeSubMenu.add(new InvertSelectedEdgesAction());
   viewEdgeSubMenu.add(new HideSelectedEdgesAction());
  JMenu selectSubmenu = new JMenu("Select");
   viewMenu.add(selectSubmenu);
   selectSubmenu.add (new EdgeTypeDialogAction ());
   mi = selectSubmenu.add (new SelectFirstNeighborsAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_F, ActionEvent.CTRL_MASK));
   selectSubmenu.add (new AlphabeticalSelectionAction ());
   selectSubmenu.add (new ListFromFileSelectionAction ());
   selectSubmenu.add (new MenuFilterAction ());
 
   ButtonGroup layoutGroup = new ButtonGroup ();
   layoutMenu = new JMenu ("Layout");
   layoutMenu.setToolTipText ("Apply new layout algorithm to graph");
   menuBar.add (layoutMenu);
 
   JRadioButtonMenuItem layoutButton;
   layoutButton = new JRadioButtonMenuItem("Circular");
   layoutGroup.add(layoutButton);
   layoutMenu.add(layoutButton);
   layoutButton.addActionListener(new CircularLayoutAction ());
   
   layoutButton = new JRadioButtonMenuItem("Hierarchicial");
   layoutGroup.add(layoutButton);
   layoutMenu.add(layoutButton);
   layoutButton.addActionListener(new HierarchicalLayoutAction ());
   
   layoutButton = new JRadioButtonMenuItem("Organic");
   layoutGroup.add(layoutButton);
   layoutMenu.add(layoutButton);
   layoutButton.setSelected(true);
   layoutButton.addActionListener(new OrganicLayoutAction ());
   
   layoutButton = new JRadioButtonMenuItem("Embedded");
   layoutGroup.add(layoutButton);
   layoutMenu.add(layoutButton);
   layoutButton.addActionListener(new EmbeddedLayoutAction ());
 
   layoutButton = new JRadioButtonMenuItem("Random");
   layoutGroup.add(layoutButton);
   layoutMenu.add(layoutButton);
   layoutButton.addActionListener(new RandomLayoutAction ());
   
   layoutMenu.addSeparator();
   mi = layoutMenu.add (new LayoutAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_L, ActionEvent.CTRL_MASK));
   layoutMenu.add (new LayoutSelectionAction ());
 
   layoutMenu.addSeparator();
   JMenu alignSubMenu = new JMenu ("Align Selected Nodes");
   layoutMenu.add(alignSubMenu);
   alignSubMenu.add (new AlignHorizontalAction ());
   alignSubMenu.add (new AlignVerticalAction   ());
   layoutMenu.add(new RotateSelectedNodesAction());
   layoutMenu.add(new ReduceEquivalentNodesAction());
 
   ShrinkExpandGraphUI shrinkExpand = new ShrinkExpandGraphUI(this);  
   vizMenu = new JMenu ("Visualization"); // always create the viz menu
   menuBar.add (vizMenu);
   vizMenu.add (new SetVisualPropertiesAction ());
   vizMenu.add( new ColorNodesFromFile());
   //  vizMenu.add (new PrintPropsAction ());
 
   opsMenu = new JMenu ("PlugIns"); // always create the plugins menu
   menuBar.add (opsMenu);
 
   return menuBar;
 
 } // createMenuBar
 //------------------------------------------------------------------------------
 protected JToolBar createToolBar ()
 {
   JToolBar bar = new JToolBar ();
   JButton b;
 
   b = bar.add (new ZoomAction (0.9));
   b.setIcon (new ImageIcon (getClass().getResource("images/ZoomOut24.gif")));
   b.setToolTipText ("Zoom Out");
   b.setBorderPainted (false);
   b.setRolloverEnabled (true);
 
   b = bar.add (new ZoomAction (1.1));
   b.setIcon (new ImageIcon (getClass().getResource("images/ZoomIn24.gif")));
   b.setToolTipText ("Zoom In");
   b.setBorderPainted (false);
   
   b = bar.add (new ZoomSelectedAction ());
   b.setIcon (new ImageIcon (getClass().getResource("images/ZoomArea24.gif")));
   b.setToolTipText ("Zoom Selected Region");
   b.setBorderPainted (false);
 
   b = bar.add (new FitContentAction ());
   b.setIcon (new ImageIcon (getClass().getResource("images/overview.gif")));
   b.setToolTipText ("Zoom out to display all of current graph");
   b.setBorderPainted (false);
 
   // bar.addSeparator ();
 
   b = bar.add (new ShowAllAction ());
   b.setIcon (new ImageIcon (getClass().getResource("images/overall.gif")));
   b.setToolTipText ("Show all nodes and edges (unhiding as necessary)");
   b.setBorderPainted (false);
 
 
   b = bar.add (new HideSelectedAction ());
   b.setIcon (new ImageIcon (getClass().getResource("images/Zoom24.gif")));
   b.setToolTipText ("Hide Selected Region");
   b.setBorderPainted (false);
 
 
   bar.addSeparator ();
   b = bar.add (new MainFilterDialogAction());
   b.setIcon (new ImageIcon (getClass().getResource("images/Grid24.gif")));
   b.setToolTipText ("Apply Filters to Graph");
   b.setBorderPainted (false);
 
 
   bar.addSeparator ();
 
     
   return bar;
 
 } // createToolBar
 //------------------------------------------------------------------------------
 public void selectNodesByName (String [] nodeNames){
     boolean clearAllSelectionsFirst = true;
     selectNodesByName (nodeNames, clearAllSelectionsFirst);
 }
 //------------------------------------------------------------------------------
 /**
  * hide every node except those explicitly named.  canonical node names must
  * be used.
  */
 protected void showNodesByName (String [] nodeNames)
 {
     // not sure if this is any faster. - owo 2002 10 03
 
     // first show all nodes
     Graph2D g = graphView.getGraph2D ();
     graphHider.unhideAll ();
     Node [] nodes = graphView.getGraph2D().getNodeArray();
     
     // construct a hash of the nodeNames
     Hashtable namedNodes = new Hashtable();
     for (int n=0; n < nodeNames.length; n++) {
         nodeNames[n].toLowerCase();
         namedNodes.put(nodeNames[n],Boolean.TRUE);
     }
     
     // if a node in the graph isn't in the hash, hide it.
     for (int i=0; i < nodes.length; i++) {
         String graphNodeName = getCanonicalNodeName (nodes [i]);
         graphNodeName.toLowerCase();
         Boolean select = (Boolean) namedNodes.get(graphNodeName);
         if(select==null) {
             graphHider.hide (nodes [i]);
         }
     }
     redrawGraph ();
 
     // old code follows:
     //
     //Graph2D g = graphView.getGraph2D ();
     //graphHider.unhideAll ();
     //Node [] nodes = graphView.getGraph2D().getNodeArray();
     //for (int i=0; i < nodes.length; i++) {
     //boolean matched = false;
     //String graphNodeName = getCanonicalNodeName (nodes [i]);
     //for (int n=0; n < nodeNames.length; n++) {
     //if (nodeNames [n].equalsIgnoreCase (graphNodeName)) {
     //matched = true;
     //break;
     //} // if equals
     //} // for n
     //if (!matched) 
     //graphHider.hide (nodes [i]);
     //} // for i
     //redrawGraph ();
 } // showNodesByName
 //------------------------------------------------------------------------------
 /**
  * a Vector version of showNodesByName
  */ 
 public void showNodesByName (Vector uniqueNodeNames)
 {
   showNodesByName ((String []) uniqueNodeNames.toArray (new String [0]));
 
 } // showNodesByName (Vector)
 //------------------------------------------------------------------------------
 public void selectNodesByName (String [] nodeNames, boolean clearAllSelectionsFirst)
 {
     // not sure if this is any faster. - owo 2002 10 03
 
     Graph2D g = graphView.getGraph2D ();
     Node [] nodes = graphView.getGraph2D().getNodeArray();
     
     // construct a hash of the nodeNames
     Hashtable namedNodes = new Hashtable();
     for (int n=0; n < nodeNames.length; n++) {
         nodeNames[n].toLowerCase();
         namedNodes.put(nodeNames[n],Boolean.TRUE);
     }
 
     // if a node in the graph is in the hash, select it;
     // if not, and clearAllSelectionsFirst is true, unselect it.
     for (int i=0; i < nodes.length; i++) {
         String graphNodeName = getCanonicalNodeName (nodes [i]);
         NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [i]);
         graphNodeName.toLowerCase();
         Boolean select = (Boolean) namedNodes.get(graphNodeName);
         if(select!=null) {
             nodeRealizer.setSelected (true);
         }
         else if (clearAllSelectionsFirst) {
             nodeRealizer.setSelected (false);
         }
     }
     redrawGraph ();
 
   // old code follows
   //
   //Graph2D g = graphView.getGraph2D();
   //Node [] nodes = graphView.getGraph2D().getNodeArray();
   //for (int i=0; i < nodes.length; i++) {
   //String graphNodeName = getCanonicalNodeName (nodes [i]);
   //NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [i]);
   //boolean matched = false;
   //for (int n=0; n < nodeNames.length; n++) {
   //if (nodeNames [n].equalsIgnoreCase (graphNodeName)) {
   //matched = true;
   //break;
   //} // if matched
   //} // for n
   //if (clearAllSelectionsFirst && !matched)
   //nodeRealizer.setSelected (false);
   //else if (matched)
   //nodeRealizer.setSelected (true);
   //} // for i
   //redrawGraph ();
 } // selectNodesByName
 //------------------------------------------------------------------------------
 // added by jtwang 30 Sep 2002
 public void selectEdges (Edge[] edgesToSelect, boolean clearAllSelectionsFirst) {
     if (clearAllSelectionsFirst)
         graph.unselectEdges();
     
     for (int i = 0; i < edgesToSelect.length; i++) {
         EdgeRealizer eR = graph.getRealizer(edgesToSelect[i]);
         eR.setSelected(true);
     }
 
     redrawGraph();
 }
 
 /**
  *  quadratic (in)efficiency:  make this smarter (pshannon, 24 may 2002)
 
     fixed by jtwang to be linear 30 Sep 2002
  */
 public void selectNodes (Node [] nodesToSelect, boolean clearAllSelectionsFirst)
 {
   if (clearAllSelectionsFirst)
       graph.unselectNodes();
 
   for (int i = 0; i < nodesToSelect.length; i++) {
       NodeRealizer nR = graph.getRealizer(nodesToSelect[i]);
       nR.setSelected(true);
   }
 
   /* Replaced by jtwang 30 Sep 2002
   Node [] nodes = graphView.getGraph2D().getNodeArray();
 
   for (int i=0; i < nodes.length; i++) {
     NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [i]);
     boolean matched = false;
     for (int n=0; n < nodesToSelect.length; n++) {
       if (nodes [i] == nodesToSelect [n]) {
         matched = true;
         break;
         } // if matched
       } // for n
     if (clearAllSelectionsFirst && !matched)
       nodeRealizer.setSelected (false);
     else if (matched)
       nodeRealizer.setSelected (true);
     } // for i
   */
 
   redrawGraph ();
 
 } // selectNodesByName
 //-----------------------------------------------------------------------------
 public void deselectAllNodes(boolean redrawGraph){
     if(redrawGraph){
         deselectAllNodes();
     }else{
         // fixed by jtwang 30 Sep 2002
         graph.unselectNodes();
         /*
         //Graph2D g = graphView.getGraph2D();
         //Node [] nodes = graphView.getGraph2D().getNodeArray();
         Node [] nodes = graph.getNodeArray();
         for (int i=0; i < nodes.length; i++) {
             //NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [i]);
             //nodeRealizer.setSelected (false);
             this.graph.setSelected(nodes[i],false);
             } // for i */
     }
     
 }
 //------------------------------------------------------------------------------
 public void deselectAllNodes ()
 {
     // fixed by jtwang 30 Sep 2002
 
     graph.unselectNodes();
 
     /*
   Graph2D g = graphView.getGraph2D();
   Node [] nodes = graphView.getGraph2D().getNodeArray();
 
   for (int i=0; i < nodes.length; i++) {
     NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [i]);
     nodeRealizer.setSelected (false);
     } // for i
     */
   redrawGraph ();
 
 } // deselectAllNodes
 //------------------------------------------------------------------------------
 protected void selectNodesStartingWith (String key)
 {
   setInteractivity (false);
   key = key.toLowerCase ();
   Graph2D g = graphView.getGraph2D();
   redrawGraph ();
 
   Node [] nodes = graphView.getGraph2D().getNodeArray();
   
   for(int i = 0; i < nodes.length; i++){
       String nodeName = graphView.getGraph2D().getLabelText (nodes [i]);
       boolean matched = false;
       if (nodeName.toLowerCase().startsWith (key))
 	  matched = true;
       else if (bioDataServer != null) {
 	  try {
 	      String [] synonyms = bioDataServer.getAllCommonNames (getSpecies (nodes [i]), nodeName);
 	      for (int s=0; s < synonyms.length; s++)
 		  if (synonyms [s].toLowerCase().startsWith (key)) {
 		      matched = true;
 		      break;
 		  } // for s
 	  }catch (Exception ignoreForNow) {;}
       }
       setNodeSelected (nodes [i], matched);
   } // for i
   
   setInteractivity (true);
   redrawGraph ();
   
 } // selectDisplyToNodesStartingWith ...
 //------------------------------------------------------------------------------
 protected void additionallySelectNodesMatching (String key)
 {
   setInteractivity (false);
   key = key.toLowerCase ();
   Graph2D g = graphView.getGraph2D();
   redrawGraph ();
 
   Node [] nodes = graphView.getGraph2D().getNodeArray();
 
   for (int i=0; i < nodes.length; i++) {
     String nodeName = graphView.getGraph2D().getLabelText (nodes [i]);
     boolean matched = false;
     if (nodeName.toLowerCase().equalsIgnoreCase (key))
       matched = true;
     else if (bioDataServer != null) {
       try {
         String [] synonyms = bioDataServer.getAllCommonNames (getSpecies (nodes [i]), nodeName);
         for (int s=0; s < synonyms.length; s++)
           if (synonyms [s].equalsIgnoreCase (key)) {
             matched = true;
             break;
          } // for s
        }
       catch (Exception ignoreForNow) {;}
       } // else if: checking synonyms
     if(matched)
         setNodeSelected (nodes [i], true);
     } // for i
 
   setInteractivity (true);
   redrawGraph ();
 
 } // selectDisplyToNodesStartingWith ...
 //------------------------------------------------------------------------------
 protected String findCanonicalName(String key) {
     String canonicalName = key;
     if (bioDataServer != null) {
         try {
             String [] synonyms = bioDataServer.getAllCommonNames (getDefaultSpecies (), key);
             for (int s = 0; s < synonyms.length; s++) {
                 String sname = synonyms[s];
                 if (sname.equalsIgnoreCase (key)) {
                     canonicalName = sname;
                     break;
                 }
             }
         } catch (Exception ignoreForNow) {;}
     }
     return canonicalName;
 } // else if: checking synonyms
 
 protected void setNodeSelected (Node node, boolean visible)
 {
     if(visible){
 	System.out.println("setNodeSelected " + node);
 	System.out.flush();
     }
     NodeRealizer r = graphView.getGraph2D().getRealizer(node);
     r.setSelected (visible);
     
 } // setNodeSelected
 //------------------------------------------------------------------------------
 protected void updateEdgeVisibilityFromNodeVisibility ()
 {
   for (EdgeCursor ec = graphView.getGraph2D().edges(); ec.ok(); ec.next()) {
     Edge e = ec.edge ();
     Node source = e.source ();
     boolean edgeShouldBeVisible = false;
     if (graphView.getGraph2D().getRealizer(source).isVisible ()) {
       Node target = e.target ();
       if (graphView.getGraph2D().getRealizer(target).isVisible ()) {
         edgeShouldBeVisible = true;
         } // if target node is visible
       } // if source node is visible
     EdgeRealizer er = graphView.getGraph2D().getRealizer(e);
     er.setVisible (edgeShouldBeVisible);
     } // for each edge
 
 } // updateEdgeVisibilityFromNodeVisibility
 //------------------------------------------------------------------------------
 public void applyLayout (boolean animated)
 {
   logger.warning ("starting layout...");
   setInteractivity (false);
   layouter.doLayout (graphView.getGraph2D ());
 
   graphView.fitContent ();
   graphView.setZoom (graphView.getZoom ()*0.9);
 
   setInteractivity (true);
   logger.info (" done");
 
 } // applyLayout
 
 
 
 // applyLayoutSelection
 //
 // apply layout, but only on currently selected nodes
 public void applyLayoutSelection() {
     Graph2D g = graphView.getGraph2D();
 
     // special case for EmbeddedLayouter: layout whole graph,
     // holding unselected nodes in place
     // OPTIMIZE ME!
     if (layouter.getClass().getName().endsWith("EmbeddedLayouter")) {
         // data provider of sluggishness for each node
         NodeMap slug = g.createNodeMap();
         g.addDataProvider("Cytoscape:slug", slug);
 
         for (NodeCursor nc = g.selectedNodes(); nc.ok(); nc.next())
             slug.setDouble(nc.node(), 0.5);
 
         Node[] nodeList = g.getNodeArray();
         int nC = g.nodeCount();
         for (int i = 0; i < nC; i++)
             if (slug.getDouble(nodeList[i]) != 0.5)
                 slug.setDouble(nodeList[i], 0.0);
 
         applyLayout(false);
 
         g.removeDataProvider("Cytoscape:slug");
         g.disposeNodeMap(slug);
     }
 
     // special case for OrganicLayouter: layout whole graph, holding
     // unselected nodes in place
     else if (layouter.getClass().getName().endsWith("OrganicLayouter")) {
         OrganicLayouter ogo = (OrganicLayouter)layouter;
 
         // data provider of selectedness for each node
         NodeMap s = g.createNodeMap();
         g.addDataProvider(Layouter.SELECTED_NODES, s);
 
         for (NodeCursor nc = g.selectedNodes(); nc.ok(); nc.next())
             s.setBool(nc.node(), true);
 
         Node[] nodeList = g.getNodeArray();
         int nC = g.nodeCount();
         for (int i = 0; i < nC; i++)
             if (s.getBool(nodeList[i]) != true)
                 s.setBool(nodeList[i], false);
         
         byte oldSphere = ogo.getSphereOfAction();
         ogo.setSphereOfAction(OrganicLayouter.ONLY_SELECTION);
         applyLayout(false);
         ogo.setSphereOfAction(oldSphere);
 
         g.removeDataProvider(Layouter.SELECTED_NODES);
         g.disposeNodeMap(s);
     }
 
 
     // other layouters
     else {
         logger.warning ("starting layout..."); 
         setInteractivity (false);
 
         Subgraph subgraph = new Subgraph(g, g.selectedNodes());
         layouter.doLayout (subgraph);
         subgraph.reInsert();
 
         // remove bends
         EdgeCursor cursor = graphView.getGraph2D().edges();
         cursor.toFirst ();
         for (int i=0; i < cursor.size(); i++){
             Edge target = cursor.edge();
             EdgeRealizer e = graphView.getGraph2D().getRealizer(target);
             e.clearBends();
             cursor.cyclicNext();
         }
 
         setInteractivity (true);
         logger.info("  done");
     }
 }
 
 
 
 //------------------------------------------------------------------------------
 class PrintAction extends AbstractAction 
 {
   PageFormat pageFormat;
   OptionHandler printOptions;
     
   PrintAction () {
     super ("Print...");
     printOptions = new OptionHandler ("Print Options");
     printOptions.addInt ("Poster Rows",1);
     printOptions.addInt ("Poster Columns",1);
     printOptions.addBool ("Add Poster Coords",false);
     final String[] area = {"View","Graph"};
     printOptions.addEnum ("Clip Area",area,1);
     }
 
   public void actionPerformed (ActionEvent e) {
 
     Graph2DPrinter gprinter = new Graph2DPrinter (graphView);
     PrinterJob printJob = PrinterJob.getPrinterJob ();
     if (pageFormat == null) pageFormat = printJob.defaultPage ();
     printJob.setPrintable (gprinter, pageFormat);
       
     if (printJob.printDialog ()) try {
       setInteractivity (false);
       printJob.print ();  
       }
     catch (Exception ex) {
       ex.printStackTrace ();
       }
     setInteractivity (true);
     } // actionPerformed
 
 } // inner class PrintAction
 
 //------------------------------------------------------------------------------
 /*
 protected class PrintPropsAction extends AbstractAction   {
   PrintPropsAction () { super ("Print Properties"); }
 
   public void actionPerformed (ActionEvent e) {
       // for all properties, print their names (keys) out.
       
       String [] attributeNames = nodeAttributes.getAttributeNames ();
       for (int i=0; i < attributeNames.length; i++) {
           String attributeName = attributeNames [i];
           logger.info(attributeName);
       }
   }
 
 }
 */
 
 
 //------------------------------------------------------------------------------
 // added by iliana 10/28/2002
 // Pops up a dialog that allows the user to enter a file with ORfs, and choose
 // a color. Nodes in the graph with the given ORFs will be colored.
 protected class ColorNodesFromFile extends AbstractAction{
     ColorNodesFromFile(){
 	super("Color Nodes From File");
     }
 
     public void actionPerformed(ActionEvent e){
 	JDialog dialog = new ColorNodesFromFileDialog(CytoscapeWindow.this);
 	dialog.pack ();
 	dialog.setLocationRelativeTo (mainFrame);
 	dialog.setVisible (true);
 
     }
 
 }//ColorNodesFromFile
 //------------------------------------------------------------------------------
 protected class SetVisualPropertiesAction extends AbstractAction   {
     MutableString labelKey;
     MutableBool shouldUpdateNodeLabels;
     SetVisualPropertiesAction () {
         super ("Set Visual Properties");
         labelKey = new MutableString("canonicalName");
         shouldUpdateNodeLabels = new MutableBool(false);
     }
     
     public void actionPerformed (ActionEvent e) {
         JDialog vizDialog = new VisualPropertiesDialog
             (mainFrame, "Set Visual Properties",
              vizMapper, nodeAttributes,
              edgeAttributes, labelKey,
              shouldUpdateNodeLabels);
         vizDialog.pack ();
         vizDialog.setLocationRelativeTo (mainFrame);
         vizDialog.setVisible (true);
 
         if(shouldUpdateNodeLabels.getBool())
             displayNodeLabels(labelKey.getString());
 
         redrawGraph();
     }
 
 }
 //------------------------------------------------------------------------------
 /*
 protected class HideEdgesAction extends AbstractAction   {
   HideEdgesAction () { super ("Hide Edges"); }
 
   public void actionPerformed (ActionEvent e) {
     graphHider.hideEdges ();
     redrawGraph ();
     }
 }
 */
 //------------------------------------------------------------------------------
 protected void hideSelectedNodes() {
     Graph2D g = graphView.getGraph2D ();
     NodeCursor nc = g.selectedNodes (); 
     while (nc.ok ()) {
         Node node = nc.node ();
         graphHider.hide (node);
         nc.next ();
     }
     redrawGraph ();
 }
 protected void hideSelectedEdges() {
     Graph2D g = graphView.getGraph2D ();
     EdgeCursor nc = g.selectedEdges (); 
     while (nc.ok ()) {
         Edge edge = nc.edge ();
         graphHider.hide (edge);
         nc.next ();
     }
     redrawGraph ();
 }
 protected class HideSelectedNodesAction extends AbstractAction   {
   HideSelectedNodesAction () { super ("Hide"); }
 
   public void actionPerformed (ActionEvent e) {
       hideSelectedNodes();
   }
 }
 protected class InvertSelectedNodesAction extends AbstractAction {
     InvertSelectedNodesAction () { super ("Invert"); }
 
     public void actionPerformed (ActionEvent e) {
         Graph2D g = graphView.getGraph2D();
         Node [] nodes = graphView.getGraph2D().getNodeArray();
         
         for (int i=0; i < nodes.length; i++) {
             NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [i]);
             nodeRealizer.setSelected (!nodeRealizer.isSelected());
         }
         redrawGraph ();
     }
 }
 protected class HideSelectedEdgesAction extends AbstractAction   {
   HideSelectedEdgesAction () { super ("Hide"); }
 
   public void actionPerformed (ActionEvent e) {
       hideSelectedEdges();
   }
 }
 protected class InvertSelectedEdgesAction extends AbstractAction {
     InvertSelectedEdgesAction () { super ("Invert"); }
 
     public void actionPerformed (ActionEvent e) {
         Graph2D g = graphView.getGraph2D();
         Edge [] edges = graphView.getGraph2D().getEdgeArray();
         
         for (int i=0; i < edges.length; i++) {
             EdgeRealizer edgeRealizer = graphView.getGraph2D().getRealizer(edges [i]);
             edgeRealizer.setSelected (!edgeRealizer.isSelected());
         }
         redrawGraph ();
     }
 }
 //------------------------------------------------------------------------------
 /*
 protected class ShowEdgesAction extends AbstractAction   {
   ShowEdgesAction () { super ("Show Edges"); }
 
   public void actionPerformed (ActionEvent e) {
     graphHider.unhideEdges ();
     redrawGraph ();
     }
 }
 */
 //------------------------------------------------------------------------------
 protected class DeleteSelectedAction extends AbstractAction   {
   DeleteSelectedAction () { super ("Delete Selected Nodes and Edges"); }
 
   public void actionPerformed (ActionEvent e) {
     Graph2D g = graphView.getGraph2D ();
 
     // added by dramage 2002-08-23
     g.firePreEvent();
     
     NodeCursor nc = g.selectedNodes (); 
     while (nc.ok ()) {
       Node node = nc.node ();
       g.removeNode (node);
       nc.next ();
       } // while
     EdgeCursor ec = g.selectedEdges ();
     while (ec.ok ()) {
       g.removeEdge (ec.edge ());
       ec.next ();
       }
 
     // added by dramage 2002-08-23
     g.firePostEvent();
 
     
     redrawGraph ();
     } // actionPerformed
   
 
 } // inner class DeleteSelectedAction
 //------------------------------------------------------------------------------
 protected class LayoutAction extends AbstractAction   {
   LayoutAction () { super ("Layout whole graph"); }
     
   public void actionPerformed (ActionEvent e) {
       undoManager.saveRealizerState();
       undoManager.pause();
       applyLayout (false);
       undoManager.resume();
       redrawGraph ();
     }
 }
 
 // lay out selected nodes only - dramage
 protected class LayoutSelectionAction extends AbstractAction {
     LayoutSelectionAction () { super ("Layout current selection"); }
 
   public void actionPerformed (ActionEvent e) {
       undoManager.saveRealizerState();
       undoManager.pause();
       applyLayoutSelection ();
       undoManager.resume();
       redrawGraph ();
     }
 }
 
 
 
 
 //------------------------------------------------------------------------------
 protected class CircularLayoutAction extends AbstractAction   {
   CircularLayoutAction () { super ("Circular"); }
     
   public void actionPerformed (ActionEvent e) {
     layouter = new CircularLayouter ();
     }
 }
 //------------------------------------------------------------------------------
 protected class HierarchicalLayoutAction extends AbstractAction   {
     HierarchicalLayoutDialog hDialog;
     
     HierarchicalLayoutAction () { super ("Hierarchical"); }
     
     public void actionPerformed (ActionEvent e) {
         
       /********************
         if (hDialog == null)
             hDialog = new HierarchicalLayoutDialog (mainFrame);
         hDialog.pack ();
         hDialog.setLocationRelativeTo (mainFrame);
         hDialog.setVisible (true);
         layouter = hDialog.getLayouter();
       ********************/
       layouter = new HierarchicLayouter ();
     }
 }
 //------------------------------------------------------------------------------
 protected class OrganicLayoutAction extends AbstractAction   {
   OrganicLayoutAction () { super ("Organic"); }
     
   public void actionPerformed (ActionEvent e) {
     OrganicLayouter ol = new OrganicLayouter ();
     ol.setActivateDeterministicMode (true);
     ol.setPreferredEdgeLength(80);
     layouter = ol;
     }
 }
 //------------------------------------------------------------------------------
 protected class RandomLayoutAction extends AbstractAction   {
   RandomLayoutAction () { super ("Random"); }
     
   public void actionPerformed (ActionEvent e) {
     layouter = new RandomLayouter ();
     }
 }
 
 
 protected class EmbeddedLayoutAction extends AbstractAction {
     EmbeddedLayoutAction () { super("Embedded"); }
 
     public void actionPerformed (ActionEvent e) {
         layouter = new EmbeddedLayouter();
     }
 }
 
 //-----------------------------------------------------------------------------
 
 protected class AlignHorizontalAction extends AbstractAction {
     AlignHorizontalAction () { super ("Horizontal"); }
 
     public void actionPerformed (ActionEvent e) {
         // remember state for undo - dramage 2002-08-22
         undoManager.saveRealizerState();
         undoManager.pause();
 
         // compute average Y coordinate
         double avgYcoord=0;
         int numSelected=0;
         for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
             Node n = nc.node();
             if (graph.isSelected(n)) {
                 avgYcoord += graph.getY(n);
                 numSelected++;
             }
         }
         avgYcoord /= numSelected;
         
         // move all nodes to average Y coord
         for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
             Node n = nc.node();
             if (graph.isSelected(n))
                 graph.setLocation(n, graph.getX(n), avgYcoord);
         }
 
         // resume undo manager's listener - dramage
         undoManager.resume();
 
         redrawGraph();
     }
 }
 
 protected class AlignVerticalAction extends AbstractAction {
     AlignVerticalAction () { super ("Vertical"); }
 
     public void actionPerformed (ActionEvent e) {
         // remember state for undo - dramage 2002-08-22
         undoManager.saveRealizerState();
         undoManager.pause();
 
         // compute average X coordinate
         double avgXcoord=0;
         int numSelected=0;
         for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
             Node n = nc.node();
             if (graph.isSelected(n)) {
                 avgXcoord += graph.getX(n);
                 numSelected++;
             }
         }
         avgXcoord /= numSelected;
         
         // move all nodes to average X coord
         for (NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
             Node n = nc.node();
             if (graph.isSelected(n))
                 graph.setLocation(n, avgXcoord, graph.getY(n));
         }
 
         // resume undo manager's listener - dramage
         undoManager.resume();
 
         redrawGraph();
     }
 }
 
 /**
  * Rotates the given selection by the specified amount.
  *
  * added by dramage 2002-08-20
  */
 protected class RotateSelectedNodesAction extends AbstractAction {
     RotateSelectedNodesAction () { super ("Rotate Selected Nodes"); }
 
     public void actionPerformed (ActionEvent e) {
         undoManager.saveRealizerState();
         undoManager.pause();
         RotateSelectionDialog d = new RotateSelectionDialog(mainFrame,
                                                          CytoscapeWindow.this,
                                                             graph);
         undoManager.resume();
     }
 }
 
 //------------------------------------------------------------------------------
 
 protected class ReduceEquivalentNodesAction extends AbstractAction  {
     ReduceEquivalentNodesAction () {
         super ("Reduce Equivalent Nodes"); 
     } // ctor
    public void actionPerformed (ActionEvent e) {
        new ReduceEquivalentNodes(nodeAttributes, edgeAttributes, graph);
        redrawGraph();
    }
 }
 
 //-----------------------------------------------------------------------------
 protected class AlphabeticalSelectionAction extends AbstractAction   {
   AlphabeticalSelectionAction () { super ("Nodes By Name"); }
 
   public void actionPerformed (ActionEvent e) {
     String answer = 
       (String) JOptionPane.showInputDialog (mainFrame, 
               "Select nodes whose name (or synonym) starts with");
     if (answer != null && answer.length () > 0)
       selectNodesStartingWith (answer.trim ());
     }
 }
 //------------------------------------------------------------------------------
 protected class ListFromFileSelectionAction extends AbstractAction   {
   ListFromFileSelectionAction () { super ("Nodes From File"); }
 
     public void actionPerformed (ActionEvent e) {
         boolean cancelSelectionAction = !useSelectionFile();
     }
 
     private boolean useSelectionFile() {
         JFileChooser fChooser = new JFileChooser(currentDirectory);     
         fChooser.setDialogTitle("Load Gene Selection File");
         switch (fChooser.showOpenDialog(null)) {
                 
         case JFileChooser.APPROVE_OPTION:
             File file = fChooser.getSelectedFile();
             currentDirectory = fChooser.getCurrentDirectory();
             String s;
 
             try {
                 FileReader fin = new FileReader(file);
                 BufferedReader bin = new BufferedReader(fin);
                 
                 // create a hash of all the nodes in the file
                 Hashtable fileNodes = new Hashtable();
                 while ((s = bin.readLine()) != null) {
                     StringTokenizer st = new StringTokenizer(s);
                     String name = st.nextToken();
                     String trimname = name.trim();
                     if(trimname.length() > 0) {
                         String canonicalName = findCanonicalName(trimname);
                         fileNodes.put(canonicalName, Boolean.TRUE);
                     }
                 }
                 fin.close();
 
                 // loop through all the node of the graph
                 // selecting those in the file
                 Graph2D g = graphView.getGraph2D();
                 Node [] nodes = graphView.getGraph2D().getNodeArray();
                 for (int i=0; i < nodes.length; i++) {
                     Node node = nodes[i];
                     String canonicalName = nodeAttributes.getCanonicalName(node);
                     if (canonicalName == null) {
                         // use node label as canonical name
                         canonicalName = graph.getLabelText(node);
                     }
                     Boolean select = (Boolean) fileNodes.get(canonicalName);
                     if (select != null) {
                         graphView.getGraph2D().getRealizer(node).setSelected(true);
                     }
                 }
                 redrawGraph ();
                   
             } catch (Exception e) {
                 JOptionPane.showMessageDialog(null, e.toString(),
                                  "Error Reading \"" + file.getName()+"\"",
                                                JOptionPane.ERROR_MESSAGE);
                 return false;
             }
 
             return true;
 
         default:
             // cancel or error
             return false;
         }
     }
     
 }
 
 //------------------------------------------------------------------------------
 // this is public so activePaths can get at it;
 // active paths depends on saveVisibleNodeNames () to save state periodically.
 
 public boolean saveVisibleNodeNames () { 
     return saveVisibleNodeNames("visibleNodes.txt"); 
 }
 
 public boolean saveVisibleNodeNames (String filename)
 {
     Graph2D g = graphView.getGraph2D();
     Node [] nodes = graphView.getGraph2D().getNodeArray();
     File file = new File(filename);
     try {
         FileWriter fout = new FileWriter(file);
         for (int i=0; i < nodes.length; i++) {
             Node node = nodes [i];
             NodeRealizer r = graphView.getGraph2D().getRealizer(node);
             String defaultName = r.getLabelText ();
             String canonicalName = nodeAttributes.getCanonicalName (node);
             fout.write(canonicalName + "\n");
         } // for i
         fout.close();
         return true;
     }  catch (IOException e) {
         JOptionPane.showMessageDialog(null, e.toString(),
                                       "Error Writing to \"" + file.getName()+"\"",
                                       JOptionPane.ERROR_MESSAGE);
         return false;
     }
           
 } // saveVisibleNodeNames
 
 //------------------------------------------------------------------------------
 protected class SaveVisibleNodesAction extends AbstractAction   {
   SaveVisibleNodesAction () { super ("Visible Nodes"); }
 
     public void actionPerformed (ActionEvent e) {
         JFileChooser chooser = new JFileChooser (currentDirectory);
         if (chooser.showSaveDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
             String name = chooser.getSelectedFile ().toString ();
             currentDirectory = chooser.getCurrentDirectory();
             boolean itWorked = saveVisibleNodeNames (name);
             Object[] options = {"OK"};
             if(itWorked) {
                 JOptionPane.showOptionDialog(null,
                                          "Visible Nodes Saved.",
                                          "Visible Nodes Saved.",
                                          JOptionPane.DEFAULT_OPTION,
                                          JOptionPane.PLAIN_MESSAGE,
                                          null, options, options[0]);
             }
         }
     }
 }
 //------------------------------------------------------------------------------
 protected class DeselectAllAction extends AbstractAction   {
   DeselectAllAction () { super ("Deselect All"); }
 
   public void actionPerformed (ActionEvent e) {
     deselectAllNodes ();
     }
 }
 
 
 /**
  * Updates the undoMenuItem and redoMenuItem enabled status depending
  * on the number of available undo and redo actions
  *
  * added by dramage 2002-08-21
  */
 public void updateUndoRedoMenuItemStatus () {
     undoMenuItem.setEnabled(undoManager.undoLength() > 0 ? true : false);
     redoMenuItem.setEnabled(undoManager.redoLength() > 0 ? true : false);
 }
 
 /**
  * Uses the UndoManager to undo changes.
  *
  * added by dramage 2002-08-21
  */
 protected class UndoAction extends AbstractAction {
     UndoAction () { super ("Undo"); }
     
     public void actionPerformed(ActionEvent e) {
       undoManager.undo();
       updateUndoRedoMenuItemStatus();
       redrawGraph();
     }
 }
 
 /**
  * Uses the UndoManager to redo changes.
  *
  * added by dramage 2002-08-21
  */
 protected class RedoAction extends AbstractAction {
     RedoAction () { super ("Redo"); }
 
     public void actionPerformed(ActionEvent e) {
       undoManager.redo();
       updateUndoRedoMenuItemStatus();
       redrawGraph();
     }
 }
 
 
 
 
 
 //------------------------------------------------------------------------------
 protected class ReadOnlyModeAction extends AbstractAction   {
   ReadOnlyModeAction () { super ("Read only Mode"); }
 
   public void actionPerformed (ActionEvent e) {
     graphView.removeViewMode (currentGraphMode);
     currentGraphMode = readOnlyGraphMode;
     graphView.addViewMode (currentGraphMode);
     deleteSelectionMenuItem.setEnabled (false);
     }
 }
 //------------------------------------------------------------------------------
 protected class EditModeAction extends AbstractAction   {
   EditModeAction () { super ("Edit Mode for Nodes and Edges"); }
 
   public void actionPerformed (ActionEvent e) {
     graphView.removeViewMode (currentGraphMode);
     currentGraphMode = editGraphMode;
     graphView.addViewMode (currentGraphMode);
     deleteSelectionMenuItem.setEnabled (true);
     }
 }
 //------------------------------------------------------------------------------
 /**
  *  select every first neighbor (directly connected nodes) of the currently
  *  selected nodes.
  */
 protected class SelectFirstNeighborsAction extends AbstractAction {
   SelectFirstNeighborsAction () { 
       super ("First neighbors of selected nodes"); 
       }
   public void actionPerformed (ActionEvent e) {
     Graph2D g = graphView.getGraph2D ();
     NodeCursor nc = g.selectedNodes (); 
     Vector newNodes = new Vector ();
     
     // for all selected nodes
     for (nc.toFirst (); nc.ok (); nc.next ()) {
       Node node = nc.node ();
       EdgeCursor ec = node.edges ();
       
       for (ec.toFirst (); ec.ok (); ec.next ()) {
         Edge edge = ec.edge ();
         Node source = edge.source ();
         if (!newNodes.contains (source))
           newNodes.add (source);
         Node target = edge.target ();
         if (!newNodes.contains (target))
           newNodes.add (target);
         } // for edges
       } // for selected nodes
     
     for (int i=0; i < newNodes.size (); i++) {
       Node node = (Node) newNodes.elementAt (i);
       NodeRealizer realizer = graphView.getGraph2D().getRealizer (node);
       realizer.setSelected (true);
       }
     
     redrawGraph ();
     } // actionPerformed
 
 } // SelectFirstNeighborsAction
 //------------------------------------------------------------------------------
 /**
  * a temporary debugging aid (pshannon, mar 2002): all attributes of the 
  * selected nodes are printed to stdout.
  */
 protected class DisplayAttributesOfSelectedNodesAction extends AbstractAction {
   DisplayAttributesOfSelectedNodesAction () { super ("Display attributes"); }
   public void actionPerformed (ActionEvent e) {
     Graph2D g = graphView.getGraph2D ();
     NodeCursor nc = g.selectedNodes (); 
     logger.info ("debug, selected node count: " + nc.size ());
     for (nc.toFirst (); nc.ok (); nc.next ()) { // get the canonical name of the old node
       String canonicalName = nodeAttributes.getCanonicalName (nc.node ());
       logger.info (canonicalName + ": " + nodeAttributes.getAttributes (canonicalName));
       } // for
     EdgeCursor ec = g.selectedEdges (); 
     logger.info ("debug, selected edge count: " + ec.size ());
     for (ec.toFirst (); ec.ok (); ec.next ()) { // get the canonical name of the old node
       String edgeName = edgeAttributes.getCanonicalName (ec.edge ());
       logger.info (edgeName + ": " + edgeAttributes.getAttributes (edgeName));
       } // for
     }
 }
 //------------------------------------------------------------------------------
 protected class NewWindowSelectedNodesOnlyAction extends AbstractAction   {
   NewWindowSelectedNodesOnlyAction () { super ("Selected nodes, All edges"); }
 
   public void actionPerformed (ActionEvent e) {
     SelectedSubGraphFactory factory = new SelectedSubGraphFactory (graph, nodeAttributes, edgeAttributes);
     Graph2D subGraph = factory.getSubGraph ();
     GraphObjAttributes newNodeAttributes = factory.getNodeAttributes ();
     GraphObjAttributes newEdgeAttributes = factory.getEdgeAttributes ();
 
     String title = "selection";
     if (titleForCurrentSelection != null) 
       title = titleForCurrentSelection;
     try {
       boolean requestFreshLayout = true;
       CytoscapeWindow newWindow =
           new CytoscapeWindow  (parentApp, config, logger, subGraph, expressionData, 
                                 bioDataServer, newNodeAttributes, newEdgeAttributes, 
                                 "dataSourceName", expressionDataFilename, title, 
                                 requestFreshLayout);
       subwindows.add (newWindow);  
       }
     catch (Exception e00) {
       System.err.println ("exception when creating new window");
       e00.printStackTrace ();
       }
 
     } // actionPerformed
 
 } // inner class NewWindowSelectedNodesOnlyAction
 //------------------------------------------------------------------------------
 protected class NewWindowSelectedNodesEdgesAction extends AbstractAction   {
   NewWindowSelectedNodesEdgesAction () { super ("Selected nodes, Selected edges"); }
 
   public void actionPerformed (ActionEvent e) {
       // allows us to temporarily hide unselected nodes/edges
       GraphHider hider = new GraphHider(graph);
 
       // hide unselected nodes
       for (NodeCursor nodes = graph.nodes(); nodes.ok(); nodes.next())
           if (!graph.isSelected(nodes.node()))
               hider.hide(nodes.node());
 
       // hide unselected edges
       for (EdgeCursor edges = graph.edges(); edges.ok(); edges.next())
           if (!graph.isSelected(edges.edge()))
               hider.hide(edges.edge());
 
       SelectedSubGraphFactory factory
           = new SelectedSubGraphFactory(graph, nodeAttributes, edgeAttributes);
       Graph2D subGraph = factory.getSubGraph ();
       GraphObjAttributes newNodeAttributes = factory.getNodeAttributes ();
       GraphObjAttributes newEdgeAttributes = factory.getEdgeAttributes ();
 
       // unhide unselected nodes & edges
       hider.unhideAll();
 
       String title = "selection";
       if (titleForCurrentSelection != null) 
           title = titleForCurrentSelection;
       try {
           boolean requestFreshLayout = true;
           CytoscapeWindow newWindow =
               new CytoscapeWindow  (parentApp, config, logger, subGraph,
                                     expressionData, bioDataServer,
                                     newNodeAttributes, newEdgeAttributes, 
                                     "dataSourceName", expressionDataFilename,
                                     title, requestFreshLayout);
           subwindows.add (newWindow);  
       }
       catch (Exception e00) {
           System.err.println ("exception when creating new window");
           e00.printStackTrace ();
       }
 
     } // actionPerformed
 
 } // inner class NewWindowSelectedNodesEdgesAction
 //------------------------------------------------------------------------------
 protected class CloneGraphInNewWindowAction extends AbstractAction   {
     CloneGraphInNewWindowAction () { super ("Whole graph"); }
 
   public void actionPerformed (ActionEvent e) {
     setInteractivity (false);
     cloneWindow ();
     setInteractivity (true);
     } // actionPerformed
 
 } // inner class CloneGraphInNewWindowAction
 //------------------------------------------------------------------------------
 /**
  *  create a new CytoscapeWindow with an exact copy of the current window,
  *  nodes, edges, and all attributes.  <p>
  *  as a temporary expedient, this method piggybacks upon the already
  *  existing and tested 'SelectedSubGraphFactory' class, by first selecting all
  *  nodes in the current window and then invoking the factory.  when time
  *  permits, the two stages (select, clone) should be refactored into two 
  *  independent operations,  probably by creating a GraphFactory class, which
  *  clones nodes, edges, and attributes.
  */
 public void cloneWindow ()
 {
   Node [] nodes = graph.getNodeArray ();
   for (int i=0; i < nodes.length; i++)
     graph.setSelected (nodes [i], true);
   SelectedSubGraphFactory factory = new SelectedSubGraphFactory (graph, nodeAttributes, edgeAttributes);
   Graph2D subGraph = factory.getSubGraph ();
   GraphObjAttributes newNodeAttributes = factory.getNodeAttributes ();
   GraphObjAttributes newEdgeAttributes = factory.getEdgeAttributes ();
 
   String title = "selection";
   if (titleForCurrentSelection != null) 
     title = titleForCurrentSelection;
   try {
     boolean requestFreshLayout = true;
     CytoscapeWindow newWindow =
         new CytoscapeWindow  (parentApp, config, logger, subGraph, expressionData, 
                               bioDataServer, newNodeAttributes, newEdgeAttributes, 
                               "dataSourceName", expressionDataFilename, title, 
                               requestFreshLayout);
     subwindows.add (newWindow);
     graph.unselectAll ();
     }
   catch (Exception e00) {
     System.err.println ("exception when creating new window");
     e00.printStackTrace ();
     }
 
 } // cloneWindow
 //------------------------------------------------------------------------------
 class SelectedSubGraphMaker {
 
   Graph2D parentGraph;
   Graph2D subGraph;
   GraphObjAttributes parentNodeAttributes, parentEdgeAttributes;
   GraphObjAttributes newNodeAttributes, newEdgeAttributes;
   HashMap parentNameMap = new HashMap ();  // maps from commonName to canonicalName
 
   SelectedSubGraphMaker (Graph2D parentGraph, GraphObjAttributes nodeAttributes,
                          GraphObjAttributes edgeAttributes) {
 
     this.parentGraph = parentGraph;
     this.parentNodeAttributes = nodeAttributes;
     this.parentEdgeAttributes = edgeAttributes;
 
     NodeCursor nc = parentGraph.selectedNodes (); 
 
     for (nc.toFirst (); nc.ok (); nc.next ()) { 
       String canonicalName = parentNodeAttributes.getCanonicalName (nc.node ());
       if (canonicalName != null) {
         String commonName = (String) parentNodeAttributes.getValue ("commonName", canonicalName);
         if (commonName != null) 
            parentNameMap.put (commonName, canonicalName);
         } // if
       } // for nc
 
     EdgeCursor ec = parentGraph.selectedEdges (); 
 
     nc.toFirst ();
     subGraph = new Graph2D (parentGraph, nc);
     Node [] newNodes = subGraph.getNodeArray ();
     logger.warning ("nodes in new subgraph: " + newNodes.length);
 
     newNodeAttributes = (GraphObjAttributes) parentNodeAttributes.clone ();
     newNodeAttributes.clearNameMap ();
     
     for (int i=0; i < newNodes.length; i++) {
       Node newNode = newNodes [i];
       String commonName = subGraph.getLabelText (newNode);
       String canonicalName = (String) parentNameMap.get (commonName);
       NodeRealizer r = subGraph.getRealizer (newNode);
       r.setLabelText (canonicalName);
       newNodeAttributes.addNameMapping (canonicalName, newNode);
       }
 
     newEdgeAttributes = (GraphObjAttributes) parentEdgeAttributes.clone ();
 
     } // ctor
 
     Graph2D getSubGraph () { return subGraph; }
     GraphObjAttributes getNodeAttributes () { return newNodeAttributes; }
     GraphObjAttributes getEdgeAttributes () { return newEdgeAttributes; }
 
 } // inner class SelectedSubGraphMaker
 //------------------------------------------------------------------------------
 protected class ShowConditionAction extends AbstractAction   {
   String conditionName;
   ShowConditionAction (String conditionName) { 
     super (conditionName);
     this.conditionName = conditionName;
     }
 
   public void actionPerformed (ActionEvent e) {
     }
 }
 //------------------------------------------------------------------------------
 protected void loadGML (String filename)
 {
     setGraph (FileReadingAbstractions.loadGMLBasic(filename,edgeAttributes));
     FileReadingAbstractions.initAttribs (bioDataServer, getDefaultSpecies (),
                                          config,graph,nodeAttributes,edgeAttributes);
     displayCommonNodeNames (); // fills in canonical name for blank common names
     geometryFilename = filename;
     setWindowTitle(filename);
     loadPlugins();
     displayNewGraph (false);
 
 } // loadGML
 //------------------------------------------------------------------------------
 protected void loadInteraction (String filename)
 {
   setGraph (FileReadingAbstractions.loadIntrBasic (bioDataServer, getDefaultSpecies (), 
                                                    filename,edgeAttributes));
   FileReadingAbstractions.initAttribs (bioDataServer, getDefaultSpecies (), config,
                                        graph,nodeAttributes,edgeAttributes);
   displayCommonNodeNames (); // fills in canonical name for blank common names
   geometryFilename = null;
   setWindowTitle(filename);
   loadPlugins();
   displayNewGraph (true);
 } // loadInteraction
 
 
 /**
  * Load the named expression data filename.
  *
  * added by dramage 2002-08-21
  */
 protected void loadExpressionData (String filename) {
     expressionData = new ExpressionData (filename);
 
     config.addNodeAttributeFilename(filename);
 
     // update plugin list
     loadPlugins();
 }
 
 //------------------------------------------------------------------------------
 protected class ExitAction extends AbstractAction  {
   ExitAction () { super ("Exit"); }
 
   public void actionPerformed (ActionEvent e) {
     parentApp.exit (0);
   }
 }
 //------------------------------------------------------------------------------
 protected class CloseWindowAction extends AbstractAction  {
   CloseWindowAction () { super ("Close"); }
 
   public void actionPerformed (ActionEvent e) {
     mainFrame.dispose ();
   }
 }
 //------------------------------------------------------------------------------
 /**
  *  write out the current graph to the specified file, using the standard
  *  interactions format:  nodeA edgeType nodeB.
  *  for example: <code>
  *
  *     YMR056C pp YLL013C
  *     YCR107W pp YBR265W
  *
  *  </code>  
  */
 protected class SaveAsInteractionsAction extends AbstractAction  
 {
   SaveAsInteractionsAction () {super ("As Interactions..."); }
 
   public void actionPerformed (ActionEvent e) {
     JFileChooser chooser = new JFileChooser (currentDirectory);
     if (chooser.showSaveDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
       String name = chooser.getSelectedFile ().toString ();
       currentDirectory = chooser.getCurrentDirectory();
       if (!name.endsWith (".sif")) name = name + ".sif";
       try {
         FileWriter fileWriter = new FileWriter (name);
         Node [] nodes = graphView.getGraph2D().getNodeArray();
         for (int i=0; i < nodes.length; i++) {
           StringBuffer sb = new StringBuffer ();
           Node node = nodes [i];
           String canonicalName = getCanonicalNodeName (node);
           if (node.edges().size() == 0)
             sb.append (canonicalName + "\n");
           else {
             EdgeCursor ec = node.outEdges ();
             for (ec.toFirst (); ec.ok (); ec.next ()) {
               Edge edge = ec.edge ();
               Node target = edge.target ();
               String canonicalTargetName = getCanonicalNodeName (target);
               String edgeName = (String)edgeAttributes.getCanonicalName(edge);
               String interactionName =
                  (String)(edgeAttributes.getValue("interaction", edgeName));
               if (interactionName == null) {interactionName = "xx";}
               sb.append (canonicalName);
               sb.append (" ");
               sb.append (interactionName);
               sb.append (" ");
               sb.append (canonicalTargetName);
               sb.append ("\n");
               } // for ec
              } // else: this node has edges, write out one line for every out edge (if any)
            fileWriter.write (sb.toString ());
           }  // for i
           fileWriter.close ();
         } 
       catch (IOException ioe) {
         System.err.println ("Error while writing " + name);
         ioe.printStackTrace ();
         } // catch
       } // if
     }  // actionPerformed
 
 } // SaveAsInteractionsAction
 //------------------------------------------------------------------------------
 protected class SaveAsGMLAction extends AbstractAction  
 {
   SaveAsGMLAction () {super ("As GML..."); }
   public void actionPerformed (ActionEvent e) {
     JFileChooser chooser = new JFileChooser (currentDirectory);
     if (chooser.showSaveDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
       currentDirectory = chooser.getCurrentDirectory();
       String name = chooser.getSelectedFile ().toString ();
       if (!name.endsWith (".gml")) name = name + ".gml";
       GraphProps props = getProps();
       GMLWriter writer = new GMLWriter(props);
       writer.write(name);
     } // if
   }
 } // SaveAsGMLAction
 //------------------------------------------------------------------------------
 protected class LoadGMLFileAction extends AbstractAction {
   LoadGMLFileAction () { super ("GML..."); }
     
   public void actionPerformed (ActionEvent e)  {
    JFileChooser chooser = new JFileChooser (currentDirectory);
    if (chooser.showOpenDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
       currentDirectory = chooser.getCurrentDirectory();
       String name = chooser.getSelectedFile ().toString ();
       geometryFilename = name;
       loadGML (name);
       } // if
     } // actionPerformed
 
 } // inner class LoadAction
 //------------------------------------------------------------------------------
 protected class LoadInteractionFileAction extends AbstractAction {
   LoadInteractionFileAction() { super ("Interaction..."); }
     
   public void actionPerformed (ActionEvent e)  {
    JFileChooser chooser = new JFileChooser (currentDirectory);
    if (chooser.showOpenDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
       currentDirectory = chooser.getCurrentDirectory();
       String name = chooser.getSelectedFile ().toString ();
       loadInteraction (name);
       } // if
     } // actionPerformed
 
 } // inner class LoadAction
 //------------------------------------------------------------------------------
 protected class LoadExpressionMatrixAction extends AbstractAction {
   LoadExpressionMatrixAction () { super ("Expression Matrix File..."); }
     
   public void actionPerformed (ActionEvent e)  {
    JFileChooser chooser = new JFileChooser (currentDirectory);
    if (chooser.showOpenDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
       currentDirectory = chooser.getCurrentDirectory();
       expressionDataFilename = chooser.getSelectedFile ().toString ();
       loadExpressionData (expressionDataFilename);
       // incorporateExpressionData ();
       } // if
     } // actionPerformed
 
 } // inner class LoadExpressionMatrix
 
 
 /**
  * Action allows the loading of a BioDataServer from the gui.
  *
  * added by dramage 2002-08-20
  */
 protected class LoadBioDataServerAction extends AbstractAction {
     LoadBioDataServerAction () { super ("Bio Data Server..."); }
 
     public void actionPerformed (ActionEvent e) {
         JFileChooser chooser = new JFileChooser (currentDirectory);
         chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
 
         if (chooser.showOpenDialog (CytoscapeWindow.this)
             == chooser.APPROVE_OPTION) {
 
             currentDirectory = chooser.getCurrentDirectory();
             String bioDataDirectory = chooser.getSelectedFile().toString();
             //bioDataServer = BioDataServerFactory.create (bioDataDirectory);
             try {
               bioDataServer = new BioDataServer (bioDataDirectory);
               }
             catch (Exception e0) {
               logger.warning ("cannot create new biodata server at " + bioDataDirectory);
               }
             displayCommonNodeNames();
             redrawGraph();
         }
             
     }
 }
 
 
 //------------------------------------------------------------------------------
 protected class DeleteSelectionAction extends AbstractAction {
   DeleteSelectionAction () { super ("Delete Selection"); }
   public void actionPerformed (ActionEvent e) {
       graphView.getGraph2D ().removeSelection ();
 
   redrawGraph ();
     }
   }
 //------------------------------------------------------------------------------
 protected class ZoomAction extends AbstractAction {
   double factor;
   ZoomAction (double factor) {
     super ();
     this.factor = factor;
     }
     
   public void actionPerformed (ActionEvent e) {
     graphView.setZoom (graphView.getZoom ()*factor);
   redrawGraph ();
     }
   }
 //------------------------------------------------------------------------------
 protected class FitContentAction extends AbstractAction  {
    FitContentAction () { super (); }
     public void actionPerformed (ActionEvent e) {
       graphView.fitContent ();
   redrawGraph ();
       }
 }
 //------------------------------------------------------------------------------
 protected class ShowAllAction extends AbstractAction  {
    ShowAllAction () { super (); }
     public void actionPerformed (ActionEvent e) {
         graph.firePreEvent();
         graphHider.unhideAll ();
         graph.firePostEvent();
 
       graphView.fitContent ();
       graphView.setZoom (graphView.getZoom ()*0.9);
   redrawGraph ();
       }
 }
 protected class HideSelectedAction extends AbstractAction  {
     HideSelectedAction () { super (); }
     public void actionPerformed (ActionEvent e) {
         graph.firePreEvent();
         hideSelectedNodes();
         hideSelectedEdges();
         graph.firePostEvent();
     }
 }
 //------------------------------------------------------------------------------
 protected class ZoomSelectedAction extends AbstractAction  {
   ZoomSelectedAction ()  { super (); }
   public void actionPerformed (ActionEvent e) {
     Graph2D g = graphView.getGraph2D ();
     NodeCursor nc = g.selectedNodes (); 
     if (nc.ok ()) { //selected nodes present? 
        Rectangle2D box = g.getRealizer (nc.node ()).getBoundingBox ();
        for (nc.next (); nc.ok (); nc.next ())
         g.getRealizer (nc.node ()).calcUnionRect (box);
         graphView.zoomToArea (box.getX(),box.getY(),box.getWidth(),box.getHeight());
         if (graphView.getZoom () > 2.0) graphView.setZoom (2.0);
         redrawGraph ();
       }
     }
 }
 //------------------------------------------------------------------------------
 protected class CursorTesterAction extends AbstractAction  {
    boolean busy = false;
    CursorTesterAction () {
      super ("Cursor test"); 
      }
    public void actionPerformed (ActionEvent e) {
      if (busy)
        setInteractivity (true);
      else
        setInteractivity (false);
      busy = !busy;
      }
 
 } // CursorTester
 //------------------------------------------------------------------------------
 class EditGraphMode extends EditMode {
   EditGraphMode () { 
    super (); 
    allowNodeCreation (true);
    allowEdgeCreation (true);
    allowBendCreation (true);
    showNodeTips (true);
    showEdgeTips (true);
 
    // added by dramage 2002-08-16
    setMoveSelectionMode(new StraightLineMoveMode());
    }
   protected String getNodeTip (Node node) {
     String geneName = graphView.getGraph2D().getRealizer(node).getLabelText();
     String canonicalName = getCanonicalNodeName (node);
     if (canonicalName != null && canonicalName.length () > 0 && !canonicalName.equals (geneName))
       return geneName + " " + canonicalName;
     return geneName;
     } // getNodeTip
 
   protected void nodeCreated (Node newNode) {
     String defaultName = graphView.getGraph2D().getLabelText (newNode);
     HashMap nodeAttributeBundle = configureNewNode (newNode);
     String commonNameKey = "commonName";
     String commonName = defaultName;
     if (nodeAttributeBundle.containsKey (commonNameKey)) {
       commonName = (String) nodeAttributeBundle.get (commonNameKey);
       NodeRealizer r = graphView.getGraph2D().getRealizer(newNode);
       r.setLabelText (commonName);
       }
     String canonicalName = (String) nodeAttributeBundle.get ("canonicalName");
     if (canonicalName == null || canonicalName.length () < 1)
       canonicalName = commonName;
 
     if (canonicalName == null || canonicalName.length () < 1)
       canonicalName = defaultName;
    
     //nodeAttributes.add (canonicalName, nodeAttributeBundle);
     nodeAttributes.set (canonicalName, nodeAttributeBundle);
     nodeAttributes.addNameMapping (canonicalName, newNode);
     } // nodeCreated
 
   protected void edgeCreated (Edge e) {
     logger.info ("edge created: " + e);
     }
 
   protected String getEdgeTip (Edge edge) {
     return edgeAttributes.getCanonicalName (edge);
     } // getEdgeTip
 
 } // inner class EditGraphMode
 //------------------------------------------------------------------------------
 class CreateEdgeMode extends ViewMode {
   CreateEdgeMode () {
     super ();
     }
   protected void edgeCreated (Edge e) {
     logger.info ("edge created: " + e);
     }
 
 } // CreateEdgeMode 
 //------------------------------------------------------------------------------
 class ReadOnlyGraphMode extends EditMode {
   ReadOnlyGraphMode () { 
    super (); 
    allowNodeCreation (false);
    allowEdgeCreation (false);
    allowBendCreation (false);
    showNodeTips (true);
    showEdgeTips (true);
 
    // added by dramage 2002-08-16
    setMoveSelectionMode(new StraightLineMoveMode());
    }
   protected String getNodeTip (Node node) {
     String geneName = graphView.getGraph2D().getRealizer(node).getLabelText();
     String canonicalName = getCanonicalNodeName (node);
     if (canonicalName != null && canonicalName.length () > 0 && !canonicalName.equals (geneName))
       return geneName + " " + canonicalName;
     return geneName;
     } // getNodeTip
 
   protected String getEdgeTip (Edge edge) {
     return edgeAttributes.getCanonicalName (edge);
     } // getEdgeTip
 
 } // inncer class ReadOnlyGraphMode
 //------------------------------------------------------------------------------
 protected HashMap configureNewNode (Node node)
 {
   OptionHandler options = new OptionHandler ("New Node");
 
   String [] attributeNames = nodeAttributes.getAttributeNames ();
 
   if (attributeNames.length == 0) {
     options.addComment ("commonName is required; canonicalName is optional and defaults to commonName");
     options.addString ("commonName", "");
     options.addString ("canonicalName", "");
     }
   else for (int i=0; i < attributeNames.length; i++) {
     String attributeName = attributeNames [i];
     Class attributeClass = nodeAttributes.getClass (attributeName);
     if (attributeClass.equals ("string".getClass ()))
       options.addString (attributeName, "");
     else if (attributeClass.equals (new Double (0.0).getClass ()))
       options.addDouble (attributeName, 0);
     else if (attributeClass.equals (new Integer (0).getClass ()))
       options.addInt (attributeName, 0);
     } // else/for i
   
   options.showEditor ();
 
   HashMap result = new HashMap ();
 
   if (attributeNames.length == 0) {
     result.put ("commonName", (String) options.get ("commonName"));
     result.put ("canonicalName", (String) options.get ("canonicalName"));
     }
   else for (int i=0; i < attributeNames.length; i++) {
     String attributeName = attributeNames [i];
     Class attributeClass = nodeAttributes.getClass (attributeName);
     if (attributeClass.equals ("string".getClass ()))
        result.put (attributeName, (String) options.get (attributeName));
     else if (attributeClass.equals (new Double (0.0).getClass ()))
        result.put (attributeName, (Double) options.get (attributeName));
     else if (attributeClass.equals (new Integer (0).getClass ()))
        result.put (attributeName, (Integer) options.get (attributeName));
     } // else/for i
 
   return result;
 
 } // configureNode
 
 protected class MainFilterDialogAction extends AbstractAction  {
     MainFilterDialogAction () {
         super(); 
     }
     MainFilterDialogAction (String title) {
         super(title);
     }
 
    public void actionPerformed (ActionEvent e) {
        String[] interactionTypes = getInteractionTypes();
        new MainFilterDialog (CytoscapeWindow.this,
                              mainFrame,
                              graph, nodeAttributes, edgeAttributes,
                              expressionData,
                              graphHider,
                              interactionTypes);
    }
 }
 
 protected class MenuFilterAction extends MainFilterDialogAction  {
     MenuFilterAction () {
         super("Using filters..."); 
     }
 }
 //---------------------------------------------------------------------------------------------------
 protected class EdgeManipulationAction extends AbstractAction {
   EdgeManipulationAction () {
     super ("Select or Hide Edges ");
     }
   public void actionPerformed (ActionEvent e) {
     String [] edgeAttributeNames = edgeAttributes.getAttributeNames ();
     HashMap attributesTree = new HashMap ();
     for (int i=0; i < edgeAttributeNames.length; i++) {
       String name = edgeAttributeNames [i];
       if (edgeAttributes.getClass (name) == "string".getClass ()) {
         String [] uniqueNames = edgeAttributes.getUniqueStringValues (name);
         attributesTree.put (name, uniqueNames);
         } // if a string attribute
       } // for i
     if (attributesTree.size () > 0) {
       JDialog dialog = new EdgeControlDialog (CytoscapeWindow.this, attributesTree, "Control Edges");
       dialog.pack ();
       dialog.setLocationRelativeTo (getMainFrame ());
       dialog.setVisible (true);
       }
     else {
       JOptionPane.showMessageDialog (null, 
          "There are no String edge attributes suitable for controlling edge display");
      }
 
    } // actionPerformed
 
 } // inner class EdgeManipulationAction
 //---------------------------------------------------------------------------------------------------
 protected class EdgeTypeDialogAction extends AbstractAction  {
     EdgeTypeDialogAction () {
         super("Edges by Interaction Type"); 
     }
    public void actionPerformed (ActionEvent e) {
        String[] interactionTypes = getInteractionTypes();
        new EdgeTypeDialogIndep (CytoscapeWindow.this,
                            mainFrame,
                            graph,  edgeAttributes,
                            graphHider,
                            getInteractionTypes());
    }
 }
 
 protected String[] getInteractionTypes() {
     String[] interactionTypes;
     // figure out the interaction types dynamically
     DiscreteMapper typesColor = (DiscreteMapper) vizMapper.getValueMapper(VizMapperCategories.EDGE_COLOR);
     Map typeMap = new HashMap(typesColor.getValueMap());
     Set typeIds = typeMap.keySet();
     interactionTypes = new String[typeIds.size()];
     Iterator iter = typeIds.iterator();
     for(int i = 0; iter.hasNext(); i++) {
         String type = (String)iter.next();
         interactionTypes[i] = type;
     }
     return interactionTypes;
 }
 
 //---------------------------------------------------------------------------------------
 } // CytoscapeWindow
