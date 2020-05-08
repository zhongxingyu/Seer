 package game.stepper;
 
 import game.Board;
 import game.Cell;
 import game.Command;
 import game.Ending;
 import game.State;
 import game.StaticConfig;
 import game.fitness.Scoring;
 
 /**
  * @author seba
  */
 public class SingleStepper {
 
   public final StaticConfig sconfig;
   
   public SingleStepper(StaticConfig sconfig) {
     this.sconfig = sconfig;
   }
   
   protected void moveRobot(State st, Command cmd) {
     int nextCol;
     int nextRow;
     
     switch (cmd) {
     case Left:
       nextCol = st.robotCol - 1;
       nextRow = st.robotRow;
       break;
     case Right:
       nextCol = st.robotCol + 1;
       nextRow = st.robotRow;
       break;
     case Up:
       nextCol = st.robotCol;
       nextRow = st.robotRow + 1;
       break;
     case Down:
       nextCol = st.robotCol;
       nextRow = st.robotRow - 1;
       break;
     case Wait:
       return;
     case Abort:
       st.ending = Ending.Abort;
       return;
     default:
       throw new IllegalArgumentException("Unknown command " + cmd);
     }
     
     Cell next = st.board.get(nextCol, nextRow); 
     switch (next) {
     case Lambda:
       ++st.collectedLambdas;
       st.removeLambda(nextCol, nextRow);
     case Empty:
     case Earth:
       moveRobot(st, nextCol, nextRow);
       break;
 
     case Lift:
       if (st.lambdaPositions.isEmpty()) {
         moveRobot(st, nextCol, nextRow);
         break;
       }
         
     case Rock:
     case FallingRock:
       if (cmd == Command.Left && st.board.get(nextCol - 1, nextRow) == Cell.Empty) {
         // push rock to the left
         st.board.set(nextCol - 1, nextRow, Cell.FallingRock);
         fallingPosition(st, nextCol - 1, nextRow);
         moveRobot(st, nextCol, nextRow);
         break;
       }
       if (cmd == Command.Right && st.board.get(nextCol + 1, nextRow) == Cell.Empty) {
         // push rock to the right
         st.board.set(nextCol + 1, nextRow, Cell.FallingRock);
         fallingPosition(st, nextCol + 1, nextRow);
         moveRobot(st, nextCol, nextRow);
         break;
       }
 
     default:
       // the move was invalid => execute Wait
       break;
     }
   }
 
   protected void fallingPosition(State st, int col, int row) {
     st.activePositions.add(col * st.board.height + row);
   }
   
   protected void freePosition(State st, int col, int row) {
     st.activePositions.add(col * st.board.height + row);
     st.activePositions.add(col * st.board.height + row + 1);
     st.activePositions.add((col + 1) * st.board.height + row + 1);
     st.activePositions.add((col - 1)* st.board.height + row + 1);
   }
   
   protected void moveRobot(State st, int nextCol, int nextRow) {
     if (st.board.get(nextCol,nextRow) == Cell.Lift)
       st.board.set(nextCol, nextRow, Cell.RobotAndLift);
     else
       st.board.set(nextCol, nextRow, Cell.Robot);
     
     if (st.board.get(st.robotCol,st.robotRow) == Cell.RobotAndLift)
       st.board.set(st.robotCol, st.robotRow, Cell.Lift);
     else {
       st.board.set(st.robotCol, st.robotRow, Cell.Empty);
       freePosition(st, st.robotCol, st.robotRow);
     }
     
     st.robotCol = nextCol;
     st.robotRow = nextRow;
   }
 
   protected void moveRock(State st, int oldCol, int oldRow, int newCol, int newRow) {
     st.board.set(oldCol, oldRow, Cell.Empty);
     freePosition(st, oldCol, oldRow);
     st.board.set(newCol, newRow, Cell.FallingRock);
     fallingPosition(st, newCol, newRow);
   }
 
   protected void updateBoard(State st) {
     Board oldBoard = st.board.clone();
 
     Integer[] positions = st.activePositions.toArray(new Integer[st.activePositions.size()]);
 
     // all further activations are only due in the next iteration
     st.activePositions.clear();
     
     for (int pos : positions) {
       int col = pos / st.board.height;
       int row = pos % st.board.height;
       
       if (oldBoard.get(col, row) == Cell.Rock || oldBoard.get(col, row) == Cell.FallingRock) {
         st.board.set(col, row, Cell.Rock);
         
         if (oldBoard.get(col, row - 1) == Cell.Empty)
           // fall straight down
           moveRock(st, col, row, col, row - 1);
         else if (oldBoard.get(col, row - 1) == Cell.Rock || oldBoard.get(col, row - 1) == Cell.FallingRock) {
           // there is a rock below
           if (oldBoard.get(col + 1,  row) == Cell.Empty &&
               oldBoard.get(col + 1, row - 1) == Cell.Empty)
             // rock slides to the right
             moveRock(st, col, row, col + 1, row - 1);
           else if (oldBoard.get(col - 1, row) == Cell.Empty &&
                    oldBoard.get(col - 1, row - 1) == Cell.Empty)
             // rock slides to the left
             moveRock(st, col, row, col - 1, row - 1);
         }
         else if (oldBoard.get(col, row - 1) == Cell.Lambda &&
                  oldBoard.get(col + 1, row) == Cell.Empty &&
                  oldBoard.get(col + 1, row - 1) == Cell.Empty)
           // rock slides to the right off the back of a lambda
           moveRock(st, col, row, col + 1, row - 1);
         
         // skip checking whether we should open lambda lifts
       }
     }
     
     /*
      * update water level
      */
     if (sconfig.floodingRate > 0) {
       st.stepsSinceLastRise++;
       if (st.stepsSinceLastRise + 1 == sconfig.floodingRate) {
         st.waterLevel++;
         st.stepsSinceLastRise = 0;
       }
     }
   }
 
   protected void checkEnding(State st) {
     if (st.lambdaPositions.isEmpty() && st.board.get(st.robotCol,st.robotRow) == Cell.RobotAndLift)
       st.ending = Ending.Win;
     else if (st.board.get(st.robotCol, st.robotRow + 1) == Cell.FallingRock)
       st.ending = Ending.LoseRock;
     // abort action sets the ending field directly during movement
     
     /*
      * update underwater step counting
      */
     if (st.robotRow <= st.waterLevel)
       st.stepsUnderwater++;
     else
       st.stepsUnderwater = 0;
     
     if (st.stepsUnderwater > sconfig.waterResistance)
       st.ending = Ending.LoseWater;
   }
   
   public State step(State st, Command cmd) {
     State newSt = st.clone();
     newSt.steps++;
     
     moveRobot(newSt, cmd);
     if (st.ending != Ending.Abort) {
       updateBoard(newSt);
       checkEnding(newSt);
     }
     newSt.score = Scoring.totalScore(newSt.steps, newSt.collectedLambdas, newSt.ending == Ending.Abort, newSt.ending == Ending.Win);
     
     return newSt.makeFinal();
   }
 
 }
