 import java.util.*;
 
 public class BoardState {
 
     public static final int PRIME = 83;
 
     public static final char FREE_SPACE_CHAR = ' ';
     public static final char GOAL_CHAR = '.';
     public static final char WALL_CHAR = '#';
     public static final char PLAYER_CHAR = '@';
     public static final char PLAYER_ON_GOAL_CHAR = '+';
     public static final char BOX_CHAR = '$';
     public static final char BOX_ON_GOAL_CHAR = '*';
 
     public static final int UP = 0;
     public static final int RIGHT = 1;
     public static final int DOWN = 2;
     public static final int LEFT = 3;
 
     private static final int FREE_SPACE     = 0;
     private static final int WALL           = 1;
     private static final int GOAL           = 2;
     private static final int PLAYER         = 4;
     private static final int BOX            = 8;
     private static final int PLAYER_ON_GOAL = PLAYER | GOAL;
     private static final int BOX_ON_GOAL    = BOX | GOAL;
     private static final int NOT_FREE       = WALL | BOX;
 
     private char[] boardCharacters = { FREE_SPACE_CHAR, WALL_CHAR, GOAL_CHAR, 0, PLAYER_CHAR, 0, PLAYER_ON_GOAL_CHAR, 0, BOX_CHAR, 0, BOX_ON_GOAL_CHAR };
     private static HashMap<Character, Integer> characterMapping;
 
     static {
         characterMapping = new HashMap<Character, Integer>();
         characterMapping.put(FREE_SPACE_CHAR, FREE_SPACE);
         characterMapping.put(GOAL_CHAR, GOAL);
         characterMapping.put(WALL_CHAR, WALL);
         characterMapping.put(PLAYER_CHAR, PLAYER);
         characterMapping.put(PLAYER_ON_GOAL_CHAR, PLAYER_ON_GOAL);
         characterMapping.put(BOX_CHAR, BOX);
         characterMapping.put(BOX_ON_GOAL_CHAR, BOX_ON_GOAL);
     }
 
     // Vectors corresponding to the moves {up, right, down, left}
     private static int[]  dr                  = { -1, 0, 1, 0 };
     private static int[]  dc                  = { 0, 1, 0, -1 };
     private static char[] directionCharacters = { 'U', 'R', 'D', 'L' };
 
     private int width, height;
     private int                      playerRow, playerCol;
     public  int                      goalCnt, boxCnt;
 
     private StackEntry               previousMove;
     public  int                      playerCnt;
     private int[][]                  board;
     private int[][]                  goalCells;
     private boolean[][]              trappingCells;
 
     private Set<Integer>              playerAndBoxesHashCells;
     private int[]                     playerAndBoxesHashCellsSimple;
     private Set<Integer>               tempTest;
     private Map<Object, Integer>              gameStateHash;
 
     public BoardState(List<String> lines) {
         height = lines.size();
         width = 0;
         for (String line : lines) {
             width = Math.max(width, line.length());
         }
         board = new int[height][width];
         int row = 0;
         List<int[]> tempGoalCells = new ArrayList<int[]>();
         List<Integer> tempBoxList = new ArrayList<Integer>();
         for (String line : lines) {
             int col = 0;
             for (char cell : line.toCharArray()) {
                 board[row][col] = characterMapping.get(cell);
                 if (isPlayer(row, col)) {
                     playerCnt++;
                     playerRow = row;
                     playerCol = col;
                 }
                 if (isGoal(row, col)) {
                     tempGoalCells.add(new int[] {row, col});
                     goalCnt++;
                 }
                 if (isBox(row, col)) {
                     board[row][col] |= boxCnt<<4;
                     tempBoxList.add(row);
                     tempBoxList.add(col);
                     boxCnt++;
                 }
                 col++;
             }
             row++;
         }
         goalCells = new int[tempGoalCells.size()][];
 
         if(Main.useGameStateHash){
 
             gameStateHash = new HashMap<Object, Integer>();
             if(Main.useSimpleHash){
                 playerAndBoxesHashCellsSimple = new int[boxCnt+1];
             }else{
                 playerAndBoxesHashCells = new HashSet<Integer>();
             }
             hashNewPosition(0, 0, playerRow, playerCol, false);
 
             for (int i = 0; i < boxCnt; i++) {
                 int boxRow = tempBoxList.get(i * 2);
                 int boxCol = tempBoxList.get(i * 2 + 1);
                 hashNewPosition(0,0,boxRow,boxCol, true);
             }
             tempTest = new HashSet<Integer>(playerAndBoxesHashCells);
         }
 
         for (int i = 0; i < tempGoalCells.size(); i++) {
             goalCells[i] = tempGoalCells.get(i);
         }
         setTrappingCells();
     }
 
     private void setTrappingCells() {
         trappingCells = new boolean[height][width];
         for (int i = 0; i < height; i++) {
             Arrays.fill(trappingCells[i], true);
         }
         for (int[] goal : goalCells) {
             if (trappingCells[goal[0]][goal[1]]) {
                 traverseTrappingCells(goal[0], goal[1], 0);
             }
         }
     }
 
     private void traverseTrappingCells(int row, int col, int direction) {
         int newRow = row + dr[direction];
         int newCol = col + dc[direction];
         if (!isWall(newRow, newCol) || isGoal(row, col)) {
             trappingCells[row][col] = false;
             for (int i = 0; i < 4; i++) {
                 newRow = row + dr[i];
                 newCol = col + dc[i];
                 if (trappingCells[newRow][newCol] && !isWall(newRow, newCol)){
                     traverseTrappingCells(newRow, newCol, i);
                 }
             }
         }
      }
 
     public boolean performMove(int direction) {
         int newRow = playerRow + dr[direction];
         int newCol = playerCol + dc[direction];
         boolean successful = false;
 
         // We don't check if the move is outside the board since the player always is surrounded by walls
         if (isFree(newRow, newCol)) {
             movePlayer(newRow, newCol);
             successful = true;
             previousMove = new StackEntry(direction, previousMove);
         } else if (isBox(newRow, newCol)) {
             int newRow2 = newRow + dr[direction];
             int newCol2 = newCol + dc[direction];
             if (isFree(newRow2, newCol2)) {
                 moveBox(newRow, newCol, newRow2, newCol2);
                 movePlayer(newRow, newCol);
                 successful = true;
                 previousMove = new StackEntry(direction|4, previousMove);
             }
         }
         return successful;
     }
 
     public int[][] getPossibleMovePositions() {
         LinkedList<int[]> moves = new LinkedList<int[]>();
         boolean[][] visited = new boolean[height][width];
         getPossibleMovePositionsRec(playerRow, playerCol, visited, moves);
         int[][] res = new int[moves.size()][];
         int i = 0;
         for (int[] move : moves) {
             res[i++] = move;
         }
         return res;
     }
 
     private void getPossibleMovePositionsRec(int row, int col, boolean[][] visited, LinkedList<int[]> moves) {
         visited[row][col] = true;
         // Check if this is a cell from where we can move a box (and is not the current player cell)
         if (row != playerRow || col != playerCol) {
             for (int dir = 0; dir < 4; dir++) {
                 int newRow = row + dr[dir], newCol = col + dc[dir];
                 if (isBox(newRow, newCol)) {
                     int newRow2 = newRow + dr[dir], newCol2 = newCol + dc[dir];
                     if (isFree(newRow2, newCol2)) {
                         moves.add(new int[] {row, col});
                         break;
                     }
                 }
             }
         }
         for (int dir = 0; dir < 4; dir++) {
             int newRow = row + dr[dir], newCol = col + dc[dir];
             if (isFree(newRow, newCol) && !visited[newRow][newCol]) {
                 getPossibleMovePositionsRec(newRow, newCol, visited, moves);
             }
         }
     }
 
     /*
      * Teleports the player to the given position. No error checking done!
      * Should only use positions given by getPossibleMovePositions()
      */
     public boolean performJump(int row, int col) {
         previousMove = new StackEntry(8|playerRow<<4|playerCol<<14, previousMove);
         movePlayer(row, col);
         return true;
     }
 
     /*
      * Determines if the move does not create an unsolvable situation
      */
     public boolean isGoodMove(int direction) {
         int newRow = playerRow + dr[direction];
         int newCol = playerCol + dc[direction];
         if (isFree(newRow, newCol)) return true;
         boolean good = false;
         if (isBox(newRow, newCol)) {
             int newRow2 = newRow + dr[direction];
             int newCol2 = newCol + dc[direction];
             if (isFree(newRow2, newCol2) && !isTrappingCell(newRow2, newCol2)) {
                 good = true;
                 moveBox(newRow, newCol, newRow2, newCol2);
                 good &= checkIfValidBox(newRow2 - 1, newCol2 - 1);
                 good &= checkIfValidBox(newRow2 - 1, newCol2    );
                 good &= checkIfValidBox(newRow2    , newCol2 - 1);
                 good &= checkIfValidBox(newRow2    , newCol2    );
                 moveBox(newRow2, newCol2, newRow, newCol);
             }
         }
         return good;
     }
 
     /*
      * Checks if the 2x2 box with top-left corner at (row, col) is valid, that is that it isn't
      * completely filled with walls/boxes or that every box is at a goal
      */
     private boolean checkIfValidBox(int row, int col) {
         boolean unmatchedBox = false;
         for (int rowDiff = 0; rowDiff < 2; rowDiff++) {
             for (int colDiff = 0; colDiff < 2; colDiff++) {
                 if (isFree(row + rowDiff, col + colDiff)) return true;
                unmatchedBox |= board[row + rowDiff][col + colDiff] == BOX;
             }
         }
         return !unmatchedBox;
     }
 
     /*
      * previousMove has the following format (bits 0-indexed:
      * Bits 0 and 1 together contain the direction of the move
      * Bit 2 decides whether a board was pushed by the move or not
      * If bit 3 is set, the move was a jump, and bits 0-2 can be ignored.
      * Bits 4-13 together determine the row that the jump was made from.
      * Bits 14-23 together determine the column that the jump was made from.
      */
     public boolean reverseMove() {
         if (previousMove == null) return false;
         if ((previousMove.val&8) != 0) {
             // Move was jump
             int r = previousMove.val>>4&((1<<10)-1);
             int c = previousMove.val>>14;
             movePlayer(r, c);
         } else {
             boolean movedBox = (previousMove.val & 4) != 0;
             int dir = previousMove.val&3;
             int oppositeDir = getOppositeDirection(dir);
             int r1 = playerRow, c1 = playerCol;
             int r0 = r1 + dr[oppositeDir], c0 = c1 + dc[oppositeDir];
             movePlayer(r0, c0);
             if (movedBox) {
                 int r2 = r1 + dr[dir], c2 = c1 + dc[dir];
                 moveBox(r2, c2, r1, c1);
             }
         }
         previousMove = previousMove.prev;
         return true;
     }
 
     public int directionLastMove() {
         if (previousMove == null) return -1;
         return previousMove.val&3;
     }
 
     public boolean movedBoxLastMove() {
         if (previousMove == null) return false;
         return (previousMove.val & 4) != 0;
     }
 
     /*
      * Resets the board to the starting position and returns the path that was taken
      */
     public String backtrackPath() {
         StringBuilder sb = new StringBuilder();
         while (previousMove != null) {
             if ((previousMove.val&8) != 0) {
                 // Move was jump
                 // Have to search for path
                 int startRow = previousMove.val>>4&((1<<10)-1);
                 int startCol = previousMove.val>>14;
                 int[][] prev = new int[height][width];
                 for (int i = 0; i < height; i++) {
                     Arrays.fill(prev[i], -2);
                 }
                 LinkedList<int[]> q = new LinkedList<int[]>();
                 q.add(new int[] {startRow, startCol});
                 prev[startRow][startCol] = -1;
                 while (!q.isEmpty()) {
                     int[] state = q.removeFirst();
                     int row = state[0], col = state[1];
                     if (row == playerRow && col == playerCol) {
                         int r = playerRow, c = playerCol;
                         while (prev[r][c] != -1) {
                             int dir = prev[r][c];
                             sb.append(directionCharacters[dir]);
                             int oppDir = getOppositeDirection(dir);
                             r += dr[oppDir];
                             c += dc[oppDir];
                         }
                         break;
                     }
                     for (int dir = 0; dir < 4; dir++) {
                         int newRow = row + dr[dir];
                         int newCol = col + dc[dir];
                         if (isFree(newRow, newCol) && prev[newRow][newCol] == -2) {
                             prev[newRow][newCol] = dir;
                             q.add(new int[] {newRow, newCol});
                         }
                     }
                 }
             } else {
                 sb.append(directionCharacters[previousMove.val&3]);
             }
             reverseMove();
         }
         return sb.reverse().toString();
     }
 
     public String temp(){
         StringBuilder sb = new StringBuilder();
         StackEntry tempVar = previousMove;
         while (tempVar != null) {
             sb.append(directionCharacters[tempVar.val&3]);
             tempVar = tempVar.prev;
         }
         return sb.reverse().toString();
     }
 
     /*
      * Helper method that does not do error checking
      */
     private void moveBox(int oldRow, int oldCol, int newRow, int newCol) {
         board[oldRow][oldCol] &= ~BOX;
         board[newRow][newCol] |= BOX;
         board[newRow][newCol] |= -16 & board[oldRow][oldCol];
         board[oldRow][oldCol] &= 15;
         if(Main.useGameStateHash){
             hashNewPosition(oldRow, oldCol, newRow, newCol, true);
         }
     }
 
     private void hashNewPosition(int oldRow, int oldCol, int newRow, int newCol, boolean isBox){
         if(isBox){
             playerAndBoxesHashCells.remove(height*width + oldCol + oldRow*width);
             playerAndBoxesHashCells.add(height*width + newCol + newRow*width);
         }else {
             playerAndBoxesHashCells.remove(oldCol + oldRow*width);
             playerAndBoxesHashCells.add(newCol + newRow*width);
         }
     }
 
     /*
      * Helper method that does not do error checking
      */
     private void movePlayer(int newRow, int newCol) {
         board[playerRow][playerCol] &= ~PLAYER;
         board[newRow][newCol] |= PLAYER;
 
         if(Main.useGameStateHash){
             hashNewPosition(playerRow, playerCol, newRow, newCol, false);
         }
         playerRow = newRow;
         playerCol = newCol;
     }
 
     public void hashCurrentGameState(int depth){
         if(Main.useSimpleHash){
             gameStateHash.put(Arrays.copyOf(playerAndBoxesHashCellsSimple, playerAndBoxesHashCells.size()), depth);
         }else{
             gameStateHash.put(new HashSet<Integer>(playerAndBoxesHashCells), depth);
         }
     }
 
     public void clearHashTable(){
         gameStateHash.clear();
         playerAndBoxesHashCells = new HashSet<Integer>(tempTest);
 
     }
 
     public boolean isPreviousGameState(int currentDepth){
         Integer oldDepth = gameStateHash.get(playerAndBoxesHashCells);
         if(oldDepth == null){
             return false;
         }else if(oldDepth > currentDepth){
             hashCurrentGameState(currentDepth);
             return false;
         }else{
             return true;
         }
     }
 
     public static int getOppositeDirection(int direction) {
         return (direction + 2)&3;
     }
 
     public boolean isTrappingCell(int row, int col){
         return trappingCells[row][col];
     }
 
     public boolean isWall(int row, int col) {
         return board[row][col] == WALL;
     }
 
     public boolean isGoal(int row, int col) {
         return (board[row][col] & GOAL) != 0;
     }
 
     public boolean isBox(int row, int col) {
         return (board[row][col] & BOX) != 0;
     }
 
     public boolean isBoxInDirection(int direction) {
         return isBox(playerRow + dr[direction], playerCol + dc[direction]);
     }
 
     public int getBoxIndexInDirection(int direction){
         return getBoxNumber(playerRow + dr[direction], playerCol + dc[direction]);
     }
 
     public boolean isPlayer(int row, int col) {
         return (board[row][col] & PLAYER) != 0;
     }
 
     public boolean isFree(int row, int col) {
         return (board[row][col] & NOT_FREE) == 0;
     }
 
     // TODO this should be updated while moving
     public boolean isBoardSolved(){
         for(int[] goal: goalCells){
            if(!(board[goal[0]][goal[1]] == BOX_ON_GOAL)){
                 return false;
             }
         }
         return true;
     }
 
     public boolean onBoard(int row, int col) {
         return row >= 0 && row < height && col >= 0 && col < width;
     }
 
     public int getWidth() {
         return width;
     }
 
     public int getHeight() {
         return height;
     }
 
     public String toString() {
         StringBuilder sb = new StringBuilder();
         for (int row = 0; row < height; row++) {
             for (int col = 0; col < width; col++) {
                 sb.append(boardCharacters[board[row][col] & 15]);
             }
             sb.append('\n');
         }
         return sb.toString();
     }
 
     static class StackEntry {
         int val;
         StackEntry prev;
         public StackEntry(int val, StackEntry prev) {
             this.val = val;
             this.prev = prev;
         }
     }
 
     public String aliveCellsToString() {
         StringBuilder sb = new StringBuilder();
         for (int row = 0; row < height; row++) {
             for (int col = 0; col < width; col++) {
                 if(isWall(row,col)){
                     sb.append(boardCharacters[board[row][col] & 15]);
                 }else if(isTrappingCell(row, col)){
                     sb.append('x');
                 }else{
                     sb.append(FREE_SPACE_CHAR);
                 }
             }
             sb.append('\n');
         }
         return sb.toString();
     }
 
     private int generateHashVectorOfTwo(int[] vector, boolean small){
         if(small)
             return vector[0] + PRIME*vector[1];
         else
             return vector[0]*PRIME + PRIME*PRIME*vector[1];
     }
 
     public int getBoxNumber(int row, int col){
         return board[row][col] >> 4;
     }
 
     public int getPlayerCol() {
         return playerCol;
     }
 
     public int getPlayerRow() {
         return playerRow;
     }
 }
