 package org.usfirst.frc348;
 
 import edu.wpi.first.wpilibj.Jaguar;
 
 public class Deployment {
     Jaguar jag;
     double gain;
    boolean running;
     
     public Deployment(int motorPort, double gain) {
 	jag = new Jaguar(motorPort);
 	this.gain = gain;
 	running = false;
     }
 
     public void periodic() {
 	if (running) {
	    jag.set(gain);
 	} else {
	    jag.set(0);
 	}
     }
 
     public void enable() {
 	running = true;
     }
 
     public void disable() {
 	running = false;
     }
 }
