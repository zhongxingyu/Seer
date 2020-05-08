 package team53;
 
 import lejos.nxt.LightSensor;
 import lejos.nxt.Motor;
 import lejos.nxt.SensorPort;
 import lejos.nxt.addon.ColorSensorHT;
 import lejos.robotics.navigation.DifferentialPilot;
 import lejos.util.PIDController;
 
 public class Robot {
 	LightSensor sensor;
 	PIDController pid;
 	DifferentialPilot pilot;
 	int speed = 600;
 
 	public Robot() {
 		sensor = new LightSensor(SensorPort.S1);
 	}
 
 	public void calibratePID(int target, float kp, float ki, float kd) {
 		pid = new PIDController(target);
 		pid.setPIDParam(PIDController.PID_KP, kp);
 		pid.setPIDParam(PIDController.PID_KI, ki);
 		pid.setPIDParam(PIDController.PID_KD, kd);
		
 	}
 	public void calibratePilot(float wheelDiameter, float trackWidth) {
 		pilot = new DifferentialPilot(wheelDiameter, trackWidth, Motor.B, Motor.C);
		pilot.setTravelSpeed(100);
		pilot.setRotateSpeed(100);
 	}
 
 	private team53.Color getColor(ColorSensorHT sensor) {
 
 		team53.Color c = null;
 		int result = 0;
 		for (int i = 0; i < 50; i++) {
 			result += sensor.getColorID();
 		}
 		result /= 50;
 		System.out.println(result);
 
 		switch (sensor.getColorID()) {
 		case 6:
 			c = team53.Color.White;
 			break;
 		case 3:
 			c = team53.Color.Yellow;
 			break;
 		case 5:
 			c = team53.Color.Red;
 			break;
 		case 0:
 			c = team53.Color.Red;
 			break;
 		case 2:
 			c = team53.Color.Blue;
 			break;
 		case 8:
 			c = team53.Color.Black;
 			break;
 		case 1:
 			c = team53.Color.Green;
 			break;
 
 		}
 
 		return c;
 	}
 
 	public void turnLeft() {
 		pilot.rotate(-90);		
 	}
 
 	public void turnRight() {
 		pilot.rotate(90);
 	}
 
 	public void forward(double num) {
 		int value = pid.doPID(sensor.getLightValue());
		System.out.println(value);
 
		pilot.steer(value);
 	}
 }
