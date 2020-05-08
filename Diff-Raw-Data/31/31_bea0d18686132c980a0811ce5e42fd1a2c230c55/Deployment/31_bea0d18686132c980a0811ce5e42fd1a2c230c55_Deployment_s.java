 package org.usfirst.frc348;
 
 import edu.wpi.first.wpilibj.Jaguar;
 
 public class Deployment {
     Jaguar jag;
     double gain;
    boolean running, direction;
    long startTime;
     
     public Deployment(int motorPort, double gain) {
 	jag = new Jaguar(motorPort);
 	this.gain = gain;
 	running = false;
     }
 
     public void periodic() {
 	if (running) {
	    jag.set(direction ? gain : -gain);

	    if ((System.currentTimeMillis() - startTime) > 2000) {
		direction = !direction;
	    }
 	} else {
	    startTime = System.currentTimeMillis();
	    direction = true;
 	}
     }
 
     public void enable() {
 	running = true;
     }
 
     public void disable() {
 	running = false;
     }
 }
