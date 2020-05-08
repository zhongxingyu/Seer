 package game;
 
 import gui.MenuGUI;
 import java.util.Random;
 
 /**
  * The <code>Difficulty</code> class is responsible for
  * generating the velocity, heading and other in game
  * parameters of various objects based on the
  * current level.
  * @author Nikolaos Bukas, Anthony Chin
  */
 public class Difficulty {
 
     final private static int INITIAL_BONUS_DROP_RATE = 30;
     final private static int BOMB_DROP_RATE = 10;
     final private static int LIFE_DROP_RATE = 20;
     final private static int SHIELD_ONE_DROP_RATE = 100;
     final private static int SHIELD_TWO_DROP_RATE = 70;
     final private static int SHIELD_THREE_DROP_RATE = 40;
     final private static int ASTEROID_SPAWN_RATE = 100;
     final private static int LARGE_ASTEROID_SPAWN_RATE = 50;
     final private static int MEDIUM_ASTEROID_SPAWN_RATE = 30;
     final private static int SMALL_ASTEROID_SPAWN_RATE = 20;
     final private static int ALIEN_SPAWN_RATE = 20;
     final private static int INITIAL_ASTEROID_SPEED = 20;
     final private static int INITIAL_ALIEN_SPEED = 10;
    final private static int MAX_LEVEL = 20;
     final private static Random rand = new Random();
 
     /**
      * Generates random heading value.
      *
      * @return random heading value
      */
     public static float randomHeading() {
         return randomFloat() * 360;
     }
 
     /**
      * Generates random velocity for asteroid.
      *
      * @param level current level of game
      * @return random velocity for asteroid
      */
     public static float randomAsteroidVelocity(int level) {
         float ratio = (float) level / MAX_LEVEL;
         int maxVal = (int) (ratio * INITIAL_ASTEROID_SPEED);
        return randomFloat() * (randomInt(1, INITIAL_ASTEROID_SPEED));
     }
 
     /**
      * Generates random size for asteroid.
      *
      * @return random size for asteroid
      */
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
 
     /**
      * Generates random velocity for alien.
      *
      * @return random velocity for alien
      */
     public static float randomAlienVelocity() {
        return randomFloat() * randomInt(1, 10);
     }
 
     /**
      * Generates random X position.
      *
      * @return new X position
      */
     public static int randomXPos() {
 //        return rand.nextInt(MenuGUI.WIDTH);
         return randomInt(0, MenuGUI.WIDTH * 2);
     }
 
     /**
      * Generates random Y position.
      *
      * @return new Y position
      */
     public static int randomYPos() {
 //        return rand.nextInt(MenuGUI.HEIGHT);
         return randomInt(0, MenuGUI.HEIGHT * 2);
     }
 
     /**
      * Returns the rate that bonus drop appears.
      *
      * @return rate of bonus drop
      */
     public static int bonusDropRate() {
         int val = randomInt(0, 100);
         if (val <= INITIAL_BONUS_DROP_RATE) {
             val = randomInt(0, 100);
             if (val <= BOMB_DROP_RATE) {
                 return BonusDrop.BOMB_BONUS_DROP;
             } else if (val <= LIFE_DROP_RATE) {
                 return BonusDrop.LIFE_BONUS_DROP;
             } else if (val <= SHIELD_THREE_DROP_RATE) {
                 return BonusDrop.SHIELD_THREE_POINTS_DROP;
             } else if (val <= SHIELD_TWO_DROP_RATE) {
                 return BonusDrop.SHIELD_TWO_POINTS_DROP;
             } else {
                 return BonusDrop.SHIELD_ONE_POINT_DROP;
             }
         } else {
             return -1;
         }
     }
 
     /**
      * Returns a random boolean value that determines whether to spawn alien or
      * not.
      *
      * @return randomly generated boolean value
      */
     public static boolean spawnAlien() {
         return randomInt(0, 100) <= ALIEN_SPAWN_RATE;
     }
 
     /**
      * Determines the number of asteroid that needs to be spawned.
      *
      * @param level current game level
      * @return number of asteroid
      */
     public static int spawnAsteroids(int level) {
         float val = (float) level / ASTEROID_SPAWN_RATE;
         return (int) (val * 100);
     }
 
     /**
      * Generates random explosion velocity.
      *
      * @return new random explosion velocity
      */
     public static float randExplosionVelocity() {
         return (rand.nextInt(5) + 1) * (rand.nextFloat() - rand.nextFloat());
     }
 
     private static float randomFloat() {
         return rand.nextFloat() * 2 - 1;
     }
 
     private static int randomInt(int min, int max) {
         return rand.nextInt(max - min) + min;
     }
 }
