 package louis.driver;
 
 import robocode.AdvancedRobot;
 import robocode.Bullet;
 import robocode.BulletHitEvent;
 import robocode.BulletHitBulletEvent;
 import robocode.BulletMissedEvent;
 import robocode.Condition;
 import robocode.DeathEvent;
 import robocode.HitByBulletEvent;
 import robocode.HitRobotEvent;
 import robocode.HitWallEvent;
 import robocode.RobocodeFileOutputStream;
 import robocode.ScannedRobotEvent;
 import robocode.WinEvent;
 import static robocode.util.Utils.normalRelativeAngleDegrees;
 
 import java.awt.*;
 import java.io.*;
 
 public class Driver {
 	AdvancedRobot robot;
 	
 	public Driver(AdvancedRobot targetRobot) {
 		robot = targetRobot;
 	}
 	
 	/* Robot's actions */
 	
 	void	ahead(double distance) { robot.ahead(distance); }
 	void	back(double distance) { robot.back(distance); }
 	void	doNothing() { robot.doNothing(); }
 	void	fire(double power) { robot.fire(power); }
 	Bullet	fireBullet(double power) { return robot.fireBullet(power); }
 	double	getBattleFieldHeight() { return robot.getBattleFieldHeight(); }
 	double	getBattleFieldWidth() { return robot.getBattleFieldWidth(); }
 	double	getEnergy() { return robot.getEnergy(); }
 	double	getGunCoolingRate() { return robot.getGunCoolingRate(); }
 	double	getGunHeading() { return robot.getGunHeading(); }
 	double	getGunHeat() { return robot.getGunHeat(); }
 	double	getHeading() { return robot.getHeading(); }
 	double	getHeight() { return robot.getHeight(); }
 	String	getName() { return robot.getName(); }
 	int	getNumRounds() { return robot.getNumRounds(); }
 	int	getOthers() { return robot.getOthers(); }
 	double	getRadarHeading() { return robot.getRadarHeading(); }
 	int	getRoundNum() { return robot.getRoundNum(); }
 	long	getTime() { return robot.getTime(); }
 	double	getVelocity() { return robot.getVelocity(); }
 	double	getWidth() { return robot.getWidth(); }
 	double	getX() { return robot.getX(); }
 	double	getY() { return robot.getY(); }
 //	void	resume() { robot.resume(); }
 	void	scan() { robot.scan(); }
 	void	setAdjustGunForRobotTurn(boolean independent) { robot.setAdjustGunForRobotTurn(independent); }
 	void	setAdjustRadarForGunTurn(boolean independent) { robot.setAdjustRadarForGunTurn(independent); }
 	void	setAdjustRadarForRobotTurn(boolean independent) { robot.setAdjustRadarForRobotTurn(independent); }
 	void	setAllColors(Color color) { robot.setAllColors(color); }
 	void	setBodyColor(Color color) { robot.setBodyColor(color); }
 	void	setBulletColor(Color color) { robot.setBulletColor(color); }
 	void	setColors(Color bodyColor, Color gunColor, Color radarColor) { robot.setColors(bodyColor, gunColor, radarColor); } 
 	void	setColors(Color bodyColor, Color gunColor, Color radarColor, Color bulletColor, Color scanArcColor) 
 	{ robot.setColors(bodyColor, gunColor, radarColor, bulletColor, scanArcColor); }
 	void	setGunColor(Color color) { robot.setGunColor(color); }
 	void	setRadarColor(Color color) { robot.setRadarColor(color); }
 	void	setScanColor(Color color) { robot.setScanColor(color); }
 //	void	stop() { robot.stop(); }
 //	void	stop(boolean overwrite) { robot.stop(overwrite); }
 	void	turnGunLeft(double degrees) { robot.turnGunLeft(degrees); }
 	void	turnGunRight(double degrees) { robot.turnGunRight(degrees); }
 	void	turnLeft(double degrees) { robot.turnLeft(degrees); }
 	void	turnRadarLeft(double degrees) { robot.turnRadarLeft(degrees); }
 	void	turnRadarRight(double degrees) { robot.turnRadarRight(degrees); }
 	void	turnRight(double degrees) { robot.turnRight(degrees); }
 	
 	/* AdvanceRobot's actions */
 	
 	
 	void	addCustomEvent(Condition condition) { robot.addCustomEvent(condition); }	
 	void	execute() { robot.execute(); }
 	File	getDataFile(String filename) { return robot.getDataFile(filename); }
 	double	getDistanceRemaining() { return robot.getDistanceRemaining(); }
 	double	getGunHeadingRadians() { return robot.getGunHeadingRadians(); }
 	double	getGunTurnRemaining() { return robot.getGunTurnRemaining(); }
 	double	getGunTurnRemainingRadians() { return robot.getGunTurnRemainingRadians(); }
 	double	getHeadingRadians() { return robot.getHeadingRadians(); }
 	double	getRadarHeadingRadians() { return robot.getRadarHeadingRadians(); }
 	double	getRadarTurnRemaining() { return robot.getRadarTurnRemaining(); }
 	double	getRadarTurnRemainingRadians() { return robot.getRadarTurnRemainingRadians(); }
 	double	getTurnRemaining() { return robot.getTurnRemaining(); }
 	double	getTurnRemainingRadians() { return robot.getTurnRemainingRadians(); }
 	boolean	isAdjustGunForRobotTurn() { return robot.isAdjustGunForRobotTurn(); }
 	boolean	isAdjustRadarForGunTurn() { return robot.isAdjustRadarForGunTurn(); }
 	boolean	isAdjustRadarForRobotTurn() { return robot.isAdjustRadarForRobotTurn(); }
 	void	setAhead(double distance) { robot.setAhead(distance); }
 	void	setBack(double distance) { robot.setBack(distance); }
 	void	setFire(double power) { robot.setFire(power); }
 	Bullet	setFireBullet(double power) { return robot.setFireBullet(power); }
 	void	setInterruptible(boolean interruptible) { robot.setInterruptible(interruptible); }
 	void	setMaxTurnRate(double newMaxTurnRate) { robot.setMaxTurnRate(newMaxTurnRate); }
 	void	setMaxVelocity(double newMaxVelocity) { robot.setMaxVelocity(newMaxVelocity); }
 //	void	setResume() { robot.setResume(); }
 //	void	setStop() { robot.setStop(); }
 //	void	setStop(boolean overwrite) { robot.setStop(overwrite); }
 	void	setTurnGunLeft(double degrees) { robot.setTurnGunLeft(degrees); }
 	void	setTurnGunLeftRadians(double radians) { robot.setTurnGunLeftRadians(radians); }
 	void	setTurnGunRight(double degrees) { robot.setTurnGunRight(degrees); }
 	void	setTurnGunRightRadians(double radians) { robot.setTurnGunRightRadians(radians); }
 	void	setTurnLeft(double degrees) { robot.setTurnLeft(degrees); }
 	void	setTurnLeftRadians(double radians) { robot.setTurnLeftRadians(radians); }
 	void	setTurnRadarLeft(double degrees) { robot.setTurnRadarLeft(degrees); }
 	void	setTurnRadarLeftRadians(double radians) { robot.setTurnRadarLeftRadians(radians); }
 	void	setTurnRadarRight(double degrees) { robot.setTurnRadarRight(degrees); }
 	void	setTurnRadarRightRadians(double radians) { robot.setTurnRadarRightRadians(radians); }
 	void	setTurnRight(double degrees) { robot.setTurnRight(degrees); }
 	void	setTurnRightRadians(double radians) { robot.setTurnRightRadians(radians); }
 	void	turnGunLeftRadians(double radians) { robot.turnGunLeftRadians(radians); }
 	void	turnGunRightRadians(double radians) { robot.turnGunRightRadians(radians); }
 	void	turnLeftRadians(double radians) { robot.turnLeftRadians(radians); }
 	void	turnRadarLeftRadians(double radians) { robot.turnRadarLeftRadians(radians); }
 	void	turnRadarRightRadians(double radians) { robot.turnRadarRightRadians(radians); }
 	void	turnRightRadians(double radians) { robot.turnRightRadians(radians); }
 	void	removeCustomEvent(Condition condition) { robot.removeCustomEvent(condition); }
 	
 	/* Driver's action */
 	
 	public void	init() { }
 	public void	loop() { }
 	public void	onBulletHit(BulletHitEvent event) { }
 	public void	onBulletHitBullet(BulletHitBulletEvent event) { }
 	public void	onBulletMissed(BulletMissedEvent event) { }
 	public void	onHitByBullet(HitByBulletEvent event) { }
 	public void	onHitRobot(HitRobotEvent event) { }
 	public void	onHitWall(HitWallEvent event) { }
 	public void	onScannedRobot(ScannedRobotEvent event) { }
 }
