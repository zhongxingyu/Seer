 package game;
 
 import game.Car.CarType;
 import physics.BMWM3Properties;
 import physics.CarProperties;
 import physics.EnginePhysics;
 import physics.F430Properties;
 import physics.tools.Conversion;
 import physics.tools.MathTools;
 import save.ProfilCurrent;
 import audio.AudioRender;
 import audio.EngineSoundStore;
 import audio.SoundStore;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.PhysicsSpace;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.input.ChaseCamera;
 import com.jme3.input.InputManager;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.light.AmbientLight;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Matrix3f;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.ViewPort;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.plugins.blender.BlenderLoader;
 import com.jme3.shadow.PssmShadowRenderer;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 import com.jme3.util.SkyFactory;
 
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.screen.Screen;
 
 /**
  * This class contains the common points between all games, such as the controls
  * of the car, initializing sounds, maps...
  * 
  * @author TANGUY Arnaud
  * 
  */
 public abstract class AbstractGameScreenState extends AbstractScreenController
 		implements ActionListener, AnalogListener, PhysicsCollisionListener {
 
 	protected ViewPort viewPort;
 	protected Node rootNode;
 	protected AssetManager assetManager;
 	protected InputManager inputManager;
 
 	protected BulletAppState bulletAppState;
 
 	protected SoundStore<String> soundStore;
 	protected EngineSoundStore engineSoundStore;
 
 	protected Car player;
 	protected CarProperties playerCarProperties;
 	protected EnginePhysics playerEnginePhysics;
 
 	protected boolean runIsOn;
 	protected boolean runFinish;
 	protected boolean playerStartKickDone;
 
 	protected ChaseCamera chaseCam;
 
 	protected TerrainQuad terrain;
 	protected Material mat_terrain;
 	protected RigidBodyControl terrainPhys;
 
 	protected PssmShadowRenderer pssmRenderer;
 
 	protected long startTime = 0;
 	protected long countDown = 0;
 
 	protected boolean soudIsActive = true;
 
 	protected AppStateManager stateManager;
 
 	protected DigitalDisplay digitalTachometer;
 	protected DigitalDisplay digitalSpeed;
 	protected DigitalDisplay digitalGear;
 	protected DigitalDisplay digitalStart;
 	protected ShiftlightLed shiftlight;
 	protected boolean isBreaking;
 	protected long rpmTimer;
 
 	protected boolean needReset;
 	protected boolean needJump = false;
 
 	protected long timerJump = 0;
 	protected long timerRedZone = 0;
 	protected long timerCrashSound = 0;
 	protected boolean playerFinish;
 	protected long timePlayer = 0;
 	protected boolean playerStoped = false;
 
 	private Vector3f jumpForce = new Vector3f(0, 15000, 0);
 
 	boolean zeroSec;
 	boolean oneSec;
 	boolean twoSec;
 	boolean threeSec;
 
 	protected AudioRender<String> audioRender;
 
 	public AbstractGameScreenState() {
 		super();
 	}
 
 	/***** Initialize Nifty gui ****/
 	@Override
 	public void stateAttached(AppStateManager stateManager) {
 	}
 
 	@Override
 	public void stateDetached(AppStateManager stateManager) {
 
 	}
 
 	@Override
 	public void bind(Nifty nifty, Screen screen) {
 		super.bind(nifty, screen);
 		// nifty.setDebugOptionPanelColors(true);
 	}
 
 	@Override
 	public void onEndScreen() {
 		audioRender.mute();
 		stateManager.detach(this);
 		
 		app.gotoStart();
 	}
 
 	@Override
 	public void onStartScreen() {
 	}
 
 	/******* Initialize game ******/
 	@Override
 	public void initialize(AppStateManager stateManager, Application a) {
 		/** init the screen */
 		super.initialize(stateManager, a);
 
 		this.rootNode = app.getRootNode();
 		this.viewPort = app.getViewPort();
 		this.assetManager = app.getAssetManager();
 		this.inputManager = app.getInputManager();
 
 		assetManager.registerLoader(BlenderLoader.class, "blend");
 	}
 
 	protected void initGame() throws Exception {
 		app.setDisplayStatView(false);
 
 		bulletAppState = new BulletAppState();
 		stateManager = app.getStateManager();
 		stateManager.attach(bulletAppState);
 		runIsOn = false;
 		runFinish = false;
 		this.isBreaking = false;
 		this.needReset = false;
 		zeroSec = false;
 		oneSec = false;
 		twoSec = false;
 		threeSec = false;
 		playerStartKickDone = false;
 
 		initAudio();
 		initGround();
 		buildPlayer();
 		setupKeys();
 
 		// Active skybox
 		Spatial sky = SkyFactory.createSky(assetManager,
 				"Textures/Skysphere.jpg", true);
 		rootNode.attachChild(sky);
 
 		// Enable a chase cam
 		chaseCam = new ChaseCamera(app.getCamera(), player.getNode(),
 				inputManager);
 		chaseCam.setSmoothMotion(true);
 		chaseCam.setMaxDistance(100);
 
 		// Set up light
 		DirectionalLight dl = new DirectionalLight();
 		dl.setDirection(new Vector3f(-0.5f, -1f, -0.3f).normalizeLocal());
 		rootNode.addLight(dl);
 
 		AmbientLight al = new AmbientLight();
 		al.setColor(ColorRGBA.White.mult(1.3f));
 		rootNode.addLight(al);
 
 		// Set up shadow
 		pssmRenderer = new PssmShadowRenderer(assetManager, 1024, 3);
 		pssmRenderer.setDirection(new Vector3f(0.5f, -0.1f, 0.3f)
 				.normalizeLocal()); // light direction
 		viewPort.addProcessor(pssmRenderer);
 
 		rootNode.setShadowMode(ShadowMode.Off); // reset all
 		player.getNode().setShadowMode(ShadowMode.CastAndReceive); // normal
 
 		// map.setShadowMode(ShadowMode.Receive);
 		terrain.setShadowMode(ShadowMode.Receive);
 
 		getPhysicsSpace().addCollisionListener(this);
 
 		digitalTachometer = new DigitalDisplay(nifty, screen,
 				"digital_tachometer", 80);
 		digitalSpeed = new DigitalDisplay(nifty, screen, "digital_speed", 50);
 		digitalGear = new DigitalDisplay(nifty, screen, "digital_gear", 50);
 		digitalStart = new DigitalDisplay(nifty, screen, "startTimer", 50);
 		shiftlight = new ShiftlightLed(nifty, screen, playerCarProperties,
 				playerEnginePhysics);
 	}
 
 	protected void initAudio() throws Exception {
 
 		// Init audio
 		soundStore = SoundStore.getInstance();
 		soundStore.setAssetManager(assetManager);
 
 		engineSoundStore = engineSoundStore.getInstance();
 		engineSoundStore.setAssetManager(assetManager);
 
 		//engineSoundStore.addSound(1000, "Models/Default/1052_P.wav");
 		engineSoundStore.addSound(1000, "Models/V8/idle.wav");
 		// channels.put(1126, "Models/Default/1126_P.wav");
 		// channels.put(1205, "Models/Default/1205_P.wav");
 		// channels.put(1289, "Models/Default/1289_P.wav");
 		// channels.put(1380, "Models/Default/1380_P.wav");
 		// channels.put(1476, "Models/Default/1476_P.wav");
 		// channels.put(1579, "Models/Default/1579_P.wav");
 		// channels.put(1690, "Models/Default/1690_P.wav");
 		// channels.put(1808, "Models/Default/1808_P.wav");
 		// channels.put(1935, "Models/Default/1935_P.wav");
 		// channels.put(2070, "Models/Default/2070_P.wav");
 		// channels.put(2215, "Models/Default/2215_P.wav");
 		// channels.put(2370, "Models/Default/2370_P.wav");
 		// channels.put(2536, "Models/Default/2536_P.wav");
 		engineSoundStore.addSound(4000, "Models/V8/med.wav");
 		// channels.put(2904, "Models/Default/2904_P.wav");
 		// channels.put(3107, "Models/Default/3107_P.wav");
 		// channels.put(3324, "Models/Default/3324_P.wav");
 		// channels.put(3557, "Models/Default/3557_P.wav");
 		// channels.put(3806, "Models/Default/3806_P.wav");
 		// channels.put(4073, "Models/Default/4073_P.wav");
 		//engineSoundStore.addSound(7358, "Models/V8/high.wav");
 		// channels.put(4663, "Models/Default/4663_P.wav");
 		// channels.put(4989, "Models/Default/4989_P.wav");
 		// channels.put(5338, "Models/Default/5338_P.wav");
 		// channels.put(5712, "Models/Default/5712_P.wav");
 		// channels.put(6112, "Models/Default/6112_P.wav");
 		engineSoundStore.addSound(9650, "Models/V8/high.wav");
 
 		soundStore.addSound("start", "Models/Default/start.wav");
 		soundStore.addSound("up", "Models/Default/up.wav");
 		soundStore.addSound("lost", "Sound/lost.wav");
 		soundStore.addSound("win", "Sound/win.wav");
 		soundStore.addSound("start_low", "Sound/start_low.wav");
 		soundStore.addSound("start_high", "Sound/start_high.wav");
 		soundStore.addSound("burst", "Sound/explosion.wav");
 		soundStore.addSound("crash", "Sound/car_crash.wav");
 
 		audioRender = new AudioRender<String>(rootNode, soundStore);
 	}
 	protected void buildPlayer() {
 		//playerCarProperties = (ProfilCurrent.getInstance() == null) ? new CarProperties () :
 			//ProfilCurrent.getInstance().getCar().get(ProfilCurrent.getInstance().getChoixCar());
 		//XXX
 		playerCarProperties = (ProfilCurrent.getInstance() == null) ? new BMWM3Properties () :
 			ProfilCurrent.getInstance().getCar().get(ProfilCurrent.getInstance().getChoixCar());
 		//playerCarProperties = new F430Properties();			
 		
 		// Create a vehicle control
 		player = new Car(assetManager, playerCarProperties, "ferrari red");
 //		player = new Car(assetManager, playerCarProperties, "corvette.j3o");
 
 		player.setType(CarType.PLAYER);
 		player.setDriverName("Player");
 		player.getNode().addControl(player);
 		player.setPhysicsLocation(new Vector3f(0, 27, 700));
 		player.setNosCharge(1);
 
 		playerCarProperties = player.getProperties();
 		playerEnginePhysics = player.getEnginePhysics();
 
 		rootNode.attachChild(player.getNode());
 
 		getPhysicsSpace().add(player);
 
 	}
 
 	public void initGround() {
 		/** 1. Create terrain material and load four textures into it. */
 		mat_terrain = new Material(assetManager,
 				"Common/MatDefs/Terrain/Terrain.j3md");
 
 		/** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
 		mat_terrain.setTexture("Alpha",
 				assetManager.loadTexture("Textures/alphamap.png"));
 
 		/** 1.2) Add GRASS texture into the red layer (Tex1). */
 		Texture grass = assetManager.loadTexture("Textures/grass.jpg");
 		grass.setWrap(WrapMode.Repeat);
 		mat_terrain.setTexture("Tex1", grass);
 		mat_terrain.setFloat("Tex1Scale", 64f);
 
 		/** 1.3) Add DIRT texture into the green layer (Tex2) */
 		Texture dirt = assetManager.loadTexture("Textures/carreau.jpg");
 		dirt.setWrap(WrapMode.Repeat);
 		mat_terrain.setTexture("Tex2", dirt);
 		mat_terrain.setFloat("Tex2Scale", 64f);
 
 		/** 1.4) Add ROAD texture into the blue layer (Tex3) */
 		Texture rock = assetManager.loadTexture("Textures/road2.jpg");
 		rock.setWrap(WrapMode.Repeat);
 		mat_terrain.setTexture("Tex3", rock);
 		mat_terrain.setFloat("Tex3Scale", 128f);
 
 		/** 2. Create the height map */
 		AbstractHeightMap heightmap = null;
 		Texture heightMapImage = assetManager
 				.loadTexture("Textures/mountains512.png");
 
 		heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
 		heightmap.load();
 
 		/**
 		 * 3. We have prepared material and heightmap. Now we create the actual
 		 * terrain: 3.1) Create a TerrainQuad and name it "my terrain". 3.2) A
 		 * good value for terrain tiles is 64x64 -- so we supply 64+1=65. 3.3)
 		 * We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
 		 * 3.4) As LOD step scale we supply Vector3f(1,1,1). 3.5) We supply the
 		 * prepared heightmap itself.
 		 */
 		int patchSize = 65;
 		terrain = new TerrainQuad("my terrain", patchSize, 513,
 				heightmap.getHeightMap());
 
 		/**
 		 * 4. We give the terrain its material, position & scale it, and attach
 		 * it.
 		 */
 		terrain.setMaterial(mat_terrain);
 		terrain.setLocalTranslation(0, -100, 0);
 		terrain.setLocalScale(2f, 1f, 2f);
 		rootNode.attachChild(terrain);
 
 		/** 5. The LOD (level of detail) depends on were the camera is: */
 		TerrainLodControl control = new TerrainLodControl(terrain,
 				app.getCamera());
 		terrain.addControl(control);
 
 		// Rendre le terrain physique
 
 		terrain.setLocalScale(3f, 2f, 4f);
 
 		terrainPhys = new RigidBodyControl(0.0f);
 		terrain.addControl(terrainPhys);
 		bulletAppState.getPhysicsSpace().add(terrainPhys);
 
 		bulletAppState.getPhysicsSpace()
 				.setGravity(new Vector3f(0, -19.81f, 0));
 		terrainPhys.setFriction(0.5f);
 
		bulletAppState.getPhysicsSpace().enableDebug(assetManager);
 	}
 
 	protected void setupKeys() {
 		inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_Q));
 		inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_D));
 		inputManager.addMapping("GearUp", new KeyTrigger(KeyInput.KEY_Z));
 		inputManager.addMapping("GearDown", new KeyTrigger(KeyInput.KEY_S));
 		inputManager.addMapping("Reset", new KeyTrigger(KeyInput.KEY_RETURN));
 		inputManager.addMapping("Mute", new KeyTrigger(KeyInput.KEY_M));
 		inputManager.addMapping("GearUp", new KeyTrigger(KeyInput.KEY_A));
 		inputManager.addMapping("GearDown", new KeyTrigger(KeyInput.KEY_E));
 
 		inputManager.addMapping("GearUp", new KeyTrigger(KeyInput.KEY_UP));
 		inputManager.addMapping("GearDown", new KeyTrigger(KeyInput.KEY_DOWN));
 		inputManager.addMapping("Throttle", new KeyTrigger(
 				KeyInput.KEY_RCONTROL));
 		inputManager.addMapping("Lefts", new KeyTrigger(KeyInput.KEY_LEFT));
 		inputManager.addMapping("Rights", new KeyTrigger(KeyInput.KEY_RIGHT));
 		inputManager.addMapping("NOS", new KeyTrigger(KeyInput.KEY_RSHIFT));
 		inputManager.addMapping("NOS", new KeyTrigger(KeyInput.KEY_LSHIFT));
 		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
 
 		inputManager.addMapping("Menu", new KeyTrigger(KeyInput.KEY_ESCAPE));
 
 		inputManager.addListener(this, "Lefts");
 		inputManager.addListener(this, "Rights");
 		inputManager.addListener(this, "Ups");
 		inputManager.addListener(this, "Downs");
 		inputManager.addListener(this, "Reset");
 		inputManager.addListener(this, "Mute");
 		inputManager.addListener(this, "GearUp");
 		inputManager.addListener(this, "GearDown");
 		inputManager.addListener(this, "Throttle");
 		inputManager.addListener(this, "NOS");
 		inputManager.addListener(this, "Jump");
 		inputManager.addListener(this, "Menu");
 
 	}
 
 	@Override
 	public void update(float tpf) {
 		int playerRpm = player.getEnginePhysics().getFreeRpm();
 		int playerSpeed = (int) Math.abs(player.getCurrentVehicleSpeedKmHour());
 
 		/** Stops 1 second after the finish line */
 		if (playerFinish && !playerStoped) {
 			player.stop(1000);
 			playerStoped = true;
 		}
 
 		if (runIsOn) {
 			// XXX why the hell is it needed !!
 			digitalStart.setText(" ");
 
 			if (!player.getBurstEnabled() && !playerFinish) {
 				if (playerStartKickDone) {
 					playerRpm = player.getEnginePhysics().getRpm();
 				} else {
 					playerStartKickDone = true;
 				}
 
 				playerEnginePhysics.setSpeed(Math.abs(Conversion
 						.kmToMiles(playerSpeed)));
 				float force = -(float) playerEnginePhysics.getForce() / 5;
 				player.accelerate(2, force * 2);
 				player.accelerate(3, force * 2);
 
 				if (needJump) {
 					player.applyImpulse(jumpForce, Vector3f.ZERO);
 					needJump = false;
 				}
 			} else if (player.getBurstEnabled()) {
 				audioRender.mute();
 				playerRpm = 0;
 			}
 		} else {
 			if (!runFinish) {
 				countDown();
 			}
 
 			// Baisser le régime moteur à l'arrêt
 			playerEnginePhysics.setRpm(playerEnginePhysics.getFreeRpm() - 100);
 		}
 
 		// Traiter le cas du sur-régime
 		if (playerRpm > (playerCarProperties.getRedline() - 500)) {
 			if (!player.getBurstEnabled()) {
 				// Déclencher le timer s'il n'est pas activé
 				if (timerRedZone == 0) {
 					timerRedZone = System.currentTimeMillis();
 				} else {
 					if (System.currentTimeMillis() - timerRedZone > 3000) {
 						player.explode();
 						audioRender.mute();
 						playerFinish = true;
 						timePlayer = 0;
 					}
 				}
 			}
 		} else {
 			timerRedZone = 0;
 		}
 
 		// Update audio
 		if (soudIsActive) {
 			if (!player.getBurstEnabled()) {
 				player.updateSound(playerRpm);
 			} else {
 				player.mute();
 			}
 			app.getListener().setLocation(
 					player.getNode().getWorldTranslation());
 		}
 
 		if (player.getNosActivity()) {
 			player.controlNos();
 		}
 
 		// particule_motor.controlBurst();
 
 		digitalTachometer.setText(((Integer) playerRpm).toString());
 		digitalSpeed.setText(((Integer) playerSpeed).toString());
 		digitalGear.setText(((Integer) playerEnginePhysics.getGear())
 				.toString());
 		shiftlight.setRpm(playerRpm);
 	}
 
 	/**
 	 * Displays a countdown
 	 */
 	protected void countDown() {
 		/*
 		 * long ellapsedTime = System.currentTimeMillis() - countDown;
 		 * 
 		 * if (ellapsedTime < time) { screen.findElementByName("startTimer")
 		 * .getRenderer(TextRenderer.class) .setText( ((Long) ((time -
 		 * ellapsedTime + 1000) / 1000)) .toString()); } else if (ellapsedTime
 		 * >= time && ellapsedTime < time + 500) {
 		 * screen.findElementByName("startTimer")
 		 * .getRenderer(TextRenderer.class).setText(""); runIsOn = true;
 		 * audio_motor.playStartBeepHigh();
 		 * playerEnginePhysics.setRpm(initialRev); startTime =
 		 * System.currentTimeMillis(); countDown = 0; }
 		 */
 		if (countDown != 0) {
 			long time = System.currentTimeMillis() - countDown;
 			if (time > 5000) {
 				if (!zeroSec) {
 					audioRender.play("start_high");
 					zeroSec = true;
 				}
 				digitalStart.setText(" ");
 				runIsOn = true;
 				startTime = System.currentTimeMillis();
 			} else if (time > 4000) {
 				if (!oneSec) {
 					audioRender.play("start_low");
 					oneSec = true;
 				}
 				digitalStart.setText("1");
 			} else if (time > 3000) {
 				if (!twoSec) {
 					audioRender.play("start_low");
 					twoSec = true;
 				}
 				digitalStart.setText("2");
 			} else if (time > 2000) {
 				if (!threeSec) {
 					audioRender.play("start_low");
 					threeSec = true;
 				}
 				digitalStart.setText("3");
 			}
 		}
 	}
 
 	protected void reset() {
 		player.setPhysicsLocation(new Vector3f(0, 27, 700));
 		player.setPhysicsRotation(new Matrix3f());
 		player.setLinearVelocity(Vector3f.ZERO);
 		player.setAngularVelocity(Vector3f.ZERO);
 		player.setNosCharge(1);
 		playerEnginePhysics.setGear(1);
 		player.resetSuspension();
 		player.steer(0);
 		audioRender.play("start");
 
 		player.accelerate(0);
 		player.setLife(100);
 		playerEnginePhysics.setSpeed(0);
 		playerEnginePhysics.setRpm(1000);
 
 		if (player.getBurstEnabled()) {
 			player.removeExplosion();
 		}
 
 		player.stopNos();
 
 		timerRedZone = 0;
 		playerFinish = false;
 		playerStoped = false;
 		runIsOn = false;
 		playerStartKickDone = false;
 		needReset = false;
 		runFinish = false;
 		needJump = false;
 		startTime = 0;
 		countDown = 0;
 
 		threeSec = false;
 		twoSec = false;
 		oneSec = false;
 		zeroSec = false;
 
 		digitalStart.setText("Ready ?");
 	}
 
 	protected PhysicsSpace getPhysicsSpace() {
 		return bulletAppState.getPhysicsSpace();
 	}
 
 	public void onAction(String binding, boolean value, float tpf) {
 		if (binding.equals("Lefts")) {
 			// XXX: Needs analog controller for releasing the wheels too!
 			if (!value) {
 				player.setSteeringValue(0.f);
 				player.steer(player.getSteeringValue());
 			}
 		} else if (binding.equals("Rights")) {
 			if (!value) {
 				player.setSteeringValue(0);
 				player.steer(0);
 			}
 		} else if (binding.equals("Space")) {
 			if (value) {
 				player.brake(700f);
 			} else {
 				player.brake(0f);
 			}
 		} else if (binding.equals("Reset")) {
 			if (value) {
 				System.out.println("Reset");
 				needReset = true;
 			}
 		} else if (binding.equals("GearUp")) {
 			if (value) {
 				audioRender.play("up");
 				playerEnginePhysics.incrementGear();
 			}
 		} else if (binding.equals("GearDown")) {
 			if (value) {
 				playerEnginePhysics.decrementGear();
 			}
 		} else if (binding.equals("NOS")) {
 			if (value) {
 				if (!player.getNosActivity()) {
 					player.addNos();
 				}
 			}
 		} else if (binding.equals("Jump")) {
 			if (value) {
 				if (System.currentTimeMillis() - timerJump > 2000
 						&& !player.getBurstEnabled() && runIsOn) {
 					needJump = true;
 					timerJump = System.currentTimeMillis();
 				}
 			}
 		} else if (binding.equals("Menu")) {
 			if (value) {
 				this.onEndScreen();
 			}
 		}
 	}
 
 	@Override
 	public void onAnalog(String binding, float value, float tpf) {
 		if (binding.equals("Throttle")) {
 			if (!player.getBurstEnabled()) {
 				// Start countdown
 				if (countDown == 0) {
 					countDown = System.currentTimeMillis();
 				}
 
 				playerEnginePhysics
 						.setRpm(playerEnginePhysics.getFreeRpm() + 400);
 			}
 		} else if (binding.equals("Rights")) {
 			float val = player.getSteeringValue();
 			val = val - value;
 			if (val < -0.5)
 				val = -0.5f;
 			player.setSteeringValue(val);
 			player.steer(player.getSteeringValue());
 		} else if (binding.equals("Lefts")) {
 			float val = player.getSteeringValue();
 			val = val + value;
 			if (val > 0.5)
 				val = 0.5f;
 			player.setSteeringValue(val);
 			player.steer(player.getSteeringValue());
 		}
 	}
 
 	@Override
 	public void collision(PhysicsCollisionEvent event) {
 		Car car1 = null;
 		Car car2 = null;
 		Car car = null;
 		if (event.getObjectA() instanceof Car) {
 			car1 = (Car) event.getObjectA();
 		}
 		if (event.getObjectB() instanceof Car) {
 			car2 = (Car) event.getObjectB();
 		}
 
 		// Two cars collide
 		if (car1 != null && car2 != null) {
 			// Trigger crash sound
 			if (car1.getType().equals(CarType.PLAYER)
 					|| car2.getType().equals(CarType.PLAYER)) {
 				// Trigger only if the sound is not playing
 				if (timerCrashSound == 0
 						|| System.currentTimeMillis() - timerCrashSound > 2000) {
 					audioRender.play("crash", 20f);
 
 					timerCrashSound = System.currentTimeMillis();
 				}
 			}
 
 			float speed1 = Math.abs(car1.getCurrentVehicleSpeedKmHour());
 			float speed2 = Math.abs(car2.getCurrentVehicleSpeedKmHour());
 			float appliedImpulse = event.getAppliedImpulse();
 			// Impact, reduce friction
 			float damageForce = (appliedImpulse - event.getCombinedFriction() / 10) / 10000;
 
 			/*
 			 * System.out.println("Collision between " + car1.getType() + " " +
 			 * car1.getDriverName() + " and " + car2.getType() + " " +
 			 * car2.getDriverName()); System.out.println("Lateral 1 impulse " +
 			 * event.getAppliedImpulseLateral1());
 			 * System.out.println("Lateral 2 impulse " +
 			 * event.getAppliedImpulseLateral2());
 			 * System.out.println("Combined friction " +
 			 * event.getCombinedFriction()); System.out.println("Force " +
 			 * appliedImpulse);
 			 */
 
 			Vector3f forward1 = new Vector3f(0, 0, 0).subtract(
 					car1.getForwardVector(null)).normalize();
 			Vector3f forward2 = new Vector3f(0, 0, 0).subtract(
 					car2.getForwardVector(null)).normalize();
 			Vector2f f1 = new Vector2f(forward1.x, forward1.z);
 			Vector2f f2 = new Vector2f(forward2.x, forward2.z);
 
 			float angle = Math.abs(MathTools.orientedAngle(f1, f2));
 
 			// Frontal collision
 			if (angle >= Math.PI - Math.PI / 4
 					&& angle <= Math.PI + Math.PI / 4) {
 				float speedPercent1 = speed1 / (speed1 + speed2);
 				float life1 = 10 * speedPercent1 * damageForce;
 				life1 = (life1 <= 50) ? life1 : 50;
 				float life2 = 10 * (1 - speedPercent1) * damageForce;
 				life2 = (life2 <= 50) ? life2 : 50;
 				car1.decreaseLife(life1);
 				car2.decreaseLife(life2);
 			} else {
 				/*
 				 * back collision if (angle <= Math.PI / 4) the car in front
 				 * will have 75% of the damages 25% for the car in back
 				 */
 				double speedDifferenceDamage = Math.abs(speed2 - speed1)
 						* damageForce / 2;
 				if (car1.inFront(car2)) {
 					car1.decreaseLife(0.75 * speedDifferenceDamage);
 					car2.decreaseLife(0.25 * speedDifferenceDamage);
 				} else {
 					car1.decreaseLife(0.25 * speedDifferenceDamage);
 					car2.decreaseLife(0.75 * speedDifferenceDamage);
 				}
 			}
 		} else {
 			RigidBodyControl control = null;
 
 			if (car1 != null) {
 				car = car1;
 				try {
 					control = (RigidBodyControl) event.getObjectB();
 				} catch (Exception e) {
 					control = null;
 				}
 			} else if (car2 != null) {
 				car = car2;
 				try {
 					control = (RigidBodyControl) event.getObjectA();
 				} catch (Exception e) {
 					control = null;
 				}
 			}
 
 			if (car != null && control != null) {
 				float speed = Math.abs(car.getCurrentVehicleSpeedKmHour());
 
 				if (control.getUserObject().equals("Tree")) {
 					car.decreaseLife(speed / 10);
 				}
 			}
 		}
 	}
 }
