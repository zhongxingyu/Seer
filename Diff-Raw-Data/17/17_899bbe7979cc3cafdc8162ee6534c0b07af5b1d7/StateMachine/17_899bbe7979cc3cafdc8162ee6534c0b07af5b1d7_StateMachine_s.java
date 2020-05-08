 import java.awt.Point;
 import java.util.*;
 
 //Purpose: Takes ambiguous state-dependent user actions & decides what to do
 //TODO somebody: block state change during animation (real thread blocking)
 public class StateMachine {
     public Grid grid;
     private State s;
     public int movesRemaining;
     private int numRows = 5;
 
     //data valid only in certain states
     private Piece selectedPiece;
     java.util.List<Point> prevPositions;
     Point prevDirection; //vector: Destination - Start
 
     //need to run() with a "NewGame" event before anything will happen
     public StateMachine(int rowSize, int colSize) {//{{{
         setState(State.GAME_OVER);
         //selectedPiece is not valid until MOVE states
 		grid = new Grid(rowSize, colSize);  
 		//win = 0;
         movesRemaining = maxMoves();
     }//}}}
 
     private int maxMoves() { return 10*numRows; }
 
     public State getState() { return s; }
     
     public Boolean outOfMoves() {//{{{ 
         if(movesRemaining <= 0) return true;
         return false;
     }//}}}
 
     public String run(String evtType, Point p) {//{{{
         //advance the state
         if(evtType == "GameOver") {
             setState(State.GAME_OVER);
         } else if (evtType == "NewGame") {
             newGame(p);
             setState(State.PLAYER_SELECT);
         } else if(evtType == "Click") {
             handleClick(p);
         } else if(evtType == "RClick") {
             handleRClick();
         }
         return messageForCurrentState();
     }//}}}
 
     private String messageForCurrentState() {//{{{
        String turnMsg = "Turn #" + (maxMoves() - movesRemaining) + " ";
         switch(s) {
             case PLAYER_SELECT:
                return turnMsg + "STATE: PLAYER_SELECT - White, please select a piece";
             case ENEMY_SELECT:
                return turnMsg + "STATE: ENEMY_SELECT - Black, please select a piece";
             case MOVE:
                return turnMsg + "STATE: MOVE - Please move your selected piece";
             case MOVE_AGAIN:
                return turnMsg + "STATE: MOVE_AGAIN - Please move the same piece again. Currently, the decline functionality has not been implemented.";
             case GAME_OVER:
                return "STATE: GAME_OVER - To play again, please reset the board with the NEW_GAME button.";
         }
         return "ERROR: Reached invalid state";
     }//}}}
 
     private void setState(State newState) {//{{{
         s = newState;
     }//}}}
 
     private void handleClick(Point globalPt) {//{{{
         //get clicked pt in grid coordinates
         Point pt = Grid.asGridCoor(globalPt);
         if(!grid.isOnGrid(pt)) { return; }
         //figure out if it is a space:0, ally piece:1, or enemy:-1
         int id = grid.getState()[pt.x][pt.y];
 
         System.out.println("Clicked: " + pt.x + ", " + pt.y);
         
         switch(s) {
             case PLAYER_SELECT:
                 if(id == 1) { 
                 	System.out.println("PLAYER_SELECT");
                     selectPiece(pt);
                     setState(State.MOVE);
                 } //else do nothing
                 break;
             case ENEMY_SELECT:
                 if(id == -1) { 
                 	System.out.println("ENEMY_SELECT");
                     selectPiece(pt);
                     setState(State.MOVE);
                 } //else do nothing
                 break;
             case MOVE:
                 if(id == 0) {
                     if (grid.isValidMove(selectedPiece.position(), pt)) {
                     	System.out.println("MOVE");
                         this.movePiece(pt);
                         handleChainedMove(); //sets next state
                     } else { grid.illegalMove(); }
                 }
                 break;
             case MOVE_AGAIN:
                 if(id == 0) {
                     //selectedPiece has been updated since last move
                     if(grid.isValidDoubleMove(selectedPiece.position(), pt, prevPositions, prevDirection)) {
                     	System.out.println("MOVE AGAIN");
                         this.movePiece(pt);
                         handleChainedMove(); //sets next state
                     } else { grid.illegalDoubleMove(); }
                 }
                 break;
             //the other states do not respond to "Click" events
         }
     }//}}}
 
     private void handleRClick() {//{{{
         switch(s) {
             case MOVE:
                 //De-select, revert other altered data
                 Boolean playerTurn = this.isPlayerTurn(); //order-dependent
                 deselectPiece();
                 selectedPiece = null;
                 if(playerTurn) {
                     setState(State.PLAYER_SELECT);
                 } else {
                     setState(State.ENEMY_SELECT);
                 }
                 break;
             case MOVE_AGAIN:
                 //decline to move
                 endTurn();
                 break;
             //the other states do not respond to "RClick" events
         }
     }//}}}
 
     private void newGame(Point p) {//{{{
         //the input is the board size #rows by #columns
 //        numRows = p.x; // what is this supposed to be for? runtime error if i keep it
         clearTempData();
         movesRemaining = maxMoves();
         grid.reset();
     }//}}}
 
     private void endTurn() {//{{{
         if(selectedPiece.isPlayer()) {
             setState(State.ENEMY_SELECT);
         } else {
             setState(State.PLAYER_SELECT);
         }
         clearTempData();
         if(outOfMoves()) { 
             grid.loseMessage();
             this.run("GameOver", null);
             return;
         }
         movesRemaining--;
     }//}}}
 
     private void handleChainedMove() {//{{{
         if(grid.canMoveAgain(selectedPiece.position(), prevPositions, prevDirection)) {
             grid.multiTurnMessage();
             selectPiece(selectedPiece.position()); //to rehighlight
             setState(State.MOVE_AGAIN); 
         } else {
             endTurn();
         }
     }//}}}
 
     private void clearTempData() {//{{{
         selectedPiece = null;
         prevPositions = new ArrayList<Point>();
         prevDirection = null;
     }//}}}
 
     private void selectPiece(Point pt) {//{{{
         selectedPiece = grid.getPieceAt(pt);
         selectedPiece.highlight();
         grid.repaint();
     }//}}}
 
     private void deselectPiece() {//{{{
         grid.getPieceAt(selectedPiece.position()).unhighlight();
         grid.repaint();
     }//}}}
 
     private void movePiece(Point pt) {//{{{
         deselectPiece();
 
         Point oldPt = selectedPiece.position();
         prevPositions.add(oldPt);
         prevDirection = getDirection(oldPt, pt);
         grid.movePiece(selectedPiece.position(), pt);
         //propagate updated position to local copy
         selectedPiece = grid.getPieceAt(pt);
     }//}}}
 
     public Boolean isPlayerTurn() {//{{{
         //order dependent
         if(s == State.PLAYER_SELECT) { return true; }
         if(s == State.ENEMY_SELECT) { return false; }
         if(selectedPiece.isPlayer()) { return true; }
         /*else*/ return false;
     }//}}}
 
     private Point getDirection(Point a, Point b) { //{{{
         return Vector.subtract(b,a);
     }//}}}
     
     private Point getPointInDirection(Point a, int direction) {//{{{
     	Point p;
     	switch(direction) {
     		case 0: 
     			p = new Point(a.x, a.y-1);
     			break;
     		case 1:
     			p = new Point(a.x+1, a.y-1);
     			break;
     		case 2:
     			p = new Point(a.x+1, a.y);
     			break;
     		case 3: 
     			p = new Point(a.x+1, a.y+1);
     			break;
     		case 4:
     			p = new Point(a.x, a.y+1);
     			break;
     		case 5: 
     			p = new Point(a.x-1, a.y+1);
     			break;
     		case 6:
     			p = new Point(a.x-1, a.y);
     			break;
     		case 7:
     			p = new Point(a.x-1, a.y-1);
     			break;
     		default:
     			p = null;
     			break;
     	}    	
     	return p;
     }//}}}
 
 }
