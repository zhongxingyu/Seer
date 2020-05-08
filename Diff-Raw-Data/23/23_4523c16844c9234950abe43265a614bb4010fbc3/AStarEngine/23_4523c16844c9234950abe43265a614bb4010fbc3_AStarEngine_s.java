 package ar.edu.itba.sia.gps.impl;
 
 import java.util.Comparator;
 import java.util.PriorityQueue;
 
 import ar.edu.itba.sia.gps.api.GPSProblem;
 import ar.edu.itba.sia.gps.api.SearchStrategy;
 
 public class AStarEngine extends GPSEngine {
 
 	private PriorityQueue<GPSNode> open;
 
 	public AStarEngine(final GPSProblem problem, SearchStrategy strategy) {
 		super(problem, strategy);
 		
 		open = new PriorityQueue<GPSNode>(50, new Comparator<GPSNode>() {
 			public int compare(GPSNode o1, GPSNode o2) {
 				int h1 = problem.getHValue(o1.getState()) + o1.getCost();
 				int h2 = problem.getHValue(o2.getState()) + o2.getCost();
 				return h1 - h2;
 			};
 		});
 	}
 
 	@Override
 	public void addNode(GPSNode node) {
 		open.add(node);
 	}
 
 	@Override
 	protected Iterable<GPSNode> getOpenNodes() {
 		return open;
 	}
 
 	@Override
 	protected GPSNode getNext() {
 		return open.peek();
 	}
 
 	@Override
 	protected void removeNode(GPSNode node) {
 		open.remove();
 	}
 
 	@Override
 	protected int getOpenSize() {
 		return open.size();
 	}
 
 }
