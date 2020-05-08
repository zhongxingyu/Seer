 
 package dbs.search.graph;
 
 import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.io.GraphMLFile;
 import edu.uci.ics.jung.graph.Edge;
 import edu.uci.ics.jung.graph.Vertex;
 import edu.uci.ics.jung.utils.Pair;
 import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.List;
 import java.util.StringTokenizer;
 
 public class GraphUtil{
 	Graph g;
 	private static GraphUtil ref ;
 	public static synchronized GraphUtil getInstance(String fileName){
 		if (ref == null) ref = new GraphUtil(fileName);
 		return ref;
 	}
 								                                                
 	private GraphUtil(Graph g){
 		this.g = g;
 	}
 	private GraphUtil(){}
 	
 	private GraphUtil(String fileName){	
 		g = (new GraphMLFile()).load(fileName);
 	}
 
 	private Graph loadGraph(String fileName){	
 		g = (new GraphMLFile()).load(fileName);
 		return g;
 	}
 	public Graph getGraph(){
 		return g;
 	}
 		
 	
 	public Vertex getVertex(String vName){
 		Set s = g.getVertices();
 		for (Iterator eIt = s.iterator(); eIt.hasNext(); ) {
 			Vertex v = (Vertex) eIt.next();
 			String vNameFromGraph =  (String) v.getUserDatum("real");
 			//if (vName.equals(vNameFromGraph)) return v;
 			if (isEqual(vName, vNameFromGraph)) return v;
 		}
 		return null;
 	}
 
 	public String getRealFromVertex(Vertex v){
 		return (String) v.getUserDatum("real");
 	}
 	public String getDefaultFromVertex(Vertex v){
 		return (String) v.getUserDatum("default");
 	}
	public String getMappedFromVertex(Vertex v){
		return (String) v.getUserDatum("mapped");
	}
 
 	public String getMappedRealName(String userName){
 		return getRealFromVertex(getMappedVertex(userName));
 	}
 	
 	public Vertex getMappedVertex(String userName){
 		Set s = g.getVertices();
 		for (Iterator eIt = s.iterator(); eIt.hasNext(); ) {
 			Vertex v = (Vertex) eIt.next();
 			String vNameFromGraph =  (String) v.getUserDatum("user");
 			//System.out.println("comparing " + userName+ "  " + vNameFromGraph);
 			if(isEqual(userName, vNameFromGraph)) return v;
 			//if (userName.equals(vNameFromGraph)) return v;
 		}
 		return null;
 	}
 	private boolean isEqual(String userName, String vNameFromGraph) {
 		StringTokenizer st = new StringTokenizer(vNameFromGraph, ",");
 		while(st.hasMoreTokens()) 
 			if (userName.trim().equals(st.nextToken().trim())) return true;
 		return false;
 
 	}
 
 	public List<Edge> getShortestPath( Vertex v1, Vertex v2){
 		return  (new DijkstraShortestPath(g, new MyWeightValue("wt")).getPath(v1,v2));
 	}
 	public List<Edge> getShortestPath(String v1, String v2){
 		return getShortestPath(getVertex(v1), getVertex(v2));
 	}
 	public String getRealtionFromEdge(Edge e) {
 		return (String)e.getUserDatum("relation");
 	}
 
 	public String getRealtionFromVertex(String vStr1, String vStr2) {
 		return getRealtionFromEdge(getEdgeBetweenVertex(vStr1, vStr2));
 	}
 	
 	public boolean doesEdgeExist(String vStr1, String vStr2) {
 		for (Iterator eIt = g.getEdges().iterator(); eIt.hasNext(); ) {
 			Pair p = ((Edge) eIt.next()).getEndpoints();
 			String v1FromGraphStr = getRealFromVertex((Vertex) p.getFirst());
 			String v2FromGraphStr = getRealFromVertex((Vertex) p.getSecond());
 			if( (vStr1.equals(v1FromGraphStr) && vStr2.equals(v2FromGraphStr)) 
 					|| (vStr1.equals(v2FromGraphStr) && vStr2.equals(v1FromGraphStr))
 					)  return true;
 		}
 		return false;
 	}
 
 	public Edge getEdgeBetweenVertex(String vStr1, String vStr2) {
 		for (Iterator eIt = g.getEdges().iterator(); eIt.hasNext(); ) {
 			Edge e = (Edge) eIt.next();
 			Pair p = e.getEndpoints();
 			String v1FromGraphStr = getRealFromVertex((Vertex) p.getFirst());
 			String v2FromGraphStr = getRealFromVertex((Vertex) p.getSecond());
 			if( (vStr1.equals(v1FromGraphStr) && vStr2.equals(v2FromGraphStr)) 
 					|| (vStr1.equals(v2FromGraphStr) && vStr2.equals(v1FromGraphStr))
 					)  return e;
 		}
 		return null;
 	}
 
 
 	public String getFirstNameFromEdge(Edge e) {
 		return getRealFromVertex((Vertex) e.getEndpoints().getFirst());
 	}
 
 	public String getSecondNameFromEdge(Edge e) {
 		return getRealFromVertex((Vertex) e.getEndpoints().getSecond());
 	}
 
 }
