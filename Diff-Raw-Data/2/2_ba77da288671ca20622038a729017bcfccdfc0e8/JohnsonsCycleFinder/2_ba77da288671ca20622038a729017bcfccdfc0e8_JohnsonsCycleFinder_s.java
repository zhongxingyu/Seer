 package org.jgrapht.alg;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Stack;
 
 import org.jgrapht.DirectedGraph;
 import org.jgrapht.graph.DirectedMaskSubgraph;
 import org.jgrapht.graph.DirectedSubgraph;
 import org.jgrapht.graph.MaskFunctor;
 
 import tools.syncBlockStats.Field;
 import tools.syncBlockStats.StaticBlock;
 
 public class JohnsonsCycleFinder<V,E> {
 	DirectedGraph<V,E> graph;
 	List<V> vertices;
 	Map<V, Boolean> blocked;
 	Map<V, Set<V>> B;
 	Stack<V> stack;
 	
 	Set<V> cycleSet;
 	int numCycles = 0;
 	int numNodes;
 	
 	DirectedGraph<V, E> sub;
 	
 	public JohnsonsCycleFinder(DirectedGraph<V,E> graph){
 		this.graph = graph;
 		vertices = new ArrayList<V>();
 		vertices.addAll(graph.vertexSet());
 		cycleSet = new HashSet<V>();
 		
 		numNodes = vertices.size();
 		
 		this.blocked = new HashMap<V, Boolean>(numNodes);
 		this.B = new HashMap<V, Set<V>>(numNodes);
 		stack = new Stack<V>();
 		
 		findCycles();
 	}
 	
 
 	public void findCycles(){
 		stack.removeAllElements();
 		int s = 0;
 		sub = new DirectedSubgraph<V,E>(graph, graph.vertexSet(), graph.edgeSet());
 		
 		while(s < numNodes){	
 			StrongConnectivityInspector<V,E> scFinder = new StrongConnectivityInspector<V,E>(sub);
 			List<Set<V>> sccs = scFinder.stronglyConnectedSets();
 			
 			Set<V> sc = null;
 			int leastNode = s;
 			//find sc with least vertex
 			for(int i = s; i < numNodes; i++){
 				for(Set<V> scc : sccs){
 					//should be no self-loops so it's fine to check for size > 1
 					if(scc.size() > 1 && scc.contains(vertices.get(i))){
 						sc = scc;
 						leastNode = i;
 						i = numNodes;
 						break;
 					}
 				}
 			}
 			if(sc != null){
 				for(V v : sc){
 					blocked.put(v, false);
 					B.put(v, new HashSet<V>());
 				}
 				traverse(sc, vertices.get(leastNode), vertices.get(leastNode));
 				
 				for(int i = s; i <= leastNode; i++){
 					V v = vertices.get(i);
 					sub.removeVertex(v);
 					sub.removeAllEdges(graph.edgesOf(v));
 				}
 				s++;
 			}
 			else{
 				break;
 			}
 		}
 		
 	}
 	
 	public boolean traverse(Set<V> sc, V start, V curr){
 		boolean f = false;
 		stack.push(curr);
 		blocked.put(curr, true);
 		for(E e : sub.outgoingEdgesOf(curr)){
 			V adj = sub.getEdgeTarget(e);
 			if(!sc.contains(adj)) continue;
 			if(adj == start){
 				numCycles++;
 				cycleSet.addAll(stack);
 				f = true;
 			}
 			else if(!blocked.get(adj)){
 				if(traverse(sc, start, adj)){
 					f = true;
 				}
 			}
 		}
 		if(f){
 			unblock(curr);
 		}
 		else{
 			for(E e : sub.outgoingEdgesOf(curr)){
 				V adj = sub.getEdgeTarget(e);
				B.get(adj).add(curr);
 			}
 		}
 		stack.remove(curr);
 		return f;
 	}
 	
 	public void unblock(V u){
 		blocked.put(u, false);
 		for(V w : B.get(u)){
 			B.get(u).remove(w);
 			if(blocked.get(w)) unblock(w);
 		}
 	}
 	
 	//was using masksubgraph, now using directedsubgraph
 	public class LeastVertexSubgraphFilter implements MaskFunctor<V,E>{
 		int index;
 		public LeastVertexSubgraphFilter(int index){
 			this.index = index;
 		}
 		public boolean isEdgeMasked(E e){
 			return isVertexMasked(graph.getEdgeSource(e)) ||
 					isVertexMasked(graph.getEdgeTarget(e)); 
 		}
 		public boolean isVertexMasked(V v){
 			return vertices.indexOf(v) < index;
 		}
 		
 	}
 	
 	public int getCycleCount(){
 		return numCycles;
 	}
 	
 	public Set<V> getCycleSet(){
 		return cycleSet;
 	}
 }
