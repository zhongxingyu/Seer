 package a3_p01_JS_MJ;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Set;
 
 import org.jgrapht.Graph;
 import org.jgrapht.Graphs;
 import org.jgrapht.WeightedGraph;
 import org.jgrapht.graph.DefaultWeightedEdge;
 import org.jgrapht.graph.WeightedPseudograph;
 import org.jgrapht.util.FibonacciHeap;
 import org.jgrapht.util.FibonacciHeapNode;
 
 import a1_p01_JS_MJ.SearchResult;
 import a2_p01_JS_MJ.AttributedNode;
 
 public class Prim<T extends Comparable<T>> {
 	
 
 	public SearchResult algorithm(
 			WeightedGraph<T, DefaultWeightedEdge> graph) {
		int acc = 0;
 		Set<T> nodeSet = new HashSet<T>(graph.vertexSet());
 		Iterator<T> it = nodeSet.iterator();
 		T sourceNode = it.next();
 		nodeSet.remove(sourceNode);
 		WeightedGraph<T, DefaultWeightedEdge> newGraph = new WeightedPseudograph<T, DefaultWeightedEdge>(
 				DefaultWeightedEdge.class);
 		newGraph.addVertex(sourceNode);
		acc++;
 		List<T> neigh;
 		List<DefaultWeightedEdge> l = new ArrayList<DefaultWeightedEdge>();
 		PriorityQueue<QueueEntry<T>> prioQueue = new PriorityQueue<QueueEntry<T>>();
 
 		while (!nodeSet.isEmpty()) {
 			neigh = Graphs.neighborListOf(graph, sourceNode);// Wir benutzen nur die Knoten die noch nciht im Baum drin sind.
 			neigh.retainAll(nodeSet);// Also die Schnittmenge der Nachbarn udn der noch nicht vorhandenen Knoten.
 			for (T node : neigh)// Alle Kanten suchen die von uns abgehen.
 			{
				acc++;
 				prioQueue.add(getQueueEntry(
 						graph.getAllEdges(sourceNode, node), graph));// Falls wir PArallelen haben muessen wuir alle Edges hinzufuegen.
 			}
 			QueueEntry<T> q = prioQueue.poll();
			acc++;
 			newGraph.addVertex(q.getTargetNode());
 			newGraph.addVertex(q.getSourceNode());
 			newGraph.addEdge(q.getSourceNode(), q.getTargetNode(), q.getEdge());
 			// Graphs.addEdgeWithVertices(newGraph, graph, q.getEdge());
 			if (Util.checkForCircles(newGraph)) {
 				newGraph.removeEdge(q.getEdge());
 			} else {
 				// count++;
 				try{
 				nodeSet.remove(q.getTargetNode());
 				nodeSet.remove(q.getSourceNode());
 				}catch(NullPointerException ex){}
 				graph.setEdgeWeight(q.getEdge(), q.getWeight());
				
 			}
 			if(sourceNode != q.getTargetNode())
 				{
 				sourceNode = q.getTargetNode();
 				}else
 				{
 					sourceNode = q.getSourceNode();
 				}
 		}
 		SearchResult s = new SearchResult(newGraph,
 				new ArrayList<DefaultWeightedEdge>(newGraph.edgeSet()), null,
 				acc);
 		return s;
 	}
 
 	private QueueEntry<T> getQueueEntry(Set<DefaultWeightedEdge> set,
 			WeightedGraph<T, DefaultWeightedEdge> g) {
 		double weight = Double.MAX_VALUE;
 		DefaultWeightedEdge edge = null;
 		for (DefaultWeightedEdge e : set) {
 			double d = g.getEdgeWeight(e);
 			if (d < weight) {
 				edge = e;
 				weight = d;
 			}
 		}
 		return new QueueEntry<T>(edge, g.getEdgeSource(edge),
 				g.getEdgeTarget(edge), weight);
 	}
 
 	public SearchResult algorithmWithFibo(
 			WeightedGraph<T, DefaultWeightedEdge> graph) {
 		int acc =0,accWeight=0;
 		Set<T> nodeSet = new HashSet<T>(graph.vertexSet());
 		Iterator<T> it = nodeSet.iterator();
 
 		T sourceNode = it.next();
 		it = null;
 		nodeSet.remove(sourceNode);
 		acc++;
 		WeightedGraph<T, DefaultWeightedEdge> newGraph = new WeightedPseudograph<T, DefaultWeightedEdge>(
 				DefaultWeightedEdge.class);
 		newGraph.addVertex(sourceNode);
 		List<T> neigh;
 		FibonacciHeap<QueueEntry<T>> heap = new FibonacciHeap<QueueEntry<T>>();
 		while (!nodeSet.isEmpty()) {
 			neigh = Graphs.neighborListOf(graph, sourceNode);// Wir benutzen nur die Knoten die noch nciht im Baum drin sind.
 			neigh.retainAll(nodeSet);// Also die Schnittmenge der Nachbarn udn der noch nicht vorhandenen Knoten.
 			for (T node : neigh)// Alle Kanten suchen die von uns abgehen.
 			{
 				QueueEntry<T> q = getQueueEntry(
 						graph.getAllEdges(sourceNode, node), graph);
 				acc++;
 				heap.insert(new FibonacciHeapNode<QueueEntry<T>>(q),
 						q.getWeight());
 			}
 			QueueEntry<T> q = heap.removeMin().getData();
 			acc++;
 //			if (!newGraph.containsVertex(q.getSourceNode())
 //					|| !newGraph.containsVertex(q.getTargetNode())) // Wenn er einen von beiden nicht hat
 //			{
 				newGraph.addVertex(q.getTargetNode());
 				newGraph.addVertex(q.getSourceNode());
 				newGraph.addEdge(q.getSourceNode(), q.getTargetNode(),
 						q.getEdge());
 				if (Util.checkForCircles(newGraph)) {
 					newGraph.removeEdge(q.getEdge());
 				} else {
 					 try{
 					nodeSet.remove(q.getTargetNode());
 					 nodeSet.remove(q.getSourceNode());
 					 }catch (NullPointerException e){}
 					graph.setEdgeWeight(q.getEdge(), q.getWeight());
 					accWeight+=q.getWeight();
 				}
 //			}
 				if(sourceNode != q.getTargetNode())
 				{
 				sourceNode = q.getTargetNode();
 				}else
 				{
 					sourceNode = q.getSourceNode();
 				}
 		}
 		 SearchResult s = new SearchResult(graph, new ArrayList<DefaultWeightedEdge>(newGraph.edgeSet()),null,acc);
 		// ArrayList<DefaultWeightedEdge>(newGraph.edgeSet()), null, acc);
 		System.out.println("Prim mit Prio:\n______________________\nZugriffe: "+acc+"\nGesamtGewicht: "+accWeight);
 		return s;
 	}
 
 }
 
 class QueueEntry<T extends Comparable<T>> implements Comparable<QueueEntry> {
 	private DefaultWeightedEdge edge;
 	private T targetNode, sourceNode;
 	private double weight;
 
 	QueueEntry(DefaultWeightedEdge e, T sourceNode, T targetNode, double weight) {
 		this.sourceNode = sourceNode;
 		this.edge = e;
 		this.targetNode = targetNode;
 		this.weight = weight;
 	}
 
 	T getSourceNode() {
 		return sourceNode;
 	}
 
 	@Override
 	public int compareTo(QueueEntry o) {
 		return (int) (this.weight - o.weight);
 
 	}
 
 	DefaultWeightedEdge getEdge() {
 		return edge;
 	}
 
 	T getTargetNode() {
 		return targetNode;
 	}
 
 	double getWeight() {
 		return weight;
 	}
 
 	public String toString() {
 		return "(" + sourceNode.toString() + "," + targetNode.toString() + ","
 				+ weight + ") " + edge.toString();
 	}
 
 }
