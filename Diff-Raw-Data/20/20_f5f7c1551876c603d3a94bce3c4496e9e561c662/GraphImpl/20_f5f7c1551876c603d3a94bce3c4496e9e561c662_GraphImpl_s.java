 package com.github.haw.ai.gkap.graph;
 
 import java.util.Set;
 
 /**
  * @author Till Theis <till.theis@haw-hamburg.de>
  * @author Patrick Detlefsen <patrick.detlefsen@haw-hamburg.de>
  * @author Benjamin Rexin <benjamin.rexin@haw-hamburg.de>
  */
 public class GraphImpl<E, V> implements Graph<E, V> {
 	public static <E, V> Graph<E, V> valueOf(Set<Edge<E, V>> edges,
 			Set<Vertex<V>> vertices) {
 		return new GraphImpl<E, V>(edges, vertices);
 	}
 
 	private Set<Edge<E, V>> edges;
 	private Set<Vertex<V>> vertices;
 
 	private GraphImpl(Set<Edge<E, V>> edges, Set<Vertex<V>> vertices) {
 		this.edges = edges;
 		this.vertices = vertices;
 	}
 
 	@Override
 	public int degree(Vertex<V> vertex) {
 		// TODO Auto-generated method stub
 		return 0;
 	}
 
 	@Override
 	public boolean isAdjacent(Vertex<V> vertex, Vertex<V> otherVertex) {
 		for (Edge<E, V> e : edges) {
 			if (e.vertices().contains(vertex)
 					&& e.vertices().contains(otherVertex)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	@Override
 	public boolean isIncident(Vertex<V> vertex, Edge<E, V> edge) {
 		if (edge.vertices().contains(vertex)) {
 			return true;
 		}
 		return false;
 	}
 
 	@Override
 	public Set<Vertex<V>> adjacent(Vertex<V> vertex) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
	public Set<Edge<E, V>> incident(Vertex<V> vertex) {
    Set<Edge<E, V> result = new HashSet<Edge<E,V>();

    for (Edge<E,V> edge : edges) {
      if (isIncident(vertex,edge) {
        result.add(edge);
      }
    }

    return result;
 	}
 }
