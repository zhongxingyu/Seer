 package com.example.wecharades.model;
 
 import java.io.Serializable;
 import java.util.Date;
 
 
 /**
  * This class represents a game.
  * 
  * @author Anton Dahlstrm
  *
  */
 public class Game implements Serializable, Comparable<Game> { //TODO make this class truly Serializable later
 	/**
 	 * auto-generated serialVersionUID
 	 */
 	private static final long serialVersionUID = -3649758045030316767L;
 	private String 	gameId;
 	private Player 	player1;
 	private Player 	player2;
 	private Player 	currentPlayer;
 	private int 	turnNumber;
 	private boolean isFinished;
 	private Date 	lastPlayed;
 
 	/**
 	 * Constructor for a local representation of a game
 	 * @param p1 - player 1
 	 * @param p2 - player 2
 	 * @param currentPlayer - the current player
 	 * @param turn - which turn the game is on
 	 * @param finished - whether the game is finished
 	 * @param lastPlayed - the date of last play
 	 */
 	public Game(String gameId, Player p1, Player p2, Player currentPlayer, int turn, boolean finished, Date lastPlayed){
 		this.gameId = gameId;
 		this.player1 = p1;
 		this.player2 = p2;
 		this.currentPlayer = currentPlayer;
 		this.turnNumber = turn;
 		this.isFinished = finished;
 		this.lastPlayed = lastPlayed;
 	}
 
 	public String getGameId() {
 		return gameId;
 	}
 
 	public Player getPlayer1() {
 		return player1;
 	}
 
 	public Player getPlayer2() {
 		return player2;
 	}
 
 	public Player getOpponent(Player otherPlayer){
 		if(otherPlayer.equals(player1) || otherPlayer.equals(player2)){
 			return (otherPlayer.equals(getPlayer1())) ? getPlayer2() : getPlayer1();
 		}
 		else return null;
 	}
 
 	public Player getCurrentPlayer() {
 		return currentPlayer;
 	}
 
 	public int getTurnNumber() {
 		return turnNumber;
 	}
 
 	public boolean isFinished() {
 		return isFinished;
 	}
 
 	public Date getLastPlayed() {
 		return (Date) lastPlayed.clone();
 	}
 
 	public void setCurrentPlayer(Player currentPlayer) {
 		//Check if the player is part of this turn
 		if(currentPlayer.equals(player1) || currentPlayer.equals(player2))
 			this.currentPlayer = currentPlayer;
 	}
 
 	/**
 	 * Increments the turn number.
 	 */
 	public void incrementTurn() {
 		if(turnNumber == 6){ 
 			isFinished = true;
 		} else{
 			turnNumber ++;
 		}
 	}
 
 	public void setFinished(){
 		isFinished = true;
 	}
 
 	public void setLastPlayed(Date lastPlayed) {
 		this.lastPlayed = lastPlayed;
 	}
 
 	/**
 	 * Returns the hashcode for this game
 	 *  @return the hashcode
 	 */
 	@Override
 	public int hashCode(){
 		return getGameId().hashCode();
 	}
 
 	@Override
 	public boolean equals(Object obj){
 		return obj != null
 				&& obj.getClass().equals(Game.class)
 				&& this.equals((Game) obj);
 	}
 
 	/**
 	 * Two games are considered equal if they have the same id 
 	 * 	
 	 * @param otherGame - the game to compare
 	 * @return if the games are equal
 	 */
 	public boolean equals(Game otherGame){
 		return this.getGameId().equals(otherGame.getGameId());
 	}
 
 	/**
 	 * A method to check if a game can be considered to be ahead of another.
 	 * 	will look on current player, turn and finished state.
 	 * @return - if the game has changed - will return false on null input
 	 */
 	public boolean aheadOf(Game game){
 		return game != null && //Check for null
			(	this.isFinished 
				|| this.getTurnNumber() > game.getTurnNumber()
				|| this.playerTurnValue() > game.getTurnNumber()
 			);
 	}
 	/**
 	 * This method will give a turn value for this player.
 	 * 	There are two levels - high and low. This will be calculated from
 	 * 	the current turn number and player
 	 */
 	private int playerTurnValue(){
 		int 	AHEAD = Integer.MAX_VALUE,
 				BEHIND = Integer.MIN_VALUE;
 		Player aheadPlayer = null;
 		if(turnNumber == 1 || turnNumber == 4 || turnNumber == 5){
 			aheadPlayer = player2;
 		} else{
 			aheadPlayer = player1;
 		} 
 		return (currentPlayer.equals(aheadPlayer) ? AHEAD : BEHIND);
 	}
 
 	@Override
 	public int compareTo(Game otherGame) {
 		return this.getGameId().compareTo(otherGame.getGameId());
 	}
 }
