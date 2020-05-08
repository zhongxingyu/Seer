 package common;
 
 
 import lejos.nxt.LightSensor;
 import lejos.nxt.Motor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.TouchSensor;
 import lejos.nxt.UltrasonicSensor;
 import lejos.robotics.navigation.DifferentialPilot;
 
 public class Robot {
 	public TouchSensor leftTouch, rightTouch;
 	public UltrasonicSensor sonar;
 	public LightSensor light;
 	
 	public NXTRegulatedMotor leftMotor, rightMotor;
 	public NXTRegulatedMotor joker;
 	
 	public DifferentialPilot pilot;
 	
 	private int leftMaxJokerAngle;
 	private int rightMaxJokerAngle;
 	
 	public Robot() {
 		leftMotor = Motor.C;
 		rightMotor = Motor.A;
 		joker = Motor.B;
 		
 		leftTouch = new TouchSensor(SensorPort.S4);
 		rightTouch = new TouchSensor(SensorPort.S1);
 		sonar = new UltrasonicSensor(SensorPort.S3);
 		light = new LightSensor(SensorPort.S2);
 		
 //		pilot = new DifferentialPilot(3.3f, 21.0f, leftMotor,
 //				rightMotor);
		pilot = new DifferentialPilot(3.65f , 11.3f, leftMotor, rightMotor);
 		
 		leftMaxJokerAngle = -90;
 		rightMaxJokerAngle = 90;
 		
 		calibrateJoker();
 	}
 	
 	public int getLineValue() {
 		return 46;
 	}
 	
 	public int getWoodValue() {
 		return 37;
 	}
 	
 	public int getFallValue() {
 		return 28;
 	}
 	
 	public int getBlackValue() {
 		return 26;
 	}
 	
 	public boolean isLineBeneath() {
 		return light.readValue() > (getLineValue() + getBlackValue())/2;
 	}
 	
 	public boolean isFallBeneath() {
 		return light.readValue() < (getWoodValue() + getFallValue())/2;
 	}
 	
 	public void alignLightLeft() {
 		joker.rotateTo(leftMaxJokerAngle);
 	}
 	
 	public void alignLightMiddle() {
 		joker.rotateTo((leftMaxJokerAngle + rightMaxJokerAngle) / 2);
 	}
 	
 	public void alignLightRight() {
 		joker.rotateTo(rightMaxJokerAngle);
 	}
 	
 	public int getLeftJoker() {
 		return leftMaxJokerAngle;
 	}
 	
 	public int getRightJoker() {
 		return rightMaxJokerAngle;
 	}
 
 	public int getMiddleJoker() {
 		return (leftMaxJokerAngle + rightMaxJokerAngle) / 2;
 	}
 
 	public void calibrateJoker() {
 		do {
 			System.out.println("calibrate joker");
 			joker.setStallThreshold(8, 3);
 			
 			//Find left boundary
 			joker.backward();
 			while (!joker.isStalled());
 			joker.stop();
 			joker.rotate(16);
 			joker.waitComplete();
 			leftMaxJokerAngle = joker.getPosition();
 			System.out.println("left max " + Integer.toString(leftMaxJokerAngle));
 			
 			//Find right boundary
 			joker.forward();
 			while (!joker.isStalled());
 			joker.stop(false);
 			joker.rotate(-16);
 			joker.waitComplete();
 			rightMaxJokerAngle = joker.getPosition();
 			System.out.println("right max " + Integer.toString(rightMaxJokerAngle));
 		} while (java.lang.Math.abs(180 - (rightMaxJokerAngle - leftMaxJokerAngle)) > 4);
 
 		joker.setStallThreshold(50, 20);
 
 		joker.rotateTo((leftMaxJokerAngle + rightMaxJokerAngle) / 2);
 	}
 }
