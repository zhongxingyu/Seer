 package ch.zhaw.mcag;
 
 import ch.zhaw.mcag.model.ItemFactory;
 import java.util.*;
 
 import ch.zhaw.mcag.model.*;
 import ch.zhaw.mcag.model.creature.*;
 import ch.zhaw.mcag.model.obstacle.*;
 
 /**
  * Game context
  */
 public class Game {
 
 	// Config
 	private Config config;
 
 	// Player
 	private Player player;
 	private double points = 0;
 	private int lifes = Config.getLifes();
 	private static final int MAX_LIFES = 10;
 
 	// Background
 	private Background background;
 
 	// Items
 	private LinkedList<Enemy> enemies = new LinkedList<>();
 	private LinkedList<Hard> hardObstacles = new LinkedList<>();
 	private LinkedList<Soft> softObstacles = new LinkedList<>();
 	private LinkedList<Shot> shots = new LinkedList<>();
 	private LinkedList<Extra> extras = new LinkedList<>();
 	private LinkedList<Explosion> explosions = new LinkedList<>();
 	private final Highscore highscore = Highscore.getHighscore("Highscore.ser");
 
 	// State
 	private boolean pause = true;
 
 	/**
 	 * Reset the game context
 	 */
 	public void resetContext() {
 		this.points = 0;
 		this.lifes = Config.getLifes();
 		this.enemies = new LinkedList<>();
 		this.hardObstacles = new LinkedList<>();
 		this.softObstacles = new LinkedList<>();
 		this.shots = new LinkedList<>();
 		this.extras = new LinkedList<>();
 		this.explosions = new LinkedList<>();
 		setPlayer(ItemFactory.createPlayer());
 		Config.setGameSpeed(Config.getInitialSpeed());
 	}
 
 	/**
 	 * Get the configuration
 	 *
 	 * @return configuration
 	 */
 	public Config getConfig() {
 		return config;
 	}
 
 	/**
 	 * Set a configuration
 	 *
 	 * @param config
 	 */
 	public void setConfig(Config config) {
 		this.config = config;
 	}
 
 	/**
 	 * Get the player
 	 *
 	 * @return player
 	 */
 	public Player getPlayer() {
 		return player;
 	}
 
 	/**
 	 * Set the player
 	 *
 	 * @param player
 	 */
 	public void setPlayer(Player player) {
 		this.player = player;
 	}
 
 	/**
 	 * Get the background
 	 *
 	 * @return background
 	 */
 	public Background getBackground() {
 		return background;
 	}
 
 	/**
 	 * Set the Background
 	 *
 	 * @param background
 	 */
 	public void setBackground(Background background) {
 		this.background = background;
 	}
 
 	/**
 	 * Get the enemies
 	 *
 	 * @return enemies
 	 */
 	public LinkedList<Enemy> getEnemies() {
 		return enemies;
 	}
 	/**
 	 * Get the hard obstacles
 	 *
 	 * @return hard obstacles
 	 */
 	public LinkedList<Hard> getHardObstacles() {
 		return hardObstacles;
 	}
 
 	/**
 	 * Get the soft Obstacles
 	 *
 	 * @return soft obstacles
 	 */
 	public LinkedList<Soft> getSoftObstacles() {
 		return softObstacles;
 	}
 
 	/**
 	 * Get the shots
 	 *
 	 * @return shots
 	 */
	public LinkedList<Shot> getShots() {
 		return shots;
 	}
 
 	/**
 	 * Is game paused?
 	 *
 	 * @return pause state
 	 */
 	public boolean isPaused() {
 		return pause;
 	}
 
 	/**
 	 * Set game paused
 	 *
 	 * @param pause
 	 */
 	public void setPause(boolean pause) {
 		this.pause = pause;
 	}
 
 	/**
 	 * Get points
 	 *
 	 * @return points
 	 */
 	public double getPoints() {
 		return points;
 	}
 
 	/**
 	 * Set points
 	 *
 	 * @param points
 	 */
 	public void setPoints(double points) {
 		this.points = points;
 	}
 
 	/**
 	 * Get extras
 	 *
 	 * @return extras
 	 */
 	public LinkedList<Extra> getExtras() {
 		return this.extras;
 	}
 
 	/**
 	 * Get the explosions
 	 *
 	 * @return explosions
 	 */
 	public LinkedList<Explosion> getExplosions() {
 		return explosions;
 	}
 
 	/**
 	 * Get the lifes
 	 *
 	 * @return
 	 */
 	public int getLifes() {
 		return lifes;
 	}
 
 	/**
 	 * Set the lifes
 	 *
 	 * @param lifes
 	 */
 	public void setLifes(int lifes) {
 		if (lifes > 0) {
 			this.lifes = lifes <= MAX_LIFES ? lifes : MAX_LIFES;
 		} else {
 			this.lifes = 0;
 			this.setPause(true);
 		}
 	}
 
 	/**
 	 * Get all the enemy items on the board
 	 *
 	 * @return evil stuff
 	 */
 	public LinkedList<Item> getEvilStuff() {
 		LinkedList<Item> tmp = new LinkedList<>();
 		tmp.addAll(this.getEnemies());
 		tmp.addAll(this.getHardObstacles());
 		tmp.addAll(this.getSoftObstacles());
 		tmp.addAll(this.getEnemies());
 
 		List<Shot> tmpShots = (List<Shot>) this.getShots().clone();
 
 		for (Shot shot : tmpShots) {
 			if (!shot.isGood()) {
 				tmp.add(shot);
 			}
 		}
 		return tmp;
 	}
 
 	/**
 	 * Get the friendly items on the board
 	 *
 	 * @return good stuff
 	 */
 	public LinkedList<Item> getGoodStuff() {
 		LinkedList<Item> tmp = new LinkedList<>();
 
 		List<Shot> tmpShots = (List<Shot>) this.getShots().clone();
 
 		for (Shot shot : tmpShots) {
 			if (shot.isGood()) {
 				tmp.add(shot);
 			}
 		}
 		tmp.add(this.getPlayer());
 		return tmp;
 	}
 
 	/**
 	 * Get all item on the board
 	 *
 	 * @return items
 	 */
 	public LinkedList<Item> getAllStuff() {
 		LinkedList<Item> tmp = new LinkedList<>();
 		tmp.addAll(this.getEnemies());
 		tmp.addAll(this.getHardObstacles());
 		tmp.addAll(this.getSoftObstacles());
 		tmp.addAll(this.getEnemies());
 		tmp.addAll(this.getShots());
 		tmp.addAll(this.getExtras());
 		tmp.addAll(this.getExplosions());
 		tmp.add(this.getPlayer());
 		return tmp;
 	}
 
 	/**
 	 * Get the Highscore
 	 *
 	 * @return
 	 */
 	public Highscore getHighscore() {
 		return highscore;
 	}
 }
