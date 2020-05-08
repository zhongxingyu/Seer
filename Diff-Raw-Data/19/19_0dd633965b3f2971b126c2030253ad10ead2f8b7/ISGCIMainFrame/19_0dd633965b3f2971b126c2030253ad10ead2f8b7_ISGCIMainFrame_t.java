 /*
  * The main window of ISGCI. Also the class to start the program.
  *
  * $Header: /home/ux/CVSROOT/teo/teo/isgci/gui/ISGCIMainFrame.java,v 2.4 2013/04/07 10:51:04 ux Exp $
  *
  * This file is part of the Information System on Graph Classes and their
  * Inclusions (ISGCI) at http://www.graphclasses.org.
  * Email: isgci@graphclasses.org
  */
 
 package teo.isgci.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Rectangle;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.FileOutputStream;
 import java.io.OutputStreamWriter;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBoxMenuItem;
 import javax.swing.JComponent;
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JViewport;
 import javax.swing.KeyStroke;
 import javax.swing.UIManager;
 import javax.swing.plaf.metal.MetalLookAndFeel;
 
 import org.jgrapht.Graphs;
 import org.jgrapht.graph.SimpleDirectedGraph;
 import org.pushingpixels.flamingo.api.common.icon.ImageWrapperResizableIcon;
 import org.pushingpixels.flamingo.api.ribbon.JRibbonFrame;
 
 import teo.Actions.DeleteSelection;
 import teo.Actions.EditLabel;
 import teo.Actions.ExitAction;
 import teo.Actions.LoadAction;
 import teo.Actions.PrintAction;
 import teo.Actions.SaveAction;
 import teo.data.services.IDataProvider;
 import teo.data.services.XmlDataProvider;
 import teo.data.xml.GraphMLWriter;
 import teo.graph.drawing.IDrawingService;
 import teo.graph.view.GraphView;
 import teo.graph.view.ViewManager;
 import teo.isgci.gc.ForbiddenClass;
 import teo.isgci.gc.GraphClass;
 import teo.isgci.grapht.GAlg;
 import teo.isgci.grapht.Inclusion;
 import teo.isgci.problem.Problem;
 import y.base.DataMap;
 import y.base.Edge;
 import y.base.EdgeCursor;
 import y.base.Node;
 import y.base.NodeCursor;
 import y.base.NodeList;
 import y.geom.OrientedRectangle;
 import y.layout.hierarchic.IncrementalHierarchicLayouter;
 import y.layout.hierarchic.incremental.IncrementalHintsFactory;
 import y.module.YModule;
 import y.option.ConstraintManager;
 import y.option.DefaultEditorFactory;
 import y.option.Editor;
 import y.option.EditorFactory;
 import y.option.GuiFactory;
 import y.option.OptionHandler;
 import y.option.OptionItem;
 import y.util.D;
 import y.util.Maps;
 import y.view.AutoDragViewMode;
 import y.view.BridgeCalculator;
 import y.view.CreateEdgeMode;
 import y.view.DefaultGraph2DRenderer;
 import y.view.DefaultLabelConfiguration;
 import y.view.EdgeLabel;
 import y.view.EdgeRealizer;
 import y.view.EditMode;
 import y.view.GenericNodeRealizer;
 import y.view.GenericNodeRealizer.Factory;
 import y.view.Graph2D;
 import y.view.Graph2DLayoutExecutor;
 import y.view.Graph2DView;
 import y.view.Graph2DViewActions;
 import y.view.Graph2DViewMouseWheelZoomListener;
 import y.view.MovePortMode;
 import y.view.NavigationComponent;
 import y.view.NodeLabel;
 import y.view.NodeRealizer;
 import y.view.Overview;
 import y.view.PopupMode;
 import y.view.ShapeNodeRealizer;
 import y.view.ShinyPlateNodePainter;
 import y.view.SmartEdgeLabelModel;
 import y.view.SmartNodeLabelModel;
 import y.view.TooltipMode;
 import y.view.ViewMode;
 import y.view.YLabel;
 import y.view.hierarchy.HierarchyManager;
 
 /*import teo.isgci.gc.GraphClass;
 import java.util.ArrayList;*/
 
 
 /** The main frame of the application.
  */
 public class ISGCIMainFrame extends JRibbonFrame
         implements WindowListener, ActionListener, ItemListener, IDrawingService {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static final Pattern PATH_SEPARATOR_PATTERN = Pattern.compile("/");
 	
 	private static final Color LABEL_LINE_COLOR = new Color(153, 204, 255, 255);
 	private static final Color LABEL_BACKGROUND_COLOR = Color.WHITE;
 	private static final String AUTO_FLIPPING_CONFIG = "AutoFlipConfig";
 	  
     public static final String APPLICATIONNAME = "ISGCI";
     
     public static final IDataProvider DataProvider = new XmlDataProvider();
     
     public IncrementalHierarchicLayouter layouter;
     public OptionHandler groupLayoutOptions;
 
     public static ISGCIMainFrame tracker; // Needed for MediaTracker (hack)
     public static LatexGraphics latex;
     public static Font font;
 
     protected teo.Loader loader;
     private BridgeCalculator bridgeCalculator;
 
     // The menu
     protected JMenuItem miNew, miExport, miExit;
     protected JMenuItem miNaming, miSearching, miDrawUnproper;
     protected JMenuItem miSelectGraphClasses, miCheckInclusion;
     protected JMenuItem miGraphClassInformation;
     protected JMenuItem miCut, miCopy, miPaste, miDelete, miSelectAll;
     protected JMenu miOpenProblem, miColourProblem;
     protected JMenuItem miSmallgraphs, miHelp, miAbout;
     
     // This is where the drawing goes.
     public JScrollPane drawingPane;
     public ViewManager viewManager;
     public JPanel mainPan;
     public Graph2DView view;
     private static LoadAction gLoader;
 
     
     protected Graph2DView createGraphView() {
         Graph2DView view = new Graph2DView();
         view.setFitContentOnResize(true);
         return view;
       }
     
     public static void applyRealizerDefaults(Graph2D graph, boolean applyDefaultSize, boolean applyFillColor) {
         for(NodeCursor nc = graph.nodes(); nc.ok(); nc.next()) {
           GenericNodeRealizer gnr = new GenericNodeRealizer(graph.getRealizer(nc.node()));
           gnr.setConfiguration(NODE_CONFIGURATION);
           if(applyFillColor) {
             gnr.setFillColor(graph.getDefaultNodeRealizer().getFillColor());
           }
           gnr.setLineColor(null);      
           if(applyDefaultSize) {
             gnr.setSize(graph.getDefaultNodeRealizer().getWidth(), graph.getDefaultNodeRealizer().getHeight());
           }
           NodeLabel label = gnr.getLabel();
           OrientedRectangle labelBounds = label.getOrientedBox();
           SmartNodeLabelModel model = new SmartNodeLabelModel();
           label.setLabelModel(model);
           label.setModelParameter(model.createModelParameter(labelBounds, gnr));
           graph.setRealizer(nc.node(), gnr);      
         }        
       }
     
     public static final String NODE_CONFIGURATION = "myConf";
     
     public static void registerDefaultNodeConfiguration(boolean drawShadows) {
         Factory factory = GenericNodeRealizer.getFactory();
         Map configurationMap = factory.createDefaultConfigurationMap();
 
         ShinyPlateNodePainter painter = new ShinyPlateNodePainter();
         // ShinyPlateNodePainter has an option to draw a drop shadow that is more efficient
         // than wrapping it in a ShadowNodePainter.
         painter.setDrawShadow(drawShadows);
 
         configurationMap.put(GenericNodeRealizer.Painter.class, painter);
         configurationMap.put(GenericNodeRealizer.ContainsTest.class, painter);
         factory.addConfiguration(NODE_CONFIGURATION, configurationMap);
       }
 
     static {
         registerDefaultNodeConfiguration(true);       
       }
     
     /** Creates the frame.
      * @param locationURL The path/URL to the applet/application.
      * @param isApplet true iff the program runs as an applet.
      */
     public ISGCIMainFrame(teo.Loader loader) {
         super(APPLICATIONNAME);
         
         UIManager.installLookAndFeel("JGoodies Windows",
 				"com.jgoodies.looks.windows.WindowsLookAndFeel");
 
 			try {
 			UIManager.setLookAndFeel(new MetalLookAndFeel());
 		} catch (Exception exc) {
 		}
         
         gLoader = new LoadAction(this);
         layouter = new IncrementalHierarchicLayouter();
         layouter.setOrthogonallyRouted(true);
         layouter.setRecursiveGroupLayeringEnabled(false);
         
         /* Load the data */
         try {
 			ISGCIMainFrame.DataProvider.loadData("data/isgci.xml");
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
         setTitle("ISGCI");
         setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         viewManager = new ViewManager();
         viewManager.attachDrawingService(this);
         view = createGraphView();
         
         //ISGCIToolBar tool = new ISGCIToolBar(this);
         
         applyRealizerDefaults(view.getGraph2D(), true, true);
 
         addGlassPaneComponents();
         
         loader.register();
         this.loader = loader;
         tracker = this;
 
         ForbiddenClass.initRules(loader, "data/smallgraphs.xml");
         PSGraphics.init(loader);
         if (latex == null) {
             latex = new LatexGraphics();
             latex.init(loader);
         }
 
         boolean createMaps = false;
         try {
             createMaps = System.getProperty("org.isgci.mappath") != null;
         } catch (Exception e) {}
 
 //        if (createMaps) {       // Create maps and terminate
 //            createCanvasPanel();
 //            new teo.isgci.util.LandMark(this).createMaps();
 //            closeWindow();
 //        }
         
         
         try{
 
             URL resource = ISGCIMainFrame.class.getClassLoader().getResource("images/favicon.png");
             
             ImageWrapperResizableIcon icon = ImageWrapperResizableIcon.getIcon(resource, new Dimension(32, 32));
             
             setApplicationIcon(icon);
             
             //SimpleResizableIcon head = new SimpleResizableIcon(RibbonElementPriority.TOP, 32, 32);
             
             
             } catch( Exception ex) {
             	System.out.println(ex.getMessage());
             }
             
             
             
             final ISGCIRibbon rmb = new ISGCIRibbon(this);
             if (rmb != null) {
             	
             }
         
         /*final JMenuBar jmb = createMenuBar();
         if (jmb != null) {
             rootPane.add(jmb);
           }
          
         setJMenuBar(createMenus());
         */
         getContentPane().add("Center", createMainPanel());
         getContentPane().add("Center", createCanvasPanel());
         
 
         registerListeners();
         setLocation(20, 20);
         pack();
        /* final JToolBar jtb = tool.createToolBar();
         if (jtb != null) {
           mainPan.add(jtb, BorderLayout.NORTH);
         }*/
         setVisible(true);
         
         Graph2D rootGraph = view.getGraph2D();
         //create a hierarchy manager with the given root graph
         createHierarchyManager(rootGraph);
         
 
         //prepare option handler for group layout options
         Object[] groupStrategyEnum = {"Global Layering", "Recursive Layering"};
         Object[] groupAlignmentEnum = {"Top", "Center", "Bottom"};
         groupLayoutOptions = new OptionHandler("Layout Properties");
         ConstraintManager cm = new ConstraintManager(groupLayoutOptions);
         OptionItem gsi = groupLayoutOptions.addEnum("Group Layering Strategy", groupStrategyEnum, 0);
         OptionItem eci = groupLayoutOptions.addBool("Enable Compact Layering", true);
         OptionItem gai = groupLayoutOptions.addEnum("Group Alignment", groupAlignmentEnum, 0);
         cm.setEnabledOnValueEquals(gsi, "Recursive Layering", eci);
         cm.setEnabledOnValueEquals(gsi, "Recursive Layering", gai);
         cm.setEnabledOnCondition(cm.createConditionValueEquals(gsi, "Recursive Layering").and(
             cm.createConditionValueEquals(eci, Boolean.TRUE).inverse()), gai);
 
         bridgeCalculator = new BridgeCalculator();
 
         // register it with the DefaultGraph2DRenderer
         ((DefaultGraph2DRenderer) view.getGraph2DRenderer()).setBridgeCalculator(bridgeCalculator);
         
         view.fitContent();
         view.getRootPane().getInputMap(mainPan.WHEN_IN_FOCUSED_WINDOW)
         .put(KeyStroke.getKeyStroke(KeyEvent.VK_H, 0), "select");
         
         view.getRootPane().getInputMap(mainPan.WHEN_IN_FOCUSED_WINDOW)
         .put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "delete");
         
         view.getRootPane().getInputMap(mainPan.WHEN_IN_FOCUSED_WINDOW)
         .put(KeyStroke.getKeyStroke(KeyEvent.VK_U, 0), "selectUp");
         
         view.getRootPane().getInputMap(mainPan.WHEN_IN_FOCUSED_WINDOW)
         .put(KeyStroke.getKeyStroke(KeyEvent.VK_B, 0), "selectDown");
         
         view.getRootPane().getActionMap().put("select", new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 selectNeighbors();
             }
         });
         
         view.getRootPane().getActionMap().put("selectUp", new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 selectSuperClasses();
             }
         });
         
         view.getRootPane().getActionMap().put("selectDown", new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 selectSubClasses();
             }
         });
         
         view.getRootPane().getActionMap().put("delete", new AbstractAction() {
             @Override
             public void actionPerformed(ActionEvent e) {
                 deleteSelected();
             }
         });
     }
     
     private void deleteSelected() {
     	Graph2D g = view.getGraph2D();
     	NodeCursor n = g.selectedNodes();
     	ArrayList<Node> l = new ArrayList<Node>();
     	Collection<GraphClass> gCol = new ArrayList<GraphClass>();
     	for (int i = 0; i < n.size(); ++i) {
     		Node neigh =  n.node(); 
     		String s = n.current().toString();
     		if (s.contains("<sub>")) {
         		if (s.substring(s.indexOf("<sub>"), s.indexOf("</sub>")).contains(",")) {
         			s = s.replace("<sub>", "_{");
             		s = s.replace("</sub>", "}");
         		} else {
         			s = s.replace("<sub>", "_");
             		s = s.replace("</sub>", "");
         		}
     		}
     		gCol.add(DataProvider.getClass(s));
     		n.next();
     	}
     	this.viewManager.del(ISGCIMainFrame.DataProvider.getNodes(gCol, false, false));
     	
     	view.updateView();
     }
     
     private Overview createOverview(Graph2DView view) {
         Overview ov = new Overview(view);
         //animates the scrolling
         ov.putClientProperty("Overview.AnimateScrollTo", Boolean.TRUE);
         //blurs the part of the graph which can currently not be seen
         ov.putClientProperty("Overview.PaintStyle", "Funky");
         //allows zooming from within the overview
         ov.putClientProperty("Overview.AllowZooming", Boolean.TRUE);
         //provides functionality for navigation via keybord (zoom in (+), zoom out (-), navigation with arrow keys)
         ov.putClientProperty("Overview.AllowKeyboardNavigation", Boolean.TRUE);
         //determines how to differ between the part of the graph that can currently be seen, and the rest
         ov.putClientProperty("Overview.Inverse", Boolean.TRUE);
         ov.setPreferredSize(new Dimension(150, 150));
         ov.setMinimumSize(new Dimension(150, 150));
 
         ov.setBorder(BorderFactory.createEtchedBorder());
         return ov;
       }
     
     private JComponent overview, navigationComponent;
     
     public void addGlassPaneComponents() {
         //get the glass pane
         JPanel glassPane = view.getGlassPane();
         //set an according layout manager
         glassPane.setLayout(new BorderLayout());
 
         JPanel toolsPanel = new JPanel(new GridBagLayout());
         toolsPanel.setOpaque(false);
         toolsPanel.setBackground(null);
         toolsPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 0));
 
         //create and add the overview to the tools panel
         GridBagConstraints gbc = new GridBagConstraints();
         gbc.gridx = 0;
         gbc.anchor = GridBagConstraints.LINE_START;
         gbc.insets = new Insets(0, 0, 16, 0);
         overview = createOverview(view);
         toolsPanel.add(overview, gbc);
 
         //create and add the navigation component to the tools panel
         navigationComponent = createNavigationComponent(view, 20, 30);
         toolsPanel.add(navigationComponent, gbc);
 
         //add the toolspanel to the glass pane
         gbc.gridx = 0;
         gbc.gridy = 0;
         gbc.weightx = 1;
         gbc.weighty = 1;
         gbc.anchor = GridBagConstraints.FIRST_LINE_START;
         JViewport viewport = new JViewport();
         viewport.add(toolsPanel);
         viewport.setOpaque(false);
         viewport.setBackground(null);
         JPanel westPanel = new JPanel(new BorderLayout());
         westPanel.setOpaque(false);
         westPanel.setBackground(null);
         westPanel.add(viewport, BorderLayout.NORTH);
         glassPane.add(westPanel, BorderLayout.WEST);
       }
 
     private NavigationComponent createNavigationComponent(Graph2DView view, double scrollStepSize, int scrollTimerDelay) {
         //create the NavigationComponent itself
         final NavigationComponent navigation = new NavigationComponent(view);
         navigation.setScrollStepSize(scrollStepSize);
         //set the duration between scroll ticks
         navigation.putClientProperty("NavigationComponent.ScrollTimerDelay", new Integer(scrollTimerDelay));
         //set the initial duration until the first scroll tick is triggered
         navigation.putClientProperty("NavigationComponent.ScrollTimerInitialDelay", new Integer(scrollTimerDelay));
         //set a flag so that the fit content button will adjust the viewports in an animated fashion
         navigation.putClientProperty("NavigationComponent.AnimateFitContent", Boolean.TRUE);
 
         //add a mouse listener that will make a semi transparent background, as soon as the mouse enters this component
         navigation.setBackground(new Color(255, 255, 255, 0));
         MouseAdapter navigationToolListener = new MouseAdapter() {
           public void mouseEntered(MouseEvent e) {
             super.mouseEntered(e);
             Color background = navigation.getBackground();
             //add some semi transparent background
             navigation.setBackground(new Color(background.getRed(), background.getGreen(), background.getBlue(), 196));
           }
 
           public void mouseExited(MouseEvent e) {
             super.mouseExited(e);
             Color background = navigation.getBackground();
             //make the background completely transparent
             navigation.setBackground(new Color(background.getRed(), background.getGreen(), background.getBlue(), 0));
           }
         };
         navigation.addMouseListener(navigationToolListener);
 
         //add mouse listener to all sub components of the navigationComponent
         for (int i = 0; i < navigation.getComponents().length; i++) {
           Component component = navigation.getComponents()[i];
           component.addMouseListener(navigationToolListener);
         }
 
         return navigation;
       }
     
     protected HierarchyManager createHierarchyManager(Graph2D rootGraph) {
         return new HierarchyManager(rootGraph);
     }
     
     protected HierarchyManager getHierarchyManager() {
         return view.getGraph2D().getHierarchyManager();
     }
     
     protected EditMode createEditMode() {
     	EditMode editMode = new EditMode();
         // show the highlighting which is turned off by default
         if (editMode.getCreateEdgeMode() instanceof CreateEdgeMode) {
           ((CreateEdgeMode) editMode.getCreateEdgeMode()).setIndicatingTargetNode(true);
         }
         if (editMode.getMovePortMode() instanceof MovePortMode) {
           ((MovePortMode) editMode.getMovePortMode()).setIndicatingTargetNode(true);
         }
 
         //allow moving view port with right drag gesture
         editMode.allowMovingWithPopup(true);
         editMode.allowLabelSelection(true);
         
         //add hierarchy actions to the views popup menu
         editMode.setPopupMode(createPopupMode());
         editMode.getMouseInputMode().setNodeSearchingEnabled(true);
 
         //Add a visual indicator for the target node of an edge creation - makes it easier to
         //see the target for nested graphs
         ViewMode createEdgeMode = editMode.getCreateEdgeMode();
         if (createEdgeMode instanceof CreateEdgeMode) {
           ((CreateEdgeMode) createEdgeMode).setIndicatingTargetNode(true);
         }
         return editMode;
       }
     
     protected void configureDefaultRealizers() {  
 
         NodeRealizer nodeRealizer = view.getGraph2D().getDefaultNodeRealizer();
         nodeRealizer.setSize(200, 100);
         final NodeLabel nodeLabel = nodeRealizer.getLabel();
         nodeLabel.setText("Smart Node Label");
         nodeLabel.setLineColor(LABEL_LINE_COLOR);
         nodeLabel.setBackgroundColor(LABEL_BACKGROUND_COLOR);
         final SmartNodeLabelModel nodeLabelModel = new SmartNodeLabelModel();
         nodeLabel.setLabelModel(nodeLabelModel);
         nodeLabel.setModelParameter(nodeLabelModel.getDefaultParameter());
 
         final YLabel.Factory factory = EdgeLabel.getFactory();
         final Map defaultConfigImplementationsMap = factory.createDefaultConfigurationMap();
         DefaultLabelConfiguration customLabelConfig = new DefaultLabelConfiguration();
         customLabelConfig.setAutoFlippingEnabled(true);
         defaultConfigImplementationsMap.put(YLabel.Painter.class, customLabelConfig);
         defaultConfigImplementationsMap.put(YLabel.Layout.class, customLabelConfig);
         defaultConfigImplementationsMap.put(YLabel.BoundsProvider.class, customLabelConfig);
         factory.addConfiguration(AUTO_FLIPPING_CONFIG, defaultConfigImplementationsMap);
 
         EdgeRealizer edgeRealizer = view.getGraph2D().getDefaultEdgeRealizer();
         final EdgeLabel edgeLabel = edgeRealizer.getLabel();
         edgeLabel.setText("Smart Edge Label");
         edgeLabel.setLineColor(LABEL_LINE_COLOR);
         edgeLabel.setBackgroundColor(LABEL_BACKGROUND_COLOR);
         final SmartEdgeLabelModel edgeLabelModel = new SmartEdgeLabelModel();
         edgeLabel.setLabelModel(edgeLabelModel);
         edgeLabel.setModelParameter(edgeLabelModel.createDiscreteModelParameter(SmartEdgeLabelModel.POSITION_CENTER));
         edgeLabel.setConfiguration(AUTO_FLIPPING_CONFIG);
       }
     
     
     private void configureNodeLabel(NodeLabel label, int position) {
         SmartNodeLabelModel model = new SmartNodeLabelModel();
         label.setLabelModel(model);
         label.setModelParameter(model.createDiscreteModelParameter(position));
       }
     
     /**
      * Layout all included graphs.
      */
     public void dolayout() {
         layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
         final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
         layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
         layoutExecutor.doLayout(view, layouter);
        view.fitContent();
       }
     
      
     public void layoutIncrementally() {
         Graph2D graph = view.getGraph2D();
 
         layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_INCREMENTAL);
 
         // create storage for both nodes and edges
         DataMap incrementalElements = Maps.createHashedDataMap();
         // configure the mode
         final IncrementalHintsFactory ihf = layouter.createIncrementalHintsFactory();
 
         for (NodeCursor nc = graph.selectedNodes(); nc.ok(); nc.next()) {
           incrementalElements.set(nc.node(), ihf.createLayerIncrementallyHint(nc.node()));
         }
 
         for (EdgeCursor ec = graph.selectedEdges(); ec.ok(); ec.next()) {
           incrementalElements.set(ec.edge(), ihf.createSequenceIncrementallyHint(ec.edge()));
         }
         graph.addDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY, incrementalElements);
         try {
           final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
           layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
           layoutExecutor.doLayout(view, layouter);
         } finally {
           graph.removeDataProvider(IncrementalHierarchicLayouter.INCREMENTAL_HINTS_DPKEY);
         }
       }
     
     /**
      * Write the entire database in GraphML to isgcifull.graphml.
      */
     private void writeGraphML() {
         OutputStreamWriter out = null;
 
         SimpleDirectedGraph<GraphClass, Inclusion> g =
             new SimpleDirectedGraph<GraphClass, Inclusion>(Inclusion.class);
         Graphs.addGraph(g, ISGCIMainFrame.DataProvider.getInclusionGraph());
         GAlg.transitiveReductionBruteForce(g);
 
         try {
             out = new OutputStreamWriter(
                     new FileOutputStream("isgcifull.graphml"), "UTF-8");
             GraphMLWriter w = new GraphMLWriter(out,
                         GraphMLWriter.MODE_PLAIN,
                         true,
                         false);
             w.startDocument();
             for (GraphClass gc : g.vertexSet()) {
                 w.writeNode(gc.getID(), gc.toString(), Color.WHITE);
             }
             for (Inclusion e : g.edgeSet()) {
                 w.writeEdge(e.getSuper().getID(), e.getSub().getID(),
                         e.isProper());
             }
             w.endDocument();
             out.close();
         } catch (Exception e) {
             e.printStackTrace();
         }
     }
 
 
     /**
      * Creates and attaches the necessary eventlisteners.
      */
     protected void registerListeners() {
         setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
         addWindowListener(this);
         /*miNew.addActionListener(this);
         miExport.addActionListener(this);
         miExit.addActionListener(this);
         miNaming.addActionListener(this);
         miSearching.addActionListener(this);
         miDrawUnproper.addItemListener(this);
         miSelectGraphClasses.addActionListener(this);
         miCheckInclusion.addActionListener(this);
         miGraphClassInformation.addActionListener(this);
         //miDelete.addActionListener(this);
         //miSelectAll.addActionListener(this);
         miOpenProblem.addActionListener(this);
         miSmallgraphs.addActionListener(this);
         miHelp.addActionListener(this);
         miAbout.addActionListener(this);*/
         
         Graph2DViewMouseWheelZoomListener wheelZoomListener = new Graph2DViewMouseWheelZoomListener();
         //zoom in/out at mouse pointer location 
         wheelZoomListener.setCenterZooming(false);
         view.getCanvasComponent().addMouseWheelListener(wheelZoomListener);
     }
 
     protected Action createLoadAction() {
         return new LoadAction(this);
       }
 
       protected Action createSaveAction() {
         return new SaveAction(this);
       }
 
       protected Action createDeleteSelectionAction() {
         return new DeleteSelection(view);
       }
     
     protected JMenuBar createMenuBar() {
     	JMenuBar menuBar = new JMenuBar();
         JMenu menu = new JMenu("File");
         Action action;
         action = createLoadAction();
         if (action != null) {
           menu.add(action);
         }
         action = createSaveAction();
         if (action != null) {
           menu.add(action);
         }
         menu.addSeparator();
         menu.add(new PrintAction(this));
         menu.addSeparator();
         menu.add(new ExitAction());
         menuBar.add(menu);
         
         if (getExampleResources() != null && getExampleResources().length != 0) {
           createExamplesMenu(menuBar);
         }
         
         return menuBar;
       }
     
     protected void createExamplesMenu(JMenuBar menuBar) {
         final String[] fileNames = getExampleResources();
         if (fileNames == null) {
           return;
         }
 
         final JMenu menu = new JMenu("Example Graphs");
         menuBar.add(menu);
 
         for (int i = 0; i < fileNames.length; i++) {
           final String filename = fileNames[i];
           final String[] path = PATH_SEPARATOR_PATTERN.split(filename);
           menu.add(new AbstractAction(path[path.length - 1]) {
             public void actionPerformed(ActionEvent e) {
             	gLoader.loadGraph(filename);
             }
           });
         }
       }
     protected String[] getExampleResources() {
         return null;
       }
     /**
      * Creates the menu system.
      * @return The created JMenuBar
      * @see JMenuBar
      */
     protected JMenuBar createMenus() {
         JMenuBar mainMenuBar = new JMenuBar();
         JMenu fileMenu, editMenu, viewMenu,  graphMenu, helpMenu, problemsMenu;
         JMenuItem menu;
 
         fileMenu = new JMenu("File");
         fileMenu.add(miNew = new JMenuItem("New window"));
         fileMenu.add(miExport = new JMenuItem("Export drawing..."));
         fileMenu.add(miExit = new JMenuItem("Exit"));
         mainMenuBar.add(fileMenu);
 
         /*editMenu = new Menu("Edit");
         editMenu.add(miDelete = new MenuItem("Delete"));
         editMenu.add(miSelectAll = new MenuItem("Select all"));
         mainMenuBar.add(editMenu);*/
 
         viewMenu = new JMenu("View");
         viewMenu.add(miSearching = new JMenuItem("Search in drawing..."));
         viewMenu.add(miNaming = new JMenuItem("Naming preference..."));
         viewMenu.add(miDrawUnproper =
                 new JCheckBoxMenuItem("Mark unproper inclusions", true));
         /* menu = new ScaleMenu();
         menu.setEnabled(false);
         viewMenu.add(menu); */
         mainMenuBar.add(viewMenu);
 
         graphMenu = new JMenu("Graph classes");
         miGraphClassInformation = new JMenuItem("Browse Database");
         graphMenu.add(miGraphClassInformation);
         miCheckInclusion = new JMenuItem("Find Relation...");
         graphMenu.add(miCheckInclusion);
         miSelectGraphClasses = new JMenuItem("Draw...");
         graphMenu.add(miSelectGraphClasses);
         mainMenuBar.add(graphMenu);
 
         problemsMenu = new JMenu("Problems");
         miOpenProblem = new JMenu("Boundary/Open classes");
         problemsMenu.add(miOpenProblem);
         Problem[] problems = ISGCIMainFrame.DataProvider.getProblems();
         for (int i = 0; i < problems.length; i++) {
             menu = new JMenuItem(
                     ((Problem) problems[i]).getName());
             miOpenProblem.add(menu);
             menu.addActionListener(this);
             menu.setActionCommand("miOpenProblem");
         }
         miColourProblem = new ProblemsMenu(this, "Colour for problem");
         problemsMenu.add(miColourProblem);
         mainMenuBar.add(problemsMenu);
 
         
         helpMenu = new JMenu("Help");
         miSmallgraphs = new JMenuItem("Small graphs");
         helpMenu.add(miSmallgraphs);
         miHelp = new JMenuItem("Help");
         helpMenu.add(miHelp);
         miAbout = new JMenuItem("About");
         helpMenu.add(miAbout);
         //mainMenuBar.add(Box.createHorizontalGlue());
         mainMenuBar.add(helpMenu);
         
         return mainMenuBar;
     }
     
     protected void populateGroupingPopup(JPopupMenu pm, final double x, final double y, Node node, boolean selected) {
         
     	add(selectItem = new JMenuItem("Show Neighbors"));
         selectItem.addActionListener(this);
         
         add(selectItemUp = new JMenuItem("Show SuperClasses"));
         selectItemUp.addActionListener(this);
         
         add(selectItemDown = new JMenuItem("Show Subclasses"));
         selectItemDown.addActionListener(this);
           
           add(infoItem = new JMenuItem("Information"));
           infoItem.addActionListener(this);  
         
           add(addUp = new JMenuItem("Add Superclasses"));
           addUp.addActionListener(this);
           
           add(addDown = new JMenuItem("Add Subclasses"));
           addDown.addActionListener(this);
           
         add(infoItem = new JMenuItem("Information"));
         infoItem.addActionListener(this);       
         
         pm.add(selectItem);
         pm.add(selectItemUp);
         pm.add(selectItemDown);
 
         pm.addSeparator();
         
         pm.add(addUp);
         pm.add(addDown);
         
         pm.addSeparator();
         
         pm.add(infoItem);
       }
     
     JMenuItem  addUp;
     JMenuItem  addDown;
     JMenuItem  infoItem;
     JMenuItem  selectItem;
     JMenuItem  selectItemUp;
     JMenuItem  selectItemDown;
     
     /**
      * Creates the drawing canvas with scrollbars at the bottom and at the
      * right.
      * @return the panel
      */
     protected JComponent createCanvasPanel() {
         mainPan = new JPanel();
         mainPan.setLayout(new BorderLayout());
 
         registerViewModes();
 
         //mainPan.add(graphCanvas, BorderLayout.CENTER);
         mainPan.add(view, BorderLayout.CENTER);
         
         return mainPan;
     }
     
     protected PopupMode createPopupMode() {
         return new HierarchicPopupMode();
       }
     
      
     private Action createDeleteSelectionActionImpl() {
         final Graph2DViewActions.DeleteSelectionAction action =
                 new Graph2DViewActions.DeleteSelectionAction(view);
         action.setKeepingParentGroupNodeSizes(true);
         return action;
       }
     
     
     protected TooltipMode createTooltipMode() {
         TooltipMode tooltipMode = new TooltipMode();
         tooltipMode.setEdgeTipEnabled(true);
        
         return tooltipMode;
       }
    
     protected void registerViewModes() {
     	
     	EditMode editMode = createEditMode();
         editMode.allowNodeCreation(false);
         editMode.allowEdgeCreation(false);
         editMode.allowNodeEditing(false);
  
         
         if (editMode != null) {
           view.addViewMode(editMode);
         }
     	
     	
         view.addViewMode(new AutoDragViewMode()); 
 
         TooltipMode tooltipMode = createTooltipMode();
         if(tooltipMode != null) {
           view.addViewMode(tooltipMode);
         }
 
         //view.addViewMode(new AutoDragViewMode());
         /*
         editMode.setPopupMode(new PopupMode() {
             public JPopupMenu getEdgePopup(Edge e) {
               JPopupMenu pm = new JPopupMenu();
               pm.add(new EditLabel(e, par));
               return pm;
             }
           });*/
       }
     
     /**
      * Creates the drawing canvas with scrollbars at the bottom and at the
      * right.
      * @return the panel
      */
     protected JComponent createMainPanel() {
         drawingPane = new JScrollPane(
                 JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                 JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
         
         drawingPane.getHorizontalScrollBar().setUnitIncrement(100);
         drawingPane.getVerticalScrollBar().setUnitIncrement(100);
         
         return drawingPane;
     }
 
 
     /**
      * Center the canvas on the given point.
      */
     public void centerCanvas(Point p) {
         JViewport viewport = drawingPane.getViewport();
         Dimension port = viewport.getExtentSize();
         Dimension view = viewport.getViewSize();
 
         p.x -= port.width/2;
         if (p.x + port.width > view.width)
             p.x = view.width - port.width;
         if (p.x < 0)
             p.x = 0;
         p.y -= port.height/2;
         if (p.y + port.height > view.height)
             p.y = view.height - port.height;
         if (p.y < 0)
             p.y = 0;
         viewport.setViewPosition(p);
     }
 
     public void printPort() {
         Rectangle view = getViewport();
         System.err.println("port: "+view);
     }
     
     public void exportGRAPHICS(PSGraphics g) {
     	g.drawImage(view.getImage(), 0, 0, view);
     }
     
     public void exportSGRAPHICS(SVGGraphics g) {
     	 	
     }
 
     public Rectangle getViewport() {
         return drawingPane.getViewport().getViewRect();
     }
 
     
     /** Closes the window and possibly terminates the program. */
     public void closeWindow() {
         setVisible(false);
         dispose();
         loader.unregister();
     }
 
     /**
      * Eventhandler for window events
      */
     public void windowClosing(WindowEvent e) {
         closeWindow();
     }
 
     /**
      * Required to overload (abstract)
      */
     public void windowOpened(WindowEvent e) {}
     public void windowClosed(WindowEvent e) {}
     public void windowIconified(WindowEvent e) {}
     public void windowDeiconified(WindowEvent e) {}
     public void windowDeactivated(WindowEvent e) {}
     public void windowActivated(WindowEvent e) {}
 
     
     public void selectNeighbors() {
     	Graph2D g = view.getGraph2D();
     	NodeCursor n = g.selectedNodes();
     	ArrayList<Node> l = new ArrayList<Node>();
     	
     	for (int i = 0; i < n.size(); ++i) {
     		NodeCursor neigh =  n.node().neighbors(); 
     		for (int j = 0; j < neigh.size(); ++j) {
     			l.add(neigh.node());
     			neigh.next();
     		}
     		n.next();
     	}
     	for (Node no: l) {
     		view.getGraph2D().setSelected(no, true);
     	}
     	view.updateView();
     }
     
     public void selectSuperClasses() {
     	Graph2D g = view.getGraph2D();
     	NodeCursor n = g.selectedNodes();
     	ArrayList<Node> l = new ArrayList<Node>();
     	
     	for (int i = 0; i < n.size(); ++i) {
     		NodeCursor neigh =  n.node().successors(); 
     		for (int j = 0; j < neigh.size(); ++j) {
     			l.add(neigh.node());
     			neigh.next();
     		}
     		n.next();
     	}
     	for (Node no: l) {
     		view.getGraph2D().setSelected(no, true);
     	}
     	view.updateView();
     }
     
     public void selectSubClasses() {
     	Graph2D g = view.getGraph2D();
     	NodeCursor n = g.selectedNodes();
     	ArrayList<Node> l = new ArrayList<Node>();
     	
     	for (int i = 0; i < n.size(); ++i) {
     		NodeCursor neigh =  n.node().predecessors(); 
     		for (int j = 0; j < neigh.size(); ++j) {
     			l.add(neigh.node());
     			neigh.next();
     		}
     		n.next();
     	}
     	for (Node no: l) {
     		view.getGraph2D().setSelected(no, true);
     	}
     	view.updateView();
     }
     
     
     public void addSuperClasses() {
     	Graph2D g = view.getGraph2D();
     	NodeCursor n = g.selectedNodes();
     	ArrayList<Node> l = new ArrayList<Node>();
     	Collection<GraphClass> gCol = new ArrayList<GraphClass>();
     	for (int i = 0; i < n.size(); ++i) {
     		Node neigh =  n.node(); 
     		String s = n.current().toString();
     		if (s.contains("<sub>")) {
         		if (s.substring(s.indexOf("<sub>"), s.indexOf("</sub>")).contains(",")) {
         			s = s.replace("<sub>", "_{");
             		s = s.replace("</sub>", "}");
         		} else {
         			s = s.replace("<sub>", "_");
             		s = s.replace("</sub>", "");
         		}
     		}
     		gCol.add(DataProvider.getClass(s));
     		n.next();
     	}
     	this.viewManager.add(ISGCIMainFrame.DataProvider.getNodes(gCol, true, false));
     	
     	view.updateView();
     }
     
     public void addSubClasses() {
     	Graph2D g = view.getGraph2D();
     	NodeCursor n = g.selectedNodes();
     	ArrayList<Node> l = new ArrayList<Node>();
     	Collection<GraphClass> gCol = new ArrayList<GraphClass>();
     	for (int i = 0; i < n.size(); ++i) {
     		Node neigh =  n.node(); 
     		String s = n.current().toString();
     		if (s.contains("<sub>")) {
         		if (s.substring(s.indexOf("<sub>"), s.indexOf("</sub>")).contains(",")) {
         			s = s.replace("<sub>", "_{");
             		s = s.replace("</sub>", "}");
         		} else {
         			s = s.replace("<sub>", "_");
             		s = s.replace("</sub>", "");
         		}
     		}
     		gCol.add(DataProvider.getClass(s));
     		n.next();
     	}
     	this.viewManager.add(ISGCIMainFrame.DataProvider.getNodes(gCol, false, true));
     	
     	view.updateView();
     }
     
     /**
      * Eventhandler for menu selections
      */
     public void actionPerformed(ActionEvent event) {
         Object object = event.getSource();
 
         Object source = event.getSource();
         if (source == infoItem) {
         	String sub = view.getGraph2D().selectedNodes().node().toString();
         	JDialog d;
         	if (sub.length() >= 0) {
         		//ToDo: Catch unknown Sequences (\cap ... )
         	/*sub = sub.substring(6, sub.indexOf("</html>"));
         	if (sub.indexOf('?') >= 0) {
         		String newSub = sub.substring(0, sub.indexOf(" "));
         		newSub += "$\\cap$ ";
         		newSub += sub.substring(sub.indexOf("?") + 1);
         		sub = newSub;
         		
         	}*/
         		sub = sub.replace("<html>", "");
         		sub = sub.replace("</html>", "");
         		
         		if (sub.contains("<sub>")) {
 	        		if (sub.substring(sub.indexOf("<sub>"), sub.indexOf("</sub>")).contains(",")) {
 	        			sub = sub.replace("<sub>", "_{");
 	            		sub = sub.replace("</sub>", "}");
 	        		} else {
 	        			sub = sub.replace("<sub>", "_");
 	            		sub = sub.replace("</sub>", "");
 	        		}
         		}
         		
         		
             d = new GraphClassInformationDialog(
                     this, ISGCIMainFrame.DataProvider.getClass(sub));
         	} else {
   		    		d = new GraphClassInformationDialog(
   		    				this, ISGCIMainFrame.DataProvider.getClass(
                		view.getGraph2D().selectedNodes().current().toString()));
         	}
         d.setLocation(50, 50);
         d.pack();
         d.setSize(800, 600);
         d.setVisible(true);
             
         } else if (object == miExit) {
             closeWindow();
         } else if (object == selectItem) {
             selectNeighbors();
         } else if (object == selectItemUp) {
             selectSuperClasses();
         } else if (object == selectItemDown) {
             selectSubClasses();
         } else if (object == addUp) {
         	addSuperClasses();
         } else if (object == addDown) {
         	addSubClasses();
         } else if (object == miNew) {
             new ISGCIMainFrame(loader);
         } else if (object == miExport) {
         	// NOTE
         	viewManager.export();
             JDialog export = new ExportDialog(this);
             export.setLocation(50, 50);
             export.pack();
             export.setVisible(true);
         } else if (object == miNaming) {
             JDialog d = new NamingDialog(this);
             d.setLocation(50,50);
             d.pack();
             d.setVisible(true);
         } else if (object == miSearching) {
             JDialog search = new SearchDialog(this);
             search.setLocation(50,50);
             search.setVisible(true);
         } else if (object == miGraphClassInformation) {
             JDialog info = new GraphClassInformationDialog(this);
             info.setLocation(50, 50);
             info.pack();
             info.setSize(800, 600);
             info.setVisible(true);
         } else if (object == miCheckInclusion) {
             JDialog check = new CheckInclusionDialog(this);
             check.setLocation(50, 50);
             check.pack();
             check.setSize(700, 400);
             check.setVisible(true);
         } else if (object == miSelectGraphClasses) {
             JDialog select = new GraphClassSelectionDialog(this);
             select.setLocation(50, 50);
             select.pack();
             select.setSize(500, 400);
             select.setVisible(true);
         } else if (object == miAbout) {
             JDialog select = new AboutDialog(this);
             select.setLocation(50, 50);
             select.setVisible(true);
         } else if (object == miHelp) {
             loader.showDocument("help.html");
         } else if (object == miSmallgraphs) {
             loader.showDocument("smallgraphs.html");
         } else if (event.getActionCommand() == "miOpenProblem") {
             JDialog open=new OpenProblemDialog(this,
                     ((JMenuItem) event.getSource()).getText());
             open.setLocation(50, 50);
             open.setVisible(true);
         }
     }
 
     public void itemStateChanged(ItemEvent event) {
         Object object = event.getSource();
 
         if (object == miDrawUnproper) {
         	viewManager.setDrawUnproper(
                     ((JCheckBoxMenuItem) object).getState());
         }
     }
         
     public static class OptionSupport {
     private final EditorFactory editorFactory;
 
     /**
      * Displays the OptionHandler of the given module in a dialog and runs the module on the given graph if either the
      * <code>Ok</code> or <code>Apply</code> button were pressed. The creation of the dialog is delegated to {@link
      * #createDialog(y.option.OptionHandler, java.awt.event.ActionListener, java.awt.Frame)}
      *
      * @param module the option handler.
      * @param graph  an optional ActionListener that is added to each button of the dialog.
      * @param owner  the owner of the created dialog.
      */
     public static void showDialog(final YModule module, final Graph2D graph, final boolean runOnOk, Frame owner) {
       if (module.getOptionHandler() == null) {
         module.start(graph);
       } else {
         final ActionListener listener = new ActionListener() {
           public void actionPerformed(ActionEvent e) {
             module.start(graph);
           }
         };
 
         showDialog(module.getOptionHandler(), listener, runOnOk, owner);
       }
     }
 
     /**
      * Displays the given OptionHandler in a dialog and invokes the given ActionListener if either the <code>Ok</code>
      * or <code>Apply</code> button were pressed. The creation of the dialog is delegated to {@link
      * #createDialog(y.option.OptionHandler, java.awt.event.ActionListener, java.awt.Frame)}
      *
      * @param oh       the option handler.
      * @param listener the ActionListener that is invoked if either the <code>Ok</code> or <code>Apply</code> button
      *                 were pressed.
      * @param owner    the owner of the created dialog.
      */
     public static void showDialog(final OptionHandler oh, final ActionListener listener,
                                   final boolean runOnOk, Frame owner) {
       final ActionListener delegatingListener = new ActionListener() {
         public void actionPerformed(final ActionEvent e) {
           final String actionCommand = e.getActionCommand();
           if ("Apply".equals(actionCommand) || (runOnOk && "Ok".equals(actionCommand))) {
             EventQueue.invokeLater(new Runnable() {
               public void run() {
                 listener.actionPerformed(e);
               }
             });
           }
         }
       };
 
       final JDialog dialog = new OptionSupport().createDialog(oh, delegatingListener, owner);
       dialog.setVisible(true);
     }
 
     public OptionSupport() {
       editorFactory = new DefaultEditorFactory();
     }
 
     /**
      * Returns an dialog for the editor of the given option handler which contains four control buttons: Ok, Apply,
      * Reset, and Cancel. An optional ActionListener can be used to add custom behavior to these buttons.
      *
      * @param oh       the option handler.
      * @param listener an optional ActionListener that is added to each button of the dialog.
      * @param owner    the owner of the created dialog.
      */
     public JDialog createDialog(final OptionHandler oh, ActionListener listener, Frame owner) {
       final JDialog dialog = new JDialog(owner, getTitle(oh), true);
 
       final AbstractAction applyAction = new AbstractAction("Apply") {
         public void actionPerformed(ActionEvent e) {
           if (oh.checkValues()) {
             oh.commitValues();
           }
         }
       };
       final JButton applyButton = createButton(applyAction, listener);
       applyButton.setDefaultCapable(true);
       applyButton.requestFocus();
 
       final AbstractAction closeAction = new AbstractAction("Cancel") {
         public void actionPerformed(ActionEvent e) {
           dialog.dispose();
         }
       };
 
       final AbstractAction okAction = new AbstractAction("Ok") {
         public void actionPerformed(ActionEvent e) {
           if (oh.checkValues()) {
             oh.commitValues();
             dialog.dispose();
           }
         }
       };
 
       final AbstractAction resetAction = new AbstractAction("Reset") {
         public void actionPerformed(ActionEvent e) {
           oh.resetValues();
         }
       };
 
       final JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
       buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 11, 5));
       buttonPanel.add(createButton(okAction, listener));
       buttonPanel.add(createButton(closeAction, listener));
       buttonPanel.add(applyButton);
       buttonPanel.add(createButton(resetAction, listener));
 
       final Editor editor = getEditorFactory().createEditor(oh);
       final JComponent editorComponent = editor.getComponent();
       if (editorComponent instanceof JPanel) {
         editorComponent.setBorder(BorderFactory.createEmptyBorder(11, 5, 5, 5));
       } else {
         editorComponent.setBorder(BorderFactory.createEmptyBorder(9, 9, 15, 9));
       }
 
       final JPanel contentPanel = new JPanel(new BorderLayout(0, 0));
       contentPanel.add(editorComponent, BorderLayout.CENTER);
       contentPanel.add(buttonPanel, BorderLayout.SOUTH);
 
       dialog.setContentPane(contentPanel);
       dialog.getRootPane().getInputMap().put(KeyStroke.getKeyStroke("ENTER"), "APPLY_ACTION");
       dialog.getRootPane().getActionMap().put("APPLY_ACTION", applyAction);
       dialog.getRootPane().getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"), "CANCEL_ACTION");
       dialog.getRootPane().getActionMap().put("CANCEL_ACTION", closeAction);
       dialog.getRootPane().setDefaultButton(applyButton);
       dialog.pack();
       dialog.setSize(dialog.getPreferredSize());
       dialog.setResizable(false);
       dialog.setLocationRelativeTo(owner);
 
       return dialog;
     }
 
     protected EditorFactory getEditorFactory() {
       return editorFactory;
     }
 
     String getTitle(OptionHandler oh) {
       final Object title = oh.getAttribute(OptionHandler.ATTRIBUTE_TITLE);
       if (title instanceof String) {
         return (String) title;
       }
 
       final Object titleKey = oh.getAttribute(OptionHandler.ATTRIBUTE_TITLE_KEY);
       if (titleKey instanceof String) {
         return getTitle(oh, (String) titleKey);
       } else {
         return getTitle(oh, oh.getName());
       }
     }
 
     static String getTitle(OptionHandler oh, String title) {
       final GuiFactory guiFactory = oh.getGuiFactory();
       return guiFactory == null ? title : guiFactory.getString(title);
     }
 
     static JButton createButton(AbstractAction action, ActionListener listener) {
       final JButton button = new JButton(action);
       button.addActionListener(listener);
       return button;
 
     }
   }
     
         
       class HierarchicPopupMode extends PopupMode {
     	    public JPopupMenu getPaperPopup(double x, double y) {
     	      JPopupMenu pm = new JPopupMenu();
     	      populateGroupingPopup(pm, x, y, null, false);
     	      return pm;
     	    }
 
     	    public JPopupMenu getNodePopup(Node v) {
     	      Graph2D graph = getGraph2D();
     	      JPopupMenu pm = new JPopupMenu();
     	      populateGroupingPopup(pm, graph.getCenterX(v), graph.getCenterY(v), v, true);
     	      return pm;
     	    }
 
     	    public JPopupMenu getSelectionPopup(double x, double y) {
     	      JPopupMenu pm = new JPopupMenu();
     	      populateGroupingPopup(pm, x, y, null, getGraph2D().selectedNodes().ok());
     	      return pm;
     	    }
     	  }
       
       
       
       
       class OpenFoldersAndLayoutAction extends Graph2DViewActions.OpenFoldersAction {
 
     	    OpenFoldersAndLayoutAction() {
     	      super(ISGCIMainFrame.this.view);
     	    }
 
     	    public void openFolder(Node folderNode, Graph2D graph) {
     	      NodeList children = new NodeList(graph.getHierarchyManager().getInnerGraph(folderNode).nodes());
     	      super.openFolder(folderNode, graph);
     	      graph.unselectAll();
     	      graph.setSelected(folderNode, true);
     	      for (NodeCursor nc = children.nodes(); nc.ok(); nc.next()) {
     	        graph.setSelected(nc.node(), true);
     	      }
 
     	      layoutIncrementally();
 
     	      graph.unselectAll();
     	      graph.setSelected(folderNode, true);
     	      graph.updateViews();
     	     
     	    }
     	  }
 
     	  /**
     	   * Collapse a group node. After collapsing the group node, an incremental layout is automatically triggered.
     	   * For this, the collapsed node is treated as an incremental element.
     	   */
     	  class CloseGroupsAndLayoutAction extends Graph2DViewActions.CloseGroupsAction {
 
     	    CloseGroupsAndLayoutAction() {
     	      super(ISGCIMainFrame.this.view);
     	    }
 
     	    public void closeGroup(Node groupNode, Graph2D graph) {
     	      super.closeGroup(groupNode, graph);
     	      graph.unselectAll();
     	      graph.setSelected(groupNode, true);
     	      for (EdgeCursor ec = groupNode.edges(); ec.ok(); ec.next()) {
     	        graph.setSelected(ec.edge(), true);
     	      }
 
     	      layoutIncrementally();
     	      graph.unselectAll();
 
     	      graph.updateViews();      
     	    }
     	  }
 
 		@Override
 		public void initializeView(List<GraphView> graphs) {
 	        D.bug("entering initializeView");
 	    	view.getGraph2D().clear();
 	    	try {	    	
 	    		//String graphML = this.viewManager.getCurrentGraphML();
 		    	
 		    	//gLoader.loadGraphMLString(graphML);
 	    		gLoader.loadGraph(this.viewManager.getCurrentViews());
 	    	} catch (Exception ex) {
 	    		// TODO
 	    	}
 	    	
 	    	this.refreshView();
 		}
 
 		@Override
 		public void updateView(List<GraphView> graphs) {
 	        D.bug("entering updateView");
 	    	view.getGraph2D().clear();
 	    	try {	    	
 	    		gLoader.loadGraph(this.viewManager.getCurrentViews());
 	    	} catch (Exception ex) {
 	    		// TODO
 	    	}
 	    	
 	    	this.refreshView();
 		}
 	    
 	    public void refreshView() {
 	    
 	    	//Find the maximum width of all used Labels.
 	    	//Alternative: Skip the first run and set the current labelwidth for all node = different nodeswidths
 	    	double maxlen = 0;
 	    	for (NodeCursor nc = view.getGraph2D().nodes(); nc.ok(); nc.next())
 		    {
 	    		Node n = nc.node();
 	    		ShapeNodeRealizer nr = (ShapeNodeRealizer)view.getGraph2D().getRealizer(n);
 		        nr.setShapeType(ShapeNodeRealizer.ROUND_RECT);
 		        
 		        double len = nr.getLabel().getContentWidth();
 
 		        if (len > maxlen) {
 		        	maxlen = len;
 		        	
 		        }
 		        
 		    }
 	    	
 	        for (NodeCursor nc = view.getGraph2D().nodes(); nc.ok(); nc.next())
 	        {
 	          Node n = nc.node();
 	          
 	          ShapeNodeRealizer nr = (ShapeNodeRealizer)view.getGraph2D().getRealizer(n);
 	          nr.setShapeType(ShapeNodeRealizer.ROUND_RECT);
 	          
	          nr.setSize(Math.max(maxlen/2, 30), 20);
 	          nr.getLabel().setFontSize(5);
 	          
 	          configureNodeLabel(nr.getLabel(), SmartNodeLabelModel.POSITION_CENTER);
 	          
 	          nr.repaint();
 	        }
 	        
 	        dolayout();
 	     }
 
 }
 
 
 /* EOF */
