 package main.game;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import main.Settings;
 import main.game.art.EmbellishmentManager;
 import main.game.entities.controls.GroundControl;
 import main.game.entities.controls.ReaperControl;
 import main.game.entities.controls.SpacecraftControl;
 import main.game.entities.userinput.GroundListener;
 import main.game.entities.userinput.SpacecraftListener;
 import main.game.entities.userinput.UniversalListener;
 import main.game.physics.HitManager;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseAxisTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.material.Material;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.RenderManager;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.texture.Texture;
 import com.jme3.util.SkyFactory;
 
 public class Core extends SimpleApplication {
 	private Settings settings;
 	private BulletAppState bulletAppState;
 	private HitManager hitManager;
 
 	// this is the body/machine, where you are inside, which you are playing
 	private Spatial character;
 	private static final float CAM_DISTANCE_BEHIND_CHAR = 50;
 	private SpacecraftControl spaceControl;
 	private GroundControl groundControl;
 	private boolean camBehindChar = false;
 
 	/**
 	 * 
 	 * @author danielwenzel, danielthevessen, fabiankessler, simonmichalke
 	 */
 	private EmbellishmentManager embi;
 
 	public static void main(String[] args) {
 		Core coreapp = new Core();
 		coreapp.start();
 	}
 
 	@Override
 	public void simpleInitApp() {
 		bulletAppState = new BulletAppState();
 		stateManager.attach(bulletAppState);
 
 		embi = new EmbellishmentManager(rootNode, assetManager, renderManager,
 				viewPort);
 
 		hitManager = new HitManager(bulletAppState.getPhysicsSpace(), embi);
 
 		settings = new Settings();
 
 		Texture northTex = assetManager
 				.loadTexture("assets/Textures/AlternativeSkybox/TestSky_back6.png");
 		Texture downTex = assetManager
 				.loadTexture("assets/Textures/AlternativeSkybox/TestSky_bottom4.png");
 		Texture southTex = assetManager
 				.loadTexture("assets/Textures/AlternativeSkybox/TestSky_front5.png");
 		Texture westTex = assetManager
 				.loadTexture("assets/Textures/AlternativeSkybox/TestSky_left2.png");
 		Texture eastTex = assetManager
 				.loadTexture("assets/Textures/AlternativeSkybox/TestSky_right1.png");
 		Texture upTex = assetManager
 				.loadTexture("assets/Textures/AlternativeSkybox/TestSky_top3.png");
 
 		final Vector3f normalScale = new Vector3f(-1, 1, 1);
 		Spatial skySpatial = SkyFactory.createSky(assetManager, westTex,
 				eastTex, northTex, southTex, upTex, downTex, normalScale);
 		rootNode.attachChild(skySpatial);
 
 		initSpatials();
 
 		inputManager.setCursorVisible(false);// hides the cursor
 
 		// ChaseCamera chaseCam = new ChaseCamera(cam, blue);
 		// Cam = new Camera();
 
 		flyCam.setEnabled(false);
 		bulletAppState.setDebugEnabled(false);
 
 		initKeys(ControlType.SPACECRAFT);
 	}
 
 	@Override
 	public void simpleUpdate(float tpf) {
 		bulletAppState.getPhysicsSpace().update(tpf);
 		bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);
 
 		inputManager.setCursorVisible(false);// no cursor
 
 		Vector3f camvec = character.getLocalTranslation();
 		// Quaternion q, p;
 
 		if (camBehindChar) {
 
 			character.localToWorld(
 					new Vector3f(0, 0, -CAM_DISTANCE_BEHIND_CHAR), camvec);
 			// p = new Quaternion(0, 0, 1, +CAM_DISTANCE_BEHIND_CHAR); //-cam*
 			// or +cam* please test
 			// p.mult(character.getLocalRotation());
 			// q.addLocal(p);
 		}
 
 		cam.setLocation(camvec);
 		cam.setRotation(character.getLocalRotation());
 	}
 
 	public void switchCam() {
 		camBehindChar = !camBehindChar;
 	}
 
 	private void initSpatials() {
 		Material mat_brick = new Material(assetManager,
 				"Common/MatDefs/Misc/Unshaded.j3md");
 		mat_brick.setTexture("ColorMap", assetManager
 				.loadTexture("Textures/Terrain/BrickWall/BrickWall.jpg"));
 
 		Spatial box = new Geometry("Box", new Box(new Vector3f(-2.5f, -2.5f,
 				-2.5f), new Vector3f(2.5f, 2.5f, 2.5f)));
 		box.setMaterial(mat_brick);
 		rootNode.attachChild(box);
 		box.setLocalTranslation(25f, -10f, 75f);
 		RigidBodyControl box_rbc = new RigidBodyControl(8f);
 		box.addControl(box_rbc);
 		bulletAppState.getPhysicsSpace().add(box_rbc);
 
 		Node spaceShip = (Node) assetManager
				.loadModel("assets/Models/reaper2.j3o");
 		spaceShip.setMaterial(mat_brick);
 		rootNode.attachChild(spaceShip);
 
 		Node dummySpaceShip = spaceShip.clone(true);
 		rootNode.attachChild(dummySpaceShip);
 		dummySpaceShip.setLocalTranslation(0, 0, 100);
 
 		spaceControl = new ReaperControl(spaceShip,
 				new BoxCollisionShape(new Vector3f(1,1,1)), 6);
 		spaceShip.addControl(spaceControl);
 		bulletAppState.getPhysicsSpace().add(spaceControl);
 		bulletAppState.getPhysicsSpace().enableDebug(assetManager);
 
 		character = spaceShip;
 	}
 
 	private void initKeys(ControlType controlType) {
 		inputManager.clearMappings();
 
 		List<String> actionKey = new ArrayList<String>();
 		List<String> analogKey = new ArrayList<String>();
 
 		if (controlType == ControlType.SPACECRAFT)
 			disectSettings(settings.getSettingsMap("SpacecraftControls"),
 					actionKey, analogKey);
 		else if (controlType == ControlType.GROUND)
 			disectSettings(settings.getSettingsMap("GroundControls"),
 					actionKey, analogKey);
 
 		if (controlType == ControlType.SPACECRAFT) {
 			SpacecraftListener spacecraftListener = new SpacecraftListener(
 					this, spaceControl);
 			inputManager.addListener(spacecraftListener.actionListener,
 					actionKey.toArray(new String[actionKey.size()]));
 			inputManager.addListener(spacecraftListener.analogListener,
 					analogKey.toArray(new String[analogKey.size()]));
 		} else if (controlType == ControlType.GROUND) {
 			GroundListener groundListener = new GroundListener(this,
 					groundControl);
 			inputManager.addListener(groundListener.actionListener,
 					actionKey.toArray(new String[actionKey.size()]));
 			inputManager.addListener(groundListener.analogListener,
 					analogKey.toArray(new String[analogKey.size()]));
 		}
 		
 		disectSettings(settings.getSettingsMap("UniversalControls"), actionKey, analogKey);
 		UniversalListener universalListener = new UniversalListener(this);
 		inputManager.addListener(universalListener.actionListener,
 				actionKey.toArray(new String[actionKey.size()]));
 		inputManager.addListener(universalListener.analogListener,
 				analogKey.toArray(new String[analogKey.size()]));
 
 	}
 
 	private void disectSettings(HashMap<String, String> controls,
 			List<String> actionKey, List<String> analogKey) {
 
 		for (String key : controls.keySet()) {
 			String binding = controls.get(key);
 			if (binding.charAt(0) == 'k') {
 				inputManager.addMapping(key,
 						new KeyTrigger(Integer.parseInt(binding.substring(1))));
 				actionKey.add(key);
 			} else if (binding.charAt(0) == 'a') {
 				if (binding.charAt(1) == 't')
 					inputManager.addMapping(
 							key,
 							new MouseAxisTrigger(Integer.parseInt(binding
 									.substring(2)), true));
 				if (binding.charAt(1) == 'f')
 					inputManager.addMapping(
 							key,
 							new MouseAxisTrigger(Integer.parseInt(binding
 									.substring(2)), false));
 				analogKey.add(key);
 			} else if (binding.charAt(0) == 'm') {
 				inputManager.addMapping(
 						key,
 						new MouseButtonTrigger(Integer.parseInt(binding
 								.substring(1))));
 				actionKey.add(key);
 			}
 		}
 	}
 
 	@Override
 	public void simpleRender(RenderManager rm) {
 		embi.updateRender();
 	}
 
 	private enum ControlType {
 		SPACECRAFT, GROUND, MACHINE
 	}
 
 }
