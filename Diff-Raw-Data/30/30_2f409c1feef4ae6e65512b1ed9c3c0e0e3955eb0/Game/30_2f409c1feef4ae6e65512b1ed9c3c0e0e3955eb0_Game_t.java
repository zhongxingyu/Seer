 package com.petrifiednightmares.singularityChess.logic;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.Set;
 
 import org.acra.ACRA;
 
 import android.graphics.Canvas;
 
 import com.petrifiednightmares.singularityChess.GameDrawingPanel;
 import com.petrifiednightmares.singularityChess.GameException;
 import com.petrifiednightmares.singularityChess.InvalidMoveException;
 import com.petrifiednightmares.singularityChess.io.GameIO;
 import com.petrifiednightmares.singularityChess.io.GameSaveable;
 import com.petrifiednightmares.singularityChess.logic.player.AIPlayer;
 import com.petrifiednightmares.singularityChess.logic.player.NonTrackedPlayer;
 import com.petrifiednightmares.singularityChess.logic.player.Player;
 import com.petrifiednightmares.singularityChess.logic.player.TrackedPlayer;
 import com.petrifiednightmares.singularityChess.pieces.AbstractPiece;
 import com.petrifiednightmares.singularityChess.pieces.Bishop;
 import com.petrifiednightmares.singularityChess.pieces.King;
 import com.petrifiednightmares.singularityChess.pieces.Knight;
 import com.petrifiednightmares.singularityChess.pieces.Pawn;
 import com.petrifiednightmares.singularityChess.pieces.Queen;
 import com.petrifiednightmares.singularityChess.pieces.Rook;
 import com.petrifiednightmares.singularityChess.ui.GameDrawable;
 import com.petrifiednightmares.singularityChess.ui.GameUI;
 
 public class Game extends GameDrawable
 {
 
 	AbstractPiece[]	whitePieces;
 	AbstractPiece[]	blackPieces;
 	private boolean	isWhiteTurn;
 
 	Board			_board;
 
 	AbstractPiece	selectedPiece, checkingPiece;
 	Set<Square>		selectedPieceMoves;
 
 	private GameUI	_gui;
 
 	private Player	_whitePlayer, _blackPlayer;
 	private Player	_currentPlayer, _notCurrentPlayer;
 	private Player	_controllingPlayer, _nonControllingPlayer;
 
 	public static final int	VSHUMAN	= 1, VSCOMP = 2, ONLINE = 3;
 
 	private int				_gameType;
 
 	public Game(GameDrawingPanel drawingPanel)
 	{
 		super(drawingPanel);
 
 		whitePieces = new AbstractPiece[16];
 		blackPieces = new AbstractPiece[16];
 
 	}
 
 	// ================================================================================
 	// Initialization
 	// ================================================================================
 
 	public void initialize(Board board, GameUI gui, int gtype)
 	{
 		this._gui = gui;
 
 		this._gameType = gtype;
 		initializePlayers(gtype);
 
 		isWhiteTurn = true;
 		gui.setTurnName(_whitePlayer.getName(), isWhiteTurn);
 
 		this._board = board;
 		initializePieces(whitePieces, true);
 		initializePieces(blackPieces, false);
 	}
 
 	public void resume(Board board, GameUI gui)
 	{
 		InputStream in = null;
 		try
 		{
 			this._gui = gui;
 			this._board = board;
 
 			GameSaveable gs = new GameSaveable(this, gui);
 
 			GameIO.intentionSaveGame();
 			in = GameIO.getInputStream();
 
 			gs.deserialize(in);
 
 			this._gameType = gs.getGameType();
 
 			whitePieces = gs.getWhitePieces();
 			blackPieces = gs.getBlackPieces();
 
 			isWhiteTurn = gs.isWhiteTurn();
 
 			_whitePlayer = gs.getWhitePlayer();
 			_blackPlayer = gs.getBlackPlayer();
 
 			_currentPlayer = isWhiteTurn ? _whitePlayer : _blackPlayer;
 			_notCurrentPlayer = isWhiteTurn ? _blackPlayer : _whitePlayer;
 
 			boolean isControllingWhite = gs.getIsControllingWhite();
 			// TODO fix
 			_controllingPlayer = isControllingWhite ? _whitePlayer : _blackPlayer;
 			_nonControllingPlayer = isControllingWhite ? _blackPlayer : _whitePlayer;
 
 			this._gui.setMoveLogger(gs.getMoveLogger());
 			this._gui.setTurnName(_currentPlayer.getName(), isWhiteTurn());
 		}
 		catch (Exception e)
 		{
 			// for some reason resume didnt work, initializing instead
 			this._gameType = VSHUMAN;
 			initialize(board, gui, _gameType);
 			gdp.displayMessage("Resume failed, starting new game against human");
 
 			ACRA.getErrorReporter().handleSilentException(e);
 			e.printStackTrace();
 		}
 		finally
 		{
 			GameIO.closeSilently(in);
 		}
 	}
 
 	private void initializePlayers(int gt)
 	{
 		switch (gt)
 		{
 			case ONLINE:
 				// use TrackedPlayer and Remote Player
 				// TODO
 				break;
 			case VSCOMP:
 				// TODO in the future we need to put logic that allows player to
 				// choose which side he/she wants to play as
 				_whitePlayer = new TrackedPlayer(true, "White player", gdp, this, _gui);
 				_blackPlayer = new AIPlayer(false, "Black player", gdp, this, _gui);
 				_controllingPlayer = _whitePlayer;
 				_nonControllingPlayer = _blackPlayer;
 				break;
 			case VSHUMAN:
 				_whitePlayer = new NonTrackedPlayer(true, "White player", gdp, this, _gui);
 				_blackPlayer = new NonTrackedPlayer(false, "Black player", gdp, this, _gui);
 				_controllingPlayer = _whitePlayer;
 				_nonControllingPlayer = _blackPlayer;
 				break;
 			default:
 				break;
 
 		}
 		_currentPlayer = _whitePlayer;
 		_notCurrentPlayer = _blackPlayer;
 	}
 
 	private void initializePieces(AbstractPiece[] piecesArray, boolean isWhite)
 	{
 		// 8 pawns
 		System.arraycopy(Pawn.makePawns(this, isWhite), 0, piecesArray, 0, 8);
 
 		// 2 rooks
 		System.arraycopy(Rook.makeRooks(this, isWhite), 0, piecesArray, 8, 2);
 
 		// 2 bishops
 		System.arraycopy(Bishop.makeBishops(this, isWhite), 0, piecesArray, 10, 2);
 
 		// 2 knights
 		System.arraycopy(Knight.makeKnights(this, isWhite), 0, piecesArray, 12, 2);
 
 		// king
 		System.arraycopy(King.makeKings(this, isWhite), 0, piecesArray, 14, 1);
 
 		// queen
 		System.arraycopy(Queen.makeQueens(this, isWhite), 0, piecesArray, 15, 1);
 
 	}
 
 	// ================================================================================
 	// Moves logic
 	// ================================================================================
 
 	public Set<Square> selectAndGetMoves(Square s) throws GameException
 	{
 		AbstractPiece p = s.getPiece();
 		if (p == null)
 			return null;
 
 		// if piece belongs to current player
 		if (p.isWhite() == isWhiteTurn())
 		{
 			// select the piece
 			selectedPiece = p;
 			p.select();
 			// remember the selected piece's moves
 			return selectedPieceMoves = selectedPiece.getMoves();
 		}
 		else
 		{
 			// otherwise unselect all pieces.
 			if (selectedPiece != null)
 				selectedPiece.unselect();
 			selectedPiece = null;
 			selectedPieceMoves = null;
 
 			// but return the moves anyway.
 			return selectedPiece.getMoves();
 		}
 	}
 
 	public boolean canMakeMove(Square target)
 	{
 		if (selectedPieceMoves != null && !selectedPieceMoves.contains(target)
 				&& target.isHighlighted())
 		{
 			selectedPieceMoves.add(target);
 		}
 
 		return selectedPiece != null && selectedPieceMoves != null
 				&& (selectedPieceMoves.contains(target))
 				&& selectedPiece.isWhite() == isWhiteTurn()
 				&& (!target.hasPiece() || target.getPiece().isCapturable());
 	}
 
 	public boolean makeMove(Square target) throws InvalidMoveException
 	{
 		if (_gui.PROMPT_WAITING)
 			return false;
 
 		if (selectedPiece != null && selectedPieceMoves.contains(target))
 		{
 			Square sourceLocation = selectedPiece.getLocation();
 
 			AbstractPiece capturedPiece = selectedPiece.makeMove(target);
 
 			// check if king is in check
 			if (!checkMoveValidity())
 			{
 				unmakeMove(capturedPiece, selectedPiece, target, sourceLocation);
 				if (checkingPiece != null)
 					select(checkingPiece);
 				return false;
 			}
 			// log to movelogger
 			if (capturedPiece == null)
 			{
 				_gui.recordMove(selectedPiece, sourceLocation, target);
 			}
 			else
 			{
 				_gui.recordMove(selectedPiece, sourceLocation, target, capturedPiece);
 			}
 
 			if (checkPostMoveConditions())
 			{
 				finishMove();
 			}
 		}
 		else
 		{
 			throw new InvalidMoveException(
 					"Invalid Move: Either piece not selected or illegal move. Piece: "
 							+ selectedPiece + " Target: " + target);
 		}
 		return true;
 	}
 
 	private void finishMove()
 	{
 		// have to check again.
 		if (!checkWinCondition())
 		{
 			if (!_gui.PROMPT_WAITING)
 			{
 				_gui.playPieceSounds();
 				switchTurns();
 				unselect();
 
 				saveGame();
 			}
 		}
 	}
 
 	private void unmakeMove(AbstractPiece capturedPiece, AbstractPiece actor,
 			Square destinationLocation, Square sourceLocation)
 	{
 		destinationLocation.removePiece();
 		if (capturedPiece != null)
 			capturedPiece.revive(destinationLocation);
 		actor.setLocation(sourceLocation);
 
 	}
 
 	private boolean checkMoveValidity()
 	{
 		// Make sure king not in check
 		AbstractPiece[] enemyPieces = isWhiteTurn() ? blackPieces : whitePieces;
 		for (AbstractPiece p : enemyPieces)
 		{
 			if (p.isAlive())
 			{
 				if (p.checkingKing())
 				{
 					gdp.displayMessage(p + " on square " + p.getLocation() + " is checking king");
 					checkingPiece = p;
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	private boolean checkPostMoveConditions()
 	{
 		// check to see if theres a check, a checkmate, or a pawn can get
 		// promoted.
 
 		if (selectedPiece instanceof Pawn)
 		{
 			if (Board.isEndOfFile(selectedPiece.getLocation()))
 			{
 				// can be promoted
 				_currentPlayer.promote(selectedPiece);
 
 				return false;
 			}
 		}
 		return true;
 	}
 
 	private void switchTurns()
 	{
 		isWhiteTurn = !isWhiteTurn;
 		_currentPlayer = isWhiteTurn ? _whitePlayer : _blackPlayer;
 		_notCurrentPlayer = isWhiteTurn ? _blackPlayer : _whitePlayer;
 		_gui.setTurnName(_currentPlayer.getName(), isWhiteTurn());
 
 		if (_gameType == VSHUMAN)
 		{
 			_controllingPlayer = _currentPlayer;
 			_nonControllingPlayer = _notCurrentPlayer;
 		}
 		_currentPlayer.doTurn();
 	}
 
 	// ================================================================================
 	// Promotions
 	// ================================================================================
 
 	public void promotePiece(AbstractPiece.PieceType pieceType)
 	{
 		if (selectedPiece instanceof Pawn)
 		{
 			AbstractPiece newPiece = ((Pawn) (selectedPiece)).promote(pieceType);
 			replacePiece(selectedPiece, newPiece);
 			finishMove();
 		}
 		else
 		{
 			gdp.displayMessage(selectedPiece + " at location: " + selectedPiece.getLocation()
 					+ " is not a pawn yet was attempted to be promoted");
 		}
 	}
 
 	public void onDraw(Canvas canvas)
 	{
 	}
 
 	// ================================================================================
 	// Ending Game
 	// ================================================================================
 
 	public void surrender()
 	{
 		_controllingPlayer.loseGame();
 		_nonControllingPlayer.winGame();
 	}
 
 	public void endGame()
 	{
 		try
 		{
 			GameIO.intentionSaveGame();
 			GameIO.removeFile();
 		}
 		finally
 		{
 			gdp.gameActivity.finish();
 		}
 	}
 
 	// ================================================================================
 	// Clicks and Selects
 	// ================================================================================
 
 	public boolean onClick(int x, int y)
 	{
 		return false;
 	}
 
 	public void select(AbstractPiece piece)
 	{
 		if (!_gui.PROMPT_WAITING)
 		{
 			try
 			{
 				_board.unhighlightAllSquares();
 				selectedPiece = piece;
 				selectedPieceMoves = piece.getMoves();
 				_board.highlightMoves(selectedPieceMoves);
 				_board.select(piece.getLocation());
 				checkingPiece = null;
 			}
 			catch (GameException e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void unselect()
 	{
 		if (!_gui.PROMPT_WAITING)
 		{
 			_board.unhighlightAllSquares();
 			selectedPiece = null;
 			selectedPieceMoves = null;
 		}
 	}
 
 	private void saveGame()
 	{
 		GameSaveable gs;
 		OutputStream out = null;
 		try
 		{
 			GameIO.intentionSaveGame();
 
 			gs = new GameSaveable(_gameType, isWhiteTurn, whitePieces, blackPieces,
 					_gui.getMoveLogger(), _whitePlayer, _blackPlayer,
 					_controllingPlayer.equals(_whitePlayer));
 
 			out = GameIO.getOutputStream();
 			gs.serialize(out);
 		}
 		catch (IOException e)
 		{
 			e.printStackTrace();
 			gdp.displayMessage(e.getMessage());
 		}
 		finally
 		{
 			GameIO.closeSilently(out);
 		}
 	}
 
 	public Player getCurrentPlayer()
 	{
 		return this._currentPlayer;
 	}
 
 	// ================================================================================
 	// Getters
 	// ================================================================================
 	public boolean isWhiteTurn()
 	{
 		return isWhiteTurn;
 	}
 
 	private boolean isChecked()
 	{
 		AbstractPiece[] enemyPieces = isWhiteTurn() ? whitePieces : blackPieces;
 		for (AbstractPiece p : enemyPieces)
 		{
 			if (p.isAlive())
 			{
 				if (p.checkingKing())
 				{
 					return true;
 				}
 			}
 		}
 
 		return false;
 	}
 
 	public Board getBoard()
 	{
 		return _board;
 	}
 
 	public GameDrawingPanel getDrawingPanel()
 	{
 		return gdp;
 	}
 
 	public boolean isTurn()
 	{
 		if (selectedPiece != null)
 			return isWhiteTurn() == selectedPiece.isWhite();
 		else
 			return false;
 	}
 
 	public boolean isControllingPlayerTurn()
 	{
 		return _controllingPlayer.equals(_currentPlayer);
 	}
 
 	public AbstractPiece[] getWhitePiece()
 	{
 		return this.whitePieces;
 	}
 
 	public AbstractPiece[] getBlackPiece()
 	{
 		return this.blackPieces;
 	}
 
 	// ================================================================================
 	// Private Helpers
 	// ================================================================================
 
 	// replace the piece in our array so the old one gets thrown away.
 	private void replacePiece(AbstractPiece oldPiece, AbstractPiece newPiece)
 	{
 		if (newPiece.isWhite())
 		{
 			int index = findPiece(selectedPiece);
 			if (index != -1)
 				whitePieces[index] = newPiece;
 		}
 		else
 		{
 			int index = findPiece(selectedPiece);
 			if (index != -1)
 				blackPieces[index] = newPiece;
 		}
 	}
 
 	private int findPiece(AbstractPiece p)
 	{
 		if (p.isWhite())
 		{
 			for (int i = 0; i < whitePieces.length; i++)
 			{
 				if (whitePieces[i].equals(p))
 				{
 					return i;
 				}
 			}
 		}
 		else
 		{
 			for (int i = 0; i < blackPieces.length; i++)
 			{
 				if (blackPieces[i].equals(p))
 				{
 					return i;
 				}
 			}
 		}
 		return -1;
 	}
 
 	private boolean checkWinCondition()
 	{
 		// check if the king of this turn is checked. if not return false;
 		if (this.isChecked())
 		{
 			// iterates all moves to see if any can save the king's ass.
 			AbstractPiece[] pieces = isWhiteTurn() ? blackPieces : whitePieces;
 			for (AbstractPiece p : pieces)
 			{
 				if (p.isAlive())
 				{
 					try
 					{
 						Set<Square> moves = p.getMoves();
 						for (Square target : moves)
 						{
 							Square sourceLocation = p.getLocation();
 							AbstractPiece capturedPiece = p.makeMove(target);
 							if (!this.isChecked())
 							{
 								// someone's ass got saved
 								unmakeMove(capturedPiece, p, target, sourceLocation);
 								return false;
 							}
 
 							unmakeMove(capturedPiece, p, target, sourceLocation);
 						}
 					}
 					catch (GameException e)
 					{
 						e.printStackTrace();
 					}
 				}
 			}
 
 			// try all the pieces and moves, king cannot be saved
 			// The king shall fall, call winGame() or loseGame()						
 			_currentPlayer.winGame();
 			_notCurrentPlayer.loseGame();
 
 			return true;
 		}
 		else
 		{
 			// iterates all moves to see if any can save the king's ass.
 			AbstractPiece[] pieces = isWhiteTurn() ? blackPieces : whitePieces;
 			for (AbstractPiece p : pieces)
 			{
 				if (p.isAlive())
 				{
 					try
 					{
 						Set<Square> moves = p.getMoves();
 						for (Square target : moves)
 						{
 							Square sourceLocation = p.getLocation();
 							AbstractPiece capturedPiece = p.makeMove(target);
 							if (!this.isChecked())
 							{
 								// someone's ass got saved
 								unmakeMove(capturedPiece, p, target, sourceLocation);
 								return false;								
 							}
 							unmakeMove(capturedPiece, p, target, sourceLocation);
 						}
 					}
 					catch (GameException e)
 					{
 						e.printStackTrace();
 					}
 				}
 			}
 
 			// try all the pieces and moves, no legal move can be made
 			// Stalemate!
 			this.gdp.showFinishPrompt("Stalemate!", 
 					"This is stale meat. Should have put it in the fridge ;) Let's Play again!");
 			
 		}
 
 		return false;
 
 	}
 }
