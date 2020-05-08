 package com.gamedev.decline;
 
 import java.util.Iterator;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.utils.Array;
 import java.util.Random;
 
 /*
  * The class that manages all of the enemies
  */
 public class EnemyManager {
 
 	// the maximum number of enemies at once
 	private int maxEnemyNumber = 15;
 	//the array that stores all of the enemy objects
 	private Enemy[] enemyArray = new Enemy[maxEnemyNumber];
 	//the array that stores all of the enemies currently on screen
 	private Array<Enemy> currentEnemies = new Array<Enemy>();
 	//the current enemy count; this number always increases
 	private int currentEnemyNumber = 0;
 	//an iterator over the currentEnemies
 	private Iterator<Enemy> enemyIter;
 	//the object representing the enemy being used at the moment
 	private Enemy currentEnemy;
 	//a random number generator
 	private Random rand = new Random();
 	//the position where a new enemy will be generated
 	private int newEnemyXPosition = Gdx.graphics.getWidth()/2 + rand.nextInt(100);
 	//the singleton holding the global variables for the game
 	private GlobalSingleton gs = GlobalSingleton.getInstance();
 	
 	/*
 	 * the defualt constructor for the class
 	 * 
 	 * texture - the image of the enemies
 	 */
 	public EnemyManager(Texture texture) 
 	{
 		for(int i = 0; i < maxEnemyNumber; i++)
 		{
 			enemyArray[i] = new Enemy(texture);
 		}
 	}
 	
 	/*
 	 * the method that makes enemies appear
 	 * 
 	 * it adds an enemy from the enemyArray to the currentEnemies array and
 	 * increments the enemy count.
 	 */
 	public void makeEnemyAppear()
 	{
 		currentEnemies.add(enemyArray[currentEnemyNumber % maxEnemyNumber]);
 		currentEnemyNumber++;
 	}
 	
 	/*
 	 * Moves the enemy objects.  Also determines if enemy objects need to 
 	 * be removed from the scene and does so accordingly
 	 */
 	public void update()
 	{
 		//System.out.println("New Enemy Position: "+newEnemyXPosition);
 		if(gs.getHeroXPos() > newEnemyXPosition)
 		{
 			makeEnemyAppear();
 			newEnemyXPosition += rand.nextInt(400)+50;
 		}
 		enemyIter = currentEnemies.iterator();
 		while(enemyIter.hasNext())
 		{
 			currentEnemy = enemyIter.next();
 			currentEnemy.update();
			if(currentEnemy.getXPos() < 0)
 			{
 				currentEnemy.setToStartingPosition();
 				enemyIter.remove();
 			}
 		}
 		
 	}
 	
 	/*
 	 * draws the enemies that are currently displayed
 	 */
 	public void draw(SpriteBatch batch)
 	{
 		enemyIter = currentEnemies.iterator();
 		while(enemyIter.hasNext())
 		{
 			currentEnemy = enemyIter.next();
 			currentEnemy.setPosition(currentEnemy.getXPos(), currentEnemy.getYPos());
 			currentEnemy.draw(batch);
 		}
 	}
 	
 	/*
 	 * returns an iterator over the enemies currently displayed
 	 */
 	public Iterator<Enemy> getCurrentEnemiesIter()
 	{
 		return currentEnemies.iterator();
 	}
 
 }
