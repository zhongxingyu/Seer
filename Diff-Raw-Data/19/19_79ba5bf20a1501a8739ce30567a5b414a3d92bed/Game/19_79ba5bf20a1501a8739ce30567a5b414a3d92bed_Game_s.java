 import java.io.Serializable;
 import java.util.ArrayList;
 
 public class Game implements Serializable {
 	private static final long serialVersionUID = 1L;
 	public static int count = 0;//Note: this is only "seen" on the computer hosting the server. Other clients don't get Game's static variables
 
 	public Player pW = null;
 	public Player pB = null;
 
 	public Tile[][] board = new Tile[8][8];
 	public Player turn = null;
 	public int turnCount = 0;
 	public int id;
 	private Player winner = null;
 	public void setWinner(Player p) {
 		winner = p;
 	}
 	public void setWinner(PieceColor pc) {
 		winner = getPlayer(pc);
 	}
 	public Player getWinner() {
 		return winner;
 	}
 
 	public Game() {
 		this.id = count++;
 		pW = new Player(PieceColor.W, id);
 		pB = new Player(PieceColor.B, id);
 		pW.opponent = pB;
 		pB.opponent = pW;
 		turn = pW;
 		Player p = pB;
 		for(int i = 0;i<8;i++) {
 			if(i == 4) p = pW;
 			for(int j = 0;j<8;j++) {
 				board[i][j] = new Tile(j,i);
 				if(i == 0 || i == 7) {
 					board[i][j].addPiece(p.pieceList.get(j));
 				}
 				else if(i == 1 || i == 6) {
 					board[i][j].addPiece(p.pieceList.get(8+j));
 				}
 			}
 		}
 	}
 	public static boolean isPointOnBoard(int x, int y) {
 		return (x >= 0 && x < 8 && y >= 0 && y < 8);
 	}
 	public String toReadableString() {
 		String returner = "";
 		for(int i = 0;i<8;i++) {
 			for(int j = 0;j<8;j++) {
 				if(board[i][j].getPiece() == null) returner += " ";
 				else returner += board[i][j].getPiece().getTileCode();
 			}
 			returner +="\n";
 		}
 		return returner;
 	}
 	public Player getOpponent() {
 		return turn.opponent;
 	}
 	public Player getPlayer(PieceColor pc) {
 		Player player;
 		if(pc == PieceColor.W) {
 			player = pW;
 		}
 		else {
 			player = pB;
 		}
 		return player;
 	}
 	
 	public boolean isLegalMove(Move m) {
 		if(!m.player.equals(turn)) return false;
 		
 		ArrayList<Move> legalMoveList;
 		boolean mInLegalMoveList = false;
 		if(m.moveType == Move.MoveType.PLACEMENT) {
 			m = new Move(m, this);
 			legalMoveList = m.piece.getLegalPlacement(this);
 		}
 		else {
 			m.piece = board[m.piece.getY()][m.piece.getX()].getPiece();//dereference from the Game, necessary for server
 			m.toTile = board[m.toTile.y][m.toTile.x];
 			if(m.piece instanceof Pawn && m.toTile.getPiece() == null) {
 				if(m.piece.getX() != m.toTile.x) m.moveType = Move.MoveType.EN_PASSANT;
 			}
 			else if(m.piece instanceof King && Math.abs(m.toTile.x-m.piece.getX()) == 2) {
 				m.moveType = Move.MoveType.CASTLE;
 			}
 			
 			legalMoveList = m.piece.getLegalMoves(this, false);
 		}
 		for(Move lm : legalMoveList) {//legalMoveList.contains(m) doesn't seem to work
 			if(lm.equals(m)) {
 				mInLegalMoveList = true;
 			}
 		}
 		return mInLegalMoveList;
 	}
 
 	public boolean applyMove(Move m) {//returns true if applied
 		boolean mInLegalMoveList = isLegalMove(m);//handles the setting of m.moveType for EN_PASSANT and CASTLE
 		m = new Move(m, this);//because the PLACEMENT one just instantiates a new one over the parameter reference
 
 		if(mInLegalMoveList) {
 			if(m.moveType == Move.MoveType.EN_PASSANT) {
 				Piece enPassantPawn = board[m.piece.getY()][m.toTile.x].getPiece();
 				getOpponent().pieceList.remove(enPassantPawn);
 				getOpponent().deadPieces.add(enPassantPawn);
 				board[m.piece.getY()][m.toTile.x].addPiece(null);
 			}
 			if(m.moveType == Move.MoveType.CASTLE) {
 				if(m.toTile.x - m.piece.getX() > 0) {
 					board[m.toTile.y][m.toTile.x-1].addPiece(board[m.toTile.y][m.toTile.x+1].getPiece());
 					board[m.toTile.y][m.toTile.x+1].addPiece(null);
 				}
 				else {
 					board[m.toTile.y][m.toTile.x+1].addPiece(board[m.toTile.y][m.toTile.x-2].getPiece());
 					board[m.toTile.y][m.toTile.x-2].addPiece(null);
 				}
 			}
 			getOpponent().pieceList.remove(m.toTile.getPiece());
 			if(m.toTile.getPiece() != null) {
 				getOpponent().deadPieces.add(m.toTile.getPiece());
 			}
 
 			if(m.moveType != Move.MoveType.PLACEMENT) {//order matters
 				m.piece.loc.addPiece(null);
 			}
 			else {
 				m.player.heldPieces.remove(m.piece);
 				m.player.pieceList.add(m.piece);
 			}
 			m.toTile.addPiece(m.piece);
 
 			if(m.toTile.getPiece() instanceof Pawn) {
 				Piece promotion = Move.MoveType.getPromotion(m.moveType, turn);
 				
 				if(promotion != null) {
					promotion.originalType = PieceType.P;
 					turn.pieceList.remove(m.toTile.getPiece());
 					turn.pieceList.add(promotion);
 					m.toTile.addPiece(promotion);
 				}
 			}
 
 			m.toTile.getPiece().lastTurnMoved = turnCount;
 			m.toTile.getPiece().numOfMovesMade++;
 			turnCount++;
 			turn = getOpponent();
 		}
 		return mInLegalMoveList;
 	}
 	public boolean leavesPlayerInCheck(Move m) {//returns true if move leaves the user in check
 		m = new Move(m, this);//dereference from the Game, necessary for server
 		Piece toTilePiece = m.toTile.getPiece();
 		Piece enPassantPawn = null;
 		if(m.moveType != Move.MoveType.PLACEMENT) {
 			enPassantPawn = board[m.piece.getY()][m.toTile.x].getPiece();
 		}
 		int fromX = -1;
 		int fromY = -1;
 		if(m.moveType != Move.MoveType.PLACEMENT) {
 			fromX = m.piece.getX();//necessary because after moving the m.piece, there's no other reference to its original location
 			fromY = m.piece.getY();
 		}
 
 		if(toTilePiece != null) {
 			getOpponent().pieceList.remove(toTilePiece);
 			getOpponent().deadPieces.add(toTilePiece);
 		}
 		if(m.moveType == Move.MoveType.EN_PASSANT) {
 			getOpponent().pieceList.remove(enPassantPawn);
 			getOpponent().deadPieces.add(enPassantPawn);
 		}
 
 		if(m.moveType != Move.MoveType.PLACEMENT) {//order matters
 			m.piece.loc.addPiece(null);
 		}
 		m.toTile.addPiece(m.piece);
 
 		
 		boolean inCheck = inCheck();
 
 		
 		m.toTile.addPiece(toTilePiece);
 		if(m.moveType != Move.MoveType.PLACEMENT) {
 			board[fromY][fromX].addPiece(m.piece);
 		}
 		else {
 			m.piece.loc = null;
 		}
 
 		if(m.moveType == Move.MoveType.EN_PASSANT) {
 			getOpponent().pieceList.add(enPassantPawn);
 			getOpponent().deadPieces.remove(enPassantPawn);
 		}
 		if(toTilePiece != null) {
 			getOpponent().pieceList.add(toTilePiece);
 			getOpponent().deadPieces.remove(toTilePiece);
 		}
 
 		return inCheck;
 	}
 
 	public boolean inCheck() {
 		Player currentPlayer = turn;
 		Player opp = this.getOpponent();
 
 		turn = opp;//needed for testing other player; yes its necessary (confirmed). MAYBE NOT SINCE I CHANGED HOW PIECES WORK
 		boolean inCheck = false;
 		for(Piece p : opp.pieceList) {
 			for(Move m : p.getLegalMoves(this, true)) {
 				if(m.toTile == currentPlayer.king.loc) {
 					inCheck = true;
 				}
 			}
 		}
 		turn = currentPlayer;
 		return inCheck;
 	}
 }
