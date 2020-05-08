 package archpirates.modules;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 
 public class Builder {
     private static double MULT = 1.2; // We need at least MULT * RESOURCES to build.
 
     private final BuilderController builder;
     private final SensorController sensor;
     private final RobotController myRC;
 
     // Cache
     private MapLocation location;
     private RobotLevel level;
     private Chassis chassis;
     private ComponentType [] components;
     private boolean turn_on;
 
     private int p = -1; //progress
 
     /**
      * Builds buildings and robots.
      *
      * Call startBuild to begin building and doBuild every round until it returns TaskState.DONE
      * or fails with TaskState.FAIL. You can also count TaskState.WAITING to impliment a timeout.
      *
      * @param rp Your robot properties.
      */
     public Builder(RobotProperties rp) {
         builder = rp.builder;
         sensor = rp.sensor;
         myRC = rp.myRC;
     }
 
     /**
      * Start a build in the first free location.
      *
      * Note, this method will NOT check if you can actually build said chassis/components.
      *
      * @param turn_on turns the robot on if true.
      * @param chassis The chassis that will be built.
      * @param components A list of components (if any) that will be built on this chassis.
      *
      * @return TaskState.WAITING
      */
     public TaskState startBuild(boolean turn_on, Chassis chassis, ComponentType... components) {
         return startBuild(turn_on, null, chassis, components);
     }
 
     /**
      * Start a build in the specified location.
      *
      * Note, this method will NOT check if you can actually build said chassis/components.
      *
      * @param turn_on turns the robot on if true.
      * @param location The location where the robot will be built.
      * @param chassis The chassis that will be built.
      * @param components A list of components (if any) that will be built on this chassis.
      *
      * @return TaskState.WAITING
      */
     public TaskState startBuild(boolean turn_on, MapLocation location, Chassis chassis, ComponentType... components) {
         this.chassis = chassis;
         this.level = chassis.level;
         this.components = components;
         this.p = -1;
         this.location = location;
         this.turn_on = turn_on;
         return TaskState.WAITING;
     }
     /**
      * Build components at a specified location and level.
      *
      * Note, this method will NOT check if you can actually build said components.
      *
      * @param turn_on turns the robot on if true.
      * @param location The location where the components will be built.
      * @param level The level at which the components will be built.
      * @param components A list of components (if any) that will be built on this chassis.
      *
      * @return TaskState.WAITING
      */
     public TaskState startBuild(boolean turn_on, MapLocation location, RobotLevel level, ComponentType... components) {
         this.p = 0;
         this.chassis = null;
         this.level = level;
         this.components = components;
         this.location = location;
         this.turn_on = turn_on;
         return TaskState.WAITING;
     }
 
     /**
      * Continue building until DONE or FAIL.
      *
      * @return The state of the build.
      *   * WAITING when waiting for space to clear or for flux.
      *   * ACTIVE when building or the builder is Active.
      *   * DONE when the build is done and the robot has turned on.
      *   * FAIL when the build fails.
      */
     public TaskState doBuild() throws GameActionException {
         // Build Chassis
         if (builder.isActive()) return TaskState.ACTIVE;
 
         if (p == -1) {
             if (myRC.getTeamResources() < MULT*chassis.cost)
                 return TaskState.WAITING;
             if (location == null) {
                Direction tmp_dir = myRC.getDirection();
                MapLocation tmp_loc = myRC.getLocation();
                for (int i = 8; --i > 0;) {
                    tmp_dir = tmp_dir.rotateRight();
                    tmp_loc = tmp_loc.add(tmp_dir);
                     if (sensor.senseObjectAtLocation(tmp_loc, level) == null) {
                         location = tmp_loc;
                         break;
                     }
                 }
                 if (location == null)
                     return TaskState.WAITING;
             }
            if (sensor.senseObjectAtLocation(location, level) == null) {
                 try {
                     builder.build(chassis, location);
                     p++;
                     return TaskState.ACTIVE;
                 } catch (Exception e) {
                     System.out.println("caught exception:");
                     e.printStackTrace();
                     p = -1;
                     location = null;
                     turn_on = true;
                     return TaskState.FAIL;
                 }
             } else {
                 return TaskState.FAIL;
             }
         }
 
         // Only build if we can
         if (this.location == null || sensor.senseObjectAtLocation(location, level) == null) {
             p = -1;
             location = null;
             return TaskState.FAIL;
         }
 
         if(components.length > 0) {
             // Build Components if we have the money.
             if (myRC.getTeamResources() < MULT*components[p].cost)
                 return TaskState.WAITING;
             else {
                 try {
                     builder.build(components[p], location, level);
                 } catch (Exception e) {
                     System.out.println("caught exception:");
                     e.printStackTrace();
                     p = -1;
                     location = null;
                     turn_on = true;
                     return TaskState.FAIL;
                 }
             }
         }
 
         // Complete build or return in progress.
         if (++p == components.length) {
             try {
                 if (turn_on)
                     myRC.turnOn(location, level);
             } catch (Exception e) {
                 System.out.println("caught exception:");
                 e.printStackTrace();
                 return TaskState.FAIL;
             } finally {
                 p = -1;
                 location = null;
                 turn_on = true;
             }
 
             return TaskState.DONE;
         } else {
             return TaskState.ACTIVE;
         }
     }
 }
