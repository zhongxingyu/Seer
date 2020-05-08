 package icaruspackage;
 
 import lejos.nxt.LCD;
 import lejos.nxt.NXT;
 import lejos.nxt.ColorSensor.Color;
 import lejos.util.Timer;
 //import lejos.util.Stopwatch;
 
 public class TwoPawnBot extends Eurobot {
 	public static void main(String[] args) {		
 		Eurobot bot = new TwoPawnBot();
 		bot.initialize();
 		bot.go();
 		NXT.shutDown();
 	}
 
 	@Override
 	public void initialize() {
 		LCD.drawString("2-Pawn Bot", 0, 0);
 		pilot.setTravelSpeed(FAST);
 		// NOTE: for some reason rotating is SSLLOOWW, so now set to 8*
		pilot.setRotateSpeed(8*rotateSpeed*Math.PI*WHEEL_BASE/180.0f); // degrees/sec#
 		pilot.setAcceleration(MIN_ACCELERATION);//(default/max is 6000)
 
 		//TODO: FIX THIS  
 		stopwatch.reset();
 		registerStopButtonInterrupt();
 		Timer matchTimer = initMatchTimer();
 		//startSonicAvoidanceThread();
 		footUp(); // Just in case!
 
 		// wait for start signal:
 		while(light.getColorID() == Color.BLACK){competition = true;}
 		matchTimer.start();
 
 		// wait 300ms to make sure the starting paper is clear of the colour sensor.
 		lejos.util.Delay.msDelay(300);
 
 	}
 
 	@SuppressWarnings("deprecation")
 	@Override
 	public void go() {
 		// get the start colour
 		int startColor;
 		do {
 			startColor = light.getColorID();
 		}while(startColor != Color.RED && startColor != Color.BLUE);
 		if(startColor == Color.RED) {LCD.drawString("RED", 0, 1);}
 		else {LCD.drawString("BLUE", 0, 1);}
 
 		int dir = (startColor == Color.BLUE)?1:-1;
 
 		int distanceDownBoard = 0;
 
 		// Move out of the starting box
 		LCD.drawString("Move from box          ", 0, 4);
 		pilot.travel(100,true);
 		while (light.getColorID() == startColor)
 		{// look for colour change
 		}
 		pilot.setAcceleration(MAX_ACCELERATION);// stop quickly
 		pilot.travel(5,false);// move extra amount past colour change
 		pilot.setAcceleration(MIN_ACCELERATION);// reset acceleration
 
 		// Turn onto the first line
 		LCD.drawString("Turn to 1st line          ", 0, 4);
 		pilot.arc(dir*20.0f,dir*90.0f);
 
 		// Drive forwards until you find a pawn (max 3 squares)
 		LCD.drawString("Seek pawn1          ", 0, 4);
 		pilot.reset();
 		pilot.travel(106, true);
 		waitForPawn();
 
 		distanceDownBoard += pilot.getMovement().getDistanceTraveled();
 		int travelDistance = 35;
 		if (distanceDownBoard < 15){// a pawn on 1st junction found
 			lejos.nxt.Sound.beep();
 			travelDistance = 15;
 			distanceDownBoard = distanceDownBoard + 20;
 		}	
 		// Move back 1 square
 		pilot.rotate(180); 
 		pilot.travel(travelDistance);
 		pilot.travel(-35);
 		pilot.rotate(-180);
 		LCD.drawString("Seek pawn2          ", 0, 4);
 
 		// move to | + + + + * | collecting 2nd pawn on the way
 		pilot.reset();
 		pilot.travel(140-distanceDownBoard, false);// was (105-distanceDownBoard, true);
 		distanceDownBoard += pilot.getMovement().getDistanceTraveled();
 
 		// Place 2nd pawn in protected area:
 		// turn 90 and move until colour changes to BLUE
 		LCD.drawString("Place pawn2          ", 0, 4);
 
 		pilot.rotate(dir*90);
 		pilot.setTravelSpeed(SLOW);// do this slowly...
 		pilot.travel(20,true);
 		while (light.getColorID() == Color.RED)
 		{// look for colour change
 		}
 		pilot.setAcceleration(MAX_ACCELERATION);// stop quickly
 		pilot.stop();
 		//pilot.travel(5,false);// move extra amount past colour change if needed
 		pilot.setAcceleration(MIN_ACCELERATION);// reset acceleration
 		pilot.setTravelSpeed(FAST);// ok to go quickly again...
 		// turn and push the pawn into the protected square
 		pilot.rotate(dir*-90);
 		pilot.travel(15);
 
 		// reverse back to horiz4
 		pilot.travel(-48);// was (distanceDownBoard-150);
 		pilot.rotate(dir*90);
 
 		// FIND VERT1 **************************
 		LCD.drawString("Find vert1          ", 0, 4);
 		if(light.getColorID() == Color.RED){
 			// reverse back to vert1
 			pilot.travel(-20, true);
 			while (light.getColorID() == Color.RED)
 			{// look for colour change
 			}
 			pilot.stop();// DO WE NEED THIS? POSSIBLY THE NEXT COMMAND CANCELS THE PREVIOUS MOVEMENT...
 			pilot.travel(-5);// move a little bit to line up with vert1
 		} else {
 			// find vert1
 			pilot.travel(20, true);
 			while (light.getColorID() != Color.RED)
 			{// look for colour change
 			}
 			pilot.stop();// DO WE NEED THIS? POSSIBLY THE NEXT COMMAND CANCELS THE PREVIOUS MOVEMENT...
 			pilot.travel(-10);// move a little bit to line up with vert1
 		}
 
 		// RE-ORIENT *****************************
 		LCD.drawString("Re-orient          ", 0, 4);
 		pilot.setAcceleration(MAX_ACCELERATION);// stop quickly
 		pilot.rotate(dir*335,true);
 		while (light.getColorID() == Color.BLUE)
 		{// look for colour change
 		}
 		float angle1=Math.abs(pilot.getAngleIncrement());// note angle1
 		while (light.getColorID() == Color.RED)
 		{// look for colour change
 		}
 		while (light.getColorID() == Color.BLUE)
 		{// look for colour change
 		}
 		float angle2=Math.abs(pilot.getAngleIncrement());// note angle1
 		LCD.drawString(angle1+" "+angle2, 0, 4);
 		pilot.stop();
 
 		float SENSOR_ANGLE = 36;// for RED start...
 		if(startColor==Color.BLUE) SENSOR_ANGLE = -65;
 		pilot.rotate(-dir*(angle2-angle1)/2 + SENSOR_ANGLE);
 		// now we should be facing back up vert1
 		pilot.setAcceleration(MIN_ACCELERATION);// reset acceleration
 		// ******************************************
 
 		// we are now  here: | + + + * + |
 
 		LCD.drawString("Go home          ", 0, 4);
 		pilot.travel(125); 
 		// we are now  here: |*+ + + + + |
 
 		/*
 		pilot.reset();
 
 		// Find next pawn
 		pilot.reset();
 		pilot.travel(105, true);
 		waitForPawn();
 
 		distanceDownBoard -= pilot.getMovement().getDistanceTraveled();
 
 		pilot.travel(distanceDownBoard + 22.0f);	
 		 */
 
 		pilot.rotate(-90*dir);
 		pilot.travel(60, true);
 
 		// Go past black
 		int n = 0;
 		do {
 			if(light.getColorID() == Color.BLACK) ++n;
 			else n = 0;
 			lejos.util.Delay.msDelay(100);
 		} while(n < 2 && pilot.isMoving());
 
 		//pilot.setAcceleration(MAX_ACCELERATION);// stop quickly
 		pilot.stop();
 
 		/*// took this next bit out to avoid jerky stop...
 		// BUT... IF THE NEXT MOVEMENT CANCELS THE PREVIOUS ONE, remove pilot.stop()
 		// Go a little bit further
 		if(competition) {
 			pilot.travel(12);
 		} else {
 			pilot.travel(5);
 		}
 		pilot.stop();
 		 */
 		footDown();
 
 		if(!competition) {
 			lejos.util.Delay.msDelay(4000);
 			footUp();
 			pilot.setTravelSpeed(FAST);
 			pilot.rotate(180);
 		} else {
 			lejos.util.Delay.msDelay(9000);//LONG WAIT, TO AVOID SAGGING BACK DOWN
 			NXT.shutDown();
 		}
 
 		// move forward to 1st position
 		// arc +90 R150 (on one wheel)
 		// if pawn detected {
 		// 		rotate +180
 		//		move forward 150mm (with pawn)
 		//		rotate -90
 		//		move forward 350mm (with pawn)
 		//		move backward 350mm
 		//		rotate -90
 		// } else {
 		//		move forward X to first pawn (max 3 squares)
 		//		rotate +180
 		//		move forward 350mm (with pawn)
 		//		move backward 350mm
 		//		rotate +180
 		// }
 		// move forward to 2nd pawn (max ... squares)
 		// rotate +90
 		// move forward 150mm (with pawn)
 		// rotate -90
 		// move forward to safe square (with pawn) (2nd pawn is now in place)
 		// move backward 450mm
 		// rotate -90
 		// move forward 500mm
 		// if no pawn detected {
 		//		rotate -90
 		//		move forward X until we hit a pawn (max 3 squares)
 		//		rotate +180
 		//		move forward X+50 (with pawn)
 		//		rotate -90
 		// } else {
 		//		rotate 90
 		//		move forward 50mm
 		//		rotate -90
 		// }
 		// arc +135 R300 (with pawn) (3rd pawn is now in place)
 		// arc -135 R300 in reverse
 		// move 350mm backward
 		// rotate -90
 		// move forward 1200mm
 		// rotate -90
 		// move forward 600mm (1st pawn is now in place
 		// lift up!
 	}
 
 
 }
