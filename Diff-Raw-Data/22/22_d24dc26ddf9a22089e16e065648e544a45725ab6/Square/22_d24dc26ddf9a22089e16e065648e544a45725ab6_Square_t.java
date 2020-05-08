 package com.petrifiednightmares.singularityChess.logic;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 
 import com.petrifiednightmares.singularityChess.GameDrawingPanel;
 import com.petrifiednightmares.singularityChess.GameException;
 import com.petrifiednightmares.singularityChess.geom.Circle;
 import com.petrifiednightmares.singularityChess.geom.ComplexShape;
 import com.petrifiednightmares.singularityChess.geom.Rectangle;
 import com.petrifiednightmares.singularityChess.pieces.AbstractPiece;
 import com.petrifiednightmares.singularityChess.ui.Preferences;
 
 public class Square
 {
 	private Square[] _corners;
 	private Square[] _sides;
 	private final char file; // rooks are on file a and h
 	private final int rank; // white on 1 black on 8
 	private Board _board;
 	static Bitmap _squareBitMap;
 	private static Canvas _squareCanvas;
 	private boolean _isWhite;
 	private Paint _paint;
 
 	private AbstractPiece _piece = null;
 	private ComplexShape _shape;
 	private boolean _highlighted;
 	private boolean _selected;
 	private boolean _showSquarePref;
 
 	double flashCount;
 
 	private static int _heightCenter = GameDrawingPanel.TOP_PADDING + 6
 			* GameDrawingPanel.CIRCLE_RADIUS_DIFFERENCE;;
 
 	public boolean NEEDS_REDRAW = true;
 
 	public boolean containsPoint(int x, int y)
 	{
 		return _shape.containsPoint(x, y);
 	}
 
 	public Square(char file, int rank, Board board)
 	{
 		this.file = file;
 		this.rank = rank;
 		this._board = board;
 	}
 
 	public Square(Square[] corners, Square[] sides, char file, int rank)
 	{
 		this._corners = corners;
 		this._sides = sides;
 		this.file = file;
 		this.rank = rank;
 	}
 
 	public void setUpBitMap()
 	{
 
 		// if parity is the same, it's white
 		_isWhite = ((file - 'a') + 1) % 2 == rank % 2;
 		_paint = GameDrawingPanel.darkPaint;
 		if (_isWhite)
 		{
 			_paint = GameDrawingPanel.lightPaint;
 		}
 		_paint.setFlags(Paint.ANTI_ALIAS_FLAG);
 
 		setupShape();
 
 		// Create bitmaps if don't exist yet
 
 		if (Square._squareBitMap == null)
 			Square._squareBitMap = Bitmap.createBitmap(GameDrawingPanel.WIDTH,
 					GameDrawingPanel.HEIGHT, Bitmap.Config.ARGB_8888);
 		if (Square._squareCanvas == null)
 			Square._squareCanvas = new Canvas(_squareBitMap);
 
 		// draw onto bitmap
 		Square._squareCanvas.save();
 
 		_shape.clip(Square._squareCanvas);
 
 		Square._squareCanvas.drawCircle(GameDrawingPanel.WIDTH / 2, _heightCenter,
 				(1 + fileOutwards() + rankOutwards()) * GameDrawingPanel.CIRCLE_RADIUS_DIFFERENCE,
 				_paint);
 
 		Square._squareCanvas.restore();
 
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
 
 		Circle outterCircle = new Circle(GameDrawingPanel.WIDTH / 2, _heightCenter,
 				(1 + fileOutwards() + rankOutwards()) * GameDrawingPanel.CIRCLE_RADIUS_DIFFERENCE);
 
 		_shape.addInsideShape(outterCircle);
 
 		Rectangle borderRect;
 
 		if (file <= 'd')
 		{
 			borderRect = new Rectangle(GameDrawingPanel.WIDTH / 2 - ('d' - file + 1) * 12
 					* GameDrawingPanel.UNIT, 0, GameDrawingPanel.WIDTH / 2 - ('d' - file) * 12
 					* GameDrawingPanel.UNIT, GameDrawingPanel.HEIGHT);
 		} else
 		{
 			borderRect = new Rectangle(GameDrawingPanel.WIDTH / 2 + (file - 'e') * 12
 					* GameDrawingPanel.UNIT, 0, GameDrawingPanel.WIDTH / 2 + (file - 'e' + 1) * 12
 					* GameDrawingPanel.UNIT, GameDrawingPanel.HEIGHT);
 		}
 		_shape.addInsideShape(borderRect);
 
 		Circle innerCircle = new Circle(GameDrawingPanel.WIDTH / 2, _heightCenter,
 				(fileOutwards() + rankOutwards()) * GameDrawingPanel.CIRCLE_RADIUS_DIFFERENCE);
 
 		_shape.addOutsideShape(innerCircle);
 
 		// Determines which side of the board the square is on
 		Rectangle boardSideRect;
 		if (rank > Board.boardRanks[file - 'a'] / 2 + 1)
 		{
 			boardSideRect = new Rectangle(0, 0, GameDrawingPanel.WIDTH, _heightCenter);
 			_shape.addInsideShape(boardSideRect);
 		} else if (rank < Board.boardRanks[file - 'a'] / 2 + 1)
 		{
 
 			boardSideRect = new Rectangle(0, _heightCenter, GameDrawingPanel.WIDTH,
 					GameDrawingPanel.HEIGHT);
 			_shape.addInsideShape(boardSideRect);
 		}
 
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
 		float textWidth = GameDrawingPanel.piecePaint.measureText(file + "" + rank);
 		c.drawText(file + "" + rank, _shape.getX() - textWidth / 2, _shape.getY(),
 				GameDrawingPanel.labelPaint);
 	}
 
 	private void flashSquare(Canvas c)
 	{
 
 		_isWhite = ((file - 'a') + 1) % 2 == rank % 2;
 		_paint = GameDrawingPanel.darkPaint;
 		if (_isWhite)
 		{
 			_paint = GameDrawingPanel.lightPaint;
 		}
 
 		drawSquare(c);
 
 		_paint = GameDrawingPanel.flashPaint;
 		_paint.setAlpha((int) (127 * Math.sin(flashCount) + 127));
 
 		flashCount += 0.1;
 
 		drawSquare(c);
 
 		NEEDS_REDRAW = true;
 	}
 
 	private void drawSquare(Canvas c)
 	{
 		c.save();
 
 		_shape.clip(c);
 
 		if (rank == Board.boardRanks[file - 'a'] / 2 + 1)
 			c.drawCircle(GameDrawingPanel.WIDTH / 2, _heightCenter,
 					(1 + fileOutwards() + rankOutwards())
 							* GameDrawingPanel.CIRCLE_RADIUS_DIFFERENCE, _paint);
 		else if (rank > Board.boardRanks[file - 'a'] / 2 + 1)
 			c.drawRect(0, 0, GameDrawingPanel.WIDTH, _heightCenter, _paint);
 		else
 			c.drawRect(0, _heightCenter, GameDrawingPanel.WIDTH, GameDrawingPanel.HEIGHT, _paint);
 
 		c.restore();
 	}
 
 	public void onDraw(Canvas c)
 	{
 		if (NEEDS_REDRAW)
 		{
 			NEEDS_REDRAW = false;
 	
 			if (Preferences.SHOW_SQUARE_LABELS)
 			{
 				labelSquare(c);
 			}		
 	
 			if (_highlighted)
 			{
 				_paint = GameDrawingPanel.highlightPaint;
 				if (_piece != null && _piece.isCapturable() == true)
 				{
 					_paint = GameDrawingPanel.attackPaint;
 				} else if (_piece != null && _piece.isCapturable() == false)
 				{
 					_paint = GameDrawingPanel.kingThreatenPaint;
 				}
 	
 				drawSquare(c);
 			}
 			if (_selected)
 			{
 				flashSquare(c);
 			}
 	
 			if (_piece != null)
 				_piece.onDraw(c, _shape.getX(), _shape.getY());
 		}
 		
 		if (_showSquarePref != Preferences.SHOW_SQUARE_LABELS)
 		{
 			NEEDS_REDRAW = true;
 			Board.NEEDS_REDRAW = true;
 			_showSquarePref = Preferences.SHOW_SQUARE_LABELS;
 		}
 	}
 
 	public void highlight()
 	{
 		_highlighted = true;
 		NEEDS_REDRAW = true;
 		if (this.getTag().compareTo("d12")==0)
 			this._board.getSquares().get("d6").highlight();
 		if (this.getTag().compareTo("e12")==0)
 			this._board.getSquares().get("e6").highlight();
 	}
 
 	public void unhighlight()
 	{
 		_highlighted = false;
 		_selected = false;
 		NEEDS_REDRAW = true;
 		if (this.getTag().compareTo("d12")==0)
 			this._board.getSquares().get("d6").unhighlight();
 		if (this.getTag().compareTo("e12")==0)
 			this._board.getSquares().get("e6").unhighlight();
 	}
 
 	public void select()
 	{
 		flashCount = 200;
 		_selected = true;
 		NEEDS_REDRAW = true;
 	}
 
 	public void removePiece()
 	{
 		this._piece = null;
 		NEEDS_REDRAW = true;
 		if (this.getTag().compareTo("d12")==0)
 			this._board.getSquares().get("d6").removePiece();
 		if (this.getTag().compareTo("e12")==0)
 			this._board.getSquares().get("e6").removePiece();
 	}
 
 	public void addPiece(AbstractPiece piece)
 	{
 		this._piece = piece;
 		NEEDS_REDRAW = true;
 		if (this.getTag().compareTo("d12")==0)
 			this._board.getSquares().get("d6").addPiece(piece);
 		if (this.getTag().compareTo("e12")==0)
 			this._board.getSquares().get("e6").addPiece(piece);
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
 				|| (this.getTag().compareTo("e12")!=0 && this.getTag().compareTo("e6")!=0 && 
 				    firstSide.getTag().compareTo("d12")==0 && s.getTag().compareTo("d6")==0)
 				|| (this.getTag().compareTo("e12")!=0 && this.getTag().compareTo("e6")!=0 && 
 				    firstSide.getTag().compareTo("d6")==0 && s.getTag().compareTo("d12")==0)
 				|| (this.getTag().compareTo("d12")!=0 && this.getTag().compareTo("d6")!=0 && 
 					firstSide.getTag().compareTo("e12")==0 && s.getTag().compareTo("e6")==0)
 				|| (this.getTag().compareTo("d12")!=0 && this.getTag().compareTo("d6")!=0 && 
 					firstSide.getTag().compareTo("e6")==0 && s.getTag().compareTo("e12")==0)
 				)
 			{
 				return _sides[(i + 2) % 4]; // might be null
 			}
 		}
 		throw new GameException("given side square " + firstSide + " is not adjacent to this square" + this);
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
 		return rank;
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
