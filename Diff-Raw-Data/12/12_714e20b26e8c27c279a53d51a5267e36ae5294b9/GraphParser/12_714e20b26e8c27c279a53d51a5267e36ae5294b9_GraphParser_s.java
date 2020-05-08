 package a1_p01_JS_MJ;
 
 import java.io.*;
 import java.util.Scanner;
 
 import org.jgrapht.Graph;
 import org.jgrapht.Graphs;
 import org.jgrapht.WeightedGraph;
 import org.jgrapht.graph.*;
 
 import a2_p01_JS_MJ.AttributedNode;
 
 public class GraphParser {
 	
 	public static Graph parseGraphFile(String filename) throws Exception {
 		File file = new File(filename);
 		Scanner scanner = new Scanner(new FileReader(file));
 	    try {    	
 	    	String firstLine = scanner.nextLine();
 	    	String secondLine = scanner.nextLine();
 	    	  	
 	    	int graphType = 0;
 	    	
 	    	// Erste Zeile Parsen
 	    	if(firstLine.equalsIgnoreCase("#gerichtet")){
 	    		graphType += 1;
 	    		
 	    	} else if(firstLine.equalsIgnoreCase("#ungerichtet")){
 	    		graphType += 11;
 	    		
 	    	} else {
 	    		throw new IOException("Falscher Header in erster Zeile");
 	    	}
 	    	
 	    	// Zweite Zeile Parsen
 	    	if(secondLine.equalsIgnoreCase("#attributiert")){
 	    		graphType += 1;
 	    		
	    	} else if(secondLine.equalsIgnoreCase("#gewichted")){
 	    		graphType += 2;
 	    		
 	    	} else if(secondLine.equalsIgnoreCase("#attributiert,gewichted")){
 	    		graphType += 3;
 	    	}
 	    	
 	    	switch (graphType) {
 	            case 1: // nur Gerichtet
 	            	return parseDircetedGraph(scanner);
 	            	
 	            case 2: // Gerichtet und Attributiert
 	            	throw new Exception("Not yet implemented");
 	            	
 	            case 3: // Gerichtet und Gewichtet
 	            	throw new Exception("Not yet implemented");
 	            	
 	            case 4: // Gerichtet und Gewichtet und Attributiert
 	            	throw new Exception("Not yet implemented");
 	            	
 	            	
 	            case 11: // nur Ungerichtet
 	            	return parseUndircetedGraph(scanner);
 	            	
 	            case 12: // Ungerichtet und Attributiert
 	            	throw new Exception("Not yet implemented");
 	            	
 	            case 13: // Ungerichtet und Gewichtet
 	            	return parseWeightedGraph(scanner);
 	            	
 	            case 14: // Ungerichtet und Gewichtet und Attributiert
 	            	return parseWeightedAttributedGraph(scanner);
 	            	
 	            default:
 	            	throw new Exception("Falscher Header");
 	    	}
 	    	
 	    } finally {
 	    	scanner.close();
 	    }
 	}
 	
 	private static Pseudograph<String, DefaultEdge> parseUndircetedGraph(Scanner scanner){
 		Pseudograph<String, DefaultEdge> graph = 
 				new Pseudograph<String, DefaultEdge>(DefaultEdge.class);
 		
 		String[] line; 
     	while (scanner.hasNextLine()){
     		line = splitLine(scanner.nextLine());
     		graph.addVertex(line[0]);
     		graph.addVertex(line[1]);
     		graph.addEdge(line[0], line[1]);
 	    }
     	
     	return graph;
 	}
 	
 	private static DirectedMultigraph<String, DefaultEdge> parseDircetedGraph(Scanner scanner){
 		DirectedMultigraph<String, DefaultEdge> graph = 
 				new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
 		
 		String[] line; 
     	while (scanner.hasNextLine()){
     		line = splitLine(scanner.nextLine());
     		graph.addVertex(line[0]);
     		graph.addVertex(line[1]);
     		graph.addEdge(line[0], line[1]);
 	    }
     	
     	return graph;
 	}
 
     public static WeightedGraph<String, DefaultWeightedEdge> parseWeightedGraph(Scanner scanner)
     {
         WeightedGraph<String, DefaultWeightedEdge> graph = new WeightedPseudograph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
         String[] line;
         while(scanner.hasNextLine())
         {
             line = splitLine(scanner.nextLine());
            Graphs.addEdgeWithVertices(graph,line[0],line[2]);
            graph.setEdgeWeight(graph.getEdge(line[0],line[2]),Double.parseDouble(line[4]));
         }
         return graph;
     }
 
 
 
     public static WeightedGraph<AttributedNode<String>,DefaultWeightedEdge> parseWeightedAttributedGraph(Scanner scanner)
     {
         WeightedGraph<AttributedNode<String>,DefaultWeightedEdge> graph = new WeightedPseudograph<AttributedNode<String>, DefaultWeightedEdge>(DefaultWeightedEdge.class);
         String[] line;
         while (scanner.hasNextLine())
         {
             line = splitLine(scanner.nextLine());
             AttributedNode<String> n1 =  new AttributedNode<String>(line[0],Double.parseDouble(line[1]));
             AttributedNode<String> n2 =  new AttributedNode<String>(line[2],Double.parseDouble(line[3]));
             Graphs.addEdgeWithVertices(graph,n1,n2);
             graph.setEdgeWeight(graph.getEdge(n1,n2),Double.parseDouble(line[4]));
         }
         return graph;
     }
 
 	private static String[] splitLine(String line){
 		return line.split(",");
 	}
 }
