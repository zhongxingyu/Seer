 package de.uni_koblenz.jgralab.impl.disk;
 
 import java.lang.ref.Reference;
 import java.lang.ref.WeakReference;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import de.uni_koblenz.jgralab.Edge;
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.GraphElement;
 import de.uni_koblenz.jgralab.GraphFactory;
 import de.uni_koblenz.jgralab.GraphStructureChangedListener;
 import de.uni_koblenz.jgralab.Incidence;
 import de.uni_koblenz.jgralab.JGraLabServer;
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.impl.JGraLabServerImpl;
 import de.uni_koblenz.jgralab.schema.Schema;
 
 public abstract class GraphDatabaseElementaryMethods implements
 		RemoteGraphDatabaseAccessWithInternalMethods {
 
 	// the global subgraph id of the toplevel dhhtgraph
 	public static final long GLOBAL_GRAPH_ID = 0x0000000100000001l;
 	// the local subgraph id of the toplevel graph if a partial one
 	public static final int TOPLEVEL_LOCAL_SUBGRAPH_ID = 1;
 	// the partial graph id of the toplevel graph, the lowest bit of the
 	// high int is set
 	public static final int TOPLEVEL_PARTIAL_GRAPH_ID = 1;
 
 	public static long getToplevelGraphForPartialGraphId(int partialGraphId) {
 		long val = (((long) partialGraphId) << 32) + TOPLEVEL_LOCAL_SUBGRAPH_ID;
 		return val;
 	}
 
 	/**
 	 * Retrieves the partial graph id of the given globalSubgraphOrElementId,
 	 * this is the highbyte of the long value.
 	 */
 	public static final int getPartialGraphId(long globalSubgraphOrElementId) {
 		return (int) (globalSubgraphOrElementId >>> 32);
 	}
 
 	/**
 	 * Retrieves the local id of the given globalSubgraphOrElementId, this is
 	 * the lowbyte of the long value.
 	 */
 	public final static int convertToLocalId(long globalSubgraphOrElementId) {
 		return (int) globalSubgraphOrElementId;
 	}
 
 	protected class GraphData {
 		long globalSubgraphId;
 		long firstVertexId;
 		long lastVertexId;
 		long firstEdgeId;
 		long lastEdgeId;
 		long edgeCount;
 		long vertexCount;
 		long incidenceCount;
 		long containingElementId;
 		int typeId;
 		List<Integer> partialGraphs;
 		public ParentEntityKind parentEntityKind;
 	}
 
 	/**
 	 * Data of local subordinate graphs such as first and last elements, number
 	 * of elements and so on
 	 */
 	protected final ArrayList<GraphData> localSubgraphData;
 
 	public final long convertToGlobalId(int localSubgraphOrElementId) {
 		long l = ((long) localPartialGraphId) << 32;
 		return l + localSubgraphOrElementId;
 	}
 
 	/**
 	 * The graph schema of the graph whose local subgraphs are managed by this
 	 * GraphDatabase belongs to
 	 */
 	protected final Schema schema;
 
 	/**
 	 * The unique id of the graph whose subgraphs are stored in this database
 	 */
 	protected final String uniqueGraphId;
 
 	/**
 	 * The id of the local toplevel graph
 	 */
 	protected final int localPartialGraphId;
 
 	/**
 	 * The id of the complete or subgraph the local partial graph directly
 	 * belongs to
 	 */
 	protected final long parentSubgraphId;
 
 	/**
 	 * The local JGraLab server instance
 	 */
 	protected final JGraLabServer localJGraLabServer;
 
 	/**
 	 * The disk storage storing local graph elements
 	 */
 	protected final DiskStorageManager localDiskStorage;
 
 	/**
 	 * Stub for the disk storage
 	 */
 	protected final RemoteDiskStorageAccess diskStorageStub;
 
 	/**
 	 * The GraphFactory to create graphs and graph elements and proxies
 	 */
 	protected GraphFactory graphFactory;
 
 	/**
 	 * true iff the local graph is currently loading
 	 */
 	protected boolean loading = false;
 
 	/**
 	 * List of vertices to be deleted by a cascading delete caused by deletion
 	 * of a composition "parent".
 	 */
 	protected final List<Long> deleteVertexList;
 
 	/**
 	 * free index list for vertices
 	 */
 	protected final FreeIndexList freeVertexList;
 
 	/**
 	 * free index list for vertices
 	 */
 	protected final FreeIndexList freeEdgeList;
 
 	/**
 	 * free index list for vertices
 	 */
 	protected final FreeIndexList freeIncidenceList;
 
 	/**
 	 * the local graph version
 	 */
 	protected long graphVersion = 0;
 
 	/**
 	 * Holds the version of the vertex sequence. For every modification (e.g.
 	 * adding/deleting a vertex or changing the vertex sequence) this version
 	 * number is increased by 1. It is set to 0 when the graph is loaded.
 	 */
 	protected long vertexListVersion;
 
 	/**
 	 * Holds the version of the edge sequence. For every modification (e.g.
 	 * adding/deleting an edge or changing the edge sequence) this version
 	 * number is increased by 1. It is set to 0 when the graph is loaded.
 	 */
 	protected long edgeListVersion;
 
 	/**
 	 * Remote GraphDatabases of the distributed graph
 	 */
 	protected Map<Integer, RemoteGraphDatabaseAccessWithInternalMethods> partialGraphDatabases;
 
 	protected final Map<Integer, RemoteDiskStorageAccess> remoteDiskStorages;
 
 	/**
 	 * The map of global subgraph ids to the local representation objects. Those
 	 * may be either objects representing a local subgraph or (proxy) objects
 	 * representing remote subgraphs
 	 */
 	protected final Map<Long, Reference<Graph>> subgraphObjects;
 
 	/**
 	 * Maps the global element id to the local proxy object representing the
 	 * remote vertex
 	 */
 	protected final Map<Long, Reference<Vertex>> remoteVertices;
 	protected final Map<Long, Reference<Edge>> remoteEdges;
 	protected final Map<Long, Reference<Incidence>> remoteIncidences;
 
 	/*
 	 * ==========================================================================
 	 * ======== Graph listeners
 	 * ==================================================
 	 * ================================
 	 */
 
 	/**
 	 * Listeners listining on changes of this graph
 	 */
 	protected final List<GraphStructureChangedListener> graphStructureChangedListeners = new ArrayList<GraphStructureChangedListener>();
 
 	protected List<GraphStructureChangedListener> getGraphStructureChangeListeners() {
 		return graphStructureChangedListeners;
 	}
 
 	public void addGraphStructureChangeListener(
 			GraphStructureChangedListener newListener) {
 		if (!graphStructureChangedListeners.contains(newListener)) {
 			graphStructureChangedListeners.add(newListener);
 		}
 	}
 
 	public void removeGraphStructureChangeListener(
 			GraphStructureChangedListener newListener) {
 		graphStructureChangedListeners.remove(newListener);
 	}
 
 	/*
 	 * ==========================================================================
 	 * ======== Constructur and basic methods to access subgraphs
 	 * ================
 	 * ==================================================================
 	 */
 
 	public GraphDatabaseElementaryMethods(Schema schema, String uniqueGraphId,
 			long parentSubgraphId, int localPartialGraphId) {
 		this.uniqueGraphId = uniqueGraphId;
 		this.schema = schema;
 		this.graphFactory = schema.getGraphFactory();
 		this.parentSubgraphId = parentSubgraphId;
 		this.localPartialGraphId = localPartialGraphId;
 		try {
 			this.localDiskStorage = new DiskStorageManager(
 					(GraphDatabaseBaseImpl) this);
 			this.diskStorageStub = (RemoteDiskStorageAccess) UnicastRemoteObject
 					.exportObject(localDiskStorage, 0);
 		} catch (Exception e) {
 			throw new RuntimeException(e);
 		}
 		partialGraphDatabases = new HashMap<Integer, RemoteGraphDatabaseAccessWithInternalMethods>();
 		remoteDiskStorages = new HashMap<Integer, RemoteDiskStorageAccess>();
 		remoteDiskStorages.put(localPartialGraphId, localDiskStorage);
 		this.freeVertexList = new FreeIndexList(Integer.MAX_VALUE);
 		this.freeEdgeList = new FreeIndexList(Integer.MAX_VALUE);
 		this.freeIncidenceList = new FreeIndexList(Integer.MAX_VALUE);
 		this.deleteVertexList = new LinkedList<Long>();
 
 		localSubgraphData = new ArrayList<GraphData>();
 		subgraphObjects = new HashMap<Long, Reference<Graph>>();
 		remoteVertices = new HashMap<Long, Reference<Vertex>>();
 		remoteEdges = new HashMap<Long, Reference<Edge>>();
 		remoteIncidences = new HashMap<Long, Reference<Incidence>>();
 		// initialize fields
 		graphVersion = -1;
 		try {
 			setGraphVersion(0);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 
 		// register graph database at server
 		localJGraLabServer = JGraLabServerImpl.getLocalInstance();
 		localJGraLabServer
 				.registerLocalGraphDatabase((GraphDatabaseBaseImpl) this);
 		partialGraphDatabases.put(localPartialGraphId, this);
 	}
 
 	/**
 	 * @param localSubgraphId
 	 * @return the graph data object storing the data of the local subgraph
 	 *         identified by <code>localSubgraphId</code>
 	 */
 	protected GraphData getGraphData(int localSubgraphId) {
 		GraphData data = localSubgraphData.get(localSubgraphId - 1);
 		return data;
 	}
 
 	/**
 	 * Retrieves the hostname that stores all subgraphs with the given partial
 	 * graph id
 	 * 
 	 * @param id
 	 * @return the hostname of the station containing the partial graph with the
 	 *         given id
 	 */
 	public abstract String getHostname(int partialGraphId);
 
 	@Override
 	public String getUniqueGraphId() {
 		return uniqueGraphId;
 	}
 
 	public int getLocalPartialGraphId() {
 		return localPartialGraphId;
 	}
 
 	@Override
 	public long getIdOfParentDistributedGraph() {
 		return parentSubgraphId;
 	}
 
 	public GraphFactory getGraphFactory() {
 		return graphFactory;
 	}
 
 	public long getGraphVersion() {
 		return graphVersion;
 	}
 
 	@Override
 	public boolean isLoading() {
 		return loading;
 
 	}
 
 	@Override
 	public void setLoading(boolean isLoading) {
 		this.loading = isLoading;
 	}
 
 	protected boolean canAddGraphElement(long eId) {
 		try {
 			return !(eId < 0 ? containsEdgeId(eId) : containsVertexId(eId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	protected RemoteGraphDatabaseAccessWithInternalMethods getGraphDatabase(
 			int partialGraphId) {
 		RemoteGraphDatabaseAccessWithInternalMethods remoteAccess = partialGraphDatabases
 				.get(partialGraphId);
 		if (remoteAccess == null) {
 			try {
 				remoteAccess = (RemoteGraphDatabaseAccessWithInternalMethods) localJGraLabServer
 						.getRemoteInstance(getHostname(partialGraphId))
 						.getGraphDatabase(uniqueGraphId);
 			} catch (RemoteException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			partialGraphDatabases
 					.put(partialGraphId,
 							(RemoteGraphDatabaseAccessWithInternalMethods) remoteAccess);
 		}
 		return remoteAccess;
 	}
 
 	protected RemoteDiskStorageAccess getDiskStorageForPartialGraph(
 			int partialGraphId) {
 		RemoteDiskStorageAccess remoteAccess = remoteDiskStorages
 				.get(partialGraphId);
 		if (remoteAccess == null) {
 			try {
 				remoteAccess = getGraphDatabase(partialGraphId)
 						.getLocalDiskStorage();
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 			remoteDiskStorages.put(partialGraphId, remoteAccess);
 		}
 		return remoteAccess;
 	}
 
 	public DiskStorageManager getLocalDiskStorage() {
 		return localDiskStorage;
 	}
 
 	public RemoteDiskStorageAccess getDiskStorage() {
 		return diskStorageStub;
 	}
 
 	public Schema getSchema() {
 		return schema;
 	}
 
 	/*
 	 * ==========================================================================
 	 * ======== Methods to access types and type ids
 	 * ============================
 	 * ======================================================
 	 */
 
 	/**
 	 * Returns the type of the graph identified by the global subgraph id
 	 * 
 	 * @param globalSubgraphId
 	 * @return the type of the
 	 */
 	@SuppressWarnings("unchecked")
 	protected Class<? extends Graph> getGraphType(long globalSubgraphId) {
 		return (Class<? extends Graph>) schema
 				.getM1ClassForId(getGraphTypeId(globalSubgraphId));
 	}
 
 	/**
 	 * Returns the type of the edge identified by its global id.
 	 * 
 	 * @param elementId
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	protected Class<? extends Edge> getEdgeType(long elementId) {
 		return (Class<? extends Edge>) schema
 				.getM1ClassForId(getEdgeTypeId(elementId));
 	}
 
 	/**
 	 * Returns the type of the edge identified by its global id.
 	 * 
 	 * @param elementId
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	protected Class<? extends Vertex> getVertexType(long elementId) {
 		return (Class<? extends Vertex>) schema
 				.getM1ClassForId(getVertexTypeId(elementId));
 	}
 
 	/**
 	 * Returns the type of the edge identified by its global id.
 	 * 
 	 * @param elementId
 	 * @return
 	 */
 	@SuppressWarnings("unchecked")
 	protected Class<? extends Incidence> getIncidenceType(long elementId) {
 		return (Class<? extends Incidence>) schema
 				.getM1ClassForId(getIncidenceTypeId(elementId));
 	}
 
 	public int getGraphTypeId(long globalSubgraphId) {
 		int partialGraphId = getPartialGraphId(globalSubgraphId);
 		if (partialGraphId != localPartialGraphId) {
 			try {
 				return getGraphDatabase(partialGraphId).getGraphTypeId(
 						globalSubgraphId);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		}
 		int localSubgraphId = convertToLocalId(globalSubgraphId);
 		return getGraphData(localSubgraphId).typeId;
 	}
 
 	public int getVertexTypeId(long vertexId) {
 		int partialGraphId = getPartialGraphId(vertexId);
 		try {
 			return getDiskStorageForPartialGraph(partialGraphId)
 					.getVertexTypeId(convertToLocalId(vertexId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public int getEdgeTypeId(long edgeId) {
 		int partialGraphId = getPartialGraphId(edgeId);
 		try {
 			return getDiskStorageForPartialGraph(partialGraphId).getEdgeTypeId(
 					convertToLocalId(edgeId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public int getIncidenceTypeId(long incidenceId) {
 		int partialGraphId = getPartialGraphId(incidenceId);
 		try {
 			return getDiskStorageForPartialGraph(partialGraphId)
 					.getIncidenceTypeId(convertToLocalId(incidenceId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/*
 	 * ==========================================================================
 	 * ======== Methods to access local and proxy objects for graphs and their
 	 * elements
 	 * ==================================================================
 	 * ================
 	 */
 
 	/**
 	 * Retrieves a Graph object (local or proxy) that represents the (partial)
 	 * (sub) graph identified by the given id
 	 * 
 	 * @param partialGraphId
 	 * @return
 	 */
 	public Graph getGraphObject(long globalSubgraphId) {
 		Reference<Graph> ref = subgraphObjects.get(globalSubgraphId);
 		Graph g = null;
 		if (ref != null) {
 			g = ref.get();
 		}
 		System.out.println("Creating graph object");
 		if (g == null) {
 			int partialGraphId = getPartialGraphId(globalSubgraphId);
 			System.out.println("Partial Graph id: " + partialGraphId);
 			RemoteGraphDatabaseAccess storingDb = getGraphDatabase(partialGraphId);
 			System.out.println("Storign graph database: " + storingDb);
 			g = graphFactory.createGraphDiskBasedStorage(
 					getGraphType(globalSubgraphId), uniqueGraphId,
 					globalSubgraphId, (GraphDatabaseBaseImpl) this, storingDb);
 			subgraphObjects.put(globalSubgraphId, new WeakReference<Graph>(g));
 		}
 		return g;
 	}
 
 	/**
 	 * Returns an object (Vertex or Edge) representing the GraphElement
 	 * identified by the given global id
 	 * 
 	 * @param elemId
 	 * @return
 	 */
 	public GraphElement<?, ?, ?> getGraphElementObject(long elemId) {
 		if (elemId < 0) {
 			return getEdgeObject(-elemId);
 		} else {
 			return getVertexObject(elemId);
 		}
 	}
 
 	/**
 	 * @return an object realizing the vertex with the given global id. The
 	 *         object may be either a local one or a proxy for a remote one
 	 */
 	public Vertex getVertexObject(long id) {
 		if (id == 0)
 			return null;
 		int partialGraphId = getPartialGraphId(id);
 		if (partialGraphId == localPartialGraphId) {
 			return localDiskStorage.getVertexObject(convertToLocalId(id));
 		}
 		Reference<Vertex> ref = remoteVertices.get(id);
 		Vertex proxy = null;
 		if (ref != null) {
 			proxy = ref.get();
 		}
 		if (proxy == null) {
 			// create new vertex proxy
 			RemoteGraphDatabaseAccess remoteDatabase = getGraphDatabase(partialGraphId);
 			Class<? extends Vertex> vc = getVertexType(id);
 			proxy = graphFactory.createVertexProxy(vc, id,
 					(GraphDatabaseBaseImpl) this, remoteDatabase);
 			ref = new WeakReference<Vertex>(proxy);
 			remoteVertices.put(id, ref);
 		}
 		return proxy;
 	}
 
 	/**
 	 * @return an object realizing the vertex with the given global id. The
 	 *         object may be either a local one or a proxy for a remote one
 	 */
 	public Edge getEdgeObject(long id) {
 		if (id == 0) {
 			return null;
 		}
 		int partialGraphId = getPartialGraphId(id);
 		if (partialGraphId == localPartialGraphId) {
 			return localDiskStorage.getEdgeObject(convertToLocalId(id));
 		}
 		Reference<Edge> ref = remoteEdges.get(id);
 		Edge proxy = null;
 		if (ref != null) {
 			proxy = ref.get();
 		}
 		if (proxy == null) {
 			// create new vertex proxy
 			RemoteGraphDatabaseAccess remoteDatabase = getGraphDatabase(partialGraphId);
 			Class<? extends Edge> ec = getEdgeType(id);
 			proxy = graphFactory.createEdgeProxy(ec, id,
 					(GraphDatabaseBaseImpl) this, remoteDatabase);
 			ref = new WeakReference<Edge>(proxy);
 			remoteEdges.put(id, ref);
 		}
 		return proxy;
 	}
 
 	/**
 	 * @return an object realizing the vertex with the given global id. The
 	 *         object may be either a local one or a proxy for a remote one
 	 */
 	public Incidence getIncidenceObject(long id) {
 		if (id == 0)
 			return null;
 		int partialGraphId = getPartialGraphId(id);
 		if (partialGraphId == localPartialGraphId) {
 			return localDiskStorage.getIncidenceObject(convertToLocalId(id));
 		}
 		Reference<Incidence> ref = remoteIncidences.get(id);
 		Incidence proxy = null;
 		if (ref != null) {
 			proxy = ref.get();
 		}
 		if (proxy == null) {
 			// create new vertex proxy
 			RemoteGraphDatabaseAccess remoteDatabase = getGraphDatabase(partialGraphId);
 			Class<? extends Incidence> vc = getIncidenceType(id);
 			proxy = graphFactory.createIncidenceProxy(vc, id,
 					(GraphDatabaseBaseImpl) this, remoteDatabase);
 			ref = new WeakReference<Incidence>(proxy);
 			remoteIncidences.put(id, ref);
 		}
 		return proxy;
 	}
 
 	/*
 	 * ===================================================== Methods to access
 	 * hierarchy =====================================================
 	 */
 
 	@Override
 	public long getSigmaIdOfVertexId(long globalElementId) {
 		int partialGraphId = getPartialGraphId(globalElementId);
 		int localElementId = convertToLocalId(globalElementId);
 		try {
 			return getDiskStorageForPartialGraph(partialGraphId)
 					.getSigmaIdOfVertexId(localElementId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setSigmaIdOfVertexId(long globalElementId, long globalSigmaId) {
 		int partialGraphId = getPartialGraphId(globalElementId);
 		int localElementId = convertToLocalId(globalElementId);
 		try {
 			getDiskStorageForPartialGraph(partialGraphId).setSigmaIdOfVertexId(
 					localElementId, globalSigmaId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public long getSigmaIdOfEdgeId(long globalElementId) {
 		int partialGraphId = getPartialGraphId(globalElementId);
 		int localElementId = convertToLocalId(globalElementId);
 		try {
 			return getDiskStorageForPartialGraph(partialGraphId)
 					.getSigmaIdOfEdgeId(localElementId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setSigmaIdOfEdgeId(long globalElementId, long globalSigmaId) {
 		int partialGraphId = getPartialGraphId(globalElementId);
 		int localElementId = convertToLocalId(globalElementId);
 		try {
 			getDiskStorageForPartialGraph(partialGraphId).setSigmaIdOfEdgeId(
 					localElementId, globalSigmaId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public int getKappaOfVertexId(long globalElementId) {
 		int partialGraphId = getPartialGraphId(globalElementId);
 		int localElementId = convertToLocalId(globalElementId);
 		try {
			return getGraphDatabase(partialGraphId).getKappaOfVertexId(
					localElementId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setKappaOfVertexId(long globalElementId, int kappa) {
 		int partialGraphId = getPartialGraphId(globalElementId);
 		int localElementId = convertToLocalId(globalElementId);
 		try {
			getGraphDatabase(partialGraphId).setKappaOfVertexId(localElementId,
					kappa);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public int getKappaOfEdgeId(long globalElementId) {
 		int partialGraphId = getPartialGraphId(globalElementId);
 		int localElementId = convertToLocalId(globalElementId);
 		try {
			return getGraphDatabase(partialGraphId).getKappaOfEdgeId(
					localElementId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setKappaOfEdgeId(long globalElementId, int kappa) {
 		int partialGraphId = getPartialGraphId(globalElementId);
 		int localElementId = convertToLocalId(globalElementId);
 		try {
			getGraphDatabase(partialGraphId).setKappaOfEdgeId(localElementId,
					kappa);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/*
 	 * ===================================================== Methods to access
 	 * vertex sequence =====================================================
 	 */
 
 	public long getVCount(long subgraphId) {
 		int partialGraphId = getPartialGraphId(subgraphId);
 		if (partialGraphId != localPartialGraphId) {
 			RemoteGraphDatabaseAccess remoteDb = getGraphDatabase(partialGraphId);
 			try {
 				return remoteDb.getVCount(subgraphId);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			return getGraphData(convertToLocalId(subgraphId)).vertexCount;
 		}
 	}
 
 	public long getMaxVCount() {
 		return Integer.MAX_VALUE;
 	}
 
 	@Override
 	public long getFirstVertexId(long globalSubgraphId) {
 		int partialGraphId = getPartialGraphId(globalSubgraphId);
 		if (partialGraphId != localPartialGraphId) {
 			RemoteGraphDatabaseAccess remoteDb = getGraphDatabase(partialGraphId);
 			try {
 				return remoteDb.getFirstVertexId(globalSubgraphId);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			return getGraphData(convertToLocalId(globalSubgraphId)).firstVertexId;
 		}
 	}
 
 	@Override
 	public void setFirstVertexId(long globalSubgraphId, long edgeId) {
 		int partialGraphId = getPartialGraphId(globalSubgraphId);
 		if (partialGraphId != localPartialGraphId) {
 			RemoteGraphDatabaseAccess remoteDb = getGraphDatabase(partialGraphId);
 			try {
 				remoteDb.setFirstVertexId(globalSubgraphId, edgeId);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			getGraphData(convertToLocalId(globalSubgraphId)).firstVertexId = edgeId;
 		}
 	}
 
 	@Override
 	public long getLastVertexId(long globalSubgraphId) {
 		int partialGraphId = getPartialGraphId(globalSubgraphId);
 		if (partialGraphId != localPartialGraphId) {
 			RemoteGraphDatabaseAccess remoteDb = getGraphDatabase(partialGraphId);
 			try {
 				return remoteDb.getLastVertexId(globalSubgraphId);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			return getGraphData(convertToLocalId(globalSubgraphId)).lastVertexId;
 		}
 	}
 
 	public void setLastVertexId(long globalSubgraphId, long edgeId) {
 		int partialGraphId = getPartialGraphId(globalSubgraphId);
 		if (partialGraphId != localPartialGraphId) {
 			RemoteGraphDatabaseAccess remoteDb = getGraphDatabase(partialGraphId);
 			try {
 				remoteDb.setLastVertexId(globalSubgraphId, edgeId);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			getGraphData(convertToLocalId(globalSubgraphId)).lastVertexId = edgeId;
 		}
 	}
 
 	public long getNextVertexId(long vertexId) {
 		int partialGraphId = getPartialGraphId(vertexId);
 		RemoteDiskStorageAccess diskStore = getDiskStorageForPartialGraph(partialGraphId);
 		try {
 			return diskStore.getNextVertexId(convertToLocalId(vertexId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	public long getPreviousVertexId(long vertexId) {
 		int partialGraphId = getPartialGraphId(vertexId);
 		RemoteDiskStorageAccess diskStore = getDiskStorageForPartialGraph(partialGraphId);
 		try {
 			return diskStore.getPreviousVertexId(convertToLocalId(vertexId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	protected void setNextVertexId(long modifiedVertexId, long nextVertexId) {
 		int partialGraphId = getPartialGraphId(modifiedVertexId);
 		RemoteDiskStorageAccess diskStore = getDiskStorageForPartialGraph(partialGraphId);
 		try {
 			diskStore.setNextVertexId(convertToLocalId(modifiedVertexId),
 					nextVertexId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	protected void setPreviousVertexId(long modifiedVertexId, long nextVertexId) {
 		int partialGraphId = getPartialGraphId(modifiedVertexId);
 		RemoteDiskStorageAccess diskStore = getDiskStorageForPartialGraph(partialGraphId);
 		try {
 			diskStore.setPreviousVertexId(convertToLocalId(modifiedVertexId),
 					nextVertexId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	/*
 	 * ===================================================== Methods to access
 	 * edge sequence =====================================================
 	 */
 
 	@Override
 	public long getFirstEdgeId(long globalSubgraphId) {
 		int partialGraphId = getPartialGraphId(globalSubgraphId);
 		if (partialGraphId != localPartialGraphId) {
 			RemoteGraphDatabaseAccess remoteDb = getGraphDatabase(partialGraphId);
 			try {
 				return remoteDb.getFirstEdgeId(globalSubgraphId);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			return getGraphData(convertToLocalId(globalSubgraphId)).firstEdgeId;
 		}
 	}
 
 	@Override
 	public void setFirstEdgeId(long globalSubgraphId, long edgeId) {
 		assert getPartialGraphId(globalSubgraphId) == getPartialGraphId(edgeId);
 		int partialGraphId = getPartialGraphId(globalSubgraphId);
 		if (partialGraphId != localPartialGraphId) {
 			RemoteGraphDatabaseAccess remoteDb = getGraphDatabase(partialGraphId);
 			try {
 				remoteDb.setFirstEdgeId(globalSubgraphId, edgeId);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			getGraphData(convertToLocalId(globalSubgraphId)).firstEdgeId = edgeId;
 		}
 	}
 
 	@Override
 	public long getLastEdgeId(long globalSubgraphId) {
 		int partialGraphId = getPartialGraphId(globalSubgraphId);
 		if (partialGraphId != localPartialGraphId) {
 			RemoteGraphDatabaseAccess remoteDb = getGraphDatabase(partialGraphId);
 			try {
 				return remoteDb.getLastEdgeId(globalSubgraphId);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			return getGraphData(convertToLocalId(globalSubgraphId)).lastEdgeId;
 		}
 	}
 
 	@Override
 	public void setLastEdgeId(long globalSubgraphId, long edgeId) {
 		assert getPartialGraphId(globalSubgraphId) == getPartialGraphId(edgeId);
 		int partialGraphId = getPartialGraphId(globalSubgraphId);
 		if (partialGraphId != localPartialGraphId) {
 			RemoteGraphDatabaseAccess remoteDb = getGraphDatabase(partialGraphId);
 			try {
 				remoteDb.setLastEdgeId(globalSubgraphId, edgeId);
 			} catch (RemoteException e) {
 				throw new RuntimeException(e);
 			}
 		} else {
 			getGraphData(convertToLocalId(globalSubgraphId)).lastEdgeId = edgeId;
 		}
 	}
 
 	@Override
 	public long getNextEdgeId(long edgeId) {
 		int partialGraphId = getPartialGraphId(edgeId);
 		RemoteDiskStorageAccess diskStore = getDiskStorageForPartialGraph(partialGraphId);
 		try {
 			return diskStore.getNextEdgeId(convertToLocalId(edgeId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	protected void setNextEdgeId(long modifiedEdgeId, long nextEdgeId) {
 		int partialGraphId = getPartialGraphId(modifiedEdgeId);
 		RemoteDiskStorageAccess diskStore = getDiskStorageForPartialGraph(partialGraphId);
 		try {
 			diskStore.setNextEdgeId(convertToLocalId(modifiedEdgeId),
 					nextEdgeId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public long getPreviousEdgeId(long edgeId) {
 		int partialGraphId = getPartialGraphId(edgeId);
 		RemoteDiskStorageAccess diskStore = getDiskStorageForPartialGraph(partialGraphId);
 		try {
 			return diskStore.getPreviousEdgeId(convertToLocalId(edgeId));
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	protected void setPreviousEdgeId(long modifiedEdgeId, long nextEdgeId) {
 		int partialGraphId = getPartialGraphId(modifiedEdgeId);
 		RemoteDiskStorageAccess diskStore = getDiskStorageForPartialGraph(partialGraphId);
 		try {
 			diskStore.setPreviousEdgeId(convertToLocalId(modifiedEdgeId),
 					nextEdgeId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	protected boolean containsIncidenceId(long iId) {
 		return getIncidenceObject(iId) != null;
 	}
 
 	@Override
 	public void setFirstIncidenceIdAtVertexId(long vertexId, long incidenceId) {
 		int partialGraphId = getPartialGraphId(vertexId);
 		try {
 			getDiskStorageForPartialGraph(partialGraphId)
 					.setFirstIncidenceIdAtVertexId(convertToLocalId(vertexId),
 							incidenceId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setLastIncidenceIdAtVertexId(long vertexId, long incidenceId) {
 		int partialGraphId = getPartialGraphId(vertexId);
 		try {
 			getDiskStorageForPartialGraph(partialGraphId)
 					.setLastIncidenceIdAtVertexId(convertToLocalId(vertexId),
 							incidenceId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setNextIncidenceIdAtVertexId(long globalIncidenceId,
 			long nextIncidenceId) {
 		int partialGraphId = getPartialGraphId(globalIncidenceId);
 		try {
 			getDiskStorageForPartialGraph(partialGraphId)
 					.setNextIncidenceAtVertexId(
 							convertToLocalId(globalIncidenceId),
 							nextIncidenceId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setPreviousIncidenceIdAtVertexId(long globalIncidenceId,
 			long nextIncidenceId) {
 		int partialGraphId = getPartialGraphId(globalIncidenceId);
 		try {
 			getDiskStorageForPartialGraph(partialGraphId)
 					.setPreviousIncidenceAtVertexId(
 							convertToLocalId(globalIncidenceId),
 							nextIncidenceId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 
 	}
 
 	@Override
 	public void setFirstIncidenceIdAtEdgeId(long edgeId, long incidenceId) {
 		int partialGraphId = getPartialGraphId(edgeId);
 		try {
 			getDiskStorageForPartialGraph(partialGraphId)
 					.setFirstIncidenceIdAtEdgeId(convertToLocalId(edgeId),
 							incidenceId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setLastIncidenceIdAtEdgeId(long edgeId, long incidenceId) {
 		int partialGraphId = getPartialGraphId(edgeId);
 		try {
 			getDiskStorageForPartialGraph(partialGraphId)
 					.setLastIncidenceIdAtEdgeId(convertToLocalId(edgeId),
 							incidenceId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setNextIncidenceIdAtEdgeId(long globalIncidenceId,
 			long nextIncidenceId) {
 		int partialGraphId = getPartialGraphId(globalIncidenceId);
 		try {
 			getDiskStorageForPartialGraph(partialGraphId)
 					.setNextIncidenceAtEdgeId(
 							convertToLocalId(globalIncidenceId),
 							nextIncidenceId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 	@Override
 	public void setPreviousIncidenceIdAtEdgeId(long globalIncidenceId,
 			long nextIncidenceId) {
 		int partialGraphId = getPartialGraphId(globalIncidenceId);
 		try {
 			getDiskStorageForPartialGraph(partialGraphId)
 					.setPreviousIncidenceAtEdgeId(
 							convertToLocalId(globalIncidenceId),
 							nextIncidenceId);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 	}
 
 }
