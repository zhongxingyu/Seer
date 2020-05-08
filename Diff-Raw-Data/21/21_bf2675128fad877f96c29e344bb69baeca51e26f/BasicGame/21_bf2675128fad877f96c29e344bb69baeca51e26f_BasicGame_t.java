 package test;
 
 import javax.vecmath.Vector3f;
 
 import org.lwjgl.opengl.Display;
 
 import engine.Engine;
 import engine.entity.*;
 
 public class BasicGame {
 	private Engine engine;
 	
 	private Actor player;
 	
 	private Camera camera;
 	
 	private static boolean multiThreaded = true;
 	
 	public static void main(String args[]) {
 		BasicGame g = new BasicGame();
 		if(multiThreaded == true)
 			g.runMultiThread();
 		else
 			g.runSingleThread();
 	}
 
 	public BasicGame() {
 		System.out.println("Creating Engine object");
 		engine = new Engine();
 		
 		//Create the player
 		System.out.println("Creating Player");
 		player = (Actor) engine.addActor("player", 10.0f, 0.5f, "box", "default");
 		player.setProperty(Entity.POSITION, new Vector3f(0, 3, 5));
 
 		//Create camera
 		System.out.println("Creating Camera");
 		camera = engine.addCamera(1f, false, "box2");
 		camera.setDistance(20f);
 		camera.focusOn(player);
 
 	}
 
 	public void runMultiThread() {
 		engine.run();
 	}
 	
 	public void runSingleThread() {
 		while(!Display.isCloseRequested()){
 			engine.renderOnce();
 			engine.physicsOnce();
 		}
 	}
 }
