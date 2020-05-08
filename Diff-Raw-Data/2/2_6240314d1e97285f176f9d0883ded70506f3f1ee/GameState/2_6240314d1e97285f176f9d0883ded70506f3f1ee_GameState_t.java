 package com.thu9group.snake;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 import java.util.Random;
 
 import android.widget.RadioButton;
 
 
 public class GameState {
 	
 	//constants
 	public static final int RUNNING = 1;
 	public static final int PAUSED = 0;
 		
 	public static final int UP = 0;
 	public static final int DOWN = 1;
 	public static final int LEFT = 2;
 	public static final int RIGHT = 3;
 	
 	public static final int EASY = 1;
 	public static final int MEDIUM = 2;
 	public static final int HARD = 3;
 	
 	//configuration options
 	public static final int X_COUNT = 14;
 	public static final int Y_COUNT = 22;
 	
 	//the percentage chance of spawning an obstacle every cycle
 	public static final double OBSTACLE_SPAWN_CHANCE_EASY = 0;
 	public static final double OBSTACLE_SPAWN_CHANCE_MEDIUM = 3; 
 	public static final double OBSTACLE_SPAWN_CHANCE_HARD = 8;
 	
 	public static final double INCR_SPAWN_CHANCE_EASY = 0.8;
 	public static final double INCR_SPAWN_CHANCE_MEDIUM = 0.5;
 	public static final double INCR_SPAWN_CHANCE_HARD = 0.25;
 	
 	public static final double DECR_SPAWN_CHANCE_EASY = 0;
 	public static final double DECR_SPAWN_CHANCE_MEDIUM = 0.4;
 	public static final double DECR_SPAWN_CHANCE_HARD = 0.22;	
 	
 	public static final int SIZE_INCREASE_DURATION = 50;
 	public static final int SIZE_MULTIPLIER = 3;
 	public static final int SIZE_DECREASE_AMT = 3;
 	
 	public static final int FOOD_LIFETIME = 100;
 	public static final int OBSTACLE_LIFETIME = 50;
 	public static final int POWERUP_LIFETIME = 50;
 	
 	
 	public static final double HARD_SCORE_MULTIPLIER = 3;
 	public static final double MEDIUM_SCORE_MULTIPLIER = 2;
 	public static final double EASY_SCORE_MULTIPLIER = 1;	
 	
 	//other variables
     public long delay;
     public static int score;
     private static final Random RNG = new Random();
     
     private int direction = UP;
     private int nextDirection = UP;
     
     private int difficulty = 2;
 	private boolean wallsEnabled = true;
     public Grid activity;
     public int state;
    
     
 
     private ArrayList<Coordinate> snakeList = new ArrayList<Coordinate>();
     private ArrayList<Feature> featureList = new ArrayList<Feature>();
 	private boolean gameOver = false;
 	private int turn = 0;
 	private int sizeIncrease = 0;
 	private int superGrow = 0;
 	private double scoreMultiplier = 0.0;
     
     public GameState(Grid activity) {
     	this.activity = activity;
     	
 
         // For now we're just going to load up a short default eastbound snake
         // that's just turned north
         snakeList.add(new Coordinate(7, 7, Coordinate.CORNER4));
         snakeList.add(new Coordinate(6, 7, Coordinate.RIGHT));
         snakeList.add(new Coordinate(5, 7, Coordinate.RIGHT));
         snakeList.add(new Coordinate(4, 7, Coordinate.RIGHT));
         snakeList.add(new Coordinate(3, 7, Coordinate.RIGHT));
         snakeList.add(new Coordinate(2, 7, Coordinate.RIGHT));
       
         direction = UP;
         difficulty = getLevel();
         
         if (difficulty == EASY) {
         	wallsEnabled = false;
         	
         	delay = 280;
         	scoreMultiplier = EASY_SCORE_MULTIPLIER;
         } else if (difficulty == MEDIUM){
         	wallsEnabled = true;
         	delay = 225;
         	scoreMultiplier = MEDIUM_SCORE_MULTIPLIER;
         } else {
         	wallsEnabled = true;
         	delay = 200;
         	scoreMultiplier = HARD_SCORE_MULTIPLIER;
         }
 
         
         System.err.println("Delay: "+ delay	);
         score = 0;
         state = RUNNING;
     }
     
     public ArrayList<Coordinate> getSnakeList() {
     	return snakeList;
     }
     
     public ArrayList<Feature> getFeatureList() {
     	return featureList;
     }
 
 	
 	
 	/*
 	 * Update direction of snake but do not allow snake to turn back on itself
 	 * Potential issue for snake to turn back on itself by doing quick 360
 	 */
 	public void updateDirection(int d) {
 		if(d == direction) {
 			; // do nothing
 		} else {
 			if((direction != UP) && (d == DOWN))
 				nextDirection = d;
 			else if((direction != DOWN) && (d == UP))
 				nextDirection = d;
 			else if((direction != LEFT) && (d == RIGHT))
 				nextDirection = d;
 			else if((direction != RIGHT) && (d == LEFT))
 				nextDirection = d;	
 		}
 	}  
     
     
   
     private int getLevel(){
 		String output=" ";
 		int level = MEDIUM;
 		try{
 			File file = activity.getFileStreamPath("level.txt");
 			if (file.exists()){
 				FileInputStream fis = new FileInputStream(file);
 				InputStreamReader isr = new InputStreamReader(fis);
 				BufferedReader br = new BufferedReader(isr);
 				String aLine = null;
 				output = " ";
 				while ((aLine=br.readLine())!=null){
 					output+=aLine;
 					
 				}
 				System.err.println("output"+output);
 				br.close();
 			}
 			
 		}  catch (IOException e){
 				e.printStackTrace();
 		} catch (Exception e2) {
 			e2.printStackTrace();
 		}
 
 		if (output.contains("3")){
 			level = HARD;
 		}else if (output.contains("1")){
 			level = EASY;
 		}
 		return level;
 	}
    
 
 	
 	/**
 	 * this is called whenever it is time for the snake to move one position forward
 	 */
 	public void cycle() {
 		if(state == PAUSED) {
 			return;
 		}
 		turn++;
 		
 		if (sizeIncrease > 0) {
 			sizeIncrease-- ;
 		} else {
 			superGrow = 0;
 		}
 		
 		
 		Coordinate oldHead = snakeList.get(0);
 		
 		Coordinate newHead = calcNewHead(oldHead);
 		
 		// Check for collision with wall
		if(newHead.x < 0 || newHead.x >= X_COUNT || newHead.y < 0 || newHead.y >= Y_COUNT) {
 			gameOver = true;
 			return;
 		}
 		
 		// Check for collision with itself
 		if(snakeList.contains(newHead)) {
 			gameOver = true;
 			return;
 		}
 		
 
 		updateSnake();
 		updateFeatures();
 
 	}
 	
 	private void updateFeatures() {
 		int size = 0;
 		for (Feature f : featureList) {
 			if (f.type == Feature.FOOD) size++;
 		}
 		while(size < 2) {
 	    	Coordinate c = generateRandomCoordinate();
 	    	featureList.add(new Feature(c, Feature.FOOD, FOOD_LIFETIME));
 			size++;
 		}
 		
 		//delete any features that have expired
 		ArrayList<Feature> toRemove = new ArrayList<Feature>();
 		for (Feature f : featureList) {
 			if (f.cyclesRemaining >= 0) {
 				f.cyclesRemaining--;
 				if (f.cyclesRemaining == 0) {
 					toRemove.add(f);
 				}
 			}
 		}
 		for (Feature f : toRemove) {
 			featureList.remove(f);
 		}
 		
 		//generate random obstacle
 		double rnd = RNG.nextDouble() * 100;
 		double spawnChance = OBSTACLE_SPAWN_CHANCE_EASY;
 		if (difficulty == MEDIUM) {
 			spawnChance = OBSTACLE_SPAWN_CHANCE_MEDIUM;
 		} else if (difficulty == HARD) {
 			spawnChance = OBSTACLE_SPAWN_CHANCE_HARD;
 		}
 		
 		if (rnd <= spawnChance) {
 	    	Coordinate c = generateRandomCoordinate();
 	    	featureList.add(new Feature(c, Feature.OBSTACLE, OBSTACLE_LIFETIME));
 		}
 		
 		//generate random powerup;
 		rnd = RNG.nextDouble() * 100;
 		spawnChance = INCR_SPAWN_CHANCE_EASY;
 		if (difficulty == MEDIUM) {
 			spawnChance = INCR_SPAWN_CHANCE_MEDIUM;
 		} else if (difficulty == HARD) {
 			spawnChance = INCR_SPAWN_CHANCE_HARD;
 		}
 		
 		if (rnd <= spawnChance) {
 	    	Coordinate c = generateRandomCoordinate();
 	    	featureList.add(new Feature(c, Feature.SIZE_INCREASE, POWERUP_LIFETIME));
 		}
 		rnd = RNG.nextDouble() * 100;
 		spawnChance = DECR_SPAWN_CHANCE_EASY;
 		if (difficulty == MEDIUM) {
 			spawnChance = DECR_SPAWN_CHANCE_MEDIUM;
 		} else if (difficulty == HARD) {
 			spawnChance = DECR_SPAWN_CHANCE_HARD;
 		}
 		if (rnd <= spawnChance) {
 	    	Coordinate c = generateRandomCoordinate();
 	    	featureList.add(new Feature(c, Feature.SIZE_DECREASE, POWERUP_LIFETIME));
 		}
 				
 		
 	}
 	
 	/**
 	 * Generates a random coordinate that isn't occupied by a snake or a feature.
 	 * The random coordinate also cannot be within 5 squares or less of the snake head.
 	 * @return
 	 */
 	private Coordinate generateRandomCoordinate() {
 		boolean illegal = true;
 		Coordinate c=null;
 		while (illegal == true) {
 			int x = RNG.nextInt(X_COUNT);
 			int y = RNG.nextInt(Y_COUNT);
 			c = new Coordinate(x,y);
 			illegal = false;
 			for (Feature f : featureList) {
 				if ((f.coordinate.x == x) && (f.coordinate.y == y)) {
 					illegal = true;
 				}
 			}
 			for (Coordinate c2 : snakeList) {
 				if ((c2.x == x) && (c2.y == y)) {
 					illegal = true;
 				}
 			}
 			Coordinate snakeHead = snakeList.get(0);
 			for (int i = 0; i<10; i++) {
 				for (int j = 0; j<10; j++) {
 					int x2 = snakeHead.x + i - 5;
 					int y2 = snakeHead.y + j - 5;
 					if (x2 < 0) {
 						x2 = X_COUNT + x2;
 					}
 					if (y2 < 0) {
 						y2 = Y_COUNT + y2;
 					}
 					
 					if ((x == x2) && (y == y2)) {
 						illegal = true;
 					}
 				}
 			}		
 			
 		}
 		return c;
 	}
 	
 	public boolean isGameOver() {
 		return gameOver;
 	}
 
 
 
 	private Coordinate calcNewHead(Coordinate oldHead) {
 		Coordinate newHead = new Coordinate(0,0);
 		
 		int oldDirection = direction;
 		direction = nextDirection;
 		
 		if(direction == UP) {
 			newHead.x = oldHead.x;
 			newHead.y = oldHead.y - 1;
 			newHead.orientation = Coordinate.UP;
 			if(oldDirection == LEFT) {
 				oldHead.orientation = Coordinate.CORNER3;
 			} else if (oldDirection == RIGHT) {
 				oldHead.orientation = Coordinate.CORNER4;
 			}
 		} else if (direction == DOWN) {
 			newHead.x = oldHead.x;
 			newHead.y = oldHead.y + 1;
 			newHead.orientation = Coordinate.DOWN;
 			if(oldDirection == LEFT) {
 				oldHead.orientation = Coordinate.CORNER1;
 			} else if (oldDirection == RIGHT) {
 				oldHead.orientation = Coordinate.CORNER2;
 			}
 		} else if (direction == LEFT) {
 			newHead.x = oldHead.x - 1;
 			newHead.y = oldHead.y;
 			newHead.orientation = Coordinate.LEFT;
 			if(oldDirection == UP) {
 				oldHead.orientation = Coordinate.CORNER2;
 			} else if (oldDirection == DOWN) {
 				oldHead.orientation = Coordinate.CORNER4;
 			}
 		} else {
 			newHead.x = oldHead.x + 1;
 			newHead.y = oldHead.y;
 			newHead.orientation = Coordinate.RIGHT;
 			if(oldDirection == UP) {
 				oldHead.orientation = Coordinate.CORNER1;
 			} else if (oldDirection == DOWN) {
 				oldHead.orientation = Coordinate.CORNER3;
 			}
 		}
 		
 		if(!wallsEnabled) {
 			if(newHead.x < 0)
 				newHead.x = X_COUNT - 1;
 			else if (newHead.x >= X_COUNT)
 				newHead.x = 0;
 			if(newHead.y < 0)
 				newHead.y = Y_COUNT - 1;
 			else if (newHead.y >= Y_COUNT)
 				newHead.y = 0;
 		}
 		return newHead;
 	}
 	
 	private void updateSnake() {
 		Coordinate oldHead = snakeList.get(0);
 		Coordinate newHead = calcNewHead(oldHead);
 		
 		snakeList.add(0, newHead);
 		boolean grow = false;
 		for(int i = 0 ; i < featureList.size() ; i++) {
 			Feature f = featureList.get(i);
 			
 			// Check for apple collision
 			if ((f.coordinate.equals(newHead)) && (f.type == Feature.FOOD)) {
 				grow = true;
 				if (sizeIncrease > 0) {
 					superGrow = SIZE_MULTIPLIER;
 					score += (SIZE_MULTIPLIER - 1) * (scoreMultiplier);
 				}
 				featureList.remove(i);
 		    	score += (1 * scoreMultiplier);				
 				if((delay > 144) && (difficulty == HARD)) {
 					delay = delay - 6;
 				}
 				break;
 			}
 			
 			//check for size increase collision
 			if ((f.coordinate.equals(newHead)) && (f.type == Feature.SIZE_INCREASE)) {
 				featureList.remove(i);
 				sizeIncrease = SIZE_INCREASE_DURATION;
 
 				break;
 			}
 			
 			//check for size decrease collision
 			if ((f.coordinate.equals(newHead)) && (f.type == Feature.SIZE_DECREASE)) {
 				featureList.remove(i);
 				int count = SIZE_DECREASE_AMT;
 				while (count > 0) {
 					if (snakeList.size() >= 5) {
 						snakeList.remove(snakeList.size() - 1);
 					}
 					count--;
 				}
 
 				break;
 			}
 			
 			//check for obstacle collision
 			if ((f.coordinate.equals(newHead)) && (f.type == Feature.OBSTACLE)) {
 				gameOver = true;
 
 				break;
 			}
 		}
 		
 
 		
 		
 		if ((grow == false) && (superGrow == 0))  {
 			snakeList.remove(snakeList.size() -1);
 		}
 		if (superGrow > 0) {
 			superGrow--;
 		}
 		
 	}
     
 }
