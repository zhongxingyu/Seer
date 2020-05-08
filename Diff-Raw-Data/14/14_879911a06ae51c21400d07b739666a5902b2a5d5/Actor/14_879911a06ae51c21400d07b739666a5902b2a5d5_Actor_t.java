 package game;
 
 /**
  * Class for the player's characters
  * 
  * @author Jacob Charles
  *
  */
 public class Actor extends GameObject {
 	
 	//current data
	//private Player owner;
 	private int airTime;
 	private int deadTime;
 	private int reload;
 	private int dir = 1;
 	private int powerup;
 	private Land onLand;
 	
 	//eventually replace this with a reference to a "character archetype?"
 	private int skin;
 	private int runSpeed;
 	private int jumpPower;
 	private int shotDelay;
 	private int shotSpeed, shotLife;
 	private int shotWid, shotHei;
 	
 	/**
 	 * Spawn a player character a location
 	 * 
 	 * @param x
 	 * 		starting x
 	 * @param y
 	 * 		starting y
 	 * @param owner
 	 * 		associated player object
 	 * TODO: Associate this with a "base" player
 	 */
 	public Actor (int x, int y, Player owner) {
 		super(x, y, 16, 16);
		//this.owner = owner;
 		dir = 1;
 		airTime = 1;
 		onLand = null;
 		powerup = 0;
 		
 		//default stats
 		runSpeed = 3;
 		jumpPower = 10;
 		shotDelay = 16;
 		shotSpeed = 16;
 		shotLife = 20;
 		shotWid = 8;
 		shotHei = 8;
 	}
 	
 	/*getters and setters for attributes*/
	/*public Player getOwner() {
 		return owner;
	}*/
 	public void setOwner(Player owner) {
		//this.owner = owner;
 	}
 	public int getAirTime() {
 		return airTime;
 	}
 	public void setAirTime(int airTime) {
 		this.airTime = airTime;
 	}
 	public int getDeadTime() {
 		return deadTime;
 	}
 	public void setDeadTime(int deadTime) {
 		this.deadTime = deadTime;
 	}
 	public int getReload() {
 		return reload;
 	}
 	public void setReload(int reload) {
 		this.reload = reload;
 	}
 	public int getDir() {
 		return dir;
 	}
 	public void setDir(int dir) {
 		this.dir = dir;
 	}
 	public int getPowerup() {
 		return powerup;
 	}
 	public void setEquip(int powerup) {
 		this.powerup = powerup;
 	}
 	public Land getOnLand() {
 		return onLand;
 	}
 	public void setOnLand(Land onLand) {
 		this.onLand = onLand;
 	}
 	public int getSkin() {
 		return skin;
 	}
 	public void setSkin(int skin) {
 		this.skin = skin;
 	}
 	public int getRunSpeed() {
 		return runSpeed;
 	}
 	public void setRunSpeed(int runSpeed) {
 		this.runSpeed = runSpeed;
 	}
 	public int getJumpPower() {
 		return jumpPower;
 	}
 	public void setJumpPower(int jumpPower) {
 		this.jumpPower = jumpPower;
 	}
 	public int getShotDelay() {
 		return shotDelay;
 	}
 	public void setShotDelay(int shotDelay) {
 		this.shotDelay = shotDelay;
 	}
 	public int getShotSpeed() {
 		return shotSpeed;
 	}
 	public void setShotSpeed(int shotSpeed) {
 		this.shotSpeed = shotSpeed;
 	}
 	public int getShotLife() {
 		return shotLife;
 	}
 	public void setShotLife(int shotLife) {
 		this.shotLife = shotLife;
 	}
 	public int getShotWid() {
 		return shotWid;
 	}
 	public void setShotWid(int shotWid) {
 		this.shotWid = shotWid;
 	}
 	public int getShotHei() {
 		return shotHei;
 	}
 	public void setShotHei(int shotHei) {
 		this.shotHei = shotHei;
 	}
 }
