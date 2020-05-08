 package application_states;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import ships.Asteroid;
 import ships.Entity;
 import ships.Player;
 import ships.SmartEnemy;
 import base.Arrow;
 import base.CollisionManager;
 import base.GameUtils;
 import base.RunnableProcessor;
 import base.Runner;
 import base.Updatable;
 import base.WeaponChooser;
 import base.events.RemoveUpdatableEvent;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.audio.AudioRenderer;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.input.InputManager;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.Camera;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.CameraNode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.system.AppSettings;
 import com.jme3.ui.Picture;
 import com.jme3.util.SkyFactory;
 
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.effects.Effect;
 import de.lessvoid.nifty.effects.EffectEventId;
 import de.lessvoid.nifty.effects.impl.Border;
 import de.lessvoid.nifty.effects.impl.ColorBar;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.layout.align.HorizontalAlign;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 
 /**
  * The pieces of the game active while use is playing. Manages spawns, damage, gui
  */
 @SuppressWarnings("unused")
 public class GameState extends AbstractAppState implements ActionListener, ScreenController
 {
 	public int level = 1;
 	int startWeapons = 2;
//	private int[] levels = {0,1,10,15,20,25,30,50,Integer.MAX_VALUE};
	private int[] levels = {0,0,0,0,0,0,0,0,Integer.MAX_VALUE};
 	
     private Node gameNode = new Node("Main Game Node");
     static private Runner instance;
     
     public ArrayList<Updatable> updatables;
     private RunnableProcessor	runnableProcessor;
     
     private Player myPlayer;
     private float healthInitial, shieldInitial;
     
     private Arrow myArrow;
     private Camera cam;
    
     BulletAppState bulletAppState;
     
    public WeaponChooser weaponChooser;
     
     private Picture crosshairs;
     
     private AssetManager assetManager;
     private InputManager inputManager;
     private AppStateManager stateManager;
     private ViewPort viewPort;
     private AppSettings settings;
     private Node guiNode;
     private ViewPort guiViewPort;
     private AudioRenderer audioRenderer;
     
     private Nifty niftyGui;
     
     public boolean isGameCreated = false;
     
     /**
      * Return root node
      * @return root node
      */
     public Node getRootNode() {
         return gameNode;
     }
     
     /**
      * Return an instance of the Runner
      * 
      * @return default Runner instance
      */
     public static Runner getInstance() {
         return instance;
     }
     
     /**
      * Returns the processor to add events on update
      * 
      * @return runnable processor instance
      */
     public RunnableProcessor getRunnableProcessor(){
     	return runnableProcessor;
     }
     
     /**
      * Start the game
      */
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
         super.initialize(stateManager, app);
         instance = (Runner) app;
         assetManager = instance.getAssetManager();
         inputManager = instance.getInputManager();
         this.stateManager = instance.getStateManager();
         cam = instance.getCamera();
         viewPort = instance.getViewPort();
         settings = instance.getSettings();
         guiNode = instance.getGuiNode();
         guiViewPort = instance.getGuiViewPort();
         audioRenderer = instance.getAudioRenderer();
         guiNode = instance.getGuiNode();
         
         //add HUD
         niftyGui = Runner.getNifty();
         
       //Start update managers
         updatables = new ArrayList<Updatable>();
         runnableProcessor = new RunnableProcessor();
 
         //set input
         initKeys();
        
         //add crosshairs
         createCrosshairs();
         
         setEnabled(false);
     }
     
     /**
      * Start the instance of the game
      */
     public void createNewGame(){
     	//Start physics engine
         initPhysics();
     	
     	//Create PC
         myPlayer = new Player(assetManager.loadModel("Models/LowPolyFighter/LowPolyFighter.j3o"));
       //  myPlayer.shipSpatial.move(0f, -2f, 0f); 	//third person
         myPlayer.getNode().move(0, -2f, -4f);			//first person
         updatables.add(myPlayer);
         healthInitial = myPlayer.health;
         shieldInitial = myPlayer.initialShield;
         
         //Create arrow
         com.jme3.scene.debug.Arrow arrowMesh = new com.jme3.scene.debug.Arrow(myPlayer.getNode().getWorldRotation().getRotationColumn(2));
         Geometry arrowSpatial = new Geometry("Arrow",arrowMesh);
         Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         mat.setColor("Color", ColorRGBA.Gray);
         arrowSpatial.setMaterial(mat);
         myArrow = new Arrow(arrowSpatial, myPlayer.getNode());
         
         //Create a light
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
         sun.setColor(ColorRGBA.White.mult(2f));
         
         //generate asteroids
         createAsteroids(2000,500);
         
         //add models to scene
         gameNode.attachChild(createSky());
         gameNode.attachChild(myPlayer.getNode());
         gameNode.addLight(sun);
         
     	//manage weapons
         weaponChooser = new WeaponChooser(myPlayer);
     	
        //Map camera to player node
        CameraNode camNode = new CameraNode("Trail", cam);
        camNode.move(0, 0, -10);  //for low poly fighter
        //camNode.move(0,6,-20);  //for Normandy
        myPlayer.getNode().attachChild(camNode);
        
        isGameCreated = true;
     }
     
     /**
      * End and clean up instance of game
      */
     public void endGame(){
     	//remove and end all updatables
     	for(Updatable u: updatables) {
 			u.deleteEntity();
 			Runner.getGameState().deregisterPhysics(u);
     	}
     	updatables.clear();
     	
     	//remove all runnables
     	runnableProcessor = new RunnableProcessor();
     	
     	isGameCreated = false;
     }
     
     /**
      * Add a physics object to the physics space
      * @param o the object to add
      */
     public void registerPhysics(Object o){
         bulletAppState.getPhysicsSpace().add(o);
     }
     
     public void deregisterPhysics(Object o){
         bulletAppState.getPhysicsSpace().remove(o);
     }
     
     /**
      * Creates a new enemy as a random location. Registers it with the update loop. Adds it to game space.
      */
     private void createEnemy(){
     	Entity enemy = new SmartEnemy(
                 assetManager.loadModel("Models/LowPolyFighter/LowPolyFighter.j3o"),
                 myPlayer);
 
         Vector3f displacement = GameUtils.randomVector(2000);
         Vector3f inFront =  myPlayer.getNode().getLocalTranslation().add(
                             myPlayer.getNode().getLocalRotation().getRotationColumn(2).mult(0));      
         enemy.getNode().move(displacement.add(inFront));
         updatables.add(enemy);
         gameNode.attachChild(enemy.getNode());
        /* Ship enemy2 = new PerfectTrackerEnemy(
                 assetManager.loadModel("Models/LowPolyFighter/LowPolyFighter.j3o"),
                 myPlayer);
         Vector3f displacement2 = GameUtils.randomVector(50);
         Vector3f inFront2 =  myPlayer.getNode().getLocalTranslation().add(
                             myPlayer.getNode().getLocalRotation().getRotationColumn(2).mult(500));      //500 units in front of ship
         enemy2.getNode().move(displacement2.add(inFront));
         updatables.add(enemy2);
         gameNode.attachChild(enemy2.getNode());
         */
     }
     
     /**
      * Creates a skybox with correct resources.
      * @return a functioning skybox
      */
     private Spatial createSky(){
     	return SkyFactory.createSky(assetManager,
     			assetManager.loadTexture("Textures/amazingBox/sky_right1.png"),
     			assetManager.loadTexture("Textures/amazingBox/sky_left2.png"),
     			assetManager.loadTexture("Textures/amazingBox/sky_back6.png"),
     			assetManager.loadTexture("Textures/amazingBox/sky_front5.png"),
     			assetManager.loadTexture("Textures/amazingBox/sky_top3.png"),
     			assetManager.loadTexture("Textures/amazingBox/sky_bottom4.png"));
     }
     
     /**
      * Create asteroids and add to display
      * 
      * @param maxDist	maximum distance for asteroids
      * @param num		number of asteroids
      */
     private void createAsteroids(float maxDist, int num){
     	Material rock = new Material(Runner.getInstance().getAssetManager(), 
     	        "Common/MatDefs/Misc/Unshaded.j3md");
     	rock.setTexture("ColorMap", Runner.getInstance().getAssetManager().loadTexture(
     			"Textures/rocky/gaspra.jpg"));
     	for(int i = 0; i < num; i++){
     		Vector3f loc = GameUtils.randomVector(maxDist);
     		//Spatial model = assetManager.loadModel("Models/AsteroidFinal/asteroid OBJ.j3o");
     		//Spatial model = new Geometry("Asteroid" + i, new Sphere(32,32,GameUtils.randomInt(20)));
     		Spatial model = assetManager.loadModel("Models/asteroid_model/asteroid.j3o");
     		model.scale(GameUtils.randomInt(10));
     		model.setMaterial(rock);
 			Asteroid a = new Asteroid(	model, 
 										GameUtils.randomVector(FastMath.PI / 5), 
 										GameUtils.randomVector(0.25f));
 			a.getNode().move(loc);
 			updatables.add(a);
 	        gameNode.attachChild(a.getNode());
     	}
     }
     
     /**
      * Create a 2d crosshairs image on GUI Node
      */
     private void createCrosshairs(){
         crosshairs = new Picture("Crosshairs");
         crosshairs.setImage(assetManager, "Textures/spaceCrosshairs.png", true);
         float size = settings.getHeight() / 5f;
         crosshairs.setWidth(size);
         crosshairs.setHeight(size);
         crosshairs.setPosition(settings.getWidth()/2 - size/2, settings.getHeight()/2 - size/2);
     }
     
     /**
      * Turn on physics engine. Register correct physics listener for collisions.
      */
     private void initPhysics(){
         //turn on physics
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
         //bulletAppState.setDebugEnabled(true);
         
         //add collision monitor
         PhysicsCollisionListener physicsListener = new CollisionManager();
         bulletAppState.getPhysicsSpace().addCollisionListener(physicsListener);
         
         //turn off excessive physics
         bulletAppState.getPhysicsSpace().setGravity(new Vector3f(0,0,0));
     }
     
     /**
      * Create original key mappings
      */
     private void initKeys(){
         inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_LEFT));
         inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_UP));
         inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_RIGHT));
         inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_DOWN));
         
         inputManager.addMapping("Shoot1", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
         inputManager.addMapping("Shoot2", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
         
         inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_ESCAPE));
         
         String[] numberKeys = {"1","2","3","4","5","6","7","8","9"};
         int[] keyTriggers = {	KeyInput.KEY_1, KeyInput.KEY_2, KeyInput.KEY_3, 
         						KeyInput.KEY_4,KeyInput.KEY_5, KeyInput.KEY_6, 
         						KeyInput.KEY_7, KeyInput.KEY_8, KeyInput.KEY_9};
         for(int i = 0; i < numberKeys.length; i++)
         	inputManager.addMapping(numberKeys[i], new KeyTrigger(keyTriggers[i]));
         
         inputManager.addMapping("Shift",
                 new KeyTrigger(KeyInput.KEY_LSHIFT),
                 new KeyTrigger(KeyInput.KEY_RSHIFT));
     }
     
     /**
      * Substitute a key mapping
      * 
      * @param name      the name of the control to map
      * @param letter    the letter to map to
      */
     public void updateKey(String name, int letter){
         inputManager.deleteMapping(name);
         inputManager.addMapping(name, new MouseButtonTrigger(letter));
     }
     
     /**
      * Enable and disable game. Disable when you want to see another enemy
      */
     @Override
     public void setEnabled(boolean enabled) {
         super.setEnabled(enabled);
 
         if (enabled) {
             //Init stuff that is in use while this state is RUNNING, AKA key input
             
             //Updates the tpf so it is not some ridiculously high number
             Runner.getInstance().getTimer().getTimePerFrame();
             niftyGui.gotoScreen("hud");
             bulletAppState.setEnabled(true);
 
             inputManager.addListener(weaponChooser, 
                     "1", "2", "3", "4", "5", "6", "7", "8", "9", "Shift");
             inputManager.addListener(myPlayer,"Left","Up","Right","Down","Shoot1","Shoot2");
             inputManager.addListener(this, "Pause");
             
             inputManager.addRawInputListener(myPlayer);
             
             if (inputManager.isCursorVisible())
                 inputManager.setCursorVisible(false);
             
             guiNode.attachChild(crosshairs);
             
             viewPort.attachScene(gameNode);
         } 
         else {
             //Take away everything not needed while this state is PAUSED, AKA key input
         	if(bulletAppState != null)
         		bulletAppState.setEnabled(false);
             
             inputManager.removeListener(this);
             inputManager.removeListener(weaponChooser);
             inputManager.removeListener(myPlayer);
             inputManager.removeRawInputListener(myPlayer);
             
             guiNode.detachChild(crosshairs);
             
             viewPort.detachScene(gameNode);
         }
     }
     
     /**
      * Executed every frame while game is running
      */
     @Override
     public void update(float tpf) {
 
         if (FastMath.rand.nextFloat() < (10f*tpf)) //add enemy rarely
             createEnemy();
         
         //process queued events
         runnableProcessor.process();
         
         Entity closest = null;
         float closestDistance = Float.MAX_VALUE;
         
         for(Updatable u: updatables) {
 			u.updateObject(tpf);
 			if(!u.isValid()){
 				runnableProcessor.addRunnable(new RemoveUpdatableEvent(u));
 				u.deleteEntity();
 				Runner.getGameState().deregisterPhysics(u);
 			}
 			
 			if(u instanceof Entity && ! (u instanceof Player) && ! (u instanceof Asteroid) ){
 				float distance = ((Entity)u).getNode().getWorldTranslation().distance(
 						myPlayer.getNode().getWorldTranslation());
 				if(distance < closestDistance){
 					closest = (Entity)u;
 					closestDistance = distance;
 				}
 					
 			}
 				
 		}
         
         //update arrow
         if(closest != null)
         	myArrow.pointTo(closest.getNode());
         
         //update health bar
         int lastLevel = levels[level-1];
         int nextLevel = levels[level];
         int need = nextLevel - lastLevel;
        GameUtils.changeNiftyText(niftyGui.getScreen("hud"), "level_text", "LEVEL " + level, HorizontalAlign.center);
         if(PlayerStats.kills >= nextLevel){
         	level++;
         	changePic(level+1);
         }
         updateHealth( 	(myPlayer.health / healthInitial) * 100f,
         				(myPlayer.shield / shieldInitial) * 100f,
         				((PlayerStats.kills-lastLevel) / need) * 100f);
         
         //MUST be at end
         gameNode.updateLogicalState(tpf);
         gameNode.updateGeometricState();
     }
     
     /**
      * Catch pauses
      */
     @Override
     public void onAction(String name, boolean isPressed, float tpf)
     {
         if (name.equals("Pause") && isPressed) {
             setEnabled(false);
             Runner.getState(PauseState.class).setEnabled(true);
         }
         
     }
     
     private String DEFAULT_COLOR = "#0000", PRIMARY_COLOR = "#00FF00", SECONDARY_COLOR = "#FF0000";
     
     /**
      * Remove boarder from a weapon
      * @param weapon
      */
     public void clearWeaponAppearance(int weapon){
     	setWeaponBorder(weapon,DEFAULT_COLOR);
     }
     
     /**
      * Display a weapon border
      * @param weapon
      * @param primary
      */
     public void updateWeaponAppearance(int weapon, boolean primary){
     	setWeaponBorder(weapon, primary? PRIMARY_COLOR: SECONDARY_COLOR);
     }
     
     /**
      * Set a weapon border
      * @param weapon
      * @param color
      */
     private void setWeaponBorder(int weapon, String color){
     	Element panel = niftyGui.getScreen("hud").findElementByName(weapon + "");
     	List<Effect> effects = panel.getEffects(EffectEventId.onActive, Border.class);
     	if(effects.size() > 0){
 	    	Effect borderEffect = effects.get(0);
 	    	borderEffect.getParameters().setProperty("color", color);
 	    	panel.startEffect(EffectEventId.onActive);
     	}
     }
     
     private void changePic(int weapon){
     	Element panel = niftyGui.getScreen("hud").findElementByName(weapon + "");
     	List<Effect> effects = panel.getEffects(EffectEventId.onActive, ColorBar.class);
     	if(effects.size() > 0){
     		Effect overlayEffect = effects.get(0);
 	    	overlayEffect.getParameters().setProperty("color", "#00000000");
 	    	panel.startEffect(EffectEventId.onActive);
     	}
     }
     
     /**
      * Update health display
      * @param percentHealth
      * @param percentShield
      */
     private void updateHealth(float percentHealth, float percentShield, float percentXP){
     	updateBar("health_bar_panel", percentHealth);
     	updateBar("shield_bar_panel", percentShield);
     	updateBar("xp_bar_panel", percentXP);
     }
     
     /**
      * Update a bar (shielding or health)
      * @param name
      * @param percent
      */
     private void updateBar(String name, float percent){
     	Element panel = niftyGui.getScreen("hud").findElementByName(name);
     	List<Effect> effects = panel.getEffects(EffectEventId.onActive, ColorBar.class);
     	if(effects.size() > 0){
     		Effect barEffect = effects.get(0);
     		barEffect.getParameters().setProperty("width", percent + "%");
     		panel.startEffect(EffectEventId.onActive);
     	}
     }
     
 	@Override
 	public void bind(Nifty arg0, Screen arg1) {}
 
 	@Override
 	public void onEndScreen() {}
 
 	@Override
 	public void onStartScreen() {}
 }
