 package algorithms;
 
 import graphrep.GraphRep;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import com.carrotsearch.hppc.BitSet;
 import com.carrotsearch.hppc.DoubleArrayList;
 import com.carrotsearch.hppc.IntArrayDeque;
 import computecore.ComputeRequest;
 import computecore.ComputeResult;
 
 public class ShortestPathCH extends GraphAlgorithm {
 
 	private ComputeRequest req = null;
 	private ComputeResult res = null;
 
 	double srclat;
 	double srclon;
 	double destlat;
 	double destlon;
 
 	int srcid;
 	int destid;
 
 	private final Heap heap;
 
 	// Used to mark nodes with BFS
 	private final BitSet marked;
 	private final BitSet visited;
 
 	// TODO: Replace with int array based fifo
 	private final IntArrayDeque deque;
 
 	public ShortestPathCH(GraphRep graph) {
 		super(graph);
 		heap = new algorithms.Heap();
 		multipliedDist = new int[graph.getNodeCount()];
 		prevEdges = new int[graph.getNodeCount()];
 		marked = new BitSet(graph.getEdgeCount());
 		visited = new BitSet(graph.getNodeCount());
 		deque = new IntArrayDeque();
 	}
 
 	@Override
 	public void setRequest(ComputeRequest req) {
 		// reset dists
 		visited.clear();
 		marked.clear();
 
 		for (int i = 0; i < graph.getNodeCount(); i++) {
 			multipliedDist[i] = Integer.MAX_VALUE;
 		}
 		heap.resetHeap();
 
 		this.req = req;
 	}
 
 	@Override
 	public ComputeResult getResult() {
 		return res;
 	}
 
 	// dists in this array are stored with the multiplier applied. They also are
 	// rounded and are stored as integers
 	private final int[] multipliedDist;
 
 	/**
 	 * edge id
 	 */
 	private final int[] prevEdges;
 
 	// in meters
 	private double directDistance;
 
 	// maybe use http://code.google.com/p/simplelatlng/ instead
 	private final double calcDirectDistance(double lat1, double lng1,
 			double lat2, double lng2) {
 		double earthRadius = 6370.97327862;
 		double dLat = Math.toRadians(lat2 - lat1);
 		double dLng = Math.toRadians(lng2 - lng1);
 		double a = (Math.sin(dLat / 2) * Math.sin(dLat / 2))
 				+ (Math.cos(Math.toRadians(lat1))
 						* Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2) * Math
 							.sin(dLng / 2));
 		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
 		double dist = earthRadius * c;
 		return dist * 1000;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void run() {
 		assert (req != null) : "We ended up without a request object in run";
 
 		ArrayList<Map<String, Double>> points = null;
 		// TODO: send error messages to client
 		try {
 			points = (ArrayList<Map<String, Double>>) req.get("points");
 		} catch (ClassCastException e) {
 			System.err.println("The request's contents are invalid\n"
 					+ e.getMessage());
 			e.printStackTrace();
 			return;
 		} catch (NullPointerException e) {
 			System.err
 					.println("The request doesn't contain the information required for this algorithm\n"
 							+ e.getMessage());
 			e.printStackTrace();
 			return;
 		}
 
 		res = req.getResultObject();
 		srclat = points.get(0).get("lt");
 		srclon = points.get(0).get("ln");
 		destlat = points.get(1).get("lt");
 		destlon = points.get(1).get("ln");
		long nntime = System.nanoTime();
 		srcid = graph.getIDForCoordinates(srclat, srclon);
 		destid = graph.getIDForCoordinates(destlat, destlon);
		System.out.println("NNSearch took " + (System.nanoTime() - nntime)
				/ 1000000.0 + " ms");
 		directDistance = calcDirectDistance(srclat, srclon, destlat, destlon);
 
 		int nodeID = destid;
 		int sourceNode;
 		int edgeId;
 		/*
 		 * Start BFS on G_down beginning at destination and marking edges for
 		 * consideration
 		 * 
 		 * TODO: Create proper infrastructure for getting arrays with |nodes|
 		 * length
 		 */
 
 		System.out.println("Start BFS");
 		long starttime = System.nanoTime();
 		deque.clear();
 		deque.addLast(destid);
 		// visited[destid] = true;
 		visited.set(destid);
 		while (!deque.isEmpty()) {
 			nodeID = deque.removeLast();
 
 			for (int i = 0; i < graph.getInEdgeCount(nodeID); i++) {
 				edgeId = graph.getInEdgeID(nodeID, i);
 				sourceNode = graph.getSource(edgeId);
 
 				if (!visited.get(sourceNode)
 						&& graph.getRank(sourceNode) >= graph.getRank(nodeID)) {
 					// Mark the edge
 					marked.set(edgeId);
 					visited.set(sourceNode);
 					// Add source for exploration
 					deque.addFirst(sourceNode);
 				}
 
 			}
 		}
 
 		multipliedDist[srcid] = 0;
 		heap.insert(srcid, multipliedDist[srcid]);
 
 		int nodeDist;
 
 		int tempDist;
 		int targetNode;
 		long dijkstratime;
 		long backtracktime;
 
 		DIJKSTRA: while (!heap.isEmpty()) {
 			nodeID = heap.peekMinId();
 			nodeDist = heap.peekMinDist();
 			heap.removeMin();
 			if (nodeID == destid) {
 				break DIJKSTRA;
 			} else if (nodeDist > multipliedDist[nodeID]) {
 				continue;
 			}
 			for (int i = 0; i < graph.getOutEdgeCount(nodeID); i++) {
 				edgeId = graph.getOutEdgeID(nodeID, i);
 				targetNode = graph.getTarget(edgeId);
 				// Either marked (by BFS) or G_up edge
 				if (marked.get(edgeId)
 						|| graph.getRank(nodeID) <= graph.getRank(targetNode)) {
 					// without multiplier = shortest path
 					// tempDist = dist[nodeID] + graph.getOutDist(nodeID, i);
 
 					// with multiplier = fastest path
 					tempDist = multipliedDist[nodeID]
 							+ graph.getMultipliedDist(edgeId);
 
 					if (tempDist < multipliedDist[targetNode]) {
 						multipliedDist[targetNode] = tempDist;
 						prevEdges[targetNode] = graph.getOutEdgeID(nodeID, i);
 						heap.insert(targetNode, tempDist);
 					}
 
 				}
 			}
 		}
 		dijkstratime = System.nanoTime();
 
 		if (nodeID != destid) {
 			// TODO: send errmsg to client and do something useful
 			System.err.println("There is no path from src to dest");
 			return;
 		}
 
 		// backtracking and shortcut unpacking use dequeue as stack
 		deque.clear();
 		// Add the edges to our unpacking stack we
 		// go from destination to start so add at the end of the deque
 		int distance = 0;
 		int currNode = destid;
 		int outofSource, outofShortcuted, shortcutted, source;
 		DoubleArrayList lats = new DoubleArrayList();
 		DoubleArrayList lons = new DoubleArrayList();
 
 		do {
 			edgeId = prevEdges[currNode];
 			deque.addFirst(edgeId);
 			currNode = graph.getSource(edgeId);
 		} while (currNode != srcid);
 
 		// System.out.println("Start unpacking " + deque.size() + " edges");
 		// Unpack shortcuts "recursively"
 		while (!deque.isEmpty()) {
 			// Get the top edge and check if it's a shortcut that needs further
 			// unpacking
 			edgeId = deque.removeFirst();
 			shortcutted = graph.getShortedId(edgeId);
 			if (shortcutted != -1) {
 
 				// We have a shortcut unpack it
 				source = graph.getSource(edgeId);
 				outofShortcuted = graph.getOutEdgeID(shortcutted,
 						graph.getEdgeShortedNum(edgeId));
 				outofSource = graph.getOutEdgeID(source,
 						graph.getEdgeSourceNum(edgeId));
 				deque.addFirst(outofShortcuted);
 				deque.addFirst(outofSource);
 			} else {
 				// No shortcut remember it
 				distance += graph.getDist(edgeId);
 				currNode = graph.getSource(edgeId);
 				lats.add(graph.getNodeLat(currNode));
 				lons.add(graph.getNodeLon(currNode));
 			}
 		}
 		// Don't forget to add destination
 		lats.add(graph.getNodeLat(destid));
 		lons.add(graph.getNodeLon(destid));
 
 		backtracktime = System.nanoTime();
 		Map<String, Integer> misc = new HashMap<String, Integer>(2);
 		misc.put("distance", distance);
 
 		System.out.println("found sp with dist = " + (distance / 1000.0)
 				+ " km (direct distance: " + (directDistance / 1000.0)
 				+ " km; Distance with multiplier: "
 				+ (multipliedDist[destid] / 1000.0) + ")");
 		System.out.println("Dijkstra: "
 				+ ((dijkstratime - starttime) / 1000000.0)
 				+ " ms; Backtracking: "
 				+ ((backtracktime - dijkstratime) / 1000000.0) + " ms");
 
 		res.put("points", new Points(lats.toArray(), lons.toArray()));
 		res.put("misc", misc);
 	}
 }
