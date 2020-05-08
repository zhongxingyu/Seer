 package sharedfiles;
 
 import java.util.Random;
 
 public class Board {
 
 	protected Piece[][] arr;
 
 	public Board() {
 		arr = new Piece[8][8];
 		for (int i = 0; i < 8; i++) {
 			for (int a = 2; a < 6; a++) {
 				arr[i][a] = new Blank(true);
 			}
 		}
 		for (int x = 0; x < 8; x++) {
 			arr[x][1] = new Pawn(false);
 			arr[x][6] = new Pawn(true);
 		}
 
 		// black setup
 		arr[0][0] = new Rook(false);
 		arr[7][0] = new Rook(false);
 
 		arr[1][0] = new Knight(false);
 		arr[6][0] = new Knight(false);
 
 		arr[2][0] = new Bishop(false);
 		arr[5][0] = new Bishop(false);
 
 		arr[3][0] = new Queen(false);
 		arr[4][0] = new King(false);
 
 		// white setup
 		arr[0][7] = new Rook(true);
 		arr[7][7] = new Rook(true);
 
 		arr[1][7] = new Knight(true);
 		arr[6][7] = new Knight(true);
 
 		arr[2][7] = new Bishop(true);
 		arr[5][7] = new Bishop(true);
 
 		arr[3][7] = new Queen(true);
 		arr[4][7] = new King(true);
 
 	}
 
 	public Piece[][] getBoardArray() {
 		return arr;
 	}
 
 	public void setBoardArray(Piece[][] pieces) {
 		arr = pieces;
 	}
 
 	public void printBoard() {
 		for (int y = 0; y < 8; y++) {
 
 			for (int x = 0; x < 8; x++) {
 				System.out.print((arr[x][y].toString().equals("WX") ? "  " : arr[x][y].toString()) + " | ");
 			}
 			System.out.println();
 			System.out.println("---------------------------------------");
 
 		}
 
 		return;
 	}
 
 	public static void main(String args[]) {
 		Board a = new Board();
 		a.printBoard();
 		a.Randomize();
 		a.printBoard();
 		a.Randomize();
 		a.printBoard();
 	}
 
 	public void buildBoard(String[] stringRep) {
 		String[] currentLine;
 		for (int i = 0; i < stringRep.length; i++) {
 			currentLine = stringRep[i].split(" . ");
 			for (int j = 0; j < 8; j++) {
 				arr[j][i] = makePiece(currentLine[j]);
 			}
 		}
 	}
 
 	public Piece makePiece(String s) {
 		boolean color = 'W' == s.charAt(0);
 		switch (s.charAt(1)) {
 			case 'X':
 				return new Blank(color);
 			case 'B':
 				return new Bishop(color);
 			case 'R':
 				return new Rook(color);
 			case 'P':
 				return new Pawn(color);
 			case 'Q':
 				return new Queen(color);
 			case 'K':
 				return new King(color);
 			case 'N':
 				return new Knight(color);
 
 			default:
 				return null;
 		}
 	}
 
	public void randomize() {
 		Random r = new Random();
 		Piece[][] temp = new Piece[8][8];
 		for (int i = 0; i < temp.length; i++) {
 			for (int j = 0; j < temp.length; j++) {
 				temp[i][j] = null;
 			}
 		}
 		for (int i = 0; i < arr.length; i++) {
 			for (int j = 0; j < arr.length; j++) {
 				int a = r.nextInt(8);
 				int b = r.nextInt(8);
 				while (temp[a][b] != null) {
 					a = r.nextInt(8);
 					b = r.nextInt(8);
 				}
 				temp[a][b] = arr[i][j];
 			}
 		}
 		arr = temp;
 	}
 
 }
