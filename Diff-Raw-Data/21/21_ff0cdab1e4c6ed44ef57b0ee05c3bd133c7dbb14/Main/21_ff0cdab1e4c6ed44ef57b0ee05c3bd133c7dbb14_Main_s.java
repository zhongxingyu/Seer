 package test;
 
 import javax.vecmath.Vector3f;
 import javax.vecmath.Vector4f;
 
 import org.lwjgl.opengl.Display;
 
 import engine.Engine;
 import engine.entity.*;
 
 import engine.render.Model;
 import engine.render.Shader;
 import engine.render.ubos.Light;
 import engine.render.ubos.Lights;
 import engine.render.ubos.Material;
 import engine.terrain.Terrain;
 
 public class Main {
 	private Engine engine;
 	
 	private Actor player;
 	private Actor model;
 	private Entity model2;
 	private Entity model3;
 	private Entity model4;
 	private Entity model5;
 	
 	private Lights lights;
     private Light light;
     private Light light2;
 	
 	private Camera camera;
 	
 	//private LightMenu lightmenu;
 	//private MaterialMenu materialmenu;
 	private static boolean multiThreaded = true;
 	
 	public static void main(String args[]) {
 		Main m = new Main();
 		if(multiThreaded == true)
 			m.runMultiThread();
 		else
 			m.runSingleThread();
 	}
 
 	public Main() {
 		engine = new Engine();
 		/*
 		//Uncomment if you need to import a model into a different format
 		Parser import_model = new Obj_Parser();
 		try {
 			import_model.readFile(new FileInputStream("/home/adam/workspace/jge3d/src/resources/models/armadillo.obj"));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		Model export_model = import_model.createModel();
 		export_model.saveXGL("/home/adam/workspace/jge3d/src/resources/models/misc/armadillo.xgl");
 		*/
 		//Create the ground to stand on
 		Terrain terrain = new Terrain(
 			0,
 			true,
 			(engine.render.Model) engine.resource_manager.getResource("subdivided_cube","models"),
 			(engine.render.Shader) engine.resource_manager.getResource("default","shaders")
 		);
 		terrain.setProperty
 		(Entity.NAME, "terrain");
 		terrain.setProperty("position",new Vector3f(0,0, 0));
 		terrain.setProperty("gravity",new Vector3f(0, 0, 0));
 		terrain.createTerrain(10);
 		engine.addEntity(terrain);
 		
 		//Make some parallax stars
 		//Stars stars = new Stars(engine,1000,10000,200,5,400);
 		//stars.getEntity().applyImpulse(new Vector3f(0,-100,0), new Vector3f(0,0,0));
 		
 		//Create the player
 		player = (Actor) engine.addActor("player", 1.0f, 0.5f, "box", "default");
 		player.setProperty("position",new Vector3f(0, 15, 0));
 		//player.setScale(new Vector3f(1,1,1));
 		//player.setFallSpeed(1);
		player.setProperty("gravity",new Vector3f(0,0,0));
 		/*
 		//Heat seeking box (if box makes contact with player then model5 explodes)
 		model = engine.addActor("model1", 1.0f, "test", "default");
 		model.setProperty(Entity.NAME, "model1");
 		model.setProperty("position", new Vector3f(10, 15, 0));
 		engine.addAIRoutine("model1", "followPlayer");
 		model.addCollisionFunctions("explodeOnPlayerContact");
 		model.setProperty("gravity", new Vector3f(0,0,0));
 
 		//Box that moves to trigger box that launches pyramid
 		model2 = engine.addEntity("model2", 1f, true, "export", "default");
 		model2.setProperty("position",new Vector3f(-6, 0, 0));
 		model2.applyImpulse(new Vector3f(2,0,0), new Vector3f(0,0,0));		
 
 		//Trigger box that launches pyramid
 		model3 = engine.addEntity("model3", 1f, false, "singlebox", "default");
 		model3.setProperty("position",new Vector3f(0, 0, 0));
 		//model3.getModel().setTransparent();
 		model3.addCollisionFunctions("explode");
 
 		//Pyramid that flies off when touched by model2
 		model4 = engine.addEntity("model4", 1f, true, "test", "default");
 		model4.setProperty(Entity.NAME, "model4");
 		model4.setProperty("position",new Vector3f(0, 0, -6));
 		
 		//Just a legoman (explodes if missle collides with player)
 		model5 = engine.addEntity("model5", 1f, true, "legoman", "default");
 		model5.setProperty(Entity.NAME, "model5");
 		model5.setProperty("position",new Vector3f(10, 0, 0));
 		//model5.setScale(new Vector3f(0.1f,0.1f,0.1f));
 		model5.setProperty("gravity",new Vector3f(0,0,0));
 		*/
 		/*
 		//Test armadillo
 		Entity armadillo = engine.addEntity("armadillo", 0f, true, "armadillo", "default");
 		armadillo.setProperty(Entity.NAME, "armadillo");
 		armadillo.setProperty("position",new Vector3f(0, 0, 0));
 		armadillo.setScale(new Vector3f(0.01f,0.01f,0.01f));
 		*/
 		/*
 		String name_to_use = "bunny";
 		Entity test = engine.addEntity(name_to_use, 1f, true, name_to_use, "default");
 		test.setProperty(Entity.NAME, name_to_use);
 		test.setProperty("position",new Vector3f(0, 30, 0));
 		test.setAngularFactor(0, new Vector3f(0,1,0));
 		test.setScale(new Vector3f(100.00f,100.00f,100.00f));
 		 */
 		/*
 		RagDoll ragdoll = new RagDoll(
 			1.0f, 
 			false, 
 			(Model)engine.resource_manager.getResource("box", "models"), 
 			(Shader)engine.resource_manager.getResource("default", "shaders")
 		);
 		ragdoll.setProperty(Entity.NAME, "ragdoll");
 		ragdoll.setProperty("position",new Vector3f(0, 8, -5));
 		engine.addEntity(ragdoll);
 		 */
 		
 		//Create a camera
 		camera = engine.addCamera(1f, false, "box2");
 		camera.setDistance(20f);
 		//camera.setProperty("position",new Vector3f(0, 10, 0));
 		//camera.setProperty("gravity",new Vector3f(0, 0, 0));
 		camera.focusOn(player);
 		
 		addUBOsToDefaultShader();
 		/*
 		lightmenu = new LightMenu();
 		lightmenu.setTheme("lightmenu");
 		engine.addWindow(lightmenu, 400, 400);
 		lightmenu.setName("Light Menu");
 		materialmenu = new MaterialMenu();
 		materialmenu.setTheme("materialmenu");
 		engine.addWindow(materialmenu, 400, 400);
 		materialmenu.setName("Material Menu");
 		
 		lightmenu.setLight(light);
 		materialmenu.setMaterial(test.getModel().getMesh(0).getMaterial());
 		*/
 	}	
 	
 	//This function is just for testing; we'll need to set this stuff at the map level
 	public void addUBOsToDefaultShader() {
 		lights = new Lights();
 		
 		Shader shader = (Shader)engine.resource_manager.getResource("default", "shaders");
 		light = new Light(
 			new Vector4f(10.0f,10.0f,10.0f,1.0f),
 			new Vector4f(2.0f,2.0f,1.0f,255.0f),
 			new Vector4f(2.0f,2.0f,1.0f,255.0f),
 			new Vector4f(2.0f,2.0f,1.0f,255.0f),
 			0.1f,
 			1.0f,
 			0.1f,
 			new Vector3f(-1.0f,-1.0f,0.0f),
 			1.0f,
 			0.1f
 		);
         light2 = new Light(
 			new Vector4f(-10.0f,10.0f,10.0f,1.0f),
 			new Vector4f(1.0f,2.0f,2.0f,255.0f),
 			new Vector4f(1.0f,2.0f,2.0f,255.0f),
 			new Vector4f(1.0f,2.0f,2.0f,255.0f),
 			0.1f,
 			1.0f,
 			0.1f,
 			new Vector3f(1.0f,-1.0f,0.0f),
 			1.0f,
 			1.0f
 		);
         lights.add(light);
         lights.add(light2);
         
         shader.addUBO(lights);
     	shader.addUBO(camera.getMVPmatrix());
     	shader.addUBO(new Material());
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
