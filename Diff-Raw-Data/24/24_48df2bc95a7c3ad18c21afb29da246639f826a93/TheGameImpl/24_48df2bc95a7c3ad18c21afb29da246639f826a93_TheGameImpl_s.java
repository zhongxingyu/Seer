 package com.RotN.acdc.logic;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import android.content.Context;
 import android.util.Log;
 
 import com.RotN.acdc.logic.CheckerContainer.BoardPositions;
 import com.RotN.acdc.logic.CheckerContainer.GameColor;
 import com.RotN.acdc.logic.TheGame.ButtonState;
 
 public class TheGameImpl {
 	Context fileContext;	
 	private TheGame gammon = new TheGame();	
 			
 	public void setFileContext(Context fileContext) {
 		this.fileContext = fileContext;
 	}
 
 	public TheGame getGammonData() {
 		return gammon;
 	}
 
 	public void setGammonData(TheGame gammon) {
 		this.gammon = gammon;
 	}
 
 	private static final String TAG = TheGameImpl.class.getSimpleName();
 
 	public interface GammonEventHandler{
 		void onBoardUpdate();
 	};
 	
 	private List<GammonEventHandler> handlers = new ArrayList<GammonEventHandler>(); 
 	
 	public void addListener(GammonEventHandler handler) {
 		handlers.add(handler);
 	}
 	
 	private void onBoardUpdate(){
 		for (GammonEventHandler listener : handlers) {
 			listener.onBoardUpdate();
 		}
 	}
 	
 	public void removeListener(GammonEventHandler handler) {
 		handlers.remove(handler);
 	}
 
 	public TheGameImpl() {
 		
 		
 		initializeGame();
 	}
 	
 	public void initializeGame() {		
 		gammon.turn = GameColor.NEITHER;
 		
 		for (CheckerContainer container : gammon.containers) {
 			
 			container.setWhiteCheckerCount(0);
 			container.setBlackCheckerCount(0);
 		}
 		
 		gammon.containers.get(BoardPositions.BLACK_BUNKER.getIndex()).setBlackCheckerCount(15);
 		gammon.containers.get(BoardPositions.WHITE_BUNKER.getIndex()).setWhiteCheckerCount(15);
 		
 		gammon.whiteMovingIn = false;
 		gammon.blackMovingIn = false;
 		gammon.allBlackPiecesOut = false;
 		gammon.allWhitePiecesOut = false;
 		
 		gammon.aceyDeucey = false;
 		
 		gammon.blackDie1 = 0;
 		gammon.blackDie2 = 0;
 		gammon.whiteDie1 = 0;
 		gammon.whiteDie2 = 0;
 		gammon.savedStatesCount = 0;
 				
 		gammon.buttonState = ButtonState.ROLL_FOR_TURN;
 		
 		this.onBoardUpdate();
 	}
 	
 	private int rollDie() {
 		return (int) (1 + Math.random() * 6);
 	}
 	
 	private void nextTurn() {
 		if (canMove() == false) {
 			
 			//acey deucey rolled
 			if ((gammon.blackDie1 + gammon.blackDie2 == 3 ||
 					gammon.whiteDie1 + gammon.whiteDie2 == 3) &&
 					gammon.movesRemaining.size() == 0) {
 				// we get to roll again
 			} else {
 				gammon.movesRemaining.clear();
 				
 				if (gammon.turn == GameColor.WHITE) {
 					gammon.turn = GameColor.BLACK;
 				}
 				else if (gammon.turn == GameColor.BLACK) {
 					gammon.turn = GameColor.WHITE;
 				}
 			}
 			
 			gammon.blackDie1 = 0;
 			gammon.blackDie2 = 0;
 			gammon.whiteDie1 = 0;
 			gammon.whiteDie2 = 0;
 			
 			if (gammon.turn == GameColor.WHITE) {
 				gammon.buttonState = ButtonState.WHITE_ROLL;
 			} else {
 				gammon.buttonState = ButtonState.RED_ROLL;
 			}
 			gammon.savedStatesCount = 0;
 		}
 		
 		this.onBoardUpdate();
 	}
 	
 	private void rollForTurn() {
 		gammon.whiteDie1 = 0;
 		gammon.whiteDie2 = 0;
 		gammon.blackDie1 = 0;
 		gammon.blackDie2 = 0;		
 		
 		gammon.movesRemaining.clear();
 		
 		while (gammon.whiteDie1 == gammon.blackDie1) {
 			gammon.whiteDie1 = rollDie();
 			gammon.blackDie1 = rollDie();
 		}
 		
 		if (gammon.whiteDie1 > gammon.blackDie1) {
 			gammon.turn = GameColor.WHITE;
 		}
 		else if (gammon.blackDie1 > gammon.whiteDie1) {
 			gammon.turn = GameColor.BLACK;
 		}
 		if (gammon.turn == GameColor.WHITE) {
 			gammon.buttonState = ButtonState.WHITE_ROLL;
 		} else {
 			gammon.buttonState = ButtonState.RED_ROLL;
 		}
 		this.onBoardUpdate();
 	}
 	
 	private boolean haveRolled() {
 		return (gammon.blackDie1 != 0 && gammon.blackDie2 != 0) || 
 				(gammon.whiteDie1 != 0 && gammon.whiteDie2 != 0) ||
 				(gammon.blackDie1 != 0 && gammon.whiteDie1 != 0);
 	}
 	
 	public Vector<BoardPositions> getPossibleMoves(BoardPositions pieceLocation, boolean recursion) {
 		Vector<BoardPositions> moves = new Vector<BoardPositions>();		
 		
 		if (hasAChecker(pieceLocation)) {
 			if (haveRolled() == true && onPokey() == false) {
 				if (pieceLocation == CheckerContainer.BoardPositions.WHITE_BUNKER) {
 					checkWhiteBunker(moves, recursion);
 				}
 				else if (pieceLocation == CheckerContainer.BoardPositions.BLACK_BUNKER) {
 					checkBlackBunker(moves, recursion);
 				}
 				else {
 					checkPointMove(pieceLocation, moves, gammon.movesRemaining, gammon.aceyDeucey, recursion);
 				}
 			} else if (haveRolled() == true && onPokey() == true && pieceLocation == BoardPositions.POKEY) {
 				if (gammon.turn == GameColor.WHITE) {
 					checkWhiteBunker(moves, recursion);
 				}
 				else if (gammon.turn == GameColor.BLACK){
 					checkBlackBunker(moves, recursion);
 				}
 			}
 		}
 		
 		return moves;
 	}
 	
 	private boolean hasAChecker(BoardPositions containerPosition) {
 		boolean hasAChecker = false;
 		if (gammon.turn == GameColor.WHITE) {
 			hasAChecker = (gammon.containers.get(containerPosition.getIndex()).getWhiteCheckerCount() > 0);
 		} else {
 			hasAChecker = (gammon.containers.get(containerPosition.getIndex()).getBlackCheckerCount() > 0);
 		}
 		return hasAChecker;
 	}
 	
 	public boolean onPokey() {
 		boolean onPokey = false;
 		
 		if ( (gammon.turn == GameColor.BLACK && gammon.containers.get(BoardPositions.POKEY.getIndex()).getBlackCheckerCount() > 0) ||
 				(gammon.turn == GameColor.WHITE && gammon.containers.get(BoardPositions.POKEY.getIndex()).getWhiteCheckerCount() > 0) ) {
 			onPokey = true;
 		}		
 		return onPokey;
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void checkWhiteBunker(Vector<CheckerContainer.BoardPositions> moves, boolean recursion) {
 		//check that it is white
 		if (gammon.turn == GameColor.WHITE  && 
 				//needs pieces in the bunker/still moving out or something on pokey
 				((gammon.containers.get(BoardPositions.WHITE_BUNKER.getIndex()).getWhiteCheckerCount() > 0 && false == gammon.allWhitePiecesOut)
 						|| onPokey()) )
 		{
 			for (int i = 0; i < gammon.movesRemaining.size(); i++) 
 			{
 				int moveLength = gammon.movesRemaining.get(i);
 				CheckerContainer possibleMove;
 				BoardPositions destMove = BoardPositions.NONE;
 				possibleMove = gammon.containers.get(moveLength);
 				
 				if (false == (possibleMove.getBlackCheckerCount() > 1) ) 
 				{
 					destMove = possibleMove.getPosition();
 					// only add it if we don't already have it
 					if (false == moves.contains(destMove)) {
 						moves.add(destMove);
 					}					
 				}
 				
 				if (destMove != BoardPositions.NONE) {
 					ArrayList<Integer> newMovesAvailable = (ArrayList<Integer>) gammon.movesRemaining.clone();
 					if (gammon.aceyDeucey) {
 						aceyDeuceyChosen(moveLength, newMovesAvailable);
 					} else if (newMovesAvailable.contains(moveLength)) {						
 						newMovesAvailable.remove((Object)moveLength);
 					}
 					
 					if (recursion) {
 						checkPointMove(destMove, moves, newMovesAvailable, false, recursion);
 					}
 				}
 			}
 		}
 	}
 	
 	@SuppressWarnings("unchecked")
 	private void checkBlackBunker(Vector<CheckerContainer.BoardPositions> moves, boolean recursion) {
 		//check that it is black
 		if (gammon.turn == GameColor.BLACK && 
 				// have to have pieces in the bunker or on pokey for this move
 				( (gammon.containers.get(BoardPositions.BLACK_BUNKER.getIndex()).getBlackCheckerCount() > 0 && false == gammon.allBlackPiecesOut)
 						|| onPokey()))
 		{
 			for (int i = 0; i < gammon.movesRemaining.size(); i++)
 			{
 				int moveLength = gammon.movesRemaining.get(i);
 				CheckerContainer possibleMove;
 				BoardPositions destMove = BoardPositions.NONE;
 				//black pieces go backwards. This moves us this direction from the bunker
 				int moveIndex = CheckerContainer.BoardPositions.BLACK_BUNKER.getIndex() - moveLength;
 				possibleMove = gammon.containers.get(moveIndex);
 				
 				if (false == (possibleMove.getWhiteCheckerCount() > 1) )
 				{
 					destMove = possibleMove.getPosition();
 
 					// only add it if we don't already have it
 					if (false == moves.contains(destMove)) {
 						moves.add(destMove);
 					}		
 				}
 				
 				if (destMove != BoardPositions.NONE) {
 					ArrayList<Integer> newMovesAvailable = (ArrayList<Integer>) gammon.movesRemaining.clone();
 					if (gammon.aceyDeucey) {
 						aceyDeuceyChosen(moveLength, newMovesAvailable);
 					} else if (newMovesAvailable.contains(moveLength)) {						
 						newMovesAvailable.remove((Object)moveLength);
 					}
 					
 					if (recursion) {
 						checkPointMove(destMove, moves, newMovesAvailable, false, recursion);
 					}
 				}
 			}
 		}
 	}
 	
 	private void checkPointMove(CheckerContainer.BoardPositions pieceLocation, Vector<CheckerContainer.BoardPositions> moves, ArrayList<Integer> movesAvailable, boolean acdc, boolean recursion) {
 		ArrayList<Integer> newMovesAvailable = new ArrayList<Integer>(movesAvailable);
 		
 		if (onPokey()) {
 			return; // they still need to move pieces out. Get out of here.
 		}
 		if (gammon.turn == GameColor.BLACK)
 		{
 			//it is in the final quadrant only moves into the bunker are allowed
 			if (pieceLocation.getIndex() <= 6) {
 				checkBlackBearingOff(pieceLocation, moves, movesAvailable);
 			} else {
 				for (int i = 0; i < movesAvailable.size(); i++)
 				{
 					int moveLength = movesAvailable.get(i);
 					BoardPositions destMove = BoardPositions.NONE;
 					CheckerContainer possibleMove;
 					//black pieces go backwards. This moves us this direction from the bunker
 					int moveIndex = pieceLocation.getIndex() - moveLength;
 					
 					if (moveIndex > CheckerContainer.BoardPositions.WHITE_BUNKER.getIndex())
 					{
 						possibleMove = gammon.containers.get(moveIndex);
 					
 						if (false == (possibleMove.getWhiteCheckerCount() > 1) )
 						{
 							destMove = gammon.containers.get(moveIndex).getPosition();
 
 							// only add it if we don't already have it
 							if (false == moves.contains(destMove)) {
 								moves.add(destMove);
 							}		
 						}
 					}
 					
 					if (destMove != BoardPositions.NONE) {
 						if (acdc) {
 							aceyDeuceyChosen(moveLength, newMovesAvailable);
 						} else if (newMovesAvailable.contains(moveLength)) {						
 							newMovesAvailable.remove((Object)moveLength);
 						}
 
 						if (recursion) {
 							checkPointMove(destMove, moves, newMovesAvailable, false, recursion);
 						}
 					}
 				}
 			}
 		}
 		else if (gammon.turn == GameColor.WHITE)
 		{
 			if (pieceLocation.getIndex() >= 19) {
 				checkWhiteBearingOff(pieceLocation, moves, movesAvailable);
 			} else {
 				for (int i = 0; i < movesAvailable.size(); i++) {
 					int moveLength = movesAvailable.get(i);
 					BoardPositions destMove = BoardPositions.NONE;
 					CheckerContainer possibleMove;
 					//black pieces go backwards. This moves us this direction from the bunker
 					int moveIndex = pieceLocation.getIndex() + moveLength;				
 					
 					if (moveIndex <= CheckerContainer.BoardPositions.BLACK_BUNKER.getIndex())
 					{
 						possibleMove = gammon.containers.get(moveIndex);
 					
 						if (false == (possibleMove.getBlackCheckerCount() > 1) )
 						{
 							destMove = gammon.containers.get(moveIndex).getPosition();
 
 							// only add it if we don't already have it
 							if (false == moves.contains(destMove)) {
 								moves.add(destMove);
 							}		
 						}
 					}
 					
 					if (destMove != BoardPositions.NONE) {
 						if (acdc) {
 							aceyDeuceyChosen(moveLength, newMovesAvailable);
 						} else if (newMovesAvailable.contains(moveLength)) {						
 							newMovesAvailable.remove((Object)moveLength);
 						}
 
 						if (recursion) {
 							checkPointMove(destMove, moves, newMovesAvailable, false, recursion);
 						}
 					}				
 				}
 			}
 		}
 	}
 	
 	private void checkBlackBearingOff(CheckerContainer.BoardPositions pieceLocation, Vector<CheckerContainer.BoardPositions> moves, ArrayList<Integer> movesAvailable) {
 		// do we even need to check?
 		if (gammon.blackMovingIn) {
 			if (movesAvailable.contains((Object) pieceLocation.getIndex())) { // must make the exact move if possible
 				moves.add(BoardPositions.BLACK_BUNKER);
 			} else {
 				for (Integer moveLength: movesAvailable) {
 					if (gammon.containers.get(moveLength).getBlackCheckerCount() > 0) {
 						// an exact move is possible might as well stop checking
 						return;
 					}
 				}
 				
 				boolean higherLegalMove = false;
 				for (int i = 6; i > pieceLocation.getIndex(); i--) {
 					CheckerContainer point = gammon.containers.get(i);
 					if (point.getBlackCheckerCount() > 0) {
 						higherLegalMove = true;
 					}
 				}
 				
 				boolean homeBoardMove = false;
 				boolean moveIntoBunker = false;
 				for (Integer moveLength: movesAvailable) {
 					Integer moveIndex = pieceLocation.getIndex() - moveLength;
 					if (moveIndex <= 0) {
 						moveIntoBunker = true;
 					} else {
 						CheckerContainer possibleMove = gammon.containers.get(moveIndex);
 						if (possibleMove.getWhiteCheckerCount() < 2) {
 							moves.add(possibleMove.getPosition());
 							homeBoardMove = true;
 						}
 					}					
 				}
 				
 				if (homeBoardMove == false && moveIntoBunker == true && higherLegalMove == false) {
 					moves.add(BoardPositions.BLACK_BUNKER);
 				}
 			}
 		}
 	}
 	
 	private void checkWhiteBearingOff(CheckerContainer.BoardPositions pieceLocation, Vector<CheckerContainer.BoardPositions> moves, ArrayList<Integer> movesAvailable) {
 		// do we even need to check?
 		if (gammon.whiteMovingIn) {
 			int distanceFromBunker = 25 - pieceLocation.getIndex();
 			if (movesAvailable.contains((Object) distanceFromBunker) ) { // must make the exact move if possible
 				moves.add(BoardPositions.WHITE_BUNKER);
 			} else {
 				for (Integer moveLength: movesAvailable) {
 					int containerIndex = 25 - moveLength;
 					if (gammon.containers.get(containerIndex).getWhiteCheckerCount() > 0) {
 						// an exact move is possible might as well stop checking
 						return;
 					}
 				}
 				
 				boolean higherLegalMove = false;
 				for (int i = 19; i < pieceLocation.getIndex(); i++) {
 					CheckerContainer point = gammon.containers.get(i);
 					if (point.getWhiteCheckerCount() > 0) {
 						higherLegalMove = true;
 					}
 				}
 				
 				boolean homeBoardMove = false;
 				boolean moveIntoBunker = false;
 				for (Integer moveLength: movesAvailable) {
 					Integer moveIndex = pieceLocation.getIndex() + moveLength;
 					if (moveIndex >= 25) {
 						moveIntoBunker = true;
 					} else {
 						CheckerContainer possibleMove = gammon.containers.get(moveIndex);
 						if (possibleMove.getBlackCheckerCount() < 2) {
 							moves.add(possibleMove.getPosition());
 							homeBoardMove = true;
 						}
 					}					
 				}
 				
 				if (homeBoardMove == false && moveIntoBunker == true && higherLegalMove == false) {
 					moves.add(BoardPositions.WHITE_BUNKER);
 				}
 			}
 		}
 	}
 	
 	public int getBlackBunkerCount()
 	{
 		return gammon.containers.get(BoardPositions.BLACK_BUNKER.getIndex()).getBlackCheckerCount();
 	}
 	
 	public int getWhiteBunkerCount()
 	{
 		return gammon.containers.get(BoardPositions.WHITE_BUNKER.getIndex()).getWhiteCheckerCount();
 	}
 	
 	public CheckerContainer getContainer(BoardPositions position) {
 		return gammon.containers.get(position.getIndex());
 	}
 	
 	public CheckerContainer getContainer(int index) {
 		return gammon.containers.get(index);
 	}
 	
 	public ArrayList<Move> movePiece(BoardPositions origPos, BoardPositions newPos) {
 		ArrayList<Move> moves = new ArrayList<Move>();
 		
 		// make sure they picked something legit
 		boolean canMove = false;
 		Vector<BoardPositions> possibleMoves = getPossibleMoves(origPos, true);	
 		
 		canMove = possibleMoves.contains(newPos);
 		
 		if (canMove) {	
 			//save where it was for undo purposes
 			storeState();
 			int newIndex = newPos.getIndex();
 			int moveLength = 0;
 			
 			if (newPos == BoardPositions.BLACK_BUNKER) { // can't move into your oppenent's bunker
 				newIndex = BoardPositions.WHITE_BUNKER.getIndex();
 			} else if (newPos == BoardPositions.WHITE_BUNKER) {
 				newIndex = BoardPositions.BLACK_BUNKER.getIndex();
 			} 
 			
 			if (origPos == BoardPositions.POKEY) { // the pokey value changes the distance you went
 				if (gammon.turn == GameColor.BLACK) {
 					moveLength = BoardPositions.BLACK_BUNKER.getIndex() - newIndex;
 				} else {
 					moveLength = newIndex;
 				}
 			}
 			else {
 				moveLength = Math.abs(newIndex - origPos.getIndex());
 			}
 			
 			ArrayList<Integer> howWeGotThere;
 			//decrement remaining moves
 			if (gammon.aceyDeucey) {					
 				gammon.aceyDeucey = false;
 				howWeGotThere = aceyDeuceyChosen(moveLength, gammon.movesRemaining);
 			}
 			else {
 				howWeGotThere = moveUsed(moveLength);
 			}
 			
 			if (newPos != BoardPositions.WHITE_BUNKER && newPos != BoardPositions.BLACK_BUNKER) {
 				moves = pokeyThem(origPos.getIndex(), howWeGotThere);
 			} else {
 				Move bearingOff = new Move(origPos, newPos, gammon.turn, moveLength);
 				moves.add(bearingOff);
 			}
 			
 			//decrement old container
 			gammon.containers.get(origPos.getIndex()).removePiece(gammon.turn);
 
 			
 			CheckerContainer newContainer = gammon.containers.get(newPos.getIndex());
 			newContainer.addPiece(gammon.turn);
 			
 			updateMovingIn();
 			checkForWin();
 			
 			int diceTotal = 0;
 			if (gammon.turn == GameColor.BLACK) {
 				diceTotal = gammon.blackDie1 + gammon.blackDie2;
 			} else {
 				diceTotal = gammon.whiteDie1 + gammon.whiteDie2;
 			}
 			if (gammon.movesRemaining.size() == 0 && diceTotal == 3 && gammon.acdcOrigMove == true) {
 				gammon.acdcOrigMove = false;
 				gammon.aceyDeucey = true;
 				gammon.movesRemaining.add(6);
 				gammon.movesRemaining.add(5);
 				gammon.movesRemaining.add(4);
 				gammon.movesRemaining.add(3);
 				gammon.movesRemaining.add(2);
 				gammon.movesRemaining.add(1);
 			}
 			
 			this.onBoardUpdate();
 		}
 		
 		return moves;
 	}
 	
 	private ArrayList<Move> pokeyThem(Integer origIndex, ArrayList<Integer> howWeGotThere) {
 		boolean gotEm = false;
 		ArrayList<Move> moves = new ArrayList<Move>();
 		int tempIndex = origIndex;
 		for (int index = 0; index < howWeGotThere.size(); index++) {
 			int moveIndex = 0;
 			int moveLength = howWeGotThere.get(index);
 			GameColor pokeyPieceColor = GameColor.NEITHER;
 			if (gammon.turn == GameColor.WHITE){
 				pokeyPieceColor = GameColor.BLACK;
 				if (tempIndex == BoardPositions.POKEY.getIndex()) { //acount for pokey changing the length
 					moveIndex = BoardPositions.WHITE_BUNKER.getIndex() + moveLength;
 				} else {
 					moveIndex = tempIndex + moveLength;
 				}
 			} else {
 				//black pieces go backwards. This moves us this direction from the bunker
 				if (tempIndex == BoardPositions.POKEY.getIndex()) { //acount for pokey changing the length
 					moveIndex = BoardPositions.BLACK_BUNKER.getIndex() - moveLength;
 				} else {
 					moveIndex = tempIndex - moveLength;
 				}
 				pokeyPieceColor = GameColor.WHITE;
 			}
 			CheckerContainer newContainer;
 			newContainer = gammon.containers.get(moveIndex);
 
 			gotEm = movePieceToPokey(newContainer);
 			
 			// record the move
 			if (gotEm) {
 				Move pokeyMove = new Move(newContainer.getPosition(), BoardPositions.POKEY, pokeyPieceColor, -1);
 				moves.add(pokeyMove);
 			}
 			Move pokeyMove = new Move(gammon.containers.get(tempIndex).getPosition(), newContainer.getPosition(), gammon.turn, moveLength);
 			moves.add(pokeyMove);			
 
 			tempIndex = newContainer.getPosition().getIndex();
 		}
 		
 		if (gotEm == false) { // missed them the first time, lets try again.
 			moves.clear();
 			tempIndex = origIndex;
 			for (int index = howWeGotThere.size() - 1; index >= 0; index--) {
 				int moveIndex = 0;
 				int moveLength = howWeGotThere.get(index);
 				GameColor pokeyPieceColor = GameColor.NEITHER;
 				if (gammon.turn == GameColor.WHITE){
 					pokeyPieceColor = GameColor.BLACK;
 					if (tempIndex == BoardPositions.POKEY.getIndex()) { //acount for pokey changing the length
 						moveIndex = BoardPositions.WHITE_BUNKER.getIndex() + moveLength;
 					} else {
 						moveIndex = tempIndex + moveLength;
 					}
 				} else {
 					//black pieces go backwards. This moves us this direction from the bunker
 					if (tempIndex == BoardPositions.POKEY.getIndex()) { //acount for pokey changing the length
 						moveIndex = BoardPositions.BLACK_BUNKER.getIndex() - moveLength;
 					} else {
 						moveIndex = tempIndex - moveLength;
 					}
 					pokeyPieceColor = GameColor.WHITE;
 				}
 				CheckerContainer newContainer;
 				newContainer = gammon.containers.get(moveIndex);
 
 				gotEm = movePieceToPokey(newContainer);
 				
 				// record the move
 				if (gotEm) {
 					Move pokeyMove = new Move(newContainer.getPosition(), BoardPositions.POKEY, pokeyPieceColor, -1);
 					moves.add(pokeyMove);
 				}
 				Move pokeyMove = new Move(gammon.containers.get(tempIndex).getPosition(), newContainer.getPosition(), gammon.turn, moveLength);
 				moves.add(pokeyMove);			
 
 				tempIndex = newContainer.getPosition().getIndex();
 			}
 		}
 		
 		return moves;
 	}
 	
 	private boolean movePieceToPokey(CheckerContainer newContainer) {
 		boolean gotEm = false;
 		
 		//makes sure that we don't steal the last one out of their bunker
 		if (newContainer.getPosition() != BoardPositions.WHITE_BUNKER &&
 				newContainer.getPosition() != BoardPositions.BLACK_BUNKER) {	
 			if (gammon.turn == GameColor.BLACK && newContainer.getWhiteCheckerCount() == 1) {
 				// add a piece to pokey
 				gammon.containers.get(BoardPositions.POKEY.getIndex()).addPiece(GameColor.WHITE);
 				//remove the piece that went to pokey
 				newContainer.removePiece(GameColor.WHITE);
 				gotEm = true;
 			} else if (gammon.turn == GameColor.WHITE && newContainer.getBlackCheckerCount() == 1) {
 				// add a piece to pokey
 				gammon.containers.get(BoardPositions.POKEY.getIndex()).addPiece(GameColor.BLACK);
 				//remove the piece that went to pokey
 				newContainer.removePiece(GameColor.BLACK);
 				gotEm = true;
 			}
 		}
 		
 		return gotEm;
 	}
 	
 	public void undoMove() {
 		if (gammon.savedStatesCount > 0) {
 			int restoreNumber = gammon.savedStatesCount - 1;
 			String FILENAME = "move" + restoreNumber;
 			
 			try {
 				FileInputStream fis = fileContext.openFileInput(FILENAME);
 				ObjectInputStream objIn = new ObjectInputStream(fis);						
 				gammon = (TheGame) objIn.readObject();
 			} catch (IOException e) {
 				e.printStackTrace();
 			} catch (ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 			//Log.d(TAG, "SavedStatesCount: " + gammon.savedStatesCount);
 		}
 		this.onBoardUpdate();
 		
 	}
 	
 	public void storeState() {
 		//just moving pieces for the AI
 		if (fileContext == null) {
 			return;
 		}
 		String FILENAME = "move" + gammon.savedStatesCount;
 		try {
 			FileOutputStream stream = fileContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);
 			ObjectOutputStream objOut = new ObjectOutputStream(stream);
 			objOut.writeObject(gammon);
 			objOut.close();
 			gammon.savedStatesCount++;
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public int getB1DieValue() {
 		return gammon.blackDie1;
 	}
 	
 	public int getB2DieValue() {
 		return this.gammon.blackDie2;
 	}
 	
 	public int getW1DieValue() {
 		return this.gammon.whiteDie1;
 	}
 	
 	public int getW2DieValue() {
 		return this.gammon.whiteDie2;
 	}
 	
 	public String getButtonText() {
 		return this.gammon.buttonState.getText();
 	}
 	
 	public void buttonPushed() {
 		switch (gammon.buttonState) {
 		case ROLL_FOR_TURN:
 			rollForTurn();
 			break;
 		case RED_ROLL:
 		case WHITE_ROLL:
 			roll();
 			break;
 		case TURN_FINISHED:
 			nextTurn();
 			break;
 		default:
 			initializeGame();
 			break;
 		}
 	}
 	
 	private ArrayList<Integer> moveUsed(int moveLength) {
 		ArrayList<Integer> howWeGotThere = new ArrayList<Integer>();
 		//Log.d(TAG, "Move used: " + moveLength);
 		if (gammon.movesRemaining.contains(moveLength)) {
 			howWeGotThere.add(moveLength);
 			gammon.movesRemaining.remove((Object)moveLength);
 		} else if ( (gammon.turn == GameColor.BLACK && gammon.blackMovingIn) ||
 				(gammon.turn == GameColor.WHITE && gammon.whiteMovingIn) ) {
 			int longestMoveAvailable = 0;
 			for (Integer moveLengthAvailable: gammon.movesRemaining) {
 				if (moveLengthAvailable > longestMoveAvailable) {
 					longestMoveAvailable = moveLengthAvailable;
 				}
 			}
 			gammon.movesRemaining.remove((Object) longestMoveAvailable);
 			
 		} else { // we used multiple to get there
 			int sumOfMoves = 0;
 			while (gammon.movesRemaining.size() > 0) {
 				sumOfMoves += gammon.movesRemaining.get(0);
 				howWeGotThere.add(gammon.movesRemaining.get(0));
 				gammon.movesRemaining.remove(0);
 				if (sumOfMoves == moveLength) {
 					break;
 				}
 			}
 		}
 				
 		//Log.d(TAG, "Moves remaining: " + gammon.movesRemaining.size());
 		return howWeGotThere;
 	}
 	
 	private void roll() {
 		gammon.whiteDie1 = 0;
 		gammon.whiteDie2 = 0;
 		gammon.blackDie1 = 0;
 		gammon.blackDie2 = 0;
 		if (gammon.turn == GameColor.BLACK) {
 			gammon.blackDie1 = rollDie();
 			gammon.blackDie2 = rollDie();
 			
 			// for testing
 			//gammon.blackDie1 = 2;
 			//gammon.blackDie2 = 1;
 			
 			gammon.movesRemaining.add(gammon.blackDie1);
 			gammon.movesRemaining.add(gammon.blackDie2);
 			if (gammon.blackDie1 == gammon.blackDie2) {
 				gammon.movesRemaining.add(gammon.blackDie1);
 				gammon.movesRemaining.add(gammon.blackDie1);
 			} else if (gammon.blackDie1 + gammon.blackDie2 == 3) { //acdc
 				gammon.acdcOrigMove = true;
 			}
 		} else {
 			gammon.whiteDie1 = rollDie();
 			gammon.whiteDie2 = rollDie();
 			
 			// for testing
 			//gammon.whiteDie1 = 1;
 			//gammon.whiteDie2 = 2;
 			
 			gammon.movesRemaining.add(gammon.whiteDie1);
 			gammon.movesRemaining.add(gammon.whiteDie2);
 			if (gammon.whiteDie1 == gammon.whiteDie2) {
 				gammon.movesRemaining.add(gammon.whiteDie1);
 				gammon.movesRemaining.add(gammon.whiteDie1);
 			}
 			else if (gammon.whiteDie1 + gammon.whiteDie2 == 3) {
 				gammon.acdcOrigMove = true;
 			}
 		}
 		gammon.buttonState = ButtonState.TURN_FINISHED;
 		this.onBoardUpdate();
 	}
 	
 	private ArrayList<Integer> aceyDeuceyChosen(int adMoveLength, ArrayList<Integer> movesAvailable) {
 		ArrayList<Integer> howWeGotThere = new ArrayList<Integer>();
 		int movesUsed = 0;
 		int acdcNumber = 6;
 		//iterate over all the possible acdc choices starting highest
 		for (; acdcNumber >= 1; acdcNumber--) {
 			int sumOfMoves = 0;
 			howWeGotThere.clear();
 			movesUsed = 0;
 			// maximum of 4 moves
 			while (movesUsed <= 4) {
 				sumOfMoves += acdcNumber;
 				howWeGotThere.add(acdcNumber);
 				movesUsed++;
 				if (sumOfMoves >= adMoveLength) {
 					break;
 				}
 			}
 			if (sumOfMoves == adMoveLength) {
 				break;
 			}
 		}
 		
 		Log.d(TAG, "ACDC number: " + acdcNumber);
 		
 		movesAvailable.clear();		
 		
 		for (int i = 0; i < 4 - movesUsed; i++) {
 			movesAvailable.add(acdcNumber);
 		}
 		
 		return howWeGotThere;
 	}
 	
 	public boolean canMove() {
 		boolean canMove = false;
 		
 		if (gammon.movesRemaining.size() > 0) {
 			for (CheckerContainer container : gammon.containers) {
 				
 				Vector<BoardPositions> options = getPossibleMoves(container.getPosition(), false);
 				if (options.size() != 0) {
 					canMove = true;
 					break;
 				}
 			}
 		}
 		
 		return canMove;
 	}
 	
 	private void updateMovingIn() {
 		if (getBlackBunkerCount() == 0) {
 			gammon.allBlackPiecesOut = true;
 			Log.d(TAG, "Black pulled Freddy");
 		}
 		
 		if (getWhiteBunkerCount() == 0) {
 			gammon.allWhitePiecesOut = true;
 			Log.d(TAG, "White pulled Freddy");
 		}
 		
 		int pieceCount = getWhiteBunkerCount();
 		for (int index = 24; index >= 19; index--) {
 			pieceCount += gammon.containers.get(index).getWhiteCheckerCount();
 		}		
 		if (pieceCount == 15 && gammon.allWhitePiecesOut) {
 			gammon.whiteMovingIn = true;
 		}
 		else {
 			gammon.whiteMovingIn = false;
 		}
 		
 		pieceCount = getBlackBunkerCount();
 		for (int index = 1; index <= 6; index++) {
 			pieceCount += gammon.containers.get(index).getBlackCheckerCount();
 		}		
 		if (pieceCount == 15 && gammon.allBlackPiecesOut) {
 			gammon.blackMovingIn = true;
 		}
 		else {
 			gammon.blackMovingIn = false;
 		}
 	}
 	
 	private void checkForWin() {
 		if (gammon.allWhitePiecesOut && getWhiteBunkerCount() == 15) {
 			gammon.buttonState = ButtonState.WHITE_WON;
 			gammon.movesRemaining.clear();
 			gammon.blackDie1 = 0;
 			gammon.blackDie2 = 0;
 			gammon.whiteDie1 = 0;
 			gammon.whiteDie2 = 0;
 		}
 		else if (gammon.allBlackPiecesOut && getBlackBunkerCount() == 15) {
 			gammon.buttonState = ButtonState.BLACK_WON;
 			gammon.movesRemaining.clear();
 			gammon.blackDie1 = 0;
 			gammon.blackDie2 = 0;
 			gammon.whiteDie1 = 0;
 			gammon.whiteDie2 = 0;
 		}
 		this.onBoardUpdate();
 	}
 	
 	public ArrayList<CheckerContainer> getContainers() {
 		return gammon.containers;
 	}
 
 	public GameColor getTurn() {
 		return gammon.turn;
 	}
 
 	public int getWhiteDie1() {
 		return gammon.whiteDie1;
 	}
 
 	public void setWhiteDie1(int whiteDie1) {
 		gammon.whiteDie1 = whiteDie1;
 	}
 
 	public int getWhiteDie2() {
 		return gammon.whiteDie2;
 	}
 
 	public void setWhiteDie2(int whiteDie2) {
 		gammon.whiteDie2 = whiteDie2;
 	}
 
 	public int getBlackDie1() {
 		return gammon.blackDie1;
 	}
 
 	public void setBlackDie1(int blackDie1) {
 		gammon.blackDie1 = blackDie1;
 	}
 
 	public int getBlackDie2() {
 		return gammon.blackDie2;
 	}
 
 	public void setBlackDie2(int blackDie2) {
 		gammon.blackDie2 = blackDie2;
 	}
 
 	public boolean isWhiteMovingIn() {
 		return gammon.whiteMovingIn;
 	}
 
 	public void setWhiteMovingIn(boolean whiteMovingIn) {
 		gammon.whiteMovingIn = whiteMovingIn;
 	}
 
 	public boolean isBlackMovingIn() {
 		return gammon.blackMovingIn;
 	}
 
 	public void setBlackMovingIn(boolean blackMovingIn) {
 		gammon.blackMovingIn = blackMovingIn;
 	}
 
 	public boolean isAllBlackPiecesOut() {
 		return gammon.allBlackPiecesOut;
 	}
 
 	public void setAllBlackPiecesOut(boolean allBlackPiecesOut) {
 		gammon.allBlackPiecesOut = allBlackPiecesOut;
 	}
 
 	public boolean isAllWhitePiecesOut() {
 		return gammon.allWhitePiecesOut;
 	}
 
 	public void setAllWhitePiecesOut(boolean allWhitePiecesOut) {
 		gammon.allWhitePiecesOut = allWhitePiecesOut;
 	}
 	
 	public ButtonState getButtonState(){
 		return gammon.buttonState;
 	}
 	
 	public int getActiveTurnCheckerCount(BoardPositions container) {
 		if (gammon.turn == GameColor.BLACK) {
 			return this.getContainer(container).getBlackCheckerCount();
 		} else {
 			return this.getContainer(container).getWhiteCheckerCount();
 		}
 	}
 }
 
 
