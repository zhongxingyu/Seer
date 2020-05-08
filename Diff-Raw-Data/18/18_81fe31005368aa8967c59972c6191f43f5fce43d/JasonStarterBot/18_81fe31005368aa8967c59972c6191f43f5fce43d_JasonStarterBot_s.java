 package com.jaxelson;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 
 import robocode.ScannedRobotEvent;
 import robocode.WinEvent;
 import robocode.util.Utils;
 
 
 public class JasonStarterBot extends ExtendedBot {
 	private Integer debug = 1;
 	private Integer state = 0;
 	EnemyBot target;
 	
 	
 	public void run() {
 		setTurnRadarRightRadians(Double.POSITIVE_INFINITY);
 //		setAdjustGunForRobotTurn(true);
 		setAdjustRadarForGunTurn(true);
 		setBulletColor(Color.red);
         while (true) {
         	
 //        	System.out.println("Looping!");
         	if(debug >= 1) {
         		printMyInfo();
         	}
         	switch(state) {
         	case 0:	// Face Left
         		System.out.println("State 0");
         		
         		
         		System.out.println("Starting at: "+ getHeadingRadians());
         		System.out.println("           : "+ getHeading());
         		
 //        		double desiredAngle = Math.PI/2;
 //        		double currentAngle = getHeadingRadians();
 //        		double turnDistanceRad = currentAngle - desiredAngle;
 //        		setTurnLeftRadians(turnDistanceRad);
         		
        		BotUtility.turnTo(Math.PI/2, this);
         		
         		if(Utils.isNear(getHeadingRadians(), Math.PI/2)) {
         			setAhead(1000);
         			state++;
         		}
         		break;
         	case 1: // Move forward
         		System.out.println("State 1");
         		if(getDistanceToRightWall() <= 300) {
         			setMaxVelocity(1);
         		}
         		if(getDistanceToRightWall() <= 200) {
         			stop();
         			state++;
         		}
         	case 2:
         		System.out.println("State 2");
 //        		setFire(1.0);
         		if(getDistanceRemaining() <= 0) {
         			state++;
         		}
         		break;
         	case 3:
 //        		System.out.println("Target: "+ target.toString());
         		System.out.println("My bot time:"+ getTime());
         		System.out.println("Last seen time: "+ target.timeSinceSeen(this.getTime()));
         		System.out.println();
         		//setTurnGunRightRadians(3);
         	}
             
 //            waitFor(new TurnCompleteCondition(this));
         	scan();
             execute();
 //            setAhead(-100);
             //setTurnGunRight(360);
             //setBack(100);
             //setTurnGunRight(360);
             // Check for new targets.
             // Only necessary for Narrow Lock because sometimes our radar is already
             // pointed at the enemy and our onScannedRobot code doesn't end up telling
             // it to turn, so the system doesn't automatically call scan() for us
             // [see the javadocs for scan()].
             scan();
         }
     }
  
     public void onScannedRobot(ScannedRobotEvent e) {
     	target = new EnemyBot(e);
 //  	System.out.println("Scanned bot: "+ target.toString());
 //    	target.printBot();
     	if(debug >= 2) {
     		System.out.println("Found robot: "+ e.getName());
     		target.printBot();
     	}
     	double radarTurn =
     		// Absolute bearing to target
     		getHeadingRadians() + e.getBearingRadians()
     		// Subtract current radar heading to get turn required
     		- getRadarHeadingRadians();
 
     	if(state >= 1) {
     		setTurnRadarRightRadians(2.0 * Utils.normalRelativeAngle(radarTurn));
     	}
     	
     	double gunTurn = getHeadingRadians() + e.getBearingRadians() - getGunHeadingRadians();
     	if(state >= 1) {
     		setTurnGunRightRadians(Utils.normalRelativeAngle(gunTurn));
     	}
     	if(state >= 3) {
     		setFire(1.0);
     	}
     }
     
     public void onWin(WinEvent e) {
     	System.out.println("I win!");
     	setAhead(200);
     	setTurnRightRadians(Math.PI);
     }
     
     /**
      * Prints information about myself
      */
     public void printMyInfo() {
     	System.out.println("Heading: "+ this.getHeading());
     	System.out.println("Heading (radians): "+ this.getHeadingRadians());
     	System.out.println("Distance remaining: "+ this.getDistanceRemaining());
     }
     
     public void onPaint(Graphics2D g) {
         // Set the paint color to red
         g.setColor(java.awt.Color.RED);
         // Paint a filled rectangle at (50,50) at size 100x150 pixels
 //        g.fillRect(50, 50, 100, 150);
 //        g.drawOval(new Double(getX()).intValue(), new Double(getY()).intValue(), 30, 30);
 //        g.drawOval(BotUtility.getCenterX(this).intValue(), BotUtility.getCenterY(this).intValue(), 100, 100);
         
         drawCenteredCircle(g, 200);
         
     }
 }
