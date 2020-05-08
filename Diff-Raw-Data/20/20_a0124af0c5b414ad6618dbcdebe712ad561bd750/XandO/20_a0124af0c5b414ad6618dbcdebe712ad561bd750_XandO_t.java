 package homework;
 
 import java.util.Random;
 import java.util.Scanner;
 
 public class XandO {
 	public static String board[][];
 	public static boolean firstPlayerTurn;
 	public static String firstPlayerType;
 	public static String computerType;
 	public static boolean draw;
 	public static int counter;
 
 	public static void main(String[] args) {
 		initArgs();
 		while (!checkWin()) {
 			if (firstPlayerTurn)
 				playerMove();
 			else
 				computerMove();
 			showBoard();
 		}
 		showResult();
 	}
 	public static void test() {
 		board[2][1] = computerType;
 		board[0][0] = computerType;
 		firstPlayerTurn = false;
 	}
 	public static void initArgs() {
 		board = new String[3][3];
 		for (int i = 0; i < 3; i++)
 			for (int j = 0; j < 3; j++)
 				board[i][j] = " ";
 		Random rn = new Random();
 		firstPlayerTurn = rn.nextBoolean();
 		System.out.println("Enter your type (X or O):");
 		Scanner sc = new Scanner(System.in);
 		firstPlayerType = " ";
 		do {
 			firstPlayerType = sc.nextLine().toUpperCase();
 		} while (!firstPlayerType.equals("X") && !firstPlayerType.equals("O"));
 		computerType = firstPlayerType.equals("X") ? "O" : "X";
 		draw = false;
 		counter = 0;
 	}
 
 	public static void showBoard() {
 		System.out.println();
 		for (String[] x : board) {
 			System.out.print("|");
 			for (String y : x) {
 				System.out.print(y + "|");
 			}
 			System.out.println();
 		}
 	}
 
 	public static void playerMove() {
 		boolean match = false;
 		Scanner sc = new Scanner(System.in);
 		int x = 0, y = 0;
 		while (!match) {
 			System.out.println("Enter coordinates where to make move(x,y):");
			x = scanCoordinates(sc);
			y = scanCoordinates(sc);
		//	x = sc.hasNextInt() ? sc.nextInt() : -1;
		//	y = sc.hasNextInt() ? sc.nextInt() : -1;
 			if (x > 0 && x < 4 && y > 0 && y < 4) {
 				if (board[x - 1][y - 1].equals(" ")) {
 					board[x - 1][y - 1] = firstPlayerType;
 					match = true;
 				} else
 					System.out.println("This place is already used! Try again :)");
 			} else {
				System.out.println("Invalid input! Try again!");;					
 			}
 
 		}
 		firstPlayerTurn = false;
 	}
	
	public static int scanCoordinates(Scanner sc){
		if (sc.hasNextInt()){
			return sc.nextInt();
		} else {
			sc.next();
			return -1;
		}		
	}
 
 	public static void computerMove() {
 		System.out.println("Computer move:");
 		firstPlayerTurn = true;
 		
 		if (computerLogic(computerType))
 			return;
 		if (computerLogic(firstPlayerType))
 			return;
 		
 		
 		boolean match = false;
 		if (board[1][1].equals(" ")) {
 			board[1][1] = computerType;
 			return;
 		}
 		
 		// dummy random logic :)
 		Random rn = new Random();
 		int i = 0, j = 0;
 		while (!match) {
 			i = rn.nextInt(3);
 			j = rn.nextInt(3);
 			if (board[i][j].equals(" ")) {
 				board[i][j] = computerType;
 				return;
 			}
 		}
 	}
 	public static boolean computerLogic(String type) {
 		for (int i = 0; i < 3; i++) {
 			for (int j = 0; j < 3; j++){
 				// Horizontal lines
 				if (board[i][j].equals(" ")	&& 
 					( (board[i][1].equalsIgnoreCase(type) && board[i][2].equalsIgnoreCase(type) && j==0) ||
 					(board[i][0].equalsIgnoreCase(type) && board[i][2].equalsIgnoreCase(type) && j==1) ||
 					(board[i][0].equalsIgnoreCase(type) && board[i][1].equalsIgnoreCase(type) && j==2) ) ) {
 						System.out.println("i =" + i + "; j = " + j);
 						board[i][j] = computerType;
 						return true;
 				}
 				// Vertical lines
 				if (board[j][i].equals(" ")	&& 
 						( (board[1][i].equalsIgnoreCase(type) && board[2][i].equalsIgnoreCase(type) && j==0) ||
 						(board[0][i].equalsIgnoreCase(type) && board[2][i].equalsIgnoreCase(type) && j==1) ||
 						(board[0][i].equalsIgnoreCase(type) && board[1][i].equalsIgnoreCase(type) && j==2) ) ) {
 							System.out.println("j = " + j +"; i =" + i);
 							board[j][i] = computerType;
 							return true;
 					}
 			}
 			// first diagonal
 			if (board[i][i].equals(" ") &&
 				((board[0][0].equalsIgnoreCase(type) && board[1][1].equalsIgnoreCase(type) && i==2) ||
 				(board[1][1].equalsIgnoreCase(type) && board[2][2].equalsIgnoreCase(type) && i==0) ||
 				(board[0][0].equalsIgnoreCase(type) && board[2][2].equalsIgnoreCase(type) && i==1)) ) {
 					System.out.println("i =" + i);
 					board[i][i] = computerType;
 					return true;
 			}
 			// second diagonal
 			for (int j = 2; j >= 0; j--){
 				if (board[i][j].equals(" ") &&
 					((board[1][1].equalsIgnoreCase(type) && board[0][2].equalsIgnoreCase(type) && i==2 && j==0) ||
 					(board[0][2].equalsIgnoreCase(type) && board[2][0].equalsIgnoreCase(type) && j==1 && i==1) ||
 					(board[1][1].equalsIgnoreCase(type) && board[2][0].equalsIgnoreCase(type) && j==2)) ) {
 						System.out.println("2i =" + i + "; j = " + j);
 						board[i][j] = computerType;
 						return true;
 				}
 			}
 		}
 		return false;
 	}
 	public static boolean checkWin() {
 		counter++;
 		for (int i = 0; i < 3; i++) {
 			if (!board[i][0].equals(" ")
 					&& board[i][0].equalsIgnoreCase(board[i][1])
 					&& board[i][1].equalsIgnoreCase(board[i][2])) {
 				return true;
 			}
 			if (!board[0][i].equals(" ")
 					&& board[0][i].equalsIgnoreCase(board[1][i])
 					&& board[1][i].equalsIgnoreCase(board[2][i])) {
 				return true;
 			}
 		}
 		if (!board[0][0].equals(" ")
 				&& board[0][0].equalsIgnoreCase(board[1][1])
 				&& board[1][1].equalsIgnoreCase(board[2][2])) {
 			return true;
 		}
 		if (!board[0][2].equals(" ")
 				&& board[0][2].equalsIgnoreCase(board[1][1])
 				&& board[1][1].equalsIgnoreCase(board[2][0])) {
 			return true;
 		}
 		draw = counter == 10 ? true : false;
 		return draw;
 	}
 
 	public static void showResult() {
 		if (draw) {
 			System.out.println("Draw!");
 		} else if (firstPlayerTurn) {
 			System.out.println("Computer wins!");
 		} else {
 			System.out.println("Player wins!");
 		}
 	}
 }
 
 /*
  Enter your type (X or O):
 X
 Enter coordinates where to make move(x,y):
 2 2
 
 | | | |
 | |X| |
 | | | |
 Computer move:
 
 | | | |
 | |X| |
 |O| | |
 Enter coordinates where to make move(x,y):
 3 1
 This place is already used! Try again :)
 Enter coordinates where to make move(x,y):
 1 3
 
 | | |X|
 | |X| |
 |O| | |
 Computer move:
 
 |O| |X|
 | |X| |
 |O| | |
 Enter coordinates where to make move(x,y):
 2 1
 
 |O| |X|
 |X|X| |
 |O| | |
 Computer move:
 
 |O|O|X|
 |X|X| |
 |O| | |
 Enter coordinates where to make move(x,y):
 2 3
 
 |O|O|X|
 |X|X|X|
 |O| | |
 Player wins!
 
 
 ==============================================================
 Enter your type (X or O):
 X
 Computer move:
 
 | | | |
 | |O| |
 | | | |
 Enter coordinates where to make move(x,y):
 1 1
 
 |X| | |
 | |O| |
 | | | |
 Computer move:
 
 |X| | |
 | |O| |
 | | |O|
 Enter coordinates where to make move(x,y):
 1 2
 
 |X|X| |
 | |O| |
 | | |O|
 Computer move:
 i =0; j = 2
 
 |X|X|O|
 | |O| |
 | | |O|
 Enter coordinates where to make move(x,y):
 2 3
 
 |X|X|O|
 | |O|X|
 | | |O|
 Computer move:
 2i =2; j = 0
 
 |X|X|O|
 | |O|X|
 |O| |O|
 Computer wins!
 
  */
