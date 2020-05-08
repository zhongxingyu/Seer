 package logic.algorithm;
 
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.PriorityQueue;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import datamodel.Grid;
 import datamodel.GridElement;
 import static datamodel.GridElementAlgoState.*;
 
 public class Dijkstra implements Algorithm {
 	private static final Logger logger = Logger.getLogger(Dijkstra.class);
 
 	public final static int INFINITE = Integer.MAX_VALUE;
 
 	private Grid grid;
 	private PriorityQueue<GridElement> reachableKnodes;
 	private GridElement startKnode, endKnode;
 	private Collection<GridElement> neighbors;
 
 	public void init(Grid grid) {
 		logger.info("Initialize "+this.getClass().toString());
 
 		this.grid = grid;
 		this.startKnode = grid.getStartElement();
 		this.endKnode   = grid.getEndElement();
 		this.reachableKnodes = new PriorityQueue<GridElement>(1, getComparator());
 
 		if(startKnode == null)
 			throw new IllegalStateException(
 					"No start knode specified for dijkstra algorithm");
 
 		if(endKnode   == null)
 			throw new IllegalStateException(
 					"No target knode specified for dijkstra algorithm");
 
 		for(GridElement v : grid.getKnodes()) {
 			v.setDistance(INFINITE);
 			v.setAlgoState(NONE);
 		}
 
 		startKnode.setDistance(0);
 		startKnode.setAlgoState(LOOKED_AT);
 		startKnode.setPath(null);
 
		Vector<Integer> weights = grid.getNeighborsFrom(startKnode, true).getWayCosts();
		Vector<GridElement> neighbors = grid.getNeighborsFrom(startKnode, true).getNeighbors();
 		// for all neighbors
 		for(GridElement neighbor : neighbors) {
 			reachableKnodes.offer(neighbor);
 			neighbor.setDistance(weights.get(neighbors.indexOf(neighbor)));
 			neighbor.setPath(startKnode);
 		}
 	}
 
 	protected Comparator<GridElement> getComparator() {
 		return new Comparator<GridElement>(){
 			public int compare(GridElement o1, GridElement o2) {
 				return o1.getDistance() - o2.getDistance();
 			}
 		};
 	}
 
 	public boolean doNextStep() {
 
 		if(!reachableKnodes.isEmpty() && endKnode.getAlgoState() != LOOKED_AT) {
 
 			// polling retrieves and removes the head of this queue
 			GridElement nearest = reachableKnodes.poll();
 			nearest.setAlgoState(LOOKED_AT);
 
			Vector<Integer> weights = grid.getNeighborsFrom(nearest, true).getWayCosts();
			Vector<GridElement> neighbors = grid.getNeighborsFrom(nearest, true).getNeighbors();
 			int neighborWeight;
 
 			for(GridElement neighbor : neighbors) {
 				neighborWeight = weights.get(neighbors.indexOf(neighbor));
 
 				if(neighbor.getAlgoState() != LOOKED_AT) {
 					if(neighbor.getDistance() == INFINITE)
 						reachableKnodes.offer(neighbor);
 
 					if(nearest.getDistance() + neighborWeight < neighbor.getDistance()) {
 						neighbor.setDistance(nearest.getDistance() + neighborWeight);
 						neighbor.setPath(nearest);
 					}
 				}
 			}
 
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isSolved() {
 		return endKnode.getAlgoState() == LOOKED_AT;
 	}
 }
