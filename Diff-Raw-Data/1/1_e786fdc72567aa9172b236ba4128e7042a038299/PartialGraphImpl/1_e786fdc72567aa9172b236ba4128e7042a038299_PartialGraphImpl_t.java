 package de.uni_koblenz.jgralab.impl;
 
 import java.io.IOException;
 import java.util.Collection;
 import java.util.Map;
 
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.GraphIO;
 import de.uni_koblenz.jgralab.GraphIOException;
 import de.uni_koblenz.jgralab.JGraLabList;
 import de.uni_koblenz.jgralab.JGraLabMap;
 import de.uni_koblenz.jgralab.JGraLabSet;
 import de.uni_koblenz.jgralab.NoSuchAttributeException;
 import de.uni_koblenz.jgralab.Record;
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.schema.GraphClass;
 import de.uni_koblenz.jgralab.schema.Schema;
 
 public abstract class PartialGraphImpl extends GraphBaseImpl {
 	
 	/* holds the graph this partial graph belongs to */
 	protected Graph completeGraph;
 	
 	boolean loading = false;
 
 	protected PartialGraphImpl(String id, GraphClass cls, Graph completeGraph) {
 		super(id, cls);
 		this.completeGraph = completeGraph;
 	}
 
 	@Override
 	public Graph getCompleteGraph() {
 		return completeGraph;
 	}
 
 	@Override
 	public Graph getView(int kappa) {
 		return graphFactory.createViewGraph(this, kappa);
 	}
 
 	@Override
 	public boolean isLoading() {
 		return loading;
 	}
 
 	@Override
 	public void loadingCompleted() {
 		loading = false;
 	}
 
 	@Override
 	public boolean containsVertex(Vertex v) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean containsEdge(Edge e) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void deleteVertex(Vertex v) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void deleteEdge(Edge e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Vertex getVertex(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Edge getEdge(int id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public int getMaxVCount() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getMaxECount() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public int getICount() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public String getUid() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void defragment() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public <T> JGraLabList<T> createList() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T> JGraLabList<T> createList(Collection<? extends T> collection) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T> JGraLabList<T> createList(int initialCapacity) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T> JGraLabSet<T> createSet() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T> JGraLabSet<T> createSet(Collection<? extends T> collection) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T> JGraLabSet<T> createSet(int initialCapacity) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T> JGraLabSet<T> createSet(int initialCapacity, float loadFactor) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <K, V> JGraLabMap<K, V> createMap() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <K, V> JGraLabMap<K, V> createMap(Map<? extends K, ? extends V> map) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <K, V> JGraLabMap<K, V> createMap(int initialCapacity) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <K, V> JGraLabMap<K, V> createMap(int initialCapacity,
 			float loadFactor) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T extends Record> T createRecord(Class<T> recordClass, GraphIO io) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T extends Record> T createRecord(Class<T> recordClass,
 			Map<String, Object> fields) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public <T extends Record> T createRecord(Class<T> recordClass,
 			Object... components) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void readAttributeValueFromString(String attributeName, String value)
 			throws GraphIOException, NoSuchAttributeException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public String writeAttributeValueToString(String attributeName)
 			throws IOException, GraphIOException, NoSuchAttributeException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void writeAttributeValues(GraphIO io) throws IOException,
 			GraphIOException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void readAttributeValues(GraphIO io) throws GraphIOException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Object getAttribute(String name) throws NoSuchAttributeException {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void setAttribute(String name, Object data)
 			throws NoSuchAttributeException {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public Class<? extends Graph> getM1Class() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public GraphClass getType() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Schema getSchema() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	protected void setVCount(int count) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void setFirstVertex(VertexImpl firstVertex) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void setLastVertex(VertexImpl lastVertex) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void setVertexListVersion(long vertexListVersion) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void setECount(int count) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void setFirstEdge(EdgeImpl firstEdge) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void setLastEdge(EdgeImpl lastEdge) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	protected void setEdgeListVersion(long edgeListVersion) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public int getECount() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public long getEdgeListVersion() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public Edge getFirstEdge() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Edge getLastEdge() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Vertex getFirstVertex() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public Vertex getLastVertex() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public int getVCount() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public long getVertexListVersion() {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	protected void setICount(int count) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	
 
 }
