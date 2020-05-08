 package model.liarsDice.gameLogic;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import model.Player;
 import model.liarsDice.gameInfo.GameHistory;
 import model.liarsDice.gameInfo.GameInfo;
 
 
 
 /**
  * An extension of the Player class specific for LiarsDiceGame.  Keeps track of its bot and dice.
  */
 public class LiarsDicePlayer extends Player{
 	private static final int INITIAL_NUM_DICE = 5;
 	
 	private LiarsDiceBot bot;
 	private List<Die> dice;
 
 	/**
 	 * Constructor.
 	 * @param bot The bot implementation to be used by this player.
 	 * @param id The unique ID that this player will be known by.  (More than one player could use the same bot, but will have unique ID's.)
 	 */
 	public LiarsDicePlayer(LiarsDiceBot bot, int id) {
 		super(id);
 		this.bot = bot;
 		dice = new ArrayList<Die>();
 		int numDice = INITIAL_NUM_DICE;
 		for (int i=0; i<numDice; i++) {
 			dice.add(new Die());	
 		}
 	}
 
	public LiarsDicePlayer(LiarsDicePlayer other, int id) {
		super(id);
		this.bot = other.bot;
		dice = new ArrayList<Die>();
		int numDice = INITIAL_NUM_DICE;
		for (int i=0; i<numDice; i++) {
			dice.add(new Die());	
		}
	}

 	/**
 	 * Removes one of the dice this player has.  (Used when they lose a round.)
 	 */
 	public void removeDie() {
 		//intentionally leaving open for exceptions
 		dice.remove(0);
 	}
 	
 	/**
 	 * Rerolls the player's remaining dice. (Random value [1,6])
 	 */
 	public void rerollDice() {
 		int numDice = dice.size();
 		dice = new ArrayList<Die>();
 		for (int i=0; i<numDice; i++) {
 			dice.add(new Die());	
 		}
 	}
 	
 	/**
 	 * Resets the player's dice to have 5 dice with new random values [1,6].
 	 */
 	public void resetDice(){
 		int numDice = INITIAL_NUM_DICE;
 		dice = new ArrayList<Die>();
 		for(int i = 0; i < numDice; i++){
 			dice.add(new Die());
 		}
 	}
 	
 	/**
 	 * Method which will ask a bot to return a decision based on the current state of the game.
 	 * @param gameInfo Current state of the game.
 	 * @return The decision made by the bot.
 	 * @throws InterruptedException 
 	 */
 	public Decision getDecision(GameInfo gameInfo) throws InterruptedException {
 		return bot.getDecision(gameInfo);
 	}
 
 	/**
 	 * @return List of this player's current dice.
 	 */
 	public List<Die> getDice() {
 		return dice;
 	}
 	
 	/**
 	 * Used to tell the bots what happened in the game after it is over - in case they want to learn from it.
 	 * @param gameHistory History of the game.
 	 */
 	public void reportGameResults(GameInfo gameInfo){
 		bot.reportGameResults(gameInfo);
 	}
 
 	/**
 	 * @return The bot's chosen name.
 	 */
 	public String getName() {
 		return bot.getName();
 	}
 }
