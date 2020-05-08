 package chalmers.TDA367.B17.model;
 
 import org.newdawn.slick.geom.Vector2f;
 
 import chalmers.TDA367.B17.controller.GameController;
 import chalmers.TDA367.B17.tanks.DefaultTank;
 
 public class Player {
 	private String name;
 	private int playerId;
 	private int score;
 	private AbstractTank tank;
 	private int lives;
 	private int respawnTimer;
 	private int respawnTime;
 	public boolean tankDead;
 	public String tankType;
 	public boolean active;
 
 	/**
 	 * Create a new Player.
 	 * @param name The player's name
 	 */
 	public Player(String name){
 		GameController.getInstance().gameConditions.addPlayer(this);
 		this.name = name;
 		tankType = "default";
 		setTank(new DefaultTank(new Vector2f(0,-1), this));
 		active = true;
		setLives(GameController.getInstance().gameConditions.getPlayerLives());
		setRespawnTime(GameController.getInstance().gameConditions.getSpawnTime());
 	}
 
 	/**
 	 * Get the players tank.
 	 * @return The tank
 	 */
 	public AbstractTank getTank(){
 		return tank;
 	}
 	
 	/**
 	 * Get the players name.
 	 * @return The name
 	 */
 	public String getName() {
 		return name;
 	}
 	
 	/**
 	 * Get the players ID.
 	 * @return The ID
 	 */
 	public int getPlayerId() {
 		return playerId;
 	}
 	
 	/**
 	 * Get the players score.
 	 * @return The score
 	 */
 	public int getScore() {
 		return score;
 	}
 	
 	public int getLives() {
 		return lives;
 	}
 
 	public void setLives(int lives) {
 		this.lives = lives;
 	}
 
 	public int getRespawnTimer() {
 		return respawnTimer;
 	}
 
 	public void setRespawnTimer(int respawnTimer) {
 		this.respawnTimer = respawnTimer;
 	}
 
 	public void setTank(AbstractTank tank) {
 		this.tank = tank;
 	}
 
 	/**
 	 * Set the players name.
 	 * @param name The new name
 	 */
 	public void setName(String name){
 		this.name = name;
 	}
 	
 	/**
 	 * Set the players ID.
 	 * @param id The new ID
 	 */
 	public void setPlayerId(int id){
 		this.playerId = id;
 	}
 	
 	/**
 	 * Set the players score.
 	 * @param score The new score
 	 */
 	public void setScore(int score){
 		this.score = score;
 	}
 	
 	
 	public void tankDeath(){
 		tankDead = true;
 		spawnTank();
 	}
 	
 	
 	public void spawnTank(){
 		if(getLives() > 0){
			setLives(getLives() - 1);
 			this.respawnTimer = respawnTime;
 			GameController.getInstance().getWorld().getTankSpawner().addPlayer(this);
 		}else{
 			setActive(false);
 		}
 	}
 
 	public boolean isActive() {
 		return active;
 	}
 
 	public void setActive(boolean active) {
 		this.active = active;
 	}

	public int getRespawnTime() {
		return respawnTime;
	}

	public void setRespawnTime(int respawnTime) {
		this.respawnTime = respawnTime;
	}
 }
