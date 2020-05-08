 package com.github.haw.ai.gkap.graph;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
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
 		if (edges == null || vertices == null || edges.contains(null) || vertices.contains(null)) {
 			throw new NullPointerException();
 		}
 		this.edges = edges;
 		this.vertices = vertices;
 	}
 
 	@Override
 	public int degree(Vertex<V> vertex) {
 		List<Edge<E,V>> list = new ArrayList<Edge<E,V>>();
 		for (Edge<E,V> edge : edges) {
 			if (isIncident(vertex, edge)) {
 				list.add(edge);
 			}
 		}
 		return list.size();
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
 		Set<Vertex<V>> result = new HashSet<Vertex<V>>();
 		for (Vertex<V> v : vertices) {
 			if (isAdjacent(v, vertex)) {
 				result.add(v);
 			}
 		}
 
 		return result;
 	}
 
 	@Override
 	public Set<Edge<E, V>> incident(Vertex<V> vertex) {
 		Set<Edge<E, V>> result = new HashSet<Edge<E, V>>();
 		for (Edge<E, V> edge : edges) {
 			if (isIncident(vertex, edge)) {
 				result.add(edge);
 			}
 		}
 		return result;
 	}
 	
 	@Override
 	public String toString() {
 	    if (vertices.size() == 0) {
 	        return "EmptyGraph";
 	    } else {
             Set<Vertex<V>> visited = new HashSet<Vertex<V>>();
             Set<Vertex<V>> unvisited = new HashSet<Vertex<V>>(vertices);
             String s = "";
             
             // make multiple runs for graphs with unconnected components
             while (!unvisited.isEmpty()) {
     	        s += toString(unvisited.iterator().next(), visited, new HashSet<Edge<E,V>>(), 0);
     	        unvisited.removeAll(visited);
             }
 	        
 	        return s;
 	    }
 	}
 
 	public String toString(Vertex<V> root, Set<Vertex<V>> visited, Set<Edge<E, V>> visitedEdges, int depth) {
 	    // remember visited edges to not traverse one edge two times in an
 	    // undirected graph
 	    
 	    if (visited.contains(root)) {
 	        return "";
 	    }
 	    
 	    Set<Edge<E, V>> adjacent = adjacenceMap().get(root);
 	    visited.add(root);
 	    String s = "";
 	    String indentation = "";
 	    
 	    for (int i = 0; i < depth*4; ++i) {
 	        indentation += " ";
 	    }
 	    
 	    if (adjacent.size() == 0) {
 	        // single vertex component
 	        s += indentation + root + "\n";
 	    } else {
     	    for (Edge<E,V> edge : adjacent) {
                 if (visitedEdges.contains(edge)) continue;
     	        visitedEdges.add(edge);
     	        
     	        if (edge.vertices().size() == 1) {
     	            // loop
     	            s += indentation + root + "-" + edge + "-" + root + "\n";
     	        } else {
     	            s += indentation + root + "-" + edge + "-" + edge.otherVertex(root) + "\n";
     	            s += toString(edge.otherVertex(root), visited, visitedEdges, depth+1) + "\n";
     	        }
     	    }
 	    }
 	    
 	    return s;
 	}
 
 	/**
 	 * @return a Map with the vertices as its keys and the reachable neighbor
 	 *         vertices as its values (a subset of the actual adjacence set)
 	 */
 	public Map<Vertex<V>, Set<Edge<E, V>>> adjacenceMap() {
 	    // caching
         
         Map<Vertex<V>, Set<Edge<E, V>>> result = new HashMap<Vertex<V>, Set<Edge<E, V>>>();
 	    
 	    // init map
 	    for (Vertex<V> v : vertices) {
 	        result.put(v, new HashSet<Edge<E,V>>());
 	    }
 	    
 	    for (Edge<E, V> edge : edges) {
 	        Set<Vertex<V>> vertices = edge.vertices();
 	        if (vertices.size() == 1) {
 	            // loop
 	            result.get(edge.left()).add(edge);
 	        } else {
 	            if (edge.isReachable(edge.left(), edge.right())) {
 	                result.get(edge.left()).add(edge);
 	            }
 	            
 	            if (edge.isReachable(edge.right(), edge.left())) {
 	                result.get(edge.right()).add(edge);
 	            }
 	        }
 	    }
 	    
 	    return result;
 	}
 
 	@Override
 	public Set<Edge<E, V>> edges() {
 		return new HashSet<Edge<E, V>>(edges);
 	}
 
 	@Override
 	public Set<Vertex<V>> vertices() {
 		return new HashSet<Vertex<V>>(vertices);
 	}
 	
 	
 	
 	// stats ...
 
 
     @Override
     public Set<Vertex<V>> adjacent(Vertex<V> vertex, AccessStats<E, V> stats) {
         Set<Vertex<V>> result = new HashSet<Vertex<V>>();
         for (Vertex<V> v : vertices) {
             stats.increment(v);
             if (isAdjacent(v, vertex)) {
                 result.add(v);
             }
         }
 
         return result;
     }
 
     @Override
     public Set<Edge<E, V>> incident(Vertex<V> vertex, AccessStats<E, V> stats) {
         Set<Edge<E, V>> result = new HashSet<Edge<E, V>>();
         for (Edge<E, V> edge : edges) {
             stats.increment(edge);
             if (isIncident(vertex, edge)) {
                 result.add(edge);
             }
         }
         return result;
     }
 
     @Override
     public boolean isAdjacent(Vertex<V> vertex, Vertex<V> otherVertex, AccessStats<E, V> stats) {
         for (Edge<E, V> e : edges) {
             stats.increment(e);
             
             for (Vertex<V> v : e.vertices()) stats.increment(v,2);
             if (e.vertices().contains(vertex)
                     && e.vertices().contains(otherVertex)) {
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public boolean isIncident(Vertex<V> vertex, Edge<E, V> edge, AccessStats<E, V> stats) {
         for (Vertex<V> v : edge.vertices()) stats.increment(v);
         if (edge.vertices().contains(vertex)) {
             return true;
         }
         return false;
     }
 }
