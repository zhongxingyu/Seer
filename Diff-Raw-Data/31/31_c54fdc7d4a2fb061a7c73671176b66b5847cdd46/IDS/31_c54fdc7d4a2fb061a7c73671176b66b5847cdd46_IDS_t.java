 package sokoban.solvers;
 
import java.util.Deque;
 import java.util.LinkedList;
 import java.util.List;
 
 import sokoban.Board;
 
 public class IDS implements Solver
 {
 
     private final Board startBoard;
     private final int DEPTH_LIMIT = 1000;
     
     public IDS(Board startBoard)
     {
         this.startBoard = startBoard;
     }
     
     /**
      * Recursive Depth-First algorithm
      * @param maxDepth  The maximum depth.
      * @return
      */
    private Deque<Board.Direction> dfs(Board board, int maxDepth)
     {
         //System.out.println(board.toString());
         
         if (board.getRemainingBoxes() == 0) {
             // Found a solution
            return new LinkedList<Board.Direction>();
         }
         
         if (maxDepth == 0) return null;
         
         for (Board.Direction dir : Board.Direction.values()) {
             // Check that the move is possible
             if (!board.canMove(dir)) continue;
             
             // The move is possible
             
             // Make the move on a copy of the board
             Board successor = (Board)board.clone();
             successor.move(dir);
             
             // Recurse
            Deque<Board.Direction> sol = dfs(successor, maxDepth - 1);
            if (sol != null) {
                sol.addFirst(dir);
                return sol;
            }
         }
         
         return null;
     }
     
    private static final char moveChars[] = { 'U', 'D', 'L', 'R' };
    
     /**
      * 
      * @param solution  An array from dfs()
      * @return The solution as a string
      */
    private String solutionToString(Deque<Board.Direction> solution) {
         StringBuilder sb = new StringBuilder(2*solution.size());
        for (Board.Direction move : solution) {
            sb.append(moveChars[move.ordinal()]);
             sb.append(' ');
         }
         return sb.toString();
     }
 
     @Override
     public String solve()
     {
         System.out.println(startBoard);
         
         for (int maxDepth = 1; maxDepth < DEPTH_LIMIT; maxDepth++) {
            Deque<Board.Direction> solution = dfs(startBoard, maxDepth);
             if (solution != null) {
                 return solutionToString(solution); 
             }
             System.out.println("Reached depth limit: "+maxDepth);
         }
         
        System.out.println("no solution!");
         return null;
     }
 
 }
