 
 
 package mygame;
 
 import com.jme3.animation.AnimControl;
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.input.ChaseCamera;
 import com.jme3.input.MouseInput;
 import com.jme3.input.RawInputListener;
 import com.jme3.input.event.JoyAxisEvent;
 import com.jme3.input.event.JoyButtonEvent;
 import com.jme3.input.event.KeyInputEvent;
 import com.jme3.input.event.MouseButtonEvent;
 import com.jme3.input.event.MouseMotionEvent;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.network.connection.Client;
 import com.jme3.network.events.MessageListener;
 import com.jme3.network.message.Message;
 import com.jme3.network.serializing.Serializer;
 import com.jme3.network.sync.ClientSyncService;
 import com.jme3.network.sync.EntityFactory;
 import com.jme3.network.sync.SyncEntity;
 import com.jme3.network.sync.SyncMessage;
 import com.jme3.renderer.Camera;
 import com.jme3.scene.Node;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import jme3tools.converters.ImageToAwt;
 
 /**
  *
  * @author blah
  */
 public class BladeClient extends SimpleApplication implements EntityFactory, MessageListener, RawInputListener{
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
 
     Client client;
     ClientCharacterEntity clientCharacter;
     ClientSyncService clientSyncService;
     boolean clientSet=false;
 
 
 
     public SyncEntity createEntity(Class<? extends SyncEntity> entityType){
         clientCharacter=new ClientCharacterEntity(model);
         rootNode.attachChild(model);
         clientSet=true;
         return clientCharacter;
     }
 
     public static void main(String[] args) {
         BladeClient app = new BladeClient();
         app.start();
     }
 
     @Override
     public void simpleInitApp() {
         Serializer.registerClass(SyncMessage.class);
         InputMessages.registerInputClasses();
 
         
         flyCam.setMoveSpeed(50);
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
         initMaterials();
         initTerrain();
 
         model = (Node) assetManager.loadModel("Models/Fighter.mesh.xml");
         model.scale(1.0f, 1.0f, 1.0f);
         model.rotate(0.0f, FastMath.HALF_PI, 0.0f);
         model.setLocalTranslation(0.0f, 0.0f, 0.0f);
 
         try{
            client=new Client(BladeMain.serverMap.get("evan"),BladeMain.port,BladeMain.port);
             
             client.start();
     //        Thread.sleep(100);
         }
         catch(Exception e){
             e.printStackTrace();
         }
 
         clientSyncService=client.getService(ClientSyncService.class);
         clientSyncService.setEntityFactory(this);
         
         try {
             Thread.sleep(1000);
         } catch (InterruptedException ex) {
             Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
         }
 
         InputMessages.addInputMessageListeners(client, this);
 
 
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
         rootNode.addLight(sun);
 
         DirectionalLight sun2 = new DirectionalLight();
         sun2.setDirection(new Vector3f(0.1f, 0.7f, 1.0f));
         rootNode.addLight(sun2);
 
 
         flyCam.setEnabled(false);
 
         chaseCam = new ChaseCamera(cam, model, inputManager);
         chaseCam.setSmoothMotion(true);
         chaseCam.setDefaultVerticalRotation(FastMath.HALF_PI / 4f);
         chaseCam.setLookAtOffset(new Vector3f(0.0f, 4.0f, 0.0f));
         registerInput();
     }
 
     @Override
     public void simpleUpdate(float tpf){
         clientSyncService.update(tpf);
         if(clientSet){
             clientCharacter.onLocalUpdate();
         }
     }
 
     public void registerInput() {
         inputManager.addRawInputListener(this);
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
 
     public void messageReceived(Message message) {
         System.out.println("Message Received");
     }
 
     public void messageSent(Message message) {
         System.out.println("Message Sent");
     }
 
     public void objectReceived(Object object) {
      //   throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void objectSent(Object object) {
     //    throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void beginInput() {
     //    throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void endInput() {
      //   throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void onJoyAxisEvent(JoyAxisEvent evt) {
      //   throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void onJoyButtonEvent(JoyButtonEvent evt) {
       //  throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void onMouseMotionEvent(MouseMotionEvent evt) {
       //  throw new UnsupportedOperationException("Not supported yet.");
     }
 
     private boolean prevPressed = false;
 
     public void onMouseButtonEvent(MouseButtonEvent evt) {
         if (evt.isPressed() != prevPressed) {
             if (evt.isPressed()) {
                 if (evt.getButtonIndex() == MouseInput.BUTTON_LEFT) {
                     System.out.println("left mouse button");
                     try {
                         client.send(new InputMessages.RotateArmCC());
                     } catch (IOException ex) {
                         Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 } else if (evt.getButtonIndex() == MouseInput.BUTTON_RIGHT) {
                     System.out.println("right mouse button");
                     try {
                         client.send(new InputMessages.RotateArmC());
                     } catch (IOException ex) {
                         Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                     }
                 }
             }
             else{
                 System.out.println("Releasing mouse button");
                 try {
                     client.send(new InputMessages.StopRotateTwist());
                 } catch (IOException ex) {
                     Logger.getLogger(BladeClient.class.getName()).log(Level.SEVERE, null, ex);
                 }
             }
         }
 
         prevPressed=evt.isPressed();
     }
 
     public void onKeyEvent(KeyInputEvent evt) {
        // throw new UnsupportedOperationException("Not supported yet.");
     }
 /*
     @Override
     public void destroy(){
         try {
             client.disconnect();
         } catch (IOException ex) {
             Logger.getLogger(BladeServer.class.getName()).log(Level.SEVERE, null, ex);
         }
     }*/
 }
