 package petrichor;
 import robocode.*;
 import java.awt.Color;
 
 /**
  * Petrichor - a robot by Piper Chester and Michael Timbrook
  */
 public class Petrichor extends AdvancedRobot
 {
 	/**
	 * Petrichor's default behavior. Initialization of robot occurs here.
 	 */
 	public void run() {
 	
 		setColors(Color.blue, Color.cyan, Color.green); // Body, gun, radar
 
 		// Robot main loop.
 		while(true) {
 			ahead(100);
 			turnGunRight(360);
 			back(100);
 			turnGunRight(360);
 		}
 	}
 
 	/**
 	 * What to do when you see another robot.
 	 */
 	public void onScannedRobot(ScannedRobotEvent e) {
 		fire(5);
 	}
 
 	/**
 	 * What to do when you're hit by a bullet.
 	 */
 	public void onHitByBullet(HitByBulletEvent e) {
 		back(10);
 	}
 	
 	/**
 	 * onHitWall: What to do when you hit a wall
 	 */
 	public void onHitWall(HitWallEvent e) {
 		back(20);
 	}	
 }
