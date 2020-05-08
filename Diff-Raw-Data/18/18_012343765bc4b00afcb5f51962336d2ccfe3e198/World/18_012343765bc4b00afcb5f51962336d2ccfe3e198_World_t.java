 package com.androidgames.mrmunch;
 
 import java.util.Random;
 
 public class World {
     static final int WORLD_WIDTH = 16;
     static final int WORLD_HEIGHT = 16;
     
     static final int FRUIT_SCORE= 2;
     
     static final int FRUITS_FOR_ACCELERATE = 5;
     
     static final int FRUITS_FOR_EXTRA_FRUIT = 10;
    static final int EXTRA_FRUIT_DURATION = 25;
     
     static final float TICK_INITIAL = 0.5f;
     static final float TICK_DECREMENT = 0.2f;
 
     public Snake snake;
     public Fruit fruit;
     public Fruit extraFruit = null;
     private int fruitsEaten = 0;
     public boolean gameOver = false;;
     public int score = 0;
 
     boolean fields[][] = new boolean[WORLD_WIDTH][WORLD_HEIGHT];
     Random random = new Random();
     float tickTime = 0;
     static int v;
     static float tick;
     	
     int deltaTick = 0;
 
     public World() {
         snake = new Snake();
         v = (Settings.gameSpeed-1)*3; //The gameSpeed 1 is the v0
         calculateTick();
         placeFruit();
     }
 
     private void placeFruit() {
         for (int x = 0; x < WORLD_WIDTH; x++) {
             for (int y = 0; y < WORLD_HEIGHT; y++) {
                 fields[x][y] = false;
             }
         }
 
         int len = snake.parts.size();
         for (int i = 0; i < len; i++) {
             SnakePart part = snake.parts.get(i);
             fields[part.x][part.y] = true;
         }
 
         int fruitX = random.nextInt(WORLD_WIDTH);
         int fruitY = random.nextInt(WORLD_HEIGHT);
         while (true) {
             if (fields[fruitX][fruitY] == false)
                 break;
             fruitX += 1;
             if (fruitX >= WORLD_WIDTH) {
                 fruitX = 0;
                 fruitY += 1;
                 if (fruitY >= WORLD_HEIGHT) {
                     fruitY = 0;
                 }
             }
         }
         fruit = new Fruit(fruitX, fruitY, random.nextInt(3));
         fields[fruitX][fruitY] = true;
         
         if(fruitsEaten!=0 && (fruitsEaten % FRUITS_FOR_EXTRA_FRUIT)==0 
         		&& extraFruit == null) {
         	fruitX = random.nextInt(WORLD_WIDTH);
         	fruitY = random.nextInt(WORLD_HEIGHT);
         	while (true) {
                 if (fields[fruitX][fruitY] == false)
                     break;
                 fruitX += 1;
                 if (fruitX >= WORLD_WIDTH) {
                     fruitX = 0;
                     fruitY += 1;
                     if (fruitY >= WORLD_HEIGHT) {
                         fruitY = 0;
                     }
                 }
             }
         	extraFruit = new Fruit(fruitX, fruitY, Fruit.TYPE_4);
         }
     }
 
     public void update(float deltaTime) {
         if (gameOver)
             return;
 
         tickTime += deltaTime;
 
         while (tickTime > tick) {
             tickTime -= tick;
             snake.advance();
             if (snake.checkBitten()) {
                 gameOver = true;
                 return;
             }
 
             SnakePart head = snake.parts.get(0);
             if (head.x == fruit.x && head.y == fruit.y) {
                 score += Settings.gameSpeed*FRUIT_SCORE;
                 fruitsEaten++;
                 snake.eat();
                 if (snake.parts.size() == WORLD_WIDTH * WORLD_HEIGHT) {
                     gameOver = true;
                     return;
                 } else {
                 	if (fruitsEaten != 0 && (fruitsEaten % FRUITS_FOR_ACCELERATE)==0) {
                 		v++;
                 		calculateTick();
                     }
                     placeFruit();
                 }
             }
             if (extraFruit != null){
 	            if (head.x == extraFruit.x && head.y == extraFruit.y) {
 	            	score += 5 * Settings.gameSpeed * FRUIT_SCORE;
 	            	snake.shrink();
 	            	extraFruit = null;
 	            	deltaTick = 0;
 	            } else {
 	                if (deltaTick >= EXTRA_FRUIT_DURATION){
 	            	    extraFruit = null;
 	            	    deltaTick = 0;
 	                } else { 
 	            	    deltaTick++;
 	                }
 	            }
             }
         }
     }
     
     static public void calculateTick(){
     		tick = TICK_INITIAL/(1+v*TICK_DECREMENT);
     }
 }
