 package navigation;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Rectangle2D;
 import java.util.ArrayList;
 import java.util.LinkedHashMap;
 
 import robocode.AdvancedRobot;
 import robocode.HitByBulletEvent;
 import robocode.RobotDeathEvent;
 import robocode.ScannedRobotEvent;
 import robocode.util.Utils;
 
 import com.jaxelson.BotUtility;
 import com.jaxelson.Enemies;
 import com.jaxelson.EnemyBot;
 import com.jaxelson.ExtendedPoint2D;
 
 /**
  * Wave Surfer based on http://robowiki.net/wiki/Wave_Surfing_Tutorial
  * @author Jason Axelson
  */
 
 
 public class WaveSurfing
         extends State {
 
 	/*
 	 * Wave surfing helper variables
 	 */
 
 	public static int BINS = 47;
 	public static double _surfStats[] = new double[BINS];
 	public ExtendedPoint2D _myLocation;     // our bot's location
 	public ExtendedPoint2D _enemyLocation;  // enemy bot's location
 
 	public ArrayList<EnemyWave> _enemyWaves;
 	public ArrayList<Integer> _surfDirections;
 	public ArrayList<Double> _surfAbsBearings;
 
 	public static double _oppEnergy = 100.0;
 	
 	private Enemies _enemies = new Enemies(robot);
 	static double _scanDir;
 	static Object sought;
 	static LinkedHashMap<String, Double> enemyHashMap;
 	
 	/** This is a rectangle that represents an 800x600 battle field,
 	    * used for a simple, iterative WallSmoothing method (by PEZ).
 	    * If you're not familiar with WallSmoothing, the wall stick indicates
 	    * the amount of space we try to always have on either end of the tank
 	    * (extending straight out the front or back) before touching a wall.
 	    */
 	    public Rectangle2D.Double _fieldRect
 	        = new java.awt.geom.Rectangle2D.Double(BotUtility.botWidth/2, BotUtility.botWidth/2,
 	        		robot.getBattleFieldWidth() - BotUtility.botWidth,
 	        		robot.getBattleFieldHeight() - BotUtility.botWidth);
 	    public static double WALL_STICK = 160;
 
 
     // CONSTRUCTORS
 
     /**
      * Creates a new TrackState for the specified robot.
      * @param robot The ExtendedRobot object used to provide data and execute
      *              commands
      */
     public WaveSurfing(ExtendedBot robot) {
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
             BotUtility.writeObject(statistics, "filename", robot);
         }        
         return statistics;
     }
 
     /**
      * Returns the name of this State.
      * @return A String containing the name of this State object
      */
     public String getName() {
         return "WaveSurfing";
     }
 
     /**
      * Returns whether the this state is valid (may be used under the
      * current circumstances).
      * @return A boolean indicating whether this State should be used
      */
     public boolean isValid() {
     	return (robot.getNumEnemies() <= 1);
     }
 
     /**
      * This method will be called to indicate the CommandListener should free
      * all resources and cease execution.
      */
     public void disable() {
         robot.removeEventListener(ON_HIT_BY_BULLET, this);
         robot.removeEventListener(ON_SCANNED_ROBOT, this);
        robot.addEventListener(ON_ROBOT_DEATH, this);
         robot.removeEventListener(ON_PAINT, this);
         energy = 0;
         updateStatistics();
     }
 
     /**
      * This method will be called to indicate the CommandListener is free to
      * begin execution.
      */
     public void enable() {
 //    	robot.setAdjustRadarForGunTurn(true);
         startTime = robot.getTime();
         energy = robot.getEnergy();
         damageTaken = 0;
         robot.addEventListener(ON_HIT_BY_BULLET, this);
         robot.addEventListener(ON_SCANNED_ROBOT, this);
         robot.addEventListener(ON_ROBOT_DEATH, this);
         robot.addEventListener(ON_PAINT, this);
         
         /** A collection of waves, to surf and gather stats on */
         _enemyWaves = new ArrayList<EnemyWave>();
         /** 
          * A collection of our direction in relation to the enemy in past ticks, 1 for
          * clock-wise, -1 for counter-clockwise.
          */
         _surfDirections = new ArrayList<Integer>();
         /** A collection of past absolute bearings to enemy. */
         _surfAbsBearings = new ArrayList<Double>();
         
         robot.setAdjustGunForRobotTurn(true);
         robot.setAdjustRadarForGunTurn(true);
         
 //        robot.setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
         
         // Melee Radar setup
         _scanDir = 1;
     	enemyHashMap = new LinkedHashMap<String, Double>(5, 2, true);
 
     }
 
     /**
      * This method will be called each turn to allow the DodgeState to
      * execute turn based instructions.
      */
     public void execute() {
 //    	if(robot.getRadarTurnRemainingRadians() == 0) {
 //    		System.out.println("Turning radar");
 //
 //        	robot.setTurnRadarRightRadians(Math.PI*2);
 //    	}
 
         if(robot.getOthers() > 1) {
         	robot.setTurnRadarRightRadians(_scanDir * Double.POSITIVE_INFINITY);
         	robot.scan();
         }
     }
 
     /**
      * This method will be called when your robot is hit by a bullet.
      * @param event A HitByBulletEvent object containing the details of your
      *              robot being hit by a bullet
      */
     public void onHitByBullet(HitByBulletEvent event) {
 //    	System.out.println("I've been hit!");
         damageTaken += BotMath.calculateDamage(event.getPower());
         
      // If the _enemyWaves collection is empty, we must have missed the
         // detection of this wave somehow.
         if (!_enemyWaves.isEmpty()) {
         	ExtendedPoint2D hitBulletLocation = new ExtendedPoint2D(
                 event.getBullet().getX(), event.getBullet().getY());
             EnemyWave hitWave = null;
 
             // look through the EnemyWaves, and find one that could've hit us.
             for (int x = 0; x < _enemyWaves.size(); x++) {
                 EnemyWave ew = (EnemyWave)_enemyWaves.get(x);
 
                 if (Math.abs(ew.distanceTraveled -
                     _myLocation.distance(ew.fireLocation)) < 50
                     && Math.abs(BotUtility.bulletVelocity(event.getBullet().getPower()) 
                         - ew.bulletVelocity) < 0.001) {
                     hitWave = ew;
                     break;
                 }
             }
 
             if (hitWave != null) {
                 logHit(hitWave, hitBulletLocation);
 
                 // We can remove this wave now, of course.
                 _enemyWaves.remove(_enemyWaves.lastIndexOf(hitWave));
             }
         }
     }
 
     
     public void onPaint(Graphics2D g) {
     	if(_debug >= 1) System.out.println("Now Painting");
         // Set the paint color to red
         g.setColor(java.awt.Color.RED);
         
         // Draw waves
         for(EnemyWave wave: _enemyWaves) {
         	wave.drawWave(g);
         }
         
     }
     
     public void onRobotDeath(RobotDeathEvent e) {
         _enemies.remove(e);
         sought = null;
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
 //        target.printBot();firePower
         String name = event.getName();
         LinkedHashMap<String, Double> ehm = enemyHashMap;
         
 //        robot.narrowRadarLock(target);
         ehm.put(name, robot.getHeadingRadians() + event.getBearingRadians());
 
         if(robot.getOthers() <= 1) {
         	robot.narrowRadarLock(target);
         } else if ((name == sought || sought == null) && ehm.size() == robot.getOthers()) {
     	_scanDir = Utils.normalRelativeAngle(ehm.values().iterator().next()
                 - robot.getRadarHeadingRadians());
             sought = ehm.keySet().iterator().next();
         }
 
         
         robot.circularTargeting(target);
 //        robot.headOnTargeting(target, 3.0);
 //        robot.setFire(1.0);
 //        if(target.getEnergy() <= 10) {
 //        	robot.linearTargeting(target, 0.1);
 //        }
         
         
         _myLocation = new ExtendedPoint2D(robot.getX(), robot.getY());
 
         double lateralVelocity = robot.getVelocity()*Math.sin(event.getBearingRadians());
         double absBearing = event.getBearingRadians() + robot.getHeadingRadians();
 
 //        robot.setTurnRadarRightRadians(Utils.normalRelativeAngle(absBearing
 //            - robot.getRadarHeadingRadians()) * 2);
 
         _surfDirections.add(0,
             new Integer((lateralVelocity >= 0) ? 1 : -1));
         _surfAbsBearings.add(0, new Double(absBearing + Math.PI));
 
         
         double bulletPower = _oppEnergy - event.getEnergy();
         if (bulletPower < 3.01 && bulletPower > 0.09
             && _surfDirections.size() > 2) {
         	
             EnemyWave ew = new EnemyWave();
             ew.fireTime = robot.getTime() - 1;
             ew.bulletVelocity = BotUtility.bulletVelocity(bulletPower);
             ew.distanceTraveled = BotUtility.bulletVelocity(bulletPower);
             ew.direction = ((Integer)_surfDirections.get(2)).intValue();
             ew.directAngle = ((Double)_surfAbsBearings.get(2)).doubleValue();
             ew.fireLocation = (ExtendedPoint2D)_enemyLocation.clone(); // last tick
 
             _enemyWaves.add(ew);
         }
         
         _oppEnergy = event.getEnergy();
 
         // update after EnemyWave detection, because that needs the previous
         // enemy location as the source of the wave
         _enemyLocation = BotUtility.project(_myLocation, absBearing, event.getDistance());
 
         updateWaves();
 //        System.out.println("Now surfing");
         doSurfing();
     	
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
 
     @SuppressWarnings("unused")
 	private boolean targetAcquired = false;
     
     /**
      * Last known bearing to the target bot
      */
     @SuppressWarnings("unused")
 	private double targetBearing;
     /**
      * The energy of the bot when this state was chosen
      */
     @SuppressWarnings("unused")
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
     
     
     class EnemyWave {
     	ExtendedPoint2D fireLocation;
     	long fireTime;
     	double bulletVelocity, directAngle, distanceTraveled;
     	int direction;
 
     	public EnemyWave() {
     	}
     	
     	public void drawWave(Graphics2D g) {
     		BotUtility.drawCircle(g, fireLocation, distanceTraveled);
     	}
     }
 
     public double wallSmoothing(ExtendedPoint2D botLocation, double angle, int orientation) {
         while (!_fieldRect.contains(BotUtility.project(botLocation, angle, WALL_STICK))) {
             angle += orientation*0.05;
         }
         return angle;
     }
 
     public static void setBackAsFront(AdvancedRobot robot, double goAngle) {
         double angle =
             Utils.normalRelativeAngle(goAngle - robot.getHeadingRadians());
         if (Math.abs(angle) > (Math.PI/2)) {
             if (angle < 0) {
                 robot.setTurnRightRadians(Math.PI + angle);
             } else {
                 robot.setTurnLeftRadians(Math.PI - angle);
             }
             robot.setBack(100);
         } else {
             if (angle < 0) {
                 robot.setTurnLeftRadians(-1*angle);
            } else {
                 robot.setTurnRightRadians(angle);
            }
             robot.setAhead(100);
         }
     }
 
     public void updateWaves() {
         for (int x = 0; x < _enemyWaves.size(); x++) {
             EnemyWave ew = (EnemyWave)_enemyWaves.get(x);
 
             ew.distanceTraveled = (robot.getTime() - ew.fireTime) * ew.bulletVelocity;
             if (ew.distanceTraveled >
                 _myLocation.distance(ew.fireLocation) + 50) {
                 _enemyWaves.remove(x);
                 x--;
             }
         }
     }
     
     public EnemyWave getClosestSurfableWave() {
         double closestDistance = Double.POSITIVE_INFINITY;
         EnemyWave surfWave = null;
 
         for (int x = 0; x < _enemyWaves.size(); x++) {
             EnemyWave ew = _enemyWaves.get(x);
             double distance = _myLocation.distance(ew.fireLocation)
                 - ew.distanceTraveled;
 
             if (distance > ew.bulletVelocity && distance < closestDistance) {
                 surfWave = ew;
                 closestDistance = distance;
             }
         }
 
         return surfWave;
     }
 
 	/**
 	 * Given the EnemyWave that the bullet was on, and the point where we were
 	 * hit, calculate the index into our stat array for that factor.
 	 * 
 	 * @param ew EnemyWave bullet was on
 	 * @param targetLocation point we were hit
 	 * @return index of our stat array for that factor
 	 */
 	public static int getFactorIndex(EnemyWave ew, ExtendedPoint2D targetLocation) {
 		double offsetAngle = (BotUtility.absoluteBearing(ew.fireLocation, targetLocation) - ew.directAngle);
         double factor = Utils.normalRelativeAngle(offsetAngle)
             / BotUtility.maxEscapeAngle(ew.bulletVelocity) * ew.direction;
 
         return (int)BotUtility.limit(0,
             (factor * ((BINS - 1) / 2)) + ((BINS - 1) / 2),
             BINS - 1);
     }
 
 	/**
 	 * Given the EnemyWave that the bullet was on, and the point where we were
 	 * hit, update our stat array to reflect the danger in that area.
 	 * 
 	 * @param ew
 	 *            EnemyWave bullet was on
 	 * @param targetLocation
 	 *            point where we were hit
 	 */
     public void logHit(EnemyWave ew, ExtendedPoint2D targetLocation) {
         int index = getFactorIndex(ew, targetLocation);
 
         for (int x = 0; x < BINS; x++) {
             // for the spot bin that we were hit on, add 1;
             // for the bins next to it, add 1 / 2;
             // the next one, add 1 / 5; and so on...
             _surfStats[x] += 1.0 / (Math.pow(index - x, 2) + 1);
         }
         System.out.println("Going to update stats");
         updateStatistics();
     }
     
     public ExtendedPoint2D predictPosition(EnemyWave surfWave, int direction) {
         ExtendedPoint2D predictedPosition = (ExtendedPoint2D)_myLocation.clone();
         double predictedVelocity = robot.getVelocity();
         double predictedHeading = robot.getHeadingRadians();
         double maxTurning, moveAngle, moveDir;
 
         int counter = 0; // number of ticks in the future
         boolean intercepted = false;
 
         do {    // the rest of these code comments are rozu's
             moveAngle =
                 wallSmoothing(predictedPosition, BotUtility.absoluteBearing(surfWave.fireLocation,
                 predictedPosition) + (direction * (Math.PI/2)), direction)
                 - predictedHeading;
             moveDir = 1;
 
             if(Math.cos(moveAngle) < 0) {
                 moveAngle += Math.PI;
                 moveDir = -1;
             }
 
             moveAngle = Utils.normalRelativeAngle(moveAngle);
 
             // maxTurning is built in like this, you can't turn more then this in one tick
             maxTurning = Math.PI/720d*(40d - 3d*Math.abs(predictedVelocity));
             predictedHeading = Utils.normalRelativeAngle(predictedHeading
                 + BotUtility.limit(-maxTurning, moveAngle, maxTurning));
 
             // this one is nice ;). if predictedVelocity and moveDir have
             // different signs you want to breack down
             // otherwise you want to accelerate (look at the factor "2")
             predictedVelocity +=
                 (predictedVelocity * moveDir < 0 ? 2*moveDir : moveDir);
             predictedVelocity = BotUtility.limit(-8, predictedVelocity, 8);
 
             // calculate the new predicted position
             predictedPosition = BotUtility.project(predictedPosition, predictedHeading,
                 predictedVelocity);
 
             counter++;
 
             if (predictedPosition.distance(surfWave.fireLocation) <
                 surfWave.distanceTraveled + (counter * surfWave.bulletVelocity)
                 + surfWave.bulletVelocity) {
                 intercepted = true;
             }
         } while(!intercepted && counter < 500);
 
         return predictedPosition;
     }
     
     public double checkDanger(EnemyWave surfWave, int direction) {
         int index = getFactorIndex(surfWave,
             predictPosition(surfWave, direction));
 
         return _surfStats[index];
     }
 
     public void doSurfing() {
         EnemyWave surfWave = getClosestSurfableWave();
 
         if (surfWave == null) { return; }
 
         double dangerLeft = checkDanger(surfWave, -1);
         double dangerRight = checkDanger(surfWave, 1);
 
         double goAngle = BotUtility.absoluteBearing(surfWave.fireLocation, _myLocation);
         if (dangerLeft < dangerRight) {
             goAngle = wallSmoothing(_myLocation, goAngle - (Math.PI/2), -1);
         } else {
             goAngle = wallSmoothing(_myLocation, goAngle + (Math.PI/2), 1);
         }
 
         setBackAsFront(robot, goAngle);
     }
 }
