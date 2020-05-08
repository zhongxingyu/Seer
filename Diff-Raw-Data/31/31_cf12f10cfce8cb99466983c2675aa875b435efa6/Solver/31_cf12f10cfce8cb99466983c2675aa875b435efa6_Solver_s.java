 package bio;
 
 import java.util.List;
 import java.util.Stack;
 
 public class Solver {
 	private Graph graph;
 
	public Solver(String[] input) throws WrongOligonucleotidesLengthException {
 		graph = new Graph(input);
 	}
 
 	public String solve(int maxErrors) throws GraphNotEuleranException {
 		for(int i = 0; i <= maxErrors; i++) {
 			System.out.println("working for " + i + " errors");
 			try {
 				Stack<String> status = new Stack<String>();
 				String output = writeOutput(graph.findEuler(i, status));
 				System.out.println(status);
 				return output;
 			} catch(GraphNotEuleranException e) {
 			}
 		}
 		throw new GraphNotEuleranException();
 	}
 
 	public String solve() throws GraphNotEuleranException {
 		return solve(0);
 	}
 
 	private String writeOutput(List<String> euler) {
 		StringBuilder result = new StringBuilder(euler.get(0));
 		for(int i = 1; i < euler.size(); i++) {
 			String string = euler.get(i);
 			result.append(string.charAt(string.length() - 1));
 		}
 		return result.toString();
 	}
 
 }
