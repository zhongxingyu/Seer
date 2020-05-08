 package com.jaxelson;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Point2D;
 
 import robocode.ScannedRobotEvent;
 import robocode.TeamRobot;
 import robocode.util.Utils;
 
 public class ExtendedBotUH extends TeamRobot {
 	public static final double DOUBLE_PI = (Math.PI * 2);
 	public static final double HALF_PI = (Math.PI / 2);
 	
 	
 	public void drawCircleAroundBot(Graphics2D g, Double radius) {
     	double x = getX();
         double y = getY();
 		BotUtility.drawCircle(g, new ExtendedPoint2D(x, y), radius);
     }
 	
     public Double getDistanceToRightWall() {
     	double botRightEdge = getX() + BotUtility.botWidth/2;
     	double rightWallLocation = getBattleFieldWidth();
     	return rightWallLocation - botRightEdge;
     }
     
     public Double getDistanceToLeftWall() {
     	double botLeftEdge = getX() - BotUtility.botWidth/2;
     	return botLeftEdge;
     }
     /**
      * Turns to the desired angle
      * @param desiredAngle the desired angle in radians
      * @return how far is needed to turn
      */
 	public Double turnTo(double desiredAngle) {
 		double currentAngle = this.getHeadingRadians();
 		double turnDistanceRad = currentAngle - desiredAngle;
 		turnDistanceRad = Utils.normalRelativeAngle(turnDistanceRad);
 		this.setTurnLeftRadians(turnDistanceRad);
 		
 		return Math.abs(turnDistanceRad);
 	}
 	
 	/**
 	 * Turns radar to the desired angle
 	 * @param desiredAngle
 	 * @return radians needed to turn
 	 */
 	public Double turnRadarTo(double desiredAngle) {
 		double currentAngle = this.getRadarHeadingRadians();
 		double turnDistanceRad = currentAngle - desiredAngle;
 		turnDistanceRad = Utils.normalRelativeAngle(turnDistanceRad);
 		this.setTurnRadarLeftRadians(turnDistanceRad);
 		
 		return Math.abs(turnDistanceRad);
 	}
 	
 	/**
 	 * Turns the gun to face in the desired absolute angle
 	 * @param desiredAngle for gun to face in radians
 	 * @return distance gun needs to turn in radians
 	 */
 	public Double turnGunTo(double desiredAngle) {
 		double currentAngle = this.getGunHeadingRadians();
 		double turnDistanceRad = currentAngle - desiredAngle;
 		turnDistanceRad = Utils.normalRelativeAngle(turnDistanceRad);
 		this.setTurnGunLeftRadians(turnDistanceRad);
 		
 		return Math.abs(turnDistanceRad);
 	}
 	
 	/**
 	 * Turns the gun to face the given target (not doing any prediction for now)
 	 * @param target to face gun at
 	 * @return distance gun needs to travel
 	 */
 	public Double turnGunTo(EnemyBot target) {
 		double targetBearing = target.getBearingRadians();
 		double desiredAngle = targetBearing + this.getHeadingRadians();
 		return turnGunTo(desiredAngle);
 	}
 
 	public void narrowRadarLock(ScannedRobotEvent event) {
 		narrowRadarLock(event, 1.9);
 	}
 	
 	/**
 	 * Executes a narrow radar lock<br>
 	 * http://robowiki.net/wiki/Radar#Narrow_lock
 	 * @param event scanned robot event (may be replaced by an enemy in the future)
 	 * @param factor narrow lock factor (how "narrow" lock is), typical values 1.0, 1.9, 2.0
 	 */
 	public void narrowRadarLock(ScannedRobotEvent event, Double factor) {
 		double radarTurn =
 			// Absolute bearing to target
 			this.getHeadingRadians() + event.getBearingRadians()
 			// Subtract current radar heading to get turn required
 			- this.getRadarHeadingRadians();
 
 		this.setTurnRadarRightRadians(factor * Utils.normalRelativeAngle(radarTurn));
 	}
 	
 	
 	public void turnGunToXY(double x, double y, double power)
 	{
 		double angle = calculateBearingToXYRadians(getX(),getY(),getHeadingRadians(),x,y);
 		if(angle > 0) {
 			setTurnRightRadians(angle/3);
 			setTurnGunRightRadians(2*angle/3);
 		} else {
 			setTurnLeftRadians(-1*angle/3);
 			setTurnGunLeftRadians(-2*angle/3);
 		}
 	}
 	
 	public void quickestScan(double velocity, Boolean left)
 	{
 		double magVelocity = Math.abs(velocity);
 		double turnRate = 10-0.75*magVelocity;
 		double totalRates = turnRate+20+45;
 		if(left)
 		{
 			setTurnLeftRadians(turnRate/totalRates*DOUBLE_PI);
 			setTurnGunLeftRadians(20f/totalRates*DOUBLE_PI);
 			setTurnRadarLeftRadians(45f/totalRates*DOUBLE_PI);
 		}
 		else
 		{
 			setTurnRightRadians(turnRate/totalRates*DOUBLE_PI);
 			setTurnGunRightRadians(20f/totalRates*DOUBLE_PI);
 			setTurnRadarRightRadians(45f/totalRates*DOUBLE_PI);
 		}
 	}
 	
 	public void quickScan(Boolean left)
 	{
 		int totalRates = 20+45;
 		if(left)
 		{
 			setTurnGunLeftRadians(20f/totalRates*DOUBLE_PI);
 			setTurnRadarLeftRadians(45f/totalRates*DOUBLE_PI);
 		}
 		else
 		{
 			setTurnGunRightRadians(20f/totalRates*DOUBLE_PI);
 			setTurnRadarRightRadians(45f/totalRates*DOUBLE_PI);
 		}	
 	}
 	
 	//--- Math helper functions---//
 	public double calculateBearingToXYRadians(double sourceX, double sourceY,
 	    double sourceHeading, double targetX, double targetY) {
 	        return normalizeRelativeAngleRadians(
 	           Math.atan2((targetX - sourceX), (targetY - sourceY)) -
 	               sourceHeading);
 	    }
 
 	public double normalizeAbsoluteAngleRadians(double angle) {
 	   if (angle < 0) {
 	        return (DOUBLE_PI + (angle % DOUBLE_PI));
 	    } else {
 	        return (angle % DOUBLE_PI);
 	    }
 	}
 
 	public static double normalizeRelativeAngleRadians(double angle) {
 	    double trimmedAngle = (angle % DOUBLE_PI);
 	    if (trimmedAngle > Math.PI) {
 	        return -(Math.PI - (trimmedAngle % Math.PI));
 	    } else if (trimmedAngle < -Math.PI) {
 	        return (Math.PI + (trimmedAngle % Math.PI));
 	    } else {
 	        return trimmedAngle;
 	    }
 	}
 	
 	public void headOnTargeting(EnemyBot target, double firePower) {
 		this.turnGunTo(target);
         this.setFire(firePower);
 	}
 	
 	public void linearTargeting(ScannedRobotEvent e) {
 		double bulletPower = Math.min(3.0,getEnergy());
 		double myX = getX();
 		double myY = getY();
 		double absoluteBearing = getHeadingRadians() + e.getBearingRadians();
 		double enemyX = getX() + e.getDistance() * Math.sin(absoluteBearing);
 		double enemyY = getY() + e.getDistance() * Math.cos(absoluteBearing);
 		double enemyHeading = e.getHeadingRadians();
 		double enemyVelocity = e.getVelocity();
 
 
 		double deltaTime = 0;
 		double battleFieldHeight = getBattleFieldHeight(), 
 		       battleFieldWidth = getBattleFieldWidth();
 		double predictedX = enemyX, predictedY = enemyY;
 		while((++deltaTime) * (20.0 - 3.0 * bulletPower) < 
 		      Point2D.Double.distance(myX, myY, predictedX, predictedY)){		
 			predictedX += Math.sin(enemyHeading) * enemyVelocity;	
 			predictedY += Math.cos(enemyHeading) * enemyVelocity;
 			if(	predictedX < 18.0 
 				|| predictedY < 18.0
 				|| predictedX > battleFieldWidth - 18.0
 				|| predictedY > battleFieldHeight - 18.0){
 				predictedX = Math.min(Math.max(18.0, predictedX), 
 		                    battleFieldWidth - 18.0);	
 				predictedY = Math.min(Math.max(18.0, predictedY), 
 		                    battleFieldHeight - 18.0);
 				break;
 			}
 		}
 		double theta = Utils.normalAbsoluteAngle(Math.atan2(
 		    predictedX - getX(), predictedY - getY()));
 
 		setTurnGunRightRadians(Utils.normalRelativeAngle(theta - getGunHeadingRadians()));
 		fire(bulletPower);
 	}
 }
