 package run;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 
 import material.Board;
 import material.Piece;
 import material.Square;
 
 public class Main {
 	private static String turn = "White";
 	private static Board board;
 	private static boolean checkmate = false;
 	private static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
 	
 	public Main() {
 		printBoard();
 	}
 	
 	private static Square algebraicToPos(String alpha, int row) {
 		int column = getAlphaConversion(alpha);
 		Square square = new Square(column, row);
 		return square;
 	}
 	
 	private static void checkSquare(String square) {
 		if (validMoveString("a1 " + square)) {
 			Square intSquare = algebraicToPos(square.substring(0,1), Integer.parseInt(square.substring(1)));
 			Piece piece = board.getSquare(intSquare);
 			if (piece != null) {
 				System.out.println(square + " contains a " + piece.getColor() + " " + piece.getType());
 			} else {
 				System.out.println("No piece on " + square);
 			}
 		} else {
 			System.out.println("Please enter a valid square to check.");
 		}
 	}
 	
 	private static boolean checkMoveString(String m) {
 		try {
 			String column = m.substring(0,1).toLowerCase();
 			Integer row = Integer.parseInt(m.substring(1));
 			int columnNum = getAlphaConversion(column);
 			if (columnNum >= 1 && columnNum <= 8 && row >= 1 && row <= 8) {
 				return true;
 			}
 		} catch (Exception e) {}
 		return false;
 	}
 	
 	private static boolean validMoveString(String move) {
 		try {
 			String[] moves = getMoves(move);
 			if (moves.length == 2) {
 				for (String m : moves) {
 					if (!checkMoveString(m)) return false;
 				}
 				return true;
 			}
 		} catch (StringIndexOutOfBoundsException e) {}
 		return false;
 	}
 	
 	private static void init() throws Exception {
 		board = new Board();
 		printBoard();
 	}
 	
 	public static int getAlphaConversion(String s) {
 		if (s.equals("a")) return 1;
 		else if (s.equals("b")) return 2;
 		else if (s.equals("c")) return 3;
 		else if (s.equals("d")) return 4;
 		else if (s.equals("e")) return 5;
 		else if (s.equals("f")) return 6;
 		else if (s.equals("g")) return 7;
 		else if (s.equals("h")) return 8;
 		return 0;
 	}
 	
 	public static String getIntConversion(int i) {
 		if (i == 1) return "a";
 		else if (i == 2) return "b";
 		else if (i == 3) return "c";
 		else if (i == 4) return "d";
 		else if (i == 5) return "e";
 		else if (i == 6) return "f";
 		else if (i == 7) return "g";
 		else if (i == 8) return "h";
 		return "";
 	}
 	
 	private static String[] getMoves(String move) {
 		String[] moves = new String[2];
 		moves[0] = move.replaceAll(" ", "").substring(0,2);
 		moves[1] = move.replaceAll(" ", "").substring(2);
 		return moves;
 	}
 	
 	private static void parseMove(String move) throws IOException, Exception {
 		if (validMoveString(move)) {
 			Piece movePiece;
 			String[] moves = getMoves(move);
 			List<Square> intMoves = new ArrayList<Square>();
 			for (int i = 0; i <=1; i++) {
 				intMoves.add(algebraicToPos(moves[i].substring(0,1).toLowerCase(),Integer.parseInt(moves[i].substring(1))));
 			}
 			if ((movePiece = board.getSquare(intMoves.get(0))) != null) {
 				if (movePiece.getColor().equalsIgnoreCase(turn)) {
 					List<Boolean> moveChecks = movePiece.checkBoard(intMoves.get(1), board);
 					if (moveChecks.get(0)) {
 						System.out.print("\n" + turn + " ---> " + movePiece.getType() + " " + moves[0] + " " + moves[1]);
 						if (moveChecks.get(2)) {
 							checkmate = true;
 							System.out.println(" Checkmate!\n\n" + turn + " WINS!");
 						} else if (moveChecks.get(1)) {
 							System.out.println("  Check!\n");
 						}
 					} else {
 						throw new IOException(moves[0] + " to " + moves[1] + " is not a valid move.  Try again.");
 					}
 				} else {
 					throw new IOException("Piece is wrong color.");
 				}
 			} else {
 				throw new IOException("No piece on square.");
 			}
 		} else {
 			throw new IOException("Typo in move, please check.");
 		}
 		
 	}
 	
 	private static void printAttackedSquares() throws Exception {
 		for (Square sq : board.getAttackedSquares(turn.toLowerCase())) {
 			System.out.println(sq);
 		}
 	}
 	
 	private static void printBoard() {
 		System.out.println(board.toString());
 	}
 	
 	private static void printValidMoves(String command) throws Exception {
		String sq = command.split(" ")[1];
		if (checkMoveString(sq)) {
			Square square = algebraicToPos(sq.toLowerCase().substring(0,1), Integer.parseInt(sq.substring(1)));
			Piece p;
			if ((p = board.getSquare(square)) != null) {
				for (Square s : p.getValidMoves(board, false)) {
					System.out.println(s);
				}
			} else {
				System.out.println("No piece on square.");
			}
		} else {
			System.out.println("Please enter a valid square.");
		}
 	}
 	
 	private static void updateTurn () {
 		if (turn.equals("White")) {
 			turn = "Black";
 		} else {
 			turn = "White";
 		}
 	}
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) throws Exception {
 		init();
 		while (!checkmate) {
 			try {
 				System.out.print("\n" + turn + "'s Turn: ");
 				String move = br.readLine();
 				if (move.contains("check")) {
 					checkSquare(move.split(" ")[1]);
 				} else if (move.contains("exit") || move.contains("quit")) {
 					System.out.println("Goodbye!");
 					System.exit(0);
 				} else if (move.contains("valid")) {
 					printValidMoves(move);
 				} else if (move.contains("attacked")) {
 					printAttackedSquares();
 				} else {
 					parseMove(move);
 					printBoard();
 					updateTurn();
 				}
 			} catch (IOException e) {
 				System.out.println(e.getMessage());
 			} catch (Exception e) {
 				e.printStackTrace();
 				System.exit(1);
 			}
 			
 			
 		}
 		
 	}
 
 }
