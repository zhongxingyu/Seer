 package com.Grateds.Reversi.AI;
 
 import com.Grateds.Reversi.AI.Agent.MoveCoord;
 import com.Grateds.Reversi.CONTROLLER.*;
 import com.Grateds.Reversi.MODEL.Board;
 import com.Grateds.Reversi.MODEL.Cell;
 
 import java.util.ArrayList;
 import java.util.Vector;
 
 public class AI {
 	
 	private static final int[] sOFFSET_MOVE_ROW = {-1, -1, -1,  0,  0,  1,  1,  1}; // move offset for row 
 	private static final int[] sOFFSET_MOVE_COL = {-1,  0,  1, -1,  1, -1,  0,  1}; // move offset for column	
 	public static final int sSUGGEST_BLACK_PIECE = 2; // susggest piece for black 
 	public static final int sSUGGEST_WHITE_PIECE = 1; // susggest piece for white 
 	private static int sEMPTY_PIECE = 0;
 	private static int sBLACK_PIECE = 2;
 	private static int sWHITE_PIECE = 1;
 	private Vector<String> mMoveList;
 	private Agent mAIAgent; // AI agent 
 	
 	public AI(){
 		mMoveList = new Vector<String>(); // init move list
 		//mAIAgent = new NegaScoutAgent(); // set up AI agent -> developing
 	} // end constructor
 	
 	public Vector<String> getMoveList() {
 	    	return mMoveList; // -> developing
 	} // end getMoveList
 	
 	 //Check whether a move is valid
 	 //parameters: board the board - piece: the piece need to check - row: row of the move - col: column of the move
 	 //return: true if the move is valid, false otherwise	 
 	public static boolean isValidMove(Board b, int piece, int row, int col) {
 		// pre: 1 <= piece <=2
 		// pos: true if the move is valid, false otherwise		
 		if (b.get(row, col) != sEMPTY_PIECE) // check whether this square is empty
 			return false;		
 		int oppPiece = (piece == sBLACK_PIECE) ? sWHITE_PIECE : sBLACK_PIECE;
 		boolean isValid = false;
 		// check 8 directions
 		for (int i = 0; i < 8; ++i) {
 			int curRow = row + sOFFSET_MOVE_ROW[i];
 			int curCol = col + sOFFSET_MOVE_COL[i];
 			boolean hasOppPieceBetween = false;
 			while (curRow >=0 && curRow < 8 && curCol >= 0 && curCol < 8) {	
 				if (b.get(curRow, curCol) == oppPiece)
 					hasOppPieceBetween = true;
 				else if ((b.get(curRow,curCol) == piece) && hasOppPieceBetween)
 				{
 					isValid = true;
 					break;
 				}
 				else
 					break;
 				curRow += sOFFSET_MOVE_ROW[i];
 				curCol += sOFFSET_MOVE_COL[i];
 			}
 			if (isValid)
 				break;
 		}
 		return isValid;
 	} // end isValidMove
 	
 	//Finds valid moves for specific piece -> Encuentra movimientos válidos para pieza específica
 	//parameters: board the board - piece the piece need to find move - isSuggest true to indicate suggested pieces on the board	 
 	public static ArrayList<Integer> findValidMove(Board b, int piece, boolean isSuggest) {
 		// pre: 1 <= piece <=2
 		// pos: return an array list of moves
//		int suggestPiece = (piece == sBLACK_PIECE) ? sSUGGEST_BLACK_PIECE : sSUGGEST_WHITE_PIECE;
 		ArrayList<Integer> moveList = new ArrayList<Integer>();
 		for (int i = 0; i < 8; ++i)
 			for (int j = 0; j < 8; ++j) {
 				// clean the suggest piece before
//				if (b.get(i,j) == sSUGGEST_BLACK_PIECE || b.get(i,j) == sSUGGEST_WHITE_PIECE)
//					b.set(i,j,sEMPTY_PIECE);	
				if (isValidMove(b,piece, i, j)){
					System.out.println("("+i+","+j+")");
 					moveList.add(i);
 					moveList.add(j);
 					// if we want suggestion, mark on board
//					if (isSuggest)
//						b.set(i,j,suggestPiece);
 				}
 			}
 		return moveList;
 	} // end findValidMove
 	
 }
 
