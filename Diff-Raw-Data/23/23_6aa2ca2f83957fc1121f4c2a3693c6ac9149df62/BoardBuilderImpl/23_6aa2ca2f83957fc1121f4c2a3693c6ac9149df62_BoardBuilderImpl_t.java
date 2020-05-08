 package javalabra.chess.core.impl;
 
 import java.util.Collection;
 
 import javalabra.chess.core.BoardBuilder;
 import javalabra.chess.domain.Bishop;
 import javalabra.chess.domain.Board;
 import javalabra.chess.domain.Color;
 import javalabra.chess.domain.King;
 import javalabra.chess.domain.Knight;
 import javalabra.chess.domain.Pawn;
 import javalabra.chess.domain.Piece;
 import javalabra.chess.domain.PlayerBlack;
 import javalabra.chess.domain.PlayerWhite;
 import javalabra.chess.domain.Queen;
 import javalabra.chess.domain.Rook;
 
 /**
  * Standard implementation of chessboard builder.
  *
  * @author Nikita Frolov
  */
 public class BoardBuilderImpl implements BoardBuilder {
 
 	/**
 	 * Builds standard chess layout.
 	 *
 	 * {@inheritDoc}
 	 */
 	@Override
 	public Board build(PlayerWhite white, PlayerBlack black) {
 		final Board board = new Board();
 
 		build(board, white.getPieces(), black.getPieces());
 
 		return board;
 	}
 
 	private void build(Board board, Collection<Piece> whites, Collection<Piece> blacks) {
 		assert whites.isEmpty() && blacks.isEmpty();
 
 		createPawnRow(Color.WHITE, board, whites);
 		createMainRow(Color.WHITE, board, whites);
 
 		createPawnRow(Color.BLACK, board, blacks);
 		createMainRow(Color.BLACK, board, blacks);
 
 		assert 16 == whites.size() && 16 == blacks.size();
 	}
 
 	private void createMainRow(Color color, Board board, Collection<Piece> pieces) {
 		final int row = Color.WHITE == color ? 0 : 7;
 
 		addPiece(pieces, new Rook(color), board, 0, row);
 		addPiece(pieces, new Knight(color), board, 1, row);
 		addPiece(pieces, new Bishop(color), board, 2, row);
 
		addPiece(pieces, new Queen(color), board, 3, row);
		addPiece(pieces, new King(color), board, 4, row);
 
 		addPiece(pieces, new Bishop(color), board, 5, row);
 		addPiece(pieces, new Knight(color), board, 6, row);
 		addPiece(pieces, new Rook(color), board, 7, row);
 	}
 
 	private void createPawnRow(Color color, Board board, Collection<Piece> pieces) {
 		final int row = Color.WHITE == color ? 1 : 6;
 		for (int col = 0; col < 8; ++col) {
 			addPiece(pieces, new Pawn(color), board, col, row);
 		}
 	}
 
 	private void addPiece(final Collection<Piece> pieces, final Piece piece, final Board board, final int col, final int row) {
 		board.setPiecePosition(piece, board.getSquare(col, row));
 		pieces.add(piece);
 	}
 
 }
