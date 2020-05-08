 package g8;
 
 import java.awt.Color;
 
 import robocode.AdvancedRobot;
 import robocode.HitByBulletEvent;
 import robocode.ScannedRobotEvent;
 import robocode.WinEvent;
 
 public class iBot extends AdvancedRobot {
 	private double x, y;
 	
 	private enum BotPositionState {
 	    NOT_ON_TRACK, TOP, RIGHT, BOTTOM, LEFT
 	}
 	private BotPositionState positionState = BotPositionState.NOT_ON_TRACK;
 
 	private boolean turnedGun = false;
 	
 	public void run() {
 		turnGunRight(90);
 		//game loop
 		while(true) {
 			//color the bot with random colors
 			setColors(new Color(randomColorValue(), randomColorValue(), randomColorValue()),
 					new Color(randomColorValue(), randomColorValue(), randomColorValue()),
 					new Color(randomColorValue(), randomColorValue(), randomColorValue()));
 
 			//get position of bot
 			x = getX();
 			y = getY();
 			
 			if(turnedGun){
 				turnGunRight(getHeading() + 90 - getGunHeading());
 			}
 			if (positionState == BotPositionState.NOT_ON_TRACK) {
 				goToTrack();
 			} else if (positionState == BotPositionState.TOP) {
 				turnRight(90-getHeading());
 				ahead(760-x);
 				positionState = BotPositionState.RIGHT;
 			} else if (positionState == BotPositionState.RIGHT) {
 				turnRight(180-getHeading());
 				ahead(y-40);
 				positionState = BotPositionState.BOTTOM;
 			} else if (positionState == BotPositionState.BOTTOM) {
 				turnRight(90);
 				ahead(x-40);
 				positionState = BotPositionState.LEFT;
 			} else if (positionState == BotPositionState.LEFT) {
 				turnRight(90);
 				ahead(560-y);
 				positionState = BotPositionState.TOP;
 			}
 		}
 	}
 	
 	private void goToTrack() {
 		if (Math.abs(x-400) < Math.abs(y-300)) {
 			if (x > 400) {	//go to right
 				turnRight(90-getHeading());
 				ahead(760-x);
 				positionState = BotPositionState.RIGHT;
 			} else { //go to left
 				turnRight(270-getHeading());
 				ahead(x-40);
 				positionState = BotPositionState.LEFT;
 			}
 		} else {
 			if (y > 300) { //go to top
 				turnRight(-getHeading());
 				ahead(560-y);
 				positionState = BotPositionState.TOP;
 			} else { //go to bottom
 				turnRight(180-getHeading());
 				ahead(y-40);
 				positionState = BotPositionState.BOTTOM;
 			}
 		}
 	}
 	
 	public void onScannedRobot(ScannedRobotEvent event) {
 		// SKYT når man ser en fiende
 		stop();
		fire(2);
 		resume();
 	}
 	
 	public void onHitByBullet(HitByBulletEvent event) {
 		
 		if(getVelocity() > 0){
 			stop();
 		}else{
 			ahead(30);
 		}
		turnGunRight(event.getBearing());	//turn against the shooting opponent
 		//select turnRight or turnLeft based on what's optimal
 		turnedGun = true;
		fire(1);
		
 	}
 	
 	public void onWin(WinEvent event) {
 		//VictoryDance comes here
 		
 	}
 	
 	public static int randomColorValue() {
 		return 100 + (int)Math.round(Math.random()*155);
 	}
 }
