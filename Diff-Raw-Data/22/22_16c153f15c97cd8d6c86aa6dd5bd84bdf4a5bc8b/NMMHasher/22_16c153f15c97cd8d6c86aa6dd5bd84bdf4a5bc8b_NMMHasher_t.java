 package edu.berkeley.gamesman.parallel.game.ninemensmorris;
 
 import java.util.Arrays;
 
 import edu.berkeley.gamesman.hasher.DBHasher;
 import edu.berkeley.gamesman.hasher.genhasher.DBInvCalculator;
 import edu.berkeley.gamesman.hasher.invhasher.OptimizingInvariantHasher;
 
 public class NMMHasher extends OptimizingInvariantHasher<NMMState> {
 	/*
 	int blackPiecesPlaced, whitePiecesPlaced;
 	int blackPiecesCaptured, whitePiecesCaptured;
 	/* 9 - blackPiecesPlaced = black Pieces that haven't been placed
 	 * blackPiecesPlaced-blackPiecesCaptured = black pieces on the board.
 	 */
 	
 	int levels;
     int elements;
 	int pieces;
 	int boardSize;
 	DBInvCalculator myUtil;
 	
 	public static int[] makeDigitBase(int pieces, int boardSize) {
 		int[] digitBase = new int[boardSize+5];
 		Arrays.fill(digitBase, 3);
 		for (int i = boardSize; i < boardSize + 4; i++) {
 			digitBase[i] = pieces+1;
 		}
 		digitBase[boardSize+1] = 2;
 		return digitBase;
 	}
 	
 	
 	public NMMHasher(int levels, int elements, int pieces) {
 		super(makeDigitBase(pieces, levels*elements));
 		this.levels = levels;
 		this.pieces = pieces;
 		this.elements = elements;
 		boardSize = levels*elements;
 		myUtil = new DBInvCalculator(boardSize);
 		// TODO Auto-generated constructor stub
 	}
 
 	@Override
 	protected NMMState genHasherNewState() {
 		// TODO Auto-generated method stub
 		
 		NMMState toReturn = new NMMState(this, levels, elements);
 		toReturn.initialize();
 		return toReturn;
 	}
 
 	@Override
 	protected long getInvariant(NMMState state) {
 		// TODO Auto-generated method stub
 		int invariant = 0;
 		if (isEmpty(state)) {
 			return 0;
 		} else {
 			for (int i = Math.max(boardSize,getStart(state)); i < boardSize+4; i++) {
 				invariant <<= 4;
 				invariant |= state.get(i);
 			}
 			invariant <<= 1;
			invariant |= state.get(boardSize+4);
 			return myUtil.getInv(invariant, state);
 		}
 	}
 	
 	/*
 	 * *******************************************
 	 * I ALSO WROTE VALID
 	 */
 	
 	@Override
 	protected boolean valid(NMMState state) {
 		return state.valid();
 
 		
 		
 	}	

	public static void main(String[] args) {
		NMMHasher h = new NMMHasher(3, 8, 9);
		System.out.println(h.numPositions(new int[0]));
	}
 }
