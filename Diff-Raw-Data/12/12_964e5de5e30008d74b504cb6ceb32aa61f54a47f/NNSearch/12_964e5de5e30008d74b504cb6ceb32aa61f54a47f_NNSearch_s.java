 package algorithms;
 
import graphrep.GraphRep;

 import computecore.ComputeRequest;
import computecore.Points;
 
 public class NNSearch extends GraphAlgorithm {
 
 	public NNSearch(GraphRep graph) {
 		super(graph);
 	}
 
 	@Override
 	public void compute(ComputeRequest req) throws ComputeException {
 		assert req != null : "We ended up without a request object in run";
 
 		// TODO: send error messages to client
		Points points = req.getPoints();
 		// Check if we have enough points to do something useful
 		if (points.size() < 1) {
 			throw new ComputeException("Not enough points, need at least 1");
 		}
 		nearestNeighbourLookup(points);
 	}
 
	public void nearestNeighbourLookup(Points points) {
 		int nodeID;
 
 		for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
 			nodeID = graph.getIdForCoordinates(points.getPointLat(pointIndex),
 					points.getPointLon(pointIndex));
 			points.setPointLat(pointIndex, graph.getNodeLat(nodeID));
 			points.setPointLon(pointIndex, graph.getNodeLon(nodeID));
 		}
 	}
 
 }
