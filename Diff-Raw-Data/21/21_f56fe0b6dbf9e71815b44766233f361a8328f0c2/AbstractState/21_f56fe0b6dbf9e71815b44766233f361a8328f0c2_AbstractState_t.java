 package mygame.state;
 
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.collision.CollisionResults;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.scene.Node;
 import mygame.engine.Camera;
 import mygame.engine.Camera.CameraMode;
 import mygame.engine.GuiCamera;
 import mygame.engine.Picking;
 import mygame.engine.gui.Crosshair;
 import mygame.engine.gui.MouseCursor;
 import mygame.engine.input.KeyInputs;
 import mygame.engine.nodes.GroupNode;
 import mygame.helpers.Share;
 
 /**
  *
  * @author Dansion
  */
 //@TODO Fix state switching listeners / picking / camera
 
 public class AbstractState extends AbstractAppState {
     public final Share shares = Share.getInstance();
     public ActionListener actionListener = null;
     public final GroupNode rootNode = new GroupNode(this.getClass().getName());
     public final GroupNode guiNode = new GroupNode(this.getClass().getName());
     
     public final Node parentNode;
     public String[] mactions = null;
     public int[] mtriggers = null;
     public Camera cam = null;
     public GuiCamera guiCam = null;
     private MouseCursor mouseCursor = null;
     private Crosshair ch = null;
     private KeyInputs kinputs = null;
     private KeyInputs global_kinputs = null;
     private CameraMode parentMode = null;
     
     private boolean cursorActive;
     
     public AbstractState(Node parent) {
         super();
         if(getCamera() != null) {
             this.parentMode = getCamera().getMode();
         }
         
         this.parentNode = parent;
         
         setEnabled(false);
     }
     
     @Override 
     public void stateAttached(AppStateManager stateManager)  {
         super.stateAttached(stateManager);
         setEnabled(true);
         
         System.out.println(this.getClass().getSimpleName() + " running");
         
         parentNode.attachChild(rootNode);
         shares.guiNode.attachChild(guiNode);
         
         if(actionListener != null) {
             if(mactions != null) {shares.inputManager.addListener(actionListener, mactions);}
         }
         
         if(kinputs != null) {
             kinputs.attachListener();
         }
         
         if(global_kinputs != null) {
             global_kinputs.attachListener();
         }
         
         if(mouseCursor != null) {
             guiNode.attachChild(mouseCursor);
         }
 
         onAttach();
     }
     
     @Override 
     public void stateDetached(AppStateManager stateManager)  {
         super.stateDetached(stateManager);
         setEnabled(false);
         
         System.out.println(this.getClass().getSimpleName() + " paused");
         
         parentNode.detachChild(rootNode);
         shares.guiNode.detachChild(guiNode);
         
         if(actionListener != null) {
             shares.inputManager.removeListener(actionListener);
         }
         
         if(kinputs != null) {
             kinputs.detachListener();
         }
         
         if(global_kinputs != null) {
             global_kinputs.detachListener();
         }
         
         if(mouseCursor != null) {
             guiNode.detachChild(mouseCursor);
         }
         
         restoreCamera();
         
         onDetach();
     }
     
     public void switchState(AbstractState state) {
          if(state.equals(this)) {
              if(state.isEnabled()) {
                  shares.stateManager.detach(state);
              }
              else {
                  state.run();
              }
          }
          else {
              //not switching itself so disable / enable own stuff
              if(state.isEnabled()) {
                  //disable target state
                  System.out.println("Substate paused.");
                  shares.stateManager.detach(state);
                  
                  //enable own state inputs
                  if(kinputs != null) {
                    System.out.println("enable own inputs.");
                     kinputs.attachListener();
                  }
                  
                  setMouseActions(mactions, mtriggers);
              }
              else {
                  //enable target state
                  System.out.println("Substate running.");
                  state.run();
                 
                  //disable own state inputs (except global_kinputs)
                  if(kinputs != null) {
                    System.out.println("disable own inputs.");
                     kinputs.detachListener();
                  }
                  
                 //re-attach listener for global keys when needed
                 if(global_kinputs != null) {
                    global_kinputs.attachListener();
                 }
                 
                  unsetMouseActions(mactions);
              }
          }
      }
     
     public final void setMouseActions(String[] actions, int[] triggers) {
         if(actions.length == triggers.length) {
             this.mactions = actions;
             this.mtriggers = triggers;
 
             for(int i=0; i<actions.length; i++) {
                 System.out.println("adding : " + actions[i]);
                 shares.inputManager.addMapping(actions[i], new MouseButtonTrigger(triggers[i]));
             }
             
             if(actionListener != null) {
                 if(mactions != null) {shares.inputManager.addListener(actionListener, mactions);}
             }
         }
         else {
             throw new UnsupportedOperationException("amount of actions is not the same as the amount of keys");
         }
     }
     
     public final void unsetMouseActions(String[] actions) {
         if(actions.length == this.mactions.length) {
             for(int i=0; i<actions.length; i++) {
                 System.out.println("removing : " + actions[i]);
                 shares.inputManager.deleteMapping(actions[i]);
             }
         }
         else {
             throw new UnsupportedOperationException("amount of mouse actions is not the same as the amount of known mouse actions");
         }
     }
     
     public void pause() {
         pause(this);
     }
     
     public void pause(AbstractState state) {
         shares.stateManager.detach(state);    
     }
     
         
     public void run() {
         shares.stateManager.attach(this);
     }
         
     @Override
     public void update(float tpf) {
         super.update(tpf);
     }
     
     public void enableCursor(boolean active) {
         if(active) {
             if(mouseCursor == null) {
                 mouseCursor = new MouseCursor();
             }
             
             guiNode.attachChild(mouseCursor);
         }
         else {
             if(mouseCursor != null) {
                 guiNode.detachChild(mouseCursor);
             }
         }
         
         cursorActive = active;
     }
     
     public boolean isCursorActive() {
         return cursorActive;
     }
     
     public MouseCursor getCursor() {
         return mouseCursor;
     }
     
     public boolean isGuiCamActive() {
         if(guiCam != null) {
             return guiCam.guiViewPort.isEnabled();
         }
         else {
             return false;
         }
     }
     
     public void enableGuiCam(boolean active) {
         enableGuiCam(active, shares.guiNode);
     }
     
     public void enableGuiCam(boolean active, Node guiNode) {
         if(active) {
             if(guiCam == null) {
                 guiCam = new GuiCamera();
             }
             
             guiCam.guiViewPort.setEnabled(true);
         }
         else {
             if(guiCam != null) {
                 guiCam.guiViewPort.setEnabled(false);
             }
         }
     }
     
     public boolean isCameraActive() {
         if(cam != null) {
             return cam.viewPort.isEnabled();
         }
         else {
             return false;
         }
     }
     
     public void enableCamera(boolean active) {
         enableCamera(active, shares.rootNode);
     }
     
     public void enableCamera(boolean active, Node rootNode) {
         if(active) {
             if(cam == null) {
                 cam = new Camera();
             }
             
             cam.viewPort.setEnabled(true);
         }
         else {
             if(cam != null) {
                 cam.viewPort.setEnabled(false);
             }
         }
     }
     
     public Camera getCamera() {
         if(cam != null) {
             return cam;
         }
         else {
             try {
                 return (Camera) shares.renderManager.getMainView("Default").getCamera();
             }
             catch(ClassCastException err) {
                 return null;
             }
         }
     }
     
     public void showCrossHair(boolean active) {
         if(active) {
             if(ch == null) {
                 ch = new Crosshair();
             }
             
             guiNode.attachChild(ch);
         }
         else {
             if(ch != null) {
                 guiNode.detachChild(ch);
             }
         }
     }
     
     public void setKeyInputs(String keyGroup, int [] keys, String [] actions) {
         setKeyInputs(keyGroup, null, keys, actions);
     }
     
     public void setKeyInputs(String keyGroup, String subGroup, int [] keys, String [] actions) {
         kinputs = new KeyInputs(keyGroup, subGroup);
         
         kinputs.addInputs(keys, actions);
     }
     
     public void setGlobalKeyInputs(String keyGroup, int [] keys, String [] actions) {
         setGlobalKeyInputs(keyGroup, null, keys, actions);
     }
     
     public void setGlobalKeyInputs(String keyGroup, String subGroup, int [] keys, String [] actions) {
         global_kinputs = new KeyInputs(keyGroup, subGroup);
         
         global_kinputs.addInputs(keys, actions);
     }
     
     public void setKeyListener(ActionListener actionListener) {
         if(kinputs != null) {
             kinputs.addListener(actionListener);
         }
         
         if(global_kinputs != null) {
             global_kinputs.addListener(actionListener);
         }
     }
     
     public CollisionResults cameraPick() {
         return Picking.cameraPick(rootNode, getCamera());
     }
     
     public CollisionResults cameraPick(Node target) {
         return Picking.cameraPick(target, getCamera());
     }
     
     public void restoreCamera() {
         if(parentMode != null) {
             getCamera().setMode(parentMode);
         }
     }
     
     public void onAttach() {}
     
     public void onDetach() {}
 }
