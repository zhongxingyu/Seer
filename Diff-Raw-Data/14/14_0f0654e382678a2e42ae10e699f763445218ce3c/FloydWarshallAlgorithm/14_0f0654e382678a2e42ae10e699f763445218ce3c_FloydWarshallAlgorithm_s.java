 package com.github.haw.ai.gkap.graph;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 public class FloydWarshallAlgorithm<E,V> {
 	private final Graph<E,V> graph;
 	private Map<Integer, Vertex<V>> map;
 	private Matrix edgeCost;
 	private Matrix next;
 	private Matrix floyd;
 	private Matrix warshall;
 	
 	private FloydWarshallAlgorithm(Graph<E, V> graph) {
 		super();
 		this.graph = graph;
 		this.map = new HashMap<Integer, Vertex<V>>();
 	}
 	public static FloydWarshallAlgorithm valueOf(Graph graph) {
 		return new FloydWarshallAlgorithm(graph);
 	}
 	public void runAlgorithm() {
 		int c = 0;
 		for (Vertex<V> v : graph.vertices()) {
 			this.map.put(c, v);
 			c++;
 		}
 		
 //		Algorithmus von Floyd
 //
 //		(1) Fr alle i,j : d[i,j] = w[i,j]
 //		(2) Fr k = 1 bis n
 //		(3)    Fr alle Paare i,j
 //		(4)        d[i,j] = min (d[i,j],d[i,k] + d[k,j])
 		edgeCost = edgeCostMatrix();
 		next = edgeCost.clone();
 		next.clear();
 		floyd = edgeCost.clone();
 		for (int k = 1; k < floyd.getHeight(); k++) {
 			for (int i = 0; i < floyd.getHeight(); i++) {
 				for (int j = 0; j < floyd.getWidth(); j++) {
 					if (floyd.get(i, k)+floyd.get(k, j) < floyd.get(i, j)) {
 						floyd.set(i, j, floyd.get(i, k)+floyd.get(k, j));
 						next.set(i, j, (double) k);
 					}
 				}
 			}
 		}
 		
 //		Algorithmus von Warshall
 //
 //		(1) Fr k = 1 bis n
 //		(2)   Fr i = 1 bis n
 //		(3)     Falls d[i,k] = 1
 //		(4)       Fr j = 1 bis n
 //		(5)         Falls d[k,j] = 1 : d[i,j] = 1
 		warshall = floyd.clone();
 		for (int k = 1; k < warshall.getHeight(); k++) {
 			for (int i = 1; i < warshall.getHeight(); i++) {
 				if (warshall.get(i, k) == 1) {
 					for (int j = 1; j < warshall.getWidth(); j++) {
 						if (warshall.get(k, j) == 1) {
 							warshall.set(i, j, 1.0);
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public List<Vertex<V>> shortestPath(Vertex<V> start, Vertex<V> finish) {
 		if (start == null || finish == null || !graph.vertices().contains(start) || !graph.vertices().contains(finish)) {
 			throw new IllegalArgumentException("Something went wrong with your start nodes.");
 		}
 		List<Vertex<V>> result = new ArrayList<Vertex<V>>();
 		int v = 0, t = 0;
 		
 		for (Map.Entry<Integer, Vertex<V>> e : map.entrySet()) {
 			if (e.getValue().equals(start)) {
 				v = e.getKey();
 			}
 			if (e.getValue().equals(finish)) {
 				t = e.getKey();
 			}
 		}
		
 		List<Integer> steps = path(v, t);
 		for (Integer i : steps) { 
 			result.add(map.get(i));
 		}
 		return result;
 	}
 	
 	private List<Integer> path(int s, int e) {
 		List<Integer> result = new ArrayList<Integer>(); 
 //		Weg rekonstuieren
 //		 10    if path[i][j] equals infinity then
 //		 11      return "no path";
 //		 12    int intermediate := next[i][j];
 //		 13    if intermediate equals 'null' then
 //		 14      return " ";   /* there is an edge from i to j, with no vertices between */
 //		 15    else
 //		 16      return GetPath(i,intermediate) + intermediate + GetPath(intermediate,j);
		if (!Double.isInfinite(warshall.get(s, e))) {
 			Double cur = next.get(s, e);
 			if (cur != 0) {
 				result.addAll(path(s,cur.intValue()));
 				result.add(cur.intValue());
 				result.addAll(path(cur.intValue(),e));
 			}
 		}
 		return result;
 	}
 	
 	private Matrix edgeCostMatrix() {
 		int n = graph.vertices().size(), i=0,j=0;
 		Double low = Double.POSITIVE_INFINITY, content;
 		Matrix result = new Matrix(n,n);
 		Set<Edge<E,V>> edges;
 		for (Vertex<V> v : graph.vertices()) {
 			for (Vertex<V> t : graph.vertices()) {
 
 				if (graph.isAdjacent(v, t)) {
 					edges = (Set)((HashSet)graph.incident(v)).clone();
 					edges.retainAll(graph.incident(t));
 					for (Edge<E, V> e : edges) {
 						content = (Double) e.content();
 						if (content < low) {
 							low = content;
 						}
 					}
 					result.set(i, j, low);
 				}
 				else {
 					result.set(i, j, Double.POSITIVE_INFINITY);
 				}
 				j++;
 			}
			low = Double.POSITIVE_INFINITY;
 			j = 0;
 			i++;
 		}
 		return result;
 	}
 }
