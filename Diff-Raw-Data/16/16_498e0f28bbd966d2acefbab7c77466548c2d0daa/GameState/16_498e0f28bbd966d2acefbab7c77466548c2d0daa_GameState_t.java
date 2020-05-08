 package model;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
import service.resources.SoundHandler;

 import model.core.PlayerIndex;
 import model.elements.Animation;
 import model.elements.Bonus;
 import model.elements.Bullet;
 import model.elements.Bunker;
 import model.elements.Invader;
 import model.elements.KillableGameElement;
 import model.elements.Player;
 
 /**
  * Represents a game session. A GameState can be intialized in different ways to
  * create different levels. A GameState should never be instatiated directly but
  * be created through the {@link GameStateFactory}.
  */
 public class GameState {
 	private int id;
 	private HashMap<PlayerIndex, Player> players;
 	private ArrayList<Bunker> bunkers;
 	private ArrayList<Invader> invaders;
 	private ArrayList<KillableGameElement> individualEnemies; //TODO for special movement... Bosses for instance or slow-shot
 	private ArrayList<Bonus> bonuses;
 	private ArrayList<Bullet> bullets;
 	private ArrayList<Animation> animations;
 
 	private long lastUpdateTime;
 	private long totalGameTime;
 	private boolean moveInvadersRight;
 	
 	private GameStateState state;
 	
 	// Patricks logik siger at denne skal v�re i GameState
 	private long lastInvaderShot; 
 	private ArrayList<Invader> lowestInvaders; // ved ikke heeelt med denne
 
 	public GameState(int id) {
 		this.setId(id);
 		this.bunkers = new ArrayList<Bunker>();
 		this.invaders = new ArrayList<Invader>();
 		this.bonuses = new ArrayList<Bonus>();
 		this.bullets = new ArrayList<Bullet>();
 		this.individualEnemies = new ArrayList<KillableGameElement>();
 		this.lowestInvaders = new ArrayList<Invader>();
 		this.lastInvaderShot = 0;
 		this.players = new HashMap<PlayerIndex, Player>();
 		this.setAnimations(new ArrayList<Animation>());
 		this.state = GameStateState.Waiting;
 	}
 
 	public Player getPlayer(PlayerIndex idx) {
 		return this.players.get(idx);
 	}
 
 	public void setPlayer(PlayerIndex idx, Player player) {
 		this.players.put(idx, player);
 	}
 
 	public ArrayList<Bunker> getBunkers() {
 		return bunkers;
 	}
 
 	public ArrayList<Invader> getInvaders() {
 		return invaders;
 	}
 
 	public ArrayList<Bonus> getBonuses() {
 		return bonuses;
 	}
 
 	public ArrayList<Bullet> getBullets() {
 		return bullets;
 	}
 
 	public long getLastUpdateTime() {
 		return lastUpdateTime;
 	}
 
 	public void setLastUpdateTime(long lastUpdateTime) {
 		this.lastUpdateTime = lastUpdateTime;
 	}
 
 	public long getTotalGameTime() {
 		return totalGameTime;
 	}
 
 	public void setTotalGameTime(long totalGameTime) {
 		this.totalGameTime = totalGameTime;
 	}
 
 	public void addTotalGameTime(long time) {
 		this.totalGameTime += time;
 	}
 
 	public boolean getMoveInvadersRight() {
 		return this.moveInvadersRight;
 	}
 
 	public void setMoveInvadersRight(boolean moveInvadersRight) {
 		this.moveInvadersRight = moveInvadersRight;
 	}
 
 	public long getLastInvaderShot() {
 		return lastInvaderShot;
 	}
 
 	public void setLastInvaderShot(long lastInvaderShot) {
 		this.lastInvaderShot = lastInvaderShot;
 	}
 
 	public int getId() {
 		return id;
 	}
 
 	public void setId(int id) {
 		this.id = id;
 	}
 
 	public GameStateState getState() {
 		return state;
 	}
 
 	public void setState(GameStateState state) {
		if(state == GameStateState.Lost){
			SoundHandler.getInstance().playSound("game_over02.wav", 0, 0,6.0f);
		}
 		this.state = state;
 	}
 
 	public ArrayList<Animation> getAnimations() {
 		return animations;
 	}
 
 	public void setAnimations(ArrayList<Animation> animations) {
 		this.animations = animations;
 	}
 
 	public ArrayList<KillableGameElement> getIndividualEnemies() {
 		return individualEnemies;
 	}
 
 	public void setIndividualEnemies(ArrayList<KillableGameElement> individualEnemies) {
 		this.individualEnemies = individualEnemies;
 	}
 }
