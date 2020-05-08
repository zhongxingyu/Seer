 package p0_Race;
 import lejos.nxt.LCD;
 import lejos.nxt.TouchSensor;
 import lejos.nxt.UltrasonicSensor;
 import lejos.robotics.subsumption.Behavior;
 import general.Movement;
 import general.SensorCache;
import general.Settings;
 
 
 public class P0_TurnRight implements Behavior {
 	
 	UltrasonicSensor sonic;
 	TouchSensor touch;
 	int speed;
 	int rotationSpeed;
 	int shouldBe; // i.e. 10
 	int minimumDifference; // i.e. 20 (--> reacts at value 10 + 20 = 30)
 	boolean suppressed;
 	Movement movement = Movement.getInstance();
 	
 
 	public P0_TurnRight(UltrasonicSensor sonic, int speed, int rotationSpeed, int shouldBe, int minimumDifference) {
 		this.sonic = sonic;
 		this.speed = speed;
 		this.rotationSpeed = rotationSpeed;
 		this.shouldBe = shouldBe;
 		this.minimumDifference = minimumDifference;
 	}
 	
 	public boolean takeControl() {
 		return (sonic.getDistance() > (shouldBe + minimumDifference) && !SensorCache.getInstance().bumperPressed);
 	}
 	
 	public void action() {
 		suppressed = false;
 		//movement.setSpeed(speed);
 		movement.setTravelSpeed(100);
 		movement.setRotationSpeed(rotationSpeed);
 
 		LCD.drawString("steering" , 1, 1);
 		movement.arcForward(-60);
 		
 				
 		
 	}
 	
 	public void suppress() {
 		suppressed = true;
 	}
 	
 	
 	
 
 }
