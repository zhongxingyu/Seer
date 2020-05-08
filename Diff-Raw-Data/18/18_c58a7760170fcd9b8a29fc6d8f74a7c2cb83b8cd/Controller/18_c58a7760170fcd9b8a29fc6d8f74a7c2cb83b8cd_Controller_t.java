 package com.game.src.engine;
 
 import org.lwjgl.input.Mouse;
 import org.lwjgl.util.vector.Vector3f;
 
 public class Controller {
 
	public Vector3f rotation = null;
	public Vector3f location = null;
 	boolean inMenu = false;
 	boolean checkesc = true;
 	private InputHandler input = new InputHandler();
 
	public Controller() {
		rotation = new Vector3f();
		location = new Vector3f();
	}
 
	public void moveController() {
 		float walkspeed = 0.15f;
 
 		float mx = Mouse.getDX();
 		float my = Mouse.getDY();
 		System.out.println(mx);
 		mx *= 0.25f;
 		my *= 0.25f;
 
 		if (input.esc) {
 			new Thread(new Runnable() {
 				public void run() {
 					if (checkesc) {
 						checkesc = false;
 						if (inMenu) inMenu = false;
 						else if (!inMenu) inMenu = true;
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
 
 		if (!inMenu) rotation.y += mx;
 		if (rotation.y > 360) rotation.y -= 360;
 		if (!inMenu) rotation.x -= my;
 		if (rotation.x > 85) rotation.x = 85;
 		if (rotation.x < -85) rotation.x = -85;
 
 		if (input.speedup && !input.slowdown) walkspeed = 0.30f;
 		if (!input.speedup && input.slowdown) walkspeed = 0.10f;
 
 		if (input.up && !input.down) {
 			float cz = (float) (walkspeed * 2 * Math.cos(Math.toRadians(rotation.y)));
 			float cx = (float) (walkspeed * Math.sin(Math.toRadians(rotation.x)));
 			location.z += cz;
 			location.x -= cx;
 		}
 
 		if (!input.up && input.down) {
 			float cz = (float) (walkspeed * 2 * Math.cos(Math.toRadians(rotation.y)));
 			float cx = (float) (walkspeed * Math.sin(Math.toRadians(rotation.x)));
 			location.z -= cz;
 			location.x += cx;
 		}
 
 		if (!input.left && input.right) {
 			float cz = (float) (walkspeed * 2 * Math.cos(Math.toRadians(rotation.y + 90)));
 			float cx = (float) (walkspeed * Math.sin(Math.toRadians(rotation.x + 90)));
 			location.z += cz;
 			location.x -= cx;
 		}
 
 		if (input.left && !input.right) {
 			float cz = (float) (walkspeed * 2 * Math.cos(Math.toRadians(rotation.y + 90)));
 			float cx = (float) (walkspeed * Math.sin(Math.toRadians(rotation.x + 90)));
 			location.z -= cz;
 			location.x += cx;
 		}
 
 		if (input.flyup && !input.flydown) location.y -= walkspeed;
 		if (!input.flyup && input.flydown) location.y += walkspeed;
 
 	}
 
 }
