 package gamestate;
 
 import com.jme3.app.Application;
 import com.jme3.app.SimpleApplication;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.asset.AssetManager;
 import com.jme3.bullet.BulletAppState;
 import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
 import com.jme3.bullet.control.CharacterControl;
 import com.jme3.input.ChaseCamera;
 import com.jme3.input.InputManager;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.light.DirectionalLight;
 import com.jme3.math.ColorRGBA;
 import com.jme3.math.FastMath;
 import com.jme3.math.Vector3f;
 import com.jme3.renderer.ViewPort;
 import com.jme3.scene.Node;
 import variables.P;
 
 /**
  * This class handles aspects of the actual game. 
  *
  * @author forssenm
  */
 public class InGameState extends AbstractAppState {
     private SimpleApplication app;
     private Node inGameRootNode;
     private AssetManager assetManager;
     private AppStateManager stateManager;
     private InputManager inputManager;
     private ViewPort viewPort;
     private BulletAppState physics;
     
     private Node player;
     
     /**
      * This method initializes the the InGameState
      *
      * @param stateManager
      * @param app
      */
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
         super.initialize(stateManager, app);
         this.app = (SimpleApplication) app;
         this.inGameRootNode = new Node();
         this.app.getRootNode().attachChild(this.inGameRootNode);
         this.assetManager = this.app.getAssetManager();
         this.stateManager = this.app.getStateManager();
         this.inputManager = this.app.getInputManager();
         this.viewPort = this.app.getViewPort();
         this.physics = new BulletAppState();
         this.stateManager.attach(physics);
         
         this.stateManager.attach(new RunningState());
        
         initLevel();
         initPlayer();
         initCamera();
         initInputs();
 
         DirectionalLight sun = new DirectionalLight();
         sun.setColor(ColorRGBA.Green);
         sun.setDirection(new Vector3f(-.5f, -.5f, -.5f).normalizeLocal());
 
         inGameRootNode.addLight(sun);
     }
    
     public void initLevel() {
         LevelControl levelControl = new LevelControl(
                 assetManager, physics.getPhysicsSpace());
         Node level = new Node();
         level.addControl(levelControl);
         inGameRootNode.attachChild(level);
     }
     
     public void initPlayer() {
         player = (Node)assetManager.loadModel("Models/ghost6anim/ghost6animgroups.j3o");
         inGameRootNode.attachChild(player);
         
         CharacterControl playerControl = new CharacterControl(new CapsuleCollisionShape(1f, 0.5f), 0.05f);
         playerControl.setWalkDirection(Vector3f.UNIT_X.multLocal(P.run_speed));
         playerControl.setJumpSpeed(P.jump_speed);
         player.setLocalTranslation(new Vector3f(0, 3f, 0));
 
         player.addControl(playerControl);
         physics.getPhysicsSpace().addAll(player);
     }
 
     @Override
     public void cleanup() {
         super.cleanup();
         this.app.getRootNode().detachChild(this.inGameRootNode);
     }
 
     @Override
     public void setEnabled(boolean enabled) {
         super.setEnabled(enabled);
         if (enabled) {
             //Initiate the things that are needed when the state is active
             System.out.println("InGameState is now active");
         } else {
             //Remove the things not needed when the state is inactive
             System.out.println("InGameState is now inactive");
         }
     }
 
     @Override
     public void update(float tpf) {
     }
 
      private ChaseCamera chaseCam;
      
      /**
       * Initializes the camera.
       * After this, the camera follows the player, looking at them from
       * the right angle.
       */
      private void initCamera() {
         this.app.getFlyByCamera().setEnabled(false);
         this.chaseCam = new ChaseCamera(this.app.getCamera(), this.player, this.inputManager);
         //this.chaseCam.setSmoothMotion(true);
         this.chaseCam.setTrailingEnabled(false);
         this.chaseCam.setDefaultHorizontalRotation(-FastMath.DEG_TO_RAD * 270);
         this.chaseCam.setDefaultDistance(50);
      }
 
     /**
      * Sets up the user inputs. Jump is triggered by
      */
     private void initInputs() {
         inputManager.addListener(actionListener, "jump");
     }
     
     private ActionListener actionListener = new ActionListener() {
         public void onAction(String binding, boolean value, float tpf) {
             if (binding.equals("jump")) {
                 player.getControl(CharacterControl.class).jump();
             }
         }
     };
 
 }
