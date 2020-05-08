 package test;
 
 import javax.vecmath.Vector3f;
 
 import engine.Engine;
 import engine.entity.*;
 
 public class Main {
 	private Engine engine;
 
 	private Actor player;
 	private Actor model;
 	private Entity model2;
 	private Entity model3;
 	private Entity model4;
 	private Entity model5;
 	private Camera camera;
 	
 	public static void main(String args[]) {
 		boolean multiThreaded = false;
 		
 		Main m = new Main();
 		if(multiThreaded == true)
 			m.runMultiThread();
 		else
 			m.runSingleThread();
 	}
 
 	public Main() {
 		engine = new Engine();
 		
 		engine.addModel("box", "resources/models/misc/box.xgl");
 		engine.addModel("box2", "resources/models/misc/box2.xgl");
 		engine.addModel("test", "resources/models/misc/test.xgl");
 		engine.addModel("export", "resources/models/misc/export.xgl");
 		engine.addModel("singlebox", "resources/models/misc/singlebox.xgl");
 		engine.addModel("legoman", "resources/models/misc/legoman.xgl");
 	
 		//Create the ground to stand on
 		//Terrain terrain = new Terrain(0,true,engine.getModelByName("singlebox"),engine.getShaderByName("default"));
 		//terrain.setProperty(Entity.NAME, "terrain");
 		//terrain.setPosition(new Vector3f(0,-10, 0));
 		//terrain.createTerrain(20);
 		//engine.addEntity(terrain);
 		
 		//Make some parallax stars
 		//Stars stars = new Stars(engine,1000,10000,200,5,400);
 		//stars.getEntity().applyImpulse(new Vector3f(0,-100,0), new Vector3f(0,0,0));
 		
 		//Create the player
 		player = (Actor) engine.addActor("player", 1.0f, 0.5f, "box", "default");
 		player.setPosition(new Vector3f(5, 15, 5));
 		player.setScale(new Vector3f(1,1,1));
 		//player.setFallSpeed(1);
 		player.setGravity(10);
 		
 		//Heat seeking box (if box makes contact with player then model5 explodes)
 		model = engine.addActor("model1", 1.0f, "test", "default");
 		model.setProperty(Entity.NAME, "model1");
 		model.setPosition(new Vector3f(10, 15, 0));
 		engine.addAIRoutine("model1", "followPlayer");
 		model.addCollisionFunctions("explodeOnPlayerContact");
 		model.setGravity(0);
 
 		//Box that moves to trigger box that launches pyramid
 		model2 = engine.addEntity("model2", 1f, true, "export", "default");
 		model2.setPosition(new Vector3f(-6, 0, 0));
 		model2.applyImpulse(new Vector3f(2,0,0), new Vector3f(0,0,0));		
 
 		//Trigger box that launches pyramid
 		model3 = engine.addEntity("model3", 1f, false, "singlebox", "default");
 		model3.setPosition(new Vector3f(0, 0, 0));
 		//model3.getModel().setTransparent();
 		model3.addCollisionFunctions("explode");
 
 		//Pyramid that flies off when touched by model2
 		model4 = engine.addEntity("model4", 1f, true, "test", "default");
 		model4.setProperty(Entity.NAME, "model4");
 		model4.setPosition(new Vector3f(0, 0, -6));
 		
 		//Just a legoman (explodes if missle collides with player)
 		model5 = engine.addEntity("model5", 1f, true, "legoman", "default");
 		model5.setProperty(Entity.NAME, "model5");
 		model5.setPosition(new Vector3f(10, 0, 0));
 		model5.setScale(new Vector3f(0.1f,0.1f,0.1f));
 		model5.setGravity(new Vector3f(0,0,0));
 		
 		//Create a camera
 		camera = engine.addCamera(1f, false, "box2");
 		camera.setDistance(30f);
 		camera.setPosition(new Vector3f(0, 0, 6));
 		camera.focusOn(player);
 	}
 
 	public void runMultiThread() {
 		engine.run();
 	}
 	
 	public void runSingleThread() {
 		while(true){
 			engine.renderOnce();
 			engine.physicsOnce();
 		}
 	}
 }
