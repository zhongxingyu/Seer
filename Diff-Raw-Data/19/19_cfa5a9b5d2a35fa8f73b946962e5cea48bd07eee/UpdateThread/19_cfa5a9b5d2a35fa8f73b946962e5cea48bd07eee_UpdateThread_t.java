 package flock03.threads;
 
 import flock03.*;
 
 public class UpdateThread extends Thread {
     FlockApplet parent;
     public UpdateThread(FlockApplet parent) {
         super("UpdateThread");
         this.parent = parent;
     }
     
     public void run() {
        while (true) {
		    if (parent.play) {
    			World.update();
    		}
 			try {
 				sleep(Settings.UPDATE_DELAY);
 			} catch (InterruptedException e) { ; }
 		}
 	}
 }
