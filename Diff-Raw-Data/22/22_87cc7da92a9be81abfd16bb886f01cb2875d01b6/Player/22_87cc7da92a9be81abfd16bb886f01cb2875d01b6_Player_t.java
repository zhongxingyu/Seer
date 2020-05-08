 /**
  * COMP6721
  * Polarized Ladder Game
  * March 2, 2013 
  */
 package polarizedladder;
 
 import java.awt.Point;
 import java.util.Scanner;
 
 /**
  * @author dimitri.tiago
  * 
  */
 public class Player {
 
 	protected int discs;			// number of discs available
 	
 	protected String playerName;	
 	protected char playerToken;
 	
 	Board board;
 	
 	public Player(){
 		
 	}
 	public Player(String playerName, char playerToken, int discs, Board board) {
 		
 		this.playerName 	= playerName;
 		this.discs 			= discs;
 		this.playerToken 	= playerToken;
 		
 		this.board = board;
 	}
 	
 	public boolean setDisc(int i, int j) {
 		
		return board.setPosition(j, i, String.valueOf(playerToken));
 		}
 	
 public Point doPlayerTurn(Player player) {
 		
 		System.out.printf("Please enter your next move %s (ex. 1A):", player.getPlayerName());
 		
 		// command line input scanner
 		Scanner input = new Scanner(System.in);	
 		
 		try
 		{
 			String playerMove 	= input.nextLine();
 			
 			// parse player move
 			String rowTemp 	= playerMove.substring(0,1);	
 			int row 		= Integer.parseInt(rowTemp);
 			char [] colTemp = playerMove.toUpperCase().toCharArray();
 			int col 		= Character.getNumericValue( colTemp[1] ) - 9;
 	
 			Point discCoordinates = new Point(col, row);	
 			return discCoordinates;
 		}
 		catch (ArrayIndexOutOfBoundsException aiob)
 		{
 			System.out.println("Invalid move.");
 			return doPlayerTurn(player);
 		}
 		catch(NumberFormatException nf)
 		{
 			System.out.println("Invalid move.");
 			return doPlayerTurn(player);
 		}
 	}
 	public String getPlayerName() {
 		return playerName;
 	}
 
 	public String getPlayerToken() {
 		return String.valueOf(playerToken);
 	}
 	
 	public int getDiscs() {
 		return discs;
 	}
 }
