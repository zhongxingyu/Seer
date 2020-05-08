 package world;
 
 import java.awt.Rectangle;
 import java.util.ConcurrentModificationException;
 import java.util.LinkedList;
 import java.util.List;
 
 import world.creatures.Critter;
 import world.creatures.Food;
 
 /**
  * World.java
  * @author John Zavniak
  * Manage the Critters, Food, Genetics, and
  * everything else that exists in the world.
  */
 
 public class World
 {
     
     public static final int WORLD_WIDTH = 500;
     public static final int WORLD_HEIGHT = 500;
     
     private List<Critter> critters;
     private List<Food> food;
     
     private Genetics genetics;
     
     /**
      * Create a new world
      */
     public World()
     {
         critters = new LinkedList<Critter>();
         food = new LinkedList<Food>();
         genetics = new Genetics(this);
     }
     
     /**
      * Get all the Critters in the world
      * @return a list of all the Critters
      */
     public List<Critter> getCritters()
     {
         return critters;
     }
     
     /**
      * Get all the Food in the world
      * @return a list of all the Food
      */
     public List<Food> getFood()
     {
         return food;
     }
     
     /**
      * Draw a vector from one Rectangle to another
      * @param c the Rectangle to draw from
      * @param f the Rectangle to draw to
      * @return A vector from c to f
      */
     private Vector getVector(Rectangle c, Rectangle f)
     {
         double delX = f.x - c.x;
         double delY = f.y - c.y;
         return new Vector(Math.atan2(delY, delX), Math.sqrt(delX*delX + delY*delY));
     }
     
     /**
      * Set the critters list which contains the Critters in the world
      * @param critters the list of Critters to set the critters list equal to
      */
     public void setCritters(List<Critter> critters)
     {
         this.critters = critters;
     }
     
     /**
      * Get all the food in a specific Critter's field of view
      * @param c the Critter to use
      * @return A list containing all the food in a specific Critter's field of view
      */
     private List<Food> visibleFood(Critter c)
     {
         List<Food> visibleFood = new LinkedList<Food>();
         Vector velocity = c.getVelocity().normalized();
         for (Food f : food)
         {
             Vector v = getVector(c, f).normalized();
             double angle = velocity.angleBetween(v);
             if (angle <= Critter.FOV/2)
             {
                 visibleFood.add(f);
             }
         }
         return visibleFood;
     }
     
     /**
      * Get the closest piece of visible food to a specific Critter
      * @param c the Critter to use
      * @return the closest piece of Food to the Critter c
      */
     private Food getClosestVisibleFood(Critter c)
     {
         List<Food> visibleFood = visibleFood(c);
         Food closest = null;
         double cMag = Double.POSITIVE_INFINITY;
         for (Food f : visibleFood)
         {
             if (closest == null)
             {
                 closest = f;
                 cMag = getVector(c,f).getMag();
                 continue;
             }
             double mag = getVector(c,f).getMag();
             if (mag < cMag)
             {
                 closest = f;
                 cMag = mag;
             }
         }
         return closest;
     }
     
     /**
      * Update all the critters
      * @throws ConcurrentModificationException if two threads try to access the critters list at once
      */
     public void update() throws ConcurrentModificationException
     {
         if (genetics.isPopulating())
         {
             return;
         }
         for (Critter c : critters)
         {
             Vector v = c.getVelocity();
             if (Math.random() < c.getFoodAttractiveness())
             {
                 Food f = getClosestVisibleFood(c);
                if (f != null)
                 {
                    double dir = getVector(c, f).getDir();
                     if (!f.isHealthy())
                     {
                        dir = (dir + Math.random()*Math.PI*7/4) % (Math.PI * 2);
                     }
                     if (dir != 0)
                     {
                         v.setDir(dir);
                     }
                 }
             }
             c.delX(v.getX());
             c.delY(v.getY());
             for (Food f : food)
             {
                 if (c.contains(f))
                 {
                     f.x = (int) (Math.random() * World.WORLD_WIDTH);
                     f.y = (int) (Math.random() * World.WORLD_HEIGHT);
                 }
             }
         }
         try
         {
            Thread.sleep(20);
         }
         catch (InterruptedException e)
         {
             e.printStackTrace();
         }
     }
     
 }
