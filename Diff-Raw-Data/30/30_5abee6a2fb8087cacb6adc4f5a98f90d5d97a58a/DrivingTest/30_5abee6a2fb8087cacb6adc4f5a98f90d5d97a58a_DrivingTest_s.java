 package test;
 
 import lejos.nxt.*;
 import lejos.nxt.addon.*;
 import lejos.nxt.comm.LCPBTResponder;
 
 public class DrivingTest extends Thread implements ButtonListener {
 
 	NXTRegulatedMotor left = Motor.B; // right
 	NXTRegulatedMotor right = Motor.C; // left
 	CompassHTSensor com = new CompassHTSensor(SensorPort.S1);
 	UltrasonicSensor son = new UltrasonicSensor(SensorPort.S2);
 	int wayDirection;
 	int speed = 300;
 	int rotationspeed = 100;
 	int drift = 2;
 	int ticks;
 	boolean run = true;
 
 	public DrivingTest() {
 		System.out.println("Initialize");
 		initialize();
 		Button.ESCAPE.addButtonListener(this);
 		calibrate();
 		System.out.println("Enter to Start");
 //		Button.ENTER.waitForPressAndRelease();
 		this.start();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		new DrivingTest();
 	}
 
 	public void initialize() {
 		left.setAcceleration(4000);
 		right.setAcceleration(4000);
 		// Escape Funktion
 		Button.ESCAPE.addButtonListener(this);
 		// Monitor Daemon
 		LCPBTResponder lcpThread = new LCPBTResponder();
 		lcpThread.setDaemon(true);
 		lcpThread.start();
 	}
 
 	public void calibrate() {
 		System.out.println("Calibrate");
 		right(50);
 		com.startCalibration();
 		sleep(14100); //Time for one rotaion
 		com.stopCalibration();
 		System.out.println("Calibrated");
		stop();
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
 
 	public void run() {
 		for (ticks = 0; run; ticks++) {
 			wayDirection = getDegrees();
 			if (son.getDistance() < 30) {
 				stepRight();
 			} else {
 				forward();
 			}
 		}
		stop();
 		System.out.println("Ticks: " + ticks);
 		System.out.println("Enter for Exit");
 //		Button.ENTER.waitForPressAndRelease();
 	}
 
	public void stop() {
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
		stop();
 	}
 
 	public void stepLeft() {
 		int direction = getDegrees();
 		left();
 		int targetDirection = (direction - 90) % 360;
 		System.out.println("90 Left\t" + targetDirection);
 		while (!checkDegrees(targetDirection)) {
 		}
		stop();
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
 }
