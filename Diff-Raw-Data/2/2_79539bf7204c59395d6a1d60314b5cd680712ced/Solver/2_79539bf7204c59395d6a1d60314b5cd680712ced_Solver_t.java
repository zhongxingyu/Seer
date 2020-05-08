 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.PriorityQueue;
 import java.util.Queue;
 
 public class Solver {
 
 	public static String solve(ArrayList<String> lines, Deadline deadline) {
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
 
 		return new StringBuffer(revSoloution).reverse().toString();
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
 		sol = sol.replaceAll("R", "l");
 		sol = sol.replaceAll("L", "r");
 		sol = sol.replaceAll("U", "d");
 		sol = sol.replaceAll("D", "u");
 
 		sol = sol.replaceAll("r", "R");
 		sol = sol.replaceAll("l", "L");
 		sol = sol.replaceAll("u", "U");
 		sol = sol.replaceAll("d", "D");
 
 		// new Guireplay(solvedState);
 
 		return sol;
 	}
 
 	public static String solveCombo(ArrayList<String> lines, Deadline deadline) {
 		Board.initialize(lines);
 		Board.setRandomNumbers();
 
 		Board.transformToBackward();
 		Map<Integer, Integer> backwardResult = searchBackward(lines, new Deadline(deadline.timeUntil()/3));
 
 		Board.initialize(lines);
 
 		HashSet<Integer> visited = new HashSet<Integer>(1000000);
 
 		State solutionCandidate = informedIdaStar(backwardResult, visited, deadline);
 		if(solutionCandidate == null ) {
 			System.out.println("No solution found");
 			return "";
 		}
 
 		if(solutionCandidate.isSolved()) {
 			System.out.println("Solved in backward search");
 			return new StringBuilder(solutionCandidate.backtrackSolution()).reverse().toString();
 		}
 
		State solvedState = fixedDepthAStar(solutionCandidate, visited, backwardResult.get(solutionCandidate.hashCode()), deadline);
 		if(solvedState == null) {
 			System.out.println("No solution found");
 			return "";
 		}
 
 		return new StringBuilder(solvedState.backtrackSolution()).reverse().toString();
 
 	}
 
 	public static Map<Integer, Integer> searchBackward(ArrayList<String> lines, Deadline deadline) {
 		System.out.println("Searching from:");
 		System.out.println(Board.initialState);
 
 //		int maxDepth = 0;
 
 		Queue<State> q = new LinkedList<State>();
 		Map<Integer, Integer> visitedDepths = new HashMap<Integer, Integer>(1000000, 0.99f);
 
 		q.add(Board.initialState);
 		visitedDepths.put(Board.initialState.hashCode(), 0);
 
 		Collection<State> children = new LinkedList<State>();
 		int hash;
 
 		while(!q.isEmpty() && deadline.timeUntil() > 0) {
 			q.poll().getChildren(children);
 			for(State child : children) {
 				hash = child.hashCode();
 				if(!visitedDepths.containsKey(hash)) {
 					visitedDepths.put(hash, child.nSignificantMoves);
 					q.add(child);
 //					maxDepth = Math.max(maxDepth, child.nSignificantMoves);
 				}
 			}
 		}
 
 		System.out.println("Traversed " + visitedDepths.size() + " states");//, max depth " + maxDepth + ", time remaining: " + deadline.TimeUntil());
 
 		return visitedDepths;
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
 
 	private static State informedIdaStar(Map<Integer, Integer> backwardResult, HashSet<Integer> visited, Deadline deadline) {
 		PriorityQueue<State> q = new PriorityQueue<State>();
 		List<State> childStates = new LinkedList<State>();
 
 		int cutoff = Board.initialState.getHeuristicValue();
 
 		while(true) {
 
 			int nextCutoff = Integer.MAX_VALUE;
 			q.add(Board.initialState);
 			visited.clear();
 
 			System.out.println("Search depth: "+cutoff);
 
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
 						return child;
 					}
 
 					if(backwardResult.containsKey(childHash)) {
 						System.out.println("Found match with backward solution at state:");
 						System.out.println(child);
 						return child;
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
 
 	private static State fixedDepthAStar(State startState, HashSet<Integer> visited, int maxDepth, Deadline deadline) {
 		PriorityQueue<State> q = new PriorityQueue<State>();
 		List<State> childStates = new LinkedList<State>();
 		State parent;
 
 		maxDepth += startState.nSignificantMoves;
 
 		q.add(startState);
 		visited.clear();
 
 		System.out.println("Search depth: "+maxDepth);
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
 }
