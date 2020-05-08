 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.LinkedList;
 import java.util.Set;
 
 
 import org.jgraph.graph.DefaultEdge;
 import org.jgrapht.alg.ConnectivityInspector;
 import org.jgrapht.graph.SimpleDirectedGraph;
 
 import algorithms.AlgorithmsDominators;
 
 import parser.ProgramParser;
 
 import structures.Edge;
 import structures.OwnEdgeFactory;
 import structures.Vertex;
 
 public class Main {
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws IOException, InterruptedException {		
 		boolean macOS = false;
 		if (args.length == 2 ){
 			if (args[1].equals("-macOS"))
 				macOS = true;
 		}else{
 			System.out.println("use with 2 params for Mac OS: <file path> <-macOS>");
 		}
 			
 		File inputScenarioFile = new File(args[0]);
 		FileReader fr;
 		ProgramParser scenarioParser;
 		SimpleDirectedGraph<Vertex,Edge> graph;
 		StringBuffer dotFile;
 		try {
 			OwnEdgeFactory<Vertex,Edge> f = new OwnEdgeFactory<Vertex,Edge>(Edge.class);
 			graph = new SimpleDirectedGraph<Vertex,Edge>(f);
 			fr = new FileReader(inputScenarioFile);
 			scenarioParser = new ProgramParser(fr);
 			ProgramParser.init(fr);
 			
 			// Parse program and write CFG in dot file
 			dotFile = new StringBuffer("digraph name {\n");
 			scenarioParser.parseProgram(graph,dotFile);
 			dotFile.append("}\n");
 			/* Write output file*/
 			FileWriter fileWriter;
 			try {
 				fileWriter = new FileWriter("graph.txt");
 				fileWriter.write(dotFile.toString());
 				fileWriter.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			// Store edges set for compute CDG
 			Set<Edge> edges = graph.edgeSet();
 			System.out.println("Edges : ");
 			for (Edge e : edges){
 				System.out.println(e);
 			}
 						
 			// Print graph
 			System.out.println("\n	Graph : \n");
 			System.out.println("Vertex : "+graph.vertexSet().toString());
 			System.out.println("Edges : "+edges);
 			System.out.println(graph.toString());
 			
 			// Run dot program to generate image and show it
 			Process p;
 			if (!macOS){
 				p = Runtime.getRuntime().exec("/usr/bin/dot -T jpg -o graph.jpg graph.txt");
 			}else{// in Mac OS
 				p = Runtime.getRuntime().exec("/opt/local/bin/dot -T jpg -o graph.jpg graph.txt");
 			}
 	    	InputStream in = p.getInputStream();
 	    	InputStreamReader inread = new InputStreamReader(in);
 	    	BufferedReader bufferedreader = new BufferedReader(inread);
 	    	// Check for failure
 			if (p.waitFor() != 0) {
 				System.out.println("exit value = " + p.exitValue());
 			}
 			System.out.println("outpud dot program : "+bufferedreader.readLine());
 			if (!macOS){
 				p = Runtime.getRuntime().exec("shotwell graph.jpg");
 			}else{
 				p = Runtime.getRuntime().exec("open graph.jpg");
 			}
 			
 			// Compute Dominators and Post Dominators and show
 			// Compute Dominator
 			AlgorithmsDominators algorithmsDominator = new AlgorithmsDominators ();
 			algorithmsDominator.computeDominators(graph);
 			System.out.println("DOMINADORES:");
 			for (Vertex v : graph.vertexSet()) {
 				System.out.println("Dom("+v.toString()+")="+v.getDominators());
 			}
 			// Compute Post-Dominator
 			SimpleDirectedGraph<Vertex,Edge> reverseGraph = algorithmsDominator.reverse(graph);
 			algorithmsDominator.computeDominators(reverseGraph);
 			System.out.println("POST-DOMINADORES:");
 			for (Vertex v : reverseGraph.vertexSet()) {
 				System.out.println("Dom("+v.toString()+")="+v.getDominators());
 			}			
 			System.out.println("DOMINADORES:");
 			for (Vertex v : graph.vertexSet()) {
 				System.out.println("Dom("+v.toString()+")="+v.getDominators());
 			}
 			
 			System.out.println("Inmediate POST-DOMINADORES:");
 			algorithmsDominator.computeidominator(reverseGraph);
 			for (Vertex v : reverseGraph.vertexSet()) {
 				System.out.println("iPostDom("+v.toString()+")="+v.getiDominators());
 			}
 			
 			// Compute Post Dominators Tree and write dot file
 			SimpleDirectedGraph<Vertex,Edge> dominatorsTree = algorithmsDominator.computeDominatorsTree(reverseGraph);
 			StringBuffer treeFile = algorithmsDominator.getOutputDot();
 			try {
 				fileWriter = new FileWriter("tree.txt");
 				fileWriter.write(treeFile.toString());
 				fileWriter.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			// Run dot program to generate image and show it
 			/*if (!macOS){
 				p = Runtime.getRuntime().exec("/usr/bin/dot -T jpg -o tree.jpg tree.txt");
 			}else{
 				p = Runtime.getRuntime().exec("/opt/local/bin/dot -T jpg -o tree.jpg tree.txt");
 			}*/
 	    	in = p.getInputStream();
 	    	inread = new InputStreamReader(in);
 	    	bufferedreader = new BufferedReader(inread);
 	    	// Check for failure
 			if (p.waitFor() != 0) {
 				System.out.println("exit value = " + p.exitValue());
 			}
 			System.out.println("outpud dot program : "+bufferedreader.readLine());
 			/*if (!macOS){
 				p = Runtime.getRuntime().exec("shotwell tree.jpg");
 			}
 			else{
 				p = Runtime.getRuntime().exec("open tree.jpg");
 			}*/
 			
 			// step a: compute set S
 			LinkedList<Edge<Vertex>> S = algorithmsDominator.edgesNotAncestralsInTree(graph, dominatorsTree);
 			
 			SimpleDirectedGraph<Vertex,Edge> cdg = algorithmsDominator.computeControlDependenceGraph(graph, dominatorsTree);
 			StringBuffer cdgFile = algorithmsDominator.getOutputDot();
 			try {
 				fileWriter = new FileWriter("cdg.txt");
 				fileWriter.write(cdgFile.toString());
 				fileWriter.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			
 			algorithmsDominator.computeReachigDefinitios(graph);
 			
 			
 			
 			// Run dot program to generate image and show it
 			/*if(!macOS){
 				p = Runtime.getRuntime().exec("/usr/bin/dot -T jpg -o cdg.jpg cdg.txt");
 			}else{
 				p = Runtime.getRuntime().exec("/opt/local/bin/dot -T jpg -o cdg.jpg cdg.txt");
 			}
 			if (!macOS){
 				p = Runtime.getRuntime().exec("shotwell cdg.jpg");
 			}
 			else{
 				p = Runtime.getRuntime().exec("open cdg.jpg");
 			}*/
 			
 			
 			
 			algorithmsDominator.computeAvailableExpressions(graph);
 			
 			algorithmsDominator.showAvailableExpressions(graph);
 			
 			algorithmsDominator.showReachingDefinitions(graph);
 			
 			algorithmsDominator.computeDataDependenceGraph(graph);
 			
 		} catch (FileNotFoundException e1) {
 			e1.printStackTrace();
 		} catch (parser.ParseException e) {
 			e.printStackTrace();
 			System.out.println("error in parsing program !!!");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		
 		
 
 	}
 
 }
