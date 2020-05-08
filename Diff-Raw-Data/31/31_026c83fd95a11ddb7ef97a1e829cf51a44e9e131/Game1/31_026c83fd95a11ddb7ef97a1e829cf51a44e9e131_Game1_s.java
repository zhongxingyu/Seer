 package wingkwai.main;
 
 import plia.core.Game;
 import plia.core.Screen;
 import plia.core.debug.Debug;
 import plia.core.event.OnTouchListener;
 import plia.core.event.TouchEvent;
 import plia.core.scene.*;
 import plia.core.scene.animation.Animation;
 import plia.core.scene.animation.PlaybackMode;
 import plia.core.scene.shading.Color3;
 import plia.math.Vector2;
 import android.os.Bundle;
 import android.text.method.Touch;
 import android.util.Log;
 import android.view.Window;
 import android.view.WindowManager;
 
 public class Game1 extends Game
 {
 	private Scene scene = new Scene();
 	private Layer<Group> layer1 = new Layer<Group>();
 	private Layer<View> layer2 = new Layer<View>();
 	
 	// Viewport
 	private Camera camera = new Camera("Main Camera");
 	
 	// Character
 	private Group buffy;
 	
 	// Terrain
 	private Terrain terrain;
 	
 	// UI
 	private Button controller;
 	
 	// Vehicle CTRL
 	private Vehicle vehicle;
 	private VehicleController vehicleController;
 	
 	// Race Track
 	BSplineCollider trackOutside, trackInside;
 	
 	public void onInitialize(Bundle arg0)
 	{
 		setRequestedOrientation(0);
 		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
 		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
 		
 		setScene(scene);
 		
 		loadContent();
 		init();
 	}
 	
 	private void loadContent()
 	{
 		terrain = terrain("terrain/heightmap.bmp", "terrain/diffusemap.jpg", 400, 2000);
 		buffy = model("model/buffylow.FBX");
 		
 		controller = button("ui/controller.png");
 	}
 
 	private void init()
 	{
 		terrain.setName("Terrain");
 		buffy.setName("Buffy");
 		
 		// Translate Terrain Center to (0, 0)
 		terrain.setPosition(-1000, -1000, 0);
 		
 		// Create Collider for Buffy
 		SphereCollider buffyCollider = collider(3);
 		buffyCollider.translate(0, 0.5f, 2);
 		
 		// Set Scale and Add Collider to Buffy
 		buffy.setScale(0.1f, 0.1f, 0.1f);
 		buffy.setCollider(buffyCollider);
 		
 		terrain.attachCollider(buffyCollider);
 		
 		// Set Buffy's Animation Clip
 		Animation buffyAnimation = buffy.getAnimation();
 		buffyAnimation.getAnimationClip("idle").set(0, 30, PlaybackMode.LOOP);
 		buffyAnimation.addAnimationClip("run", 35, 50, PlaybackMode.LOOP);
 		buffyAnimation.play("idle");
 		
 		// Setup Camera Position and Set Sky Dome
 		camera.setPosition(0, -10, 9);
 		camera.rotate(-10, 0, 0);
 		camera.setRange(2500);
 		camera.setSky(skydome("sky/sky_sphere01.jpg"));
 		
 		// Camera Follow Buffy
 		buffy.addChild(camera);
 		
 		vehicle = new Vehicle(buffy);
 		vehicleController = new VehicleController(vehicle);
 
 		float ratio = (float)Screen.getWidth() / Screen.getHeight();
 		float scalef = 0.2f;
 		controller.setScale(scalef, scalef * ratio);
 		controller.setActive(false);
 		controller.setOnTouchListener(new OnTouchListener()
 		{
 			
 			public void onTouch(Button button, int action, float x, float y)
 			{
 				Vector2 center = button.getCenter();
 				
 				float dx = center.x - x;
 				float dy = center.y - y;
 				
 				Vector2 dir = vec2(dx, dy).getNormalized();
 
 				if(dir.y != 0.0f && !Float.isNaN(dir.y))
 				{
 					vehicleController.accelerate(0.03f * dir.y);
 				}
 
 				if(dir.x != 0.0f && !Float.isNaN(dir.x))
 				{
 					vehicleController.turn(dir.x);
 				}
 			}
 		});
 		
 		Scene.setMainCamera(camera);
 		
 		Vector2[] p = new Vector2[14];
 		p[0] = new Vector2(514, 190);
 		p[1] = new Vector2(791, 10);
 		p[2] = new Vector2(800, -172);
 		p[3] = new Vector2(621, -409);
 		p[4] = new Vector2(80, -658);
 		p[5] = new Vector2(-218, -631);
 		p[6] = new Vector2(-569, -427);
 		p[7] = new Vector2(-815, -158);
 		p[8] = new Vector2(-780, 95);
 		p[9] = new Vector2(-617, 188);
 		p[10] = new Vector2(317, 209);
 		p[11] = new Vector2(514, 190);
 		p[12] = new Vector2(791, 10);
 		p[13] = new Vector2(800, -172);
 		
 		trackOutside = new BSplineCollider(0.25f, 200, false, p);
 //		trackOutside.addCollider(buffyCollider);
 		
		collider = collider(0, -1, 0, 50, 50);
 		collider.setPosition(152, 218, 140);
 		
 		layer1.addChild(terrain, buffy, trackOutside, collider);
 		layer2.addChild(controller);
 		
 		scene.addLayer(layer1);
 		scene.addLayer(layer2);
 	}
 
 	public void onUpdate()
 	{
 		vehicleController.update();
 		
 		if(Collider.intersect(collider, buffy.getCollider()))
 		{
 			Log.println(Log.ASSERT, "", "Is Collision");
 		}
 		
//		print(collider.getScale().toString());
//		print(buffy.getPosition());
 		
 		Debug.drawBounds(collider, new Color3(1, 1, 0));
 		
 //		Debug.drawBounds(trackOutside, new Color3(0.5f, 1, 0.5f));
 		
 		Debug.drawBounds(buffy.getCollider(), new Color3(0.5f, 1, 0.5f));
 	}
 	
 	PlaneCollider collider;
 
 	@Override
 	public void onTouchEvent(int action, float x, float y)
 	{
 		if(action == TouchEvent.ACTION_UP || action == TouchEvent.ACTION_NONE)
 		{
 			controller.setActive(false);
 		}
 		else if(action == TouchEvent.ACTION_DOWN)
 		{
 			controller.setActive(true);
 			controller.setCenter(x, y);
 		}
 	}
 }
