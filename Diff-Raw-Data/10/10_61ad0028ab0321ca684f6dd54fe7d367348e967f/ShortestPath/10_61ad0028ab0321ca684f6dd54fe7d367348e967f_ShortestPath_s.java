 package algorithms;
 
 import graphrep.GraphRep;
 
 import java.util.HashMap;
 import java.util.Map;
 
import computecore.ComputeRequest;
import computecore.Points;

 public class ShortestPath extends GraphAlgorithm {
 
 	// DijkstraStructs used by the ShortestPathCH
 	private final DijkstraStructs ds;
 
 	double directDistance;
 
 	public ShortestPath(GraphRep graph, DijkstraStructs rs) {
 		super(graph);
 		ds = rs;
 	}
 
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
 
 	@Override
 	public void compute(ComputeRequest req) throws ComputeException {
 		assert (req != null) : "We ended up without a request object in run";
 
		Points points = req.getPoints();
 
 		// Check if we have enough points to do something useful
 		if (points.size() < 2) {
 			throw new ComputeException("Not enough points, need at least 2");
 		}
 
 		Points resultPoints = req.getResulWay();
 		int distance = 0;
 		int srclat, srclon;
 		int destlat, destlon;
 		int srcId, destId;
 		int resultAddIndex = 0;
 
 		for (int pointIndex = 0; pointIndex < points.size() - 1; pointIndex++) {
 			// get data structures to use
 			try {
 				int[] dists = ds.borrowDistArray();
 				int[] prevEdges = ds.borrowPrevArray();
 				Heap heap = ds.borrowHeap();
 
 				srclat = points.getPointLat(pointIndex);
 				srclon = points.getPointLon(pointIndex);
 				destlat = points.getPointLat(pointIndex + 1);
 				destlon = points.getPointLon(pointIndex + 1);
 
 				srcId = graph.getIdForCoordinates(srclat, srclon);
 				destId = graph.getIdForCoordinates(destlat, destlon);
 				directDistance = calcDirectDistance(srclat, srclon, destlat,
 						destlon);
 
 				dists[srcId] = 0;
 				heap.insert(srcId, dists[srcId]);
 
 				int nodeId = -1;
 				int nodeDist;
 				int targetNode = 0;
 				int tempDist;
 
 				long starttime = System.nanoTime();
 				long dijkstratime;
 				long backtracktime;
 
 				DIJKSTRA: while (!heap.isEmpty()) {
 					nodeId = heap.peekMinId();
 					nodeDist = heap.peekMinDist();
 					heap.removeMin();
 					if (nodeId == destId) {
 						break DIJKSTRA;
 					} else if (nodeDist > dists[nodeId]) {
 						continue;
 					}
 					for (int i = 0; i < graph.getOutEdgeCount(nodeId); i++) {
 						// Ignore Shortcuts
 						if (graph.getOutFirstShortcuttedEdge(nodeId, i) != -1) {
 							continue;
 						}
 						targetNode = graph.getOutTarget(nodeId, i);
 
 						// without multiplier = shortest path
 						tempDist = dists[nodeId] + graph.getOutDist(nodeId, i);
 
 						// with multiplier = fastest path
 						// tempDist = multipliedDist[nodeID]
 						// + graph.getOutMultipliedDist(nodeID, i);
 
 						if (tempDist < dists[targetNode]) {
 							dists[targetNode] = tempDist;
 							prevEdges[targetNode] = graph.getOutEdgeId(nodeId,
 									i);
 							heap.insert(targetNode, dists[targetNode]);
 						}
 					}
 				}
 				dijkstratime = System.nanoTime();
 
 				if (nodeId != destId) {
 					System.err.println("There is no path from src: " + srcId
 							+ " to trgt: " + destId);
 					throw new ComputeException("No path found");
 				}
 
 				// Find out how much space to allocate
 				int currNode = nodeId;
 				int routeElements = 0;
 
 				while (currNode != srcId) {
 					routeElements++;
 					currNode = graph.getSource(prevEdges[currNode]);
 				}
 
 				System.out
 						.println("path goes over " + routeElements + " nodes");
 				// Add points to the end
 				resultAddIndex = resultPoints.size();
 				// Add them without values we set the values in the next step
 				resultPoints.addEmptyPoints(routeElements);
 
 				// backtracking here
 				// Don't read distance from multipliedDist[], because there are
 				// distances with
 				// regard to the multiplier
 				currNode = nodeId;
 				while (routeElements > 0) {
 					distance += graph.getDist(prevEdges[currNode]);
 					routeElements--;
 
 					resultPoints.setPointLat(resultAddIndex + routeElements,
 							graph.getNodeLat(currNode));
 					resultPoints.setPointLon(resultAddIndex + routeElements,
 							graph.getNodeLon(currNode));
 
 					currNode = graph.getSource(prevEdges[currNode]);
 				}
 				ds.returnDistArray(false);
 				ds.returnHeap();
 				ds.returnPrevArray();
 				backtracktime = System.nanoTime();
 
 				System.out.println("found sp with dist = "
 						+ (distance / 1000.0) + " km (direct distance: "
 						+ (directDistance / 1000.0)
 						+ " km; Distance with multiplier: "
 						+ (dists[destId] / 1000.0) + ")");
 				System.out.println("Dijkstra: "
 						+ ((dijkstratime - starttime) / 1000000.0)
 						+ " ms; Backtracking: "
 						+ ((backtracktime - dijkstratime) / 1000000.0) + " ms");
 			} catch (IllegalAccessException e) {
 				// if this happens we likely got a programming bug
 				e.printStackTrace();
 			}
 
 		}
 
 		Map<String, Object> misc = new HashMap<String, Object>(1);
 		misc.put("distance", distance);
 		req.setMisc(misc);
 	}
 }
