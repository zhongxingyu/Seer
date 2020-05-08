 package sokoban;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Deque;
 import java.util.HashSet;
 import java.util.LinkedList;
 
 import sokoban.ReachableBox;
 
 /**
  *
  */
 public class Board implements Cloneable
 {
     // Input values
     /**
      * Value for a WALL on the board
      */
     public final static byte WALL = 0x01;
     /**
      * Value for a BOX on the board
      */
     public final static byte BOX = 0x02;
     /**
      * Value for a GOAL position on the board
      */
     public final static byte GOAL = 0x04;
 
     // Generated values
     /**
      * Boxes will get stuck in this square
      */
     public final static byte BOX_TRAP = 0x08;
     /**
      * The player has already passed this cell the since last move
      */
     public final static byte VISITED = 0x10;
     /**
      * Starting position of a box
      */
     public final static byte BOX_START = 0x20;
     /**
      * Starting position of the player
      */
     public final static byte PLAYER_START = 0x40;
 
     // Bitmasks
     /**
      * A bitmask that says that a cell can't be walked into when
      * pushing, but not pulling, is allowed.
      */
     public final static byte REJECT_WALK = WALL | VISITED;
     /**
      * A bitmask that says that a cell can't be walked into when
      * pulling, but not pushing, is allowed.
      */
     public final static byte REJECT_PULL = WALL | VISITED | BOX;
     /**
      * A bitmask that says that a box can't be moved into the cell, for one or
      * more of the following reasons:
      * <ul>
      * <li>The cell is a wall.</li>
      * <li>The cell contains another box.</li>
      * <li>The cell is a box trap, meaning that the box could never be moved
      * away from there.</li>
      * </ul>
      */
     public final static byte REJECT_BOX = WALL | BOX | BOX_TRAP;
 
     /**
      * Don't move boxes here at any time!
      */
     public final static byte WALL_OR_TRAP = WALL | BOX_TRAP;
 
     /**
      * All four allowed moves: { row, column }
      */
     public static final int moves[][] = { { -1, 0 }, { 1, 0 }, { 0, -1 },
             { 0, 1 } };
 
     public enum Direction {
         UP, DOWN, LEFT, RIGHT,
     }
 
     /**
      * The width of the board
      */
     public final int width;
     /**
      * The height of the board
      */
     public final int height;
 
     /**
      * A bitmask for the input cell values.
      */
     public final static byte INPUT_CELL_MASK = WALL | GOAL | BOX;
 
     /**
      * The actual board
      */
     public byte cells[][];
     /**
      * The column at which the player resides
      */
     public int playerCol;
     /**
      * The row at which the player resides
      */
     public int playerRow;
     private int remainingBoxes;
     private int boxesInStart;
     
     /**
      * The topmost, leftmost square the player can reach. Please update with
      * updateTopLeftReachable() after the board has changed and before it's
      * hashed.
      */
     public int topLeftReachable;
 
     /**
      * Initialize a new board
      * 
      * @param width
      *            The width of the board
      * @param height
      *            The height of the board
      * @param playerRow
      *            The Y position of the player
      * @param playerCol
      *            The X position of the player
      */
     public Board(int width, int height, int playerRow, int playerCol)
     {
         cells = new byte[height][width];
         this.width = width;
         this.height = height;
         this.playerCol = playerCol;
         this.playerRow = playerRow;
     }
 
     /**
      * Gets the number of goal cells that don't have box yet.
      * 
      * @return The number of remaining boxes
      */
     public int getRemainingBoxes()
     {
         return remainingBoxes;
     }
 
     /**
      * Returns true if the square has any of the bits in the mask.
      * 
      * @param square The square at which to check for the bitmask
      * @param mask The mask to check against
      * @return true if the square and the mask matches, false otherwise
      */
     public static boolean is(byte square, byte mask)
     {
         return (square & mask) != 0;
     }
 
     /**
      * Updates some variables after a board has been loaded.
      */
     public void refresh()
     {
         countBoxes();
         markNonBoxSquares();
         updateTopLeftReachable();
     }
 
     /**
      * Counts the boxes that are not in a goal, and updates remainingBoxes.
      */
     private void countBoxes()
     {
         int remaining = 0;
         for (int row = 0; row < height; row++) {
             for (int col = 0; col < width; col++) {
                 if ((cells[row][col] & (BOX | GOAL)) == BOX) {
                     remaining++;
                 }
             }
         }
         remainingBoxes = remaining;
     }
 
     /**
      * Print the current board
      * 
      * @return The string representing the board
      */
     @Override
     public String toString()
     {
         StringBuilder sb = new StringBuilder(width * height + height);
         for (int i = 0; i < height; ++i) {
             for (int j = 0; j < width; ++j) {
                 sb.append(cellToChar(i, j));
             }
             sb.append("\n");
         }
         return sb.toString();
     }
 
     /**
      * Returns the official Sokoban char for the given internal value.
      * 
      * @param value
      *            The internal byte representation of a cell.
      * @return The character that is represented by the given internal value.
      */
     private char valueToChar(byte value)
     {
         switch (value & Board.INPUT_CELL_MASK) {
             case Board.WALL:
                 return '#';
             case Board.GOAL:
                 return '.';
             case Board.BOX:
                 return '$';
             case Board.GOAL | Board.BOX:
                 return '*';
             default:
                 return ((value & Board.BOX_TRAP) == 0 ? ' ' : '-');
         }
     }
 
     /**
      * Returns the Sokoban char for the given cell. Takes care of player
      * position.
      * 
      * @param row
      *            The row index of the cell to specify.
      * @param col
      *            The column index of the cel to specify.
      * @return The Sokoban char for the specified cell according to
      *         http://www.sokobano.de/wiki/index.php?title=Level_format.
      */
     public char cellToChar(int row, int col)
     {
         byte cell = cells[row][col];
 
         // Check for some errors first
         if (is(cell, BOX_TRAP) && is(cell, GOAL))
             return 'E'; // Goal on trap = error!
 
         if (is(cell, BOX_TRAP) && is(cell, BOX))
             return 'e'; // Box in trap = error!
 
         // No errors detected in this cell
         if (playerRow == row && playerCol == col) {
             return is(cell, Board.GOAL) ? '+' : '@';
         }
         else {
             return valueToChar(cell);
         }
     }
 
     private static final char moveChars[] = { 'U', 'D', 'L', 'R' };
 
     /**
      * Print the solution.
      * 
      * @param solution A list of board directions
      * @return The solution as a string
      */
     public static String solutionToString(Deque<Board.Direction> solution)
     {
         StringBuilder sb = new StringBuilder(2 * solution.size());
         for (Board.Direction move : solution) {
             sb.append(moveChars[move.ordinal()]);
             sb.append(' ');
         }
         return sb.toString();
     }
 
     /**
      * Marks squares that boxes would get stuck in (dead squares / box traps).
      */
     private void markNonBoxSquares()
     {
         // Mark corners
         for (int row = 1; row < height - 1; row++) {
             for (int col = 1; col < width - 1; col++) {
                 // Goal squares usually aren't traps
                 // (if the right block is placed there)
                 if (is(cells[row][col], GOAL))
                     continue;
 
                 boolean horizontalBlocked = is(cells[row - 1][col], WALL)
                         || is(cells[row + 1][col], WALL);
                 boolean verticalBlocked = is(cells[row][col - 1], WALL)
                         || is(cells[row][col + 1], WALL);
 
                 // This is a corner
                 if (horizontalBlocked && verticalBlocked)
                     cells[row][col] |= BOX_TRAP;
             }
         }
 
         // Find dead lines between dead squares
         boolean changed;
         do {
             changed = false;
             for (int row = 1; row < height - 1; row++) {
                 for (int col = 1; col < width - 1; col++) {
                     // Always start at a box trap
                     if (!is(cells[row][col], BOX_TRAP))
                         continue;
 
                     // Look to the right
                     for (int right = col + 1; right < width - 1; right++) {
                         // Stop at goals
                         if (is(cells[row][right], GOAL))
                             break;
 
                         // Stop and mark cells if there's either wall or a trap
                         // cell
                         if (is(cells[row][right], WALL_OR_TRAP)) {
                             // Mark cells
                             for (int i = col + 1; i < right; i++) {
                                 cells[row][i] |= BOX_TRAP;
                                 changed = true;
                             }
                             break;
                         }
 
                         // Check if there's a way to move out the block
                         if (!is(cells[row - 1][right], WALL)
                                 && !is(cells[row + 1][right], WALL))
                             break;
                     }
 
                     // Look below
                     for (int down = row + 1; down < height - 1; down++) {
                         // Stop at goals
                         if (is(cells[down][col], GOAL))
                             break;
 
                         // Stop and mark cells if there's either wall or a trap
                         // cell
                         if (is(cells[down][col], WALL_OR_TRAP)) {
                             // Mark cells
                             for (int i = row + 1; i < down; i++) {
                                 cells[i][col] |= BOX_TRAP;
                                 changed = true;
                             }
                             break;
                         }
 
                         // Check if there's a way to move out the block
                         if (!is(cells[down][col - 1], WALL)
                                 && !is(cells[down][col + 1], WALL))
                             break;
                     }
                 }
             }
         }
         while (changed);
     }
 
     /**
      * Returns a deep copy of this board.
      */
     public Object clone()
     {
         try {
             Board copy = (Board) super.clone();
 
             // Deep copy cells
             copy.cells = new byte[height][width];
             for (int row = 0; row < height; ++row) {
                 // Fastest way according to the following web page:
                 // http://www.javapractices.com/topic/TopicAction.do?Id=3
                 System.arraycopy(this.cells[row], 0, copy.cells[row], 0, width);
             }
 
             return copy;
         }
         catch (CloneNotSupportedException e) {
             throw new Error(
                     "This should not occur since we implement Cloneable");
         }
     }
 
     /**
      * Move the player in the specified direction
      * 
      * @param dir The direction to move the player in
      */
     public void move(Direction dir)
     {
         int move[] = moves[dir.ordinal()];
 
         // The cell that the player moves to
         int row = playerRow + move[0];
         int col = playerCol + move[1];
 
         // The cell that the box (if any) moves to
         int row2 = playerRow + 2 * move[0];
         int col2 = playerCol + 2 * move[1];
 
         // Mark as visited
         cells[playerRow][playerCol] |= VISITED;
 
         // Move player
         playerRow = row;
         playerCol = col;
 
         if (is(cells[row][col], BOX)) {
             // Move box
             cells[row][col] &= ~BOX;
             cells[row2][col2] |= BOX;
 
             // Keep track of remaining boxes
             remainingBoxes += (is(cells[row][col], GOAL) ? +1 : 0)
                     + (is(cells[row2][col2], GOAL) ? -1 : 0);
             // System.out.println("remaining boxes: "+remainingBoxes);
 
             // Clear "visited" marks
             clearVisited();
         }
     }
     
     /**
      * Moves a box and updates remainingBoxes. This method ignores the
      * player position.
      */
     public void moveBox(Position from, Position to)
     {
         cells[from.row][from.column] &= ~BOX;
        cells[to.row][to.column] |= BOX;
         
         if (is(cells[from.row][from.column], GOAL))
             remainingBoxes++;
         if (is(cells[to.row][to.column], GOAL))
             remainingBoxes--;
     }
     
     /**
      * Removes the VISITED flag from all squares. The VISITED flag is typically
      * used temporarily by algorithms to find shortest paths and avoid loops.
      */
     public void clearVisited() {
         for (int r = 0; r < height; r++) {
             for (int c = 0; c < width; c++) {
                 cells[r][c] &= ~VISITED;
             }
         }
     }
 
     /**
      * Gets a hash value of all of the boxes. This does not include
      * the player position.
      * 
      * This works by XOR:ing the box spacing when the cells are laid
      * out on a line. To use the bits better in the hash, we rotate
      * the position we XOR with.
      * 
      * @return The hash
      */
     public long getBoxesHash()
     {
         long hash = 0; // 64 bits
         final int STEP = 7; // Just some relative prime to 64 so it
         // doesn't wrap around to 0 and overwrite
         // too many previous values boards with
         // only a few boxes.
 
         int bits = 0;
         int spacing = 0;
 
         for (int y = 1; y < height - 1; y++) {
             for (int x = 1; x < width - 1; x++) {
                 if (is(cells[y][x], BOX)) {
                     hash ^= (spacing << bits) ^ (spacing >> (64 - bits));
                     bits = (bits + STEP) % 64;
                     spacing = 0;
                 }
                 else {
                     spacing++;
                 }
             }
         }
 
         return hash;
     }
 
     /**
      * Returns a hash of the boxes and the topmost, leftmost reachable
      * player position.
      * 
      * @return The hash
      */
     public long getPlayerBoxesHash()
     {
         // Add the topmost, leftmost reachable position
         // to the last 16 bytes of the hash
         return getBoxesHash() ^ (topLeftReachable << 48);
     }
 
     /**
      * Compares two boards for equality.
      *
      * @note The topmost, leftmost reachable position is compared instead of
      *       the actual player position.
      */
     @Override
     public boolean equals(Object other)
     {
         if (!(other instanceof Board))
             return false;
 
         Board o = (Board) other;
 
         if (topLeftReachable != o.topLeftReachable)
             return false;
 
         // The outer rows/columns are always walls (or not reachable)
         for (int y = 1; y < height - 1; y++) {
             for (int x = 1; x < width - 1; x++) {
                 int cell1 = cells[y][x] & BOX;
                 int cell2 = o.cells[y][x] & BOX;
                 if (cell1 != cell2)
                     return false;
             }
         }
 
         return true;
     }
 
     @Override
     public int hashCode()
     {
         long h = getPlayerBoxesHash();
         return (int) (h ^ (h >> 32));
     }
 
     /**
      * Returns whether or not the given position is contained in this board.
      * 
      * @param pos The position.
      * @return True if this board contains the position, otherwise false.
      */
     public boolean contains(Position pos)
     {
         return contains(pos.row, pos.column);
     }
 
     /**
      * Returns whether or not the position specified by the given row and column
      * exists on this board.
      * 
      * @param row The row index.
      * @param col The column index.
      * @return True if it exists, otherwise false.
      */
     public boolean contains(int row, int col)
     {
         return row >= 0 && row < height && col >= 0 && col < width;
     }
 
     /**
      * Return the position for all boxes on the board
      * 
      * @return A collection of positions of boxes
      */
     public Collection<Position> getBoxes()
     {
         Collection<Position> tmp = new LinkedList<Position>();
         for (int row = 0; row < height; row++) {
             for (int col = 0; col < width; col++) {
                 if (is(cells[row][col], BOX)) {
                     tmp.add(new Position(row, col));
                 }
             }
         }
         return tmp;
     }
 
     /**
      * Finds a path from the player's current position on this board to the
      * specified goal position.
      * 
      * TODO: We might want to traverse the whole board (starting at players
      * position) and for each ripple (think of water) out from the player we
      * denote the direction in which we should go from that square to get back
      * to the player.
      * 
      * @param goal
      *            The position of the cell that we want to find a path to.
      * @return A collection
      */
     public Deque<Direction> findPath(Position goal)
     {
         clearVisited();
         return findPath(new Position(playerRow, playerCol), goal);
     }
 
     /**
      * Finds a path from the start position to the goal position recursively.
      * 
      * @param start
      *            Starting position.
      * @param goal
      *            Goal position.
      * @return A list of directions to go from start to goal.
      */
     public Deque<Direction> findPath(Position start, Position goal)
     {
         // TODO: might want to skip .equals() in favour for performance
         if (start.equals(goal)) {
             return new LinkedList<Direction>();
         }
 
         cells[start.row][start.column] |= VISITED;
 
         for (Direction dir : Direction.values()) {
             Position newPosition = new Position(start, moves[dir.ordinal()]);
 
             
             // We do not move any boxes while going this path.
             if (contains(newPosition)
                     && !is(cells[newPosition.row][newPosition.column],
                             (byte) (WALL | BOX | VISITED))) {
 
                 Deque<Direction> solution = findPath(newPosition, goal);
 
                 if (solution != null) {
                     solution.addFirst(dir);
                     return solution;
                 }
             }
         }
 
         return null;
     }
     
     /**
      * Finds all boxes that can be reached by the player.
      * 
      * @return A collection of ReachableBox objects
      */
     public Collection<ReachableBox> findReachableBoxSquares()
     {
         clearVisited();
         ArrayList<ReachableBox> reachable = new ArrayList<ReachableBox>(20);
         findReachableWithDFS(reachable, playerRow, playerCol,
             new LinkedList<Direction>());     
         return reachable;
     }
     
     /**
      * Finds all boxes that can be reached by the player.
      * 
      * @return A collection of ReachableBox objects
      */
     public void findReachableWithDFS(
         ArrayList<ReachableBox> reachable, int startRow, int startCol,
         LinkedList<Direction> path)
     {
         cells[startRow][startCol] |= VISITED;
         
         boolean boxNearby = false;
         for (Direction dir : Direction.values()) {
             int row = startRow + moves[dir.ordinal()][0];
             int col = startCol + moves[dir.ordinal()][1];
             
             if (is(cells[row][col], BOX)) {
                 boxNearby = true;
                 continue;
             }
             
             if (!is(cells[row][col], REJECT_WALK)) {
                 path.addLast(dir);
                 findReachableWithDFS(reachable, row, col, path);
                 path.removeLast();
             }
         }
         
         if (boxNearby) {
             reachable.add(new ReachableBox(new Position(startRow, startCol),
                 new LinkedList<Direction>(path)));
         }
     }
     
     /**
      * Updates the minimum top left position that the player can move to,
      * defined as (row*width)+col. This is used for duplicate detection.
      */
     public void updateTopLeftReachable()
     {
         clearVisited();
         int topLeftReachable = updateTopLeftReachableDFS(playerRow, playerCol);
     }
     
     /**
      * Recursive part of updateTopLeftReachable
      */
     public int updateTopLeftReachableDFS(int startRow, int startCol)
     {
         cells[startRow][startCol] |= VISITED;
         
         int minimum = (startRow*width) + startCol;
         for (Direction dir : Direction.values()) {
             int row = startRow + moves[dir.ordinal()][0];
             int col = startCol + moves[dir.ordinal()][1];
             
             if (!is(cells[row][col], (byte) (WALL | VISITED | BOX))) {
                 int pos = updateTopLeftReachableDFS(row, col);
                 if (pos < minimum) minimum = pos;
             }
         }
         
         return minimum;
     }
     
 }
