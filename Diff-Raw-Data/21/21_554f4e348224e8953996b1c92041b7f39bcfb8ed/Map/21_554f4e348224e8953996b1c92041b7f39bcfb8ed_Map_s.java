 package com.me.mygdxgame;
 
 import java.util.Random;
 //hiasd
 
 public class Map {
     /*
      * Bird is only able to go 2 directions: left and right.
      *
      */
 
     private static int maxX = 5, maxY = 5, maxZ = 5;
 //Temporary numbers, also waiting for
 //position
     //stub
 
     public static Bird spawnBird() {
     	Bird bird = new Bird();
     	return bird;
     }
     
     public static Bird refreshBird(Bird bird){
     	bird.fly();
     	return bird;
     	
     }
 
     public static Pyramid spawnPyramids(int birdxStart, int birdxEnd) {
         Random rand = new Random();
         int x = 0, y = 0, z = 0;
         if (x == 0 || x <= birdxEnd || x >= birdxStart|| y == 0 || z == 0) {
             x = rand.nextInt(maxX) + 1;
             y = maxY;
             z = rand.nextInt(maxZ) + 1;
         }
        Pyramid pyramid = new Pyramid(0,0,5,5,0,5);
         
         //Need to write base case
 
         return pyramid;
     }
 
     public static Powerup spawnPowerups(int birdxStart, int birdxEnd) {
         Random rand = new Random();
        Powerup powerup = new Powerup();
         int x = 0, y = 0, z = 0;
         //Need to write base case
         if (x == 0 ||x <= birdxEnd || x >= birdxStart || y == 0 || z == 0) {
             x = rand.nextInt(maxX) + 1;
             y = maxY;
             z = rand.nextInt(maxZ) + 1;
         }
 
         return powerup;
     }
 
    public static Pyramid refreshPyramids(int pyramidy, Pyramid pyramid) {
         /*
          * Need the pyramids class
          */
        pyramidy--; //Pyramids move closer, bird stays the same.
 
         return pyramid; 
     }
 
     public static Powerup refreshPowerups(int powerupy, Powerup powerup) {
         /*
          * Need the power ups class
          */
        powerupy--; //Power ups move closer, bird stays the same.
         return powerup; 
     }
 }
 
