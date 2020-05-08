 /**
  * 
  */
 package sokoban.solvers;
 
 import java.util.Collection;
 import java.util.Deque;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Set;
 
 import sokoban.Board;
 import sokoban.Position;
 import sokoban.Board.Direction;
 import sokoban.ReachableBox;
 
 /**
  * A solver that pushes boxes around.
  */
 public class Pusher implements Solver
 {
     public static int generatedNodes = 0;
 
     /**
     * Small little container class for a search node
     * Each search node contains the path to it, the board its handling and the
      * parent.
      */
     private class SearchNode
     {
         /**
         * The path to this search node
          */
         public final Deque<Direction> path;
         /**
         * The board at this search node
          */
         public final Board board;
         /**
         * The parent search node
          */
         public final SearchNode parent;
 
         /**
         * Initialize a search node
          * 
         * @param path The path to this search node
         * @param board The board at this search node
         * @param parent The parent search node
          */
         public SearchNode(Deque<Direction> path, Board board, SearchNode parent)
         {
             this.path = path;
             this.board = (Board) board.clone();
             this.parent = parent;
             Pusher.generatedNodes++;
         }
 
         /**
          * Create a new SearchNode from a parent and moves the player in the
          * specified direction.
          * 
          * @param newPath The path from the parent to this node
          * @param dir The direction to move the player in.
          * @param parent The parent state.
          */
         public SearchNode(Deque<Direction> newPath, Board.Direction dir,
                 Position playerStartPosition, SearchNode parent)
         {
             // TODO it is possible to generate the path from parent when needed.
             this(newPath, parent.board, parent);
             board.playerCol = playerStartPosition.column;
             board.playerRow = playerStartPosition.row;
             path.add(dir);
             board.move(dir);
 
             board.updateTopLeftReachable();
         }
     }
 
     public int getIterationsCount()
     {
         return generatedNodes;
     }
 
     public String solve(Board board)
     {
         // Set of visited boards, including the player position
         Set<Board> visitedBoards = new HashSet<Board>();
         Queue<SearchNode> queue = new LinkedList<SearchNode>();
         queue.add(new SearchNode(new LinkedList<Direction>(), board, null));
         SearchNode node;
         do {
             if ((node = queue.poll()) == null) {
                 return null;
             }
 
             if (visitedBoards.contains(node.board)) {
                 continue; // This node has been visited so skip it
             }
             else {
                 visitedBoards.add(node.board);
             }
 
             // TODO we could check that we only add unvisited nodes
             queue.addAll(getAllSuccessorStates(node));
         }
         while (node.board.getRemainingBoxes() != 0);
 
         // Go through the path and print it
         SearchNode tmp = node;
         Deque<Direction> path = new LinkedList<Direction>();
         Iterator<Direction> descIt;
 
         while (tmp != null) {
             descIt = tmp.path.descendingIterator();
             while (descIt.hasNext()) {
                 path.addFirst(descIt.next());
             }
             tmp = tmp.parent;
         }
         return Board.solutionToString(path);
     }
 
     /**
      * Finds all possible new states, expands and returns them for a specific
      * search node.
      * 
      * @param board The board
      * @return A list of new SearchNodes
      */
     private Collection<SearchNode> getAllSuccessorStates(SearchNode node)
     {
         Collection<SearchNode> successors = new LinkedList<SearchNode>();
         byte[][] cells = node.board.cells;
 
         for (ReachableBox reachable : node.board.findReachableBoxSquares()) {
             for (Direction dir : Board.Direction.values()) {
                 Position from = new Position(reachable.position,
                         Board.moves[dir.ordinal()]);
                 Position to = new Position(from, Board.moves[dir.ordinal()]);
                 if (Board.is(cells[from.row][from.column], Board.BOX)
                         && !Board
                                 .is(cells[to.row][to.column], Board.REJECT_BOX)) {
 
                     Deque<Direction> playerPath = new LinkedList<Direction>(
                             reachable.path);
                     // playerPath.addLast(dir);
 
                     successors.add(new SearchNode(playerPath, dir,
                             reachable.position, node));
                 }
             }
         }
 
         return successors;
     }
 }
