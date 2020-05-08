 package projectrts.controller;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import projectrts.io.ImageManager;
 import projectrts.io.MaterialManager;
 import projectrts.io.TextureManager;
import projectrts.model.Difficulty;
 import projectrts.model.GameModel;
 import projectrts.model.IGame;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.niftygui.NiftyJmeDisplay;
 
 import de.lessvoid.nifty.Nifty;
 
 /**
  * The top-level controller.
  * 
  * Is the connection to jMonkeyEngine (extends SimpleApplication)
  * and handles top-level stuff like swapping AppStates.
  * @author Markus Ekstrm Modifed by Jakob Svensson, Filip Brynfors
  *
  */
 public class AppController extends SimpleApplication implements PropertyChangeListener{
 	private Nifty nifty;
 	private MenuState menuState;
 	private InGameState ingameState;
 	private HighscoreState highscoreState;
 	private IGame game;
 	
     @Override
     public void simpleInitApp() {
     	
 		NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(
 	            getAssetManager(), getInputManager(), getAudioRenderer(), getGuiViewPort());
 	    nifty = niftyDisplay.getNifty();
 	    
 	    getGuiViewPort().addProcessor(niftyDisplay);
 
 	 
 	    nifty.loadStyleFile("nifty-default-styles.xml");
 	    nifty.loadControlFile("nifty-default-controls.xml");
     	
     	this.cam.setParallelProjection(true);
     	TextureManager.INSTANCE.initializeTextures(this);
     	MaterialManager.INSTANCE.initializeMaterial(this);
     	ImageManager.INSTANCE.initializeImages(nifty);
 
     	startMenuState();
         
         // Set logger level
         Logger.getLogger("").setLevel(Level.SEVERE);
          
     }
     
     private void startMenuState(){
     	menuState = new MenuState(nifty);    	
     	menuState.setEnabled(false);
     	menuState.addListener(this);
     	
        	this.stateManager.attach(menuState);
     }
     
     private void startIngameState(Difficulty difficulty){
         game = new GameModel();
         game.addListener(this);
         game.setDifficulty(difficulty);
     	ingameState = new InGameState(game, nifty);
     	this.stateManager.attach(ingameState);
     	ingameState.setEnabled(true);
     }
     
     private void startHighscoreState(float time){
     	highscoreState = new HighscoreState(nifty, time);
     	this.stateManager.attach(highscoreState);
     	highscoreState.setEnabled(true);
     	highscoreState.addListener(this);
     }
 
 	@Override
 	public void propertyChange(PropertyChangeEvent evt) {
 		if(evt.getPropertyName().equals("Start")){
 			menuState.setEnabled(false);
 			getStateManager().detach(menuState);
 			startIngameState((Difficulty)evt.getNewValue());
 			
 		} else if(evt.getPropertyName().equals("gameIsOver")){
 			ingameState.setEnabled(false);
 			getStateManager().detach(ingameState);
 			getRootNode().detachAllChildren();
 			startHighscoreState(game.getGameTime());
 			
 		} else if (evt.getPropertyName().equals("Menu")){
 			highscoreState.setEnabled(false);
 			getStateManager().detach(highscoreState);
 			startMenuState();
 		}
 	}
 }
