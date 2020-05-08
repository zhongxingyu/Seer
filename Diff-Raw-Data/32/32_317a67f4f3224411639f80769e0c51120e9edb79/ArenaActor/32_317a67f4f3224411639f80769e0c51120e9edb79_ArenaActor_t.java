 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.util.List;
 
 /**
  * Common elements of actors that act in the Arena.
  * 
  * 
  * @author Brendan Redmond and Haley B-E
  * @version 0.6.0
  */
 abstract public class ArenaActor extends Actor
 {
     //the number of time units elapsed for each actor's act method
     public static final int ACT_TIME = 10;
 
     //the current speed of this ArenaActor in cells per unit time
     private double speed;
     //the exact rotation of this ArenaActor in degrees
     private double rotation;
     //the exact location of this ArenaActor
     private Location location;
 
     /**
      * Constructor: set speed, rotation to 0
      */
     public ArenaActor()
     {
         super();
         this.speed = 0;
         this.setExactRotation(0);
         this.location = null;
        
     }
     
     /**
      * Constructor: set speed, direction to given values
      * 
      * @param speed     the given speed
      * @param direction the given direction
      */
     public ArenaActor(double speed, double direction)
     {
         super();
         assert(speed >= 0);
         assert(Math.abs(direction) < 360);
         assert(Math.abs(direction) >= 0);
         
         this.speed = speed;
         this.setExactRotation(direction);
         this.location = null;
     }
     
     //other public getter/setter methods and variable modifiers
 
     
     public double getSpeed()
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
      */
     public void changeSpeed(double change)
     {
         this.speed += change;
     }
     
     
     public void act()
     {
         this.move(1);
     }
 
     //methods dealing with movement    
     
     /**
      * Moves this ArenaActor a given distance in the direction it is facing
      * 
      * @param distance  the given distance
      */
     public void move(int distance)
     {
         assert(distance >= 0);
         
         while (distance > 0)
         {
             this.moveOne();
             this.resolveCollisions();
             distance--;
         }
     }
     
     /**
      * Moves this ArenaActor one cell width in the direction it is facing
      */
     public void moveOne()
     {
         double x = this.getExactX();
         double y = this.getExactY();
         
         double xAdd = Math.cos(Math.toRadians(this.getExactRotation()));
         double yAdd = Math.sin(Math.toRadians(this.getExactRotation()));
         
         this.setExactLocation(x + xAdd, y + yAdd);
     }
     
     /**
      * Returns the angle this ArenaActor must have to face two given coordinates
      * 
      * @param x     the x coordinate
      * @param y     the y coordinate
      * @return      the angle this ArenaActor must have to face two given coordinates
      */
     public double getAngleTowards(double x, double y)
     {
         assert(Arena.xIsInBoundaries(x));
         assert(Arena.yIsInBoundaries(y));
         
         double angle;
         
         if (x > this.getExactX())
         {
             angle = Math.toDegrees(Math.atan((y - this.getY()) / (x - this.getX())));
         }
         else if (x < this.getExactX())
         {
             angle = Math.toDegrees(Math.atan((y - this.getY()) / (x - this.getX()))) + 180;
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
                 return this.getExactRotation();
             }
         }
         
         assert(angle >= 0);
         assert(angle < 360);
         
         return angle;
     }
     
     /**
      * Returns the angle this ArenaActor must have to face a given ArenaActor
      * 
      * @param a     the given ArenaActor
      */
     public double getAngleTowards(ArenaActor a)
     {
         return this.getAngleTowards(a.getExactX(), a.getExactY());
     }
 
     
     /**
      * Resolves collisions between this ArenaActor and any intersecting ArenaActors
      */
     private void resolveCollisions()
     {
         List<ArenaActor> arenaActors = this.getIntersectingObjects(ArenaActor.class);
         List<Wall> walls = this.getIntersectingObjects(Wall.class);
         
         
         for (Wall w : walls)
         {
             this.deflect(w);
         }
         
         for (ArenaActor a : arenaActors)
         {
             this.collideWith(a);
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
     private void deflect(ArenaActor a)
     {
         assert(a != null);
         this.setExactRotation(2 * ((this.getAngleTowards(a) + 90) % 180) - this.getExactRotation());
         a.setExactRotation(2 * ((a.getAngleTowards(this) + 90) % 180) - a.getExactRotation());
         
         while (this.getIntersectingObjects(ArenaActor.class).contains(a))
         {
             this.moveOne();
             a.moveOne();
         }
     }
     
     /**
      * Changes this ArenaActor's direction due to a collision with a given Wall
      * 
      * @param a     the given Wall
      */
     private void deflect(Wall w)
     {
         assert(w != null);
         
         if (this.getIntersectingObjects(Wall.class).size() > 1)
         {
             this.setExactRotation(this.getExactRotation() - 180);
         }
         else
         {
             this.setExactRotation(2 * w.getRotation() - this.getExactRotation());
         }
         
         while (this.getOneIntersectingObject(Wall.class) != null)
         {
             this.moveOne();
         }
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
     public void setExactRotation(double rotation)
     {
        rotation = (360 + rotation) % 360;
         
         super.setRotation((int) (rotation + .5));
         this.rotation = rotation;
     }
     
     
     //do we need assertions for turn?
     /**
      * Turns this ArenaActor by a given angle
      * 
      * @param angle     the angle to be turned by
      */
     public void turn(int angle)
     {
         assert(rotation >= 0);
         assert(rotation < 360);
         
         super.turn(angle);
         this.rotation += angle;
         this.rotation %= 360;
     }
     
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
     
     public double getExactRotation()
     {
         return this.rotation;
     }
 }
