 package team094.modules;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 
 public class Builder {
 
     private final BuilderController builder;
     private final RobotController myRC;
 
     // Cache
     private MapLocation location;
     private RobotLevel level;
     private Chassis chassis;
     private ComponentType [] components;
     private boolean turn_on;
     private int cost;
     private int p = -2; //progress
     private double res_mult = 1.2; // We need at least res_mult * RESOURCES to build.
 
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
         myRC = rp.myRC;
     }
 
     /**
      * Sets the cost of the build.
      */
     private void setCost() {
         if (this.chassis != null)
             this.cost = this.chassis.cost;
         else
             this.cost = 0;
         for (ComponentType c : this.components)
             this.cost += c.cost;
     }
 
     /**
      * Start a build in the specified location.
      *
      * Note, this method will NOT check if you can actually build said chassis/components.
      *
      * @param turn_on turns the robot on if true.
      * @param res_mult The resource multiplier for this build.
      * @param location The location where the robot will be built.
      * @param chassis The chassis that will be built.
      * @param components A list of components (if any) that will be built on this chassis.
      *
      * @return TaskState.WAITING
      */
     public TaskState startBuild(boolean turn_on, double res_mult, MapLocation location, Chassis chassis, ComponentType... components) {
         this.res_mult = res_mult;
         this.chassis = chassis;
         this.level = chassis.level;
         this.components = components;
         this.p = -1;
         this.location = location;
         this.turn_on = turn_on;
         setCost();
         return TaskState.WAITING;
     }
     /**
      * Build components at a specified location and level.
      *
      * Note, this method will NOT check if you can actually build said components.
      *
      * @param turn_on turns the robot on if true.
      * @param res_mult The resource multiplier for this build.
      * @param location The location where the components will be built.
      * @param level The level at which the components will be built.
      * @param components A list of components (if any) that will be built on this chassis.
      *
      * @return TaskState.WAITING
      */
     public TaskState startBuild(boolean turn_on, double res_mult, MapLocation location, RobotLevel level, ComponentType... components) {
         this.res_mult = res_mult;
         this.p = 0;
         this.chassis = null;
         this.level = level;
         this.components = components;
         this.location = location;
         this.turn_on = turn_on;
         setCost();
         return TaskState.WAITING;
     }
 
     /**
      * Weather or not we are in the process of building somthing.
      *
      * @return true if building.
      */
     public boolean isBuilding() {
         return (p >= -1);
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
     @SuppressWarnings("fallthrough")
     public TaskState doBuild() throws GameActionException {
         // Build Chassis
         if (builder.isActive()) return TaskState.ACTIVE;
 
         switch(p) {
             case -2:
                 // I shouldn't be able to build after a fail without initializing.
                 return TaskState.FAIL;
             case -1:
                 if (chassis == null) {
                     p = -2;
                     return TaskState.FAIL;
                 }
                 if (myRC.getTeamResources() < res_mult*this.cost)
                     return TaskState.WAITING;
                 try {
                     if (builder.canBuild(chassis, location)) {
                         builder.build(chassis, location);
                         p++;
                         return TaskState.ACTIVE;
                     } else {
                         p = -2;
                         return TaskState.FAIL;
                     }
                 } catch (Exception e) {
                     System.out.println("caught exception:");
                     e.printStackTrace();
                     p = -2;
                     return TaskState.FAIL;
                 }
             case 0:
                 if (components.length == 0) break;
             default:
                 // I don't check if I can still build because the exception doesn't really cost that much and checking this is a PAIN.
                 if (myRC.getTeamResources() < res_mult*components[p].cost)
                     return TaskState.ACTIVE;
                 else {
                     try {
                         builder.build(components[p], location, level);
                         p++;
                     } catch (Exception e) {
                     // Thrown often, not worth checking.
                         //System.out.println("caught exception:");
                         //e.printStackTrace();
                         p = -2;
                         return TaskState.FAIL;
                     }
                 }
         }
 
         // Complete build or return in progress.
         if (p == components.length) {
             try {
                 if (turn_on)
                     myRC.turnOn(location, level);
                 p = -2;
             } catch (Exception e) {
                 System.out.println("caught exception:");
                 e.printStackTrace();
                return TaskState.FAIL;
                 p = -2;
             }
             return TaskState.DONE;
         } else {
             return TaskState.ACTIVE;
         }
     }
 }
