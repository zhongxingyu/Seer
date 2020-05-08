 package team039.common;
 
 import battlecode.common.*;
 
 /**
  * Knowledge class keeps track of information to be used anywhere in relevant Robot's code,
  * i.e. in the RobotPlayer or in the SpecificPlayer or the ComponentHandler or anywhere else
  * it might end up being useful.
  * 
  * Similarly, methods that might want to be used anywhere should be put here (example: the
  * getExceptionMessage method).
  * @author Jason
  *
  */
 public class Knowledge {
     
     private final RobotController myRC;
     
     /*** State ***/
     public         RobotState          myState;
     
     /*** Constants ***/
     public  final  Team                myTeam;
     public  final  MapLocation         myStartLocation;
     public  final  int                 myRobotID;
     public  final  Robot               myRobot;
     
     /*** Round constants ***/
     public         MapLocation         myLocation;
     public         MapLocation         myPreviousLocation;
     public         Direction           myMovementDirection;
     public         Direction           myDirection;
     public         double              previousFlux    = 0;
     public         double              deltaFlux       = 0;  
     public         int                 roundNum;
     
     /*** Sense information ***/
     public         int                 numberOfSensedEnemies;
     public         int                 lowestAlliedRecyclerID = 65536;
     public         MapLocation         lowestAlliedRecyclerIDLocation;
     public         MapLocation         startingTurnedOnRecyclerLocation;
     public         MapLocation[]       startingUnminedMineLocations = new MapLocation[2];
 
     /* Each piece of data should be time stamped somehow.  Otherwise, when a
      * robot receives two conflicting pieces of information it won't know
      * which one to choose. Clearly, the more up-to-date info is more important.
      */
 
 
     /*** Locations of fixed objects ***/
     // I feel that they should be uncommented as they come into use.
     /***public         MapLocation[]       unminedMineLocations     = new MapLocation[100];
     public         MapLocation[]       ourMineLocations         = new MapLocation[100];
     public         MapLocation[]       theirMineLocations       = new MapLocation[100];
     public         MapLocation[]       minedOutLocations        = new MapLocation[100];
     public         MapLocation[]       debrisLocations          = new MapLocation[100];
     public         MapLocation[]       destroyedDebrisLocations = new MapLocation[100];
     public         MapLocation[]       ourRecyclerLocations     = new MapLocation[100];
     public         MapLocation[]       ourFactoryLocations      = new MapLocation[100];
     public         MapLocation[]       ourArmoryLocations       = new MapLocation[100];
     public         MapLocation[]       ourBuildingLocations     = new MapLocation[100];
     public         MapLocation[]       theirRecyclerLocations   = new MapLocation[100];
     public         MapLocation[]       theirFactoryLocations    = new MapLocation[100];
     public         MapLocation[]       theirArmoryLocations     = new MapLocation[100];
     public         MapLocation[]       theirBuildingLocations   = new MapLocation[100];***/
     
     
     
     /**
      * Sole constructor, initializes final variables.
      * 
      * @param    rc    RobotController associated with this RobotPlayer
      */
     public Knowledge (RobotController rc) {
         myRC            = rc;
         myTeam          = myRC.getTeam();
         myStartLocation = myRC.getLocation();
         myRobot         = myRC.getRobot();
         myRobotID       = myRobot.getID();
        myLocation      = myStartLocation;
     }
     
     
     
     /**
      * Called at the beginning of each round, should update all relevant information.
      */
     public void update () {
         // Determine delta flux
         // TODO: ignore delta's associated with building, etc.
         // TODO: recognize delta-delta flux that signifies loss of unit, creation of unit,
         //            creation of mine, etc.
         deltaFlux = myRC.getTeamResources() - previousFlux;
         previousFlux = deltaFlux + previousFlux;
         
         roundNum = Clock.getRoundNum();
         
         MapLocation myNewLocation = myRC.getLocation();
         if(myNewLocation != myLocation) {
             myPreviousLocation = myLocation;
             myLocation = myNewLocation;
             myMovementDirection = myPreviousLocation.directionTo(myNewLocation);
         }
         myDirection = myRC.getDirection();
     }
 
 
     /**
      * Encodes knowledge acquired since a certain round
      */
     public void encodeMessage ( Integer timeStamp )
     {
         /* The timestamp of each map location should be kept as the time when
          * it was found, rather than the time that it was sent in a message.
          * Proposed format: Use MapLocations for map locations, integers for
          * timestamps, and strings for designations such as recycler, factory.
          */
     }
 
     /**
      * Changes knowledge based on information from a message.  Needs to be
      * reasonably efficient. Main restrictions include message size and processing;
      * decoding a lot of messages could be processor intensive, so there should
      * be a way of filtering messages. Might be complex enough for a message
      * handler class.
      */
     public void decodeMessage ( Message msgReceived )
     {
         //should increase knowledge based on new information
         //also, do we want a separate message handler class or should we do that here?
     }
     
     
     
     /**
      * Prints string to help debug, as well as exception stack trace.
      * @param   e   Exception
      */
     public void printExceptionMessage(Exception e) {
         System.out.println("Robot " + myRC.getRobot().getID() + 
                            " during round " + Clock.getRoundNum() + 
                            " caught exception:");
         e.printStackTrace();
     }
 
 }
