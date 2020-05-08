 package com.jeremyP.diseasedefense;
 
 import java.util.ArrayList;
 
 import com.jeremyP.diseasedefense.framework.Graphics;
 
 public class EnemyPool 
 {
 	private ArrayList<Enemy> enemyPool;
 	private Graphics g;
 	private int maxEnemies;
 	private int min;
 	private int maxX;
 	private int maxY;
 	private int enemySpeed;
 	
 	public EnemyPool(Graphics g, Character character, int maxEnemies, int enemySpeed)
 	{
 		this.maxEnemies = maxEnemies;
 		this.enemyPool = new ArrayList<Enemy>();
 		this.enemySpeed = enemySpeed;
 		this.g = g;
 		this.min = 0;
 		this.maxX = g.getWidth();
 		this.maxY = g.getHeight();
 		createEnemies(character);
 	}
 	
 	private void createEnemies(Character character)
 	{
 		for (int i = 0; i < maxEnemies; i++)
 		{
 			Enemy enemy = new Enemy(g, Assets.badGuys[0], enemySpeed, 1, 1);
 	     	
 	     	int xCoord = character.getCoords().getX();
 	 		int yCoord = character.getCoords().getY();
 	     	while(xCoord == character.getCoords().getX() && yCoord == character.getCoords().getY())
 	 		{
 	     	  xCoord = min + (int)(Math.random() * ((maxX - min) + 1));
 	 	      yCoord = min + (int)(Math.random() * ((maxY - min) + 1));
 	 		}
 	     	
 	     	
 	 	    //enemy.setCoords(xCoord, yCoord);
 	     	enemy.setCoords(0, 0);
 	 	    enemyPool.add(i, enemy);
 		}
 	}
 	
 	public Enemy getEnemy(int index)
 	{
 		if (index < enemyPool.size() - 1)
 		{
 		 return enemyPool.get(index);
 		}
 		
 		else
 		{
 			return null;
 		}
 	}
 	
 	public void newEnemy(Character character)
 	{
 		Enemy enemy = new Enemy(g, Assets.badGuys[0], enemySpeed, 1, 1);
      	
      	int xCoord = character.getCoords().getX();
  		int yCoord = character.getCoords().getY();
      	while(xCoord == character.getCoords().getX() && yCoord == character.getCoords().getY())
  		{
      	  xCoord = min + (int)(Math.random() * ((maxX - min) + 1));
  	      yCoord = min + (int)(Math.random() * ((maxY - min) + 1));
  		}
      	
 	    enemy.setCoords(xCoord, yCoord);
  	    enemyPool.add(enemy);
 	}
 }
