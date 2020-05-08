 package game;
 
 import java.util.*;
 
 import obstacles.CircleObstacle;
 import obstacles.Obstacle;
 import obstacles.RectObstacle;
 
 
 
 
 public class Wall {
 	private int maxNumObstacles;
 	private int maxDiffObstacles = 2;
 	private ArrayList<Obstacle> obstacles;
 	private SnakeBoundary snake;
 
 	private Random r = new 	Random();
 	/**
 	 * 
 	 * @param n the max amount of obstacles on the screen at any given time
 	 */
 	public Wall(int n, SnakeBoundary snake){
 		obstacles = new ArrayList<Obstacle>();
 		maxNumObstacles = n;
 		this.snake = snake;
 	}
 	
 	/**
 	 * Create a random obstacle at a random location on the screen
 	 * 
 	 */
 	public void createObstacle(){
 		int num = 0;
 		int dur = 5;
		float x = r.nextFloat() * 300;
		float y = r.nextFloat() * 300;
 		float r = 30;
 		
 		num =  (((int )Math.random()*100)%maxDiffObstacles);
 		switch(num){
 		case 0: 	obstacles.add(new CircleObstacle(dur, x, y , r, snake));		break;
 		case 1: 	obstacles.add(new RectObstacle(dur, x, y, r, snake));			break;
 		}
 		
 	}
 	
 	/**
 	 * Delete an obstacle (after it passes the snake without collision)
 	 *
 	 */
 	public void deleteObstacle( Obstacle o){
 		obstacles.remove(o);
 	}
 	
 	public ArrayList<Obstacle> getObstacles(){
 		return obstacles;
 	}
 
 }
