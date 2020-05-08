 package mygame;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.asset.TextureKey;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.PhysicsCollisionEvent;
 import com.jme3.bullet.collision.PhysicsCollisionListener;
 import com.jme3.bullet.collision.PhysicsCollisionObject;
 import com.jme3.bullet.collision.shapes.SphereCollisionShape;
 import com.jme3.bullet.control.RigidBodyControl;
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
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.RenderManager;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Box;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.texture.Texture;
 import com.jme3.util.TangentBinormalGenerator;
 import java.math.BigInteger;
 
 
 public class Main extends SimpleApplication {
   
     private Boolean isRunning = true;
     private Boolean isBall1Alive = true;
     private Boolean isBall2Alive = true;   
     private boolean gameEnd = false;    
     
     private BulletAppState bulletAppState;
     private Geometry ball;
     private Geometry ball2;
     private Spatial bear_geo;
     private RigidBodyControl ball_phy;
     private RigidBodyControl ball2_phy;
     private RigidBodyControl bear_phy;
     private SphereCollisionShape ball1Shape;
     private SphereCollisionShape ball2Shape;
     private Material mat_lit;
     private Material mat_rock;
     private Material mat_red;
     private final float gax = (float) 0.5;       
     private Sphere c;   
     private ParticleEmitter fire;    
     private BitmapText ch;
      
         //board parameter
     int boardLength;
     int boardWidth;
     int currentmap;
     int[] maptexture;
     int[] mapobjects;
     
     //time parameter
     long time;
     long elapsetime = System.nanoTime();
     int counter = 0;
     
     //Player abilities
     //0 = dash, 1 = jump;
     int abilitySize = 2;
     BigInteger[] p1Abilities = new BigInteger[abilitySize];
     BigInteger[] p2Abilities = new BigInteger[abilitySize];    
     
    //Testing for Destory
    RigidBodyControl[] ctrl;
     
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
     private int[] obj3 = {5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 
         5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 8, 0, 
         0, 0, 0, 8, 0, 0, 0, 0, 8, 0, 0, 0, 0, 8, 0, 0, 0, 0, 0, 0, 6, 7, 0, 0, 
         0, 0, 6, 7, 0, 0};
     private int[] obj4 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 4, 0, 0, 0, 4, 
         0, 4, 4, 0, 4, 0, 0, 0, 4, 0, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
     private int[] obj5 = {0, 0, 0, 0, 6, 0, 7, 0, 0, 0, 0, 0, 0, 6, 6, 0, 0, 0, 
         0, 0, 0, 7, 0, 6, 0, 0, 0, 0, 0, 0, 7, 7, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 6, 0, 0, 0, 7, 
         0, 0, 0, 0, 7, 0, 0, 0, 6, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
         
     public static void main(String[] args) {
         Main app = new Main();
         app.start();
     }
    
     @Override
     public void simpleInitApp() {
         
         // Set camera
         flyCam.setEnabled(false);        
         cam.setLocation(new Vector3f(0f,25f,40f));        
         cam.setAxes(new Vector3f(0f,0f,0f),new Vector3f(0f,0f,0f),new Vector3f(-100f,0f,0f));
         cam.lookAt(new Vector3f(0f,0f,0f), cam.getUp());
         
         /** Set up Physics Game */
         bulletAppState = new BulletAppState();
         stateManager.attach(bulletAppState);
         
         //Physic debug
         bulletAppState.getPhysicsSpace().enableDebug(assetManager);
         
         //Selected map
         currentmap = 1;
         
         //Load Map texture and object arraies
         maptexture = selectedMapTexture(currentmap);
         mapobjects = selectedMapObject(currentmap);
         
         //creates Map
         createFloor(currentmap);
         
         c = new Sphere(20, 20, 0.5f, true, false);               
  
         // Initialize map, objects, UI
         
         /** Must add a light to make the lit object visible! */
         DirectionalLight sun = new DirectionalLight();
         sun.setDirection(new Vector3f(1,0,-2).normalizeLocal());
         sun.setColor(ColorRGBA.White);
         rootNode.addLight(sun);      
         
         viewPort.setBackgroundColor(new ColorRGBA(0.7f, 0.8f, 1f, 1f));
         setUpLight();
         setUpKeys();        
         initMaterials();
         createBall();
         initCrossHairs();
         createPowerUp();
         initAbilities(p1Abilities);
         bulletAppState.getPhysicsSpace().addCollisionListener(physicsCollisionListener);
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
         fire.setStartSize(3f);
         fire.setEndSize(0.1f);
         fire.setGravity(0, 0, 0);
         fire.setLowLife(1f);
         fire.setHighLife(3f);
         fire.getParticleInfluencer().setVelocityVariation(0.1f);
         
         //place on the map
         rootNode.attachChild(fire);         
     }
     
     //initialize ball
     private void createBall(){
         c.setTextureMode(Sphere.TextureMode.Projected);
         TangentBinormalGenerator.generate(c);        
         
         // make ball 1 and attach to the map, applying physics
         ball = new Geometry("Ball1", c);            
         ball1Shape = new SphereCollisionShape(0.5f);
         ball.setMaterial(mat_lit);
         ball.setLocalTranslation(2,gax,3);
         rootNode.attachChild(ball);
         ball_phy = new RigidBodyControl(ball1Shape,0.9f);
         ball.addControl(ball_phy); 
         bulletAppState.getPhysicsSpace().add(ball_phy);
         ball_phy.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
         ball_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
         ball_phy.setRestitution(1f);
         
         // make ball 2 and attach to the map, applying phsyics
         ball2 = new Geometry("Ball2", c);
         ball2Shape = new SphereCollisionShape(0.5f);
         ball2.setMaterial(mat_rock);
         ball2.setLocalTranslation(4,gax,3);
         rootNode.attachChild(ball2);
         ball2_phy = new RigidBodyControl(ball2Shape,0.9f);
         ball2.addControl(ball2_phy);        
         bulletAppState.getPhysicsSpace().add(ball2_phy); 
         ball2_phy.setCollisionGroup(PhysicsCollisionObject.COLLISION_GROUP_01);
         ball2_phy.addCollideWithGroup(PhysicsCollisionObject.COLLISION_GROUP_01);        
         ball2_phy.setRestitution(1f);
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
         
         //inputManager.addMapping("Pause",  new KeyTrigger(KeyInput.KEY_P));
         inputManager.addMapping("Reset",  new KeyTrigger(KeyInput.KEY_P));
         //inputManager.addListener(this, "Pause");
         inputManager.addListener(actionListener, "Reset");
         
         // Player 1 keybindings and listener
         
         inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_F));
         inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_H));
         inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_T));
         inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_G));
         inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
         inputManager.addMapping("Dash", new KeyTrigger(KeyInput.KEY_M));
         
         inputManager.addListener(analogListener,new String[]{ "Left","Right", "Up", "Down"});
         inputManager.addListener(actionListener, "Jump");
         inputManager.addListener(actionListener, "Dash");
         
         // Player 2 test keybindings and listener
         inputManager.addMapping("Left2", new KeyTrigger(KeyInput.KEY_J));
         inputManager.addMapping("Right2", new KeyTrigger(KeyInput.KEY_L));
         inputManager.addMapping("Up2", new KeyTrigger(KeyInput.KEY_I));
         inputManager.addMapping("Down2", new KeyTrigger(KeyInput.KEY_K));
         
         inputManager.addListener(analogListener,new String[]{ "Left2","Right2", "Up2", "Down2"});
         
     }
     
     private AnalogListener analogListener = new AnalogListener() {
         public void onAnalog(String binding, float value, float tpf) {
             //player 1 actions listener
             
                 if (binding.equals("Left")) {                
                     Vector3f v = ball.getLocalTranslation();
                     ball.setLocalTranslation(v.x + value*speed*3, v.y, v.z);
                     ball.rotate(-value*speed*3, 0, 0);
                     ball_phy.applyForce(new Vector3f(-3f, 0, 0),new Vector3f(-3f, 0, 0)); // push the ball foward
                 } else if (binding.equals("Right")) {
                     Vector3f v = ball.getLocalTranslation();
                     ball.setLocalTranslation(v.x - value*speed*3, v.y, v.z);
                     ball.rotate(value*speed*3, 0, 0);
                     ball_phy.applyForce(new Vector3f(3f, 0, 0),new Vector3f(3f, 0, 0));
                 } else if (binding.equals("Up")) {
                     Vector3f v = ball.getLocalTranslation();
                     ball.setLocalTranslation(v.x , v.y , v.z + value*speed*3);
                     ball.rotate(0, 0, -value*speed*3);
                     ball_phy.applyForce(new Vector3f(0, 0, -3f),new Vector3f(0, 0, -3f));
                 } else if (binding.equals("Down")) {
                     Vector3f v = ball.getLocalTranslation();
                     ball.setLocalTranslation(v.x , v.y, v.z - value*speed*3);
                     ball.rotate(0, 0, value*speed*3);
                     ball_phy.applyForce(new Vector3f(0, 0, 3f),new Vector3f(0, 0, 3f));
                 }           
             
             //player 2 action listener
            
                 if (binding.equals("Left2")) {
                   Vector3f v = ball2.getLocalTranslation();
                   ball2.setLocalTranslation(v.x + value*speed*3, v.y, v.z);
                   ball2.rotate(-value*speed*3, 0, 0);
                   ball2_phy.applyForce(new Vector3f(-3f, 0, 0),new Vector3f(-3f, 0, 0)); // push the ball foward
               } else if (binding.equals("Right2")) {
                   Vector3f v = ball2.getLocalTranslation();
                   ball2.setLocalTranslation(v.x - value*speed*3, v.y, v.z);
                   ball2.rotate(value*speed*3, 0, 0);
                   ball2_phy.applyForce(new Vector3f(3f, 0, 0),new Vector3f(3f, 0, 0));
               } else if (binding.equals("Up2")) {
                   Vector3f v = ball2.getLocalTranslation();
                   ball2.setLocalTranslation(v.x , v.y , v.z + value*speed*3);
                   ball2.rotate(0, 0, -value*speed*3);
                   ball2_phy.applyForce(new Vector3f(0, 0, -3f),new Vector3f(0, 0, -3f));
               } else if (binding.equals("Down2")) {
                   Vector3f v = ball2.getLocalTranslation();
                   ball2.setLocalTranslation(v.x , v.y, v.z - value*speed*3);
                   ball2.rotate(0, 0, value*speed*3);
                   ball2_phy.applyForce(new Vector3f(0, 0, 3f),new Vector3f(0, 0, 3f));
               }     
             
             
         }
     };     
     
     private ActionListener actionListener = new ActionListener() {
     public void onAction(String name, boolean keyPressed, float tpf) {
       if (name.equals("Pause") && !keyPressed) {
             isRunning = !isRunning;
       } else if (name.equals("Reset") && !keyPressed){
             System.out.println("RESET");
           
       } else if (name.equals("Jump") && !keyPressed && !onCooldown(p1Abilities,1)) {
           if(getyLocation() < 0.5 && getyLocation() > 0){
             ball2_phy.applyImpulse(new Vector3f(0, 10f, 0), Vector3f.ZERO );               
           }             
       } else if(name.equals("Dash") && !keyPressed && !onCooldown(p1Abilities,0)){                       
             Vector3f v = ball_phy.getLinearVelocity();
             if(v.x != 0 && v.z!=0){
                 ball_phy.applyImpulse(new Vector3f(v.x*50,0,v.z*50), Vector3f.ZERO);
             }
       }
     }
   };    
     
     //return y coords
     private float getyLocation(){
         Vector3f v = ball2.getLocalTranslation(); 
         return v.y;
     }    
     
     // Listens to collision between objects
     private PhysicsCollisionListener physicsCollisionListener = new PhysicsCollisionListener(){        
         public void collision(PhysicsCollisionEvent event) {
             if (event.getNodeA().getName().equals("Ball1") || event.getNodeB().getName().equals("Ball1l")) {                
             if (event.getNodeA().getName().equals("Ball2") || event.getNodeB().getName().equals("Ball2")) {
                 System.out.println("Ouch my balls!!!!!!!!"); 
             }    
             }
         }
     };
     
     // creates a cool crosshair
     protected void initCrossHairs() {
         BitmapText ch = new BitmapText(guiFont, false);        
         ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
         ch.setText("+");        // fake crosshairs :)
         ch.setLocalTranslation( // center
         settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
         settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
         guiNode.attachChild(ch);
   }     
     
     @Override
     public void simpleUpdate(float tpf) {
       
       //Check if ball is within the map; if not, destroy the ball
       //System.out.println(tpf);
       Vector3f loc = ball_phy.getPhysicsLocation();
       Vector3f loc2 = ball2_phy.getPhysicsLocation();
       if(loc.y < 0 && isBall1Alive){
           System.out.println("Ball 1 DEAD");   
           isBall1Alive = false;           
           if(!isBall2Alive){
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
           bulletAppState.getPhysicsSpace().remove(ball_phy);
           rootNode.detachChild(ball);
       } else if(loc2.y < 0 && isBall2Alive){
           System.out.println("Ball 2 Dead");
           isBall2Alive = false;            
           if(!isBall1Alive){
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
           bulletAppState.getPhysicsSpace().remove(ball2_phy);
           rootNode.detachChild(ball2);
       } 
       if(!isBall1Alive && !isBall2Alive && !gameEnd){
           gameEnd = true;
           createObstacle();
       }
   }
     private void createObstacle(){
         bear_geo = assetManager.loadModel("Models/bear.j3o");
         bear_geo.setLocalTranslation(0f,0f,0f);        
         rootNode.attachChild(bear_geo);
         bear_phy = new RigidBodyControl(2f);
         bear_geo.addControl(bear_phy);
         bear_phy.setPhysicsLocation(new Vector3f(0f,10f,0f));
         bulletAppState.getPhysicsSpace().add(bear_phy);
     }
     
      public void createFloor(int j) {
         int i = 0;
         int x = boardLength-1;
         int y= boardWidth-1;
         int dx = x;
         int dy = y-1;
         int xlim = 0;
         int ylim = 0;
         int check = 0;
         Vector3f coord = new Vector3f(0,0,0);
         
         //Iitiallize RigidBotyControl array
         ctrl = new RigidBodyControl[boardLength*boardWidth];
         
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
     }
     
     //create a Map tile with randomly generated terrain to attach to Map
     public void makeMapTile(Vector3f loc, int i) {
         Box box2 = new Box( Vector3f.ZERO, 1f,-.1f,1f);
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
         tile.getControl(RigidBodyControl.class).setKinematic(false);
         bulletAppState.getPhysicsSpace().add(tile);
         rootNode.attachChildAt(tile, i);
         Vector3f objcoord = new Vector3f(loc.x,loc.y+1,loc.z);
         loadObject(objcoord,mapobjects[i],tile);
 
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
             key = new TextureKey("Textures/Terrain/splat/dirt.jpg");            
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
             temp = assetManager.loadModel("Models/boulder_02.j3o");
             temp.scale(.2f);
         }
         else if (i == 5) {
             temp = assetManager.loadModel("Models/ground_pallisade.j3o");
             temp.scale(.9f);
         }
         else if (i == 6) {
             temp = assetManager.loadModel("Models/ramp.j3o");
         }
         else if (i == 7) {
             temp = assetManager.loadModel("Models/ramp.j3o");
             temp.rotate(0, 180, 0);
         }
         else if (i == 8) {
             temp = assetManager.loadModel("Models/barrel.j3o");
         }
         else {
             return;
         }
         temp.setLocalTranslation(loc);        
         rootNode.attachChild(temp);
         temp.addControl(new RigidBodyControl(0f));
         temp.getControl(RigidBodyControl.class).setPhysicsLocation(loc);
         bulletAppState.getPhysicsSpace().add(temp.getControl(RigidBodyControl.class));
     }
     
     //Destory a piece of the Map
     public void destoryMap(int i) {
        rootNode.getChild(i).getControl(RigidBodyControl.class).setMass(100f);
        //rootNode.detachChildAt(i);
     }
     
     public void initAbilities(BigInteger[] temp) {
         for (int i = 0; i < abilitySize; i++) {
             temp[i] = new BigInteger(String.valueOf(System.nanoTime()));
         }
     }
     
     public boolean onCooldown(BigInteger[] temp, int i) {
         if (temp[i].compareTo(new BigInteger(String.valueOf(System.nanoTime()))) < 0) {
             temp[i] = new BigInteger(String.valueOf(System.nanoTime())).add(
                     new BigInteger("5000000000"));
             return false;
         }
         else {
             return true;
         }
     }
 }
