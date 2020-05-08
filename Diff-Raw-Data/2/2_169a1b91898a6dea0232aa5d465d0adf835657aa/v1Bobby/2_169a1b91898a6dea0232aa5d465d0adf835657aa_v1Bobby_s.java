 package generalChess;
 
 import java.awt.Point;
 import java.util.ArrayList;
 import java.util.Random;
 
 import sharedfiles.Bishop;
 import sharedfiles.Blank;
 import sharedfiles.Board;
 import sharedfiles.King;
 import sharedfiles.Knight;
 import sharedfiles.Pawn;
 import sharedfiles.Piece;
 import sharedfiles.Queen;
 import sharedfiles.Rook;
 
 public class v1Bobby {
 	public boolean color;
 	public Piece[][] b;
 	public int move;
 	public int numTurns = 0;
 
 	public v1Bobby(Board b, boolean c) {
 		this.color = c;
 		this.b = new Piece[8][8];
 		getBoard(b);
 		move = 0;
 	}
 
 	public v1Bobby(Piece[][] b, boolean c) {
 		this.color = c;
 		this.b = new Piece[8][8];
 		getBoard(b);
 		move = 0;
 	}
 
 	public void set(int a, int c, Piece p) {
 		b[a][c] = p;
 	}
 
 	public boolean getColor() {
 		return color;
 	}
 
 	public Piece[][] getPieceArray() {
 		return b;
 	}
 
 	public Piece[][] getB() {
 		return b;
 	}
 
 	public void getBoard(Board b) {
 		for (int y = 0; y < 8; y++) {
 			for (int x = 0; x < 8; x++) {
 				char t = b.getBoardArray()[x][y].toString().charAt(1);
 				boolean tt = b.getBoardArray()[x][y].getColor();
 				switch (t) {
 					case 'P':
 						this.b[x][y] = new Pawn(tt);
 						break;
 					case 'R':
 						this.b[x][y] = new Rook(tt);
 						break;
 
 					case 'N':
 						this.b[x][y] = new Knight(tt);
 						break;
 
 					case 'B':
 						this.b[x][y] = new Bishop(tt);
 						break;
 
 					case 'K':
 						this.b[x][y] = new King(tt);
 						break;
 
 					case 'Q':
 						this.b[x][y] = new Queen(tt);
 						break;
 
 					case 'X':
 						this.b[x][y] = new Blank(true);
 						break;
 
 				}
 			}
 		}
 	}
 
 	public void getBoard(Piece[][] u) {
 		for (int y = 0; y < 8; y++) {
 			for (int x = 0; x < 8; x++) {
 				char t = u[x][y].toString().charAt(1);
 				boolean tt = u[x][y].getColor();
 				switch (t) {
 					case 'P':
 						this.b[x][y] = new Pawn(tt);
 						break;
 					case 'R':
 						this.b[x][y] = new Rook(tt);
 						break;
 
 					case 'N':
 						this.b[x][y] = new Knight(tt);
 						break;
 
 					case 'B':
 						this.b[x][y] = new Bishop(tt);
 						break;
 
 					case 'K':
 						this.b[x][y] = new King(tt);
 						break;
 
 					case 'Q':
 						this.b[x][y] = new Queen(tt);
 						break;
 
 					case 'X':
 						this.b[x][y] = new Blank(true);
 						break;
 
 				}
 			}
 		}
 	}
 
 	public void getBoard(String[] a) {
 		for (int i = 0; i < 8; i++) {
 			boolean col;
 			char typ;
 			int g = 0;
 			for (int j = 0; j < 40; j += 5) {
 				if (a[i].charAt(j) == 'B') {
 					col = false;
 				} else
 					col = true;
 				typ = a[i].charAt(j + 1);
 				switch (typ) {
 					case 'P':
 						this.b[g][i] = new Pawn(col);
 						break;
 					case 'R':
 						this.b[g][i] = new Rook(col);
 						break;
 
 					case 'N':
 						this.b[g][i] = new Knight(col);
 						break;
 
 					case 'B':
 						this.b[g][i] = new Bishop(col);
 						break;
 
 					case 'K':
 						this.b[g][i] = new King(col);
 						break;
 
 					case 'Q':
 						this.b[g][i] = new Queen(col);
 						break;
 
 					case 'X':
 						this.b[g][i] = new Blank(true);
 						break;
 
 				}
 				g++;
 			}
 		}
 	}
 
 	public void move(int ax, int ay, int bx, int by) {
 		char t = this.b[ax][ay].toString().charAt(1);
 		boolean c = this.b[ax][ay].getColor();
 		this.b[ax][ay] = new Blank(true);
 		switch (t) {
 			case 'P':
 				this.b[bx][by] = new Pawn(c);
 				break;
 			case 'R':
 				this.b[bx][by] = new Rook(c);
 				break;
 			case 'N':
 				this.b[bx][by] = new Knight(c);
 				break;
 			case 'B':
 				this.b[bx][by] = new Bishop(c);
 				break;
 			case 'K':
 				this.b[bx][by] = new King(c);
 				break;
 			case 'Q':
 				this.b[bx][by] = new Queen(c);
 				break;
 		}
 
 	}
 
 	public void printBoard() {
 		for (int x = 0; x < 8; x++) {
 			for (int y = 0; y < 8; y++) {
 				if (b[y][x].toString().equals("WX"))
 					System.out.print("   | ");
 				else
 					System.out.print(b[y][x].toString() + " | ");
 			}
 			System.out.println();
 			System.out.println("---------------------------------------");
 		}
 
 		System.out.println(".");
 		System.out.println();
 
 	}
 
 	public ArrayList<ArrayList> kMoves() {
 		ArrayList<ArrayList> d = new ArrayList();
 		// king can't move into check
 		v1Bobby enemy = new v1Bobby(this.getB(), !color);
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'K') {
 					ArrayList g = new ArrayList();
 					int x = j;
 					int y = k;
 					g.add(b[x][y]);
 					g.add(new Point(x, y));
 					if (x == -1 || y == -1) {
 						return null;
 					} else {
 						for (int a = x - 1; a < x + 2; a++) {
 							for (int b = y - 1; b < y + 2; b++) {
 								if (a > -1 && a < 8 && b > -1 && b < 8) {
 									if ((this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 											&& (enemy.numDefenders(a, b) == 0)) {
 	
 											//checks to see if piece moved to a,b if it would still be under attack 
 											Point oldLocation= new Point(x,y);
 											Point p4=new Point(a,b);
 											boolean possibleMove=false;
 											boolean dontLook= false;
 											if(this.b[a][b].toString().charAt(1) != 'X'){
 												dontLook=true;
 											}
 											if(dontLook==false)
 											{	
 											move(x,y,a,b);
 											enemy.getBoard(this.b);
 											if(enemy.numDefenders(a,b)==0) possibleMove=true;
 											move(a,b,x,y);
 											}
 											enemy.getBoard(this.b);
 											
 										if(possibleMove==true) g.add(new Point(a, b));
 									}
 								}
 							}
 						}
 					}
 					d.add(g);
 				}
 			}
 		}
 
 		return d;
 	}
 
 	public ArrayList<ArrayList> qMoves() {
 		ArrayList<ArrayList> d = new ArrayList<ArrayList>();
 		int x = 0;
 		int y = 0;
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'Q') {
 					ArrayList g = new ArrayList();
 					x = j;
 					y = k;
 					g.add(b[x][y]);
 					g.add(new Point(x, y));
 					if (x == -1 || y == -1) {
 						return null;
 					} else {
 						// checks queen for diagonal down to the right. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						int a = x;
 						int b = y;
 
 						int quit = 0;
 						if (b != 7 && a != 7) // if the queen is on the last row
 												// or furthest right column of
 												// the board no need to check
 												// down and to the right
 						{
 							do {
 								a = a + 1;
 								b = b + 1;
 								if (a == 7)
 									quit = -999;
 								if (b == 7)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks queen for diagonal down to the left. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 7 && a != 0) // if the queen is on the last row
 												// or leftmost column of the
 												// board no need to check down
 												// and to the left
 						{
 							do {
 								a = a - 1;
 								b = b + 1;
 								if (a == 0)
 									quit = -999;
 								if (b == 7)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks queen for diagonal up to the left. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 0 && a != 0) {// if the queen is on the first
 												// row of the board or leftmost
 												// column no need to check up
 												// and to the left
 							do {
 								a = a - 1;
 								b = b - 1;
 								if (a == 0)
 									quit = -999;
 								if (b == 0)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks queen for diagonal up to the right. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 0 && a != 7)
 						// if queen is on the highest row and rightmost column
 						// no need to look up and to the right
 						{
 							do {
 								a = a + 1;
 								b = b - 1;
 								if (a == 7)
 									quit = -999;
 								if (b == 0)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks queen north. quits if adds an opposite color
 						// piece or
 						// reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (b == 0)
 							b = b + 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							b = b - 1;
 							if (b == 0)
 								quit = -999;
 							if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks queen south. quits if adds an opposite color
 						// piece or
 						// reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (b == 7)
 							b = b - 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							b = b + 1;
 							if (b == 7)
 								quit = -999;
 							if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks queen to the west. quits if adds an opposite
 						// color piece
 						// or reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (a == 7)
 							a = a - 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							a = a + 1;
 							if (a == 7)
 								quit = -999;
 							if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks queen to the east. quits if adds an opposite
 						// color piece
 						// or reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (a == 0)
 							a = a + 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							a = a - 1;
 							if (a == 0)
 								quit = -999;
 							if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 					}
 					d.add(g);
 				}
 			}
 		}
 
 		return d;
 	}
 
 	public ArrayList<ArrayList> rMoves() {
 		ArrayList<ArrayList> d = new ArrayList<ArrayList>();
 		int x = 0;
 		int y = 0;
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'R') {
 					ArrayList g = new ArrayList();
 					x = j;
 					y = k;
 					g.add(b[x][y]);
 					g.add(new Point(x, y));
 					if (x == -1 || y == -1) {
 						return null;
 					} else {
 						// checks queen rook. quits if adds an opposite color
 						// piece or
 						// reaches end of board or hits our piece
 						int a = x;
 						int b = y;
 						if (b == 0)
 							b = b + 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						int quit = 0;
 						do {
 							b = b - 1;
 							if (b == 0)
 								quit = -999;
 							if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks rook south. quits if adds an opposite color
 						// piece or
 						// reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (b == 7)
 							b = b - 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							b = b + 1;
 							if (b == 7)
 								quit = -999;
 							if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks rook to the west. quits if adds an opposite
 						// color piece or
 						// reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (a == 7)
 							a = a - 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							a = a + 1;
 							if (a == 7)
 								quit = -999;
 							if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks rook to the east. quits if adds an opposite
 						// color piece or
 						// reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (a == 0)
 							a = a + 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							a = a - 1;
 							if (a == 0)
 								quit = -999;
 							if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 					}
 					d.add(g);
 				}
 			}
 		}
 
 		return d;
 	}
 
 	public ArrayList<ArrayList> nMoves() {
 		ArrayList<ArrayList> v = new ArrayList<ArrayList>();
 		for (int y = 0; y < 8; y++) {
 			for (int x = 0; x < 8; x++) {
 				if (b[x][y].getColor() == color && b[x][y].toString().charAt(1) == 'N') {
 					ArrayList d = new ArrayList();
 					d.add(b[x][y]);
 					d.add(new Point(x, y));
 					if (x - 2 > -1 && y - 1 > -1
 							&& (b[x - 2][y - 1].toString().charAt(1) == 'X' || b[x - 2][y - 1].getColor() == !color))
 						d.add(new Point(x - 2, y - 1));
 					if (x - 1 > -1 && y - 2 > -1
 							&& (b[x - 1][y - 2].toString().charAt(1) == 'X' || b[x - 1][y - 2].getColor() == !color))
 						d.add(new Point(x - 1, y - 2));
 
 					if (x - 2 > -1 && y + 1 < 8
 							&& (b[x - 2][y + 1].toString().charAt(1) == 'X' || b[x - 2][y + 1].getColor() == !color))
 						d.add(new Point(x - 2, y + 1));
 					if (x - 1 > -1 && y + 2 < 8
 							&& (b[x - 1][y + 2].toString().charAt(1) == 'X' || b[x - 1][y + 2].getColor() == !color))
 						d.add(new Point(x - 1, y + 2));
 
 					if (x + 2 < 8 && y + 1 < 8
 							&& (b[x + 2][y + 1].toString().charAt(1) == 'X' || b[x + 2][y + 1].getColor() == !color))
 						d.add(new Point(x + 2, y + 1));
 					if (x + 1 < 8 && y + 2 < 8
 							&& (b[x + 1][y + 2].toString().charAt(1) == 'X' || b[x + 1][y + 2].getColor() == !color))
 						d.add(new Point(x + 1, y + 2));
 
 					if (x + 1 < 8 && y - 2 > -1
 							&& (b[x + 1][y - 2].toString().charAt(1) == 'X' || b[x + 1][y - 2].getColor() == !color))
 						d.add(new Point(x + 1, y - 2));
 					if (x + 2 < 8 && y - 1 > -1
 							&& (b[x + 2][y - 1].toString().charAt(1) == 'X' || b[x + 2][y - 1].getColor() == !color))
 						d.add(new Point(x + 2, y - 1));
 					v.add(d);
 				}
 			}
 		}
 		return v;
 	}
 
 	public ArrayList<ArrayList> bMoves() {
 		ArrayList<ArrayList> m = new ArrayList<ArrayList>();
 		int x = 0;
 		int y = 0;
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'B') {
 					ArrayList g = new ArrayList();
 					x = j;
 					y = k;
 					g.add(b[x][y]);
 					g.add(new Point(x, y));
 					if (x == -1 || y == -1) {
 						return null;
 					} else {
 						// checks bishop for diagonal down to the right. quits
 						// if adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						int a = x;
 						int b = y;
 
 						int quit = 0;
 						if (b != 7 && a != 7) // if the bishop is on the last
 												// row or furthest right column
 												// of the board no need to check
 												// down and to the right
 						{
 							do {
 								a = a + 1;
 								b = b + 1;
 								if (a == 7)
 									quit = -999;
 								if (b == 7)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks queen for diagonal down to the left. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 7 && a != 0) // if the bishop is on the last
 												// row or leftmost column of the
 												// board no need to check down
 												// and to the left
 						{
 							do {
 								a = a - 1;
 								b = b + 1;
 								if (a == 0)
 									quit = -999;
 								if (b == 7)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks bishop for diagonal up to the left. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 0 && a != 0) {// if the bishop is on the first
 												// row of the board or leftmost
 												// column no need to check up
 												// and to the left
 							do {
 								a = a - 1;
 								b = b - 1;
 								if (a == 0)
 									quit = -999;
 								if (b == 0)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks bishop for diagonal up to the right. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 0 && a != 7)
 						// if bishop is on the highest row and rightmost column
 						// no need to look up and to the right
 						{
 							do {
 								a = a + 1;
 								b = b - 1;
 								if (a == 7)
 									quit = -999;
 								if (b == 0)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 					}
 					m.add(g);
 				}
 			}
 		}
 
 		return m;
 	}
 
 	public ArrayList<ArrayList> pMoves() {
 		ArrayList<ArrayList> v = new ArrayList<ArrayList>();
 		for (int y = 0; y < 8; y++) {
 			for (int x = 0; x < 8; x++) {
 				if (b[x][y].getColor() == color && b[x][y].toString().charAt(1) == 'P') {
 					ArrayList d = new ArrayList();
 					d.add(b[x][y]);
 					d.add(new Point(x, y));
 					if (color == false) {
 						if (y == 1 && b[x][y + 2].toString().charAt(1) == 'X'
 								&& b[x][y + 1].toString().charAt(1) == 'X')
 							d.add(new Point(x, y + 2));
 						if (y + 1 < 8 && b[x][y + 1].toString().charAt(1) == 'X')
 							d.add(new Point(x, y + 1));
 						if (x + 1 < 8 && y + 1 < 8
 								&& (b[x + 1][y + 1].toString().charAt(1) != 'X' && b[x + 1][y + 1].getColor() != color))
 							d.add(new Point(x + 1, y + 1));
 						if (x - 1 > -1 && y + 1 < 8
 								&& (b[x - 1][y + 1].toString().charAt(1) != 'X' && b[x - 1][y + 1].getColor() != color))
 							d.add(new Point(x - 1, y + 1));
 					}
 
 					if (color == true) {
 						if (y == 6 && b[x][y - 2].toString().charAt(1) == 'X'
 								&& b[x][y - 1].toString().charAt(1) == 'X')
 							d.add(new Point(x, y - 2));
 						if (y - 1 > -1 && b[x][y - 1].toString().charAt(1) == 'X')
 							d.add(new Point(x, y - 1));
 						if (x + 1 < 8 && y - 1 > -1
 								&& (b[x + 1][y - 1].toString().charAt(1) != 'X' && b[x + 1][y - 1].getColor() != color))
 							d.add(new Point(x + 1, y - 1));
 						if (x - 1 > -1 && y - 1 > -1
 								&& (b[x - 1][y - 1].toString().charAt(1) != 'X' && b[x - 1][y - 1].getColor() != color))
 							d.add(new Point(x - 1, y - 1));
 					}
 					v.add(d);
 				}
 			}
 		}
 		return v;
 	}
 
 	// takes an array of possible returns the index of the highest ranking
 	// opponent piece that any specific piece
 	// has in its possible moves
 	// 1 if no possible pieces to take
 	public int takeIfPossible(ArrayList a) {
 		int king = 1;
 		int queen = 1;
 		int rook = 1;
 		int bishop = 1;
 		int knight = 1;
 		int pawn = 1;
 
 		v1Bobby enemy = new v1Bobby(this.getB(), !color);
 		for (int i = 2; i < a.size(); i++) {
 
 			if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].getColor() == !color
 					&& b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) != 'X') {
 				if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) == 'K') {
 					king = i;
 					return king;
 				}
 			}
 
 			if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].getColor() == !color
 					&& b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) != 'X') {
 				if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) == 'Q') {
 					// first finds an enemy piece to take, then checks to see if
 					// there are more than one enemy piece available
 					// if so, then it chooses the enemy piece with the least
 					// number of defenders for take if possible
 					if (queen == 1) {
 						queen = i;
 					} else if (enemy.numDefenders((int) ((Point) a.get(i)).getX(), (int) ((Point) a.get(i)).getY()) < enemy
 							.numDefenders((int) ((Point) a.get(queen)).getX(), (int) ((Point) a.get(queen)).getY())) {
 						queen = i;
 					}
 				}
 			}
 
 			if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].getColor() == !color
 					&& b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) != 'X') {
 				if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) == 'R') {
 					// first finds an enemy piece to take, then checks to see if
 					// there are more than one enemy piece available
 					// if so, then it chooses the enemy piece with the least
 					// number of defenders for take if possible
 					if (rook == 1) {
 						rook = i;
 					} else if (enemy.numDefenders((int) ((Point) a.get(i)).getX(), (int) ((Point) a.get(i)).getY()) < enemy
 							.numDefenders((int) ((Point) a.get(rook)).getX(), (int) ((Point) a.get(rook)).getY())) {
 						rook = i;
 					}
 				}
 
 			}
 
 			if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].getColor() == !color
 					&& b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) != 'X') {
 				if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) == 'N') {
 					// first finds an enemy piece to take, then checks to see if
 					// there are more than one enemy piece available
 					// if so, then it chooses the enemy piece with the least
 					// number of defenders for take if possible
 					if (knight == 1) {
 						knight = i;
 					} else if (enemy.numDefenders((int) ((Point) a.get(i)).getX(), (int) ((Point) a.get(i)).getY()) < enemy
 							.numDefenders((int) ((Point) a.get(knight)).getX(), (int) ((Point) a.get(knight)).getY())) {
 						knight = i;
 					}
 				}
 
 			}
 
 			if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].getColor() == !color
 					&& b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) != 'X') {
 				if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) == 'B') {
 					// first finds an enemy piece to take, then checks to see if
 					// there are more than one enemy piece available
 					// if so, then it chooses the enemy piece with the least
 					// number of defenders for take if possible
 					if (bishop == 1) {
 						bishop = i;
 					} else if (enemy.numDefenders((int) ((Point) a.get(i)).getX(), (int) ((Point) a.get(i)).getY()) < enemy
 							.numDefenders((int) ((Point) a.get(bishop)).getX(), (int) ((Point) a.get(bishop)).getY())) {
 						bishop = i;
 					}
 				}
 
 			}
 
 			if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].getColor() == !color
 					&& b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) != 'X') {
 				if (b[(int) ((Point) a.get(i)).getX()][(int) ((Point) a.get(i)).getY()].toString().charAt(1) == 'P') {
 					// first finds an enemy piece to take, then checks to see if
 					// there are more than one enemy piece available
 					// if so, then it chooses the enemy piece with the least
 					// number of defenders for take if possible
 					if (pawn == 1) {
 						pawn = i;
 					} else if (enemy.numDefenders((int) ((Point) a.get(i)).getX(), (int) ((Point) a.get(i)).getY()) < enemy
 							.numDefenders((int) ((Point) a.get(pawn)).getX(), (int) ((Point) a.get(pawn)).getY())) {
 						pawn = i;
 					}
 				}
 			}
 		}
 
 		if (queen != 1)
 			return queen;
 		if (rook != 1)
 			return rook;
 		if (knight != 1)
 			return knight;
 		if (bishop != 1)
 			return bishop;
 		if (pawn != 1)
 			return pawn;
 		return 1;
 	}
 
 	public void newBestPieceToTake() {
 		ArrayList<Point> v = new ArrayList<Point>();
 
 		int best = 0;
 		int best1 = 0;
 		int best2 = 0;
 		int equals = 0;
 		int lessThan = -999;
 		ArrayList currPiece = allMoves().get(0);
 		int high = 0;
 		int high2 = -1;
 
 		v1Bobby enemy = new v1Bobby(this.getB(), !color);
 
 		for (int i = 0; i < allMoves().size(); i++) {
 
 			if(takeIfPossible(allMoves().get(i))>1)
 			{
 			
 			char charcurVal=(char) allMoves().get(i).get(0).toString().charAt(1);
 			
 			// p=2 n=3 b=3 r=5 q=9 k=100
 			int curVal = 0;
 			if ( charcurVal== 'P')
 				curVal = 2;
 			else if (charcurVal == 'N')
 				curVal = 3;
 			else if (charcurVal == 'B')
 				curVal = 3;
 			else if (charcurVal == 'R')
 				curVal = 5;
 			else if (charcurVal == 'Q')
 				curVal = 9;
 			else if (charcurVal == 'K')
 				curVal = 100;
 
 			int takeVal = 0;
 			currPiece = allMoves().get(i);
 			
 			char chartakeVal = b[(int) ((Point) currPiece.get(takeIfPossible(currPiece))).getX()][(int) ((Point) currPiece
 					.get(takeIfPossible(currPiece))).getY()].toString().charAt(1);
 			
 			if ( chartakeVal == 'P')
 				takeVal = 2;
 			else if (chartakeVal == 'N')
 				takeVal = 3;
 			else if (chartakeVal == 'B')
 				takeVal = 3;
 			else if (chartakeVal == 'R')
 				takeVal = 5;
 			else if (chartakeVal == 'Q')
 				takeVal = 9;
 			else if (chartakeVal == 'K')
 				takeVal = 100;
 
 			if (takeVal - curVal > high) {
 
 				high = takeVal - curVal;
 				best = i;
 
 			}
 			else if (takeVal - curVal == 0) {
 				// if there is no piece to take of a higher value, it checks the
 				// best piece to take of the same value which the one with less
 				// defenders
 				// so it wont get eaten next turn
 				if (numDefenders(
 						(int) (((Point) allMoves().get(best1).get(takeIfPossible(allMoves().get(best1)))).getX()),
 						(int) (((Point) allMoves().get(best1).get(takeIfPossible(allMoves().get(best1)))).getY())) < numDefenders(
 						(int) (((Point) allMoves().get(i).get(takeIfPossible(allMoves().get(i)))).getX()),
 						(int) (((Point) allMoves().get(i).get(takeIfPossible(allMoves().get(i)))).getY())))
 				{
 				high2 = 0;
 				best1 = i;
 				}
 			}
 
 			else if (takeVal - curVal > lessThan) {
 
 				Point pointOfContention = new Point((int) (((Point) allMoves().get(i).get(
 						takeIfPossible(allMoves().get(i)))).getX()), (int) (((Point) allMoves().get(i).get(
 						takeIfPossible(allMoves().get(i)))).getY()));
 
 				if (enemy.numDefenders((int) pointOfContention.getX(), (int) pointOfContention.getY()) < numDefenders(
 						(int) pointOfContention.getX(), (int) pointOfContention.getY())) {
 					lessThan = takeVal - curVal;
 					best2 = i;
 				}
 			}
 		}
 		}
 		// by now has picked the best piece of a higher value, of equal value,
 		// and if no higher value or equal value available, of lower value
 		// first sees if higher value possible, then equal value, then lower
 		// value. if no pieces can be taken or a piece has too many defenders
 		// will return an array with two of the same location pieces
 		if (high > 0) {
 			v.add((Point) allMoves().get(best).get(1));
 			v.add((Point) allMoves().get(best).get(takeIfPossible(allMoves().get(best))));
 
 		}
 
 		else if (high2 == 0) {
 			v.add((Point) allMoves().get(best1).get(1));
 			v.add((Point) allMoves().get(best1).get(takeIfPossible(allMoves().get(best1))));
 
 		} else {
 
 			v.add((Point) allMoves().get(best2).get(1));
 			v.add((Point) allMoves().get(best2).get(takeIfPossible(allMoves().get(best2))));
 
 		}
 		if (!(v.get(0).equals(v.get(1)))) {
 			this.numTurns++;
 			System.out.println("yum! " + v.get(0) + " moves to " + v.get(1));
 		}
 		move((int) v.get(0).getX(), (int) v.get(0).getY(), (int) v.get(1).getX(), (int) v.get(1).getY());
 	}
 	
 	public ArrayList<Point> newBestPieceToTakeCoords() {
 		ArrayList<Point> v = new ArrayList<Point>();
 
 		int best = 0;
 		int best1 = 0;
 		int best2 = 0;
 		int equals = 0;
 		int lessThan = -999;
 		ArrayList currPiece = allMoves().get(0);
 		int high = 0;
 		int high2 = -1;
 
 		v1Bobby enemy = new v1Bobby(this.getB(), !color);
 
 		for (int i = 0; i < allMoves().size(); i++) {
 
 			if(takeIfPossible(allMoves().get(i))>1)
 			{
 			
 			char charcurVal=(char) allMoves().get(i).get(0).toString().charAt(1);
 			
 			// p=2 n=3 b=3 r=5 q=9 k=100
 			int curVal = 0;
 			if ( charcurVal== 'P')
 				curVal = 2;
 			else if (charcurVal == 'N')
 				curVal = 3;
 			else if (charcurVal == 'B')
 				curVal = 3;
 			else if (charcurVal == 'R')
 				curVal = 5;
 			else if (charcurVal == 'Q')
 				curVal = 9;
 			else if (charcurVal == 'K')
 				curVal = 100;
 
 			int takeVal = 0;
 			currPiece = allMoves().get(i);
 			
 			char chartakeVal = b[(int) ((Point) currPiece.get(takeIfPossible(currPiece))).getX()][(int) ((Point) currPiece
 					.get(takeIfPossible(currPiece))).getY()].toString().charAt(1);
 			
 			if ( chartakeVal == 'P')
 				takeVal = 2;
 			else if (chartakeVal == 'N')
 				takeVal = 3;
 			else if (chartakeVal == 'B')
 				takeVal = 3;
 			else if (chartakeVal == 'R')
 				takeVal = 5;
 			else if (chartakeVal == 'Q')
 				takeVal = 9;
 			else if (chartakeVal == 'K')
 				takeVal = 100;
 
 			if (takeVal - curVal > high) {
 
 				high = takeVal - curVal;
 				best = i;
 
 			}
 			else if (takeVal - curVal == 0) {
 				// if there is no piece to take of a higher value, it checks the
 				// best piece to take of the same value which the one with less
 				// defenders
 				// so it wont get eaten next turn
 				if (numDefenders(
 						(int) (((Point) allMoves().get(best1).get(takeIfPossible(allMoves().get(best1)))).getX()),
 						(int) (((Point) allMoves().get(best1).get(takeIfPossible(allMoves().get(best1)))).getY())) < numDefenders(
 						(int) (((Point) allMoves().get(i).get(takeIfPossible(allMoves().get(i)))).getX()),
 						(int) (((Point) allMoves().get(i).get(takeIfPossible(allMoves().get(i)))).getY())))
 
 					high2 = 0;
 				best1 = i;
 
 			}
 
 			else if (takeVal - curVal > lessThan) {
 
 				Point pointOfContention = new Point((int) (((Point) allMoves().get(i).get(
 						takeIfPossible(allMoves().get(i)))).getX()), (int) (((Point) allMoves().get(i).get(
 						takeIfPossible(allMoves().get(i)))).getY()));
 
 				if (enemy.numDefenders((int) pointOfContention.getX(), (int) pointOfContention.getY()) < numDefenders(
 						(int) pointOfContention.getX(), (int) pointOfContention.getY())) {
 					lessThan = takeVal - curVal;
 					best2 = i;
 				}
 			}
 		}
 		}
 
 		// by now has picked the best piece of a higher value, of equal value,
 		// and if no higher value or equal value available, of lower value
 		// first sees if higher value possible, then equal value, then lower
 		// value. if no pieces can be taken or a piece has too many defenders
 		// will return an array with two of the same location pieces
 		if (high > 0) {
 			v.add((Point) allMoves().get(best).get(1));
 			v.add((Point) allMoves().get(best).get(takeIfPossible(allMoves().get(best))));
 
 		}
 
 		else if (high2 == 0) {
 			v.add((Point) allMoves().get(best1).get(1));
 			v.add((Point) allMoves().get(best1).get(takeIfPossible(allMoves().get(best1))));
 
 		} else {
 
 			v.add((Point) allMoves().get(best2).get(1));
 			v.add((Point) allMoves().get(best2).get(takeIfPossible(allMoves().get(best2))));
 
 		}
 		
 		return v;
 	}
 	
 	public void bestPieceGetOutOfDanger() {
 		ArrayList<Point> v = new ArrayList<Point>();
 		v1Bobby enemy = new v1Bobby(this.getB(), !color);
 
 		v=enemy.newBestPieceToTakeCoords();
 		
 		
 		
 		if ((v.get(0).equals(v.get(1)))) return;
 		else
 				{
 		Point needsToMove = new Point((int)v.get(1).getX(),(int)v.get(1).getY());
 		int gottaMoveIndex= -1;
 		for (int k = 0; k < allMoves().size(); k++) {
 			if (needsToMove.equals((Point) allMoves().get(k).get(1))){
 				gottaMoveIndex=k;
 			}
 			}
 		
 		//allMoves().get(gottaMoveIndex) is a piece that is in the most danger from the opponents and needs to move to safety
 		
 		
 		//piece will move to where there is no enemy defenders if that is possible
 		boolean canIMoveOutOfDanger = false;
 		int counter= 2;
 		while(canIMoveOutOfDanger == false && counter<=allMoves().get(gottaMoveIndex).size()-1)
 		{
 			
 			//move the piece to the location to see if then there would be any attackers on it, then move back
 			Point oldLocation= new Point((int)((Point)allMoves().get(gottaMoveIndex).get(1)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(1)).getY());
 			
 			Point p4=new Point((int)((Point)allMoves().get(gottaMoveIndex).get(counter)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(counter)).getY());
 			
 			//we dont need to move our guy onto an enemy piece
 			boolean dontLook= false;
 			if(b[(int)p4.getX()][(int)p4.getY()].getColor()==!color){
 				dontLook=true;
 			}
 			if(dontLook==false)
 			{	
 			move((int)((Point)allMoves().get(gottaMoveIndex).get(1)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(1)).getY(),
 			(int)((Point)allMoves().get(gottaMoveIndex).get(counter)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(counter)).getY());
 			
 
 			
 			int fakePieceIsHere = -999;
 			for(int z =0; z<allMoves().size(); z++)
 			{
 				Point p3 =new Point((Point)allMoves().get(z).get(1));
 				
 				if(p3.equals(p4)) fakePieceIsHere = z;
 			}
 
 			enemy.getBoard(b);
 			if(enemy.numDefenders((int)((Point)allMoves().get(fakePieceIsHere).get(1)).getX(),(int)((Point)allMoves().get(fakePieceIsHere).get(1)).getY())==0)
 				canIMoveOutOfDanger=true;
 			//find curr location of fake moved piece
 			
 			
 			
 			move((int)((Point)allMoves().get(fakePieceIsHere).get(1)).getX(),(int)((Point)allMoves().get(fakePieceIsHere).get(1)).getY(),(int)oldLocation.getX(),(int)oldLocation.getY());
 			}
 			enemy.getBoard(b);
 			//enemy.allMoves().add(temp);
 			if(canIMoveOutOfDanger==false)counter++;
 		}
 		
 		if(canIMoveOutOfDanger==true)
 		{
 			
 			Point p1=new Point((int)((Point)allMoves().get(gottaMoveIndex).get(1)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(1)).getY());
 			Point p2=new Point((int)((Point)allMoves().get(gottaMoveIndex).get(counter)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(counter)).getY());
 	
 			move((int)((Point)allMoves().get(gottaMoveIndex).get(1)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(1)).getY(),
 			(int)((Point)allMoves().get(gottaMoveIndex).get(counter)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(counter)).getY());
 			this.numTurns++;
 			System.out.println("running from " + p1 + "to " + p2);
 		}
 		else{
 		int bestPlaceIndex= 1;
 		for(int w=2; w<allMoves().get(gottaMoveIndex).size(); w++)
 		{
 			//if the place the in danger piece wants to move is better defended than attacked in pieces and value then it will move there
 			int currDefenders= numDefenders((int)((Point)allMoves().get(gottaMoveIndex).get(w)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(w)).getY()) -1;
 			int enemycurrDefenders= enemy.numDefenders((int)((Point)allMoves().get(gottaMoveIndex).get(w)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(w)).getY());
 			//NEED TO SUBTRACT PIECE VALUE THAT WILL BE MOVING THERE
 			int currDefendersValue= numDefenderValue((int)((Point)allMoves().get(gottaMoveIndex).get(w)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(w)).getY());
 			int enemycurrDefendersValue= enemy.numDefenderValue((int)((Point)allMoves().get(gottaMoveIndex).get(w)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(w)).getY());
 			
 			if(currDefenders>0 && currDefenders>=enemycurrDefenders && currDefendersValue<=enemycurrDefendersValue) bestPlaceIndex=w; 
 		}	
 		
 		Point p1=new Point((int)((Point)allMoves().get(gottaMoveIndex).get(1)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(1)).getY());
 		Point p2=new Point((int)((Point)allMoves().get(gottaMoveIndex).get(bestPlaceIndex)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(bestPlaceIndex)).getY());
 		if(!p1.equals(p2))
 		{
 		this.numTurns++;
 		System.out.println("running from " + p1 + "to " + p2);
 		move((int)((Point)allMoves().get(gottaMoveIndex).get(1)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(1)).getY(),
 				(int)((Point)allMoves().get(gottaMoveIndex).get(bestPlaceIndex)).getX(),(int)((Point)allMoves().get(gottaMoveIndex).get(bestPlaceIndex)).getY());
 		}
 		}
 				
 				
 				}
 		
 			
 			
 	}
 		
 	
 
 	// return numDefenders, 0 if no piece is defending that location, there may
 	// be a piece on that location
 	public int numDefenders(int x, int y) {
 		// num defenders takes in the coordinates of a piece and returns the
 		// number of pieces on its team that are defending it.
 		// numDefenders starts at -1 because the piece that will be moving to
 		// that square doesnt count as a defende
 		int a = 0;
 		if (this.b[x][y].getColor() == color && this.b[x][y].toString().charAt(1) != 'X')
 			a = a - 1;
 		for (int i = 0; i < allMovesDefending().size(); i++) {
 			for (int j = 1; j < allMovesDefending().get(i).size(); j++) {
 				if (((Point) allMovesDefending().get(i).get(j)).getX() == x
 						&& ((Point) allMovesDefending().get(i).get(j)).getY() == y)
 					a++;
 				// if there is a piece at the location then it will add to the
 				// array so we have to check for that
 
 			}
 		}
 
 		return a;
 	}
 
 	public int numDefenderValue(int x, int y) {
 		// num defender value takes in the coordinates of a piece and returns
 		// the added number of the value of the pieces on its team that are
 		// defending it.
 		// useful for seeing if a trade should take place aka if you have a rook
 		// that wants to take a pawn, and that square is defended by
 		// your other rook, and attacked by their knight, you wouldn't want to
 		// trade
 		int a = 0;
 		if (this.b[x][y].getColor() == color && this.b[x][y].toString().charAt(1) == 'K')
 			a = a - 2;
 		if (this.b[x][y].getColor() == color && this.b[x][y].toString().charAt(1) == 'Q')
 			a = a - 9;
 		if (this.b[x][y].getColor() == color && this.b[x][y].toString().charAt(1) == 'R')
 			a = a - 5;
 		if (this.b[x][y].getColor() == color && this.b[x][y].toString().charAt(1) == 'B')
 			a = a - 3;
 		if (this.b[x][y].getColor() == color && this.b[x][y].toString().charAt(1) == 'N')
 			a = a - 3;
 		if (this.b[x][y].getColor() == color && this.b[x][y].toString().charAt(1) == 'P')
 			a = a - 1;
 
 		for (int i = 0; i < allMovesDefending().size(); i++) {
 			for (int j = 1; j < allMovesDefending().get(i).size(); j++) {
 				if (((Point) allMovesDefending().get(i).get(j)).getX() == x
 						&& ((Point) allMovesDefending().get(i).get(j)).getY() == y) {
 					// finds the current location of the piece that is
 					// "defending" the location at i,j
 					if (b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 							.get(i).get(1)).getY()].getColor() == color
 							&& b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 									.get(i).get(1)).getY()].toString().charAt(1) == 'K') {
 						// king is given a value of 2 here because it is not
 						// that valuable in defending a piece cuz it cant move
 						// into check
 
 						a += 2;
 					}
 					if (b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 							.get(i).get(1)).getY()].getColor() == color
 							&& b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 									.get(i).get(1)).getY()].toString().charAt(1) == 'Q') {
 						// queen is 9
 
 						a += 9;
 					}
 					if (b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 							.get(i).get(1)).getY()].getColor() == color
 							&& b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 									.get(i).get(1)).getY()].toString().charAt(1) == 'R') {
 						// rook is 5
 
 						a += 5;
 					}
 					if (b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 							.get(i).get(1)).getY()].getColor() == color
 							&& b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 									.get(i).get(1)).getY()].toString().charAt(1) == 'B') {
 						// bishop is 3
 
 						a += 3;
 					}
 					if (b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 							.get(i).get(1)).getY()].getColor() == color
 							&& b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 									.get(i).get(1)).getY()].toString().charAt(1) == 'N') {
 						// knight is 3
 
 						a += 3;
 					}
 					if (b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 							.get(i).get(1)).getY()].getColor() == color
 							&& b[(int) ((Point) allMovesDefending().get(i).get(1)).getX()][(int) ((Point) allMovesDefending()
 									.get(i).get(1)).getY()].toString().charAt(1) == 'P') {
 						// pawn is 1
 
 						a += 1;
 					}
 				}
 			}
 		}
 
 		return a;
 	}
 
 	public boolean willThisMoveCauseCheckOrEaten(int x, int y, int a, int b)
 	{
 		boolean causeCheck=false;
 		v1Bobby enemy = new v1Bobby(this.getB(), !color);
 		Point oldLocation= new Point(x,y);
 		Point p4=new Point(a,b);
 		boolean possibleMove=false;
 		boolean dontLook= false;
 		if(this.b[a][b].toString().charAt(1) != 'X'){
 			dontLook=true;
 		}
 		if(dontLook==false)
 		{	
 		move(x,y,a,b);
 		enemy.getBoard(this.b);
 		int bob= enemy.numDefenders(a,b);
		if(bob!=0 ||  bob>numDefenders(a,b)) causeCheck=true;
 		if(check()==true) causeCheck=true;
 		move(a,b,x,y);
 		}
 		enemy.getBoard(this.b);
 		
 		return causeCheck;
 	}
 	public void randomMove() {
 		
 		//MAKE ARRAY OF PIECES THAT CAN MOVE RIGHT OFF THE BAT, THEN SEARCH THROUGH THOSE
 		
 		Random r = new Random();
 		// p = 1-35
 		// n = 36-50
 		// b = 50-65
 		// r = 66-80
 		// q = 81-95
 		// k = 96-100
 		// chooses which piece will move
 		boolean isMoveMade = false;
 		while (isMoveMade == false) {
 			int z = r.nextInt(100) + 1;
 			int counter=0;
 			// chooses a random pawn and moves it to a random legal location,
 			// first must check to makes sure there are pawns left that have
 			// legal moves to make
 			boolean isTherePawnsThatCanMakeMoves;
 			isTherePawnsThatCanMakeMoves = false;
 			
 			counter=0;
 			
 			if (z >= 1 && z <= 35) {
 				while(pMoves().size()>counter && isTherePawnsThatCanMakeMoves == false){
 					if (pMoves().get(counter).size() > 2) isTherePawnsThatCanMakeMoves = true;
 					counter++;
 				}
 				if(isTherePawnsThatCanMakeMoves == true){
 				int a = r.nextInt(pMoves().size());
 				while (pMoves().get(a).size() <= 2) {
 					a = r.nextInt(pMoves().size());
 				}
 				int b = r.nextInt(pMoves().get(a).size() - 2) + 2;
 				
 				if(willThisMoveCauseCheckOrEaten((int) ((Point) pMoves().get(a).get(1)).getX(), (int) ((Point) pMoves().get(a).get(1)).getY(),
 						(int) ((Point) pMoves().get(a).get(b)).getX(), (int) ((Point) pMoves().get(a).get(b)).getY())==false)
 				{
 				System.out.println("random pawn move!: " + pMoves().get(a).get(1) + " moves to "
 						+ pMoves().get(a).get(b));
 				move((int) ((Point) pMoves().get(a).get(1)).getX(), (int) ((Point) pMoves().get(a).get(1)).getY(),
 						(int) ((Point) pMoves().get(a).get(b)).getX(), (int) ((Point) pMoves().get(a).get(b)).getY());
 				isMoveMade = true;
 				}
 				}
 			}
 
 			// chooses a random knight and moves it to a random legal location,
 			// first must check to makes sure there are knights left that have
 			// legal moves to make
 			boolean isThereKnightsThatCanMakeMoves;
 			isThereKnightsThatCanMakeMoves = false;
 			counter=0;
 
 			if (z >= 36 && z <= 50) {
 					while(nMoves().size()>counter && isThereKnightsThatCanMakeMoves == false){
 						if (nMoves().get(counter).size() > 2) isThereKnightsThatCanMakeMoves = true;
 						counter++;
 					}
 				if(isThereKnightsThatCanMakeMoves == true){
 				int a = r.nextInt(nMoves().size());
 				while (nMoves().get(a).size() <= 2) {
 					a = r.nextInt(nMoves().size());
 				}
 				int b = r.nextInt(nMoves().get(a).size() - 2) + 2;
 
 				
 				
 					if(willThisMoveCauseCheckOrEaten((int) ((Point) nMoves().get(a).get(1)).getX(), (int) ((Point) nMoves().get(a).get(1)).getY(),
 							(int) ((Point) nMoves().get(a).get(b)).getX(),
 							(int) ((Point) nMoves().get(a).get(b)).getY())==false)
 					{
 					System.out.println("random knight move!: " + nMoves().get(a).get(1) + " moves to "
 							+ nMoves().get(a).get(b));
 					move((int) ((Point) nMoves().get(a).get(1)).getX(), (int) ((Point) nMoves().get(a).get(1)).getY(),
 							(int) ((Point) nMoves().get(a).get(b)).getX(),
 							(int) ((Point) nMoves().get(a).get(b)).getY());
 					isMoveMade = true;
 				}
 				
 			}
 				}
 
 			// chooses a random bishop and moves it to a random legal location,
 			// first must check to makes sure there are bishops left that have
 			// legal moves to make
 			boolean isThereBishopsThatCanMakeMoves;
 			isThereBishopsThatCanMakeMoves = false;
 			
 			counter=0;
 			if (z >= 51 && z <= 65) {
 				while(bMoves().size()>counter && isThereBishopsThatCanMakeMoves == false){
 					if (bMoves().get(counter).size() > 2) isThereBishopsThatCanMakeMoves = true;
 					counter++;
 				}
 			if(isThereBishopsThatCanMakeMoves == true){
 				int a = r.nextInt(bMoves().size());
 				while (bMoves().get(a).size() <= 2) {
 					a = r.nextInt(bMoves().size());
 				}
 				int b = r.nextInt(bMoves().get(a).size() - 2) + 2;
 
 				
 					if(willThisMoveCauseCheckOrEaten((int) ((Point) bMoves().get(a).get(1)).getX(), (int) ((Point) bMoves().get(a).get(1)).getY(),
 							(int) ((Point) bMoves().get(a).get(b)).getX(),
 							(int) ((Point) bMoves().get(a).get(b)).getY())==false)
 					{
 					System.out.println("random bishop move!: " + bMoves().get(a).get(1) + " moves to "
 							+ bMoves().get(a).get(b));
 					move((int) ((Point) bMoves().get(a).get(1)).getX(), (int) ((Point) bMoves().get(a).get(1)).getY(),
 							(int) ((Point) bMoves().get(a).get(b)).getX(),
 							(int) ((Point) bMoves().get(a).get(b)).getY());
 					isMoveMade = true;
 					}
 					
 				}
 			}
 
 			// chooses a random rook and moves it to a random legal location,
 			// first must check to makes sure there are rooks left that have
 			// legal moves to make
 			boolean isThereRooksThatCanMakeMoves;
 			isThereRooksThatCanMakeMoves = false;
 			
 			counter=0;
 			
 			if (z >= 66 && z <= 80) {
 				
 				while(rMoves().size()>counter && isThereRooksThatCanMakeMoves == false){
 					if (rMoves().get(counter).size() > 2) isThereRooksThatCanMakeMoves = true;
 					counter++;
 				}
 			if(isThereRooksThatCanMakeMoves == true){
 				
 				int a = r.nextInt(rMoves().size());
 				while (rMoves().get(a).size() <= 2) {
 					a = r.nextInt(rMoves().size());
 				}
 				int b = r.nextInt(rMoves().get(a).size() - 2) + 2;
 
 				
 					if(willThisMoveCauseCheckOrEaten((int) ((Point) rMoves().get(a).get(1)).getX(), (int) ((Point) rMoves().get(a).get(1)).getY(),
 							(int) ((Point) rMoves().get(a).get(b)).getX(),
 							(int) ((Point) rMoves().get(a).get(b)).getY())==false)
 					{
 					System.out.println("random rook move!: " + rMoves().get(a).get(1) + " moves to "
 							+ rMoves().get(a).get(b));
 					move((int) ((Point) rMoves().get(a).get(1)).getX(), (int) ((Point) rMoves().get(a).get(1)).getY(),
 							(int) ((Point) rMoves().get(a).get(b)).getX(),
 							(int) ((Point) rMoves().get(a).get(b)).getY());
 					isMoveMade = true;
 					}
 					
 				}
 			}
 
 			// chooses a random queen and moves it to a random legal location,
 			// first must check to makes sure there are queens left that have
 			// legal moves to make
 			boolean isThereQueensThatCanMakeMoves;
 			isThereQueensThatCanMakeMoves = false;
 			
 			counter=0;
 			
 			if (z >= 81 && z <= 95) {
 				
 				while(qMoves().size()>counter && isThereQueensThatCanMakeMoves == false){
 					if (qMoves().get(counter).size() > 2) isThereQueensThatCanMakeMoves = true;
 					counter++;
 				}
 			if(isThereQueensThatCanMakeMoves == true){
 				
 				int a = r.nextInt(qMoves().size());
 				while (qMoves().get(a).size() <= 2) {
 					a = r.nextInt(qMoves().size());
 				}
 				int b = r.nextInt(qMoves().get(a).size() - 2) + 2;
 
 				
 					if(willThisMoveCauseCheckOrEaten((int) ((Point) qMoves().get(a).get(1)).getX(), (int) ((Point) qMoves().get(a).get(1)).getY(),
 							(int) ((Point) qMoves().get(a).get(b)).getX(),
 							(int) ((Point) qMoves().get(a).get(b)).getY())==false)
 					{
 					System.out.println("random queen move!: " + qMoves().get(a).get(1) + " moves to "
 							+ qMoves().get(a).get(b));
 					move((int) ((Point) qMoves().get(a).get(1)).getX(), (int) ((Point) qMoves().get(a).get(1)).getY(),
 							(int) ((Point) qMoves().get(a).get(b)).getX(),
 							(int) ((Point) qMoves().get(a).get(b)).getY());
 					isMoveMade = true;
 					}
 					
 				}
 			}
 
 			// chooses a random king and moves it to a random legal location,
 			// first must check to makes sure there are kings left that have
 			// legal moves to make
 		
 			if (z >= 96 && z <= 100) {
 				if(kMoves().size()>2)
 				{
 				int a = r.nextInt(kMoves().size());
 				while (kMoves().get(a).size() <= 2) {
 					a = r.nextInt(kMoves().size());
 				}
 				int b = r.nextInt(kMoves().get(a).size() - 2) + 2;
 				
 					System.out.println("random king move!: " + kMoves().get(a).get(1) + " moves to "
 							+ kMoves().get(a).get(b));
 					move((int) ((Point) kMoves().get(a).get(1)).getX(), (int) ((Point) kMoves().get(a).get(1)).getY(),
 							(int) ((Point) kMoves().get(a).get(b)).getX(),
 							(int) ((Point) kMoves().get(a).get(b)).getY());
 					isMoveMade = true;
 				}
 				
 			}
 		}
 
 	}
 
 	public Point randomKingMove() {
 
 		Random r = new Random();
 		int m = r.nextInt(kMoves().size());
 		while (kMoves().get(m).size() == 2) {
 			m = r.nextInt(kMoves().size());
 		}
 		Point st = new Point((Point) kMoves().get(m).get(1));
 		Random q = new Random();
 		int choose = q.nextInt((kMoves().get(m).size() - 2)) + 2;
 		Point fn = new Point((Point) kMoves().get(m).get(choose));
 		move((int) st.getX(), (int) st.getY(), (int) fn.getX(), (int) fn.getY());
 		return fn;
 
 	}
 
 	public boolean checkmate() {
 		if(this.kMoves().get(0).size()==2 && check()==true && stopCheck()==false) return true;
 		else return false;
 	}
 
 	public boolean check() {
 		if (isThreatened((int) ((Point) kMoves().get(0).get(1)).getX(), (int) ((Point) kMoves().get(0).get(1)).getY())
 				.size() != 0)
 			return true;
 		else
 			return false;
 	}
 	
 	public boolean stopCheck(){
 		int currTurns= numTurns;
 		if(takeIfPossible(kMoves().get(0))!=1)
 		{
 			move(((int) ((Point) kMoves().get(0).get(1)).getX()), ((int) ((Point) kMoves().get(0).get(1)).getY()),
 					((int) ((Point) kMoves().get(0).get(takeIfPossible(kMoves().get(0)))).getX()), ((int) ((Point) kMoves().get(0).get(takeIfPossible(kMoves().get(0)))).getY()));
 			System.out.println("don't check me or i'll eat ya!");
 			numTurns++;
 		}
 			
 		else if(this.kMoves().get(0).size()>=3){
 			move(((int) ((Point) kMoves().get(0).get(1)).getX()), ((int) ((Point) kMoves().get(0).get(1)).getY()),
 					((int) ((Point) kMoves().get(0).get(2)).getX()), ((int) ((Point) kMoves().get(0).get(2)).getY()));
 			System.out.println("King on the run!");
 			numTurns++;
 		}
 		
 		else
 		{
 		v1Bobby enemy = new v1Bobby(this.getB(), !color);
 		System.out.println("here");
 		Point enemyCoord=new Point(-1,-1);
 		Point kingCoord=new Point((int)((Point)kMoves().get(0).get(1)).getX(),(int)((Point)kMoves().get(0).get(1)).getY());
 		//finds loc of enemy attacking king
 		for (int i = 0; i < enemy.allMoves().size(); i++) {
 				if(((Point)enemy.allMoves().get(i).get(takeIfPossible(enemy.allMoves().get(i)))).equals((Point)this.kMoves().get(0).get(1)))
 				{
 					System.out.println("here1");
 					enemyCoord.setLocation(((Point)enemy.allMoves().get(i).get(1)).getX(), ((Point)enemy.allMoves().get(i).get(1)).getY());
 				}
 		}
 		
 		//sees if another one of our pieces can take it if king can't/can't move
 			int quit=-999;
 			for (int r = 0; r < allMoves().size(); r++) {
 			for (int c = 2; c < allMoves().get(r).size(); c++) {		
 				
 				while(quit==-999)
 				{
 					if(((Point)allMoves().get(r).get(c)).equals(enemyCoord)) 
 				{
 						System.out.println("here3");
 						move((int)((Point)allMoves().get(r).get(1)).getX(),(int)((Point)allMoves().get(r).get(1)).getY(),
 						(int)((Point)allMoves().get(r).get(c)).getX(),(int)((Point)allMoves().get(r).get(c)).getY());
 				numTurns++;
 				quit=0;
 				}
 				}
 			}
 		}
 		}
 			
 		if(currTurns==numTurns)
 		{
 			System.out.println("her4e");
 			getOutOfCheck();
 		}
 		
 			
 		
 		//false means no move was made to save the king and we're in check
 				if(currTurns==numTurns) return false;
 				else return true;
 	
 	
 	}
 	
 	public void getOutOfCheck() {
 		// replicate board state
 		v1Bobby a = new v1Bobby(b, color);
 		Point p;
 		int x, y;
 		for (int i = 0; i < a.allMoves().size(); i++) {
 			for (int k = 1; k < a.allMoves().get(i).size(); k++) {
 				x = (int) ((Point) a.allMoves().get(i).get(k)).getX();
 				y = (int) ((Point) a.allMoves().get(i).get(k)).getY();
 				int x1 = (int) ((Point) a.allMoves().get(i).get(1)).getX();
 				int y1 = (int) ((Point) a.allMoves().get(i).get(1)).getY();
 				a.move((int) ((Point) a.allMoves().get(i).get(1)).getX(),
 						(int) ((Point) a.allMoves().get(i).get(1)).getY(),
 						(int) ((Point) a.allMoves().get(i).get(k)).getX(),
 						(int) ((Point) a.allMoves().get(i).get(k)).getY());
 				if (a.check() == false) {
 					move(x1, y1, x, y);
 					numTurns++;
 					break;
 				} else
 					a.move(x, y, x1, y1);
 			}
 		}
 	}
 
 	public ArrayList<ArrayList> allMoves() {
 		ArrayList<ArrayList> a = new ArrayList<ArrayList>();
 		for (int i = 0; i < pMoves().size(); i++) {
 			a.add(pMoves().get(i));
 		}
 		for (int i = 0; i < nMoves().size(); i++) {
 			a.add(nMoves().get(i));
 		}
 		for (int i = 0; i < bMoves().size(); i++) {
 			a.add(bMoves().get(i));
 		}
 		for (int i = 0; i < rMoves().size(); i++) {
 			a.add(rMoves().get(i));
 		}
 		for (int i = 0; i < qMoves().size(); i++) {
 			a.add(qMoves().get(i));
 		}
 		for (int i = 0; i < kMoves().size(); i++) {
 			a.add(kMoves().get(i));
 		}
 		return a;
 	}
 
 	public ArrayList<Point> isThreatened(int d, int e) {
 		ArrayList<Point> a = new ArrayList<Point>();
 		v1Bobby c = new v1Bobby(b, !color);
 		int k;
 		for (int i = 0; i < c.allMoves().size(); i++) {
 			
 			k= takeIfPossible(c.allMoves().get(i));	
 			if (k!=1 && ((Point) c.allMoves().get(i).get(k)).equals(new Point(d, e))) {
 					a.add((Point) c.allMoves().get(i).get(1));
 				
 			}
 
 		}
 		return a;
 	}
 
 	public Piece[][] turn(Piece[][] b) {
 		getBoard(b);
 		int currNumTurns = numTurns;
 
 		if (numTurns < 6) {
 			if (this.numTurns == 0 && this.color == true) {
 				move(4, 6, 4, 4);
 			}
 			if (this.numTurns == 0 && this.color == false) {
 				move(4, 1, 4, 3);
 			}
 			if (this.numTurns == 1 && this.color == true) {
 				move(3, 6, 3, 5);
 			}
 			if (this.numTurns == 1 && this.color == false) {
 				move(3, 1, 3, 2);
 			}
 			if (this.numTurns == 2 && this.color == true) {
 				move(6, 7, 5, 5);
 			}
 			if (this.numTurns == 2 && this.color == false) {
 				move(6, 0, 5, 2);
 			}
 			if (this.numTurns == 3 && this.color == true) {
 				move(6, 6, 6, 5);
 			}
 			if (this.numTurns == 3 && this.color == false) {
 				move(6, 1, 6, 2);
 			}
 			if (this.numTurns == 4 && this.color == true) {
 				move(5, 7, 6, 6);
 			}
 			if (this.numTurns == 4 && this.color == false) {
 				move(5, 0, 6, 1);
 			}
 			if (this.numTurns == 5 && this.color == true) {
 				move(4, 7, 6, 7);
 				move(7, 7, 5, 7);
 			}
 			if (this.numTurns == 5 && this.color == false) {
 				move(4, 0, 6, 0);
 				move(7, 0, 5, 0);
 			}
 
 			this.numTurns++;
 		}
 
 		else {
 
 			if (checkmate() == true) System.out.print("Lose");
 			else if (check() == true) {stopCheck(); if (checkmate() == true) System.out.print("Lose");} 
 			else {
 				long start;
 				start = System.currentTimeMillis();
 
 				newBestPieceToTake();
 				float time = System.currentTimeMillis() - start;
 				time = time / 1000F;
 				System.out.println(time);
 				}
 			if (currNumTurns == numTurns) bestPieceGetOutOfDanger();
 			//then write random so if it moves it doesn't endanger other pieces, also write pawn so it
 			//only moves into danger if its defended
 			if (currNumTurns == numTurns){
 				randomMove();
 				this.numTurns++;
 			}
 		}
 
 		System.out.println("numTurns= " + this.numTurns);
 		return this.b;
 	}
 
 	public ArrayList<ArrayList> kMovesDefending() {
 		ArrayList<ArrayList> d = new ArrayList();
 
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'K') {
 					ArrayList g = new ArrayList();
 					int x = j;
 					int y = k;
 					g.add(b[x][y]);
 					g.add(new Point(x, y));
 					if (x == -1 || y == -1) {
 						return null;
 					} else {
 						for (int a = x - 1; a < x + 2; a++) {
 							for (int b = y - 1; b < y + 2; b++) {
 								if (a > -1 && a < 8 && b > -1 && b < 8) {
 									if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color
 											|| this.b[a][b].getColor() == color) {
 										g.add(new Point(a, b));
 									}
 								}
 							}
 						}
 					}
 					d.add(g);
 				}
 			}
 		}
 
 		return d;
 	}
 
 	public ArrayList<ArrayList> qMovesDefending() {
 		ArrayList<ArrayList> d = new ArrayList<ArrayList>();
 		int x = 0;
 		int y = 0;
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'Q') {
 					ArrayList g = new ArrayList();
 					x = j;
 					y = k;
 					g.add(b[x][y]);
 					g.add(new Point(x, y));
 					if (x == -1 || y == -1) {
 						return null;
 					} else {
 						// checks queen for diagonal down to the right. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						int a = x;
 						int b = y;
 
 						int quit = 0;
 						if (b != 7 && a != 7) // if the queen is on the last row
 												// or furthest right column of
 												// the board no need to check
 												// down and to the right
 						{
 							do {
 								a = a + 1;
 								b = b + 1;
 								if (a == 7)
 									quit = -999;
 								if (b == 7)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color
 										|| this.b[a][b].getColor() == color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks queen for diagonal down to the left. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 7 && a != 0) // if the queen is on the last row
 												// or leftmost column of the
 												// board no need to check down
 												// and to the left
 						{
 							do {
 								a = a - 1;
 								b = b + 1;
 								if (a == 0)
 									quit = -999;
 								if (b == 7)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color
 										|| this.b[a][b].getColor() == color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks queen for diagonal up to the left. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 0 && a != 0) {// if the queen is on the first
 												// row of the board or leftmost
 												// column no need to check up
 												// and to the left
 							do {
 								a = a - 1;
 								b = b - 1;
 								if (a == 0)
 									quit = -999;
 								if (b == 0)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color
 										|| this.b[a][b].getColor() == color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks queen for diagonal up to the right. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 0 && a != 7)
 						// if queen is on the highest row and rightmost column
 						// no need to look up and to the right
 						{
 							do {
 								a = a + 1;
 								b = b - 1;
 								if (a == 7)
 									quit = -999;
 								if (b == 0)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color
 										|| this.b[a][b].getColor() == color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks queen north. quits if adds an opposite color
 						// piece or
 						// reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (b == 0)
 							b = b + 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							b = b - 1;
 							if (b == 0)
 								quit = -999;
 							if ((!(g.contains(new Point(a, b))))
 									&& (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color || this.b[a][b]
 											.getColor() == color))
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks queen south. quits if adds an opposite color
 						// piece or
 						// reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (b == 7)
 							b = b - 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							b = b + 1;
 							if (b == 7)
 								quit = -999;
 							if ((!(g.contains(new Point(a, b))))
 									&& (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color || this.b[a][b]
 											.getColor() == color))
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks queen to the west. quits if adds an opposite
 						// color piece
 						// or reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (a == 7)
 							a = a - 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							a = a + 1;
 							if (a == 7)
 								quit = -999;
 							if ((!(g.contains(new Point(a, b))))
 									&& (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color || this.b[a][b]
 											.getColor() == color))
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks queen to the east. quits if adds an opposite
 						// color piece
 						// or reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (a == 0)
 							a = a + 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							a = a - 1;
 							if (a == 0)
 								quit = -999;
 							if ((!(g.contains(new Point(a, b))))
 									&& (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color || this.b[a][b]
 											.getColor() == color))
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 					}
 					d.add(g);
 				}
 			}
 		}
 
 		return d;
 	}
 
 	public ArrayList<ArrayList> rMovesDefending() {
 		ArrayList<ArrayList> d = new ArrayList<ArrayList>();
 		int x = 0;
 		int y = 0;
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'R') {
 					ArrayList g = new ArrayList();
 					x = j;
 					y = k;
 					g.add(b[x][y]);
 					g.add(new Point(x, y));
 					if (x == -1 || y == -1) {
 						return null;
 					} else {
 						// checks queen rook. quits if adds an opposite color
 						// piece or
 						// reaches end of board or hits our piece
 						int a = x;
 						int b = y;
 						if (b == 0)
 							b = b + 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						int quit = 0;
 						do {
 							b = b - 1;
 							if (b == 0)
 								quit = -999;
 							if (!(g.contains(new Point(a, b)))
 									&& (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color || this.b[a][b]
 											.getColor() == color))
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks rook south. quits if adds an opposite color
 						// piece or
 						// reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (b == 7)
 							b = b - 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							b = b + 1;
 							if (b == 7)
 								quit = -999;
 							if (!(g.contains(new Point(a, b)))
 									&& (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color || this.b[a][b]
 											.getColor() == color))
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks rook to the west. quits if adds an opposite
 						// color piece or
 						// reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (a == 7)
 							a = a - 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							a = a + 1;
 							if (a == 7)
 								quit = -999;
 							if ((!(g.contains(new Point(a, b))))
 									&& (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color || this.b[a][b]
 											.getColor() == color))
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 
 						// checks rook to the east. quits if adds an opposite
 						// color piece or
 						// reaches end of board or hits our piece
 						a = x;
 						b = y;
 						if (a == 0)
 							a = a + 1; // if piece is already on edge of board,
 										// need to
 										// adjust for the do while loop to work
 						quit = 0;
 						do {
 							a = a - 1;
 							if (a == 0)
 								quit = -999;
 							if ((!(g.contains(new Point(a, b))))
 									&& (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color || this.b[a][b]
 											.getColor() == color))
 								g.add(new Point(a, b));
 							if (this.b[a][b].toString().charAt(1) != 'X')
 								quit = -999;
 						} while (quit != -999);
 					}
 					d.add(g);
 				}
 			}
 		}
 
 		return d;
 	}
 
 	public ArrayList<ArrayList> nMovesDefending() {
 		ArrayList<ArrayList> v = new ArrayList<ArrayList>();
 		for (int y = 0; y < 8; y++) {
 			for (int x = 0; x < 8; x++) {
 				if (b[x][y].getColor() == color && b[x][y].toString().charAt(1) == 'N') {
 					ArrayList d = new ArrayList();
 					d.add(b[x][y]);
 					d.add(new Point(x, y));
 					if (x - 2 > -1 && y - 1 > -1)
 						d.add(new Point(x - 2, y - 1));
 					if (x - 1 > -1 && y - 2 > -1)
 						d.add(new Point(x - 1, y - 2));
 
 					if (x - 2 > -1 && y + 1 < 8)
 						d.add(new Point(x - 2, y + 1));
 					if (x - 1 > -1 && y + 2 < 8)
 						d.add(new Point(x - 1, y + 2));
 
 					if (x + 2 < 8 && y + 1 < 8)
 						d.add(new Point(x + 2, y + 1));
 					if (x + 1 < 8 && y + 2 < 8)
 						d.add(new Point(x + 1, y + 2));
 
 					if (x + 1 < 8 && y - 2 > -1)
 						d.add(new Point(x + 1, y - 2));
 					if (x + 2 < 8 && y - 1 > -1)
 						d.add(new Point(x + 2, y - 1));
 					v.add(d);
 				}
 			}
 		}
 		return v;
 	}
 
 	public ArrayList<ArrayList> bMovesDefending() {
 		ArrayList<ArrayList> m = new ArrayList<ArrayList>();
 		int x = 0;
 		int y = 0;
 		for (int j = 0; j < 8; j++) {
 			for (int k = 0; k < 8; k++) {
 				if (b[j][k].getColor() == color && b[j][k].toString().charAt(1) == 'B') {
 					ArrayList g = new ArrayList();
 					x = j;
 					y = k;
 					g.add(b[x][y]);
 					g.add(new Point(x, y));
 					if (x == -1 || y == -1) {
 						return null;
 					} else {
 						// checks bishop for diagonal down to the right. quits
 						// if adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						int a = x;
 						int b = y;
 
 						int quit = 0;
 						if (b != 7 && a != 7) // if the bishop is on the last
 												// row or furthest right column
 												// of the board no need to check
 												// down and to the right
 						{
 							do {
 								a = a + 1;
 								b = b + 1;
 								if (a == 7)
 									quit = -999;
 								if (b == 7)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color
 										|| this.b[a][b].getColor() == color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks queen for diagonal down to the left. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 7 && a != 0) // if the bishop is on the last
 												// row or leftmost column of the
 												// board no need to check down
 												// and to the left
 						{
 							do {
 								a = a - 1;
 								b = b + 1;
 								if (a == 0)
 									quit = -999;
 								if (b == 7)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color
 										|| this.b[a][b].getColor() == color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks bishop for diagonal up to the left. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 0 && a != 0) {// if the bishop is on the first
 												// row of the board or leftmost
 												// column no need to check up
 												// and to the left
 							do {
 								a = a - 1;
 								b = b - 1;
 								if (a == 0)
 									quit = -999;
 								if (b == 0)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color
 										|| this.b[a][b].getColor() == color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 						// checks bishop for diagonal up to the right. quits if
 						// adds an
 						// opposite color piece or reaches end of board or our
 						// piece
 						a = x;
 						b = y;
 
 						quit = 0;
 						if (b != 0 && a != 7)
 						// if bishop is on the highest row and rightmost column
 						// no need to look up and to the right
 						{
 							do {
 								a = a + 1;
 								b = b - 1;
 								if (a == 7)
 									quit = -999;
 								if (b == 0)
 									quit = -999;
 								if (this.b[a][b].toString().charAt(1) == 'X' || this.b[a][b].getColor() != color
 										|| this.b[a][b].getColor() == color)
 									g.add(new Point(a, b));
 								if (this.b[a][b].toString().charAt(1) != 'X')
 									quit = -999;
 							} while (quit != -999);
 						}
 					}
 					m.add(g);
 				}
 			}
 		}
 
 		return m;
 	}
 
 	public ArrayList<ArrayList> pMovesDefending() {
 		ArrayList<ArrayList> v = new ArrayList<ArrayList>();
 		for (int y = 0; y < 8; y++) {
 			for (int x = 0; x < 8; x++) {
 				if (b[x][y].getColor() == color && b[x][y].toString().charAt(1) == 'P') {
 					ArrayList d = new ArrayList();
 					d.add(b[x][y]);
 					d.add(new Point(x, y));
 					if (color == false) {
 						if (y == 1 && b[x][y + 2].toString().charAt(1) == 'X'
 								&& b[x][y + 1].toString().charAt(1) == 'X')
 							d.add(new Point(x, y + 2));
 						if (y + 1 < 8 && b[x][y + 1].toString().charAt(1) == 'X')
 							d.add(new Point(x, y + 1));
 						if (x + 1 < 8 && y + 1 < 8 && this.b[x + 1][y + 1].toString().charAt(1) != 'X')
 							d.add(new Point(x + 1, y + 1));
 						if (x - 1 > -1 && y + 1 < 8 && this.b[x - 1][y + 1].toString().charAt(1) != 'X')
 							d.add(new Point(x - 1, y + 1));
 					}
 
 					if (color == true) {
 
 						if (y == 6 && b[x][y - 2].toString().charAt(1) == 'X'
 								&& b[x][y - 1].toString().charAt(1) == 'X')
 							d.add(new Point(x, y - 2));
 						if (y - 1 > -1 && b[x][y - 1].toString().charAt(1) == 'X')
 							d.add(new Point(x, y - 1));
 						if (x + 1 < 8 && y - 1 > -1 && this.b[x + 1][y - 1].toString().charAt(1) != 'X') {
 
 							d.add(new Point(x + 1, y - 1));
 						}
 						if (x - 1 > -1 && y - 1 > -1 && this.b[x - 1][y - 1].toString().charAt(1) != 'X')
 							d.add(new Point(x - 1, y - 1));
 					}
 					v.add(d);
 				}
 			}
 		}
 		return v;
 	}
 
 	public ArrayList<ArrayList> allMovesDefending() {
 		ArrayList<ArrayList> a = new ArrayList<ArrayList>();
 		for (int i = 0; i < pMovesDefending().size(); i++) {
 			a.add(pMovesDefending().get(i));
 		}
 		for (int i = 0; i < nMovesDefending().size(); i++) {
 			a.add(nMovesDefending().get(i));
 		}
 		for (int i = 0; i < bMovesDefending().size(); i++) {
 			a.add(bMovesDefending().get(i));
 		}
 		for (int i = 0; i < rMovesDefending().size(); i++) {
 			a.add(rMovesDefending().get(i));
 		}
 		for (int i = 0; i < qMovesDefending().size(); i++) {
 			a.add(qMovesDefending().get(i));
 		}
 		for (int i = 0; i < kMovesDefending().size(); i++) {
 			a.add(kMovesDefending().get(i));
 		}
 		return a;
 	}
 
 }
