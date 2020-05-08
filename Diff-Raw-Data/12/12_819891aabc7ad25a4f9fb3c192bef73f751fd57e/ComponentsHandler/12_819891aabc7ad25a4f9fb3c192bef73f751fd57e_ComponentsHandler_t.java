 package team039.common;
 
 import battlecode.common.*;
 
 /**
  * Hopefully abstracts some component functionality away from tedium.
  * @author Jason
  *
  */
 public class ComponentsHandler {
 
     private final RobotController myRC;
     private final Knowledge       knowledge;
     
     /*** Controllers ***/
     public         MovementController   myMC;
     // Code only utilizes one sensor at the moment... even if there's more than one.
     public         SensorController[]   mySCs = new SensorController[4];
     public         BuilderController    myBC;
     public         WeaponController[]   myWCs = new WeaponController[18];
     public         BroadcastController  myCC;
 
     /*** Controller info ***/
     public         int                  numberOfSensors = 0;
     public         boolean              hasBuilder      = false;
     public         int                  numberOfWeapons = 0;
     public         boolean              hasComm         = false;
     
     
     
     public ComponentsHandler(RobotController rc, Knowledge know) {
         myRC = rc;
         knowledge = know;
     }
     
     
     
     /**
      * Returns an array of Robots that can be sensed
      * @return        an array of all nearby robots, empty if there are no robots or no sensors
      */
     public Robot[] getSensedRobots() {
         if(numberOfSensors == 0) return new Robot[0];
         return mySCs[0].senseNearbyGameObjects(Robot.class);
     }
     
     
     
     /**
      * Updates sense-related allied recycler information.
      * 
      * More specifically, identifies and locates lowest ID recycler so that it can turn
      * off if it's not the lowest...
      */
     public void updateAlliedRecyclerInformation() {
         if(numberOfSensors == 0) return;
         try {
             SensorController sensor = mySCs[0];
            int         lowest    = knowledge.myRobotID;
            MapLocation lowestLoc = knowledge.myLocation;
             for(Robot robot : sensor.senseNearbyGameObjects(Robot.class)) {
                 RobotInfo   robotInfo = sensor.senseRobotInfo(robot);
                 Team        team      = robot.getTeam();
                 if(team == knowledge.myTeam) {
                     ComponentType[] compTypes = robotInfo.components;
                     switch(robotInfo.chassis) {
                     case BUILDING:
                         for(ComponentType compType : compTypes) {
                             if(compType == ComponentType.RECYCLER) {
                                 int id = robot.getID();
                                System.out.println("found recycler with id " +
                                        String.valueOf(id));
                                 if(id < lowest) {
                                     lowest    = id;
                                     lowestLoc = robotInfo.location;
                                 }
                                 break;
                             }
                         }
                     }
                 }
                 else {
                     knowledge.numberOfSensedEnemies += 1;
                 }
             }
            knowledge.lowestAlliedRecyclerID         = lowest;
            knowledge.lowestAlliedRecyclerIDLocation = lowestLoc;
            
         }
         catch(Exception e) {
             knowledge.printExceptionMessage(e);
         }
     }
     
     
     /**
      * Looks at new components, sorts them, and returns the most interesting
      * 
      * @return        returns any component that might change the SpecificPlayer
      */
     // TODO: Allow returning of more than one component-type...
     public ComponentType updateComponents() {
         ComponentController[] newComps = myRC.newComponents();
         
         if(newComps.length == 0) return null;
         
         ComponentType result = null;
         
         for(ComponentController newComp: newComps) {
             switch(newComp.type()) { // TODO: handle IRON, JUMP, DROPSHIP, BUG, DUMMY
                                      // TODO: handle passives
             
             case BUILDING_MOTOR:
             case SMALL_MOTOR:
             case MEDIUM_MOTOR:
             case LARGE_MOTOR:
             case FLYING_MOTOR:
                 myMC = (MovementController) newComp;
                 break;
                 
             case ANTENNA:
             case DISH:
             case NETWORK:
                 myCC = (BroadcastController) newComp;
                 hasComm = true;
                 break;
                 
             case SATELLITE:
             case TELESCOPE:
             case SIGHT:
             case RADAR:
             case BUILDING_SENSOR:
                 mySCs[numberOfSensors] = (SensorController) newComp;
                 numberOfSensors += 1;
                 break;
                 
             case BLASTER:
                 result = ComponentType.BLASTER;
             case SMG:
             case RAILGUN:
             case HAMMER:
             case BEAM:
             case MEDIC: // Should MEDIC be under weapons?
                 myWCs[numberOfWeapons] = (WeaponController) newComp;
                 numberOfWeapons += 1;
                 break;
                 
             case CONSTRUCTOR:
                 result = ComponentType.CONSTRUCTOR;
                 myBC = (BuilderController) newComp;
                 break;
                 
             case RECYCLER:
                 result = ComponentType.RECYCLER;
                 myBC = (BuilderController) newComp;
                 break;
                 
             case FACTORY:
                 result = ComponentType.FACTORY;
                 myBC = (BuilderController) newComp;
                 break;
                 
             case ARMORY:
                 result = ComponentType.ARMORY;
                 myBC = (BuilderController) newComp;
                 break;
             }
         }
         return result;
     }
 }
