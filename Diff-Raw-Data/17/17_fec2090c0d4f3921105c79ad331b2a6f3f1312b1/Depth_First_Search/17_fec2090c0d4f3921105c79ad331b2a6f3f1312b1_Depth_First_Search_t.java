 package a1_p01_JS_MJ;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.jgrapht.Graphs;
 import org.jgrapht.graph.*;
 
 public class Depth_First_Search {
 	static int acc = 1;
 
	public static SearchResult searchShortestPath(AbstractBaseGraph<String, DefaultEdge> graph, String start, String end) {
 		
 		if (!graph.containsVertex(end))
 			return new SearchResult(graph, new ArrayList<String>(), new HashMap<String, Integer>(), acc);
 //			return new ArrayList<String>();
 		
 		List<String> open = new ArrayList<String>();
 		open.add(start);
 		if (start == end)
 			return new SearchResult(graph, open, new HashMap<String, Integer>(), acc);
 //			return open;
 
 		Map<String, Integer> closed = new HashMap<String, Integer>();
 		closed.put(start, 0);
 		dfs_rek1(open, closed, end, 0, graph);
 		
 		List<String> path = createPath(closed, end, graph);
 		return new SearchResult(graph, path, closed, acc);
 //		return createPath(closed, end, graph);
 	}
 	
 	private static List<String> createPath(Map<String, Integer> closed,
 			String end, AbstractBaseGraph<String, DefaultEdge> graph) {
 		List<String> ret = new ArrayList<String>();
 		if (closed.containsKey(end)) {
 			int count = closed.get(end);
 			String[] path = new String[count + 1];
 			path[count] = end;
 			for (count = closed.get(end); count > 0; --count) {
 				for (String s : Graphs.neighborListOf(graph, path[count])) {
 					int i = count;
 					acc++;
 					if (i > closed.get(s) && graph.containsEdge(s, path[count])) {// i=
 																					// closed.get(s)+1;
 						i = closed.get(s);
 						path[count - 1] = s;
 					}
 				}
				
			}
			
			for(int i=0;i<path.length;i++){
				ret.add(path[i]);
 			}
 		}
 		return ret;
 	}
 
 	private static Map<String, Integer> dfs_rek1(List<String> l,
 			Map<String, Integer> m, String end, int count,
 			AbstractBaseGraph<String, DefaultEdge> g) {
 
 		int i = count;
 		while (!l.isEmpty()) {
 			String s = l.remove(l.size() - 1);
 			/*
 			 * if (!m.containsKey(s) || i + 1 < m.get(s)){ i+=1; m.put(s, i);}
 			 */
 			acc++;
 			List<String> t = Graphs.neighborListOf(g, s);
 
 			for (String ss : t) {
 				acc++;
 				if ((!m.containsKey(ss) || m.get(ss) > (m.get(s) + 1))
 						&& g.containsEdge(s, ss)) {
 					m.put(ss, m.get(s) + 1);
 
 					if (!l.contains(ss))
 						l.add(ss);
 				}
 			}
 			dfs_rek1(l, m, end, i, g);
 		}
 
 		return null;
 	}
 }
