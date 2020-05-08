 package world.creatures;
 
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 
 import world.Vector;
 import world.World;
 
 /**
  * Critter.java
  * @author John Zavidniak
  * The Critters who learn how to find and avoid
  * certain foods
  */
 
 public class Critter extends Rectangle
 {
     
     public static final double FOV = Math.PI/2;
     public static final double VELOCITY = 5.0;
     public static final double GROWTH_RATE = 0.003;
     public static final int CRITTER_RADIUS = 12;
     private static final Color CRITTER_COLOR = Color.BLACK;
     private static final Color CRITTER_FINAL_COLOR = Color.GREEN;
     
     private int foodEaten;
     /*
      * How attracted a Critter is to healthy food,
      * and how unattracted a Critter is to unhealthy food
      */
     private double foodAttractiveness;
     private Vector velocity;
     
     /**
      * Create a new Critter at the given position
      * @param x the x-position of the new Critter
      * @param y the y-position of the new Critter
      */
     public Critter(int x, int y)
     {
         super(x, y, Critter.CRITTER_RADIUS, Critter.CRITTER_RADIUS);
         this.velocity = new Vector(Math.random() * Math.PI * 2, Critter.VELOCITY);
         this.foodEaten = 0;
         this.foodAttractiveness = 0.0;
     }
     
     /**
      * Determine whether or not a Critter contains (can eat) a certain piece of food
      * @param f The piece of Food to determine whether or not the Critter contains
      * @return true if the Critter contains f, false otherwise
      */
     public boolean contains(Food f)
     {
         double delX = x - f.x;
         double delY = y - f.y;
         int dist = (int) Math.sqrt(delX*delX + delY*delY);
         if (dist <= Critter.CRITTER_RADIUS + Food.FOOD_RADIUS)
         {
             if (f.isHealthy())
             {
                 foodEaten += (foodEaten < 1 / Critter.GROWTH_RATE) ? 1 : 0;
                 if (foodAttractiveness <= 1 - Critter.GROWTH_RATE && Math.random() > 0.5)
                 {
                     foodAttractiveness += Critter.GROWTH_RATE;
                 }
             }
             else
             {
                 if (foodAttractiveness >= Critter.GROWTH_RATE && Math.random() > 0.5)
                 {
                     foodAttractiveness -= Critter.GROWTH_RATE;
                 }
             }
             return true;
         }
         return false;
     }
     
     /**
      * Get the velocity of the Critter
      * @return the velocity of the Critter
      */
     public Vector getVelocity()
     {
         return velocity;
     }
     
     /**
      * Get this Critter's foodAttractiveness
      * @return this Critter's foodAttractiveness
      */
     public double getFoodAttractiveness()
     {
         return foodAttractiveness;
     }
     
     /**
      * Get the amount of food this Critter has eaten
      * @return the amount of food this Critter has eaten
      */
     public int getFoodEaten()
     {
         return foodEaten;
     }
     
     /**
      * Change this Critter's x-position
      * @param del the amount to change this Critter's x-position by
      */
     public void delX(double del)
     {
         if (del == 0)
         {
             return;
         }
         double abs = Math.abs(del);
         int futureX =  this.x + (int) (Math.ceil(abs) * del/abs);
         if (futureX >= World.WORLD_WIDTH - Critter.CRITTER_RADIUS && velocity.getX() > 0)
         {
             velocity.setX(-velocity.getX());
             return;
         }
         if (futureX <= Critter.CRITTER_RADIUS && velocity.getX() < 0)
         {
             velocity.setX(-velocity.getX());
             return;
         }
         this.x = futureX;
     }
     
     /**
      * Change this Critter's y-position
      * @param del the amount to change this Critter's y-position by
      */
     public void delY(double del)
     {
         if (del == 0)
         {
             return;
         }
         double abs = Math.abs(del);
         int futureY = this.y + (int) (Math.ceil(abs) * del/abs);
         if (futureY >= World.WORLD_HEIGHT - Critter.CRITTER_RADIUS && velocity.getY() > 0)
         {
             velocity.setY(-velocity.getY());
             return;
         }
         if (futureY <= Critter.CRITTER_RADIUS && velocity.getY() < 0)
         {
             velocity.setY(-velocity.getY());
             return;
         }
         this.y = futureY;
     }
     
     /**
      * Set this Critter's foodAttractiveness
      * @param foodAttraciveness this Critter's new foodAttractiveness
      */
     public void setFoodAttractiveness(double foodAttraciveness)
     {
         this.foodAttractiveness = foodAttraciveness;
         this.foodEaten = (int) (foodAttractiveness / Critter.GROWTH_RATE);
     }
     
     /**
      * Set the amount of food this Critter has eaten
      * @param foodEaten the amount of food this Critter has eaten
      */
     public void setFoodEaten(int foodEaten)
     {
         this.foodEaten = foodEaten;
         this.foodAttractiveness = foodEaten * Critter.GROWTH_RATE;
     }
     
     /**
      * Set the velocity of this Critter
      * @param velocity the velocity of this Critter
      */
     public void setVelocity(Vector velocity)
     {
         this.velocity = velocity;
     }
     
     /**
      * Draw the inner circle of this Critter, which shows what direction it is facing
      * @param g the Graphics object used to draw
      */
     private void drawInnerCirlce(Graphics g)
     {
         g.setColor(Color.BLUE);
         int xPos = (int) (Critter.CRITTER_RADIUS/2 * Math.cos(velocity.getDir()) + x);
         int yPos = (int) (Critter.CRITTER_RADIUS/2 * Math.sin(velocity.getDir()) + y);
         int radius = Critter.CRITTER_RADIUS/3;
         g.fillOval(xPos - radius, yPos - radius,
                 radius * 2, radius * 2);
     }
     
     /**
      * Draw this Critter
      * @param g the Graphics object used to draw
      */
     public void draw(Graphics g)
     {
         if (foodAttractiveness < .75)
         {
             g.setColor(Critter.CRITTER_COLOR);
         }
         else
         {
             g.setColor(Critter.CRITTER_FINAL_COLOR);
         }
         g.fillOval(x - Critter.CRITTER_RADIUS, y - Critter.CRITTER_RADIUS,
                 Critter.CRITTER_RADIUS*2, Critter.CRITTER_RADIUS * 2);
         this.drawInnerCirlce(g);
     }
     
 }
