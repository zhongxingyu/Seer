 import java.io.File;
 import java.io.FileNotFoundException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import datastructures.Stack;
 
 public class DFSSolver extends Solver{
 	private class SearchNode {
 	    private Board board;
 	    private int moves;
 	   	private SearchNode previous;
 	    	
 	    public SearchNode(Board b, int moves, SearchNode previous) {
 	    	this.board = b;
 	    	this.moves = moves;
 	    	this.previous = previous;
 	   	}
 	}
 			
 	/**
 	 *  Find the solution of the initial board using the DFS algorithm.
 	 * @param initial		The Board to be solved
 	 */
 	public DFSSolver(Board initial) {
     	// find a solution to the initial board (using the DFS algorithm)
     	Stack<SearchNode> stack = new Stack<SearchNode>();
     	ArrayList<Board> explored = new ArrayList<Board>();
 
     	time = System.currentTimeMillis();
     	
     	stack.push(new SearchNode(initial, 0, null));    	
     	SearchNode sn = null;
     	
     	while (!stack.isEmpty()) {
     		sn = stack.pop();
     		if (explored.contains(sn.board)) continue;
     		if (sn.board.isGoal()) break;
     		
     		explored.add(sn.board);
 			expNodes++;
			
     		for (Board b: sn.board.neighbors()) {
     			if (!explored.contains(b))
     				stack.push(new SearchNode(b, sn.moves + 1, sn));
     		}
     	} 
     	
 
     	if (stack.isEmpty()) {
     		solvable = false;
     		return;
     	}
     	
     	time = System.currentTimeMillis() - time;
     	
     	SearchNode prev = sn;
     	movements = new Stack<Action>();
     	
     	while (prev != null) {
     		movements.push(prev.board.getAction());
     		moves++;
     		prev = prev.previous;
     	}
     	solvable = true;
     }
 
 
 	public long getRunningTime() {
 		return time;
 	}
 
 	public int expandedNodes() {
 		return expNodes;
 	}
 
 	public boolean isSolvable() {
 		return solvable;
 	}
 
 	public int moves() {
 		return moves;
 	}
 
 	public Iterable<Action> solution() {
 		if (!solvable) return null;
     	return movements;
 	}
 	
 	public static void main(String[] args) {
		 // create initial board from file
		File file = new File("C:\\Users\\JORGE\\workspace\\Solver\\puzzles\\" + args[0]);
 		
 		Scanner in = null;
 		try {
 			in = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 		
 		in.next();
 		
         int N = 6;
         char[][] blocks = new char[N][N];
         for (int i = 0; i < N; i++)
             for (int j = 0; j < N; j++)
                 blocks[i][j] = in.next().charAt(0);
        

         Board initial = new Board(blocks);
        
        // solve the puzzle
        DFSSolver solver = new DFSSolver(initial);

        // print solution to standard output       
         System.out.println("Minimum number of moves = " + solver.moves());
        
         for (Action a: solver.solution())
         	System.out.println(a);
 	}
 }
