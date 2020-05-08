 // Package Declaration //
 package com.gamedev.decline;
 
 // Java Package Support //
 import java.util.Iterator;
 import java.util.Random;
 import java.util.ArrayList;
 
 // Badlogic Package Support //
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.utils.Array;
 
 /**
  * 
  * com/gamedev/decline/EnemyManager.java
  * 
  * @author(s) : Ian Middleton, Zach Coker, Zach Ogle
  * @version : 2.0 Last Update : 3/25/2013 Update By : Ian Middleton
  * 
  *          Source code for the EnemyManager class. The EnemyManager class takes
  *          care of creating, updating, drawing, and reallocating Enemy objects.
  * 
  */
 public class EnemyManager {
 
 	// Global Singleton //
 	private GlobalSingleton gs = GlobalSingleton.getInstance();
 
 	// Constants for the Object //
 	public static final int MAX_ENEMIES = 15;
 	public static final int MAX_FALCONERS = 8;
 
 	// Internal Variables //
 	private Enemy[] enemies = new Enemy[MAX_ENEMIES];
 	private Falconer[] falconers = new Falconer[MAX_FALCONERS];
 	private Array<Enemy> currentEnemies = new Array<Enemy>();
 	private Array<Falconer> currentFalconers = new Array<Falconer>();
 	private Iterator<Enemy> enemyIter;
 	private Iterator<Falconer> falconerIter;
 	private Enemy currentEnemy;
 	private Falconer currentFalconer;
 	private int currentEnemyNumber = 0;
 	private int currentFalconerNumber = 0;
 	private Random rand = new Random();
 	private int newEnemyXPosition = Gdx.graphics.getWidth() / 2 + rand.nextInt(100);
 	private int newFalconerXPosition = Gdx.graphics.getWidth() / 2 + rand.nextInt(400);
 	Texture enemyTexture, falconerTexture, falconerWithoutFalconTexture, falconTexture;
 
 	/**
 	 * Instantiates a new EnemyManager object. The EnemyManager fills an array
 	 * of new Enemy objects with the given Texture to be used in the game. This
 	 * is done to create a buffer of Enemy objects.
 	 * 
 	 * @param texture
 	 *            : The image to be used for the Enemy objects.
 	 */
 	public EnemyManager(Texture enemyTexture, Texture falconerTexture, Texture falconerWithoutFalconTexture, Texture falconTexture)
 	{
 		this.enemyTexture = enemyTexture;
 		this.falconerTexture = falconerTexture;
 		this.falconerWithoutFalconTexture = falconerWithoutFalconTexture;
 		this.falconTexture = falconTexture;
 		for (int i = 0; i < MAX_ENEMIES; i++) {
 			enemies[i] = new Enemy(enemyTexture);
 		} // end for
 		for (int i = 0; i < MAX_FALCONERS; i++)
 		{
 			falconers[i] = new Falconer(falconerTexture, falconerWithoutFalconTexture, falconTexture);
 		}
 	} // end EnemyManager
 
 	/**
 	 * Gets all currently active Enemies.
 	 * 
 	 * @return : A badlogic array of all the active Enemies.
 	 */
 	public Array<Enemy> getActiveEnemies() {
 		return currentEnemies;
 	}// end getActiveEnemies()
 	
 	/**
 	 * Gets all currently active Falconers.
 	 * 
 	 * @return : A badlogic array of all active Falconers.
 	 */
 	public Array<Falconer> getActiveFalconers()
 	{
 		return currentFalconers;
 	}
 
 	/**
 	 * Handles an enemy being damaged
 	 * @param index - the index of the enemy in the displayed enemy
 	 *       array
 	 * @param damage - the amount that the enemy was damaged
 	 */
 	public boolean enemyDamagedEvent(Enemy enemy, int damage)
 	{
 		currentEnemy = enemy;
 	  if(!currentEnemy.getHasHealthBar())
 	  {
 	    gs.getHealthBarManager().add(currentEnemy);
 	    currentEnemy.setHasHealthBar(true);
 	  }
 	  currentEnemy.setHealth(currentEnemy.getHealth() - damage);
 	  if(!currentEnemy.getIsAlive())
 	  {
 	    return true;
 	  }
 	  else
 	  {
 		  return false;
 	  }
 	}
 	
 	/**
 	 * Handles a Falconer being damaged.
 	 * @param index - The index of the Falconer in the displayed
 	 * 		Falconer array.
 	 * @param damage - The amount that the enemy was damaged.
 	 */
 	public boolean falconerDamagedEvent(Falconer falconer, int damage)
 	{
 		currentFalconer = falconer;
 		if (!currentFalconer.getHasHealthBar())
 		{
 			gs.getHealthBarManager().add(currentFalconer);
 			currentFalconer.setHasHealthBar(true);
 		}
 		currentFalconer.setHealth(currentFalconer.getHealth() - damage);
 		if (!currentFalconer.getIsAlive())
 		{
 			return true;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	/**
 	 * Removes a specific active Enemy from the group of active Enemies.
 	 * 
 	 * @param index
 	 *            : The index of the active Enemy to be removed.
 	 */
 	public void removeActiveEnemy(int index) {
 		currentEnemies.removeIndex(index);
 	}// end removeActiveEnemy()
 	
 	/**
 	 * Removes a specific active Falconer from the group of active Falconers.
 	 * 
 	 * @param index : The index of the active Falconer to be removed.
 	 */
 	public void removeActiveFalconer(int index)
 	{
 		currentFalconers.removeIndex(index);
 	}
 
 	/**
 	 * Grabs a Enemy from the Enemy buffer created when the manager was
 	 * constructed. Sets the initial values for this specific Enemy. This Enemy
 	 * is then added to the array of Enemy that are to be drawn to the screen
 	 * and updated.
 	 */
 	public void makeEnemyAppear() {
 		//currentEnemy = enemies[currentEnemyNumber % MAX_ENEMIES];
 		currentEnemy = new Enemy(enemyTexture);
 		currentEnemy.setToInitialDrawPosition();
 		currentEnemy.setIsAlive(true);
 		currentEnemy.setXPos(gs.getWorldXPos() + Enemy.START_XDRAW);
 		currentEnemy.setYPos(Enemy.START_YDRAW);
 		currentEnemies.add(currentEnemy);
 		currentEnemyNumber++;
 	}// end makeEnemyAppear()
 	
 	/**
 	 * Grabs a Falconer from the Falconer buffer created when the manager was
 	 * 	constructed. Sets the initial values for this specific Falconer. This
 	 * 	Falconer is then added to the array of Falconers that are to be drawn
 	 * 	to the screen and updated.
 	 */
 	public void makeFalconerAppear()
 	{
 		//currentFalconer = falconers[currentFalconerNumber % MAX_FALCONERS];
 		if (currentFalconers.size < MAX_FALCONERS)
 		{
 			currentFalconer = new Falconer(falconerTexture, falconerWithoutFalconTexture, falconTexture);
 			currentFalconer.setToInitialDrawPosition();
 			currentFalconer.setIsAlive(true);
 			currentFalconer.setXPos(gs.getWorldXPos() + Falconer.START_XDRAW);
 			currentFalconer.setYPos(Falconer.START_YDRAW);
 			currentFalconer.setFalconXPos(gs.getWorldXPos() + Falconer.START_XDRAW);
 			currentFalconer.setFalconYPos(Falconer.START_YDRAW);
 			currentFalconers.add(currentFalconer);
 			currentFalconerNumber++;
 		}
 	}
 
 	/**
 	 * Checks to see if the hero has moved farther than the random amount
 	 * required for a new enemy to be spawned. If the hero has moved this
 	 * distance than a new enemy is created. Afterwards, the function iterates
 	 * through all Enemies on the screen and calls their update method. If an
 	 * Enemy has traveled off screen after updating then that Enemy is removed.
 	 */
 	public void update() {
		if(gs.getHeroXPos() < 19000){
 			if (gs.getHeroXPos() > newEnemyXPosition) {
 				makeEnemyAppear();
 				newEnemyXPosition += rand.nextInt(400) + 50;
 			}// end if
 		}
 		enemyIter = currentEnemies.iterator();
 		while (enemyIter.hasNext()) {
 			currentEnemy = enemyIter.next();
 			currentEnemy.update();
 			if (currentEnemy.getX() < -1 * currentEnemy.getWidth()) 
 			{
 			   currentEnemy.setIsAlive(false);
 			}// end if
             if(!currentEnemy.getIsAlive())
             {
             	enemyIter.remove();                          
             }
 		}// end while
		if (gs.getHeroXPos() < 19000)
 		{
 			if (gs.getHeroXPos() > newFalconerXPosition)
 			{
 				makeFalconerAppear();
 				//makeFalconAppear();
 				newFalconerXPosition += rand.nextInt(500) + 300;
 			}
 		}
 		//System.out.println("Size of current falconers:"+currentFalconers.size);
 		falconerIter = currentFalconers.iterator();
 		while (falconerIter.hasNext())
 		{
 			currentFalconer = falconerIter.next();
 			currentFalconer.update();
 			if (currentFalconer.getX() < -1 * currentFalconer.getWidth())
 			{
 				currentFalconer.setIsAlive(false);
 			}
 			if (!currentFalconer.getIsAlive())
 			{
 				falconerIter.remove();
 			}
 		}
 	}// end update()
 
 	/**
 	 * Iterates through the array of Enemies to be drawn to the screen and calls
 	 * the draw function for each Enemy.
 	 * 
 	 * @param batch
 	 *            - The SpriteBatch object which will draw the Enemy objects.
 	 */
 	public void draw(SpriteBatch batch)
 	{
 		enemyIter = currentEnemies.iterator();
 		while (enemyIter.hasNext())
 		{
 			currentEnemy = enemyIter.next();
 			currentEnemy.draw(batch);
 		}
 		falconerIter = currentFalconers.iterator();
 		while (falconerIter.hasNext())
 		{
 			currentFalconer = falconerIter.next();
 			currentFalconer.draw(batch);
 		}
 	}
 }// end EnemyManager class
