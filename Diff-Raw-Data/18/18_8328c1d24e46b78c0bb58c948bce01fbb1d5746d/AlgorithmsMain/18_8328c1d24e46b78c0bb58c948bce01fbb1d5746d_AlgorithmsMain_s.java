 package com.max.algs;
 
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import com.max.algs.graph.Graph;
 
 
 
 
 
 
 public final class AlgorithmsMain {	
 	
 	
 
 	private AlgorithmsMain() throws Exception {		
 		
 		
 		Graph<String> graph = Graph.createGraph();
 		
 		graph.addVertex("x1");
 		graph.addVertex("x2");
 		graph.addVertex("x3");
 		graph.addVertex("x4");
 		graph.addVertex("x5");
 		
 		graph.addEdge("x1", "x2");
 		graph.addEdge("x1", "x3");
 		
 		graph.addEdge("x2", "x3");
 		graph.addEdge("x2", "x4");
 		
 		graph.addEdge("x3", "x4");
 		
 		graph.addEdge("x4", "x5");
 		
 		System.out.println( graph );
 		
 		
 		Set<Integer> differentColors = new HashSet<>();
 		
 		Map<String, Integer> coloredVertexes = graph.colorGraph();		
 		System.out.println( coloredVertexes );
 		
 		
 		for( int color : coloredVertexes.values() ){
 			differentColors.add( color );
 		}
 		
 		System.out.println( "Colors count: " + differentColors.size() );
     
 		System.out.println( "Main done" );		
 	}	
 	
 
 	
 
 	
 
 	public static void main(String[] args) {
 		try {			
 			new AlgorithmsMain();
 		}
 		catch( Exception ex ){
 			ex.printStackTrace();
 		}
 	}
 		
 
 }
