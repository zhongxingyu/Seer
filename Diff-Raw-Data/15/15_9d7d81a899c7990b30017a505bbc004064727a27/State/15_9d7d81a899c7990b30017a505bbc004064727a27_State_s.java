 package sokoban;
 /*
  * State
  * 
  * Version 0.1
  * 
  * Represents the Sokoban State.
  * 
  * Should not be confused with the class Map. 
  */
 
 import java.util.Arrays;
 import java.util.Stack;
 import java.util.Vector;
 
 public class State implements Cloneable {
 
 	
 	private State parentState;
     private static Stack<State> reusableStates = new Stack<State>();
 
 	private Vector<Box> boxes = new Vector<Box>();
 	private Vector<Cell> reachableCells; //should be HashMap?
 	private Vector<Cell> unReachableCells; //should be HashMap?
 
 	private int playerRow; // Player position in this state (after move that led to this state)
 	private int playerCol;
 
     private int lastBoxMovedIndex; // Index of the box moved that led to this state
 	private char lastMoveDir; // Move direction of box according to above that led to this state
 
 	private int parentKey; //reference to make it possible to find parent state - for final path building.
 	
 	private int g = 0; // Cost of moving to this position.
 	private double h = 0; // Heuristic cost.
 
     boolean[] goalsOccupied; //Vector which indicates if a goal(index) is occupied.
 	int nbOfBoxesOnGoal;
 
     // Time fields
     public static long constructorTime;
     public static long allSuccessorsTime;
     public static long tryMoveTime;
     public static long changeBoxConfigTime;
     private long constructorStartTime;
     private long allSuccessorsStartTime;
     private long tryMoveStartTime;
     private long changeBoxConfigStartTime;
 	
     /**
 	 * Constructs the internal representation of the State
 	 *
 	 * USED ONLY WHEN CREATING INITIAL STATE
 	 * @param
 	 */
 	public State(Vector<Box> pBoxes, int pPlayerRow, int pPlayerCol, boolean[] pGoalsOccupied, int pNbOfBoxesOnGoal) {
 		boxes = pBoxes;
 		playerRow = pPlayerRow;
 		playerCol= pPlayerCol;
 		lastBoxMovedIndex = -1; //No box is last moved in initial state.
 		//Should maybe be set to the box the player is moved to?
 		lastMoveDir = 'I'; // Initial state 'I', no last move direction.
 		goalsOccupied = pGoalsOccupied;
 		nbOfBoxesOnGoal = pNbOfBoxesOnGoal;
 		this.parentState = null;
 
 
 	} // End constructor State
 
 	/**
 	 * Constructs a state which is the result of applying one move.
 	 * "final" in arguments to avoid applying changes to parent state
 	 *
 	 * @param pParentState
 	 * @param pBoxIndex    index if the boxes vector
 	 * @param pMoveDir     the direction to move the box
 	 */
 	public State(final State pParentState, int pBoxIndex, char pMoveDir) {
         // Constructor Time
         if (Sokoban.profilingMode)
              constructorStartTime = System.currentTimeMillis();
 
 		// set boxes
 		for (int boxIndex = 0; boxIndex < Board.getNbOfBoxes(); boxIndex++){
             if(boxIndex == pBoxIndex){
                 this.boxes.add(new Box(pParentState.getBoxes().get(boxIndex))); // necessary to avoid having several states sharing same box objects!
             }
             else{
                 this.boxes.add(pParentState.getBoxes().get(boxIndex)); // necessary to avoid having several states sharing same box objects!
             }
 		}
 
 		// set player position before moving the box
 		playerRow = this.boxes.get(pBoxIndex).getRow();
 		playerCol = this.boxes.get(pBoxIndex).getCol();
 		lastBoxMovedIndex = pBoxIndex;
 		lastMoveDir = pMoveDir;
 		this.parentState = pParentState;
 		this.nbOfBoxesOnGoal = pParentState.nbOfBoxesOnGoal;
 		
 		this.goalsOccupied = new boolean[pParentState.goalsOccupied.length];
 		for(int i = 0; i < pParentState.goalsOccupied.length; i++){
 			this.goalsOccupied[i] = pParentState.goalsOccupied[i];
 		}
 		
 		boolean onGoalBeforeMove = this.boxes.get(pBoxIndex).isOnGoal();
     	if (onGoalBeforeMove){
     		goalsOccupied[Board.getGoalIndexAt(playerRow, playerCol)] = false;
     	}
 		// move the box
 		this.boxes.get(pBoxIndex).move(pMoveDir);
        
 		int lRowAfterMove = this.boxes.get(pBoxIndex).getRow();
 		int lColAfterMove = this.boxes.get(pBoxIndex).getCol();
 				
 		// if the box position is on goal
         if (Board.isGoal(lRowAfterMove, lColAfterMove)){
         	boxes.get(pBoxIndex).setIsOnGoal(true);
         	goalsOccupied[Board.getGoalIndexAt(lRowAfterMove, lColAfterMove)] = true;
         	if(!onGoalBeforeMove){
         		nbOfBoxesOnGoal++;
         	}
         }
         else{
         	boxes.get(pBoxIndex).setIsOnGoal(false);
         	if(onGoalBeforeMove){
         		nbOfBoxesOnGoal--;
         	}
         }
         
         this.g = pParentState.g + 1;
         this.h = Heuristic.getDavidDistanceHeuristic(this);
 
         // Append constructorTime to the constructorTime field
         if (Sokoban.profilingMode)
             State.constructorTime = State.constructorTime + (System.currentTimeMillis() - constructorStartTime);
 
 	} // End constructor State
 
     // Change box configuration
     private void changeBoxConfig(State pParentState, int pBoxIndex, char pMoveDir) {
         // Constructor Time
         if (Sokoban.profilingMode)
             changeBoxConfigStartTime = System.currentTimeMillis();
 
         // Remove old boxes
         boxes.clear();
 
         // set boxes
         for (int boxIndex = 0; boxIndex < Board.getNbOfBoxes(); boxIndex++){
             if(boxIndex == pBoxIndex){
                 this.boxes.add(new Box(pParentState.getBoxes().get(boxIndex))); // necessary to avoid having several states sharing same box objects!
             }
             else{
                 this.boxes.add(pParentState.getBoxes().get(boxIndex)); // necessary to avoid having several states sharing same box objects!
             }
         }
 
         // set player position before moving the box
         playerRow = this.boxes.get(pBoxIndex).getRow();
         playerCol = this.boxes.get(pBoxIndex).getCol();
         lastBoxMovedIndex = pBoxIndex;
         lastMoveDir = pMoveDir;
         this.parentState = pParentState;
         this.nbOfBoxesOnGoal = pParentState.nbOfBoxesOnGoal;
 
         this.goalsOccupied = new boolean[pParentState.goalsOccupied.length];
         for(int i = 0; i < pParentState.goalsOccupied.length; i++){
             this.goalsOccupied[i] = pParentState.goalsOccupied[i];
         }
 
         boolean onGoalBeforeMove = this.boxes.get(pBoxIndex).isOnGoal();
         if (onGoalBeforeMove){
             goalsOccupied[Board.getGoalIndexAt(playerRow, playerCol)] = false;
         }
         // move the box
         this.boxes.get(pBoxIndex).move(pMoveDir);
 
         int lRowAfterMove = this.boxes.get(pBoxIndex).getRow();
         int lColAfterMove = this.boxes.get(pBoxIndex).getCol();
 
         // if the box position is on goal
         if (Board.isGoal(lRowAfterMove, lColAfterMove)){
             boxes.get(pBoxIndex).setIsOnGoal(true);
             goalsOccupied[Board.getGoalIndexAt(lRowAfterMove, lColAfterMove)] = true;
             if(!onGoalBeforeMove){
                 nbOfBoxesOnGoal++;
             }
         }
         else{
             boxes.get(pBoxIndex).setIsOnGoal(false);
             if(onGoalBeforeMove){
                 nbOfBoxesOnGoal--;
             }
         }
 
         this.g = pParentState.g + 1;
         this.h = Heuristic.getDavidDistanceHeuristic(this);
 
         // Append constructorTime to the constructorTime field
         if (Sokoban.profilingMode)
             State.changeBoxConfigTime = State.changeBoxConfigTime + (System.currentTimeMillis() - changeBoxConfigStartTime);
     }
 
 
     
     /**
      * Creates a string representation for each state which is
      * only dependent on which cells are occupied with boxes
      * (irrespective of which box is where)
      * @return
      */
 
     
     public char getCharLastMove(){
     	return this.lastMoveDir;
     }
     
     public State getParent(){
     	return this.parentState;
     }
     
     public Box getLastMovedBox(){
     	return this.boxes.get(this.lastBoxMovedIndex);
     }
     
 	/**
 	 * Creates a string representation for each state which is
 	 * only dependent on which cells are occupied with boxes
 	 * (irrespective of which box is where)
 	 * @return
 	 */
 	public String hashString(){
 		String[] lStringArray = new String[boxes.size()];
 
 		for (int i = 0; i < boxes.size(); i++){
 			lStringArray[i] = boxes.get(i).hashString();
 		}
 
 		Arrays.sort(lStringArray);
 
 		StringBuilder builder = new StringBuilder();
 		for(String element : lStringArray) {
 			builder.append(element);
 		}
 		return builder.toString();
 	}
 	
 
 	@Override
 	public int hashCode() {
 		//Should be improved!?
 		return hashString().hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		
 		//first part can be removed for better performance?
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		
 		State other = (State) obj;
 		if (this.hashCode() != other.hashCode())
 			return false;
 	
 		
 		if(!Solver.isPathToPath(this, this.playerRow, this.playerCol, other.playerRow, other.playerCol))
 			return false;
 	
 		
 		return true;
 	}
 
 	public int getPlayerRow(){
 		return playerRow;
 	}
 
 	public int getPlayerCol(){
 		return playerCol;
 	}
 
     public int getLastBoxMovedIndex() {
         return lastBoxMovedIndex;
     }
 
     public Vector<Box> getBoxes() {
         return boxes;
     }
 
 	/**
 	 * Does all necessary checks to see if a box is movable to the position.
 	 *
 	 * Checks are:
 	 * 1) no wall in that position
 	 * 2) no box in that position
 	 * 3) no dead lock type 0 in that position (i.e. a corner)
 	 * 4) can player reach the opposite position.
 	 *
 	 * @return
 	 */
 	public boolean tryMove(Box pBox, char pDir){
 
         // Start time
         if (Sokoban.profilingMode)
             tryMoveStartTime = System.currentTimeMillis();
 
 		int lR = pBox.getRow();
 		int lC = pBox.getCol();
 
 		int lMoveToRow = 0;
 		int lMoveToCol = 0;
 		int lPlayerRow = 0;
 		int lPlayerCol = 0;
 
 		boolean lCorrectInput=false;
 
 		switch (pDir) {
 		case 'U':
 			lMoveToRow = lR - 1;
 			lMoveToCol = lC;
 			lPlayerRow = lR + 1;
 			lPlayerCol = lC;
 			lCorrectInput = true;
 			break;
 		case 'D':
 			lMoveToRow = lR + 1;
 			lMoveToCol = lC;
 			lPlayerRow = lR - 1;
 			lPlayerCol = lC;
 			lCorrectInput = true;
 			break;
 		case 'R':
 			lMoveToRow = lR;
 			lMoveToCol = lC + 1;
 			lPlayerRow = lR;
 			lPlayerCol = lC - 1;
 			lCorrectInput = true;
 			break;
 		case 'L':
 			lMoveToRow = lR;
 			lMoveToCol = lC - 1;
 			lPlayerRow = lR;
 			lPlayerCol = lC + 1;
 			lCorrectInput = true;
 			break;
 		}
 
 		if(!lCorrectInput){
 			if (Sokoban.debugMode) System.out.println("StateError: TryMove: wrong direction input");
 		}
 		/*
 		 * All of the below must be valid, add check deadlock later!
 		 */
 		if (isFree(lMoveToRow, lMoveToCol) &&
 				!Board.isDeadLockT0(lMoveToRow, lMoveToCol) &&
 				Solver.isPathToPath(this, playerRow, playerCol, lPlayerRow, lPlayerCol)) {
 
             // Append time
             if (Sokoban.profilingMode)
                 tryMoveTime = tryMoveTime + (System.currentTimeMillis() - tryMoveStartTime);
 
             return true;
         } else return false;
 	}
 
 
     /* This Method returns the gradient value */
     public int getGradValue(Box pBox, char pDir){
 
         switch (pDir) {
             // C = center = current box position
             case 'C':
                 return Board.getGoalGradMerged(pBox.getRow(), pBox.getCol());
             case 'U':
                 return Board.getGoalGradMerged(pBox.getRow() - 1, pBox.getCol());
             case 'D':
                 return Board.getGoalGradMerged(pBox.getRow() + 1, pBox.getCol());
             case 'R':
                 return Board.getGoalGradMerged(pBox.getRow(), pBox.getCol() + 1);
             case 'L':
                 return Board.getGoalGradMerged(pBox.getRow(), pBox.getCol() - 1);
         }
         // if wrong input, return -1.
         return -1;
     }
 
 	public boolean isBox( int pRow, int pCol){
 		/*
 		 * TODO
 		 * 
 		 * Very inefficient way of checking must be rewritten later.
 		 */
 
 		for(Box box : boxes){
 			if (box.getRow() == pRow && box.getCol() == pCol){
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public boolean isFree(int pRow, int pCol){
 		return !Board.isWall(pRow, pCol) && !this.isBox(pRow, pCol);
 	}
 
 
 	private boolean isConnected(int pRow1, int pCol1,  int pRow2 , int pCol2){
 
 		//skrivs av Adam...
 
 		return true;
 	}
 	/**
 	 * Returnerar den cell som expanderades senast innan
 	 *
 	 * @param pRow1
 	 * @param pCol1
 	 * @param pRTarget
 	 * @param pCTarget
 	 * @return
 	 */
 	private Cell cellNeighbour(int pRow1, int pCol1,  int pRTarget , int pCTarget){
 
 
 		//skrivs av Adam...
 
 		return new Cell(0, 0);
 	}
 
 	/**
 	 * Operates on an (empty) Vector with States
 	 * and "fills" with all successor states
 	 *
 	 * @param pStates
 	 */
 	public void allSuccessors(Vector<State> pStates) {
 		/*
 		 * TODO
 		 * 
 		 * Idea taken from HW1
 		 * I guess that the reason for sending in an empty vector that this function
 		 * "operates" on is to avoid incorrect references...
 		 * 
 		 */
 
         // Start the timer
         if (Sokoban.profilingMode)
             allSuccessorsStartTime = System.currentTimeMillis();
 
 		pStates.clear();
 
 		int boxIndex = 0;
 		for (Box box : boxes) {
 
             // If there are reusable states... then use them,
             // Else, create new State
             if (tryMove(box, 'U'))
                 if (!reusableStates.isEmpty()) {
                     pStates.add(reusableStates.pop());
                     pStates.lastElement().changeBoxConfig(this, boxIndex, 'U');
                 } else {
                     pStates.add(new State(this, boxIndex, 'U'));
                 }
             if (tryMove(box, 'D'))
                 if (!reusableStates.isEmpty()) {
                     pStates.add(reusableStates.pop());
                     pStates.lastElement().changeBoxConfig(this, boxIndex, 'D');
                 } else {
                     pStates.add(new State(this, boxIndex, 'D'));
                 }
             if (tryMove(box, 'R'))
                 if (!reusableStates.isEmpty()) {
                     pStates.add(reusableStates.pop());
                     pStates.lastElement().changeBoxConfig(this, boxIndex, 'R');
                 } else {
                     pStates.add(new State(this, boxIndex, 'R'));
                 }
             if (tryMove(box, 'L'))
                 if (!reusableStates.isEmpty()) {
                     pStates.add(reusableStates.pop());
                     pStates.lastElement().changeBoxConfig(this, boxIndex, 'L');
                 } else {
                     pStates.add(new State(this, boxIndex, 'L'));
                 }
             boxIndex++;
         } // End for boxes
 
         // Append time
         if (Sokoban.profilingMode)
             State.allSuccessorsTime = State.allSuccessorsTime + (System.currentTimeMillis() - allSuccessorsStartTime);
 
 	} // End allSuccessors
 
 	/**
 	 * Operates on an (empty) Vector with States
 	 * and "fills" with all successor states
 	 *
 	 * @param pStates
 	 */
 	public void selectiveSuccessors(Vector<State> pStates, int pBoxIndex) {
 
 		pStates.clear();
         /* If a move is possible, then add the new state in the pStates vector */
         if (tryMove(boxes.get(pBoxIndex), 'U'))
             if (!reusableStates.isEmpty()) {
                 pStates.add(reusableStates.pop());
                 pStates.lastElement().changeBoxConfig(this, pBoxIndex, 'U');
             } else {
                 pStates.add(new State(this, pBoxIndex, 'U'));
             }
         if (tryMove(boxes.get(pBoxIndex), 'D'))
             if (!reusableStates.isEmpty()) {
                 pStates.add(reusableStates.pop());
                 pStates.lastElement().changeBoxConfig(this, pBoxIndex, 'D');
             } else {
                 pStates.add(new State(this, pBoxIndex, 'D'));
             }
         if (tryMove(boxes.get(pBoxIndex), 'R'))
             if (!reusableStates.isEmpty()) {
                 pStates.add(reusableStates.pop());
                 pStates.lastElement().changeBoxConfig(this, pBoxIndex, 'R');
             } else {
                 pStates.add(new State(this, pBoxIndex, 'R'));
             }
         if (tryMove(boxes.get(pBoxIndex), 'L'))
             if (!reusableStates.isEmpty()) {
                 pStates.add(reusableStates.pop());
                 pStates.lastElement().changeBoxConfig(this, pBoxIndex, 'L');
             } else {
                 pStates.add(new State(this, pBoxIndex, 'L'));
             }
 
     } // End allSuccessors
 
     /**
      * Not working....
      */
     public void gradientDecentSuccessor(Vector<State> pStates, int pBoxIndex) {
 
             // Set initial null char
             char moveDir = '\0';
             // Get gradient value at current position
             int gradValue = getGradValue(boxes.get(pBoxIndex), 'C');
 
             /* If a move is possible, then add the new state in the pStates vector */
             if (tryMove(boxes.get(pBoxIndex), 'U')) {
                 if (gradValue > getGradValue(boxes.get(pBoxIndex), 'U')) {
                     moveDir = 'U';
                     gradValue = getGradValue(boxes.get(pBoxIndex), 'U');
                 }
             }
 
             if (tryMove(boxes.get(pBoxIndex), 'D')) {
                 if (gradValue > getGradValue(boxes.get(pBoxIndex), 'D')) {
                     moveDir = 'D';
                     gradValue = getGradValue(boxes.get(pBoxIndex), 'D');
                 }
             }
 
             if (tryMove(boxes.get(pBoxIndex), 'R')) {
                 if (gradValue > getGradValue(boxes.get(pBoxIndex), 'R')) {
                     moveDir = 'R';
                     gradValue = getGradValue(boxes.get(pBoxIndex), 'R');
                 }
             }
 
             if (tryMove(boxes.get(pBoxIndex), 'L')) {
                 if (gradValue > getGradValue(boxes.get(pBoxIndex), 'L'))
                     moveDir = 'L';
             }
 
             if (moveDir != '\0') {
                 if (!reusableStates.isEmpty()) {
                     pStates.add(reusableStates.pop());
                     pStates.lastElement().changeBoxConfig(this, pBoxIndex, moveDir);
                 } else {
                     pStates.add(new State(this, pBoxIndex, moveDir));
                 }
             } else {
                 // If it didn't work, call selective...
                 //selectiveSuccessors(pStates, pBoxIndex);
                 // allSuccessors(pStates);
             }
 
     } // End allSuccessors
 
     /**
      * This method returns the index of the box that is blocking the way for another box.
      * The two for-loops loops through a 3 by 3 square to check the surrounding tiles for a lower state.
      *
      * @return the index of a box that blocks the index
      */
     public int getBlockingBoxIndex(int pCurrentBoxIndex) {
         // Get the gradient value at current position
         int lowestGradValue = getGradValue(getBoxes().get(pCurrentBoxIndex), 'C');
         // The blocking box-index
         int newBoxIndex = -1;
         // The coordinates, starting in the upper left corner in the 3 by 3 surrounding
         int upperLeftRow = boxes.get(pCurrentBoxIndex).getRow() - 1;
         int upperLeftCol = boxes.get(pCurrentBoxIndex).getCol() - 1;
 
         // Iterate through every row end every column
         for (int i = 0; i < 3; i++) {
             for (int j = 0; j < 3; j++) {
                 // If the gradient score is lower and there is a box there (and not deadlock)
                 if (Board.getGoalGradMerged(i, j) < lowestGradValue && getBox(i, j) != null) {
                     newBoxIndex = getBox(i, j);
                     lowestGradValue = Board.getGoalGradMerged(upperLeftRow + i, upperLeftCol + j);
                 }
             }
         }// End check surrounding
 
         // return the new box index
         return newBoxIndex;
     }
 
    public void boxJudge(Vector<State> pStates) {

        for (int boxIndex = 0; boxIndex < boxes.size() ; boxIndex++) {
            // If gradient value is grater than some value then use gradientDec
            if (boxes.get(boxIndex).isOnGoal()){
                selectiveSuccessors(pStates, boxIndex);
            }else if (getGradValue(boxes.get(boxIndex), 'C') < 3) {
                gradientDecentSuccessor(pStates, boxIndex);
            } else {
                selectiveSuccessors(pStates, boxIndex);
            }
        }
    }


 
     public boolean isFinalState() {
         return nbOfBoxesOnGoal == Board.getNbOfGoals();
     }
    
    // Return value of cost to path.
    public int getG() {
 	   return this.g;
    }
    
    // Return heuristic value.
    public double getH() {
 	   return this.h;
    }
 
     // Static method to add reusable states from Solver
     public static void addReusableState(State pState) {
         reusableStates.push(pState);
     }
 
     /**
      * @return if the goal is occupied
      */
     public boolean isGoalOccupied(int pGoalIndex) {
         return goalsOccupied[pGoalIndex];
     }
    
 } // End Class State
