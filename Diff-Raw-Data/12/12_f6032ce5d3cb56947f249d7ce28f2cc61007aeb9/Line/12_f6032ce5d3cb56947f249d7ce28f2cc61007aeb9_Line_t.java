 import lejos.nxt.*;
 import lejos.nxt.addon.ColorSensor;
 
 public class Line extends Thread {
 	final int LINE_SPEED = 250;
 	
 	protected int counter = 0;
 	protected boolean grey = true;
 	protected boolean color = false;
 	protected ColorSensor counterSensor = new ColorSensor(SensorPort.S3);
 	
 	public Line() {
 		Motor.A.setSpeed(this.LINE_SPEED);
 	}
 	
 	public void run() {
 		while (!Button.ENTER.isPressed()) {
 			if (counterSensor.getColorNumber() >= 5 && counterSensor.getColorNumber() <= 10) {
 				if (grey && !color) {
 					grey = false;
 					color = true;
 				}
 				if (color && !grey) {
 					if (Motor.A.isBackward()) {
 						color = false;
 						counter++;
 						Motor.A.stop();
 					}
 					if (Motor.A.isForward()) {
 						color = false;
 						counter--;
 						Motor.A.stop();
 					}
 				}
 			}
 			else if (counterSensor.getColorNumber() < 5) {
 				grey = true;
 			}
 		}
 	}
 
 	public ColorSensor getCounterSensor() {
 		return this.counterSensor;
 	}
 	
 	public int getCount() {
 		return this.counter;
 	}
 	
 	public boolean driveLeft() {
		if(this.counter == 0 || this.counter == 1){
 			return false;
 		}
 		else{
 			Motor.A.forward();
 			while(!Motor.A.isStopped()){}
 			return true;			
 		}
 	}
 	
 	public boolean driveRight() {
 		if(this.counter == 9){
 			return false;
 		}
 		else {
 			Motor.A.backward();
 			while(!Motor.A.isStopped()){}
 			return true;
 		}
 	}
 }
