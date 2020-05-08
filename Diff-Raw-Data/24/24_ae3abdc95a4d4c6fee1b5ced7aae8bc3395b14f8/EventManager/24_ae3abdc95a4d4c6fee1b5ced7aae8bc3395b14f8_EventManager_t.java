 package event_handler;
 
 import java.awt.event.KeyEvent;
 import event_handler.InteractionEvent;
 import player.Player;
 
 public class EventManager {
 	// List of all portals?  
 		// break up by type?
     	    // break up by.. interactionability?
 	Player player;
 	
 	public void setPlayer(Player p) {
 		player = p;
 	}
 	public void handleEvent(InteractionEvent e) {	
 		if(e.getType() == InteractionEvent.KYB) {
 			KeyEvent k = e.getKeyEvent();
 			int key = k.getKeyCode();
 			if(key== KeyEvent.VK_KP_UP || 
 					key==KeyEvent.VK_KP_DOWN ||
 					key== KeyEvent.VK_KP_LEFT|| 
					key== KeyEvent.VK_KP_RIGHT ||
					key == KeyEvent.VK_UP || 
					key == KeyEvent.VK_DOWN|| 
					key == KeyEvent.VK_LEFT|| 
					key == KeyEvent.VK_RIGHT){
				System.out.println("wtf?");
 				player.acceptInteraction(e);
 			}
 		}
 		
 	}
 
 }
