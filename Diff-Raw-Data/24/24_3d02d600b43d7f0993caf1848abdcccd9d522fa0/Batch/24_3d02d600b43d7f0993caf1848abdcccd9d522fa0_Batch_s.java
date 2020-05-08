 package dna.updates;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import com.google.common.collect.Iterables;
 
 import dna.graph.Edge;
 import dna.graph.Graph;
 import dna.graph.GraphDatastructures;
 import dna.graph.Node;
 import dna.graph.directed.DirectedEdge;
 import dna.graph.directed.DirectedGraphDatastructures;
 import dna.graph.undirected.UndirectedEdge;
 import dna.graph.undirected.UndirectedGraphDatastructures;
 import dna.util.Log;
 
 public class Batch<E extends Edge> {
 
 	private ArrayList<Update<E>> nodeAdditions;
 
 	private ArrayList<Update<E>> nodeRemovals;
 
 	private ArrayList<Update<E>> nodeWeightUpdates;
 
 	private ArrayList<Update<E>> edgeAdditions;
 
 	private ArrayList<Update<E>> edgeRemovals;
 
 	private ArrayList<Update<E>> edgeWeightUpdates;
 
 	private Iterable<Update<E>> all;
 
 	private GraphDatastructures<Graph<Node<E>, E>, Node<E>, E> ds;
 
 	private long from;
 
 	private long to;
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public Batch(DirectedGraphDatastructures ds, long from, long to) {
 		this((GraphDatastructures) ds, from, to);
 	}
 
 	@SuppressWarnings({ "unchecked", "rawtypes" })
 	public Batch(UndirectedGraphDatastructures ds, long from, long to) {
 		this((GraphDatastructures) ds, from, to);
 	}
 
 	public Batch(GraphDatastructures<Graph<Node<E>, E>, Node<E>, E> ds,
 			long from, long to) {
 		this(ds, from, to, 0, 0, 0, 0, 0, 0);
 	}
 
 	@SuppressWarnings("unchecked")
 	public Batch(GraphDatastructures<Graph<Node<E>, E>, Node<E>, E> ds,
 			long from, long to, int nodeAdditions, int nodeRemovals,
 			int nodeWeightUpdates, int edgeAdditions, int edgeRemovals,
 			int edgeWeightUpdates) {
 		this.nodeAdditions = new ArrayList<Update<E>>(nodeAdditions);
 		this.nodeRemovals = new ArrayList<Update<E>>(nodeRemovals);
 		this.nodeWeightUpdates = new ArrayList<Update<E>>(nodeWeightUpdates);
 		this.edgeAdditions = new ArrayList<Update<E>>(edgeAdditions);
 		this.edgeRemovals = new ArrayList<Update<E>>(edgeRemovals);
 		this.edgeWeightUpdates = new ArrayList<Update<E>>(edgeWeightUpdates);
 		this.all = Iterables.unmodifiableIterable(Iterables.concat(
 				this.nodeAdditions, this.nodeRemovals, this.nodeWeightUpdates,
 				this.edgeAdditions, this.edgeRemovals, this.edgeWeightUpdates));
 		this.ds = ds;
 		this.from = from;
 		this.to = to;
 	}
 
 	public String toString() {
 		return "B " + this.from + " -> " + to + " ("
 				+ this.getNodeAdditionCount() + ","
 				+ this.getNodeRemovalCount() + ","
 				+ this.getNodeWeightUpdateCount() + "/"
 				+ this.getEdgeAdditionCount() + ","
 				+ this.getEdgeRemovalCount() + ","
 				+ this.getEdgeWeightUpdateCount() + ")";
 	}
 
 	public boolean addAll(Iterable<Update<E>> updates) {
 		boolean success = true;
 		for (Update<E> u : updates) {
 			success &= this.add(u);
 		}
 		return success;
 	}
 
 	public boolean add(Update<E> update) {
 		if (update instanceof NodeAddition<?>) {
 			this.nodeAdditions.add((NodeAddition<E>) update);
 			return true;
 		}
 		if (update instanceof NodeRemoval<?>) {
 			this.nodeRemovals.add((NodeRemoval<E>) update);
 			return true;
 		}
 		if (update instanceof NodeWeightUpdate<?>) {
 			this.nodeWeightUpdates.add((NodeWeightUpdate<E>) update);
 			return true;
 		}
 		if (update instanceof EdgeAddition<?>) {
 			this.edgeAdditions.add((EdgeAddition<E>) update);
 			return true;
 		}
 		if (update instanceof EdgeRemoval<?>) {
 			this.edgeRemovals.add((EdgeRemoval<E>) update);
 			return true;
 		}
 		if (update instanceof EdgeWeightUpdate<?>) {
 			this.edgeWeightUpdates.add((EdgeWeightUpdate<E>) update);
 			return true;
 		}
 		return false;
 	}
 
 	public Iterable<Update<E>> getNodeAdditions() {
 		return nodeAdditions;
 	}
 
 	public Iterable<Update<E>> getNodeRemovals() {
 		return nodeRemovals;
 	}
 
 	public Iterable<Update<E>> getNodeWeightUpdates() {
 		return nodeWeightUpdates;
 	}
 
 	public Iterable<Update<E>> getEdgeAdditions() {
 		return edgeAdditions;
 	}
 
 	public Iterable<Update<E>> getEdgeRemovals() {
 		return edgeRemovals;
 	}
 
 	public Iterable<Update<E>> getEdgeWeightUpdates() {
 		return edgeWeightUpdates;
 	}
 
 	public Iterable<Update<E>> getAllUpdates() {
 		return this.all;
 	}
 
 	public int getNodeAdditionCount() {
 		return nodeAdditions.size();
 	}
 
 	public int getNodeRemovalCount() {
 		return nodeRemovals.size();
 	}
 
 	public int getNodeWeightUpdateCount() {
 		return nodeWeightUpdates.size();
 	}
 
 	public int getEdgeAdditionCount() {
 		return edgeAdditions.size();
 	}
 
 	public int getEdgeRemovalCount() {
 		return edgeRemovals.size();
 	}
 
 	public int getEdgeWeightUpdateCount() {
 		return edgeWeightUpdates.size();
 	}
 
 	public int getSize() {
 		return this.nodeAdditions.size() + this.nodeRemovals.size()
 				+ this.nodeWeightUpdates.size() + this.edgeAdditions.size()
 				+ this.edgeRemovals.size() + this.edgeWeightUpdates.size();
 	}
 
 	public GraphDatastructures<Graph<Node<E>, E>, Node<E>, E> getGraphDatastructures() {
 		return this.ds;
 	}
 
 	public long getFrom() {
 		return this.from;
 	}
 
 	public long getTo() {
 		return this.to;
 	}
 
 	public void print() {
 		this.print(this.nodeAdditions);
 		this.print(this.nodeRemovals);
 		this.print(this.nodeWeightUpdates);
 		this.print(this.edgeAdditions);
 		this.print(this.edgeRemovals);
 		this.print(this.edgeWeightUpdates);
 	}
 
 	private void print(ArrayList<? extends Update<E>> list) {
 		for (Update<E> u : list) {
 			System.out.println(u);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public boolean apply(Graph<? extends Node<E>, ? extends E> graph) {
 		boolean success = true;
 		Graph<Node<E>, E> g = (Graph<Node<E>, E>) graph;
 		success &= this.apply(g, this.nodeAdditions);
 		return success;
 	}
 
 	private boolean apply(Graph<Node<E>, E> g, ArrayList<Update<E>> updates) {
 		boolean success = true;
 		for (Update<E> u : updates) {
 			success &= u.apply(g);
 		}
 		return success;
 	}
 
 	/**
 	 * performs a sanitization of the updates stored in this batch, i.e.,
 	 * deleted all edge removals that point to a node which is removed anyways
 	 * and delete all edge additions that point to a node that is removed but
 	 * not added again
 	 * 
 	 * @return
 	 */
 	@SuppressWarnings({ "rawtypes" })
 	public BatchSanitizationStats sanitize() {
 		BatchSanitizationStats stats = new BatchSanitizationStats();
 
 		HashSet<Node> removedN = new HashSet<Node>(this.getNodeRemovalCount());
 		for (Update u : this.nodeRemovals) {
 			removedN.add(((NodeRemoval) u).getNode());
 		}
 
 		HashSet<Node> addedN = new HashSet<Node>(this.getNodeAdditionCount());
 		for (Update u : this.nodeAdditions) {
 			addedN.add(((NodeAddition) u).getNode());
 		}
 
 		HashSet<Edge> removedE = new HashSet<Edge>(this.getEdgeRemovalCount());
 		for (Update u : this.edgeRemovals) {
 			removedE.add(((EdgeRemoval) u).getEdge());
 		}
 
 		/**
 		 * delete edge removals of nodes which are going to be deleted anyways
 		 */
 		HashSet<EdgeRemoval> edgeRemovalsToDelete = new HashSet<EdgeRemoval>();
 		for (Update u : this.edgeRemovals) {
 			Edge e = ((EdgeRemoval) u).getEdge();
 			Node[] n = this.getNodesFromEdge(e);
 			if (removedN.contains(n[0]) || removedN.contains(n[1])) {
 				edgeRemovalsToDelete.add((EdgeRemoval) u);
 			}
 		}
 		for (EdgeRemoval u : edgeRemovalsToDelete) {
 			this.edgeRemovals.remove(u);
 		}
 		stats.setDeletedEdgeRemovals(edgeRemovalsToDelete.size());
 
 		/**
 		 * delete edge additions of nodes which are going to be deleted anyways
 		 * (only keep them in case the node is afterwards added again)
 		 */
 		HashSet<EdgeAddition> edgeAdditionsToDelete = new HashSet<EdgeAddition>();
 		for (Update u : this.edgeAdditions) {
 			Edge e = ((EdgeAddition) u).getEdge();
 			Node[] n = this.getNodesFromEdge(e);
 			if ((removedN.contains(n[0]) && !addedN.contains(n[0]))
 					|| (removedN.contains(n[1]) && !addedN.contains(n[1]))) {
 				edgeAdditionsToDelete.add((EdgeAddition) u);
 			}
 		}
 		for (EdgeAddition u : edgeAdditionsToDelete) {
 			this.edgeAdditions.remove(u);
 		}
 		stats.setDeletedEdgeAdditions(edgeAdditionsToDelete.size());
 
 		/**
 		 * delete node weight updates for nodes that are removed anyways
 		 */
 		HashSet<NodeWeightUpdate> nodeWeightUpdatesToDelete = new HashSet<NodeWeightUpdate>();
 		for (Update u : this.nodeWeightUpdates) {
 			Node n = ((NodeWeightUpdate) u).getNode();
 			if (removedN.contains(n)) {
 				nodeWeightUpdatesToDelete.add((NodeWeightUpdate) u);
 			}
 		}
 		for (NodeWeightUpdate u : nodeWeightUpdatesToDelete) {
 			this.nodeWeightUpdates.remove(u);
 		}
 		stats.setDeletedNodeWeightUpdates(nodeWeightUpdatesToDelete.size());
 
 		/**
 		 * delete edge weight updates for edges that are removed anyways (either
 		 * by themselves or because they point to a node that is to be removed)
 		 */
 		HashSet<EdgeWeightUpdate> edgeWeightUpdatesToDelete = new HashSet<EdgeWeightUpdate>();
 		for (Update u : this.edgeWeightUpdates) {
 			Edge e = ((EdgeWeightUpdate) u).getEdge();
 			Node[] n = this.getNodesFromEdge(e);
 			if (removedE.contains(e) || removedN.contains(n[0])
 					|| removedN.contains(n[1])) {
 				edgeWeightUpdatesToDelete.add((EdgeWeightUpdate) u);
 				continue;
 			}
 		}
 		for (EdgeWeightUpdate u : edgeWeightUpdatesToDelete) {
 			this.edgeWeightUpdates.remove(u);
 		}
 		stats.setDeletedEdgeWeightUpdates(edgeWeightUpdatesToDelete.size());
 
 		return stats;
 	}
 
 	@SuppressWarnings("rawtypes")
 	private Node[] getNodesFromEdge(Edge e) {
 		if (e instanceof DirectedEdge) {
 			return new Node[] { ((DirectedEdge) e).getSrc(),
 					((DirectedEdge) e).getDst() };
 		} else if (e instanceof UndirectedEdge) {
 			return new Node[] { ((UndirectedEdge) e).getNode1(),
 					((UndirectedEdge) e).getNode2() };
 		} else {
 			Log.error("edge type '" + e.getClass().getCanonicalName()
 					+ "' not supported in batch sanitization");
 			return null;
 		}
 	}
 }
