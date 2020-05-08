 package gamestate;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.input.InputManager;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 
 /**
  * This class activates the user controls used when the player is running. 
  * In The current implementation this means that jumps are triggered by 
  * <code>KeyInput.KEY_SPACE</code> and <code>MouseInput.BUTTON_LEFT</code>.
  * <br/> <br/>
  * Relavant things to add to this class could be to switch 
 * controls related to the player.
  * @author dagen
  */
 public class RunningState extends AbstractAppState implements ActionListener{
     private InputManager inputManager;
     
     /**{@inheritDoc}*/
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
         super.initialize(stateManager, app);
         inputManager = app.getInputManager();
         
         this.initInput();
     }
     
     /**
      * Initializes user input associated with the RunningState.
      * Mapps <code>KeyInput.KEY_SPACE</code> and 
      * <code>MouseInput.BUTTON_LEFT</code> to the "jump" mapping and 
      * adds itself as listener to the "jump" mapping.
      */
     private void initInput(){
         inputManager.addMapping("jump", 
                 new KeyTrigger(KeyInput.KEY_SPACE),
                 new MouseButtonTrigger(MouseInput.BUTTON_LEFT)
                 );
         inputManager.addListener(this, "jump");
     }
     
     /**
      * Does nothing. Nothing to update continously.
      * @param tpf time per frame.
      */
     @Override
     public void update(float tpf) {
         /* Does nothing */
     }
     
     /**{@inheritDoc}*/
     @Override
     public void cleanup() {
         this.cleanupInput();
         super.cleanup();
     }
     
     /**
      * Cleans up the user input associated with the <code>RunningState</code>.
      * Deletes the "jump" input mapping and removes <code>this</code> 
      * as <code>ActionListener</code>.
      */
     private void cleanupInput(){
         inputManager.deleteMapping("jump");
         inputManager.removeListener(this);
     }
 
     /**
      * @deprecated 
      * This method currently checks for the mapping "jump" and 
      * then does nothing. 
      * @param name the name of the mapping i.e. "jump".
      * @param isPressed true on mouse down, false on mouse up. 
      * @param tpf used for input with continous state. 
      */
     public void onAction(String name, boolean isPressed, float tpf) {
         //TODO implement jumping hereor even better: implement it in a PlayerControl
         if(name.equals("jump") && isPressed){
             //Button pressed
         }else{
             //Button released
         }
     }
 }
