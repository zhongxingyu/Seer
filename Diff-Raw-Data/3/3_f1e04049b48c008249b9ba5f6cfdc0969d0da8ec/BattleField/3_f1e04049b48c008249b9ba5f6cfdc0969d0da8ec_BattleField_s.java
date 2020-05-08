 package fr.umlv.escape.front;
 
 import java.util.ArrayList;
 import java.util.Hashtable;
 
 import org.jbox2d.dynamics.Body;
 import org.jbox2d.dynamics.Filter;
 
 import fr.umlv.escape.bonus.Bonus;
 import fr.umlv.escape.front.SpriteShip.SpriteType;
 import fr.umlv.escape.game.Game;
 import fr.umlv.escape.ship.Ship;
 import fr.umlv.escape.weapon.Bullet;
 import fr.umlv.escape.world.Bodys;
 import fr.umlv.escape.world.EscapeWorld;
 import android.graphics.Bitmap;
 
 public class BattleField {
 	private Sprite[] boundPlayer;
 	private int WIDTH;
 	private int HEIGHT;
 	private final int LIMIT = 500;
 	private final int ANIMATIONSPEED = 50;
 	BackGroundScroller backgoundScroller;
 	final ArrayList<Ship> shipList;
 	final ArrayList<Bullet> bulletList;
 	final ArrayList<SpriteAnimation> animationList;
 	final ArrayList<Bonus> bonusList;
 	final Hashtable<Body,Ship> shipMap=new Hashtable<Body,Ship>(50);
 	final Hashtable<Body,Bullet> bulletMap=new Hashtable<Body,Bullet>(50);
 	final Hashtable<Body,Bonus> bonusMap=new Hashtable<Body,Bonus>(10);
 
 	
 	private final BattleFieldCleaner bfCleaner;
 	
 	Object shipLock = new Object();
 	Object bonusLock = new Object();
 	Object bulletLock = new Object();
 	Object animationLock = new Object();
 	
 	/**
 	 * Thread that remove all elements of the {@link Battlefield} that should not be
 	 * drawn anymore at fixed rate
 	 */
 	private class BattleFieldCleaner extends Thread{
 		@Override
 		public void run() {
 			while(!Thread.currentThread().isInterrupted()){
 				synchronized (shipLock) {
 					// Treating ships
 					for(int i = 0; i < shipList.size(); i++){
 						Ship tmp = shipList.get(i);
 						if(!tmp.isAlive()){
 							animationList.add(new SpriteAnimation(ANIMATIONSPEED, tmp.getPosXCenter(), tmp.getPosYCenter(), FrontApplication.frontImage.getImage("explosion1")));
 							if(tmp.equals(Game.getTheGame().getPlayer1().getShip())){
 								tmp.setCurrentSprite(SpriteType.DEAD_SHIP);
 							}
 						}
 						if( !tmp.isStillDisplayable()			||
 							tmp.getPosXCenter()< (-LIMIT) 		|| 
 						    tmp.getPosXCenter()> (WIDTH+LIMIT)	||
 						    tmp.getPosYCenter()< (-LIMIT)		||
 						    tmp.getPosYCenter()> (HEIGHT+LIMIT) ){
 							shipList.remove(i);
 							shipMap.remove(tmp);
 						}
 					}
 				}
 				synchronized (bulletLock) {
 					// Treating bullets
 					for(int i = 0; i < bulletList.size(); i++){
 						Bullet tmp = bulletList.get(i);
 						if( !tmp.isStillDisplayable()			||
 							tmp.getPosXCenter()< (-LIMIT) 		|| 
 						    tmp.getPosXCenter()> (WIDTH+LIMIT)	||
 						    tmp.getPosYCenter()< (-LIMIT)		||
 						    tmp.getPosYCenter()> (HEIGHT+LIMIT) ){
 							bulletList.remove(i);
 							bulletMap.remove(tmp);
 						}
 					}
 				}
 				synchronized (bonusLock) {
 					// Treating bonus
 					for(int i = 0; i < bonusList.size(); i++){
 						Bonus tmp = bonusList.get(i);
 						if( !tmp.isStillDisplayable()			||
 							tmp.getPosXCenter()< (-LIMIT) 		|| 
 						    tmp.getPosXCenter()> (WIDTH+LIMIT)	||
 						    tmp.getPosYCenter()< (-LIMIT)		||
 						    tmp.getPosYCenter()> (HEIGHT+LIMIT) ){
 							bonusList.remove(i);
 							bonusMap.remove(tmp);
 						}
 					}
 				}
 				synchronized (animationLock) {
 					// Treating animation
 					for(int i = 0; i < animationList.size(); i++){
 						SpriteAnimation tmp = animationList.get(i);
 						if( !tmp.isStillDisplayable()			||
 							tmp.getPosXCenter()< (-LIMIT) 		|| 
 						    tmp.getPosXCenter()> (WIDTH+LIMIT)	||
 						    tmp.getPosYCenter()< (-LIMIT)		||
 						    tmp.getPosYCenter()> (HEIGHT+LIMIT) ){
							bulletList.remove(i);
							bulletMap.remove(tmp);
 						}
 					}
 				}
 				
 				try {
 					Thread.sleep(100);
 				} catch (InterruptedException e) {
 					Thread.currentThread().interrupt();
 				}
 			}
 		}	
 	}
 	
 	public BattleField(int width, int height, Bitmap background) {
 		this.HEIGHT = height;
 		this.WIDTH = width;
 		this.backgoundScroller = new BackGroundScroller(width,height,background);
 		this.shipList = new ArrayList<Ship>();
 		this.bulletList = new ArrayList<Bullet>();
 		this.bonusList = new ArrayList<Bonus>();
 		this.animationList = new ArrayList<SpriteAnimation>();
 		this.bfCleaner = new BattleFieldCleaner();
 		this.boundPlayer = new Sprite[4];
 		
 		//Creating the bounds for the player
 		boundPlayer[0]= new Sprite(Bodys.createBasicRectangle(0, height, width, 1,2),null);	//wall bot
 		boundPlayer[1]= new Sprite(Bodys.createBasicRectangle(0, 0, width, 1,2),null);		//wall top
 		boundPlayer[2]= new Sprite(Bodys.createBasicRectangle(0, 0, 1, height,2),null);		//wall left
 		boundPlayer[3]= new Sprite(Bodys.createBasicRectangle(width, 0, 1, height,2),null);	//wall right
 		
 		Filter filter=new Filter();
 		filter.categoryBits=EscapeWorld.CATEGORY_DECOR;
 		filter.maskBits=EscapeWorld.CATEGORY_PLAYER;
 		
 		for(int i=0;i<boundPlayer.length;++i){
 			boundPlayer[i].body.getFixtureList().setFilterData(filter);
 			boundPlayer[i].body.getFixtureList().m_isSensor=false;
 		}
 	}
 	
 	public void launchBfCleaner(){
 		this.bfCleaner.start();
 	}
 	
 	/**
 	 * Add a bullet to the battlefield. This method is ThreadSafe.
 	 * @param bullet the bullet to add to the battlefield.
 	 */
 	public void addBullet(Bullet bullet){
 		synchronized(bulletLock){
 			bulletList.add(bullet);
 			bulletMap.put(bullet.getBody(), bullet);
 		}
 	}
 	
 	/**
 	 * Add a ship to the battlefield. This method is ThreadSafe.
 	 * @param ship the ship to add to the battlefield.
 	 */
 	public void addShip(Ship ship){
 		synchronized(shipLock){
 			System.out.println(ship.toString()+" added");
 			shipList.add(ship);
 			shipMap.put(ship.getBody(), ship);
 		}
 	}
 	
 	/**
 	 * Add a bonus to the battlefield. This method is ThreadSafe.
 	 * @param bonus the bonus to add to the battlefield.
 	 */
 	public void addBonus(Bonus bonus){
 		synchronized(bonusLock){
 			bonusList.add(bonus);
 			bonusMap.put(bonus.getBody(), bonus);
 		}
 	}
 	
 	/**
 	 * Delete all sprite in the battlefield except the player sprite. This method is ThreadSafe.
 	 */
 	public void deleteAllBonus(){
 		synchronized(bulletLock){
 			bulletList.clear();
 			bulletMap.clear();
 		}
 		synchronized(shipLock){
 			shipList.clear();
 			shipMap.clear();
 		}
 		synchronized(bonusLock){
 			bonusList.clear();
 			bonusMap.clear();
 		}
 	}
 	
 	/**
 	 * Return a ship associated to a body in O(1).
 	 * @param body the body of the ship to get.
 	 * @return The ship associated to the body in parameter.
 	 */
 	public Ship getShip(Body body){
 		return shipMap.get(body);
 	}
 	
 	/**
 	 * Return a bullet associated to a body in O(1).
 	 * @param body the body of the bullet to get.
 	 * @return The bullet associated to the body in parameter.
 	 */
 	public Bullet getBullet(Body body){
 		return bulletMap.get(body);
 	}
 	
 	/**
 	 * Return a bonus associated to a body in O(1).
 	 * @param body the body of the bonus to get.
 	 * @return The bonus associated to the body in parameter.
 	 */
 	public Bonus getBonus(Body body){
 		return bonusMap.get(body);
 	}
 	
 	public void updateSreenSize(int width, int height){
 		this.WIDTH = width;
 		this.HEIGHT = height;
 		backgoundScroller.updateScreenSizes(width, height);
 	}
 	
 	public int getWIDTH() {
 		return WIDTH;
 	}
 
 	public int getHEIGHT() {
 		return HEIGHT;
 	}
 }
