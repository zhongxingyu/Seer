 package RoboDojo;
 import robocode.*;
 import java.awt.Color;
 import robocode.util.*;
 
 public class NanoSatanRT extends AdvancedRobot
 {
 	// RT is Random Targeting
 	public void run() {
 		setColors(Color.red, Color.red, Color.black);
 		setScanColor(Color.red);
 		setBulletColor(Color.black);
 		if (Math.random() > 0.5) {
 			turnLeft(getHeading() % 90);
 		} else {
 			turnRight(getHeading() % 90);
 		}
 		while(true) {
 			setTurnGunRight(360);
 			ahead(100);
 			if (Math.random() < 0.5) {
 				turnLeft(90);
 			} else {
 				turnRight(90);
 			}
 		}
 	}
 	
 	public void onScannedRobot(ScannedRobotEvent e) {
 		//This is the (unused) Linear Targeting
 		//double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
 		//setTurnGunRightRadians(Utils.normalRelativeAngle(absoluteBearing - getGunHeadingRadians() + (e.getVelocity() * Math.sin(e.getHeadingRadians() - absoluteBearing) / 13.0)));
 		//setFire(3.0);
 		// This is the Random Targeting
 		double randomGuessFactor = (Math.random() - .5) * 2;
 		double bulletPower = 3;
     	double maxEscapeAngle = Math.asin(8.0/(20 - (3 * bulletPower)));
    		double firingAngle = randomGuessFactor * maxEscapeAngle;
     	double absBearingToEnemy = e.getBearingRadians() + getHeadingRadians();
    		setTurnGunRightRadians(Utils.normalRelativeAngle(
         absBearingToEnemy + firingAngle - getGunHeadingRadians()));
     	fire(bulletPower);
 	}
 	
 	public void onHitWall(HitWallEvent e) {
 		System.out.println("I hit a wall");
 		setTurnGunRight(360);
		turnRight(180);
 		ahead(200);
 	}
 	public void onWin(WinEvent e) {
 		System.out.println("I win!");
 	}
 }
