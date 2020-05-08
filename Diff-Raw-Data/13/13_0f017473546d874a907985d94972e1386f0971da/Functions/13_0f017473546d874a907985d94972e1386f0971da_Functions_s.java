 package green;
 
 import java.awt.Color;
 import java.util.ArrayList;
 
 import structures.*;
 
 public class Functions {
 	public AdjListGraph<Integer, Integer> colorGraph(AdjListGraph<Integer, Integer> graph){
 		ArrayList<Integer> colors = new ArrayList<Integer>();
 		ArrayList<Integer> available;
 		colors.add(0);
 		
 		for(Vertex v:(Iterable<Vertex<Integer>>)graph.vertices()){
 			available = new ArrayList<Integer>();
 			available.addAll(colors);
 			for(Edge e:(Iterable<Edge<Integer>>)v.getIncidentEdges()){
				int element = (int)graph.opposite(v, e).element();
 				if(available.contains(element))
 					available.remove(available.indexOf(element));
 			}
 			
 			if(available.size()==0){
 				colors.add(colors.size());
 				available.add(colors.size()-1);
 			}
 			
 			v.setElement(available.get(0));
 		
 		}
 		
 		graph.attributes.put("colors", colors.size());
 		graph.attributes.put("bipartite", colors.size()==2?true:false);
 		
 		return graph;
 	}
 	
 	public AdjListGraph<Integer, Integer> findPath(AdjListGraph<Integer, Integer> graph, Vertex<Integer> start, Vertex<Integer> end){
 		for(Edge e:graph.edges()){
 			e.put("discovered", null);
 			e.put("color", Color.BLACK);
 		}
 		for(Vertex v:graph.vertices())
 			v.put("distance", null);
 		ArrayList<Vertex<Integer>> queue = new ArrayList<Vertex<Integer>>();
 		queue.add(start);
 		start.put("distance", 0);
 		while(!queue.isEmpty()){
 			Vertex current = queue.get(0);
 			if(current == end) return buildPath(graph, start, end);
 			queue.remove(0);
 			for(Edge e:(Iterable<Edge<Integer>>)current.getIncidentEdges()){
 				if(e.get("discovered")==null){
 					Vertex<Integer> v = graph.opposite(current, e);
					if(v.get("distance")==null) v.put("distance", ((int)current.get("distance"))+1);
					else if(((int)v.get("distance"))>((int)current.get("distance"))+1) v.put("distance", ((int)current.get("distance"))+1);
 					e.put("discovered", true);
 					queue.add(v);
 				}
 			}
 		}
 		
 		return graph;
 	}
 	
 	public AdjListGraph<Integer, Integer> buildPath(AdjListGraph<Integer,Integer> graph, Vertex<Integer> start, Vertex<Integer> end){
 		Vertex<Integer> current = end;
 		while(current != start){
 			int min =Integer.MAX_VALUE;
 			Vertex<Integer> minVert = null;
 			Edge<Integer> minEdge = null;
 			for(Edge e:(Iterable<Edge>)current.getIncidentEdges()){
 				Vertex<Integer> adj = graph.opposite(current, e);
				if(adj.get("distance")!=null&&((int)adj.get("distance"))<min){
 					minVert = adj;
 					minEdge = e;
					min = (int)adj.get("distance");
 				}
 			}
 			minEdge.put("color", Color.YELLOW);
 			current = minVert;
 		}
 		
 		return graph;
 	}
 }
