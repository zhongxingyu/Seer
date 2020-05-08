 package ex3.search;
 
 import ex3.PRMGraph;
import ex3.PRMUtil;
 import ex3.Vertex;
 import ex3.heuristics.Heuristic;
 import java.util.LinkedList;
 import java.util.HashMap;
import java.util.HashSet;
 
 public class Dijkstra implements SearchAlgorithm {
 
     Heuristic heuristic;
 
     public Dijkstra(Heuristic h){
         this.heuristic = h;
     }
 
     @Override
    public LinkedList<Vertex> shortestPath(Vertex v1, Vertex v2, PRMGraph graph, PRMUtil util) {
         HashMap<Vertex, DijkstraTuple> map = new HashMap<Vertex, DijkstraTuple>();
 	HashSet<Vertex> unchecked = new HashSet<Vertex>();
 	
         for (Vertex inGraph : graph.getVertices()) {
             map.put(inGraph, new DijkstraTuple(Double.MAX_VALUE, null));
	    unchecked.add(inGraph);
 	}
 
         map.get(v1).setDistance(0);
 
 	while(!unchecked.isEmpty()){
 	    Vertex minVertex = shortestDistance(map);
 	    DijkstraTuple vData = map.get(minVertex);
 	    unchecked.remove(minVertex);
 	    
 	    if (minVertex.isEqual(v2)){ // If we found the destination
 		LinkedList<Vertex> path = new LinkedList<Vertex>();
 		path.addFirst(minVertex); // Add the end vertex to the list
 		
 		Vertex current = minVertex;
 		DijkstraTuple curData = vData;
 		
                 // Go back through the list and construct the path
 		while (curData != null){
 		    current = curData.getPrevious();
 		    curData = map.get(current);
 		    
 		    path.addFirst(current);
 		}
 						
 		return path;
 		
 	    }
 	    
 	    // Break if remaining vertices inaccessible from this node
 	    if (vData.getDistance() == Double.MAX_VALUE){
 		break;
 	    }
 	    
 	    for (Vertex neighbour : minVertex.getConnectedVertices()){
		if (!unchecked.contains(neighbour)){
 		    continue; // skip the node if we've already visited it.
 		}
 		
 		// Get the distance through the current vertex to the neighbour
 		double altDist = vData.getDistance() + util.getEuclideanDistance(minVertex, neighbour);
 		
 		DijkstraTuple nData = map.get(neighbour);
 		
 		// If the distance through the current node to the neighbour is shorter than the
 		// current tentative distance we have for that neighbour
 		if (altDist < nData.getDistance()){
 		    nData.setDistance(altDist);
 		    nData.setPrevious(minVertex);
 		}
 		
 	    }
 	}
 	
 	return null;
 	
     }
 
     /*
      * Finds the node in the map which has the shortest distance.
      */
     private Vertex shortestDistance(HashMap<Vertex, DijkstraTuple> map){
	double minDist = Double.MAX_VALUE;
 	double curDist = minDist;
 	Vertex minVertex = null;
 	
 	for (Vertex dt : map.keySet()){
 	    curDist = map.get(dt).getDistance();
 	    if (curDist < minDist){
 		minDist = curDist;
 		minVertex = dt;
 	    }
 	    
 	}
 	return minVertex;
     }
 
 }
