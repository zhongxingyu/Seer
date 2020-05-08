 package adp2.implementations;
 
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import adp2.interfaces.Graph;
 import adp2.interfaces.Path;
 
 public class NaG implements Graph {
 	public static Graph instance;
 
 	public static Graph valueOf() {
 		if (instance == null) {
             instance = new NaG();
         }
         return instance;
 	}
 
 	private NaG() {}
 
 	@Override
 	public int distance(int von, int nach) {
 		return -1;
 	}
 
 	@Override
 	public Set<Integer> neighbors(int node) {
 		return new HashSet<Integer>();
 	}
 
 	@Override
 	public Set<Integer> allNodes() {
 		return new HashSet<Integer>();
 	}
 
 	@Override
 	public double intensity(int von, int nach) {
 		return -1;
 	}
 
 	@Override
 	public void decrementPheromone(int value) {}
 
 
 	@Override
 	public void highlightPath(Path p) {
 	}
 
 	@Override
 	public void incrementPheromone(List<List<Integer>> pheromoneUpdateList) {
 		
 	}
 }
