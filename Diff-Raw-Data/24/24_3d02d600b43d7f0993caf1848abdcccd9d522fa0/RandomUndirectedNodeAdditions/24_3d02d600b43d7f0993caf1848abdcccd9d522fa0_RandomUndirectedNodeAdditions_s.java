 package dna.updates.undirected;
 
 import dna.graph.Graph;
 import dna.graph.GraphDatastructures;
 import dna.graph.Node;
 import dna.graph.undirected.UndirectedEdge;
 import dna.graph.undirected.UndirectedGraph;
 import dna.graph.undirected.UndirectedGraphDatastructures;
 import dna.graph.undirected.UndirectedNode;
 import dna.updates.Batch;
 import dna.updates.NodeAddition;
 import dna.util.parameters.IntParameter;
 
 /**
  * 
  * batch generator for random node additions. new nodes are added starting with
  * the current largest node index. no node is added twice.
  * 
  * @author benni
  * 
  */
 public class RandomUndirectedNodeAdditions extends UndirectedBatchGenerator {
 
 	private int additions;
 
 	/**
 	 * 
 	 * @param additions
 	 *            number of nodes to add with each batch
 	 * @param datastructures
 	 *            datastructures
 	 */
 	public RandomUndirectedNodeAdditions(int additions,
 			UndirectedGraphDatastructures datastructures) {
 		super("randomUndirectedNodeAdditions", new IntParameter("additions",
 				additions), datastructures);
 		this.additions = additions;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public Batch<UndirectedEdge> generate(
 			Graph<? extends Node<UndirectedEdge>, UndirectedEdge> graph) {
 		UndirectedGraph g = (UndirectedGraph) graph;
 		Batch<UndirectedEdge> batch = new Batch<UndirectedEdge>(
 				(GraphDatastructures) this.ds, graph.getTimestamp(),
 				graph.getTimestamp() + 1, this.additions, 0, 0, 0, 0, 0);
 		int index = graph.getMaxNodeIndex() + 1;
 		while (batch.getSize() < this.additions) {
 			UndirectedNode n = (UndirectedNode) this.ds.newNodeInstance(index);
 			if (!g.containsNode(n)) {
				batch.add(new NodeAddition<UndirectedEdge>(this.ds
						.newNodeInstance(index)));
 			}
 			index++;
 		}
 		return batch;
 	}
 
 	@Override
 	public void reset() {
 	}
 
 }
