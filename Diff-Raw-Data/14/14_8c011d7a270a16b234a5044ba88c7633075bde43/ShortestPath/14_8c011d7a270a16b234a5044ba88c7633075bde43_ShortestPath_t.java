 package algorithms;
 
 import graphrep.GraphRep;
 import graphrep.Heap;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.json.simple.JSONAware;
 
 import computecore.ComputeRequest;
 import computecore.ComputeResult;
 
 public class ShortestPath extends GraphAlgorithm {
 
 	private class Points implements JSONAware {
 		private final double[] lats;
 		private final double[] lons;
 		StringBuilder sb;
 
 		public Points(double[] lats, double[] lons) {
 			this.lats = lats;
 			this.lons = lons;
 			sb = new StringBuilder();
 		}
 
 		@Override
 		public String toJSONString() {
 			sb.append("[");
 			for (int i = 0; i < (lats.length - 1); i++) {
 				sb.append("{\"lt\":" + lats[i] + ",\"ln\":" + lons[i] + "},");
 			}
 			sb.append("{\"lt\":" + lats[lats.length - 1] + ",\"ln\":"
 					+ lons[lats.length - 1] + "}");
 			sb.append("]");
 			return sb.toString();
 		}
 	}
 
 	private ComputeRequest req = null;
 	private ComputeResult res = null;
 
 	double srclat;
 	double srclon;
 	double destlat;
 	double destlon;
 
 	int srcid;
 	int destid;
 
 	private final Heap heap;
 
 	public ShortestPath(GraphRep graph) {
 		super(graph);
 		heap = new graphrep.Heap();
 		multipliedDist = new int[graph.getNodeCount()];
 		prevEdges = new int[graph.getNodeCount()];
 	}
 
 	@Override
 	public void setRequest(ComputeRequest req) {
 		// reset dists
 		for (int i = 0; i < multipliedDist.length; i++) {
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
 
 		res = req.getResultObject();
 
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
 
 		srclat = points.get(0).get("lt");
 		srclon = points.get(0).get("ln");
 		destlat = points.get(1).get("lt");
 		destlon = points.get(1).get("ln");
 
 		srcid = graph.getIDForCoordinates(srclat, srclon);
 		destid = graph.getIDForCoordinates(destlat, destlon);
 		directDistance = calcDirectDistance(srclat, srclon, destlat, destlon);
 		multipliedDist[srcid] = 0;
 		heap.insert(srcid, multipliedDist[srcid]);
 
 		int nodeID = -1;
 		int nodeDist;
 		int targetNode = 0;
 		int tempDist;
 
 		long starttime = System.nanoTime();
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
 				targetNode = graph.getOutTarget(nodeID, i);
 
 				// without multiplier = shortest path
 				// tempDist = dist[nodeID] + graph.getOutDist(nodeID, i);
 
 				// with multiplier = fastest path
 				tempDist = multipliedDist[nodeID]
 						+ graph.getOutMultipliedDist(nodeID, i);
 
 				if (tempDist < multipliedDist[targetNode]) {
 					multipliedDist[targetNode] = tempDist;
 					prevEdges[targetNode] = graph.getOutEdgeID(nodeID, i);
 					heap.insert(targetNode, multipliedDist[targetNode]);
 				}
 			}
 		}
 		dijkstratime = System.nanoTime();
 
 		if (nodeID != destid) {
 			// TODO: send errmsg to client and do something useful
 			System.err.println("There is no path from src to dest");
 			return;
 		}
 
 		// Find out how much space to allocate
 		int currNode = nodeID;
 		int routeElements = 0;
 
 		do {
 			routeElements++;
 			currNode = graph.getSource(prevEdges[currNode]);
 		} while (currNode != srcid);
 
		System.out.println("path goes over " + routeElements + " nodes");
 		double[] lats = new double[routeElements];
 		double[] lons = new double[routeElements];
 
 		// backtracking here
 		// Don't read distance from dist[], because there are distances with
 		// regard to the multiplier
 		int distance = 0;
 		currNode = nodeID;
 		do {
 			distance += graph.getDist(prevEdges[currNode]);
 			routeElements--;
 			lats[routeElements] = graph.getNodeLat(currNode);
 			lons[routeElements] = graph.getNodeLon(currNode);
 			currNode = graph.getSource(prevEdges[currNode]);
 		} while (currNode != srcid);
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
 
 		res.put("points", new Points(lats, lons));
 		res.put("misc", misc);
 	}
 }
