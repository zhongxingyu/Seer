 package archpirates.modules;
 
 import battlecode.common.*;
 import static battlecode.common.GameConstants.*;
 public class Attacker {
     // {{{ Finals
     private final SensorController sensor;
     private final WeaponController [] guns;
     private final WeaponController [] beams;
 
 	private final RobotController myRC;
     private final RobotProperties myRP;
     private WeaponController minGun; // not a final but meh...
     // }}}
 
     // {{{ Settings
     private int chassisMask;
 	private Team team;
     // }}}
 
     // {{{ Cache
     private Robot [] robots;
     private int round = -1;
     private Robot robot;
     private boolean fired; // Only scan if we have not fired recently.
     // }}}
 
     // {{{ Constructors
     /**
      * Controll weapons and medics and target robots on the opposing team.
      *
      * @param properties The properties of the controlling robot.
      */
     public Attacker(RobotProperties rp, Chassis... chassis) {
         this(rp, rp.opTeam, chassis);
     }
 
     /**
      * Target a robot.
      *
      * @param rp The properties object describing the current robot.
      * @param team The team that the robot is on.
      * @param chassis A list of chassis to target.
      */
     public Attacker(RobotProperties rp, Team team, Chassis... chassis) {
         this.myRP = rp;
         this.sensor = rp.sensor;
         this.guns = rp.guns;
         this.beams = rp.beams;
         this.team = team;
         this.myRC = rp.myRC;
         int min_range = 1000;
         int this_range;
         for (WeaponController gun : guns) {
             this_range = gun.type().range;
             if (this_range < min_range) {
                 min_range = this_range;
                 minGun = gun;
             }
         }
         if (min_range == 1000) min_range = 0; // If no guns etc...
 
         setChassis(chassis);
     }
     // }}}
 
     // {{{ Set Settings
     /**
      * Set the chassis that will be targeted.
      * 
      * @param chassis A list of chassis to target.
      */
     public void setChassis(Chassis... chassis) {
         this.chassisMask = 0;
         if (chassis != null) {
             for (Chassis c: chassis) {
                 this.chassisMask |= (1 << c.ordinal());
             }
             robot = null; // No longer store robotInfo.
         }
     }
 
     /**
      * Set the team that will be targeted.
      * 
      * @param team the team that will be targeted.
      */
     public void setTeam(Team team) {
         if (this.team != team) {
             this.team = team;
             robot = null;
         }
     }
     // }}}
 
     // {{{ Set Target
     /**
      * Sets the target.
      *
      * @param robot The robot to target.
      * @return true if the target is within sensor range.
      */
     public boolean setTarget(Robot robot) {
         if (sensor.canSenseObject(robot)) {
             this.robot = robot;
             return true;
         }
         return false;
     }
 
     /**
      * Sets the target.
      *
      * @param location The location to target.
      * @param level The level of the target.
      * @return true if the target is within sensor range.
      */
     public boolean setTarget(MapLocation location, RobotLevel level) throws GameActionException {
         if (sensor.canSenseSquare(location)) {
             robot = (Robot)sensor.senseObjectAtLocation(location, level);
             return true;
         }
         return false;
     }
     // }}}
 
     // {{{ Fire At
     /**
      * Fire everything location.
      */
     //TODO: Clean this up.
     public void fireAt(MapLocation location, RobotLevel level) throws GameActionException {
         fireAt(location, level, guns);
         fireAt(location, level, beams);
     }
     public void fireGunsAt(MapLocation location, RobotLevel level) throws GameActionException {
         fireAt(location, level, guns);
     }
     public void fireBeamsAt(MapLocation location, RobotLevel level) throws GameActionException {
         fireAt(location, level, beams);
     }
     public void fireAt(MapLocation location, RobotLevel level, WeaponController [] weapons) throws GameActionException {
         for (WeaponController weapon : weapons) {
             if ( !weapon.isActive() && weapon.withinRange(location)) {
                 try {
                     weapon.attackSquare(location, level);
                 } catch(GameActionException e) {}
             }
         }
     }
     // }}}
 
     // {{{ Auto Fire
     /**
      * Fire all guns on targets.
      *
      * @param targeter The targeter.
      *
      * @return The location of the currently targeted robot.
      */
     public MapLocation autoFire() throws GameActionException {
         RobotInfo robotInfo = null, tmpRobotInfo = null;
         int tmpDistSq, minDistSq = 1000;
 
         if (fired && robot != null && sensor.canSenseObject(robot)) {
             robotInfo = sensor.senseRobotInfo(robot);
         } else {
             MapLocation myLoc = myRC.getLocation();
             robots = sensor.senseNearbyGameObjects(Robot.class);
             robot = null; // will be overwritten anyway but we DEFINITELY don't want this as not null if it is invalid.
 
             // Find a new target
             for (Robot r : robots) {
                 if (team != r.getTeam())
                     continue;
                 tmpRobotInfo = sensor.senseRobotInfo(r);
                 if ((chassisMask == 0 || (chassisMask & (1 << tmpRobotInfo.chassis.ordinal())) != 0)) 
                 {
                     // FIXME: see if this is slower/faster. may get rid of withinRange.
                     if (minGun.withinRange(tmpRobotInfo.location)) {
                         robotInfo = tmpRobotInfo;
                         robot = r;
                         break;
                     } else if ((tmpDistSq = myLoc.distanceSquaredTo(tmpRobotInfo.location)) < minDistSq) {
                         minDistSq = tmpDistSq;
                         robotInfo = tmpRobotInfo;
                         robot = r;
                     }
                 }
             }
         }
 
         // We haven't fired this round and don't know if any guns are active.
         fired = false;
 
         // Return if nothing is within range
         if (robotInfo == null) return null;
 
         for (WeaponController gun : guns) {
             if (!gun.isActive()) {
                 try {
                     if (gun.withinRange(robotInfo.location)) {
                         gun.attackSquare(robotInfo.location, robot.getRobotLevel());
                         fired = true;
                     }
                 } catch(GameActionException e) {e.printStackTrace();}
             } else {
                 fired = true;
             }
         }
         return robotInfo.location;
     }
 
     /**
      * Fire gun on targets.
      * Should be grouped by type for speed.
      *
      * @param weapon The weapon to fire.
      *
      * @return The location of the currently targeted robot.
      */
     public MapLocation autoFire(WeaponController weapon) throws GameActionException {
         RobotInfo robotInfo = null, tmpRobotInfo = null;
 
         if (fired && robot != null && sensor.canSenseObject(robot)) {
             robotInfo = sensor.senseRobotInfo(robot);
         } else {
             robots = sensor.senseNearbyGameObjects(Robot.class);
             robot = null; // will be overwritten anyway but we DEFINITELY don't want this as not null if it is invalid.
 
             // Find a new target
             for (Robot r : robots) {
                 if (team != r.getTeam())
                     continue;
                 tmpRobotInfo = sensor.senseRobotInfo(r);
                 if ((chassisMask == 0 || (chassisMask & (1 << tmpRobotInfo.chassis.ordinal())) != 0)) 
                 {
                     robotInfo = tmpRobotInfo;
                     robot = r;
                     if (weapon.withinRange(robotInfo.location)) {
                         break;
                     }
                 }
             }
         }
 
         // We haven't fired this round and don't know if any guns are active.
         fired = false;
 
         // Return if nothing is within range
         if (robotInfo == null) {
             return null;
         } else if (!weapon.isActive()) {
             try {
                 if (weapon.withinRange(robotInfo.location)) {
                     weapon.attackSquare(robotInfo.location, robot.getRobotLevel());
                     fired = true;
                 }
             } catch(GameActionException e) {e.printStackTrace();}
         } else {
             fired = true;
         }
         return robotInfo.location;
     }
     // }}}
 }
 
