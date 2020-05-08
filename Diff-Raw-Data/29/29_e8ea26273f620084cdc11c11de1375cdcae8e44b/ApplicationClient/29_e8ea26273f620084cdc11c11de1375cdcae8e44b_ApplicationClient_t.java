 package org.sanchome.shy.engine;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.sanchome.shy.engine.entity.BlenderTree;
 import org.sanchome.shy.engine.entity.Crate;
 import org.sanchome.shy.engine.entity.Fence;
 import org.sanchome.shy.engine.entity.IEntity;
 import org.sanchome.shy.engine.entity.IUpdatable;
 import org.sanchome.shy.engine.entity.Sheep;
 import org.sanchome.shy.engine.entity.tool.FenceFactory;
 import org.sanchome.shy.engine.player.LocalPlayer;
 import org.sanchome.shy.engine.world.HelloWorld;
 import org.sanchome.shy.engine.world.IWorld;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.BulletAppState.ThreadingType;
 import com.jme3.font.BitmapText;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseAxisTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.light.AmbientLight;
 import com.jme3.light.DirectionalLight;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.Caps;
 import com.jme3.renderer.queue.RenderQueue.ShadowMode;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.shadow.BasicShadowRenderer;
 import com.jme3.shadow.PssmShadowRenderer;
 import com.jme3.shadow.PssmShadowRenderer.FilterMode;
 import com.jme3.util.SkyFactory;
 
 public class ApplicationClient extends SimpleApplication implements ActionListener {
 
 	private static final Logger logger = Logger.getLogger(ApplicationClient.class.getName());
 
 	private BulletAppState bulletAppState;
 	private boolean physicDebugEnabled = false;
 	private static IWorld world;
 	private Node mobilesNode;
 	LocalPlayer localPlayer;
 	Map<IEntity, Integer> entitiesToStabilizePhysicaly = new HashMap<IEntity, Integer>();
 	List<IUpdatable> uptatableEntities = new ArrayList<IUpdatable>();
 
 	static public IWorld getCurrentWorld() {
 		return world;
 	}
 
 	@Override
 	public void simpleInitApp() {
 		logger.info("Starting shy client ...");
 		
 		// Removes unused default mapping
 		for (String builtinMapping : 
 			new String[] {
 				"FLYCAM_RotateDrag",
 				"FLYCAM_Forward",
 				"FLYCAM_Backward",
 				"FLYCAM_Rise",
 				"FLYCAM_Lower",
 				"FLYCAM_StrafeLeft",
 				"FLYCAM_StrafeRight"
 			}) {
 			inputManager.deleteMapping(builtinMapping);
 
 		}
 		
 		// Setups system input
 		inputManager.addMapping("Enable_Physic_Debug", new KeyTrigger(KeyInput.KEY_F6));
 		
 		inputManager.addListener(this, "Enable_Physic_Debug");
 		
 		// Setups localPlayer input
 		inputManager.addMapping("Forward", new KeyTrigger(KeyInput.KEY_Z));
 		inputManager.addMapping("Backward", new KeyTrigger(KeyInput.KEY_S));
 		inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_Q));
 		inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
 		inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
 		inputManager.addMapping("Run", new KeyTrigger(KeyInput.KEY_LSHIFT));
 		
 		inputManager.addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
 		inputManager.addMapping("FootControl_Lock", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
 		
 		inputManager.addMapping("FootControl_Left", new MouseAxisTrigger(MouseInput.AXIS_X, true));
 		inputManager.addMapping("FootControl_Right", new MouseAxisTrigger(MouseInput.AXIS_X, false));
 		inputManager.addMapping("FootControl_Up", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
 		inputManager.addMapping("FootControl_Down", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
 
 		// Speed up the cam a bit
 		flyCam.setMoveSpeed(30);
 
 		/** Set up Physics */
 		bulletAppState = new BulletAppState();
 		bulletAppState.setThreadingType(ThreadingType.PARALLEL);
 		// bulletAppState.setBroadphaseType(BroadphaseType.SIMPLE);
 		bulletAppState.setWorldMin(new Vector3f(-256.0f, -200.0f, -256.0f));
 		bulletAppState.setWorldMax(new Vector3f(256.0f, 200.0f, 256.0f));
 		stateManager.attach(bulletAppState);
 		//bulletAppState.getPhysicsSpace().setMaxSubSteps(100);
 		//bulletAppState.getPhysicsSpace().enableDebug(assetManager);
 
 		// Instanciate the chossen terrain
 		world = new HelloWorld();
 		world.init(assetManager, cam, rootNode, bulletAppState);
 
 		mobilesNode = new Node("mobilesNode");
 		rootNode.attachChild(mobilesNode);
 
 		// Local player
 		localPlayer = new LocalPlayer();
 		localPlayer.init(assetManager, cam, mobilesNode, bulletAppState);
 		inputManager.addListener(localPlayer,
 				"Forward", "Backward", "Left", "Right", "Jump", "Run", "Shoot", "FootControl_Lock",
 				"FootControl_Left",
 				"FootControl_Right",
 				"FootControl_Up",
 				"FootControl_Down");
 
 		// Crates
 		for (int i = 0; i < UserSettings.CRATE_NUMBER; i++) {
 			Crate crate = new Crate();
 			crate.init(assetManager, cam, mobilesNode, bulletAppState);
 			// entitiesToStabilizePhysicaly.put(crate, 0);
 		}
 
 		// Trees
 		for (int i = 0; i < UserSettings.TREE_NUMBER; i++) {
 			BlenderTree tree = new BlenderTree();
 			tree.init(assetManager, cam, mobilesNode, bulletAppState);
 			// entitiesToStabilizePhysicaly.put(tree, 0);
 		}
 
 		// Sheeps
 		for (int i = 0; i < UserSettings.SHEEP_NUMBER; i++) {
 			Sheep sheep = new Sheep();
 			sheep.init(assetManager, cam, mobilesNode, bulletAppState);
 			// entitiesToStabilizePhysicaly.put(sheep, 0);
 			uptatableEntities.add(sheep);
 		}
 
 		// Fences
 		for(int i = 0; i < UserSettings.FENCE_NUMBER; i++) {
 			Fence fence = new Fence();
 			fence.init(assetManager, cam, mobilesNode, bulletAppState);
 			fence.setPosition(getCurrentWorld().getRandomPosition());
 		}
 		
 		// Draw a line of fences
 		FenceFactory.getPencil(assetManager, cam, mobilesNode, bulletAppState).drawLine(256.0f, 30.0f, 50.0f, -256.0f);
 		FenceFactory.getPencil(assetManager, cam, mobilesNode, bulletAppState).drawRectangle(-200.0f, -200.0f, 50.0f, 50.0f);
		FenceFactory.getPencil(assetManager, cam, mobilesNode, bulletAppState).drawCircle(-128.0f, 128.0f, 70.0f);
		//FenceFactory.getPencil(assetManager, cam, mobilesNode, bulletAppState).fenceAngleTest();
 		
 		// Light
 		AmbientLight al = new AmbientLight();
 		al.setColor(ColorRGBA.White.mult(0.5f));
 		rootNode.addLight(al);
 
 		DirectionalLight sun = new DirectionalLight();
 		sun.setColor(ColorRGBA.White);
 		sun.setDirection(new Vector3f(-0.7f, -1.0f, -1.0f).normalizeLocal());
 		rootNode.addLight(sun);
 
 		// Sky
 		Spatial sky;
 		if (renderer.getCaps().contains(Caps.FloatTexture)) {
 			sky = SkyFactory.createSky(assetManager, "Textures/Sky/Bright/BrightSky.dds", false);
 		} else {
 			// TODO : find a better sky..
 			sky = SkyFactory.createSky(assetManager, "textures/alternateSky.jpg", true);
 		}
 		rootNode.attachChild(sky);
 
 		// Shadow
 		rootNode.setShadowMode(ShadowMode.Off);
 		
 		if (UserSettings.SHADOW_MODE == org.sanchome.shy.engine.UserSettings.ShadowMode.BASIC) {
 			BasicShadowRenderer bsr = new BasicShadowRenderer(assetManager, UserSettings.SHADOW_MODE_DEFINITION );
 			bsr.setDirection(sun.getDirection());
 			viewPort.addProcessor(bsr);
 		}
 		else if (UserSettings.SHADOW_MODE == org.sanchome.shy.engine.UserSettings.ShadowMode.PSSM) {
 			PssmShadowRenderer pssmRenderer = new PssmShadowRenderer(assetManager, UserSettings.SHADOW_MODE_DEFINITION, 4);
 			pssmRenderer.setDirection(sun.getDirection());
 			pssmRenderer.setShadowIntensity(0.5f);
 			pssmRenderer.setFilterMode(FilterMode.Nearest);
 			//pssmRenderer.setShadowZExtend(100.0f);
 			viewPort.addProcessor(pssmRenderer);
 		}
 
 		initCrossHairs();
 	}
 
 	public void onAction(String name, boolean isPressed, float tpf) {
 		if ("Enable_Physic_Debug".equals(name)) {
 			if (isPressed) {
 				physicDebugEnabled = !physicDebugEnabled;
 				if (physicDebugEnabled)
 					bulletAppState.getPhysicsSpace().enableDebug(assetManager);
 				else
 					bulletAppState.getPhysicsSpace().disableDebug();
 			}
 		}
 	}
 	
 	protected void initCrossHairs() {
 		//guiNode.detachAllChildren();
 		guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
 		BitmapText ch = new BitmapText(guiFont, false);
 		ch.setSize(guiFont.getCharSet().getRenderedSize() * 1.0f);
 		ch.setText("+"); // fake crosshairs :)
 		ch.setLocalTranslation(
 				// center
 				settings.getWidth() / 2.0f - guiFont.getCharSet().getRenderedSize() / 3.0f * 1.0f,
 				settings.getHeight() / 2.0f + ch.getLineHeight() / 2.0f, 0.0f);
 		guiNode.attachChild(ch);
 	}
 
 	@Override
 	public void simpleUpdate(float tpf) {
 		for (IUpdatable updatable : uptatableEntities)
 			updatable.update(tpf);
 
 		localPlayer.simpleUpdate(tpf);
 	}
 }
