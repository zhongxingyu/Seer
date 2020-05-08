 package dna.graph.nodes;
 
import java.util.Arrays;
 import java.util.Collection;
 
 import dna.graph.Edge;
 import dna.graph.Node;
 import dna.util.Rand;
 
 public class NodesA<N extends Node<E>, E extends Edge> extends Nodes<N, E> {
 
 	private N[] nodes;
 
 	private int nodeCount;
 
 	@SuppressWarnings("unchecked")
 	public NodesA(int nodes) {
 		this.nodes = (N[]) new Node[nodes];
 		this.nodeCount = 0;
 	}
 
 	@Override
 	public N getNode(int index) {
 		if (this.nodes.length <= index) {
 			return null;
 		}
 		return this.nodes[index];
 	}
 
 	@Override
 	public int getMaxNodeIndex() {
 		return this.nodes.length - 1;
 	}
 
 	@Override
 	public int getNodeCount() {
 		return this.nodeCount;
 	}
 
 	@Override
 	public Collection<N> getNodes() {
		return Arrays.asList(this.nodes);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public boolean addNode(N n) {
 		if (this.nodes.length == 0) {
 			this.nodes = (N[]) new Node[n.getIndex() + 1];
 		}
 		while (n.getIndex() >= this.nodes.length) {
 			N[] nodes = (N[]) new Node[this.nodes.length * 2];
 			System.arraycopy(this.nodes, 0, nodes, 0, this.nodes.length);
 			this.nodes = nodes;
 		}
 		if (this.nodes[n.getIndex()] != null) {
 			return false;
 		}
 		this.nodes[n.getIndex()] = n;
 		this.nodeCount++;
 		return true;
 	}
 
 	@Override
 	public boolean removeNode(N n) {
 		if (this.nodes.length <= n.getIndex()) {
 			return false;
 		}
 		if (this.nodes[n.getIndex()] == null) {
 			return false;
 		}
 		this.nodes[n.getIndex()] = null;
		// TODO decrease size of array in case last node is removed
 		this.nodeCount--;
 		return true;
 	}
 
 	@Override
 	public boolean containsNode(N n) {
 		return this.nodes.length > n.getIndex()
 				&& this.nodes[n.getIndex()] != null;
 	}
 
 	@Override
 	public N getRandomNode() {
		return this.nodes[Rand.rand.nextInt(this.nodes.length)];
 	}
 }
