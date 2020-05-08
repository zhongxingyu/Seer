 package dna.metrics.clusterCoefficient;
 
 import dna.graph.Node;
 import dna.graph.directed.DirectedEdge;
 import dna.graph.directed.DirectedNode;
 import dna.graph.undirected.UndirectedEdge;
 import dna.graph.undirected.UndirectedNode;
 import dna.updates.Batch;
 import dna.updates.EdgeAddition;
 import dna.updates.EdgeRemoval;
 import dna.updates.NodeAddition;
 import dna.updates.NodeRemoval;
 import dna.updates.Update;
 import dna.util.ArrayUtils;
 
 @SuppressWarnings("rawtypes")
 public class ClosedTriangleClusteringCoefficientUpdate extends
 		ClosedTriangleClusteringCoefficient {
 
 	public ClosedTriangleClusteringCoefficientUpdate() {
 		super("closedTriangleClusteringCoefficientUpdate",
 				ApplicationType.BeforeAndAfterUpdate);
 	}
 
 	@Override
 	public boolean applyBeforeBatch(Batch b) {
 		return false;
 	}
 
 	@Override
 	public boolean applyAfterBatch(Batch b) {
 		return false;
 	}
 
 	@Override
 	public boolean applyBeforeUpdate(Update u) {
 		if (DirectedNode.class.isAssignableFrom(this.g.getGraphDatastructures()
 				.getNodeType())) {
 			return this.applyBeforeUpdateDirected(u);
 		} else if (UndirectedNode.class.isAssignableFrom(this.g
 				.getGraphDatastructures().getNodeType())) {
 			return this.applyBeforeUpdateUndirected(u);
 		}
 		return false;
 	}
 
 	@Override
 	public boolean applyAfterUpdate(Update u) {
 		if (DirectedNode.class.isAssignableFrom(this.g.getGraphDatastructures()
 				.getNodeType())) {
 			return this.applyAfterUpdateDirected(u);
 		} else if (UndirectedNode.class.isAssignableFrom(this.g
 				.getGraphDatastructures().getNodeType())) {
 			return this.applyAfterUpdateUndirected(u);
 		}
 		return false;
 	}
 
 	private boolean applyBeforeUpdateDirected(Update u) {
 		if (u instanceof NodeAddition) {
 			Node n = ((NodeAddition) u).getNode();
 			this.localCC = ArrayUtils.set(this.localCC, n.getIndex(), 0,
 					Double.NaN);
 			this.nodePotentialCount = ArrayUtils.set(this.nodePotentialCount,
 					n.getIndex(), 0, Long.MIN_VALUE);
 			this.nodeTriangleCount = ArrayUtils.set(this.nodeTriangleCount,
 					n.getIndex(), 0, Long.MIN_VALUE);
 			this.averageCC = ArrayUtils.avgIgnoreNaN(this.localCC);
 		} else if (u instanceof NodeRemoval) {
 
 			DirectedNode n = (DirectedNode) ((NodeRemoval) u).getNode();
 
 			DirectedNode[] neighbors = new DirectedNode[n.getNeighborCount()];
 			int index = 0;
 			for (DirectedNode neighbor : n.getNeighbors()) {
 				neighbors[index++] = neighbor;
 			}
 
 			for (int i = 0; i < neighbors.length; i++) {
 				for (int j = i + 1; j < neighbors.length; j++) {
 					if (neighbors[i].hasEdge(new DirectedEdge(neighbors[i],
 							neighbors[j]))
 							&& neighbors[i].hasEdge(new DirectedEdge(
 									neighbors[j], neighbors[i]))) {
 						this.removeTriangle(n);
 						this.removeTriangle(neighbors[i]);
 						this.removeTriangle(neighbors[j]);
 					}
 				}
 				this.removePotentials(neighbors[i],
 						neighbors[i].getNeighborCount() - 1);
 			}
 
 			this.removePotentials(n,
 					n.getNeighborCount() * (n.getNeighborCount() - 1) / 2);
 
 			this.localCC[n.getIndex()] = Double.NaN;
 			this.nodePotentialCount[n.getIndex()] = Long.MIN_VALUE;
 			this.nodeTriangleCount[n.getIndex()] = Long.MIN_VALUE;
 			this.localCC = ArrayUtils.truncateNaN(this.localCC);
 			this.nodePotentialCount = ArrayUtils.truncate(
 					this.nodePotentialCount, Long.MIN_VALUE);
 			this.nodeTriangleCount = ArrayUtils.truncate(
 					this.nodeTriangleCount, Long.MIN_VALUE);
 
			this.averageCC = ArrayUtils.avgIgnoreNaN(this.localCC);

 		} else if (u instanceof EdgeAddition) {
 			DirectedEdge e = (DirectedEdge) ((EdgeAddition) u).getEdge();
 			DirectedNode a = e.getSrc();
 			DirectedNode b = e.getDst();
 			if (a.hasEdge(new DirectedEdge(b, a))) {
 				// new triangles
 				for (DirectedNode c : a.getNeighbors()) {
 					if (b.hasNeighbor(c)) {
 						this.addTriangle(a);
 						this.addTriangle(b);
 						this.addTriangle(c);
 					}
 				}
 				// new potentials
 				this.addPotentials(a, a.getNeighborCount());
 				this.addPotentials(b, b.getNeighborCount());
 			}
 		}
 		return true;
 	}
 
 	private boolean applyAfterUpdateDirected(Update u) {
 		if (u instanceof EdgeRemoval) {
 			DirectedEdge e = (DirectedEdge) ((EdgeRemoval) u).getEdge();
 			DirectedNode a = e.getSrc();
 			DirectedNode b = e.getDst();
 			if (a.hasEdge(new DirectedEdge(b, a))) {
 				// remove triangles
 				for (DirectedNode c : a.getNeighbors()) {
 					if (b.hasNeighbor(c)) {
 						this.removeTriangle(a);
 						this.removeTriangle(b);
 						this.removeTriangle(c);
 					}
 				}
 				// remove potentials
 				this.removePotentials(a, a.getNeighborCount());
 				this.removePotentials(b, b.getNeighborCount());
 			}
 		}
 		return true;
 	}
 
 	private boolean applyBeforeUpdateUndirected(Update u) {
 		if (u instanceof NodeAddition) {
 			Node n = ((NodeAddition) u).getNode();
 			this.localCC = ArrayUtils.set(this.localCC, n.getIndex(), 0,
 					Double.NaN);
 			this.nodePotentialCount = ArrayUtils.set(this.nodePotentialCount,
 					n.getIndex(), 0, Long.MIN_VALUE);
 			this.nodeTriangleCount = ArrayUtils.set(this.nodeTriangleCount,
 					n.getIndex(), 0, Long.MIN_VALUE);
 		} else if (u instanceof NodeRemoval) {
 			UndirectedNode n = (UndirectedNode) ((NodeRemoval) u).getNode();
 
 			UndirectedNode[] neighbors = new UndirectedNode[n.getDegree()];
 			int index = 0;
 			for (UndirectedEdge e : n.getEdges()) {
 				neighbors[index++] = e.getDifferingNode(n);
 			}
 
 			for (int i = 0; i < neighbors.length; i++) {
 				for (int j = i + 1; j < neighbors.length; j++) {
 					if (neighbors[i].hasEdge(new UndirectedEdge(neighbors[i],
 							neighbors[j]))) {
 						this.removeTriangle(n);
 						this.removeTriangle(neighbors[i]);
 						this.removeTriangle(neighbors[j]);
 					}
 				}
 				this.removePotentials(neighbors[i],
 						neighbors[i].getDegree() - 1);
 			}
 
 			this.removePotentials(n, n.getDegree() * (n.getDegree() - 1) / 2);
 
 			this.localCC[n.getIndex()] = Double.NaN;
 			this.nodePotentialCount[n.getIndex()] = Long.MIN_VALUE;
 			this.nodeTriangleCount[n.getIndex()] = Long.MIN_VALUE;
 			this.localCC = ArrayUtils.truncateNaN(this.localCC);
 			this.nodePotentialCount = ArrayUtils.truncate(
 					this.nodePotentialCount, Long.MIN_VALUE);
 			this.nodeTriangleCount = ArrayUtils.truncate(
 					this.nodeTriangleCount, Long.MIN_VALUE);
 		} else if (u instanceof EdgeAddition) {
 			UndirectedEdge e = (UndirectedEdge) ((EdgeAddition) u).getEdge();
 			UndirectedNode a = e.getNode1();
 			UndirectedNode b = e.getNode2();
 			// new triangles
 			for (UndirectedEdge c_ : a.getEdges()) {
 				UndirectedNode c = c_.getDifferingNode(a);
 				if (c.hasEdge(new UndirectedEdge(c, b))) {
 					this.addTriangle(a);
 					this.addTriangle(b);
 					this.addTriangle(c);
 				}
 			}
 			// new potentials
 			this.addPotentials(a, a.getDegree());
 			this.addPotentials(b, b.getDegree());
 		}
 		return true;
 	}
 
 	private boolean applyAfterUpdateUndirected(Update u) {
 		if (u instanceof EdgeRemoval) {
 			UndirectedEdge e = (UndirectedEdge) ((EdgeRemoval) u).getEdge();
 			UndirectedNode a = e.getNode1();
 			UndirectedNode b = e.getNode2();
 			for (UndirectedEdge a_ : a.getEdges()) {
 				UndirectedNode c = a_.getDifferingNode(a);
 				if (c.hasEdge(new UndirectedEdge(c, b))) {
 					// remove triangles
 					this.removeTriangle(a);
 					this.removeTriangle(b);
 					this.removeTriangle(c);
 				}
 			}
 			// remove potentials
 			this.removePotentials(a, a.getDegree());
 			this.removePotentials(b, b.getDegree());
 		}
 		return true;
 	}
 
 }
