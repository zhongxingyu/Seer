  
 
 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.util.List;
 
 /**
  * Common elements of actors that act in the Arena.
  * 
  * 
  * @author Brendan Redmond and Haley B-E
  * @version 0.6.0
  */
 abstract public class ArenaActor extends Actor implements Collidable
 {
     //the number of time units elapsed for each actor's act method
     public static final int ACT_TIME = 10;
 
 //the current speed of this ArenaActor in cells per unit time
 //private double speed;
 //the exact rotation of this ArenaActor in degrees
//private double rotation;
     //the exact location of this ArenaActor
     private Location location;
     
    private List<ArenaActor> intersectingActors;
 
     /**
      * Constructor: set speed, rotation to 0
      */
     public ArenaActor()
     {
         super();
         //this.speed = 0;
         this.setRotation(0);
         this.location = null;
         this.intersectingActors = null;
     }
     
     /**
      * Constructor: set speed, direction to given values
      * 
      * @param speed     the given speed
      * @param direction the given direction
      */
     public ArenaActor(/*double speed,*/ int direction)
     {
         super();
         //assert(speed >= 0);
         assert(Math.abs(direction) < 360);
         assert(Math.abs(direction) >= 0);
         
         //this.speed = speed;
         this.setRotation(direction);
         this.location = null;
         this.intersectingActors = null;
     }
     
     //other public getter/setter methods and variable modifiers
 
     
     /*public double getSpeed()
     {
         return this.speed;
     }
     
     public void setSpeed(double speed)
     {
         this.speed = speed;
     }
     
     /**
      * Changes the speed of this ArenaActor by a given amount
      * 
      * @param change  the amount by which the speed of this ArenaActor will change (negative number decreases speed, positive increases)
      *
     public void changeSpeed(double change)
     {
         this.speed += change;
     }*/
     
     
     public void act()
     {
         //this.move(1);
     }
 
     //methods dealing with movement    
     
     /**
      * Moves this ArenaActor a given distance in the direction it is facing
      * 
      * @param distance  the given distance
      */
     public void move(double distance)
     {
         assert(distance >= 0);
         
         
         while (distance > 1)
         {
             this.smallMove(1);
             this.resolveCollisions();
             distance--;
         }
         
         this.smallMove(distance);
         this.resolveCollisions();
     }
     
     /**
      * Moves this ArenaActor one cell width in the direction it is facing
      */
     public void smallMove(double distance)
     {
         this.setExactLocation(this.getExactX() + distance * Math.cos(Math.toRadians(this.getRotation())),
                               this.getExactY() + distance * Math.sin(Math.toRadians(this.getRotation())));
     }
     
     /**
      * Returns the angle this ArenaActor must have to face two given coordinates
      * 
      * @param x     the x coordinate
      * @param y     the y coordinate
      * @return      the angle this ArenaActor must have to face two given coordinates
      */
     public int getAngleTowards(double x, double y)
     {
         assert(Arena.xIsInBoundaries(x));
         assert(Arena.yIsInBoundaries(y));
         
         if (x > this.getExactX())
         {
             return (int) Math.toDegrees(Math.atan((y - this.getY()) / (x - this.getX())));
         }
         else if (x < this.getExactX())
         {
             return (int) Math.toDegrees(Math.atan((y - this.getY()) / (x - this.getX()))) + 180;
         }
         else
         {
             if (y > this.getExactY())
             {
                 return 90;
             }
             else if (y < this.getExactY())
             {
                 return 270;
             }
             else
             {
                 return this.getRotation();
             }
         }
     }
     
     /**
      * Returns the angle this ArenaActor must have to face a given ArenaActor
      * 
      * @param a     the given ArenaActor
      */
     public int getAngleTowards(ArenaActor a)
     {
         return this.getAngleTowards(a.getExactX(), a.getExactY());
     }
 
     
     /**
      * Resolves collisions between this ArenaActor and any intersecting ArenaActors
      */
     private void resolveCollisions()
     {
         this.intersectingActors = this.getIntersectingObjects(Collidable.class);
         
         for (Collidable actor : this.intersectingActors)
         {
             this.deflect(actor);
         }
     }
     
     /**
      * Deals collision damage to the given intersecting ArenaActor and changes the movement of
      *  this ArenaActor as a result of the collision
      *  
      * @param a     the intersecting ArenaActor
      */
     private void collideWith(ArenaActor a)
     {
         assert(a != null);
         this.deflect(a);
         
         //will contain method to take damage
     }
     
     
     
     //we could possibly combine these two deflect methods, getting the exact rotation of the other ArenaActor
     //probably isnt vital and we could just use getRotation()
     /**
      * Changes this ArenaActor's direction due to a collision with a given ArenaActor
      * 
      * @param a     the given ArenaActor
      */
     private void deflect(Collidable actor)
     {
         assert(actor != null);
         
         this.setDeflectionRotation(2 * actor.getDeflectionAngle() - this.getRotation());
         actor.setDeflectionRotation(2 * this.getDeflectionAngle() - actor.getRotation());
         
         while (this.getIntersectingObjects(Collidable.class).contains(actor))
         {
             this.smallMove(1);
             actor.smallMove(1);
         }
     }
     
     public int getDeflectionAngle()
     {
         return (this.getRotation() + 90) % 180;
     }
     
     public void setDeflectionRotation(int angle)
     {
         this.setRotation(angle);
     }
     
     //abstract methods
     
     /**
      * Returns the mass of this ArenaActor
      * 
      * @return  the mass of this ArenaActor
      */
     abstract public double getMass();
     
     /**
      * Has this ArenaActor take a given amount of damage
      * 
      * @param damage    the damage to be dealt
      */
     abstract public void takeDamage(double damage);
     
     /**
      * Returns the health of this ArenaActor
      * 
      * @return  the health of this ArenaActor
      */
     abstract public double getHealth();
     
         
     //wrapper methods that take doubles in order to utilize greenfoot's movement methods that require ints
     
     /**
      * Sets the ArenaActor's location to the nearest integer location to the double coordinates passed
      * @param x     double x-coordinate of new location
      * @param y     double y-coordinate of new location
      */
     public void setExactLocation(double x, double y)
     {
         assert(Arena.xIsInBoundaries(x));
         assert(Arena.yIsInBoundaries(y));
         
         super.setLocation((int) (x + .5), (int) (y + .5));
         
         this.location.setX(x);
         this.location.setY(y);
     }
     
     /**
      * Sets the ArenaActor's location to a given Location
      * 
      * @param location      the given location
      */
     public void setExactLocation(Location location)
     {
         assert(location != null);
         
         this.location = location;
     }
 
     /**
      * Sets the ArenaActor's rotation to a given rotation
      * 
      * @param rotation  the given rotation
      */
 /**    public void setRotation(double rotation)
     {
         rotation = (360 + rotation) % 360;
         
         super.setRotation((int) (rotation + .5));
         this.rotation = rotation;
     }
   */  
     
     //do we need assertions for turn?
     /**
      * Turns this ArenaActor by a given angle
      * 
      * @param angle     the angle to be turned by
      */
     /*public void turn(int angle)
     {
         assert(rotation >= 0);
         assert(rotation < 360);
         
         super.turn(angle);
         this.rotation += angle;
         this.rotation %= 360;
     }*/
     
     public double getExactX()
     {
         return this.location.getX();
     }
     
     public double getExactY()
     {
         return this.location.getY();
     }
     
     public Location getExactLocation()
     {
         return this.location;
     }
     
     /*public double getRotation()
     {
         return this.rotation;
     }*/
 }
