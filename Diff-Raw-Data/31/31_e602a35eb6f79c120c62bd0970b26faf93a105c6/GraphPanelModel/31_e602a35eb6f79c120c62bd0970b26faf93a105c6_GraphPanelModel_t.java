 package logic;
 
 import defs.DecorableConstants;
 import defs.EdgeFormat;
 import java.awt.Point;
 import java.util.Iterator;
 import java.util.Observable;
 import defs.GUICommandConstants;
 import defs.FormatHelper;
 import defs.GraphFormat;
 import defs.ModelEventConstants;
 import defs.VertexFormat;
 import logic.extlib.Edge;
 import logic.extlib.GraphExamples;
 import logic.extlib.IncidenceListGraph;
 import logic.extlib.Vertex;
 import ui.controls.ChooseStartVertexDialog;
 import ui.controls.GraphFormatDialog;
 
 public class GraphPanelModel<V, E> extends Observable {
 	// Members
 	private boolean isGUIRefreshDisabled = false;
 	private IncidenceListGraph<V, E> graph;
 	private GraphExamples<V, E> graphExamples;
 	Vertex<V> selectedVertex = null;
 	Vertex<V> changedVertex = null;
 	private GraphDataProcessor<V, E> graphDataProcessor = new GraphDataProcessor<V, E>();
 	private final AlgorithmDataProcessor algorithmDataProcessor = new AlgorithmDataProcessor();
 
 	// End of Members
 
 	// Constructors
 	public GraphPanelModel(IncidenceListGraph<V, E> g) {
 		this.graphExamples = new GraphExamples<V, E>();
 		this.graphExamples.setRecorder(algorithmDataProcessor);
 		// input graph?
 		if (null != g) {
 			setExternalGraph(g);
 		} else {
 			setNewGraph();
 		}
 
 	}
 
 	// End of Constructors
 
 	private void setNewGraph() {
 		GraphFormat format = new GraphFormat();
 		GraphFormatDialog gFormatDialog = new GraphFormatDialog(format, true);
 		gFormatDialog.setVisible(true);
 		if (gFormatDialog.getSaved()) {
 			format = gFormatDialog.getFormat();
 		}
 		if (gFormatDialog.getSaved() || null == this.graph) {
 			this.graph = new IncidenceListGraph<V, E>(format.isDirected());
 			this.graph.set(FormatHelper.FORMAT, format);
 		}
 	}
 
 	private void setExternalGraph(IncidenceListGraph<V, E> g) {
 		this.graph = g;
 		GraphFormat format = FormatHelper.getFormat(GraphFormat.class,
 				this.graph);
 		if (null == format) {
 			format = new GraphFormat();
 		}
 		format.setDirected(this.graph.isDirected());
 		this.graph.set(FormatHelper.FORMAT, format);
 	}
 
 	// Methods
 	public IncidenceListGraph<V, E> getGraph() {
 		return this.graph;
 	}
 
 	public GraphFormat getGraphFormat() {
 		GraphFormat f = FormatHelper.getFormat(GraphFormat.class, this.graph);
 		if (null == f) {
 			f = new GraphFormat();
 		}
 		return f;
 	}
 
 	public Vertex<V> getSelectedVertex() {
 		return this.selectedVertex;
 	}
 
 	public void setSelectedVertex(Vertex<V> selectedVertex) {
 		if (null == this.selectedVertex
 				|| !this.selectedVertex.equals(selectedVertex)) {
 			this.selectedVertex = selectedVertex;
 			setChanged();
 			notifyObservers(ModelEventConstants.VERTEXSELECTION);
 		}
 	}
 
 	public Vertex<V> getChangedVertex() {
 		return this.changedVertex;
 	}
 
 	public boolean isGUIRefreshDisabled() {
 		return this.isGUIRefreshDisabled;
 	}
 
 	// Graph manipulation Methods
 	public void addVertex(Vertex<V> sourceVertex, VertexFormat format,
 			double weight) {
 		// Update data
 		// create object
 		V vElement = null;
 		Vertex<V> vNew = this.graph.insertVertex(vElement);
 		// set format
 		if (null == format) {
 			format = new VertexFormat();
 		}
 
 		if (null != sourceVertex) {
 			// Place the new vertex under the source vertex
 			VertexFormat sourceFormat = FormatHelper.getFormat(
 					VertexFormat.class, sourceVertex);
 			if (null != sourceFormat) {
 				Point sourceCenter = sourceFormat.getCenterPoint();
 				if (null != sourceCenter) {
 					format.setCenterPoint(sourceCenter.x, sourceCenter.y + 2
 							* GraphFormat.OUTERCIRCLEDIAMETER);
 				}
 			}
 		} else {
 			// Place the vertex at a default location
 			format.setCenterPoint(2 * GraphFormat.OUTERCIRCLEDIAMETER,
 					2 * GraphFormat.OUTERCIRCLEDIAMETER);
 		}
 		vNew.set(FormatHelper.FORMAT, format);
 
 		if (null != sourceVertex) {
 			// connect via edge if has source (if there is no source, it will be
 			// null)
 			E eElement = null;
 			Edge edge = this.graph.insertEdge(sourceVertex, vNew, eElement);
 			if (weight > Double.NEGATIVE_INFINITY) {
 				edge.set(DecorableConstants.WEIGHT, weight);
 			}
 		}
 
 		// Update UI
 		changedVertex = vNew;
 		setChanged();
 		notifyObservers(ModelEventConstants.VERTEXADDED);
 	}
 
 	public void connectVertices(Vertex<V> sourceVertex, Vertex<V> targetVertex,
 			double weight) {
 		// Check variables
 		if (null == sourceVertex || null == targetVertex) {
 			return;
 		}
 
 		// connect via new Edge
 		E eElement = null;
 		Edge<E> edge = this.graph.insertEdge(sourceVertex, targetVertex,
 				eElement);
 		if (weight > Double.NEGATIVE_INFINITY) {
 			edge.set(DecorableConstants.WEIGHT, weight);
 		}
 
 		// Update UI
 		changedVertex = sourceVertex; // For the edge recalculations
 		setChanged();
 		notifyObservers(ModelEventConstants.VERTEXCONNECTED);
 	}
 
 	public void deleteSelectedVertex() {
 		// Check variables
 		if (null == this.selectedVertex) {
 			return;
 		}
 
 		// Update data
 		// Remove Vertex
 		this.graph.removeVertex(this.selectedVertex);
 
 		// Update UI
 		changedVertex = this.selectedVertex;
 		setChanged();
 		notifyObservers(ModelEventConstants.VERTEXDELETED);
 	}
 
 	public void updateFormat(GraphFormat newFormat) {
 		if (null == newFormat) {
 			newFormat = FormatHelper.getFormat(GraphFormat.class, this.graph);
 			if (null == newFormat) {
 				newFormat = new GraphFormat();
 			}
 		}
 		this.graph.setDirected(newFormat.isDirected());
 		Iterator<Edge<E>> itE = this.graph.edges();
 		Edge<E> edge = null;
 		while (itE.hasNext()) {
 			edge = itE.next();
 			if (newFormat.isWeighted()) {
 				if (!edge.has(DecorableConstants.WEIGHT)) {
 					edge.set(DecorableConstants.WEIGHT, 0);
 				}
 			} else {
 				if (edge.has(DecorableConstants.WEIGHT)) {
 					edge.destroy(DecorableConstants.WEIGHT);
 				}
 			}
 		}
 		// observable implementation notifies the gui
 		this.graph.set(FormatHelper.FORMAT, newFormat);
 	}
 
 	public void resetFormatAndDecorable(IncidenceListGraph graph) {
 		IncidenceListGraph graphToReset = graph;
 		Iterator<Vertex<V>> vIt = graphToReset.vertices();
 		Vertex<V> vertex = null;
 		VertexFormat vertexFormat = null;
 		while (vIt.hasNext()) {
 			vertex = vIt.next();
 			if (vertex.has(DecorableConstants.VISITED)) {
 				vertex.destroy(DecorableConstants.VISITED);
 			}
			if (vertex.has(DecorableConstants.ACTIVE)) {
				vertex.destroy(DecorableConstants.ACTIVE);
			}
 			if (vertex.has(DecorableConstants.MSF)) {
 				vertex.destroy(DecorableConstants.MSF);
 			}
 			if (vertex.has(DecorableConstants.PQLOCATOR)) {
 				vertex.destroy(DecorableConstants.PQLOCATOR);
 			}
 			if (vertex.has(DecorableConstants.DISTANCE)) {
 				vertex.destroy(DecorableConstants.DISTANCE);
 			}
 			FormatHelper.getFormat(VertexFormat.class, vertex).setUnvisited();
 		}
 
 		Iterator<Edge<E>> eIt = graphToReset.edges();
 		Edge edge = null;
 		while (eIt.hasNext()) {
 			edge = eIt.next();
 			if (edge.has(DecorableConstants.VISITED)) {
 				edge.destroy(DecorableConstants.VISITED);
 			}
 			if (edge.has(DecorableConstants.MSF)) {
 				edge.destroy(DecorableConstants.MSF);
 			}
 			FormatHelper.getFormat(EdgeFormat.class, edge).setUnvisited();
 		}
 	}
 
 	// End of graph manipulation methods
 
 	// Main GUI handlers
 	public void handleMainGUICommand(String gUICommandConstant, Object param) {
 		// apply algorithms
 		if (gUICommandConstant.equals(GUICommandConstants.DIJKSTRA)) {
 			this.algorithmDataProcessor.resetGraphList();
 			resetFormatAndDecorable(this.graph);
 			ChooseStartVertexDialog csvDialog = new ChooseStartVertexDialog(
 					this.graph.vertices());
 			if (csvDialog.getSaved()) {
 				this.isGUIRefreshDisabled = true;
 				this.graphExamples.dijkstra(this.graph,
 						csvDialog.getStartVertex());
 				this.isGUIRefreshDisabled = false;
 				this.setExternalGraph(this.algorithmDataProcessor.first());
 				setChanged();
 				notifyObservers(ModelEventConstants.GRAPHREPLACED);
 			}
 		} else if (gUICommandConstant.equals(GUICommandConstants.KRUSKAL)) {
 			this.algorithmDataProcessor.resetGraphList();
 			resetFormatAndDecorable(this.graph);
 			this.isGUIRefreshDisabled = true;
 			this.graphExamples.kruskal(this.graph);
 			this.isGUIRefreshDisabled = false;
 			this.setExternalGraph(this.algorithmDataProcessor.first());
 			setChanged();
 			notifyObservers(ModelEventConstants.GRAPHREPLACED);
 
 		} else if (gUICommandConstant.equals(GUICommandConstants.BFS)) {
 			this.algorithmDataProcessor.resetGraphList();
 			resetFormatAndDecorable(this.graph);
 			ChooseStartVertexDialog csvDialog = new ChooseStartVertexDialog(
 					this.graph.vertices());
 			if (csvDialog.getSaved()) {
 				this.isGUIRefreshDisabled = true;
 				this.graphExamples.bfs(this.graph, csvDialog.getStartVertex());
 				this.isGUIRefreshDisabled = false;
 				this.setExternalGraph(this.algorithmDataProcessor.first());
 				setChanged();
 				notifyObservers(ModelEventConstants.GRAPHREPLACED);
 			}
 		} else if (gUICommandConstant.equals(GUICommandConstants.CUSTOMGRAPH)) {
 			this.algorithmDataProcessor.resetGraphList();
 			resetFormatAndDecorable(this.graph);
 			ChooseStartVertexDialog csvDialog = new ChooseStartVertexDialog(
 					this.graph.vertices());
 			if (csvDialog.getSaved()) {
 				this.isGUIRefreshDisabled = true;
 				this.graphExamples.customAlgorithm(this.graph,
 						csvDialog.getStartVertex());
 				this.isGUIRefreshDisabled = false;
 				this.setExternalGraph(this.algorithmDataProcessor.first());
 				setChanged();
 				notifyObservers(ModelEventConstants.GRAPHREPLACED);
 			}
 		}
 		// Iterate throug completed Algorithm
 		else if (gUICommandConstant.equals(GUICommandConstants.FORWARD)) {
 			this.setExternalGraph(this.algorithmDataProcessor.forward());
 			setChanged();
 			notifyObservers(ModelEventConstants.GRAPHREPLACED);
 		}
 
 		else if (gUICommandConstant.equals(GUICommandConstants.BACKWARD)) {
 			this.setExternalGraph(this.algorithmDataProcessor.backward());
 			setChanged();
 			notifyObservers(ModelEventConstants.GRAPHREPLACED);
 		} else if (gUICommandConstant.equals(GUICommandConstants.RUN)) {
 			this.setExternalGraph(this.algorithmDataProcessor.first());
 			setChanged();
 			notifyObservers(ModelEventConstants.GRAPHREPLACED);
 			int listSize = this.algorithmDataProcessor.getGraphList().size();
 			for (int i = 0; i < listSize; i++) {
 
 				this.setExternalGraph(this.algorithmDataProcessor.forward());
 				setChanged();
 				notifyObservers(ModelEventConstants.GRAPHREPLACED);
 			}
 
 		} else if (gUICommandConstant.equals(GUICommandConstants.FIRST)) {
 			this.setExternalGraph(this.algorithmDataProcessor.first());
 			setChanged();
 			notifyObservers(ModelEventConstants.GRAPHREPLACED);
 		} else if (gUICommandConstant.equals(GUICommandConstants.LAST)) {
 			this.setExternalGraph(this.algorithmDataProcessor.last());
 			setChanged();
 			notifyObservers(ModelEventConstants.GRAPHREPLACED);
 		}
 		// load/save/clear graph
 		else if (gUICommandConstant.equals(GUICommandConstants.NEWGRAPH)) {
 			setNewGraph();
 			setChanged();
 			notifyObservers(ModelEventConstants.GRAPHREPLACED);
 		} else if (gUICommandConstant.equals(GUICommandConstants.LOADGRAPH)) {
 			if (null != param && String.class.isInstance(param)) {
 				IncidenceListGraph<V, E> g = graphDataProcessor
 						.importGraph((String) param);
 				if (null != g) {
 					setExternalGraph(g);
 				}
 				setChanged();
 				notifyObservers(ModelEventConstants.GRAPHREPLACED);
 			}
 		} else if (gUICommandConstant.equals(GUICommandConstants.SAVEGRAPH)) {
 			if (null != param && String.class.isInstance(param)) {
 				graphDataProcessor.exportGraph(this.graph, (String) param);
 			}
 		}
 
 	}
 	// Main GUI handlers
 
 	// End of Methods
 
 }
