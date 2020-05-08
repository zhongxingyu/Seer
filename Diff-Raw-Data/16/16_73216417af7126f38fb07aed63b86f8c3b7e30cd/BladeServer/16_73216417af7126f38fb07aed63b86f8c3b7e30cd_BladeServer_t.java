 package mygame;
 
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.Bone;
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.input.ChaseCamera;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector3f;
 import com.jme3.network.connection.Server;
 import com.jme3.network.serializing.Serializer;
 import com.jme3.network.sync.ServerSyncService;
 import com.jme3.network.sync.SyncMessage;
 import com.jme3.renderer.Camera;
 import com.jme3.scene.Node;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.scene.shape.Sphere.TextureMode;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 import java.util.ArrayList;
 import java.util.List;
 import jme3tools.converters.ImageToAwt;
 
 public class BladeServer extends SimpleApplication implements AnalogListener, ActionListener{
 
     private AnimControl control;
     private ChaseCamera chaseCam;
     private Node model;
     private float angle = 0;
     private float[] upArmAngle = {0, 0, 0};
     private float rate = 1;
     private BulletAppState bulletAppState;
     private TerrainQuad terrain;
     Material mat_terrain;
     Material wall_mat;
     Material stone_mat;
     Material floor_mat;
     private RigidBodyControl terrain_phy;
 
     Server server;
     ServerSyncService serverSyncService;
     CharacterEntity serverCharacter;
 
     public static void main(String[] args) {
         BladeServer app = new BladeServer();
         app.start();
 
     }
 
     public void onAnalog(String name, float value, float tpf) {
 
         if (name.equals("twistUpArmLeftCW")) {
             Bone b = control.getSkeleton().getBone("UpArmL");
 
             upArmAngle[2] += (FastMath.HALF_PI / 2f) * tpf * 10f;
 
             Quaternion q = new Quaternion();
             q.fromAngles(0, upArmAngle[2], 0);
 
             b.setUserControl(true);
             b.setUserTransforms(Vector3f.ZERO, q, Vector3f.UNIT_XYZ);
             serverCharacter.setUpArmAngle(upArmAngle);
             serverCharacter.setDelta(1);
         } else if (name.equals("twistUpArmLeftCCW")) {
             Bone b = control.getSkeleton().getBone("UpArmL");
 
             upArmAngle[2] -= (FastMath.HALF_PI / 2f) * tpf * 10f;
 
             Quaternion q = new Quaternion();
             q.fromAngles(0, upArmAngle[2], 0);
 
             b.setUserControl(true);
             b.setUserTransforms(Vector3f.ZERO, q, Vector3f.UNIT_XYZ);
             serverCharacter.setUpArmAngle(upArmAngle);
             serverCharacter.setDelta(-1);
         }
         else{
             serverCharacter.setDelta(0);
         }
 
     }
 
     public void onAction(String name, boolean keyPressed, float tpf) {
         if (name.equals("displayPosition") && keyPressed) {
         }
     }
 
     @Override
     public void simpleInitApp() {
         Serializer.registerClass(SyncMessage.class);
         try {
            server = new Server(BladeMain.port,BladeMain.port);
             server.start();
             serverSyncService=server.getService(ServerSyncService.class);
         }
         catch(Exception e){
             e.printStackTrace();
         }
 
 
         flyCam.setMoveSpeed(50);
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
         initMaterials();
         initTerrain();
         /** 1. Create terrain material and load four textures into it. */
         // Load a model from test_data (OgreXML + material + texture)
         model = (Node) assetManager.loadModel("Models/Fighter.mesh.xml");
         model.scale(1.0f, 1.0f, 1.0f);
         model.rotate(0.0f, FastMath.HALF_PI, 0.0f);
         model.setLocalTranslation(0.0f, 0.0f, 0.0f);
         rootNode.attachChild(model);
 
 
         serverCharacter=new CharacterEntity(model);
         serverSyncService.addNpc(serverCharacter);
 
         serverSyncService.setNetworkSimulationParams(0.0f, 50);
 
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
         rootNode.addLight(sun);
 
         DirectionalLight sun2 = new DirectionalLight();
         sun2.setDirection(new Vector3f(0.1f, 0.7f, 1.0f));
         rootNode.addLight(sun2);
 
         control = model.getControl(AnimControl.class);
 
         flyCam.setEnabled(false);
 
         chaseCam = new ChaseCamera(cam, model, inputManager);
         chaseCam.setSmoothMotion(true);
         chaseCam.setDefaultVerticalRotation(FastMath.HALF_PI / 4f);
         chaseCam.setLookAtOffset(new Vector3f(0.0f, 4.0f, 0.0f));
         registerInput();
     }
 
     @Override
     public void simpleUpdate(float tpf){
         serverSyncService.update(tpf);
     }
 
     public void registerInput() {
         inputManager.addMapping("twistUpArmLeftCCW", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
         inputManager.addMapping("twistUpArmLeftCW", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
         inputManager.addMapping("displayPosition", new KeyTrigger(keyInput.KEY_P));
         inputManager.addListener(this, "twistUpArmLeftCCW", "twistUpArmLeftCW");
         inputManager.addListener(this, "displayPosition");
     }
 
     public void initTerrain() {
         mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
 
         /** 1.1) Add ALPHA map (for red-blue-green coded splat textures) */
         mat_terrain.setTexture("m_Alpha", assetManager.loadTexture("Textures/alpha.png"));
 
         /** 1.2) Add GRASS texture into the red layer (m_Tex1). */
         Texture grass = assetManager.loadTexture("Textures/grass.jpg");
         grass.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_Tex1", grass);
         mat_terrain.setFloat("m_Tex1Scale", 64f);
 
         /** 1.3) Add DIRT texture into the green layer (m_Tex2) */
         Texture dirt = assetManager.loadTexture("Textures/TiZeta_SmlssWood1.jpg");
         dirt.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_Tex2", dirt);
         mat_terrain.setFloat("m_Tex2Scale", 32f);
 
         /** 1.4) Add ROAD texture into the blue layer (m_Tex3) */
         Texture rock = assetManager.loadTexture("Textures/TiZeta_cem1.jpg");
         rock.setWrap(WrapMode.Repeat);
         mat_terrain.setTexture("m_Tex3", rock);
         mat_terrain.setFloat("m_Tex3Scale", 128f);
 
         /** 2. Create the height map */
         AbstractHeightMap heightmap = null;
         Texture heightMapImage = assetManager.loadTexture("Textures/grayscale.png");
         heightmap = new ImageBasedHeightMap(
                 ImageToAwt.convert(heightMapImage.getImage(), false, true, 0));
         heightmap.load();
 
         /** 3. We have prepared material and heightmap. Now we create the actual terrain:
          * 3.1) We create a TerrainQuad and name it "my terrain".
          * 3.2) A good value for terrain tiles is 64x64 -- so we supply 64+1=65.
          * 3.3) We prepared a heightmap of size 512x512 -- so we supply 512+1=513.
          * 3.4) As LOD step scale we supply Vector3f(1,1,1).
          * 3.5) At last, we supply the prepared heightmap itself.
          */
         terrain = new TerrainQuad("my terrain", 65, 513, heightmap.getHeightMap());
 
         /** 4. We give the terrain its material, position & scale it, and attach it. */
         terrain.setMaterial(mat_terrain);
         terrain.setLocalTranslation(0, -100, 0);
         terrain.setLocalScale(2f, 1f, 2f);
         rootNode.attachChild(terrain);
 
         /** 5. The LOD (level of detail) depends on were the camera is: */
         List<Camera> cameras = new ArrayList<Camera>();
         cameras.add(getCamera());
         TerrainLodControl control = new TerrainLodControl(terrain, cameras);
         terrain_phy = new RigidBodyControl(0.0f);
         terrain.addControl(terrain_phy);
         bulletAppState.getPhysicsSpace().add(terrain_phy);
 
 
     }
 
     public void initMaterials() {
         wall_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
         TextureKey key = new TextureKey("Textures/road.jpg");
         key.setGenerateMips(true);
         Texture tex = assetManager.loadTexture(key);
         wall_mat.setTexture("ColorMap", tex);
 
         stone_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
         TextureKey key2 = new TextureKey("Textures/road.jpg");
         key2.setGenerateMips(true);
 
         Texture tex2 = assetManager.loadTexture(key2);
         stone_mat.setTexture("ColorMap", tex2);
 
         floor_mat = new Material(assetManager, "Common/MatDefs/Misc/SimpleTextured.j3md");
         TextureKey key3 = new TextureKey("Textures/grass.jpg");
         key3.setGenerateMips(true);
         Texture tex3 = assetManager.loadTexture(key3);
         tex3.setWrap(WrapMode.Repeat);
         floor_mat.setTexture("ColorMap", tex3);
 
     }
 }
