 package tspsolver.model.algorithm.start;
 
 import java.util.HashSet;
 import java.util.Set;
 import java.util.Stack;
 import java.util.TreeSet;
 import java.util.Vector;
 
 import tspsolver.model.algorithm.StartAlgorithm;
 import tspsolver.model.comparators.grid.EdgeWeightComparator;
 import tspsolver.model.scenario.grid.Edge;
 import tspsolver.model.scenario.grid.Node;
 
 public class MinimumSpanningTreeHeuristik extends StartAlgorithm {
 
 	private enum Phase {
 		CREATE_SPANNING_TREE, DO_EULERIAN_TRAIL
 	}
 
 	private final Vector<Edge> spanningTreeEdges;
 	private final Set<Node> spanningTreeNodes;
 	private final TreeSet<Edge> spanningTreePossibleEdges;
 	private final Stack<Node> brancheNodes;
 
 	private Phase phase;
 	private Node currentNode;
 
 	public MinimumSpanningTreeHeuristik() {
 		this.spanningTreeEdges = new Vector<Edge>();
 		this.spanningTreeNodes = new HashSet<Node>();
 
 		this.spanningTreePossibleEdges = new TreeSet<Edge>(new EdgeWeightComparator());
 		this.brancheNodes = new Stack<Node>();
 
 		this.reset();
 	}
 
 	@Override
 	protected void doInitialize() {
 		this.spanningTreeNodes.add(this.getStartingNode());
 
 		// Sort all possible edges by weight
 		for (final Edge edge : this.getStartingNode().getEdges()) {
 			this.spanningTreePossibleEdges.add(edge);
 		}
 
 		this.phase = Phase.CREATE_SPANNING_TREE;
 	}
 
 	@Override
 	protected void doReset() {
 		this.spanningTreeNodes.clear();
 		this.spanningTreeEdges.clear();
 
 		this.spanningTreePossibleEdges.clear();
 		this.brancheNodes.clear();
 
 		this.phase = null;
 		this.currentNode = null;
 	}
 
 	@Override
 	public boolean doStep() {
 		boolean success = false;
 		switch (this.phase) {
 			case CREATE_SPANNING_TREE:
 				success = this.doStepCreateSpanningTree();
 				break;
 			case DO_EULERIAN_TRAIL:
 				success = this.doStepEulerianTrail();
 				break;
 			default:
 				break;
 		}
 		return success;
 	}
 
 	private boolean doStepCreateSpanningTree() {
 		final boolean successfulStep = true;
 
 		// Take the lowest one, that not build a circle and add it to the tree.
 		for (final Edge edge : this.spanningTreePossibleEdges) {
 			if (this.spanningTreeNodes.contains(edge.getFirstNode()) == false) {
 
 				// Add the edge to the spanning tree.
 				this.spanningTreeEdges.add(edge);
 				this.getPathUpdater().addEdge(edge);
 
 				if (this.spanningTreeEdges.size() < this.getGrid().getNumberOfNodes() - 1) {
 
 					// Prepare for next step.
 					this.spanningTreeNodes.add(edge.getFirstNode());
 
 					// Add all new possible edges
 					// FIXME: Bin nicht sicher ob dieser Schritt richtig
 					// funktioniert, ich gehe davon aus das in einem TreeSet
 					// jeweils nur eine Instanz des selben Edges drin sein kann.
 					for (final Edge edgeToAdd : edge.getFirstNode().getEdges()) {
 						this.spanningTreePossibleEdges.add(edgeToAdd);
 					}
 
 					// The edge is used now.
 					this.spanningTreePossibleEdges.remove(edge);
 				}
 				else {
 					// finish create spanning tree
 					this.initEulerianTrail();
 				}
 
 				break;
 			}
 			else if (this.spanningTreeNodes.contains(edge.getSecondNode()) == false) {
 
 				// Add the edge to the spanning tree.
 				this.spanningTreeEdges.add(edge);
 				this.getPathUpdater().addEdge(edge);
 
 				if (this.spanningTreeEdges.size() < this.getGrid().getNumberOfNodes() - 1) {
 
 					// Prepare for next step.
 					this.spanningTreeNodes.add(edge.getSecondNode());
 
 					// Add all new possible edges
 					// FIXME: Bin nicht sicher ob dieser Schritt richtig
 					// funktioniert, ich gehe davon aus das in einem TreeSet
 					// jeweils nur eine Instanz des selben Edges drin sein kann.
 					for (final Edge edgeToAdd : edge.getSecondNode().getEdges()) {
 						this.spanningTreePossibleEdges.add(edgeToAdd);
 					}
 
 					// The edge is used now.
 					this.spanningTreePossibleEdges.remove(edge);
 				}
 				else {
 					// finish create spanning tree
 					this.initEulerianTrail();
 				}
 
 				break;
 
 			}
 			else {
 				// This edge will build a circle, remove it to improve
 				// performance
 				// FIXME: Alternative zu diesem Schritt währe, dass man die
 				// jeweiligen Kanten, welche einen Kreis bilden, nicht
 				// hinzufügt. Ich denke aber das diese Variante bei einer
 				// grossen Anzahl Knoten besser ist.
 				this.spanningTreeNodes.remove(edge);
 			}
 		}
 
 		this.getPathUpdater().updatePath();
 		return successfulStep;
 	}
 
 	private void initEulerianTrail() {
 		this.phase = Phase.DO_EULERIAN_TRAIL;
 		this.currentNode = this.getStartingNode();
 		this.brancheNodes.push(this.currentNode);
 	}
 
 	private boolean doStepEulerianTrail() {
 
 		final Node brancheNode = this.brancheNodes.pop();
 
 		int i = 1;
 		while (this.spanningTreeEdges.isEmpty() == false) {
 			final Edge edge = this.spanningTreeEdges.elementAt(this.spanningTreeEdges.size() - i);
 
 			// Find the next edge in the spanning tree that is connected to the
 			// current branch node.
 			if (edge.getFirstNode() == brancheNode) {
 
 				final Edge newEdge = this.currentNode.getEdgeToNode(edge.getSecondNode());
 				if (newEdge == null) {
 					// FIXME: this path does not work, what do we do now?
 					return false;
 				}
 
 				// Remove the edge from the spanning tree, because this edge is
 				// used
 				this.spanningTreeEdges.remove(edge);
 
 				this.getPathUpdater().removeEdge(edge);
 
 				// Add the new edge to the path.
 				this.getPathUpdater().addEdge(newEdge);
 
 				// Set the other node as new current node.
 				this.currentNode = edge.getSecondNode();
 
 				this.brancheNodes.add(brancheNode);
 				this.brancheNodes.add(this.currentNode);
 
 				this.getPathUpdater().updatePath();
 				return true;
 
 			}
 			else if (edge.getSecondNode() == brancheNode) {
 
 				final Edge newEdge = this.currentNode.getEdgeToNode(edge.getFirstNode());
 				if (newEdge == null) {
 					// FIXME: this path does not work, what do we do now?
 					return false;
 				}
 
 				// Remove the edge from the spanning tree, because this edge is
 				// used
 				this.spanningTreeEdges.remove(edge);
 
 				this.getPathUpdater().removeEdge(edge);
 
 				// Add the new edge to the path.
 				this.getPathUpdater().addEdge(newEdge);
 
 				// Set the other node as new current node.
 				this.currentNode = edge.getFirstNode();
 
 				this.brancheNodes.add(brancheNode);
 				this.brancheNodes.add(this.currentNode);
 
 				this.getPathUpdater().updatePath();
 				return true;
 
 			}
			else if (i >= this.spanningTreeEdges.size()) {
 				// A leaf from the spanning tree
 				// Nothing happen to the path, so recall the step.
 
 				// FIXME: Remove the recursion
 				return this.doStepEulerianTrail();
 			}
 			else {
 				i++;
 			}
 		}
 
 		// Finishing the eulerian trail:
 		// Connect the last node from the eulerian path with the start node to
 		// close the circle
 		final Edge newEdge = this.getStartingNode().getEdgeToNode(this.currentNode);
 		if (newEdge == null) {
 			// FIXME: this path does not work, what do we do now?
 			return false;
 		}
 
 		this.getPathUpdater().addEdge(newEdge);
 
 		this.getPathUpdater().updatePath();
 		this.finishedSuccessfully();
 		return true;
 	}
 }
