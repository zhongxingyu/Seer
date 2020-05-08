 package com.softcocoa.eightpuzzle.solvers;
 
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.LinkedList;
 import java.util.Stack;
 import java.util.TreeSet;
 
 import com.softcocoa.eightpuzzle.C;
 import com.softcocoa.eightpuzzle.PuzzleState;
 import com.softcocoa.eightpuzzle.heuristic.HeuristicCalculator;
 
 public class AStarSearching extends PuzzleSolveAlgorithm {
 	
 	private TreeSet<PuzzleState> nodes;
 	private ArrayList<PuzzleState> closedNodes;
 
 	public AStarSearching(PuzzleState initialState, HeuristicCalculator hCalculator) {
 		super(initialState, hCalculator);
 		nodes = new TreeSet<PuzzleState>(puzzleStateHeuristicComparator);
 		closedNodes = new ArrayList<PuzzleState>(C.INITIAL_CLOSED_LIST_SIZE);
 		nodes.add(initialState);
 	}
 
 	@Override
 	public ArrayList<PuzzleState> solvePuzzle() {
 		ArrayList<PuzzleState> solvingSteps = new ArrayList<PuzzleState>(C.INITIAL_STEP_LIST_SIZE);
 		// TODO expand PuzzleState in nodes TreeSet and keep expanded nodes in closedNodes
 		// until we reach the goal and extract the solution path from closedNodes into solvingSteps.
 		
 		// testing dummy
		/*steps.add(initialState);
		steps.add(new PuzzleState(new int[]{1,0,3,4,2,5,7,8,6}));
		steps.add(new PuzzleState(new int[]{1,3,4,0,5,2,7,8,6}));
		steps.add(new PuzzleState(C.FINISH_PUZZLE_TILES()));
		steps.trimToSize();*/
 		
 		solvingSteps.trimToSize();
 		return solvingSteps;
 	}
 	
 	private Comparator<PuzzleState> puzzleStateHeuristicComparator = new Comparator<PuzzleState>() {
 		@Override
 		public int compare(PuzzleState o1, PuzzleState o2) {
 			int result = 0;
 			
 			if (o1.getHeuristic()<o2.getHeuristic())
 				result = -1;
 			else if (o1.getHeuristic()>o2.getHeuristic())
 				result = 1;
 			
 			return result;
 		}
 	};
 
 }
