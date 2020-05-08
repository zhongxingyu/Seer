 package summative2013.lifeform;
 
 import summative2013.Summative.TERRAIN;
 import summative2013.phenomena.Weather.WEATHER;
 
 /**
  * Parent class of immobile food generating autotrophic benevolent food chain
  * base
  *
  * @author 322303413
  */
 public abstract class Vegetable extends Lifeform {
 
     /**
      * Stores current level of being eaten regenerates over time dies at 0
      */
     protected int health;
     /**
      * Maximum amount of healthy
      */
     protected int maxHealth;
     /**
      * Stores amount of happy life sustaining UV solar fusion glory praise the
      * sun required for life
      */
     protected int sun;
     /**
      * Amount of time to regenerate and produce more foodstuffs
      */
     protected int regenTime;
     /**
      * How close it is to regenerating
      */
     protected int regenCounter;
     /**
      * Maximum amount of food at any time, regeneration fills to max
      */
     protected int capacity;
     /**
      * Stores amount of food currently on plant
      */
     protected int current;
     /**
      * How long the plant requires to reproduce
      */
     protected int reproTime;
 
     /**
      * Default
      */
     public Vegetable() {
         super();
         health = 99;
         sun = 50;
     }
 
     /**
      * Will regenerate all the food and then reset the counter
      */
     protected void regenerate() {
         current = capacity;
         regenCounter = regenTime;
     }
 
     @Override
     /**
      * Changes variables depending on weather
      */
     public void act(WEATHER weather) {
        findWater();
         if (weather == WEATHER.SUN) {
             regenCounter = regenCounter - 2;
             sun = sun + 20;
             thirst = thirst + 2;
             reproTime = reproTime - 2;
         } else if (weather == WEATHER.RAIN) {
             regenCounter = regenCounter - 1;
             thirst = thirst - 5;
             sun = sun - 1;
             reproTime = reproTime - 2;
         } else if (weather == WEATHER.CLOUD) {
             regenCounter = regenCounter - 1;
             thirst = thirst + 1;
             sun = sun - 1;
             reproTime = reproTime - 1;
         } else if (weather == WEATHER.NIGHT) {
             thirst = thirst + 1;
             sun = sun - 1;
         }
 
         if (summative.terrainGet(location) == TERRAIN.SEA) {
             thirst = -1;
         } else if (Math.abs(water.x - location.x) <= 1 && Math.abs(water.y - location.y) <= 1
                 && Math.abs(water.x - location.x) + Math.abs(water.y - location.y) < 2) {
             if (thirst > 0) {
                 thirst = 0;
             }
         }
         if (sun < 0) {
             alive = false;
         } else if (thirst > 99 || thirst < -50) {
             alive = false;
         } else if (health < 0) {
             alive = false;
         }
 
         if (health < 0) {
             alive = false;
         } else {
             health = health + 5;
             if (health > maxHealth) {
                 health = maxHealth;
             }
         }
 
         if (reproTime <= 0) {
             reproduce();
         }
 
         if (alive = false) {
             suicide();
         }
     }
 
     /**
      * Changes the health
      *
      * @param change
      */
     public void changeHealth(int change) {
         health = health + change;
     }
 
     /**
      * Changes the current fruit or what have you
      *
      * @param change
      */
     public void changeCurrent(int change) {
         current = current + change;
     }
 
     /**
      * Returns the current fruit
      *
      * @return
      */
     public int getCurrent() {
         return current;
     }
 }
