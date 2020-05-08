 package game;
 
 import gui.MenuGUI;
 import java.util.Random;
 
 /**
  *
  * @author Nikolaos Bukas, Anthony Chin
  */
 public class Difficulty {
 
     final private static int INITIAL_BONUS_DROP_RATE = 30;
     final private static int BOMB_DROP_RATE = 10;
     final private static int LIFE_DROP_RATE = 30;
     final private static int SHIELD_ONE_DROP_RATE = 30;
     final private static int SHIELD_TWO_DROP_RATE = 18;
     final private static int SHIELD_THREE_DROP_RATE = 12;
     final private static int ASTEROID_SPAWN_RATE = 50;
     final private static int LARGE_ASTEROID_SPAWN_RATE = 50;
     final private static int MEDIUM_ASTEROID_SPAWN_RATE = 30;
     final private static int SMALL_ASTEROID_SPAWN_RATE = 20;
     final private static int ALIEN_SPAWN_RATE = 20;
     final private static int INITIAL_ASTEROID_SPEED = 20;
     final private static int INITIAL_ALIEN_SPEED = 10;
     final private static int MAX_LEVEL = 50;
     final private static Random rand = new Random();
 
     public static float randomHeading() {
         return randomFloat() * 360;
     }
 
     public static float randomAsteroidVelocity(int level) {
         float ratio = (float) level / MAX_LEVEL;
         int maxVal = (int) (ratio * INITIAL_ASTEROID_SPEED);
        return randomFloat() * (randomInt(1, maxVal));
     }
 
     public static int randomAsteroidSize() {
         int val = randomInt(0, 100);
         if (val <= SMALL_ASTEROID_SPAWN_RATE) {
             return Asteroid.SMALL_ASTEROID_SIZE;
         } else if (val <= SMALL_ASTEROID_SPAWN_RATE + MEDIUM_ASTEROID_SPAWN_RATE) {
             return Asteroid.MEDIUM_ASTEROID_SIZE;
         } else {
             return Asteroid.LARGE_ASTEROID_SIZE;
         }
     }
 
     public static float randomAlienVelocity() {
         return randomFloat() * randomInt(1, 10);
     }
 
     public static int randomXPos() {
 //        return rand.nextInt(MenuGUI.WIDTH);
         return randomInt(0, MenuGUI.WIDTH * 2);
     }
 
     public static int randomYPos() {
 //        return rand.nextInt(MenuGUI.HEIGHT);
         return randomInt(0, MenuGUI.HEIGHT * 2);
     }
 
     public static String bonusDropRate() {
         return null;
     }
 
     public static boolean spawnAlien() {
         return randomInt(0, 100) <= ALIEN_SPAWN_RATE;
     }
 
     public static int spawnAsteroids(int level) {
         float val = (float) level / ASTEROID_SPAWN_RATE;
         return (int) (val * 100);
     }
 
     public static float randExplosionVelocity() {
         return (rand.nextInt(5) + 1) * (rand.nextFloat() - rand.nextFloat());
     }
 
     private static float randomFloat() {
         return rand.nextFloat() * 2 - 1;
     }
     
     private static int randomInt(int min, int max)
     {
         return rand.nextInt(max - min) + min;
     }
 }
