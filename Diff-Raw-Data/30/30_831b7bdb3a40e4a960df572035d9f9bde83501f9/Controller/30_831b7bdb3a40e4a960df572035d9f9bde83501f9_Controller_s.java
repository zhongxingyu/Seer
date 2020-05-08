 package com.game.engine;
 
 import org.lwjgl.input.Mouse;
 
 import com.game.src.Game;
 import com.game.src.graphics.Camera;
 
 public class Controller {
 
 	boolean inMenu = false;
 	boolean checkesc = true;
 	Camera cam;
 	
 	public Controller(Camera cam) {
 		this.cam = cam;
 	}
 
 	public void moveController() {
		float walkspeed = 0.15f;
 
 		float mx = Mouse.getDX();
 		float my = Mouse.getDY();
 		mx *= 0.25f;
 		my *= 0.25f;
 
 		if (InputHandler.esc) {
 			new Thread(new Runnable() {
 				public void run() {
 					if (checkesc) {
 						checkesc = false;
 						if (inMenu) {
 							Mouse.setGrabbed(true);
 							inMenu = false;
 						} else if (!inMenu) {
 							Mouse.setGrabbed(false);
 							inMenu = true;
 							Mouse.setCursorPosition(Game.width / 2, Game.height / 2);
 						}
 						try {
 							Thread.sleep(500);
 						} catch (InterruptedException e) {
 							e.printStackTrace();
 						}
 						checkesc = true;
 					}
 				}
 			}).start();
 		}
 
 		if (!inMenu) cam.setRY(cam.getRY() + mx);
 		if (cam.getRY() > 360) cam.setRY(cam.getRY() - 360);
 		if (!inMenu) cam.setRX(cam.getRX() - my);
 		if (cam.getRX() > 85) cam.setRX(85);
 		if (cam.getRX() < -85) cam.setRX(-85);
 
		if (InputHandler.speedup && !InputHandler.slowdown) walkspeed = 0.30f;
		if (!InputHandler.speedup && InputHandler.slowdown) walkspeed = 0.10f;
 
 		if (InputHandler.up && !InputHandler.down) {
 			cam.moves(walkspeed, 1);
 		}
 
 		if (!InputHandler.up && InputHandler.down) {
 			cam.moves(-walkspeed, 1);
 		}
 
 		if (!InputHandler.left && InputHandler.right) {
 			cam.moves(-walkspeed, 0);
 		}
 
 		if (InputHandler.left && !InputHandler.right) {
 			cam.moves(walkspeed, 0);
 		}
 
 		//if (InputHandler.flyup && !InputHandler.flydown) location.y -= walkspeed;
 		//if (!InputHandler.flyup && InputHandler.flydown) location.y += walkspeed;
 
 	}
 
 }
