 package sokoban.solvers;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import sokoban.Board;
 import sokoban.Position;
 import sokoban.SearchInfo;
 import sokoban.SearchStatus;
 import sokoban.Board.Direction;
 
 /**
  * A solver that pushes boxes around with iterative deepening and
  * hashing to avoid duplicate states.
  */
 public class IDSPusher extends IDSCommon implements Solver
 {
 
     private int remainingDepth;
 
     private int failedGoalTests;
     private int numLeafNodes;
     private int lastLeafCount;
     private int maxDepth;
 
     public IDSPusher(Board startBoard,
             HashSet<Long> failedBoards,
             HashMap<Long, BoxPosDir> pusherStatesMap,
             HashMap<Long, BoxPosDir> pullerStatesMap)
     {
         super(startBoard, failedBoards, pusherStatesMap, pullerStatesMap);
 
         numLeafNodes = 0;
         lastLeafCount = -1;
     }
 
     public IDSPusher()
     {
     }
 
     /**
      * Returns the result of the DFS with the specified maximum depth.
      * 
      * @param maxDepth The maximum depth allowed for this DFS.
      * @return A SearchInfo result.
      */
     public SearchInfo dfs(int maxDepth)
     {   
         remainingDepth = maxDepth;
         failedGoalTests = 0;
         numLeafNodes = 0;
        this.maxDepth = maxDepth; 
         
         board = (Board) startBoard.clone();
         visitedBoards = new HashSet<Long>(failedBoards);
         visitedBoards.add(board.getZobristKey());
         
         return dfs();
     }
 
     /**
      * Recursive Depth-First algorithm
      * 
      * @return
      */
     private SearchInfo dfs()
     {
         generatedNodes++;
 
         if (board.getRemainingBoxes() == 0) {
             // Found a solution
             return SearchInfo.emptySolution();
         }
 
         final long hash = board.getZobristKey();
 
         BoxPosDir collision = otherStatesMap.get(hash);
 
         if (remainingDepth <= 0) {
             failedGoalTests += board.getRemainingBoxes();
             numLeafNodes++;
             return SearchInfo.Inconclusive;
         }
 
         // True if at least one successor tree was inconclusive.
         boolean inconclusive = false;
 
         final Position source = board.positions[board.getPlayerRow()][board
                 .getPlayerCol()];
         remainingDepth--;
 
         final byte[][] cells = board.cells;
         for (final Position player : board.findReachableBoxSquares()) {
             for (final Direction dir : Board.Direction.values()) {
                 if (collision != null) {
                     // We reached a state from the other end (IDSPuller)
                     if (dir != collision.dir) {
                         // This is successor state isn't on the path from
                         // the other end.
                         continue;
                     }
                 }
                 
                 final Position boxFrom = board.getPosition(player,
                         Board.moves[dir.ordinal()]);
                 Position boxTo = board.getPosition(boxFrom, Board.moves[dir
                         .ordinal()]);
 
                 // Check if the move is possible
                 if (Board.is(cells[boxFrom.row][boxFrom.column], Board.BOX)
                         && !Board.is(cells[boxTo.row][boxTo.column],
                                 Board.REJECT_BOX)) {
 
                     final int move[] = Board.moves[dir.ordinal()];
 
                     // Tunnel detection:
                     // If found, push as many steps in same direction as
                     // possible.
                     int numberOfTunnelMoves = 0;
                     /* TODO FIX TUNNEL
                      * while (inTunnel(dir, boxTo)
                             && !Board.is(
                                     cells[boxTo.row + move[0]][boxTo.column
                                             + move[1]],
                                     (byte) (Board.REJECT_BOX | Board.GOAL))) {
                         // Count tunnel moves.
                         numberOfTunnelMoves++;
                         // Update boxTo position one step.
                         boxTo = board.getPosition(boxTo, move);
                     }*/
 
                     final Position playerTo = board.getPosition(boxTo,
                             Board.moves[dir.reverse().ordinal()]);
 
                     // Move the player and push the box
                     board.moveBox(boxFrom, boxTo);
                     board.movePlayer(playerTo);
 
                     SearchInfo result = SearchInfo.Failed;
                     // Check if we got a freeze deadlock
                     if (!freezeDeadlock(boxTo, DEADLOCK_BOTH,
                             new HashSet<Position>())) {
                         if (visitedBoards.add(board.getZobristKey())) {
 
                            ourStatesMap.put(board.getZobristKey(),
                                     new BoxPosDir(dir, boxFrom, source)
                            );
                             
                             result = dfs();
                         }
                     }
 
                     // Restore changes
                     board.moveBox(boxTo, boxFrom);
                     board.movePlayer(source);
 
                     // Evaluate result
                     switch (result.status) {
                         case Solution:
                             // We have found a solution. Find the path of
                             // the move and add it to the solution.
                             board.clearFlag(Board.VISITED);
 
                             // Add tunnel path directions, if any.
                             /*for (int i = 0; i < numberOfTunnelMoves; i++) {
                                 // We always walk in the same direction in a
                                 // tunnel.
                                 result.solution.addFirst(dir);
                             }*/
 
                             // Add standard direction for this state.
                             result.solution.addFirst(dir);
 
                             // Add path from previous player position to
                             // reachable position.
                             result.solution.addAll(0, board.findPath(source,
                                     player));
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
 
     /**
      * Checks if the given box position is a placement that has no influence on
      * the board, i.e. if it is inside a tunnel.
      * 
      * @param dir The direction in which the box was pushed in order to get
      *            where it is.
      * @param box The position of the box after it has been pushed in the given
      *            direction.
      * @return True if the box is has gone into a tunnel, which has no influence
      *         on the board, and can therefore be pushed all the way outside.
      */
     private boolean inTunnel(final Direction dir, final Position box)
     {
         // #v#
         // 1$2
         if (dir == Direction.DOWN && isWall(box.row - 1, box.column - 1)
                 && isWall(box.row - 1, box.column + 1)) {
             // 1 or 2 above is a wall (or both)
             return isWall(box.row, box.column - 1)
                     || isWall(box.row, box.column + 1);
         }
 
         // 1$2
         // #^#
         if (dir == Direction.UP && isWall(box.row + 1, box.column - 1)
                 && isWall(box.row + 1, box.column + 1)) {
             // 1 or 2 above is a wall (or both)
             return isWall(box.row, box.column - 1)
                     || isWall(box.row, box.column + 1);
         }
 
         // #1
         // >$
         // #2
         if (dir == Direction.RIGHT && isWall(box.row - 1, box.column - 1)
                 && isWall(box.row + 1, box.column - 1)) {
             // 1 or 2 above is a wall (or both)
             return isWall(box.row - 1, box.column)
                     || isWall(box.row + 1, box.column);
         }
 
         // 1#
         // $<
         // 2#
         if (dir == Direction.LEFT && isWall(box.row - 1, box.column + 1)
                 && isWall(box.row + 1, box.column + 1)) {
             // 1 or 2 above is a wall (or both)
             return isWall(box.row - 1, box.column)
                     || isWall(box.row + 1, box.column);
         }
 
         return false;
     }
 
     /**
      * Checks if the position specified by the given row and column is a wall.
      * 
      * @param row The row.
      * @param col The column.
      * @return True if the given position is a wall, otherwise false.
      */
     private boolean isWall(final int row, final int col)
     {
         return Board.is(board.cells[row][col], Board.WALL);
     }
 
     public String solve(final Board startBoard)
     {
         failedBoards = new HashSet<Long>();
         final long startTime = System.currentTimeMillis();
         final int lowerBound = lowerBound(startBoard);
         System.out.println("lowerBound(): " + lowerBound + " took "
                 + (System.currentTimeMillis() - startTime) + " ms");
         System.out.println("IDS depth limit (progress): ");
         
         lastLeafCount = -1;
         for (maxDepth = lowerBound; maxDepth < DEPTH_LIMIT; nextDepth(lowerBound)) {
             System.out.print(maxDepth + ".");
 
             visitedBoards = new HashSet<Long>(failedBoards);
             remainingDepth = maxDepth;
             board = (Board) startBoard.clone();
             visitedBoards.add(board.getZobristKey());
             failedGoalTests = 0;
 
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
     
     public int nextDepth(int lowerBound) {
         // If we have many boxes in the goals we can take a larger step
         int nonGoalPerNode = failedGoalTests / (numLeafNodes + 1);
         int goalStep = lowerBound / (board.boxCount - nonGoalPerNode + 1);
         
         // If we have pruned so many nodes we have less leaf nodes this
         // time we take a larger step
         int depthChangeStep = 10 * (numLeafNodes / lastLeafCount);
         
         lastLeafCount = numLeafNodes;
         int step = Math.max(3, Math.max(goalStep, depthChangeStep));
         
         maxDepth += step;
         return maxDepth;
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
 
     private boolean freezeDeadlock(final Position box, final byte type,
             final HashSet<Position> visited)
 
     {
 
         visited.add(box);
 
         if (Board.is(board.cells[box.row][box.column], Board.GOAL)) {
             return false;
         }
         
         // TODO: Do not move the box before checking freeze deadlock, creates
         // deadlock with it self.
         if (type == DEADLOCK_BOTH) {
             boolean blockedVertical = false;
             boolean blockedHorizontal = false;
 
             // If there is a wall to the left or right
             if (Board.is(board.cells[box.row][box.column + 1], Board.WALL)
                     || Board.is(board.cells[box.row][box.column - 1],
                             Board.WALL)) {
                 blockedHorizontal = true;
             }
 
             // If there is a wall to the top or bottom
             if (Board.is(board.cells[box.row + 1][box.column], Board.WALL)
                     || Board.is(board.cells[box.row - 1][box.column],
                             Board.WALL)) {
                 blockedVertical = true;
             }
 
             // If there is a box_trap (simple deadlock check) to the left and
             // right
             if (Board.is(board.cells[box.row][box.column + 1], Board.BOX_TRAP)
                     && Board.is(board.cells[box.row][box.column - 1],
                             Board.BOX_TRAP)) {
                 blockedHorizontal = true;
             }
 
             // If there is a box_trap (simple deadlock check) to the top and
             // bottom
             if (Board.is(board.cells[box.row + 1][box.column], Board.BOX_TRAP)
                     && Board.is(board.cells[box.row - 1][box.column],
                             Board.BOX_TRAP)) {
                 blockedVertical = true;
             }
 
             // If we are both blocked horizontal and vertical, return deadlock
             if (blockedVertical && blockedHorizontal) {
 
                 return true;
                 // Only horizontal
             }
             else if (!blockedVertical && blockedHorizontal) {
                 if (Board.is(board.cells[box.row + 1][box.column], Board.BOX)) {
                     final Position tempPos = board.positions[box.row + 1][box.column];
 
                     if (!visited.contains(tempPos)) {
                         return freezeDeadlock(tempPos, DEADLOCK_HORIZONTAL,
                                 visited);
                     }
                 }
 
                 if (Board.is(board.cells[box.row - 1][box.column], Board.BOX)) {
                     final Position tempPos = board.positions[box.row - 1][box.column];
                     if (!visited.contains(tempPos)) {
                         return freezeDeadlock(tempPos, DEADLOCK_HORIZONTAL,
                                 visited);
                     }
                 }
                 // Only vertical
             }
             else if (!blockedHorizontal && blockedVertical) {
                 if (Board.is(board.cells[box.row][box.column + 1], Board.BOX)) {
                     final Position tempPos = board.positions[box.row][box.column + 1];
                     if (!visited.contains(tempPos)) {
                         return freezeDeadlock(tempPos, DEADLOCK_VERTICAL,
                                 visited);
                     }
                 }
 
                 if (Board.is(board.cells[box.row][box.column - 1], Board.BOX)) {
                     final Position tempPos = board.positions[box.row][box.column - 1];
 
                     if (!visited.contains(tempPos)) {
                         return freezeDeadlock(tempPos, DEADLOCK_VERTICAL,
                                 visited);
                     }
                 }
                 // No deadlock
             }
             else {
                 return false;
             }
             // HORIZONTAL CHECK
         }
         else if (type == DEADLOCK_HORIZONTAL) {
             // Check goal
             if (Board.is(board.cells[box.row][box.column],
                     (byte) (Board.GOAL & Board.BOX))) {
                 return false;
             }
 
             // If there is a wall to the left or right
             if (Board.is(board.cells[box.row][box.column + 1], Board.WALL)
                     || Board.is(board.cells[box.row][box.column - 1],
                             Board.WALL)) {
                 return true;
             }
 
             // If there is a box_trap (simple deadlock check) to the left and
             // right
             if (Board.is(board.cells[box.row][box.column + 1], Board.BOX_TRAP)
                     && Board.is(board.cells[box.row][box.column - 1],
                             Board.BOX_TRAP)) {
                 return true;
             }
 
             if (Board.is(board.cells[box.row][box.column + 1], Board.BOX)) {
                 final Position tempPos = board.positions[box.row][box.column + 1];
                 if (!visited.contains(tempPos)) {
                     return freezeDeadlock(tempPos, DEADLOCK_VERTICAL, visited);
                 }
             }
 
             if (Board.is(board.cells[box.row][box.column - 1], Board.BOX)) {
                 final Position tempPos = board.positions[box.row][box.column - 1];
                 if (!visited.contains(tempPos)) {
                     return freezeDeadlock(tempPos, DEADLOCK_VERTICAL, visited);
                 }
             }
             return false;
             // VERTICAL CHECK
         }
         else if (type == DEADLOCK_VERTICAL) {
             // Check goal
             if (Board.is(board.cells[box.row][box.column],
                     (byte) (Board.GOAL & Board.BOX))) {
                 return false;
             }
 
             // If there is a wall to the top or bottom
             if (Board.is(board.cells[box.row + 1][box.column], Board.WALL)
                     || Board.is(board.cells[box.row - 1][box.column],
                             Board.WALL)) {
                 return true;
             }
 
             // If there is a box_trap (simple deadlock check) to the top and
             // bottom
             if (Board.is(board.cells[box.row + 1][box.column], Board.BOX_TRAP)
                     && Board.is(board.cells[box.row - 1][box.column],
                             Board.BOX_TRAP)) {
                 return true;
             }
 
             if (Board.is(board.cells[box.row + 1][box.column], Board.BOX)) {
                 final Position tempPos = board.positions[box.row + 1][box.column];
                 if (!visited.contains(tempPos)) {
                     return freezeDeadlock(tempPos, DEADLOCK_HORIZONTAL, visited);
                 }
             }
 
             if (Board.is(board.cells[box.row - 1][box.column], Board.BOX)) {
                 final Position tempPos = board.positions[box.row - 1][box.column];
                 if (!visited.contains(tempPos)) {
                     return freezeDeadlock(tempPos, DEADLOCK_HORIZONTAL, visited);
                 }
             }
             return false;
         }
         return false;
     }
 }
