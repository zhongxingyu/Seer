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
                     
                     System.out.println("\n\nLast board:\n" + board);
                     // Check if we got a freeze deadlock
                     if (freezeDeadlock(boxTo, DEADLOCK_BOTH)) {
                         System.out.println("DEADLOCK FOUND");
                         return SearchInfo.Failed;
                     }
                     
                     // Move the player and push the box
                     board.moveBox(boxFrom, boxTo);
                     board.movePlayer(source, boxFrom);
                     
                     System.out.println("\n\nLast board:\n" + board);
                     // Check if we got a freeze deadlock
                     if (freezeDeadlock(boxTo, DEADLOCK_BOTH)) {
                         System.out.println("DEADLOCK FOUND");
                         return SearchInfo.Failed;
                     }
 
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
     private final static byte DEADLOCK_BOTH = 0;
     private final static byte DEADLOCK_HORIZONTAL = 1;
     private final static byte DEADLOCK_VERTICAL = 2;
     private boolean freezeDeadlock(Position box, byte type)
     {
         // TODO: Do not move the box before checking freeze deadlock, creates deadlock with it self.
         if (type == DEADLOCK_BOTH) {
             boolean blockedVertical = false;
             boolean blockedHorizontal = false;
             
             System.out.println("Looking at position: " + box);
             
             // If there is a wall to the left or right
             if (Board.is(board.cells[box.row][box.column+1], Board.WALL) || Board.is(board.cells[box.row][box.column-1], Board.WALL)) {
                 blockedHorizontal = true;
                 System.out.println("wall left or right");
             }
             
             // If there is a wall to the top or bottom
             if (Board.is(board.cells[box.row+1][box.column], Board.WALL) || Board.is(board.cells[box.row-1][box.column], Board.WALL)) {
                 blockedVertical = true;
                 System.out.println("wall top or bottom");
             }
             
             // If there is a box_trap (simple deadlock check) to the left and right
             if (Board.is(board.cells[box.row][box.column+1], Board.BOX_TRAP) && Board.is(board.cells[box.row][box.column-1], Board.BOX_TRAP)) {
                 blockedHorizontal = true;
                 System.out.println("box trap left and right");
             }
             
             // If there is a box_trap (simple deadlock check) to the top and bottom
             if (Board.is(board.cells[box.row+1][box.column], Board.BOX_TRAP) && Board.is(board.cells[box.row-1][box.column], Board.BOX_TRAP)) {
                 blockedVertical = true;
                 System.out.println("box trap top and bottom");
             }
             
             // If we are both blocked horizontal and vertical, return deadlock
             if (blockedVertical && blockedHorizontal) {
                 System.out.println("Both top/bottom and left/right are blocked");
                 return true;
             // Only horizontal
             } else if (!blockedVertical && blockedHorizontal) {
                 System.out.println("Only horizontal (left/right) is blocked.");
                 if (Board.is(board.cells[box.row+1][box.column], Board.BOX)) {
                     System.out.println("Box below, check it");
                     return freezeDeadlock(new Position(box.row+1, box.column), DEADLOCK_HORIZONTAL);
                 }
                 
                 if (Board.is(board.cells[box.row-1][box.column], Board.BOX)) {
                     System.out.println("Box above, check it");
                     return freezeDeadlock(new Position(box.row-1, box.column), DEADLOCK_HORIZONTAL);
                 }
             // Only vertical
             } else if (!blockedHorizontal && blockedVertical) {
                 System.out.println("Only vertical (top/bottom) is blocked.");
                 if (Board.is(board.cells[box.row][box.column+1], Board.BOX)) {
                     System.out.println("Box to the right");
                     return freezeDeadlock(new Position(box.row, box.column+1), DEADLOCK_VERTICAL);
                 }
                 
                 if (Board.is(board.cells[box.row][box.column-1], Board.BOX)) {
                     System.out.println("Box to the left");
                     return freezeDeadlock(new Position(box.row, box.column-1), DEADLOCK_VERTICAL);
                 }
             // No deadlock
             } else {
                 return false;
             }
         // HORIZONTAL CHECK
         } else if (type == DEADLOCK_HORIZONTAL) {
             // If there is a wall to the left or right
             if (Board.is(board.cells[box.row][box.column+1], Board.WALL) || Board.is(board.cells[box.row][box.column-1], Board.WALL)) {
                 return true;
             }
             
             // If there is a box_trap (simple deadlock check) to the left and right
             if (Board.is(board.cells[box.row][box.column+1], Board.BOX_TRAP) && Board.is(board.cells[box.row][box.column-1], Board.BOX_TRAP)) {
                 return true;
             }
             
             if (Board.is(board.cells[box.row][box.column+1], Board.BOX)) {
                 return freezeDeadlock(new Position(box.row, box.column+1), DEADLOCK_VERTICAL);
             }
             
             if (Board.is(board.cells[box.row][box.column-1], Board.BOX)) {
                 return freezeDeadlock(new Position(box.row, box.column-1), DEADLOCK_VERTICAL);
             }
             return false;
         // VERTICAL CHECK
         } else if (type == DEADLOCK_VERTICAL) {
             // If there is a wall to the top or bottom
             if (Board.is(board.cells[box.row+1][box.column], Board.WALL) || Board.is(board.cells[box.row-1][box.column], Board.WALL)) {
                 return true;
             }
             
             // If there is a box_trap (simple deadlock check) to the top and bottom
             if (Board.is(board.cells[box.row+1][box.column], Board.BOX_TRAP) && Board.is(board.cells[box.row-1][box.column], Board.BOX_TRAP)) {
                 return true;
             }
             
             if (Board.is(board.cells[box.row+1][box.column], Board.BOX)) {
                 return freezeDeadlock(new Position(box.row+1, box.column), DEADLOCK_HORIZONTAL);
             }
             
             if (Board.is(board.cells[box.row-1][box.column], Board.BOX)) {
                 return freezeDeadlock(new Position(box.row-1, box.column), DEADLOCK_HORIZONTAL);
             }
             return false;
         }
         return false;
     }
 }
