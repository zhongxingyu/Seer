 // Package Declaration //
 package com.gamedev.decline;
 
 //Java Package Support //
 import java.util.Random;
 import java.util.Iterator;
 
 //Badlogic Package Support //
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.utils.Array;
 
 /**
  * 
  * com/gamedev/decline/ItemManager.java
  * 
  * @author(s) : Ian Middleton, Zach Coker, Zach Ogle
  * @version : 2.0 Last Update : 3/23/2013 Update By : Ian Middleton
  * 
  *          Source code for the ItemManager class. The ItemManager class takes
  *          care of updating, drawing, and placing Item objects (HealthPack and
  *          Ammo).
  * 
  */
 public class ItemManager {
 	// Global Singleton //
 	GlobalSingleton gs = GlobalSingleton.getInstance();
 
 	// Constants of the Object //
 	public static final int MAX_AMMO = 5;
 	public static final int MAX_HEALTH = 5;
 	public static final int MAX_BUSHES = 10;
 
 	// Internal Variables //
 	private Ammo[] ammoArray = new Ammo[MAX_AMMO];
 	private Bush[] bushArray = new Bush[MAX_BUSHES];
 	private HealthPack[] healthArray = new HealthPack[MAX_HEALTH];
 	private Array<Ammo> currentAmmo = new Array<Ammo>();
 	private Array<Bush> currentBushes = new Array<Bush>();
 	private Array<HealthPack> currentHealthPacks = new Array<HealthPack>();
 	private Iterator<Ammo> ammoIter;
 	private Iterator<HealthPack> healthIter;
 	private Iterator<Bush> bushIter;
 	private Ammo ammo;
 	private HealthPack pack;
 	private Bush bush;
 	private Random rand = new Random();
 	private int newAmmoPosition = 800 + rand.nextInt() % 500;
 	private int newHealthPackPosition = 800 + rand.nextInt() % 500;
 	private int newBushPosition = 500 + rand.nextInt() % 500;
 	private int currentAmmoCount = 0;
 	private int currentHealthCount = 0;
 	private int currentBushCount = 0;
 	private Texture ammoTexture, healthTexture, bushTexture;
 
 	/**
 	 * Instantiates a new ItemManager object. The ItemManager fills an array of
 	 * new Ammo objects with the ammo texture and an array of new HealthPack
 	 * objects with the health texture to be used in the game. This is done to
 	 * create a buffer of Ammo and HealthPack objects.
 	 * 
 	 * @param ammoTexture
 	 *            : The image to be used for the Ammo objects.
 	 * @param healthTexture
 	 *            : The image to be used for the HealthPack objects.
 	 */
 	public ItemManager(Texture ammoTexture, Texture healthTexture,
 	    Texture bushTexture) {
 		this.ammoTexture = ammoTexture;
 		this.healthTexture = healthTexture;
 		this.bushTexture = bushTexture;
 		for (int i = 0; i < ammoArray.length; i++) {
 			ammoArray[i] = new Ammo(ammoTexture);
 		}
 		for (int i  = 0; i < bushArray.length; i++){
 		      bushArray[i] = new Bush(bushTexture);
 		}
 		
 		for (int i = 0; i < healthArray.length; i++) {
 			healthArray[i] = new HealthPack(healthTexture);
 		}
 	}
 
 	public Array<Ammo> getActiveAmmo() {
 		return currentAmmo;
 	}
 	
 	public Array<Bush> getActiveBush() {
 	   return currentBushes;
 	}
 
 	public Array<HealthPack> getActiveHealthPacks() {
 		return currentHealthPacks;
 	}
 
 	public void removeActiveAmmo(int index) {
 		currentAmmo.removeIndex(index);
 	}
 	
 	public void removeActiveBush(int index) {
 	      currentBushes.removeIndex(index);
 	}
 
 	public void removeActiveHealthPack(int index) {
 		currentHealthPacks.removeIndex(index);
 	}
 
 	/**
 	 * Grabs an Ammo from the Ammo buffer created when the manager was
 	 * constructed. This Ammo is then added to the array of Ammo that are to be
 	 * drawn to the screen and updated.
 	 */
 	public void makeAmmoAppear() {
 		//ammo = ammoArray[currentAmmoCount % MAX_AMMO];
 		ammo = new Ammo(ammoTexture);
 		ammo.setToInitialDrawPosition();
 		ammo.setXPos(gs.getWorldXPos() + Item.START_XDRAW);
 		ammo.setYPos(Item.START_YDRAW);
 		ammo.setAmountOfAmmoStored(rand.nextInt(5) * 5 + 5);
 		currentAmmo.add(ammo);
 		currentAmmoCount++;
 	}
 	
 	public void makeBushAppear() {
 		//bush = bushArray[currentBushCount % MAX_AMMO];
 		bush = new Bush(bushTexture);
 		bush.setToInitialDrawPosition();
 		bush.setXPos(gs.getWorldXPos() + Item.START_XDRAW);
 		bush.setYPos(Item.START_YDRAW);
 		currentBushes.add(bush);
 		currentBushCount++;
 	}
 
 	/**
 	 * Grabs a HealthPack from the HealthPack buffer created when the manager
 	 * was constructed. This HealthPack is then added to the array of HealthPack
 	 * that are to be drawn to the screen and updated.
 	 */
 	public void makeHealthAppear() {
 		//pack = healthArray[currentHealthCount % MAX_HEALTH];
 		pack = new HealthPack(healthTexture);
 		pack.setToInitialDrawPosition();
 		pack.setXPos(gs.getWorldXPos() + Item.START_XDRAW);
 		pack.setYPos(Item.START_YDRAW);
 		pack.setAmountOfHealth(rand.nextInt(10) * 5 + 5);
 		currentHealthPacks.add(pack);
 		currentHealthCount++;
 	}
 
 	/**
 	 * Checks to see if the hero has moved farther than the random amount
 	 * required for a new Item object to be spawned. If the hero has moved this
 	 * distance then a new Item object is created. Afterwards, the function
 	 * iterates through all Item objects on the screen and calls their update
 	 * method.
 	 */
 	public void update() {
		if(gs.getHeroXPos() < 19000){
 			if (gs.getHeroXPos() > newAmmoPosition) {
 				makeAmmoAppear();
 				newAmmoPosition += 800 + rand.nextInt() % 500;
 			}
 		}
 		ammoIter = currentAmmo.iterator();
 		while (ammoIter.hasNext()) {
 			ammo = ammoIter.next();
 			ammo.update();
 			if (ammo.getX() < -1 * ammo.getWidth()) {
 				ammoIter.remove();
 			}
 		}
 		
		if(gs.getHeroXPos() < 19000){
 			if (gs.getHeroXPos() > newBushPosition) {
 				makeBushAppear();
 				newBushPosition += 500 + rand.nextInt() % 500;
 			}
 		}
 		bushIter = currentBushes.iterator();
 		while (bushIter.hasNext()) {
 			bush = bushIter.next();
 			bush.update();
 			if (bush.getX() < -1 * (bush.getWidth() + 1000)) {
 				bushIter.remove();
 			}
 			
 		}
 		
		if(gs.getHeroXPos() < 19000){
 			if (gs.getHeroXPos() > newHealthPackPosition) {
 				makeHealthAppear();
 				newHealthPackPosition += 800 + rand.nextInt() % 500;
 			}
 		}
 		healthIter = currentHealthPacks.iterator();
 		while (healthIter.hasNext()) {
 			pack = healthIter.next();
 			pack.update();
 			if (pack.getX() < -1 * pack.getWidth()) {
 				healthIter.remove();
 			}
 			
 		}
 	}
 
 	/**
 	 * Iterates through the array of Ammo objects and if they are showing, the
 	 * Ammo object's draw function is called.
 	 * 
 	 * @param batch
 	 *            - The SpriteBatch object which will draw the Ammo objects.
 	 */
 	public void draw(SpriteBatch batch) {
 		
 		
 		bushIter = currentBushes.iterator();
 		while (bushIter.hasNext())
 		{
 		  bush = bushIter.next();
 		  bush.draw(batch);
 		}
 		
 		ammoIter = currentAmmo.iterator();
 		while (ammoIter.hasNext()) {
 			ammo = ammoIter.next();
 			ammo.draw(batch);
 		}
 		
 		healthIter = currentHealthPacks.iterator();
 		while (healthIter.hasNext()) {
 			pack = healthIter.next();
 			pack.draw(batch);
 		}
 	}
 }
