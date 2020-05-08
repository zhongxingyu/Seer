 package mygame;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.bullet.collision.PhysicsCollisionObject;
 import com.jme3.bullet.collision.shapes.SphereCollisionShape;
 import com.jme3.bullet.control.GhostControl;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.collision.CollisionResult;
 import com.jme3.collision.CollisionResults;
 import com.jme3.effect.ParticleEmitter;
 import com.jme3.effect.ParticleMesh;
 import com.jme3.font.BitmapText;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.light.AmbientLight;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.material.RenderState.BlendMode;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Ray;
 import com.jme3.math.Vector3f;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.renderer.RenderManager;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.texture.Texture;
 import com.jme3.util.SkyFactory;
 import com.jme3.util.TangentBinormalGenerator;
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.builder.ControlBuilder;
 import de.lessvoid.nifty.builder.EffectBuilder;
 import de.lessvoid.nifty.builder.LayerBuilder;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.builder.PopupBuilder;
 import de.lessvoid.nifty.builder.ScreenBuilder;
 import de.lessvoid.nifty.builder.StyleBuilder;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.controls.console.builder.ConsoleBuilder;
 import de.lessvoid.nifty.controls.dropdown.builder.DropDownBuilder;
 import de.lessvoid.nifty.controls.label.builder.LabelBuilder;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.tools.Color;
 import java.util.List;
 import niftyclass.CommonBuilders;
 import niftyclass.DialogPanelControlDefinition;
 import niftyclass.MenuButtonControlDefinition;
 import screens.GameCustomizeControlDefinition;
 import screens.GameCustomizeController;
 import screens.GameSettingControlDefinition;
 import screens.GameSettingController;
 import screens.JmeScreenController;
 
 public class Main extends SimpleApplication {
   
     private Boolean isRunning = false;
     private Boolean isBall1Alive = true;
     private Boolean isBall2Alive = true;   
     private Boolean isBall3Alive = true;
     private Boolean isBall4Alive = true;
     private Boolean gameEnd = false;    
     private Boolean isScreenEnd = false;
     private Boolean isghost=false;
     private int ballLeft;
     
     private BulletAppState bulletAppState;
 
     private Material mat_lit;
     private Material mat_rock;
     private Material mat_dirt;
     private Material mat_road;
     private Material mat_red;
     private Material mat_ghost;
     private final float gax = (float) 0.5;   
     
     private Sphere c;   
     private ParticleEmitter fire;    
     private BitmapText ch;
     private DirectionalLight sun;
     private int counter = 0;
      
     //board parameter
     private int boardLength;
     private int boardWidth;
     public int currentmap;
     private int[] maptexture;
     private int[] mapobjects;    
         
     //Destory Map stuff
     private long deathClk;
     private Spatial[] mapObj;
     private int objNum;
     
     //Ghost Control
     GhostControl ghost=new GhostControl(new SphereCollisionShape(5));
     GhostControl ghosts=new GhostControl(new SphereCollisionShape(21));
     Node gnode = new Node("a ghost-controlled thing");
     
     // array of ball instances
     private RigidBodyControl [] ball_phy;
     private SphereCollisionShape [] ballShape;
     private Geometry [] ball;
     private int [] ballSpeed;
     private Vector3f [] loc;
     
     // Mine instance
     private RigidBodyControl mine_phy;
     private Geometry mine;
     private int mineCnt = 0;
     
     // Hardcoded number of players for testing
     public int numberPlayer;
     private static Main app;
     
     // GUI instance
     private static CommonBuilders builders = new CommonBuilders();
     private NiftyJmeDisplay niftyDisplay;
     private Nifty nifty;
     
     // abilities variables
     private long [] abilityMapping;
     public int [] assignAbility;
     public int [] abilityFromUI;
     
     //Buff varaibles
     private long buffTimer = (System.nanoTime()/1000000000) + 5;
     
     //Map data
     private int[] map1 = {0, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 0, 0, 
         0, 0, 0, 0, 0, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 0, 
         0, 0, 0, 0, 0, 3, 3, 3, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 2, 2, 2, 0,
         0, 2, 2, 2, 0, 0};
     private int[] map2 = {2, 3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 2, 
         3, 3, 3, 3, 3, 3, 3, 3, 3, 2, 2, 2, 2, 2, 2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 0, 
         0, 0, 0, 0, 0, 1, 1, 1, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
     private int[] map3 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0};
     private int[] map4 = {3, 0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 0, 0, 3, 3, 0, 0, 3, 
         0, 0, 0, 3, 3, 3, 0, 0, 0, 3, 0, 0, 3, 3, 0, 0, 0, 3, 0, 0, 0, 0, 0, 3, 
         0, 3, 0, 0, 3, 0, 3, 0, 0, 0, 0, 0, 3, 0, 3, 0, 0, 3, 0, 0, 0, 3, 0, 0, 
         0, 0, 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 3, 3, 3, 0, 0, 3, 3, 3, 0};
     private int[] map5 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
     
     //map objects
     private int[] obj1 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,  
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0};
     private int[] obj2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
     private int[] obj3 = {5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 5, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 
         8, 0, 0, 8, 0, 0, 0, 0, 0, 8, 0, 0, 0, 7, 0, 6, 0, 0, 6, 0, 7, 0, 0};
     private int[] obj4 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 4, 
         0, 4, 4, 0, 4, 0, 0, 0, 4, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
     private int[] obj5 = {0, 0, 0, 0, 7, 0, 6, 0, 0, 0, 0, 0, 0, 7, 7, 0, 0, 0, 
         0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 6, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 7, 0, 0, 0, 6, 
         0, 0, 0, 0, 6, 0, 0, 0, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
     
 
     
     public static void main(String[] args) {
         app = new Main();
         app.start();
     } 
     
     @Override
     public void simpleInitApp() {        
         app.setDisplayFps(false);
         app.setDisplayStatView(false);
         
         /** Set up Physics Game */
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
         bulletAppState.getPhysicsSpace().addCollisionListener(physicsCollisionListener);
         
        
         //Selected map
         setCam();
         setUpLight();
         //Load Map texture and object arraies 
         InitGUI();
    }   
    
     // initialize balls, obstacle and GUI
     private void InitObj(){ 
         getInputFromGUI();
         flyCam.setDragToRotate(false);
         maptexture = selectedMapTexture(currentmap);
         mapobjects = selectedMapObject(currentmap);   
         variableInit();
         initMaterials(); 
         createBallArray(numberPlayer); 
         setUpKeys();             
         createFloor(currentmap);
         createPowerUp();  
         abilityMapping = new long [numberPlayer];
         initAbilities(abilityMapping);
         rootNode.attachChild(SkyFactory.createSky(
             assetManager, "Textures/Sky/Bright/BrightSky.dds", false));
     }
     
     private void InitGUI(){                
         // GUI init
         
         flyCam.setDragToRotate(true);
         isScreenEnd = false;
         isRunning = false;
         niftyDisplay = new NiftyJmeDisplay(
             assetManager, inputManager, audioRenderer, guiViewPort);
         nifty = niftyDisplay.getNifty();
         guiViewPort.addProcessor(niftyDisplay);    
         nifty.loadStyleFile("nifty-default-styles.xml");
         nifty.loadControlFile("nifty-default-controls.xml");
 
         registerMenuButtonHintStyle(nifty);
         registerConsolePopup(nifty);
         // register some helper controls
         MenuButtonControlDefinition.register(nifty);
         DialogPanelControlDefinition.register(nifty);
         // register the dialog controls
         GameSettingControlDefinition.register(nifty);
         GameCustomizeControlDefinition.register(nifty);
         openGameSettingScreen(nifty);
         nifty.gotoScreen("settings");
         
     }
     
     private void variableInit(){
         ballSpeed = new int [numberPlayer];
         isRunning = true;
         isBall1Alive = true;
         isBall2Alive = true;  
         isBall3Alive = true;
         isBall4Alive = true;
         gameEnd = false;      
         mineCnt = 0;
         deathClk = (System.nanoTime()/1000000000) + 5;        
     }
     
     // set camera position and light
     private void setCam(){        
         viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
         flyCam.setEnabled(false);        
         if(currentmap == 0){
             cam.setLocation(new Vector3f(11f,20f,25f)); 
             cam.setAxes(new Vector3f(0f,0f,0f),new Vector3f(0f,0f,0f),new Vector3f(-100f,0f,0f));
             cam.lookAt(new Vector3f(11f,-25f,-10f), cam.getUp());
         }
         else{
             cam.setLocation(new Vector3f(10f,20f,25f)); 
             cam.setAxes(new Vector3f(0f,0f,0f),new Vector3f(0f,0f,0f),new Vector3f(-100f,0f,0f));
             cam.lookAt(new Vector3f(10f,-25f,-10f), cam.getUp());
             
         }
         sun = new DirectionalLight();
         sun.setDirection(new Vector3f(1,0,-2).normalizeLocal());
         sun.setColor(ColorRGBA.White);
         rootNode.addLight(sun);             
     }
     
     //Initialize power-up objects
     private void createPowerUp(){        
         //create and edit properties
         fire = 
            new ParticleEmitter("Emitter", ParticleMesh.Type.Triangle, 30);
         mat_red = new Material(assetManager, 
            "Common/MatDefs/Misc/Particle.j3md");
         mat_red.setTexture("Texture", assetManager.loadTexture(
            "Effects/Explosion/flame.png"));
         fire.setMaterial(mat_red);
         fire.setImagesX(2); 
         fire.setImagesY(2); // 2x2 texture animation
         fire.setEndColor(new ColorRGBA(1f, 0f, 1f, 1f));   // red
         fire.setStartColor(new ColorRGBA(1f, 0f, 2f, 0.5f)); // yellow
         fire.getParticleInfluencer().setInitialVelocity(new Vector3f(0, 2, 0));
         fire.setStartSize(2f);
         fire.setEndSize(0.1f);
         fire.setGravity(0, 0, 0);
         fire.setLowLife(1f);
         fire.setHighLife(2f);
         fire.getParticleInfluencer().setVelocityVariation(0.1f);
         fire.setLocalTranslation(new Vector3f(boardWidth+2f,0.1f,boardLength-3f));        
         //place on the map
         rootNode.attachChild(fire);         
     }
     
     
     private void createBallArray(int number){
         
         c = new Sphere(20, 20, 0.5f, true, false);
         c.setTextureMode(Sphere.TextureMode.Projected);
         TangentBinormalGenerator.generate(c);  
         ball = new Geometry [number];
         ballShape = new SphereCollisionShape[number];
         ball_phy = new RigidBodyControl[number];
         
         for(int i = 0; i < number; i++){
             ball[i] = new Geometry("Ball "+i, c);
             ballShape[i] = new SphereCollisionShape(0.5f);
             if(i==0)
             {
             ball[i].setMaterial(mat_lit);
             ball[i].setLocalTranslation(1.5f,gax,1.5f);
             }
             else if(i==1)
             {
                 ball[1].setMaterial(mat_rock);
                 ball[i].setLocalTranslation(1.5f,gax,2*boardWidth-3.5f);
             }
             else if(i==2)
             {
                 ball[i].setMaterial(mat_dirt);
                 ball[i].setLocalTranslation(2*boardLength-3.5f,gax,2*boardWidth-3.5f);
             }
             else if(i==3)
             {
                 ball[i].setMaterial(mat_road);
                 ball[i].setLocalTranslation(2*boardLength-3.5f,gax,1.5f);
             }
             ballSpeed[i] = 1;
             rootNode.attachChild(ball[i]);
             ball_phy[i] = new RigidBodyControl(ballShape[i],0.9f);
             ball[i].addControl(ball_phy[i]); 
             bulletAppState.getPhysicsSpace().add(ball_phy[i]);
             ball_phy[i].setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
             ball_phy[i].addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
             ball_phy[i].setCollisionGroup(3);
             ball_phy[i].setRestitution(1f);            
             ball_phy[i].setDamping(.4f,.4f);
         }        
     }       
     
     //Initialize material and texture
     public void initMaterials(){
 
         //Defining Ball 1 material
         mat_lit = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat_lit.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond.jpg"));
         mat_lit.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Pond/Pond_normal.png"));
         mat_lit.setBoolean("UseMaterialColors",true);    
         mat_lit.setColor("Specular",ColorRGBA.White);
         mat_lit.setColor("Diffuse",ColorRGBA.White);
         mat_lit.setFloat("Shininess", 5f); // [1,128]   
         
         //Defining Ball 2 material
         mat_rock = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat_rock.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/Rock/Rock.PNG"));
         mat_rock.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/Rock/Rock_normal.png"));
         mat_rock.setBoolean("UseMaterialColors",true);    
         mat_rock.setColor("Specular",ColorRGBA.White);
         mat_rock.setColor("Diffuse",ColorRGBA.White);
         mat_rock.setFloat("Shininess", 5f); // [1,128] 
         
          //Defining Ball 3 material
         mat_dirt = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat_dirt.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg"));
         mat_dirt.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/splat/dirt.jpg"));
         mat_dirt.setBoolean("UseMaterialColors",true);    
         mat_dirt.setColor("Specular",ColorRGBA.White);
         mat_dirt.setColor("Diffuse",ColorRGBA.White);
         mat_dirt.setFloat("Shininess", 5f); // [1,128]
         
         //Defining Ball 4 material
         mat_road = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         mat_road.setTexture("DiffuseMap", assetManager.loadTexture("Textures/Terrain/splat/road.jpg"));
         mat_road.setTexture("NormalMap", assetManager.loadTexture("Textures/Terrain/splat/road.jpg"));
         mat_road.setBoolean("UseMaterialColors",true);    
         mat_road.setColor("Specular",ColorRGBA.White);
         mat_road.setColor("Diffuse",ColorRGBA.White);
         mat_road.setFloat("Shininess", 5f);
 
     }
 
     @Override
     public void simpleRender(RenderManager rm) {
         //TODO: add render code
     }
     
     //Initialize lighting
     private void setUpLight() {
         // We add light so we see the scene and attach to map
         AmbientLight al = new AmbientLight();
         al.setColor(ColorRGBA.White.mult(1.3f));
         rootNode.addLight(al);
     }    
 
     private void setUpKeys() {
         
         //System Keys        
         inputManager.addMapping("Reset",  new KeyTrigger(KeyInput.KEY_P));
         inputManager.addListener(actionListener, "Reset");
         
         
         // Ability keybindings and listener
         inputManager.addMapping("Ability1", new KeyTrigger(KeyInput.KEY_X));
         inputManager.addListener(actionListener, "Ability1");
         inputManager.addMapping("Ability2", new KeyTrigger(KeyInput.KEY_B));
         inputManager.addListener(actionListener, "Ability2");
         if(numberPlayer >= 3){
             inputManager.addMapping("Ability3", new KeyTrigger(KeyInput.KEY_COMMA));
             inputManager.addListener(actionListener, "Ability3");
             if(numberPlayer == 4){
             inputManager.addMapping("Ability4", new KeyTrigger(KeyInput.KEY_NUMPAD2));        
             inputManager.addListener(actionListener, "Ability4");     
             }
         }
         
         // Player 1 keybindings and listener
         
         inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
         inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
         inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
         inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));        
         
         inputManager.addListener(analogListener,new String[]{ "Left","Right", "Up", "Down"});
       
         // Player 2 keybindings and listener
         inputManager.addMapping("Left2", new KeyTrigger(KeyInput.KEY_F));
         inputManager.addMapping("Right2", new KeyTrigger(KeyInput.KEY_H));
         inputManager.addMapping("Up2", new KeyTrigger(KeyInput.KEY_T));
         inputManager.addMapping("Down2", new KeyTrigger(KeyInput.KEY_G));
         
         inputManager.addListener(analogListener,new String[]{ "Left2","Right2", "Up2", "Down2"});        
         
         // Player 3 keybindings and listener
         if(numberPlayer >= 3){
         inputManager.addMapping("Left3", new KeyTrigger(KeyInput.KEY_J));
         inputManager.addMapping("Right3", new KeyTrigger(KeyInput.KEY_L));
         inputManager.addMapping("Up3", new KeyTrigger(KeyInput.KEY_I));
         inputManager.addMapping("Down3", new KeyTrigger(KeyInput.KEY_K));
          
         inputManager.addListener(analogListener,new String[]{ "Left3","Right3", "Up3", "Down3"});        
         if(numberPlayer == 4){
             // Player 4 keybindings and listener
             inputManager.addMapping("Left4", new KeyTrigger(KeyInput.KEY_NUMPAD4));
             inputManager.addMapping("Right4", new KeyTrigger(KeyInput.KEY_NUMPAD6));
             inputManager.addMapping("Up4", new KeyTrigger(KeyInput.KEY_NUMPAD8));
             inputManager.addMapping("Down4", new KeyTrigger(KeyInput.KEY_NUMPAD5));
 
             inputManager.addListener(analogListener,new String[]{ "Left4","Right4", "Up4", "Down4"});
             }
         
         }
     }
     
     private AnalogListener analogListener = new AnalogListener() {
         public void onAnalog(String binding, float value, float tpf) {
            
             // New code: maps ball array
             // player 1 action listener
             if (binding.equals("Left")) {                     
                     ball_phy[0].applyForce(new Vector3f(-3f*ballSpeed[0], 0, 0),new Vector3f(-3f*ballSpeed[0], 0, 0)); // push the ball foward
                 } else if (binding.equals("Right")) {                   
                     ball_phy[0].applyForce(new Vector3f(3f*ballSpeed[0], 0, 0),new Vector3f(3f*ballSpeed[0], 0, 0));
                 } else if (binding.equals("Up")) {                    
                     ball_phy[0].applyForce(new Vector3f(0, 0, -3f*ballSpeed[0]),new Vector3f(0, 0, -3f*ballSpeed[0]));
                 } else if (binding.equals("Down")) {
                     ball_phy[0].applyForce(new Vector3f(0, 0, 3f*ballSpeed[0]),new Vector3f(0, 0, 3f*ballSpeed[0]));
                 }                       
                 //player 2 action listener
            
               if (binding.equals("Left2")) {                   
                     ball_phy[1].applyForce(new Vector3f(-3f*ballSpeed[1], 0, 0),new Vector3f(-3f*ballSpeed[1], 0, 0)); // push the ball foward
                 } else if (binding.equals("Right2")) {                    
                     ball_phy[1].applyForce(new Vector3f(3f*ballSpeed[1], 0, 0),new Vector3f(3f*ballSpeed[1], 0, 0));
                 } else if (binding.equals("Up2")) {                    
                     ball_phy[1].applyForce(new Vector3f(0, 0, -3f*ballSpeed[1]),new Vector3f(0, 0, -3f*ballSpeed[1]));
                 } else if (binding.equals("Down2")) {                  
                     ball_phy[1].applyForce(new Vector3f(0, 0, 3f*ballSpeed[1]),new Vector3f(0, 0, 3f*ballSpeed[1]));
                 }      
               
               //player 3 action listener
               if (binding.equals("Left3") && numberPlayer >= 3) {                
                     ball_phy[2].applyForce(new Vector3f(-3f*ballSpeed[2], 0, 0),new Vector3f(-3f*ballSpeed[2], 0, 0)); // push the ball foward
                 } else if (binding.equals("Right3")&& numberPlayer >= 3) {                    
                     ball_phy[2].applyForce(new Vector3f(3f*ballSpeed[2], 0, 0),new Vector3f(3f*ballSpeed[2], 0, 0));
                 } else if (binding.equals("Up3")&& numberPlayer >= 3) {                   
                     ball_phy[2].applyForce(new Vector3f(0, 0, -3f*ballSpeed[2]),new Vector3f(0, 0, -3f*ballSpeed[2]));
                 } else if (binding.equals("Down3")&& numberPlayer >= 3) {
                     ball_phy[2].applyForce(new Vector3f(0, 0, 3f*ballSpeed[2]),new Vector3f(0, 0, 3f*ballSpeed[2]));
                 } 
                //player 4 action listener
               if (binding.equals("Left4") && numberPlayer == 4) {  
                     ball_phy[3].applyForce(new Vector3f(-3f*ballSpeed[3], 0, 0),new Vector3f(-3f*ballSpeed[3], 0, 0)); // push the ball foward
                 } else if (binding.equals("Right4") && numberPlayer == 4) {
                     ball_phy[3].applyForce(new Vector3f(3f*ballSpeed[3], 0, 0),new Vector3f(3f*ballSpeed[3], 0, 0));
                 } else if (binding.equals("Up4")&& numberPlayer == 4) {
                     ball_phy[3].applyForce(new Vector3f(0, 0, -3f*ballSpeed[3]),new Vector3f(0, 0, -3f*ballSpeed[3]));
                 } else if (binding.equals("Down4")&& numberPlayer == 4) {
                     ball_phy[3].applyForce(new Vector3f(0, 0, 3f*ballSpeed[3]),new Vector3f(0, 0, 3f*ballSpeed[3]));
                 }
 
         }
     };     
     
     private ActionListener actionListener = new ActionListener() {
     public void onAction(String name, boolean keyPressed, float tpf) {
         if (name.equals("Ability1") && !keyPressed && !onCooldown(abilityMapping,0)) {
             actAbility(0);               
         } else if (name.equals("Ability2") && !keyPressed && !onCooldown(abilityMapping,1)) {
             actAbility(1);
         } else if (name.equals("Ability3") && !keyPressed && !onCooldown(abilityMapping,2)) {
             if(numberPlayer >=3){
                 actAbility(2);
             }
         } else if (name.equals("Ability4") && !keyPressed && !onCooldown(abilityMapping,3)) {
             if(numberPlayer==4){
                 actAbility(3);
             }
         }     
     }
   };    
     //looks for the player's ability
     public void actAbility(int i) {
         if (abilityFromUI[i] == 1) {
             dash(i);
         }
         else if (abilityFromUI[i] == 2) {
             jump(i);
         }
         else if (abilityFromUI[i] == 3) {
             glue(i);
         }
         else if (abilityFromUI[i] == 4) {
             forcePush(i);
         }
         else if (abilityFromUI[i] == 5) {
             ghost(i);
         }
         else if (abilityFromUI[i] == 6) {
             blink(i);
         }
         else if (abilityFromUI[i] == 7) {
             mine(i);
         }
     }
     
     public void dash(int i) {
         Vector3f v = ball_phy[i].getLinearVelocity();
             if(v.x != 0 && v.z!=0){
                 ball_phy[i].applyImpulse(new Vector3f(v.x*2,0,v.z*2), Vector3f.ZERO);
             }
     }
 
     public void jump(int i) {
         ball_phy[i].applyImpulse(new Vector3f(0, 7f, 0), Vector3f.ZERO );
     }
     
     public void mine(int i) {
         Sphere c = new Sphere(5, 5, 0.2f, true, false);
         c.setTextureMode(Sphere.TextureMode.Projected);
         TangentBinormalGenerator.generate(c);
         mine = new Geometry("Mine",c);
         Material min_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
         TextureKey key2 = new TextureKey("Textures/Terrain/Rock/Rock.PNG");
         key2.setGenerateMips(true);
         min_mat.setBoolean("UseMaterialColors",true);   
         min_mat.setColor("Specular",ColorRGBA.Blue);
         min_mat.setColor("Diffuse",ColorRGBA.Blue);
         mine.setMaterial(min_mat);
         rootNode.attachChild(mine);
         mine.setLocalTranslation(ball_phy[i].getPhysicsLocation());
         mine_phy = new RigidBodyControl(1f);
         mine.addControl(mine_phy);
         mine.addControl(ghost);
         bulletAppState.getPhysicsSpace().add(ghost);
         bulletAppState.getPhysicsSpace().add(mine);
         mine_phy.setMass(1000f);
         mine_phy.setDamping(0f, 1f); 
         mine_phy.setGravity(new Vector3f(0f,-10f,0f));
         mine_phy.setRestitution(3f);
         mineCnt++;
     }
     
     public void ghost(int i) {
         if(isghost)
                 {
                     isghost=false;
                     ball[i].setMaterial(mat_road);
                 }
                 else
                 {   
                     isghost=true;
                     gnode.addControl(ghosts);
                     rootNode.attachChild(gnode);            
                     mat_ghost= new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                     mat_ghost.setColor("Color", new ColorRGBA(0,0,0,0.5f));
                     mat_ghost.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);
                     ball[i].setMaterial(mat_ghost);
                     ball[i].setQueueBucket(Bucket.Transparent);
                     bulletAppState.getPhysicsSpace().add(ghosts);
                 }
     }
     
     
     public void forcePush(int i) {
         Ray push = new Ray(ball_phy[i].getPhysicsLocation(), ball_phy[i].getLinearVelocity()); 
         CollisionResult cr;
         CollisionResults results= new CollisionResults();
         int x=1;
         results.clear();
         rootNode.collideWith(push, results);              
                
         while(x<results.size())
         {
             cr = results.getCollision(x);
             cr.getGeometry().getControl(RigidBodyControl.class).applyImpulse(
                     push.getDirection(), Vector3f.ZERO);
             x++;
          }
     }
     
     public void blink(int i) {
         Vector3f temp = ball[i].getLocalTranslation();
         //System.out.println(ball_phy[i].getLinearVelocity().x+ball_phy[i].getLinearVelocity().z);
         temp.add(ball_phy[i].getLinearVelocity().mult(3));
         //ball[i].setLocalTranslation(temp);
         ball_phy[i].setPhysicsLocation(temp);
     }
     
     public void glue(int i) {
         ball_phy[i].setLinearVelocity(new Vector3f(0,0,0));
     }
     
     // Listens to collision between objects
     private PhysicsCollisionListener physicsCollisionListener = new PhysicsCollisionListener(){        
         public void collision(PhysicsCollisionEvent event) {
             if (event.getNodeA().getName().equals("Ball 1") || event.getNodeB().getName().equals("Ball 1 ")) {                
                 if (event.getNodeA().getName().equals("Ball 2") || event.getNodeB().getName().equals("Ball 2")) {
                     System.out.println("Ouch my balls!!!!!!!!"); 
                 }    
             }          
         }
     };  
 
     
     @Override
     public void simpleUpdate(float tpf) {
         
         if(isScreenEnd){
               isScreenEnd = false;
               InitObj();
         }
         loc = new Vector3f [numberPlayer];
         //Check if ball is within the map; if not, destroy the ball
         if(isRunning == true){      
             deathTimer();
             for(int i = 0; i < numberPlayer; i++){
                 loc[i] = ball_phy[i].getPhysicsLocation();
         }
 
       
     
       //Update ghost node
         int z=0;
         if(isghost && numberPlayer == 4)
         {
            gnode.move(ball_phy[3].getPhysicsLocation());
            while(z<ghosts.getOverlappingCount())
            {
               List k = ghosts.getOverlappingObjects();
 
               if(k.get(z).equals(ball_phy[0])||k.get(z).equals(ball_phy[1])||k.get(z).equals(ball_phy[2]))
               {
                   ball_phy[3].removeCollideWithGroup(2);
               }
               z++;
            }
         }
         else if(!isghost && numberPlayer==4)
         {
             ball_phy[3].addCollideWithGroup(2);
         }
         
         if(loc[0].y < 0 && isBall1Alive){          
             isBall1Alive = false;  
             ballLeft--;
             if(!isBall2Alive || !isBall3Alive || !isBall4Alive){
               ch.detachAllChildren();
             }
             ch = new BitmapText(guiFont, false);  
             ch.setSize(guiFont.getCharSet().getRenderedSize() * 5);
             ch.setText("Ball 1 DEAD");       
             ch.setColor(ColorRGBA.Red);
             ch.setLocalTranslation(
             settings.getWidth() / 3f,
             settings.getHeight() / 4f, 0);
             guiNode.attachChild(ch);
             bulletAppState.getPhysicsSpace().remove(ball_phy[0]);
             rootNode.detachChild(ball[0]);
         } 
         else if(loc[1].y < 0 && isBall2Alive){
             isBall2Alive = false;            
             ballLeft--;
             if(!isBall1Alive || !isBall3Alive || !isBall4Alive){
               ch.detachAllChildren();
             }
 
             ch = new BitmapText(guiFont, false); 
             ch.setSize(guiFont.getCharSet().getRenderedSize() * 5);
             ch.setText("Ball 2 DEAD");        
             ch.setColor(ColorRGBA.Red);
             ch.setLocalTranslation(
             settings.getWidth() / 3f,
             settings.getHeight() / 4f, 0);
             guiNode.attachChild(ch);
             bulletAppState.getPhysicsSpace().remove(ball_phy[1]);
             rootNode.detachChild(ball[1]);
         }
         else if(numberPlayer>=3){
             if(loc[2].y < 0 && isBall3Alive){
                 isBall3Alive = false;            
                 ballLeft--;
                 if(!isBall2Alive || !isBall1Alive || !isBall4Alive){
                   ch.detachAllChildren();
                 }
 
                 ch = new BitmapText(guiFont, false); 
                 ch.setSize(guiFont.getCharSet().getRenderedSize() * 5);
                 ch.setText("Ball 3 DEAD");        
                 ch.setColor(ColorRGBA.Red);
                 ch.setLocalTranslation(
                 settings.getWidth() / 3f,
                 settings.getHeight() / 4f, 0);
                 guiNode.attachChild(ch);
                 bulletAppState.getPhysicsSpace().remove(ball_phy[2]);
                 rootNode.detachChild(ball[2]);   
             }
             else if(numberPlayer==4){
                 if(loc[3].y < 0 && isBall4Alive){
                 isBall4Alive = false;      
                 ballLeft--;
                 if(!isBall2Alive || !isBall1Alive || !isBall3Alive){
                     ch.detachAllChildren();
                 }
                 ch = new BitmapText(guiFont, false); 
                 ch.setSize(guiFont.getCharSet().getRenderedSize() * 5);
                 ch.setText("Ball 4 DEAD");        
                 ch.setColor(ColorRGBA.Red);
                 ch.setLocalTranslation(
                 settings.getWidth() / 3f,
                 settings.getHeight() / 4f, 0);
                 guiNode.attachChild(ch);
                 bulletAppState.getPhysicsSpace().remove(ball_phy[3]);
                 rootNode.detachChild(ball[3]);
                 }    
             }
         }
       
       }
       if(ballLeft==1 && !gameEnd){
             if(isBall1Alive){
                 bulletAppState.getPhysicsSpace().remove(ball_phy[0]);         
             }
             else if(isBall2Alive){
                 bulletAppState.getPhysicsSpace().remove(ball_phy[1]);         
             }
             else if(isBall3Alive){
                 bulletAppState.getPhysicsSpace().remove(ball_phy[2]);         
             }
             else if(isBall4Alive){
                 bulletAppState.getPhysicsSpace().remove(ball_phy[3]);         
             }
             gameEnd = true;
             isRunning = false;
             isScreenEnd = false;
             destroyObj();
             InitGUI();
       }      
   }
     
     // destroy objects
     private void destroyObj(){
         for (int i = counter; i < boardLength*boardWidth; i++) {
             destroyMap(i);
         }
         rootNode.detachAllChildren(); 
         counter = 0;        
         ch.detachAllChildren();        
         if(mineCnt > 0){
             bulletAppState.getPhysicsSpace().remove(mine_phy);
         }
     }    
     
      public void initAbilities(long[] temp) {
         for (int i = 0; i < numberPlayer; i++) {
             temp[i] = System.nanoTime()/1000000000;
         }
     }
     
     // damage buff
     public void buffDamage(int player, boolean on) {
         if (on) {
            ball_phy[player].setRestitution(1f);
            ball_phy[player].setMass(1.7f);
            ballSpeed[player] = 2;
         }
         else {
             ball_phy[player].setRestitution(1);
             ball_phy[player].setMass(1);
             ballSpeed[player] = 1;
         }
     }
     
     //Speed buff
      public void buffSpeed(int player, boolean on) {
          if (on) {
             ballSpeed[player] = 2;
          }
          else {
             ballSpeed[player] = 1;
          }
      }
 
      //Juggernaut buff
      public void buffJugg(int player, boolean on) {//Juggarnaut ability
          if (on) {
             ball_phy[player].setRestitution(.5f);
             ball_phy[player].setMass(2f);
             ballSpeed[player] = 2;
          }
          else {
             ball_phy[player].setRestitution(1);
             ball_phy[player].setMass(1);
             ballSpeed[player] = 1;   
          }
      }
 
      //Size buff
      public void buffSize(int player, boolean on) {
          if (on) {
             ball[player].scale(2);
             ball_phy[player].setMass(2);
          }
          else {
             ball[player].scale(1f);
             ball_phy[player].setMass(1);
          }
      }
     
     public boolean onCooldown(long[] temp, int i) {
         if (temp[i] < (System.nanoTime()/1000000000)) {
             temp[i] = (System.nanoTime()/1000000000) + 5;
             return false;
         }
         else {
             return true;
         }
     }
       public void createFloor(int j) {
         int i = 0;
         int x = boardLength-1;
         int y = boardWidth-1;
         int dx = x;
         int dy = y - 1;
         int xlim = 0;
         int ylim = 0;
         int check = 0;
         Vector3f coord = new Vector3f(0,0,0);
         
         //Create Obj array if current map has objects to load    
         if (currentmap > 1) {
             mapObj = new Spatial[20];
             objNum = 0;
         }
         
         //populate populate map in a spiral fashion
         while (i < boardLength*boardWidth) {
             //Create a Tile
             makeMapTile(coord.set((boardLength-x-1), -.1f, 
                     (boardWidth-y-1)), i++);
         
             /*Algorithm for spiral: Move straight until 
              * it reaches a corner, then turn Right and continue 
              * until all blocks filled*/
             if (check == 0) {
                 if (x == xlim) {
                     check = 1;
                     y--;
                     continue;
                 }
                 x--;
             }
             else if (check == 1) {
                 if (y == ylim){
                     check = 2;
                     x++;
                     continue;
                 }
                 y--;
             }
             else if (check == 2) {
                 if (x == dx) {
                     check = 3;
                     y++;
                     dx--;
                     xlim++;
                     continue;
                 }
                 x++;
             }
             else if (check == 3) {
                 if (y == dy) {
                     check = 0;
                     x--;
                     dy--;
                     ylim++;
                     continue;
                 }
                 y++;
             }
         }
         objNum = 0;
     }
     
     //create a Map tile with randomly generated terrain to attach to Map
     public void makeMapTile(Vector3f loc, int i) {
         Box box2 = new Box( Vector3f.ZERO, 1f,.1f,1f);
         Geometry tile = new Geometry("Box", box2);
         Material mat2 = new Material(assetManager,
                 "Common/MatDefs/Misc/Unshaded.j3md");
         TextureKey key = loadTexture(maptexture[i]);
         key.setGenerateMips(true);
         Texture tex = assetManager.loadTexture(key);
         tex.setWrap(Texture.WrapMode.Repeat);
         mat2.setTexture("ColorMap",tex);
         tile.setMaterial(mat2);
         tile.setLocalTranslation(loc.mult(2));
         tile.addControl(new RigidBodyControl(0.0f));
         //tile.getControl(RigidBodyControl.class).setKinematic(false);
         bulletAppState.getPhysicsSpace().add(tile);
         rootNode.attachChildAt(tile, i);
         
         if (maptexture[i] == 0) {
            tile.getControl(RigidBodyControl.class).setFriction(10f);
            tile.getControl(RigidBodyControl.class);
         }
         else if (maptexture[i] == 1) {
             tile.getControl(RigidBodyControl.class).setFriction(100f);
             tile.getControl(RigidBodyControl.class);
         }
         else if (maptexture[i] == 2) {
             tile.getControl(RigidBodyControl.class).setEnabled(false);
         }
         else {
             tile.getControl(RigidBodyControl.class).setFriction(100f);
             tile.getControl(RigidBodyControl.class);
         }
         
         loadObject(loc,mapobjects[i],tile);
 
     }
     
     //Load the map we want
     public int[] selectedMapTexture(int i) {
         int[] temp;
         if (i == 0) {
             temp = map1;
             boardLength = 12;
             boardWidth = 8;
         }
         else if (i == 1) {
             temp = map2;
             boardLength = 11;
             boardWidth = 8;
         }
         else if (i == 2) {
             temp = map3;
             boardLength = 11;
             boardWidth = 8;
         }
         else if (i == 3) {
             temp = map4;
             boardLength = 11;
             boardWidth = 8;
         }
         else {
             temp = map5;
             boardLength = 11;
             boardWidth = 8;
         }
         return temp;
     }
     
     //Load the object map we want
     public int[] selectedMapObject(int i) {
         int[] temp;
         if (i == 0) {
             temp = obj1;
         }
         else if (i == 1) {
             temp = obj2;
         }
         else if (i == 2) {
             temp = obj3;
         }
         else if (i == 3) {
             temp = obj4;
         }
         else {
             temp = obj5;
         }
         return temp;
     }
     
     //Return the texture for the map
     public TextureKey loadTexture(int i) {
         TextureKey key;
         if (i == 0) {
             key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
         }
         else if (i == 1) {
             key = new TextureKey("Textures/Terrain/Pond/Pond.jpg");
         }
         else if (i == 2) {
             key = new TextureKey("Textures/lava.png");            
         }
         else {
             key = new TextureKey("Textures/Terrain/splat/dirt.jpg");
         }
         return key;
     }
     
         //Attach an object to the if it exsist on the selected map
     public void loadObject(Vector3f loc, int i, Geometry j) {
         Spatial temp;
         if (i == 4) {
             temp = assetManager.loadModel("Models/stone_felsite7.j3o");
             temp.scale(.3f);
         }
         else if (i == 5) {
             temp = assetManager.loadModel("Models/ground_pallisade.j3o");
             temp.scale(2f);
         }
         else if (i == 6) {
             temp = assetManager.loadModel("Models/ramp.j3o");
             temp.rotate(0, 29.8f, 0);
         }
         else if (i == 7) {
             temp = assetManager.loadModel("Models/ramp.j3o");
             temp.rotate(0, 45.55f, 0);
         }
         else if (i == 8) {
             temp = assetManager.loadModel("Models/barrel.j3o");
             temp.scale(2);
         }
         else {
             return;
         }
         loc.y = 0;
         temp.setLocalTranslation(loc.mult(2));
         mapObj[objNum++] = temp;
         rootNode.attachChild(temp);
         temp.addControl(new RigidBodyControl(0f));
         bulletAppState.getPhysicsSpace().add(temp.getControl(RigidBodyControl.class));
         if (i== 6||i==7) {
             temp.getControl(RigidBodyControl.class).setFriction(-.5f);
         }
     }
     
     //Destory Object on the map when tile is destoryed
     public void onDestroy(int i) {
         if (mapobjects[i] > 0 && currentmap > 1) {
             mapObj[objNum].getControl(RigidBodyControl.class).setMass(10f);
             mapObj[objNum].getControl(RigidBodyControl.class).setGravity(
                 new Vector3f(0f,-10f,0f));
             mapObj[objNum].getControl(RigidBodyControl.class).activate();
             mapObj[objNum].getControl(RigidBodyControl.class).setLinearVelocity(new Vector3f(0,4,0));
             objNum++;
         }
     }
     
     //Destory a piece of the Map
     public void destroyMap(int i) {
         rootNode.getChild(i).getControl(RigidBodyControl.class).setMass(10f);
         rootNode.getChild(i).getControl(RigidBodyControl.class).setGravity(
                 new Vector3f(0f,-10f,0f));
         rootNode.getChild(i).getControl(RigidBodyControl.class).setEnabled(true);
         rootNode.getChild(i).getControl(RigidBodyControl.class).activate();
         rootNode.getChild(i).getControl(
                 RigidBodyControl.class).setLinearVelocity(new Vector3f(0,4,0));        
         onDestroy(i);
     }
     
     //Timer to destory the Map
     public void deathTimer() {
         if (deathClk < (System.nanoTime()/1000000000) && counter < boardLength*boardWidth) {
             destroyMap(counter);
             deathClk = (System.nanoTime()/1000000000) + 5;
             counter++;
             
         }
     }
     
     // GUI
     private static Screen openGameSettingScreen(final Nifty nifty) {
       final CommonBuilders common = new CommonBuilders();
       Screen screen = new ScreenBuilder("settings") {
 
         {        
           controller(new JmeScreenController(
                   "menuButtonDialog1", "dialog1",
                   "menuButtonDialog2", "dialog2"){
               @Override
               public void onEndScreen(){
                   app.isScreenEnd = true;
                   
               }        
           });
 
           inputMapping("de.lessvoid.nifty.input.mapping.DefaultInputMapping"); // this will enable Keyboard events for the screen controller
           layer(new LayerBuilder("layer") {
 
             {
               backgroundImage("Interface/Penguins.jpg");
               childLayoutVertical();
               panel(new PanelBuilder("navigation") {
 
                 {
                   width("100%");
                   height("80px");
                   backgroundColor("#5588");
                   childLayoutHorizontal();
                   padding("50px");
 
                   control(MenuButtonControlDefinition.getControlBuilder("menuButtonDialog1", "Game Set-up", "Game Settings"));
                   panel(builders.hspacer("20px"));
                   control(MenuButtonControlDefinition.getControlBuilder("menuButtonDialog2", "Customize Game", "Customize your ball, select a map, etc"));
 
                 }
               });
               panel(new PanelBuilder("dialogParent") {
                 {
                   childLayoutOverlay();
                  width("100%");
                   alignCenter();
                   valignCenter();
                   control(new ControlBuilder("dialog1", GameSettingControlDefinition.NAME));
                   control(new ControlBuilder("dialog2", GameCustomizeControlDefinition.NAME));
                 }
               });
             }
           });
           layer(new LayerBuilder() {
             {
               childLayoutVertical();
               panel(new PanelBuilder() {
                 {
                   height("*");
                 }
               });
               panel(new PanelBuilder() {
                 {
                   childLayoutCenter();
                   height("15px");
                   width("100%");
                   backgroundColor("#5588");
                 }
               });
                           panel(new PanelBuilder() {
 
               {
                 childLayoutCenter();
                 height("50px");
                 width("100%");
                 backgroundColor("#5588");
                 panel(new PanelBuilder() {
 
                   {
                     paddingLeft("25px");
                     paddingRight("25px");
                     height("50%");
                     width("100%");
                     alignCenter();
                     valignCenter();
                     childLayoutHorizontal();
 
                     panel(common.hspacer("7px"));
                     control(new ButtonBuilder("quitGameButton", "Quit") {
 
                       {
                       }
                     });
                     panel(common.hspacer("*"));
                     control(new ButtonBuilder("startGameButton", "Start Game!") {
 
                       {
                       }
                     });
                   }
                 });
               }
             });
             }
           });
                   layer(new LayerBuilder("whiteOverlay") {
             {
               onCustomEffect(new EffectBuilder("renderQuad") {
                 {
                   customKey("onResolutionStart");
                   length(350);
                   neverStopRendering(false);
                 }
               });
               onStartScreenEffect(new EffectBuilder("renderQuad") {
                 {
                   length(300);
                   effectParameter("startColor", "#ddff");
                   effectParameter("endColor", "#0000");
                 }
               });
               onEndScreenEffect(new EffectBuilder("renderQuad") {
                 {
                   length(300);
                   effectParameter("startColor", "#0000");
                   effectParameter("endColor", "#ddff");
                 }
               });
             }
           });          
         }
       }.build(nifty);
       return screen;
     }
 
     private static void registerMenuButtonHintStyle(final Nifty nifty) {
       new StyleBuilder() {
         {
           id("special-hint");
           base("nifty-panel-bright");
           childLayoutCenter();
           onShowEffect(new EffectBuilder("fade") {
             {
               length(150);
               effectParameter("start", "#0");
               effectParameter("end", "#d");
               inherit();
               neverStopRendering(true);
             }
           });
           onShowEffect(new EffectBuilder("move") {
             {
               length(150);
               inherit();
               neverStopRendering(true);
               effectParameter("mode", "fromOffset");
               effectParameter("offsetY", "-15");
             }
           });
           onCustomEffect(new EffectBuilder("fade") {
             {
               length(150);
               effectParameter("start", "#d");
               effectParameter("end", "#0");
               inherit();
               neverStopRendering(true);
             }
           });
           onCustomEffect(new EffectBuilder("move") {
             {
               length(150);
               inherit();
               neverStopRendering(true);
               effectParameter("mode", "toOffset");
               effectParameter("offsetY", "-15");
             }
           });
         }
       }.build(nifty);
       new StyleBuilder() {
         {
           id("special-hint#hint-text");
           base("base-font");
           alignLeft();
           valignCenter();
           textHAlignLeft();
           color(new Color("#000f"));
         }
       }.build(nifty);
     }
 
     private static void registerConsolePopup(Nifty nifty) {
       new PopupBuilder("consolePopup") {
         {
             
           childLayoutAbsolute();
           panel(new PanelBuilder() {
             {
               childLayoutCenter();
               width("100%");
               height("100%");
               alignCenter();
               valignCenter();
               control(new ConsoleBuilder("console") {
                 {
                   width("80%");
                   lines(25);
                   alignCenter();
                   valignCenter();
                   onStartScreenEffect(new EffectBuilder("move") {
                     {
                       length(150);
                       inherit();
                       neverStopRendering(true);
                       effectParameter("mode", "in");
                       effectParameter("direction", "top");
                     }
                   });
                   onEndScreenEffect(new EffectBuilder("move") {
                     {
                       length(150);
                       inherit();
                       neverStopRendering(true);
                       effectParameter("mode", "out");
                       effectParameter("direction", "top");
                     }
                   });
                 }
               });
             }
           });
         }
       }.registerPopup(nifty);
     }    
 
     public void getInputFromGUI(){       
           if(JmeScreenController.getExitStatus()){
               app.stop();
           }
           numberPlayer = GameSettingController.getNumberPlayer();  
           ballLeft = numberPlayer;
           currentmap = GameCustomizeController.getCurrentMap();
           abilityFromUI = GameSettingController.getPlayerAbility();
           System.out.println("p1: " + abilityFromUI[0]);
           System.out.println("p2: " + abilityFromUI[1]);
           System.out.println("p3: " + abilityFromUI[2]);
           System.out.println("p4: " + abilityFromUI[3]);
           System.out.println("Map: " + currentmap);
     }
 
 }
