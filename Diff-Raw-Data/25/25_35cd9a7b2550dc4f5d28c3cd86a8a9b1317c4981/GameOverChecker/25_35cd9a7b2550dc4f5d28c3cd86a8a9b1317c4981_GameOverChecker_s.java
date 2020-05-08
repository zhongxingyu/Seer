 package reversestack.minmax;
 
 public class GameOverChecker {
 
 	private static boolean hor;
 	private static boolean ver;
 	private static boolean diag;
 	private static boolean diagRev;
 
 	public static boolean isGameOver(BoardState state) {
 		int piece = 0;
 
 		for (int x = 0; x < state.size(); x++) {
 			for (int y = 0; y < state.size(); y++) {
 				hor = ver = diag = diagRev = true;
 				piece = state.pieceAt(x, y);
 				if (piece != 0) {
 					for (int a = 1; a < 4; a++) {
 						if (x <= state.size() - 4) {
							if (state.pieceAt(x+a, y) != piece)
 								hor = false;
 						} else
 							hor = false;
 
 						if (y <= state.size() - 4) {
							if (state.pieceAt(x, y+a) != piece)
 								ver = false;
 						} else
 							ver = false;
						
 						if ((x <= state.size() - 4) && (y <= state.size() - 4)) {
 							if (state.pieceAt(x + a, y + a) != piece)
 								diag = false;
 						} else
 							diag = false;
						
						if ((x >=3) && (y <= state.size() - 4)) {
 							if (state.pieceAt(x - a, y + a) != piece)
 								diagRev = false;
 						} else
 							diagRev = false;
 					}
					if (x <= state.size() - 4 && hor)
 						return true;
					if (y <= state.size() - 4 && ver)
 						return true;
					if ((x <= state.size() - 4) && (y <= state.size() - 4) && diag)
 						return true;
					if ((x > 3) && (y <= state.size() - 4) && diagRev)
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 
 	public static boolean isHorizontal() {
 		return hor;
 	}
 
 	public static boolean isVertical() {
 		return ver;
 	}
 
 	public static boolean isDiagonal() {
 		return diag;
 	}
 
 	public static boolean isDiagonalReverse() {
 		return diagRev;
 	}
 
 }
