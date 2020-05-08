 package test;
 
 import lejos.nxt.*;
 import lejos.nxt.addon.*;
 import lejos.nxt.comm.LCPBTResponder;
 import lejos.robotics.RangeReading;
 import lejos.robotics.RangeReadings;
 import lejos.robotics.objectdetection.*;
 import lejos.util.Delay;
 
 public class ObjectDetection extends Thread implements ButtonListener,
 		FeatureListener {
 
 	boolean run = true;
 	NXTRegulatedMotor left = Motor.B; // right
 	NXTRegulatedMotor right = Motor.C; // left
 	CompassHTSensor com = new CompassHTSensor(SensorPort.S1);
 	UltrasonicSensor son = new UltrasonicSensor(SensorPort.S2);
 	int wayDirection;
 	int speed = 300;
 	int rotationspeed = 100;
 	int drift = 2;
 	int ticks;
 	private int count = 0;
 
 	public ObjectDetection() {
 		System.out.println("Initialize");
 		initialize();
 		Button.ESCAPE.addButtonListener(this);
 		// calibrate();
 		System.out.println("Enter to Start");
 		Button.ENTER.waitForPressAndRelease();
 		this.start();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new ObjectDetection();
 	}
 
 	public void run() {
 		while (true) {
 			count++;
 			int i = 0;
 			Sound.beep();
 			float[] reads = son.getRanges();
 			for (float read : reads) {
 				System.out.println(count + "  " + ++i + "  " + (int) read);
 			}
 			Delay.msDelay(1000);
 			LCD.clear();
 		}
 	}
 
 	public void initialize() {
 		Sound.setVolume(30);
 		left.setAcceleration(4000);
 		right.setAcceleration(4000);
 		// Escape Funktion
 		Button.ESCAPE.addButtonListener(this);
 		// Monitor Daemon
 		LCPBTResponder lcpThread = new LCPBTResponder();
 		lcpThread.setDaemon(true);
 		lcpThread.start();
 		// new
 		RangeFeatureDetector objectDetector = new RangeFeatureDetector(son, 50,
 				2000);
 		// objectDetector.addListener(this);
 	}
 
 	public void calibrate() {
 		System.out.println("Calibrate");
 		right(50);
 		com.startCalibration();
 		sleep(14100); // Time for one rotaion
 		com.stopCalibration();
 		System.out.println("Calibrated");
		stopMotor();
 	}
 
 	public void right(int rotationspeed) {
 		setSpeed(rotationspeed);
 		left.forward();
 		right.backward();
 	}
 
 	public void right() {
 		right(rotationspeed);
 	}
 
 	public void left() {
 		left(rotationspeed);
 	}
 
 	public void left(int rotationspeed) {
 		setSpeed(rotationspeed);
 		left.backward();
 		right.forward();
 	}
 
	public void stopMotor() {
 		left.stop();
 		right.stop();
 	}
 
 	public void setSpeed(int speed) {
 		left.setSpeed(speed);
 		right.setSpeed(speed);
 	}
 
 	public void forward() {
 		setSpeed(speed);
 		left.forward();
 		right.forward();
 	}
 
 	public void backward() {
 		setSpeed(speed / 2);
 		left.backward();
 		right.backward();
 	}
 
 	private void sleep(int millis) {
 		try {
 			Thread.sleep(millis);
 		} catch (InterruptedException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public void buttonPressed(Button b) {
 		if (b.getId() == Button.ID_ESCAPE) {
 			run = false;
 			System.out.println("Try to Exit");
 		}
 	}
 
 	@Override
 	public void buttonReleased(Button b) {
 		// TODO Auto-generated method stub
 
 	}
 
 	public void stepRight() {
 		int direction = getDegrees();
 		right();
 		int targetDirection = (direction + 90) % 360;
 		System.out.println("90 Right\t" + targetDirection);
 		while (!checkDegrees(targetDirection)) {
 		}
		stopMotor();
 	}
 
 	public void stepLeft() {
 		int direction = getDegrees();
 		left();
 		int targetDirection = (direction - 90) % 360;
 		System.out.println("90 Left\t" + targetDirection);
 		while (!checkDegrees(targetDirection)) {
 		}
		stopMotor();
 	}
 
 	public boolean checkDegrees(int targetDirection) {
 		int direction = getDegrees();
 		// System.out.println(direction + "\t" + targetDirection);
 		if (Math.abs(direction - targetDirection) % 360 < drift) {
 			return true;
 		} else
 			return false;
 	}
 
 	public int getDegrees() {
 		return (int) com.getDegrees();
 	}
 
 	@Override
 	public void featureDetected(Feature feature, FeatureDetector detector) {
 		LCD.clear();
 		RangeReadings reads = feature.getRangeReadings();
 		Sound.beep();
 
 		int i = 0;
 		++count;
 		for (RangeReading read : reads) {
 			int range = (int) read.getRange();
 			int angel = (int) read.getAngle();
 			System.out
 					.println(count + "  " + ++i + "  " + angel + "  " + range);
 		}
 	}
 }
