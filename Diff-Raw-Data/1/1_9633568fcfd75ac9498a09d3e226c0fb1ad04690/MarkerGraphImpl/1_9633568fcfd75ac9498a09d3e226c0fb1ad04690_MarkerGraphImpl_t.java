 package de.uni_koblenz.jgralab.impl.mem;
 
 import java.io.IOException;
 import java.security.InvalidParameterException;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.pcollections.PMap;
 import org.pcollections.PSet;
 import org.pcollections.PVector;
 
 import de.uni_koblenz.jgralab.AttributedElement;
 import de.uni_koblenz.jgralab.BinaryEdge;
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.GraphElement;
 import de.uni_koblenz.jgralab.GraphFactory;
 import de.uni_koblenz.jgralab.GraphIO;
 import de.uni_koblenz.jgralab.GraphIOException;
 import de.uni_koblenz.jgralab.GraphStructureChangedListener;
 import de.uni_koblenz.jgralab.Incidence;
 import de.uni_koblenz.jgralab.NoSuchAttributeException;
 import de.uni_koblenz.jgralab.Record;
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.impl.RemoteGraphDatabaseAccess;
 import de.uni_koblenz.jgralab.schema.EdgeClass;
 import de.uni_koblenz.jgralab.schema.GraphClass;
 import de.uni_koblenz.jgralab.schema.Schema;
 import de.uni_koblenz.jgralab.schema.VertexClass;
 
 /**
  * A MarkerGraph is used as a traversalcontext for VertexType- or EdgeType-induced subgraphs in the evaluation of {@link SubgraphDefinition}s.
  * It may not be used for anything else, which is why all other methods throw {@link UnsupportedOperationException}s.
  * 
  * @author jtheegarten@uni-koblenz.de 2012, Diploma Thesis
  *
  */
 public class MarkerGraphImpl implements Graph {
 
 	private static final String MARKER_GRAPH_EXCEPTION = "The MarkerGraph should only be used as TraversalContext. Any operations have to be executed on the original graph (available via getOriginalGraph()).";
 
 	/** The original graph. Only elements of that graph are accepted as elements of this MarkerGraph.
 	 * 
 	 */
 	private Graph originalGraph = null;
 
 	/**
 	 * Stores the traversal context of each {@link Thread} working on this
 	 * {@link Graph}.
 	 */
 	private HashMap<Thread, Stack<Graph>> traversalContextMap;
 	private HashMap<Long, Vertex> vertexMap;
 	private HashMap<Long, Edge> edgeMap;
 	private HashMap<Long, Incidence> incidenceMap;
 
 	public MarkerGraphImpl(Graph graph) {
 		originalGraph = graph;
 		vertexMap = new HashMap<Long, Vertex>((int) (graph.getVCount() / 10));
 		edgeMap = new HashMap<Long, Edge>((int) (graph.getECount() / 10));
 		incidenceMap = new HashMap<Long, Incidence>(
 				(int) (graph.getICount() / 10));
 	}
 
 	@Override
 	public void readAttributeValueFromString(String attributeName, String value)
 			throws GraphIOException, NoSuchAttributeException {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public String writeAttributeValueToString(String attributeName)
 			throws IOException, GraphIOException, NoSuchAttributeException {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public void writeAttributeValues(GraphIO io) throws IOException,
 			GraphIOException {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public void readAttributeValues(GraphIO io) throws GraphIOException {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public Object getAttribute(String name) throws NoSuchAttributeException {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public void setAttribute(String name, Object data)
 			throws NoSuchAttributeException {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public void initializeAttributesWithDefaultValues() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public Graph getGraph() {
 		return this;
 	}
 
 	public Graph getOriginalGraph() {
 		return originalGraph;
 	}
 
 	@Override
 	public Class<? extends Graph> getM1Class() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public GraphClass getType() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public GraphClass getGraphClass() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Schema getSchema() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public int compareTo(Graph o) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Graph getTraversalContext() {
 		if (traversalContextMap == null) {
 			return null;
 		}
 		Stack<Graph> stack = traversalContextMap.get(Thread.currentThread());
 		if (stack == null || stack.isEmpty()) {
 			return this;
 		} else {
 			return stack.peek();
 		}
 	}
 
 	@Override
 	public void useAsTraversalContext() {
 		setTraversalContext(this);
 	}
 
 	@Override
 	public void releaseTraversalContext() {
 		if (traversalContextMap == null) {
 			return;
 		}
 		Stack<Graph> stack = this.traversalContextMap.get(Thread
 				.currentThread());
 		if (stack != null) {
 			stack.pop();
 			if (stack.isEmpty()) {
 				traversalContextMap.remove(Thread.currentThread());
 				if (traversalContextMap.isEmpty()) {
 					traversalContextMap = null;
 				}
 			}
 		}
 	}
 
 	public void setTraversalContext(Graph traversalContext) {
 		if (this.traversalContextMap == null) {
 			this.traversalContextMap = new HashMap<Thread, Stack<Graph>>();
 		}
 		Stack<Graph> stack = this.traversalContextMap.get(Thread
 				.currentThread());
 		if (stack == null) {
 			stack = new Stack<Graph>();
 			this.traversalContextMap.put(Thread.currentThread(), stack);
 		}
 		stack.add(traversalContext);
 	}
 
 	@Override
 	public Graph getCompleteGraph() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Graph getLocalPartialGraph() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public AttributedElement getParentGraphOrElement() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Graph getParentGraph() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public boolean isPartOfGraph(Graph other) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Graph getView(int kappa) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Graph getViewedGraph() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Graph createPartialGraphInGraph(String hostnameOfPartialGraph) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public List<? extends Graph> getPartialGraphs() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Graph getPartialGraph(int partialGraphId) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Deprecated
 	@Override
 	public void savePartialGraphs(GraphIO graphIO) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public String getUniqueGraphId() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public long getGlobalId() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public int getLocalId() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public int getPartialGraphId() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public boolean isLocalElementId(long id) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public <T extends Vertex> T createVertex(Class<T> cls) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public <T extends Edge> T createEdge(Class<T> cls) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public <T extends BinaryEdge> T createEdge(Class<T> cls, Vertex alpha,
 			Vertex omega) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public <T extends Incidence> T connect(Class<T> cls, Vertex vertex,
 			Edge edge) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public boolean containsVertex(Vertex v) {
 		return (v != null) && (v.getGraph() == this.getOriginalGraph())
 				&& (vertexMap.get(((VertexImpl) v).id) == v);
 	}
 
 	@Override
 	public boolean containsEdge(Edge e) {
 		return (e != null) && (e.getGraph() == this.getOriginalGraph())
 				&& (edgeMap.get(((EdgeImpl) e).id) == e);
 	}
 
 	@Override
 	public boolean containsElement(GraphElement elem) {
 		if (elem instanceof Vertex) {
 			return containsVertex((Vertex) elem);
 		} else if (elem instanceof Edge) {
 			return containsEdge((Edge) elem);
 		} else {
 			throw new InvalidParameterException(
 					"Only GraphElements (Edges and Vertices) can be checked by this method.");
 		}
 	}
 
 	public boolean addVertexWithIncidences(Vertex v) {
 		assert v != null;
 		if (!(v.getGraph() == getOriginalGraph())) {
 			return false;
 		}
 		if (vertexMap.get(v.getGlobalId()) == null) {
 			vertexMap.put(v.getGlobalId(), v);
 			for (Incidence i : v.getIncidences()) {
 				addIncidence(i);
 				addEdge(i.getEdge());
 			}
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	public boolean addEdgeWithIncidences(Edge e) {
 		assert e != null;
 		if (!(e.getGraph() == getOriginalGraph())) {
 			return false;
 		}
 		if (edgeMap.get(e.getGlobalId()) == null) {
 			edgeMap.put(e.getGlobalId(), e);
 			for (Incidence i : e.getIncidences()) {
 				addIncidence(i);
 				addVertex(i.getVertex());
 			}
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	private boolean addIncidence(Incidence i) {
 		assert i != null;
 		if (!(i.getGraph() == getOriginalGraph())) {
 			return false;
 		}
 		if (incidenceMap.get(i.getGlobalId()) == null) {
 			incidenceMap.put(i.getGlobalId(), i);
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	private boolean addVertex(Vertex v) {
 		assert v != null;
 		if (!(v.getGraph() == getOriginalGraph())) {
 			return false;
 		}
 		if (vertexMap.get(v.getGlobalId()) == null) {
 			vertexMap.put(v.getGlobalId(), v);
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	private boolean addEdge(Edge e) {
 		assert e != null;
 		if (!(e.getGraph() == getOriginalGraph())) {
 			return false;
 		}
 		if (edgeMap.get(e.getGlobalId()) == null) {
 			edgeMap.put(e.getGlobalId(), e);
 		} else {
 			return false;
 		}
 		return true;
 	}
 
 	@Override
 	public void deleteVertex(Vertex v) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public void deleteEdge(Edge e) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public Vertex getFirstVertex() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Vertex getLastVertex() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Vertex getFirstVertex(VertexClass vertexClass) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Vertex getFirstVertex(VertexClass vertexClass, boolean noSubclasses) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Vertex getFirstVertex(Class<? extends Vertex> vertexClass) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Vertex getFirstVertex(Class<? extends Vertex> vertexClass,
 			boolean noSubclasses) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Edge getFirstEdge() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Edge getLastEdge() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Edge getFirstEdge(EdgeClass edgeClass) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Edge getFirstEdge(EdgeClass edgeClass, boolean noSubclasses) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Edge getFirstEdge(Class<? extends Edge> edgeClass) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Edge getFirstEdge(Class<? extends Edge> edgeClass,
 			boolean noSubclasses) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Vertex getVertex(long id) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Edge getEdge(long id) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public long getMaxVCount() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public long getMaxECount() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public long getMaxICount() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public long getVCount() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public long getECount() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public long getICount() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Iterable<Vertex> getVertices() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Iterable<Vertex> getVertices(VertexClass vertexclass) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Iterable<Vertex> getVertices(Class<? extends Vertex> vertexClass) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Iterable<Edge> getEdges() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Iterable<Edge> getEdges(EdgeClass edgeClass) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Iterable<Edge> getEdges(Class<? extends Edge> edgeClass) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public void sortVertices(Comparator<Vertex> comp) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public void sortEdges(Comparator<Edge> comp) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public void defragment() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public void addGraphStructureChangedListener(
 			GraphStructureChangedListener newListener) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public void removeGraphStructureChangedListener(
 			GraphStructureChangedListener listener) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public void removeAllGraphStructureChangedListeners() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 
 	}
 
 	@Override
 	public int getGraphStructureChangedListenerCount() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public boolean isLoading() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public boolean isGraphModified(long previousVersion) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public long getGraphVersion() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public boolean isVertexListModified(long previousVersion) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public long getVertexListVersion() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public boolean isEdgeListModified(long edgeListVersion) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public long getEdgeListVersion() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public GraphFactory getGraphFactory() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public RemoteGraphDatabaseAccess getGraphDatabase() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public <T> PVector<T> createList() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public <T> PSet<T> createSet() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public <K, V> PMap<K, V> createMap() {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 	@Override
 	public Record createRecord(Class<? extends Record> recordDomain,
 			Map<String, Object> values) {
 		throw new UnsupportedOperationException(MARKER_GRAPH_EXCEPTION);
 	}
 
 }
