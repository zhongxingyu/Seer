 package battlechallenge.visual;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 
 import battlechallenge.ActionResult;
 import battlechallenge.ActionResult.ShotResult;
 import battlechallenge.Coordinate;
 import battlechallenge.server.ServerPlayer;
 import battlechallenge.ship.Ship;
 
 /**
  * The Class BoardExporter.
  */
 public class BoardExporter {
 
 	/** The DEFAUL t_ filename. */
 	public static String DEFAULT_FILENAME;
 
 	static {
 		// FIXME: relative path assuming base dir is in bin directory
 		DEFAULT_FILENAME = "../boards/curr.out";
 	}
 	
 	/**
 	 * Export boards to default filename.
 	 *
 	 * @param p1 the first player
 	 * @param p2 the second player
 	 * @param width the board width
 	 * @param height the board height
 	 */
 	public static void exportBoards(ServerPlayer p1, ServerPlayer p2, int width, int height) {
 		exportBoards(DEFAULT_FILENAME, p1, p2, width, height);
 	}
 	
 	/**
 	 * Export boards.
 	 *
 	 * @param filename the ouput filename
 	 * @param p1 the first player
 	 * @param p2 the second player
 	 * @param width the board width
 	 * @param height the board height
 	 */
 	public static void exportBoards(String filename, ServerPlayer p1, ServerPlayer p2, int width, int height) {
 		char[][] b1 = getBoard(p1.getShips(), p2.getActionLog(), width, height);
 		char[][] b2 = getBoard(p2.getShips(), p1.getActionLog(), width, height);
 		exportBoards(filename, b1, b2, p1.getName(), p2.getName());
 	}
 	
 	/**
 	 * Gets the board as a 2D array of chars. The characters stand for:
 	 * 
 	 *    - Ocean: '.'
 	 *    - Boat:  'b'
 	 *    - Hit:   'O'
 	 *    - Sunk:  'S'
 	 *    - Miss:  'X'
 	 *
 	 * @param ships the ships
 	 * @param actionResults the action results
 	 * @param width the board width
 	 * @param height the board height
 	 * @return the board
 	 */
 	public static char[][] getBoard(List<Ship> ships,
 			List<ActionResult> actionResults, int width, int height) {
 		char[][] board = new char[height][width];
 		for (int i=0;i<board.length;i++)
 			for(int j=0;j<board[i].length;j++)
 				board[i][j] = 'l';
 		for (Ship s : ships) {
 			if (s.isSunken()) {
 				for (String c : s.getCoordinateStrings())
 					fillSpot(board, 'S', c);
 			} else {
 				for (String c : s.getCoordinateStrings())
					fillSpot(board, (char)('0'+s.getHealth()), c);
 			}
 		}
 		return board;
 	}
 
 	/**
 	 * Fill spot.
 	 *
 	 * @param board the board
 	 * @param c the c
 	 * @param coord the coord
 	 */
 	private static void fillSpot(char[][] board, char c, Coordinate coord) {
 		fillSpot(board,c, coord.getRow(), coord.getCol());
 	}
 	
 	/**
 	 * Fill spot.
 	 *
 	 * @param board the board
 	 * @param c the c
 	 * @param coord the coord
 	 */
 	private static void fillSpot(char[][] board, char c, String coord) {
 		String[] arr = coord.split(",");
 		fillSpot(board,c, Integer.parseInt(arr[0]), Integer.parseInt(arr[1]));
 	}
 	
 	/**
 	 * Fill spot.
 	 *
 	 * @param board the board
 	 * @param c the c
 	 * @param row the row
 	 * @param col the col
 	 */
 	private static void fillSpot(char[][] board, char c, int row, int col) {
 		board[row][col] = c;
 	}
 
 	/**
 	 * Export board for a single player with default filename.
 	 *
 	 * @param ships the ships
 	 * @param actionResults the action results
 	 * @param width the width
 	 * @param height the height
 	 * @return true, if successful
 	 * @deprecated Use exportBoards
 	 */
 	public static boolean exportBoard(List<Ship> ships,
 			List<ActionResult> actionResults, int width, int height) {
 		return exportBoard("", ships, actionResults, width, height);
 	}
 
 	/**
 	 * Export board for a single player.
 	 *
 	 * @param filename the filename
 	 * @param ships the ships
 	 * @param actionResults the action results
 	 * @param width the width
 	 * @param height the height
 	 * @return true, if successful
 	 * @deprecated Use exportBoards
 	 */
 	public static boolean exportBoard(String filename, List<Ship> ships,
 			List<ActionResult> actionResults, int width, int height) {
 		try {
 			BufferedWriter bf = new BufferedWriter(new FileWriter(filename));
 			char[][] board = getBoard(ships, actionResults, width, height);
 			for (int i=0;i<board.length;i++) {
 				for(int j=0;j<board[i].length;j++) {
 					bf.append(board[i][j]);
 				}
 				bf.newLine();
 			}
 			bf.close();
 			return true;
 		} catch (IOException e) { /* Ignore exceptions and return false */}
 		return false;
 	}
 	
 
 
 	/**
 	 * Writes the boards to a file.
 	 *
 	 * @param filename the filename
 	 * @param p1 the p1
 	 * @param p2 the p2
 	 * @param n1 the n1
 	 * @param n2 the n2
 	 * @return true, if successful
 	 */
 	public static boolean exportBoards(String filename, char[][] p1, char[][] p2, String n1, String n2) {
 		try {
 			File f = new File(filename);
 			BufferedWriter bf = new BufferedWriter(new FileWriter(f));
 			bf.append(p1.length+"").append(",").append(p1[0].length+"");
 			bf.append(",").append(n1);
 			bf.newLine();
 			for (int i=0;i<p1.length;i++) {
 				for(int j=0;j<p1[i].length;j++) {
 					bf.append(p1[i][j]);
 				}
 				bf.newLine();
 			}
 			bf.append(p2.length+"").append(",").append(p2[0].length+"");
 			bf.append(",").append(n2);
 			bf.newLine();
 			for (int i=0;i<p2.length;i++) {
 				for(int j=0;j<p2[i].length;j++) {
 					bf.append(p2[i][j]);
 				}
 				bf.newLine();
 			}
 			bf.close();
 			return true;
 		} catch (IOException e) { e.printStackTrace();/* Ignore exceptions and return false */}
 		return false;
 	}
 }
