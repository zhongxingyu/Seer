 package controller;
 
 import java.util.HashSet;
 
 import model.Edge;
 import model.Graph;
 import model.Vertex;
 
 public class Kruskal {
 	private Graph graph;
 	private Forrest forrest = new Forrest();
 	private HashSet<Edge> cleanGrid = new HashSet<Edge>();
 	
 	public Kruskal(Graph graph) {
 		this.graph = graph;
 		
 		// initialise clean grid
 		// clean grid contains no reverse edges
 		for (Vertex v : graph.getVertices()) {
 			for (Edge e : v.getEdges()) {
 				if (!cleanGrid.contains(e.getTarget().findEdge(v))) {
 					// ignore reverse connections
 					cleanGrid.add(e);
 				}
 			}
 		}
 	}
 	
 	public void execute() {
 		int i = 0;
 		
 		while (true) {
 			Edge shortestEdge = getShortestEdge();
 			
 			if (shortestEdge == null) {
 				System.out.println("no shortest edge found");
 				System.exit(0);
 			}
 			
 			System.out.println("shortest found: " + shortestEdge);
 			
 			forrest.add(shortestEdge);
 			
 			// all trees are connected
 			if (forrest.size() == 1 && i >= 1) {
 				break;
 			}
 			
 			/*if (tree.size() - 1 == graph.getVertices().size()) {
 				break;
 			}*/
 			
 			i++;
 		}
 		
 		System.out.println("done");
 	}
 	
 	// get shortest edge that does not complete a circuit
 	private Edge getShortestEdge() {
 		Edge shortest = null;
 		for (Edge e : cleanGrid) {
 			
 			// no point if it's already included
 			// also reverse-edges
 			if (forrest.contains(e) || forrest.contains(e.getTarget().findEdge(e.getOrigin()))) {
 				continue;
 			}
 			
 			// check for curcuits
 			
 			if (shortest == null || e.getWeight() < shortest.getWeight()) {
 				shortest = e;
 			}
 		}
 		return shortest;
 	}
 	
 	// a tree contains a set of edges
 	public class Tree {
 		private HashSet<Edge> edges = new HashSet<Edge>();
 		
 		public void add(Edge e) {
 			edges.add(e);
 		}
 		
 		public void remove(Edge e) {
 			edges.remove(e);
 		}
 		
 		public HashSet<Edge> getEdges() {
 			return edges;
 		}
 		
 		// contains edge or reverse-edge
 		public boolean contains(Edge edge) {
 			return edges.contains(edge) || edges.contains(edge.getTarget().findEdge(edge.getOrigin()));
 		}
 		
 		// does the edge touch the tree
 		public boolean connects(Edge edge) {
 			for (Edge e : edges) {
				if (edge.getOrigin() == e.getOrigin() || e.getOrigin() == e.getTarget()) return true;
				if (edge.getTarget() == e.getOrigin() || e.getTarget() == e.getTarget()) return true;
 			}
 			return false;
 		}
 	}
 	
 	// a forrest contains a list of separate (disconnected) trees
 	public class Forrest {
 		private HashSet<Tree> trees = new HashSet<Tree>();
 		
 		public void add(Tree t) {
 			trees.add(t);
 		}
 		
 		public void remove(Tree t) {
 			trees.remove(t);
 		}
 		
 		// merge two trees
 		public void merge(Tree t1, Tree t2) {
 			for (Edge e : t1.getEdges()) {
 				t2.add(e);
 			}
 			
 			// update reference
 			t1 = t2;
 		}
 		
 		public int size() {
 			return trees.size();
 		}
 		
 		public HashSet<Tree> getTrees() {
 			return trees;
 		}
 		
 		// add edge to forrest
 		public void add(Edge e) {
 			Tree tree1 = null;
 			Tree tree2 = null;
 			for (Tree t : trees) {
 				if (t.connects(e)) {
 					tree1 = t;
 					break;
 				}
 			}
 			if (tree1 != null) {
 				for (Tree t : trees) {
 					if (t != tree1 && t.connects(e)) {
 						tree2 = t;
 						break;
 					}
 				}
 			}
 			
 			if (tree1 != null && tree2 != null) {
 				// if edge connects to two trees, merge
 				tree1.add(e);
 				tree2.add(e);
 				merge(tree1, tree2);
 			} else if (tree1 != null) {
 				// connects to one tree, add
 				tree1.add(e);
 			} else {
 				
 				// new tree, create
 				Tree t = new Tree();
 				t.add(e);
 				add(t);
 			}
 		}
 		
 		// contains edge
 		public boolean contains(Edge e) {
 			for (Tree t : trees) {
 				if (t.contains(e)) return true;
 			}
 			return false;
 		}
 	}
 	
 	public static void main(String[] args) {
 		Vertex a = new Vertex("A");
 		Vertex b = new Vertex("B");
 		Vertex c = new Vertex("C");
 		Vertex d = new Vertex("D");
 		Vertex e = new Vertex("E");
 		Vertex f = new Vertex("F");
 
 		a.connectTo(b, 2);
 		a.connectTo(c, 6);
 		b.connectTo(c, 3);
 		b.connectTo(d, 3);
 		b.connectTo(e, 4);
 		c.connectTo(d, 1);
 		d.connectTo(e, 5);
 		d.connectTo(f, 5);
 		e.connectTo(f, 3);
 		
 		Graph graph = new Graph();
 		graph.addVertex(a, b, c, d, e, f);
 		
 		Kruskal kruskal = new Kruskal(graph);
 		kruskal.execute();
 	}
 }
