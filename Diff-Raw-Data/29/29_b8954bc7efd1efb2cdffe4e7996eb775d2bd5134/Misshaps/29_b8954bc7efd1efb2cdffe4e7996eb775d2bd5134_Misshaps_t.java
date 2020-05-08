 package pxc.puzzles;
 
 import pxc.util.polyominos.Board;
 import pxc.util.polyominos.Polyomino;
 
 public class Misshaps {
 	
 	private static final int MAX_ACROSS = 5;
 	private static final int MAX_DOWN = 5;
 	private static final int POLYOMINO_LEN = 5;
 	private static final int POLYOMINO_SIZE = 6;
 
 	private static Board board;
 
	public static void main(String[] args) throws CloneNotSupportedException {
 		
 		// Try different rectangle sizes based on number of polyominoes across and down
 		for (int num_across = 1; num_across < MAX_ACROSS; num_across++) {
 			for (int num_down = 1; num_down < MAX_DOWN; num_down++) {
 				int width = POLYOMINO_LEN * num_across;
 				int height = POLYOMINO_LEN * num_down + 2;
 				int area = width * height;
 				
 				// If divisible by the polyomino size, we have a possible rectangle, so start placing...
 				if (area % POLYOMINO_SIZE == 0) {
 					System.out.println("Trying rectangle of size " + num_across + "x" + num_down);
 					
 					Polyomino hexomino1 = new Polyomino('a',"xxxxx.x...", 2, 5, false, false);
					@SuppressWarnings("unused")
                    Polyomino hexomino2 = new Polyomino('b',"xxxxx.x...", 2, 5, Polyomino.Orientation.flip90, false, false);
 					board = new Board(width, height);
 					
 					
 					board = hexomino1.placeAt(board, 0, 0);
 					//board = hexomino2.placeAt(board, 1, 0);
 					
					board.printBoard();
 				}
 			}
 		}
 		
 
 	}
 
 }
