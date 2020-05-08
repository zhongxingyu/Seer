 package com.petrifiednightmares.singularityChess.logic;
 
 import java.util.Set;
 
 import android.graphics.Canvas;
 
 import com.petrifiednightmares.singularityChess.GameDrawingPanel;
 import com.petrifiednightmares.singularityChess.GameException;
 import com.petrifiednightmares.singularityChess.InvalidMoveException;
 import com.petrifiednightmares.singularityChess.logging.MoveLogger;
 import com.petrifiednightmares.singularityChess.pieces.AbstractPiece;
 import com.petrifiednightmares.singularityChess.pieces.Bishop;
 import com.petrifiednightmares.singularityChess.pieces.King;
 import com.petrifiednightmares.singularityChess.pieces.Knight;
 import com.petrifiednightmares.singularityChess.pieces.Pawn;
 import com.petrifiednightmares.singularityChess.pieces.Queen;
 import com.petrifiednightmares.singularityChess.pieces.Rook;
 import com.petrifiednightmares.singularityChess.ui.BottomBar;
 import com.petrifiednightmares.singularityChess.ui.Preferences;
 import com.petrifiednightmares.singularityChess.ui.SUI;
 import com.petrifiednightmares.singularityChess.ui.TopBar;
 
 public class Game
 {
 	GameDrawingPanel drawingPanel;
 	Board board;
 
 	AbstractPiece[] whitePieces;
 	AbstractPiece[] blackPieces;
 
 	boolean isWhiteTurn;
 
	AbstractPiece selectedPiece;
 	Set<Square> selectedPieceMoves;
 
 	MoveLogger ml;
 
 	public static boolean NEEDS_REDRAW;
 
 	private String whiteName, blackName;
 
 	TopBar topBar;
 	BottomBar bottomBar;
 
 	public Game(GameDrawingPanel drawingPanel)
 	{
 
 		NEEDS_REDRAW = true;
 		this.drawingPanel = drawingPanel;
 		board = new Board(drawingPanel.getResources(), this);
 		ml = new MoveLogger();
 
 		isWhiteTurn = true;
 		whitePieces = new AbstractPiece[16];
 		blackPieces = new AbstractPiece[16];
 		initializePieces(whitePieces, true);
 		initializePieces(blackPieces, false);
 
 		whiteName = "White";
 		blackName = "Black";
 
 		this.topBar = new TopBar(whiteName);
 		this.bottomBar = new BottomBar();
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
 
 	public Set<Square> selectAndGetMoves(Square s) throws GameException
 	{
 		AbstractPiece p = s.getPiece();
 		if (p == null)
 			return null;
 
 		// if piece belongs to current player
 		if (p.isWhite() == isWhiteTurn)
 		{
 			// select the piece
 			selectedPiece = p;
 			p.select();
 			// remember the selected piece's moves
 			return selectedPieceMoves = selectedPiece.getMoves();
 		} else
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
 				&& (selectedPieceMoves.contains(target)) && selectedPiece.isWhite() == isWhiteTurn
 				&& (!target.hasPiece() || target.getPiece().isCapturable());
 	}
 
 	public boolean makeMove(Square target) throws InvalidMoveException
 	{
 		if (selectedPiece != null && selectedPieceMoves.contains(target))
 		{
 			Square sourceLocation = selectedPiece.getLocation();
 
 			AbstractPiece capturedPiece = selectedPiece.makeMove(target);
 
 			// check if king is in check
 			if (!checkMoveValidity())
 			{
 				unmakeMove(capturedPiece, selectedPiece, target, sourceLocation);
 				return false;
 			}
 			String actionLog;
 			// log to movelogger
 			if (capturedPiece == null)
 			{
 				actionLog = ml.addMove(selectedPiece, sourceLocation, target);
 			} else
 			{
 				actionLog = ml.addMove(selectedPiece, sourceLocation, target, capturedPiece);
 			}
 			// TODO, display actionLog
 			System.out.println(actionLog);
 
 			checkPostMoveConditions();
 
 			playPieceSounds();
 			switchTurns();
 			unselect();
 		} else
 		{
 			throw new InvalidMoveException(
 					"Invalid Move: Either piece not selected or illegal move");
 		}
 		return true;
 	}
 
 	private void unmakeMove(AbstractPiece capturedPiece, AbstractPiece actor,
 			Square destinationLocation, Square sourceLocation)
 	{
 		destinationLocation.removePiece();
 		if (capturedPiece != null)
 			capturedPiece.revive(destinationLocation);
 		actor.setLocation(sourceLocation);
 
 	}
 
 	private void playPieceSounds()
 	{
 		if (!Preferences.MUTE)
 		{
 			SUI.pieceSound.start();
 		}
 	}
 
 	private void switchTurns()
 	{
 		isWhiteTurn = !isWhiteTurn;
 		topBar.setTurnName(isWhiteTurn ? whiteName : blackName, isWhiteTurn);
 	}
 
 	private boolean checkMoveValidity()
 	{
 		// Make sure king not in check
 		AbstractPiece[] enemyPieces = isWhiteTurn ? blackPieces : whitePieces;
 		for (AbstractPiece p : enemyPieces)
 		{
 			if (p.isAlive())
 			{
 				if (p.checkingKing())
 				{
 					drawingPanel.displayMessage(p + " on square " + p.getLocation()
 							+ " is checking king");
					select(p);
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 
 	private void checkPostMoveConditions()
 	{
 		// check to see if theres a check, a checkmate, or a pawn can get
 		// promoted.
 	}
 
 	public boolean isTurn()
 	{
 		if (selectedPiece != null)
 			return isWhiteTurn == selectedPiece.isWhite();
 		else
 			return false;
 	}
 
 	public void onDraw(Canvas canvas)
 	{
 		if (NEEDS_REDRAW)
 		{
 			NEEDS_REDRAW = false;
 			// draw background
 			canvas.drawBitmap(GameDrawingPanel.background, 0, 0, null);
 
 			canvas.save();
 			canvas.clipRect(SUI.PADDING, 0, SUI.WIDTH - SUI.PADDING, SUI.HEIGHT);
 			canvas.drawCircle(SUI.WIDTH / 2, SUI.HEIGHT_CENTER, 6 * SUI.CIRCLE_RADIUS_DIFFERENCE
 					+ SUI.BORDER_WIDTH, SUI.borderShadowPaint);
 
 			canvas.drawCircle(SUI.WIDTH / 2, SUI.HEIGHT_CENTER, 6 * SUI.CIRCLE_RADIUS_DIFFERENCE
 					+ SUI.BORDER_WIDTH, SUI.borderPaint);
 			canvas.restore();
 		}
 		board.onDraw(canvas);
 		topBar.onDraw(canvas);
 		bottomBar.onDraw(canvas);
 	}
 
 	public Board getBoard()
 	{
 		return board;
 	}
 
 	public GameDrawingPanel getDrawingPanel()
 	{
 		return drawingPanel;
 	}
 
 	// **********************************Saving and restoring
 	// *********************************************************//
 
 	// *********************************UI related
 	// shits***********************************/
 
 	public void onClick(int x, int y)
 	{
 		board.onClick(x, y);
 	}
 
 	public void select(AbstractPiece piece)
 	{
 		try
 		{
 			board.unhighlightAllSquares();
 			selectedPiece = piece;
 			selectedPieceMoves = piece.getMoves();
 			board.highlightMoves(selectedPieceMoves);
 			board.select(piece.getLocation());
 		} catch (GameException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void unselect()
 	{
 		board.unhighlightAllSquares();
 		selectedPiece = null;
 		selectedPieceMoves = null;
 	}
 
 }
