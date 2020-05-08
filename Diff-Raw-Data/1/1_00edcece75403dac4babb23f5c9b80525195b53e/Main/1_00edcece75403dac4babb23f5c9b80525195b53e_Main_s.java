 package mygame;
 
 import com.jme3.system.AppSettings;
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.asset.plugins.ZipLocator;
 import com.jme3.audio.AudioNode;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
 import com.jme3.bullet.collision.shapes.CollisionShape;
 //import com.jme3.bullet.control.CharacterControl;
 import com.jme3.bullet.control.RigidBodyControl;
 import com.jme3.bullet.util.CollisionShapeFactory;
 import com.jme3.collision.CollisionResult;
 import com.jme3.collision.CollisionResults;
 import com.jme3.font.BitmapText;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.light.AmbientLight;
 import com.jme3.light.DirectionalLight;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Ray;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.scene.shape.Sphere.TextureMode;
 import com.jme3.texture.Texture;
 import com.jme3.texture.Texture.WrapMode;
 import com.jme3.ui.Picture;
 import java.util.ArrayList;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.TimeUnit;
 
 /**
  * test
  * @author normenhansen
  */
 public class Main extends SimpleApplication implements ActionListener{
     
 
     private Node treasureChests;
     private Node shootables;
     private Spatial sceneModel;
     private BulletAppState bulletAppState;
     private RigidBodyControl landscape;
     private CharacterControl player;
     private Vector3f walkDirection = new Vector3f();
     private boolean left = false;
     private boolean right = false;
     private boolean up = false;
     private boolean down = false;
     private static final ScheduledExecutorService worker = 
             Executors.newSingleThreadScheduledExecutor();
     private ArrayList<Timer> tasks;
        // Prepare the Physics Application State (jBullet)
     
     // Prepare Materials
     Material wallMat;
     Material stoneMat;
     Material floorMat;
     
     // Prepare geometries and physical nodes for bricks and cannon balls.
     private RigidBodyControl brickPhy;
     private static final Box box;
     private RigidBodyControl ballPhy;
     private static final Sphere sphere;
     private RigidBodyControl floorPhy;
     private static final Box floor;
     
     // Dimensions used for bricks and walls
     private static final float brickLength = 0.48f;
     private static final float brickWidth = 0.24f;
     private static final float brickHeight = 0.12f;
     private static final int MAX_X = 1280;
     private static final int MAX_Y = 720;
     
     private int enemyNum = 500;
     private Vector3f[] direction = new Vector3f[enemyNum];
     private Geometry[] geom = new Geometry[enemyNum];
     
         //Make a timer for the bullet shots
     private int ammo;
     private boolean reload;
     
     private float timeElapsed;
     private int playerHealth;
     private int totalScore;
     
     private ArrayList<Spatial> aliveEnemies;
     private ArrayList<Spatial> deadFish;
     private Spatial fish;
     
     private BitmapText playerScoreBT;
     private Node playerScoreNode;
     private Node hudHolder;
     
     private BitmapText playerHealthTotal;
     private Node playerHealthNode;
     private Node hudHolder1;
     
     
     private AudioNode audio_gun;
     private AudioNode audio_water;
     
     
         static {
         /*
          * Initialize the cannon ball geometry
          */
         sphere = new Sphere(50, 50, 0.03f);
         sphere.setTextureMode(TextureMode.Projected);
 
         /*
          * Initialize the brick geometry
          */
         box = new Box(Vector3f.ZERO, brickLength, brickHeight, brickWidth);
         box.scaleTextureCoordinates(new Vector2f(1f, 0.5f));
         /*
          * Initialize the floor geometry
          */
         floor = new Box(Vector3f.ZERO, 10000f, 2.0f, 5000f);
         floor.scaleTextureCoordinates(new Vector2f(3, 6));
     }
 
     
     
     public static void main(String[] args) {
         Main app = new Main();
         AppSettings appSettings = new AppSettings(true);
     appSettings.setSettingsDialogImage("Splash/StartScreen.png");
     app.setSettings(appSettings);
         app.start();
     }
 
     @Override
     public void simpleInitApp() {
         
         timeElapsed = 0;
         playerHealth = 10;
         ammo = 12;
         reload = false;
         totalScore = 0;
         
         tasks = new ArrayList<Timer>();
         deadFish = new ArrayList<Spatial>();
         aliveEnemies = new ArrayList<Spatial>();
         
         
         // add a fish...
         fish = assetManager.loadModel("Models/fish1.obj");
 
         /*
          * Set up physics
          */
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
         
         initMaterials();
         initFloor();
         
         // We re-use the flyby camera for rotation, while positioning is handled by pyhsics
         viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
         flyCam.setMoveSpeed(100);
         setUpKeys();
         setUpLight();
         
         
         /*
          * We set up collision detection for the scene by creating a
          * compound collision shape and a static RigidBodyControl with mass zero.
          */
         shootables = new Node("shootables");
         rootNode.attachChild(shootables);
         loadTreasureChests();
 
         
         /*
          * We set up collision detection for the player by creating
          * a capsule collision shape and a CharacterControl.
          * The CharacterControl offers extra settings for
          * size, stepheight, jumping, falling, and gravity.
          * We also put the player i  its starting position.
          */
         CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(1.5f, 6f, 1);
         player = new CharacterControl(capsuleShape, 0.05f);
         player.setJumpSpeed(20);
         player.setFallSpeed(5);
         player.setGravity(30);
         player.setPhysicsLocation(new Vector3f(0, 60, 0));
         
         /*
          * We attach the scene and the player to the rootNode and the physics
          * space, to make them appear in the game world.
          */
         bulletAppState.getPhysicsSpace().add(player);
         
         setupPhysics();
         initAudio();
 
         hudHolder = new Node();
         playerScoreNode = new Node();
         playerScoreBT = new BitmapText(guiFont, false);
         
         hudHolder1 = new Node();
         playerHealthNode = new Node();
         playerHealthTotal = new BitmapText(guiFont, false);
         
         
         for(int i=0; i<enemyNum; i++)
 	{
             
             if ( i % 5 == 0) {
             	    Box box = new Box(Vector3f.ZERO, 1, 1, 1);
                 Geometry cube = new Geometry("EvilFish", box);
                 Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                 Texture texture = assetManager.loadTexture("Textures/fishPic.jpg");
                 mat1.setTexture("ColorMap", texture);
                 cube.setMaterial(mat1);
                 aliveEnemies.add(cube);
               
             } else if (i % 1 == 0) {
               Spatial fish = assetManager.loadModel("Models/fish1.obj");
                 Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
                 Texture texture = assetManager.loadTexture("Textures/PTERO_01.jpg");
                 mat.setTexture("ColorMap", texture);
                 fish.setMaterial(mat);
                 fish.scale(1f);	
                 fish.rotate(0, 55f, 0);
                 aliveEnemies.add(fish);
             } else {
                 Spatial fish = assetManager.loadModel("Models/seaweed.obj");
                 Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
                 Texture texture = assetManager.loadTexture("Textures/PTERO_01.jpg");
                 mat.setTexture("ColorMap", texture);
                 fish.setMaterial(mat);
                 fish.scale(2f);	
                 aliveEnemies.add(fish);
             }
             
 
         
         }
 
   	for(int j=0; j<enemyNum; j++)
         {
             int x = FastMath.nextRandomInt(-600, 600);
             int y = FastMath.nextRandomInt(0, 60);
             int z = FastMath.nextRandomInt(-600, 600);
             aliveEnemies.get(j).move(x, y, z);
             shootables.attachChild(aliveEnemies.get(j));
             
             
             
                  Spatial sw = assetManager.loadModel("Models/seaweed.obj");
                 Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
                 Texture texture = assetManager.loadTexture("Textures/green.png");
                 mat.setTexture("ColorMap", texture);
                 sw.setMaterial(mat);
                 sw.scale(75f);	
                 
                 sw.move(new Vector3f(FastMath.nextRandomInt(-600,600),  2f, FastMath.nextRandomInt(-600,600)));
                 rootNode.attachChild(sw);
                 
                                  Spatial sw2 = assetManager.loadModel("Models/seaweed2.obj");
                 Material mat2 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
                 Texture texture2 = assetManager.loadTexture("Textures/green.png");
                 mat2.setTexture("ColorMap", texture2);
                 sw2.setMaterial(mat2);
                 sw2.scale(0.5f);	
                 
                 sw2.move(new Vector3f(FastMath.nextRandomInt(-1000,1000),  2f, FastMath.nextRandomInt(-1000,1000)));
                 rootNode.attachChild(sw2);
         }
         
     }
 
     /**
      * This is the main event loop--walking happens here.
      * We check in which direction the player is walking by interpreting
      * the camera direction forward (camDir) and to the side (camLeft).
      * The setWalkDirection() command is what lets a physics-controlled player
      * walk.  We also make sure here that the camera moves with the player.
      */
     @Override
     public void simpleUpdate(float tpf) {
         
                    if (guiNode.hasChild(hudHolder1)){
                        hudHolder.detachChildNamed("hud1");
                    }
                     String str2 = playerHealth + "";
                     BitmapText hudText1 = new BitmapText(guiFont, false);          
                     hudText1.setSize(30);      // font size
                     hudText1.setColor(ColorRGBA.Red);                             // font color
                     hudText1.setText("Health: " + str2);             // the text
                     hudText1.setLocalTranslation(60, 600, 0); // position
                     hudText1.setName("hud1");
                     hudHolder.attachChild(hudText1);
                     guiNode.attachChild(hudHolder1);
         
         for ( int i = 0; i < tasks.size(); i++ ) {
             
             Timer t = tasks.get(i);
             t.timeElapsed += tpf;
             if (t.timeElapsed > t.delayTime) {
                 // Call method
                 if (t.task.equals("removePicture")) {
                     removePicture("Score");
                 }
                 tasks.remove(t);
             }
         }
         
         
         Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
         Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
         walkDirection.set(0, 0, 0);
         if (left && !isTreasureChest()) {
             walkDirection.addLocal(camLeft);
         }
         if (right && !isTreasureChest()) {
             walkDirection.addLocal(camLeft.negate());
         }
         if (up && !isTreasureChest()) {
             walkDirection.addLocal(camDir);
         }
         if (down && !isTreasureChest()) {
             walkDirection.addLocal(camDir.negate());
         }
         player.setWalkDirection(walkDirection);
         cam.setLocation(player.getPhysicsLocation());
         
         for( Spatial enemy: aliveEnemies) {
             
             if (enemy.getName().equals("EvilFish")) {
                 Vector3f vec = player.getPhysicsLocation().subtract(enemy.getLocalTranslation()).normalize().mult(9);
                 if (enemy.getLocalScale().distance(cam.getLocation()) > 5.5) {
 //                System.out.println(enemy.getLocalScale().distance(cam.getLocation()));
                 enemy.move(vec.mult(tpf));
 //                System.out.println("You have Not been hit!!\n\n");
             } else {
                 enemy.move(vec.mult(-tpf));
                 changeHealth(-1);
                 System.out.println("You have been hit!!\n\n");
             }
             } else {
              Vector3f vec = player.getPhysicsLocation().add(enemy.getLocalTranslation()).normalize().mult(1);   
              enemy.move(vec.mult(tpf));
             }
             
             
         }
         
         for (Spatial fish: deadFish) {
             Vector3f vec = new Vector3f(0,1000,0).subtract(fish.getLocalTranslation()).normalize().mult(5);
             fish.move(vec.mult(tpf/2));
         }
 
     }
     
     private boolean isTreasureChest() {
 //        System.out.println();
         for (Spatial chest: treasureChests.getChildren()) {
 
             if ( !isClose(chest.getLocalTranslation())) {
 //                return true;
             }
         }
         
         return false;
     }
     
     private boolean isClose(Vector3f loc) {
         
         return (int) (loc.x) == (int)(cam.getLocation().getX()) &&
                 (int) (loc.y) == (int) (cam.getLocation().getY()) &&
                 (int)(loc.z) == (int)(cam.getLocation().getZ());
     }
     
     /**
      * These are our custom actions triggered by key presses.
      * We do not walk yet, we just keep track of the direction the
      * user pressed.
      */
     public void onAction(String binding, boolean value, float tpf) {
         if (binding.equals("Left")) {
             left = value;
         } else if (binding.equals("Right")) {
             right = value;
         } else if (binding.equals("Up")) {
             up = value;
         } else if (binding.equals("Down")) {
             down = value;
         } else if (binding.equals("Jump")) {
             player.jump();
         } else if (binding.equals("Interact")) {
             // 1. Reset resulrs list.
             CollisionResults results = new CollisionResults();
             // 2. Aim the ray from loc to cam direction.
             Ray ray = new Ray(cam.getLocation(), cam.getDirection());
             // 3. Collect intersections betwen Ray and Shootables in results list. 
             treasureChests.collideWith(ray, results);
 
             // 5. Use the results (we mark the hit object)
             if (results.size() > 0) {
                 // The closest collision point is what was truly hit:
                 CollisionResult closest = results.getClosestCollision();
                 Geometry geo = closest.getGeometry();
                 if ( closest.getDistance() < 9.0f ) {                    
 
                     if (geo.getParent().equals(treasureChests)) {
 //                       geo.getMaterial().setColor("Color", ColorRGBA.Black);
                     geo.removeFromParent();
 //                    rootNode.attachChild(geo);
                     int xCoor = (MAX_X/2) - ((settings.getWidth() / 4) / 2);
                     int yCoor = (MAX_Y/2) - ((settings.getHeight() / 4) / 2);
                     setPicture("Materials/+500.png", "Score", xCoor, yCoor);
                     addToPlayerScore(500);
                     Timer task = new Timer(1, "removePicture");
                     tasks.add(task); 
                     }
                     
 
                 }
 
             }
         }
     }
 
     private void setUpKeys() {
         inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
         inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
         inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
         inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
         inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
         inputManager.addMapping("Interact", new KeyTrigger(KeyInput.KEY_E));
         inputManager.addMapping("shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
         inputManager.addMapping("reload", new KeyTrigger(KeyInput.KEY_R));
         inputManager.addListener(this, "Left");
         inputManager.addListener(this, "Right");
         inputManager.addListener(this, "Up");
         inputManager.addListener(this, "Down");
         inputManager.addListener(this, "Jump");
         inputManager.addListener(this, "Interact");
         inputManager.addListener(actionListener, "shoot");
         inputManager.addListener(actionListener, "reload");
     }
     
         /**
      * Every time the shoot action is triggered, a new cannon ball is produced.
      * The ball is set up to fly from the camera position in the camera direction.
      */
     private ActionListener actionListener = new ActionListener() {
 
         public void onAction(String name, boolean isPressed, float tpf) {
             if (name.equals("shoot")  && !isPressed) {
 
                 //Checks to see if you have anymore ammo
                 //if you do not then it shows you need to reload your gun
                 if(ammo==0 && !reload){
                     reload = true;
                    Picture pic = new Picture("HUD Picture");
                    pic.setImage(assetManager, "Textures/reload-button.png", true);
                    pic.setWidth(settings.getWidth()/4);
                    pic.setHeight(settings.getHeight()/4);
                    pic.setPosition(960, 0);
                    pic.setName("Reload");
                    guiNode.attachChild(pic);
 
                    
                 }
                 //If you have ammo then shoot! Depreciates the value by 1 everytime
                else if(!reload && ammo > 0){
                    
                    if(guiNode.hasChild(hudHolder)){
                        hudHolder.detachChildNamed("hud");
                    }
                     String strI = Integer.toString(ammo);
                     BitmapText hudText = new BitmapText(guiFont, false);          
                     hudText.setSize(30);      // font size
                     hudText.setColor(ColorRGBA.Red);                             // font color
                     hudText.setText("Bullets: " + strI);             // the text
                     hudText.setLocalTranslation(60, 665, 0); // position
                     hudText.setName("hud");
                     hudHolder.attachChild(hudText);
                     guiNode.attachChild(hudHolder);
                    
 
 
                    //   Reset results list.
                     CollisionResults results = new CollisionResults();
                     //  Aim the ray from cam loc to cam direction.
                     Ray ray = new Ray(cam.getLocation(), cam.getDirection());
                     //  Collect intersections between Ray and Shootables in results list.
                     rootNode.collideWith(ray, results);
                     
                     // 3. Collect intersections betwen Ray and Shootables in results list. 
                     shootables.collideWith(ray, results);
 
                     // 5. Use the results (we mark the hit object)
                     if (results.size() > 0) {
                         // The closest collision point is what was truly hit:
                         CollisionResult closest = results.getClosestCollision();
                         Geometry geo = closest.getGeometry();
                         if (geo.getParent().equals(shootables)) {
                             aliveEnemies.remove(geo);
                             int xCoor = (MAX_X/2) - ((settings.getWidth() / 4) / 2);
                             int yCoor = (MAX_Y/2) - ((settings.getHeight() / 4) / 2);
                             
                             Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
                             Texture texture = assetManager.loadTexture("Textures/fishPic.jpg");
                             mat1.setTexture("ColorMap", texture);
                             
                             if (geo.getName().equals("EvilFish") ) {
                                 if (!deadFish.contains(geo)) {
                                                                     setPicture("Materials/+1.png", "Score", xCoor, yCoor);
                                 Timer task = new Timer(1, "removePicture");
                                 tasks.add(task); 
                                 addToPlayerScore(1);
                                 deadFish.add(geo);
                                 Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
                                 texture = assetManager.loadTexture("Textures/deadFish.jpg");
                                 mat.setTexture("ColorMap", texture);
                                 geo.setMaterial(mat);
                                 }
 
                             } else {
                                 
                                 addToPlayerScore(-1);
                                 Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");  // create a simple material
                                 texture = assetManager.loadTexture("Materials/fish-skeleton.jpg");
                                 mat.setTexture("ColorMap", texture);
                                 geo.setMaterial(mat);
                                 deadFish.add(geo);
                                 setPicture("Materials/_1.png", "Score", xCoor, yCoor);
                                 Timer task = new Timer(1, "removePicture");
                                 tasks.add(task); 
                             }
                             
                             
                             
                         }
                         
 
                     }
                     
                     for (int i = 0; i < results.size(); i++) {
                     // For each hit, we know distance, impact point, name of geometry.
                     String hit = results.getCollision(i).getGeometry().getName();
                    //what is this doing here?? (git test comment) 
                     if(hit.toString().equalsIgnoreCase("Brick"))
                     {
                         
                     }
                     
                      }
 
                     makeBullet();
                     audio_gun.playInstance();
                     ammo = ammo - 1;
 
                 }
                 
                 
             }
             else if (name.equals("reload")  && !isPressed) {
 
                                 if(reload){
                     worker.schedule(loadAmmo, 1, TimeUnit.SECONDS);
                     guiNode.detachChildNamed("Reload");
                     audio_reload.playInstance();
                 }
                 else{
                    worker.schedule(loadAmmo, 1, TimeUnit.SECONDS);
                    audio_reload.playInstance();
                 }
             }
         }
     };
     
         Runnable loadAmmo = new Runnable() {
        public void run() {
          ammo = 12;
          reload = false;
        }
      };
         
          /**
      * 
      * This method will convert the playerscore int to string and then add 
      * it to the player score bitmap text
      * 
      */
     private void addToPlayerScore(int score){
         totalScore += score;
         String strng = totalScore + "";
         if(guiNode.hasChild(playerScoreNode))
         {
             guiNode.detachChildNamed("playerscore");
         }
         playerScoreBT.setText("Score: " +strng);
         playerScoreBT.setSize(30);      // font size
         playerScoreBT.setColor(ColorRGBA.Red);                // the text
         playerScoreBT.setLocalTranslation(60, 700, 0); // position
         playerScoreBT.setName("playerscore");
         playerScoreNode.attachChild(playerScoreBT);
         guiNode.attachChild(playerScoreNode);
     }
     
     private void changeHealth(int health) {
 //        playerHealth += health;
 //        String strng = playerHealth + "";
 //        if (guiNode.hasChild(playerHealthNode)) {
 //            guiNode.detachChildNamed("playerHealth");
 //        }
 //        playerHealthTotal.setText("Health: " + strng);
 //        playerHealthTotal.setSize(30);
 //        playerHealthTotal.setColor(ColorRGBA.Red);
 //        playerHealthTotal.setLocalTranslation(60, 600, 0);
 //        playerHealthTotal.setName("playerHealth");
 //        playerHealthNode.attachChild(playerHealthTotal);
 //        guiNode.attachChild(playerHealthNode);
     }
 
 
     private void setUpLight() {
         
         viewPort.setBackgroundColor(new ColorRGBA(0, 0, 128, 1.0f));
         // We add light so we see the scene
         AmbientLight al = new AmbientLight();
         al.setColor(ColorRGBA.White.mult(1.3f));
         rootNode.addLight(al);
         
         DirectionalLight dl = new DirectionalLight();
         dl.setColor(ColorRGBA.White);
         dl.setDirection(new Vector3f(2.8f, -2.8f, -2.8f).normalizeLocal());
         rootNode.addLight(dl);
     }
 
     /**
      * load up treasure chests
      */
     private void loadTreasureChests() {
         treasureChests = new Node("Treasure");
         rootNode.attachChild(treasureChests);
         treasureChests.attachChild(makeTreasureChest("Box1", 20f, 0f, 0f));
         treasureChests.attachChild(makeTreasureChest("Box2", 700f, 2f, 0f));
         treasureChests.attachChild(makeTreasureChest("Box3", 40f, 2f, 412f));
         for (int i = 0; i< 5; i ++) {
             treasureChests.attachChild(makeTreasureChest("Box" + (i + 4), FastMath.nextRandomInt(-1000, 1000), 0f, FastMath.nextRandomInt(-1000, 1000)));
         }
     }
     
     /** A cube object for target practice */
 	  protected Geometry makeCube(String name, float x, float y, float z) {
 	    Box box = new Box(new Vector3f(x, y, z), 1, 1, 1);
 	    Geometry cube = new Geometry(name, box);
 	    Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
 	    Texture texture = assetManager.loadTexture("Textures/zombie.jpg");
             mat1.setTexture("ColorMap", texture);
 	    cube.setMaterial(mat1);
 	    return cube;
 	  }
     
     /*
      * A Cube object for target practice
      */
     protected Geometry makeTreasureChest(String name, float x, float y, float z) {
         Box box = new Box(new Vector3f(x,y,z), 3, 3, 2);
         
         Geometry cube = new Geometry(name, box);
         Material mat1 = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         Texture texture = assetManager.loadTexture("Textures/panels.jpg");
         mat1.setTexture("ColorMap", texture);
         cube.setMaterial(mat1);
         return cube;
     }
     
     private void setPicture(String pictureLocation, String name, float xPosition, float yPosition) {
         Picture pic = new Picture("HUD Picture");
         pic.setImage(assetManager, pictureLocation, true);
         pic.setWidth(settings.getWidth()/4);
         pic.setHeight(settings.getHeight()/4);
         pic.setPosition(xPosition, yPosition);
         pic.setName(name);
         guiNode.attachChild(pic);
     }
     
     private void removePicture(String name) {
         guiNode.detachChildNamed(name);
     }
     
     private void loadAmmo() { 
        ammo = 12;
        reload = false;
     }
 
     private void setupPhysics() {
                 // Set up a Physics game
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
                 
         // Configure cam to look at scene
         cam.setLocation(new Vector3f(0, 4f, 6f));
         cam.lookAt(new Vector3f(2, 2, 0), Vector3f.UNIT_Y);
         // Initialze the scene, materials, and physics space
         initMaterials();
         initCrossHairs();
     }
     
     /**
      * A plus sign used as crosshairs to help the player with aiming
      */
     private void initCrossHairs() {
         guiNode.detachAllChildren();
         Picture crh = new Picture("HUD Picture");
         crh.setImage(assetManager, "Textures/crosshairs.png", true);
         crh.setWidth(280);
         crh.setHeight(280);
         crh.setPosition(504,212);
         crh.setName("crosshair");
         guiNode.attachChild(crh);
     }
 
     /**
      * Make a solid floor and add it to the scene
      */
     private void initFloor() {
         Geometry floorGeo = new Geometry("Floor", floor);
         floorGeo.setMaterial(floorMat);
         floorGeo.setLocalTranslation(0, -0.1f, 0);
         this.rootNode.attachChild(floorGeo);
         
         // Make the floor physical with mass 0.0f
         floorPhy = new RigidBodyControl(0.0f);
         floorGeo.addControl(floorPhy);
         bulletAppState.getPhysicsSpace().add(floorPhy);
     }
 
     /**
      * Initialize the materials used in this scene.
      */
     private void initMaterials() {
        wallMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key = new TextureKey("Textures/Terrain/BrickWall/BrickWall.jpg");
        key.setGenerateMips(true);
        Texture tex = assetManager.loadTexture(key);
        wallMat.setTexture("ColorMap", tex);
        
        stoneMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key2 = new TextureKey("Textures/bulletTexture.png");
        key2.setGenerateMips(true);
        Texture tex2 = assetManager.loadTexture(key2);
        stoneMat.setTexture("ColorMap", tex2);
        
        floorMat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        TextureKey key3 = new TextureKey("Environment/sand.jpg");
        key3.setGenerateMips(true);
        Texture tex3 = assetManager.loadTexture(key3);
        tex3.setWrap(WrapMode.Repeat);
        floorMat.setTexture("ColorMap", tex3);
     }
 
     /**
      * This loop builds a wall out of individual bricks
      */
     private void initWall() {
         float startpt = brickLength / 4;
         float height = 0;
         for (int j = 0; j < 15; j++) {
             for (int i = 0; i < 6; i++) {
                 Vector3f vt =
                         new Vector3f( i*brickLength * 2 + startpt,
                         brickHeight + height, 0);
                 makeBrick(vt);
             }
             startpt = -startpt;
             height += 2 * brickHeight;
         }
     }
     /** We create two audio nodes. */
   private void initAudio() {
     /* gun shot sound is to be triggered by a mouse click. */
     audio_gun = new AudioNode(assetManager, "Sounds/Effects/Gun.wav", false);
     audio_gun.setLooping(false);
     audio_gun.setVolume(3);
     rootNode.attachChild(audio_gun);
     
      audio_reload = new AudioNode(assetManager, "Sounds/Effects/gunreload33.wav", false);
     audio_reload.setLooping(false);
     audio_reload.setVolume(3);
     rootNode.attachChild(audio_gun);
  
     /* nature sound - keeps playing in a loop. */
    audio_water = new AudioNode(assetManager, "Sounds/Effects/Jaws.wav", false);
     audio_water.setLooping(true);  // activate continuous playing
     audio_water.setPositional(true);
     audio_water.setLocalTranslation(Vector3f.ZERO.clone());
     audio_water.setVolume(1);
     rootNode.attachChild(audio_water);
     audio_water.play(); 
   }
     
     /**
      * This method creates one individual physical brick
      * 
      * @param loc   The vector of the bricks location
      */
     private void makeBrick(Vector3f loc) {
         // Create a brick geometry and attach to the scene graph.
         Geometry brickGeo = new Geometry("brick", box);
         brickGeo.setMaterial(wallMat);
         rootNode.attachChild(brickGeo);
         
         // Position the brick geometry
         brickGeo.setLocalTranslation(loc);
         
         // Make brick physical with a mass > 0.0f.
         brickPhy = new RigidBodyControl(1f);
                 
         // Add physical brick to physics space.
         brickGeo.addControl(brickPhy);
         bulletAppState.getPhysicsSpace().add(brickPhy);
     }
     
      /**
      * This method creates one individual physical cannon ball.
      * By default, the ball is accelerated and flies from the
      * camera position in the camera direction.
      */
     private void makeBullet() {
         // Create a cannon ball geometry and attach to the scene graph
         Geometry ballGeo = new Geometry("bullet", sphere);
         ballGeo.setMaterial(stoneMat);
         rootNode.attachChild(ballGeo);
         
         // Position the cannon ball
         ballGeo.setLocalTranslation(cam.getLocation());
         
         // Make th ball physical with a mass > 0.0f
         ballPhy = new RigidBodyControl(100f);
         
          
         // Add physical ball to physics space
         ballGeo.addControl(ballPhy);
         bulletAppState.getPhysicsSpace().add(ballPhy);
         ballPhy.setCcdMotionThreshold(5f);
         // Accelerate the physical ball to shoot it
         ballPhy.setLinearVelocity(cam.getDirection().mult(40f));
         ballPhy.setGravity(Vector3f.ZERO);
         
     }
     
     
     private class Timer {
         public float timeElapsed;
         public float delayTime;
         public String task;
         
         public Timer(float delayTime, String task) {
             this.timeElapsed = 0;
             this.delayTime = delayTime;
             this.task = task;
         }
     }
     
 }
