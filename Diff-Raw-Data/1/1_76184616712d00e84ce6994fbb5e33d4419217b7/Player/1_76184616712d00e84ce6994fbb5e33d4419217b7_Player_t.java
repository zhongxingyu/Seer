 package se.chalmers.tda367.std.core;
 
 import com.google.common.eventbus.Subscribe;
 
 import se.chalmers.tda367.std.core.events.EnemyDeadEvent;
 import se.chalmers.tda367.std.utilities.EventBus;
import se.chalmers.tda367.std.utilities.Position;
 
 /**
  * Represents a player. Contains name, score etc.
  * @author Unchanged
  * @modified Johan Gustafsson
  * @modified Emil Edholm (May 14, 2012)
  * @date Mar 22, 2012
  */
 public class Player {
 	
 	public static final String DEFAULT_NAME   = "Random Player";
 	public static final int    STARTING_MONEY = 500;
 	
 	private String name;
 	private int currentScore;
 	private int money;
 	private IPlayerCharacter character;
 	
 	/**
 	 * Create a new player with the name {@code DEFAULT_NAME}.
 	 */
 	public Player(){
 		this(DEFAULT_NAME);
 	}
 	public Player(String name){
 		this.name = name;
 		this.character = new PlayerCharacter(new Position(100, 250));
 		this.setScore(0);
 		this.addMoney(STARTING_MONEY);
 		
 		EventBus.INSTANCE.register(this);
 	}
 	/**
 	 * @return the current player score
 	 */
 	public int getCurrentScore() {
 		return currentScore;
 	}
 	/**
 	 * @param score - the player score to set.
 	 */
 	public void setScore(int score) {
 		this.currentScore = score;
 	}
 	/**
 	 * @return the amount of money in the treasury.
 	 */
 	public int getMoney() {
 		return money;
 	}
 	
 	/**
 	 * @param amount - the amount which will be removed from the treasury
 	 */
 	public void removeMoney(int money) {
 		this.money -= money;
 	}
 	
 	/**
 	 * @param amount - the amount which will be added to the treasury
 	 */
 	public void addMoney(int money) {
 		this.money += money;
 	}
 	
 	/**
 	 * @return the name of the player
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * This will return a reference to this player's character
 	 * @return a reference to the player's {@code PlayerCharacter}
 	 */
 	public IPlayerCharacter getCharacter() {
 		return character;
 	}
         /**
 	 * Event handler for when an enemy dies and it should be looted.
 	 * @param e - the event that contains the dead enemy.
 	 */
 	@Subscribe
 	public void lootDeadEnemy(EnemyDeadEvent e) {
 		int lootedMoney = e.getDeadEnemy().getLootValue();
 		addMoney(lootedMoney);
 	}
 }
