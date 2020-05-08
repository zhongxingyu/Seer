 package org.javaan.graph;
 
 import java.util.HashSet;
 import java.util.Set;
 
 public class CondensedEdge<V, E> {
 
 	private final V target;
 	
 	private final V source;
 	
 	private final Set<E> edges = new HashSet<>();
 	
	public CondensedEdge(V target, V source) {
 		this.target = target;
 		this.source = source;
 	}
 
 	public V getTarget() {
 		return target;
 	}
 
 	public V getSource() {
 		return source;
 	}
 
 	public Set<E> getEdges() {
 		return edges;
 	}
 	
 	public void addEdge(E edge) {
 		edges.add(edge);
 	}
 }
