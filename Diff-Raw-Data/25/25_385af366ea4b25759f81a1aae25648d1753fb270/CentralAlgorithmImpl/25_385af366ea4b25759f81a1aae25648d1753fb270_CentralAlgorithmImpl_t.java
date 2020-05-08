 package de.uni_koblenz.jgralab.algolib;
 
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import de.uni_koblenz.jgralab.Graph;
 import de.uni_koblenz.jgralab.Vertex;
 import de.uni_koblenz.jgralab.impl.disk.GraphDatabaseElementaryMethods;
 
 /**
  * Implementation of central part of the search algorithm with all
  * datastructures needed to control the algorithm such as the buffer and the
  * marking of vertices and edges
  */
 public abstract class CentralAlgorithmImpl implements CentralAlgorithm {
 
 	// each element is identified by a combination of the partial graph Id it
 	// belongs to and its element Id local to the partial graph
 	protected Map<Long, Long> number = new HashMap<Long, Long>();
 	protected List<Long> order = new ArrayList<Long>();
 	protected Map<Long, Long> parentVertexInc = new HashMap<Long, Long>();
 	protected Map<Long, Long> parentEdgeInc = new HashMap<Long, Long>();
 	protected long num = 0;
 	protected Buffer<Long> buffer;
 	protected Graph graph;
 	protected CentralAlgorithm stub;
 
 	/* maps partial graph Ids to the remote algorithms stored as local proxies */
 	private Map<Integer, SatelliteAlgorithmRemoteAccess> remoteAlgorithms = new HashMap<Integer, SatelliteAlgorithmRemoteAccess>();
 
 	/* returns the SatelliteAlgorithm for the graph the element belongs to */
 	private SatelliteAlgorithmRemoteAccess getAlgorithmForElementId(
 			Long elementId) {
 		return remoteAlgorithms.get(GraphDatabaseElementaryMethods
 				.getPartialGraphId(elementId));
 	}
 
 	/** creates a local algorithm object as central instance of a search */
 	public CentralAlgorithmImpl(Graph partialGraph) {
 		this.graph = partialGraph;
 		// create satellite algorithms for all partial graphs incl. the local
 		// one
 		try {
 			this.stub = (CentralAlgorithm) UnicastRemoteObject.exportObject(
 					this, 0);
 		} catch (RemoteException e) {
 			throw new RuntimeException(e);
 		}
 		for (Graph pg : partialGraph.getCompleteGraph().getPartialGraphs()) {
 			remoteAlgorithms.put(pg.getPartialGraphId(),
					SatelliteAlgorithmImpl.createRemote(pg, stub));
 		}
 	}
 
 	public void run(Vertex startVertex) throws RemoteException {
 		Long vertexId = startVertex.getGlobalId();
 		SatelliteAlgorithmRemoteAccess remoteAlgorithm = getAlgorithmForElementId(vertexId);
 		number.put(vertexId, ++num); // number first vertex with 1
 		order.add(vertexId);
 		handleRoot(vertexId);
 		handleVertex(vertexId);
 		buffer.add(vertexId);
 		remoteAlgorithm.processRoot(vertexId);
 		while (!buffer.isEmpty()) {
			long vId = buffer.get();
			System.out.println("Processing vertex " + vId);
			getAlgorithmForElementId(vertexId)
					.processVertex(vId /* buffer.get() */);
 		}
 	}
 
 	public boolean testAndProcessEdge(Long edgeId, Long incId)
 			throws RemoteException {
 		if (!parentEdgeInc.containsKey(edgeId)) {
 			parentEdgeInc.put(edgeId, incId);
 			handleEdge(edgeId);
 			handleTreeIncidence(incId);
 			return true;
 		}
 		handleCrossIncidence(incId);
 		return false;
 	}
 
 	public boolean testAndProcessVertex(Long vertexId, Long incId)
 			throws RemoteException {
 		if (!number.containsKey(vertexId)) {
 			number.put(vertexId, ++num);
 			order.add(vertexId);
 			parentVertexInc.put(vertexId, incId);
 			buffer.add(vertexId);
 			handleVertex(vertexId);
 			handleTreeIncidence(incId);
 			return true;
 		}
 		handleCrossIncidence(incId);
 		return false;
 	}
 
 	public int getPartialGraphId() {
 		return graph.getPartialGraphId();
 	}
 }
