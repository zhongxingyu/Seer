 import greenfoot.*;  // (World, Actor, GreenfootImage, Greenfoot and MouseInfo)
 import java.util.ArrayList;
 //import robot.Robot;
 
 /**
  * A battle arena.
  * 
  * @author Brendan Redmond and Haley B-E
  * @version 0.3.0
  */
 public class Arena extends World
 {
     //the width of the arena in cells
     public static final int WIDTH = 600;
     //the height of the arena in cells
     public static final int HEIGHT = 600;
     //the maximum number of robots in this Arena
     public static final int MAX_ROBOTS = 8;
     //the maximum height of the robot in pixels
     public static final int MAX_ROBOT_HEIGHT = 50;
     //the maximum width of the robot in pixels
     public static final int MAX_ROBOT_WIDTH = 50;
     
     //length of time to be given as a turn
     private static final int TURN_LENGTH = 10;
 
     /**
      * Constructor: creates the arena, then adds the robots to the arena
      * 
      */
     public Arena()
     {    
         // Create a new world with 600x400 cells with a cell size of 1x1 pixels.
         super(WIDTH, HEIGHT, 1);
         
         Builder builder = new Builder();
         ArrayList<Robot> robots = builder.getRobots();
         ArrayList<Location> spawnLocations = SpawnMap.getSpawnLocations(robots.size());
         
         for (Robot robot : robots)
         {
             //adds each robot to a random spawn location and removes that spawn location
             //this.add(robot, spawnLocations.remove((int) (Math.random() * spawnLocations.size())));
             
         }
         
         Robot robot = new Robot();
         this.add(robot, 100, 200);
        robot.setExactRotation(Math.random() * 360);
         
         Robot robot2 = new Robot();
         this.add(robot2, 100, 400);
        robot2.setExactRotation(Math.random() * 360);
         
         this.makeWalls();
         this.setActOrder(Robot.class, Projectile.class);
     }
     
     /**
      * Creates the walls that bound the Arena
      */
     private void makeWalls()
     {
         Wall wall;
         
         //top
         wall = new Wall(WIDTH - Wall.THICKNESS * 2, Wall.THICKNESS);
         this.addObject(wall, WIDTH / 2, Wall.THICKNESS / 2);
         wall.setRotation(0);
         
         //bottom
         wall = new Wall(WIDTH - Wall.THICKNESS * 2, Wall.THICKNESS);
         this.addObject(wall, WIDTH / 2, HEIGHT - Wall.THICKNESS / 2);
         wall.setRotation(0);
         
         //right
         wall = new Wall(HEIGHT, Wall.THICKNESS);
         this.addObject(wall, WIDTH - Wall.THICKNESS / 2 - 1, HEIGHT / 2);
         wall.setRotation(90);
         
         //left
         wall = new Wall(HEIGHT, Wall.THICKNESS);
         this.addObject(wall, Wall.THICKNESS / 2 - 1, HEIGHT / 2);
         wall.setRotation(90);
     }
     
     /**
      * 
      */
     public void act()
     {
     }
     
     //Methods for adding/removing things to the arena
     
     /**
      * Wrapper method for addObject, takes an Actor and a Location
      * 
      * @param actor     the actor to be added
      * @param location  the location at which the actor will be added
      */
     private void add(ArenaActor actor, Location location)
     {
         this.addObject(actor, (int) (location.getX() + .5), (int) (location.getY() + .5));
         actor.setExactLocation(location);
     }
     
     /**
      * Wrapper method for addObject, takes an Actor, an x and a y coordinate
      * 
      * @param actor     the actor to be added
      * @param x         the x coordinate of the location at which the actor will be added
      * @param y         the y coordinate of the location at which the actor will be added
      */
     public void add(ArenaActor actor, double x, double y)
     {
         this.addObject(actor, (int) (x + .5), (int) (y + .5));
         actor.setExactLocation(new Location(x, y));
     }
     
     /**
      * Removes given ArenaActor from arena
      * 
      * @param toRemove      ArenaActor to remove from arena
      */
     public void remove(ArenaActor toRemove)
     {
         this.removeObject(toRemove);
     }
     
     //Methods for ArenaActor interaction
     
     /**
      * Finds the distance between the starting point and the nearest ArenaActor
      * 
      * @param direction         direction in which to search for ArenaActor (angle in degrees)
      * @param startingPoint     point of origin for search
      * @return                  distance between starting point and nearest ArenaActor
      */
     public int getNearest(int direction, int startingPoint)
     {
         return 0;
     }
     
     /**
      * Finds the distance between starting point and the nearest ArenaActor within a given range of directions
      *              (for a field of vision rather than a line of sight)
      * Precondition: startDirection + tolerance <= endDirection
      * 
      * @param startDirection        the beginning of the range in which to look for an ArenaActor (angle in degrees)
      * @param endDirection          the end of the range in which to look for an ArenaActor (angle in degrees)
      * @param startingPoint         point of origin for search
      * @param tolerance             distance between each search (angle in degrees)
      * @return                      distance between starting point and nearest ArenaActor within range
      */
     public int getNearestInRange(int startDirection, int endDirection, int startingPoint, int tolerance)
     {
         int nearest = this.getNearest(startDirection, startingPoint);
         for (int i = (startDirection + tolerance); i <= endDirection; i += tolerance)
         {
             int temp = this.getNearest(i, startingPoint);
             if (temp < nearest)
             {
                 nearest = temp;
             }
         }
         return nearest;
     }
     
     //methods that ensure given coordinates are within the Arena
 
     public static boolean xIsInBoundaries(double x)
     {
         return ((x >= 0) && (x <= Arena.WIDTH));
     }
    
     public static boolean yIsInBoundaries(double y)
     {
         return ((y >= 0) && (y <= Arena.WIDTH));
     }
     
     public static boolean isInBoundaries(double x, double y)
     {
         return (xIsInBoundaries(x) && yIsInBoundaries(y));
     }
 }
