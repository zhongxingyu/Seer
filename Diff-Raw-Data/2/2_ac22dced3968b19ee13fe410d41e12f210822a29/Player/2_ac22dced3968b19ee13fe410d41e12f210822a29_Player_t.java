 package com.tictactoe.player;
 
 /**
  * The <code>Player</code> class is a must for every player-based game.
  * It holds the player attributes and player-related methods.
  * This class in particular, designed specifically for the Tic Tac Toe
  * implementation has very few important attributes to make use of.
  * Name and Type are a must. Sign of the player is also required
  * (players sign in the boxes in Tic Tac Toe).
  * @author Kenshin Himura
  *
  */
 public class Player
 {
 	/**
 	 * Holds the player's name. Used to provide a minor personalized experience
 	 * and may be used to display/update the High Scores table (just an idea!)
 	 */
 	private String playerName;
 	/**
 	 * Holds the player's type. A String which is initialized to <code>"User"</code>
 	 * or <code>"AI"</code> during construction. Just for the Game to know if it has
 	 * to ask for input or it should ask for the AI to calculate.
 	 */
 	private String playerType;
 	/**
 	 * Holds the sign of the player. Whether he signs as X or O. Used to
 	 * update tables.
 	 */
 	private char playerSign;
 	/**
 	 * Keeps count of the number of players to properly set the name and sign of 
 	 * both the user and the AI player.
 	 */
 	static private int playerCount=1;
 	/**
 	 * Default constructor of <code>Player</code> class. Used for instantiating
 	 * AI players. Reduced complexity as of now, as it generates default names
 	 * automatically. Also sets the AI player's sign to 'X' if first player of
 	 * the game or 'O' if it is the second player.
 	 */
 	public Player()
 	{
 		setPlayerName("DefaultPlayer"+playerCount);
 		setPlayerType("AI");
 		if((playerCount%2)==1)
 			setPlayerSign('X');
 		else
 			setPlayerSign('O');
 		playerCount++;
 	}
 	/**
 	 * Constructor of the <code>Player</code> class used to instantiate a Player
	 * of type "User" with a specified name. Also sets the player's sign.
 	 * Currently only one human player per game can be created.
 	 * @param playerName Name of the player to be created.
 	 */
 	public Player(String playerName)
 	{
 		setPlayerName(playerName);
 		setPlayerType("User");
 		if((playerCount%2)==1)
 			setPlayerSign('X');
 		else
 			setPlayerSign('O');
 		playerCount++;
 	}
 	/**
 	 * Generic getter method to obtain the player's name. Being a private variable,it has
 	 * to be accessed by a public getter method.
 	 * @return Player's name
 	 */
 	public String getPlayerName()
 	{
 		return playerName;
 	}
 	/**
 	 * Generic setter method used to set the player name. Used primarily (only) during
 	 * construction in this program, although it can be used elsewhere and is defined
 	 * merely as good programming practice.
 	 * @param playerName Name of the Player
 	 */
 	public void setPlayerName(String playerName)
 	{
 		this.playerName = playerName;
 	}
 	/**
 	 * Generic getter method to obtain the player's type. Being a private variable,it has
 	 * to be accessed by a public getter method.
 	 * @return <code>"AI"</code> if player is AI, <code>"User"</code> if not
 	 */
 	public String getPlayerType()
 	{
 		return playerType;
 	}
 	/**
 	 * Generic setter method used to set the player type. Used primarily (only) during
 	 * construction in this program, although it can be used elsewhere and is defined
 	 * merely as good programming practice.
 	 * @param playerType Type of the Player
 	 */
 	public void setPlayerType(String playerType)
 	{
 		this.playerType = playerType;
 	}
 	/**
 	 * Generic getter method to obtain the player's sign. Being a private variable,it has
 	 * to be accessed by a public getter method.
 	 * @return Player's Sign as a character
 	 */
 	public char getPlayerSign()
 	{
 		return playerSign;
 	}
 	/**
 	 * Generic setter method used to set the player sign. Used primarily (only) during
 	 * construction in this program, although it can be used elsewhere and is defined
 	 * merely as good programming practice.
 	 * @param playerSign Sign of the Player
 	 */
 	public void setPlayerSign(char playerSign)
 	{
 		this.playerSign = playerSign;
 	}
 }
