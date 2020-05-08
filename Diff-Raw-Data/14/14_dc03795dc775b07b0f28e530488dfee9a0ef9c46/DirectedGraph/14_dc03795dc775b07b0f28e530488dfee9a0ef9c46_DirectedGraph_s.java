 package se.chalmers.datastrukt.lab3;
 
 import java.util.*;
 
 public class DirectedGraph<E extends Edge> {
 
 	private List<E>[] edges;
 	private CompKruskalEdge cKE = new CompKruskalEdge();
 	private int noOfNodes;
 	private PriorityQueue<ComparableDijkstraPath> prioQueue;
 
 	public DirectedGraph(int noOfNodes) {
 		this.noOfNodes = noOfNodes;
 		this.prioQueue = new PriorityQueue<ComparableDijkstraPath>();
 		edges = new List[noOfNodes];
 		for (int i = 0; i < noOfNodes; i++) {
 			edges[i] = new LinkedList<E>();
 		}
 	}
 
 	public void addEdge(E e) {
 		edges[e.getSource()].add(e);
 
 	}
 
 	public Iterator<E> shortestPath(int from, int to) {
 		boolean[] visitedNode = new boolean[noOfNodes];

 		prioQueue.add(new ComparableDijkstraPath<E>(from));
 		while (prioQueue.size() != 0) {
			ComparableDijkstraPath<E> dE = prioQueue.poll();
 			if (!visitedNode[dE.to]) {
 				if (dE.to == to) {
 					dE.getPath().iterator();
 				} else {
 					visitedNode[dE.to] = true;
 					for (E edge : this.edges[dE.to]) {
						visitedNode[edge.to] = true;
						prioQueue.add(new ComparableDijkstraPath(dE, edge));
 					}
 				}
 			}
 		}
 		return null;
 
 	}
 
 	public Iterator<E> minimumSpanningTree() {
 		return null;
 	}
 
 	private class CompKruskalEdge {
 
 	}
 
 	private class ComparableDijkstraPath<E extends Edge> implements
 			Comparable<ComparableDijkstraPath> {
 		// private public kmr t dem i klassen eftersom dem r publika men ej
 		// utanfr klassen eftersom dem r i en inre klass!
 		public int to;
 		public double weight;
 		public List<E> theShortestPath;
 
 		public ComparableDijkstraPath(int from) {
 			this.to = from;
 			this.weight = 0;
 			this.theShortestPath = new LinkedList<E>();
 
 		}
 
 		public ComparableDijkstraPath(ComparableDijkstraPath<E> path, E edge) {
 			this.to = path.to; // to?
 			this.weight = path.weight;// hmmm
 			this.theShortestPath = new LinkedList<E>(path.theShortestPath);
 			addpathentToShortestPath(theShortestPath, edge);
 
 		}
 
 		public void addpathentToShortestPath(List<E> shPath, E edge) {
 			theShortestPath.add(edge);
 			this.weight += edge.getWeight();
 			this.to = edge.to;
 		}
 
 		public List<E> getPath() {
 			List<E> temp = new LinkedList<E>();
 
 			for (E path : this.theShortestPath) {
 				temp.add(path);
 			}
 
 			return temp;
 		}
 
 		public int compareTo(ComparableDijkstraPath other) {
 			return Double.compare(weight, other.weight);
 		}
 	}
 }
