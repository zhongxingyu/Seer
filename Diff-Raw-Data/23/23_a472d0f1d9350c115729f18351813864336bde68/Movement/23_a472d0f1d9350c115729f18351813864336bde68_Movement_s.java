 import org.lwjgl.input.Keyboard;
 import org.lwjgl.input.Mouse;
 
 public class Movement {
 	
 	public static void check() {
 		
 		// just testing .... 
 		if (Mouse.next()) { 	// buffered events
 			if (Mouse.getEventButtonState()) { // pressed down?
 				if (Player.onlinePlayers.get(0).inBounds(Mouse.getX(), Mouse.getY())) {
 					SimpleText.drawString("YOU CLICKED BOX 0", 500, 100);
 				}
 			}
 		}
 		
 		// why the bloody hell is this in this file??
 		// this is for player movement ONLY!!!!!!!
 	/*	if (Keyboard.isKeyDown(Keyboard.KEY_ESCAPE)) {
 			States.setState(2);
 		}
 	*/	
 		
 		// currently just moving to the mouse coordinates
 		if (Keyboard.isKeyDown(Keyboard.KEY_W)) {
			Player.onlinePlayers.get(0).update(Mouse.getDX(), Mouse.getDY());
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_S)) {
 			Player.onlinePlayers.get(0).update(Mouse.getDX(), Mouse.getDY());
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_A)) {
 			Player.onlinePlayers.get(0).update(Mouse.getDX(), Mouse.getDY());
 		}
 		if (Keyboard.isKeyDown(Keyboard.KEY_D)) {
 			Player.onlinePlayers.get(0).update(Mouse.getDX(), Mouse.getDY());
 		}
 		
 	/*
 	 * int dx = Mouse.getDX();   // speed of movement on x axis
 	 * int dy = -Mouse.getDY();   // speed of movement on y axis
 	 */
 	}
 }
