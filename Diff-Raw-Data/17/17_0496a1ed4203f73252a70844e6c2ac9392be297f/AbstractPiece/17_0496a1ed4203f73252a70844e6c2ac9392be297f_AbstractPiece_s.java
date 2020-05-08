 package com.petrifiednightmares.singularityChess.pieces;
 
 import java.util.Set;
 
 import android.graphics.Bitmap;
 import android.graphics.Canvas;
 
 import com.petrifiednightmares.singularityChess.GameException;
 import com.petrifiednightmares.singularityChess.logic.Game;
 import com.petrifiednightmares.singularityChess.logic.Square;
 import com.petrifiednightmares.singularityChess.ui.SUI;
 
 public abstract class AbstractPiece
 {
 	protected Game game;
 	protected Square location;
 	protected boolean alive;
 	protected boolean isWhite;
 	private String _label; // DO NOT CHANGE THIS, let's keep the unicode as a
 							// label
 	// If using bitmaps again, add a new variable
 	private boolean _isSelected;
 
 	private Bitmap _icon;
 
 	public static enum PieceType
 	{
 		King(0), Queen(1), Rook(2), Knight(3), Bishop(4), Pawn(5);
 
 		private final int value;
 
 		private PieceType(int value)
 		{
 			this.value = value;
 		}
 
 		public int getValue()
 		{
 			return value;
 		}
 	}
 
 	PieceType type;
 
 	public AbstractPiece(Game game, Square location, boolean isWhite, String label, Bitmap icon,
 			PieceType type)
 	{
 		this.game = game;
 		this.location = location;
 		this.alive = true;
 		this.isWhite = isWhite;
 		this._label = label;
 
 		this._icon = icon;
 		this.type = type;
 	}
 
 	public void select()
 	{
 		_isSelected = true;
 	}
 
 	public void unselect()
 	{
 		_isSelected = false;
 	}
 
 	public boolean getIsSelected()
 	{
 		return _isSelected;
 	}
 
 	public abstract Set<Square> getMoves() throws GameException;
 
 	public void onDraw(Canvas c, int x, int y)
 	{
 		c.drawBitmap(_icon, x - _icon.getWidth() / 2, y - _icon.getHeight() / 2, SUI.piecePaint);
 	}
 
 	// returns captured pieces
 	public AbstractPiece makeMove(Square target)
 	{
 		this.location.removePiece();
 		this.location = target;
 		AbstractPiece captured = this.location.getPiece();
 		this.location.addPiece(this);
 		if (captured != null)
 		{
 			captured.getCaptured();
 		}
 		return captured;
 	}
 
 	public void getCaptured()
 	{
 		this.alive = false;
 	}
 
 	public Game getGame()
 	{
 		return game;
 	}
 
 	public Square getLocation()
 	{
 		return location;
 	}
 
 	public boolean isAlive()
 	{
 		return alive;
 	}
 
 	public boolean isWhite()
 	{
 		return isWhite;
 	}
 
 	public boolean isCapturable()
 	{
 		return true;
 	}
 
 	public String getIcon()
 	{
 		return _label;
 	}
 
 	public PieceType getType()
 	{
 		return type;
 	}
 
 	public boolean checkingKing()
 	{
 		try
 		{
 			Set<Square> moves = getMoves();
 			for (Square m : moves)
 			{
 				if (m.hasPiece())
 				{
 					AbstractPiece p = m.getPiece();
 					if (p.isWhite != isWhite && p instanceof King)
 					{
 						return true;
 					}
 				}
 			}
 		} catch (GameException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return false;
 	}
 
 	public void revive(Square s)
 	{
 		alive = true;
 		setLocation(s);
 	}
 
 	public void setLocation(Square s)
 	{
 		location = s;
 		s.addPiece(this);
 	}
 
 	public void setIsAlive(boolean isAlive)
 	{
 		this.alive=isAlive;
 	}
 
 }
