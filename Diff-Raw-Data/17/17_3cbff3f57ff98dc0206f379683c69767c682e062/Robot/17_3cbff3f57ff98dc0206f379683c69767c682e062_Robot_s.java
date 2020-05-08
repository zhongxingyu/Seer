 package program.main;
 
 import java.util.ArrayList;
 import lejos.nxt.Motor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.addon.ColorHTSensor;
 import lejos.nxt.comm.RConsole;
 import lejos.robotics.Color;
 import lejos.robotics.navigation.DifferentialPilot;
 import lejos.util.Timer;
 import program.control.PIDControllerX;
 import program.mapping.Direction;
 import program.mapping.Park;
 import program.mapping.Step;
 
 public class Robot {
 	
 	private final float MAXSPEED = 2 * 360; // 2 RPS
 	private final double WHEELDIAMETER = 4;
 	private final double TRACKWIDTH = 4;
 	private final int TURN_TIME_MS = 400;
 	
 	private final NXTRegulatedMotor leftMotor, rightMotor;
 	public ColorHTSensor leftColorSensor, midColorSensor, rightColorSensor;
 	public DifferentialPilot pilot;
 	
 	public PIDControllerX pid;
 		
 	public Robot() {
 		leftMotor = new NXTRegulatedMotor(RoboMap.LEFT_MOTOR_PORT);
 		rightMotor = new NXTRegulatedMotor(RoboMap.RIGHT_MOTOR_PORT);
 		pilot = new DifferentialPilot(WHEELDIAMETER, TRACKWIDTH, leftMotor, rightMotor);
 		leftColorSensor = new ColorHTSensor(RoboMap.LEFT_COLOR_SENSOR_PORT);
 		midColorSensor = new ColorHTSensor(RoboMap.MID_COLOR_SENSOR_PORT);
 		rightColorSensor = new ColorHTSensor(RoboMap.RIGHT_COLOR_SENSOR_PORT);
 		pid = new PIDControllerX((1.0/250.0), 0.0, 0.0, RoboMap.PID_WHITE_SETPOINT);
 		pid.setOutputCaps(-.5, .5);
 	}
 	
 	public Color getLeftColor()  { return leftColorSensor.getColor(); }
 	public Color getMidColor()   { return midColorSensor.getColor(); }
 	public Color getRightColor() { return rightColorSensor.getColor(); }
 	
 	public boolean checkForStop() {
 		if (checkColor(midColorSensor).equals("RED")) return true;
 		return false;
 	}
 	
 	public double getLTacoCount() {
 		return leftMotor.getTachoCount();
 	}
 	public double getRTacoCount() {
 		return rightMotor.getTachoCount();
 	}
 	public double getAveTacoCount() {
 		return (getLTacoCount() + getRTacoCount()) / 2;
 	}	
 
 	public String checkColor(ColorHTSensor sensor) {
 		int black = sensor.getRGBComponent(ColorHTSensor.BLACK);
 		int white = sensor.getRGBComponent(ColorHTSensor.WHITE);
 		int red   = sensor.getRGBComponent(ColorHTSensor.RED);
 		int green = sensor.getRGBComponent(ColorHTSensor.GREEN);
 		int blue  = sensor.getRGBComponent(ColorHTSensor.BLUE);
 		int yellow = sensor.getRGBComponent(ColorHTSensor.YELLOW);
 		if (black < 30 && white > 230 && yellow > 40 && yellow < 80 && blue > 5 && blue < 40 && green < 80 && green > 25
 				&& red > 220)
 			return "BLACK";
 		if (black > 80 && yellow > 10 && yellow < 120 && white > 180 && green > 180 && red > 180
 				&& blue > 80 && blue < 220)
 			return "WHITE";
 		if (black > 80 && black < 160 && yellow > 30 && yellow < 100 && white > 150 && blue > 10 && blue < 80
 				&& green > 160 && red > 200)
 			return "YELLOW";
 		if (black > 10 && black < 60 && white > 230 && yellow > 10 && yellow < 70 && blue < 50 && green > 30 && green < 85
 				&& red > 150)
 			return "GREEN";
 		if (black > 25 && black < 50 && white > 120 && white < 160 && yellow < 25 && blue > 40 && blue < 80 && green < 130 && green > 100
 				&& red < 110)
 			return "BLUE";
 		if (black < 40 && white < 50 && yellow < 60 && blue < 20 && green > 50 && green < 100 && red > 225)
 			return "RED";
 		
 		return "ERROR";
 	}
 
	public double runPID (boolean leftPID) {
 		ColorHTSensor colorSensor = leftPID ? this.leftColorSensor : this.rightColorSensor;
 		ColorHTSensor otherSensor = leftPID ? this.rightColorSensor : this.leftColorSensor;
 		String colorID = checkColor(colorSensor);
 		String otherID = checkColor(otherSensor);
 		
 		if (otherID.equals("WHITE") || otherID.equals("YELLOW") || otherID.equals("BLUE")) {
 			//return .3;
 		}
 		
 		int colorValue = colorSensor.getRGBComponent(ColorHTSensor.BLACK);
		if (colorID.equals("BLACK")) return -0.04;
 		if (colorID.equals("GREEN")) return -.4;
 		if (colorID.equals("YELLOW")) { 
 			this.pid.setSetpoint(RoboMap.PID_YELLOW_SETPOINT); 
			this.pid.setPIDConstants(0.0055, 0, 0.00003); 
 		}
 		if (colorID.equals("WHITE")) { 
 			this.pid.setSetpoint(RoboMap.PID_WHITE_SETPOINT);
			this.pid.setPIDConstants(0.003, 0, 0.00003);
 		}
 		return this.pid.getOutput(colorValue);
 	}
 	
 	public void followLeftLine(boolean isCircle) {
 		double speed = .5;
		double value = runPID(true);
 		RConsole.println(String.valueOf(value));
 		if(isCircle) {
 			//value  /= 1.1;
 			value /= 1.2;
 			tankDrive(.4 - value, .6 + value);
 			return;
 		}
 		//System.out.println(value);
 		//System.out.println((speed - (speed * value)) + ", " + (speed + (speed * value)));
 		tankDrive(speed - value, speed + value);
 	}
 	public void followRightLine(boolean isCircle) {
 		double speed = .5;
		double value = runPID(false);
 		value /= 1.5;
 		if (isCircle) {
 			value /= 1.25;
 			tankDrive(.6 + value, .4-value);
 			return;
 		}
 		//System.out.println(value);mine	
 		//System.out.println((speed + (speed * value)) + ", " + (speed - (speed * value)));
 		tankDrive(speed + value, speed - value);
 	}
 	public void turnLeft() {
 		stop();
 		waitOneSecond();
 		pilot.travel(25);
 		pilot.arc(0, 430);
 	}
 	public void turnRight() {
 		stop();
 		waitOneSecond();
 		pilot.travel(25);
 		pilot.arc(0, -430);
 	}
 	public void followSteps(ArrayList<Step> steps) {
 		Step currentStep, nextStep;
 		while (steps.size() > 1) {
 			currentStep = steps.get(0);
 			nextStep = steps.get(1);
 			boolean circle = currentStep.getRoad().isCircle();
 			if (currentStep.getDirection() == Direction.Straight && nextStep.getDirection() == Direction.Right) {
 				do followRightLine(circle); while (!checkForStop());
 				waitOneSecond();
 				turnRight();
 			}
 			else if (currentStep.getDirection() == Direction.Straight && nextStep.getDirection() == Direction.Left) {
 				do followLeftLine(circle); while (!checkForStop());
 				waitOneSecond();
 				turnLeft();
 			}			
 			else if (currentStep.getDirection() == Direction.Straight && nextStep.getDirection() == Direction.Straight) {
 				do followLeftLine(false); while(!checkForStop());
 				waitOneSecond();
 				pilot.travel(6);
 			}
 			else if (currentStep.getDirection() == Direction.Straight && nextStep instanceof Park) {
 				Park nextPark = (Park) nextStep;
 				if (nextPark.getDirection() == Direction.Left) {
 
 				}
 				else if (nextPark.getDirection() == Direction.Right) {
 
 				}
 			}
 			steps.remove(0);
 		}
 	}
 
 	public void tankDrive(double left, double right) {
 		//clamp values between [-1, 1]
 		left = MathUtils.clamp(left, -1, 1);
 		right = MathUtils.clamp(right, -1, 1);
 		
 		float leftDrive = (float) left;
 		float rightDrive = (float) right;
 		
 		leftMotor.setSpeed(MAXSPEED * leftDrive);
 		rightMotor.setSpeed(MAXSPEED * rightDrive);
 		leftMotor.forward();
 		rightMotor.forward();
 	}
 	public void stop() {
 		pilot.stop();
 	}
 	public void waitOneSecond() {
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException ex) { 
 			ex.printStackTrace(); 
 		}
 	}
 
 }
