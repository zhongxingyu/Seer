 package controller;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashSet;
 
 import model.Edge;
 import model.Graph;
 import model.Vertex;
 import tools.Shell;
 
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
 
 	private int i = 0;
 	private void generateImage() {
 		try {
 			File tempFile = File.createTempFile("graphviz", null);
 			
 			BufferedWriter out = new BufferedWriter(new FileWriter(tempFile.getAbsoluteFile()));
 			out.write("graph g {\n");
 			HashSet<Edge> processed = new HashSet<Edge>();
 			for (Edge e : graph.getEdges()) {
 				if (processed.contains(e.getTarget().findEdge(e.getOrigin()))) {
 					continue;
 				}
 				out.write("\t" + e.getOrigin() + " -- " + e.getTarget() + " [color=" + e.getColor() + " label=" + e.getWeight() + "];\n");
 				processed.add(e);
 			}
 			out.write("}\n");
 			out.close();
 			
 			// run dot to generate gifs
 			Shell.exec("circo -Tgif -o kruskal" + i++ + ".gif < " + tempFile.getAbsolutePath());
 			
 			//
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
 	}
 	
 	public void execute() {
 		
 		// prepare all edges for display
 		for (Edge e : graph.getEdges()) {
 			e.setColor("grey");
 		}
 		generateImage();
 		
 		while (true) {
 			Edge shortestEdge = getShortestEdge();
 			
 			if (shortestEdge == null) {
 				System.out.println("no shortest edge found");
 				System.exit(0);
 			}
 			
 			System.out.println("shortest found: " + shortestEdge);
 			
 			shortestEdge.setColor("red");
 			shortestEdge.getTarget().findEdge(shortestEdge.getOrigin()).setColor("red");
 			generateImage();
 			
 			forrest.add(shortestEdge);
 			
 			if (forrest.size() == 1 && forrest.countEdges() == graph.getVertices().size() - 1) {
 				break;
 			}
 		}
 		
 		System.out.println("done");
 		
 		try {
 			// make an animated gif from out images
 			Shell.exec("gifsicle --delay=200 --loop kruskal*.gif > anim_kruskal.gif");
 			
 			// clean up
			Shell.exec("rm /home/igor/kruskal*.gif");
 		} catch (IOException ex) {
 			ex.printStackTrace();
 		}
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
 			
 			// check for curcuits (origin and target connect to forrest)
 			if (forrest.connectsBoth(e)) {
 				continue;
 			}
 			
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
 				if (edge.getOrigin() == e.getOrigin() || edge.getOrigin() == e.getTarget()) return true;
 				if (edge.getTarget() == e.getOrigin() || edge.getTarget() == e.getTarget()) return true;
 			}
 			return false;
 		}
 		
 		// do both edges touch the tree
 		public boolean connectsBoth(Edge edge) {
 			int c = 0;
 			for (Edge e : edges) {
 				if (edge.getOrigin() == e.getOrigin() || edge.getOrigin() == e.getTarget()) c++;
 				if (edge.getTarget() == e.getOrigin() || edge.getTarget() == e.getTarget()) c++;
 				
 				if (c >= 2) {
 					// both edges touch the tree
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		// number of edges
 		public int size() {
 			return edges.size();
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
 			
 			// remove t1
 			remove(t1);
 			
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
 		
 		// does edge connect to tree
 		public boolean connectsBoth(Edge e) {
 			for (Tree t : trees) {
 				if (t.connectsBoth(e)) {
 					return true;
 				}
 			}
 			return false;
 		}
 		
 		// count all edges
 		public int countEdges() {
 			int count = 0;
 			for (Tree t : trees) {
 				count += t.size();
 			}
 			return count;
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
