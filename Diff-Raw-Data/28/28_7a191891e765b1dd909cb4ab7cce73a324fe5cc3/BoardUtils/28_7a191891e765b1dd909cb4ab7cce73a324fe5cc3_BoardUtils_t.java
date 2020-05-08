 package utils;
 
 import gridlock.Token;
 
 public class BoardUtils {
 
 	public static int getHTokenSize(Token[][] board, int token, int i, int j) {
 		int size = 1;
 		if (j < 5 && board[i][j + 1].getValue() == token) {
 			for (int h = j + 1; h < board.length
 					&& board[i][h].getValue() == token; h++) {
 				size++;
 			}
 		} else {
 			for (int h = j - 1; h >= 0 && board[i][h].getValue() == token; h--) {
 				size++;
 			}
 		}
 		return size;
 	}
 
 	public static int getVTokenSize(Token[][] board, int token, int i, int j) {
 		int[][] rawBoard = getIntBoard(board);
 		return getVTokenSize(rawBoard, token,i,j);
 	}
 
 	public static int getVTokenSize(int[][] board, int token, int i, int j) {
 		int size = 1;
 		if (i < 5 && board[i + 1][j] == token) {
 			for (int h = i + 1; h < board.length && board[h][j] == token; h++) {
 				size++;
 			}
 		} else {
			for (int h = i - 1; h >= 0 && board[h][j] == token; h--) {
 				size++;
 			}
 		}
 		return size;
 	}
 
 	public static boolean isHorizontal(int token) {
 		if ((token >= 'a' && token <= 'z') || token == '0') {
 			return true;
 		}
 		return false;
 	}
 
 	public static boolean isVertical(int token) {
 		if (token >= '1' && token <= '9') {
 			return true;
 		}
 		return false;
 	}
 
 	public static void copyBoard(int[][] tempBoard, int[][] board2) {
 		for (int i = 0; i < board2.length; i++) {
 			for (int j = 0; j < board2.length; j++) {
 				tempBoard[i][j] = board2[i][j];
 			}
 		}
 	}
 
 	public static int[][] getIntBoard(Token[][] board) {
 		int[][] ans = new int[6][6];
 		for (int i = 0; i < 6; i++) {
 			for (int j = 0; j < 6; j++) {
 				ans[i][j] = board[i][j].getValue();
 			}
 		}
 		return ans;
 
 	}
 
 	public static void copyBoard(Token[][] ans, Token[][] board) {
 		for (int i = 0; i < board.length; i++) {
 			for (int j = 0; j < board.length; j++) {
 				ans[i][j] = board[i][j];
 			}
 		}
 		
 	}
 
 }
