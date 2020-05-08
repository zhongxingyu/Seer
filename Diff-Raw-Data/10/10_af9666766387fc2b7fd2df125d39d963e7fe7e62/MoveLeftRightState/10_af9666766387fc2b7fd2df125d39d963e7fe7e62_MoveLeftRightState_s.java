 package navigation;
 
 import robocode.HitByBulletEvent;
 import robocode.ScannedRobotEvent;
 
 import com.jaxelson.EnemyBot;
 
 /**
  * Simple tracking state.
  * @author David McCoy
  */
 public class MoveLeftRightState
         extends State {
 
     // CONSTRUCTORS
 
     /**
      * Creates a new TrackState for the specified robot.
      * @param robot The ExtendedRobot object used to provide data and execute
      *              commands
      */
     public MoveLeftRightState(ExtendedBot robot) {
         super(robot);
     }
 
     // PUBLIC METHODS
 
     /**
      * Returns the statistics of how this state has performed against the
      * current target.
      * @return The NavigationStateStatistics object for the current state
      *         and target
      */
     public Statistics getStatistics() {
         if (statistics == null) {
             statistics = new Statistics();
         }        
         return statistics;
     }
 
     /**
      * Returns the name of this State.
      * @return A String containing the name of this State object
      */
     public String getName() {
         return "MoveLeftRightState";
     }
 
     /**
      * Returns whether the this state is valid (may be used under the
      * current circumstances).
      * @return A boolean indicating whether this State should be used
      */
     public boolean isValid() {
         return true;
     }
 
     /**
      * This method will be called to indicate the CommandListener should free
      * all resources and cease execution.
      */
     public void disable() {
         robot.removeEventListener(ON_HIT_BY_BULLET, this);
        robot.removeEventListener(ON_HIT_BY_BULLET, this);
         energy = 0;
         updateStatistics();
     }
 
     /**
      * This method will be called to indicate the CommandListener is free to
      * begin execution.
      */
     public void enable() {
     	robot.setAdjustRadarForGunTurn(true);
         startTime = robot.getTime();
         energy = robot.getEnergy();
         damageTaken = 0;
         robot.addEventListener(ON_HIT_BY_BULLET, this);
         robot.addEventListener(ON_SCANNED_ROBOT, this);
     }
 
     /**
      * This method will be called each turn to allow the DodgeState to
      * execute turn based instructions.
      */
     public void execute() {
 //        robot.setTurnRightRadians(targetBearing);
 //        robot.setAhead(100);
     	switch(state) {
     	case 0:
 //    		System.out.println("Case 0");
 //    		robot.turnGunTo(targetBearing);
 //  		robot.turnGunTo(Math.PI);
     		robot.setTurnRadarRightRadians(Math.PI*2);
             robot.turnTo(Math.PI/2);
     		if(robot.getTurnRemaining() == 0) state++;
     		break;
     	case 1:
 //    		System.out.println("Case 1");
 //    		System.out.println("\n\nTurning gun to: "+ targetBearing);
 //    		robot.turnGunTo(targetBearing);
     		robot.setAhead(robot.getDistanceToRightWall());
 //    		robot.turnGunTo(Math.PI*3/2);
 //    		System.out.println("Distance to wall: "+ robot.getDistanceToRightWall());
     		if(robot.getDistanceToRightWall() == 0) {
     			state++;
     		}
     		break;
     	case 2:
 //    		System.out.println("Case 2");
     		robot.turnTo(Math.PI*3/2);
     		if(robot.getTurnRemaining() == 0) state++;
     		break;
     	case 3:
 //    		System.out.println("Case 3");
     		robot.setAhead(robot.getDistanceToLeftWall());
 //    		System.out.println("distance to left wall: "+ robot.getDistanceToLeftWall());
     		if(robot.getDistanceToLeftWall() == 0) state = 0;
     		break;
     	default:
 //    		System.out.println("Default");
     	}
     	
         
     }
 
     /**
      * This method will be called when your robot is hit by a bullet.
      * @param event A HitByBulletEvent object containing the details of your
      *              robot being hit by a bullet
      */
     public void onHitByBullet(HitByBulletEvent event) {
         damageTaken += BotMath.calculateDamage(event.getPower());
     }
 
     /**
      * This method will be called when your robot sees another robot.<br>
      * NOTE: This class provides a blank instantiation of this method.
      * @param event A ScannedRobotEvent object containing the details of your
      *              robot's sighting of another robot
      */
     public void onScannedRobot(ScannedRobotEvent event) {
         targetBearing = event.getBearingRadians();
         targetAcquired = true;
         EnemyBot target = new EnemyBot(event, robot);
 //        target.printBot();
         
         robot.narrowRadarLock(event);
         
         robot.turnGunTo(target);
         robot.setFire(1.0);
     	
     }
 
     // PRIVATE METHODS
 
     /**
      * Recalculates state statistics.
      */
     private void updateStatistics() {
         statistics.update(robot.getOthers(),
                           damageTaken,
                           (robot.getTime() - startTime));
     }
 
     // INSTANCE VARIABLES
 
     // Ordinarily I would use accessor methods exclusively to access instance
     // variables, but in the interest of speed I have allowed direct access.
 
     private int state = 0;
     private boolean targetAcquired = false;
     
     /**
      * Last known bearing to the target bot
      */
     private double targetBearing;
     /**
      * The energy of the bot when this state was chosen
      */
     private double energy;
     /**
      * The total energy lost from bullet hits while this state has been
      * in use
      */
     private double damageTaken;
     /**
      * The time when this state was enabled.
      */
     private long startTime;
     /**
      * Used to track statistics of this state in battle
      */
     private static Statistics statistics;
 
 }
