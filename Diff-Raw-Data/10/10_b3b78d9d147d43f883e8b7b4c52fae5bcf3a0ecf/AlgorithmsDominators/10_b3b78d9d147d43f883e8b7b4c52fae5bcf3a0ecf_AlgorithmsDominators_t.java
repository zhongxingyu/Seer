 package algorithms;
 
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 
 import org.jgrapht.graph.SimpleDirectedGraph;
 
 import structures.Edge;
 import structures.OwnEdgeFactory;
 import structures.Vertex;
 import structures.VertexType;
 
 public class AlgorithmsDominators {
 	
 	public AlgorithmsDominators() {
 	}
 
 	private LinkedList<Vertex> getPredecessors(Vertex v, SimpleDirectedGraph<Vertex,Edge> graph){
 		LinkedList<Vertex> result = new LinkedList<Vertex>();
 		Set<Edge> edgeSet = graph.incomingEdgesOf(v);
 		
 		for (Edge<Vertex> e : edgeSet){
 			result.add(graph.getEdgeSource(e));
 		}
 		return result;
 		
 	}
 	
 	public void computeDominators(SimpleDirectedGraph<Vertex,Edge> graph){
 		LinkedList<Vertex> vertexList = new LinkedList<Vertex>();
 		Set<Vertex> vertexs = graph.vertexSet();
 		Iterator<Vertex> iterator = vertexs.iterator();
 		
 		while(iterator.hasNext()){
 			vertexList.add(iterator.next());
 		}
 		
 		for (Vertex v : vertexs) {
 			if (!v.isBegin()){
 				v.setDominators((LinkedList<Vertex>)vertexList.clone());
 			}else{
 				LinkedList<Vertex> l = new LinkedList<Vertex>();
 				l.add(v);
 				v.setDominators(l);
 			}
 		}
 		Boolean done = false;
 		while (!done){
 			done = true;
 			for (Vertex v : vertexs){
 				int length = v.getDominators().size();
 				if(!v.isBegin()){
 					LinkedList<Vertex> newDom = v.getDominators();
 					LinkedList<Vertex> pre = getPredecessors(v,graph);
 					for (Vertex p : pre){
 						newDom = intersection(newDom, p.getDominators());
 					}
 					v.setDominators(newDom);
 					v.addDominators(v);
 				}
 				if(length != v.getDominators().size()){
 					done = false;
 				}
 			}
 		}
 	}
 	
 	private LinkedList<Vertex> intersection(LinkedList<Vertex> newDom,LinkedList<Vertex> otherDoms){
 		LinkedList<Vertex> result = new LinkedList<Vertex>() ;
 		for (Vertex v : newDom){
 			if(otherDoms.contains(v)){
 				result.add(v);
 			}
 		}
 		return result;
 	}
 	
 	public SimpleDirectedGraph<Vertex, Edge> reverse(SimpleDirectedGraph<Vertex, Edge> graph){
 		OwnEdgeFactory<Vertex,Edge> f = new OwnEdgeFactory<Vertex,Edge>(Edge.class);
 		SimpleDirectedGraph<Vertex, Edge> reverseGraph = new SimpleDirectedGraph<Vertex, Edge>(f);
 		
		
		
 		for (Vertex v : graph.vertexSet()){
 			if(v.isBegin()){
 				v.setType(VertexType.END);
 			}else{
 				if(v.isEnd()){
 					v.setType(VertexType.BEGIN);
 				}
 			}
 			reverseGraph.addVertex(v);
 		}
 		System.out.println("finished add vertexs");
 		
 		Set<Edge> set = graph.edgeSet();
 		Vertex source;
 		Vertex target;
 		System.out.println(" "+set);
 		for (Edge<Vertex> e : set){
 			System.out.println("edge : "+e);
 			source = graph.getEdgeTarget(e);
 			System.out.println("edge : "+source);
 			target = graph.getEdgeSource(e);
 			System.out.println("edge : "+target);
 			reverseGraph.addEdge(source,target);
 		}
 		System.out.println("reverse graph "+reverseGraph.edgeSet());
 		return reverseGraph;
 		
 	}
 	
 	
 }
