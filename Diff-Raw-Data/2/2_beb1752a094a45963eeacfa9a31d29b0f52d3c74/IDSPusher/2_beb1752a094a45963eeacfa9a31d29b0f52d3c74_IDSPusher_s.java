 package sokoban.solvers;
 
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.Queue;
 
 import sokoban.Board;
 import sokoban.Position;
 import sokoban.ReachableBox;
 import sokoban.Board.Direction;
 
 /**
  * A solver that pushes boxes around with iterative deepening and
  * a bloom filter to avoid duplicate states.
  */
 public class IDSPusher implements Solver
 {
     private final int DEPTH_LIMIT = 1000;
     /**
      * The number of generated nodes
      */
     public static int generatedNodes = 0;
     private static int remainingDepth;
     private static Board board;
 
     public int getIterationsCount()
     {
         return generatedNodes;
     }
 
     /**
      * Set of visited boards, including the player position
      */
     private HashSet<Long> visitedBoards;
 
     /**
      * Boards that just lead to deadlocks or already visited boards. It
      * doesn't make sense to visit these in later iterations.
      */
     private HashSet<Long> failedBoards;
 
     enum SearchStatus {
         /**
          * The search reached the maximum depth, and no solution was found,
          * so it's inconclusive (a solution could follow, but we don't know).
          */
         Inconclusive,
 
         /**
          * This search resulted in a solution.
          */
         Solution,
 
         /**
          * This search failed without reached the maximum depth, so there's
          * no point in trying it again with a greater search depth.
          */
         Failed,
     };
 
     /**
      * Contains information about a search, whether it is failed, reached a
      * solution or is inconclusive.
      */
     final static class SearchInfo
     {
         final SearchStatus status;
         final LinkedList<Board.Direction> solution;
 
         static SearchInfo Inconclusive = new SearchInfo(
                 SearchStatus.Inconclusive);
         static SearchInfo Failed = new SearchInfo(SearchStatus.Inconclusive);
 
         public SearchInfo(final SearchStatus status)
         {
             this.status = status;
             solution = null;
         }
 
         private SearchInfo()
         {
             status = SearchStatus.Solution;
             solution = new LinkedList<Board.Direction>();
         }
 
         public static SearchInfo emptySolution()
         {
             return new SearchInfo();
         }
     }
 
     /**
      * Recursive Depth-First algorithm
      * 
      * @param maxDepth The maximum depth.
      * @return
      */
     private SearchInfo dfs()
     {
         generatedNodes++;
 
         if (board.getRemainingBoxes() == 0) {
             // Found a solution
             return SearchInfo.emptySolution();
         }
 
         if (remainingDepth <= 0) {
             return SearchInfo.Inconclusive;
         }
 
         if (!visitedBoards.add(board.getZobristKey())) {
             // Duplicate state
             return SearchInfo.Failed;
         }
 
         // True if at least one successor tree was inconclusive.
         boolean inconclusive = false;
 
         final Position source = new Position(board.getPlayerRow(), board.getPlayerCol());
         remainingDepth--;
 
         // TODO optimize: no need for paths here
         final byte[][] cells = board.cells;
         for (final ReachableBox reachable : board.findReachableBoxSquares()) {
             for (final Direction dir : Board.Direction.values()) {
                 final Position boxFrom = new Position(reachable.position,
                         Board.moves[dir.ordinal()]);
                 final Position boxTo = new Position(boxFrom, Board.moves[dir
                         .ordinal()]);
                 if (Board.is(cells[boxFrom.row][boxFrom.column], Board.BOX)
                         && !Board
                                 .is(cells[boxTo.row][boxTo.column], Board.REJECT_BOX)) {
                     // The move is possible
                     
                     // Move the player and push the box
                     board.moveBox(boxFrom, boxTo);
                     board.movePlayer(source, boxFrom);
 
                     // Check if we got a freeze deadlock
                    if (freezeDeadlock(from, to)) {
                         return SearchInfo.Failed;
                     }
 
                     // Process successor states
                     final SearchInfo result = dfs();
 
                     // Restore changes
                     board.moveBox(boxTo, boxFrom);
                     board.movePlayer(boxFrom, source);
 
                     // Evaluate result
                     switch (result.status) {
                         case Solution:
                             // Found a solution. Return it now!
 
                             // Add the last movement first
                             result.solution.addFirst(dir);
                             // So we can put the rest in front of it
                             result.solution.addAll(0, reachable.path);
 
                             return result;
                         case Inconclusive:
                             // Make the parent inconclusive too
                             inconclusive = true;
                             continue;
                         case Failed:
                             // Mark this node as failed
                             failedBoards.add(board.getZobristKey());
                             continue;
                     }
                 }
             }
         }
 
         remainingDepth++;
 
         if (inconclusive) {
             // Add all successors that failed to the failed set
             return SearchInfo.Inconclusive;
         }
         else {
             // All successors failed, so this node is failed
             failedBoards.add(board.getZobristKey());
             return SearchInfo.Failed;
         }
     }
 
     public String solve(final Board startBoard)
     {
         failedBoards = new HashSet<Long>();
         long startTime = System.currentTimeMillis();
         final int lowerBound = lowerBound(startBoard);
         System.out.println("lowerBound(): " + lowerBound + " took "
                 + (System.currentTimeMillis() - startTime) + " ms");
         System.out.println("IDS depth limit (progress): ");
         for (int maxDepth = lowerBound; maxDepth < DEPTH_LIMIT; maxDepth += 3) {
             System.out.print(maxDepth + ".");
 
             visitedBoards = new HashSet<Long>(failedBoards);
             remainingDepth = maxDepth;
             board = (Board) startBoard.clone();
 
             final SearchInfo result = dfs();
             if (result.solution != null) {
                 System.out.println();
                 return Board.solutionToString(result.solution);
             }
             else if (result.status == SearchStatus.Failed) {
                 System.out.println("no solution!");
                 return null;
             }
         }
 
         System.out.println("maximum depth reached!");
         return null;
     }
 
     private static int lowerBound(final Board board)
     {
         final ArrayList<Position> boxes = new ArrayList<Position>();
         final Queue<Position> goals = new LinkedList<Position>();
         for (int row = 0; row < board.height; row++) {
             for (int col = 0; col < board.width; col++) {
                 if (Board.is(board.cells[row][col], Board.BOX)) {
                     boxes.add(new Position(row, col));
                 }
                 if (Board.is(board.cells[row][col], Board.GOAL)) {
                     goals.add(new Position(row, col));
                 }
             }
         }
         int result = 0;
         while (!goals.isEmpty()) {
             final Position goal = goals.poll();
             Position minBox = null;
             int min = Integer.MAX_VALUE;
             for (final Position box : boxes) {
                 final int tmp = distance(goal, box);
                 if (tmp < min) {
                     min = tmp;
                     minBox = box;
                 }
             }
 
             boxes.remove(minBox);
             result += min;
         }
         return result;
     }
 
     /**
      * Approximate the distance between two positions
      * 
      * The distance will be the absolute minimum and are guaranteed to be equal
      * to or greater then the real distance.
      * 
      * TODO It might be smarter to implement a search that takes the actual
      * board into account.
      * 
      * @param a One of the positions
      * @param b The other position
      * @return The approximate distance between the two.
      */
     private static int distance(final Position a, final Position b)
     {
         return Math.abs(a.column - b.column) + Math.abs(a.row - b.row);
     }
 
     /**
      * Check if the move resulted in a freeze deadlock
      * (two boxes between each other next to a wall)
      * 
      * This method assumes the move is valid.
      * 
      * @param from The previous position
      * @param to The new position
      * @return True if there is a freeze deadlock
      */
     private boolean freezeDeadlock(Position from, Position to)
     {
         boolean blocked = false;
         // Horisontal
 
         // #  If there is a wall on the left or on the right side of the box then the box is blocked along this axis 
         if (Board.is(board.cells[to.row][to.column+1], Board.WALL) || Board.is(board.cells[to.row][to.column-1], Board.WALL))
             blocked = true;
         // #  If there is a simple deadlock square on both sides (left and right) of the box the box is blocked along this axis 
         else if (Board.is(board.cells[to.row][to.column+1], Board.BOX_TRAP) && Board.is(board.cells[to.row][to.column-1], Board.BOX_TRAP))
             blocked = true;
         // #  If there is a box one the left or right side then this box is blocked if the other box is blocked. 
         else {
             // If there is a box on the right
             if (Board.is(board.cells[to.row][to.column+1], Board.BOX)) {
                 // check if that box is blocked
             }
             if (Board.is(board.cells[to.row][to.column-1], Board.BOX)) {
                 // check if that box is blocked
             }
         }
         
         // #  If there is a wall on the left or on the right side of the box then the box is blocked along this axis 
         if (Board.is(board.cells[to.row+1][to.column], Board.WALL) || Board.is(board.cells[to.row-1][to.column], Board.WALL))
             if (blocked) return true;
         // #  If there is a simple deadlock square on both sides (left and right) of the box the box is blocked along this axis 
         else if (Board.is(board.cells[to.row+1][to.column], Board.BOX_TRAP) && Board.is(board.cells[to.row-1][to.column], Board.BOX_TRAP))
             if (blocked) return true;
         // #  If there is a box one the left or right side then this box is blocked if the other box is blocked. 
         else {
             // If there is a box on the right
             if (Board.is(board.cells[to.row+1][to.column], Board.BOX)) {
                 // check if that box is blocked
             }
             if (Board.is(board.cells[to.row-1][to.column], Board.BOX)) {
                 // check if that box is blocked
             }
         }
         return false;
 //        byte WALL_OR_BOX = Board.BOX | Board.WALL;
 //        
 //        if (Board.is(board.cells[to.row+1][to.column], Board.BOX) && !(from.row == to.row+1 && from.column == to.column)) {
 //            // If the box is above
 //            if (Board.is(board.cells[to.row][to.column+1], WALL_OR_BOX) && Board.is(board.cells[to.row+1][to.column+1], WALL_OR_BOX)) {
 //                // If there is a wall to the right of them
 //                return true;
 //            }
 //            if (Board.is(board.cells[to.row][to.column-1], WALL_OR_BOX) && Board.is(board.cells[to.row+1][to.column-1], WALL_OR_BOX)) {
 //                // If there is a wall to the left of them
 //                return true;
 //            }
 //        }
 //        if (Board.is(board.cells[to.row-1][to.column], Board.BOX) && !(from.row == to.row-11 && from.column == to.column)) {
 //            // If the box is below
 //            if (Board.is(board.cells[to.row][to.column+1], WALL_OR_BOX) && Board.is(board.cells[to.row-1][to.column+1], WALL_OR_BOX)) {
 //                // If there is a wall to the right of them
 //                return true;
 //            }
 //            if (Board.is(board.cells[to.row][to.column-1], WALL_OR_BOX) && Board.is(board.cells[to.row-1][to.column-1], WALL_OR_BOX)) {
 //                // If there is a wall to the left of them
 //                return true;
 //            }
 //        }
 //        if (Board.is(board.cells[to.row][to.column+1], Board.BOX)) {
 //            // If the box is to the right
 //            if (Board.is(board.cells[to.row+1][to.column], WALL_OR_BOX) && Board.is(board.cells[to.row+1][to.column+1], WALL_OR_BOX)) {
 //                // If there is a wall above
 //                return true;
 //            }
 //            if (Board.is(board.cells[to.row-1][to.column], WALL_OR_BOX) && Board.is(board.cells[to.row-1][to.column+1], WALL_OR_BOX)) {
 //                // If there is a wall belove
 //                return true;
 //            }
 //        }
 //        if (Board.is(board.cells[to.row][to.column-1], Board.BOX)) {
 //            // If the box is to the left
 //            if (Board.is(board.cells[to.row+1][to.column], WALL_OR_BOX) && Board.is(board.cells[to.row+1][to.column-1], WALL_OR_BOX)) {
 //                // If there is a wall above
 //                return true;
 //            }
 //            if (Board.is(board.cells[to.row-1][to.column], WALL_OR_BOX) && Board.is(board.cells[to.row-1][to.column-1], WALL_OR_BOX)) {
 //                // If there is a wall to below
 //                return true;
 //            }
 //        }
 //        
 //        return false;
     }
 }
