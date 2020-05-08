 package de.tourenplaner.algorithms;
 
 import de.tourenplaner.computecore.ComputeRequest;
 import de.tourenplaner.computecore.RequestPoints;
 import de.tourenplaner.graphrep.GraphRep;
 
 /**
  * @author Christoph Haag, Sascha Meusel, Niklas Schnelle, Peter Vollmer
  *
  * Provides an implementation of NNSearch to find nearest neighbours for given points.
  */
 public class NNSearch extends GraphAlgorithm {
 
 	public NNSearch(GraphRep graph) {
 		super(graph);
 	}
 
 	@Override
 	public void compute(ComputeRequest req) throws ComputeException {
 		assert req != null : "We ended up without a request object in run";
 
 		// TODO: send error messages to client
 		RequestPoints points = req.getPoints();
 		// Check if we have enough points to do something useful
 		if (points.size() < 1) {
 			throw new ComputeException("Not enough points, need at least 1");
 		}
 		nearestNeighbourLookup(points);
 	}
 
 
     /**
     * find the nearest neighbours to the given points in the graphrep
      *
      * @param points
      */
 	public void nearestNeighbourLookup(RequestPoints points) {
 		int nodeID;
 
 		for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
 			nodeID = graph.getIdForCoordinates(points.getPointLat(pointIndex),
 					points.getPointLon(pointIndex));
 			points.setPointLat(pointIndex, graph.getNodeLat(nodeID));
 			points.setPointLon(pointIndex, graph.getNodeLon(nodeID));
 		}
 	}
 
 }
