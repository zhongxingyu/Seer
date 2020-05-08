 package navigation;
 
 import java.util.LinkedHashMap;
 
 import robocode.HitByBulletEvent;
 import robocode.RobotDeathEvent;
 import robocode.ScannedRobotEvent;
 import robocode.util.Utils;
 
 /**
  * Melee Radar based on wiki code
  * http://robowiki.net/wiki/Radar#Oldest_Scanned
  * @author Jason Axelson
  */
 public class OldestScannedRadarState
         extends State {
 
 	public int _debug = 0;
 	
 	static LinkedHashMap<String, Double> enemyHashMap;
 	static double scanDir;
 	static Object sought;
 	
     // CONSTRUCTORS
 
     /**
      * Creates a new CannonFodderState for the specified robot.
      * @param robot The ExtendedRobot object used to provide data and execute
      *              commands
      */
     public OldestScannedRadarState(ExtendedBot robot) {
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
         return "OldestScannedRadarState";
     }
 
     /**
      * State is only valid if there are multiple enemies
      * @return true if there are multiple enemies
      */
     public boolean isValid() {
         if(robot.getNumEnemies() > 1) {
         	return true;
         } else {
         	return false;
         }
     }
 
     /**
     * This method will be called to indicate ttargethe CommandListener should free
      * all resources and cease execution.
      */
     public void disable() {
         robot.removeEventListener(ON_HIT_BY_BULLET, this);
         robot.removeEventListener(ON_SCANNED_ROBOT, this);
         robot.removeEventListener(ON_ROBOT_DEATH, this);
         updateStatistics();
     }
 
     /**
      * This method will be called to indicate the CommandListener is free to
      * begin execution.
      */
     public void enable() {
         startTime = robot.getTime();
         damageTaken = 0;
         robot.addEventListener(ON_HIT_BY_BULLET, this);
         robot.addEventListener(ON_SCANNED_ROBOT, this);
         robot.addEventListener(ON_ROBOT_DEATH, this);
         
         scanDir = 1;
         enemyHashMap = new LinkedHashMap<String, Double>(5, 2, true);
     }
 
     /**
      * This method will be called each turn to allow the DodgeState to
      * execute turn based instructions.
      */
     public void execute() {
     	if(_debug >= 1) System.out.println("MeleeRadarState executing");
     	robot.setTurnRadarRightRadians(scanDir * Double.POSITIVE_INFINITY);
         robot.scan();
     }
 
     /**
      * This method will be called when your robot is hit by a bullet.
      * @param event A HitByBulletEvent object containing the details of your
      *              robot being hit by a bullet
      */
     public void onHitByBullet(HitByBulletEvent event) {
         damageTaken += BotMath.calculateDamage(event.getPower());
     }
     
     public void onRobotDeath(RobotDeathEvent e) {
     	_enemies.update(e);
     	enemyHashMap.remove(e.getName());
         sought = null;
     }
 
     /**
      * This method will be called when your robot sees another robot.<br>
      * NOTE: This class provides a blank instantiation of this method.
      * @param event A ScannedRobotEvent object containing the details of your
      *              robot's sighting of another robot
      */
     public void onScannedRobot(ScannedRobotEvent e) {
         String name = e.getName();
         _enemies.update(e);
         LinkedHashMap<String, Double> ehm = enemyHashMap;
 
         ehm.put(name, robot.getHeadingRadians() + e.getBearingRadians());
 
         if ((name == sought || sought == null) && ehm.size() == robot.getOthers()) {
         	scanDir = Utils.normalRelativeAngle(ehm.values().iterator().next()
         			- robot.getRadarHeadingRadians());
         	sought = ehm.keySet().iterator().next();
         }
     }
 
     // PRIVATE METHODS
 
     /**
      * Recalculates state statistics.
      */
     private void updateStatistics() {
         statistics.update(robot.getOthers(),
                           damageTaken,
                           (robot.getTime() - startTime),
                           robot);
     }
 
     // INSTANCE VARIABLES
 
     // Ordinarily I would use accessor methods exclusively to access instance
     // variables, but in the interest of speed I have allowed direct access.
 
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
