 package logic;
 
 import networking.common.Network;
 import UI.Display;
 
 import state.World;
 
 /**
  * A timer thread for periodic screen and game state updates.
  * At this point, created and started by the Window class.
  * @author markovmadd
  *
  */
 public class UpdateThread extends Thread {
 
 private final Display display;
 private final World world;
 private GameUpdate gUpdate;
 
 /**
  * Creates a new UpdateThread with references to the World and Display.
  * @param w - world to update.
  * @param d - display to update.
  */
 	public UpdateThread(World w, Display d) {
     world = w;
     display = d;
     gUpdate = new GameUpdate();
     w.setGameUpdate(gUpdate);
 	}
 
 	/**
 	 * Every 100 milliseconds, updates the display and the world state.
 	 */
 public void run(){
	while(1 == 1){
 		world.update();
 
 		//Network.sendUpdate(gUpdate);
 		//get the update back here
 		gUpdate = new GameUpdate();
 		world.setGameUpdate(gUpdate);
 		display.repaint();
 		try {
 			Thread.sleep(100);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 	}
 
 }
 }
 }
