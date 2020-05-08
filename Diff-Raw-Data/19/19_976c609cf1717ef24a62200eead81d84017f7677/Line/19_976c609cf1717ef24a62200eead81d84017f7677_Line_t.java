 import lejos.nxt.*;
 import lejos.nxt.addon.ColorSensor;
 import lejos.util.*;
 
 public class Line extends Thread {
 	private int counter = 0;
 	private int length = 0;
 	private long time;
 	private boolean grey = false;
 	private boolean color = false;
 	private boolean failed = false;
 	private ColorSensor counterSensor = new ColorSensor(SensorPort.S3);
 	
 	private Timer timer = new Timer(1000, 
 		new TimerListener(){ 
 			public void timedOut() {
 				Motor.A.stop();
 				failed = true;
 			}
 		}
 	);
 	
 	public Line(int length) {
 		this.length = length;
 		Motor.A.setSpeed(Common.LINE_SPEED);
 		initialize();
 	}
 	
 	private void initialize() {
 		if(counterSensor.getColorNumber() != 2)
 			Motor.A.forward();
 		while(counterSensor.getColorNumber() != 2) {}
 		Motor.A.stop();
 	}
 	
 	public void run() {
 		while(true) {
 			if ((counterSensor.getColorNumber() >= 5 && counterSensor.getColorNumber() <= 12) || counterSensor.getColorNumber() == 2 ) {
 				if (grey && !color) {
 					grey = false;
 					color = true;
 				}
 				if (color && !grey) {
 					if (Motor.A.isBackward()) {
 						color = false;
 						this.counter++;
 						Motor.A.stop();
 					}
 					if (Motor.A.isForward()) {
 						color = false;
 						this.counter--;
 						Motor.A.stop();
 					}
 				}
 			}
 			else if (counterSensor.getColorNumber() == 3)
 				grey = true;
 		}
 	}
 
 	public boolean moveLeft() {
 		if(this.counter == 0)
 			return false;
 		else{
			timer.stop();
			timer.setDelay(1000);
			failed = false;
 			Motor.A.forward();
 			timer.start();
 			while(!Motor.A.isStopped()){}
			if(failed)
 				return false;
 			timer.stop();
 			return true;
 		}
 	}
 	
 	public boolean moveRight() {
 		if(this.counter == this.length)
 			return false;
 		else {
			failed = false;
			timer.stop();
			timer.setDelay(1000);
 			Motor.A.backward();
 			timer.start();
 			while(!Motor.A.isStopped()){}
			if(failed)
 				return false;
 			timer.stop();
 			return true;
 		}
 	}
 	
 
 	public ColorSensor getCounterSensor() {
 		return this.counterSensor;
 	}
 	
 	public int getCount() {
 		return this.counter;
 	}
 }
