 package edu.ppt.impossible.helpers;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import edu.ppt.impossible.model.Graph;
 import edu.ppt.impossible.model.Node;
 
 public class RandomNodeGroupper implements NodeGroupper {
 
 	private final Random random;
 
 	public RandomNodeGroupper(Random random) {
 		super();
 		this.random = random;
 	}
 
 	@Override
	public List<Node> group(Graph graph, int groupSize) {
 		List<Node> result = new ArrayList<>();
 		for (int n = 0; n < groupSize; ++n) {
 			Node candidate;
 			do {
				int index = random.nextInt(graph.getNumNodes());
				candidate = graph.getNode(index);
 
 			} while (result.contains(candidate));
 			result.add(candidate);
 		}
 		return result;
 	}
 
 }
