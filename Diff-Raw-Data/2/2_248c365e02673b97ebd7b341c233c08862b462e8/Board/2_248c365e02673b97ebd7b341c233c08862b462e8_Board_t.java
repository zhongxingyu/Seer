 package edu.selu.android.classygames.games.checkers;
 
 
 import org.json.JSONException;
 import org.json.JSONObject;
 
 import edu.selu.android.classygames.games.Coordinate;
 import edu.selu.android.classygames.games.GenericBoard;
 import edu.selu.android.classygames.games.Position;
 
 
 /**
  * Class representing a Checkers board. This board is made up of a bunch of
  * positions. Checkers is 8 by 8, so that's 64 positions.
  */
 public class Board extends GenericBoard
 {
 
 
 	private final static byte LENGTH_HORIZONTAL = 8;
 	private final static byte LENGTH_VERTICAL = 8;
 	private final static byte MAX_TEAM_SIZE = 12;
 
 
 
 
 	/**
 	 * Holds a handle to the piece that the user last moved. This isn eeded for
 	 * multijump purposes.
 	 */
 	private Piece lastMovedPiece;
 
 
 
 
 	/**
 	 * Creates a Checkers board object.
 	 * 
 	 * @throws JSONException
 	 * If a glitch or something happened while trying to create some JSON data
 	 * then this JSONException will be thrown. When using this particular
 	 * constructor this should never happen.
 	 */
 	public Board() throws JSONException
 	{
 		super(LENGTH_HORIZONTAL, LENGTH_VERTICAL);
 	}
 
 
 	/**
 	 * Creates a Checkers board object using the given JSON String.
 	 * 
 	 * @param boardJSON
 	 * JSONObject that represents the board.
 	 * 
 	 * @throws JSONException
 	 * If a glitch or something happened while trying to create some JSON data
 	 * then this JSONException will be thrown.
 	 */
 	public Board(final JSONObject boardJSON) throws JSONException
 	{
 		super(LENGTH_HORIZONTAL, LENGTH_VERTICAL, boardJSON);
 	}
 
 
 
 
 	@Override
 	protected Piece buildPiece(final byte whichTeam, final int type)
 	{
 		return new Piece(whichTeam, type);
 	}
 
 
 	@Override
 	public byte checkValidity()
 	{
 		byte piecesCountOpponent = 0;
 		byte piecesCountPlayer = 0;
 
 		for (byte x = 0; x < lengthHorizontal; ++x)
 		{
 			for (byte y = 0; y < lengthVertical; ++y)
 			{
 				final Coordinate coordinate = new Coordinate(x, y);
 
 				final Position position = getPosition(coordinate);
 				final Piece piece = (Piece) position.getPiece();
 
 				if (piece != null && coordinate.areBothEitherEvenOrOdd())
 				// check to see if this piece is in an invalid position on the
 				// board
 				{
 					return BOARD_INVALID;
 				}
 
 				if (piece != null)
 				// count the size of the teams
 				{
 					if (piece.isTeamOpponent())
 					{
 						++piecesCountOpponent;
 					}
 					else if (piece.isTeamPlayer())
 					{
 						++piecesCountPlayer;
 					}
 				}
 
 				if (!coordinate.areBothEitherEvenOrOdd())
 				{
 					if (y > 4)
 					{
 						if (piece == null)
 						{
 							return BOARD_INVALID;
 						}
 						else if (piece.isTeamPlayer())
 						{
 							return BOARD_INVALID;
 						}
 						else if (piece.isTypeKing())
 						{
 							return BOARD_INVALID;
 						}
 					}
 					else if (y == 2)
 					{
 						if (piece != null)
 						{
 							if (piece.isTeamOpponent())
 							{
 								return BOARD_INVALID;
 							}
 							else if (piece.isTypeKing())
 							{
 								return BOARD_INVALID;
 							}
 						}
 					}
 					else if (y < 2)
 					{
 						if (piece == null)
 						{
 							return BOARD_INVALID;
 						}
 						else if (piece.isTeamOpponent())
 						{
 							return BOARD_INVALID;
 						}
 						else if (piece.isTypeKing())
 						{
 							return BOARD_INVALID;
 						}
 					}
 				}
 
 				if (y == 7 && x % 2 == 0 || y == 6 && x % 2 != 0 || y == 5 && x % 2 == 0 || y == 1 && x % 2 == 0 || y == 0 && x % 2 != 0)
 				// only the first row should have a piece moved from a valid
 				// spot at first
 				{
 					if (piece == null)
 					{
 						return BOARD_INVALID;
 					}
 				}
 
 				if (y == 3)
 				// make sure that the first move came from a valid place
 				{
 					if (piece != null)
 					{
 						if (x == 0)
 						// check that farthest left piece moved to this position
 						{
 							if (getPosition(x + 1, y - 1).hasPiece())
 							{
 								return BOARD_INVALID;
 							}
 						}
 						else if (x % 2 == 0)
 						// check the rest of the pieces for a valid first turn
 						// move
 						{
 							if (getPosition(x - 1, y - 1).hasPiece() && getPosition(x + 1, y - 1).hasPiece())
 							{
 								return BOARD_INVALID;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		if (piecesCountOpponent != MAX_TEAM_SIZE || piecesCountPlayer != MAX_TEAM_SIZE)
 		{
 			return BOARD_INVALID;
 		}
 
 		return BOARD_NEW_GAME;
 	}
 
 
 	@Override
 	public byte checkValidity(final JSONObject boardJSON)
 	{
 		try
 		{
 			final Board board = new Board(boardJSON);
 			byte piecesCountOpponent = 0;
 			byte piecesCountPlayer = 0;
 
 			for (byte x = 0; x < lengthHorizontal; ++x)
 			{
 				for (byte y = 0; y < lengthVertical; ++y)
 				{
 					final Coordinate coordinate = new Coordinate(x, y);
 
 					final Position position = getPosition(coordinate);
 					final Position positionNew = board.getPosition(coordinate);
 					final Piece pieceNew = (Piece) positionNew.getPiece();
 
 					if (coordinate.areBothEitherEvenOrOdd())
 					// check to see if this piece is in an invalid position on
 					// the board
 					{
 						if (pieceNew != null)
 						{
 							return BOARD_INVALID;
 						}
 					}
 
 					if (pieceNew != null)
 					// count the size of the teams
 					{
 						if (pieceNew.isTeamOpponent())
 						{
 							++piecesCountOpponent;
 						}
 						else if (pieceNew.isTeamPlayer())
 						{
 							++piecesCountPlayer;
 						}
 					}
 
 					if (piecesCountOpponent > MAX_TEAM_SIZE || piecesCountPlayer > MAX_TEAM_SIZE)
 					{
 						return BOARD_INVALID;
 					}
 
 					if (piecesCountOpponent == 0)
 					{
 						return BOARD_WIN;
 					}
 				}
 			}
 		}
 		catch (final JSONException e)
 		{
 			return BOARD_INVALID;
 		}
 
 		return BOARD_NEW_MOVE;
 	}
 
 
 	@Override
 	protected void initializeDefaultBoard()
 	{
 		// player team
 		getPosition(1, 0).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(3, 0).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(5, 0).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(7, 0).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(0, 1).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(2, 1).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(4, 1).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(6, 1).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(1, 2).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(3, 2).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(5, 2).setPiece(new Piece(Piece.TEAM_PLAYER));
 		getPosition(7, 2).setPiece(new Piece(Piece.TEAM_PLAYER));
 
 		// opponent team
 		getPosition(0, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(2, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(4, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(6, 7).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(1, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(3, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(5, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(7, 6).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(0, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(2, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(4, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 		getPosition(6, 5).setPiece(new Piece(Piece.TEAM_OPPONENT));
 	}
 
 
 
 
 	@Override
 	public boolean move(final Position previous, final Position current)
 	{
 		boolean isMoveValid = false;
 
 		if (isBoardLocked)
 		{
 			isMoveValid = false;
 		}
 		else if (previous.hasPiece() && current.hasPiece())
 		{
 			isMoveValid = false;
 		}
 		else if ((previous.hasPiece() && previous.getPiece().isTeamOpponent()) || (current.hasPiece() && current.getPiece().isTeamPlayer()))
 		{
 			isMoveValid = false;
 		}
 		else if (previous.hasPiece())
 		{
 			final Piece piece = (Piece) previous.getPiece();
 
 			if ((previous.getCoordinate().getX() == current.getCoordinate().getX() - 1)
 				|| (previous.getCoordinate().getX() == current.getCoordinate().getX() + 1))
 			{
 				switch (piece.getType())
 				{
 					case Piece.TYPE_KING:
 						if (previous.getCoordinate().getY() == current.getCoordinate().getY() + 1)
 						{
 							isMoveValid = true;
 							isBoardLocked = true;
 						}
 
 					case Piece.TYPE_NORMAL:
 						if (previous.getCoordinate().getY() == current.getCoordinate().getY() - 1)
 						{
 							isMoveValid = true;
 							isBoardLocked = true;
 						}
 				}
 			}
 			else if ((previous.getCoordinate().getX() == current.getCoordinate().getX() - 2)
 				|| (previous.getCoordinate().getX() == current.getCoordinate().getX() + 2))
 			{
 				boolean isJumpValid = false;
 
 				switch (piece.getType())
 				{
 					case Piece.TYPE_KING:
 						if (previous.getCoordinate().getY() == current.getCoordinate().getY() + 2)
 						{
 							isJumpValid = true;
 						}
 						break;
 
 					case Piece.TYPE_NORMAL:
 						if (previous.getCoordinate().getY() == current.getCoordinate().getY() - 2)
 						{
 							isJumpValid = true;
 						}
 						break;
 				}
 
 				if (isJumpValid)
 				{
 					final byte middleX = (byte) Math.abs(previous.getCoordinate().getX() - current.getCoordinate().getX());
 					final byte middleY = (byte) Math.abs(previous.getCoordinate().getY() - current.getCoordinate().getY());
 					final Coordinate middleCoordinate = new Coordinate(middleX, middleY);
 					final Position middlePosition = getPosition(middleCoordinate);
 
 					if (middlePosition.hasPiece() && middlePosition.getPiece().isTeamOpponent())
 					{
 						if (lastMovedPiece != null && lastMovedPiece == piece)
 						{
 							middlePosition.getPiece().kill();
 							isMoveValid = true;
 						}
 					}
 				}
 			}
 
 			if (isMoveValid)
 			{
 				current.setPiece(new Piece(piece));
 				previous.removePiece();
 				lastMovedPiece = (Piece) current.getPiece();
 
 				if (current.getCoordinate().getY() == lengthVertical - 1)
 				{
 					((Piece) current.getPiece()).ascendToKing();
 				}
 			}
 		}
 		else if (current.hasPiece())
 		{
 			isMoveValid = false;
 		}
 		else
 		{
 			isMoveValid = false;
 		}
 
 		return isMoveValid;
 	}
 
 
 	@Override
 	protected void resetBoard()
 	{
 		lastMovedPiece = null;
 	}
 
 
 }
