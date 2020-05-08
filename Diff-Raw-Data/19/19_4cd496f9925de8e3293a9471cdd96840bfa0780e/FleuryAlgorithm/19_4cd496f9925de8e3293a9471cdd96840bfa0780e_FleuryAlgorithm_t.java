 package com.github.haw.ai.gkap.algorithms;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.Set;
 import java.util.List;
 import com.github.haw.ai.gkap.graph.Edge;
 import com.github.haw.ai.gkap.graph.Graph;
 import com.github.haw.ai.gkap.graph.Vertex;
 
 public class FleuryAlgorithm<E,V> {
 	private Graph<E, V> graph;
 	private List<Edge<E, V>> path;
 	private HashSet<Edge<E, V>> marked;
 	
 	public static <E, V> List<Edge<E, V>> fleuryPath(Graph graph) {
 		FleuryAlgorithm<E, V> result = new FleuryAlgorithm<E, V>(graph);
 		result.runAlgorithm();
 		return result.path();
 	}
 	
 	public FleuryAlgorithm(Graph<E,V> graph) {
 		this.graph = graph;
 	}
 	public void runAlgorithm() {
 		this.marked = new HashSet<Edge<E,V>>();
 		Set<Edge<E,V>> unbridged = null;
 		Set<Edge<E,V>> incidented = null;
 		this.path = new ArrayList<Edge<E, V>>();
 		Edge<E,V> edge = null;
 		
 		if (graph.vertices().isEmpty()) return; 
 		
 		// 1. Whle einen beliebigen Knoten als aktuellen Knoten.
		Vertex<V> current = startVertex();
 		
 		// 5. Wenn noch unmarkierte Kanten existieren, dann gehe zu Schritt 2.
 		while (marked.size() < graph.edges().size()) {
 			// 2. Whle unter den unmarkierten, mit dem aktuellen Knoten inzidenten 
 			// Kanten eine beliebige Kante aus. Dabei sind zuerst Kanten zu whlen, 
 			// die im unmarkierten Graphen keine Brckenkanten sind.
 			incidented = graph.incident(current);
 			incidented.removeAll(marked);
 			unbridged = unbridged(incidented);
 			if (unbridged.size() > 0) {
 				edge = unbridged.iterator().next();
 			}
 			else {
 				if (incidented.iterator().hasNext()) {
 					edge = incidented.iterator().next();
 				}
 				else {
 					System.out.println("sorry, no edge left");
 					System.out.println(path);
					break;
 				}
 			}
 			// 3. Markiere die gewhlte Kante und fge sie der Kantenfolge hinzu.
 			marked.add(edge);
			path.add(edge);
 			// 4. Whle den anderen Knoten der gewhlten Kante als neuen aktuellen Knoten.
 			current = edge.otherVertex(current);
 		} 
 	}
 	
	private Vertex<V> startVertex() {
		for (Vertex<V> v : graph.vertices()) {
			if (graph.incident(v).size() % 2 == 1) {
				return v;
			}
		}
		return graph.vertices().iterator().next();
	}

 	public List<Edge<E, V>> path() {
 		return this.path;
 	}
 	
 	private Set<Edge<E,V>> unbridged(Set<Edge<E,V>> edges) {
 		Set<Edge<E,V>> result = new HashSet<Edge<E,V>>();
 		for (Edge<E,V> e : edges) {
 			if (e.isBridge(graph)) result.add(e);
 		}
 		return result;
 	}
 }
