 package fr.umlv.escape.weapon;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.Body;
 
 import android.graphics.Bitmap;
 import fr.umlv.escape.front.FrontApplication;
 import fr.umlv.escape.front.Sprite;
 import fr.umlv.escape.move.Movable;
 
 /**
  * Represent a Bullet
  */
 public abstract class Bullet extends Sprite {
 	private final int power;
 	private int currentLoad;
 	private final int maxLoad;
 	private Movable move;
 	private final String name;
 	private long lastTimeLoad;
 	private final boolean playerBullet;
 	
 	/**
 	 * Constructor of bullet
 	 * @param power bullet's power affect the inflict damage
 	 * @param maxLoad bullet's max load
 	 * @param name bullet's kind
 	 * @param body bullet's body
 	 * @param playerBullet if it's a player who launch the bullet
 	 */
 	public Bullet(int power, int maxLoad, String name, Body body, boolean playerBullet, Bitmap img){
 		super(body, img);
 		if(name == null || body == null) throw new IllegalArgumentException();
 		this.name = name;
 		this.power = power;
 		this.currentLoad = 1;
 		this.maxLoad = maxLoad;
 		this.lastTimeLoad = System.currentTimeMillis();
 		this.playerBullet = playerBullet;
 	}
 	
 	/**
 	 * @return the value of the max bullet's charge
 	 */
 	public int getMaxLoad() {
 		return maxLoad;
 	}
 	
 	/**
 	 * @return true if it's a player who launch the bullet else false
 	 */
 	public boolean isPlayerBullet(){
 		return playerBullet;
 	}
 	
 	/**
 	 * Set the move get in params
 	 * @param move
 	 */
 	public void setMove(Movable move) {
 		this.move = move;
 	}
 
 	/**
 	 * @return bullet's body 
 	 */
 	public Body getBody() {
 		return body;
 	}
 
 	/**
 	 * @return the charge of the bullet
 	 */
 	public int getCurrentLoad() {
 		return currentLoad;
 	}
 
 	/**
 	 * @return the move who will be apply
 	 */
 	public Movable getMove() {
 		return move;
 	}
 	
 	/**
 	 * @return the bullet's name
 	 */
 	public String getName() {
 		return name;
 	}
 
 	/**
 	 * Launch the bullet with his move.
 	 * @param p direction of bullet
 	 */
 	public abstract void fire(Vec2 p);
 
 	/**
 	 * If the bullet must be upgrade because time since last load is spend it's increment currentLoad and load the image in {@link FrontImage}
 	 * @return true if currentLoad are increment else false
 	 */
 	public boolean loadPower() {
 		long timeNow = System.currentTimeMillis();
 		if(currentLoad < maxLoad && timeNow - lastTimeLoad > 2000){
 			lastTimeLoad = timeNow;
 			currentLoad = currentLoad + 1;
 			//BitmapFactory.decodeResource(getResources(), R.drawable.);
 			return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * @return power's weapon
 	 */
 	public int getDamage(){
 		return this.power;
 	}
 }
