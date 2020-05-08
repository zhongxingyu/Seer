 package bio;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.Stack;
 
 import org.jgrapht.alg.ConnectivityInspector;
 import org.jgrapht.graph.DefaultEdge;
 import org.jgrapht.graph.DirectedMultigraph;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Iterables;
 
 public class Graph {
 
 	private DirectedMultigraph<String, DefaultEdge> bioGraph;
 
 	public Graph(String... input) throws WrongOligonucleotidesLengthException {
 		bioGraph = new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
 
 		validateLength(input);
 		buildGraph(input);
 	}
 
 	public Graph(Graph graph) {
 		bioGraph = new DirectedMultigraph<String, DefaultEdge>(DefaultEdge.class);
 		for(String v : graph.vertexSet()) {
 			bioGraph.addVertex(new String(v));
 		}
 		for(DefaultEdge e : graph.edgeSet()) {
 			bioGraph.addEdge(graph.getEdgeSource(e), graph.getEdgeTarget(e));
 		}
 	}
 
 	private void buildGraph(String[] input) {
 		for(String v : generateVerices(input[0].length() - 1)) {
 			bioGraph.addVertex(v);
 		}
 		for(String v : input) {
 			String post = v.substring(1);
 			String pre = v.substring(0, v.length() - 1);
 			// bioGraph.addVertex(pre);
 			// bioGraph.addVertex(post);
 			bioGraph.addEdge(pre, post);
 		}
 	}
 
 	public static List<String> generateVerices(int length) {
 		if(length == 0) {
 			return Arrays.asList("");
 		}
 		ArrayList<String> ret = new ArrayList<String>();
 		for(String letter : Arrays.asList("A", "C", "T", "G")) {
 			List<String> previous = generateVerices(length - 1);
 			for(String pre : previous) {
 				ret.add(letter + pre);
 			}
 		}
 		return ret;
 	}
 
 	private void validateLength(String[] input) throws WrongOligonucleotidesLengthException {
 		int k = input[0].length();
 		if(k < 2) {
 			throw new WrongOligonucleotidesLengthException("Oligonucleotides too short");
 		}
 
 		for(String v : input) {
 			if(k != v.length()) {
 				throw new WrongOligonucleotidesLengthException("Length of oligonucleotides are not the same!!!");
 			}
 		}
 	}
 
 	public boolean isFullEuler() {
 		if(!isConnectedOrIsolated()) {
 			return false;
 		}
 		return Iterables.all(bioGraph.vertexSet(), new Predicate<String>() {
 			@Override
 			public boolean apply(String arg0) {
 				return bioGraph.outDegreeOf(arg0) == bioGraph.inDegreeOf(arg0);
 			}
 		});
 	}
 
 	public boolean isHalfEuler() {
 		if(!isConnectedOrIsolated()) {
 			return false;
 		}
 		int out = 0;
 		int in = 0;
 		for(String arg0 : bioGraph.vertexSet()) {
 			int outDegreeOf = bioGraph.outDegreeOf(arg0);
 			int inDegreeOf = bioGraph.inDegreeOf(arg0);
 			out += Math.max(0, outDegreeOf - inDegreeOf);
 			in += Math.max(0, inDegreeOf - outDegreeOf);
 		}
 		return out == 1 && in == 1;
 	}
 
 	public List<String> findEuler(int maxErrors, Stack<String> status) throws GraphNotEuleranException {
 		if(maxErrors < 1 || isFullEuler() || isHalfEuler()) {
 			return findEuler(getEulerVertex());
 		}
 		String[] vertices = bioGraph.vertexSet().toArray(new String[] {});
 		for(int sv = 0; sv < vertices.length; sv++) {
 			for(int dv = 0; dv < vertices.length; dv++) {
 				String source = vertices[sv];
 				String dest = vertices[dv];
				System.out.println("testing (" + source + ", " + dest + ") edge");
 				if(source.substring(1, source.length()).equals(dest.substring(0, dest.length() - 1))) {
 					try {
 						Graph copy = new Graph(this);
 						copy.addEdge(source, dest);
 						status.push("added (" + source + ", " + dest + ") edge");
 						return copy.findEuler(maxErrors - 1, status);
 					} catch(GraphNotEuleranException e) {
 						status.pop();
 					}
 				}
 
 				try {
 					Graph copy = new Graph(this);
 					copy.removeEdge(source, dest);
 					status.push("deleted (" + source + ", " + dest + ") edge");
 					return copy.findEuler(maxErrors - 1, status);
 				} catch(GraphNotEuleranException e) {
 					status.pop();
 				}
 			}
 		}
 		throw new GraphNotEuleranException();
 	}
 
 	private LinkedList<String> findEuler(String starting) {
 		LinkedList<String> path = new LinkedList<String>();
 		path.add(starting);
 		Graph copy = new Graph(this);
 		while(copy.edgeSet().size() > 0) {
 			String last = path.getLast();
 			for(DefaultEdge edge : copy.outgoingEdgesOf(last)) {
 				String targetVertex = copy.getEdgeTarget(edge);
 
 				Graph copy2 = new Graph(copy);
				copy2.removeEdge(edge);
				if(copy2.isConnectedOrIsolated() && !(copy2.edgeSet().size() > 1 && copy2.outgoingEdgesOf(targetVertex).size() == 0)) {
 					copy.removeEdge(edge);
 					path.add(targetVertex);
 					break;
 				}
 
 			}
 		}
 		return path;
 	}
 
 	public List<String> findEuler() throws GraphNotEuleranException {
 		return findEuler(0, new Stack<String>());
 	}
 
 	public boolean isConnected() {
 		return (new ConnectivityInspector<String, DefaultEdge>(bioGraph)).isGraphConnected();
 	}
 
 	public boolean isConnectedOrIsolated() {
 		int notIsolated = 0;
 		for(Set<String> s : new ConnectivityInspector<String, DefaultEdge>(bioGraph).connectedSets()) {
			if(s.size() != 1) {
 				notIsolated++;
 				if(notIsolated > 1) {
 					return false;
 				}
 			}
 		}
 		return true;
 	}
 
 	public String getEulerVertex() throws GraphNotEuleranException {
 		boolean fullEuler = isFullEuler();
 		boolean halfEuler = isHalfEuler();
 		if(!fullEuler && !halfEuler) {
 			throw new GraphNotEuleranException();
 		}
 		if(fullEuler) {
 			return Iterables.find(bioGraph.vertexSet(), new Predicate<String>() {
 				@Override
 				public boolean apply(String arg0) {
 					return (bioGraph.outDegreeOf(arg0) != 0);
 				}
 			});
 		} else {
 			return Iterables.find(bioGraph.vertexSet(), new Predicate<String>() {
 				@Override
 				public boolean apply(String arg0) {
 					return (bioGraph.outDegreeOf(arg0) - bioGraph.inDegreeOf(arg0)) == 1;
 				}
 			});
 		}
 	}
 
 	@Override
 	public String toString() {
 		return bioGraph.toString();
 	}
 
 	public Set<DefaultEdge> edgeSet() {
 		return bioGraph.edgeSet();
 	}
 
 	public String getEdgeSource(DefaultEdge arg0) {
 		return bioGraph.getEdgeSource(arg0);
 	}
 
 	public String getEdgeTarget(DefaultEdge arg0) {
 		return bioGraph.getEdgeTarget(arg0);
 	}
 
 	public Set<String> vertexSet() {
 		return bioGraph.vertexSet();
 	}
 
 	public DefaultEdge addEdge(String arg0, String arg1) {
 		return bioGraph.addEdge(arg0, arg1);
 	}
 
 	public Set<DefaultEdge> outgoingEdgesOf(String arg0) {
 		return bioGraph.outgoingEdgesOf(arg0);
 	}
 
 	public boolean removeEdge(DefaultEdge arg0) {
 		return bioGraph.removeEdge(arg0);
 	}
 
 	public DefaultEdge removeEdge(String arg0, String arg1) {
 		return bioGraph.removeEdge(arg0, arg1);
 	}
 
 }
