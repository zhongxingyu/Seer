 package mygame.model.character;
 
 import com.jme3.animation.AnimChannel;
 import com.jme3.animation.AnimControl;
 import com.jme3.animation.AnimEventListener;
 import com.jme3.animation.Animation;
 import com.jme3.animation.LoopMode;
 import com.jme3.app.Application;
 import com.jme3.app.SimpleApplication;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.audio.AudioNode;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.cinematic.events.AnimationEvent;
 import com.jme3.collision.CollisionResult;
 import com.jme3.collision.CollisionResults;
 import com.jme3.font.BitmapFont;
 import com.jme3.font.BitmapText;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.material.Material;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.Ray;
 import com.jme3.math.Vector3f;
 import com.jme3.scene.Geometry;
 import com.jme3.scene.Node;
 import com.jme3.scene.Spatial;
 import com.jme3.scene.shape.Sphere;
 import com.jme3.system.AppSettings;
 import java.awt.DisplayMode;
 import java.awt.GraphicsDevice;
 import java.awt.GraphicsEnvironment;
 import java.util.ArrayList;
 import java.util.List;
 import mygame.Controller;
 import mygame.States.Scenario.Scenario;
 import mygame.model.weapon.WeaponInterface;
 import mygame.model.zombie.ZombieInterface;
 import mygame.model.zombie.ZombieManagerInterface;
 import mygame.sound.SoundManager;
 
 /**
  * Example 9 - How to make walls and floors solid. This collision code uses
  * Physics and a custom Action Listener.
  *
  * @authors: rociotovar & politmiro
  * @test and comments: ernest
  *
  */
 /**
  * Main Character JMonkey class. It contains all necessary methods in order to
  * load, situate, move and run our character correctly. It has necessary methods
  * for charge weapon and if it is required, shoot. It also contains buttons and
  * listeners so that it makes its moves. In addition, it has ambient music and
  * footsteps sound and Pause and Mute states. Getters and setters for
  * Character's attributes
  */
 public final class CharacterMainJMonkey
         implements AnimEventListener, ActionListener, CharacterMainInterface {
 
     private BulletAppState bulletAppState;
     private WeaponInterface currentWeapon;
     private List<WeaponInterface> weapons;  // list of caught weapons
     private CharacterControl playerControl; // character control
     private Spatial playerModelLoad;
     private AssetManager assetManager;
     private Vector3f walkDirection = new Vector3f();
     private boolean left = false, right = false, up = false, down = false, 
             run = false, mute = false, weapon1=false, weapon2=false; // booleans for listeners
     private SimpleApplication app;
     boolean primeraVez = true;
     boolean isPaused;
     boolean isMuted = false; // Booleano que controla si es luego esta silenciado
     boolean shootActivated = false;
     boolean created = false;
     AudioNode audio_environment;  // environment audio node
     Node shootables;
     Geometry marcaVermella;  // red mark for shoot
     Scenario escenari;  // scenario object
     protected BitmapFont guiFont;  // fonts
     private Node pivot; // secundary node in order to avoid that character flies.
     private String modelLoad="";
     private AnimChannel channelAnim;
     private AnimControl controlAnim;
     private ArrayList<ZombieInterface> zombiesMI;
     
     
     /**
      * Initialize method. Main method called in RunningGameState. It initializes
      * all character's variables. It creates and loads our character. It
      * initializes our listeners and keys and sets up music nodes and other
      * calls other functional methods. arguments: StateManager and JMonkey
      * Application
      */
     //Stefan: cambio initalize por el constructor!
     public CharacterMainJMonkey(AppStateManager stateManager, Application applicooter) {
         //super.initialize(stateManager, applicooter);
         //Stefan: en ves de el set lo conseguimos de app
         bulletAppState = applicooter.getStateManager().getState(BulletAppState.class);
 
         this.app = (SimpleApplication) applicooter;
         this.assetManager = app.getAssetManager();
         this.escenari = new Scenario(app);  // creating scenario
         this.weapons = new ArrayList<WeaponInterface>(); // creating weapons list
         this.pivot = new Node();
         this.shootables = new Node("Shootables");
        
         setUpKeys();  // set up keys and listeners
         
         // Creating and setting character's features as:
         // collision box, jump speed, fall speed and gravity
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(3f, 8f, 1);
         playerControl = new CharacterControl(capsuleShape, 0.05f);
         playerControl.setJumpSpeed(40);
         playerControl.setFallSpeed(100);
         playerControl.setGravity(100);
 
         // Creating pivot Node so that our character doesn't float
         playerModelLoad = app.getAssetManager().loadModel("Character/porra.j3o");
            
         // Loding our first character model        
         //Material playerMaterial = app.getAssetManager().loadMaterial("Character/Cube.002.j3m");
         pivot.attachChild(playerModelLoad);  // attach 'player model porra' node as a child of pivot node of character
         pivot.addControl(playerControl); // setting control
         playerModelLoad.move(-0.5f, -3.75f, 0f); // setting correct position in order to appears on the floor
         // Positionig char and attaching pivot
         playerControl.setPhysicsLocation(new Vector3f(0, 5, 0));
         bulletAppState.getPhysicsSpace().add(playerControl);
         app.getRootNode().attachChild(pivot);
           
         // Creating node for shoot action and ataching it as a child of Scenario object
         app.getRootNode().attachChild(shootables);
         shootables.attachChild(escenari.getEscenari());        
 
         isPaused = false;
         
         initAnimacio();
     }    
     
     /******By pòlit*****/
     /*****Metode afegir models al node shootables*******/
     public Node getShootables(){
         return shootables;
     }
     
     public void damageToZombies(Geometry g){
         zombiesMI = ((Controller)app).getZombieManager().getZombies();
         for(ZombieInterface z: zombiesMI){
            //System.out.println("ZombiePositon: " + z.getZombieShape().getWorldTranslation());
            //System.out.println("GeometryPosition: " + g.getWorldTranslation());
            if(z.getZombieShape().getWorldTranslation().equals(g.getWorldTranslation())){
                z.doDamage(50, true);
            }
         }
     }
     
     //By Polit
     //Metode que els zombies criden per fernos mal i matarnos
     public void doDamage(int value){
         int vida = ((Controller)app).getScenarioManager().getGuiPlayer().getSaludGUI();
         vida = vida - value;
         ((Controller)app).getScenarioManager().getGuiPlayer().setSaludGUI(vida);
         if (vida <=0){
             //Ara mateix, com que no tenim animacio de morir, i que no se que passa
             //quan ens morim, el jugador no mor, i crido a pausa, per veure un canvi
             //despres de quedarnos sense vida.
             
             isPaused = !isPaused;
             ((Controller)app).setIsRunning(isPaused);
         }
     }
 
     /**
      * Method which assign keys with character actions through listeners.
      */
     public void setUpKeys() {
         
         // @ Ernest, deleting zoom of flycam
         app.getInputManager().deleteMapping("FLYCAM_ZoomOut");
         app.getInputManager().deleteMapping("FLYCAM_ZoomIn");
         
         // Adding left, right, up, down and run actions as a defined keyboard keys
         app.getInputManager().addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
         app.getInputManager().addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
         app.getInputManager().addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
         app.getInputManager().addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
         
         //MIllor saltar amb l'espai i poder correr amb el shift
         app.getInputManager().addMapping("Run", new KeyTrigger(KeyInput.KEY_LSHIFT));
         app.getInputManager().addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
         
         app.getInputManager().addMapping("Mute", new KeyTrigger(KeyInput.KEY_M));
         app.getInputManager().addMapping("Paused", new KeyTrigger(KeyInput.KEY_P));
         
         app.getInputManager().addListener(this, "Left");
         app.getInputManager().addListener(this, "Right");
         app.getInputManager().addListener(this, "Up");
         app.getInputManager().addListener(this, "Down");
         app.getInputManager().addListener(this, "Run");
         app.getInputManager().addListener(this, "Jump");
         
         app.getInputManager().addListener(this, "Mute");
         app.getInputManager().addListener(this, "Paused");
         
     }
     
     
     /** @Ernest --> Method which adds shoot action listener and mapping. It only used in fire weapons model
      */ 
     public void shootListenerMappingManagement(boolean isActivated) {
         
         if (!created && isActivated) {
         app.getInputManager().addMapping("Shoot", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
         app.getInputManager().addListener(accioDisparar, "Shoot");
         created = true;
        }
         
         if (created && !isActivated) {
           app.getInputManager().deleteMapping("Shoot");
           created = false;  
         }
     }
     
     
     /** @Ernest --> Method wich controls if list of weapons has gun and allows change weapon when players had grabbed a gun 
      */
     
     public void controlChangeWeapons () {
             
             app.getInputManager().addMapping("Weapon1", new KeyTrigger(KeyInput.KEY_1));
             app.getInputManager().addMapping("Weapon2", new KeyTrigger(KeyInput.KEY_2));
         
             app.getInputManager().addListener(changeWeapon, "Weapon1");
             app.getInputManager().addListener(changeWeapon, "Weapon2");
     }
     
     /**
      * Method which has our custom actions triggered by key presses. We do not
      * walk yet, we just keep track of the direction the user pressed.
      * Arguments: string to compare: binding boolean flag : value and float
      */
     public void onAction(String binding, boolean value, float tpf) {
 
         if (!isPaused) { // When game isn't in pause
 
             // Set moves, run and jump
             if (binding.equals("Left")) {
                 left = value;
             } else if (binding.equals("Right")) {
                 right = value;
             } else if (binding.equals("Up")) {
                 up = value;
             } else if (binding.equals("Run")) {
                 run = value;
             } else if (binding.equals("Down")) {
                 down = value;
             } else if (binding.equals("Jump")) {
                 playerControl.jump(); 
             } else if (binding.equals("Mute")) {
                 mute = value;
             }
         }
         
          if (binding.equals("Paused") && !value) {               //@Emilio nuevo, para pausar
             isPaused = !isPaused;
             ((Controller)app).setIsRunning(isPaused);
         }
 
         // Controla si se ha pulsado "M" y si el juego esta mutado
         if (mute && !isMuted) {               
             SoundManager.muteAllSounds(app.getRootNode());
             isMuted = !isMuted;
         } else if(mute && isMuted) {               
             SoundManager.unMuteAllSounds(app.getRootNode());
             isMuted = !isMuted;
         }
         // Si esta pulsada "W","A","S" o "D" suenan pasos
         if ((left || right || up || down)) {
             SoundManager.footStepsPlay(app.getRootNode());
         } else {
             SoundManager.footStepsPause(app.getRootNode());
         }
     }
 
     /**
      * Method with the main event loop--walking. We check in which direction the
      * playerControl is walking by interpreting the camera direction forward
      * (camDir) and to the side (camLeft). The setWalkDirection() command is
      * what lets a physics-controlled playerControl walk. We also make sure here
      * that the camera moves with playerControl.
      */
     public void personatgeUpdate() {
         Vector3f camDir = app.getCamera().getDirection().clone().multLocal(0.6f);
         Vector3f camLeft = app.getCamera().getLeft().clone().multLocal(0.4f);
         Vector3f viewDirection = new Vector3f();
         walkDirection.set(0, 0, 0); // setting walking direction as 0,0,0
         
         
         /*  @David C. -- Añadido Condicional isPaused y 
          *  bloqueo de cámara cuando está en pause
          */
         this.app.getFlyByCamera().setEnabled(!isPaused);
         if (!this.isPaused) {
             //@Ernest --  Setting limits in up/down camera movement
              if(app.getCamera().getUp().y < 0) // axis more than 0
              {
               // look at this direction  
               //  app.getCamera().lookAtDirection( new Vector3f(0,app.getCamera().getDirection().y,0),new Vector3f(app.getCamera().getUp().x,-0.1f, app.getCamera().getUp().z));
               // setting axis restiction
               app.getCamera().setAxes(camLeft, new Vector3f (0,1,0), viewDirection);
                  
              }
             //camDir.setY(0); // set y as 0 
             //camDir = camDir.normalize().multLocal(0.2f); 
 
 
             // Setting camera according to action
             if (left) {
                 walkDirection.addLocal(camLeft);
             }
             if (right) {
                 walkDirection.addLocal(camLeft.negate());
             }
             if (up) {
                 walkDirection.addLocal(camDir);
             }
             if (down) {
                 walkDirection.addLocal(camDir.negate());
             }
                 
             if(run){ // Running action  @Ernest --someone delete it before, and character didn't run
                 if (left)  { walkDirection.addLocal(camLeft.mult(3)); }
                 if (right) { walkDirection.addLocal(camLeft.negate().mult(3)); }
                 if (up)    { walkDirection.addLocal(camDir.mult(3)); }
                 if (down)  { walkDirection.addLocal(camDir.negate().mult(3)); }
             }
             
         } else {
             // @David C. -- Bloqueo de las direcciones cuando está en pause
             left = false;
             right = false;
             up = false;
             down = false;
             run = false;
         }
 
         // Creating view direction vector as direction which our character looks
         viewDirection.set(new Vector3f(camDir.getX(), camDir.getY(), camDir.getZ()));
 
         // Setting walk direction
         playerControl.setWalkDirection(walkDirection);
         // Setting view direction. Characters always goes with camera move.
         playerControl.setViewDirection(viewDirection.negate());
 
         // Setting camera location at the same point of character phisics location (first person)
         app.getCamera().setLocation(playerControl.getPhysicsLocation());
     }
 
     /**
      * Player pyhisic position getter
      */
     public Vector3f getPlayerPosition() {
         return playerControl.getPhysicsLocation();
     }
 
     /**
      * Method which adds weapon on weapons list Argument: weapon instance
      * created: weapon
      */
     public void addWeapon(WeaponInterface weapon) {
         weapons.add(weapon);
     }
 
     /**
      * Method which gets our current weapon
      */
     public WeaponInterface getCurrentWeapon() {
         return currentWeapon;
     }
     
         
     //by Polit
     public void initAnimacio(){
         //controlAnim = playerModelLoad.getControl(AnimControl.class);
         //System.out.println("Model carregat: " + playerModelLoad);
         //System.out.println("ControlAnimacio: " + controlAnim);
         //controlAnim.addListener(this);
         //channelAnim = controlAnim.createChannel();
         //channelAnim.setAnim("shootAction");
         //channelAnim.setSpeed(0f);
         //channelAnim.setLoopMode(LoopMode.Loop);
         
     }
     
     /**
      * Shoot Listener Method and listener which performs shooting action in
      * scenario whithin peephole and red mark. It also and shows collision
      * sceneario elements
      */
     private ActionListener accioDisparar = new ActionListener() {
         public void onAction(String name, boolean keyPressed, float tpf) {
             // If receives a 'Shoot action
             if (name.equals("Shoot") && !keyPressed) {
                 // Creating collision box and peephole
                 CollisionResults resultat = new CollisionResults();
                 Ray raig = new Ray(app.getCamera().getLocation(), app.getCamera().getDirection());
                 shootables.collideWith(raig, resultat);
                 System.out.println("----- Collisions? " + resultat.size() + "-----");
 
                 for (int i = 0; i < resultat.size(); i++) {
                     // For each hit, we know distance, impact point, name of geometry.
                     float dist = resultat.getCollision(i).getDistance();
                     Vector3f pt = resultat.getCollision(i).getContactPoint();
                     String hit = resultat.getCollision(i).getGeometry().getName();
                     System.out.println("* Collision #" + i);
                     System.out.println("  You shot " + hit + " at " + pt + ", " + dist + " wu away.");
                 }
                 if (resultat.size() > 0) {
                     // The closest collision point is what was truly hit:
                     CollisionResult closest = resultat.getClosestCollision();
                     damageToZombies(closest.getGeometry());
                     // Let's interact - we mark the hit with a red dot.
                     marcaVermella.setLocalTranslation(closest.getContactPoint());
                     app.getRootNode().attachChild(marcaVermella); // put red sphere at that point
                     //channelAnim.setAnim("shootAction", 0.50f);
                     //channelAnim.setLoopMode(LoopMode.DontLoop);
                     System.out.println("shootAction");
                 } else {
                     // No hits? Then remove the red mark.
                     app.getRootNode().detachChild(marcaVermella);
                 }
             }
         }
     };
     
      private void carregaModel(String model){
         pivot.detachChild(playerModelLoad);
         if (model.equals("porra")){
             playerModelLoad = (Node) app.getAssetManager().loadModel("Character/porra.j3o");
             playerModelLoad.move(-0.5f, -3.5f, 0f); // setting correct position in order to appears on the floor
             shootActivated = false;
         }
         if (model.equals("pistola")){
             playerModelLoad = (Node) app.getAssetManager().loadModel("Character/playerPistola.j3o");
             playerModelLoad.move(0f, -5.5f, 0f); // setting correct position in order to appears on the floor
             shootActivated = true;
             inicialitzarMarcaCollisio();  // call shooting red mark method
         }
         
         shootListenerMappingManagement(shootActivated);
         pivot.attachChild(playerModelLoad);
         //app.getRootNode().attachChild(pivot);
     }
      
     private ActionListener changeWeapon = new ActionListener() {
         public void onAction(String name, boolean keyPressed, float tpf) {
            if (name.equals("Weapon1") && !keyPressed) {
                carregaModel("porra");
                modelLoad = "porra";
            } 
            if (name.equals("Weapon2") && !keyPressed) {
                carregaModel("pistola");
                modelLoad = "pistola";
                
            }
         }
     };
 
     /**
      * Method which creates a red mark as a sphere. Useful for test if weapon
      * can shoot
      */
     protected void inicialitzarMarcaCollisio() {
         // Creating red sphere and set its material
         Sphere sphere = new Sphere(30, 30, 0.2f);
         marcaVermella = new Geometry("BOOM!", sphere);
         Material marcaVermella_mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
         marcaVermella_mat.setColor("Color", ColorRGBA.Red);
         marcaVermella.setMaterial(marcaVermella_mat);
     }
 
     /**
      * Method which constructs a peephole that recreates our target shooting It
      * points to the objective where our red sphere will go
      */
     /*protected void initMirilla() {
         app.getGuiNode().detachAllChildren();
         guiFont = loadGuiFont();
         guiFont = assetManager.loadFont("Interface/Fonts/Default.fnt");
         BitmapText ch = new BitmapText(guiFont, false);
 
         ch.setSize(guiFont.getCharSet().getRenderedSize() * 2);
         ch.setText("+"); // crosshairs
         AppSettings settings = new AppSettings(true);
         
         ch.setLocalTranslation( // center
 
         // Width and height settings        
         settings.getWidth() / 2 - guiFont.getCharSet().getRenderedSize() / 3 * 2,
         settings.getHeight() / 2 + ch.getLineHeight() / 2, 0);
         
         System.out.print("Amplada nova: " + settings.getWidth());
         System.out.print("Alçada nova: " + settings.getHeight());
         
         GraphicsDevice device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
         DisplayMode[] modes = device.getDisplayModes();
         device.getDisplayMode();
         System.out.println("Amplada mode 0: " + modes[0].getWidth());
         System.out.println("Alçada mode 0: " + modes[0].getHeight());
         System.out.println("Bits mode 0: " + modes[0].getBitDepth());
         System.out.println("Amplada mode 1: " + modes[1].getWidth());
         System.out.println("Alçada mode 1: " + modes[1].getHeight());
         System.out.println("Bits mode 1: " + modes[1].getBitDepth());
         System.out.println("Amplada mode 2: " + modes[2].getWidth());
         System.out.println("Alçada mode 2: " + modes[2].getHeight());
         
         System.out.println("Patata1: " + device.getDisplayMode().getHeight());
         System.out.println("Patata2: " + device.getDisplayMode().getWidth());
         
 
         app.getGuiNode().attachChild(ch); // attaching to GUI
     }*/
     
 
     /**
      * Method which loads fonts
      */
     protected BitmapFont loadGuiFont() {
         return assetManager.loadFont("Interface/Fonts/Default.fnt");
     }
 
     /**
      * Lives getter
      */
     public int getNLives() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      * Energy getter
      */
     public double getEnergy() {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      * Current weapon setter
      */
     public void setCurrentWeapon(WeaponInterface weapon) {
         this.currentWeapon = weapon;
     }
 
     /**
      * Method which increments or decrements our energy
      */
     public void incrementEnergy(double quantity) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     /**
      * Method which increment or decrements our lives
      */
     public void incrementNLives(int quantity) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
 
     // @David C. -- Añadido getters del parámetro isPaused
     public boolean isPaused() {
         return isPaused;
     }
 
     public void onAnimCycleDone(AnimControl control, AnimChannel channel, String animName) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 
     public void onAnimChange(AnimControl control, AnimChannel channel, String animName) {
         throw new UnsupportedOperationException("Not supported yet.");
     }
 }
