 package edu.berkeley.gamesman.parallel.game.ninemensmorris;
 
 import edu.berkeley.gamesman.game.type.GameValue;
 import edu.berkeley.gamesman.game.util.BitSetBoard;
 import edu.berkeley.gamesman.hasher.counting.CountingState;
 import edu.berkeley.gamesman.hasher.genhasher.GenHasher;
 import edu.berkeley.gamesman.parallel.game.connect4.C4Hasher;
 
 public class NMMState extends CountingState {
 	private final int levelsOfBoxes, elementsInABox, boardSize, pieces;
 
 	
 	public NMMState(NMMHasher myHasher, int levels, int elements) {
 		
 		super(myHasher, levels * elements);
 		this.levelsOfBoxes = levels;
 		this.pieces = myHasher.pieces;
 		this.elementsInABox = elements;
 		this.boardSize = levels * elements;	
 		
 		//changePlace = boardSize - 1;
 		//this.myBoard = new BitSetBoard(elements, levels);
 	}
 	
 	public void initialize() {
 		
 		set(boardSize, pieces);
 		set(boardSize+1, pieces);
 		set(boardSize+2, 0);
 		set(boardSize+3, 0);
 		set(boardSize+4, 1);
 
 	}
 	
 	public int getTurn() {
 		return get(boardSize+4)+1;
 	}
 	
 	public boolean valid() {
 		
 		int oToPlace = get(boardSize);
 		int xToPlace = get(boardSize + 1);
 		int oOnBoard = get(boardSize + 2);
 		int xOnBoard = get(boardSize + 3);
 		int actualXonBoard = 0;
 		int actualOonBoard = 0;
 		boolean rightNumberPieces, oOnBoardIsCorrect, xOnBoardIsCorrect, toPlaceCorrect;
 		
 		//count how many Xs and Os are on the board to check if it matches the prefix.
 		for (int i=0; i<boardSize; i++) {
 			if (get(i) == 1)
 				actualXonBoard++;
 			else if (get(i) == 2)
 				actualOonBoard++;
 		}
 		oOnBoardIsCorrect = actualOonBoard == oOnBoard;
 		xOnBoardIsCorrect = actualXonBoard == xOnBoard;
		if(get(boardSize+4) == 1 ) {
 			toPlaceCorrect = (oToPlace==xToPlace);	
 		}
		else{
			assert get(boardSize+4)==0;
 			toPlaceCorrect =(oToPlace==xToPlace+1);
		}
 		rightNumberPieces = (xOnBoard + xToPlace <= pieces) && (oOnBoard + oToPlace <= pieces);
 		return oOnBoardIsCorrect && xOnBoardIsCorrect && toPlaceCorrect && rightNumberPieces;
 		
 	}
 
 	public char getXPiecesToBePlacedAsChar() {
 		return new Integer(get(boardSize+1)).toString().charAt(0);
 	}
 	
 
 	public char getOPiecesToBePlacedAsChar() {
 		return new Integer(get(boardSize)).toString().charAt(0);
 	}
 
 	
 	public GameValue getValue(int lastTurn) {
 		int thisTurn;
 		int thisTurnPiecesLeft;
 		int thisTurnPiecesToPlace;
 		if (lastTurn == 1) {
 			thisTurnPiecesLeft = get(boardSize+2);
 			thisTurnPiecesToPlace = get(boardSize);
 		} else {
 			thisTurnPiecesLeft = get(boardSize+3);
 			thisTurnPiecesToPlace = get(boardSize+1);
 		}
 		
 		if (thisTurnPiecesToPlace == 0 && thisTurnPiecesLeft < 3)
 			return GameValue.LOSE;
 			
 		return null;
 	}
 
 	public char getTurnAsChar() {
 		
 		return new Integer(get(boardSize+4)).toString().charAt(0);
 	}
 }
