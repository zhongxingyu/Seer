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
 import java.util.Hashtable;
 import java.util.HashMap;
 import java.util.Vector;
 import java.util.Enumeration;
 import java.util.StringTokenizer;
 
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
 import cytoscape.data.readers.*;
 import cytoscape.data.servers.*;
 import cytoscape.dialogs.*;
 import cytoscape.layout.*;
 import cytoscape.vizmap.*;
 import cytoscape.util.MutableString;
 //-----------------------------------------------------------------------------------
 public class CytoscapeWindow extends JPanel { // implements VizChooserClient {
 
   protected static final int DEFAULT_WIDTH = 700;
   protected static final int DEFAULT_HEIGHT = 700;
 
   protected cytoscape parentApp;
   protected Graph2D graph;
   protected String geometryFilename;
   protected String expressionDataFilename;
 
   protected JFrame mainFrame;
   protected JMenuBar menuBar;
   protected JMenu opsMenu, vizMenu, selectMenu, layoutMenu;
   protected JToolBar toolbar;
   protected JLabel infoLabel;
 
 
   protected Cursor defaultCursor = Cursor.getPredefinedCursor (Cursor.DEFAULT_CURSOR);
   protected Cursor busyCursor = Cursor.getPredefinedCursor (Cursor.WAIT_CURSOR);
 
   protected Layouter layouter;
 
   protected Graph2DView graphView;
   protected ViewMode editGraphMode  = new EditGraphMode ();
   protected ViewMode readOnlyGraphMode = new ReadOnlyGraphMode ();
   protected ViewMode currentGraphMode = readOnlyGraphMode;
   protected ViewMode nodeAttributesPopupMode = new NodeAttributesPopupMode ();
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
 
 
   // protected VizChooser theVizChooser = new VizChooser();
 
   protected GraphHider graphHider;
   protected Vector subwindows = new Vector ();
 
   protected String windowTitle;
    // selected nodes can be displayed in a new window.  this next variable
    // provides a title for that new window
   protected String titleForCurrentSelection = null;
   protected CytoscapeConfig config;
 
   protected JMenuItem deleteSelectionMenuItem;
 
 //------------------------------------------------------------------------------
 public CytoscapeWindow (cytoscape parentApp,
                         CytoscapeConfig config,
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
   this.parentApp = parentApp;
   this.graph = graph;
   this.geometryFilename = geometryFilename;
   this.expressionDataFilename = expressionDataFilename;
   this.bioDataServer = bioDataServer;
   this.expressionData = expressionData;
 
   if (nodeAttributes != null) 
     this.nodeAttributes = nodeAttributes;
 
   if (edgeAttributes != null)
     this.edgeAttributes = edgeAttributes;
 
   vizMapperCategories = new VizMapperCategories();
   vizMapper = new AttributeMapper( vizMapperCategories.getInitialDefaults() );
   AttributeMapperPropertiesAdapter adapter =
       new AttributeMapperPropertiesAdapter(vizMapper, vizMapperCategories);
   adapter.applyAllRangeProperties( config.getProperties() );
 
   if (title == null)
     this.windowTitle = "";
   else
     this.windowTitle = title;
   this.config = config;
 
   initializeWidgets ();
 
   displayCommonNodeNames ();
   displayNewGraph (doFreshLayout);
 
   mainFrame.setVisible (true);
   mainFrame.addWindowListener (parentApp);
 
     // load plugins last, after the main window is setup, since they will
     // often need access to all of the parts of a fully instantiated CytoscapeWindow
 
   PluginLoader pluginLoader = new PluginLoader (this, config, nodeAttributes, edgeAttributes);
   pluginLoader.load ();
 
 } // ctor
 //------------------------------------------------------------------------------
 public void windowStateChanged (WindowEvent e)
 {
   System.out.println ("--- windowStateChanged: " + e);
 }
 //------------------------------------------------------------------------------
 public Graph2D getGraph ()
 {  
   return graph;
 }
 //------------------------------------------------------------------------------
 public GraphHider getGraphHider ()
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
   graphView.paintImmediately (0, 0, graphView.getWidth(), graphView.getHeight());
   int nodeCount = graphView.getGraph2D().nodeCount();
   int edgeCount = graphView.getGraph2D().edgeCount();
   infoLabel.setText ("  Nodes: " + nodeCount + " Edges: " + edgeCount);
 
 } // redrawGraph
 //------------------------------------------------------------------------------
 public JFrame getMainFrame ()
 {
   return mainFrame;
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
 protected void displayNewGraph (boolean doLayout)
 {
   if (graph == null) graph = new Graph2D ();
 
   OrganicLayouter ol = new OrganicLayouter ();
   ol.setActivateDeterministicMode (true);
   ol.setPreferredEdgeLength (80);
   layouter = ol;
   graphView.setGraph2D (graph);
   graphHider = new GraphHider (graph);
 
   this.redrawGraph(doLayout);
 
   graphView.fitContent ();
   graphView.setZoom (graphView.getZoom ()*0.9);
 
 } // displayGraph
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
 
   for (int i=0; i < nodes.length; i++) {
     Node node = nodes [i];
     String canonicalName = nodeAttributes.getCanonicalName (node);
     HashMap bundle = nodeAttributes.getAttributes (canonicalName);
     Color nodeColor =
 	vizMapperCategories.getNodeFillColor(bundle, vizMapper);
     Color nodeBorderColor =
 	vizMapperCategories.getNodeBorderColor(bundle, vizMapper);
     int nodeHeight =
 	vizMapperCategories.getNodeHeight(bundle, vizMapper);
     int nodeWidth =
 	vizMapperCategories.getNodeWidth(bundle, vizMapper);
     byte nodeShape =
 	vizMapperCategories.getNodeShape(bundle, vizMapper);
 
     NodeRealizer nr = graphView.getGraph2D().getRealizer(node);
     nr.setFillColor (nodeColor);
     nr.setLineColor(nodeBorderColor);
     nr.setHeight(nodeHeight);
     nr.setWidth(nodeWidth);
     if (nr instanceof ShapeNodeRealizer) {
 	ShapeNodeRealizer snr = (ShapeNodeRealizer)nr;
 	snr.setShapeType(nodeShape);
     }
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
 
 } // renderNodesAndEdges
 //------------------------------------------------------------------------------
 public Node getNode (String canonicalNodeName)
 {
   Node [] nodes = graphView.getGraph2D().getNodeArray();
   for (int i=0; i < nodes.length; i++) {
     Node node = nodes [i];
     String canonicalName = nodeAttributes.getCanonicalName (node);
     // System.out.println (" -- checking " + canonicalNodeName + " against " + canonicalName + " " + node);
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
 public GraphObjAttributes getEdgeAttributes ()
 {
   return edgeAttributes;
 }
 //------------------------------------------------------------------------------
 /**
  * 
  */
 protected void displayCommonNodeNames ()
 {
   if (bioDataServer == null) return;
 
   Node [] nodes = graph.getNodeArray ();
 
   for (int i=0; i < nodes.length; i++) {
     Node node = nodes [i];
     NodeRealizer r = graphView.getGraph2D().getRealizer(node);
     String defaultName = r.getLabelText ();
     String newName = defaultName;
     String canonicalName = getCanonicalNodeName(node);
     try {
       //String [] synonyms = bioDataServer.getSynonyms (defaultName);
       String [] synonyms = bioDataServer.getSynonyms (canonicalName);
       if (synonyms.length > 0) {
         newName = synonyms [0];
         }
       //nodeAttributes.add ("commonName", defaultName, newName);
       nodeAttributes.add ("commonName", canonicalName, newName);
       r.setLabelText (newName);
       }
     catch (Exception ignoreForNow) {;}
     } // for i
 
 } // displayCommonNodeNames
 
 
 /**
  *  displayNodeLabels()
  *  attempts to display the hashed graphObjAttribute value
  *  at key "key" as the label on every node.  Special case
  *  if the key is "canonicalName": canonicalName is stored
  *  in a different data structure, so we access it differently.
  */
 protected void displayNodeLabels (String key)
 {
     Node [] nodes = graph.getNodeArray ();
 
     for (int i=0; i < nodes.length; i++) {
 	Node node = nodes [i];
 	String canonicalName = getCanonicalNodeName(node);
 	String newName = "";
 	if(key!="canonicalName") {
 	    HashMap attribmap = nodeAttributes.getAttributes(canonicalName);
 	    Object newObjectWithName  = (Object)attribmap.get(key);
 	    if(newObjectWithName != null)
 		newName = newObjectWithName.toString();
 	}
 	else
 	    newName = canonicalName;
 	NodeRealizer r = graphView.getGraph2D().getRealizer(node);
 	r.setLabelText (newName);
     }
     
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
   return selectMenu;
 }
 //------------------------------------------------------------------------------
 public JMenu getLayoutMenu ()
 {
   return layoutMenu;
 }
 //------------------------------------------------------------------------------
 public void setInteractivity (boolean newState)
 {
   if (newState == true) { // turn interactivity ON
     if (!viewModesInstalled) {
       graphView.addViewMode (currentGraphMode);
       graphView.addViewMode (nodeAttributesPopupMode);
       viewModesInstalled = true;
       }
     graphView.setViewCursor (defaultCursor);
     setCursor (defaultCursor); 
     }
   else {  // turn interactivity OFF
     if (viewModesInstalled) {
       graphView.removeViewMode (currentGraphMode);
       graphView.removeViewMode (nodeAttributesPopupMode); 
       viewModesInstalled = false;
       }
     graphView.setViewCursor (busyCursor);
     setCursor (busyCursor);
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
 
   JMenu viewMenu = new JMenu ("View");
   viewMenu.add (new HideEdgesAction ());
   viewMenu.add (new ShowEdgesAction ());
   mi = viewMenu.add (new HideSelectedNodesAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_H, ActionEvent.CTRL_MASK));
 
   mi = viewMenu.add (new DisplaySelectedInNewWindowAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_N, ActionEvent.CTRL_MASK));
 
   menuBar.add (viewMenu);
 
   JMenu selectiveDisplayMenu = new JMenu ("Select");
   menuBar.add (selectiveDisplayMenu);
   selectiveDisplayMenu.add (new DeselectAllAction ());
   mi = selectiveDisplayMenu.add (new InvertSelectionAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_K, ActionEvent.CTRL_MASK));
 
   if (bioDataServer != null) selectiveDisplayMenu.add (new GoIDSelectAction ());
   selectiveDisplayMenu.add (new AlphabeticalSelectionAction ());
   selectiveDisplayMenu.add (new ListFromFileSelectionAction ());
 
   mi = selectiveDisplayMenu.add (new SelectFirstNeighborsAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_F, ActionEvent.CTRL_MASK));
 
   mi = selectiveDisplayMenu.add (new DisplayAttributesOfSelectedNodesAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_D, ActionEvent.CTRL_MASK));
   
 
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
   layoutButton.addActionListener(new OrganicLayoutAction ());
   
   layoutButton = new JRadioButtonMenuItem("Embedded");
   layoutGroup.add(layoutButton);
   layoutMenu.add(layoutButton);
   layoutButton.setSelected(true);
   layoutButton.addActionListener(new EmbeddedLayoutAction ());
 
   layoutButton = new JRadioButtonMenuItem("Random");
   layoutGroup.add(layoutButton);
   layoutMenu.add(layoutButton);
   layoutButton.addActionListener(new RandomLayoutAction ());
   
   layoutMenu.addSeparator();
   layoutMenu.add (new LayoutAction ());
   layoutMenu.add (new LayoutSelectionAction ());
 
   layoutMenu.addSeparator();
   JMenu alignSubMenu = new JMenu ("Align Nodes");
   layoutMenu.add(alignSubMenu);
   alignSubMenu.add (new AlignHorizontalAction ());
   alignSubMenu.add (new AlignVerticalAction   ());
 
   vizMenu = new JMenu ("Visualization"); // always create the viz menu
   menuBar.add (vizMenu);
   vizMenu.add (new SetVisualPropertiesAction ());
   //  vizMenu.add (new PrintPropsAction ());
 
   opsMenu = new JMenu ("Ops"); // always create the ops menu
   menuBar.add (opsMenu);
 
 
   if (bioDataServer != null) selectiveDisplayMenu.add (new GoIDSelectAction ());
   selectiveDisplayMenu.add (new AlphabeticalSelectionAction ());
   selectiveDisplayMenu.add (new ListFromFileSelectionAction ());
   selectiveDisplayMenu.add (new ReduceEquivalentNodesAction ());
   
   mi = selectiveDisplayMenu.add (new SelectFirstNeighborsAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_F, ActionEvent.CTRL_MASK));
 
   mi = selectiveDisplayMenu.add (new DisplayAttributesOfSelectedNodesAction ());
   mi.setAccelerator (KeyStroke.getKeyStroke (KeyEvent.VK_D, ActionEvent.CTRL_MASK));
   
   return menuBar;
 
 } // createMenuBar
 //------------------------------------------------------------------------------
 protected JToolBar createToolBar ()
 {
   JToolBar bar = new JToolBar ();
   bar.add (new ZoomAction (0.9));
   bar.add (new ZoomAction (1.1));
   bar.add (new ZoomSelectedAction ());
   bar.add (new FitContentAction ());
   bar.add (new ShowAllAction ());
   // bar.add (new RenderAction ());
 
   bar.addSeparator ();
   // bar.add (new AppearanceControllerLauncherAction (nodeAttributes, edgeAttributes));
     
   return bar;
 
 } // createToolBar
 //------------------------------------------------------------------------------
 public void selectNodesByName (String [] nodeNames)
 {
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
   Graph2D g = graphView.getGraph2D ();
   graphHider.unhideAll ();
   Node [] nodes = graphView.getGraph2D().getNodeArray();
 
   for (int i=0; i < nodes.length; i++) {
     boolean matched = false;
     String graphNodeName = getCanonicalNodeName (nodes [i]);
     for (int n=0; n < nodeNames.length; n++) {
       if (nodeNames [n].equalsIgnoreCase (graphNodeName)) {
         matched = true;
         break;
         } // if equals
        } // for n
      if (!matched) 
        graphHider.hide (nodes [i]);
     } // for i
 
   redrawGraph ();
 
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
   Graph2D g = graphView.getGraph2D();
   Node [] nodes = graphView.getGraph2D().getNodeArray();
 
   for (int i=0; i < nodes.length; i++) {
     String graphNodeName = getCanonicalNodeName (nodes [i]);
     NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [i]);
     boolean matched = false;
     for (int n=0; n < nodeNames.length; n++) {
       // String canonicalName = biodataSever.getCanonicalName (nodes [i]);
       if (nodeNames [n].equalsIgnoreCase (graphNodeName)) {
         matched = true;
         break;
         } // if matched
       } // for n
     if (clearAllSelectionsFirst && !matched)
       nodeRealizer.setSelected (false);
     else if (matched)
       nodeRealizer.setSelected (true);
     } // for i
 
   redrawGraph ();
 
 } // selectNodesByName
 //------------------------------------------------------------------------------
 public void deselectAllNodes ()
 {
   Graph2D g = graphView.getGraph2D();
   Node [] nodes = graphView.getGraph2D().getNodeArray();
 
   for (int i=0; i < nodes.length; i++) {
     NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [i]);
     nodeRealizer.setSelected (false);
     } // for i
 
   redrawGraph ();
 
 } // deselectAllNodes
 protected void invertSelection () {
   Graph2D g = graphView.getGraph2D();
   Node [] nodes = graphView.getGraph2D().getNodeArray();
 
   for (int i=0; i < nodes.length; i++) {
     NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [i]);
     nodeRealizer.setSelected (!nodeRealizer.isSelected());
     } // for i
 
   redrawGraph ();
 }
 
 //------------------------------------------------------------------------------
 protected void selectNodesStartingWith (String key)
 {
   setInteractivity (false);
   key = key.toLowerCase ();
   Graph2D g = graphView.getGraph2D();
   redrawGraph ();
 
   Node [] nodes = graphView.getGraph2D().getNodeArray();
 
   for (int i=0; i < nodes.length; i++) {
     String nodeName = graphView.getGraph2D().getLabelText (nodes [i]);
     boolean matched = false;
     if (nodeName.toLowerCase().startsWith (key))
       matched = true;
     else if (bioDataServer != null) {
       try {
         String [] synonyms = bioDataServer.getSynonyms (nodeName);
         for (int s=0; s < synonyms.length; s++)
           if (synonyms [s].toLowerCase().startsWith (key)) {
             matched = true;
             break;
          } // for s
        }
       catch (Exception ignoreForNow) {;}
       } // else if: checking synonyms
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
         String [] synonyms = bioDataServer.getSynonyms (nodeName);
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
 	    String [] synonyms = bioDataServer.getSynonyms(key);
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
 
 protected void selectNodesSharingGoID (int goID)
 {
   setInteractivity (false);
   Graph2D g = graphView.getGraph2D();
   Node [] nodes = graphView.getGraph2D().getNodeArray();
 
   try {
     for (int n=0; n < nodes.length; n++) {
       String nodeName = graphView.getGraph2D().getLabelText (nodes [n]);
       int [] bioProcessIDs = bioDataServer.getBioProcessIDs (nodeName);
       int [] molFuncIDs = bioDataServer.getMolecularFunctionIDs (nodeName);
       int [] cellularComponentIDs = bioDataServer.getCellularComponentIDs (nodeName);
       int [] allIDs = new int [bioProcessIDs.length +
                                molFuncIDs.length +
                                cellularComponentIDs.length];
       int d=0;  // destination (allIDs) index
   
       for (int i=0; i < bioProcessIDs.length; i++)
         allIDs [d++] = bioProcessIDs [i];
   
       for (int i=0; i < molFuncIDs.length; i++)
         allIDs [d++] = molFuncIDs [i];
   
       for (int i=0; i < cellularComponentIDs.length; i++)
         allIDs [d++] = cellularComponentIDs [i];
   
       Vector allPaths = new Vector ();
       for (int i=0; i < allIDs.length; i++) {
         Vector tmp = bioDataServer.getAllBioProcessPaths (allIDs [i]);
         for (int t=0; t < tmp.size (); t++)
           allPaths.addElement (tmp.elementAt (t));
         } // for i
   
       boolean matched = false;
       for (int v=0; v < allPaths.size (); v++) {
         Vector path = (Vector) allPaths.elementAt (v);
         for (int p=path.size()-1; p >= 0; p--) {
           Integer ID = (Integer) path.elementAt (p);
           int id = ID.intValue ();
           if (id == goID) {
             matched = true;
             // todo: break out of inner and outer loops from here
             }
           } // for p
         } // for v
       NodeRealizer nodeRealizer = graphView.getGraph2D().getRealizer(nodes [n]);
       nodeRealizer.setSelected (matched);
       } // for n
     } // try
   catch (Exception ignoreForNow) {;}
 
   setInteractivity (true);
   redrawGraph ();
 
 } // selectNodesSharingGoId
 //------------------------------------------------------------------------------
 protected void setNodeSelected (Node node, boolean visible)
 {
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
   System.out.print ("starting layout...");  System.out.flush ();
   setInteractivity (false);
   layouter.doLayout (graphView.getGraph2D ());
 
   graphView.fitContent ();
   graphView.setZoom (graphView.getZoom ()*0.9);
 
   setInteractivity (true);
   System.out.println (" done");
 
 } // applyLayout
 
 
 
 // applyLayoutSelection
 //
 // apply layout, but only on currently selected nodes
 protected void applyLayoutSelection() {
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
 	System.out.print ("starting layout...");  System.out.flush ();
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
 	System.out.println("  done");
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
 	  System.out.println(attributeName);
       }
   }
 
 }
 */
 
 
 //------------------------------------------------------------------------------
 protected class SetVisualPropertiesAction extends AbstractAction   {
     MutableString labelKey;
 
     SetVisualPropertiesAction () {
 	super ("Set Visual Properties");
 	labelKey = new MutableString("commonName");
     }
     
     public void actionPerformed (ActionEvent e) {
 	
 	JDialog vizDialog = new VisualPropertiesDialog
 	    (mainFrame, "Set Visual Properties", vizMapper, nodeAttributes, labelKey);
 	vizDialog.pack ();
 	vizDialog.setLocationRelativeTo (mainFrame);
 	vizDialog.setVisible (true);
 	
 	displayNodeLabels(labelKey.getString());
         redrawGraph();
     }
 
 }
 //------------------------------------------------------------------------------
 protected class HideEdgesAction extends AbstractAction   {
   HideEdgesAction () { super ("Hide Edges"); }
 
   public void actionPerformed (ActionEvent e) {
     graphHider.hideEdges ();
     redrawGraph ();
     }
 }
 //------------------------------------------------------------------------------
 protected class HideSelectedNodesAction extends AbstractAction   {
   HideSelectedNodesAction () { super ("Hide Selected Nodes"); }
 
   public void actionPerformed (ActionEvent e) {
       Graph2D g = graphView.getGraph2D ();
       NodeCursor nc = g.selectedNodes (); 
       while (nc.ok ()) {
 	  Node node = nc.node ();
 	  graphHider.hide (node);
 	  nc.next ();
       } // while
       redrawGraph ();
   } // actionPerformed
 }
 //------------------------------------------------------------------------------
 protected class ShowEdgesAction extends AbstractAction   {
   ShowEdgesAction () { super ("Show Edges"); }
 
   public void actionPerformed (ActionEvent e) {
     graphHider.unhideEdges ();
     redrawGraph ();
     }
 }
 //------------------------------------------------------------------------------
 protected class DeleteSelectedAction extends AbstractAction   {
   DeleteSelectedAction () { super ("Delete Selected Nodes and Edges"); }
 
   public void actionPerformed (ActionEvent e) {
     Graph2D g = graphView.getGraph2D ();
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
     redrawGraph ();
     } // actionPerformed
   
 
 } // inner class DeleteSelectedAction
 //------------------------------------------------------------------------------
 protected class LayoutAction extends AbstractAction   {
   LayoutAction () { super ("Whole graph"); }
     
   public void actionPerformed (ActionEvent e) {
     applyLayout (false);
     redrawGraph ();
     }
 }
 
 // lay out selected nodes only - dramage
 protected class LayoutSelectionAction extends AbstractAction {
     LayoutSelectionAction () { super ("Current selection"); }
 
   public void actionPerformed (ActionEvent e) {
       applyLayoutSelection ();
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
   HierarchicalLayoutAction () { super ("Hierarchical"); }
     
   public void actionPerformed (ActionEvent e) {
     HierarchicLayouter hl = new HierarchicLayouter ();
     hl.setMinimalLayerDistance (400);
     hl.setMinimalNodeDistance (40);
     hl.setRoutingStyle(HierarchicLayouter.ROUTE_ORTHOGONAL);
     hl.setLayerer(new AsIsLayerer());
     layouter = hl;
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
 	redrawGraph();
     }
 }
 
 protected class AlignVerticalAction extends AbstractAction {
     AlignVerticalAction () { super ("Vertical"); }
 
     public void actionPerformed (ActionEvent e) {
 
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
 	redrawGraph();
     }
 }
 
 //------------------------------------------------------------------------------
 
 protected class ReduceEquivalentNodesAction extends AbstractAction  {
     ReduceEquivalentNodesAction () {
 	super ("Reduce Equivalent Nodes"); 
     } // ctor
    public void actionPerformed (ActionEvent e) {
        new ReduceEquivalentNodes(nodeAttributes, edgeAttributes, graph);
    }
 }
 
 //-----------------------------------------------------------------------------
 protected class GoIDSelectAction extends AbstractAction   {
   GoIDSelectAction () { super ("By GO ID"); }
 
   public void actionPerformed (ActionEvent e) {
     String answer = 
       (String) JOptionPane.showInputDialog (mainFrame, "Select genes with GO ID");
     if (answer != null && answer.length () > 0) try {
       int goID = Integer.parseInt (answer);
       selectNodesSharingGoID (goID);
       }
     catch (NumberFormatException nfe) {
       JOptionPane.showMessageDialog (mainFrame, "Not an integer: " + answer);
       }
     } // actionPerformed
 
 }// GoIDSelectAction
 //------------------------------------------------------------------------------
 protected class AlphabeticalSelectionAction extends AbstractAction   {
   AlphabeticalSelectionAction () { super ("By Name"); }
 
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
   ListFromFileSelectionAction () { super ("From File"); }
 
     public void actionPerformed (ActionEvent e) {
 	boolean cancelSelectionAction = !useSelectionFile();
     }
 
     private boolean useSelectionFile() {
 	JFileChooser fChooser = new JFileChooser();	
 	fChooser.setDialogTitle("Load Gene Selection File");
 	switch (fChooser.showOpenDialog(null)) {
 		
 	case JFileChooser.APPROVE_OPTION:
 	    File file = fChooser.getSelectedFile();
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
 public boolean saveVisibleNodeNames ()
 {
     Graph2D g = graphView.getGraph2D();
     Node [] nodes = graphView.getGraph2D().getNodeArray();
     
     File file = new File("visibleNodes.txt");
     try {
 	FileWriter fout = new FileWriter(file);
 	for (int i=0; i < nodes.length; i++) {
 	    Node node = nodes [i];
 	    NodeRealizer r = graphView.getGraph2D().getRealizer(node);
 	    String defaultName = r.getLabelText ();
 	    fout.write(r.getLabelText() + "\n");
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
 	boolean itWorked = saveVisibleNodeNames ();
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
 //------------------------------------------------------------------------------
 protected class DeselectAllAction extends AbstractAction   {
   DeselectAllAction () { super ("Deselect All"); }
 
   public void actionPerformed (ActionEvent e) {
     deselectAllNodes ();
     }
 }
 protected InvertSelectionAction createInvertSelectionAction () {
     return new InvertSelectionAction();
 }
 protected class InvertSelectionAction extends AbstractAction {
     InvertSelectionAction () { super ("Invert Selection"); }
 
     public void actionPerformed (ActionEvent e) {
 	invertSelection ();
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
   DisplayAttributesOfSelectedNodesAction () { super ("DEBUG: display attributes of selected nodes"); }
   public void actionPerformed (ActionEvent e) {
     Graph2D g = graphView.getGraph2D ();
     NodeCursor nc = g.selectedNodes (); 
     System.out.println ("debug, selected node count: " + nc.size ());
     for (nc.toFirst (); nc.ok (); nc.next ()) { // get the canonical name of the old node
       String canonicalName = nodeAttributes.getCanonicalName (nc.node ());
       System.out.println (canonicalName + ": " + nodeAttributes.getAttributes (canonicalName));
       } // for
     EdgeCursor ec = g.selectedEdges (); 
     System.out.println ("debug, selected edge count: " + ec.size ());
     for (ec.toFirst (); ec.ok (); ec.next ()) { // get the canonical name of the old node
       String edgeName = edgeAttributes.getCanonicalName (ec.edge ());
       System.out.println (edgeName + ": " + edgeAttributes.getAttributes (edgeName));
       } // for
     }
 }
 //------------------------------------------------------------------------------
 protected class DisplaySelectedInNewWindowAction extends AbstractAction   {
   DisplaySelectedInNewWindowAction () { super ("Display Selected Nodes in New Window"); }
 
   public void actionPerformed (ActionEvent e) {
     SelectedSubGraphFactory factory = new SelectedSubGraphFactory (graph, nodeAttributes, edgeAttributes);
     Graph2D subGraph = factory.getSubGraph ();
     GraphObjAttributes newNodeAttributes = factory.getNodeAttributes ();
     GraphObjAttributes newEdgeAttributes = factory.getEdgeAttributes ();
     //System.out.println ("newEdgeAttributes: " + newEdgeAttributes);
 
     String title = "selection";
     if (titleForCurrentSelection != null) 
       title = titleForCurrentSelection;
     try {
       boolean requestFreshLayout = true;
       CytoscapeWindow newWindow =
           new CytoscapeWindow  (parentApp, config, subGraph, expressionData, 
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
 
 } // inner class DisplaySelectedInNewWindowAction
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
     // System.out.println ("nodes in new subgraph: " + newNodes.length);
 
     newNodeAttributes = (GraphObjAttributes) parentNodeAttributes.clone ();
     newNodeAttributes.clearNameMap ();
     
     for (int i=0; i < newNodes.length; i++) {
       Node newNode = newNodes [i];
       String commonName = subGraph.getLabelText (newNode);
       String canonicalName = (String) parentNameMap.get (commonName);
       NodeRealizer r = subGraph.getRealizer (newNode);
       r.setLabelText (canonicalName);
       newNodeAttributes.addNameMapping (canonicalName, newNode);
       //System.out.println (" new graph, commonName: " + commonName + "   canonical: " + canonicalName); 
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
     //System.out.println ("show " + conditionName);
     // displayNodesWithExpressionValues (conditionName);
     }
 }
 //------------------------------------------------------------------------------
 protected void loadGML (String filename)
 {
     graph=FileReadingAbstractions.loadGMLBasic(filename,edgeAttributes);
     FileReadingAbstractions.initAttribs(config,graph,nodeAttributes,edgeAttributes);
    displayCommonNodeNames (); // fills in canonical name for blank common names
     displayNewGraph (false);
 } // loadGML
 //------------------------------------------------------------------------------
 protected void loadInteraction (String filename)
 {
     graph = FileReadingAbstractions.loadIntrBasic(filename,edgeAttributes);
     FileReadingAbstractions.initAttribs(config,graph,nodeAttributes,edgeAttributes);
    displayCommonNodeNames (); // fills in canonical name for blank common names
     displayNewGraph (true);
 } // loadInteraction
 //------------------------------------------------------------------------------
 protected class ExitAction extends AbstractAction  {
   ExitAction () { super ("Exit"); }
 
   public void actionPerformed (ActionEvent e) {
     parentApp.exit ();
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
     File currentDirectory = new File (System.getProperty ("user.dir"));
     JFileChooser chooser = new JFileChooser (currentDirectory);
     if (chooser.showSaveDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
       String name = chooser.getSelectedFile ().toString ();
       if (!name.endsWith (".intr")) name = name + ".intr";
       try {
         FileWriter fileWriter = new FileWriter (name);
         Node [] nodes = graphView.getGraph2D().getNodeArray();
         for (int i=0; i < nodes.length; i++) {
           Node node = nodes [i];
           String canonicalName = getCanonicalNodeName (node);
           EdgeCursor ec = node.outEdges ();
           for (ec.toFirst (); ec.ok (); ec.next ()) {
              Edge edge = ec.edge ();
              Node target = edge.target ();
              String canonicalTargetName = getCanonicalNodeName (target);
 	     String edgeName = (String)edgeAttributes.getCanonicalName(edge);
 	     String interactionName =
 	    	 (String)(edgeAttributes.getValue("interaction", edgeName));
 	     if (interactionName == null) {interactionName = "xx";}
 	     fileWriter.write (canonicalName + " ");
 	     fileWriter.write (interactionName);
 	     fileWriter.write (" " + canonicalTargetName);
              fileWriter.write ("\n");
              } // for ec
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
   SaveAsGMLAction () {super ("GML..."); }
   public void actionPerformed (ActionEvent e) {
     File currentDirectory = new File (System.getProperty ("user.dir"));
     JFileChooser chooser = new JFileChooser (currentDirectory);
     if (chooser.showSaveDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
       String name = chooser.getSelectedFile ().toString ();
       if (!name.endsWith (".gml")) name = name + ".gml";
       GraphProps props = new GraphProps(graph, nodeAttributes, edgeAttributes);
       GMLWriter writer = new GMLWriter(props);
       writer.write(name);
     } // if
   }
 } // SaveAsGMLAction
 //------------------------------------------------------------------------------
 protected class LoadGMLFileAction extends AbstractAction {
   LoadGMLFileAction () { super ("GML..."); }
     
   public void actionPerformed (ActionEvent e)  {
    File currentDirectory = new File (System.getProperty ("user.dir"));
    JFileChooser chooser = new JFileChooser (currentDirectory);
    if (chooser.showOpenDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
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
    File currentDirectory = new File (System.getProperty ("user.dir"));
    JFileChooser chooser = new JFileChooser (currentDirectory);
    if (chooser.showOpenDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
       String name = chooser.getSelectedFile ().toString ();
       loadInteraction (name);
       } // if
     } // actionPerformed
 
 } // inner class LoadAction
 //------------------------------------------------------------------------------
 protected class LoadExpressionMatrixAction extends AbstractAction {
   LoadExpressionMatrixAction () { super ("Expression Matrix File..."); }
     
   public void actionPerformed (ActionEvent e)  {
    File currentDirectory = new File (System.getProperty ("user.dir"));
    JFileChooser chooser = new JFileChooser (currentDirectory);
    if (chooser.showOpenDialog (CytoscapeWindow.this) == chooser.APPROVE_OPTION) {
       expressionDataFilename = chooser.getSelectedFile ().toString ();
       expressionData = new ExpressionData (expressionDataFilename);
       // incorporateExpressionData ();
       } // if
     } // actionPerformed
 
 } // inner class LoadExpressionMatrix
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
     super ("Zoom " +  (factor > 1.0 ? "In" : "Out"));
     this.factor = factor;
     }
     
   public void actionPerformed (ActionEvent e) {
     graphView.setZoom (graphView.getZoom ()*factor);
   redrawGraph ();
     }
   }
 //------------------------------------------------------------------------------
 protected class FitContentAction extends AbstractAction  {
    FitContentAction () { super ("Fit Content"); }
     public void actionPerformed (ActionEvent e) {
       graphView.fitContent ();
   redrawGraph ();
       }
 }
 //------------------------------------------------------------------------------
 protected class ShowAllAction extends AbstractAction  {
    ShowAllAction () { super ("Show All"); }
     public void actionPerformed (ActionEvent e) {
       graphHider.unhideAll ();
       graphView.fitContent ();
       graphView.setZoom (graphView.getZoom ()*0.9);
   redrawGraph ();
       }
 }
 //------------------------------------------------------------------------------
 protected class ZoomSelectedAction extends AbstractAction  {
   ZoomSelectedAction ()  { super ("Zoom Selected"); }
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
    //System.out.println ("EditGraphMode, createEdgeMode: " + getCreateEdgeMode ());
    //setCreateEdgeMode (new CreateEdgeMode ());
    //System.out.println ("EditGraphMode, createEdgeMode: " + getCreateEdgeMode ());
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
    
     nodeAttributes.add (canonicalName, nodeAttributeBundle);
     nodeAttributes.addNameMapping (canonicalName, newNode);
     } // nodeCreated
 
   protected void edgeCreated (Edge e) {
     System.out.println ("edge created: " + e);
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
     System.out.println ("edge created: " + e);
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
 protected class NodeAttributesPopupMode extends PopupMode {
 
   public JPopupMenu getNodePopup (Node v) {
     NodeRealizer r = graphView.getGraph2D().getRealizer(v);
     JDialog dialog = null;
     r.setSelected (false);
     String nodeName = r.getLabelText ();
     String [] nodeNames = new String [1];
     nodeNames [0] = nodeName;
     String currentCondition = null;
     dialog = new NodeAttributesPopupTable (mainFrame, nodeNames, bioDataServer, 
                                            currentCondition, expressionData,
                                            nodeAttributes);
     dialog.pack ();
     dialog.setLocationRelativeTo (mainFrame);
     dialog.setVisible (true);
     return null;
     }
 
   public JPopupMenu getPaperPopup (double x, double y) {
     return null;
     }
     
   public JPopupMenu getSelectionPopup (double x, double y) {
     Graph2D g = graphView.getGraph2D ();
     NodeCursor nc = g.selectedNodes (); 
     Vector nodeList = new Vector ();
     while (nc.ok ()) {
       Node node = nc.node ();
       NodeRealizer r = graphView.getGraph2D().getRealizer(node);
       String nodeName = r.getLabelText (); 
       nodeList.addElement (nodeName);
       nc.next ();
       }
     if (nodeList.size () > 0) {
       String [] nodeNames = new String [nodeList.size ()];
       for (int i=0; i < nodeList.size (); i++)
         nodeNames [i] = (String) nodeList.elementAt (i);
       JDialog dialog = null;
       String currentCondition = null;
       dialog = new NodeAttributesPopupTable (mainFrame, nodeNames, bioDataServer, 
                                              currentCondition, expressionData,
                                              nodeAttributes);
       dialog.pack ();
       dialog.setLocationRelativeTo (mainFrame);
       dialog.setVisible (true);
       } // if nodeList > 0     
     return null;
     }
 
 } // inner class NodeAttributesPopupMode
 //---------------------------------------------------------------------------------------
 protected HashMap configureNewNode (Node node)
 {
   OptionHandler options = new OptionHandler ("New Node");
 
   String [] attributeNames = nodeAttributes.getAttributeNames ();
   //System.out.println ("attributes: " + attributeNames.length);
 
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
     //System.out.println ("result: " + result);
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
 //---------------------------------------------------------------------------------------
 } // CytoscapeWindow
