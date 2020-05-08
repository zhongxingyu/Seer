 package a1_p01_JS_MJ_test;
 
 import static org.junit.Assert.*;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 
 import org.jgrapht.graph.AbstractBaseGraph;
 import org.jgrapht.graph.DefaultEdge;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 import a1_p01_JS_MJ.Breadth_First_Search;
 import a1_p01_JS_MJ.Depth_First_Search;
 import a1_p01_JS_MJ.GraphParser;
 import a1_p01_JS_MJ.GraphvizAdapter;
 import a1_p01_JS_MJ.SearchResult;
 
 public class a1Test {
 
 	static AbstractBaseGraph<String, DefaultEdge> graph1;
 	static AbstractBaseGraph<String, DefaultEdge> graph2;
 	static AbstractBaseGraph<String, DefaultEdge> graphk5;
 	static Set<ArrayList<String>> Graph1_shortestPath;
 	static List<String> Graph2_shortestPath;
 	 
 	@BeforeClass
 	public static void init() {
 		
 		try {
 			graph1 = GraphParser.parseGraphFile("Graph1.gka");
 			graph2 = GraphParser.parseGraphFile("Graph2.gka");
 			graphk5 = GraphParser.parseGraphFile("GraphK5.gka");
 		} catch(IOException ex) {
 			ex.printStackTrace();
 		}
 		
 		ArrayList<String> graph1_shortestPath1 = new ArrayList<String>(){{
 			add("a");
 			add("c");
 			add("g");
 			add("e");
 			add("f");
 		}};
 		
 		ArrayList<String> graph1_shortestPath2 = new ArrayList<String>(){{
 			add("a");
 			add("k");
 			add("g");
 			add("e");
 			add("f");
 		}};
 		
 		ArrayList<String> graph1_shortestPath3 = new ArrayList<String>(){{
 			add("a");
 			add("c");
 			add("d");
 			add("e");
 			add("f");
 		}};
 		
 		Graph1_shortestPath = new HashSet<ArrayList<String>>();
 		Graph1_shortestPath.add(graph1_shortestPath1);
 		Graph1_shortestPath.add(graph1_shortestPath2);
 		Graph1_shortestPath.add(graph1_shortestPath3);
 		
 		Graph2_shortestPath = new ArrayList<String>(){{
 			add("a");
 			add("c");
 			add("f");
 		}};
 	}
 	
 	@Test
 	public void breadth_first_graph1_test() {
 		SearchResult result = Breadth_First_Search.searchShortestPath(graph1, "a", "f");
 
 		printTestResult("Test 1: Breath First - Graph 1", result);
 		
 		assertEquals(true, Graph1_shortestPath.contains(result.getPath()));
 		assertEquals(4, result.getPathLength());
 			
 		try {
 			GraphvizAdapter.buildDotFileWithSearchResult("breadth_first_graph1", result);
 			GraphvizAdapter.compileDotFile("breadth_first_graph1");
 		} catch(Exception ex){
 			fail("Could not draw Graph file.");
 		}
 		
 		System.out.println("Test Green.");
 		System.out.println("");
 	}
 	
 	@Test
 	public void breadth_first_graph2_test() {
 		SearchResult result = Breadth_First_Search.searchShortestPath(graph2, "a", "f");
 		
 		printTestResult("Test 2: Breath First - Graph 2", result);
 		
 		assertEquals(Graph2_shortestPath, result.getPath());
 		assertEquals(2, result.getPathLength());
 		
 		try {
 			GraphvizAdapter.buildDotFileWithSearchResult("breadth_first_graph2",result);
 			GraphvizAdapter.compileDotFile("breadth_first_graph2");
 		} catch(Exception ex){
 			fail("Could not draw Graph file.");
 		}
 		
 		System.out.println("Test Green.");
 		System.out.println("");
 	}
 	
 	@Test
 	public void depth_first_graph1_test() {
 		SearchResult result = Depth_First_Search.searchShortestPath(graph1, "a", "f");
 		
 		printTestResult("Test 3: Depth First - Graph 1", result);
 		
 		assertEquals(true, Graph1_shortestPath.contains(result.getPath()));
 		assertEquals(4, result.getPathLength());
 		
 		try {
 			GraphvizAdapter.buildDotFileWithSearchResult("depth_first_graph1", result);
 			GraphvizAdapter.compileDotFile("depth_first_graph1");
 		} catch(Exception ex){
 			fail("Could not draw Graph file.");
 		}
 	}
 	
 	@Test
 	public void deapth_first_graph2_test() {
 		SearchResult result = Depth_First_Search.searchShortestPath(graph2, "a", "f");
 		
 		printTestResult("Test 4: Depth First - Graph 2", result);
 		
 		assertEquals(Graph2_shortestPath, result.getPath());
 		assertEquals(2, result.getPathLength());
 		
 		try {
 			GraphvizAdapter.buildDotFileWithSearchResult("depth_first_graph2", result);
 			GraphvizAdapter.compileDotFile("depth_first_graph2");
 		} catch(Exception ex){
 			fail("Could not draw Graph file.");
 		}
 		
 		System.out.println("Test Green.");
 		System.out.println("");
 	}
 
 	
 	@Test
 	public void deapth_first_graphK5_test() {
 		Set<String> vertexes = graphk5.vertexSet();
 		
 		assertEquals(5, vertexes.size());
 		
 		for(String v1 : vertexes){
 			for(String v2 : vertexes){
 				if(v1 != v2) assertEquals(1, Depth_First_Search.searchShortestPath(graphk5, v1, v2).getPathLength());
 			}
 		}	
 	}
 	
 	public void printTestResult(String testname, SearchResult result){
 		System.out.println(testname);
 		System.out.print("Path: ");
 		for(String s : result.getPath()){
 			System.out.print(s+ ", ");
 		}
 		System.out.println("");
 		System.out.println("Path Length " + result.getPathLength());
 		System.out.println("Accssess Counter: " + result.getAccsessCounter());
 	}
 }
