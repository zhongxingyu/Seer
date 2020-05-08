 /**
  * 
  */
 package sokoban.solvers;
 
 import java.util.ArrayList;
 import java.util.Random;
 
 import sokoban.Board;
 
 /**
  * A Solover that pulls the boxes instead of the usual pushing
  */
 public class Puller implements Solver
 {
     private Board startBoard;
     private Board board;
     private int numBoxes;
     private Random rand;
     private int iterationsCount = 0;
     
     private Position[] boxes;
 
     /**
      * Small class that stores a position
      */
     public class Position
     {
         int x, y;
 
         /**
          * Create a new position
          * 
          * @param x X coordinate
          * @param y Y coordinate
          */
         public Position(int x, int y)
         {
             this.x = x;
             this.y = y;
         }
     }
 
     /**
      * Small class containing the player position and its relative box position
      */
     public class PlayerPosDir extends Position
     {
         final int bx, by; // relative box position
 
         /**
          * Create a new player position
          * 
          * @param x TODO
          * @param y TODO
          * @param bx TODO
          * @param by TODO
          */
         public PlayerPosDir(int x, int y, int bx, int by)
         {
             super(x, y);
             this.bx = bx;
             this.by = by;
         }
     }
 
     /**
      * Resets the board to the starting board.
      */
     private void reset()
     {
         board = (Board) startBoard.clone();
 
         // Store all boxes
         int b = 0;
         for (int i = 0; i < board.height; ++i) {
             for (int j = 0; j < board.width; ++j) {
                 if (Board.is(board.cells[i][j], Board.BOX)) {
                     boxes[b] = new Position(i, j);
                     b++;
                 }
             }
         }
     }
 
     @Override
     public String solve(Board inputBoard)
     {
         rand = new Random();
         startBoard = (Board) inputBoard.clone();
         numBoxes = startBoard.getRemainingBoxes();
         startBoard.reverse();
         boxes = new Position[numBoxes];
         
         
         return solverAlgorithm();
     }
     
     public String solverAlgorithm()
     {
         finished: while (true) {
             reset();
             
             PlayerPosDir playerPosDir;
             do {
                 // TODO must be able to move to this block!
                 playerPosDir = choosePosition();
                 while (moveBox(playerPosDir)) {
                     if (solved()) break finished;
                     // TODO choose Condition X and exit from
                     //      loop if Condition X fails
                 }
             } while (!deadlock(playerPosDir));
         }
         
         return null;
     }
 
     private boolean solved()
     {
         return board.getBoxesInStart() == numBoxes;
     }
 
     private boolean deadlock(PlayerPosDir pos)
     {
         return !(Board.is(board.cells[pos.x-1][pos.y], Board.REJECT_PULL) &&
                 Board.is(board.cells[pos.x+1][pos.y], Board.REJECT_PULL) &&
                 Board.is(board.cells[pos.x][pos.y-1], Board.REJECT_PULL) &&
                 Board.is(board.cells[pos.x][pos.y+1], Board.REJECT_PULL));
     }
 
     private boolean moveBox(PlayerPosDir pos)
     {
         int newx = pos.x - pos.bx;
         int newy = pos.y - pos.by;
         
         iterationsCount++;
         
         // See if there next square is empty
         if (Board.is(board.cells[newy][newx], Board.REJECT_PULL))
             return false;
         
         // Move the box
         board.pull(pos.x, pos.y, pos.bx, pos.by);
         pos.x += newx;
         pos.y += newy;
         
         return true;
     }
 
     private PlayerPosDir choosePosition()
     {
         final int boxCount = boxes.length;
         final int max = 4*boxCount;
         final int[] stepX = { 0, 1, 0,-1};
         final int[] stepY = {-1, 0, 1, 0};
         int triesLeft = max;
         int p = rand.nextInt(max);
         
         while (triesLeft > 0) {
             Position box = boxes[p / 4];
             int dir = p % 4;
             int x = box.x + stepX[dir];
             int y = box.y + stepY[dir];
            if (x >= 0 && x < board.width && y >= 0 && y < board.height &&
                     !Board.is(board.cells[y][x], Board.REJECT_PULL)) {
                 return new PlayerPosDir(x, y, -stepX[dir], -stepY[dir]);
             }
             p = (p+1) % max;
             triesLeft--;
         }
         return null;
     }
     
     @Override
     public int getIterationsCount()
     {
         return iterationsCount;
     }
 
 }
