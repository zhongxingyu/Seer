 package dna.updates.directed;
 
 import dna.graph.Graph;
 import dna.graph.GraphDatastructures;
 import dna.graph.Node;
 import dna.graph.directed.DirectedEdge;
 import dna.graph.directed.DirectedGraph;
 import dna.graph.directed.DirectedGraphDatastructures;
 import dna.graph.directed.DirectedNode;
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
 public class RandomDirectedNodeAdditions extends DirectedBatchGenerator {
 
 	private int additions;
 
 	/**
 	 * 
 	 * @param additions
 	 *            number of nodes to add with each batch
 	 * @param datastructures
 	 *            datastructures
 	 */
 	public RandomDirectedNodeAdditions(int additions,
 			DirectedGraphDatastructures datastructures) {
 		super("randomDirectedNodeAdditions", new IntParameter("additions",
 				additions), datastructures);
 		this.additions = additions;
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	@Override
 	public Batch<DirectedEdge> generate(
 			Graph<? extends Node<DirectedEdge>, DirectedEdge> graph) {
 		DirectedGraph g = (DirectedGraph) graph;
 		Batch<DirectedEdge> batch = new Batch<DirectedEdge>(
 				(GraphDatastructures) this.ds, graph.getTimestamp(),
 				graph.getTimestamp() + 1, this.additions, 0, 0, 0, 0, 0);
 		int index = graph.getMaxNodeIndex() + 1;
 		while (batch.getSize() < this.additions) {
 			DirectedNode n = (DirectedNode) this.ds.newNodeInstance(index);
 			if (!g.containsNode(n)) {
				batch.add(new NodeAddition<DirectedEdge>(n));
 			}
 			index++;
 		}
 		return batch;
 	}
 
 	@Override
 	public void reset() {
 	}
 
 }
