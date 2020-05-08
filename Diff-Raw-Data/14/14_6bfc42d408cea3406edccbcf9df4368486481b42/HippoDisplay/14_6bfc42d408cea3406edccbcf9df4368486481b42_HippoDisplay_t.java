 package FullGame;
 
 import de.lessvoid.nifty.builder.*;
 import de.lessvoid.nifty.*;
 import de.lessvoid.nifty.builder.PanelBuilder;
 import de.lessvoid.nifty.builder.ScreenBuilder;
 import de.lessvoid.nifty.controls.button.builder.ButtonBuilder;
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.elements.render.TextRenderer;
 import de.lessvoid.nifty.screen.*;
  
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.PhysicsCollisionObject;
 import com.jme3.bullet.collision.shapes.BoxCollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.input.KeyInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.light.AmbientLight;
 import com.jme3.light.PointLight;
 import com.jme3.light.DirectionalLight;
 import com.jme3.light.PointLight;
 import com.jme3.material.Material;
 import com.jme3.material.RenderState.BlendMode;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.renderer.queue.RenderQueue.Bucket;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.scene.shape.Sphere.TextureMode;
 import com.jme3.system.Timer;
 import com.jme3.input.*;
 import com.jme3.input.controls.*;
 /**Hungry Hungry Hippos.
  * 
  * @author vcoleman, rboy, alee, dvermilya
  * @author adapted from base code by double1984, updated by zathras
  */
 public class HippoDisplay extends SimpleApplication {
  
 	public static void main(String args[]) {
 	    HippoDisplay app = new HippoDisplay();
 	    app.start();
 	}
 
 	/**Nifty API*/
 	private Nifty nifty;
	private boolean isRunning= true;
  
 	/** Prepare the Physics Application State (jBullet) */
 	private BulletAppState bulletAppState;
  
 	/** Prepare Materials */
 	Material wall_mat;
 	Material red_ball_mat;
 	Material blue_ball_mat;
 	Material green_ball_mat;
 	Material hippo_mat;
  
 	/** Prepare geometries and physical nodes for bricks and cannon balls. */
 	private RigidBodyControl    ball_phy;
 	private HippoControl    hippo_phy;
 	private static final Sphere sphere;
 	private static final Box hippo;
 	private RigidBodyControl    wall_phy;
 	private Node hippo_node;
 	private Spatial hippo_geo;
 	  
 	 
 	/** dimensions used for transparent walls */
 	private static final float wallSide = 5f;
 	private static final float wallThickness = .2f;
 	
 	private int score = 0;
 	private Mouth mouth = new Mouth(1,1,1);
 	
 	private WebCam webcam;
 	private InterestPointFactory interestPoint;
 	
 	
 	private Timer timer = getTimer();
 	private int canEat = 0;
 	
 	static {
 		/** Initialize the marble geometry */
 		sphere = new Sphere(32, 32, 0.4f, true, false);
 		sphere.setTextureMode(TextureMode.Projected);
 		hippo = new Box(1,1,1);
 	}
  
   @Override
 	public void simpleInitApp() {
 		setDisplayFps(false);
 	  	setDisplayStatView(false);
 	  	flyCam.setEnabled(false);
 	  	inputManager.setCursorVisible(true);
 
 		/** GUI stuff */
 	    NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay (assetManager, inputManager, audioRenderer, guiViewPort);
 	    nifty = niftyDisplay.getNifty();
 	    guiViewPort.addProcessor(niftyDisplay);
 
 	     Screens s = new Screens();
 	    // <screen>
 	    nifty.addScreen("start", s.startScreen(nifty, this));
 	    nifty.addScreen("hud", s.hudScreen(nifty, this));
 	    nifty.addScreen("pause", s.pauseScreen(nifty, this));
 	    
 	    nifty.gotoScreen("start");
 
 	    /** Set up Physics Game */
 	    bulletAppState = new BulletAppState();
 	    stateManager.attach(bulletAppState);
 	    
 	    bulletAppState.getPhysicsSpace().setGravity(Vector3f.ZERO);
 	 
 	    /** Configure cam to look at scene */
 	    cam.setLocation(new Vector3f(0, 3*wallSide, 0));
 	    cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
 	    
 	    /** Initialize the scene, materials, and physics space */
 	    WorldGenerator world = new WorldGenerator();
 	    world.initWorld();
 	    interestPoint = new InterestPointFactory(mouth);
 	    webcam = new WebCam(new BinaryFactory(interestPoint));
 	}
    
    /* This is the update loop */
    @Override
    public void simpleUpdate(float tpf) {
        if (timer.getTime() > 200){
     	   hippo_phy.getRecentCollisions().timeOutList();
     	   timer.reset();
        }
 	   if (canEat>0) {
 		   addScore(hippo_phy.getRecentCollisions().eatBalls());
 		   canEat = 0;
        }
 	   //System.out.println("score: "+ score);
        mouth = interestPoint.myMouth;
        Vector3f hippo_pos = mouth.getPosition();
        hippo_pos.setY(0);
        //System.out.println(mouth.getPosition());
        hippo_pos = hippo_pos.mult(wallSide*3).subtract(new Vector3f(wallSide,wallSide,wallSide));
 
        if (hippo_pos.getZ()>wallSide) { hippo_pos.setZ(wallSide);}
        else if (hippo_pos.getZ()<-1*wallSide) { hippo_pos.setZ(-1*wallSide);}
 
        if (hippo_pos.getX()>wallSide) { hippo_pos.setX(wallSide);}
        else if (hippo_pos.getX()<-1*wallSide) { hippo_pos.setX(-1*wallSide);}
        
        hippo_node.setLocalTranslation(hippo_pos);
    }
    
    private void addScore(int i){
 	   score += i;
 	   Element niftyElement = nifty.getScreen("hud").findElementByName("score");
 	   niftyElement.getRenderer(TextRenderer.class).setText("Current Score: "+ score);
    }
    
    public int getScore(){
 	   return score;
    }
    
    private class WorldGenerator{
 	   
 	   public void initWorld() {
 	   		initLighting();
 		   	initMaterials();
 		    initWalls();
 		    initMarbles();
 		    initHippo();
 		    initKeys();
 	   }
 	   
 	   	/** Initialize the materials used in this scene. */
 		public void initMaterials() {
 			wall_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
 		    wall_mat.setColor("Color", new ColorRGBA(1,1,1,0.1f));
 		    wall_mat.getAdditionalRenderState().setBlendMode(BlendMode.Alpha);    		
 		  
 		    red_ball_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");  	
 		    red_ball_mat.setBoolean("UseMaterialColors", true);
 		    //red_ball_mat.setColor("Color", new ColorRGBA(1,0,0,0.01f));
 	    	red_ball_mat.setColor("Ambient", ColorRGBA.Red);   // ... color of this object
 	    	red_ball_mat.setColor("Diffuse", ColorRGBA.White);   // ... color of light being reflected
 	  	    
 		    blue_ball_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
 		    blue_ball_mat.setBoolean("UseMaterialColors",true);  // Set some parameters, e.g. blue.
 	    	blue_ball_mat.setColor("Ambient", ColorRGBA.Blue);   // ... color of this object
 	    	blue_ball_mat.setColor("Diffuse", ColorRGBA.White);   // ... color of light being reflected
 		   
 		    green_ball_mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
 		    green_ball_mat.setBoolean("UseMaterialColors",true);  // Set some parameters, e.g. blue.
 	    	green_ball_mat.setColor("Ambient", ColorRGBA.Green);   // ... color of this object
 	    	green_ball_mat.setColor("Diffuse", ColorRGBA.White);   // ... color of light being reflected
 	    	
 	    	//hippo_mat = assetManager.loadMaterial( "Materials/Frog/frogSkin.j3m");
 		    
 		}
 	 
 	  /** This loop builds a wall out of individual bricks. **/
 	public void initWalls() {
 	    Box facing = new Box(Vector3f.ZERO, wallSide, wallSide, wallThickness);
 	    Box side = new Box(Vector3f.ZERO, wallThickness, wallSide, wallSide);
 	    Box level = new Box(Vector3f.ZERO, wallSide, wallThickness, wallSide); 
 
 	    Vector3f front_loc = new Vector3f(0, 0, wallSide);
 	    makeWall(front_loc, facing);
 
 	    Vector3f back_loc = new Vector3f(0, 0, -1*wallSide);
 	    makeWall(back_loc, facing);
 
 	    Vector3f left_loc = new Vector3f(-1*wallSide, 0, 0);
 	    makeWall(left_loc, side);    
 	    
 	    Vector3f right_loc = new Vector3f(wallSide, -0, 0);
 	    makeWall(right_loc, side); 
 
 	    Vector3f top_loc = new Vector3f(0,wallSide,0);
 	    makeWall(top_loc, level);    
 
 	    Vector3f bottom_loc = new Vector3f(0,-1*wallSide,0);
 	    makeWall(bottom_loc, level);  
 	  }
 	 
 	/* This method creates one individual physical wall. */
 	  private void makeWall(Vector3f loc, Box orientedBox) {
 	    /** Create a brick geometry and attach to scene graph. */
 		Geometry wall_geo = new Geometry("Wall", orientedBox);
 	    wall_geo.setMaterial(wall_mat);
 	    wall_geo.setQueueBucket(Bucket.Transparent); 
 	    rootNode.attachChild(wall_geo);
 	    /** Position the brick geometry  */
 	    wall_geo.setLocalTranslation(loc);
 	    /** Make brick physical with a mass > 0.0f. */
 	    wall_phy = new RigidBodyControl(0.0f);
 	    /** Add physical brick to physics space. */
 	    wall_geo.addControl(wall_phy);
 	    bulletAppState.getPhysicsSpace().add(wall_phy);
 	    wall_phy.setRestitution(1f);
 	    wall_phy.setFriction(0f);
 	    wall_phy.setDamping(0f, 0f);
 	  }
 	  
 	  
 	  public void initMarbles(){
 		  ArrayList<Material> materials = new ArrayList<Material>();
 		  int index;
 	      Random randomGenerator = new Random();  
 		  materials.add(blue_ball_mat);
 		  materials.add(red_ball_mat);
 		  materials.add(green_ball_mat);
 		  for (int i=0;i<3;i++){
 			  for (int j=0;j<3;j++){		  
 				  for (int k=0;k<3;k++){
 					  Vector3f vel = new Vector3f(i, j, k).mult(2);
 					  Vector3f pos = new Vector3f(i,j,k);
 					  index = randomGenerator.nextInt(materials.size());
 					  makeMarble(vel, pos, materials.get(index));
 				  }
 			  }
 		  }
 	  }
 	 
 	  /** This method creates one individual physical marble. **/
 
 	   private void makeMarble(Vector3f vel_vect, Vector3f pos, Material mat) {
 	    /** Create a marble geometry and attach to scene graph. */
 	    Geometry ball_geo = new Geometry("marble", sphere);   
 	    ball_geo.setMaterial(mat);
 	    rootNode.attachChild(ball_geo);
 	    /** Position the marble  */  
 	    ball_geo.setLocalTranslation(pos);
 	    /** Make the ball physical with a mass > 0.0f */
 	    ball_phy = new RigidBodyControl(.001f);
 	    /** Add physical ball to physics space. */
 	    ball_geo.addControl(ball_phy);
 	    bulletAppState.getPhysicsSpace().add(ball_phy);
 	    /** Accelerate the physical ball to shoot it. */
 	    ball_phy.setLinearVelocity(vel_vect);
 	    ball_phy.setRestitution(1f);
 	    ball_phy.setFriction(0f);
 	    ball_phy.setDamping(0f, 0f);
 	  }
 	  
 	public void initHippo(){ 
 	    HashMap<String, Material> materials = new HashMap<String, Material>();
 		Material frog_mat = new Material(assetManager, "Common/MatDefs/Misc/ShowNormals.j3md");   	
 	    materials.put("frog_mat", frog_mat);
 
 		Material eye_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
 	    eye_mat.setColor("Color", ColorRGBA.White);	        
 	    materials.put("eye_mat", eye_mat);
 
 	    Material mouth_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
 	    mouth_mat.setColor("Color", new ColorRGBA(.5f, 0f,0f, 0f));		    
 	    materials.put("mouth_mat", mouth_mat);
 
 	    Material puple_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
 	    puple_mat.setColor("Color", ColorRGBA.Black);
 	    materials.put("puple_mat", puple_mat);
 		   
 		Frog frog = new Frog(materials);
 		hippo_node = frog.makeFrog();
 		   
 		rootNode.attachChild(hippo_node);
 		hippo_node.rotate(90*FastMath.DEG_TO_RAD, 0f, 180*FastMath.DEG_TO_RAD);
    		Vector3f hippo_loc = new Vector3f(3,3,3);
 		hippo_node.setLocalTranslation(hippo_loc);
 		hippo_phy = new HippoControl(new BoxCollisionShape(new Vector3f(1,1,1)),3, bulletAppState);
 		hippo_node.addControl(hippo_phy);
 		bulletAppState.getPhysicsSpace().add(hippo_phy);
 		
 
 	}
 	   
 	   public void initLighting(){
 		    
 		   
 		   AmbientLight al = new AmbientLight();
 		   al.setColor(ColorRGBA.White.mult(1.3f));
 		   rootNode.addLight(al);
 		   
 		   
 		
 		   PointLight lamp_light = new PointLight();
 		   lamp_light.setColor(ColorRGBA.White);
 		   lamp_light.setRadius(100f);
 		   lamp_light.setPosition(new Vector3f(1,1,1));
 		   rootNode.addLight(lamp_light);
 
 	   }
 	   
 	   private void initKeys() {
 			ActionListener actionListener = new ActionListener() {
 				public void onAction(String name, boolean keyPressed, float tpf) {
 					if (name.equals("Eating") && !keyPressed) {
 						canEat = 4;
 					}
 					if (name.equals("Pause") && !keyPressed) {
 						isRunning = !isRunning;
 						bulletAppState.setEnabled(isRunning);
 						if(!isRunning){
 							nifty.gotoScreen("pause");
 							//webcam.cleanupCapture();
 							Element niftyElement = nifty.getScreen("pause").findElementByName("score");
 							niftyElement.getRenderer(TextRenderer.class).setText("Current Score: "+ score);
 						}
 						else{
 							nifty.gotoScreen("hud");
 							//webcam.restart();
 						}
 					}
 				}
 			};
 		   
 		   inputManager.addMapping("Eating", new KeyTrigger(KeyInput.KEY_SPACE));
 		   inputManager.addMapping("Pause", new KeyTrigger(KeyInput.KEY_P));
 		   inputManager.addListener(actionListener,  new String[]{"Eating", "Pause"});
 	   }
 	   
 	   
    }
 
 }
 
 
 
