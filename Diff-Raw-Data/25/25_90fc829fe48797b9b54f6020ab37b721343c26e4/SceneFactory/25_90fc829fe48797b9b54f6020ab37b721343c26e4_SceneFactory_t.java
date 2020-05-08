 package is.nord.dutchess;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.anddev.andengine.audio.music.MusicManager;
 import org.anddev.andengine.engine.camera.BoundCamera;
 import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.engine.handler.IUpdateHandler;
 import org.anddev.andengine.entity.layer.ILayer;
 import org.anddev.andengine.entity.primitive.Rectangle;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.background.ColorBackground;
 import org.anddev.andengine.entity.scene.background.SpriteBackground;
 import org.anddev.andengine.entity.scene.menu.MenuScene;
 import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
 import org.anddev.andengine.entity.scene.menu.item.ColoredTextMenuItem;
 import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.entity.shape.modifier.RotationModifier;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.text.ChangeableText;
 import org.anddev.andengine.entity.text.Text;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.opengl.font.Font;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.anddev.andengine.util.HorizontalAlign;
 
 import android.hardware.SensorManager;
 import android.util.Log;
 import android.view.KeyEvent;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 /**
  * @Hopureitt
  *
  * This class is constructs and manages scenes both for game play and menus. A part of that registering the scene to an 
  * update handler and implementing it's onUpdate method.  
  * 
  * FIXME: Coins don't always spawn in the frame. 
  *
  */
 
 public class SceneFactory {
 
 	// Local variables
 	private CodenameDutchess activity;
 	private BoundCamera bCamera;
 	private Font font;
 	private GameObjectRegistry gor;
 	private AudioManager am;
 	private AgentSprite agent;
 
 	/*
 	 * Usage:	SceneFactory sf = new SceneFactory(camera, font, scene);
 	 * Pre:		camera is of type Camera, font of type Font, and scene of type Scene, and all three have been set up
 	 * Post:	sf is a SceenFactory object based on the parameters
 	 */
 	public SceneFactory(CodenameDutchess activity, BoundCamera camera, Font font, GameObjectRegistry gor, final AudioManager am)
 	{
 		this.activity = activity;
 		this.bCamera = camera;
 		this.bCamera.setBoundsEnabled(true);
 		this.font = font;
 		this.gor = gor;
 		this.am = am;
 		
 		//Play a random song
 		Random r = new Random();
 		this.am.getPlayList().get(r.nextInt(this.am.getPlayList().size()-1)).play();
 	}
 
 	public Scene createStartScene(IOnMenuItemClickListener listener) {
 		MenuScene menuScene = new MenuScene(this.bCamera);
 		menuScene.addMenuItem(new ColoredTextMenuItem(CodenameDutchess.MENU_MAIN_NEWGAME, this.font, "NEW GAME", 1.0f,0.7f,0.7f, 0.7f,0.7f,0.7f));
 		menuScene.addMenuItem(new ColoredTextMenuItem(CodenameDutchess.MENU_MAIN_QUIT, this.font, "QUIT", 1.0f,0.7f,0.7f, 0.7f,0.7f,0.7f));
 		//menuScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
 		menuScene.buildAnimations();
 		menuScene.setOnMenuItemClickListener(listener);
 		return menuScene;
 	}
 	
 	public Scene createPauseScene(IOnMenuItemClickListener listener) {
 		MenuScene menuScene = new MenuScene(this.bCamera);
 		menuScene.addMenuItem(new ColoredTextMenuItem(CodenameDutchess.MENU_PAUSE_CONTINUE, this.font, "CONTINUE", 1.0f,0.7f,0.7f, 0.7f,0.7f,0.7f));
 		menuScene.addMenuItem(new ColoredTextMenuItem(CodenameDutchess.MENU_MAIN_QUIT, this.font, "QUIT", 1.0f,0.7f,0.7f, 0.7f,0.7f,0.7f));
 		//menuScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));
 		menuScene.buildAnimations();
 		menuScene.setOnMenuItemClickListener(listener);
 		menuScene.centerShapeInCamera(agent); // FIXME?
 		
 		return menuScene;
 	}
 	
 	public Scene createGameOverScene(IOnMenuItemClickListener listener) {
 		MenuScene menuScene = new MenuScene(this.bCamera);
 		menuScene.addMenuItem(new ColoredTextMenuItem(CodenameDutchess.MENU_UNHANDLED, this.font, "GAME OVER\n", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f));
 		menuScene.addMenuItem(new ColoredTextMenuItem(CodenameDutchess.MENU_GAMEOVER_CONTINUE, this.font, "CONTINUE", 1.0f, 0.7f, 1.0f, 0.7f, 0.7f, 0.7f));
 		menuScene.addMenuItem(new ColoredTextMenuItem(CodenameDutchess.MENU_GAMEOVER_QUIT, this.font, "QUIT", 1.0f, 0.7f, 1.0f, 0.7f, 0.7f, 0.7f));
 		menuScene.buildAnimations();
 		menuScene.setOnMenuItemClickListener(listener);
 		menuScene.centerShapeInCamera(agent); // FIXME?
 		
 		return menuScene;
 	}
 	
 	public Scene createWinningScene(IOnMenuItemClickListener listener) {
 		MenuScene menuScene = new MenuScene(this.bCamera);
 		menuScene.addMenuItem(new ColoredTextMenuItem(CodenameDutchess.MENU_UNHANDLED, this.font, "YOU WON!\n", 1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 1.0f));
 		menuScene.addMenuItem(new ColoredTextMenuItem(CodenameDutchess.MENU_WINNING_NEXT_LEVEL, this.font, "NEXT LEVEL", 1.0f, 0.7f, 1.0f, 0.7f, 0.7f, 0.7f));
 		menuScene.addMenuItem(new ColoredTextMenuItem(CodenameDutchess.MENU_WINNING_QUIT, this.font, "QUIT", 1.0f, 0.7f, 1.0f, 0.7f, 0.7f, 0.7f));
 		menuScene.buildAnimations();
 		menuScene.setOnMenuItemClickListener(listener);
 		menuScene.centerShapeInCamera(agent); // FIXME?
 		
 		return menuScene;
 	}
 	
 	
 	/*
 	 * Usage:	sf.createDemoScene();
 	 * Pre:		sf is a SceneFactory object
 	 * Post:	the active scene is a demo scene, similar to the one from earlier vesions of the game.
 	 * 			Proof of concept but not a final or definite version of how game levels are constructed
 	 */
 	public Scene createLevelScene(int level)
 	{
 		final int COINS = level*5;
 		final int TIME = level*100;
 		Scene scene;
 		SceneUpdateHandler sceneUpdateHandler;
 		HUD hud;
 		PhysicsWorld physicsWorld;
 		ChangeableText coinsLeft;
 		ChangeableText timeLeft;
 		List<GameObject> gameObjects;
 		//AgentSprite agent;
 		Body agentBody;
 		GameObject gameObject;
 		
 		scene = new Scene(1);
 		hud = new HUD();
 		this.bCamera.setHUD(hud);
 		
 		coinsLeft = new ChangeableText(5, 5, this.font, String.valueOf(COINS), 2);
 		coinsLeft.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		coinsLeft.setAlpha(0.5f);
 		hud.getTopLayer().addEntity(coinsLeft);
 		
 		timeLeft = new ChangeableText(480-100, 5, this.font, String.valueOf(TIME), HorizontalAlign.RIGHT, 4);
 		timeLeft.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		timeLeft.setAlpha(0.5f);
 		timeLeft.setScale(0.9f);
 		hud.getTopLayer().addEntity(timeLeft);
 
 		physicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false); // FIXME: should be fixed parameters?
 		
 		agent = new AgentSprite(0, 0, this.gor.getAgentTextureRegion());
 		agentBody = PhysicsFactory.createBoxBody(physicsWorld, agent, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f));
 		physicsWorld.registerPhysicsConnector(new PhysicsConnector(agent, agentBody, true, false, true, false));
 		scene.getTopLayer().addEntity(agent);
 		this.bCamera.setChaseShape(agent);
 		
 		sceneUpdateHandler = new SceneUpdateHandler(activity, physicsWorld, timeLeft, coinsLeft, TIME, COINS, scene, agent);
 		activity.setAccelerometerSensor(sceneUpdateHandler);
 		gameObjects = new ArrayList<GameObject>();
 
 		//scene.setBackground(this.gor.getRepeatingBackground());
 		//SpriteBackground backgroundsprite = new SpriteBackground(starbackground);
 		//SpriteBackground backgroundsprite = new SpriteBackground(new Sprite(0, 0, gor.getSpriteBackground()));
 		
 		//scene.setBackground(new SpriteBackground(new Sprite(0, 0, gor.getSpriteBackground())));
 		
 		scene.registerUpdateHandler(physicsWorld);
 		initBorders(scene, this.bCamera, physicsWorld); // make the frame
		Random rand = new Random();
 		
 		for(int i=0; i!=COINS; i++)
 		{
 			gameObject = new CoinSprite(SceneFactory.randomNumber(20, 480*2-20), 
					SceneFactory.randomNumber(20, 320*2-20),
 					20,
 					20,
 					this.gor.getCoinTextureRegion(),
 					sceneUpdateHandler);
 			gameObjects.add(gameObject);
 			scene.getTopLayer().addEntity(gameObject);	
 		}
 		
 		//GrenadeSprite gren = new GrenadeSprite(100, 100, this.gor.getCoinTextureRegion());
 		//scene.getTopLayer().addEntity(gren);
 		
		//Random rand = new Random();
 		for(int i=0; i!= (20+level); i++)
 		{
 			gameObject = new WallSprite(SceneFactory.randomNumber(30, 480*2), 
					SceneFactory.randomNumber(30, 320*2), 
 					this.gor.getWallTextureRegion(), 
 					physicsWorld);
 			gameObjects.add(gameObject);
 			scene.getTopLayer().addEntity(gameObject);
 		}
 		
 		for(int i=0; i!= (20+level); i++)
 		{
 			gameObject = new WallSprite(SceneFactory.randomNumber(30, (480*2)), 
					SceneFactory.randomNumber(30, (320*2)), 
 					this.gor.getVerticalWallTextureRegion(), 
 					physicsWorld);
 			gameObjects.add(gameObject);
 			scene.getTopLayer().addEntity(gameObject);
 		}
 		sceneUpdateHandler.setGameObjects(gameObjects);
 		scene.registerUpdateHandler(sceneUpdateHandler);
 		
 		return scene;
 	}
 	
 	/*
 	 * Usage:	this.initBorders();
 	 * Pre:		this is a sf object
 	 * Post:	The game level's borders have been initialized to form a frame
 	 */
 	private void initBorders(Scene scene, Camera camera, PhysicsWorld physicsWorld) {
 		final Shape bottomOuter = new Rectangle(0, camera.getHeight()*2 - 2, camera.getWidth()*2, 2);
 		final Shape topOuter = new Rectangle(0, 0, camera.getWidth()*2, 2);
 		final Shape leftOuter = new Rectangle(0, 0, 2, camera.getHeight()*2);
 		final Shape rightOuter = new Rectangle(camera.getWidth()*2 - 2, 0, 2, camera.getHeight()*2);
 		
 		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0.5f, 0.5f);
 		PhysicsFactory.createBoxBody(physicsWorld, bottomOuter, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(physicsWorld, topOuter, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(physicsWorld, leftOuter, BodyType.StaticBody, wallFixtureDef);
 		PhysicsFactory.createBoxBody(physicsWorld, rightOuter, BodyType.StaticBody, wallFixtureDef);
 		
 		final ILayer bottomLayer = scene.getTopLayer();
 		bottomLayer.addEntity(bottomOuter);
 		bottomLayer.addEntity(topOuter);
 		bottomLayer.addEntity(leftOuter);
 		bottomLayer.addEntity(rightOuter);	
 	}
 	
 	public static int randomNumber(int min, int max) { return min + (new Random()).nextInt(max-min); }
 }
