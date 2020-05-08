 package edu.ycp.cs481.ycpgames;
 
 import android.util.Log;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 /**
  * Created by brian on 10/4/13.
  * <p/>
  * AI adapted from:
  * http://www3.ntu.edu.sg/home/ehchua/programming/java/JavaGame_TicTacToe_AI.html
  */
 public class TicTacToeAI extends Player {
 	private static final String TAG = "YCPGamesTicTacToeAI";
 	private int difficulty;
 	Random rand;
 	int[][] grid;
 	/*
 	 * lookup table of preferred moves
 	 */
 	private int[][] preferredMoves = {
 			{1, 1}, {0, 2}, {0, 0},
 			{2, 2}, {2, 0}, {0, 1},
 			{1, 2}, {1, 0}, {2, 1}};
 
 	public TicTacToeAI(int num) {
 		super(num);
 		difficulty = Settings.getInstance().getDifficulty();//get difficulty from settings
 		rand = new Random();
 		setHumanPlayer(false);
 	}
 
 	@Override
 	public int[] makeMove(int[][] g) {
 		int[] move = {0, 0};
 		grid = g;
		switch (difficulty) {
 			case 0://difficulty low (very very low)
 				move = randMove();
                 Log.d(TAG,"randMove() = "+move[0]+ " "+move[1]);
                 break;
 			case 1://difficulty medium (prefers some moves over others, but will make no attempt to
 				// block player 1 win
 				move = lookupTable();
 				break;
 			case 2://difficulty high
 				int[] result = minimax(4, playerNum);
 				move[0] = result[1];
 				move[1] = result[2];
 				break;
 			default://error
 				move[0] = -1;
 				move[1] = -1;
 				break;
 		}
 
 
 		return move;
 	}
 
 	/*
 	 * Helper method to come up with a random move
 	 * used for lowest difficulty setting
 	 */
 	private int[] randMove() {
 		int[] move;
         move = new int[]{-1, -1};
         do {
 			move[0] = rand.nextInt(3);
 
 			move[1] = rand.nextInt(3);
 			Log.d(TAG, "Loop " + move[0] + " " + move[1]);
 		} while (isMoveInvalid(move));
 
 		return move;
 	}
 
 	/*
 	 * helper method to ensure that a move is valid
 	 */
 	private boolean isMoveInvalid(int[] move) {
         return grid[move[0]][move[1]] != 0;
 
 	}
 
 	private int[] lookupTable() {
 		for (int[] move : preferredMoves) {
 			if (grid[move[0]][move[1]] == 0) {
 				return move;
 			}
 		}
 		//no empty spaces
 		return null;
 	}
 
 
 	/**
 	 * Recursive  minimax method for finding best move
 	 *
 	 * @param depth  level in the tree
 	 * @param player active player
 	 * @return array of best score, best x position, best y position
 	 */
 	private int[] minimax(int depth, int player) {
 		//Generate all possible moves
 		List<int[]> moves = generateMoves();
 		//set goal
 		int bestScore;
 		if (player == playerNum) {
 			bestScore = Integer.MIN_VALUE;
 		} else {
 			bestScore = Integer.MAX_VALUE;
 		}
 		int currentScore;
 		int bestX = -1;
 		int bestY = -1;
 
 		if (moves.isEmpty() || depth == 0) {
 			//either game over, or depth reached
 			bestScore = evaluate();
 		} else {
 			//evaluate branches
 			for (int[] move : moves) {
 				//try move
 				grid[move[0]][move[1]] = player;
				if (player == playerNum) {
 					//here is the part where i wished i had implemented player number better...
 					//recursive call
 					if (player == 1) {
 						currentScore = minimax(depth - 1, 2)[0];
 					} else {
 						currentScore = minimax(depth - 1, 1)[0];
 					}
 					//if these has a better score then use it
 					if (currentScore > bestScore) {
 						bestScore = currentScore;
 						bestX = move[0];
 						bestY = move[1];
 					}
 				} else {
 					//recursive call
 					if (player == 1) {
 						currentScore = minimax(depth - 1, 2)[0];
 					} else {
 						currentScore = minimax(depth - 1, 1)[0];
 					}
 					//if these has a better score then use it
 					if (currentScore < bestScore) {
 						bestScore = currentScore;
 						bestX = move[0];
 						bestY = move[1];
 					}
 				}
 				//undo move
 				grid[move[0]][move[1]] = 0;
 			}
 		}
 
 
 		return new int[]{bestScore, bestX, bestY};
 	}
 
 	/**
 	 * @return list of available moves
 	 */
 	private List<int[]> generateMoves() {
 		List<int[]> moves = new ArrayList<int[]>();
 
 		//find empty cells and add to list
 		for (int x = 0; x <= 2; x++) {
 			for (int y = 0; y <= 2; y++) {
 				if (grid[x][y] == 0) {
 					moves.add(new int[]{x, y});
 				}
 			}
 		}
 		return moves;
 	}
 
 	/**
 	 * heuristic eval function for the current board state
 	 *
 	 * @return:    evaluated score
 	 *
 	 * +100, +10, +1 for EACH 3-, 2-, 1-in-a-line for computer.
 	 * -100, -10, -1 for EACH 3-, 2-, 1-in-a-line for opponent.
 	 * 0 otherwise
 	 */
 	private int evaluate() {
 		int score = 0;
 		//evaluate and sum score for each possible line
 		score += evaluateLine(0, 0, 0, 1, 0, 2);
 		score += evaluateLine(1, 0, 1, 1, 1, 2);
 		score += evaluateLine(2, 0, 2, 1, 2, 2);
 		score += evaluateLine(0, 0, 1, 0, 2, 0);
 		score += evaluateLine(0, 1, 1, 1, 2, 1);
 		score += evaluateLine(0, 2, 1, 2, 2, 2);
 		score += evaluateLine(0, 0, 1, 1, 2, 2);
 		score += evaluateLine(0, 2, 1, 1, 2, 0);
 		return score;
 	}
 
 	/*
 	 * heuristic eval function
 	 * returns:
 	 * +100, +10, +1 for 3-, 2-, 1-in-a-line for computer.
 	 * -100, -10, -1 for 3-, 2-, 1-in-a-line for opponent.
 	 * 0 otherwise
 	 */
 	private int evaluateLine(int x1, int y1, int x2, int y2, int x3, int y3) {
 		int score = 0;
 
 		// checking first cell
 		if (grid[x1][y1] == playerNum) { //if piece is mine
 			score = 1;
 		} else if (grid[x1][y1] != 0) { //if piece is opponent
 			score = -1;
 		}
 
 		//checking second cell
 		if (grid[x2][y2] == playerNum) { //if piece is mine
 			if (score == 1) { //first cell is my piece
 				score = 10;
 			} else if (score == -1) { // first cell is player 1 piece
 				return 0;
 			} else { //first cell is empty;
 				score = 1;
 			}
 		} else if (grid[x2][y2] != 0) { //if piece is opponent
 			if (score == -1) {//first cell is player 1
 				score = -10;
 			} else if (score == 1) { //first cell is my piece
 				return 0; //blocked!
 			} else { //first cell is empty
 				score = -1;
 			}
 		}
 
 		//checking third cell
 		if (grid[x3][y3] == playerNum) { //if piece is mine
 			if (score > 0) {//cell 1 and/or 2 are my piece
				return (score * 10);
 			} else if (score < 0) { //cell 1 and/or 2 are player 1
 				return 0;
 			} else { //cells 1/2 are empty
 				return 1;
 			}
 		} else if (grid[x3][y3] != 0) { // if piece is opponent
 			if (score < 0) { //cell 1 and/or 2 are player 1
				return (score * 10);
 			} else if (score > 1) { //cell 1 and/or 2 are my pieces
 				return 0;
 			} else { // cells 1/2 are empty
 				return -1;
 			}
 		}
 
 		return score;
 	}
 }
