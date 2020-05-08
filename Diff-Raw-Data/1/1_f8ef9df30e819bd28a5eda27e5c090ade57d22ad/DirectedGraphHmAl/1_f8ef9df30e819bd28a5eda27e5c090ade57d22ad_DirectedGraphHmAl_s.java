 package dna.graph.directed;
 
 import dna.graph.edges.EdgesAl;
 import dna.graph.nodes.NodesHm;
 
 public class DirectedGraphHmAl extends DirectedGraph {
 
 	public DirectedGraphHmAl(String name, long timestamp, int nodes, int edges) {
 		super(name, timestamp, new NodesHm<DirectedNode, DirectedEdge>(nodes),
 				new EdgesAl<DirectedEdge>(edges));
 
 }
