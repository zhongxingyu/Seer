 package icaruspackage;
 
 import lejos.nxt.ColorSensor.Color;
 import lejos.nxt.NXT;
 import lejos.util.Timer;
 
 public class SimpleBot extends Eurobot {
 	public static void main(String[] args) {		
 		Eurobot bot = new SimpleBot();
 		bot.initialize();
 		bot.go();
 		NXT.shutDown();
 	}
 	
 	@Override
 	public void initialize() {
 		pilot.setTravelSpeed(speed);
		pilot.setRotateSpeed(speed*Math.PI*WHEEL_BASE/180.0f); // degrees/sec#
 		
 		registerStopButtonInterrupt();
 		Timer matchTimer = initMatchTimer();
 		startSonicAvoidanceThread();
 		footUp(); // Just in case!
 		
 		// wait for start signal:
 		while(light.getColorID() == Color.BLACK){competition = true;}
 		matchTimer.start();
 		
 		// wait 300ms to make sure the starting paper is clear of the colour sensor.
 		lejos.util.Delay.msDelay(300);
 	}
 	
 	@Override
 	public void go() {
 		// get the start colour
 		int startColor;
 		do {
 			 startColor = light.getColorID();
 		}while(startColor != Color.RED && startColor != Color.BLUE);
 		int dir = (startColor == Color.BLUE)?1:-1;
 		
 		// Move out of the starting box
 		pilot.travel(100, true);
 		while (pilot.isMoving()) {
 			if (light.getColorID() != startColor) pilot.stop();
 		}
 	
 		// Turn onto the first line
 		pilot.arc(dir*20.0f,dir*90.0f);
 		
 		// Drive forwards until you find a pawn
 		pilot.travel(200, true);
 		while (pilot.isMoving()) {
 			if (pawn.isPressed()) pilot.stop(); //Found a pawn!
 		}
 		// Remember how far you drove
 		float travel2 = pilot.getMovement().getDistanceTraveled();
 
 		// Turn round and go home
 		pilot.rotate(180); 
 		pilot.travel(travel2+22.0f);
 		pilot.rotate(-90*dir);
 		pilot.travel(60, true);
 
 		// Go past black
 		int n = 0;
 		do {
 			if(light.getColorID() == Color.BLACK) ++n;
 			else n = 0;
 			lejos.util.Delay.msDelay(100);
 		} while(n < 2 && pilot.isMoving());
 		
 		pilot.stop();
 			
 		// Go a little bit further
 		if(competition) {
 			pilot.travel(12);
 		} else {
 			pilot.travel(5);
 		}
 		
 		footDown();
 		
 		if(!competition) {
 			lejos.util.Delay.msDelay(4000);
 			footUp();
 			pilot.setTravelSpeed(speed);
 			pilot.rotate(180);
 		} else {
 			NXT.shutDown();
 		}
 	}
 
 }
