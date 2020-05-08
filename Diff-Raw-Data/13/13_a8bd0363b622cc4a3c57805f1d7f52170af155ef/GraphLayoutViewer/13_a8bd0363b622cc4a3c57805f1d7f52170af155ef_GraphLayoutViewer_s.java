 package de.unikassel.ann.gui.graph;
 
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseWheelEvent;
 import java.awt.event.MouseWheelListener;
 import java.awt.geom.Point2D;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 
 import de.unikassel.ann.controller.EdgeController;
 import de.unikassel.ann.controller.LayerController;
 import de.unikassel.ann.controller.Settings;
 import de.unikassel.ann.controller.VertexController;
 import de.unikassel.ann.factory.EdgeFactory;
 import de.unikassel.ann.factory.VertexFactory;
 import de.unikassel.ann.model.Layer;
 import de.unikassel.ann.model.Network;
 import de.unikassel.ann.model.Neuron;
 import de.unikassel.ann.model.Synapse;
 import edu.uci.ics.jung.algorithms.layout.AbstractLayout;
 import edu.uci.ics.jung.algorithms.layout.StaticLayout;
 import edu.uci.ics.jung.graph.DirectedGraph;
 import edu.uci.ics.jung.graph.DirectedSparseGraph;
 import edu.uci.ics.jung.graph.util.EdgeType;
 import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
 import edu.uci.ics.jung.visualization.VisualizationViewer;
 import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
 
 public class GraphLayoutViewer {
 
 	/*
 	 * fields
 	 */
 	protected DirectedGraph<Vertex, Edge> graph;
 	protected AbstractLayout<Vertex, Edge> layout;
 	protected VisualizationViewer<Vertex, Edge> viewer;
 	protected VertexFactory vertexFactory;
 	protected EdgeFactory edgeFactory;
 	protected GraphMouse<Vertex, Edge> graphMouse;
 
 	private JFrame frame;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param Dimension
 	 *            dim
 	 * @param Container
 	 *            parent
 	 */
 	public GraphLayoutViewer(final Dimension dim, final Container parent) {
 		graph = new DirectedSparseGraph<Vertex, Edge>();
 		layout = new StaticLayout<Vertex, Edge>(graph, dim);
 
 		viewer = new VisualizationViewer<Vertex, Edge>(layout);
 		viewer.setBackground(Color.white);
 		viewer.addPreRenderPaintable(new VisualizationViewer.Paintable() {
 			@Override
 			public void paint(final Graphics g) {
 				final int height = viewer.getHeight();
 				final int width = viewer.getWidth();
 
 				// Position of the vertex depends on the number of vertices that
 				// are already in the same layer.
 				// Since new vertices are always added at the last position, get
 				// the number of vertices to compute the position for the new
 				// vertex.
 				LayerController<Layer> layerController = LayerController
 						.getInstance();
 
 				// TODO Take the vertices from the graph or from the
 				// LayerController?
 				// NOTE Vorteil bei den Vertices des LayerControllers: Sie sind
 				// bereits nach Layer sortiert -> Ueberpruefung ob Vertex im
 				// gleichen Layer liegt entfaellt!
 				HashMap<Layer, List<Vertex>> vertices = layerController
 						.getVertices();
 
 				// No vertices? -> No fun!
 				if (vertices.size() == 0) {
 					return;
 				}
 
 				// Collection<Vertex> vertices = graph.getVertices();
 
 				// List to sort the vertices by their indexes
 				// List<Vertex> verticesSorted = new ArrayList<Vertex>();
 
 				// int count = 0, layer = 0, index = 0, dist, numVertices;
 				Point2D location;
 
 				// Count the number of vertices in each layer
 				// for (Vertex v : vertices) {
 				// verticesSorted.add(v);
 				// }
 
 				// Sort the vertices accordingly to their index.
 				// Collections.sort(verticesSorted);
 
 				// Gap between the layers
 				int gapY = height / layerController.getLayer().size();
 
 				Iterator<Entry<Layer, List<Vertex>>> iter;
 				Map.Entry<Layer, List<Vertex>> entry;
 				Layer layer;
 				List<Vertex> layerVertices;
 
 				iter = vertices.entrySet().iterator();
 				while (iter.hasNext()) {
 					entry = (Entry<Layer, List<Vertex>>) iter.next();
 					layer = entry.getKey();
 					layerVertices = entry.getValue();
 
 					// Skip empty layers
 					if (layerVertices.size() == 0) {
 						continue;
 					}
 
 					// Compute the gap between two vertices
 					int gapX = width / layerVertices.size();
 
 					int vertexIndex = -1;
 					for (Vertex vertex : layerVertices) {
 						vertexIndex++;
 
 						// Compute location of the vertex
 						int x = vertexIndex * gapX + (gapX / 2);
 						int y = layer.getIndex() * gapY + (gapY / 2);
 						location = new Point2D.Double(x, y);
 
 						// Set vertex location and lock it
 						layout.setLocation(vertex, location);
 						layout.lock(vertex, true);
 					}
 				}
 
 				// This is where the magic happens...
 				// for (Vertex v : verticesSorted) {
 				// // Get layer of the vertex and set its index within this
 				// // layer
 				// if (v.getLayer() == layer) {
 				// index++;
 				// } else {
 				// index = 0;
 				// layer = v.getLayer();
 				// }
 				// // The number of gaps between the vertices in the current
 				// // layer depends on the number of vertices in it.
 				// numVertices = layerController.getNumVerticesInLayer(layer);
 				//
 				// // Set distance between two vertices
 				// dist = width / numVertices;
 				//
 				// // Set the location of the current vertex
 				// location = new Point2D.Double(index * dist, (layer + 1)
 				// * rowHeight);
 				//
 				// System.out.println(v);
 				// System.out.println(location);
 				//
 				// layout.setLocation(v, location);
 				// layout.lock(v, true);
 				// }
 			}
 
 			@Override
 			public boolean useTransform() {
 				return false;
 			}
 		});
 
 		//
 		// Controller
 		//
 		// TODO DISCUSS: Is a GraphController which creates the Vertex- and
 		// EdgeController more sensible?
 
 		// Vertex
 		VertexController.getInstance().init(graph, viewer);
 
 		// Edge
 		EdgeController.getInstance().init(graph, viewer);
 
 		//
 		// Panel
 		// TODO DISCUSS use really the GraphZoomScollPane or maybe create an own
 		//
 		final GraphZoomScrollPane panel = new GraphZoomScrollPane(viewer);
 		parent.add(panel);
 
 		//
 		// Factories
 		//
 		vertexFactory = new VertexFactory();
 		edgeFactory = new EdgeFactory();
 	}
 
 	public void init() {
 		//
 		// Graph Mouse
 		//
 		initGraphMouse();
 		addMouseModeMenu();
 	}
 
 	public void setFrame(final JFrame frame) {
 		this.frame = frame;
 	}
 
 	public void repaint() {
 		viewer.repaint();
 	}
 
 	/**
 	 * Remove all vertices and their edges from the graph.
 	 */
 	// public void clear() {
 	// HashMap<Layer, List<Vertex>> vertices = LayerController.getInstance()
 	// .getVertices();
 	// Iterator<Entry<Layer, List<Vertex>>> iter;
 	// Map.Entry<Layer, List<Vertex>> entry;
 	// List<Vertex> layerVertices;
 	//
 	// iter = vertices.entrySet().iterator();
 	// while (iter.hasNext()) {
 	// entry = (Entry<Layer, List<Vertex>>) iter.next();
 	// layerVertices = entry.getValue();
 	//
 	// for (Vertex vertex : layerVertices) {
 	// // vertex.remove();
 	// graph.removeVertex(vertex);
 	// }
 	// }
 	// repaint();
 	// }
 
 	/**
 	 * Render neurons and their synapses as Vertices and Edges into the Graph.
 	 * 
 	 * @param network
 	 */
 	public void renderNetwork(final Network network) {
 		// Clear current view -> TODO
 		// clear();
 
 		//
 		// Render Vertices into their Layers
 		//
 		LayerController<Layer> layerController = LayerController.getInstance();
 		List<Layer> layers = network.getLayers();
 		for (Layer layer : layers) {
 			layerController.addLayer(layer);
 
 			List<Neuron> neurons = layer.getNeurons();
 			for (Neuron neuron : neurons) {
 				Vertex vertex = vertexFactory.create();
 
 				// Set the id of the neuron as index of the vertex
 				vertex.setIndex(neuron.getId());
 				vertex.setModel(neuron);
 
 				// Add the new vertex to the current jung layer
 				layerController.addVertex(layer.getIndex(), vertex);
 
 				// Add the new vertex to the graph
 				graph.addVertex(vertex);
 			}
 		}
 
 		//
 		// Render Synapses
 		//
 		Set<Synapse> synapseSet = network.getSynapseSet();
 		for (Iterator iterator = synapseSet.iterator(); iterator.hasNext();) {
 			Synapse synapse = (Synapse) iterator.next();
 
 			// Get the vertices of the synapse by their id of the their models
 			Integer fromId = synapse.getFromNeuron().getId();
 			Integer toId = synapse.getToNeuron().getId();
 
 			Vertex fromVertex = layerController.getVertexById(fromId);
 			Vertex toVertex = layerController.getVertexById(toId);
 
 			if (fromVertex == null || toVertex == null) {
 				// Problem! Both vertices are mandatory for the edge!
 				continue;
 			}
 
 			// Create new edge with its synapse and the both vertexes
 			Edge edge = edgeFactory.create();
 			edge.createModel(fromVertex.getModel(), toVertex.getModel());
 
 			// Add the new edge to the graph
 			graph.addEdge(edge, fromVertex, toVertex, EdgeType.DIRECTED);
 		}
 
 		repaint();
 	}
 
 	private void initGraphMouse() {
 		// Create Graph Mouse (set in and out parameter to '1f' to disable zoom)
 		graphMouse = new GraphMouse<Vertex, Edge>(viewer.getRenderContext(),
 				vertexFactory, edgeFactory, 1f, 1f);
 
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
 
 	/**
 	 * Add MouseMode Menu to the MainMenu of the frame to set the mode of the
 	 * Graph Mouse.
 	 */
 	private void addMouseModeMenu() {
 		if (frame == null) {
 			// No frame -> No menu -> No fun!
 			return;
 		}
 		JMenu modeMenu = graphMouse.getModeMenu();
 		modeMenu.setText(Settings.getI18n("menu.mousemode", "Mode"));
		modeMenu.setIcon(null);
		modeMenu.setPreferredSize(new Dimension(50, 20));
 		JMenuBar menu = frame.getJMenuBar();
 		menu.add(modeMenu, menu.getComponentCount() - 1);
 		graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
 	}
 
 }
