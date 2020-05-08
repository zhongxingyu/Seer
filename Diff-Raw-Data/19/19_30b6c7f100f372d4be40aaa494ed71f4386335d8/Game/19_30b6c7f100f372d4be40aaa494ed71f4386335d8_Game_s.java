 package quoridor;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 
 public class Game{
 
 	public static final int num_moves = 100;
 	public static  int num_players = 4;
 
 	public static void main(String[] args) throws IOException {
 
 		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
 		String str = "";
 		System.out.print("How many players would you like? 2-4: ");
 		str = in.readLine();
 
 		num_players = getNumPlayers(str);
 		if (num_players == -1) {
 			System.out.println("There should be between 2 and 4 players. Selecting 2");
 			num_players = 2;
 		}
 		System.out.println(num_players);
 		int turnNumber = 0;
 		int totalTurnNumber = 0;
 		boolean gameOver = false;	
 		String[] moves = new String[num_moves];
 		String[] allMoves = new String[num_moves];
 
 		Player[] players = new Player[num_players+1];
 		for (int i = 1; i <= num_players; i++) {
 			players[i] = new Player(i);
 			moves[i-1] = intToString(players[i].getY()).concat(((Integer)players[i].getX()).toString());
 			allMoves[i-1] = moves[i-1];
 			turnNumber += 1;
 			totalTurnNumber += 1;
 		}
 
 
 		Board b = new Board(players);
 		b.displayBoard();
 		int playerTurn = 0;
 
 
 		while (str != null && gameOver == false) {
 			playerTurn = (turnNumber % num_players) + 1;
 
 			System.out.print("> enter move for player " + playerTurn + " (current position is ["+intToString(players[playerTurn].getY())+","+players[playerTurn].getX()+ "] "+players[playerTurn].getNumWalls()+" walls left):");
 			str = in.readLine();
 			//System.out.println("Str = " + str);
 			// If they're undoing we call the undo function, we then proceed play as normal
 			if (str.equals("u")) {
 				if (turnNumber < 2+num_players) {
 					System.out.println("Error: Not enough moves to undo");
 				} else { 
 					undoMove(b, moves, players, turnNumber);
 					allMoves[totalTurnNumber] = str;
 					totalTurnNumber++;
 					turnNumber -= 2;
 				}
 				System.out.print("> enter move for player " + playerTurn + " (current position is ["+intToString(players[playerTurn].getY())+","+players[playerTurn].getX()+ "] "+players[playerTurn].getNumWalls()+" walls left):");
 				str = in.readLine();
 			}
 
 			while (!isValidInput(str)) {
 				System.out.println("Valid moves are of the form yx[vh]*");
 				System.out.print("> enter move for player " + playerTurn + " (current position is ["+intToString(players[playerTurn].getY())+","+players[playerTurn].getX()+ "] "+players[playerTurn].getNumWalls()+" walls left):");
 				str = in.readLine();
 			}
 			if (processMove(str,players[playerTurn],b, false) == true) {
 				moves[turnNumber] = str;
 				allMoves[totalTurnNumber] = str;
 				turnNumber++;
 				totalTurnNumber++;
 				b.displayBoard();
 			}
 
 			Player check = b.checkWinner(players);
 			if (check != null) {
 				System.out.print("Player " + check.getPlayer() + " wins");
 				gameOver = true;
 			}
 			// if the move was valid increment turn number
 			printMoves(moves, turnNumber);
 
 		}
 	}
 
 	public static int getNumPlayers(String s) {
 		int retVal = -1;
 		if (s.length() == 1) {
 			retVal = (int)(s.toCharArray()[0]-48);
 			if (retVal < 2 || retVal > 4) {
 				retVal = -1;
 			}
 		}
 		return retVal;
 	}
 
 	public static boolean isValidInput(String s) {
 		boolean retVal = true;
 		char[] a = s.toCharArray();
 		if (s.length() < 2 || s.length() > 3) {
 			retVal = false;
 		} else {
 			if (convertCharToInt(a[0]) < 0 || convertCharToInt(a[0]) > 8) {
 				retVal = false;
 			} else if (Character.getNumericValue(a[1]) < 0 || Character.getNumericValue(a[1]) > 8){
 				retVal = false;
 			} else if (s.length() == 3) {
 				if (a[2] != 'h' && a[2] != 'v') {
 					retVal = false;
 				}
 			}
 		}
 		return retVal;
 	}
 
 	//if this returns false a invalid move was given, don't increment turnNumber
 	public static boolean processMove(String s, Player p, Board b, boolean undo) {
 		char[] a = s.toCharArray();
 		boolean r = false;
 		if (convertCharToInt(a[0])<Board.numRows && Character.getNumericValue(a[1])<b.numCols) {
 			if (a.length == 3) {
 				if (p.getNumWalls()>0) {
 					if (undo) {
 						r = b.removeWall(p, Character.getNumericValue(a[1]), convertCharToInt(a[0]), a[2]);
 					} else {
 						r = b.placeWall(p, Character.getNumericValue(a[1]), convertCharToInt(a[0]), a[2]);
 					}
 				} else {
 					System.out.println("out of walls");
 					r=false;
 				}
 			} else {
				r = b.movePlayer(p,Character.getNumericValue(a[1]),convertCharToInt(a[0]));
 			}
 		} else {
 			System.out.println("Invalid Move: square out of range");
 		}
 		//System.out.print(27+"[2J");
 
 		return r;
 	}
 
 	public static void undoMove (Board b, String[] moves, Player[] players, int turnNumber) {
 		// move player's position back to where they were two moves ago
 		// do the same for the other player as well
 		// can only undo if you have more than 1 move
 
 		for (int i = 1; i < players.length; i++) {
 			// if it's a wall remove it
 			if (moves[turnNumber-i].length() == 3) {
 				System.out.println("removing wall of player " + (((turnNumber -i)  % num_players) + 1) + " to " + moves[turnNumber-i]);
 				processMove(moves[turnNumber-i], players[((turnNumber-i) % num_players) + 1], b, true);
 				// else revert back to the previous position
 			} else {
 				System.out.println("reverting player " + ((turnNumber -i)  % num_players + 1) + " to " + moves[turnNumber-i-num_players]);
 				processMove(moves[turnNumber-i-num_players], players[((turnNumber-i) % num_players) +1], b, true);
 			}
 		}
 
 		b.displayBoard();
 
 	}
 
 	//used to convert a-i to its
 	private static int convertCharToInt(char s) {
 		return ((int)s-97);
 	}
 
 	private static String intToString(int i) {
 		i = i + 97;
 		char a[] = Character.toChars(i);
 		return Character.toString(a[0]);
 	}
 
 	public static void printMoves(String[] moves, int turnNumber) {
 		for (int i = 0; i < turnNumber; i++) {
 			System.out.print(moves[i] + " ");
 		}
 		System.out.println();
 	}
 }
