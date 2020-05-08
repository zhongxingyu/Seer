 package pl.edu.uj.ii.goofy.algorithm.testpaths;
 
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Stack;
 
 import pl.edu.uj.ii.goofy.MultiMap;
 import edu.uci.ics.jung.graph.Graph;
 import edu.uci.ics.jung.graph.util.Pair;
 
 
 
 public class TestPathGenerator<N,E> {
 	public TestPathGenerator(Graph<N, E> graph, List<N> start, List<N> end, Touring touring) {
 		this.graph = graph;
 		this.startNodes = start;
 		this.endNodes = end;
 		this.allowedLength = graph.getVertexCount() * 3;
 		this.touring = touring;
 		this.pathReq = new MultiMap<LinkedList<N>, List<N>>();
 		paths = new LinkedList<LinkedList<N>>();
 	}
 	
 	public LinkedList<LinkedList<N>> getAllPaths() {
 		for (N startNode : startNodes) {
 			LinkedList<N> startPath = new LinkedList<N>();
 			startPath.add(startNode);
 			
			if (touring == Touring.Sidetrips || touring == Touring.SidetripsAndDetours) {
				findCyclePaths(startPath);
			} else {
				findSimplePaths(startPath);
			}
 		}
 		
 		return paths;
 	}
 	
 	public MultiMap<LinkedList<N>, List<N>> reducePaths(List<List<N>> requirements) {
 		MultiMap<LinkedList<N>, List<N>> prePathReq = new MultiMap<LinkedList<N>, List<N>>();
 		
 		for (List<N> requirement : requirements) {
 			for (LinkedList<N> path : paths) {
 				if (isCoverRequirement(path, requirement)) {
 					prePathReq.put(path, requirement);
 				}
 			}
 		}
 	
 		MultiMap<Collection<List<N>>, LinkedList<N>> dict = new MultiMap<Collection<List<N>>, LinkedList<N>>();
 		
 		for (LinkedList<N> key : prePathReq.keySet()) {
 			dict.put(prePathReq.getValues(key), key);
 		}
 		
 		prePathReq = new MultiMap<LinkedList<N>, List<N>>();
 		
 		for (Collection<List<N>> key : dict.keySet()) { 
 			LinkedList<N> shortest = null;
 			
 			for (LinkedList<N> path : dict.getValues(key)) {
 				if (shortest == null || path.size() < shortest.size()) {
 					shortest = path;
 				}
 			}
 			
 			prePathReq.put(shortest, key);
 		}
 		
 		for (LinkedList<N> key : prePathReq.keySet()) { 
 			Collection<List<N>> values = prePathReq.getValues(key);
 			boolean contains = false;
 			for (LinkedList<N> key2 : prePathReq.keySet()) { 
 				if (key != key2) {
 					Collection<List<N>> values2 = prePathReq.getValues(key2);
 					if (values2.containsAll(values)) {
 						contains = true;
 					}
 				}
 			}
 			
 			if (!contains) {
 				pathReq.put(key, values);
 			}
 		}
 		
 		return pathReq;
 	}
 	
 	private boolean isCoverRequirement(LinkedList<N> path, List<N> requirement) {
 		if (touring == Touring.OnlyTouring) {
 			return isSubPath(path, requirement);
 		}
 		
 		return longestCommonSubsequence(path, requirement);
 	}
 	
 	private boolean isSubPath(List<N> p1, List<N> p2) {
 		if (p1.size() < p2.size()) {
 			return false;
 		}
 		
 		for (int i = 0; i < p1.size() - p2.size() + 1; ++i) {
 			if (p1.subList(i, i + p2.size()).equals(p2)) {
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private boolean longestCommonSubsequence(List<N> path, List<N> requirement) {
 		N[] pathArray = (N[]) path.toArray();
 		N[] requArray = (N[]) requirement.toArray();
 		LinkedList<Integer> sequence = new LinkedList<Integer>();
 				
 		int C[][] = new int[pathArray.length + 1][requArray.length + 1];
 		
 		for (int i = 0; i < pathArray.length; ++i) {
 			for (int j = 0; j < requArray.length; ++j) {
 				if (pathArray[i].equals(requArray[j])) {
 					C[i + 1][j + 1] = C[i][j] + 1;
 				} else {
 					C[i + 1][j + 1] = Math.max(C[i][j + 1], C[i + 1][j]);
 				}
 			}
 		}
 		
 		return C[pathArray.length][requArray.length] == requirement.size(); 
 	}
 	
 	private void findSimplePaths(LinkedList<N> begin) {
 
 		for (N succ : graph.getSuccessors(begin.getLast())) {
 			if (!begin.contains(succ) || endNodes.contains(succ)) {
 				LinkedList<N> newPath = new LinkedList<N>(begin);
 				newPath.add(succ);
 				findSimplePaths(newPath);
 			}
 		}
 		
 		if (endNodes.contains(begin.getLast())) {
 			paths.add(begin);
 		}
 	}
 	
 	private void findCyclePaths(LinkedList<N> begin) {
 
 		if (begin.size() < allowedLength) {
 			for (N succ : graph.getSuccessors(begin.getLast())) {
 					LinkedList<N> newPath = new LinkedList<N>(begin);
 					newPath.add(succ);
 					findCyclePaths(newPath);
 			}
 		}
 		
 		if (endNodes.contains(begin.getLast())) {
 			paths.add(begin);
 		}
 	}
 	
 //	public int howManyCycles(LinkedList<N> path, N last) {
 //		
 //		Iterator<N> it;
 //		int length = 0;
 //		for (it = path.descendingIterator(); it.hasNext(); ) {
 //			N actNode = it.next();
 //			
 //			if (actNode == last) {
 //				++length;
 //				break;
 //			}
 //			
 //			++length;
 //		}
 //		
 //		if (length == 0) {
 //			return 0;
 //		}
 //		
 //		int actLength = 1;
 //		int count = 0;
 //		Iterator<N> itEnd = path.descendingIterator();
 //		
 //		while (it.hasNext()) {
 //			N fromEnd = itEnd.next();
 //			N mid = it.next();
 //			
 //			if (actLength == length) {
 //				++count;
 //				actLength = 0;
 //			}
 //			
 //			if (fromEnd == mid) {
 //				++actLength;
 //			} else {
 //				break;
 //			}
 //		}
 //		
 //		if (actLength == length) {
 //			++count;
 //		}
 //		
 //		return count;
 //	}
 	
 	private LinkedList<LinkedList<N>> paths;
 	private MultiMap<LinkedList<N>, List<N>> pathReq;
 	private Graph<N, E> graph;
 	private List<N> startNodes;
 	private List<N> endNodes;
 	private int allowedLength;
 	private Touring touring;
 }
