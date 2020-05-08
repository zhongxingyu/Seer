 package de.unikassel.ann.gui.graph;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.Point2D;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 
 import de.unikassel.ann.controller.EdgeController;
 import de.unikassel.ann.controller.LayerController;
 import de.unikassel.ann.controller.VertexController;
 import de.unikassel.ann.model.Layer;
 import de.unikassel.ann.model.Network;
 import de.unikassel.ann.model.Neuron;
 import de.unikassel.ann.model.Synapse;
 import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
 import edu.uci.ics.jung.algorithms.layout.Layout;
 import edu.uci.ics.jung.algorithms.layout.StaticLayout;
 import edu.uci.ics.jung.graph.DirectedGraph;
 import edu.uci.ics.jung.graph.DirectedSparseGraph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
 import edu.uci.ics.jung.visualization.VisualizationServer.Paintable;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
 import edu.uci.ics.jung.visualization.layout.LayoutTransition;
 import edu.uci.ics.jung.visualization.util.Animator;
 
 public class GraphLayoutViewer {
 
 	/*
 	 * fields
 	 */
 	protected DirectedGraph<Vertex, Edge> graph;
 	protected AbstractLayout<Vertex, Edge> layout;
 	protected VisualizationViewer<Vertex, Edge> viewer;
 	public GraphMouse<Vertex, Edge> graphMouse;
 
 	private JFrame frame;
 	private Dimension dim;
 	private Container parent;
 
 	private static GraphLayoutViewer instance;
 
 	/**
 	 * Private Constructor
 	 */
 	private GraphLayoutViewer() {
 	}
 
 	public static GraphLayoutViewer getInstance() {
 		if (instance == null) {
 			instance = new GraphLayoutViewer();
 		}
 		return instance;
 	}
 
 	/**
 	 * Initialize
 	 */
 	public void init() {
 		//
 		// Graph - Layout - Viewer
 		//
 		graph = new DirectedSparseGraph<Vertex, Edge>();
 		layout = new StaticLayout<Vertex, Edge>(graph, dim);
 		viewer = new VisualizationViewer<Vertex, Edge>(layout);
 		viewer.setBackground(Color.white);
 		viewer.addPreRenderPaintable(new Paintable() {
 
 			@Override
 			public boolean useTransform() {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public void paint(final Graphics g) {
 				// Update the index of vertices
 				int index = 0;
 				ArrayList<JungLayer> layers = LayerController.getInstance().getLayers();
 				for (JungLayer jungLayer : layers) {
 					for (Vertex vertex : jungLayer.getVertices()) {
 						vertex.setIndex(index++);
 					}
 				}
 			}
 		});
 
 		// viewer.addComponentListener(new ComponentAdapter() {
 		// @Override
 		// public void componentResized(final ComponentEvent evt) {
 		// super.componentResized(evt);
 		// layout.setSize(evt.getComponent().getSize());
 		// }
 		// });
 
 		//
 		// Controller
 		//
 		VertexController.getInstance().init();
 		EdgeController.getInstance().init();
 
 		//
 		// Panel
 		//
 		parent.add(new GraphZoomScrollPane(viewer));
 
 		//
 		// Graph Mouse
 		//
 		initGraphMouse();
 		// addMouseModeMenu();
 	}
 
 	/*
 	 * Getter & Setter
 	 */
 
 	public DirectedGraph<Vertex, Edge> getGraph() {
 		return graph;
 	}
 
 	public void setGraph(final DirectedGraph<Vertex, Edge> graph) {
 		this.graph = graph;
 	}
 
 	public AbstractLayout<Vertex, Edge> getLayout() {
 		return layout;
 	}
 
 	public void setLayout(final AbstractLayout<Vertex, Edge> layout) {
 		this.layout = layout;
 	}
 
 	public VisualizationViewer<Vertex, Edge> getViewer() {
 		return viewer;
 	}
 
 	public void setViewer(final VisualizationViewer<Vertex, Edge> viewer) {
 		this.viewer = viewer;
 	}
 
 	public void setDimension(final Dimension dim) {
 		this.dim = dim;
 	}
 
 	public void setParent(final JPanel parent) {
 		this.parent = parent;
 	}
 
 	public void setFrame(final JFrame frame) {
 		this.frame = frame;
 	}
 
 	/**
 	 * Compute and set the position of each graph component. Animate the layout changes.
 	 */
 	public void repaint() {
 		GraphLayoutViewer.repaint(viewer);
 	}
 
 	/**
 	 * Remove all vertices and their edges from the graph.
 	 */
 	public void clear() {
 		// Reset factories
 		VertexController.getInstance().getVertexFactory().reset();
 		EdgeController.getInstance().getEdgeFactory().reset();
 
		// Clear current LayerController
		LayerController.getInstance().clear();
 
		// Create new graph
		graph = new DirectedSparseGraph<Vertex, Edge>();
		layout.setGraph(graph);
		layout.initialize();
 		repaint(viewer);
 	}
 
 	/**
 	 * Render neurons and their synapses as Vertices and Edges into the Graph.
 	 * 
 	 * @param network
 	 */
 	public void renderNetwork(final Network network) {
 		// Clear current graph view
 		clear();
 
 		//
 		// Render Vertices into their Layers
 		//
 		LayerController<Layer> layerController = LayerController.getInstance();
 		VertexController<Vertex> vertexController = VertexController.getInstance();
 		List<Layer> layers = network.getLayers();
 		for (Layer layer : layers) {
 			layerController.addLayer(layer.getIndex());
 
 			List<Neuron> neurons = layer.getNeurons();
 			for (Neuron neuron : neurons) {
 				Vertex vertex = vertexController.getVertexFactory().create();
 
 				// Set the id of the neuron as index of the vertex
 				vertex.setIndex(neuron.getId());
 				vertex.setModel(neuron);
 
 				// Add the new vertex to the current jung layer
 				layerController.addVertex(layer.getIndex(), vertex, false);
 
 				// Add the new vertex to the graph
 				graph.addVertex(vertex);
 			}
 		}
 
 		//
 		// Render Synapses
 		//
 		for (Synapse synapse : network.getSynapseSet()) {
 
 			// Get the vertices of the synapse by their id of the their models
 			Integer fromId = synapse.getFromNeuron().getId();
 			Integer toId = synapse.getToNeuron().getId();
 
 			Vertex fromVertex = layerController.getVertexById(fromId);
 			Vertex toVertex = layerController.getVertexById(toId);
 
 			if (fromVertex == null || toVertex == null) {
 				// Problem! Both vertices are mandatory for the edge!
 				continue;
 			}
 
 			if (fromVertex.getModel().getLayer().getIndex() == toVertex.getModel().getLayer().getIndex()) {
 				// Do not conntect two neurons in the same layer with an
 				// edge!
 				continue;
 			}
 
 			// Create new edge with its synapse and the both vertexes
 			Edge edge = EdgeController.getInstance().getEdgeFactory().create();
 			edge.createModel(fromVertex.getModel(), toVertex.getModel());
 
 			// Add the new edge to the graph
 			graph.addEdge(edge, fromVertex, toVertex, EdgeType.DIRECTED);
 		}
 
 		repaint();
 	}
 
 	private void initGraphMouse() {
 		// Create Graph Mouse (set in and out parameter to '1f' to disable zoom)
 		graphMouse = new GraphMouse<Vertex, Edge>(viewer.getRenderContext(), VertexController.getInstance().getVertexFactory(),
 				EdgeController.getInstance().getEdgeFactory(), 1f, 1f);
 
 		viewer.setGraphMouse(graphMouse);
 		viewer.addKeyListener(graphMouse.getModeKeyListener());
 		viewer.addMouseWheelListener(new MouseWheelListener() {
 
 			@Override
 			public void mouseWheelMoved(final MouseWheelEvent e) {
 			}
 		});
 
 		graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
 		graphMouse.setZoomAtMouse(false);
 	}
 
 	// /**
 	// * Add MouseMode Menu to the MainMenu of the frame to set the mode of the Graph Mouse.
 	// */
 	// private void addMouseModeMenu() {
 	// if (frame == null) {
 	// // No frame -> No menu -> No fun!
 	// return;
 	// }
 	// JMenu modeMenu = graphMouse.getModeMenu();
 	// modeMenu.setText(Settings.getI18n("menu.mousemode", "Mode"));
 	// JMenuBar menu = frame.getJMenuBar();
 	// menu.add(modeMenu, menu.getComponentCount() - 1);
 	// graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
 	// }
 
 	/**
 	 * @param vv
 	 */
 	public static void repaint(final VisualizationViewer<Vertex, Edge> thisViewer) {
 		final int height = thisViewer.getHeight();
 		final int width = thisViewer.getWidth();
 
 		AbstractLayout<Vertex, Edge> thisLayout = getInstance().getLayout();
 		DirectedGraph<Vertex, Edge> thisGraph = getInstance().getGraph();
 
 		// Position of the vertex depends on the number of vertices that
 		// are already in the same layer.
 		// Since new vertices are always added at the last position, get
 		// the number of vertices to compute the position for the new
 		// vertex.
 		LayerController<Layer> layerController = LayerController.getInstance();
 		ArrayList<ArrayList<Vertex>> layers = layerController.getVertices();
 
 		// System.out.println(layers);
 
 		// No layers? -> No need to positionize anything!
 		if (layers.size() == 0) {
 			thisViewer.repaint();
 			return;
 		}
 
 		// Vertex position
 		Point2D location;
 
 		// Gap between the layers
 		int gapY = height / layerController.getLayersSize();
 
 		int layerIndex = -1;
 		for (ArrayList<Vertex> vertices : layers) {
 			layerIndex++;
 
 			int layerSize = vertices.size();
 
 			// Skip empty layers
 			if (layerSize == 0) {
 				continue;
 			}
 
 			// Compute the gap between two vertices
 			int gapX = width / layerSize;
 
 			int vertexIndex = -1;
 			for (Vertex vertex : vertices) {
 				vertexIndex++;
 
 				// Compute location of the vertex
 				int x = vertexIndex * gapX + gapX / 2;
 				int y = layerIndex * gapY + gapY / 2;
 				location = new Point2D.Double(x, y);
 
 				// Set vertex location and lock it
 				thisLayout.setLocation(vertex, location);
 				thisLayout.lock(vertex, true);
 			}
 		}
 
 		Layout<Vertex, Edge> startLayout = thisViewer.getGraphLayout();
 		StaticLayout<Vertex, Edge> endLayout = new StaticLayout<Vertex, Edge>(thisGraph, thisLayout);
 		LayoutTransition<Vertex, Edge> lt = new LayoutTransition<Vertex, Edge>(thisViewer, startLayout, endLayout);
 		Animator animator = new Animator(lt);
 		animator.start();
 		thisViewer.repaint();
 	}
 
 }
