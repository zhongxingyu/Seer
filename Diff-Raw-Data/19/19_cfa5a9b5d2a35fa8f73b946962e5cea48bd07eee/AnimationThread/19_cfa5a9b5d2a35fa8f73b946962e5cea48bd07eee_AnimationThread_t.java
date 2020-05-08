 package flock03.threads;
 
 import flock03.*;
 
 public class AnimationThread extends Thread {
     FlockApplet parent;
     public AnimationThread(FlockApplet parent) {
         super("AnimationThread");
         this.parent = parent;
     }
 	public void run() {
		while (true) {
		    if (parent.play) {
		        Renderer.render(parent.bufferGraphics);
    			parent.repaint();
			}
 			try {
 				sleep(Settings.ANIMATION_DELAY);
 			} catch (InterruptedException e) { ; }
 		}
 	}
 }
