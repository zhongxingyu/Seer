 package projectrts.controller;
 
 import java.util.List;
 
 import projectrts.global.constants.Constants;
 import projectrts.global.utils.Utils;
 import projectrts.model.core.EntityManager;
 import projectrts.model.core.IGame;
 import projectrts.model.core.P;
 import projectrts.model.core.Position;
 import projectrts.model.core.abilities.IAbility;
 import projectrts.model.core.entities.IEntity;
 import projectrts.model.core.entities.IPlayerControlledEntity;
 import projectrts.model.core.entities.PlayerControlledEntity;
 import projectrts.view.GameView;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseAxisTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 
 import de.lessvoid.nifty.elements.Element;
 import de.lessvoid.nifty.screen.Screen;
 
 /**
  * A class for handling all input.
  * @author Markus Ekstrm
  *
  */
 public class InputController {
 
 	// Before the mouse is moved it has the position (0, 0), causing the camera to move in that direction.
 	// mouseActivated suppresses the camera until set to true (which is done when the mouse is first moved).
 	private boolean mouseActivated = false; 
 	private SimpleApplication app;
 
 	private IGame game; // The model
 	private GameView view; 
 	private GUIControl guiControl;
 	
 	
 	public InputController(SimpleApplication app, IGame game, GameView view) {
 		this.app = app;
 		this.game = game;
 		this.view = view;
 		initializeKeys();
 	}
 	
 	/**
 	 * Updates the input, should be hooked into the main update loop
 	 * @param tpf Time-per-frame
 	 */
     public void update(float tpf) {
     	
     	
         if(app.getStateManager().getState(InGameState.class).isEnabled()){
           // do the following while game is RUNNING // modify scene graph...
         	updateCamera(tpf);
         } else {
           // do the following while game is PAUSED, e.g. play an idle animation.
           //...        
         }
       }
     
     /**
      * Updates the camera.
      * 
      * If the mouse cursor is close enough to one of the edges, it moves the camera a certain amount
      * in that direction. The amount is decided by the getCameraSpeed method in the Constants class. 
      * @param tpf
      */
     private void updateCamera(float tpf) {
     	if(mouseActivated) {
 	    	Vector3f loc = app.getCamera().getLocation();
 	    	Vector2f mLoc = app.getInputManager().getCursorPosition();
 	    	float margin = Constants.INSTANCE.getCameraMoveMargin();
 	    	if(mLoc.x >= app.getCamera().getWidth() - margin && loc.x <= P.INSTANCE.getWorldWidth() * Constants.INSTANCE.getModelToWorld()) {
 	    		app.getCamera().setLocation(loc.add(tpf * Constants.INSTANCE.getCameraSpeed(), 0, 0));
 	    	}
 	    	if(mLoc.x <= margin && loc.x >= 0) {
 	    		app.getCamera().setLocation(loc.add(tpf * -Constants.INSTANCE.getCameraSpeed(), 0, 0));
 	    	}
 	    	if(mLoc.y <= margin && loc.y >= -P.INSTANCE.getWorldHeight() * Constants.INSTANCE.getModelToWorld()) {
 	    		app.getCamera().setLocation(loc.add(0, tpf * -Constants.INSTANCE.getCameraSpeed(), 0));
 	    	}
 	    	if(mLoc.y >= app.getCamera().getHeight() - margin && loc.y <= 0) {
 	    		app.getCamera().setLocation(loc.add(0, tpf * Constants.INSTANCE.getCameraSpeed(), 0));
 	    	}
     	}
     }
 	
 	/** 
 	 * Initializes all keybinds.
 	 * 
 	 * Maps named actions to inputs and assigns listeners. 
 	 */
     private void initializeKeys() {
     	this.app.getInputManager().setCursorVisible(true);
     	this.app.getInputManager().clearMappings();
     	
     	// You can map one or several inputs to one named action
     	this.app.getInputManager().addMapping("cameraRightKey",  new KeyTrigger(KeyInput.KEY_RIGHT));
     	this.app.getInputManager().addMapping("cameraLeftKey",   new KeyTrigger(KeyInput.KEY_LEFT));
     	this.app.getInputManager().addMapping("cameraUpKey",  new KeyTrigger(KeyInput.KEY_UP));
     	this.app.getInputManager().addMapping("cameraDownKey", new KeyTrigger(KeyInput.KEY_DOWN));
     	
     	this.app.getInputManager().addMapping("cameraRightMouse",  new MouseAxisTrigger(MouseInput.AXIS_X, true) );
     	this.app.getInputManager().addMapping("cameraLeftMouse",   new MouseAxisTrigger(MouseInput.AXIS_X, false) );
     	this.app.getInputManager().addMapping("cameraUpMouse",  new MouseAxisTrigger(MouseInput.AXIS_Y, true) );
     	this.app.getInputManager().addMapping("cameraDownMouse", new MouseAxisTrigger(MouseInput.AXIS_Y, false) );
     	
     	// Map left and right mouse buttons
     	this.app.getInputManager().addMapping("mouseLeftButton", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
     	this.app.getInputManager().addMapping("mouseRightButton", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
     	
     	// Add the names to the action/analog listener.	
     	this.app.getInputManager().addListener(analogListener, new String[]{"cameraRightKey", "cameraLeftKey", "cameraUpKey", "cameraDownKey"});
     	this.app.getInputManager().addListener(actionListener, new String[]{"cameraRightMouse", "cameraLeftMouse", "cameraUpMouse", "cameraDownMouse", 
     																		"mouseLeftButton", "mouseRightButton"});
     	//Debug control mapping
     	this.app.getInputManager().addMapping("exit",  new KeyTrigger(KeyInput.KEY_ESCAPE));
     	this.app.getInputManager().addMapping("checkMouseLoc", new KeyTrigger(KeyInput.KEY_M));
     	
     	//Add debug controls to action/analog listener
     	this.app.getInputManager().addListener(actionListener, new String[]{"exit", "checkMouseLoc"});
     }
     
     /**
      * An analog listener, use if the input is analog - i.e. it can take on several
      * values, not just "on" and "off". 
      * 
      */
     private AnalogListener analogListener = new AnalogListener() {
 	    public void onAnalog(String name, float value, float tpf) {
 	    	
 	    	// Make sure we are in the correct state and that it is enabled.
 	    	if (app.getStateManager().getState(InGameState.class).isEnabled()) {
 	    		Vector3f loc = app.getCamera().getLocation();
 	    		
 	            if (name.equals("cameraRightKey") && loc.x <= P.INSTANCE.getWorldWidth() * Constants.INSTANCE.getModelToWorld()) {
 	            	app.getCamera().setLocation(loc.add(new Vector3f(value*Constants.INSTANCE.getCameraSpeed(), 0, 0)));
 	            }
 	            if (name.equals("cameraLeftKey") && loc.x >= 0) {
 	            	app.getCamera().setLocation(loc.add(new Vector3f(-value*Constants.INSTANCE.getCameraSpeed(), 0, 0)));
 	            }
 	            if (name.equals("cameraUpKey") && loc.y <= 0) {
 	            	app.getCamera().setLocation(loc.add(new Vector3f(0, value*Constants.INSTANCE.getCameraSpeed(), 0)));
 	            }
 	            if (name.equals("cameraDownKey") && loc.y >= -P.INSTANCE.getWorldHeight() * Constants.INSTANCE.getModelToWorld()) {
 	            	app.getCamera().setLocation(loc.add(new Vector3f(0, -value*Constants.INSTANCE.getCameraSpeed(), 0)));
 	            }
           	} else {
 	        	  System.out.println("Press P to unpause.");
           	}
     	}
     };
       
     /**
      * A digital listener, use if the input is digital - i.e. it can only
      * be either "on" or "off".
      */
     private ActionListener actionListener = new ActionListener() {
     	public void onAction(String name, boolean keyPressed, float tpf) {
     		if (name.equals("exit") && keyPressed) {
 	            app.stop();
 	    	}
     		
 	    	if(app.getStateManager().getState(InGameState.class).isEnabled()) {
 	    		if (name.equals("mouseLeftButton") && keyPressed) {
 	    			handleLeftClick();
 	    		}
 	    		if (name.equals("mouseRightButton") && keyPressed) {
 	    			handleRightClick();
 	    		}
 	    		
 	    		//Debugging
 	    		if (name.equals("checkMouseLoc") && keyPressed) {
 	    			System.out.println("mLoc = " + app.getInputManager().getCursorPosition().toString());
 	    			System.out.println("wLoc = " + app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0));
 	    		}
 	    	} else {
 	    		
 	    	}
 	    	
 	    	// Bypass the fact that the cursor position is (0, 0) before it is moved,
 	    	// which causes the camera to move towards that location would this not be in place.
 	    	if ((!mouseActivated && name.equals("cameraRightMouse") ||
 	    			name.equals("cameraLeftMouse") ||
 	    			name.equals("cameraUpMouse") ||
 	    			name.equals("cameraDownMouse"))) {
 	            mouseActivated = true;
 	    	}	
 	    }
     	
     	private void handleLeftClick(){
     		game.getPlayer().select(Utils.INSTANCE.convertWorldToModel(app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0)));
 			view.drawSelected(game.getPlayer().getSelectedEntities());
 			if(guiControl!=null){
 				guiControl.updateAbilities(game.getPlayer().getSelectedEntities());
 			}
 			
     	}
     	
     	private void handleRightClick(){
     		Position click = Utils.INSTANCE.convertWorldToModel(app.getCamera().getWorldCoordinates(
     				app.getInputManager().getCursorPosition(), 0));
     		IEntity e = getEntityAtPosition(click);
     		if(e!=null){
     			if(e.getName().equals("Resource")){
     				game.getPlayer().useAbilitySelected("GatherResource", click);
     				
     			}else if(e instanceof PlayerControlledEntity){
     				PlayerControlledEntity pce = (PlayerControlledEntity) e;
     				if(!pce.getOwner().equals(game.getPlayer())){
     					game.getPlayer().useAbilitySelected("Attack", pce.getPosition());
     				}else{
     					game.getPlayer().useAbilitySelected("Move",Utils.INSTANCE.convertWorldToModel(
     	    					app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0)));
     				}
     			}
     			
     		}
     		else{
     			game.getPlayer().useAbilitySelected("Move",Utils.INSTANCE.convertWorldToModel(
     					app.getCamera().getWorldCoordinates(app.getInputManager().getCursorPosition(), 0)));
     		}
     	}
     	
     	private boolean isWithin(double p, double low, double high){
     		return (p>=low && p<=high);
     	}
     	
     	private IEntity getEntityAtPosition(Position pos){
     		List<IEntity> entities = EntityManager.getInstance().getAllEntities();
    		for(IEntity entity: entities){
    			
 				float unitSize = entity.getSize();
 				Position unitPos = entity.getPosition();
 				
 				//If the point is within the area of the unit
 				if(isWithin(pos.getX(), unitPos.getX()-unitSize/2, unitPos.getX()+unitSize/2)
 						&& isWithin(pos.getY(), unitPos.getY()-unitSize/2, unitPos.getY() + unitSize/2)){
 					
 					return entity;
 					
 				}
			
     		}
     		return null;
         }
         
         private void updateAbilities(List<IEntity> selectedEntities){
         	Screen screen = view.getNifty().getScreen("Screen_ID");
     	
     	
         	boolean oneIsSelected = selectedEntities.size()==1;
         	List<IAbility> abilities = null;
         
         	
         	if(oneIsSelected && selectedEntities.get(0) instanceof IPlayerControlledEntity){
         		IPlayerControlledEntity pce = (IPlayerControlledEntity) selectedEntities.get(0);
         		abilities = pce.getAbilities();
         	}
         	
         	//Loops through every button and sets its attributes
         	for(int i = 0; i<8; i++){
         		Element button = screen.findElementByName("Button_Ability_" + (i+1));
       
         		if(button != null){
         			
     		    	if(abilities != null && i<abilities.size()){
     		    		//button.setVisibleToMouseEvents(true);
     		    		
     		    		button.setVisible(true);
     		    		
     		    	} else {
     		    		button.setVisible(false);
     		    	}
        		}
 
         	}
         }
     };
     
     /**
      * Sets the GUI Control
      * @param guiControl
      */
     public void setGUIControl(GUIControl guiControl){
     	this.guiControl = guiControl;
     }
     
     
     /**
      * Selects an ability
      * @param ability the ability to become selected
      */
     public void selectAbility(IAbility ability){
     	//TODO Afton: Add code to handle ability clicks
     	System.out.println(ability.getName());
     }
 }
