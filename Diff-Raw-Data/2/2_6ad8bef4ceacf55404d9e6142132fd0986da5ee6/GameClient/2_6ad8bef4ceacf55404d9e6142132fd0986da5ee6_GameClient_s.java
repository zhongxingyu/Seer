 package game;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.font.BitmapText;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseAxisTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Quaternion;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.renderer.RenderManager;
 import com.jme3.scene.CameraNode;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.control.CameraControl;
 import com.jme3.scene.shape.Box;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.util.SkyFactory;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.elements.Element;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import rice.p2p.commonapi.Id;
 import rice.p2p.commonapi.NodeHandle;
 
 /**
  * 
  * @author JP
  */
 public class GameClient extends SimpleApplication implements AnalogListener
 {
     static GameClient globalMain;
     private boolean isStarted = false;
     private boolean isInMenu = false;
     private String playerName = "";
     
     Nifty nifty;
     Element gameMenu;
     
     public static void main(String[] args)
     {
         Logger.getLogger("").setLevel(Level.SEVERE);
         GameClient app = new GameClient();
         globalMain = app;
         app.start();
     }
 
     // =========================================================================
     // SimpleGame implementation
     // =========================================================================
     
     StartScreenController startController;
     GameScreenController hudController;
     GameModel model;
     
     @Override
     public void simpleInitApp()
     {   
         NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
                 assetManager,
                 inputManager,
                 audioRenderer,
                 guiViewPort);
         nifty = niftyDisplay.getNifty();
         nifty.addControls();
         nifty.fromXml("Interface/TestInterface.xml", "start");
         startController = (StartScreenController)nifty.getScreen("start").getScreenController();
         hudController = (GameScreenController)nifty.getScreen("hud").getScreenController();
         stateManager.attach(startController);
         stateManager.attach(hudController);
         guiViewPort.addProcessor(niftyDisplay);
         
         
         setDisplayStatView(false);
         setDisplayFps(false);
 
         flyCam.setDragToRotate(true);
 
         initMenuKeys();
         
         model = new GameModel(this);
     }
     
     int timeToPlayCount = 0;
     
     @Override
     public void simpleUpdate(float tpf)
     {        
         model.update();
         
         // handle the logic to display the load screen. this handles the
         // transition between the start screen and the game screen.
         if(timeToPlay && timeToPlayCount < 2)
         {
             if(timeToPlayCount == 0)
             {
                 nifty.gotoScreen("load");
             }
             else if(timeToPlayCount == 1)
             {
                 loadScene();
 
                 model.connect(targetIP, targetPort, thisPort);
 
                 nifty.gotoScreen("hud");
 
                 isStarted = true;
             }
             timeToPlayCount++;
         }
     }
 
     @Override
     public void simpleRender(RenderManager rm)
     {}
 
     // =========================================================================
     // Game functionality
     // =========================================================================
     
     BitmapText hudText;
     boolean useTerrain = false;
 
     public void loadScene()
     {        
         flyCam.setMoveSpeed(100);
         createTerrain();
         createLocalPlayer();
         createCamera();
         createLight();
         createSkybox();
         initGameKeys();
         
         setPlayerYPos();
         
         hudText = new BitmapText(guiFont, false);          
         hudText.setSize(guiFont.getCharSet().getRenderedSize());
         hudText.setColor(ColorRGBA.Black);
         hudText.setText("[player position]");
         hudText.setLocalTranslation(450, 29, 0);
         guiNode.attachChild(hudText);
     }
     
     TerrainQuad terrain;
     Material mat_terrain;
     public void createTerrain()
     {
         if(!useTerrain)
         {
             for(int x=0; x<11; x++)
             {
                 for(int y=0; y<11; y++)
                 {
                     float xLoc = (x*100-517)+40;
                     float yLoc = (y*100-517)+40;
                     
                     ColorRGBA color = new ColorRGBA(0, 164.f/255.f, 239.f/255.f, 1.f);
                     if(x % 2 == 0 && y % 2 == 0 || x % 2 == 1 && y % 2 == 1)
                     {
                         color = new ColorRGBA(127.f/255.f, 186.f/255.f, 0, 1.f);
                     }
 
                     Box b = new Box(new Vector3f(xLoc, -1.f, yLoc), 50, 1, 50);
                     Geometry geom = new Geometry("Box", b);
                     Material mat = new Material(
                             assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                     mat.setColor("Color", color);
                     geom.setMaterial(mat);
                     rootNode.attachChild(geom);
                 }
             }
         }
         
         // 1. create terrain material and load four textures into it.
         mat_terrain = new Material(assetManager, "Common/MatDefs/Terrain/Terrain.j3md");
         mat_terrain.setTexture("Alpha", assetManager.loadTexture(
           "Textures/Terrain/splat/alphamap.png"));
         Texture grass = assetManager.loadTexture("Textures/Terrain/splat/grass.jpg");
         grass.setWrap(Texture.WrapMode.Repeat);
         mat_terrain.setTexture("Tex1", grass);
         mat_terrain.setFloat("Tex1Scale", 64f);
         Texture dirt = assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg");
         dirt.setWrap(Texture.WrapMode.Repeat);
         mat_terrain.setTexture("Tex2", dirt);
         mat_terrain.setFloat("Tex2Scale", 32f);
         Texture rock = assetManager.loadTexture("Textures/Terrain/splat/road.jpg");
         rock.setWrap(Texture.WrapMode.Repeat);
         mat_terrain.setTexture("Tex3", rock);
         mat_terrain.setFloat("Tex3Scale", 128f);
 
         // 2. create heightmap
         Texture heightMapImage = assetManager.loadTexture("Textures/Terrain/splat/mountains512.png");
         AbstractHeightMap heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
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
     }
     
     Spatial ninja;
     Node playerNode;
     Vector3f direction = new Vector3f();
     
     public void createLocalPlayer()
     {
         ninja = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
         ninja.scale(0.04f, 0.04f, 0.04f);
         ninja.rotate(0.0f, -3.0f, 0.0f);    
 
         playerNode = new Node("playerNode");
         playerNode.attachChild(ninja);
         rootNode.attachChild(playerNode);
     }
     
     CameraNode camNode;
     float camHeight = 30;
     public void createCamera()
     {
         flyCam.setEnabled(false);
         camNode = new CameraNode("Camera Node", cam);
         camNode.setControlDir(CameraControl.ControlDirection.SpatialToCamera);
         playerNode.attachChild(camNode);
         camNode.setLocalTranslation(new Vector3f(0, camHeight, -30));
         camNode.lookAt(ninja.getLocalTranslation().add(new Vector3f(0, 15, 0)), Vector3f.UNIT_Y);   
     }
     
     public void createSkybox()
     {
         rootNode.attachChild(SkyFactory.createSky(assetManager,
                 "Textures/Sky/Bright/BrightSky.dds", false));
     }
     
     public void createLight()
     {
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(new Vector3f(-0.1f, -0.7f, -1.0f));
         rootNode.addLight(sun);
     }
     
     public void setPlayerYPos()
     {
         Vector3f pt = playerNode.getLocalTranslation();
         float h = getTerrainHeight(pt);
         playerNode.setLocalTranslation(pt.x, h, pt.z);
     }
     
     public float getTerrainHeight(Vector3f pt)
     {
         if(!useTerrain)
         {
             return 0;
         }
         
         Vector3f ts = terrain.getLocalScale();
         Vector3f tt = terrain.getLocalTranslation();
         return terrain.getHeightmapHeight(new Vector2f(pt.x, pt.z)) * ts.y + tt.y; 
     }
 
     // =========================================================================
     // For modifying objects
     // =========================================================================
     
     Map<Id, Geometry> objects = new TreeMap<Id, Geometry>();
     int objectCount = 0;
     
     public void updateObject(ChestObject object)
     {
         Vector3f location = new Vector3f(
                 object.x,
                 getTerrainHeight(new Vector3f(object.x, 0, object.y)),
                 object.y);
                     
         Region playerRegion = GameModel.getPlayersRegion(model.position);
         Region itemRegion = GameModel.getPlayersRegion(location);
         if(
                 Math.abs(itemRegion.x - playerRegion.x) > 1 ||
                 Math.abs(itemRegion.y - playerRegion.y) > 1)
         {
             System.out.println("SKIPPING (" + itemRegion.x + ", " + itemRegion.y + ")");
             return;
         }
         
         if(!objects.containsKey(object.id))
         {                    
             Box b = new Box(Vector3f.ZERO, 1, 1, 1);
             Geometry geom = new Geometry("Box" + objectCount, b);
             Material mat = new Material(
                     assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
             mat.setColor("Color", ColorRGBA.Blue);
             geom.setMaterial(mat);
             geom.setLocalTranslation(location);
             rootNode.attachChild(geom);
             objectCount++;
             objects.put(object.id, geom);
         }
     }
     
     public void clearDirtyRegions()
     {
         Region playerRegion = GameModel.getPlayersRegion(model.position);
         //System.out.println("moved to region (" + playerRegion.x + ", " + playerRegion.y + ")");
         
         // remove any items not within a 9x9 block of regions centered on
         // the player's current region
         for(Iterator it = objects.entrySet().iterator(); it.hasNext();)
         {
             Map.Entry pair = (Map.Entry)it.next();
             Geometry item = (Geometry)pair.getValue();
             
             Region itemRegion =
                     GameModel.getPlayersRegion(item.getLocalTranslation());
             
             //System.out.println("item at region (" + itemRegion.x + ", " + itemRegion.y + ")");
             if(
                     Math.abs(itemRegion.x - playerRegion.x) > 1 ||
                     Math.abs(itemRegion.y - playerRegion.y) > 1)
             {
                 System.out.println("REMOVING (" + itemRegion.x + ", " + itemRegion.y + ")");
                 item.removeFromParent();
                 it.remove();
             }    
         }
         
         // remove any players not within the 9x9 block
         if(model.clearViewOnRegionChange)
         {
             for(Iterator it = players.entrySet().iterator(); it.hasNext();)
             {
                 Map.Entry pair = (Map.Entry)it.next();
                Spatial item = (Geometry)pair.getValue();
 
                 Region itemRegion =
                         GameModel.getPlayersRegion(item.getLocalTranslation());
 
                 if(
                         Math.abs(itemRegion.x - playerRegion.x) > 1 ||
                         Math.abs(itemRegion.y - playerRegion.y) > 1)
                 {
                     item.removeFromParent();
                     it.remove();
                 }    
             }
         }
     }
     
     // =========================================================================
     // For modifying players
     // =========================================================================
     
     Map<NodeHandle, Spatial> players = new HashMap<NodeHandle, Spatial>();
     Map<NodeHandle, PlayerData> playerLocations = new HashMap<NodeHandle, PlayerData>();
     List<NodeHandle> playerRemovals = new LinkedList<NodeHandle>();
   
     public Vector3f getPlayerPosition()
     {
         return playerNode.getLocalTranslation();
     }
 
     public Quaternion getPlayerRotation()
     {
         return playerNode.getLocalRotation();
     }
     
     public void removePlayer(NodeHandle id)
     { 
         if(players.containsKey(id))
         {
             players.get(id).removeFromParent();
             players.remove(id);
         }
     }
     
     public void updatePlayer(PlayerData data)
     {
         if(!players.containsKey(data.id))
         {
             Spatial ninja1 = assetManager.loadModel("Models/Ninja/Ninja.mesh.xml");
             ninja1.scale(0.04f, 0.04f, 0.04f);
             ninja1.rotate(0.0f, -3.0f, 0.0f);
             rootNode.attachChild(ninja1);
             players.put(data.id, ninja1);
         }
         
         Spatial pNode = players.get(data.id);
         
         Vector3f pos = data.position;
         Quaternion rot = data.rotation;
         rot = rot.inverse();
 
         Vector3f pt = pos;
         float h = getTerrainHeight(pt);
         pos.y = h;
 
         pNode.setLocalTranslation(pos);
         pNode.setLocalRotation(rot);
     }
   
     public String getPlayerName()
     {
         return playerName;
     }
     
     public void receiveChatMessage(String message)
     {
         hudController.addMessage(message);
     }
     
     public void sendChatMessage(String message)
     {
         model.sendChatMessage(message);
     }
   
     // =========================================================================
     // Game interface to GUI
     // =========================================================================
     
     boolean timeToPlay = false;
     String targetIP;
     int targetPort;
     int thisPort;
     public void play(String name, String ip, String port, String thisPort)
     {
         playerName = name;
         targetIP = ip;
         targetPort = Integer.parseInt(port);
         this.thisPort = Integer.parseInt(thisPort);
         timeToPlay = true;
     }
     
     public void quit()
     {
         model.shutdown();
         this.stop();
     }
     
     public void toggleMenuPopup()
     {
         if(!isStarted)
         {
             return;
         }
         
         if(!isInMenu)
         {
             gameMenu = nifty.createPopup("popupExit");
             nifty.showPopup(
                     nifty.getCurrentScreen(), gameMenu.getId(), null);
         }
         else
         {
             nifty.closePopup(gameMenu.getId());
         }
         isInMenu = !isInMenu;
     }
     
     // =========================================================================
     // Input stuff
     // =========================================================================
     
     private void initMenuKeys()
     {
         inputManager.deleteMapping(SimpleApplication.INPUT_MAPPING_EXIT);
         inputManager.addMapping("Escape",  new KeyTrigger(KeyInput.KEY_ESCAPE));
         inputManager.addListener(actionListener, new String[]{"Escape"});
     }
     
     private void initGameKeys()
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
     
     private ActionListener actionListener = new ActionListener()
     { 
         public void onAction(String name, boolean keyPressed, float tpf)
         {
             if(name.equals("Escape") && !keyPressed)
             {
                 toggleMenuPopup();
             }
         }
     };
     
     public void onAnalog(String name, float value, float tpf)
     {
         if(isInMenu)
         {
             return;
         }
 
         model.positionChanged = false;
         direction.set(cam.getDirection()).normalizeLocal();
 
         float moveSpeed = 50f;
         float rotateSpeed = 8f;
 
         if(name.equals("moveForward"))
         {
             direction.multLocal(tpf * moveSpeed);      
             playerNode.move(direction);       
             model.positionChanged = true;
         }
 
         if(name.equals("moveBackward"))
         {
             direction.multLocal(-1 * tpf * moveSpeed);
             playerNode.move(direction);     
             model.positionChanged = true;
         }
 
         if(name.equals("moveRight"))
         {
             direction.crossLocal(Vector3f.UNIT_Y).multLocal(tpf * moveSpeed);
             playerNode.move(direction);
             model.positionChanged = true;
         }
 
         if(name.equals("moveLeft"))
         {
             direction.crossLocal(Vector3f.UNIT_Y).multLocal(-1 * tpf * moveSpeed);
             playerNode.move(direction);       
             model.positionChanged = true;
         }
 
         if(name.equals("rotateRight"))
         {
             playerNode.rotate(0, tpf * rotateSpeed, 0);
             model.positionChanged = true;
         }
 
         if(name.equals("rotateLeft"))
         {
             playerNode.rotate(0, -1 * tpf * rotateSpeed, 0);
             model.positionChanged = true;
         }
 
         if(name.equals("rotateUp"))
         {}
 
         if(name.equals("rotateDown"))
         {}
 
         setPlayerYPos();
 
         // Here we need to see if our region has changed as well
         if(model.positionChanged)
         {
             model.checkRegionChange();
         }
         
         hudText.setText(
                 model.position.toString() + "         " +
                 model.region.x + ", " + model.region.y);
     }
 }
