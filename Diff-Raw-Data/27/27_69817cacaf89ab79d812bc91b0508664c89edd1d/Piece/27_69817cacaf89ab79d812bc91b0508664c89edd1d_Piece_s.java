 import java.awt.Graphics;
 import javax.swing.ImageIcon;
 
 public abstract class Piece {
 	// A piece is white or black; White moves first
 	public boolean white;
 	public String str;
 	public ImageIcon img;
 	
 	// Set to false after a piece's first move
 	public boolean firstMove = true;
 	
 	abstract boolean validMove(int xfrom, int yfrom, int xto, int yto, boolean capture);
 	
 	static boolean isDiagonal(int xfrom, int yfrom, int xto, int yto) {
 		// A move is diagonal if abs(dx) == abs(dy)
 		return (Math.abs(xto - xfrom) == Math.abs(yto - yfrom));
 	}
 	
 	static boolean isUpDownLeftRight(int xfrom, int yfrom, int xto, int yto) {
 		// Either only the y changes, or only the x changes.
 		return ((xfrom == xto && yfrom != yto) ||
 				(yfrom == yto && xfrom != xto));
 	}
 	
 	public void paint(Graphics g, int x, int y) {
 		g.drawImage(this.img.getImage(), x, y, null);
 	}
 }
 
 /*
  * Concrete implementations of Piece follow. 
  * Each of the following classes are responsible for validating moves
  * of their own type. (Move validations attempt to conform to chess standards.)
  */
 
 class Pawn extends Piece {
 	
 	public Pawn(boolean white) {
 		this.white = white;
 		this.str = (white) ? "P" : "p";
 		
 		String imgstr = "resources/" + ((white) ? "wpawn.gif" : "bpawn.gif");
 		this.img = new ImageIcon( getClass().getResource(imgstr) );
 	}
 	
 	@Override
 	boolean validMove(int xfrom, int yfrom, int xto, int yto, boolean capture) {
		return (this.validDirection(yfrom, yto) &&			// Pawns move in the (relatively) positive direction.
				(Math.abs(yto - yfrom) <= this.yLimit(yfrom)) &&	// They only move 1 space forward in the y.
				((capture && Math.abs(xto - xfrom) == 1) ||	// It is a capture, and the move is diagonal by 1.
						(!capture && xfrom == xto)));		// It isn't a capture, and the piece moves only forward.
 	}
 	
 	private int yLimit(int yfrom) {
 		return (this.onHomeRow(yfrom)) ? 2 : 1;
 	}
 
 	private boolean validDirection(int yfrom, int yto) {
 		return (this.white) ? (yfrom <= yto) : (yto <= yfrom);
 	}
 	
 	private boolean onHomeRow(int yfrom) {
 		return (this.white) ? (yfrom == 1) : (yfrom == 6);
 	}
 }
 
 class King extends Piece {
 	
 	public King(boolean white) {
 		this.white = white;
 		this.str = (white) ? "K" : "k";
 		
 		String imgstr = "resources/" + ((white) ? "wking.gif" : "bking.gif");
 		this.img = new ImageIcon( getClass().getResource(imgstr) );
 	}
 	
 	@Override
 	boolean validMove(int xfrom, int yfrom, int xto, int yto, boolean capture) {
 		// A King can move one space in either or both the x and y direction.
 		return (Math.abs(xfrom - xto) <= 1 &&
 				Math.abs(yfrom - yto) <= 1);
 	}
 }
 
 class Queen extends Piece {
 	
 	public Queen(boolean white) {
 		this.white = white;
 		this.str = (white) ? "Q" : "q";
 		
 		String imgstr = "resources/" + ((white) ? "wqueen.gif" : "bqueen.gif");
 		this.img = new ImageIcon( getClass().getResource(imgstr) );
 	}
 	
 	@Override
 	boolean validMove(int xfrom, int yfrom, int xto, int yto, boolean capture) {
 		// A Queen can move any number of spaces vertically, horizontally,
 		// diagonally, in any direction.
 		return (isDiagonal(xfrom, yfrom, xto, yto) ||
 				isUpDownLeftRight(xfrom, yfrom, xto, yto));
 	}
 }
 
 class Bishop extends Piece {
 
 	public Bishop(boolean white) {
 		this.white = white;
 		this.str = (white) ? "B" : "b";
 		
 		String imgstr = "resources/" + ((white) ? "wbishop.gif" : "bbishop.gif");
 		this.img = new ImageIcon( Piece.class.getResource(imgstr) );
 	}
 	
 	@Override
 	boolean validMove(int xfrom, int yfrom, int xto, int yto, boolean capture) {
 		return isDiagonal(xfrom, yfrom, xto, yto);
 	}
 }
 
 class Knight extends Piece {
 
 	public Knight(boolean white) {
 		this.white = white;
 		this.str = (white) ? "N" : "n";
 		
 		String imgstr = "resources/" + ((white) ? "wknight.gif" : "bknight.gif");
 		this.img = new ImageIcon( Piece.class.getResource(imgstr) );
 	}
 	
 	@Override
 	boolean validMove(int xfrom, int yfrom, int xto, int yto, boolean capture) {
 		return ((Math.abs(xfrom - xto) == 1 && Math.abs(yfrom - yto) == 2) ||
 				(Math.abs(yfrom - yto) == 1 && Math.abs(xfrom - xto) == 2));
 	}
 }
 
 class Rook extends Piece {
 
 	public Rook(boolean white) {
 		this.white = white;
 		this.str = (white) ? "R" : "r";
 		
 		String imgstr = "resources/" + ((white) ? "wrook.gif" : "brook.gif");
 		this.img = new ImageIcon( getClass().getResource(imgstr) );
 	}
 	
 	@Override
 	boolean validMove(int xfrom, int yfrom, int xto, int yto, boolean capture) {
 		return isUpDownLeftRight(xfrom, yfrom, xto, yto);
 	}
 }
