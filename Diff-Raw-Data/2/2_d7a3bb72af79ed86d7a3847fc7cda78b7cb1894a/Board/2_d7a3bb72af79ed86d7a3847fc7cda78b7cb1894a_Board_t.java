 package game;
 import pieces.Bishop;
 import pieces.King;
 import pieces.Knight;
 import pieces.Pawn;
 import pieces.Piece;
 import pieces.Queen;
 import pieces.Rook;
 
 public class Board {
 
 	private final static int BOARD_SIZE = 8;
 
 	private Piece[][] pieces;
 
 	public Board() {
 		pieces = new Piece[BOARD_SIZE][BOARD_SIZE];
 		placePieces(PieceColor.WHITE);
 		placePieces(PieceColor.BLACK);
 		
 	}
 
 	private void placePieces(PieceColor pieceColor) {
 		
 		int mainRow = pieceColor == PieceColor.WHITE ? 1 : 8;
 		int pawnRow = pieceColor == PieceColor.WHITE ? 2 : 7;
 		
 		setPiece("a" + mainRow, new Rook(pieceColor));
 		setPiece("b" + mainRow, new Knight(pieceColor));
 		setPiece("c" + mainRow, new Bishop(pieceColor));
 		setPiece("d" + mainRow, new Queen(pieceColor));
 		setPiece("e" + mainRow, new King(pieceColor));
 		setPiece("f" + mainRow, new Bishop(pieceColor));
 		setPiece("g" + mainRow, new Knight(pieceColor));
 		setPiece("h" + mainRow, new Rook(pieceColor));
 		
 		setPiece("a" + pawnRow, new Pawn(pieceColor));
 		setPiece("b" + pawnRow, new Pawn(pieceColor));
 		setPiece("c" + pawnRow, new Pawn(pieceColor));
 		setPiece("d" + pawnRow, new Pawn(pieceColor));
 		setPiece("e" + pawnRow, new Pawn(pieceColor));
 		setPiece("f" + pawnRow, new Pawn(pieceColor));
 		setPiece("g" + pawnRow, new Pawn(pieceColor));
 		setPiece("h" + pawnRow, new Pawn(pieceColor));
 		
 	}
 
 	public Piece getPiece(String position) {
 		return getPiece(getColumnIndex(position), getRowIndex(position));
 	}
 
 	private Piece getPiece(int column, int row) {
 		return pieces[column][row];
 	}
 
 	public void setPiece(String position, Piece piece) {
 		pieces[getColumnIndex(position)][getRowIndex(position)] = piece;
 	}
 
 	public static int getColumnIndex(String position) {
 		System.out.println("stringpos: "+position);
 		System.out.println(position.charAt(0) - 'a');
 		return position.charAt(0) - 'a';
 	}
 
 	public static int getRowIndex(String position) {
 		return position.charAt(1) - '1';
 	}
 
 	public static boolean isStraight(String from, String to) {
 		return getColumnDistance(from, to) == 0
 				|| getRowDistance(from, to) == 0;
 	}
 
 	public static boolean isDiagonal(String from, String to) {
		return Math.abs(getColumnDistance(from, to)) == Math.abs(getRowDistance(from, to));
 	}
 
 	public boolean isOccupiedBetween(String from, String to) {
 		int fromColumn = getColumnIndex(from);
 		int fromRow = getRowIndex(from);
 		int toColumn = getColumnIndex(to);
 		int toRow = getRowIndex(to);
 		int dColumn = (fromColumn == toColumn ? 0 : (toColumn - fromColumn)
 				/ Math.abs(toColumn - fromColumn));
 		int dRow = (fromRow == toRow ? 0 : (toRow - fromRow)
 				/ Math.abs(toRow - fromRow));
 		for (int column = fromColumn + dColumn, row = fromRow + dRow; column != toColumn
 				|| row != toRow; column += dColumn, row += dRow) {
 			if (getPiece(column, row) != null) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public static int getRowDistance(String from, String to) {
 		return getRowIndex(to) - getRowIndex(from);
 	}
 
 	public static int getColumnDistance(String from, String to) {
 		return getColumnIndex(to) - getColumnIndex(from);
 	}
 
 	public static void validateBoardPosition(String position) {
 
 		int column = getColumnIndex(position);
 		int row = getRowIndex(position);
 
 		if (column < 0 || column >= BOARD_SIZE || row < 0 || row >= BOARD_SIZE)
 			throw new IllegalBoardPosition(position);
 	}
 
 	public boolean isLegalMove(PieceColor pieceColor, String from, String to) {
 		try {
 			validateBoardPosition(from);
 			validateBoardPosition(to);
 		} catch (IllegalBoardPosition e) {
 			return false;
 		}
 
 		Piece myPiece = getPiece(from);
 		Piece toPiece = getPiece(to);
 		if (myPiece == null || myPiece.getPieceColor() != pieceColor)
 			return false;
 
 		return (toPiece != null && toPiece.getPieceColor() != pieceColor) ? myPiece
 				.canTake(from, to, this) : toPiece == null
 				&& myPiece.canMove(from, to, this);
 	}
 
 	public boolean movePiece(PieceColor pieceColor, String from, String to) {
 		Piece piece = getPiece(from);
 		if (isLegalMove(pieceColor, from, to)) {
 			setPiece(from, null);
 			setPiece(to, piece);
 			return true;
 		} else {
 			System.out.println("Illegal move");
 			return false;
 		}
 	}
 
 	@Override
 	public String toString() {
 
 		StringBuilder board = new StringBuilder();
 
 		for (int y = BOARD_SIZE - 1; y >= 0; y--) {
 			board.append("+-------------------------------+\n");
 			board.append("| ");
 			for (int x = 0; x < BOARD_SIZE; x++) {
 				board.append(getPiece(x, y) == null ? " " : getPiece(x, y));
 				board.append(" | ");
 			}
 			board.append("\n");
 		}
 		board.append("+-------------------------------+\n");
 
 		return board.toString();
 	}
 
 
 	public boolean isCheck(PieceColor kingColor) {
 		String kingPosition = findKing(kingColor);
 		
 		BoardIterator bi = new BoardIterator();
 
 		PieceColor otherColor = kingColor.getOtherColor();
 		while (bi.hasNext()) {
 			String position = bi.next();
 			Piece piece = getPiece(position);
 			if (piece != null && piece.getPieceColor() == otherColor && piece.canTake(position, kingPosition, this)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public String findKing(PieceColor kingColor) {
 		BoardIterator bi = new BoardIterator();
 		
 		while(bi.hasNext()){
 
 			String position = bi.next();
 			//TODO bi.next() gir nullverdi -> getPiece(position) gir nullpointerException
 			Piece piece= getPiece(position);
 
 			if (piece instanceof King && piece.getPieceColor() == kingColor) {
 				return position;
 			}
 		}
 		return null;
 	}
 
 }
