 package tttai;
 
 import java.util.Arrays;
 
 import tictactoeai.Place;
 
 public class Board {
 	String[] board = {"0","1","2","3",
 			"4","5","6",
 			"7","8","9"};
 	int spacesFree;
 	String winner;
 
 	Board(){
 		spacesFree = 9;
 		winner = null;
 	}
 	/**
 	 * constructor for creating a new board with an updated move
 	 * @param b
 	 * @param space
 	 * @param player
 	 */
 	Board(Board b, int space, String player){
 		this.board = Arrays.copyOf(b.board, 10);
 		this.spacesFree = b.spacesFree-1;
 		this.board[space] = player;
 	}
 	/**
 	 * returns true if the game is over (won or full-tie)
 	 */
 	public boolean isOver(){
 		if(spacesFree==0)
 			return true;
 		if(this.isWon())
 			return true;
 		else
 			return false;
 	}
 	/**
 	 * returns true if the board is won
 	 */
 	public boolean isWon(){
 		if(this.testWon("X")){
 			this.winner = "X";
 			return true;
 		}
 		if(this.testWon("O")){
 			this.winner = "O";
 			return true;
 		}
 		else
 			return false;
 	}
 	/**
 	 * returns true if string is the winner
 	 */
 	public boolean testWon(String s){
		if(board[1].equals(s)&&board[2].equals(s)&&board[3].equals(s))
 			return true;
		if(board[4].equals(s)&&board[5].equals(s)&&board[6].equals(s))
 			return true;
		if(board[7].equals(s)&&board[8].equals(s)&&board[9].equals(s))
 			return true;
		if(board[1].equals(s)&&board[4].equals(s)&&board[7].equals(s))
 			return true;
		if(board[2].equals(s)&&board[5].equals(s)&&board[8].equals(s))
 			return true;
		if(board[3].equals(s)&&board[6].equals(s)&&board[9].equals(s))
 			return true;
		if(board[1].equals(s)&&board[5].equals(s)&&board[9].equals(s))
 			return true;
		if(board[3].equals(s)&&board[5].equals(s)&&board[7].equals(s))
 			return true;
 		return false;
 	}
 	/**
 	 * returns true if two boards are the same
 	 */
 	public boolean equals(Board b){
 		for(int i=1; i<=9; i++){
 			if(b.board[i]!=this.board[i])
 				return false;
 		}
 		return true;
 	}
 	/**
 	 * returns a board with the current board plus the passed move
 	 */
 	public Board makeMove(String player, int square){
 		return new Board(this, square, player);
 	}
 	/**
 	 * toString()
 	 */
 	public String toString(){
 		StringBuilder sb = new StringBuilder();
 		for(int i=1; i<=9; i++){
 			sb.append(board[i]+" ");
 			if(i%3==0){
 				sb.append("\n");
 			}
 		}
 		return sb.toString();
 	}
 }
