 package de.unikassel.ann.controller;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import de.unikassel.ann.gui.graph.GraphLayoutViewer;
 import de.unikassel.ann.gui.graph.JungLayer;
 import de.unikassel.ann.gui.graph.Vertex;
 import de.unikassel.ann.model.Layer;
 
 public class LayerController<T> {
 
 	private static LayerController<Layer> instance;
 
 	public static LayerController<Layer> getInstance() {
 		if (instance == null) {
 			instance = new LayerController<Layer>();
 		}
 		return instance;
 	}
 
 	// List of jungLayers which contain the vertices
 	protected ArrayList<JungLayer> layers;
 
 	/**
 	 * Constructor
 	 */
 	private LayerController() {
 		layers = new ArrayList<JungLayer>();
 	}
 
 	/**
 	 * Add a layer at the end of the layers list.
 	 */
 	public void addLayer() {
 		addLayer(layers.size());
 	}
 
 	/**
 	 * Add a layer at the given index in the layers list.
 	 * 
 	 * @param index
 	 */
 	public void addLayer(final int index) {
 		// Create new Junglayer as a wrapper which contains the layer and its
 		// vertices. The Junglayer has the same index as the layer.
 		JungLayer jungLayer = new JungLayer(index);
 
 		// Check if the index is out of range for the layers list
 		if (index < 0 || index > layers.size()) {
 			layers.add(jungLayer);
 		} else {
 			layers.add(index, jungLayer);
 		}
 	}
 
 	/**
 	 * Add Vertex (1)
 	 * 
 	 * Add a new vertex to the layer with the index.
 	 * 
 	 * @param layerIndex
 	 * @param addToGraph
 	 * @return boolean
 	 */
 	public boolean addVertex(final int layerIndex) {
 		return addVertex(layerIndex, false);
 	}
 
 	/**
 	 * Add Vertex (2)
 	 * 
 	 * Add a new vertex to the layer with the index.
 	 * 
 	 * @param layerIndex
 	 * @param addToGraph
 	 * @return boolean
 	 */
 	public boolean addVertex(final int layerIndex, final boolean addToGraph) {
 		// Create a new vertex
 		Vertex vertex = VertexController.getInstance().getVertexFactory().create();
 		vertex.setup(layerIndex);
 
 		return addVertex(layerIndex, vertex, addToGraph);
 	}
 
 	/**
 	 * Add Vertex (3)
 	 * 
 	 * Add a vertex with the index to the layer with the index.
 	 * 
 	 * @param layerIndex
 	 * @param vertexIndex
 	 * @return boolean
 	 */
 	public boolean addVertex(final int layerIndex, final int vertexIndex) {
 		return addVertex(layerIndex, vertexIndex, false);
 	}
 
 	/**
 	 * Add Vertex (4)
 	 * 
 	 * Add a vertex with its index to the layer with the given index. Set the addToGraph parameter to TRUE to add the vertex to the graph.
 	 * 
 	 * @param layerIndex
 	 * @param vertexIndex
 	 * @param addToGraph
 	 * @return boolean
 	 */
 	public boolean addVertex(final int layerIndex, final int vertexIndex, final boolean addToGraph) {
 		int layerSize = getLayerSize(layerIndex);
		if (vertexIndex <= layerSize) {
 			// Vertex already exists
 			return false;
 		}
 		return addVertex(layerIndex, addToGraph);
 	}
 
 	/**
 	 * Add Vertex (5)
 	 * 
 	 * Add the vertex to the layer with the index.
 	 * 
 	 * @param layerIndex
 	 * @param vertex
 	 * @param addToGraph
 	 * @return boolean
 	 */
 	public boolean addVertex(final int layerIndex, final Vertex vertex, final boolean addToGraph) {
 		System.out.println("addVertex(" + layerIndex + ", " + vertex + ", " + addToGraph + ")");
 
 		if (layerIndex >= layers.size()) {
 			// Layer with the index does not exist -> Create new Layer
 			addLayer(layerIndex);
 		}
 
 		// Add vertex to the layer
 		JungLayer jungLayer = layers.get(layerIndex);
 		jungLayer.addVertex(vertex);
 
 		// Add vertex to the graph
 		if (addToGraph) {
 			GraphLayoutViewer glv = GraphLayoutViewer.getInstance();
 			glv.getGraph().addVertex(vertex);
 			glv.repaint();
 		}
 
 		return true;
 	}
 
 	public void removeLayer() {
 		int index = layers.size() - 1;
 		layers.remove(index);
 	}
 
 	public void removeLayer(final int index) {
 		JungLayer layer = layers.get(index);
 		List<Vertex> vertices = layer.getVertices();
 
 		// Remove the last layer and all its vertices
 		for (Vertex vertex : vertices) {
 			vertex.remove();
 		}
 		layers.remove(index);
 	}
 
 	public void removeVertex(final int layerIndex, final int vertexIndex) {
 		int layerSize;
 		try {
 			layerSize = getLayerSize(layerIndex);
 		} catch (IndexOutOfBoundsException ex) {
 			// Layer not found -> doesn't it exist?
 			return;
 		}
 		if (layerSize <= vertexIndex) {
 			return;
 		}
 		removeVertex(layerIndex);
 	}
 
 	public void removeVertex(final int layerIndex) {
 		JungLayer layer = layers.get(layerIndex);
 		layer.removeVertex();
 	}
 
 	public ArrayList<JungLayer> getLayers() {
 		return layers;
 	}
 
 	/**
 	 * Get number of vertices in the layer with the index.
 	 * 
 	 * @param layerIndex
 	 * @return
 	 */
 	public int getLayerSize(final int layerIndex) {
 		JungLayer jungLayer;
 		try {
 			jungLayer = layers.get(layerIndex);
 		} catch (Exception ex) {
 			// Layer not found!
 			return 0;
 		}
 		return jungLayer.getVertices().size();
 	}
 
 	/**
 	 * Get the number of layers.
 	 * 
 	 * @return int
 	 */
 	public int getLayersSize() {
 		return getLayers().size();
 	}
 
 	/**
 	 * Get all vertices as lists in a list of all layers.
 	 * 
 	 * @return ArrayList
 	 */
 	public ArrayList<ArrayList<Vertex>> getVertices() {
 		// Sort layers by their index
 		Collections.sort(layers);
 
 		ArrayList<ArrayList<Vertex>> vertices = new ArrayList<ArrayList<Vertex>>();
 		for (JungLayer jungLayer : layers) {
 			if (jungLayer.getIndex() > vertices.size()) {
 				vertices.add(jungLayer.getVertices());
 			} else {
 				vertices.add(jungLayer.getIndex(), jungLayer.getVertices());
 			}
 		}
 		return vertices;
 	}
 
 	/**
 	 * Get all vertices on the layer with the index.
 	 * 
 	 * @param index
 	 * @return
 	 */
 	public List<Vertex> getVerticesInLayer(final int index) {
 		try {
 			return layers.get(index).getVertices();
 		} catch (IndexOutOfBoundsException ex) {
 			// Return empty list
 			return new ArrayList<Vertex>();
 		}
 	}
 
 	/**
 	 * Get a vertice by its id.
 	 * 
 	 * @param id
 	 * @return
 	 */
 	public Vertex getVertexById(final Integer id) {
 		for (JungLayer junglayer : layers) {
 			for (Vertex vertex : junglayer.getVertices()) {
 				if (vertex.getModel().getId().equals(id)) {
 					return vertex;
 				}
 			}
 		}
 		// Vertex not found!
 		return null;
 	}
 
 	public void clear() {
 		// Remove all layers with their vertices
 		layers.clear();
 	}
 
 }
