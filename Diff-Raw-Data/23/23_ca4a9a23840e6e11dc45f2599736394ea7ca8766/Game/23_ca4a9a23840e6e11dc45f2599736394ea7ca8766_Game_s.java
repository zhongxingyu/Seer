 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package main.game;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.app.state.AppState;
 import com.jme3.audio.AudioNode;
 import com.jme3.audio.LowPassFilter;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.collision.CollisionResults;
 import com.jme3.font.BitmapFont;
 import com.jme3.font.BitmapText;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Matrix3f;
 import com.jme3.math.Matrix4f;
 import com.jme3.math.Ray;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.math.Vector4f;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.post.FilterPostProcessor;
 import com.jme3.post.filters.BloomFilter;
 import com.jme3.post.filters.DepthOfFieldFilter;
 import com.jme3.post.filters.LightScatteringFilter;
 import com.jme3.renderer.Camera;
 import com.jme3.renderer.RenderManager;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.terrain.geomipmap.TerrainLodControl;
 import com.jme3.terrain.geomipmap.TerrainQuad;
 import com.jme3.terrain.heightmap.AbstractHeightMap;
 import com.jme3.terrain.heightmap.ImageBasedHeightMap;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture2D;
 import com.jme3.util.SkyFactory;
 import com.jme3.water.WaterFilter;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.builder.ScreenBuilder;
 import de.lessvoid.nifty.screen.DefaultScreenController;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import main.common.Circle;
 import main.exception.ActionNotEnabledException;
 import main.game.action.Action;
 import main.game.action.SpawnAction;
 import main.game.action.creature.AttackAction;
 import main.game.action.creature.EatAction;
 import main.game.action.creature.FleeAction;
 import main.game.action.creature.MoveAction;
 import main.game.action.creature.PickupFoodAction;
 import main.game.gui.CreatureMenuController;
 import main.game.gui.FeedMenuController;
 import main.game.gui.HudController;
 import main.game.gui.SpawnMenuController;
 import main.game.model.Base;
 import main.game.model.Explosion;
 import main.game.model.FoodSource;
 import main.game.model.cell.Cell;
 import main.game.model.cell.DeepWaterCell;
 import main.game.model.cell.LandCell;
 import main.game.model.cell.RockCell;
 import main.game.model.cell.ShallowWaterCell;
 import main.game.model.control.FoodSourceControl;
 import main.game.model.creature.AirborneCreature;
 import main.game.model.creature.Creature;
 import main.game.model.creature.LandCreature;
 import main.game.model.creature.SeaCreature;
 import main.game.setup.RtsCam;
 import main.lobby.Lobby;
 
 /**
  *
  * @author Daniel
  */
 public class Game extends SimpleApplication
 {
     /**
      * GUI
      */
     
     private HudController hudController;
     private CreatureMenuController cmController;
     
     /**
      * Networking
      */
     
     private boolean quackReceived = false;
     private boolean quack = false;
     private boolean leaveGame = false;
     private Player receiveLeaveGamePlayer = null;
     
     /**
      * While running
      */
     
     private Object selectedObject = null;
     
     /**
      * Setup
      */
     
     private boolean showInfo = true;
     private boolean isRunning = true;
     private ActionListener actionListener = new ActionListener()
     {
         public void onAction(String name, boolean keyPressed, float tpf)
         {
             if (name.equals("Pause") && !keyPressed)
             {
                 showInfo = !showInfo;
                 setDisplayFps(showInfo); // to hide the FPS
                 setDisplayStatView(showInfo); // to hide the statistics 
                 //isRunning = !isRunning;
             }
 
             /**
              * [RIGHT-CLICK] Select duck
              */
             
             if (!keyPressed && name.equals("RightClick"))
             {
                 Vector2f click2d = inputManager.getCursorPosition();
                 Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
                 Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
 
                 CollisionResults results = new CollisionResults();
                 Ray ray = new Ray(click3d, dir);
                 world.getSelectableObjects().collideWith(ray, results);
 
                 // Check if something was selected
                 if (results.getClosestCollision() != null && results.getClosestCollision().getGeometry().getParent() != null)
                 {
                     Spatial selectedSpatial = results.getClosestCollision().getGeometry().getParent();
                     String modelType = selectedSpatial.getUserData("modelType");
                     
                     if (modelType != null)
                     {
                         if (modelType.equals("Duck") && world.findDuck().isQuackable())
                         {
                             quack = true;
                             world.findDuck().quack(quackAudio, false);
                             selectedObject = null;
                         }
                     }
                 }
             }
             
             /**
              * [RIGHT-CLICK] Select creature, base
              */
             if (actionsEnabled && !keyPressed && name.equals("RightClick"))
             {
                 Vector2f click2d = inputManager.getCursorPosition();
                 Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
                 Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
 
                 CollisionResults results = new CollisionResults();
                 Ray ray = new Ray(click3d, dir);
                 world.getSelectableObjects().collideWith(ray, results);
 
                 // Check if something was selected
                 if (results.getClosestCollision() != null)
                 {
                     Spatial selectedSpatial = results.getClosestCollision().getGeometry().getParent();
                     //Geometry selectedGeometry = results.getClosestCollision().getGeometry();
                     String modelType = selectedSpatial.getUserData("modelType");
 
                     /**
                      * Determine which model has been selected
                      */
                     Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                     mat.setColor("Color", ColorRGBA.Orange);
 
                     if (modelType == null)
                     {
                         // Do nothing here
                     }
                     else if (modelType.equals("Base"))
                     {
                         Base base = (Base) world.findBaseById((Integer) selectedSpatial.getUserData("parentId"));
 
                         // You can only select your own base
                         if (base.getPlayer().equals(me))
                         {
                             // Show spawn menu
                             nifty.gotoScreen("spawnMenu");
                         }
                     }
                     else if (modelType.equals(LandCreature.CODE_ID))
                     {
                         System.out.println((String) selectedSpatial.getUserData("parentId"));
                         LandCreature creature = (LandCreature) world.findCreatureById((String) selectedSpatial.getUserData("parentId"));
                         
                         if (creature.getPlayer().equals(me) && creature.isIsAlive())
                         {
                             selectedObject = creature;
                             //creature.getModel().setMaterial(mat);
                         }
                     }
                     else if (modelType.equals(SeaCreature.CODE_ID))
                     {
                         SeaCreature creature = (SeaCreature) world.findCreatureById((String) selectedSpatial.getUserData("parentId"));
                         
                         if (creature.getPlayer().equals(me) && creature.isIsAlive())
                         {
                             selectedObject = creature;
                             //creature.getModel().setMaterial(mat);
                         }
                     }
                     else if (modelType.equals(AirborneCreature.CODE_ID))
                     {
                         AirborneCreature creature = (AirborneCreature) world.findCreatureById((String) selectedSpatial.getUserData("parentId"));
                         
                         if (creature.getPlayer().equals(me) && creature.isIsAlive())
                         {
                             selectedObject = creature;
                             //creature.getModel().setMaterial(mat);
                         }
                     }
                 }
                 else
                 {
                     selectedObject = null;
                 }
             }
 
             /**
              * [LEFT-CLICK while nothing selected]
              */
             /*if (actionsEnabled && !keyPressed && name.equals("Select") && selectedObject == null)
             {
                 Vector2f click2d = inputManager.getCursorPosition();
                 Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
                 Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
 
                 CollisionResults results = new CollisionResults();
                 Ray ray = new Ray(click3d, dir);
                 world.getSelectableObjects().collideWith(ray, results);
 
                 // Check if something was selected
                 if (results.getClosestCollision() != null)
                 {
                     Geometry selectedGeometry = results.getClosestCollision().getGeometry();
                     String modelType = selectedGeometry.getUserData("modelType");
 
                     // Determine which model has been selected
                 }
             }*/
             /**
              * [LEFT-CLICK while something selected] Select terrain, food source, other creature
              */
             if (actionsEnabled && !keyPressed && name.equals("Select") && selectedObject != null)
             {
                 // Get rid of radius circle if it is there
                 if (rootNode.getChild("radiusCircle") != null)
                 {
                     rootNode.detachChild(rootNode.getChild("radiusCircle"));
                 }
                 
                 Vector2f click2d = inputManager.getCursorPosition();
                 Vector3f click3d = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 0f).clone();
                 Vector3f dir = cam.getWorldCoordinates(new Vector2f(click2d.x, click2d.y), 1f).subtractLocal(click3d).normalizeLocal();
 
                 CollisionResults terrainResults = new CollisionResults();
                 CollisionResults results = new CollisionResults();
                 Ray ray = new Ray(click3d, dir);
 
                 terrain.collideWith(ray, terrainResults);
                 world.getSelectableObjects().collideWith(ray, results);
                 
                 // Check if something was selected
                 if (terrainResults.getClosestCollision() != null)
                 {
                     Cell selectedCell = null;
 
                     // Determine which cell was selected
                     Vector3f contactPoint = terrainResults.getClosestCollision().getContactPoint();
 
                     if (contactPoint.y < 0)
                     {
                         // Water cell must be selected
                         Vector3f inverseDir = dir.negate();
                         inverseDir = inverseDir.normalize();
                         inverseDir = inverseDir.mult(0.2f);
                         Vector3f result = contactPoint.clone();
 
                         while (result.y < 0)
                         {
                             result = result.add(inverseDir);
                         }
 
                         selectedCell = world.getCellFromWorldCoordinates(result);
                     }
                     else
                     {
                         selectedCell = world.getCellFromWorldCoordinates(contactPoint);
                     }
 
                     if (selectedObject instanceof Creature)
                     {
                        /**
                         * Attack action
                         */
 
                         boolean enemyInCell = false;
                         Creature enemy = null;
 
                         for (Creature c : selectedCell.getOccupants())
                         {
                            if (!c.getPlayer().equals(me))
                            {
                                enemyInCell = true;
                                enemy = c;
                                break;
                            }
                         }
 
                         if (enemyInCell)
                         {
                            /**
                             * AttackAction
                             */
 
                            AttackAction act = new AttackAction(me, (Creature) selectedObject, enemy, selectedCell);
                            me.registerAction(act);
                         }
                         else if (((Creature) selectedObject).isInFight())
                         {
                             /**
                              * Flee action
                              */
                             
                             FleeAction act = new FleeAction(me, (Creature) selectedObject, selectedCell);
                             me.registerAction(act);
                         }
                         else if (selectedCell.getFoodSource() != null)
                         {
                             /**
                              * PickupFoodAction (from selected cell)
                              */
 
                             PickupFoodAction act = new PickupFoodAction(me, (Creature) selectedObject, selectedCell.getFoodSource());
                             me.registerAction(act);
                         }
                         else if (results.size() > 0)
                         {
                             if (results.getClosestCollision().getGeometry().getParent().getUserData("modelType").equals("FoodSource"))
                             {                                
                                 /**
                                  * PickupFoodAction (from clicked food source)
                                  */
 
                                 PickupFoodAction act = new PickupFoodAction(me, (Creature) selectedObject, world.findFoodSourceById((Integer) results.getClosestCollision().getGeometry().getParent().getUserData("parentId")));
                                 me.registerAction(act);
                             }
                         }
                         else if (!((Creature) selectedObject).isInFight())
                         {
                             /**
                              * Move action
                              */
                             
                             MoveAction act = new MoveAction(me, (Creature) selectedObject, selectedCell);
                             me.registerAction(act);
                             
                             selectedObject = null;
                         }
                         
                         selectedObject = null;
                         
                         // De-select creature
                         //Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                         //mat.setColor("Color", ColorRGBA.White);
                         //((Creature) selectedObject).getModel().setMaterial(mat);
                     }
                     
                     selectedObject = null;
                 }
                 
                 selectedObject = null;
             }
             
             /**
              * Generate circle if necessary
              */
             
             
             if (rootNode.getChild("selectionCircle") != null)
             {
                 rootNode.detachChild(rootNode.getChild("selectionCircle"));
             }
 
             if (rootNode.getChild("selectionDiamond") != null)
             {
                 rootNode.detachChild(rootNode.getChild("selectionDiamond"));
             }
             
             if (selectedObject instanceof Creature)
             {
                 Creature creature = (Creature) selectedObject;
                 
                 /**
                  * Draw radius circle
                  */
                 
                 Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                 mat.setColor("Color", ColorRGBA.Orange);
                 
                 Circle circle = new Circle(creature.getActionRadius() * 32, 100);
                 Geometry circleGeo = new Geometry("selectionCircle", circle);
                 circleGeo.setLocalTranslation(creature.getLocation().getWorldCoordinates());
                 circleGeo.setMaterial(mat);
                 rootNode.attachChild(circleGeo);
                 
                 /**
                  * Draw diamond above its head
                  */
                 
                 Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                 mat2.setColor("Color", ColorRGBA.Orange);
                 
                 Box diamond = new Box(3.5f, 3.5f, 3.5f);
                 Geometry diamondGeo = new Geometry("selectionDiamond", diamond);
                 diamondGeo.setMaterial(mat2);
                 diamondGeo.rotate(FastMath.QUARTER_PI, FastMath.QUARTER_PI, FastMath.QUARTER_PI);
                 diamondGeo.setLocalTranslation(creature.getModel().getWorldTranslation().add(0f, 30f, 0f));
                 rootNode.attachChild(diamondGeo);
                 
                 /**
                  * Create marker here
                  */
                 
                 
             }
 
         }
     };
     
     private AnalogListener analogListener = new AnalogListener()
     {
         public void onAnalog(String name, float value, float tpf)
         {
             if (isRunning)
             {
                 if (name.equals("Rotate"))
                 {
                     player.rotate(0, value * speed, 0);
                 }
                 if (name.equals("Right"))
                 {
                     Vector3f v = player.getLocalTranslation();
                     player.setLocalTranslation(v.x + value * speed, v.y, v.z);
                 }
                 if (name.equals("Left"))
                 {
                     Vector3f v = player.getLocalTranslation();
                     player.setLocalTranslation(v.x - value * speed, v.y, v.z);
                 }
 
             } else
             {
                 System.out.println("Press P to unpause.");
             }
         }
     };
     
     /**
      * Game identification
      */
     
     private Game parent = this;
     private GameCredentials gameCredentials;
     private Lobby lobby;
     
     /**
      * Game constants
      */
     public final static int CONST_CREATURES_LIMIT = 10;
     public final static int CONST_SET_MODE_TIME_LIMIT = 20;
     public final static int CONST_INIT_RANGE_OF_SIGHT = 10;
     public final static int CONST_INIT_START_FOOD = 10;
     public final static int CONST_REGENERATE_FOOD_ROUNDS = 5;
     
     /**
      * Game state variables
      */
     private boolean started;
     private boolean inSetMode = false;
     private int round = 0;
     private int regenTime = 10;
     
     /**
      * Properties
      */
     private World world;
     private Player me;
     private List<Player> players;
     private List<Explosion> explosions = new ArrayList<Explosion>();
     
     /**
      * Terrain
      */
     private Vector3f lightDir = new Vector3f(-4.9236743f, -1.27054665f, 5.896916f);
     private WaterFilter water;
     TerrainQuad terrain;
     Material matRock;
     private BulletAppState bulletAppState;
     private RigidBodyControl landscape;
     AudioNode waves;
     LowPassFilter underWaterAudioFilter = new LowPassFilter(0.5f, 0.1f);
     LowPassFilter underWaterReverbFilter = new LowPassFilter(0.5f, 0.1f);
     LowPassFilter aboveWaterAudioFilter = new LowPassFilter(1, 1);
     //This part is to emulate tides, slightly varrying the height of the water plane
     private float time = 0.0f;
     private float waterHeight = 0.0f;
     private float initialWaterHeight = 0.8f;
     private boolean underWater = false;
     
     /**
      * GUI
      */
     
     NiftyJmeDisplay niftyDisplay;
     Nifty nifty;
     private AudioNode quackAudio;
     
     /**
      * Constructor
      */
     public Game(Lobby lobby, GameCredentials gameCredentials)
     {
         super();
 
         this.lobby = lobby;
         this.gameCredentials = gameCredentials;
 
         this.players = new ArrayList<Player>();
     }
 
     /**
      * Business logic
      */
     /**
      * Initialize the game.
      */
     @Override
     public void simpleInitApp()
     {
         /**
          * Initialize mouse
          */
         inputManager.setCursorVisible(true);
 
         /**
          * Initialize camera
          */
         flyCam.setEnabled(false);
         final RtsCam rtsCam = new RtsCam(cam, rootNode);
         rtsCam.registerWithInput(inputManager);
         rtsCam.setCenter(new Vector3f(20f, 50f, 20f));
 
         /**
          * Initialize GUI setup
          */
         
         initHud();
         
         this.niftyDisplay = new NiftyJmeDisplay(assetManager, inputManager, audioRenderer, guiViewPort);
         this.nifty = niftyDisplay.getNifty();
 
         this.hudController = new HudController(this);
         this.cmController = new CreatureMenuController(this);
         nifty.fromXml("Interface/gui.xml", "hud", this.cmController, this.hudController, new SpawnMenuController(this), new FeedMenuController(this));
         this.hudController.init();
         this.cmController.init();
         guiViewPort.addProcessor(niftyDisplay);
         nifty.gotoScreen("hud");
 
         /**
          * Initialize world
          */
         
         Node worldNode = new Node("worldNode");
         rootNode.attachChild(worldNode);
         this.world = new World(this, worldNode);
 
         Box box = new Box(new Vector3f(0, -4, -5), 15, .2f, 15);
         Geometry floor = new Geometry("the Floor", box);
         Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         mat1.setColor("Color", ColorRGBA.Gray);
         floor.setMaterial(mat1);
         worldNode.attachChild(floor);
 
         Box b = new Box(Vector3f.ZERO, 1, 1, 1);
         player = new Geometry("blue cube", b);
         Material mat = new Material(assetManager,
                 "Common/MatDefs/Misc/Unshaded.j3md");
         mat.setColor("Color", ColorRGBA.Blue);
         player.setMaterial(mat);
         //rootNode.attachChild(player);
 
         /**
          * Initialize world-terrain
          */
         createTerrain(this.world.getWorldNode());
         createGrid();
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(lightDir);
         sun.setColor(ColorRGBA.White.clone().multLocal(1.7f));
         this.world.getWorldNode().addLight(sun);
 
         Spatial sky = SkyFactory.createSky(assetManager, "Scenes/Beach/FullskiesSunset0068.dds", false);
         sky.setLocalScale(350);
 
         this.world.getWorldNode().attachChild(sky);
         cam.setFrustumFar(4000);
 
         water = new WaterFilter(rootNode, lightDir);
         FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
         fpp.addFilter(water);
         BloomFilter bloom = new BloomFilter();
         bloom.setExposurePower(55);
         bloom.setBloomIntensity(1.0f);
         fpp.addFilter(bloom);
         LightScatteringFilter lsf = new LightScatteringFilter(lightDir.mult(-300));
         lsf.setLightDensity(1.0f);
         fpp.addFilter(lsf);
         DepthOfFieldFilter dof = new DepthOfFieldFilter();
         dof.setFocusDistance(0);
         dof.setFocusRange(100);
         fpp.addFilter(dof);
 
         water.setWaveScale(0.003f);
         water.setMaxAmplitude(2f);
         water.setFoamExistence(new Vector3f(1f, 4, 0.5f));
         water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam2.jpg"));
         water.setRefractionStrength(0.2f);
 
         water.setWaterHeight(initialWaterHeight);
         underWater = cam.getLocation().y < waterHeight;
 
         waves = new AudioNode(assetManager, "Sound/Environment/Ocean Waves.ogg", false);
         waves.setLooping(true);
         waves.setReverbEnabled(true);
 
         if (underWater)
         {
             waves.setDryFilter(new LowPassFilter(0.5f, 0.1f));
         } else
         {
             waves.setDryFilter(aboveWaterAudioFilter);
         }
         audioRenderer.playSource(waves);
         viewPort.addProcessor(fpp);
         water.setFoamTexture((Texture2D) assetManager.loadTexture("Common/MatDefs/Water/Textures/foam3.jpg"));
 
         /**
          * Initialize me
          */
         if (this.me == null || this.players == null)
         {
             Player me = new Player(this, 0, "TestPlayer");
             this.players.add(me);
             this.me = me;
 
 
             Player secondPlayer = new Player(this, 1, "Player2");
             this.players.add(secondPlayer);
         }
 
         /**
          * Initialize world
          */
         this.world.initializeBases();
         this.world.initializeFoodSources();
         this.world.initializeDuck();
 
         initKeys();
 
         //(set audio location
         quackAudio = new AudioNode(assetManager, "Sounds/Quack.ogg", false);
         quackAudio.setLooping(false);
         quackAudio.setPositional(true);
         quackAudio.move(this.world.findDuck().getLocation().getWorldCoordinates());
         quackAudio.setVolume(5);
         rootNode.attachChild(quackAudio);
         
         /**
          * Some testing
          */
         //this.world.addCreature(this.me, Creature.TYPE_LAND, this.world.getCells()[32][32]);
         /*SpawnAction spawn = new SpawnAction(this.me, LandCreature.CODE_ID);
         try
         {
             spawn.performAction(this);
         } catch (ActionNotEnabledException ex)
         {
             Logger.getLogger(Game.class.getName()).log(Level.SEVERE, null, ex);
         }*/
     }
 
     /**
      * Feed the creature that is currently selected.
      * 
      * @Pre this.selectedObject instanceof Creature
      */
     public void feedSelectedCreature(int numFood)
     {
         EatAction act = new EatAction(me, ((Creature) this.selectedObject), numFood);
         me.registerAction(act);
     }
     
     /**
      * Initialize key mappings
      */
     private void initKeys()
     {
         // You can map one or several inputs to one named action
         inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
         inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_J));
         inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_K));
         inputManager.addMapping("Rotate", new KeyTrigger(KeyInput.KEY_SPACE));
         //new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
         inputManager.addMapping("Select", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
         inputManager.addMapping("RightClick", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
 
 
         // Add the names to the action listener.
         inputManager.addListener(actionListener, new String[]
                 {
                     "Pause", "Select", "RightClick"
                 });
         inputManager.addListener(analogListener, new String[]
                 {
                     "Left", "Right", "Rotate"
                 });
     }
 
     /**
      * Networking
      */
     
     public boolean setModeSent = true;
     public boolean getModeBlocked = false;
     
     private long countSetMode = CONST_SET_MODE_TIME_LIMIT - 1000;
     private long countGetMode = 0;
     private boolean getModePerformed = false;
     
     /**
      * Perform the update loop.
      *
      * @param tpf
      */
     @Override
     public void simpleUpdate(float tpf)
     {
         /**
          * Handle quack
          */
         
         if (this.quackReceived)
         {
             this.quackReceived = false;
             this.world.findDuck().quack(this.quackAudio, true);
         }
         
         //System.out.println(this.countSetMode + " - " + tpf + " - " + this.countGetMode);
         
         if (this.inSetMode)
         {
             // SET-mode
             this.countSetMode += tpf * 1000;
             
             if (countSetMode > CONST_SET_MODE_TIME_LIMIT * 1000)
             {
                 System.out.println("Set mode done");
                 this.inSetMode = false;
                 this.countSetMode = 0;
                 this.countGetMode = 0;
                 this.getModeBlocked = true;
                 this.getModePerformed = false;
                 this.disableActions();
             }
         }
         else
         {
             // GET-mode
             if (!this.getModeBlocked)
             {
                 if (!this.getModePerformed)
                 {
                     this.getModePerformed = true;
                     
                     /**
                      * Perform actions
                      */
                     
                     List<Action> actions = new ArrayList<Action>();
                     
                     for (Player player : players)
                     {
                         for (Action action : player.getActions())
                         {
                             actions.add(action);
                         }
                         
                         player.getActions().clear();
                     }
                     
                     // First perform all flee actions
                     for (Action action : actions)
                     {
                         if (action instanceof FleeAction)
                         {
                             try
                             {
                                 action.performAction(this);
                             }
                             catch (ActionNotEnabledException ex)
                             {
                                 System.out.println("Flee action no longer available.");
                             }
                         }
                     }
                     
                     // Secondly, perform all attack actions
                     for (Action action : actions)
                     {
                         if (action instanceof AttackAction)
                         {
                             try
                             {
                                 action.performAction(this);
                             }
                             catch (ActionNotEnabledException ex)
                             {
                                 System.out.println("Attack action no longer available.");
                             }
                         }
                     }
                     
                     // Thirdly, perform all eat actions
                     for (Action action : actions)
                     {
                         if (action instanceof EatAction)
                         {
                             try
                             {
                                 action.performAction(this);
                             }
                             catch (ActionNotEnabledException ex)
                             {
                                 System.out.println("Attack action no longer available.");
                             }
                         }
                     }
                     
                     // Fourthly, perform all other actions
                     for (Action action : actions)
                     {
                         if (!(action instanceof FleeAction) && !(action instanceof AttackAction) && !(action instanceof EatAction))
                         {
                             try
                             {
                                 action.performAction(this);
                             }
                             catch (ActionNotEnabledException ex)
                             {
                                 System.out.println("Attack action no longer available.");
                             }
                         }
                     }
                     
                     
                 }
                 
                 this.countGetMode += tpf * 1000;
             
                 if (this.countGetMode > 10000)
                 {
                     System.out.println("Get mode done.");
                     this.enableActions();
                     this.inSetMode = true;
                     this.countSetMode = 0;
                     this.countGetMode = 0;
                     this.setModeSent = false;
                     
                     /**
                      * Do some bookkeeping
                      */
                     
                     this.nextRound();
                     
                     /**
                      * Handle leave games
                      */
                     
                     if (this.receiveLeaveGamePlayer != null)
                     {
                         this.removePlayerInternally();
                     }
                     
                     /**
                     * Handle battles
                     */
 
                     // For any of our creatures that are in fight
                     for (Creature creature : this.me.getCreatures())
                     {
                         if (creature.isInFight())
                         {
                             // Attack anybody who is in the same cell as this creature (from the opposite team)
                             for (Creature opponent : creature.getLocation().getOccupants())
                             {
                                 if (!(this.me.equals(opponent.getPlayer())))
                                 {
                                     AttackAction act = new AttackAction(this.me, creature, opponent, creature.getLocation());
                                     this.me.registerAction(act);
                                 }
                             }
                         }
                     }
                 }
             }
         }
         
         /**
          * set your ears at the cam position, so we can hear 3D sounds
          */
         listener.setLocation(cam.getLocation());
         listener.setRotation(cam.getRotation());
 
         /**
          * Add creature controllers
          */
         for (Creature creature : this.world.getCreatures())
         {
             creature.getController().update(tpf);
         }
 
         /**
          * Add food source controllers
          */
         
         for (FoodSource foodSource : this.world.getFoodSources())
         {
             foodSource.getController().update(tpf);
         }
         
         /**
          * Add duck controller
          */
         
         this.world.findDuck().getController().update(tpf);
         
         /**
          * Terrain
          */
         
         time += tpf;
         waterHeight = (float) Math.cos(((time * 0.6f) % FastMath.TWO_PI)) * 1.5f;
         water.setWaterHeight(initialWaterHeight + waterHeight);
         if (water.isUnderWater() && !underWater)
         {
 
             waves.setDryFilter(new LowPassFilter(0.5f, 0.1f));
             underWater = true;
         }
         if (!water.isUnderWater() && underWater)
         {
             underWater = false;
             waves.setDryFilter(new LowPassFilter(1, 1f));
 
         }
         
         /**
          * Update creature headers (health bar)
          */
         
         String header;
         
         for (Creature creature : this.world.getCreatures())
         {
             if (creature.isIsAlive())
             {
                 if (creature instanceof AirborneCreature)
                 {
                     header = "Health: " + creature.getHealth() + "% / Stamina: " + ((AirborneCreature) creature).getStamina() + "%";
                 }
                 else
                 {
                     header = "Health: " + creature.getHealth() + "%";
                 }
 
                 if (creature.isInFight())
                 {
                     header += " (!)";
                 }
 
                 creature.getCreatureHeader().setText(header);
 
                 if (creature.getLocation().getOccupants().size() > 1)
                 {
                     creature.getCreatureHeader().setLocalTranslation(cam.getScreenCoordinates(creature.getModel().getWorldTranslation().add(0, 20f + Float.parseFloat(creature.getId().split("_")[1]) * creature.getCreatureHeader().getHeight(), 0)).add(-1 * creature.getCreatureHeader().getLineWidth() / 2f, 0f, 0f));
                 }
                 else
                 {
                     creature.getCreatureHeader().setLocalTranslation(cam.getScreenCoordinates(creature.getModel().getWorldTranslation().add(0, 20f, 0)).add(-1 * creature.getCreatureHeader().getLineWidth() / 2f, 0f, 0f));
                 }
             }
         }
         
         /**
          * Update explosions
          */
         
         for (Explosion explosion : this.explosions)
         {
             explosion.getControl().update(tpf);
         }
         
         /**
          * Update hud info
          */
         
         this.hudController.update(tpf);
         this.cmController.update(tpf);
     }
     
     private BitmapText modeInfo;
     private BitmapText roundInfo;
     private BitmapText playerInfo;
     
     public void addExplosion(Explosion explosion)
     {
         this.rootNode.attachChild(explosion.getControl().getSpatial());
         this.explosions.add(explosion);
     }
     
     public void removeExplosion(Explosion explosion)
     {
         this.explosions.remove(explosion);
         this.rootNode.detachChild(explosion.getControl().getSpatial());
     }
     
     public void initHud()
     {
         this.modeInfo = new BitmapText(guiFont, false);
         this.roundInfo = new BitmapText(guiFont, false);
         this.playerInfo = new BitmapText(guiFont, false);
         
         this.modeInfo.setSize(guiFont.getCharSet().getRenderedSize() * 1.5f);
         this.roundInfo.setSize(guiFont.getCharSet().getRenderedSize() * 1.5f);
         this.playerInfo.setSize(guiFont.getCharSet().getRenderedSize() * 1.5f);
         
         this.modeInfo.setColor(ColorRGBA.White);
         this.roundInfo.setColor(ColorRGBA.White);
         this.playerInfo.setColor(ColorRGBA.White);
         
         this.modeInfo.setLocalTranslation(5f, 5f + modeInfo.getHeight(), 0f);
         this.roundInfo.setLocalTranslation(5f, 10f + modeInfo.getHeight() + roundInfo.getHeight(), 0f);
         this.playerInfo.setLocalTranslation(5f, 15f + modeInfo.getHeight() + roundInfo.getHeight() + playerInfo.getHeight(), 0f);
         
         guiNode.attachChild(modeInfo);
         guiNode.attachChild(roundInfo);
         guiNode.attachChild(playerInfo);
     }
 
     @Override
     public void simpleRender(RenderManager rm)
     {
         //TODO: add render code
     }
 
     public Player getPlayerById(int id)
     {
         for (Player player : this.players)
         {
             if (player.getId() == id)
             {
                 return player;
             }
         }
         
         return null;
     }
     
     /**
      * Start the game.
      */
     @Override
     public void start()
     {
         this.started = true;
         super.start();
     }
 
     /**
      * End the game.
      */
     public void end()
     {
         super.stop();
         this.started = false;
     }
 
     private boolean actionsEnabled = true;
     
     private void disableActions()
     {
         this.actionsEnabled = false;
     }
 
     private void enableActions()
     {
         this.actionsEnabled = true;
     }
 
     private void nextRound()
     {
         this.round++;
         
         /*
          * Regenerate food when necessary
          */
         
         if (this.round % CONST_REGENERATE_FOOD_ROUNDS == 0)
         {
             for (FoodSource foodSource : this.world.getFoodSources())
             {
                 FoodSourceControl c= (FoodSourceControl)foodSource.getController();
                 Node s = (Node) c.getSpatial();
                 s.attachChild(c.getFull());
                 c.setSpatial(null);
                 c.setSpatial(s);
                 foodSource.refill();
             }
         }
         
         /**
          * Decrease/increase stamina of airborne creatures
          */
         
         for (Creature creature : this.world.getCreatures())
         {
             if (creature instanceof AirborneCreature)
             {
                 if (((AirborneCreature) creature).isAirborne())
                 {
                     ((AirborneCreature) creature).decreaseStamina();
                 }
                 else
                 {
                     ((AirborneCreature) creature).increaseStamina();
                 }
             }
         }
     }
     protected Geometry player;
 
     private void createTerrain(Node rootNode)
     {
         /**
          * Set up Physics
          */
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
         /**
          * 1. Create terrain material and load four textures into it.
          */
         Material mat_terrain = new Material(assetManager,
                 "Common/MatDefs/Terrain/Terrain.j3md");
 
         /**
          * 1.1) Add ALPHA map (for red-blue-green coded splat textures)
          */
         mat_terrain.setTexture("Alpha", assetManager.loadTexture(
                 "Textures/alphamap.jpg"));
 
         /**
          * 1.2) Add GRASS texture into the red layer (Tex1).
          */
         Texture grass = assetManager.loadTexture(
                 "Textures/Terrain/splat/road.jpg");
         grass.setWrap(Texture.WrapMode.Repeat);
         mat_terrain.setTexture("Tex1", grass);
         mat_terrain.setFloat("Tex1Scale", 64f);
 
         /**
          * 1.3) Add DIRT texture into the green layer (Tex2)
          */
         Texture dirt = assetManager.loadTexture(
                 "Textures/Terrain/splat/grass.jpg");
         dirt.setWrap(Texture.WrapMode.Repeat);
         mat_terrain.setTexture("Tex2", dirt);
         mat_terrain.setFloat("Tex2Scale", 32f);
 
         /**
          * 1.4) Add ROAD texture into the blue layer (Tex3)
          */
         Texture rock = assetManager.loadTexture(
                 "Textures/Terrain/splat/dirt.jpg");
         rock.setWrap(Texture.WrapMode.Repeat);
         mat_terrain.setTexture("Tex3", rock);
         mat_terrain.setFloat("Tex3Scale", 128f);
 
         /**
          * 2. Create the height map
          */
         AbstractHeightMap heightmap = null;
         Texture heightMapImage = assetManager.loadTexture(
                 "Textures/heightmap_raw.jpg");
         heightmap = new ImageBasedHeightMap(heightMapImage.getImage());
         heightmap.load();
 
         /**
          * 3. We have prepared material and heightmap. Now we create the actual
          * terrain: 3.1) Create a TerrainQuad and name it "my terrain". 3.2) A
          * good value for terrain tiles is 64x64 -- so we supply 64+1=65. 3.3)
          * We prepared a heightmap of size 1024x1024 -- so we supply
          * 1024+1=1025. 3.4) As LOD step scale we supply Vector3f(1,1,1). 3.5)
          * We supply the prepared heightmap itself.
          */
         int patchSize = 65;
         terrain = new TerrainQuad("my terrain", patchSize, 1025, heightmap.getHeightMap());
 
         /**
          * 4. We give the terrain its material, position & scale it, and attach
          * it.
          */
         terrain.setMaterial(mat_terrain);
         terrain.setLocalTranslation(0, -100, 0);
         terrain.setLocalScale(2f, 1f, 2f);
         rootNode.attachChild(terrain);
 
         /**
          * 5. The LOD (level of detail) depends on were the camera is:
          */
         TerrainLodControl control = new TerrainLodControl(terrain, getCamera());
         terrain.addControl(control);
 
         /**
          * 6. Add physics:
          */
         // We set up collision detection for the scene by creating a
         // compound collision shape and a static RigidBodyControl with mass zero.*/
         CollisionShape terrainShape = CollisionShapeFactory.createMeshShape((Node) terrain);
         landscape = new RigidBodyControl(terrainShape, 0);
         terrain.addControl(landscape);
 
         bulletAppState.getPhysicsSpace().add(terrain);
 
     }
 
     private void createGrid()
     {
         float height;
         int cell_size = 32;
         int num_cells = 2048 / cell_size;
         Cell[][] cells = new Cell[num_cells][num_cells];
 
         Vector3f trans = terrain.getWorldTranslation();
         Vector3f worldCoors;
 
         for (int i = 0; i < num_cells; i++)
         {
             for (int j = 0; j < num_cells; j++)
             {
                 float xCoord = -1024 + i * cell_size + cell_size / 2f;
                 float yCoord = -1024 + j * cell_size + cell_size / 2f;
                 height = terrain.getHeight(new Vector2f(xCoord, yCoord));
                 worldCoors = new Vector3f(xCoord, height, yCoord);
                 worldCoors = worldCoors.add(trans);
                 //System.out.println(trans);
 
                 Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
 
                 /*
                  * use constructor for cell in this way:
                  * new Cell(xCoor, yCoor, worldCoors)
                  * xCoor and yCoor are the atributes in the corisponding class
                  * worldCoor should be added for knowing the absolute position
                  * of the cell in our world.
                  */
                 if (height <= 90)
                 {
                     cells[i][j] = new DeepWaterCell(this.world, i, j, worldCoors);
                     cells[i][j].setWorldCoordinates(new Vector3f(worldCoors.x, 90 - 100, worldCoors.z));
                     mat.setColor("Color", ColorRGBA.Blue);
                 } else if (height > 90 && height <= 105)
                 {
                     cells[i][j] = new ShallowWaterCell(this.world, i, j, worldCoors);
                     mat.setColor("Color", ColorRGBA.Cyan);
                 } else if (height > 105 && height <= 130)
                 {
                     cells[i][j] = new LandCell(this.world, i, j, worldCoors);
                     mat.setColor("Color", ColorRGBA.Green);
                 } else if (height > 130)
                 {
                     cells[i][j] = new RockCell(this.world, i, j, worldCoors);
                     mat.setColor("Color", ColorRGBA.Gray);
                 }
 
                 /*Box box = new Box(Vector3f.ZERO, 1, 1, 1);
                  Geometry geometry = new Geometry("box_" + i + "_" + j, box);
                
                  Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                  geometry.setMaterial(mat);*/
 
                 //this.world.getWorldNode().attachChild(geometry);
                 //geometry.setLocalTranslation(cells[i][j].getWorldCoordinates());/
             }
         }
 
         this.world.setCells(cells);
     }
     
     public void quack()
     {
         this.quackReceived = true;
     }
 
     public void removePlayerFromGame(Player player)
     {
         this.receiveLeaveGamePlayer = player;
     }
     
     private void removePlayerInternally()
     {        
         Player player = this.receiveLeaveGamePlayer;
         this.receiveLeaveGamePlayer = null;
         
         if (this.me.equals(player))
         {
             this.stop();
         }
         else
         {
             // Remove player from players
             this.players.remove(player);
 
             // Remove all creatures from this player from the game
             Set<Creature> removeSet = new HashSet<Creature>();
 
             for (Creature creature : this.world.getCreatures())
             {
                 if (creature.getPlayer().equals(player))
                 {
                     removeSet.add(creature);
                 }
             }
 
             for (Creature creature : removeSet)
             {
                 this.world.removeCreature(creature);
             }
 
             // Remove base of player
             this.world.removebase(player.getBase());
         }
     }
     
     /**
      * Getters & setters
      */
     public boolean isIsRunning()
     {
         return isRunning;
     }
 
     public void setIsRunning(boolean isRunning)
     {
         this.isRunning = isRunning;
     }
 
     public Object getSelectedObject() {
         return selectedObject;
     }
 
     public void setSelectedObject(Object selectedObject) {
         this.selectedObject = selectedObject;
     }
 
     public GameCredentials getGameCredentials()
     {
         return gameCredentials;
     }
 
     public void setGameCredentials(GameCredentials gameCredentials)
     {
         this.gameCredentials = gameCredentials;
     }
 
     public Lobby getLobby()
     {
         return lobby;
     }
 
     public void setLobby(Lobby lobby)
     {
         this.lobby = lobby;
     }
 
     public boolean isStarted()
     {
         return started;
     }
 
     public void setStarted(boolean started)
     {
         this.started = started;
     }
 
     public boolean isInSetMode()
     {
         return inSetMode;
     }
 
     public void setInSetMode(boolean inSetMode)
     {
         this.inSetMode = inSetMode;
     }
 
     public int getRound()
     {
         return round;
     }
 
     public void setRound(int round)
     {
         this.round = round;
     }
 
     public int getRegenTime()
     {
         return regenTime;
     }
 
     public void setRegenTime(int regenTime)
     {
         this.regenTime = regenTime;
     }
 
     public World getWorld()
     {
         return world;
     }
 
     public void setWorld(World world)
     {
         this.world = world;
     }
 
     public Player getMe()
     {
         return me;
     }
 
     public void setMe(Player me)
     {
         this.me = me;
     }
 
     public Geometry getPlayer()
     {
         return player;
     }
 
     public void setPlayer(Geometry player)
     {
         this.player = player;
     }
 
     public TerrainQuad getTerrain()
     {
         return terrain;
     }
 
     public void setTerrain(TerrainQuad terrain)
     {
         this.terrain = terrain;
     }
 
     public List<Player> getPlayers()
     {
         return players;
     }
 
     public void setPlayers(List<Player> players)
     {
         this.players = players;
     }
     
     public BitmapFont getGuiFont()
     {
         return this.guiFont;
     }
 
     public boolean isQuack() {
         return quack;
     }
 
     public void setQuack(boolean quack) {
         this.quack = quack;
     }
 
     public long getCountSetMode()
     {
         return countSetMode;
     }
 
     public void setCountSetMode(long countSetMode)
     {
         this.countSetMode = countSetMode;
     }
 
     public long getCountGetMode()
     {
         return countGetMode;
     }
 
     public void setCountGetMode(long countGetMode)
     {
         this.countGetMode = countGetMode;
     }
     
     public boolean isLeaveGame() {
         return leaveGame;
     }
 
     public void setLeaveGame(boolean leaveGame) {
         this.leaveGame = leaveGame;
 
     }
 
     public Nifty getNifty()
     {
         return nifty;
     }
 
     public void setNifty(Nifty nifty)
     {
         this.nifty = nifty;
     }
 }
