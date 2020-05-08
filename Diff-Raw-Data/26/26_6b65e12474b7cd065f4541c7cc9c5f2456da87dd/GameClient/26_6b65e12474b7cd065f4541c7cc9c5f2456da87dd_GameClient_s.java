 package game;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.material.Material;
 import com.jme3.renderer.Camera;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.geomipmap.lodcalc.DistanceLodCalculator;
 import com.jme3.terrain.heightmap.HillHeightMap; // for exercise 2
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 import java.util.ArrayList;
 import java.util.List;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Spatial;
 import com.jme3.font.BitmapText;
 import com.jme3.light.DirectionalLight;
 import com.jme3.math.Vector3f;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.scene.shape.Box;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 import com.jme3.util.SkyFactory;
 import java.util.Map;
 import java.util.HashMap;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import rice.environment.Environment;
 import rice.pastry.NodeIdFactory;
 import rice.pastry.PastryNode;
 import rice.pastry.PastryNodeFactory;
 import rice.pastry.socket.SocketPastryNodeFactory;
 import rice.pastry.standard.RandomNodeIdFactory;
 import java.util.Scanner;
 import com.jme3.scene.Node;
 import com.jme3.scene.CameraNode;
 import com.jme3.scene.control.CameraControl.ControlDirection;
 import rice.p2p.commonapi.NodeHandle;
 import com.jme3.input.ChaseCamera;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.MouseAxisTrigger;
 import com.jme3.math.Quaternion;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.builder.ScreenBuilder;
 import de.lessvoid.nifty.builder.LayerBuilder;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.screen.DefaultScreenController;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 import java.util.Iterator;
 
 /** Sample 10 - How to create fast-rendering terrains from heightmaps,
 and how to use texture splatting to make the terrain look good.  */
 public class GameClient extends SimpleApplication implements AnalogListener, ActionListener, ScreenController
 {
   Boolean isRunning = true;  
   private TerrainQuad terrain;
   Material mat_terrain;
   BitmapText hudText;
   DirectionalLight sun;
   CameraNode camNode;
   float camHeight = 30;
   
   // player variables
   Spatial ninja;
   Node playerNode;
   boolean positionChanged=false;
   Vector3f direction = new Vector3f();
   Map<NodeHandle, Spatial> players = new HashMap<NodeHandle, Spatial>();
   Map<NodeHandle, PlayerData> playerLocations = new HashMap<NodeHandle, PlayerData>();
   
   //Game node
   GameNode gamenode;
   boolean connected = false;
   
   private Nifty nifty;
 
   public static void main(String[] args)
   {
     GameClient game = new GameClient();
     game.setDisplayStatView(false);
     game.start();
   }
 
   @Override
   public void simpleInitApp()
   {
     flyCam.setMoveSpeed(100);
 
     // 1. create terrain material and load four textures into it.
     mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
     mat_terrain.setTexture("Alpha", assetManager.loadTexture(
       "Textures/Terrain/splat/alphamap.png"));
     Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
     grass.setWrap(WrapMode.Repeat);
     mat_terrain.setTexture("Tex1", grass);
     mat_terrain.setFloat("Tex1Scale", 64f);
     Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
     dirt.setWrap(WrapMode.Repeat);
     mat_terrain.setTexture("Tex2", dirt);
     mat_terrain.setFloat("Tex2Scale", 32f);
     Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
     rock.setWrap(WrapMode.Repeat);
     mat_terrain.setTexture("Tex3", rock);
     mat_terrain.setFloat("Tex3Scale", 128f);
 
     // 2. create heightmap
     AbstractHeightMap heightmap = null;
     //Texture heightMapImage = assetManager.loadTexture("Textures/map_test.png");
     Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
     heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
     heightmap.load();
 
     // 3. create terrain
     int patchSize = 65;
     terrain = new TerrainQuad("my terrain", patchSize, 513, heightmap.getHeightMap());
     terrain.setMaterial(mat_terrain);
     terrain.setLocalTranslation(0, -150, 0);
     terrain.setLocalScale(2f, 0.5f, 2f);
     rootNode.attachChild(terrain);
 
     // 4. LOD depends on the position of the camera
     TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
     terrain.addControl(control);
     
     // load the player model
     ninja = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
     ninja.scale(0.04f, 0.04f, 0.04f);
     ninja.rotate(0.0f, -3.0f, 0.0f);
 
     playerNode = new Node("playerNode");
     playerNode.attachChild(ninja);
     rootNode.attachChild(playerNode);
     
     flyCam.setEnabled(false);
     camNode = new CameraNode("Camera Node", cam);
     camNode.setControlDir(ControlDirection.SpatialToCamera);
     playerNode.attachChild(camNode);
     camNode.setLocalTranslation(new Vector3f(0, camHeight, -30));
     camNode.lookAt(ninja.getLocalTranslation().add(new Vector3f(0, 15, 0)), Vector3f.UNIT_Y);
     
     // directional light
     sun = new DirectionalLight();
     sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
     rootNode.addLight(sun);
     
     // a skybox
     rootNode.attachChild(SkyFactory.createSky(assetManager,
       "Textures/Sky/Bright/BrightSky.dds", false));
     
     // debug text
     hudText = new BitmapText(guiFont, false);          
     hudText.setSize(guiFont.getCharSet().getRenderedSize());
     hudText.setColor(ColorRGBA.White);
     hudText.setText("");
     hudText.setLocalTranslation(300, hudText.getLineHeight(), 0);
     guiNode.attachChild(hudText);
     
     registerInput();
     
     NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
       assetManager,
       inputManager,
       audioRenderer,
       guiViewPort);
     
     nifty = niftyDisplay.getNifty();
     nifty.fromXml("Interface/interface.xml", "start", this);
     guiViewPort.addProcessor(niftyDisplay);
 
     try
     {
       connect(6001, "GEOFF-LENOVO", 8000);
       connected = true;
     }
     catch(Exception e)
     {
       connected = false;
     }
   }
   
   @Override
   public void simpleUpdate(float tpf)
   {
     Iterator it = playerLocations.entrySet().iterator();
     while(it.hasNext())
     {
       Map.Entry pairs = (Map.Entry)it.next();
      players.get(pairs.getKey()).setLocalTranslation(((PlayerData)pairs.getValue()).position);
      players.get(pairs.getKey()).setLocalRotation(((PlayerData)pairs.getValue()).rotation);
       it.remove();
     }
   }
   
   int numPlayers = 0;
   public void updatePlayer(NodeHandle id, Vector3f location, Quaternion rotation)
   {
       if(!players.containsKey(id))
       {
           numPlayers++;
           Spatial ninja1 = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
           ninja1.scale(0.04f, 0.04f, 0.04f);
           ninja1.rotate(0.0f, -3.0f, 0.0f);
           rootNode.attachChild(ninja1);
           players.put(id, ninja1);
       }
       
       PlayerData data = new PlayerData();
       data.position = location;
       data.rotation = rotation;
       playerLocations.put(id, data);
   }
   
   public void removePlayer(NodeHandle id)
   {
       if(players.containsKey(id))
       {
           players.remove(id);
       }
   }
   
   public Vector3f getPlayerPosition()
   {
       return playerNode.getLocalTranslation();
   }
   
   public Quaternion getPlayerRotation()
   {
     return playerNode.getLocalRotation();
   }
   
   // Custom Keybinding: Map named actions to inputs.
   private void registerInput()
   {
     inputManager.addMapping("moveForward", new KeyTrigger(keyInput.KEY_UP), new KeyTrigger(keyInput.KEY_W));
     inputManager.addMapping("moveBackward", new KeyTrigger(keyInput.KEY_DOWN), new KeyTrigger(keyInput.KEY_S));
     inputManager.addMapping("moveRight", new KeyTrigger(keyInput.KEY_RIGHT), new KeyTrigger(keyInput.KEY_D));
     inputManager.addMapping("moveLeft", new KeyTrigger(keyInput.KEY_LEFT), new KeyTrigger(keyInput.KEY_A));
     inputManager.addMapping("toggleRotate", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
     inputManager.addMapping("rotateRight", new MouseAxisTrigger(MouseInput.AXIS_X, true));
     inputManager.addMapping("rotateLeft", new MouseAxisTrigger(MouseInput.AXIS_X, false));
     inputManager.addMapping("rotateUp", new MouseAxisTrigger(MouseInput.AXIS_Y, true));
     inputManager.addMapping("rotateDown", new MouseAxisTrigger(MouseInput.AXIS_Y, false));
     inputManager.addListener(this, "moveForward", "moveBackward", "moveRight", "moveLeft");
     inputManager.addListener(this, "rotateRight", "rotateLeft", "rotateUp", "rotateDown", "toggleRotate");
   }
  
   public void onAnalog(String name, float value, float tpf)
   {
     direction.set(cam.getDirection()).normalizeLocal();
       
     float moveSpeed = 50f;
     float rotateSpeed = 8f;
     float rotateXSpeed = 30f;
     
     if (name.equals("moveForward")) {
       direction.multLocal(tpf * moveSpeed);
       playerNode.move(direction);       
       positionChanged = true;
     }
     if (name.equals("moveBackward")) {
       direction.multLocal(-1 * tpf * moveSpeed);
       playerNode.move(direction);     
       positionChanged = true;
     }
     if (name.equals("moveRight")) {
       direction.crossLocal(Vector3f.UNIT_Y).multLocal(tpf * moveSpeed);
       playerNode.move(direction);
       positionChanged = true;
     }
     if (name.equals("moveLeft")) {
       direction.crossLocal(Vector3f.UNIT_Y).multLocal(-1 * tpf * moveSpeed);
       playerNode.move(direction);       
       positionChanged = true;
     }
     if (name.equals("rotateRight")) {
       playerNode.rotate(0, tpf * rotateSpeed, 0);
       positionChanged = true;
     }
     if (name.equals("rotateLeft")) {
       playerNode.rotate(0, -1 * tpf * rotateSpeed, 0);
       positionChanged = true;
     }
     if (name.equals("rotateUp")) {
       //camHeight += rotateXSpeed * tpf;
       //camNode.setLocalTranslation(new Vector3f(0, camHeight, -30));
       //camNode.lookAt();
     }
     if (name.equals("rotateDown")) {
       //camHeight -= rotateXSpeed * tpf;
       //camNode.setLocalTranslation(new Vector3f(0, camHeight, -30));
     }
     
     Vector3f pt = playerNode.getLocalTranslation();
     Vector3f ts = terrain.getLocalScale();
     Vector3f tt = terrain.getLocalTranslation();
     float h = terrain.getHeightmapHeight(new Vector2f(pt.x, pt.z)) * ts.y + tt.y;
     playerNode.setLocalTranslation(pt.x, h, pt.z);
   }
 
   public void onAction(String name, boolean keyPressed, float tpf)
   {
     if (name.equals("displayPosition") && keyPressed)
     {
       if(connected)
       {
         playerNode.move(10, 10, 10);
       }
     }
   }
   
   public void connect(int bind, String address, int boot) throws Exception
   {
     // Loads pastry configurations
     Environment env = new Environment();
     
     // disable the UPnP setting (in case you are testing this on a NATted LAN)
     env.getParameters().setString("nat_search_policy","never");
 
     try
     {
       // the port to use locally
       int bindport = bind;
       // build the bootaddress from the command line args
       InetAddress bootaddr = InetAddress.getByName(address);
       int bootport = boot;
       InetSocketAddress bootaddress = new InetSocketAddress(bootaddr, bootport);
       
       // launch our node!
       gamenode = new GameNode(bindport, bootaddress, env, this);
     }
     catch (Exception e)
     {
       System.out.println("Shit didn't work, did you pass the right args?");
       throw e;
     }
   }
 
   public void bind(Nifty nifty, Screen screen) {
       System.out.println("bind( " + screen.getScreenId() + ")");
   }
 
   public void onStartScreen() {
       System.out.println("onStartScreen");
   }
 
   public void onEndScreen() {
       System.out.println("onEndScreen");
   }
 
   public void quit(){
       nifty.gotoScreen("end");
   }
 }
