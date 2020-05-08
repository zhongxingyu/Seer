 package main.game;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import main.Settings;
 import main.game.entities.controls.ReaperControl;
 import main.game.entities.controls.SpacecraftControl;
 import main.game.entities.userinput.ReaperListener;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseAxisTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.shape.Box;
 import com.jme3.util.SkyFactory;
 
 public class Core extends SimpleApplication {
 	private Settings settings;
 	private BulletAppState bulletAppState;
 
 	public static void main(String[] args) {
 		Core coreapp = new Core();
 		coreapp.start();
 	}
 
 	@Override
 	public void simpleInitApp() {
 		bulletAppState= new BulletAppState();
 		stateManager.attach(bulletAppState);
 		settings = new Settings();
 		rootNode.attachChild(SkyFactory.createSky(assetManager,
 				"assets/Textures/OutputCube2.dds", false));
 
 		Box box1 = new Box(Vector3f.ZERO, 1, 1, 1);
 		Geometry blue = new Geometry("Box", box1);
 		Material mat1 = new Material(assetManager,
 				"Common/MatDefs/Misc/Unshaded.j3md");
 		mat1.setColor("Color", ColorRGBA.Blue);
 		blue.setMaterial(mat1);
 		SpacecraftControl space = new ReaperControl(blue, 6);
 		blue.addControl(space);
 		bulletAppState.getPhysicsSpace().add(space);
 		rootNode.attachChild(blue);
 
 		flyCam.setEnabled(false);
 		ReaperListener.spacecraft = space;
 		initKeys();
 	}
 
 	private void initKeys() {
 		inputManager.clearMappings();
 
 		HashMap<String, String> reaperControls = settings.getSettingsMap("ReaperControls");
 		List<String> actionKey = new ArrayList<String>();
 		List<String> analogKey = new ArrayList<String>();
 		for (String key : reaperControls.keySet()) {
 			String binding = reaperControls.get(key);
 			if (binding.charAt(0) == 'k') {
 				inputManager.addMapping(key, new KeyTrigger(Integer.parseInt(binding.substring(1))));
 				actionKey.add(key);
 			} else if (binding.charAt(0) == 'a') {
 				if(binding.charAt(1) == 't')
 					inputManager.addMapping(key, new MouseAxisTrigger(Integer.parseInt(binding.substring(2)),true));
 				if(binding.charAt(1) == 'f')
 					inputManager.addMapping(key, new MouseAxisTrigger(Integer.parseInt(binding.substring(2)),false));
 				analogKey.add(key);
 			} else if (binding.charAt(0) == 'm') {
 				inputManager.addMapping(key, new MouseButtonTrigger(Integer.parseInt(binding.substring(1))));
 				actionKey.add(key);
 			}
 		}
 
 		ReaperListener reaperListener = new ReaperListener();//Temporarily an object, maybe it will changed to static again 
		inputManager.addListener(reaperListener.actionListener,  actionKey.toArray(new String[actionKey.size()]));
 
		inputManager.addListener(reaperListener.analogListener, analogKey.toArray(new String[actionKey.size()]));
 
 	}
 
 }
