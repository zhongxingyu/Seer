 package team153;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 import java.util.*;
 
 public class SensationalSensing extends Base {
 
     public MapStore map;
     public MexicanMessaging messaging;
     public NaughtyNavigation navigation;
     public RobotCache robotCache;
 
     public SensationalSensing(NovaPlayer player) {
         super(player);
         map = player.map;
         messaging = player.messaging;
        navigation = player.navigation;
         robotCache = new RobotCache();
     }
 
     /**
      * Returns a 2D array with MapLocations or null objects based on the actual tiles this robot can sense.
      */
     public MapLocation[][] getSensibleTiles() {
         // return int locations of all tiles i can sense
         int radius = controller.getRobotType().sensorRadius();
         int size = radius * 2 + 1;
         MapLocation[][] tiles = new MapLocation[size][size];
         int currentX = controller.getLocation().getX();
         int currentY = controller.getLocation().getY();
 
         for(int x = -radius; x <= radius; x++) {
             for(int y = -radius; y <= radius; y++) {
                 MapLocation location = new MapLocation(x + currentX, y + currentY);
                 if(controller.canSenseSquare(location)) {
                     tiles[x + radius][y + radius] = location;
                 } else {
                     tiles[x + radius][y + radius] = null;
                 }
             }
         }
 
         return tiles;
     }
 
     public boolean runSensorCallbacks(NovaMapData data) {
         boolean ret = true;
         if(data != null) {
             ret = player.tileSensedCallback(data);
             /*
              TODO:: This breaks AttackPlayer badly.
              if(data.airRobot != null || data.groundRobot != null) {
                 ret = player.enemyInSightCallback(data) && ret;
             }*/
         }
         return ret;
     }
 
     /**
      * Iteratres through each of the tiles in sensor range of the
      */
     public void senseAllTiles() {
         senseTiles(getSensibleTiles());
     }
 
     /**
      * This is used to sense the new tiles for robots with a 360 degree sensor angle.
      * Those robots pass in 3 int arrays representing the distance from the current robots location
      * of each new cell the robot can sense.  These distances are based on when the robot moves south,
      * west, and southwest respectively.
      *
      * Each of these is translated into a map location based on the direction the robot is facing.
      * ie: if the robot is moving east instead of west, the deltas are negated.
      *
      * The callback tileSensedCallback is called for each of the tiles.
      */
     public void senseDeltas(int[] verticalDeltas, int[] horizontalDeltas, int[] diagonalDeltas) {
         Direction dir = controller.getDirection();
         int[] directionDelta = navigation.getDirectionDelta(dir);
         int currentX = controller.getLocation().getX(), currentY = controller.getLocation().getY();
 
         int xDelta, yDelta, x, y;
         int[] deltas;
 
         if(directionDelta[0] == 0) {
             xDelta = 1;
             yDelta = directionDelta[1];
             deltas = verticalDeltas;
         } else if(directionDelta[1] == 0) {
             xDelta = directionDelta[0];
             yDelta = 1;
             deltas = horizontalDeltas;
         } else {
             xDelta = directionDelta[0];
             yDelta = directionDelta[1];
             deltas = diagonalDeltas;
         }
 
         for(int c = 0; c < deltas.length; c += 2) {
             x = currentX + deltas[c] * xDelta;
             y = currentY + deltas[c + 1] * yDelta;
             runSensorCallbacks(senseTile(new MapLocation(x, y)));
         }
     }
 
     public ArrayList<RobotInfo> senseEnemyRobotInfoInSensorRange() {
         return robotCache.senseEnemyRobotInfoInSensorRange();
     }
 
     public ArrayList<RobotInfo> senseAlliedRobotInfoInSensorRange() {
         return robotCache.senseAlliedRobotInfoInSensorRange();
     }
 
     public ArrayList<MapLocation> senseEnemyRobotLocations() {
         return robotCache.senseEnemyRobotLocations();
     }
 
     /**
      * Sense the 8 tiles around the robot.
      */
     public NovaMapData[] senseSurroundingSquares() {
         return senseSurroundingSquares(controller.getLocation());
     }
 
     public NovaMapData[] senseSurroundingSquares(MapLocation location) {
         NovaMapData[] ret = new NovaMapData[9];
         int x = location.getX(), y = location.getY();
 
         ret[0] = senseTile(new MapLocation(x - 1, y - 1));
         ret[4] = senseTile(new MapLocation(x, y - 1));
         ret[1] = senseTile(new MapLocation(x + 1, y - 1));
 
         ret[5] = senseTile(new MapLocation(x - 1, y));
         ret[8] = senseTile(new MapLocation(x, y));
         ret[6] = senseTile(new MapLocation(x + 1, y));
 
         ret[2] = senseTile(new MapLocation(x - 1, y + 1));
         ret[7] = senseTile(new MapLocation(x, y + 1));
         ret[3] = senseTile(new MapLocation(x + 1, y + 1));
 
         return ret;
     }
 
     public NovaMapData senseTileType(MapLocation location) {
         NovaMapData data = map.get(location);
         if(data.tile != null) return data;
         if(controller.canSenseSquare(location)) {
             data.tile = controller.senseTerrainTile(location);
         }
         return data;
     }
 
     /**
      * Sense the terrain and block height of a tile, and whether the tile has a robot or flux deposit on it.
      * If the location is offmap, a MapData object will still be returned, but it will not be saved in the
      * mapstore object.  Walls will be automatically updated to reflect the new locations.
      */
     public NovaMapData senseTile(MapLocation location) {
         if(!controller.canSenseSquare(location)) {
             return null;
         }
 
         NovaMapData data = senseTileType(location);
         return data;
     }
 
     /**
      * Calls senseTile on each of the given tiles that are not null and then calls the callback
      * function tileSensedCallback for each of the resulting MapData objects.
      */
     public void senseTiles(MapLocation[][] tiles) {
         for(MapLocation[] row : tiles) {
             for(MapLocation tile : row) {
                 if(tile != null) {
                     if(!runSensorCallbacks(senseTile(tile))) {
                         return;
                     }
                 }
             }
         }
     }
 
     class RobotCache {
 
         public int airSensed = Integer.MIN_VALUE, groundSensed = Integer.MIN_VALUE,
                 airInfoSensed = Integer.MIN_VALUE, groundInfoSensed = Integer.MIN_VALUE,
                 enemyInfoSensed = Integer.MIN_VALUE, alliedInfoSensed = Integer.MIN_VALUE, enemyLocationSensed = Integer.MIN_VALUE;
         public Robot[] air, ground;
         public ArrayList<RobotInfo> airInfo, groundInfo, enemyRobots, alliedRobots;
         public ArrayList<MapLocation> enemyLocations;
         public int oldDataTolerance = 1;
 
         public RobotCache() {
         }
 
         public ArrayList<RobotInfo> senseEnemyRobotInfoInSensorRange() {
             if(enemyInfoSensed >= Clock.getRoundNum() - oldDataTolerance) {
                 return enemyRobots;
             }
 
             enemyRobots = new ArrayList<RobotInfo>();
             robotCache.getGroundRobotInfo();
             robotCache.getAirRobotInfo();
 
             for(RobotInfo robot : groundInfo) {
                 if(!robot.team.equals(player.team)) {
                     enemyRobots.add(robot);
                 }
             }
 
             for(RobotInfo robot : airInfo) {
                 if(!robot.team.equals(player.team)) {
                     enemyRobots.add(robot);
                 }
             }
 
             return enemyRobots;
         }
 
         public ArrayList<RobotInfo> senseAlliedRobotInfoInSensorRange() {
             if(alliedInfoSensed >= Clock.getRoundNum() - oldDataTolerance) {
                 return alliedRobots;
             }
 
             alliedRobots = new ArrayList<RobotInfo>();
             robotCache.getGroundRobotInfo();
 
             for(RobotInfo robot : groundInfo) {
                 if(robot.team.equals(player.team)) {
                     alliedRobots.add(robot);
                 }
             }
 
             return alliedRobots;
         }
 
         public ArrayList<MapLocation> senseEnemyRobotLocations() {
             if(enemyLocationSensed >= Clock.getRoundNum() - oldDataTolerance) {
                 return enemyLocations;
             }
 
             senseEnemyRobotInfoInSensorRange();
             enemyLocations = new ArrayList<MapLocation>();
             for(RobotInfo r : enemyRobots) {
                 enemyLocations.add(r.location);
             }
             
             return enemyLocations;
         }
 
         public Robot[] getAirRobots() {
             if(airSensed >= Clock.getRoundNum() - oldDataTolerance) {
                 return air;
             }
 
             try {
                 air = controller.senseNearbyAirRobots();
                 airSensed = Clock.getRoundNum();
             } catch(Exception e) {
                 System.out.println("----Caught Exception in getAirRobots.  Exception: " + e.toString());
             }
             return air;
         }
 
         public Robot[] getGroundRobots() {
             if(groundSensed >= Clock.getRoundNum() - oldDataTolerance) {
                 return ground;
             }
 
             try {
                 ground = controller.senseNearbyGroundRobots();
                 groundSensed = Clock.getRoundNum();
             } catch(Exception e) {
                 System.out.println("----Caught Exception in getGroundRobots.  Exception: " + e.toString());
             }
             return ground;
         }
 
         public ArrayList<RobotInfo> getAirRobotInfo() {
             if(airInfoSensed >= Clock.getRoundNum() - oldDataTolerance) {
                 return airInfo;
             }
 
             getAirRobots();
             airInfo = new ArrayList<RobotInfo>();
             for(Robot robot : air) {
                 try {
                     if(controller.canSenseObject(robot)) {
                         airInfo.add(controller.senseRobotInfo(robot));
                     }
                 } catch(Exception e) {
                     System.out.println("----Caught Exception in getAirRobotInfo.  Exception: " + e.toString());
                 }
             }
             airInfoSensed = Clock.getRoundNum();
             return airInfo;
         }
 
         public ArrayList<RobotInfo> getGroundRobotInfo() {
             if(groundInfoSensed >= Clock.getRoundNum() - oldDataTolerance) {
                 return groundInfo;
             }
 
             getGroundRobots();
             groundInfo = new ArrayList<RobotInfo>();
             for(Robot robot : ground) {
                 try {
                     if(controller.canSenseObject(robot)) {
                         groundInfo.add(controller.senseRobotInfo(robot));
                     }
                 } catch(Exception e) {
                     System.out.println("----Caught Exception in getGroundRobotInfo.  Exception: " + e.toString());
                 }
             }
             groundInfoSensed = Clock.getRoundNum();
             return groundInfo;
         }
     }
 }
