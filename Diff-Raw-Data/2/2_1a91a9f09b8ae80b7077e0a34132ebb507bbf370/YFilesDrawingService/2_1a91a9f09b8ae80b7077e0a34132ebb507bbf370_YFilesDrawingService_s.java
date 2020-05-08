 package teo.isgci.yfiles;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.print.PageFormat;
 import java.awt.print.PrinterJob;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.swing.BorderFactory;
 import javax.swing.JComponent;
 import javax.swing.JPanel;
 import javax.swing.JViewport;
 
 import teo.isgci.core.EdgeView;
 import teo.isgci.core.GraphView;
 import teo.isgci.core.IDrawingService;
 import teo.isgci.core.NodeView;
 import teo.isgci.data.gc.GraphClass;
 import teo.isgci.view.gui.ISGCIMainFrame;
 import teo.isgci.view.gui.PSGraphics;
 import y.base.Edge;
 import y.base.Node;
 import y.base.NodeCursor;
 import y.geom.OrientedRectangle;
 import y.io.GraphMLIOHandler;
 import y.io.IOHandler;
 import y.layout.hierarchic.IncrementalHierarchicLayouter;
 import y.option.OptionHandler;
 import y.util.D;
 import y.view.Arrow;
 import y.view.AutoDragViewMode;
 import y.view.BridgeCalculator;
 import y.view.CreateEdgeMode;
 import y.view.DefaultGraph2DRenderer;
 import y.view.EdgeRealizer;
 import y.view.EditMode;
 import y.view.GenericNodeRealizer;
 import y.view.GenericNodeRealizer.Factory;
 import y.view.Graph2D;
 import y.view.Graph2DLayoutExecutor;
 import y.view.Graph2DPrinter;
 import y.view.Graph2DView;
 import y.view.Graph2DViewMouseWheelZoomListener;
 import y.view.HtmlLabelConfiguration;
 import y.view.MovePortMode;
 import y.view.NavigationComponent;
 import y.view.NodeLabel;
 import y.view.NodeRealizer;
 import y.view.Overview;
 import y.view.ShapeNodeRealizer;
 import y.view.ShinyPlateNodePainter;
 import y.view.SmartNodeLabelModel;
 import y.view.ViewMode;
 import y.view.YLabel;
 import yext.export.io.EPSOutputHandler;
 import yext.svg.io.SVGIOHandler;
 
 /**
  * Drawing service class for the yfiles library.
  * 
  * @author Calum *
  */
 public class YFilesDrawingService implements IDrawingService {
 
 	/**
 	 * Holds the yfiles Graph2DView element (which contains
 	 * the Graph2D canvas).
 	 */
 	private Graph2DView graphView = null;
 	/**
 	 * Holds the yfiles Graph2D element.
 	 */
 	private Graph2D graph2D = null;
 	/**
 	 * Holds the layouter object for yfiles graphs.
 	 */
 	private IncrementalHierarchicLayouter layouter = null;
 	/**
 	 * Contains the nodes currently being displayed. Used to 
 	 * allow updates to the graph without redrawing the graph
 	 * completely.
 	 */
 	private Map<NodeView, Node> currentNodes = null;
 	/**
 	 * Contains the edges currently being displayed. Used to 
 	 * allow updates to the graph without redrawing the graph
 	 * completely.
 	 */
 	private List<EdgeView> currentEdges = null;
 	/**
 	 * Contains the improper edges currently being displayed. 
 	 * Used to allow updates to the graph without redrawing
 	 * completely. A map is used here to continue supporting
 	 * a different setting for each graph view currently being
 	 * shown.
 	 */
 	private Map<GraphView, List<Edge>> currentImproperEdges = null;
 	/**
 	 * A map containing the labels of the current nodes and 
 	 * the respective graph classes full name. Enables us to
 	 * find a graph class using the text displayed on the 
 	 * corresponding label.
 	 */
 	private Map<String, String> nodeLabelMap = null;
 	/**
 	 * The parent frame.
 	 */
 	private ISGCIMainFrame parent;
 	/**
 	 * PageFormat object used when printing the current graph. Saved
 	 * as a field to allow settings to be persisted between printing
 	 * runs (as long as the program isn't closed).
 	 */
 	private PageFormat pageFormat;
 
 	/**
 	 * Key for setting the node configuration.
 	 */
 	public static final String NODE_CONFIGURATION = "myConf";
 	/**
 	 * Key for setting the HTML label configuration.
 	 */
 	public static final String HTML_CONFIGURATION = "HtmlConfig";
 
 
 	/**
 	 * Static c'tor. Registers the default node configuration when the type is first used.
 	 */
 	static {
 		registerDefaultNodeConfiguration(true);
 	}
 
 	/**
 	 * Creates a new instance.
 	 * 
 	 * @param parent The parent frame.
 	 */
 	public YFilesDrawingService(ISGCIMainFrame parent) {
 		/* Store the parent frame */
 		this.parent = parent;
 
 		/* Create the graph view */
 		this.graphView = new Graph2DView();
 		this.graphView.setFitContentOnResize(true);
 		registerViewModes();
 
 		/* Zoom in/out at mouse pointer location */
 		Graph2DViewMouseWheelZoomListener wheelZoomListener = new Graph2DViewMouseWheelZoomListener();
 		wheelZoomListener.setCenterZooming(false);
 		this.graphView.getCanvasComponent().addMouseWheelListener(
 				wheelZoomListener);
 		
 		/* Initialise the collections */
 		currentNodes = new HashMap<NodeView, Node>();
 		currentEdges = new ArrayList<EdgeView>();
 		currentImproperEdges = new HashMap<GraphView, List<Edge>>();
 
 		/* This must be created here (before setting up the graph) so it 
 		 * can be used in the tool-tip method (createEditMode).
 		 */
 		nodeLabelMap = new HashMap<String, String>();
 
 		this.graph2D = graphView.getGraph2D();
 		this.applyRealizerDefaults(true, true);
 
 		/* Create the layouter */
 		layouter = new IncrementalHierarchicLayouter();
 		layouter.setOrthogonallyRouted(true);
 		layouter.setRecursiveGroupLayeringEnabled(false);
 
 		/* Add the glass panel */
 		addGlassPaneComponents();
 
 		/* Register the graph view with the DefaultGraph2DRenderer */
 		BridgeCalculator bridgeCalculator = new BridgeCalculator();
 		((DefaultGraph2DRenderer) this.graphView.getGraph2DRenderer())
 				.setBridgeCalculator(bridgeCalculator);
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#getCanvas()
 	 */
 	@Override
 	public JComponent getCanvas() {
 		return this.graphView;
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#initializeView(java.util.List, java.util.HashMap)
 	 */
 	@Override
 	public void initializeView(List<GraphView> graphs) {
 		//D.bug("entering initializeView");
 		
 		/* Clear the current graph and collections. */
 		graph2D.clear();
 		nodeLabelMap.clear();
 		currentNodes.clear();
 		currentEdges.clear();
 		currentImproperEdges.clear();
 
 		for (GraphView view : graphs) {
 			List<NodeView> nodes = view.getNodes();
 			List<EdgeView> edges = view.getEdges();
 			Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();
 			Map<Node, List<Node>> improperNodeMap = new HashMap<Node, List<Node>>();
 			//D.bug("Before: " + nodes.size() + " nodes & " + edges.size() + " edges");
 
 			/* Create all the nodes required for each of the edges. */
 			for (EdgeView edge : edges) {
 				processEdge(edge, nodeMap, improperNodeMap);
 			}
 
 			/* Run through the from-to map of nodes creating the edges. */
 			for (Node from : nodeMap.keySet()) {
 				//D.bug("From " + from);
 				List<Node> toNodes = nodeMap.get(from);
 				for (Node to : toNodes) {
 					//D.bug("\tTo " + to);
 					EdgeRealizer edgeRealizer = graph2D
 							.getDefaultEdgeRealizer().createCopy();
 					edgeRealizer.setTargetArrow(Arrow.STANDARD);
 					
 					graph2D.createEdge(from, to, edgeRealizer);
 				}
 			}
 
 			/* Add any improper inclusions. Start by adding an entry
 			 * to currentImproperEdges - this just makes the code below
 			 * a bit easier to read (no if-block to check if the view
 			 * is in the collection and then adding a new list if not). */
 			currentImproperEdges.put(view, new ArrayList<Edge>());
 			for (Node from : improperNodeMap.keySet()) {
 				//D.bug("From " + from);
 				List<Node> toNodes = improperNodeMap.get(from);
 				for (Node to : toNodes) {
 					//D.bug("\tTo " + to);
 					EdgeRealizer edgeRealizer = graph2D
 							.getDefaultEdgeRealizer().createCopy();
 					edgeRealizer.setTargetArrow(Arrow.STANDARD);
 					
 					/* Only draw the source arrow if necessary */
 					if (view.getIncludeImproper()) {
 						edgeRealizer.setSourceArrow(Arrow.STANDARD);
 					}
 					
 					Edge edge = graph2D.createEdge(from, to, edgeRealizer);
 					currentImproperEdges.get(view).add(edge);
 				}
 			}
 			
 			/* Now check to see if we have any nodes that don't have any edges. */
 			for (NodeView node : nodes) {
 				if (!currentNodes.containsKey(node)) {
 					this.createNode(node);
 					//D.bug("Adding orphaned node");
 				}
 			}
 			//D.bug("After: " + currentNodes.size() + " nodes & " + currentEdges.size() + " edges");
 		}
 		this.refreshView();
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#updateView(java.util.List, java.util.HashMap)
 	 */
 	@Override
 	public void updateView(List<GraphView> graphs) {
 		//D.bug("entering updateView");
 
 		/* Run through the graphs updating the nodes and edges */
 		for (GraphView view : graphs) {
 			List<NodeView> nodes = view.getNodes();
 			List<EdgeView> edges = view.getEdges();
 			Map<Node, List<Node>> nodeMap = new HashMap<Node, List<Node>>();
 			Map<Node, List<Node>> improperNodeMap = new HashMap<Node, List<Node>>();			
 			//D.bug("Before: " + nodes.size() + " nodes & " + edges.size() + " edges");
 			
 			/* Create a copy of the current edges so we can keep track of which
 			 * ones have been removed and which of the new edges aren't present. */
 			List<EdgeView> excessEdges = new ArrayList<EdgeView>(this.currentEdges);
 			for (EdgeView edge : edges) {
 				if (excessEdges.contains(edge)) {
 					/* Remove the edge from the queue so that we only have
 					 * missing edges left in the collection at the end. */
 					excessEdges.remove(edge);
 				}
 				else {
 					/* This is a new edge */
 					processEdge(edge, nodeMap, improperNodeMap);
 				}
 			}
 			
 			/* Remove the excess edges */
 			for (EdgeView edge : excessEdges) {
 				
 				/* This next bit doesn't work as the nodes are not
 				 * found in the currentNodes collection.
 				 * It doesn't matter in the current implementation
 				 * as edges cannot be removed separately and when
 				 * a node is removed the associated edges will also
 				 * be removed. */			
 				Node from = this.currentNodes.get(edge.getFrom());
 				Node to = this.currentNodes.get(edge.getTo());
 				
 				if ((from != null) && (to != null)) {
 					/* Never runs - see above */
 					Edge edgeToRemove = from.getEdge(to);
 					this.graph2D.removeEdge(edgeToRemove);
 				}
 				/* Remove the edge from the main collection */
 				this.currentEdges.remove(edge);
 			}
 
 			/* Add the new edges */
 			for (Node from : nodeMap.keySet()) {
 				//D.bug("From " + from);
 				List<Node> toNodes = nodeMap.get(from);
 				for (Node to : toNodes) {
 					//D.bug("\tTo " + to);
 					EdgeRealizer edgeRealizer = graph2D
 							.getDefaultEdgeRealizer().createCopy();
 					edgeRealizer.setTargetArrow(Arrow.STANDARD);
 					graph2D.createEdge(from, to, edgeRealizer);
 				}
 			}
 			
 			/* Add any improper inclusions. Start by adding an entry
 			 * to currentImproperEdges - this just makes the code below
 			 * a bit easier to read (no if-block to check if the view
 			 * is in the collection and then adding a new list if not). */
 			if (!currentImproperEdges.containsKey(view)) {
 				currentImproperEdges.put(view, new ArrayList<Edge>());
 			}
 			for (Node from : improperNodeMap.keySet()) {
 				//D.bug("From " + from);
 				List<Node> toNodes = improperNodeMap.get(from);
 				for (Node to : toNodes) {
 					//D.bug("\tTo " + to);
 					EdgeRealizer edgeRealizer = graph2D
 							.getDefaultEdgeRealizer().createCopy();
 					edgeRealizer.setTargetArrow(Arrow.STANDARD);
 					
 					/* Only draw the source arrow if necessary */
 					if (view.getIncludeImproper()) {
 						edgeRealizer.setSourceArrow(Arrow.STANDARD);
 					}
 					
 					Edge edge = graph2D.createEdge(from, to, edgeRealizer);
 					currentImproperEdges.get(view).add(edge);
 				}
 			}
 			
 			/* Remove any excess nodes */
 			List<NodeView> excessNodes = new ArrayList<NodeView>(this.currentNodes.keySet());
 			for (NodeView node : nodes) {
 				if (excessNodes.contains(node)) {
 					excessNodes.remove(node);
 				}				
 			}
 			for (NodeView node : excessNodes) {
 				this.graph2D.removeNode(this.currentNodes.get(node));
 				this.currentNodes.remove(node);
 			}
 			//D.bug("excessNodes size " + excessNodes.size());
 			
 			/* Add the nodes that have no edges */
 			//D.bug(nodes.size() + " NODES");
 			for (NodeView node : nodes) {
 				if (!currentNodes.containsKey(node)) {
 					this.createNode(node);
 				}
 			}
 		}
 		this.refreshView();
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#updateColors()
 	 */
 	@Override
 	public void updateColors() {
 		/* Run through all the nodes updating the colour with that
 		 * of the corresponding node view. */
         for (NodeView nodeView : this.currentNodes.keySet())
         {
         	Node n = this.currentNodes.get(nodeView);
         	ShapeNodeRealizer nr = (ShapeNodeRealizer)graph2D.getRealizer(n);          
         	nr.setFillColor(nodeView.getColor());
           	nr.repaint();
         }
 	}
 	
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#updateColors()
 	 */
 	@Override
 	public void updateImproperInclusions(List<GraphView> graphs) {
 		for (GraphView view : graphs) {
 			if (this.currentImproperEdges.containsKey(view)) {
 				/* Run through all the improper edges for this view */
 				for (Edge edge : this.currentImproperEdges.get(view)) {
 					EdgeRealizer edgeRealizer = this.graph2D.getRealizer(edge);
 					if (view.getIncludeImproper()) {
 						edgeRealizer.setSourceArrow(Arrow.STANDARD);
 					}
 					else {
 						edgeRealizer.setSourceArrow(Arrow.NONE);
 					}
 					edgeRealizer.repaint();
 				}
 			}
 		}
 		this.graph2D.updateViews();
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#refreshView()
 	 */
 	@Override
 	public void refreshView() {
 		/* Don't ask me why this has to be in the order it is - it just does :) 
 		 * We spent a lot of time trying to get this to work properly but it seems
 		 * that yfiles is a little bit sensitive here. Would really need to get into
 		 * the actual mechanics of all this to figure out what is going on. */
 		layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
 		final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
 		layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
 		this.graphView.fitContent();
 		updateNodeSize();
 		layoutExecutor.doLayout(this.graphView, layouter);
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#doLayout()
 	 */
 	@Override
 	public void doLayout() {
 		
 		/* Just delegate the work to the yfiles classes. */
 		layouter.setLayoutMode(IncrementalHierarchicLayouter.LAYOUT_MODE_FROM_SCRATCH);
 		final Graph2DLayoutExecutor layoutExecutor = new Graph2DLayoutExecutor();
 		layoutExecutor.getLayoutMorpher().setSmoothViewTransform(true);
 		layoutExecutor.doLayout(this.graphView, layouter);
 	}
 	
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#clearView()
 	 */
 	@Override
 	public void clearView() {
 		this.graph2D.clear();
 		this.graph2D.updateViews();
 	}
 	
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#loadGraphML(java.lang.String)
 	 */
 	@Override
 	public void loadGraphML(String gmlString) {
 		
 		/* Convert the string to a byte stream so we can load it
 		 * with the IOHandler below. The override of IOHandler.read
 		 * that takes a string expects a file name and not a string
 		 * containing graphML so we need to use the override that 
 		 * takes a stream. */
 		byte[] bytes = gmlString.getBytes();
 		InputStream is = new ByteArrayInputStream(bytes);
 
 		try {
 			IOHandler ioh = new GraphMLIOHandler();
 
 			ioh.read(this.graph2D, is);
 		} catch (IOException e) {
 			String message = "Unexpected error while loading resource \""
 					+ "\" due to " + e.getMessage();
 			D.bug(message);
 			throw new RuntimeException(message, e);
 		}
 		
 		/* Don't forget to refresh the view so that the graph is laid out
 		 * properly and a fit-to-screen is done. */
 		this.refreshView();
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#loadGraphML(java.net.URL)
 	 */
 	@Override
 	public void loadGraphML(URL resource) {
 		if (resource == null) {
 			String message = "Resource \"" + resource
 					+ "\" not found in classpath";
 			D.showError(message);
 			throw new RuntimeException(message);
 		}
 
 		try {
 			IOHandler ioh = new GraphMLIOHandler();
 
 			// Tested Version for loading explicit GraphML strings.
 			// String s =
 			// "<?xml version=\"1.0\" encoding=\"utf-8\"?><graphml xmlns:schemaLocation=\"http://graphml.graphdrawing.org/xmlns http://www.yworks.com/xml/schema/graphml/1.0/ygraphml.xsd\" xmlns:y=\"http://www.yworks.com/xml/graphml\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><key id=\"d0\" for=\"node\" yfiles.type=\"nodegraphics\"/><key id=\"e0\" for=\"edge\" yfiles.type=\"edgegraphics\"/><graph id=\"isgci\" edgedefault=\"directed\"><desc>ISGCI graph class diagram, generated 2013-06-15 14:55 by http://www.graphclasses.org</desc><node id=\"0\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;binary tree &#8745; partial grid&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"1\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-chordal &#8745; X-conformal &#8745;...&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"2\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite tolerance&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"3\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;chordal bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"4\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(fork,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"5\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; maximum degree 4...&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"6\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bi-cograph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"7\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; claw-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"8\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;grid graph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"9\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;PURE-2-DIR&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"10\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(odd-cycle,star&lt;sub&gt;1,2,3&lt;/sub&gt;,sunlet&lt;sub&gt;4&lt;/sub&gt;)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"11\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;independent module-composed&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"12\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;median&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"13\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;binary tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"14\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; probe interval&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"15\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"16\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(claw &#8746; 3K&lt;sub&gt;1&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"17\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;E-free &#8745; bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"18\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;triad convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"19\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;1-bounded bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"20\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(XC&lt;sub&gt;11&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"21\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;tolerance &#8745; tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"22\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;perfect elimination bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"23\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;hypercube&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"24\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;difference&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"25\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;2-bounded bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"26\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(odd-cycle,star&lt;sub&gt;1,2,3&lt;/sub&gt;)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"27\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(K&lt;sub&gt;1,4&lt;/sub&gt;,odd-cycle)-free &#8745; planar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"28\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-conformal &#8745; bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"29\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(P&lt;sub&gt;7&lt;/sub&gt;,odd-cycle,star&lt;sub&gt;1,2,3&lt;/sub&gt;)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"30\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;interval bigraph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"31\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;solid grid graph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"32\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;probe interval bigraph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"33\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;C&lt;sub&gt;6&lt;/sub&gt;-free &#8745; modular&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"34\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;P&lt;sub&gt;6&lt;/sub&gt;-free &#8745; chordal bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"35\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-chordal &#8745; bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"36\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; unit grid intersection&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"37\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;C&lt;sub&gt;4&lt;/sub&gt;-free &#8745; C&lt;sub&gt;6&lt;/sub&gt;-free &#8745;...&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"38\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;median &#8745; planar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"39\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; maximum degree 3&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"40\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;circular convex bipartite&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"41\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;partial grid&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"42\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;probe interval &#8745; tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"43\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(X&lt;sub&gt;177&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"44\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;grid&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"45\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;X-star-chordal&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"46\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;caterpillar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"47\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;interval containment bigraph&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"48\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;tree convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"49\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; planar&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"50\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;probe bipartite chain&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"51\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;almost median&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"52\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;absolute bipartite retract&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"53\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;hereditary median&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"54\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;premedian&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"55\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(A,T&lt;sub&gt;2&lt;/sub&gt;,odd-cycle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"56\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FF0000\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(0,2)-colorable&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"57\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;cubical&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"58\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;grid graph &#8745; maximum degree 3&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"59\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;tree&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"60\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;(P&lt;sub&gt;4&lt;/sub&gt;,triangle)-free&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"61\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;partial cube&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"62\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;biconvex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"63\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;modular&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"64\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00B200\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; tolerance&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"65\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#00FF00\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;bipartite &#8745; bithreshold&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><node id=\"66\"><data key=\"d0\"><y:ShapeNode><y:Fill color=\"#FFFFFF\"/><y:Shape type=\"ellipse\"/><y:NodeLabel>&lt;html&gt;star convex&lt;/html&gt;</y:NodeLabel></y:ShapeNode></data></node><edge source=\"1\" target=\"3\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"47\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"5\" target=\"41\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"5\" target=\"27\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"9\" target=\"49\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"9\" target=\"36\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"17\" target=\"4\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"52\" target=\"33\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"10\" target=\"17\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"59\" target=\"21\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"59\" target=\"13\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"47\" target=\"30\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"33\" target=\"3\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"25\" target=\"19\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"34\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"34\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"34\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"61\" target=\"54\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"15\" target=\"62\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"16\" target=\"25\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"46\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"2\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"21\" target=\"42\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"11\" target=\"59\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"22\" target=\"3\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"63\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"48\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"57\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"35\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"26\" target=\"10\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"12\" target=\"53\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"12\" target=\"23\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"12\" target=\"38\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"53\" target=\"59\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"62\" target=\"2\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"64\" target=\"14\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"64\" target=\"21\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"29\" target=\"6\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"29\" target=\"34\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"31\" target=\"44\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"13\" target=\"0\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"35\" target=\"45\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"36\" target=\"30\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"38\" target=\"59\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"39\" target=\"27\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"43\" target=\"4\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"43\" target=\"25\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"48\" target=\"66\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"48\" target=\"18\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"49\" target=\"5\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"49\" target=\"38\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"51\" target=\"12\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"63\" target=\"52\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"4\" target=\"19\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"4\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"4\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"8\" target=\"31\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"8\" target=\"58\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"14\" target=\"15\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"14\" target=\"42\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"20\" target=\"39\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"20\" target=\"5\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"27\" target=\"13\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"28\" target=\"1\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"30\" target=\"64\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"32\" target=\"30\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"41\" target=\"8\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"19\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"6\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"45\" target=\"1\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"55\" target=\"4\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"57\" target=\"61\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"54\" target=\"51\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"34\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"50\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"3\" target=\"11\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"17\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"17\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"10\" target=\"6\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"10\" target=\"25\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"37\" target=\"59\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"33\" target=\"53\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"16\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"11\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"11\" target=\"60\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"26\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"55\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"16\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"43\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"37\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"20\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"22\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"40\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"32\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"9\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"56\" target=\"28\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"26\" target=\"29\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"53\" target=\"44\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"42\" target=\"46\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"36\" target=\"34\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"38\" target=\"44\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"43\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"48\" target=\"3\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"63\" target=\"12\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"50\" target=\"65\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"50\" target=\"24\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"27\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"27\" target=\"58\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"32\" target=\"50\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"40\" target=\"15\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"41\" target=\"7\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"41\" target=\"0\" directed=\"false\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"short\"/></y:PolyLineEdge></data></edge><edge source=\"55\" target=\"46\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge><edge source=\"57\" target=\"8\"><data key=\"e0\"><y:PolyLineEdge><y:Arrows target=\"standard\" source=\"none\"/></y:PolyLineEdge></data></edge></graph></graphml>";
 			// loadGraphMLString(s);
 
 			ioh.read(this.graph2D, resource);
 		} catch (Exception e) {
 			String message = "Unexpected error while loading resource \""
 					+ resource + "\" due to " + e.getMessage();
 			D.bug(message);
 			throw new RuntimeException(message, e);
 		}
 		
 		/* Don't forget to refresh the view so that the graph is laid out
 		 * properly and a fit-to-screen is done. */
 		this.refreshView();
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#exportGraphics(teo.isgci.view.gui.PSGraphics)
 	 */
 	@Override
 	public void exportGraphics(PSGraphics g) {
 		g.drawImage(this.graphView.getImage(), 0, 0, this.graphView);
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#exportPS(java.lang.String)
 	 */
 	@Override
 	public void exportPS(String file) throws IOException {
 		IOHandler ioh = new EPSOutputHandler();
 		double tmpPDT = this.graphView.getPaintDetailThreshold();
 		this.graphView.setPaintDetailThreshold(0.0);
 		ioh.write(this.graph2D, file);
 		this.graphView.setPaintDetailThreshold(tmpPDT);
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#exportSVG(java.lang.String)
 	 */
 	@Override
 	public void exportSVG(String file) throws IOException {
 		IOHandler ioh = new SVGIOHandler();
 		double tmpPDT = this.graphView.getPaintDetailThreshold();
 		this.graphView.setPaintDetailThreshold(0.0);
 		ioh.write(this.graph2D, file);
 		this.graphView.setPaintDetailThreshold(tmpPDT);
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#exportGML(java.lang.String)
 	 */
 	@Override
 	public void exportGML(String file) throws IOException {
 		/* NOTE:
 		 * Case-sensitivity!! Not sure if this is a
 		 * case-sensitive check in Java but I assume so. It
 		 * isn't critical in this case, maybe just a bit silly
 		 * (e.g. temp.GRAPHML.graphml), so I will leave this 
 		 * as-is for now.
 		 */
 		if (!file.endsWith(".graphml")) {
 			file += ".graphml";
 		}
 		IOHandler ioh = new GraphMLIOHandler();
 		ioh.write(this.graph2D, file);
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#selectNeighbors()
 	 */
 	@Override
 	public void selectNeighbors() {
 		ArrayList<Node> list = new ArrayList<Node>();
 		
 		/* Get a cursor for the currently selected nodes. */
 		Graph2D g = this.graphView.getGraph2D();
 		NodeCursor n = g.selectedNodes();
 
 		/* Run through all the neighbouring nodes for each of 
 		 * the selected nodes. */
 		for (int i = 0; i < n.size(); ++i) {
 			NodeCursor neigh = n.node().neighbors();
 			for (int j = 0; j < neigh.size(); ++j) {
 				list.add(neigh.node());
 				neigh.next();
 			}
 			n.next();
 		}
 		/* Run through the list and set each node to selected. */
 		for (Node node : list) {
 			this.graphView.getGraph2D().setSelected(node, true);
 		}
 		
 		/* Finally, update the view. */
 		this.graphView.updateView();
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#selectSuperClasses()
 	 */
 	@Override
 	public void selectSuperClasses() {
 		ArrayList<Node> l = new ArrayList<Node>();
 		
 		/* Run through the selected nodes and store the 
 		 * predecessors for each node. */
 		NodeCursor n = this.graph2D.selectedNodes();
 
 		for (int i = 0; i < n.size(); ++i) {
 			NodeCursor neigh = n.node().predecessors();
 			for (int j = 0; j < neigh.size(); ++j) {
 				l.add(neigh.node());
 				neigh.next();
 			}
 			n.next();
 		}
 		
 		/* Now run through the nodes found and select them. Do this
 		 * after the above loop so we don't alter the collection being
 		 * iterated over. */
 		for (Node no : l) {
 			this.graph2D.setSelected(no, true);
 		}
 		this.graphView.updateView();
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#selectSubClasses()
 	 */
 	@Override
 	public void selectSubClasses() {
 		ArrayList<Node> l = new ArrayList<Node>();
 		
 		/* Run through the selected nodes and store the 
 		 * successors for each node. */
 		NodeCursor n = this.graph2D.selectedNodes();
 		for (int i = 0; i < n.size(); ++i) {
 			NodeCursor neigh = n.node().successors();
 			for (int j = 0; j < neigh.size(); ++j) {
 				l.add(neigh.node());
 				neigh.next();
 			}
 			n.next();
 		}
 		
 		/* Now run through the nodes found and select them. Do this
 		 * after the above loop so we don't alter the collection being
 		 * iterated over. */
 		for (Node no : l) {
 			this.graph2D.setSelected(no, true);
 		}
 		this.graphView.updateView();
 	}
 	
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#getSelection()
 	 */
 	@Override
 	public Collection<GraphClass> getSelection() {
 		Graph2D g = this.graphView.getGraph2D();
 		NodeCursor n = g.selectedNodes();
 		Collection<GraphClass> graphClasses = new ArrayList<GraphClass>();
 		for (int i = 0; i < n.size(); ++i) {
 
 			/* Get the user data attached to the label. */
 			NodeView nodeView = getNodeView(n.node());
 			
 			/* Add the default class to the selection */
 			graphClasses.add(nodeView.getDefaultClass());
 			n.next();
 		}
 		return graphClasses;
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#invertSelection()
 	 */
 	@Override
 	public void invertSelection() {
 		/* Run through all the nodes toggling the selection */
 		for (NodeCursor nc = this.graph2D.nodes(); nc.ok(); nc.next()) {
 			Node n = nc.node();
 			NodeRealizer nr = this.graph2D.getRealizer(n);
 			nr.setSelected(!nr.isSelected());
 			nr.repaint();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#search(teo.isgci.core.NodeView)
 	 */
 	@Override
 	public void search(NodeView view) {
 		// TODO SWP: Fit nodes to the current window. At the moment the nodes
 		// are just selected. Sometimes the view is such that it is hard to 
 		// see where the nodes are. To do this we need to update the method 
 		// to accept an array of node views so that we can process all the
 		// nodes being searched for at once.
 		
 		/* Run through all the nodes looking for the Node associated with the
 		 * NodeView passed.	 */
 		for (NodeCursor nc = this.graphView.getGraph2D().nodes(); nc.ok(); nc
 				.next()) {
 			
 			/* Get the associated NodeView. This way we can search for 
 			 * equivalent classes without relying on the node views name. */
 			Node node = nc.node();
 			NodeView current = getNodeView(node);
 
 			if (current.equals(view)) {
 				/* Select the node and then break */
 				this.graphView.getGraph2D().setSelected(node, true);
 				break;
 			}
 		}
 		this.graphView.repaint();
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#print()
 	 */
 	@Override
 	public void print() {
 		/* Setup option handler */
 		OptionHandler printOptions = new OptionHandler("Print Options");
 		printOptions.addInt("Poster Rows", 1);
 		printOptions.addInt("Poster Columns", 1);
 		printOptions.addBool("Add Poster Coords", false);
 		final String[] area = { "View", "Graph" };
 		printOptions.addEnum("Clip Area", area, 1);
 
 		Graph2DPrinter gprinter = new Graph2DPrinter(this.graphView);
 
 		/* Show custom print dialog and adopt values */
 		if (!printOptions.showEditor(this.graphView.getFrame())) {
 			return;
 		}
 		gprinter.setPosterRows(printOptions.getInt("Poster Rows"));
 		gprinter.setPosterColumns(printOptions.getInt("Poster Columns"));
 		gprinter.setPrintPosterCoords(printOptions.getBool("Add Poster Coords"));
 		if ("Graph".equals(printOptions.get("Clip Area"))) {
 			gprinter.setClipType(Graph2DPrinter.CLIP_GRAPH);
 		} else {
 			gprinter.setClipType(Graph2DPrinter.CLIP_VIEW);
 		}
 
 		/* Show default print dialogs */
 		PrinterJob printJob = PrinterJob.getPrinterJob();
 		if (pageFormat == null) {
 			pageFormat = printJob.defaultPage();
 		}
 		PageFormat pf = printJob.pageDialog(pageFormat);
 		if (pf == pageFormat) {
 			return;
 		} else {
 			pageFormat = pf;
 		}
 
 		/*
 		 * Setup print job Graph2DPrinter is of type Printable
 		 */
 		printJob.setPrintable(gprinter, pageFormat);
 
 		if (printJob.printDialog()) {
 			try {
 				printJob.print();
 			} catch (Exception ex) {
 				ex.printStackTrace();
 			}
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see teo.isgci.core.IDrawingService#getCurrentFile()
 	 */
 	@Override
 	public URL getCurrentFile() {
 		return this.graph2D.getURL();
 	}
 
 
 	/**
 	 * Gets the full name for a node.
 	 * @param node The node.
 	 * @return The full name.
 	 */
 	String getNodeName(Node node) {
 		return getNodeView(node).getFullName();
 	}
 
 	/**
 	 * Gets the NodeView for a node.
 	 * @param node The node.
 	 * @return The NodeView.
 	 */
 	NodeView getNodeView(Node node) {
 		NodeRealizer realizer = this.graph2D.getRealizer(node);
 		NodeLabel label = realizer.getLabel();
 		return (NodeView)label.getUserData();
 	}
 
 	/**
 	 * Updates a node label with the new default class for a node view.
 	 * @param nodeView	The node view with a new default class.
 	 */
 	void updateLabel(NodeView nodeView) {
         if (this.currentNodes.containsKey(nodeView)) {
         	/* Get the node and realizer corresponding to 
         	 * the node view. */
         	Node node = this.currentNodes.get(nodeView);
         	NodeRealizer realizer = graph2D.getRealizer(node);
 
         	/* Set the node label text. */
         	NodeLabel nodeLabel = realizer.getLabel();
     		nodeLabel.setText(nodeView.getHtmlLabel());
     		
     		/* Repaint before updating the node size. If we
     		 * don't do this then the calculations are wrong. */
     		realizer.repaint();
 
         	/* Now we need to update the node size (for all nodes) */
         	this.updateNodeSize();
         }
 	}
 	
 	/**
 	 * Updates the width of all nodes using the maximum label size. This
 	 * uses a uniform width accross all nodes.
 	 */
 	private void updateNodeSize() {
 		// Find the maximum width of all used Labels.
 		// Alternative: Skip the first run and set the current labelwidth for
 		// all node = different nodeswidths
 		double maxlen = 0;
 		boolean bigger = false;
 		for (NodeCursor nc = graph2D.nodes(); nc.ok(); nc.next()) {
 			Node n = nc.node();
 			ShapeNodeRealizer nr = (ShapeNodeRealizer) graph2D.getRealizer(n);
 			nr.setShapeType(ShapeNodeRealizer.ROUND_RECT);
 
 			NodeLabel label = nr.getLabel();
 			System.out.println(label.getFontSize());
 			
 			/* Set the font size */
 			if (label.getFontSize() > 5) {
 				bigger = true;
 			}
 			label.setFontSize(5);
 			
 			/* Set the label model (for centering) */
 			SmartNodeLabelModel model = new SmartNodeLabelModel();
 			label.setLabelModel(model);
 			label.setModelParameter(
 					model.createDiscreteModelParameter(SmartNodeLabelModel.POSITION_CENTER));
 
 			/* Get the content width and adjust the max width if necessary */
 			double len = label.getContentWidth() + 10;
 			if (len > maxlen) {
 				maxlen = len;
 			}
 		}
 		System.out.println("Max Width: " + maxlen);
 
 		/* Run through all the nodes updating the size */
 		for (NodeCursor nc = graph2D.nodes(); nc.ok(); nc.next()) {
 			Node n = nc.node();
 
 			ShapeNodeRealizer nr = (ShapeNodeRealizer) graph2D.getRealizer(n);
 			nr.setShapeType(ShapeNodeRealizer.ROUND_RECT);
 
 			if (bigger) {
 				nr.setSize(Math.max(maxlen *5 / 12, 30), 20);
 			} else {
 				nr.setSize(Math.max(maxlen, 30), 20);
 			}
 
 			nr.repaint();
 		}
 	}
 
 	/**
 	 * Creates any nodes required for the given edge
 	 * @param edge The edge to be processed.
 	 * @param nodeMap A map of nodes and their connected
 	 * nodes (key = from node, value = to nodes). This is 
 	 * used to create the yfiles Edge objects after all 
 	 * the nodes have been created.
 	 * @param improperNodeMap The node map for improper edges.
 	 */
 	private void processEdge(EdgeView edge,
 			Map<Node, List<Node>> nodeMap, Map<Node, List<Node>> improperNodeMap) {
 		/* Get the node view objects for the edge */
 		NodeView from = edge.getFromNode();
 		NodeView to = edge.getToNode();
 
 		/* Get or create the yfiles Node objects for the node views. */
 		Node tempFrom;
 		if (currentNodes.containsKey(from)) {
 			tempFrom = currentNodes.get(from);
 		} else {
 			tempFrom = createNode(from);
 		}
 		Node tempTo;
 		if (currentNodes.containsKey(to)) {
 			tempTo = currentNodes.get(to);
 		} else {
 			tempTo = createNode(to);
 		}
 
 		/* Update the node map with the current from-to mapping */
 		if (edge.getProper()) {			
 			if (nodeMap.containsKey(tempFrom)) {
 				nodeMap.get(tempFrom).add(tempTo);
 			} else {
 				ArrayList<Node> toNodes = new ArrayList<Node>();
 				toNodes.add(tempTo);
 				nodeMap.put(tempFrom, toNodes);
 			}
 		}
 		else {
 			if (improperNodeMap.containsKey(tempFrom)) {
 				improperNodeMap.get(tempFrom).add(tempTo);
 			} else {
 				ArrayList<Node> toNodes = new ArrayList<Node>();
 				toNodes.add(tempTo);
 				improperNodeMap.put(tempFrom, toNodes);
 			}
 		}
 		currentEdges.add(edge);
 	}
 	
 	/**
 	 * Creates a yfiles Node for the given NodeView.
 	 * @param nodeView The node view.
 	 * @return The newly created node.
 	 */
 	private Node createNode(NodeView nodeView) {
 
 		/* Get a node realizer and create a node. */
 		NodeRealizer realizer = graph2D.getDefaultNodeRealizer().createCopy();
 		realizer.setFillColor(nodeView.getColor());
 		Node node = graph2D.createNode(realizer);
 
 		/* Set the node label. */
 		setNodeLabel(nodeView, realizer);
 
 		/* Add the label/fullname mapping to the label map. */
 		String label = nodeView.getHtmlLabel();
 		String fullname = nodeView.getFullName();
 		if (!this.nodeLabelMap.containsKey(label)) {
 			this.nodeLabelMap.put(label, fullname);
 		}		
 		/* Save and return the node */
 		currentNodes.put(nodeView, node);
 		return node;
 	}
 
 	/**
 	 * Gets a node label for the given node.
 	 * @param nodeView The node.
 	 * @return The node label.
 	 */
 	private NodeLabel setNodeLabel(NodeView nodeView, NodeRealizer nodeRealizer) {
 		/* Create a new label and set the HTML configuration */
 		NodeLabel nodeLabel = nodeRealizer.createNodeLabel();
 		nodeLabel.setConfiguration(HTML_CONFIGURATION); 
 		
 		/* Set the label text */
 		D.bug("Node label: " + nodeView.getHtmlLabel());
 
 		nodeLabel.setText(nodeView.getHtmlLabel());
 		
 		/* The following line can be use to automatically trim the text
 		 * without using the NodeView label property. This would allow for
 		 * a fixed node width without any text size calculation. */
 		//nodeLabel.setAutoSizePolicy(YLabel.AUTOSIZE_CONTENT);
 		
 		/* Set the tag so we can find the class associated with this node */
 		nodeLabel.setUserData(nodeView);
 		
 		/* Set the realizers label */
 		nodeRealizer.setLabel(nodeLabel);		
 
 		return nodeLabel;
 	}
 
 	/**
 	 * Applies the node and edge realizer defaults to the current graph.
 	 * @param applyDefaultSize Whether to apply the default size.
 	 * @param applyFillColor Whether to apply the default fill colour.
 	 */
 	private void applyRealizerDefaults(boolean applyDefaultSize, boolean applyFillColor) {
 		for (NodeCursor nc = this.graph2D.nodes(); nc.ok(); nc.next()) {
 			GenericNodeRealizer gnr = new GenericNodeRealizer(
 					this.graph2D.getRealizer(nc.node()));
 			gnr.setConfiguration(NODE_CONFIGURATION);
 			if (applyFillColor) {
 				gnr.setFillColor(this.graph2D.getDefaultNodeRealizer().getFillColor());
 			}
 			gnr.setLineColor(null);
 			if (applyDefaultSize) {
 				gnr.setSize(this.graph2D.getDefaultNodeRealizer().getWidth(), this.graph2D
 						.getDefaultNodeRealizer().getHeight());
 			}
 			NodeLabel label = gnr.getLabel();
 			OrientedRectangle labelBounds = label.getOrientedBox();
 			SmartNodeLabelModel model = new SmartNodeLabelModel();
 			label.setLabelModel(model);
 			label.setModelParameter(model
 					.createModelParameter(labelBounds, gnr));
 			this.graph2D.setRealizer(nc.node(), gnr);
 		}
 	}
 
 	/**
 	 * Registers the default node configuration.
 	 * @param drawShadows Whether or not to draw shadows around the nodes.
 	 */
 	private static void registerDefaultNodeConfiguration(boolean drawShadows) {
 		Factory factory = GenericNodeRealizer.getFactory();
 
 		/* Stupid Factory method returns a raw Map type... grrr :) */
 		@SuppressWarnings("unchecked")
 		Map<Object, Object> configurationMap = factory.createDefaultConfigurationMap();
 
 		ShinyPlateNodePainter painter = new ShinyPlateNodePainter();
 		// ShinyPlateNodePainter has an option to draw a drop shadow that is
 		// more efficient than wrapping it in a ShadowNodePainter.
 		painter.setDrawShadow(drawShadows);
 		
 		configurationMap.put(GenericNodeRealizer.Painter.class, painter);
 		configurationMap.put(GenericNodeRealizer.ContainsTest.class, painter);
 
 		factory.addConfiguration(NODE_CONFIGURATION, configurationMap);
 
 		/* Instantiate HtmlLabelConfiguration. */  
 		YLabel.Factory labelFactory = NodeLabel.getFactory();  
 		HtmlLabelConfiguration htmlConfig = new HtmlLabelConfiguration();  
 		  
 		/* Stupid factory methods with their raw types :( */
 		@SuppressWarnings("unchecked")
 		Map<Object, Object> htmlConfigMap = labelFactory.createDefaultConfigurationMap();  
 		htmlConfigMap.put(YLabel.Painter.class, htmlConfig);  
 		htmlConfigMap.put(YLabel.Layout.class, htmlConfig);  
 		htmlConfigMap.put(YLabel.BoundsProvider.class, htmlConfig);
 		  
 		/* Add the HTML configuration to the factory. */
 		labelFactory.addConfiguration(HTML_CONFIGURATION, htmlConfigMap);
 		  
 	}
 
 	/**
 	 * Adds the glass panel components (including the navigaiton pane).
 	 */
 	private void addGlassPaneComponents() {
 		// get the glass pane
 		JPanel glassPane = this.graphView.getGlassPane();
 		// set an according layout manager
 		glassPane.setLayout(new BorderLayout());
 
 		JPanel toolsPanel = new JPanel(new GridBagLayout());
 		toolsPanel.setOpaque(false);
 		toolsPanel.setBackground(null);
 		toolsPanel.setBorder(BorderFactory.createEmptyBorder(16, 16, 0, 0));
 
 		// create and add the overview to the tools panel
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.gridx = 0;
 		gbc.anchor = GridBagConstraints.LINE_START;
 		gbc.insets = new Insets(0, 0, 16, 0);
 		JComponent overview = this.createOverview();
 		toolsPanel.add(overview, gbc);
 
 		// create and add the navigation component to the tools panel
 		NavigationComponent navigationComponent = createNavigationComponent(20, 30);
 		toolsPanel.add(navigationComponent, gbc);
 
 		// add the toolspanel to the glass pane
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
 
 	/**
 	 * Creates the navigation component for the glass pane.
 	 * @param scrollStepSize The step size for scrolling.
 	 * @param scrollTimerDelay The timer delay for scrolling.
 	 * @return The navigation component.
 	 */
 	private NavigationComponent createNavigationComponent(double scrollStepSize, int scrollTimerDelay) {
 		// create the NavigationComponent itself
 		final NavigationComponent navigation = new NavigationComponent(this.graphView);
 		navigation.setScrollStepSize(scrollStepSize);
 		// set the duration between scroll ticks
 		navigation.putClientProperty("NavigationComponent.ScrollTimerDelay",
 				new Integer(scrollTimerDelay));
 		// set the initial duration until the first scroll tick is triggered
 		navigation.putClientProperty(
 				"NavigationComponent.ScrollTimerInitialDelay", new Integer(
 						scrollTimerDelay));
 		// set a flag so that the fit content button will adjust the viewports
 		// in an animated fashion
 		navigation.putClientProperty("NavigationComponent.AnimateFitContent",
 				Boolean.TRUE);
 
 		// add a mouse listener that will make a semi transparent background, as
 		// soon as the mouse enters this component
 		navigation.setBackground(new Color(255, 255, 255, 0));
 		MouseAdapter navigationToolListener = new MouseAdapter() {
 			public void mouseEntered(MouseEvent e) {
 				super.mouseEntered(e);
 				Color background = navigation.getBackground();
 				// add some semi transparent background
 				navigation.setBackground(new Color(background.getRed(),
 						background.getGreen(), background.getBlue(), 196));
 			}
 
 			public void mouseExited(MouseEvent e) {
 				super.mouseExited(e);
 				Color background = navigation.getBackground();
 				// make the background completely transparent
 				navigation.setBackground(new Color(background.getRed(),
 						background.getGreen(), background.getBlue(), 0));
 			}
 		};
 		navigation.addMouseListener(navigationToolListener);
 
 		// add mouse listener to all sub components of the navigationComponent
 		for (int i = 0; i < navigation.getComponents().length; i++) {
 			Component component = navigation.getComponents()[i];
 			component.addMouseListener(navigationToolListener);
 		}
 
 		return navigation;
 	}
 
 	/**
 	 * Registers the edit and auto-drag view modes.
 	 */
 	private void registerViewModes() {
 
 		EditMode editMode = createEditMode();
 		editMode.allowNodeCreation(false);
 		editMode.allowEdgeCreation(false);
 		editMode.allowNodeEditing(false);
 
 		this.graphView.addViewMode(editMode);
 		this.graphView.addViewMode(new AutoDragViewMode());
 	}
 
 	/**
 	 * Creates the overview component for the glass pane (the 'map' of the graph).
 	 * @return The overview component.
 	 */
 	private Overview createOverview() {
 		Overview ov = new Overview(this.graphView);
 		// animates the scrolling
 		ov.putClientProperty("Overview.AnimateScrollTo", Boolean.TRUE);
 		// blurs the part of the graph which can currently not be seen
 		ov.putClientProperty("Overview.PaintStyle", "Funky");
 		// allows zooming from within the overview
 		ov.putClientProperty("Overview.AllowZooming", Boolean.TRUE);
 		// provides functionality for navigation via keybord (zoom in (+), zoom
 		// out (-), navigation with arrow keys)
 		ov.putClientProperty("Overview.AllowKeyboardNavigation", Boolean.TRUE);
 		// determines how to differ between the part of the graph that can
 		// currently be seen, and the rest
 		ov.putClientProperty("Overview.Inverse", Boolean.TRUE);
 		ov.setPreferredSize(new Dimension(150, 150));
 		ov.setMinimumSize(new Dimension(150, 150));
 
 		ov.setBorder(BorderFactory.createEtchedBorder());
 		return ov;
 	}
 
 	/**
 	 * Creates the edit mode for nodes and edges. This sets the
 	 * tool tip mode, dragging with the right mouse button etc. 
 	 * @return The edit mode.
 	 */
 	private EditMode createEditMode() {
 		
 		final YFilesDrawingService drawingService = this;
 		EditMode editMode = new EditMode(){
 		      /* Overwrite getNodeTip with the classes full name */
 		      public String getNodeTip(Node v) {
 		        return drawingService.getNodeName(v);
 		      }
 		    };
 		// show the highlighting which is turned off by default
 		if (editMode.getCreateEdgeMode() instanceof CreateEdgeMode) {
 			((CreateEdgeMode) editMode.getCreateEdgeMode())
 					.setIndicatingTargetNode(true);
 		}
 		if (editMode.getMovePortMode() instanceof MovePortMode) {
 			((MovePortMode) editMode.getMovePortMode())
 					.setIndicatingTargetNode(true);
 		}
 		
 		//enable node tooltips
 	    editMode.showNodeTips(true);
 
 		// allow moving view port with right drag gesture
 		editMode.allowMovingWithPopup(true);
 		editMode.allowLabelSelection(true);
 
 		// add hierarchy actions to the views popup menu
 		editMode.setPopupMode(new HierarchicPopupMode(this.parent, this));
 
 		editMode.getMouseInputMode().setNodeSearchingEnabled(true);
 		editMode.getMouseInputMode().setEdgeSearchingEnabled(true);
 
 		// Add a visual indicator for the target node of an edge creation -
 		// makes it easier to
 		// see the target for nested graphs
 		ViewMode createEdgeMode = editMode.getCreateEdgeMode();
 		if (createEdgeMode instanceof CreateEdgeMode) {
 			((CreateEdgeMode) createEdgeMode).setIndicatingTargetNode(true);
 		}
 		return editMode;
 	}
 
 }
