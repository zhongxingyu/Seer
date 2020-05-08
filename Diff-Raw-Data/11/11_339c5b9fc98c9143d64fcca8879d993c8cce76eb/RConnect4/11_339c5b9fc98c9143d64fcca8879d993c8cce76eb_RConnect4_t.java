 package edu.berkeley.gamesman.game;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 
 import edu.berkeley.gamesman.core.Configuration;
 import edu.berkeley.gamesman.core.ItergameState;
 import edu.berkeley.gamesman.core.PrimitiveValue;
 import edu.berkeley.gamesman.core.TieredIterGame;
 import edu.berkeley.gamesman.game.util.PieceRearranger;
 import edu.berkeley.gamesman.util.Pair;
 import edu.berkeley.gamesman.util.Util;
 
 /**
  * Implementation of Connect 4 using the general IterArrangerHasher
  * 
  * @author DNSpies
  */
 public class RConnect4 extends TieredIterGame {
 	private final int[][] indices;
 	private final int[] colHeights;
 	private final int piecesToWin;
 	private final int gameHeight, gameWidth;
 	private final ArrayList<Pair<Integer, Integer>> pieces;
 	private final int[] moveTiers;
 	private int tier;
 	private char turn;
 	private PieceRearranger iah;
 
 	/**
 	 * @param conf The configuration object
 	 */
 	public RConnect4(Configuration conf) {
 		super(conf);
 		gameWidth = Integer.parseInt(conf.getProperty("gamesman.game.width", "7"));
		gameHeight = Integer.parseInt(conf.getProperty("gamesman.game.height", "6"));
		piecesToWin = Integer.parseInt(conf.getProperty("connect4.pieces", "4"));
 		indices = new int[gameHeight][gameWidth];
 		pieces = new ArrayList<Pair<Integer, Integer>>(gameWidth * gameHeight);
 		moveTiers = new int[gameWidth];
 		colHeights = new int[gameWidth];
 	}
 
 	@Override
 	public RConnect4 clone() {
 		RConnect4 other = new RConnect4(conf);
 		if(iah!=null)
 			other.setToString(stateToString());
 		return other;
 	}
 
 	@Override
 	public String displayState() {
 		String s = stateToString();
 		StringBuffer str = new StringBuffer((gameWidth + 3) * gameHeight);
 		for (int row = gameHeight - 1; row >= 0; row--)
 			str.append("|"
 					+ s.substring(row * gameWidth, (row + 1) * gameWidth)
 					+ "|\n");
 		return str.toString();
 	}
 
 	@Override
 	public ItergameState getState() {
 		return new ItergameState(tier, iah.getHash());
 	}
 
 	@Override
 	public int getTier() {
 		return tier;
 	}
 
 	@Override
 	public boolean hasNextHashInTier() {
 		return iah.hasNext();
 	}
 
 	@Override
 	public void nextHashInTier() {
 		iah.next();
 	}
 
 	@Override
 	public BigInteger numHashesForTier() {
 		return iah.arrangements;
 	}
 
 	@Override
 	public int numStartingPositions() {
 		return 1;
 	}
 
 	/*
 	 * Look for pieces that might have been the last move made (at the top of
 	 * they're column and of the right color. Then return lose if the piece is
 	 * part of a four-in-a-row. Otherwise return Tie or Undecided depending on
 	 * whether the board is full or not.
 	 */
 	@Override
 	public PrimitiveValue primitiveValue() {
 		char oppTurn = turn == 'O' ? 'X' : 'O';
 		boolean openColumns = false;
 		for (int col = 0; col < gameWidth; col++) {
 			if (colHeights[col] > 0
 					&& get(colHeights[col]-1,col) == oppTurn
 					&& checkLastWin(colHeights[col] - 1, col))
 				return PrimitiveValue.LOSE;
 			else if(colHeights[col]<gameHeight)
 				openColumns = true;
 		}
 		if (openColumns)
 			return PrimitiveValue.UNDECIDED;
 		else
 			return PrimitiveValue.TIE;
 	}
 
 	/*
 	 * Looks for a win that uses the given piece.
 	 */
 	private boolean checkLastWin(final int row, final int col) {
 		char turn = get(row, col);
 		int ext;
 		int stopPos;
 		char p;
 
 		// Check horizontal win
 		ext = 1;
 		stopPos = Math.min(col, piecesToWin - ext);
 		for (int i = 1; i <= stopPos; i++) {
 			p = get(row, col - i);
 			if (p == turn)
 				ext++;
 			else
 				break;
 		}
 		stopPos = Math.min(gameWidth - 1 - col, piecesToWin - ext);
 		for (int i = 1; i <= stopPos; i++) {
 			p = get(row, col + i);
 			if (p == turn)
 				ext++;
 			else
 				break;
 		}
 		if (ext >= piecesToWin)
 			return true;
 
 		// Check DownLeft/UpRight Win
 		ext = 1;
 		stopPos = Math.min(Math.min(row, col), piecesToWin - ext);
 		for (int i = 1; i <= stopPos; i++) {
 			p = get(row - i, col - i);
 			if (p == turn)
 				ext++;
 			else
 				break;
 		}
 		stopPos = Math.min(Math.min(gameHeight - 1 - row, gameWidth - 1 - col),
 				piecesToWin - ext);
 		for (int i = 1; i <= stopPos; i++) {
 			p = get(row + i, col + i);
 			if (p == turn)
 				ext++;
 			else
 				break;
 		}
 		if (ext >= piecesToWin)
 			return true;
 
 		// Check UpLeft/DownRight Win
 		ext = 1;
 		stopPos = Math.min(Math.min(gameHeight - 1 - row, col), piecesToWin - ext);
 		for (int i = 1; i <= stopPos; i++) {
 			p = get(row + i, col - i);
 			if (p == turn)
 				ext++;
 			else
 				break;
 		}
 		stopPos = Math.min(Math.min(row, gameWidth - 1 - col), piecesToWin - ext);
 		for (int i = 1; i <= stopPos; i++) {
 			p = get(row - i, col + i);
 			if (p == turn)
 				ext++;
 			else
 				break;
 		}
 		if (ext >= piecesToWin)
 			return true;
 
 		// Check Vertical Win: Since it's assumed x,y is on top, it's only
 		// necessary to look down, not up
 		if (row >= piecesToWin - 1)
 			for (ext = 1; ext < piecesToWin; ext++) {
 				if (get(row - ext, col) != turn)
 					break;
 			}
 		if (ext >= piecesToWin)
 			return true;
 		return false;
 	}
 
 	private char get(int row, int col) {
 		return iah.get(indices[row][col]);
 	}
 
 	@Override
 	public void setStartingPosition(int n) {
 		setTier(0);
 	}
 
 	@Override
 	public void setState(ItergameState pos) {
 		setTier(pos.tier());
 		iah.unHash(pos.hash());
 	}
 
 	@Override
 	public void setTier(int tier) {
 		int colHash = 1;
 		this.tier = tier;
 		pieces.clear();
 		int numPieces = 0;
 		StringBuilder s = new StringBuilder(gameWidth * gameHeight);
 		for (int col = 0; col < gameWidth; col++) {
 			colHeights[col] = tier % (gameHeight + 1);
 			numPieces += colHeights[col];
 			tier /= (gameHeight + 1);
 			int row;
 			for (row = 0; row < colHeights[col]; row++) {
 				s.append('T');
 				indices[row][col] = pieces.size();
 				pieces.add(new Pair<Integer, Integer>(row, col));
 			}
 			for (; row < gameHeight; row++)
 				indices[row][col] = pieces.size();
 			if (colHeights[col] < gameHeight) {
 				s.append(' ');
 				pieces.add(new Pair<Integer, Integer>(row, col));
 				moveTiers[col] = this.tier + colHash;
 			}
 			colHash *= gameHeight + 1;
 		}
 		try {
 			iah = new PieceRearranger(s.toString(), numPieces / 2,
 					(numPieces + 1) / 2);
 			turn = (numPieces % 2 == 1) ? 'O' : 'X';
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	public BigInteger numHashesForTier(int tier) {
 		int totPieces = 0;
 		for (int col = 0; col < gameWidth; col++) {
 			totPieces += tier % (gameHeight + 1);
 			tier /= (gameHeight + 1);
 		}
 		return BigInteger.valueOf(Util.nCr(totPieces, totPieces / 2));
 	}
 
 	@Override
 	public void setToString(String pos) {
 		tier = 0;
 		int colTier = 1;
 		StringBuilder iahString = new StringBuilder(gameHeight*gameWidth);
 		for(int col = 0; col < gameWidth; col++){
 			for(int row = 0; row < gameHeight; row++){
 				char c = pos.charAt(row*gameWidth+col);
 				if(c == ' '){
 					iahString.append(' ');
 					break;
 				}else{
 					tier+=colTier;
 					iahString.append(c);
 				}
 			}
 			colTier *= gameHeight + 1;
 		}
 		setTier(tier);
 		try {
 			iah = new PieceRearranger(iahString.toString());
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public String stateToString() {
 		StringBuilder board = new StringBuilder(gameHeight * gameWidth);
 		for (int row = 0; row < gameHeight; row++) {
 			for (int col = 0; col < gameWidth; col++) {
 				board.append(get(row, col));
 			}
 		}
 		return board.toString();
 	}
 
 	@Override
 	public Collection<Pair<String, ItergameState>> validMoves() {
 		Collection<Pair<Integer, BigInteger>> children = iah
 				.getChildren(turn);
 		ArrayList<Pair<String, ItergameState>> moves = new ArrayList<Pair<String, ItergameState>>(
 				children.size());
 		for (Pair<Integer, BigInteger> p : children) {
 			int col = pieces.get(p.car).cdr;
 			moves.add(new Pair<String, ItergameState>("c" + col,
 					new ItergameState(moveTiers[col], p.cdr)));
 		}
 		return moves;
 	}
 
 	@Override
 	public String describe() {
 		return "IAH Connect 4";
 	}
 
 	@Override
 	public int numberOfTiers() {
 		return (int) Util.longpow(gameHeight + 1, gameWidth);
 	}
 
 }
