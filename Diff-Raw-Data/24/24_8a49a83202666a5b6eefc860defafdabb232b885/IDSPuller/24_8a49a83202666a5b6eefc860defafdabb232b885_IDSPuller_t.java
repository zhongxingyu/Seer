 package sokoban.solvers;
 
 import java.util.Collection;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import org.omg.PortableInterceptor.NON_EXISTENT;
 
 import sokoban.Board;
 import sokoban.Position;
 import sokoban.SearchInfo;
 import sokoban.SearchStatus;
 import sokoban.Board.Direction;
 
 /**
  * A solver that pulls boxes around (it finds a path from the goal to the
  * start) with iterative deepening and hashing to avoid duplicate states.
  */
 public class IDSPuller extends IDSCommon implements Solver
 {
     private int depth, maxDepth;
     
     private int failedGoalTests;
     private int numLeafNodes;
     private int lastLeafCount;
 
     // Extra information for the puller
     private int boxesNotInStart, initialBoxesNotInStart;
     private boolean[][] boxStart;
     private Position playerStart;
 
     public IDSPuller(Board startBoard, HashSet<Long> failedBoards,
             HashMap<Long, BoxPosDir> pusherStatesMap,
             HashMap<Long, BoxPosDir> pullerStatesMap)
     {
         super(startBoard, failedBoards, pusherStatesMap, pullerStatesMap);
 
         numLeafNodes = 0;
         lastLeafCount = -1;
 
        this.startBoard = (Board) startBoard.clone();
        reverseBoard(this.startBoard);
     }
 
     /**
      * Returns the result of the DFS with the specified maximum depth.
      * 
      * @param maxDepth The maximum depth allowed for this DFS.
      * @param startBoard The start board.
      * @return A SearchInfo result.
      */
     public SearchInfo dfs(int maxDepth)
     {
         depth = 0;
         this.maxDepth = maxDepth;
         failedGoalTests = 0;
         numLeafNodes = 0;
         forceDirection = false;
         
         board = (Board) startBoard.clone();
         visitedBoards = new HashSet<Long>(failedBoards);
         visitedBoards.add(board.getZobristKey());
         board = (Board) startBoard.clone();
        boxesNotInStart = initialBoxesNotInStart;
         
         return dfs();
     }
 
     private boolean forceDirection = false;
     private Direction forcedDirection;
 
     /**
      * Recursive Depth-First algorithm
      * 
      * @return
      */
     private SearchInfo dfs()
     {
         generatedNodes++;
 
         if (boxesNotInStart == 0) {
             // Found a solution, try to go back to the start
             Position player = board.positions[board.getPlayerRow()][board
                     .getPlayerCol()];
             Deque<Direction> path = board.findPath(playerStart, player);
             if (path != null) {
                 SearchInfo result = SearchInfo.emptySolution();
                 result.solution.addAll(0, path);
                 return result;
             }
         }
 
         if (depth >= maxDepth) {
             failedGoalTests += boxesNotInStart;
             numLeafNodes++;
             return SearchInfo.Inconclusive;
         }
 
         // True if at least one successor tree was inconclusive.
         boolean inconclusive = false;
 
         long hash = board.getZobristKey();
 
         final Position source = board.positions[board.getPlayerRow()][board
                 .getPlayerCol()];
         depth++;
 
         final byte[][] cells = board.cells;
         for (final Position boxTo : findReachableBoxSquares()) {
             for (final Direction dir : Board.Direction.values()) {
                 if (forceDirection && dir != forcedDirection) {
                     continue;
                 }
 
                 final Position boxFrom = board.getPosition(boxTo,
                         Board.moves[dir.ordinal()]);
                 final Position playerTo = board.getPosition(boxTo,
                         Board.moves[dir.reverse().ordinal()]);
 
                 if (Board.is(cells[boxFrom.row][boxFrom.column], Board.BOX)
                         && !Board.is(cells[boxTo.row][boxTo.column],
                                 Board.REJECT_BOX)
                         && !Board.is(cells[playerTo.row][playerTo.column],
                                 Board.REJECT_PULL)) {
                     // The move is possible
 
                     // Move the player and pull the box
                     board.moveBox(boxFrom, boxTo);
                     board.movePlayer(playerTo);
 
                     if (boxStart[boxFrom.row][boxFrom.column])
                         boxesNotInStart++;
                     if (boxStart[boxTo.row][boxTo.column])
                         boxesNotInStart--;
 
                     // Process successor states
                     SearchInfo result = SearchInfo.Failed;
                     if (visitedBoards.add(board.getZobristKey())) {
                         // This state hasn't been visited before
                         ourStatesMap.put(board.getZobristKey(), new BoxPosDir(
                                 dir, boxTo, playerTo));
                         result = dfs();
                     }
 
                     // Restore changes
                     board.moveBox(boxTo, boxFrom);
                     board.movePlayer(source);
 
                     if (boxStart[boxFrom.row][boxFrom.column])
                         boxesNotInStart--;
                     if (boxStart[boxTo.row][boxTo.column])
                         boxesNotInStart++;
 
                     // Evaluate result
                     switch (result.status) {
                         case Solution:
                             // We have found a solution. Find the reverse
                             // path of the move and add it to the solution.
                             result.solution.addLast(dir);
                             if (depth > 1) {
                                 result.solution.addAll(board.findPath(boxTo,
                                         source));
                             }
                             depth--;
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
 
         depth--;
 
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
 
     private Collection<Position> findReachableBoxSquares()
     {
         long hash = board.getZobristKey();
         if (otherStatesMap.containsKey(hash)) {
             Collection<Position> boxes = new HashSet<Position>(1);
             BoxPosDir nextState = otherStatesMap.get(hash);
 
             // Add next reachable square and force the correct direction.
             boxes.add(nextState.box);
             forceDirection = true;
             forcedDirection = nextState.dir;
 
             return boxes;
         }
         else if (depth == 1) {
             forceDirection = false;
             Collection<Position> boxes = new HashSet<Position>(board.boxCount);
             for (int row = 1; row < board.height - 1; row++) {
                 for (int col = 1; col < board.width - 1; col++) {
                     if (!Board.is(board.cells[row][col], Board.BOX)) {
                         continue;
                     }
 
                     for (Direction dir : Direction.values()) {
                         int spaceRow = row + Board.moves[dir.ordinal()][0];
                         int spaceCol = col + Board.moves[dir.ordinal()][1];
                         if (spaceRow > 0 && spaceRow < board.height - 1
                                 && spaceCol > 0 && spaceCol < board.width - 1) {
                             boxes.add(board.positions[spaceRow][spaceCol]);
                         }
                     }
                 }
             }
             return boxes;
         }
         else {
             forceDirection = false;
             return board.findReachableBoxSquares();
         }
     }
 
     public String solve(final Board startBoard)
     {
         failedBoards = new HashSet<Long>();
         final int lowerBound = lowerBound(startBoard);
         System.out.println("lowerBound(): " + lowerBound);
         System.out.println("IDS depth limit (progress): ");
 
         reverseBoard(startBoard);
         
         lastLeafCount = -1;
         for (maxDepth = lowerBound; maxDepth < DEPTH_LIMIT; nextDepth(lowerBound)) {
             System.out.print(maxDepth + ".");
 
             visitedBoards = new HashSet<Long>(failedBoards);
             depth = 0;
             board = (Board) startBoard.clone();
             boxesNotInStart = initialBoxesNotInStart;
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
         //System.out.println("fGT "+failedGoalTests+"   nLN "+numLeafNodes+" ("+lastLeafCount+")  boxC "+board.boxCount+"  lB "+lowerBound);
         // If we have many boxes in the goals we can take a larger step
        int nonGoalPerNode = failedGoalTests / numLeafNodes;
         //System.out.println("nonGoalPerNode: "+nonGoalPerNode);
         int goalStep = lowerBound / Math.max(board.boxCount - nonGoalPerNode + 1, 1);
         
         // If we have pruned so many nodes we have less leaf nodes this
         // time we take a larger step
         int depthChangeStep = 10 * (numLeafNodes / lastLeafCount);
         
         lastLeafCount = numLeafNodes;
         int step = Math.max(3, Math.max(goalStep, depthChangeStep));
 
         maxDepth += step;
         return maxDepth;
     }
 
     private void reverseBoard(final Board board)
     {
         // Store starting positions
         playerStart = board.positions[board.getPlayerRow()][board
                 .getPlayerCol()];
         boxStart = new boolean[board.height][board.width];
         initialBoxesNotInStart = board.boxCount;
         for (int row = 0; row < board.height; row++) {
             for (int column = 0; column < board.width; column++) {
                 if (Board.is(board.cells[row][column], Board.BOX)) {
                     if (Board.is(board.cells[row][column], Board.GOAL)) {
                         initialBoxesNotInStart--;
                     }
                     board.removeBox(board.positions[row][column]);
                     boxStart[row][column] = true;
                 }
             }
         }
 
         // Put the boxes in the goals
         for (int row = 0; row < board.height; row++) {
             for (int column = 0; column < board.width; column++) {
                 if (Board.is(board.cells[row][column], Board.GOAL)) {
                     board.addBox(board.positions[row][column]);
                 }
             }
         }
 
         board.forceReachabilityUpdate();
     }
 
 }
