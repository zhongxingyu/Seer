 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 
 public class Solver {
 
 	public static String solveForward(ArrayList<String> lines, Deadline deadline) {
 		Board.initialize(lines);
 		Board.setRandomNumbers();
 
 		System.out.println("Board to solve:");
 		System.out.println(Board.initialState);
 
 		State solvedState = idaStar(new HashSet<Integer>(), deadline);
 		if(solvedState == null) {
 			return "";
 		}
 		String revSoloution = solvedState.backtrackSolution();
 		// new Guireplay(solvedState);
 
 		return reverseString(revSoloution);
 	}
 
 	public static String solveBackward(ArrayList<String> lines, Deadline deadline) {
 		Board.initialize(lines);
 		Board.setRandomNumbers();
 		Board.transformToBackward();
 
 		System.out.println("Board to solve:");
 		System.out.println(Board.initialState);
 
 		State solvedState = idaStar(new HashSet<Integer>(), deadline);
 		System.out.println("Solved state:");
 		System.out.println(solvedState);
 
 		if(solvedState == null) {
 			return "";
 		}
 		String backSoloution = solvedState.backtrackSolution();
 		String preSolution =
 				solvedState.connectivity.backtrackPathString(
 						BackwardState.playerStartPosition,
 						solvedState.playerPosition);
 
 		System.out.println("Additional moves: " + preSolution);
 
 		String sol = preSolution + backSoloution;
 		sol = convertBackwardString(sol);
 
 		// new Guireplay(solvedState);
 
 		return sol;
 	}
 
 	public static String solveCombo(ArrayList<String> lines, Deadline deadline) {
 		Board.initialize(lines);
 		Board.setRandomNumbers();
 
 		Board.transformToBackward();
 		Set<State> backwardVisited =
 				backwardBFS(lines, new Deadline(deadline.timeUntil() * 2 / 3));
 
 		Board.initialize(lines);
 
 		return searchForward(backwardVisited, deadline);
 
 	}
 
 	public static Set<State> backwardBFS(ArrayList<String> lines, Deadline deadline) {
 		System.out.println("Searching from:");
 		System.out.println(Board.initialState);
 
 		Queue<State> q = new LinkedList<State>();
 		Set<State> visited = new HashSet<State>(1000000, 0.99f);
 
 		q.add(Board.initialState);
 		visited.add(Board.initialState);
 
 		Collection<State> children = new LinkedList<State>();
 
 		while(!q.isEmpty() && deadline.timeUntil() > 0) {
 			q.poll().getChildren(children);
 			for(State child : children) {
 				if(visited.add(child)) {
 					q.add(child);
 				}
 			}
 		}
 
 		System.out.println("Backward search visited " + visited.size() + " states");
 
 		return visited;
 	}
 
 	private static State idaStar(HashSet<Integer> visited, Deadline deadline) {
 		PriorityQueue<State> nodesLeft = new PriorityQueue<State>();
 		List<State> childStates = new LinkedList<State>();
 		State parent;
 
 		int cutoff = Board.initialState.getHeuristicValue();
 
 		while(true) {
 			
 			int nextCutoff = Integer.MAX_VALUE;
 			nodesLeft.add(Board.initialState);
 			visited.clear();
 						
 			System.out.println("Search depth: "+cutoff);
 			while(!nodesLeft.isEmpty()) {
 				
 				if(deadline.timeUntil()<0) {
 					return null;
 				}
 				
 				parent = nodesLeft.poll();
 
 				if(!visited.contains(parent.hashCode())) {
 					visited.add(parent.hashCode());
 
 					parent.getChildren(childStates);
 
 					for(State child : childStates) {
 						
 						if(visited.contains(child.hashCode())) {
 							continue;
 						}
 
 						if(child.isSolved()) {
 							System.out.println("Solved in "
 									+ child.getNumberOfSignificantMoves()
 									+ " significant moves.");
 							return child;
 						}
 
 						int childCost = child.getNumberOfSignificantMoves() + child.getHeuristicValue();
 						if(childCost > cutoff) {
 							nextCutoff = Math.min(nextCutoff, childCost);
 						} else if(!nodesLeft.contains(child)) {
 							nodesLeft.add(child);
 						}
 
 					}
 				}
 			}
 			if(cutoff < nextCutoff) {
 				cutoff = nextCutoff;
 			} else {
 				return null;
 			}
 		}
 	}
 
 	private static String searchForward(Set<State> backwardVisited, Deadline deadline) {
 		HashSet<Integer> visited = new HashSet<Integer>();
 		PriorityQueue<State> q = new PriorityQueue<State>();
 		List<State> childStates = new LinkedList<State>();
 
 		int cutoff = Board.initialState.getHeuristicValue();
 
 		while(true) {
 
 			int nextCutoff = Integer.MAX_VALUE;
 			q.add(Board.initialState);
 			visited.clear();
 
 			System.out.println("Search depth in first forward search: "+cutoff);
 
 			int childHash;
 
 			while(!q.isEmpty()) {
 
 				if(deadline.timeUntil()<0) {
 					return null;
 				}
 
 				q.poll().getChildren(childStates);
 
 				for(State child : childStates) {
 
 					childHash = child.hashCode();
 
 					if(!visited.add(childHash)) {
 						continue;
 					}
 
 					if(child.isSolved()) {
 						System.out.println("Solved in "
 								+ child.getNumberOfSignificantMoves()
 								+ " significant moves.");
 						return new StringBuilder(child.backtrackSolution()).reverse().toString();
 					}
 
 					if(backwardVisited.contains(child)) {
 						System.out.println("Found match with backward solution!");
 						System.out.println("Time remaining: " + deadline.timeUntil() + " ms");
 						System.out.println("This state:");
 						System.out.println(child);
 						System.out.println("Matched state:");
 						backwardVisited.retainAll(Arrays
 								.asList(new State[]{child}));
 
 						State matched = backwardVisited.iterator().next();
 						System.out.println(matched);
 
 						String childMoves = reverseString(child.backtrackSolution());
 
 						System.out.println("Moves this far: " + childMoves);
 
 						String intermediateMoves =
 								reverseString(
 										child.getConnectivity().backtrackPathString(
 										matched.playerPosition,
 										child.playerPosition));
 						System.out.println("Intermediate moves: " + intermediateMoves);
 
 						String futureString = convertBackwardString(matched.backtrackSolution());
 						System.out.println("Future moves: " + futureString);
 
 						System.out.println("Future:");
 						for(State s = matched.parent; s != null; s = s.parent) {
 							System.out.println(s);
 						}
 						return childMoves + intermediateMoves + futureString;
 					}
 
 					int childCost = child.getNumberOfSignificantMoves() + child.getHeuristicValue();
 					if(childCost > cutoff) {
 						nextCutoff = Math.min(nextCutoff, childCost);
 					} else {
 						q.add(child);
 					}
 
 				}
 			}
 			if(cutoff < nextCutoff) {
 				cutoff = nextCutoff;
 			} else {
 				return null;
 			}
 		}
 	}
 
 	private static State fixedDepthAStar(State startState, int maxDepth, Deadline deadline) {
 		HashSet<Integer> visited = new HashSet<Integer>();
 		PriorityQueue<State> q = new PriorityQueue<State>();
 		List<State> childStates = new LinkedList<State>();
 		State parent;
 
 		q.add(startState);
 
 		System.out.println("Search depth in final forward search: "+maxDepth);
 		while(!q.isEmpty() && deadline.timeUntil() > 0) {
 
 			parent = q.poll();
 
 			if(visited.add(parent.hashCode())) {
 
 				parent.getChildren(childStates);
 
 				for(State child : childStates) {
 
 					if(visited.contains(child.hashCode())) {
 						continue;
 					}
 
 					if(child.isSolved()) {
 						System.out.println("Solved in "
 								+ child.getNumberOfSignificantMoves()
 								+ " significant moves.");
 						return child;
 					}
 
 					if(child.nSignificantMoves <= maxDepth && !q.contains(child)) {
 						q.add(child);
 					}
 
 				}
 			}
 		}
 		return null;
 	}
 
 	private static String reverseString(String s) {
 		return new StringBuilder(s).reverse().toString();
 	}
 
 	/**
 	 * Converts a backward string to a forward one, or vice versa. More
 	 * precisely, flips "R"<->"L" and "U"<->"D".
 	 *
 	 * @param sol the {@link String} to convert
 	 * @return
 	 */
 	private static String convertBackwardString(String sol) {
 		sol = sol.replaceAll("R", "l");
 		sol = sol.replaceAll("L", "r");
 		sol = sol.replaceAll("U", "d");
 		sol = sol.replaceAll("D", "u");
 
 		sol = sol.replaceAll("r", "R");
 		sol = sol.replaceAll("l", "L");
 		sol = sol.replaceAll("u", "U");
 		sol = sol.replaceAll("d", "D");
 		return sol;
 	}
 }
