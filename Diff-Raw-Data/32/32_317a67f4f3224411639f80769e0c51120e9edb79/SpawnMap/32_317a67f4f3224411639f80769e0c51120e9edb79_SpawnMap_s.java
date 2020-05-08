 import java.util.ArrayList;
 
 /**
  * Spawn locations for given numbers of robots
  * 
  * @author Brendan Redmond 
  * @version 0.0.0
  */
 public class SpawnMap  
 {
     //the array of ArrayLists of spawn locations for a given number of robots
     private static Location[] locations = 
     {
         //top left
         new Location(Arena.MAX_ROBOT_WIDTH + Wall.THICKNESS, Arena.MAX_ROBOT_HEIGHT + Wall.THICKNESS),
         //bottom right
         new Location(Arena.WIDTH - Arena.MAX_ROBOT_WIDTH - Wall.THICKNESS,
                      Arena.HEIGHT - Arena.MAX_ROBOT_HEIGHT - Wall.THICKNESS),
         //bottom left
         new Location(Arena.MAX_ROBOT_WIDTH + Wall.THICKNESS, Arena.HEIGHT - Arena.MAX_ROBOT_HEIGHT - Wall.THICKNESS),
         //top right
         new Location(Arena.WIDTH - Arena.MAX_ROBOT_WIDTH - Wall.THICKNESS, Arena.MAX_ROBOT_HEIGHT + Wall.THICKNESS),
         //top middle
        new Location(Arena.WIDTH, Arena.MAX_ROBOT_HEIGHT + Wall.THICKNESS),
         //left middle
        new Location(Arena.MAX_ROBOT_WIDTH + Wall.THICKNESS, Arena.HEIGHT),
         //right middle
        new Location(Arena.WIDTH - Arena.MAX_ROBOT_WIDTH - Wall.THICKNESS, Arena.HEIGHT),
         //bottom middle
        new Location(Arena.WIDTH, Arena.HEIGHT - Arena.MAX_ROBOT_HEIGHT - Wall.THICKNESS)
     };
     
     
     public static ArrayList<Location> getSpawnLocations(int numLocations)
     {
         assert(numLocations <= locations.length);
         
         ArrayList<Location> spawnLocations = new ArrayList<Location>();
         
         for (int i = 0; i < numLocations; i++)
         {
             spawnLocations.add(locations[i]);
         }
         
         return spawnLocations;
     }
 }
