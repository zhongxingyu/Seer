 package test1;
 
 import java.io.Serializable;
 
 import javax.swing.JOptionPane;
 
 /**
  * 
  * @author yamilasusta
  *
  */
 public class Status implements Serializable{
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -3811193190303251896L;
 	/**
 	 * Default constructor
 	 * @param player1 Player 1 name.
 	 * @param player2 Player 2 name.
 	 */
 	public Status(String player1, String player2) {
 		this.player1 = player1;
 		this.player2 = player2;
 		current = player1;
 		movesplayer1 = 0;
 		movesplayer2 = 0;
 		logger = "";
 		turn = 1;
 		JOptionPane.showMessageDialog(null, current + " start the game!");
 	}
 
 	/**
 	 * 
 	 * @return The amount of moves made by player 1
 	 */
 	public int totalPlayer1() {
 		return movesplayer1;
 	}
 
 	/**
 	 * 
 	 * @return The amount of moves made by player 2
 	 */
 	public int totalPlayer2() {
 		return movesplayer2;
 	}
 
 	/**
 	 * 
 	 * @return Current turn of the game.
 	 */
 	public String getStatus() {
 		return current;
 	}
 
 	/**
 	 * 
 	 * @return Next players turn.
 	 */
 	public String switchStatus() {
 		current = player2;
 		player2 = player1;
 		player1 = current;
		if(!current.equals("Rofongo"))
 			JOptionPane.showMessageDialog(null, current + " is your turn bro");
 		return current;
 	}
 
 	/**
 	 * Increase the amount of moves made by player one
 	 */
 	public void incrementP1() {
 		movesplayer1++;
 	}
 
 	/**
 	 * Increase the amount of moves made by player two
 	 */
 	public void incrementP2() {
 		movesplayer2++;
 	}
 
 	/**
 	 * Provides a game log
 	 * @param location Target made by player
 	 * @param hits Indicator of result
 	 */
 	public void log(String location, boolean hits) {
 		if(turn == 0)
 			System.out.println("Start of the game.");
 		String parse = "";
 		if(hits)
 			parse = "Hits.";
 		else
 			parse = "Misses.";
 		logger = "Turn " + turn + ": " + current + " tried " + location + ". " + parse;
 		System.out.println(logger);
 		turn++;
 
 	}
 
 	//Instance variables
 	private String player1;
 	private String player2;
 	private String current;
 	private int movesplayer1;
 	private int movesplayer2;
 	private String logger;
 	private int turn;
 }
