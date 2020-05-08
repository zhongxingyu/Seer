 package game.stepper;
 
 import game.Board;
 import game.Cell;
 import game.Command;
 import game.Ending;
 import game.State;
 import game.StaticConfig;
 import game.util.Scoring;
 
 /**
  * @author seba
  */
 public class SingleStepper {
 
   public final StaticConfig sconfig;
   
   public SingleStepper(StaticConfig sconfig) {
     this.sconfig = sconfig;
   }
   
   /**
    * Moves robot. Returns true if move was valid.
    */
   protected boolean moveRobot(State st, Command cmd) {
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
       return true;
     case Abort:
       st.ending = Ending.Abort;
       return true;
     case Shave:
      return st.shaveBeard(st.robotCol, st.robotRow);
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
       return true;
 
     case Lift:
       if (st.lambdaPositions.isEmpty() && st.board.bitsets[Cell.HoRock.ordinal()].isEmpty() && st.board.bitsets[Cell.FallingHoRock.ordinal()].isEmpty()) {
         moveRobot(st, nextCol, nextRow);
         return true;
       }
       // invalid move
       return false;
     case Razor:
      ++st.board.razors;
      moveRobot(st, nextCol, nextRow);
      return true;
     case Rock:
     case FallingRock:
       if (cmd == Command.Left && st.board.get(nextCol - 1, nextRow) == Cell.Empty) {
         // push rock to the left
         st.board.set(nextCol - 1, nextRow, Cell.FallingRock);
         fallingPosition(st, nextCol - 1, nextRow);
         moveRobot(st, nextCol, nextRow);
         return true;
       }
       if (cmd == Command.Right && st.board.get(nextCol + 1, nextRow) == Cell.Empty) {
         // push rock to the right
         st.board.set(nextCol + 1, nextRow, Cell.FallingRock);
         fallingPosition(st, nextCol + 1, nextRow);
         moveRobot(st, nextCol, nextRow);
         return true;
       }
     case HoRock:
     case FallingHoRock:
       if (cmd == Command.Left && st.board.get(nextCol - 1, nextRow) == Cell.Empty) {
         // push rock to the left
         st.board.set(nextCol - 1, nextRow, Cell.FallingHoRock);
         fallingPosition(st, nextCol - 1, nextRow);
         moveRobot(st, nextCol, nextRow);
         return true;
       }
       if (cmd == Command.Right && st.board.get(nextCol + 1, nextRow) == Cell.Empty) {
         // push rock to the right
         st.board.set(nextCol + 1, nextRow, Cell.FallingHoRock);
         fallingPosition(st, nextCol + 1, nextRow);
         moveRobot(st, nextCol, nextRow);
         return true;
       }
     case Target:
       return false;
     case Trampoline:
       int trampolinePos = st.board.position(nextCol, nextRow);
       String trampoline = st.board.trampolinePos.get(trampolinePos);
       st.board.set(nextCol, nextRow, Cell.Empty);
       String target = st.board.trampolineTargets.get(trampoline);
       int jumpPos = st.board.targetPos.get(target);
       st.board.set(jumpPos, Cell.Empty);
       int jumpCol = st.board.col(jumpPos);
       int jumpRow = st.board.row(jumpPos);
       moveRobot(st, jumpCol, jumpRow);
       // remove all trampolines to used target
       removeTrampolines(target, st);
       return true;
     default:
       // the move was invalid => execute Wait
       return false;
     }
   }
 
   private void removeTrampolines(String target, State st) {
 	for(int row = 0; row <= st.board.height; ++row) {
 		for(int col = 0; col <= st.board.width; ++col) {
 			if(st.board.bitsets[Cell.Trampoline.ordinal()].get(st.board.position(col, row))) {
 				String trampoline = st.board.trampolinePos.get(st.board.position(col, row));
 				String jumptarget = st.board.trampolineTargets.get(trampoline);
 				if(jumptarget.equals(target)) {
 				  st.board.set(st.board.position(col, row), Cell.Empty);
 				  freePosition(st, col, row);
 				}
 			}
 		}
 	}
   }
 
   protected void fallingPosition(State st, int col, int row) {
     st.activePositions.add(col * st.board.height + row);
   }
   
   protected void freePosition(State st, int col, int row) {
     st.activePositions.add(col * st.board.height + row);
     st.activePositions.add((col + 1) * st.board.height + row);
     st.activePositions.add((col - 1)* st.board.height + row);
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
 
   protected void moveRock(State st, int oldCol, int oldRow, int newCol, int newRow, Cell type, Board board) {
     st.board.set(oldCol, oldRow, Cell.Empty);
     freePosition(st, oldCol, oldRow);
     if(type == Cell.Rock)
     	st.board.set(newCol, newRow, Cell.FallingRock);
     else 
     	st.board.set(newCol, newRow, Cell.FallingHoRock);
     fallingPosition(st, newCol, newRow); 
     if(type == Cell.HoRock) {
     	if(board.get(newCol, newRow-1) != Cell.Empty
     		&& board.get(newCol, newRow-1) != Cell.Robot 
     		&& board.get(newCol, newRow-1) != Cell.Rock 
     		&& board.get(newCol, newRow-1) != Cell.FallingRock 
     		&& board.get(newCol, newRow-1) != Cell.HoRock
     		&& board.get(newCol, newRow-1) != Cell.FallingHoRock) {
     		st.board.set(newCol, newRow, Cell.Lambda);
     	} else if(board.get(newCol, newRow-1) != Cell.Empty
     		&& board.get(newCol, newRow-1) != Cell.Robot
     		&& board.get(newCol-1, newRow-1) != Cell.Empty
     		&& board.get(newCol+1, newRow-1) != Cell.Empty) {
     		st.board.set(newCol, newRow, Cell.Lambda);
     	}
     }
   }
 
   protected void updateBoard(State st) {
     Board oldBoard = st.board.clone();
 
     Integer[] positions = st.activePositions.toArray(new Integer[st.activePositions.size()]);
 
     // all further activations are only due in the next iteration
     st.activePositions.clear();
     for (int pos : positions) {
       int col = pos / st.board.height;
       int row = pos % st.board.height;
       
       if (oldBoard.get(col, row) == Cell.Rock || oldBoard.get(col, row) == Cell.FallingRock || oldBoard.get(col, row) == Cell.HoRock || oldBoard.get(col, row) == Cell.FallingHoRock) {
     	Cell oldType;
     	if(oldBoard.get(col, row) == Cell.Rock || oldBoard.get(col, row) == Cell.FallingRock)
     		oldType = Cell.Rock;
     	else 
     		oldType = Cell.HoRock;
     	st.board.set(col, row, oldType);
         
         if (oldBoard.get(col, row - 1) == Cell.Empty) {
           // fall straight down
           moveRock(st, col, row, col, row - 1, oldType, oldBoard);
         }
         else if (oldBoard.get(col, row - 1) == Cell.Rock || oldBoard.get(col, row - 1) == Cell.FallingRock) {
           // there is a rock below
           if (oldBoard.get(col + 1,  row) == Cell.Empty &&
               oldBoard.get(col + 1, row - 1) == Cell.Empty)
             // rock slides to the right
             moveRock(st, col, row, col + 1, row - 1, oldType, oldBoard);
           else if (oldBoard.get(col - 1, row) == Cell.Empty &&
                    oldBoard.get(col - 1, row - 1) == Cell.Empty)
             // rock slides to the left
             moveRock(st, col, row, col - 1, row - 1, oldType, oldBoard);
         }
         else if (oldBoard.get(col, row - 1) == Cell.Lambda &&
                  oldBoard.get(col + 1, row) == Cell.Empty &&
                  oldBoard.get(col + 1, row - 1) == Cell.Empty) {
           // rock slides to the right off the back of a lambda
           moveRock(st, col, row, col + 1, row - 1, oldType, oldBoard);
         }
         else if (oldBoard.immovable(col, row)) {
           // rock cannot ever be moved => rock can be considered a wall
           st.board.set(col,  row, Cell.Wall);
         }
         
         // skip checking whether we should open lambda lifts
       }
     }
     
     //Update beards
     if(oldBoard.growthcounter <= 0) {
       for(int pos = 0; pos < oldBoard.length; ++pos) {
     	int col = oldBoard.col(pos);
     	int row = oldBoard.row(pos);
     	if(oldBoard.get(col, row) == Cell.Beard) {  
 		  if(oldBoard.get(col-1, row-1) == Cell.Empty ) {
 		    st.board.set(col-1,  row-1, Cell.Beard);
 		  }
 		  if(oldBoard.get(col, row-1) == Cell.Empty ) {
 	   	    st.board.set(col,  row-1, Cell.Beard);
 	  	  }
 		  if(oldBoard.get(col+1, row-1) == Cell.Empty ) {
 	   	    st.board.set(col+1,  row-1, Cell.Beard);
 	   	  }
 		  if(oldBoard.get(col-1, row) == Cell.Empty ) {
 	        st.board.set(col-1,  row, Cell.Beard);
 	      }
 		  if(oldBoard.get(col+1, row) == Cell.Empty ) {
 	        st.board.set(col+1,  row, Cell.Beard);
 	      }
 		  if(oldBoard.get(col-1, row+1) == Cell.Empty ) {
 	        st.board.set(col-1,  row+1, Cell.Beard);
 	      }
 		  if(oldBoard.get(col, row+1) == Cell.Empty ) {
 	        st.board.set(col,  row+1, Cell.Beard);
 	      }
 		  if(oldBoard.get(col+1, row+1) == Cell.Empty ) {
 	        st.board.set(col+1,  row+1, Cell.Beard);
 	      }
     	}
       }
 	  st.board.growthcounter = sconfig.beardgrowth;
     }
 
     --st.board.growthcounter;
     
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
     if (st.lambdaPositions.isEmpty() && 
         st.board.bitsets[Cell.HoRock.ordinal()].isEmpty() && 
         st.board.bitsets[Cell.FallingHoRock.ordinal()].isEmpty() && 
         st.board.get(st.robotCol,st.robotRow) == Cell.RobotAndLift) {
       st.ending = Ending.Win;
       return;
     }
     
     Cell onTop = st.board.get(st.robotCol, st.robotRow + 1);
     switch (onTop) {
     case FallingHoRock:
     case FallingRock:
       st.ending = Ending.LoseRock;
     }
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
     newSt.score = Scoring.totalScore(newSt.steps, newSt.collectedLambdas, newSt.ending != Ending.LoseRock && newSt.ending != Ending.LoseWater, newSt.ending == Ending.Win);
     
     return newSt.makeFinal();
   }
 
 }
 
