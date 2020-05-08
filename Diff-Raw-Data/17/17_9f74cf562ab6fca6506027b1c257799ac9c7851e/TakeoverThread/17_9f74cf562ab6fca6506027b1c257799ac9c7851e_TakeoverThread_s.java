 package projectubernahme;
 
 import projectubernahme.lifeforms.Lifeform;
 
 public class TakeoverThread extends Thread {
 
 	private Lifeform l;
 	private Lifeform prey;
 	private Player player;
 
 	public TakeoverThread(Lifeform lifeform, Player controllingPlayer, Lifeform lifeform2) {
 		l = lifeform;
 		player = controllingPlayer;
 		prey = lifeform2;
 
 
 	}
 
 	public void run () {
 		/* set the lifeform to busy */
 		l.busy = true;
 		l.actionProgress = 0.0;
 
 
 		try {
 			/* walk towards the victim */
			if (l.distance(prey) > 0.5) {
 				double xdist, ydist;
 				xdist = prey.getX() - l.getX();
 				ydist = prey.getY() - l.getY();
 				
 				l.viewAngle = Math.atan2(ydist, xdist);
 				l.localxvsign = 1;
 			}
 			
 			/* stop when reached the prey */
 			l.localxvsign = 0;
 
 			/* burn time until it is ready */
 			while (l.actionProgress < 1.0) {
 				l.actionProgress += 0.05;
 
 				sleep(50);
 			}
 
 			/* take over the lifeform */
 			if (l.canTakeover(prey)) {
 				prey.setControlled(player);
 	
 				// TODO make a special adding method in the player class
 				player.controlledLifeforms.add(prey);
 			}
 
 
 		} catch (InterruptedException e) {
 			/* if the action was interrupted, then it will just stop, the busy state is reset at the end of this method anyway */
 		}
 
 		/* set the lifeform back to normal */
 		l.busy = false;
 	}
 
 
 }
