 package sokoban.solvers;
 
 import java.io.IOException;
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
  * hashing to avoid duplicate states.
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
         static SearchInfo Failed = new SearchInfo(SearchStatus.Failed);
 
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
 
         // True if at least one successor tree was inconclusive.
         boolean inconclusive = false;
 
         long hash = board.getZobristKey();
         
         final Position source = new Position(board.getPlayerRow(), board.getPlayerCol());
         remainingDepth--;
         
 //        System.out.println("Board before branch:\n" + board);
         
         // TODO optimize: no need for paths here
         final byte[][] cells = board.cells;
         for (final Position player : board.findReachableBoxSquares()) {
             for (final Direction dir : Board.Direction.values()) {
                 final Position boxFrom = new Position(player,
                         Board.moves[dir.ordinal()]);
                 final Position boxTo = new Position(boxFrom, Board.moves[dir
                         .ordinal()]);
                 // Check if the move is possible
                 if (Board.is(cells[boxFrom.row][boxFrom.column], Board.BOX)
                         && !Board.is(cells[boxTo.row][boxTo.column],
                                 Board.REJECT_BOX)) {
 
                     int move[] = Board.moves[dir.ordinal()];
 //                    boolean wasTunnel = false;
 //                    if (inTunnel(dir, boxTo) && !Board.is(cells[boxTo.row+move[0]][boxTo.column+move[1]], (byte) (Board.REJECT_BOX | Board.GOAL))) {
 //                        System.out.println("tunnel\n"+board);
 //                        wasTunnel = true;
 //                    }
                     
                     // Tunnel detection:
                     // If found, push as many steps in same direction as possible.
                    int numberOfTunnelMoves = 0;
                     while (inTunnel(dir, boxTo) && !Board.is(cells[boxTo.row+move[0]][boxTo.column+move[1]], (byte) (Board.REJECT_BOX | Board.GOAL))) {
                         //if(0==0)break;
 //                        System.out.print("Tunnel \n");
                        // Count tunnel moves.
                        numberOfTunnelMoves++;
                        // Update boxTo position one step.
                         boxTo.row += move[0];
                         boxTo.column += move[1];
                     }
                     
                     Position playerTo = new Position(boxTo, Board.moves[dir.reverse().ordinal()]);
 
                     // Move the player and push the box
                     board.moveBox(boxFrom, boxTo);
                     board.movePlayer(source, playerTo);
                     
 //                    if (wasTunnel) System.out.println(board);
 
                     // Process successor states
                     SearchInfo result = SearchInfo.Failed;
                     if (visitedBoards.add(board.getZobristKey())) {
                         // This state hasn't been visited before
                         result = dfs();
                     }
 
                     // Restore changes
                     board.moveBox(boxTo, boxFrom);
                     board.movePlayer(playerTo, source);
 
                     // Evaluate result
                     switch (result.status) {
                         case Solution:
                             // We have found a solution. Find the path of
                             // the move and add it to the solution.
                             board.clearFlag(Board.VISITED);

                            // Add tunnel path directions, if any.
                            for (int i = 0; i < numberOfTunnelMoves; i++) {
                                // We always walk in the same direction in a tunnel.
                                result.solution.addFirst(dir);
                            }
                            
                            // Add standard direction for this state. 
                             result.solution.addFirst(dir);
 
                            // Add path from previous player position to reachable position. 
                             result.solution.addAll(0, board.findPath(source, player));
                             return result;
                         case Inconclusive:
                             // Make the parent inconclusive too
                             inconclusive = true;
                             continue;
                         case Failed:
                             // Mark this node as failed
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
             failedBoards.add(hash);
             return SearchInfo.Failed;
         }
     }
 
     private boolean inTunnel(final Direction dir, final Position box)
     {
         // 1 or 2 must be a wall
         
         // #@#
         if (isWall(box.row, box.column + 1) && isWall(box.row, box.column - 1)) {
             // 1 2
             // #$#
             //  ^ 
             if (dir == Direction.UP && isWall(box.row - 1, box.column + 1)
                     || isWall(box.row - 1, box.column - 1)) {
                 return true;
             }
             //  v
             // #$#
             // 1 2
             if (dir == Direction.DOWN && isWall(box.row + 1, box.column + 1)
                     || isWall(box.row + 1, box.column - 1)) {
                 return true;
             }
         }
         // #
         // @
         // #
         if (isWall(box.row + 1, box.column) && isWall(box.row - 1, box.column)) {
             // 1#
             //  $<
             // 2#
             if (dir == Direction.LEFT && isWall(box.row - 1, box.column - 1)
                     || isWall(box.row + 1, box.column - 1)) {
                 return true;
             }
 
             //  #1
             // >$ 
             //  #2
             if (dir == Direction.RIGHT && isWall(box.row - 1, box.column + 1)
                     || isWall(box.row + 1, box.column + 1)) {
                 return true;
             }
 
         }
         return false;
     }
 
     private boolean isWall(final int row, final int col)
     {
         return Board.is(board.cells[row][col], Board.WALL);
     }
 
     public String solve(final Board startBoard)
     {
         failedBoards = new HashSet<Long>();
         final int lowerBound = lowerBound(startBoard);
         System.out.println("lowerBound(): " + lowerBound);
         System.out.println("IDS depth limit (progress): ");
         for (int maxDepth = lowerBound; maxDepth < DEPTH_LIMIT; maxDepth += 3) {
             System.out.print(maxDepth + ".");
 
             visitedBoards = new HashSet<Long>(failedBoards);
             remainingDepth = maxDepth;
             board = (Board) startBoard.clone();
             visitedBoards.add(board.getZobristKey());
 
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
 }
