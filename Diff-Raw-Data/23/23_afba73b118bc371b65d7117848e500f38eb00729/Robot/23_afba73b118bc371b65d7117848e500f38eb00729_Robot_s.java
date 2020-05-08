 package program.main;
 
 import java.util.ArrayList;
 
 import lejos.nxt.ColorSensor.Color;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.addon.ColorHTSensor;
 import lejos.nxt.comm.RConsole;
 import lejos.robotics.navigation.DifferentialPilot;
 import program.control.PIDControllerX;
 import program.mapping.Direction;
 import program.mapping.Park;
 import program.mapping.Step;
 
 public class Robot {
 	
 	private final NXTRegulatedMotor leftMotor, rightMotor;
 	public ColorHTSensor leftColorSensor, midColorSensor, rightColorSensor;
 	public DifferentialPilot pilot;
 	private float speed;
 	private int component = ColorHTSensor.BLACK;
 	int sign = 1;
 	
 	public PIDControllerX pid;
 		
 	public Robot() {
 		leftMotor = new NXTRegulatedMotor(RoboMap.LEFT_MOTOR_PORT);
 		rightMotor = new NXTRegulatedMotor(RoboMap.RIGHT_MOTOR_PORT);
 		pilot = new DifferentialPilot(RoboMap.WHEELDIAMETER, RoboMap.TRACKWIDTH, leftMotor, rightMotor);
 		setSpeed(RoboMap.SLOWSPEED);
 		leftColorSensor = new ColorHTSensor(RoboMap.LEFT_COLOR_SENSOR_PORT);
 		midColorSensor = new ColorHTSensor(RoboMap.MID_COLOR_SENSOR_PORT);
 		rightColorSensor = new ColorHTSensor(RoboMap.RIGHT_COLOR_SENSOR_PORT);
 		pid = new PIDControllerX((1.0/250.0), 0.0, 0.0, RoboMap.PID_WHITE_SETPOINT);
 		pid.setOutputCaps(-.5, .5);
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
 
 	/* Color Detection Algorithm *********************************************/
 	public String checkColor(ColorHTSensor sensor) {
 		int black = sensor.getRGBComponent(ColorHTSensor.BLACK);
 		int white = sensor.getRGBComponent(ColorHTSensor.WHITE);
 		int red   = sensor.getRGBComponent(ColorHTSensor.RED);
 		int green = sensor.getRGBComponent(ColorHTSensor.GREEN);
 		int blue  = sensor.getRGBComponent(ColorHTSensor.BLUE);
 		int yellow = sensor.getRGBComponent(ColorHTSensor.YELLOW);
 		if (MathUtils.isWithin(black, RoboMap.BLACK_MIN[0], RoboMap.BLACK_MAX[0]) 
 				&& MathUtils.isWithin(white, RoboMap.BLACK_MIN[1], RoboMap.BLACK_MAX[1])
 				&& MathUtils.isWithin(red, RoboMap.BLACK_MIN[2], RoboMap.BLACK_MAX[2])
 				&& MathUtils.isWithin(green, RoboMap.BLACK_MIN[3], RoboMap.BLACK_MAX[3])
 				&& MathUtils.isWithin(blue, RoboMap.BLACK_MIN[4], RoboMap.BLACK_MAX[4])
 				&& MathUtils.isWithin(yellow, RoboMap.BLACK_MIN[5], RoboMap.BLACK_MAX[5]))
 			return "BLACK";
 		if (MathUtils.isWithin(black, RoboMap.WHITE_MIN[0], RoboMap.WHITE_MAX[0]) 
 				&& MathUtils.isWithin(white, RoboMap.WHITE_MIN[1], RoboMap.WHITE_MAX[1])
 				&& MathUtils.isWithin(red, RoboMap.WHITE_MIN[2], RoboMap.WHITE_MAX[2])
 				&& MathUtils.isWithin(green, RoboMap.WHITE_MIN[3], RoboMap.WHITE_MAX[3])
 				&& MathUtils.isWithin(blue, RoboMap.WHITE_MIN[4], RoboMap.WHITE_MAX[4])
 				&& MathUtils.isWithin(yellow, RoboMap.WHITE_MIN[5], RoboMap.WHITE_MAX[5]))
 			return "WHITE";
 		if (MathUtils.isWithin(black, RoboMap.YELLOW_MIN[0], RoboMap.YELLOW_MAX[0]) 
 				&& MathUtils.isWithin(white, RoboMap.YELLOW_MIN[1], RoboMap.YELLOW_MAX[1])
 				&& MathUtils.isWithin(red, RoboMap.YELLOW_MIN[2], RoboMap.YELLOW_MAX[2])
 				&& MathUtils.isWithin(green, RoboMap.YELLOW_MIN[3], RoboMap.YELLOW_MAX[3])
 				&& MathUtils.isWithin(blue, RoboMap.YELLOW_MIN[4], RoboMap.YELLOW_MAX[4])
 				&& MathUtils.isWithin(yellow, RoboMap.YELLOW_MIN[5], RoboMap.YELLOW_MAX[5]))
 			return "YELLOW";		
 		if (MathUtils.isWithin(black, RoboMap.GREEN_MIN[0], RoboMap.GREEN_MAX[0]) 
 				&& MathUtils.isWithin(white, RoboMap.GREEN_MIN[1], RoboMap.GREEN_MAX[1])
 				&& MathUtils.isWithin(red, RoboMap.GREEN_MIN[2], RoboMap.GREEN_MAX[2])
 				&& MathUtils.isWithin(green, RoboMap.GREEN_MIN[3], RoboMap.GREEN_MAX[3])
 				&& MathUtils.isWithin(blue, RoboMap.GREEN_MIN[4], RoboMap.GREEN_MAX[4])
 				&& MathUtils.isWithin(yellow, RoboMap.GREEN_MIN[5], RoboMap.GREEN_MAX[5]))
 			return "GREEN";
 		if (MathUtils.isWithin(black, RoboMap.BLUE_MIN[0], RoboMap.BLUE_MAX[0]) 
 				&& MathUtils.isWithin(white, RoboMap.BLUE_MIN[1], RoboMap.BLUE_MAX[1])
 				&& MathUtils.isWithin(red, RoboMap.BLUE_MIN[2], RoboMap.BLUE_MAX[2])
 				&& MathUtils.isWithin(green, RoboMap.BLUE_MIN[3], RoboMap.BLUE_MAX[3])
 				&& MathUtils.isWithin(blue, RoboMap.BLUE_MIN[4], RoboMap.BLUE_MAX[4])
 				&& MathUtils.isWithin(yellow, RoboMap.BLUE_MIN[5], RoboMap.BLUE_MAX[5]))
 			return "BLUE";
 		if (MathUtils.isWithin(black, RoboMap.RED_MIN[0], RoboMap.RED_MAX[0]) 
 				&& MathUtils.isWithin(white, RoboMap.RED_MIN[1], RoboMap.RED_MAX[1])
 				&& MathUtils.isWithin(red, RoboMap.RED_MIN[2], RoboMap.RED_MAX[2])
 				&& MathUtils.isWithin(green, RoboMap.RED_MIN[3], RoboMap.RED_MAX[3])
 				&& MathUtils.isWithin(blue, RoboMap.RED_MIN[4], RoboMap.RED_MAX[4])
 				&& MathUtils.isWithin(yellow, RoboMap.RED_MIN[5], RoboMap.RED_MAX[5]))
 			return "RED";
 		
 		
 		
 		if (black < 40 && white < 50 && yellow < 60 && blue < 20 && green > 50 && green < 100 && red > 225)
 			return "RED";
 		return "ERROR";
 	}
 	public boolean checkForStop() {
 		if (checkColor(midColorSensor).equals("RED")) return true;
 		return false;
 	}
 
 	/* Autonomous Map Following with State Map and PID control ***************/
 	public double runPID (boolean leftPID, boolean isCircle) {
 		
 		ColorHTSensor colorSensor = leftPID ? this.leftColorSensor : this.rightColorSensor;
 		
 		String colorID = checkColor(colorSensor);
 		RConsole.println("Color: " + colorID);
 		
 		// Check the color of the other sensor and adjust if it finds a following color
 		String otherID = checkColor(leftPID ? this.rightColorSensor : this.leftColorSensor);
 		if (otherID.equals("WHITE") || otherID.equals("YELLOW") || otherID.equals("BLUE")) {
 			//return .3;
 		}
 		
 		if (colorID.equals("BLACK") && !isCircle && component == ColorHTSensor.BLACK) 
 			return 0.05;
         else if (colorID.equals("BLACK") && isCircle) return 0.15;
 		if (colorID.equals("GREEN")) return -.6;
 		if (colorID.equals("YELLOW")) {
 			this.pid.reset();
 			sign = 1;
 			component = ColorHTSensor.BLACK;
 			this.pid.setSetpoint(RoboMap.PID_YELLOW_SETPOINT); 
 			this.pid.setPIDConstants(0.007, 0, 0.00003);
 		} else if (colorID.equals("WHITE")) {
 			this.pid.reset();
 			sign = 1;
 			component = ColorHTSensor.BLACK;
 			this.pid.setSetpoint(RoboMap.PID_WHITE_SETPOINT);
 			this.pid.setPIDConstants(0.0025, 0, 0.00003);
 		} else if(colorID.equals("BLUE")) {
 			sign = -1;
 			component = ColorHTSensor.RED;
 			this.pid.setSetpoint(RoboMap.PID_BLUE_SETPOINT);
 			this.pid.setPIDConstants(0.004, 0.000, 0.0000);			
 		}
 		else {
 			return 0;
 		}
 		
 		if(isCircle) this.pid.setPIDConstants(0.005, 0, 0.00003);
 		
 		int colorValue = colorSensor.getRGBComponent(component);
 		return sign * this.pid.getOutput(colorValue);
 	}
 	public void followLeftLine(boolean isCircle) {
 		double speed = .5;
 		double value = runPID(true, isCircle);
 		
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
 		double value = runPID(false, isCircle);
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
 	public void followSteps(ArrayList<Step> steps) {
 		RConsole.println("Following Steps...");
 		int i = 0;
 		RConsole.println("SPEED: " + String.valueOf(speed));
 		RConsole.println("Going strait...");
 		Direction dir = steps.get(i+1).getDirection();
 		if(dir == Direction.Right)
 			while(!checkForStop()) followRightLine(false);
 		else
 			while(!checkForStop()) followLeftLine(false);
 		
 		stop();
 		i++;
 		Step currentStep, nextStep, lastStep;
 		while (steps.size() > i) {
 			RConsole.println("Color:" + checkColor(leftColorSensor));
 			lastStep = steps.get(i - 1);
 			currentStep = steps.get(i);
 			nextStep = steps.get(i + 1);
 			
 			boolean circle = currentStep.getRoad().isCircle();
 			boolean lastWasCircle = lastStep.getRoad().isCircle();
 			
 			if(currentStep.getRoad().isSlow()) setSpeed(RoboMap.SLOWSPEED);
 			else setSpeed(RoboMap.MAXSPEED);
 			
 			
 			if(currentStep.getDirection() == Direction.Right) {
 				RConsole.println("Turning Right...");
 				
 				double angle = RoboMap.NORMAL_TURN_ANGLE;
 				
 				if(circle) angle = RoboMap.TURN_ON_CIRCLE_ANGLE;// we have to turn onto a circle
 				else if(lastWasCircle) angle = RoboMap.TURN_OFF_CIRCLE_ANGLE;// we just came from a circle
 				RConsole.println("Angle: " + String.valueOf(angle));
 				turnRight((int)angle);				
 			} else if(currentStep.getDirection() == Direction.Left) {
 				RConsole.println("Turning Left...");
 								
 				double angle = RoboMap.NORMAL_TURN_ANGLE;
 				
 				if(circle) angle = RoboMap.TURN_ON_CIRCLE_ANGLE;// we have to turn onto a circle
 				else if(lastWasCircle) angle = RoboMap.TURN_OFF_CIRCLE_ANGLE;// we just came from a circle
 				RConsole.println("Angle: " + String.valueOf(angle));
 				turnLeft((int)angle);				
 			} else if(currentStep.getDirection() == Direction.Straight) {
 				RConsole.println("Going Strait...");
 				
 				waitOneSecond();
 				if(circle) {					
 					pilot.travel(RoboMap.KEEP_ON_CIRCLE_DISTANCE);
 				} else {
 					pilot.travel(RoboMap.TURN_TRAVEL_DISTANCE);
 				}
 				
 			}
 			
 			if(nextStep instanceof Park){
 				RConsole.println("Parking...");
 				i++;
 			}
 			else if (circle) {
 				RConsole.println("Following Circle...");
 				while(!checkForStop()) followLeftLine(circle);
 			}
 			else if(currentStep.getDirection() == Direction.Left) {
 				RConsole.println("Following Left...");
 				while(!checkForStop()) followLeftLine(circle);
 			}
 			
 			else if(currentStep.getDirection() == Direction.Right) {
 				RConsole.println("Following Right...");
 				while(!checkForStop()) followRightLine(circle);
 			}
 			
 			else if(currentStep.getDirection() == Direction.Straight) {
 				RConsole.println("Going Left because of next Straight...");
 				while(!checkForStop()) followLeftLine(circle);
 			}
 			
 			stop();
 
 			i++;
 		}
 	}
 	
 	/* Basic Driving Code ****************************************************/
 	public void tankDrive(double left, double right) {
 		left = MathUtils.clamp(left, -1, 1);
 		right = MathUtils.clamp(right, -1, 1);
 		
 		float leftDrive = (float) left;
 		float rightDrive = (float) right;
 		
 		leftMotor.setSpeed(speed * leftDrive);
 		rightMotor.setSpeed(speed * rightDrive);
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
 	public void turnLeft(int angle) {
 		stop();
 		waitOneSecond();
 		pilot.travel(RoboMap.TURN_TRAVEL_DISTANCE);		
 		pilot.arc(0, angle);
 	}
 	public void turnRight(int angle) {
 		stop();
 		waitOneSecond();
 		pilot.travel(RoboMap.TURN_TRAVEL_DISTANCE);		
 		pilot.arc(0, -angle);
 	}
 	public void setSpeed(float speed) {
 		this.speed = speed;
 		this.pilot.setTravelSpeed(speed / 50f);
 	}
 	public void park(boolean left, int space, boolean isbuffer){
 		double len = RoboMap.PARK_LENGTH * space;
 		int sign = left ? 1 : -1;
 		ColorHTSensor colorSensor = left ? this.leftColorSensor : this.rightColorSensor;
 		if(isbuffer) {
			while(!checkColor(colorSensor).equals("BLUE")){
				left ? followLeftLine(colorSensor) : followRightLine(colorSensor);
 			}
			int offset = getAveTacoCount();
 			double dist = 0;
			while(len > dist) {			
				left ? followLeftLine(colorSensor) : followRightLine(colorSensor);
 				
				dist = (getAveTacoCount - offset) * RoboMap.TACO_TO_CM;
 			}
 			pilot.travel(RoboMap.PARK_TRAVEL_DISTANCE);
			pilot.turn(sign * RoboMap.NORMAL_TURN_ANGLE);
 			
 		}
 		
 	
 	}
 }
