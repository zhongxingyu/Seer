 package com.petrifiednightmares.singularityChess.logic;
 
 import android.graphics.Canvas;
 import android.graphics.Paint;
 import android.graphics.Rect;
 
 import com.petrifiednightmares.singularityChess.GameDrawingPanel;
 import com.petrifiednightmares.singularityChess.GameException;
 import com.petrifiednightmares.singularityChess.geom.Circle;
 import com.petrifiednightmares.singularityChess.geom.ComplexShape;
 import com.petrifiednightmares.singularityChess.pieces.AbstractPiece;
 import com.petrifiednightmares.singularityChess.ui.GameDrawable;
 import com.petrifiednightmares.singularityChess.ui.Preferences;
 import com.petrifiednightmares.singularityChess.ui.SUI;
 
 public class Square extends GameDrawable
 {
 	private Square[] _corners;
 	private Square[] _sides;
 	private final char file; // rooks are on file a and h
 	private final int rank; // white on 1 black on 8
 	private Board _board;
 	private boolean _isWhite;
 	private Paint _paint;
 
 	private AbstractPiece _piece = null;
 	private ComplexShape _shape;
 	private boolean _highlighted;
 	private boolean _selected;
 	private boolean _showSquarePref;
 
 	double flashCount;
 
 	public boolean onClick(int x, int y)
 	{
 		return _shape.containsPoint(x, y);
 	}
 
 	public Square(char file, int rank, Board board, GameDrawingPanel gdp)
 	{
 		super(gdp);
 		this.file = file;
 		this.rank = rank;
 		this._board = board;
 	}
 
 	public Square(Square[] corners, Square[] sides, char file, int rank, Board board,
 			GameDrawingPanel gdp)
 	{
 		super(gdp);
 		this._corners = corners;
 		this._sides = sides;
 		this.file = file;
 		this.rank = rank;
 		this._board = board;
 	}
 
 	public void setUpBitMap(Canvas c)
 	{
 
 		// if parity is the same, it's white
 		_isWhite = ((file - 'a') + 1) % 2 == rank % 2;
 		_paint = SUI.darkPaint;
 		if (_isWhite)
 		{
 			_paint = SUI.lightPaint;
 		}
 		_paint.setFlags(Paint.ANTI_ALIAS_FLAG);
 
 		setupShape();
 
 		_shape.onDraw(c, _paint);
 
 	}
 
 	public void setSides(Square[] sides)
 	{
 		this._sides = sides;
 	}
 
 	public void setCorners(Square[] corners)
 	{
 		this._corners = corners;
 	}
 
 	private void setupShape()
 	{
 		_shape = new ComplexShape();
 
 		Circle outterCircle = new Circle(SUI.WIDTH / 2, SUI.HEIGHT_CENTER,
 				(1 + fileOutwards() + rankOutwards()) * SUI.CIRCLE_RADIUS_DIFFERENCE);
 
 		_shape.setOutterCircle(outterCircle);
 
 		Rect borderRect;
 
 		// determine left or right side of board
 		if (file <= 'd')
 		{
 			borderRect = new Rect(SUI.WIDTH / 2 - ('d' - file + 1) * SUI.CIRCLE_RADIUS_DIFFERENCE,
 					0, SUI.WIDTH / 2 - ('d' - file) * SUI.CIRCLE_RADIUS_DIFFERENCE, SUI.HEIGHT);
 			_shape.setRight(false);
 		} else
 		{
 			borderRect = new Rect(SUI.WIDTH / 2 + (file - 'e') * SUI.CIRCLE_RADIUS_DIFFERENCE, 0,
 					SUI.WIDTH / 2 + (file - 'e' + 1) * SUI.CIRCLE_RADIUS_DIFFERENCE, SUI.HEIGHT);
 			_shape.setRight(true);
 		}
 		_shape.setBoundingRect(borderRect);
 
 		// build inner circle
 		Circle innerCircle = new Circle(SUI.WIDTH / 2, SUI.HEIGHT_CENTER,
 				(fileOutwards() + rankOutwards()) * SUI.CIRCLE_RADIUS_DIFFERENCE);
 
 		_shape.setInnerCircle(innerCircle);
 
 		// Determines which side of the board the square is on
 		if (rank > Board.boardRanks[file - 'a'] / 2 + 1)
 		{
 			_shape.setTop(1);
 		} else if (rank < Board.boardRanks[file - 'a'] / 2 + 1)
 		{
 			_shape.setTop(0);
 		}
 
 		_shape.setupPath();
 
 	}
 
 	private int fileOutwards()
 	{
 		if (file <= 'd')
 			return 'd' - file;
 		else
 			return file - 'e';
 	}
 
 	private int rankOutwards()
 	{
 
 		if (rank >= Board.boardRanks[file - 'a'] / 2 + 1)
 		{
 			return rank - (Board.boardRanks[file - 'a'] / 2 + 1);
 		} else
 		{
 			return Board.boardRanks[file - 'a'] / 2 + 1 - rank;
 		}
 
 	}
 
 	private void labelSquare(Canvas c)
 	{
 		float textWidth = SUI.piecePaint.measureText(file + "" + rank);
 		c.drawText(file + "" + rank, _shape.getX() - textWidth / 2, _shape.getY(), SUI.labelPaint);
 	}
 
 	public void onDraw(Canvas c)
 	{
 
 		if (Preferences.SHOW_SQUARE_LABELS)
 		{
 			labelSquare(c);
 		}
 
 		if (_highlighted)
 		{
 			_paint = _board.getGame().isTurn() ? SUI.highlightPaint : SUI.highlightPaint2;
 			if (_piece != null && _piece.isCapturable() == true)
 			{
 				_paint = _board.getGame().isTurn() ? SUI.attackPaint : SUI.attackPaint2;
 			} else if (_piece != null && _piece.isCapturable() == false)
 			{
 				_paint = _board.getGame().isTurn() ? SUI.kingThreatenPaint : SUI.kingThreatenPaint2;
 			}
 
 			drawSquare(c);
 		}
 		if (_selected)
 		{
 			flashSquare(c);
 		}
 
		if (_piece != null)
 			_piece.onDraw(c, _shape.getX(), _shape.getY());
 
 		if (_showSquarePref != Preferences.SHOW_SQUARE_LABELS)
 		{
 			redraw();
 			_board.redraw();
 			_showSquarePref = Preferences.SHOW_SQUARE_LABELS;
 		}
 	}
 
 	private void drawSquare(Canvas c)
 	{
 		c.save();
 
 		_shape.onDraw(c, _paint);
 
 		c.restore();
 	}
 
 	private void flashSquare(Canvas c)
 	{
 
 		_isWhite = ((file - 'a') + 1) % 2 == rank % 2;
 		_paint = SUI.darkPaint;
 		if (_isWhite)
 		{
 			_paint = SUI.lightPaint;
 		}
 
 		drawSquare(c);
 
 		_paint = _board.getGame().isTurn() ? SUI.flashPaint : SUI.flashPaint2;
 		_paint.setAlpha((int) (127 * Math.sin(flashCount) + 127));
 
 		flashCount += 0.1;
 
 		drawSquare(c);
 
 		redraw();
 	}
 
 	public void highlight()
 	{
 		_highlighted = true;
 		redraw();
 		if (this.getTag().compareTo("d12") == 0)
 			this._board.getSquares().get("d6").highlight();
 		if (this.getTag().compareTo("e12") == 0)
 			this._board.getSquares().get("e6").highlight();
 	}
 
 	public void unhighlight()
 	{
 		_highlighted = false;
 		_selected = false;
 		redraw();
 		if (this.getTag().compareTo("d12") == 0)
 			this._board.getSquares().get("d6").unhighlight();
 		if (this.getTag().compareTo("e12") == 0)
 			this._board.getSquares().get("e6").unhighlight();
 	}
 
 	public void select()
 	{
 		flashCount = 200;
 		_selected = true;
 		redraw();
 	}
 
 	public void removePiece()
 	{
 		this._piece = null;
 		redraw();
 		if (this.getTag().compareTo("d12") == 0 && this._board.getSquares().get("d6").hasPiece())
 			this._board.getSquares().get("d6").removePiece();
 		if (this.getTag().compareTo("d6") == 0 && this._board.getSquares().get("d12").hasPiece())
 			this._board.getSquares().get("d12").removePiece();
 		if (this.getTag().compareTo("e12") == 0 && this._board.getSquares().get("e6").hasPiece())
 			this._board.getSquares().get("e6").removePiece();
 		if (this.getTag().compareTo("e6") == 0 && this._board.getSquares().get("e12").hasPiece())
 			this._board.getSquares().get("e12").removePiece();
 	}
 
 	public void addPiece(AbstractPiece piece)
 	{
 		this._piece = piece;
 		redraw();
 		if (this.getTag().compareTo("d12") == 0 && !this._board.getSquares().get("d6").hasPiece())
 			this._board.getSquares().get("d6").addPiece(piece);
 		if (this.getTag().compareTo("e12") == 0 && !this._board.getSquares().get("e6").hasPiece())
 			this._board.getSquares().get("e6").addPiece(piece);
 		if (this.getTag().compareTo("d6") == 0 && !this._board.getSquares().get("d12").hasPiece())
 			this._board.getSquares().get("d12").addPiece(piece);
 		if (this.getTag().compareTo("e6") == 0 && !this._board.getSquares().get("e12").hasPiece())
 			this._board.getSquares().get("e12").addPiece(piece);
 	}
 
 	public AbstractPiece getPiece()
 	{
 		return this._piece;
 	}
 
 	public Square getNextSide(Square firstSide) throws GameException
 	{
 		for (int i = 0; i < 4; i++)
 		{
 			Square s = _sides[i];
 			if (firstSide.equals(s)
 					|| (this.getTag().compareTo("e12") != 0 && this.getTag().compareTo("e6") != 0
 							&& firstSide.getTag().compareTo("d12") == 0 && s.getTag().compareTo(
 							"d6") == 0)
 					|| (this.getTag().compareTo("e12") != 0 && this.getTag().compareTo("e6") != 0
 							&& firstSide.getTag().compareTo("d6") == 0 && s.getTag().compareTo(
 							"d12") == 0)
 					|| (this.getTag().compareTo("d12") != 0 && this.getTag().compareTo("d6") != 0
 							&& firstSide.getTag().compareTo("e12") == 0 && s.getTag().compareTo(
 							"e6") == 0)
 					|| (this.getTag().compareTo("d12") != 0 && this.getTag().compareTo("d6") != 0
 							&& firstSide.getTag().compareTo("e6") == 0 && s.getTag().compareTo(
 							"e12") == 0))
 			{
 				return _sides[(i + 2) % 4]; // might be null
 			}
 		}
 		throw new GameException("given side square " + firstSide
 				+ " is not adjacent to this square" + this);
 	}
 
 	// This is for knights, it gets the elbow shaped ones.
 	public Square[] getAdjacentSides(Square firstSide) throws GameException
 	{
 		for (int i = 0; i < 4; i++)
 		{
 			Square s = _sides[i];
 			if (firstSide.equals(s))
 			{
 				// + 3 is same thing as -1
 				return new Square[] { _sides[(i + 1) % 4], _sides[(i + 3) % 4] };
 			}
 		}
 		throw new GameException("given square side " + firstSide
 				+ " is not adjacent to this square " + this + ".");
 	}
 
 	public Square getNextCorner(Square firstCorner) throws GameException
 	{
 		for (int i = 0; i < 4; i++)
 		{
 			Square c = _corners[i];
 			if (firstCorner.equals(c))
 			{
 				return _corners[(i + 2) % 4]; // might be null
 			}
 		}
 		throw new GameException("given corner " + firstCorner + " is not adjacent to this square"
 				+ this);
 	}
 
 	@Override
 	public String toString()
 	{
 		return "Square: " + file + "" + rank;
 	}
 
 	public Square[] getCorners()
 	{
 		return _corners;
 	}
 
 	public Square[] getSides()
 	{
 		return _sides;
 	}
 
 	public char getFile()
 	{
 		return file;
 	}
 
 	public String getTag()
 	{
 		String s = Character.toString(file);
 		s += Integer.toString(rank);
 		return s;
 	}
 
 	public int getRank()
 	{
 		return rank == 12 ? 6 : rank;
 	}
 
 	public boolean hasPiece()
 	{
 		return _piece != null;
 	}
 
 	public boolean isHighlighted()
 	{
 		return this._highlighted;
 	}
 
 }
