 package master;
 
 /**
  * 
  */
 
 import lejos.nxt.LightSensor;
 import lejos.nxt.NXTRegulatedMotor;
 import lejos.nxt.Sound;
 import lejos.nxt.LCD;
 import lejos.nxt.Motor;
 import lejos.util.Delay;
 
 /**
  * @author Frank the Tank
  * 
  */
 public class LineDetectorThread extends Thread {
 
 	private LightSensor ls;
 	private Odometer odo;
 	private int a;
 	private int b;
 	private int c;
 	private int d;
 	private int lightValue;
 	private int delta1;
 	private int delta2;
 	private int delta3;
 	private int delta4;
 	private double DelayConstant;
 	private static NXTRegulatedMotor MotorX;
 	public boolean onLine;
 	public int counter;
 	int lineCounter;
 
 	public LineDetectorThread(LightSensor ls, Odometer odo,
 			NXTRegulatedMotor motor) {
 		this.ls = ls;
 		this.odo = odo;
 		MotorX = motor;
 	}
 
 	@Override
 	public void run() {
 
 		while (true) {
 			int currentMotor = MotorX.getSpeed();
 			if (currentMotor == 0) {
 				currentMotor = 1;
 			}
 			DelayConstant = 22;
 			lightValue = ls.getLightValue();
 			Delay.msDelay((int) DelayConstant);
 			a = ls.getLightValue();
 			Delay.msDelay((int) DelayConstant);
 			b = ls.getLightValue();
 			Delay.msDelay((int) DelayConstant);
 			c = ls.getLightValue();
 			Delay.msDelay((int) DelayConstant);
 			d = ls.getLightValue();
 
 			delta1 = lightValue - a;
 			delta2 = lightValue - b;
 			delta3 = lightValue - c;
 			delta4 = lightValue - d;
 
 			// beeps when sensor detects line,
			if (delta1 < 4 && (delta2 > 4) || (delta3 > 4) || (delta4 > 4)) {
 
 
 				onLine = true;
 				
 			} else {
 				onLine = false;
 			}
 			Delay.msDelay((int) DelayConstant);
 		}
 	}
 
 }
