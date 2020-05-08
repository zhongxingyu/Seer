 import java.util.Collection;
 
 import com.jme3.app.Application;
 import com.jme3.app.SimpleApplication;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.PhysicsSpace;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.bullet.objects.PhysicsRigidBody;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.light.AmbientLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Box;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.scene.shape.Sphere.TextureMode;
 import com.jme3.system.AppSettings;
 
 
 public class Jenga extends SimpleApplication {
 
   public static final float BLOCK_HEIGHT = 0.1f; 
 	public static final float BLOCK_WIDTH = 0.25f;
 	public static final float BLOCK_LENGTH = 0.75f;
 
 	private float mHeight = 0.0f;
 	private BulletAppState physicsSpace;
 	private PhysicsSpace space;
 	private Geometry floorGeometry;
 	private boolean pause = true;	
 	
 	private void increaseHeight() {
 		mHeight += BLOCK_HEIGHT + 0.002f;
 	}
 
 	private Geometry createBlock(Vector3f location, int angle) {
 		Material boxMaterial = new Material(getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
 		Box box = new Box(BLOCK_WIDTH, BLOCK_HEIGHT, BLOCK_LENGTH);
 		Geometry boxGeometry = new Geometry("Box", box);
 		boxGeometry.setMaterial(boxMaterial);
 
 		RigidBodyControl bulletControl = new RigidBodyControl(0f);
 
 
 		Quaternion rotation = new Quaternion();
 		rotation.fromAngles(0f, angle * FastMath.DEG_TO_RAD, 0f);
 
 		boxGeometry.addControl(bulletControl);
 		bulletControl.setDamping(0.75f, 0f);
 		bulletControl.setFriction(1f);
 		bulletControl.setMass(1f);
 		bulletControl.setRestitution(0.0f);
 		bulletControl.setSleepingThresholds(1f, 1f);
 		bulletControl.activate();
 		rootNode.attachChild(boxGeometry);
 		getPhysicsSpace().add(bulletControl);
 
 		bulletControl.setPhysicsLocation(location);
 		bulletControl.setPhysicsRotation(rotation);
 
 		return boxGeometry;
 	}
 
 	private void createRow(boolean odd) {
 		if(odd) {
 			createBlock(new Vector3f(-2.01f * BLOCK_WIDTH, mHeight * 1.01f, 0), 0);
 			createBlock(new Vector3f(0, mHeight* 1.01f, 0), 0);
 			createBlock(new Vector3f(+2.01f * BLOCK_WIDTH, mHeight * 1.01f, 0), 0);
 		} else {
 			createBlock(new Vector3f(0, mHeight* 1.01f, -2.01f * BLOCK_WIDTH), 90);
 			createBlock(new Vector3f(0, mHeight* 1.01f,0), 90);
 			createBlock(new Vector3f(0, mHeight* 1.01f, 2.01f * BLOCK_WIDTH), 90);
 		}
 		increaseHeight();
 	}
 
 	private void buildTower() {
		//20 Reihen x 3 Blšcke
 		for(int i = 0; i<=20; i++) {
 			if(i%2==0) {
 				//gerade
 				createRow(false);
 			} else {
 				//ungerade
 				createRow(true);
 			}
 			increaseHeight();
 		}
 	}
 
 	public float getHeight() {
 		return mHeight;
 	}
 
 	public void setHeight(float height) {
 		this.mHeight = height;
 	}
 
 
 	public void simpleInitApp() {
 		space = new PhysicsSpace();
 		physicsSpace = new BulletAppState();
 		stateManager.attach(physicsSpace);
 
 		getPhysicsSpace().enableDebug(assetManager);
 		getPhysicsSpace().setAccuracy(1f/200f);
 
 		getCamera().setLocation(new Vector3f(0, 2, 10));
 
 		AmbientLight light = new AmbientLight();
 		light.setColor(ColorRGBA.LightGray);
 		rootNode.addLight(light);
 
 		Material material = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
 		material.setColor("Color", ColorRGBA.DarkGray);
 
 		Box floorBox = new Box(140, 0.1f, 140);
 		floorGeometry = new Geometry("Boden", floorBox);
 		floorGeometry.setMaterial(material);
 		floorGeometry.setLocalTranslation(0, -0.1f, 0);
 		floorGeometry.addControl(new RigidBodyControl(0));
 
 		rootNode.attachChild(floorGeometry);
 		physicsSpace.getPhysicsSpace().add(floorGeometry);
 
 		addControls(this, rootNode, space);
 		buildTower();
 		
 		
 		Pointer pointer = new Pointer(getAssetManager(), getPhysicsSpace());
 		pointer.setLocalTranslation(2f, 2f, 2f);
 		
 		rootNode.attachChild(pointer);
 		getPhysicsSpace().add(pointer);
 	}
 
 	public void addControls(final Application app, final Node rootNode, final PhysicsSpace space) {
 		ActionListener actionListener = new ActionListener() {
 			@Override
 			public void onAction(String name, boolean isPressed, float tpf) {
 				if(name.equals("activatePhysics")) {
 					Collection<PhysicsRigidBody> bodies = getPhysicsSpace().getRigidBodyList();
 					for (PhysicsRigidBody physicsRigidBody : bodies) {
 						if(!pause) {
 							physicsRigidBody.setFriction(1f);
 							getPhysicsSpace().setAccuracy(1f/200f);
 						} else {
 							physicsRigidBody.setFriction(0.4f);
 							getPhysicsSpace().setAccuracy(1f/60f);
 						}
 						physicsRigidBody.activate();
 					}
 					floorGeometry.getControl(RigidBodyControl.class).setMass(0f);
 				}
 				
 				if (name.equals("shootSphere") && !isPressed) {
 					Sphere sphere = new Sphere(32, 32, 0.4f, true, false);
 					sphere.setTextureMode(TextureMode.Projected);
 					Material sphereMaterial = new Material(app.getAssetManager(), "Common/MatDefs/Misc/Unshaded.j3md");
                     Geometry sphereGeometry = new Geometry("Kugel", sphere);
                     sphereGeometry.setMaterial(sphereMaterial);
                     sphereGeometry.setLocalTranslation(app.getCamera().getLocation());
                     RigidBodyControl bulletControl = new RigidBodyControl(1f);
                     sphereGeometry.addControl(bulletControl);
                     bulletControl.setLinearVelocity(app.getCamera().getDirection().mult(25));
                     sphereGeometry.addControl(bulletControl);
                     rootNode.attachChild(sphereGeometry);
                     getPhysicsSpace().add(bulletControl);
 				}
 			}
 		};
 
 		app.getInputManager().addMapping("activatePhysics", new KeyTrigger(KeyInput.KEY_SPACE));
 		app.getInputManager().addListener(actionListener, "activatePhysics");
 		
 		app.getInputManager().addMapping("shootSphere", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
         app.getInputManager().addListener(actionListener, "shootSphere");
 	}
 
 
 	private PhysicsSpace getPhysicsSpace(){
 		if(physicsSpace == null) {
 			physicsSpace = new BulletAppState();
 		}
 		return physicsSpace.getPhysicsSpace();
 	}
 
 	public static void main(String[] args) {
 		AppSettings settings = new AppSettings(true);
 		settings.setResolution(800, 600);
 		settings.setBitsPerPixel(32);
 		settings.setFullscreen(false);
 
 		Jenga app = new Jenga();
 		app.setSettings(settings);
 		app.setShowSettings(false);
 		app.start();
 	}
 
 
 }
